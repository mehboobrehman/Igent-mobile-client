# Igent Telemetry Server

A standalone Node.js telemetry and remote config server built with Express for the **Igent** mobile client.

## Features

- **`POST /api/telemetry`**: Receives single or batched logs/telemetry events from the mobile app and stores them in memory (with automatic capacity capping).
- **`GET /api/config`**: Serves dynamic feature flags, remote configuration, min supported version, and announcements.
- **`POST /api/sync`**: Handles client synchronization requests and status checks.
- **`GET /api/telemetry`**: Inspect recently received logs for debugging.

---

## Getting Started

### Prerequisites

Ensure you have [Node.js](https://nodejs.org/) installed on your machine.

### Installation

Navigate to the `telemetry-server` directory and install dependencies:

```bash
cd telemetry-server
npm install
```

### Running the Server

Start the server normally:

```bash
npm start
```

Or run in development mode with automatic restart (`--watch`):

```bash
npm run dev
```

By default, the server runs on port **8080**. You can customize the port via the `PORT` environment variable:

```bash
PORT=3000 npm start
```

---

## API Endpoints Reference

### 1. Receive Telemetry
- **URL**: `/api/telemetry`
- **Method**: `POST`
- **Body**: JSON object or array of log entries.
- **Response**: `200 OK` with storage confirmation.

### 2. Get Remote Config & Feature Flags
- **URL**: `/api/config`
- **Method**: `GET`
- **Response**: JSON payload containing feature flags (`telemetryEnabled`, `forceError`, features map).

### 3. Client Sync
- **URL**: `/api/sync`
- **Method**: `POST`
- **Body**: Client state object.
- **Response**: `200 OK` with sync status and server timestamp.
