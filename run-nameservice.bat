@echo off
set JAVA8_HOME=C:\Users\sehli\AppData\Local\Programs\Eclipse Adoptium\jdk-8.0.472.8-hotspot

echo ========================================
echo   DEMARRAGE SERVICE DE NOMS CORBA
echo ========================================
echo.
echo Demarrage sur le port 1050...
echo.

start "NameService CORBA" "%JAVA8_HOME%\bin\tnameserv" -ORBInitialPort 1050

echo.
echo Service de noms demarre!
echo Port: 1050
echo.
echo Gardez cette fenetre ouverte pendant l'utilisation.
echo.
pause
