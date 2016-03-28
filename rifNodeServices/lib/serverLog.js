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
// Rapid Enquiry Facility (RIF) - serverLog logging primitive
//                                Logs to stderr; uses Magic-Globals 
//								  https://www.npmjs.com/package/magic-globals by Gavin Engel
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

require('magic-globals'); // For file and line information. Does not work from Event queues
var util = require('util');
 
/*
 * Function:	serverLog.serverLog()
 * Parameters:	Message, HTTP request object, error object
 * Returns:		Nothing
 * Description: serverLog logging primitive, e.g.

		serverLog.serverLog("TopoJson.topology() stderr for file " + d.no_files + ": " + d.file.file_name + ">>>\n"  + 
			d.file.topojson_stderr + "<<<", 
			req);	

   DO NOT USE WITH EVENT QUEUES - THE STACK IS MANGLED; use the second form below
   
Stderr log:
			
[C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\routes\toTopojson:269; function: _process_json();
 url: /toTopojson; ip: ::ffff:127.0.0.1; Content-Encoding: undefined]
TopoJson.topology() stderr for file 1: test_6_sahsu_4_level4_0_0_0.js.gz>>>
bounds: -7.58829438 52.68753577 -4.88653786 55.5268098 (spherical)
pre-quantization: 753m (0.00677°) 791m (0.00712°)
topology: 3986 arcs, 18197 points
<<<		
 
 */
serverLog = function(msg, req, err) {
	var calling_function = arguments.callee.caller.name || '(anonymous)';
	// Get file information from magic-globals: __stack
	var file=__stack[2].getFileName().split('/').slice(-1)[0].split('.').slice(0)[0];
	var line=__stack[2].getLineNumber();
	
	serverLog2(file, line, calling_function, msg, req, err)
}

/*
 * Function:	serverLog.serverLog2()
 * Parameters:	File called from, line number called from, procedure called from, 
 *				Message, HTTP request object, error object
 * Returns:		Nothing
 * Description: serverLog logging primitive for event anonymous functions, e.g.

			serverLog.serverLog2(__file, __line, "req.busboy.on:('finish')", 
				"Processed: " + response.no_files + " files; debug message:\n" + response.message, req);	

Stderr log:
			
[C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\routes\toTopojson:333; function: req.busboy.on:('finish')();
 url: /toTopojson; ip: ::ffff:127.0.0.1; Content-Encoding: undefined]
Processed: 1 files; debug message:

Field: my_test[gzip geoJSON file]; 
Field: my_reference[5]; 
Field: verbose[true]; verbose mode enabled
Field: zoomLevel[0]; Quantization set to: 400
Field: length[1021397]; 
[test_6_sahsu_4_level4_0_0_0.js.gz:1] OK:
>>>
bounds: -7.58829438 52.68753577 -4.88653786 55.5268098 (spherical)
pre-quantization: 753m (0.00677°) 791m (0.00712°)
topology: 3986 arcs, 18197 points
<<<	
 
 */
serverLog2 = function(file, line, calling_function, msg, req, err) {
	// Get file information from magic-globals: __stack
	var file_trace=file + 
		":" + line + 
		"; function: " + calling_function + "()";
	var request_tracer="";
	var error_tracer=";\nNo exceptions";
	var theDate = new Date();
	
	// Add request tracer if present
	if (req && req.get) {
		request_tracer=";\n url: " + req.url + 
					"; ip: " + req.ip;
		if (req.get('Content-Encoding')) {
			request_tracer+="; Content-Encoding: " + req.get('Content-Encoding');
		}
	}
	// Add error tracer if present
	if (err && err.message && err.stack) {
		error_tracer="\n\nError(" + err.name + "): " + err.message + "\nStack>>>\n" + err.stack + "<<<";
	}
	
	console.error(theDate.toString() + "\n[" + file_trace + request_tracer + "]\n" + msg + error_tracer);
}

// Likewise for error; except RAISE the error
serverError = function(msg, req, err) {
	var calling_function = arguments.callee.caller.name || '(anonymous)';
	// Get file information from magic-globals: __stack
	var file=__stack[2].getFileName().split('/').slice(-1)[0].split('.').slice(0)[0];
	var line=__stack[2].getLineNumber();
	
	serverLog2(file, line, calling_function, msg, req, err);
	if (err) {
		throw err;
	}
	else {
		throw new Error(msg);
	}
}

serverError2 = function(file, line, calling_function, msg, req, err) {		
	serverLog2(file, line, calling_function, msg, req, err);
	if (err) {
		throw err;
	}
	else {
		throw new Error(msg);
	}
}

/*
 * Function:	generateUUID()
 * Parameters:	None
 * Returns:		UUID v1
 * Description: UUID generator - Generate RFC4122 version 1 compliant UUID
 * 				Use first six bits of HMAC 256 key for the Node ID
 * 				Key is mac address of eth0 + or hostname + process ID if eth0 does not exist	
 * 				E.g. 7a9ee1c0-e469-11e5-b100-2f737ba57483
 * 
 *				The use of the MAC address as the node is correct, but does reveal information useful for spoofing.
 *				Therefore the bytes are swapped in the order 012 [Organizationally Unique Identifier (OUI)] 534 [NIC specific]
 */
generateUUID = function() {
	const os = require('os'),
	      uuid = require('node-uuid'),
	      crypto = require('crypto');
	      
	var networkInterfaces = os.networkInterfaces();
	var key;
	var nif;
	var buf;
	var hash;
	
	if (networkInterfaces['eth0']) { // For Linux - easy
		nif=networkInterfaces['eth0'];	
		if (nif[0].mac != '00:00:00:00:00:00') {
			key=nif[0].mac;
//				console.error("A mac: " + key + "; " + key.replace(/:/g, '').toString());
			buf=new Buffer(key.replace(/:/g, '').toString(), 'hex');
		}			
	}
	else {
		for (var key in networkInterfaces) { // Windows - can be called  all sort of things. 
											 // Use the first one that is not localhost
			nif=networkInterfaces[key];
			if (nif[0].mac != '00:00:00:00:00:00') {
				key=nif[0].mac;
//					console.error("B mac: " + key + "; " + key.replace(/:/g, '').toString());
				buf=new Buffer(key.replace(/:/g, '').toString(), 'hex');
				break;
			}
		}
	}

	if (!buf) { // no non zeros nic; use the hostname + PID
		key=os.hostname() + "+" + process.pid;
		hash=crypto.createHmac('sha256', key).digest('hex');	
		buf=new Buffer(hash, 'hex');			
	}	

//		console.error("Key: " + key + "\n" +
//			"hash[0]: [0x" + hash.substring(0, 2) + "], [0x" + buf[0].toString(16) + "]\n" +
//			"hash[1]: [0x" + hash.substring(2, 4) + "], [0x" + buf[1].toString(16) + "]\n" +
//			"hash[2]: [0x" + hash.substring(4, 6) + "], [0x" + buf[2].toString(16) + "]\n" +
//			"hash[3]: [0x" + hash.substring(6, 8) + "], [0x" + buf[3].toString(16) + "]\n" +
//			"hash[4]: [0x" + hash.substring(8, 10) + "], [0x" + buf[4].toString(16) + "]\n" +
//			"hash[5]: [0x" + hash.substring(10, 12) + "], [0x" + buf[5].toString(16) + "]\n" +
//			"networkInterface: " + JSON.stringify(nif, null, 4));
	return uuid.v1({
		node: [buf[0], buf[1], buf[2], buf[5], buf[3], buf[4]] // first 6 bytes of hash; NIC bits swapped
		});		
} /* End of generateUUID() */
	
module.exports.serverError = serverError;
module.exports.serverError2 = serverError2;
module.exports.serverLog = serverLog;
module.exports.serverLog2 = serverLog2;
module.exports.generateUUID = generateUUID;
