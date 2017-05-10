@echo off
rem  ------------------------------------------------------
rem  SET SFHOME property if not set before to <smartfrog_dist>. 
rem  Call this script from Smartfrog Distro dir or <smartfrog_dist>/bin. 
rem  ------------------------------------------------------

if defined SFHOME goto continue1
  if exist "%cd%\sfDaemon.bat" cd ..
  set SFHOME=%cd%
  cd ..
:continue1

echo SFHOME="%SFHOME%"
