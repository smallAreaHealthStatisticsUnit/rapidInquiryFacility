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
// Rapid Enquiry Facility (RIF) - Node Geospatial webservices: common support functions
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
// Usage: tests/requests.js
//
// Uses:
//
// CONVERTS GEOJSON(MAX 100MB) TO TOPOJSON
// Only POST requests are processed
// Expects a vaild geojson as input
// Topojson have quantization on
// The level of quantization is based on map tile zoom level
// More info on quantization here: https://github.com/mbostock/topojson/wiki/Command-Line-Reference
//
// Prototype author: Federico Fabbri
// Imperial College London
//
 
/*
 * Function:	createTemporaryDirectory()
 * Parameters:	Directory component array [$TEMP/shpConvert, <uuidV1>, <fileNoext>], internal response object, Express HTTP request object, serverLog object
 * Returns:		Final directory (e.g. $TEMP/shpConvert/<uuidV1>/<fileNoext>)
 * Description: Create temporary directory (for shapefiles)
 */
createTemporaryDirectory = function createTemporaryDirectory(dirArray, response, req, serverLog) {
	
	const fs = require('fs');
		  
	scopeChecker(__file, __line, {
		serverLog: serverLog,
		dirArray: dirArray,
		req: req,
		response: response,
		serverLog: serverLog
	});

	var tdir;
	for (var i = 0; i < dirArray.length; i++) {  
		if (!tdir) {
			tdir=dirArray[i];
		}
		else {
			tdir+="/" + dirArray[i];
		}	
		try {
			var stats=fs.statSync(tdir);
		} catch (e) { 
			if (e.code == 'ENOENT') {
				try {
					fs.mkdirSync(tdir);
					response.message += "\nmkdir: " + tdir;
				} catch (e) { 
					serverLog.serverError2(__file, __line, "createTemporaryDirectory", 
						"ERROR: Cannot create directory: " + tdir + "; error: " + e.message, req);
//							shapeFileComponentQueueCallback();		// Not needed - serverError2() raises exception 
				}			
			}
			else {
				serverLog.serverError2(__file, __line, "createTemporaryDirectory", 
					"ERROR: Cannot access directory: " + tdir + "; error: " + e.message, req);
//						 shapeFileComponentQueueCallback();		// Not needed - serverError2() raises exception 					
			}
		}
	}
	return tdir;
} /* End of createTemporaryDirectory() */

/*
 * Function: 	responseProcessing()
 * Parameters:	Express HTTP request object, HTTP response object, internal response object, serverLog, httpErrorResponse object, ofields object
 * Description: Send express HTTP response
 *
 * geo2TopoJSON response object - no errors:
 *                    
 * no_files: 		Numeric, number of files    
 * field_errors: 	Number of errors in processing fields
 * file_list: 		Array file objects:
 *						file_name: File name
 *						topojson: TopoJSON created from file geoJSON,
 *						topojson_stderr: Debug from TopoJSON module,
 *						topojson_runtime: Time to convert geoJSON to topoJSON (S),
 *						file_size: Transferred file size in bytes,
 *						transfer_time: Time to transfer file (S),
 *						uncompress_time: Time to uncompress file (S)/undefined if file not compressed,
 *						uncompress_size: Size of uncompressed file in bytes
 * message: 		Processing messages, including debug from topoJSON               
 * fields: 			Array of fields; includes all from request plus any additional fields set as a result of processing 
 *
 * shpConvert response object - no errors, store=false
 *                    
 * no_files: 		Numeric, number of files    
 * field_errors: 	Number of errors in processing fields
 * file_list: 		Array file objects:
 *						file_name: File name
 *						file_size: Transferred file size in bytes,
 *						transfer_time: Time to transfer file (S),
 *						uncompress_time: Time to uncompress file (S)/undefined if file not compressed,
 *						uncompress_size: Size of uncompressed file in bytes
 * message: 		Processing messages, including debug from topoJSON               
 * fields: 			Array of fields; includes all from request plus any additional fields set as a result of processing 
 *  
 * shpConvert response object - no errors, store=true [Processed by shpConvertCheckFiles()]
 *  	 
 * no_files: 		Numeric, number of files    
 * field_errors: 	Number of errors in processing fields
 * file_list: 		Array file objects:
 *						file_name: File name
 *						geojson: GeoJSON created from shapefile,
 *						file_size: Transferred file size in bytes,
 *						transfer_time: Time to transfer files (S),
 *						geojson_time: Time to convert to geojson (S),
 *						uncompress_time: Time to uncompress file (S)/undefined if file not compressed,
 *						uncompress_size: Size of uncompressed file in bytes
 * message: 		Processing messages              
 * fields: 			Array of fields; includes all from request plus any additional fields set as a result of processing 	 
 *
 */
responseProcessing = function responseProcessing(req, res, response, serverLog, httpErrorResponse, ofields) {
	var msg="";
	const fs = require('fs');
		
	scopeChecker(__file, __line, {
		serverLog: serverLog,
		httpErrorResponse: httpErrorResponse,
		req: req,
		res: res,
		response: response,
		ofields: ofields
	});
	
	if (response.diagnosticsTimer) { // Disable the diagnostic file write timer
		response.message+="\nDisable the diagnostic file write timer";
		clearInterval(response.diagnosticsTimer);
		response.diagnosticsTimer=undefined;
	}			
	if (response.fields && response.fields["diagnosticFileDir"] && response.fields["diagnosticFileName"]) {
		fs.writeFileSync(response.fields["diagnosticFileDir"] + "/" + response.fields["diagnosticFileName"], 
			response.message);
	}	
	if (!ofields["my_reference"]) { 
		ofields["my_reference"]=undefined;
	}	
	if (ofields["topojson_options"]) { // Remove topojson_options
		ofields["topojson_options"]=undefined;
	}	
	response.fields=ofields;				// Add return fields not already present	

	if (response.field_errors == 0 && response.file_errors == 0) { // OK
	
		addStatus(__file, __line, response, "END", 200 /* HTTP OK */, serverLog, req); // Add status
		
		response.fields["diagnosticFileDir"]=undefined;	// Remove diagnosticFileDir as it reveals OS type
	
//			serverLog.serverLog2(__file, __line, "responseProcessing", msg, req);	
		if (!res.finished) { // Reply with error if httpErrorResponse.httpErrorResponse() NOT already processed	
			serverLog.serverLog2(__file, __line, "responseProcessing", 
				"Diagnostics >>>\n" +
				response.message + "\n<<< End of diagnostics", req);
			if (!response.fields.verbose) {
				response.message="";	
			}	

			var output = JSON.stringify(response);// Convert output response to JSON 

			if (response.fields["diagnosticFileDir"] && response.fields["responseFileName"]) { // Save to response file
				fs.writeFileSync(response.fields["diagnosticFileDir"] + "/" + response.fields["responseFileName"], 
					output);	
			}
			else if (!response.fields["responseFileName"]) {	
				serverLog.serverError(__file, __line, "responseProcessing", "Unable to save response file; no responseFileName", req);
			}
	
// Need to test res was not finished by an expection to avoid "write after end" errors			
			try {
				res.write(output);                  // Write output  
				res.end();	

				if (response.fields.verbose) {
					serverLog.serverLog2(__file, __line, "responseProcessing", 
						"Response sent; size: " + output.length + " bytes", req);					
				}
								
			}
			catch(e) {
				serverLog.serverError(__file, __line, "responseProcessing", "Error in sending response to client", req, e);
			}
		}
		else {
			serverLog.serverLog2(__file, __line, "responseProcessing", 
				"Diagnostics >>>\n" +
				response.message + "\n<<< End of diagnostics", req);
			serverLog.serverError(__file, __line, "responseProcessing", "Unable to return OK reponse to user - httpErrorResponse() already processed", req);
		}	
//					console.error(util.inspect(req));
//					console.error(JSON.stringify(req.headers, null, 4));
	}
	else if (response.field_errors > 0 && response.file_errors > 0) {
		msg+="\nFAIL! Field processing ERRORS! " + response.field_errors + 
			" and file processing ERRORS! " + response.file_errors + "\n" + msg;
		response.message = msg + "\n" + response.message;						
		httpErrorResponse.httpErrorResponse(__file, __line, "rresponseProcessing", 
			serverLog, 500, req, res, msg, undefined, response);				  
	}				
	else if (response.field_errors > 0) {
		msg+="\nFAIL! Field processing ERRORS! " + response.field_errors + "\n" + msg;
		response.message = msg + "\n" + response.message;
		httpErrorResponse.httpErrorResponse(__file, __line, "responseProcessing", 
			serverLog, 500, req, res, msg, undefined, response);				  
	}	
	else if (response.file_errors > 0) {
		msg+="\nFAIL! File processing ERRORS! " + response.file_errors + "\n" + msg;
		response.message = msg + "\n" + response.message;					
		httpErrorResponse.httpErrorResponse(__file, __line, "responseProcessing", 
			serverLog, 500, req, res, msg, undefined, response);				  
	}	
	else {
		msg+="\nUNCERTAIN! Field processing ERRORS! " + response.field_errors + 
			" and file processing ERRORS! " + response.file_errors + "\n" + msg;
		response.message = msg + "\n" + response.message;						
		httpErrorResponse.httpErrorResponse(__file, __line, "responseProcessing", 
			serverLog, 500, req, res, msg, undefined, response);
	}
} // End of responseProcessing

/*
 * Function: 	setupDiagnostics()
 * Parameters:	File, line called from, Express HTTP request object, ofields object, internal response object, serverLog, httpErrorResponse object
 * Description: Send express HTTP response
 */	
var setupDiagnostics = function setupDiagnostics(lfile, lline, req, ofields, response, serverLog, httpErrorResponse) {

	var calling_function = arguments.callee.caller.name || '(anonymous)';
    const os = require('os'),
          fs = require('fs');
	
	scopeChecker(__file, __line, {
		lfile: lfile,
		lline: lline,
		serverLog: serverLog,
		httpErrorResponse: httpErrorResponse,
		req: req,
		response: response,
		status: response.status,
		ofields: ofields
	});
	
	if (!ofields["uuidV1"]) { // Generate UUID
		ofields["uuidV1"]=serverLog.generateUUID();
	}		
//		if (!ofields["my_reference"]) { // Use UUID
//			ofields["my_reference"]=ofields["uuidV1"];
//		}
	
//	
// Create directory: $TEMP/shpConvert/<uuidV1> as required
//
	var dirArray=[os.tmpdir() + req.url, ofields["uuidV1"]];
	ofields["diagnosticFileDir"]=createTemporaryDirectory(dirArray, response, req, serverLog);
	
//	
// Setup file names
//	
	ofields["diagnosticFileName"]="diagnostics.log";
	ofields["statusFileName"]="status.json";
	ofields["responseFileName"]="response.json";
	
//	
// Write diagnostics file
//			
	if (fs.existsSync(ofields["diagnosticFileDir"] + "/" + ofields["diagnosticFileName"])) { // Exists
		serverLog.serverError2(lfile, lline, calling_function, 
			"ERROR: Cannot write diagnostics file, already exists: " + ofields["diagnosticFileDir"] + "/" + ofields["diagnosticFileName"], req);
	}
	else {
		response.message+="\n[" + lfile + ":" + lline + "; function: " + calling_function + "()] Creating diagnostics file: " + 
			ofields["diagnosticFileDir"] + "/" + ofields["diagnosticFileName"];
		response.fields=ofields;
		fs.writeFileSync(ofields["diagnosticFileDir"] + "/" + ofields["diagnosticFileName"], 
			response.message);
	}
	
//
// Write status file
//
	if (fs.existsSync(ofields["diagnosticFileDir"] + "/" + ofields["statusFileName"])) { // Exists
		serverLog.serverError2(lfile, lline, calling_function, 
			"ERROR: Cannot write status file, already exists: " + ofields["diagnosticFileDir"] + "/" + ofields["statusFileName"], req);
	}
	else {
		response.message+="\n[" + response.fields["uuidV1"] + "] Creating status file: " + response.fields["statusFileName"];
		var statusText = JSON.stringify(response.status);// Convert response.status to JSON 
		fs.writeFileSync(response.fields["diagnosticFileDir"] + "/" + response.fields["statusFileName"], 
			statusText);	
	}

	var dstart = new Date().getTime();
	// Re-create every second
	response.diagnosticsTimer=setInterval(recreateDiagnosticsLog /* Callback */, 1000 /* delay mS */, response, serverLog, httpErrorResponse, dstart);
} // End of setupDiagnostics

/*
 * Function:	recreateDiagnosticsLog()
 * Parameters:	response, serverLog, httpErrorResponse objects
 * Returns:		Nothing
 * Description: Re-create diagnostics file
 */
recreateDiagnosticsLog = function recreateDiagnosticsLog(response, serverLog, httpErrorResponse, dstart) {		
    const fs = require('fs');
		  
	scopeChecker(__file, __line, {
		serverLog: serverLog,
		httpErrorResponse: httpErrorResponse,
		response: response,
		fields: response.fields,
		message: response.message,
		dstart: dstart
	});
	
	var dend = new Date().getTime();
	var elapsedTime=(dend - dstart)/1000; // in S
	if (response.fields["diagnosticFileDir"] && response.fields["diagnosticFileName"]) {
		response.message+="\n[" + response.fields["uuidV1"] + "+" + elapsedTime + " S] Re-creating diagnostics file: " + response.fields["diagnosticFileName"];
		fs.writeFileSync(response.fields["diagnosticFileDir"] + "/" + response.fields["diagnosticFileName"], 
			response.message);	
	}
} // End of recreateDiagnosticsLog	

/*
 * Function:	addStatus()
 * Parameters:	file, line called from, response object, textual status, http status code, serverLog object, Express HTTP request object
 * Returns:		Nothing
 * Description: Add status to response status array
 */	
addStatus = function addStatus(sfile, sline, response, status, httpStatus, serverLog, req) {
	var calling_function = arguments.callee.caller.name || '(anonymous)';
	const path = require('path'),
		  fs = require('fs');
		
	scopeChecker(__file, __line, {
		serverLog: serverLog,
		response: response,
		sfile: sfile,
		sline: sline,
		calling_function: calling_function,
		status: response.status,
		message: response.message,
		httpStatus: httpStatus
	});	
	var msg;

	// Check status, httpStatus
	switch (httpStatus) {
		case 200: /* HTTP OK */
			break;
		case 405: /* HTTP service not supported */
			break;				
		case 500: /* HTTP error */
			break;
		case 501: /* HTTP general exception trap */
			break;				
		default:
			msg="addStatus() invalid httpStatus: " + httpStatus;
			response.message+="\n" + msg;
			throw new Error(msg);
			break;
	}
	
	response.status[response.status.length]= {
			statusText: status,
			httpStatus: httpStatus,
			sfile: path.basename(sfile),
			sline: sline,
			calling_function: calling_function,
			stime: new Date().getTime(),
			etime: 0
		}
		
	if (response.status.length == 1) {	
		msg="[" + sfile + ":" + sline + "] Initial state: " + status + "; code: " + httpStatus;
	}
	else {				
		response.status[response.status.length-1].etime=(response.status[response.status.length-1].stime - response.status[0].stime)/1000; // in S
		msg="[" + sfile + ":" + sline + ":" + calling_function + "()] +" + response.status[response.status.length-1].etime + "S new state: " + 
		response.status[response.status.length-1].statusText + "; code: " + response.status[response.status.length-1].httpStatus;
	}
	
	if (response.fields["uuidV1"] && response.fields["diagnosticFileDir"] && response.fields["statusFileName"]) { // Can save state
		response.message+="\n[" + response.fields["uuidV1"] + "+" + response.status[response.status.length-1].etime + 
			" S] Re-creating status file: " + response.fields["statusFileName"];
		var statusText = JSON.stringify(response.status);// Convert response.status to JSON 
		fs.writeFileSync(response.fields["diagnosticFileDir"] + "/" + response.fields["statusFileName"], 
			statusText);	
	}
// Do not log it - it will be - trust me
//		else { 		
//			serverLog.serverLog2(__file, __line, "addStatus", msg, req);
//		}
	
} // End of addStatus

module.exports.setupDiagnostics = setupDiagnostics;
module.exports.createTemporaryDirectory = createTemporaryDirectory;
module.exports.recreateDiagnosticsLog = recreateDiagnosticsLog;
module.exports.responseProcessing = responseProcessing;
module.exports.addStatus = addStatus;

// Eof