# CDC RIF 4.0 Progress Report September 2015

## Highlight

No milestones.

## Data Loader - Kevin
   
* Kevin working on other projects
 
## Front end (webPlatform)

### Disease Mapping - No progress until a new Javascript developer is hired.

### Data viewer - No progress until a new Javascript developer is hired.

The data viewer prototype is currently frozen until it can be integrated into the Middleware.

## Middleware - Nan

Building demonstration systems for the visit to the CDC in Atlanta showed that the middleware would not build and install correctly in Tomcat; the process 
had to be carried out manually using Eclipse. The following tasks were allocated to Nan:

* Fix pom.xml (Java Maven build configuration) so that the Tomcat rif-services.war file is built correctly;
* Upgrade webservices interface to latest Jersey version.

Currently, integration is via make and copy; Tomcat automatically unpacks the rif-services.war file. If this fails this is logged into <log directory; e.g. /var/log/tomcat6 on Linux>/catalina.<date>.log.
This potentially could be improved by using the Tomcat Client Deployer package.

Java updates are also a problem as the Tomcat configuration needs to be edited.

Additionally, the web services themselves need to be manually verified as working in the middleware; as it is possible for:

a) The rif-services.war file unpack but the services fails to start (normally a pom.xml issue). 
b) RIf services starts but one or more services fail. i.e. RIF services needs regression testing.  
 
Download RIF 4.0 from Github on a laptop, try to build the database (Postgres version) by following Peter’s online documents.

The long process encounters errors, the bugs have been reported to Peter. Accordingly, Peter modifies the code and text in the documentation. 
Finally, the database is installed successfully. (However, make provided by MinGW sometimes cannot work when Windows installs the latest updates.)

A full report is at:  https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/Progress%20Reports/Nan%20Lin.md

### Web services (rifServices). 

### Run study batch - Nan

No progress, waiting Peter.

### Ontology support - Nan


## Databases

### Postgres, integration - Peter

Test harness refactor; Node.js version working
  
* EXPLAIN plan support
* Linked tests (based on the study 1 test scenario) 

Added: 

* Debug functions
* Instrumentation
* Conversion to Mutex locked version to prevent stack overload with waterfall of cascading callback functions

Mid September onwards:

* Regression test build
* User setup documentation
* Support in test harness for anonymous PL/pgSQL
* State machine verification tests
* Timestamps in test machine output connection trace
* Build documentation
* Small fixes to build (e.g. ::1 localhost fix, Node make fixes), regression tests complete opn Windows and Linux

A full report is at: https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/Progress%20Reports/Peter%20Hambly.md

### Microsoft SQL server - Margaret

Mostly working on other projects.  I perfomed more testing of the table and view triggers, and I spent some time getting comfortable using Node.js.

* I finished writing a Perl script to convert data files with quotes around only a few fields + mult-line text in a single cell into something that can be loaded into SQL Server so I can continue loading test data into SQL Server
* Quick edit to my script to convert Postgres data files into SQL Server data files to use | as the field delimiter, remove all double quotes (plus line breaks in multi-line comments).  This format works well with bulk import.
* More (informal/unstructured) testing of queries.
* Lots of struggles to get Node.js working on my computer with all the extra modules for connecting to the different databases.  Also playing around with basic Node scripts to understand how the technology works.

A full report is at: https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/Progress%20Reports/Margaret%20Douglass.md



 

 
