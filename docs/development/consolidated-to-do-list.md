---
layout: default
title: Consolidated To-do List
---

**Created 2018-09-03** by Martin McCallion, from various documents relating to Kev’s departure a year or more ago, and existing `TODO.md` documents. I intend to turn everything  here into issues in the GitHub project, as that will be a better way of managing the project into the future.

## The Original Documents

The original documents are referred to below as:
* [redev -- "The RIF Redevelopment"]({{ site.baseurl }}/development/The-RIF-re-development)
* [kevroad - "Kev’s Suggested Road Map with the Middleware"]({{ site.baseurl }}/development/Kevs-Suggested-Road-Map-with-the-Middleware)
* [todo -- "RIF to-do List"]({{ site.baseurl }}/development/TODO).


## The List

Links to the original documents are in brackets. My comments are in bold. 
<mark style="color:back;background-color:yellow">Peter Hambly comments are in Yellow</mark>

- Optimise performance on very large datasets ([redev]({{ site.baseurl }}/development/The-RIF-re-development)). 
  <span style="color:back;background-color:yellow">This work is envisaged to require:
  <ul>
    <li>Use of partitioning for Health data (especially denomninators);</li>
    <li>Potential for the tuning of extraction SQL, especially on SQL Server. If partitioning is used it is essential to verify that partition elimination 
    occurs so that the database only fetches the years of data actually required by the study, index are not disabled and the query plan remains 
	structurally the same;</li>
    <li>RIF leaflet maps perform acceptably at high resolutions (issue #78 Risk Analysis selection at high resolution (e.g. MSOA) does not perform acceptably; 
    also issue #66.</li>
  </ul>
  </span>
- Audit trail (kind of already there, in that SQL statements and similar are put into the log) 
  ([redev]({{ site.baseurl }}/development/The-RIF-re-development)). These are also logged in the database.
- “New technical features will include enhancement of flexibility by clearly defined XML interfaces, giving the RIF a batch mode for the first time 
   and allowing for the export of the data into other tools. Statistical processing will be built in modular manner so it can be easily extended. 
   Additionally, there are plans to integrate RIF risk analysis with the BREEZE AERMOD / ISC new generation air quality modelling system 
   (http://www.breeze-software.com/aermod/). It is also hoped to support Wind roses.” ([redev]({{ site.baseurl }}/development/The-RIF-re-development))
- “Existing RIF statistical functionality such as Satscan (for cluster dectection), INLA and LinBUGS/WinBUGS (for Bayesian Smoothing) will continue to be supported” -- **INLA is certainly used; not sure about the rest**. ([redev]({{ site.baseurl }}/development/The-RIF-re-development))
- Data Loader project ([kevroad]({{ site.baseurl }}/development/Kevs-Suggested-Road-Map-with-the-Middleware))
- “Eliminate `HealthOutcomeManager`” -- **I think this is out of date, as that class is actually used now**. ([kevroad]({{ site.baseurl }}/development/Kevs-Suggested-Road-Map-with-the-Middleware))
- ICD 9 (various) -- **This will be needed at some point [probably before end 2018] **.
- Improve logging ([todo]({{ site.baseurl }}/development/TODO)) -- **I also want to do this; todo says “PH done September 2017”, so it’s talking 
  about something else. I’d like to modernise the whole thing, switching to SLF4J  & Logback; and also vastly improve the internal handling**.
  - “Issues with log4j log rotation” ([todo]({{ site.baseurl }}/development/TODO)) -- **Should be fixed by the above, or can be addressed separately**.
  - <span style="color:back;background-color:yellow">Logging of SQL Exceptions needs to include:
    * SQL Statement;
	* Bind values;
	* Row number;
  - Log console output in batches to Front End logger;</span>
- Rengine not being shutdown correctly on reload of service ([todo]({{ site.baseurl }}/development/TODO)) -- **investigate**. Run a study, RIF service 
  web application will fail to load R DLL as it is still attached to an old thread.
  ```
  Cannot find JRI native library!
  Please make sure that the JRI native library is in a directory listed in java.library.path.

  java.lang.UnsatisfiedLinkError: Native Library C:\Program Files\R\R-3.4.0\library\rJava\jri\x64\jri.dll already loaded in another classloader
        at java.lang.ClassLoader.loadLibrary0(Unknown Source)
        at java.lang.ClassLoader.loadLibrary(Unknown Source)
        at java.lang.Runtime.loadLibrary0(Unknown Source)
        at java.lang.System.loadLibrary(Unknown Source)
        at org.rosuda.JRI.Rengine.<clinit>(Rengine.java:19)
        at rifServices.dataStorageLayer.pg.PGSQLSmoothResultsSubmissionStep.performStep(PGSQLSmoothResultsSubmissionStep.java:183)
        at rifServices.dataStorageLayer.pg.PGSQLRunStudyThread.smoothResults(PGSQLRunStudyThread.java:257)
        at rifServices.dataStorageLayer.pg.PGSQLRunStudyThread.run(PGSQLRunStudyThread.java:176)
        at java.lang.Thread.run(Unknown Source)
        at rifServices.dataStorageLayer.pg.PGSQLAbstractRIFStudySubmissionService.submitStudy(PGSQLAbstractRIFStudySubmissionService
  ```
  The solution is to restart tomcat. Server reload needs to stop R. This requires a ```@WebListener
  [Context Listener (javax.servlet.ServletContextListener)]```.  
- **Review all non-completed front-end items in ([todo]({{ site.baseurl }}/development/TODO)) with Peter**
- “Missing information not stored in the database: Retrieve information on a completed study. Used in the info button in disease mapping and data 
  viewer. The database cannot return all the required information. This requires changes to both the backend and middleware. [Planned PH]” -- 
  **discuss with Peter** ([todo]({{ site.baseurl }}/development/TODO)). This is believed to be complete
- Setting various web headers ([todo]({{ site.baseurl }}/development/TODO))
- Data Extract ZIP file. PH completed initial middleware support.
  * Risk analysis support: add shapes to maps, export shapes to shapefiles (issue #61);
  * Check manual R script using CSV files still work, and support for Unixen;
  * Improve map layout; US maps are wrong;
  * Add support for printing what the user selects in the mapping screens. Initial (default) setup and been done in the database. This will also
	include map centre and bounds;
  * Allow the use of more fields; handle fields with no data so they do not cause and NPE (pull #73 partially fixed this);
  * Allow the user to change the resolution of the images;
  * Additional outputs as required;
- Map synchronisation issues (#57):  
	* Choropleth map defaults disabled as being run before thee map data has complete loading. Synchronisation in the 
	  promises chains needs to be improved;
	* Zoom to study extent sometimes does not work on drawing the map;
- Add local basemap cache to RIF for standard Openstreetmap basemap. Will need a webapp for the files and the UTRL changed to be a local version