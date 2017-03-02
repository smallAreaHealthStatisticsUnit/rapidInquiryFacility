/*
29 May 2015: This table no longer appears in Postgres rif40 table list.  Was it moved or deleted?
*/


IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_a_and_e]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[rif40_a_and_e]
END
GO

CREATE TABLE [rif40].[rif40_a_and_e](
	[a_and_e_3char] [varchar](4) NOT NULL,
	[text_3char] [varchar](200) NULL,
 CONSTRAINT [rif40_a_and_e_pk] PRIMARY KEY CLUSTERED 
(
	[a_and_e_3char] ASC
) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

GRANT SELECT ON [rif40].[rif40_a_and_e] TO public
GO


