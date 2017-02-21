--
-- Delete everything; truncate if it has not got foreign keys
-- Disable, delete then enable triggers on rif40_tables, t_rif40_geolevels
--
-- When studies are present they will need to be deleted to.
--

TRUNCATE TABLE [rif_data].[sahsuland_level1];
GO
TRUNCATE TABLE [rif_data].[sahsuland_level2];
GO
TRUNCATE TABLE [rif_data].[sahsuland_level3];
GO
TRUNCATE TABLE [rif_data].[sahsuland_level4];
GO
TRUNCATE TABLE [rif_data].[sahsuland_covariates_level3];
GO
TRUNCATE TABLE [rif_data].[sahsuland_covariates_level4];
GO
TRUNCATE TABLE [rif_data].[sahsuland_geography];
GO
TRUNCATE TABLE [rif40].[rif40_version];
GO
TRUNCATE TABLE [rif40].[rif40_age_groups];
GO
DELETE FROM [rif40].[rif40_predefined_groups];
GO
DELETE FROM [rif40].[rif40_table_outcomes];
GO
DELETE FROM [rif40].[rif40_outcome_groups];
GO
TRUNCATE TABLE [rif40].[rif40_table_outcomes];
GO

IF EXISTS (SELECT *
             FROM sys.objects
		    WHERE object_id = OBJECT_ID(N'[rif40].[tr_rif40_tables_checks]')
		      AND type IN ( N'TR' ))
	DISABLE TRIGGER [rif40].[tr_rif40_tables_checks] ON [rif40].[rif40_tables];
GO
DELETE FROM [rif40].[rif40_tables];
GO
IF EXISTS (SELECT *
             FROM sys.objects
		    WHERE object_id = OBJECT_ID(N'[rif40].[tr_rif40_tables_checks]')
		      AND type IN ( N'TR' ))
	ENABLE TRIGGER [rif40].[tr_rif40_tables_checks] ON [rif40].[rif40_tables];
GO

DELETE FROM [rif40].[rif40_age_group_names];
GO
TRUNCATE TABLE [rif40].[rif40_reference_tables];
GO
DELETE FROM [rif40].[rif40_outcomes];
GO
DELETE FROM [rif40].[rif40_health_study_themes];
GO
DELETE FROM [rif40].[rif40_covariates];
GO

IF EXISTS (SELECT *
             FROM sys.objects
		    WHERE object_id = OBJECT_ID(N'[rif40].[tr_geolevel_check]')
		      AND type IN ( N'TR' ))
	DISABLE TRIGGER [rif40].[tr_geolevel_check] ON [rif40].[t_rif40_geolevels];
GO
DELETE FROM [rif40].[rif40_tables];
GO
DELETE FROM [rif40].[t_rif40_geolevels];
GO
IF EXISTS (SELECT *
             FROM sys.objects
		    WHERE object_id = OBJECT_ID(N'[rif40].[tr_geolevel_check]')
		      AND type IN ( N'TR' ))
	ENABLE TRIGGER [rif40].[tr_geolevel_check] ON [rif40].[t_rif40_geolevels];
GO

DELETE FROM [rif40].[rif40_geographies];
GO
TRUNCATE TABLE [rif40].[t_rif40_parameters];
GO
TRUNCATE TABLE [rif40].[rif40_columns];
GO
TRUNCATE TABLE [rif40].[rif40_triggers];
GO
DELETE FROM [rif40].[rif40_tables_and_views];
GO

TRUNCATE TABLE [rif_data].[sahsuland_cancer];
GO
TRUNCATE TABLE [rif_data].[sahsuland_pop];
GO
