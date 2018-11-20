---
layout: default
title: RIF To-do List
---

* David Morley, September 2017
* Peter Hambly, October-December 2017
* Peter Hambly, March 2018,
* Peter Hambly, September 2018

# Contents

- [Java Middleware](#java-middleware)
- [JavaScript](#javascript)
- [Database](#database)
  - [Missing information not stored in the database](#missing-information-not-stored-in-the-database)
  - [Postgres Port](#postgres-port)
  - [SEER test dataset](#seer-test-dataset)
- [TileMaker and TileViewer](#tilemaker-and-tileviewer)
- [Information Governance Tool](#information-governance-tool)
- [Data Loading Tool](#data-loading-tool)
- [Security Testing](#security-testing)
- [TODO](#todo)
  - [Milestones](#milestones)
  - [April to May 2018](#april-to-may-2018)
  - [June to July 2018](#june-to-july-2018)
  - [August to September 2018](#august-to-september-2018)
- [Wish list](#wish-list)
  - [Middleware Wish list](#middleware-wish-list)
  - [Front End Wish list](#front-end-wish-list)
- [Issues](#issues)
  - [Middleware Issues](#middleware-issues)
  - [Front End Issues](#front-end-issues)
  - [Database Issues](#database-issues)
  - [Data loader Issues](#data-loader-issues)
  - [Documentation Issues](#documentation-issues)

# Java Middleware

1.	DataStorageLayer is split into ms and pg. All web services have either /ms or /pg in the URL to signify which database is being used.
    This needs to be refactored into a common super class for both databases and dispense of the two separate lumps of code for each
	database type. The actual differences between ms and pg are not very big. The RIF works the way things are now, but there is a lot
	of duplication of code and it will cause a huge maintenance problem in the future.

    Done MM.
	
2.	The R script uses ODBC. Now that JRI is used this can be changed to JDBC to allow Linux version of RIF. The JRI code has now been
    isolated into RIF_odbc.R prior to conversion.
	
	Done MM. Uses JDBC on Postgres, ODBC on SQL Server

3.	Improved logging. [PH done partially September 2017]; including correct error recovery tracing. Note there are issues with
    log4j log rotation.
	
	Some improvements to log rotation by using one log file per service. Added front end logger at the same time. Logging no longer "hangs" at the 
	end of the day (the log was only written if you shut down Tomcat nicely); but log rotation and the delay to write are still problems.
	Any solution 

4.	Still A LOT of redundant, dead-end, stubbed, duplicate or unused code resulting from the lack of initial scoping as to what the RIF
    was going to do.

5.	Risk Analysis: Done, but more work needed on maps (do not include selection shapes)

6.  Data Extract ZIP file. PH completed initial middleware support.

	* Risk analysis support: add shapes to maps, export shapes to shapefiles;
	* Check manual R script using CSV files still work, and support for Unixen;
	* Improve map layout; US maps are wrong;
	* Add support for printing what the user selects in the mapping screens. Initial (default) setup and been done in the database. This will also
	  include map centre and bounds;
	* Allow the use of more fields; handle fields with no data so they do not cause and NPE (pull #73 partially fixed this);
	* Allow the user to change the resolution of the images

7.  Rengine not being shutdown correctly on reload of service:
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

# JavaScript

1.	Code mostly works - may need some tidying in places. Possible refactor the submission mapping tools (rifd-dsub-maptable) to fit in with the Leaflet stuff used in disease mapping and data
    viewer as there is a lot of duplication. It works fine as it is though, just a maintenance issue.
	Especially: rifp-dsub-maptable.html, rifs-util-mapping.js

2.	Export map to png functionality. [Done PH using Java]

3.	Save rifSubmission to text file (.JSON). This is currently done with a directive in JS and as such is a bit temperamental because of
    various browser/security issues. We need a new middleware method to save the rifSubmission.txt as a
	.json file [see above]. [Done PH - implemented using middleware]. Files are now in JSON5 format to make fore readable

4.	Some of the references to parent and child scopes are messy and non-angular and may need looking at. But it does work. Generally best 
    replaced by services (e.g. the AlertService to access the AlertController)

5.	Main CSS needs removal of redundant code

6.	At some point, Leaflet version used will need to be updated to v1.2.x. Breaking changes to the RIF expected.

7.	New D3 output graphs as-and-when requested by users. It is likely that when risk analysis is done, new graphs and/or tables will be
    needed.

8. A download link is required to download the actual ZIP file [this requires a Middleware method too!] [Done PH. Is intelligent]. May need a [redo] 
   download button

9. When you change the geography the numerator and denominator do not change and need to be
    changed manually; [Done; bug fix]

10. Login initialisation errors if a) you shoot tomcat whilst logged on [the RIF must be reloaded]
    and b) spurious complaints caused by the process of logging out; e.g.
	```
	ERROR: API method "isLoggedIn" has a null "userID" parameter.
	ERROR: Record "User" field "User ID" cannot be empty.
	```

    Normally reloading the RIF allows the user to logon again; although if the user is already logged on
    the Middleware will not let the user log on a second time; [Done: 29/3/2018 PH]

11. The newest study completed when the RIF initialised is displayed, this does not change with even when
    the user goes to the tab for the first time;

12.	Add save study/comparison bands to file. Upload from file must have fields named ID,Band and can have
    other fields (e.g. NAME). Names are restricted and a save to file option would be good.
    File: rifd-dsub-maptable.js; [Done PH 3/9/2018]

12. Map synchronisation issues: [First set done: PH 18/12/2017]
	* A change in geography from one study to another causes chaos in the data viewer and disease
	  mapping tabs. The best solution is to set both tabs to the same geography then set up the maps
	  and finally zoom to map extent. This will fix the map to the correct location;
	* Chrome is the worst browser and often does not refresh unless the map setup is reapplied;
	* Needs caching (i.e. the middleware slowes it down). This is particularly noticeable on
	  slow systems;
	  
	Second set:
	* Fixed problems when changing from one geography to another between studies;
	* Choropleth map defaults disabled as being run before thee map data has complete loading. Synchronisation in the 
	  promises chains needs to be improved;
	* Zoom to study extent sometimes does not work on drawing the map;

13. The map hover displays the *area_id* property and should also display the *name* property if it is available [Done: 7/11/2017]. See also 
    issue #65;

14. Null zoomlevel error, appears when moving between the data viewer and the disease mapper. Made much more
    likely by changing from one geography to another! [Partially done: PH 18/12/2017]
	```
	11:58:59.708 XML Parsing Error: no element found
	Location: https://localhost:8080/rifServices/studyResultRetrieval/ms/getTileMakerTiles?userID=peter&geographyName=USA_2014&geoLevelSelectName=CB_2014_US_COUNTY_500K&zoomlevel=null&x=1&y=0
	Line Number 1, Column 1: 1 getTileMakerTiles:1:1
	```

    Appears to stop the "zoom" to map and then to study extent;

19. Memory leaks [Done: PH 18/12/2017]

	* Layer orphan issues (fixed temporarily by removing the layer):
	  ```
		if (mapID === undefined) { // Occurs only on SQL Server!
	  //							  Do nothing!
			$scope.consoleError("Null mapID; layer options: " + JSON.stringify(layer.options, null, 2));
			if (layer !== undefined) {
				layer.remove(); 	// Remove
			}
		}
	  ```

	  This appears to only occur in SQL Server; this is probably because the SQL Server
	  code is faster.
	  This has dealt with the symptoms, **not the cause**. The direct cause is the layer add
	  function is being called before the map has initialised properly. Taken with the
	  *Null zoomlevel error* there is a synchronisation bug in the leaflet code. This issue
	  was experienced with the TileMaker code and the fix - stronger asynchronous control is
	  the same (i.e do not rely on Leaflet to do it all). This will have the effect of
	  some counties not being displayed (say 40/3233).
	* Some work is still needed on memory leaks (objects never going out of scope).
	  Disease mapping is leaking badly (3GB/hour) doing nothing when displaying a large US
	  study. The data viewer population pyramid also has a one off leak every time you change
	  the year. Logging off does not release resources so maps are not being destroyed on
	  logoff (other events will need to be checked). Almost certainly related to the above.
	* Memory leak analysis in Firefox shows the function *link()* in the Angular directive
	  *rifd-dmap-d3rrzoom.js* (SVG relative risk maps) is responsible for about 7% of the leaks,
	  the 93% of the leaks have no stack available;
	* Memory leak analysis in Chrome is by object, not by stack but shows that:
	  * Function *resetTable()* in *rifc-dsub-params.js* is leaking
		```
			resetTable = function () {
				$scope.thisICDselection.length = 0; // This causes a leak!
				// $scope.thisICDselection.splice(0, $scope.thisICDselection.length);
					// A fix
			};
		```

		This may be a Chrome specific bug; as the original code **should** work. It is
		a widespread problem in the code; e.g. *clearTheMapOnError(mapID)* in
		*rifc-util-mapping.js*; and more testing is needed to determine if this really causes
		a leak! 60 javascript files were found to contain the string ```.length=```.
	  * Lots of *_map()* objects leaking re-inforcing the above observations
	  * SVG related leaks as seen above.
	* Leak actual caused by $watch synchronmisation in disease mapping D3 graphs. This was redrawing
	  the graphs several times per second! The actual leak was not fixed since it is small. The only conclusion is to avoid
	  overly complex directives as self modification will cause a re-run!
	* Code was restructured to remove mal-synchronisation issues and to parallelise tile and data fetching

  **BEWARE: THIS ISSUE COULD RETURN: ALWAYS TEST CHANGES TO ANY MAP CODE FOR LEAKS**
  
# Database

## Missing information not stored in the database

1.	Retrieve information on a completed study. Used in the info button in disease mapping and data viewer.
    The database cannot return all the required information. This requires changes to both the backend and
	middleware. [Done PH]

2.	I'm not sure the statistical method is being stored in the database correctly, that is it is always NONE.
    [Done PH]

## Postgres Port

1. The Postgres port is slow. This is because by the logging function *rif40_log()* is not compiled in Postgres and therefore slow. Needs to
   either be made acceptablely fast or the debug messages commented out. This particularily effects triggers. The Postgres SEER data is also
   extracting 3x slower than SQL Server; this requires further analysis; [Done: 13/11/2017; room for improvement]
2. Data loading scripts needs to be made make independent - i.e. run from a single script like the SQL server ones, with one file/object;
3. Patches need to be merged.

## SEER test dataset

1. A large scale test dataset of real data is required for testing. The US County level SEER data was selected. [Done: both ports]

   * Needs to be used as a test dataset for the data loader
   * Manual data loading and required data format and cleaning need to be specified

   The SEER cancer data has 9,176,963 rows and requires 800MB for the data and 1.3GB for the indexes.

   The test study *1004 SEER 2000-13 lung cancer HH income mainland states.json*:

   * States were chosen so they mapped compactly:

     * California
     * Connecticut
     * Georgia
     * Iowa
     * Kentucky
     * Louisiana
     * Michigan
     * New Jersey
     * New Mexico
     * Utah
     * Washington;

   * Years: 2000-2013 (the maximum period available accross these states);
   * Covariate: median head of houshold income, quntilised;
   * Both sexes;
   * All ages;
   * Lung cancer:

     * C33: Malignant neoplasm of trachea
	 * C340: Main bronchus
	 * C342: Middle lobe, bronchus or lung
	 * C341: Upper lobe, bronchus or lung
	 * C343: Lower lobe, bronchus or lung
	 * C348: Overlapping lesion of bronchus and lung
	 * C349: Bronchus or lung, unspecified

   * Full Bayesian smoothing

   Data is extracted SQL server in 35s and R INLA in 40s.

# Security Testing  [Done: PH 24/11/2017]

205 unique URLs were tested using OWASP ZAP (https://www.owasp.org/index.php/OWASP_Zed_Attack_Proxy_Project) Ajax Spider

The report is at: ({{ site.baseurl }}/development/owasp_zap_test1.md) and the URL list
tested is at: ({{ site.baseurl }}/development/url_list2.txt).

One medium and three low medium isses were highlighted for fixing.

## Medium Issues

1. X-Frame-Options Header Not Set

   X-Frame-Options header is not included in the HTTP response to protect against 'ClickJacking' attacks.

### Solution

* Most modern Web browsers support the X-Frame-Options HTTP header. Ensure it's set on all web pages returned by your site (if you expect the
  page to be framed only by pages on your server (e.g. it's part of a FRAMESET) then you'll want to use SAMEORIGIN, otherwise if you never expect
  the page to be framed, you should use DENY. ALLOW-FROM allows specific websites to frame the web page in supported web browsers).

### References

* http://blogs.msdn.com/b/ieinternals/archive/2010/03/30/combating-clickjacking-with-x-frame-options.aspx

## Low Medium Issues

1. Incomplete or No Cache-control and Pragma HTTP Header Set

   The cache-control and pragma HTTP header have not been set properly or are missing allowing the browser and proxies to cache content.

### Solution

* Whenever possible ensure the cache-control HTTP header is set with no-cache, no-store, must-revalidate; and that the pragma HTTP header is set
  with no-cache.

### Reference

* https://www.owasp.org/index.php/Session_Management_Cheat_Sheet#Web_Content_Caching

2. X-Content-Type-Options Header Missing

   The Anti-MIME-Sniffing header X-Content-Type-Options was not set to 'nosniff'. This allows older versions of Internet Explorer and Chrome
   to perform MIME-sniffing on the response body, potentially causing the response body to be interpreted and displayed as a content type other
   than the declared content type. Current (early 2014) and legacy versions of Firefox will use the declared content type (if one is set),
   rather than performing MIME-sniffing.

### Solution

* Ensure that the application/web server sets the Content-Type header appropriately, and that it sets the X-Content-Type-Options header to
  'nosniff' for all web pages.</p><p>If possible, ensure that the end user uses a standards-compliant and modern web browser that does not
  perform MIME-sniffing at all, or that can be directed by the web application/web server to not perform MIME-sniffing.

### Other information

* This issue still applies to error type pages (401, 403, 500, etc) as those pages are often still affected by injection issues, in which case
  there is still concern for browsers sniffing pages away from their actual content type.</p><p>At "High" threshold this scanner will not
  alert on client or server error responses.

### References

* http://msdn.microsoft.com/en-us/library/ie/gg622941%28v=vs.85%29.aspx
* https://www.owasp.org/index.php/List_of_useful_HTTP_headers

3. Web Browser XSS Protection Not Enabled

   Web Browser XSS Protection is not enabled, or is disabled by the configuration of the 'X-XSS-Protection' HTTP response header
   on the web server

### Solution

* Ensure that the web browser's XSS filter is enabled, by setting the X-XSS-Protection HTTP response header to '1'.

### Other information

* The X-XSS-Protection HTTP response header allows the web server to enable or disable the web browser's XSS protection mechanism.
  The following values would attempt to enable it:

  * X-XSS-Protection: 1; mode=block
  * X-XSS-Protection: 1; report=http://www.example.com/xss

  The following values would disable it:

  * X-XSS-Protection: 0

  The X-XSS-Protection HTTP response header is currently supported on Internet Explorer, Chrome and Safari (WebKit). Note that this
  alert is only raised if the response body could potentially contain an XSS payload (with a text-based content type, with a
  non-zero length).

### References

* https://www.owasp.org/index.php/XSS_(Cross_Site_Scripting)_Prevention_Cheat_Sheet
* https://blog.veracode.com/2014/03/guidelines-for-setting-security-headers/

# TileMaker and TileViewer

TileMaker is currently working with some minor faults but needs to:

1. Support for geogrpahic centroids [Done];
2. Run the generated scripts. This requires the ability to logon and PSQL copy needs to be replaced to SQL COPY from STDIN/to STDOUT with STDIN/STOUT
   file handlers in Node.js;
3. UTF8/16 support (e.g. Slättåkra-Kvibille should not be mangled as at present);
4. Support very large shapefiles (e.g. COA2011) [Done];
5. Needs a manual [Done]!
6. GUI's needs to be merged and brought up to same standard as the rest of the RIF. The TileViewer screen is in better shape
   than the TileMaker screen. Probably the best solution is to use Angular;
7. Support for database logons;
8. Needs to calculate geographic centroids using the database.

# Information Governance Tool

Needs to be specified.

# Data Loading Tool

Needs to be discussed with CDC.

# TODO

## Milestones

* April to May 2018: Build SAHSU production system; support CDC in installing RIF
* June to July 2018: Complete risk analysis; test SAHSU production system; add remaining database related
  functionality:
  * Support for study extract warning messages
  * Remaining data required for study JSON file (areas actually selected by the user)
  * Save and restore map state in the database
* August to September 2018: RIF Handover - Peter Hambly to Martin McCallion; handover SAHSU production
  system to Hima Daby; add risk analysis to SAHSU production system

### PH Final Deliverables

These are to end of contract 10th October 2018

1.	Laymen installation manual and technical manual for setting up RIF software installation on a new machine by any
    of the members of the RIF core team (i.e. Martin, Brandon and Fred) – 10th May 2018

    (Instruction to be cross checked on ICL on site computers by SAHSU team before sign off)

2.	Layman method for adding new user login. For both non-secured and secure environment ie private network. Provided as
    part of the database administrators manual. To be provided in SQL and requires database administration privileges.
	Automation to be a part of the future information governance tool – May 2018

3.	UK geographies to be added and protocol for adding new geographies to the RIF to be available. Clear documented methods for adding new geographies. – May-June 2018

4.	Straightforward way of adding data to the RIF database. Supply data formatting instructions to the SAHSU data team lead. – June 2018

5.	Disease mapping and risk analysis: the same functionalities as originally included in the RIF 3.2 (except multiple
    investigations) should be included and working. Having additional functionalities (e.g. SatSCAN) would be good, but if not possible, clear guidance on how to add functionalities should be available. All the functionalities available in the beta-version should have been tested by the end of the contract period (with the support of the RIF team, including Aina and Fred). While Brandon and Martin will have key roles in the development of the risk analysis functionalities, you will oversee the integration into the RIF of the different pieces developed by each of them. – July 2018

6.	ICD codes. Provide protocol for application notes. Will add support to the RIF for using ICD9, 10 and 11
    simultaneously if time permits – Aug 2018

7.	Confounders. Sex and age defaults. Protocol on how to add and test other cofounders to be made available for the
    SAHSU team to be able to manage. such as socio-demographic status (Carstairs or IMD), ethnicity or smoking. – Aug 2018

8.	TileMaker and TileViewer: Details of coding and troubleshooting suggestions provided and possible bug fixes.
    Both to be tested on differing geographies before handover. – Apr-Sep 2018

9.	A complete manual describing the functionalities of the RIF and their use, to be developed with other members of
    the RIF team, including Brandon, Martin, Aina and Fred. To be kept current. – Apr-Sep 2018

10.	Fully up to date Github repository, with clear annotations and explanations. To be kept current – Apr-Sep 2018


### Chart

| Who              | April to May 2018                                 | June to July 2018                        | August  to September 2018                     |
|------------------|---------------------------------------------------|------------------------------------------|-----------------------------------------------|
| Peter Hambly     | Build SAHSU production system, UK 2011 geography  | test SAHSU production system             | RIF Handover to Martin McCallion              |
|                  | Manuals: system manager, data loader, revise user | Remaining database related functionality | Handover SAHSU production system to Hima Daby |
| Martin McCallion | Risk analysis                                     | Complete risk analysis                   | Data loader                                   |
| Brandon Parkes   | RIF results field renaming specification          |                                          |                                               |
|                  | Statistical script for processing risk analysis   |                                          |                                               |

## April to May 2018

### Dependencies

* RIF results field renaming specification [Brandon Parkes];
* Statistical script for processing risk analysis [Brandon Parkes];

### Middleware

* Risk analysis middleware [Martin McCallion];
* Fixed logging rotation problems [Peter Hambly - DONE];

### SAHSU RIF [Peter Hambly];

* SAHSU production system install [DONE];
* Build UK 2011 geography for RIF;
* Dataloader documentation;
* RIF system manager manual;
* Revise user manual - especially the Study extract and JSON specification file

### Database [Peter Hambly];

* Support for (rif data) views;
* Fix maljoin in extract with covariates [DONE - in report not extract];
* Production install script [DONE]:
  * Need to create schema called %NEWUSER% not "peter";
  * Create dummy pgpass.conf for admin and user;
  * Check that %NEWSER% != "rif40"

## June to July 2018

### SAHSU RIF [Peter Hambly];

* Test SAHSU production system;
* Handover documentation
* Assist with trial data loading

### Middleware

* Risk analysis middleware [Martin McCallion];
* Data loader [Martin McCallion]

### Front End [Peter Hambly]

#### High Priority

* Package front end as WAR [Done MM]
* Login screen: focus on user name field: https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/22 [Done: PH]
* Able to load, save and run risk analysis studies. [Done: all]
* Able to choose continuous covariate variables (not currently supported) https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/20

#### Low Priority

* Save and restore map state in the database; [Done: PH]
* Sort popups in data viewer and disease maps [need to discuss]

### Database [Peter Hambly]

#### High Priority

* Add generate_series() to SQL Server port; [Done: PH]
* Fix predefined_groiup name issues - should be length 30 in t_rif40_inv_conditions: https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/21 [Done: PH]
* Save risk analysis bands as geojson in the database and the save file. Circle may need conversion to geojson (as a polygon of many, many short, linestrings). [Done: PH]
* Save selection options (study and comparison resolution and selection geolevels, geolevels selected. Create
  t_rif40_study_select/rif40_study_select, t_rif40_comparison_select/rif40_comparison_select for areas/bands actually selected by user; [Done: PH]
* Add area name to results map table;

#### Low Priority

* Add t_rif40_warnings/rif40_warnings to contain warning messages on a study basis. Can be created
  by extract or R scripts. Add traps for:
  * Out of range or null covariates by year;
  * Missing years of numerator or denominator data;
  * Males/females not present when requested in numerator or denominator;
  * ICD codes not present when requested in numerator;
  * Maljoin detection
* Add population to map table (max, min, average, rate increase/decrease?), remove join from getAttributes;
* Add viewer/diseasemap[12]_mapping, export_date, last_update_date, comparison_geolevel_select,
  study_geolevel_select to rif40_studies;
* Add default background layer support for geography (so sahsuland has no background);
* Rename results fields as required;
* Add unique keys to description files on rif tables/projects/health themes to protect against
  the middleware using them as a Key;

### Front End [Peter Hambly]

* Complete generation of the study setup JSON used by the web browser [this will need the missing database fields to be added, principally smoothing
  type].
  This **MUST** be exactly the same information as generated by the front end!
* Update study and re-generate ZIP file
* Support for t_rif40_study_select/rif40_study_select, t_rif40_comparison_select/rif40_comparison_select
  for areas/bands actually seleted by user

## August to September 2018

RIF Handover [Peter Hambly]

* Document TileMaker
* Handover to Martin McCallion:
  * Documentation;
  * Front end;
  * Tile maker
* Assist with testing of production RIF system
* Dataloader [Martin McCallion]

# Wish list

This is nice to have functionality that is on hold pending an assessment of need and/or technical feasibility

## Middleware Wish list

* Maps if feasible:
  * Investigate vector grid styling, e.g. grid labels (I don't think it is possible)
  * Get SVG to support layers: http://batik.2283329.n4.nabble.com/Combine-SVG-out-of-single-SVG-files-td3616984.html
* Graphs if feasible:
  * Add support for css <style> tags in jfreechart SVG generator. Also support for rgb to hex
    conversion for end color when graphic bar renderer used.

## Front End Wish list

* Extend PouchDB to base layer

# Issues

These are issues that have been noted but do not affect the running of the RIF

## Middleware Issues

* Logging not rotating properly at end of day. Thought to be caused by log4j threading issues. A simple fix would be to get the
  taxonomy service to use a separate logger; it may be necessary to replace log4j with SLF4J; Logs will appear
  if you stop and start the RIF via the catalina scripts. [Fixed: 21/4/2018]

## Front End Issues

* Leaflet sync plugin not panning correctly. Was upgraded to current version as part of leak testing. Will regress and re-test.
* Choropleth panel: the breaks displayed may not be correct when e.g. changed the number of quantiles will result in zeros!
* Choropleth panel: users can change break points [this could be a feature!]

## Database Issues

* AreaName duplicates to be allowed; key enforcementment to be in the heirarchy table; this allows
  for duplicate county names within a state
* Change CREATE study to run in own schema; create procedure to transfer study/map tables to correct schema
  and grant back permissions [i.e. remove security issue with current Postgres code; SQL Server does not have this issue]
* Add t_rif40_study_areas trigger check (once per INSERT/UPDATE) for correct use of band_id in rif40_study_shares.
  Alternatively check in rif40_run_study
* Rename offset in age_sex_groups (reserved keyword)
* USA_2014 integration:
  * RIF40_GEOGRAPHIES set up wrong: default study and comnparison area names use original field name, no setup field names
    Tomcat error:
    ```
    AbstractSQLManager logSQLQuery 1rifServices.dataStorageLayer.pg.PGSQLRIFContextManager==
    ==========================================================
	QUERY NAME:checkGeoLevelViewExistsQuery
	PARAMETERS:
			1:"GEOID"
			2:"USA_2014"
			3:"1"

	SQL QUERY TEXT
	SELECT 1
	FROM
	   rif40_geolevels
	WHERE
	   geolevel_name=? AND
	   geography=?;

	==========================================================

	rifGenericLibrary.system.RIFServiceException: Record "Area types" with value "GEOID" not found in the database.
			at rifServices.dataStorageLayer.pg.PGSQLRIFContextManager.checkGeoLevelSelectExists(PGSQLRIFContextManager.java:1192)
			at rifServices.dataStorageLayer.pg.PGSQLRIFContextManager.validateCommonMethodParameters(PGSQLRIFContextManager.
    ```
    This is caused by the wrong setup in rif40_geogrpaphies (i.e. tilemaker)
    ```
	sahsuland=> select * from rif40_geographies;
	 geography |               description                |   hierarchytable    | srid  | defaultcomparea  | defaultstudyarea | postal_population_table | postal_point_column | partition | max_geojson_digits |   geometrytable    |    tiletable    | minzoomlevel | maxzoomlevel |   adjacencytable
	-----------+------------------------------------------+---------------------+-------+------------------+------------------+-------------------------+---------------------+-----------+--------------------+--------------------+-----------------+--------------+--------------+---------------------
	 SAHSULAND | SAHSU Example geography                  | HIERARCHY_SAHSULAND | 27700 | SAHSU_GRD_LEVEL1 | SAHSU_GRD_LEVEL3 |                         |                     |         1 |                  6 | GEOMETRY_SAHSULAND | TILES_SAHSULAND |            6 |           11 | ADJACENCY_SAHSULAND
	 USA_2014  | US 2014 Census geography to county level | HIERARCHY_USA_2014  |  4269 | GEOID            | STATENS          |                         |                     |         1 |                  6 | GEOMETRY_USA_2014  | TILES_USA_2014  |            6 |            9 |
	(2 rows)
    ```
    Attempt at fix failed (trigger fault)
	```
	UPDATE rif40_geographies
	   SET defaultcomparea  = 'CB_2014_US_STATE_500K'
	 WHERE defaultcomparea  = 'GEOID' AND geography = 'USA_2014';
	UPDATE rif40_geographies
	   SET defaultstudyarea = 'CB_2014_US_COUNTY_500K'
	 WHERE  geography = 'USA_2014';
	```
  So the script was fixed, reload needed DELETE FROM rif40_covariates WHERE geography = 'USA_2014' adding.
  This was fixed from before; I appear to have used an older script!
  Also STATE was not a comparison are; this is probably a fault in the scripts. All flags should be set in *rif40_geolevels*
  Postgres run study middleware code not stopping on error!
* Harden SQL Server port against SQL Injection getting past middleware into meta data
* Disable guest logins on SQL Server

## Data loader Issues

  * TOTAL_FIELD should be total
  * No covariate fields in data table
  * Data loader to generate primary keys. PK on pop_sahsuland_pop_pk + cluster (see: v4_0_create_sahsuland.sql)

## Documentation Issues

* Add localhost notes to tomcat install doc; add network setup to SQL Server install notes;
  add notes on cross site scripting errors (caused by URL/webservices name mismatch); firefox example:
  ```
  GET XHR https://peter-pc:8080/rifServices/studySubmission/ms/getDatabaseType?userID=peter [HTTP/1.1 200  25ms]
  09:09:31.552 Cross-Origin Request Blocked: The Same Origin Policy disallows reading the remote resource at https://peter-pc:8080/rifServices/studySubmission/ms/getDatabaseType?userID=peter. (Reason: CORS header 'Access-Control-Allow-Origin' missing). 1 (unknown)
  ```

Peter Hambly
May 2nd 2018
