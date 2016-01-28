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
var zlib = require('zlib'),
    fs = require('fs'),
    os = require('os');
var l_file_name;
var file_encoding;
var extension;
	
exports.convert = function(req, res) {

//  req.setEncoding('utf-8'); // This corrupts the data stream with binary data
    res.setHeader("Content-Type", "text/plain");
	
    req.busboy.on('file', function(fieldname, file, filename, encoding, mimetype) {
		file_encoding=req.get('Content-Encoding');
		extension = filename.split('.').pop();
		if (!file_encoding) {
			if (extension === "gz") {
					file_encoding="gzip";
			}
			else if (extension === "zip") {
					file_encoding="zip";
			}
		}
			
		l_file_name = os.tmpdir()  + "/" + filename;
		
		var chunks = [];
		
		file.on('data', function(chunk) {
			chunks.push(chunk);
		});
		file.on('end', function() {
			data=Buffer.concat(chunks);
		});
    });

    req.busboy.on('finish', function() {

			var buf;
			console.log('Gzipped binary stream: ' + data.toString('hex').substring(0, 132));

			if (file_encoding === "gzip") {	
				buf=zlib.gunzipSync(data);
			}
			else if (file_encoding === "zip" || file_encoding === "zlib") {	
				buf=zlib.inflateSync(data);
			}
			else {
				return res.status(500).send({
					message: "FAIL: : " + l_file_name + "; extension: " + extension + "; file_encoding: " + file_encoding
				});				
			}
			var jsonData = JSON.parse(buf.toString());
							
	        return res.status(200).send({
				message: "OK: : " + l_file_name + "; extension: " + extension + "; file_encoding: " + file_encoding +
					";\ndata:\n" + JSON.stringify(jsonData, null, 4).substring(0, 132)
			});

    });

    req.pipe(req.busboy);
};