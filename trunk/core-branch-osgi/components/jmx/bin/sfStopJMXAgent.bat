@echo off
setlocal

if defined SFHOME goto continue1
  if exist "%cd%\sfStopJMXAgent.bat" cd ..
  set SFHOME=%cd%
  cd bin
:continue1

call %SFHOME%\bin\setClassPath
java com.hp.SmartFrog.SFSystem -h localhost -t sfJMXAgent -e
endlocal
