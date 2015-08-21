
/*
BULK
INSERT [rif_data].[sahsuland_level1]
FROM '$(path)\Postgres\sahsuland\data\sahsuland_level1.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '\n'
)
GO

BULK
INSERT [rif_data].[sahsuland_level2]
FROM '$(path)\Postgres\sahsuland\data\sahsuland_level2.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '\n'
)
GO

BULK
INSERT [rif_data].[sahsuland_level3]
FROM '$(path)\Postgres\sahsuland\data\sahsuland_level3.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '\n'
)
GO

BULK
INSERT [rif_data].[sahsuland_level4]
FROM '$(path)\Postgres\sahsuland\data\sahsuland_level4.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '\n'
)
GO

BULK
INSERT [rif_data].[sahsuland_covariates_level3]
FROM '$(path)\Postgres\sahsuland\data\sahsuland_covariates_level3.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '\n'
)
GO

BULK
INSERT [rif_data].[sahsuland_covariates_level4]
FROM '$(path)\Postgres\sahsuland\data\sahsuland_covariates_level4.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '\n'
)
GO

BULK
INSERT [rif_data].[sahsuland_geography]
FROM '$(path)\Postgres\sahsuland\data\sahsuland_geography.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '\n'
)
GO

BULK
INSERT [rif_data].[sahsuland_cancer_import]
FROM '$(path)\Postgres\sahsuland\data\sahsuland_cancer.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '\n'
)
GO

BULK
INSERT [rif_data].[sahsuland_pop_import]
FROM '$(path)\Postgres\sahsuland\data\sahsuland_pop.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '\n'
)
GO
*/

/*
BULK
INSERT [rif40].[rif40_version]
FROM '$(path)\Postgres\sahsuland\data\rif40_version.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '\n'
)
GO

BULK
INSERT [rif40].[rif40_age_group_names]
FROM '$(path)\Postgres\sahsuland\data\rif40_age_group_names.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '\n'
)
GO

BULK
INSERT [rif40].[rif40_age_groups]
FROM '$(path)\Postgres\sahsuland\data\rif40_age_groups.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '\n'
)
GO

BULK
INSERT [rif40].[rif40_reference_tables]
FROM '$(path)\Postgres\sahsuland\data\rif40_reference_tables.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '\n'
)
GO

BULK
INSERT [rif40].[rif40_outcomes]
FROM '$(path)\Postgres\sahsuland\data\rif40_outcomes.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '\n'
)
GO

BULK
INSERT [rif40].[rif40_health_study_themes]
FROM '$(path)\Postgres\sahsuland\data\rif40_health_study_themes.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '\n'
)
GO

BULK
INSERT [rif40].[rif40_geographies]
FROM '$(path)\Postgres\sahsuland\data\rif40_geographies.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '\n'
)
GO
*/
/*
BULK
INSERT [rif40].[t_rif40_geolevels]
FROM '$(path)\Postgres\sahsuland\data\t_rif40_geolevels.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '\n'
)
GO
*/

/*
BULK
INSERT [rif40].[rif40_tables]
FROM '$(path)\Postgres\sahsuland\data\rif40_tables.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '\n'
)
BULK
INSERT [rif40].[rif40_columns]
FROM '$(path)\Postgres\sahsuland\data\rif40_columns.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '\n'
)
BULK
INSERT [rif40].[rif40_covariates]
FROM '$(path)\Postgres\sahsuland\data\rif40_covariates.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '\n'
)
BULK
INSERT [rif40].[rif40_outcome_groups]
FROM '$(path)\Postgres\sahsuland\data\rif40_outcome_groups.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '\n'
)
BULK
INSERT [rif40].[rif40_table_outcomes]
FROM '$(path)\Postgres\sahsuland\data\rif40_table_outcomes.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '\n'
)
BULK
INSERT [rif40].[t_rif40_fdw_tables]
FROM '$(path)\Postgres\sahsuland\data\t_rif40_fdw_tables.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '\n'
)
BULK
INSERT [rif40].[t_rif40_parameters]
FROM '$(path)\Postgres\sahsuland\data\t_rif40_parameters.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '\n'
)
*/

BULK
INSERT [rif40].[rif40_tables_and_views]
FROM '$(path)\Postgres\sahsuland\data\rif40_tables_and_views.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '\n'
)


--csv needs to be preprocessed:
/*
BULK
INSERT [rif40].[rif40_triggers]
FROM '$(path)\Postgres\sahsuland\data\rif40_triggers.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '\n'
)
BULK
INSERT [rif40].[rif40_predefined_groups]
FROM '$(path)\Postgres\sahsuland\data\rif40_predefined_groups.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '\n'
)
*/