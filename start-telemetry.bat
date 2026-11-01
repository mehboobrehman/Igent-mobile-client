@echo off
:: ============================================================================
:: Igent Telemetry Server Startup Script for Windows
:: 
:: This script:
:: 1. Navigates to the telemetry-server directory.
:: 2. Checks if node_modules exists; runs 'npm install' if missing.
:: 3. Starts the telemetry server using 'npm start'.
:: ============================================================================

echo ==========================================
echo Starting Igent Telemetry Server (Windows)
echo ==========================================

:: Navigate to the telemetry-server directory relative to the script location
cd /d "%~dp0telemetry-server"
if errorlevel 1 (
    echo [ERROR] Could not find 'telemetry-server' directory.
    pause
    exit /b 1
)

:: Check if node_modules exists; install dependencies if not found
if not exist "node_modules" (
    echo [INFO] node_modules not found. Installing dependencies...
    npm install
    if errorlevel 1 (
        echo [ERROR] 'npm install' failed. Please ensure Node.js and npm are installed correctly.
        pause
        exit /b 1
    )
) else (
    echo [INFO] node_modules found. Skipping dependency installation.
)

:: Start the telemetry server
echo [INFO] Starting telemetry server on http://localhost:3000...
echo [INFO] Press Ctrl+C to stop the server.
echo ------------------------------------------
npm start

pause
