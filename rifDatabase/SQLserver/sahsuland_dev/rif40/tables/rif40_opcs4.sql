/*
28 May 2015: No longer in Postgres database -- should this be deleted?
*/


IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_opcs4]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[rif40_opcs4]
END
GO


CREATE TABLE [rif40].[rif40_opcs4](
	[opcs4_1char] [varchar](20) NULL,
	[opcs4_3char] [varchar](3) NULL,
	[opcs4_4char] [varchar](4) NULL,
	[text_1char] [varchar](250) NULL,
	[text_3char] [varchar](250) NULL,
	[text_4char] [varchar](250) NULL
) ON [PRIMARY]
GO

GRANT SELECT ON [rif40].[rif40_opcs4] TO public
GO

/*
COMMENT ON TABLE rif40_opcs4
  IS 'Office of Population Censuses and Surveys [OPCS] Classification of Interventions and Procedures';
COMMENT ON COLUMN rif40_opcs4.opcs4_1char IS 'OPCS4 chapter';
COMMENT ON COLUMN rif40_opcs4.opcs4_3char IS '3 Character OPCS4 code';
COMMENT ON COLUMN rif40_opcs4.opcs4_4char IS '4 Character OPCS4 code';
COMMENT ON COLUMN rif40_opcs4.text_1char IS 'OPCS4 chapter textual description';
COMMENT ON COLUMN rif40_opcs4.text_3char IS '3 Character OPCS4 textual description';
COMMENT ON COLUMN rif40_opcs4.text_4char IS '4 Character OPCS4 textual description';
*/

