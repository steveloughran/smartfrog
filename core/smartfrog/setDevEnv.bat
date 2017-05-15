echo off

set JAVA_HOME=c:\java\jdk\jdk1.7.0_25
rem set JAVA_HOME=c:\java\jdk1.8.0_74_x64
rem set JAVA_HOME=c:\java\jdk\jdk1.6.0_29x64\
rem set ANT_HOME=c:\java\ant17.final
rem set ANT_HOME=c:\java\ant-1.10.1
set ANT_HOME=c:\java\ant-1.9.9
rem set ANT_OPTS=-Dhttp.proxyHost=web-proxy -Dhttp.proxyPort=8088 -Xmx1G -Dclover.debug=true


set MY_SF_HOME=c:\code\smartfrog_SF\core\smartfrog\
set SFCOMPONENTS=c:\code\smartfrog_SF\\cvs_sf\core\components

rem set FORREST_HOME=D:\java\forrest\

rem set MAVEN_HOME=d:\java\mavenHome

rem set path=%MY_SF_HOME%\dist\bin;%SF_TEST_HARNESS%;%JAVA_HOME%;%JAVA_HOME%\bin;%ANT_HOME%\bin;%MAVEN_HOME%\bin;%path%
set path=%JAVA_HOME%;%JAVA_HOME%\bin;%ANT_HOME%\bin;%MY_SF_HOME%\dist\bin;%path%
set classpath=.;%classpath%;%SWT_JAR;
echo %path%

echo _
echo *-----------------------*
echo  JDK 1.7, ANT
echo      ----------------
java -version
ant -version
echo      ----------------
echo SFVersion:
java com.hp.SmartFrog.Version
echo _
echo *-----------------------*
echo _
rem cmd.exe
echo.
PAUSE
