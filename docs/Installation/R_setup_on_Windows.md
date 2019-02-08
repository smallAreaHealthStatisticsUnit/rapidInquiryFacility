---
layout: default
title: Setting Up the RIF's R Environment On Windows
---

## Installation

Download and install R: <https://cran.ma.imperial.ac.uk/bin/windows/base>

As with Java, do NOT use the 32-bit only version unless you have to. These instructions assume you use the 64-bit version

Add the 64 bit R executable to the path; e.g. *C:\Program Files\R\R-3.4.4\bin\x64*. Not: **C:\Program Files\R\R-3.4.4\bin**
or you will cause [jri.dll: Can't find dependent libraries]({{ site.baseurl }}/Installation/rifWebApplication#cannot-find-jri-native-library-jridll-cannot-find-dependent-libraries)


## R and Printing Output Directories

Create directories for extract (extractDirectory) and policies (extraDirectoryForExtractFiles). The defaults in *RIFServiceStartupProperties.properties* are:

* Extract: ```extractDirectory=c:\\rifDemo\\scratchSpace```
* Policies: ```extraDirectoryForExtractFiles=C:\\rifDemo\\generalDataExtractPolicies```

Grant appropriate read, write and execute access to these directories for Tomcat and SQL Server. Both normally run as the local administrator group: Administrators
(e.g. DESKTOP-4P2SA80\Administrators) so you do not need to do anything, it is advised to grant access to your local user if you are on a development system.

## R ODBC

This is only required for SQL Server ports of the RIF. All Postgres ports use R JDBC. We have been forced to use *RODBC* on SQL Server due to an *RJDBC* error:
```
saveDataFrameToDatabaseTable() ERROR: execute JDBC update query failed in dbSendUpdate (The incoming tabular data
stream (TDS) remote procedure call (RPC) protocol stream is incorrect. Parameter 5 (""): The supplied value is not
a valid instance of data type float. Check the source data for invalid values. An example of an invalid value is
data of numeric type with scale greater than precision.
```

Create and test a system ODBC datasource

* Using "control panel", "administrative tools", "ODBC Data Sources(64 bit)", right click "run as Adminstrator" for the database in use
* Use SQL Server Native Client version 11, 2011 version or later;
  ![SQL Server ODBC Setup]({{ site.baseurl }}/rifWebApplication/sql_server_odbc_sqlserver.png).

  The ODBC sytstem data source from *RIFServiceStartupProperties.properties* is: ```odbcDataSourceName=SQLServer13```; so
  the name is *SQLServer13*.

  1. Choose server. Normally you have to type in the host name as discovery will be turned off by default. You may need append "tcp:" to the hostname to force the use of
     TCP/IP:

     ![SQL Server ODBC Setup 1]({{ site.baseurl }}/rifWebApplication/sql_server_odbc_setup.png).

  2. Set the connection type to SQL Server authentication using a login and password. Make sure you supply the login and password.

	 ![SQL Server ODBC Setup 2]({{ site.baseurl }}/rifWebApplication/sql_server_odbc_setup2.png).

  3. Change the database to your database name (e.g. *sahsuland*)

     ![SQL Server ODBC Setup 3]({{ site.baseurl }}/rifWebApplication/sql_server_odbc_setup3.png).

* If you cannot see a SQL Server database list (you will get an error when SQL server tries to build a list) or get SQL Server connection errors on test see:
  [SQL Server ODBC Connection Errors]({{ site.baseurl }}/Installation/rifWebApplication#sql-server-odbc-connection-errors)

* Make sure you test the ODBC connection using the RIF user username and password.!

## R Packages

[See Setting Up R Packages](R_setup_packages).

## R Environment

1. Add R_HOME, e.g. *C:\Program Files\R\R-3.4.4* to the environment

2. Add the 64bit JRI native library location and the R_HOME bin\x64 directory to the path

   To use R from Tomcat Java you will need to install JRI. Fortunately, JRI is now a part of rJava and is installed with it.
   JRI will require its own native shared library which is already installed with rJava. To locate JRI installed with
   rJava, use
	```
	> system.file("jri",package="rJava")
	[1] "C:/Program Files/R/R-3.4.4/library/rJava/jri"
	```
   from inside of R [command-line]. Above command will give you a path. If you look in this directory you will see *i386*
   and *x64* sub directories. In *x64* you will be able to find the 64 bit *libjri.so*
   which is the shared library JRI is looking for.

   This 32 or 64 bit subdirectory appended needs to be added to the path:
   *C:\Program Files\R\R-3.4.4\library\rJava\jri\x64*. This ensures that file "x64\jri.dll"
   is in java.library.path. If you have 64bit Java (as instructed previous) you will need to use the 64 bit version.

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

	* **RESTART YOUR ADMINISTRATOR WINDOW TO PICK UP YOUR CHANGES**
	* [You can now start the rif]({{ site.baseurl }}/Installation/rifWebApplication#running-tomcat-on-the-command-line) (using the *start_rif.bat* script or by running *catalina.bat start* in the directory
	  *%CATALINA_HOME%\bin* as an Administrator.). The web services will fail to start on the first user logon if the R environment not setup correctly]; see:
  [RIF Services crash on logon]({{ site.baseurl }}/Installation/rifWebApplication#rif-services-crash-on-logon).
	* Then you can logon. See section 5
	  [Running the RIF]({{ site.baseurl }}/Installation/rifWebApplication#running-the-rif)
	  for logon instructions

