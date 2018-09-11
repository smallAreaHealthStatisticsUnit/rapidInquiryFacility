---
layout: default
title: General RIF Installation Notes for the Middleware and Data Loader Tool
---
by Kevin Garwood

This page covers some installation notes for setting up parts of the RIF that are not already covered by the database installation notes.

# Building and Installing the Main RIF Web Application


# Step 1: Set the properties for the RIF Service
Now that you have checked out the code repository, you may need to make some changes to the parameter values that
rifServices uses to support a web application.  Most of these will relate to the database connection properties
that rifServices will use to interact with the RIF production database that contains all the tables that are used
by the scientists who are creating studies.



# Step 2: Use Maven to Build the RIF Web Application
When Maven is trying to find things, it downloads and moves what it needs to a directory called .m2.  You can usually
find it somewhere like: C:\Users\kgarwood\.m2.  In this repository, we have some dependencies between projects:
rifServices depends on rifGenericLibrary and rapidInquiryFacility sub-projects.  In order for Maven to find these
dependencies, they must first be installed.  Create a command-line window.  We are going to execute the following
command in multiple directories:

"mvn -Dmaven.test.skip=true install".

The 'skip' part tells Maven to not bother running any JUnit test cases.  The install part means that whether the
product of the build is a pom.xml file, a jar file or a war file, it should be installed in the .m2 directory.

1. Go to rapidInquiryFacility/rapidInquiryFacility and type the command.
2. Go to rapidInquiryFacility/rifGenericLibrary and type the command.
3. Go to rapidInquiryFacility/taxonomyServices and type the command.
4. Go to rapidInquiryFacility/rifServices and type the command.

To see what this sequence of commands has done, go to the .m2\repository\rapidInquiryFacility subdirectory. You
should see a number of folders, each of which will correspond to an artefactID shown in one of the project POM
files.  This build process is trying to treat the build products of each sub-project as if they were any other
kind of dependency to consider.

Now grab the product that matters most.  Go to the target directory for rifServices
(ie: rapidInquiryFacility/rifServices/target).  Copy the war file into Tomcat's web application directory
(eg: C:/Program Files/Apache Software Foundation/Tomcat 8.0/webapps).  If Tomcat is already running, you should see
that suddenly a folder called "rifServices" appears, which matches the name of the *.war file you just added to
the webapps directory).


# Building and Installing the Data Loader Tool

**Step 1**: Ensure the scratch database for the Data Loader Tool has been created.
Make sure you have installed PostgreSQL correctly and that you are able to make a new blank database.  Currently,
that is called 'tmp_sahsu_db'.  This is going to be the temporary database that the Data Loader Tool uses for
scratch space as it transforms data files that it imports.  We need to do more to set this up but we need a code
file in the repository to do it.

**Step 2**: Ensure you have checked out the repository
Go to the main code repository page and choose how you want to acquire the RIF code base.  You can either use the
Github Desktop tool to clone it, or you can download it all as a zip file.
https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility

Now go and run the script that is located at: rapidInquiryFacility/rifDataLoaderTool/PGCreateRIFDataLoaderToolDatabase.sql
You can either try to run the script from a command line, or you can use pgAdminIII, open a query window for the
'tmp_sahsu_db' database and copy-and-paste all the text.  Then execute the query.

However you do it, the result is that a few critical tables and a bunch of functions will be created.  The Data Loader
Tool assumes that these exist.  Take note of the host and port that you would access the database.  Typically, it
would be host=localhost and port=5232 for PostgreSQL. In SQL Server, port would typically be 1433.  You'll need these
pieces of information for the next step.

**Step 3**: Set database connection parameters for your database.
Edit the properties file located at:
rapidInquiryFacility\rifDataLoaderTool\src\main\resources\RIFDataLoaderToolStartupProperties.properties file.

You may need to alter a few of the properties before the build can produce an executable that will work.  These
are:
1. databaseType: by default it's 'pg' for PostgreSQL but in future it will also need 'ms' for SQL Server.  This
property tells the Data Loader Tool whether it should use the pg.* property values or the ms.* property values.
2. databaseName: We use tmp_sahsu_db but in future it may be renamed.
2. host: typically localhost but it refer to a network host as well.
3. port: typically 5432 for PostgreSQL and 1433 for SQL Server.
4. databasePasswordFile: The full path of a text file that contains the userID and password of the user that the
Data Loader Tool will act as when it tries to create and process tables in tmp_sahsu_db. An example path might
be C://rif_scripts//db//RIFDatabaseProperties.txt and that file will contain two lines that might look like
this:

userID=postgres
password=wilbur1

The Data Loader Tool will now know enough to determine how to connect with the tmp_sahsu_db that you have set up
already.  This property file will automatically be included in the build we do to create an executable jar file,
but to do that, we'll need Maven.

**Step 4**: Ensure you have installed Maven
We've set up the database, and now we want to create an executable image for the application that uses the database.
For that, we'll rely on maven to produce a single executable jar file that you should be able to double click on to
start the Data Loader Tool Application.

Download Maven 3.3.9 and ensure that you have added the path to the mvn executable to your path variable.  For example, you might add something like C:\apache-maven-3.3.9\bin.  Ensure that you can type 'mvn' from the command line, no matter
what directory you're in.

**Step 5**: Use Maven to build an Executable Jar File
Open a command-line window and navigate to the rifDataLoaderTool directory in the Github repository.  It might look like:
C:\Users\kgarwood\Desktop\GitHub\rapidInquiryFacility\rifDataLoaderTool.  You should see a pom.xml file there that will
contain all the details Maven needs to produce the jar file.

Type: "mvn compile assembly:single" and press Enter.  The script will probably take awhile to run as it gathers all
the dependencies it needs to work.  When the script has successfully completed, you should be able to see a new
JAR file called rifDataLoaderTool.jar.  It will be located in the target directory Maven uses to generate its build
products.  For example, it could be something like "C:\Users\kgarwood\Desktop\GitHub\rapidInquiryFacility\rifDataLoaderTool\target\rifDataLoaderTool-jar-with-dependencies.jar"

Now double click on rifDataLoaderTool-jar-with-dependencies.jar.  It should work now, but remember if you take it away to another machine you will need to make sure you have the same file available in the same location that is expected in the
databasePasswordFile property that appears in the RIFDataLoaderToolStartupProperties.properties file we described
in Step 3.
















