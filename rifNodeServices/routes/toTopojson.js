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
    fs = require('fs'),
//    setStatusCode = function(res, code ) {
//        res.statusCode = code;
//    },
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
        this.fullData = '';
		this.chunks = [],
        this.fName = '';
        this.topology = '';
        this.output = '';		
		this.response = '';
        this.withinLimit = true;
        this.upper_limit = 1e8;
		
        return this; 
     };

function _process_json(d1, ofields, options, stderr, req, res, encoding) {
	var d=d1;
	
    try {	
		console.error("XXXX[" + ofields["my_reference"] + "] 3: " + d.fullData.length); 						
		jsonData = JSON.parse(d.fullData.toString()); // Parse file stream data to JSON

		// Re-route topoJSON stderr to stderr.str
		stderr.disable();
		d.topology = topojson.topology({   // Convert geoJSON to topoJSON
			collection: jsonData
			}, options);				
		stderr.enable(); 				   // Re-enable stderr
		
		var topojson_stderr=stderr.str();  // Get stderr as a string
		console.error(topojson_stderr);
		d.response = {                     // Set output response    
			message: 'OK',                 // Parse and convert is OK  
			topojson: d.topology,          // Add topoJSON 
			fields: ofields				   // Add return fields
		};

		console.error('TopoJson stderr(' + topojson_stderr.length + '): \n'  + topojson_stderr);	
		if (topojson_stderr.length > 0) {  // Add topoJSON stderr to message		
			d.response.message = 'OK:\n' + topojson_stderr;
		}
		stderr.restore();                  // Restore normal stderr functionality 
		
		d.output = JSON.stringify(d.response);// Convert output response to JSON 
		res.write(d.output);                  // Write output  
		res.end();
		
		console.error("toTopoJSON._process_json() [" + d.response.fields["my_reference"] + 
			"; " + req.url + "; " + req.ip + "]: " + 
			"file: " + d.fName + "; size: " + d.fullData.length + "/" + d.response.fields["length"] + "\r\nData:\r\n" + 
		d.output.substring(0, 132) + "\r\n");			
										   // Write trace to strerr 
															   
		return d;								   
	} catch (e) {                            // Catch conversion errors
		var msg;
		
		if (typeof d.response.fields !== 'undefined' && d.response.fields) { // Fields are defined
			msg="EXCEPTION! toTopoJSON._process_json(): [" + d.response.fields["my_reference"] + 
				"; " + req.url + "; " + req.ip + "]:" + "]: " + 
				"Your input file: " + 
				d.fName + "; size: " + d.fullData.length + "/" + d.response.fields["length"] + 
				": does not seem to be valid: \n\n" + 
				e + 
				"\r\nTruncated data:\r\n" + 
				d.fullData.substring(0, 132) + "...\r\n";
		}
		else {	
			msg="EXCEPTION! toTopoJSON._process_json() [UNK; " + req.url + "; " + req.ip + "]: " +  
				"Your input file: " + 
				d.fName + "; size: " + d.fullData.length + 
				": does not seem to be valid: \n\n" + 
				e + 
				"\r\nTruncated data:\r\n" + 
				d.fullData.toString('hex').substring(0, 132) + "...\r\n" + 
				'; Content-Transfer-Encoding: ' + encoding + 
				'; Content-Encoding: ' + req.get('Content-Encoding');
		}					
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

    var d = new TempData(); // This is local to the post requests; the field processing cannot see it
	
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
            d.fName = filename;
			var file_encoding=req.get('Content-Encoding');
			var extension = filename.split('.').pop();
			if (!file_encoding) {
				if (extension === "gz") {
						file_encoding="gzip";
				}
				else if (extension === "zip") {
						file_encoding="zip";
				}
			}
			console.error(extension + ": headers[" + filename + "]: " + JSON.stringify(req.headers, null, 4));
		
// Data processor			
            stream.on('data', function(data) {
				/*

KKKK: 1f8b08084b1599560003746573745f365f73616873755f345f6c6576656c345f305f305f302e6a7300bcfd4d8f364d729e07ff15e359d900df41e57726778621af64
				
1f efbfbd08084b15efbfbd560003746573745f365f73616873755f345f6c6576656c34

0000000 8b1f 0808 154b 5699 0300 6574 7473 365f
0000020 735f 6861 7573 345f 6c5f 7665 6c65 5f34
0000040 5f30 5f30 2e30 736a bc00 4dfd 368f 724d
0000060 079e 15ff 59e3 00d9 41df 77e5 7726 2186
0000100 64af 2bc0 0c6f 1883 8f49 0204 0e43 1e31

0000000 037 213  \b  \b   K 025 231   V  \0 003   t   e   s   t   _   6
0000020   _   s   a   h   s   u   _   4   _   l   e   v   e   l   4   _
0000040   0   _   0   _   0   .   j   s  \0 274 375   M 217   6   M   r
0000060 236  \a 377 025 343   Y 331  \0 337   A 345   w   &   w 206   !
0000100 257   d 300   +   o  \f 203 030   I 217 004 002   C 016   1 036
0000120   .  \b 201 377 375 215   #   +   " 262 250 273 343 352 356 253
0000140  \t 223   "   4 323   q   W   ]   U   Y 231 361   y 306 031 377
0000160 375 267 277 376 313   ? 375 376 333 337 376 366 277 377 376 307
0000200 277 376 363   _   ~ 377 337 376 374 247   ? 375 376 237 377 372
0000220 367 177 376 307 337 376 346 267 377   z 377 355 377 375 355   o
 */
				if ((file_encoding === "zip") && (d.chunks.length == '')) {					
					console.error("ZIP; 1: " + data.toString('hex').substring(0, 132)); 
				}
				else if ((file_encoding === "gzip") && (d.chunks.length == '')) {					
					console.error("GZIP; 1: " + data.toString('hex').substring(0, 132)); 
				}
				else if ((file_encoding === "zlib") && (d.chunks.length == '')) {					
					console.error("ZLIB; 1: " + data.toString('hex').substring(0, 132)); 
				}				
				d.chunks.push(data);  
			
//			    if (d.fullData != '') {
//					console.error("toTopoJSON(): read: " + d.fullData.length);
//			    }
			    if (d.fullData.length > d.upper_limit) { // Max geojs allowed upper_limit
					d.withinLimit = false;  
					try { 
						console.log("toTopoJSON(): Stopping file: " + d.fName + " upload...");
					} catch (e) { 
						var msg="EXCEPTION! toTopoJSON.js: File: " + d.fName + " upload stopped: " + e; 
						                        console.error(msg);					  
						res.status(500);					  
						res.write(msg);
						res.end();						
                        return;
					};     
			    };
			});

// EOF processor 
            stream.on('end', function() {
				console.error('END');
//			     d.fullData = d.fullData.replace(/(\r\n|\n|\r)/gm,""); CRLF=> CR
//                 if (d.fName != '' && d.withinLimit) {

						var zmsg="YYYY";	
	
						var buf=Buffer.concat(d.chunks);
						if (file_encoding === "zip" || file_encoding === "gzip" || file_encoding === "zlib") {	
							d.fullData=zlib.gunzipSync(buf)							
							console.error(file_encoding + ": [" + ofields["my_reference"] + "] 2: " + d.fullData.length + 
								"; buf: " + buf.length); 
							if (d.fullData.length > 0) {
								d=_process_json(d, ofields, options, stderr, req, res, encoding);				
							}	
						}
						else {
							d.fullData=Buffer.concat(d.chunks);
							console.error(file_encoding + ": [" + ofields["my_reference"] + "] 4: " + d.fullData.length); 							
							d=_process_json(d, ofields, options, stderr, req, res, encoding);								
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