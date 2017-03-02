
--drop table if exists
IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_num_denom]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[t_rif40_num_denom]
END
GO

--table definition
CREATE TABLE [rif40].[t_rif40_num_denom](
	[geography] [varchar](50) NOT NULL,
	[numerator_table] [varchar](30) NOT NULL,
	[denominator_table] [varchar](30) NOT NULL,
) ON [PRIMARY]
GO

--comments
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Private copy of extra numerator and denominator pairs not added automatically.' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_num_denom'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Geography (e.g EW2001)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_num_denom', @level2type=N'COLUMN',@level2name=N'geography'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Numerator table', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_num_denom', @level2type=N'COLUMN',@level2name=N'numerator_table'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Denominator table', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_num_denom', @level2type=N'COLUMN',@level2name=N'denominator_table'
GO

--index
CREATE UNIQUE INDEX rif40_num_denom_pk
  ON [rif40].[t_rif40_num_denom](geography)
GO
