-- ************************************************************************
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - Compare SQL Server and Postgres RIF40 databases
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
-- Margaret Douglass, Peter Hambly, SAHSU
--
-- MS SQL Server specific parameters
--
-- Usage: sqlcmd -d sahsuland_dev -b -m-1 -e -i postgres_diff_report.sql
--
-- MUST BE RUN AS ADMINSTRATOR SO CAN CREATE OBJECTS OR RUN AS RIF40 (with -U rif40)
--
:on error exit
SET QUOTED_IDENTIFIER ON;
:setvar SQLCMDCOLWIDTH 132

-- Compare tables with Postgres RIF40_TABLES_AND_VIEWS
WITH common AS (
	SELECT CASE table_type WHEN 'BASE TABLE' THEN 'TABLE' ELSE table_type END AS table_type, 
		   UPPER(table_name) AS table_or_view_name
	  FROM information_schema.tables WHERE table_schema IN ('rif40', 'rif_data') AND table_catalog = 'sahsuland_dev'
	INTERSECT
	SELECT table_or_view, UPPER(table_or_view_name_hide) AS table_or_view_name
	  FROM rif40.rif40_tables_and_views
), extra AS (
	SELECT CASE table_type WHEN 'BASE TABLE' THEN 'TABLE' ELSE table_type END AS table_type, 
		   UPPER(table_name) AS table_or_view_name
	  FROM information_schema.tables WHERE table_schema IN ('rif40', 'rif_data') AND table_catalog = 'sahsuland_dev'
	EXCEPT
	SELECT table_or_view, UPPER(table_or_view_name_hide) AS table_or_view_name
	  FROM rif40.rif40_tables_and_views
), missing AS (
	SELECT table_or_view, UPPER(table_or_view_name_hide) AS table_or_view_name
	  FROM rif40.rif40_tables_and_views
	EXCEPT
	SELECT CASE table_type WHEN 'BASE TABLE' THEN 'TABLE' ELSE table_type END AS table_type, 
		   UPPER(table_name) AS table_or_view_name
	  FROM information_schema.tables WHERE table_schema IN ('rif40', 'rif_data') AND table_catalog = 'sahsuland_dev'
), new_type AS (
	SELECT COALESCE(CASE t.table_type WHEN 'BASE TABLE' THEN 'TABLE' ELSE t.table_type END, 'UNK') AS table_type, 
	       table_or_view_name
	  FROM (
		SELECT extra.table_or_view_name
		  FROM extra
		 INTERSECT
		SELECT missing.table_or_view_name
		  FROM missing
	  ) AS n1
		LEFT OUTER JOIN information_schema.tables t ON (t.table_schema IN ('rif40', 'rif_data') AND 
													  t.table_catalog = 'sahsuland_dev' AND 
													  UPPER(t.table_name) = n1.table_or_view_name)
), results AS (
	SELECT 'Common' AS type, common.*
	  FROM common
	UNION
	SELECT 'Extra' AS type, extra.*
	  FROM extra
	 WHERE extra.table_or_view_name NOT IN (SELECT table_or_view_name FROM new_type)
	UNION
	SELECT 'Missing' AS type, missing.*
	  FROM missing
	 WHERE missing.table_or_view_name NOT IN (SELECT table_or_view_name FROM new_type)
	UNION
	SELECT 'TABLE <=> VIEW change' AS type, new_type.*
	  FROM new_type
)
SELECT results.type, 
       CAST(results.table_type AS VARCHAR(30)) AS table_type, 
       CAST(results.table_or_view_name AS VARCHAR(50)) AS table_or_view_name
  FROM results
ORDER BY 1, 2, 3;
GO

-- Columns added or removed
WITH common AS (
	SELECT UPPER(table_name) AS table_or_view_name,
		   UPPER(column_name) AS column_name,	
		   CASE is_nullable WHEN 'YES' THEN 'NULL' ELSE 'NOT NULL' END AS nullable,
		   data_type
	  FROM information_schema.columns
	INTERSECT
	SELECT UPPER(table_or_view_name_hide) AS table_or_view_name,
		   UPPER(column_name_hide) AS column_name,	
           nullable, 
           oracle_data_type AS data_type
      FROM rif40.rif40_columns	  
), extra AS (
	SELECT e1.table_or_view_name, 
	       e1.column_name,	
		   CASE c1.is_nullable WHEN 'YES' THEN 'NULL' ELSE 'NOT NULL' END AS nullable,
		   c1.data_type
	  FROM (
		SELECT UPPER(table_name) AS table_or_view_name,
			   UPPER(column_name) AS column_name
		  FROM information_schema.columns
		EXCEPT
		SELECT UPPER(table_or_view_name_hide) AS table_or_view_name,
			   UPPER(column_name_hide) AS column_name
		  FROM rif40.rif40_columns	
		  ) AS e1
		  LEFT OUTER JOIN information_schema.columns c1 ON (UPPER(e1.table_or_view_name) = UPPER(c1.table_name) AND
															UPPER(e1.column_name) = UPPER(c1.column_name))
	 WHERE e1.table_or_view_name NOT IN (
			SELECT e2.table_or_view_name
			   FROM (
					 SELECT CASE table_type WHEN 'BASE TABLE' THEN 'TABLE' ELSE table_type END AS table_type, 
						   UPPER(table_name) AS table_or_view_name
					  FROM information_schema.tables WHERE table_schema IN ('rif40', 'rif_data') AND table_catalog = 'sahsuland_dev'
					EXCEPT
					SELECT table_or_view, UPPER(table_or_view_name_hide) AS table_or_view_name
					  FROM rif40.rif40_tables_and_views
				) AS e2
			  ) /* Exclude extra tables */
), missing AS (
	SELECT m1.table_or_view_name, 
	       m1.column_name,	
           nullable, 
           oracle_data_type AS data_type
	  FROM (	  
		SELECT UPPER(table_or_view_name_hide) AS table_or_view_name,
			   UPPER(column_name_hide) AS column_name
		  FROM rif40.rif40_columns	
		EXCEPT
		SELECT UPPER(table_name) AS table_or_view_name,
			   UPPER(column_name) AS column_name
		  FROM information_schema.columns	
		  ) AS m1
		  LEFT OUTER JOIN rif40.rif40_columns c2 ON (UPPER(m1.table_or_view_name) = UPPER(c2.table_or_view_name_hide) AND
															UPPER(m1.column_name) = UPPER(c2.column_name_hide))  
	 WHERE m1.table_or_view_name NOT IN (
			SELECT m2.table_or_view_name
			   FROM (
					SELECT table_or_view, UPPER(table_or_view_name_hide) AS table_or_view_name
					  FROM rif40.rif40_tables_and_views
					EXCEPT
					SELECT CASE table_type WHEN 'BASE TABLE' THEN 'TABLE' ELSE table_type END AS table_type, 
						   UPPER(table_name) AS table_or_view_name
					  FROM information_schema.tables WHERE table_schema IN ('rif40', 'rif_data') AND table_catalog = 'sahsuland_dev'
			  ) AS m2
		  ) /* Exclude missing tables */
), results AS (
	SELECT 'Extra' AS type, extra.*
	  FROM extra
	UNION
	SELECT 'Missing' AS type, missing.*
	  FROM missing
)
SELECT results.type, 
       CAST(results.table_or_view_name + '.' + results.column_name AS VARCHAR(61)) AS column_name, 
       CAST(results.nullable AS VARCHAR(10)) AS nullable, 
       CAST(results.data_type AS VARCHAR(50)) AS data_type
  FROM results
ORDER BY 1, 2, 3;
GO

-- Column datatype or nullity change
WITH common AS (
	SELECT UPPER(table_name) AS table_or_view_name,
		   UPPER(column_name) AS column_name,	
		   CASE is_nullable WHEN 'YES' THEN 'NULL' ELSE 'NOT NULL' END AS nullable,
		   data_type
	  FROM information_schema.columns
	INTERSECT
	SELECT UPPER(table_or_view_name_hide) AS table_or_view_name,
		   UPPER(column_name_hide) AS column_name,	
           nullable,      
		   CASE /* Convert Oracle/PGSQL datatypes to MSSQL */
				WHEN oracle_data_type = 'DATE' THEN 'datetime2'
				WHEN oracle_data_type = 'TIMESTAMP(6) WITH TIME ZONE' THEN 'datetimeoffset'
				WHEN SUBSTRING(oracle_data_type, 1, 8) = 'VARCHAR2' THEN 'varchar'
				WHEN SUBSTRING(oracle_data_type, 1, 6) = 'NUMBER' AND 
					SUBSTRING(oracle_data_type, 
						CHARINDEX('(', oracle_data_type, 1)+1, 
						CHARINDEX(')', oracle_data_type, 1)-CHARINDEX('(', oracle_data_type, 1)-1) BETWEEN '1' AND '8' THEN 'int'
				ELSE oracle_data_type
		   END AS data_type
      FROM rif40.rif40_columns	  
), new_type AS (
	SELECT n1.table_or_view_name, 
	       n1.column_name,	
		   CASE c3.is_nullable WHEN 'YES' THEN 'NULL' ELSE 'NOT NULL' END AS mssql_nullable,
		   c3.data_type AS mssql_data_type,
		   c4.nullable AS pgsql_nullable,      
		   CASE /* Convert Oracle/PGSQL datatypes to MSSQL */
				WHEN c4.oracle_data_type = 'DATE' THEN 'datetime2'
				WHEN c4.oracle_data_type = 'TIMESTAMP(6) WITH TIME ZONE' THEN 'datetimeoffset'
				WHEN SUBSTRING(c4.oracle_data_type, 1, 8) = 'VARCHAR2' THEN 'varchar'
				WHEN SUBSTRING(c4.oracle_data_type, 1, 6) = 'NUMBER' AND 
					SUBSTRING(c4.oracle_data_type, 
						CHARINDEX('(', c4.oracle_data_type, 1)+1, 
						CHARINDEX(')', c4.oracle_data_type, 1)-CHARINDEX('(', c4.oracle_data_type, 1)-1) BETWEEN '1' AND '8' THEN 'int'
				ELSE c4.oracle_data_type
		   END AS pgsql_data_type
	  FROM (
		SELECT UPPER(table_name) AS table_or_view_name,
			   UPPER(column_name) AS column_name	
		  FROM information_schema.columns
		 EXCEPT
		SELECT common.table_or_view_name, column_name
		  FROM common
		  ) AS n1
		  LEFT OUTER JOIN information_schema.columns c3 ON (UPPER(n1.table_or_view_name) = UPPER(c3.table_name) AND
															UPPER(n1.column_name) = UPPER(c3.column_name)) 
		  LEFT OUTER JOIN rif40.rif40_columns c4 ON (UPPER(n1.table_or_view_name) = UPPER(c4.table_or_view_name_hide) AND
															UPPER(n1.column_name) = UPPER(c4.column_name_hide)) 
 WHERE c4.column_name_hide IS NOT NULL /* In Postgres DB */
), results AS (
	SELECT 'Datatype change' AS type, new_type.*
	  FROM new_type
)
SELECT CAST(results.table_or_view_name + '.' + results.column_name AS VARCHAR(61)) AS column_name, 
	   CAST(
		   CASE 
				WHEN results.mssql_nullable = results.pgsql_nullable THEN results.mssql_nullable 
				ELSE results.pgsql_nullable + '=>' + results.mssql_nullable 
		   END AS VARCHAR(20)) AS nullable_change, 
       CAST(
		   CASE 
				WHEN results.mssql_data_type = results.pgsql_data_type THEN results.mssql_data_type 
				ELSE results.pgsql_data_type + ' to ' + results.mssql_data_type
		   END AS VARCHAR(40)) AS data_type_change
  FROM results
ORDER BY 1, 2, 3;
GO


--
-- Eof
