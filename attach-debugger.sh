#!/usr/bin/env bash
# ============================================================================
# Igent Debugger-Attach Helper Script for macOS / Linux
#
# This script:
# 1. Verifies connected Android devices/emulators via ADB.
# 2. Sets up required port forwarding (adb reverse tcp:3000 tcp:3000).
# 3. Optionally installs / launches the debug build package (com.igent.mobile).
# 4. Prints exact attach instructions for Android Studio / IntelliJ.
# 5. Guides running alongside the telemetry stream simultaneously.
# ============================================================================

set -e

echo "========================================================"
echo "  Igent Debugger-Attach Helper & Port Forwarding"
echo "========================================================"

# Check for adb
if ! command -v adb &> /dev/null; then
    echo "[ERROR] ADB (Android Debug Bridge) not found in PATH."
    echo "Please ensure Android SDK platform-tools is installed and added to PATH."
    exit 1
fi

# Verify connected devices
echo "[INFO] Verifying connected Android devices/emulators..."
devices=$(adb devices | grep -w "device" | awk '{print $1}')
if [ -z "$devices" ]; then
    echo "[WARNING] No active ADB devices or emulators detected!"
    echo "Please start an emulator or connect a physical device with USB debugging enabled."
    echo ""
    read -p "Press Enter to continue anyway or Ctrl+C to abort..."
else
    for dev in $devices; do
        echo "[INFO] Found active device: $dev"
        # Setup port forwarding
        adb -s "$dev" reverse tcp:3000 tcp:3000
        echo "[INFO] Configured 'adb reverse tcp:3000 tcp:3000' on $dev"
    done
fi

# Optional APK install / launch
PACKAGE_NAME="com.igent.mobile"
echo "[INFO] Target package: $PACKAGE_NAME"

# Check if app is installed
installed=$(adb shell pm list packages | grep "$PACKAGE_NAME" || true)
if [ -n "$installed" ]; then
    echo "[INFO] App ($PACKAGE_NAME) is installed on device."
    echo "[INFO] Launching main activity..."
    adb shell am start -n "$PACKAGE_NAME/.MainActivity" 2>/dev/null || adb shell monkey -p "$PACKAGE_NAME" -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1
    echo "[INFO] App launched successfully."
else
    echo "[WARNING] App ($PACKAGE_NAME) is not currently installed on the device."
    echo "[INFO] To build and install via Gradle, run: ./gradlew installDebug"
fi

echo ""
echo "========================================================"
echo "  INSTRUCTIONS FOR ANDROID STUDIO / INTELLIJ DEBUGGER:"
echo "========================================================"
echo "1. Open the project workspace in Android Studio."
echo "2. In the top menu, go to: Run -> Attach Debugger to Android Process"
echo "3. Select process: 'com.igent.mobile' (Debuggable process)."
echo "4. Set breakpoints in your Kotlin code (e.g. ViewModels, Repositories, UseCases)."
echo ""
echo "  HANG-SUPPRESSION & TELEMETRY NOTES:"
echo "- Debugger-Attached mode automatically suppresses ANR/hang triggers"
echo "  when paused at breakpoints (via android.os.Debug.isDebuggerConnected())."
echo "- To view live telemetry logs alongside your debugging session,"
echo "  open a separate terminal and run:"
echo "      cd telemetry-server && npm run tail"
echo "========================================================"
