USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_study_shares]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[rif40_study_shares]
END
GO

CREATE TABLE [rif40].[rif40_study_shares](
	[study_id] [numeric](8, 0) NOT NULL,
	[grantor] [varchar](90) NOT NULL DEFAULT (user_name()),
	[grantee_username] [varchar](90) NOT NULL,
	[rowid] [uniqueidentifier] NOT NULL DEFAULT (newid()),
 CONSTRAINT [rif40_study_shares_pk] PRIMARY KEY CLUSTERED 
(
	[study_id] ASC,
	[grantee_username] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
CONSTRAINT [rif40_study_shares_study_id_fk] FOREIGN KEY([study_id])
	REFERENCES [rif40].[t_rif40_studies] ([study_id])
	ON UPDATE NO ACTION ON DELETE NO ACTION
) ON [PRIMARY]
GO

GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_study_shares] TO [rif_manager]
GO
GRANT SELECT ON [rif40].[rif40_study_shares] TO [rif_user]
GO

/*
COMMENT ON TABLE rif40_study_shares
  IS 'Users granted access by a RIF_MANAGER to study data';
COMMENT ON COLUMN rif40_study_shares.study_id IS 'Unique study index: study_id. Created by SEQUENCE rif40_study_id_seq';
COMMENT ON COLUMN rif40_study_shares.grantor IS 'Username doing grant';
COMMENT ON COLUMN rif40_study_shares.grantee_username IS 'Username granted access by a RIF_MANAGER to study data';
*/

CREATE INDEX rif40_study_shares_grantee_bm
  ON [rif40].[rif40_study_shares] (grantee_username)
GO

CREATE INDEX rif40_study_shares_grantor_bm
  ON [rif40].[rif40_study_shares] (grantor)
GO

--trigger
