# CDC RIF 4.0 Progress Report August 2015

## Highlight

No milestones.

## Data Loader - Kevin

* Working on middleware
  
## Front end (webPlatform)

### Disease Mapping - No progress until a new Javascript developer is hired.

### Data viewer - No progress until a new Javascript developer is hired.

The data viewer prototype is currently frozen until it can be integrated into the Middleware.

## Middleware

## August 10 to August 14: Kevin 
   * Improving auditing, packaging results into a zip file.
   
### Web services (rifServices). 

### Run study batch - Nan

No progress, waiting Peter.

### Ontology support - Nan

A full report is at:  https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/Progress%20Reports/Nan%20Lin.md

## Databases

### Postgres, integration - Peter 

Created test harness test data (to test the functionality); the basic principle is that each test 
(or series of linked tests) is a single transaction. This is not possible in Plpgsql; so needs
one of:

a) dblink (tested, works, no support for debugging or output capture;
b) Foreign data wrappers, no support for output capture;
c) Java, considered, actually choose;
d) Node.js to avoid confusion with middleware
 
Convert test data from Postgres array to XML format. 
New node based test harness

* Support for Postgres debugging
* Replace dblink test harness runner with Node.js version so that debugging and output can be controlled.

Also:

* CDC teleconference

A full report is at: https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/Progress%20Reports/Peter%20Hambly.md

### Microsoft SQL server - Margaret

A full report is at: https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/Progress%20Reports/Margaret%20Douglass.md



 

 
