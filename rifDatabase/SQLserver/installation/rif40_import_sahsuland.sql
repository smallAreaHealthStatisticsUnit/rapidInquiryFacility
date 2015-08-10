BULK
INSERT CSVTest
FROM '..\..\Postgres\sahsuland\data\rif40_version.csv'
WITH
(
FIELDTERMINATOR = ',',
ROWTERMINATOR = '\n'
)
GO
