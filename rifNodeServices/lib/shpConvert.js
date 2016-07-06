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
// Rapid Enquiry Facility (RIF) - shpConvert - Shapefile file convertor; method specfic functions
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
 
 // Functions exported to nodeGeoSptialServices.jhs
 
/*
 * Function:	shpConvertFieldProcessor()
 * Parameters:	fieldname, val, shapefile_options, ofields [field parameters array], response object, 
 *				express HTTP request object, RIF logging object
 * Returns:		Text of field processing log
 * Description: shpConvert method field processor. Called from req.busboy.on('field') callback function
 *
 *				verbose: 	Set Topojson.Topology() ???? option if true. 
 */ 
shpConvertFieldProcessor=function shpConvertFieldProcessor(fieldname, val, shapefile_options, ofields, response, req, serverLog) {
	
	scopeChecker(__file, __line, {
		fieldname: fieldname,
		val: val,
		ofields: ofields,
		shapefile_options: shapefile_options,
		req: req,
		response: response,
		serverLog: serverLog
	});	
	
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
	else {
		ofields[fieldname]=val;	
	}	
	
	return text;
} // End of shpConvertFieldProcessor()

/*
 * Function:	shpConvert()
 * Parameters:	Ofields array, files array, response object, HTTP request object, HTTP response object
 *				shapefile options
 * Returns:		Nothing
 * Description: Generate UUID if required, process all files into ShpList
 *				Check which files and extensions are present, convert shapefiles to geoJSON, simplify etc	
 *
 *				Calls shpConvertFileProcessor() in a queue to process each component file
 *				On queue end, call: shpConvertCheckFiles() - check which files and extensions are present, convert shapefile to geoJSON, simplify etc			  
 */	 
shpConvert = function shpConvert(ofields, d_files, response, req, res, shapefile_options) {	

	scopeChecker(__file, __line, {
		ofields: ofields,
		shapefile_options: shapefile_options,
		req: req,
		res: res,
		response: response
	});	
	
	var shpList = {};
	var shpTotal=0;		

	const serverLog = require('../lib/serverLog'),
	      httpErrorResponse = require('../lib/httpErrorResponse'),
	      nodeGeoSpatialServicesCommon = require('../lib/nodeGeoSpatialServicesCommon'),
		  async = require('async'); 

	if (!req) {
		throw new Error("No HTTP request object [out of scope]");
	}		  
	if (!res) {
		throw new Error("No HTTP response object [out of scope]");
	}

	// Set up async queue; 1 worker
	var shapeFileComponentQueue = async.queue(function(fileData, shapeFileComponentQueueCallback) {
		var shapeFileComponentQueueCallbackFunc = function shapeFileComponentQueueCallbackFunc(err) {
			shapeFileComponentQueueCallback(err);
		}
		
		try {
			shpConvertFileProcessor(fileData["d"], fileData["shpList"], fileData["shpTotal"], fileData["response"], fileData["uuidV1"], 
				fileData["req"], fileData["serverLog"], fileData["httpErrorResponse"],
				shapeFileComponentQueueCallbackFunc, fileData["nodeGeoSpatialServicesCommon"]);	
		}
		catch (e) {
			httpErrorResponse.httpErrorResponse(__file, __line, "shpConvert", 
				serverLog, 500, req, res, "async.queue(): Unhandled exception in shpConvertFileProcessor()", e, response);			
		}
	}, 1 /* Single threaded - fileData needs to become an object */); // End of async.queue()
	
	/*
	 * Function:	shapeFileComponentQueue.drain()
	 * Parameters: 	None
	 * Returns:		N/A; calls shpConvertCheckFiles() to check which files and extensions are present, convert shapefile to geoJSON, simplify etc
	 * Description: Async queue drain function; mwhen when all shapefile components have been processed 
	 */
	shapeFileComponentQueue.drain = function shapeFileComponentQueueDrain() {
		shpTotal=Object.keys(shpList).length;
		
		// Free up memory
		for (var i = 0; i < response.no_files; i++) {
//			response.message+="\nFreeing " + d_files.d_list[i].file.file_size + " bytes for file: " + d_files.d_list[i].file.file_name;
			d_files.d_list[i].file.file_data=undefined;
			if (global.gc && d_files.d_list[i].file.file_size > (1024*1024*500)) { // GC if file > 500M
				serverLog.serverLog2(__file, __line, "Force garbage collection for file: " + d_files.d_list[i].file.file_name, fileData["req"]);
				global.gc();
			}
		}
	
		if (!shpTotal || shpTotal == 0) {
			var msg="ERROR! no shapefiles found";
			response.file_errors++;	
			response.message = msg + "\n" + response.message;
		
			httpErrorResponse.httpErrorResponse(__file, __line, "shpConvert", 
				serverLog, 500, req, res, msg, undefined, response);			
		}
		response.no_files=shpTotal;				// Add number of files process to response
		response.fields=ofields;				// Add return fields	
		
		// Call: shpConvertCheckFiles() - check which files and extensions are present, convert shapefile to geoJSON, simplify etc						
		rval=shpConvertCheckFiles(shpList, response, shpTotal, ofields, serverLog, httpErrorResponse,
			req, res, shapefile_options);
		if (rval.file_errors > 0 || response.file_errors) {
			response.file_errors+=rval.file_errors;	
			response.message = rval.msg + "\n" + response.message;
		
			httpErrorResponse.httpErrorResponse(__file, __line, "shpConvert", 
				serverLog, 500, req, res, rval.msg, undefined, response);							
		}		
	} // End of shapeFileComponentQueue.drain()
		
	for (var i = 0; i < response.no_files; i++) {
		var fileData = {
			d:			d_files.d_list[i],
			shpList: 	shpList,
			shpTotal: 	shpList.length,
			response: 	response,
			uuidV1: 	ofields["uuidV1"],
			i: 			0,
			req: 		req,
			serverLog: 	serverLog,
			httpErrorResponse: 	httpErrorResponse,
			nodeGeoSpatialServicesCommon: nodeGeoSpatialServicesCommon
		}
		
		fileData.i=i;
		response.message+="\nQueued (shapeFileComponentQueue) file for shpConvertFileProcessor[" + fileData["i"] + "]: " + fileData.d.file.file_name;
		// Add to queue			
//		serverLog.serverLog2(__file, __line, "shpConvert", 
//		"In shpConvertFileProcessor(), shapeFileComponentQueue[" + fileData["i"] + "]: " + fileData.d.file.file_name);

		shapeFileComponentQueue.push(fileData, function shapeFileComponentQueuePush(err) {
			if (err) {
//				var msg='ERROR! [' + fileData["uuidV1"] + '] in shapefile read: ' + fileData.d.file.file_name;
				var msg="Error! in shpConvertFileProcessor()";
				
				response.message+="\n" + msg;	
				response.file_errors++;
				serverLog.serverLog2(__file, __line, "shpConvertFileProcessor().shapeFileComponentQueue.push()", msg, 
					fileData["req"], err);	
			} // End of err		
			// else {
				// response.message+="\nCompleted processing file[" + fileData["i"] + "]: " + fileData.d.file.file_name;
				//
				// Release memory
				//fileData.d.file.file_data=undefined; // NO DONT - STILL IN USE i = i == end of loop; as is fileData
			// }
		});	
										
	} // End of for loop	
	
} // End of shpConvert()
		
// Export
module.exports.shpConvert = shpConvert;
module.exports.shpConvertFieldProcessor = shpConvertFieldProcessor;

// Local functions:

/*
 * Function:	shpConvertFileProcessor()
 * Parameters:	d object (temporary processing data), Shapefile list, total shapefiles, response object, 
 *				uuidV1, HTTP request object, serverLog object, httpErrorResponse object, callback, nodeGeoSpatialServicesCommon object
 * Returns:		Rval object { file_errors, msg, total shapefiles }
 * Description: Note which files and extensions are present, generate RFC412v1 UUID if required, save shapefile to temporary directory
 *				Called once per file from shapeFileComponentQueue
 */
shpConvertFileProcessor = function shpConvertFileProcessor(d, shpList, shpTotal, response, uuidV1, req, serverLog, httpErrorResponse, 
							shapeFileComponentQueueCallback, nodeGeoSpatialServicesCommon) {
		
	scopeChecker(__file, __line, {
		serverLog: serverLog,
		httpErrorResponse: httpErrorResponse,
		nodeGeoSpatialServicesCommon: nodeGeoSpatialServicesCommon
	});	
		
	var streamWriteFileWithCallback = require('../lib/streamWriteFileWithCallback');
	
	const os = require('os'),
	      fs = require('fs'),
	      path = require('path');
 
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

	if (extName == ".zip") { // Ignore zip files; process contents
		response.message+="\nIgnore zip file; process contents (loaded as individual files when zip file unpacked): " + d.file.file_name;	
	}
	else {
//	
// Shapefile checks
//	
		if (!shpList[fileNoext]) { // Use file name without the extension as an index into the shapefile lisy
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
	}
	
//	
// Create directory: $TEMP/shpConvert/<uuidV1>/<fileNoext> as required
//
	var dirArray=[os.tmpdir() + "/shpConvert", uuidV1, fileNoext];
	dir=nodeGeoSpatialServicesCommon.createTemporaryDirectory(dirArray, response, req, serverLog);
	
//	
// Write file to directory
//	
	var file=dir + "/" + fileNoext + extName;
	if (fs.existsSync(file)) { // Exists
		serverLog.serverError2(__file, __line, "shpConvertFileProcessor", 
			"ERROR: Cannot write file, already exists: " + file, req);
//			shapeFileComponentQueueCallback();		// Not needed - serverError2() raises exception 
	}
	else {
		// Also delete file data after the file is written to free memory
		streamWriteFileWithCallback.streamWriteFileWithCallback(file, d.file.file_data, serverLog, uuidV1, req, response, 
			undefined /* Records */, true /* delete data (by undefining) at stream end */, shapeFileComponentQueueCallback);
//		response.message += "\nSaving file: " + file;
	}
}
	
/*
 * Function:	shpConvertCheckFiles()
 * Parameters:	Shapefile list, response object, total shapefiles, ofields [field parameters array], 
 *				RIF logging object, httpErrorResponse object, express HTTP request object, express HTTP response object, shapefile options
 * Returns:		Rval object { file_errors, msg }
 * Description: Check which files and extensions are present, convert shapefiles to geoJSON
 * 				Called after all shapefile components have been saved to disk
 *
 * create shapeFileQueue to process each shapefile, call readShapeFile to read shapefile, process bounding box, convert to WGS84 if required
 * On end of shapeFileQueue:
 *				Re-order shapefiles by total areas; check all bounding boxes are the same
 * 				Setup and write XML config
 *				Process errors and retiurn response
 */
shpConvertCheckFiles=function shpConvertCheckFiles(shpList, response, shpTotal, ofields, serverLog, httpErrorResponse, req, res, shapefile_options) {
	const os = require('os'),
	      path = require('path'),
	      fs = require('fs'),
	      shapefile = require('shapefile'),
	      reproject = require('reproject'),
	      srs = require('srs'),
	      async = require('async'),
		  streamWriteFileWithCallback = require('../lib/streamWriteFileWithCallback'),
		  simplifyGeoJSON = require('../lib/simplifyGeoJSON');

	var rval = {
		file_errors: 0,
		msg: ""
	};

	scopeChecker(__file, __line, {
		serverLog: serverLog,
		httpErrorResponse: httpErrorResponse
	});	
		
	// Queue functions
	
	/*
	 * Function: 	createXmlFile()
	 * Parameters:	xmlConfig JSON
	 * Returns: 	nothing
	 * Description: Creates XML config file
	 */
	var createXmlFile = function createXmlFile(xmlConfig) {
		
		scopeChecker(__file, __line, {
			serverLog: serverLog,
			httpErrorResponse: httpErrorResponse,		
			response: response,
			message: response.message
		});	
		
		var fs = require('fs'),
		    xml2js = require('xml2js');

		var builder = new xml2js.Builder({
			rootName: "geoDataLoader" /*,
			doctype: { // DOES NOT WORK!!!!
					'ext': "geoDataLoader.xsd"
			} */
			});
		var xmlDoc = builder.buildObject(xmlConfig);
		
//		response.message+="\nCreated XML config [xml] >>>\n" + xmlDoc + "\n<<< end of XML config [xml]";
		response.message+="\nXML config as json>>>\n" + JSON.stringify(xmlConfig, null, 4) + "\n<<< end of XM config as json";
			
		fs.writeFileSync(xmlConfig.xmlFileDir + "/" + xmlConfig.xmlFileName, xmlDoc);
	} // End of createXmlFile()
	
	/*
	 * Function:	shapefileReadNextRecord()
	 * Parameters:	shapefile record, shape file data object, response object, reader function 
	 * Description:	Read next shapefile record, call reader function
	 */
	var shapefileReadNextRecord = function shapefileReadNextRecord(record, shapefileData, response, shapefileReader) {
		
		const nodeGeoSpatialServicesCommon = require('../lib/nodeGeoSpatialServicesCommon');
		
		var msg;
		var recNo=shapefileData["featureList"].length+1;
		var lRec=JSON.stringify(record);
		scopeChecker(__file, __line, {
			serverLog: serverLog,
			httpErrorResponse: httpErrorResponse,
			streamWriteFileWithCallback: streamWriteFileWithCallback,
			simplifyGeoJSON: simplifyGeoJSON
		});		
		
		shapefileData["recLen"]+=lRec.length;
		lRec=undefined;
		
		var doTrace=false;

		var end = new Date().getTime();
		var elapsedReadTime=(end - shapefileData["lstart"])/1000; // in S	
		if (!shapefileData["elapsedReadTime"]) {
			shapefileData["elapsedReadTime"]=elapsedReadTime;
		}
			
		if (((recNo/1000)-Math.floor(recNo/1000)) == 0 || recNo == 1 || elapsedReadTime > (shapefileData["elapsedReadTime"] + 1)) { 
			// Print read record diagnostics every 1000 shapefile records or second
			doTrace=true;
	
			msg="Reading shapefile record [" + recNo + "]; file: " + shapefileData["fileNoExt"] + "; size: " + shapefileData["recLen"];						
			if (shapefileData["recLen"] > 100*1024*1024) { // Write a log message every 100 MB
				serverLog.serverLog2(__file, __line, "readShapeFile", "+" + shapefileData["elapsedReadTime"] + "S; " + msg, shapefileData["req"]);
			}
			if (elapsedReadTime > (shapefileData["elapsedReadTime"] + 1)) { // Add status every 1S
				shapefileData["elapsedReadTime"]=elapsedReadTime;
				nodeGeoSpatialServicesCommon.addStatus(__file, __line, response, msg, 
					200 /* HTTP OK */, serverLog, req);  // Add end of shapefile read status
			}
			response.message+="\n+" + shapefileData["elapsedReadTime"] + "S; " + msg;
		}

		if (shapefileData["mySrs"].srid != "4326") { // Re-project to 4326
			try {
				msg="\nshapefile read [" + recNo	+ "] call reproject.toWgs84() for: " + shapefileData["fileNoExt"];
				if (shapefileData["featureList"].length == 0) { // Re-project BBOX with no features (or you will shrink it to the first feature!)
					msg+="\nBounding box conversion from (" + shapefileData["mySrs"].srid + "): " +
						"xmin: " + response.file_list[shapefileData["shapefile_no"]-1].geojson.bbox[0] + ", " +
						"ymin: " + response.file_list[shapefileData["shapefile_no"]-1].geojson.bbox[1] + ", " +
						"xmax: " + response.file_list[shapefileData["shapefile_no"]-1].geojson.bbox[2] + ", " +
						"ymax: " + response.file_list[shapefileData["shapefile_no"]-1].geojson.bbox[3] + "]; to\n";
						
					var min=reproject.toWgs84(
						{"type":"Point","coordinates":[
							response.file_list[shapefileData["shapefile_no"]-1].geojson.bbox[0], 
							response.file_list[shapefileData["shapefile_no"]-1].geojson.bbox[1]]}, 
						"EPSG:" + shapefileData["mySrs"].srid, 
						shapefileData["crss"]);
					response.file_list[shapefileData["shapefile_no"]-1].geojson.bbox[0]=min.coordinates[0];
					response.file_list[shapefileData["shapefile_no"]-1].geojson.bbox[1]=min.coordinates[1];
					var max=reproject.toWgs84(
						{"type":"Point","coordinates":[
							response.file_list[shapefileData["shapefile_no"]-1].geojson.bbox[2], 
							response.file_list[shapefileData["shapefile_no"]-1].geojson.bbox[3]]}, 
						"EPSG:" + shapefileData["mySrs"].srid, 
						shapefileData["crss"]);
					response.file_list[shapefileData["shapefile_no"]-1].geojson.bbox[2]=max.coordinates[0];
					response.file_list[shapefileData["shapefile_no"]-1].geojson.bbox[3]=max.coordinates[1];							

					msg+="Bounding box (4326): " + 
						"xmin: " + response.file_list[shapefileData["shapefile_no"]-1].geojson.bbox[0] + ", " +
						"ymin: " + response.file_list[shapefileData["shapefile_no"]-1].geojson.bbox[1] + ", " +
						"xmax: " + response.file_list[shapefileData["shapefile_no"]-1].geojson.bbox[2] + ", " +
						"ymax: " + response.file_list[shapefileData["shapefile_no"]-1].geojson.bbox[3] + "];";
				}
				shapefileData["featureList"].push(
					reproject.toWgs84(
						{type: "FeatureCollection", bbox: response.file_list[shapefileData["shapefile_no"]-1].geojson.bbox, features: [record]}, 
						"EPSG:" + shapefileData["mySrs"].srid, 
						shapefileData["crss"]).features[0]
					);
			}
			catch (e) {
				serverLog.serverError2(__file, __line, "shapefileReadNextRecord", 
					"[" + recNo + "]; reproject.toWgs84() failed [" + shapefileData["uuidV1"] + 
					"] File: " + shapefileData["shapeFileName"] + "\n" + 
					"\nProjection data:\n" + shapefileData["prj"] + "\n<<<" +
					"\nCRSS database >>>\n" + JSON.stringify(shapefileData["crss"], null, 2) + "\n<<<" +
					"\nGeoJSON sample >>>\n" + JSON.stringify(shapefileData["featureList"], null, 2).substring(0, 600) + "\n<<<", 
					shapefileData["req"], e);	
			}
		}
		else {
			shapefileData["featureList"].push(record); 		// Add feature to collection
		}

		record=undefined;	
		// Force garbage collection
		if (global.gc && shapefileData["recLen"] > (1024*1024*500) && ((recNo/10000)-Math.floor(recNo/10000)) == 0) { // GC if json > 500M;  every 10K records
			const v8 = require('v8');
			
			global.gc();
			var heap=v8.getHeapStatistics();
			msg+="\nMemory heap >>>";
			for (var key in heap) {
				msg+="\n" + key + ": " + heap[key];
			}
			msg+="\n<<< End of memory heap";
			serverLog.serverLog2(__file, __line, "shapefileReadNextRecord", "OK [" + shapefileData["uuidV1"] + 
				"] Force garbage collection shapefile at read [" + recNo + "] for: " + shapefileData["fileNoExt"] + "; size: " + shapefileData["recLen"] + msg, shapefileData["req"]);					
		}

		process.nextTick(shapefileData["reader"].readRecord, shapefileReader); 	// Read next record
	} // End of shapefileReadNextRecord()
		
	/*
	 * Function:	shapefileReadNextRecord()
	 * Parameters:	shapefile record, shape file data object, response object 
	 * Description:	Read last shapefile record, call writeGeoJsonbyFeature function to end shapefile process async queue item
	 */		
	var shapefileReadLastRecord = function shapefileReadLastRecord(record, shapefileData, response) {
		const nodeGeoSpatialServicesCommon = require('../lib/nodeGeoSpatialServicesCommon');
		
		var msg;
		
		scopeChecker(__file, __line, {
			serverLog: serverLog,
			httpErrorResponse: httpErrorResponse
		});
		
		response.file_list[shapefileData["shapefile_no"]-1].geojson.features=shapefileData["featureList"];
		shapefileData["featureList"]=undefined;
		var recNo=response.file_list[shapefileData["shapefile_no"]-1].geojson.features.length;				
		msg="shapefile read [" + recNo	+ "] completed for: " + shapefileData["fileNoExt"] + "; geoJSON length: " + shapefileData["recLen"];


		shapefileData["reader"].close(function readerClose(err) {
			var msg;
			
			if (err) {
				msg='ERROR! [' + shapefileData["uuidV1"] + '] in shapefile reader.close: ' + shapefileData["shapeFileName"];
				serverLog.serverError2(__file, __line, "readerClose", 
					msg, shapefileData["req"], err);							
			}	
			shapefileData["reader"]=undefined; // Release for gc			var end = new Date().getTime();
			
			var end = new Date().getTime();			
			shapefileData["elapsedReadTime"]=(end - shapefileData["lstart"])/1000; // in S
			msg="Read shapefile: " + shapefileData["shapeFileBaseName"] +
				"; " + shapefileData["recLen"] + " records";
			if (shapefileData["recLen"] > 50*1024*1024) { // 50 MB
				serverLog.serverLog2(__file, __line, "readerClose", "+" + shapefileData["elapsedReadTime"] + "S; " + msg, shapefileData["req"]);
			}
			response.message+="\n+" + shapefileData["elapsedReadTime"] + "S; " + msg;
			nodeGeoSpatialServicesCommon.addStatus(__file, __line, response, msg, 
				200 /* HTTP OK */, serverLog, req);  // Add end of shapefile read status
			
			if (response.file_list[shapefileData["shapefile_no"]-1].geojson.bbox) { // Check bounding box present
				var msg="File: " + shapefileData["shapeFileName"] + 
					"\nTotal time to process shapefile: " + shapefileData["elapsedReadTime"] + 
					" S\nBounding box [" +
					"xmin: " + response.file_list[shapefileData["shapefile_no"]-1].geojson.bbox[0] + ", " +
					"ymin: " + response.file_list[shapefileData["shapefile_no"]-1].geojson.bbox[1] + ", " +
					"xmax: " + response.file_list[shapefileData["shapefile_no"]-1].geojson.bbox[2] + ", " +
					"ymax: " + response.file_list[shapefileData["shapefile_no"]-1].geojson.bbox[3] + "];" + 
					"\nProjection name: " + shapefileData["mySrs"].name + "; " +
					"srid: " + shapefileData["mySrs"].srid + "; " +
					"proj4: " + shapefileData["mySrs"].proj4;
	//					serverLog.serverLog2(__file, __line, "shapefileReadLastRecord", "WGS 84 geoJSON (1..4000 chars)>>>\n" +
	//						JSON.stringify(response.file_list[shapefileData["shapefile_no"]-1].geojson, null, 2).substring(0, 4000) + "\n\n<<< formatted WGS 84");
				var dbf_fields = [];

				// Get DBF field names from features[i].properties
				if (response.file_list[shapefileData["shapefile_no"]-1].geojson.features[0].properties) {
					for (var key in response.file_list[shapefileData["shapefile_no"]-1].geojson.features[0].properties) {
						dbf_fields.push(key.toUpperCase());
					}						
				}
				// Get number of points from features[i].geometry.coordinates arrays; supports:  Point, LineString, Polygon, MultiPoint, MultiLineString, MultiPolygon
				if (response.file_list[shapefileData["shapefile_no"]-1].geojson.features[0].geometry.coordinates[0]) {
					var points=0;
					for (var i=0;i < response.file_list[shapefileData["shapefile_no"]-1].geojson.features.length;i++) {
						for (var j=0;j < response.file_list[shapefileData["shapefile_no"]-1].geojson.features[i].geometry.coordinates.length;j++) {
							var coordinates=response.file_list[shapefileData["shapefile_no"]-1].geojson.features[i].geometry.coordinates;
							if (coordinates[j][0][0]) { // a further dimension
								for (var k=0;k < coordinates[j].length;k++) {
									points+=coordinates[j][k].length+1;
	//											if (j<10 && k<10) {
	//												console.error("Feature [" + i + "." + j + "." + k + "] points: " + coordinates[j].length +
	//													JSON.stringify(coordinates[j], null, 2).substring(0, 132));
	//											}
								}
							}
							else {
								points+=coordinates[j].length+1;
	//								if (j<10) {								
	//									console.error("Feature [" + i + "." + j + "] points: " + coordinates.length +
	//									JSON.stringify(coordinates, null, 2).substring(0, 132));
	//								}
							}
						}
					}
					response.file_list[shapefileData["shapefile_no"]-1].points=points;
				}
				// Probably need to add geometry collection
				
	//					for (var i=0;i < response.file_list[shapefileData["shapefile_no"]-1].geojson.features.length;i++) {
	//							if (response.file_list[shapefileData["shapefile_no"]-1].geojson.features[i].properties) {
	//								console.error("Feature [" + i + "]: " + JSON.stringify(response.file_list[shapefileData["shapefile_no"]-1].geojson.features[i].properties, null, 2));
	//							}
	//					}
				if (recNo != response.file_list[shapefileData["shapefile_no"]-1].geojson.features.length) { // Record check
					var msg='ERROR! [' + shapefileData["uuidV1"] + "] in shapefile record check failed; expected: " + recNo + 
						"; got: " + response.file_list[shapefileData["shapefile_no"]-1].geojson.features.length + 
						"; for: " + shapefileData["shapeFileName"];
					serverLog.serverError2(__file, __line, "shapefileReadLastRecord", 
						msg, shapefileData["req"], err);		
				}
				msg+="\n" + dbf_fields.length + " fields: " + JSON.stringify(dbf_fields) + "; areas: " + 
					response.file_list[shapefileData["shapefile_no"]-1].geojson.features.length;
				if (response.file_list[shapefileData["shapefile_no"]-1].geojson.features.length && shapefileData["elapsedReadTime"] > 0) {
					msg+="; processed: " + Math.round(response.file_list[shapefileData["shapefile_no"]-1].geojson.features.length/shapefileData["elapsedReadTime"]) + " records/S";
				}
				response.message+="\n" + msg;

				// Convert to geoJSON and return
				response.file_list[shapefileData["shapefile_no"]-1].file_size=fs.statSync(shapefileData["shapeFileName"]).size;
				response.file_list[shapefileData["shapefile_no"]-1].geojson_time=shapefileData["elapsedReadTime"];
				var boundingBox = {
					xmin: response.file_list[shapefileData["shapefile_no"]-1].geojson.bbox[0],
					ymin: response.file_list[shapefileData["shapefile_no"]-1].geojson.bbox[1],
					xmax: response.file_list[shapefileData["shapefile_no"]-1].geojson.bbox[2],
					ymax: response.file_list[shapefileData["shapefile_no"]-1].geojson.bbox[3] 
				};
				
				response.file_list[shapefileData["shapefile_no"]-1].boundingBox=boundingBox;
				response.file_list[shapefileData["shapefile_no"]-1].proj4=shapefileData["mySrs"].proj4;
				response.file_list[shapefileData["shapefile_no"]-1].srid=shapefileData["mySrs"].srid;
				response.file_list[shapefileData["shapefile_no"]-1].projection_name=shapefileData["mySrs"].name;
				response.file_list[shapefileData["shapefile_no"]-1].total_areas=response.file_list[shapefileData["shapefile_no"]-1].geojson.features.length;
				response.file_list[shapefileData["shapefile_no"]-1].dbf_fields=dbf_fields;
				
				response.message+="\nCompleted processing shapefile[" + shapefileData["shapefile_no"] + "]: " + shapefileData["shapeFileName"];
	
				writeGeoJsonbyFeature(shapefileData, response);	// Write geoJSON file feature by feature	
					// Runs callbacks:
					// * streamWriteFilePieceWithCallback (footer) calls:
					// 	 * testFunc; calls:
					//     * topoFunction; calls:
					//       * shapeFileQueueCallbackFunc which run shapeFileQueueCallback
					// Nothing is now run at stream end!
			}
			else {
				serverLog.serverError2(__file, __line, "shapefileReadLastRecord", 
					'ERROR! [' + shapefileData["uuidV1"] + '] no collection.bbox: ' + 
					shapefileData["shapeFileName"], shapefileData["req"]);	
			}		
		});
	} // End of shapefileReadLastRecord()
	
	/*
	 * Function:	writeGeoJsonbyFeature()
	 * Parameters:
	 * Description:	Write geoJSON file feature by feature
	 
	 				Runs callbacks:
					* streamWriteFilePieceWithCallback (footer) calls:
					  * testFunc; calls:
					     * topoFunction; calls:
					       * shapeFileQueueCallbackFunc which run shapeFileQueueCallback
					Nothing is now run at stream end!
	 */ 	
	var writeGeoJsonbyFeature = function writeGeoJsonbyFeature(shapefileData, response) {
		const nodeGeoSpatialServicesCommon = require('../lib/nodeGeoSpatialServicesCommon');
		
		scopeChecker(__file, __line, {
			serverLog: serverLog,
			httpErrorResponse: httpErrorResponse,
			shapefileData: shapefileData,
			response: response,
			file_no: response.file_list[shapefileData["shapefile_no"]-1],
			geojson: response.file_list[shapefileData["shapefile_no"]-1].geojson,
			bbox: response.file_list[shapefileData["shapefile_no"]-1].geojson.bbox,
			async: async,
			streamWriteFileWithCallback: streamWriteFileWithCallback,
			simplifyGeoJSON: simplifyGeoJSON
		});
		
		var header="{\"type\":\"FeatureCollection\",\"bbox\":" + 
				JSON.stringify(response.file_list[shapefileData["shapefile_no"]-1].geojson.bbox) + ",\"features\":[";	
		var footer="]}";
		var z=0;
		var y=0;
		var feature="";
		var wStream;
		var numFeatures=response.file_list[shapefileData["shapefile_no"]-1].geojson.features.length;
		var lstart=new Date().getTime();
	
		async.forEachOfSeries(response.file_list[shapefileData["shapefile_no"]-1].geojson.features	/* col */, 
			function writeGeoJsonbyFeatureSeries(value, index, seriesCallback) {
					var inCallback=false;
					
					var seriesCallbackFunc = function seriesCallbackFunc(e) { // Cause seriesCallback to be named
						try {
							inCallback=true;
							seriesCallback(e);
						}
						catch (e2) {	
							if (e) {
								serverLog.serverError2(__file, __line, "seriesCallbackFunc", "Stacked error running callback; first error: " + e.message, req, e2);
							}
							else {
								serverLog.serverError2(__file, __line, "seriesCallbackFunc", "First error running callback", req, e2);
							}								
						}
					}
					
					try {						
						z++;
						y++;
						var end = new Date().getTime();
						var elapsedJsonSaveTime=(end - lstart)/1000; // in S
						if (!shapefileData["elapsedJsonSaveTime"]) {	
							shapefileData["elapsedJsonSaveTime"]=elapsedJsonSaveTime;
						}
						else if (elapsedJsonSaveTime > shapefileData["elapsedJsonSaveTime"] + 1) { // Update JSON save status every second
							shapefileData["elapsedJsonSaveTime"]=elapsedJsonSaveTime;						
							nodeGeoSpatialServicesCommon.addStatus(__file, __line, response, "[+" + elapsedJsonSaveTime+ "s] Saved JSON feature: "  + z, 
								200 /* HTTP OK */, serverLog, req);  // Add end of shapefile read status
						}
						if (z == 1) {
							// Write header
							response.message+="\nWrite header for: " + shapefileData["jsonFileName"]; 
							wStream=streamWriteFileWithCallback.createWriteStreamWithCallback(shapefileData["jsonFileName"], 
								undefined /* data: do not undefine! */, 
								serverLog, shapefileData["uuidV1"], shapefileData["req"], response, numFeatures, false /* do not delete data (by undefining) at stream end */, 
								undefined /* No callbacks at stream end! */);			
							wStream.write(header, 'binary');	// Write header						
							feature=JSON.stringify(response.file_list[shapefileData["shapefile_no"]-1].geojson.features[index]);
						}
						else {
							feature+="," + JSON.stringify(response.file_list[shapefileData["shapefile_no"]-1].geojson.features[index]);
						}
						if (z == numFeatures) {
//									console.error("Write final feature at index: " + index + "/" + numFeatures + "; feature length: " + feature.length);
							response.message+="\nWrite final feature at index: " + index + "/" + numFeatures + "; feature length: " + feature.length;
							streamWriteFileWithCallback.streamWriteFilePieceWithCallback(shapefileData["jsonFileName"], 
								feature, 
								wStream,
								serverLog, shapefileData["uuidV1"], shapefileData["req"], response, 
								false /* lastPiece */, lstart, seriesCallbackFunc);
							feature="";									
						}
						else if (feature.length > 1024*1024*50) {
//									console.error("Write feature at index: " + index + "/" + numFeatures + "; feature length: " + feature.length);
							response.message+="\nWrite feature at index: " + index + "/" + numFeatures + "; feature length: " + feature.length;
							streamWriteFileWithCallback.streamWriteFilePieceWithCallback(shapefileData["jsonFileName"], 
								feature, 
								wStream,
								serverLog, shapefileData["uuidV1"], shapefileData["req"], response, 
								false /* lastPiece */, lstart, seriesCallbackFunc);
							feature="";
						}
						else if (y > 1000) {
							y=0;
							process.nextTick(seriesCallbackFunc); // Avoid Maximum call stack size exceeded;
						}
						else {
							seriesCallbackFunc();
						}
					}
/*
Use inCallback boollean to avoid:

C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\lib\shpConvert.js:813
						throw err;
						^

Error: Callback was already called.
    at C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\node_modules\async\dist\async.js:839:36
    at seriesCallbackFunc (C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\lib\shpConvert.js:720:8)
    at writeGeoJsonbyFeatureSeries (C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\lib\shpConvert.js:807:7)
    at replenish (C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\node_modules\async\dist\async.js:873:21)
    at C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\node_modules\async\dist\async.js:883:15
    at eachOfLimit (C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\node_modules\async\dist\async.js:4015:26)
    at Object.<anonymous> (C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\node_modules\async\dist\async.js:929:20)
    at writeGeoJsonbyFeature (C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\lib\shpConvert.js:715:9)
    at readerClose (C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\lib\shpConvert.js:661:5)
    at FSReqWrap.oncomplete (fs.js:82:15)
	
This error in actually originating from the error handler function	
 */
					catch (e) {
						if (inCallback) {
							serverLog.serverError2(__file, __line, "writeGeoJsonbyFeatureSeries", e.message, req, e);
						}
						else {
							try {
								seriesCallbackFunc(e);
							}
							catch (err) {
								serverLog.serverError2(__file, __line, "writeGeoJsonbyFeatureSeries", "First error: " + e.message, req, err);	

							}
						}
					}
			}, 
			function writeGeoJsonbyFeatureSeriesEnd(err) {																	/* Callback at end */
				if (err) {
					serverLog.serverError2(__file, __line, "writeGeoJsonbyFeatureSeriesEnd", err.message, req, undefined);
				}
				else { // Write footer
					// Callbacks:
					// * streamWriteFilePieceWithCallback (footer) calls:
					// 	 * testFunc; calls:
					//     * topoFunction; calls:
					//       * shapeFileQueueCallbackFunc which run shapeFileQueueCallback
					// Nothing is now run at stream end!
					
					var shapeFileQueueCallbackFunc = function shapeFileQueueCallbackFunc(e) {
						scopeChecker(__file, __line, {	
							callback: shapefileData["callback"],
							message: response.message
						});		
						
						var end = new Date().getTime();
						var elapsedTime=(end - lstart)/1000; // in S	
						if (e) {
							nodeGeoSpatialServicesCommon.addStatus(__file, __line, response, "TopoJSON creation and save failed: " + shapefileData["topojsonFileBaseName"] + " took: " + elapsedTime + "S", 
								500 /* HTTP Failure */, serverLog, req);  // Add end of shapefile read status	
						}
						nodeGeoSpatialServicesCommon.addStatus(__file, __line, response, "TopoJSON creation and save: " + shapefileData["topojsonFileBaseName"] + " took: " + elapsedTime + "S", 
							200 /* HTTP OK */, serverLog, req);  // Add end of shapefile read status
						response.message+=";\nRun shapeFileQueueCallback callback()";
						shapefileData["callback"](e);								
					}	
					var topoFunction=function topoFunction(e) {
						
						var end = new Date().getTime();
						var elapsedTime=(end - lstart)/1000; // in S	
						
						if (e) {
							nodeGeoSpatialServicesCommon.addStatus(__file, __line, response, "JSON save failed: " + shapefileData["jsonFileBaseName"] + " took: " + elapsedTime + "S", 
								500 /* HTTP OK */, serverLog, req);  // Add end of shapefile read status					
							shapeFileQueueCallbackFunc(e);		
						}
						nodeGeoSpatialServicesCommon.addStatus(__file, __line, response, "JSON save: " + shapefileData["jsonFileBaseName"] + " took: " + elapsedTime + "S", 
							200 /* HTTP OK */, serverLog, req);  // Add end of shapefile read status
						// Create topoJSON
						simplifyGeoJSON.shapefileSimplifyGeoJSON(response.file_list[shapefileData["shapefile_no"]-1], response, shapefileData, 
							shapefileData["topojson_options"], shapeFileQueueCallbackFunc /* Callback */);							
					}							
// For testing					
					var testFunc = function testFunc(e) {
						
						if (e) {
							throw e;
						}
//								console.error("Creating: " + shapefileData["jsonFileName"] + ".2");
						streamWriteFileWithCallback.streamWriteFileWithCallback(shapefileData["jsonFileName"] + ".2", 
							JSON.stringify(response.file_list[shapefileData["shapefile_no"]-1].geojson), 
							serverLog, shapefileData["uuidV1"], shapefileData["req"], response, 
							numFeatures /* records */, false /* do not delete data (by undefining) at stream end */,
							topoFunction /* callback */);
					}			

					response.message+="\nWrite footer for: " + shapefileData["jsonFileName"]; 
					try {
						streamWriteFileWithCallback.streamWriteFilePieceWithCallback(shapefileData["jsonFileName"], 
								footer, 
								wStream,
								serverLog, shapefileData["uuidV1"], shapefileData["req"], response, 
								true /* lastPiece */, lstart, topoFunction /* testFunc */ /* callback */);
					}
					catch (e) {			
						serverLog.serverLog2(__file, __line, "writeGeoJsonbyFeatureSeriesEnd", 
							"Unexpected error in: streamWriteFilePieceWithCallback() handler", req, e);
						shapeFileQueueCallbackFunc(e);
					}
				}
			}); // End of async feature loop
	} // End of writeGeoJsonbyFeature()
		
	/*
	 * Function:	readShapeFile()
	 * Parameters:	Shapefile data object:
	 *					Shapefile name with path, 
	 *					DBF file name with path, 
	 *					Projection file name with path, 
	 *					JSON file with path, 
	 *					number of waits,
	 *					RIF logging object, 
	 *					express HTTP request object, 
	 *					express HTTP response object, 
	 *					start time, 
	 *					uuidV1, 
	 *					shapefile options,
	 *					Response object, 
	 *					shapefile number,,
	 *					key,
	 *					shpTotal
	 *					write time,
	 *					elapsed time
	 * Parameters:	Shapefile name with path, projection name with path, 
	 *				RIF logging object, express HTTP request object, express HTTP response object, start time, uuidV1, shapefile options, 
	 *				time to write file,
	 *				JSON file name with path, response object, shapefile number
	 * Returns:		Nothing
	 * Description: Read shapefile, process bounding box, convert to WGS84 if required
	 */
	var readShapeFile = function readShapeFile(shapefileData) {
		
		const nodeGeoSpatialServicesCommon = require('../lib/nodeGeoSpatialServicesCommon');
		
		scopeChecker(__file, __line, {
			serverLog: serverLog,
			httpErrorResponse: httpErrorResponse,
			ofields: response.fields	// For exception testing
		});
		
		/*
		 * Function: 	shapefileReader()
		 * Parameters:	Error, record (header/feature/end) from shapefile
		 * Returns: 	nothing
		 * Description: Shapefile reader function. Reads shapefile line by line; converting to WGS84 if required to minimise meory footprint
		 *
		 *				Must be within the scope of readShapeFile() because of process.nextTick()
		 */
		var shapefileReader = function shapefileReader(err, record) {

			var msg;
			
			scopeChecker(__file, __line, {
				shapefileData: shapefileData,
				reader: shapefileData["reader"],
				fileNoExt: shapefileData["fileNoExt"],
				mySrs: shapefileData["mySrs"],
				crss: shapefileData["crss"],
				shapefile_no: shapefileData["shapefile_no"],
				featureList: shapefileData["featureList"],
				file_list: response.file_list[shapefileData["shapefile_no"]-1],
				reproject: reproject,
				req: shapefileData["req"],
				response: response,
				message: response.message,
				serverLog: serverLog,
				httpErrorResponse: httpErrorResponse,
				nodeGeoSpatialServicesCommon: nodeGeoSpatialServicesCommon
			});
			
			if (err) {
				msg='ERROR! [' + shapefileData["uuidV1"] + '] in shapefile reader.read: ' + shapefileData["shapeFileName"];
				shapefileData["serverLog"].serverError2(__file, __line, "shapefileReader", 
					msg, shapefileData["req"], err);						
			}
			else if (record.bbox) { // Header						
				var lRec=JSON.stringify(record);
				shapefileData["recLen"]+=lRec.length;
				lRec=undefined;
				msg="shapefile read header for: " + shapefileData["fileNoExt"];
				response.message+="\n" + msg;					
				response.file_list[shapefileData["shapefile_no"]-1].geojson={type: "FeatureCollection", bbox: record.bbox, features: []};
				
				record=undefined;	
				
				process.nextTick(shapefileData["reader"].readRecord, shapefileReader);	// Read next record (first data record)
			}
			else if (record !== shapefile.end) {
				shapefileReadNextRecord(record, shapefileData, response, shapefileReader);
			}
			else { // At shapefile.end
				shapefileReadLastRecord(record, shapefileData, response);
			}
		} // End of shapefileReader() function
					
		var msg;
			
		if (shapefileData["lstart"]) {
			serverLog.serverError2(__file, __line, "readShapeFile", "Called > once: " + shapefileData["shapeFileName"], undefined, undefined);//Run > once - this should never occur
		}
		else {
			shapefileData["lstart"]=new Date().getTime();
		}	
		
		// Work out projection; convert to 4326 if required 
		shapefileData["prj"]=fs.readFileSync(shapefileData["projFileName"]);
		shapefileData["crss"]={
			"EPSG:2400": "+lon_0=15.808277777799999 +lat_0=0.0 +k=1.0 +x_0=1500000.0 +y_0=0.0 +proj=tmerc +ellps=bessel +units=m +towgs84=414.1,41.3,603.1,-0.855,2.141,-7.023,0 +no_defs",
			"EPSG:3006": "+proj=utm +zone=33 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs",
			"EPSG:4326": "+proj=longlat +ellps=WGS84 +datum=WGS84 +no_defs",
			"EPSG:3857": "+proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 +lon_0=0.0 +x_0=0.0 +y_0=0 +k=1.0 +units=m +nadgrids=@null +wktext  +no_defs"
		};
		
		/*		
		Add missing crss projections; created using PostGIS: 
		CREATE OR REPLACE VIEW missing_crss_projections AS
		SELECT '    shapefileData["crss"]["'||auth_name||':'||auth_srid||'"]="'||RTRIM(proj4text)||'";' as text FROM spatial_ref_sys
		 WHERE auth_srid NOT IN (2400, 3006, 4326, 3857)
		   AND auth_name = 'EPSG';
		\copy (SELECT * FROM missing_crss_projections) to missing_crss_projections.csv
		
		Copy into ../lib/missing_crss_projections.json
		*/
		var addMissingCrssProjections=require('../lib/addMissingCrssProjections');
		addMissingCrssProjections.addMissingCrssProjections(shapefileData);
		
		if (shapefileData["prj"]) {
			shapefileData["mySrs"]=srs.parse(shapefileData["prj"]);
			if (!shapefileData["mySrs"].srid) { // Add exceptions not spotted by srs.parse
				if (shapefileData["mySrs"].name.match(/British_National_Grid/)) {
					shapefileData["mySrs"].srid="27700";
				}
				else { // Error
					serverLog.serverError2(__file, __line, "readShapeFile", 
						"ERROR! no SRID for projection: " + shapefileData["mySrs"].name || "(no name)" + "; data: " + shapefileData["prj"] + " in shapefile: " +
						shapefileData["shapeFileName"], shapefileData["req"]);	
//						callback();	// Not needed - serverError2() raises exception 
				}
			}
			if (!shapefileData["crss"]["EPSG:" + shapefileData["mySrs"].srid]) { // Add missing projections to table
				serverLog.serverLog2(__file, __line, "readShapeFile", 
					"WARNING! Added SRID: " + shapefileData["mySrs"].srid + " for projection: " + (shapefileData["mySrs"].name || "(no name)") + 
					";\ndata: " + shapefileData["prj"] + " in shapefile: " +
					shapefileData["shapeFileName"], shapefileData["req"]);					
				shapefileData["crss"]["EPSG:" + shapefileData["mySrs"].srid] = shapefileData["mySrs"].proj4;
			}
		}
//		serverLog.serverLog2(__file, __line, "readShapeFile", 
//			"In readShapeFile(), call[" + shapefileData["shapefile_no"] + "] shapefile.read() for: " + shapefileData["shapeFileName"], shapefileData["req"]);
					
		// Now read shapefile

		shapefileData["reader"]=shapefile.reader(shapefileData["shapeFileName"], shapefileData["shapefile_options"]);
		response.file_list[shapefileData["shapefile_no"]-1].geojson=undefined;
		shapefileData["reader"].readHeader(shapefileReader); // Read shapefile

	} // End of readShapeFile()
	
	// End of queue functions

	// Set up async queue; 1 worker
	var shapeFileQueue = async.queue(function(shapefileData, shapeFileQueueCallback) {
		const nodeGeoSpatialServicesCommon = require('../lib/nodeGeoSpatialServicesCommon');
		
		var shapeFileQueueCallbackFunc = function shapeFileQueueCallbackFunc(err) {
			shapeFileQueueCallback(err);
		}
		scopeChecker(__file, __line, {
			serverLog: serverLog,
			httpErrorResponse: httpErrorResponse
		});
			
		try {
	//		response.message+="\nWaiting for shapefile [" + shapefileData.shapefile_no + "]: " + shapefileData.shapeFileName;
			response.message+="\nasync.queue() for write shapefile [" + shapefileData.shapefile_no + "]: " + shapefileData.shapeFileName;	
			shapefileData["callback"]=shapeFileQueueCallbackFunc; // Register callback for readShapeFile

			readShapeFile(shapefileData); // Does callback();
		}
		catch (e) {
			httpErrorResponse.httpErrorResponse(__file, __line, "shpConvert", 
				serverLog, 500, req, res, "async.queue(): Unhandled exception in readShapeFile()", e, response);			
		}
	}, 1 /* Single threaded - shapefileData needs to become an object */); // End of async.queue()

	/* 
	 * Function: 	shpConvertFieldProcessor().q.drain()
	 * Description: Async module drain function assign a callback at end of processing
	 *
	 *				Re-order shapefiles by total areas; check all bounding boxes are the same
	 * 				Setup and write XML config
	 *				Process errors and retiurn response
	 */
	shapeFileQueue.drain = function shapeFileQueueDrain() {
		const nodeGeoSpatialServicesCommon = require('../lib/nodeGeoSpatialServicesCommon');
		
		scopeChecker(__file, __line, {
			response: response,
			serverLog: serverLog,
			httpErrorResponse: httpErrorResponse
		});
		
		var os = require('os');
		var path = require('path');
		var dir = os.tmpdir() + "/shpConvert/" + response.fields["uuidV1"];
		var xmlConfig = {
			xmlFileName: 			"geoDataLoader.xml",
			xmlFileDir:				dir,
			uuidV1: 				response.fields["uuidV1"],
			geographyName: 			"To be added by user",
			geographyDescription: 	"To be added by user",
			shapeFileList: {
				shapeFiles: []
			},
			projection: {},
			defaultStudyArea: 		undefined,
			defaultComparisonArea: 	undefined,
			hierarchyTableName: 	undefined,
			hierarchyTableData: 	{
				hierarchyTableRow:	[]
			},
			simplificationFactor: 	response.fields["simplificationFactor"],
			quantization: 			response.fields["quantization"]			
		};
		
		try {
			var msg="All " + response.no_files + " shapefiles have been processed";
							// WE NEED TO WAIT FOR MULTIPLE FILES TO COMPLETE BEFORE RETURNING A RESPONSE

			// Re-order shapefiles by total areas; check all bounding boxes are the same
			var geolevels = [];
			var bbox=response.file_list[0].boundingBox;
			var bbox_errors=0;
			
			for (var i=0; i<response.file_list.length; i++) {
				geolevels[i] = {
					i: i,
					file_name: response.file_list[i].file_name,
					total_areas: response.file_list[i].total_areas,
					points:  response.file_list[i].points,
					geolevel_id: 0
				};
				
				if (bbox[0] != response.file_list[i].boundingBox[0] &&
				    bbox[1] != response.file_list[i].boundingBox[1] &&
				    bbox[2] != response.file_list[i].boundingBox[2] &&
				    bbox[3] != response.file_list[i].boundingBox[3]) { // Bounding box checks
					bbox_errors++;
					msg+="\nERROR: Bounding box " + i + ": [" +
						"xmin: " + response.file_list[i].geojson.bbox[0] + ", " +
						"ymin: " + response.file_list[i].geojson.bbox[1] + ", " +
						"xmax: " + response.file_list[i].geojson.bbox[2] + ", " +
						"ymax: " + response.file_list[i].geojson.bbox[3] + "];" +
						"\n is not the same as the first bounding box: " + 
						"xmin: " + response.file_list[i].geojson.bbox[0] + ", " +
						"ymin: " + response.file_list[i].geojson.bbox[1] + ", " +
						"xmax: " + response.file_list[i].geojson.bbox[2] + ", " +
						"ymax: " + response.file_list[i].geojson.bbox[3] + "];";
				}
			}
			if (bbox_errors > 0) {
				response.file_errors+=bbox_errors;
			}
			else {
				msg+="\nAll bounding boxes are the same";
			}
			
			var ngeolevels = geolevels.sort(function (a, b) { // Sort function: sort geolevels by area
				if (a.total_areas > b.total_areas) {
					return 1;
				}
				if (a.total_areas < b.total_areas) {
					return -1;
				}
				// a must be equal to b
				return 0;
			});
			
			for (var i=0; i<ngeolevels.length; i++) { // Create sorted ngeolevels array for geolevel_id for re=order (if required)		
				ngeolevels[i].geolevel_id=i+1;
				if (i == 0 && ngeolevels.length > 1 && ngeolevels[i].total_areas != 1) { // Geolevel 1 - Check that minimum resolution shapefile has only 1 area
					msg+="\nERROR: geolevel 1/" + ngeolevels.length + " shapefile: " + ngeolevels[i].file_name + " has >1 (" + ngeolevels[i].total_areas + ") area)";
					response.file_errors++;
				}
			}
			
			
			var defaultZoomLevels = [];
			for (var i=0; i<ngeolevels.length; i++) {	
				msg+="\nShape file [" + ngeolevels[i].i + "]: " + ngeolevels[i].file_name + 
					"; areas: " + ngeolevels[i].total_areas + 
					"; points: " + ngeolevels[i].points + 
					"; geolevel: " + ngeolevels[i].geolevel_id; 
				
				var lookupTableRow = [];
				var topojsonGeometries;
				if (response.file_list[ngeolevels[i].i].topojson[0].topojson &&
				    response.file_list[ngeolevels[i].i].topojson[0].topojson.objects &&
				    response.file_list[ngeolevels[i].i].topojson[0].topojson.objects.collection &&
				    response.file_list[ngeolevels[i].i].topojson[0].topojson.objects.collection.geometries &&
				    response.file_list[ngeolevels[i].i].topojson[0].topojson.objects.collection.geometries[0] &&
				    response.file_list[ngeolevels[i].i].topojson[0].topojson.objects.collection.geometries[0].properties) {
					topojsonGeometries=response.file_list[ngeolevels[i].i].topojson[0].topojson.objects.collection.geometries;
					msg+="; topojson has properties";
				}
				else if (response.file_list[ngeolevels[i].i].topojson[0].topojson == undefined) {
					msg+="; no topojson";	
				}
				else if (response.file_list[ngeolevels[i].i].topojson[0].topojson.objects == undefined) {
					msg+="; topojson has no objects";	
				}
				else if (response.file_list[ngeolevels[i].i].topojson[0].topojson.objects.collection == undefined) {
					msg+="; topojson has no collection";	
				}
				else if (response.file_list[ngeolevels[i].i].topojson[0].topojson.objects.collection.geometries == undefined) {
					msg+="; topojson has no geometries";	
				}
				else if (response.file_list[ngeolevels[i].i].topojson[0].topojson.objects.collection.geometries[0] == undefined) {
					msg+="; topojson has no geometries array";	
				}
				else if (response.file_list[ngeolevels[i].i].topojson[0].topojson.objects.collection.geometries[0].properties == undefined) {
					msg+="; topojson has no properties";	
				}
				else {
					msg+="; topojson has unknown state";	
				}
				
				if (topojsonGeometries) {
					msg+="\nProcessing topojsonGeometries: " + topojsonGeometries.length;
					for (var j=0; j<topojsonGeometries.length; j++) {
						var feature=topojsonGeometries[j];
						if (feature && feature.properties) {
							if (feature.properties.areaName && feature.properties.areaID) {
								lookupTableRow.push({
									areaID:		feature.properties.areaID,
									areaName: 	feature.properties.areaName
									});
							}
						}
					}	
				}
				response.file_list[ngeolevels[i].i].geolevel_id = ngeolevels[i].geolevel_id;
				
				xmlConfig.shapeFileList.shapeFiles[i] = {
					primaryKey:						"id",
					areas: 							ngeolevels[i].total_areas,
					points: 						ngeolevels[i].points,
					dbfFieldList: {
						dbfFields: response.file_list[ngeolevels[i].i].dbf_fields
					},
					geolevelId: 					ngeolevels[i].geolevel_id,
					geolevelName: 					(response.file_list[ngeolevels[i].i].areaName || 
														"To be added by user from dbfFieldList"),
					geolevelDescription: 			(response.file_list[ngeolevels[i].i].desc || 
														"To be added by user/from extended attributes file"),
					shapeFileAreaNameDescription: 	(response.file_list[ngeolevels[i].i].areaName_desc || 
														"To be added by user/from extended attributes file"),
					shapeFileName: 					ngeolevels[i].file_name,
					shapeFileDir: 					dir,
					lookupTable:					undefined,
					lookupTableDescriptionColumn:	undefined,
					lookupTableData: {
						lookupTableRow:				lookupTableRow,
					},
					shapeFileTable:					path.basename(ngeolevels[i].file_name.toUpperCase(), 
														path.extname(ngeolevels[i].file_name.toUpperCase())),
					shapeFileAreaIdColumn: 			(response.file_list[ngeolevels[i].i].areaID || 
														"To be added by user from dbfFieldList"),
					shapeFileAreaIdDescription: 	(response.file_list[ngeolevels[i].i].areaID_desc || 
														"To be added by user/from extended attributes file"),
					simplicationList: {
						simplification: []
					}
				}
				
				if (response.file_list[ngeolevels[i].i].topojson) {
					msg+="\nFile [" + ngeolevels[i].i + "] topojson zoomlevels: " + response.file_list[ngeolevels[i].i].topojson.length +
						"; from: " + response.file_list[ngeolevels[i].i].topojson[response.file_list[ngeolevels[i].i].topojson.length-1].zoomlevel +
						" to: " + response.file_list[ngeolevels[i].i].topojson[0].zoomlevel;
					for (var j=0; j<response.file_list[ngeolevels[i].i].topojson.length; j++) {	
						xmlConfig.shapeFileList.shapeFiles[i].simplicationList.simplification.push({
							zoomLevel: 			response.file_list[ngeolevels[i].i].topojson[j].zoomlevel,
							points: 			response.file_list[ngeolevels[i].i].topojson[j].topojson_points,
							arcs:	 			response.file_list[ngeolevels[i].i].topojson[j].topojson_arcs,
							length:				response.file_list[ngeolevels[i].i].topojson[j].topojson_length,
							runtime:			response.file_list[ngeolevels[i].i].topojson[j].topojson_runtime
						});							
					}					
				}					
			}
			
			// XML config setup
			xmlConfig.projection = {
				projectionName: 	response.file_list[0].projection_name,
				proj4: 				response.file_list[0].proj4,
				srid: 				response.file_list[0].srid,
				boundingBox: 		response.file_list[0].boundingBox
			}
			ofields["srid"]=response.file_list[0].srid;
			
			createXmlFile(xmlConfig); // Create XML configuration file
			
			// Final processing
			if (response.no_files == 0) { 
				response.message = msg + "\n" + response.message;
				msg="FAIL! No files attached\n";						
				response.message = msg + "\n" + response.message;
				response.fields=ofields;				// Add return fields
				response.file_errors++;					// Increment file error count	
				httpErrorResponse.httpErrorResponse(__file, __line, "req.busboy.on('finish')", 
					serverLog, 500, req, res, msg, undefined, response);							
				return;						
			}
			else { 				
				msg+="\nshpConvertFieldProcessor().shapeFileQueue.drain() OK";
				response.message = response.message + "\n" + msg;
				nodeGeoSpatialServicesCommon.responseProcessing(req, res, response, serverLog, httpErrorResponse, ofields);
			}
		}
		catch (e) {
			msg+='\nCaught exception: ' + e.message;
			response.file_errors++;
			response.message = msg + "\n" + response.message;			
			httpErrorResponse.httpErrorResponse(__file, __line, "shpConvertFieldProcessor().shapeFileQueue.drain()", 
				serverLog, 500, req, res, msg, e, response);
		}
	} // End of shpConvertFieldProcessor().shapeFileQueue.drain()	

	var shapefile_count=0;
	var shapefile_total=0;

	for (var key in shpList) {
		shapefile_total++;
	}
//	serverLog.serverLog2(__file, __line, "readShapeFile", 
//		"In readShapeFile(), queue items: " + shapefile_total);	
	for (var key in shpList) {
		shapefile_count++;
		var shapefile_no=shapefile_count;

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
			var dir=os.tmpdir() + "/shpConvert/" + ofields["uuidV1"] + "/" + key;
			var shapefileData = {
				shapeFileBaseName: key + ".shp", 
				jsonFileBaseName: key + ".json", 
				topojsonFileBaseName: key + ".topojson", 
				shapeFileName: dir + "/" + key + ".shp", 
				dbfFileName: dir + "/" + key + ".dbf", 
				projFileName: dir + "/" + key + ".prj", 
				jsonFileName: dir + "/" + key + ".json",
				topojsonFileName: dir + "/" + key + ".topojson",
				waits: 0, 
				req: req,
				res: res, 
				lstart: undefined, 
				uuidV1: ofields["uuidV1"], 
				shapefile_options: shapefile_options, 
				response: response, 
				shapefile_no: shapefile_no,
				key: key,
				shpTotal: shpTotal,
				total_areas: 0,
				dbf_fields: 0,
				geolevel_id: 0,
				desc: undefined,
				areaID: undefined,
				areaName: undefined,
				areaIDDesc: undefined,
				areaNameDesc: undefined,
				callback: undefined,
				recLen: 0,
				fileNoExt: undefined,
				featureList: [],
				mySrs: undefined,
				prj: undefined,
				reader: undefined,
				elapsedReadTime: undefined,
				elapsedJsonSaveTime: undefined,
				topojson_options: ofields["topojson_options"]
			}
		
			shapefileData["fileNoExt"] = path.basename(shapefileData["shapeFileName"]);	
			
			response.file_list[shapefile_no-1] = {
				file_name: key + ".shp",
	//			geojson: '',
				file_size: '',
				transfer_time: '',
				geojson_time: '',
				desc: undefined,
				areaID: undefined,
				areaName: undefined,
				areaID_desc: undefined,
				areaName_desc: undefined,
				uncompress_time: undefined,
				uncompress_size: undefined
			};
			response.file_list[shapefile_no-1].transfer_time=shpList[key].transfer_time;
			
			response.message+="\nProcessing shapefile [" + shapefile_no + "]: " + shapefileData["shapeFileName"];		
			response.no_files=shpTotal;				// Add number of files process to response
			response.fields=ofields;				// Add return fields
			response.file_errors+=rval.file_errors;

			// Populate shapefile area fields (areaID, areaName and 3x descriptions) from fields
			var areaFieldsList = ['desc', 'areaID', 'areaName', 'areaID_desc', 'areaName_desc'];
			for (var i=0; i< areaFieldsList.length; i++) {
				var areaKey=areaFieldsList[i];
				var areaField=key + "_" + areaKey;
				if (ofields[areaField]) {
					if (areaKey == "areaID" || areaKey == "areaName") {
						ofields[areaField]=ofields[areaField].toUpperCase();
					}
					shapefileData[areaKey]=ofields[areaField];
					var fileNo=response.file_list[shapefile_no-1];
					fileNo[areaKey]=ofields[areaField];
					response.message+="\nFile [" + (shapefile_no-1) + "]; key: " + areaKey + "; areaField: " + areaField + "=" + fileNo[areaKey];
				}
				else {
					response.message+="\nareaField: " + areaField + " not found";
				}
			}
			
			// Add to queue			

//			serverLog.serverLog2(__file, __line, "readShapeFile", 
//				"In readShapeFile(), shapeFileQueue shapefile [" + shapefile_no + "/" + shapefile_total + "]: " + shapefileData["shapeFileName"]);			
			shapeFileQueue.push(shapefileData, function shapeFileQueuePush(err) {
				if (err) {
					var msg='ERROR! in readShapeFile(): ' + err.message + "\nStack: " + err.stack;
						
					response.message+="\n" + msg;	
					
					response.file_errors++;					// Increment file error count
					serverLog.serverLog2(__file, __line, "shpConvertFieldProcessor().shapeFileQueue.push()", msg, 
						shapefileData["req"], err);	
				} // End of err		
//				else {
//					response.message+="\nXXXX Completed processing shapefile[" + shapefileData["shapefile_no"] + "]: " + shapefileData["shapeFileName"];
//				}
			});		
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
} // End of shpConvertCheckFiles()
