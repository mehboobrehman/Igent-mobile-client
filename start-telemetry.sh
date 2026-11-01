#!/usr/bin/env bash
# ============================================================================
# Igent Telemetry Server Startup Script for macOS / Linux
#
# This script:
# 1. Navigates to the telemetry-server directory.
# 2. Checks if node_modules exists; runs 'npm install' if missing.
# 3. Starts the telemetry server using 'npm start'.
# ============================================================================

set -e

echo "=========================================="
echo "Starting Igent Telemetry Server (macOS/Linux)"
echo "=========================================="

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Navigate to the telemetry-server directory
cd "$SCRIPT_DIR/telemetry-server" || {
    echo "[ERROR] Could not find 'telemetry-server' directory."
    exit 1
}

# Check if node_modules exists; install dependencies if not found
if [ ! -d "node_modules" ]; then
    echo "[INFO] node_modules not found. Installing dependencies..."
    npm install
else
    echo "[INFO] node_modules found. Skipping dependency installation."
fi

# Start the telemetry server
echo "[INFO] Starting telemetry server on http://localhost:3000..."
echo "[INFO] Press Ctrl+C to stop the server."
echo "------------------------------------------"
npm start
