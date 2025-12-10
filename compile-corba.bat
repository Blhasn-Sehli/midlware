@echo off
echo ========================================
echo   COMPILATION CORBA
echo ========================================
echo.

set JAVA8_HOME=C:\Users\sehli\AppData\Local\Programs\Eclipse Adoptium\jdk-8.0.472.8-hotspot
set PATH=%JAVA8_HOME%\bin;%PATH%

echo Utilisation de Java 8: %JAVA8_HOME%
echo.

echo [1/3] Compilation du fichier IDL...
cd corba-implementation
"%JAVA8_HOME%\bin\idlj" -fall ProductionControl.idl
if %ERRORLEVEL% NEQ 0 (
    echo [ERREUR] Compilation IDL echouee!
    cd ..
    pause
    exit /b 1
)
cd ..

echo.
echo [2/3] Compilation des classes generees...
"%JAVA8_HOME%\bin\javac" -encoding UTF-8 -d bin corba-implementation/ProductionControl/*.java
if %ERRORLEVEL% NEQ 0 (
    echo [ERREUR] Compilation classes generees echouee!
    pause
    exit /b 1
)

echo.
echo [3/3] Compilation du serveur et client CORBA...
"%JAVA8_HOME%\bin\javac" -encoding UTF-8 -d bin -cp bin corba-implementation/*.java
if %ERRORLEVEL% NEQ 0 (
    echo [ERREUR] Compilation serveur/client echouee!
    pause
    exit /b 1
)

echo.
echo ========================================
echo   COMPILATION CORBA TERMINEE !
echo ========================================
echo.
pause
