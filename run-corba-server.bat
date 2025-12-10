@echo off
set JAVA8_HOME=C:\Users\sehli\AppData\Local\Programs\Eclipse Adoptium\jdk-8.0.472.8-hotspot

echo ========================================
echo   SERVEUR CORBA - PRODUCTION
echo ========================================
echo.
echo Demarrage du serveur CORBA...
echo.

"%JAVA8_HOME%\bin\java" -cp bin ProductionControlServerCORBA -ORBInitialPort 1050 -ORBInitialHost localhost

pause
