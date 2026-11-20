/**
 * @file entity-versioning.js
 * @description Robust Entity Versioning and History Tracking System using SQLite (node:sqlite).
 * 
 * ## 1. Versioning Strategy: Snapshot-Based Versioning
 * 
 * **Strategy Chosen**: Snapshot-Based Versioning.
 * 
 * **Architectural Justification**:
 * - **Read Performance (O(1))**: Retrieving any historical version or performing time-travel queries is a direct lookup against the immutable history table (`entity_history`) without needing to replay delta/patch chains from version 1.
 * - **Reliability & Simplicity**: Eliminates the risk of cascade failures or corrupted delta chains where a missing patch breaks all subsequent versions.
 * - **Storage Efficiency**: For configuration entities, state objects, and telemetry metadata (typically ranging from a few hundred bytes to a few kilobytes), storing full JSON snapshots per version is extremely compact. SQLite's page storage and compression handle this with negligible storage overhead.
 * - **Immutability & Audit Integrity**: Historical records are strictly append-only and immutable. Rollbacks are straightforward: reverting to version N simply takes snapshot N and creates a new version N+1 from it, preserving a complete, unbroken audit trail.
 */

const { DatabaseSync } = require('node:sqlite');
const path = require('path');
const fs = require('fs');

class OptimisticLockError extends Error {
  constructor(message, currentVersion, expectedVersion) {
    super(message);
    this.name = 'OptimisticLockError';
    this.currentVersion = currentVersion;
    this.expectedVersion = expectedVersion;
  }
}

class VersionNotFoundError extends Error {
  constructor(message) {
    super(message);
    this.name = 'VersionNotFoundError';
  }
}

class EntityVersioningSystem {
  constructor(dbPath = ':memory:') {
    if (dbPath !== ':memory:') {
      const dir = path.dirname(dbPath);
      if (!fs.existsSync(dir)) {
        fs.mkdirSync(dir, { recursive: true });
      }
    }
    this.db = new DatabaseSync(dbPath);
    this.initSchema();
  }

  initSchema() {
    // Main entity table holding current state and monotonically increasing version
    this.db.exec(`
      CREATE TABLE IF NOT EXISTS entities (
        id TEXT PRIMARY KEY,
        type TEXT NOT NULL,
        state TEXT NOT NULL,
        version INTEGER NOT NULL DEFAULT 1,
        updated_at INTEGER NOT NULL,
        updated_by TEXT
      );
    `);

    // Immutable append-only history/audit table
    this.db.exec(`
      CREATE TABLE IF NOT EXISTS entity_history (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        entity_id TEXT NOT NULL,
        version INTEGER NOT NULL,
        snapshot TEXT NOT NULL,
        changed_by TEXT,
        change_reason TEXT,
        created_at INTEGER NOT NULL,
        UNIQUE(entity_id, version)
      );
    `);

    // Indexes for fast version and time-travel lookup
    this.db.exec(`
      CREATE INDEX IF NOT EXISTS idx_entity_history_lookup ON entity_history(entity_id, version);
      CREATE INDEX IF NOT EXISTS idx_entity_history_time ON entity_history(entity_id, created_at);
    `);
  }

  /**
   * Create a new entity with version 1 and record the initial snapshot in audit history.
   */
  createEntity(id, type, stateObj, changedBy = 'system', changeReason = 'Initial creation') {
    const now = Date.now();
    const stateStr = JSON.stringify(stateObj);

    const insertEntity = this.db.prepare(`
      INSERT INTO entities (id, type, state, version, updated_at, updated_by)
      VALUES (?, ?, ?, 1, ?, ?)
    `);

    const insertHistory = this.db.prepare(`
      INSERT INTO entity_history (entity_id, version, snapshot, changed_by, change_reason, created_at)
      VALUES (?, 1, ?, ?, ?, ?)
    `);

    // Execute in transaction
    this.db.exec('BEGIN TRANSACTION;');
    try {
      insertEntity.run(id, type, stateStr, now, changedBy);
      insertHistory.run(id, stateStr, changedBy, changeReason, now);
      this.db.exec('COMMIT;');
      return { id, type, state: stateObj, version: 1, updated_at: now, updated_by: changedBy };
    } catch (err) {
      this.db.exec('ROLLBACK;');
      throw err;
    }
  }

  /**
   * Update an entity with optimistic locking and automatic version creation.
   * Throws OptimisticLockError if expectedVersion does not match current version.
   */
  updateEntity(id, newStateObj, expectedVersion, changedBy = 'system', changeReason = 'Update') {
    const now = Date.now();
    const newStateStr = JSON.stringify(newStateObj);

    const getStmt = this.db.prepare('SELECT version FROM entities WHERE id = ?');
    const updateStmt = this.db.prepare(`
      UPDATE entities
      SET state = ?, version = version + 1, updated_at = ?, updated_by = ?
      WHERE id = ? AND version = ?
    `);
    const historyStmt = this.db.prepare(`
      INSERT INTO entity_history (entity_id, version, snapshot, changed_by, change_reason, created_at)
      VALUES (?, ?, ?, ?, ?, ?)
    `);

    this.db.exec('BEGIN TRANSACTION;');
    try {
      const row = getStmt.get(id);
      if (!row) {
        this.db.exec('ROLLBACK;');
        throw new Error(`Entity with id ${id} not found.`);
      }

      if (row.version !== expectedVersion) {
        this.db.exec('ROLLBACK;');
        throw new OptimisticLockError(
          `Optimistic locking conflict: expected version ${expectedVersion}, but current version is ${row.version}`,
          row.version,
          expectedVersion
        );
      }

      const newVersion = row.version + 1;
      const res = updateStmt.run(newStateStr, now, changedBy, id, expectedVersion);
      if (res.changes === 0) {
        this.db.exec('ROLLBACK;');
        throw new OptimisticLockError('Concurrent update detected during execution.', row.version, expectedVersion);
      }

      historyStmt.run(id, newVersion, newStateStr, changedBy, changeReason, now);
      this.db.exec('COMMIT;');

      return { id, state: newStateObj, version: newVersion, updated_at: now, updated_by: changedBy };
    } catch (err) {
      if (this.db.inTransaction) {
        this.db.exec('ROLLBACK;');
      }
      throw err;
    }
  }

  /**
   * Retrieve current entity state.
   */
  getEntity(id) {
    const stmt = this.db.prepare('SELECT id, type, state, version, updated_at, updated_by FROM entities WHERE id = ?');
    const row = stmt.get(id);
    if (!row) return null;
    return {
      ...row,
      state: JSON.parse(row.state)
    };
  }

  /**
   * Time-Travel Query: Get entity state as of a specific version number.
   */
  asOfVersion(id, version) {
    const stmt = this.db.prepare(`
      SELECT entity_id, version, snapshot, changed_by, change_reason, created_at
      FROM entity_history
      WHERE entity_id = ? AND version = ?
    `);
    const row = stmt.get(id, version);
    if (!row) {
      throw new VersionNotFoundError(`Entity ${id} version ${version} not found in history.`);
    }
    return {
      entityId: row.entity_id,
      version: row.version,
      state: JSON.parse(row.snapshot),
      changedBy: row.changed_by,
      changeReason: row.change_reason,
      createdAt: row.created_at
    };
  }

  /**
   * Time-Travel Query: Get entity state as of a specific timestamp (point-in-time recovery).
   */
  asOfTimestamp(id, timestamp) {
    const stmt = this.db.prepare(`
      SELECT entity_id, version, snapshot, changed_by, change_reason, created_at
      FROM entity_history
      WHERE entity_id = ? AND created_at <= ?
      ORDER BY version DESC
      LIMIT 1
    `);
    const row = stmt.get(id, timestamp);
    if (!row) {
      throw new VersionNotFoundError(`No history found for entity ${id} at or before timestamp ${timestamp}.`);
    }
    return {
      entityId: row.entity_id,
      version: row.version,
      state: JSON.parse(row.snapshot),
      changedBy: row.changed_by,
      changeReason: row.change_reason,
      createdAt: row.created_at
    };
  }

  /**
   * Rollback Capability: Revert entity to any prior version, generating a new version representing restoration.
   */
  rollback(id, targetVersion, changedBy = 'system', changeReason = null) {
    // 1. Get snapshot at target version
    const pastRecord = this.asOfVersion(id, targetVersion);
    const currentEntity = this.getEntity(id);
    if (!currentEntity) {
      throw new Error(`Entity ${id} not found for rollback.`);
    }

    const reason = changeReason || `Rollback to version ${targetVersion}`;
    // 2. Update entity with past snapshot state using current version for optimistic locking safety
    return this.updateEntity(id, pastRecord.state, currentEntity.version, changedBy, reason);
  }

  /**
   * Retrieve full audit history log for an entity.
   */
  getHistory(id) {
    const stmt = this.db.prepare(`
      SELECT entity_id, version, snapshot, changed_by, change_reason, created_at
      FROM entity_history
      WHERE entity_id = ?
      ORDER BY version ASC
    `);
    const rows = stmt.all(id);
    return rows.map(r => ({
      entityId: r.entity_id,
      version: r.version,
      state: JSON.parse(r.snapshot),
      changedBy: r.changed_by,
      changeReason: r.change_reason,
      createdAt: r.created_at
    }));
  }

  /**
   * Compare two versions of an entity and return diff summary.
   */
  getDiff(id, versionA, versionB) {
    const recA = this.asOfVersion(id, versionA);
    const recB = this.asOfVersion(id, versionB);

    const stateA = recA.state;
    const stateB = recB.state;

    const added = {};
    const removed = {};
    const modified = {};
    const unchanged = {};

    const allKeys = new Set([...Object.keys(stateA), ...Object.keys(stateB)]);
    for (const key of allKeys) {
      const valA = stateA[key];
      const valB = stateB[key];

      if (!(key in stateA)) {
        added[key] = valB;
      } else if (!(key in stateB)) {
        removed[key] = valA;
      } else if (JSON.stringify(valA) !== JSON.stringify(valB)) {
        modified[key] = { from: valA, to: valB };
      } else {
        unchanged[key] = valA;
      }
    }

    return {
      entityId: id,
      versionA,
      versionB,
      diff: {
        added,
        removed,
        modified,
        unchanged
      }
    };
  }

  close() {
    this.db.close();
  }
}

module.exports = {
  EntityVersioningSystem,
  OptimisticLockError,
  VersionNotFoundError
};
