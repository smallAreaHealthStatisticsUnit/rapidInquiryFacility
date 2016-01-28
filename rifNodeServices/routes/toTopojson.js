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
		
        return this; 
     };

function _process_json(d1, ofields, options, stderr, req, res, encoding, response) {
	var d=d1;
	
    try {	
		console.error("XXXX[" + ofields["my_reference"] + "] 3: " + d.file.file_data.length); 						
		d.file.jsonData = JSON.parse(d.file.file_data.toString()); // Parse file stream data to JSON
		console.error("YYYY[" + ofields["my_reference"] + "] 3: " +  JSON.stringify(d.file.jsonData, null, 4).length); 						

		// Re-route topoJSON stderr to stderr.str
		stderr.disable();
		d.file.topojson = topojson.topology({   // Convert geoJSON to topoJSON
			collection: d.file.jsonData
			}, options);				
		stderr.enable(); 				   // Re-enable stderr
		
		d.file.topojson_stderr=stderr.str();  // Get stderr as a string
		console.error(d.file.topojson_stderr);

		console.error("TopoJson stderr[" + d.file.file_name + ":" + d.no_files + "]: \n"  + d.file.topojson_stderr);	
		stderr.restore();                  // Restore normal stderr functionality 

// Add file to response
		response.no_files++;
		response.file_list[response.no_files-1] = {
			file_name: d.file.file_name,
			topojson: d.file.topojson,
			topojson_stderr: d.file.topojson_stderr
		};		
		if (d.file.topojson_stderr.length > 0) {  // Add topoJSON stderr to message		
			response.message = response.message + "[" + d.file.file_name + ":" + d.no_files + "]:\n" + 
				d.file.topojson_stderr;
		}
			
//		console.error("toTopoJSON._process_json() [" + d.response.fields["my_reference"] + 
//			"; " + req.url + "; " + req.ip + "]: " + 
//			"file: " + d.file.file_name + "; size: " + 
//			d.file.file_data.length + "/" + d.response.fields["length"] + "\r\nData:\r\n" + 
//			d.output.substring(0, 132) + "\r\n");			
										   // Write trace to strerr 
															   
		return d;								   
	} catch (e) {                            // Catch conversion errors
		var msg;

			msg="EXCEPTION! toTopoJSON._process_json() [UNK; " + req.url + "; " + req.ip + "]: " +  
				"Your input file: " + 
				d.file.file_name + "; size: " + d.file.file_data.length + 
				": does not seem to be valid: \n\n" + 
				e /* + 
				"\r\nTruncated data:\r\n" + 
				d.file.file_data.toString('hex').substring(0, 132) + "...\r\n" */ + 
				'; Content-Transfer-Encoding: ' + encoding + 
				'; Content-Encoding: ' + req.get('Content-Encoding');
				
		console.error(msg + "\n" + e.stack);					  
		res.status(500);					  
		res.write(msg);
		res.end();		
		return;
	}; 	
}

/*
 * Function: 	exports.convert()
 * Parameters:	Express HTTP request object, response object
 * Description:	Express web server handler function for topoJSON conversion
 */
exports.convert = function(req, res) {

//  req.setEncoding('utf-8'); // This corrupts the data stream with binary data
//	req.setEncoding('binary'); // So does this! Leave it alone - it gets it right!

    res.setHeader("Content-Type", "text/plain");
	
// Add stderr hook to capture debug output from topoJSON	
	var stderr = stderrHook.stderrHook(function(output, obj) { 
		output.str += obj.str;
	});
	
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
	
// Response	
		var response = {                 // Set output response    
			no_files: 0,
			file_list: [],
			message: 'OK',               
			fields: [] 
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

			d.no_files++;	// Increment file counter
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
			console.error(d.file.extension + "[" + d.no_files + "]: headers[" + d.file.filename + "]: " + JSON.stringify(req.headers, null, 4));
		
// Data processor			
            stream.on('data', function(data) {
				if  (d.file.chunks.length == '') {
				}
				if (d.file.file_encoding === "zip" || d.file.file_encoding === "gzip" || d.file.file_encoding === "zlib") {					
					console.error(d.file.file_encoding + "; 1: " + data.toString('hex').substring(0, 132)); 
				}	
//				else {
//					console.error("toTopoJSON(): read: " + d.file.file_data.length);
//				}
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
				console.error('END');
//			     d.file.file_data = d.file.file_data.replace(/(\r\n|\n|\r)/gm,""); CRLF=> CR
//                 if (d.file.file_name != '' && d.withinLimit) {	
	
						var buf=Buffer.concat(d.file.chunks);
						
						d.file.file_data="";
						if (d.file.file_encoding === "gzip") {
							d.file.file_data=zlib.gunzipSync(buf)							
							console.error(d.file.file_encoding + ": [" + ofields["my_reference"] + "] zlib.gunzip(): " + d.file.file_data.length + 
								"; from buf: " + buf.length); 
							if (d.file.file_data.length > 0) {
								d=_process_json(d, ofields, options, stderr, req, res, encoding, response);				
							}	
						}	
						else if (d.file.file_encoding === "zlib") {	
							d.file.file_data=zlib.inflateSync(buf)							
							console.error(d.file.file_encoding + ": [" + ofields["my_reference"] + "] zlib.inflate(): " + d.file.file_data.length + 
								"; from buf: " + buf.length); 
							if (d.file.file_data.length > 0) {
								d=_process_json(d, ofields, options, stderr, req, res, encoding, response);				
							}	
						}
						else if (d.file.file_encoding === "zip") {
							return res.status(500).send({
								message: "FAIL[" + d.response.files + "]: " + d.file.file_name + "; extension: " + 
									file.extension + "; file_encoding: " + d.file.file_encoding
							});								
						}
						else {
							d.file.file_data=buf;
							console.error(d.file.file_encoding + ": [" + ofields["my_reference"] + "] 4: " + d.file.file_data.length); 							
							d=_process_json(d, ofields, options, stderr, req, res, encoding, response);								
						}
					
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
			else {
				ofields[fieldname]=val;				
			}	
			console.error('Field: ' + fieldname + '[' + val + ']; ' + text);
         }); // End of field processing function
          		  
// End of request - complete response		  
        req.busboy.on('finish', function() {
			console.error('FINISH');
					
			response.fields=ofields;				   // Add return fields		
			var output = JSON.stringify(response);// Convert output response to JSON 
			
// Need to test res was not finished by an expection to avoid "write after end" errors			
			res.write(output);                  // Write output  
			res.end();
        });

        req.pipe(req.busboy); // Pipe request stream to busboy form data handler
          
    } // End of post method
	else {
		var msg="ERROR! toTopojson.js: GET Requests not allowed; please see: " + 
			"https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifNodeServices/readme.md Node Web Services API for RIF 4.0 documentation for help";
		console.error(msg);
        res.status(405);				  
		res.write(msg);
		res.end();		
		return;		  
	}
  
};