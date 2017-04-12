RIF Web Services
================

# Contents

- [1. Installation Prerequistes](#1-installation-prerequistes)
   - [1.1 Apache Maven](#11-apache-maven)	
   - [1.2 Java Runtime Environment](#12-java-runtime-environment)	
   - [1.3 Apache Tomcat](#13-apache-tomcat)	
     - [1.3.1 Apache Tomcat on a single host](#131-apache-tomcat-on-a-single-host)	
     - [1.3.2 Apache Tomcat for internet use](#132-apache-tomcat-for-internet-use)	
	 - [1.3.3 Running Tomcat on the command line](#133-running-tomcat-on-the-command-line)
   - [1.4 R](#14-r)	
- [2. Building Web Services using Maven](#2-building-web-services-using-maven)
- [3. Installing Web Services in Tomcat](#3-installing-web-services-in-tomcat)
   - [3.1 Web Services](#31-web-services)
     - [3.1.1 RIF Services](#311-rif-services)
     - [3.1.2 Taxonomy Service](#312-taxonomy-service)
   - [3.2 RIF Web Application](#32-rif-web-application)
- [4. RIF Setup](#4-rif-setup)
   - [4.1 Setup Database](#41-setup-database)
   - [4.2 Setup Network](#42-setup-network)
     - [4.2.1 TLS](#421-tls)
   - [4.3 Common Setup Errors](#43-common-setup-errors)
     - [4.3.1 Unable to Logon](#431-unable-to-logon)
     - [4.3.2 TLS Errors](#432-tls-errors)
     - [4.3.3 Unable to unpack war files](#433-unable-to-unpack-war-files)
     - [4.3.4 No Taxonomy Services](#434-no-taxonomy-services)
	 - [4.3.5 RIF Services crash on logon](#435-rif-services-crash-on-logon)
- [ 5. Running the RIF](#5-running-the-rif)
   - [5.1 Logging On](#51-logging-on)
   - [5.2 Logon troubleshooting](#52-logon-troubleshooting)
   
# 1. Installation Prerequistes

## 1.1 Apache Maven

Download and install Apache Maven: https://maven.apache.org/download.cgi

## 1.2 Java Runtime Environment

Download and install the Java Runtime Environment (JRE): http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)

Configure Tomcat to use the default Java installed on the machine. This prevents upgrades from breaking *tomcat*!
![alt text](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifWebApplication/configure_tomcat_app_java.png?raw=true "Setting Java version autodetect")

## 1.3 Apache Tomcat

Apache Tomcat can be downloaded from: https://tomcat.apache.org/download-80.cgi

Please use tomcat version 8, not 9 as we have not tested 9. The version tested was 8.5.13.

### 1.3.1 Apache Tomcat on a single host

This is suitables for laptops and developers with no access from other machines. Download and install tomcat; make sure your firewall blocks 
port 8080.

### 1.3.2 Apache Tomcat for internet use

The is the normal production use case. It is important that Apache Tomcat is installed securely.

Download Apache Tomcat 8.5: Follow the [OWASP guidelines](https://www.owasp.org/index.php/Securing_tomcat#Sample_Configuration_-_Good_Security for securing tomcat with good security.

*Do not just install **Tomcat** without reading the instructions first*. In particular on Windows:

- Download the core windows service installer
- Start the installation, click Next and Agree to the licence
- Untick native, documentation, examples and webapps then click Next
- Choose an installation directory (referenced as *CATALINA_HOME* from now on), preferably on a different drive to the OS.
- Choose an administrator username (NOT admin) and a secure password that complies with your organisations password policy.
- Complete tomcat installation, but do not start service.
- Set *CATALINA_HOME* in the environment (e.g. *C:\Program Files\Apache Software Foundation\Tomcat 8.5*). If you do not do this the web 
  services will not work [The web services will crash on user logon if it is not set, this will be changed to a more obvious error]; see:
  4.3.5 RIF Services crash on logon.

### 1.3.3 Running Tomcat on the command line

Tomcat can be run from the command line. The advantage of this is all the output appears in the same place! To do this the tomcat server must be
stopped (i.e. in the Windows services panel or via Linux runvel scripts (/etc/init.d/tomcat*). Notrmally tomcat is run as a server (i.e. as a 
daemon in Unix parlance).

cd to %CATALINA_HOME%\bin; run *tomcat8.exe* e.g.
```
C:\Program Files\Apache Software Foundation\Tomcat 8.5\bin> tomcat8.exe
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
* To abort, use control-C or quit the command window.

## 1.4 R

Download and install R: https://cran.ma.imperial.ac.uk/bin/windows/base

# 2. Building Web Services using Maven

Download and install Apache Maven: https://maven.apache.org/download.cgi

If you have installed make (i.e. you are building the Postgrs port from Scratch), run make from the root of the github repository,
e.g. *C:\Users\Peter\Documents\GitHub\rapidInquiryFacility*

Otherwise run the following commands by hand:
```
mvn --version
cd rifGenericLibrary; mvn -Dmaven.test.skip=true install
cd rapidInquiryFacility ;mvn -Dmaven.test.skip=true install
cd rifServices ; mvn -Dmaven.test.skip=true instal
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

# 3. Installing Web Services in Tomcat

## 3.1 Web Services

### 3.1.1 RIF Services

rifServices.war
From: C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifServices\target
To: C:\Program Files\Apache Software Foundation\Tomcat 8.5\webapps

### 3.1.2 Taxonomy Service

1) Get the Taxonomy Service XML file *ClaML.dtd*. This is stored in is stored in ...rifServices\src\main\resources. A complete ICD10 version 
   is available from SAHSU for Organisations compliant with the WHO licence.
2) Build the Taxonomy Service using *maven*.
   Either: 
   - if you have *make* installed, in the top level github directory type *make taxonomyservice" as per Maven build instructions or
   - Change to the taxonomyServices directory. In local RIF tree, go to ...rapidInquiryFacility/taxonomyServices, 
   e.g. C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\taxonomyServices and type:

	```
	mvn –Dmaven.test.skip=TRUE install
	```

3) Copy ‘taxonomyServices.war’ into Tomcat webapps as with rifServices. 

## 3.2 RIF Web Application

Create RIF4 in web-apps, cd:

cd C:\Program Files\Apache Software Foundation\Tomcat 8.5\webapps\RIF4
cd C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifWebApplication\src\main\webapp\WEB-INF
cp all to C:\Program Files\Apache Software Foundation\Tomcat 8.5\webapps\ROOT\WEB-INF

**BEFORE YOU RUN THE RIF YOU MUST SETUP THE DATABASE AND NETWORKING IN TOMCAT FIRST**

Running the RIF and logging on is detailed in section 5.

# 4 RIF Setup

## 4.1 Setup Database

rifServices/src/main/resources/RIFServiceStartupProperties.properties

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

The RIF web application file RIF4\backend\services\rifs-back-requests.js (e.g. C:\Program Files\Apache Software Foundation\Tomcat 8.5\webapps\RIF4\backend\services\rifs-back-requests.js)
define the URLs for the services.

```javascript
/*
 * SERVICE for all requests to the middleware
 */
angular.module("RIF")
        .constant('studySubmissionURL', "http://localhost:8080/rifServices/studySubmission/")
        .constant('studyResultRetrievalURL', "http://localhost:8080/rifServices/studyResultRetrieval/")
        .constant('taxonomyServicesURL', "http://localhost:8080/taxonomyServices/taxonomyServices/")
```

Edit these to match:

* The port number in use; e.g. 8081 as in the above example or 8443 if you are in a production environment with TLS enabled;
* The server for the remote service, e.g. *https://aepw-rif27.sm.med.ic.ac.uk*

**BEWARE** Make sure you keep a copy of this file; any front end RIF web application upgrade will overwrite it.

Running the RIF and logging on is detailed in section 5.

### 4.2.1 TLS

To install and configure SSL/TLS support on Tomcat, you need to follow these simple steps. For more information, read the rest of this HOW-TO.

Create a keystore file to store the server's private key and self-signed certificate by executing the following command in the $CATALINA_BASE/conf directory:
Windows:
```
cd C:\Program Files\Apache Software Foundation\Tomcat 8.5\conf

"%JAVA_HOME%\bin\keytool" -genkey -alias tomcat -keyalg RSA -keystore "C:\Program Files\Apache Software Foundation\Tomcat 8.5\conf\localhost-rsa.jks"

Unix:
```

$JAVA_HOME/bin/keytool -genkey -alias tomcat -keyalg RSA
```
On a Unix system the keystore will be put in ~/.keystore and needs to be copied to $CATALINA_BASE/conf/localhost-rsa.jks
```

Example output:
```
C:\Program Files\Apache Software Foundation\Tomcat 8.5\conf>"%JAVA_HOME%\bin\keytool" -genkey -alias tomcat -keyalg RSA
:\Program Files\Apache Software Foundation\Tomcat 8.5\conf\localhost-rsa.jks"
Enter keystore password:
Re-enter new password:
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
Check the keysore is in the corret place:
```
C:\Program Files\Apache Software Foundation\Tomcat 8.5\conf>dir localhost-rsa.jks
 Volume in drive C is OS
 Volume Serial Number is BEDC-5990

 Directory of C:\Program Files\Apache Software Foundation\Tomcat 8.5\conf

11/04/2017  13:45             2,255 localhost-rsa.jks
               1 File(s)          2,255 bytes
               0 Dir(s)  82,625,716,224 bytes free
```

Uncomment the "SSL HTTP/1.1 Connector" entry in $CATALINA_BASE/conf/server.xml and modify as described in the Configuration section in: https://tomcat.apache.org/tomcat-8.5-doc/ssl-howto.html


and specify a password value of "changeit".


## 4.3 Common Setup Errors

```
11-Apr-2017 13:47:10.353 INFO [Thread-6] org.apache.coyote.AbstractProtocol.pause Pausing ProtocolHandler ["http-nio-8081"]
11-Apr-2017 13:47:10.405 INFO [Thread-6] org.apache.coyote.AbstractProtocol.pause Pausing ProtocolHandler ["https-jsse-nio-8443"]
11-Apr-2017 13:47:10.405 INFO [Thread-6] org.apache.coyote.AbstractProtocol.pause Pausing ProtocolHandler ["ajp-nio-8009"]
11-Apr-2017 13:47:10.436 INFO [Thread-6] org.apache.catalina.core.StandardService.stopInternal Stopping service Catalina
11-Apr-2017 13:47:10.467 INFO [Thread-6] org.apache.coyote.AbstractProtocol.stop Stopping ProtocolHandler ["http-nio-8081"]
11-Apr-2017 13:47:10.467 INFO [Thread-6] org.apache.coyote.AbstractProtocol.stop Stopping ProtocolHandler ["ajp-nio-8009"]
```

Errors in the $CATALINA_BASE/logs directory

C:\Program Files\Apache Software Foundation\Tomcat 8.5\logs\tomcat8-stderr.2017-04-10.log

```
10-Apr-2017 13:45:12.240 SEVERE [main] org.apache.tomcat.util.net.SSLUtilBase.getStore Failed to load keystore type [JKS] with path [conf/localhost-rsa.jks] due to [C:\Program Files\Apache Software Foundation\Tomcat 8.5\conf\localhost-rsa.jks (The system cannot find the file specified)]

```

### 4.3.1 Unable to Logon

### 4.3.2 TLS Errors

### 4.3.3 Unable to unpack war files

### 4.3.4 No Taxonomy Services

See *3.1.2 Taxonomy Service*, and *4.3.3 Unable to unpack war files*

  ![alt text](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifWebApplication/taxonomy_sevice_error.png?raw=true "Taxonomy Services error")
  
### 4.3.5 RIF Services crash on logon  

This is an unhandled exception; caused by CATALINA_HOME not being set in the environment. This will be changed to a more obvious error.

```
RIFServiceStartupOptions is web deployment
C A T A L I N A  H O M E==null==
12-Apr-2017 15:12:33.627 SEVERE [http-nio-8080-exec-1] com.sun.jersey.spi.container.ContainerResponse.mapMappableContainerException
The RuntimeException could not be mapped to a response, re-throwing to the HTTP container
 java.lang.NullPointerException
        at rifServices.system.RIFServiceStartupOptions.getRIFServiceResourcePath(RIFServiceStartupOptions.java:488)
        at rifServices.dataStorageLayer.pg.PGSQLHealthOutcomeManager.<init>(PGSQLHealthOutcomeManager.java:120)
```

# 5. Running the RIF

* In a non networked single machine environment (e.g. a laptop) the RIF is at: http://localhost:8081/RIF4
* In a networked environment the RIF is at: http://<your domain>/RIF4, e.g. https://aepw-rif27.sm.med.ic.ac.uk/RIF4

## 5.1 Logging On

* Use the *TESTUSER* created when the database was built. Do not attemot to logon as a server administrator (e.g. postgres) or the RIF 
  software owner (rif40).
* Connect to the RIF. You should see to logon page:

  ![alt text](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifWebApplication/rif_logon.png?raw=true "RIF logon")

* After logon you should see the study submission page:

  ![alt text](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifWebApplication/rif_after_logon.png?raw=true "RIF after logon")

* If you do not see the section on logon troubleshootinbg below

## 5.2 Logon troubleshooting

1. Call the web service directly in a brwoser window.

http://localhost:8080/rifServices/studySubmission/pg/login?userID=peter&password=XXXXXXXXXXXXXXX

[{"result":"User peter logged in."}]

Peter Hambly, 12th April 2017