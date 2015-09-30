#!/usr/bin/env node

// ************************************************************************
//
// GIT Header
//
// $Format:Git ID: (%h) %ci$
// $Id: 7ccec3471201c4da4d181af6faef06a362b29526 $
// Version hash: $Format:%H$
//
// Description:
//
// Rapid Enquiry Facility (RIF) - Node.js test harness
//
// Copyright:
//
// The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
// that rapidly addresses epidemiological and public health questions using 
// routinely collected health and population data and generates standardised 
// rates and relative risks for any given health outcome, for specified age 
// and year ranges, for any given geographical area.
//
// Copyright 2014 Imperial College London, developed by the Small Area
// Health Statistics Unit. The work of the Small Area Health Statistics Unit 
// is funded by the Public Health England as part of the MRC-PHE Centre for 
// Environment and Health. Funding for this project has also been received 
// from the Centers for Disease Control and Prevention.  
//
// This file is part of the Rapid Inquiry Facility (RIF) project.
// RIF is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// RIF is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
// to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
// Boston, MA 02110-1301 USA
//
// Author:
//
// Peter Hambly, SAHSU
//
// Usage: test_harness [options]
//
// Version: 0.1
//
// RIF 4.0 Database test harness.
//
// Options:
//  -d, --debug     RIF database PL/pgsql debug level      [default: 0]
//  -D, --database  name of Postgres database              [default: "sahsuland_dev"]
//  -U, --username  Postgres database username             [default: "pch"]
//  -P, --port      Postgres database port                 [default: 5432]
//  -H, --hostname  hostname of Postgres database          [default: "wpea-rif1"]
//  -F, --failed    re-run failed tests                    [default: false]
//  -C, --class	    Test run class						   [default: NULL]
//  --help          display this helpful message and exit  [default: false]
//
// E.g.
//
// node db_test_harness.js -H wpea-rif1 -D sahsuland_dev -U pch -d 1
//
// Connects using Postgres native driver (not JDBC) as rif40.
//
// Uses:
//
// https://github.com/brianc/node-postgres
// https://github.com/substack/node-optimist
//
// See: Node Makefile for build instructions
//

/* 
 * Function: 	main()
 * Parameters: 	ARGV
 * Returns:		Nothing
 * Description: Create control database connecton, then run rif40_startup()
 *				Then _rif40_sql_test_log_setup() ...
 */	
function main() {
	
	var pg = null;
	var optimist  = require('optimist');
	var mutexjs = require('mutexjs');
	
	var client1 = null; // Client 1: Master; hard to remove 
	var p_debug_level = null;
	
// Process Args using optimist
	var argv = optimist
    .usage("Usage: \033[1mtest_harness\033[0m [options] -- [test run class]\n\n"

+ "Version: 0.1\n\n"
+ "RIF 4.0 Database test harness.")

    .options("d", {
      alias: "debug_level",
      describe: "RIF database PL/pgsql debug level",
	  type: "integer",
      default: 0
    })
    .options("D", {
      alias: "database",
      describe: "name of Postgres database",
	  type: "string",
      default: pg_default("PGDATABASE")
    })
    .options("U", {
      alias: "username",
      describe: "Postgres database username",
	  type: "string",
      default: pg_default("PGUSER") 
    })	
    .options("P", {
      alias: "port",
      describe: "Postgres database port",
	  type: "integer",
      default: pg_default("PGPORT") 
    })		
    .options("H", {
      alias: "hostname",
      describe: "hostname of Postgres database",
	  type: "string",
      default: pg_default("PGHOST")
    })
    .options("F", {
      alias: "failed",
      describe: "re-run failed tests",
	  type: "boolean",
      default: false
    })	
    .options("C", {
      alias: "class",
      describe: "Test run class",
	  type: "string",
      default: null
    })	
    .options("help", {
      describe: "display this helpful message and exit",
      type: "boolean",
      default: false
    })	
	 .check(function(argv) {
      if (argv.help) return;
    })
    .argv;

	if (argv.help) return optimist.showHelp();

//
// Load database module
// Will eventually support SQL server as well
//
	try {
		pg=require('pg');
	}
	catch(err) {
		console.error('1: Could not load postgres database module.', err);				
		process.exit(1);
	}
	
	// Create 2x Postgres clients; one for control, the second for running each test in turn.
	// The 2nd client is created in _rif40_sql_test_log_setup(), called from rif40_startup()
	db_connect(pg, mutexjs, argv["hostname"] , argv["database"], argv["username"], argv["port"], 
			argv["failed"], argv["debug_level"], 1, argv["class"]);
} /* End of main */
	
/* 
 * Function: 	pg_default()
 * Parameters: 	Postgres variable name
 * Returns:		Defaulted value
 * Description: Setup postgres defaults (i.e. use PGDATABASE etc from env or set sensible default
 *				Used by optimist
 */
function pg_default(p_var) {
	var p_def;
	
	if (p_var == "PGDATABASE") {
		p_def="sahsuland_dev";
	}
	else if (p_var == "PGHOST") {
		p_def="localhost";
	}
	else if (p_var == "PGUSER") {
		p_def=process.env["USERNAME"];
		if (p_def === undefined) {
			p_def=process.env["USER"];
			if (p_def === undefined) {
				p_def="";
			}
		}
	}	
	else if (p_var == "PGPORT") {
		p_def=5432;
	}
	
	if (process.env[p_var]) { 
		return process.env[p_var];
	} 
	else { 
		return p_def;
	}
}
	
/* 
 * Function: 	db_connect()
 * Parameters: 	Postgres PG package connection handle, MutexJS package handle, 
 *				database host, name, username, port, failed flag, debug level, connection number: 
 *               1 - master; 2 - worker slave, test run class [optional] 
 * Returns:		Nothing
 * Description:	Connect to database, call rif40_startup()
 */
function db_connect(p_pg, p_mutexjs, p_hostname, p_database, p_user, p_port, p_failed, p_debug_level, p_num, p_test_run_class) {
	
	var client1 = null; // Client 1: Master; hard to remove	

	var conString = 'postgres://' + p_user + '@' + p_hostname + ':' + p_port + '/' + p_database + '?application_name=db_test_harness';

// Use PGHOST, native authentication (i.e. same as psql)
	client1 = new p_pg.Client(conString);
// Connect to Postgres database
	client1.connect(function(err) {
		if (err) {
			console.error(p_num + ': Could not connect to postgres client ' + p_num + ' using: ' + conString, err);
			if (p_hostname === 'localhost') {
				
// If host = localhost, use IPv6 numeric notation. This prevent ENOENT errors from getaddrinfo() in Windows
// when Wireless is disconnected. This is a Windows DNS issue. psql avoids this somehow.
// You do need entries for ::1 in pgpass			

				console.log(p_num + ': Attempt 2 (::1 instead of localhost) to connect to Postgres using: ' + conString);
				conString = 'postgres://' + p_user + '@' + '[::1]' + ':' + p_port + '/' + p_database + '?application_name=db_test_harness';
				client1 = new p_pg.Client(conString);
// Connect to Postgres database
				client1.connect(function(err) {
					if (err) {
						console.error(p_num + ': Could not connect [2nd attempt] to postgres client ' + p_num + ' using: ' + conString, err);
						process.exit(1);	
					}
					else {
						if (p_failed && p_debug_level == 0) {
							p_debug_level=1;
						}
// Call rif40_startup; then subsequent functions in an async tree
						rif40_startup(p_pg, p_mutexjs, client1, p_num, p_debug_level, conString, client1, p_failed, p_test_run_class);
					} // End of else connected OK 
				}); // End of connect				
				console.log(p_num + ': Connected to Postgres [2nd attempt] using: ' + conString);				
			}
		}
		else {			
			if (p_failed && p_debug_level == 0) {
				p_debug_level=1;
			}
// Call rif40_startup; then subsequent functions in an async tree
			rif40_startup(p_pg, p_mutexjs, client1, p_num, p_debug_level, conString, client1, p_failed, p_test_run_class);
		} // End of else connected OK 
	}); // End of connect		
	console.log(p_num + ': Connected to Postgres using: ' + conString);	

	// Notice message event processors
	client1.on('notice', function(msg) {
		  console.log('1: %s', msg);
	});
}

/* 
 * Function: 	rif40_startup()
 * Parameters: 	Postgres PG package connection handle, MutexJS package handle, 
 *				client connection, connection number (1 or 2), debug level, connection string, 
 *              master client (1) connection [may NOT be null], failed flag, test run class [optional]
 * Returns:		Nothing
 * Description: Run rif40_startup()
 *				Then _rif40_sql_test_log_setup()
 */
function rif40_startup(p_pg, p_mutexjs, p_client, p_num, p_debug_level, p_conString, p_client1, p_failed_flag, p_test_run_class) {
	var sql_stmt;
	
// Check master client (1) connection is NOT undefined	
	if (p_client1 === undefined) {
		console.error(p_num + ': rif40_startup() master client (1) connection is undefined');			
		process.exit(1);	
	}

// Check failed flag is NOT undefined	
	if (p_failed_flag === undefined) {
		console.error(p_num + ': rif40_startup() failed flag is undefined');			
		process.exit(1);	
	}
	
	if (p_num == 1) {
		sql_stmt = 'SELECT rif40_sql_pkg.rif40_startup() AS a';
	}
	else {
		sql_stmt = 'SELECT rif40_sql_pkg.rif40_startup(TRUE /* Do not do checks */) AS a';
	}
		
	// Connected OK, run SQL query
	var query = p_client.query(sql_stmt, function(err, result) {
		if (err) {
			// Error handler
			console.error(p_num + ': Error running query: ' + sql_stmt + ';', err);
			p_client.end();			
			process.exit(1);
		}
		else {	
			query.on('row', function(row) {
				//fired once for each row returned
				result.addRow(row);
			});
			query.on('end', function(result) {
				_rif40_sql_test_log_setup(p_pg, p_mutexjs, p_client, p_num, p_debug_level, p_conString, p_client1, p_failed_flag, p_test_run_class);
				return;
			});	
		}
	});	
}

/* 
 * Function: 	_rif40_sql_test_log_setup()
 * Parameters: 	Postgres PG package connection handle, MutexJS package handle, 
 *				client connection, connection number (1 or 2), debug level, connection string, 
 *              master client (1) connection [may NOT be null], failed flag, test run class [optional]
 * Returns:		Nothing
 * Description: Run _rif40_sql_test_log_setup()
 *				Then if you are the control connection (1) call init_test_harness()
 *				Otherwise can run_test_harness() using the control connection
 */
function _rif40_sql_test_log_setup(p_pg, p_mutexjs, p_client, p_num, p_debug_level, p_conString, p_client1, p_failed_flag, p_test_run_class) {
	
// Check master client (1) connection is NOT undefined	
	if (p_client1 === undefined) {
		console.error(p_num + ': _rif40_sql_test_log_setup() master client (1) connection is undefined');			
		process.exit(1);	
	}
		
	var sql_stmt = 'SELECT rif40_sql_pkg._rif40_sql_test_log_setup(' + p_debug_level + ') AS a';
			
	// Connected OK, run SQL query
	var query = p_client.query(sql_stmt, function(err, result) {
		if (err) {
			// Error handler
			console.error(p_num + ': Error running query: ' + sql_stmt + ';', err);
			p_client.end();			
			process.exit(1);
		}
		else {	
			query.on('row', function(row) {
				//fired once for each row returned
				result.addRow(row);
			});
			query.on('end', function(result) {
				if (p_num == 1) { // p_client is master [1]
					// Client 1 [Master] waits after test harness initialisation
					console.log('1: Wait for client 2 initialisation; debug_level: ' + p_debug_level);
					// Create 2nd client(worker thread) for running test cases
					init_test_harness(p_pg, p_mutexjs, p_client1, p_debug_level, p_conString, p_failed_flag, p_test_run_class);
				}
				else { // p_client is slave [2]
					// Client 2 [slave worker] is now initialised so we can run the test harness tests
					console.log('1: Client 2 initialised; debug_level: ' + p_debug_level);
					run_test_harness(p_conString, p_mutexjs, p_client1, p_client, p_failed_flag, p_test_run_class); 
				}
				return;
			});	
		}
	});	
	
}

/* 
 * Function: 	init_test_harness()
 * Parameters: 	Postgres PG package connection handle, MutexJS package handle, 
 *				master client (1) connection, debug level, connection string, failed flag, test run class [optional]
 * Returns:		Nothing
 * Description: Create 2nd client (worker thread) for running test cases
 */
function init_test_harness(p_pg, p_mutexjs, p_client1, p_debug_level, p_conString, p_failed_flag, p_test_run_class) {
	var client2 = null;

// Check master client (1) connection is NOT undefined	
	if (p_client1 === undefined) {
		console.error('1: init_test_harness() master client (1) connection is undefined');			
		process.exit(1);	
	}
	
// Create 2nd client (worker thread) for running test cases
	try {
		console.log('2: Connect to Postgres using: ' + p_conString);		
		client2 = new p_pg.Client(p_conString);
	}
	catch(err) {
			return console.error('2: Could not create postgres 2nd client [slave] using: ' + p_conString, err);
	}
	client2.on('notice', function(msg) {
// Suppress client 2 (worker thread) initialisation messages; re-enabled in run_test_harness()
//	      console.log('2: %s', msg);
	});
	client2.connect(function(err) {
		if (err) {
			return console.error('2: Could not connect to postgres 2nd client [slave] using: ' + p_conString, err);
		}
		else {	
	        // Call rif40_startup; then subsequent functions in an async tree
			rif40_startup(p_pg, p_mutexjs, client2, 2, p_debug_level, p_conString, p_client1, p_failed_flag, p_test_run_class);
		} // End of else connected OK 
	}); // End of connect
}
	
/* 
 * Function: 	run_test_harness()
 * Parameters: 	Connection string, MutexJS package handle, Master client (1) connection, worker thread client (2) connection, failed flag, test run class [optional]
 * Returns:		Nothing
 * Description: Run test harness:
 *
 *              COMMIT; to complete initial on logon transaction
 *              Build SQL statement to display tests per test_run_class, handling -F flag to re-run failed tests only
 *				Display tests per test_run_class, total tests
 * 			    Finally run the tests: function run_test_harness_tests()
 *
 * Count query exmaple - all failed tests:
 
WITH RECURSIVE a AS (
	SELECT b.test_id AS root_test_id, b.test_id, b.parent_test_id, b.test_run_class, 0 AS level
 	  FROM rif40_test_harness b
     WHERE b.parent_test_id IS NULL	 
       AND b.pass = FALSE
	UNION ALL
	SELECT a.root_test_id, b.test_id, b.parent_test_id, b.test_run_class, a.level+1 AS level
 	  FROM a
     JOIN rif40_test_harness b ON (b.test_run_class = a.test_run_class AND a.test_id = b.parent_test_id)
), b AS (
	SELECT a.*, 
	       ROW_NUMBER() OVER(PARTITION BY root_test_id ORDER BY level, test_id) AS recursive_test_number, 
	       COUNT(root_test_id) OVER(
									PARTITION BY root_test_id 
									ORDER BY level, test_id 
									ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS recursive_test_total 
	  FROM a
)
SELECT test_run_class, COUNT(test_run_class) AS tests
  FROM b
 GROUP BY test_run_class
 ORDER BY test_run_class;
 
 */
function run_test_harness(p_conString, p_mutexjs, p_client1, p_client2, p_failed_flag, p_test_run_class_filter) {
	var total_tests=0;
	// COMMIT; to complete initial on logon transaction
	var end = p_client2.query('COMMIT', function(err, result) {
		if (err) {
			p_client2.end();
			return console.error('2: Error in COMMIT transaction;', err);
		}
		else {
			// Transaction COMMIT OK 
			end.on('end', function(result) {	
				console.log('2: COMMIT transaction;');					
			    // Build SQL statement to display tests per test_run_class, handling -F flag to re-run failed tests only
			var sql_stmt = 'WITH RECURSIVE a AS (\n' +
				'SELECT b.test_id AS root_test_id, b.*, 0 AS level\n' +
				'  FROM rif40_test_harness b\n' +
				' WHERE b.parent_test_id IS NULL /* Ignore dependent tests */\n';
			if (p_failed_flag) {
				sql_stmt = sql_stmt + '  AND b.pass = FALSE /* Filter on failed tests only */\n';
			}	
			
			var lp_test_run_class_filter;	
			if ((p_test_run_class_filter !== undefined)&&(p_test_run_class_filter !== null)) {
				lp_test_run_class_filter=p_test_run_class_filter
				sql_stmt = sql_stmt + '  AND b.test_run_class = $1 /* Filter on test_run_class */\n';					
			}	
			else {
				lp_test_run_class_filter=1
				sql_stmt = sql_stmt + '  AND 1 = $1 /* Dummy filter for test_run_class */\n';					
			}	
			sql_stmt = sql_stmt +
				'UNION ALL\n' +
				'SELECT a.root_test_id, b.*, a.level+1 AS level\n' +
				'  FROM a\n' +
				'		 JOIN rif40_test_harness b ON (b.test_run_class = a.test_run_class AND a.test_id = b.parent_test_id)\n' +
				'), b AS (\n' +
				'	SELECT a.*,\n' +
				'	       ROW_NUMBER() OVER(PARTITION BY root_test_id ORDER BY level, test_id) AS recursive_test_number,\n' +
				'	       COUNT(root_test_id) OVER(\n' +
				'						PARTITION BY root_test_id\n' +
				'						ORDER BY level, test_id\n' +
				'						ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS recursive_test_total\n' +
				'	  FROM a\n' +
				')\n' +
				'SELECT test_run_class, COUNT(test_run_class) AS tests\n' +
				'  FROM b\n' + 
				' GROUP BY test_run_class\n' +
				' ORDER BY test_run_class';
						
				// Connected OK, run SQL query
				var query = p_client1.query({
						text: sql_stmt, 
						values: [lp_test_run_class_filter]}, function(err, result) {
					if (err) {
						// Error handler
						console.error('1: Error running query: ' + sql_stmt + ';', err);
						p_client1.end();			
						process.exit(1);
					}
					else {	
						query.on('row', function(row) {
							//fired once for each row returned
							result.addRow(row);
						});
						query.on('end', function(result) {
							// Re-enable client 2 [worker] debug messages
							p_client2.on('notice', function(msg) {
								var timestamp = new Date();
								console.log('2[%s %s] %s', timestamp.toLocaleTimeString(), timestamp.getMilliseconds(), msg);
							});			
							// End of query processing - process results array - tests per class
							
							var p_rif40_test_harness_results = {};
											
							row_count = result.rowCount;
							for (i = 1; i <= row_count; i++) { 
								p_rif40_test_harness_results[result.rows[i-1].test_run_class] = { 
									tests: result.rows[i-1].tests,
									time_taken: 0,
									passed: 0,
									failed: 0
								}
								console.log('1: Class: %s Tests: %s', 
									result.rows[i-1].test_run_class, result.rows[i-1].tests);
								total_tests=total_tests + parseInt(result.rows[i-1].tests, 10);
							}
							console.log('1: Total tests to run: %s', total_tests);
							// Finally run the tests: function run_test_harness_test()
							run_test_harness_tests(p_conString, p_mutexjs, p_client1, p_client2, total_tests, p_failed_flag, 
								p_test_run_class_filter, sql_stmt, p_rif40_test_harness_results);							
						});	
					}
				});		
			});
		}
	});		
}

/* 
 * Function: 	run_test_harness_tests()
 * Parameters: 	Connection string, MutexJS package handle, Master client (1) connection, worker thread client (2) connection, 
 *				number of tests, failed flag, test run class [optional], sql_stmt used to count tests,
 *				results array
 * Returns:		Nothing
 * Description: Run test harness tests:
 *
 *				Build SQL statement to run tests, handling -F flag to re-run failed tests only
 *				Run query, push results data into results array
 *				Run the first test using rif40_sql_test(). 
 *				The rest of the tests are run recursively from rif40_sql_test() from query.on('end', ...) 
 *				so the SQl statements and transactions are run in the correct order.
 */
function run_test_harness_tests(p_conString, p_mutexjs, p_client1, p_client2, p_tests, 
			p_failed_flag, p_test_run_class_filter, count_sql_stmt, p_rif40_test_harness_results) {
	// Test arrays; 
	var p_test_run_class = [];
	var p_test_case_title = [];	
	var p_test_stmt = []; 
	var p_results = []; 
	var p_results_xml = []; 
	var p_pg_error_code_expected = []; 
	var p_raise_exception_on_failure = []; 
	var p_test_id = [];
	var p_expected_result = [];
	var p_root_test_id = []; 
	var p_level = []; 
	var p_recursive_test_number = []; 
	var p_recursive_test_total = []; 
	var p_pg_debug_functions = []; 
	var p_pass = [];
	var p_time_taken = [];

	// Build SQL statement to run tests, handling -F flag to re-run failed tests only:
	/*
WITH RECURSIVE a AS (
	SELECT b.test_id AS root_test_id, b.test_id, b.parent_test_id, b.test_run_class, 0 AS level
 	  FROM rif40_test_harness b
     WHERE b.test_run_class = 'rif40_create_disease_mapping_example'
       AND b.parent_test_id IS NULL	   
	UNION ALL
	SELECT a.root_test_id, b.test_id, b.parent_test_id, b.test_run_class, a.level+1 AS level
 	  FROM a
     JOIN rif40_test_harness b ON (b.test_run_class = a.test_run_class AND a.test_id = b.parent_test_id)
), b AS (
	SELECT a.*, 
	       ROW_NUMBER() OVER(PARTITION BY root_test_id ORDER BY level, test_id) AS recursive_test_number, 
	       COUNT(root_test_id) OVER(
									PARTITION BY root_test_id 
									ORDER BY level, test_id 
									ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS recursive_test_total 
	  FROM a
)
SELECT * FROM b
 ORDER BY root_test_id, level, test_id;
 
Without the "rif40_create_disease_mapping_example" filter gives:
 
 root_test_id | test_id | parent_test_id |            test_run_class            | level | recursive_test_number | recursive_test_total
--------------+---------+----------------+--------------------------------------+-------+-----------------------+----------------------
            1 |       1 |                | rif40_create_disease_mapping_example |     0 |                     1 |                    7
            1 |       2 |              1 | rif40_create_disease_mapping_example |     1 |                     2 |                    7
            1 |       3 |              2 | rif40_create_disease_mapping_example |     2 |                     3 |                    7
            1 |       4 |              3 | rif40_create_disease_mapping_example |     3 |                     4 |                    7
            1 |       5 |              4 | rif40_create_disease_mapping_example |     4 |                     5 |                    7
            1 |       6 |              5 | rif40_create_disease_mapping_example |     5 |                     6 |                    7
            1 |       7 |              6 | rif40_create_disease_mapping_example |     6 |                     7 |                    7
            8 |       8 |                | test_8_triggers.sql                  |     0 |                     1 |                    1
            9 |       9 |                | test_8_triggers.sql                  |     0 |                     1 |                    1
           10 |      10 |                | test_8_triggers.sql                  |     0 |                     1 |                    1
           11 |      11 |                | test_8_triggers.sql                  |     0 |                     1 |                    1
           12 |      12 |                | test_8_triggers.sql                  |     0 |                     1 |                    1
           13 |      13 |                | test_8_triggers.sql                  |     0 |                     1 |                    1
(13 rows)

WITH RECURSIVE a AS (
	SELECT b.test_id AS root_test_id, b.test_id, b.parent_test_id, b.test_run_class, 0 AS level
 	  FROM rif40_test_harness b
     WHERE b.parent_test_id IS NULL	 
       AND b.pass = FALSE
	UNION ALL
	SELECT a.root_test_id, b.test_id, b.parent_test_id, b.test_run_class, a.level+1 AS level
 	  FROM a
     JOIN rif40_test_harness b ON (b.test_run_class = a.test_run_class AND a.test_id = b.parent_test_id)
), b AS (
	SELECT a.*, 
	       ROW_NUMBER() OVER(PARTITION BY root_test_id ORDER BY level, test_id) AS recursive_test_number, 
	       COUNT(root_test_id) OVER(
									PARTITION BY root_test_id 
									ORDER BY level, test_id 
									ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS recursive_test_total 
	  FROM a
)
SELECT * FROM b
 ORDER BY root_test_id, level, test_id;
 
WITH RECURSIVE a AS (
	SELECT b.test_id AS root_test_id, b.test_id, b.parent_test_id, b.test_run_class, 0 AS level
 	  FROM rif40_test_harness b
     WHERE b.parent_test_id IS NULL	   
	UNION ALL
	SELECT a.root_test_id, b.test_id, b.parent_test_id, b.test_run_class, a.level+1 AS level
 	  FROM a
     JOIN rif40_test_harness b ON (a.test_id = b.parent_test_id)
), b AS (
	SELECT a.*, 
	       ROW_NUMBER() OVER(PARTITION BY root_test_id ORDER BY level, test_id) AS recursive_test_number, 
	       COUNT(root_test_id) OVER(
									PARTITION BY root_test_id 
									ORDER BY level, test_id 
									ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS recursive_test_total 
	  FROM a
)
SELECT * FROM b
 ORDER BY root_test_id, level, test_id;
 
    */
	var sql_stmt = 'WITH RECURSIVE a AS (\n' +
		'SELECT b.test_id AS root_test_id, b.*, 0 AS level\n' +
		'  FROM rif40_test_harness b\n' +
		' WHERE b.parent_test_id IS NULL /* Ignore dependent tests */\n';
	if (p_failed_flag) {
		sql_stmt = sql_stmt + '  AND b.pass = FALSE /* Filter on failed tests only */\n';
	}	
	
	var lp_test_run_class_filter;	
	if ((p_test_run_class_filter !== undefined)&&(p_test_run_class_filter !== null)) {
		lp_test_run_class_filter=p_test_run_class_filter
		sql_stmt = sql_stmt + '  AND b.test_run_class = $1 /* Filter on test_run_class */\n';					
	}	
	else {
		lp_test_run_class_filter=1
		sql_stmt = sql_stmt + '  AND 1 = $1 /* Dummy filter for test_run_class */\n';					
	}	
	sql_stmt = sql_stmt +
		'UNION ALL\n' +
		'SELECT a.root_test_id, b.*, a.level+1 AS level\n' +
		'  FROM a\n' +
		'		 JOIN rif40_test_harness b ON (b.test_run_class = a.test_run_class AND a.test_id = b.parent_test_id)\n' +
		'), b AS (\n' +
		'	SELECT a.*,\n' +
		'	       ROW_NUMBER() OVER(PARTITION BY root_test_id ORDER BY level, test_id) AS recursive_test_number,\n' +
		'	       COUNT(root_test_id) OVER(\n' +
		'						PARTITION BY root_test_id\n' +
		'						ORDER BY level, test_id\n' +
		'						ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS recursive_test_total\n' +
		'	  FROM a\n' +
		')\n' +
		'SELECT * FROM b\n' +
		' ORDER BY root_test_id, level, test_id';
					
	// Connected OK, run SQL query
	var query = p_client1.query({
						text: sql_stmt, 
						values: [lp_test_run_class_filter]}, function(err, test_array) {
		if (err) {
			// Error handler
			console.error('1: Error running query: ' + sql_stmt + ';', err);
			p_client1.end();			
			process.exit(1);
		}
		else {	
			query.on('row', function(row) {
				//fired once for each row returned
				test_array.addRow(row);
			});
			query.on('end', function(test_array) {	
				// End of query processing - process results array
				row_count = test_array.rowCount;
				
				// Check for test counter mismatch 
				if (row_count > p_tests) {
					console.error('2: run_test_harness_tests() row_count (' + row_count + ') > p_tests (' + p_tests + 
						')\n SQL>\n' + sql_stmt + ';\n\nSQL used to count>\n' + count_sql_stmt + ';\n\nlp_test_run_class_filter: ' + lp_test_run_class_filter);			
					process.exit(1);		
				}
	
				p_test_run_class = new Array();
				p_test_case_title = new Array(); 
				p_test_stmt = new Array(); 
				p_results = new Array(); 
				p_results_xml = new Array(); 
				p_pg_error_code_expected = new Array(); 
				p_raise_exception_on_failure = new Array(); 
				p_test_id = new Array(); 
				p_expected_result = new Array();
				p_pass = new Array();
				p_time_taken = new Array();
				p_root_test_id = new Array(); 
				p_level = new Array(); 
				p_recursive_test_number = new Array(); 
				p_recursive_test_total = new Array(); 
				p_pg_debug_functions = new Array(); 
				
				var p_rif40_test_harness = {};
				var p_passed_or_failed = { p_passed:0 /* p_passed */, p_failed:0 /* p_failed */};
				
				if (row_count > 0) {
					// Push results data into results array
					for (j = 0; j <row_count; j++) { 
						p_test_run_class.push(test_array.rows[j].test_run_class /* Deep copy */);
						p_test_case_title.push(test_array.rows[j].test_case_title /* Deep copy */);
						p_test_stmt.push(test_array.rows[j].test_stmt /* Deep copy */);
						p_results.push(test_array.rows[j].results /* Deep copy */);
						p_results_xml.push(test_array.rows[j].results_xml /* Deep copy */);
						p_pg_error_code_expected.push(test_array.rows[j].pg_error_code_expected /* Deep copy */);	
						p_raise_exception_on_failure.push(test_array.rows[j].raise_exception_on_failure /* Deep copy */);
						p_test_id.push(test_array.rows[j].test_id /* Deep copy */);		
						p_expected_result.push(test_array.rows[j].expected_result /* Deep copy */);	
						p_root_test_id.push(test_array.rows[j].root_test_id /* Deep copy */);
						p_level.push(test_array.rows[j].level /* Deep copy */);					
						p_recursive_test_number.push(test_array.rows[j].recursive_test_number /* Deep copy */);					
						p_recursive_test_total.push(test_array.rows[j].recursive_test_total /* Deep copy */);					
						p_pg_debug_functions.push(test_array.rows[j].pg_debug_functions /* Deep copy */);						
						p_pass.push(undefined);
						p_time_taken.push(undefined);						
					
						if (p_test_case_title[j] === undefined) {
							console.error('1: ' + test + ' run_test_harness_tests() p_test_case_title[' + j + '] is undefined');			
							process.exit(1);							
						}
						p_rif40_test_harness[j] = { 
							test_run_class: test_array.rows[j].test_run_class, 
							test_case_title: test_array.rows[j].test_case_title, 
							test_stmt: test_array.rows[j].test_stmt, 
							results: test_array.rows[j].results,
							results_xml: test_array.rows[j].results_xml,
							pg_error_code_expected: test_array.rows[j].pg_error_code_expected, 
							raise_exception_on_failure: test_array.rows[j].raise_exception_on_failure,
							test_id: test_array.rows[j].test_id,
							expected_result: test_array.rows[j].expected_result,
							level: test_array.rows[j].level,
							root_test_id: test_array.rows[j].root_test_id,
							recursive_test_number: test_array.rows[j].recursive_test_number,
							recursive_test_total: test_array.rows[j].recursive_test_total,
							pg_debug_functions: test_array.rows[j].pg_debug_functions,
							pass: undefined,
							time_taken: undefined };	
						if (p_rif40_test_harness[j].test_case_title === undefined) {
							console.error('1: ' + test + ' run_test_harness_tests() p_rif40_test_harness[' + j + '].test_case_title is undefined');			
							process.exit(1);							
						}							
					}
						
					// Run the first test using rif40_sql_test(). 
					// The rest of the tests are run recursively from rif40_sql_test() from query.on('end', ...) 
					// so the SQL statements and transactions are run in the correct order.
					var start_time = Date.now();
					
//
// This is no longer recursive, replaced with a for loop and a Mutex lock
//						

						var k = 1;
						for (; k <= row_count; k++) { 	
							(function(p_k) {
								process.nextTick(function() {						
//									console.log('1: p_k: ' + p_k + ', row_count: ' + row_count);
									try {
										if (p_k > row_count) {
											console.error('1: run_test_harness_tests() p_k (' + p_k + ') > row_count (' + row_count + ')');				
											process.exit(1);
										}
										var p_mutex_id;
										var mutex_name = 'db_test_harness.js-test';								
										p_mutexjs.lock(mutex_name, function(id) {
											p_mutex_id=id;
//											console.log('1: Gained mutex [' + p_k + '/' + p_tests + ']: ' + mutex_name + ', id: ' + p_mutex_id);
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

				}
				else if (p_failed_flag) {
					console.log('1: No failed tests to run');	
					process.exit(0);					
				}
				else {
					console.error('1: No tests to run, SQL> ' + sql_stmt + ';\n[lp_test_run_class_filter: ' + lp_test_run_class_filter + ']');
					process.exit(1);					
				}
				return;
			});	
		}
	});				
}

/* 
 * Function: 	rif40_sql_test()
 * Parameters: 	Connection string, MutexJS package handle, Master client (1) connection, worker thread client (2) connection, test number, number of tests, 
 *              {number passed, number failed}, failed flag, test harness results array, time of test harness start, 
 *				Test Mutex,
 *				results array
 * Returns:		Nothing
 * Description: Begin transaction, call _rif40_sql_test() to run test:
 *
 *				Run SQL test using a bind type SQL statement and rif40_sql_pkg._rif40_sql_test()
 *
 *					Error handler. rif40_sql_pkg._rif40_sql_test() normally catches errors and returns pass or fail:
 *					Print SQL statement, parameters and error. This is an unhandled condition [bug!] in rif40_sql_pkg._rif40_sql_test()
 *				Process results. One row expected - pass boolean: true/false:
 *					Check for one row 
 *					Check pass/fail against expected 
 * 				call: _rif40_sql_test_end(): Rollback test case, recurse to next test case
 */
function rif40_sql_test(p_conString, p_mutexjs, p_client1, p_client2, p_j, p_tests, 
				p_passed_or_failed, p_failed_flag, p_rif40_test_harness, p_start_time, p_mutex_id, p_rif40_test_harness_results) {	
	
	var test='[' + p_j + '/' + p_tests + ']: ' + p_rif40_test_harness[p_j-1].test_run_class + '] ' + 
			p_rif40_test_harness[p_j-1].test_case_title;
				
	// Check failed flag is NOT undefined	
	if (p_failed_flag === undefined) {
		console.error('1: ' + test + ' rif40_sql_test() failed flag is undefined');			
		process.exit(1);	
	}
	// Check p_rif40_test_harness object of arrays is NOT undefined	
	if (p_rif40_test_harness === undefined) {
		console.error('1: ' + test + ' rif40_sql_test() p_rif40_test_harness object of arrays is undefined');			
		process.exit(1);	
	}
	if (p_rif40_test_harness[p_j-1] === undefined) {
		console.error('2: ' + test + ' rif40_sql_test() p_rif40_test_harness[' + p_j-1 + '] is undefined');			
		process.exit(1);							
	}		
	if (p_rif40_test_harness[p_j-1].test_stmt === undefined) {
		console.error('2: ' + test + ' rif40_sql_test() p_rif40_test_harness[' + p_j-1 + '].p_test_stmt is undefined');			
		process.exit(1);							
	}
	// Check p_rif40_test_harness_results object of arrays is NOT undefined	
	if (p_rif40_test_harness_results === undefined) {
		console.error('1: ' + test + ' rif40_sql_test() p_rif40_test_harness_results object of arrays is undefined');			
		process.exit(1);	
	}
	if (p_rif40_test_harness_results[p_rif40_test_harness[p_j-1].test_run_class] === undefined) {
		console.error('1: ' + test + ' rif40_sql_test() p_rif40_test_harness_results[p_rif40_test_harness[' + p_j-1 + 
			'].test_run_class] object of arrays is undefined');			
		process.exit(1);	
	}
	
// Check for test counter mismatch 
	if (p_j > p_tests) {
		console.error('2: ' + test + ' rif40_sql_test() p_j (' + p_j + ') > p_tests (' + p_tests + ')');			
		process.exit(1);		
	}
	
	if (p_rif40_test_harness[p_j-1].level == 0) {
		var begin = p_client2.query('BEGIN', function(err, result) {
			if (err) {
				p_client2.end();
				console.error('2: Error in BEGIN transaction;', err);
				p_client1.end();			
				process.exit(1);
			}
			else {
				// Transaction start OK 
				begin.on('end', function(result) {			
					console.log('1: ' + test);			
					console.log('2: BEGIN transaction: ' + test);		

					_rif40_sql_test(p_conString, p_mutexjs, p_client1, p_client2, p_j, p_tests, 
							p_passed_or_failed, p_failed_flag, p_rif40_test_harness, test, p_start_time, p_mutex_id,
							p_rif40_test_harness_results);
				});
			}
		});
	}
	else {	
		console.log('1: RECURSE level ' + p_rif40_test_harness[p_j-1].level + ': ' + test);			
		_rif40_sql_test(p_conString, p_mutexjs, p_client1, p_client2, p_j, p_tests, 
				p_passed_or_failed, p_failed_flag, p_rif40_test_harness, test, p_start_time, p_mutex_id,
				p_rif40_test_harness_results);
	}
}

/* 
 * Function: 	_rif40_sql_test()
 * Parameters: 	Connection string, MutexJS package handle, Master client (1) connection, worker thread client (2) connection, test number, number of tests, 
 *              {number passed, number failed}, failed flag, test harness results array, textual description of test, time of test harness start, 
 *				Test Mutex,
 *				results array
 * Returns:		Nothing
 * Description: Run SQL test using a bind type SQL statement and rif40_sql_pkg._rif40_sql_test()
 *
 *			Error handler. rif40_sql_pkg._rif40_sql_test() normally catches errors and returns pass or fail:
 *				Print SQL statement, parameters and error. This is an unhandled condition [bug!] in rif40_sql_pkg._rif40_sql_test()
 *			Process results. One row expected - pass boolean: true/false:
 *				Check for one row 
 *				Check pass/fail against expected
 *          Call: _rif40_sql_test_end(): Rollback test case, recurse to next test case
 */
function _rif40_sql_test(p_conString, p_mutexjs, p_client1, p_client2, p_j, p_tests, 
				p_passed_or_failed, p_failed_flag, p_rif40_test_harness, p_test_text, p_start_time, p_mutex_id,
				p_rif40_test_harness_results) {
// Check mutex defined
	if (p_mutexjs === undefined) {
		console.error('1: rif40_sql_test_end(' + p_j + ') p_mutexjs is undefined');			
		process.exit(1);							
	}	
	if (p_mutex_id === undefined) {
		console.error('1: rif40_sql_test_end(' + p_j + ') p_mutex_id is undefined');			
		process.exit(1);							
	}
	// Check p_rif40_test_harness_results object of arrays is NOT undefined	
	if (p_rif40_test_harness_results === undefined) {
		console.error('1: ' + test + ' _rif40_sql_test() p_rif40_test_harness_results object of arrays is undefined');			
		process.exit(1);	
	}
	if (p_rif40_test_harness_results[p_rif40_test_harness[p_j-1].test_run_class] === undefined) {
		console.error('1: ' + test + ' _rif40_sql_test() p_rif40_test_harness_results[p_rif40_test_harness[' + p_j-1 + 
			'].test_run_class] object of arrays is undefined');			
		process.exit(1);	
	}
	
	// Build bind type SQL statement
	var sql_stmt = 'SELECT rif40_sql_pkg.rif40_sql_test(' + '\n' + 
			'$1::VARCHAR    /* test_stmt */,' + '\n' + 
			'$2::VARCHAR    /* test_case_title */,' + '\n' + 
			'$3::Text[][]   /* results */,' + '\n' + 
			'$4::XML        /* results_xml */,' + '\n' +
			'$5::VARCHAR    /* pg_error_code_expected */,' + '\n' +
			'$6::BOOLEAN    /* raise_exception_on_failure */,' + '\n' +
			'$7::INTEGER    /* test_id */,' + '\n' +
			'$8::Text[]     /* pg_debug_functions */,' + '\n' +
			'$9::BOOLEAN    /* expected_result */) AS rbool';
	var start_time=Date.now();
		
	// Check failed flag is NOT undefined	
	if (p_failed_flag === undefined) {
		console.error('1: ' + p_test_text + ' _rif40_sql_test() failed flag is undefined');			
		process.exit(1);	
	}
	// Check p_rif40_test_harness object of arrays is NOT undefined	
	if (p_rif40_test_harness === undefined) {
		console.error('1: ' + p_test_text + ' _rif40_sql_test() p_rif40_test_harness object of arrays is undefined');			
		process.exit(1);	
	}
	if (p_rif40_test_harness[p_j-1] === undefined) {
		console.error('2: ' + p_test_text + ' _rif40_sql_test() p_rif40_test_harness[' + p_j-1 + '] is undefined');			
		process.exit(1);							
	}		
	if (p_rif40_test_harness[p_j-1].test_stmt === undefined) {
		console.error('2: ' + p_test_text + ' _rif40_sql_test() p_rif40_test_harness[' + p_j-1 + '].p_test_stmt is undefined');			
		process.exit(1);							
	}
	
	// Run SQL statement
	var run = p_client2.query({
				text: sql_stmt, 
				values: [p_rif40_test_harness[p_j-1].test_stmt,
						 p_rif40_test_harness[p_j-1].test_case_title, 
						 p_rif40_test_harness[p_j-1].results, 
						 p_rif40_test_harness[p_j-1].results_xml,
						 p_rif40_test_harness[p_j-1].pg_error_code_expected,
						 p_rif40_test_harness[p_j-1].raise_exception_on_failure,
						 p_rif40_test_harness[p_j-1].test_id,
						 p_rif40_test_harness[p_j-1].pg_debug_functions,
						 p_rif40_test_harness[p_j-1].expected_result]}, 
		// Cursor execution callback function				 
		function(err, result) {
			// Error handler. rif40_sql_pkg._rif40_sql_test() normally catches errors and returns pass or fail;
			// Print SQL statement, param,eters and error. This is an unhandled condition [bug!] in rif40_sql_pkg._rif40_sql_test()
			if (err) {
				console.error('2: Error in run test; SQL> ' + sql_stmt + 
		'*\n* [Parameter 1: test_stmt                 VARCHAR]\nSQL>>>' + p_rif40_test_harness[p_j-1].test_stmt + ';\n' + 
		'* [Parameter 2: test_case_title              VARCHAR]  ' + p_rif40_test_harness[p_j-1].test_case_title + '\n' + 
		'* [Parameter 3: results                      TEXT[][]] ' + p_rif40_test_harness[p_j-1].results + '\n' + 
		'* [Parameter 4: results_xml                  XML]      ' + p_rif40_test_harness[p_j-1].results_xml + '\n' + 
		'* [Parameter 5: pg_error_code_expected       VARCHAR]  ' + p_rif40_test_harness[p_j-1].pg_error_code_expected + '\n' + 
		'* [Parameter 6: raise_exception_on_failure   BOOLEAN]  ' + p_rif40_test_harness[p_j-1].raise_exception_on_failure + '\n' + 
		'* [Parameter 7: test_id                      INTEGER]  ' + p_rif40_test_harness[p_j-1].test_id + '\n' +  
		'* [Parameter 8: pg_debug_functions           Text[]]   ' + p_rif40_test_harness[p_j-1].pg_debug_functions + '\n' + 
		'* [Parameter 9: expected_result              BOOLEAN]  ' + p_rif40_test_harness[p_j-1].expected_result + '\n' + 
		'*\n' + '*****************************************************************************\n', err);	
				console.error('2: ROLLBACK transaction: ' + p_test_text);
				var end = p_client2.query('ROLLBACK', function(err, result) {
					if (err) {
						console.error('2: Error in ROLLBACK transaction after error;', err);							
						p_client2.end();
						p_client1.end();			
						process.exit(1);			
					}
					else {	
						p_client2.end();
						p_client1.end();							
						process.exit(1);
					}	
				});
			}
			else {
				// Test OK 
					run.on('row', function(row) {
						//fired once for each row returned
						result.addRow(row);
					});
				// End of run processing - process results. One row expected - pass boolean: true/false 					
				run.on('end', function(result) {
					row_count = result.rowCount;	
	//							console.log('rbool: ' + result.rows[0].rbool + '; p_expected_result[p_j-1]: ' + 
	//								p_rif40_test_harness[p_j].expected_result);
					if (row_count != 1) {
						console.error('2: Test FAILED: (' + row_count + ') rows ' + p_test_text + '\n' + 'SQL> ' + sql_stmt);	
						p_passed_or_failed.p_failed++;	
						p_rif40_test_harness_results[p_rif40_test_harness[p_j-1].test_run_class].failed++;
					}
					// OK - so we need to check pass/fail against expected
					else if (result.rows[0].rbool == false)   /* Test failed */ {
						p_rif40_test_harness[p_j-1].pass = false;
						p_passed_or_failed.p_failed++;
						p_rif40_test_harness_results[p_rif40_test_harness[p_j-1].test_run_class].failed++;
						if (p_rif40_test_harness[p_j-1].expected_result == true) /* It was expected to pass */ {
							test_result(false, 'Test FAILED, expected to PASS: ' + p_test_text, sql_stmt,
									p_rif40_test_harness, p_j-1);
						}
						else {
							test_result(false, 'Test PASSED, expected to FAIL: ' + p_test_text, sql_stmt,
									p_rif40_test_harness, p_j-1);								
						}						}
					else { /* Test passed */
						p_rif40_test_harness[p_j-1].pass = true;
						p_passed_or_failed.p_passed++;
						p_rif40_test_harness_results[p_rif40_test_harness[p_j-1].test_run_class].passed++;
						if (p_rif40_test_harness[p_j-1].expected_result == true) /* It was expected to pass */ {								
							test_result(true, 'Test OK: ' + p_test_text, sql_stmt,
									p_rif40_test_harness, p_j-1);
						}
						else {
							test_result(true, 'Test FAILED as expected: ' + p_test_text, sql_stmt,
									p_rif40_test_harness, p_j-1);									
						}											
					}
					p_rif40_test_harness[p_j-1].time_taken = (Date.now()-start_time)/1000;
					p_rif40_test_harness_results[p_rif40_test_harness[p_j-1].test_run_class].time_taken += 
						p_rif40_test_harness[p_j-1].time_taken;
					_rif40_sql_test_end(p_conString, p_mutexjs, p_client1, p_client2, p_j, p_tests, 
							p_passed_or_failed, p_failed_flag, p_rif40_test_harness, p_test_text, p_start_time, p_mutex_id,
							p_rif40_test_harness_results);
				}); /* End of process results - run.on('end', ... ) */
			} /* End of if (err) */
		});					
}	

/* 
 * Function: 	_rif40_sql_test_end()
 * Parameters: 	Connection string, MutexJS package handle, Master client (1) connection, worker thread client (2) connection, test number, number of tests, 
 *              {number passed, number failed}, failed flag, test harness results array, textual description of test, time of test harness start, 
 *				Test Mutex,
 *				results array
 * Returns:		Nothing
 * Description: Rollback test case, recurse to next test case
 */
function _rif40_sql_test_end(p_conString, p_mutexjs, p_client1, p_client2, p_j, p_tests, 
				p_passed_or_failed, p_failed_flag, p_rif40_test_harness, p_test_text, p_start_time, p_mutex_id,
				p_rif40_test_harness_results) {
					
	var next=p_j+1;
	
	// Check failed flag is NOT undefined	
	if (p_failed_flag === undefined) {
		console.error('1: ' + p_test_text + ' _rif40_sql_test_end() failed flag is undefined');			
		process.exit(1);	
	}
	// Check p_rif40_test_harness object of arrays is NOT undefined	
	if (p_rif40_test_harness === undefined) {
		console.error('1: ' + p_test_text + ' _rif40_sql_test_end() p_rif40_test_harness object of arrays is undefined');			
		process.exit(1);	
	}
	if (p_rif40_test_harness[p_j-1] === undefined) {
		console.error('2: ' + p_test_text + ' _rif40_sql_test_end() p_rif40_test_harness[' + p_j-1 + '] is undefined');			
		process.exit(1);							
	}		
	if (p_rif40_test_harness[p_j-1].test_stmt === undefined) {
		console.error('2: ' + p_test_text + ' _rif40_sql_test_end() p_rif40_test_harness[' + p_j-1 + '].p_test_stmt is undefined');			
		process.exit(1);							
	}
// Check mutex defined
	if (p_mutexjs === undefined) {
		console.error('1: _rif40_sql_test_end(' + p_j + ') p_mutexjs is undefined');			
		process.exit(1);							
	}	
	if (p_mutex_id === undefined) {
		console.error('1: _rif40_sql_test_end(' + p_j + ') p_mutex_id is undefined');			
		process.exit(1);							
	}	
	// Check p_rif40_test_harness_results object of arrays is NOT undefined	
	if (p_rif40_test_harness_results === undefined) {
		console.error('1: ' + test + ' _rif40_sql_test_end() p_rif40_test_harness_results object of arrays is undefined');			
		process.exit(1);	
	}
	if (p_rif40_test_harness_results[p_rif40_test_harness[p_j-1].test_run_class] === undefined) {
		console.error('1: ' + test + ' _rif40_sql_test_end() p_rif40_test_harness_results[p_rif40_test_harness[' + p_j-1 + 
			'].test_run_class] object of arrays is undefined');			
		process.exit(1);	
	}
		
	if (p_rif40_test_harness[p_j-1].recursive_test_number == p_rif40_test_harness[p_j-1].recursive_test_total) {	
		var end = p_client2.query('ROLLBACK', function(err, result) {
			if (err) {
				p_client2.end();
				console.error('2: Error in ROLLBACK transaction;', err);
				p_client1.end();			
				process.exit(1);			
			}
			else {
				// Transaction ROLLBACK OK 
				end.on('end', function(result) {	
					console.log('2: ROLLBACK transaction: ' + p_test_text);
//
// OK - done last test					
					if (p_j === p_tests) {
// Release Mutex
//						console.log('1: [' + p_j + '/' + p_tests + '] ' + ' Release test mutex: ' + p_mutex_id);
						p_mutexjs.release(p_mutex_id);
						
//
// This is no longer recursive, replaced with a for loop and a Mutex lock
//						

						var k = 1;
// Now this is no longer recursive it needs to be updated - as a {} it should be passed by reference!						
//						p_passed_or_failed.passed=0;
//						p_passed_or_failed.failed=0;
						for (; k <= p_j; k++) { 	
							(function(p_k) {
								process.nextTick(function() {						
//									console.log('1: p_k: ' + p_k + ', p_j: ' + p_j);
									try {
										if (p_k > p_j) {
											console.error('1: _rif40_sql_test_end() p_k (' + p_k + ') > p_j (' + p_j + ')');				
											process.exit(1);
										}
										var mutex_id;
										var mutex_name = 'db_test_harness.js-update';	
/*										if (p_rif40_test_harness[p_k-1].pass) {
											p_passed_or_failed.passed++;
										}
										else {
											p_passed_or_failed.failed++;
										} */
										p_mutexjs.lock(mutex_name, function(id) {
											mutex_id=id;
//											console.log('1: Gained update mutex [' + p_k + '/' + p_tests + ']: ' + mutex_name + ', id: ' + mutex_id);
											end_test_harness(p_conString, p_mutexjs, p_client1, p_client2, p_passed_or_failed, p_tests, p_k, p_failed_flag, 
													p_rif40_test_harness, p_start_time, mutex_id, p_rif40_test_harness_results); 
										});
									}
									catch(err) {
										console.error('1: _rif40_sql_test_end() Could not acquire test Mutex: ' + mutex_name, err);				
										process.exit(1);
									}					
								});	
							})(k);							
						}				
					}
					else {
//
// Recursion replaced by a Mutex lock
// Release Mutex
//						console.log('1: [' + p_j + '/' + p_tests + '] ' + ' Release test mutex: ' + p_mutex_id);
						p_mutexjs.release(p_mutex_id);
//									// Recurse to next test
	
						console.log('1: DEPENDENT Recurse; next: ' + next);
//						rif40_sql_test(p_mutexjs, p_client1, p_client2, next, p_tests, 
//								p_passed_or_failed, p_failed_flag, p_rif40_test_harness, p_start_time, p_mutex_id);
					}													
				});
			}
		});	/* End of rollback */	
	}
	else {
//
// Recursion replaced by a Mutex lock
// Release Mutex
//		console.log('1: [' + p_j + '/' + p_tests + '] ' + ' Release test mutex: ' + p_mutex_id);
		p_mutexjs.release(p_mutex_id);
//									// Recurse to next test
	
		console.log('1: DEPENDENT Recurse; next: ' + next);
//		rif40_sql_test(p_mutexjs, p_client1, p_client2, next, p_tests, 
//				p_passed_or_failed, p_failed_flag, p_rif40_test_harness, p_start_time, p_mutex_id);		
	}
}
				
/* 
 * Function: 	test_result()
 * Parameters: 	pass or fail flag, test rsult text, SQL statement, test harness results array, index to array
 * Returns:		Nothing
 * Description: Print test results
 */
function test_result(p_pass, p_text, p_sql_stmt, p_rif40_test_harness, p_j) {

	// Check p_rif40_test_harness object of arrays is NOT undefined	
	if (p_rif40_test_harness === undefined) {
		console.error('2: ' + test + ' test_result() p_rif40_test_harness object of arrays is undefined');			
		process.exit(1);	
	}
	if (p_rif40_test_harness[p_j] === undefined) {
		console.error('2: ' + test + ' test_result() p_rif40_test_harness[' + p_j + '] is undefined');			
		process.exit(1);							
	}		
	if (p_rif40_test_harness[p_j].test_stmt === undefined) {
		console.error('2: ' + test + ' test_result() p_rif40_test_harness[' + p_j + '].p_test_stmt is undefined');			
		process.exit(1);							
	}
	var r_text;
	if (p_rif40_test_harness[p_j].recursive_test_number != p_rif40_test_harness[p_j].recursive_test_total) {	
		r_text = '[Recursive test ' + p_rif40_test_harness[p_j].recursive_test_number + '/' + p_rif40_test_harness[p_j].recursive_test_total + '] ';
	}
	else if (p_rif40_test_harness[p_j].recursive_test_number == 1) {
		r_text = '';		
	}
	else {
		r_text = '[Recursive test ' + p_rif40_test_harness[p_j].recursive_test_number + '/' + p_rif40_test_harness[p_j].recursive_test_total + '] ';
	}
	if (p_pass) {
			console.log('*****************************************************************************\n' + '*\n' +
				'* 2: ' + r_text + p_text + '\n' + 
				'*\n' + '*****************************************************************************\n');
	}
	else {
			console.log('*****************************************************************************\n' + '*\n' +
				'* 2: ' + p_text + '\n' + 
				'*\n' + 
				'SQL> ' + p_sql_stmt + ';' + '\n' + 
				'*\n* [Parameter 1: test_stmt                 VARCHAR]\nSQL>>>' + p_rif40_test_harness[p_j].test_stmt + ';\n' + 
				'* [Parameter 2: test_case_title              VARCHAR]  ' + p_rif40_test_harness[p_j].test_case_title + '\n' + 
				'* [Parameter 3: results                      TEXT[][]] ' + p_rif40_test_harness[p_j].results + '\n' + 
				'* [Parameter 4: results_xml                  XML]      ' + p_rif40_test_harness[p_j].results_xml + '\n' + 
				'* [Parameter 5: pg_error_code_expected       VARCHAR]  ' + p_rif40_test_harness[p_j].pg_error_code_expected + '\n' + 
				'* [Parameter 6: raise_exception_on_failure   BOOLEAN]  ' + p_rif40_test_harness[p_j].raise_exception_on_failure + '\n' + 
				'* [Parameter 7: test_id                      INTEGER]  ' + p_rif40_test_harness[p_j].test_id + '\n' + 
				'*\n' + '*****************************************************************************\n');
	}
}

/* 
 * Function: 	end_test_harness()
 * Parameters: 	Connection string, MutexJS package handle, Master client (1) connection, worker thread client (2) connection, {tests passed, tests failed}, number of tests, 
 *              failed flag, test harness results array, time of test harness start, Update Mutex,
 *				results array  
 * Returns:		Nothing
 * Description: End processing for tes harness. Recursive, called for each test from test 1
 *
 *				Check p_failed_flag, p_rif40_test_harness are defined correctly
 *				For first test:
 *					Summarize run 
 *					BEGIN update transaction
 *				call _end_test_harness() which:
 * 					Do update
 * 					Summarise result
 *					On final test, COMMIT and exit with number of failed tests
 *					unlock mutex to allow next test resuklt to be updated
 */
function end_test_harness(p_conString, p_mutexjs, p_client1, p_client2, p_passed_or_failed, p_tests, p_j, p_failed_flag, p_rif40_test_harness, 
		p_start_time, p_mutex_id, p_rif40_test_harness_results) {
					
// Check failed flag is NOT undefined	
	if (p_failed_flag === undefined) {
		console.error('1: end_test_harness(' + p_j + ') failed flag is undefined');			
		process.exit(1);	
	}
	// Check p_rif40_test_harness object of arrays is NOT undefined	
	if (p_rif40_test_harness === undefined) {
		console.error('1: end_test_harness(' + p_j + ') p_rif40_test_harness object of arrays is undefined');			
		process.exit(1);	
	}
	if (p_rif40_test_harness[p_j-1] === undefined) {
		console.error('1: end_test_harness() p_rif40_test_harness[' + p_j-1 + '] is undefined');			
		process.exit(1);							
	}		
	if (p_rif40_test_harness[p_j-1].pass === undefined) {
		console.error('1: end_test_harness() p_rif40_test_harness[' + p_j-1 + '].pass is undefined');			
		process.exit(1);							
	}	
	if (p_rif40_test_harness[p_j-1].time_taken === undefined) {
		console.error('1: end_test_harness() p_rif40_test_harness[' + p_j-1 + '].time_taken is undefined');			
		process.exit(1);							
	}	
	if (p_rif40_test_harness[p_j-1].test_id === undefined) {
		console.error('1: end_test_harness() p_rif40_test_harness[' + p_j-1 + '].test_id is undefined');			
		process.exit(1);							
	}	
// Check mutex defined
	if (p_mutexjs === undefined) {
		console.error('1: end_test_harness(' + p_j + ') p_mutexjs is undefined');			
		process.exit(1);							
	}	
	if (p_mutex_id === undefined) {
		console.error('1: end_test_harness(' + p_j + ') p_mutex_id is undefined');			
		process.exit(1);							
	}		
	
	// Summarize run
	if (p_j == 1) {
		if (p_failed_flag) {
				console.log('1: Ran with -F flag to re-run failed tests only');
		}
		if (p_passed_or_failed.p_failed > 0) {
			console.error('1: Test harness complete; ' + p_tests + ' tests completed; passed: ' + p_passed_or_failed.p_passed +
				'; failed: ' + p_passed_or_failed.p_failed);	
		}
		else {
			console.log('1: Test harness complete; ' + p_tests + ' tests completed; passed: ' + p_passed_or_failed.p_passed +
				'; none failed.');	
		}
		
		// BEGIN update transaction
		var begin = p_client1.query('BEGIN', function(err, result) {
			if (err) {
				p_client2.end();
				console.error('1: Error in BEGIN transaction: results UPDATE;', err);
				p_client1.end();			
				process.exit(1);
			}
			else {
				// Transaction start OK 
				begin.on('end', function(result) {						
					console.log('1: BEGIN transaction: results UPDATE');

					console.log('1: Test run results by test >>>');
					_end_test_harness(p_conString, p_mutexjs, p_client1, p_client2, p_passed_or_failed, p_tests, p_j, p_failed_flag, 
							p_rif40_test_harness, p_start_time, p_mutex_id, p_rif40_test_harness_results);
				});
			}
		});
	}
	else {
		_end_test_harness(p_conString, p_mutexjs, p_client1, p_client2, p_passed_or_failed, p_tests, p_j, p_failed_flag, 
					p_rif40_test_harness, p_start_time, p_mutex_id, p_rif40_test_harness_results);
	}
}

/* 
 * Function: 	_end_test_harness()
 * Parameters: 	Connection string, MutexJS package handle, Master client (1) connection, worker thread client (2) connection, {tests passed, tests failed}, number of tests, 
 *              failed flag, test harness results array, time of test harness start, Update Mutex,
 *				results array 
 * Returns:		Nothing
 * Description: Helper functions for test harness end processing; called from end_test_harness().
 *
 * 				Do update to rif40_test_harness and insert into rif40_test_runs
 * 				Summarise result
 *				On final test, COMMIT and exit with number of failed tests
 *				unlock mutex to allow next test resuklt to be updated
 */
function _end_test_harness(p_conString, p_mutexjs, p_client1, p_client2, p_passed_or_failed, p_tests, p_j, p_failed_flag, p_rif40_test_harness, 
				p_start_time, p_mutex_id, p_rif40_test_harness_results) {
	// Check p_rif40_test_harness_results object of arrays is NOT undefined	
	if (p_rif40_test_harness_results === undefined) {
		console.error('1: ' + test + ' rif40_sql_test() p_rif40_test_harness_results object of arrays is undefined');			
		process.exit(1);	
	}
	if (p_rif40_test_harness_results[p_rif40_test_harness[p_j-1].test_run_class] === undefined) {
		console.error('1: ' + test + ' rif40_sql_test() p_rif40_test_harness_results[p_rif40_test_harness[' + p_j-1 + 
			'].test_run_class] object of arrays is undefined');			
		process.exit(1);	
	}
	
// Bindable update statement
	var update_stmt='UPDATE rif40_test_harness\n' +
					'   SET pass       = $1,\n' + 
					'       time_taken = $2,\n' + 
					'       test_date  = statement_timestamp()\n' +
					' WHERE test_id = $3';
					
	// Do update to rif40_test_harness
	var update = p_client1.query({
						text: update_stmt, 
						values: [p_rif40_test_harness[p_j-1].pass,
								 p_rif40_test_harness[p_j-1].time_taken,
								 p_rif40_test_harness[p_j-1].test_id
								]},
				function(err, result) {
					if (err) {
						p_client2.end();
						console.error('1: Error in UPDATE: ' + update_stmt + '; ', err);
						p_client1.end();			
						process.exit(1);			
					}
					else { // UPDATE OK 
						update.on('end', function(result) {	
						    // Summarise result
							var r_text;
							if (p_rif40_test_harness[p_j-1].recursive_test_number != p_rif40_test_harness[p_j-1].recursive_test_total) {	
								r_text = '[Recursive test ' + p_rif40_test_harness[p_j-1].recursive_test_number + '/' + p_rif40_test_harness[p_j-1].recursive_test_total + '] ';
							}
							else if (p_rif40_test_harness[p_j-1].recursive_test_number == 1) {
								r_text = '';		
							}
							else {
								r_text = '[Recursive test ' + p_rif40_test_harness[p_j-1].recursive_test_number + '/' + p_rif40_test_harness[p_j-1].recursive_test_total + '] ';
							}
							console.log('1: [' + p_j + '/' + p_tests + '] ' + r_text + 
								'; id: ' +  p_rif40_test_harness[p_j-1].test_id + 
								'; expected: ' + p_rif40_test_harness[p_j-1].expected_result + 
								'; pass: ' + p_rif40_test_harness[p_j-1].pass + 
								'; time taken: ' + p_rif40_test_harness[p_j-1].time_taken + ' S; ' + p_rif40_test_harness[p_j-1].test_case_title);	
								// On final test, COMMIT and exit with number of failed tests
								if (p_j == p_tests) {
									
// Individual tests update comple, now summarise and INSERT record in rif40_test_runs
//								
									var r_test_run_title = [];								
									var r_tests = [];								
									var r_passed = [];								
									var r_failed = [];									
									var r_time_taken = [];
									var l=0;
									var non_runs=0;
									console.log('1: Test run results by test class >>>');
									for (var obj in p_rif40_test_harness_results) {

										if ((p_rif40_test_harness_results[obj].tests === 0)&&
										    (p_rif40_test_harness_results[obj].passed === 0)&&
											(p_rif40_test_harness_results[obj].failed === 0)&&
											(p_rif40_test_harness_results[obj].time_taken === 0)) {
											console.log('1: ' + obj + ' not run');		
											non_runs++;
										}
										else {								
//											console.log('1: ' + obj + 
//												': tests: ' + p_rif40_test_harness_results[obj].tests + 
//												', passed: ' + p_rif40_test_harness_results[obj].passed + 
//												', failed: ' + p_rif40_test_harness_results[obj].failed + 
//												', time taken: ' + p_rif40_test_harness_results[obj].time_taken);	
											r_test_run_title[l] = obj;
											r_tests[l] = p_rif40_test_harness_results[obj].tests;
											r_passed[l] = p_rif40_test_harness_results[obj].passed;
											r_failed[l] = p_rif40_test_harness_results[obj].failed;
											r_time_taken[l] = p_rif40_test_harness_results[obj].time_taken;

											l++;												
										}
									}

// Check if any test cases where zero									
									if (non_runs > 0) {
										console.error('1: ' + non_runs + ' non runs where time_taken, tests_run, number_passed, number_failed are all zero');
										p_client2.end();
										p_client1.end();			
										process.exit(1);	
									}
	
// Do array insert into rif40_test_runs
/*
1: INSERT INTO rif40_test_runs; [{"command":"INSERT","rowCount":3,"oid":0,"rows":[{"test_run_id":1,"test_run_title":"rif40_create_disease_ma
pping_example","test_date":"2015-09-29T14:09:27.311Z","time_taken":"20.166999999999998","username":"pch","tests_run":7,"number_passed":7,"nu
mber_failed":0,"number_test_cases_registered":0,"number_messages_registered":0},{"test_run_id":2,"test_run_title":"test_8_triggers.sql","tes
t_date":"2015-09-29T14:09:27.311Z","time_taken":"14.373999999999999","username":"pch","tests_run":8,"number_passed":8,"number_failed":0,"num
ber_test_cases_registered":0,"number_messages_registered":0},{"test_run_id":3,"test_run_title":"trgf_rif40_studies","test_date":"2015-09-29T
14:09:27.311Z","time_taken":"172.373","username":"pch","tests_run":26,"number_passed":26,"number_failed":0,"number_test_cases_registered":0,
"number_messages_registered":0}],"fields":[{"name":"test_run_id","tableID":868973,"columnID":1,"dataTypeID":23,"dataTypeSize":4,"dataTypeMod
ifier":-1,"format":"text"},{"name":"test_run_title","tableID":868973,"columnID":2,"dataTypeID":1043,"dataTypeSize":-1,"dataTypeModifier":-1,
"format":"text"},{"name":"test_date","tableID":868973,"columnID":3,"dataTypeID":1184,"dataTypeSize":8,"dataTypeModifier":-1,"format":"text"}
,{"name":"time_taken","tableID":868973,"columnID":4,"dataTypeID":1700,"dataTypeSize":-1,"dataTypeModifier":-1,"format":"text"},{"name":"user
name","tableID":868973,"columnID":5,"dataTypeID":1043,"dataTypeSize":-1,"dataTypeModifier":94,"format":"text"},{"name":"tests_run","tableID"
:868973,"columnID":6,"dataTypeID":23,"dataTypeSize":4,"dataTypeModifier":-1,"format":"text"},{"name":"number_passed","tableID":868973,"colum
nID":7,"dataTypeID":23,"dataTypeSize":4,"dataTypeModifier":-1,"format":"text"},{"name":"number_failed","tableID":868973,"columnID":8,"dataTy
peID":23,"dataTypeSize":4,"dataTypeModifier":-1,"format":"text"},{"name":"number_test_cases_registered","tableID":868973,"columnID":9,"dataT
ypeID":23,"dataTypeSize":4,"dataTypeModifier":-1,"format":"text"},{"name":"number_messages_registered","tableID":868973,"columnID":10,"dataT
ypeID":23,"dataTypeSize":4,"dataTypeModifier":-1,"format":"text"}],"_parsers":[null,null,null,null,null,null,null,null,null,null],"rowAsArra
y":false} rows]; SQL> INSERT INTO rif40_test_runs(test_run_title,
                time_taken, tests_run, number_passed, number_failed,
                number_test_cases_registered, number_messages_registered)
SELECT
                unnest('{rif40_create_disease_mapping_example,test_8_triggers.sql,trgf_rif40_studies}'::VARCHAR[]) AS test_run_title,
                unnest('{20.166999999999998,14.373999999999999,172.373}'::NUMERIC[]) AS time_taken,
                unnest('{7,8,26}'::INTEGER[]) AS tests_run,
                unnest('{7,8,26}'::INTEGER[]) AS number_passed,
                unnest('{0,0,0}'::INTEGER[]) AS number_failed,
                0::INTEGER AS number_test_cases_registered,
                0::INTEGER AS number_messages_registered 
RETURNING *;
 */
									var insert_stmt='INSERT INTO rif40_test_runs(test_run_title,\n' +
									'		time_taken, tests_run, number_passed, number_failed,\n' +
									'		number_test_cases_registered, number_messages_registered)\n' +
									'SELECT \n' +
									'		unnest(\'{' + r_test_run_title + '}\'::VARCHAR[]) AS test_run_title,\n' +
									'		unnest(\'{' + r_time_taken + '}\'::NUMERIC[]) AS time_taken,\n' +
									'		unnest(\'{' + r_tests + '}\'::INTEGER[]) AS tests_run,\n' +
									'		unnest(\'{' + r_passed + '}\'::INTEGER[]) AS number_passed,\n' +
									'		unnest(\'{' + r_failed + '}\'::INTEGER[]) AS number_failed,\n' +
									'		0::INTEGER AS number_test_cases_registered,\n' +
									'		0::INTEGER AS number_messages_registered /* db_test_harness.js does not register */\n' +
									'RETURNING *';	
										var p_test_run_title = 'db_test_harness.js';
										if (p_failed_flag) {
											p_test_run_title = p_test_run_title + ' -F';
										}	
										var time_taken = (Date.now()-p_start_time)/1000;
										var insert = p_client1.query(insert_stmt, 				 
											function(err, result) {
												if (err) {
													p_client2.end();
													console.error('1: Error in INSERT INTO rif40_test_runs; SQL> ' + 
														insert_stmt + ';\n', err);
													p_client1.end();			
													process.exit(1);														
												}
												else {
													insert.on('row', function(row) {
														//fired once for each row returned
//														console.log('1: INSERT ROW!!!!');
														result.addRow(row);
													});
													insert.on('end', function(result) {
//														console.log('1: INSERT INTO rif40_test_runs; [' + JSON.stringify(result) + ' rows]; SQL> ' + 
//															insert_stmt + ';');															
														if (result.rowCount === 0) {
															p_client2.end();
															console.error('1: No rows inserted by INSERT INTO rif40_test_runs; SQL> ' + 
																insert_stmt + ';\n');
															p_client1.end();			
															process.exit(1);															
														}		
														else {
															for (m = 1; m <= result.rowCount; m++) { 
																console.log('1: ' + result.rows[m-1].test_run_title + 
																	': tests: ' + result.rows[m-1].tests_run + 
																	', passed: ' + result.rows[m-1].number_passed + 
																	', failed: ' + result.rows[m-1].number_failed + 
																	', time taken: ' + result.rows[m-1].time_taken);
															}
														}
														// Commit
														var commit = p_client1.query('COMMIT', function(err, result) {
															if (err) {
																p_client2.end();
																console.error('1: Error in COMMIT transaction;', err);
																p_client1.end();			
																process.exit(1);			
															}
															else {
																// Transaction COMMIT OK 
																commit.on('end', function(result) {	
																	console.log('1: COMMIT transaction.');
																	
																	p_client2.end();
																	p_client1.end();	
																	
	//																console.log('1: [' + p_j + '/' + p_tests + '] ' + ' Release update mutex: ' + p_mutex_id);
																	p_mutexjs.release(p_mutex_id);
										
																	var msg;
																	if (p_passed_or_failed.p_failed == 0) {
																		msg = '* Test harness run had no error(s); ' + p_passed_or_failed.p_passed + ' passed\n';																	
																	}
																	else if (p_passed_or_failed.p_failed == 1) {
																		msg = '* Test harness run had: 1 error; ' + p_passed_or_failed.p_passed + ' passed\n';																	
																	}
																	else {
																		msg = '* Test harness run had: ' + p_passed_or_failed.p_failed + ' errors; ' + 
																			p_passed_or_failed.p_passed + ' passed\n';
																	}
																	msg = msg + '* Total time taken: ' + time_taken + ' S\n';
																	msg = msg + '* Connection string: ' + p_conString + '\n';
																	// Exit point on "normal" run. Fails if any tests are failed
																	console.log('*****************************************************************************\n' + '*\n' +
																		msg + 
																		'*\n' + '*****************************************************************************\n');
																	var passed_and_failed=p_passed_or_failed.p_passed + p_passed_or_failed.p_failed;
																	if (passed_and_failed === p_tests) {
																		process.exit(p_passed_or_failed.p_failed);
																	}
																	else {
																		console.error('1: Number of tests (' + p_tests + ') != passed+failed: (' + passed_and_failed + ')');
																		process.exit(1);	
																	}
																});
															}
														});
													});
												}
											});												
								} /* At last test */
//
// Recursion replaced by a Mutex lock
//
								else {
									// Release Mutex
//									console.log('1: [' + p_j + '/' + p_tests + '] ' + ' Release update mutex: ' + p_mutex_id);
									p_mutexjs.release(p_mutex_id);
//									// Recurse to next test
//									end_test_harness(p_mutexjs, p_client1, p_client2, p_passed_or_failed, 
//											p_tests, p_j + 1, p_failed_flag, p_rif40_test_harness, p_start_time); 
								}
							});
					}
	});		

} 

main();

//
// Eof

