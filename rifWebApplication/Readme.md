RIF Web Services
================

# Contents

- [1. Installation Prerequistes](#1-installation-prerequistes)
   - [1.1 Apache Maven](#11-apache-maven)	
   - [1.2 Java Runtime Environment](#12-java-runtime-environment)	
   - [1.3 Apache Tomcat](#13-apache-tomcat)	
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
   
# 1. Installation Prerequistes

## 1.1 Apache Maven

Download and install Apache Maven: https://maven.apache.org/download.cgi

## 1.2 Java Runtime Environment

Download and install the Java Runtime Environment (JRE): http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)

Configure Tomcat to use the default Java installed on the machine. This prevents upgrades from breaking *tomcat*!
![alt text](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifWebApplication/configure_tomcat_app_java%20_ab.png?raw=true "Setting Java version autodetect")

## 1.3 Apache Tomcat

Download Apache Tomcat 8.5: Follow the [OWASP guidelines](https://www.owasp.org/index.php/Securing_tomcat#Sample_Configuration_-_Good_Security for securing tomcat with good security.

*Do not just install **Tomcat** without reading the instructions first*. In particular on Windows:

- Download the core windows service installer
- Start the installation, click Next and Agree to the licence
- Untick native, documentation, examples and webapps then click Next
- Choose an installation directory (referenced as *CATALINA_HOME* from now on), preferably on a different drive to the OS.
- Choose an administrator username (NOT admin) and a secure password that complies with your organisations password policy.
- Complete tomcat installation, but do not start service.
- Set *CATALINA_HOME* in the environment (e.g. *C:\Program Files\Apache Software Foundation\Tomcat 8.5*). If you do not do this the web 
  services will not work [The web services crash if it is not set, the will be changed to a more obvious error]
  
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

1) Change to the taxonomyServices/target directory
In local RIF tree, go to...rapidInquiryFacility/taxonomyServices/target, e.g. C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\/taxonomyServices/target

2) Build using *maven*
mvn –Dmaven.test.skip=TRUE install

3) Copy ‘taxonomyServices.war’ into Tomcat webapps as with rifServices

(The XML file used for this is *ClaML.dtd* which is stored in \rifServices\src\main\resources). A complete ICD10 version is available from SAHSU 
for Organisation compliant with the WHO licence.

## 3.2 RIF Web Application

Create RIF4 in web-apps, cd:

cd C:\Program Files\Apache Software Foundation\Tomcat 8.5\webapps\RIF4
cd C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifWebApplication\src\main\webapp\WEB-INF
cp all to C:\Program Files\Apache Software Foundation\Tomcat 8.5\webapps\ROOT\WEB-INF

http://localhost:8081/RIF4

# 4 RIF Setup

## 4.1 Setup Database

rifServices/src/main/resources/RIFServiceStartupProperties.properties


## 4.2 Setup Network

C:\Program Files\Apache Software Foundation\Tomcat 8.5\webapps\RIF4\backend\services\rifs-back-requests.js

Port 8080/netstat -ban

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
and specify a password value of "changeit".



Peter Hambly, 12th April 2017