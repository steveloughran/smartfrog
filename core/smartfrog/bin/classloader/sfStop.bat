@echo off
setlocal

if defined SFHOME goto continue1
  if exist "%cd%\sfStop.bat" cd ..
  set SFHOME=%cd%
  cd bin
:continue1
if (%1) == () goto usage
if (%2) == () goto usage
rem call %SFHOME%\bin\setClassPath

set CLASSPATH=%SFHOME%\lib\smartfrog.jar;%SFHOME%\lib\sfServices.jar;%SFHOME%\lib\sfTestCases.jar;%CLASSPATH%  
set SERVER=localhost:8080
rem Please edit codebase if you have any other jar file in webserver 
set CODEBASE="http://%SERVER%/sfExamples.jar" 

java -Dorg.smartfrog.iniFile=%SFHOME%\bin\default.ini -Dorg.smartfrog.codebase=%CODEBASE% org.smartfrog.SFSystem -h %1 -t %2 -e
GOTO end
:usage
echo Insufficient arguments to use sfStop
echo Usage: sfStop HostName ApplicationName
:end
endlocal
