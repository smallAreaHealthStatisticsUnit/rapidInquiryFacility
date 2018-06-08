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
 * Function:	createWriteStreamWithCallback()
 * Parameters:	file name with path, data, RIF logging object, uuidV1, response object, 
 *				number of records (may be undefined), callback (may be undefined)
 * Returns:		Writeable stream
 * Description: Create writable stream for large file writes using 1MB chunks; e.g. GeoJSON, topoJSON, shapefiles 
 * 				Install error and stream end handlers.
 *				At end, close stream, rename <file>.tmp to <file>, call callback if defined; undefine data if data set
 */ 
createWriteStreamWithCallback=function(file, data, serverLog, uuidV1, req, response, records, callback) {

	const fs = require('fs');
	var msg;
	var lstart = new Date().getTime();
	
	scopeChecker(__file, __line, {	
		file: file,			
		uuidV1: uuidV1,
		req: req,
		response: response,	
		message: response.message,
		serverLog: serverLog
	} /* Manadatory */,
	{
		callback: callback
	} /* Optional */);

	const path = require('path');
	var baseName=path.basename(file);
	
	// Create stream as <file>.tmp so can be renamed at the end
	var wStream=fs.createWriteStream(file + '.tmp', // Do nice async non blocking IO in 1MB chunks
	{
		encoding: 'binary',
		mode: 0o600,
	});
	
	wStream.on('finish', function() {
		scopeChecker(__file, __line, {	
			file: file,			
			uuidV1: uuidV1,
			req: req,
			response: response,	
			message: response.message,
			serverLog: serverLog
		} /* Manadatory */,
		{
			callback: callback
		} /* Optional */);
		var end = new Date().getTime();
		var elapsedTime=(end - lstart)/1000; // in S
	
		try { // And do an atomic rename when complete
			fs.renameSync(file + '.tmp', file);
			var len=fs.statSync(file).size;
			msg="[" + elapsedTime + "s] OK Saved file: " + baseName + "; size: " + len + " bytes";
			if (elapsedTime > 0) {
				var mbytesPerSec=len/(elapsedTime*1024*1024);
				mbytesPerSec=Math.round(mbytesPerSec * 100) / 100;
				msg+="; " + mbytesPerSec + " MB/S";
			}
			if (data && len != data.length) {
				serverLog.serverError2(__file, __line, "createWriteStreamWithCallback.wStream.on('finish')", 
					'ERROR! [' + uuidV1 + '] file: ' + baseName + ' is the wrong size, expecting: ' + data.length + ' got: ' + len +
					'\n\nDiagnostics >>>\n' + response.message + '\n<<<= End of diagnostics\n', req);				
			}
			else if (data) {
				data=undefined; // Be nice. Could force GC at this point		
/*				if (global.gc && len > (1024*1024*500)) { // GC is file > 500M
					serverLog.serverLog2(__file, __line, "createWriteStreamWithCallback.wStream.on('finish')", "OK [" + uuidV1 + "] " + msg + "\nForce garbage collection", req);
					global.gc();
				} */
			}

			if (records && elapsedTime > 0) {
				msg+="; " + Math.round(records/elapsedTime) + " records/S";
			}
			
			if (callback) { 
				msg+=";\nRun callback(" + typeof(callback) + "): " + (callback.name || "anonymous");
				callback();	
			}
			else {	
				msg+="; (no callback)";
			}
			response.message+="\n" + msg;
//			serverLog.serverLog2(__file, __line, "createWriteStreamWithCallback.wStream.on('finish')", 
//				msg, req);
		} catch (e) { 
			serverLog.serverError2(__file, __line, "createWriteStreamWithCallback.wStream.on('finish')", 
				'ERROR! [' + uuidV1 + '] renaming file: ' + file + '.tmp' + ";\nsize: " + len + " bytes" +
				";\nDiagnostics>>>\n" + response.message + "\n<<< End of diagnostics\n", req, e);
		}
	});
	wStream.on('error', function (err) {
		try {
			fs.unlinkSync(file + '.tmp');
			serverLog.serverError2(__file, __line, "createWriteStreamWithCallback.wStream.on('error')", 
				'ERROR! [' + uuidV1 + '] writing file: ' + file + '.tmp', req, err);
		}
		catch (e) { 
			serverLog.serverError2(__file, __line, "createWriteStreamWithCallback.wStream.on('error')", 
				'ERROR! [' + uuidV1 + '] deleting file (after fs.writeFile error: ' + e.message + '): ' + file + '.tmp', req, e);
		}
	}); 
	msg="Created stream for file: " + file; 
	if (callback) { 
		msg+="; with callback(" + typeof(callback) + "): " + (callback.name || "anonymous");	
	}
	else {	
		msg+="; (no callback)";
	}
	response.message+="\n" + msg;
			
	return wStream;
}

/*
 * Function:	streamWriteFileWithCallback()
 * Parameters:	file name with path, data, RIF logging object, uuidV1 
 *				express HTTP request object, response object, 
 *				number of records (may be undefined), delete data (by undefining) at stream end, callback (may be undefined)
 * Returns:		Text of field processing log
 * Description: Write large file in 1MB chunks using a stream; e.g. GeoJSON, topoJSON, shapefiles 
 * 				Data will be undefined at the end
 */ 
streamWriteFileWithCallback=function streamWriteFileWithCallback(file, data, serverLog, uuidV1, req, response, records, deleteData, callback) {

	scopeChecker(__file, __line, {	
		file: file,		
		data: data,			
		uuidV1: uuidV1,
		req: req,
		response: response,
		message: response.message,
		serverLog: serverLog,
		ofields: response.fields	// For exception testing
	} /* Manadatory */,
	{
		callback: callback
	} /* Optional */);
	
	var lstart = new Date().getTime();

	// This needs to be done asynchronously, so save as <file>.tmp
	
	// Create writable stream for large file writes using 1MB chunks; e.g. GeoJSON, topoJSON, shapefiles 
	// Install error and stream end handlers.
	// At end, close stream, rename <file>.tmp to <file>, call callback if defined
	var wStream;
	if (deleteData && deleteData == true) {
		wStream=createWriteStreamWithCallback(file, data /* Delete at end */, serverLog, uuidV1, req, response, records, callback);
	}
	else {
		wStream=createWriteStreamWithCallback(file, undefined /* Do not delete at end */, serverLog, uuidV1, req, response, records, callback);
	}
	
	streamWriteFilePieceWithCallback(file, data, wStream, serverLog, uuidV1, req, response, true /* lastPiece */, lstart, 
		undefined /* no callback, callback called from steam.end() */);
	
} // End of streamWriteFileWithCallback
	
/*
 * Function:	streamWriteFilePieceWithCallback()
 * Parameters:	file name with path, data, writeable stream, RIF logging object, uuidV1 
 *				express HTTP request object, response object, lastPiece (true/false), start time, callback
 * Returns:		Text of field processing log
 * Description: Write large file in 1MB chunks using a stream; e.g. GeoJSON, topoJSON, shapefiles 
 */ 
streamWriteFilePieceWithCallback=function streamWriteFilePieceWithCallback(file, data, wStream, serverLog, uuidV1, req, response, lastPiece, lstart, callback) {

	var isWriteableStream = function (obj) {
		var stream = require('stream');
		
		return obj instanceof stream.Stream &&
			typeof (obj._write === 'function') &&
			typeof (obj._writeableState === 'object');
	}

	scopeChecker(__file, __line, {	
		file: file,		
		data: data,			
		wStream: wStream,
		uuidV1: uuidV1,
		req: req,
		response: response,
		message: response.message,
		serverLog: serverLog,
		lstart: lstart,
		lastPiece: lastPiece,
		ofields: response.fields	// For exception testing
	} /* Manadatory */,
	{
		callback: callback
	} /* Optional */);
	if (lastPiece !== true && lastPiece !== false) {
 		serverLog.serverError2(__file, __line, "streamWriteFilePieceWithCallback", "Invalid value for lastPiece: " + lastPiece, req, undefined);
	}
	if (!isWriteableStream(wStream)) {
 		serverLog.serverError2(__file, __line, "streamWriteFilePieceWithCallback", "Invalid writeable wStream: " + JSON.stringify(wStream, null, 4), req, undefined);
	}
	
	const path = require('path');
	var baseName=path.basename(file);
	
	var pos=0;
	var len=1024*1024; // 1MB chunks
	var i=0;
	var drains=0;
	var drainElapsedTime=0;
	var drainMsg="";
	
	_myWrite(); // Do first write

	function _postDrain() {
		var end = new Date().getTime();
		var elapsedTime=(end - lstart)/1000; // in S
		
		if (elapsedTime > drainElapsedTime)	{	
			drainMsg+="\n[" + elapsedTime + "s] Stream drained: " + drains + " for file: " + baseName + " [" + i + "] pos: " + pos + 
				"; len: " + len + "; data.length: " + data.length;
			drainElapsedTime=elapsedTime;
		}
		_myWrite(); // Do next write
	}
	
	/*
	 * Function: 	_myWrite()
	 * Parameters:	None
	 * Returns: 	nothing
	 * Description: Write out Buffer to stream in 1MB pieces; waiting for IO drain
	 */
	function _myWrite() {
		var ok=true;
		var j=0;
		
		do {
			i++;
			j++;
			buf=data.slice(pos, pos+len);
			ok=wStream.write(buf, 'binary');
			pos+=len;	
			if (pos >= data.length) { // Piece has been written 
				var end = new Date().getTime();
				var elapsedTime=(end - lstart)/1000; // in S
				if (drainMsg) {
					response.message+="\n" + drainMsg;
				}
				if (lastPiece === true) {
					response.message+="\n[" + elapsedTime + "s] End write file: " + baseName + "; [" + i + "] new pos: " + pos + 
						"; len: " + len + "; data.length: " + data.length + "; lastPiece: " + lastPiece + "; ok: " + ok;	
					wStream.end(); // Signal end of stream
				}
				
				if (callback) {
					response.message+=";\nRun callback(" + typeof(callback) + "): " + (callback.name || "anonymous");	
					callback();					
				}
				else {
					response.message+="; no callback defined";	
				}
			}
			else if (i == 1 || j > 10) { // Log first, then every 10 writes
				var end = new Date().getTime();
				var elapsedTime=(end - lstart)/1000; // in S
				
				j=0;
				response.message+="\n[" + elapsedTime + "s] Wrote " + buf.length + " bytes to file: " + baseName + "; recurse [" + i + "] new pos: " + pos + 
					"; len: " + len + "; data.length: " + data.length + "; ok: " + ok;
			}			
		}
		while (pos < data.length && ok);
		
		if (pos < data.length && !ok) { // Wait for drain event
								 // i.e. ok == false: This return value is strictly advisory. You MAY continue to write, even if it returns false.
								 // However, writes will be buffered in memory
			var end = new Date().getTime();
			var elapsedTime=(end - lstart)/1000; // in S
			
			drains++;		
			wStream.once('drain', _postDrain); // When drained, call myWrite() again
		}
	} // End of _myWrite()

} // End of streamWriteFilePieceWithCallback
	
// Export
module.exports.streamWriteFileWithCallback = streamWriteFileWithCallback;
module.exports.createWriteStreamWithCallback = createWriteStreamWithCallback;
module.exports.streamWriteFilePieceWithCallback = streamWriteFilePieceWithCallback;