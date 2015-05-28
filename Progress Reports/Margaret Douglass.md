#Margaret Douglass Progress Report

##2015

###May
This month is dedicated to completing scripts necessary to create all tables and views in the rif40 schema (+ the required functions and objects needed for the creation of tables and views). 

######28 May
* All views are now done!  Finished rif40_num_denom, corrected typos in rif40_numerator_outcome_columns, wrote rif40_num_denom_errors and rif40_parameters
* Completed draft functions needed for rif40_num_denom (no logging or good error handling): 
	rif40_object_resolve, rif40_num_denom_validate, rif40_auto_indirect_checks
* Reviewed how SQL Server deals with cursors.


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
