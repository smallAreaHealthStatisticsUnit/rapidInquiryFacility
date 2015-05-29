USE [sahsuland_dev]
GO

--drop table if exists
IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_study_shares]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[rif40_study_shares]
END
GO

--table definition

CREATE TABLE [rif40].[rif40_study_shares](
	[study_id] [int] NOT NULL DEFAULT ([rif40].[rif40_sequence_current_value](N'rif40.rif40_study_id_seq')),
	[grantor] [varchar](90) NOT NULL DEFAULT (SUSER_SNAME()),
	[grantee_username] [varchar](90) NOT NULL,
 CONSTRAINT [rif40_study_shares_pk] PRIMARY KEY CLUSTERED 
(
	[study_id] ASC,
	[grantee_username] ASC
) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON),
CONSTRAINT [rif40_study_shares_study_id_fk] FOREIGN KEY([study_id])
	REFERENCES [rif40].[t_rif40_studies] ([study_id])
	ON UPDATE NO ACTION ON DELETE NO ACTION
) ON [PRIMARY]
GO

--permissions
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_study_shares] TO [rif_manager]
GO
GRANT SELECT ON [rif40].[rif40_study_shares] TO [rif_user]
GO

--comments
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Users granted access by a RIF_MANAGER to study data' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_study_shares'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_study_shares', @level2type=N'COLUMN',@level2name=N'study_id'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Username doing grant', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_study_shares', @level2type=N'COLUMN',@level2name=N'grantor'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Username granted access by a RIF_MANAGER to study data', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_study_shares', @level2type=N'COLUMN',@level2name=N'grantee_username'
GO

--indices
CREATE INDEX rif40_study_shares_grantee_bm
  ON [rif40].[rif40_study_shares] (grantee_username)
GO

CREATE INDEX rif40_study_shares_grantor_bm
  ON [rif40].[rif40_study_shares] (grantor)
GO


