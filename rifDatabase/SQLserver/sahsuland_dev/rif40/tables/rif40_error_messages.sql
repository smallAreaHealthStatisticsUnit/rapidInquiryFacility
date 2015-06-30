USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_error_messages]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[rif40_error_messages]
END
GO


CREATE TABLE [rif40].[rif40_error_messages](
	[error_code] [numeric](5, 0) NOT NULL,
	[tag] [varchar](80) NOT NULL,
	[table_name] [varchar](30) NULL,
	[cause] [varchar](4000) NOT NULL,
	[action] [varchar](512) NOT NULL,
	[message] [varchar](512) NOT NULL,
 CONSTRAINT [rif40_error_messages_pk] PRIMARY KEY CLUSTERED 
(
	[error_code] ASC,
	[tag] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
CONSTRAINT [rif40_error_messages_code_ck] CHECK 
	(([error_code] in ((-1), (-4088), (-2290), (-2291))) OR ([error_code] >= (-20999) AND [error_code] <= (-20000)))
) ON [PRIMARY]
GO

GRANT SELECT ON [rif40].[rif40_error_messages] TO public
GO

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'RIF error messages resulting from schema violations' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_error_messages'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Error code', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_error_messages', @level2type=N'COLUMN',@level2name=N'error_code'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Contextual tag for error code', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_error_messages', @level2type=N'COLUMN',@level2name=N'tag'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Table raising error. If NULL, procedure or other code.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_error_messages', @level2type=N'COLUMN',@level2name=N'table_name'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Cause of error', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_error_messages', @level2type=N'COLUMN',@level2name=N'cause'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Action to resolve error', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_error_messages', @level2type=N'COLUMN',@level2name=N'action'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Error message', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_error_messages', @level2type=N'COLUMN',@level2name=N'message'
GO
