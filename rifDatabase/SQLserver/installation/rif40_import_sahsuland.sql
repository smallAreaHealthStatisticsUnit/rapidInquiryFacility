TRUNCATE TABLE [rif40].[rif40_version]
GO

BULK
INSERT [rif40].[rif40_version]
FROM '$(path)\Postgres\sahsuland\data\rif40_version.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '\n'
)
GO
