@echo off
echo ╔════════════════════════════════════════════════════╗
echo ║  Demarrage du Serveur Socket                       ║
echo ╚════════════════════════════════════════════════════╝
echo.

if not exist bin (
    echo ❌ Le repertoire 'bin' n'existe pas.
    echo    Veuillez compiler d'abord avec: compile-socket.bat
    pause
    exit /b 1
)

java -cp bin socket.server.ProductionControlServer
pause
