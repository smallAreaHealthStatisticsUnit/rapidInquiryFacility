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
-- Rapid Enquiry Facility (RIF) - RI40_NUM_DENOM check procedure.
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
-- psql -U rif40 -d sahsuland -w -e -f rifDatabase\Postgres\PLpgsql\rif40_sql_pkg\rif40_auto_indirect_checks.sql
--
\set ECHO all
\set ON_ERROR_STOP ON
\timing


CREATE OR REPLACE FUNCTION rif40_sql_pkg.rif40_auto_indirect_checks(l_table_name VARCHAR)
RETURNS VARCHAR
SECURITY INVOKER
AS $func$

/*

Function: 	rif40_auto_indirect_checks()
Parameters:	table name
Returns: 	NULL if not found or not an indirect denominator, geography and counts
Description:	Automatic (Able to be used in automatic RIF40_NUM_DENOM (0/1, default 0). 
		A user specific T_RIF40_NUM_DENOM is supplied for other combinations. 
		Cannot be applied to direct standardisation denominator) is restricted to 1 denominator per geography.
 */
DECLARE
	c1autoin CURSOR(l_table VARCHAR) IS
		SELECT isindirectdenominator, isnumerator, automatic
		  FROM rif40_tables
		 WHERE l_table = table_name;
	c2 CURSOR(l_table VARCHAR) IS
		WITH valid_geog AS (
			SELECT g.geography
			  FROM rif40_geographies g
			 WHERE rif40_sql_pkg.rif40_num_denom_validate(g.geography, l_table) = 1
		)
		SELECT valid_geog.geography geography, COUNT(t.table_name) total_denominators
		  FROM rif40_tables t, valid_geog 
		 WHERE t.table_name            != l_table
		   AND t.isindirectdenominator = 1
   		   AND t.automatic             = 1
		   AND rif40_sql_pkg.rif40_num_denom_validate(valid_geog.geography, t.table_name) = 1
		 GROUP BY valid_geog.geography
		 ORDER BY 1;
	c3 CURSOR(l_geography VARCHAR, l_table VARCHAR) IS
		SELECT t.table_name 
		  FROM rif40_tables t 
		 WHERE t.table_name != l_table
		   AND t.isindirectdenominator = 1
   		   AND t.automatic             = 1
		   AND rif40_sql_pkg.rif40_num_denom_validate(l_geography, t.table_name) = 1
		 ORDER BY 1;
--
	c1_rec RECORD;
	c2_rec RECORD;
	c3_rec RECORD;
--
	msg 		VARCHAR:=NULL;
	dmsg 		VARCHAR:=NULL;
	i		INTEGER:=0;
	j		INTEGER:=0;
BEGIN
--
-- Must be rif40 or have rif_user or rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		IF CURRENT_DATABASE() = 'sahsuland_dev' THEN
			PERFORM rif40_log_pkg.rif40_error(-20999, 'rif40_auto_indirect_checks', 'User % must be rif40 or have rif_user or rif_manager role', 
				USER::VARCHAR);
		ELSE 
			RETURN 0;
		END IF;		
	END IF;
--
-- If inputs are NULL return NULL 
--
	IF l_table_name IS NULL THEN
		RETURN NULL;
	END IF;
--
-- automatic indirect denominator checks
--
	OPEN c1autoin(l_table_name);
	FETCH c1autoin INTO c1_rec;
	CLOSE c1autoin;
	IF c1_rec.automatic = 0 OR c1_rec.isindirectdenominator != 1 OR c1_rec.isnumerator = 1 THEN	
		IF CURRENT_DATABASE() = 'sahsuland_dev' THEN
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_auto_indirect_checks', 'table: % is not an automatic indirect denominator', 
				l_table_name::VARCHAR);
		END IF;
		RETURN NULL;
	END IF;
--
-- Check object is resolvable
--
	IF rif40_sql_pkg.rif40_is_object_resolvable(l_table_name) = 0 THEN
		IF CURRENT_DATABASE() = 'sahsuland_dev' THEN
			PERFORM rif40_log_pkg.rif40_log('DEBUG1', 'rif40_auto_indirect_checks', 'table: % not resolvable', 
				l_table_name::VARCHAR);
		END IF;
		RETURN NULL;
	END IF;
--
	IF c1_rec.isindirectdenominator = 1 THEN
		FOR c2_rec IN c2(l_table_name) LOOP
			j:=j+1;
			IF c2_rec.total_denominators > 0 THEN
				IF msg IS NULL THEN
					msg:=E'\n'||c2_rec.geography||' '||c2_rec.total_denominators;
				ELSE
					msg:=msg||', '||c2_rec.geography||' '||c2_rec.total_denominators;
				END IF;
--
				i:=0;
				FOR c3_rec IN c3(c2_rec.geography, l_table_name) LOOP
					i:=i+1;
					IF i = 1 THEN
						dmsg:=' ('||c3_rec.table_name;
					ELSE
						dmsg:=dmsg||', '||c3_rec.table_name;
					END IF;
				END LOOP;
				dmsg:=dmsg||')';		
				IF CURRENT_DATABASE() = 'sahsuland_dev' THEN
					PERFORM rif40_log_pkg.rif40_log('DEBUG2', 'rif40_auto_indirect_checks', 'table[%]: %, geography: %; total_denominators = %; %', 
						j::VARCHAR, l_table_name::VARCHAR, c2_rec.geography::VARCHAR, c2_rec.total_denominators::VARCHAR, dmsg::VARCHAR);
				END IF;	
			ELSE		
				IF CURRENT_DATABASE() = 'sahsuland_dev' THEN
					PERFORM rif40_log_pkg.rif40_log('DEBUG2', 'rif40_auto_indirect_checks', 'table[%]: %, geography: %; total_denominators = 0', 
						j::VARCHAR, l_table_name::VARCHAR, c2_rec.geography::VARCHAR);
				END IF;	
			END IF;	
		END LOOP;
	END IF;	
	RETURN msg;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_sql_pkg.rif40_auto_indirect_checks(VARCHAR) IS 'Function: 	rif40_auto_indirect_checks()
Parameters:	table name
Returns: 	NULL if not found or not an indirect denominator, geography and counts
Description:	Automatic (Able to be used in automatic RIF40_NUM_DENOM (0/1, default 0). 
		A user specific T_RIF40_NUM_DENOM is supplied for other combinations. 
		Cannot be applied to direct standardisation denominator) is restricted to 1 denominator per geography.';

\df rif40_sql_pkg.rif40_auto_indirect_checks
