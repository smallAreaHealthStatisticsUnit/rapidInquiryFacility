USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_parameters]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[t_rif40_parameters]
END
GO

CREATE TABLE [rif40].[t_rif40_parameters](
	[param_name] [varchar](30) NOT NULL,
	[param_value] [varchar](50) NOT NULL,
	[param_description] [varchar](250) NOT NULL,
 CONSTRAINT [t_rif40_parameters_pk] PRIMARY KEY CLUSTERED 
(
	[param_name] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[t_rif40_parameters] TO [rif_manager]
GO
GRANT SELECT ON [rif40].[t_rif40_parameters] TO public
GO

/*
COMMENT ON TABLE t_rif40_parameters
  IS 'RIF40 parameters. Use this table for INSERT/UPDATE/DELETE; use RIF40_PARAMETERS for SELECT. User needs RIF_NO_SUPPRESSION granted as a role to see unsuppressed results';
COMMENT ON COLUMN t_rif40_parameters.param_name IS 'Parameter';
COMMENT ON COLUMN t_rif40_parameters.param_value IS 'Value';
COMMENT ON COLUMN t_rif40_parameters.param_description IS 'Description';
*/
