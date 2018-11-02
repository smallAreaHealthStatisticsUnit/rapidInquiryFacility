These are some notes I've put together for handover.

# Installation and Build Issues
## Install Tool Chain.

1. Download and install PostgreSQL/SQL Server and create the RIF database.  We will assume for the installation
of the middleware that this has already been done.  The key things to take note of here are the host, port
database names and user/password combinations to support the middleware's interaction with the database.
2. Download and install Java.  Don't forget to set JAVA_HOME to the directory where the JDK begins.
3. Download and install Maven.  Ensure that you've added the path to Maven's "bin" directory to the path environment
variable so that you can type "mvn" on a command line in whatever directory you're in.
4. Download and install R.  Remember to set the R_HOME environment variable because this is what
rifServices.dataStorageLayer.AbstractRService uses to find out what path is needed to execute the R program
"RScript".  Remember that R typically installs both 32 and 64 bit installations.
5. Download and install Tomcat.  Make sure you set the CATALINA_HOME environment variable, because this is how
rifServices.system.RIFServiceStartupOptions finds out where it should look for some of its files.  Other classes
such as rifServices.dataStorageLayer.SQLSmoothResultsSubmissionStep indirectly uses CATALINA_HOME to figure out
where the R smoothing script Adj_Cov_Smooth.R is located.

## Installation and Configuration Issues
Most of the configuration issues related to the RIF middleware and data loader tool relate to either JDBC connection
activities or scratch space directories where the RIF can write to disk.  There are only three configuration files
that may need to be configured:
1. rifServices/src/main/resources/RIFServiceStartupProperties.properties
2. rifDataLoaderTool/src/main/resources/RIFDataLoaderToolStartupProperties.properties
3. taxonomyServices/src/main/resources/TaxonomyServicesConfiguration.xml


Let's go over each of these.  In RIFServiceStartupProperties.properties, the database connection properties relate to
the main RIF production database, which is currently called sahsuland. Two other notable properties are:

extraDirectoryForExtractFiles: this is where the RIF would dump a zipped file containing the original study submission,
extract and smoothed results to file.  I'm not sure if it's invoked at the moment but it did work before.

extractDirectory: This is where the RIF will generate a temporary .bat file and invoke it to run the smoothing.
odbcDataSourceName: This is the name of the ODBC data source you need to create to allow the R smoothing script access
to the RIF production database.  The smoothing script Adj_Cov_Smooth.R needs ODBC connection parameters so that it can
write smoothed results back to the database.

database.isSSLSupported: deals with support for SSL in the database.  The way you set this variable depends a lot on
how you may have configured settings in PostgreSQL or SQL Server.

In RIFDataLoaderToolStartupProperties.properties, there are these properties to consider:

databaseType: set to 'ms' if you're using SQL Server, or 'pg' if you're using PostgreSQL.
databaseName: this the name of the scratch database used by the Data Loader Tool, it is not the name of the RIF production
database.

databasePasswordFile: this is the file path of a file that will contain property-value pairs userID=XXX, password=XXX.
It resides somewhere on your system to help obscure login parameters.

The TaxonomyServicesConfiguration.xml files specifies the configuration of one or more configurations for taxonomy services. When you look at it, you'll notice that one of the parameters for the ICD 10 service is "icd10_ClaML_file" and it has a value of ExampleClaMLICD10Codes.xml.  We decided we can't actually ship the whole ICD 10 code list because of the potential for licensing issues with the way the WHO allows the codes to be distributed.  This is why the ExampleClaMLICD10Codes.xml file contains only a few terms - it uses the same structure as the whole ICD 10 file but it is designed to demonstrate rather than support user activities.  When you install your production version, you will need to replace ExampleClaMLICD10Codes.xml with whatever file you've created that contains all of the codes.

If you're wondering how an ICD 9 service could be made, you would make a class along the lines of taxonomyServices.ICDTaxonomyService and ensure it implemented the interface
rifGenericLibrary.taxonomyServices.TaxonomyServiceAPI.  Note that most of the code used to support taxonomy services is
generic and does not rely on RIF concepts - this is why it is found in the rifGenericLibrary.taxonomyServices package.


## Other Files
The only other files you may need to configure should already be covered in the database installation guide but are worth
briefly reviewing here.  pg_hba.conf is a file that PostgreSQL uses to control who can access what on the database. You
would look somewhere like C:\Program Files\PostgreSQL\9.4\data\pg_hba.conf to find it.  You'd proably alter this if the RIF complained about permissions problems or if an error was complaining that your IP address wasn't recognised as having
the ability to do certain things to the database.

The other file to consider is pgpass.conf, which lets you store your userID and password for different systems.  An entry
might look like localhost:5432*:kgarwood:kgarwood which means that any database served on host 'localhost', port '5432'
can associate user kgarwood with password kgarwood.  Typically this file is found in the "AppData" folder on your system
and that usually appears faded out when you see it in Windows Explorer.  You might find it at a path like:
C:\Users\kgarwood\AppData\Roaming\postgresql\pgpass.conf

## Maven commands to build all the RIF applications

To build all the RIF tools, you'll have to type the same command "mvn -Dmaven.test.skip=true install" in each of the
following directories, in the order I've suggested.

1. In rapidInquiryFacility/rapidInquiryFacility.
2. In rapidInquiryFacility/rifGenericLibrary.
3. In rapidInquiryFacility/rifServices.

And for the fourth step, use the command "mvn clean compile assembly:single":
4. In rapidInquiryFacility/rifDataLoaderTool

You will be able to see the products of the tools in the .m2 directory that Maven creates.  For example, it might be located at a file path like: C:\Users\kgarwood\.m2\repository.  In there, for every pair of groupId, artifactId described in each project's pom.xml file, you should see an entry in the directory.  You should see the following subdirectories:

[may vary] \.m2\repository\rapidInquiryFacility\rifGeneral
[may vary] \.m2\repository\rapidInquiryFacility\rifGenericLibrary
[may vary] \.m2\repository\rapidInquiryFacility\taxonomyServices
[may vary] \.m2\repository\rapidInquiryFacility\rifServices
[may vary] \.m2\repository\rapidInquiryFacility\rifDataLoaderTool

More important, you should now be able to see where all the build products you need:

1. [may vary] \GitHub\rapidInquiryFacility\rifGenericLibrary\target\rifGenericLibrary-0.0.1-SNAPSHOT.jar
2. [may vary] \GitHub\rapidInquiryFacility\rifServices\target\rifServices.war
3. [may vary] \GitHub\rapidInquiryFacility\taxonomyServices\target\taxonomy.war
4. [may vary] \GitHub\rapidInquiryFacility\rifDataLoaderTool\target\rifDataLoaderTool-jar-with-dependencies.jar

The rifGenericLibrary is the JAR file that contains the generic code used by all the other tools.  The rifServices.war
file contains the web application.  You need to put it in Tomcat's webapps folder.  The rifDataLoaderTool jar
is an executable jar file.  You should be able to double click on it and it will run the loader tool.  If it doesn't,
then you may want to go back to the properties files and see if something needs to be set.

