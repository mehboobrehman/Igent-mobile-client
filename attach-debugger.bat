@echo off
:: ============================================================================
:: Igent Debugger-Attach Helper Script for Windows
::
:: This script:
:: 1. Verifies connected Android devices/emulators via ADB.
:: 2. Sets up required port forwarding (adb reverse tcp:3000 tcp:3000).
:: 3. Checks app installation and launches the debug build.
:: 4. Prints exact attach instructions for Android Studio / IntelliJ.
:: 5. Guides running alongside the telemetry stream simultaneously.
:: ============================================================================

echo ========================================================
echo   Igent Debugger-Attach Helper ^& Port Forwarding
echo ========================================================

:: Check for adb
where adb >nul 2>&1
if errorlevel 1 (
    echo [ERROR] ADB (Android Debug Bridge) not found in PATH.
    echo Please ensure Android SDK platform-tools is installed and added to PATH.
    pause
    exit /b 1
)

echo [INFO] Verifying connected Android devices/emulators...
adb devices

echo [INFO] Setting up adb reverse port forwarding (tcp:3000 -> tcp:3000)...
adb reverse tcp:3000 tcp:3000
if errorlevel 1 (
    echo [WARNING] 'adb reverse' failed or no device connected. Ensure device is online.
) else (
    echo [INFO] Port forwarding successfully configured.
)

set PACKAGE_NAME=com.igent.mobile
echo [INFO] Target package: %PACKAGE_NAME%

:: Check if package is installed
adb shell pm list packages | findstr /i "%PACKAGE_NAME%" >nul
if errorlevel 1 (
    echo [WARNING] App (%PACKAGE_NAME%) is not currently installed on the device.
    echo [INFO] To build and install via Gradle, run: gradlew.bat installDebug
) else (
    echo [INFO] App is installed. Launching main activity...
    adb shell am start -n "%PACKAGE_NAME%/.MainActivity" >nul 2>&1
    if errorlevel 1 (
        adb shell monkey -p "%PACKAGE_NAME%" -c android.intent.category.LAUNCHER 1 >nul 2>&1
    )
    echo [INFO] App launch command sent.
)

echo.
echo ========================================================
echo   INSTRUCTIONS FOR ANDROID STUDIO / INTELLIJ DEBUGGER:
echo ========================================================
echo 1. Open the project workspace in Android Studio.
echo 2. In the top menu, go to: Run -^> Attach Debugger to Android Process
echo 3. Select process: 'com.igent.mobile' (Debuggable process).
echo 4. Set breakpoints in your Kotlin code (ViewModels, Repositories, etc.).
echo.
echo   HANG-SUPPRESSION ^& TELEMETRY NOTES:
echo - Debugger-Attached mode automatically suppresses ANR/hang triggers
echo   when paused at breakpoints (via android.os.Debug.isDebuggerConnected()).
echo - To view live telemetry logs alongside your debugging session,
echo   open a separate terminal and run:
echo       cd telemetry-server ^& npm run tail
echo ========================================================
pause
