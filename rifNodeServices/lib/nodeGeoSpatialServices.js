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
 
//  Globals: it needs to be because it checks for variable scoping
global.scopeChecker=require('../lib/scopeChecker');

// Locals to this module. Not necesarily to all async or required modules (hence the need for the scope checker)!!!!	
var util = require('util'),
    async = require('async'),
    os = require('os'),
    fs = require('fs'),
	zlib = require('zlib'),
	path = require('path'),
    geo2TopoJSON = require('../lib/geo2TopoJSON'),
    shpConvert = require('../lib/shpConvert'),
	stderrHook = require('../lib/stderrHook'),
    httpErrorResponse = require('../lib/httpErrorResponse'),
    serverLog = require('../lib/serverLog'),
    nodeGeoSpatialServicesCommon = require('../lib/nodeGeoSpatialServicesCommon'),
	simplifyGeoJSON=require('../lib/simplifyGeoJSON'),
	tileViewer=require('../lib/tileViewer'),
/*
 * Function: 	TempData() 
 * Parameters:  NONE
 * Description: Construction for TempData (d object)
 */
     TempData = function TempData() {
		
		this.file = '';
		this.file_list = [];
		this.no_files = 0;	
		this.myId = '';
		
        return this; 
     }; // End of globals

	/*
	 * Function:	createD()
	 * Parameters:	filename, encoding, mimetype, response object, HTTP request object
	 * Returns:		D object
	 * Description: Create D object. Should really be an object!!!
	 */		
	var createD = function createD(filename, encoding, mimetype, response, req) {

		scopeChecker(__file, __line, {
			response: response,
			filename: filename,
			req: req
		});	
		
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
			else if (d.file.extension === "zip") {
					d.file.file_encoding="zip";
			}					
		}
		
		return d;
	} // End of createD

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
		
// Set timeout for 10 minutes
		req.setTimeout(10*60*1000, function myTimeoutFunction() {
			serverLog.serverError2(__file, __line, "exports.convert", "Message timed out at: " + req.timeout, 
				undefined /* req */, undefined /* err */, response);	
			req.abort();
		}); // 10 minutes
		
// Add stderr hook to capture debug output from topoJSON	
		var stderr = stderrHook.stderrHook(function myStderrHook(output, obj) { 
			output.str += obj.str;
		});
		
/*
 * Services supported:
 * 
 * shpConvert: Upload then convert shapefile to geoJSON;
 * geo2TopoJSON: Convert geoJSON to TopoJSON;
 * getShpConvertStatus: Get shapefile conversion status;
 * getShpConvertTopoJSON: Get shapefile converts into topoJSON optimised for zoomnlevels 6-11
 * shpConvertGetConfig.xml: Get shapefile conversion XML configuration
 * shpConvertGetResults.zip: Get shapefile conversion results zip file
 * getGeographies: Get geogrpahies in a database
 * getMapTile: Get maptile for specified database, geography, geolevel, zoomlevel, X and Y tile number.
 *
 * Not yet imnplement:
 *
 * simplifyGeoJSON: Load, validate, aggregate, clean and simplify converted shapefile data;
 * geoJSONtoWKT: Convert geoJSON to Well Known Text (WKT);
 * createHierarchy: Create hierarchical geospatial intersection of all the shapefiles;
 * createCentroids: Create centroids for all shapefiles;
 * createMaptiles: Create topoJSON maptiles for all geolevels and zoomlevels; 
 * getGeospatialData: Fetches GeoSpatial Data;
 * getNumShapefilesInSet: Returns the number of shapefiles in the set. This is the same as the highest resolution geolevel id;
 */		
		if (!((req.params["shpConvert"] == 'shpConvert') ||
			  (req.params["shpConvert"] == 'geo2TopoJSON') ||
			  (req.params["shpConvert"] == 'getShpConvertStatus') ||
			  (req.params["shpConvert"] == 'getShpConvertTopoJSON') ||
			  (req.params["shpConvert"] == 'shpConvertGetConfig.xml') ||
			  (req.params["shpConvert"] == 'shpConvertGetResults.zip') ||
			  (req.params["shpConvert"] == 'getGeographies') ||
			  (req.params["shpConvert"] == 'getMapTile') /* ||
			  (req.params["shpConvert"] == '/simplifyGeoJSON') ||
			  (req.params["shpConvert"] == '/geoJSONtoWKT') ||
			  (req.params["shpConvert"] == '/createHierarchy') ||
			  (req.params["shpConvert"] == '/createCentroids') ||
			  (req.params["shpConvert"] == '/createMaptiles') ||
			  (req.params["shpConvert"]["shpConvert"] == '/getGeospatialData') ||
			  (req.params["shpConvert"] == '/getNumShapefilesInSet') */ )) {
			var msg="ERROR! " + req.url + " service invalid; please see: " + 
				"https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifNodeServices/readme.md Node Web Services API for RIF 4.0 documentation for help"; 
			response.fields=req.query;
			response.message="ERROR! " + req.url + " invalid service; params: " + JSON.stringify(req.params);
			httpErrorResponse.httpErrorResponse(__file, __line, "exports.convert", 
				serverLog, 405, req, res, msg, undefined /* error */, response);		
			return;					
		}
	
	// Post method	
		if (req.method == 'POST') {
		
	// Default field value - for return	
			var ofields = {	
				my_reference: '', 
				verbose: false
			}
			var topojson_options = {
				verbose: true		
			};				
			if (req.url == '/geo2TopoJSON') {
				// Default geo2TopoJSON options (see topology Node.js module)
//				ofields.zoomLevel=0; 
//				ofields.quantization=topojson_options["pre-quantization"];
//				ofields.projection=topojson_options.projection;
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
			else {								// All other post methods are errors
				var msg="ERROR! "+ req.url + " post requests not allowed; please see: " + 
					"https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifNodeServices/readme.md Node Web Services API for RIF 4.0 documentation for help";
				httpErrorResponse.httpErrorResponse(__file, __line, "exports.convert", 
					serverLog, 405, req, res, msg);		
				return;		  
			}
			
			nodeGeoSpatialServicesCommon.addStatus(__file, __line, response, "INIT", 200 /* HTTP OK */, serverLog, req);  // Add initial status
			
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
				
				var d=createD(filename, encoding, mimetype, response, req); // Create D object
				
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
					d.file.file_data=buf;
					if (d.file.file_encoding) {
						response.message+="\nFile received OK [" + d.no_files + "]: " + d.file.file_name + 
							"; encoding: " + d.file.file_encoding +
							"; uncompressed data: " + d.file.file_data.length + " bytes", req;
					}
					else {
						response.message+="\nFile received OK [" + d.no_files + "]: " + d.file.file_name + 
							"; uncompressed data: " + d.file.file_data.length + " bytes", req; 
					}										
					d_files.d_list[d.no_files-1] = d;

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
				
				// Process common fields
				if ((fieldname == 'verbose')&&(val == 'true')&&(ofields["verbose"] != "true")) {
					text+="verbose mode enabled";
					ofields[fieldname]="true";
				}
				if ((fieldname == 'diagnostics')&&(val == 'true')&&(ofields["verbose"] != "true")) { // Called diagnostics in JQuery UI interface
					text+="verbose mode enabled";
					ofields["verbose"]="true";
				}				
				else if ((fieldname == 'batchMode')&&(val == 'true')) {
					text+="batch mode enabled";
					ofields[fieldname]="true";
				}					
				else if (fieldname == 'quantization') {
					if (val && isNaN(val) && (typeof val != 'number')) {
						text+="\FIELD PROCESSING ERROR! value is not a number: " + val;
						response.field_errors++;					
					}
					else if (val && (val < 1500 || val > 1e9)) {			
						text+="\FIELD PROCESSING ERROR! value is not between 1500 and 1e9: " + val;
						response.field_errors++;						
					}
					else if (val) {
					   ofields["quantization"]=val;
					   topojson_options["pre-quantization"] = val;
					   topojson_options["post-quantization"] = val;
					   text+="Initial pre and post quantization: " + topojson_options["pre-quantization"];
					}
					else {
						text+="\FIELD PROCESSING ERROR! value is null";
						response.field_errors++;
					}
				}
				else if (fieldname == 'simplificationFactor') {
					if (val && isNaN(val) && (typeof val != 'number')) {
						text+="\FIELD PROCESSING ERROR! value is not a number: " + val;
						response.field_errors++;					
					}
					else if (val && (val < 0.1 || val > 1)) {			
						text+="\FIELD PROCESSING ERROR! value is not between 0.1 and 1: " + val;
						response.field_errors++;						
					}
					else if (val) {
					   ofields["simplificationFactor"]=val;
					   text+="Simplification factor: " + ofields["simplificationFactor"];
					}
					else {
						text+="\FIELD PROCESSING ERROR! value is null";
						response.field_errors++;
					}
				}		
				else if (fieldname == "min_zoomLevel") {
					if (val && isNaN(val) && (typeof val != 'number') && (val % 1 != 0)) {
						text+="\FIELD PROCESSING ERROR! value is not an integer: " + val;
						response.field_errors++;					
					}
					else if (val && (val < 0 || val > 11)) {			
						text+="\FIELD PROCESSING ERROR! value is not between 0 and 11: " + val;
						response.field_errors++;						
					}					
					else if (val) {
						topojson_options["pre-quantization"] = simplifyGeoJSON.getQuantization(val);
						topojson_options["post-quantization"] = simplifyGeoJSON.getQuantization(val);
						ofields["min_zoomLevel"]=val;
					}
					else {
						text+="\FIELD PROCESSING ERROR! value is null";
						response.field_errors++;
					}
				}				
				else if (fieldname == "max_zoomLevel") {
					if (val && isNaN(val) && (typeof val != 'number') && (val % 1 != 0)) {
						text+="\FIELD PROCESSING ERROR! value is not an integer: " + val;
						response.field_errors++;					
					}
					else if (val && (val < 0 || val > 11)) {			
						text+="\FIELD PROCESSING ERROR! value is not between 0 and 11: " + val;
						response.field_errors++;						
					}					
					else if (val) {
						topojson_options["pre-quantization"] = simplifyGeoJSON.getQuantization(val);
						topojson_options["post-quantization"] = simplifyGeoJSON.getQuantization(val);
						ofields["max_zoomLevel"]=val;
						if (!ofields["quantization"]) {
							text+="Initial zoomelvel: " + ofields["max_zoomLevel"] + "; quantization: " + topojson_options["pre-quantization"];
							ofields["quantization"]=topojson_options["pre-quantization"];
						}
					}
					else {
						text+="\FIELD PROCESSING ERROR! value is null";
						response.field_errors++;
					}
				}
				else if ((fieldname == 'uuidV1')&&(!response.fields["diagnosticFileDir"])) { // Start the diagnostics log as soon as possible
					ofields[fieldname]=val;
					nodeGeoSpatialServicesCommon.setupDiagnostics(__file, __line, req, ofields, response, serverLog, httpErrorResponse);
				}
				
				// Call URL specific code
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

				/*
				 * Function: 	fileCompressionProcessing()
				 * Parameters:	d [data] object, internal response object, serverLog object, d_files flist list object,
				 *				HTTP request object, callback
				 * Description:	Call file processing; handle zlib, gz and zip files
				 */	
				var fileCompressionProcessing = function fileCompressionProcessing(d, index, response, serverLog, d_files, req, callback) {
					var msg;
					
					const JSZip = require('JSZip');
					scopeChecker(__file, __line, {
						d: d,
						index: index,
						response: response,
						serverLog: serverLog,
						d_files: d_files,
						req: req,
						zlib: zlib,
						JSZip: JSZip,
						callback: callback,
						async: async,
						nodeGeoSpatialServicesCommon: nodeGeoSpatialServicesCommon,
						addStatus: nodeGeoSpatialServicesCommon.addStatus,
						addStatus: nodeGeoSpatialServicesCommon.fileSize
					});
					const path = require('path');
					
					var lstart = new Date().getTime();
					var new_no_files=0;
						
					if (d.file.file_encoding === "gzip") {
						nodeGeoSpatialServicesCommon.addStatus(__file, __line, response, "Processing gzip file [" + (index+1) + "]: " + d.file.file_name + 
							"; size: " + nodeGeoSpatialServicesCommon.fileSize(d.file.file_data.length), 
							200 /* HTTP OK */, serverLog, req,  // Add file compression processing status		
							/*
							 * Function: 	gzipAddStatusCallback()
							 * Parameters:	error object
							 * Description:	Add status callback
							 */							
							function gzipAddStatusCallback(err, result) {
								if (err) {								
									serverLog.serverLog2(__file, __line, "gzipAddStatusCallback", 
										"WARNING: Unable to add gzip file processing status", req, err);
								}
							
								zlib.gunzip(d.file.file_data, 
									/*
									 * Function: 	gunzipFileCallback()
									 * Parameters:	error object, result
									 * Description:	Add status callback
									 */	
									function gunzipFileCallback(err, result) {
									if (err) {	
										msg="FAIL! File [" + (index+1) + "]: " + d.file.file_name + "; extension: " + 
										d.file.extension + "; file_encoding: " + d.file.file_encoding + " inflate exception";
										d.file.file_error=msg;	
										response.message=msg + "\n" + response.message;
										response.file_errors++;					// Increment file error count	
										serverLog.serverLog2(__file, __line, "fileCompressionProcessing", msg, req);	// Not an error; handled after all files are processed					
			//							d_files.d_list[index-1] = d;	
										try {
											callback(err);
										}
										catch (e) {
											serverLog.serverError2(__file, __line, "fileCompressionProcessing", 
												"Recursive error in fileCompressionProcessing() callback", req, e, response);
										}
									}
									else {		
										d.file.file_data=result;
										nodeGeoSpatialServicesCommon.addStatus(__file, __line, response, "Processed gzip file [" + (index+1) + "]: " + d.file.file_name + 
											"; size: " + nodeGeoSpatialServicesCommon.fileSize(d.file.file_data.length), 
											200 /* HTTP OK */, serverLog, req,
												/*
												 * Function: 	gzipProcessingSeriesAddStatus()
												 * Parameters:	error object
												 * Description:	Add status callback
												 */												
												function gzipProcessingSeriesAddStatus(err) {
													if (err) {
														serverLog.serverLog2(__file, __line, "gzipProcessingSeriesAddStatus", 
															"WARNING: Unable to add zlib file processing status", req, err);
													}
													var end = new Date().getTime();	
													d.file.uncompress_time=(end - lstart)/1000; // in S		
													d.file.uncompress_size=d.file.file_data.length;								
													response.message+="\nFile [" + (index+1) + "]: " + d.file.file_name + "; encoding: " +
														d.file.file_encoding + "; zlib.gunzip(): " + d.file.file_data.length + 
														"; from buffer: " + d.file.file_data.length, req; 	
													callback(err);														
												}
											);  // Add file compression processing status						
									}	
								});	// End of zlib.gunzip()							
							}
						);

					}	
					else if (d.file.file_encoding === "zlib") {	
						nodeGeoSpatialServicesCommon.addStatus(__file, __line, response, "Processing zlib file [" + (index+1) + "]: " + d.file.file_name + 
							"; size: " + nodeGeoSpatialServicesCommon.fileSize(d.file.file_data.length), 
							200 /* HTTP OK */, serverLog, req, // Add file compression processing status
							/*
							 * Function: 	zlibAddStatusCallback()
							 * Parameters:	error object
							 * Description:	Add status callback
							 */							
							function zlibAddStatusCallback(err, result) {
								if (err) {								
									serverLog.serverLog2(__file, __line, "zlibAddStatusCallback", 
										"WARNING: Unable to add zlib file processing status", req, err);
								}	

								zlib.inflate(d.file.file_data, 
									/*
									 * Function: 	zlibInflateFileCallback()
									 * Parameters:	error object, result
									 * Description:	Add status callback
									 */							
									function zlibInflateFileCallback(err, result) {
										if (err) {
											msg="FAIL! File [" + (index+1) + "]: " + d.file.file_name + "; extension: " + 
												d.file.extension + "; file_encoding: " + d.file.file_encoding + " inflate exception";
											d.file.file_error=msg;	
											response.message=msg + "\n" + response.message;
											response.file_errors++;					// Increment file error count	
											serverLog.serverLog2(__file, __line, "fileCompressionProcessing", msg, req);	// Not an error; handled after all files are processed					
				//							d_files.d_list[index-1] = d;	
											try {
												callback(err);
											}
											catch (e) {
												serverLog.serverError2(__file, __line, "fileCompressionProcessing", 
													"Recursive error in fileCompressionProcessing() callback", req, e, response);
											}
										}
										else {	
											d.file.file_data=result;
											nodeGeoSpatialServicesCommon.addStatus(__file, __line, response, "Processed zlib file " + (index+1) + ": " + d.file.file_name + 
												"; size: " + nodeGeoSpatialServicesCommon.fileSize(d.file.file_data.length), 
												200 /* HTTP OK */, serverLog, req,
												/*
												 * Function: 	zlibProcessingSeriesAddStatus()
												 * Parameters:	error object
												 * Description:	Add status callback
												 */												
												function zlibProcessingSeriesAddStatus(err) {
													if (err) {
														serverLog.serverLog2(__file, __line, "zlibProcessingSeriesAddStatus", 
															"WARNING: Unable to add zlib file processing status", req, err);
													}
																						
													var end = new Date().getTime();	
													d.file.uncompress_time=(end - lstart)/1000; // in S		
													d.file.uncompress_size=d.file.file_data.length;		
													response.message+="\nFile [" + (index+1) + "]: " + d.file.file_name + "; encoding: " +
														d.file.file_encoding + "; zlib.inflate(): " + d.file.file_data.length + 
														"; from buffer: " + d.file.file_data.length, req; 
													callback(err);		
												} // End of zlibProcessingSeriesAddStatus()												
											);  // Add file compression processing status
										
										}	
									} // End of zlibInflateFileCallback()
								); // End of zlib.inflate()
							}								
						);						
					}
					else if (d.file.file_encoding === "zip") {
						nodeGeoSpatialServicesCommon.addStatus(__file, __line, response, "Processing zip file [" + (index+1) + "]: " + d.file.file_name + 
							"; size: " + nodeGeoSpatialServicesCommon.fileSize(d.file.file_data.length), 
							200 /* HTTP OK */, serverLog, req,
							/*
							 * Function: 	zipProcessingSeriesAddStatus1()
							 * Parameters:	error object
							 * Description:	Add status callback
							 */												
							function zipProcessingSeriesAddStatus1(err) {
//								console.error("zipProcessingSeriesAddStatus1");
								if (err) {
									serverLog.serverLog2(__file, __line, "zipProcessingSeriesAddStatus1", 
										"WARNING: Unable to add zip file processing status (1)", req, err);
								}

								var zip=new JSZip(d.file.file_data, {} /* Options */);
//								JSZip.loadAsync(d.file.file_data).then(function JSZipLoadAsync(zip) {
									var noZipFiles=0;
									var zipUncompressedSize=0;
//									console.error("zip files: " + zip.files.length);
									
									msg="";
									async.forEachOfSeries(zip.files /* col */, 
										function zipProcessingSeries(zipFileName, ZipIndex, seriesCallback) { // Process zip file and uncompress			
						
											var seriesCallbackFunc = function seriesCallbackFunc(e) { // Cause seriesCallback to be named
												seriesCallback(e);
											}
												
											scopeChecker(__file, __line, {
												zip: zip,
												zipFileName: zipFileName,
												index: index,
												ZipIndex: ZipIndex,
												response: response,
												serverLog: serverLog,
												d: d,
												d_files: d_files,
												d_list: d_files.d_list,
												req: req,
												ofields: ofields,
												nodeGeoSpatialServicesCommon: nodeGeoSpatialServicesCommon,
												addStatus: nodeGeoSpatialServicesCommon.addStatus
											});
																			
											noZipFiles++;	
											
	//										console.error("zipProcessingSeries" + noZipFiles);
											var fileContainedInZipFile=zip.files[ZipIndex];	
											if (fileContainedInZipFile.dir) {
												msg+="Zip file[" + noZipFiles + "]: directory: " + fileContainedInZipFile.name + "\n";
												seriesCallbackFunc();
											}
											else {
	//											console.error("fileContainedInZipFile object: " + JSON.stringify(fileContainedInZipFile, null, 4));
												var d2=createD(path.basename(fileContainedInZipFile.name), undefined /* encoding */, undefined /* mimetype */, 
													response, req); // Create D object for each zip file file

												msg+="Zip file[" + noZipFiles + "]: " + d2.file.file_name + "; relativePath: " + fileContainedInZipFile.name + 
													"; date: " + fileContainedInZipFile.date + "\n";  
												if (fileContainedInZipFile._data) {
													zipUncompressedSize+=fileContainedInZipFile._data.uncompressedSize;
													msg+="Decompress from: " + fileContainedInZipFile._data.compressedSize + " to: " +  fileContainedInZipFile._data.uncompressedSize;
													d2.file.file_size=fileContainedInZipFile._data.compressedSize;
													d2.file.file_uncompress_size=fileContainedInZipFile._data.uncompressedSize;
													d2.file.file_data=zip.files[ZipIndex].asNodeBuffer(); 
														// No longer causes Error(RangeError): Invalid string length with >255M files!!! (as expected)
														
													if (d2.file.file_data.length != d2.file.file_uncompress_size) { // Check length is as expected
														seriesCallbackFunc(new Error("Zip file[" + noZipFiles + "]: " + d2.file.file_name + "; expecting length: " + 
															 d2.file.file_uncompress_size + ";  got: " + d2.file.file_data.length));
													}
													if (d.file.extension == "json" || d.file.extension == "js") {
														d2.file.file_data.toString().replace(/\r?\n|\r/g, ""); // Remove any CRLF
													}
													msg+="; size: " + d2.file.file_data.length + " bytes\n";
												
													var end = new Date().getTime();	
													d2.file.uncompress_time=(end - lstart)/1000; // in S	
													
													new_no_files++;
//													console.error("A: response.no_files: " + response.no_files + "; new_no_files: " + new_no_files);
//														+ "\nData >>>\n" + d2.file.file_data.toString().substring(0, 200) + "\n<<<\n");
													d.no_files=response.no_files;
													d_files.d_list[response.no_files-1] = d2;
													
													nodeGeoSpatialServicesCommon.addStatus(__file, __line, response, "Expanded " + (index+1) + "." + noZipFiles + ": " + 
														d.file.file_name + "//:" + d2.file.file_name + " as file " + response.no_files+new_no_files + 
														" to list; size: " + nodeGeoSpatialServicesCommon.fileSize(d2.file.file_data.length), 
														200 /* HTTP OK */, serverLog, req,
														/*
														 * Function: 	zipProcessingSeriesAddStatus()
														 * Parameters:	error object
														 * Description:	Add status callback
														 */												
														function zipProcessingSeriesAddStatus(err) {
															if (err) {
																serverLog.serverLog2(__file, __line, "zipProcessingSeriesAddStatus", 
																	"WARNING: Unable to add zip file processing status", req, err);
															}
															
															seriesCallbackFunc(err);	
														}
													);  // Add file compression processing status								
												}
												else {
													seriesCallbackFunc(new Error("No fileContainedInZipFile._data for file in zip: " + fileContainedInZipFile.name));
												}
											}									
										}, 
										/*
										 * Function: 	zipProcessingSeriesEnd()
										 * Parameters:	error object
										 * Description:	Zip process end function
										 */								
										function zipProcessingSeriesEnd(err) {	
											if (err) {
												msg="FAIL! File [" + (index+1) + "]: " + d.file.file_name + "; extension: " + 
													d.file.extension + "; file_encoding: " + d.file.file_encoding + " unzip exception";
												d.file.file_error=msg;	
												response.message=msg + "\n" + response.message;
												response.file_errors++;					// Increment file error count	
												serverLog.serverLog2(__file, __line, "fileCompressionProcessing", msg, req);	// Not an error; handled after all files are processed		
												callback(err);
											}
											else {
//												console.error("B: response.no_files: " + response.no_files +
//														"; last new file[" + (response.no_files-1) + "]: " + d_files.d_list[response.no_files-1].file.file_name + 
//														"\nData >>>\n" + d_files.d_list[response.no_files-1].file.file_data.toString().substring(0, 200) + "\n<<<\n");
												
												nodeGeoSpatialServicesCommon.addStatus(__file, __line, response, "Processed zip file " + (index+1) + ": " + d.file.file_name + 
													"; size: " + nodeGeoSpatialServicesCommon.fileSize(d.file.file_data.length) + 
													"; added: " + new_no_files + " file(s)", 
													200 /* HTTP OK */, serverLog, req,
													/*
													 * Function: 	zipProcessingSeriesAddStatus2()
													 * Parameters:	error object
													 * Description:	Add status callback
													 */											
													function zipProcessingSeriesAddStatus2(err) {
														if (err) {
															serverLog.serverLog2(__file, __line, "zipProcessingSeriesAddStatus", 
																"WARNING: Unable to add zip file processing status (2)", req, err);
														}												
														msg+="Processed Zipfile [" + (index+1) + "]: " + d.file.file_name + "; extension: " + 
															d.file.extension + "; number of files: " + noZipFiles + "; Uncompressed size: " + zipUncompressedSize;
								//						d.file.file_error=msg;			
														response.message=msg + "\n" + response.message;	
								//						response.file_errors++;					// Increment file error count; now supported - no longer an error	
								//						serverLog.serverLog2(__file, __line, "fileCompressionProcessing", msg, req);	// Not an error; handled after all files are processed					
								//						d_files.d_list[index-1] = d;				
														callback(err);											
													} // End of zipProcessingSeriesAddStatus2()
												);  // Add file compression processing status			
											}
										}
									); // End of async zip file processing		
//								}); // End of JSZip.loadAsync()
							} // End of zipProcessingSeriesAddStatus1()											
						);  // Add file compression processing status								
					}
					else {
						nodeGeoSpatialServicesCommon.addStatus(__file, __line, response, "Processed file " + (index+1) + ": " + d.file.file_name + 
							"; size: " + nodeGeoSpatialServicesCommon.fileSize(d.file.file_data.length), 
							200 /* HTTP OK */, serverLog, req,
							/*
							 * Function: 	fileCompressionProcessingAddStatus()
							 * Parameters:	error object
							 * Description:	Add status callback
							 */	
							function fileCompressionProcessingAddStatus(err) {
								if (err) {
									serverLog.serverLog2(__file, __line, "fileCompressionProcessingAddStatus", 
										"WARNING: Unable to add zip file processing status", req, err);
								}							
								callback(err);														
							});  // Add file compression processing status		
					}
				} // End of fileCompressionProcessing()

				/*
				 * Function: 	urlSpecific()
				 * Parameters:	ofields object, d_files flist list object, internal response object,
				 *				HTTP request object, HTTP response object, shapefile_options, topojson_options, stderr object
				 * Description:	Call URL specific processing
				 */					
				var urlSpecific = function urlSpecific(ofields, d_files, response, req, res, shapefile_options, topojson_options, stderr) {	
					// Run url specific code	
			
					scopeChecker(__file, __line, {
						response: response,
						serverLog: serverLog,
						d_files: d_files,
						req: req,
						httpErrorResponse: httpErrorResponse
					});
				
//					response.fields=ofields; // Bring into scope early
					if (req.url == '/geo2TopoJSON') {
						
						if (!geo2TopoJSON.geo2TopoJSONFiles(d_files, ofields, topojson_options, stderr, response, serverLog, req)) {
							return;
						}
					}			
					else if (req.url == '/shpConvert') { // Note which files and extensions are present, 
																						// generate serial if required, save 
						if (!shpConvert.shpConvert(ofields, d_files, response, req, res, shapefile_options)) {
							return;
						}
					}
				} // End of urlSpecific()	
						
				scopeChecker(__file, __line, {
					response: response,
					serverLog: serverLog,
					d_files: d_files,
					d_list: d_files.d_list,
					req: req,
					ofields: ofields
				});
					
				try {
					var msg="";

					if (!response.fields["diagnosticFileDir"]) {
						nodeGeoSpatialServicesCommon.setupDiagnostics(__file, __line, req, ofields, response, serverLog, httpErrorResponse);
					}
					nodeGeoSpatialServicesCommon.addStatus(__file, __line, response, "All form data and fields loaded; running completion processing", 
						200 /* HTTP OK */, serverLog, req);  // Add onBusboyFinish status
					
					// Set any required parameters not yet set
					if (!ofields["quantization"]) {
						   topojson_options["pre-quantization"] = simplifyGeoJSON.getQuantization(11); // For zoomlevel 11
						   topojson_options["post-quantization"] = simplifyGeoJSON.getQuantization(11); // For zoomlevel 11
						   if (!ofields["quantization"]) {
								ofields["quantization"]=topojson_options["pre-quantization"];
						   }
						   response.message+="\nDefault initial quantization: " + topojson_options["pre-quantization"];
					}
/*
 * Set zoomlevel
 *
 * Zoomlevel		Quantization
 * ---------		------------
 *
 * <=6				1,500
 * 7				3,000
 * 8				5,000
 * 9				10,000
 * 10				100,000
 * 11				1,000,0000
 */
					if (!ofields["max_zoomlevel"] && topojson_options["pre-quantization"] >= 1e6) { // For zoomlevel 11
						ofields["max_zoomlevel"]=11;
						   response.message+="\nInitial max zooomlevel: " + ofields["max_zoomlevel"] + "; quantization: " + topojson_options["pre-quantization"];
					}
					else if (!ofields["max_zoomlevel"] && topojson_options["pre-quantization"] >= 1e5) { // For zoomlevel 10
						ofields["max_zoomlevel"]=10;
						   response.message+="\nInitial max zooomlevel: " + ofields["max_zoomlevel"] + "; quantization: " + topojson_options["pre-quantization"];
					}
					else if (!ofields["max_zoomlevel"] && topojson_options["pre-quantization"] < 1e5) { // For zoomlevel 9
						ofields["max_zoomlevel"]=9;
						   response.message+="\nInitial max zooomlevel: " + ofields["max_zoomlevel"] + "; quantization: " + topojson_options["pre-quantization"];
					}					
					else if (!ofields["max_zoomlevel"]) {
						ofields["max_zoomlevel"]=11;
						   response.message+="\nInitial default max zooomlevel: " + ofields["max_zoomlevel"] + "; quantization: " + topojson_options["pre-quantization"];
					}
					
					if (!ofields["min_zoomlevel"]) {
						ofields["min_zoomlevel"]=6;
						   response.message+="\nInitial default min zooomlevel: " + ofields["min_zoomlevel"];
					}
					
					if (ofields["min_zoomlevel"] > ofields["max_zoomlevel"]) {
						response.message+="\nFAIL! min_zoomlevel: " + min_zoomlevel + " > max_zoomlevel: " + max_zoomlevel;
						response.field_errors++;	
					}
					
					ofields["topojson_options"]=topojson_options;
					
					// Check for errors before more file processing
					if (response.file_errors > 0) { 
						msg="FAIL! File errors detected: " + response.file_errors;						
						response.message = msg + "\n" + response.message;	
						httpErrorResponse.httpErrorResponse(__file, __line, "onBusboyFinish", 
							serverLog, 500, req, res, msg, undefined, response);							
						return;						
					}	
					else if (response.field_errors > 0) { 
						msg="FAIL! Field errors detected: " + response.file_errors;						
						response.message = msg + "\n" + response.message;
						httpErrorResponse.httpErrorResponse(__file, __line, "onBusboyFinish", 
							serverLog, 500, req, res, msg, undefined, response);							
						return;						
					}
					
					
					if (req.url == '/geo2TopoJSON' || req.url == '/shpConvert') {
						const async = require('async');
						
						response.no_files=d_files.d_list.length; // Add number of files process to response
						response.fields=ofields;				 // Add return fields	

						if (ofields["batchMode"] === "true") {		 // Batch mode - return now	
							response.message+="\nBatch mode: " + ofields["batchMode"] + "; returning just before file compression processing.";						
							nodeGeoSpatialServicesCommon.responseProcessing(req, res, response, serverLog, httpErrorResponse, ofields, undefined /* optional callback */);
						}
						else {	
							response.message+="\nBatch mode: " + ofields["batchMode"] + "; continuing.";						
						}
					
						async.forEachOfSeries(d_files.d_list /* col */, 
							function fileCompressionProcessingSeries(d, index, seriesCallback) { // Process file list for compressed file and uncompress them				
			
								var seriesCallbackFunc = function seriesCallbackFunc(e) { // Cause seriesCallback to be named
									seriesCallback(e);
								}
									
								scopeChecker(__file, __line, {
									d: d,
									index: index,
									response: response,
									serverLog: serverLog,
									d_files: d_files,
									d_list: d_files.d_list,
									req: req,
									ofields: ofields
								});
				
								fileCompressionProcessing(d, index, response, serverLog, d_files, req, seriesCallbackFunc); 
									// Call file processing; handle zlib, gz and zip files								
							}, 
							function fileCompressionProcessingSeriesEnd(err) {																	/* Callback at end */
								var msg;

								scopeChecker(__file, __line, {
									response: response,
									serverLog: serverLog,
									d_files: d_files,
									d_list: d_files.d_list,
									req: req,
									ofields: ofields,
									nodeGeoSpatialServicesCommon: nodeGeoSpatialServicesCommon,
									responseProcessing: nodeGeoSpatialServicesCommon.responseProcessing
								});
								if (err) { // Handle errors
									msg="Error in async series end function";						
									response.message = msg + "\n" + response.message;
									response.no_files=0;					// Add number of files process to response
									response.file_errors++;					// Increment file error count
									httpErrorResponse.httpErrorResponse(__file, __line, "fileCompressionProcessingSeriesEnd", 
										serverLog, 500, req, res, msg, err, response);											
								}
								else { // Async forEachOfSeries loop complete
									
									for (var i = 0; i < response.no_files; i++) { // Process file list for errors	
										var d3=d_files.d_list[i];
//										console.error("C[" + i + "]: response.no_files: " + response.no_files + "; d3.no_files: " + d3.no_files + 
//											"; " + d3.file.file_name + 
//											"\nData >>>\n" + d3.file.file_data.toString().substring(0, 200) + "\n<<<\n");
											
										if (!d3) { // File could not be processed, httpErrorResponse.httpErrorResponse() already processed
											msg="FAIL! File [" + (i+1) + "/" + response.no_files + "]: entry not found, no file list" + 
												"; httpErrorResponse.httpErrorResponse() NOT already processed";						
											response.message = msg + "\n" + response.message;
											response.no_files=0;					// Add number of files process to response
											response.file_errors++;					// Increment file error count
											httpErrorResponse.httpErrorResponse(__file, __line, "fileCompressionProcessingSeriesEnd", 
												serverLog, 500, req, res, msg, undefined, response);				
											return;							
										}
										else if (!d3.file) {
											msg="FAIL! File [" + (i+1) + "/" + response.no_files + "]: object not found in list" + 
												"\n";
											response.message = msg + "\n" + response.message;
											response.file_errors++;					// Increment file error count	
											httpErrorResponse.httpErrorResponse(__file, __line, "fileCompressionProcessingSeriesEnd", 
												serverLog, 500, req, res, msg, undefined, response);							
											return;			
										}
										else if (d3.file.file_error) {
											msg="FAIL! File [" + (i+1) + "/" + response.no_files + "]: " + d3.file.file_name + "; extension: " + 
												d3.file.extension + "; error >>>\n" + d3.file.file_error + "\n<<<";
											response.message = msg + "\n" + response.message;
											response.file_errors++;					// Increment file error count	
											httpErrorResponse.httpErrorResponse(__file, __line, "fileCompressionProcessingSeriesEnd", 
												serverLog, 500, req, res, msg, undefined, response);							
											return;							
										}
										else if (d3.file.file_data.length == 0) {
											msg="FAIL! File [" + (i+1) + "/" + response.no_files + "]: " + d3.file.file_name + "; extension: " + 
												d3.file.extension + "; file size is zero" + 
												"\n";
											response.message = msg + "\n" + response.message;
											response.file_errors++;					// Increment file error count	
											httpErrorResponse.httpErrorResponse(__file, __line, "fileCompressionProcessingSeriesEnd", 
												serverLog, 500, req, res, msg, undefined, response);							
											return;
										}	
									} // End of for loop	

									if (response.no_files == 0) { 
										msg="FAIL! No files attached\n";						
										response.message = msg + "\n" + response.message;
										response.file_errors++;					// Increment file error count	
										httpErrorResponse.httpErrorResponse(__file, __line, "fileCompressionProcessingSeriesEnd", 
											serverLog, 500, req, res, msg, undefined, response);							
										return;						
									}
									else if (!ofields["my_reference"]) {
										msg+="[No my_reference] Processed: " + response.no_files + " files";
									}
									else {
										msg+="[my_reference: " + ofields["my_reference"] + "] Processed: " + response.no_files + " files";
									}		

									urlSpecific(ofields, d_files, response, req, res, shapefile_options, topojson_options, stderr);
										// Run url specific code

									// Final processing										
									if (req.url == '/shpConvert') { // Processed by shpConvertCheckFiles() - uses async
									}
									else if (req.url == '/geo2TopoJSON') {	
										nodeGeoSpatialServicesCommon.responseProcessing(req, res, response, serverLog, httpErrorResponse, ofields, undefined /* optional callback */);
									}										
								}
						}); // End of async file processing loop					
					
					}
					else {
						var msg="ERROR! " + req.url + " service not not yet supported";
		
						if (d_files && _files.d_list) {
							response.no_files=d_files.d_list.length; // Add number of files process to response
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
					if (response) {
						httpErrorResponse.httpErrorResponse(__file, __line, "req.busboy.on('finish')", 
							serverLog, 500, req, res, 'Caught unexpected error (possibly async)', e, response);
					}
					else {
						var l_response = {                 // Set output response    
							error: e.message,
							no_files: 0,    
							field_errors: 0,
							file_errors: 0,
							file_list: [],
							message: '',  
							diagnostic: '',
							fields: [] 
						};
						httpErrorResponse.httpErrorResponse(__file, __line, "req.busboy.on('finish')", 
							serverLog, 500, req, res, 'Caught unexpected error (possibly async)', e, l_response /* Nominal response */);
					}
					return;
				}
			} // End of req.busboy.on('finish')
			);

			req.pipe(req.busboy); // Pipe request stream to busboy form data handler
			  
		} // End of post method	
		else if (req.method == 'GET' && (req.params["shpConvert"] == 'getShpConvertStatus')) { // Get method: getShpConvertStatus		

			scopeChecker(__file, __line, {
				serverLog: serverLog,
				httpErrorResponse: httpErrorResponse,
				response: response,
				req: req,
				res: res
			});
	
			nodeGeoSpatialServicesCommon.getStatus(response, req, res, serverLog, httpErrorResponse);				
		} // End of get method: getShpConvertStatus
		else if (req.method == 'GET' && (req.params["shpConvert"] == 'getShpConvertTopoJSON')) { // Get method: getShpConvertTopoJSON		

			scopeChecker(__file, __line, {
				serverLog: serverLog,
				httpErrorResponse: httpErrorResponse,
				response: response,
				req: req,
				res: res
			});
	
			nodeGeoSpatialServicesCommon.getShpConvertTopoJSON(response, req, res, serverLog, httpErrorResponse);				
		} // End of get method: getShpConvertTopoJSON		
		else if (req.method == 'GET' && (req.params["shpConvert"] == 'shpConvertGetConfig.xml')) { // Get method: shpConvertGetConfig.xml		

			scopeChecker(__file, __line, {
				serverLog: serverLog,
				httpErrorResponse: httpErrorResponse,
				response: response,
				req: req,
				res: res
			});
	
			nodeGeoSpatialServicesCommon.shpConvertGetConfig(response, req, res, serverLog, httpErrorResponse);				
		} // End of get method: shpConvertGetConfig.xml		
		else if (req.method == 'GET' && (req.params["shpConvert"] == 'shpConvertGetResults.zip')) { // Get method: shpConvertGetResults.zip		

			scopeChecker(__file, __line, {
				serverLog: serverLog,
				httpErrorResponse: httpErrorResponse,
				response: response,
				req: req,
				res: res
			});
	
			nodeGeoSpatialServicesCommon.shpConvertGetResults(response, req, res, serverLog, httpErrorResponse);				
		} // End of get method: shpConvertGetResults.zip		
		else if (req.method == 'GET' && (req.params["shpConvert"] == 'getGeographies')) { // Get method: getGeographies		

			scopeChecker(__file, __line, {
				serverLog: serverLog,
				httpErrorResponse: httpErrorResponse,
				response: response,
				req: req,
				res: res
			});
	
			tileViewer.getGeographies(response, req, res, serverLog, httpErrorResponse);				
		} // End of get method: getGeographies
		else if (req.method == 'GET' && (req.params["shpConvert"] == 'getMapTile')) { // Get method: getMapTile		

			scopeChecker(__file, __line, {
				serverLog: serverLog,
				httpErrorResponse: httpErrorResponse,
				response: response,
				req: req,
				res: res
			});
	
			tileViewer.getMapTile(response, req, res, serverLog, httpErrorResponse);				
		} // End of get method: getMapTile		
		else {								// All other methods are errors
			var msg="ERROR! "+ req.method + " Requests not allowed; please see: " + 
				"https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifNodeServices/readme.md Node Web Services API for RIF 4.0 documentation for help";
			httpErrorResponse.httpErrorResponse(__file, __line, "exports.convert", 
				serverLog, 405, req, res, msg);		
			return;		  
		}
		
	} catch (e) {                          // Catch syntax errors
		var l_response = {                 // Set output response    
			error: e.message,
			no_files: 0,    
			field_errors: 0,
			file_errors: 0,
			file_list: [],
			message: '',  
			diagnostic: '',
			fields: [] 
		}	
		var msg="General processing ERROR!";				  
		httpErrorResponse.httpErrorResponse(__file, __line, "exports.convert catch()", serverLog, 500, req, res, msg, e, l_response /* Nominal response */);		
		return;
	}
	  
};