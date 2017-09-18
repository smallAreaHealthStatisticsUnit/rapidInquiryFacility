REM Tomcat log4j2 setup
REM 
REM Add this script to %CATALINA_HOME%\bin
REM
REM A copy of this script is provided in %CATALINA_HOME%\webapps\rifServices\WEB-INF\classes\
REM
REM Do not set LOGGING_MANAGER to jul, tomcat will NOT sart
REM set LOGGING_MANAGER=org.apache.logging.log4j.jul.LogManager
set CATALINA_OPTS=-Djava.util.logging.manager=org.apache.logging.log4j.jul.LogManager -Dlog4j.configurationFile="%CATALINA_HOME%\conf\log4j2.xml"
REM
REM Add -Dlog4j2.debug=true if tomcat exceptions/does not start 
REM (catalina.bat run is useful if no output)
REM
REM Default CLASSPATH; no need to be added
REM set CLASSPATH=%CATALINA_HOME%\bin\bootstrap.jar;%CATALINA_HOME%\bin\tomcat-juli.jar
REM
REM Added JUL and Log4j2 to tomcat CLASSAPATH
set CLASSPATH=%CATALINA_HOME%\lib\log4j-core-2.9.0.jar;%CATALINA_HOME%\lib\log4j-api-2.9.0.jar;%CATALINA_HOME%\lib\log4j-jul-2.9.0.jar
REM
REM Do not do this, use CATALINA_OPTS instead. This will work on Linux
REM
REM set LOGGING_CONFIG="-Dlog4j.configurationFile=%CATALINA_HOME%\conf\log4j2.xml"
REM
REM EOf
