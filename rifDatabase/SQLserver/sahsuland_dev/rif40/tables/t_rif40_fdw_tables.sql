USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_fdw_tables]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[t_rif40_fdw_tables]
END
GO

CREATE TABLE [rif40].[t_rif40_fdw_tables](
	[username] [varchar](90) NOT NULL DEFAULT (user_name()),
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

GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[t_rif40_fdw_tables] TO [rif_user]
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[t_rif40_fdw_tables] TO [rif_manager]
GO
GRANT SELECT, REFERENCES ON [rif40].[t_rif40_fdw_tables] TO public
GO

/*
COMMENT ON TABLE t_rif40_fdw_tables
  IS 'RIF numerator tables which are Foreign data wrappers in Postgres';
COMMENT ON COLUMN t_rif40_fdw_tables.username IS 'User name';
COMMENT ON COLUMN t_rif40_fdw_tables.table_name IS 'RIF table name. Normally the schema owner will not be able to see the health data tables, so no error is raised if the table cannot be resolved to an acceisble object. The schema owner must have access to automatic indirect standardisation denominators.';
COMMENT ON COLUMN t_rif40_fdw_tables.create_status IS 'Create status: C (Created, no errors), E(Created, errors in test SELECT), N(Not created, errors)';
COMMENT ON COLUMN t_rif40_fdw_tables.error_message IS 'Error message when create status is: E(Created, errors in test SELECT, N(Not created, errors)';
COMMENT ON COLUMN t_rif40_fdw_tables.date_created IS 'Date FDW table created (or attempted to be)';
COMMENT ON COLUMN t_rif40_fdw_tables.rowtest_passed IS 'SELECT rowtest passed (0/1)';
*/

