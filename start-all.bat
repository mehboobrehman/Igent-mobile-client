@echo off
setlocal
:: ============================================================================
:: Igent Master Startup Script for Windows (start-all.bat)
:: 
:: Orchestrates:
:: 1. Navigating to telemetry-server/, checking [node_modules], npm install if missing,
::    and starting the telemetry server in a separate terminal window.
:: 2. Waiting briefly for telemetry server to start.
:: 3. Detecting connected Android emulators/devices via adb devices and running
::    adb -s <device> reverse tcp:3000 tcp:3000.
:: 4. Building and installing debug APK via gradlew.bat installDebug.
:: 5. Launching live telemetry tail viewer (npm run tail) in a separate terminal window.
:: 6. Robust error handling, clear logging ([INFO], [SUCCESS], [WARNING], [ERROR]).
:: ============================================================================

echo ========================================================
echo   Igent Master Developer Startup (Windows)
echo ========================================================

set "SCRIPT_DIR=%~dp0"

:: 1. Navigate to telemetry-server, check dependencies, and start telemetry server in a new window
echo [INFO] Navigating to telemetry-server directory...
cd /d "%SCRIPT_DIR%telemetry-server"
if errorlevel 1 (
    echo [ERROR] Could not find 'telemetry-server' directory at "%SCRIPT_DIR%telemetry-server".
    goto :error
)

echo [INFO] Checking telemetry-server dependencies [node_modules]...
if not exist "node_modules" (
    echo [INFO] node_modules not found. Running 'npm install'...
    call npm install
    if errorlevel 1 (
        echo [ERROR] 'npm install' failed in telemetry-server.
        goto :error
    )
    echo [SUCCESS] Telemetry server dependencies installed successfully.
) else (
    echo [SUCCESS] Telemetry server dependencies [node_modules] found.
)

echo [INFO] Starting Telemetry Server in a separate terminal window...
start /d "%SCRIPT_DIR%telemetry-server" "Igent Telemetry Server" cmd /c "npm start"
echo [SUCCESS] Telemetry Server launch command dispatched.

:: 2. Wait briefly for telemetry server to start
echo [INFO] Waiting 3 seconds for telemetry server to initialize...
timeout /t 3 /nobreak >nul

:: 3. Detect connected Android devices/emulators and set up adb reverse
echo [INFO] Detecting connected Android devices via ADB...
where adb >nul 2>nul
if errorlevel 1 (
    echo [ERROR] ADB not found in PATH. Please ensure Android SDK platform-tools is installed and added to PATH.
    goto :error
)

set "DEVICE_FOUND=0"
for /f "tokens=1,2" %%a in ('adb devices') do (
    if "%%b"=="device" (
        set "DEVICE_FOUND=1"
        echo [SUCCESS] Found active Android device/emulator: %%a
        adb -s %%a reverse tcp:3000 tcp:3000
        if not errorlevel 1 (
            echo [SUCCESS] Configured 'adb reverse tcp:3000 tcp:3000' for %%a
        ) else (
            echo [WARNING] Failed to configure adb reverse for %%a
        )
    )
)

if "%DEVICE_FOUND%"=="0" (
    echo [WARNING] No active Android devices or emulators detected via ADB!
    echo [INFO] Please start an emulator or connect a physical device with USB debugging enabled.
)

:: 4. Build and install debug APK onto connected devices via Gradle
echo [INFO] Building and installing debug APK via Gradle [installDebug]...
cd /d "%SCRIPT_DIR%"
call gradlew.bat installDebug
if errorlevel 1 (
    echo [ERROR] Gradle build and installation [installDebug] failed.
    goto :error
)
echo [SUCCESS] Debug APK successfully built and installed on connected devices!

:: 5. Launch live telemetry tail viewer (npm run tail) in a separate terminal window
echo [INFO] Launching live telemetry tail viewer in a separate terminal window...
start /d "%SCRIPT_DIR%telemetry-server" "Igent Telemetry Tail Viewer" cmd /c "npm run tail"
echo [SUCCESS] Telemetry tail viewer launch command dispatched.

echo.
echo ========================================================
echo   [SUCCESS] Igent Master Startup Orchestration Complete!
echo ========================================================
echo   - Telemetry Server: Running in separate window (Port 3000)
echo   - Tail Viewer: Running in separate window
echo   - App: Installed as debug build on connected devices
echo ========================================================
goto :end

:error
echo.
echo [ERROR] Startup orchestration failed. Please check the error messages above.
pause
exit /b 1

:end
pause
