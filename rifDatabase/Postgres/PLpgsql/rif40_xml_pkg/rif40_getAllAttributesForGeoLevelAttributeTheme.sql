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
-- Rapid Enquiry Facility (RIF) - Web services integration functions for middleware
--     				  GgetAllAttributesForGeoLevelAttributeTheme
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
-- rif40_xml_pkg:
--
-- getAllAttributesForGeoLevelAttributeTheme: 		50800 to 50999
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

CREATE OR REPLACE FUNCTION rif40_xml_pkg.rif40_getAllAttributesForGeoLevelAttributeTheme(
	l_geography 		VARCHAR,
	l_geolevel_select	VARCHAR,
	l_theme			rif40_xml_pkg.rif40_geolevelAttributeTheme)
RETURNS TABLE(
		attribute_source	VARCHAR, 
		attribute_name		VARCHAR, 
		theme			rif40_xml_pkg.rif40_geolevelAttributeTheme,
		source_description	VARCHAR,
		name_description	VARCHAR)
SECURITY INVOKER
AS $func$
/*
 */
DECLARE
	c1getallatt4theme CURSOR(l_geography VARCHAR) FOR
		SELECT *
		  FROM rif40_geographies
		 WHERE geography     = l_geography;
	c2getallatt4theme CURSOR(l_geography VARCHAR, l_geolevel_select VARCHAR) FOR
		SELECT *
		  FROM rif40_geolevels
		 WHERE geography     = l_geography
		   AND geolevel_name = l_geolevel_select;
--
	c1_rec RECORD;
	c2_rec RECORD;
BEGIN
--
-- Must be rif40 or have rif_user or rif_manager role
--
	IF NOT rif40_sql_pkg.is_rif40_user_manager_or_schema() THEN
		PERFORM rif40_log_pkg.rif40_error(-50800, 'rif40_getAllAttributesForGeoLevelAttributeTheme', 
			'User % must be rif40 or have rif_user or rif_manager role', 
			USER::VARCHAR	/* Username */);
	END IF;
	--
-- Test geography
--
	IF l_geography IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-50801, 'rif40_getAllAttributesForGeoLevelAttributeTheme', 'NULL geography parameter');
	END IF;	
--
	OPEN c1getallatt4theme(l_geography);
	FETCH c1getallatt4theme INTO c1_rec;
	CLOSE c1getallatt4theme;
--
	IF c1_rec.geography IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-50802, 'rif40_getAllAttributesForGeoLevelAttributeTheme', 'geography: % not found', 
			l_geography::VARCHAR		/* Geography */);
	END IF;	
--
-- Test <geolevel select> exists
--
	OPEN c2getallatt4theme(l_geography, l_geolevel_select);
	FETCH c2getallatt4theme INTO c2_rec;
	CLOSE c2getallatt4theme;
--
	IF c2_rec.geolevel_name IS NULL THEN
		PERFORM rif40_log_pkg.rif40_error(-50803, 'rif40_getAllAttributesForGeoLevelAttributeTheme', 
			'geography: %, <geolevel select> %: not found', 
			l_geography::VARCHAR		/* Geography */, 
			l_geolevel_select::VARCHAR	/* geolevel select */);
	END IF;	
--
-- Process themes
--
	IF l_theme = 'covariate' THEN
		RETURN QUERY
			WITH a AS (
				SELECT a.covariate_name, 
				       b.covariate_table
				  FROM rif40_covariates a
					LEFT OUTER JOIN rif40_geolevels b ON 
						(a.geography = b.geography AND a.geolevel_name = b.geolevel_name)
				 WHERE a.geography     = l_geography
				   AND a.geolevel_name = l_geolevel_select
				   AND has_any_column_privilege(b.covariate_table, 'SELECT'::Text) /* Can user access covariate table */
			), b AS (	
	       			SELECT a.covariate_name,  
				       a.covariate_table,
			       	       COALESCE(obj_description(a.covariate_table::regclass, 'pg_class' /* Obj id */), 
						'[Table not found]') AS source_description,
		    		       COALESCE(col_description(a.covariate_table::regclass /* Obj id */, 
						b.ordinal_position /* Column number */), '[Column not found]') AS name_description
				  FROM a
				LEFT OUTER JOIN information_schema.columns b ON 
					(quote_ident(LOWER(a.covariate_table)) = b.table_name AND 
					 quote_ident(LOWER(a.covariate_name))  = b.column_name)
			)
			SELECT LOWER(b.covariate_table)::VARCHAR AS attirbute_source,
			       LOWER(b.covariate_name)::VARCHAR AS attribute_name,  
			       l_theme::rif40_xml_pkg.rif40_geolevelAttributeTheme AS theme,
			       b.source_description::VARCHAR AS source_description,
			       b.name_description::VARCHAR AS name_description 
			  FROM b
			 ORDER BY 1, 2;
	ELSIF l_theme = 'health' THEN
		RETURN QUERY
			WITH a AS (
				SELECT a.numerator_table,
				       a.numerator_description AS source_description
				  FROM rif40_num_denom a
			), b AS (
				SELECT a.numerator_table,
				       b.column_name,
				       a.source_description,
		    		       COALESCE(col_description((a.numerator_table)::regclass /* Obj id */, 
						b.ordinal_position /* Column number */), '[Column not found]') AS name_description
				  FROM a, information_schema.columns b 
				 WHERE quote_ident(LOWER(a.numerator_table)) = b.table_name
				   AND LOWER(b.column_name) != LOWER(l_geolevel_select) 	/* You get this anyway!!! */
		 	)
			SELECT LOWER(b.numerator_table)::VARCHAR AS attribute_source,
			       b.column_name::VARCHAR AS attribute_name,
			       l_theme::rif40_xml_pkg.rif40_geolevelAttributeTheme AS theme,
			       b.source_description::VARCHAR AS source_description,
			       b.name_description::VARCHAR AS name_description
			  FROM b
			 ORDER BY 1, 2;
	ELSIF l_theme = 'extract' THEN
		RETURN QUERY
			WITH a AS (
				SELECT a.extract_table,
			               'Study: '||a.study_id::VARCHAR||'; '||
						COALESCE(a.summary, COALESCE(a.description, '[No summary or description]')) AS source_description
				  FROM rif40_studies a
				 WHERE a.geography           = l_geography
				   AND a.study_geolevel_name = l_geolevel_select
				   AND has_any_column_privilege('rif_studies.'||a.extract_table, 'SELECT'::Text) /* Can user access extract table */
			), b AS (
				SELECT a.extract_table,
				       b.column_name,
				       a.source_description,
		    		       COALESCE(col_description(('rif_studies.'||a.extract_table)::regclass /* Obj id */, 
						b.ordinal_position /* Column number */), '[Column not found]') AS name_description
				  FROM a, information_schema.columns b 
				 WHERE quote_ident(LOWER(a.extract_table)) = b.table_name
				   AND b.column_name != 'area_id' 	/* You get this anyway!!! */
		 	)
			SELECT LOWER(b.extract_table)::VARCHAR AS attribute_source,
			       b.column_name::VARCHAR AS attribute_name,
			       l_theme::rif40_xml_pkg.rif40_geolevelAttributeTheme AS theme,
			       b.source_description::VARCHAR AS source_description,
			       b.name_description::VARCHAR AS name_description
			  FROM b
			 ORDER BY 1, 2;
	ELSIF l_theme = 'results' THEN
		RETURN QUERY
			WITH a AS (
				SELECT a.map_table,
			               'Study: '||a.study_id::VARCHAR||'; '||
						COALESCE(a.summary, COALESCE(a.description, '[No summary or description]')) AS source_description
				  FROM rif40_studies a
				 WHERE a.geography           = l_geography
				   AND a.study_geolevel_name = l_geolevel_select
				   AND has_any_column_privilege('rif_studies.'||a.map_table, 'SELECT'::Text) /* Can user access map table */
			), b AS (
				SELECT a.map_table,
				       b.column_name,
				       a.source_description,
		    		       COALESCE(col_description(('rif_studies.'||a.map_table)::regclass /* Obj id */, 
						b.ordinal_position /* Column number */), '[Column not found]') AS name_description
				  FROM a, information_schema.columns b 
				 WHERE quote_ident(LOWER(a.map_table)) = b.table_name
				   AND b.column_name != 'area_id' 	/* You get this anyway!!! */
		 	)
			SELECT LOWER(b.map_table)::VARCHAR AS attribute_source,
			       b.column_name::VARCHAR AS attribute_name,
			       l_theme::rif40_xml_pkg.rif40_geolevelAttributeTheme AS theme,
			       b.source_description::VARCHAR AS source_description,
			       b.name_description::VARCHAR AS name_description
			  FROM b
			 ORDER BY 1, 2;
	ELSIF l_theme = 'population' THEN
		RETURN QUERY
			WITH a AS (
				SELECT a.denominator_table,
				       a.denominator_table AS source_description
				  FROM rif40_num_denom a
			), b AS (
				SELECT a.denominator_table,
				       b.column_name,
				       a.source_description,
		    		       COALESCE(col_description((a.denominator_table)::regclass /* Obj id */, 
						b.ordinal_position /* Column number */), '[Column not found]') AS name_description
				  FROM a, information_schema.columns b 
				 WHERE quote_ident(LOWER(a.denominator_table)) = b.table_name
				   AND LOWER(b.column_name) != LOWER(l_geolevel_select) 	/* You get this anyway!!! */
		 	)
			SELECT LOWER(b.denominator_table)::VARCHAR AS attribute_source,
			       b.column_name::VARCHAR AS attribute_name,
			       l_theme::rif40_xml_pkg.rif40_geolevelAttributeTheme AS theme,
			       b.source_description::VARCHAR AS source_description,
			       b.name_description::VARCHAR AS name_description
			  FROM b
			 ORDER BY 1, 2;
	ELSIF l_theme = 'geometry' THEN
	ELSE
--
-- This may mean the theme is not supported yet...
--
		PERFORM rif40_log_pkg.rif40_error(-50804, 'rif40_getAllAttributesForGeoLevelAttributeTheme', 
			'Invalid theme: %',
			l_theme::VARCHAR);
	END IF;
--
	RETURN;
END;
$func$
LANGUAGE PLPGSQL;

COMMENT ON FUNCTION rif40_xml_pkg.rif40_getAllAttributesForGeoLevelAttributeTheme(VARCHAR, VARCHAR, rif40_xml_pkg.rif40_geolevelAttributeTheme) IS '';

GRANT EXECUTE ON FUNCTION rif40_xml_pkg.rif40_getAllAttributesForGeoLevelAttributeTheme(VARCHAR, VARCHAR, rif40_xml_pkg.rif40_geolevelAttributeTheme) TO rif_manager;
GRANT EXECUTE ON FUNCTION rif40_xml_pkg.rif40_getAllAttributesForGeoLevelAttributeTheme(VARCHAR, VARCHAR, rif40_xml_pkg.rif40_geolevelAttributeTheme) TO rif_user;

--
-- Eof
