
IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_age_group_names]') AND type in (N'U'))
BEGIN

	--first disable foreign key references
	IF EXISTS (SELECT * FROM sys.objects 
		WHERE object_id = OBJECT_ID(N'[rif40].[rif40_age_groups]') AND type in (N'U'))
		AND EXISTS (SELECT * FROM sys.foreign_keys 
		WHERE name='rif40_age_group_id_fk')
	BEGIN
		ALTER TABLE [rif40].[rif40_age_groups] DROP CONSTRAINT [rif40_age_group_id_fk];
	END;
	IF EXISTS (SELECT * FROM sys.objects 
		WHERE object_id = OBJECT_ID(N'[rif40].[rif40_tables]') AND type in (N'U'))
		AND EXISTS (SELECT * FROM sys.foreign_keys 
		WHERE name='rif40_tables_age_group_id_fk')
	BEGIN
		ALTER TABLE [rif40].[rif40_tables] DROP CONSTRAINT [rif40_tables_age_group_id_fk];
	END;

	DROP TABLE [rif40].[rif40_age_group_names]
END
GO

CREATE TABLE [rif40].[rif40_age_group_names](
	[age_group_id] [numeric](3, 0) NOT NULL,
	[age_group_name] [varchar](50) NOT NULL,
 CONSTRAINT [rif40_age_group_names_pk] PRIMARY KEY CLUSTERED 
(
	[age_group_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO


--recreate foreign key references
IF EXISTS (SELECT * FROM sys.objects 
	WHERE object_id = OBJECT_ID(N'[rif40].[rif40_age_groups]') AND type in (N'U'))
BEGIN
	ALTER TABLE [rif40].[rif40_age_groups]  WITH CHECK ADD  
	CONSTRAINT [rif40_age_group_id_fk] FOREIGN KEY([age_group_id])
	REFERENCES [rif40].[rif40_age_group_names] ([age_group_id])
	ON UPDATE NO ACTION ON DELETE NO ACTION;
END
GO
IF EXISTS (SELECT * FROM sys.objects 
	WHERE object_id = OBJECT_ID(N'[rif40].[rif40_tables]') AND type in (N'U'))
BEGIN
	ALTER TABLE [rif40].[rif40_tables]  WITH CHECK ADD  
	CONSTRAINT [rif40_tables_age_group_id_fk] FOREIGN KEY([age_group_id])
	REFERENCES [rif40].[rif40_age_group_names] ([age_group_id])
	ON UPDATE NO ACTION ON DELETE NO ACTION;
END
GO


GRANT SELECT, REFERENCES ON  [rif40].[rif40_age_group_names] TO public
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_age_group_names] TO [rif_manager]
GO

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'RIF Age group names' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_age_group_names'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'AGE_GROUP_ID', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_age_group_names', @level2type=N'COLUMN',@level2name=N'age_group_id'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Age Group Name', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_age_group_names', @level2type=N'COLUMN',@level2name=N'age_group_name'
GO
