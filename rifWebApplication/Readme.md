RIF Web Services
================

# Contents

- [1. Installation Prerequistes](#1-installation-prerequistes)
   - [1.1 Apache Maven](#11-apache-maven)	
   - [1.2 Java Development Environment](#12-java-development-environment)	
   - [1.3 Apache Tomcat](#13-apache-tomcat)	
     - [1.3.1 Apache Tomcat on a single host](#131-apache-tomcat-on-a-single-host)	
     - [1.3.2 Apache Tomcat for internet use](#132-apache-tomcat-for-internet-use)	
	 - [1.3.3 Running Tomcat on the command line](#133-running-tomcat-on-the-command-line)
	 - [1.3.4 Running Tomcat as a service](#134-running-tomcat-as-a-service)
	 - [1.3.5 Middleware Logging (Log4j2) Setup](#135-middleware-logging-log4j2-setup)
	 - [1.3.6 Tomcat Logging (Log4j2) Setup](#136-tomcat-logging-log4j2-setup) 
	 - [1.3.7 Using JConsole with Tomcat](#137-using-jconsole-with-tomcat) 
  - [1.4 R](#14-r)	
- [2. Building Web Services using Maven](#2-building-web-services-using-maven)
   - [2.1 Building Using Make](#21-building-using-make)	
   - [2.2 Building Using a Windows Batch File](#22-building-using-a-windows-batch-file)	
   - [2.3 Building By Hand](#23-building-by-hand)	
- [3. Installing Web Services in Tomcat](#3-installing-web-services-in-tomcat)
   - [3.1 Web Services](#31-web-services)
     - [3.1.1 RIF Services](#311-rif-services)
     - [3.1.2 Taxonomy Service](#312-taxonomy-service)
   - [3.2 RIF Web Application](#32-rif-web-application)
- [4. RIF Setup](#4-rif-setup)
   - [4.1 Setup Database](#41-setup-database)
     - [4.1.1 SQL Server](#411-sql-server)
     - [4.1.2 Postgres](#412-postgres)
   - [4.2 Setup Network](#42-setup-network)
     - [4.2.1 TLS](#421-tls)
   - [4.3 Setup R](#43-setup-r)
     - [4.3.1 R Debugging](#431-r-debugging)
	 - [4.3.2 R Memory Management](#432-r-memory-management)
   - [4.4 Common Setup Errors](#44-common-setup-errors)
     - [4.4.1 Logon RIF Serice Call Incorrect](#441-logon-rif-serice-call-incorrect)
     - [4.4.2 TLS Errors](#442-tls-errors)
     - [4.4.3 Unable to unpack war files](#443-unable-to-unpack-war-files)
     - [4.4.4 No Taxonomy Services](#444-no-taxonomy-services)
	 - [4.4.5 RIF Services crash on logon](#445-rif-services-crash-on-logon)
	 - [4.4.6 SQL Server TCP/IP Java Connection Errors](#446-sql-server-tcpip-java-connection-errors)
- [ 5. Running the RIF](#5-running-the-rif)
   - [5.1 Logging On](#51-logging-on)
   - [5.2 Logon troubleshooting](#52-logon-troubleshooting)
   - [5.3 R Issues](#53-r-issues)
- [ 6. Patching](#6-patching)
   - [6.1 RIF Web Application](#61-rif-web-application)
   - [6.2 RIF Middleware](#62-rif-middleware)
   - [6.3 Tomcat](#63-tomcat)
   
# 1. Installation Prerequistes

These instructions are for Windows Apache Tomcat. Linux Tomcat will be very similar. It is assumed that the 
installer knows how to:

* Set environment variables; check settings; setup up the executable and library search paths
* Can install and de-install programs 
* Can start and stop system services
* Is able to administer the installation machine.

The RIF web application will install on a modern laptop.

Complex Apacahe Tomcat setup (e.g. clustering, runtime deployment of updated WAR files) are not within the scope of this document 
od this document and are not required for simple RIF setups.


## 1.1 Apache Maven

Apache Maven is required to build the RIF web application (War) files and the data loader tool from source. It is 
not required if you are supplied with pre-built copies.

Download and install Apache Maven: https://maven.apache.org/download.cgi

## 1.2 Java Runtime Development

The Java Runtime Environment (JRE) can be used if the war files are pre-supplied and the OWASP requirement to remove the 
version string from HTTP error messages by repacking  %CATALINA_HOME%/server/lib/catalina.jar with an updated 
*ServerInfo.properties* file is not required. 

Make sure all the older versions of Java are removed before you install Java; **especially the 32 bit versions**.
To test for you Java version, use *java -showversion*, e.g.
```
C:\Program Files\Apache Software Foundation\Tomcat 8.5\bin>java -showversion
java version "1.8.0_144"
Java(TM) SE Runtime Environment (build 1.8.0_144-b01)
Java HotSpot(TM) 64-Bit Server VM (build 25.144-b01, mixed mode)
...
```

Download and install the Java Development Environment (JDK): http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

**Make sure to install the 64 bit version of Java**, unless you have a 32 bit ONLY machine (*This is very unlikely 
and has not been tested - we DO NOT have any!*). The 32 bit version will cause 32/64 bit issues with R.

- If you use the Java Runtime Environment (JRE), set *JRE_HOME* in the environment (*C:\Program Files\Java\jre1.8.0_111*).
- If you use the Java Development Environment (JDK), set *JAVA_HOME* in the environment (*C:\Program Files\Java\jdk1.8.0_111*).
- Add the Java bin directory (*C:\Program Files\Java\jdk1.8.0_111\bin*) to the path.
- Test Java is installed correctly with *java -showversion* in a new command window.

JRE_HOME is used by the Apache tomcat manual start script *catalina.bat*. Normally, Java upgrades go into the same 
directory as installed, but if Java is upgraded by hand or re-installed these environment settings may need to 
be changed.

Use the configure Tomcat application (tomcat8w) to use the default Java installed on the machine. 
This prevents upgrades from breaking *tomcat*!
![alt text](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifWebApplication/tomcat8_configuration_3.PNG?raw=true "Setting Java version autodetect")
This makes tomcat Java uopgrade proof; but this may have unintended effects if:

* You have not removed all the old Java releases
* You install another version of Java (e.g. the Oracle installer may do this)
 
## 1.3 Apache Tomcat

Apache Tomcat can be downloaded from: https://tomcat.apache.org/download-80.cgi

Please use tomcat version 8, not 9 as we have not tested 9. The version tested was 8.5.13. It is advised to use the MSI
version.

### 1.3.1 Apache Tomcat on a single host

This is suitable for laptops and developers with no access from other machines. Download and install tomcat; make sure your firewall blocks 
port 8080. You do **NOT** need to follow the OWASP guidelines or to configure TLS.

### 1.3.2 Apache Tomcat for internet use

The is the normal production use case. It is important that Apache Tomcat is installed securely.

Download Apache Tomcat 8.5 and follow the [OWASP Tomcat guidelines](https://www.owasp.org/index.php/Securing_tomcat#Sample_Configuration_-_Good_Security) for securing tomcat with good security.

*Do not just install **Tomcat** without reading the instructions first*. In particular on Windows:

- Download the core windows service installer
- Start the installation, click Next and Agree to the licence
- Untick native, documentation, examples and webapps then click Next
- Choose an installation directory (referenced as *CATALINA_HOME* from now on), preferably on a different drive to the OS.
- Choose an administrator username (NOT admin) and a secure password that complies with your organisations password policy.
- Complete tomcat installation, but do not start service.
- Set *CATALINA_HOME* in the environment (e.g. *C:\Program Files\Apache Software Foundation\Tomcat 8.5*). If you do not do this the web 
  services will not work [The web services will crash on user logon if it is not set]; see:
  4.4.5 RIF Services crash on logon. If *CATALINA_HOME* is 
  *C:\Program Files (x86)\Apache Software Foundation\Tomcat 8.5* you have installed the 32 bit version of Java.
  Remove tomcat and Java and re-install a 64 bit Java (unless you are on a really old 32 bit only Machine...)
  
When accessed from the internet the RIF **must** be secured using TLS to protect the login details and any health data viewed.

Notes on the OWASP section on removing the version string from HTTP error messages by repacking *%CATALINA_HOME%/server/lib/catalina.jar* with 
an updated ServerInfo.properties:

* The JAR file is in: *%CATALINA_HOME%/lib/catalina.jar*
* The intention of this change is to defeat Lamdba probes by malicious pentration testers. This change **may** have the side affect of defeating your own 
  security assurance software (it appears to defeat Nessus). It may therefore be necessary to not implement this chnage until you have 
  completed security testing.

### 1.3.3 Running Tomcat on the command line

Tomcat can be run from the command line. The advantage of this is all the output appears in the same place! To do this the tomcat server must be
stopped (i.e. in the Windows services panel or via Linux runvel scripts (/etc/init.d/tomcat*). Notmally tomcat is run as a server (i.e. as a 
daemon in Unix parlance).

cd to %CATALINA_HOME%\bin; run *catalina.bat* with the parameter *start* or *stop*. 

Do NOT run *tomcat8.exe*; this will work but you will not be able to interupt Tomcat! (This is caused by the 
Java R interface remving the control-C handler)

e.g.
```
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility> cd %CATALINA_HOME%\bin
C:\Program Files\Apache Software Foundation\Tomcat 8.5\bin> catalina.bat start*
Using CATALINA_BASE:   "C:\Program Files\Apache Software Foundation\Tomcat 8.5"
Using CATALINA_HOME:   "C:\Program Files\Apache Software Foundation\Tomcat 8.5"
Using CATALINA_TMPDIR: "C:\Program Files\Apache Software Foundation\Tomcat 8.5\temp"
Using JRE_HOME:        "C:\Program Files\Java\jdk1.8.0_111"
Using CLASSPATH:       "C:\Program Files\Apache Software Foundation\Tomcat 8.5\bin\bootstrap.jar;C:\Program Files\Apache Software Fo
undation\Tomcat 8.5\bin\tomcat-juli.jar"
```

This pops up a Java scrollable window:
```
11-Apr-2017 14:38:54.070 INFO [main] org.apache.catalina.startup.VersionLoggerListener.log Server version:        Apache Tomcat/8.5.13
11-Apr-2017 14:38:54.074 INFO [main] org.apache.catalina.startup.VersionLoggerListener.log Server built:          Mar 27 2017 14:25:04 UTC
11-Apr-2017 14:38:54.074 INFO [main] org.apache.catalina.startup.VersionLoggerListener.log Server number:         8.5.13.0
11-Apr-2017 14:38:54.074 INFO [main] org.apache.catalina.startup.VersionLoggerListener.log OS Name:               Windows 8.1
11-Apr-2017 14:38:54.074 INFO [main] org.apache.catalina.startup.VersionLoggerListener.log OS Version:            6.3
11-Apr-2017 14:38:54.074 INFO [main] org.apache.catalina.startup.VersionLoggerListener.log Architecture:          amd64
11-Apr-2017 14:38:54.074 INFO [main] org.apache.catalina.startup.VersionLoggerListener.log Java Home:             C:\Program Files\Java\jre1.8.0_121
11-Apr-2017 14:38:54.074 INFO [main] org.apache.catalina.startup.VersionLoggerListener.log JVM Version:           1.8.0_121-b13
11-Apr-2017 14:38:54.074 INFO [main] org.apache.catalina.startup.VersionLoggerListener.log JVM Vendor:            Oracle Corporation

11-Apr-2017 14:38:54.074 INFO [main] org.apache.catalina.startup.VersionLoggerListener.log CATALINA_BASE:         C:\Program Files\Apache Software Foundation\Tomcat 8.5
11-Apr-2017 14:38:54.074 INFO [main] org.apache.catalina.startup.VersionLoggerListener.log CATALINA_HOME:         C:\Program Files\Apache Software Foundation\Tomcat 8.5
11-Apr-2017 14:38:54.074 INFO [main] org.apache.catalina.startup.VersionLoggerListener.log Command line argument: -Dcatalina.home=C:\Program Files\Apache Software Foundation\Tomcat 8.5
11-Apr-2017 14:38:54.074 INFO [main] org.apache.catalina.startup.VersionLoggerListener.log Command line argument: -Dcatalina.base=C:\Program Files\Apache Software Foundation\Tomcat 8.5
11-Apr-2017 14:38:54.074 INFO [main] org.apache.catalina.startup.VersionLoggerListener.log Command line argument: -Djava.io.tmpdir=C:\Program Files\Apache Software Foundation\Tomcat 8.5\temp
11-Apr-2017 14:38:54.074 INFO [main] org.apache.catalina.startup.VersionLoggerListener.log Command line argument: -Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager
11-Apr-2017 14:38:54.074 INFO [main] org.apache.catalina.startup.VersionLoggerListener.log Command line argument: -Djava.util.logging.config.file=C:\Program Files\Apache Software Foundation\Tomcat 8.5\conf\logging.properties
...
12-Apr-2017 14:24:38.326 SEVERE [main] org.apache.catalina.core.StandardServer.await StandardServer.await: create[localhost:8005]:
 java.net.BindException: Address already in use: JVM_Bind
        at java.net.DualStackPlainSocketImpl.bind0(Native Method)
        at java.net.DualStackPlainSocketImpl.socketBind(Unknown Source)
        at java.net.AbstractPlainSocketImpl.bind(Unknown Source)
        at java.net.PlainSocketImpl.bind(Unknown Source)
        at java.net.ServerSocket.bind(Unknown Source)
        at java.net.ServerSocket.<init>(Unknown Source)
        at org.apache.catalina.core.StandardServer.await(StandardServer.java:440)
        at org.apache.catalina.startup.Catalina.await(Catalina.java:743)
        at org.apache.catalina.startup.Catalina.start(Catalina.java:689)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(Unknown Source)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(Unknown Source)
        at java.lang.reflect.Method.invoke(Unknown Source)
        at org.apache.catalina.startup.Bootstrap.start(Bootstrap.java:355)
        at org.apache.catalina.startup.Bootstrap.main(Bootstrap.java:495)
```

* In this case the service is still running, hence the *Address already in use* error;
* You are advised to make the Java window and buffer bigger; e.g. 132x40 with a 132x9999 line buffer;
* To abort, use *catalina.bat stop* or quit the Java window. Use of control-C in the Java Window 
  will not work once a study have been run.

Two scripts are provided to start and stop the RIF from the command line:
  
* start_rif.bat
* stop_rif.bat

These can be placed on the desktop. The shortcuts created then need to be run as an Adminstrator.

  ![alt text](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifWebApplication/setting_runas_administrator.png?raw=true "Make a shortcut run as an administrator")

When running Tomcat at the command line on Windows 10 the new Unix like copy paste functionality will prevent
the buffer from scrolling and thence cause tomcat to hang. This can be alleviated by typing <enter> or 
<return> in the log window and fixed by changing the properies of the log window:

  ![alt text](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifWebApplication/tomcat_console_properties.png?raw=true "Windows 10 Tomcat console window properties")

### 1.3.4 Running Tomcat as a service
  
* Use the configure Tomcat application (tomcat8w) to make the startup type automatic.

  ![alt text](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifWebApplication/tomcat8_configuration_1.png?raw=true "Make the startup type automatic")

* Use the configure Tomcat application (tomcat8w) to set the logging level to debug.

  ![alt text](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifWebApplication/tomcat8_configuration_2.PNG?raw=true "Set the logging level to debug")

* Edit %CATALINA_HOME%/conf/logging.properties and change the default log level enable debugging 
  (*ALL* not *DEBUG*!):
  ```
  ############################################################
  # Facility specific properties.
  # Provides extra control for each logger.
  ############################################################
  
  # org.apache.catalina.core.ContainerBase.[Catalina].[localhost].level = INFO
  org.apache.catalina.core.ContainerBase.[Catalina].[localhost].level = ALL
  ```
  
* Restart Tomcat using the configure Tomcat application (tomcat8w) or the services panel.   
  The *tomcat* output trace will appear in %CATALINA_HOME%/logs as:
  *tomcat8-stderr.<date in format YYYY-MM-DD>* and also possibly *tomcat8-stdout.<date in format YYYY-MM-DD>*.

### 1.3.5 Middleware Logging (Log4j2) Setup

The RIF middleware now uses Log4j version 2 for logging. The configuration file: 
*%CATALINA_HOME%\webapps\rifServices\WEB-INF\classes\log4j2.xml* sets up two loggers:
  
  1. The default logger: *rifGenericLibrary.util.RIFLogger* used by the middleware: RIF_middleware.log
  2. "Other" for logger output not from *rifGenericLibrary.util.RIFLogger*: Other.log
  
  Logs go to STDOUT and ```%CATALINA_HOME%/log4j2/<YYYY>-<MM>/RIF_middleware.log-<N>``` and ```%CATALINA_HOME%/log4j2/<YYYY>-<MM>/Other.log-<N>```
  where ```<YYYY>``` is the  year, ```<MM>``` is the numeric month numeric and ```<N>``` is the log sequence number.
  Other messages go to the console. RIF middleware message DO NOT go to the console so we can find
  messages not using *rifGenericLibrary.util.RIFLogger*
  
  Logs are rotated everyday or every 100 MB in the year/month specific directory
  
  Typical log entry: 
```
14:29:37.812 [http-nio-8080-exec-5] INFO  rifGenericLibrary.util.RIFLogger: [rifServices.dataStorageLayer.pg.PGSQLRIFContextManager]:
PGSQLAbstractSQLManager logSQLQuery >>>
QUERY NAME: getGeographies
PARAMETERS:
PGSQL QUERY TEXT: 
SELECT DISTINCT 
   geography 
FROM 
   rif40_geographies 
ORDER BY 
   geography ASC;
<<< End PGSQLAbstractSQLManager logSQLQuery
```
	
  Configuration file:
	
```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="debug" monitorInterval="30" name="RIF Services Default">
  <Properties>
    <!-- Log file names -->
    <Property name="rif_middleware">RIF_middleware.log</Property>
    <Property name="other">Other.log</Property>
    <Property name="rif_log_pattern">%d{HH:mm:ss.SSS} [%t] %-5level: %msg%n</Property> 
										<!-- No logging source; always rifGenericLibrary.util.RIFLogger -->
    <Property name="other_log_pattern">%d{HH:mm:ss.SSS} [%t] %-5level %logger{36}: %msg%n</Property>
  </Properties>
  <Appenders>
    <Console name="Console" target="SYSTEM_OUT" direct="true">
      <PatternLayout pattern="${other_log_pattern}"/>
    </Console>
	<!-- File logs are in ${catalina.base}/log4j2 - %CATALINA_HOME/log4j2 -->
    <RollingFile name="RIF_middleware" 
				 filePattern="${sys:catalina.base}/log4j2/$${date:yyyy-MM}/${rif_middleware}-%d{yyyy-MM-dd}-%i.log"
				 immediateFlush="true" bufferedIO="true" bufferSize="1024">
      <PatternLayout pattern="${rif_log_pattern}"/>
	  <Policies>
		<TimeBasedTriggeringPolicy />              <!-- Rotated everyday -->
		<SizeBasedTriggeringPolicy size="100 MB"/> <!-- Or every 100 MB -->
	  </Policies>
    </RollingFile>
    <RollingFile name="Other" 
				 filePattern="${sys:catalina.base}/log4j2/$${date:yyyy-MM}/${other}-%d{yyyy-MM-dd}-%i.log"
				 immediateFlush="false" bufferedIO="true" bufferSize="1024">
      <PatternLayout pattern="${other_log_pattern}"/>
	  <Policies>
		<TimeBasedTriggeringPolicy />              <!-- Rotated everyday -->
		<SizeBasedTriggeringPolicy size="100 MB"/> <!-- Or every 100 MB -->
	  </Policies>
    </RollingFile>
  </Appenders>
  <Loggers>
    <!-- Default logger: rifGenericLibrary.util.RIFLogger -->
    <Logger name="rifGenericLibrary.util.RIFLogger" level="info" additivity="false">
      <!-- Disable the console to check all messages go through rifGenericLibrary.util.RIFLogger -->
      <!-- <AppenderRef ref="Console"/> uncomment to see RIF middleware output on the console -->
      <AppenderRef ref="RIF_middleware"/>
    </Logger>
	<!-- Other logging -->
    <Root level="trace">
      <AppenderRef ref="Console"/>
      <AppenderRef ref="Other"/>
    </Root>
  </Loggers>
</Configuration>
```

**To send  RIF output to the console uncomment: 
```<!-- <AppenderRef ref="Console"/> uncomment to see RIF middleware output on the console -->```**. 

R stdout/stderroutput always appears on the console and cannot be redirected:
```
Rengine.eval(rm(list=ls())): BEGIN Thread[http-nio-8080-exec-8,5,main]
Rengine.eval(rm(list=ls())): END (OK)Thread[http-nio-8080-exec-8,5,main]
Rengine.eval(print(.libPaths())): BEGIN Thread[http-nio-8080-exec-8,5,main]
Rengine.eval(print(.libPaths())): END (OK)Thread[http-nio-8080-exec-8,5,main]
Rengine.eval(print(sessionInfo())): BEGIN Thread[http-nio-8080-exec-8,5,main]
Rengine.eval(print(sessionInfo())): END (OK)Thread[http-nio-8080-exec-8,5,main]
Rengine.eval(source("C:\\Program Files\\Apache Software Foundation\\Tomcat 8.5\\webapps\\rifServices\\WEB-INF\\classes\\Adj_Cov_Smoo
th_JRI.R")): BEGIN Thread[http-nio-8080-exec-8,5,main]
Rengine.eval(source("C:\\Program Files\\Apache Software Foundation\\Tomcat 8.5\\webapps\\rifServices\\WEB-INF\\classes\\Adj_Cov_Smoo
th_JRI.R")): END (OK)Thread[http-nio-8080-exec-8,5,main]
Rengine.eval(as.integer(a <- runRSmoothingFunctions())): BEGIN Thread[http-nio-8080-exec-8,5,main]
Rengine.eval(as.integer(a <- runRSmoothingFunctions())): END (OK)Thread[http-nio-8080-exec-8,5,main]
Terminating R thread.
```

Logging output within the application is controlled in three ways:

1. The logging level. This should be WARN, DEBUG or INFO. INFO is normally sufficient
2. INFO logging is controlled by class using the properties file; these are searched for in the order:

   * *%CATALINA_HOME%\comf\RIFLogger.properties*
   * *%CATALINA_HOME%\webapps\rifServices\WEB-INF\classes\RIFLogger.properties*
   
   The source is in: rapidInquiryFacility\rifServices\src\main\resources\RIFLogger.properties*. Note that
   most database later classes have Postgres and SQLServer versions so have two entries.
   
```
#
# All logging is usually enabled unless it is irritating 
# (e.g. rifServices.restfulWebServices.pg.PGSQLRIFStudySubmissionWebServiceResource)
#
rifServices.system.RIFServiceStartupOptions=true

taxonomyServices.RIFTaxonomyWebServiceApplication=true
taxonomyServices.WebServiceResponseUtility=true
taxonomyServices.ICD10TaxonomyTermParser=true

rifGenericLibrary.dataStorageLayer.pg.PGSQLQueryUtility=true
rifGenericLibrary.dataStorageLayer.pg.PGUserDatabaseConnections=true

rifServices.restfulWebServices.pg.PGSQLRIFStudySubmissionWebServiceResource=true

rifServices.dataStorageLayer.pg.PGSQLProductionRIFStudyRetrievalService=false

rifServices.dataStorageLayer.pg.PGSQLRIFContextManager=true

...

```
3. SQL Query INFO logging is controlled by query name using the properties file; these are searched for in the order:

   * *%CATALINA_HOME%\comf\AbstractSQLManager.properties*
   * *%CATALINA_HOME%\webapps\rifServices\WEB-INF\classes\AbstractSQLManager.properties*
   
   The source is in: rapidInquiryFacility\rifGenericLibrary\src\main\resources\AbstractSQLManager.properties*
```
# MS/PGSQL AbstractSQLManager.logSQLQuery logging enabler/disabler by queryName
# To enable queryName must be set to TRUE (any case)
#
# Default: GET methods are FALSE
#
getGeographies=false
getHealthThemes=false
getProjects=false
getAgeIDQuery=false

...

#
# Default: DO methods are TRUE
#
createStatusTable=true
```

### 1.3.6 Tomcat Logging (Log4j2) Setup

This uses the log4j JDK Logging Adapter. The JDK Logging Adapter is a custom implementation of 
java.util.logging.LogManager that uses Log4j. 

Create a log4j version 2 XML configuation file for tomcat. Two choices are provided here:

1. Stock tomcat logging, as per the standard setup but in the *%CATALINA_HOME%/log4j2 diretory*. Beware that 
   this configuiartion will overide the Middleware Logging (Log4j2) Setup in the previous section.
2. RIF Tomcat logging to the following files:

   * ```%CATALINA_HOME%/log4j2/<YYYY>-<MM>/tomcat.log-<N>```: Tomcat/catalina messages (org.apache.catalina.core.ContainerBase.[Catalina].[localhost]*)
   * ```%CATALINA_HOME%/log4j2/<YYYY>-<MM>/RIF_middleware.log-<N>```: RIF messages
   * ```%CATALINA_HOME%/log4j2/<YYYY>-<MM>/Other.log-<N>```: Other messages
   
   where ```<YYYY>``` is the  year, ```<MM>``` is the numeric month numeric and ```<N>``` is the log sequence number.
   Logs are rotated everyday or every 100 MB in the year/month specific directory

   This configuration superceeds the rifServices configuration file.
   
Both send non RIF output to the console.

The configuration file is in *%CATALINA_HOME%/conf/log4j2.xml*. This configuration file completely replaces the 
configuration in the previous section (which is a subset).

Stock tomcat logging configuarion:
```xml
<?xml version="1.0" encoding="utf-8"?>
<Configuration status="info" monitorInterval="30" name="Tomcat Default">
  <Properties>
    <Property name="logdir">${sys:catalina.base}/log4j2</Property>
    <Property name="layout">%d [%t] %-5p %c- %m%n</Property>
  </Properties>
  <Appenders>
    <Console name="CONSOLE" target="SYSTEM_OUT">
      <PatternLayout pattern="${layout}"/>
    </Console>
    <RollingFile name="CATALINA"
        fileName="${logdir}/catalina.log"
        filePattern="${logdir}/catalina.%d{yyyy-MM-dd}-%i.log">
      <PatternLayout pattern="${layout}"/>
      <Policies>
        <TimeBasedTriggeringPolicy />
        <SizeBasedTriggeringPolicy size="1 MB"/>
      </Policies>
      <DefaultRolloverStrategy max="10"/>
    </RollingFile>
    <RollingFile name="LOCALHOST"
        fileName="${logdir}/localhost.log"
        filePattern="${logdir}/localhost.%d{yyyy-MM-dd}-%i.log">
      <PatternLayout pattern="${layout}"/>
      <Policies>
        <TimeBasedTriggeringPolicy />
        <SizeBasedTriggeringPolicy size="1 MB"/>
      </Policies>
      <DefaultRolloverStrategy max="10"/>
    </RollingFile>
    <RollingFile name="MANAGER"
        fileName="${logdir}/manager.log"
        filePattern="${logdir}/manager.%d{yyyy-MM-dd}-%i.log">
      <PatternLayout pattern="${layout}"/>
      <Policies>
        <TimeBasedTriggeringPolicy />
        <SizeBasedTriggeringPolicy size="1 MB"/>
      </Policies>
      <DefaultRolloverStrategy max="10"/>
    </RollingFile>
    <RollingFile name="HOST-MANAGER"
        fileName="${logdir}/host-manager.log"
        filePattern="${logdir}/host-manager.%d{yyyy-MM-dd}-%i.log">
      <PatternLayout pattern="${layout}"/>
      <Policies>
        <TimeBasedTriggeringPolicy />
        <SizeBasedTriggeringPolicy size="1 MB"/>
      </Policies>
      <DefaultRolloverStrategy max="10"/>
    </RollingFile>
  </Appenders>
  <Loggers>
    <Root level="info">
      <AppenderRef ref="CATALINA"/>
    </Root>
    <Logger name="org.apache.catalina.core.ContainerBase.[Catalina].[localhost]"
        level="info" additivity="false">
      <AppenderRef ref="LOCALHOST"/>
    </Logger>
    <Logger name="org.apache.catalina.core.ContainerBase.[Catalina].[localhost].[/manager]"
        level="info" additivity="false">
      <AppenderRef ref="MANAGER"/>
    </Logger>
    <Logger name="org.apache.catalina.core.ContainerBase.[Catalina].[localhost].[/host-manager]"
        level="info" additivity="false">
      <AppenderRef ref="HOST-MANAGER"/>
    </Logger>
  </Loggers>
</Configuration>
```

RIF Tomcat logging configuarion. This file must be placed in: *%CATALINA_HOME%\comf\log4j2.xml*
and an example is found in: *%CATALINA_HOME%\webapps\rifServices\WEB-INF\classes\log4j2.xml*
   
The source is in: *rapidInquiryFacility\rifGenericLibrary\src\main\resources\log4j2.xml*

**To send  RIF output to the console uncomment: 
```<!-- <AppenderRef ref="CONSOLE"/> uncomment to see RIF middleware output on the console -->```**. 

```xml
<?xml version="1.0" encoding="utf-8"?>
<Configuration status="debug" monitorInterval="30" name="RIF Tomcat Default">
  <Properties>
   <Properties>
    <Property name="logdir">${sys:catalina.base}/log4j2/$${date:yyyy-MM}</Property>
    <Property name="default_log_pattern">%d{HH:mm:ss.SSS} [%t] %-5level %class %logger{36}: %msg%n</Property>
    <Property name="rif_log_pattern">%d{HH:mm:ss.SSS} [%t] %-5level %class : %msg%n</Property> 
    <Property name="other_log_pattern">%d{HH:mm:ss.SSS} [%t] %-5level %class %logger{36}: %msg%n</Property>
  </Properties>
  <Appenders>
    <Console name="CONSOLE" target="SYSTEM_OUT">
      <PatternLayout pattern="${default_log_pattern}"/>
    </Console>
	<!-- File logs are in ${catalina.base}/log4j2 - %CATALINA_HOME%/log4j2 -->
	
    <RollingFile name="RIF_MIDDLEWARE" 
				 filePattern="${logdir}/RIF_middleware.%d{yyyy-MM-dd}-%i.log"
				 immediateFlush="true" bufferedIO="true" bufferSize="1024">
      <PatternLayout pattern="${rif_log_pattern}"/>
	  <Policies>
		<TimeBasedTriggeringPolicy />              <!-- Rotated everyday -->
		<SizeBasedTriggeringPolicy size="100 MB"/> <!-- Or every 100 MB -->
	  </Policies>
    </RollingFile>
    <RollingFile name="OTHER" 
				 filePattern="${logdir}/Other.%d{yyyy-MM-dd}-%i.log"
				 immediateFlush="true" bufferedIO="true" bufferSize="1024">
      <PatternLayout pattern="${other_log_pattern}"/>
	  <Policies>
		<TimeBasedTriggeringPolicy />              <!-- Rotated everyday -->
		<SizeBasedTriggeringPolicy size="100 MB"/> <!-- Or every 100 MB -->
	  </Policies>
    </RollingFile>	 
    <RollingFile name="TOMCAT"
				 filePattern="${logdir}/tomcat.%d{yyyy-MM-dd}-%i.log"
				 immediateFlush="true" bufferedIO="true" bufferSize="1024">
      <PatternLayout pattern="${other_log_pattern}"/>
      <Policies>
		<TimeBasedTriggeringPolicy />              <!-- Rotated everyday -->
		<SizeBasedTriggeringPolicy size="100 MB"/> <!-- Or every 100 MB -->
      </Policies>
    </RollingFile>
  </Appenders>
  
  <Loggers>

	<!-- Other logging -->
    <Root level="info">
	  <AppenderRef ref="CONSOLE"/>
      <AppenderRef ref="OTHER"/>
    </Root>
	
	<!-- tomcat logging -->
    <Logger name="org.apache.catalina.core.ContainerBase.[Catalina].[localhost]"
        level="info" additivity="false">
      <AppenderRef ref="TOMCAT"/>
	  <AppenderRef ref="CONSOLE"/>
    </Logger>
    <Logger name="org.apache.catalina.core.ContainerBase.[Catalina].[localhost].[/manager]"
        level="info" additivity="false">
      <AppenderRef ref="TOMCAT"/>
	  <AppenderRef ref="CONSOLE"/>
    </Logger>
    <Logger name="org.apache.catalina.core.ContainerBase.[Catalina].[localhost].[/host-manager]"
        level="info" additivity="false">
      <AppenderRef ref="TOMCAT"/>
	  <AppenderRef ref="CONSOLE"/>
    </Logger>

      <!-- RIF middleware logger: rifGenericLibrary.util.RIFLogger -->
    <Logger name="rifGenericLibrary.util.RIFLogger"
		level="info" additivity="false">
      <!-- <AppenderRef ref="CONSOLE"/> uncomment to see RIF middleware output on the console -->
      <AppenderRef ref="RIF_MIDDLEWARE"/>
    </Logger>
		
  </Loggers>
</Configuration>
```

Create an environment overrides file for catalina.bat as %CATALINA_HOME%\bin\setenv.bat. A copy is provided in:
%CATALINA_HOME%\webapps\rifServices\WEB-INF\classes
```bat
REM Tomcat log4j2 setup
REM 
REM Add this script to %CATALINA_HOME%\bin
REM
REM A copy of this script is provided in %CATALINA_HOME%\webapps\rifServices\WEB-INF\classes\
REM
REM Do not set LOGGING_MANAGER to jul, tomcat will NOT sart
REM set LOGGING_MANAGER=org.apache.logging.log4j.jul.LogManager
REM
REM To enable Jconsole add %ENABLE_JMX% to CATALINA_OPTS. Set to run on port 9999 and only allow connections from localhost
REM
set ENABLE_JMX=-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9999 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=localhost
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
```
Add the following files to *%CATALINA_HOME%\lib*:

* log4j-api-2.9.0.jar
* log4j-core-2.9.0.jar
* log4j-jul-2.9.0.jar

If you use Maven to build the Middleware, these files are in subdirectories below 
*%USER%\.m2\repository\org\apache\logging\log4j\*

Do NOT set the enviroment variables LOGGING_MANAGER or LOGGING_CONFIG.
Do set set:
```
CATALINA_OPTS=-Djava.util.logging.manager=org.apache.logging.log4j.jul.LogManager -Dlog4j.configurationFile="%CATALINA_HOME%\conf\log4j2.xml"
CLASSPATH=%CATALINA_HOME%\lib\log4j-core-2.9.0.jar;%CATALINA_HOME%\lib\log4j-api-2.9.0.jar;%CATALINA_HOME%\lib\log4j-jul-2.9.CONSOLE0.jar
```

Debugging:

* Adding  -Dlog4j2.debug=true to CATALINA_OPTS if tomcat exceptions/does not start 
* Use catalina.bat run if there is no output from te script and the Java windows disappears immediately
* Set the configuration status to **debug**

### 1.3.7 Using JConsole with Tomcat

The JConsole graphical user interface is a monitoring tool for Java applications. JConsole is composed of six tabs:

* Overview: Displays overview information about the Java VM and monitored values.
* Memory: Displays information about memory use.
* Threads: Displays information about thread use.
* Classes: Displays information about class loading.
* VM: Displays information about the Java VM.
* MBeans: Displays information about MBeans

See: http://docs.oracle.com/javase/8/docs/technotes/guides/management/jconsole.html

The Java Development Kit (JDK) must be installed.

Set the following *CATALINA_OPTS* in *%CATALINA_HOME%\bin\setenv.bat*:

```
-Dcom.sun.management.jmxremote
-Dcom.sun.management.jmxremote.port=9999
-Dcom.sun.management.jmxremote.authenticate=false
-Dcom.sun.management.jmxremote.ssl=false 
-Djava.rmi.server.hostname=localhost
```

Run Jconsole from *%JAVA_HOME%\bin* e.g. ```"%JAVA_HOME%\bin\Jconsole"```

 ![alt text](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifWebApplication/Jconsole.png?raw=true "Jconsole")

## 1.4 R
Download and install R: https://cran.ma.imperial.ac.uk/bin/windows/base

See: [R setup](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifWebApplication/Readme.md#43-setup-r)

As with Java, do NOT use the 32 bit only version unless you have to. These instructions assume you you the 64 
bit version

# 2. Building Web Services using Maven

## 2.1 Building Using Make

If you have installed make (i.e. you are building the Postgrs port from Scratch), run make from the 
root of the github repository, e.g. *C:\Users\Peter\Documents\GitHub\rapidInquiryFacility*

This method requires 7zip to be installed in *C:\Program Files\7-Zip\7z.exe*

The following make targets are provided:

* *clean*: remove targets, clean maven build areas
* *all*: build targets
* *install*: clean then all
* *rifservice*: build rifServices.war target
* *taxonomyService*: build taxonomyServices.war target
* *RIF4*: build RIF4.7z target

To run a make target type *make <target>;e.g. *make install*.

The following files are then built and copied into the rapidInquiryFacility directory: 
*taxonomyServices.war*, *rifServices.war*, *RIF4.7z*

**Make currently only works on Windows and requires the Mingw development kit and 7zip to be installed.**

## 2.2 Building Using a Windows Batch File

This method requires 7zip to be installed in *C:\Program Files\7-Zip\7z.exe*

Run *java_build.bat* from the root of the github repository, 
e.g. *C:\Users\Peter\Documents\GitHub\rapidInquiryFacility*. The files *taxonomyServices.war*, 
*rifServices.war*, *RIF4.7z* are the end product.

```
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility>java_build.bat

C:\Users\Peter\Documents\GitHub\rapidInquiryFacility>ECHO OFF

C:\Users\Peter\Documents\GitHub\rapidInquiryFacility>SET PWD=C:\Users\Peter\Documents\GitHub\rapidInquiryFacility

C:\Users\Peter\Documents\GitHub\rapidInquiryFacility>call mvn --version
Apache Maven 3.3.9 (bb52d8502b132ec0a5a3f4c09453c07478323dc5; 2015-11-10T16:41:47+00:00)
Maven home: C:\Program Files\Apache Software Foundation\apache-maven-3.3.9\bin\..
Java version: 1.8.0_111, vendor: Oracle Corporation
Java home: C:\Program Files\Java\jdk1.8.0_111\jre
Default locale: en_GB, platform encoding: Cp1252
OS name: "windows 8.1", version: "6.3", arch: "amd64", family: "dos"
[INFO] Scanning for projects...
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] Building rifGenericLibrary 0.0.1-SNAPSHOT
[INFO] ------------------------------------------------------------------------
[INFO]
[INFO] --- maven-clean-plugin:2.5:clean (default-clean) @ rifGenericLibrary ---
[INFO] Deleting C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifGenericLibrary\target
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 0.564 s
[INFO] Finished at: 2017-04-27T09:34:05+01:00
[INFO] Final Memory: 7M/245M
[INFO] ------------------------------------------------------------------------
[INFO] Scanning for projects...
[WARNING]
[WARNING] Some problems were encountered while building the effective model for rapidInquiryFacility:taxonomyServices:war:0.0.1-SNAP
SHOT
[WARNING] 'dependencies.dependency.(groupId:artifactId:type:classifier)' must be unique: rapidInquiryFacility:rifGenericLibrary:jar
-> duplicate declaration of version 0.0.1-SNAPSHOT @ line 133, column 16
[WARNING]
[WARNING] It is highly recommended to fix these problems because they threaten the stability of your build.
[WARNING]
[WARNING] For this reason, future Maven versions might no longer support building such malformed projects.
[WARNING]
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Build Order:
[INFO]
[INFO] General RIF Tool Suite Settings
[INFO] rifGenericLibrary
[INFO] RIF Middleware
[INFO] taxonomyServices
[INFO] rifDataLoaderTool
[INFO] RIF IT Governance Tool
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] Building General RIF Tool Suite Settings 0.0.1-SNAPSHOT
[INFO] ------------------------------------------------------------------------
...
2017-02-01 13:19:42 ....A         4524               utils\services\rifs-util-JSON.js
2017-03-20 08:11:00 ....A        20129               utils\services\rifs-util-leafletdraw.js
2017-02-01 13:19:42 ....A         2685               utils\services\rifs-util-mapping.js
2017-02-01 13:19:42 ....A         2329               utils\services\rifs-util-uigrid.js
------------------- ----- ------------ ------------  ------------------------
2017-04-12 09:24:32            4920556      2238557  199 files, 35 folders

C:\Users\Peter\Documents\GitHub\rapidInquiryFacility>
```

## 2.3 Building By Hand

Otherwise run the following commands by hand from the 
root of the github repository, e.g. *C:\Users\Peter\Documents\GitHub\rapidInquiryFacility*:

```
mvn --version
cd rifGenericLibrary
mvn -Dmaven.test.skip=true install
cd ..\rapidInquiryFacility
mvn -Dmaven.test.skip=true install
cd ..\rifServices
mvn -Dmaven.test.skip=true install
```

Maven produces a lot of output:
```
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility>make
mvn --version
Apache Maven 3.3.9 (bb52d8502b132ec0a5a3f4c09453c07478323dc5; 2015-11-10T16:41:47+00:00)
Maven home: c:\Program Files\Apache Software Foundation\apache-maven-3.3.9
Java version: 1.8.0_111, vendor: Oracle Corporation
Java home: c:\Program Files\Java\jdk1.8.0_111\jre
Default locale: en_GB, platform encoding: Cp1252
OS name: "windows 8.1", version: "6.3", arch: "amd64", family: "dos"
cd rifGenericLibrary; mvn -Dmaven.test.skip=true install
[INFO] Scanning for projects...
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] Building rifGenericLibrary 0.0.1-SNAPSHOT
[INFO] ------------------------------------------------------------------------
Downloading: https://repo.maven.apache.org/maven2/commons-codec/commons-codec/maven-metadata.xml
Downloaded: https://repo.maven.apache.org/maven2/commons-codec/commons-codec/maven-metadata.xml (612 B at 1.1 KB/sec)
```
This then continues...
```
[INFO] Installing c:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifServices\pom.xml to C:\Users\Peter\.m2\repository\rapidInq
uiryFacility\rifServices\0.0.1-SNAPSHOT\rifServices-0.0.1-SNAPSHOT.pom
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 9.500 s
[INFO] Finished at: 2017-04-11T13:54:18+01:00
[INFO] Final Memory: 23M/360M
[INFO] ------------------------------------------------------------------------
```
*Any message other than **[INFO] BUILD SUCCESS** indicates a build error. Do not install the output war files and report the fault to the 
development team. 

The order is important; the directories must be built in the order: rifGenericLibrary, rapidInquiryFacility, rifServices. It is always
assumed you build taxonomyServices later. If you get a build failure try a *mvn clean* in each directory first; then retry with a 
*mvn  -Dmaven.test.skip=true install*.

This method  does not build the *taxonomyServices* or the web application 7zip file.

# 3. Installing Web Services in Tomcat

## 3.1 Web Services

### 3.1.1 RIF Services

* Copy *rifServices.war* from: *rapidInquiryFacility\rifServices\target*, e.g. *C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifServices\target*
  to: *%CATALINA_HOME%\webapps*, e.g. *C:\Program Files\Apache Software Foundation\Tomcat 8.5\webapps*

### 3.1.2 Taxonomy Service

If SAHSU has supplied a taxonomyServices.war file skip to step 3.

1) Get the Taxonomy Service XML file *ClaML.dtd*. This is stored in is stored in ...rifServices\src\main\resources. A complete ICD10 version 
   is available from SAHSU for Organisations compliant with the WHO licence.
   
   For a full ICD10 listing add the following SAHSU supplied files to: 
   %CATALINE_HOME%\webapps\taxonomyServices\WEB-INF\classes and restart tomcat

   * icdClaML2016ens.xml
   * TaxonomyServicesConfiguration.xml

2) Build the Taxonomy Service using *maven*.
   Either: 
   - if you have *make* installed, in the top level github directory type *make taxonomyservice" as per Maven build instructions or
   - Change to the taxonomyServices directory. In local RIF tree, go to ...rapidInquiryFacility/taxonomyServices, 
   e.g. C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\taxonomyServices and type:

	```
	mvn –Dmaven.test.skip=TRUE install
	```

	Log from a succsful web service deployment:
	```
	12-Apr-2017 17:44:56.103 INFO [localhost-startStop-2] org.apache.catalina.startup.HostConfig.deployWAR Deploying web application archive C:\Program Files\Apache Software Foundation\Tomcat 8.5\webapps\taxonomyServices.war
	12-Apr-2017 17:44:57.886 INFO [localhost-startStop-2] org.apache.jasper.servlet.TldScanner.scanJars At least one JAR was scanned for TLDs yet contained no TLDs. Enable debug logging for this logger for a complete list of JARs that were scanned but no TLDs were found in them. Skipping unneeded JARs during scanning can improve startup time and JSP compilation time.
	12-Apr-2017 17:44:57.900 INFO [localhost-startStop-2] com.sun.jersey.server.impl.container.servlet.JerseyServletContainerInitializer
	.addServletWithApplication Registering the Jersey servlet application, named taxonomyServices.RIFTaxonomyWebServiceApplication, at the servlet mapping, /taxonomyServices/*, with the Application class of the same name
	12-Apr-2017 17:44:57.924 INFO [localhost-startStop-2] com.sun.jersey.api.core.servlet.WebAppResourceConfig.init Scanning for root re
	source and provider classes in the Web app resource paths:	
	  /WEB-INF/lib
	  /WEB-INF/classes
	12-Apr-2017 17:44:58.739 INFO [localhost-startStop-2] com.sun.jersey.api.core.ScanningResourceConfig.logClasses Root resource classes found:
	  class taxonomyServices.RIFTaxonomyWebServiceResource
	12-Apr-2017 17:44:58.740 INFO [localhost-startStop-2] com.sun.jersey.api.core.ScanningResourceConfig.logClasses Provider classes found:
	  class org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider
	  class org.codehaus.jackson.jaxrs.JacksonJsonProvider
	  class org.codehaus.jackson.jaxrs.JsonMappingExceptionMapper
	  class org.codehaus.jackson.jaxrs.JsonParseExceptionMapper
	12-Apr-2017 17:44:58.877 INFO [localhost-startStop-2] com.sun.jersey.server.impl.application.WebApplicationImpl._initiate Initiating Jersey application, version 'Jersey: 1.19 02/11/2015 03:25 AM'
	12-Apr-2017 17:45:00.002 INFO [localhost-startStop-2] org.apache.catalina.startup.HostConfig.deployWAR Deployment of web application archive C:\Program Files\Apache Software Foundation\Tomcat 8.5\webapps\taxonomyServices.war has finished in 3,899 ms
	```

3) Copy ‘taxonomyServices.war’ from the *target* directory into the Tomcat webapps folder as with rifServices. 

## 3.2 RIF Web Application

Create RIF4 in web-apps:

* Change directory to *%CATALINA_HOME%\webapps*; e,g, *cd "C:\Program Files\Apache Software Foundation\Tomcat 8.5\webapps"*
* Create the directory *RIF4*
* Copy all files and directories from the directory: *"C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifWebApplication\src\main\webapp\WEB-INF"* 
  to *C:\Program Files\Apache Software Foundation\Tomcat 8.5\webapps\RIF4*

If you are supplied with the *7zip* archive, RIF4.7z needs to be copied to: 
*%CATALINA_HOME%\webapps\RIF4* and unpacked using the file manager *7zip*. Do not use the command line 
(```"C:\Program Files\7-Zip\7z.exe" x RIF4.7z```) it does not work!

```
C:\Program Files\Apache Software Foundation\Tomcat 8.5\webapps\RIF4>"C:\Program Files\7-Zip\7z.exe" x RIF4.7z

7-Zip [64] 16.04 : Copyright (c) 1999-2016 Igor Pavlov : 2016-10-04

Scanning the drive for archives:
1 file, 2242469 bytes (2190 KiB)

Extracting archive: RIF4.7z
--
Path = RIF4.7z
Type = 7z
Physical Size = 2242469
Headers Size = 3912
Method = LZMA2:6m
Solid = +
Blocks = 1


Would you like to replace the existing file:
  Path:     .\backend\services\rifs-back-database.js
  Size:     2160 bytes (3 KiB)
  Modified: 2017-04-10 11:03:15
with the file from archive:
  Path:     backend\services\rifs-back-database.js
  Size:     2168 bytes (3 KiB)
  Modified: 2017-04-12 09:24:31
? (Y)es / (N)o / (A)lways / (S)kip all / A(u)to rename all / (Q)uit? A

Everything is Ok

Folders: 35
Files: 199
Size:       4920556
Compressed: 2242469
```
  
**BEFORE YOU RUN THE RIF YOU MUST SETUP THE DATABASE AND NETWORKING IN TOMCAT FIRST**

Running the RIF and logging on is detailed in section 5. You must restart Tomcat when you create RIF4 for the first time, 
it is not automatically spotted unlike the services *.war* files..

# 4 RIF Setup

## 4.1 Setup Database

The Java connector for theRifServices middleware is setup in the file: *%CATALINA_HOME%\webapps\rifServices\WEB-INF\classes\RIFServiceStartupProperties.properties*

* If the folder rifServices does not exist; start tomcat and it will be expanded from the war file.
* The database name (databaseName) is normally *sahsuland*
* The database host (database.host) is *localhost* on a standalone machine with no network access or the hostname. This does not notrmally need to be fully qualifed 
  (i.e. aepw-rif27.sm.med.ic.ac.uk is not required)
* The SQL Server host will be the same as the SQLCMDSERVER variable.

Do not set up the database not network access or open the firewall ports unless this is required; it is secure on *localhost*! The database 
can be remote but users must take care to ensure that it is setup securely. If you use a remote database, user are advised the secure the database:

* Always use TLS.
* Restrict access using **BOTH** the database software (*hba.conf* in Postgres) and the network infrastruture
* Keep the database fully patched as per vendor advice.
* Follow the appropriate guidelines, e.g. OWASP, but be consult SAHSU as some of the changes may break the RIF: 
  - [OWASP Postgres guidelines](https://www.owasp.org/index.php/OWASP_Backend_Security_Project_PostgreSQL_Hardening)
  - [OWASP SQL Server guidelines](https://www.owasp.org/index.php/OWASP_Backend_Security_Project_SQLServer_Hardening)

### 4.1.1 SQL Server

```java
#SQL SERVER
database.driverClassName=com.microsoft.sqlserver.jdbc.SQLServerDriver
database.jdbcDriverPrefix=jdbc:sqlserver
database.host=localhost\\SQLEXPRESS
database.port=1433
database.databaseName=sahsuland
database.databaseType=sqlServer
```

### 4.1.2 Postgres

```java
#POSTGRES
database.driverClassName=org.postgresql.Driver
database.jdbcDriverPrefix=jdbc:postgresql
database.host=aepw-rif27
database.port=5432
database.databaseName=sahsuland
database.databaseType=postgresql
```

**BEWARE** Make sure you keep a copy of this file; any front end RIF web application upgrade will overwrite it.

## 4.2 Setup Network

By default tomcat runs on port 8080, if you have installed the Apache webserver (Postgres install can) then it will appear on port 8081. This can be 
detected using the netstat command (the syntax will be slightly differ on Linux):

```
C:\Program Files\Apache Software Foundation\Tomcat 8.5\bin>netstat -ban | findstr 8080
  TCP    0.0.0.0:8080           0.0.0.0:0              LISTENING
  TCP    [::]:8080              [::]:0                 LISTENING
```

```
C:\Program Files\Apache Software Foundation\Tomcat 8.5\bin>netstat -ba
  Proto  Local Address          Foreign Address        State
  TCP    0.0.0.0:80             0.0.0.0:0              LISTENING
 [Skype.exe]
...

  TCP    [::]:8009              [::]:0                 LISTENING
 [Tomcat8.exe]
  TCP    [::]:8080              [::]:0                 LISTENING
 [Tomcat8.exe]
```

The RIF web application file RIF4\backend\services\rifs-back-urls.js (e.g. C:\Program Files\Apache Software Foundation\Tomcat 8.5\webapps\RIF4\backend\services\rifs-back-urls.js)
define the URLs for the services.

```javascript
/*
 * SERVICE for URL middleware calls. Localhost can be edited here
 */

angular.module("RIF")
        .constant('studySubmissionURL', "http://localhost:8080/rifServices/studySubmission/")
        .constant('studyResultRetrievalURL', "http://localhost:8080/rifServices/studyResultRetrieval/")
        .constant('taxonomyServicesURL', "http://localhost:8080/taxonomyServices/taxonomyServices/");
```

Edit these to match:

* The port number in use; e.g. 8080 as in the above example or 8443 if you are in a production environment with TLS enabled;
* The server for the remote service, e.g. *https://aepw-rif27.sm.med.ic.ac.uk*

**BEWARE** Make sure you keep a copy of this file; any front end RIF web application upgrade will overwrite it.

Running the RIF and logging on is detailed in section 5.

### 4.2.1 TLS

TLS or *Transport Layer Security* is required to secure the RIF in a networked environment. It is not required if you are just running locally as 
a developer or on a laptop.

To install and configure SSL/TLS support on Tomcat, you need to follow these simple steps. For more information, read the rest of this HOW-TO.

Create a keystore file to store the server's private key and self-signed certificate by executing the following command in the $CATALINA_BASE/conf directory:
Windows. Do **NOT** use a password of *changeit*:
```
cd C:\Program Files\Apache Software Foundation\Tomcat 8.5\conf

"%JAVA_HOME%\bin\keytool" -genkey -alias tomcat -keyalg RSA -keystore "C:\Program Files\Apache Software Foundation\Tomcat 8.5\conf\localhost-rsa.jks" -storepass changeit

Unix:
```

$JAVA_HOME/bin/keytool -genkey -alias tomcat -keyalg RSA
```
On a Unix system the keystore will be put in ~/.keystore and needs to be copied to $CATALINA_BASE/conf/localhost-rsa.jks
```

Example output:
```
C:\Program Files\Apache Software Foundation\Tomcat 8.5\bin>"%JAVA_HOME%\bin\keytool" -genkey -alias tomcat -keyalg RSA -keystore "C:\Program Files\Apache Software Foundation\Tomcat 8.5\conf\localhost-rsa.jks" -storepass changeit
What is your first and last name?
  [Unknown]:  Peter Hambly
What is the name of your organizational unit?
  [Unknown]:  SAHSU
What is the name of your organization?
  [Unknown]:  Imperial College
What is the name of your City or Locality?
  [Unknown]:  London
What is the name of your State or Province?
  [Unknown]:  England
What is the two-letter country code for this unit?
  [Unknown]:  UK
Is CN=Peter Hambly, OU=SAHSU, O=Imperial College, L=London, ST=England, C=UK correct?
  [no]:  yes

Enter key password for <tomcat>
        (RETURN if same as keystore password):

```
Check the keystore is in the correct place:
```
C:\Program Files\Apache Software Foundation\Tomcat 8.5\conf>dir localhost-rsa.jks
 Volume in drive C is OS
 Volume Serial Number is BEDC-5990

 Directory of C:\Program Files\Apache Software Foundation\Tomcat 8.5\conf

11/04/2017  13:45             2,255 localhost-rsa.jks
               1 File(s)          2,255 bytes
               0 Dir(s)  82,625,716,224 bytes free
```

Uncomment the "SSL HTTP/1.1 Connector" entry in $CATALINA_BASE/conf/server.xml and modify as described in the Configuration section 
in: https://tomcat.apache.org/tomcat-8.5-doc/ssl-howto.html. Change the port number from 8443 to 8080; remove the original 8080 connector.
Set the password correctly; as used above. Do **NOT** use a password of *changeit*.

```
<Connector port="8080" 
           protocol="org.apache.coyote.http11.Http11NioProtocol"
           server="Apache"
           maxThreads="150"
           scheme="https"
           keystoreFile="conf/localhost-rsa.jks"
           keystorePass="changeit"
           secure="true"
           clientAuth="false"
           sslProtocol="TLSv1.2"
           sslEnabledProtocols="TLSv1.2"
           ciphers="TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,
           		TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384,TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384,
           		TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
           		TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256,TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256,
           		TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384,TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384,
           		TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,
           		TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384,TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384,
           		TLS_ECDH_RSA_WITH_AES_256_CBC_SHA,TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA,
           		TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256,TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256,
           		TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
           		TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256,TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256,
           		TLS_ECDH_RSA_WITH_AES_128_CBC_SHA,TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA"
           SSLEnabled="true">
</Connector>	
```

This will generate a self signed certificate; this will cause browsers to complain:

  ![alt text](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifWebApplication/insecure_connection_warning.png?raw=true "Insecure TLS warning")

To sign the certificates, follow the instructions in: https://tomcat.apache.org/tomcat-8.5-doc/ssl-howto.html#SSL_and_Tomcat

This setup will support:

- Android 4.4.2 and later
- Firefox 32 and later. Note that Firefox 45 does not work!
- IE 11 and later
- Microsoft Edge 38 and later
- IE Mobile 11 and later
- Java 8 b132 (March 10, 2014)
- Safari 7 and later

## 4.3 Setup R
  
1. Create directories for extract (extractDirectory) and policies (extraDirectoryForExtractFiles). The defaults are:

   * Extract: ```extractDirectory=c:\\rifDemo\\scratchSpace```
   * Policies: ```extraDirectoryForExtractFiles=C:\\rifDemo\\generalDataExtractPolicies```

   Grant read, write and execute access to these directories for Tomcat and SQL Server
   
2. Create and test a system ODBC datasource for the database in use; the default is:

   * ODBC sytstem data source: ```odbcDataSourceName=PostgreSQL30```

   For SQL Server use SQL Server Native Client version 11, 2011 version or later; 
   For Postgres use the latest driver from https://www.postgresql.org/ftp/odbc/versions/msi/
   
   For Postgres, select the datasourcde tab and set *Max Varchar* and *Max Long Varchar* to 8190.
   
These settings are in the Java connector for the RifServices middleware: *%CATALINA_HOME%\webapps\rifServices\WEB-INF\classes\RIFServiceStartupProperties.properties*

**BEWARE** Make sure you keep a copy of this file; any front end RIF web application upgrade will overwrite it.

```java
webApplicationDirectory=rifServices
rScriptDirectory=rScripts
maximumMapAreasAllowedForSingleDisplay=200
extractDirectory=c:\\rifDemo\\scratchSpace
odbcDataSourceName=PostgreSQL30
#odbcDataSourceName=SQLServer11
extraDirectoryForExtractFiles=C:\\rifDemo\\generalDataExtractPolicies
```

3. Start *R* as Administrator and run the following script:

```R
# CHECK & AUTO INSTALL MISSING PACKAGES
packages <- c("pryr", "plyr", "abind", "maptools", "spdep", "RODBC", "MatrixModels", "rJava")
if (length(setdiff(packages, rownames(installed.packages()))) > 0) {
  install.packages(setdiff(packages, rownames(installed.packages())))  
}
if (!require(INLA)) {
	install.packages("INLA", repos="https://www.math.ntnu.no/inla/R/stable")
}

``` 

* R will ask for the nearest CRAN (R code archive); select one geographically near you (e.g. same country).
* R output:

```
--- Please select a CRAN mirror for use in this session ---
also installing the dependencies 'gtools', 'gdata', 'Rcpp', 'sp', 'LearnBayes', 'deldir', 'coda', 'gmodels', 'expm'


  There are binary versions available but the source versions are later:
       binary source needs_compilation
deldir 0.1-12 0.1-14              TRUE
spdep  0.6-12 0.6-13              TRUE

Do you want to install from sources the packages which need compilation?
y/n: if (!require(INLA)) {
trying URL 'https://cran.ma.imperial.ac.uk/bin/windows/contrib/3.2/gtools_3.5.0.zip'
Content type 'application/zip' length 144014 bytes (140 KB)
downloaded 140 KB

trying URL 'https://cran.ma.imperial.ac.uk/bin/windows/contrib/3.2/gdata_2.17.0.zip'
Content type 'application/zip' length 1178306 bytes (1.1 MB)
downloaded 1.1 MB

trying URL 'https://cran.ma.imperial.ac.uk/bin/windows/contrib/3.2/Rcpp_0.12.10.zip'
Content type 'application/zip' length 3261850 bytes (3.1 MB)
downloaded 3.1 MB

trying URL 'https://cran.ma.imperial.ac.uk/bin/windows/contrib/3.2/sp_1.2-4.zip'
Content type 'application/zip' length 1528674 bytes (1.5 MB)
downloaded 1.5 MB

trying URL 'https://cran.ma.imperial.ac.uk/bin/windows/contrib/3.2/LearnBayes_2.15.zip'
Content type 'application/zip' length 1129565 bytes (1.1 MB)
downloaded 1.1 MB

trying URL 'https://cran.ma.imperial.ac.uk/bin/windows/contrib/3.2/deldir_0.1-12.zip'
Content type 'application/zip' length 171603 bytes (167 KB)
downloaded 167 KB

trying URL 'https://cran.ma.imperial.ac.uk/bin/windows/contrib/3.2/coda_0.19-1.zip'
Content type 'application/zip' length 201300 bytes (196 KB)
downloaded 196 KB

trying URL 'https://cran.ma.imperial.ac.uk/bin/windows/contrib/3.2/gmodels_2.16.2.zip'
Content type 'application/zip' length 73931 bytes (72 KB)
downloaded 72 KB

trying URL 'https://cran.ma.imperial.ac.uk/bin/windows/contrib/3.2/expm_0.999-2.zip'
Content type 'application/zip' length 194188 bytes (189 KB)
downloaded 189 KB

trying URL 'https://cran.ma.imperial.ac.uk/bin/windows/contrib/3.2/plyr_1.8.4.zip'
Content type 'application/zip' length 1121290 bytes (1.1 MB)
downloaded 1.1 MB

trying URL 'https://cran.ma.imperial.ac.uk/bin/windows/contrib/3.2/abind_1.4-5.zip'
Content type 'application/zip' length 40002 bytes (39 KB)
downloaded 39 KB

trying URL 'https://cran.ma.imperial.ac.uk/bin/windows/contrib/3.2/maptools_0.9-2.zip'
Content type 'application/zip' length 1818632 bytes (1.7 MB)
downloaded 1.7 MB

trying URL 'https://cran.ma.imperial.ac.uk/bin/windows/contrib/3.2/spdep_0.6-12.zip'
Content type 'application/zip' length 3819364 bytes (3.6 MB)
downloaded 3.6 MB

trying URL 'https://cran.ma.imperial.ac.uk/bin/windows/contrib/3.2/RODBC_1.3-15.zip'
Content type 'application/zip' length 829585 bytes (810 KB)
downloaded 810 KB

package 'gtools' successfully unpacked and MD5 sums checked
package 'gdata' successfully unpacked and MD5 sums checked
package 'Rcpp' successfully unpacked and MD5 sums checked
package 'sp' successfully unpacked and MD5 sums checked
package 'LearnBayes' successfully unpacked and MD5 sums checked
package 'deldir' successfully unpacked and MD5 sums checked
package 'coda' successfully unpacked and MD5 sums checked
package 'gmodels' successfully unpacked and MD5 sums checked
package 'expm' successfully unpacked and MD5 sums checked
package 'plyr' successfully unpacked and MD5 sums checked
package 'abind' successfully unpacked and MD5 sums checked
package 'maptools' successfully unpacked and MD5 sums checked
package 'spdep' successfully unpacked and MD5 sums checked
package 'RODBC' successfully unpacked and MD5 sums checked

The downloaded binary packages are in
        C:\Users\admin\AppData\Local\Temp\RtmpSkeuRW\downloaded_packages
> install.packages("INLA", repos="https://www.math.ntnu.no/inla/R/stable")
Warning: dependency 'MatrixModels' is not available
trying URL 'https://www.math.ntnu.no/inla/R/stable/bin/windows/contrib/3.2/INLA_0.0-1485844051.zip'
Content type 'application/zip' length 93004915 bytes (88.7 MB)
downloaded 88.7 MB


The downloaded binary packages are in
        C:\Users\admin\AppData\Local\Temp\RtmpSkeuRW\downloaded_packages
```

4. Add R_HOME to the environment

5. Add the 64bit JRI native library location and the R_HOME bin\x64 directory to the path

   To use R from Tomcat Java you will need to install JRI. Fortunately, JRI is now a part of rJava and is installed with it.
   JRI will require its own native shared library which is already installed with rJava. To locate JRI installed with 
   rJava, use
	```
	> system.file("jri",package="rJava")
	[1] "C:/Program Files/R/R-3.4.0/library/rJava/jri"
	```
   from inside of R [command-line]. Above command will give you a path. You will be able to find the 64 bit *libjri.so*
   which is the shared library JRI is looking for.
  
   These directories with the 32 or 64 bit subdirectory appended needs to be added to the path: 
   *C:\Program Files\R\R-3.4.0\bin\x64;C:\Program Files\R\R-3.4.0\library\rJava\jri\x64*. This ensures that file "x64\jri.dll" 
   is in java.library.path
   
   Just after user logon the middleware can print the JAVA LIBRARY PATH: *System.getProperty("java.library.path")*

   As with Java and R, normally the 64 bit version is used.
   
	```
	JAVA LIBRARY PATH >>>
	C:\Program Files\Apache Software Foundation\Tomcat 8.5\bin;C:\Windows\Sun\Java\bin;C:\Windows\system32;C:\Windows;C:\ProgramData\Ora
	cle\Java\javapath;C:\Python27\;C:\Python27\Scripts;C:\Program Files (x86)\Intel\iCLS Client\;C:\Program Files\Intel\iCLS Client\;C:\
	Windows\system32;C:\Windows;C:\Windows\System32\Wbem;C:\Windows\System32\WindowsPowerShell\v1.0\;C:\Program Files\Intel\Intel(R) Man
	agement Engine Components\DAL;C:\Program Files (x86)\Intel\Intel(R) Management Engine Components\DAL;C:\Program Files\Intel\Intel(R)
	 Management Engine Components\IPT;C:\Program Files (x86)\Intel\Intel(R) Management Engine Components\IPT;C:\Program Files\Intel\WiFi
	\bin\;C:\Program Files\Common Files\Intel\WirelessCommon\;C:\Users\admin\.dnx\bin;C:\Program Files\Microsoft DNX\Dnvm\;C:\Program Fi
	les (x86)\Windows Kits\8.1\Windows Performance Toolkit\;C:\MinGW\msys\1.0\bin;C:\Program Files\PostgreSQL\9.5\bin;C:\Program Files\R
	\R-3.2.3\bin\x64;C:\Program Files\Microsoft SQL Server\Client SDK\ODBC\110\Tools\Binn\;C:\Program Files (x86)\Microsoft SQL Server\1
	20\Tools\Binn\;C:\Program Files\Microsoft SQL Server\120\Tools\Binn\;C:\Program Files\Microsoft SQL Server\120\DTS\Binn\;C:\Program
	Files (x86)\Microsoft SQL Server\120\Tools\Binn\ManagementStudio\;C:\Program Files (x86)\Microsoft SQL Server\120\DTS\Binn\;C:\Progr
	am Files\nodejs\;C:\Program Files\Apache Software Foundation\apache-maven-3.3.9\bin;C:\Program Files\R\R-3.4.0\bin;C:\Program Files
	(x86)\Skype\Phone\;C:/Program Files/R/R-3.4.0/library/rJava/jri;.
	```
	
6. JRI Errors
	
	``` 
	=======getInvestigationID========2===
	About to call next
	called next
	Investigation name==TEST 1001  ID==12==
	Cannot find JRI native library!
	Please make sure that the JRI native library is in a directory listed in java.library.path.

	java.lang.UnsatisfiedLinkError: C:\Program Files\R\R-3.4.0\library\rJava\jri\x64\jri.dll: Can't find dependent libraries
	```
   In this case, the directory *C:\Program Files\R\R-3.4.0\bin\x64* was missing from the path.
   
### 4.3.1 R Debugging

Since R now uses JRI, all errors appear in the tomcat logs.

1. Beware of 32 bit Java errors

```
=======getInvestigationID========2===
About to call next
called next
Investigation name==TEST 1001  ID==5==
Cannot find JRI native library!
Please make sure that the JRI native library is in a directory listed in java.library.path.

java.lang.UnsatisfiedLinkError: C:\Program Files\R\R-3.4.1\library\rJava\jri\x64\jri.dll: Can't load AMD 64-bit .dll on a IA 32-bit platform
        at java.lang.ClassLoader$NativeLibrary.load(Native Method)
        at java.lang.ClassLoader.loadLibrary0(Unknown Source)
        at java.lang.ClassLoader.loadLibrary(Unknown Source)
        at java.lang.Runtime.loadLibrary0(Unknown Source)
        at java.lang.System.loadLibrary(Unknown Source)
        at org.rosuda.JRI.Rengine.<clinit>(Rengine.java:19)
        at rifServices.dataStorageLayer.pg.PGSQLSmoothResultsSubmissionStep.performStep(PGSQLSmoothResultsSubmissionStep.java:193)
```

2. Typical errors for the JRI Middleware:

   * *R_HOME* not setup: ```command==null\bin\x64\RScript```
   * No scratchSpace directory: *c:\rifDemo\scratchSpace\* ```(The system cannot find the path specified)```
   * R script errors
   
   If there are R script errors JRI the middleware will crash with an error. This will be saved in the *Study status* pane:
   
   ![alt text](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifWebApplication/study-status.png?raw=true "Study status")  

   Clicking on the *trace* button will bring up the trace pane. 
  
   ![alt text](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifWebApplication/R-trace.png?raw=true "R Trace")  
   
   If there are R script errors JRI will log them to the middleware log: ```%CATALINA_HOME%/log4j2/<YYYY>-<MM>/RIF_middleware.log-<N>```:
   
```
17:06:02.767 [http-nio-8080-exec-7] INFO  rifGenericLibrary.util.RIFLogger : [rifServices.dataStorageLayer.ms.MSSQLSmoothResultsSubmissionStep]:
Rengine Started
17:06:02.787 [http-nio-8080-exec-7] INFO  rifGenericLibrary.util.RIFLogger : [rifServices.system.RIFServiceStartupOptions]:
RIFServiceStartupOptions is web deployment
17:06:02.787 [http-nio-8080-exec-7] INFO  rifGenericLibrary.util.RIFLogger : [rifServices.system.RIFServiceStartupOptions]:
Get CATALINA_HOME=C:\Program Files\Apache Software Foundation\Tomcat 8.5
17:06:02.788 [http-nio-8080-exec-7] INFO  rifGenericLibrary.util.RIFLogger : [rifServices.dataStorageLayer.ms.MSSQLSmoothResultsSubmissionStep]:
Source: Adj_Cov_Smooth_JRI="C:\\Program Files\\Apache Software Foundation\\Tomcat 8.5\\webapps\\rifServices\\WEB-INF\\classes\\Adj_Cov_Smooth_JRI.R"
17:06:04.418 [http-nio-8080-exec-7] INFO  rifGenericLibrary.util.RIFLogger : [rifServices.dataStorageLayer.ms.MSSQLAbstractRService$LoggingConsole]:
rFlushConsole[1] calls: 62, length: 1458, time period: PT1.892S

R version 3.4.0 (2017-04-21) -- "You Stupid Darkness"
Copyright (C) 2017 The R Foundation for Statistical Computing
Platform: x86_64-w64-mingw32/x64 (64-bit)

R is free software and comes with ABSOLUTELY NO WARRANTY.
You are welcome to redistribute it under certain conditions.
Type 'license()' or 'licence()' for distribution details.

R is a collaborative project with many contributors.
Type 'contributors()' for more information and
'citation()' on how to cite R or R packages in publications.

Type 'demo()' for some demos, 'help()' for on-line help, or
'help.start()' for an HTML browser interface to help.
Type 'q()' to quit R.

[1] "C:/Program Files/R/R-3.4.0/library"
R version 3.4.0 (2017-04-21)
Platform: x86_64-w64-mingw32/x64 (64-bit)
Running under: Windows 8.1 x64 (build 9600)

Matrix products: default

locale:
[1] LC_COLLATE=English_United Kingdom.1252 
[2] LC_CTYPE=English_United Kingdom.1252   
[3] LC_MONETARY=English_United Kingdom.1252
[4] LC_NUMERIC=C                           
[5] LC_TIME=English_United Kingdom.1252    

attached base packages:
[1] stats     graphics  grDevices utils     datasets  methods   base     

loaded via a namespace (and not attached):
[1] compiler_3.4.0
R Error/Warning/Notice: Loading required package: sp
R Error/Warning/Notice: Loading required package: Matrix
R Error/Warning/Notice: This is INLA 0.0-1485844051, dated 2017-01-31 (09:14:12+0300).
See www.r-inla.org/contact-us for how to get help.

17:06:04.980 [http-nio-8080-exec-7] INFO  rifGenericLibrary.util.RIFLogger : [rifServices.dataStorageLayer.ms.MSSQLSmoothResultsSubmissionStep]:
Source: RIF_odbc="C:\\Program Files\\Apache Software Foundation\\Tomcat 8.5\\webapps\\rifServices\\WEB-INF\\classes\\RIF_odbc.R"
17:06:04.996 [http-nio-8080-exec-7] INFO  rifGenericLibrary.util.RIFLogger : [rifServices.dataStorageLayer.ms.MSSQLSmoothResultsSubmissionStep]:
Source: performSmoothingActivity="C:\\Program Files\\Apache Software Foundation\\Tomcat 8.5\\webapps\\rifServices\\WEB-INF\\classes\\performSmoothingActivity.R"
17:06:24.801 [http-nio-8080-exec-7] INFO  rifGenericLibrary.util.RIFLogger : [rifServices.dataStorageLayer.ms.MSSQLAbstractRService$LoggingConsole]:
rFlushConsole[2] calls: 8, length: 957, time period: PT20.382S

R Error/Warning/Notice: Checking rgeos availability: FALSE
 	Note: when rgeos is not available, polygon geometry 	computations in maptools depend on gpclib,
 	which has a restricted licence. It is disabled by default;
 	to enable gpclib, type gpclibPermit()
Copy:  C:\Program Files\Apache Software Foundation\Tomcat 8.5\webapps\rifServices\WEB-INF\classes\performSmoothingActivity.R  to:  c:\rifDemo\scratchSpace\s33\performSmoothingActivity.R 
Copy:  C:\Program Files\Apache Software Foundation\Tomcat 8.5\webapps\rifServices\WEB-INF\classes\Adj_Cov_Smooth_csv.R  to:  c:\rifDemo\scratchSpace\s33\Adj_Cov_Smooth_csv.R 
Copy:  C:\Program Files\Apache Software Foundation\Tomcat 8.5\webapps\rifServices\WEB-INF\classes\rif40_run_R.bat  to:  c:\rifDemo\scratchSpace\s33\rif40_run_R.bat 
Create:  c:\rifDemo\scratchSpace\s33\rif40_run_R_env.bat 
Connect to database: SQLServer11
Performing basic stats and smoothing
EXTRACT TABLE NAME: rif_studies.s33_extract
17:06:24.816 [http-nio-8080-exec-7] INFO  rifGenericLibrary.util.RIFLogger : [rifServices.dataStorageLayer.ms.MSSQLAbstractRService$LoggingConsole]:
rFlushConsole[3] calls: 75, length: 2422, time period: PT59.244S


Saving extract frame to: c:\rifDemo\scratchSpace\s33\tmp_s33_extract.csv
rif_studies.s33_extract numberOfRows=433312==
rif40_GetAdjacencyMatrix numberOfRows=1229==
Saving adjacency matrix to: c:\rifDemo\scratchSpace\s33\tmp_s33_adjacency_matrix.csv
Covariates: none
Bayes smoothing with BYM model type no adjustment
Stack tracer >>>

 .handleSimpleError(function (obj) 
{
    calls = sys.calls()
    calls = ca <text>#1: INLA::f(area_order, model = "bym", graph = IM, adjust.for.con.com eval(parse(text = gsub("^f\\(", "INLA::f(", terms[i])), envir = data, enclo eval(parse(text = gsub("^f\\(", "INLA::f(", terms[i])), envir = data, enclo inla.interpret.formula(formula, data.same.len = data.same.len, data = data, performSmoothingActivity.R#609: inla(formula, family = "poisson", E = EXP_U performSmoothingActivity(data, AdjRowset) Adj_Cov_Smooth_JRI.R#360: withVisible(expr) Adj_Cov_Smooth_JRI.R#360: withCallingHandlers(withVisible(expr), error = er withErrorTracing({
    data = fetchExtractTable()
    AdjRowset = getAdjace doTryCatch(return(expr), name, parentenv, handler) tryCatchOne(expr, names, parentenv, handlers[[1]]) tryCatchList(expr, names[-nh], parentenv, handlers[-nh]) doTryCatch(return(expr), name, parentenv, handler) tryCatchOne(tryCatchList(expr, names[-nh], parentenv, handlers[-nh]), names tryCatchList(expr, classes, parentenv, handlers) tryCatch({
    withErrorTracing({
        data = fetchExtractTable()
       eval(expr, pf) eval(expr, pf) withVisible(eval(expr, pf)) evalVis(expr) Adj_Cov_Smooth_JRI.R#381: capture.output({
    tryCatch({
        withError runRSmoothingFunctions() 
<<< End of stack tracer.
callPerformSmoothingActivity() ERROR:  argument is of length zero ; call stack:  if 
callPerformSmoothingActivity() ERROR:  argument is of length zero ; call stack:  scale.model 
callPerformSmoothingActivity() ERROR:  argument is of length zero ; call stack:  {
    if (constr) 
        rankdef = rankdef + 1
    if (!empty.extraconstr(extraconstr)) 
        rankdef = rankdef + dim(extraconstr$A)[1]
} 
callPerformSmoothingActivity() ERROR:  argument is of length zero ; call stack:  {
    if (constr) 
        rankdef = rankdef + 1
    rankdef = rankdef + cc.n1
    if (!empty.extraconstr(extraconstr)) 
        rankdef = rankdef + dim(extraconstr$A)[1]
} 
callPerformSmoothingActivity exitValue: 1
Closing database connection
Adj_Cov_Smooth_JRI.R exitValue: 1; error tracer: 36

17:06:24.817 [http-nio-8080-exec-7] INFO  rifGenericLibrary.util.RIFLogger : [rifServices.dataStorageLayer.ms.MSSQLSmoothResultsSubmissionStep]:
Rengine Stopped, exit value==1==
```

3. Tracing R script errors

   You will almost certainly get no results in the data viewer; the extract R processing can be re-run manually using a batch 
   file (*rif40_run_R.bat*) created in the R script scratch area: ```extractDirectory=c:\\rifDemo\\scratchSpace```:

   e.g. *C:\rifDemo\scratchSpace\s33*

   This contains the following files:

   * *Adj_Cov_Smooth_csv.R* - CSV version of R script (the middleware uses Adj_Cov_Smooth_JRI.R)
   * *performSmoothingActivity.R* - Smoothing module
   * *rif40_run_R.bat* - Script to run R to redo smoothing
   * *rif40_run_R_env.bat* - Study settings in use. Does *NOT* include the password!
   * *tmp_s33_adjacency_matrix.csv* - adjacency matrix for extract
   * *tmp_s33_extract.csv* - study data extract

   **This will be added to the extract during October 2017.**
   
```bat
C:\rifDemo\scratchSpace\s33>rif40_run_R.bat
New user [default peter]:
##########################################################################################
#
# Run R script on a study extract.
#
# USERID=peter
# DBNAME=sahsuland
# DBHOST=localhost\SQLEXPRESS
# DBPORT=1433
# DB_DRIVER_PREFIX=jdbc:sqlserver
# DB_DRIVER_CLASS_NAME=com.microsoft.sqlserver.jdbc.SQLServerDriver
# STUDYID=33
# INVESTIGATIONNAME=HELLO
# INVESTIGATIONID=33
# ODBCDATASOURCE=SQLServer11
# MODEL=BYM
# COVARIATENAME=none
#
##########################################################################################
"C:\Program Files\R\R-3.4.0\bin\x64\RScript" Adj_Cov_Smooth_csv.R ^
--db_driver_prefix=jdbc:sqlserver --db_driver_class_name=com.microsoft.sqlserver.jdbc.SQLServerDriver --odbcDataSource=SQLServer11 ^

--dbHost=localhost\SQLEXPRESS --dbPort=1433 --dbName=sahsuland ^
--studyID=33 --investigationName=HELLO --investigationId=33 ^
--model=BYM --covariateName=none ^
--userID=peter --password=XXXXXXXXXXXXXXXXXXXXXX ^
--scratchspace=c:\rifDemo\scratchSpace\ --dumpframestocsv=FALSE
Loading required package: sp
Loading required package: methods
Loading required package: Matrix
This is INLA 0.0-1485844051, dated 2017-01-31 (09:14:12+0300).
See www.r-inla.org/contact-us for how to get help.
Checking rgeos availability: FALSE
        Note: when rgeos is not available, polygon geometry     computations in maptools depend on gpclib,
        which has a restricted licence. It is disabled by default;
        to enable gpclib, type gpclibPermit()
CATALINA_HOME=C:\Program Files\Apache Software Foundation\Tomcat 8.5
Source: C:\Program Files\Apache Software Foundation\Tomcat 8.5\webapps\rifServices\WEB-INF\classes\performSmoothingActivity.R
13  arguments were supplied
Parsing parameters
                   name                                        value
1      db_driver_prefix                               jdbc:sqlserver
2  db_driver_class_name com.microsoft.sqlserver.jdbc.SQLServerDriver
3        odbcDataSource                                  SQLServer11
4                dbHost                        localhost\\SQLEXPRESS
5                dbPort                                         1433
6                dbName                                    sahsuland
7               studyID                                           33
8     investigationName                                        HELLO
9       investigationId                                           33
10                model                                          BYM
11        covariateName                                         none
12         scratchspace                  c:\\rifDemo\\scratchSpace\\
13      dumpframestocsv                                        FALSE
Performing basic stats and smoothing
Covariates: NONE
Bayes smoothing with BYM model type no adjustment
Stack tracer >>>

 .handleSimpleError(function (obj)
{
    calls = sys.calls()
    calls = ca INLA::f(area_order, model = "bym", graph = IM, adjust.for.con.comp = FALSE, eval(parse(text = gsub("^f\\(", "INLA::f(
", terms[i])), envir = data, enclo eval(parse(text = gsub("^f\\(", "INLA::f(", terms[i])), envir = data, enclo inla.interpret.formul
a(formula, data.same.len = data.same.len, data = data, inla(formula, family = "poisson", E = EXP_UNADJ, data = data[whichrows, ],  p
erformSmoothingActivity(data, AdjRowset) withVisible(expr) withCallingHandlers(withVisible(expr), error = errorTracer) withErrorTrac
ing({
    data = read.table(temporaryExtractFileName, header = doTryCatch(return(expr), name, parentenv, handler) tryCatchOne(expr, names,
 parentenv, handlers[[1]]) tryCatchList(expr, names[-nh], parentenv, handlers[-nh]) doTryCatch(return(expr), name, parentenv, handle
r) tryCatchOne(tryCatchList(expr, names[-nh], parentenv, handlers[-nh]), names tryCatchList(expr, classes, parentenv, handlers) tryC
atch({
    withErrorTracing({
        data = read.table(temporaryExtrac eval(expr, pf) eval(expr, pf) withVisible(eval(expr, pf)) evalVis(expr) capture.output({
    tryCatch({
        withErrorTracing({
            data runRSmoothingFunctions()
<<< End of stack tracer.
callPerformSmoothingActivity() ERROR:  argument is of length zero ; call stack:  if
callPerformSmoothingActivity() ERROR:  argument is of length zero ; call stack:  scale.model
callPerformSmoothingActivity() ERROR:  argument is of length zero ; call stack:  {
    if (constr)
        rankdef = rankdef + 1
    if (!empty.extraconstr(extraconstr))
        rankdef = rankdef + dim(extraconstr$A)[1]
}
callPerformSmoothingActivity() ERROR:  argument is of length zero ; call stack:  {
    if (constr)
        rankdef = rankdef + 1
    rankdef = rankdef + cc.n1
    if (!empty.extraconstr(extraconstr))
        rankdef = rankdef + dim(extraconstr$A)[1]
}
callPerformSmoothingActivity exitValue: 1
Adj_Cov_Smooth_JRI.R exitValue: 1; error tracer: 30
R script had error >>>
Covariates: NONE
Bayes smoothing with BYM model type no adjustment
Stack tracer >>>

 .handleSimpleError(function (obj)
{
    calls = sys.calls()
    calls = ca INLA::f(area_order, model = "bym", graph = IM, adjust.for.con.comp = FALSE, eval(parse(text = gsub("^f\\(", "INLA::f(
", terms[i])), envir = data, enclo eval(parse(text = gsub("^f\\(", "INLA::f(", terms[i])), envir = data, enclo inla.interpret.formul
a(formula, data.same.len = data.same.len, data = data, inla(formula, family = "poisson", E = EXP_UNADJ, data = data[whichrows, ],  p
erformSmoothingActivity(data, AdjRowset) withVisible(expr) withCallingHandlers(withVisible(expr), error = errorTracer) withErrorTrac
ing({
    data = read.table(temporaryExtractFileName, header = doTryCatch(return(expr), name, parentenv, handler) tryCatchOne(expr, names,
 parentenv, handlers[[1]]) tryCatchList(expr, names[-nh], parentenv, handlers[-nh]) doTryCatch(return(expr), name, parentenv, handle
r) tryCatchOne(tryCatchList(expr, names[-nh], parentenv, handlers[-nh]), names tryCatchList(expr, classes, parentenv, handlers) tryC
atch({
    withErrorTracing({
        data = read.table(temporaryExtrac eval(expr, pf) eval(expr, pf) withVisible(eval(expr, pf)) evalVis(expr) capture.output({
    tryCatch({
        withErrorTracing({
            data runRSmoothingFunctions()
<<< End of stack tracer.
callPerformSmoothingActivity() ERROR:  argument is of length zero ; call stack:  if
callPerformSmoothingActivity() ERROR:  argument is of length zero ; call stack:  scale.model
callPerformSmoothingActivity() ERROR:  argument is of length zero ; call stack:  {
    if (constr)
        rankdef = rankdef + 1
    if (!empty.extraconstr(extraconstr))
        rankdef = rankdef + dim(extraconstr$A)[1]
}
callPerformSmoothingActivity() ERROR:  argument is of length zero ; call stack:  {
    if (constr)
        rankdef = rankdef + 1
    rankdef = rankdef + cc.n1
    if (!empty.extraconstr(extraconstr))
        rankdef = rankdef + dim(extraconstr$A)[1]
}
callPerformSmoothingActivity exitValue: 1

<<< End of error trace.
Test study failed: Adj_Cov_Smooth_csv.R procedure had error for study: 33; investigation: 33
```

4. A JRI sucessful run:

A typical JRI sucsssful run looks like:

```
16:08:27.201 [http-nio-8080-exec-9] INFO  rifGenericLibrary.util.RIFLogger : [rifServices.dataStorageLayer.ms.MSSQLSmoothResultsSubmissionStep]:
Source: Adj_Cov_Smooth_JRI="C:\\Program Files\\Apache Software Foundation\\Tomcat 8.5\\webapps\\rifServices\\WEB-INF\\classes\\Adj_Cov_Smooth_JRI.R"
16:08:27.938 [http-nio-8080-exec-9] INFO  rifGenericLibrary.util.RIFLogger : [rifServices.dataStorageLayer.ms.MSSQLAbstractRService$LoggingConsole]:
rFlushConsole[1] calls: 61, length: 1320, time period: PT1.155S

R version 3.4.0 (2017-04-21) -- "You Stupid Darkness"
Copyright (C) 2017 The R Foundation for Statistical Computing
Platform: x86_64-w64-mingw32/x64 (64-bit)

R is free software and comes with ABSOLUTELY NO WARRANTY.
You are welcome to redistribute it under certain conditions.
Type 'license()' or 'licence()' for distribution details.

R is a collaborative project with many contributors.
Type 'contributors()' for more information and
'citation()' on how to cite R or R packages in publications.

Type 'demo()' for some demos, 'help()' for on-line help, or
'help.start()' for an HTML browser interface to help.
Type 'q()' to quit R.

[1] "C:/Program Files/R/R-3.4.0/library"
R version 3.4.0 (2017-04-21)
Platform: x86_64-w64-mingw32/x64 (64-bit)
Running under: Windows 8.1 x64 (build 9600)

Matrix products: default

locale:
[1] LC_COLLATE=English_United Kingdom.1252 
[2] LC_CTYPE=English_United Kingdom.1252   
[3] LC_MONETARY=English_United Kingdom.1252
[4] LC_NUMERIC=C                           
[5] LC_TIME=English_United Kingdom.1252    

attached base packages:
[1] stats     graphics  grDevices utils     datasets  methods   base     

loaded via a namespace (and not attached):
[1] compiler_3.4.0
R Error/Warning/Notice: Loading required package: sp
R Error/Warning/Notice: Loading required package: Matrix

16:08:28.980 [http-nio-8080-exec-9] INFO  rifGenericLibrary.util.RIFLogger : [rifServices.dataStorageLayer.ms.MSSQLAbstractRService$LoggingConsole]:
rFlushConsole[2] calls: 1, length: 139, time period: PT1.042S

R Error/Warning/Notice: This is INLA 0.0-1485844051, dated 2017-01-31 (09:14:12+0300).
See www.r-inla.org/contact-us for how to get help.

16:08:29.541 [http-nio-8080-exec-9] INFO  rifGenericLibrary.util.RIFLogger : [rifServices.dataStorageLayer.ms.MSSQLSmoothResultsSubmissionStep]:
Source: RIF_odbc="C:\\Program Files\\Apache Software Foundation\\Tomcat 8.5\\webapps\\rifServices\\WEB-INF\\classes\\RIF_odbc.R"
16:08:29.553 [http-nio-8080-exec-9] INFO  rifGenericLibrary.util.RIFLogger : [rifServices.dataStorageLayer.ms.MSSQLSmoothResultsSubmissionStep]:
Source: performSmoothingActivity="C:\\Program Files\\Apache Software Foundation\\Tomcat 8.5\\webapps\\rifServices\\WEB-INF\\classes\\performSmoothingActivity.R"
16:09:03.282 [http-nio-8080-exec-9] INFO  rifGenericLibrary.util.RIFLogger : [rifServices.dataStorageLayer.ms.MSSQLAbstractRService$LoggingConsole]:
rFlushConsole[3] calls: 10, length: 1093, time period: PT34.302S

R Error/Warning/Notice: 
Attaching package: 'INLA'

R Error/Warning/Notice: The following object is masked from 'package:pryr':

    f

R Error/Warning/Notice: Checking rgeos availability: FALSE
 	Note: when rgeos is not available, polygon geometry 	computations in maptools depend on gpclib,
 	which has a restricted licence. It is disabled by default;
 	to enable gpclib, type gpclibPermit()
Copy:  C:\Program Files\Apache Software Foundation\Tomcat 8.5\webapps\rifServices\WEB-INF\classes\performSmoothingActivity.R  to:  c:\rifDemo\scratchSpace\s49\performSmoothingActivity.R 
Copy:  C:\Program Files\Apache Software Foundation\Tomcat 8.5\webapps\rifServices\WEB-INF\classes\Adj_Cov_Smooth_csv.R  to:  c:\rifDemo\scratchSpace\s49\Adj_Cov_Smooth_csv.R 
Copy:  C:\Program Files\Apache Software Foundation\Tomcat 8.5\webapps\rifServices\WEB-INF\classes\rif40_run_R.bat  to:  c:\rifDemo\scratchSpace\s49\rif40_run_R.bat 
Create:  c:\rifDemo\scratchSpace\s49\rif40_run_R_env.bat 
Connect to database: SQLServer11
Performing basic stats and smoothing
EXTRACT TABLE NAME: rif_studies.s49_extract
16:09:03.728 [http-nio-8080-exec-9] INFO  rifGenericLibrary.util.RIFLogger : [rifServices.dataStorageLayer.ms.MSSQLAbstractRService$LoggingConsole]:
rFlushConsole[4] calls: 108, length: 1830, time period: PT1M16.182S


Saving extract frame to: c:\rifDemo\scratchSpace\s49\tmp_s49_extract.csv
rif_studies.s49_extract numberOfRows=433312==
rif40_GetAdjacencyMatrix numberOfRows=1229==
Saving adjacency matrix to: c:\rifDemo\scratchSpace\s49\tmp_s49_adjacency_matrix.csv
Covariates: none
Bayes smoothing with HET model type no adjustment
Posterior probability calculated
callPerformSmoothingActivity exitValue: 0
performSmoothingActivity() OK:  0 
check.integer: 01.001.000100.1; as.numeric(str): 1; isNumeric: TRUE; isInteger: TRUE; isNotRounded: TRUE; isIntRegexp: FALSE; check.integer.Result: FALSE
typeof(result$area_id[1]) ---->  integer ; check.integer(result$area_id[1]):  FALSE ; result$area_id[1]:  01.001.000100.1 
check.integer: 01.001.000100.1; as.numeric(str): 1; isNumeric: TRUE; isInteger: TRUE; isNotRounded: TRUE; isIntRegexp: FALSE; check.integer.Result: FALSE
Saving data frame to: c:\rifDemo\scratchSpace\s49\tmp_s49_map.csv
Creating temporary table: peter.tmp_s49_map
Creating study_id index on temporary table
Creating area_id index on temporary table
Creating genders index on temporary table
Created indices on temporary table
Updated map table: rif_studies.s49_map
.Primitive("return")
Dropping temporary table: peter.tmp_s49_map
Closing database connection
Total memory is use: 162554880
Memory by object:
AdjRowset: 304232
area_id_is_integer: 48
connDB: 3120
data: 17413544
errorTrace: 2384
ototal: 48
result: 746696
R Error/Warning/Notice: Garbage collection 165 = 120R Error/Warning/Notice: +21R Error/Warning/Notice: +24R Error/Warning/Notice:  (level 2) ... R Error/Warning/Notice: 
104.3 Mbytes of cons cells used (50%)
R Error/Warning/Notice: 33.4 Mbytes of vectors used (26%)
Free 18470072 memory; total memory is use: 144247440
Memory by object:
errorTrace: 2384
Adj_Cov_Smooth_JRI.R exitValue: 0; error tracer: 20
```

### 4.3.2 R Memory Management

R is run as a attached DLL from the first midleware worker thread that runs a study. The per thread memory usage is printed at the end 
of each smoothing operation so that thread memory leakage can be detected:

```
16:09:03.744 [http-nio-8080-exec-9] INFO  rifGenericLibrary.util.RIFLogger : [rifGenericLibrary.util.RIFMemoryManager]:
Thread list, 44 threads >>>
Thread: Thread-5; ID : 56; state: TIMED_WAITING; memory: 24.1 KB
Thread: Log4j2-TF-5-Scheduled-1; ID : 54; state: TIMED_WAITING; memory: 304 B
Thread: Log4j2-TF-5-Scheduled-1; ID : 52; state: TIMED_WAITING; memory: 416 B
Thread: ajp-nio-8009-AsyncTimeout; ID : 49; state: TIMED_WAITING; memory: 5.6 KB
Thread: ajp-nio-8009-Acceptor-0; ID : 48; state: RUNNABLE; memory: 80 B
Thread: ajp-nio-8009-ClientPoller-1; ID : 47; state: RUNNABLE; memory: 9.6 KB
Thread: ajp-nio-8009-ClientPoller-0; ID : 46; state: RUNNABLE; memory: 9.6 KB
Thread: ajp-nio-8009-exec-10; ID : 45; state: WAITING; memory: 32 B
Thread: ajp-nio-8009-exec-9; ID : 44; state: WAITING; memory: 32 B
Thread: ajp-nio-8009-exec-8; ID : 43; state: WAITING; memory: 32 B
Thread: ajp-nio-8009-exec-7; ID : 42; state: WAITING; memory: 32 B
Thread: ajp-nio-8009-exec-6; ID : 41; state: WAITING; memory: 32 B
Thread: ajp-nio-8009-exec-5; ID : 40; state: WAITING; memory: 32 B
Thread: ajp-nio-8009-exec-4; ID : 39; state: WAITING; memory: 32 B
Thread: ajp-nio-8009-exec-3; ID : 38; state: WAITING; memory: 32 B
Thread: ajp-nio-8009-exec-2; ID : 37; state: WAITING; memory: 32 B
Thread: ajp-nio-8009-exec-1; ID : 36; state: WAITING; memory: 32 B
Thread: http-nio-8080-AsyncTimeout; ID : 35; state: TIMED_WAITING; memory: 5.6 KB
Thread: http-nio-8080-Acceptor-0; ID : 34; state: RUNNABLE; memory: 300.7 KB
Thread: http-nio-8080-ClientPoller-1; ID : 33; state: RUNNABLE; memory: 29.1 KB
Thread: http-nio-8080-ClientPoller-0; ID : 32; state: RUNNABLE; memory: 25.9 KB
Thread: http-nio-8080-exec-10; ID : 31; state: WAITING; memory: 6.8 MB
CURRENT thread: http-nio-8080-exec-9; ID : 30; state: RUNNABLE; memory: 34.6 MB
Thread: http-nio-8080-exec-8; ID : 29; state: WAITING; memory: 112.1 MB
Thread: http-nio-8080-exec-7; ID : 28; state: WAITING; memory: 41.0 MB
Thread: http-nio-8080-exec-6; ID : 27; state: WAITING; memory: 187.9 MB
Thread: http-nio-8080-exec-5; ID : 26; state: WAITING; memory: 9.6 MB
Thread: http-nio-8080-exec-4; ID : 25; state: WAITING; memory: 38.6 MB
Thread: http-nio-8080-exec-3; ID : 24; state: WAITING; memory: 7.2 MB
Thread: http-nio-8080-exec-2; ID : 23; state: WAITING; memory: 11.5 MB
Thread: http-nio-8080-exec-1; ID : 22; state: WAITING; memory: 39.2 MB
Thread: ContainerBackgroundProcessor[StandardEngine[Catalina]]; ID : 21; state: TIMED_WAITING; memory: 618.6 KB
Thread: NioBlockingSelector.BlockPoller-2; ID : 18; state: RUNNABLE; memory: 3.5 KB
Thread: NioBlockingSelector.BlockPoller-1; ID : 17; state: RUNNABLE; memory: 24.2 KB
Thread: GC Daemon; ID : 16; state: TIMED_WAITING; memory: 0 B
Thread: RMI TCP Accept-0; ID : 15; state: RUNNABLE; memory: 616 B
Thread: RMI TCP Accept-9999; ID : 14; state: RUNNABLE; memory: 616 B
Thread: RMI TCP Accept-0; ID : 13; state: RUNNABLE; memory: 1.2 KB
Thread: Log4j2-TF-5-Scheduled-1; ID : 12; state: TIMED_WAITING; memory: 928 B
Thread: Attach Listener; ID : 5; state: RUNNABLE; memory: 0 B
Thread: Signal Dispatcher; ID : 4; state: RUNNABLE; memory: 0 B
Thread: Finalizer; ID : 3; state: WAITING; memory: 1.9 KB
Thread: Reference Handler; ID : 2; state: WAITING; memory: 0 B
Thread: main; ID : 1; state: RUNNABLE; memory: 61.0 MB
<<<
Total thread memory: 550.5 MB
Memory: max: 3.5 GB, total: 601.5 MB, free: 495.5 MB, available: 2.9 GB; processors: 4
```

This does *NOT* trace the memory used by *R* or by the *inla* executable that performs the Bayesian smoothing. R prints the 
memory is use just before the script exits; then releases the memory. *R* itself does *NOT* exit but stays running ready for the next 
study. Only one study can be smoothed at a time with JRI.

```
Total memory is use: 162554880
Memory by object:
AdjRowset: 304232
area_id_is_integer: 48
connDB: 3120
data: 17413544
errorTrace: 2384
ototal: 48
result: 746696
R Error/Warning/Notice: Garbage collection 165 = 120R Error/Warning/Notice: +21R Error/Warning/Notice: +24R Error/Warning/Notice:  (level 2) ... R Error/Warning/Notice: 
104.3 Mbytes of cons cells used (50%)
R Error/Warning/Notice: 33.4 Mbytes of vectors used (26%)
Free 18470072 memory; total memory is use: 144247440
```

As Java cannot manage the memory used by *R* or *inla* the *R* script prints outs the process ID of the R processs. As R is attached as 
a DLL this is the process id of *tomcat*. 

[Process Explorer](https://docs.microsoft.com/en-gb/sysinternals/downloads/process-explorer) is a Windows tool that aloows the user to 
see the hidden R thread and the *inla* sub process.

R process ID tracer:

```
16:08:27.167 [http-nio-8080-exec-9] INFO  rifGenericLibrary.util.RIFLogger : [rifServices.dataStorageLayer.ms.MSSQLSmoothResultsSubmissionStep]:
Rengine Started; Rpid: 10644; JRI version: 266; thread ID: 30
```

![alt text](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifWebApplication/process-explorer-1.png?raw=true "Process explorer")

![alt text](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifWebApplication/process-explorer-2.png?raw=true "Java Process details")

R will be limited to the maximum private memory (resident set size) of Java, typically around 3.3GB on Windows 8.1. To go beyond this 
you will need to a) use 64bit Java! and b) set the *-Xmx* flag in  *%CATALINA_HOME%\bin\setenv.bat*; e.g. add ```-Xmx6g``` to 
*CATALINA_OPTS*

## 4.4 Common Setup Errors

A errors are to be found in the $CATALINA_BASE/logs directory, e.g.: *C:\Program Files\Apache Software Foundation\Tomcat 8.5\logs\tomcat8-stderr.2017-04-10.log*

```
10-Apr-2017 13:45:12.240 SEVERE [main] org.apache.tomcat.util.net.SSLUtilBase.getStore Failed to load keystore type [JKS] with path [conf/localhost-rsa.jks] due to [C:\Program Files\Apache Software Foundation\Tomcat 8.5\conf\localhost-rsa.jks (The system cannot find the file specified)]
```

If the RIF is started as per these instructions, the *tomcat* output trace will appear in 
*tomcat8-stdout.<date in format YYYY-MM-DD>*.

If it does not, check the tomcat service setup.

### 4.4.1 Logon RIF Serice Call Incorrect

Use developer mode in the browser to bring up the console log:

  ![alt text](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifWebApplication/caching_error.png?raw=true "Logon RIF Serice Call Incorrect")

In this example the RIF web application file RIF4\backend\services\rifs-back-urls.js (e.g. C:\Program Files\Apache Software Foundation\Tomcat 8.5\webapps\RIF4\backend\services\rifs-back-urls.js)
is set to use http://localhost:8080; but the browser, usually Chrome, used https://localhost:8080.

* Should have used: https://localhost:8080/rifServices/studySubmission/pg/login?userID=peter&password=XXXXXXXXXX
* Used cached version: http://localhost:8080/rifServices/studySubmission/pg/login?userID=peter&password=XXXXXXXXXX

```javascript
/*
 * SERVICE for URL middleware calls. Localhost can be edited here
 */
angular.module("RIF")
        .constant('studySubmissionURL', "http://localhost:8080/rifServices/studySubmission/")
        .constant('studyResultRetrievalURL', "http://localhost:8080/rifServices/studyResultRetrieval/")
        .constant('taxonomyServicesURL', "http://localhost:8080/taxonomyServices/taxonomyServices/")
```

This is caused by *rifs-back-urls.js* being changed, Tomcat restarted and Chrome or Firefox caching the rprevious service call. Flush the browser cache.

Firefox console log example:

```
17:05:09.245 Error: res.data is null
loggedIn@https://localhost:8080/RIF4/backend/services/rifs-back-interceptor.js:48:69
e/<@https://localhost:8080/RIF4/libs/standalone/angular.min.js:131:20
vf/this.$get</m.prototype.$eval@https://localhost:8080/RIF4/libs/standalone/angular.min.js:145:343
vf/this.$get</m.prototype.$digest@https://localhost:8080/RIF4/libs/standalone/angular.min.js:142:412
vf/this.$get</m.prototype.$apply@https://localhost:8080/RIF4/libs/standalone/angular.min.js:146:111
l@https://localhost:8080/RIF4/libs/standalone/angular.min.js:97:320
J@https://localhost:8080/RIF4/libs/standalone/angular.min.js:102:34
gg/</e@https://localhost:8080/RIF4/libs/standalone/angular.min.js:103:55
 1 angular.min.js:118:8
	e/< https://localhost:8080/RIF4/libs/standalone/angular.min.js:118:8
	hf/this.$get</< https://localhost:8080/RIF4/libs/standalone/angular.min.js:90:220
	e/< https://localhost:8080/RIF4/libs/standalone/angular.min.js:131:103
	vf/this.$get</m.prototype.$eval https://localhost:8080/RIF4/libs/standalone/angular.min.js:145:343
	vf/this.$get</m.prototype.$digest https://localhost:8080/RIF4/libs/standalone/angular.min.js:142:412
	vf/this.$get</m.prototype.$apply https://localhost:8080/RIF4/libs/standalone/angular.min.js:146:111
	l https://localhost:8080/RIF4/libs/standalone/angular.min.js:97:320
	J https://localhost:8080/RIF4/libs/standalone/angular.min.js:102:34
	gg/</e https://localhost:8080/RIF4/libs/standalone/angular.min.js:103:55
```

### 4.4.2 TLS Errors

TLS errors tend to be:

* Keyfile in the wrong location:
	```
	10-Apr-2017 13:45:12.240 SEVERE [main] org.apache.tomcat.util.net.SSLUtilBase.getStore Failed to load keystore type [JKS] with path [conf/localhost-rsa.jks] due to [C:\Program Files\Apache Software Foundation\Tomcat 8.5\conf\localhost-rsa.jks (The system cannot find the file specified)]
	```
* Invalid keyfile password.

### 4.4.3 Unable to unpack war files

In this case the .war file (e.g. rifServices.war) is not unpacked and the service is not available in tomcat. Find in error in the Tomcat stderr log and send to the development team. 
This is indicative of a build problem.

* Screenshots and log will be added when this happens again!*

### 4.4.4 No Taxonomy Services

See *3.1.2 Taxonomy Service*, and *4.4.3 Unable to unpack war files*

  ![alt text](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifWebApplication/taxonomy_sevice_error.png?raw=true "Taxonomy Services error")

### 4.4.5 RIF Services crash on logon  

This is an unhandled exception; caused by CATALINA_HOME not being set in the environment (```C A T A L I N A  H O M E==null==```). This will be changed to a more obvious error.

```
RIFServiceStartupOptions is web deployment
C A T A L I N A  H O M E==null==
12-Apr-2017 15:12:33.627 SEVERE [http-nio-8080-exec-1] com.sun.jersey.spi.container.ContainerResponse.mapMappableContainerException
The RuntimeException could not be mapped to a response, re-throwing to the HTTP container
 java.lang.NullPointerException
        at rifServices.system.RIFServiceStartupOptions.getRIFServiceResourcePath(RIFServiceStartupOptions.java:488)
        at rifServices.dataStorageLayer.pg.PGSQLHealthOutcomeManager.<init>(PGSQLHealthOutcomeManager.java:120)
```

### 4.4.6 SQL Server TCP/IP Java Connection Errors

This error below is caused by firewall issues, users can connect using SQL Server Management studio and *sqlcmd*:

```
com.microsoft.sqlserver.jdbc.SQLServerException: The TCP/IP connection to the host localhost, port 1433 has failed. Error: "Connection refused: connect. 
Verify the connection properties. 
Make sure that an instance of SQL Server is running on the host and accepting TCP/IP connections at the port. 
Make sure that TCP connections to the port are not blocked by a firewall.".
        at com.microsoft.sqlserver.jdbc.SQLServerException.makeFromDriverError(SQLServerException.java:206)
        at com.microsoft.sqlserver.jdbc.SQLServerException.ConvertConnectExceptionToSQLServerException(SQLServerException.java:257)
        at com.microsoft.sqlserver.jdbc.SocketFinder.findSocket(IOBuffer.java:2385)
        at com.microsoft.sqlserver.jdbc.TDSChannel.open(IOBuffer.java:567)
        at com.microsoft.sqlserver.jdbc.SQLServerConnection.connectHelper(SQLServerConnection.java:1955)
        at com.microsoft.sqlserver.jdbc.SQLServerConnection.login(SQLServerConnection.java:1616)
        at com.microsoft.sqlserver.jdbc.SQLServerConnection.connectInternal(SQLServerConnection.java:1447)
        at com.microsoft.sqlserver.jdbc.SQLServerConnection.connect(SQLServerConnection.java:788)
        at com.microsoft.sqlserver.jdbc.SQLServerDriver.connect(SQLServerDriver.java:1187)
        at java.sql.DriverManager.getConnection(Unknown Source)
        at java.sql.DriverManager.getConnection(Unknown Source)
        at rifServices.dataStorageLayer.ms.MSSQLConnectionManager.createConnection(MSSQLConnectionManager.java:695)
        at rifServices.dataStorageLayer.ms.MSSQLConnectionManager.login(MSSQLConnectionManager.java:325)
        at rifServices.dataStorageLayer.ms.MSSQLAbstractStudyServiceBundle.login(MSSQLAbstractStudyServiceBundle.java:192)
        at rifServices.dataStorageLayer.ms.MSSQLProductionRIFStudyServiceBundle.login(MSSQLProductionRIFStudyServiceBundle.java:63)
        at rifServices.restfulWebServices.ms.MSSQLAbstractRIFWebServiceResource.login(MSSQLAbstractRIFWebServiceResource.java:172)
        at rifServices.restfulWebServices.ms.MSSQLRIFStudySubmissionWebServiceResource.login(MSSQLRIFStudySubmissionWebServiceResource.java:136)
```

It is presumed that you can connect normally using *sqlcmd*:

```
sqlcmd -U peter -P XXXXXXXXXX -d sahsuland_dev -S localhost\SAHSU
1> quit

sqlcmd -U peter -P XXXXXXXXXX -d sahsuland_dev -S peter-pc\SAHSU
1> quit
```

However, attempting to connect via an IP address or full qualified domain name will fail:

```
sqlcmd -U peter -P XXXXXXXXXX -d sahsuland_dev -S 192.168.1.65\SAHSU
HResult 0xFFFFFFFF, Level 16, State 1
SQL Server Network Interfaces: Error Locating Server/Instance Specified [xFFFFFFFF].
Sqlcmd: Error: Microsoft SQL Server Native Client 10.0 : A network-related or instance-specific error has occurred while establishing a connection to SQL Server. Server is not
Sqlcmd: Error: Microsoft SQL Server Native Client 10.0 : Login timeout expired.

sqlcmd -U peter -P XXXXXXXXXX -d sahsuland_dev -S 127.0.0.1\SAHSU
HResult 0xFFFFFFFF, Level 16, State 1
SQL Server Network Interfaces: Error Locating Server/Instance Specified [xFFFFFFFF].
Sqlcmd: Error: Microsoft SQL Server Native Client 10.0 : A network-related or instance-specific error has occurred while establishing a connection to SQL Server. Server is not
Sqlcmd: Error: Microsoft SQL Server Native Client 10.0 : Login timeout expired.
```

Examination of ```netstat -ban``` output shows that SQL Server is running using dynamic ports; 57034 and 55625 in this case and not 
1433 and 1434 as expected (and setup in the firewall). The *sqlcmd* session is using shared memory, so is able to connect as long 
as you do not use an IP address or fully qualified domain name.

```
  TCP    0.0.0.0:55625          0.0.0.0:0              LISTENING
 [sqlservr.exe]
  TCP    0.0.0.0:57034          0.0.0.0:0              LISTENING
 [sqlservr.exe]
```

The method for configuring a specific port is detailed in: https://docs.microsoft.com/en-us/sql/database-engine/configure-windows/configure-a-server-to-listen-on-a-specific-tcp-port

* For all entries, clear TCP dynamic ports, set the TCP port to 1433
  ![alt text](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifWebApplication/sqlserver-change-port.png?raw=true "Configuring a specifc SQL Server port")

* Check you can logon as before using shared memory/named pipes, and then check the port
	```
	sqlcmd -U peter -P XXXXXXXXXX -d sahsuland_dev -S 192.168.1.65\SAHSU,1433
	1> quit
	```

# 5. Running the RIF

* Make sure you have restarted tomcat before attempting to run the RIF for the first time
* In a non networked single machine environment (e.g. a laptop) the RIF is at: http://localhost:8080/RIF4
* In a networked environment the RIF is at: ```http://<your domain>/RIF4```, e.g. *https://aepw-rif27.sm.med.ic.ac.uk/RIF4*

## 5.1 Logging On

* Use the *TESTUSER* created when the database was built. Do not attemot to logon as a server administrator (e.g. postgres) or the RIF 
  software owner (rif40).
* Connect to the RIF. You should see to logon page:

  ![alt text](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifWebApplication/rif_logon.png?raw=true "RIF logon")

* After logon you should see the study submission page:

  ![alt text](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifWebApplication/rif_after_logon.png?raw=true "RIF after logon")

* If you do not see this then use the section on logon troubleshootinbg below

## 5.2 Logon troubleshooting

1. Call the web service directly in a browser window, setting the username and password as appropriate.

	http://localhost:8080/rifServices/studySubmission/pg/login?userID=peterh&password=XXXXXXXXXXXXXXX

	* A sucessful logon returns:

	```
	[{"result":"User peterh logged in."}]
	```

	* A failed logon returns (as from above, my password is not *XXXXXXXXXXXXXXX*):

	```
	[{"errorMessages":["Unable to register \"peterh\"."]}]
	```

	The tomcat logs can be check for the actual error:

	```
	org.postgresql.util.PSQLException: FATAL: password authentication failed for user "peterh"
			at org.postgresql.core.v3.ConnectionFactoryImpl.doAuthentication(ConnectionFactoryImpl.java:408)
	```

2. Check the logs for any errors listed in *4.4 Common Setup Errors*
3. Use the browser developer facilities to trace the middleware web services calls. 

The service address and port used should match what you setup up in *4.2 Setup Network*. If this does not:

* Restart tomcat;
* Flush your browser cache (this is especially important for Google Chrome and Mozilla Firefox).

## 5.3 R Issues

The RIF uses Java R integration to access R directly from Java

* Rengine not being shutdown correctly on reload of service:
  ```
  Cannot find JRI native library!
  Please make sure that the JRI native library is in a directory listed in java.library.path.

  java.lang.UnsatisfiedLinkError: Native Library C:\Program Files\R\R-3.4.0\library\rJava\jri\x64\jri.dll already loaded in another classloader
        at java.lang.ClassLoader.loadLibrary0(Unknown Source)
        at java.lang.ClassLoader.loadLibrary(Unknown Source)
        at java.lang.Runtime.loadLibrary0(Unknown Source)
        at java.lang.System.loadLibrary(Unknown Source)
        at org.rosuda.JRI.Rengine.<clinit>(Rengine.java:19)
        at rifServices.dataStorageLayer.pg.PGSQLSmoothResultsSubmissionStep.performStep(PGSQLSmoothResultsSubmissionStep.java:183)
        at rifServices.dataStorageLayer.pg.PGSQLRunStudyThread.smoothResults(PGSQLRunStudyThread.java:257)
        at rifServices.dataStorageLayer.pg.PGSQLRunStudyThread.run(PGSQLRunStudyThread.java:176)
        at java.lang.Thread.run(Unknown Source)
        at rifServices.dataStorageLayer.pg.PGSQLAbstractRIFStudySubmissionService.submitStudy(PGSQLAbstractRIFStudySubmissionService
  ```
  The solution is to restart tomcat.
  
  1. Server reload needs to stop R
  2. R crashes (usually inla) and ideally script errors need to stop R
  
# 6. Patching 

## 6.1 RIF Web Application  

* Save the RIF web application file *%CATALINA_HOME%\webapps\RIF4\backend\services\rifs-back-urls.js* outside of the tomcat tree; 
* Stop Tomcat;
* Change directory to *%CATALINA_HOME%\webapps*; rename RIF4 to RIF4.old;
* Follow the instructions in 
[section 3.2 for installing the RIF Web Application](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifWebApplication/Readme.md#32-rif-web-application)
* Restore *%CATALINA_HOME%\webapps\RIF4\backend\services\rifs-back-urls.js*;
* Start tomcat;
* When you are satisiffied with the patch remove the RIF4.old directory in *%CATALINA_HOME%\webapps*.

## 6.2 RIF Middleware

* Save the Java connector for the RifServices middleware: *%CATALINA_HOME%\webapps\rifServices\WEB-INF\classes\RIFServiceStartupProperties.properties* 
outside of the tomcat tree;
* Stop Tomcat;
* Change directory to *%CATALINA_HOME%\webapps*; rename the .WAR files to .WAR.OLD; rename the rifServices and taxonomyServices trees to .old;
* Follow the instructions in 
[section 3.1 for installing the web services](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifWebApplication/Readme.md#311-rif-services);
* Start tomcat, check rifServices and taxonomyServices are unpacked and check they are running in the logs;
* Restore *%CATALINA_HOME%\webapps\rifServices\WEB-INF\classes\RIFServiceStartupProperties.properties*;
* Restart tomcat;
* When you are satisiffied with the patch remove the .old files and directories in *%CATALINA_HOME%\webapps*.

## 6.3 Tomcat

To be added. Files to be saved/restored:

* *%CATALINA_HOME%/conf/server.xml*
* *%CATALINA_HOME%/conf/web.xml*
 
ALWAYS RESTART THE SERVER!
 
Peter Hambly, 12th April 2017; revised 4th August 2017
