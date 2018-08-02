These are my notes for getting on with future development on the RIF.

# Testing
if you want to run any automated test cases, they will be located in the src/test/java directory.  To run tests, you can do one of three things:
1. Right click over a package
2. Right click over a class
3. Right click over a method in a class
4. Click "Run As..." and choose JUnit Test.

# Eclipse tips
If you're wondering what is calling what, the most useful command is to highlight a method name, right click and choose
"Call Hierarchy".  This will tell you what might be calling the method in the code base.  If nothing is calling it, then
it might be a piece of dead code that you can comment out and then later remove.

# Project: rifJobSubmissionTool
This is a legacy project from the early development of RIF v4.0.  It's built using Java Swing and was initially created to
help me figure out what kinds of API methods should exist in a service that is letting scientists specify study submissions
for the RIF.  It has done its job but now it should be deleted from the tree.  The old tool will now appear incomplete 
because we never invested in giving map manipulation features. It now presents a maintenance overhead in that whenever
the APIs defined in rifServices change, you'd have to change code in the rifJobSubmissionTool, even though nobody uses it.

My suggestions:
1. Sync your copy of GitHub to get latest version of the repository files.
2. Delete rifSubmissionTool project in Eclipse.
3. Now look for any occurrences of "import rifJobSubmissionTool." If you don't get any results, it's safe to assume that
nothing depends on it.
4. Open a command line window and navigate to rapidInquiryFacility/rapidInquiryFacility.  Now type 
"mvn -Dmaven.test.skip=true compile".  If it is successful, you can again confirm nothing depends on it.


# Project: taxonomyServices
This project has been designed to be as independent of the RIF as possible.  Much of its source code draws upon generic 
code found in the rifGenericLibrary.  The taxonomyServices project will not be affected by Porting issues.

## Creating an ICD-9 service
This is the only likely taxonomy service the RIF will need in the near future.  
Advice:
1. Create a new class called ICD9TaxonomyService.  Make it extend rifGenericLibrary.taxonomyServices.AbstractTaxonomyService.
Draw heavily from taxonomyServices.ICDTaxonomyService (and then perhaps rename this class to ICD10TaxonomyService because
you'll end up with both one for ICD 9 and ICD 10).
2. Create and test a parser that is capable of reading ICD 9 codes from some sort of text file.  Make sure that parser
creates instances of rifGenericLibrary.taxonomyServices.TaxonomyTerm and set its parent and child properties appropriately.
3. Inside the parser class you make to read ICD 9 terms, register the TaxonomyTerm instances in an instance of 

rifGenericLibrary.taxonomyServices.TaxonomyTermManager.  This class provides the code that will support a lot of the 
methods that are required by the rifGenericLibrary.taxonomyServices.TaxonomyServiceAPI API.
4. Make sure any of your resource files for the ICD 9 service are located in the src/main/resources directory.  Note that
as we have done in ICD10, do not check the file containing all ICD 9 terms into GitHub.  This will help us avoid any 
potential licensing issues.
4. I would test the service independently of the rif.  Re-build the taxonomyServices web app to create the taxonomyServices.jar.
Put this into Tomcat's webapps directory and run it again. To test the service, call up a browser and in the URL line
put http://localhost:8080/taxonomyServices/taxonomyServices/initialiseServices.  It should return true.  Then type something
like this for your icd 9 service: http://localhost:8080/taxonomyServices/taxonomyServices/getRootTerms?taxonomy_id=icd9
If you get a bunch of terms expressed in JSON, you'll know it's probably working.

# Project: rifServices

## Eliminate HealthOutcomeManager
Early in development, rifServices contains classes to help support taxonomy services.  These were later moved to an independent
web application as a way of ensuring that the RIF would not be tied to changes in the way individual taxonomies evolved.
There should be no need for HealthOutcomeManager, because everything the main RIF web application needs from taxonomies is
supported by the taxonomyServices web application.


## Porting
My suggestion is that you change the package rifServices.dataStorageLayer to rifServices.dataStorageLayer.ps, to make it 
mirrow what will be done for the rifGenericLibrary and rifDataLoaderTool projects.  Then I would copy all of those classes
into a new package called rifServices.dataStorageLayer.ms.  Rename all of the classes in this directory so they begin with
MS - for Microsoft SQL Server.  In each of these classes, change references to underlying rifGenericLibrary.pg.* classes
to rifGenericLibrary.ms.* classes.  From then on, you'll have to coordinate changes for porting.  If you end up with 
classes that are common to both, put them into the package rifServices.dataStorageLayer.


## Rework the web services based on concept themes rather than task types.
Early in development, I tried making two independent services, one for each type of task: study submission or study result
retrieval.  They largely divided along the lines that: 
1. study result submission would involve a write operation that involved a complex tree of objects from classes in 

rifServices.businessConceptlayer.*.  
2. study result retrieval was read-only and would be pulling back concepts mainly related to columns and rows in the
extract and map database tables in the database.  

Over time, it turned out that the study submission tool and the study result retrieval tools would need many of the same 
things.  It also turned out that production and test versions of the same services would also share many of the same things.
This explains why rifServices.restfulWebServiceResource is so large - it ended up supporting most of the same behaviour shared
by the different services.  It should be refactored so that many smaller web services are created.  For example, you could
have one for Investigations, another for retrieving data for the interactive map display etc.

# Project: rifDataLoaderTool
The most important class is rifDataLoaderTool.presentationLayer.interactive.RIFDataLoaderToolApplication.  This contains the main class that will create the RIF Data Loader Tool dialog.










