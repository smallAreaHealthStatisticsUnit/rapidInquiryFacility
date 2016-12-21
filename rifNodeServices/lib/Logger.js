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
	
const Winston=require('winston');
	
/*
 * Function: 	Logger()
 * Parameters:	loggerParams object, e.g. see default below 
 * Returns:		Nothing
 * Description:	Setup winston logging. Memory and file logs are verbose by default, and write to progName.log
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
				level: (loggerParams.memoryFileDebug || 'verbose'),
				json: true
			}),
			new (Winston.transports.File) ({ 
				level: (loggerParams.memoryFileDebug || 'verbose'),
				filename: (loggerParams.progName || 'unknown') + '.log' 
			})
		]
	  };
			
	this.winston = new (Winston.Logger)(this.winstonParams);	
	
	this.info("Created " + this.winston.level + " log file: " + this.winston.transports.file.filename);
} // End of Logger constructor
Logger.prototype = { // Add methods
		/*
		 * Function: 	log()
		 * Parameters:	Variable argument list
		 * Returns:		Nothing
		 * Description:	Calls winston.log
		 */	
		log: function() {
			if (arguments.length > 1) {
				var args = Array.prototype.slice.call(arguments);
				var logType=args.shift();
				var format=args.shift();
				if (args.length == 0) {
					this.winston.log(logType, format);
				}
				else {
					this.winston.log(logType, format, ...args); // ES2015 way
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
			var args = Array.prototype.slice.call(arguments);
			var format=args.shift();
			this.log('verbose', format, args);
		},
		info: function() {
			var args = Array.prototype.slice.call(arguments);
			var format=args.shift();
			this.log('info', format, args);
		},
		error: function() {
			var args = Array.prototype.slice.call(arguments);
			var format=args.shift();
			this.log('error', format, args);
		},
		debug: function() {
			var args = Array.prototype.slice.call(arguments);
			var format=args.shift();
			this.log('debug', format, args);
		}
}
module.exports.Logger = Logger;

//
// Eof	