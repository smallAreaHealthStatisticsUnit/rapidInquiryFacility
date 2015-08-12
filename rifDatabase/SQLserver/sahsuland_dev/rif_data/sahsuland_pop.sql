USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif_data].[sahsuland_pop]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif_data].[sahsuland_pop]
END
GO

CREATE TABLE [rif_data].[sahsuland_pop]
(
  year smallint NOT NULL, -- Year
  age_sex_group smallint NOT NULL, -- Age sex group
  level1 varchar(20) NOT NULL, -- level1
  level2 varchar(20) NOT NULL, -- level2
  level3 varchar(20) NOT NULL, -- level3
  level4 varchar(20) NOT NULL, -- level4
  total float NOT NULL, -- Total
  age_group as age_sex_group % 100,
  sex as round(age_sex_group/100,1,1) -- truncates instead of rounds
);

GRANT SELECT ON [rif_data].[sahsuland_pop] TO [rif_user];
GRANT SELECT ON [rif_data].[sahsuland_pop] TO [rif_manager];

--comments
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'SAHSU land population' , @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_pop'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Year', @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_pop', @level2type=N'COLUMN',@level2name=N'year'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Age sex group', @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_pop', @level2type=N'COLUMN',@level2name=N'age_sex_group'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'level1', @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_pop', @level2type=N'COLUMN',@level2name=N'level1'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'level2', @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_pop', @level2type=N'COLUMN',@level2name=N'level2'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'level3', @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_pop', @level2type=N'COLUMN',@level2name=N'level3'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'level4', @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_pop', @level2type=N'COLUMN',@level2name=N'level4'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Total', @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_pop', @level2type=N'COLUMN',@level2name=N'total'
GO

--indexes
CREATE INDEX sahsuland_pop_age_group
  ON rif_data.sahsuland_pop(age_group);

CREATE INDEX sahsuland_pop_age_sex_group
  ON rif_data.sahsuland_pop(age_sex_group);

CREATE INDEX sahsuland_pop_level1
  ON rif_data.sahsuland_pop(level1);

CREATE INDEX sahsuland_pop_level2
  ON rif_data.sahsuland_pop(level2);

CREATE INDEX sahsuland_pop_level3
  ON rif_data.sahsuland_pop(level3);

CREATE INDEX sahsuland_pop_level4
  ON rif_data.sahsuland_pop(level4);

CREATE UNIQUE INDEX sahsuland_pop_pk
  ON rif_data.sahsuland_pop (year, level4, age_sex_group);

CREATE INDEX sahsuland_pop_sex
  ON rif_data.sahsuland_pop(sex);

CREATE INDEX sahsuland_pop_year
  ON rif_data.sahsuland_pop(year);
  
--needed for importing data with computed columns
 IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif_data].[sahsuland_pop_import]') AND type in (N'V'))
BEGIN
	DROP VIEW [rif_data].[sahsuland_pop_import]
END
GO

CREATE VIEW [rif_data].[sahsuland_pop_import] AS 
select year, age_sex_group, level1, level2, level3, level4, total
from [rif_data].[sahsuland_pop];

