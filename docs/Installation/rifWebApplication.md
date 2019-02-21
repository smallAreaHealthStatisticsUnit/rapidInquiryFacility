---
layout: default
title: RIF Web Application and Middleware Installation
---

1. Contents
{:toc}

# Installation Prerequisites

These instructions are to install and setup the RIF middleware and web application (front end) and are for Windows Apache Tomcat. Linux Tomcat will be very similar. It is assumed that the installer knows how to:

* Modify Windows file permissions
* Set environment variables; check settings; setup up the executable and library search paths
* Install and de-install programs
* Start and stop system services
* Has the access rights to administer the installation machine.

If you are running with power user privilege, as most laptops and Imperial staff PCs do, you already have *far too much* privilege and you may not need
to modify file permissions much.

For help on file permission see: [Windows file permissions](https://technet.microsoft.com/en-us/library/dd277411.aspx). As a general rule, it is much
better to add yourself, probably with full control to a file or directory than to take ownership of a file or folder. Also, be careful about giving yourself full control over
binary and configuration directories. Remember less privilege is always more secure!

The RIF web application will install on a modern laptop.

Complex Apache Tomcat setup (e.g. clustering, runtime deployment of updated WAR files) are not within the scope of this document
of this document and are not required for a simple RIF setup.

## Apache Maven

Apache Maven is required to build the RIF web application (War) files and the data loader tool from source. It is
not required if you are supplied with pre-built copies (in the *Tomcat webaapps* directory).

Download and install Apache Maven: (https://maven.apache.org/download.cgi)

## Java Runtime Development

Java is the software language the RIF middleware  code in and it must be installed on your machine.

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

If you build the *war* files using Maven you **MUST** download and install the Java Development Environment (JDK): http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

**DO NOT USE JAVA 9 or JAVA 10**. They have not been tested!

**Make sure to install the 64 bit version of Java**, unless you have a 32 bit ONLY machine (*This is very unlikely
and has not been tested - we DO NOT have any!*). The 32 bit version will cause 32/64 bit issues with R.

- If you use the Java Runtime Environment (JRE), set *JRE_HOME* in the environment (e.g. *C:\Program Files\Java\jre1.8.0_111*).
- If you use the Java Development Environment (JDK), set *JAVA_HOME* in the environment (e.g. *C:\Program Files\Java\jdk1.8.0_111*).
- Add the Java bin directory (*C:\Program Files\Java\jdk1.8.0_111\bin*) to the path.
- Test Java is installed correctly with *java -showversion* in a new command window.

JRE_HOME is used by the Apache Tomcat manual start script *catalina.bat*. Normally, Java upgrades go into the same
directory as installed, but if Java is upgraded by hand or re-installed these environment settings may need to
be changed.

## Apache Tomcat

Apache Tomcat in the Java web server that runs the RIF. Tomcat can be downloaded from: https://tomcat.apache.org/download-80.cgi

Please use Tomcat version 8, not 9 as we have not tested 9. The version tested was 8.5.13. It is advised to use the MSI
version.

Set the following environment variables using the system control panel: *Control Panel\All Control Panel Items\System:*. This is
well hidden on Windows 10, but you can type the path into Windows explorer! Choose *Advanced System Settings*, *Environment variables* and modify the *System Variables* using administrator privileges.

* Add CATALINA_HOME=&lt;Tomcat install directory; e.g. C:\Program Files\Apache Software Foundation\Tomcat 8.5&gt; to the global environment.
* Add &lt;Tomcat bin directory; e.g. C:\Program Files\Apache Software Foundation\Tomcat 8.5\bin&gt; to the path

Start a new command window as an Administrator (type *cmd* into windows search, right click on the command icon and select "run as Administrator").

Use the configure Tomcat application (Tomcat8w) to use the default Java installed on the machine. This prevents upgrades from breaking Tomcat!

![Setting Java version autodetect]({{ site.baseurl }}/rifWebApplication/tomcat8_configuration_3.PNG)

This makes Tomcat Java upgrade proof; but this may have unintended effects if:

* You have not removed all the old Java releases
* You install another version of Java (e.g. the Oracle installer may do this)

### Apache Tomcat on a single host

This is suitable for laptops and developers with no access from other machines. Download and install Tomcat; make sure your firewall blocks
port 8080. You do **NOT** need to follow the OWASP guidelines or to configure TLS as described in
[Securing Tomcat]({{ site.baseurl }}/Installation/rifWebApplication#securing-tomcat).

### Apache Tomcat for internet use

The is the normal production use case. It is important that Apache Tomcat is installed securely. It is *NOT* required for laptops.

Download Apache Tomcat 8.5 and follow the [OWASP Tomcat guidelines](https://www.owasp.org/index.php/Securing_tomcat#Sample_Configuration_-_Good_Security) for securing Tomcat with good security.

*Do not just install Tomcat without reading the instructions first*. In particular on Windows:

- Download the core windows service installer
- Start the installation, click Next and Agree to the licence
- Untick native, documentation, examples and webapps then click Next
- Choose an installation directory (referenced as *CATALINA_HOME* from now on), preferably on a different drive to the OS.
- Choose an administrator username (NOT admin) and a secure password that complies with your organisations password policy.
- Complete Tomcat installation, but do not start service.
- Set *CATALINA_HOME* in the environment (e.g. *C:\Program Files\Apache Software Foundation\Tomcat 8.5*). If you do not do this the web
  services will not work [The web services will fail to start on the first user logon if it is not set]; see:
  [RIF Services crash on logon]({{ site.baseurl }}/Installation/rifWebApplication#rif-services-crash-on-logon).
- If *CATALINA_HOME* is *C:\Program Files (x86)\Apache Software Foundation\Tomcat 8.5* you have installed the 32 bit version of Java.
  Remove Tomcat and Java and re-install a 64 bit Java (unless you are on a really old 32 bit only Machine...)

When accessed from the internet the RIF **must** be secured using TLS to protect the login details and any health data viewed.

Notes on the OWASP section on removing the version string from HTTP error messages by repacking *%CATALINA_HOME%/server/lib/catalina.jar* with
an updated ServerInfo.properties:

* The JAR file is in: *%CATALINA_HOME%/lib/catalina.jar*
* The intention of this change is to defeat Lamdba probes by malicious penetration testers. This change **may** have the side affect of defeating your own
  security assurance software (it appears to defeat Nessus). It may therefore be necessary to not implement this change until you have
  completed security testing.

### Running Tomcat on the command line

**Do this first, before you try to run the RIF as a service or configure the logging.**

Tomcat can be run from the command line. The advantage of this is all the output appears in the same place! To do this the Tomcat server must be
stopped (i.e. in the Windows services panel or via Linux runlevel scripts (/etc/init.d/tomcat*). Normally Tomcat is run as a server (i.e. as a
daemon in Unix parlance).

**Make sure you start a new command window (cmd) after setting any environment variables**. The new settings will *NOT* be picked up otherwise.

**It is advisable at this point to install the WAR files in the %CATALINA_HOME\webapps directory before you start the RIF. See section 3. Normally
these are pre-supplied (in the *Tomcat webapps* folder)This will get Tomcat to expand the WAR files and all the configuration and example
files in this section will then appear. The RIF will not work until you configure it correctly in section 4.**

cd to %CATALINA_HOME%\bin; run *catalina.bat* with the parameter *start* or *stop*.

Do NOT run *tomcat8.exe*; this will work but you will not be able to interrupt Tomcat! (This is caused by the
Java R interface removing the control-C handler)

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

Not setting JRE_HOME or JAVA_HOME results in:

```
C:\Program Files\Apache Software Foundation\Tomcat 8.5\bin>catalina.bat
Neither the JAVA_HOME nor the JRE_HOME environment variable is defined
At least one of these environment variable is needed to run this program
```

You may get a complaint from your firewall or security software; allow Tomcat the access it requires. Do *NOT* disable Tomcat or the RIF will not work!

  ![Prevent Tomcat from being disabled by your security software]({{ site.baseurl }}/rifWebApplication/windows_defender_message.png)

You may need to consult a system or r network administrator at this point.

Normally this pops up a Java scrollable window:
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

A successful start of the RIF looks like:
```
12-Apr-2018 14:58:26.423 INFO [main] org.apache.catalina.startup.VersionLoggerListener.log Server version:        Apache Tomcat/8.5.29
12-Apr-2018 14:58:26.425 INFO [main] org.apache.catalina.startup.VersionLoggerListener.log Server built:          Mar 5 2018 13:11:12 UTC
12-Apr-2018 14:58:26.425 INFO [main] org.apache.catalina.startup.VersionLoggerListener.log Server number:         8.5.29.0
12-Apr-2018 14:58:26.425 INFO [main] org.apache.catalina.startup.VersionLoggerListener.log OS Name:               Windows 10
12-Apr-2018 14:58:26.425 INFO [main] org.apache.catalina.startup.VersionLoggerListener.log OS Version:            10.0
12-Apr-2018 14:58:26.425 INFO [main] org.apache.catalina.startup.VersionLoggerListener.log Architecture:          amd64
12-Apr-2018 14:58:26.426 INFO [main] org.apache.catalina.startup.VersionLoggerListener.log Java Home:             C:\Program Files\Java\jdk1.8.0_162\jre
12-Apr-2018 14:58:26.426 INFO [main] org.apache.catalina.startup.VersionLoggerListener.log JVM Version:           1.8.0_162-b12
12-Apr-2018 14:58:26.426 INFO [main] org.apache.catalina.startup.VersionLoggerListener.log JVM Vendor:            Oracle Corporation
12-Apr-2018 14:58:26.427 INFO [main] org.apache.catalina.startup.VersionLoggerListener.log CATALINA_BASE:         C:\Program Files\Apache Software Foundation\Tomcat 8.5
12-Apr-2018 14:58:26.427 INFO [main] org.apache.catalina.startup.VersionLoggerListener.log CATALINA_HOME:         C:\Program Files\Apache Software Foundation\Tomcat 8.5
12-Apr-2018 14:58:26.428 INFO [main] org.apache.catalina.startup.VersionLoggerListener.log Command line argument: -Djava.util.logging.config.file=C:\Program Files\Apache Software Foundation\Tomcat 8.5\conf\logging.properties
12-Apr-2018 14:58:26.428 INFO [main] org.apache.catalina.startup.VersionLoggerListener.log Command line argument: -Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager
12-Apr-2018 14:58:26.428 INFO [main] org.apache.catalina.startup.VersionLoggerListener.log Command line argument: -Djdk.tls.ephemeralDHKeySize=2048
12-Apr-2018 14:58:26.428 INFO [main] org.apache.catalina.startup.VersionLoggerListener.log Command line argument: -Djava.protocol.handler.pkgs=org.apache.catalina.webresources
12-Apr-2018 14:58:26.428 INFO [main] org.apache.catalina.startup.VersionLoggerListener.log Command line argument: -Dignore.endorsed.dirs=
12-Apr-2018 14:58:26.428 INFO [main] org.apache.catalina.startup.VersionLoggerListener.log Command line argument: -Dcatalina.base=C:\Program Files\Apache Software Foundation\Tomcat 8.5
12-Apr-2018 14:58:26.429 INFO [main] org.apache.catalina.startup.VersionLoggerListener.log Command line argument: -Dcatalina.home=C:\Program Files\Apache Software Foundation\Tomcat 8.5
12-Apr-2018 14:58:26.429 INFO [main] org.apache.catalina.startup.VersionLoggerListener.log Command line argument: -Djava.io.tmpdir=C:\Program Files\Apache Software Foundation\Tomcat 8.5\temp
12-Apr-2018 14:58:26.429 INFO [main] org.apache.catalina.core.AprLifecycleListener.lifecycleEvent The APR based Apache Tomcat Native library which allows optimal performance in production environments was not found on the java.library.path: [C:\Program Files\Java\jdk1.8.0_162\bin;C:\Windows\Sun\Java\bin;C:\Windows\system32;C:\Windows;C:\Program Files (x86)\Intel\Intel(R) Management Engine Components\iCLS\;C:\Program Files\Intel\Intel(R) Management Engine Components\iCLS\;C:\Windows\system32;C:\Windows;C:\Windows\System32\Wbem;C:\Windows\System32\WindowsPowerShell\v1.0\;C:\Program Files (x86)\Intel\Intel(R) Management Engine Components\DAL;C:\Program Files\Intel\Intel(R) Management Engine Components\DAL;C:\Program Files (x86)\Intel\Intel(R) Management Engine Components\IPT;C:\Program Files\Intel\Intel(R) Management Engine Components\IPT;C:\Program Files\PostgreSQL\9.6\bin;C:\Program Files\Java\jdk1.8.0_162\bin;C:\Program Files\Apache Software Foundation\apache-maven-3.5.3\bin;C:\Program Files\Microsoft SQL Server\Client SDK\ODBC\130\Tools\Binn\;C:\Program Files (x86)\Microsoft SQL Server\130\Tools\Binn\;C:\Program Files\Microsoft SQL Server\130\Tools\Binn\;C:\Program Files\Microsoft SQL Server\130\DTS\Binn\;C:\Program Files (x86)\Microsoft SQL Server\Client SDK\ODBC\130\Tools\Binn\;C:\Program Files (x86)\Microsoft SQL Server\140\Tools\Binn\;C:\Program Files (x86)\Microsoft SQL Server\140\DTS\Binn\;C:\Program Files (x86)\Microsoft SQL Server\140\Tools\Binn\ManagementStudio\;C:\Program Files\nodejs\;C:\Program Files\dotnet\;C:\MinGW\msys\1.0\bin;C:\Program Files\Apache Software Foundation\Tomcat 8.5\bin;C:\Users\admin\AppData\Local\Microsoft\WindowsApps;.]
12-Apr-2018 14:58:26.725 INFO [main] org.apache.coyote.AbstractProtocol.init Initializing ProtocolHandler ["http-nio-8080"]
12-Apr-2018 14:58:27.012 INFO [main] org.apache.tomcat.util.net.NioSelectorPool.getSharedSelector Using a shared selector for servlet write/read
12-Apr-2018 14:58:27.028 INFO [main] org.apache.coyote.AbstractProtocol.init Initializing ProtocolHandler ["ajp-nio-8009"]
12-Apr-2018 14:58:27.030 INFO [main] org.apache.tomcat.util.net.NioSelectorPool.getSharedSelector Using a shared selector for servlet write/read
12-Apr-2018 14:58:27.031 INFO [main] org.apache.catalina.startup.Catalina.load Initialization processed in 1530 ms
12-Apr-2018 14:58:27.251 INFO [main] org.apache.catalina.core.StandardService.startInternal Starting service [Catalina]
12-Apr-2018 14:58:27.253 INFO [main] org.apache.catalina.core.StandardEngine.startInternal Starting Servlet Engine: Apache Tomcat/8.5.29
12-Apr-2018 14:58:27.350 INFO [localhost-startStop-1] org.apache.catalina.startup.HostConfig.deployWAR Deploying web application archive [C:\Program Files\Apache Software Foundation\Tomcat 8.5\webapps\rifServices.war]
12-Apr-2018 14:58:44.273 INFO [localhost-startStop-1] org.apache.jasper.servlet.TldScanner.scanJars At least one JAR was scanned for TLDs yet contained no TLDs. Enable debug logging for this logger for a complete list of JARs that were scanned but no TLDs were found in them. Skipping unneeded JARs during scanning can improve startup time and JSP compilation time.
12-Apr-2018 14:58:44.367 INFO [localhost-startStop-1] com.sun.jersey.server.impl.container.servlet.JerseyServletContainerInitializer.addServletWithApplication Registering the Jersey servlet application, named rifServices.restfulWebServices.ms.MSSQLRIFStudyResultRetrievalWebServiceApplication, at the servlet mapping, /studyResultRetrieval/ms/*, with the Application class of the same name
12-Apr-2018 14:58:44.368 INFO [localhost-startStop-1] com.sun.jersey.server.impl.container.servlet.JerseyServletContainerInitializer.addServletWithApplication Registering the Jersey servlet application, named rifServices.restfulWebServices.pg.PGSQLRIFStudySubmissionWebServiceApplication, at the servlet mapping, /studySubmission/pg/*, with the Application class of the same name
12-Apr-2018 14:58:44.369 INFO [localhost-startStop-1] com.sun.jersey.server.impl.container.servlet.JerseyServletContainerInitializer.addServletWithApplication Registering the Jersey servlet application, named rifServices.restfulWebServices.ms.MSSQLRIFStudySubmissionWebServiceApplication, at the servlet mapping, /studySubmission/ms/*, with the Application class of the same name
12-Apr-2018 14:58:44.370 INFO [localhost-startStop-1] com.sun.jersey.server.impl.container.servlet.JerseyServletContainerInitializer.addServletWithApplication Registering the Jersey servlet application, named rifServices.restfulWebServices.pg.PGSQLRIFStudyResultRetrievalWebServiceApplication, at the servlet mapping, /studyResultRetrieval/pg/*, with the Application class of the same name
12-Apr-2018 14:58:44.391 INFO [localhost-startStop-1] com.sun.jersey.api.core.PackagesResourceConfig.init Scanning for root resource and provider classes in the packages:
  PGSQLRIFStudySubmissionWebServiceResource
  PGSQLRIFStudyResultRetrievalWebServiceResource
  MSSQLRIFStudySubmissionWebServiceResource
  MSSQLRIFStudyResultRetrievalWebServiceResource
12-Apr-2018 14:58:44.522 INFO [localhost-startStop-1] com.sun.jersey.server.impl.application.WebApplicationImpl._initiate Initiating Jersey application, version 'Jersey: 1.19 02/11/2015 03:25 AM'
12-Apr-2018 14:58:44.926 SEVERE [localhost-startStop-1] com.sun.jersey.server.impl.application.RootResourceUriRules.<init> The ResourceConfig instance does not contain any root resource classes.
12-Apr-2018 14:58:45.186 INFO [localhost-startStop-1] org.apache.catalina.startup.HostConfig.deployWAR Deployment of web application archive [C:\Program Files\Apache Software Foundation\Tomcat 8.5\webapps\rifServices.war] has finished in [17,836] ms
12-Apr-2018 14:58:45.226 INFO [localhost-startStop-1] org.apache.catalina.startup.HostConfig.deployWAR Deploying web application archive [C:\Program Files\Apache Software Foundation\Tomcat 8.5\webapps\taxonomyServices.war]
12-Apr-2018 14:58:47.470 INFO [localhost-startStop-1] org.apache.jasper.servlet.TldScanner.scanJars At least one JAR was scanned for TLDs yet contained no TLDs. Enable debug logging for this logger for a complete list of JARs that were scanned but no TLDs were found in them. Skipping unneeded JARs during scanning can improve startup time and JSP compilation time.
12-Apr-2018 14:58:47.478 INFO [localhost-startStop-1] com.sun.jersey.server.impl.container.servlet.JerseyServletContainerInitializer.addServletWithApplication Registering the Jersey servlet application, named taxonomyServices.RIFTaxonomyWebServiceApplication, at the servlet mapping, /taxonomyServices/*, with the Application class of the same name
12-Apr-2018 14:58:47.494 INFO [localhost-startStop-1] com.sun.jersey.api.core.servlet.WebAppResourceConfig.init Scanning for root resource and provider classes in the Web app resource paths:
  /WEB-INF/lib
  /WEB-INF/classes
12-Apr-2018 14:58:47.851 INFO [localhost-startStop-1] org.apache.catalina.startup.HostConfig.deployWAR Deployment of web application archive [C:\Program Files\Apache Software Foundation\Tomcat 8.5\webapps\taxonomyServices.war] has finished in [2,625] ms
12-Apr-2018 14:58:47.858 INFO [localhost-startStop-1] org.apache.catalina.startup.HostConfig.deployDirectory Deploying web application directory [C:\Program Files\Apache Software Foundation\Tomcat 8.5\webapps\ROOT]
12-Apr-2018 14:58:47.983 INFO [localhost-startStop-1] org.apache.catalina.startup.HostConfig.deployDirectory Deployment of web application directory [C:\Program Files\Apache Software Foundation\Tomcat 8.5\webapps\ROOT] has finished in [126] ms
12-Apr-2018 14:58:47.996 INFO [main] org.apache.coyote.AbstractProtocol.start Starting ProtocolHandler ["http-nio-8080"]
12-Apr-2018 14:58:48.025 INFO [main] org.apache.coyote.AbstractProtocol.start Starting ProtocolHandler ["ajp-nio-8009"]
12-Apr-2018 14:58:48.032 INFO [main] org.apache.catalina.startup.Catalina.start Server startup in 21000 ms
```


Two scripts (in the scripts directory) are provided to start and stop the RIF from the command line:

* start_rif.bat
* stop_rif.bat

These should be copied to a local directory (e.g. *%CATALINA_HOME%\bin*) and then sent to the desktop as a shortcut; find each file, right click, "select send to" then "Desktop (create shortcut)".
The shortcuts created then need to be modified to run as an Administrator (right click on shortcut, select properties, in shortcut properties window select advanced then check run as administrator).

  ![Make a shortcut run as an administrator]({{ site.baseurl }}/rifWebApplication/setting_runas_administrator.png)

When running Tomcat at the command line on Windows 10 the new Unix like copy paste functionality will prevent the buffer from scrolling and thence cause Tomcat to hang. This can be alleviated by typing `<enter>` or
`<return>` in the log window and fixed by changing the properties of the log window (right click on Tomcat in the top left corner of the Java logging window,
select properties; In options unset "quick edit mode", "insert mode", "filter clipboard contents on paste" and "enable line wrapping selection"):

  ![Windows 10 Tomcat console window properties]({{ site.baseurl }}/rifWebApplication/tomcat_console_properties.png)

Tomcat can be stopped using "control-C" if R has not been run or using `stop_rif.bat`.

* When further instructions tell you to stop and start Tomcat you will need to use the configure Tomcat application (Tomcat8w) or the services panel

### Middleware Logging (Log4j2) Introduction

This section introduces RIF logging. You do not need to do anything!

The RIF middleware now uses Log4j version 2 for logging. The configuration file `log4j2.xml` (example in:
*%CATALINA_HOME%\webapps\rifServices\WEB-INF\classes\log4j2.xml*) sets up five loggers:

  1. The Tomcat logger: `org.apache.catalina.core.ContainerBase.[Catalina].[localhost]` used by the middleware: tomcat.log
  2. The middleware logger: `rifGenericLibrary.util.RIFLogger` used by the *rifServices* middleware: `RIF_middleware.log`
  3. The taxonomy services logger: `rifGenericLibrary.util.TaxonomyLogger` used by the *taxonomyservices* middleware: `TaxonomyLogger.log`
  4. The front end (RIF web application) logger: `rifGenericLibrary.util.FrontEndLogger` used by the *rifServices* front end logger: `FrontEndLogger.log`
  5. "Other" for all other logger output not the above: `Other.log`

Log4j2 was chosen because it is easy to integrate with Tomcat; it is however old and does not always rotate the logs well.

Logs go to STDOUT and `%CATALINA_HOME%/log4j2/<YYYY>-<MM>/<Log name>.<YYYY>-<MM>-<DD>-<N>.log`, where:

* `<Log name>` is one of: Tomcat, *RIF_middleware", "TaxonomyLogger*, *FrontEndLogger*;
* `<YYYY>` is the year;
* `<MM>` is the numeric month;
* `<DD>` is the numeric day and;
* `<N>` is the log sequence number.

Log4j has a bug in it where if more than one service logs to the same log source the log files will not rotate. Therefore if you have a RIF without (13/4/2018)
a separate *TaxonomyLogger* you must upgrade `log4j2.xml` to add the TaxonomyLogger.

Log4j also tends to change the date one day in arrears; i..e it will start on the correct day and then be one day behind. Changing to SL4J, which is a much
more modern logger would probably fix these issues but Tomcat is built with log4j. This means to use log4j a custom Tomcat would be required with all the
support difficulties this would entail.

Other messages go to the console. RIF middleware message **DO NOT** go to the console so we can find messages not using `rifGenericLibrary.util.RIFLogger`. You can change this.

Logs are rotated everyday or every 100 MB in the year/month specific directory. Note the roll-over occurs with the first message received after the time/size condition is met (i.e. don't expect it to occur at midnight)

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

As an example a stripped down RIF middleware only configuration file (**DO NOT USE!**):

```
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
		<TimeBasedTriggeringPolicy interval="1" modulate="true" />              <!-- Rotated everyday -->
		<SizeBasedTriggeringPolicy size="100 MB"/> <!-- Or every 100 MB -->
	  </Policies>
    </RollingFile>
    <RollingFile name="Other"
				 filePattern="${sys:catalina.base}/log4j2/$${date:yyyy-MM}/${other}-%d{yyyy-MM-dd}-%i.log"
				 immediateFlush="false" bufferedIO="true" bufferSize="1024">
      <PatternLayout pattern="${other_log_pattern}"/>
	  <Policies>
		<TimeBasedTriggeringPolicy interval="1" modulate="true" />              <!-- Rotated everyday -->
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

   * `%CATALINA_HOME%\conf\RIFLogger.properties`
   * `%CATALINA_HOME%\webapps\rifServices\WEB-INF\classes\RIFLogger.properties`

The source is in: rapidInquiryFacility\rifServices\src\main\resources\RIFLogger.properties*. Note that most database later classes have Postgres and SQLServer versions so have two entries.

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

   * `%CATALINA_HOME%\conf\AbstractSQLManager.properties`
   * `%CATALINA_HOME%\webapps\rifServices\WEB-INF\classes\AbstractSQLManager.properties`

The source is in: `rapidInquiryFacility\rifGenericLibrary\src\main\resources\AbstractSQLManager.properties`

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

### Tomcat Logging (Log4j2) Setup

This uses the log4j JDK Logging Adapter. The JDK Logging Adapter is a custom implementation of
java.util.logging.LogManager that uses Log4j.

The configuration file is in *%CATALINA_HOME%/conf/log4j2.xml*. This configuration file completely replaces the
configuration in the previous section (which is a subset).

* The RIF Tomcat logging configuration file must be placed in: *%CATALINA_HOME%\comf\log4j2.xml*.
  An example is found in: *%CATALINA_HOME%\webapps\rifServices\WEB-INF\classes\log4j2.xml*.
  The source is in: *rapidInquiryFacility\rifServices\src\main\resources\log4j2.xml*

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

* **To send  RIF output to the console uncomment the following line in *log4j2.xml*:
  ```<!-- <AppenderRef ref="CONSOLE"/> uncomment to see RIF middleware output on the console -->```**.

* Create an environment overrides file for catalina.bat as %CATALINA_HOME%\bin\setenv.bat. A copy is provided in:
  %CATALINA_HOME%\webapps\rifServices\WEB-INF\classes. Copy this file to *%CATALINA_HOME%\bin*.

```bat
	REM Tomcat log4j2 setup
	REM
	REM Add this script to %CATALINA_HOME%\bin
	REM
	REM A copy of this script is provided in %CATALINA_HOME%\webapps\rifServices\WEB-INF\classes\
	REM
	REM Do not set LOGGING_MANAGER to jul, Tomcat will NOT sart
	REM set LOGGING_MANAGER=org.apache.logging.log4j.jul.LogManager
	REM
	REM To enable Jconsole add %ENABLE_JMX% to CATALINA_OPTS. Set to run on port 9999 and only allow connections from localhost
	REM
	set ENABLE_JMX=-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9999 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=localhost
	set CATALINA_OPTS=-Djava.util.logging.manager=org.apache.logging.log4j.jul.LogManager -Dlog4j.configurationFile="%CATALINA_HOME%\conf\log4j2.xml"

	REM
	REM Add -Dlog4j2.debug=true if Tomcat exceptions/does not start
	REM (catalina.bat run is useful if no output)
	REM
	REM Default CLASSPATH; no need to be added
	REM set CLASSPATH=%CATALINA_HOME%\bin\bootstrap.jar;%CATALINA_HOME%\bin\tomcat-juli.jar
	REM
	REM Added JUL and Log4j2 to Tomcat CLASSAPATH
	set CLASSPATH=%CATALINA_HOME%\lib\log4j-core-2.9.0.jar;%CATALINA_HOME%\lib\log4j-api-2.9.0.jar;%CATALINA_HOME%\lib\log4j-jul-2.9.0.jar
	REM
	REM Do not do this, use CATALINA_OPTS instead. This will work on Linux
	REM
	REM set LOGGING_CONFIG="-Dlog4j.configurationFile=%CATALINA_HOME%\conf\log4j2.xml"
	REM
	REM EOf
```

* Add the following files to *%CATALINA_HOME%\lib*:
  * log4j-api-2.9.0.jar
  * log4j-core-2.9.0.jar
  * log4j-jul-2.9.0.jar

  If you use Maven to build the Middleware, these files are in subdirectories below
  `%USER%\.m2\repository\org\apache\logging\log4j\<log4j module>\2.9.0` where `<log4j module>` is log4j-api etc.

  SAHSU will normally supply these JAR files (in the log4j directory) together with the war files (in the Tomcat webapps directory).

  Do NOT set the enviroment variables LOGGING_MANAGER or LOGGING_CONFIG.
  This script sets *CATALINA_OPTS* and *CLASSPATH* in the Tomcat environment*:
  ```
  CATALINA_OPTS=-Djava.util.logging.manager=org.apache.logging.log4j.jul.LogManager -Dlog4j.configurationFile="%CATALINA_HOME%\conf\log4j2.xml"
  CLASSPATH=%CATALINA_HOME%\lib\log4j-core-2.9.0.jar;%CATALINA_HOME%\lib\log4j-api-2.9.0.jar;%CATALINA_HOME%\lib\log4j-jul-2.9.CONSOLE0.jar
  ```

* Restart Tomcat using the configure Tomcat application (Tomcat8w) or the services panel.
  The Tomcat output trace will appear in %CATALINA_HOME%/logs as:
  *tomcat8-stderr.<date in format YYYY-MM-DD>* and also possibly *tomcat8-stdout.<date in format YYYY-MM-DD>*.

Debugging logging faults:

* Adding  -Dlog4j2.debug=true to the CATALINA_OPTS environment variable if Tomcat exceptions/does not start
* Use `catalina.bat run` if there is no output from te script and the Java windows disappears immediately
* Set the configuration status to **debug**

### Tomcat Logging File (Log4j2.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="debug" monitorInterval="30" name="RIF Tomcat Default">
<!--
  The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU
  that rapidly addresses epidemiological and public health questions using
  routinely collected health and population data and generates standardised
  rates and relative risks for any given health outcome, for specified age
  and year ranges, for any given geographical area.

  Copyright 2014 Imperial College London, developed by the Small Area
  Health Statistics Unit. The work of the Small Area Health Statistics Unit
  is funded by the Public Health England as part of the MRC-PHE Centre for
  Environment and Health. Funding for this project has also been received
  from the United States Centers for Disease Control and Prevention.

  This file is part of the Rapid Inquiry Facility (RIF) project.
  RIF is free software: you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  RIF is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with RIF. If not, see <http://www.gnu.org/licenses/>; or write
  to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
  Boston, MA 02110-1301 USA

  Default log4j2 setup for the RIF middleware.

  The RIF middleware now uses Log4j version 2 for logging. The configuration file:
  *%CATALINA_HOME%\webapps\rifServices\WEB-INF\classes\log4j2.xml* sets up five loggers:

  1. The Tomcat logger: *org.apache.catalina.core.ContainerBase.[Catalina].[localhost]* used by the middleware: tomcat.log
  2. The middleware logger: *rifGenericLibrary.util.RIFLogger* used by the middleware: RIF_middleware.log
  3. The taxonomy services logger: *rifGenericLibrary.util.TaxonomyLogger* used by the middleware: TaxonomyLogger.log
  4. The front end (RIF web application) logger: *rifGenericLibrary.util.FrontEndLogger* used by the middleware: FrontEndLogger.log
  5. "Other" for all other logger output not the above: Other.log

  Log4j2 was chosen because it is easy to integrate with Tomcat; it is however old and does not always rotate the logs well.

  Logs go to STDOUT and ```%CATALINA_HOME%/log4j2/<YYYY>-<MM>/<Log name>.<YYYY>-<MM>-<DD>-<N>.log```; where:

  * ```<Log name>``` is one of: Tomcat, *RIF_middleware", "TaxonomyLogger*, *FrontEndLogger*;
  * ```<YYYY>``` is the year;
  * ```<MM>``` is the numeric month;
  * ```<DD>``` is the numeric day and;
  * ```<N>``` is the log sequence number.

  Other messages go to the console. RIF middleware message **DO NOT** go to the console so we can find
  messages not using *rifGenericLibrary.util.RIFLogger*. You can change this.

  Logs are rotated everyday or every 100 MB in the year/month specific directory. Note the rollover occrus with the first
  message received after the time/size condition is met (i.e. don't expect it to occur at midnight)

  Logs go to STDOUT and ${sys:catalina.base}/log4j2/<YYYY>-<MM>/ and %CATALINA_HOME/log4j2/<YYYY>-<MM>/
  Other messages go to the console. RIF middleware message DO NOT go to the console so we can find
  messages not using rifGenericLibrary.util.RIFLogger

  Logs are rotated everyday or every 100 MB in the year/month specific directory

  Typical log entry:

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

  Author: Peter Hambly; 12/9/2017
  -->
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
		<TimeBasedTriggeringPolicy interval="1" modulate="true"/>              <!-- Rotated everyday -->
		<SizeBasedTriggeringPolicy size="100 MB"/> <!-- Or every 100 MB -->
	  </Policies>
    </RollingFile>
    <RollingFile name="FRONTENDLOGGER"
				 filePattern="${logdir}/FrontEndLogger.%d{yyyy-MM-dd}-%i.log"
				 immediateFlush="true" bufferedIO="true" bufferSize="1024">
      <PatternLayout pattern="${rif_log_pattern}"/>
	  <Policies>
		<TimeBasedTriggeringPolicy interval="1" modulate="true"/>              <!-- Rotated everyday -->
		<SizeBasedTriggeringPolicy size="100 MB"/> <!-- Or every 100 MB -->
	  </Policies>
    </RollingFile>
    <RollingFile name="TAXONOMYLOGGER"
				 filePattern="${logdir}/TaxonomyLogger.%d{yyyy-MM-dd}-%i.log"
				 immediateFlush="true" bufferedIO="true" bufferSize="1024">
      <PatternLayout pattern="${rif_log_pattern}"/>
	  <Policies>
		<TimeBasedTriggeringPolicy interval="1" modulate="true"/>              <!-- Rotated everyday -->
		<SizeBasedTriggeringPolicy size="100 MB"/> <!-- Or every 100 MB -->
	  </Policies>
	</RollingFile>
    <RollingFile name="STATISTICSLOGGER"
				 filePattern="${logdir}/StatisticsLogger.%d{yyyy-MM-dd}-%i.log"
				 immediateFlush="true" bufferedIO="true" bufferSize="1024">
      <PatternLayout pattern="${rif_log_pattern}"/>
	  <Policies>
		<TimeBasedTriggeringPolicy interval="1" modulate="true"/>              <!-- Rotated everyday -->
		<SizeBasedTriggeringPolicy size="100 MB"/> <!-- Or every 100 MB -->
	  </Policies>
    </RollingFile>
    <RollingFile name="OTHER"
				 filePattern="${logdir}/Other.%d{yyyy-MM-dd}-%i.log"
				 immediateFlush="true" bufferedIO="true" bufferSize="1024">
      <PatternLayout pattern="${other_log_pattern}"/>
	  <Policies>
		<TimeBasedTriggeringPolicy interval="1" modulate="true"/>              <!-- Rotated everyday -->
		<SizeBasedTriggeringPolicy size="100 MB"/> <!-- Or every 100 MB -->
	  </Policies>
    </RollingFile>
    <RollingFile name="TOMCAT"
				 filePattern="${logdir}/tomcat.%d{yyyy-MM-dd}-%i.log"
				 immediateFlush="true" bufferedIO="true" bufferSize="1024">
      <PatternLayout pattern="${other_log_pattern}"/>
      <Policies>
		<TimeBasedTriggeringPolicy interval="1" modulate="true"/>              <!-- Rotated everyday -->
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

	<!-- Tomcat logging -->
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
      <!-- RIF FRont End logger: rifGenericLibrary.util.FrontEndLogger -->
    <Logger name="rifGenericLibrary.util.FrontEndLogger"
		level="info" additivity="false"> <!-- Chnage to debug for more output -->
      <!-- <AppenderRef ref="CONSOLE"/> uncomment to see RIF Front End console logging on the Tomcat console -->
      <AppenderRef ref="FRONTENDLOGGER"/>
    </Logger>
      <!-- RIF FRont End logger: org.sahsu.rif.generic.util.TaxonomyLogger -->
    <Logger name="org.sahsu.rif.generic.util.TaxonomyLogger"
		level="info" additivity="false"> <!-- Change to debug for more output -->
      <!-- <AppenderRef ref="CONSOLE"/> uncomment to see RIF Front End console logging on the Tomcat console -->
      <AppenderRef ref="TAXONOMYLOGGER"/>
    </Logger>
      <!-- RIF FRont End logger: org.sahsu.rif.generic.util.StatisticsLogger -->
    <Logger name="org.sahsu.rif.generic.util.StatisticsLogger"
		level="info" additivity="false"> <!-- Change to debug for more output -->
      <!-- <AppenderRef ref="CONSOLE"/> uncomment to see RIF Front End console logging on the Tomcat console -->
      <AppenderRef ref="STATISTICSLOGGER"/>
    </Logger>

  </Loggers>
</Configuration>
```

## R

See [Setup R](#setup-r)


# Building Web Services using Maven

Normally users will be supplied with pre built files in the *Tomcat webapps* folder:

* RIF middleware: `rifServices.war`
* Statistics service: `statistics.war`
* Taxonomy service (ICD9, ICD10, possibly others): `taxonomy.war`
* Front end: `RIF40.war`

## Building Using Make

If you have installed make (i.e. you are building the Postgres port from Scratch), run `make` from the
root of the github repository, e.g. *C:\Users\Peter\Documents\GitHub\rapidInquiryFacility*

The following make targets are provided:

* *clean*: remove targets, clean maven build areas
* *all*: build targets
* *install*: clean then all
* *rifservice*: build rifServices.war target
* *taxonomyService*: build taxonomy.war target
* *RIF40*: build RIF40.war target

To run a make target type *make <target>;e.g. *make install*.

The following files are then built and copied into the rapidInquiryFacility directory:
*taxonomy.war*, *rifServices.war*, *RIF40.war*, *statistics.war*

## Building Using a Windows Batch File

Run *java_build.bat* from the root of the github repository,
e.g. *C:\Users\Peter\Documents\GitHub\rapidInquiryFacility*. The files *taxonomy.war*,
*rifServices.war*, *RIF40.war* are the end product.

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

## Building By Hand

Otherwise run the following commands by hand from the
root of the github repository, e.g. *C:\Users\Peter\Documents\GitHub\rapidInquiryFacility*:

```
mvn --version
mvn clean
mvn install
```

This method also builds the *taxonomy.war* or the web application *RIF40.war* file.

Or, using subdirectries for *rifServices* only:

```
mvn --version
cd rifGenericLibrary
mvn clean
mvn install
cd ..\rapidInquiryFacility
mvn clean
mvn install
cd ..\rifServices
mvn clean
mvn install
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
cd rifGenericLibrary; mvn install
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

The order is important when building using the sub-directories; the directories must be built in the order:
*rifGenericLibrary*, *rapidInquiryFacility*, *rifServices*. It is always assumed you build *RIF40.war and
*taxonomy.war* later. If you get a build failure try a *mvn clean* in each directory first;
then retry with a *mvn install*.

# Installing Web Services in Tomcat

## Web Services

### RIF Services

* Copy *rifServices.war* from: *rapidInquiryFacility\rifServices\target*, e.g. *C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifServices\target*
  to: *%CATALINA_HOME%\webapps*, e.g. *C:\Program Files\Apache Software Foundation\Tomcat 8.5\webapps*

You do NOT need to do anything else (other than copy the file) if you are using Postgres without TLS (i.e. on a laptop).

The *RIFServiceStartupProperties.properties* file contains the commented out parameter *taxonomyServicesServer*.
This is the network location of the taxonomy services server, and is to be used when:

* The taxonomy services is not running on the same server as rifServices
* HTTPS is used

If *taxonomyServicesServer* is set to: *https://localhost:8080* as suggested then host validation is disabled;
otherwise you must set up JAVA TLS host verification with fully signed certificates; typical errors include:

* *No name matching a.b.com found*
  ```java.security.cert.CertificateException: No name matching a.b.com found```
  **This means you need create a correctly signed certificate and add to the keystore**
* *Unable to find valid certification path to requested target*
  ```
  javax.net.ssl.SSLHandshakeException: sun.security.validator.ValidatorException: PKIX path building
  failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification
  path to requested target
  ```
  **This means there is a certificate but it is not correctly signed**

You can also typically get meessages that Java cannot find the keystore and/or TLS is not setup
correctly.

See: (http://java.globinch.com/enterprise-java/security/pkix-path-building-failed-validation-sun-security-validatorexception/)
For most purposes; localhost will do fine; as long as Tomcat is setup to run on localhost

RIF services uses Taxonomy services directly a) when creating study JSON from the database using
"Save completed study" and b) when creating the same file for the export ZIP file.

This is code in `...rapidInquiryFacility\rifServices\src\main\java\rifServices\dataStorageLayer\common\GetStudyJSON.java`

### Taxonomy Services

For a full ICD10 listing add the following SAHSU supplied files (in *Taxonomy services configuration files*) to:
*%CATALINA_HOME%\conf* and restart Tomcat

  * icdClaML2016ens.xml
  * TaxonomyServicesConfiguration.xml
  * ClaML.dtd

See the: [Taxonomy Services]({{ site.baseurl }}/taxonomyServices/Taxonomy-Services)
manual.

### The Statistics Service

This runs in the background providing R language services. It should not change often or need much attention.

Copy `statistics.war` from `rapidInquiryFacility/statsService/target` to `CATALINA_HOME/webapps`. For example, in Windows:

```
copy C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifServices\target C:\Program Files\Apache Software Foundation\Tomcat 8.5\webapps
```

Note that if you _do_ have to redeploy this WAR file, you will have to stop and restart the Tomcat server. It cannot be hot deployed because of the way it connects to the R native library.

## RIF Web Application

Normally method 2 is used.

### Method 1: manual

Create RIF40 in web-apps:

* Change directory to *%CATALINA_HOME%\webapps*; e,g, *cd "C:\Program Files\Apache Software Foundation\Tomcat 8.5\webapps"*
* Create the directory *RIF40*
* Copy all files and directories from the directory: *"C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifWebApplication\src\main\webapp\WEB-INF"*
  to *C:\Program Files\Apache Software Foundation\Tomcat 8.5\webapps\RIF40*

### Method 2: Using pre-supplied RIF40.war (in the *Tomcat webapps* folder)

* *RIF40.war* needs to be copied to: *%CATALINA_HOME%\webapps\RIF40.war*
* Once you have started Tomcat check that the *%CATALINA_HOME%\webapps\RIF* contains files:
  ```
	C:\Program Files\Apache Software Foundation\Tomcat 8.5>cd %CATALINA_HOME%\webapps\RIF40

	C:\Program Files\Apache Software Foundation\Tomcat 8.5\webapps\RIF40>dir
	 Volume in drive C is OS
	 Volume Serial Number is 76AD-DC24

	 Directory of C:\Program Files\Apache Software Foundation\Tomcat 8.5\webapps\RIF40

	13/04/2018  08:37    <DIR>          .
	13/04/2018  08:37    <DIR>          ..
	27/03/2018  21:03    <DIR>          backend
	27/03/2018  21:03    <DIR>          css
	27/03/2018  21:03    <DIR>          dashboards
	27/03/2018  21:03             5,430 favicon.ico
	27/03/2018  21:03    <DIR>          images
	11/04/2018  14:54            12,034 index.html
	11/04/2018  14:54    <DIR>          libs
	27/03/2018  21:03    <DIR>          modules
	27/03/2018  21:03               283 todo.js
	27/03/2018  21:03    <DIR>          utils
				   3 File(s)         17,747 bytes
				   9 Dir(s)  295,976,181,760 bytes free
  ```

**BEFORE YOU RUN THE RIF YOU MUST SETUP THE DATABASE AND NETWORKING IN TOMCAT FIRST**. See the next section.

Running the RIF and logging on is detailed in section 5. You must restart Tomcat to create RIF40 for
the first time, it is now automatically updated (after 20/5/2018) using the *RIF40.war* file.

# RIF Setup

## Setup Database

The Java connector for the RifServices middleware is setup in the file: *%CATALINA_HOME%\webapps\rifServices\WEB-INF\classes\RIFServiceStartupProperties.properties*.
This should be copied to *%CATALINA_HOME%\conf* so it is not overwritten by middleware upgrades.

If you are running on a laptop and using Postgres you only need to copy the file.

* If the folder rifServices does not exist; start Tomcat and it will be expanded from the war file.
* The default database is setup as follows:
  * type (key database.databaseType) is Postgres. You will need to comment out the Postgres setting and use the SQL Server examples for SQL Server;
  * name (key database.databaseName) is *sahsuland*;
  * Port (key database.port) is *5432*;
  * host (key database.host) is *localhost* for Postgres. Normally Tomcat is installed on the same server as the database; if this is not the case Postgres and the
    firewalls will need to be setup correctly, see [Postgres Client Authentication](https://www.postgresql.org/docs/9.6/auth-methods.html).
	This usually requires skilled database and network administrators. The SQL Server host will be the same as the SQLCMDSERVER variable;

    Do not set up the database not network access or open the firewall ports unless this is required; it is secure on *localhost*! The database
    can be remote but users must take care to ensure that it is setup securely. If you use a remote database, users are advised the secure the database:

    * Always use TLS.
    * Restrict access using **BOTH** the database software (*hba.conf* in Postgres) and the network infrastructure
    * Keep the database fully patched as per vendor advice.
    * Follow the appropriate guidelines, e.g. OWASP, but be consult SAHSU as some of the changes may break the RIF:
      - [OWASP Postgres guidelines](https://www.owasp.org/index.php/OWASP_Backend_Security_Project_PostgreSQL_Hardening)
      - [OWASP SQL Server guidelines](https://www.owasp.org/index.php/OWASP_Backend_Security_Project_SQLServer_Hardening)

### SQL Server

```java
#SQL SERVER
database.driverClassName=com.microsoft.sqlserver.jdbc.SQLServerDriver
database.jdbcDriverPrefix=jdbc:sqlserver
database.host=localhost\\SQLEXPRESS
database.port=1433
database.databaseName=sahsuland
database.databaseType=sqlServer

#
# Set the ODBC data source for SQL Server only. Postgres uses JDBC
odbcDataSourceName=SQLServer13

```

### Postgres

These are the defaults as supplied in the WAR file:

```java
#POSTGRES
database.driverClassName=org.postgresql.Driver
database.jdbcDriverPrefix=jdbc:postgresql
database.host=localhost
database.port=5432
database.databaseName=sahsuland
database.databaseType=postgresql

#
# Set the ODBC data source for SQL Server only. Postgres uses JDBC
#odbcDataSourceName=SQLServer13
```

**BEWARE** Make sure you keep a copy of this file; a RIF services upgrade will overwrite it.

## Setup Network

This section not required is you are running on localhost (e.g. a laptop).

By default Tomcat runs on port 8080, if you have installed the Apache webserver (Postgres installs can) then it will appear on port 8081. This can be
detected using the ```netstat``` command (the syntax will be slightly differ on Linux):

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

The RIF web application file RIF40\backend\services\rifs-back-urls.js (e.g.
C:\Program Files\Apache Software Foundation\Tomcat 8.5\webapps\RIF40\backend\services\rifs-back-urls.js)
define the URLs for the services.

```javascript
/*
 * SERVICE for URL middleware calls.
 *
 * Rewritten to remove the need for hard coding	HTTPS, hostname etc
 * and access via servicesConfig.studyResultRetrievalURL etc.
 */
angular.module("RIF")
        .factory('servicesConfig', [
            function() {
				var serviceHost=window.location.protocol + "//" + window.location.hostname + ":" + window.location.port;
				return {
					studySubmissionURL: serviceHost + "/rifServices/studySubmission/",
					studyResultRetrievalURL: serviceHost + "/rifServices/studyResultRetrieval/",
					taxonomyServicesURL: serviceHost + "/taxonomies/service/"
				}
/*

Use the hardcoded  version, e.g. if not using the web protocol of the current page and hostname; and/or port 8080
Localhost can be edited here
				return {
					studySubmissionURL: "https://localhost:8080/rifServices/studySubmission/",
					studyResultRetrievalURL: "https://localhost:8080/rifServices/studyResultRetrieval/",
					taxonomyServicesURL: "https://localhost:8080/rifServices/taxonomyServices/"
				}
 */
		}]);
```

Usually the script is able to detect protocol, port and hostname; so does not need to be changed. If it doesn't or you are installing into an unusual environment,
use the hardcoded version and edit:

* The port number in use; e.g. 8080 as in the above example or 8443 if you are in a production environment with TLS enabled;
* The server for the remote service, e.g. *https://aepw-rif27.sm.med.ic.ac.uk*

If you do this, **BEWARE** Make sure you keep a copy of this file; any front end RIF web application upgrade will overwrite it.

Running the RIF and logging on is detailed in section 5.

### TLS

TLS or *Transport Layer Security* is required to secure the RIF in a networked environment. It is not required if you are just running locally as
a developer or on a laptop.

To install and configure SSL/TLS support on Tomcat, you need to follow these simple steps. For more information, read the rest of this HOW-TO.

Create a keystore file to store the server's private key and self-signed certificate by executing the following command in the $CATALINA_BASE/conf directory:
Windows. Do **NOT** use a password of *changeit*:
```
cd %CATALINA_HOME%\conf

"%JAVA_HOME%\bin\keytool" -genkey -alias tomcat -keyalg RSA -keystore "%CATALINA_HOME%\conf\localhost-rsa.jks" -storepass changeit

Unix:
```

$JAVA_HOME/bin/keytool -genkey -alias tomcat -keyalg RSA
```
On a Unix system the keystore will be put in ~/.keystore and needs to be copied to $CATALINA_BASE/conf/localhost-rsa.jks
```

Example output:
```
C:\Program Files\Apache Software Foundation\Tomcat 8.5\bin>"%JAVA_HOME%\bin\keytool" -genkey -alias tomcat -keyalg RSA -keystore "%CATALINA_HOME%\conf\localhost-rsa.jks" -storepass changeit
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
Warning:
The JKS keystore uses a proprietary format. It is recommended to migrate to PKCS12 which is an industry standard format using "keyto
ol -importkeystore -srckeystore C:\Program Files\Apache Software Foundation\Tomcat 8.5\conf\localhost-rsa.jks -destkeystore C:\Progr
am Files\Apache Software Foundation\Tomcat 8.5\conf\localhost-rsa.jks -deststoretype pkcs12".

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
in: (https://tomcat.apache.org/tomcat-8.5-doc/ssl-howto.html). Change the port number from 8443 to 8080; remove the original 8080 connector.
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

  ![Insecure TLS warning]({{ site.baseurl }}/rifWebApplication/insecure_connection_warning.png){:width="100%"}

To sign the certificates, follow the instructions in: (https://tomcat.apache.org/tomcat-8.5-doc/ssl-howto.html#SSL_and_Tomcat)

**Do not sign certificates if your server will be on an air gapped network such as the SAHSU private network**. The clients will be unable to verify the server certificate
with the signing authority and the connection **WILL** fail!

This setup will support:

- Android 4.4.2 and later
- Firefox 32 and later. Note that Firefox 45 does not work!
- IE 11 and later
- Microsoft Edge 38 and later
- IE Mobile 11 and later
- Java 8 b132 (March 10, 2014)
- Safari 7 and later

## Setup R

* [Setting up on Windows](/Installation/R_setup_on_Windows)
* [Setting up on Mac](/Installation/R_setup_on_Mac).

## Common Setup Errors

The RIF middleware now uses Log4j version 2 for logging. The configuration file *log4j2.xml* (example in:
*%CATALINA_HOME%\webapps\rifServices\WEB-INF\classes\log4j2.xml*) sets up five loggers:

  1. The Tomcat logger: *org.apache.catalina.core.ContainerBase.[Catalina].[localhost]* used by the middleware: tomcat.log
  2. The middleware logger: *rifGenericLibrary.util.RIFLogger* used by the *rifServices* middleware: RIF_middleware.log
  3. The taxonomy services logger: *rifGenericLibrary.util.TaxonomyLogger* used by the *taxonomyservices* middleware: TaxonomyLogger.log
  4. The front end (RIF web application) logger: *rifGenericLibrary.util.FrontEndLogger* used by the *rifServices* front end logger: FrontEndLogger.log
  5. "Other" for all other logger output not the above: Other.log

When run from catalina.bat all Tomcat output appears in the console window. When run as a service Tomcat logs to:
commons-daemon.<date e.g., 2018-04-16>.log, tomcat8-stderr.<date e.g., 2018-04-16>.log, tomcat8-stdout.<date e.g., 2018-04-16>.log instead of to the console

### Logon RIF Service Call Incorrect

Use developer mode in the browser to bring up the console log:

  ![Logon RIF Service Call Incorrect]({{ site.baseurl }}/rifWebApplication/caching_error.png){:width="100%"}

In this example the RIF web application file RIF40\backend\services\rifs-back-urls.js (e.g.
C:\Program Files\Apache Software Foundation\Tomcat 8.5\webapps\RIF40\backend\services\rifs-back-urls.js)
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
        .constant('taxonomyServicesURL', "http://localhost:8080/taxonomies/service/")
```

This is caused by *rifs-back-urls.js* being changed, Tomcat restarted and Chrome or Firefox caching the previous service call. Flush the browser cache.

Firefox console log example:

```
17:05:09.245 Error: res.data is null
loggedIn@https://localhost:8080/RIF40/backend/services/rifs-back-interceptor.js:48:69
e/<@https://localhost:8080/RIF40/libs/standalone/angular.min.js:131:20
vf/this.$get</m.prototype.$eval@https://localhost:8080/RIF40/libs/standalone/angular.min.js:145:343
vf/this.$get</m.prototype.$digest@https://localhost:8080/RIF40/libs/standalone/angular.min.js:142:412
vf/this.$get</m.prototype.$apply@https://localhost:8080/RIF40/libs/standalone/angular.min.js:146:111
l@https://localhost:8080/RIF40/libs/standalone/angular.min.js:97:320
J@https://localhost:8080/RIF40/libs/standalone/angular.min.js:102:34
gg/</e@https://localhost:8080/RIF40/libs/standalone/angular.min.js:103:55
 1 angular.min.js:118:8
	e/< https://localhost:8080/RIF40/libs/standalone/angular.min.js:118:8
	hf/this.$get</< https://localhost:8080/RIF40/libs/standalone/angular.min.js:90:220
	e/< https://localhost:8080/RIF40/libs/standalone/angular.min.js:131:103
	vf/this.$get</m.prototype.$eval https://localhost:8080/RIF40/libs/standalone/angular.min.js:145:343
	vf/this.$get</m.prototype.$digest https://localhost:8080/RIF40/libs/standalone/angular.min.js:142:412
	vf/this.$get</m.prototype.$apply https://localhost:8080/RIF40/libs/standalone/angular.min.js:146:111
	l https://localhost:8080/RIF40/libs/standalone/angular.min.js:97:320
	J https://localhost:8080/RIF40/libs/standalone/angular.min.js:102:34
	gg/</e https://localhost:8080/RIF40/libs/standalone/angular.min.js:103:55
```

### TLS Errors

TLS errors tend to be:

* Keyfile in the wrong location:
	```
	10-Apr-2017 13:45:12.240 SEVERE [main] org.apache.tomcat.util.net.SSLUtilBase.getStore Failed to load keystore type [JKS] with path [conf/localhost-rsa.jks] due to [C:\Program Files\Apache Software Foundation\Tomcat 8.5\conf\localhost-rsa.jks (The system cannot find the file specified)]
	```
* Invalid keyfile password.

### Unable to unpack war files

In this case the .war file (e.g. rifServices.war) is not unpacked and the service is not available in Tomcat. Find in error in the Tomcat stderr log and send to the development team.
This is indicative of a build problem.

* Screen shots and log will be added when this happens again!*

### No Taxonomy Services

See *3.1.2 Taxonomy Service*, and *4.4.3 Unable to unpack war files*

  ![Taxonomy Services error]({{ site.baseurl }}/rifWebApplication/taxonomy_sevice_error.png){:width="100%"}

### RIF Services crash on logon

On rifServices startup the following checks are carried out to assist tracing installation faults:

* CATALINA_HOME is set
* The R environment is setup correctly on rifServices start :
  - R_HOME in PATH
  - %R_HOME%/bin/x64 in PATH
  - %R_HOME%/library/rJava/jri/x64 in PATH

A typical failure:
```
11:22:44.667 [http-nio-8080-exec-10] ERROR rifGenericLibrary.util.RIFLogger : [rifServices.system.RIFServiceStartupOptions]:
RIFServiceStartupOptions error
getMessage:          RIFServiceException: C:\Program Files\R\R-3.4.4\library\rJava\jri\x64 not in Path/PATH
getRootCauseMessage: RIFServiceException: C:\Program Files\R\R-3.4.4\library\rJava\jri\x64 not in Path/PATH
getThrowableCount:   1
getRootCauseStackTrace >>>
rifGenericLibrary.system.RIFServiceException: C:\Program Files\R\R-3.4.4\library\rJava\jri\x64 not in Path/PATH
	at rifServices.system.RIFServiceStartupOptions.checkREnvironment(RIFServiceStartupOptions.java:544)
	at rifServices.system.RIFServiceStartupOptions.getRIFServiceResourcePath(RIFServiceStartupOptions.java:596)
	at rifServices.dataStorageLayer.ms.MSSQLHealthOutcomeManager.<init>(MSSQLHealthOutcomeManager.java:123)
	at rifServices.dataStorageLayer.ms.MSSQLRIFServiceResources.<init>(MSSQLRIFServiceResources.java:125)
	at rifServices.dataStorageLayer.ms.MSSQLRIFServiceResources.newInstance(MSSQLRIFServiceResources.java:217)
	at rifServices.dataStorageLayer.ms.MSSQLAbstractStudyServiceBundle.initialise(MSSQLAbstractStudyServiceBundle.java:104)
```

A successful start looks like:
```
10:50:09.367 [http-nio-8080-exec-3] INFO  rifGenericLibrary.util.RIFLogger : [rifServices.system.RIFServiceStartupOptions]:
RIFServiceStartupOptions is web deployment
10:50:09.367 [http-nio-8080-exec-3] INFO  rifGenericLibrary.util.RIFLogger : [rifServices.system.RIFServiceStartupOptions]:
Get CATALINA_HOME=C:\Program Files\Apache Software Foundation\Tomcat 8.5
10:50:09.368 [http-nio-8080-exec-3] INFO  rifGenericLibrary.util.RIFLogger : [rifServices.system.RIFServiceStartupOptions]:
Check R_HOME=C:\Program Files\R\R-3.4.4
10:50:09.368 [http-nio-8080-exec-3] INFO  rifGenericLibrary.util.RIFLogger : [rifServices.system.RIFServiceStartupOptions]:
Check Path/PATH for required R components:
[0] C:\Program Files (x86)\Intel\Intel(R) Management Engine Components\iCLS\;
[1] C:\Program Files\Intel\Intel(R) Management Engine Components\iCLS\;
[2] C:\Windows\system32;
[3] C:\Windows;
[4] C:\Windows\System32\Wbem;
[5] C:\Windows\System32\WindowsPowerShell\v1.0\;
[6] C:\Program Files (x86)\Intel\Intel(R) Management Engine Components\DAL;
[7] C:\Program Files\Intel\Intel(R) Management Engine Components\DAL;
[8] C:\Program Files (x86)\Intel\Intel(R) Management Engine Components\IPT;
[9] C:\Program Files\Intel\Intel(R) Management Engine Components\IPT;
[10] C:\Program Files\PostgreSQL\9.6\bin;
[11] C:\Program Files\Java\jdk1.8.0_162\bin;
[12] C:\Program Files\Apache Software Foundation\apache-maven-3.5.3\bin;
[13] C:\Program Files\Microsoft SQL Server\Client SDK\ODBC\130\Tools\Binn\;
[14] C:\Program Files (x86)\Microsoft SQL Server\130\Tools\Binn\;
[15] C:\Program Files\Microsoft SQL Server\130\Tools\Binn\;
[16] C:\Program Files\Microsoft SQL Server\130\DTS\Binn\;
[17] C:\Program Files (x86)\Microsoft SQL Server\Client SDK\ODBC\130\Tools\Binn\;
[18] C:\Program Files (x86)\Microsoft SQL Server\140\Tools\Binn\;
[19] C:\Program Files (x86)\Microsoft SQL Server\140\DTS\Binn\;
[20] C:\Program Files (x86)\Microsoft SQL Server\140\Tools\Binn\ManagementStudio\;
[21] C:\Program Files\nodejs\;
[22] C:\Program Files\dotnet\;
[23] C:\MinGW\msys\1.0\bin;
[24] C:\Program Files\Apache Software Foundation\Tomcat 8.5\bin;
R bin [25] C:\Program Files\R\R-3.4.4\bin\x64;
JRI [26] C:\Program Files\R\R-3.4.4\library\rJava\jri\x64;
[27] C:\Program Files\MiKTeX 2.9\miktex\bin\x64\;
[28] C:\Python27;
[29] C:\Users\admin\AppData\Local\Microsoft\WindowsApps;

10:50:09.368 [http-nio-8080-exec-3] INFO  rifGenericLibrary.util.RIFLogger : [rifServices.system.RIFServiceStartupOptions]:
Print java.library.path:
[0] C:\Program Files\Java\jdk1.8.0_162\bin;
[1] C:\Windows\Sun\Java\bin;
[2] C:\Windows\system32;
[3] C:\Windows;
[4] C:\Program Files (x86)\Intel\Intel(R) Management Engine Components\iCLS\;
[5] C:\Program Files\Intel\Intel(R) Management Engine Components\iCLS\;
[6] C:\Windows\system32;
[7] C:\Windows;
[8] C:\Windows\System32\Wbem;
[9] C:\Windows\System32\WindowsPowerShell\v1.0\;
[10] C:\Program Files (x86)\Intel\Intel(R) Management Engine Components\DAL;
[11] C:\Program Files\Intel\Intel(R) Management Engine Components\DAL;
[12] C:\Program Files (x86)\Intel\Intel(R) Management Engine Components\IPT;
[13] C:\Program Files\Intel\Intel(R) Management Engine Components\IPT;
[14] C:\Program Files\PostgreSQL\9.6\bin;
[15] C:\Program Files\Java\jdk1.8.0_162\bin;
[16] C:\Program Files\Apache Software Foundation\apache-maven-3.5.3\bin;
[17] C:\Program Files\Microsoft SQL Server\Client SDK\ODBC\130\Tools\Binn\;
[18] C:\Program Files (x86)\Microsoft SQL Server\130\Tools\Binn\;
[19] C:\Program Files\Microsoft SQL Server\130\Tools\Binn\;
[20] C:\Program Files\Microsoft SQL Server\130\DTS\Binn\;
[21] C:\Program Files (x86)\Microsoft SQL Server\Client SDK\ODBC\130\Tools\Binn\;
[22] C:\Program Files (x86)\Microsoft SQL Server\140\Tools\Binn\;
[23] C:\Program Files (x86)\Microsoft SQL Server\140\DTS\Binn\;
[24] C:\Program Files (x86)\Microsoft SQL Server\140\Tools\Binn\ManagementStudio\;
[25] C:\Program Files\nodejs\;
[26] C:\Program Files\dotnet\;
[27] C:\MinGW\msys\1.0\bin;
[28] C:\Program Files\Apache Software Foundation\Tomcat 8.5\bin;
[29] C:\Program Files\R\R-3.4.4\bin\x64;
[30] C:\Program Files\R\R-3.4.4\library\rJava\jri\x64;
[31] C:\Program Files\MiKTeX 2.9\miktex\bin\x64\;
[32] C:\Python27;
[33] C:\Users\admin\AppData\Local\Microsoft\WindowsApps;
[34] .;
```

### SQL Server TCP/IP Java Connection Errors

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

The method for configuring a specific port is detailed in: (https://docs.microsoft.com/en-us/sql/database-engine/configure-windows/configure-a-server-to-listen-on-a-specific-tcp-port)

* For all entries, clear TCP dynamic ports, set the TCP port to 1433
  ![Configuring a specifc SQL Server port]({{ site.baseurl }}/rifWebApplication/sqlserver-change-port.png)

* Check you can logon as before using shared memory/named pipes, and then check the port
	```
	sqlcmd -U peter -P XXXXXXXXXX -d sahsuland_dev -S 192.168.1.65\SAHSU,1433
	1> quit
	```

### Tomcat service will not start

When the Tomcat application (Tomcat8w) is used to set the default Java installed on the machine, some desktop systems may not run Tomcat as a service if a 32bit Java was installed first.
the Windows event log has the following cryptic message
```
The Apache Tomcat 8.5 Tomcat8 service terminated with the following service-specific error:
Incorrect function.
```

The comms deamon log: commons-daemon.<date e.g.,  2018-04-16>.log has:
```
[2018-04-16 12:58:37] [info]  ( prunsrv.c:1733) [18188] Commons Daemon procrun (1.1.0.0 64-bit) started
[2018-04-16 12:58:37] [info]  ( prunsrv.c:1643) [18188] Running 'Tomcat8' Service...
[2018-04-16 12:58:37] [debug] ( prunsrv.c:1417) [19736] Inside ServiceMain...
[2018-04-16 12:58:37] [debug] ( prunsrv.c:885 ) [19736] reportServiceStatusE: dwCurrentState = 2, dwWin32ExitCode = 0, dwWaitHint = 3000, dwServiceSpecificExitCode = 0
[2018-04-16 12:58:37] [info]  ( prunsrv.c:1175) [19736] Starting service...
[2018-04-16 12:58:37] [error] ( prunsrv.c:1210) [19736] Failed creating Java
[2018-04-16 12:58:37] [error] ( prunsrv.c:1580) [19736] ServiceStart returned 1
[2018-04-16 12:58:37] [debug] ( prunsrv.c:885 ) [19736] reportServiceStatusE: dwCurrentState = 1, dwWin32ExitCode = 1066, dwWaitHint = 0, dwServiceSpecificExitCode = 1
[2018-04-16 12:58:37] [info]  ( prunsrv.c:1645) [18188] Run service finished.
[2018-04-16 12:58:37] [info]  ( prunsrv.c:1814) [18188] Commons Daemon procrun finished
```

Since there is no sign of Java in the program listing, but the system came with 32bit JRE pre-installed.

### OutOfMemoryError: Java heap space

The front end reports: ```ERROR: Study tables export error for: 1002 LUNG CANCER```

The middleware log contains:
```
Adding RIFGRAPHICS_JPEG for report file: c:\rifDemo\scratchSpace\d1-100\s7\maps\smoothed_smr_7_inv7_males_1000dpi.jpg; pixel width: 7480; pixels/mm: 39.37008
13:26:48.942 [http-nio-8080-exec-5] ERROR rifGenericLibrary.util.RIFLogger : [rifServices.dataStorageLayer.common.RifZipFile]:
createStudyExtract() OutOfMemoryError; heap usage: 117M, 228M
getMessage:          OutOfMemoryError: Java heap space
getRootCauseMessage: OutOfMemoryError: Java heap space
getThrowableCount:   1
getRootCauseStackTrace >>>
java.lang.OutOfMemoryError: Java heap space
	at java.awt.image.DataBufferInt.<init>(DataBufferInt.java:75)
	at java.awt.image.SinglePixelPackedSampleModel.createDataBuffer(SinglePixelPackedSampleModel.java:242)
	at java.awt.image.Raster.createWritableRaster(Raster.java:941)
	at org.apache.batik.gvt.renderer.StaticRenderer.updateWorkingBuffers(StaticRenderer.java:536)
	at org.apache.batik.gvt.renderer.StaticRenderer.repaint(StaticRenderer.java:375)
	at org.apache.batik.gvt.renderer.StaticRenderer.repaint(StaticRenderer.java:344)
	at org.apache.batik.transcoder.image.ImageTranscoder.transcode(ImageTranscoder.java:111)
	at org.apache.batik.transcoder.XMLAbstractTranscoder.transcode(XMLAbstractTranscoder.java:142)
	at org.apache.batik.transcoder.SVGAbstractTranscoder.transcode(SVGAbstractTranscoder.java:156)
	at rifServices.graphics.RIFGraphics.graphicsTranscode(RIFGraphics.java:250)
	at rifServices.graphics.RIFGraphics.addGraphicsFile(RIFGraphics.java:413)
	at rifServices.graphics.RIFGraphics.addGraphicsFile(RIFGraphics.java:279)
	at rifServices.graphics.RIFMaps.createGraphicsMaps(RIFMaps.java:1217)
	at rifServices.graphics.RIFMaps.writeMap(RIFMaps.java:532)
	at rifServices.graphics.RIFMaps.writeResultsMaps(RIFMaps.java:346)
	at rifServices.dataStorageLayer.common.RifGeospatialOutputs.writeGeospatialFiles(RifGeospatialOutputs.java:330)
	at rifServices.dataStorageLayer.common.RifZipFile.createStudyExtract(RifZipFile.java:481)
	at rifServices.dataStorageLayer.pg.PGSQLStudyExtractManager.createStudyExtract(PGSQLStudyExtractManager.java:485)
	at rifServices.dataStorageLayer.pg.PGSQLAbstractRIFStudySubmissionService.createStudyExtract(PGSQLAbstractRIFStudySubmissionService.java:1475)
	at rifServices.restfulWebServices.pg.PGSQLAbstractRIFWebServiceResource.createZipFile(PGSQLAbstractRIFWebServiceResource.java:965)
	at rifServices.restfulWebServices.pg.PGSQLRIFStudySubmissionWebServiceResource.createZipFile(PGSQLRIFStudySubmissionWebServiceResource.java:1239)
```

Check the memory available to your Java version:
```
C:\Users\phamb\Documents\GitHub\rapidInquiryFacility>java -XX:+PrintFlagsFinal -version | findstr HeapSize
    uintx ErgoHeapSizeLimit                         = 0                                   {product}
    uintx HeapSizePerGCThread                       = 87241520                            {product}
    uintx InitialHeapSize                          := 199229440                           {product}
    uintx LargePageHeapSizeThreshold                = 134217728                           {product}
    uintx MaxHeapSize                              := 3187671040                          {product}
java version "1.8.0_162"
Java(TM) SE Runtime Environment (build 1.8.0_162-b12)
Java HotSpot(TM) 64-Bit Server VM (build 25.162-b12, mixed mode)
```

<<<<<<< HEAD
In the case the initial size is 192M and the maximum heap size is 3040M. In the Tomcat configurator 8romcat8w* the maximum memory size on the Java pane is 256M,
increase this to a much larger value less than the maximum, at least 2048M. Restart the Tomcat service.
=======
In the case the initial size is 192M and the maximum heap size is 3040M. In the tomcat configurator 8romcat8w* the maximum memory size on the Java pane is 256M,
increase this to a much larger value less than the maximum, at least 2048M. Tile generation can require large amounts of memory, UK census output area tiles
require 7G of memory. Restart the tomcat service.
>>>>>>> master

### Study extracts but R does not run

* User gets study is now running, but nothing every happens;
* The RIF is unresponsively; reloading the RIF fails with the server not responding: "Firefox csan't establish a connect to serfver at localhost:8080
* Tomcat is not running

Restarting the server using ```catalina.bat run``` and re-running the study results in an "Cannot find JRI native library" error

See:

* [jri.dll: Can't find dependent libraries]({{ site.baseurl }}/Installation/rifWebApplication#cannot-find-jri-native-library-jridll-cannot-find-dependent-libraries)

### SQL Server ODBC Connection Errors

Symptoms: when creating a SQL Server ODBC connection:

* No items in database list.

  ![SQL Server ODBC No database List]({{ site.baseurl }}/rifWebApplication/sql_server_odbc_connection_no_databases.png)

* ODBC error in connection test.

  ![SQL Server ODBC Connection Error]({{ site.baseurl }}/rifWebApplication/sql_server_odbc_connection_error.png)

* No items in database list is a symptom of no discovery services and is not an error. Type in your *hostname* manually.

  ![SQL Server ODBC No connection List]({{ site.baseurl }}/rifWebApplication/sql_server_odbc_connection_no_list.png)

**Tip:** type *hostname* for your TCP/IP hostname. This is not normally a fully qualified domain name (i.e. will only work locally)
```
C:\Users\phamb\Documents\GitHub\rapidInquiryFacility>hostname
DESKTOP-4P2SA80
```

Resolution:

a) Check TCP/IP connections to the database are permitted;
b) Check your firewall/other security software is permitting access to your server host port 1433/1434.

* Check if remote access is enabled (it should be) using SQL Server Management Studio as administrator: https://msdn.microsoft.com/en-gb/library/ms191464(v=sql.120).aspx
* Check TCP access is enabled using SQL Server Configuration Manager as administrator: https://msdn.microsoft.com/en-us/library/ms189083.aspx
  If when you open SQL Server Configuration Manager in SQL Server you get the following error: "Cannot connect to WMI provider. You do not have permission or the server is unreachable"; see:
  (https://support.microsoft.com/en-us/help/956013/error-message-when-you-open-sql-server-configuration-manager-in-sql-se). Make sure you set number to the highest version present in the directory:

  *mofcomp "%programfiles(x86)%\Microsoft SQL Server\**&lt;number&gt;**\Shared\sqlmgmproviderxpsp2up.mof"*
  e.g.
  ```
	C:\Program Files\Apache Software Foundation\Tomcat 8.5\bin>mofcomp "%programfiles(x86)%\Microsoft SQL Server\140\Shared\sqlmgmprovid
	erxpsp2up.mof"
	Microsoft (R) MOF Compiler Version 6.3.9600.16384
	Copyright (c) Microsoft Corp. 1997-2006. All rights reserved.
	Parsing MOF file: C:\Program Files (x86)\Microsoft SQL Server\140\Shared\sqlmgmproviderxpsp2up.mof
	MOF file has been successfully parsed
	Storing data in the repository...
	Done!
  ```
* Check the SQL server port (1433) is listening on TCP/IP for both localhost internal machine connections [::1 and 127.0.0.1:] and if required for network connections
  ```C:\Users\phamb\Documents\GitHub\rapidInquiryFacility\rifDatabase\SQLserver\installation>netstat -an | findstr "143[34]"
	  TCP    0.0.0.0:1433           0.0.0.0:0              LISTENING
	  TCP    127.0.0.1:1434         0.0.0.0:0              LISTENING
	  TCP    129.31.247.202:60396   129.31.247.202:1433    TIME_WAIT
	  TCP    192.168.1.101:60392    192.168.1.101:1433     TIME_WAIT
	  TCP    192.168.1.101:60397    192.168.1.101:1433     TIME_WAIT
	  TCP    [::]:1433              [::]:0                 LISTENING
	  TCP    [::1]:1434             [::]:0                 LISTENING
	  TCP    [2001:0:4137:9e76:2464:202e:7ee0:835]:60398  [2001:0:4137:9e76:2464:202e:7ee0:835]:1433  TIME_WAIT
	  TCP    [fe80::2464:202e:7ee0:835%3]:1433  [fe80::2464:202e:7ee0:835%3]:60395  ESTABLISHED
	  TCP    [fe80::2464:202e:7ee0:835%3]:60395  [fe80::2464:202e:7ee0:835%3]:1433  ESTABLISHED
  ```
  If it is then the first two points have worked and you have a firewall issue!
* Check your firewall permits access to TCP port 1433. **Be careful _not_ to allow Internet access unless you intend it.**
* The following is more helpful than the official Microsoft manuals: https://blogs.msdn.microsoft.com/walzenbach/2010/04/14/how-to-enable-remote-connections-in-sql-server-2008/
* Check you can connect using *sqlcmd -U **&lt;your username&gt;** -P **&lt;your password&gt;** -S tcp:**&lt;your hostname&gt;**```:
  ```
  C:\Users\phamb\Documents\GitHub\rapidInquiryFacility\rifDatabase\SQLserver\installation>sqlcmd -U peter -P XXXXXXXXXXXX -S tcp:DESKTOP-4P2SA80
  1>
  ```

# Running the RIF

* Make sure you have restarted Tomcat before attempting to run the RIF for the first time
* In a non networked single machine environment (e.g. a laptop) the RIF is at: (http://localhost:8080/RIF40)
* In a networked environment the RIF is at: ```http://<your domain>/RIF40```, e.g. *https://aepw-rif27.sm.med.ic.ac.uk/RIF40*
* Test cases are provided in the *tests* folder of the SAHSU supplied bundle:
  - *TEST 1002 LUNG CANCER HET 95_96.json typically takes around 85 seconds on a Postgres database;
  - *TEST 1003 LUNG CANCER BYM ALL YEARS 89_16.json* typically takes around 160 seconds on a Postgres database

## Logging On

* Use the *TESTUSER* created when the database was built. Do not attempt to logon as a server administrator (e.g. postgres) or the RIF
  software owner (rif40).
* Connect to the RIF. You should see the logon page:

  ![RIF logon]({{ site.baseurl }}/rifWebApplication/rif_logon.png){:width="100%"}

* After logon you should see the study submission page:

  ![RIF after logon]({{ site.baseurl }}/rifWebApplication/rif_after_logon.png){:width="100%"}

* If you do not see this then use the section on logon troubleshooting below

## Logon troubleshooting

1. Call the web service directly in a browser window, setting the username and password as appropriate.

	http://localhost:8080/rifServices/studysubmission/login?userID=peterh&password=XXXXXXXXXXXXXXX

	* A successful logon returns:

	```
	[{"result":"User peterh logged in."}]
	```

	* A failed logon returns (as from above, my password is not *XXXXXXXXXXXXXXX*):

	```
	[{"errorMessages":["Unable to register \"peterh\"."]}]
	```

	The Tomcat logs can be check for the actual error:

	```
	org.postgresql.util.PSQLException: FATAL: password authentication failed for user "peterh"
			at org.postgresql.core.v3.ConnectionFactoryImpl.doAuthentication(ConnectionFactoryImpl.java:408)
	```

2. Check the logs for any errors listed in *4.4 Common Setup Errors*
3. Use the browser developer facilities to trace the middleware web services calls.

The service address and port used should match what you setup up in *4.2 Setup Network*. If this does not:

* Restart Tomcat;
* Flush your browser cache (this is especially important for Google Chrome and Mozilla Firefox).

## R Issues

### Cannot find JRI native library; jri.dll already loaded

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
  The solution is to restart Tomcat.

  1. Server reload needs to stop R
  2. R crashes (usually inla) and ideally script errors need to stop R

### R ERROR: argument is of length zero ; call stack: if scale.model

This is typified by the R error trace:

```
.handleSimpleError(function (obj)
{
    calls = sys.calls()
    calls = ca <text>#1: INLA::f(area_order, model = "bym", graph = IM, adjust.for.con.com eval(parse(text = gsub("^f\\(", "INLA::f(", terms[i])), envir = data, enclo eval(parse(text = gsub("^f\\(", "INLA::f(", terms[i])), envir = data, enclo inla.interpret.formula(formula, data.same.len = data.same.len, data = data, performSmoothingActivity.R#609: inla(formula, family = "poisson", E = EXP_U performSmoothingActivity(data, AdjRowset) Adj_Cov_Smooth_JRI.R#361: withVisible(expr) Adj_Cov_Smooth_JRI.R#361: withCallingHandlers(withVisible(expr), error = er withErrorTracing({
    data = fetchExtractTable()
    AdjRowset = getAdjace doTryCatch(return(expr), name, parentenv, handler) tryCatchOne(expr, names, parentenv, handlers[[1]]) tryCatchList(expr, names[-nh], parentenv, handlers[-nh]) doTryCatch(return(expr), name, parentenv, handler) tryCatchOne(tryCatchList(expr, names[-nh], parentenv, handlers[-nh]), names tryCatchList(expr, classes, parentenv, handlers) tryCatch({
    withErrorTracing({
        data = fetchExtractTable()
       eval(expr, pf) eval(expr, pf) withVisible(eval(expr, pf)) evalVis(expr) Adj_Cov_Smooth_JRI.R#382: capture.output({
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
```

This occurs under INLA 0.0-1485844051; re-install the latest R-INLA:

```install.packages("INLA", repos="https://inla.r-inla-download.org/R/stable", dep=TRUE)```

After the upgrade you should get INLA_17.06.20 or later:

```
> library('INLA')
Loading required package: sp
Loading required package: Matrix
This is INLA_17.06.20 built 2017-06-20 03:42:30 UTC.
See www.r-inla.org/contact-us for how to get help.
>
```

This fixes the error : "R BYM sahsuland fault\R BYM sahsuland fault - no covariates.txt"

### Cannot find JRI native library; jri.dll Cannot find dependent libraries

* Tomcat server crashes

* Tomcat log:
  ```
	2018-04-19 15:37:26,223 http-nio-8080-exec-9 DEBUG Now writing to C:\Program Files\Apache Software Foundation\Tomcat 8.5/log4j2/2018
	-04/RIF_middleware.2018-04-19-1.log at 2018-04-19T15:37:26.223+0100
	Cannot find JRI native library!
	Please make sure that the JRI native library is in a directory listed in java.library.path.

	java.lang.UnsatisfiedLinkError: C:\Program Files\R\R-3.4.4\library\rJava\jri\x64\jri.dll: Can't find dependent libraries
			at java.lang.ClassLoader$NativeLibrary.load(Native Method)
			at java.lang.ClassLoader.loadLibrary0(ClassLoader.java:1941)
			at java.lang.ClassLoader.loadLibrary(ClassLoader.java:1857)
			at java.lang.Runtime.loadLibrary0(Runtime.java:870)
			at java.lang.System.loadLibrary(System.java:1122)
			at org.rosuda.JRI.Rengine.<clinit>(Rengine.java:19)
			at rifServices.dataStorageLayer.pg.PGSQLSmoothResultsSubmissionStep.performStep(PGSQLSmoothResultsSubmissionStep.java:232)
			at rifServices.dataStorageLayer.pg.PGSQLRunStudyThread.smoothResults(PGSQLRunStudyThread.java:314)
			at rifServices.dataStorageLayer.pg.PGSQLRunStudyThread.run(PGSQLRunStudyThread.java:191)
			at java.lang.Thread.run(Thread.java:748)
			at rifServices.dataStorageLayer.pg.PGSQLAbstractRIFStudySubmissionService.submitStudy(PGSQLAbstractRIFStudySubmissionService
	.java:1078)
  ```

* RIF middleware log:
  ```
	=======getInvestigationID========2===
	About to call next
	called next
	Investigation name==TEST 1001  ID==12==
  ```

* Check: *C:\Program Files\R\R-3.4.4\bin\x64* is in the path and not *C:\Program Files\R\R-3.4.4\bin*

# Patching

## RIF Web Application

Be aware that the RIF war file is versioned (it creates a directory *RIF40*) and this will modify the
instructions if it is updated say to *RIF41*.

* IF you have modified it (you normally do not need to) save the RIF web application file
  *%CATALINA_HOME%\webapps\RIF40\backend\services\rifs-back-urls.js* outside of the Tomcat tree;
* Stop Tomcat;
* Change directory to *%CATALINA_HOME%\webapps*; rename RIF40 to RIF40.old;
* Follow the instructions in
  [installing the RIF Web Application]({{ site.baseurl }}/Installation/rifWebApplication#rif-web-application)
  i.e. copy the replacement *RIF40.war* file into the *%CATALINA_HOME%\webapps\* directory;
* Restore *%CATALINA_HOME%\webapps\RIF40\backend\services\rifs-back-urls.js* if you have modified it;
* When you are satisfied with the patch remove the RIF40.old directory in *%CATALINA_HOME%\webapps*.

The RIF web application may require you to patch the database. You will get messages on logon such as
**alter_10.sql (post 3rd August 2018 changes for risk analysis) not run** to tell you to run the alter scripts.

![alter_10.sql (post 3rd August 2018 changes for risk analysis) not run]({{ site.baseurl }}/rifWebApplication/alter_10.PNG){:width="100%"}


See the database Management manual: [Patching](https://smallareahealthstatisticsunit.github.io/rapidInquiryFacility/rifDatabase/databaseManagementManual.html#patching)

## RIF Middleware

* If you have not already moved it then save the Java connector for the RifServices middleware: *%CATALINA_HOME%\webapps\rifServices\WEB-INF\classes\RIFServiceStartupProperties.properties*
  to *%CATALINA_HOME%\conf\RIFServiceStartupProperties.properties*;
* Stop Tomcat;
* Change directory to *%CATALINA_HOME%\webapps*; rename the .WAR files to .WAR.OLD; rename the rifServices
  and taxonomyServices trees to .old;
* Follow the instructions in
  [installing the web services]({{ site.baseurl }}/Installation/rifWebApplication#rif-services).
<<<<<<< HEAD
  i.e. copy replacement *taxonomy.war and rifServices.war* files into the *%CATALINA_HOME%\webapps\* directory;
* Start Tomcat, check rifServices and taxonomyservices are unpacked and check they are running in the logs;
* Restart Tomcat;
=======
  i.e. copy replacement *taxonomy.war*, *statistics.war* and *rifServices.war* files into the *%CATALINA_HOME%\webapps\* directory;
* Start tomcat, check rifServices and taxonomyservices are unpacked and check they are running in the logs;
* Restart tomcat;
>>>>>>> master
* When you are satisfied with the patch remove the .old files and directories in *%CATALINA_HOME%\webapps*.

Do **NOT** attempt to warm upgrade the RIF middleware. It will fail if any of the following are true:

* You have run a study (R does not shutdown correctly);
* You have not copied the optional logging properties files to *%CATALINA_HOME%\conf* and they are in use;
* You have any file in %CATALINA_HOME%\webapps* open in an editor.

In the first case *tomcat&* will restart the services but R will not run as it cannot attach the R shared library (see earlier). In the other two cases Tomcat will still be running
but the service will be down with a minimal file tree under *%CATALINA_HOME%\webapps*\rifServices*. The front end will report that the middleware is down.

In both cases restart Tomcat.

## Tomcat

This has not been tested ans it has not been required. Files to be saved/restored:

* *%CATALINA_HOME%/conf/server.xml*
* *%CATALINA_HOME%/conf/web.xml*

**ALWAYS RESTART THE SERVER!**

## R

If you upgrade R to newer version then follow the instructions for installing and configuring R and JRI in
[Setup R]({{ site.baseurl }}/Installation/rifWebApplication#setup-r).
Make absolutely sure the PATH and R_HOME are set correctly.

Updating the packages can also be done (consult your statisticians first); on a private network you have two choices:

* Create a private CRAN on a webserver and get R to use your local CRAN. This is the method used on the SAHSU private work before;
* Update the packages manually from R .tar.gz/.zip files. This requires a knowledge of the dependencies and is not recommended apart from for INLA.
  - Download INLA from: https://inla.r-inla-download.org/R/stable/bin/windows/contrib/3.4/INLA_0.0-1485844051.zip
  - Install INLA manually as Administrator:
    ```
    R CMD INSTALL INLA_0.0-1485844051.zip
    ```

# Front End and Middleware Software Upgrades

The RIF uses frozen in time the front end Java and libraries. The following updates in particular will need to be carried out in 2019 to keep the code stable, current and supported:

* Update Java from version 8 to 10. JDK 8 end of life is January 2019;
* Angular: 1.5.8 to 1.6.9. Moving to Angular 2.x is likely far too difficult for little gain;
* Leaflet: 1.0.3 to 1.3.1;
* Jackson: 1.9.2 to 2.9.5+;
* Jersey: 1.19 to 2.27+;
* JRI: 0.8.4 to 0.9.9+;

Of these updates, Java, Jersey and JAckson are likely to create the most problems.

# Advanced Setup

## Running Tomcat as a service

You only need to do this if you want Tomcat to start when the server boots. This is not advised on a laptop as it uses 2GB of memory; stop and start Tomcat manually.
You can do this last!

* It is advised to reinstall the Tomcat service as the Tomcat installer usually messes it up! In the directory %CATALINA_HOME%/bin; see:
  [Windows service HOW-TO](http://tomcat.apache.org/tomcat-8.0-doc/windows-service-howto.html#Installing_services)

	```
	C:\Program Files\Apache Software Foundation\Tomcat 8.5\bin>service.bat install
	Installing the service 'Tomcat8' ...
	Using CATALINA_HOME:    "C:\Program Files\Apache Software Foundation\Tomcat 8.5"
	Using CATALINA_BASE:    "C:\Program Files\Apache Software Foundation\Tomcat 8.5"
	Using JAVA_HOME:        "C:\Program Files\Java\jdk1.8.0_162"
	Using JRE_HOME:         "C:\Program Files\Java\jdk1.8.0_162\jre"
	Using JVM:              "C:\Program Files\Java\jdk1.8.0_162\jre\bin\server\jvm.dll"
	Failed installing 'Tomcat8' service

	C:\Program Files\Apache Software Foundation\Tomcat 8.5\bin>service.bat remove
	Removing the service 'Tomcat8' ...
	Using CATALINA_BASE:    "C:\Program Files\Apache Software Foundation\Tomcat 8.5"
	The service 'Tomcat8' has been removed

	C:\Program Files\Apache Software Foundation\Tomcat 8.5\bin>service.bat install
	Installing the service 'Tomcat8' ...
	Using CATALINA_HOME:    "C:\Program Files\Apache Software Foundation\Tomcat 8.5"
	Using CATALINA_BASE:    "C:\Program Files\Apache Software Foundation\Tomcat 8.5"
	Using JAVA_HOME:        "C:\Program Files\Java\jdk1.8.0_162"
	Using JRE_HOME:         "C:\Program Files\Java\jdk1.8.0_162\jre"
	Using JVM:              "C:\Program Files\Java\jdk1.8.0_162\jre\bin\server\jvm.dll"
	The service 'Tomcat8' has been installed.
	```

  Then use the configure Tomcat application (Tomcat8w) to use the default Java installed on the machine. This prevents upgrades from breaking Tomcat!

  ![Setting Java version autodetect]({{ site.baseurl }}/rifWebApplication/tomcat8_configuration_3.PNG)

  Note: on some desktop systems this may prevent Tomcat running as a service if a 32bit Java was installed first, with the Windows event log having the cryptic message
  ```
  The Apache Tomcat 8.5 Tomcat8 service terminated with the following service-specific error:
  Incorrect function.
  ```
  Tomcat logs to: commons-daemon.<date e.g.,  2018-04-16>.log, tomcat8-stderr.<date e.g.,  2018-04-16>.log, tomcat8-stdout.<date e.g.,  2018-04-16>.log instead of to the console

* Use the configure Tomcat application (Tomcat8w) to make the startup type automatic.

  ![Make the startup type automatic]({{ site.baseurl }}/rifWebApplication/tomcat8_configuration_1.png)

* Use the configure Tomcat application (Tomcat8w) to set the logging level to debug.

  ![Set the logging level to debug]({{ site.baseurl }}/rifWebApplication/tomcat8_configuration_2.PNG?)

* Check the memory available to your Java version:
  ```
	C:\Users\phamb\Documents\GitHub\rapidInquiryFacility>java -XX:+PrintFlagsFinal -version | findstr HeapSize
		uintx ErgoHeapSizeLimit                         = 0                                   {product}
		uintx HeapSizePerGCThread                       = 87241520                            {product}
		uintx InitialHeapSize                          := 199229440                           {product}
		uintx LargePageHeapSizeThreshold                = 134217728                           {product}
		uintx MaxHeapSize                              := 3187671040                          {product}
	java version "1.8.0_162"
	Java(TM) SE Runtime Environment (build 1.8.0_162-b12)
	Java HotSpot(TM) 64-Bit Server VM (build 25.162-b12, mixed mode)
  ```

  In the case the initial size is 192M and the maximum heap size is 3040M. In the Tomcat configurator 8romcat8w* the maximum memory size on the Java pane is 256M,
  increase this to a much larger value less than the maximum, at least 2048M.

## Using JConsole with Tomcat

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

 ![Jconsole]({{ site.baseurl }}/rifWebApplication/Jconsole.png){:width="100%"}

## Securing Tomcat

Injecting HTTP Response with the secure header can mitigate most of the web security vulnerabilities. These changes
implement the necessary HTTP headers to comply with OWASP security standards.

Having a secure header instructs the browser to do or not to do certain things and thence prevent certain security attacks.

Tomcat 8 has added support for following HTTP response headers.

* X-Frame-Options – to prevent clickjacking attack
* X-XSS-Protection – to avoid cross-site scripting attack
* X-Content-Type-Options – block content type sniffing
* HSTS – add strict transport security

As a best practice, take a backup of necessary configuration file before making changes or test in a non-production environment.

In the *%CATALINA_HOME%/conf* folder under path where Tomcat is installed edit *web.xml* and uncomment the following filter (by default it is commented out):
```xml
    <filter>
        <filter-name>httpHeaderSecurity</filter-name>
        <filter-class>org.apache.catalina.filters.HttpHeaderSecurityFilter</filter-class>
        <async-supported>true</async-supported>
    </filter>
```

By uncommenting above, you instruct Tomcat to support HTTP Header Security filter.

Add the following just after the above filter:

```xml
<filter-mapping>
    <filter-name>httpHeaderSecurity</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
```

By adding above you instruct Tomcat to inject the HTTP Header in all the application URL.

Restart the Tomcat and access the application to verify the headers.

Tomcat security defaults (default values are in square brackets):

* hstsEnabled Should the HTTP Strict Transport Security (HSTS) header be added to the response? See RFC 6797
  for more information on HSTS. [true]
* hstsMaxAgeSeconds The max age value that should be used in the HSTS header. Negative values will be treated
  as zero. [0]
* hstsIncludeSubDomains Should the includeSubDomains parameter be included in the HSTS header.
* antiClickJackingEnabled Should the anti click-jacking header X-Frame-Options be added to every response? [true]                                         -->
* antiClickJackingOption What value should be used for the header. Must be one of DENY, SAMEORIGIN, ALLOW-FROM
  (case-insensitive). [DENY]
* antiClickJackingUri IF ALLOW-FROM is used, what URI should be allowed? []
* blockContentTypeSniffingEnabled Should the header that blocks content type sniffing be added to every response? [true]

### Adding an expires filter

ExpiresFilter is a Java Servlet API port of Apache mod_expires. This filter controls the setting of the
Expires HTTP header and the max-age directive of the Cache-Control HTTP header in server responses. The
expiration date can set to be relative to either the time the source file was last modified, or to the
time of the client access.

These HTTP headers are an instruction to the client about the document's validity and persistence. If
cached, the document may be fetched from the cache rather than from the source until this time has passed.
After that, the cache copy is considered "expired" and invalid, and a new copy must be obtained from the
source.

Replace:
```xml
<filter-mapping>
    <filter-name>httpHeaderSecurity</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
```

With:

```xml
    <filter-mapping>
        <filter-name>httpHeaderSecurity</filter-name>
        <url-pattern>/*</url-pattern>
        <dispatcher>REQUEST</dispatcher>
    </filter-mapping>

	<filter>
	 <filter-name>ExpiresFilter</filter-name>
	 <filter-class>org.apache.catalina.filters.ExpiresFilter</filter-class>
	 <init-param>
		<param-name>ExpiresByType image</param-name>
		<param-value>access plus 10 minutes</param-value>
	 </init-param>
	 <init-param>
		<param-name>ExpiresByType text/css</param-name>
		<param-value>access plus 10 minutes</param-value>
	 </init-param>
	 <init-param>
		<param-name>ExpiresByType application/javascript</param-name>
		<param-value>access plus 10 minutes</param-value>
	 </init-param>
	</filter>

	<filter-mapping>
	 <filter-name>ExpiresFilter</filter-name>
	 <url-pattern>/*</url-pattern>
	 <dispatcher>REQUEST</dispatcher>
	</filter-mapping>
```

## Other Setup

### PNG Tile Generation

For *geolevels* with more than 5000 areas the RIF middleware can auto generate PNG tiles on startup. If you do not do this the tiles
will be generated on the fly; this can take up to 60 seconds per tile for the most complex tiles with >200,000 areas. Tiles are then cached.
To rebuild the cache delete the tiles scratchSpace directory: *c:\rifDemo\scratchSpace\scratchSpace\tiles\<my geography>* For UK 2011
census geography this typically takes 10 to 20 minutes. You need to edit the *conf* directory *RIFServiceStartupProperties.properties*:
```
#
# Tile generator: set if you need automatic tile generation for geolevels with more than 5000 areas
# (see: disableMouseClicksAt in frontEndParameters.json5)
#
tileGeneratorUsername=<username>
tileGeneratorPassword=<password>
```

This process requires 7G of memory; edit %CATALINA_HOME%/bin/setenv.bat to add **-Xmx7g** to *CATALINA_OPTS*:
```
Exception in thread "http-nio-8080-AsyncTimeout"
Exception: java.lang.OutOfMemoryError thrown from the UncaughtExceptionHandler in thread "http-nio-8080-AsyncTimeout"
Exception in thread "http-nio-8080-ClientPoller-0" java.lang.OutOfMemoryError: GC overhead limit exceeded

Exception: java.lang.OutOfMemoryError thrown from the UncaughtExceptionHandler in thread "ajp-nio-8009-ClientPoller-1"
Exception in thread "http-nio-8080-exec-1" Exception in thread "http-nio-8080-exec-2" java.lang.OutOfMemoryError: GC overhead limit
exceeded
Exception in thread "http-nio-8080-exec-6"
Exception: java.lang.OutOfMemoryError thrown from the UncaughtExceptionHandler in thread "http-nio-8080-exec-6"
java.lang.OutOfMemoryError: GC overhead limit exceeded
Exception in thread "pool-611-thread-1" java.lang.OutOfMemoryError: GC overhead limit exceeded
Exception in thread "ajp-nio-8009-exec-3" Exception in thread "http-nio-8080-exec-12" java.lang.OutOfMemoryError: GC overhead limit
exceeded
java.lang.OutOfMemoryError: GC overhead limit exceeded
```

### Front End Logging

Front end logging is enabled by default to the log file: ```%CATALINA_HOME%/log4j2/<YYYY>-<MM>/FrontEndLogger.log-<N>```; e.g.
 *FrontEndLogger.2017-11-27-1.log*.

To enable debugging in the front end, copy frontEndParameters.json5 from %CATALINA_HOME%\webapps\rifServices\WEB-INF\classes. Copy this file to *%CATALINA_HOME%\conf*.
Set *debugEnabled* to *true*

This file contains various parameter to help debug the front end web application; all should be false unless you are a front end developer.
You will need to check the code to see what they have:

* usePouchDBCache:  DO NOT Use PouchDB caching in TopoJSONGridLayer.js; it interacts with the diseasemap sync;
* disableMapLocking:Disable disease map initial sync [You can re-enable it!]
* disableSelectionLocking:  Disable selection locking [You can re-enable it!]
* syncMapping2EventsDisabled: Disable syncMapping2Events handler [for leak testing]
* rrDropLineRedrawDisabled: Disable rrDropLineRedraw handler [for leak testing]
* rrchartWatchDisabled: Disable Angular $watch on rrchart<mapID> [for leak testing]

Aslo:

* mapLockingOptions: Map locking options (options for Leaflet.Sync())

Other parameters:

* You can define a *defaultLogin* do not do this in a production environment; for use on single user tests system only!
* The parameter *userMethods* allows you to define your own methods for mapping.
* The parameter *mappingDefaults* sets up the defaults for the three maps (viewermap, dismap1 and 1).

Mapping parameters should be changed with extreme caution as they will break the RIF badly if you set them up incorrectly.
You must logout, restart Tomcat and login again if you test any of the parameters.

```json
/**
 *
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU
 * that rapidly addresses epidemiological and public health questions using
 * routinely collected health and population data and generates standardised
 * rates and relative risks for any given health outcome, for specified age
 * and year ranges, for any given geographical area.
 *
 * <p>
 * Copyright 2017 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit
 * is funded by the Public Health England as part of the MRC-PHE Centre for
 * Environment and Health. Funding for this project has also been received
 * from the United States Centers for Disease Control and Prevention.
 * </p>
 *
 * <pre>
 * This file is part of the Rapid Inquiry Facility (RIF) project.
 * RIF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RIF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RIF. If not, see <http://www.gnu.org/licenses/>; or write
 * to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA 02110-1301 USA
 * </pre>
 *
 * <hr>
 * Peter Hambly
 * @author phambly
 * @version
 */

// THIS FILE MUST BE VALID JSON5; can contain comments and line feeds!
// http://json5.org/ or https://github.com/json5/json5

/*
The following is the exact list of additions to JSON’s syntax introduced by JSON5. All of these are optional, and all of these come from ES5.

Objects:
* Object keys can be unquoted if they’re valid identifiers. Yes, even reserved keywords (like default) are valid unquoted keys in ES5 [§11.1.5, §7.6]. (More info)
  (TODO: Unicode characters and escape sequences aren’t yet supported in this implementation.)
* Object keys can also be single-quoted.
* Objects can have trailing commas.

Arrays:
* Arrays can have trailing commas.

Strings:
* Strings can be single-quoted.
* Strings can be split across multiple lines; just prefix each newline with a backslash. [ES5 §7.8.4]

Numbers:
* Numbers can be hexadecimal (base 16).
* Numbers can begin or end with a (leading or trailing) decimal point.
* Numbers can include Infinity, -Infinity, NaN, and -NaN.
* Numbers can begin with an explicit plus sign.

Comments:
* Both inline (single-line) and block (multi-line) comments are allowed.
*/
{
	parameters: {
		usePouchDBCache: 	false,			// DO NOT Use PouchDB caching in TopoJSONGridLayer.js; it interacts with the diseasemap sync;
		debugEnabled:		false,			// Disable front end debugging
		disableMapLocking:	false,			// Disable disease map initial sync [You can re-enable it!]
		disableSelectionLocking: false,		// Disable selection locking [You can re-enable it!]

		syncMapping2EventsDisabled: false,	// Disable syncMapping2Events handler [for leak testing]
		rrDropLineRedrawDisabled: false,	// Disable rrDropLineRedraw handler [for leak testing]
		rrchartWatchDisabled: false,		// Disable Angular $watch on rrchart<mapID> [for leak testing]

		mapLockingOptions: {},				// Map locking options (for Leaflet.Sync())

		/*
		 * For the Color Brewer names see: https://github.com/timothyrenner/ColorBrewer.jl
		 * Derived from: http://colorbrewer2.org/
		 */
		mappingDefaults: {
			'diseasemap1': {
					method: 	'quantile',
					feature:	'smoothed_smr',
					intervals: 	9,
					invert:		true,
					brewerName:	"PuOr"
			},
			'diseasemap2': {
					method: 	'AtlasProbability',
					feature:	'posterior_probability',
					breaks: 	[0.0, 0.20, 0.81, 1.0],
					invert:		false,
					brewerName:	"RdYlGn"
			},
			'viewermap': {
					method: 	'AtlasRelativeRisk',
					feature:	'relative_risk',
					breaks:		[-Infinity, 0.68, 0.76, 0.86, 0.96, 1.07, 1.2, 1.35, 1.51, Infinity],
					invert:		true,
					brewerName: "PuOr"
			}
		},
		defaultLogin: {						// DO NOT SET in a production environment; for use on single user tests system only!
			username: 	"",
			password:	""
		},
		userMethods: {
			'AtlasRelativeRisk': {
					description: 'Atlas Relative Risk',
					breaks:		[-Infinity, 0.68, 0.76, 0.86, 0.96, 1.07, 1.2, 1.35, 1.51, Infinity],
					invert:		true,
					brewerName: "PuOr",
					invalidScales: ["Constant", "Dark2", "Accent", "Pastel2", "Set2"]
			},
			'AtlasProbability': {
					description: 'Atlas Probability',
					feature:	'posterior_probability',
					breaks: 	[0.0, 0.20, 0.81, 1.0],
					invert:		false,
					brewerName:	"RdYlGn",
                    invalidScales: ["Constant"]
			}
		}
	}
}
```

Change the log level to debug in the log4j setup for *rifGenericLibrary.util.FrontEndLogger*:

```xml
    <!-- RIF FRont End logger: rifGenericLibrary.util.FrontEndLogger -->
    <Logger name="rifGenericLibrary.util.FrontEndLogger"
		level="debug" additivity="false"> <!-- Chnage to debug for more output -->
      <!-- <AppenderRef ref="CONSOLE"/> uncomment to see RIF Front End console logging on the Tomcat console -->
      <AppenderRef ref="FRONTENDLOGGER"/>
    </Logger>
```

Example from: *FrontEndLogger.2017-11-27-1.log*:

```
13:06:23.479 [https-jsse-nio-8080-exec-10] ERROR rifGenericLibrary.util.FrontEndLogger :
userID:       peter
browser type: Firefox; v57
iP address:   0:0:0:0:0:0:0:1
message:      Could not initialise the taxonomy service
error stack>>>
rifMessage@https://localhost:8080/RIF4/utils/controllers/rifc-util-alert.js:290:10
$scope.showError@https://localhost:8080/RIF4/utils/controllers/rifc-util-alert.js:347:5
handleInitialiseError@https://localhost:8080/RIF4/dashboards/login/controllers/rifc-login-login.js:120:33
e/<@https://localhost:8080/RIF4/libs/standalone/angular.min.js:131:20
$eval@https://localhost:8080/RIF4/libs/standalone/angular.min.js:145:343
$digest@https://localhost:8080/RIF4/libs/standalone/angular.min.js:142:412
$apply@https://localhost:8080/RIF4/libs/standalone/angular.min.js:146:111
l@https://localhost:8080/RIF4/libs/standalone/angular.min.js:97:320
J@https://localhost:8080/RIF4/libs/standalone/angular.min.js:102:34
gg/</t.onload@https://localhost:8080/RIF4/libs/standalone/angular.min.js:103:4
<<<
actual time:  27/11/2017 13:06:23
relative:     +28.5
```

### Printing Defaults

The RIF has implemented the Elsevier guidelines: (https://www.elsevier.com/authors/author-schemas/artwork-and-media-instructions/artwork-sizing)

Number of pixels versus resolution and print size, for bitmap images
Image resolution, number of pixels and print size are related mathematically: Pixels = Resolution (DPI) × Print size (in inches); 300 DPI for halftone images; 500 DPI for combination art; 1000 DPI for line art. 72 Points in one inch.

| TARGET SIZE                | Image width     | Pixels@300dpi | Pixels@500dpi | Pixels@1000dpi |
|----------------------------|-----------------|:--------------|:--------------|:---------------|
| Minimal size               | 30 mm (85 pt)   | 354           | 591           | 1181           |
| Single column              | 90 mm (255 pt)  | 1063          | 1772          | 3543           |
| 1.5 column                 | 140 mm (397 pt) | 1654          | 2756          | 5512           |
| Double column (full width) | 190 mm (539 pt) | 2244          | 3740          | 7480           |

The PlosOne guidelines are: (http://journals.plos.org/plosone/s/figures)

Figure File Requirements
The list below is an abbreviated summary of the figure specifications. Read the full details of the requirements in the corresponding sections on this page.

* File Format:			TIFF or EPS
* Dimensions:			Width: 789 – 2250 pixels (at 300 dpi). Height maximum: 2625 pixels (at 300 dpi).
* Resolution:			300 – 600 dpi
* File Size:			<10 MB
* Text within Figures: 	Arial, Times, or Symbol font only in 8-12 point
* Figure Files: 		Fig1.tif, Fig2.eps, and so on. Match file name to caption label and citation.
* Captions:				In the manuscript, not in the figure file.

Printing defaults can be set system wide in *%CATALINA_HOME%\conf\RIFServiceStartupProperties.properties*. If you have not already moved it then save the
Java connector for the RifServices middleware: *%CATALINA_HOME%\webapps\rifServices\WEB-INF\classes\RIFServiceStartupProperties.properties*
to *%CATALINA_HOME%\conf\RIFServiceStartupProperties.properties*;

The RIF has the following defaults:

* printingDPI = 1000
  100 dots per inch
* denominatorPyramidWidthPixels = 3543
  Single column
* mapWidthPixels = 7480
  Double column (full width)
* jpegQuality = 0.8
  JPEG quality: between 0.75 and 1.0 (no loss)
  Below 0.75 will result is visible artefacts
* populationPyramidAspactRatio = 1.43
  Allows you to change the population pyramid aspect ratio
* copyrightInfo=null (not set)
   Set this to appropriate text to define Copyright in map images (only GEOTIFF supported at present)
   \u00A9 is the Unicode for the Copyright symbol (C)
* enableMapGrids=true
  This enables grid lines on the maps. This are to provide a scale
* enableCoordinateDisplay=false
  This is an experimental feature and add coordinates to the grids

Example from *RIFServiceStartupProperties.properties*
```.properties
#
# Printing setup:
#
# Journal requirements:
#
# PlosOne: http://journals.plos.org/plosone/s/figures
#
# Figure File Requirements
# The list below is an abbreviated summary of the figure specifications. Read the full details of the requirements in the corresponding sections on this page.
# File Format:			TIFF or EPS
# Dimensions:			Width: 789 – 2250 pixels (at 300 dpi). Height maximum: 2625 pixels (at 300 dpi).
# Resolution:			300 – 600 dpi
# File Size:			<10 MB
# Text within Figures: 	Arial, Times, or Symbol font only in 8-12 point
# Figure Files: 		Fig1.tif, Fig2.eps, and so on. Match file name to caption label and citation.
# Captions:				In the manuscript, not in the figure file.

# Elsevier: https://www.elsevier.com/authors/author-schemas/artwork-and-media-instructions/artwork-sizing
#
# Number of pixels versus resolution and print size, for bitmap images
# Image resolution, number of pixels and print size are related mathematically: Pixels = Resolution (DPI) × Print size (in inches); 300 DPI for halftone images; 500 DPI for combination art; 1000 DPI for line art. 72 Points in one inch.
# TARGET SIZE                   Image width 	Pixels@300dpi 	Pixels@500dpi 	Pixels@1000dpi
# Minimal size                   30 mm (85 pt)  354 	 		591 			1181
# Single column                  90 mm (255 pt) 1063 			1772 			3543
# 1.5 column 	                140 mm (397 pt) 1654 			2756 			5512
# Double column (full width)    190 mm (539 pt) 2244 			3740 			7480

#
# RIF default setup:
# denominator [population] Pyramid: 1000 dpi, 90mm width
#
# 1000dpi = 39.370079 pixel/mm
# 500dpi  = 16.685039 pixel/mm
# 300dpi  = 11.811024 pixel/mm
#
# 1 inch = 25.4mm
# 1000dpi => pixel/mm = dpi/25.4
#
printingDPI = 1000
denominatorPyramidWidthPixels = 3543
mapWidthPixels = 7480
#
# JPEG quality: between 0.75 and 1.0 (no loss)
# Below 0.75 will result is visible artefacts
#
jpegQuality = 0.8
#
# Population pyramid aspect ratio
#
populationPyramidAspactRatio = 1.43

#
# Set this to appropriate text to define Copyright in map images (only GEOTIFF supported at present)
# \u00A9 is the Unicode for the Copyright symbol (C)
#
#copyrightInfo="(C) <enter your name here>"

#
# To disable the map grids
#
# enableMapGrids=false

#
# To enable Coordinate Display
#
# enableCoordinateDisplay=true

#
# To change to the level of rounding in the results; including quantiles etc.
#
# roundDP=3
```

### Debugging

Since R now uses JRI, all errors appear in the Tomcat logs.

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

   ![Study status]({{ site.baseurl }}/rifWebApplication/study-status.png){:width="100%"}

   Clicking on the *trace* button will bring up the trace pane.

   ![R Trace]({{ site.baseurl }}/rifWebApplication/R-trace.png){:width="100%"}

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
   file (*rif40_run_R.bat*) created in the R script scratch area: ```extractDirectory=c:\\rifDemo\\scratchSpace```
   in the *<study id>\\data\\* sub directory.

   e.g. *C:\rifDemo\scratchSpace\s33\data*

   This contains the following files:

   * *Adj_Cov_Smooth_csv.R* - CSV version of R script (the middleware uses Adj_Cov_Smooth_JRI.R)
   * *performSmoothingActivity.R* - Smoothing module
   * *rif40_run_R.bat* - Script to run R to redo smoothing
   * *rif40_run_R_env.bat* - Study settings in use. Does *NOT* include the password!
   * *tmp_s33_adjacency_matrix.csv* - adjacency matrix for extract
   * *tmp_s33_extract.csv* - study data extract

```bat
C:\rifDemo\scratchSpace\s33\data>rif40_run_R.bat
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

4. A JRI successful run:

A typical JRI successful run looks like:

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

### R Memory Management

R is run as a attached DLL from the first middleware worker thread that runs a study. The per thread memory usage is printed at the end
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
a DLL this is the process id of Tomcat.

[Process Explorer](https://docs.microsoft.com/en-gb/sysinternals/downloads/process-explorer) is a Windows tool that aloows the user to
see the hidden R thread and the *inla* sub process.

R process ID tracer from the middleware log:

```
16:08:27.167 [http-nio-8080-exec-9] INFO  rifGenericLibrary.util.RIFLogger : [rifServices.dataStorageLayer.ms.MSSQLSmoothResultsSubmissionStep]:
Rengine Started; Rpid: 10644; JRI version: 266; thread ID: 30
```

![Java Process details]({{ site.baseurl }}/rifWebApplication/process-explorer-2.png){:width="100%"}

R will be limited to the maximum private memory (resident set size) of Java, typically around 3.3GB on Windows 8.1. To go beyond this
you will need to a) use 64bit Java! and b) set the *-Xmx* flag in  *%CATALINA_HOME%\bin\setenv.bat*; e.g. add ```-Xmx6g``` to
*CATALINA_OPTS*

**Peter Hambly, 12th April 2017; revised 4th August 2017 and 12th April 2018**
