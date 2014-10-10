-------------------------------
------ALL rif490 views--------- 
-------------------------------



--------------------------
-- comparison areas
---------------------------
CREATE VIEW rif40_comparison_areas AS 
 SELECT c.username,
    
c.study_id,
    c.area_id
   FROM t_rif40_comparison_areas c
     
LEFT JOIN rif40_study_shares s ON c.study_id = s.study_id AND s.grantee_username= SUSER_SNAME() 
  
WHERE (c.username = SUSER_SNAME()  OR
   IS_MEMBER(N'[rif40_manager]') = 1  OR 
  s.grantee_username IS NOT NULL) AND 
  
s.grantee_username<> ''
  --ORDER BY c.username;




-----------------------------------
-- view : rif40_contextual_stats
------------------------------------

 --drop view rif40_contextual_stats
CREATE VIEW rif40_contextual_stats AS 
 SELECT c.username,
    c.study_id,
    c.inv_id,
    c.area_id,
    c.area_population,
    c.area_observed,
    c.total_comparision_population,
    c.variance_high,
    c.variance_low
   FROM t_rif40_contextual_stats c
     LEFT JOIN rif40_study_shares s ON c.study_id = s.study_id AND  s.grantee_username= SUSER_SNAME() 
   WHERE c.username = SUSER_SNAME()  OR --current login
   IS_MEMBER(N'[rif40_manager]') = 1 OR 
   s.grantee_username IS NOT NULL AND
    s.grantee_username <> ''
  --ORDER BY c.username; -- order by not allowed in view creation 

  ------------------------------------
  --rif40_fdw_tables : postgreSQL 
  -------------------------------------
  CREATE VIEW rif40_fdw_tables_2 AS 
 SELECT t_rif40_fdw_tables.username,
    t_rif40_fdw_tables.table_name,
    t_rif40_fdw_tables.create_status,
    t_rif40_fdw_tables.error_message,
    t_rif40_fdw_tables.date_created,
    t_rif40_fdw_tables.rowtest_passed
   FROM t_rif40_fdw_tables
  WHERE t_rif40_fdw_tables.username = SUSER_SNAME()

  ----------------------------
  --inv_conditions
  ---------------------------
  CREATE   VIEW rif40_inv_conditions 
  AS 
 SELECT c.username,
    c.study_id,
    c.inv_id,
    c.line_number,
    c.condition
   FROM t_rif40_inv_conditions c
     LEFT JOIN rif40_study_shares s ON c.study_id = s.study_id AND s.grantee_username::name = "current_user"()
  WHERE c.username = SUSER_SNAME() OR 
  IS_MEMBER(N'[rif40_manager]') = 1 OR 
  (s.grantee_username IS NOT NULL AND s.grantee_username <> '')
  ORDER BY c.username;

  ------------------------
  --rif40_inv_covariates
  ------------------------
  CREATE VIEW rif40_inv_covariates 
  AS 
 SELECT c.username,
    c.study_id,
    c.inv_id,
    c.covariate_name,
    c.min,
    c.max,
    c.geography,
    c.study_geolevel_name
   FROM t_rif40_inv_covariates c
     LEFT JOIN rif40_study_shares s ON c.study_id = s.study_id AND s.grantee_username=SUSER_SNAME()
  WHERE c.username=SUSER_SNAME() OR 
  IS_MEMBER(N'[rif40_manager]') = 1 OR 
  (s.grantee_username IS NOT NULL AND s.grantee_username <> '')


  ---------------------------------
  -- rif40_investigations
  ---------------------------------
  
  CREATE VIEW rif40_investigations 
  AS 
 SELECT c.username,
    c.inv_id,
    c.study_id,
    c.inv_name,
    c.year_start,
    c.year_stop,
    c.max_age_group,
    c.min_age_group,
    c.genders,
    c.numer_tab,
    c.mh_test_type,
    c.inv_description,
    c.classifier,
    c.classifier_bands,
    c.investigation_state
   FROM t_rif40_investigations c
     LEFT JOIN rif40_study_shares s ON c.study_id = s.study_id AND s.grantee_username=SUSER_SNAME()
  WHERE c.username=SUSER_SNAME() OR 
  IS_MEMBER(N'[rif40_manager]') = 1 OR 
  (s.grantee_username IS NOT NULL AND s.grantee_username <> '')


  ---------------------------------
  -- rif40_projects : didn't work 
  ---------------------------------
  CREATE VIEW rif40_projects AS 
 SELECT t_rif40_projects.project,
    t_rif40_projects.description,
    t_rif40_projects.date_started,
    t_rif40_projects.date_ended
   FROM t_rif40_projects
  WHERE (t_rif40_projects.project::text IN ( SELECT t_rif40_user_projects.project
           FROM t_rif40_user_projects
          WHERE t_rif40_user_projects.username= SUSER_SNAME() OR  IS_MEMBER(N'[rif40_manager]') = 1


-------------------------------
-- rif40_results
-------------------------------

CREATE  VIEW rif40_results AS 
 SELECT c.username,
    c.study_id,
    c.inv_id,
    c.band_id,
    c.genders,
    c.direct_standardisation,
    c.adjusted,
    c.observed,
    c.expected,
    c.lower95,
    c.upper95,
    c.relative_risk,
    c.smoothed_relative_risk,
    c.posterior_probability,
    c.posterior_probability_upper95,
    c.posterior_probability_lower95,
    c.residual_relative_risk,
    c.residual_rr_lower95,
    c.residual_rr_upper95,
    c.smoothed_smr,
    c.smoothed_smr_lower95,
    c.smoothed_smr_upper95
   FROM t_rif40_results c
     LEFT JOIN rif40_study_shares s ON c.study_id = s.study_id AND  s.grantee_username=SUSER_SNAME()
  WHERE c.username=SUSER_SNAME() OR 
  IS_MEMBER(N'[rif40_manager]') = 1 OR 
  (s.grantee_username IS NOT NULL AND s.grantee_username <> '')


  ----------------------------
  -- study areas
  ----------------------------

  CREATE VIEW rif40_study_areas AS 
 SELECT c.username,
    c.study_id,
    c.area_id,
    c.band_id
   FROM t_rif40_study_areas c
     LEFT JOIN rif40_study_shares s ON c.study_id = s.study_id AND s.grantee_username=SUSER_SNAME()
  WHERE c.username=SUSER_SNAME() OR 
  IS_MEMBER(N'[rif40_manager]') = 1 OR 
  (s.grantee_username IS NOT NULL AND s.grantee_username <> '')


  --------------------------
  -- study SQL 
  --------------------------

  CREATE VIEW rif40_study_sql AS 
 SELECT c.username,
    c.study_id,
    c.statement_type,
    c.statement_number,
    c.sql_text,
    c.line_number,
    c.status
   FROM t_rif40_study_sql c
     LEFT JOIN rif40_study_shares s ON c.study_id = s.study_id AND s.grantee_username=SUSER_SNAME()
  WHERE c.username=SUSER_SNAME() OR 
  IS_MEMBER(N'[rif40_manager]') = 1 OR 
  (s.grantee_username IS NOT NULL AND s.grantee_username <> '')

  --------------
  -- STUDY LOG 
  ---------------

 CREATE VIEW rif40_study_sql_log AS 
 SELECT c.username,
    c.study_id,
    c.statement_type,
    c.statement_number,
    c.log_message,
    c.audsid,
    c.log_sqlcode,
    c.rowcount,
    c.start_time,
    c.elapsed_time
   FROM t_rif40_study_sql_log c
     LEFT JOIN rif40_study_shares s ON c.study_id = s.study_id AND s.grantee_username=SUSER_SNAME()
  WHERE c.username=SUSER_SNAME() OR 
  IS_MEMBER(N'[rif40_manager]') = 1 OR 
  (s.grantee_username IS NOT NULL AND s.grantee_username <> '')

  -------------------
  -- USER PROJECTS
  -------------------

  CREATE  VIEW rif40_user_projects AS 
 SELECT a.project,
    a.username,
    a.grant_date,
    a.revoke_date,
    b.description,
    b.date_started,
    b.date_ended
   FROM t_rif40_user_projects a,
    t_rif40_projects b
  WHERE a.project = b.project AND a.username= SUSER_SNAME() 
  OR  IS_MEMBER(N'[rif40_manager]') = 1

