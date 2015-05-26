USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_inv_id_seq]') AND type in (N'SO'))
BEGIN
	DROP SEQUENCE [rif40].[rif40_inv_id_seq]
END
GO

CREATE SEQUENCE [rif40].[rif40_inv_id_seq]
	AS BIGINT
	START WITH 18
	INCREMENT by 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	NO CYCLE
	CACHE 1 
GO
	
GRANT UPDATE ON [rif40].[rif40_inv_id_seq] TO [rif_user]
GO
GRANT UPDATE ON [rif40].[rif40_inv_id_seq] TO [rif_manager]
GO

/*
COMMENT ON SEQUENCE rif40_inv_id_seq
  IS 'Used as sequence for unique study index: study_id; auto populated.';
*/