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
// Rapid Enquiry Facility (RIF) - GeoJSON to topoJSON converter webservice
//								  Uses node.js TopoJSON module
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
var inspect = require('util').inspect,
	topojson = require('topojson'),
	zlib = require('zlib'),
    stderrHook = require('./stderrHook'),
    rifLog = require('./rifLog'),
    os = require('os'),
    fs = require('fs'),

    getQuantization = function(lvl) {
         if (lvl <= 6) {
            return 400;
         } else if (lvl == 7) {
            return 700;
         } else if (lvl == 8) {
            return 1500;
         } else if (lvl == 9) {
            return 3000;
         } else if (lvl == 10) {
            return 5000;
         } else {
            return 10000;
         }
     },
     TempData = function() {
		
		this.file = '';
		this.file_list = [];
		this.no_files = 0;	
		this.myId = '';
		
        return this; 
     },

/*
 * Function: 	_http_error_response() 
 * Parameters:  File called from, line number called from, procedure called from, 
 *				HTTP status,
 *				HTTP request object,
 *				HTTP response object,
 * 				Message text,
 *				Error object
 */		
	_http_error_response=function(file, line, calling_function, status, req, res, msg, err) {
		rifLog.rifLog2(file, line, calling_function, msg, req, err);
		res.status(500);					  
		res.write(msg);
		res.end();	
	},
	 
/*
 * Function:	_process_json()
 * Parameters:	d object (temporary processing data, 
				ofields [field parameters array],
				TopoJSON topology processing options,
				HTTP request object,
				HTTP response object, 
				my response object
 * Returns:		d object topojson/Nothing on failure
 * Description: TopoJSON processing:
 *				- converts string to JSON
 *				- calls topojson.topology() using options
 * 				- Add file name, stderr and topoJSON to my response
 */
	_process_json=function(d, ofields, options, stderr, req, res, response) {
	var msg="File [" + d.no_files + "]: " + d.file.file_name;
	
	response.message = response.message + '\nProcessing ' + msg;	
    try {	
		d.file.jsonData = undefined;
		d.file.jsonData = JSON.parse(d.file.file_data.toString()); // Parse file stream data to JSON

		// Re-route topoJSON stderr to stderr.str
		stderr.disable();
		d.file.topojson = topojson.topology({   // Convert geoJSON to topoJSON
			collection: d.file.jsonData
			}, options);				
		stderr.enable(); 				   // Re-enable stderr
		
		d.file.topojson_stderr=stderr.str();  // Get stderr as a string	
		stderr.clean();						// Clean down stderr string
		stderr.restore();                  // Restore normal stderr functionality 

// Add file name, stderr and topoJSON to my response
// This will need a mutex if > 1 thread is being processed at the same time
		response.file_list[d.no_files-1] = {
			file_name: d.file.file_name,
			topojson: d.file.topojson,
			topojson_stderr: d.file.topojson_stderr
		};	
		
		msg+= "; topoJSON: " + JSON.stringify(d.file.topojson).length + "]"
		if (d.file.topojson_stderr.length > 0) {  // Add topoJSON stderr to message	
// This will need a mutex if > 1 thread is being processed at the same time	
			response.message = response.message + "\n" + msg + " OK:\nTopoJson.topology() stderr >>>\n" + 
				d.file.topojson_stderr + "<<< TopoJson.topology() stderr";
			rifLog.rifLog("TopoJson.topology() stderr; " + msg  + "TopoJson.topology() stderr >>>\n"  + 
				d.file.topojson_stderr + "<<< TopoJson.topology() stderr", 
				req);
		}
		else {
// This will need a mutex if > 1 thread is being processed at the same time
			response.message = response.message + "\n" + msg + " OK";
			rifLog.rifLog("TopoJson.topology() no stderr; " + msg, 
				req);		
		}			
															   
		return d.file.topojson;								   
	} catch (e) {                            // Catch conversion errors

		stderr.restore();                  // Restore normal stderr functionality 	
		if (!d.file.jsonData) {
			msg="does not seem to contain valid JSON";
		}
		else {
			msg="does not seem to contain valid TopoJSON";
		}
		msg="Your input file " + d.no_files + ": " + 
			d.file.file_name + "; size: " + d.file.file_data.length + 
			"; " + msg + ": \n" + "Debug message:\n" + response.message + "\n\n";
		if (d.file.file_data.length > 0) {
			msg=msg + "\nTruncated data:\n" + 
				d.file.file_data.toString('hex').substring(0, 132) + "...\r\n";
		}
					
		_http_error_response(__file, __line, "_process_json()", 500, req, res, msg, e);				
		return;
	} 	
};

/*
 * Function: 	exports.convert()
 * Parameters:	Express HTTP request object, response object
 * Description:	Express web server handler function for topoJSON conversion
 */
exports.convert = function(req, res) {

	try {
		
//  req.setEncoding('utf-8'); // This corrupts the data stream with binary data
//	req.setEncoding('binary'); // So does this! Leave it alone - it gets it right!

		res.setHeader("Content-Type", "text/plain");
		
	// Add stderr hook to capture debug output from topoJSON	
		var stderr = stderrHook.stderrHook(function(output, obj) { 
			output.str += obj.str;
		});
		
	// Response	
		var response = {                 // Set output response    
			no_files: 0,    
			field_errors: 0,
			file_list: [],
			message: '',               
			fields: [] 
		};
		var d_files = { 
			d_list: []
		}
			
	// Post method	
		if (req.method == 'POST') {
	 
	// Default topojson options 
			var options = {
				verbose: false,
				quantization: 1e4,				
				projection: "4326"		
			};
		
	// Default return fields	
			var ofields = {
				my_reference: '', 
				zoomLevel: 0, 
				verbose: false,
				quantization: options.quantization,
				projection: options.projection
			};	
			
	// File attachment processing function		  
			req.busboy.on('file', function(fieldname, stream, filename, encoding, mimetype) {
				
				var d = new TempData(); // This is local to the post requests; the field processing cannot see it	
				
	//        this.withinLimit = true;
	//        this.upper_limit = 1e8;				
				d.file = { // File return data type
					file_name: "",
					temp_file_name: "",
					file_encoding: "",	
					extension: "",
					jsonData: "",
					file_data: "",
					chunks: [],
					topojson: "",
					topojson_stderr: ""
				};

				// This will need a mutex if > 1 thread is being processed at the same time
				response.no_files++;	// Increment file counter
				d.no_files=response.no_files; // Local copy
				
				d.file.file_name = filename;
				d.file.temp_file_name = os.tmpdir()  + "/" + filename;
				d.file.file_encoding=req.get('Content-Encoding');
				d.file.extension = filename.split('.').pop();
				
				if (!d.file.file_encoding) {
					if (d.file.extension === "gz") {
							d.file.file_encoding="gzip";
					}
					else if (d.file.extension === "lz77") {
							d.file.file_encoding="zlib";
					}
				}
			
	// Data processor			
				stream.on('data', function(data) {
					d.file.chunks.push(data);  
				
	/*			    if (d.file.file_data.length > d.upper_limit) { // Max geojs allowed upper_limit
						d.withinLimit = false;  
						try { 
							console.log("toTopoJSON(): Stopping file: " + d.file.file_name + " upload...");
						} catch (e) { 
							var msg="EXCEPTION! toTopoJSON.js: File: " + d.file.file_name + " upload stopped: " + e; 
													console.error(msg);					  
							res.status(500);					  
							res.write(msg);
							res.end();						
							return;
						};     
					}; */
				});

	// EOF processor 
				stream.on('end', function() {
	//			     d.file.file_data = d.file.file_data.replace(/(\r\n|\n|\r)/gm,""); CRLF=> CR
	//                 if (d.file.file_name != '' && d.withinLimit) {	
		
							var buf=Buffer.concat(d.file.chunks);
							
							d.file.file_data="";
							if (d.file.file_encoding === "gzip") {
								d.file.file_data=zlib.gunzipSync(buf)							
								rifLog.rifLog2(__file, __line, "req.busboy.on('file').stream.on:('end')", 
									"File [" + d.no_files + "]: " + d.file.file_name + "; encoding: " +
									d.file.file_encoding + "; zlib.gunzip(): " + d.file.file_data.length + 
									"; from buf: " + buf.length, req); 
							}	
							else if (d.file.file_encoding === "zlib") {	
								d.file.file_data=zlib.inflateSync(buf)							
								rifLog.rifLog2(__file, __line, "req.busboy.on('file').stream.on:('end')", 
									"File [" + d.no_files + "]: " + d.file.file_name + "; encoding: " +
									d.file.file_encoding + "; zlib.inflate(): " + d.file.file_data.length + 
									"; from buf: " + buf.length, req); 
							}
							else if (d.file.file_encoding === "zip") {
								msg="FAIL! File [" + d.no_files + "]: " + d.file.file_name + "; extension: " + 
										file.extension + "; file_encoding: " + d.file.file_encoding + " not supported";
								_http_error_response(__file, __line, "req.busboy.on('file').stream.on:('end')", 500, req, res, msg);
								return;							
							}
							else {
								d.file.file_data=buf;
								rifLog.rifLog2(__file, __line, "req.busboy.on('file').stream.on:('end')", 			
									"File [" + d.no_files + "]: " + d.file.file_name + "; encoding: " +
									"; uncompressed data: " + d.file.file_data.length, req); 												
							}
							
							d_files.d_list[d.no_files-1] = d;						
						
	//                }
				}); // End of EOF processor
					
			}); // End of file attachment processing function
			  
	// Field processing function        
			req.busboy.on('field', function(fieldname, val, fieldnameTruncated, valTruncated) {
				var text="";
				if (fieldname == 'zoomLevel') {
				   options.quantization = getQuantization(val);
				   text="Quantization set to: " + options.quantization;
				   ofields["quantization"]=options.quantization;
				}
				else if (fieldname == 'projection') {
				   options.projection = val;
				   text="Projection set to: " + options.projection;
				   ofields["projection"]=options.projection;
				}
				else if ((fieldname == 'verbose')&&(val == 'true')) {
					options.verbose = true;
					text="verbose mode enabled";
					ofields[fieldname]="true";
				}
				else if (fieldname == 'id') {				
					text="myId() function id field set to: " + val;
					ofields[fieldname]=val;				
	//
	// Promote tile gid to id
	//					
					ofields.myId = function(d) {
	// Dont use eval() = it is source of potential injection
	//					var rval=eval("d.properties." + ofields[fieldname]);
						if (!d.properties[ofields[fieldname]]) {
							response.field_errors++;
							var msg="ERROR! Id field: d.properties." + ofields[fieldname] + " does not exist";
							if (options.id) {
								rifLog.rifLog2(__file, __line, "req.busboy.on('field')", msg, req);	
								options.id = undefined; // Prevent this section running again!	
							}
						}
						else {
							return d.properties[ofields[fieldname]];
						}
	//					response.message = response.message + "\nCall myId() for id field: " + ofields[fieldname] + "; value: " + rval;									
					}						
					options.id = ofields.myId;				
				}
				else {
					ofields[fieldname]=val;				
				}	
				response.message = response.message + "\nField: " + fieldname + "[" + val + "]; " + text;
			 }); // End of field processing function
					  
	// End of request - complete response		  
			req.busboy.on('finish', function() {
				var msg;
				
				for (i = 0; i < response.no_files; i++) {	
					d=d_files.d_list[i];
					if (!d.file) {
						msg="FAIL! File [" + (i+1) + "/" + d.no_files + "]: not found in list";
						_http_error_response(__file, __line, "req.busboy.on('finish')", 500, req, res, msg);
						return;			
					}
					else if (d.file.file_data.length > 0) {
						d=_process_json(d, ofields, options, stderr, req, res, response);	
						if (!d) {
							return; // _process_json() has emitted the error
						}
					}	
					else {
						msg="FAIL! File [" + (i+1) + "/" + d.no_files + "]: " + d.file.file_name + "; extension: " + 
								file.extension + "; file size is zero";
						_http_error_response(__file, __line, "req.busboy.on('finish')", 500, req, res, msg);
						return;
					}	
				}
				if (!ofields["my_reference"]) {
					msg="[No my_reference] Processed: " + response.no_files + " files; debug message:\n" + response.message
				}
				else {
					msg="[my_reference: " + ofields["my_reference"] + "] Processed: " + response.no_files + " files; debug message:\n" + response.message
				}
					
				response.fields=ofields;				// Add return fields	
				var output;
				if (response.field_errors == 0) { // OK
					rifLog.rifLog2(__file, __line, "req.busboy.on:('finish')", msg, req);					
					output = JSON.stringify(response);// Convert output response to JSON 
	// Need to test res was not finished by an expection to avoid "write after end" errors			
					res.write(output);                  // Write output  
					res.end();				
				}
				else {
					msg = "FAIL! Field processing ERRORS! " + response.field_errors + "\n" + msg;
					_http_error_response(__file, __line, "req.busboy.on('finish')", 500, req, res, msg);				  
				}		

			});

			req.pipe(req.busboy); // Pipe request stream to busboy form data handler
			  
		} // End of post method
		else {
			var msg="ERROR! GET Requests not allowed; please see: " + 
				"https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifNodeServices/readme.md Node Web Services API for RIF 4.0 documentation for help";
			_http_error_response(__file, __line, "exports.convert", 405, req, res, msg);		
			return;		  
		}
		
	} catch (e) {                            // Catch syntax errors
		var msg="General processing ERROR!";				  
		_http_error_response(__file, __line, "exports.convert catch()", 500, req, res, msg, e);		
		return;
	}
	  
};