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
     TempData = function() {
		
		this.file = '';
		this.file_list = [];
		this.no_files = 0;	
		this.myId = '';
		
        return this; 
     }; // End of globals

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
			fields: [] 
		};
		var d_files = { 
			d_list: []
		}
		
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
			req.busboy.on('filesLimit', function() {
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
			req.busboy.on('fieldsLimit', function() {	
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
			req.busboy.on('partsLimit', function() {
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
			req.busboy.on('file', function(fieldname, stream, filename, encoding, mimetype) {
				
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
 * Parameters:	None
 * Description: Data processor. Push data onto d.file.chunks[] array. Binary safe.
 *				Emit message every 10M
 */
				stream.on('data', function(data) {
					d.file.chunks.push(data); 
					d.file.partial_chunk_size+=data.length;
					d.file.chunks_length+=data.length;
					if (d.file.partial_chunk_size > 10*1024*1024) { // 10 Mb
						response.message+="\nFile [" + d.no_files + "]: " + d.file.file_name + "; encoding: " +
							d.file.file_encoding + 
							'; read [' + d.file.chunks.length + '] ' + d.file.partial_chunk_size + ', ' + d.file.chunks_length + ' total';
						d.file.partial_chunk_size=0;
					}
				});
				
/*
 * Function: 	req.busboy.on('file').stream.on:('error') callback function
 * Parameters:	None
 * Description: EOF processor. Concatenate d.file.chunks[] array, uncompress if needed.
 */
				stream.on('error', function(err) {
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
				});				

/*
 * Function: 	req.busboy.on('file').stream.on:('end') callback function
 * Parameters:	None
 * Description: EOF processor. Concatenate d.file.chunks[] array, uncompress if needed.
 */
				stream.on('end', function() {
					
					var msg;
					var buf=Buffer.concat(d.file.chunks); // Safe binary concat
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
				}); // End of EOF processor
					
			}); // End of file attachment processing function: req.busboy.on('file')
			  
/*
 * Function: 	req.busboy.on('field') callback function
 * Parameters:	fieldname, value, fieldnameTruncated, valTruncated
 * Description:	Field processing function; fields supported  
 *
 *			 	verbose: 	Produces debug returned as part of reponse.message
 */ 
			req.busboy.on('field', function(fieldname, val, fieldnameTruncated, valTruncated) {
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
			req.busboy.on('finish', function() {
				try {
					var msg="";
					
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
							var shpList = {};
							var shpTotal=0;			

							if (!ofields["uuidV1"]) { // Generate UUID
								ofields["uuidV1"]=serverLog.generateUUID();
							}
																									
							for (var i = 0; i < response.no_files; i++) {
								d=d_files.d_list[i];
								rval=shpConvert.shpConvertFileProcessor(d, shpList, shpTotal, path, response, ofields["uuidV1"]);
								if (rval.file_errors > 0 ) {
									httpErrorResponse.httpErrorResponse(__file, __line, "req.busboy.on('finish')", 
										serverLog, 500, req, res, rval.msg, undefined, response);							
									return;
								}	
								else {
									if (rval.msg) {
										msg+=rval.msg + "\n";			
									}
									shpTotal=rval.shpTotal;
								}									
							} // End of for loop	
						
							// Check which files and extensions are present, convert shapefiles to geoJSON						
							rval=shpConvert.shpConvertCheckFiles(shpList, response, shpTotal, ofields, serverLog, 
								req, res, shapefile_options);
							if (rval.file_errors > 0 ) {
								httpErrorResponse.httpErrorResponse(__file, __line, "req.busboy.on('finish')", 
									serverLog, 500, req, res, rval.msg, undefined, response);							
								return;
							}
							else {
								if (rval.msg) {
									msg+=rval.msg + "\n";			
								}
							}							
						}	

						if (!ofields["my_reference"]) {
							msg+="[No my_reference] Processed: " + response.no_files + " files";
						}
						else {
							msg+="[my_reference: " + ofields["my_reference"] + "] Processed: " + response.no_files + " files";
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
					response.fields=ofields;				// Add return fields	
					if (response.no_files == 0) { 
						msg="FAIL! No files attached\n";						
						response.message = msg + "\n" + response.message;
						response.fields=ofields;				// Add return fields
						response.file_errors++;					// Increment file error count	
						httpErrorResponse.httpErrorResponse(__file, __line, "req.busboy.on('finish')", 
							serverLog, 500, req, res, msg, undefined, response);							
						return;						
					}
					
					// Final processing
					if (req.url == '/shpConvert') { // Processed by shpConvertCheckFiles() - uses async
					}
					else if (req.url == '/geo2TopoJSON') {	
						if (response.no_files == 0) { 
							msg="FAIL! No files attached\n";						
							response.message = msg + "\n" + response.message;
							response.fields=ofields;				// Add return fields
							response.file_errors++;					// Increment file error count	
							httpErrorResponse.httpErrorResponse(__file, __line, "req.busboy.on('finish')", 
								serverLog, 500, req, res, msg, undefined, response);							
							return;						
						}
						else if (response.field_errors == 0 && response.file_errors == 0) { // OK
							serverLog.serverLog2(__file, __line, "req.busboy.on:('finish')", msg, req);	
							if (!req.finished) { // Reply with error if httpErrorResponse.httpErrorResponse() NOT already processed					
								var output = JSON.stringify(response);// Convert output response to JSON 
			// Need to test res was not finished by an expection to avoid "write after end" errors			
								res.write(output);                  // Write output  
								res.end();	
							}
							else {
								serverLog.serverLog("FATAL! Unable to return OK reponse to user - httpErrorResponse() already processed", req);
							}	
		//					console.error(util.inspect(req));
		//					console.error(JSON.stringify(req.headers, null, 4));
						}
						else if (response.field_errors > 0 && response.file_errors > 0) {
							msg+="\nFAIL! Field processing ERRORS! " + response.field_errors + 
								" and file processing ERRORS! " + response.file_errors + "\n" + msg;
							response.message = msg + "\n" + response.message;						
							httpErrorResponse.httpErrorResponse(__file, __line, "req.busboy.on('finish')", 
								serverLog, 500, req, res, msg, undefined, response);				  
						}				
						else if (response.field_errors > 0) {
							msg+="\nFAIL! Field processing ERRORS! " + response.field_errors + "\n" + msg;
							response.message = msg + "\n" + response.message;
							httpErrorResponse.httpErrorResponse(__file, __line, "req.busboy.on('finish')", 
								serverLog, 500, req, res, msg, undefined, response);				  
						}	
						else if (response.file_errors > 0) {
							msg+="\nFAIL! File processing ERRORS! " + response.file_errors + "\n" + msg;
							response.message = msg + "\n" + response.message;					
							httpErrorResponse.httpErrorResponse(__file, __line, "req.busboy.on('finish')", 
								serverLog, 500, req, res, msg, undefined, response);				  
						}	
						else {
							msg+="\nUNCERTAIN! Field processing ERRORS! " + response.field_errors + 
								" and file processing ERRORS! " + response.file_errors + "\n" + msg;
							response.message = msg + "\n" + response.message;						
							httpErrorResponse.httpErrorResponse(__file, __line, "req.busboy.on('finish')", 
								serverLog, 500, req, res, msg, undefined, response);
						}
					}
				} catch(e) {
					httpErrorResponse.httpErrorResponse(__file, __line, "req.busboy.on('finish')", 
						serverLog, 500, req, res, 'Caught unexpected error (possibly async)', e, undefined /* My response */);
					return;
				}
			});

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