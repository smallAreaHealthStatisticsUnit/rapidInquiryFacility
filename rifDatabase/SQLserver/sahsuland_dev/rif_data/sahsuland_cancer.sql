USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif_data].[sahsuland_cancer]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif_data].[sahsuland_cancer]
END
GO

CREATE TABLE [rif_data].[sahsuland_cancer]
(
  year smallint NOT NULL, -- Year
  age_sex_group smallint NOT NULL, -- Age sex group
  level1 varchar(20) NOT NULL, -- level1
  level2 varchar(20) NOT NULL, -- level2
  level3 varchar(20) NOT NULL, -- level3
  level4 varchar(20) NOT NULL, -- level4
  icd varchar(4) NOT NULL, -- ICD
  total float NOT NULL, -- Total
  age_group as age_sex_group % 100,
  sex as round(age_sex_group/100,1,1) -- truncates instead of rounds
);

GRANT SELECT ON [rif_data].[sahsuland_cancer] TO [rif_user];
GRANT SELECT ON [rif_data].[sahsuland_cancer] TO [rif_manager];

--comments
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'SAHSU land cancer' , @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_cancer'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Year', @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_cancer', @level2type=N'COLUMN',@level2name=N'year'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Age sex group', @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_cancer', @level2type=N'COLUMN',@level2name=N'age_sex_group'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'level1', @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_cancer', @level2type=N'COLUMN',@level2name=N'level1'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'level2', @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_cancer', @level2type=N'COLUMN',@level2name=N'level2'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'level3', @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_cancer', @level2type=N'COLUMN',@level2name=N'level3'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'level4', @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_cancer', @level2type=N'COLUMN',@level2name=N'level4'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'ICD', @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_cancer', @level2type=N'COLUMN',@level2name=N'icd'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Total', @level0type=N'SCHEMA',@level0name=N'rif_data', @level1type=N'TABLE',@level1name=N'sahsuland_cancer', @level2type=N'COLUMN',@level2name=N'total'
GO


--indexes
CREATE INDEX sahsuland_cancer_age_group
  ON rif_data.sahsuland_cancer(age_group);

CREATE INDEX sahsuland_cancer_age_sex_group
  ON rif_data.sahsuland_cancer(age_sex_group);

CREATE INDEX sahsuland_cancer_icd
  ON rif_data.sahsuland_cancer(icd);
  
CREATE INDEX sahsuland_cancer_level1
  ON rif_data.sahsuland_cancer(level1);

CREATE INDEX sahsuland_cancer_level2
  ON rif_data.sahsuland_cancer(level2);
  
CREATE INDEX sahsuland_cancer_level3
  ON rif_data.sahsuland_cancer(level3);

CREATE INDEX sahsuland_cancer_level4
  ON rif_data.sahsuland_cancer(level4);
  
CREATE UNIQUE INDEX sahsuland_cancer_pk
  ON rif_data.sahsuland_cancer(year, level4, age_sex_group,icd);
  
CREATE INDEX sahsuland_cancer_sex
  ON rif_data.sahsuland_cancer(sex);
  
CREATE INDEX sahsuland_cancer_year
  ON rif_data.sahsuland_cancer(year);
  
--needed for importing data with computed columns
 IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif_data].[sahsuland_cancer_import]') AND type in (N'V'))
BEGIN
	DROP VIEW [rif_data].[sahsuland_cancer_import]
END
GO

CREATE VIEW [rif_data].[sahsuland_cancer_import] AS 
select year, age_sex_group, level1, level2, level3, level4, icd, total
from [rif_data].[sahsuland_cancer];
GO
