const test = require('node:test');
const assert = require('node:assert');
const { validateEnvelope } = require('../server.js');

test('validateEnvelope validates correct envelope', () => {
  const validItem = {
    schemaVersion: '1.0.0',
    sessionId: 'session_123',
    deviceId: 'device_456',
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
    sessionId: 'session_123',
    deviceId: 'device_456',
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

test('validateEnvelope rejects invalid type', () => {
  const invalidItem = {
    schemaVersion: '1.0.0',
    sessionId: 'session_123',
    deviceId: 'device_456',
    seq: 1,
    timestamp: Date.now(),
    wallTime: Date.now(),
    type: 'unknown_type',
    payload: { message: 'test log' }
  };
  const err = validateEnvelope(invalidItem);
  assert.notStrictEqual(err, null);
  assert.match(err, /Invalid telemetry type/);
});

test('validateEnvelope rejects missing required fields', () => {
  const invalidItem = {
    schemaVersion: '1.0.0',
    sessionId: 'session_123'
    // missing deviceId, seq, etc.
  };
  const err = validateEnvelope(invalidItem);
  assert.notStrictEqual(err, null);
});
