@echo off
setlocal

set "JAVAC=javac"
where javac >nul 2>nul
if errorlevel 1 (
    set "JAVAC="
    for /d %%J in ("%ProgramFiles%\Java\jdk*") do if exist "%%~fJ\bin\javac.exe" set "JAVAC=%%~fJ\bin\javac.exe"
)
if not defined JAVAC (
    echo A JDK 8 or newer is required. Install a JDK and add javac to PATH.
    exit /b 1
)

if not exist build mkdir build
"%JAVAC%" -source 8 -target 8 -sourcepath src\main\java -d build src\main\java\parkour\Main.java
if errorlevel 1 exit /b 1
java -cp build parkour.Main
