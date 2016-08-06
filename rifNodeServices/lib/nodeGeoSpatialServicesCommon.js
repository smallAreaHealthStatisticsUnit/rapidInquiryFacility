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
 
const nodeGeoSpatialServicesCommon = require('../lib/nodeGeoSpatialServicesCommon'),
       os = require('os');
 
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
						"ERROR: Cannot create directory: " + tdir + "; error: " + e.message, req, undefined /* err */, response);
//							shapeFileComponentQueueCallback();		// Not needed - serverError2() raises exception 
				}			
			}
			else {
				serverLog.serverError2(__file, __line, "createTemporaryDirectory", 
					"ERROR: Cannot access directory: " + tdir + "; error: " + e.message, req, undefined /* err */, response);
//						 shapeFileComponentQueueCallback();		// Not needed - serverError2() raises exception 					
			}
		}
	}
	return tdir;
} /* End of createTemporaryDirectory() */

/*
 * Function: 	responseProcessing()
 * Parameters:	Express HTTP request object, HTTP response object, internal response object, serverLog, httpErrorResponse object, 
 *				ofields object, optional callback
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
responseProcessing = function responseProcessing(req, res, response, serverLog, httpErrorResponse, ofields, callback) {
	var msg="";
	const fs = require('fs');
		
	scopeChecker(__file, __line, {
		serverLog: serverLog,
		httpErrorResponse: httpErrorResponse,
		req: req,
		res: res,
		response: response,
		ofields: ofields
	},
	{
		callback: callback
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
	var diagnosticFileDir=response.fields["diagnosticFileDir"]; // Save for later
	
	/*
	 * Function:	responseProcessingAddStatusCallback()
	 * Parameters:	Error object
	 * Returns:		Nothing
	 * Description: AddStatus callback
	 */	
	function responseProcessingAddStatusCallback(err) {
		if (err) {						
			serverLog.serverLog2(__file, __line, "responseProcessingAddStatusCallback", "ERROR! in add status", req, err);
		}
		else {			
			response.fields["diagnosticFileDir"]=undefined;	// Remove diagnosticFileDir as it reveals OS type
		
	//			serverLog.serverLog2(__file, __line, "responseProcessing", msg, req);	
			if (!res.finished) { // Response NOT already processed
				var msg=response.message; // Save
				if (!response.fields.verbose) { // Only send diagnostics if requested
					response.message="";	
				}	
				var output = JSON.stringify(response);// Convert output response to JSON 
				response.fields["diagnosticFileDir"]=diagnosticFileDir // Restore
				msg+="\nCreated reponse, size: " + output.length + "; saving to file: " + response.fields["responseFileName"] + ".1"; 
				response.message=msg; // Restore
				serverLog.serverLog2(__file, __line, "responseProcessing", 
					"Diagnostics >>>\n" +
					msg + "\n<<< End of diagnostics", req);
				
				writeResponseFile(serverLog, response, req, output,  ".3", // Final END version
					/*
					 * Function:	writeResponseFileCallback()
					 * Parameters:	Error object
					 * Returns:		Nothing
					 * Description: writeResponseFile callback
					 */
					function writeResponseFileCallback(err) {	
						if (err) {						
							serverLog.serverLog2(__file, __line, "writeResponseFileCallback", "ERROR! in writing response file", req, err);
						}
						else {	
							try { // Have tested res was not finished by an expection to avoid "write after end" errors
								res.write(output);                  // Write output  
								res.end();	

								if (response.fields.verbose || output.length > (10*1024*1024) /* 10MB */) {
									serverLog.serverLog2(__file, __line, "writeResponseFileCallback", 
										"Response sent; size: " + output.length + " bytes", req);					
								}
								if (callback) {
									callback();
								}				
							}
							catch(e) {
								serverLog.serverLog2(__file, __line, "writeResponseFileCallback", "ERROR! in sending response to client", req, e);
							}
						}
					}
				); // End of writeResponseFileCallback()

			}
			else if (ofields["batchMode"] == "true") { // Batch mode
				var msg=response.message; // Save
				if (!response.fields.verbose) { // Only send diagnostics if requested
					response.message="";	
				}	
				var output = JSON.stringify(response);// Convert output response to JSON 
				response.fields["diagnosticFileDir"]=diagnosticFileDir // Restore
				msg+="\nCreated response, size: " + output.length; 
				response.message=msg; // Restore
				serverLog.serverLog2(__file, __line, "responseProcessing", 
					"Batch end diagnostics >>>\n" +
					msg + "\n<<< End of diagnostics", req);

				writeResponseFile(serverLog, response, req, output,  ".1", undefined /* no callback */); // BEGIN BATCH version		
	//
			}
			else { // Error if response has already been processed
				serverLog.serverLog2(__file, __line, "responseProcessing", 
					"Diagnostics >>>\n" +
					response.message + "\n<<< End of diagnostics", req);
				serverLog.serverError(__file, __line, "responseProcessing", 
					"Unable to return OK reponse to user - httpErrorResponse() already processed", req, undefined /* err */, response);
			}	
//					console.error(util.inspect(req));
//					console.error(JSON.stringify(req.headers, null, 4));	
		}	
	} // End of responseProcessingAddStatusCallback()
	
	if (response.field_errors == 0 && response.file_errors == 0) { // OK
		if (ofields["batchMode"] == "true") { // Batch mode
			if (res.finished) { // Response has been processed
				addStatus(__file, __line, response, "BATCH_END", 200 /* HTTP OK */, serverLog, req, responseProcessingAddStatusCallback); // Add status
			}
			else { // Response NOT already processed
				addStatus(__file, __line, response, "BATCH_START", 200 /* HTTP OK */, serverLog, req, responseProcessingAddStatusCallback); // Add status
			}
		}
		else {
			addStatus(__file, __line, response, "END", 200 /* HTTP OK */, serverLog, req, responseProcessingAddStatusCallback); // Add status
		}
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
 * Function: 	writeResponseFile()
 * Parameters:	ServerLog object, Internal response object, Express HTTP request object, output (JSION as text), file version (.1, .2 etc), optional callback
 * Returns:		Nothing
 * Description: Write response file to disk
 */	
writeResponseFile = function writeResponseFile(serverLog, response, req, output, fileVersion, callback) {		

	const fs = require('fs');
	
	scopeChecker(__file, __line, {
		serverLog: serverLog,
		req: req,
		output: output,
		response: response
	},
	{ // Optional
		callback: callback
	});
	
	if (response.fields["diagnosticFileDir"] && response.fields["responseFileName"]) { // Save to response file
		fs.writeFile(response.fields["diagnosticFileDir"] + "/" + response.fields["responseFileName"] + fileVersion, 
			output,
			/*
			 * Function:	writeResponseFileError()
			 * Parameters:	Error object
			 * Returns:		Nothing
			 * Description: writeFile callback
			 */				
			function writeResponseFileError(err) {
				if (err) {
					serverLog.serverLog(__file, __line, "writeResponseFileError", 
						"Unable to write response file: " + response.fields["responseFileName"] + fileVersion, req, err);
				}
				if (callback) {
					try {
						callback(err);
					}
					catch (e) {
						serverLog.serverError2(__file, __line, "writeResponseFileError", 
							"Recursive error in writeResponseFileError() callback", req, e, response);
					}
				}
			});	
	}
	else if (!response.fields["responseFileName"]) {	
		serverLog.serverError(__file, __line, "responseProcessing", "Unable to save response file; no responseFileName", 
			req, undefined /* err */, response);
	}
}

/*
 * Function: 	setupDiagnostics()
 * Parameters:	File, line called from, Express HTTP request object, ofields object, internal response object, serverLog, httpErrorResponse object
 * Returns:		Nothing
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
		serverLog.serverLog2(lfile, lline, calling_function, 
			"WARNING: Diagnostics file already exists: " + ofields["diagnosticFileDir"] + "/" + ofields["diagnosticFileName"], req);
	}
	response.message+="\n[" + lfile + ":" + lline + "; function: " + calling_function + "()] Creating diagnostics file: " + 
		ofields["diagnosticFileDir"] + "/" + ofields["diagnosticFileName"];
	response.fields=ofields;
	fs.writeFileSync(ofields["diagnosticFileDir"] + "/" + ofields["diagnosticFileName"], 
		response.message);

//
// Write status file
//
	if (fs.existsSync(ofields["diagnosticFileDir"] + "/" + ofields["statusFileName"])) { // Exists
		serverLog.serverLog2(lfile, lline, calling_function, 
			"WARNING: Status file already exists: " + ofields["diagnosticFileDir"] + "/" + ofields["statusFileName"], req);
	}
	response.message+="\n[" + response.fields["uuidV1"] + "] Creating status file: " + response.fields["statusFileName"];
	var statusText = JSON.stringify(response.status);// Convert response.status to JSON 
	fs.writeFileSync(response.fields["diagnosticFileDir"] + "/" + response.fields["statusFileName"], 
		statusText);	

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
 * Function:	getShpConvertTopoJSON()
 * Parameters:	response
 * Returns:		Intermediate  TopoJSON response
 * Description: Get intermediate TopoJSON response from file
 */	
getShpConvertTopoJSON = function getShpConvertTopoJSON(response, req, res, serverLog, httpErrorResponse) {
	const os = require('os'),
		  fs = require('fs');
		  
	scopeChecker(__file, __line, {
		serverLog: serverLog,
		httpErrorResponse: httpErrorResponse,
		response: response,
		req: req,
		res: res
	});
	
	response.message="In: getShpConvertTopoJSON()";	
	var msg="getShpConvertTopoJSON(): ";	
	response.fields=req.query;
	if (response.fields && response.fields["uuidV1"] && response.fields["responseFileName"]) { // Can get state
		if (response.fields["diagnosticFileDir"] == undefined) {
			response.fields["diagnosticFileDir"]=os.tmpdir() + "/shpConvert/" + response.fields["uuidV1"];
		}
		var fileName=response.fields["diagnosticFileDir"] + "/" + response.fields["responseFileName"];
		fs.stat(fileName, 
			/*
			 * Function:	getStatusFExists()
			 * Parameters:	Error object, stat object
			 * Returns:		Nothing
			 * Description: stat callback
			 */		
			function getStatusFExists(err, stats) {
			if (err) {
				msg+="Iintermediate TopoJSON response file: " + fileName + " does not exist";
				httpErrorResponse.httpErrorResponse(__file, __line, "getShpConvertTopoJSON", 
					serverLog, 500, req, res, msg, err /* Error */, response);		
				return;	
			}
			else {
				fs.readFile(fileName, 
				/*
				 * Function:	getStatusFReadFile()
				 * Parameters:	Error object, read data
				 * Returns:		Nothing
				 * Description: readFile callback
				 */
				function getStatusFReadFile(err, topoResponseText) {
					if (err) {
						msg+="Unable to read get intermediate TopoJSON response file: " + fileName;
						httpErrorResponse.httpErrorResponse(__file, __line, "getShpConvertTopoJSON", 
							serverLog, 500, req, res, msg, err /* Error */, response);		
						return;	
					}
					else {	
						response.message+="\nRead get intermediate TopoJSON response file: " + fileName + "; response size: " + topoResponseText.length;
						if (topoResponseText && topoResponseText.length > 0) {
							
							try {
								var topoResponse=JSON.parse(topoResponseText);
								if (topoResponse) {
									if (!res.finished) { // Reply with error if httpErrorResponse.httpErrorResponse() NOT already processed
								
										var output = JSON.stringify(topoResponse);// Convert output response to JSON 
							// Need to test res was not finished by an expection to avoid "write after end" errors			
										try {
											res.write(output);                  // Write output  
											res.end();		
										}
										catch(e) {
											serverLog.serverError(__file, __line, 
												"getShpConvertTopoJSON", "Error in sending response to client", req, e, response);
										}
									}
									else {
										serverLog.serverLog2(__file, __line, "getShpConvertTopoJSON", 
											"Diagnostics >>>\n" +
											response.message + "\n<<< End of diagnostics", req);
										serverLog.serverError2(__file, __line, 
											"getShpConvertTopoJSON", "Unable to return OK response to user - httpErrorResponse() already processed", 
											req, undefined /* err */, response);
									}	
								}
								else {
									throw new Error("Unable to get get intermediate TopoJSON response: response is undefined after parse");
								}
							}
							catch (e) {
								msg+="Unable to get response: parse error: " + e.message;
								httpErrorResponse.httpErrorResponse(__file, __line, "getShpConvertTopoJSON", 
									serverLog, 500, req, res, msg, e /* Error */, response);		
								return;	
							}
						}
						else {
							msg+="Unable to get status: Zero length intermediate TopoJSON response text";
							httpErrorResponse.httpErrorResponse(__file, __line, "getShpConvertTopoJSON", 
								serverLog, 500, req, res, msg, new Error("Unable to get intermediate TopoJSON response: Zero length response") /* Error */, response);		
							return;	
						}	
					}
				});
			}
		});
	}
	else {
		response.message+="\nCannot get intermediate TopoJSON response from file; insufficent fields";
		return undefined;
	}
} // End of getShpConvertTopoJSON()
	
/*
 * Function:	getStatus()
 * Parameters:	response
 * Returns:		Status array as JSON
 * Description: Get status from file
 */	
getStatus = function getStatus(response, req, res, serverLog, httpErrorResponse) {
    const os = require('os'),
		  fs = require('fs');
		  
	scopeChecker(__file, __line, {
		serverLog: serverLog,
		httpErrorResponse: httpErrorResponse,
		response: response,
		req: req,
		res: res
	});
	
	response.message="In: getShpConvertStatus()";	
	var msg="getShpConvertStatus(): ";	
	response.fields=req.query;
	if (response.fields && response.fields["uuidV1"] && response.fields["statusFileName"]) { // Can get state
		if (response.fields["diagnosticFileDir"] == undefined) {
			response.fields["diagnosticFileDir"]=os.tmpdir() + "/shpConvert/" + response.fields["uuidV1"];
		}
		var fileName=response.fields["diagnosticFileDir"] + "/" + response.fields["statusFileName"];
		fs.stat(fileName, 
			/*
			 * Function:	getStatusFExists()
			 * Parameters:	Error object, stat object
			 * Returns:		Nothing
			 * Description: stat callback
			 */		
			function getStatusFExists(err, stats) {
			if (err) {
				msg+="Status file: " + fileName + " does not exist";
				httpErrorResponse.httpErrorResponse(__file, __line, "getStatus", 
					serverLog, 500, req, res, msg, err /* Error */, response);		
				return;	
			}
			else {
				fs.readFile(fileName, 
				/*
				 * Function:	getStatusFReadFile()
				 * Parameters:	Error object, read data
				 * Returns:		Nothing
				 * Description: readFile callback
				 */
				function getStatusFReadFile(err, statusText) {
					if (err) {
						msg+="Unable to read status file: " + fileName;
						httpErrorResponse.httpErrorResponse(__file, __line, "getStatus", 
							serverLog, 500, req, res, msg, err /* Error */, response);		
						return;	
					}
					else {	
						response.message+="\nRead status file: " + fileName + "; status size: " + statusText.length;
						if (statusText && statusText.length > 0) {
							
							try {
								var status=JSON.parse(statusText);
								if (status) {
									response.status=status;
									if (!res.finished) { // Reply with error if httpErrorResponse.httpErrorResponse() NOT already processed
								
										var output = JSON.stringify(response);// Convert output response to JSON 
							// Need to test res was not finished by an expection to avoid "write after end" errors			
										try {
											res.write(output);                  // Write output  
											res.end();		
										}
										catch(e) {
											serverLog.serverError(__file, __line, "getStatus", "Error in sending response to client", req, e, response);
										}
									}
									else {
										serverLog.serverLog2(__file, __line, "getStatus", 
											"Diagnostics >>>\n" +
											response.message + "\n<<< End of diagnostics", req);
										serverLog.serverError2(__file, __line, "getStatus", 
											"Unable to return OK response to user - httpErrorResponse() already processed", 
											req, undefined /* err */, response);
									}	
								}
								else {
									throw new Error("Unable to get status: status is undefined after parse");
								}
							}
							catch (e) {
								msg+="Unable to get status: parse error: " + e.message + "\nStatus text >>>\n" + statusText + "\n<<< End of statusText.";
								httpErrorResponse.httpErrorResponse(__file, __line, "getStatus", 
									serverLog, 500, req, res, msg, e /* Error */, response);		
								return;	
							}
						}
						else {
							msg+="Unable to get status: Zero length status text";
							httpErrorResponse.httpErrorResponse(__file, __line, "getStatus", 
								serverLog, 500, req, res, msg, new Error("Unable to get status: Zero length status text") /* Error */, response);		
							return;	
						}	
					}
				});
			}
		});
	}
	else {
		response.message+="\nCannot get state; insufficent fields";
		return undefined;
	}
}
	
/*
 * Function:	addStatus()
 * Parameters:	file, line called from, response object, textual status, http status code, serverLog object, Express HTTP request object, optional callback
 * Returns:		Nothing
 * Description: Add status to response status array
 */	
addStatus = function addStatus(sfile, sline, response, status, httpStatus, serverLog, req, callback) {
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
	}, { // Optional
		callback: callback
	});	
	var msg;
	var isError=false;

	// Check status, httpStatus
	switch (httpStatus) {
		case 200: /* HTTP OK */
			break;
		case 405: /* HTTP service not supported */
			isError=true;
			break;				
		case 500: /* HTTP error */
			isError=true;
			break;
		case 501: /* HTTP general exception trap */
			isError=true;
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
	
	msg="+" + response.status[response.status.length-1].etime + 
		" S addStatus: " + msg;
			
	if (response.fields["uuidV1"] && response.fields["statusFileName"]) { // Can save state

		if (response.fields["diagnosticFileDir"] == undefined) {
			response.fields["diagnosticFileDir"]=os.tmpdir() + "/shpConvert/" + response.fields["uuidV1"];
		}
		
		msg+="\nRe-creating status file: " + response.fields["statusFileName"];
		if (isError) {		
			serverLog.serverLog2(__file, __line, "addStatus", 
				"WARNING: Error status: " + msg, req, undefined /* Error */, response);
		}
//		console.error(msg);
		response.message+="\n" + msg;
		var statusText = JSON.stringify(response.status);// Convert response.status to JSON 

		fs.writeFile(response.fields["diagnosticFileDir"] + "/" + response.fields["statusFileName"] + ".new", 
			statusText, 
			/*
			 * Function:	addStatusWriteDiagnosticFile()
			 * Parameters:	Error object
			 * Returns:		Nothing
			 * Description: writeFile callback
			 */
			function addStatusWriteDiagnosticFile(err) {
				
				scopeChecker(__file, __line, {
					serverLog: serverLog,
					response: response,
					message: response.message,
					httpStatus: httpStatus
				}, { // Optional
					callback: callback
				});				
				
				if (err) {
					serverLog.serverLog2(__file, __line, "addStatusWriteDiagnosticFile", "WARNING: Unable to write status file", req, err);
				}
				else {
					fs.rename(response.fields["diagnosticFileDir"] + "/" + response.fields["statusFileName"] + ".new",
						response.fields["diagnosticFileDir"] + "/" + response.fields["statusFileName"],
						/*
						 * Function:	addStatusWriteDiagnosticFileRename()
						 * Parameters:	Error object
						 * Returns:		Nothing
						 * Description: rename callback
						 */
						function addStatusWriteDiagnosticFileRename() {
							
							scopeChecker(__file, __line, {
								serverLog: serverLog,
								response: response,
								message: response.message,
								httpStatus: httpStatus
							}, { // Optional
								callback: callback
							});				
	
							if (err) {
								serverLog.serverLog2(__file, __line, "addStatusWriteDiagnosticFileRename", 
									"WARNING: Unable to rename status file", req, err, response);
							}							
							if (callback) {
								try {
									callback(err);
								}
								catch (e) {
									serverLog.serverError2(__file, __line, "addStatusWriteDiagnosticFileRename", 
										"Recursive error in addStatusWriteDiagnosticFileRename() callback; addStatus: " + msg, req, e, response);
								}
							}
//							else {
//								serverLog.serverLog2(__file, __line, "addStatusWriteDiagnosticFileRename", 
//									"WARNING: No callback", req, err);
//							}
						}); // End of addStatusWriteDiagnosticFileRename()
				}
			}); // End of addStatusWriteDiagnosticFile()
// Do not log it - it will be - trust me
//		else { 		
//			serverLog.serverLog2(__file, __line, "addStatus", msg, req);
//		}
	}
	else {
		if (callback && response.fields) {
			msg+="\nNo status file to re-create, missing fields from response: " + JSON.stringify(response.fields, null, 4);
			serverLog.serverLog2(__file, __line, "addStatus", 
				"WARNING: Error with addStatus: " + msg, req, undefined /* Error */, response);
			response.message+="\n" + msg;	
			try {
				callback();
			}
			catch (e) {
				serverLog.serverLog2(__file, __line, "addStatus", 
					"Recursive error in addStatus() callback", req, e);
			}
		}
		else if (callback && response.fields == undefined) {
			msg+="\nNo status file to re-create";
			serverLog.serverLog2(__file, __line, "addStatus", 
				"WARNING: Error with addStatus: " + msg, req, undefined /* Error */, response);
			response.message+="\n" + msg;	
			try {
				callback();
			}
			catch (e) {
				serverLog.serverLog2(__file, __line, "addStatus", 
					"Recursive error in addStatus() callback", req, e);
			}
		}
		else if (response.fields) {
			msg+="\nNo status file to re-create and no callback, missing fields from response: " + 
				JSON.stringify(response.fields, null, 4);
			serverLog.serverLog2(__file, __line, "addStatus", 
				"WARNING: Error with addStatus: " + msg, req, undefined /* Error */, response);
			response.message+="\n" + msg;	
		}
		else {
			msg+="\nNo status file to re-create and no callback";
			response.message+="\n" + msg;	
		}
	}
} // End of addStatus()

/*
 * Function: 	fileSize()
 * Parameters: 	File size
 * Returns: 	Nicely formatted file size
 * Description:	Display file size nicely	
 */
function fileSize(file_size) {
	var niceFileSize;
	if (!file_size) {
		return undefined;
	}
	else if (file_size > 1024 * 1024 * 1024) {
		niceFileSize = (Math.round(file_size * 100 / (1024 * 1024 * 1024)) / 100).toString() + 'GB';
	}
	else if (file_size > 1024 * 1024) {
		niceFileSize = (Math.round(file_size * 100 / (1024 * 1024)) / 100).toString() + 'MB';
	} 
	else {
		niceFileSize = (Math.round(file_size * 100 / 1024) / 100).toString() + 'KB';
	}
	return niceFileSize;
}

/*
 * Function:	shpConvertGetConfig()
 * Parameters:	response
 * Returns:		Status array as JSON
 * Description: Get status from file
 */	
shpConvertGetConfig = function shpConvertGetConfig(response, req, res, serverLog, httpErrorResponse) {
    const os = require('os'),
		  fs = require('fs');
		  
	scopeChecker(__file, __line, {
		serverLog: serverLog,
		httpErrorResponse: httpErrorResponse,
		response: response,
		req: req,
		res: res
	});
	
	response.message="In: shpConvertGetConfig()";	
	var msg="shpConvertGetConfig(): ";	
	response.fields=req.query;
	if (response.fields && response.fields["uuidV1"]) { // Can get XML config
		if (response.fields["diagnosticFileDir"] == undefined) {
			response.fields["diagnosticFileDir"]=os.tmpdir() + "/shpConvert/" + response.fields["uuidV1"];
		}
		if (response.fields["xmlFileName"] == undefined) {
			response.fields["xmlFileName"]="geoDataLoader.xml";
		}
		var fileName=response.fields["diagnosticFileDir"] + "/" + response.fields["xmlFileName"];
		fs.stat(fileName, 
			/*
			 * Function:	shpConvertGetConfigFExists()
			 * Parameters:	Error object, stat object
			 * Returns:		Nothing
			 * Description: stat callback
			 */		
			function shpConvertGetConfigFExists(err, stats) {
			if (err) {
				msg+="\nXML file: " + fileName + " does not exist";
				httpErrorResponse.httpErrorResponse(__file, __line, "shpConvertGetConfigFExists", 
					serverLog, 500, req, res, msg, err /* Error */, response);		
				return;	
			}
			else {
				fs.readFile(fileName, 
				/*
				 * Function:	shpConvertGetConfigFReadFile()
				 * Parameters:	Error object, read data
				 * Returns:		Nothing
				 * Description: readFile callback
				 */
				function shpConvertGetConfigFReadFile(err, xmlText) {
					if (err) {
						msg+="\nUnable to read XML file: " + fileName;
						httpErrorResponse.httpErrorResponse(__file, __line, "shpConvertGetConfigFReadFile", 
							serverLog, 500, req, res, msg, err /* Error */, response);		
						return;	
					}
					else {	
						msg+="\nRead XML file: " + fileName + "; XML size: " + xmlText.length;
						if (xmlText && xmlText.length > 0) {
							
							if (!res.finished) { // Reply with error if httpErrorResponse.httpErrorResponse() NOT already processed
						
					// Need to test res was not finished by an expection to avoid "write after end" errors			
								try {	
									res.setHeader('Content-Type', 'application/xml');
									res.write(xmlText);                  // Write output  
									res.end();		
								}
								catch(e) {
									serverLog.serverError(__file, __line, "shpConvertGetConfigFReadFile", 
										"Error in sending response to client", req, e, response);
								}
							}
							else {
								serverLog.serverLog2(__file, __line, "shpConvertGetConfigFReadFile", 
									"Diagnostics >>>\n" +
									response.message + "\n<<< End of diagnostics", req);
								serverLog.serverError2(__file, __line, "shpConvertGetConfigFReadFile", 
									"Unable to return OK response to user - httpErrorResponse() already processed", 
									req, undefined /* err */, response);
							}	
						}
						else {
							console.error(response.message)
							msg+="\nUnable to get XML: Zero length XML text";
							httpErrorResponse.httpErrorResponse(__file, __line, "shpConvertGetConfigFReadFile", 
								serverLog, 500, req, res, msg, new Error("Unable to get XML: Zero length XML text") /* Error */, response);		
							return;	
						}	
					}
				});
			}
		});
	}
	else {
		msg="Cannot get state; insufficent fields: " + JSON.stringify(req.query, null, 4);	
		httpErrorResponse.httpErrorResponse(__file, __line, "shpConvertGetConfigFReadFile", 
			serverLog, 500, req, res, msg, new Error(msg) /* Error */, response);		
		return;	
	}
} // End of shpConvertGetConfig

/*
 * Function:	shpConvertGetResults()
 * Parameters:	response
 * Returns:		Status array as JSON
 * Description: Get status from file
 */	
shpConvertGetResults = function shpConvertGetResults(response, req, res, serverLog, httpErrorResponse) {
    const os = require('os'),
		  fs = require('fs');
		  
	scopeChecker(__file, __line, {
		serverLog: serverLog,
		httpErrorResponse: httpErrorResponse,
		response: response,
		req: req,
		res: res
	});
	
	response.message="In: shpConvertGetResults()";	
	var msg="shpConvertGetResults(): ";	
	response.fields=req.query;
	if (response.fields && response.fields["uuidV1"]) { // Can get results ZIP file
		if (response.fields["diagnosticFileDir"] == undefined) {
			response.fields["diagnosticFileDir"]=os.tmpdir() + "/shpConvert/" + response.fields["uuidV1"];
		}
		if (response.fields["xmlFileName"] == undefined) {
			response.fields["xmlFileName"]="geoDataLoader.zip";
		}
		var fileName=response.fields["diagnosticFileDir"] + "/" + response.fields["xmlFileName"];
		fs.stat(fileName, 
			/*
			 * Function:	shpConvertGetResultsFExists()
			 * Parameters:	Error object, stat object
			 * Returns:		Nothing
			 * Description: stat callback
			 */		
			function shpConvertGetResultsFExists(err, stats) {
			if (err) {
				msg+="\nresults ZIP file: " + fileName + " does not exist";
				httpErrorResponse.httpErrorResponse(__file, __line, "shpConvertGetResultsFExists", 
					serverLog, 500, req, res, msg, err /* Error */, response);		
				return;	
			}
			else {
				fs.readFile(fileName, 
				/*
				 * Function:	shpConvertGetResultsFReadFile()
				 * Parameters:	Error object, read data
				 * Returns:		Nothing
				 * Description: readFile callback
				 */
				function shpConvertGetResultsFReadFile(err, xmlText) {
					if (err) {
						msg+="\nUnable to read results ZIP file: " + fileName;
						httpErrorResponse.httpErrorResponse(__file, __line, "shpConvertGetResultsFReadFile", 
							serverLog, 500, req, res, msg, err /* Error */, response);		
						return;	
					}
					else {	
						msg+="\nRead results ZIP file: " + fileName + "; results ZIP size: " + xmlText.length;
						if (xmlText && xmlText.length > 0) {
							
							if (!res.finished) { // Reply with error if httpErrorResponse.httpErrorResponse() NOT already processed
						
					// Need to test res was not finished by an expection to avoid "write after end" errors			
								try {	
									res.setHeader('Content-Type', 'application/xml');
									res.write(xmlText);                  // Write output  
									res.end();		
								}
								catch(e) {
									serverLog.serverError(__file, __line, "shpConvertGetResultsFReadFile", 
										"Error in sending response to client", req, e, response);
								}
							}
							else {
								serverLog.serverLog2(__file, __line, "shpConvertGetResultsFReadFile", 
									"Diagnostics >>>\n" +
									response.message + "\n<<< End of diagnostics", req);
								serverLog.serverError2(__file, __line, "shpConvertGetResultsFReadFile", 
									"Unable to return OK response to user - httpErrorResponse() already processed", 
									req, undefined /* err */, response);
							}	
						}
						else {
							console.error(response.message)
							msg+="\nUnable to get results ZIP: Zero length results ZIP text";
							httpErrorResponse.httpErrorResponse(__file, __line, "shpConvertGetResultsFReadFile", 
								serverLog, 500, req, res, msg, new Error("Unable to get results ZIP: Zero length XML text") /* Error */, response);		
							return;	
						}	
					}
				});
			}
		});
	}
	else {
		msg="Cannot get state; insufficent fields: " + JSON.stringify(req.query, null, 4);	
		httpErrorResponse.httpErrorResponse(__file, __line, "shpConvertGetResultsFReadFile", 
			serverLog, 500, req, res, msg, new Error(msg) /* Error */, response);		
		return;	
	}
} // End of shpConvertGetResults

module.exports.setupDiagnostics = setupDiagnostics;
module.exports.createTemporaryDirectory = createTemporaryDirectory;
module.exports.recreateDiagnosticsLog = recreateDiagnosticsLog;
module.exports.responseProcessing = responseProcessing;
module.exports.addStatus = addStatus;
module.exports.getStatus = getStatus;
module.exports.getShpConvertTopoJSON = getShpConvertTopoJSON;
module.exports.writeResponseFile = writeResponseFile;
module.exports.fileSize = fileSize;
module.exports.shpConvertGetConfig = shpConvertGetConfig;
module.exports.shpConvertGetResults = shpConvertGetResults;

// Eof