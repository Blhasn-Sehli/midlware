@echo off
set JAVA8_HOME=C:\Users\sehli\AppData\Local\Programs\Eclipse Adoptium\jdk-8.0.472.8-hotspot

echo ========================================
echo   CLIENT MACHINE CORBA
echo ========================================
echo.

if "%1"=="" (
    echo Usage: run-corba-machine.bat ^<machine_id^>
    echo.
    echo Exemples:
    echo   run-corba-machine.bat 1    ^<- Machine M1
    echo   run-corba-machine.bat 2    ^<- Machine M2 ^(backup^)
    echo   run-corba-machine.bat 3    ^<- Machine M3
    echo   run-corba-machine.bat 4    ^<- Machine M4
    echo   run-corba-machine.bat 5    ^<- Machine M5
    echo.
    pause
    exit /b 1
)

echo Demarrage Machine M%1 (CORBA)...
echo.
"%JAVA8_HOME%\bin\java" -cp bin MachineClientCORBA %1 -ORBInitialPort 1050 -ORBInitialHost localhost

pause
