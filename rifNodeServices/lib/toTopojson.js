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
// Rapid Enquiry Facility (RIF) - toTopojson - GeoJSON to TopoJSON convertor
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

/*
 * Function:	toTopojson()
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
toTopojson=function(d, ofields, options, stderr, req, res, response) {
	var httpErrorResponse = require('../lib/httpErrorResponse')
        rifLog = require('../lib/rifLog'),
	    topojson = require('topojson');
	
	var msg="File [" + d.no_files + "]: " + d.file.file_name;
	
	response.message = response.message + '\nProcessing ' + msg;	
	try {	
		d.file.jsonData = undefined;
		// Set up file list reponse now, in case of exception
		
/* Array file objects:
*						file_name: File name
*						topojson: TopoJSON created from file geoJSON,
*						topojson_stderr: Debug from TopoJSON module,
*						topojson_runtime: Time to convert geoJSON to topoJSON (S),
*						file_size: Transferred file size in bytes,
*						transfer_time: Time to transfer file (S),
*						uncompress_time: Time to uncompress file (S)/undefined if file not compressed,
*						uncompress_size: Size of uncompressed file in bytes
*/
		response.file_list[d.no_files-1] = {
			file_name: d.file.file_name,
			topojson: '',
			topojson_stderr: '',
			topojson_runtime: '',
			file_size: '',
			transfer_time: '',
			uncompress_time: undefined,
			uncompress_size: undefined
		};				
		d.file.jsonData = JSON.parse(d.file.file_data.toString()); // Parse file stream data to JSON

		// Re-route topoJSON stderr to stderr.str
		stderr.disable();
		var lstart = new Date().getTime();			
		d.file.topojson = topojson.topology({   // Convert geoJSON to topoJSON
			collection: d.file.jsonData
			}, options);				
		stderr.enable(); 				   // Re-enable stderr
		
		d.file.topojson_stderr=stderr.str();  // Get stderr as a string	
		stderr.clean();						// Clean down stderr string
		stderr.restore();                  // Restore normal stderr functionality 

// Add file stderr and topoJSON to my response
// This will need a mutex if > 1 thread is being processed at the same time
		response.file_list[d.no_files-1].topojson=d.file.topojson;
		response.file_list[d.no_files-1].topojson_stderr=d.file.topojson_stderr;

		var end = new Date().getTime();
		response.file_list[d.no_files-1].topojson_runtime=(end - lstart)/1000; // in S			
		response.file_list[d.no_files-1].file_size=d.file.file_size;
		response.file_list[d.no_files-1].transfer_time=d.file.transfer_time;
		response.file_list[d.no_files-1].uncompress_time=d.file.uncompress_time;
		response.file_list[d.no_files-1].uncompress_size=d.file.uncompress_size;
		
		msg+= "; runtime: " + "; topoJSON length: " + JSON.stringify(d.file.topojson).length + "]"
		if (d.file.topojson_stderr.length > 0) {  // Add topoJSON stderr to message	
// This will need a mutex if > 1 thread is being processed at the same time	
			response.message = response.message + "\n" + msg + " OK:\nTopoJson.topology() stderr >>>\n" + 
				d.file.topojson_stderr + "<<< TopoJson.topology() stderr";
			rifLog.rifLog(msg + "TopoJson.topology() stderr >>>\n"  + 
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
		if (d.file.file_data.length > 0) { // Add first 132 chars of file to message
			var truncated_data=d.file.file_data.toString().substring(0, 132);
			if (!/^[\x00-\x7F]*$/.test(truncated_data)) { // Test if not ascii
				truncated_data=d.file.file_data.toString('hex').substring(0, 132); // Binary: display as hex
			}
			if (truncated_data.length > 132) {
				msg=msg + "\nTruncated data:\n" + truncated_data + "\n";
			}
			else {
				msg=msg + "\nData:\n" + truncated_data + "\n";
			}
		}
	
		response.no_files=d.no_files;			// Add number of files process to response
		response.fields=ofields;				// Add return fields			
		httpErrorResponse.httpErrorResponse(__file, __line, "toTopojson()", rifLog, 
			500, req, res, msg, e, response);				
		return;
	} 	
}

module.exports.toTopojson = toTopojson;	