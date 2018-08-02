# Code Road Map

The RIF code base is organised into multiple sub-projects, most of which support the Java-based middleware.  The code road map focuses mainly on how the Java code is organised, what the various parts of it do and how it can be extended.  Our discussion here is limited to the following sub-projects:
rapidInquiryFacility
* rifGenericLibrary
* rifServices
* rifJobSubmissionTool
* rifDataLoaderTool
* taxonomyServices

## Overview of the RIF Sub-Projects
rapidInquiryFacility is a top-level project whose purpose is to define resources that are shared by the others and to help maven compile the other sub-projects.  It contains no Java code and is unlikely to ever be altered unless there are Java Jar files that are likely to be needed by one or more of the other projects.

**rifGenericLibrary** holds code that originally appeared in one of the other Java projects.  However, it was thought that in future it might apply across multiple projects, including those that may not necessarily relate to the RIF.  If any RIF code would have a life beyond the RIF software suite, it would be found here. Its greatest area of future change will be the packages in rifGenericLibrary.dataStorageLayer, which are responsible for generating SQL queries for PostgreSQL and SQL Server.  It may gain more query formatter classes to correspond to basic SQL commands (eg: ALTER TABLE ADD CONSTRAINT...) and when one is added, it will almost almost always have parallel versions for PostgreSQL (denoted by rifGenericLibrary.dataStorageLayer.pg) and SQL Server (rifGenericLibrary.dataStorageLayer.ms).

**taxonomyServices** contains code that supports a web service which can provide terms from multiple taxonomies.  Currently it supports an ICD 10 taxonomy but in future it will support ICD9, OPCS and other services as well.  Its associated product is a web application that would run within Tomcat and be called by the part of the RIF scientist web application that lists diagnosis and operation codes.  It depends on rapidInquiryFacility and rifGenericLibrary projects.
rifServices contains code that supports the RIF scientist web application; the application allows users to submit studies and retrieve study results. It has no GUI component, and its main job is to use Java business classes to create SQL queries that can be executed against the underlying RIF database.  

This package is one of the oldest parts of the code base and began in the days when we were trying to define core RIF concepts and to identify service API methods that would first support the needs of a desktop GUI and alter a web-based GUI.  rifServices depends on rapidInquiryFacility and rifGenericLibrary.  It is used by the rifJobSubmissionTool and the rifWebApplication, which is JavaScript based.  It will change in response to new end-user features that are required in the RIF scientist web application and in response to SQL porting needs for PostgreSQL and SQL Server.

**rifJobSubmissionTool**: this is one of the oldest parts of the code base and contain Java Swing-based GUI components.  It was originally created to help prototype both the end-user features and the service APIS that would service various forms of front-end application.  The project depends on rapidInquiryFacility and rifServices, but is itself not a dependency for anything else.  Now that the rifWebApplication has matured, the early prototype embodied by the rifJobSubmissionTool project should be deprecated.

**rifDataLoaderTool**: this is one of the newest parts of the code base.  The RIF code base was first developed to support a prototype where the RIF database was already populated.  Once that had been completed, the focus of the RIF project switched to loading the database from its constituent data sets.  The rifDataLoaderTool and rifServices projects represent two fairly independent bodies of code that treat the RIF production database as a dividing line.  The rifDataLoaderTool knows nothing about the RIF scientist web application and does not care what studies are created from the data sets it loads. The rifServices project assumes all the denominator, numerator and covariate data sets it needs have already been loaded.  It does not care how they are transformed and loaded into the RIF database.  

The rifDataLoaderTool probably represents the most active area of future development.  In the short term, its greatest modifications will be SQL porting in the packages rifDataLoaderTool.dataStorageLayer.  There will be a close correspondence between classes and methods in the rifDataLoaderTool.dataStorageLayer.pg and rifDataLoaderTool.dataStorageLayer.ms.  In future, the tool’s front-end will be replaced by a web-based application that will use web services in a similar way code has been developed for rifServices.restfulWebServices
Entry Points into the Code Base

**taxonomyServices**: When Tomcat starts, it will first identify the taxonomyServices.RIFTaxonomyWebServiceApplication class, which is responsible for the first part of a URL used to make a service call (eg: http://localhost:8080/taxonomyServices).  From there, the next most important class will be taxonomyServices.RIFTaxonomyWebServiceResource, which will map the operation described in the URL to a specific method.  For example, as a Jersey web service resource, the command http://localhost:8080/taxonomyService/getTaxonomyServiceProviders will be associated with the method getTaxonomyServicerProviders. 

**rifServices**: The project supports two web service resources, each of which will be referenced in different parts of the rifWebApplication.  Finding where to start understanding the code for rifServices follows a similar path to the taxonomyServices project.  URLs beginning with http://localhost:8080/rifServices/studySubmission or http://localhost:8080/rifServices/studyResultRetrieval are related respectively to the RIFStudySubmissionWebServiceApplication and RIFStudyResultRetrievalWebServiceApplication classes found in the rifServices.restfulWebServices package.  The next part of the URL will correspond to a method in either RIFStudySubmissionWebServiceResource and RIFStudyResultRetrievalWebServiceResource classes in the same package.  Most of the important code supporting web services methods is located in the AbstractRIFWebServiceResource class.

In a typical web service method, the URL parameters are mapped to Java objects, which are then passed to instances of services whose APIs are defined in the rifServices.businessConceptLayer.*API interfaces.  The implementations of the services APIs are found in the rifServices.dataStorageLayer package and end in “Service”.  
I would recommend that when you create or modify a web service method, you first add it to the Service APIs and create automated test cases that test the API methods before you start to test whether the web services can be called through a URL.

**rifDataLoaderTool**: The main entry point into the code here will be rifDataLoaderTool.presentationLayer.interactive.RIFDataLoaderToolApplication, which causes the main GUI window to appear.  If you’re more concerned with tweeking the generated scripts that will be used to load finished files into the RIF production database, you may want to uncomment the “main” method that appears in rifDataLoaderTool.targetDBScriptGenerator.pg.PGDataLoadingScriptGenerator.  You can create new test data by modifying rifDataLoaderTool.dataStorageLayer.SampleDataGenerator and then calling the ScriptGenerator’s writeScript(...) method.  This will save you the time of creating an instance of the tool, loading a configuration file and then running it.  
If you’ve gotten to porting, then you may want to invest in making test cases that extend rifDataLoaderTool.test.AbstractRIFDataLoaderTestCase.  You can try to change the init(DatabaseType.POSTGRESQL) to init(DatabaseType.SQL_SERVER).  Then run the same test cases but switch the database type.

## Testing Facilities
Maven organises test case classes into project directories marked with src/main/test.  In order to run automated tests in Eclipse, you should can right click on the “src/main/test” folder, right click on an package within that folder, on a class or a method in that class.  In each case, you can choose “Run As...” and then select “JUnit Test”.

## Package-Level Coding Conventions
The following list shows some common phrases that will appear in the names of one or more packages appearing in various projects.  

**_presentationLayer_**: contains code that relates to front-end GUI components written using Java-Swing.  Although this layer will support features for guided data entry, it is not meant to do much validation.

**_dataStorageLayer_**: contains code that uses attributes of business class objects to assemble and execute queries against the RIF databases.  The data storage layer also converts the JDBC result set data back into business class objects.

**_businessConceptLayer_**: contains definitions for business classes and service APIs.  The classes each act as a data container, as well as defining important behaviour for creating, copying, comparing or validating business objects.  Service APIs use methods for actions and business class objects for parameters.  This is the kind of package that end-users will most likely be able to understand.

**__system__**: contains code that is used ubiquitously throughout one project.  Typically it will contain an Enumeration containing error codes that can be used to test for specific kinds of error scenarios that may be exercised in some automated test cases. It will also contain a properties class that fetches message strings from a *.properties file.  

**_fileFormats_**: contains code that will serialise or de-serialise business class objects for various file formats.  It will usually be XML and HTML.

## Class-Level Coding Conventions

**_*.fileFormats.[business concept]ContentHandler_**: are classes that will read and write fragments of XML corresponding to various business classes.  Reading XML is done using the SAX parser.  Sometimes the file will also contain write methods for creating HTML output.

**_*.fileFormats.*Reader/Writer_**: Reads and writes files, and are often what the File Menu items in applications will call.  Each of these classes will initialise and use the *ContentHandler class that corresponds to the top level of a document.

**_*.fileFormats.*FileFilter_**: usually used to help JFileChooser objects limit the kinds of files they let users specify.

**_*.businessConceptLayer.*API_**: contain definitions of service interfaces.  

**_*.dataStorageLayer.SQL[business concept]Manager_**: assembles SQL queries using Java business class objects, executes 
them using JDBC and stuffs JDBC result sets back into other business class objects.  Generally they manage CRUD routines for a single business concept.

**_*.dataStorageLayer.[Test/Production/Abstract]*Service_**: implements some service API defined in the businessConceptLayer.  They will be start with Test, Production or Abstract, which correspond to an implementation for a test service, an implementation for a production service or an Abstract class.  If it begins with Test, it will likely have other methods that delete records from the database in order to reset the state of a test case to what it was before it ran.

**_*.dataStorageLayer.*ServiceBundle_**: These are classes that create and initialise instances of the various manager classes in a way that ensures they can’t be used until all of the managers have been created.  They were developed as a convenient way to access manager instances and to minimise the likelihood they would be involved in concurrency error scenarios.

**_*.dataStorageLayer.*SubmissionStep_**: These classes contain code for a step in the work flow used to process CSV files.

**_*.restfulWebServices.[business concept]Proxy_**: these classes are designed to make it easy for the Jackson libraries to serialise a Java object into a JSON representation.  Proxies exist so that the implementations of their corresponding business classes do not have to be influenced in response to Jackson’s annotation conventions.  The only part of the code base in a project that should know anything about web services should be the *.restfulWebServices project.  The rest of the code, especially the business classes, should remain ignorant of it.

**_*.restfulWebServices.[business concept]Converter_**: a special type of convenience class that helps serialise collections of certain types of business objects into JSON.  

**_*.restfulWebServices.*WebServiceApplication_**: used by Jersey to identify an application, and this is where you’ll find the first phrases that will appear in a web service URL (eg: http://localhost:8080/rifServices/studySubmission) 

**_*.restfulWebServices.*WebServiceResource_**: these are used by Jersey and will help bind a fragment of a web service URL and its parameters to a method eg: getTaxonomyServiceProviders in the taxonomyServices. RIFTaxonomyWebServiceResource class.

**_*.*StartupOptions_**: These are the classes that will contain the parameter values that are taken from property files.

**_*.*StartupProperties_**: a class that gets values of property names that relate to the way some kind of RIF service will start.

**_*.presentationLayer.*Application_**: This will contain a Main class that will create the main GUI screen when it is run.

**_*Utility_**: Typically the code for these classes has come from a business class that was getting too large.  In an effort to curb having too much behaviour expressed in one class, methods were migrated to a utility class.

**_*Factory_**: Used to help centralise repetitive code used to instantiate objects.  They can also be used to help make the creation of objects safe with respect to concurrency concerns.

**_rifGenericLibrary.dataStorageLayer.*QueryFormatter_**: they capture code that creates parameterised templates of SQL queries that are meant to be used in PostgreSQL or SQL Server.  They try to hide implementation details of database porting and render the query in a format that is meant to foster readability.

## Property File Conventions
All property files will be stored in the src/main/resources part of a project and they will all end with
*.properties.  Each property file will contain name-value pairs that are used to help keep the text of
GUI and error messages out of the class files.

## How to...
### Add more code to the scripts that are generated when the Data Loader Tool is run?
If it's code that relates to the entire script, you'll want to add methods to rifDataLoaderTool.targetScriptGenerator.pg.PGDataLoadingScriptGenerator. If it only applies to the numerator, denominator or covariates, then consult the scripts for those themes in the script generator packages.