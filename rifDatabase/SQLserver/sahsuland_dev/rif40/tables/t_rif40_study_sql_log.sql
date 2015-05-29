USE [sahsuland_dev]
GO

--drop table if exists
IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_study_sql_log]') AND type in (N'U'))
BEGIN
	--first disable foreign key references
	IF EXISTS (SELECT * FROM sys.objects 
		WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_study_sql]') AND type in (N'U'))
		AND EXISTS (SELECT * FROM sys.foreign_keys 
		WHERE name='t_rif40_study_sql_sid_line_fk')
	BEGIN
		ALTER TABLE [rif40].[t_rif40_study_sql] DROP CONSTRAINT [t_rif40_study_sql_sid_line_fk];
	END;
	
	DROP TABLE [rif40].[t_rif40_study_sql_log]
END
GO

--table definition
CREATE TABLE [rif40].[t_rif40_study_sql_log](
	[username] [varchar](90) NOT NULL DEFAULT (SUSER_SNAME()),
	[study_id] [integer] NOT NULL DEFAULT ([rif40].[rif40_sequence_current_value](N'rif40.rif40_study_id_seq')),
	[statement_type] [varchar](30) NOT NULL,
	[statement_number] [numeric](6, 0) NOT NULL,
	[log_message] [varchar](4000) NOT NULL,
	[log_sqlcode] [varchar](5) NOT NULL,
	[rowcount] [numeric](12, 0) NOT NULL,
	[start_time] [datetimeoffset](6) NOT NULL DEFAULT (sysdatetime()),
	[elapsed_time] [numeric](10, 4) NOT NULL,
	[audsid] [varchar](90) NOT NULL  DEFAULT (@@spid),
 CONSTRAINT [t_rif40_study_sql_log_pk] PRIMARY KEY CLUSTERED 
(
	[study_id] ASC,
	[statement_number] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
CONSTRAINT [t_rif40_study_sqllog_stdid_fk] FOREIGN KEY([STUDY_ID])
	REFERENCES [rif40].[t_rif40_studies] ([study_id])
	ON UPDATE NO ACTION ON DELETE NO ACTION,
CONSTRAINT [statement_type_ck1] CHECK  
	(([statement_type]='DENOMINATOR_CHECK' OR [statement_type]='NUMERATOR_CHECK' OR [statement_type]='POST_INSERT' OR [statement_type]='INSERT' OR [statement_type]='CREATE'))	
) ON [PRIMARY]
GO

--replace foreign keys
IF EXISTS (SELECT * FROM sys.objects 
	WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_study_sql]') AND type in (N'U'))
BEGIN
	ALTER TABLE [rif40].[t_rif40_study_sql]  WITH CHECK ADD  
	CONSTRAINT [t_rif40_study_sql_sid_line_fk] FOREIGN KEY([study_id], [statement_number])
	REFERENCES [rif40].[t_rif40_study_sql_log] ([study_id], [statement_number])
	ON UPDATE NO ACTION ON DELETE NO ACTION
END
GO

--permissions
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[t_rif40_study_sql_log] TO [rif_user]
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[t_rif40_study_sql_log] TO [rif_manager]
GO

--comments
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Log of SQL executed for study.' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_study_sql_log'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Username', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_study_sql_log', @level2type=N'COLUMN',@level2name=N'username'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_study_sql_log', @level2type=N'COLUMN',@level2name=N'study_id'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Statement type: CREATE, INSERT, POST_INSERT, NUMERATOR_CHECK, DENOMINATOR_CHECK', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_study_sql_log', @level2type=N'COLUMN',@level2name=N'statement_type'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Statement number', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_study_sql_log', @level2type=N'COLUMN',@level2name=N'statement_number'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Log message', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_study_sql_log', @level2type=N'COLUMN',@level2name=N'log_message'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'SQL code (SQLSTATE hex code in Postgres)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_study_sql_log', @level2type=N'COLUMN',@level2name=N'log_sqlcode'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Row count', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_study_sql_log', @level2type=N'COLUMN',@level2name=N'rowcount'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Start time', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_study_sql_log', @level2type=N'COLUMN',@level2name=N'start_time'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Elapsed time', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_study_sql_log', @level2type=N'COLUMN',@level2name=N'elapsed_time'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Link to Oracle audit subsystem. On Postgres is &quot;backend PID.Julian day.Seconds from midnight.uSeconds (backend start time)&quot;. This can be correlated to the logging messages.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_study_sql_log', @level2type=N'COLUMN',@level2name=N'audsid'
GO

--indices
CREATE INDEX t_rif40_study_sqllog_type_bm
  ON [rif40].[t_rif40_study_sql_log](statement_type)
GO
CREATE INDEX t_rif40_study_sqllog_uname_bm
  ON [rif40].[t_rif40_study_sql_log](username)
GO
