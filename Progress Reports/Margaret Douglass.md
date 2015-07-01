#Margaret Douglass Progress Report

Principal Work Area: **Microsoft SQL server database port** 

##2015

###July
I plan to finish the trigger work that I began last month and set up a proper test suite to verify that everything has been set up correctly.  Also continue work on simple batch installation scripts so I can easily re-create the latest version of the sahsuland_dev database.

######1 July
* Working on the batch installation scripts to set up logging, error handling, and all the custom error messages.  Cleaning up the error handling code and related functions.
* As part of installation, check if custom error codes are already used by another program.  Currently checking if any custom errors exist -- should that be changed to only the specific error codes we are going to use?
* It is stupid that you cannot call a stored procedure from inside a function in SQL Server.  The internet agrees with me that that is stupid.  That breaks my logging, unless I manually insert rows into a log table (ick).
* To do: install currently completed triggers.  (Don't install the triggers I haven't verified yet but that RH wrote/began to write?)

###June
The goal for this month is to complete all triggers (including logging and error handling) for the rif40 tables and views.  I got sidetracked into creating batch installation scripts due to a change in test servers, and so the triggers are still a work in progress.

######30 June
* Working on installation script for views, fixing up errors in the create table scripts.
* Something strange going on with rif40_num_denom_errors.sql: create view script fails if I add comments but runs fine if the view is created in one script and the comments are added in a separate script.  Needed a 'GO'.
* Still need to deal with setting up custom error messages, error handling functions, and logging.  Then I can return to triggers.

######29 June
* Still working on installation batch scripts to set up new sahsuland_dev database.  Views script caused tons of errors that I need to sort through.

######26 June
* A different Windows server has been found, and so the sahsuland_dev database must be entirely rebuilt.  
* Working on putting together installation scripts.

######18-25 June
* Many delays in getting the Windows test server fixed, so no code could be tested or further developed.
* Instead lots of time was spent discussing the Data Loader tool and related design decisions.

######17 June
* Working on laptop (no access to SQL Server) while I wait for the Windows test server with SQL Server to be rebuilt.
* Worked more on the trigger for t_rif40_studies, stopped at IG part

######15 June
* Problems with the Windows server running the SQL Server test system.  Still needs to be sorted.  
* Wrote more trigger code, but code could not be tested because of server problems.
* Some error messages need to be corrected (misspelled table name)

######12 June
* Slowly sorting out more of the t_rif40_studies_trigger code. 

######11 June
* More trigger work.  Still working on t_rif40_studies_trigger

######10 June
* Finished rif40_study_shares_trigger.  Tested it by inserting data into it and related tables, correcting table definitions and other code as I encounter errors.
* Onto next trigger: t_rif40_studies_trigger.sql, another complicated one.

######9 June
* Triggers: working on rif40_study_shares_trigger code, have not completed the delete part  

######8 June
* Set up basic logging function.  Messages are not saved at present, but the log procedure can be used in triggers.
* Updated error handling function to experiment with adding better logging.
* More work on triggers.  Went through rif40_user_projects, now incorporating error handling and logging.  Started on rif40_study_areas triggers.

######5 June
* Things finally are working for error handling, and I know more options for logging.  SQL Server does not have autonomous transactions for logging.  I will be careful with where to call rollbacks.
* Fixed rif40_version triggers to have the correct error handling.  

######4 June
* Looking more at T-SQL error handling in order to improve the error handling in the SQL Server trigger code.  RAISERROR code should be replaced with THROW code.  Rollbacking transactions is removing existing error handling logs. 
* Still trying to get proper logging in the simple rif40_version trigger, but I have not succeeded yet.

######3 June
* Still reviewing existing trigger code in both SQL Server and Postgres to understand how it works. 
* Looking at the differences between SQL Server and Postgres trigger code: no "before" triggers in SQL Server, must deal with all changed rows together without cursors in SQL Server whereas Postgres can process each row separately.
* Wrote some basic trigger-related code for rif40_version to get used to the SQL Server code.
* Custom error messages from RH need to be updated.  I wrote a procedure to remove old custom error messages.

######2 June
* Finished updating and commenting all tables and views.
* Experimented with running batch SQL on rif3 with "sqlcmd -d sahsuland_dev -i H:\SAHSU\rif_test.sql".
* Onto triggers.  Now focusing on understanding what is in Postgres and what RH completed to understand how much needs to be done.
* There are 65 triggers in Postgres.  RH implemented 15, but naming is inconsistent so it is not obvious which of the Postgres triggers have been implemented.  I suspect RH combined several Postgres triggers into one single trigger per table.  None of RH's triggers has been checked.

######1 June
* Still fixing up the tables and views to have the correct columns, data types, and comments as in the current Postgres version.
* Finished almost all the tables.  About half of the views have comments.

---
###May
This month is dedicated to completing scripts necessary to create all tables and views in the rif40 schema (+ the required functions and objects needed for the creation of tables and views). 

######29 May
* Working more on adding comments to all tables and views in rif40 schema + checking that the object definitions are still the same in Peter's Postgres database.
* Experimenting with how to identify, drop, disable foreign keys and constraints for rebuilding tables that have references to others in SQL Server
* There is no built-in function to get current value of a sequence in SQL Server (really?), so I had to write my own in order to easily set default column definitions to current value of sequences
* Study_id datatype has changed, and that is a field used in many foreign keys.  Messy.  Still working my way through all the tables that have wrong study_id type.  stopped at t_rif40_investigations
(select * FROM [sahsuland_dev].[INFORMATION_SCHEMA].[COLUMNS] where column_name='study_id' and data_type != 'int')
 
######28 May
* All views are now done!  Finished rif40_num_denom, corrected typos in rif40_numerator_outcome_columns, wrote rif40_num_denom_errors and rif40_parameters
* Completed draft functions needed for rif40_num_denom (no logging or good error handling): 
	rif40_object_resolve, rif40_num_denom_validate, rif40_auto_indirect_checks
* Reviewed how SQL Server deals with cursors.
* Started going through the tables to add comments, and I have found several tables that have been changed significantly since we began porting the database.  I need an easy way to compare all of Peter's tables/columns to mine in order to spot the differences.  Stopped commenting at rif40_outcomes

Still to do: logs/error handling, more comments

######27 May
* Continuing to work on new views: rif40_numerator_outcome_columns (done), rif40_num_denom (still in progress)
* All the functions needed for rif40_num_denom are rather complicated.  I put together a draft of rif_is_object_resolvable that will be refined once I sort out logging and error handling.
* Experimenting with customized error messages and how RH handled errors.  She added new customized error messages to the SQL Server master system tables.  There does not appear to be a way to have schema-level customized error messages.  Are system-wide customized error messages wise?  (what if other tools in db were already using those codes?)
* Looking into logging options.  Custom logging seems to be an SSIS thing, not standard T-SQL/SQL Server.  Take the easy approach of manually inserting rows into a rif40_log table?  But then database rollbacks would also rollback log of what went wrong.

To do: decide how to deal with logs and error messages to be consistent with what the middleware expects and what Peter produces in Postgres

######26 May
* Finished new views: rif40_inv_conditions, rif40_projects, rif40_studies.  Starting rif40_num_denom
* Discovered that table t_rif40_inv_conditions definition was out-of-date and required creation of sequence objects
* Experimented with comments in SQL Server.  I will need to spend a boring afternoon porting that code for all the existing rif40 objects.
	(ex. sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Username' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'VIEW',@level1name=N'rif40_investigations', @level2type=N'COLUMN',@level2name=N'username'
		fn_listextendedproperty (NULL, 'schema', 'rif40', 'view', 'rif40_investigations', 'column', 'inv_id'))

Additional To do: add comments to tables and views in the extended properties fields
	
######21 May
* Finished adapting RH's old view code to my new database, but that is only 10 out of 19 views needed for the rif40 schema.  All her code is now integrated the sahsuland_dev new version.
* Added new views: rif40_user_version, rif40_geolevels (flagged question to discuss with Peter)

To do: write code for remaining views, trigger code for tables that had not been written, all trigger code for views 

######20 May
* Finally moved all my current code onto Github and reorganized Raunaque's old code so it can be out of the way while still accessible.
* Added remaining missing tables: rif40_columns, rif40_tables_and_views, rif40_triggers
* Started on adapting Raunaque's view code.  No view triggers have been written yet.
  - missing views (not ported by RH): rif40_geolevels, rif40_inv_conditions, rif40_num_denom, rif40_num_denom_errors, 
  rif40_numerator_outcome_columns, rif40_projects, rif40_studies, rif40_user_version
  
---
###April
I am working on creating a clean set of scripts to build the sahsuland_dev database on SQL Server, based on the work that Raunaque did last year and on the Postgres code from Peter.

All work I have done so far is on the rif40 schema.  Code is being tested on the internal rif3 server.

To do:
- 3 tables are missing: rif40_columns, rif40_tables_and_views, rif40_triggers
- Table triggers are missing on: rif40_predefined_groups, rif40_tables, t_rif40_comparison_areas, t_rif40_inv_conditions, t_rif40_study_sql_log, t_rif40_study_sql
- I have not started to work on the views and view triggers

Known issues: 
- Logging and error handling are inconsistent in the triggers.  
- The functionality of the existing triggers must be tested.  Raunaque left comments in some of the trigger code saying that she was not certain whether parts were correct.
