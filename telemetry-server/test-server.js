const { spawn } = require('child_process');
const fs = require('fs');
const path = require('path');
const http = require('http');
const net = require('net');

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

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

async function runTests() {
  console.log('=== Starting Telemetry Server Port Fallback & Startup Tests ===');
  const serverDir = __dirname;
  const activePortFile = path.join(serverDir, '.active-port');

  // Clean up any existing .active-port file
  if (fs.existsSync(activePortFile)) {
    fs.unlinkSync(activePortFile);
  }

  // Test Scenario: Occupy test port 3100 to force fallback to 3101
  const testPort = 3100;
  console.log(`[TEST] Occupying port ${testPort} to test fallback...`);
  const blocker = net.createServer();
  await new Promise((resolve, reject) => {
    blocker.once('error', reject);
    blocker.listen(testPort, resolve);
  });
  console.log(`[TEST] Port ${testPort} successfully blocked by test harness.`);

  // Start server.js with PORT=3100
  const serverProcess = spawn('node', ['server.js'], {
    cwd: serverDir,
    env: { ...process.env, PORT: String(testPort) },
    stdio: ['ignore', 'pipe', 'pipe']
  });

  let serverOutput = '';
  serverProcess.stdout.on('data', (data) => {
    serverOutput += data.toString();
    process.stdout.write(`[SERVER STDOUT] ${data}`);
  });
  serverProcess.stderr.on('data', (data) => {
    process.stderr.write(`[SERVER STDERR] ${data}`);
  });

  // Wait for server to start and write .active-port
  let activePort = null;
  for (let i = 0; i < 25; i++) {
    await sleep(400);
    if (fs.existsSync(activePortFile)) {
      activePort = parseInt(fs.readFileSync(activePortFile, 'utf8').trim(), 10);
      if (!isNaN(activePort)) break;
    }
  }

  try {
    if (!activePort) {
      throw new Error('Server failed to write .active-port file within timeout.');
    }

    console.log(`[TEST] Detected active port from .active-port file: ${activePort}`);
    if (activePort === testPort) {
      throw new Error(`Fallback failed: Server bound to occupied port ${testPort} instead of falling back!`);
    }
    console.log(`[TEST] PASS: Port fallback successfully selected port ${activePort} (avoided ${testPort}).`);

    // Test health endpoint
    console.log('[TEST] Testing GET /health...');
    const healthRes = await httpRequest(`http://localhost:${activePort}/health`);
    console.log('Health response:', healthRes);
    if (healthRes.status !== 200 || healthRes.body.status !== 'ok') {
      throw new Error(`Health check failed: status ${healthRes.status}`);
    }

    // Test config endpoint
    console.log('[TEST] Testing GET /api/config...');
    const configRes = await httpRequest(`http://localhost:${activePort}/api/config`);
    if (configRes.status !== 200 || !configRes.body.features) {
      throw new Error(`Config check failed: status ${configRes.status}`);
    }

    // Test telemetry POST endpoint
    console.log('[TEST] Testing POST /api/telemetry...');
    const telemetryRes = await httpRequest(`http://localhost:${activePort}/api/telemetry`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' }
    }, { event: 'test_log', message: 'Hello from test suite' });
    if (telemetryRes.status !== 200 || !telemetryRes.body.success) {
      throw new Error(`Telemetry POST failed: status ${telemetryRes.status}`);
    }

    // Test sync POST endpoint
    console.log('[TEST] Testing POST /api/sync...');
    const syncRes = await httpRequest(`http://localhost:${activePort}/api/sync`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' }
    }, { clientVersion: '1.0.0' });
    if (syncRes.status !== 200 || !syncRes.body.success) {
      throw new Error(`Sync POST failed: status ${syncRes.status}`);
    }

    console.log('=== ALL TELEMETRY SERVER TESTS PASSED SUCCESSFULLY! ===');
  } finally {
    // Cleanup
    serverProcess.kill('SIGINT');
    blocker.close();
    if (fs.existsSync(activePortFile)) {
      fs.unlinkSync(activePortFile);
    }
  }
}

runTests().catch(err => {
  console.error('[TEST ERROR]', err);
  process.exit(1);
});
