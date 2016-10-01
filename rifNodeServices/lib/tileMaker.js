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
// Rapid Enquiry Facility (RIF) - Tile maker code
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
	
const serverLog = require('../lib/serverLog'),
	  nodeGeoSpatialServicesCommon = require('../lib/nodeGeoSpatialServicesCommon'),
	  httpErrorResponse = require('../lib/httpErrorResponse'),
	  scopeChecker = require('../lib/scopeChecker');

const async = require('async'),
	  os = require('os'),
	  fs = require('fs'),
	  path = require('path'),
	  turf = require('turf'),
	  geojson2svg = require('geojson2svg'),
	  converter = geojson2svg({
			viewportSize: { width: 256, height: 256 },
			attributes: { 'style': 'stroke:#000000; fill-opacity: 0.0; stroke-width:0.5px;' },
			output: 'svg'
		}),
	  reproject = require("reproject"),
	  proj4 = require("proj4");

/*
 * Function: 	writeSVGTile
 * Parameters:	path, geolevel, zoomlevel, X, Y, callback, insertion geoJSON
 * Returns:		tile X
 * Description: Create SVG tile from geoJSON
 */
var writeSVGTile = function writeSVGTile(path, geolevel, zoomlevel, X, Y, callback, intersection) {
	scopeChecker(__file, __line, {
		path: path,
		geolevel: geolevel, 
		zoomlevel: zoomlevel, 
		X: X, 
		Y: Y, 
		callback: callback, 
		intersection: intersection, 
		bbox: intersection.bbox
	});
	
//	
// Create directory: path/geolevel/zoomlevel/X as required
//		  
	var dirArray=[path, geolevel, zoomlevel, Y];
	var dir=nodeGeoSpatialServicesCommon.createTemporaryDirectory(dirArray);
	var svgFileName=dir + "/" + Y + ".svg";
	
//
// Create stream for tile
//	
	var svgStream = fs.createWriteStream(svgFileName, { flags : 'w' });	
	svgStream.on('finish', 
		function svgStreamClose() {
			callback();
		});		
	svgStream.on('error', 
		function svgStreamError(e) {
			callback(e);						
		});

//
// Get bounding box from intersection, reproject to 3857
//
	var bboxPolygon = turf.bboxPolygon(intersection.bbox);		
	var bbox3857Polygon = reproject.reproject(
		bboxPolygon,'EPSG:4326','EPSG:3857',proj4.defs);
	var mapExtent={ 
		left: bbox3857Polygon.geometry.coordinates[0][0][0], 	// Xmin
		bottom: bbox3857Polygon.geometry.coordinates[0][1][1], 	// Ymin
		right: bbox3857Polygon.geometry.coordinates[0][2][0], 	// Xmax
		top: bbox3857Polygon.geometry.coordinates[0][3][1] 		// Ymax
	};		
	var svgOptions = {
		mapExtent: mapExtent,
		attributes: { id: svgFileName }
	};
	
//
// Reproject intersection to 3857 and convert to SVG
//	
	var intersection3857 = reproject.reproject(
		intersection,'EPSG:4326','EPSG:3857',proj4.defs);
	var svgString = converter.convert(intersection3857, svgOptions);
	svgString='<?xml version="1.0" standalone="no"?>\n' +
		' <!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">\n' +
		'  <svg width="256" height="256" version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">\n' + 
		'   ' + svgString + '\n' +
		'  </svg>';

//
// Write SVG file
//		
	svgStream.write(svgString);
	svgStream.end();
} 

/*
 * Function: 	longitude2tile
 * Parameters:	Longitude, zoom level
 * Returns:		tile X
 * Description:	Convert longitude (WGS84 - 4326) to OSM tile x
 
	From: http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
	
	Derivation of the tile X/Y 

	* Reproject the coordinates to the Mercator projection (from EPSG:4326 to EPSG:3857):

	x = lon
	y = arsinh(tan(lat)) = log[tan(lat) + sec(lat)]
	(lat and lon are in radians)

	* Transform range of x and y to 0 ? 1 and shift origin to top left corner:

	x = [1 + (x / p)] / 2
	y = [1 - (y / p)] / 2

	* Calculate the number of tiles across the map, n, using 2**zoom
	* Multiply x and y by n. Round results down to give tilex and tiley.
	
	X and Y
	
	* X goes from 0 (left edge is 180 °W) to 2**zoom − 1 (right edge is 180 °E)
	* Y goes from 0 (top edge is 85.0511 °N) to 2**zoom − 1 (bottom edge is 85.0511 °S) in a Mercator projection
	
	For the curious, the number 85.0511 is the result of arctan(sinh(π)). By using this bound, the entire map becomes a (very large) square.
 */		
var longitude2tile = function longitude2tile(longitude, zoomLevel) {
	var tileX=Math.floor( (longitude + 180) / 360 * Math.pow(2, zoomLevel) );
	return tileX;
}
	
/*
 * Function: 	latitude2tile
 * Parameters:	Latitude, zoom level
 * Returns:		tile Y
 * Description:	Convert latitude (WGS84 - 4326) to OSM tile y
 */ 
var latitude2tile = function latitude2tile(latitude, zoomLevel) {
	var tileY=Math.floor(
		(1.0 - Math.log( /* Natural Log */
				Math.tan(latitude * (Math.PI/180)) + 1.0 / Math.cos(latitude * (Math.PI/180))) / 
			Math.PI) / 2.0 * Math.pow(2, zoomLevel)
		);
	return tileY;
}

/*
 * Function: 	tile2longitude
 * Parameters:	tile X, zoom level
 * Returns:		Longitude
 * Description:	Convert OSM tile X to longitude (WGS84 - 4326)
 */
function tile2longitude(x,zoomLevel) {
	return (x/Math.pow(2,zoomLevel)*360-180);
}
	
/*
 * Function: 	tile2latitude
 * Parameters:	tile Y, zoom level
 * Returns:		Latitude
 * Description:	Convert OSM tile Y to latitude (WGS84 - 4326)
 */	
function tile2latitude(y,zoomLevel) {
	var n=Math.PI-2*Math.PI*y/Math.pow(2,zoomLevel);
	return (180/Math.PI*Math.atan(0.5*(Math.exp(n)-Math.exp(-n))));
}
	
/*
 * Function: 	tileMaker()
 * Parameters:	Internal response object, HTTP request object, HTTP response object, callback to call at end of processing
 * Description:	
 *				Then call geojsonToCSV() - Convert geoJSON to CSV; save as CSV files; create load scripts for Postgres and MS SQL server
 */		
 
var tileMaker = function tileMaker(response, req, res, endCallback) {

	scopeChecker(__file, __line, {
		response: response,
		message: response.message,
		serverLog: serverLog,
		req: req,
		res: res,
		httpErrorResponse: httpErrorResponse,
		nodeGeoSpatialServicesCommon: nodeGeoSpatialServicesCommon,
		callback: endCallback
	});
		
	const geojsonToCSV = require('../lib/geojsonToCSV');
 
/* 
 * Function: 	tile()
 * Parameters:	zoomlevel, X tile number, Y tile number, geolevel_id, tileArray
 * Returns:		tile object
 * Description:	Object constructor; add to tileArray if defined
 */
	function tile(zoomlevel, X, Y, geolevel_id, tileArray) { 
		this.zl=zoomlevel;
		this.X=X; 
		this.Y=Y; 
		this.gl=geolevel_id;

		tileArray.push(this);	
		
		return this;
	}

/* 
 * Function: 	createTile()
 * Parameters:	Processing index, tile object, geoJSON for tile geolevel/file
 * Returns:		TopoJSON tile or undefined if no topoJSON data within the bounding boc
 * Description:	Crrate tile:
 *					create bounding box for tile
 */	
	function createTile(k, ntile, geoJSON) {
		if (geoJSON == undefined) {
			throw new Error("createTile(" + k + ") no geoJSON for zoomlevel: " + ntile.zl +
				"\nntile: " + JSON.stringify(ltile, null, 4).substring(0, 132));
		}
		
		var bbox=[];
		var bboxPolygon;
		var intersection;
		var result;

//
// Fix min/max in wrong order		
// Bounding box [xmin: -179.14733999999999, ymin: -14.552548999999997, xmax: 179.77847, ymax: 71.352561];
// turf.bbox-polygon() failed for bbox: [-180, 85.0511287798066, 180, -85.0511287798066]
//		
		bbox[0]=tile2longitude(ntile.X, ntile.zl);		// xmin
		bbox[1]=tile2latitude(ntile.Y, ntile.zl);		// ymin
		bbox[2]=tile2longitude(ntile.X+1, ntile.zl);	// xmax
		bbox[3]=tile2latitude(ntile.Y+1, ntile.zl);		// ymax
		if (bbox[3] < bbox[1]) { // Swap Y
			var t=bbox[3];
			bbox[3]=bbox[1];
			bbox[1]=t;
		}
		if (bbox[2] < bbox[0]) { // Swap X
			var t=bbox[2];
			bbox[2]=bbox[0];
			bbox[0]=t;
		}
		
		var ltile = {			// Result object
			zl: ntile.zl,
			X: ntile.X,
			Y: ntile.Y,
			gl: ntile.gl,
			bbox: bbox,
			intersects: 0,
			svg: undefined,
			id: "/" + ntile.gl + "/" + ntile.zl + "/" + ntile.X + "/" + ntile.Y + ".png",
			topojson: undefined
		}
		
		try {
			bboxPolygon=turf.bboxPolygon(bbox);	// Convert bounding box to polygon
		}
		catch (e) {
			throw new Error("createTile(" + k + ") turf.bboxPolygon() failed for bbox: " + JSON.stringify(bbox, null, 4) +
				"\nError: " + e.message +
				"\nltile: " + JSON.stringify(ltile, null, 4).substring(0, 132));
		}	
		
		var intersectlist = [];					// Intersect bounding box polygon with geoJSON feature by feature
		for (var i = 0; i < geoJSON.features.length; i++) {
			var kinks = turf.kinks(geoJSON.features[i]);

			if (kinks && kinks.intersections && kinks.intersections.features) { // Look for self intersections
				var kinksFeatures = kinks.intersections.features.concat(geoJSON.features[i]);
				var kinksFeatureCollection = {
				  "type": "FeatureCollection",
				  "features": kinksFeatures
				};
				throw new Error("createTile(" + k + ") feature: " + i + "; turf.kinks() found self intersections: " + 
					JSON.stringify(kinksFeatureCollection, null, 4).substring(0, 400));
			}
			
			try {
				var intersectedFeature = turf.intersect(geoJSON.features[i], bboxPolygon);
				if (intersectedFeature) {
					intersectlist.push(geoJSON.features[i]); // Use geoJSON NOT boundinng box!
				}
			}
			catch (e) {
				throw new Error("createTile(" + k + ") turf.intersect() failed for feature: " + i + 
					"; bboxPolygon: " + JSON.stringify(bboxPolygon, null, 4) +
					"\nError: " + e.message +
					"\nGeoJSON: " + JSON.stringify(geoJSON.features[i], null, 2).substring(0, 800) + 
					"\nltile: " + JSON.stringify(ltile, null, 4).substring(0, 132));
			}
		}	
		
		if (intersectlist.length > 0) { // Intersected
			ltile.intersects=1;

			intersectlist.push(bboxPolygon); // Add boundary to tile for test purtposes			
			var intersection= {
				type: "FeatureCollection",
				features: intersectlist
			}
			// Make topoJSOB
			ltile.topojson=intersection3857;
				
			var intersection3857 = reproject.reproject(	// Re-project to 3857 (WGS 84 / Pseudo-Mercator) for SVG
				intersection,'EPSG:4326', 'EPSG:3857', proj4.defs);	
			var bbox3857Polygon = reproject.reproject(
				bboxPolygon,'EPSG:4326', 'EPSG:3857', proj4.defs);
			var mapExtent={ 
				left: bbox3857Polygon.geometry.coordinates[0][0][0], 	// Xmin
				bottom: bbox3857Polygon.geometry.coordinates[0][1][1], 	// Ymin
				right: bbox3857Polygon.geometry.coordinates[0][2][0], 	// Xmax
				top: bbox3857Polygon.geometry.coordinates[0][3][1] 		// Ymax
			};		
			var svgOptions = {
				mapExtent: mapExtent,
				attributes: { id: ltile.id }
			};
			try {
// Need to clip - turf.difference() ?
//				var tileJSON=turf.bboxClip(intersection, bbox);
				var svgString = converter.convert(intersection3857, svgOptions);
				ltile.svg='<?xml version="1.0" standalone="no"?>\n' +
					' <!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">\n' +
					'  <svg width="256" height="256" version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">\n' + 
					'   ' + svgString + '\n' +
					'  </svg>';
			}
			catch (e) {
				throw new Error("createTile(" + k + ") tile create failed for bboxPolygon: " + JSON.stringify(bboxPolygon, null, 4) +
					"\nError: " + e.message +
					"\nintersection: " + JSON.stringify(intersection, null, 2).substring(0, 800) + 
					"\nltile: " + JSON.stringify(ltile, null, 4).substring(0, 132));
			}			
		}
		
		if (k<30) {
			console.error("createTile(" + k + "): " + JSON.stringify(ltile, null, 4).substring(0, 132));
		}
		
		return ltile;
	}
	
/* 
 * Function: 	tile2csv()
 * Parameters:	gid, tile object
 * Description:	Dump tile object to CSV
 */	
	function tile2csv(gid, tile) {
		if (tile) {
			try {
				return gid + "," + tile.zl + "," + tile.X + "," + tile.Y + "," + tile.gl + "\r\n";
			}
			catch (err) {
				throw new error("tile2csv() error: " + err.message + "; tile: " + JSON.stringify(tile, null, 4));
			}
		}
		else {
			throw new error("tile2csv() tile not defined");
		}
	}

/* 
 * Function: 	createTileArray()
 * Parameters:	None
 * Returns:		tile array object
 * Description:	Create tile Array
 */	
	function createTileArray() {
		var tileArray=[];

		var geolevelId2fileIndex = {};
		for (var i=0; i<response.file_list.length; i++) { 	
			geolevelId2fileIndex[response.file_list[i].geolevel_id]=i;	
		}
	
	/*
	 * Create tile array by looping through file array
	 */	
		for (var j=1; j<=response.file_list.length; j++) { 
			var i=geolevelId2fileIndex[j];
	//
	// Get tile max/ lat/long from bounding box
	//
			var maxZoomlevel=response.file_list[i].topojson[0].zoomlevel;
			var minZoomlevel=response.file_list[i].topojson[(response.file_list[i].topojson.length-1)].zoomlevel;
			var xmin=response.file_list[i].bbox[0];
			var ymin=response.file_list[i].bbox[1];
			var xmax=response.file_list[i].bbox[2];
			var ymax=response.file_list[i].bbox[3];
			
			var xminTile;
			var yminTile;
			var xmaxTile;
			var ymaxTile;
			var xStart=xminTile;
			var xEnd=xmaxTile;	
			var yStart=yminTile;
			var yEnd=ymaxTile;
			var tileCount=0;
			
	// Calculate x/y start and end tile numbers. Handle southern hemisphere 
			if (xminTile > xmaxTile) {
				xStart=xmaxTile;
				xEnd=xminTile;
			}
			if (yminTile > ymaxTile) {
				yStart=ymaxTile;
				yEnd=yminTile;
			}	

			var msg="\ntileMaker() file [" + i + "/" + response.file_list.length + "]: " + 
				(response.file_list[i].file_name || "No file name") +
				"; geolevel_id: " + (response.file_list[i].geolevel_id|| "No geolevel_id") + 
				"; zoom levels: " + (response.file_list[i].topojson.length|| "No zoomlevels") +
				"; min zoomlevel: " + minZoomlevel +
				"; max zoomlevel: " + maxZoomlevel +
				"\nBounding box (4326) [" + // [left, bottom, right, top]
				"xmin: " + xmin + ", " +
				"ymin: " + ymin + ", " +
				"xmax: " + xmax + ", " +
				"ymax: " + ymax + "]; ";
					
	// Create tile array for performance		
			for (var zoomlevel=0; zoomlevel<=maxZoomlevel; zoomlevel++) {
				xminTile=longitude2tile(xmin, zoomlevel);
				yminTile=latitude2tile(ymin, zoomlevel);
				xmaxTile=longitude2tile(xmax, zoomlevel);
				ymaxTile=latitude2tile(ymax, zoomlevel);
				
				xStart=xminTile;
				xEnd=xmaxTile;	
				yStart=yminTile;
				yEnd=ymaxTile;
				
		// Calculate x/y start and end tile numbers. Handle southern hemisphere 
				if (xminTile > xmaxTile) {
					xStart=xmaxTile;
					xEnd=xminTile;
				}
				if (yminTile > ymaxTile) {
					yStart=ymaxTile;
					yEnd=yminTile;
				}

				 msg+="\nTile numbers (" + zoomlevel +") [" + 
					"xminTile: " + xminTile + ", " +
					"yminTile: " + yminTile + ", " +
					"xmaxTile: " + xmaxTile + ", " +
					"ymaxTile: " + ymaxTile + "];";
			
				for (var X=xStart; X<=xEnd; X++) {				
					for (var Y=yStart; Y<=yEnd; Y++) {
						new tile(zoomlevel, X, Y, response.file_list[i].geolevel_id, tileArray);
						tileCount++;
					} // Y loop
				} // X loop
				msg+="\nTotal tiles: " + tileCount;
			} // Zoom level loop
				
	//			
	// Comparision with SQL Server calculation:
	//
	// TRACE: tileMaker() file [0/3]: cb_2014_us_county_500k.shp; geolevel_id: 3; zoom levels: 6; min zoomlevel: 6; max zoomlevel: 11
	// Bounding box (4326) [xmin: -179.148909, ymin: -14.548699000000001, xmax: 179.77847, ymax: 71.36516200000001]; 
	// Tile numbers (11) [xmin: 4, ymin: 1107, xmax: 2046, ymax: 434];
	//
	// geography       zoomlevel       Xmin        Xmax       Ymin       Ymax       Y_mintile  Y_maxtile   X_mintile   X_maxtile
	// --------------- --------------- ----------- ---------- ---------- ---------- ---------- ----------- ----------- ----------
	// cb_2014_us_500k              11  -179.14734  179.77847  -14.55255   71.35256       1107         435           4       2046
	//			
	// This shows that YmaxTile is one less; this is caused by projection error in the SQL; this needs to be reduced:
	//
	// i.e. 71.35256 compared to 71.365162
	//
	// sahsuland_dev=> SELECT tileMaker_latitude2tile(71.365162, 11);
	//  tilemaker_latitude2tile
	// -------------------------
	//                     434
	// (1 row)
	//
			response.message+=msg;
		} // File loop
		
		return tileArray;
	} // End of createTileArray()

/* 
 * Function: 	createTilesSeriesUpdate()
 * Parameters:	Tile array index number (+1);, number of tiles in array, tile csv file size
 * Description:	Create tile Array
 */		
	function createTilesSeriesUpdate(m, nTiles, csvFileSize) {
		var pct=Math.round((m/nTiles)*10000)/100;
		var msg="Created " + m + " tiles, " + pct + "% done; size: " + nodeGeoSpatialServicesCommon.fileSize(csvFileSize);
//		console.error("createTilesSeriesUpdate(): " + msg);
		addStatus(__file, __line, response, msg, 200 /* HTTP OK */, serverLog, req, // Add status
			function createTilesSeriesUpdateCallback(err) { 	
				if (err) {						
					serverLog.serverLog2(__file, __line, "createTilesSeriesUpdateCallback", "ERROR! in adding tiles", req, e);
				}
			}
		);		
	} // End of createTilesSeriesUpdate()


/* 
 * Function: 	createTilesSeriesEndUpdate()
 * Parameters:	Number of tiles in array, tile csv file size, error object (optional)
 * Returns:		tile array object
 * Description:	Create tile Array
 */		
	function createTilesSeriesEndUpdate(nTiles, csvFileSize, e) {
		var msg;
		var httpStatus;
		var stack;
		
		tileArray=undefined;	// Free memory
		
		if (e) {
			msg="Error creating " + nTiles + " tiles: " + e.message;
			httpStatus=501; /*  HTTP general exception trap */
			stack=e.stack;
		}
		else {
			msg="Created " + nTiles + " tiles; size: " + nodeGeoSpatialServicesCommon.fileSize(csvFileSize);
			httpStatus=200; /* HTTP OK */
		}
//		console.error("createTilesSeriesEndUpdate(): " + msg);
		
		addStatus(__file, __line, response, msg, httpStatus, serverLog, req, // Add status
			function createTilesSeriesEndUpdateCallback(err) { 	
				if (err || e) {						
					serverLog.serverLog2(__file, __line, "createTilesSeriesEndUpdateCallback", "ERROR! in adding tiles", req, e);
					endCallback(err || e);
				}
				else {
					try {
						// Call geojsonToCSV() - Convert geoJSON to CSV; save as CSV files; create load scripts for Postgres and MS SQL server
						geojsonToCSV.geojsonToCSV(response, req, res, endCallback); // Convert geoJSON to CSV
					}
					catch (e) {	
						serverLog.serverError2(__file, __line, "createTilesSeriesEndUpdateCallback", 
							"Exception thrown by tileMaker.tileMaker() ", req, e, response);	
					}
				}				
			}, stack /* additionalInfo, errorName */);		
	} // End of createTilesSeriesEndUpdate()
	
	var tileArray=createTileArray();	
	var nTiles=tileArray.length;	
	var l=0; // 1000 record counter
	var m=0; // 100,000 record counter
//	
// Create directory: $TEMP/shpConvert/<uuidV1>/data as required
//		  
	var dirArray=[os.tmpdir() + "/shpConvert", response.fields["uuidV1"], "data"];
	var dir=nodeGeoSpatialServicesCommon.createTemporaryDirectory(dirArray, response, req, serverLog);
	var csvFileName=dir + "/tiles.csv";
	var csvStream = fs.createWriteStream(csvFileName, { flags : 'w' });	
	csvStream.on('finish', function csvStreamClose() {
		response.message+="\nTile svStreamClose(): " + csvFileName;
	});		
	csvStream.on('error', function csvStreamError(e) {
		serverLog.serverLog2(__file, __line, "csvStreamError", 
			"WARNING: Exception in Tile CSV write to file: " + csvFileName, req, e, response);										
	});
	var buf;
	var csvBuf=undefined;
	var csvFileSize=0;
					
	var geolevelId2fileIndex = {};
	for (var i=0; i<response.file_list.length; i++) { 	
		geolevelId2fileIndex[response.file_list[i].geolevel_id]=i;	
	}
	var zoomlevelIndex = {};
	for (var i=0; i<response.file_list[0].topojson.length; i++) {
		zoomlevelIndex[response.file_list[0].topojson[i].zoomlevel]=i;
	}
	var maxZoomlevel=response.file_list[0].topojson[0].zoomlevel;	
	var minZoomlevel=response.file_list[0].topojson[(response.file_list[0].topojson.length-1)].zoomlevel;
	for (var i=0; i<=maxZoomlevel; i++) {
		if (zoomlevelIndex[i] == undefined) {
			if (i<minZoomlevel) {
				zoomlevelIndex[i]=zoomlevelIndex[minZoomlevel];
			}
			else if (i>maxZoomlevel) {
				zoomlevelIndex[i]=zoomlevelIndex[maxZoomlevel];
			}
		}
	}
//	console.error("zoomlevelIndex: " + JSON.stringify(zoomlevelIndex, null, 4));
	
	async.forEachOfSeries(tileArray, 
		function createTilesSeries(ntile, k, tileCallback) { // Processing code
			if (l == 0) {
				csvBuf=undefined;
			}
			l++;
			m++;
			try {
				if (m >= 100000) {
					createTilesSeriesUpdate((k+1), nTiles, csvFileSize);	// Update status
					m=0;
				}
				
				if (zoomlevelIndex[ntile.zl] == undefined) {
					throw new Error("createTilesSeries(" + k + ") no zoomlevelIndex for zoomlevel: " + ntile.zl);
				}				
				else if (geolevelId2fileIndex[ntile.gl] == undefined) {
					throw new Error("createTilesSeries(" + k + ") no geolevelId2fileIndex for geolevel: " + ntile.gl);
				}
				else if (response.file_list[geolevelId2fileIndex[ntile.gl]] == undefined) {
					throw new Error("createTilesSeries(" + k + ") no response.file_list array item for geolevel: " + 
						ntile.gl + 
						"; file index: " + geolevelId2fileIndex[ntile.gl]);
				}
				else if (response.file_list[geolevelId2fileIndex[ntile.gl]].topojson[zoomlevelIndex[ntile.zl]] == undefined) {
					throw new Error("createTilesSeries(" + k + ") no topojson object for geolevel: " + 
						ntile.gl + 
						"; file index: " + geolevelId2fileIndex[zoomlevelIndex[ntile.zl]] +
						"; zoomlevel index: " + zoomlevelIndex[ntile.zl] +
						"; zoomlevel: " + ntile.zl);
				}
				else if (response.file_list[geolevelId2fileIndex[ntile.gl]].topojson[zoomlevelIndex[ntile.zl]].geojson == undefined) {
					throw new Error("createTilesSeries(" + k + ") geojson for geolevel: " + 
						ntile.gl + 
						"; file index: " + geolevelId2fileIndex[ntile.gl] +
						"; zoomlevel index: " + zoomlevelIndex[ntile.zl] +
						"; zoomlevel: " + ntile.zl);
				}
				var geojson=response.file_list[geolevelId2fileIndex[ntile.gl]].topojson[zoomlevelIndex[ntile.zl]].geojson;
				var ltile=createTile(k, ntile, geojson);
				buf=tile2csv(k, ltile);
				csvFileSize+=buf.length;
				ntile={};	// Free memory
				if (csvBuf == undefined) {
					csvBuf=buf;
				}
				else {
					csvBuf+=buf;
				}
					
				if (l >= 10 /* 00 */) { // Keep the stack under control!
					l=0;
					var nextTickFunc = function nextTick() {
						csvStream.write(csvBuf, tileCallback);
					}
					process.nextTick(nextTickFunc);
				}
				else {
					/*
					if (csvBuf.length > 10000000) { // 10 MB
						var bufCallBackFunc = function bufCallBack() {
							csvBuf=undefined;
							tileCallback();				
						}
						csvStream.write(csvBuf, bufCallBackFunc);					
					}
					else { */
						tileCallback();
				/*	} */
				} 
			}
			catch (e) {
				var msg="Created " + nTiles + " tiles; size: " + csvFileSize + "; error: " +
					e.message + "\nStack: " + e.stack;
				console.error("createTilesSeries catch(): " + msg);
				tileCallback(e);
			}
		}, // End of createTilesSeries() [Processing code]
		function createTilesSeriesEnd(err) { //  Callback	
			csvStream.end();	
			if (err) {
				createTilesSeriesEndUpdate(nTiles, csvFileSize, err);
			}
			else {
				createTilesSeriesEndUpdate(nTiles, csvFileSize, undefined /* NO ERROR */);
			}
		});
											
} // End of tileMaker()

module.exports.tileMaker = tileMaker;
module.exports.tile2longitude = tile2longitude;
module.exports.tile2latitude = tile2latitude;
module.exports.writeSVGTile = writeSVGTile;

// Eof