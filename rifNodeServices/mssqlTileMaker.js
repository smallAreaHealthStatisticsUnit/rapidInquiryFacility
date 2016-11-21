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
//  -V, --verbose   Verbose mode                           [default: false]
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

/* 
 * Function: 	main()
 * Parameters: 	ARGV
 * Returns:		Nothing
 * Description: Create control database connecton, then run rif40_startup()
 *				Then _rif40_sql_test_log_setup() ...
 */	
function main() {
	
	const optimist  = require('optimist');
	
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
    .options("password", {
      alias: "pw",
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
    .options("V", {
      alias: "verbose",
      describe: "Verbose mode",
	  type: "boolean",
      default: mssql_default("VERBOSE")
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
	if (argv.verbose) {
		process.env.VERBOSE=true;
	}
	
//
// Load database module
//
	try {
		mssql=require('mssql');
	}
	catch(err) {
		console.error('1: Could not load SQL server database module.', err);				
		process.exit(1);
	}

//
// TileMakerConfig
//
	try {
		tileMaker=require('./lib/tileMaker');	
	}
	catch(err) {
		console.error('1: Could not load Postgres database module.', err);				
	}
	
	try {
		TileMakerConfig = require('./lib/TileMakerConfig');
	}
	catch(err) {
		console.error('1: Could not load TileMakerConfig database module.', err);				
		process.exit(1);
	}
	var tileMakerConfig=new TileMakerConfig.TileMakerConfig(argv["xmlfile"]);	
	tileMakerConfig.parseConfig(function (err, data) {
		if (err) {
			console.error(err.message);			
			process.exit(1);		
		}
		tileMakerConfig.setXmlConfig(data);
			
		// Create SQL server client;
		mssql_db_connect(mssql, argv["hostname"] , argv["database"], argv["username"], argv["password"], argv["pngfile"], tileMakerConfig);
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
	else if (p_var == "VERBOSE") {	
		p_def=false; // Disable verbose log messages
	}
	
	if (process.env[p_var]) { 
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
 *				database host, name, username, password, generate PNG files, tileMakerConfig object
 * Returns:		Nothing
 * Description:	Connect to database, ...
 */
function mssql_db_connect(p_mssql, p_hostname, p_database, p_user, p_password, p_pngfile, tileMakerConfig) {

	var endCallBack = function endCallBack(err) {
		if (err) {
			console.error("SQL server error: " + err.message);
			process.exit(0);		
		}
		process.exit(1);	
	}

	var config = {
		driver: 'msnodesqlv8',
		server: p_hostname,
		database: p_database,
		options: {
			trustedConnection: false,
			useUTC: true,
			appName: 'mssqlTileMaker.js'
		}
	};
	if (p_user != "") {
		config.user=p_user;
		config.password=p_password;
	}
	else {
		config.options.trustedConnection=true;
	}
	

// Connect to SQL server database
	var client1=p_mssql.connect(config, function(err) {
		if (err) {
			console.error('Could not connect to SQL server client using: ' + JSON.stringify(config, null, 4) + "\nError: " + err);
			process.exit(1);	
		}
		else {				
			var DEBUG = (typeof v8debug === 'undefined' ? 'undefined' : _typeof(v8debug)) === 'object' || process.env.DEBUG === 'true' || process.env.VERBOSE === 'true';
			console.log('Connected to SQL server using: ' + JSON.stringify(config, null, 4) + "; debug: " + DEBUG);

// Call mssqlTileMaker()...
			tileMaker.dbTileMaker(p_mssql, client1, p_pngfile, tileMakerConfig, "MSSQLServer", endCallBack);
		} // End of else connected OK 
	}); // End of connect		

	// Notice message event processors
//	client1.on('notice', function(msg) {
//		  console.log('MSSQL: %s', msg);
//	});
}

var mssql = undefined;
var tileMaker=undefined;
var TileMakerConfig = undefined;
main();

//
// Eof