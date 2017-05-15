echo off

if defined SFHOME goto continue1
  if exist "%cd%\sfGui.bat" cd ..
  set SFHOME=%cd%
  cd ..
:continue1  
echo _
echo -------------------------
echo _
echo Stop sfDaemon (just in case)
start bin\sfStopDaemon localhost
PAUSE
echo _
echo *-----------------------*
echo _
echo Start sfDaemon
start bin\sfDaemon 
echo _
echo _
PAUSE
echo _
echo **---------------------**
echo _
echo Fail to deploy a sf decription file
start bin\sfStart localhost quickTest org/smartfrog/example/subprocesses/subprocess.sf
echo _
echo _
PAUSE
echo _
echo ***-------------------***
echo _
echo Deploy SubProcesses example sf decription file
start bin\sfStart localhost quickTest org/smartfrog/examples/subprocesses/subprocess.sf
echo _
echo _
PAUSE
echo _
echo ****-----------------****
echo _
echo Stop sdDaemon
 bin\sfStopDaemon localhost
echo _
echo _
PAUSE
echo _
echo *****---------------*****
echo _
 echo The End
