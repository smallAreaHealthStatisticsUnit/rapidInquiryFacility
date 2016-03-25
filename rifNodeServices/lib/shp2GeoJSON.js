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
// Rapid Enquiry Facility (RIF) - toTopojson - Shapefile file to GeoJSON convertor; method specfic functions
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
 * Function:	shp2GeoJSONWriteFile()
 * Parameters:	file name with path, data, RIF logging object, uuidV1 
 *				express HTTP request object
 * Returns:		Text of field processing log
 * Description: Write GeoJSON file 
 */ 
shp2GeoJSONWriteFile=function(file, data, serverLog, uuidV1, req) {
	const fs = require('fs');
	
/* 1.3G SOA 2011 file uploads in firefox (NOT chrome) but gives:

Error(Error): toString failed
Stack>>>
Error: toString failed
    at Buffer.toString (buffer.js:382:11)
    at shp2GeoJSONWriteFile (C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\lib\shp2GeoJSON.js:59:35)
    at Object.shp2GeoJSONFileProcessor (C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\lib\shp2GeoJSON.js:505:3)
    at Busboy.<anonymous> (C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\lib\nodeGeoSpatialServices.js:524:27)
    at emitNone (events.js:72:20)
    at Busboy.emit (events.js:166:7)
    at Busboy.emit (C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\node_modules\connect-busboy\node_modules\busboy\lib\main.js:31:35)
    at C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\node_modules\connect-busboy\node_modules\busboy\lib\types\multipart.js:52:13
    at doNTCallback0 (node.js:419:9)
    at process._tickCallback (node.js:348:13)<<<
	
This does mean it converted to shapefile to geojson...

 */
	// This needs to be done asynchronously, so save as <file>.tmp
	fs.writeFile(file + '.tmp', data.toString('binary'), 
		{
			encoding: 'binary',
			mode: 0o600,
		}, function(err) {
		if (err) {
			serverLog.serverLog2(__file, __line, "shp2GeoJSONWriteFile", 
				'ERROR! [' + uuidV1 + '] writing file: ' + file + '.tmp', req, err);
			try {
				fs.unlinkSync(file + '.tmp');
			}
			catch (e) { 
				serverLog.serverLog2(__file, __line, "shp2GeoJSONWriteFile", 
					'ERROR! [' + uuidV1 + '] deleting file (after fs.writeFile error): ' + file + '.tmp', req, e);
			}
		}
		try { // And do an atomic rename when complete
			fs.renameSync(file + '.tmp', file);
			serverLog.serverLog2(__file, __line, "shp2GeoJSONWriteFile", 
				'OK! [' + uuidV1 + '] saved file: ' + file, req, err);			
		} catch (e) { 
			serverLog.serverLog2(__file, __line, "shp2GeoJSONWriteFile", 
				'ERROR! [' + uuidV1 + '] renaming file: ' + file + '.tmp', req, e);
		}
	}); // End of fs.writeFile()
}
		
/*
 * Function:	shp2GeoJSONFieldProcessor()
 * Parameters:	fieldname, val, shapefile_options, ofields [field parameters array], response object, 
 *				express HTTP request object, RIF logging object
 * Returns:		Text of field processing log
 * Description: shp2GeoJSON method field processor. Called from req.busboy.on('field') callback function
 *
 *				verbose: 	Set Topojson.Topology() ???? option if true. 
 */ 
shp2GeoJSONFieldProcessor=function(fieldname, val, shapefile_options, ofields, response, req, rifLog) {
	var msg;
	var text="";
	
	if ((fieldname == 'verbose')&&(val == 'true')) {
		if (shapefile_options) {
			shapefile_options.verbose = true;
		}
	}
	else if (fieldname == 'uuidV1') {
		text+="uuidV1: " + val;
		ofields["uuidV1"]=val;		
	}
	else if (fieldname == 'encoding') {
		text+="DBF file encoding set to: " + val;
		ofields["encoding"]=val;		
		shapefile_options.encoding = val;
	}
	else if ((fieldname == 'ignore-properties')&&(val == 'true')) {
		text+="Read faster (ignore-properties): " + val;
		ofields["ignore-properties"]=val;	
		shapefile_options["ignore-properties"] = true;
	}	
	else if (fieldname == 'shapefileBaseName') {
		text+="shapefileBaseName set to: " + val;
		ofields["shapefileBaseName "]=val;		
	}
//	else if (fieldname == 'geometryColumn') {
//		text+="geometryColumn set to: " + val;
//		ofields["geometryColumn"]=val;		
//	}
	else if ((fieldname == 'store')&&(val == 'true')) {
		text+="JSON file will be stored";
		ofields["store"]=val;		
		shapefile_options["store"] = true;
	}	
	else {
		ofields[fieldname]=val;	
	}	
	
	return text;
}

/*
 * Function:	shp2GeoJSONCheckFiles()
 * Parameters:	Shapefile list, response object, total shapefiles, ofields [field parameters array], 
 *				RIF logging object, express HTTP request object, express HTTP response object, shapefile options
 * Returns:		Rval object { file_errors, msg }
 * Description: Check which files and extensions are present, convert shapefiles to geoJSON
 */
shp2GeoJSONCheckFiles=function(shpList, response, shpTotal, ofields, serverLog, req, res, shapefile_options) {
	const os = require('os'),
	      path = require('path'),
	      fs = require('fs'),
	      shapefile = require('shapefile'),
	      reproject = require('reproject'),
	      srs = require('srs');
		  
	var shapefile_no=0;
	var rval = {
		file_errors: 0,
		msg: ""
	};
	
	/*
	 * Function:	readShapeFile()
	 * Parameters:	Shapefile name with path, projection name with path, 
	 *				RIF logging object, express HTTP request object, express HTTP response object, start time, uuidV1, shapefile options, time to write file,
	 *				JSON file name with path, response object, shapefile number
	 * Returns:		Nothing
	 * Description: Read shapefile
	 */
	var readShapeFile = function(shapeFileName, projFileName, serverLog, req, res, lstart, uuidV1, shapefile_options, writeTime, jsonFileName, 
		response, shapefile_no) {

		// Work out projection; convert to 4326 if required - NEEDS ERROR HANDLERS
		var prj=fs.readFileSync(projFileName);
		var mySrs;
		var crss={
			"EPSG:2400": "+lon_0=15.808277777799999 +lat_0=0.0 +k=1.0 +x_0=1500000.0 +y_0=0.0 +proj=tmerc +ellps=bessel +units=m +towgs84=414.1,41.3,603.1,-0.855,2.141,-7.023,0 +no_defs",
			"EPSG:3006": "+proj=utm +zone=33 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs",
			"EPSG:4326": "+proj=longlat +ellps=WGS84 +datum=WGS84 +no_defs",
			"EPSG:3857": "+proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 +lon_0=0.0 +x_0=0.0 +y_0=0 +k=1.0 +units=m +nadgrids=@null +wktext  +no_defs"
		};
		
		if (prj) {
			mySrs=srs.parse(prj);
			if (!mySrs.srid) { // 
				if (mySrs.name == "British_National_Grid") {
					mySrs.srid="27700";
				}
				else { // Error - needs to be defined
				}
			}
			crss["EPSG:" + mySrs.srid] = mySrs.proj4;
		}
		
		// Now read shapefile
		shapefile.read(shapeFileName, shapefile_options, function(err, collection) {
			if (err) {
				serverLog.serverLog2(__file, __line, "readShapeFile", 
					'ERROR! [' + uuidV1 + '] in shapefile read: ' + shapeFileName, req, err);
			} // End of err
			// OK
			var end = new Date().getTime();
			var elapsedTime=(end - lstart)/1000; // in S
			var proj4;

			if (mySrs.srid != "4326") {
				try {
					wgs84=reproject.toWgs84(collection, "EPSG:" + mySrs.srid, crss);
				}
				catch (e) {
					serverLog.serverLog2(__file, __line, "readShapeFile", 
						"reproject.toWgs84() failed [" + uuidV1 + "] File: " + shapeFileName + "\n" + e.message + 
						"\nProjection data:\n" + prj + "\n<<<" +
						"\nCRSS database >>>\n" + JSON.stringify(crss, null, 2) + "\n<<<" +
						"\nGeoJSON sample >>>\n" + JSON.stringify(collection, null, 2).substring(0, 600) + "\n<<<");
				}
			}
			else {
				wgs84=collection;
			}
			if (collection.bbox) { // Check bounding box present
				serverLog.serverLog2(__file, __line, "readShapeFile", 
					"OK [" + uuidV1 + "] File: " + shapeFileName + 
					"\nwritten after: " + writeTime + " S; total time: " + elapsedTime + " S\nBounding box [" +
					"xmin: " + collection.bbox[0] + ", " +
					"ymin: " + collection.bbox[1] + ", " +
					"xmax: " + collection.bbox[2] + ", " +
					"ymax: " + collection.bbox[3] + "];" + 
					"\nProjection name: " + mySrs.name + "; " +
					"srid: " + mySrs.srid + "; " +
					"proj4: " + mySrs.proj4);
				var boundingBox = {
					xmin: 0,
					ymin: 0,
					xmax: 0,
					ymax: 0
				};
				boundingBox.xmin=wgs84.bbox[0];
				boundingBox.ymin=wgs84.bbox[1];
				boundingBox.xmax=wgs84.bbox[2];					
				boundingBox.ymax=wgs84.bbox[3];
				if (shapefile_options.store) {
					shp2GeoJSONWriteFile(jsonFileName, JSON.stringify(collection), serverLog, uuidV1, req);
				}
				else { // Convert to geoJSON and return
					response.file_list[shapefile_no-1].file_size=fs.statSync(shapeFileName).size;
					response.file_list[shapefile_no-1].geojson_time=elapsedTime;
					response.file_list[shapefile_no-1].geojson=wgs84;
					response.file_list[shapefile_no-1].boundingBox=boundingBox;
					response.file_list[shapefile_no-1].proj4=mySrs.proj4;
					response.file_list[shapefile_no-1].srid=mySrs.srid;
					response.file_list[shapefile_no-1].projection_name=mySrs.name;
					
					// WE NEED TO WAIT FOR MULTIPLE FILES TO COMPLETE BEFORE RETURNING A RESPONSE
					if (!req.finished) { // Reply with error if httpErrorResponse.httpErrorResponse() NOT already processed					
						var output = JSON.stringify(response);// Convert output response to JSON 
		// Need to test res was not finished by an expection to avoid "write after end" errors			
						res.write(output);                  // Write output  
						res.end();	
					}
					else {
						serverLog.serverLog("FATAL! Unable to return OK reponse to user - httpErrorResponse() already processed", req);
					}				
				}
				// Move to here
			}
			else {
				serverLog.serverLog2(__file, __line, "readShapeFile", 
					'ERROR! [' + ofields["uuidV1"] + '] no collection.bbox: ' + 
					shapeFileName, req);						
			}	
		}); /* End of shapefile.read() */	
	},
	/*
	 * Function:	waitForShapeFileWrite()
	 * Parameters:	Shapefile name with path, DBF file name with path, Projection file name with path, 
	 *				JSON file with path, number of waits,
	 *				RIF logging object, express HTTP request object, express HTTP response object, start time, uuidV1, shapefile options,
	 *				Response object, shapefile number
	 * Returns:		Nothing
	 * Description: Wait for shapefile to appear, call readShapeFile()
	 */
	waitForShapeFileWrite = function(shapeFileName, dbfFileName, projFileName, jsonFileName, 
		waits, serverLog, req, res, lstart, uuidV1, shapefile_options, response, shapefile_no) {
		
		if (waits > 5) {
			if (fs.existsSync(shapeFileName) || fs.existsSync(shapeFileName + ".tmp") ||
			    fs.existsSync(dbfFileName) || fs.existsSync(dbfFileName + ".tmp") ||
				fs.existsSync(projFileName) || fs.existsSync(projFileName + ".tmp")) { // Exists			
			}
			else {
				var end = new Date().getTime();
				var elapsedTime=(end - lstart)/1000; // in S
			
				serverLog.serverLog2(__file, __line, "waitForShapeFileWrite", 
					"[" + uuidV1 + "] FAIL Wait[" + waits + "; " + elapsedTime + " S];" +
					" Shapefile[" + i + "/" + shpTotal + "/" + key + "]: " + shapeFileName + 
					" shapefile/dbf file/projection file was not written", req);
				return;									
			}
		}
		
		// Warning this code is asynchronous!		
		setTimeout(function() { // Timeout function
			var end = new Date().getTime();
			var elapsedTime=(end - lstart)/1000; // in S
			
			if (waits > 100) { // Timeout
				serverLog.serverLog2(__file, __line, "waitForShapeFileWrite().setTimeout", 
					'ERROR! [' + uuidV1 + '] timeout (' + elapsedTime + ' S) waiting for file: ' + shapeFileName, req);
				return;
			}
			else if (fs.existsSync(shapeFileName) && fs.existsSync(dbfFileName) && fs.existsSync(projFileName)) { // OK			
				readShapeFile(shapeFileName, projFileName, serverLog, req, res, lstart, uuidV1, shapefile_options, elapsedTime, jsonFileName, 
					response, shapefile_no);
				return;
			}
			else { // OK			
				serverLog.serverLog2(__file, __line, "waitForShapeFileWrite().setTimeout", 
					"[" + uuidV1 + "] Wait(" + elapsedTime + " S): " + waits + ";\nshapefile: " + shapeFileName + 
					";\ntests: " + fs.existsSync(shapeFileName) + ", " + fs.existsSync(shapeFileName + ".tmp"), req, e);
				waitForShapeFileWrite(shapeFileName, dbfFileName, projFileName, jsonFileName,
					waits+1, serverLog, req, res, lstart, uuidV1, shapefile_options, response, shapefile_no); //Recurse  
			}
			
		}, 1000 /* 1S */); // End of setTimeout
	} // End of waitForShapeFileWrite()

	for (var key in shpList) {
		shapefile_no++;

//
// Check if geometryColumn field is defined - NOT NEEDED - LIBRARY HANDLES IT
//
//		if (!ofields["geometryColumn"] || ofields["geometryColumn"] == "") {
//			rval.file_errors++;					// Increment file error count	
//			rval.msg+="\nFAIL geometryColumn field is not defined";			
//		}
//
// All require files present
//		
		if (shpList[key].hasShp && shpList[key].hasPrj && shpList[key].hasDbf) {
			var dir=os.tmpdir() + "/shp2GeoJSON/" + ofields["uuidV1"] + "/" + key;
			var shapeFileName=dir + "/" + key + ".shp";
			var dbfFileName=dir + "/" + key + ".dbf";
			var projFileName=dir + "/" + key + ".prj";
			var jsonFileName=dir + "/" + key + ".json";
			
			var lstart = new Date().getTime();
			
			response.file_list[shapefile_no-1] = {
				file_name: key + ".shp",
	//			geojson: '',
				file_size: '',
				transfer_time: '',
				geojson_time: '',
				uncompress_time: undefined,
				uncompress_size: undefined
			};
			response.file_list[shapefile_no-1].transfer_time=shpList[key].transfer_time;
			
			rval.msg+="\nProcessing shapefile[" + shapefile_no + "]: " + shapeFileName;		
			response.no_files=shpTotal;				// Add number of files process to response
			response.fields=ofields;				// Add return fields
			response.file_errors+=rval.file_errors;

			// Wait for shapefile to appear
			// This continues processing, return control to core calling function			
			waitForShapeFileWrite(shapeFileName, dbfFileName, projFileName, jsonFileName, 
				0, serverLog, req, res, lstart, ofields["uuidV1"], shapefile_options, response, shapefile_no);		
		}	

		
//
// Missing shapefile/DBF file/Projection file
//		
		else {		
			rval.file_errors++;					// Increment file error count	
			rval.msg+="\nFAIL Shapefile[" + shapefile_no + "/" + shpTotal + "/" + key + "]:\n" + shpList[key].fileName + 
				" is missing a shapefile/DBF file/Projection file";							
		}
	} // Shapefiles for loop
	
	response.no_files=shpTotal;				// Add number of files process to response
	response.fields=ofields;				// Add return fields
	response.file_errors+=rval.file_errors;
	response.message = response.message + "\n" + rval.msg;

	return rval;
}

/*
 * Function:	shp2GeoJSONFileProcessor()
 * Parameters:	d object (temporary processing data), Shapefile list, total shapefiles, path Node.js library, response object, 
 *				RIF logging object, express HTTP request object
 * Returns:		Rval object { file_errors, msg, total shapefiles }
 * Description: Note which files and extensions are present, generate RFC412v1 UUID if required, save shapefile to temporary directory
 *				Called once per file
 */
shp2GeoJSONFileProcessor = function(d, shpList, shpTotal, path, response, ofields, serverLog, req) {
	const uuid = require('node-uuid'),
	      crypto = require('crypto'),
	      os = require('os'),
	      fs = require('fs');

	var rval = {
		file_errors: 0,
		msg: "",
		shpTotal: shpTotal
	};
	
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
	var generateUUID = function() {
	
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
	} /* End of generateUUID() */, 
	/*
	 * Function:	createTemporaryDirectory()
	 * Parameters:	Directory component array [$TEMP/shp2GeoJSON, <uuidV1>, <fileNoext>]
	 * Returns:		Final directory (e.g. $TEMP/shp2GeoJSON/<uuidV1>/<fileNoext>)
	 * Description: Create temporary directory (for shapefiles)
	 */
	createTemporaryDirectory = function(dirArray, rval, response, fs) {
		var tdir;
		for (var i = 0; i < dirArray.length; i++) {  
			if (!tdir) {
				tdir=dirArray[i];
			}
			else {
				tdir+="/" + dirArray[i];
			}	
			try {
				var stats=fs.statSync(tdir);
			} catch (e) { 
				if (e.code == 'ENOENT') {
					try {
						fs.mkdirSync(tdir);
						response.message += "\nmkdir: " + tdir;
					} catch (e) { 
						rval.msg = "ERROR: Cannot create directory: " + e.message;
						rval.file_errors++;
					}			
				}
				else {
					rval.msg = "ERROR: Cannot access directory: " + e.message;
					rval.file_errors++;
				}
			}
		}
		return tdir;
	} /* End of createTemporaryDirectory() */;
	
	var extName = path.extname(d.file.file_name);
	var fileNoext = path.basename(d.file.file_name, extName);
	var extName2 = path.extname(fileNoext); /* undefined if .shp, dbf etc; */
	if (extName == ".xml") {
		while (extName2) { 		// deal with funny ESRI XML files: .shp.xml, .shp.iso.xml, .shp.ea.iso.xml 
			extName=extName2 + extName;
			fileNoext = path.basename(d.file.file_name, extName);
			extName2 = path.extname(fileNoext); 
		}
	}
//
// UUID generator
//	
	if (!ofields["uuidV1"]) { // Generate UUID
		ofields["uuidV1"]=generateUUID();
	}

//	
// Shapefile checks
//	
	if (!shpList[fileNoext]) { // Use file name without the extension as an index into the shapefile lisy
		rval.shpTotal++;
		shpList[fileNoext] = {
			fileName: d.file.file_name,
			transfer_time: 0,
			hasShp: false,
			hasPrj: false,
			hasDbf: false
		};
	}
	shpList[fileNoext].transfer_time=d.file.transfer_time;
	
	// Check for shp, dbf and prj extensions
	if (extName == '.shp') {
		shpList[fileNoext].hasShp=true;
		response.message+="\nhasShp for file: " + shpList[fileNoext].fileName;
	}
	else if (extName == '.prj') {
		shpList[fileNoext].hasPrj=true;
		response.message+="\nhasPrj for file: " + shpList[fileNoext].fileName;
	}
	else if (extName == '.dbf') {
		shpList[fileNoext].hasDbf=true;
		response.message+="\nhasDbf for file: " + shpList[fileNoext].fileName;
	}
	else {
		response.message+="\nIgnore extension: " + extName + " for file: " + shpList[fileNoext].fileName;
	}
	
//	
// Create directory: $TEMP/shp2GeoJSON/<uuidV1>/<fileNoext> as required
//
	var dirArray=[os.tmpdir() + "/shp2GeoJSON", ofields["uuidV1"], fileNoext];
	dir=createTemporaryDirectory(dirArray, rval, response, fs);
	
//	
// Write file to directory
//	
	var file=dir + "/" + fileNoext + extName;
	if (fs.existsSync(file)) { // Exists
		rval.msg = "ERROR: Cannot write file, already exists: " + file;
		rval.file_errors++;
	}
	else {
		shp2GeoJSONWriteFile(file, d.file.file_data, serverLog, ofields["uuidV1"], req);
		response.message += "\nSaving file: " + file;
	}
	
	if (rval.file_errors > 0) {
		response.no_files=shpTotal;				// Add number of files process to response
		response.fields=ofields;				// Add return fields
		response.file_errors+=rval.file_errors;	
		response.message = rval.msg + "\n" + response.message;
	}

	return rval;
}
							
// Export
module.exports.shp2GeoJSONFileProcessor = shp2GeoJSONFileProcessor;
module.exports.shp2GeoJSONCheckFiles = shp2GeoJSONCheckFiles;
module.exports.shp2GeoJSONFieldProcessor = shp2GeoJSONFieldProcessor;