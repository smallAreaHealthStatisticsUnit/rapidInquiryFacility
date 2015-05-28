USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_age_groups]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[rif40_age_groups]
END
GO

CREATE TABLE [rif40].[rif40_age_groups](
	[age_group_id] [numeric](3, 0) NOT NULL,
	[offset] [numeric](3, 0) NOT NULL,
	[low_age] [numeric](3, 0) NOT NULL,
	[high_age] [numeric](3, 0) NOT NULL,
	[fieldname] [varchar](50) NOT NULL,
 CONSTRAINT [rif40_age_groups_pk] PRIMARY KEY CLUSTERED 
(
	[age_group_id] ASC,
	[offset] ASC
) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
CONSTRAINT [rif40_age_group_id_fk] FOREIGN KEY([age_group_id])
 REFERENCES [rif40].[rif40_age_group_names] ([age_group_id])
     ON UPDATE NO ACTION ON DELETE NO ACTION
) ON [PRIMARY]
GO

GRANT SELECT, REFERENCES ON [rif40].[rif40_age_groups] TO public
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_age_groups] TO [rif_manager]
GO

CREATE UNIQUE INDEX [rif40_age_groups_pk2]
ON [rif40].[rif40_age_groups] (age_group_id)
GO

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'RIF age groups' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_age_groups'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'AGE_GROUP_ID', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_age_groups', @level2type=N'COLUMN',@level2name=N'age_group_id'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Offset', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_age_groups', @level2type=N'COLUMN',@level2name=N'offset'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Low age', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_age_groups', @level2type=N'COLUMN',@level2name=N'low_age'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'High age', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_age_groups', @level2type=N'COLUMN',@level2name=N'high_age'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Fieldname', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_age_groups', @level2type=N'COLUMN',@level2name=N'fieldname'
GO
