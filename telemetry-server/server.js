const express = require('express');
const app = express();
const PORT = process.env.PORT || 8080;

app.use(express.json());

// In-memory storage for logs and sync states
const receivedLogs = [];
const maxStoredLogs = 1000;

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

// POST /api/telemetry: Receives batch/single logs from mobile client
app.post('/api/telemetry', (req, res) => {
  try {
    const payload = req.body;
    const timestamp = new Date().toISOString();

    if (Array.isArray(payload)) {
      console.log(`[${timestamp}] Received batch of ${payload.length} telemetry logs.`);
      payload.forEach(log => {
        receivedLogs.push({ ...log, receivedAt: timestamp });
      });
    } else if (payload && typeof payload === 'object') {
      console.log(`[${timestamp}] Received single telemetry log:`, JSON.stringify(payload));
      receivedLogs.push({ ...payload, receivedAt: timestamp });
    } else {
      return res.status(400).json({ error: 'Invalid telemetry payload format. Expected JSON object or array.' });
    }

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

    // Perform mock synchronization logic or return sync updates
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

// Start server
app.listen(PORT, () => {
  console.log(`========================================`);
  console.log(` Igent Telemetry & Remote Config Server `);
  console.log(` Running on port ${PORT}                 `);
  console.log(` Endpoints:                             `);
  console.log(`   POST /api/telemetry                  `);
  console.log(`   GET  /api/config                     `);
  console.log(`   POST /api/sync                       `);
  console.log(`========================================`);
});
