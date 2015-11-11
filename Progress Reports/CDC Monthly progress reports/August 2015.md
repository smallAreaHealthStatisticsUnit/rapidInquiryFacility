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

No progress.

### Run study batch - Nan

No progress, waiting Peter.

### Ontology support - Nan

Wrap ICD10ClaMLTaxonomyProvider class developed last month into Jersey, exposing the work-alone service as a RESTful web service. 

The methodology follows the MVC convention: ICD10ClaMLTaxonomyProvider works as the controller, borrowed class HealthCode, TaxonomyTerm (from RIF) as the model, borrowed class HealthCodeProxy and WebServiceResponseGenerator (from RIF) as the view. 

The project is managed by maven, which uses the latest version of Jersey and Jackson. The package has been deployed on a Tomcat 8 container, it returns expected results when URLs have different combinations of parameters. 

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

Complete INSERT/UPDATE/DELETE table triggers and VIEW INSERT triggers. The views all users to only see studies they a) have created or b) have had shared with them.

A full report is at: https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/Progress%20Reports/Margaret%20Douglass.md



 

 
