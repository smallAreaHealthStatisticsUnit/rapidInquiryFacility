-- ************************************************************************
--
-- GIT Header
--
-- $Format:Git ID: (%h) %ci$
-- $Id$
-- Version hash: $Format:%H$
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - RIF state machine
--     				  rif40_verify_state_change
--
-- Copyright:
--
-- The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
-- that rapidly addresses epidemiological and public health questions using 
-- routinely collected health and population data and generates standardised 
-- rates and relative risks for any given health outcome, for specified age 
-- and year ranges, for any given geographical area.
--
-- Copyright 2014 Imperial College London, developed by the Small Area
-- Health Statistics Unit. The work of the Small Area Health Statistics Unit 
-- is funded by the Public Health England as part of the MRC-PHE Centre for 
-- Environment and Health. Funding for this project has also been received 
-- from the Centers for Disease Control and Prevention.  
--
-- This file is part of the Rapid Inquiry Facility (RIF) project.
-- RIF is free software: you can redistribute it and/or modify
-- it under the terms of the GNU Lesser General Public License as published by
-- the Free Software Foundation, either version 3 of the License, or
-- (at your option) any later version.
--
-- RIF is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
-- GNU Lesser General Public License for more details.
--
-- You should have received a copy of the GNU Lesser General Public License
-- along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
-- to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
-- Boston, MA 02110-1301 USA
--
-- Author:
--
-- Peter Hambly, SAHSU
--
-- Error codes assignment (see PLpgsql\Error_codes.txt):
--
-- rif40_sm_pkg:
--
-- rif40_verify_state_change: 		55000 to 55199
--
\set ECHO all
\set ON_ERROR_STOP ON
\timing

--
-- Check user is rif40
--
DO LANGUAGE plpgsql $$
BEGIN
	IF user = 'rif40' THEN
		RAISE INFO 'User check: %', user;	
	ELSE
		RAISE EXCEPTION 'C20900: User check failed: % is not rif40', user;	
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
	IF old_study_state = 'U' AND new_study_state != old_study_state THEN
		PERFORM rif40_log_pkg.rif40_error(-55000, 'rif40_verify_state_change', 
			'Attempting to change the state (%=>%) of upgraded RIF30 study %. Please clone',
			old_study_state::VARCHAR,
			new_study_state::VARCHAR,
			study_id::VARCHAR);
	ELSIF old_study_state = 'R' AND new_study_state != old_study_state THEN
		PERFORM rif40_log_pkg.rif40_error(-55001, 'rif40_verify_state_change', 
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
		PERFORM rif40_log_pkg.rif40_error(-55002, 'rif40_verify_state_change', 
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
			'[55003] SQL> %;',
			sql_stmt::VARCHAR);
		OPEN c1_verst FOR EXECUTE sql_stmt USING study_id;
		FETCH c1_verst INTO c1_rec;
		IF c1_rec.total = 0 THEN
			missing_study_id_rows:=missing_study_id_rows+1;
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_verify_state_change', 
				'[55004] % table has no rows during attempted state transition (%=>%) for study %',
				l_table::VARCHAR,
				old_study_state::VARCHAR,
				new_study_state::VARCHAR,
				study_id::VARCHAR);		
		ELSE
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_verify_state_change', 
				'[55005] % table has % rows during attempted state transition (%=>%) for study %',
				l_table::VARCHAR,
				c1_rec.total::VARCHAR,
				old_study_state::VARCHAR,
				new_study_state::VARCHAR,
				study_id::VARCHAR);		
		END IF;
		CLOSE c1_verst;
	END LOOP;
	IF missing_study_id_rows > 0 THEN
		PERFORM rif40_log_pkg.rif40_error(-55006, 'rif40_verify_state_change', 
			'% study_id tables have no rows during attempted state transition (%=>%) for study %',
			missing_study_id_rows::VARCHAR,
			old_study_state::VARCHAR,
			new_study_state::VARCHAR,
			study_id::VARCHAR);		
	ELSE
		PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_verify_state_change', 
			'[55007] No study_id tables have no rows during attempted state transition (%=>%) for study %',
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
				'[55008] SQL> %;',
				sql_stmt::VARCHAR);
			OPEN c1_verst FOR EXECUTE sql_stmt USING study_id;
			FETCH c1_verst INTO c1_rec;
			IF c1_rec.total = 0 THEN
				NULL;
			ELSE
				unexpected_results_rows:=unexpected_results_rows+1;
				PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_verify_state_change', 
					'[55009] Results % table has % rows during attempted state transition (%=>%) for study %',
					l_table::VARCHAR,
					c1_rec.total::VARCHAR,
					old_study_state::VARCHAR,
					new_study_state::VARCHAR,
					study_id::VARCHAR);
			END IF;
			CLOSE c1_verst;
		END LOOP;
		IF unexpected_results_rows > 0 THEN
			PERFORM rif40_log_pkg.rif40_error(-55010, 'rif40_verify_state_change', 
				'% study_id results tables have no rows during attempted state transition (%=>%) for study %',
				unexpected_results_rows::VARCHAR,
				old_study_state::VARCHAR,
				new_study_state::VARCHAR,
				study_id::VARCHAR);		
		ELSE	
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_verify_state_change', 
				'[55011] No study_id results tables have no rows during attempted state transition (%=>%) for study %',
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
		PERFORM rif40_log_pkg.rif40_error(-55012, 'rif40_verify_state_change', 
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
			'[55013] Multiple covariates (% investigations()) not yet supported during attempted state transition (%=>%) for study %',
			c2_rec.max_inv_covariate_count::VARCHAR,
			old_study_state::VARCHAR,
			new_study_state::VARCHAR,
			study_id::VARCHAR);		
	END IF;
	IF c2_rec.direct_stand_tab IS NOT NULL THEN
		not_implemented:=not_implemented+1;
		PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_verify_state_change', 
			'[55014] Direct standardisation using % not yet supported during attempted state transition (%=>%) for study %',
			c2_rec.direct_stand_tab::VARCHAR,
			old_study_state::VARCHAR,
			new_study_state::VARCHAR,
			study_id::VARCHAR);		
	END IF;
	FOR c4_rec IN c4_verst(study_id) LOOP
		IF c4_rec.numer_age_sex_group_field_name IS NULL THEN
			not_implemented:=not_implemented+1;
			PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_verify_state_change', 
				'[55015] AGE_GROUP+SEX columns not yet supported (AGE_SEX_GROUP only) for numerator table % during attempted state transition (%=>%) for study %, investigation %',
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
			'[55016] AGE_GROUP+SEX columns not yet supported (AGE_SEX_GROUP only) for denominator table % during attempted state transition (%=>%) for study %',
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
--	IF c2_rec.study_type != 1 THEN
--		not_implemented:=not_implemented+1;
--		PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_verify_state_change', 
--			'[55017] Non disease mapping studies (%) not yet supported during attempted state transition (%=>%) for study %',
--			c2_rec.study_type::VARCHAR,
--			old_study_state::VARCHAR,
--			new_study_state::VARCHAR,
--			study_id::VARCHAR);	
--	END IF;
--
--	Covariate study_geolevel_name must be the same as rif40_studies.study_geolevel_name
--
	OPEN c3_verst(study_id);
	FETCH c3_verst INTO c3_rec;
	IF NOT FOUND THEN
		PERFORM rif40_log_pkg.rif40_error(-55018, 'rif40_verify_state_change', 
			'Study not found in rif40_inv_covariates during attempted state transition (%=>%) for study %',
			old_study_state::VARCHAR,
			new_study_state::VARCHAR,
			study_id::VARCHAR);
	END IF;
	CLOSE c3_verst;
	IF c3_rec.total_study_geolevel_name != 0 THEN
		not_implemented:=not_implemented+1;
		PERFORM rif40_log_pkg.rif40_log('WARNING', 'rif40_verify_state_change', 
			'[55019] % investigation covariates found with differing <study_geolevel_name> - not yet supported during attempted state transition (%=>%) for study %',
			c3_rec.total_study_geolevel_name::VARCHAR,
			old_study_state::VARCHAR,
			new_study_state::VARCHAR,
			study_id::VARCHAR);	
	END IF;
--
	IF not_implemented > 0 THEN
		PERFORM rif40_log_pkg.rif40_error(-55020, 'rif40_verify_state_change', 
			'% not yet supported features during attempted state transition (%=>%) for study %',
			not_implemented::VARCHAR,
			old_study_state::VARCHAR,
			new_study_state::VARCHAR,
			study_id::VARCHAR);
	END IF;
--		
	PERFORM rif40_log_pkg.rif40_log('INFO', 'rif40_verify_state_change', 
		'[55021] Attempted state transition (%=>%) verified for study %',
		old_study_state::VARCHAR,
		new_study_state::VARCHAR,
		study_id::VARCHAR);
--
	RETURN new_study_state;
END;
$func$
LANGUAGE 'plpgsql';

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
   
GRANT EXECUTE ON FUNCTION rif40_sm_pkg.rif40_verify_state_change(INTEGER, VARCHAR, VARCHAR) TO rif40;
GRANT EXECUTE ON FUNCTION rif40_sm_pkg.rif40_verify_state_change(INTEGER, VARCHAR, VARCHAR) TO rif_user,rif_manager;

--
-- Eof
