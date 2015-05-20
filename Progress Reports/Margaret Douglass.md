#Margaret Douglass Progress Report

##2015

###May
######20 May 2015:
* Finally moved all my current code onto Github and reorganized Raunaque's old code so it can be out of the way while still accessible.
* Added remaining missing tables: rif40_columns, rif40_tables_and_views, rif40_triggers
* Started on adapting Raunaque's view code.  No view triggers have been written yet.
  - missing views: rif40_geolevels, rif40_inv_conditions, rif40_num_denom, rif40_num_denom_errors, rif40_numerator_outcome_columns,
  rif40_numerator_outcome_columns, rif40_projects, rif40_studies
  
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
