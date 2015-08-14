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
    topojson  = require('topojson');

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

var test_count=0;

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
	var sql_stmt = 'SELECT rif40_sql_pkg.rif40_startup() AS a';
			
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
	
//	client.on('drain', function(drain) {
//		console.log('_rif40_sql_test_log_setup() done.')	
//	});	
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
	var end = p_client2.query('COMMIT', function(err, result) {
		if (err) {
			p_client2.end();
			return console.error('2: Error in COMMIT transaction;', err);
		}
		else {
			// Transaction ROLLBACK OK 
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
								run_test_harness_test(i, row_count, p_client1, p_client2, result.rows[i-1].test_run_class, result.rows[i-1].tests);
							}
						});	
					}
				});	
	
			});
		}
	});		
}

function run_test_harness_test(p_i, p_classes, p_client1, p_client2, p_test_run_class, p_tests) {
	
	var sql_stmt = 'SELECT *\n' +
		'  FROM rif40_test_harness a\n' +
		' WHERE a.parent_test_id IS NULL\n' +
		'   AND a.test_run_class = \'' + p_test_run_class + '\'\n' +
		' ORDER BY a.test_id';
			
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
				// End of query processing - process results array
				row_count = result.rowCount;
				for (j = 1; j <= row_count; j++) { 
					rif40_sql_test(p_client1, p_client2, p_i, j, p_tests, p_classes, result.rows[j-1].test_run_class, result.rows[j-1].test_case_title);
				}
				return;
			});	
		}
	});				
}

function rif40_sql_test(p_client1, p_client2, p_i, p_j, p_tests, p_classes, p_test_run_class, p_test_case_title) {	

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
				var test='[' + p_i + '.' + p_j + '/' + p_tests + '] ' + p_test_case_title;
				if (j == 1) {
					console.log('2: [' + p_i + '/' + p_classes + '] Test run class: ' + p_test_run_class + '; tests: ' + p_tests);
				}
				console.log('2: ' + test);			
				console.log('2: BEGIN transaction: ' + test);			
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
							if ((p_i == p_classes)&&(p_j == p_tests)) {
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
