const http = require('http');
const fs = require('fs');
const path = require('path');

const activePortFile = path.join(__dirname, '.active-port');
let port = process.env.PORT || 3000;
if (fs.existsSync(activePortFile)) {
  try {
    const p = parseInt(fs.readFileSync(activePortFile, 'utf8').trim(), 10);
    if (!isNaN(p)) port = p;
  } catch {}
}

const token = process.env.TELEMETRY_AUTH_TOKEN || 'igent-telemetry-secret-token-2026';
const url = `http://localhost:${port}/api/live?token=${token}`;

console.log(`========================================`);
console.log(` Igent Telemetry CLI Tailing Viewer      `);
console.log(` Connecting to ${url}...                 `);
console.log(`========================================`);

function getColorForType(type) {
  switch (type) {
    case 'crashes':
    case 'anrs':
      return '\x1b[31m'; // Red
    case 'network':
      return '\x1b[32m'; // Green
    case 'performance':
      return '\x1b[33m'; // Yellow
    case 'logs':
      return '\x1b[36m'; // Cyan
    case 'state':
    case 'lifecycle':
      return '\x1b[35m'; // Magenta
    default:
      return '\x1b[37m'; // White
  }
}

const resetColor = '\x1b[0m';
const httpLib = url.startsWith('https') ? require('https') : http;

function connect() {
  const req = httpLib.get(url, (res) => {
    if (res.statusCode !== 200) {
      console.error(`[ERROR] Connection failed with status ${res.statusCode}`);
      return;
    }

    res.on('data', (chunk) => {
      const lines = chunk.toString().split('\n');
      for (const line of lines) {
        if (line.startsWith('data: ')) {
          try {
            const data = JSON.parse(line.substring(6));
            if (data.type === 'connected') {
              console.log(`[INFO] Connected to telemetry live stream at ${new Date().toLocaleTimeString()}`);
              continue;
            }
            const color = getColorForType(data.type);
            const timeStr = new Date(data.timestamp || data.wallTime || Date.now()).toLocaleTimeString();
            console.log(`${color}[${timeStr}] [${data.type.toUpperCase()}] Dev: ${data.deviceId} | Sess: ${data.sessionId} | Seq: ${data.seq}${resetColor}`);
            console.log(JSON.stringify(data.payload, null, 2));
            console.log('----------------------------------------');
          } catch (e) {
            // ignore parse error or keepalive
          }
        }
      }
    });

    res.on('end', () => {
      console.log('[INFO] Stream ended. Reconnecting in 3 seconds...');
      setTimeout(connect, 3000);
    });
  });

  req.on('error', (err) => {
    console.error(`[ERROR] Connection error: ${err.message}. Retrying in 3 seconds...`);
    setTimeout(connect, 3000);
  });
}

connect();
