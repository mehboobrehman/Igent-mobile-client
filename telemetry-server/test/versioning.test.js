const test = require('node:test');
const assert = require('node:assert');
const { EntityVersioningSystem, OptimisticLockError, VersionNotFoundError } = require('../versioning/entity-versioning.js');

test('EntityVersioning: Create entity and verify initial version 1', () => {
  const versioning = new EntityVersioningSystem(':memory:');
  try {
    const entity = versioning.createEntity('config_1', 'remote_config', { theme: 'dark', timeout: 30 }, 'admin', 'Initial setup');
    assert.strictEqual(entity.id, 'config_1');
    assert.strictEqual(entity.version, 1);
    assert.deepStrictEqual(entity.state, { theme: 'dark', timeout: 30 });
    assert.strictEqual(entity.updated_by, 'admin');

    const fetched = versioning.getEntity('config_1');
    assert.strictEqual(fetched.version, 1);
    assert.deepStrictEqual(fetched.state, { theme: 'dark', timeout: 30 });
  } finally {
    versioning.close();
  }
});

test('EntityVersioning: Automatic version increment and history tracking on updates', () => {
  const versioning = new EntityVersioningSystem(':memory:');
  try {
    versioning.createEntity('config_2', 'remote_config', { v: 1 }, 'user1', 'v1');
    
    const updated = versioning.updateEntity('config_2', { v: 2 }, 1, 'user2', 'Upgrade to v2');
    assert.strictEqual(updated.version, 2);
    assert.deepStrictEqual(updated.state, { v: 2 });

    const current = versioning.getEntity('config_2');
    assert.strictEqual(current.version, 2);

    const history = versioning.getHistory('config_2');
    assert.strictEqual(history.length, 2);
    assert.strictEqual(history[0].version, 1);
    assert.deepStrictEqual(history[0].state, { v: 1 });
    assert.strictEqual(history[0].changedBy, 'user1');
    assert.strictEqual(history[0].changeReason, 'v1');

    assert.strictEqual(history[1].version, 2);
    assert.deepStrictEqual(history[1].state, { v: 2 });
    assert.strictEqual(history[1].changedBy, 'user2');
    assert.strictEqual(history[1].changeReason, 'Upgrade to v2');
  } finally {
    versioning.close();
  }
});

test('EntityVersioning: Optimistic locking and concurrency handling prevents lost updates', () => {
  const versioning = new EntityVersioningSystem(':memory:');
  try {
    versioning.createEntity('config_conc', 'settings', { count: 10 }, 'system', 'init');

    // Simulate concurrent thread A updating from version 1
    const resA = versioning.updateEntity('config_conc', { count: 15 }, 1, 'threadA', 'Update A');
    assert.strictEqual(resA.version, 2);

    // Simulate concurrent thread B attempting to update from stale version 1
    assert.throws(() => {
      versioning.updateEntity('config_conc', { count: 20 }, 1, 'threadB', 'Update B stale');
    }, (err) => {
      assert.strictEqual(err instanceof OptimisticLockError, true);
      assert.strictEqual(err.expectedVersion, 1);
      assert.strictEqual(err.currentVersion, 2);
      return true;
    });

    // Thread B successfully updates using correct current version 2
    const resB = versioning.updateEntity('config_conc', { count: 20 }, 2, 'threadB', 'Update B correct');
    assert.strictEqual(resB.version, 3);
  } finally {
    versioning.close();
  }
});

test('EntityVersioning: Time-Travel Querying (asOfVersion and asOfTimestamp)', async () => {
  const versioning = new EntityVersioningSystem(':memory:');
  try {
    versioning.createEntity('time_ent', 'doc', { step: 1 }, 'author', 'first');
    const rec1 = versioning.asOfVersion('time_ent', 1);
    const t1 = rec1.createdAt;

    await new Promise(r => setTimeout(r, 15));
    versioning.updateEntity('time_ent', { step: 2 }, 1, 'author', 'second');
    const rec2 = versioning.asOfVersion('time_ent', 2);
    const t2 = rec2.createdAt;

    await new Promise(r => setTimeout(r, 15));
    versioning.updateEntity('time_ent', { step: 3 }, 2, 'author', 'third');

    // Test asOfVersion
    const v1State = versioning.asOfVersion('time_ent', 1);
    assert.strictEqual(v1State.version, 1);
    assert.deepStrictEqual(v1State.state, { step: 1 });

    const v2State = versioning.asOfVersion('time_ent', 2);
    assert.strictEqual(v2State.version, 2);
    assert.deepStrictEqual(v2State.state, { step: 2 });

    // Test asOfTimestamp using exact recorded createdAt timestamps
    const timePast = versioning.asOfTimestamp('time_ent', t1);
    assert.strictEqual(timePast.version, 1);
    assert.deepStrictEqual(timePast.state, { step: 1 });

    const timeMid = versioning.asOfTimestamp('time_ent', t2);
    assert.strictEqual(timeMid.version, 2);
    assert.deepStrictEqual(timeMid.state, { step: 2 });

    // Test non-existent version throws VersionNotFoundError
    assert.throws(() => {
      versioning.asOfVersion('time_ent', 99);
    }, (err) => err instanceof VersionNotFoundError);
  } finally {
    versioning.close();
  }
});

test('EntityVersioning: Rollback capability reverts to prior version and creates new version', () => {
  const versioning = new EntityVersioningSystem(':memory:');
  try {
    versioning.createEntity('rollback_ent', 'profile', { role: 'guest' }, 'admin', 'v1');
    versioning.updateEntity('rollback_ent', { role: 'user' }, 1, 'admin', 'v2');
    versioning.updateEntity('rollback_ent', { role: 'admin' }, 2, 'admin', 'v3');

    assert.strictEqual(versioning.getEntity('rollback_ent').version, 3);
    assert.deepStrictEqual(versioning.getEntity('rollback_ent').state, { role: 'admin' });

    // Rollback to version 1
    const rollbackRes = versioning.rollback('rollback_ent', 1, 'supervisor', 'Revert to guest due to security review');
    assert.strictEqual(rollbackRes.version, 4);
    assert.deepStrictEqual(rollbackRes.state, { role: 'guest' });

    const history = versioning.getHistory('rollback_ent');
    assert.strictEqual(history.length, 4);
    assert.strictEqual(history[3].version, 4);
    assert.deepStrictEqual(history[3].state, { role: 'guest' });
    assert.strictEqual(history[3].changeReason, 'Revert to guest due to security review');
  } finally {
    versioning.close();
  }
});

test('EntityVersioning: Diff generation between versions', () => {
  const versioning = new EntityVersioningSystem(':memory:');
  try {
    versioning.createEntity('diff_ent', 'settings', { a: 1, b: 2, c: 3 }, 'system', 'v1');
    versioning.updateEntity('diff_ent', { a: 1, b: 20, d: 4 }, 1, 'system', 'v2');

    const diff = versioning.getDiff('diff_ent', 1, 2);
    assert.strictEqual(diff.versionA, 1);
    assert.strictEqual(diff.versionB, 2);
    assert.deepStrictEqual(diff.diff.unchanged, { a: 1 });
    assert.deepStrictEqual(diff.diff.removed, { c: 3 });
    assert.deepStrictEqual(diff.diff.added, { d: 4 });
    assert.deepStrictEqual(diff.diff.modified.b, { from: 2, to: 20 });
  } finally {
    versioning.close();
  }
});
