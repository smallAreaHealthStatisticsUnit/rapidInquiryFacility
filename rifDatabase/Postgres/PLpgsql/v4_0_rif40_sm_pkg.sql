-- *************************************************************************************************
--
-- CVS/RCS Header
--
-- $Author: peterh $
-- $Date: 2014/02/27 11:29:37 $
-- Type: Postgres PSQL script
-- $RCSfile: v4_0_rif40_sm_pkg.sql,v $
-- $Source: /home/EPH/CVS/repository/SAHSU/projects/rif/V4.0/database/postgres/PLpgsql/v4_0_rif40_sm_pkg.sql,v $
-- $Revision: 1.6 $
-- $Id: v4_0_rif40_sm_pkg.sql,v 1.6 2014/02/27 11:29:37 peterh Exp $
-- $State: Exp $
-- $Locker:  $
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - PG psql code (state machine and extract SQL generation)
--
-- Copyright:
--
-- The RIF is free software; you can redistribute it and/or modify it under
-- the terms of the GNU General Public License as published by the Free
-- Software Foundation; either version 2, or (at your option) any later
-- version.
--
-- The RIF is distributed in the hope that it will be useful, but WITHOUT ANY
-- WARRANTY; without even the implied warranty of MERCHANTABILITY or
-- FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
-- for more details.
--
-- You should have received a copy of the GNU General Public License
-- along with this file; see the file LICENCE.  If not, write to:
--
-- UK Small Area Health Statistics Unit,
-- Dept. of Epidemiology and Biostatistics
-- Imperial College School of Medicine (St. Mary's Campus),
-- Norfolk Place,
-- Paddington,
-- London, W2 1PG
-- United Kingdom
--
-- The RIF uses Oracle 11g, PL/SQL, PostGres and PostGIS as part of its implementation.
--
-- Oracle11g, PL/SQL and Pro*C are trademarks of the Oracle Corporation.
--
-- All terms mentioned in this software and supporting documentation that are known to be trademarks
-- or service marks have been appropriately capitalised. Imperial College cannot attest to the accuracy
-- of this information. The use of a term in this software or supporting documentation should NOT be
-- regarded as affecting the validity of any trademark or service mark.
--
-- Summary of functions/procedures:
--
-- To be added
--
-- Error handling strategy:
--
-- Output and logging procedures do not HANDLE or PROPAGATE errors. This makes them safe to use
-- in package initialisation and NON recursive.
--
-- References:
--
-- 	None
--
-- Dependencies:
--
--	Packages: None
--
-- 	<This should include: packages, non packages procedures and functions, tables, views, objects>
--
-- Portability:
--
--	Linux, Windows 2003/2008, Oracle 11gR1
--
-- Limitations:
--
-- Change log:
--
-- $Log: v4_0_rif40_sm_pkg.sql,v $
-- Revision 1.6  2014/02/27 11:29:37  peterh
--
-- About to test isolated code tree for trasnfer to Github/public network
--
-- Revision 1.5  2014/02/11 14:56:25  peterh
-- Baseline after simplifcation integration and testing of new Geo setup scripts
--
-- Revision 1.4  2014/01/14 08:59:49  peterh
--
-- Baseline prior to adding multipolygon support for simplification
--
-- Revision 1.3  2013/09/18 15:20:32  peterh
-- Checkin at end of 6 week RIF focus. Got as far as SAHSULAND run study to completion for observed only
--
-- Revision 1.2  2013/09/02 14:08:33  peterh
--
-- Baseline after full trigger implmentation
--
-- Revision 1.1  2013/03/14 17:35:39  peterh
-- Baseline for TX to laptop
--
--
\set ECHO all
\set ON_ERROR_STOP ON
\timing

\echo Building rif40_sm_pkg (state machine and extract SQL generation) package...

--
-- Check user is rif40
--
DO LANGUAGE plpgsql $$
BEGIN
	IF user = 'rif40' THEN
		RAISE INFO 'User check: %', user;	
	ELSE
		RAISE EXCEPTION 'C209xx: User check failed: % is not rif40', user;	
	END IF;
END;
$$;

CREATE OR REPLACE FUNCTION rif40_sm_pkg.rif40_verify_state_change(study_id INTEGER, old_study_state VARCHAR, new_study_state VARCHAR)
RETURNS VARCHAR
SECURITY INVOKER
AS $func$
DECLARE
/*
Function:	rif40_verify_state_change()
Parameter:	Study ID, old study state, new study state
Returns:	New state after verification
Description:	State change function/checking functions - check:

Verify state transition
All study_id tables are populated
Results are not created before the appropriate state has been reached
Any not implemented WARNINGS become errors
Functionality reduction restrictions. Restricted to:
	One covariate
 	No direct standardisation (rif40_studies.direct_stand_tab IS NULL)
	Covariate study_geolevel_name must be the same as rif40_studies.study_geolevel_name
	Disease mapping only
	AGE_SEX_GROUP column only (i.e. no separate AGE_GROUP/AGE/SEX columns)

Study state - 

C: created, not verfied; 
V: verified, but no other work done; 
E - extracted imported or created, but no results or maps created; 
R: results computed; 
U: upgraded record from V3.1 RIF (has an indeterminate state; probably R).

*/
	c1_verst REFCURSOR;
	c2_verst CURSOR(l_study_id INTEGER) FOR
		WITH b AS (
			SELECT COUNT(inv_id) AS investigation_count
			  FROM rif40_investigations b1
			 WHERE l_study_id = b1.study_id
		), c AS ( 
			SELECT MAX(inv_covariate_count) AS max_inv_covariate_count
			  FROM (
				SELECT inv_id, COUNT(covariate_name) AS inv_covariate_count
				  FROM rif40_inv_covariates c1
				 WHERE l_study_id = c1.study_id
				 GROUP BY inv_id) c2
		)
		SELECT study_state, a.study_id, b.investigation_count, c.max_inv_covariate_count,
		       direct_stand_tab, study_type, 
		       d.age_sex_group_field_name AS denom_age_sex_group_field_name, denom_tab
		  FROM rif40_studies a, b, c, rif40_tables d
		 WHERE l_study_id  = a.study_id
		   AND a.denom_tab = d.table_name;
	c3_verst CURSOR(l_study_id INTEGER) FOR
		SELECT COUNT(a.study_geolevel_name) AS total_study_geolevel_name
		  FROM rif40_inv_covariates a, rif40_studies b
		 WHERE a.study_id            = b.study_id
		   AND a.study_geolevel_name != b.study_geolevel_name
		   AND l_study_id            = a.study_id;
	c4_verst CURSOR(l_study_id INTEGER) FOR
		SELECT age_sex_group_field_name AS numer_age_sex_group_field_name, numer_tab, inv_id
		  FROM rif40_investigations a, rif40_tables t
		 WHERE a.numer_tab = t.table_name
		   AND l_study_id  = a.study_id;
--
	c1_rec RECORD;
	c2_rec RECORD;
	c3_rec RECORD;
	c4_rec RECORD;
--
	c_v_transition_tables VARCHAR[] := ARRAY['rif40_inv_conditions', 'rif40_investigations', 
		'rif40_study_areas', 'rif40_comparison_areas'];
	v_e_transition_tables VARCHAR[] := ARRAY['rif40_study_sql', 'rif40_study_sql_log', 'rif40_inv_conditions', 'rif40_investigations', 
		'rif40_study_areas', 'rif40_comparison_areas'];
	e_r_transition_tables VARCHAR[] := ARRAY['rif40_study_sql', 'rif40_study_sql_log', 'rif40_inv_conditions', 'rif40_investigations', 
		'rif40_study_areas', 'rif40_comparison_areas', 'rif40_results' /*, 'rif40_contextual_stats' */];
	results_tables VARCHAR[] := ARRAY['rif40_results' /*, 'rif40_contextual_stats' */];
	transition_tables VARCHAR[];
	l_table VARCHAR;
	sql_stmt VARCHAR;
	missing_study_id_rows INTEGER:=0;
	unexpected_results_rows INTEGER:=0;
	not_implemented INTEGER:=0;
BEGIN
--
-- Verify state transition
--
	IF old_study_state = 'R' AND new_study_state != old_study_state THEN
		PERFORM rif40_log_pkg.rif40_error(-90800, 'rif40_verify_state_change', 
			'Attempting to change the state (%=>%) of upgraded RIF30 study %. Please clone',
			old_study_state::VARCHAR,
			new_study_state::VARCHAR,
			study_id::VARCHAR);
	ELSIF old_study_state = 'R' AND new_study_state != old_study_state THEN
		PERFORM rif40_log_pkg.rif40_error(-90801, 'rif40_verify_state_change', 
			'Attempting to change the state (%=>%) of a completed study %. Please clone',
			old_study_state::VARCHAR,
			new_study_state::VARCHAR,
			study_id::VARCHAR);
	ELSIF new_study_state = 'C' THEN /* Reset */
		transition_tables:=c_v_transition_tables;
	ELSIF old_study_state = 'C' AND new_study_state = 'V' THEN
		transition_tables:=c_v_transition_tables;
	ELSIF old_study_state = 'V' AND new_study_state = 'E' THEN
		transition_tables:=v_e_transition_tables;
	ELSIF old_study_state = 'E' AND new_study_state = 'R' THEN
		transition_tables:=e_r_transition_tables;
	ELSE
		PERFORM rif40_log_pkg.rif40_error(-90802, 'rif40_verify_state_change', 
			'Attempting invalid state transition (%=>%) for study %',
			old_study_state::VARCHAR,
			new_study_state::VARCHAR,
			study_id::VARCHAR);		
	END IF;
--
-- Check all study_id tables are populated
--
	FOREACH l_table IN ARRAY transition_tables LOOP
		sql_stmt:='SELECT COUNT(study_id) AS total FROM '||LOWER(l_table)||' WHERE study_id = $1';
		PERFORM rif40_log_pkg.rif40_log('DEBUG2', 'rif40_verify_state_change', 	
			'SQL> %;',
			sql_stmt::VARCHAR);
		OPEN c1_verst FOR EXECUTE sql_stmt USING study_id;
		FETCH c1_verst INTO c1_rec;
		IF c1_rec.total = 0 THEN
			missing_study_id_rows:=missing_study_id_rows+1;
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_verify_state_change', 
				'[90803] % table has no rows during attempted state transition (%=>%) for study %',
				l_table::VARCHAR,
				old_study_state::VARCHAR,
				new_study_state::VARCHAR,
				study_id::VARCHAR);		
		ELSE
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_verify_state_change', 
				'[90803] % table has % rows during attempted state transition (%=>%) for study %',
				l_table::VARCHAR,
				c1_rec.total::VARCHAR,
				old_study_state::VARCHAR,
				new_study_state::VARCHAR,
				study_id::VARCHAR);		
		END IF;
		CLOSE c1_verst;
	END LOOP;
	IF missing_study_id_rows > 0 THEN
		PERFORM rif40_log_pkg.rif40_error(-90804, 'rif40_verify_state_change', 
			'% study_id tables have no rows during attempted state transition (%=>%) for study %',
			missing_study_id_rows::VARCHAR,
			old_study_state::VARCHAR,
			new_study_state::VARCHAR,
			study_id::VARCHAR);		
	ELSE
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_verify_state_change', 
			'[90804] No study_id tables have no rows during attempted state transition (%=>%) for study %',
			old_study_state::VARCHAR,
			new_study_state::VARCHAR,
			study_id::VARCHAR);		
	END IF;
--
-- Results are not created before the appropriate state has been reached
--
	IF old_study_state IN ('C', 'V') THEN
		FOREACH l_table IN ARRAY results_tables LOOP
			sql_stmt:='SELECT COUNT(study_id) AS total FROM '||LOWER(l_table)||' WHERE study_id = $1';
			PERFORM rif40_log_pkg.rif40_log('DEBUG2', 'rif40_verify_state_change', 	
				'SQL> %;',
				sql_stmt::VARCHAR);
			OPEN c1_verst FOR EXECUTE sql_stmt USING study_id;
			FETCH c1_verst INTO c1_rec;
			IF c1_rec.total = 0 THEN
				NULL;
			ELSE
				unexpected_results_rows:=unexpected_results_rows+1;
				PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_verify_state_change', 
					'[90805] Results % table has % rows during attempted state transition (%=>%) for study %',
					l_table::VARCHAR,
					c1_rec.total::VARCHAR,
					old_study_state::VARCHAR,
					new_study_state::VARCHAR,
					study_id::VARCHAR);
			END IF;
			CLOSE c1_verst;
		END LOOP;
		IF unexpected_results_rows > 0 THEN
			PERFORM rif40_log_pkg.rif40_error(-90806, 'rif40_verify_state_change', 
				'% study_id results tables have no rows during attempted state transition (%=>%) for study %',
				unexpected_results_rows::VARCHAR,
				old_study_state::VARCHAR,
				new_study_state::VARCHAR,
				study_id::VARCHAR);		
		ELSE	
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_verify_state_change', 
				'[90806] No study_id results tables have no rows during attempted state transition (%=>%) for study %',
				old_study_state::VARCHAR,
				new_study_state::VARCHAR,
				study_id::VARCHAR);		
		END IF;
	END IF;
--
-- Any not implemented WARNINGS become errors
-- Functionality reduction restrictions. Restricted to:
--	One covariate
-- 	No direct standardisation (rif40_studies.direct_stand_tab IS NULL)
--	Disease mapping only
--	AGE_SEX_GROUP column only (i.e. no separate AGE_GROUP/AGE/SEX columns)
--
	OPEN c2_verst(study_id);
	FETCH c2_verst INTO c2_rec;
	IF NOT FOUND THEN
		PERFORM rif40_log_pkg.rif40_error(-90807, 'rif40_verify_state_change', 
			'Study not found during attempted state transition (%=>%) for study %',
			old_study_state::VARCHAR,
			new_study_state::VARCHAR,
			study_id::VARCHAR);
	END IF;
	CLOSE c2_verst;
--
	IF c2_rec.max_inv_covariate_count > 1 THEN
		not_implemented:=not_implemented+1;
		PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_verify_state_change', 
			'[90808] Multiple covariates (% investigations()) not yet supported during attempted state transition (%=>%) for study %',
			c2_rec.max_inv_covariate_count::VARCHAR,
			old_study_state::VARCHAR,
			new_study_state::VARCHAR,
			study_id::VARCHAR);		
	END IF;
	IF c2_rec.direct_stand_tab IS NOT NULL THEN
		not_implemented:=not_implemented+1;
		PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_verify_state_change', 
			'[90809] Direct standardisation using % not yet supported during attempted state transition (%=>%) for study %',
			c2_rec.direct_stand_tab::VARCHAR,
			old_study_state::VARCHAR,
			new_study_state::VARCHAR,
			study_id::VARCHAR);		
	END IF;
	FOR c4_rec IN c4_verst(study_id) LOOP
		IF c4_rec.numer_age_sex_group_field_name IS NULL THEN
			not_implemented:=not_implemented+1;
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_verify_state_change', 
				'[90810] AGE_GROUP+SEX columns not yet supported (AGE_SEX_GROUP only) for numerator table % during attempted state transition (%=>%) for study %, investigation %',
				c4_rec.numer_tab::VARCHAR,
				old_study_state::VARCHAR,
				new_study_state::VARCHAR,
				study_id::VARCHAR,
				c4_rec.inv_id::VARCHAR);		
		END IF;
	END LOOP;
	IF c2_rec.denom_age_sex_group_field_name IS NULL THEN
		not_implemented:=not_implemented+1;
		PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_verify_state_change', 
			'[90811] AGE_GROUP+SEX columns not yet supported (AGE_SEX_GROUP only) for denominator table % during attempted state transition (%=>%) for study %',
			c2_rec.denom_tab::VARCHAR,
			old_study_state::VARCHAR,
			new_study_state::VARCHAR,
			study_id::VARCHAR);		
	END IF;
--
-- Study type:
-- 	1 - disease mapping, 
--	11 - Risk Analysis (many areas, one band), 
--	12 - Risk Analysis (point sources), 
--	13 - Risk Analysis (exposure covariates), 
--	14 - Risk Analysis (coverage shapefile), 
--	15 - Risk Analysis (exposure shapefile)
--
	IF c2_rec.study_type != 1 THEN
		not_implemented:=not_implemented+1;
		PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_verify_state_change', 
			'[90812] Non disease mapping studies (%) not yet supported during attempted state transition (%=>%) for study %',
			c2_rec.study_type::VARCHAR,
			old_study_state::VARCHAR,
			new_study_state::VARCHAR,
			study_id::VARCHAR);	
	END IF;
--
--	Covariate study_geolevel_name must be the same as rif40_studies.study_geolevel_name
--
	OPEN c3_verst(study_id);
	FETCH c3_verst INTO c3_rec;
	IF NOT FOUND THEN
		PERFORM rif40_log_pkg.rif40_error(-90813, 'rif40_verify_state_change', 
			'Study not found in rif40_inv_covariates during attempted state transition (%=>%) for study %',
			old_study_state::VARCHAR,
			new_study_state::VARCHAR,
			study_id::VARCHAR);
	END IF;
	CLOSE c3_verst;
	IF c3_rec.total_study_geolevel_name != 0 THEN
		not_implemented:=not_implemented+1;
		PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_verify_state_change', 
			'[90814] % investigation covariates found with differing <study_geolevel_name> - not yet supported during attempted state transition (%=>%) for study %',
			c3_rec.total_study_geolevel_name::VARCHAR,
			old_study_state::VARCHAR,
			new_study_state::VARCHAR,
			study_id::VARCHAR);	
	END IF;
--
	IF not_implemented > 0 THEN
		PERFORM rif40_log_pkg.rif40_error(-90815, 'rif40_verify_state_change', 
			'% not yet supported features during attempted state transition (%=>%) for study %',
			not_implemented::VARCHAR,
			old_study_state::VARCHAR,
			new_study_state::VARCHAR,
			study_id::VARCHAR);
	END IF;
--		
	PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_verify_state_change', 
		'[90816] Attempted state transition (%=>%) verified for study %',
		old_study_state::VARCHAR,
		new_study_state::VARCHAR,
		study_id::VARCHAR);
--
	RETURN new_study_state;
END;
$func$
LANGUAGE 'plpgsql';

GRANT EXECUTE ON FUNCTION rif40_sm_pkg.rif40_verify_state_change(INTEGER, VARCHAR, VARCHAR) TO rif40;
COMMENT ON FUNCTION rif40_sm_pkg.rif40_verify_state_change(INTEGER, VARCHAR, VARCHAR) IS 'Function:	rif40_verify_state_change()
Parameter:	Study ID, old study state, new study state
Returns:	New state after verification
Description:	State change function/checking functions - check:

Verify state transition
All study_id tables are populated
Results are not created before the appropriate state has been reached
Any not implemented WARNINGS become errors
Functionality reduction restrictions. Restricted to:
	One covariate
 	No direct standardisation (rif40_studies.direct_stand_tab IS NULL)
	Covariate study_geolevel_name must be the same as rif40_studies.study_geolevel_name
	Disease mapping only
	AGE_SEX_GROUP column only (i.e. no separate AGE_GROUP/AGE/SEX columns)

Study state - 

C: created, not verfied; 
V: verified, but no other work done; 
E - extracted imported or created, but no results or maps created; 
R: results computed; 
U: upgraded record from V3.1 RIF (has an indeterminate state; probably R).
';
   
CREATE OR REPLACE FUNCTION rif40_sm_pkg.rif40_run_study(study_id INTEGER, recursion_level INTEGER DEFAULT 0)
RETURNS BOOLEAN
SECURITY INVOKER
AS $func$
DECLARE
/*
Function:	rif40_run_study()
Parameter:	Study ID
Returns:	Success or failure [BOOLEAN]
		Note this is to allow SQL executed by study extraction/results created to be logged (Postgres does not allow autonomous transactions)
		Verification and error checking raises EXCEPTIONS in the usual way; and will cause the SQL log to be lost
Description:	Run study 

Check study state - 

C: created, not verfied; 
V: verified, but no other work done; 
E - extracted imported or created, but no results or maps created; 
R: results computed; 
U: upgraded record from V3.1 RIF (has an indeterminate state; probably R).

Define transition
Create extract, call: rif40_sm_pkg.rif40_create_extract()
Runs as rif40_sm_pkg NOT the user. This is so all objects created can be explicitly granted to the user
Compute results, call: rif40_sm_pkg.rif40_compute_results()
Do update. This forces verification
(i.e. change in study_State on rif40_studies calls rif40_sm_pkg.rif40_verify_state_change)
Recurse until complete
 */
	c1_runst CURSOR(l_study_id INTEGER) FOR
		WITH b AS (
			SELECT COUNT(inv_id) AS investigation_count
			  FROM rif40_investigations c
			 WHERE l_study_id = c.study_id
		)
		SELECT study_state, a.study_id, b.investigation_count 
		  FROM t_rif40_studies a, b /* MUST USE TABLE NOT VIEWS WHEN USING LOCKS/WHERE CURRENT OF */
		 WHERE l_study_id = a.study_id
		   FOR UPDATE;
	c1_rec RECORD;
--
	new_study_state 	VARCHAR;
	investigation_count 	INTEGER;
	study_count 		INTEGER;
	n_recursion_level 	INTEGER:=recursion_level+1;
--
	stp		TIMESTAMP WITH TIME ZONE := clock_timestamp();
	etp		TIMESTAMP WITH TIME ZONE;
BEGIN
--
-- Check study state and define transition
--
	OPEN c1_runst(study_id);
	FETCH c1_runst INTO c1_rec;
	IF NOT FOUND THEN
		CLOSE c1_runst;
		PERFORM rif40_log_pkg.rif40_error(-90701, 'rif40_run_study', 
			'Study ID % not found',
			study_id::VARCHAR);
	END IF;
--
	IF c1_rec.study_state = 'C' THEN
		 new_study_state = 'V';
	ELSIF c1_rec.study_state = 'V' THEN
		 new_study_state = 'E';
	ELSIF c1_rec.study_state = 'E' THEN
		 new_study_state = 'R';
	ELSE
		CLOSE c1_runst;
		PERFORM rif40_log_pkg.rif40_error(-90702, 'rif40_run_study', 
			'Study ID % cannot be run, in state: %, needs to be in ''V'' or ''E''',
			study_id::VARCHAR,
			c1_rec.study_state::VARCHAR);
	END IF;
--
-- Create extract, call: rif40_sm_pkg.rif40_create_extract()
--
	IF new_study_state = 'E' THEN
		IF rif40_sm_pkg.rif40_create_extract(c1_rec.study_id) = FALSE THEN
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_run_study',
				'[90703] Call rif40_create_extract() for study % failed, see previous warnings',
				c1_rec.study_id::VARCHAR);
			CLOSE c1_runst;
			RETURN FALSE;
		ELSE
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_run_study',
				'[90704] Call rif40_create_extract() for study % OK',
				c1_rec.study_id::VARCHAR);
		END IF;
--
-- Compute results, call: rif40_sm_pkg.rif40_compute_results()
--
	ELSIF new_study_state = 'R' THEN
		IF rif40_sm_pkg.rif40_compute_results(c1_rec.study_id) = FALSE THEN
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_run_study',
				'[90705] Call rif40_compute_results() for study % failed, see previous warnings',
				c1_rec.study_id::VARCHAR);
			CLOSE c1_runst;
			RETURN FALSE;
		ELSE
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_run_study',
				'[90706] Call rif40_compute_results() for study % OK',
				c1_rec.study_id::VARCHAR);
		END IF;
	END IF;

--
-- Do update. This forces verification
-- (i.e. change in study_State on rif40_studies calls rif40_sm_pkg.rif40_verify_state_change)
--
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_run_study',
		'[90707] Start state transition (%=>%) for study %',
		c1_rec.study_state::VARCHAR,
		new_study_state::VARCHAR,
		c1_rec.study_id::VARCHAR);
	UPDATE rif40_investigations a SET investigation_state = new_study_state WHERE a.study_id = c1_rec.study_id;
	GET DIAGNOSTICS investigation_count = ROW_COUNT;
	IF investigation_count != c1_rec.investigation_count THEN
		PERFORM rif40_log_pkg.rif40_error(-90708, 'rif40_run_study', 
			'Excpeting to update % investigation(s), updated % during state transition (%=>%) for study %',
			c1_rec.investigation_count::VARCHAR,
			investigation_count::VARCHAR,
			c1_rec.study_state::VARCHAR,
			new_study_state::VARCHAR,
			c1_rec.study_id::VARCHAR);
	END IF;
--
-- MUST USE TABLE NOT VIEWS WHEN USING LOCKS/WHERE CURRENT OF
--
	UPDATE rif40_studies a SET study_state = new_study_state WHERE a.study_id = c1_rec.study_id;
	GET DIAGNOSTICS study_count = ROW_COUNT;
	etp:=clock_timestamp();
	PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_run_study',
		'[90709] Recurse [%] Completed state transition (%=>%) for study % with % investigation(s); time taken %',
		n_recursion_level::VARCHAR,
		c1_rec.study_state::VARCHAR,
		new_study_state::VARCHAR,
		c1_rec.study_id::VARCHAR,
		investigation_count::VARCHAR,
		age(etp, stp)::VARCHAR);
 
	CLOSE c1_runst;
--
-- Recurse until complete
--
	IF new_study_state != c1_rec.study_state AND new_study_state IN ('V', 'E') THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_run_study',
			'[90710] Recurse [%] rif40_run_study using new state % for study %',
			n_recursion_level::VARCHAR,
			new_study_state::VARCHAR,
			c1_rec.study_id::VARCHAR);
		IF rif40_sm_pkg.rif40_run_study(c1_rec.study_id, n_recursion_level) = FALSE THEN /* Halt on failure */
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_run_study',
				'[90711] Recurse [%] rif40_run_study to new state % for study % failed, see previous warnings',
				n_recursion_level::VARCHAR,
				new_study_state::VARCHAR,
				c1_rec.study_id::VARCHAR);
			RETURN FALSE;
		END IF;
	ELSIF new_study_state != c1_rec.study_state AND new_study_state = 'R' THEN
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_run_study', E'\n'||
'************************************************************************'||E'\n'||
'*                                                                      *'||E'\n'||
'* [90712] Completed study %                         *'||E'\n'||
'*                                                                      *'||E'\n'||
'************************************************************************',
			RPAD(c1_rec.study_id::VARCHAR, 20)::VARCHAR);
	ELSE
		OPEN c1_runst(study_id);
		FETCH c1_runst INTO c1_rec;
		IF NOT FOUND THEN
			CLOSE c1_runst;
			PERFORM rif40_log_pkg.rif40_error(-90713, 'rif40_run_study', 
				'Study ID % not found, study in unexpected and unknown state',
				study_id::VARCHAR);
		END IF;
		PERFORM rif40_log_pkg.rif40_error(-90714, 'rif40_run_study', 
			'Study % in unexpected state %',
			c1_rec.study_id::VARCHAR,
			c1_rec.study_state::VARCHAR);
	END IF;
--
-- All recursion unwound
--
	IF recursion_level = 0 THEN
		etp:=clock_timestamp();
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_run_study',
			'[90713] Recursion complete rif40_run_study study % with % investigation(s); time taken %',
			c1_rec.study_id::VARCHAR,
			investigation_count::VARCHAR,
			age(etp, stp)::VARCHAR);
	END IF;
--
	RETURN TRUE;
END;
$func$
LANGUAGE 'plpgsql';

GRANT EXECUTE ON FUNCTION rif40_sm_pkg.rif40_run_study(INTEGER, INTEGER) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_sm_pkg.rif40_run_study(INTEGER, INTEGER) TO rif_user;
GRANT EXECUTE ON FUNCTION rif40_sm_pkg.rif40_run_study(INTEGER, INTEGER) TO rif40;
COMMENT ON FUNCTION rif40_sm_pkg.rif40_run_study(INTEGER, INTEGER) IS 'Function:	rif40_run_study()
Parameter:	Study ID
Returns:	Success or failure [BOOLEAN]
		Note this is to allow SQL executed by study extraction/results created to be logged (Postgres does not allow autonomous transactions)
		Verification and error checking raises EXCEPTIONS in the usual way; and will cause the SQL log to be lost
		
Description:	Run study 

Check study state - 

C: created, not verfied; 
V: verified, but no other work done; 
E: extracted imported or created, but no results or maps created; 
R: results computed; 
U: upgraded record from V3.1 RIF (has an indeterminate state; probably R).

Define transition
Create extract, call: rif40_sm_pkg.rif40_create_extract()
Runs as rif40_sm_pkg NOT the user. This is so all objects created can be explicitly granted to the user
Compute results, call: rif40_sm_pkg.rif40_compute_results()
Do update. This forces verification
(i.e. change in study_State on rif40_studies calls rif40_sm_pkg.rif40_verify_state_change)
Recurse until complete';
   
CREATE OR REPLACE FUNCTION rif40_sm_pkg.rif40_create_extract(study_id INTEGER)
RETURNS BOOLEAN
SECURITY INVOKER
AS $func$
DECLARE
/*
Function:	rif40_create_extract()
Parameter:	Study ID
Returns:	Success or failure [BOOLEAN]
		Note this is to allow SQL executed by study extraction/results created to be logged (Postgres does not allow autonomous transactions)
		Verification and error checking raises EXCEPTIONS in the usual way; and will cause the SQL log to be lost
Description:	Create extract table. Calls rif40_insert_extract() to populate extract table.

Notes:

1. SQL created by rif40_study_ddl_definer() runs as rif40_sm_pkg NOT the user. This is so all objects created can be explicitly granted to the user
2. Same on Postgres and Postgres/Oracle FDW variants. Oracle remote execution is handled by rif40_insert_extract()

Check extract table does not exist
Check extract is permitted

Create extract table

The basis for this is the performance tests created from the new EHA extract for Lea in September 2012
The table has the following standard columns

CREATE TABLE <extract_table> (
 	year                    SMALLINT 	NOT NULL,
	study_or_comparison	VARCHAR(1) 	NOT NULL,
	study_id		INTEGER 	NOT NULL,
 	area_id                 VARCHAR 	NOT NULL,
	band_id			INTEGER,
 	sex                     SMALLINT,
 	age_group               VARCHAR,
 	total_pop               DOUBLE PRECISION,

One column per distinct covariate

 	<rif40_inv_covariates.covariate_name>    VARCHAR,

One column per investigation

 	<rif40_investigations>.<inv_name>        VARCHAR);

Index: year, study_or_comparison if no partitoning
       area_id, band_id, sex, age_group

Comment extract table and columns

Grant to study owner and all grantees in rif40_study_shares if extract_permitted=1 

Call rif40_sm_pkg.rif40_study_ddl_definer (i.e. runs as rif40_sm_pkg owner rif40) to process

Call rif40_insert_extract() to populate extract table.

Vacuum analyze

Partitioned by RANGE year

Call rif40_sm_pkg.rif40_study_ddl_definer (i.e. runs as rif40_sm_pkg owner rif40) to process
 */
	c1_creex CURSOR(l_study_id INTEGER) FOR
		SELECT *
		  FROM rif40_studies a
		 WHERE l_study_id = a.study_id;
	c1a_creex CURSOR(l_study_id INTEGER) FOR
		SELECT *
		  FROM rif40_studies a
		 WHERE l_study_id = a.study_id;
	c2_creex CURSOR(l_study_id INTEGER) FOR
		SELECT DISTINCT(a.covariate_name) AS covariate_name
		  FROM rif40_inv_covariates a
		 WHERE l_study_id = a.study_id
		 ORDER BY a.covariate_name;
	c3_creex CURSOR(l_study_id INTEGER) FOR
		SELECT *
		  FROM rif40_investigations a
		 WHERE l_study_id = a.study_id
  		 ORDER BY inv_id;
	c4_creex CURSOR(l_study_id INTEGER) FOR
		SELECT *
		  FROM rif40_study_shares a
		 WHERE l_study_id = a.study_id;
	c1_rec RECORD;
	c1a_rec RECORD;
	c2_rec RECORD;
	c3_rec RECORD;
	c4_rec RECORD;
--
	schema 		VARCHAR;
--
	sql_stmt 	VARCHAR;
	ddl_stmts	VARCHAR[];
	t_ddl		INTEGER:=0;
--
	index_column	VARCHAR;
	index_columns 	VARCHAR[] := ARRAY['area_id', 'band_id', 'sex', 'age_group']; 
	table_column	VARCHAR;
	table_columns	VARCHAR[] := ARRAY['year', 'study_or_comparison', 'study_id', 'area_id',
		'band_id', 'sex',  'age_group', 'total_pop'];
	column_comments	VARCHAR[] := ARRAY['Year', 'Study (S) or comparison (C) area', 'Study ID', 'Area ID',
		'Band ID', 'Sex',  'Age group', 'Total population'];
	i		INTEGER:=0;
BEGIN
	OPEN c1_creex(study_id);
	FETCH c1_creex INTO c1_rec;
	IF NOT FOUND THEN
		CLOSE c1_creex;
		PERFORM rif40_log_pkg.rif40_error(-90601, 'rif40_create_extract', 
			'Study ID % not found',
			study_id::VARCHAR);
	END IF;
	CLOSE c1_creex;
	OPEN c1a_creex(study_id);
	FETCH c1a_creex INTO c1a_rec;
	CLOSE c1a_creex;
--
-- Check extract table does not exist
--
	IF c1_rec.extract_table IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-90602, 'rif40_create_extract', 
			'RIF40_STUDIES study % extract table: not defined',
			c1_rec.study_id::VARCHAR	/* Study id */);
	END IF;
	schema:=rif40_sql_pkg.rif40_object_resolve(c1_rec.extract_table::VARCHAR);
	IF schema IS NULL AND c1_rec.study_state = 'V'  THEN
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_create_extract', 
			'[90603] RIF40_STUDIES study % extract table: % defined, awaiting creation',
			c1_rec.study_id::VARCHAR	/* Study id */,
			c1_rec.extract_table::VARCHAR 	/* extract_table */);
	ELSIF c1_rec.study_state != 'V' THEN
		PERFORM rif40_log_pkg.rif40_error(-90604, 'rif40_create_extract', 
			'RIF40_STUDIES study % extract table: %; in wrong state: %',
			c1_rec.study_id::VARCHAR	/* Study id */,
			c1_rec.extract_table::VARCHAR 	/* extract_table */,
			c1_rec.study_state::VARCHAR	/* State */);
	ELSE /* schema IS NOT NULL */
		PERFORM rif40_log_pkg.rif40_error(-90605, 'rif40_create_extract', 
			'RIF40_STUDIES study % extract table: %; exists in schema: %',
			c1_rec.study_id::VARCHAR	/* Study id */,
			c1_rec.extract_table::VARCHAR 	/* extract_table */,
			schema::VARCHAR			/* Schema */);
	END IF;         
--
-- Check extract is permitted
--
	IF c1_rec.extract_permitted != 1 THEN
		PERFORM rif40_log_pkg.rif40_error(-90606, 'rif40_create_extract', 
			'RIF40_STUDIES study % extract not currently permitted [use RIF IG tool]',
			c1_rec.study_id::VARCHAR	/* Study id */);
	END IF;

--
-- Check extract is bweing run by study owner
--
	IF c1_rec.username != USER THEN
		PERFORM rif40_log_pkg.rif40_error(-90607, 'rif40_create_extract', 
			'RIF40_STUDIES study % extract must be run by study owner % not %',
			c1_rec.study_id::VARCHAR	/* Study id */,
			c1_rec.username::VARCHAR	/* Study owner */,
			USER::VARCHAR			/* User of this function */);
	END IF;

--
-- Create extract table
--
-- The basis for this is the performance tests created from the new EHA extract for Lea in September 2012
-- The table has the following standard columns
--
--CREATE TABLE <extract_table> (
-- 	year                    SMALLINT 	NOT NULL,
--	study_or_comparison	VARCHAR(1) 	NOT NULL,
--	study_id		INTEGER 	NOT NULL,
-- 	area_id                 VARCHAR 	NOT NULL,
--	band_id			INTEGER,
-- 	sex                     SMALLINT,
-- 	age_group               VARCHAR,
-- 	total_pop               DOUBLE PRECISION,
--
	sql_stmt:='CREATE TABLE rif_studies.'||LOWER(c1_rec.extract_table)||' ('||E'\n'||
		 	E'\t'||'year                    SMALLINT 	NOT NULL,'||E'\n'||
			E'\t'||'study_or_comparison	VARCHAR(1) 	NOT NULL,'||E'\n'||
			E'\t'||'study_id		INTEGER 	NOT NULL,'||E'\n'||
 			E'\t'||'area_id                 VARCHAR 	NOT NULL,'||E'\n'||
			E'\t'||'band_id			INTEGER,'||E'\n'||
 			E'\t'||'sex                     SMALLINT,'||E'\n'||
 			E'\t'||'age_group               SMALLINT,'||E'\n';
--
-- One column per distinct covariate
--
-- 	<rif40_inv_covariates>.<covariate_name>    VARCHAR,
--
	FOR c2_rec IN c2_creex(study_id) LOOP
		sql_stmt:=sql_stmt||E'\t'||LOWER(c2_rec.covariate_name)||'               VARCHAR,'||E'\n'; /* Covariate value always coerced to a VARCHAR */
		table_columns:=array_append(table_columns, c2_rec.covariate_name::VARCHAR);
		column_comments:=array_append(column_comments, c2_rec.covariate_name::VARCHAR);
	END LOOP;
--
-- One column per investigation
--
-- 	<rif40_investigations>.<inv_name>          VARCHAR);
--
	FOR c3_rec IN c3_creex(study_id) LOOP
		sql_stmt:=sql_stmt||E'\t'||LOWER(c3_rec.inv_name)||'               BIGINT,'||E'\n';
		table_columns:=array_append(table_columns, LOWER(c3_rec.inv_name)::VARCHAR);
		column_comments:=array_append(column_comments, c3_rec.inv_description::VARCHAR);
	END LOOP;
	sql_stmt:=sql_stmt||E'\t'||'total_pop               DOUBLE PRECISION)';
--
	t_ddl:=t_ddl+1;	
	ddl_stmts[t_ddl]:=sql_stmt;

--
-- Partitioned by RANGE year, study_or_comparison
--

--
-- Comment extract table and columns
--
	sql_stmt:='COMMENT ON TABLE rif_studies.'||LOWER(c1_rec.extract_table)||' IS '''||c1_rec.description||'''';
	t_ddl:=t_ddl+1;	
	ddl_stmts[t_ddl]:=sql_stmt;
	FOREACH table_column IN ARRAY table_columns LOOP
		i:=i+1;
		sql_stmt:='COMMENT ON COLUMN rif_studies.'||LOWER(c1_rec.extract_table)||'.'||table_column||' IS '''||column_comments[i]||'''';
		t_ddl:=t_ddl+1;	
		ddl_stmts[t_ddl]:=sql_stmt;
	END LOOP;

--
-- Grant to study owner and all grantees in rif40_study_shares if extract_permitted=1 
--
	IF c1_rec.extract_permitted = 1 THEN
		sql_stmt:='GRANT SELECT,INSERT,TRUNCATE ON rif_studies.'||LOWER(c1_rec.extract_table)||' TO '||USER;
		t_ddl:=t_ddl+1;	
		ddl_stmts[t_ddl]:=sql_stmt;
		FOR c4_rec IN c4_creex(study_id) LOOP
			sql_stmt:='GRANT SELECT,INSERT ON rif_studies.'||LOWER(c1_rec.extract_table)||' TO '||c4_rec.grantee_username;
			t_ddl:=t_ddl+1;	
			ddl_stmts[t_ddl]:=sql_stmt;
		END LOOP;
	END IF;

--
-- Call rif40_sm_pkg.rif40_study_ddl_definer (i.e. runs as rif40_sm_pkg owner rif40)
--
	IF rif40_sm_pkg.rif40_study_ddl_definer(c1_rec.study_id, c1_rec.username, c1a_rec.audsid, ddl_stmts) = FALSE THEN
		PERFORM rif40_log_pkg.rif40_log ('WARNING', 'rif40_create_extract', 
			'[90607] RIF40_STUDIES study % extract creation failed, see previous warnings',
			c1_rec.study_id::VARCHAR	/* Study id */);
		RETURN FALSE;
	END IF;
--
-- Call rif40_insert_extract() to populate extract table.
-- 
	IF rif40_sm_pkg.rif40_insert_extract(c1_rec.study_id) = FALSE THEN
		PERFORM rif40_log_pkg.rif40_log ('WARNING', 'rif40_create_extract', 
			'[90608] RIF40_STUDIES study % populated extract failed, see previous warnings',
			c1_rec.study_id::VARCHAR	/* Study id */);
		RETURN FALSE;
	END IF;
--
-- Reset DDL statement array
--
	ddl_stmts:=NULL;
	t_ddl:=0;
--
-- Index: year, study_or_comparison if no partitoning
--	  area_id, band_id, sex, age_group
--
-- NEEDS TO BE MOVED TO AFTER INSERT, ADD PK
--
	IF c1_rec.partition_parallelisation = 0 THEN
		index_columns:=array_cat(index_columns, ARRAY['year', 'study_or_comparison']::VARCHAR[]);
	END IF;
	FOREACH index_column IN ARRAY index_columns LOOP
		sql_stmt:='CREATE INDEX '||LOWER(c1_rec.extract_table)||'_'||index_column||
			' ON rif_studies.'||LOWER(c1_rec.extract_table)||'('||index_column||')';
		t_ddl:=t_ddl+1;	
		ddl_stmts[t_ddl]:=sql_stmt;
	END LOOP;

--
-- Vacuum analyze - raises 25001 "VACUUM cannot run inside a transaction block"
--
-- DEFER TO LATER
--
	sql_stmt:='ANALYZE rif_studies.'||LOWER(c1_rec.extract_table);
	t_ddl:=t_ddl+1;	
	ddl_stmts[t_ddl]:=sql_stmt;
--
-- Call rif40_sm_pkg.rif40_study_ddl_definer (i.e. runs as rif40_sm_pkg owner rif40)
--
	IF rif40_sm_pkg.rif40_study_ddl_definer(c1_rec.study_id, c1_rec.username, c1a_rec.audsid, ddl_stmts) = FALSE THEN
		PERFORM rif40_log_pkg.rif40_log ('WARNING', 'rif40_create_extract', 
			'[90609] RIF40_STUDIES study % extract index/analyze failed, see previous warnings',
			c1_rec.study_id::VARCHAR	/* Study id */);
		RETURN FALSE;
	END IF;
--
	RETURN TRUE;
END;
$func$
LANGUAGE 'plpgsql';

GRANT EXECUTE ON FUNCTION rif40_sm_pkg.rif40_create_extract(INTEGER) TO rif40;
COMMENT ON FUNCTION rif40_sm_pkg.rif40_create_extract(INTEGER) IS 'Function:	rif40_create_extract()
Parameter:	Study ID
Returns:	Success or failure [BOOLEAN]
		Note this is to allow SQL executed by study extraction/results created to be logged (Postgres does not allow autonomous transactions)
		Verification and error checking raises EXCEPTIONS in the usual way; and will cause the SQL log to be lost
Description:	Create extract table. Calls rif40_insert_extract() to populate extract table.

Notes:

1. SQL created by rif40_study_ddl_definer() runs as rif40_sm_pkg NOT the user. This is so all objects created can be explicitly granted to the user
2. Same on Postgres and Postgres/Oracle FDW variants. Oracle remote execution is handled by rif40_insert_extract()

Check extract table does not exist
Check extract is permitted

Create extract table

The basis for this is the performance tests created from the new EHA extract for Lea in September 2012
The table has the following standard columns

CREATE TABLE <extract_table> (
 	year                    SMALLINT 	NOT NULL,
	study_or_comparison	VARCHAR(1) 	NOT NULL,
	study_id		INTEGER 	NOT NULL,
 	area_id                 VARCHAR 	NOT NULL,
	band_id			INTEGER,
 	sex                     SMALLINT,
 	age_group               VARCHAR,
 	total_pop               DOUBLE PRECISION,

One column per distinct covariate

 	<rif40_inv_covariates.covariate_name>    VARCHAR,

One column per investigation

 	<rif40_investigations>.<inv_name>        VARCHAR);

Index: year, study_or_comparison if no partitoning
       area_id, band_id, sex, age_group

Comment extract table and columns

Grant to study owner and all grantees in rif40_study_shares if extract_permitted=1 

Call rif40_sm_pkg.rif40_study_ddl_definer (i.e. runs as rif40_sm_pkg owner rif40) to process

Call rif40_insert_extract() to populate extract table.

Vacuum analyze

Partitioned by RANGE year

Call rif40_sm_pkg.rif40_study_ddl_definer (i.e. runs as rif40_sm_pkg owner rif40) to process';

CREATE OR REPLACE FUNCTION rif40_sm_pkg.rif40_study_ddl_definer(study_id INTEGER, username VARCHAR, audsid VARCHAR, ddl_stmts VARCHAR[])
RETURNS BOOLEAN
SECURITY DEFINER /* i.e. RIF40 */
AS $func$
DECLARE
/*
Function:	rif40_study_ddl_definer()
Parameter:	Study ID, username [study owner], audsid, DDL statements to execute
Returns:	Success or failure [BOOLEAN]
		Note this is to allow SQL executed by study extraction/results created to be logged (Postgres does not allow autonomous transactions)
		Verification and error checking raises EXCEPTIONS in the usual way; and will cause the SQL log to be lost
Description:	Execute DDL statements as RIF40 (so the user cannot control access to extracted data)
		Log statements. Stop and return FALSE on error
Notes:

1. SQL created runs as rif40_sm_pkg NOT the user. This is so all objects created can be explicitly granted to the user
2. Verify AUDSID and USER

	Verify AUDSID and USER
	Study AUDSID and function AUDSID must be the same
	Study username and session username must be the same
	Study username and function username must be the same
	Study AUDSID and session AUDSID must be the same
	Confirm calling function is rif40_create_extract() [needs 9.2]

   Note that this currently limits this function to being executed by rif40_create_extract()

Issues:

Fix statement type in view/make it an integer

psql:v4_0_sahsuland_examples.sql:178: ERROR:  column "statement_type" is of type smallint but expression is of type text
LINE 11:     NEW.statement_type -* no default value *-,
             ^
HINT:  You will need to rewrite or cast the expression.
QUERY:  INSERT INTO t_rif40_study_sql (
                                username,
                                study_id,
                                statement_type,
                                statement_number,
                                sql_text,
                                line_number)
                        VALUES(
                                coalesce(NEW.username, "current_user"()),
                                coalesce(NEW.study_id, (currval('rif40_study_id_seq'::regclass))::integer),
                                NEW.statement_type -* no default value *-,
                                NEW.statement_number -* no default value *-,
                                NEW.sql_text -* no default value *-,
                                NEW.line_number -* no default value *-)
CONTEXT:  PL/pgSQL function "trgf_rif40_study_sql" line 8 at SQL statement
SQL statement "INSERT INTO rif40_study_sql(statement_type, statement_number, sql_text, line_number)
                VALUES (1::INTEGER -* Local Postgres statement *-, t_ddl, sql_stmt, 1::INTEGER)"
PL/pgSQL function "rif40_create_extract" line 193 at SQL statement

 */
	c1stddl CURSOR(l_study_id INTEGER) FOR
		SELECT MAX(statement_number) AS max_statement_number
		  FROM t_rif40_study_sql_log a
		 WHERE l_study_id = a.study_id;
	c2stddl CURSOR(l_study_id INTEGER) FOR
		SELECT *
		  FROM t_rif40_studies a
		 WHERE l_study_id = a.study_id;
	c1_rec RECORD;
	c2_rec RECORD;
--
	sql_stmt 	VARCHAR;
	t_ddl		INTEGER:=0;
	l_rows		INTEGER:=0;
	stp		TIMESTAMP WITH TIME ZONE := clock_timestamp();
	etp		TIMESTAMP WITH TIME ZONE;
	elapsed_time	FLOAT:=0;
	l_log_message	VARCHAR:='OK';
	l_sqlcode	VARCHAR:='00000';
--
-- INSERT INTO t_rif_study_sql (view wont work until statement_type is fixed
--
-- 1 CREATE
-- 2 INSERT
-- 3 POST_INSERT
-- 4 NUMERATOR_CHECK
-- 5 DENOMINATOR_CHECK
--
	l_statement_type VARCHAR:='CREATE' /* CREATE: Local Postgres statement */;
	session_audsid	VARCHAR:=SYS_CONTEXT('USERENV', 'SESSIONID');
BEGIN
--
-- Verify AUDSID and USER
-- Study AUDSID and function AUDSID must be the same
-- Study username and session username must be the same
-- Study username and function username must be the same
-- Study AUDSID and session AUDSID must be the same
-- 
-- Note that this currently limits this function to being executed by rif40_run_study()
--
	OPEN c2stddl(study_id);
	FETCH c2stddl INTO c2_rec;
	IF NOT FOUND THEN
		CLOSE c2stddl;
		PERFORM rif40_log_pkg.rif40_error(-90620, 'rif40_study_ddl_definer', 
			'Study ID % not found',
			study_id::VARCHAR		/* Study ID */);
	END IF;
	CLOSE c2stddl;
	IF c2_rec.audsid != audsid THEN
		PERFORM rif40_log_pkg.rif40_error(-90621, 'rif40_study_ddl_definer', 
			'Study ID % wrong audsid, expecting: %; got: %',
			study_id::VARCHAR		/* Study ID */,
			c2_rec.audsid::VARCHAR		/* Record AUDSID */,
			audsid::VARCHAR			/* Function AUDSID */);
	ELSIF c2_rec.username != username THEN
		PERFORM rif40_log_pkg.rif40_error(-90622, 'rif40_study_ddl_definer', 
			'Study ID % wrong username, expecting: %; got: %',
			study_id::VARCHAR		/* Study ID */,
			c2_rec.username::VARCHAR	/* Record USERNAME */,
			username::VARCHAR		/* Function USERNAME */);
	ELSIF session_user != username THEN
		PERFORM rif40_log_pkg.rif40_error(-90623, 'rif40_study_ddl_definer', 
			'Study ID % wrong session username, expecting: %; got: %; execution context user: %',
			study_id::VARCHAR		/* Study ID */,
			session_user::VARCHAR		/* Session USERNAME */,
			username::VARCHAR		/* Function USERNAME */,
			current_user::VARCHAR		/* Execution context USERNAME */);
	ELSIF session_audsid != audsid THEN
		PERFORM rif40_log_pkg.rif40_error(-90624, 'rif40_study_ddl_definer', 
			'Study ID % wrong session AUDSID, expecting: %; got: %; execution context user: %',
			study_id::VARCHAR		/* Study ID */,
			session_audsid::VARCHAR		/* Session USERNAME */,
			audsid::VARCHAR			/* Function USERNAME */,
			current_user::VARCHAR		/* Execution context USERNAME */);
	ELSE
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_study_ddl_definer', 
			'[90625] Study ID % session, record and function username verified: %; AUDSID verified: %, execution context user: %',
			study_id::VARCHAR		/* Study ID */,
			session_user::VARCHAR		/* Session USERNAME */,
			audsid::VARCHAR			/* Function AUDSID */,
			current_user::VARCHAR		/* Execution context USERNAME (RIF40) */);
	END IF;

--
-- Confirm calling function is rif40_create_extract() [needs 9.3/PG_CONTEXT]
--

--
-- Get the maximum statement number for the logs
--
	OPEN c1stddl(study_id);
	FETCH c1stddl INTO c1_rec;
	IF NOT FOUND THEN
		CLOSE c1stddl;
		PERFORM rif40_log_pkg.rif40_error(-90626, 'rif40_study_ddl_definer', 
			'Study ID % not found',
			study_id::VARCHAR		/* Study ID */);
	END IF;
	CLOSE c1stddl;
	t_ddl:=COALESCE(c1_rec.max_statement_number, 0);
	IF t_ddl > 0 THEN
		l_statement_type:='POST_INSERT';
	END IF;
--
	FOREACH sql_stmt IN ARRAY ddl_stmts LOOP
		t_ddl:=t_ddl+1;	
--
-- Execute statement
--
		IF sql_stmt IS NOT NULL THEN
			BEGIN
				l_rows:=rif40_sql_pkg.rif40_ddl(sql_stmt);
--
				etp:=clock_timestamp();
				l_log_message:='OK, took: '||age(etp, stp);
 		     	EXCEPTION
--
-- Handle all errors
--
				WHEN others THEN
					etp:=clock_timestamp();
					l_sqlcode:=SQLSTATE;
					l_log_message:=format('ERROR, [90627] %s "%s" raised, took: %s', 
						l_sqlcode::VARCHAR		/* Error */,
						sqlerrm::VARCHAR		/* Error */,
						age(etp, stp)::VARCHAR		/* Human readable time take */);      
					PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_study_ddl_definer',
						'[90628] Study %: statement % error: % "%" raised by'||E'\n'||'SQL> %;'||E'\n'||' after: %',
						study_id::VARCHAR,		/* Study ID */
						t_ddl::VARCHAR			/* Statement number */,
						l_sqlcode::VARCHAR		/* Error */,
						sqlerrm::VARCHAR		/* Error */,
						sql_stmt::VARCHAR		/* SQL */,
						age(etp, stp)::VARCHAR		/* Human readable time take */);      
			END;
--
			elapsed_time:=EXTRACT(EPOCH FROM etp-stp);
--
-- Log statement 
--
			INSERT INTO t_rif40_study_sql_log(
				username, study_id, statement_type, statement_number, log_message, log_sqlcode, rowcount, start_time, elapsed_time, audsid)
			VALUES (
				username, study_id,l_statement_type, 
				t_ddl, l_log_message, l_sqlcode, coalesce(l_rows, 0), stp, elapsed_time, audsid);
			INSERT INTO t_rif40_study_sql(username, study_id, statement_type, statement_number, sql_text, line_number)
			VALUES (username, study_id, l_statement_type, t_ddl, sql_stmt, 1);  
--
--  Detect failure
--
			EXIT WHEN l_sqlcode != '00000' /* OK */;
		END IF;
	END LOOP;
--
	IF l_sqlcode != '00000' /* OK */ THEN
		PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_study_ddl_definer',
			'[90629] Study %: error prevents further processing',
			study_id::VARCHAR		/* Study ID */);
		RETURN FALSE;
	ELSE
		RETURN TRUE;
	END IF;
END;
$func$
LANGUAGE 'plpgsql';

GRANT EXECUTE ON FUNCTION rif40_sm_pkg.rif40_study_ddl_definer(INTEGER, VARCHAR, VARCHAR, VARCHAR[]) TO rif40;
COMMENT ON FUNCTION rif40_sm_pkg.rif40_study_ddl_definer(INTEGER, VARCHAR, VARCHAR, VARCHAR[]) IS 'Function:	rif40_study_ddl_definer()
Parameter:	Study ID, username [study owner], audsid, DDL statements to execute
Returns:	Success or failure [BOOLEAN]
		Note this is to allow SQL executed by study extraction/results created to be logged (Postgres does not allow autonomous transactions)
		Verification and error checking raises EXCEPTIONS in the usual way; and will cause the SQL log to be lost
Description:	Execute DDL statements as RIF40 (so the user cannot control access to extracted data)
		Log statements. Stop and return FALSE on error
Notes:

1. SQL created runs as rif40_sm_pkg NOT the user. This is so all objects created can be explicitly granted to the user
2. Verify AUDSID and USER

	Verify AUDSID and USER
	Study AUDSID and function AUDSID must be the same
	Study username and session username must be the same
	Study username and function username must be the same
	Study AUDSID and session AUDSID must be the same
	Confirm calling function is rif40_create_extract() [needs 9.3]

   Note that this currently limits this function to being executed by rif40_create_extract()
';

CREATE OR REPLACE FUNCTION rif40_sm_pkg.rif40_insert_extract(study_id INTEGER)
RETURNS BOOLEAN
SECURITY INVOKER
AS $func$
DECLARE
/*
Function:	rif40_insert_extract()
Parameter:	Study ID
Returns:	Success or failure [BOOLEAN]
		Note this is to allow SQL executed by study extraction/results created to be logged (Postgres does not allow autonomous transactions)
		Verification and error checking raises EXCEPTIONS in the usual way; and will cause the SQL log to be lost
Description:	Insert data into extract table


 */
	c1insext2 CURSOR(l_study_id INTEGER) FOR
		SELECT * 
		  FROM rif40_studies a
		 WHERE a.study_id = l_study_id;
	c1_rec RECORD;
--
	sql_stmt		VARCHAR;
--
	stp		TIMESTAMP WITH TIME ZONE := clock_timestamp();
	etp		TIMESTAMP WITH TIME ZONE;
BEGIN
	OPEN c1insext2(study_id);
	FETCH c1insext2 INTO c1_rec;
	IF NOT FOUND THEN
		CLOSE c1insext2;
		PERFORM rif40_log_pkg.rif40_error(-90641, 'rif40_insert_extract', 
			'Study ID % not found',
			study_id::VARCHAR		/* Study ID */);
	END IF;
	CLOSE c1insext2;
--
-- Study area insert
--
	sql_stmt:='INSERT INTO g_rif40_study_areas(study_id, area_id, band_id)'||E'\n'||
		'SELECT study_id, area_id, band_id FROM rif40_study_areas WHERE study_id = $1 /* Current study ID */'::VARCHAR;
	IF rif40_sm_pkg.rif40_execute_insert_statement(study_id, sql_stmt, 'Study area insert'::VARCHAR) = FALSE THEN 
		RETURN FALSE;
	END IF;
--
-- Study extract insert
--	
-- This will eventually support paralleisation. Do year by year for the moment
--
	FOR i IN c1_rec.year_start .. c1_rec.year_stop LOOP
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_insert_extract', 
			'[90642] Study ID % INSERT study year %',
			study_id::VARCHAR		/* Study ID */,
			i::VARCHAR);
--
-- Do explain plan at the same time
--
		IF i = c1_rec.year_start THEN
			sql_stmt:=rif40_sm_pkg.rif40_create_insert_statement(study_id, 'S', i, i);
			IF rif40_sm_pkg.rif40_execute_insert_statement(study_id, 
				'EXPLAIN (VERBOSE, FORMAT text)'||E'\n'||sql_stmt, 
				'Study extract insert '||i::VARCHAR||' (EXPLAIN)'::VARCHAR, i, i) = FALSE THEN 
				RETURN FALSE;
			ELSIF rif40_sm_pkg.rif40_execute_insert_statement(study_id, 
				'EXPLAIN (ANALYZE, VERBOSE, COSTS, BUFFERS, /* TIMING, (9.2+) */ FORMAT text)'||E'\n'||sql_stmt, 
				'Study extract '||i::VARCHAR||' insert (EXPLAIN ANALYZE)'::VARCHAR, i, i) = FALSE THEN 
				RETURN FALSE;
			END IF;
		ELSIF rif40_sm_pkg.rif40_execute_insert_statement(study_id, sql_stmt, 
			'Study extract insert '||i::VARCHAR, i, i) = FALSE THEN 
			RETURN FALSE;
		END IF;
	END LOOP;
--
-- Comparison area insert
--
	sql_stmt:='INSERT INTO g_rif40_comparison_areas(study_id, area_id)'||E'\n'||
		'SELECT study_id, area_id FROM rif40_comparison_areas WHERE study_id = $1 /* Current study ID */'::VARCHAR;
	IF rif40_sm_pkg.rif40_execute_insert_statement(study_id, sql_stmt, 'Comparison area insert'::VARCHAR) = FALSE THEN 
		RETURN FALSE;
	END IF;
--
-- Comparison extract insert
--	
-- This will eventually support paralleisation. Do year by year for the moment
--
	FOR i IN c1_rec.year_start .. c1_rec.year_stop LOOP
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_insert_extract', 
			'[90643] Study ID % INSERT comparison year %',
			study_id::VARCHAR		/* Study ID */,
			i::VARCHAR);
--
-- Do explain plan at the same time
--
		IF i = c1_rec.year_start THEN
			sql_stmt:=rif40_sm_pkg.rif40_create_insert_statement(study_id, 'C', i, i);
			IF rif40_sm_pkg.rif40_execute_insert_statement(study_id, 
				'EXPLAIN (VERBOSE, FORMAT text)'||E'\n'||sql_stmt, 
				'Comparison extract insert '||i::VARCHAR||' (EXPLAIN)'::VARCHAR, i, i) = FALSE THEN 
				RETURN FALSE;
			ELSIF rif40_sm_pkg.rif40_execute_insert_statement(study_id, 
				'EXPLAIN (ANALYZE, VERBOSE, COSTS, BUFFERS, /* TIMING, (9.2+) */ FORMAT text)'||E'\n'||sql_stmt, 
				'Comparison extract '||i::VARCHAR||' insert (EXPLAIN ANALYZE)'::VARCHAR, i, i) = FALSE THEN 
				RETURN FALSE;
			END IF;
		ELSIF rif40_sm_pkg.rif40_execute_insert_statement(study_id, sql_stmt, 
			'Comparison extract insert '||i::VARCHAR, i, i) = FALSE THEN 
			RETURN FALSE;
		END IF;
	END LOOP;
--
	etp:=clock_timestamp();
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_insert_extract', 
		'[90644] Study ID % extract table INSERT in %',
		study_id::VARCHAR		/* Study ID */,
		age(etp, stp)::VARCHAR);
--
	RETURN TRUE;
--
END;
$func$
LANGUAGE 'plpgsql';

GRANT EXECUTE ON FUNCTION rif40_sm_pkg.rif40_insert_extract(INTEGER) TO rif40;
COMMENT ON FUNCTION rif40_sm_pkg.rif40_insert_extract(INTEGER) IS 'A';

CREATE OR REPLACE FUNCTION rif40_sm_pkg.rif40_execute_insert_statement(study_id INTEGER, sql_stmt VARCHAR, description VARCHAR, 
	year_start INTEGER DEFAULT NULL, year_stop INTEGER DEFAULT NULL)
RETURNS BOOLEAN 
SECURITY INVOKER
AS $func$
DECLARE
/*
Function:	rif40_execute_insert_statement()
Parameter:	Study ID, SQL statement, description, year_start, year_stop
Returns:	Success or failure [BOOLEAN]
		Note this is to allow SQL executed by study extraction/results created to be logged (Postgres does not allow autonomous transactions)
		Verification and error checking raises EXCEPTIONS in the usual way; and will cause the SQL log to be lost
Description:	Execute INSERT SQL statement
		Log statements. Stop and return FALSE on error
 */
	c1exinst CURSOR(l_study_id INTEGER) FOR
		SELECT MAX(statement_number) AS max_statement_number
		  FROM rif40_study_sql_log a
		 WHERE l_study_id = a.study_id;
	c2exinst REFCURSOR;
	c1_rec RECORD;
--
	l_rows		INTEGER:=0;
	stp		TIMESTAMP WITH TIME ZONE := clock_timestamp();
	etp		TIMESTAMP WITH TIME ZONE;
	elapsed_time	FLOAT:=0;
	l_log_message	VARCHAR:='OK';
	l_sqlcode	VARCHAR:='00000';
	t_ddl 		INTEGER;
	query_plan	VARCHAR[];
	query_plan_text	VARCHAR;
--
-- INSERT INTO t_rif_study_sql (view wont work until statement_type is fixed
--
-- 1 CREATE
-- 2 INSERT
-- 3 POST_INSERT
-- 4 NUMERATOR_CHECK
-- 5 DENOMINATOR_CHECK
--
	l_statement_type VARCHAR:='INSERT' /* INSERT: Local Postgres statement */;
	i		INTEGER:=0;
BEGIN
--
-- Get the maximum statement number for the logs
--
	OPEN c1exinst(study_id);
	FETCH c1exinst INTO c1_rec;
	IF NOT FOUND THEN
		CLOSE c1exinst;
		PERFORM rif40_log_pkg.rif40_error(-90426, 'rif40_execute_insert_statement', 
			'Study ID % not found',
			study_id::VARCHAR		/* Study ID */);
	END IF;
	CLOSE c1exinst;
	t_ddl:=COALESCE(c1_rec.max_statement_number, 0)+1;
--
	BEGIN
		IF SUBSTR(sql_stmt, 1, 7) = 'EXPLAIN' THEN
			IF year_start IS NOT NULL AND year_stop IS NOT NULL AND year_start != year_stop THEN
				OPEN c2exinst FOR EXECUTE sql_stmt USING study_id, year_start, year_stop;
			ELSIF year_start IS NOT NULL AND year_stop IS NOT NULL AND year_start = year_stop THEN
				OPEN c2exinst FOR EXECUTE sql_stmt USING study_id, year_start;
			ELSE
				OPEN c2exinst FOR EXECUTE sql_stmt USING study_id;
			END IF;
			GET DIAGNOSTICS l_rows = ROW_COUNT;
 			LOOP
				i:=i+1;
				FETCH c2exinst INTO query_plan_text;
				EXIT WHEN NOT FOUND;
				query_plan[i]:=query_plan_text;
			END LOOP;
			etp:=clock_timestamp();
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_execute_insert_statement', 
				'[90427] Study ID %, statement: %'||E'\n'||'Description: %'||E'\n'||' query plan:'||E'\n'||'%'::VARCHAR,
				study_id::VARCHAR				/* Study ID */,
				t_ddl::VARCHAR					/* Statement number */,
				description::VARCHAR				/* Description */,
				array_to_string(query_plan, E'\n')::VARCHAR	/* Query plan */);
			l_log_message:=description::VARCHAR||' OK, took: '||age(etp, stp)::VARCHAR;
		ELSE
			IF year_start IS NOT NULL AND year_stop IS NOT NULL AND year_start != year_stop THEN
				EXECUTE sql_stmt USING study_id, year_start, year_stop;
			ELSIF year_start IS NOT NULL AND year_stop IS NOT NULL AND year_start = year_stop THEN
				EXECUTE sql_stmt USING study_id, year_start;
			ELSE
				EXECUTE sql_stmt USING study_id;
			END IF;
			GET DIAGNOSTICS l_rows = ROW_COUNT;
			etp:=clock_timestamp();
			l_log_message:=description::VARCHAR||' OK, inserted '||l_rows::VARCHAR||' rows, took: '||age(etp, stp)::VARCHAR;
		END IF;
     	EXCEPTION
--
-- Handle all errors
--
		WHEN others THEN
			etp:=clock_timestamp();
			l_sqlcode:=SQLSTATE;
			l_log_message:=format('ERROR, [90428] %s %s "%s" raised, took: %s', 
				description::VARCHAR		/* Description */,
				l_sqlcode::VARCHAR		/* SQLSTATE */,
				sqlerrm::VARCHAR		/* Error */,
				age(etp, stp)::VARCHAR		/* Human readable time take */);      
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_execute_insert_statement',
				'[90429] Study %: statement % (%) error: % "%" raised by'||E'\n'||'SQL> %;'||E'\n'||'After: %'::VARCHAR,
				study_id::VARCHAR		/* Study ID */,
				description::VARCHAR		/* Description */,
				t_ddl::VARCHAR			/* Statement number */,
				l_sqlcode::VARCHAR		/* SQLSTATE */,
				sqlerrm::VARCHAR		/* Error */,
				sql_stmt::VARCHAR		/* SQL */,
				age(etp, stp)::VARCHAR		/* Human readable time take */);      
			RETURN FALSE;
	END;
--
	elapsed_time:=EXTRACT(EPOCH FROM etp-stp);
--
-- Log statement 
--
	INSERT INTO rif40_study_sql_log(
		username, study_id, statement_type, statement_number, log_message, log_sqlcode, rowcount, start_time, elapsed_time)
	VALUES (
		USER, study_id,l_statement_type, 
		t_ddl, l_log_message, l_sqlcode, coalesce(l_rows, 0), stp, elapsed_time);
	INSERT INTO rif40_study_sql(username, study_id, statement_type, statement_number, sql_text, line_number)
	VALUES (USER, study_id, l_statement_type, t_ddl, sql_stmt, 1);  
--
	PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_execute_insert_statement',
		'[90430] Study %: %',
		study_id::VARCHAR,		/* Study ID */
		l_log_message::VARCHAR		/* Log message */);
--
	RETURN TRUE;
--
END;
$func$
LANGUAGE 'plpgsql';

GRANT EXECUTE ON FUNCTION rif40_sm_pkg.rif40_execute_insert_statement(INTEGER, VARCHAR, VARCHAR, INTEGER, INTEGER) TO rif40;
COMMENT ON FUNCTION rif40_sm_pkg.rif40_execute_insert_statement(INTEGER, VARCHAR, VARCHAR, INTEGER, INTEGER) IS 'Function:	rif40_execute_insert_statement()
Parameter:	Study ID, SQL statement, description, year_start, year_stop
Returns:	Success or failure [BOOLEAN]
		Note this is to allow SQL executed by study extraction/results created to be logged (Postgres does not allow autonomous transactions)
		Verification and error checking raises EXCEPTIONS in the usual way; and will cause the SQL log to be lost
Description:	Execute INSERT SQL statement
		Log statements. Stop and return FALSE on error';

CREATE OR REPLACE FUNCTION rif40_sm_pkg.rif40_create_insert_statement(study_id INTEGER, study_or_comparison VARCHAR, 
	year_start INTEGER DEFAULT NULL, year_stop INTEGER DEFAULT NULL)
RETURNS VARCHAR
SECURITY INVOKER
AS $func$
DECLARE
/*
Function:	rif40_create_insert_statement()
Parameter:	Study ID, study or comparison (S/C), year_start, year_stop
Returns:	SQL statement
Description:	Create INSERT SQL statement
 */
	c1insext CURSOR(l_study_id INTEGER) FOR
		SELECT * 
		  FROM rif40_studies a
		 WHERE a.study_id = l_study_id;
	c2insext CURSOR(l_table VARCHAR) FOR
		SELECT *
		  FROM information_schema.columns a
		 WHERE a.table_schema = 'rif_studies'
		   AND a.table_name   = l_table
		 ORDER BY a.ordinal_position;
	c3insext CURSOR(l_study_id INTEGER) FOR
		SELECT COUNT(DISTINCT(numer_tab)) AS distinct_numerators
		  FROM rif40_investigations a
		 WHERE a.study_id = l_study_id;
	c4insext CURSOR(l_study_id INTEGER) FOR
		WITH b AS (
			SELECT DISTINCT(a.numer_tab) AS numer_tab
			  FROM rif40_investigations a
		 	 WHERE a.study_id   = l_study_id
		)
		SELECT b.numer_tab,
 		       t.description, t.total_field, t.year_start, t.year_stop, 
   		       t.sex_field_name, t.age_group_field_name, t.age_sex_group_field_name, t.age_group_id
		  FROM b, rif40_tables t 
		 WHERE t.table_name = b.numer_tab;
	c5insext CURSOR(l_study_id INTEGER, l_numer_tab VARCHAR) FOR
		SELECT *
		  FROM rif40_investigations a
		 WHERE a.study_id  = l_study_id
		   AND a.numer_tab = l_numer_tab
 		 ORDER BY inv_id;
	c6insext CURSOR(l_study_id INTEGER, l_inv_id INTEGER) FOR
		SELECT *
		  FROM rif40_inv_conditions a
		 WHERE a.study_id = l_study_id
		   AND a.inv_id = l_inv_id
 		 ORDER BY inv_id, line_number;
	c7insext CURSOR(l_study_id INTEGER) FOR
		SELECT DISTINCT covariate_name AS covariate_name
		  FROM rif40_inv_covariates a
		 WHERE a.study_id = l_study_id
 		 ORDER BY covariate_name;
	c8insext CURSOR(l_study_id INTEGER) FOR
		SELECT b.denom_tab,
 		       t.description, t.total_field, t.year_start, t.year_stop,
   		       t.sex_field_name, t.age_group_field_name, t.age_sex_group_field_name, t.age_group_id, 
		       MIN(a.offset) AS min_age_group, MAX(a.offset) AS max_age_group
		  FROM rif40_studies b, rif40_tables t, rif40_age_groups a
		 WHERE t.table_name   = b.denom_tab
		   AND t.age_group_id = a.age_group_id
		   AND b.study_id     = l_study_id
		 GROUP BY b.denom_tab,
 		       t.description, t.total_field, t.year_start, t.year_stop,
   		       t.sex_field_name, t.age_group_field_name, t.age_sex_group_field_name, t.age_group_id;
	c1_rec RECORD;
	c2_rec RECORD;
	c3_rec RECORD;
	c4_rec RECORD;
	c5_rec RECORD;
	c6_rec RECORD;
	c7_rec RECORD;
	c8_rec RECORD;
--
	sql_stmt	VARCHAR;
	i		INTEGER:=0;
	j		INTEGER:=0;
	k		INTEGER:=0;
	inv_array	VARCHAR[];
	inv_join_array	VARCHAR[];
--
	areas_table	VARCHAR:='g_rif40_study_areas';
BEGIN
--
-- Use different areas_table for comparision (it has no band_id)
--	
	IF study_or_comparison = 'C' THEN
		areas_table:='g_rif40_comparison_areas';
	END IF;
	OPEN c1insext(study_id);
	FETCH c1insext INTO c1_rec;
	IF NOT FOUND THEN
		CLOSE c1insext;
		PERFORM rif40_log_pkg.rif40_error(-90641, 'rif40_create_insert_statement', 
			'Study ID % not found',
			study_id::VARCHAR		/* Study ID */);
	END IF;
	CLOSE c1insext;
--
-- Create INSERT statement
-- 
	sql_stmt:='INSERT INTO '||LOWER(c1_rec.extract_table)||' ('||E'\n';
--
-- Add columns
-- 
	FOR c2_rec IN c2insext(LOWER(c1_rec.extract_table)) LOOP
		i:=i+1;
		IF i = 1 THEN
			sql_stmt:=sql_stmt||E'\t'||c2_rec.column_name;
		ELSE
			sql_stmt:=sql_stmt||','||c2_rec.column_name;
		END IF;
	END LOOP;
	IF i = 0 THEN
		PERFORM rif40_log_pkg.rif40_error(-90642, 'rif40_create_insert_statement', 
			'Study ID % no columns found for extract table: %',
			study_id::VARCHAR		/* Study ID */,		
			c1_rec.extract_table::VARCHAR 	/* Extract table */);
	END IF;
--
-- Get number of distinct numerators
--
	OPEN c3insext(study_id);
	FETCH c3insext INTO c3_rec;
	IF NOT FOUND THEN
		CLOSE c3insext;
		PERFORM rif40_log_pkg.rif40_error(-90643, 'rif40_create_insert_statement', 
			'Study ID % not found',
			study_id::VARCHAR		/* Study ID */);
	END IF;
	CLOSE c3insext;
	sql_stmt:=sql_stmt||') /* '||c3_rec.distinct_numerators::VARCHAR||' numerator(s) */'||E'\n';
--
-- Get denominator setup
--
	OPEN c8insext(study_id);
	FETCH c8insext INTO c8_rec;
	IF NOT FOUND THEN
		CLOSE c8insext;
		PERFORM rif40_log_pkg.rif40_error(-90644, 'rif40_create_insert_statement', 
			'Study ID % not found',
			study_id::VARCHAR		/* Study ID */);
	END IF;
	CLOSE c8insext;
--
--
-- Loop through distinct numerators
--
	i:=0;
	FOR c4_rec IN c4insext(study_id) LOOP
		i:=i+1;
--
-- Open WITH clause (common table expression)
-- 
		IF i = 1 THEN
			sql_stmt:=sql_stmt||'WITH n'||i::VARCHAR||' AS ('||E'\t'||'/* '||c4_rec.numer_tab||' - '||c4_rec.description||' */'||E'\n';
		ELSE
			sql_stmt:=sql_stmt||', n'||i::VARCHAR||' AS ('||E'\t'||'/* '||c4_rec.numer_tab||' - '||c4_rec.description||' */'||E'\n';
		END IF;
--
-- Numerator JOINS
--
		inv_join_array[i]:=E'\t'||'LEFT OUTER JOIN n'||i::VARCHAR||' ON ( '||
			E'\t'||'/* '||c4_rec.numer_tab||' - '||c4_rec.description||' */'||E'\n'||
			E'\t'||E'\t'||'    d.area_id'||E'\t'||E'\t'||' = n'||i::VARCHAR||'.area_id'||E'\n'||
			E'\t'||E'\t'||'AND d.year'||E'\t'||E'\t'||' = n'||i::VARCHAR||'.year'||E'\n'||
--
-- [Add conversion support for differing age/sex/group names; convert to AGE_SEX_GROUP]
--
			E'\t'||E'\t'||'AND d.'||LOWER(c8_rec.age_sex_group_field_name)||E'\t'||' = n'||i::VARCHAR||'.n_age_sex_group)';
				/* List of numerator joins (for use in FROM clause) */
		sql_stmt:=sql_stmt||E'\t'||'SELECT s.area_id'||E'\t'||E'\t'||'/* Study or comparision resolution */,'||E'\n';
		sql_stmt:=sql_stmt||E'\t'||'       c.year,'||E'\n';
--
-- [Add support for differing age/sex/group names; convert to AGE_SEX_GROUP]
--
		sql_stmt:=sql_stmt||E'\t'||'       c.'||LOWER(c4_rec.age_sex_group_field_name)||' AS n_age_sex_group,'||E'\n';
--
-- Individual investigations [add age group/sex/year filters]
-- 
		FOR c5_rec IN c5insext(study_id, c4_rec.numer_tab) LOOP
			j:=j+1;
			inv_array[j]:='       COALESCE('||
				'n'||i::VARCHAR||'.inv_'||c5_rec.inv_id::VARCHAR||'_'||LOWER(c5_rec.inv_name)||
				', 0) AS inv_'||c5_rec.inv_id::VARCHAR||'_'||LOWER(c5_rec.inv_name); 
				/* List of investigations (for use in final SELECT) */
			IF j > 1 THEN
				sql_stmt:=sql_stmt||','||E'\n';
			END IF;
			sql_stmt:=sql_stmt||E'\t'||'       SUM(CASE '||E'\t'||E'\t'||'/* Numerators - can overlap */'||E'\n';
			sql_stmt:=sql_stmt||E'\t'||E'\t'||E'\t'||'WHEN (('||E'\t'||'/* Investigation '||j::VARCHAR||' ICD filters */'||E'\n';
--
-- Add conditions
--
			k:=0;
			FOR c6_rec IN c6insext(study_id, c5_rec.inv_id) LOOP
				k:=k+1;
				IF k = 1 THEN
					sql_stmt:=sql_stmt||E'\t'||E'\t'||E'\t'||E'\t'||'    '||c6_rec.condition||' /* Filter '||k::VARCHAR||' */';
				ELSE
					sql_stmt:=sql_stmt||E'\n'||E'\t'||E'\t'||E'\t'||E'\t'||' OR '||c6_rec.condition||' /* Filter '||k::VARCHAR||' */';
				END IF;
			END LOOP;
			sql_stmt:=sql_stmt||') /* '||k::VARCHAR||' lines of conditions: study: '||
				study_id::VARCHAR||', inv: '||c5_rec.inv_id::VARCHAR||' */'||E'\n';
			sql_stmt:=sql_stmt||E'\t'||E'\t'||E'\t'||'AND (1=1'||E'\n';
--
-- Processing years filter
--
/*			IF year_start = year_stop THEN
				sql_stmt:=sql_stmt||E'\t'||E'\t'||E'\t'||'   AND (c.year = $2'||E'\t'||'-* Denominator (INSERT) year filter *-'||E'\n';
			ELSE
				sql_stmt:=sql_stmt||E'\t'||E'\t'||E'\t'||'   AND (c.year BETWEEN $2 AND $3'||E'\t'||'-* Denominator (INSERT) year filter *-'||E'\n';
			END IF; */
--
-- Investigation filters: year, age group, genders
--
			IF c5_rec.year_start = c5_rec.year_stop THEN
				sql_stmt:=sql_stmt||E'\t'||E'\t'||E'\t'||'   AND  c.year = '||c5_rec.year_start::VARCHAR||E'\n';
			ELSIF c4_rec.year_start = c5_rec.year_start AND c4_rec.year_stop = c5_rec.year_stop THEN
				sql_stmt:=sql_stmt||E'\t'||E'\t'||E'\t'||E'\t'||'        /* No year filter required for investigation '||j::VARCHAR||' */'||E'\n';
			ELSE
				sql_stmt:=sql_stmt||E'\t'||E'\t'||E'\t'||'   AND  c.year BETWEEN '||c5_rec.year_start::VARCHAR||
					' AND '||c5_rec.year_stop::VARCHAR||E'\n';
			END IF;
			IF c5_rec.genders = 3 THEN
				sql_stmt:=sql_stmt||E'\t'||E'\t'||E'\t'||E'\t'||'        /* No genders filter required for investigation '||j::VARCHAR||' */'||E'\n';
			ELSE
				sql_stmt:=sql_stmt||E'\t'||E'\t'||E'\t'||'   AND  TRUNC(c.'||LOWER(c4_rec.age_sex_group_field_name)||'/100) = '||c5_rec.genders::VARCHAR||E'\n';
			END IF;
			IF c8_rec.min_age_group = c5_rec.min_age_group AND c8_rec.max_age_group = c5_rec.max_age_group THEN
				sql_stmt:=sql_stmt||E'\t'||E'\t'||E'\t'||E'\t'||'        /* No age group filter required for investigation '||j::VARCHAR||' */)'||E'\n';
			ELSE 
				sql_stmt:=sql_stmt||E'\t'||E'\t'||E'\t'||'   AND  MOD(c.'||LOWER(c4_rec.age_sex_group_field_name)||', 100) BETWEEN '||
					c5_rec.min_age_group::VARCHAR||' AND '||c5_rec.max_age_group::VARCHAR||
					' /* Investigation '||j::VARCHAR||' year, age group filter */)'||E'\n';
			END IF;
--
			IF c4_rec.total_field IS NULL THEN /* Handle total fields */
				sql_stmt:=sql_stmt||E'\t'||E'\t'||E'\t'||') THEN 1'||E'\n';
			ELSE
				sql_stmt:=sql_stmt||E'\t'||E'\t'||E'\t'||') THEN '||LOWER(c4_rec.total_field)||E'\n';
			END IF;
			sql_stmt:=sql_stmt||E'\t'||E'\t'||E'\t'||'ELSE 0'||E'\n';
			sql_stmt:=sql_stmt||E'\t'||'       END) inv_'||c5_rec.inv_id::VARCHAR||'_'||LOWER(c5_rec.inv_name)||
				E'\t'||'/* Investigation '||j::VARCHAR||' - '||c5_rec.inv_description||' */ ';
		END LOOP;
--
-- Check at least one investigation
--
		IF j = 0 THEN
			PERFORM rif40_log_pkg.rif40_error(-90642, 'rif40_create_insert_statement', 
				'Study ID % no investigations created: distinct numerator: %',
				study_id::VARCHAR		/* Study ID */,		
				c4_rec.numer_tab::VARCHAR 	/* Distinct numerators */);
		END IF;
		sql_stmt:=sql_stmt||E'\n';

--
-- From clause
--
		sql_stmt:=sql_stmt||E'\t'||'  FROM '||LOWER(c4_rec.numer_tab)||' c, '||E'\t'||'/* '||c4_rec.description||' */'||E'\n';
		sql_stmt:=sql_stmt||E'\t'||'       '||areas_table||' s '||E'\t'||'/* Study or comparision area to be extracted */'||E'\n';
		IF study_or_comparison = 'C' THEN
			sql_stmt:=sql_stmt||E'\t'||' WHERE c.'||LOWER(c1_rec.comparison_geolevel_name)||' = s.area_id '||E'\t'||'/* Comparison selection */'||E'\n';
		ELSE
			sql_stmt:=sql_stmt||E'\t'||' WHERE c.'||LOWER(c1_rec.study_geolevel_name)||' = s.area_id '||E'\t'||'/* Study selection */'||E'\n';
		END IF;
--
-- [Add correct age_sex_group limits]
--
		IF c8_rec.min_age_group = c1_rec.min_age_group AND c8_rec.max_age_group = c1_rec.max_age_group THEN
			sql_stmt:=sql_stmt||E'\t'||'       /* No age group filter required for denominator */'||E'\n';
		ELSE 
			sql_stmt:=sql_stmt||E'\t'||'   AND MOD(c.'||LOWER(c8_rec.age_sex_group_field_name)||', 100) BETWEEN '||
				c1_rec.min_age_group::VARCHAR||' AND '||c1_rec.max_age_group::VARCHAR||
				' /* All valid age groups for denominator */'||E'\n';
		END IF;
		sql_stmt:=sql_stmt||E'\t'||'   AND s.study_id = $1'||E'\t'||E'\t'||'/* Current study ID */'||E'\n';
--
-- Processing years filter
--
		IF year_start = year_stop THEN
			sql_stmt:=sql_stmt||E'\t'||'   AND c.year = $2'||E'\t'||E'\t'||'/* Denominator (INSERT) year filter */'||E'\n';
		ELSE
			sql_stmt:=sql_stmt||E'\t'||'   AND c.year BETWEEN $2 AND $3'||E'\t'||'/* Denominator (INSERT) year filter */'||E'\n';
		END IF;
--
-- Group by clause
-- [Add support for differing age/sex/group names]
--
		sql_stmt:=sql_stmt||E'\t'||' GROUP BY c.year, s.area_id, c.'||LOWER(c4_rec.age_sex_group_field_name)||E'\n';

--
-- Close WITH clause (common table expression)
-- 
		sql_stmt:=sql_stmt||') /* '||c4_rec.numer_tab||' - '||c4_rec.description||' */'||E'\n';
--
	END LOOP;
--
-- Denominator CTE
--
	sql_stmt:=sql_stmt||', d AS ('||E'\n';
	IF study_or_comparison = 'C' THEN
		sql_stmt:=sql_stmt||E'\t'||'SELECT d1.year, s.area_id, NULL::INTEGER AS band_id, d1.'||LOWER(c8_rec.age_sex_group_field_name)||
			', SUM(COALESCE('||LOWER(coalesce(c8_rec.total_field, 'total'))||', 0)) AS total_pop'||E'\n';
	ELSE
		sql_stmt:=sql_stmt||E'\t'||'SELECT d1.year, s.area_id, s.band_id, d1.'||LOWER(c8_rec.age_sex_group_field_name)||
			', SUM(COALESCE('||LOWER(coalesce(c8_rec.total_field, 'total'))||', 0)) AS total_pop'||E'\n';
	END IF;
	sql_stmt:=sql_stmt||E'\t'||'  FROM '||LOWER(c1_rec.denom_tab)||' d1, '||areas_table||' s'||E'\t'||'/* Study or comparison area to be extracted */'||E'\n';
	sql_stmt:=sql_stmt||E'\t'||' WHERE d1.year = $2'||E'\t'||E'\t'||'/* Denominator (INSERT) year filter */'||E'\n';
	IF study_or_comparison = 'C' THEN
		sql_stmt:=sql_stmt||E'\t'||'   AND s.area_id  = d1.'||LOWER(c1_rec.comparison_geolevel_name)||E'\t'||'/* Comparison geolevel join */'||E'\n';
	ELSE
		sql_stmt:=sql_stmt||E'\t'||'   AND s.area_id  = d1.'||LOWER(c1_rec.study_geolevel_name)||E'\t'||'/* Study geolevel join */'||E'\n';
	END IF;
	sql_stmt:=sql_stmt||E'\t'||'   AND s.area_id  IS NOT NULL'||E'\t'||'/* Exclude NULL geolevel */'||E'\n';
	sql_stmt:=sql_stmt||E'\t'||'   AND s.study_id = $1'||E'\t'||E'\t'||'/* Current study ID */'||E'\n';
--
-- [Add correct age_sex_group limits]
--
	IF c8_rec.min_age_group = c1_rec.min_age_group AND c8_rec.max_age_group = c1_rec.max_age_group THEN
		sql_stmt:=sql_stmt||E'\t'||'       /* No age group filter required for denominator */'||E'\n';
	ELSE 
		sql_stmt:=sql_stmt||E'\t'||'   AND MOD(c.'||LOWER(c8_rec.age_sex_group_field_name)||', 100) BETWEEN '||
			c1_rec.min_age_group::VARCHAR||' AND '||c1_rec.max_age_group::VARCHAR||
			' /* All valid age groups for denominator */'||E'\n';
	END IF;
--
-- [Add gender filter]
--
	IF study_or_comparison = 'C' THEN
		sql_stmt:=sql_stmt||E'\t'||' GROUP BY d1.year, s.area_id, '||LOWER(c8_rec.age_sex_group_field_name)||E'\n';
	ELSE
		sql_stmt:=sql_stmt||E'\t'||' GROUP BY d1.year, s.area_id, s.band_id, d1.'||LOWER(c8_rec.age_sex_group_field_name)||E'\n';
	END IF;
	sql_stmt:=sql_stmt||')'||E'\n';
--
-- Main SQL statement
--
	sql_stmt:=sql_stmt||'SELECT d.year,'||E'\n';
	sql_stmt:=sql_stmt||'       '''||study_or_comparison||''' AS study_or_comparison,'||E'\n';
	sql_stmt:=sql_stmt||'       $1 AS study_id,'||E'\n';
	sql_stmt:=sql_stmt||'       d.area_id,'||E'\n';
	sql_stmt:=sql_stmt||'       d.band_id,'||E'\n';
--
-- [Add support for differing age/sex/group names]
--
	sql_stmt:=sql_stmt||'       TRUNC(d.'||LOWER(c8_rec.age_sex_group_field_name)||'/100) AS sex,'||E'\n';
	sql_stmt:=sql_stmt||'       MOD(d.'||LOWER(c8_rec.age_sex_group_field_name)||', 100) AS age_group,'||E'\n';
--
-- Add covariate names (Assumes 1 covariate table)
--
	k:=0;
	FOR c7_rec IN c7insext(study_id) LOOP
		k:=k+1;
		IF study_or_comparison = 'C' THEN
			sql_stmt:=sql_stmt||'       NULL::INTEGER AS '||LOWER(c7_rec.covariate_name)||','||E'\n';
		ELSE
			sql_stmt:=sql_stmt||'       c.'||LOWER(c7_rec.covariate_name)||','||E'\n';
		END IF;
	END LOOP;
--
-- Add investigations 
--
	sql_stmt:=sql_stmt||array_to_string(inv_array, ','||E'\n')||', '||E'\n';
--
-- Add denominator
--
	sql_stmt:=sql_stmt||'       d.total_pop'||E'\n';
--
-- FROM clause
--
	sql_stmt:=sql_stmt||'  FROM d'||E'\t'||E'\t'||E'\t'||'/* Denominator - '||c8_rec.description||' */'||E'\n';
	sql_stmt:=sql_stmt||array_to_string(inv_join_array, E'\n')||E'\n';
	IF study_or_comparison = 'S' THEN
		sql_stmt:=sql_stmt||E'\t'||'LEFT OUTER JOIN '||LOWER(c1_rec.covariate_table)||' c ON ('||E'\t'||'/* Covariates */'||E'\n';
		sql_stmt:=sql_stmt||E'\t'||E'\t'||'    d.area_id = c.'||LOWER(c1_rec.study_geolevel_name)||E'\n';
		sql_stmt:=sql_stmt||E'\t'||E'\t'||'AND d.year    = c.year)'||E'\n';
	END IF;
--
-- ORDER BY caluse
--
	sql_stmt:=sql_stmt||' ORDER BY 1, 2, 3, 4, 5, 6, 7';
--
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_create_insert_statement', 
		'SQL> %;', sql_stmt::VARCHAR);
--
	RETURN sql_stmt;
--
END;
$func$
LANGUAGE 'plpgsql';

GRANT EXECUTE ON FUNCTION rif40_sm_pkg.rif40_create_insert_statement(INTEGER, VARCHAR, INTEGER, INTEGER) TO rif40;
COMMENT ON FUNCTION rif40_sm_pkg.rif40_create_insert_statement(INTEGER, VARCHAR, INTEGER, INTEGER) IS 'A';

CREATE OR REPLACE FUNCTION rif40_sm_pkg.rif40_compute_results(study_id INTEGER)
RETURNS BOOLEAN
SECURITY INVOKER
AS $func$
DECLARE
/*
Function:	rif40_compute_results()
Parameter:	Study ID
Returns:	Success or failure [BOOLEAN]
		Note this is to allow SQL executed by study extraction/results created to be logged (Postgres does not allow autonomous transactions)
		Verification and error checking raises EXCEPTIONS in the usual way; and will cause the SQL log to be lost
Description:	Compute results from extract table. Create map table

INSERT INTO rif40_results (study_id, inv_id, band_id, genders, direct_standardisation, adjusted, observed)
WITH a AS (
	SELECT study_id, band_id, sex,
	       SUM(COALESCE(inv_1, 0)) AS inv_1_observed
	  FROM s217_extract
	 WHERE study_or_comparison = 'S'
	 GROUP BY study_id, band_id, sex
)
SELECT study_id, 217 AS inv_id, band_id, 1 AS genders, 0 AS direct_standardisation, 0 AS adjusted, inv_1_observed AS observed
  FROM a
 WHERE sex = 1
UNION
SELECT study_id, 217 AS inv_id, band_id, 2 AS genders, 0 AS direct_standardisation, 0 AS adjusted, inv_1_observed AS observed
  FROM a
 WHERE sex = 2
UNION
SELECT study_id, 217 AS inv_id, band_id, 3 AS genders, 0 AS direct_standardisation, 0 AS adjusted, SUM(COALESCE(inv_1_observed, 0)) AS observed
  FROM a
 GROUP BY study_id, band_id
 ORDER BY 1, 2, 3, 4, 5, 6;

 */
	c1comp CURSOR(l_study_id INTEGER) FOR
		SELECT * 
		  FROM rif40_studies a
		 WHERE a.study_id = l_study_id;
	c1acomp CURSOR(l_study_id INTEGER) FOR
		SELECT * 
		  FROM rif40_studies a
		 WHERE a.study_id = l_study_id;
	c2comp CURSOR(l_study_id INTEGER) FOR
		SELECT * 
		  FROM rif40_investigations a
		 WHERE a.study_id = l_study_id
		 ORDER BY inv_id;
	c3comp CURSOR(l_study_id INTEGER) FOR
		SELECT *
		  FROM rif40_study_shares a
		 WHERE l_study_id = a.study_id;
	c4comp CURSOR FOR
		SELECT col_description(a.oid, c.ordinal_position) AS description, c.column_name
		  FROM information_schema.columns c, pg_class a
			LEFT OUTER JOIN pg_namespace n ON (n.oid = a.relnamespace)			
		 WHERE a.relowner IN (SELECT oid FROM pg_roles WHERE rolname = 'rif40')
		   AND a.relname = 't_rif40_results'
		   AND a.relname = c.table_name
		 ORDER BY 1;
--
	c1_rec RECORD;
	c1a_rec RECORD;
	c2_rec RECORD;
	c3_rec RECORD;
	c4_rec RECORD;
--
	sql_stmt	VARCHAR;
	ddl_stmts	VARCHAR[];
	i		INTEGER:=0;
	t_ddl		INTEGER:=1;
	inv_array	INTEGER[];
	inv		INTEGER;
BEGIN
	OPEN c1comp(study_id);
	FETCH c1comp INTO c1_rec;
	OPEN c1acomp(study_id);
	FETCH c1acomp INTO c1a_rec;
	IF NOT FOUND THEN
		CLOSE c1comp;
		CLOSE c1acomp;
		PERFORM rif40_log_pkg.rif40_error(-90641, 'rif40_compute_results', 
			'Study ID % not found',
			study_id::VARCHAR		/* Study ID */);
	END IF;
	CLOSE c1comp;
	CLOSE c1acomp;
--
-- Calculate observed
--
-- [No genders support]
--
	sql_stmt:='INSERT INTO rif40_results (study_id, inv_id, band_id, genders, direct_standardisation, adjusted, observed)'||E'\n';
	sql_stmt:=sql_stmt||E'\t'||'WITH a AS ('||E'\n';
	sql_stmt:=sql_stmt||E'\t'||'SELECT study_id, band_id, sex,';
	FOR c2_rec IN c2comp(study_id) LOOP
		i:=i+1;
		inv_array[i]:=c2_rec.inv_id;
		IF i = 1 THEN
		       	sql_stmt:=sql_stmt||E'\n'||E'\t'||'       SUM(COALESCE(inv_'||i::VARCHAR||', 0)) AS inv_'||i::VARCHAR||'_observed'||
				E'\t'||'/* '||c2_rec.inv_id||' -  '||c2_rec.numer_tab||' - '||c2_rec.inv_description||' */';
		ELSE
		       	sql_stmt:=sql_stmt||','||E'\n'||E'\t'||'       SUM(COALESCE(inv_'||i::VARCHAR||', 0)) AS inv_'||i::VARCHAR||'_observed'||
				E'\t'||'/* '||c2_rec.inv_id||' -  '||c2_rec.numer_tab||' - '||c2_rec.inv_description||' */';
		END IF;
	END LOOP;
	sql_stmt:=sql_stmt||E'\t'||'	  FROM '||LOWER(c1_rec.extract_table)||E'\n';
	sql_stmt:=sql_stmt||E'\t'||'	 WHERE study_or_comparison = ''S'''||E'\n';
	sql_stmt:=sql_stmt||E'\t'||'	 GROUP BY study_id, band_id, sex'||E'\n';
	sql_stmt:=sql_stmt||')'||E'\n';
	FOREACH inv IN ARRAY inv_array LOOP
		IF i > 1 THEN
			sql_stmt:=sql_stmt||'UNION'||E'\n';
		END IF;
		sql_stmt:=sql_stmt||'SELECT study_id, '||inv::VARCHAR||' AS inv_id, band_id, 1 AS genders, 0 /* Indirect */ AS direct_standardisation, 0 /* Unadjusted */ AS adjusted, inv_'||
			i::VARCHAR||'_observed AS observed'||E'\n';
		sql_stmt:=sql_stmt||'  FROM a'||E'\n';
		sql_stmt:=sql_stmt||' WHERE sex = 1'||E'\n';
		sql_stmt:=sql_stmt||'UNION'||E'\n';
		sql_stmt:=sql_stmt||'SELECT study_id, '||inv::VARCHAR||' AS inv_id, band_id, 2 AS genders, 0 /* Indirect */ AS direct_standardisation, 0 /* Unadjusted */ AS adjusted, inv_'||
			i::VARCHAR||'_observed AS observed'||E'\n';
		sql_stmt:=sql_stmt||'  FROM a'||E'\n';
		sql_stmt:=sql_stmt||' WHERE sex = 2'||E'\n';
		sql_stmt:=sql_stmt||'UNION'||E'\n';
		sql_stmt:=sql_stmt||'SELECT study_id, '||inv::VARCHAR||
			' AS inv_id, band_id, 3 /* both */ AS genders, 0 /* Indirect */ AS direct_standardisation, 0 /* Unadjusted */ AS adjusted, SUM(COALESCE(inv_'||
			i::VARCHAR||'_observed, 0)) AS observed'||E'\n';
		sql_stmt:=sql_stmt||'  FROM a'||E'\n';
		sql_stmt:=sql_stmt||' GROUP BY study_id, band_id'||E'\n';
	END LOOP;
	sql_stmt:=sql_stmt||' ORDER BY 1, 2, 3, 4, 5, 6';
--
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_compute_results', 	
		'SQL> %;',
		sql_stmt::VARCHAR);
	IF rif40_sm_pkg.rif40_execute_insert_statement(study_id, sql_stmt, 'rif40_results observed INSERT'::VARCHAR) = FALSE THEN 
		RETURN FALSE;
	END IF;
--
-- Create map table [DOES NOT CREATE ANY ROWS]
--
-- [CONTAINS NO GEOMETRY]
--
	ddl_stmts[t_ddl]:='CREATE TABLE rif_studies.'||LOWER(c1_rec.map_table)||' AS SELECT * FROM rif40_results WHERE study_id = '||
		study_id::VARCHAR||' /* Current study ID */ LIMIT 1'::VARCHAR; 
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_compute_results', 	
		'SQL> %;',
		ddl_stmts[t_ddl]::VARCHAR);
--
-- Truncate it anyway to make sure
--
	t_ddl:=t_ddl+1;	
	ddl_stmts[t_ddl]:='TRUNCATE TABLE rif_studies.'||LOWER(c1_rec.map_table);
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_compute_results', 	
		'SQL> %;',
		ddl_stmts[t_ddl]::VARCHAR);
--
-- Grant to study owner and all grantees in rif40_study_shares if extract_permitted=1 
--
	IF c1_rec.extract_permitted = 1 THEN
		sql_stmt:='GRANT SELECT,INSERT,TRUNCATE ON rif_studies.'||LOWER(c1_rec.map_table)||' TO '||USER;
		t_ddl:=t_ddl+1;	
		ddl_stmts[t_ddl]:=sql_stmt;
		FOR c3_rec IN c3comp(study_id) LOOP
			sql_stmt:='GRANT SELECT,INSERT ON rif_studies.'||LOWER(c1_rec.map_table)||' TO '||c3_rec.grantee_username;
			t_ddl:=t_ddl+1;	
			ddl_stmts[t_ddl]:=sql_stmt;
		END LOOP;
	END IF;
--
-- Comment
--
	sql_stmt:='COMMENT ON TABLE rif_studies.'||LOWER(c1_rec.map_table)||' IS ''Study :'||study_id::VARCHAR||' extract table''';
	t_ddl:=t_ddl+1;	
	ddl_stmts[t_ddl]:=sql_stmt;
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_compute_results', 	
		'SQL> %;',
		ddl_stmts[t_ddl]::VARCHAR);
	FOR c4_rec IN c4comp LOOP
		sql_stmt:='COMMENT ON COLUMN rif_studies.'||LOWER(c1_rec.map_table)||'.'||c4_rec.column_name||
			' IS '''||c4_rec.description||'''';
		t_ddl:=t_ddl+1;	
		ddl_stmts[t_ddl]:=sql_stmt;
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_compute_results', 	
			'SQL> %;',
			ddl_stmts[t_ddl]::VARCHAR);
	END LOOP;

--
-- Execute DDL code as rif40
--
	IF rif40_sm_pkg.rif40_study_ddl_definer(c1_rec.study_id, c1_rec.username, c1a_rec.audsid, ddl_stmts) = FALSE THEN
		RETURN FALSE;
	END IF;
--
-- Now do real insert
--
	sql_stmt:='INSERT INTO rif_studies.'||LOWER(c1_rec.map_table)||' SELECT * FROM rif40_results WHERE study_id = '||
		study_id::VARCHAR||' /* Current study ID */ ORDER BY 1, 2, 3, 4, 5, 6'::VARCHAR; 
	IF rif40_sm_pkg.rif40_execute_insert_statement(study_id, sql_stmt, 'Map table observed INSERT'::VARCHAR) = FALSE THEN 
		RETURN FALSE;
	END IF;
--
-- Index, analyze
--
	ddl_stmts:=NULL;
	t_ddl:=1;
	sql_stmt:='ALTER TABLE rif_studies.'||LOWER(c1_rec.map_table)||' ADD CONSTRAINT "'||LOWER(c1_rec.map_table)||'_pk" PRIMARY KEY (study_id, band_id, inv_id, genders, adjusted, direct_standardisation)';
	ddl_stmts[t_ddl]:=sql_stmt;
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_compute_results', 	
		'SQL> %;',
		sql_stmt::VARCHAR);
	t_ddl:=t_ddl+1;	
	sql_stmt:='ANALYZE rif_studies.'||LOWER(c1_rec.map_table);
	ddl_stmts[t_ddl]:=sql_stmt;
	PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_compute_results', 	
		'SQL> %;',
		sql_stmt::VARCHAR);
--
-- Execute DDL code as rif40
--
	IF rif40_sm_pkg.rif40_study_ddl_definer(c1_rec.study_id, c1_rec.username, c1a_rec.audsid, ddl_stmts) = FALSE THEN
		RETURN FALSE;
	END IF;

--
	PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_compute_results', 
		'[90645] Study ID % map table % created',
		study_id::VARCHAR,
		c1_rec.map_table::VARCHAR);
--
-- Next expected...
--
	PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_compute_results', 
		'[90645] Study ID % rif40_compute_results() not fully implemented',
		study_id::VARCHAR);
	RETURN TRUE;
--
END;
$func$
LANGUAGE 'plpgsql';

GRANT EXECUTE ON FUNCTION rif40_sm_pkg.rif40_compute_results(INTEGER) TO rif40;
COMMENT ON FUNCTION rif40_sm_pkg.rif40_compute_results(INTEGER) IS 'A';

CREATE OR REPLACE FUNCTION rif40_sm_pkg.cleanup_orphaned_extract_and_map_tables(truncate BOOLEAN DEFAULT FALSE)
RETURNS void
SECURITY DEFINER 
AS $func$
DECLARE
/*
Function:	cleanup_orphaned_extract_and_map_tables()
Parameter:	Truncate tables (default FALSE)
Returns:	Nothing
Description:	Cleanup and map and extract tables not referenced by a study (runs as rif40)
		Do not drop table if it contains any data unless truncate is true
		Orphans are defined as tables in rif_studies schema no longer referenced by studies

Note:		DO NOT PUT TABLES INTO THE rif_studies SCHEMA MANUALLY - they will be deleted if empty
 */
	c1_clorpf CURSOR FOR
		SELECT table_name /* tables in rif_studies schema no longer referenced by studies */
		  FROM information_schema.tables
		 WHERE table_schema = 'rif_studies'
		   AND table_type   = 'BASE TABLE'
		EXCEPT
		SELECT extract_table
		  FROM t_rif40_studies
		EXCEPT
		SELECT map_table
		  FROM t_rif40_studies
		 ORDER BY 1;
	c2_clorpf REFCURSOR;
	c1_rec RECORD;
	c2_rec RECORD;
--
	sql_stmt	VARCHAR;
	t_ddl		INTEGER:=0;
	ddl_stmts	VARCHAR[];
BEGIN
	FOR c1_rec IN c1_clorpf LOOP
		sql_stmt:='SELECT COUNT(study_id) AS total'||E'\n'||
			'  FROM ('||E'\n'||
			'SELECT study_id'||E'\n'||
			'  FROM rif_studies.'||c1_rec.table_name||E'\n'||
			' LIMIT 1) a';
		OPEN c2_clorpf FOR EXECUTE sql_stmt;
		FETCH c2_clorpf INTO c2_rec;
		IF NOT FOUND THEN
			CLOSE c2_clorpf;
			PERFORM rif40_log_pkg.rif40_error(-90661, 'cleanup_orphaned_extract_and_map_tables', 
				'[90661] Extract/map table % not found',
				c1_rec.table_name::VARCHAR);
		END IF;
		CLOSE c2_clorpf;
--
		IF c2_rec.total = 0 THEN
			PERFORM rif40_log_pkg.rif40_log('INFO', 'cleanup_orphaned_extract_and_map_tables', 
				'[90662] Extract/map table % does not contain data; will be cleaned up',
				c1_rec.table_name::VARCHAR);
			sql_stmt:='DROP TABLE rif_studies.'||c1_rec.table_name;
			t_ddl:=t_ddl+1;	
			ddl_stmts[t_ddl]:=sql_stmt;
		ELSIF truncate THEN
			PERFORM rif40_log_pkg.rif40_log('INFO', 'cleanup_orphaned_extract_and_map_tables', 
				'[90663] Extract/map table % contains data; truncate is TRUE; will be cleaned up',
				c1_rec.table_name::VARCHAR);
			sql_stmt:='TRUNCATE TABLE rif_studies.'||c1_rec.table_name;
			t_ddl:=t_ddl+1;	
			ddl_stmts[t_ddl]:=sql_stmt;
			sql_stmt:='DROP TABLE rif_studies.'||c1_rec.table_name;
			t_ddl:=t_ddl+1;	
			ddl_stmts[t_ddl]:=sql_stmt;
		ELSE
			PERFORM rif40_log_pkg.rif40_log('INFO', 'cleanup_orphaned_extract_and_map_tables', 
				'[90664] Extract/map table % contains data; will not be cleaned up',
				c1_rec.table_name::VARCHAR);
		END IF;
	END LOOP;
--
-- Now drop the tables
--
	IF t_ddl > 0 THEN
		PERFORM rif40_sql_pkg.rif40_ddl(ddl_stmts);
		PERFORM rif40_log_pkg.rif40_log('INFO', 'cleanup_orphaned_extract_and_map_tables', 
			'[90665] % orphaned extract/map table(s) cleaned up',
			t_ddl::VARCHAR);
	END IF;
END;
$func$
LANGUAGE 'plpgsql';

GRANT EXECUTE ON FUNCTION rif40_sm_pkg.cleanup_orphaned_extract_and_map_tables(BOOLEAN) TO rif40;
COMMENT ON FUNCTION rif40_sm_pkg.cleanup_orphaned_extract_and_map_tables(BOOLEAN) IS 'Function:	cleanup_orphaned_extract_and_map_tables()
Parameter:	Truncate tables (default FALSE)
Returns:	Nothing
Description:	Cleanup and map and extract tables not referenced by a study (runs as rif40)
		Do not drop table if it contains any data unless truncate is true
		Orphans are defined as tables in rif_studies schema no longer referenced by studies

Note:		DO NOT PUT TABLES INTO THE rif_studies SCHEMA MANUALLY - they will be deleted if empty';

CREATE OR REPLACE FUNCTION rif40_sm_pkg.rif40_delete_study(study_id INTEGER)
RETURNS void
SECURITY INVOKER
AS $func$
DECLARE
/*
Function:	rif40_delete_study()
Parameter:	Study ID
Returns:	Nothing
Description:	Delete study from tables:

RIF40_STUDY_SQL
RIF40_STUDY_SQL_LOG
RIF40_RESULTS
RIF40_CONTEXTUAL_STATS
RIF40_INV_CONDITIONS 
RIF40_INV_COVARIATES 
RIF40_INVESTIGATIONS 
RIF40_STUDY_AREAS 
RIF40_COMPARISON_AREAS 
RIF40_STUDY_SHARES
RIF40_STUDIES 

Truncate extract and map tables if defined

Call: cleanup_orphaned_extract_and_map_tables()

 */
	c1_delst	CURSOR(l_study_id INTEGER) FOR
		SELECT *
		  FROM rif40_studies a
		 WHERE l_study_id = a.study_id;
	c1_rec		RECORD;
--
	sql_stmt	VARCHAR[];
	rows		INTEGER;
	schema 		VARCHAR;
BEGIN
	OPEN c1_delst(study_id);
	FETCH c1_delst INTO c1_rec;
	IF NOT FOUND THEN
		CLOSE c1_delst;
		PERFORM rif40_log_pkg.rif40_error(-90601, 'rif40_delete_study', 
			'Study ID % not found',
			study_id::VARCHAR);
	END IF;
	CLOSE c1_delst;
--
	sql_stmt[1]:='DELETE FROM rif40_study_sql WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[2]:='DELETE FROM rif40_study_sql_log WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[3]:='DELETE FROM rif40_results WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[4]:='DELETE FROM rif40_contextual_stats WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[5]:='DELETE FROM rif40_inv_conditions WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[6]:='DELETE FROM rif40_inv_covariates WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[7]:='DELETE FROM rif40_investigations WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[8]:='DELETE FROM rif40_study_areas WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[9]:='DELETE FROM rif40_comparison_areas WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[10]:='DELETE FROM rif40_study_shares WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[11]:='DELETE FROM rif40_studies WHERE study_id = '||study_id::VARCHAR;
--
-- Drop extract and map tables if defined
-- (Cannot be dropped yets - requires definer function to cleanup all zero sized orphaned extract tables)
--
	schema:=rif40_sql_pkg.rif40_object_resolve(c1_rec.extract_table::VARCHAR);
	IF schema IS NOT NULL AND has_table_privilege(USER, schema||'.'||c1_rec.extract_table, 'truncate') THEN
		sql_stmt[array_length(sql_stmt, 1)+1]:='TRUNCATE TABLE '||schema||'.'||LOWER(c1_rec.extract_table);
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_delete_study', 
			'[90940] Will truncate extract table % for study %',
			c1_rec.extract_table::VARCHAR	/* Extract table */,
			study_id::VARCHAR		/* Study ID */);
--		sql_stmt[array_length(sql_stmt, 1)+1]:='DROP TABLE '||schema||'.'||LOWER(c1_rec.extract_table);
	END IF;
	schema:=rif40_sql_pkg.rif40_object_resolve(c1_rec.map_table::VARCHAR);
	IF schema IS NOT NULL AND has_table_privilege(USER, schema||'.'||c1_rec.map_table, 'truncate') THEN
		sql_stmt[array_length(sql_stmt, 1)+1]:='TRUNCATE TABLE '||schema||'.'||LOWER(c1_rec.map_table);
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_delete_study', 
			'[90941] Will truncate map table % for study %',
			c1_rec.map_table::VARCHAR	/* Map table */,
			study_id::VARCHAR		/* Study ID */);
--		sql_stmt[array_length(sql_stmt, 1)+1]:='DROP TABLE '||schema||'.'||LOWER(c1_rec.map_table);
	END IF;
--
	rows:=rif40_sql_pkg.rif40_ddl(sql_stmt);

	PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_delete_study', 
		'[90942] Deleted study: %; % rows deleted',
		 study_id::VARCHAR		/* Study ID */,
		 rows::VARCHAR 			/* Rows deleted */);
--
-- Cleanup and map and extract tables not referenced by a study (runs as rif40)
--
	PERFORM rif40_sm_pkg.cleanup_orphaned_extract_and_map_tables();
--
	RETURN;
END;
$func$
LANGUAGE 'plpgsql';

GRANT EXECUTE ON FUNCTION rif40_sm_pkg.rif40_delete_study(INTEGER) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_sm_pkg.rif40_delete_study(INTEGER) TO rif_user;
GRANT EXECUTE ON FUNCTION rif40_sm_pkg.rif40_delete_study(INTEGER) TO rif40;
COMMENT ON FUNCTION rif40_sm_pkg.rif40_delete_study(INTEGER) IS 'Function:	rif40_delete_study()
Parameter:	Study ID
Returns:	Nothing
Description:	Delete study from tables:

RIF40_STUDY_SQL
RIF40_STUDY_SQL_LOG
RIF40_RESULTS
RIF40_CONTEXTUAL_STATS
RIF40_INV_CONDITIONS 
RIF40_INV_COVARIATES 
RIF40_INVESTIGATIONS 
RIF40_STUDY_AREAS 
RIF40_COMPARISON_AREAS 
RIF40_STUDY_SHARES
RIF40_STUDIES

Truncate extract and map tables if defined

Call: cleanup_orphaned_extract_and_map_tables()
';

CREATE OR REPLACE FUNCTION rif40_sm_pkg.rif40_reset_study(study_id INTEGER)
RETURNS void
SECURITY INVOKER
AS $func$
DECLARE
/*
Function:	rif40_reset_study()
Parameter:	Study ID
Returns:	Nothing
Description:	Reset study to the 'C' (created state) so it can be re-run

Delete study from tables:

RIF40_STUDY_SQL
RIF40_STUDY_SQL_LOG
RIF40_RESULTS
RIF40_CONTEXTUAL_STATS

Truncate extract and map tables if defined

Update study state to C

Call: cleanup_orphaned_extract_and_map_tables()
 */
	c1_reset CURSOR(l_study_id INTEGER) FOR
		SELECT *
		  FROM rif40_studies a
		 WHERE l_study_id = a.study_id;
	c1_rec		RECORD;
--
	sql_stmt	VARCHAR[];
	rows		INTEGER;
	schema 		VARCHAR;
--
	study_upd_count INTEGER;
	inv_upd_count INTEGER;
BEGIN
	OPEN c1_reset(study_id);
	FETCH c1_reset INTO c1_rec;
	IF NOT FOUND THEN
		CLOSE c1_reset;
		PERFORM rif40_log_pkg.rif40_error(-90945, 'rif40_reset_study', 
			'Study ID % not found',
			study_id::VARCHAR);
	END IF;
	CLOSE c1_reset;
--
	sql_stmt[1]:='DELETE FROM rif40_study_sql WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[2]:='DELETE FROM rif40_study_sql_log WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[3]:='DELETE FROM rif40_results WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[4]:='DELETE FROM rif40_contextual_stats WHERE study_id = '||study_id::VARCHAR;
--
-- Drop extract and map tables if defined
-- (Cannot be dropped yets - requires definer function to cleanup all zero sized orphaned extract tables)
--
	schema:=rif40_sql_pkg.rif40_object_resolve(c1_rec.extract_table::VARCHAR);
	IF schema IS NOT NULL AND has_table_privilege(USER, schema||'.'||c1_rec.extract_table, 'truncate') THEN
		sql_stmt[array_length(sql_stmt, 1)+1]:='TRUNCATE TABLE '||schema||'.'||LOWER(c1_rec.extract_table);
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_reset_study', 
			'[90946] Will truncate extract table % for study %',
			c1_rec.extract_table::VARCHAR	/* Extract table */,
			study_id::VARCHAR		/* Study ID */);
--		sql_stmt[array_length(sql_stmt, 1)+1]:='DROP TABLE '||schema||'.'||LOWER(c1_rec.extract_table);
	END IF;
	schema:=rif40_sql_pkg.rif40_object_resolve(c1_rec.map_table::VARCHAR);
	IF schema IS NOT NULL AND has_table_privilege(USER, schema||'.'||c1_rec.map_table, 'truncate') THEN
		sql_stmt[array_length(sql_stmt, 1)+1]:='TRUNCATE TABLE '||schema||'.'||LOWER(c1_rec.map_table);
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_reset_study', 
			'[90947] Will truncate map table % for study %',
			c1_rec.map_table::VARCHAR	/* Map table */,
			study_id::VARCHAR		/* Study ID */);
--		sql_stmt[array_length(sql_stmt, 1)+1]:='DROP TABLE '||schema||'.'||LOWER(c1_rec.map_table);
	END IF;
--
	rows:=rif40_sql_pkg.rif40_ddl(sql_stmt);
--
-- Update study state
--
	EXECUTE 'UPDATE rif40_investigations a
	   SET investigation_state = ''C''
 	 WHERE a.study_id = $1' USING study_id;
	GET DIAGNOSTICS study_upd_count = ROW_COUNT;
	EXECUTE 'UPDATE rif40_studies a
	   SET study_state = ''C''
 	 WHERE a.study_id = $1' USING study_id;
	GET DIAGNOSTICS inv_upd_count = ROW_COUNT;
--
	PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_reset_study', 
		'[90948] Reset study: %; % rows deleted; % study, % investigation(s) updated',
		study_id::VARCHAR		/* Study ID */,
		rows::VARCHAR 			/* Rows deleted */,
		study_upd_count::VARCHAR 	/* Study update count */,
		inv_upd_count::VARCHAR 		/* Study update count */);
--
-- Cleanup and map and extract tables not referenced by a study (runs as rif40)
--
	PERFORM rif40_sm_pkg.cleanup_orphaned_extract_and_map_tables();
--
	RETURN;
--
END;
$func$
LANGUAGE 'plpgsql';

GRANT EXECUTE ON FUNCTION rif40_sm_pkg.rif40_reset_study(INTEGER) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_sm_pkg.rif40_reset_study(INTEGER) TO rif40;

COMMENT ON FUNCTION rif40_sm_pkg.rif40_reset_study(INTEGER) IS 'Function:	rif40_reset_study()
Parameter:	Study ID
Returns:	Nothing
Description:	Reset study to the ''C'' (created state) so it can be re-run

Delete study from tables:

RIF40_STUDY_SQL
RIF40_STUDY_SQL_LOG
RIF40_RESULTS
RIF40_CONTEXTUAL_STATS

Truncate extract and map tables if defined

Update study state to ''C''

Call: cleanup_orphaned_extract_and_map_tables()
';

CREATE OR REPLACE FUNCTION rif40_sm_pkg.rif40_clone_study(study_id INTEGER)
RETURNS INTEGER
SECURITY INVOKER
AS $func$
DECLARE
/*
Function:	rif40_clone_study()
Parameter:	Study ID
Returns:	New study ID
Description:	Clone study [testing purposes only] from tables:

RIF40_INV_CONDITIONS 
RIF40_INV_COVARIATES 
RIF40_INVESTIGATIONS 
RIF40_STUDY_AREAS 
RIF40_COMPARISON_AREAS 
RIF40_STUDY_SHARES
RIF40_STUDIES 

Does not clone:

RIF40_STUDY_SQL
RIF40_STUDY_SQL_LOG
RIF40_RESULTS
RIF40_CONTEXTUAL_STATS

 */
	sql_stmt	VARCHAR[];
	rows		INTEGER;
BEGIN
	sql_stmt[7]:='INSERT INTO rif40_inv_conditions(condition) SELECT condition FROM rif40_inv_conditions WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[6]:='INSERT INTO rif40_inv_covariates(geography, covariate_name, study_geolevel_name, min, max)
		 SELECT geography, covariate_name, study_geolevel_name, min, max FROM rif40_inv_covariates WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[5]:='INSERT INTO rif40_investigations(geography, inv_name, inv_description, genders, numer_tab, 
			year_start, year_stop, max_age_group, min_age_group) 
			SELECT geography, inv_name, inv_description, genders, numer_tab, 
			year_start, year_stop, max_age_group, min_age_group FROM rif40_investigations WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[4]:='INSERT INTO rif40_study_areas(area_id, band_id) SELECT area_id, band_id FROM rif40_study_areas WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[3]:='INSERT INTO rif40_comparison_areas(area_id) SELECT area_id FROM rif40_comparison_areas WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[2]:='INSERT INTO rif40_study_shares(grantee_username) SELECT grantee_username FROM rif40_study_shares WHERE study_id = '||study_id::VARCHAR;
	sql_stmt[1]:='INSERT INTO rif40_studies(geography, project, study_name, study_type,
 		comparison_geolevel_name, study_geolevel_name, denom_tab,            
 		year_start, year_stop, max_age_group, min_age_group, 
 		suppression_value, extract_permitted, transfer_permitted) SELECT geography, project, study_name, study_type,
 		comparison_geolevel_name, study_geolevel_name, denom_tab,            
 		year_start, year_stop, max_age_group, min_age_group, 
 		suppression_value, extract_permitted, transfer_permitted FROM rif40_studies WHERE study_id = '||study_id::VARCHAR;
--
	rows:=rif40_sql_pkg.rif40_ddl(sql_stmt);

	PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_clone_study', 
		'[90930] Cloned study: %; % rows inserted',
		 study_id::VARCHAR		/* Study ID */,
		 rows::VARCHAR 			/* Rows inserted */);
--
	RETURN currval('rif40_study_id_seq'::regclass);
END;
$func$
LANGUAGE 'plpgsql';

GRANT EXECUTE ON FUNCTION rif40_sm_pkg.rif40_clone_study(INTEGER) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_sm_pkg.rif40_clone_study(INTEGER) TO rif40;
COMMENT ON FUNCTION rif40_sm_pkg.rif40_clone_study(INTEGER) IS 'Function:	rif40_clone_study()
Parameter:	Study ID
Returns:	New study ID
Description:	Clone study [testing purposes only] from tables:

RIF40_INV_CONDITIONS 
RIF40_INV_COVARIATES 
RIF40_INVESTIGATIONS 
RIF40_STUDY_AREAS 
RIF40_COMPARISON_AREAS 
RIF40_STUDY_SHARES
RIF40_STUDIES 

Does not clone:

RIF40_STUDY_SQL
RIF40_STUDY_SQL_LOG
RIF40_RESULTS
RIF40_CONTEXTUAL_STATS
';

--DROP FUNCTION rif40_sm_pkg.rif40_create_disease_mapping_example(
--	VARCHAR,VARCHAR,VARCHAR,VARCHAR,VARCHAR,VARCHAR[],VARCHAR,VARCHAR,VARCHAR,VARCHAR,INTEGER,INTEGER,VARCHAR[],VARCHAR[]);

CREATE OR REPLACE FUNCTION rif40_sm_pkg.rif40_create_disease_mapping_example(
	geography		VARCHAR,
	geolevel_view		VARCHAR,
	geolevel_area		VARCHAR,
	geolevel_map		VARCHAR,
	geolevel_select		VARCHAR,
	geolevel_selection	VARCHAR[],
	project			VARCHAR,
	study_name		VARCHAR,
	denom_tab		VARCHAR,
	numer_tab		VARCHAR,
	year_start		INTEGER,
	year_stop		INTEGER,
	investigation_icd_array	VARCHAR[],
	investigation_desc_array VARCHAR[],
	covariate_array		VARCHAR[]
	)
RETURNS void
SECURITY INVOKER
AS $func$
/*
Function: 	rif40_create_disease_mapping_example()
Parameters:	Geography, geolevel view, geolevel area, geolevel map, geolevel select, 
		geolevel selection array, project, study name, denominator table, numerator table,
 		year_start, year_stop,
		investigation ICD conditions array, investigation descriptions array, covariate array
Returns:	Nothing
Description:	Create disease mapping exmaple

Setup a disease mapping example.

WARNING: This function does not test the validity of the inputs, it relies on the trigger functions.
This allows it to be used for test putposes

1. Setup geographic area to be studied

View the geolevel <geolevel_view> of <geolevel_area> and select at <geolevel_select> geolevel and map at <geolevel_map>. 
Provide list of areas selected <geolevel_selection>

E.g. [Values required in ()'s]

Geography: 		England and Wales 2001 (EW01)
Geolevel view: 		2001 Government office region (GOR2001)
Geolevel area: 		London (H)
Geolevel map: 		2001 Census statistical ward (WARD2001)
Gelevel select: 	2001 local area district/unitary authority (LADUA2001)
Geolevel selection:	Array of LADUA2001

Geolevel view and area define the geolevel and area to be mapped (so the user can select geolevel section)
Gelevel select defines the geoelevel the user will select at
Geolevel map define the geolevel the RIF will map at
Geolevel selection is an array of <geolevel_select> that the user selected. The forms the study area.
The comparison geolevel is the default set in rif40_geographies
The comparison area is the RIF default (the array produced by rif40_geo_pkg.get_default_comparison_area())

2. INSERT INTO rif40_studies

RIF40_STUDIES defaults (schema):

username		USER
study_id 		(nextval('rif40_study_id_seq'::regclass))::integer
study_date		LOCALIMESTAMP
study_state		C
audsid			sys_context('USERENV'::character varying, 'SESSIONID'::character varying)

RIF40_STUDIES defaults (from trigger):

extract_table		S_<study_id>_EXTRACT
map_table		S_<study_id>_MAP

RIF40_STUDIES defaults (this function):

study_type		1		[disease mapping]
direct_stand_tab	NULL
suppression_value	parameter "SuppressionValue"
extract_permitted	1 for SAHSU geogrpahy, 0 otherwise
transfer_permitted	1 for SAHSU geogrpahy, 0 otherwise

RIF40_STUDIES values  (where different from parameter names):
 
comparison_geolevel_name	<geolevel_view>
study_geolevel_name		<geolevel_map>
min_age_group, max_age_group	MIN/MAX defined for denominator

3. INSERT INTO rif40_investigations

RIF40_INVESTIGATIONS defaults (schema)

username		USER
study_id 		(currval('rif40_study_id_seq'::regclass))::integer
inv_id			(nextval('rif40_inv_id_seq'::regclass))::integer
classifier		QUANTILE
classifier_bands	5
investigation_state	C
mh_test_type		No test

RIF40_INVESTIGATIONS defaults (this function):

genders			3 [both]
min_age_group, max_age_group	MIN/MAX defined for denominator
inv_name		INV_<n> [index in investigation ICD conditions array]

4. INSERT INTO rif40_inv_conditions

RIF40_INV_CONDITIONS defaults (schema)

username		USER
study_id 		(currval('rif40_study_id_seq'::regclass))::integer
inv_id			(nextval('rif40_inv_id_seq'::regclass))::integer
line_number		1

5. INSERT INTO rif40_study_areas

RIF40_STUDY_AREAS defaults (schema)

username		USER
study_id 		(currval('rif40_study_id_seq'::regclass))::integer

RIF40_STUDY_AREAS defaults (this function)

band_id			<n> [index in geolevel selection array]	

6. INSERT INTO rif40_comparison_areas

RIF40_COMPARISON_AREAS defaults (schema)

username		USER
study_id 		(currval('rif40_study_id_seq'::regclass))::integer

The actual comparison area comes from rif40_geo_pkg.rif40_get_default_comparison_area().
This returns all areas at the default comparison area level covered by the users selected geolevels.

7. INSERT INTO rif40_inv_covariates

RIF40_INV_COVARIATES defaults (schema)

username		USER
study_id 		(currval('rif40_study_id_seq'::regclass))::integer
inv_id			(nextval('rif40_inv_id_seq'::regclass))::integer


RIF40_INV_COVARIATES defaults (this function)

min, max		MIN/MAX for covariate
study_geolevel_name	study_geolevel_name FROM rif40_studies

8. INSERT INTO rif40_study_shares if any RIF DB role is a RIF manager and extraction is permitted

RIF40_STUDY_SHARES defaults (schema)

grantor			USER
study_id 		(currval('rif40_study_id_seq'::regclass))::integer

 */
DECLARE
	c1cdm CURSOR(l_table_name VARCHAR) FOR  
		WITH a AS (
			SELECT a.age_group_id, MIN(a.offset) AS min_age_group, MAX(a.offset) AS max_age_group
			  FROM rif40_age_groups a, rif40_tables b
			 WHERE a.age_group_id = b.age_group_id
			   AND b.table_name   = l_table_name
			 GROUP BY a.age_group_id
		)
		SELECT min_age_group, max_age_group, 
		       b.fieldname AS min_age_group_name,
		       c.fieldname AS max_age_group_name
		  FROM a
			LEFT OUTER JOIN rif40_age_groups b ON (a.age_group_id = b.age_group_id AND a.min_age_group = b.offset) 
			LEFT OUTER JOIN rif40_age_groups c ON (a.age_group_id = c.age_group_id AND a.max_age_group = c.offset); 
	c1cdm_rec RECORD;
	c2cdm CURSOR FOR
		SELECT p2.param_name, 
		       CASE WHEN pg_has_role(USER, 'rif_no_suppression', 'USAGE') THEN 1 /* Not Suppressed */ ELSE p2.param_value::INTEGER END suppression_value
		  FROM rif40.rif40_parameters p2
		 WHERE p2.param_name = 'SuppressionValue';
	c2cdm_rec RECORD;
	c3cdm CURSOR(l_geography VARCHAR) FOR
		SELECT defaultcomparea, hierarchytable
		  FROM rif40_geographies a
		 WHERE a.geography = l_geography;
	c3cdm_rec RECORD;
	c4cdm CURSOR FOR /* Valid RIF users */
		SELECT rolname, pg_has_role(rolname, 'rif_manager', 'USAGE') AS is_rif_manager
		  FROM pg_roles r, pg_namespace n
		 WHERE (pg_has_role(rolname, 'rif_user', 'USAGE') OR pg_has_role(rolname, 'rif_manager', 'USAGE'))
		   AND n.nspowner = r.oid
		   AND nspname = rolname;
	c4cdm_rec RECORD;
--
	i 			INTEGER:=0;
	l_inv_name	 	VARCHAR;
	l_area_id 		VARCHAR;
	icd 			VARCHAR;
	comparision_area	VARCHAR[];
	study_area_count	INTEGER;
	comparison_area_count	INTEGER;
	covariate_count		INTEGER;
-- 
	sql_stmt		VARCHAR;
--
	l_extract_permitted 	INTEGER:=0;
	l_transfer_permitted 	INTEGER:=0;
--
BEGIN
--
-- Check INVESTIGATION array are the same length
--
	IF array_length(investigation_icd_array, 1) != array_length(investigation_desc_array, 1) THEN
		PERFORM rif40_log_pkg.rif40_error(-90901, 'rif40_create_disease_mapping_example', 
			'icd array length (%) != description array length (%)',
			array_length(investigation_icd_array, 1)::VARCHAR,
			array_length(investigation_desc_array, 1)::VARCHAR);
	END IF;
--
-- Get MIN/MAX age groups. Check table exists
--
	OPEN c1cdm(numer_tab);
	FETCH c1cdm INTO c1cdm_rec;
	IF NOT FOUND THEN
		PERFORM rif40_log_pkg.rif40_error(-90902, 'rif40_create_disease_mapping_example', 
			'Cannot find numerator table: %',
			numer_tab::VARCHAR);
	END IF;
	CLOSE c1cdm;
--
-- Get suppression_value
--
	OPEN c2cdm;
	FETCH c2cdm INTO c2cdm_rec;
	IF NOT FOUND THEN
		PERFORM rif40_log_pkg.rif40_error(-90903, 'rif40_create_disease_mapping_example', 
			'Cannot find SuppressionValue parameter');
	END IF;
	CLOSE c2cdm;
--
-- Get comparison geolevel - is the default set in rif40_geographies
--
	OPEN c3cdm(geography);
	FETCH c3cdm INTO c3cdm_rec;
	IF NOT FOUND THEN
		PERFORM rif40_log_pkg.rif40_error(-90904, 'rif40_create_disease_mapping_example', 
			'Cannot find rif40_geographies geography: %',
			geography::VARCHAR);
	END IF;
	CLOSE c3cdm;
--
-- Set up IG - SAHSU no restrictions; otherwise full restrictions
--
	IF geography = 'SAHSU' THEN
		l_extract_permitted:=1;
		l_transfer_permitted:=1;
	END IF;
--
-- 2. INSERT INTO rif40_studies
--
	INSERT INTO rif40_studies (
 		geography, project, study_name, study_type,
 		comparison_geolevel_name, study_geolevel_name, denom_tab,            
 		year_start, year_stop, max_age_group, min_age_group, 
 		suppression_value, extract_permitted, transfer_permitted)
	VALUES (
		 geography 			/* geography */,
		 project 			/* project */, 
		 study_name 			/* study_name */,
		 1 				/* study_type [disease mapping] */,
		 c3cdm_rec.defaultcomparea	/* comparison_geolevel_name */,
		 geolevel_map 			/* study_geolevel_name */,   
		 denom_tab 			/* denom_tab */,            
		 year_start			/* year_start */,       
		 year_stop 			/* year_stop */,      
		 c1cdm_rec.max_age_group 	/* max_age_group */, 
		 c1cdm_rec.min_age_group 	/* min_age_group */, 
		 c2cdm_rec.suppression_value 	/* suppression_value */, 
		 l_extract_permitted 		/* extract_permitted */, 
		 l_transfer_permitted		/* transfer_permitted */);
	PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_create_disease_mapping_example', 
		'[90905] Created study: % % for project %',  
		currval('rif40_study_id_seq'::regclass)::VARCHAR,
		study_name::VARCHAR,
		project::VARCHAR);
	PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_create_disease_mapping_example', 
		'[90906] View the geolevel % of "%" and select at % geolevel and map at %; default comparison area %',
		geolevel_view::VARCHAR,
		geolevel_area::VARCHAR, 
		geolevel_select::VARCHAR,
		geolevel_map::VARCHAR,
		c3cdm_rec.defaultcomparea::VARCHAR);
	PERFORM rif40_log_pkg.rif40_log('DEBUG3', 'rif40_create_disease_mapping_example', 
		'[90907] List of areas selected: "%"', 
		array_to_string(geolevel_selection, '","')::VARCHAR);
	PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_create_disease_mapping_example', 
		'[90908] Denominator: %; period: %-%; age groups % to %',
		denom_tab::VARCHAR,
		year_start::VARCHAR,
		year_stop::VARCHAR,
		c1cdm_rec.min_age_group_name::VARCHAR,
		c1cdm_rec.max_age_group_name::VARCHAR);
	PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_create_disease_mapping_example', 
		'[90909] Suppression value: %; extract permitted: %; transfer permitted %',
		c2cdm_rec.suppression_value::VARCHAR,
		l_extract_permitted::VARCHAR,
		l_transfer_permitted::VARCHAR);
--
-- 3. INSERT INTO rif40_investigations
--
-- Process investigations array
--
	FOREACH icd IN ARRAY investigation_icd_array LOOP
		i:=i+1;
		l_inv_name:='INV_'||i::VARCHAR;
		INSERT INTO rif40_investigations(
			geography,
			inv_name,
			inv_description,
			genders,
			numer_tab, 
			year_start,
			year_stop, 
			max_age_group,
			min_age_group
		)
		VALUES (
		 	geography 		/* geography */,
			l_inv_name 		/* inv_name */,  
			investigation_desc_array[i]	/* inv_description */,
			3			/* genders [both] */,
			numer_tab		/* numer_tab */,
		 	year_start		/* year_start */,       
			year_stop 		/* year_stop */,      
			c1cdm_rec.max_age_group /* max_age_group */, 
			c1cdm_rec.min_age_group /* min_age_group */);
--
-- 4. INSERT INTO rif40_inv_conditions
--
		INSERT INTO rif40_inv_conditions(
			condition)
		VALUES (
			investigation_icd_array[i]	/* ICD */);
		PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_create_disease_mapping_example', 	
			'[90910] Created investigation: % (%): %; numerator: %; condition: "%"',  
			currval('rif40_inv_id_seq'::regclass)::VARCHAR,
			l_inv_name::VARCHAR,
			investigation_desc_array[i]::VARCHAR,
			numer_tab::VARCHAR,
			investigation_icd_array[i]::VARCHAR);
	END LOOP;
--
-- 5. INSERT INTO rif40_study_areas
--
-- Process geolevel_selection array, populate study areas
--
/*
	i:=0;
	FOREACH l_area_id IN ARRAY geolevel_selection LOOP
		i:=i+1;
		INSERT INTO rif40_study_areas(
			area_id,
			band_id)
		VALUES (
			l_area_id,
			i);
		PERFORM rif40_log_pkg.rif40_log('DEBUG4', 'rif40_create_disease_mapping_example', 	
			'Study area band: %, area_id: %',
			i::VARCHAR,
			l_area_id::VARCHAR);
	END LOOP;
 */
	IF geolevel_map = geolevel_select THEN
		INSERT INTO rif40_study_areas(area_id, band_id) 
		SELECT unnest(geolevel_selection) /* at Geolevel select */ AS study_area, ROW_NUMBER() OVER() AS band_id;
--
-- User selection carried out at different level to mapping
--
	ELSE
		BEGIN    
			sql_stmt:='INSERT INTO rif40_study_areas(area_id, band_id)'||E'\n'||
				'SELECT DISTINCT '||LOWER(geolevel_map)||', ROW_NUMBER() OVER() AS band_id'||E'\n'||
				E'\t'||'  FROM '||LOWER(c3cdm_rec.hierarchytable)||E'\n'||
				E'\t'||' WHERE '||LOWER(geolevel_select)||' IN ('||E'\n'||
				E'\t'||'SELECT unnest($1) /* at Geolevel select */ AS study_area)';
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_create_disease_mapping_example', 	
				'SQL> %;',
				sql_stmt::VARCHAR);
			EXECUTE sql_stmt USING geolevel_selection;
  	     	EXCEPTION
			WHEN others THEN
				PERFORM rif40_log_pkg.rif40_error(-90912, 'rif40_create_disease_mapping_example',
					'[90911] INSERT INTO rif40_study_areas % raised by: %',
					sqlerrm::VARCHAR		/* Error */,
					sql_stmt::VARCHAR		/* SQL */);      
		END;
	END IF;
	GET DIAGNOSTICS study_area_count = ROW_COUNT;

--
-- Get default comparison area, populate comparison areas [INSERT/trigger]
--
	comparision_area:=rif40_geo_pkg.rif40_get_default_comparison_area(geography, geolevel_select, geolevel_selection);
	IF comparision_area IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-90913, 'rif40_create_disease_mapping_example', 
			'rif40_geo_pkg.rif40_get_default_comparison_area(%, %, <geolevel_selection>) returned NULL',
			geography::VARCHAR,
			geolevel_select::VARCHAR);
	END IF;
--
-- 6. INSERT INTO rif40_comparison_areas
--
/*
	FOREACH l_area_id IN ARRAY comparision_area LOOP
		INSERT INTO  rif40_comparison_areas(
			area_id)
		VALUES (
			l_area_id);
		PERFORM rif40_log_pkg.rif40_log('DEBUG4', 'rif40_create_disease_mapping_example', 	
			'Comparison area_id: %',
			l_area_id::VARCHAR);
	END LOOP;
 */
	INSERT INTO rif40_comparison_areas(area_id) SELECT unnest(comparision_area) AS comparision_area;
	GET DIAGNOSTICS comparison_area_count = ROW_COUNT;
--
-- 7. INSERT INTO rif40_inv_covariates
--
	INSERT INTO rif40_inv_covariates(geography, covariate_name, study_geolevel_name, min, max) 
		WITH a AS (
			SELECT unnest(covariate_array) AS covariate_name, geolevel_map AS study_geolevel_name, geography AS geography 
		)
		SELECT a.geography, a.covariate_name, a.study_geolevel_name, b.min, b.max
		  FROM a
			LEFT OUTER JOIN rif40_covariates b ON 
				(a.covariate_name = b.covariate_name AND a.study_geolevel_name = b.geolevel_name AND a.geography = b.geography);
	GET DIAGNOSTICS covariate_count = ROW_COUNT;
--
	PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_create_disease_mapping_example', 	
		'[90914] Inserted % study area(s); % comparision area(s) [default comparison area geolevel: %]; % covariate(s)',
		study_area_count::VARCHAR,
		comparison_area_count::VARCHAR,
		c3cdm_rec.defaultcomparea::VARCHAR,
		covariate_count::VARCHAR);
--
-- 8. INSERT INTO rif40_study_shares if any RIF DB role is a RIF manager and extraction is permitted
--
	FOR c4cdm_rec IN c4cdm LOOP
		IF c4cdm_rec.is_rif_manager AND l_extract_permitted = 1 THEN
			INSERT INTO rif40_study_shares(grantee_username) VALUES (c4cdm_rec.rolname);
			PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_create_disease_mapping_example', 
				'[90915] Shared study % to %',
				currval('rif40_study_id_seq'::regclass)::VARCHAR,
				c4cdm_rec.rolname::VARCHAR);	
		END IF;
	END LOOP;
--
END;
$func$ LANGUAGE plpgsql;

COMMENT ON FUNCTION rif40_sm_pkg.rif40_create_disease_mapping_example(
	VARCHAR,VARCHAR,VARCHAR,VARCHAR,VARCHAR,VARCHAR[],VARCHAR,VARCHAR,VARCHAR,VARCHAR,INTEGER,INTEGER,VARCHAR[],VARCHAR[],VARCHAR[]) 
IS 'Function: 	rif40_create_disease_mapping_example()
Parameters:	Geography, geolevel view, geolevel area, geolevel map, geolevel select, 
		geolevel selection array, project, study name, denominator table, numerator table,
 		year_start, year_stop,
		investigation ICD conditions array, investigation descriptions array, covariate array
Returns:	Nothing
Description:	Create disease mapping exmaple

Setup a disease mapping example.

WARNING: This function does not test the validity of the inputs, it relies on the trigger functions.
This allows it to be used for test putposes

1. Setup geographic area to be studied

View the geolevel <geolevel_view> of <geolevel_area> and select at <geolevel_select> geolevel and map at <geolevel_map>. 
Provide list of areas selected <geolevel_selection>

E.g. [Values required in ()''s]

Geography: 		England and Wales 2001 (EW01)
Geolevel view: 		2001 Government office region (GOR2001)
Geolevel area: 		London (H)
Geolevel map: 		2001 Census statistical ward (WARD2001)
Gelevel select: 	2001 local area district/unitary authority (LADUA2001)
Geolevel selection:	Array of LADUA2001

Geolevel view and area define the geolevel and area to be mapped (so the user can select geolevel section)
Gelevel select defines the geoelevel the user will select at
Geolevel map define the geolevel the RIF will map at
Geolevel selection is an array of <geolevel_select> that the user selected. The forms the study area.
The comparison geolevel is the default set in rif40_geographies
The comparison area is the RIF default (the array produced by rif40_geo_pkg.get_default_comparison_area())

2. INSERT INTO rif40_studies

RIF40_STUDIES defaults (schema):

username		USER
study_id 		(nextval(''rif40_study_id_seq''::regclass))::integer
study_date		LOCALIMESTAMP
study_state		C
audsid			sys_context(''USERENV''::character varying, ''SESSIONID''::character varying)

RIF40_STUDIES defaults (from trigger):

extract_table		S_<study_id>_EXTRACT
map_table		S_<study_id>_MAP

RIF40_STUDIES defaults (this function):

study_type		1		[disease mapping]
direct_stand_tab	NULL
suppression_value	parameter "SuppressionValue"
extract_permitted	1 for SAHSU geogrpahy, 0 otherwise
transfer_permitted	1 for SAHSU geogrpahy, 0 otherwise

RIF40_STUDIES values  (where different from parameter names):
 
comparison_geolevel_name	<geolevel_view>
study_geolevel_name		<geolevel_map>
min_age_group, max_age_group	MIN/MAX defined for denominator

3. INSERT INTO rif40_investigations

RIF40_INVESTIGATIONS defaults (schema)

username		USER
study_id 		(currval(''rif40_study_id_seq''::regclass))::integer
inv_id			(nextval(''rif40_inv_id_seq''::regclass))::integer
classifier		QUANTILE
classifier_bands	5
investigation_state	C
mh_test_type		No test

RIF40_INVESTIGATIONS defaults (this function):

genders			3 [both]
min_age_group, max_age_group	MIN/MAX defined for denominator
inv_name		INV_<n> [index in investigation ICD conditions array]

4. INSERT INTO rif40_inv_conditions

RIF40_INV_CONDITIONS defaults (schema)

username		USER
study_id 		(currval(''rif40_study_id_seq''::regclass))::integer
inv_id			(nextval(''rif40_inv_id_seq''::regclass))::integer
line_number		1

5. INSERT INTO rif40_study_areas

RIF40_STUDY_AREAS defaults (schema)

username		USER
study_id 		(currval(''rif40_study_id_seq''::regclass))::integer

RIF40_STUDY_AREAS defaults (this function)

band_id			<n> [index in geolevel selection array]	

6. INSERT INTO rif40_comparison_areas

RIF40_COMPARISON_AREAS defaults (schema)

username		USER
study_id 		(currval(''rif40_study_id_seq''::regclass))::integer

The actual comparison area comes from rif40_geo_pkg.rif40_get_default_comparison_area().
This returns all areas at the default comparison area level covered by the users selected geolevels.

7. INSERT INTO rif40_inv_covariates

RIF40_INV_COVARIATES defaults (schema)

username		USER
study_id 		(currval(''rif40_study_id_seq''::regclass))::integer
inv_id			(nextval(''rif40_inv_id_seq''::regclass))::integer


RIF40_INV_COVARIATES defaults (this function)

min, max		MIN/MAX for covariate
study_geolevel_name	study_geolevel_name FROM rif40_studies

8. INSERT INTO rif40_study_shares if any RIF DB role is a RIF manager and extraction is permitted

RIF40_STUDY_SHARES defaults (schema)

grantor			USER
study_id 		(currval(''rif40_study_id_seq''::regclass))::integer
';

GRANT EXECUTE ON FUNCTION rif40_sm_pkg.rif40_create_disease_mapping_example(
	VARCHAR,VARCHAR,VARCHAR,VARCHAR,VARCHAR,VARCHAR[],VARCHAR,VARCHAR,VARCHAR,VARCHAR,INTEGER,INTEGER,VARCHAR[],VARCHAR[],VARCHAR[]) 
	TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_sm_pkg.rif40_create_disease_mapping_example(
	VARCHAR,VARCHAR,VARCHAR,VARCHAR,VARCHAR,VARCHAR[],VARCHAR,VARCHAR,VARCHAR,VARCHAR,INTEGER,INTEGER,VARCHAR[],VARCHAR[],VARCHAR[]) 
 	TO rif40;

\echo Built rif40_sm_pkg (state machine and extract SQL generation) package.
--
-- Eof

