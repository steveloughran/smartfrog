@echo off
setlocal

if defined SFHOME goto continue1
  if exist "%cd%\sfDetachAndTerminate.bat" cd ..
  set SFHOME=%cd%
  cd bin
:continue1
if (%1) == () goto usage
if (%1)==(-?) GOTO help
if (%2) == () goto usage

call "%SFHOME%\bin\setSFProperties"

%SFJVM% %SFCMDPARAMETERS%  org.smartfrog.SFSystem -a \"%2\":DETaTERM:::%1: -e

GOTO end
:usage
echo Insufficient arguments to use sfDetachAndTerminate
echo Usage: sfDetachAndTerminate HostName ComponentName
exit /B 69
:help
echo Usage: sfDetachAndTerminate HostName ComponentName
exit /B 0
:end
endlocal
