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
// Rapid Enquiry Facility (RIF) - RIF 4.0 Database SQL server tile maker.
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
// Usage: mssqlTileMaker [options]
//
// Version: 0.1
//
// RIF 4.0 Database SQL server tile maker.
//
// Options:
// -D, --database    name of SQL server database           [default: <user default>]
// -U, --username    SQL server database username          [default: NONE (use trusted connection)]
// --password, --pw  SQL server database password
//  -H, --hostname  hostname of SQL server database        [default: "localhost"]
//  -V, --verbose   Verbose mode                           [default: 0: false; 1 or 2]
//  -X, --xmlfile   XML Configuration file                 [default: "geoDataLoader.xml"]
//  -p, --pngfile   Make SVG/PNG files                     [default: false]
//  -h, --help      display this helpful message and exit  [default: false]
//
// Defaults are detected from the SQL server environment setup
//
// E.g.
//
// node mssqlTileMaker.js -H wpea-rif1 -D sahsuland_dev -U pch
//
// Connects using SQL server native driver (not JDBC).
//
// Uses:
//
// https://github.com/brianc/node-mssql
// https://github.com/substack/node-optimist
//
// See: Node Makefile for build instructions
//
	  
//
// Load database module, tileMaker modules
//
const mssql=require('mssql'),
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
    .usage("Usage: \033[1mmssqlTileMaker.\033[0m [options]\n\n"
+ "Version: 0.1\n\n")

    .options("D", {
      alias: "database",
      describe: "name of SQL server database",
	  type: "string",
      default: mssql_default("SQLCMDDBNAME")
    })
    .options("U", {
      alias: "username",
      describe: "SQL server database username",
	  type: "string",
      default: mssql_default("SQLCMDUSER") 
    })
    .options("P", {
      alias: "password",
      describe: "SQL server database password",
	  type: "string",
      default: mssql_default("SQLCMDPASSWORD") 
    })		
    .options("H", {
      alias: "hostname",
      describe: "hostname of SQL server database",
	  type: "string",
      default: mssql_default("SQLCMDSERVER")
    })
    .options("v", {
      alias: "verbose",
      describe: "Verbose mode",
	  type: "integer",
      default: mssql_default("VERBOSE")
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
		progName:			'mssqlTileMaker',
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
		//		console.error("argv: " + JSON.stringify(argv, null, 4));							
				// Create Postgres client;
				mssql_db_connect(mssql, argv["hostname"] , argv["database"], argv["username"], argv["password"], argv["pngfile"], argv['zoomlevel'], argv['blocks'],
					tileMakerConfig, winston);		
			}
		});

	});
			
} /* End of main */

/* 
 * Function: 	mssql_default()
 * Parameters: 	SQL server variable name
 * Returns:		Defaulted value
 * Description: Setup SQL server defaults (i.e. use PGDATABASE etc from env or set sensible default
 *				Used by optimist
 */
function mssql_default(p_var) {
	var p_def;
	
	if (p_var == "SQLCMDDBNAME") {
		p_def=""; 			// User Default database
	}
	else if (p_var == "SQLCMDSERVER") {
		p_def="localhost" 	// default instance of SQL Server on the local computer
	}
	else if (p_var == "SQLCMDUSER") {
		if (p_def === undefined) {
			p_def="";
		}
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
 * Function: 	mssql_db_connect()
 * Parameters: 	SQL server package connection handle,
 *				database host, name, username, password, generate PNG files, max zoomlevel, tile blocks per processing trip, tileMakerConfig object, logging object
 * Returns:		Nothing
 * Description:	Connect to database, ...
 */
function mssql_db_connect(p_mssql, p_hostname, p_database, p_user, p_password, p_pngfile, maxZoomlevel, blocks, tileMakerConfig, winston) {
	var start = new Date().getTime();
	
	var endCallBack = function endCallBack(err) {
		if (err) {
			winston.log("error", "mssqlTileMaker.js exit due to SQL server error: %", err.message, err);
			process.exit(1);		
		}

//		console.error(JSON.stringify(winston, null, 4));
		
		// Arrays with output and error lines
		var messages = winston.winston.transports.memory.writeOutput;
		var errors = winston.winston.transports.memory.errorOutput;

		var end = new Date().getTime();
		var elapsedTime=(end - start)/1000; // in S
		
		winston.log("info", "mssqlTileMaker.js exit: OK;  took: " + elapsedTime + "s; " + (errors.length || 0) + " error(s); " + (messages.length || 0) + " messages(s)");
		process.exit(0);
	}

	var config = {
		driver: 'msnodesqlv8',
		server: p_hostname,
		requestTimeout: 300000, // 5 mins. Default 15s per SQL statement
		options: {
			trustedConnection: false,
			useUTC: true,
			appName: 'mssqlTileMaker.js',
			encrypt: true
		}
	};
	if (p_database != "") {
		config.database=p_database;
	}
//	console.error("p_user: " + p_user + "; p_password: " + p_password);
	if (p_user && p_user != "") {
		config.user=p_user;
		if (p_password && p_password != "") {
			config.password=p_password;
		}
		else {
			winston.log("error", 'Could not connect to SQL server client no password specified for user: %s', p_user);
			process.exit(1);	
		}
	}
	else {
		config.options.trustedConnection=true;
	}
	winston.log("info", 'About to connected to SQL server using: ' + JSON.stringify(config, null, 4));
	
// Connect to SQL server database
	try {
		var client1=p_mssql.connect(config, function(err) {
			if (err) {
				winston.log("error", 'Could not connect to SQL server client using: %s\nError: %s\nStack: %s', JSON.stringify(config, null, 4), err.message, err.stack);
				process.exit(1);	
			}
			else {				
				var DEBUG = (typeof v8debug === 'undefined' ? 'undefined' : _typeof(v8debug)) === 'object' || process.env.DEBUG === 'true' || process.env.VERBOSE === 'true';
				winston.log("info", 'Connected to SQL server using: ' + JSON.stringify(config, null, 4) + "; log level: " + winston.winston.level);

	// Call mssqlTileMaker()...
				tileMaker.dbTileMaker(p_mssql, client1, p_pngfile, tileMakerConfig, "MSSQLServer", endCallBack, maxZoomlevel, blocks, winston);
			} // End of else connected OK  hy
		}); // End of connect	
	}	
	catch(err) {
		winston.log("error", 'Exception connecting to SQL server client using: %s\nError: %s\nStack: %s', JSON.stringify(config, null, 4), err.message, err.stack);
		process.exit(1);	
	}

	// Notice message event processors
//	client1.on('notice', function(msg) {
//		  console.log('MSSQL: %s', msg);
//	});
}
	  
main();

//
// Eof