@echo off
REM 123panNextGen Android - Gradle Wrapper for Windows

set DIRNAME=%~dp0
set APP_HOME=%DIRNAME%
set GRADLE_VERSION=8.5
set GRADLE_USER_HOME=%USERPROFILE%\.gradle

REM Determine if this is a 64-bit system
if "%PROCESSOR_ARCHITECTURE%"=="AMD64" set ARCH=-x86_64
if "%PROCESSOR_ARCHITEW6432%"=="AMD64" set ARCH=-x86_64

set GRADLE_DIST=gradle-%GRADLE_VERSION%-bin.zip
set GRADLE_URL=https://services.gradle.org/distributions/%GRADLE_DIST%
set GRADLE_CACHE_DIR=%GRADLE_USER_HOME%\wrapper\dists\%GRADLE_DIST%\%GRADLE_HASH%
set GRADLE_BIN_DIR=%GRADLE_CACHE_DIR%\gradle-%GRADLE_VERSION%\bin

REM Try to find an existing Gradle installation
if exist "%GRADLE_BIN_DIR%\gradle.bat" goto :RUN_GRADLE

REM Download Gradle if not cached
echo Gradle %GRADLE_VERSION% not found locally.
echo Downloading from %GRADLE_URL% ...
echo.

if not exist "%TEMP%\gradle-%GRADLE_VERSION%" mkdir "%TEMP%\gradle-%GRADLE_VERSION%"

REM Try PowerShell download first
powershell -Command "& { [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; try { Invoke-WebRequest -Uri '%GRADLE_URL%' -OutFile '%TEMP%\gradle-%GRADLE_VERSION%\gradle.zip' -TimeoutSec 120 -ErrorAction Stop; Write-Host 'Download complete' } catch { Write-Host 'PowerShell download failed: ' $_; exit 1 } }" 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo PowerShell download failed, trying curl...
    REM Try curl as fallback
    curl -L --connect-timeout 10 --retry 3 -o "%TEMP%\gradle-%GRADLE_VERSION%\gradle.zip" "%GRADLE_URL%" 2>&1
    if %ERRORLEVEL% NEQ 0 (
        echo.
        echo Cannot download Gradle. Please install Gradle manually:
        echo   1. Download from: https://gradle.org/releases/
        echo   2. Or use Android Studio to build
        echo.
        pause
        exit /b 1
    )
)

echo Extracting Gradle...
powershell -Command "& { Add-Type -AssemblyName System.IO.Compression.FileSystem; [System.IO.Compression.ZipFile]::ExtractToDirectory('%TEMP%\gradle-%GRADLE_VERSION%\gradle.zip', '%TEMP%\gradle-%GRADLE_VERSION%\extracted'); Write-Host 'Extraction complete' }" 2>&1

if not exist "%TEMP%\gradle-%GRADLE_VERSION%\extracted\gradle-%GRADLE_VERSION%" (
    echo Extraction failed. Please try manual installation.
    pause
    exit /b 1
)

REM Move to cache location
if not exist "%GRADLE_CACHE_DIR%" mkdir "%GRADLE_CACHE_DIR%"
move "%TEMP%\gradle-%GRADLE_VERSION%\extracted\gradle-%GRADLE_VERSION%" "%GRADLE_CACHE_DIR%\" >nul 2>&1

:CHECK_GRADLE
if not exist "%GRADLE_BIN_DIR%\gradle.bat" (
    echo Gradle executable not found after installation.
    pause
    exit /b 1
)

:RUN_GRADLE
echo Using Gradle from %GRADLE_BIN_DIR%
echo.
set "PATH=%GRADLE_BIN_DIR%;%PATH%"

cd /d "%APP_HOME%"

REM Check for ANDROID_HOME
if "%ANDROID_HOME%"=="" (
    if exist "%LOCALAPPDATA%\Android\Sdk" (
        set "ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk"
    ) else (
        echo.
        echo WARNING: ANDROID_HOME is not set.
        echo Please set it to your Android SDK location.
        echo For example: set ANDROID_HOME=C:\Users\%USERNAME%\AppData\Local\Android\Sdk
        echo.
    )
)

gradle.bat %*
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Build failed. Make sure Android SDK is installed.
    echo If not, install Android Studio and open this project in it.
    pause
    exit /b 1
)