# CDC RIF 4.0 Progress Report September 2015

## Highlight

No milestones.

## Data Loader - Kevin
   
* Kevin working on other projects
 
## Front end (webPlatform)

### Disease Mapping - No progress until a new Javascript developer is hired.

### Data viewer - No progress until a new Javascript developer is hired.

The data viewer prototype is currently frozen until it can be integrated into the Middleware.

## Middleware

### Web services (rifServices). 

### Run study batch - Nan

No progress, waiting Peter.

### Ontology support - Nan

A full report is at:  https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/Progress%20Reports/Nan%20Lin.md

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



 

 
