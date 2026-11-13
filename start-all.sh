#!/usr/bin/env bash
# ============================================================================
# Igent Master Startup Script for macOS / Linux (start-all.sh)
#
# Orchestrates:
# 1. Navigating to telemetry-server/, checking node_modules, npm install if missing,
#    and starting the telemetry server in a separate terminal window.
# 2. Waiting briefly for telemetry server to start.
# 3. Detecting connected Android emulators/devices via adb devices and running
#    adb -s <device> reverse tcp:3000 tcp:3000.
# 4. Building and installing debug APK via ./gradlew installDebug.
# 5. Launching live telemetry tail viewer (npm run tail) in a separate terminal window.
# 6. Robust error handling, clear logging ([INFO], [SUCCESS], [WARNING], [ERROR]).
# ============================================================================

set -e

echo "========================================================"
echo "  Igent Master Developer Startup (macOS/Linux)"
echo "========================================================"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Error trap
trap 'echo ""; echo "[ERROR] Startup orchestration failed at line $LINENO."; exit 1' ERR

# 1. Navigate to telemetry-server, check dependencies, and start telemetry server in a new window
echo "[INFO] Navigating to telemetry-server directory..."
cd "$SCRIPT_DIR/telemetry-server" || {
    echo "[ERROR] Could not find 'telemetry-server' directory at $SCRIPT_DIR/telemetry-server."
    exit 1
}

echo "[INFO] Checking telemetry-server dependencies (node_modules)..."
if [ ! -d "node_modules" ]; then
    echo "[INFO] node_modules not found. Running 'npm install'..."
    npm install
    echo "[SUCCESS] Telemetry server dependencies installed successfully."
else
    echo "[SUCCESS] Telemetry server dependencies (node_modules) found."
fi

echo "[INFO] Starting Telemetry Server in a separate terminal window..."
if [[ "$OSTYPE" == "darwin"* ]]; then
    osascript -e "tell application \"Terminal\" to do script \"cd '$SCRIPT_DIR/telemetry-server' && npm start\""
elif command -v gnome-terminal &> /dev/null; then
    gnome-terminal -- bash -c "cd '$SCRIPT_DIR/telemetry-server' && npm start; exec bash"
elif command -v konsole &> /dev/null; then
    konsole --hold -e bash -c "cd '$SCRIPT_DIR/telemetry-server' && npm start"
elif command -v xterm &> /dev/null; then
    xterm -e bash -c "cd '$SCRIPT_DIR/telemetry-server' && npm start; exec bash"
else
    echo "[WARNING] No graphical terminal emulator found. Starting telemetry server in background..."
    npm start &
    SERVER_PID=$!
fi
echo "[SUCCESS] Telemetry Server launch command dispatched."

# 2. Wait briefly for telemetry server to start
echo "[INFO] Waiting 3 seconds for telemetry server to initialize..."
sleep 3

# 3. Detect connected Android devices/emulators and set up adb reverse
echo "[INFO] Detecting connected Android devices via ADB..."
if ! command -v adb &> /dev/null; then
    echo "[ERROR] ADB not found in PATH. Please ensure Android SDK platform-tools is installed and added to PATH."
    exit 1
fi

devices=$(adb devices | awk 'NR>1 && $2=="device" {print $1}')
if [ -z "$devices" ]; then
    echo "[WARNING] No active Android devices or emulators detected via ADB!"
    echo "[INFO] Please start an emulator or connect a physical device with USB debugging enabled."
else
    for dev in $devices; do
        echo "[SUCCESS] Found active Android device/emulator: $dev"
        adb -s "$dev" reverse tcp:3000 tcp:3000
        echo "[SUCCESS] Configured 'adb reverse tcp:3000 tcp:3000' for $dev"
    done
fi

# 4. Build and install debug APK onto connected devices via Gradle
echo "[INFO] Building and installing debug APK via Gradle (installDebug)..."
cd "$SCRIPT_DIR"
if [ -f "./gradlew" ]; then
    chmod +x ./gradlew
    ./gradlew installDebug
else
    echo "[ERROR] gradlew not found in workspace root ($SCRIPT_DIR)."
    exit 1
fi
echo "[SUCCESS] Debug APK successfully built and installed on connected devices!"

# 5. Launch live telemetry tail viewer (npm run tail) in a separate terminal window
echo "[INFO] Launching live telemetry tail viewer in a separate terminal window..."
if [[ "$OSTYPE" == "darwin"* ]]; then
    osascript -e "tell application \"Terminal\" to do script \"cd '$SCRIPT_DIR/telemetry-server' && npm run tail\""
elif command -v gnome-terminal &> /dev/null; then
    gnome-terminal -- bash -c "cd '$SCRIPT_DIR/telemetry-server' && npm run tail; exec bash"
elif command -v konsole &> /dev/null; then
    konsole --hold -e bash -c "cd '$SCRIPT_DIR/telemetry-server' && npm run tail"
elif command -v xterm &> /dev/null; then
    xterm -e bash -c "cd '$SCRIPT_DIR/telemetry-server' && npm run tail; exec bash"
else
    echo "[WARNING] No graphical terminal emulator found for tail viewer."
    echo "[INFO] You can open another terminal and run: cd telemetry-server && npm run tail"
fi
echo "[SUCCESS] Telemetry tail viewer launch command dispatched."

echo ""
echo "========================================================"
echo "  [SUCCESS] Igent Master Startup Orchestration Complete!"
echo "========================================================"
echo "  - Telemetry Server: Running in separate window (Port 3000)"
echo "  - Tail Viewer: Running in separate window"
echo "  - App: Installed as debug build on connected devices"
echo "========================================================"
