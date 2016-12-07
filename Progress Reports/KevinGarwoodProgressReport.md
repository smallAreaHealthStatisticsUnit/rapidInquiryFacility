#Kevin Garwood Progress Report 
Principal Work Area: **Java Middleware** 

##May 2015
### Responding to ad-hoc requests for new API methods to support the web-based study submission tool
In recent weeks, I’ve had to step up efforts to add new API methods to support various activities in the web client.  The addition of new calls is often easy and predictable but still laborious.  Sometimes I’ve had to change the code in response to discussions about whether the responsibility of an activity should rest in the middleware or in the database.

### Developing middleware calls for run study and create extract
I’ve had to do quite a bit of work trying to successfully add a study to the database and run it.  Both activities rely on procedures that are part of the database, not the middleware.  Although the middleware performs its own validation activities, the database performs its own set of checks as well.  Often it adds new data to sophisticated table views, which trigger various checks related to integrity of field values and access permissions of the user.

The test and fix cycle is exercising database code which has not been extensively tested and we’ve caught some errors in the database procedures.   

### Relaxing Validation
Early development of the middleware was informed by a Swing-based desktop application that helped tell us identify which API methods should be supported by the middleware.  During that time, we also developed our business concepts, and established all of the data fields which could appear in a study submission.  Because the desktop application was supported using Java Swing, the front end was able to build up an in-memory tree of business objects which were then passed to the service API methods.  The ability for the front end to retain so much detail about studies in these objects had an effect on the conversations between front end and middleware that have been reflected in the current service APIs.

The advent of the web-based submission tool has caused us to support conversations that contain far less data in the parameters.  For example, our AgeGroup business class supports the notion of a name, a lower limit and an upper limit.  The name field is really a code that corresponds to a primary key in the database.  The lower limit and upper limit are numeric fields and the lower limit must be less than or equal to the upper limit.  

In the desktop application, we could afford to submit name, lower limit and upper limit.  Therefore, the associated validation routines could also support checks to ensure that lower limit and upper limit were numeric and made a sensible range of values.

However, in the web-based application, the client application describes an age group solely through the name field.  When the middleware receives a study submission from the web client, the age group does not contain values for lower or upper bounds. 

In order to accommodate the web application, we’ve had to make the validation support a ‘strict’ and ‘relaxed’ policy.  If the fields have values, then they are subject to more detailed validation.  If non-critical fields do not contain values, we provide the option for not complaining that they are not valid.

Thus, the notions of ‘strict’ and ‘relaxed’ validation are influenced by the nature of the conversation that happens between the front end and the middleware.  In future, we may develop a batch tool that is able to accept a collection of study submissions that exist as XML files produced by some other application.  We may yet have to retain strict validation for this use case.  As well, we may find that we have to reuse concepts from the study submission tool in the data loader tool.  The data loader tool may need the ability to process more fully described versions of business objects than the web clients.  For now, we will retain strict and relaxed forms of validation to ensure that the design of our business concepts are not being warped too much by the needs of a specific type of front end (ie a web client).  

### Modifying documentation for the RIF web site
Although GitHub has some facilities for hosting a project Wiki, I thought this was inadequate for making a proper web site where we could describe a lot of technical information describing how the RIF worked.  Although GitHub has very advanced support for managing code, it seems to have fewer facilities to monitor traffic that would visit a web site about the project.  I’ve been investigating how to create a “gh-pages” branch which apparently will produce a GitHub web site, but I still have to look at it more.  I’ve used SourceForge, which at least long ago used to display readily accessible statistics about visits to web pages. However, they seemed to have changed their support.  If SourceForge's once easy-to-access statistics are no longer supported, we may migrate the project web site to GitHub as well.

For now, we want to establish a project web site that attempts to explain aspects of the RIF in terms of formal essays and design discussions more than through a sequence of wiki status updates.  The web site may be viewed a a kind of product of the RIF project, where we can discuss development issues in making this kind of complex geospatial health application.

##Week of June 8
###Summary
	* finished a method that allows a study and its results to be written to ZIP files
	* resumed work on features in the Data Loader Tool that would allow the processing of new data sets to be specified as 
	an XML file

###Finished method used to write study results to zip files
I finished method that uses the study ID to generate a ZIP file containing the original query, the extract files and 
information governance documents.  In future we will need to determine where zip files are generated.  At the moment it 
is coded as a user-configurable property that is read at startup of the main RIF services.  However in a production 
deployment, having the property file available within a web application folder of Tomcat would present a security issue. 

We have to decide how we want users to retrieve any zip files that are created.  In one scenario, when a scientist submits 
a study and there is no governance tool to delay the production of results, then a zip file may be streamed back to the 
browser client.  The user would then download the zip file to somewhere local.  The problem with this approach is that 
   1. it doesn’t work well when information governance does delay the production of results and 
   2. it doesn’t work well if running the study would take a long time.  
   
I personally favour having the user become used to a batch submission approach.  In this scenario, a scientists submit a 
study but do not expect immediate results back.  When the study is eventually processed and a ZIP file of the results is 
created, the RIF will then e-mail the scientists.  The e-mail message will include a link to access a secure FTP server 
which is available on another host that is not the same as the one running the web application.  Scientists would then 
log into the secure FTP site and retrieve an encrypted zip file.  ZIP files would be available in tightly controlled 
directory space that deleted any files after a period of a few days.

However for now, the zip files will be generated in a directory that is on the same machine as the browser client.  
Eventually we will change this setup.
Resuming work on Data Loader Tool

In response to changes in staff, I had switched emphasis of development from working on the Data Loader Tool to 
finishing more features to support the web-based Study Submission Tool.  I’ve now resumed working on the Data Loader 
Tool and have been working to address some challenges.  The first challenge is trying to support two ways of loading 
data: relying on a GUI application and editing an XML file.  Both must support the same basic workflow of steps that 
promote newly loaded data to become new parts of the RIF schema.  However, it is unclear whether these two methods 
should support exactly the same feature set and whether they should assume different levels of technical skill.

I feel that non-technical users will only use a GUI application that uses guided data entry features to strictly 
control the way data are promoted through different stages.  However, RIF managers who are more technically skilled 
will likely want to use a command-line tool that processes a data set based on an XML-based work flow description.  
A key part of designing either tool is to assess the technical skill of RIF managers, which is a topic that warrants 
further discussion.

For now I’m giving priority to having the RIF support an XML-based workflow engine that will load CSV files into 
areas of the RIF schema such as numerator health data.

##Week of June 15, 2015
I spent most of the week developing business classes and XML serialisation classes that support configuration options 
in the Data Loader Tool.  However, three issues have arisen:
*	determining what steps should be supported in the process of transforming data from a CSV file into a published data set
*	the influence  that command-line and GUI-based versions of the data loader tool have on the code
*	the design tension between data-centric and process-centric views of the data loader activities
*	the skill levels a RIF manager should have
*	the skill levels a RIF manager should have

The outcome of this work is a design discussion that I am trying to write up as part of the formal design documentation 
for the tool.

The first issue is defining steps in the workflow that will transform an imported data set into a data set that can 
be used as part of the RIF database.  

## June 29 to July 3 
   * Annual Leave

## July 6 to 31
   * Rewriting most of the data loader tool.  The rewrite of the data loader tool had a knock-on effect in most parts 
   of the code base.
   * Completely rewrote the UI from scratch, got rid of old prototype code because it's no longer relevant

# August 3 to August 7 
   * Holidays

## August 10 to August 14 
   * Improving auditing, packaging results into a zip file.

## August 17 to August 21
   * August. 19, 20: Annual Leave
   * Preparing learning and presentation tutorials for the CDC, scenario testing.

## August 24 - August 2th: 
   * CDC Atlanta Visit, attendance of Environmental Health Tracking Conference

## September 1 to November 30
   * working on other projects, no RIF work will be done in this period.
   * 
   
##Sept to December 2015:
I was mainly deployed on other projects at Imperial College.

##January to June 2016:
Whereas my work last year related to helping maintain the middleware to support the front end web applications, my work has shifted to developing the facilities for loading data into the RIF production database.  This has taken a lot of effort and the work is described in great detail at:

[Data Loader Tool Report] (https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifDesignManual/docs/RIF_ETL_Design_v1.pdf)

Currently, there are two issues I'm dealing with: exploring the network installation issues related to setting up the RIF database; making the taxonomy service report independent from the rest of the RIF code base; and doing production testing of taxonomy services.

#July 2016
Finished work on the taxonomy services.  The taxonomy services that are used by the web-based applications to retrieve taxonomy terms (eg: ICD-10 codes) now work properly and have been separated into a web application that can be run independently form the rest of the RIF.  It may now be regarded as a reusable piece of software that can be used to service other software projects.  Furthermore, it hides dependencies on future taxonomy technologies from the rest of the code base.

As part of the invoking R code from a Java program, I have worked to produce code that can be viewed as "sandwiching" the Bayesian smoothing algorithms for HET, BYM and CAR.  Currently, the inclusion of the R smoothing algorithms is focusing on interoperability rather than integration; Java builds up a command-line call to an R engine and invokes it.  The R script then runs as if it were invoked on its own and it receives command-line arguments supplied by the Java program.  My work has been limited to: (1) minimising potential problems in creating and invoking the command-line call to the smoothing script and (2) ensuring that when the algorithm finishes, the results are copied to the appropriate tables (they are of the form rif_studies.s[studyID]_map.  That work is still ongoing.  

My most significant progress for the month has been to take an inventory of existing web service calls that support our applications and to analyse all of the database queries that they use.  The analysis has allowed me to more accurately estimate the amount of work that will be required to support SQL Server porting activities.  Of the 54 middleware methods that are responsible for supporting end-user application features, it can be proven that 10 of them will not require any porting work because they do not use SQL queries.  A further 17 are unlikely to require porting because the SQL queries they use are already likely to be completely compliant with the most basic parts of the SQL standard.  

2 may require some porting because they have query fragments that may not be equally supported between PostgreSQL and SQL Server.  23 existing methods would require that code from a database query be moved into Java-based middleware classes.  However, some of these methods may prove unnecessary to support a production environment and others may be able to be deleted in favour of having them easily rewritten in the front end.  1 method needs to be implemented and 1 requires that the database be modified to support a new concept: calculation methods.  

We are currently investigating three ways of simplifying the work needed to be done in the middleware: reviewing methods to ensure they are justified by production use and not just from prototyping efforts; moving some methods into the front end; and reviewing whether we can retrieve large blocks of data from result tables to reduce the number of methods needed to support rendering smoothed data.

#August 2016
The main work on the RIF related to enabling middleware methods that help support visualising smoothed data extract results.  The work I’ve done on that has not just involved coding new middleware methods but also making design decisions to help expedite the need to make the RIF work on both PostgreSQL and SQL Server systems.  The need to find a simple solution was made more pressing because I was ill for ten days.

This is the main summary of work, and you can stop here if you just want to see the status report.  But what follows is a more detailed discussion about decision decisions I made and the effects it has had on the system design.
Currently there are a number of database procedures in PostgreSQL which, given a study id, will retrieve smoothed results.  These procedures have been designed to retrieve parts of the result sets to facilitate showing results as pages in the front end.  However, considering the factors of effort needed to maintain the code for these features and to maintain that code for both PostgreSQL and SQL Server platforms, I’ve had to re-evaluate using them.

###The Design Challenge of Creating an Effective Way for Retrieving Smoothed Result Sets
The system needs to support an effective way of communicating result sets between the middleware services and the web-based front end applications.  This ‘dialogue’ describes what we’ll call a round trip of communication.  That trip has the following steps:
1.	 The web client uses web page controls to create a URL and issue a request to a RIF web service.
2.	The RIF web service uses parts of the URL path to determine what middleware operation it needs to call.  
3.	The RIF web service converts URL parameters into Java objects that can be passed to Java-based service classes
4.	The appropriate method in the Java-based service class constructs an SQL query to retrieve data from the database
5.	The database returns table rows to the Java-based service classes
6.	The Java-based service classes package the tabular data into results that are expressed in Java objects
7.	The RIF web service serialises the Java objects in a JSON format that can be used by the web-based front clients
8.	The RIF web service returns the JSON result to the web client
9.	The web client renders the results.

These steps occur during almost every request that the web application may make to the RIF middleware.  Part of designing the middleware is to provide methods that support ‘conversations’ that are appropriate for the characteristics of the end-user activities. Here we consider two approaches: “Talk Once” and “Talk Often”. Then we evaluate how well they work to serve project needs.

####Talk Often
In many cases, the ‘dialogue’ involves fetching chunks of large data sets in order to minimise the time and memory involved with transferring the data to and holding data in the front-end.  The dialogue is typically expressed to end-users through controls that let them select the first, previous, next and last result pages.   For example, when a user presses “next”, the web client issues a request to get the next set of N results that it can present in a display.

The main benefit of this approach is that the middleware fetches the minimum amount of data that users will see at any given time.  Retrieving data in small chunks rather than all at once may reduce the time needed for SQL queries to execute.  The approach will definitely mean that the middleware does less work translating SQL table data into Java objects and then to a JSON format.  It also means that the web service will return a smaller amount of data to the web client and that the web client can operate on client machines that may have little memory resources.

The main drawback of the “Talk Often” approach is that some part of the system must remember where a user is within a result set when the next chunk of data is being retrieved from the database.  This can add extra complexity to the code base that may require additional thought to how it could be made portable across SQL Server and PostgreSQL databases.

One way of doing this would be to have the client application remember a current start and end index for the portion of the data set it is currently visualising.  These indices can be used to parameterise the next request.  The middleware can then create and execute SQL queries that are parameterised by limits it may not have to remember, which means it is easier to manage its sense of state.  However, this approach may result in using a lot of computing resources to do similar queries.

Another way of doing this is to have query results persist so that the work done to retrieve results is done once.  This is usually accomplished with the use of cursors, which provide a mechanism for navigating through a result set.  When we talk about cursors in the middleware, we need to identify two kinds that could be used: database cursors and JDBC cursors.  Database cursors are managed within the database.  Query results are retrieved into temporary tables and the database retains memory of where the currently accessed record points to in the table.  Database cursors manage SQL tables, whereas JDBC cursors manage current record positions within a Java-based ResultSet that holds query data.  

The main challenges with using JDBC cursors is that I’ve had difficulty getting them to demonstrate fetching part rather than all a data set and that even assuming it would work, they appear to only work by default with result sets that only go forward.  You can configure them to move the current record forward (ie: “next”, “last”) or backward (ie: “previous”, “first”) by making them Scrollable but I’ve not had time to try this out and determine if there are performance problems with it. My main concerns with using database cursors are whether their use would present database portability problems and how references to cursors would be managed by the middleware. 

####Talk Once
With talk once, the web client requests the middleware to retrieve the entire data set.  The client then becomes responsible for navigating through pages of results.  The main benefit of this approach is its simplicity: its solution requires a simple SQL Select statement which will present no portability issues between SQL Server and PostgreSQL.

Its drawbacks are that it may add more complexity to manage data in the web client, it requires clients that have a lot of memory to hold all the data and the initial request for the data set may be time consuming and memory intensive.

####Evaluation  
I believe that we would have a system that behaves better for very large data sets if we use the Talk Often approach to engineering the dialogue between the web clients and the middleware.  However, the main problem here is that it adds more complexity to the code base and I have to spend more time considering any portability issues that may arise from cursor-based fetch calls to SQL Server or PostgreSQL databases.  

The Talk Once option will likely result in performance problems for large data sets.  But its main appeal is that it can be done more quickly within project milestone periods.  We currently have a good front-end developer (Dave Morley) who would be able to add extra support for having the web applications remember a whole data set.  I was ill for ten days and I’m under further time constraints from other projects.  

Finally, when you’re designing for performance, the golden rule is to do it when you need to and to make sure you benchmark to justify decisions for adding more complexity.  However, for the next few months, we would only have the data sets from SAHSULAND to judge performance and they were not designed to stress test a system.  It begs the question: how do you design for performance when your test data set has not been designed to test aspects of performance? 

We can spend more time developing more test data and shortly we hope we can load large real data sets into the RIF.  These data sets could then gauge the value of savings that would be realised by retrieving results in chunks rather than all at once.  Until then, we observe that retrieving all of the current demonstration data sets takes about two seconds, which seems acceptable.

I observe that we can take steps to reduce the amount of data we retrieve.  First, it doesn’t make sense to return a lot of the columns that appear in the smoothed data sets; they exist mainly to facilitate linking with other database tables and other fields look as though their value is the same for all rows.  Second, the results contain far too many significant digits than are warranted by the calculations.  By reducing the number of digits, we reduce the length of the JSON data that are returned to the web client.
The Work.  I’ve developed three methods that can retrieve all the data set or specific columns from a data set. The work for managing paged results will then be moved to the web clients.

##Sept to October 2016
Created middleware methods to support the data viewer tool.  Trying to fix bugs related to the overall process of submitting a study, having it register in the database, having it successfully smoothed by the R program, and advertising that it has been done to the data viewer.  All of this is working, except for two problems that we expected would come up during test.  First, the run_study method in 
the database has not been well tested with various study submission test data.  Second, we have experienced some integration issues in coordinating the activities of the R-based smoothing code with the extract and map tables that are created the process of running the study.

I've also spent three separate weeks working on an unrelated project.  Currently in the process of training up some of the other developers so they may make more changes to the middleware code.

##November 2016
I’ve been doing knowledge transfer and I’ve spent time testing, fixing and enhancing the RIF Data Loader Tool with SAHSULAND CSV files.
Initially, work on the RIF was divided by layer in a three-tier architecture style that made separation of concerns between the front-end development of JavaScript-based web applications, Java-based middleware and PostgreSQL-based back-end work.  As the web applications reach a greater level of maturity and as the PostgreSQL-based code to support the back-end stabilises, the work needed to maintain and enhance the large codebase is changing in three important respects.  

First, more effort is shifting to sharing maintenance of the Java-based middleware across more members of the team.  That has required me to identify logical divisions in the architecture that can support parallelisation of work amongst team members in a way that maximises the autonomy of their work.  I’ve divided the Java part of the codebase into two independent areas: code that assumes the RIF database is populated and is used to support the web application used by scientists and code that assumes the RIF database is empty and supports the Data Loader Tool that will be used by RIF managers.  I’ve spent time identifying a distinct work path for two people on the team who don’t have a strong previous background in either Java or the architecture and coding patterns used to support the middleware.  So some of the recent work I’ve been doing has focused on knowledge transfer to other members of the team.

Second, the architecture of the middleware has been modified to support database porting activities.  I’ve gone through the rifGenericLibrary and the rifDataLoaderTool subpackages, and isolated parts of the code that may have to be modified to support both SQL Server and PostgreSQL back-ends.  An important design decision has been how to set up the code so that database porting efforts can minimise my own efforts to modify data loader tool code that may need to be changed in response to testing it with SAHSULAND CSV files (see next item).  I’ve gone through the classes and signposted code fragments that may warrant special attention for porting efforts.  I’ve then created mirrored packaging for both both SQL Server and PostgreSQL.  

Margaret, who has more extensive knowledge of differences between SQL Server and PostgreSQL, should now be able to modify and test code in the SQL Server packages without much affecting modifications I may have to make in testing and fixing the Data Loader Tool. 
With respect to code used to support the web applications, I’m preparing to hand over code maintenance of middleware methods to Dave, who has been spending time making a great set of interactive web forms.  Rather than trying to create new middleware methods myself to support web app features, I’m in the process of making knowledge transfer materials that can make it easier for him to make these changes himself.

Third, the Data Loader Tool is now being tested, fixed and enhanced as we try to use it to populate an empty version of the RIF database with a set of CSV files that contain all the data for SAHSULAND. Currently, these files are being used as part of Peter’s database build script.  However, we need to test that the tool is fit for purpose by creating the same SAHSULAND data set as if we’re loading it from scratch.

This activity has revealed some important enhancements that will need to be made to the Data Loader Tool.  The tool’s feature for loading geospatial data will be simplified so that it imports metadata about geographies from a metadata file produced by Peter’s shapefile simplification service.  RIF managers will now first process geospatial data sets using his service, and the geography and geographical resolutions it identifies will be identified so that the metadata can tell the Data Loader Tool what geographical resolutions can be associated with data set configuration options.  

Up until now, the Data Loader Tool has been designed to process any one CSV file independently of any CSV file that comes before or after it in processing.  However, the needs for numerators to be linked to a corresponding denominator, and to link notions of geography and health theme for these kinds of data sets – now require us to impose an order of creating the health-related data sets.  The tool will now enforce an ordering of specifying records in the following order: health themes, denominators, numerators and covariates.

Providing explicit support for these mechanisms requires that the DL need to check for and serialise dependencies.  For example, suppose a RIF manager creates a denominator and then a numerator that refers to it.  The tool would need to prevent the RIF manager from removing the denominator when other data sets refer to it.  Because the Data Loader Tool stores configuration details of data sets in an XML file, we need to modify the way serialisation is done so that dependencies can be reconstituted when configuration files are loaded.

Prescribing explicit support for dependencies also helps answer an important design issue: should the tasks of loading the RIF database be spread out in multiple configuration files or one? For example, one configuration file could load files for a specific set of years or for only numerator tables.  By requiring more explicit links between descriptive parts of the data loading activity, it is most convenient that all data sets used for the RIF database are specified in a single configuration file where the integrity of link references can be most easily managed.

Supporting dependency management will require enhancements to multiple parts of the Data Loader Tool, and add to feature enhancements that are not related to the more pressing task of porting the SQL fragments that are created by the middleware.


