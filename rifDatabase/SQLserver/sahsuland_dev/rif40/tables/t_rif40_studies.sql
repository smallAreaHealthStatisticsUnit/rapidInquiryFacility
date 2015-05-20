USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_studies]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[t_rif40_studies]
END
GO


CREATE TABLE [rif40].[t_rif40_studies](
	[study_id] [numeric](8, 0) NOT NULL,
	[username] [varchar](90) NOT NULL DEFAULT (user_name()),
	[geography] [varchar](50) NOT NULL,
	[project] [varchar](30) NOT NULL,
	[study_name] [varchar](200) NOT NULL,
	[summary] [varchar](200) NULL,
	[description] [varchar](2000) NULL,
	[other_notes] [varchar](2000) NULL,
	[extract_table] [varchar](30) NOT NULL,
	[map_table] [varchar](30) NOT NULL,
	[study_date] [datetime2](0) NOT NULL DEFAULT (sysdatetime()),
	[study_type] [numeric](2, 0) NOT NULL,
	[study_state] [varchar](1) NOT NULL  DEFAULT ('C'),
	[comparison_geolevel_name] [varchar](30) NOT NULL,
	[study_geolevel_name] [varchar](30) NOT NULL,
	[denom_tab] [varchar](30) NOT NULL,
	[direct_stand_tab] [varchar](30) NULL,
	[suppression_value] [numeric](2, 0) NOT NULL,
	[extract_permitted] [numeric](1, 0) NOT NULL DEFAULT ((0)),
	[transfer_permitted] [numeric](1, 0) NOT NULL DEFAULT ((0)),
	[authorised_by] [varchar](90) NULL,
	[authorised_on] [datetime2](0) NULL,
	[authorised_notes] [varchar](200) NULL,
	[audsid] [varchar](90) NOT NULL DEFAULT (@@spid),
 CONSTRAINT [t_rif40_studies_pk] PRIMARY KEY CLUSTERED 
(
	[study_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
CONSTRAINT [rif40_studies_project_fk] FOREIGN KEY([project])
	REFERENCES [rif40].[t_rif40_projects] ([PROJECT])
	ON UPDATE NO ACTION ON DELETE NO ACTION,
CONSTRAINT [t_rif40_stud_denom_tab_fk] FOREIGN KEY([denom_tab])
	REFERENCES [rif40].[rif40_tables] ([table_name])
	ON UPDATE NO ACTION ON DELETE NO ACTION,
CONSTRAINT [t_rif40_stud_direct_stand_fk] FOREIGN KEY([direct_stand_tab])
	REFERENCES [rif40].[rif40_tables] ([table_name])
	ON UPDATE NO ACTION ON DELETE NO ACTION,
CONSTRAINT [t_rif40_stud_extract_perm_ck] CHECK  
	(([extract_permitted]=(1) OR [extract_permitted]=(0))),
CONSTRAINT [t_rif40_stud_transfer_perm_ck] CHECK  
	(([transfer_permitted]=(1) OR [transfer_permitted]=(0))),
CONSTRAINT [t_rif40_studies_study_state_ck] CHECK  
	(([study_state]='U' OR [study_state]='R' OR [study_state]='E' OR [study_state]='V' OR [study_state]='C')),
CONSTRAINT [t_rif40_studies_study_type_ck] CHECK  
	(([study_type]=(15) OR [study_type]=(14) OR [study_type]=(13) OR [study_type]=(12) OR [study_type]=(11) OR [study_type]=(1))),
CONSTRAINT [t_rif40_std_comp_geolevel_fk] FOREIGN KEY ([geography], [comparison_geolevel_name])
      REFERENCES [rif40].[t_rif40_geolevels] ([geography], [geolevel_name])
      ON UPDATE NO ACTION ON DELETE NO ACTION,
CONSTRAINT [t_rif40_std_study_geolevel_fk] FOREIGN KEY ([geography], [study_geolevel_name])
      REFERENCES [rif40].[t_rif40_geolevels] ([geography], [geolevel_name])
      ON UPDATE NO ACTION ON DELETE NO ACTION,
CONSTRAINT [t_rif40_studies_geography_fk] FOREIGN KEY ([geography])
      REFERENCES [rif40].[rif40_geographies] ([geography])
      ON UPDATE NO ACTION ON DELETE NO ACTION
) ON [PRIMARY]
GO

GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[t_rif40_studies] TO [rif_user]
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[t_rif40_studies] TO [rif_manager]
GO

/*
COMMENT ON TABLE t_rif40_studies
  IS 'RIF studies';
COMMENT ON COLUMN t_rif40_studies.study_id IS 'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq';
COMMENT ON COLUMN t_rif40_studies.username IS 'Username';
COMMENT ON COLUMN t_rif40_studies.geography IS 'Geography (e.g EW2001)';
COMMENT ON COLUMN t_rif40_studies.project IS 'Project running the study. The user must be allocated to the project.';
COMMENT ON COLUMN t_rif40_studies.study_name IS 'Study name';
COMMENT ON COLUMN t_rif40_studies.summary IS 'Study summary';
COMMENT ON COLUMN t_rif40_studies.description IS 'Study description';
COMMENT ON COLUMN t_rif40_studies.other_notes IS 'Study other notes';
COMMENT ON COLUMN t_rif40_studies.extract_table IS 'Extract table. Must only contain A-Z0-9_ and start with a letter.';
COMMENT ON COLUMN t_rif40_studies.map_table IS 'Map table. Must only contain A-Z0-9_ and start with a letter.';
COMMENT ON COLUMN t_rif40_studies.study_date IS 'Study date';
COMMENT ON COLUMN t_rif40_studies.study_type IS 'Study type: 1 - disease mapping, 11 - Risk Analysis (many areas, one band), 12 - Risk Analysis (point sources), 13 - Risk Analysis (exposure covariates), 14 - Risk Analysis (coverage shapefile), 15 - Risk Analysis (exposure shapefile)';
COMMENT ON COLUMN t_rif40_studies.study_state IS 'Study state - C: created, not verfied; V: verified, but no other work done; E - extracted imported or created, but no results or maps created; R: results computed; U: upgraded record from V3.1 RIF (has an indeterminate state; probably R.';
COMMENT ON COLUMN t_rif40_studies.comparison_geolevel_name IS 'Comparison area geolevel name. Must be a valid GEOLEVEL_NAME for the study GEOGRPAHY in T_RIF40_GEOLEVELS, with COMPAREA=1';
COMMENT ON COLUMN t_rif40_studies.study_geolevel_name IS 'Study area geolevel name';
COMMENT ON COLUMN t_rif40_studies.denom_tab IS 'Denominator table name. May be &quot;DUMMY&quot; if extract created outside of the RIF. Note the old RIF allowed studies to have different denominators between investigations; this capability has been removed to simplify the extract SQL and to allow a single rotated high performance extract table to be used for all investigations in a study. The extract table is then based on the standard rotated denominator (i.e. in age_sex_group, total format rather than M0 .. M5-9 ... etc) with one extract column per covariate and investigation. Multiple investigations may use a different numerator (1 per investigation).';
COMMENT ON COLUMN t_rif40_studies.direct_stand_tab IS 'Name of table to be used in direct standardisation. COMPARISON_GEOLEVEL_NAME must be NULL. May be &quot;DUMMY&quot; if extract created outside of the RIF. Note the old RIF allowed studies to have different denominators between investigations; this capability has been removed to simplify the extract SQL and to allow a single rotated high performance extract table to be used for all investigations in a study. The extract table is then based on the standard rotated denominator (i.e. in age_sex_group, total format rather than M0 ... M5-9 ... etc) with one extract column per covariate and investigation. Multiple investigations may use a different numerator (1 per investigation).';
COMMENT ON COLUMN t_rif40_studies.suppression_value IS 'Suppress results with low cell counts below this value. If the role RIF_NO_SUPRESSION is granted and the user is not a RIF_STUDENT then SUPPRESSION_VALUE=0; otherwise is equals the parameter &quot;SuppressionValue&quot;. If >0 all results with the value or below will be set to 0.';
COMMENT ON COLUMN t_rif40_studies.extract_permitted IS 'Is extract permitted from the database: 0/1. Only a RIF MANAGER may change this value. This user is still permitted to create and run a RIF study and to view the results. Geolevel access is rectricted by the RIF40_GEOLEVELS.RESTRICTED Inforamtion Governance restrictions (0/1). If 1 (Yes) then a) students cannot access this geolevel and b) if the system parameter ExtractControl=1 then the user must be granted permission by a RIF_MANAGER to extract from the database the results, data extract and maps tables. All students must be granted permission by a RIF_MANAGER for any extract if the system parameter ExtractControl=1. This is enforced by the RIF application.';
COMMENT ON COLUMN t_rif40_studies.transfer_permitted IS 'Is transfer permitted from the Secure or Private Network: 0/1. This is for purely documentatary purposes only. Only a RIF MANAGER may change this value. The value defaults to the same as EXTRACT_PERMITTED. Only geolevels where RIF40_GEOLEVELS.RESTRICTED=0 may be transferred.';
COMMENT ON COLUMN t_rif40_studies.authorised_by IS 'Who authorised extract and/or transfer. Must be a RIF MANAGER.';
COMMENT ON COLUMN t_rif40_studies.authorised_on IS 'When was the extract and/or transfer authorised';
COMMENT ON COLUMN t_rif40_studies.authorised_notes IS 'IG authorisation notes. Must be filled in if EXTRACT_PERMITTED=1.';
COMMENT ON COLUMN t_rif40_studies.audsid IS 'Link to Oracle audit subsystem. On Postgres is &quot;backend PID.Julian day.Seconds from midnight.uSeconds (backend start time)&quot;. This can be correlated to the logging messages.';
*/

CREATE UNIQUE INDEX [t_rif40_extract_table_uk]
  ON [rif40].[t_rif40_studies] (extract_table)
GO

CREATE UNIQUE INDEX [t_rif40_map_table_uk]
  ON [rif40].[t_rif40_studies] (map_table)
GO

--trigger