USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_investigations]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[t_rif40_investigations]
END
GO

CREATE TABLE [rif40].[t_rif40_investigations](
	[inv_id] [numeric](8, 0) NOT NULL,
	[study_id] [numeric](8, 0) NOT NULL,
	[username] [varchar](90) NOT NULL DEFAULT (user_name()),
	[geography] [varchar](30) NOT NULL,
	[inv_name] [varchar](20) NOT NULL,
	[inv_description] [varchar](250) NOT NULL,
	[classifier] [varchar](30) NOT NULL DEFAULT ('QUANTILE'),
	[classifier_bands] [numeric](2, 0) NOT NULL DEFAULT ((5)),
	[genders] [numeric](1, 0) NOT NULL,
	[numer_tab] [varchar](30) NOT NULL,
	[year_start] [numeric](4, 0) NOT NULL,
	[year_stop] [numeric](4, 0) NOT NULL,
	[max_age_group] [numeric](8, 0) NOT NULL,
	[min_age_group] [numeric](8, 0) NOT NULL,
	[investigation_state] [varchar](1) NOT NULL DEFAULT ('C'),
	[mh_test_type] [varchar](50) NOT NULL DEFAULT ('No Test'),
	[rowid] [uniqueidentifier] NOT NULL DEFAULT (newid()),
 CONSTRAINT [t_rif40_investigations_pk] PRIMARY KEY CLUSTERED 
(
	[study_id] ASC,
	[inv_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
CONSTRAINT [t_rif40_inv_numer_tab_fk] FOREIGN KEY([numer_tab])
	REFERENCES [rif40].[rif40_tables] ([table_name])
	ON UPDATE NO ACTION ON DELETE NO ACTION,
CONSTRAINT [t_rif40_inv_study_id_fk] FOREIGN KEY([study_id])
	REFERENCES [rif40].[t_rif40_studies] ([study_id])
	ON UPDATE NO ACTION ON DELETE NO ACTION,
CONSTRAINT [t_rif40_inv_class_bands_ck] CHECK  
	(([classifier_bands]>=(2) AND [classifier_bands]<=(20))),
CONSTRAINT [t_rif40_inv_classifier_ck] CHECK  
	(([classifier]='UNIQUE_INTERVAL' OR [classifier]='STANDARD_DEVIATION' OR [classifier]='QUANTILE' OR [classifier]='JENKS' 
	OR [classifier]='EQUAL_INTERVAL')),
CONSTRAINT [t_rif40_inv_genders_ck] CHECK  
	(([genders]>=(1) AND [genders]<=(3))),
CONSTRAINT [t_rif40_inv_mh_test_type_ck] CHECK  
	(([mh_test_type]='Unexposed Area' OR [mh_test_type]='Comparison Areas' OR [mh_test_type]='No Test')),
CONSTRAINT [t_rif40_inv_state_ck] CHECK  
	(([investigation_state]='U' OR [investigation_state]='R' OR [investigation_state]='E' OR [investigation_state]='V' OR [investigation_state]='C'))	
) ON [PRIMARY]
GO

GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[t_rif40_investigations] TO [rif_user]
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[t_rif40_investigations] TO [rif_manager]
GO

/*
COMMENT ON TABLE t_rif40_investigations
  IS 'Details of each investigation in a study';
COMMENT ON COLUMN t_rif40_investigations.inv_id IS 'Unique investigation index: inv_id. Created by SEQUENCE rif40_inv_id_seq';
COMMENT ON COLUMN t_rif40_investigations.study_id IS 'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq';
COMMENT ON COLUMN t_rif40_investigations.username IS 'Username';
COMMENT ON COLUMN t_rif40_investigations.inv_name IS 'Name of investigation. Must be a valid database column name, i.e. only contain A-Z0-9_ and start with a letter.';
COMMENT ON COLUMN t_rif40_investigations.inv_description IS 'Description of investigation';
COMMENT ON COLUMN t_rif40_investigations.classifier IS 'Maps classifier. EQUAL_INTERVAL: each classifier band represents the same sized range and intervals change based on max an min, JENKS: Jenks natural breaks, QUANTILE: equiheight (even number) distribution, STANDARD_DEVIATION, UNIQUE_INTERVAL: a version of EQUAL_INTERVAL that takes into account unique values, &lt;BESPOKE&gt;; default QUANTILE. &lt;BESPOKE&gt; classification bands are defined in: RIF40_CLASSFIER_BANDS, RIF40_CLASSFIER_BAND_NAMES and are used to create maps that are comparable accross investigations';
COMMENT ON COLUMN t_rif40_investigations.classifier_bands IS 'Map classifier bands; default 5. Must be between 2 and 20';
COMMENT ON COLUMN t_rif40_investigations.genders IS 'Genders to be investigated: 1 - males, 2 female or 3 - both';
COMMENT ON COLUMN t_rif40_investigations.numer_tab IS 'Numerator table name. May be &quot;DUMMY&quot; if extract created outside of the RIF.';
COMMENT ON COLUMN t_rif40_investigations.year_start IS 'Year investigation is to start. Must be between the limnits specified in the numerator and denominator tables';
COMMENT ON COLUMN t_rif40_investigations.year_stop IS 'Year investigation is to stop';
COMMENT ON COLUMN t_rif40_investigations.max_age_group IS 'Maximum age group (LOW_AGE to HIGH_AGE) in RIF40_AGE_GROUPS. OFFSET must be &gt; MIN_AGE_GROUP OFFSET, and a valid AGE_GROUP_ID in RIF40_AGE_GROUP_NAMES.AGE_GROUP_NAME';
COMMENT ON COLUMN t_rif40_investigations.min_age_group IS 'Minimum age group (LOW_AGE to HIGH_AGE) in RIF40_AGE_GROUPS. OFFSET must be &lt; MAX_AGE_GROUP OFFSET, and a valid AGE_GROUP_ID in RIF40_AGE_GROUP_NAMES.AGE_GROUP_NAME';
COMMENT ON COLUMN t_rif40_investigations.investigation_state IS 'Investigation state - C: created, not verfied; V: verified, but no other work done; E - extracted imported or created, but no results or maps created; R: results computed; U: upgraded record from V3.1 RIF (has an indeterminate state; probably R.';
COMMENT ON COLUMN t_rif40_investigations.mh_test_type IS 'Mantel-Haenszel test type: &quot;No test&quot;, &quot;Comparison Areas&quot;, &quot;Unexposed Area&quot;.';
*/

--triggers

