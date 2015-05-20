USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_triggers]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[rif40_triggers]
END
GO

CREATE TABLE [rif40].[rif40_triggers](
	[table_name] [varchar](30),
	[column_name] [varchar](4000),
	[trigger_name] [varchar](30),
	[trigger_type] [varchar](16),
	[triggering_event] [varchar](227),
	[when_clause] [varchar](4000),
	[action_type] [varchar](11),
	[comments] [varchar](4000)
) ON [PRIMARY]
GO

GRANT SELECT ON [rif40].[rif40_triggers] TO [rif_user]
GO
GRANT SELECT ON [rif40].[rif40_triggers] TO [rif_manager]
GO

/*
COMMENT ON TABLE rif40_triggers
  IS 'RIF40 Triggers';
COMMENT ON COLUMN rif40_triggers.table_name IS 'Table name';
COMMENT ON COLUMN rif40_triggers.column_name IS 'Column name';
COMMENT ON COLUMN rif40_triggers.trigger_name IS 'Trigger name';
COMMENT ON COLUMN rif40_triggers.trigger_type IS 'Type type';
COMMENT ON COLUMN rif40_triggers.triggering_event IS 'Triggering event';
COMMENT ON COLUMN rif40_triggers.when_clause IS 'When clause';
COMMENT ON COLUMN rif40_triggers.action_type IS 'Action type';
COMMENT ON COLUMN rif40_triggers.comments IS 'Comments';
*/
