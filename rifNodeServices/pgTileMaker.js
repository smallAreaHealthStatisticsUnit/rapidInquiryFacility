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
// Rapid Enquiry Facility (RIF) - RIF 4.0 Database postgres tile maker.
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
// Usage: pgTileMaker [options]
//
// Version: 0.1
//
// RIF 4.0 Database postgres tile maker.
//
// Options:
// -D, --database  name of Postgres database              [default: "sahsuland_dev"]
// -U, --username  Postgres database username             [default: "peter"]
// -P, --port      Postgres database port                 [default: 5432]
// -H, --hostname  hostname of Postgres database          [default: "localhost"]
// -V, --verbose   Verbose mode                           [default: 0: false; 1 or 2]
// -X, --xmlfile   XML Configuration file
// -p, --pngfile   Make SVG/PNG files                     [default: false]
// -h, --help      display this helpful message and exit  [default: false]
//
// Defaults are detected from the Postgres environment setup
//
// E.g.
//
// node pgTileMaker.js -H wpea-rif1 -D sahsuland_dev -U pch
//
// Connects using Postgres native driver (not JDBC).
//
// Uses:
//
// https://github.com/brianc/node-postgres
// https://github.com/substack/node-optimist
//
// See: Node Makefile for build instructions
//
  
//
// Load database module, tileMaker modules
//
const pg=require('pg'),
	  tileMaker=require('./lib/tileMaker'),
	  Logger=require('./lib/Logger'),
      TileMakerConfig = require('./lib/TileMakerConfig'),
	  optimist = require('optimist');	

var winston;	  
/* 
 * Function: 	main()
 * Parameters: 	ARGV
 * Returns:		Nothing
 * Description: Create control database connecton, then run rif40_startup()
 *				Then _rif40_sql_test_log_setup() ...
 */	
function main() {
	
// Process Args using optimist
	var argv = optimist
    .usage("Usage: \033[1mpgTileMaker.\033[0m [options]\n\n" + "Version: 0.1\n\n")

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
    .options("v", {
      alias: "verbose",
      describe: "Verbose mode",
	  type: "integer",
      default: pg_default("VERBOSE")
    })
    .options("z", {
      alias: "zoomlevel",
      describe: "Maximum zoomlevel",
	  type: "integer",
      default: 11
    })
    .options("b", {
      alias: "blocks",
      describe: "Blocks of tiles to process at once",
	  type: "integer",
      default: 10
    })
    .options("X", {
      alias: "xmlfile",
      describe: "XML Configuration file",
	  type: "string",
      default: "geoDataLoader.xml"
    })
    .options("p", {
      alias: "pngfile",
      describe: "Make SVG/PNG files",
	  type: "boolean",
      default: false
    })
    .options("h", {
      alias: "help",
      describe: "display this helpful message and exit",
      type: "boolean",
      default: false
    })	
	 .check(function(argv) {
      if (argv.help) return;
    })
    .argv;

	if (argv.help) return optimist.showHelp();

	
	var LoggerParams = {
		progName:			'pgTileMaker',
		debugLevel: 		'info',
		memoryFileDebug:	'verbose'
	};
	if (argv.verbose == 1) {
		process.env.VERBOSE=true;
		LoggerParams.debugLevel='verbose';
		LoggerParams.memoryFileDebug='verbose';
	}
	else if (argv.verbose >= 2) {
		process.env.DEBUG=true;	
		LoggerParams.debugLevel='debug';
		LoggerParams.memoryFileDebug='debug';
	}
//	else {
//		console.error("argv.verbose: " + argv.verbose);
//	}
	
//
// Load logger module
//
	winston=new Logger.Logger(LoggerParams);
	
//
// TileMakerConfig
//
	var tileMakerConfig=new TileMakerConfig.TileMakerConfig(argv["xmlfile"]);	
	tileMakerConfig.parseConfig(function (err, data) {
		if (err) {
			winston.log("error", err.message, err);			
			process.exit(1);		
		}
		tileMakerConfig.setXmlConfig(data, (err) => { // callback
			if (!err) {
				
		//		console.error("Parsed: " + tileMakerConfig.xmlConfig.xmlFileDir + "/" + tileMakerConfig.xmlConfig.xmlFileName + "\n" +
		//			JSON.stringify(tileMakerConfig.xmlConfig, null, 4));
												
				// Create Postgres client;
				pg_db_connect(pg, argv["hostname"] , argv["database"], argv["username"], argv["port"], argv["pngfile"], argv['zoomlevel'], argv['blocks'],
					tileMakerConfig, winston);	
			}
		});
	});
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
				p_def="<NO USER DEFINED>";
			}
		}
	}	
	else if (p_var == "PGPORT") {
		p_def=5432;
	}
	
	if (p_var == "VERBOSE") {	
		p_def=0; // Disable verbose log messages
		if (process.env[p_var]) { 
			p_def=1; // Enable verbose log messages
		}
		else if (process.env["DEBUG"]) { 
			p_def=2; // Enable debug log messages
		}
	}
	else if (process.env[p_var]) { 
//		console.error(p_var + ": " + (process.env[p_var]||'Not defined'));
		return process.env[p_var];
	}
	else { 
		return p_def;
	}
}
	
/* 
 * Function: 	pg_db_connect()
 * Parameters: 	Postgres PG package connection handle,
 *				database host, name, username, port, generate PNG files, max zoomlevel, tile blocks per processing trip, tileMakerConfig object, logging object
 * Returns:		Nothing
 * Description:	Connect to database, ...
 */
function pg_db_connect(p_pg, p_hostname, p_database, p_user, p_port, p_pngfile, maxZoomlevel, blocks, tileMakerConfig, winston) {
	
	var client1 = null; // Client 1: Master; hard to remove	
	var start = new Date().getTime();
	
	var endCallBack = function endCallBack(err) {
		if (err) {
			winston.log("error", "pgTileMaker.js exit due to error: " + err.message, err);
			process.exit(1);		
		}
//		console.error(JSON.stringify(winston, null, 4));
		
		// Arrays with output and error lines
		var messages = winston.winston.transports.memory.writeOutput;
		var errors = winston.winston.transports.memory.errorOutput;

		var end = new Date().getTime();
		var elapsedTime=(end - start)/1000; // in S		
		winston.log("info", "pgTileMaker.js exit: OK; took: " + elapsedTime + "s; " + (errors.length || 0) + " error(s); " + (messages.length || 0) + " messages(s)");
		process.exit(0);
	}
	
	var conString = 'postgres://' + p_user + '@' + p_hostname + ':' + p_port + '/' + p_database + '?application_name=pgTileMaker';

	var DEBUG = (typeof v8debug === 'undefined' ? 'undefined' : _typeof(v8debug)) === 'object' || process.env.DEBUG === 'true' || process.env.VERBOSE === 'true';
	
// Use PGHOST, native authentication (i.e. same as psql)
	try {
		client1 = new p_pg.Client(conString);
	// Connect to Postgres database
		client1.connect(function(err) {
			if (err) {
				winston.log("error", 'Could not connect to postgres client using: ' + conString, err);
				if (p_hostname === 'localhost') {
					
	// If host = localhost, use IPv6 numeric notation. This prevent ENOENT errors from getaddrinfo() in Windows
	// when Wireless is disconnected. This is a Windows DNS issue. psql avoids this somehow.
	// You do need entries for ::1 in pgpass			

					winston.log("info", 'Attempt 2 (127.0.0.1 instead of localhost) to connect to Postgres using: ' + conString);
					conString = 'postgres://' + p_user + '@' + '[127.0.0.1]' + ':' + p_port + '/' + p_database + '?application_name=db_test_harness';
					client1 = new p_pg.Client(conString);
	// Connect to Postgres database
					client1.connect(function(err) {
						if (err) {
							winston.log("error", 'Could not connect [2nd attempt] to postgres client using: ' + conString, err);
							process.exit(1);	
						}
						else {
	// Call pgTileMaker()...
							winston.log("error", 'Connected to Postgres [2nd attempt] using: ' + conString + "; log level: " + winston.winston.level);		
							tileMaker.dbTileMaker(p_pg, client1,  p_pngfile, tileMakerConfig, "PostGres", endCallBack, maxZoomlevel, blocks, winston);
						} // End of else connected OK 
					}); // End of connect						
				}
			}
			else {			
	// Call pgTileMaker()...

				winston.log("info", 'Connected to Postgres using: ' + conString + "; log level: " + winston.winston.level);	
				tileMaker.dbTileMaker(p_pg, client1, p_pngfile, tileMakerConfig, "PostGres", endCallBack, maxZoomlevel, blocks, winston);
			} // End of else connected OK 
		}); // End of connect		
	}	
	catch(err) {
		winston.log("error", 'Exception connecting to Postgres client using: %s\nError: %s\nStack: %s', conString, err.message, err.stack);
		process.exit(1);	
	}
	
	// Notice message event processors
	client1.on('notice', function(msg) {
		  winston.log("info", 'PG: ' + msg);
	});
}
	
main();

//
// Eof