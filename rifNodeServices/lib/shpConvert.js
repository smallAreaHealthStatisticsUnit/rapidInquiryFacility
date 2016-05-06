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
shpConvertFieldProcessor=function(fieldname, val, shapefile_options, ofields, response, req, rifLog) {
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
}

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
shpConvert = function(ofields, d_files, response, req, res, shapefile_options) {							
	var shpList = {};
	var shpTotal=0;		

	const serverLog = require('../lib/serverLog'),
	      httpErrorResponse = require('../lib/httpErrorResponse'),
		  async = require('async'); 

	if (!req) {
		throw new Error("No HTTP request object [out of scope]");
	}		  
	if (!res) {
		throw new Error("No HTTP response object [out of scope]");
	}
	if (!ofields["uuidV1"]) { // Generate UUID
		ofields["uuidV1"]=serverLog.generateUUID();
	}

	// Set up async queue; 1 worker
	var shapeFileComponentQueue = async.queue(function(fileData, shapeFileComponentQueueCallback) {
		shpConvertFileProcessor(fileData["d"], fileData["shpList"], fileData["shpTotal"], fileData["response"], fileData["uuidV1"], 
			fileData["req"], fileData["serverLog"], fileData["httpErrorResponse"],
			shapeFileComponentQueueCallback);	
	}, 1 /* Single threaded - fileData needs to become an object */); // End of async.queue()
	
	/*
	 * Function:	shapeFileComponentQueue.drain()
	 * Parameters: 	None
	 * Returns:		N/A; calls shpConvertCheckFiles() to check which files and extensions are present, convert shapefile to geoJSON, simplify etc
	 * Description: Async queue drain function; mwhen when all shapefile components have been processed 
	 */
	shapeFileComponentQueue.drain = function() {
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
		if (rval.file_errors > 0 ) {
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
			httpErrorResponse: 	httpErrorResponse
		}
		
		fileData.i=i;
		response.message+="\nQueued (shapeFileComponentQueue) file for shpConvertFileProcessor[" + fileData["i"] + "]: " + fileData.d.file.file_name;
		// Add to queue			
//		serverLog.serverLog2(__file, __line, "shpConvert", 
//		"In shpConvertFileProcessor(), shapeFileComponentQueue[" + fileData["i"] + "]: " + fileData.d.file.file_name);

		shapeFileComponentQueue.push(fileData, function(err) {
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
 * Function: 	scopeChecker()
 * Parameters:	file, line called from, named array object to scope checked mandatory, optional array
 * Description: Scope checker function. Throws error if not in scope
 */
scopeChecker = function(fFile, sLine, array, optionalArray) {
	var errors=0;optionalArray
	var undefinedKeys="";
	var msg="";
	
	for (var key in array) {
		if (typeof array[key] == "undefined") {
			undefinedKeys+=key + ", ";
			errors++;
		}
	}
	if (errors > 0) {
		msg+=errors + " variable(s) not in scope: " + undefinedKeys;
	}
	if (array["serverLog"]) { // Check error and logging in scope
		if (typeof array["serverLog"].serverError2 != "function") {
			msg+="\nserverLog.serverError2 is not a function: " + typeof array["serverLog"];
			errors++;
		}
		if (typeof array["serverLog"].serverLog2 != "function") {
			msg+="\nserverLog.serverLog2 is not a function: " + typeof array["serverLog"];
			errors++;
		}
		if (typeof array["serverLog"].serverError != "function") {
			msg+="\nserverLog.serverError is not a function: " + typeof array["serverLog"];
			errors++;
		}
		if (typeof array["serverLog"].serverLog != "function") {
			msg+="\nserverLog.serverLog is not a function: " + typeof array["serverLog"];
			errors++;
		}		
	}
	if (array["httpErrorResponse"]) { // Check httpErrorResponse in scope
		if (typeof array["httpErrorResponse"].httpErrorResponse != "function") {
			msg+="\httpErrorResponse.httpErrorResponse is not a function: " + typeof array["httpErrorResponse"];
			errors++;
		}
	}	
	// Check callback
	if (array["callback"]) { // Check callback is a function if in scope
		if (typeof array["callback"] != "function") {
			serverLog.serverError2(__file, __line, "createWriteStreamWithCallback", 
				"Mandatory callback (" + typeof(callback) + "): " + (callback.name || "anonymous") + " is in use but is not a function: " + 
				typeof callback, req, undefined);
			errors++;	
		}
	}	
	// Check callback
	if (optionalArray && optionalArray["callback"]) { // Check callback is a function if in scope
		if (typeof optionalArray["callback"] != "function") {
			serverLog.serverError2(__file, __line, "createWriteStreamWithCallback", 
				"optional callback (" + typeof(callback) + "): " + (callback.name || "anonymous") + " is in use but is not a function: " + 
				typeof callback, req, undefined);
			errors++;	
		}
	}	
	
	if (errors > 0) {
		if (array["serverLog"] && array["req"] && typeof array["serverLog"].serverLog2 == "function") {
			array["serverLog"].serverError2(fFile, sLine, "scopeChecker", 
				msg, array["req"], undefined);
		}
		else {
			throw new Error(msg);
		}
	}	
}
	
/*
 * Function:	shpConvertFileProcessor()
 * Parameters:	d object (temporary processing data), Shapefile list, total shapefiles, response object, 
 *				uuidV1, HTTP request object, serverLog object, httpErrorResponse object, callback
 * Returns:		Rval object { file_errors, msg, total shapefiles }
 * Description: Note which files and extensions are present, generate RFC412v1 UUID if required, save shapefile to temporary directory
 *				Called once per file from shapeFileComponentQueue
 */
shpConvertFileProcessor = function(d, shpList, shpTotal, response, uuidV1, req, serverLog, httpErrorResponse, shapeFileComponentQueueCallback) {
		
	scopeChecker(__file, __line, {
		serverLog: serverLog,
		httpErrorResponse: httpErrorResponse
	});	
		
	var streamWriteFileWithCallback = require('../lib/streamWriteFileWithCallback');
		  
	/*
	 * Function:	createTemporaryDirectory()
	 * Parameters:	Directory component array [$TEMP/shpConvert, <uuidV1>, <fileNoext>]
	 * Returns:		Final directory (e.g. $TEMP/shpConvert/<uuidV1>/<fileNoext>)
	 * Description: Create temporary directory (for shapefiles)
	 */
	createTemporaryDirectory = function(dirArray, response, req) {
		const fs = require('fs');

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
						serverLog.serverError2(__file, __line, "createTemporaryDirectory", 
							"ERROR: Cannot create directory: " + tdir + "; error: " + e.message, req);
//							shapeFileComponentQueueCallback();		// Not needed - serverError2() raises exception 
					}			
				}
				else {
					serverLog.serverError2(__file, __line, "createTemporaryDirectory", 
						"ERROR: Cannot access directory: " + tdir + "; error: " + e.message, req);
//						 shapeFileComponentQueueCallback();		// Not needed - serverError2() raises exception 					
				}
			}
		}
		return tdir;
	} /* End of createTemporaryDirectory() */;
	
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
	
//	
// Create directory: $TEMP/shpConvert/<uuidV1>/<fileNoext> as required
//
	var dirArray=[os.tmpdir() + "/shpConvert", uuidV1, fileNoext];
	dir=createTemporaryDirectory(dirArray, response, req);
	
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
		streamWriteFileWithCallback.streamWriteFileWithCallback(file, d.file.file_data, serverLog, uuidV1, req, response, 
			undefined /* Records */, shapeFileComponentQueueCallback);
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
shpConvertCheckFiles=function(shpList, response, shpTotal, ofields, serverLog, httpErrorResponse, req, res, shapefile_options) {
	const os = require('os'),
	      path = require('path'),
	      fs = require('fs'),
	      shapefile = require('shapefile'),
	      reproject = require('reproject'),
	      srs = require('srs'),
	      async = require('async'),
		  streamWriteFileWithCallback = require('../lib/streamWriteFileWithCallback');
		  
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
	var createXmlFile = function(xmlConfig) {
		
		scopeChecker(__file, __line, {
			serverLog: serverLog,
			httpErrorResponse: httpErrorResponse
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
		
		serverLog.serverLog2(__file, __line, "XML config [xml] >>>\n" + xmlDoc + "\n<<< end of XML config [xml]" +
			"\nXML config [json] >>>\n" + JSON.stringify(xmlConfig, null, 4) + "\n<<< end of json config [xml]");
			
		fs.writeFileSync(xmlConfig.xmlFileDir + "/" + xmlConfig.xmlFileName, xmlDoc);
	} // End of createXmlFile()
	
	/*
	 * Function:	simplifyGeoJSON()
	 * Parameters:	shapefile (base for geojson etc), response, shapefileData object, 
	 *				topojson options (may be undefined), callback (may be undefined)
	 * Returns:		Nothing
	 * Description:	Simplify geoJSOn to topoJSON optimised for zoomlevel 9,
					Run callback
	 
	 Using to postGIS simplify caluylator as a base:
	 
		SELECT * FROM rif40_geo_pkg.rif40_zoom_levels();
psql:alter_scripts/v4_0_alter_5.sql:134: INFO:  [DEBUG1] rif40_zoom_levels(): [60001] latitude: 0
 zoom_level | latitude |    tiles     | degrees_per_tile | m_x_per_pixel_est | m_x_per_pixel | m_y_per_pixel |   m_x    |   m_y    | simplify_tolerance |      scale
------------+----------+--------------+------------------+-------------------+---------------+---------------+----------+----------+--------------------+------------------
		  0 |        0 |            1 |              360 |            156412 |        155497 |               | 39807187 |          |               1.40 | 1 in 591,225,112
		  1 |        0 |            4 |              180 |             78206 |         77748 |               | 19903593 |          |               0.70 | 1 in 295,612,556
		  2 |        0 |           16 |               90 |             39103 |         39136 |         39070 | 10018754 | 10001966 |               0.35 | 1 in 148,800,745
		  3 |        0 |           64 |               45 |             19552 |         19568 |         19472 |  5009377 |  4984944 |               0.18 | 1 in 74,400,373
		  4 |        0 |          256 |             22.5 |              9776 |          9784 |          9723 |  2504689 |  2489167 |               0.09 | 1 in 37,200,186
		  5 |        0 |         1024 |            11.25 |              4888 |          4892 |          4860 |  1252344 |  1244120 |               0.04 | 1 in 18,600,093
		  6 |        0 |         4096 |            5.625 |              2444 |          2446 |          2430 |   626172 |   622000 |              0.022 | 1 in 9,300,047
		  7 |        0 |        16384 |            2.813 |              1222 |          1223 |          1215 |   313086 |   310993 |              0.011 | 1 in 4,650,023
		  8 |        0 |        65536 |            1.406 |               611 |           611 |           607 |   156543 |   155495 |             0.0055 | 1 in 2,325,012
		  9 |        0 |       262144 |            0.703 |               305 |           306 |           304 |    78272 |    77748 |             0.0027 | 1 in 1,162,506
		 10 |        0 |      1048576 |            0.352 |               153 |           153 |           152 |    39136 |    38874 |             0.0014 | 1 in 581,253
		 11 |        0 |      4194304 |            0.176 |                76 |            76 |            76 |    19568 |    19437 |            0.00069 | 1 in 290,626
		 12 |        0 |     16777216 |            0.088 |                38 |            38 |            38 |     9784 |     9718 |            0.00034 | 1 in 145,313
		 13 |        0 |     67108864 |            0.044 |                19 |            19 |            19 |     4892 |     4859 |            0.00017 | 1 in 72,657
		 14 |        0 |    268435456 |            0.022 |               9.5 |           9.6 |           9.5 |     2446 |     2430 |          0.0000858 | 1 in 36,328
		 15 |        0 |   1073741824 |            0.011 |               4.8 |           4.8 |           4.7 |     1223 |     1215 |          0.0000429 | 1 in 18,164
		 16 |        0 |   4294967296 |            0.005 |               2.4 |           2.4 |           2.4 |      611 |      607 |          0.0000215 | 1 in 9,082
		 17 |        0 |  17179869184 |            0.003 |              1.19 |          1.19 |          1.19 |      306 |      304 |          0.0000107 | 1 in 4,541
		 18 |        0 |  68719476736 |           0.0014 |              0.60 |          0.60 |          0.59 |      153 |      152 |          0.0000054 | 1 in 2,271
		 19 |        0 | 274877906944 |          0.00069 |              0.30 |          0.30 |          0.30 |       76 |       76 |          0.0000027 | 1 in 1,135
		 
	For zoomlevel 9 the area at the equator is  78272 x 77748 = 6.085 square km and a pixel is 306 x 304 = 0.093 square km
	In steradians = (0.093 / (510,072,000 * 12.56637) [area of earth] = 1.4512882642054046732729181896167e-11 steradians
	 */
	var simplifyGeoJSON = function(shapefile, response, shapefileData, topojson_options, callback) {
		scopeChecker(__file, __line, {
			shapefile: shapefile,
			topojsonFileName: shapefileData["topojsonFileName"],
			response: response,
			shapefileData: shapefileData,
			geojson: shapefile.geojson,
			features: shapefile.geojson.features,
			file_no: response.file_list[shapefileData["shapefile_no"]-1]
		} /* Manadatory */,
		{
			callback: callback
		} /* Optional */);	
		
		var topojson = require('topojson'),
			stderrHook = require('../lib/stderrHook');
			
// Default geo2TopoJSON options (see topology Node.js module)
		if (!topojson_options) {
			topojson_options = {
				verbose:      true,
				quantization: 1e6,	
				simplify: 1.451e-11 // For zoomlevel 9
			}; 		
		}
		
		var records
		if (shapefile.geojson.features) {
			records=shapefile.geojson.features.length;
		}
		
// Add stderr hook to capture debug output from topoJSON	
		var stderr = stderrHook.stderrHook(function(output, obj) { 
			output.str += obj.str;
		});
	
		// Re-route topoJSON stderr to stderr.str
		stderr.disable();
		
		shapefile.topojson = topojson.topology({   // Convert geoJSON to topoJSON
			collection: shapefile.geojson
			}, topojson_options);				
		stderr.enable(); 				   // Re-enable stderr
		
		response.message+="\nConvert to topojson:\n" + stderr.str();  // Get stderr as a string	
		stderr.clean();						// Clean down stderr string
		stderr.restore();                   // Restore normal stderr functionality 		
		
	// This need to be replaced with write record by record and then do the callback here
	// We can then also remove the geojson

		if (response.file_list[shapefileData["shapefile_no"]-1].topojson) {
			response.file_list[shapefileData["shapefile_no"]-1].topojson_length=JSON.stringify(shapefile.topojson).length;
				shapefile.geojson.features=undefined;
		}
			
//		var end=new Date().getTime();

//		shapefileData["elapsedTime"]=(end - shapefileData["lstart"])/1000; // in S
//		shapefileData["writeTime"]=(end - shapefileData["lstart"])/1000; // in S	

// Write topoJSON file				
		streamWriteFileWithCallback.streamWriteFileWithCallback(shapefileData["topojsonFileName"], 
			JSON.stringify(response.file_list[shapefileData["shapefile_no"]-1].topojson), 
			serverLog, shapefileData["uuidV1"], shapefileData["req"], response, records, callback);		
	} // End of simplifyGeoJSON()
	
	/*
	 * Function:	shapefileReadNextRecord()
	 * Parameters:	shapefile record, shape file data object, response object, reader function 
	 * Description:	Read next shapefile record, call reader function
	 */
	var shapefileReadNextRecord = function(record, shapefileData, response, shapefileReader) {
		var msg;
		var recNo=shapefileData["featureList"].length+1;
		var lRec=JSON.stringify(record);
		scopeChecker(__file, __line, {
			serverLog: serverLog,
			httpErrorResponse: httpErrorResponse
		});		
		
		shapefileData["recLen"]+=lRec.length;
		lRec=undefined;
		
		var doTrace=false;
		
		msg="shapefile read [" + recNo + "] for: " + shapefileData["fileNoExt"] + "; size: " + shapefileData["recLen"];
		if (((recNo/1000)-Math.floor(recNo/1000)) == 0 || recNo == 1) { // Print read record diagnostics every 1000 shapefile records
			doTrace=true;
			if (shapefileData["recLen"] > 50*1024*1024) { // 50 MB
				serverLog.serverLog2(__file, __line, "readShapeFile", "In shapefileReadNextRecord(), " + msg, shapefileData["req"]);
			}
			response.message+="\n" + msg;
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

					msg+=
						"Bounding box (4326): " + 
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
	var shapefileReadLastRecord = function(record, shapefileData, response) {
		var msg;
		
		scopeChecker(__file, __line, {
			serverLog: serverLog,
			httpErrorResponse: httpErrorResponse
		});
		
		response.file_list[shapefileData["shapefile_no"]-1].geojson.features=shapefileData["featureList"];
		shapefileData["featureList"]=undefined;
		var recNo=response.file_list[shapefileData["shapefile_no"]-1].geojson.features.length;				
		msg="shapefile read [" + recNo	+ "] completed for: " + shapefileData["fileNoExt"] + "; geoJSON length: " + shapefileData["recLen"];
		if (shapefileData["recLen"] > 50*1024*1024) { // 50 MB
			serverLog.serverLog2(__file, __line, "shapefileReadLastRecord", "In shapefileReader(), " + msg, shapefileData["req"]);
		}
		response.file_list[shapefileData["shapefile_no"]-1].geojson_length=shapefileData["recLen"];
		response.message+="\n" + msg;
		shapefileData["reader"].close(function(err) {
			if (err) {
				var msg='ERROR! [' + shapefileData["uuidV1"] + '] in shapefile reader.close: ' + shapefileData["shapeFileName"];
				serverLog.serverError2(__file, __line, "shapefileReadLastRecord", 
					msg, shapefileData["req"], err);							
			}	
			shapefileData["reader"]=undefined; // Release for gc
			
			var end = new Date().getTime();
			shapefileData["elapsedTime"]=(end - shapefileData["lstart"])/1000; // in S
			
			if (response.file_list[shapefileData["shapefile_no"]-1].geojson.bbox) { // Check bounding box present
				var msg="File: " + shapefileData["shapeFileName"] + 
					"\nwritten after: " + shapefileData["writeTime"] + " S; total time: " + shapefileData["elapsedTime"] + 
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
						dbf_fields.push(key);
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

				response.message+="\n" + msg;

				// Convert to geoJSON and return
				response.file_list[shapefileData["shapefile_no"]-1].file_size=fs.statSync(shapefileData["shapeFileName"]).size;
				response.file_list[shapefileData["shapefile_no"]-1].geojson_time=shapefileData["elapsedTime"];
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
	var writeGeoJsonbyFeature = function (shapefileData, response) {
		
		scopeChecker(__file, __line, {
			serverLog: serverLog,
			httpErrorResponse: httpErrorResponse,
			shapefileData: shapefileData,
			response: response,
			file_no: response.file_list[shapefileData["shapefile_no"]-1],
			geojson: response.file_list[shapefileData["shapefile_no"]-1].geojson,
			bbox: response.file_list[shapefileData["shapefile_no"]-1].geojson.bbox
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
			function (value, index, seriesCallback) {
					var seriesCallbackFunc = function seriesCallbackFunc(e) { // Cause seriesCallback to be named
						seriesCallback(e);
					}
					
					try {						
						z++;
						y++;
						if (z == 1) {
							// Write header
							response.message+="\nWrite header for: " + shapefileData["jsonFileName"]; 
							wStream=streamWriteFileWithCallback.createWriteStreamWithCallback(shapefileData["jsonFileName"], 
								undefined /* data: do not undefine! */, 
								serverLog, shapefileData["uuidV1"], shapefileData["req"], response, numFeatures, 
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
					} catch (e) {
						return seriesCallbackFunc(e);
					}
			}, 
			function (err) {																	/* Callback at end */
				if (err) {
					serverLog.serverError2(__file, __line, "streamWriteFilePieceWithCallbackasync.forEachOfSeries", err.message, req, undefined);
				}
				else { // Write footer
					// Callbacks:
					// * streamWriteFilePieceWithCallback (footer) calls:
					// 	 * testFunc; calls:
					//     * topoFunction; calls:
					//       * shapeFileQueueCallbackFunc which run shapeFileQueueCallback
					// Nothing is now run at stream end!
					
					var shapeFileQueueCallbackFunc = function shapeFileQueueCallbackFunc() {
						scopeChecker(__file, __line, {	
							callback: shapefileData["callback"],
							message: response.message
						});
						response.message+=";\nRun shapeFileQueueCallback callback()";
						shapefileData["callback"]();								
					}	
					var topoFunction=function topoFunction() {
						// Create topoJSON
						simplifyGeoJSON(response.file_list[shapefileData["shapefile_no"]-1], response, shapefileData, 
							undefined /* topojson_options */, shapeFileQueueCallbackFunc /* Callback */);							
					}							
// For testing					
					var testFunc = function testFunc() {
//								console.error("Creating: " + shapefileData["jsonFileName"] + ".2");
						streamWriteFileWithCallback.streamWriteFileWithCallback(shapefileData["jsonFileName"] + ".2", 
							JSON.stringify(response.file_list[shapefileData["shapefile_no"]-1].geojson), 
							serverLog, shapefileData["uuidV1"], shapefileData["req"], response, 
							numFeatures /* records */,
							topoFunction /* callback */);
					}			

					response.message+="\nWrite footer for: " + shapefileData["jsonFileName"]; 
					streamWriteFileWithCallback.streamWriteFilePieceWithCallback(shapefileData["jsonFileName"], 
							footer, 
							wStream,
							serverLog, shapefileData["uuidV1"], shapefileData["req"], response, 
							true /* lastPiece */, lstart, topoFunction /* testFunc */ /* callback */);
							

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
	var readShapeFile = function(shapefileData) {
		
		scopeChecker(__file, __line, {
			serverLog: serverLog,
			httpErrorResponse: httpErrorResponse
		});
		
		/*
		 * Function: 	shapefileReader()
		 * Parameters:	Error, record (header/feature/end) from shapefile
		 * Returns: 	nothing
		 * Description: Shapefile reader function. Reads shapefile line by line; converting to WGS84 if required to minimise meory footprint
		 *
		 *				Must be within the scope of readShapeFile() because of process.nextTick()
		 */
		var shapefileReader = function(err, record) {

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
				httpErrorResponse: httpErrorResponse
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
		
		const v8 = require('v8');	
			
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
		
		if (shapefileData["prj"]) {
			shapefileData["mySrs"]=srs.parse(shapefileData["prj"]);
			if (!shapefileData["mySrs"].srid) { // 
				if (shapefileData["mySrs"].name == "British_National_Grid") {
					shapefileData["mySrs"].srid="27700";
				}
				else { // Error
					serverLog.serverError2(__file, __line, "readShapeFile", 
						"ERROR! no SRID for projection data: " + shapefileData["prj"] + " in shapefile: " +
						shapefileData["shapeFileName"], shapefileData["req"]);	
//						callback();	// Not needed - serverError2() raises exception 
				}
			}
			shapefileData["crss"]["EPSG:" + shapefileData["mySrs"].srid] = shapefileData["mySrs"].proj4;
		}
		serverLog.serverLog2(__file, __line, "readShapeFile", 
			"In readShapeFile(), call[" + shapefileData["shapefile_no"] + "] shapefile.read() for: " + shapefileData["shapeFileName"], shapefileData["req"]);
					
		// Now read shapefile

		shapefileData["reader"]=shapefile.reader(shapefileData["shapeFileName"], shapefileData["shapefile_options"]);
		response.file_list[shapefileData["shapefile_no"]-1].geojson=undefined;
		shapefileData["reader"].readHeader(shapefileReader); // Read shapefile

	} // End of readShapeFile()
	
	// End of queue functions

	// Set up async queue; 1 worker
	var shapeFileQueue = async.queue(function(shapefileData, shapeFileQueueCallback) {
		
		scopeChecker(__file, __line, {
			serverLog: serverLog,
			httpErrorResponse: httpErrorResponse
		});
			
//		response.message+="\nWaiting for shapefile [" + shapefileData.shapefile_no + "]: " + shapefileData.shapeFileName;
		response.message+="\nasync.queue() for write shapefile [" + shapefileData.shapefile_no + "]: " + shapefileData.shapeFileName;	
		shapefileData["callback"]=shapeFileQueueCallback; // Register callback for readShapeFile

		readShapeFile(shapefileData); // Does callback();
	}, 1 /* Single threaded - shapefileData needs to become an object */); // End of async.queue()

	/* 
	 * Function: 	shpConvertFieldProcessor().q.drain()
	 * Description: Async module drain function assign a callback at end of processing
	 *
	 *				Re-order shapefiles by total areas; check all bounding boxes are the same
	 * 				Setup and write XML config
	 *				Process errors and retiurn response
	 */
	shapeFileQueue.drain = function() {
		
		scopeChecker(__file, __line, {
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
			simplicationList: {
				simplification: []
			},
			defaultStudyArea: 		undefined,
			defaultComparisonArea: 	undefined,
			hierarchyTableName: 	undefined,
			hierarchyTableData: 	{
				hierarchyTableRow:	[]
			}
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
			
			var ngeolevels = geolevels.sort(function (a, b) {
				if (a.total_areas > b.total_areas) {
					return 1;
				}
				if (a.total_areas < b.total_areas) {
					return -1;
				}
				// a must be equal to b
				return 0;
			});
			for (var i=0; i<ngeolevels.length; i++) {		
				ngeolevels[i].geolevel_id=i+1;
				if (i == 0 && ngeolevels.length > 1 && ngeolevels[i].total_areas != 1) { // Geolevel 1 - Check that minimum resolution shapefile has only 1 area
					msg+="\nERROR: geolevel 1/" + ngeolevels.length + " shapefile: " + ngeolevels[i].file_name + " has >1 (" + ngeolevels[i].total_areas + ") area)"
					response.file_errors++;
				}
				msg+="\nShape file [" + ngeolevels[i].i + "]: " + ngeolevels[i].file_name + 
					"; areas: " + ngeolevels[i].total_areas + 
					"; points: " + ngeolevels[i].points + 
					"; geolevel: " + ngeolevels[i].geolevel_id; 
				response.file_list[ngeolevels[i].i].geolevel_id = ngeolevels[i].geolevel_id;
				
				xmlConfig.shapeFileList.shapeFiles[i] = {
					primaryKey:						"id",
					areas: 							ngeolevels[i].total_areas,
					points: 						ngeolevels[i].points,
					dbfFieldList: {
						dbfFields: response.file_list[ngeolevels[i].i].dbf_fields
					},
					uniqueKey:						"To be added by user from dbfFieldList",
					uniqueName:						"To be added by user from dbfFieldList",
					geolevelId: 					ngeolevels[i].geolevel_id,
					geolevelName: 					"To be added by user",
					geolevelDescription: 			"To be added by user",
					shapeFileName: 					ngeolevels[i].file_name,
					shapeFileDir: 					dir,
					lookupTable:					undefined,
					lookupTableDescriptionColumn:	undefined,
					lookupTableData: {
						lookupTableRow:	[]
					},
					shapeFileTable:					path.basename(ngeolevels[i].file_name.toUpperCase(), path.extname(ngeolevels[i].file_name.toUpperCase())),
					shapeFileAreaIdColumn: 			"To be added by user from dbfFieldList",
					shapeFileDescriptionColumn: 	"To be added by user from dbfFieldList"
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
			var defaultZoomLevels = [6,8,11];
			for (var i=0; i<defaultZoomLevels.length; i++) {	
				xmlConfig.simplicationList.simplification[i] = {
					zoomLevel: 			defaultZoomLevels[i],
					points: 			undefined,
					simplifyPrecision: 	undefined
				}
			}
			
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
			else if (response.field_errors == 0 && response.file_errors == 0) { // OK
				msg+="\nshpConvertFieldProcessor().shapeFileQueue.drain() OK";
				response.message = response.message + "\n" + msg;
				
				if (!shapefile_options.verbose) {
					response.message="";	
				}
				else {		
					serverLog.serverLog2(__file, __line, "shpConvertFieldProcessor().shapeFileQueue.drain()", 
						"Diagnostics enabled; diagnostics >>>\n" +
						response.message + "\n<<< End of diagnostics");	
				}
			
				if (!req.finished) { // Reply with error if httpErrorResponse.httpErrorResponse() NOT already processed					
					var output = JSON.stringify(response);// Convert output response to JSON 
	// Need to test res was not finished by an expection to avoid "write after end" errors			
					res.write(output);                  // Write output  
					res.end();	
				}
				else {
					serverLog.serverError2(__file, __line, "shpConvertFieldProcessor().shapeFileQueue.drain()", 
						"FATAL! Unable to return OK reponse to user - httpErrorResponse() already processed", 
						req);
				}				
			}
			else if (response.field_errors > 0 && response.file_errors > 0) {
				msg+="\nFAIL! Field processing ERRORS! " + response.field_errors + 
					" and file processing ERRORS! " + response.file_errors + "\n" + msg;
				response.message = msg + "\n" + response.message;						
				httpErrorResponse.httpErrorResponse(__file, __line, "shpConvertFieldProcessor().shapeFileQueue.drain()", 
					serverLog, 500, req, res, msg, undefined, response);				  
			}				
			else if (response.field_errors > 0) {
				msg+="\nFAIL! Field processing ERRORS! " + response.field_errors + "\n" + msg;
				response.message = msg + "\n" + response.message;
				httpErrorResponse.httpErrorResponse(__file, __line, "shpConvertFieldProcessor().shapeFileQueue.drain()", 
					serverLog, 500, req, res, msg, undefined, response);				  
			}	
			else if (response.file_errors > 0) {
				msg+="\nFAIL! File processing ERRORS! " + response.file_errors + "\n" + msg;
				response.message = msg + "\n" + response.message;					
				httpErrorResponse.httpErrorResponse(__file, __line, "shpConvertFieldProcessor().shapeFileQueue.drain()", 
					serverLog, 500, req, res, msg, undefined, response);				  
			}	
			else {
				msg+="\nUNCERTAIN! Field processing ERRORS! " + response.field_errors + 
					" and file processing ERRORS! " + response.file_errors + "\n" + msg;
				response.message = msg + "\n" + response.message;						
				httpErrorResponse.httpErrorResponse(__file, __line, "shpConvertFieldProcessor().shapeFileQueue.drain()", 
					serverLog, 500, req, res, msg, undefined, response);
			}		
		}
		catch (e) {
			msg+='\nCaught exception: ' + e.message;
			response.message = msg + "\n" + response.message;						
			httpErrorResponse.httpErrorResponse(__file, __line, "shpConvertFieldProcessor().shapeFileQueue.drain()", 
				serverLog, 500, req, res, msg, undefined, response);
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
				callback: undefined,
				recLen: 0,
				fileNoExt: undefined,
				featureList: [],
				mySrs: undefined,
				prj: undefined,
				reader: undefined,
			}
		
			shapefileData["fileNoExt"] = path.basename(shapefileData["shapeFileName"]);	
			
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
			
			response.message+="\nProcessing shapefile [" + shapefile_no + "]: " + shapefileData["shapeFileName"];		
			response.no_files=shpTotal;				// Add number of files process to response
			response.fields=ofields;				// Add return fields
			response.file_errors+=rval.file_errors;

			// Add to queue			

			serverLog.serverLog2(__file, __line, "readShapeFile", 
				"In readShapeFile(), shapeFileQueue shapefile [" + shapefile_no + "/" + shapefile_total + "]: " + shapefileData["shapeFileName"]);			
			shapeFileQueue.push(shapefileData, function(err) {
				if (err) {
					var msg='ERROR! in readShapeFile()';
						
					response.message+="\n" + msg;	
					response.file_errors++;
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
