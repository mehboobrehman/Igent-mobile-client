@echo off
:: ============================================================================
:: Igent Unified Developer Tooling Startup Script for Windows
:: 
:: This script:
:: 1. Checks for connected Android devices/emulators via ADB and sets up port forwarding (reverse tcp:3000 tcp:3000).
:: 2. Installs telemetry server dependencies if needed.
:: 3. Starts the telemetry collector and dashboard server.
:: 4. Instructs how to run the real-time tailing viewer (`npm run tail`).
:: ============================================================================

echo ========================================================
echo   Igent Live Debug Telemetry - Unified Developer Startup
echo ========================================================

cd /d "%~dp0telemetry-server"
if errorlevel 1 (
    echo [ERROR] Could not find 'telemetry-server' directory.
    pause
    exit /b 1
)

:: Check dependencies
if not exist "node_modules" (
    echo [INFO] Installing telemetry-server dependencies...
    npm install
    if errorlevel 1 (
        echo [ERROR] 'npm install' failed.
        pause
        exit /b 1
    )
) else (
    echo [INFO] Dependencies found.
)

:: Setup ADB port forwarding if ADB is available and device is connected
echo [INFO] Checking ADB devices and setting up port forwarding...
where adb >nul 2>nul
if %errorlevel% equ 0 (
    for /f "tokens=2" %%i in ('adb devices ^| findstr /r "\<device\>"') do (
        echo [INFO] Found ADB device/emulator: %%i
        adb -s %%i reverse tcp:3000 tcp:3000
        echo [INFO] Configured 'adb reverse tcp:3000 tcp:3000' for device %%i
    )
) else (
    echo [WARNING] ADB not found in PATH. Skipping automatic adb port forwarding. (Ensure app can reach collector via host IP or emulator alias).
)

echo.
echo ========================================================
echo   Telemetry Server is ready!
echo   - Web Dashboard: http://localhost:3000
echo   - Tailing Viewer: Open a new terminal and run:
echo       cd telemetry-server && npm run tail
echo ========================================================
echo.

npm start
pause
