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
// Rapid Enquiry Facility (RIF) - scopeChecker() - global scope checking function. For 
// tracking module include/async issues with variable not being in scope when expected to be...
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

/*
 * Function: 	scopeChecker()
 * Parameters:	file, line called from, named array object to scope checked mandatory, 
 * 				optional array (used to check optional callbacks)
 * Description: Scope checker function. Throws error if not in scope
 *				Tests: serverError2(), serverError(), serverLog2(), serverLog() are functions; serverLog module is in scope
 *				Checks if callback is a function if in scope
 *				Raise a test exception if the calling function matches the exception field value
 * 				For this to work the function name must be defined, i.e.:
 *
 *					scopeChecker = function scopeChecker(fFile, sLine, array, optionalArray) { ... 
 *				Not:
 *					scopeChecker = function(fFile, sLine, array, optionalArray) { ... 
 *				Add the ofields (formdata fields) array must be included
 */
scopeChecker = function scopeChecker(fFile, sLine, array, optionalArray) {
	var errors=0;
	var undefinedKeys;
	var msg="";
	var calling_function = arguments.callee.caller.name || '(anonymous)';
	
	for (var key in array) {
		if (typeof array[key] == "undefined") {
			if (!undefinedKeys) {
				undefinedKeys=key;
			}
			else {
				undefinedKeys+=", " + key;
			}
			errors++;
		}
	}
	if (errors > 0) {
		msg+=errors + " variable(s) not in scope: " + undefinedKeys;
	}
	if (array["serverLog"] && typeof array["serverLog"] !== "undefined") { // Check error and logging in scope
		if (typeof array["serverLog"].serverError2 != "function") {
			msg+="\nserverLog.serverError2 is not a function: " + typeof array["serverLog"];
			errors++;
		}
		if (typeof array["serverLog"].serverLog2 != "function") {
			msg+="\nserverLog.serverLog2 is not a function: " + typeof array["serverLog"];
			errors++;
		}
		if (typeof array["serverLog"].serverError != "function") {
			msg+="\nserverLog.serverError is not a function: " + typeof array["serverLog"];
			errors++;
		}
		if (typeof array["serverLog"].serverLog != "function") {
			msg+="\nserverLog.serverLog is not a function: " + typeof array["serverLog"];
			errors++;
		}		
	}
	else if (array["serverLog"] && typeof array["serverLog"] == "undefined") {	
		msg+="\nserverLog module is not in scope: " + array["serverLog"];
		errors++;
	}
	if (array["httpErrorResponse"]) { // Check httpErrorResponse in scope
		if (typeof array["httpErrorResponse"].httpErrorResponse != "function") {
			msg+="\httpErrorResponse.httpErrorResponse is not a function: " + typeof array["httpErrorResponse"];
			errors++;
		}
	}	
	// Check callback
	if (array["callback"]) { // Check callback is a function if in scope
		if (typeof array["callback"] != "function") {
			msg+="\nMandatory callback (" + typeof(callback) + "): " + (callback.name || "anonymous") + " is in use but is not a function: " + 
				typeof callback;
			errors++;
		}
	}	
	// Check optional callback
	if (optionalArray && optionalArray["callback"]) { // Check callback is a function if in scope
		if (typeof optionalArray["callback"] != "function") {
			msg+="\noptional callback (" + typeof(callback) + "): " + (callback.name || "anonymous") + " is in use but is not a function: " + 
				typeof callback;
			errors++;
		}
	}

	// Raise a test exception if the calling function matches the exception field value 
	if (array["ofields"] && typeof array["ofields"] !== "undefined") {
		if (array["ofields"].exception == calling_function) { 
			msg+="\nRaise test exception in: " + array["ofields"].exception;
			errors++;
		}
//		else {
//			console.error("scopeChecker() ignore: " + array["ofields"].exception + "; calling function: " + calling_function);
//		}
	}
	
	// Raise exception if errors
	if (errors > 0) {
		// Prefereably by serverLog.serverError2()
		if (array["serverLog"] && array["req"] && typeof array["serverLog"].serverError2 == "function") {
			array["serverLog"].serverError2(fFile, sLine, "scopeChecker", 
				msg, array["req"], undefined);
		}
		else if (serverLog && serverLog.serverError2 == "function") {
			serverLog.serverError2(fFile, sLine, "scopeChecker", 
				msg, array["req"], undefined);
		}
		else {
			msg+="\nscopeChecker() Forced to RAISE exception; serverLog.serverError2() not in scope";
			throw new Error(msg);
		}
	}	
} // End of scopeChecker()
	
module.exports = scopeChecker;	