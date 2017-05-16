echo off

set JAVA_DIR=c:\_java
rem set JAVA_HOME=%JAVA_DIR%\jdk1.8.0_11
rem set JAVA_HOME=%JAVA_DIR%\jdk\jdk1.7.0_25
set JAVA_HOME=%JAVA_DIR%\jdk1.8.0_74_x64
rem set JAVA_HOME=%JAVA_DIR%\jdk\jdk1.6.0_29x64\

rem set ANT_HOME=%JAVA_DIR%\apache-ant-1.9.4
set ANT_HOME=%JAVA_DIR%\ant-1.9.9
set ANT_OPTS=-Dhttp.proxyHost=web-proxy -Dhttp.proxyPort=8088
rem set HTTP_PROXY=http://web-proxy:8080
rem set HTTPS_PROXY=http://web-proxy:8080

set M2_HOME=%JAVA_DIR%\apache-maven-3.2.3\

set MY_SF_HOME=c:\code\SmartFrog\ 
set SFCOMPONENTS=%MY_SF_HOME%\core\components

rem set path=%MY_SF_HOME%\dist\bin;%SF_TEST_HARNESS%;%JAVA_HOME%;%JAVA_HOME%\bin;%ANT_HOME%\bin;%MAVEN_HOME%\bin;%CVS_HOME%;%path%;
set path=%JAVA_HOME%;%JAVA_HOME%\bin;%ANT_HOME%\bin;%M2_HOME%\bin;%path%;
set path=%JAVA_HOME%;%JAVA_HOME%\bin;%ANT_HOME%\bin;%MY_SF_HOME%\dist\bin;%path%

set classpath=.;%classpath%;

echo %path%
echo _
echo *-----------------------*
echo  JDK version
echo      ----------------
java -version
echo      ----------------
REM echo SFVersion:
REM java com.hp.SmartFrog.Version
echo _
echo *-----------------------*
echo _
cmd.exe

