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
INSERT [rif40].[t_rif40_projects]
FROM '$(path)\Postgres\sahsuland\data\t_rif40_projects.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '\n'
)
GO

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
*/

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
