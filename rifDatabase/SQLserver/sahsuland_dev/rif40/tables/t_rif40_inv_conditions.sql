
--drop table if exists
IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[t_rif40_inv_conditions]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[t_rif40_inv_conditions]
END
GO

--table definition
CREATE TABLE [rif40].[t_rif40_inv_conditions](
	[inv_id] [integer] NOT NULL CONSTRAINT t_rif40_inv_condition_inv_id_seq DEFAULT ([rif40].[rif40_sequence_current_value](N'rif40.rif40_inv_id_seq')),
	[study_id] [integer] NOT NULL CONSTRAINT t_rif40_inv_condition_study_id_seq DEFAULT ([rif40].[rif40_sequence_current_value](N'rif40.rif40_study_id_seq')),
	[username] [varchar](90) NOT NULL DEFAULT (SUSER_SNAME()),
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

EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Lines of SQL conditions pertinent to an investigation.' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_inv_conditions'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_inv_conditions', @level2type=N'COLUMN',@level2name=N'study_id'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Unique investigation inde:inv_id. Created by SEQUENCE rif40_inv_id_seq', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_inv_conditions', @level2type=N'COLUMN',@level2name=N'inv_id'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Username', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_inv_conditions', @level2type=N'COLUMN',@level2name=N'username'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Line number', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_inv_conditions', @level2type=N'COLUMN',@level2name=N'line_number'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Minimum condition; if max condition is not null SQL WHERE Clause evaluates to: "WHERE <field_name> LIKE ''<min_condition>%''".', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_inv_conditions', @level2type=N'COLUMN',@level2name=N'min_condition'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Maximum condition; if max condition is not null SQL WHERE Clause evaluates to: "WHERE <field_name> BETWEEN ''<min_condition> AND <max_condition>~''" ', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_inv_conditions', @level2type=N'COLUMN',@level2name=N'max_condition'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Predefined Group Name. E.g LUNG_CANCER', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_inv_conditions', @level2type=N'COLUMN',@level2name=N'predefined_group_name'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Outcome Group Name. E.g SINGLE_VARIABLE_ICD', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N't_rif40_inv_conditions', @level2type=N'COLUMN',@level2name=N'outcome_group_name'
GO

