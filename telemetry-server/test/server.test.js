const test = require('node:test');
const assert = require('node:assert');
const http = require('http');
const { startServer, validateEnvelope } = require('../server.js');

async function httpRequest(url, options = {}, data = null) {
  return new Promise((resolve, reject) => {
    const lib = url.startsWith('https') ? require('https') : http;
    const req = lib.request(url, options, (res) => {
      let body = '';
      res.on('data', chunk => body += chunk);
      res.on('end', () => {
        try {
          resolve({ status: res.statusCode, headers: res.headers, body: JSON.parse(body) });
        } catch {
          resolve({ status: res.statusCode, headers: res.headers, body });
        }
      });
    });
    req.on('error', reject);
    if (data) {
      req.write(typeof data === 'string' ? data : JSON.stringify(data));
    }
    req.end();
  });
}

test('validateEnvelope validates correct envelope', () => {
  const validItem = {
    schemaVersion: '1.0.0',
    sessionId: 'session_test_123',
    deviceId: 'device_test_456',
    seq: 1,
    timestamp: Date.now(),
    wallTime: Date.now(),
    type: 'logs',
    payload: { message: 'test log', level: 'INFO', tag: 'Test' }
  };
  const err = validateEnvelope(validItem);
  assert.strictEqual(err, null);
});

test('validateEnvelope rejects invalid schemaVersion', () => {
  const invalidItem = {
    schemaVersion: '2.0.0',
    sessionId: 'session_test_123',
    deviceId: 'device_test_456',
    seq: 1,
    timestamp: Date.now(),
    wallTime: Date.now(),
    type: 'logs',
    payload: { message: 'test log' }
  };
  const err = validateEnvelope(invalidItem);
  assert.notStrictEqual(err, null);
  assert.match(err, /schemaVersion/);
});

test('Integration Test: Telemetry Server Session Persistence, Export & Endpoints', async () => {
  const server = await startServer();
  const address = server.address();
  const port = typeof address === 'object' && address ? address.port : 3000;
  const token = 'igent-telemetry-secret-token-2026';
  const headers = {
    'Content-Type': 'application/json',
    'x-telemetry-token': token
  };

  try {
    const health = await httpRequest(`http://localhost:${port}/health`);
    assert.strictEqual(health.status, 200);
    assert.strictEqual(health.body.status, 'ok');

    const dashboard = await httpRequest(`http://localhost:${port}/`);
    assert.strictEqual(dashboard.status, 200);
    assert.match(dashboard.body, /Igent Telemetry Collector/);

    const testSessionId = 'sess_integration_' + Date.now();
    const envelopes = [
      {
        schemaVersion: '1.0.0',
        sessionId: testSessionId,
        deviceId: 'dev_integ_1',
        seq: 1,
        timestamp: Date.now(),
        wallTime: Date.now(),
        type: 'logs',
        payload: { level: 'INFO', message: 'Startup log' }
      },
      {
        schemaVersion: '1.0.0',
        sessionId: testSessionId,
        deviceId: 'dev_integ_1',
        seq: 2,
        timestamp: Date.now(),
        wallTime: Date.now(),
        type: 'network',
        payload: { method: 'GET', url: 'https://api.example.com/data', statusCode: 200, durationMs: 120, requestSize: 10, responseSize: 500 }
      },
      {
        schemaVersion: '1.0.0',
        sessionId: testSessionId,
        deviceId: 'dev_integ_1',
        seq: 3,
        timestamp: Date.now(),
        wallTime: Date.now(),
        type: 'crashes',
        payload: { exception: 'NullPointerException', fatal: true }
      }
    ];

    const postRes = await httpRequest(`http://localhost:${port}/api/telemetry`, {
      method: 'POST',
      headers
    }, envelopes);

    assert.strictEqual(postRes.status, 200);
    assert.strictEqual(postRes.body.success, true);

    const sessionsRes = await httpRequest(`http://localhost:${port}/api/sessions?token=${token}`);
    assert.strictEqual(sessionsRes.status, 200);
    assert.strictEqual(sessionsRes.body.totalSessions > 0, true);
    const foundSess = sessionsRes.body.sessions.find(s => s.sessionId === testSessionId);
    assert.notStrictEqual(foundSess, undefined);
    assert.strictEqual(foundSess.eventCount, 3);
    assert.strictEqual(foundSess.errorCount, 1);

    const detailRes = await httpRequest(`http://localhost:${port}/api/sessions/${testSessionId}?token=${token}`);
    assert.strictEqual(detailRes.status, 200);
    assert.strictEqual(detailRes.body.sessionId, testSessionId);
    assert.strictEqual(detailRes.body.events.length, 3);

    const exportJsonRes = await httpRequest(`http://localhost:${port}/api/sessions/${testSessionId}/export?format=json&token=${token}`);
    assert.strictEqual(exportJsonRes.status, 200);
    assert.strictEqual(exportJsonRes.body.sessionId, testSessionId);
    assert.strictEqual(Array.isArray(exportJsonRes.body.events), true);

    const exportHarRes = await httpRequest(`http://localhost:${port}/api/sessions/${testSessionId}/export?format=har&token=${token}`);
    assert.strictEqual(exportHarRes.status, 200);
    assert.notStrictEqual(exportHarRes.body.log, undefined);
    assert.strictEqual(exportHarRes.body.log.version, '1.2');
    assert.strictEqual(Array.isArray(exportHarRes.body.log.entries), true);
    assert.strictEqual(exportHarRes.body.log.entries.length, 3);

    const deleteRes = await httpRequest(`http://localhost:${port}/api/sessions/${testSessionId}?token=${token}`, {
      method: 'DELETE',
      headers
    });
    assert.strictEqual(deleteRes.status, 200);
    assert.strictEqual(deleteRes.body.success, true);

    const verifyDeleted = await httpRequest(`http://localhost:${port}/api/sessions/${testSessionId}?token=${token}`);
    assert.strictEqual(verifyDeleted.status, 404);

  } finally {
    await new Promise((resolve) => server.close(resolve));
  }
});
