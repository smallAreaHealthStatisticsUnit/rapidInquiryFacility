USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_inv_conditions]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[t_rif40_inv_conditions]
END
GO

CREATE TABLE [rif40].[t_rif40_inv_conditions](
	[inv_id] [numeric](8, 0) NOT NULL,
	[study_id] [numeric](8, 0) NOT NULL,
	[username] [varchar](90) NOT NULL DEFAULT (user_name()),
	[line_number] [numeric](5, 0) NOT NULL DEFAULT ((1)),
	[condition] [varchar](4000) NOT NULL DEFAULT ('1=1'),
	[rowid] [uniqueidentifier] NOT NULL DEFAULT (newid()),
 CONSTRAINT [t_rif40_inv_conditions_pk] PRIMARY KEY CLUSTERED 
(
	[study_id] ASC,
	[inv_id] ASC,
	[line_number] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
CONSTRAINT [t_rif40_inv_conditions_si_fk] FOREIGN KEY([study_id], [inv_id])
	REFERENCES [rif40].[t_rif40_investigations] ([study_id], [inv_id])
	ON UPDATE NO ACTION ON DELETE NO ACTION
) ON [PRIMARY]
GO

GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[t_rif40_inv_conditions] TO [rif_user]
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[t_rif40_inv_conditions] TO [rif_manager]
GO

/*
COMMENT ON TABLE t_rif40_inv_conditions
  IS 'Lines of SQL conditions pertinent to an investigation.';
COMMENT ON COLUMN t_rif40_inv_conditions.inv_id IS 'Unique investigation index: inv_id. Created by SEQUENCE rif40_inv_id_seq';
COMMENT ON COLUMN t_rif40_inv_conditions.study_id IS 'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq';
COMMENT ON COLUMN t_rif40_inv_conditions.username IS 'Username';
COMMENT ON COLUMN t_rif40_inv_conditions.line_number IS 'Line number';
COMMENT ON COLUMN t_rif40_inv_conditions.condition IS 'SQL WHERE clause, with the WHERE keyword omitted). Default to 1=1 (use all records matching year/age/sex criteria). Checked for SQL injection.';
*/

--trigger: WHERE IS IT?