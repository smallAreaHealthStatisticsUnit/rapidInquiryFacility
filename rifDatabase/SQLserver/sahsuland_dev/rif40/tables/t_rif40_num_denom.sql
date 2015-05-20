USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_num_denom]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[t_rif40_num_denom]
END
GO

CREATE TABLE [rif40].[t_rif40_num_denom](
	[geography] [varchar](50) NOT NULL,
	[numerator_table] [varchar](30) NOT NULL,
	[denominator_table] [varchar](30) NOT NULL,
 CONSTRAINT [t_rif40_num_denom_pk] PRIMARY KEY CLUSTERED 
(
	[geography] ASC,
	[numerator_table] ASC,
	[denominator_table] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
CONSTRAINT [t_rif40_num_denom_denom_fk] FOREIGN KEY([denominator_table])
	REFERENCES [rif40].[rif40_tables] ([table_name])
	ON UPDATE NO ACTION ON DELETE NO ACTION,
CONSTRAINT [t_rif40_num_denom_geog_fk] FOREIGN KEY([geography])
	REFERENCES [rif40].[rif40_geographies] ([geography])
	ON UPDATE NO ACTION ON DELETE NO ACTION,
CONSTRAINT [t_rif40_num_denom_numer_fk] FOREIGN KEY([numerator_table])
	REFERENCES [rif40].[rif40_tables] ([table_name])
	ON UPDATE NO ACTION ON DELETE NO ACTION
) ON [PRIMARY]
GO

/*
COMMENT ON TABLE t_rif40_num_denom
  IS 'Private copy of extra numerator and denominator pairs not added automatically.';
COMMENT ON COLUMN t_rif40_num_denom.geography IS 'Geography (e.g EW2001)';
COMMENT ON COLUMN t_rif40_num_denom.numerator_table IS 'Numerator table';
COMMENT ON COLUMN t_rif40_num_denom.denominator_table IS 'Denominator table';
*/

CREATE UNIQUE INDEX rif40_num_denom_pk
  ON [rif40].[t_rif40_num_denom](geography)
GO

--triggers