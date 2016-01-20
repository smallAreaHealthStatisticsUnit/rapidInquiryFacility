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
    stderrHook = require('./stderrHook'),
    fs = require('fs'),
    setStatusCode = function(res, code ) {
        res.statusCode = code;
    },
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
        this.fName = '';
        this.topology = '';
        this.output = '';		
		this.response = '';
        this.withinLimit = true;
        this.upper_limit = 1e8;
		
        return this; 
     };

/*
 * Function: 	exports.convert()
 * Parameters:	Express HTTP request object, response object
 * Description:	Express web server handler function for topoJSON conversion
 */
exports.convert = function(req, res) {

    req.setEncoding('utf-8');
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

// Data processor			
            stream.on('data', function(data) {
			    d.fullData += data;  
//			    if (d.fullData != '') {
//					console.error("toTopoJSON(): read: " + d.fullData.length);
//			    }
			    if (d.fullData.length > d.upper_limit) { // Max geojs allowed upper_limit
					d.withinLimit = false;  
					try { 
						console.log("toTopoJSON(): Stopping file: " + d.fName + " upload...");
					} catch (e) { 
						res.end("toTopoJSON(): File: " + d.fName + " upload stopped."); 
					};     
			    };
			});

// EOF processor 
            stream.on('end', function() {
//			     d.fullData = d.fullData.replace(/(\r\n|\n|\r)/gm,""); CRLF=> CR
                 if (d.fName != '' && d.withinLimit) {
                    try {
						jsonData = JSON.parse(d.fullData); // Parse file stream data to JSON
						// Re-route topoJSON stderr to stderr.str
						stderr.disable();
						d.topology = topojson.topology({   // Convert geoJSON to topoJSON
							collection: jsonData
							}, options);				
						stderr.enable(); 				   // Re-enable stderr
						
						var topojson_stderr=stderr.str();
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
					    console.error("toTopoJSON() [" + d.response.fields["my_reference"] + 
							"]: file: " + d.fName + "; size: " + d.fullData.length  + "/" + d.response.fields["length"] + "\r\nData:\r\n" + 
						d.output.substring(0, 132) + "\r\n");			
                                                           // Write trace to strerr 						
                    } catch (e) {                            // Catch conversion errors
                        console.error("ERROR! toTopoJSON() [" + d.response.fields["my_reference"] + 
							"]: Your input file: " + 
							d.fName + "; size: " + d.fullData.length + "/" + d.response.fields["length"] + ": does not seem to be valid: \n\n" + 
							e + "\r\nTruncated data:\r\n" + 
							d.fullData.substring(0, 132) + "...\r\n");					  
						res.status(500);					  
						res.write("ERROR! toTopoJSON() [" + d.response.fields["my_reference"] + 
							"]: Your input file: " + 
							d.fName + "; size: " + d.fullData.length + "/" + d.response.fields["length"] + ": does not seem to be valid: \n\n" + 
							e + "\r\Truncated data:\r\n" + 
							d.fullData.substring(0, 132) + "...\r\n");
                        return;
					}; 
                };
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
          		  
// End of request - complete reponse		  
        req.busboy.on('finish', function() {
            if (d.withinLimit){
                res.end();
            }
        });

        req.pipe(req.busboy);
          
    } // End of post method
	else {
		var msg="toTopojson.js: GET Requests not allowed; please see: " + 
			"https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifNodeServices/readme.md Node Web Services API for RIF 4.0 documentation for help";
		console.error(msg);
        setStatusCode( res, 405  );
        res.end(msg);   };      
};