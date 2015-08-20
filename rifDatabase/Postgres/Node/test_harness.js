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
    topojson  = require('topojson'),
    escape    = require('pg-escape');
	
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
var conString = 'postgres://' + argv["username"] + '@' +  argv["hostname"] + '/' + argv["database"]; // Use PGHOST, native authentication (i.e. same as psql)
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
			return console.error('1: Could create postgres client using: ' + conString, err);
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
				result.addRow(row);
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
				
				for (j = 0; j <row_count; j++) { 
					p_test_run_class.push(test_array.rows[j].test_run_class /* Deep copy */);
					p_test_case_title.push(test_array.rows[j].test_case_title /* Deep copy */);
					p_test_stmt.push(test_array.rows[j].test_stmt /* Deep copy */);
					p_results.push(test_array.rows[j].results /* Deep copy */);
					p_results_xml.push(test_array.rows[j].results_xml /* Deep copy */);
					p_pg_error_code_expected.push(test_array.rows[j].pg_error_code_expected /* Deep copy */);	
					p_raise_exception_on_failure.push(test_array.rows[j].raise_exception_on_failure /* Deep copy */);
					p_test_id.push(test_array.rows[j].test_id /* Deep copy */);					
					}
				rif40_sql_test(p_client1, p_client2, 1, p_tests, p_test_run_class, p_test_case_title, 
						p_test_stmt, p_results, p_results_xml, p_pg_error_code_expected, p_raise_exception_on_failure, p_test_id);		
				return;
			});	
		}
	});				
}

function rif40_sql_test(p_client1, p_client2, p_j, p_tests, p_test_run_class, p_test_case_title, 
				p_test_stmt, p_results, p_results_xml, p_pg_error_code_expected, p_raise_exception_on_failure, p_test_id) {	
	
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
						escape.literal(p_test_stmt[p_j-1]) + '::VARCHAR /* test_stmt */,' + '\n' + 
						escape.literal(p_test_case_title[p_j-1]) + '::VARCHAR /* test_case_title */,' + '\n';
				if (p_results[p_j-1] === null) {
					sql_stmt = sql_stmt +	
						'NULL::VARCHAR[][] /* NULL results */,' + '\n';
				}
				else {
					sql_stmt = sql_stmt +		
						'\'' + escape.literal(p_results[p_j-1]) + '\'::VARCHAR[][] /* results */,' + '\n';
				}
				sql_stmt = sql_stmt +			
						escape.literal(p_results_xml[p_j-1]) + '::XML /* results_xml */,' + '\n' +
						escape.literal(p_pg_error_code_expected[p_j-1]) + '::VARCHAR /* pg_error_code_expected */,' + '\n' +
						p_raise_exception_on_failure[p_j-1] + '::BOOLEAN /* raise_exception_on_failure */,' + '\n' +
						p_test_id[p_j-1] + '::INTEGER /* test_id */)::INTEGER AS rcode';
				var run = p_client2.query(sql_stmt, function(err, result) {
					if (err) {
						console.error('2: Error in run test; SQL> ' + sql_stmt, err);	
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
						run.on('end', function(result) {	
							console.log('2: OK: ' + test);
							
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
											console.log('1: Test harness complete; ' + test_count + ' tests completed.');	
											/*
											p_client2.on('drain', function() {
												console.log('2: Disconnect.');
												p_client2.end();
											});	
											p_client1.on('drain', function() {
												console.log('1: Disconnect.');					
												p_client1.end();						
											}); */
											p_client2.end();
											p_client1.end();	
											process.exit(0);	
										} 
										else {
											rif40_sql_test(p_client1, p_client2, next, p_tests, p_test_run_class, p_test_case_title, 
													p_test_stmt, p_results, p_results_xml, p_pg_error_code_expected, 
													p_raise_exception_on_failure, p_test_id);								
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

main();

//
// Eof
