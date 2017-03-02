
IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_test_runs]') AND type in (N'U'))
BEGIN
	
	--check for foreign keys
	IF EXISTS (SELECT * FROM sys.objects 
		WHERE object_id = OBJECT_ID(N'[rif40].[rif40_test_harness]') AND type in (N'U'))
		AND EXISTS (SELECT * FROM sys.foreign_keys 
		WHERE name='rif40_test_harness_test_run_id_fk')
	BEGIN
		ALTER TABLE [rif40].[rif40_test_harness] DROP CONSTRAINT [rif40_test_harness_test_run_id_fk];
	END;

	DROP TABLE [rif40].[rif40_test_runs]
END
GO

CREATE TABLE [rif40].[rif40_test_runs] (
  test_run_id integer NOT NULL DEFAULT (NEXT VALUE FOR [rif40].[rif40_test_run_id_seq]), -- Unique investigation index: test_run_id. Created by SEQUENCE rif40_test_run_id_seq
  test_run_title varchar(max) NOT NULL, -- Test run title
  test_date [datetime2](0) NOT NULL DEFAULT (sysdatetime()), -- Test date
  time_taken numeric NOT NULL DEFAULT 0, -- Time taken for test run (seconds)
  username varchar(90) NOT NULL DEFAULT (SUSER_SNAME()), -- user name running test run
  tests_run integer NOT NULL DEFAULT 0, -- Number of tests run (should equal passed+failed!)
  number_passed integer NOT NULL DEFAULT 0, -- Number of tests passed
  number_failed integer NOT NULL DEFAULT 0, -- Number of tests failed
  number_test_cases_registered integer NOT NULL DEFAULT 0, -- Number of test cases registered [OBSOLETE]
  number_messages_registered integer NOT NULL DEFAULT 0, -- Number of error and informational messages registered
  CONSTRAINT rif40_test_runs_pk PRIMARY KEY (test_run_id)
)
GO

--recreate foreign keys
IF EXISTS (SELECT * FROM sys.objects 
	WHERE object_id = OBJECT_ID(N'[rif40].[rif40_test_harness]') AND type in (N'U'))
BEGIN
	ALTER TABLE [rif40].[rif40_test_harness]  WITH CHECK ADD  
	CONSTRAINT rif40_test_harness_test_run_id_fk FOREIGN KEY (test_run_id)
    REFERENCES [rif40].[rif40_test_runs] (test_run_id) 
    ON UPDATE NO ACTION ON DELETE NO ACTION;
END
GO

GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_test_runs] TO [rif_manager];
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_test_runs] TO [notarifuser];
GRANT SELECT ON [rif40].[rif40_test_runs] TO [rif_user];

--comments
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Test harness test run information' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_test_runs'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Unique investigation index: test_run_id. Created by SEQUENCE rif40_test_run_id_seq', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_test_runs', @level2type=N'COLUMN',@level2name=N'test_run_id'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Test run title', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_test_runs', @level2type=N'COLUMN',@level2name=N'test_run_title'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Test date', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_test_runs', @level2type=N'COLUMN',@level2name=N'test_date'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Time taken for test run (seconds)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_test_runs', @level2type=N'COLUMN',@level2name=N'time_taken'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'user name running test run', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_test_runs', @level2type=N'COLUMN',@level2name=N'username'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Number of tests run (should equal passed+failed!)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_test_runs', @level2type=N'COLUMN',@level2name=N'tests_run'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Number of tests passed', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_test_runs', @level2type=N'COLUMN',@level2name=N'number_passed'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Number of tests failed', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_test_runs', @level2type=N'COLUMN',@level2name=N'number_failed'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Number of test cases registered [OBSOLETE]', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_test_runs', @level2type=N'COLUMN',@level2name=N'number_test_cases_registered'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Number of error and informational messages registered', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_test_runs', @level2type=N'COLUMN',@level2name=N'number_messages_registered'
GO
