
if defined SFHOME goto continue1
  if exist "%cd%\sfGui.bat" cd ..
  set SFHOME=%cd%
  cd ..
:continue1  
start bin\sfStopDaemon localhost
PAUSE
start bin\sfDaemon 
PAUSE
start bin\sfStart localhost quickTest org/smartfrog/examples/subprocesses/subprocess.sf
                             
PAUSE

 bin\sfStopDaemon localhost
 
 PAUSE