@echo off
setlocal
cd /d %~dp0

set "JAVA_BIN="
where java >nul 2>nul && set "JAVA_BIN=java"
if defined JAVA_BIN goto run
if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_BIN=%JAVA_HOME%\bin\java.exe"
if defined JAVA_BIN goto run
if exist "C:\Users\LENOVO\android-dev-tools\jdk-extract\jdk-17.0.20.1+1\bin\java.exe" set "JAVA_BIN=C:\Users\LENOVO\android-dev-tools\jdk-extract\jdk-17.0.20.1+1\bin\java.exe"
if defined JAVA_BIN goto run
echo [ERROR] Java not found. Please install JDK 17 or set JAVA_HOME.
pause
exit /b 1

:run
start "CampusCall" "%JAVA_BIN%" -Dfile.encoding=UTF-8 -cp "CampusCall.jar;lib\org.eclipse.paho.client.mqttv3-1.2.5.jar" com.campus.call.Main
endlocal