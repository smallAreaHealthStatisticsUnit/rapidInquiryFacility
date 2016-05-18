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
// Rapid Enquiry Facility (RIF) - Node Geospatial webservices
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
 
//  Globals
var util = require('util'),
    os = require('os'),
    fs = require('fs'),
	zlib = require('zlib'),
	JSZip = require('JSZip'),
	path = require('path'),
    geo2TopoJSON = require('../lib/geo2TopoJSON'),
    shpConvert = require('../lib/shpConvert'),
	stderrHook = require('../lib/stderrHook'),
    httpErrorResponse = require('../lib/httpErrorResponse'),
    serverLog = require('../lib/serverLog'),
	
/*
 * Function: 	TempData() 
 * Parameters:  NONE
 * Description: Construction for TempData
 */
     TempData = function TempData() {
		
		this.file = '';
		this.file_list = [];
		this.no_files = 0;	
		this.myId = '';
		
        return this; 
     }; // End of globals

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
		var errors=0;optionalArray
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
			if (array["serverLog"] && array["req"] && typeof array["serverLog"].serverLog2 == "function") {
				array["serverLog"].serverError2(fFile, sLine, "scopeChecker", 
					msg, array["req"], undefined);
			}
			else {
				msg+="\nscopeChecker() Forced to RAISE exception; serverLog.serverError2() not in scope";
				throw new Error(msg);
			}
		}	
	} // End of scopeChecker()
	
	/*
	 * Function:	createTemporaryDirectory()
	 * Parameters:	Directory component array [$TEMP/shpConvert, <uuidV1>, <fileNoext>], internal response object, Express HTTP request object
	 * Returns:		Final directory (e.g. $TEMP/shpConvert/<uuidV1>/<fileNoext>)
	 * Description: Create temporary directory (for shapefiles)
	 */
	createTemporaryDirectory = function createTemporaryDirectory(dirArray, response, req) {
		
		scopeChecker(__file, __line, {
			serverLog: serverLog,
			dirArray: dirArray,
			req: req,
			response: response,
			fs: fs
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
	 */
	responseProcessing = function responseProcessing(req, res, response, serverLog, httpErrorResponse, ofields) {
		var msg="";
		
		scopeChecker(__file, __line, {
			serverLog: serverLog,
			httpErrorResponse: httpErrorResponse,
			req: req,
			res: res,
			response: response,
			ofields: ofields
		});
		
		if (response.diagnosticsTimer) { // Disable the diagnostic file write timer
			msg+="\nDisable the diagnostic file write timer";
			clearInterval(response.diagnosticsTimer);
			response.diagnosticsTimer=undefined;
		}		
		serverLog.serverLog2(__file, __line, "responseProcessing", 
			"Diagnostics >>>\n" +
			response.message + "\n<<< End of diagnostics");	
		if (response.fields && response.fields["diagnosticFileDir"] && response.fields["diagnosticFileName"]) {
			fs.writeFileSync(response.fields["diagnosticFileDir"] + "/" + response.fields["diagnosticFileName"], 
				response.message);
		}	
		ofields["diagnosticFileDir"]=undefined;	// Remove diagnosticFileDir as it reveals OS type
		if (!ofields["my_reference"]) { 
			ofields["my_reference"]=undefined;
		}		
		response.fields=ofields;				// Add return fields not already present	
		if (response.field_errors == 0 && response.file_errors == 0) { // OK
		
			addStatus(__file, __line, response, "END", 200 /* HTTP OK */, serverLog, req); // Add status
			
			serverLog.serverLog2(__file, __line, "responseProcessing", msg, req);	
			if (!req.finished) { // Reply with error if httpErrorResponse.httpErrorResponse() NOT already processed	
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
				res.write(output);                  // Write output  
				res.end();	
			}
			else {
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
	 * Parameters:	Express HTTP request object, ofields object, internal response object, serverLog, httpErrorResponse object
	 * Description: Send express HTTP response
	 */	
	var setupDiagnostics = function setupDiagnostics(req, ofields, response, serverLog, httpErrorResponse) {
		
		scopeChecker(__file, __line, {
			serverLog: serverLog,
			httpErrorResponse: httpErrorResponse,
			req: req,
			response: response,
			status: response.status,
			ofields: ofields,
			os: os,
			fs: fs
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
		ofields["diagnosticFileDir"]=createTemporaryDirectory(dirArray, response, req);
		
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
			serverLog.serverError2(__file, __line, "setupDiagnostics", 
				"ERROR: Cannot write diagnostics file, already exists: " + ofields["diagnosticFileDir"] + "/" + ofields["diagnosticFileName"], req);
		}
		else {
			response.message+="\nCreating diagnostics file: " + ofields["diagnosticFileDir"] + "/" + ofields["diagnosticFileName"];
			response.fields=ofields;
			fs.writeFileSync(ofields["diagnosticFileDir"] + "/" + ofields["diagnosticFileName"], 
				response.message);
		}
		
//
// Write status file
//
		if (fs.existsSync(ofields["diagnosticFileDir"] + "/" + ofields["statusFileName"])) { // Exists
			serverLog.serverError2(__file, __line, "setupDiagnostics", 
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
	
		scopeChecker(__file, __line, {
			serverLog: serverLog,
			httpErrorResponse: httpErrorResponse,
			response: response,
			fields: response.fields,
			message: response.message,
			dstart: dstart,
			fs: fs
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
		const path = require('path');
		
		scopeChecker(__file, __line, {
			serverLog: serverLog,
			response: response,
			sfile: sfile,
			sline: sline,
			calling_function: calling_function,
			status: response.status,
			message: response.message,
			httpStatus: httpStatus,
			req: req
		});	
		var msg;

		// Check status, httpStatus
		switch (httpStatus) {
			case 200: /* HTTP OK */
				break;
			case 405: /* HTTP service not suuport */
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
			response.message+="\n[" + response.fields["uuidV1"] + "+" + response.status[response.status.length-1].etime + " S] Re-creating status file: " + response.fields["statusFileName"];
			var statusText = JSON.stringify(response.status);// Convert response.status to JSON 
			fs.writeFileSync(response.fields["diagnosticFileDir"] + "/" + response.fields["statusFileName"], 
				statusText);	
		}		
		else {		
			serverLog.serverLog2(__file, __line, "addStatus", msg, req);
		}
		
	} // End of addStatus
							
/*
 * Function: 	exports.convert()
 * Parameters:	Express HTTP request object, response object
 * Description:	Express web server handler function for topoJSON conversion
 */
exports.convert = function exportsConvert(req, res) {

	try {
		
//  req.setEncoding('utf-8'); // This corrupts the data stream with binary data
//	req.setEncoding('binary'); // So does this! Leave it alone - it gets it right!

		res.setHeader("Content-Type", "text/plain");
		
// Set timeout for 10 minutes
		req.setTimeout(10*60*1000, function myTimeoutFunction() {
			serverLog.serverError2(__file, __line, "exports.convert", "Message timed out at: " + req.timeout, undefined, undefined);	
			req.abort();
		}); // 10 minutes
		
// Add stderr hook to capture debug output from topoJSON	
		var stderr = stderrHook.stderrHook(function myStderrHook(output, obj) { 
			output.str += obj.str;
		});
		
/*
 * Response object - no errors:
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
 */ 
		var response = {                 // Set output response    
			no_files: 0,    
			field_errors: 0, 
			file_errors: 0,
			file_list: [],
			message: '',               
			fields: [],
			status: []
		};
		var d_files = { 
			d_list: []
		}
		addStatus(__file, __line, response, "INIT", 200 /* HTTP OK */, serverLog, req);  // Add initial status
		
/*
 * Services supported:
 * 
 * shpConvert: Upload then convert shapefile to geoJSON;
 * simplifyGeoJSON: Load, validate, aggregate, clean and simplify converted shapefile data;
 * geo2TopoJSON: Convert geoJSON to TopoJSON;
 * geoJSONtoWKT: Convert geoJSON to Well Known Text (WKT);
 * createHierarchy: Create hierarchical geospatial intersection of all the shapefiles;
 * createCentroids: Create centroids for all shapefiles;
 * createMaptiles: Create topoJSON maptiles for all geolevels and zoomlevels; 
 * getGeospatialData: Fetches GeoSpatial Data;
 * getNumShapefilesInSet: Returns the number of shapefiles in the set. This is the same as the highest resolution geolevel id;
 * getMapTile: Get maptile for specified geolevel, zoomlevel, X and Y tile number.
 */		
		if (!((req.url == '/shpConvert') ||
			  (req.url == '/simplifyGeoJSON') ||
			  (req.url == '/geo2TopoJSON') ||
			  (req.url == '/geoJSONtoWKT') ||
			  (req.url == '/createHierarchy') ||
			  (req.url == '/createCentroids') ||
			  (req.url == '/createMaptiles') ||
			  (req.url == '/getGeospatialData') ||
			  (req.url == '/getNumShapefilesInSet') ||
			  (req.url == '/getMapTile'))) {
			var msg="ERROR! " + req.url + " service invalid; please see: " + 
				"https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifNodeServices/readme.md Node Web Services API for RIF 4.0 documentation for help";
			httpErrorResponse.httpErrorResponse(__file, __line, "exports.convert", 
				serverLog, 405, req, res, msg);		
			return;					
		}
	
	// Post method	
		if (req.method == 'POST') {
		
	// Default field value - for return	
			var ofields = {	
				my_reference: '', 
				verbose: false
			}
			if (req.url == '/geo2TopoJSON') {
				// Default geo2TopoJSON options (see topology Node.js module)
				var topojson_options = {
					verbose: false,
					quantization: 1e4		
				};	
				ofields.zoomLevel=0; 
				ofields.quantization=topojson_options.quantization;
				ofields.projection=topojson_options.projection;
			}
			else if (req.url == '/shpConvert') {
				// Default shpConvert options (see shapefile Node.js module)
				var shapefile_options = {
					verbose: false,
					encoding: 'ISO-8859-1',
					store: false
				};
				shapefile_options["ignore-properties"] = false;
			}
			
/*
 * Function: 	req.busboy.on('filesLimit') callback function
 * Parameters:	None
 * Description:	Processor if the files limit has been reached  
 */				  
			req.busboy.on('filesLimit', function filesLimit() {
				var msg="FAIL! Files limit reached: " + response.no_files;
				response.message=msg + "\n" + response.message;
				response.file_errors++;				// Increment file error count	
				serverLog.serverLog2(__file, __line, "req.busboy.on('filesLimit')", msg, req);								
			});
			
/*
 * Function: 	req.busboy.on('fieldsLimit') callback function
 * Parameters:	None
 * Description:	Processor if the fields limit has been reached  
 */				  
			req.busboy.on('fieldsLimit', function fieldsLimit() {	
				var msg="FAIL! fields limit reached: " + (response.fields.length+1);
				response.fields=ofields;				// Add return fields	
				response.message=msg + "\n" + response.message;
				response.field_errors++;				// Increment field error count			
				serverLog.serverLog2(__file, __line, "req.busboy.on('fieldsLimit')", msg, req);	
			});
			
/*
 * Function: 	req.busboy.on('partsLimit') callback function
 * Parameters:	None
 * Description:	Processor if the parts limit has been reached  
 */				  
			req.busboy.on('partsLimit', function partsLimit() {
				var msg="FAIL! Parts limit reached.";
				response.message=msg + "\n" + response.message;
				response.file_errors++;				// Increment file error count			
				serverLog.serverLog2(__file, __line, "req.busboy.on('partsLimit')", msg, req);	
			});
				
/*
 * Function: 	req.busboy.on('file') callback function
 * Parameters:	fieldname, stream, filename, encoding, mimetype
 * Description:	File attachment processing function  
 */				  
			req.busboy.on('file', function fileAttachmentProcessing(fieldname, stream, filename, encoding, mimetype) {

				/*
				 * Function: 	fileCompressionProcessing()
				 * Parameters:	d [data] object, internal response object, serverLog object, d_files flist list object, data buffer (received file),
				 *				HTTP request object
				 * Description:	Call file processing; handle zlib, gz and zip files
				 */	
				var fileCompressionProcessing = function fileCompressionProcessing(d, response, serverLog, d_files, buf, req) {
					var msg;
					
					scopeChecker(__file, __line, {
						d: d,
						response: response,
						serverLog: serverLog,
						d_files: d_files,
						buf: buf,
						req: req
					});

					addStatus(__file, __line, response, "Processing file: " + d.file.file_name, 
						200 /* HTTP OK */, serverLog, req);  // Add file compression processing status
					
					d.file.file_data="";
					var lstart = new Date().getTime();
					if (d.file.file_encoding === "gzip") {
						try {
							d.file.file_data=zlib.gunzipSync(buf);
						}
						catch (e) {
							msg="FAIL! File [" + d.no_files + "]: " + d.file.file_name + "; extension: " + 
								d.file.extension + "; file_encoding: " + d.file.file_encoding + " inflate exception";
							d.file.file_error=msg;	
							response.message=msg + "\n" + response.message;
							response.no_files=d.no_files;			// Add number of files process to response
							response.fields=ofields;				// Add return fields		
							response.file_errors++;					// Increment file error count	
							serverLog.serverLog2(__file, __line, "req.busboy.on('file').stream.on('error')", msg, req);						
							d_files.d_list[d.no_files-1] = d;							
							return;
						}	
						end = new Date().getTime();		
						d.file.uncompress_time=(end - lstart)/1000; // in S		
						d.file.uncompress_size=d.file.file_data.length;								
						response.message+="\nFile [" + d.no_files + "]: " + d.file.file_name + "; encoding: " +
							d.file.file_encoding + "; zlib.gunzip(): " + d.file.file_data.length + 
							"; from buf: " + buf.length, req; 
					}	
					else if (d.file.file_encoding === "zlib") {	
						try {
							d.file.file_data=zlib.inflateSync(buf);
						}
						catch (e) {
							msg="FAIL! File [" + d.no_files + "]: " + d.file.file_name + "; extension: " + 
								d.file.extension + "; file_encoding: " + d.file.file_encoding + " inflate exception";
							d.file.file_error=msg;	
							response.message=msg + "\n" + response.message;
							response.no_files=d.no_files;			// Add number of files process to response
							response.fields=ofields;				// Add return fields	
							response.file_errors++;					// Increment file error count	
							serverLog.serverLog2(__file, __line, "req.busboy.on('file').stream.on('error')", msg, req);						
							d_files.d_list[d.no_files-1] = d;				
							return;											
						}
						end = new Date().getTime();	
						d.file.uncompress_time=(end - lstart)/1000; // in S		
						d.file.uncompress_size=d.file.file_data.length;		
						response.message+="\nFile [" + d.no_files + "]: " + d.file.file_name + "; encoding: " +
							d.file.file_encoding + "; zlib.inflate(): " + d.file.file_data.length + 
							"; from buf: " + buf.length, req; 
					}
					else if (d.file.file_encoding === "zip") {
						msg="FAIL! File [" + d.no_files + "]: " + d.file.file_name + "; extension: " + 
							d.file.extension + "; file_encoding: " + d.file.file_encoding + " not supported";
						d.file.file_error=msg;			
						response.message=msg + "\n" + response.message;
						response.no_files=d.no_files;			// Add number of files process to response
						response.fields=ofields;				// Add return fields	
						response.file_errors++;					// Increment file error count	
						serverLog.serverLog2(__file, __line, "req.busboy.on('file').stream.on('error')", msg, req);						
						d_files.d_list[d.no_files-1] = d;				
						return;							
					}
					else {
						d.file.file_data=buf;
						if (d.file.file_encoding) {
							response.message+="\nFile received OK [" + d.no_files + "]: " + d.file.file_name + 
								"; encoding: " + d.file.file_encoding +
								"; uncompressed data: " + d.file.file_data.length, req;
						}
						else {
							response.message+="\nFile received OK [" + d.no_files + "]: " + d.file.file_name + 
								"; uncompressed data: " + d.file.file_data.length, req; 
						}
								
					}
										
					d_files.d_list[d.no_files-1] = d;
				} // End of fileCompressionProcessing()
			
				var d = new TempData(); // This is local to the post requests; the field processing cannot see it
			
				d.file = { // File return data type
					file_name: "",
					temp_file_name: "",
					file_encoding: "",	
					file_error: "",
					extension: "",
					jsonData: "",
					file_data: "",
					chunks: [],
					partial_chunk_size: 0,
					chunks_length: 0,
//					topojson: "",
//					topojson_stderr: "",
					file_size: 0,
					transfer_time: '',
					uncompress_time: undefined,
					uncompress_size: undefined,
					lstart: ''
				};

				// This will need a mutex if > 1 thread is being processed at the same time
				response.no_files++;	// Increment file counter
				d.no_files=response.no_files; // Local copy
				
				d.file.file_name = filename;
				d.file.temp_file_name = os.tmpdir()  + "/" + filename;
				d.file.file_encoding=req.get('Content-Encoding');
				d.file.extension = filename.split('.').pop();
				d.file.lstart=new Date().getTime();
				
				if (!d.file.file_encoding) {
					if (d.file.extension === "gz") {
							d.file.file_encoding="gzip";
					}
					else if (d.file.extension === "lz77") {
							d.file.file_encoding="zlib";
					}
				}
				
/*
 * Function: 	req.busboy.on('file').stream.on:('data') callback function
 * Parameters:	Data
 * Description: Data processor. Push data onto d.file.chunks[] array. Binary safe.
 *				Emit message every 10M
 */
				stream.on('data', function onStreamData(data) {
					var nbuf;
					if (d.file.extension == "json" || d.file.extension == "js") {
						nbuf=new Buffer(data.toString().replace(/\r?\n|\r/g, "")); // Remove any CRLF
						d.file.chunks.push(nbuf); 
						d.file.partial_chunk_size+=nbuf.length;
						d.file.chunks_length+=nbuf.length;	
					}
					else {
						d.file.chunks.push(data); 
						d.file.partial_chunk_size+=data.length;
						d.file.chunks_length+=data.length;						
					}

					if (d.file.partial_chunk_size > 10*1024*1024) { // 10 Mb
						
						if (d.file.extension == "json" || d.file.extension == "js") {
							response.message+="\nFile [" + d.no_files + "]: " + d.file.file_name + "; encoding: " +
								(d.file.file_encoding || "N/A") + 
								'; read [' + d.file.chunks.length + '] ' + d.file.partial_chunk_size + ', ' + d.file.chunks_length + ' total; ' +
								"crlf replaced: " + (data.length - nbuf.length);
						}
						else {
							response.message+="\nFile [" + d.no_files + "]: " + d.file.file_name + "; encoding: " +
								(d.file.file_encoding || "N/A") + 
								'; read [' + d.file.chunks.length + '] ' + d.file.partial_chunk_size + ', ' + d.file.chunks_length + ' total';
						}
						d.file.partial_chunk_size=0;
					}
				} // End of req.busboy.on('file').stream.on:('data') callback function
				);
				
/*
 * Function: 	req.busboy.on('file').stream.on:('error') callback function
 * Parameters:	Error
 * Description: EOF processor. Concatenate d.file.chunks[] array, uncompress if needed.
 */
				stream.on('error', function onStreamError(err) {
					var msg="FAIL! Strream error; file [" + d.no_files + "]: " + d.file.file_name + "; encoding: " +
							d.file.file_encoding + 
							'; read [' + d.file.chunks.length + '] ' + d.file.partial_chunk_size + ', ' + d.file.chunks_length + ' total';
					d.file.file_error=msg;	
					var buf=Buffer.concat(d.file.chunks); // Safe binary concat					
					d.file.file_size=buf.length;
					var end = new Date().getTime();
					d.file.transfer_time=(end - d.file.lstart)/1000; // in S						
					response.message=msg + "\n" + response.message;			
					response.no_files=d.no_files;			// Add number of files process to response				
					response.fields=ofields;				// Add return fields	
					response.file_errors++;					// Increment file error count	
					serverLog.serverLog2(__file, __line, "req.busboy.on('file').stream.on('error')", msg, req);							
					d_files.d_list[d.no_files-1] = d;		
				} // End of req.busboy.on('file').stream.on:('error') callback function
				);				

/*
 * Function: 	req.busboy.on('file').stream.on:('end') callback function
 * Parameters:	None
 * Description: EOF processor. Concatenate d.file.chunks[] array, uncompress if needed.
 */
				stream.on('end', function onStreamEnd() {
					
					var msg;
					var buf=Buffer.concat(d.file.chunks); 	// Safe binary concat
					d.file.chunks=undefined;				// Release memory
					d.file.file_size=buf.length;
					var end = new Date().getTime();
					d.file.transfer_time=(end - d.file.lstart)/1000; // in S	
					
					if (stream.truncated) { // Test for truncation
						msg="FAIL! File [" + d.no_files + "]: " + d.file.file_name + "; extension: " + 
							d.file.extension + "; file_encoding: " + d.file.file_encoding + 
							"; file is truncated at " + d.file.file_size + " bytes"; 
						d.file.file_error=msg;		
						response.message=msg + "\n" + response.message;
						response.no_files=d.no_files;			// Add number of files process to response
						response.fields=ofields;				// Add return fields		
						response.file_errors++;					// Increment file error countv					
						d_files.d_list[d.no_files-1] = d;		
//						httpErrorResponse.httpErrorResponse(__file, __line, "req.busboy.on('file').stream.on('end')", 
//							serverLog, 500, req, res, msg, undefined, response);						
						return;
					}
					
					fileCompressionProcessing(d, response, serverLog, d_files, buf, req); // Call file processing; handle zlib, gz and zip files	
					buf=undefined;	// Release memory
				}); // End of EOF processor
					
			} // End of req.busboy.on('file').stream.on:('end') callback function
			); // End of file attachment processing function: req.busboy.on('file')
			  
/*
 * Function: 	req.busboy.on('field') callback function
 * Parameters:	fieldname, value, fieldnameTruncated, valTruncated
 * Description:	Field processing function; fields supported  
 *
 *			 	verbose: 	Produces debug returned as part of reponse.message
 */ 
			req.busboy.on('field', function fieldProcessing(fieldname, val, fieldnameTruncated, valTruncated) {
				var text="\nField: " + fieldname + "[" + val + "]; ";
				
				// Handle truncation
				if (fieldnameTruncated) {
					text+="\FIELD PROCESSING ERROR! field truncated";
					response.field_errors++;
				}
				if (valTruncated) {
					text+="\FIELD PROCESSING ERROR! value truncated";
					response.field_errors++;
				}
				
				// Process fields
				if ((fieldname == 'verbose')&&(val == 'true')) {
					text+="verbose mode enabled";
					ofields[fieldname]="true";
				}
				if (req.url == '/geo2TopoJSON') {
					text+=geo2TopoJSON.geo2TopoJSONFieldProcessor(fieldname, val, topojson_options, ofields, response, req, serverLog);
				}
				else if (req.url == '/shpConvert') {
					text+=shpConvert.shpConvertFieldProcessor(fieldname, val, shapefile_options, ofields, response, req, serverLog);
				}					
				response.message += text;
			 }); // End of field processing function

/*
 * Function: 	req.busboy.on('finish') callback function
 * Parameters:	None
 * Description:	End of request - complete response		  
 */ 
			req.busboy.on('finish', function onBusboyFinish() {
				try {
					var msg="";

					setupDiagnostics(req, ofields, response, serverLog, httpErrorResponse);
					addStatus(__file, __line, response, "Busboy Finish", 200 /* HTTP OK */, serverLog, req);  // Add onBusboyFinish status
	
					if (req.url == '/geo2TopoJSON' || req.url == '/shpConvert') {

						for (var i = 0; i < response.no_files; i++) {	
							d=d_files.d_list[i];
							if (!d) { // File could not be processed, httpErrorResponse.httpErrorResponse() already processed
								msg="FAIL! File [" + (i+1) + "/?]: entry not found, no file list" + 
									"; httpErrorResponse.httpErrorResponse() NOT already processed";						
								if (!req.finished) { // Reply with error if httpErrorResponse.httpErrorResponse() NOT already processed
									response.message = msg + "\n" + response.message;
									response.no_files=0;					// Add number of files process to response
									response.fields=ofields;				// Add return fields
									response.file_errors++;					// Increment file error count
									httpErrorResponse.httpErrorResponse(__file, __line, "req.busboy.on('finish')", 
										serverLog, 500, req, res, msg, undefined, response);				
								}
								else {
									serverLog.serverLog2(__file, __line, "req.busboy.on('finish')", req, msg, undefined);
								}
								return;							
							}
							else if (!d.file) {
								msg="FAIL! File [" + (i+1) + "/" + d.no_files + "]: object not found in list" + 
									"\n";
								response.message = msg + "\n" + response.message;
								response.no_files=d.no_files;			// Add number of files process to response
								response.fields=ofields;				// Add return fields	
								response.file_errors++;					// Increment file error count	
								httpErrorResponse.httpErrorResponse(__file, __line, "req.busboy.on('finish')", 
									serverLog, 500, req, res, msg, undefined, response);							
								return;			
							}
							else if (d.file.file_error) {
								msg=d.file.file_error;
								response.message = msg + "\n" + response.message;
								response.no_files=d.no_files;			// Add number of files process to response
								response.fields=ofields;				// Add return fields
								response.file_errors++;					// Increment file error count	
								httpErrorResponse.httpErrorResponse(__file, __line, "req.busboy.on('finish')", 
									serverLog, 500, req, res, msg, undefined, response);							
								return;							
							}
							else if (d.file.file_data.length == 0) {
								msg="FAIL! File [" + (i+1) + "/" + d.no_files + "]: " + d.file.file_name + "; extension: " + 
									d.file.extension + "; file size is zero" + 
									"\n";
								response.no_files=d.no_files;			// Add number of files process to response
								response.fields=ofields;				// Add return fields
								response.file_errors++;					// Increment file error count	
								httpErrorResponse.httpErrorResponse(__file, __line, "req.busboy.on('finish')", 
									serverLog, 500, req, res, msg, undefined, response);							
								return;
							}	
						} // End of for loop	
								
						if (response.no_files == 0) { 
							msg="FAIL! No files attached\n";						
							response.message = msg + "\n" + response.message;
							response.fields=ofields;				// Add return fields
							response.file_errors++;					// Increment file error count	
							httpErrorResponse.httpErrorResponse(__file, __line, "req.busboy.on('finish')", 
								serverLog, 500, req, res, msg, undefined, response);							
							return;						
						}
						else if (!ofields["my_reference"]) {
							msg+="[No my_reference] Processed: " + response.no_files + " files";
						}
						else {
							msg+="[my_reference: " + ofields["my_reference"] + "] Processed: " + response.no_files + " files";
						}
							
						// url specific code	
						if (req.url == '/geo2TopoJSON') {			
							for (var i = 0; i < response.no_files; i++) {
								d=d_files.d_list[i];
								// Call GeoJSON to TopoJSON converter
								d=geo2TopoJSON.geo2TopoJSONFile(d, ofields, topojson_options, stderr, response);	
								if (!d) {
									httpErrorResponse.httpErrorResponse(__file, __line, "geo2TopoJSON.geo2TopoJSONFile", serverLog, 
										500, req, res, msg, response.error, response);							
									return; 
								}								
							} // End of for loop
						}			
						else if (req.url == '/shpConvert') { // Note which files and extensions are present, 
																							// generate serial if required, save 
							if (!shpConvert.shpConvert(ofields, d_files, response, req, res, shapefile_options)) {
								return;
							}
						}	
					
		/*
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
		 */ 				
						// Final processing
						if (req.url == '/shpConvert') { // Processed by shpConvertCheckFiles() - uses async
						}
						else if (req.url == '/geo2TopoJSON') {	
							responseProcessing(req, res, response, serverLog, httpErrorResponse, ofields);
						}
					}
					else {
						var msg="ERROR! " + req.url + " service not not yet supported";
						if (d && d.no_files) {
							response.no_files=d.no_files;			// Add number of files process to response
						}
						if (ofields) {
							response.fields=ofields;				// Add return fields
						}
						response.file_errors++;					// Increment file error count	
						httpErrorResponse.httpErrorResponse(__file, __line, "req.busboy.on('finish')", 
							serverLog, 405, req, res, msg, undefined, response);		
						return;		
					}
	//				console.error("req.busboy.on('finish') " + msg);					
				} catch(e) {
					httpErrorResponse.httpErrorResponse(__file, __line, "req.busboy.on('finish')", 
						serverLog, 500, req, res, 'Caught unexpected error (possibly async)', e, undefined /* My response */);
					return;
				}
			} // End of req.busboy.on('finish')
			);

			req.pipe(req.busboy); // Pipe request stream to busboy form data handler
			  
		} // End of post method
		else {								// All other methods are errors
			var msg="ERROR! "+ req.method + " Requests not allowed; please see: " + 
				"https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifNodeServices/readme.md Node Web Services API for RIF 4.0 documentation for help";
			httpErrorResponse.httpErrorResponse(__file, __line, "exports.convert", 
				serverLog, 405, req, res, msg);		
			return;		  
		}
		
	} catch (e) {                            // Catch syntax errors
		var msg="General processing ERROR!";				  
		httpErrorResponse.httpErrorResponse(__file, __line, "exports.convert catch()", serverLog, 500, req, res, msg, e);		
		return;
	}
	  
};