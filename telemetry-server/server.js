const express = require('express');
const app = express();
const TELEMETRY_AUTH_TOKEN = process.env.TELEMETRY_AUTH_TOKEN || 'igent-telemetry-secret-token-2026';

const fs = require('fs');
const path = require('path');
const net = require('net');

app.use(express.json());

// In-memory storage for logs and sessions
const receivedLogs = [];
const maxStoredLogs = 2000;
const sseClients = new Set();

const sessionsDir = path.join(__dirname, 'sessions');
if (!fs.existsSync(sessionsDir)) {
  fs.mkdirSync(sessionsDir, { recursive: true });
}

const sessionsMap = new Map(); // sessionId -> session object

function createSessionSummary(sessionId, events) {
  let deviceId = 'unknown';
  let startTime = Infinity;
  let endTime = -Infinity;
  let errorCount = 0;
  const types = {};
  const snapshots = [];
  const timelineMarkers = [];

  for (const ev of events) {
    if (ev.deviceId) deviceId = ev.deviceId;
    const t = ev.timestamp || ev.wallTime || Date.now();
    if (t < startTime) startTime = t;
    if (t > endTime) endTime = t;

    if (ev.type === 'crashes' || ev.type === 'anrs') {
      errorCount++;
      timelineMarkers.push({
        timestamp: t,
        type: ev.type,
        title: `${ev.type.toUpperCase()}: ${ev.payload?.exceptionClass || 'Error'}`,
        detail: ev.payload?.message || ev.payload?.stackTrace || '',
        event: ev
      });
    } else if (ev.type === 'snapshots') {
      snapshots.push(ev);
      timelineMarkers.push({
        timestamp: t,
        type: 'snapshots',
        title: `Snapshot: ${ev.payload?.triggerCategory || 'Trigger'} (${ev.payload?.triggerId || 'unknown'})`,
        detail: `Occurrence: ${ev.payload?.occurrenceCount || 1}`,
        event: ev
      });
    } else if (ev.type === 'logs' && ev.payload && (ev.payload.level === 'ERROR' || ev.payload.level === 'FATAL')) {
      errorCount++;
      timelineMarkers.push({
        timestamp: t,
        type: 'error_log',
        title: `Error Log: ${ev.payload.tag}`,
        detail: ev.payload.message,
        event: ev
      });
    }

    types[ev.type] = (types[ev.type] || 0) + 1;
  }

  timelineMarkers.sort((a, b) => a.timestamp - b.timestamp);
  snapshots.sort((a, b) => (a.timestamp || 0) - (b.timestamp || 0));

  if (startTime === Infinity) startTime = Date.now();
  if (endTime === -Infinity) endTime = startTime;

  return {
    sessionId,
    deviceId,
    startTime,
    endTime,
    eventCount: events.length,
    errorCount,
    types,
    snapshots,
    timelineMarkers,
    events
  };
}

function loadExistingSessions() {
  try {
    if (!fs.existsSync(sessionsDir)) return;
    const files = fs.readdirSync(sessionsDir);
    for (const file of files) {
      if (file.endsWith('.jsonl')) {
        const sessionId = file.replace('.jsonl', '');
        const filePath = path.join(sessionsDir, file);
        const content = fs.readFileSync(filePath, 'utf8');
        const lines = content.split('\n');
        const events = [];
        for (const line of lines) {
          if (!line.trim()) continue;
          try {
            const envelope = JSON.parse(line);
            events.push(envelope);
            receivedLogs.push(envelope);
          } catch (e) {
            // ignore corrupted line
          }
        }
        if (events.length > 0) {
          sessionsMap.set(sessionId, createSessionSummary(sessionId, events));
        }
      }
    }
    if (receivedLogs.length > maxStoredLogs) {
      receivedLogs.splice(0, receivedLogs.length - maxStoredLogs);
    }
    console.log(`[INFO] Loaded ${sessionsMap.size} historical sessions from disk (${receivedLogs.length} total events).`);
  } catch (err) {
    console.error('[ERROR] Failed to load existing sessions:', err);
  }
}

function persistEnvelope(envelope) {
  const sessionId = envelope.sessionId;
  const filePath = path.join(sessionsDir, `${sessionId}.jsonl`);
  try {
    fs.appendFileSync(filePath, JSON.stringify(envelope) + '\n', 'utf8');
  } catch (err) {
    console.error(`[ERROR] Failed to persist envelope for session ${sessionId}:`, err);
  }

  let session = sessionsMap.get(sessionId);
  if (!session) {
    session = {
      sessionId,
      deviceId: envelope.deviceId,
      startTime: envelope.timestamp || Date.now(),
      endTime: envelope.timestamp || Date.now(),
      eventCount: 0,
      errorCount: 0,
      types: {},
      snapshots: [],
      timelineMarkers: [],
      events: []
    };
    sessionsMap.set(sessionId, session);
  }

  session.events.push(envelope);
  session.eventCount++;
  const t = envelope.timestamp || envelope.wallTime || Date.now();
  if (t < session.startTime) session.startTime = t;
  if (t > session.endTime) session.endTime = t;
  if (envelope.deviceId) session.deviceId = envelope.deviceId;

  if (envelope.type === 'crashes' || envelope.type === 'anrs') {
    session.errorCount++;
    session.timelineMarkers.push({
      timestamp: t,
      type: envelope.type,
      title: `${envelope.type.toUpperCase()}: ${envelope.payload?.exceptionClass || 'Error'}`,
      detail: envelope.payload?.message || envelope.payload?.stackTrace || '',
      event: envelope
    });
  } else if (envelope.type === 'snapshots') {
    session.snapshots.push(envelope);
    session.timelineMarkers.push({
      timestamp: t,
      type: 'snapshots',
      title: `Snapshot: ${envelope.payload?.triggerCategory || 'Trigger'} (${envelope.payload?.triggerId || 'unknown'})`,
      detail: `Occurrence: ${envelope.payload?.occurrenceCount || 1}`,
      event: envelope
    });
  } else if (envelope.type === 'logs' && envelope.payload && (envelope.payload.level === 'ERROR' || envelope.payload.level === 'FATAL')) {
    session.errorCount++;
    session.timelineMarkers.push({
      timestamp: t,
      type: 'error_log',
      title: `Error Log: ${envelope.payload.tag}`,
      detail: envelope.payload.message,
      event: envelope
    });
  }

  if (envelope.type === 'logpoints' && envelope.payload && envelope.payload.logpointId) {
    const lpId = envelope.payload.logpointId;
    let lp = logpointsMap.get(lpId);
    if (!lp) {
      lp = {
        id: lpId,
        tag: envelope.payload.tag || 'Logpoint',
        defaultLevel: envelope.payload.level || 'INFO',
        callSite: envelope.payload.callSite || '',
        enabled: true,
        level: envelope.payload.level || 'INFO',
        samplingRate: 1.0,
        condition: null,
        hitCount: 0,
        dropCount: 0,
        lastHitTime: t
      };
      logpointsMap.set(lpId, lp);
    }
    lp.hitCount++;
    lp.lastHitTime = t;
  }

  session.types[envelope.type] = (session.types[envelope.type] || 0) + 1;
  session.timelineMarkers.sort((a, b) => a.timestamp - b.timestamp);
  session.snapshots.sort((a, b) => (a.timestamp || 0) - (b.timestamp || 0));
}

// Load historical sessions on startup
loadExistingSessions();

const logpointsMap = new Map();

const VALID_TYPES = [
  'logs', 'crashes', 'anrs', 'network', 'performance',
  'lifecycle', 'state', 'config', 'commands', 'heartbeats', 'snapshots', 'logpoints'
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
  if (req.path === '/health' || req.path === '/api/config' || req.path === '/' || req.path === '/favicon.ico') {
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
app.use((req, _, next) => {
  const timestamp = new Date().toISOString();
  console.log(`[${timestamp}] ${req.method} ${req.originalUrl}`);
  next();
});

// Health check endpoint
app.get('/health', (_, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

// Root: Serve Web UI Dashboard
app.get('/', (_, res) => {
  res.send(`<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Igent Telemetry Collector Dashboard</title>
  <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-slate-900 text-slate-100 min-h-screen">
  <div class="container mx-auto px-4 py-6">
    <!-- Header -->
    <header class="flex flex-col md:flex-row justify-between items-center mb-6 pb-4 border-b border-slate-700">
      <div>
        <h1 class="text-3xl font-bold tracking-tight text-emerald-400 flex items-center gap-2">
          <span>📡</span> Igent Telemetry Collector
        </h1>
        <p class="text-slate-400 text-sm mt-1">Real-time session monitoring, replay, analytics, and export dashboard</p>
      </div>
      <div class="mt-4 md:mt-0 flex items-center gap-3">
        <span id="connection-status" class="px-3 py-1 rounded-full text-xs font-semibold bg-emerald-900 text-emerald-300 border border-emerald-700">Connected</span>
        <input type="password" id="auth-token" placeholder="Auth Token" value="igent-telemetry-secret-token-2026" class="bg-slate-800 border border-slate-700 rounded px-3 py-1.5 text-sm w-48 focus:outline-none focus:border-emerald-500">
        <button onclick="refreshData()" class="bg-emerald-600 hover:bg-emerald-500 text-white px-4 py-1.5 rounded text-sm font-medium transition">Refresh</button>
      </div>
    </header>

    <!-- Stats Cards -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      <div class="bg-slate-800 border border-slate-700 rounded-xl p-4 shadow">
        <div class="text-slate-400 text-xs font-medium uppercase">Total Sessions</div>
        <div id="stat-sessions" class="text-2xl font-bold text-white mt-1">0</div>
      </div>
      <div class="bg-slate-800 border border-slate-700 rounded-xl p-4 shadow">
        <div class="text-slate-400 text-xs font-medium uppercase">Total Events</div>
        <div id="stat-events" class="text-2xl font-bold text-white mt-1">0</div>
      </div>
      <div class="bg-slate-800 border border-slate-700 rounded-xl p-4 shadow">
        <div class="text-slate-400 text-xs font-medium uppercase">Errors / Crashes</div>
        <div id="stat-errors" class="text-2xl font-bold text-red-400 mt-1">0</div>
      </div>
      <div class="bg-slate-800 border border-slate-700 rounded-xl p-4 shadow">
        <div class="text-slate-400 text-xs font-medium uppercase">Active Devices</div>
        <div id="stat-devices" class="text-2xl font-bold text-cyan-400 mt-1">0</div>
      </div>
    </div>

    <!-- Controls / Filters Bar -->
    <div class="bg-slate-800 border border-slate-700 rounded-xl p-4 mb-6 flex flex-wrap gap-4 items-center justify-between">
      <div class="flex flex-wrap gap-3 items-center">
        <div>
          <label class="block text-xs text-slate-400 mb-1">Session</label>
          <select id="filter-session" onchange="filterEvents()" class="bg-slate-900 border border-slate-700 rounded px-3 py-1.5 text-sm text-slate-200">
            <option value="">All Sessions</option>
          </select>
        </div>
        <div>
          <label class="block text-xs text-slate-400 mb-1">Event Type</label>
          <select id="filter-type" onchange="filterEvents()" class="bg-slate-900 border border-slate-700 rounded px-3 py-1.5 text-sm text-slate-200">
            <option value="">All Types</option>
            <option value="logs">logs</option>
            <option value="crashes">crashes</option>
            <option value="anrs">anrs</option>
            <option value="network">network</option>
            <option value="performance">performance</option>
            <option value="lifecycle">lifecycle</option>
            <option value="state">state</option>
            <option value="config">config</option>
            <option value="commands">commands</option>
            <option value="heartbeats">heartbeats</option>
          </select>
        </div>
        <div>
          <label class="block text-xs text-slate-400 mb-1">Search</label>
          <input type="text" id="search-input" oninput="filterEvents()" placeholder="Search logs/payload..." class="bg-slate-900 border border-slate-700 rounded px-3 py-1.5 text-sm text-slate-200">
        </div>
      </div>
      <div class="flex items-center gap-3">
        <button id="btn-tail-toggle" onclick="toggleTail()" class="bg-amber-600 hover:bg-amber-500 text-white px-3 py-1.5 rounded text-sm font-medium transition">Pause Live Tail</button>
        <button onclick="clearLiveFeed()" class="bg-slate-700 hover:bg-slate-600 text-slate-200 px-3 py-1.5 rounded text-sm transition">Clear Feed</button>
      </div>
    </div>

    <!-- Main Grid: Sessions List & Live Feed -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- Sessions Sidebar -->
      <div class="bg-slate-800 border border-slate-700 rounded-xl p-4 flex flex-col h-[600px]">
        <h2 class="text-lg font-semibold mb-3 text-emerald-400 flex justify-between items-center">
          <span>Sessions</span>
          <span id="session-count-badge" class="text-xs bg-slate-700 px-2 py-0.5 rounded-full">0</span>
        </h2>
        <div id="sessions-list" class="overflow-y-auto flex-1 space-y-2 pr-1">
          <!-- Session items inserted dynamically -->
        </div>
      </div>

      <!-- Live Feed / Replay Panel -->
      <div class="lg:col-span-2 bg-slate-800 border border-slate-700 rounded-xl p-4 flex flex-col h-[600px]">
        <div class="flex justify-between items-center mb-3">
          <h2 id="feed-title" class="text-lg font-semibold text-emerald-400">Live Telemetry Feed</h2>
          <div id="export-buttons" class="hidden flex gap-2">
            <button onclick="exportCurrentSession('json')" class="bg-indigo-600 hover:bg-indigo-500 text-white px-3 py-1 rounded text-xs font-medium">Export JSON</button>
            <button onclick="exportCurrentSession('har')" class="bg-purple-600 hover:bg-purple-500 text-white px-3 py-1 rounded text-xs font-medium">Export HAR</button>
            <button onclick="deleteCurrentSession()" class="bg-red-600 hover:bg-red-500 text-white px-3 py-1 rounded text-xs font-medium">Delete</button>
          </div>
        </div>
        <div id="events-container" class="overflow-y-auto flex-1 space-y-3 pr-2 font-mono text-xs">
          <!-- Event items inserted dynamically -->
        </div>
      </div>
    </div>
  </div>

  <script>
    let allEvents = [];
    let sessionsData = [];
    let isTailPaused = false;
    let selectedSessionId = null;

    function getToken() {
      return document.getElementById('auth-token').value;
    }

    async function fetchSessions() {
      try {
        const res = await fetch('/api/sessions?token=' + encodeURIComponent(getToken()));
        if (!res.ok) throw new Error('Failed to fetch sessions');
        const data = await res.json();
        sessionsData = data.sessions || [];
        updateSessionsUI();
      } catch (err) {
        console.error('Error fetching sessions:', err);
      }
    }

    async function fetchTelemetry() {
      try {
        const res = await fetch('/api/telemetry?token=' + encodeURIComponent(getToken()) + '&limit=500');
        if (!res.ok) throw new Error('Failed to fetch telemetry');
        const data = await res.json();
        allEvents = data.logs || [];
        filterEvents();
      } catch (err) {
        console.error('Error fetching telemetry:', err);
      }
    }

    async function refreshData() {
      await fetchSessions();
      await fetchTelemetry();
    }

    function updateSessionsUI() {
      document.getElementById('stat-sessions').innerText = sessionsData.length;
      document.getElementById('session-count-badge').innerText = sessionsData.length;
      
      const devices = new Set(sessionsData.map(s => s.deviceId));
      document.getElementById('stat-devices').innerText = devices.size;

      const select = document.getElementById('filter-session');
      const currentSelected = select.value;
      select.innerHTML = '<option value="">All Sessions</option>';
      for (const sess of sessionsData) {
        const opt = document.createElement('option');
        opt.value = sess.sessionId;
        opt.text = \`\${sess.sessionId} (\${sess.deviceId} - \${sess.eventCount} events)\`;
        select.appendChild(opt);
      }
      select.value = currentSelected;

      const listEl = document.getElementById('sessions-list');
      listEl.innerHTML = '';
      if (sessionsData.length === 0) {
        listEl.innerHTML = '<div class="text-slate-500 text-center py-8">No recorded sessions found.</div>';
        return;
      }

      for (const sess of sessionsData) {
        const div = document.createElement('div');
        div.className = \`p-3 rounded-lg border cursor-pointer transition \${selectedSessionId === sess.sessionId ? 'bg-slate-700 border-emerald-500' : 'bg-slate-900/50 border-slate-700 hover:bg-slate-700/50'}\`;
        div.onclick = () => selectSession(sess.sessionId);
        
        const dateStr = new Date(sess.startTime).toLocaleString();
        div.innerHTML = \`
          <div class="flex justify-between items-start mb-1">
            <span class="font-bold text-sm text-emerald-300 truncate max-w-[180px]">\${sess.sessionId}</span>
            <span class="text-xs px-2 py-0.5 rounded \${sess.errorCount > 0 ? 'bg-red-900 text-red-300' : 'bg-slate-800 text-slate-300'}">\${sess.errorCount} errors</span>
          </div>
          <div class="text-slate-400 text-xs truncate">Device: \${sess.deviceId}</div>
          <div class="flex justify-between items-center mt-2 text-xs text-slate-500">
            <span>\${sess.eventCount} events</span>
            <span>\${dateStr}</span>
          </div>
        \`;
        listEl.appendChild(div);
      }
    }

    async function selectSession(sessionId) {
      selectedSessionId = sessionId;
      document.getElementById('filter-session').value = sessionId;
      const exportBtns = document.getElementById('export-buttons');
      
      if (!sessionId) {
        exportBtns.classList.add('hidden');
        document.getElementById('feed-title').innerText = 'Live Telemetry Feed';
        await fetchTelemetry();
        return;
      }

      exportBtns.classList.remove('hidden');
      document.getElementById('feed-title').innerText = \`Session Replay: \${sessionId}\`;

      try {
        const res = await fetch(\`/api/sessions/\${sessionId}?token=\` + encodeURIComponent(getToken()));
        if (!res.ok) throw new Error('Failed to fetch session detail');
        const sess = await res.json();
        allEvents = sess.events || [];
        renderEvents(allEvents);
        updateSessionsUI();
      } catch (err) {
        console.error('Error loading session detail:', err);
      }
    }

    function filterEvents() {
      if (selectedSessionId) return;

      const filterType = document.getElementById('filter-type').value;
      const search = document.getElementById('search-input').value.toLowerCase();
      const filterSession = document.getElementById('filter-session').value;

      let filtered = allEvents;
      if (filterSession) {
        filtered = filtered.filter(e => e.sessionId === filterSession);
      }
      if (filterType) {
        filtered = filtered.filter(e => e.type === filterType);
      }
      if (search) {
        filtered = filtered.filter(e => JSON.stringify(e).toLowerCase().includes(search));
      }

      renderEvents(filtered);
    }

    function renderEvents(events) {
      const container = document.getElementById('events-container');
      container.innerHTML = '';

      let totalEvCount = sessionsData.reduce((acc, s) => acc + s.eventCount, 0);
      let totalErrCount = sessionsData.reduce((acc, s) => acc + s.errorCount, 0);
      document.getElementById('stat-events').innerText = totalEvCount || allEvents.length;
      document.getElementById('stat-errors').innerText = totalErrCount;

      if (events.length === 0) {
        container.innerHTML = '<div class="text-slate-500 text-center py-12">No telemetry events match criteria.</div>';
        return;
      }

      for (const ev of events) {
        const div = document.createElement('div');
        div.className = 'bg-slate-900 border border-slate-700/60 rounded-lg p-3';

        const timeStr = new Date(ev.timestamp || ev.wallTime || Date.now()).toLocaleTimeString();
        let badgeColor = 'bg-slate-800 text-slate-300';
        if (ev.type === 'crashes' || ev.type === 'anrs') badgeColor = 'bg-red-900 text-red-300';
        else if (ev.type === 'network') badgeColor = 'bg-emerald-900 text-emerald-300';
        else if (ev.type === 'performance') badgeColor = 'bg-amber-900 text-amber-300';
        else if (ev.type === 'logs') badgeColor = 'bg-cyan-900 text-cyan-300';

        div.innerHTML = \`
          <div class="flex justify-between items-center mb-1.5">
            <div class="flex items-center gap-2">
              <span class="px-2 py-0.5 rounded text-[10px] font-bold uppercase \${badgeColor}">\${ev.type}</span>
              <span class="text-slate-400 text-[11px]">Sess: \${ev.sessionId}</span>
              <span class="text-slate-500 text-[11px]">Dev: \${ev.deviceId}</span>
            </div>
            <div class="flex items-center gap-2 text-slate-400 text-[11px]">
              <span>seq: \${ev.seq}</span>
              <span>\${timeStr}</span>
            </div>
          </div>
          <pre class="bg-slate-950 p-2 rounded text-slate-300 overflow-x-auto">\${escapeHtml(JSON.stringify(ev.payload, null, 2))}</pre>
        \`;
        container.appendChild(div);
      }
    }

    function escapeHtml(str) {
      return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
    }

    function toggleTail() {
      isTailPaused = !isTailPaused;
      const btn = document.getElementById('btn-tail-toggle');
      if (isTailPaused) {
        btn.innerText = 'Resume Live Tail';
        btn.className = 'bg-emerald-600 hover:bg-emerald-500 text-white px-3 py-1.5 rounded text-sm font-medium transition';
      } else {
        btn.innerText = 'Pause Live Tail';
        btn.className = 'bg-amber-600 hover:bg-amber-500 text-white px-3 py-1.5 rounded text-sm font-medium transition';
      }
    }

    function clearLiveFeed() {
      allEvents = [];
      renderEvents([]);
    }

    function exportCurrentSession(format) {
      if (!selectedSessionId) return;
      window.open(\`/api/sessions/\${selectedSessionId}/export?format=\${format}&token=\` + encodeURIComponent(getToken()), '_blank');
    }

    async function deleteCurrentSession() {
      if (!selectedSessionId) return;
      if (!confirm(\`Are you sure you want to delete session \${selectedSessionId}?\`)) return;

      try {
        const res = await fetch(\`/api/sessions/\${selectedSessionId}?token=\` + encodeURIComponent(getToken()), {
          method: 'DELETE'
        });
        if (!res.ok) throw new Error('Failed to delete session');
        selectedSessionId = null;
        document.getElementById('filter-session').value = '';
        document.getElementById('export-buttons').classList.add('hidden');
        document.getElementById('feed-title').innerText = 'Live Telemetry Feed';
        await refreshData();
      } catch (err) {
        alert('Error deleting session: ' + err.message);
      }
    }

    function initSSE() {
      const statusEl = document.getElementById('connection-status');
      const evtSource = new EventSource('/api/live?token=' + encodeURIComponent(getToken()));

      evtSource.onopen = () => {
        statusEl.innerText = 'Connected';
        statusEl.className = 'px-3 py-1 rounded-full text-xs font-semibold bg-emerald-900 text-emerald-300 border border-emerald-700';
      };

      evtSource.onmessage = (event) => {
        if (isTailPaused) return;
        try {
          const data = JSON.parse(event.data);
          if (data.type === 'connected') return;

          allEvents.push(data);
          if (allEvents.length > 1000) allEvents.shift();
          filterEvents();
        } catch (e) {}
      };

      evtSource.onerror = () => {
        statusEl.innerText = 'Disconnected';
        statusEl.className = 'px-3 py-1 rounded-full text-xs font-semibold bg-red-900 text-red-300 border border-red-700';
      };
    }

    refreshData();
    initSSE();
  </script>
</body>
</html>`);
});

// POST /api/telemetry: Receives batch/single versioned telemetry envelopes
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

    for (const item of validatedItems) {
      persistEnvelope(item);
      // Broadcast via SSE
      for (const client of sseClients) {
        client.write(`data: ${JSON.stringify(item)}\n\n`);
      }
    }

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

// GET /api/telemetry: Inspect received logs
app.get('/api/telemetry', (req, res) => {
  const limit = parseInt(req.query.limit) || 100;
  res.json({
    totalStored: receivedLogs.length,
    logs: receivedLogs.slice(-limit).reverse()
  });
});

// GET /api/sessions: List all recorded sessions with summary stats
app.get('/api/sessions', (_, res) => {
  const sessionsList = [];
  for (const [, sess] of sessionsMap.entries()) {
    sessionsList.push({
      sessionId: sess.sessionId,
      deviceId: sess.deviceId,
      startTime: sess.startTime,
      endTime: sess.endTime,
      eventCount: sess.eventCount,
      errorCount: sess.errorCount,
      types: sess.types
    });
  }
  sessionsList.sort((a, b) => b.startTime - a.startTime);
  res.json({
    totalSessions: sessionsList.length,
    sessions: sessionsList
  });
});

// GET /api/sessions/:sessionId: Retrieve full event logs for a specific session
app.get('/api/sessions/:sessionId', (req, res) => {
  const sessionId = req.params.sessionId;
  const sess = sessionsMap.get(sessionId);
  if (!sess) {
    return res.status(404).json({ error: `Session not found: ${sessionId}` });
  }
  res.json(sess);
});

// GET /api/sessions/:sessionId/export?format=json|har: Export session logs as JSON or HAR
app.get('/api/sessions/:sessionId/export', (req, res) => {
  const sessionId = req.params.sessionId;
  const format = (req.query.format || 'json').toLowerCase();
  const sess = sessionsMap.get(sessionId);
  if (!sess) {
    return res.status(404).json({ error: `Session not found: ${sessionId}` });
  }

  if (format === 'har') {
    const harEntries = [];
    for (const ev of sess.events) {
      if (ev.type === 'network') {
        const p = ev.payload || {};
        harEntries.push({
          startedDateTime: new Date(ev.timestamp || ev.wallTime || Date.now()).toISOString(),
          time: p.durationMs || 0,
          request: {
            method: p.method || 'GET',
            url: p.url || 'http://localhost/',
            httpVersion: 'HTTP/1.1',
            cookies: [],
            headers: [],
            queryString: [],
            headersSize: -1,
            bodySize: p.requestSize || 0
          },
          response: {
            status: p.statusCode || 200,
            statusText: p.statusCode === 200 ? 'OK' : 'Error',
            httpVersion: 'HTTP/1.1',
            cookies: [],
            headers: [],
            content: {
              size: p.responseSize || 0,
              mimeType: 'application/json',
              text: ''
            },
            redirectURL: '',
            headersSize: -1,
            bodySize: -1
          },
          cache: {},
          timings: { send: 0, wait: p.durationMs || 0, receive: 0 }
        });
      } else {
        harEntries.push({
          startedDateTime: new Date(ev.timestamp || ev.wallTime || Date.now()).toISOString(),
          time: 0,
          request: {
            method: 'POST',
            url: `internal://telemetry/${ev.type}`,
            httpVersion: 'HTTP/1.1',
            cookies: [],
            headers: [],
            queryString: [],
            headersSize: -1,
            bodySize: 0
          },
          response: {
            status: 200,
            statusText: 'OK',
            httpVersion: 'HTTP/1.1',
            cookies: [],
            headers: [],
            content: {
              size: JSON.stringify(ev.payload).length,
              mimeType: 'application/json',
              text: JSON.stringify(ev.payload)
            },
            redirectURL: '',
            headersSize: -1,
            bodySize: -1
          },
          cache: {},
          timings: { send: 0, wait: 0, receive: 0 }
        });
      }
    }

    const harDoc = {
      log: {
        version: '1.2',
        creator: { name: 'Igent Telemetry Collector', version: '1.0.0' },
        entries: harEntries
      }
    };
    res.setHeader('Content-Type', 'application/json');
    res.setHeader('Content-Disposition', `attachment; filename="${sessionId}.har"`);
    return res.send(JSON.stringify(harDoc, null, 2));
  } else {
    res.setHeader('Content-Type', 'application/json');
    res.setHeader('Content-Disposition', `attachment; filename="${sessionId}.json"`);
    return res.send(JSON.stringify(sess, null, 2));
  }
});

// GET /api/sessions/:sessionId/snapshots: Retrieve all snapshots for a session
app.get('/api/sessions/:sessionId/snapshots', (req, res) => {
  const sessionId = req.params.sessionId;
  const sess = sessionsMap.get(sessionId);
  if (!sess) {
    return res.status(404).json({ error: `Session not found: ${sessionId}` });
  }
  res.json({
    sessionId,
    totalSnapshots: sess.snapshots.length,
    snapshots: sess.snapshots
  });
});

// GET /api/sessions/:sessionId/snapshots/diff: Diff two snapshots by event index or snapshotId
app.get('/api/sessions/:sessionId/snapshots/diff', (req, res) => {
  const sessionId = req.params.sessionId;
  const sess = sessionsMap.get(sessionId);
  if (!sess) {
    return res.status(404).json({ error: `Session not found: ${sessionId}` });
  }
  const id1 = req.query.id1;
  const id2 = req.query.id2;
  if (!id1 || !id2) {
    return res.status(400).json({ error: 'Both id1 and id2 query parameters are required for diffing' });
  }

  const snap1 = sess.snapshots.find(s => s.payload?.snapshotId === id1 || s.seq.toString() === id1);
  const snap2 = sess.snapshots.find(s => s.payload?.snapshotId === id2 || s.seq.toString() === id2);

  if (!snap1 || !snap2) {
    return res.status(404).json({ error: 'One or both snapshots not found' });
  }

  const p1 = snap1.payload || {};
  const p2 = snap2.payload || {};
  const keys = new Set([...Object.keys(p1), ...Object.keys(p2)]);
  const diff = {};
  for (const key of keys) {
    const val1 = JSON.stringify(p1[key]);
    const val2 = JSON.stringify(p2[key]);
    if (val1 !== val2) {
      diff[key] = { from: p1[key], to: p2[key] };
    }
  }

  res.json({
    sessionId,
    snapshot1Id: id1,
    snapshot2Id: id2,
    differenceCount: Object.keys(diff).length,
    diff
  });
});

// DELETE /api/sessions/:sessionId: Clear/delete a session
app.delete('/api/sessions/:sessionId', (req, res) => {
  const sessionId = req.params.sessionId;
  const sess = sessionsMap.get(sessionId);
  if (!sess) {
    return res.status(404).json({ error: `Session not found: ${sessionId}` });
  }

  sessionsMap.delete(sessionId);
  const filePath = path.join(sessionsDir, `${sessionId}.jsonl`);
  try {
    if (fs.existsSync(filePath)) {
      fs.unlinkSync(filePath);
    }
  } catch (err) {
    console.error(`[ERROR] Failed to delete session file ${filePath}:`, err);
  }

  for (let i = receivedLogs.length - 1; i >= 0; i--) {
    if (receivedLogs[i].sessionId === sessionId) {
      receivedLogs.splice(i, 1);
    }
  }

  return res.json({ success: true, message: `Session ${sessionId} deleted successfully` });
});

// GET /api/live: Server-Sent Events live telemetry stream
app.get('/api/live', (req, res) => {
  res.setHeader('Content-Type', 'text/event-stream');
  res.setHeader('Cache-Control', 'no-cache');
  res.setHeader('Connection', 'keep-alive');
  res.flushHeaders();

  res.write(`data: ${JSON.stringify({ type: 'connected', timestamp: Date.now() })}\n\n`);

  const client = res;
  sseClients.add(client);

  const heartbeat = setInterval(() => {
    res.write(': ping\n\n');
  }, 15000);

  req.on('close', () => {
    clearInterval(heartbeat);
    sseClients.delete(client);
  });
});

// GET /api/config: Returns dynamic feature flags and remote config
app.get('/api/config', (_, res) => {
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

// GET /api/logpoints: List all registered logpoints
app.get('/api/logpoints', (req, res) => {
  res.json({ success: true, logpoints: Array.from(logpointsMap.values()) });
});

// POST /api/logpoints/config: Configure logpoint live
app.post('/api/logpoints/config', (req, res) => {
  try {
    const { id, enabled, level, samplingRate, condition } = req.body;
    if (!id) {
      return res.status(400).json({ success: false, error: 'Logpoint id required' });
    }
    let lp = logpointsMap.get(id);
    if (!lp) {
      lp = {
        id,
        tag: req.body.tag || 'Logpoint',
        defaultLevel: level || 'INFO',
        callSite: req.body.callSite || '',
        enabled: enabled !== undefined ? enabled : true,
        level: level || 'INFO',
        samplingRate: samplingRate !== undefined ? samplingRate : 1.0,
        condition: condition || null,
        hitCount: 0,
        dropCount: 0,
        lastHitTime: Date.now()
      };
      logpointsMap.set(id, lp);
    } else {
      if (enabled !== undefined) lp.enabled = enabled;
      if (level !== undefined) lp.level = level;
      if (samplingRate !== undefined) lp.samplingRate = samplingRate;
      if (condition !== undefined) lp.condition = condition;
    }
    res.json({ success: true, logpoint: lp });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
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
        serverStatus: "healthy",
        logpoints: Array.from(logpointsMap.values())
      }
    };

    return res.status(200).json(syncResponse);
  } catch (err) {
    console.error('Error processing sync:', err);
    return res.status(500).json({ error: 'Internal server error during sync' });
  }
});

// Port fallback mechanism
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
      console.log(`   GET  / (Web UI Dashboard)            `);
      console.log(`   POST /api/telemetry                  `);
      console.log(`   GET  /api/sessions                   `);
      console.log(`   GET  /api/sessions/:sessionId        `);
      console.log(`   GET  /api/sessions/:sessionId/export `);
      console.log(`   DELETE /api/sessions/:sessionId      `);
      console.log(`   GET  /api/live (SSE Stream)          `);
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

    return server;
  } catch (err) {
    console.error('[ERROR] Failed to start telemetry server:', err);
    process.exit(1);
  }
}

// Graceful shutdown handling
function gracefulShutdown(signal) {
  console.log(`[INFO] Received ${signal}. Shutting down telemetry server gracefully...`);
  try {
    const activePortFilePath = path.join(__dirname, '.active-port');
    if (fs.existsSync(activePortFilePath)) {
      fs.unlinkSync(activePortFilePath);
    }
  } catch (err) {
    console.error('Error cleaning up .active-port:', err);
  }
  process.exit(0);
}

process.on('SIGINT', () => gracefulShutdown('SIGINT'));
process.on('SIGTERM', () => gracefulShutdown('SIGTERM'));

// Only start server if run directly (not required in tests)
if (require.main === module) {
  startServer();
} else {
  module.exports = { app, startServer, validateEnvelope, sessionsMap, sessionsDir };
}
