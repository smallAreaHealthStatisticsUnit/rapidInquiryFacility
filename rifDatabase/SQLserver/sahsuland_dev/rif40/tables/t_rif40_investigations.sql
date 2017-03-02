
--drop table if exists
IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_investigations]') AND type in (N'U'))
BEGIN

	--drop foreign keys referencing it
	IF EXISTS (SELECT * FROM sys.objects 
		WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_inv_covariates]') AND type in (N'U'))
		AND EXISTS (SELECT * FROM sys.foreign_keys 
		WHERE name='t_rif40_inv_covariates_si_fk')
	BEGIN
		ALTER TABLE [rif40].[t_rif40_inv_covariates] DROP CONSTRAINT [t_rif40_inv_covariates_si_fk];
	END;
	IF EXISTS (SELECT * FROM sys.objects 
		WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_contextual_stats]') AND type in (N'U'))
		AND EXISTS (SELECT * FROM sys.foreign_keys 
		WHERE name='t_rif40_constats_study_id_fk')
	BEGIN
		ALTER TABLE [rif40].[t_rif40_contextual_stats] DROP CONSTRAINT [t_rif40_constats_study_id_fk];
	END;
	IF EXISTS (SELECT * FROM sys.objects 
		WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_inv_conditions]') AND type in (N'U'))
		AND EXISTS (SELECT * FROM sys.foreign_keys 
		WHERE name='t_rif40_inv_conditions_si_fk')
	BEGIN
		ALTER TABLE [rif40].[t_rif40_inv_conditions] DROP CONSTRAINT [t_rif40_inv_conditions_si_fk];
	END;
	IF EXISTS (SELECT * FROM sys.objects 
		WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_results]') AND type in (N'U'))
		AND EXISTS (SELECT * FROM sys.foreign_keys 
		WHERE name='t_rif40_results_study_id_fk')
	BEGIN
		ALTER TABLE [rif40].[t_rif40_results] DROP CONSTRAINT [t_rif40_results_study_id_fk];
	END;	
	
	DROP TABLE [rif40].[t_rif40_investigations]
END
GO

--table definition
CREATE TABLE [rif40].[t_rif40_investigations](
	[inv_id] [integer] NOT NULL CONSTRAINT t_rif40_investigation_inv_id_seq DEFAULT (NEXT VALUE FOR [rif40].[rif40_inv_id_seq]),
	[study_id] [integer] NOT NULL CONSTRAINT t_rif40_investigation_study_id_seq DEFAULT ([rif40].[rif40_sequence_current_value](N'rif40.rif40_study_id_seq')),
	[username] [varchar](90) NOT NULL DEFAULT (SUSER_SNAME()),
	[inv_name] [varchar](20) NOT NULL,
	[inv_description] [varchar](250) NOT NULL,
	[classifier] [varchar](30) NOT NULL DEFAULT ('QUANTILE'),
	[classifier_bands] [numeric](2, 0) NOT NULL DEFAULT ((5)),
	[genders] [numeric](1, 0) NOT NULL,
	[numer_tab] [varchar](30) NOT NULL,
	[year_start] [numeric](4, 0) NOT NULL,
	[year_stop] [numeric](4, 0) NOT NULL,
	[max_age_group] [integer] NOT NULL,
	[min_age_group] [integer] NOT NULL,
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

	--replace foreign key references
	IF EXISTS (SELECT * FROM sys.objects 
		WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_contextual_stats]') AND type in (N'U'))
	BEGIN
		ALTER TABLE [rif40].[t_rif40_contextual_stats]  WITH CHECK ADD  
		CONSTRAINT [t_rif40_constats_study_id_fk] FOREIGN KEY([study_id], [inv_id])
		REFERENCES [rif40].[t_rif40_investigations] ([study_id], [inv_id])
		ON UPDATE NO ACTION ON DELETE NO ACTION;
	END;
	IF EXISTS (SELECT * FROM sys.objects 
		WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_inv_conditions]') AND type in (N'U'))
	BEGIN
		ALTER TABLE [rif40].[t_rif40_inv_conditions]  WITH CHECK ADD  
		CONSTRAINT [t_rif40_inv_conditions_si_fk] FOREIGN KEY([study_id], [inv_id])
		REFERENCES [rif40].[t_rif40_investigations] ([study_id], [inv_id])
		ON UPDATE NO ACTION ON DELETE NO ACTION;
	END;
	IF EXISTS (SELECT * FROM sys.objects 
		WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_inv_covariates]') AND type in (N'U'))
	BEGIN
		ALTER TABLE [rif40].[t_rif40_inv_covariates]  WITH CHECK ADD  
		CONSTRAINT [t_rif40_inv_covariates_si_fk] FOREIGN KEY([study_id], [inv_id])
		REFERENCES [rif40].[t_rif40_investigations] ([study_id], [inv_id])
		ON UPDATE NO ACTION ON DELETE NO ACTION;
	END;
	IF EXISTS (SELECT * FROM sys.objects 
		WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_results]') AND type in (N'U'))
	BEGIN
		ALTER TABLE [rif40].[t_rif40_results]  WITH CHECK ADD  
		CONSTRAINT [t_rif40_results_study_id_fk] FOREIGN KEY([study_id], [inv_id])
		REFERENCES [rif40].[t_rif40_investigations] ([study_id], [inv_id])
		ON UPDATE NO ACTION ON DELETE NO ACTION;
	END;

--Permissions
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[t_rif40_investigations] TO [rif_user]
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[t_rif40_investigations] TO [rif_manager]
GO

--Comments
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Details of each investigation in a study' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_investigations'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Unique investigation index: inv_id. Created by SEQUENCE rif40_inv_id_seq', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_investigations', @level2type=N'COLUMN',@level2name=N'inv_id'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_investigations', @level2type=N'COLUMN',@level2name=N'study_id'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Username', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_investigations', @level2type=N'COLUMN',@level2name=N'username'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Name of investigation. Must be a valid database column name, i.e. only contain A-Z0-9_ and start with a letter.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_investigations', @level2type=N'COLUMN',@level2name=N'inv_name'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Description of investigation', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_investigations', @level2type=N'COLUMN',@level2name=N'inv_description'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Maps classifier. EQUAL_INTERVAL: each classifier band represents the same sized range and intervals change based on max an min, JENKS: Jenks natural breaks, QUANTILE: equiheight (even number) distribution, STANDARD_DEVIATION, UNIQUE_INTERVAL: a version of EQUAL_INTERVAL that takes into account unique values, &lt;BESPOKE&gt;; default QUANTILE. &lt;BESPOKE&gt; classification bands are defined in: RIF40_CLASSFIER_BANDS, RIF40_CLASSFIER_BAND_NAMES and are used to create maps that are comparable across investigations', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_investigations', @level2type=N'COLUMN',@level2name=N'classifier'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Map classifier bands; default 5. Must be between 2 and 20', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_investigations', @level2type=N'COLUMN',@level2name=N'classifier_bands'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Genders to be investigated: 1 - males, 2 female or 3 - both', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_investigations', @level2type=N'COLUMN',@level2name=N'genders'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Numerator table name. May be &quot;DUMMY&quot; if extract created outside of the RIF.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_investigations', @level2type=N'COLUMN',@level2name=N'numer_tab'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Year investigation is to start. Must be between the limnits specified in the numerator and denominator tables', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_investigations', @level2type=N'COLUMN',@level2name=N'year_start'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Year investigation is to stop', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_investigations', @level2type=N'COLUMN',@level2name=N'year_stop'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Maximum age group (LOW_AGE to HIGH_AGE) in RIF40_AGE_GROUPS. OFFSET must be &gt; MIN_AGE_GROUP OFFSET, and a valid AGE_GROUP_ID in RIF40_AGE_GROUP_NAMES.AGE_GROUP_NAME', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_investigations', @level2type=N'COLUMN',@level2name=N'max_age_group'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Minimum age group (LOW_AGE to HIGH_AGE) in RIF40_AGE_GROUPS. OFFSET must be &lt; MAX_AGE_GROUP OFFSET, and a valid AGE_GROUP_ID in RIF40_AGE_GROUP_NAMES.AGE_GROUP_NAME', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_investigations', @level2type=N'COLUMN',@level2name=N'min_age_group'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Investigation state - C: created, not verfied; V: verified, but no other work done; E - extracted imported or created, but no results or maps created; R: results computed; U: upgraded record from V3.1 RIF (has an indeterminate state; probably R.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_investigations', @level2type=N'COLUMN',@level2name=N'investigation_state'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Mantel-Haenszel test type: &quot;No test&quot;, &quot;Comparison Areas&quot;, &quot;Unexposed Area&quot;.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_investigations', @level2type=N'COLUMN',@level2name=N'mh_test_type'
GO

