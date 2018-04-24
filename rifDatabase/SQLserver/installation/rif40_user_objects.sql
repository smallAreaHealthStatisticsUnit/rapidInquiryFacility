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
	IF NOT EXISTS (SELECT name FROM sys.database_principals WHERE name = N'$(NEWUSER)')
	CREATE USER [$(NEWUSER)] FOR LOGIN [$(NEWUSER)] WITH DEFAULT_SCHEMA=[dbo]
	ELSE ALTER USER [$(NEWUSER)] WITH LOGIN=[$(NEWUSER)];
	
--
-- Object privilege grants
--
	GRANT CREATE FUNCTION TO [$(NEWUSER)];
	GRANT CREATE PROCEDURE TO [$(NEWUSER)];
	GRANT CREATE TABLE TO [$(NEWUSER)];
	GRANT CREATE VIEW TO [$(NEWUSER)];
--
-- Allow SHOWPLAN
--
	GRANT SHOWPLAN TO [$(NEWUSER)];
--
	IF NOT EXISTS (SELECT name FROM sys.schemas WHERE name = N'$(NEWUSER)')
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

--
-- Save sequence in current valid sequences object for later use by
-- CURRVAL function: [rif40].[rif40_sequence_current_value]()
--
IF (OBJECT_ID('tempdb..##t_rif40_studies_seq') IS NOT NULL)
	DROP TABLE ##t_rif40_studies_seq;
GO
CREATE TABLE ##t_rif40_studies_seq (
	study_id INTEGER NOT NULL
);
GO
IF (OBJECT_ID('tempdb..##t_rif40_investigations_seq') IS NOT NULL)
	DROP TABLE ##t_rif40_investigations_seq;
GO
CREATE TABLE ##t_rif40_investigations_seq (
	inv_id INTEGER NOT NULL
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

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[$(NEWUSER)].[rif40_num_denom_errors]') AND type in (N'V'))
BEGIN
	DROP VIEW [$(NEWUSER)].[rif40_num_denom_errors]
END
GO

CREATE VIEW [$(NEWUSER)].[rif40_num_denom_errors] AS 
 WITH n AS (
         SELECT n1.geography,
            n1.numerator_table,
            n1.numerator_description,
            n1.automatic,
            n1.is_object_resolvable,
            n1.n_num_denom_validated,
            n1.numerator_owner
           FROM ( SELECT g.geography,
                    n_1.table_name AS numerator_table,
                    n_1.description AS numerator_description,
                    n_1.automatic,
                    [rif40].[rif40_is_object_resolvable](n_1.table_name) AS is_object_resolvable,
                    [rif40].[rif40_num_denom_validate](g.geography, n_1.table_name) AS n_num_denom_validated,
                    [rif40].[rif40_object_resolve](n_1.table_name) AS numerator_owner
                   FROM [rif40].[rif40_geographies] g,
                    [rif40].[rif40_tables] n_1
                  WHERE n_1.isnumerator = 1 AND n_1.automatic = 1) n1
        ), d AS (
         SELECT d1.geography,
            d1.denominator_table,
            d1.denominator_description,
            d1.is_object_resolvable,
            d1.d_num_denom_validated,
            d1.denominator_owner,
            [rif40].[rif40_auto_indirect_checks](d1.denominator_table) AS auto_indirect_error
           FROM ( SELECT g.geography,
                    d_1.table_name AS denominator_table,
                    d_1.description AS denominator_description,
                    [rif40].[rif40_is_object_resolvable](d_1.table_name) AS is_object_resolvable,
                    [rif40].[rif40_num_denom_validate](g.geography, d_1.table_name) AS d_num_denom_validated,
                    [rif40].[rif40_object_resolve](d_1.table_name) AS denominator_owner
                   FROM [rif40].[rif40_geographies] g,
                    [rif40].[rif40_tables] d_1
                  WHERE d_1.isindirectdenominator = 1 AND d_1.automatic = 1) d1
        )
 SELECT n.geography,
    n.numerator_owner,
    n.numerator_table,
    n.is_object_resolvable AS is_numerator_resolvable,
    n.n_num_denom_validated,
    n.numerator_description,
    d.denominator_owner,
    d.denominator_table,
    d.is_object_resolvable AS is_denominator_resolvable,
    d.d_num_denom_validated,
    d.denominator_description,
    n.automatic,
        CASE
            WHEN d.auto_indirect_error IS NULL THEN 0
            ELSE 1
        END AS auto_indirect_error_flag,
    d.auto_indirect_error /*,
    f.create_status AS n_fdw_create_status,
    f.error_message AS n_fdw_error_message,
    f.date_created AS n_fdw_date_created,
    f.rowtest_passed AS n_fdw_rowtest_passed */
   FROM d,
    n
  WHERE n.geography = d.geography;
GO

EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'All possible numerator and indirect standardisation denominator pairs with error diagnostic fields. As this is a CROSS JOIN the will be a lot of output as tables are not rejected on the basis of user access or containing the correct geography geolevel fields.' , 
	@level0type=N'SCHEMA',@level0name=N'$(NEWUSER)', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Geography', 
	@level0type=N'SCHEMA',@level0name=N'$(NEWUSER)', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', 
	@level2type=N'COLUMN',@level2name=N'geography'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Numerator table owner' , 
	@level0type=N'SCHEMA',@level0name=N'$(NEWUSER)', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', 
	@level2type=N'COLUMN',@level2name=N'numerator_owner'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Numerator table' , 
	@level0type=N'SCHEMA',@level0name=N'$(NEWUSER)', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', 
	@level2type=N'COLUMN',@level2name=N'numerator_table'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Is the numerator table resolvable and accessible (0/1)' , 
	@level0type=N'SCHEMA',@level0name=N'$(NEWUSER)', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', 
	@level2type=N'COLUMN',@level2name=N'is_numerator_resolvable'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Is the numerator valid for this geography (0/1). If N_NUM_DENOM_VALIDATED and D_NUM_DENOM_VALIDATED are both 1 then the pair will appear in RIF40_NUM_DENOM.', 
	@level0type=N'SCHEMA',@level0name=N'$(NEWUSER)', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', 
	@level2type=N'COLUMN',@level2name=N'n_num_denom_validated'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Numerator table description', 
	@level0type=N'SCHEMA',@level0name=N'$(NEWUSER)', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', 
	@level2type=N'COLUMN',@level2name=N'numerator_description'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Denominator table owner', 
	@level0type=N'SCHEMA',@level0name=N'$(NEWUSER)', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', 
	@level2type=N'COLUMN',@level2name=N'denominator_owner'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Denominator table', 
	@level0type=N'SCHEMA',@level0name=N'$(NEWUSER)', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', 
	@level2type=N'COLUMN',@level2name=N'denominator_table'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Is the denominator table resolvable and accessible (0/1)', 
	@level0type=N'SCHEMA',@level0name=N'$(NEWUSER)', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', 
	@level2type=N'COLUMN',@level2name=N'is_denominator_resolvable'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Is the denominator valid for this geography (0/1). If N_NUM_DENOM_VALIDATED and D_NUM_DENOM_VALIDATED are both 1 then the pair will appear in RIF40_NUM_DENOM.', 
	@level0type=N'SCHEMA',@level0name=N'$(NEWUSER)', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', 
	@level2type=N'COLUMN',@level2name=N'd_num_denom_validated'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Denominator table description', 
	@level0type=N'SCHEMA',@level0name=N'$(NEWUSER)', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', 
	@level2type=N'COLUMN',@level2name=N'denominator_description'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Is the pair automatic (0/1). Cannot be applied to direct standardisation denominator. Restricted to 1 denominator per geography. The default in RIF40_TABLES is 0 because of the restrictions.', 
	@level0type=N'SCHEMA',@level0name=N'$(NEWUSER)', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', 
	@level2type=N'COLUMN',@level2name=N'automatic'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Error flag 0/1. Denominator table with automatic set to "1" that fails the RIF40_CHECKS.RIF40_AUTO_INDIRECT_CHECKS test. Restricted to 1 denominator per geography to prevent the automatic RIF40_NUM_DENOM having >1 pair per numerator.', 
	@level0type=N'SCHEMA',@level0name=N'$(NEWUSER)', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', 
	@level2type=N'COLUMN',@level2name=N'auto_indirect_error_flag'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'Denominator table with automatic set to "1" that fails the RIF40_CHECKS.RIF40_AUTO_INDIRECT_CHECKS test. Restricted to 1 denominator per geography to prevent the automatic RIF40_NUM_DENOM having >1 pair per numerator. List of geographies and tables in error.', 
	@level0type=N'SCHEMA',@level0name=N'$(NEWUSER)', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', 
	@level2type=N'COLUMN',@level2name=N'auto_indirect_error'
GO

--
-- Fix projects for user, creating if required and possible 
-- Assumes that the first time it is run the install usder has rif_manager privilege
--
BEGIN
	DECLARE @has_rif_manager INTEGER = IS_MEMBER('rif_manager');
	DECLARE @username		 VARCHAR(240) = N'$(NEWUSER)'; 
	IF @username = '$(NEWUSER)' SET @username = USER;
	IF NOT EXISTS (SELECT project FROM rif40.t_rif40_projects WHERE project = 'TEST') 
		BEGIN
			IF @has_rif_manager = 1 OR @username = 'dbo'
				BEGIN
					INSERT INTO rif40.t_rif40_projects (project, description) VALUES ('TEST', 'Test project');
					IF NOT EXISTS (SELECT project FROM rif40.t_rif40_user_projects WHERE project = 'TEST')
						INSERT INTO rif40.t_rif40_user_projects (project, username) VALUES ('TEST', N'$(NEWUSER)');
				END;
			ELSE IF @has_rif_manager IS NULL
				RAISERROR(N'RIF_MANAGER role does not exist', 16, 1);
			ELSE 
				RAISERROR(N'TEST project does not exist, user %s does not have the rif_manager role (%d) to create it.', 16, 1, @username, @has_rif_manager);
		END;
	ELSE IF NOT EXISTS (SELECT project FROM rif40.t_rif40_user_projects WHERE project = 'TEST')
		INSERT INTO rif40.t_rif40_user_projects (project, username) VALUES ('TEST', @username);
	ELSE
		PRINT 'Project TEST is OK for ' + @username;	
END;
GO

/*
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'RIF numerator foreign data wrappers table create status: C (Created, no errors), E(Created, errors in test SELECT), N(Not created, errors).', 
	@level0type=N'SCHEMA',@level0name=N'$(NEWUSER)', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', 
	@level2type=N'COLUMN',@level2name=N'n_fdw_create_status'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'RIF numerator foreign data wrappers table error message when create status is: E(Created, errors in test SELECT, N(Not created, errors).', 
	@level0type=N'SCHEMA',@level0name=N'$(NEWUSER)', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', 
	@level2type=N'COLUMN',@level2name=N'n_fdw_error_message'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'RIF numerator foreign data wrappers table date FDW table created (or attempted to be).', 
	@level0type=N'SCHEMA',@level0name=N'$(NEWUSER)', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', 
	@level2type=N'COLUMN',@level2name=N'n_fdw_date_created'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', 
	@value=N'RIF numerator foreign data wrappers table SELECT rowtest passed (0/1).', 
	@level0type=N'SCHEMA',@level0name=N'$(NEWUSER)', @level1type=N'VIEW',@level1name=N'rif40_num_denom_errors', 
	@level2type=N'COLUMN',@level2name=N'n_fdw_rowtest_passed'
GO
 */

-- 
-- Eof (rif40_user_objects.sql)
