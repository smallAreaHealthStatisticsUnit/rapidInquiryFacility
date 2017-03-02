
--drop table if exists
IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_triggers]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[rif40_triggers]
END
GO

--table definition
CREATE TABLE [rif40].[rif40_triggers](
	[table_name] [varchar](40),
	[column_name] [varchar](4000),
	[trigger_name] [varchar](30),
	[trigger_type] [varchar](16),
	[triggering_event] [varchar](227),
	[when_clause] [varchar](4000),
	[action_type] [varchar](11),
	[comments] [varchar](4000),
	CONSTRAINT [rif40_triggers_pk] PRIMARY KEY CLUSTERED (
		[table_name] ASC, 
		[trigger_name] ASC),
	CONSTRAINT [table_or_view_name_hide_trg_fk] FOREIGN KEY ([table_name])
		REFERENCES [rif40].[rif40_tables_and_views] ([table_or_view_name_hide])  
		ON UPDATE NO ACTION ON DELETE NO ACTION
) ON [PRIMARY]
GO

--permissions
GRANT SELECT ON [rif40].[rif40_triggers] TO [rif_user]
GO
GRANT SELECT ON [rif40].[rif40_triggers] TO [rif_manager]
GO

--comments
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'RIF40 Triggers' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_triggers'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Table name', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_triggers', @level2type=N'COLUMN',@level2name=N'table_name'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Column name', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_triggers', @level2type=N'COLUMN',@level2name=N'column_name'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Trigger name', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_triggers', @level2type=N'COLUMN',@level2name=N'trigger_name'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Trigger type', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_triggers', @level2type=N'COLUMN',@level2name=N'trigger_type'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Triggering event', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_triggers', @level2type=N'COLUMN',@level2name=N'triggering_event'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'When clause', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_triggers', @level2type=N'COLUMN',@level2name=N'when_clause'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Action type', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_triggers', @level2type=N'COLUMN',@level2name=N'action_type'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Comments', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_triggers', @level2type=N'COLUMN',@level2name=N'comments'
GO
