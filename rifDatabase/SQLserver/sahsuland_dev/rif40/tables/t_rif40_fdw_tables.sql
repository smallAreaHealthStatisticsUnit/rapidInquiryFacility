
--drop table if exists
IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_fdw_tables]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[t_rif40_fdw_tables]
END
GO

/* FOREIGN DATA WRAPPER TABLES - Postgres only!

--table definition
CREATE TABLE [rif40].[t_rif40_fdw_tables](
	[username] [varchar](90) NOT NULL DEFAULT (SUSER_SNAME()),
	[table_name] [varchar](30) NOT NULL,
	[create_status] [varchar](1) NOT NULL,
	[error_message] [varchar](300) NULL,
	[date_created] [datetime2](0) NOT NULL DEFAULT (sysdatetime()),
	[rowtest_passed] [numeric](1, 0) NOT NULL DEFAULT ((0)),
 CONSTRAINT [t_rif40_fdw_tables_pk] PRIMARY KEY CLUSTERED 
(
	[table_name] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
CONSTRAINT [t_rif40_fdw_tables_tn_fk] FOREIGN KEY([table_name])
	REFERENCES [rif40].[rif40_tables] ([table_name])
	ON UPDATE NO ACTION ON DELETE NO ACTION,
CONSTRAINT [t_rif40_fdw_tables_ck1] CHECK  
	(([create_status]='N' OR [create_status]='E' OR [create_status]='C')),
CONSTRAINT [t_rif40_fdw_tables_ck2] CHECK  
	(([rowtest_passed]=(1) OR [rowtest_passed]=(0)))
) ON [PRIMARY]
GO

--permissions
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[t_rif40_fdw_tables] TO [rif_user]
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[t_rif40_fdw_tables] TO [rif_manager]
GO
GRANT SELECT, REFERENCES ON [rif40].[t_rif40_fdw_tables] TO public
GO

--comments
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'RIF numerator tables which are Foreign data wrappers in SQL Server' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_fdw_tables'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Username', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_fdw_tables', @level2type=N'COLUMN',@level2name=N'username'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'RIF table name. Normally the schema owner will not be able to see the health data tables, so no error is raised if the table cannot be resolved to an acceisble object. The schema owner must have access to automatic indirect standardisation denominators.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_fdw_tables', @level2type=N'COLUMN',@level2name=N'table_name'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Create status: C (Created, no errors), E(Created, errors in test SELECT), N(Not created, errors)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_fdw_tables', @level2type=N'COLUMN',@level2name=N'create_status'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Error message when create status is: E(Created, errors in test SELECT, N(Not created, errors)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_fdw_tables', @level2type=N'COLUMN',@level2name=N'error_message'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Date FDW table created (or attempted to be)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_fdw_tables', @level2type=N'COLUMN',@level2name=N'date_created'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'SELECT rowtest passed (0/1)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_fdw_tables', @level2type=N'COLUMN',@level2name=N'rowtest_passed'
GO
 */
