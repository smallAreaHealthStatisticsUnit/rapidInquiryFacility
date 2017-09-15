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
	 - [1.3.6 Tomcat Logging (Log4j2) Setup](#135-tomcat-logging-log4j2-setup) 
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
  
  Logs go to STDOUT and ```${sys:catalina.base}/log4j2/<YYYY>-<MM>/``` and ```%CATALINA_HOME%/log4j2/<YYYY>-<MM>/```
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
      <!-- <AppenderRef ref="Console"/> -->
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

Logging output within the application is controlled in three ways:

1. The logging level. This should be WARN, DEBUG or INFO. INFO is normally sufficient
2. INFO logging is controlled by class using the properties file: 
   *%CATALINA_HOME%\webapps\rifServices\src\main\resources\RIFLogger.properties*. Note that
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
rifGenericLibrary.dataStorageLayer.pg.PGSQLQueryUtility=true

rifServices.restfulWebServices.pg.PGSQLRIFStudySubmissionWebServiceResource=true

rifServices.dataStorageLayer.pg.PGSQLProductionRIFStudyRetrievalService=false

rifServices.dataStorageLayer.pg.PGSQLRIFContextManager=true

...

```
3. SQL Query INFO logging is controlled by query name using the properties file: 
   *%CATALINA_HOME%\webapps\rifServices\src\main\resources\AbstractSQLManager.properties*
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

To be added.

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
packages <- c("plyr", "abind", "maptools", "spdep", "RODBC", "MatrixModels", "rJava")
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
   *C:\Program Files\R\R-3.4.0\bin\x64;C:\Program Files\R\R-3.4.0\library\rJava\jri\x64*. This ensures that file "x64\jri.dll" is in java.library.path
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
    
	If there are errors loading JRI the middleware will crash with an error like:
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

Beware of 32 bit Java errors

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

Typical errors for the older batch script version (now obsolecent):

* *R_HOME* not setup: ```command==null\bin\x64\RScript```
* No scratchSpace directory: *c:\rifDemo\scratchSpace\* ```(The system cannot find the path specified)```

Tomcat logs:

1. Errors shown above:
```
Investigation name==My_New_Investigation  ID==5==
command==null\bin\x64\RScript C:\\"Program Files"\\"Apache Software Foundation"\\"Tomcat 8.5"\\webapps\\rifServices\\WEB-INF\\classe
s\Adj_Cov_Smooth.R  --db_driver_prefix=jdbc:postgresql  --db_host=localhost  --db_port=5432  --db_name=sahsuland  --db_driver_class_
name=org.postgresql.Driver  --study_id=5  --investigation_name=MY_NEW_INVESTIGATION  --covariate_name=NONE  --investigation_id=5  --
r_model=bym_r_procedure  --odbc_data_source=PostgreSQL30  --user_id=peter  --password=xxxxxx==
java.io.FileNotFoundException: c:\rifDemo\scratchSpace\kevTest22_01-May-2017.bat (The system cannot find the path specified)
        at java.io.FileOutputStream.open0(Native Method)
        at java.io.FileOutputStream.open(FileOutputStream.java:270)
        at java.io.FileOutputStream.<init>(FileOutputStream.java:213)
        at java.io.FileOutputStream.<init>(FileOutputStream.java:162)
        at rifServices.dataStorageLayer.pg.PGSQLAbstractRService.createBatchFile(PGSQLAbstractRService.java:274)
        at rifServices.dataStorageLayer.pg.PGSQLSmoothResultsSubmissionStep.performStep(PGSQLSmoothResultsSubmissionStep.java:186)
        at rifServices.dataStorageLayer.pg.PGSQLRunStudyThread.smoothResults(PGSQLRunStudyThread.java:278)
        at rifServices.dataStorageLayer.pg.PGSQLRunStudyThread.run(PGSQLRunStudyThread.java:194)
        at java.lang.Thread.run(Thread.java:745)
        at rifServices.dataStorageLayer.pg.PGSQLAbstractRIFStudySubmissionService.submitStudy(PGSQLAbstractRIFStudySubmissionService
.java:1878)
        at rifServices.restfulWebServices.pg.PGSQLAbstractRIFWebServiceResource.submitStudy(PGSQLAbstractRIFWebServiceResource.java:
```

If you get no results in the data viewer; re-run the batch file command manually:

```bat
C:\Windows\system32>cd C:\rifDemo\scratchSpace

C:\rifDemo\scratchSpace>kevTest22_01-May-2017.bat
C:\rifDemo\scratchSpace>C:\"Program Files"\R\R-3.4.0\bin\x64\RScript C:\\"Program Files"\\"Apache Software Foundation"\\"Tomcat 8.5"
\\webapps\\rifServices\\WEB-INF\\classes\Adj_Cov_Smooth.R  --db_driver_prefix=jdbc:postgresql  --db_host=localhost  --db_port=5432
--db_name=sahsuland  --db_driver_class_name=org.postgresql.Driver  --study_id=7  --investigation_name=MY_NEW_INVESTIGATION  --covari
ate_name=NONE  --investigation_id=7  --r_model=bym_r_procedure  --odbc_data_source=PostgreSQL35W  --user_id=peter  --password=xxxxxx

Loading required package: sp
Loading required package: methods
Loading required package: Matrix
This is INLA 0.0-1485844051, dated 2017-01-31 (09:14:12+0300).
See www.r-inla.org/contact-us for how to get help.
Checking rgeos availability: FALSE
        Note: when rgeos is not available, polygon geometry     computations in maptools depend on gpclib,
        which has a restricted licence. It is disabled by default;
        to enable gpclib, type gpclibPermit()
[1] "Arguments were supplied"
[1] "Parsing parameters"
                   name                 value
1      db_driver_prefix       jdbc:postgresql
2               db_host             localhost
3               db_port                  5432
4               db_name             sahsuland
5  db_driver_class_name org.postgresql.Driver
6              study_id                     7
7    investigation_name  MY_NEW_INVESTIGATION
8        covariate_name                  NONE
9      investigation_id                     7
10              r_model       bym_r_procedure
11     odbc_data_source         PostgreSQL35W
12              user_id                 peter
13             password                 peter
[1] "Study ID: 7"
[1] "Performing basic stats and smoothing"
[1] "============EXTRACT TABLE NAME ===================="
[1] "rif_studies.s7_extract"
[1] "============EXTRACT TABLE NAME ===================="
[1] "rif_studies.s7_extract numberOfRows=433312=="
[1] "rif40_GetAdjacencyMatrix numberOfRows=1229=="
[1] "Bayes smoothing with BYM model type no adjustment"
Error in if (scale.model) { : argument is of length zero
Calls: performSmoothingActivity ... inla.interpret.formula -> eval -> eval -> <Anonymous>
Execution halted
Warning message:
closing unused RODBC handle 1
```

2. Tomcat sucessful run:
```
==========================================================

SQLStudyStateManager updateStudyStatus 2
run generate results AFTER state==Study extracted==
run smooth results BEFORE state==Study extracted==
SQLSmoothedResultsSubmissionStep getInvestigationID studyID==7==investigation_name==My_New_Investigation==inv_description====
=======getInvestigationID========1===
StudyID==7==
Inv_name==MY_NEW_INVESTIGATION==
SELECT
   inv_id
FROM
   rif40.rif40_investigations
WHERE
   study_id=? AND
   inv_name=?;


;


=======getInvestigationID========2===
About to call next
called next
Investigation name==My_New_Investigation  ID==7==
command==C:\"Program Files"\R\R-3.4.0\bin\x64\RScript C:\\"Program Files"\\"Apache Software Foundation"\\"Tomcat 8.5"\\webapps\\rifS
ervices\\WEB-INF\\classes\Adj_Cov_Smooth.R  --db_driver_prefix=jdbc:postgresql  --db_host=localhost  --db_port=5432  --db_name=sahsu
land  --db_driver_class_name=org.postgresql.Driver  --study_id=7  --investigation_name=MY_NEW_INVESTIGATION  --covariate_name=NONE
--investigation_id=7  --r_model=bym_r_procedure  --odbc_data_source=PostgreSQL35W  --user_id=peter  --password=peter==
Writing batch file==C:\"Program Files"\R\R-3.4.0\bin\x64\RScript C:\\"Program Files"\\"Apache Software Foundation"\\"Tomcat 8.5"\\we
bapps\\rifServices\\WEB-INF\\classes\Adj_Cov_Smooth.R  --db_driver_prefix=jdbc:postgresql  --db_host=localhost  --db_port=5432  --db
_name=sahsuland  --db_driver_class_name=org.postgresql.Driver  --study_id=7  --investigation_name=MY_NEW_INVESTIGATION  --covariate_
name=NONE  --investigation_id=7  --r_model=bym_r_procedure  --odbc_data_source=PostgreSQL35W  --user_id=peter  --password=xxxxx
Exit value==1==
SQLStudyStateManager updateStudyStatus 1
AbstractSQLManager logSQLQuery 1rifServices.dataStorageLayer.pg.PGSQLStudyStateManager==
==========================================================
QUERY NAME:createStatusTable
PARAMETERS:
        1:"user"

SQL QUERY TEXT
CREATE TABLE IF NOT EXISTS peter.study_status (
   study_id INTEGER NOT NULL,
   study_state VARCHAR NOT NULL,
   creation_date TIMESTAMP NOT NULL,
   ith_update SERIAL NOT NULL,
   message VARCHAR(255));


==========================================================

SQLStudyStateManager updateStudyStatus 2
run smooth results AFTER state==Study results computed==
Finished!!
isClientBrowserIE==true==
```

3. STUDY_STATUS local user table:
```
sahsuland=> SELECT * FROM study_status WHERE study_id = 7 ORDER BY ith_update;
 study_id | study_state |       creation_date        | ith_update |                                       message
----------+-------------+----------------------------+------------+-------------------------------------------------------------------------------------
        7 | C           | 2017-05-01 16:01:01.121765 |          0 | The study has been created but it has not been verified.
        7 | E           | 2017-05-01 16:03:48.402219 |          1 | Study extracted imported or created but neither results nor maps have been created.
        7 | R           | 2017-05-01 16:04:06.503855 |          2 | The study results have been computed and they are now ready to be used.
(3 rows)
```

**More to be added; especially R debugging.**

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
 
Peter Hambly, 12th April 2017; revised 4th August 2017
