-- ************************************************************************
--
-- This view code is for use by alter_N.sql series scripts, not the initial 
-- SAHSULAND build
--
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
-- Rapid Enquiry Facility (RIF) - RIF40_CONTEXTUAL_STATS view
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

DROP VIEW IF EXISTS "rif40_contextual_stats";
CREATE VIEW "rif40_contextual_stats" ("username", "study_id", "inv_id", "area_id", "area_population", 
	"area_observed", "total_comparison_population", "variance_high", "variance_low", "hash_partition_number") 
AS 
SELECT  username, c.study_id, inv_id, area_id, area_population, area_observed, total_comparison_population, 
        variance_high, variance_low, c.hash_partition_number
   FROM t_rif40_contextual_stats c
	LEFT OUTER JOIN rif40_study_shares s ON (c.study_id = s.study_id AND s.grantee_username = USER)
 WHERE username = USER OR
       ('RIF_MANAGER' = (SELECT granted_role FROM user_role_privs WHERE granted_role = 'RIF_MANAGER')) OR
       (s.grantee_username IS NOT NULL AND s.grantee_username::text <> '')
 ORDER BY 1;

GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE rif40_contextual_stats TO rif_user;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE rif40_contextual_stats TO rif_manager;
COMMENT ON VIEW rif40_contextual_stats
  IS 'Contextual stats for results map. Also includes values used in internal calculations.';
COMMENT ON COLUMN rif40_contextual_stats.username IS 'Username';
COMMENT ON COLUMN rif40_contextual_stats.study_id IS 'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq';
COMMENT ON COLUMN rif40_contextual_stats.inv_id IS 'Unique investigation index: inv_id. Created by SEQUENCE rif40_inv_id_seq';
COMMENT ON COLUMN rif40_contextual_stats.area_id IS 'An area id, the value of a geolevel; i.e. the value of the column T_RIF40_GEOLEVELS.GEOLEVEL_NAME in table T_RIF40_GEOLEVELS.LOOKUP_TABLE';
COMMENT ON COLUMN rif40_contextual_stats.area_population IS 'Total population in area';
COMMENT ON COLUMN rif40_contextual_stats.area_observed IS 'Total observed in area';
COMMENT ON COLUMN rif40_contextual_stats.total_comparison_population IS 'Total comparison population. Used for internal calculations.';
COMMENT ON COLUMN rif40_contextual_stats.variance_high IS 'Variance (observed &gt; 100). Used for internal calculations.';
COMMENT ON COLUMN rif40_contextual_stats.variance_low IS 'Variance (observed &lt;= 100). Used for internal calculations.';
COMMENT ON COLUMN rif40_contextual_stats.hash_partition_number IS 'Hash partition number. Used for partition elimination';

--
-- UPDATE/INSERT trigger created separately by rif40_trg_pkg.drop/create_instead_of_triggers
--

--
-- Eof
