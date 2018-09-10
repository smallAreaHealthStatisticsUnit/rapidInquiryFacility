---
layout: default
title: README for RIF node.js database test harness
---

* node.js is required to build to geoJSON to topoJSON converter by Mike Bostock
  at: https://github.com/mbostock/topojson/wiki/Installation
* node.js is available from: http://nodejs.org/

## Node Installation

* On Windows install MS Visual Studio; e.g. from Dreamspark
* [Install GDAL if QGis is not installed]
* Install Python (2.7 or later) from https://www.python.org/downloads/ **NOT 3.x.x series!**
* Install node.js

### Install topojson

**Not required at present**

Then install topojson through npm: *npm install topojson*

```topojson
C:\Users\pch\AppData\Roaming\npm\topojson.cmd -q 1e6 -o test_6_geojson_test_01.json ..\psql_scripts\test_scripts\data\test_6_geojson_test_01.json
bounds: -6.68852598 54.6456466 -6.32507059 55.01219818 (spherical)
pre-quantization: 0.0404m (3.63e-7�) 0.0408m (3.67e-7�)
topology: 160 arcs, 3502 points
prune: retained 160 / 160 arcs (100%)
```

### Install Postgres connectors pg and pg-native

Checks:

* Postgres must be installed.
* Type: pg_config to test if Postgres extensibility is installed, pg-native requires MS Visual Studio.
* check you can connect to psql without a password (i.e. using pgass/Kerberos). pg-native must be able to connect to the database to install!

```npm
P:\Github\rapidInquiryFacility\rifDatabase\Postgres\psql_scripts>npm install pg pg-native
> libpq@1.6.4 install P:\Github\rapidInquiryFacility\rifDatabase\TestHarness\db_test_harness\node_modules\pg-native\node_modules\libpq
> node-gyp rebuild


P:\Github\rapidInquiryFacility\rifDatabase\TestHarness\db_test_harness\node_modules\pg-native\node_modules\libpq>node "c:\Program Files\nodejs\node_modules\npm\bin\node-gyp-bin\\..
\..\node_modules\node-gyp\bin\node-gyp.js" rebuild
Building the projects in this solution one at a time. To enable parallel build, please add the "/m" switch.
  connection.cc
  connect-async-worker.cc
  addon.cc
p:\github\rapidinquiryfacility\rifdatabase\testharness\db_test_harness\node_modules\pg-native\node_modules\libpq\src\addon.h(27): warning C4005: 'THIS' : macro redefinition [P:\Gi
thub\rapidInquiryFacility\rifDatabase\TestHarness\db_test_harness\node_modules\pg-native\node_modules\libpq\build\addon.vcxproj]
          C:\Program Files (x86)\Microsoft SDKs\Windows\v7.0A\include\objbase.h(206) : see previous definition of 'THIS'
p:\github\rapidinquiryfacility\rifdatabase\testharness\db_test_harness\node_modules\pg-native\node_modules\libpq\src\addon.h(27): warning C4005: 'THIS' : macro redefinition [P:\Gi
thub\rapidInquiryFacility\rifDatabase\TestHarness\db_test_harness\node_modules\pg-native\node_modules\libpq\build\addon.vcxproj]
          C:\Program Files (x86)\Microsoft SDKs\Windows\v7.0A\include\objbase.h(206) : see previous definition of 'THIS'
p:\github\rapidinquiryfacility\rifdatabase\testharness\db_test_harness\node_modules\pg-native\node_modules\libpq\src\addon.h(27): warning C4005: 'THIS' : macro redefinition [P:\Gi
thub\rapidInquiryFacility\rifDatabase\TestHarness\db_test_harness\node_modules\pg-native\node_modules\libpq\build\addon.vcxproj]
          C:\Program Files (x86)\Microsoft SDKs\Windows\v7.0A\include\objbase.h(206) : see previous definition of 'THIS'
..\src\connection.cc(691): warning C4267: 'initializing' : conversion from 'size_t' to 'int', possible loss of data [P:\Github\rapidInquiryFacility\rifDatabase\TestHarness\db_test
_harness\node_modules\pg-native\node_modules\libpq\build\addon.vcxproj]
C:\Users\pch\.node-gyp\0.10.36\deps\v8\include\v8.h(179): warning C4506: no definition for inline function 'v8::Persistent<T> v8::Persistent<T>::New(v8::Handle<T>)' [P:\Github\rap
idInquiryFacility\rifDatabase\TestHarness\db_test_harness\node_modules\pg-native\node_modules\libpq\build\addon.vcxproj]
          with
          [
              T=v8::Object
          ]
C:\Users\pch\.node-gyp\0.10.36\deps\v8\include\v8.h(179): warning C4506: no definition for inline function 'v8::Persistent<T> v8::Persistent<T>::New(v8::Handle<T>)' [P:\Github\rap
idInquiryFacility\rifDatabase\TestHarness\db_test_harness\node_modules\pg-native\node_modules\libpq\build\addon.vcxproj]
          with
          [
              T=v8::Object
          ]
C:\Users\pch\.node-gyp\0.10.36\deps\v8\include\v8.h(179): warning C4506: no definition for inline function 'v8::Persistent<T> v8::Persistent<T>::New(v8::Handle<T>)' [P:\Github\rap
idInquiryFacility\rifDatabase\TestHarness\db_test_harness\node_modules\pg-native\node_modules\libpq\build\addon.vcxproj]
          with
          [
              T=v8::Object
          ]
     Creating library P:\Github\rapidInquiryFacility\rifDatabase\TestHarness\db_test_harness\node_modules\pg-native\node_modules\libpq\build\Release\addon.lib and object P:\Github
  \rapidInquiryFacility\rifDatabase\TestHarness\db_test_harness\node_modules\pg-native\node_modules\libpq\build\Release\addon.exp
  Generating code
  Finished generating code
  addon.vcxproj -> P:\Github\rapidInquiryFacility\rifDatabase\TestHarness\db_test_harness\node_modules\pg-native\node_modules\libpq\build\Release\\addon.node
pg@4.2.0 node_modules\pg
+-- packet-reader@0.2.0
+-- pg-connection-string@0.1.3
+-- buffer-writer@1.0.0
+-- generic-pool@2.1.1
+-- pg-types@1.6.0
+-- semver@4.2.0
+-- pgpass@0.0.3 (split@0.3.3)
```

### NPM (Node package manager) Make integration

#### Make targets

The Makefile will building the requiored Node.js modules

* *all*: Build modules, run the complete database test harness
* *modules*: Build required Node.js modules using *npm install --save* to update dependencies in *package.json*
* *update*: Update required Node.js modules using *npm update --save* to update dependencies in *package.json*
* *db_test_harness*: Run the complete database test harness (same as target test)
* *rif40_create_disease_mapping_example*: Run *rif40_create_disease_mapping_example* test (standard test 1)
* *test*: Run the complete database test harness
* *retest*: Re-run the failed database test harness tests (-F flag)
* *help*: Display makefile help, db_test_harness.js help

# Test Harness Design

The dsatabase layer test harness is driven by two tables:

* RIF40_TEST_RUNS: Test runs
* RIF40_TEST_HARNESS: Tests

The *rif40_test_harness.parent_test_id* column is used to create a series of chained tests within a single transaction.
Tests cannot be shared by multiple transactions; this would require the *rif40_test_harness* to be split into a further
*rif40_test_harness_runs*.

## RIF40_TEST_HARNESS Table

Column | Type | Modifiers | Description
-------|------|-----------|------------
test_id | integer | not null default (nextval( 'rif40_test_id_seq'::regclass ))::integer | Unique investigation index: test_id. Created by SEQUENCE rif40_test_id_seq
parent_test_id | integer | | Parent test ID; NULL means first (test statement). Allows for a string of connected test cases. Multiple inheritance of test cas es is not permitted!
test_run_class | character varying | not null | Test run class; usually the name of the SQL script that originally ran it
test_stmt | character varying | not null | SQL statement for test
test_case_title | character varying | not null | Test case title. Must be unique
pg_error_code_expected | character varying | | [negative] Postgres error SQLSTATE expected [as part of an exception]; passed as PG_EXCEPTION_DETAIL
mssql_error_code_expected | character varying | | Microsoft SQL server error code expected [as part of an exception].
raise_exception_on_failure | boolean | not null default true | Raise exception on failure. NULL means it is expected to NOT raise an exception, raise exception on failure
expected_result | boolean | not null default true | Expected result; tests are allowed to deliberately fail! If the test raises the expection pg_error_code_expected it would normally be expected to pass.
register_date | timestamp with time zone | not null default statement_timestamp() | Date registered
results | text[] | | Results array
results_xml | xml | | Results array in portable XML
pass | boolean | | Was the test passed? Pass means the test passed with no exception if the exception is null or if the exoected exception was cau ght. Note that some tests do fail deliberately to test the harness
test_run_id | integer | | Test run id for test. Foreign key to rif40_test_runs table.
test_date | timestamp with time zone | | Test date
time_taken | numeric | | Time taken for test (seconds)
pg_debug_functions | text[] | | Array of Postgres functions for test harness to enable debug on

Indexes:
* "rif40_test_harness_pk" PRIMARY KEY, btree (test_id)
* "rif40_test_harness_uk" UNIQUE, btree (parent_test_id)

Foreign-key constraints:
* "rif40_test_harness_parent_test_id_fk" FOREIGN KEY (parent_test_id) REFERENCES rif40_test_harness(test_id)
* "rif40_test_harness_test_run_id_fk" FOREIGN KEY (test_run_id) REFERENCES rif40_test_runs(test_run_id)

Referenced by:
* TABLE "rif40_test_harness" CONSTRAINT "rif40_test_harness_parent_test_id_fk" FOREIGN KEY (parent_test_id) REFERENCES rif40_test_harness( test_id)

## RIF40_TEST_RUNS Table
Column | Type | Modifiers | Description
-------|------|-----------|------------
test_run_id | integer | not null default (nextval( 'rif40_test_run_id_seq'::regclass ))::integer | Unique investigation index: test_run_id. Created by SEQUENCE rif40_test_run_id_seq
test_run_title | character varying | not null | Test run title
test_date | timestamp with time zone | not null default statement_timestamp() | Test date
time_taken | numeric | not null default 0 | Time taken for test run (seconds)
username | character varying(90) | not null default "current_user"() | user name running test run
tests_run | integer | not null default 0 | Number of tests run (should equal passed+failed!)
number_passed | integer | not null default 0 | Number of tests passed
number_failed | integer | not null default 0 | Number of tests failed
number_test_cases_registered | integer | not null default 0 | Number of test cases registered [OBSOLETE]
number_messages_registered | integer | not null default 0 | Number of error and informational messages registered

Indexes:
* "rif40_test_runs_pk" PRIMARY KEY, btree (test_run_id)

Referenced by:
* TABLE "rif40_test_harness" CONSTRAINT "rif40_test_harness_test_run_id_fk" FOREIGN KEY (test_run_id) REFERENCES rif40_test_runs(test_run_ id)

## Test Functions

### Function: rif40_sql_pkg.rif40_sql_test()

Parameters:

* SQL test (SELECT or INSERT/UPDATE/DELETE with RETURNING clause) statement,
* Test case title,
* Results 3d text array,
* Results as XML,
* [negative] error SQLSTATE expected [as part of an exception]; the first negative
  number in the message is assumed to be the number;
  NULL means it is expected to NOT raise an exception,
* Raise exception on failure (true/false),
* Test id,
* Array of Postgres functions for test harness to enable debug on,
* Expected result (true/false); pass is true

Returns:

Pass (true)/Fail (false) unless raise_exception_on_failure is TRUE

Description:

Calls _rif40_sql_test() to log and execute dynamic SQL SELECT statement
or INSERT/UPDATE/DELETE with RETURNING clause

Checks expected results against actual; pass if they match, fail if they do not.

Exception behaviour controlled by _rif40_sql_test()

### Function: rif40_sql_pkg._rif40_sql_test()

Parameters:

* SQL test (SELECT or INSERT/UPDATE/DELETE with RETURNING clause) statement,
* test case title,
* Results 3d text array,
* Results as XML,
* [negative] error SQLSTATE expected [as part of an exception]; the first
  negative number in the message is assumed to be the number;
  NULL means it is expected to NOT raise an exception, raise exception on failure,
* Test id,
* Array of Postgres functions for test harness to enable debug on [default NULL]

Returns:

Pass (true)/Fail (false) unless raise_exception_on_failure is TRUE.
Note that this is the result of the test and is not influenced by the expected result:

* To pass:
  * No exception, results as expected;
  * Exception as expected

* Everything else is a fail.

Description:

Log and execute dynamic SQL SELECT statement or INSERT/UPDATE/DELETE with RETURNING clause.
Used to check test SQL statements and triggers

## Transactions

The test harness runs with two connections with independent transactions:

* Connection 1: query up the test list; updates list with results, creates run summary.
* Connection 2: runs each tests, or set of linked tests as a single transaction and rolls
  back the transaction at the end. Does NOT effect the database.

## Linked Tests, Inheritance

The *rif40_test_harness.parent_test_id* column is used to create a series of chained tests within a single transaction.
Tests cannot be shared by multiple transactions; this would require the *rif40_test_harness* to be split into a further
*rif40_test_harness_runs*.

Inheritance is therefore not permitted.

## Use of Async

Node.js is highly asynchronous; as is the Postgres driver (pg). Executing SQL statements results in the
statement becoming queued up and not necessarily running in the same order as submitted to the queue. For loops
have the same effect. This obviously is not good for tranactionaal control. Originally the SQL statements were chained
using the the *cursor.on('end', function(result) {}* functionality; this results in a recursive large stack that grows linearly
per test. To avoid stack issues a Mutux was used so that the for loop could execute in a synchronous manner:

```
//
// This is no longer recursive, replaced with a for loop and a Mutex lock
//

var k = 1;
for (; k <= row_count; k++) {
	(function(p_k) {
		process.nextTick(function() {
			try {
				if (p_k > row_count) {
					console.error('1: run_test_harness_tests() p_k (' + p_k + ') > row_count (' + row_count + ')');
					process.exit(1);
				}
				var p_mutex_id;
				var mutex_name = 'db_test_harness.js-test';
				p_mutexjs.lock(mutex_name, function(id) {
					p_mutex_id=id;
					rif40_sql_test(p_conString, p_mutexjs, p_client1, p_client2, p_k, p_tests,
						p_passed_or_failed, p_failed_flag, p_rif40_test_harness, start_time, p_mutex_id,
						p_rif40_test_harness_results);
				});
			}
			catch(err) {
				console.error('1: _rif40_sql_test_end() Could not acquire Mutex: ' + mutex_name, err);
				process.exit(1);
			}
		});
	})(k);
}
```
Note:

* Use of process.nextTick() to slow big loops and reduce stack stress
* This code is from run_test_harness_tests()
* The mutex is released in _rif40_sql_test_end() when the test case is rolled back.

## Code Portablility

The following are issues with code portability to Microsoft SQL Server:

* Use of unnest() and array type functionality in the standard SAHSULAND test exmaple
* No support for SQL server debugging functions
* Use of RETURNING in _end_test_harness() INSERT INTO rif40_tests_runs SQL.
* Potential fix (use of RETURNING) to rif40_sql_pkg._rif40_sql_test() so the SQL runs once (i.e. use capture the results). This avoids issues with functions
  (e.g. rif40_run)_study() that errors if run more than once.
* Node.js drivers (Azure/Tedious) and Postgres have differing interface. There is no common DB abstraction
  (apart from any-db for Postgres, MySQL and SQLLite3) as in Perl, PGP etc.

## Test Examples

These are all Postgres examples.

a) SELECT statements:

Actual test INSERT code:

{% raw %}
```
	PERFORM rif40_sql_pkg._rif40_sql_test_register(
                 'SELECT level1, level2, level3, level4 FROM sahsuland_geography WHERE level3 IN (''01.015.016900'', ''01.015.016200'') ORDER BY level4',
				 'test_8_triggers.sql',
                 'T8--07: test_8_triggers.sql: Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200',
 '{{01,01.015,01.015.016200,01.015.016200.2}
 ,{01,01.015,01.015.016200,01.015.016200.3}
 ,{01,01.015,01.015.016200,01.015.016200.4}
 ,{01,01.015,01.015.016900,01.015.016900.1}
 ,{01,01.015,01.015.016900,01.015.016900.2}
 ,{01,01.015,01.015.016900,01.015.016900.3}
 }'::Text[][],
 '<row xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
   <level1>01</level1>
   <level2>01.015</level2>
   <level3>01.015.016200</level3>
   <level4>01.015.016200.2</level4>
 </row>
 <row xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
   <level1>01</level1>
   <level2>01.015</level2>
   <level3>01.015.016200</level3>
   <level4>01.015.016200.3</level4>
 </row>
 <row xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
   <level1>01</level1>
   <level2>01.015</level2>
   <level3>01.015.016200</level3>
   <level4>01.015.016200.4</level4>
 </row>
 <row xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
   <level1>01</level1>
   <level2>01.015</level2>
   <level3>01.015.016900</level3>
   <level4>01.015.016900.1</level4>
 </row>
 <row xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
   <level1>01</level1>
   <level2>01.015</level2>
   <level3>01.015.016900</level3>
   <level4>01.015.016900.2</level4>
 </row>
 <row xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
   <level1>01</level1>
   <level2>01.015</level2>
   <level3>01.015.016900</level3>
   <level4>01.015.016900.3</level4>
 </row>'::XML);
```
{% endraw %}
Example runtime code:

{% raw %}
```
IF NOT (rif40_sql_pkg.rif40_sql_test(
	'SELECT level1, level2, level3, level4 FROM sahsuland_geography WHERE level3 IN (''01.015.016900'', ''01.015.016200'') ORDER BY level4',
	'Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200',
	'{{01,01.015,01.015.016200,01.015.016200.2}
	,{01,01.015,01.015.016200,01.015.016200.3}
	,{01,01.015,01.015.016200,01.015.016200.4}
	,{01,01.015,01.015.016900,01.015.016900.1}
	,{01,01.015,01.015.016900,01.015.016900.2}
	,{01,01.015,01.015.016900,01.015.016900.3}
	}'::Text[][]
	/* Use defaults */)) THEN
	errors:=errors+1;
END IF;
```
{% endraw %}

Example output:
```
psql:test_scripts/test_8_triggers.sql:276: INFO:  rif40_method4():
Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200
---------------------------------------------------------------------
psql:test_scripts/test_8_triggers.sql:276: INFO:  rif40_method4():
level1                                   | level2                                   | level3                                   | level4
----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
01                                       | 01.015                                   | 01.015.016200                            | 01.015.016200.2
01                                       | 01.015                                   | 01.015.016200                            | 01.015.016200.3
01                                       | 01.015                                   | 01.015.016200                            | 01.015.016200.4
01                                       | 01.015                                   | 01.015.016900                            | 01.015.016900.1
01                                       | 01.015                                   | 01.015.016900                            | 01.015.016900.2
01                                       | 01.015                                   | 01.015.016900                            | 01.015.016900.3
(6 rows)
psql:test_scripts/test_8_triggers.sql:276: INFO:  rif40_sql_test(): [71152] PASSED: no extra rows for test: Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200
psql:test_scripts/test_8_triggers.sql:276: INFO:  rif40_sql_test(): [71155] PASSED: no missing rows for test: Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200
psql:test_scripts/test_8_triggers.sql:276: INFO:  rif40_sql_test(): [71158] PASSED: Test case: Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200 no exceptions, no errors, no missing or extra data
```

i)   Original SQL statement:
	SELECT * FROM sahsuland_geography WHERE level3 IN ('01.015.016900', '01.015.016200');
ii)  Add ORDER BY clause, expand * (This becomes the test case SQL)
	SELECT level1, level2, level3, level4 FROM sahsuland_geography WHERE level3 IN ('01.015.016900', '01.015.016200') ORDER BY level4;
iii) Convert results to array form (Cast to text, string )
	[the function rif40_sql_pkg._rif40_test_sql_template() will automate this]

{% raw %}
```
	SELECT ''''||
		   REPLACE(ARRAY_AGG(
				(ARRAY[level1::Text, level2::Text, level3::Text, level4::Text]::Text||E'\n')::Text ORDER BY level4)::Text,
				'"'::Text, ''::Text)||'''::Text[][]' AS res
	  FROM sahsuland_geography
	 WHERE level3 IN ('01.015.016900', '01.015.016200');

						 res
	---------------------------------------------
	 '{{01,01.015,01.015.016200,01.015.016200.2}+
	 ,{01,01.015,01.015.016200,01.015.016200.3} +
	 ,{01,01.015,01.015.016200,01.015.016200.4} +
	 ,{01,01.015,01.015.016900,01.015.016900.1} +
	 ,{01,01.015,01.015.016900,01.015.016900.2} +
	 ,{01,01.015,01.015.016900,01.015.016900.3} +
	 }'::Text[][]
	(1 row)
```
{% endraw %}

Example call:

{% raw %}
```
	PERFORM rif40_sql_pkg.rif40_sql_test(
		'SELECT level1, level2, level3, level4 FROM sahsuland_geography WHERE level3 IN (''01.015.016900'', ''01.015.016200'') ORDER BY level4',
		'Display SAHSULAND hierarchy for level 3: 01.015.016900, 01.015.016200',
		'{{01,01.015,01.015.016200,01.015.016200.2}
		,{01,01.015,01.015.016200,01.015.016200.3}
		,{01,01.015,01.015.016200,01.015.016200.4}
		,{01,01.015,01.015.016900,01.015.016900.1}
		,{01,01.015,01.015.016900,01.015.016900.2}
		,{01,01.015,01.015.016900,01.015.016900.3}
		}'::Text[][]);
```
{% endraw %}

Example expand of array to setof record

{% raw %}
```
		WITH a AS (
			SELECT '{{01,01.015,01.015.016200,01.015.016200.2}
		,{01,01.015,01.015.016200,01.015.016200.3}
		,{01,01.015,01.015.016200,01.015.016200.4}
		,{01,01.015,01.015.016900,01.015.016900.1}
		,{01,01.015,01.015.016900,01.015.016900.2}
		,{01,01.015,01.015.016900,01.015.016900.3}
		}'::Text[][] AS res
		), row AS (
			SELECT generate_series(1,array_upper(a.res, 1)) AS series
			  FROM a
		)
		SELECT  row.series,
				(a.res)[row.series][1] AS level1,
				(a.res)[row.series][2] AS level2,
				(a.res)[row.series][3] AS level3,
				(a.res)[row.series][4] AS level4
		  FROM row, a;

	WITH a AS ( /* Test data */
		SELECT '{{01,01.015,01.015.016200,01.015.016200.2}
			,{01,01.015,01.015.016200,01.015.016200.3}
			,{01,01.015,01.015.016200,01.015.016200.4}
			,{01,01.015,01.015.016900,01.015.016900.1}
			,{01,01.015,01.015.016900,01.015.016900.2}
			,{01,01.015,01.015.016900,01.015.016900.3}
			}'::Text[][] AS res
	), b AS ( /* Test SQL */
		SELECT level1, level2, level3, level4
		  FROM sahsuland_geography WHERE level3 IN ('01.015.016900', '01.015.016200')
		 ORDER BY level4
	), c AS ( /* Convert to 2D array via record */
		SELECT REPLACE(
					REPLACE(
						REPLACE(
								ARRAY_AGG(b.*)::Text,
								'"'::Text, ''::Text),
							'('::Text, '{'::Text),
						')'::Text, '}'::Text)::Text[][] AS res
		FROM b
	)
	SELECT rif40_sql_pkg._rif40_reduce_dim(c.res) AS missing_data
	  FROM c
	EXCEPT
	SELECT rif40_sql_pkg._rif40_reduce_dim(a.res)
	  FROM a;
```
{% endraw %}

b) TRIGGERS

These use INSERT/UPDATE OR DELETE statements. RETURNING is supported, but it must be a single
text value (test_value). The results array should should also be  single value. Beware that the
INSERTed data from the table is not in scope, so you can return a sequence, an input value, but not trigger modified data

Example code:
```
	IF NOT (rif40_sql_pkg.rif40_sql_test(
		'INSERT INTO rif40_studies(geography, project, study_name, extract_table, map_table, study_type, comparison_geolevel_name, study_geolevel_name, denom_tab, suppression_value)
VALUES (''SAHSU'', ''TEST'', ''TRIGGER TEST #1'', ''EXTRACT_TRIGGER_TEST_1'', ''MAP_TRIGGER_TEST_1'', 1 /* Disease mapping */, ''LEVEL1'', ''LEVEL4'', NULL /* FAIL HERE */, 0)',
		'TRIGGER TEST #1: rif40_studies.denom_tab IS NULL',
		NULL::Text[][] 	/* No results for trigger */,
		'P0001' 		/* Expected SQLCODE (P0001 - PGpsql raise_exception (from rif40_error) */,
		FALSE 			/* Do not RAISE EXCEPTION on failure */)) THEN
		errors:=errors+1;
    END IF;
```

Example output:
```
psql:test_scripts/test_8_triggers.sql:276: WARNING:  rif40_ddl(): SQL in error (P0001)> INSERT INTO rif40_studies(geography, project, study_name, extract_table, map_table, study_type, comparison_geolevel_name, study_geolevel_name, denom_tab, suppression_value)
VALUES ('SAHSU', 'TEST', 'TRIGGER TEST #1', 'EXTRACT_TRIGGER_TEST_1', 'MAP_TRIGGER_TEST_1', 1 /* Diease mapping */, 'LEVEL1', 'LEVEL4', NULL /* FAIL HERE */, 0);
psql:test_scripts/test_8_triggers.sql:276: WARNING:  71167: rif40_sql_test('TRIGGER TEST #1: rif40_studies.denom_tab IS NULL') caught:
rif40_trg_pkg.trigger_fct_t_rif40_studies_checks(): T_RIF40_STUDIES study 140 denominator:  not found in RIF40_TABLES in SQL >>>
INSERT INTO rif40_studies(geography, project, study_name, extract_table, map_table, study_type, comparison_geolevel_name, study_geolevel_name, denom_tab, suppression_value)
VALUES ('SAHSU', 'TEST', 'TRIGGER TEST #1', 'EXTRACT_TRIGGER_TEST_1', 'MAP_TRIGGER_TEST_1', 1 /* Diease mapping */, 'LEVEL1', 'LEVEL4', NULL /* FAIL HERE */, 0);
<<<
Error context and message >>>
Message:  rif40_trg_pkg.trigger_fct_t_rif40_studies_checks(): T_RIF40_STUDIES study 140 denominator:  not found in RIF40_TABLES
Hint:     Consult message text
Detail:   -20211
Context:  SQL statement "SELECT rif40_log_pkg.rif40_error(-20211, 'trigger_fct_t_rif40_studies_checks',
                        'T_RIF40_STUDIES study % denominator: % not found in RIF40_TABLES',
                        NEW.study_id::VARCHAR           /* Study id */,
                        NEW.denom_tab::VARCHAR          /* Denominator */)"
PL/pgSQL function rif40_trg_pkg.trigger_fct_t_rif40_studies_checks() line 460 at PERFORM
SQL statement "INSERT INTO t_rif40_studies (
                                username,
                                study_id,
                                extract_table,
                                study_name,
                                summary,
                                description,
                                other_notes,
                                study_date,
                                geography,
                                study_type,
                                study_state,
                                comparison_geolevel_name,
                                denom_tab,
                                direct_stand_tab,
                                study_geolevel_name,
                                map_table,
                                suppression_value,
                                extract_permitted,
                                transfer_permitted,
                                authorised_by,
                                authorised_on,
                                authorised_notes,
                                audsid,
                                project)
                        VALUES(
                                coalesce(NEW.username, "current_user"()),
                                coalesce(NEW.study_id, (nextval('rif40_study_id_seq'::regclass))::integer),
                                NEW.extract_table /* no default value */,
                                NEW.study_name /* no default value */,
                                NEW.summary /* no default value */,
                                NEW.description /* no default value */,
                                NEW.other_notes /* no default value */,
                                coalesce(NEW.study_date, ('now'::text)::timestamp without time zone),
                                NEW.geography /* no default value */,
                                NEW.study_type /* no default value */,
                                coalesce(NEW.study_state, 'C'::character varying),
                                NEW.comparison_geolevel_name /* no default value */,
                                NEW.denom_tab /* no default value */,
                                NEW.direct_stand_tab /* no default value */,
                                NEW.study_geolevel_name /* no default value */,
                                NEW.map_table /* no default value */,
                                NEW.suppression_value /* no default value */,
                                coalesce(NEW.extract_permitted, 0),
                                coalesce(NEW.transfer_permitted, 0),
                                NEW.authorised_by /* no default value */,
                                NEW.authorised_on /* no default value */,
                                NEW.authorised_notes /* no default value */,
                                coalesce(NEW.audsid, sys_context('USERENV'::character varying, 'SESSIONID'::character varying)),
                                NEW.project /* no default value */)"
PL/pgSQL function rif40_trg_pkg.trgf_rif40_studies() line 8 at SQL statement
SQL statement "INSERT INTO rif40_studies(geography, project, study_name, extract_table, map_table, study_type, comparison_geolevel_name, study_geolevel_name, denom_tab, suppression_value)
VALUES ('SAHSU', 'TEST', 'TRIGGER TEST #1', 'EXTRACT_TRIGGER_TEST_1', 'MAP_TRIGGER_TEST_1', 1 /* Diease mapping */, 'LEVEL1', 'LEVEL4', NULL /* FAIL HERE */, 0)"
PL/pgSQL function rif40_ddl(character varying) line 51 at EXECUTE statement
SQL statement "SELECT rif40_sql_pkg.rif40_ddl(test_stmt)"
PL/pgSQL function rif40_sql_test(character varying,character varying,anyarray,character varying,boolean) line 253 at PERFORM
PL/pgSQL function inline_code_block line 157 at IF
SQLSTATE: P0001
<<< End of trace.

psql:test_scripts/test_8_triggers.sql:276: WARNING:  rif40_sql_test(): [71169] Test case: TRIGGER TEST #1: rif40_studies.denom_tab IS NULL PASSED, caught expecting SQLSTATE P0001;
Message:  rif40_trg_pkg.trigger_fct_t_rif40_studies_checks(): T_RIF40_STUDIES study 140 denominator:  not found in RIF40_TABLES
Detail:   -20211
```

## Success and failure in tests

Success or failure is determined by *rif40_test_harness.pass*; the expected result:

* TRUE - The test ran ok: :+1: or *rif40_test_harness.pg_error_code_expected* [negative] matches the Postgres
          error SQLSTATE expected [as part of an exception]; passed as PG_EXCEPTION_DETAIL in Postgres.
* FALSE - The test failed: :-1: either the test ran OK when it was expected to raise an exception or it
          it raised a different exception to that expected. **Tests are allowed to deliberately fail!**. This
		  is used to test the test harness.

## To do

* Per test logging to separate files.
* Remove *rif40_test_runs_.number_test_cases_registered*.
* Add *rif40_test_harness.port_specific_test*; either: P (Postgres only) or: S (SQL Server only).

## Bugs

* Fix rif40_sql_pkg._rif40_sql_test() so the SQL runs once (i.e. use capture the results). This avoids issues with functions
  (e.g. rif40_run)_study() that errors if run more than once.

## Potential future enhancements; lowest priority

* Support for multiple users and user types; e.g. *notarifuser*.
* Constraints:

    a) NOT null
    b) Check i) correct; ii) incorrect
    c) Primary key i) correct; ii) duplicate iii) missing parent

* Access control:

    d) notarifuser access

# Usage

``` node
node db_test_harness.js --help
Usage: test_harness [options]

Version: 0.1

RIF 4.0 Database test harness.

Options:
  -d, --debug_level  RIF database PL/pgsql debug level      [default: 0]
  -D, --database     name of Postgres database              [default: "sahsuland_dev"]
  -U, --username     Postgres database username             [default: "pch"]
  -P, --port         Postgres database port                 [default: 5432]
  -H, --hostname     hostname of Postgres database          [default: "wpea-rif1"]
  -F, --failed       re-run failed tests                    [default: false]
  -C, --class        Test run class                         [default: null]
  --help             display this helpful message and exit  [default: false]
```

## Example: rif40_create_disease_mapping_example

{% raw %}
``` node
C:\Users\pch\Documents\GitHub\rapidInquiryFacility\rifDatabase\TestHarness\db_test_harness>make rif40_create_disease_mapping_example
Debug level set to default: 1
node db_test_harness.js -H localhost -D sahsuland_dev -U pch -d 1 -C rif40_create_disease_mapping_example
1: Connected to Postgres using: postgres://pch@localhost:5432/sahsuland_dev?application_name=db_test_harness
1: notice: rif40_log_setup() DEFAULTED send DEBUG to INFO: off; debug function list: []
1: notice: rif40_startup(): SQL> SET search_path TO pch,rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies,
rif40_partitions,rif_studies;
1: notice: rif40_startup(): Created temporary table: g_rif40_study_areas
1: notice: rif40_startup(): Created temporary table: g_rif40_comparison_areas
1: notice: Function postgis_topology_scripts_installed() not found. Is topology support enabled and topology.sql installed?
1: notice: rif40_startup(): PostGIS extension V2.1.3 (POSTGIS="2.1.3 r12547" GEOS="3.4.2-CAPI-1.8.2 r3924" PROJ="Rel. 4.8.0, 6 March 2012" G
DAL="GDAL 1.10.0, released 2013/04/24" LIBXML="2.7.8" LIBJSON="UNKNOWN" RASTER)
1: notice: rif40_startup(): FDW functionality disabled - FDWServerName, FDWServerType, FDWDBServer RIF parameters not set.
1: notice: rif40_startup(): V$Revision: 1.11 $ DB version $Revision: 1.11 $ matches
1: notice: rif40_startup(): V$Revision: 1.11 $ rif40_geographies, rif40_tables, rif40_health_study_themes exist for user: pch
1: notice: rif40_startup(): search_path: rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partition
s, rif_studies, reset: rif40, public, topology, gis, pop, rif_data, data_load, rif40_sql_pkg, rif_studies, rif40_partitions
1: notice: rif40_startup(): Deleted 0, created 2 tables/views/foreign data wrapper tables
1: notice: rif40_log_setup() send DEBUG to INFO: off; debug function list: []
1: notice: _rif40_sql_test_log_setup(): [71402]: debug_level 1
1: notice: rif40_send_debug_to_info(true) SET send DEBUG to INFO: off; debug function list: [_rif40_sql_test_log_setup:DEBUG1]
1: notice: _rif40_sql_test_log_setup(): [71403]: Enable debug for function: rif40_delete_study
1: notice: _rif40_sql_test_log_setup(): [71403]: Enable debug for function: rif40_ddl
1: notice: _rif40_sql_test_log_setup(): [71403]: Enable debug for function: _rif40_sql_test
1: notice: _rif40_sql_test_log_setup(): [71403]: Enable debug for function: _rif40_test_sql_template
1: notice: _rif40_sql_test_log_setup(): [71403]: Enable debug for function: _rif40_sql_test_register
1: notice: _rif40_sql_test_log_setup(): [71403]: Enable debug for function: rif40_startup
1: notice: _rif40_sql_test_log_setup(): [71403]: Enable debug for function: _rif40_sql_test_log_setup
1: notice: _rif40_sql_test_log_setup(): [71403]: Enable debug for function: rif40_remove_from_debug
1: Wait for client 2 initialisation; debug_level: 1
2: Connect to Postgres using: postgres://pch@localhost:5432/sahsuland_dev?application_name=db_test_harness
1: Client 2 initialised; debug_level: 1
2: COMMIT transaction;
1: Class: rif40_create_disease_mapping_example Tests: 7
1: Total tests to run: 7
1: [1/7]: rif40_create_disease_mapping_example] SAHSULAND test 4 study_id 1 example
2: BEGIN transaction: [1/7]: rif40_create_disease_mapping_example] SAHSULAND test 4 study_id 1 example
2[9:11:29 AM 563] notice: _rif40_sql_test(): [71250]: Enable debug for function: trigger_fct_t_rif40_studies_checks
2[9:11:29 AM 783] notice: [DEBUG1] rif40_ddl(): SQL> INSERT /* 1 */ INTO rif40_studies (
                geography, project, study_name, study_type,
                comparison_geolevel_name, study_geolevel_name, denom_tab,
                year_start, year_stop, max_age_group, min_age_group,
                suppression_value, extract_permitted, transfer_permitted)
        VALUES (
                 'SAHSU'                                        /* geography */,
                 'TEST'                                                 /* project */,
                 'SAHSULAND test 4 study_id 1 example'                                  /* study_name */,
                 1                                                                      /* study_type [disease mapping] */,
                 'LEVEL2'       /* comparison_geolevel_name */,
                 'LEVEL4'                               /* study_geolevel_name */,
                 'SAHSULAND_POP'                                        /* denom_tab */,
                 1989                                   /* year_start */,
                 1996                                   /* year_stop */,
                 21     /* max_age_group */,
                 0      /* min_age_group */,
                 5 /* suppression_value */,
                 1              /* extract_permitted */,
                 1              /* transfer_permitted */);
2[9:11:29 AM 961] notice: [DEBUG1] trigger_fct_t_rif40_studies_checks(): [20200-5] T_RIF40_STUDIES study 13 CRUD checks OK
2[9:11:30 AM 74] notice: [DEBUG1] trigger_fct_t_rif40_studies_checks(): [20206-7] T_RIF40_STUDIES study 13 comparison area geolevel name: "L
EVEL2" OK
2[9:11:30 AM 169] notice: [DEBUG1] trigger_fct_t_rif40_studies_checks(): [20209] T_RIF40_STUDIES study 13 study area geolevel name: "LEVEL4"
 OK
2[9:11:31 AM 283] notice: [DEBUG1] trigger_fct_t_rif40_studies_checks(): [20212] T_RIF40_STUDIES study 13 denominator accessible as: pop.SAH
SULAND_POP
2[9:11:31 AM 377] notice: [DEBUG1] trigger_fct_t_rif40_studies_checks(): [20216-21] T_RIF40_STUDIES study 13 year/age bands checks OK agains
t RIF40_TABLES
2[9:11:31 AM 470] notice: [DEBUG1] trigger_fct_t_rif40_studies_checks(): [20222] T_RIF40_STUDIES study 13 study area geolevel id (4/LEVEL4)
>= comparision area (2/LEVEL2) [i.e study area has the same or higher resolution]
2[9:11:31 AM 563] notice: [DEBUG1] trigger_fct_t_rif40_studies_checks(): [20223] T_RIF40_STUDIES study 13 suppressed at: 5
2[9:11:31 AM 655] notice: [DEBUG1] trigger_fct_t_rif40_studies_checks(): [20224-5] T_RIF40_STUDIES study 13 may be extracted, study geolevel
 LEVEL4 is not restricted for user: pch
2[9:11:33 AM 913] notice: [DEBUG1] trigger_fct_t_rif40_studies_checks(): [20228] T_RIF40_STUDIES study 13 extract table: S13_EXTRACT cannot
be accessed; state: C [IGNORED]
2[9:11:36 AM 148] notice: [DEBUG1] trigger_fct_t_rif40_studies_checks(): [20229] T_RIF40_STUDIES study 13 map table: S13_MAP cannot be acces
sed; state: C [IGNORED]
2[9:11:36 AM 279] notice: [DEBUG1] trigger_fct_t_rif40_studies_checks(): [20233] T_RIF40_STUDIES study: 13 denominator RIF40_TABLES age sex
group field column: pop.SAHSULAND_POP.AGE_SEX_GROUP found
2[9:11:36 AM 396] notice: [DEBUG1] rif40_ddl(): Statement took: 00:00:06.611, proccessed 1 rows
2[9:11:36 AM 403] notice: _rif40_sql_test(): [71267] PASSED: Test case 1: SAHSULAND test 4 study_id 1 example no exceptions, no error expect
ed
2[9:11:36 AM 561] notice: rif40_sql_test(): [71150]: Test case 1: SAHSULAND test 4 study_id 1 example
PASSED expected result = actual (true)
*****************************************************************************
*
* 2: [Recursive test 1/7] Test OK: [1/7]: rif40_create_disease_mapping_example] SAHSULAND test 4 study_id 1 example
*
*****************************************************************************

1: DEPENDENT Recurse; next: 2
1: RECURSE level 1: [2/7]: rif40_create_disease_mapping_example] SAHSULAND test 4 study_id 1 example
2[9:11:36 AM 575] notice: _rif40_sql_test(): [71250]: Enable debug for function: trigger_fct_t_rif40_investigations_checks
2[9:11:36 AM 792] notice: [DEBUG1] rif40_ddl(): SQL> INSERT /* 2 */ INTO rif40_investigations(
        inv_name,
        inv_description,
        genders,
        numer_tab,
        year_start,
        year_stop,
        max_age_group,
        min_age_group
)
VALUES (
        'INV_1'                 /* inv_name */,
        'Lung cancer'   /* inv_description */,
        3                       /* genders [both] */,
        'SAHSULAND_CANCER'              /* numer_tab */,
        1989            /* year_start */,
        1996            /* year_stop */,
        21 /* max_age_group */,
        0 /* min_age_group */);
2[9:11:36 AM 901] notice: [DEBUG1] trigger_fct_t_rif40_investigations_checks(): [20700-4] T_RIF40_INVESTIGATIONS study: 13 investigation: 9
CRUD checks OK
2[9:11:38 AM 169] notice: [DEBUG1] trigger_fct_t_rif40_investigations_checks(): [20706] T_RIF40_INVESTIGATIONS study: 13 investigation: 9 nu
merator: SAHSULAND_CANCER accessible
2[9:11:38 AM 266] notice: [DEBUG1] trigger_fct_t_rif40_investigations_checks(): [20707-17] T_RIF40_INVESTIGATIONS study: 13 investigation: 9
 year/age checks OK
2[9:11:38 AM 537] notice: [DEBUG1] trigger_fct_t_rif40_investigations_checks(): [20718] T_RIF40_INVESTIGATIONS study: 13 investigation: 9 nu
merator RIF40_TABLES total field column: rif_data.SAHSULAND_CANCER.TOTAL found
2[9:11:38 AM 638] notice: [DEBUG1] trigger_fct_t_rif40_investigations_checks(): [20721] T_RIF40_INVESTIGATIONS study: 13 investigation: 9 nu
merator RIF40_TABLES age sex group field column: rif_data.SAHSULAND_CANCER.AGE_SEX_GROUP found
2[9:11:38 AM 769] notice: [DEBUG1] trigger_fct_t_rif40_investigations_checks(): [20722-5] T_RIF40_INVESTIGATIONS study: 13 investigation: 9
age_group IDs match; 1, same AGE_SEX_GROUP/AGE_GROUP/SEX_FIELD_NAMES used
2[9:11:38 AM 865] notice: [DEBUG1] rif40_ddl(): Statement took: 00:00:02.066, proccessed 1 rows
2[9:11:38 AM 872] notice: _rif40_sql_test(): [71267] PASSED: Test case 2: SAHSULAND test 4 study_id 1 example no exceptions, no error expect
ed
2[9:11:39 AM 34] notice: rif40_sql_test(): [71150]: Test case 2: SAHSULAND test 4 study_id 1 example
PASSED expected result = actual (true)
*****************************************************************************
*
* 2: [Recursive test 2/7] Test OK: [2/7]: rif40_create_disease_mapping_example] SAHSULAND test 4 study_id 1 example
*
*****************************************************************************

1: DEPENDENT Recurse; next: 3
1: RECURSE level 2: [3/7]: rif40_create_disease_mapping_example] SAHSULAND test 4 study_id 1 example
2[9:11:39 AM 46] notice: _rif40_sql_test(): [71250]: Enable debug for function: trigger_fct_t_rif40_inv_conditions_checks
2[9:11:39 AM 265] notice: [DEBUG1] rif40_ddl(): SQL> INSERT /* 3 */ INTO rif40_inv_conditions(
                        outcome_group_name, min_condition, max_condition, predefined_group_name, line_number)
WITH data AS (
                                SELECT '{{SAHSULAND_ICD,C34,NULL,NULL},{SAHSULAND_ICD,162,1629,NULL}}'::Text[][] AS arr
                        ), b AS (
                                SELECT arr[i][1] AS outcome_group_name,
                                           arr[i][2] AS min_condition,
                                           arr[i][3] AS max_condition,
                                           arr[i][4] AS predefined_group_name,
                   ROW_NUMBER() OVER() AS line_number
                              FROM data, generate_subscripts((SELECT arr FROM data), 1) i
                        )
                SELECT outcome_group_name, min_condition, max_condition, predefined_group_name, line_number
                  FROM b
                RETURNING outcome_group_name, min_condition, max_condition, predefined_group_name;
2[9:11:39 AM 371] notice: [DEBUG1] trigger_fct_t_rif40_inv_conditions_checks(): [20500-3] T_RIF40_INV_CONDITIONS study: 13 investigation: 9
line: 1 CRUD checks OK
2[9:11:39 AM 485] notice: [DEBUG1] trigger_fct_t_rif40_inv_conditions_checks(): [20500-3] T_RIF40_INV_CONDITIONS study: 13 investigation: 9
line: 2 CRUD checks OK
2[9:11:39 AM 652] notice: _rif40_sql_test(): [71267] PASSED: Test case 3: SAHSULAND test 4 study_id 1 example no exceptions, no error expect
ed
2[9:11:39 AM 812] notice: rif40_sql_test(): [71150]: Test case 3: SAHSULAND test 4 study_id 1 example
PASSED expected result = actual (true)
*****************************************************************************
*
* 2: [Recursive test 3/7] Test OK: [3/7]: rif40_create_disease_mapping_example] SAHSULAND test 4 study_id 1 example
*
*****************************************************************************

1: DEPENDENT Recurse; next: 4
1: RECURSE level 3: [4/7]: rif40_create_disease_mapping_example] SAHSULAND test 4 study_id 1 example
2[9:11:39 AM 825] notice: _rif40_sql_test(): [71250]: Enable debug for function: trigger_fct_t_rif40_study_areas_checks
2[9:11:39 AM 975] notice: _rif40_sql_test(): [71250]: Enable debug for function: trigger_fct_t_rif40_study_areas_checks2
2[9:11:40 AM 229] notice: [DEBUG1] rif40_ddl(): SQL> INSERT /* 4 */ INTO rif40_study_areas(area_id, band_id)
SELECT DISTINCT level4, ROW_NUMBER() OVER() AS band_id
          FROM sahsuland_geography
         WHERE level3 IN (
        SELECT unnest(
'{01.001.000100,01.001.000200,01.001.000300,01.002.000300,01.002.000400,01.002.000500,01.002.000600,01.002.000700,01.002.000800,01.002.00090
0,01.002.001000,01.002.001100,01.002.001200,01.002.001300,01.002.001400,01.002.001500,01.002.001600,01.002.001700,01.002.001800,01.002.00190
0,01.002.002000,01.002.002100,01.002.002200,01.002.002300,01.003.003300,01.003.003400,01.004.011100,01.004.011200,01.004.011300,01.004.01140
0,01.004.011500,01.004.011600,01.004.011700,01.004.011800,01.004.011900,01.005.002400,01.006.002500,01.006.002600,01.006.002700,01.007.01200
0,01.007.012100,01.007.012200,01.007.012300,01.007.012400,01.007.012500,01.008.001000,01.008.002900,01.008.003500,01.008.003600,01.008.00370
0,01.008.003800,01.008.003900,01.008.004000,01.008.004100,01.008.004200,01.008.004300,01.008.004400,01.008.004500,01.008.004600,01.008.00470
0,01.008.004800,01.008.004900,01.008.005000,01.008.005100,01.008.005200,01.008.005300,01.008.005400,01.008.005500,01.008.005600,01.008.00570
0,01.008.005800,01.008.005900,01.008.006000,01.008.006100,01.008.006200,01.008.006300,01.008.006400,01.008.006500,01.008.006600,01.008.00670
0,01.008.006800,01.008.006900,01.008.007000,01.008.007100,01.008.007200,01.008.007300,01.008.007400,01.008.007500,01.008.007600,01.008.00770
0,01.008.007800,01.008.007900,01.008.008000,01.008.008100,01.008.008200,01.008.008300,01.008.008400,01.008.008500,01.008.008600,01.008.00870
0,01.008.008800,01.008.008900,01.008.009000,01.008.009100,01.008.009200,01.008.009300,01.008.009400,01.008.009500,01.008.009600,01.008.00970
0,01.008.009800,01.008.009900,01.008.010100,01.008.010200,01.008.010300,01.008.010400,01.008.010500,01.008.010600,01.008.010700,01.008.01080
0,01.008.010900,01.008.011000,01.009.002700,01.009.002800,01.009.002900,01.009.003000,01.009.003100,01.009.003200,01.011.012600,01.011.01270
0,01.011.012800,01.011.012900,01.011.013000,01.011.013100,01.011.013200,01.011.013300,01.011.013400,01.011.013500,01.011.013600,01.011.01370
0,01.011.013800,01.011.013900,01.011.014000,01.011.014100,01.011.014200,01.011.014300,01.011.014400,01.011.014500,01.011.014600,01.011.01470
0,01.011.014800,01.011.014900,01.011.015000,01.011.015100,01.011.015200,01.011.015300,01.011.015400,01.011.015500,01.011.015600,01.011.01570
0,01.011.015800,01.012.003000,01.012.015900,01.012.016000,01.013.016000,01.013.016100,01.013.016200,01.013.016300,01.013.016400,01.013.01650
0,01.013.016600,01.013.016700,01.013.016800,01.014.017200,01.014.017300,01.014.017500,01.014.017600,01.014.017700,01.014.017800,01.014.01790
0,01.014.018000,01.014.018100,01.014.018200,01.014.018300,01.014.018400,01.014.018500,01.014.018600,01.014.018700,01.014.018800,01.015.01620
0,01.015.016900,01.016.017000,01.016.017100,01.017.018900,01.017.019000,01.018.019100,01.018.019200,01.018.019300,01.018.019400,01.018.01950
0}'::Text[]) /* at Geolevel select */ AS study_area);
2[9:11:41 AM 969] notice: [DEBUG1] rif40_ddl(): Statement took: 00:00:01.737, proccessed 1230 rows
2[9:11:41 AM 977] notice: _rif40_sql_test(): [71267] PASSED: Test case 4: SAHSULAND test 4 study_id 1 example no exceptions, no error expect
ed
2[9:11:42 AM 311] notice: rif40_sql_test(): [71150]: Test case 4: SAHSULAND test 4 study_id 1 example
PASSED expected result = actual (true)
*****************************************************************************
*
* 2: [Recursive test 4/7] Test OK: [4/7]: rif40_create_disease_mapping_example] SAHSULAND test 4 study_id 1 example
*
*****************************************************************************

1: DEPENDENT Recurse; next: 5
1: RECURSE level 4: [5/7]: rif40_create_disease_mapping_example] SAHSULAND test 4 study_id 1 example
2[9:11:42 AM 328] notice: _rif40_sql_test(): [71250]: Enable debug for function: trigger_fct_t_rif40_comp_areas_checks
2[9:11:42 AM 474] notice: _rif40_sql_test(): [71250]: Enable debug for function: trigger_fct_t_rif40_comp_areas_checks2
2[9:11:42 AM 727] notice: [DEBUG1] rif40_ddl(): SQL> INSERT /* 5 */ INTO rif40_comparison_areas(area_id)
SELECT unnest(
'{01.001,01.002,01.003,01.004,01.005,01.006,01.007,01.008,01.009,01.011,01.012,01.013,01.014,01.015,01.016,01.017,01.018}'::Text[]) AS compa
rision_area;
2[9:11:46 AM 437] notice: [DEBUG1] rif40_ddl(): Statement took: 00:00:03.707, proccessed 17 rows
2[9:11:46 AM 448] notice: _rif40_sql_test(): [71267] PASSED: Test case 5: SAHSULAND test 4 study_id 1 example no exceptions, no error expect
ed
2[9:11:46 AM 780] notice: rif40_sql_test(): [71150]: Test case 5: SAHSULAND test 4 study_id 1 example
PASSED expected result = actual (true)
*****************************************************************************
*
* 2: [Recursive test 5/7] Test OK: [5/7]: rif40_create_disease_mapping_example] SAHSULAND test 4 study_id 1 example
*
*****************************************************************************

1: DEPENDENT Recurse; next: 6
1: RECURSE level 5: [6/7]: rif40_create_disease_mapping_example] SAHSULAND test 4 study_id 1 example
2[9:11:46 AM 794] notice: _rif40_sql_test(): [71250]: Enable debug for function: trigger_fct_t_rif40_inv_covariates_checks
2[9:11:47 AM 15] notice: [DEBUG1] rif40_ddl(): SQL> INSERT /* 6 */ INTO rif40_inv_covariates(geography, covariate_name, study_geolevel_name,
 min, max)
WITH a AS (
        SELECT unnest(
         '{SES}'::Text[])::Text AS covariate_name,
              'LEVEL4'::Text AS study_geolevel_name,
              'SAHSU'::Text AS geography
)
SELECT a.geography, a.covariate_name, a.study_geolevel_name, b.min, b.max
  FROM a
        LEFT OUTER JOIN rif40_covariates b ON
                (a.covariate_name = b.covariate_name AND a.study_geolevel_name = b.geolevel_name AND a.geography = b.geography);
2[9:11:47 AM 121] notice: [DEBUG1] trigger_fct_t_rif40_inv_covariates_checks(): [20600-3] T_RIF40_INV_COVARIATES study: 13 investigation: 9
covariate: SES CRUD checks OK
2[9:11:47 AM 219] notice: [DEBUG1] trigger_fct_t_rif40_inv_covariates_checks(): T_RIF40_INV_COVARIATES study: 13 investigation: 9 covariate:
 SES study area geolevel name: LEVEL4 (id 4) same as geolevel in T_RIF40_STUDIES for study 13
2[9:11:47 AM 318] notice: [DEBUG1] trigger_fct_t_rif40_inv_covariates_checks(): [20266-71] T_RIF40_INV_COVARIATES study: 13 investigation: 9
 covariate: SES max/in checks OK
2[9:11:48 AM 441] notice: [DEBUG1] trigger_fct_t_rif40_inv_covariates_checks(): [20272-3] T_RIF40_INV_COVARIATES study: 13 investigation: 9
covariate: SES covariate table column: rif_data.SAHSULAND_COVARIATES_LEVEL4.SES can be accessed
2[9:11:48 AM 543] notice: [DEBUG1] trigger_fct_t_rif40_inv_covariates_checks(): [20274] T_RIF40_INV_COVARIATES study: 13 investigation: 9 co
variate: SES covariate table column: rif_data.SAHSULAND_COVARIATES_LEVEL4.YEAR can be accessed
2[9:11:48 AM 644] notice: [DEBUG1] trigger_fct_t_rif40_inv_covariates_checks(): [20275] T_RIF40_INV_COVARIATES study: 13 investigation: 9 co
variate: SES covariate table column: rif_data.SAHSULAND_COVARIATES_LEVEL4.LEVEL4 can be accessed
2[9:11:48 AM 783] notice: [DEBUG1] rif40_ddl(): Statement took: 00:00:01.768, proccessed 1 rows
2[9:11:48 AM 790] notice: _rif40_sql_test(): [71267] PASSED: Test case 6: SAHSULAND test 4 study_id 1 example no exceptions, no error expect
ed
2[9:11:48 AM 949] notice: rif40_sql_test(): [71150]: Test case 6: SAHSULAND test 4 study_id 1 example
PASSED expected result = actual (true)
*****************************************************************************
*
* 2: [Recursive test 6/7] Test OK: [6/7]: rif40_create_disease_mapping_example] SAHSULAND test 4 study_id 1 example
*
*****************************************************************************

1: DEPENDENT Recurse; next: 7
1: RECURSE level 6: [7/7]: rif40_create_disease_mapping_example] SAHSULAND test 4 study_id 1 example
2[9:11:48 AM 972] notice: _rif40_sql_test(): [71250]: Enable debug for function: trigger_fct_rif40_study_shares_checks
2[9:11:49 AM 187] notice: [DEBUG1] rif40_ddl(): SQL> INSERT /* 7 */ INTO rif40_study_shares(grantee_username) VALUES ('pch');
2[9:11:49 AM 285] notice: [DEBUG1] trigger_fct_rif40_study_shares_checks(): [20325] RIF40_STUDY_SHARES study_id: 13 not owned by grantor: pc
h; owned by: pch; but grantor is a RIF_MANAGER
2[9:11:49 AM 379] notice: [DEBUG1] trigger_fct_rif40_study_shares_checks(): [20327] RIF40_STUDY_SHARES study_id: 13 grantee username: pch is
 a RIF_USER/RIF_MANAGER
2[9:11:49 AM 475] notice: [DEBUG1] trigger_fct_rif40_study_shares_checks(): [20320-7] RIF40_STUDY_SHARES study: 13 CRUD checks OK
2[9:11:49 AM 652] notice: _rif40_sql_test(): [71267] PASSED: Test case 7: SAHSULAND test 4 study_id 1 example no exceptions, no error expect
ed
2[9:11:49 AM 808] notice: rif40_sql_test(): [71150]: Test case 7: SAHSULAND test 4 study_id 1 example
PASSED expected result = actual (true)
*****************************************************************************
*
* 2: [Recursive test 7/7] Test OK: [7/7]: rif40_create_disease_mapping_example] SAHSULAND test 4 study_id 1 example
*
*****************************************************************************

2: ROLLBACK transaction: [7/7]: rif40_create_disease_mapping_example] SAHSULAND test 4 study_id 1 example
1: Test harness complete; 7 tests completed; passed: 7; none failed.
1: BEGIN transaction: results UPDATE
1: Test run results by test >>>
1: [1/7] [Recursive test 1/7] ; id: 1; expected: true; pass: true; time taken: 7.014 S; SAHSULAND test 4 study_id 1 example
1: [2/7] [Recursive test 2/7] ; id: 2; expected: true; pass: true; time taken: 2.469 S; SAHSULAND test 4 study_id 1 example
1: [3/7] [Recursive test 3/7] ; id: 3; expected: true; pass: true; time taken: 0.776 S; SAHSULAND test 4 study_id 1 example
1: [4/7] [Recursive test 4/7] ; id: 4; expected: true; pass: true; time taken: 2.496 S; SAHSULAND test 4 study_id 1 example
1: [5/7] [Recursive test 5/7] ; id: 5; expected: true; pass: true; time taken: 4.464 S; SAHSULAND test 4 study_id 1 example
1: [6/7] [Recursive test 6/7] ; id: 6; expected: true; pass: true; time taken: 2.167 S; SAHSULAND test 4 study_id 1 example
1: [7/7] [Recursive test 7/7] ; id: 7; expected: true; pass: true; time taken: 0.851 S; SAHSULAND test 4 study_id 1 example
1: Test run results by test class >>>
1: rif40_create_disease_mapping_example: tests: 7, passed: 7, failed: 0, time taken: 20.237000000000002
1: COMMIT transaction.
*****************************************************************************
*
* Test harness run had no error(s); 7 passed
* Total time taken: 20.337 S
* Connection string: postgres://pch@localhost:5432/sahsuland_dev?application_name=db_test_harness
*
*****************************************************************************
```
{% endraw %}

Peter Hambly, 2nd September 2015

