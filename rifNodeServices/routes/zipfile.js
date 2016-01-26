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
var zlib = require('zlib'),
    fs = require('fs');
var l_file_name;
	
exports.convert = function(req, res) {

//    req.setEncoding('utf-8'); // This corrupts the data stream with binary data
    res.setHeader("Content-Type", "text/plain");

	
    req.busboy.on('file', function(fieldname, file, filename, encoding, mimetype) {
		l_file_name = __dirname + "x.js.gz";
		console.error("DDD: " + l_file_name);
		 
        var writeStream = fs.createWriteStream(
            l_file_name, {
            flags: 'w',
			defaultEncoding: undefined,
            content_type: mimetype,
            metadata: {
                encoding: encoding,
            }
        });

        file.pipe(writeStream);
    });

    req.busboy.on('finish', function() {
		json_file2 = fs.createReadStream(l_file_name);
		var data = new Buffer('');
		var chunks = [];
		var chunk;

		json_file2.on('readable', function() {
			while ((chunk=json_file2.read()) != null) {
				chunks.push(chunk);
			}
		});

		json_file2.on('end', function() {
			data=Buffer.concat(chunks);
			console.log('Gzipped binary stream: ' + data.toString('hex').substring(0, 132));
			
	        return res.status(200).send({
				message: "OK: " + l_file_name + '; Gzipped binary stream: ' + data.toString('hex').substring(0, 132)
				});
		});	

    });

    req.pipe(req.busboy);
};