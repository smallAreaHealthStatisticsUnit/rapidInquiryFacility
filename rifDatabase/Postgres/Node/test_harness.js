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
// Usage: node topojson_convert.js <PGHOST; default: localhost> <geography; default: sahsu>
//
// Connects using Postgres native driver (not JDBC) as rif40.
//
// Uses:
//
// https://github.com/mbostock/topojson
// https://github.com/brianc/node-postgres
// https://github.com/substack/node-optimist
//
// See: Node Makefile for build instructions
//
var pg = require('pg'),
    optimist  = require('optimist'),
    topojson  = require('topojson') /*,
    escape    = require('pg-escape') */;
	
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
		p_def="";
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

// Process Args using optimist
var argv = optimist
    .usage("Usage: \033[1mtest_harness\033[0m [options] -- [test run class]\n\n"

+ "Version: 0.1\n\n"
+ "RIF 4.0 Database test harness.")

    .options("d", {
      alias: "debug",
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

// Create 2x Postgres clients; one for control, the second for running each test in turn.
var conString = 'postgres://' + argv["username"] + '@'; // Use PGHOST, native authentication (i.e. same as psql)
// If host = localhost, use IPv6 numeric notation. This prevent ENOENT errors from getaddrinfo() in Windows
// when Wireless is disconnected. This is a Windows DNS issue. psql avoids this somehow.
// You do need entries for ::1 in pgpass
if (argv["hostname"] == 'localhost') {
	conString=conString + '[::1]';
}
else {
	conString=conString + argv["hostname"];
}
if (argv["port"] != 5432) {
	conString=conString + ':' + argv["port"];
}
conString=conString + '/' + argv["database"] + '?application_name=db_test_harness';

var client = null;
var client2 = null;

// Test arrays
var test_count=0;
var p_test_run_class = [];
var p_test_case_title = [];	
var p_test_stmt = []; 
var p_results = []; 
var p_results_xml = []; 
var p_pg_error_code_expected = []; 
var p_raise_exception_on_failure = []; 
var p_test_id = [];
var p_expected_result = [];

var p_pass = [];
var p_time_taken = [];

/* 
 * Function: 	main()
 * Parameters: 	ARGV
 * Returns:		Nothing
 * Description: Create control database connecton, then run rif40_startup()
 *				Then _rif40_sql_test_log_setup() ...
 */	
function main() {
	try {
		client = new pg.Client(conString);
		console.log('1: Connect to Postgres using: ' + conString);
		
	}
	catch(err) {
			return console.error('1: Could not create postgres client using: ' + conString, err);
	}

// Notice message event processors
	client.on('notice', function(msg) {
		  console.log('1: %s', msg);
	});
		
// Connect to Postgres database
	client.connect(function(err) {
		if (err) {
			return console.error('1: Could not connect to postgres using: ' + conString, err);
		}
		else {
// Call rif40_startup; then subsequent functions in an async tree
			rif40_startup(client, 1);
		} // End of else connected OK 
	}); // End of connect
}

/* 
 * Function: 	rif40_startup()
 * Parameters: 	Client connection, connection number (1 or 2)
 * Returns:		Nothing
 * Description: Run rif40_startup()
 *				Then _rif40_sql_test_log_setup()
 */
function rif40_startup(p_client, p_num) {
	var sql_stmt;
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
				_rif40_sql_test_log_setup(p_client, p_num, argv["debug"]);
				return;
			});	
		}
	});	
}

/* 
 * Function: 	_rif40_sql_test_log_setup()
 * Parameters: 	Client connection, connection number (1 or 2), debug level
 * Returns:		Nothing
 * Description: Run _rif40_sql_test_log_setup()
 *				Then if you are the control connection (1) call init_test_harness()
 *				Otherwise can run_test_harness() using the control connection
 */
function _rif40_sql_test_log_setup(p_client, p_num, p_debug_level) {
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
				if (p_num == 1) {
					console.log('1: Wait for client 2 initialisation');
					init_test_harness(p_client /* client 2 */, p_num);
				}
				else {
					console.log('1: Client 2 initialised');
					run_test_harness(client, client2);
				}
				return;
			});	
		}
	});	
	
}

function init_test_harness(p_client2, p_num) {
// Create 2nd client for running test cases
	try {
		console.log('2: Connect to Postgres using: ' + conString);		
		client2 = new pg.Client(conString);
	}
	catch(err) {
			return console.error('2: Could create postgres 2nd client using: ' + conString, err);
	}
	client2.on('notice', function(msg) {
//	      console.log('2: %s', msg);
	});
	client2.connect(function(err) {
		if (err) {
			return console.error('2: Could not connect to postgres using: ' + conString, err);
		}
		else {
	// Call rif40_startup; then subsequent functions in an async tree
			rif40_startup(client2, 2);
		} // End of else connected OK 
	}); // End of connect
}
	
function run_test_harness(p_client1, p_client2) {
	var total_tests=0;
	var end = p_client2.query('COMMIT', function(err, result) {
		if (err) {
			p_client2.end();
			return console.error('2: Error in COMMIT transaction;', err);
		}
		else {
			// Transaction COMMIT OK 
			end.on('end', function(result) {	
				console.log('2: COMMIT transaction;');					
				
				var sql_stmt = 'SELECT test_run_class, COUNT(test_run_class) AS tests, MIN(register_date) AS min_register_date\n' +
					'  FROM rif40_test_harness a\n' +
					' WHERE parent_test_id IS NULL\n' +
					' GROUP BY test_run_class\n' +
					' ORDER BY 3';
						
				// Connected OK, run SQL query
				var query = p_client1.query(sql_stmt, function(err, result) {
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
							p_client2.on('notice', function(msg) {
								console.log('2: XX %s', msg);
							});			
							// End of query processing - process results array
							row_count = result.rowCount;
							for (i = 1; i <= row_count; i++) { 
								console.log('2: Class: %s Tests: %s', result.rows[i-1].test_run_class, result.rows[i-1].tests);
								total_tests=total_tests + parseInt(result.rows[i-1].tests, 10);
							}
							console.log('2: Total tests %s', total_tests);
							run_test_harness_test(p_client1, p_client2, total_tests);							
						});	
					}
				});	
	
			});
		}
	});		
}

function run_test_harness_test(p_client1, p_client2, p_tests) {

	var sql_stmt = 'SELECT a.*\n' +
		'  FROM rif40_test_harness a\n' +
		' WHERE a.parent_test_id IS NULL\n' +
		' ORDER BY a.test_id';
			
	// Connected OK, run SQL query
	var query = p_client1.query(sql_stmt, function(err, test_array) {
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
				
				if (row_count > 0) {
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

						p_pass.push(undefined);
						p_time_taken.push(undefined);						
						}
					rif40_sql_test(p_client1, p_client2, 1, p_tests, p_test_run_class, p_test_case_title, 
							p_test_stmt, p_results, p_results_xml, p_pg_error_code_expected, p_raise_exception_on_failure, p_test_id, p_expected_result,
							0 /* p_passed */, 0 /* p_failed */);
				}
				else {
					console.error('1: No tests to run');
					process.exit(1);					
				}
				return;
			});	
		}
	});				
}

function test_result(p_pass, p_text, p_sql_stmt,
				p_test_stmt, p_test_case_title, p_results, p_results_xml, p_pg_error_code_expected, p_raise_exception_on_failure, p_test_id) {
	if (p_pass) {
			console.log('*****************************************************************************\n' + '*\n' +
				p_text + '\n' + 
				'*\n' + '*****************************************************************************\n');
	}
	else {
			console.log('*****************************************************************************\n' + '*\n' +
				p_text + '\n' + 'SQL> ' + p_sql_stmt + ';' + '\n' + 
				'*\n* [Parameter 1: p_test_stmt                 VARCHAR]\nSQL>>>' + p_test_stmt + ';\n' + 
				'* [Parameter 2: p_test_case_title              VARCHAR]  ' + p_test_case_title + '\n' + 
				'* [Parameter 3: p_results                      TEXT[][]] ' + p_results + '\n' + 
				'* [Parameter 4: p_results_xml                  XML]      ' + p_results_xml + '\n' + 
				'* [Parameter 5: p_pg_error_code_expected       VARCHAR]  ' + p_pg_error_code_expected + '\n' + 
				'* [Parameter 6: p_raise_exception_on_failure   BOOLEAN]  ' + p_raise_exception_on_failure + '\n' + 
				'* [Parameter 7: p_test_id                      INTEGER]  ' + p_test_id + '\n' + 
				'*\n' + '*****************************************************************************\n');
	}
}

function rif40_sql_test(p_client1, p_client2, p_j, p_tests, p_test_run_class, p_test_case_title, 
				p_test_stmt, p_results, p_results_xml, p_pg_error_code_expected, p_raise_exception_on_failure, p_test_id, p_expected_result,
				p_passed, p_failed) {	
	
	var start_time=Date.now();
	var begin = p_client2.query('BEGIN', function(err, result) {
		var next=p_j+1;
		if (err) {
			p_client2.end();
			console.error('2: Error in BEGIN transaction;', err);
			p_client1.end();			
			process.exit(1);
		}
		else {
			// Transaction start OK 
			begin.on('end', function(result) {			
				var test='[' + p_j + '/' + p_tests + ':' + p_test_run_class[p_j-1] + '] ' + p_test_case_title[p_j-1];
				console.log('1: ' + test);			
				console.log('2: BEGIN transaction: ' + test);		

				// Run test
				var sql_stmt = 'SELECT rif40_sql_pkg._rif40_sql_test(' + '\n' + 
						'$1::VARCHAR 	/* test_stmt */,' + '\n' + 
						'$2::VARCHAR 	/* test_case_title */,' + '\n' + 
						'$3::Text[][] 	/* results */,' + '\n' + 
						'$4::XML 		/* results_xml */,' + '\n' +
						'$5::VARCHAR 	/* pg_error_code_expected */,' + '\n' +
						'$6::BOOLEAN 	/* raise_exception_on_failure */,' + '\n' +
						'$7::INTEGER 	/* test_id */) AS rbool';

				var run = p_client2.query({
							text: sql_stmt, 
							values: [p_test_stmt[p_j-1],
									 p_test_case_title[p_j-1], 
									 p_results[p_j-1], 
								     p_results_xml[p_j-1],
									 p_pg_error_code_expected[p_j-1],
									 p_raise_exception_on_failure[p_j-1],
									 p_test_id[p_j-1]]}, 
						function(err, result) {
					if (err) {
						console.error('2: Error in run test; SQL> ' + sql_stmt + 
				'*\n* [Parameter 1: p_test_stmt                 VARCHAR]\nSQL>>>' + p_test_stmt[p_j-1] + ';\n' + 
				'* [Parameter 2: p_test_case_title              VARCHAR]  ' + p_test_case_title[p_j-1] + '\n' + 
				'* [Parameter 3: p_results                      TEXT[][]] ' + p_results[p_j-1] + '\n' + 
				'* [Parameter 4: p_results_xml                  XML]      ' + p_results_xml[p_j-1] + '\n' + 
				'* [Parameter 5: p_pg_error_code_expected       VARCHAR]  ' + p_pg_error_code_expected[p_j-1] + '\n' + 
				'* [Parameter 6: p_raise_exception_on_failure   BOOLEAN]  ' + p_raise_exception_on_failure[p_j-1] + '\n' + 
				'* [Parameter 7: p_test_id                      INTEGER]  ' + p_test_id[p_j-1] + '\n' + 
				'*\n' + '*****************************************************************************\n', err);	
						console.error('2: ROLLBACK transaction: ' + test);
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
						// End of run processing - process results 					
						run.on('end', function(result) {
							row_count = result.rowCount;	
							console.log('rbool: ' + result.rows[0].rbool + '; p_expected_result[p_j-1]: ' + p_expected_result[p_j-1]);
							if (row_count != 1) {
									console.error('2: Test FAILED: (' + row_count + ') rows ' + test + '\n' + 'SQL> ' + sql_stmt);	
									p_failed++;			
							}
							else if (result.rows[0].rbool == false)   /* Test failed */ {
								if (p_expected_result[p_j-1] == true) /* It was expected to pass */ {
									test_result(false, '2: Test FAILED, expected to PASS: ' + test, sql_stmt,
											p_test_stmt[p_j-1],
											p_test_case_title[p_j-1], 
											p_results[p_j-1], 
											p_results_xml[p_j-1],
											p_pg_error_code_expected[p_j-1],
											p_raise_exception_on_failure[p_j-1],
											p_test_id[p_j-1]);
									p_pass[p_j-1]=false;
									p_failed++;
								}
								else {
									test_result(true, '2: Test FAILED as expected: ' + test, sql_stmt,
											p_test_stmt[p_j-1],
											p_test_case_title[p_j-1], 
											p_results[p_j-1], 
											p_results_xml[p_j-1],
											p_pg_error_code_expected[p_j-1],
											p_raise_exception_on_failure[p_j-1],
											p_test_id[p_j-1]);
									p_pass[p_j-1]=true;
									p_passed++; 										
								}
							}
							else { /* Test passed */
								if (p_expected_result[p_j-1] == true) /* It was expected to pass */ {								
									test_result(true, '2: Test OK: ' + test, sql_stmt,
											p_test_stmt[p_j-1],
											p_test_case_title[p_j-1], 
											p_results[p_j-1], 
											p_results_xml[p_j-1],
											p_pg_error_code_expected[p_j-1],
											p_raise_exception_on_failure[p_j-1],
											p_test_id[p_j-1]);
									p_pass[p_j-1]=true;
									p_passed++;
								}
								else {
									test_result(false, '2: Test PASSED, expected to FAIL: ' + test, sql_stmt,
											p_test_stmt[p_j-1],
											p_test_case_title[p_j-1], 
											p_results[p_j-1], 
											p_results_xml[p_j-1],
											p_pg_error_code_expected[p_j-1],
											p_raise_exception_on_failure[p_j-1],
											p_test_id[p_j-1]);
									p_pass[p_j-1]=false;										
									p_failed++;									
								}
							}
							p_time_taken[p_j-1] = (Date.now()-start_time)/1000;
							
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
										console.log('2: ROLLBACK transaction: ' + test);
										test_count++;
										if (p_j === p_tests) {
											end_test_harness(p_client1, p_client2, p_passed, p_failed, p_tests, p_test_case_title, p_test_id, p_pass, p_time_taken, 1); 
										}
										else {
											console.log('1: Recurse; next: ' + next);
											rif40_sql_test(p_client1, p_client2, next, p_tests, 
													p_test_run_class, p_test_case_title, 
													p_test_stmt, p_results, p_results_xml, 
													p_pg_error_code_expected, p_raise_exception_on_failure, p_test_id, p_expected_result,
													p_passed, p_failed);
										}													
									});
								}
							});	
						});
					}
				});					
			});			
		}
	});

}

function end_test_harness(p_client1, p_client2, p_passed, p_failed, p_tests, p_test_case_title, p_test_id, p_pass, p_time_taken, p_j) {
	var update_stmt='UPDATE rif40_test_harness\n' +
					'   SET pass       = $1,\n' + 
					'       time_taken = $2,\n' + 
					'       test_date  = statement_timestamp()\n' +
					' WHERE test_id = $3';
					
	if (p_j == 1) {
		if (p_failed > 0) {
			console.error('1: Test harness complete; ' + p_tests + ' tests completed; passed: ' + p_passed +
				'; failed: ' + p_failed);	
		}
		else {
			console.log('1: Test harness complete; ' + p_tests + ' tests completed; passed: ' + p_passed +
				'; none failed.');	
		}
	}

	var update = p_client1.query({
						text: update_stmt, 
						values: [p_pass[p_j-1],
								 p_time_taken[p_j-1],
								 p_test_id[p_j-1]
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
							console.log('1: [' + p_j + '/' + p_tests + '] ' + p_test_case_title[p_j-1] + 
								'; id: ' + p_test_id[p_j-1] + '; pass: ' + p_pass[p_j-1] + 
								'; time taken: ' + p_time_taken[p_j-1] + ' S');
								if (p_j == p_tests) {
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
													process.exit(p_failed);
													});
											}
										});
								}
								else {
									end_test_harness(p_client1, p_client2, p_passed, p_failed, p_tests, p_test_case_title, p_test_id, p_pass, p_time_taken, p_j + 1); 
								}
							});
					}
	});		

} 

main();

//
// Eof

