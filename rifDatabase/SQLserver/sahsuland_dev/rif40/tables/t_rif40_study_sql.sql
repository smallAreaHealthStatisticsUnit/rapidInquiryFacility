USE [sahsuland_dev]
GO

--drop table if exists
IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_study_sql]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[t_rif40_study_sql]
END
GO

--table definition
CREATE TABLE [rif40].[t_rif40_study_sql](
	[username] [varchar](90) NOT NULL DEFAULT (SUSER_SNAME()),
	[study_id] [integer] NOT NULL CONSTRAINT t_rif40_study_sql_study_id_seq DEFAULT ([rif40].[rif40_sequence_current_value](N'rif40.rif40_study_id_seq')),
	[statement_type] [varchar](30) NOT NULL,
	[statement_number] [numeric](6, 0) NOT NULL,
	[sql_text] [varchar](4000) NOT NULL,
	[line_number] [integer] NOT NULL,
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

--permissions
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[t_rif40_study_sql] TO [rif_user]
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[t_rif40_study_sql] TO [rif_manager]
GO

--comments
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'SQL created for study execution.' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_study_sql'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Username', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_study_sql', @level2type=N'COLUMN',@level2name=N'username'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_study_sql', @level2type=N'COLUMN',@level2name=N'study_id'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Statement type: CREATE, INSERT, POST_INSERT, NUMERATOR_CHECK, DENOMINATOR_CHECK', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_study_sql', @level2type=N'COLUMN',@level2name=N'statement_type'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Statement number', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_study_sql', @level2type=N'COLUMN',@level2name=N'statement_number'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'SQL text', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_study_sql', @level2type=N'COLUMN',@level2name=N'sql_text'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Line number', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_study_sql', @level2type=N'COLUMN',@level2name=N'line_number'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Status: C(reated) or R(un)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_study_sql', @level2type=N'COLUMN',@level2name=N'status'
GO

--indices
CREATE INDEX t_rif40_study_sql_type_bm
  ON [rif40].[t_rif40_study_sql](statement_type)
GO
  
CREATE INDEX t_rif40_study_sql_uname_bm
  ON [rif40].[t_rif40_study_sql](username)
GO
