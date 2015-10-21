/*
Test harness test cases and last run information
*/

USE [sahsuland_dev]
GO

IF EXISTS (SELECT * FROM sys.objects 
WHERE object_id = OBJECT_ID(N'[rif40].[rif40_test_harness]') AND type in (N'U'))
BEGIN
	DROP TABLE [rif40].[rif40_test_harness]
END
GO

CREATE TABLE [rif40].[rif40_test_harness] (
  test_id integer NOT NULL DEFAULT (NEXT VALUE FOR [rif40].[rif40_test_id_seq]),  -- Unique investigation index: test_id. Created by SEQUENCE rif40_test_id_seq
  parent_test_id integer, -- Parent test ID; NULL means first (test statement). Allows for a string of connected test cases. Multiple inheritance of test cases is not permitted!
  test_run_class varchar(max) NOT NULL, -- Test run class; usually the name of the SQL script that originally ran it
  test_stmt varchar(max) NOT NULL, -- SQL statement for test
  test_case_title varchar(max) NOT NULL, -- Test case title. Must be unique
  pg_error_code_expected varchar(max), -- [negative] Postgres error SQLSTATE expected [as part of an exception]; passed as PG_EXCEPTION_DETAIL
  mssql_error_code_expected varchar(max), -- Microsoft SQL server error code expected [as part of an exception].
  raise_exception_on_failure numeric (1,0) NOT NULL DEFAULT 1, -- Raise exception on failure. NULL means it is expected to NOT raise an exception, raise exception on failure
  expected_result numeric (1,0) NOT NULL DEFAULT 1, -- Expected result; tests are allowed to deliberately fail! If the test raises the expection pg_error_code_expected it would normally be expected to pass.
  register_date [datetime2](0) NOT NULL DEFAULT (sysdatetime()), -- Date registered
   results varchar(max), -- Results array, but there are no arrays in MSSQL so currently just a long string
  results_xml xml, -- Results array in portable XML
  pass numeric (1,0), -- Was the test passed? Pass means the test passed with no exzception if the exception is null or if the exoected exception was caught. Note that some tests do fail deliberately to test the harness
  test_run_id integer, -- Test run id for test. Foreign key to rif40_test_runs table.
  test_date [datetime2](0), -- Test date
  time_taken numeric, -- Time taken for test (seconds)
  pg_debug_functions varchar(max), -- Array of Postgres functions for test harness to enable debug on, but currently just a long string since MSSQL does not have arrays
  CONSTRAINT rif40_test_harness_pk PRIMARY KEY (test_id),
  CONSTRAINT rif40_test_harness_parent_test_id_fk FOREIGN KEY (parent_test_id)
      REFERENCES [rif40].[rif40_test_harness] (test_id)
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT rif40_test_harness_test_run_id_fk FOREIGN KEY (test_run_id)
      REFERENCES [rif40].[rif40_test_runs] (test_run_id) 
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
GO

GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_test_harness] TO [rif_manager];
GRANT SELECT, UPDATE, INSERT, DELETE ON [rif40].[rif40_test_harness] TO [notarifuser];
GRANT SELECT ON [rif40].[rif40_test_harness] TO [rif_user];

--comments
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Test harness test cases and last run information' , @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_test_harness'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Unique investigation index: test_id. Created by SEQUENCE rif40_test_id_seq', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_test_harness', @level2type=N'COLUMN',@level2name=N'test_id'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Parent test ID; NULL means first (test statement). Allows for a string of connected test cases. Multiple inheritance of test cases is not permitted!', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_test_harness', @level2type=N'COLUMN',@level2name=N'parent_test_id'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Test run class; usually the name of the SQL script that originally ran it', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_test_harness', @level2type=N'COLUMN',@level2name=N'test_run_class'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'SQL statement for test', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_test_harness', @level2type=N'COLUMN',@level2name=N'test_stmt'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Test case title. Must be unique', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_test_harness', @level2type=N'COLUMN',@level2name=N'test_case_title'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'[negative] Postgres error SQLSTATE expected [as part of an exception]; passed as PG_EXCEPTION_DETAIL', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_test_harness', @level2type=N'COLUMN',@level2name=N'pg_error_code_expected'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Microsoft SQL server error code expected [as part of an exception].', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_test_harness', @level2type=N'COLUMN',@level2name=N'mssql_error_code_expected'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Raise exception on failure. NULL means it is expected to NOT raise an exception, raise exception on failure', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_test_harness', @level2type=N'COLUMN',@level2name=N'raise_exception_on_failure'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Expected result; tests are allowed to deliberately fail! If the test raises the expection pg_error_code_expected it would normally be expected to pass.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_test_harness', @level2type=N'COLUMN',@level2name=N'expected_result'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Date registered', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_test_harness', @level2type=N'COLUMN',@level2name=N'register_date'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Results array (currently string in MSSQL)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_test_harness', @level2type=N'COLUMN',@level2name=N'results'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Results array in portable XML', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_test_harness', @level2type=N'COLUMN',@level2name=N'results_xml'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Was the test passed? Pass means the test passed with no exzception if the exception is null or if the exoected exception was caught. Note that some tests do fail deliberately to test the harness', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_test_harness', @level2type=N'COLUMN',@level2name=N'pass'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Test run id for test. Foreign key to rif40_test_runs table.', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_test_harness', @level2type=N'COLUMN',@level2name=N'test_run_id'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Test date', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_test_harness', @level2type=N'COLUMN',@level2name=N'test_date'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Time taken for test (seconds)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_test_harness', @level2type=N'COLUMN',@level2name=N'time_taken'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Array of Postgres functions for test harness to enable debug on (currently string in MSSQL)', @level0type=N'SCHEMA',@level0name=N'rif40', @level1type=N'TABLE',@level1name=N'rif40_test_harness', @level2type=N'COLUMN',@level2name=N'pg_debug_functions'
GO

--index
CREATE UNIQUE INDEX rif40_test_harness_uk
  ON [rif40].[rif40_test_harness](parent_test_id);