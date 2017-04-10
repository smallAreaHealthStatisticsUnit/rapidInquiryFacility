-- ************************************************************************
--
-- Description:
--
-- Rapid Enquiry Facility (RIF) - RIF40 user objects
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
BEGIN
	DECLARE @mydb VARCHAR(30) = db_name();
	IF @mydb = 'test' BEGIN
		RAISERROR('Cannot run RIF user objects creation on DB: %s', 16, 1, @mydb);
	END;
END;
	
BEGIN
	IF EXISTS (SELECT name FROM sys.schemas WHERE name = N'$(NEWUSER)')
		DROP SCHEMA [$(NEWUSER)];
	IF EXISTS (SELECT name FROM sys.database_principals WHERE name = N'$(NEWUSER)')
		DROP USER [$(NEWUSER)];

	CREATE USER [$(NEWUSER)] FOR LOGIN [$(NEWUSER)] WITH DEFAULT_SCHEMA=[dbo];
--
-- Object privilege grants
--
	GRANT CREATE FUNCTION TO [$(NEWUSER)];
	GRANT CREATE PROCEDURE TO [$(NEWUSER)];
	GRANT CREATE TABLE TO [$(NEWUSER)];
	GRANT CREATE VIEW TO [$(NEWUSER)];
--
	EXEC('CREATE SCHEMA [$(NEWUSER)] AUTHORIZATION [$(NEWUSER)]');
	ALTER USER [$(NEWUSER)] WITH DEFAULT_SCHEMA=[$(NEWUSER)];
	ALTER ROLE rif_user ADD MEMBER [$(NEWUSER)];
	ALTER ROLE rif_manager ADD MEMBER [$(NEWUSER)];	
END;
GO

SELECT name, type_desc FROM sys.database_principals WHERE name = N'$(NEWUSER)';
GO
SELECT * FROM sys.schemas WHERE name = N'$(NEWUSER)';
GO

CREATE TABLE [$(NEWUSER)].study_status (
	  study_id 		INTEGER NOT NULL,
	  study_state 	VARCHAR(1) NOT NULL,
	  creation_date TIMESTAMP NOT NULL,
	  ith_update 	INTEGER NOT NULL,
	  message 		VARCHAR(255)
);
GO 

--
-- Create a test object - this will not re-run
--
/*
EXECUTE AS USER = '$(NEWUSER)';
IF OBJECT_ID('test_table', 'U') IS NOT NULL DROP TABLE test_table;
GO
SELECT db_name() AS db_name INTO [$(NEWUSER)].test_table;
SELECT SUSER_NAME(), USER_NAME(); 
GO
 */

--
-- RIF40 num_denom, rif40_num_denom_errors
--
-- needs functions:
--	rif40_is_object_resolvable, OK
--	rif40_num_denom_validate, OK
--	rif40_auto_indirect_checks
--

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[$(NEWUSER)].[rif40_num_denom]') AND type in (N'V'))
BEGIN
	DROP VIEW [$(NEWUSER)].[rif40_num_denom]
END
GO

CREATE VIEW [$(NEWUSER)].[rif40_num_denom] AS 
 WITH n AS (
         SELECT n1.geography,
            n1.numerator_table,
            n1.numerator_description,
            n1.automatic,
            n1.theme_description
           FROM ( SELECT g.geography,
                    n_1.table_name AS numerator_table,
                    n_1.description AS numerator_description,
                    n_1.automatic,
                    t.description AS theme_description
                   FROM [rif40].[rif40_geographies] g,
                        [rif40].[rif40_tables] n_1,
                        [rif40].[rif40_health_study_themes] t
                  WHERE n_1.isnumerator = 1 AND n_1.automatic = 1
  				    AND [rif40].[rif40_is_object_resolvable](n_1.table_name) = 1
					AND n_1.theme = t.theme) n1
          WHERE [rif40].[rif40_num_denom_validate](n1.geography, n1.numerator_table) = 1
        ), d AS (
         SELECT d1.geography,
            d1.denominator_table,
            d1.denominator_description
           FROM ( SELECT g.geography,
                    d_1.table_name AS denominator_table,
                    d_1.description AS denominator_description
                   FROM [rif40].[rif40_geographies] g,
                        [rif40].[rif40_tables] d_1
                  WHERE d_1.isindirectdenominator = 1
  				    AND d_1.automatic = 1
					AND [rif40].[rif40_is_object_resolvable](d_1.table_name) = 1) d1
          WHERE [rif40].[rif40_num_denom_validate](d1.geography, d1.denominator_table) = 1 
		    AND [rif40].[rif40_auto_indirect_checks](d1.denominator_table) IS NULL
        )
 SELECT n.geography,
    n.numerator_table,
    n.numerator_description,
    n.theme_description,
    d.denominator_table,
    d.denominator_description,
    n.automatic
   FROM n,
    d
  WHERE n.geography = d.geography
GO

EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Numerator and indirect standardisation denominator pairs. Use RIF40_NUM_DENOM_ERROR if your numerator and denominator table pair is missing. You must have your own copy of RIF40_NUM_DENOM or you will only see the tables RIF40 has access to. Tables not rejected if the user does not have access or the table does not contain the correct geography geolevel fields.' , 
	@level0type=N'SCHEMA',@level0name=N'$(NEWUSER)', @level1type=N'VIEW', @level1name=N'rif40_num_denom'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Geography', 
	@level0type=N'SCHEMA', @level0name=N'$(NEWUSER)', @level1type=N'VIEW', @level1name=N'rif40_num_denom', 
	@level2type=N'COLUMN',@level2name=N'geography'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Numerator table', 
	@level0type=N'SCHEMA', @level0name=N'$(NEWUSER)', @level1type=N'VIEW', @level1name=N'rif40_num_denom', 
	@level2type=N'COLUMN',@level2name=N'numerator_table'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Numerator table description', 
	@level0type=N'SCHEMA', @level0name=N'$(NEWUSER)', @level1type=N'VIEW', @level1name=N'rif40_num_denom', 
	@level2type=N'COLUMN',@level2name=N'numerator_description'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Numerator table health study theme description', 
	@level0type=N'SCHEMA', @level0name=N'$(NEWUSER)', @level1type=N'VIEW', @level1name=N'rif40_num_denom', 
	@level2type=N'COLUMN',@level2name=N'theme_description'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Denominator table', 
	@level0type=N'SCHEMA', @level0name=N'$(NEWUSER)', @level1type=N'VIEW', @level1name=N'rif40_num_denom', 
	@level2type=N'COLUMN',@level2name=N'denominator_table'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Denominator table description', 
	@level0type=N'SCHEMA', @level0name=N'$(NEWUSER)', @level1type=N'VIEW', @level1name=N'rif40_num_denom', 
	@level2type=N'COLUMN',@level2name=N'denominator_description'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Is the pair automatic (0/1). Cannot be applied to direct standardisation denominator. Restricted to 1 denominator per geography. The default in RIF40_TABLES is 0 because of the restrictions.' , 
	@level0type=N'SCHEMA',@level0name=N'$(NEWUSER)', @level1type=N'VIEW', @level1name=N'rif40_num_denom', 
	@level2type=N'COLUMN',@level2name=N'automatic'
GO

--
-- Eof