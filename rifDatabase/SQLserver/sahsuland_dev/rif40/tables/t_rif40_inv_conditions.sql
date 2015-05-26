USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_inv_conditions]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[t_rif40_inv_conditions]
END
GO

CREATE TABLE [rif40].[t_rif40_inv_conditions](
	[inv_id] [numeric](8, 0) NOT NULL DEFAULT (NEXT VALUE FOR [rif40].[rif40_inv_id_seq]),
	[study_id] [numeric](8, 0) NOT NULL DEFAULT (NEXT VALUE FOR [rif40].[rif40_study_id_seq]),
	[username] [varchar](90) NOT NULL DEFAULT (user_name()),
	[line_number] [numeric](5, 0) NOT NULL DEFAULT ((1)),
	[min_condition] [varchar](5) DEFAULT ('1=1'),
	[max_condition] [varchar] (5),
	[predefined_group_name][varchar](30),
	[outcome_group_name] [varchar](30) NOT NULL, 
 CONSTRAINT [t_rif40_inv_conditions_pk] PRIMARY KEY CLUSTERED 
(
	[study_id] ASC,
	[inv_id] ASC,
	[line_number] ASC
),
CONSTRAINT [t_rif40_inv_conditions_si_fk] FOREIGN KEY([study_id], [inv_id]) 
	REFERENCES [rif40].[t_rif40_investigations] ([study_id], [inv_id])
	ON UPDATE NO ACTION ON DELETE NO ACTION,
CONSTRAINT [t_rif40_inv_conditons_ogn]  FOREIGN KEY ([outcome_group_name]) 
	REFERENCES [rif40].[rif40_outcome_groups] ([outcome_group_name]) 
	ON UPDATE NO ACTION ON DELETE NO ACTION,
CONSTRAINT [t_rif40_inv_conditons_pgn] FOREIGN KEY ([predefined_group_name]) 	
	REFERENCES [rif40].[rif40_predefined_groups] ([predefined_group_name])
	ON UPDATE NO ACTION ON DELETE NO ACTION,
CONSTRAINT [max_condition_ck] CHECK
	((predefined_group_name IS NULL AND max_condition IS NOT NULL AND min_condition IS NOT NULL AND max_condition <> min_condition)
	OR (predefined_group_name IS NULL AND max_condition IS NULL)
	OR predefined_group_name IS NOT NULL),
CONSTRAINT [predefined_group_name_ck] CHECK
	((predefined_group_name IS NOT NULL AND min_condition IS NULL )
	OR (predefined_group_name IS NULL AND min_condition IS NOT NULL))
) ON [PRIMARY]
GO

GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[t_rif40_inv_conditions] TO [rif_user]
GO
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[t_rif40_inv_conditions] TO [rif_manager]
GO

/*
COMMENT ON TABLE t_rif40_inv_conditions
  IS 'Lines of SQL conditions pertinent to an investigation.';
COMMENT ON COLUMN t_rif40_inv_conditions.inv_id IS 'Unique investigation index: inv_id. Created by SEQUENCE rif40_inv_id_seq';
COMMENT ON COLUMN t_rif40_inv_conditions.study_id IS 'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq';
COMMENT ON COLUMN t_rif40_inv_conditions.username IS 'Username';
COMMENT ON COLUMN t_rif40_inv_conditions.line_number IS 'Line number';
COMMENT ON COLUMN t_rif40_inv_conditions.min_condition IS 'Minimum condition; if max condition is not null SQL WHERE Clause evaluates to: "WHERE <field_name> LIKE ''<min_condition>%''". ';
COMMENT ON COLUMN t_rif40_inv_conditions.max_condition IS 'Maximum condition; if max condition is not null SQL WHERE Clause evaluates to: "WHERE <field_name> BETWEEN ''<min_condition> AND <max_condition>~''" ';
COMMENT ON COLUMN t_rif40_inv_conditions.predefined_group_name IS 'Predefined Group Name. E.g LUNG_CANCER';
COMMENT ON COLUMN t_rif40_inv_conditions.outcome_group_name IS 'Outcome Group Name. E.g SINGLE_VARIABLE_ICD';
*/

