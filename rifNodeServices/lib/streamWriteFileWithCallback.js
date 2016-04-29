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
// Rapid Enquiry Facility (RIF) - streamWriteFileWithCallback - Write large file in 1MB checkunk using a stream; e.g. GeoJSON, topoJSON, shapefiles 
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
 * Function:	streamWriteFileWithCallback()
 * Parameters:	file name with path, data, RIF logging object, uuidV1 
 *				express HTTP request object, response object, callback
 * Returns:		Text of field processing log
 * Description: Write large file in 1MB checkunk using a stream; e.g. GeoJSON, topoJSON, shapefiles 
 */ 
streamWriteFileWithCallback=function(file, data, serverLog, uuidV1, req, response, callback) {
	const fs = require('fs');
	const path = require('path');

	scopeChecker(__file, __line, {	
		file: file,		
		data: data,			
		uuidV1: uuidV1,
		req: req,
		response: response,
		message: response.message,
		serverLog: serverLog
	});
	
	// Check callback
	if (callback) {
		if (typeof callback != "function") {
			throw new Error("Callback in use but is not a function: " + typeof callback)
		}
	}
	
	var baseName=path.basename(file);

	// This needs to be done asynchronously, so save as <file>.tmp
	var wStream=fs.createWriteStream(file + '.tmp', // Do nice async non blocking IO
		{
			encoding: 'binary',
			mode: 0o600,
		});
	var msg;
	
	wStream.on('finish', function() {
		try { // And do an atomic rename when complete
			fs.renameSync(file + '.tmp', file);
			var len=fs.statSync(file).size;
			msg="Saved file: " + baseName + "; size: " + len + " bytes";
			response.message+="\n" + msg;
			if (len != data.length) {
				serverLog.serverError2(__file, __line, "streamWriteFileWithCallback", 
					'ERROR! [' + uuidV1 + '] file: ' + baseName + ' is the wrong size, expecting: ' + data.length + ' got: ' + len +
					'\n\nDiagnostics >>>\n' + response.message + '\n<<<= End of diagnostics\n', req);				
			}
			else {
				
				data=undefined; // Be nice. Could force GC at this point		
				if (global.gc && len > (1024*1024*500)) { // GC is file > 500M
					serverLog.serverLog2(__file, __line, "streamWriteFileWithCallback", "OK [" + uuidV1 + "] " + msg + "\nForce garbage collection", req);
					global.gc();
				}
				else {
					serverLog.serverLog2(__file, __line, "streamWriteFileWithCallback", "OK [" + uuidV1 + "] " + msg, req);
				}
			}
			if (callback) { 
				callback();	
			}
		} catch (e) { 
			serverLog.serverError2(__file, __line, "streamWriteFileWithCallback", 
				'ERROR! [' + uuidV1 + '] renaming file: ' + file + '.tmp', req, e);
		}
	});
	wStream.on('error', function (err) {
		try {
			fs.unlinkSync(file + '.tmp');
			serverLog.serverError2(__file, __line, "streamWriteFileWithCallback", 
				'ERROR! [' + uuidV1 + '] writing file: ' + file + '.tmp', req, err);
		}
		catch (e) { 
			serverLog.serverError2(__file, __line, "streamWriteFileWithCallback", 
				'ERROR! [' + uuidV1 + '] deleting file (after fs.writeFile error: ' + e.message + '): ' + file + '.tmp', req, e);
		}
	}); 
	
	
/* 1.3G SOA 2011 file uploads in firefox (NOT chrome) but gives:

Error(Error): toString failed
Stack>>>
Error: toString failed
    at Buffer.toString (buffer.js:382:11)
    at streamWriteFileWithCallback (C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\lib\shpConvert.js:59:35)
    at Object.shpConvertFileProcessor (C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\lib\shpConvert.js:505:3)
    at Busboy.<anonymous> (C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\lib\nodeGeoSpatialServices.js:524:27)
    at emitNone (events.js:72:20)
    at Busboy.emit (events.js:166:7)
    at Busboy.emit (C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\node_modules\connect-busboy\node_modules\busboy\lib\main.js:31:35)
    at C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\node_modules\connect-busboy\node_modules\busboy\lib\types\multipart.js:52:13
    at doNTCallback0 (node.js:419:9)
    at process._tickCallback (node.js:348:13)<<<
	
This does mean it converted to shapefile to geojson...

So write in pieces...
 */	
	var pos=0;
	var len=1024*1024; // 1MB chunks
	var i=0;
	var drains=0;
	
	myWrite(); // Do first write

	/*
	 * Function: 	myWrite()
	 * Parameters:	None
	 * Returns: 	nothing
	 * Description: Write out Buffer to stream in 1MB pieces; waiting for IO drain
	 */
	function myWrite() {
		var ok=true;
		
		do {
			i++;
			buf=data.slice(pos, pos+len);
			ok=wStream.write(buf, 'binary');
			response.message+="\nWrote " + buf.length + " bytes to file: " + baseName + "; recurse [" + i + "] pos: " + pos + 
				"; len: " + len + "; data.length: " + data.length + "; ok: " + ok;
			pos+=len;	
			if (pos >= data.length) {
				response.message+="\nEnd write file: " + baseName + "; recurse [" + i + "] pos: " + pos + 
					"; len: " + len + "; data.length: " + data.length;			
				wStream.end(); // Signal end of stream
			}		
		}
		while (pos < data.length && ok);
		
		if (pos < data.length) { // Wait for drain event
			drains++;
			response.message+="\nWait for stream drain: " + drains + " for file: " + baseName + " [" + i + "] pos: " + pos + 
				"; len: " + len + "; data.length: " + data.length;		
			wStream.once('drain', myWrite); // When drained, call myWrite() again
		}
	} // End of myWrite()

} // End of streamWriteFileWithCallback
		
// Export
module.exports.streamWriteFileWithCallback = streamWriteFileWithCallback;
