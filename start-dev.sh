#!/usr/bin/env bash
# ============================================================================
# Igent Unified Developer Tooling Startup Script for macOS / Linux
#
# This script:
# 1. Checks for connected Android devices/emulators via ADB and sets up port forwarding (reverse tcp:3000 tcp:3000).
# 2. Installs telemetry server dependencies if needed.
# 3. Starts the telemetry collector and dashboard server.
# ============================================================================

set -e

echo "========================================================"
echo "  Igent Live Debug Telemetry - Unified Developer Startup"
echo "========================================================"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/telemetry-server" || {
    echo "[ERROR] Could not find 'telemetry-server' directory."
    exit 1
}

# Check dependencies
if [ ! -d "node_modules" ]; then
    echo "[INFO] Installing telemetry-server dependencies..."
    npm install
fi

# Setup ADB port forwarding if ADB is available
echo "[INFO] Checking ADB devices and setting up port forwarding..."
if command -v adb &> /dev/null; then
    devices=$(adb devices | grep -w "device" | awk '{print $1}')
    for dev in $devices; do
        echo "[INFO] Found ADB device/emulator: $dev"
        adb -s "$dev" reverse tcp:3000 tcp:3000
        echo "[INFO] Configured 'adb reverse tcp:3000 tcp:3000' for device $dev"
    done
else
    echo "[WARNING] ADB not found in PATH. Skipping automatic adb port forwarding."
fi

echo ""
echo "========================================================"
echo "  Telemetry Server starting..."
echo "  Note: Active port and host network IP addresses are"
echo "  printed dynamically upon server start below."
echo "  - Tailing Viewer: Open a new terminal and run:"
echo "      cd telemetry-server && npm run tail"
echo "========================================================"
echo ""

npm start
