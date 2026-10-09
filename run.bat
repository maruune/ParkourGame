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
if exist build\sources.txt del build\sources.txt
for /r src\main\java %%F in (*.java) do echo "%%F" >> build\sources.txt

"%JAVAC%" -source 8 -target 8 -d build @build\sources.txt
if errorlevel 1 exit /b 1
java -cp build parkour.Main
