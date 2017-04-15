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
   - [1.4 R](#14-r)	
- [2. Building Web Services using Maven](#2-building-web-services-using-maven)
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
   
# 1. Installation Prerequistes

## 1.1 Apache Maven

Download and install Apache Maven: https://maven.apache.org/download.cgi

## 1.2 Java Runtime Development

The Java Runtime Environment (JRE) can be used if the war files are pre-supplied and the OWASP requirement to remove the 
version string from HTTP error messages by repacking  %CATALINA_HOME%/server/lib/catalina.jar with an updated 
*ServerInfo.properties* file is not required. 

Download and install the Java Development Environment (JDK): http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

Make sure all the older versions of Java are removed.

Configure Tomcat to use the default Java installed on the machine. This prevents upgrades from breaking *tomcat*!
![alt text](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifWebApplication/configure_tomcat_app_java.png?raw=true "Setting Java version autodetect")

## 1.3 Apache Tomcat

Apache Tomcat can be downloaded from: https://tomcat.apache.org/download-80.cgi

Please use tomcat version 8, not 9 as we have not tested 9. The version tested was 8.5.13.

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
  services will not work [The web services will crash on user logon if it is not set, this will be changed to a more obvious error]; see:
  4.4.5 RIF Services crash on logon.
  
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

cd to %CATALINA_HOME%\bin; run *tomcat8.exe* e.g.
```
C:\Users\Peter\Documents\GitHub\rapidInquiryFacility> cd %CATALINA_HOME%\bin
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

* Copy *rifServices.war* from: *rapidInquiryFacility\rifServices\target*, e.g. *C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifServices\target*
  to: *%CATALINA_HOME%\webapps*, e.g. *C:\Program Files\Apache Software Foundation\Tomcat 8.5\webapps*

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

3) Copy ‘taxonomyServices.war’ into the Tomcat webapps folder as with rifServices. 

## 3.2 RIF Web Application

Create RIF4 in web-apps:

* *cd "C:\Program Files\Apache Software Foundation\Tomcat 8.5\webapps\RIF4"*
* Create the directory *RIF4*
* Copy all from the drecitory: *"C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifWebApplication\src\main\webapp\WEB-INF"* 
  to *C:\Program Files\Apache Software Foundation\Tomcat 8.5\webapps\ROOT\WEB-INF*

**BEFORE YOU RUN THE RIF YOU MUST SETUP THE DATABASE AND NETWORKING IN TOMCAT FIRST**

Running the RIF and logging on is detailed in section 5. You must restart Tomcat when you create RIF4 for the first time, 
it is not automatically spotted unlike the services *.war* files..

# 4 RIF Setup

## 4.1 Setup Database

The Java connector for theRifServices middles is setup in the file: *%CATALINA_HOME%\webapps\rifServices\WEB-INF\classes\RIFServiceStartupProperties.properties*

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
database.host=AEPW-RIF27\\SQLEXPRESS
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
- Firefox 32 and later
- IE 11 and later
- IE Mobile 11 and later
- Java 8 b132
- Safari 7 and later

## 4.3 Setup R
  
1. Create directories for extract (extractDirectory) and policies (extraDirectoryForExtractFiles). The defaults are:

   * Extract: ```extractDirectory=c:\\rifDemo\\scratchSpace```
   * Policies: ```extraDirectoryForExtractFiles=C:\\rifDemo\\generalDataExtractPolicies```

2. Create a system ODBC datasource for the database in use; the default is:

   * ODBC sytsrem data source: ```odbcDataSourceName=PostgreSQL30```

These setting are in the Java connector for the RifServices middleware: *rifServices/src/main/resources/RIFServiceStartupProperties.properties*

**BEWARE** Make sure you keep a copy of this file; any front end RIF web application upgrade will overwrite it.

```java
webApplicationDirectory=rifServices
rScriptDirectory=rScripts
maximumMapAreasAllowedForSingleDisplay=200
extractDirectory=c:\\rifDemo\\scratchSpace
odbcDataSourceName=PostgreSQL30
extraDirectoryForExtractFiles=C:\\rifDemo\\generalDataExtractPolicies
```
  
More to be added.

## 4.4 Common Setup Errors

A errors are to be found in the $CATALINA_BASE/logs directory, e.g.: *C:\Program Files\Apache Software Foundation\Tomcat 8.5\logs\tomcat8-stderr.2017-04-10.log*

```
10-Apr-2017 13:45:12.240 SEVERE [main] org.apache.tomcat.util.net.SSLUtilBase.getStore Failed to load keystore type [JKS] with path [conf/localhost-rsa.jks] due to [C:\Program Files\Apache Software Foundation\Tomcat 8.5\conf\localhost-rsa.jks (The system cannot find the file specified)]
```

### 4.4.1 Logon RIF Serice Call Incorrect

Use developer mode in the browser to bring up the console log:

  ![alt text](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifWebApplication/caching_error.png?raw=true "Logon RIF Serice Call Incorrect")

In this example the RIF web application file RIF4\backend\services\rifs-back-requests.js (e.g. C:\Program Files\Apache Software Foundation\Tomcat 8.5\webapps\RIF4\backend\services\rifs-back-requests.js)
is set to use http://localhost:8080; but the browser, usually Chrome, used https://localhost:8080.

* Should have used: https://localhost:8080/rifServices/studySubmission/pg/login?userID=peter&password=XXXXXXXXXX
* Used cached version: http://localhost:8080/rifServices/studySubmission/pg/login?userID=peter&password=XXXXXXXXXX

```javascript
/*
 * SERVICE for all requests to the middleware
 */
angular.module("RIF")
        .constant('studySubmissionURL', "http://localhost:8080/rifServices/studySubmission/")
        .constant('studyResultRetrievalURL', "http://localhost:8080/rifServices/studyResultRetrieval/")
        .constant('taxonomyServicesURL', "http://localhost:8080/taxonomyServices/taxonomyServices/")
```

This is caused by *rifs-back-requests.js* being changed, Tomcat restarted and Chrome or Firefox caching the rprevious service call. Flush the browser cache.

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

* Screenshiots and log will be added when this happens again!*

### 4.4.4 No Taxonomy Services

See *3.1.2 Taxonomy Service*, and *4.4.3 Unable to unpack war files*

  ![alt text](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifWebApplication/taxonomy_sevice_error.png?raw=true "Taxonomy Services error")

### 4.4.5 RIF Services crash on logon  

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

### 4.4.6 SQL Server TCP/IP Java Connection Errors

This error below is caused by firewall issues:

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
sqlcmd -U peter -P peter -d sahsuland_dev -S 192.168.1.65\SAHSU
HResult 0xFFFFFFFF, Level 16, State 1
SQL Server Network Interfaces: Error Locating Server/Instance Specified [xFFFFFFFF].
Sqlcmd: Error: Microsoft SQL Server Native Client 10.0 : A network-related or instance-specific error has occurred while establishing a connection to SQL Server. Server is not
Sqlcmd: Error: Microsoft SQL Server Native Client 10.0 : Login timeout expired.

sqlcmd -U peter -P peter -d sahsuland_dev -S 127.0.0.1\SAHSU
HResult 0xFFFFFFFF, Level 16, State 1
SQL Server Network Interfaces: Error Locating Server/Instance Specified [xFFFFFFFF].
Sqlcmd: Error: Microsoft SQL Server Native Client 10.0 : A network-related or instance-specific error has occurred while establishing a connection to SQL Server. Server is not
Sqlcmd: Error: Microsoft SQL Server Native Client 10.0 : Login timeout expired.
```

Examination of ```netstat -ban``` output shows that SQL SErver is running using dynamic ports; 57034 and 55625 in this case and not 
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
	sqlcmd -U peter -P peter -d sahsuland_dev -S 192.168.1.65\SAHSU,1433
	1> quit
	```

# 5. Running the RIF

* Make sure you have restarted tomcat before attempting to run the RIF for the first time
* In a non networked single machine environment (e.g. a laptop) the RIF is at: http://localhost:8081/RIF4
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
* Flush your rowser cache (this is especially important for Google Chrome and Mozilla Firefox).

Peter Hambly, 12th April 2017