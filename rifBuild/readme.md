# Build and Install for RIF4

This directory tree contains the rationalised build and Install Instructions. The following ports are supported:

* Windows 7+/Postgres 9.3+
* Windows 7+/Microsoft SQL Server 2012
* Redhat Enterprise Linux 6.6/Postgres 9.3+. Other Linux ports should not be a problem for an experienced system administrator. 

Enterprise Linux ports will almost certainly require a substantial tool chain build for PostGres and in particular the PostGIS component. 
An example is provided for Redhat EL 6.6 (which ships with Postgres 8.4!).

In addition, the Postgres ports will support the remote storage of rif40_tables and rif40_covariates in a remote Oracle database using the 
foreign data wrapper and dblink functionality.

The RIF will eventually be available as a Windows/Postgres installer bundle for 64 bit Windows 7+ only.

## Software Requisites - all ports

### Database layer

* Postgres 9.3+ with PostgGIS or SQL Server 2012 enterprise
* R 3.1+
* PLR is optional for the Postgres port. Otherwise R is called as a program running scripts and loading/saving data via CSV files.

Postgres 9.2 will not work because it only has very basic JSON support features and lack the improve error diagnostics, in 
particular ```GET STACKED DIAGNOSTICS```. Both 9.3 and 9.4 are in use at SAHSU.

Microsoft SQL server versions before 2012 do not contain adequate GIS support. The enterprise version is required.

### Middleware

* Up to date latest Java (1.7+); JDK required if you build the RIF; JRE for the Postgres installer Bundle
* Apache Tomcat 8 (Tomcat 7 will probably work; but not advised). The dependency is WebSockets 1.1

### Front End

* Apache httpd 2.2+; Microsoft IIS server subject to test. It possible to use tomcat as a front end.

### Building the RIF

* Maven 3.3+ 
* GNU make 
* Java JDK 1.7+