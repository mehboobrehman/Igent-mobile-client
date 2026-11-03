const express = require('express');
const app = express();
const PORT = process.env.PORT || 3000;
const TELEMETRY_AUTH_TOKEN = process.env.TELEMETRY_AUTH_TOKEN || 'igent-telemetry-secret-token-2026';

app.use(express.json());

// In-memory storage for logs and sync states
const receivedLogs = [];
const maxStoredLogs = 1000;

const VALID_TYPES = [
  'logs', 'crashes', 'anrs', 'network', 'performance',
  'lifecycle', 'state', 'config', 'commands', 'heartbeats'
];

function validateEnvelope(item) {
  if (!item || typeof item !== 'object') {
    return 'Payload item must be an object';
  }
  if (item.schemaVersion !== '1.0.0') {
    return `Invalid or missing schemaVersion. Expected '1.0.0', got '${item.schemaVersion}'`;
  }
  if (!item.sessionId || typeof item.sessionId !== 'string') {
    return 'Missing or invalid sessionId';
  }
  if (!item.deviceId || typeof item.deviceId !== 'string') {
    return 'Missing or invalid deviceId';
  }
  if (typeof item.seq !== 'number' || item.seq < 0) {
    return 'Missing or invalid seq number';
  }
  if (typeof item.timestamp !== 'number') {
    return 'Missing or invalid timestamp';
  }
  if (typeof item.wallTime !== 'number') {
    return 'Missing or invalid wallTime';
  }
  if (!VALID_TYPES.includes(item.type)) {
    return `Invalid telemetry type '${item.type}'. Must be one of: ${VALID_TYPES.join(', ')}`;
  }
  if (!item.payload || typeof item.payload !== 'object' || Array.isArray(item.payload)) {
    return 'Missing or invalid payload object';
  }
  return null;
}

// Auth token validation middleware
function validateAuthToken(req, res, next) {
  // Allow health check and remote config without token
  if (req.path === '/health' || req.path === '/api/config') {
    return next();
  }
  const tokenHeader = req.headers['x-telemetry-token'];
  const authHeader = req.headers['authorization'];
  let token = tokenHeader;
  if (!token && authHeader && authHeader.startsWith('Bearer ')) {
    token = authHeader.substring(7);
  }
  if (!token && req.query && req.query.token) {
    token = req.query.token;
  }

  if (!token || token !== TELEMETRY_AUTH_TOKEN) {
    const timestamp = new Date().toISOString();
    console.warn(`[${timestamp}] [WARN] Unauthorized request to ${req.originalUrl}`);
    return res.status(401).json({ error: 'Unauthorized: Invalid or missing telemetry auth token' });
  }
  next();
}

app.use(validateAuthToken);

// Request logging middleware
app.use((req, res, next) => {
  const timestamp = new Date().toISOString();
  console.log(`[${timestamp}] ${req.method} ${req.originalUrl}`);
  next();
});

// Health check endpoint
app.get('/health', (req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

// POST /api/telemetry: Receives batch/single versioned telemetry envelopes from mobile client
app.post('/api/telemetry', (req, res) => {
  try {
    const payload = req.body;
    const timestamp = new Date().toISOString();

    const items = Array.isArray(payload) ? payload : [payload];
    
    if (!Array.isArray(payload) && (!payload || typeof payload !== 'object')) {
      return res.status(400).json({ error: 'Invalid telemetry payload format. Expected JSON object or array.' });
    }

    const validatedItems = [];
    for (let i = 0; i < items.length; i++) {
      const item = items[i];
      const errorMsg = validateEnvelope(item);
      if (errorMsg) {
        console.warn(`[${timestamp}] [WARN] Validation failed at index ${i}: ${errorMsg}`);
        return res.status(400).json({ error: `Validation error at index ${i}: ${errorMsg}` });
      }
      validatedItems.push({
        ...item,
        receivedAt: timestamp
      });
    }

    if (Array.isArray(payload)) {
      console.log(`[${timestamp}] Received batch of ${validatedItems.length} valid telemetry envelopes.`);
    } else {
      console.log(`[${timestamp}] Received single valid telemetry envelope of type: ${validatedItems[0].type}`);
    }

    receivedLogs.push(...validatedItems);

    // Trim logs array if it exceeds max capacity
    if (receivedLogs.length > maxStoredLogs) {
      receivedLogs.splice(0, receivedLogs.length - maxStoredLogs);
    }

    return res.status(200).json({ 
      success: true, 
      message: 'Telemetry stored successfully',
      storedCount: receivedLogs.length 
    });
  } catch (err) {
    console.error('Error processing telemetry:', err);
    return res.status(500).json({ error: 'Internal server error processing telemetry' });
  }
});

// GET /api/telemetry: Inspect received logs (helpful for debugging)
app.get('/api/telemetry', (req, res) => {
  const limit = parseInt(req.query.limit) || 100;
  res.json({
    totalStored: receivedLogs.length,
    logs: receivedLogs.slice(-limit).reverse()
  });
});

// GET /api/config: Returns dynamic feature flags and remote config
app.get('/api/config', (req, res) => {
  const config = {
    telemetryEnabled: true,
    forceError: false,
    syncIntervalMinutes: 15,
    minSupportedVersion: "1.0.0",
    features: {
      aiAssistant: true,
      psxTicker: true,
      rssFeed: true,
      pushNotifications: true
    },
    announcement: "Welcome to Igent Mobile! All systems operational.",
    serverTime: new Date().toISOString()
  };
  res.json(config);
});

// POST /api/sync: Endpoint for client sync requests
app.post('/api/sync', (req, res) => {
  try {
    const clientState = req.body || {};
    const timestamp = new Date().toISOString();
    console.log(`[${timestamp}] Received client sync request:`, JSON.stringify(clientState));

    const syncResponse = {
      success: true,
      serverTimestamp: timestamp,
      syncId: "sync_" + Date.now(),
      updates: {
        newConfigAvailable: false,
        pendingMessagesCount: 0,
        serverStatus: "healthy"
      }
    };

    return res.status(200).json(syncResponse);
  } catch (err) {
    console.error('Error processing sync:', err);
    return res.status(500).json({ error: 'Internal server error during sync' });
  }
});

// Start server with robust port fallback mechanism
const net = require('net');
const fs = require('fs');
const path = require('path');

const requestedPort = parseInt(process.env.PORT, 10) || 3000;

function checkPort(port) {
  return new Promise((resolve) => {
    const tester = net.createServer()
      .once('error', (err) => {
        if (err.code === 'EADDRINUSE') {
          resolve(false);
        } else {
          resolve(false);
        }
      })
      .once('listening', () => {
        tester.close(() => {
          resolve(true);
        });
      })
      .listen(port);
  });
}

async function findAvailablePort(startPort) {
  let port = startPort;
  const maxPort = startPort + 100;
  while (port <= maxPort) {
    const available = await checkPort(port);
    if (available) {
      return port;
    }
    console.log(`[INFO] Port ${port} is already in use.`);
    port++;
  }
  throw new Error(`No available ports found between ${startPort} and ${maxPort}`);
}

async function startServer() {
  try {
    const activePort = await findAvailablePort(requestedPort);
    if (activePort !== requestedPort) {
      console.log(`[INFO] Requested port ${requestedPort} was busy. Fallback port ${activePort} selected.`);
    }

    const server = app.listen(activePort, () => {
      console.log(`========================================`);
      console.log(` Igent Telemetry & Remote Config Server `);
      console.log(` Running on active port: ${activePort}   `);
      console.log(` Endpoints:                             `);
      console.log(`   POST /api/telemetry                  `);
      console.log(`   GET  /api/config                     `);
      console.log(`   POST /api/sync                       `);
      console.log(`========================================`);

      try {
        const activePortFilePath = path.join(__dirname, '.active-port');
        fs.writeFileSync(activePortFilePath, String(activePort), 'utf8');
        console.log(`[INFO] Wrote active port ${activePort} to ${activePortFilePath}`);
      } catch (fileErr) {
        console.error('[ERROR] Failed to write .active-port file:', fileErr);
      }
    });

    module.exports = server;
  } catch (err) {
    console.error('[ERROR] Failed to start telemetry server:', err);
    process.exit(1);
  }
}

// Only start server if run directly (not required in tests)
if (require.main === module) {
  startServer();
} else {
  module.exports = { app, startServer, validateEnvelope };
}
