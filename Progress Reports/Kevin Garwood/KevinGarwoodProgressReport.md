#Kevin Garwood Progress Report 
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


Trying to be more brief in reports, will transfer design issue discussions to web site for RIF design documentation (see rifDesignManual

##June 8 to 12
   * finished a result in the data submission tool that allows results to be zipped to a file.
   * resumed work on the data loader tool, beginning to investigate XML format used to express a data loading job

##June 15 to 19
   * Hit a major design decision because there were two design tensions: (1) viewing the data loading activity in a data set centric view vs viewing it in a process centric view (2) 
     the anticipated technical skill level of RIF managers who used the command-line deployment vs 
     the skill of RIF managers who would use the graphical tool
   * Finalised the process stages of loading data from a CSV file to when it becomes published.

##June 22 to June 26
   * Developed new XML format for the data loader tool that favours a data centric view.
   * Developed a new data model to match the new XML format 
##June 29 to July 3 
   * Annual Leave

##July 6 to 31
   * Rewriting most of the data loader tool.  The rewrite of the data loader tool had a knock-on effect in most parts 
   of the code base.
   * Completely rewrote the UI from scratch, got rid of old prototype code because it's no longer relevant

##August 3 to August 7 
   * Holidays
##August 10 to August 14 
   * Improving auditing, packaging results into a zip file.
##August 17 to August 21
   * August. 19, 20: Annual Leave
   * Preparing learning and presentation tutorials for the CDC, scenario testing.

##August 24 - August 2th: 
   * CDC Atlanta Visit, attendance of Environmental Health Tracking Conference

##September 1 to November 30
   * working on other projects, no RIF work will be done in this period.
   
