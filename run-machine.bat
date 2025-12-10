@echo off
echo ========================================
echo   LANCEMENT CLIENT MACHINE
echo ========================================
echo.

if "%1"=="" (
    echo Usage: run-machine.bat ^<machine_id^>
    echo.
    echo Exemples:
    echo   run-machine.bat 1    ^<- Machine M1
    echo   run-machine.bat 2    ^<- Machine M2 ^(backup^)
    echo   run-machine.bat 3    ^<- Machine M3
    echo   run-machine.bat 4    ^<- Machine M4
    echo   run-machine.bat 5    ^<- Machine M5
    echo.
    pause
    exit /b 1
)

echo Demarrage Machine M%1...
echo.
java -cp bin socket.client.MachineClient %1

pause
