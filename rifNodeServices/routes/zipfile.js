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
// Rapid Enquiry Facility (RIF) - compression testing webservice
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
// Usage: tests/request4.js
//
// Uses: zlib, fs, os
//
//
 
//  Globals
const zlib = require('zlib'),
    fs = require('fs'),
    os = require('os');
	
exports.convert = function(req, res) {
	
// Post method	
    if (req.method == 'POST') {
		
		var response = {                     // Set output response   
			message: "UNK",
			files: 0,
			file_list: []
		};

	//  req.setEncoding('utf-8'); // This corrupts the data stream with binary data
		res.setHeader("Content-Type", "text/plain");
		
		req.busboy.on('file', function(fieldname, stream, filename, encoding, mimetype) {
			var file = { // File return data type
				file_name: filename,
				temp_file_name: "",
				file_encoding: "",	
				extension: "",
				jsonData: ""
			}
			var chunks = [];
			var buf;
			
			response.files++;	// Increment file counter	

			// Determine file enconding from content or extension
			file.file_encoding=req.get('Content-Encoding');
			file.extension = filename.split('.').pop();
			if (!file.file_encoding) {
				if (file.extension === "gz") {
						file.file_encoding="gzip";
				}
				else if (file.extension === "zip") {
						file.file_encoding="zip";
				}
				else if (file.extension === "lz77") {
						file.file_encoding="zlib";
				}
			}
				
			file.temp_file_name = os.tmpdir()  + "/" + filename;
			
			// Streeam handlers
			stream.on('data', function(chunk) {
				chunks.push(chunk);
			});
			stream.on('end', function() {
			
				data=Buffer.concat(chunks);
				
				if (file.file_encoding === "gzip") {	
					buf=zlib.gunzipSync(data);
				}
				else if (file.file_encoding === "zlib") {	
					buf=zlib.inflateSync(data);
				}
				else {
					return res.status(500).send({
						message: "FAIL[" + response.files + "]: " + file.file_name + "; extension: " + 
							file.extension + "; file_encoding: " + file.file_encoding
					});				
				}

				console.error("Gzipped binary stream[" + response.files + "]: " + file.file_name + 
					"; " + data.length + " => " + buf.length + " >>>\n" + 
					data.toString('hex').substring(0, 80));
					
				// Add file to response
				file.jsonData = JSON.parse(buf.toString());
				response.file_list[response.files-1]=file;
			});

		});

		// Set response message; JSON it; write response
		req.busboy.on('finish', function() {
			var message="OK: " + response.files + " file(s) processed";
			var output;
			
			console.error(message);
			response.message=message			
			output = JSON.stringify(response);// Convert output response to JSON 

			res.write(output);                    // Write output  
			res.end();		
		});

		req.pipe(req.busboy); // Pipe request stream to busboy form data handler
	}
	else {
		var msg="ERROR! zipfile.js: GET Requests not allowed; please see: " + 
			"https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/blob/master/rifNodeServices/readme.md Node Web Services API for RIF 4.0 documentation for help";
		console.error(msg);
        res.status(405);				  
		res.write(msg);
		res.end();		
		return;		  
	}	
}