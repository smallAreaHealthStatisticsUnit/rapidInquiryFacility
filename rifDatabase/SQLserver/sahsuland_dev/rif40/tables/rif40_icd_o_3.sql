/*
28 May 2015: No longer in Postgres -- should this be deleted?
*/


IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_icd_o_3]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[rif40_icd_o_3]
END
GO

CREATE TABLE [rif40].[rif40_icd_o_3](
	[icd_o_3_1char] [varchar](20) NULL,
	[icd_o_3_4char] [varchar](4) NOT NULL,
	[text_1char] [varchar](250) NULL,
	[text_4char] [varchar](250) NULL,
 CONSTRAINT [rif40_icd_o_3_pk] PRIMARY KEY CLUSTERED 
(
	[icd_o_3_4char] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

GRANT SELECT ON [rif40].[rif40_icd_o_3] TO public
GO

CREATE INDEX rif40_icd_o_3_1char_bm
  ON [rif40].[rif40_icd_o_3](icd_o_3_1char)
 GO
 
