USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_fdw_tables]') AND type in (N'V'))
BEGIN
	DROP VIEW [rif40].[rif40_fdw_tables]
END
GO

CREATE VIEW [rif40].[rif40_fdw_tables] AS 
SELECT t_rif40_fdw_tables.username,
    t_rif40_fdw_tables.table_name,
    t_rif40_fdw_tables.create_status,
    t_rif40_fdw_tables.error_message,
    t_rif40_fdw_tables.date_created,
    t_rif40_fdw_tables.rowtest_passed
   FROM [rif40].[t_rif40_fdw_tables]
  WHERE t_rif40_fdw_tables.username = SUSER_SNAME()
 GO

GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_fdw_tables] TO [rif_user]
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_fdw_tables] TO [rif_manager]
GO

/*
COMMENT ON VIEW rif40_fdw_tables
  IS 'RIF numerator tables which are Foreign data wrappers in Postgres';
COMMENT ON COLUMN rif40_fdw_tables.username IS 'User name';
COMMENT ON COLUMN rif40_fdw_tables.table_name IS 'RIF table name. Normally the schema owner will not be able to see the health data tables, so no error is raised if the table cannot be resolved to an acceisble object. The schema owner must have access to automatic indirect standardisation denominators.';
COMMENT ON COLUMN rif40_fdw_tables.create_status IS 'Create status: C (Created, no errors), E(Created, errors in test SELECT), N(Not created, errors)';
COMMENT ON COLUMN rif40_fdw_tables.error_message IS 'Error message when create status is: E(Created, errors in test SELECT, N(Not created, errors)';
COMMENT ON COLUMN rif40_fdw_tables.date_created IS 'Date FDW table created (or attempted to be)';
COMMENT ON COLUMN rif40_fdw_tables.rowtest_passed IS 'SELECT rowtest passed (0/1)';
*/
