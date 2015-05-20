USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_age_group_names]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[rif40_age_group_names]
END
GO

CREATE TABLE [rif40].[rif40_age_group_names](
	[age_group_id] [numeric](3, 0) NOT NULL,
	[age_group_name] [varchar](50) NOT NULL,
 CONSTRAINT [rif40_age_group_names_pk] PRIMARY KEY CLUSTERED 
(
	[age_group_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

GRANT SELECT, REFERENCES ON  [rif40].[rif40_age_group_names] TO public
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_age_group_names] TO [rif_manager]
GO
