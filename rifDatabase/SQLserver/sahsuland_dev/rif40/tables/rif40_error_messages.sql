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
END
GO

GRANT SELECT ON [rif40].[rif40_error_messages] TO public
GO

/*
COMMENT ON TABLE rif40_error_messages
  IS 'RIF error messages resulting from schema violations';
COMMENT ON COLUMN rif40_error_messages.error_code IS 'Error code';
COMMENT ON COLUMN rif40_error_messages.tag IS 'Contextual tag for error code';
COMMENT ON COLUMN rif40_error_messages.table_name IS 'Table raising error. If NULL, procedure or other code.';
COMMENT ON COLUMN rif40_error_messages.cause IS 'Cause of error';
COMMENT ON COLUMN rif40_error_messages.action IS 'Action to resolve error';
COMMENT ON COLUMN rif40_error_messages.message IS 'Error message';
*/

--triggers