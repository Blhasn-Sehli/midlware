@echo off
echo ╔════════════════════════════════════════════════════╗
echo ║  Compilation du Projet - Implementation Socket    ║
echo ╚════════════════════════════════════════════════════╝
echo.

REM Creer le repertoire de sortie
if not exist bin mkdir bin

echo 1️⃣  Compilation des classes communes...
javac -d bin common\*.java
if %errorlevel% neq 0 (
    echo    ❌ Erreur de compilation des classes communes
    pause
    exit /b 1
)
echo    ✅ Classes communes compilees

echo.
echo 2️⃣  Compilation du serveur Socket...
javac -cp bin -d bin socket-implementation\*.java
if %errorlevel% neq 0 (
    echo    ❌ Erreur de compilation du serveur
    pause
    exit /b 1
)
echo    ✅ Serveur Socket compile

echo.
echo ╔════════════════════════════════════════════════════╗
echo ║  ✅ Compilation terminee avec succes !             ║
echo ╚════════════════════════════════════════════════════╝
echo.
echo Pour executer:
echo   Serveur: run-socket-server.bat
echo   Client:  run-socket-client.bat
echo.
pause
