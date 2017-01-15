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
// Rapid Enquiry Facility (RIF) - Logger module
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

require('magic-globals'); // For file and line information. Does not work from Event queues
	
const Winston=require('winston'),
	  path=require('path');
	
/*
 * Function: 	Logger()
 * Parameters:	loggerParams object, e.g. see default below 
 * Returns:		Nothing
 * Description:	Setup winston logging. Memory and file logs are verbose by default, and write to progName.log
 *				Beware that winston is NOT inherited for performance reasons, i.e the internal winston object is prefixed by winston
 *				e.g. the memory messages array is:
 *
 *				var messages = winston.winston.transports.memory.writeOutput
 */		
function Logger(loggerParams) {
	if (loggerParams == undefined) {
		loggerParams = {
			progName:			'mssqlTileMaker',
			debugLevel: 		'info',
			memoryFileDebug:	'verbose'
		};
	}
	this.winstonParams = {
		level: (loggerParams.debugLevel || 'info'),
		transports: [
			new (Winston.transports.Console)({
				level: (loggerParams.debugLevel || 'info'),
				json: true,
				timestamp: function() {
					return Date.now();
				},
				stringify: function(options) {
					// Return string will be passed to logger.
					return /* options.timestamp() +' '+ options.level.toUpperCase() +' '+  */(options.message ? options.message : '') +
					  (options.meta && Object.keys(options.meta).length ? '\n\t'+ JSON.stringify(options.meta) : '' );
				}
			}),
			new (Winston.transports.Memory) ({ 
				level: (loggerParams.debugLevel || loggerParams.memoryFileDebug || 'verbose'),
				json: true
			}),
			new (Winston.transports.File) ({ 
				level: (loggerParams.debugLevel || loggerParams.memoryFileDebug || 'verbose'),
				filename: (loggerParams.progName || 'unknown') + '.log' 
			})
		]
	  };
			
	this.winston = new (Winston.Logger)(this.winstonParams);	
	
	// Add exception handler
	this.winston.handleExceptions=this.winston.transports,
	this.winston.humanReadableUnhandledException=true,
	this.winston.exitOnError=this.exceptionHandler,
	
	this.info("Created " + this.winston.level + " log file: " + this.winston.transports.file.filename);
} // End of Logger constructor
Logger.prototype = { // Add methods
		/*
		 * Function: 	exceptionHandler()
		 * Parameters:	Error object
		 * Returns:		Nothing
		 * Description:	Calls winston.log, exits
		 */
		exceptionHandler: function(err) {
			console.error("Exit due to unhandled exception: " + err.message + "\nStack> " + err.stack);
			process.exit(1);		
		},
		/*
		 * Function: 	log()
		 * Parameters:	Variable argument list
		 * Returns:		Nothing
		 * Description:	Calls winston.log
		 */	
		log: function() {
			var calling_function = arguments.callee.caller.name || '(anonymous)';
			// Get file information from magic-globals: __stack
			var file=path.basename(__stack[2].getFileName().split('/').slice(-1)[0].split('.').slice(0)[0]);
			var line=__stack[2].getLineNumber();
	
			if (arguments.length > 1) {
				var args = Array.prototype.slice.call(arguments);
				var logType=args.shift();
				var trace;
				if (logType == 'info' && this.winston.level == 'info') {
					trace="";
				}
				else if (logType == 'warn' ) {
					trace="WARNING: [" + file + ":" + line+ ":" + calling_function + "()] ";
				}
				else if (logType == 'verbose' || logType == 'info') {
					trace="[" + file + ":" + line+ ":" + calling_function + "()] ";
				}
				else {
					trace=logType + " [" + file + ":" + line+ ":" + calling_function + "()] ";
				}
				var format=args.shift();
				if (args.length == 0) {
					this.winston.log(logType, trace + format);
				}
				else {
					this.winston.log(logType, trace + format, ...args); // ES2015 way
				}
			}
			else {
				throw new Error("Logger.log(): insufficient arguments");
			}
		},
		/*
		 * Function: 	verbose, info, debug, error()
		 * Parameters:	Variable argument list
		 * Returns:		Nothing
		 * Description:	Calls log then winston.log
		 */	
		verbose: function() {
			var calling_function = arguments.callee.caller.name || '(anonymous)';
			// Get file information from magic-globals: __stack
			var file=path.basename(__stack[2].getFileName().split('/').slice(-1)[0].split('.').slice(0)[0]);
			var line=__stack[2].getLineNumber();
			var trace="[" + file + ":" + line+ ":" + calling_function + "()]";
	
			var args = Array.prototype.slice.call(arguments);
			var format=args.shift();
			this.log('verbose', format, args);
		},
		info: function() {
			var calling_function = arguments.callee.caller.name || '(anonymous)';
			// Get file information from magic-globals: __stack
			var file=path.basename(__stack[2].getFileName().split('/').slice(-1)[0].split('.').slice(0)[0]);
			var line=__stack[2].getLineNumber();
			var trace;
			if (this.winston.level == 'info') {
				trace=""
			}
			else {
				trace="[" + file + ":" + line+ ":" + calling_function + "()]";
			}	
			var args = Array.prototype.slice.call(arguments);
			var format=args.shift();
			this.log('info', trace + format, args);
		},
		error: function() {
			var calling_function = arguments.callee.caller.name || '(anonymous)';
			// Get file information from magic-globals: __stack
			var file=path.basename(__stack[2].getFileName().split('/').slice(-1)[0].split('.').slice(0)[0]);
			var line=__stack[2].getLineNumber();
			var trace="[" + file + ":" + line+ ":" + calling_function + "()]";
	
			var args = Array.prototype.slice.call(arguments);
			var format=args.shift();
			this.log('error', trace + format, args);
		},
		debug: function() {
			var trace="[" + file + ":" + line+ ":" + calling_function + "()]";
			var calling_function = arguments.callee.caller.name || '(anonymous)';
			// Get file information from magic-globals: __stack
			var file=path.basename(__stack[2].getFileName().split('/').slice(-1)[0].split('.').slice(0)[0]);
			var line=__stack[2].getLineNumber();
	
			var args = Array.prototype.slice.call(arguments);
			var format=args.shift();
			this.log('debug', trace + format, args);
		}
}
module.exports.Logger = Logger;

//
// Eof	