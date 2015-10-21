USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_test_run_id_seq]') AND type in (N'SO'))
BEGIN
	DROP SEQUENCE [rif40].[rif40_test_run_id_seq]
END
GO

CREATE SEQUENCE [rif40].[rif40_test_run_id_seq]
	AS BIGINT
	START WITH 3
	INCREMENT by 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	NO CYCLE
	NO CACHE 
GO

GRANT UPDATE ON [rif40].[rif40_test_run_id_seq] TO [rif_manager];
GRANT UPDATE ON  [rif40].[rif40_test_run_id_seq] TO [notarifuser];

/*
COMMENT ON SEQUENCE rif40_test_run_id_seq
  IS 'Artificial primary key for: RIF40_TEST_RUNS';
*/