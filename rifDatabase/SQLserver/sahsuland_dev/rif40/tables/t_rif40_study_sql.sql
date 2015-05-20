USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_study_sql]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[t_rif40_study_sql]
END
GO

CREATE TABLE [rif40].[t_rif40_study_sql](
	[username] [varchar](90) NOT NULL DEFAULT (user_name()),
	[study_id] [numeric](8, 0) NOT NULL,
	[statement_type] [varchar](30) NOT NULL,
	[statement_number] [numeric](6, 0) NOT NULL,
	[sql_text] [varchar](4000) NOT NULL,
	[line_number] [numeric](6, 0) NOT NULL,
	[status] [varchar](1) NULL,
 CONSTRAINT [t_rif40_study_sql_pk] PRIMARY KEY CLUSTERED 
(
	[study_id] ASC,
	[statement_number] ASC,
	[line_number] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
CONSTRAINT [t_rif40_study_sql_sid_line_fk] FOREIGN KEY([study_id], [statement_number])
	REFERENCES [rif40].[t_rif40_study_sql_log] ([study_id], [statement_number])
	ON UPDATE NO ACTION ON DELETE NO ACTION,
CONSTRAINT [statement_type_ck2] CHECK  
	(([statement_type]='DENOMINATOR_CHECK' OR [statement_type]='NUMERATOR_CHECK' OR [statement_type]='POST_INSERT' OR [statement_type]='INSERT' OR [statement_type]='CREATE'))
) ON [PRIMARY]
GO

GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[t_rif40_study_sql] TO [rif_user]
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[t_rif40_study_sql] TO [rif_manager]
GO

/*
COMMENT ON TABLE t_rif40_study_sql
  IS 'SQL created for study execution.';
COMMENT ON COLUMN t_rif40_study_sql.username IS 'Username';
COMMENT ON COLUMN t_rif40_study_sql.study_id IS 'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq';
COMMENT ON COLUMN t_rif40_study_sql.statement_type IS 'Statement type: CREATE, INSERT, POST_INSERT, NUMERATOR_CHECK, DENOMINATOR_CHECK';
COMMENT ON COLUMN t_rif40_study_sql.statement_number IS 'Statement number';
COMMENT ON COLUMN t_rif40_study_sql.sql_text IS 'SQL text';
COMMENT ON COLUMN t_rif40_study_sql.line_number IS 'Line number';
COMMENT ON COLUMN t_rif40_study_sql.status IS 'Status: C(reated) or R(un)';
*/

CREATE INDEX t_rif40_study_sql_type_bm
  ON [rif40].[t_rif40_study_sql](statement_type)
GO
  
CREATE INDEX t_rif40_study_sql_uname_bm
  ON [rif40].[t_rif40_study_sql](username)
GO

--trigger MISSING