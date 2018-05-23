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
	  dbLoad = require('../lib/dbLoad'),
	  nodeGeoSpatialServicesCommon = require('../lib/nodeGeoSpatialServicesCommon'),
	  httpErrorResponse = require('../lib/httpErrorResponse'),
	  scopeChecker = require('../lib/scopeChecker'),
	  TileMakerConfig = require('../lib/TileMakerConfig'),
	  svg2png = require('../lib/svg2png-many-mod');

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
	  proj4 = require("proj4"),
	  wellknown = require('wellknown'),
	  topojson = require('topojson'),
	  sizeof = require('object-sizeof'),
	  xml2js = require('xml2js');

/*
 * Function: 	createSVGTile
 * Parameters:	geolevel, zoomlevel, X, Y, callback, insertion geoJSON
 * Returns:		{svgString, clipRect}
 * Description: Create SVG tile from geoJSON
 */
var createSVGTile = function createSVGTile(geolevel_id, zoomlevel, x, y, geojson) {
	scopeChecker(__file, __line, {
		geojson: geojson,
		bbox: geojson.bbox
	});
//
// Get bounding box from tile X/Y, reproject to 3857
//
	var bbox=[];
	bbox[0]=tile2longitude(x, zoomlevel); 	/* Xmin (4326); e.g. -179.13729006727 */
	bbox[1]=tile2latitude(y, zoomlevel);	/* Ymin (4326); e.g. -14.3737802873213 */
	bbox[2]=tile2longitude(x+1, zoomlevel);	/* Xmax (4326); e.g.  179.773803959804 */
	bbox[3]=tile2latitude(y+1, zoomlevel);	/* Ymax (4326); e.g. 71.352561 */
	geojson.bbox=bbox;						// Bounding box of tile
	var bboxPolygon = turf.bboxPolygon(bbox);	
	bboxPolygon.properties.gid='bound';	
// Could use: turf-bbox-clip:
//	for (var i=0; i<geojson.features.length; i++) {
//		turf.bbox-clip(geojson.features[i], bbox);		
//	}
	geojson.features.push(bboxPolygon);		// Add bounding box for debug purposes
	var bbox3857Polygon = reproject.reproject(
		bboxPolygon, 'EPSG:4326', 'EPSG:3857', proj4.defs);
	var mapExtent={ 
		left: bbox3857Polygon.geometry.coordinates[0][0][0], 	// Xmin
		bottom: bbox3857Polygon.geometry.coordinates[0][1][1], 	// Ymin
		right: bbox3857Polygon.geometry.coordinates[0][2][0], 	// Xmax
		top: bbox3857Polygon.geometry.coordinates[0][3][1] 		// Ymax
	};	
	/* 
	var clipRect = { // EPSG 3857 clipRect
		left: bbox3857Polygon.geometry.coordinates[0][0][0], 	// Xmin
		top: bbox3857Polygon.geometry.coordinates[0][3][1],  	// Ymin
		width: Math.abs(bbox3857Polygon.geometry.coordinates[0][2][0]-bbox3857Polygon.geometry.coordinates[0][0][0]), // Xmax-Xmin
		height: Math.abs(bbox3857Polygon.geometry.coordinates[0][3][1]-bbox3857Polygon.geometry.coordinates[0][1][1]) // Ymax-Ymin 
	}; */
	var clipRect = { // Pixel clipRect
		left: 0, 	
		top: 0,  	
		width: 256, 
		height: 256
	};	
	
	var clipPath='<clipPath id="clipRect"><rect x="' + clipRect.left + '" y="' + clipRect.top + '" width="' + clipRect.width + '" height="' + clipRect.height + '" /></clipPath>';
	/*
	winston.log("debug", 'bbox: ' + JSON.stringify(bbox, null, 2));
	winston.log("debug", 'bbox3857Polygon: ' + JSON.stringify(bbox3857Polygon, null, 2));
	winston.log("debug", 'mapExtent: ' + JSON.stringify(mapExtent, null, 2));
	winston.log("debug", 'clipRect: ' + JSON.stringify(clipRect, null, 2));
	winston.log("debug", 'clipPath: ' + JSON.stringify(clipPath, null, 2));

	throw new Error("XXX");
*/
	var svgFileName=geolevel_id + "/" + zoomlevel + "/" + x + "/" + y + ".svg";	
	var svgOptions = {
		mapExtent: 	mapExtent,
		attributes: { 
			id: svgFileName /*, 
			"clip-path": 'url(#clipRect)'  */
			}
	};
	
//
// Reproject intersection to 3857 and convert to SVG
//	
	var geojson3857 = reproject.reproject(
		geojson, 'EPSG:4326', 'EPSG:3857', proj4.defs);
	var svgString = converter.convert(geojson3857, svgOptions);

//
// Clipping disabled; causes PhantomJS to hang...
//
	var rval={
		svgString: '<?xml version="1.0" standalone="no"?>\n' +
			' <!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">\n' +
			'  <svg width="256" height="256" version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">\n' + 
//			'   <defs>' + clipPath + '</defs>\n' +
			'   ' + svgString + '\n' +
			'  </svg>',
		clipRect: clipRect
	};
	winston.log("verbose", svgFileName + "; size: " + nodeGeoSpatialServicesCommon.fileSize(sizeof(svgString)) +
		'; clipPath: ' + JSON.stringify(clipPath, null, 2) +
		'; mapExtent: ' + JSON.stringify(mapExtent, null, 2));

	return rval;
} // End of createSVGTile()
	
/*
 * Function: 	getSVGTileFileName
 * Parameters:	path, geolevel, zoomlevel, X, Y
 * Returns:		tile X
 * Description: Get SVG tile name (without extension)
 */
var getSVGTileFileName = function getSVGTileFileName(path, geolevel_id, zoomlevel, x, y) {	
	var svgFileName=path + "/" + geolevel_id + "/" + zoomlevel + "/" + x + "/" + y;
	
	return svgFileName;
}

/*
 * Function: 	writeSVGTile
 * Parameters:	path, geolevel, zoomlevel, X, Y, callback, SVG string
 * Returns:		tile X
 * Description: Create SVG tile from geoJSON
 */
var writeSVGTile = function writeSVGTile(path, geolevel_id, zoomlevel, X, Y, callback, svgTile) {
	scopeChecker(__file, __line, {
		path: path,
		callback: callback, 
		svgTile: svgTile
	});
	
//	
// Create directory: path/geolevel/zoomlevel/X as required
//		  
	var dirArray=[path, geolevel_id, zoomlevel, X];
	var dir=nodeGeoSpatialServicesCommon.createTemporaryDirectory(dirArray);
	var svgFileName=dir + "/" + Y + ".svg";

//
// Create stream for tile
//	
	var svgStream = fs.createWriteStream(svgFileName, { flags : 'w' });	
	svgStream.on('finish', 
		function svgStreamClose() {
			winston.log("verbose", "Wrote svg file: " + svgFileName + "; size: " + nodeGeoSpatialServicesCommon.fileSize(sizeof(svgTile)));
			callback();
		});		
	svgStream.on('error', 
		function svgStreamError(e) {
			callback(e);						
		});
		
//
// Write SVG file
//		
	svgStream.write(svgTile.svgString);
	svgStream.end();
	
} // End of writeSVGTile()

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
 
//	dbTileMaker(endCallback)										
} // End of tileMaker()

/*
 * Function: 	dbTileMaker()
 * Parameters:	Database object (pg or mssql), Database client connectiom, create PNG files (true/false), tile make configuration, 
 *				database type (PostGres or MSSQLServer), callback, max zoomlevel, tile blocks per processing trip, logging object
 * Returns:		Nothing
 * Description:	Creates topoJSONtiles in the database, SVG and PNG tiles if required
 */
var dbTileMaker = function dbTileMaker(dbSql, client, createPngfile, tileMakerConfig, dbType, dbTileMakerCallback, maxZoomlevel, blocks, winston) {

	winston.verbose("Parsed: " + tileMakerConfig.xmlConfig.xmlFileDir + "/" + tileMakerConfig.xmlConfig.xmlFileName, tileMakerConfig.xmlConfig);				
	scopeChecker(__file, __line, { // Check callback
		callback: dbTileMakerCallback,
		tileMakerConfig: tileMakerConfig,
		xmlConfig: tileMakerConfig.xmlConfig,
		xmlFileDir: tileMakerConfig.xmlConfig.xmlFileDir,
		xmlFileName: tileMakerConfig.xmlConfig.xmlFileName, 
		dbType: dbType
	});

	if (dbType != "PostGres" && dbType != "MSSQLServer") {
		throw new Error("dbTileMaker(): Invalid dbType: " + dbType)
	}
	
	/*
	 * Function: 	dbErrorHandler()
	 * Parameters:	Error object, SQL query, callback
	 * Returns:		Nothing
	 * Description:	Creates new Error; raise using callback if defined or dbTileMakerCallback
	 */
	var dbErrorHandler = function dbErrorHandler(err, sqlInError, callback) {
		var nerr;
		if (callback == undefined && err) {
			nerr=new Error("dbErrorHandler(no callback): " + err.message + "\nStack:\n" + err.stack + "\nSQL> " + (sqlInError||sql));
			nerr.stack=err.stack; // Prepend previous stack
		} 
		else if (callback == undefined && callback == undefined) {
			nerr=new Error("dbErrorHandler(no callback): No error object passed\nSQL> " + (sqlInError||sql||"no sql"));
		} 
		else if (typeof callback != "function") {
			nerr=new Error("dbErrorHandler(callback: " + (callback.name || "anonymous") + " is not a function): " + 
				err.message + "\nStack:\n" + err.stack + "\nSQL> " + (sqlInError||sql||"no sql"));
			nerr.stack=err.stack; // Prepend previous stack
		}
		else if (err) {	
			nerr=new Error("dbErrorHandler(" + (callback.name || "anonymous") + "): " + err.message + "\nStack:\n" + err.stack + "\nSQL> " + (sqlInError||sql));
			nerr.stack=err.stack; // Prepend previous stack
		}
		else {
			nerr=new Error("dbErrorHandler(" + (callback.name || "anonymous") + ") No error object passed")
		}
		try {
			winston.log("error", "dbErrorHandler() Error: " + nerr.message);
			endTransaction(nerr, callback || dbTileMakerCallback, (sqlInError||sql||"no sql"));
		}
		catch (e) {
			winston.log("error", "dbErrorHandler() Caught error in end transaction: " + e.message);
			dbTileMakerCallback(nerr);
		}
	} // End of dbErrorHandler()						

	/*
	 * Function: 	TopojsonOptions()
	 * Parameters:	None
	 * Returns:		Object
	 * Description:	Topojson module options object constructor
	 */
	function TopojsonOptions() { 
		this.verbose=false;

		this["property-transform"] = function TopojsonOptionsProperties(d) {					
			return d.properties;
		};
		this.id = function TopojsonOptionsId(d) {
			if (!d.properties["gid"]) {
				throw new Error("FIELD PROCESSING ERROR! Invalid id field: d.properties.gid does not exist in geoJSON");
			}
			else {
				return d.properties["gid"];
			}							
		};
	} // End of TopojsonOptions() object constructor

	/*
	 * Function: 	Tile()
	 * Parameters:	Row object (from SQL query of tile intersects), geojson, topojson module options object, tile array
	 * Returns:		Object
	 * Description:	Create internal tile object
	 */	
	function Tile(row, geojson, topojson_options, tileArray) { 
		scopeChecker(__file, __line, {		
			row: 				row,
 			tileId: 			row.tile_id,
 			geolevel_id:		row.geolevel_id,
 			zoomlevel:			row.zoomlevel,
 			x:					row.x,
 			y:					row.y,
 			geojson: 			geojson,
 			topojson_options: 	topojson_options,
 			tileArray: 			tileArray
 		});

		this.tileId=row.tile_id;
		this.geojson={type: "FeatureCollection", bbox: undefined, features: [geojson]};
		this.geolevel_id=row.geolevel_id;	
		this.geolevel_name=row.geolevel_name;	
		this.zoomlevel=row.zoomlevel;
		this.x=row.x;
		this.y=row.y;
		this.addTopoJsonCalls=0;
		this.areaid_count=1;
		
		this.topojson_options=topojson_options;		

		if (tileArray) {
			tileArray.push(this);	
			tileNo++;
			this.id=tileNo;
			if (this.geojson.features[0].properties.gid == undefined) {
				this.geojson.features[0].properties.gid=this.id;
			}
			
			// Add other keys to properties; i.e, anything else in lookup table
			var usedAready=['tile_id', 'geolevel_id', 'zoomlevel', 'optimised_wkt', 
				'areaid', 'areaname', 'block', 'geographic_centroid', 'lookup_gid'];
			for (var key in row) {
				if (usedAready.indexOf(key) ==	-1) { // Not tile_id, geolevel_id, zoomlevel, optimised_wkt, areaid, areaName
					// Already added: areaName, areaID
					this.geojson.features[0].properties[key] = row[key];	
				}
			}
			this.geojson.features[0].properties.zoomlevel=this.zoomlevel;
			if (this.geojson.features[0].properties.name == undefined) {
				this.geojson.features[0].properties.name=(row[areaname] || row[areaid]);
			}
			winston.log("debug", "Create tile: " + this.id + "; tile_id: " + this.tileId +
				"; features: " + JSON.stringify(this.geojson.features[0].properties));
		}
		else {
			throw new Error("No tileArray defined");
		}		
	} // End of ~Tile() object constructor
	Tile.prototype = { // Add methods
		/*
		 * Function: 	toCSV()
		 * Parameters:	database type
		 * Returns:		tile row as CSV
		 * Description:	Convert tile data to CSV for database import
		 */	
		toCSV: function(dbType) {
			var str='{"type": "FeatureCollection","features":[]}'; // Null geojson
			if (this.topojson) {
				str=JSON.stringify(this.topojson);
				if (dbType == "PostGres") {
					str=str.split('"' /* search: " */).join('""' /* replacement: "" */);	// CSV escape data 	
				}	
			}
			this.topojson=undefined; // Free up memory used for topojson	
			// geolevel_id, zoomlevel, x, y, tile_id, areaid_count, optimised_topojson
			return (
				'"' +						// Quote enclose all fields
				this.geolevel_id + '","' + 	// ID for ordering (1=lowest resolution). Up to 99 supported.
				this.zoomlevel + '","' +	// Number of tiles is 2**<zoom level> * 2**<zoom level>; i.e. 1, 2x2, 4x4 ... 2048x2048 at zoomlevel 11
				this.x + '","' + 			// X tile number. From 0 to (2**<zoomlevel>)-1
				this.y + '","' + 			// Y tile number. From 0 to (2**<zoomlevel>)-1
				this.tileId + '","' + 		// Tile ID in the format <geolevel number>_<geolevel name>_<zoomlevel>_<X tile number>_<Y tile number>
				this.areaid_count + '","' + // Area ID count
				str + '"'	  				// Tile multipolygon in TopoJSON format, optimised for zoomlevel N. The SRID is always 4326.
			);
		},
		/*
		 * Function: 	removePointFromFeature()
		 * Parameters:	geojson
		 * Returns:		Nothing
		 * Description:	From points from geoJSON feaure
		 */	
		removePointFromFeature: function(geojson) {
			if (geojson && geojson.geometry && geojson.geometry.geometries != undefined) {
				for (var j=0; j<geojson.geometry.geometries.length; j++) {
					if (geojson.geometry.geometries[j].type && 
						geojson.geometry.geometries[j].type == "Point") {
						winston.log("verbose", "Remove Point from geojson feature geometries[" + j + 
							"]: tile: " + this.id + "; tile_id: " + this.tileId + "\nGeoJSON: " + 
							JSON.stringify(geojson.geometry.geometries[j]).substring(1, 300));
						delete geojson.geometry.geometries[j];			
					}
					else if (geojson.geometry.geometries[j].type && 
						geojson.geometry.geometries[j].type == "MultiPoint") {
						winston.log("verbose", "Remove MultiPoint from geojson feature geometries[" + j + 
							"]: tile: " + this.id + "; tile_id: " + this.tileId + "\nGeoJSON: " + 
							JSON.stringify(geojson.geometry.geometries[j]).substring(1, 300));
						delete geojson.geometry.geometries[j];			
					}
					else if (geojson.geometry.geometries[j].type && 
						geojson.geometry.geometries[j].type == "LineString") {
						winston.log("verbose", "Remove LineString from geojson feature geometries[" + j + 
							"]: tile: " + this.id + "; tile_id: " + this.tileId + "\nGeoJSON: " + 
							JSON.stringify(geojson.geometry.geometries[j]).substring(1, 300));
						delete geojson.geometry.geometries[j];			
					}
					else if (geojson.geometry.geometries[j].type && geojson.geometry.geometries[j].type && 
						geojson.geometry.geometries[j].type == "Polygon") { 
						// Ignore
					}
					else if (geojson.geometry.geometries[j].type && geojson.geometry.geometries[j].type && 
						geojson.geometry.geometries[j].type == "MultiPolygon") { 
						// Ignore
					}
					else if (geojson.geometry.geometries[j].type && geojson.geometry.geometries[j].type && 
						geojson.geometry.geometries[j].type != undefined) {
						throw new Error("addFeature(): Unable to add feature, geojson geometries unknown type[" + j + 
							"]: " + geojson.geometry.geometries[j].type + "; tile: " + 
							this.id + "; tile_id: " + this.tileId + "\nGeoJSON: " + 
							JSON.stringify(geojson.geometry.geometries[j]).substring(1, 300));
					}
				} 
			}
			else if (geojson && geojson.geometry && geojson.geometry.type && geojson.geometry.type == "Polygon") { 
				// Ignore
			}
			else if (geojson && geojson.geometry && geojson.geometry.type && geojson.geometry.type == "MultiPolygon") {
				// Ignore
			}
			else if (geojson && geojson.geometry && geojson.geometry.type && geojson.geometry.type == "LineString") {
				winston.log("verbose", "Remove LineString from geojson feature geometry; tile: " + 
					this.id + "; tile_id: " + this.tileId + "\nGeoJSON: " + 
					JSON.stringify(geojson.geometry).substring(1, 300));
				delete geojson.geometry;			
			}
			else if (geojson && geojson.geometry && geojson.geometry.type && geojson.geometry.type == "MultiPoint") {
				winston.log("verbose", "Remove MultiPoint from geojson feature geometry; tile: " + 
					this.id + "; tile_id: " + this.tileId + "\nGeoJSON: " + 
					JSON.stringify(geojson.geometry).substring(1, 300));
				delete geojson.geometry;			
			}	
			else if (geojson && geojson.geometry && geojson.geometry.type && geojson.geometry.type == "Point") {
				winston.log("verbose", "Remove Point from geojson feature geometry; tile: " + 
					this.id + "; tile_id: " + this.tileId + "\nGeoJSON: " + 
					JSON.stringify(geojson.geometry).substring(1, 300));
				delete geojson.geometry;			
			}				
			else if (geojson && geojson.geometry && geojson.geometry.type && geojson.geometry.type != undefined) {
				throw new Error("addFeature(): Unable to add feature, geojson geometry unknown type: " + 
					geojson.geometry.type + "; tile: " + 
					this.id + "; tile_id: " + this.tileId + "\nGeoJSON: " + 
					JSON.stringify(geojson.geometry).substring(1, 300));
			}			
		},
		/*
		 * Function: 	addFeature()
		 * Parameters:	Row object (from SQL query), geojson
		 * Returns:		Nothing
		 * Description:	Add geoJSON feature to object
		 */			
		addFeature: function(row, geojson) {
			scopeChecker(__file, __line, {		
				row: 				row,
				tileId: 			row.tile_id,
				geolevel_id:		row.geolevel_id,
				zoomlevel:			row.zoomlevel,
				x:					row.x,
				y:					row.y,
				geojson: 			geojson,
				topojson_options: 	topojson_options,
				tileArray: 			tileArray
			});
		
			if (this.geojson == undefined) {
				throw new Error("addFeature(): Unable to add feature, no pre-existing geojson for tile: " + 
					this.id + "; tile_id: " + this.tileId);
			}
			else if (geojson == undefined) {
				throw new Error("addFeature(): Unable to add feature, no additional geojson supplied for tile: " + 
					this.id + "; tile_id: " + this.tileId);
			}
							
			this.geojson.features.push(geojson);
			this.areaid_count++;
			if (this.geojson.features[(this.geojson.features.length-1)].properties.gid == undefined) {
				this.geojson.features[(this.geojson.features.length-1)].properties.gid=this.id;
			}
			
			// Add other keys to properties; i.e, anything else in lookup table
			var usedAready=['tile_id', 'geolevel_id', 'zoomlevel', 'optimised_wkt', 
				'areaid', 'areaname', 'block', 'geographic_centroid', 'lookup_gid'];
			for (var key in row) {
				if (usedAready.indexOf(key) ==	-1 && 
					this.geojson.features[(this.geojson.features.length-1)].properties[key] == undefined) { 
					// Not tile_id, geolevel_id, zoomlevel, optimised_wkt, areaid, areaname
					// Already added: areaName, areaID
					this.geojson.features[(this.geojson.features.length-1)].properties[key] = row[key];
				}
			}
			this.geojson.features[(this.geojson.features.length-1)].properties.zoomlevel=row.zoomlevel;
			if (this.geojson.features[(this.geojson.features.length-1)].properties.name == undefined) {
				this.geojson.features[(this.geojson.features.length-1)].properties.name=(row[areaname] || row[areaid]);
			}
		},
		/*
		 * Function: 	addTopoJson()
		 * Parameters:	None
		 * Returns:		Size of stringified tile topoJSON
		 * Description:	Convert geoJSON featureList to topojson, add to object, free up memory used for geoJSON
		 */			
		addTopoJson: function() {
			var tileSize=0;
			this.addTopoJsonCalls++;
			
			if (this.topojson) {
				throw new Error("addTopoJson(): called " + this.addTopoJsonCalls + ": topojson exists for tile: " + this.tileId)
			}
			else if (this.geojson) {
				var bbox;
				var numFeatures=(this.geojson.features.length || 0);	
				try {
					bbox=turf.bbox(this.geojson);
				}
				catch (e) {
					var nerr=new Error("addTopoJson(): caught: " + e.message + " for tile: " + this.tileId + 
						"; features: " + numFeatures +
						"\nGeoGSON: " + JSON.stringify(this.geojson).substring(1, 300));
					nerr.stack=e.stack;
					throw nerr;
				}
				this.geojson.bbox=bbox;
				
				for (var j=0; j<this.geojson.features.length; j++) {
					// Remove point data from geojson
					this.removePointFromFeature(this.geojson.features[j]);
				}
				
				if (this.id == 1 && (winston.level == "debug" || winston.level == "silly")) {
					this.topojson_options.verbose=true;
					winston.log("debug", "GEOJSON: " + JSON.stringify(this.geojson, null, 2).substring(0, 1000));
				}
				else {
					this.topojson_options.verbose=false;
				}		
				
				if (createPngfile) { // Create SVG tile			 	
					this.svgTile=createSVGTile(this.geolevel_id, this.zoomlevel, this.x, this.y, this.geojson);
				}
				this.topojson=topojson.topology({   // Convert geoJSON to topoJSON
					collection: this.geojson
					}, this.topojson_options);
					
				this.insertArray=[this.geolevel_id, this.zoomlevel, this.x, this.y, this.tileId, JSON.stringify(this.topojson), 
					numFeatures];
				tileSize=sizeof(this.insertArray);
				winston.log("verbose", 'Make tile ' + this.id + ': "' + this.tileId + 
					'", ' +  numFeatures + ' feature(s)' + 
					'; size: ' + (nodeGeoSpatialServicesCommon.fileSize(tileSize)||'N/A') +
					'; bounding box: ' + JSON.stringify(bbox));	
				if (this.id == 1) {
					winston.log("debug", "TOPOJSON: " + JSON.stringify(this.topojson, null, 2).substring(0, 1000));
				}	
//				else {
//					winston.log("debug", "addTopoJson() called " + this.addTopoJsonCalls + ": " + this.tileId);
//				}
				this.geojson=undefined; // Free up memory used for geoJSON
			}	
			else {
				throw new Error("addTopoJson() called " + this.addTopoJsonCalls + ": no geoJSON for tile: " + this.tileId)
			}
//			winston.log("debug", "addTopoJson(): " + tileSize)
			return tileSize;
		}
	}; // End of Tile() object
			
	/*
	 * Function: 	tileIntersectsRowProcessing()
	 * Parameters:	Row object (from SQL query)
	 * Returns:		Size of stringified tile topoJSON
	 * Description:	Per row tile intersect processing

Generates GEOJSON: {
  "type": "FeatureCollection",
  "features": [
	{
	  "type": "Feature",
	  "properties": {
		"areaID": "01785533",
		"areaName": "Alaska"
	  },
	  "geometry": {
		"type": "MultiPolygon",
		"coordinates": [
		  [
			[
			  [
				-179.148191144524,
				51.2701475586686
			  ],
			  [
				-179.145678650359,
				51.2714362678723
			  ],
			  [
				-179.146755433572,
				51.2753023954834
			  ],
			  [
				-179.144960794883,
				51.2785671254661
			  ],
			  [
				-179.14029473429,
				51.2834642204402
			  ],
			  [
				-179.135628673697,
				51.2862993806884
			  ],
			  [
				-179.130603685366,
				51.2874162619983
			  ],
							
REFERENCE (from shapefile) {
	"type": "FeatureCollection",
	"bbox": [-179.148909,
	-14.548699000000001,
	179.77847,
	71.36516200000001],
	"features": [{
		"type": "Feature",
		"properties": {
			"STATEFP": "02",
			"STATENS": "01785533",
			"AFFGEOID": "0400000US02",
			"GEOID": "02",
			"STUSPS": "AK",
			"NAME": "Alaska",
			"LSAD": "00",
			"ALAND": 1477849359548,
			"AWATER": 245487700921,
			"GID": 1,
			"AREAID": "01785533",
			"AREANAME": "Alaska",
			"AREA_KM2": 1516559.358611932,
			"GEOGRAPHIC_CENTROID_WKT": "POINT (-150.4186689317295 58.382944477050366)"
		},
		"geometry": {
			"type": "MultiPolygon",
			"coordinates": [[[[-157.903963,
			56.346443],
			[-157.89897200000001,
			56.347497000000004],
 */	
	function tileIntersectsRowProcessing(row, geography, dbType) {
		
		var wkt;
		
		if (row.optimised_wkt == undefined) {
			throw new Error("tileIntersectsRowProcessing() row.optimised_wkt is undefined; row: " + JSON.stringify(row).substring(0, 1000));
		}
//		else {
//			winston.log("debug", "tileIntersectsRowProcessing() row.optimised_wkt: " + JSON.stringify(row.optimised_wkt, null, 2).substring(0, 1000));	
//		}

		if (dbType == "PostGres") {
			wkt=row.optimised_wkt;
		}
		else if (dbType == "MSSQLServer") {	
			if (Array.isArray(row.optimised_wkt)) {			
				wkt=row.optimised_wkt.join("");
			}
			else {			
				wkt=row.optimised_wkt;
			}
		}
		var tileArray=getTileArray();
		var geographic_centroid;
		try {
			if (row.geographic_centroid) { 
				geographic_centroid=JSON.parse(row.geographic_centroid);
			}
		}
		catch (e) {
			var nerr=new Error("tileIntersectsRowProcessing() cannot parse geographic_centroid: " + e.message +
				"\nrow: " + 
				JSON.stringify(row).substring(0, 1000));
			nerr.stack=e.stack;
			winston.log("error", nerr.message);
			throw nerr;
		}
		
		var geojson={
			type: "Feature",
			properties: {
				gid: 	  			 row.lookup_gid,
				area_id:   			 row.areaid,
				name: 				 (row.areaname||row.areaid),
				geographic_centroid: geographic_centroid
			}, 
			geometry: wellknown.parse(wkt)
		};

		var geojsonOK=false;
		if (geojson.geometry) {
			geojsonOK=true
		}
		if (row.block <2) {
			winston.log("debug", "Block: " + row.block +
				"; tile_id: " + row.tile_id +
				"; gid: " + geojson.properties.gid +
				"; areaid: " + geojson.properties.areaid +
				"; name: " + geojson.properties.name +
				"; geographic_centroid: " + JSON.stringify(geographic_centroid) +
				"; wkt len: " + wkt.length +
				"; geojsonOK: " + geojsonOK);		
		}
		else {
			winston.log("debug", "Block: " + row.block +
				"; tile_id: " + row.tile_id +
				"; gid: " + geojson.properties.gid +
				"; name: " + geojson.properties.name +
				"; geographic_centroid: " + JSON.stringify(geographic_centroid) +
				"; wkt len: " + wkt.length +
				"; geojsonOK: " + geojsonOK);
		}
		var tileSize=0;

		if (tileArray.length == 0) { // First row in zoomlevel/geolevel combination
			var geojsonTile=new Tile(row, geojson, topojson_options, tileArray);
			winston.log("debug", 'First tile ' + geojsonTile.id + ': ' + geojsonTile.tileId + ": " + 
				geojsonTile.geojson.features[0].properties["area_id"] + "; " +
				geojsonTile.geojson.features[0].properties["name"] + 
				"\nRow: " + JSON.stringify(row, null, 2).substring(1, 300));
		}
		else {
			var geojsonTile=tileArray[(tileArray.length-1)]; // Get last tile from array
			if (geojsonTile.tileId == row.tile_id) { 	// Same tile
				if (geojson) {
					geojsonTile.addFeature(row, geojson);	// Add geoJSON feature to collection
					winston.log("debug", 'Tile ' + geojsonTile.id + ': "' + geojsonTile.tileId + 
						'; add areaID: ' + row.areaid + ": " + 
						geojsonTile.geojson.features[(geojsonTile.geojson.features.length-1)].properties["name"] +
						"; properties: " + 
						JSON.stringify(geojsonTile.geojson.features[(geojsonTile.geojson.features.length-1)].properties));
				}
				else {
					winston.log("warn", 'Tile ' + geojsonTile.id + ': "' + geojsonTile.tileId + 
						'; no geojson; unable to add areaID: ' + row.areaid + ": " + 
						geojsonTile.geojson.features[(geojsonTile.geojson.features.length-1)].properties["mame"]);
				}
			}
			else if (geojson) {										// New tile or already processed
				var foundTile=false;
				for (i=0; i<tileArray.length; i++) {	// Tiles should be in order, but in case the DB gets it wrong (Postgres did!)
					if (tileArray[i].tileId == row.tile_id) { 	// Same tile
						if (tileArray[i].id == geojsonTile.id) {
							geojsonTile=tileArray[i];
							winston.log("debug", 'Old Tile ' + geojsonTile.id + ': "' + geojsonTile.tileId + 
								'; add areaID: ' + row.areaid + ": " + row.areaname);
							geojsonTile.addFeature(row, geojson);	// Add geoJSON feature to collection
							foundTile=true;
						}
						else { // This will almost certainly throw as tiles blocks rows are out of sequence
							throw new Error('Old Tile ' + tileArray[i].id + ': "' + tileArray[i].tileId + 
								" row ID mismatch (" + geojsonTile.id + "); unable to add areaID: " + row.areaid + ": " + row.areaname);
						}
					}
				}
				if (foundTile) {
					tileSize=geojsonTile.addTopoJson();		// Complete last tile
					geojsonTile=new Tile(row, geojson, topojson_options, tileArray);
															// Create new tile
					winston.log("debug", 'Next tile ' + geojsonTile.id + ': "' + geojsonTile.tileId + ": " + 
						geojsonTile.geojson.features[0].properties["name"]);	
				}
				else {
					var geojsonTile=tileArray[(tileArray.length-1)]; // Get last tile from array
					if (geojsonTile) {		
						tileSize=geojsonTile.addTopoJson();		// Complete last tile
					}
					geojsonTile=new Tile(row, geojson, topojson_options, tileArray);
															// Create new tile
					winston.log("debug", 'New tile ' + geojsonTile.id + ': ' + geojsonTile.tileId + ": " + 
						geojsonTile.geojson.features[0].properties["area_id"] + "; " +
						geojsonTile.geojson.features[0].properties["name"]);	
				}
			}	
			else {
				winston.log("warn", 'Tile ' + geojsonTile.id + '; no geojson; unable to add(2) areaID: ' + row.areaid + ": " + 
					geojsonTile.geojson.features[(geojsonTile.geojson.features.length-1)].properties["name"]);
			}
		}	
		return tileSize;
	} // End of tileIntersectsRowProcessing()	

	/*
	 * Function: 	endTransaction()
	 * Parameters:	Error object, Callback: addUserToPath(), SQL causing error
	 * Returns:		Nothing
	 * Description:	End transaction: COMMIT or rollback if error
	 */	
	function endTransaction(terr, endTransactionCallback, sqlInError) {
		
		if (dbType == "PostGres") {
			if (terr) {
				sql='ROLLBACK TRANSACTION';	
			}
			else {
				sql='COMMIT TRANSACTION';		
			}	
			var query=client.query(sql, function endTransactionQuery(err, result) {
				if (err) {
					if (err != terr) {
						throw err; // New error - avoid recursion
					}	
				}
				if ((terr || err) && sqlInError) {
					var nerr=(terr || err);
					winston.log("error", "endTransaction() due to: " + nerr.message + "\nSQL> " + sql + "\nSQL in error> " + sqlInError);
					sql=sqlInError;		// Restore SQL in error
				}
				else {
					winston.log("info", "endTransaction(): " + sql);
				}
				endTransactionCallback(terr || err);	
			});		
		}
		else if (dbType == "MSSQLServer") {	
			if (terr) {
				var query=transaction.rollback(function endTransaction(err) {
					if (err) {
						if (err != terr) {
							throw err; // New error - avoid recursion
						}	
					}
					if ((terr || err) && sqlInError) {
						var nerr=(terr || err);
						winston.log("error", "endTransaction() due to: " + nerr.message + "\nSQL> ROLLBACK\nSQL in error> " + sqlInError);
						sql=sqlInError;		// Restore SQL in error
					}
					endTransactionCallback(terr || err);	
				});				
			}
			else {
				var query=transaction.commit(function endTransaction(err) {
					if (err) {
						dbErrorHandler(err, "COMMIT");
					}
					else {
						winston.log("info", "endTransaction(): COMMIT;");
					}
					endTransactionCallback();	
				});			
			}			
		}
	}
	
	/*
	 * Function: 	startTransaction()
	 * Parameters:	Callback: addUserToPath()
	 * Returns:		Nothing
	 * Description:	Start transaction
	 */	
	function startTransaction(startTransactionCallback) {
		sql='BEGIN TRANSACTION';	
		if (dbType == "PostGres") {
			var query=client.query(sql, function beginTransaction(err, result) {
				if (err) {
					dbErrorHandler(err, sql);
				}
				startTransactionCallback();	
			});		
		}
		else if (dbType == "MSSQLServer") {
			
			transaction = new dbSql.Transaction();
			var query=transaction.begin(function beginTransaction(err) {
				if (err) {
					dbErrorHandler(err, sql);
				}
				startTransactionCallback();	
			});		
		}
	}
	
	/*
	 * Function: 	addUserToPath()
	 * Parameters:	Callback: getNumGeolevelsZoomlevels()
	 * Returns:		Nothing
	 * Description:	Add username to path, call callback (start of geolevel processing)
	 */		
	function addUserToPath(addUserToPathCallback) {
	//
	// Add $user to path
	//	
		if (dbType == "PostGres") {
			sql="SELECT reset_val FROM pg_settings WHERE name='search_path'";
			var query=client.query(sql, function getSearchPath(err, result) {
				if (err) {
					dbErrorHandler(err, sql);
				}
				var currPath=result.rows[0].reset_val;
				sql='SET SEARCH_PATH TO "$user",' + currPath;
			
				var query=client.query(sql, function setSearchPath(err, result) {
					if (err) {
						dbErrorHandler(err, sql);
					}
					else {
						winston.log("info", 'Set Postgres search path to: "$user",' + currPath);
						sql="SET client_encoding='UTF-8'";
						var query=client.query(sql, function setSearchPath(err, result) {
							if (err) {
								dbErrorHandler(err, sql);
							}
							else {
								winston.log("info", sql);
								addUserToPathCallback();
							}
						});
					}					
				});
			});	
		}
		else if (dbType == "MSSQLServer") {	// Not supported in SQL server. Default schema must be User_name()
			var sql="SELECT SCHEMA_NAME() AS default_schema, user_name() AS username;";
			var request = new dbSql.Request();
			request.query(sql, function setSearchPath(err, recordset) {
				if (err) {
					dbErrorHandler(err, sql);
				}
				else {
					if (recordset && recordset[0] && recordset[0].default_schema.toLowerCase() == recordset[0].username.toLowerCase()) {
						winston.log("info", "SQL Server path OK");
						addUserToPathCallback();
					}
					else if (recordset && recordset.recordset && recordset.recordset[0] && recordset.recordset[0].default_schema.toLowerCase() == recordset.recordset[0].username.toLowerCase()) {
						winston.log("info", "SQL Server path OK");
						addUserToPathCallback();
					}
					else if (!recordset) {
						dbErrorHandler(new Error("addUserToPath(): no recordset returned"), sql);
					}
					else if (recordset && !recordset[0]) {
						dbErrorHandler(new Error("addUserToPath(): no rows returned; recordset: " + JSON.stringify(recordset, null, 4)), sql);
					}
					else {
						dbErrorHandler(new Error("addUserToPath(): default schema (" +
							recordset[0].default_schema + ") != username (" + recordset[0].username + ")"), sql);
					}
				}
			});
		}
	} // End of addUserToPath()

	/*
	 * Function: 	getNumGeolevelsZoomlevels()
	 * Parameters:	Geography table name, description, XML file directory, callback: tileIntersectsProcessingGeolevelLoop()
	 * Returns:		Nothing
	 * Description:	Convert geoJSON featureList to topojson, add to object, free up memory used for geoJSON
	 */		
	function getNumGeolevelsZoomlevels(geographyTable, geographyTableDescription, xmlFileDir, getNumGeolevelsZoomlevelsCallback) {
		sql="SELECT * FROM " + geographyTable;
		
		var request;
		if (dbType == "PostGres") {
			request=client;
		}		
		else if (dbType == "MSSQLServer") {	
			request=new dbSql.Request();
		}	

		winston.log("info", "Processing " + geographyTable + "\nDescription: " + geographyTableDescription)
		var query=request.query(sql, function setSearchPath(err, result) {
			if (err) {
				dbErrorHandler(err, sql);
			}
			else {
				var geographyTableData;
				if (dbType == "PostGres") {
					if (result.rows.length != 1) {
						dbErrorHandler(new Error("getNumGeolevelsZoomlevels() geography table: " + geographyTable + " fetch rows !=1 (" + result.rows.length + ")"), sql);
					}
					geographyTableData=result.rows[0];
				}		
				else if (dbType == "MSSQLServer") {	
					var record;
					if (result.recordset) {
						record=result.recordset;
					}
					else {
						record=result;
					}
					if (record.length != 1) {
						dbErrorHandler(new Error("getNumGeolevelsZoomlevels() geography table: " + geographyTable + " fetch rows !=1 (" + record.rows.length + ")"), sql);
					}
					geographyTableData=record[0];
				}
				
				sql="TRUNCATE TABLE t_tiles_" + geographyTableData.geography.toLowerCase();
				winston.log("debug", "SQL> %s;", sql);
				var query=request.query(sql, function t_tilesDelete(err, result) {
					if (err) {
						dbErrorHandler(err, sql);
					}
					else {
						sql="SELECT MAX(geolevel_id) AS max_geolevel_id,\n" +
								"       MAX(zoomlevel) AS max_zoomlevel\n" +
								"  FROM " + geographyTableData.geometrytable;
								
						var query=request.query(sql, function setSearchPath(err, result) {
							if (err) {
								dbErrorHandler(err, sql);
							}
							if (dbType == "PostGres") {
								numZoomlevels=result.rows[0].max_zoomlevel;
								numGeolevels=result.rows[0].max_geolevel_id;
							}		
							else if (dbType == "MSSQLServer") {	
								var record;
								if (result.recordset) {
									record=result.recordset;
								}
								else {
									record=result;
								}
								numZoomlevels=record[0].max_zoomlevel;
								numGeolevels=record[0].max_geolevel_id;
							}
							
							if (numZoomlevels > maxZoomlevel) { // CLI overide
								winston.log('info', 'Reduced zoomlevels from %d available to: %d', numZoomlevels, maxZoomlevel);
							}
							else {
								winston.log('info', 'Zoomlevels to process per geolevel: %d', numZoomlevels);
								maxZoomlevel=numZoomlevels;
							}
							// For testing
							// maxZoomlevel=5;
							getNumGeolevelsZoomlevelsCallback(geographyTableData, xmlFileDir);	
						});		
					}
				});					
			}	
		});	

	} // End of getNumGeolevelsZoomlevels()

	/*
	 * Function: 	createTileBlocksTable()
	 * Parameters:	Geography name, createTileBlocksTableCallback
	 * Returns:		Nothing
	 * Description:	Create tile blocka table 
	 */		
	function createTileBlocksTable(geographyName, createTileBlocksTableCallback) {
		if (geographyName == undefined) {
			createTileBlocksTableCallback(new Error("createTileBlocksTable(): geographyName is undefined"));
		}
		var tileIntersectsTable='tile_intersects_' + geographyName.toLowerCase();
		var tileBlocksTable='tile_blocks_' + geographyName.toLowerCase();
		
		var request;
		if (dbType == "PostGres") {
			request=client;
			sql="DROP TABLE IF EXISTS " + tileBlocksTable;
		}		
		else if (dbType == "MSSQLServer") {	
			request=new dbSql.Request();
			sql="IF OBJECT_ID('" + tileBlocksTable + 
				"', 'U') IS NOT NULL DROP TABLE " + tileBlocksTable;
		}
		winston.log("debug", "SQL> " + sql);
		
		var query=request.query(sql, function dropTileBlocks(err, result) {
			if (err) {
				dbErrorHandler(err, sql);
			}
			else {		
				var cte="WITH a AS (\n" +
						"	SELECT geolevel_id, zoomlevel, x, y, COUNT(areaid) AS total_areas\n" + 
						"	  FROM  " + tileIntersectsTable + "\n" + 
						"	GROUP BY geolevel_id, zoomlevel, x, y\n" + 
						"), b AS (\n" + 
						"	SELECT geolevel_id, zoomlevel, x, y, total_areas,\n" + 
//						"	       (geolevel_id*1000000)+(zoomlevel*1000)+(ABS((ROW_NUMBER() OVER(PARTITION BY geolevel_id, zoomlevel ORDER BY x, y))/" + blocks + ")+1) AS block\n" + 
						"	       ABS((ROW_NUMBER() OVER(PARTITION BY geolevel_id, zoomlevel ORDER BY x, y))/" + blocks + ")+1 AS block\n" + 
						"	  FROM a\n" + 
						")";
				if (dbType == "PostGres") {
					cte+=", c AS (\n" +
						"SELECT geolevel_id, zoomlevel, block, x, y, total_areas,\n" +
						"       geolevel_id::Text||'_'||zoomlevel::Text||'_'||x::Text||'_'||y::Text AS tile\n" + 
						"  FROM b\n" + 
						")\n";

					sql="CREATE TABLE " + tileBlocksTable + "\n" + 
						"AS\n" + cte +
						"SELECT geolevel_id, zoomlevel, block, x, y, total_areas, tile\n" +
						"  FROM c\n" +
						" ORDER BY geolevel_id, zoomlevel, block, tile";
				}
				else if (dbType == "MSSQLServer") {	
					cte+=", c AS (\n" +
						"SELECT geolevel_id, zoomlevel, block, x, y, total_areas,\n" +
						"       CAST(geolevel_id AS VARCHAR) + '_' + CAST(zoomlevel AS VARCHAR) + '_' + CAST(x AS VARCHAR) + '_' + CAST(y AS VARCHAR) AS tile\n" +  
						"  FROM b\n" + 
						")\n";

					sql=cte +
						"SELECT geolevel_id, zoomlevel, block, x, y, total_areas, tile\n" +
						"  INTO " + tileBlocksTable + "\n" + 
						"  FROM c\n" +						
						" ORDER BY geolevel_id, zoomlevel, block, tile";		
				}
				winston.log("debug", "SQL> " + sql);
		
				var query=request.query(sql, function createTileBlocks(err, result) {
					if (err) {
						dbErrorHandler(err, sql);
					}		
					else {
						sql="ALTER TABLE " + tileBlocksTable + " ADD PRIMARY KEY (geolevel_id, zoomlevel, x, y)";
						var query=request.query(sql, function pkTileBlocks(err, result) {
							if (err) {
								dbErrorHandler(err, sql);
							}				
							else {								
								winston.log("info", "Created tile blocks table: " + tileBlocksTable);	
								createTileBlocksTableCallback(); // Callback
							}
						}); // End of pkTileBlocks()
					}
				});	// End of createTileBlocks()	
			}
		}); // End of dropTileBlocks()
	} // End of createTileBlocksTable()
						
	
	/*
	 * Function: 	tileIntersectsProcessingZoomlevelLoopOuter()
	 * Parameters:	Geography table data, XML file directory
	 * Returns:		Nothing
	 * Description:	Create tile bounds table to process geolevel/zoomlevel tile data in blocks of 100 to reduce memory usage;
	 *				calls tileIntersectsProcessingZoomlevelLoop() for each geolevel
	 */		
	function tileIntersectsProcessingZoomlevelLoopOuter(tileIntersectsTable, 
			geolevel_name, geolevel_id, 
			geography, xmlFileDir, callback) {			

		var tileBlocksTable='tile_blocks_' + geography.toLowerCase();
		
		// Check PK: geolevel_id, zoomlevel, x, y
		sql="SELECT tile,\n" + 
			"       COUNT(DISTINCT(block)) AS total_block_pks,\n" +
			"       SUM(total_areas) AS total_areas\n" + 
			"  FROM " + tileBlocksTable + "\n" + 
			" GROUP BY tile\n" + 
			" HAVING COUNT(block) > 1\n" +
			" ORDER BY tile";
		var request;
		if (dbType == "PostGres") {
			request=client;
		}		
		else if (dbType == "MSSQLServer") {	
			request=new dbSql.Request();
		}		
		var query=request.query(sql, function pkTileBlocks(err, result) {
			if (err) {
				dbErrorHandler(err, sql);
			}		
			else {
				var duplicateTileData;
				
				if (dbType == "PostGres") {
					duplicateTileData=result.rows;
				}		
				else if (dbType == "MSSQLServer") {	
				
					if (result.recordset) {
						duplicateTileData=result.recordset;
					}
					else {
						duplicateTileData=result;
					}
				}	
				
				for (var i=0; i<duplicateTileData.length; i++) {
					winston.log("warn", "Duplicate block: " + 
						duplicateTileData[i].tile + "; duplicate PKs: " + duplicateTileData[i].total_block_pks + 
						", areas: " + duplicateTileData[i].total_areas);
				}
				if (duplicateTileData.length > 0) {
					callback(new Error(duplicateTileData.length + " duplicate blocks detected in tileBlocksTable: " + tileBlocksTable));
				}
				else {
					winston.log("verbose", "No duplicate blocks detected in tileBlocksTable: " + tileBlocksTable);
					tileIntersectsProcessingZoomlevelLoop(tileBlocksTable, tileIntersectsTable, geolevel_name, geolevel_id, 
						geography, xmlFileDir, callback);
				}
			}
		});
	} // End of tileIntersectsProcessingZoomlevelLoopOuter()
	
	/*
	 * Function: 	tileIntersectsProcessingGeolevelLoop()
	 * Parameters:	Geography table data, XML file directory
	 * Returns:		Nothing
	 * Description:	Asynchronous do while loop to process all geolevels; calls tileIntersectsProcessingZoomlevelLoop() for each geolevel
	 */		
	function tileIntersectsProcessingGeolevelLoop(geographyTableData, xmlFileDir) {
	
		var tileIntersectsTable='tile_intersects_' + geographyTableData.geography;
		var geolevelsTable='geolevels_' + geographyTableData.geography;
		var request;
		if (dbType == "PostGres") {
			request=client;
		}		
		else if (dbType == "MSSQLServer") {	
			request=new dbSql.Request();
		}
		
		sql="SELECT geolevel_name, geolevel_id, description FROM " + geolevelsTable +
		    " WHERE geography = '" +geographyTableData.geography  + "' ORDER BY geolevel_id";
		var query=request.query(sql, function setSearchPath(err, result) {
			if (err) {
				dbErrorHandler(err, sql);
			}
			var geolvelTableData;
			
			if (dbType == "PostGres") {
				geolvelTableData=result.rows;
			}		
			else if (dbType == "MSSQLServer") {	
				if (result.recordset) {
					geolvelTableData=result.recordset;
				}
				else {
					geolvelTableData=result;
				}
			}		  
			if (geolvelTableData.length < 1) {
				dbErrorHandler(new Error("tileIntersectsProcessingGeolevelLoop() geolevelsTable table: " + geolevelsTable + 
					" fetch rows <1 (" + geolvelTableData.length + ") for geography: " + geographyTableData.geography), sql);	
			}
			winston.log("verbose", "Geography: " + geographyTableData.geography + "; geolevels: " + geolvelTableData.length);
	
			var i=0;
			var geographyMetaData = {
				geography: {
					name: geographyTableData.geography,
					geographical_resolution_level: []
				}
			};
			async.doWhilst(
				function geolevelProcessing(callback) {		
					geographyMetaData.geography.geographical_resolution_level[i] = {
						order: geolvelTableData[i].geolevel_id,
						display_name: (geolvelTableData[i].description || "Unknown"),
						database_field_name: geolvelTableData[i].geolevel_name
					}
					tileIntersectsProcessingZoomlevelLoopOuter(tileIntersectsTable, 
						geolvelTableData[i].geolevel_name, geolvelTableData[i].geolevel_id, 
						geographyTableData.geography, xmlFileDir, callback);
				}, 
				function geolevelTest() { // Mimic for loop
					var res=false;
					
					i++;
					if (i<numGeolevels) { res=true; }; 
					return (res);
				},
				function geolevelProcessingEndCallback(err) { // Call main tileMaker complete 
					createGeographyMetaData(geographyMetaData, 
						function createGeographyMetaDataCallback(err) {
						if (err) {
							dbErrorHandler(err);
						}
						else {
							tileTests(geographyTableData.geography, function preTransactionCallback(err) {
								if (err) {
									endTransaction(err, dbTileMakerCallback, undefined  /* sql */);			
								}
								else if (createPngfile) {
									convertSVG2Png(function convertSVG2PngCallback(err) {	// Convert all SVG to PNG
										endTransaction(err, dbTileMakerCallback, undefined  /* sql */);			
									});		
								}
								else {	
									endTransaction(err, dbTileMakerCallback, undefined  /* sql */);			
								}
							});	
						}
					});
				});		
		});
		 
	} // End of tileIntersectsProcessingGeolevelLoop()
	
	/*
	 * Function: 	createGeographyMetaData()
	 * Parameters:	Geography meta data, callback: main tileMaker complete
	 * Returns:		Nothing
	 * Description:	Create geography metadata
	 
<geography_meta_data>
	<file_path>C:\Users\Kevin\Documents\rif_dl\sahsu_data_sets\sahsuland_geography_metadata.xml</file_path>
	<geographies>
		<geography>
			<name>SAHSULAND</name>
			<geographical_resolution_level>
				<order>1</order>
				<display_name>Level 1</display_name>
				<database_field_name>SAHSU_GRD_LEVEL1</database_field_name>
			</geographical_resolution_level>
			<geographical_resolution_level>
				<order>2</order>
				<display_name>Level 2</display_name>
				<database_field_name>SAHSU_GRD_LEVEL2</database_field_name>
			</geographical_resolution_level>
			<geographical_resolution_level>
				<order>3</order>
				<display_name>Level 3</display_name>
				<database_field_name>SAHSU_GRD_LEVEL3</database_field_name>
			</geographical_resolution_level>
			<geographical_resolution_level>
				<order>4</order>
				<display_name>Level 4</display_name>
				<database_field_name>SAHSU_GRD_LEVEL4</database_field_name>
			</geographical_resolution_level>
		</geography>
	</geographies>
</geography_meta_data>

	 */		
	function createGeographyMetaData(geographyMetaData, createGeographyMetaDataCallback) {
		scopeChecker(__file, __line, { // Check callback
			callback: createGeographyMetaDataCallback,
			xmlConfig: tileMakerConfig.xmlConfig
		});		
		try {		
			var geography_meta_data = {
				file_path: tileMakerConfig.xmlConfig.xmlFileDir + "/" + 
					geographyMetaData.geography.name.toLowerCase() + "_geography_metadata.xml" ,
				geographies: [geographyMetaData]
			}
			var builder = new xml2js.Builder({
				rootName: "geography_meta_data" /*,
				doctype: { // DOES NOT WORK!!!!
						'ext': "geoDataLoader.xsd"
				} */
				});		
			var xmlDoc = builder.buildObject(geography_meta_data);
			
			winston.log("info", "Created dataLoader XML config [xml]: " + geography_meta_data.file_path + 
				" >>>\n" + xmlDoc + "\n<<< end of XML config [xml]");
			winston.log("verbose", "dataLoader XML config as json >>>\n" + JSON.stringify(geography_meta_data, null, 4) + "\n<<< end of XM config as json");

			fs.writeFile(geography_meta_data.file_path, xmlDoc, createGeographyMetaDataCallback);
		}		
		catch (e) {
			createGeographyMetaDataCallback(e);
		}
	} // End of createGeographyMetaData()
	
	/*
	 * Function: 	convertSVG2Png()
	 * Parameters:	Callback: end of transcation
	 * Returns:		Nothing
	 * Description:	Converts all SVG files in svgFileList to PNG
	 */	
	function convertSVG2Png(convertSVG2PngEndCallback) {
		var start = new Date().getTime();
		var sizes = {
			height: 256,
			width: 256
		};
// 10:
// 180 SVG files have been converted successfully in 102.967 S; 1.75 tiles/S		
// 20:
// 180 SVG files have been converted successfully in 101.856 S; 1.77 tiles/S
// 40:
// 180 SVG files have been converted successfully in 117.797 S; 1.53 tiles/S
		var parallelPages = 20;
		winston.log("info", 'Converting ' + Object.keys(svgFileList).length + ' SVG files to PNG');
		svg2png.svg2PngFiles(svgFileList, sizes, parallelPages, clipRectObj).then(results => {
			var end = new Date().getTime();
			var elapsedTime=(end - start)/1000; // in S
			var tilesPerSec=Math.round((Object.keys(svgFileList).length/elapsedTime)*100)/100; 
			if (Array.isArray(results)) {
				winston.log("info", results.length + ' SVG files have been converted successfully in ' + elapsedTime + " S; " + tilesPerSec + " tiles/S");
			} 
			else {
				winston.log("info", 'SVG convert completed with result ' + results + elapsedTime + " S; " + tilesPerSec + " tiles/S");
			}
			
			svgFileList={};
			convertSVG2PngEndCallback();		
		}, errors => {
			if (!Array.isArray(errors)) {
				errors = [errors];
			}
			var err=new Error('convertSVG2Png() completed with ' + errors.length + ' error(s)');
			errors.forEach(error => err.message+='\n' + (error.stack || error));
			dbErrorHandler(err, undefined /* sql */);
		});	
	} // End of convertSVG2Png()
	
	/*
	 * Function: 	tileIntersectsProcessingZoomlevelLoop()
	 * Parameters:	Tile bounds table name, tile intersects table name, geolevel name, geolevel id, geography, XML file directory, 
	 *				geolevel processing callback (callback from geolevelProcessing async)
	 * Returns:		Nothing
	 * Description:	Asynchronous do while loop to process all zoomlevels in a geolevel; calls tileIntersectsProcessing() for each zoomlevel
	 */		
	function tileIntersectsProcessingZoomlevelLoop(tileBlocksTable, tileIntersectsTable, geolevelName, geolevel_id, 
		geography, xmlFileDir, geolevelProcessingCallback) {
		 
		scopeChecker(__file, __line, {
			tileBlocksTable: tileBlocksTable,
			tileIntersectsTable: tileIntersectsTable,
			geolevelName: geolevelName,
			geolevel_id: geolevel_id,
			geography: geography,
			xmlFileDir: xmlFileDir,
			callback: geolevelProcessingCallback
		});
	
// 
// Primary key: geolevel_id, zoomlevel, areaid, x, y
//			
		if (dbType == "PostGres") {	
			sql="WITH a AS (\n" +
				"	SELECT z.geolevel_id::VARCHAR||'_'||'" + geolevelName + 
								"'||'_'||z.zoomlevel::VARCHAR||'_'||z.x::VARCHAR||'_'||z.y::VARCHAR AS tile_id,\n" +
				"	       z.areaid, z.geolevel_id, z.zoomlevel, ST_AsText(z.geom) AS optimised_wkt, z.x, z.y, y.block,\n" + 
				" 		   a.gid AS lookup_gid, a.*\n" +								
				"	  FROM " + tileIntersectsTable + " z, " + tileBlocksTable + " y, lookup_" + geolevelName + " a\n" +
				"	 WHERE z.geolevel_id = $2\n" + 
				"	   AND z.zoomlevel   = $1\n" + 
				"	   AND y.block       = $3\n" +
				"	   AND z.areaid      = a." + geolevelName + "\n" + 
				"	   AND y.geolevel_id = z.geolevel_id\n" +
				"	   AND y.zoomlevel   = z.zoomlevel\n" +
				"	   AND y.x           = z.x\n" +
				"	   AND y.y           = z.y\n" +
				")\n" +
				"SELECT tile_id, areaid, geolevel_id, zoomlevel, x, y, block,\n" + 
				"       areaname, " + geolevelName + ", geographic_centroid::Text AS geographic_centroid, optimised_wkt, lookup_gid\n" + 
				"       FROM a\n" + 
				" ORDER BY tile_id, areaid";
		}		
		else if (dbType == "MSSQLServer") {		
			sql="WITH a AS (\n" +
				"	SELECT CAST(z.geolevel_id AS VARCHAR) + '_' + '" + geolevelName + 
							"' + '_' + CAST(z.zoomlevel AS VARCHAR) + '_' + CAST(z.x AS VARCHAR) + '_' + CAST(z.y AS VARCHAR) AS tile_id,\n" +
				"  	     z.geolevel_id, z.zoomlevel, z.geom.STAsText() AS optimised_wkt, z.areaid, z.x, z.y, y.block,\n" + 
				" 		 a.gid AS lookup_gid, a.*\n" +				
				" 	 FROM " + tileBlocksTable + " y, " + tileIntersectsTable + " z, lookup_" + geolevelName + " a\n" +
				"	 WHERE y.geolevel_id = @geolevel_id\n" + 
				"	   AND y.zoomlevel   = @zoomlevel\n" + 
				"	   AND y.block       = @block\n" +
				"	   AND y.geolevel_id = z.geolevel_id\n" +
				"	   AND y.zoomlevel   = z.zoomlevel\n" +
				"	   AND y.x           = z.x\n" +
				"	   AND y.y           = z.y\n" +
				"	   AND z.areaid      = a." + geolevelName + "\n" + 
				")\n" +
				"SELECT tile_id, areaid, geolevel_id, zoomlevel, x, y, block,\n" + 
				"       areaname, " + geolevelName + ", geographic_centroid, optimised_wkt, lookup_gid\n" + 
				"       FROM a\n" + 
				" ORDER BY tile_id, areaid"
		}
	
		var zoomlevel=0;
		zstart = new Date().getTime(); // Set timer for zoomlevel
		
		// Create CSV file for tiles
		var fileNoext;
		
		if (dbType == "PostGres") {	
			fileNoext="pg_t_tiles_" + geolevelName.toLowerCase();	
		}
		else if (dbType == "MSSQLServer") {
			fileNoext="mssql_t_tiles_" + geolevelName.toLowerCase();	
		}
		
		var csvFileName;		
		if (fs.existsSync(xmlFileDir + "/data")) {
			csvFileName=(xmlFileDir + "/data/" + fileNoext + ".csv");
		}
		else {
			csvFileName=(xmlFileDir + "/" + fileNoext + ".csv");
		}
			
		try { // Create tiles CSV files for each geolevel
			var tilesCsvStream = fs.createWriteStream(csvFileName, { flags : 'w' });
			winston.log("info", "Creating tile CSV file: " + csvFileName);	
			tilesCsvStream.on('finish', function csvStreamClose() {
				winston.log("verbose", "Tile csvStreamClose(): " + csvFileName);
			});		
			tilesCsvStream.on('error', function csvStreamError(e) {
				winston.log("error", "Exception in CSV write to file: " + csvFileName, e.message);		
				geolevelProcessingCallback(e);				
			});
		}
		catch (e) {
			dbErrorHandler(e, sql);
		}	
		
		tilesCsvStream.write("GEOLEVEL_ID,ZOOMLEVEL,X,Y,TILE_ID,AREAID_COUNT,OPTIMISED_TOPOJSON\r\n", // Write header
			function tilesCsvStreamHeaderCallback(err) {
				if (err) {
					geolevelProcessingCallback(err);
				}
				else {
					async.doWhilst(
						function zoomlevelProcessing(zoomlevelProcessingCallback) {		
							tileIntersectsProcessing(sql, zoomlevel, geolevel_id, geography, tilesCsvStream, geolevelName, zoomlevelProcessingCallback);
						}, 
						function zoomlevelTest() { // Mimic for loop
							var res=false;
							
							zoomlevel++;
							/*
							 * 5: 180 tiles in 42.579 S
							 * 6: 410 tiles in 70.932 S
							 * 7: 1024 tiles in 143.471 S
							 * 8: 2681 tiles in 363.273 S
							 */
							if (zoomlevel <= maxZoomlevel) { res=true; }; 
							return (res);
						},
						function zoomlevelProcessingEndCallback(err) {
							if (err) {
								geolevelProcessingCallback(err);
							}
							else {
								tilesCsvStream.end();
				//				winston.log("debug", 'zoomlevelProcessingEndCallback(): zoomlevel: ' + zoomlevel + ', geolevel_id: ' + geolevel_id + ', TileNo: ' + tileNo + '; numTiles: ' + numTiles);

								geolevelProcessingCallback();
							}
						});	
				}
			}
		);
	} // End of tileIntersectsProcessingZoomlevelLoop()

	/*
	 * Function: 	tileIntersectsProcessing()
	 * Parameters:	SQL statement, zoomlevel, geolevel id, geography, tiles CSV file stream, geolevelName, 
	 *				tile intersects processing callback (callback from zoomlevelProcessing async)
	 * Returns:		Nothing
	 * Description:	Asynchronous SQL fetch for all tile intersects in a geolevel/zoomlevel. Calls tileIntersectsRowProcessing() for each row. 
	 *				Process last tile if required
	 */			
	function tileIntersectsProcessing(sql, zoomlevel, geolevel_id, geography, tilesCsvStream, geolevelName, tileIntersectsProcessingCallback) {

		/*
		 * Function: 	pgTileInsert()
		 * Parameters:	Exepcted rows, callback
		 * Returns:		Nothing
		 * Description:	Multi row insert in t_tile_<geography> table
		 */	
		function pgTileInsert(expectedRows, pgTileInsertCallback) {
			var pgInsertSql="INSERT INTO t_tiles_" + geography +
				 '	(geolevel_id, zoomlevel, x, y, tile_id, optimised_topojson, areaid_count)\nVALUES ';
			var j=1;
			var insertArray=[];
			for (var i=0; i<tileArray.length; i++) {
				if (i>0) {
					pgInsertSql+=",\n";
				}
				pgInsertSql+='($' + j + ',$' + (j+1) + ',$' + (j+2) + ',$' + (j+3) + ',$' + (j+4) + ',$' + (j+5) + ',$' + (j+6) + ') /* Row ' + i + ' */';
				j+=7;
				insertArray=insertArray.concat(tileArray[i].insertArray);
			}
	
			var request=client;
			var query=request.query(pgInsertSql, insertArray, function pgTilesInsert(err, result) {
				if (err) {
					winston.log("warn", "INSERT SQL> " + pgInsertSql + "\nValues (" + insertArray.length + "): " + 
						JSON.stringify(insertArray, null, 2).substring(0, 1000));
					dbErrorHandler(err, pgInsertSql);
				}
				else if (result == undefined) {
					dbErrorHandler(
						new Error("pgTilesInsert() tile INSERT: result undefined != expected: " + expectedRows), 
						pgInsertSql);
					
				}
				else if (result.rowCount == undefined && expectedRows > 0) {
					dbErrorHandler(
						new Error("pgTilesInsert() tile INSERT: result.rowCoun undefined != expected: " + expectedRows), 
						pgInsertSql);
					
				}
				else if (expectedRows != result.rowCount) {
					dbErrorHandler(
						new Error("pgTilesInsert() tile INSERT: " + result.rowCount + " != expected: " + expectedRows), 
						pgInsertSql);
				}
				else {
					tileArray=[];  // Re-initialize tile array
					pgTileInsertCallback(); // callback from zoomlevelProcessing async
				}	
			});	
		} // End of pgTileInsert()

		/*
		 * Function: 	mssqlTileInsert()
		 * Parameters:	Exepcted rows, callback
		 * Returns:		Nothing
		 * Description:	Multi row insert in t_tile_<geography> table
		 */	
		function mssqlTileInsert(expectedRows, mssqlTileInsertCallback) {
			var insertSql="INSERT INTO t_tiles_" + geography + ' (geolevel_id, zoomlevel, x, y, tile_id, optimised_topojson, areaid_count)\n' +
				 'VALUES (@geolevel_id, @zoomlevel, @x, @y, @tile_id, @optimised_topojson, @areaid_count)';
			var j=0;
			var HY104Sql = []; // Problem with NVarChar(MAX) and small (<4K) strings; redo as textual sql
			var request = new dbSql.Request();			
			
			async.forEachOfSeries(tileArray, 
				function mssqlTileInsertSeries(value, i, mssqlTileInsertCallback) { // Processing code
					j++;
					// First problem tile: 3_cb_2014_us_county_500k_4_4_7
					var request = new dbSql.Request();

					request.input('geolevel_id', dbSql.Int, tileArray[i].insertArray[0]);
					request.input('zoomlevel', dbSql.Int, tileArray[i].insertArray[1]);
					request.input('x', dbSql.Int, tileArray[i].insertArray[2]);
					request.input('y', dbSql.Int, tileArray[i].insertArray[3]);
					request.input('tile_id', dbSql.VarChar(200), tileArray[i].insertArray[4]);
					request.input('optimised_topojson', dbSql.NVarChar(dbSql.MAX), tileArray[i].insertArray[5]);
					request.input('areaid_count', dbSql.Int, tileArray[i].insertArray[6]);
					var data={
						geolevel_id: 		tileArray[i].insertArray[0],
						zoomlevel: 			tileArray[i].insertArray[1],
						x: 					tileArray[i].insertArray[2],
						y: 					tileArray[i].insertArray[3],
						tile_id: 			tileArray[i].insertArray[4],
						optimised_topojson: tileArray[i].insertArray[5],
						areaid_count: 		tileArray[i].insertArray[6]
					};					
					var query=request.query(insertSql, 
						function mssqlTileInsertSeries(err, recordset) {
							var rowCount=recordset.rowsAffected;
							if (err && err.state == "HY104") { // Problem with NVarChar(MAX) and small (<4K) strings; redo as textual sql
//										winston.log("debug", "[" + data.tile_id + "] INSERT SQL> " + insertSql + "\noptimised_topojson(" + data.optimised_topojson.length + 
//										"): " + data.optimised_topojson.substring(0, 2500));
								HY104Sql.push("INSERT INTO t_tiles_" + geography + ' (geolevel_id, zoomlevel, x, y, tile_id, optimised_topojson, areaid_count)\n' +
										'VALUES (' + data.geolevel_id + ', ' + data.zoomlevel + ', ' + data.x + ', ' + data.y + 
										", '" + data.tile_id + "', '" + data.optimised_topojson.replace("'", "") +
										"', " + data.areaid_count + ")");
								winston.log("debug", "[" + data.tile_id + "] Caught HY104 ERROR: " + JSON.stringify(err, null, 2) + 
									"\nDeferred SQL[" + (HY104Sql.length-1) + "]> " + HY104Sql[(HY104Sql.length-1)]);
								mssqlTileInsertCallback();					
							}
							else if (err && err.state != "HY104") {
								dbErrorHandler(err, insertSql, mssqlTileInsertCallback);
							}
							else if (rowCount == undefined && expectedRows > 0) {
								dbErrorHandler(
									new Error("mssqlTilesInsert() [" + data.tile_id + "] tile INSERT: rowCount undefined != expected: " + expectedRows + 
										"; recordset: " + JSON.stringify(recordset, null, 4)), 
									insertSql, mssqlTileInsertCallback);	
							}
							else if (1 != rowCount) {
								dbErrorHandler(
									new Error("mssqlTilesInsert() [" + data.tile_id + "] tile INSERT: " + rowCount + " != expected: " + 1), 
									insertSql, mssqlTileInsertCallback);
							}
							else {
								mssqlTileInsertCallback();		
							}
						} // End of mssqlTileInsertSeries
					);						
				}, // End of mssqlTileInsertSeries()
				function mssqlTileInsertEnd(err) { //  Callback	
					if (err) {
						dbErrorHandler(err, insertSql, mssqlTileInsertCallback);
					}
					else if (expectedRows != j) {
						dbErrorHandler(
							new Error("mssqlTilesInsert() tile INSERT: " + j + " != expected: " + expectedRows), 
							insertSql, mssqlTileInsertCallback);
					}
					else if (HY104Sql.length > 0) {						
						tileArray=[];  						// Re-initialize tile array
						winston.log("verbose", "Start: " + HY104Sql.length + " HY104 redo inserts");
						async.forEachOfSeries(HY104Sql, 
							function mssqlTileInsert2Series(value, i, mssqlTileInsert2Callback) { // Processing code
							
								var insertSql2=value;
								winston.log("debug", "HY104 Deferred SQL[" + i + "/" + HY104Sql.length + "]> " + insertSql2);
								var request = new dbSql.Request();
								var query=request.query(insertSql2, function mssqlTilesInsert2(err2, result, rowCount) {
									if (err2) {
										winston.log("error", "HY104 Deferred SQL[" + i + "/" + HY104Sql.length + "] error: " + err2.message);
										dbErrorHandler(err2, insertSql2, mssqlTileInsert2Callback);
									}
									else if (rowCount == undefined) {
										dbErrorHandler(new Error("mssqlTilesInsert2() [" + i + "] HY104 redo INSERT: rowCount undefined != expected: " + expectedRows), 
											insertSql2, mssqlTileInsert2Callback);	
									}
									else if (1 != rowCount) {
										dbErrorHandler(new Error("mssqlTilesInsert2() [" + i + "] HY104 redo INSERT: " + rowCount + " != expected: " + 1), 
											insertSql2, mssqlTileInsert2Callback);
									}
									else {
										winston.log("info", "mssqlTilesInsert2() [" + i + "] Recvoer from HY104 ERROR redo: Insert " + i + "/" + HY104Sql.length + " OK");
										mssqlTileInsert2Callback();
									}										
								});						
							}, // End of mssqlTileInsert2Series()
							function mssqlTileInsert2End(err) { //  Callback	
								if (err) {
									dbErrorHandler(err, undefined, mssqlTileInsertCallback);
								}	
								else {
									winston.log("debug", "mssqlTileInsert2End() [" + tileArray.length + "]: Inserts OK; " + HY104Sql.length + " HY104 redo inserts");
									mssqlTileInsertCallback(); // callback from zoomlevelProcessing async
								}
							} // End of mssqlTileInsert2End()
						);
					}
					else {
						winston.log("debug", "mssqlTileInsertEnd() [" + tileArray.length + "]: Inserts OK");
						tileArray=[];  						// Re-initialize tile array
						mssqlTileInsertCallback(); // callback from zoomlevelProcessing async
					} // End of mssqlTileInsertUnprepare()			
				} // End of mssqlTileInsertEnd()
			); // End of async()						

		} // End of mssqlTileInsert()
		
		/*
		 * Function: 	 tileIntersectsProcessingEnd()
		 * Parameters:	 rowCount, rowsProcessed, resultRows array,
		 *				database type, tiles CSV file stream, block, number of blocks, getBlockCallback
		 * Returns:		 Nothing
		 * Descrioption: Processing at end of zoomlevel
		 */
		function tileIntersectsProcessingEnd(rowCount, rowsProcessed, resultRows, 
			dbType, tilesCsvStream, block, numBlocks, getBlockCallback) {
				
			scopeChecker(__file, __line, {
				dbType: dbType,
				tilesCsvStream: tilesCsvStream,
				block: block,
				numBlocks: numBlocks,
				callback: getBlockCallback
			});
		
			var end = new Date().getTime();
			var elapsedTime=(end - zstart)/1000; // in S
			var tElapsedTime=(end - lstart)/1000; // in S
			
			if (rowsProcessed != rowCount) {
				throw new Error("tileIntersectsProcessingEnd(): rowsProcessed (" + rowsProcessed + 
					") != rowCount (" + rowCount + ")");
			}
			
			for (var i=0; i<resultRows.length; i++) { // Process rows
				totalTileSize+=tileIntersectsRowProcessing(resultRows[i], geography, dbType);	
			}
			
			if (tileArray.length == 0) {

				winston.log("info", 'Geolevel: ' + geolevel_id + '; zooomlevel: ' + zoomlevel + 
					'; END block: ' + block + '/' + numBlocks + '; ' + 
					rowCount + ' tile intersects processed no tiles in ' + elapsedTime + " S; " +  
					"total: " + tileNo + " tiles in " + tElapsedTime + " S; size: " + 
					(nodeGeoSpatialServicesCommon.fileSize(totalTileSize)|| "0 bytes"));					
				getBlockCallback(); // callback from zoomlevelProcessing async
			}	
			else if (tileArray.length > 0) { 
				// Process last tile if it exists
				var geojsonTile=tileArray[(tileArray.length-1)];
				if (geojsonTile) {
					winston.log("debug", "Process final tile: " + geojsonTile.tileId);
					totalTileSize+=geojsonTile.addTopoJson();				// Complete final tile	
				}	

				winston.log("info", 'Geolevel: ' + geolevel_id + '; zooomlevel: ' + zoomlevel + '; ' + 
					'block: ' + block + '/' + numBlocks + '; ' + 
					rowCount + ' tile intersects processed; ' + tileArray.length + " tiles in " + elapsedTime + " S; " +  
					Math.round((tileArray.length/elapsedTime)*100)/100 + " tiles/S" + 
					"; total: " + tileNo + " tiles in " + tElapsedTime + " S; size: " + 
					(nodeGeoSpatialServicesCommon.fileSize(totalTileSize)||"0 bytes"));
				var expectedRows=tileArray.length;
				numTiles+=tileArray.length;			
				
				function csvFileCallback() {
					if (createPngfile) { // Create SVG tiles	
						async.forEachOfSeries(tileArray, 
						function writeSVGTileSeries(value, j, writeSVGTileCallback) { // Processing code	
							// Write SVG tiles to disk

								writeSVGTile(tileMakerConfig.xmlConfig.xmlFileDir + "/" + "/tiles", 
									value.geolevel_id, value.zoomlevel, value.x, value.y, writeSVGTileCallback, value.svgTile);
									var svgTileFileName=getSVGTileFileName(tileMakerConfig.xmlConfig.xmlFileDir + "/" + "/tiles", 
										value.geolevel_id, value.zoomlevel, value.x, value.y)
								svgFileList[svgTileFileName + '.svg']=svgTileFileName + '.png';
								clipRectObj[svgTileFileName + '.svg']=value.svgTile.clipRect;
			//  C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\node_modules\phantomjs-prebuilt\lib\phantom\bin\phantomjs.exe
							},
							function writeSVGTileEnd(err) { //  Callback
				
								if (err) {
									dbErrorHandler(err);
								}
								else {							
									if (dbType == "PostGres") {
										pgTileInsert(expectedRows, getBlockCallback);	// Calls getBlockCallback()	
									}		
									else if (dbType == "MSSQLServer") {
										mssqlTileInsert(expectedRows, getBlockCallback);	// Calls getBlockCallback()	
									}			
								}
							}
						); // End of async.forEachOfSeries(tileArray, ...)	
					
					}
					else {					
						if (dbType == "PostGres") {
							pgTileInsert(expectedRows, getBlockCallback);	// Calls getBlockCallback()	
						}		
						else if (dbType == "MSSQLServer") {
							mssqlTileInsert(expectedRows, getBlockCallback);	// Calls getBlockCallback()	
						}	
					}					
				} // End of csvFileCallback()			
				
				var l=0;
				async.forEachOfSeries(tileArray, 
					function tileArrayCSVSeries(value, j, csvCallback) { // Processing code	
						l++;
//						winston.log("verbose", "value[" + l + "/" + tileArray.length + "]: " + JSON.stringify(value).substring(0, 200));
						buf=value.toCSV(dbType);
						
//						winston.log("verbose", "buf[" + l + "/" + tileArray.length + "]: " + JSON.stringify(buf).substring(0, 200));
						buf+="\r\n";
						if (l >= 1000) {
							l=0;
							var nextTickFunc = function nextTick() {
								tilesCsvStream.write(buf, csvCallback);
							}
							process.nextTick(nextTickFunc);
						}
						else {
							tilesCsvStream.write(buf, csvCallback);
						} 		
					}, // End of tileArrayCSVSeries
					function tileArrayCSVSeriesEnd(err) { //  Callback
						csvFileCallback(err);
					} // End of tileArrayCSVSeriesEnd()		
				); // End of async.forEachOfSeries()								
			} // tileArray.length > 0					
		} // End of tileIntersectsProcessingEnd()

		/*
		 * Function: 	 blockProcessing()
		 * Parameters:	 SQL, zoomlevel, geolevel_id, block, number of blocks, geolevelName, getBlockCallback
		 * Returns:		 Nothing
		 * Descrioption: Process a block of geolevel/zoomlevel data
		 */
		function blockProcessing(sql, zoomlevel, geolevel_id, block, numBlocks, geolevelName, getBlockCallback) {
					
			scopeChecker(__file, __line, {
				tilesCsvStream: tilesCsvStream,
				block: block,
				numBlocks: numBlocks,
				zoomlevel: zoomlevel,
				geolevel_id: geolevel_id,
				geolevelName: geolevelName,
				sql: sql,
				callback: getBlockCallback
			});
			
			winston.log("debug", "Block: " + block + "/" + numBlocks + "; geolevel_id: " + 
				geolevel_id + "; zoomlevel: " + zoomlevel + 
				"\nSQL> " + sql);
			
			zstart = new Date().getTime(); // Set timer for zoomlevel/geolevel	
			var rowsProcessed=0;
			
			if (dbType == "PostGres") {
				request=client;
				query = request.query(sql, [zoomlevel, geolevel_id, block]);
				stream=query;
			}		
			else if (dbType == "MSSQLServer") {	
				request=new dbSql.Request();
				request.stream=true;
				stream=request;
				request.input('zoomlevel', zoomlevel);
				request.input('geolevel_id', geolevel_id);
				request.input('block', block);
				query = request.query(sql);
			}
			stream.on('error', dbErrorHandler);

			var resultRows={};
			stream.on('row', function tileIntersectsRow(row) {
				rowsProcessed++;

				delete row.geolevel_name;
				if (resultRows[row.tile_id] == undefined) {
					resultRows[row.tile_id]=[];
				}
				resultRows[row.tile_id].push(row);
				
				winston.log("debug", "SELECT[" + rowsProcessed + "] " +
					"Block: " + row.block +
					"; tile_id[" + (resultRows[row.tile_id].length-1) + "]: " + 
						row.tile_id +
					"; x: " + row.x +
					"; y: " + row.y +
					"; areaid: " + row.areaid);
			}); // End of stream.on('row'...);
			
			if (dbType == "MSSQLServer") {
				stream.on('done', function endBlockProcessingStream(returnValue, affected) {
					var nresultRows=[];
					for (tile in resultRows) {
						nresultRows=nresultRows.concat(resultRows[tile]); // Reorder array by tile ids
					}
					if (nresultRows.length > 0) {
						var xmlFileDir=tileMakerConfig.xmlConfig.xmlFileDir;
						var rows=[];
						rows.push({ // Fake first row for column headings
							geolevel_id: nresultRows[0].geolevel_id,
							zoomlevel: nresultRows[0].zoomlevel,
							x: nresultRows[0].x,
							y: nresultRows[0].y,
							tile_id: nresultRows[0].tile_id,
							areaid_count: 1,
							optimised_topojson: '{"type": "FeatureCollection","features":[]}'});
							
						if (fs.existsSync(xmlFileDir + "/data")) {
							dbLoad.createSqlServerFmtFile(xmlFileDir + "/data", "t_tiles_" + geolevelName.toLowerCase(), rows, 
								function tileIntersectsProcessingEndCallback(err) {							
									sql=undefined;
									tileIntersectsProcessingEnd(nresultRows.length, rowsProcessed, nresultRows, 
										dbType, tilesCsvStream, block, numBlocks, getBlockCallback);
								});
						}
						else {
							dbLoad.createSqlServerFmtFile(xmlFileDir, "t_tiles_" + geolevelName.toLowerCase(), rows, 
								function tileIntersectsProcessingEndCallback(err) {							
									sql=undefined;
									tileIntersectsProcessingEnd(nresultRows.length, rowsProcessed, nresultRows, 
										dbType, tilesCsvStream, block, numBlocks, getBlockCallback);
								});
						}
					}	
					else {
						tileIntersectsProcessingEnd(nresultRows.length, rowsProcessed, nresultRows, 
							dbType, tilesCsvStream, block, numBlocks, getBlockCallback);
					}
				}); // End of stream.on('end'...);
			}		
			else if (dbType == "PostGres") {			
				stream.on('end', function endBlockProcessingStream(result) {		
					var nresultRows=[];
					for (tile in resultRows) {
						nresultRows=nresultRows.concat(resultRows[tile]); // Reorder array by tile ids
					}
					sql=undefined;
					tileIntersectsProcessingEnd(nresultRows.length, rowsProcessed, nresultRows, 
						dbType, tilesCsvStream, block, numBlocks, getBlockCallback);
				}); // End of stream.on('end'...);
			}			
		} // End of blockProcessing()
		
		var request;
		var query;
		var stream;		
		var endEventName;
		
		var request;
		if (dbType == "PostGres") {
			request=client;
		}		
		else if (dbType == "MSSQLServer") {	
			request=new dbSql.Request();
		}
		
		blockSql="SELECT zoomlevel, geolevel_id, block, COUNT(block) AS total FROM tile_blocks_" + 
			geography.toLowerCase() + "\n" +
		    " WHERE zoomlevel   = " + zoomlevel  + "\n" +
		    "   AND geolevel_id = " + geolevel_id  + "\n" +
			" GROUP BY zoomlevel, geolevel_id, block"+
			" ORDER BY zoomlevel, geolevel_id, block";
		var query=request.query(blockSql, function getBlock(err, result) {
			if (err) {
				dbErrorHandler(err, blockSql);
			}
			else {
				var boundData;
				
				if (dbType == "PostGres") {
					boundData=result.rows;
				}		
				else if (dbType == "MSSQLServer") {	
					if (result.recordset) {
						boundData=result.recordset;
					}
					else {
						boundData=result;
					}
				}		  
				if (boundData.length < 1) {
					if (geolevel_id == 1 && zoomlevel > 0) {
						blockProcessing(sql, zoomlevel, geolevel_id, 0, 0, geolevelName, tileIntersectsProcessingCallback);	
					}
					else {
						dbErrorHandler(new Error("getBlock() table: tile_blocks_" + geography + 
							" fetch rows <1 (" + boundData.length + ") for geolevel_id: " + geolevel_id + "; zoomlevel: " + zoomlevel), 
							blockSql);
					}	
				}		
				else {				
					async.forEachOfSeries(boundData, 
						function boundDataProcessing(value, i, getBlockCallback) { // Processing code	
							blockProcessing(sql, 
								boundData[i].zoomlevel, boundData[i].geolevel_id, boundData[i].block, 
								boundData.length, geolevelName, getBlockCallback);
						}, // End of boundDataProcessing
						function boundDataEnd(err) { //  Callback
							tileIntersectsProcessingCallback(err);
						} // End of boundDataEnd()		
					); // End of async.forEachOfSeries()	
				}				
			}
		}); // End of request.query()
		
	} // End of tileIntersectsProcessing()

	/*
	 * Function: 	getDataLoaderParameter()
	 * Parameters:	Data loader object (dataLoader in XML), parameter name, default Parameter
	 * Returns:		Value
	 * Description:	Get data loader parameter. Cope with single arrays instead of object itemdsds
	 */		
	function getDataLoaderParameter(dataLoader, parameter, defaultParameter) {

		var value=dataLoader[parameter];
		if (value == undefined) {
			if (defaultParameter) {
				value=defaultParameter;
				if (typeof value == "object") {
					winston.log("verbose", "Parameter: " + parameter + '="' + JSON.stringify(dataLoader, value, 2) + '" [DEFAULT]');
				}
				else {
					winston.log("verbose", "Parameter: " + parameter + '="' + value + '" [DEFAULT]');
				}
			}
			else {
				dbErrorHandler(new Error("Unable to get dataLoder parameter: " + parameter + "; dataLoder object: " + JSON.stringify(dataLoader, null, 2)), 
					undefined /* SQL */);
			}
		}
		if (typeof value == "object") {
			winston.log("verbose", "Parameter: " + parameter + '="' + JSON.stringify(dataLoader, value, 2) + '"');
		}
		else {
			winston.log("verbose", "Parameter: " + parameter + '="' + value + '"');
		}
		
		return value;
	} // End of getDataLoaderParameter()
	
	/*
	 * Function: 	adjacencyProcessing()
	 * Parameters:	adjacency processing callback: geometryProcessing(),
	 *				Geography table object (dataLoader in XML), XML file directory (original location of XML file)
	 * Returns:		Nothing
	 * Description:	Dump adjacency tables to CSV, call lookup processing callback: geometryProcessing()
	 */	 
	 function adjacencyProcessing(adjacencyProcessingCallback, dataLoader, xmlFileDir) {
		var geographyName=getDataLoaderParameter(dataLoader, "geographyName");
		var geoLevel=getDataLoaderParameter(dataLoader, "geoLevel");
		var geographyTableDescription=getDataLoaderParameter(dataLoader, "geographyDesc");
		
		var adjacencyTable=getDataLoaderParameter(dataLoader, "adjacencyTable", "adjacency_" + geographyName);
		adjacencyTable=adjacencyTable.toString().toLowerCase();
		var csvFileName;
		
		var request;
		var sql="SELECT geolevel_id, areaid, num_adjacencies, adjacency_list\n" +
				"  FROM " + adjacencyTable + " ORDER BY geolevel_id, areaid";
		if (dbType == "PostGres") {
			if (fs.existsSync(xmlFileDir + "/data")) {
				csvFileName=xmlFileDir + "/data/pg_" + adjacencyTable + ".csv";
			}
			else {
				csvFileName=xmlFileDir + "/pg_" + adjacencyTable + ".csv";
			}
			request=client;
		}		
		else if (dbType == "MSSQLServer") {	
		
			if (fs.existsSync(xmlFileDir + "/data")) {
				csvFileName=xmlFileDir + "/data/mssql_" + adjacencyTable + ".csv";
			}
			else {
				csvFileName=xmlFileDir + "/mssql_" + adjacencyTable + ".csv";
			}
			request=new dbSql.Request();
		}					

		try { // Create CSV file for adjacency table
			var adjacencyCsvStream = fs.createWriteStream(csvFileName, { flags : 'w' });
			winston.log("info", "Creating adjacency CSV file: " + csvFileName + 
				" for " + geographyName + ": " + geographyTableDescription);	
			adjacencyCsvStream.on('finish', function csvStreamClose() {
				winston.log("verbose", "adjacency csvStreamClose(): " + csvFileName);
			});		
			adjacencyCsvStream.on('error', function csvStreamError(e) {
				winston.log("error", "Exception in adjacency CSV write to file: " + csvFileName, e.message);			
				dbTileMakerCallback(e);											
			});
		}
		catch (e) {
			dbErrorHandler(e, sql);
		}

		var query=request.query(sql, function(err, recordSet) {
	
			if (err) {		
				dbErrorHandler(err, sql);
			}
			else {	
//				winston.log("verbose", "SQL> " + sql);
				var record;
				if (dbType == "PostGres") {
					record=recordSet.rows;
				}
				else if (dbType == "MSSQLServer") {	
					if (recordSet.recordset) {
						record=recordSet.recordset;
					}
					else {
						record=recordSet;
					}
				}			
				var rowsAffected=record.length;
				if (rowsAffected == undefined || rowsAffected == 0) {
					dbErrorHandler(new Error("No rows returned in adjacency SELECT"), sql);
				}
				var buf=Object.keys(record[0]).join(',').toUpperCase(); // Header
				buf+="\r\n";
				for (var i=0; i<record.length; i++) {
					var keys=Object.keys(record[i]);
					var values=[];
					for (var j=0; j<keys.length; j++) {
						var str=record[i][keys[j]].toString();
						str=str.split('"' /* search: " */).join('""' /* replacement: "" */);	// CSV escape data 	
						values.push(str);
					}
					
					buf+='"' + values.join('","') + '"\r\n'; // Quote enclose data
				}
				adjacencyCsvStream.write(buf, function adjacencyProcessingWrite(err) {
					if (err) {
						adjacencyProcessingCallback(err);
					}
					else {
						adjacencyCsvStream.end();
						
						if (dbType == "MSSQLServer") {	
							if (fs.existsSync(xmlFileDir + "/data")) {
								dbLoad.createSqlServerFmtFile(xmlFileDir + "/data", adjacencyTable, record, adjacencyProcessingCallback);	
							}
							else {
								dbLoad.createSqlServerFmtFile(xmlFileDir, adjacencyTable, record, adjacencyProcessingCallback);	
							}
						}
						else {
							adjacencyProcessingCallback();
						}
					}
					
				});
			}
		});	
		
	 } // End of adjacencyProcessing()
	 
	/*
	 * Function: 	lookupProcessing()
	 * Parameters:	lookup processing callback: adjacencyProcessing(),
	 *				Geography table object (dataLoader in XML), XML file directory (original location of XML file)
	 * Returns:		Nothing
	 * Description:	Dump lookup tables to CSV, call lookup processing callback: adjacencyProcessing()
	 */	
	 
	 function lookupProcessing(lookupProcessingCallback, dataLoader, xmlFileDir) {
		var geographyName=getDataLoaderParameter(dataLoader, "geographyName");
		var geoLevel=getDataLoaderParameter(dataLoader, "geoLevel");
		var geographyTableDescription=getDataLoaderParameter(dataLoader, "geographyDesc");
		
		if (geoLevel) {
			async.forEachOfSeries(geoLevel, 
				function geoLevelProcessing(value, i, geoLevelCallback) {
					
					var lookupTable=getDataLoaderParameter(value, "lookupTable")||"lookup_" + geographyName;
					lookupTable=lookupTable.toString().toLowerCase();
					var csvFileName;
					
					var request;
					var shapefileTable=geoLevel[i].shapeFileTable.toString().toLowerCase();
					var sql;
					if (dbType == "PostGres") {	
						if (fs.existsSync(xmlFileDir + "/data")) {
							csvFileName=xmlFileDir + "/data/pg_" + lookupTable + ".csv";
						}
						else {
							csvFileName=xmlFileDir + "/pg_" + lookupTable + ".csv";
						}
						request=client;
						sql="SELECT " + shapefileTable + ", areaname, gid, geographic_centroid::Text AS geographic_centroid\n" +
							"  FROM " + lookupTable + " ORDER BY gid";
					}		
					else if (dbType == "MSSQLServer") {	
						if (fs.existsSync(xmlFileDir + "/data")) {
							csvFileName=xmlFileDir + "/data/mssql_" + lookupTable + ".csv";
						}
						else {
							csvFileName=xmlFileDir + "/mssql_" + lookupTable + ".csv";
						}
						request=new dbSql.Request();
						sql="SELECT " + shapefileTable + ", areaname, gid, geographic_centroid\n" +
							"  FROM " + lookupTable + " ORDER BY gid";
					}					

					try { // Create CSV file for lookup table
						var lookupCsvStream = fs.createWriteStream(csvFileName, { flags : 'w' });
						winston.log("info", "Creating lookup CSV file: " + csvFileName + 
							" for " + geographyName + ": " + geographyTableDescription);	
						lookupCsvStream.on('finish', function csvStreamClose() {
							winston.log("verbose", "lookup csvStreamClose(): " + csvFileName);
						});		
						lookupCsvStream.on('error', function csvStreamError(e) {
							winston.log("error", "Exception in lookup CSV write to file: " + csvFileName, e.message);		
							dbTileMakerCallback(e);												
						});
					}
					catch (e) {
						dbErrorHandler(e, sql);
					}

					var query=request.query(sql, function(err, recordSet) {
				
						if (err) {		
							dbErrorHandler(err, sql);
						}
						else {	
			//				winston.log("verbose", "SQL> " + sql);
							var record;
							if (dbType == "PostGres") {
								record=recordSet.rows;
							}
							else if (dbType == "MSSQLServer") {	
								if (recordSet.recordset) {
									record=recordSet.recordset;
								}
								else {
									record=recordSet;
								}
							}			
							var rowsAffected=record.length;
							if (rowsAffected == undefined || rowsAffected == 0) {
								dbErrorHandler(new Error("No rows returned in lookup SELECT"), sql);
							}
							var buf=Object.keys(record[0]).join(',').toUpperCase(); // Header
							buf+="\r\n";
							for (var i=0; i<record.length; i++) {
								var keys=Object.keys(record[i]);
								var values=[];
								for (var j=0; j<keys.length; j++) {
									var str=record[i][keys[j]].toString();
									str=str.split('"' /* search: " */).join('""' /* replacement: "" */);	// CSV escape data 	
									values.push(str);
								}
								
								buf+='"' + values.join('","') + '"\r\n'; // Quote enclose data
							}
							lookupCsvStream.write(buf, function lookupProcessingWrite(err) {
								if (err) {
									geoLevelCallback(err);
								}
								else {
									lookupCsvStream.end();
									
									if (dbType == "MSSQLServer") {	
										if (fs.existsSync(xmlFileDir + "/data")) {
											dbLoad.createSqlServerFmtFile(xmlFileDir + "/data", lookupTable, record, geoLevelCallback);	
										}
										else {
											dbLoad.createSqlServerFmtFile(xmlFileDir, lookupTable, record, geoLevelCallback);	
										}
									}
									else {
										geoLevelCallback();
									}
								}
								
							});
						}
					});						
				},
				function geoLevelError(err) {
					lookupProcessingCallback(err);
				}
			); // End of async.forEachOfSeries()	
			
		}
		else {		
			dbErrorHandler(new Error("lookupProcessing() geoLevel id not defined"), undefined /*sql */);
		}			
				 
	 } // End of lookupProcessing()
	 
	/*
	 * Function: 	hierarchyProcessing()
	 * Parameters:	hierarchy processing callback: lookupProcessing(),
	 *				Geography table object (dataLoader in XML), XML file directory (original location of XML file)
	 * Returns:		Nothing
	 * Description:	Dump hierarchy tsbles to CSV, call hierarchy processing callback: lookupProcessing()
	 */	
	 function hierarchyProcessing(hierarchyProcessingCallback, dataLoader, xmlFileDir) {
		 
		var geographyName=getDataLoaderParameter(dataLoader, "geographyName");
		var hierarchyTable=getDataLoaderParameter(dataLoader, "hierarchyTable")||"hierarchy_" + geographyName;
		hierarchyTable=hierarchyTable.toString().toLowerCase();
		var csvFileName;
		var geographyTableDescription=getDataLoaderParameter(dataLoader, "geographyDesc");
		
		var request;
		if (dbType == "PostGres") {
			if (fs.existsSync(xmlFileDir + "/data")) {
				csvFileName=xmlFileDir + "/data/pg_" + hierarchyTable + ".csv";
			}
			else {
				csvFileName=xmlFileDir + "/pg_" + hierarchyTable + ".csv";
			}
			request=client;
		}		
		else if (dbType == "MSSQLServer") {	
			if (fs.existsSync(xmlFileDir + "/data")) {
				csvFileName=xmlFileDir + "/data/mssql_" + hierarchyTable + ".csv";
			}
			else {
				csvFileName=xmlFileDir + "/mssql_" + hierarchyTable + ".csv";
			}
			request=new dbSql.Request();
		}
		var sql="SELECT * FROM " + hierarchyTable;
		
		var geoLevel=getDataLoaderParameter(dataLoader, "geoLevel");
		for (var i=1; i<=geoLevel.length; i++) { // Add ordering
			if (i == 1) {
				sql+="\n ORDER BY 1";
			}
			else {
				sql+=", " + i;
			}
		}
		try { // Create CSV file for hierarchy table
			var hierarchyCsvStream = fs.createWriteStream(csvFileName, { flags : 'w' });
			winston.log("info", "Creating hierarchy CSV file: " + csvFileName + 
				" for " + geographyName + ": " + geographyTableDescription);	
			hierarchyCsvStream.on('finish', function csvStreamClose() {
				winston.log("verbose", "hierarchy csvStreamClose(): " + csvFileName);
			});		
			hierarchyCsvStream.on('error', function csvStreamError(e) {
				winston.log("error", "Exception in hierarchy CSV write to file: " + csvFileName, e.message);	
				dbTileMakerCallback(e);				
			});
		}
		catch (e) {
			dbErrorHandler(e, sql);
		}

		var query=request.query(sql, function(err, recordSet) {
	
			if (err) {		
				dbErrorHandler(err, sql);
			}
			else {	
				winston.log("debug", "SQL> " + sql);
				var record;
				if (dbType == "PostGres") {
					record=recordSet.rows;
				}
				else if (dbType == "MSSQLServer") {	
					if (recordSet.recordset) {
						record=recordSet.recordset;
					}
					else {
						record=recordSet;
					}
				}			
				var rowsAffected=record.length;
				if (rowsAffected == undefined || rowsAffected == 0) {
					dbErrorHandler(new Error("No rows returned in hierarchy SELECT"), sql);
				}
				var buf=Object.keys(record[0]).join(',').toUpperCase(); // Header
				buf+="\r\n";
				for (var i=0; i<record.length; i++) {
					var keys=Object.keys(record[i]);
					var values=[];
					for (var j=0; j<keys.length; j++) {
						var str=record[i][keys[j]];
						str=str.split('"' /* search: " */).join('""' /* replacement: "" */);	// CSV escape data 	
						values.push(str);
					}
					
					buf+='"' + values.join('","') + '"\r\n'; // Quote enclose data					
				}
				hierarchyCsvStream.write(buf, function hierarchyProcessingWrite(err) {
					if (err) {
						hierarchyProcessingCallback(err);
					}
					else {
						hierarchyCsvStream.end();
						
						if (dbType == "MSSQLServer") {	
							if (fs.existsSync(xmlFileDir + "/data")) {
								dbLoad.createSqlServerFmtFile(xmlFileDir + "/data", hierarchyTable, record,	hierarchyProcessingCallback);
							}
							else {							
								dbLoad.createSqlServerFmtFile(xmlFileDir, hierarchyTable, record,	hierarchyProcessingCallback);	
							}
						}
						else {
							hierarchyProcessingCallback();
						}
					}
				});
			}
		});			
	} // End of hierarchyProcessing()
	
	/*
	 * Function: 	geometryProcessing()
	 * Parameters:	Geometry processing callback: tileProcessing(),
	 *				Geography table object (dataLoader in XML), XML file directory (original location of XML file), 
	 *				tile processing callback function: tileIntersectsProcessingGeolevelLoop
	 * Returns:		Nothing
	 * Description:	Dump geometry tsbles to CSV, call Geometry processing callback: tileProcessing()
	 */		
	function geometryProcessing(geometryProcessingCallback, dataLoader, xmlFileDir, tileProcessingCallback) {

		var geographyName=getDataLoaderParameter(dataLoader, "geographyName").toString();
		var geographyTable="geography_" + geographyName;
		var geometryTable=getDataLoaderParameter(dataLoader, "geometryTable");
		geometryTable=geometryTable.toString().toLowerCase();
		var hierarchyTable=getDataLoaderParameter(dataLoader, "hierarchyTable");
		var geographyTableDescription=getDataLoaderParameter(dataLoader, "geographyDesc");
	
		var csvFileName;
		
		var l=0;
		var request;
		var sql;
		if (dbType == "PostGres") {
			if (fs.existsSync(xmlFileDir + "/data")) {
				csvFileName=xmlFileDir + "/data/pg_" + geometryTable + ".csv";	
			}
			else {
				csvFileName=xmlFileDir + "/pg_" + geometryTable + ".csv";	
			}
			sql="SELECT geolevel_id, areaid, zoomlevel, ST_AsText(geom) AS wkt FROM " + geometryTable /* geometry table */;
			request=client;
		}		
		else if (dbType == "MSSQLServer") {	
			if (fs.existsSync(xmlFileDir + "/data")) {
				csvFileName=xmlFileDir + "/data/mssql_" + geometryTable + ".csv";
			}
			else {
				csvFileName=xmlFileDir + "/mssql_" + geometryTable + ".csv";	
			}
			sql="SELECT geolevel_id, areaid, zoomlevel, geom.STAsText() AS wkt FROM " + geometryTable /* geometry table */;
			request=new dbSql.Request();
		}
		if (maxZoomlevel && maxZoomlevel > 6 && maxZoomlevel != 11) { // Changed from CLI default
			sql+=" WHERE zoomlevel <= " + maxZoomlevel;
		}
				
		try { // Create CSV file for geometry table
			var geometryCsvStream = fs.createWriteStream(csvFileName, { flags : 'w' });
			winston.log("info", "Creating geometry CSV file: " + csvFileName + " for " + geographyName + ": " + geographyTableDescription);	
			geometryCsvStream.on('finish', function csvStreamClose() {
				winston.log("verbose", "geometry csvStreamClose(): " + csvFileName);
			});		
			geometryCsvStream.on('error', function csvStreamError(e) {
				winston.log("error", "Exception in geometry CSV write to file: " + csvFileName, e.message);										
			});
		}
		catch (e) {
			dbErrorHandler(e, sql);
		}
		
		var query=request.query(sql, function(err, recordSet) {
	
			if (err) {		
				dbErrorHandler(err, sql);
			}
			else {	
//				winston.log("verbose", "SQL> " + sql);
				var record;
				if (dbType == "PostGres") {
					record=recordSet.rows;
				}
				else if (dbType == "MSSQLServer") {	
					if (recordSet.recordset) {
						record=recordSet.recordset;
					}
					else {
						record=recordSet;
					}
				}			
				var rowsAffected=record.length;
				if (rowsAffected == undefined || rowsAffected == 0) {
					dbErrorHandler(new Error("No rows returned in Geometry SELECT"), sql);
				}
				else {
					var buf=Object.keys(record[0]).join(',').toUpperCase(); // Header
					buf+="\r\n";
					geometryCsvStream.write(buf, function geometryCsvHeaderCallback(err) {
						if (err) {
							geometryProcessingCallback(err);
						}
						else {
							async.forEachOfSeries(record, 
								function mssqlTileGeometrySeries(value, i, mssqlTileGeometryCallback) { // Processing code		
									var str;
									if (Array.isArray(value.wkt)) {			
										str=value.wkt.join("");
									}
									else {			
										str=value.wkt;
									}
									if (str) {
			//								winston.log("verbose", "str: " + JSON.stringify(str).substring(0, 200));
										str=str.split('"' /* search: " */).join('""' /* replacement: "" */);	// CSV escape data 	
									}
									else {
										str="";
									}							
									var buf='"'+ value.geolevel_id + '","' + value.areaid + '","' + value.zoomlevel + '","' + str + '"';
			//							winston.log("verbose", "buf[" + (i+1) + "/" + rowsAffected + "]: " + JSON.stringify(buf).substring(0, 200));
									buf+="\r\n";
									
									function mssqlTileGeometryCallback2(err) {
										buf=undefined;
										value=undefined;
										mssqlTileGeometryCallback(err);
									}
									
									if (l >= 1000) {
										l=0;
										var nextTickFunc = function nextTick() {
											geometryCsvStream.write(buf, mssqlTileGeometryCallback2);
										}
										process.nextTick(nextTickFunc);
									}
									else {
										geometryCsvStream.write(buf, mssqlTileGeometryCallback2);
									} 	
								}, // End of mssqlTileGeometrySeries
								function tmssqlTileGeometryEnd(err) { //  Callback				
									geometryCsvStream.end();
									winston.log("verbose", "Geometry rows processed: " + rowsAffected);
									
									function geographyTableProcessingCallback(err) {
										record=undefined;
										geometryProcessingCallback(err, 
											geographyName, geographyTable, geographyTableDescription, 
											xmlFileDir, tileProcessingCallback); // Call tileProcessing
									}
									
									if (dbType == "MSSQLServer") {	
										if (fs.existsSync(xmlFileDir + "/data")) {
											dbLoad.createSqlServerFmtFile(xmlFileDir + "/data", geometryTable, record,
												geographyTableProcessingCallback);	
										}
										else {
											dbLoad.createSqlServerFmtFile(xmlFileDir, geometryTable, record,
												geographyTableProcessingCallback);	
										}
									}
									else {
										geographyTableProcessingCallback();
									}
								} // End of tmssqlTileGeometryEnd()		
							); // End of async.forEachOfSeries()
						}			
					});	
				}
			}
		});
	} // End of geometryProcessing()

	/*
	 * Function: 	tileTests()
	 * Parameters:	Geograpy table, callback
	 * Returns:		Nothing
	 * Description:	Run tile tests
	 */	
	function tileTests(geographyTable, tileTestsEndCallback) {
		var tests=[];
		tests.push({
			description: "Missing tiles in tile blocks",
			sql: "SELECT geolevel_id, zoomlevel, x, y\n" +
				"  FROM tile_blocks_" + geographyTable.toLowerCase() + "\n" +
				"EXCEPT\n" +
				"SELECT geolevel_id, zoomlevel, x, y\n" +
				"  FROM t_tiles_" + geographyTable.toLowerCase() + "\n" +
				" ORDER BY 1, 2, 3, 4",
			results: undefined,
			passed: undefined
		});
		tests.push({
			description: "Missing tiles in tile interescts",
			sql: "SELECT geolevel_id, zoomlevel, x, y\n" +
				"  FROM tile_intersects_" + geographyTable.toLowerCase() + "\n" +
				"EXCEPT\n" +
				"SELECT geolevel_id, zoomlevel, x, y\n" +
				"  FROM t_tiles_" + geographyTable.toLowerCase() + "\n" +
				" ORDER BY 1, 2, 3, 4",
			results: undefined,
			passed: undefined
		});
		tests.push({
			description: "Missing tile blocks in tile interescts",
			sql: "SELECT geolevel_id, zoomlevel, x, y\n" +
					"  FROM tile_intersects_" + geographyTable.toLowerCase() + "\n" +
					"EXCEPT\n" +
					"SELECT geolevel_id, zoomlevel, x, y\n" +
					"  FROM tile_blocks_" + geographyTable.toLowerCase() + "\n" +
					" ORDER BY 1, 2, 3, 4",
			results: undefined,
			passed: undefined
		});
		tests.push({
			description: "Extra tiles not in blocks",
			sql: "SELECT geolevel_id, zoomlevel, x, y\n" +
				"  FROM t_tiles_" + geographyTable.toLowerCase() + "\n" +
				"EXCEPT\n" +
				"SELECT geolevel_id, zoomlevel, x, y\n" +
				"  FROM tile_blocks_" + geographyTable.toLowerCase() + "\n" +
				" ORDER BY 1, 2, 3, 4",
			results: undefined,
			passed: undefined
		});
		tests.push({
			description: "Extra tiles not in tile intersects",
			sql: "SELECT geolevel_id, zoomlevel, x, y\n" +
				"  FROM t_tiles_" + geographyTable.toLowerCase() + "\n" +
				"EXCEPT\n" +
				"SELECT geolevel_id, zoomlevel, x, y\n" +
				"  FROM tile_intersects_" + geographyTable.toLowerCase() + "\n" +
				" ORDER BY 1, 2, 3, 4",
			results: undefined,
			passed: undefined
		});
		tests.push({
			description: "Extra tile blocks not in tile intersects",
			sql: "SELECT geolevel_id, zoomlevel, x, y\n" +
				"  FROM tile_blocks_" + geographyTable.toLowerCase() + "\n" +
				"EXCEPT\n" +
				"SELECT geolevel_id, zoomlevel, x, y\n" +
				"  FROM tile_intersects_" + geographyTable.toLowerCase() + "\n" +
				" ORDER BY 1, 2, 3, 4",
			results: undefined,
			passed: undefined
		});
					
		var passed=0;
		var failed=0;
		
		async.forEachOfSeries(tests, 
			function tileTestsSeries(value, i, tileTestsCallback) { // Processing code
				
				winston.log("debug", "SQL> " + value.sql);
				var request;
				if (dbType == "PostGres") {
					request=client;
				}		
				else if (dbType == "MSSQLServer") {	
					request=new dbSql.Request();
				}
				
				var query=request.query(value.sql, function(err, recordSet) {
			
					if (err) {		
						dbErrorHandler(err, sql, tileTestsCallback);
					}
					else {			
						if (dbType == "PostGres") {
							value.results=recordSet.rows;
						}
						else if (dbType == "MSSQLServer") {
							if (recordSet.recordset) {
								value.results=recordSet.recordset;
							}
							else {
								value.results=recordSet;
							}
						}			
						var rowsAffected=(value.results || value.results.length);
						if (rowsAffected == undefined || rowsAffected == 0) {
							value.passed=true;
							passed++;
							winston.log("verbose", "Test [" + (i+1) + "] passed: " + value.description);
						}	
						else {
							value.passed=false;
							failed++;
							var str="";
							var hdr="";
							var hdr2="";
							var padStr="                    ";
							var padStr2="--------------------";
							for (var k=0; (k<value.results.length && k < 20); k++) {
								keys=Object.keys(value.results[k]);
								for (var j=0; j<keys.length; j++) {
									if (k == 0) {
										hdr+=(padStr + keys[j]).slice(-20) + " ";
										hdr2+=(padStr2).slice(-20) +" ";
									}
									str+=(padStr + (value.results[k][keys[j]]||"")).slice(-20) + " ";
								}
								str+="\n";
							}
							winston.log("warn", "Test [" + (i+1) + "] failed: " + value.description + " (first 20 rows)\n" +
								(hdr||"") + "\n" + (hdr2||"") + "\n" + str);
						}
						tileTestsCallback();
					}
				});
			}, // End of tileTestsSeries()
			function tileTestsSeriesEnd(err) {
				if (err) {
					dbErrorHandler(err, undefined /* sql */, tileTestsEndCallback);
				}
				else if (failed > 0) {
					tileTestsEndCallback(
						new Error("tileTestsSeriesEnd() " + failed + " tests failed; " + passed + " passed"));
				}
				else  {
					winston.log("info", "All " + passed + " tests passed, none failed");
			/* 
			 * Generate PIVOT the old way:
			 
			WITH a AS (
	SELECT geolevel_id, zoomlevel,   
		   SUM(CASE 
				WHEN optimised_topojson::Text = '{"type": "FeatureCollection","features":[]}' THEN 1 
				ELSE 0 END)::Text||'/'||
		   COUNT(tile_id)::Text AS tiles
	  FROM t_tiles_sahsuland
	 GROUP BY geolevel_id, zoomlevel
)
SELECT a2.zoomlevel, 
       a1.tiles AS geolevel_1,
       a2.tiles AS geolevel_2,
       a3.tiles AS geolevel_3,
       a4.tiles AS geolevel_4
  FROM a a2
	LEFT OUTER JOIN a a1 ON (a1.zoomlevel = a2.zoomlevel AND a1.geolevel_id = 1)
	LEFT OUTER JOIN a a3 ON (a3.zoomlevel = a2.zoomlevel AND a3.geolevel_id = 3)
	LEFT OUTER JOIN a a4 ON (a4.zoomlevel = a2.zoomlevel AND a4.geolevel_id = 4)
 WHERE a2.geolevel_id = 2
 ORDER BY 1;
  */
					var sql="WITH a AS (\n" +
"	SELECT geolevel_id, zoomlevel,\n";
					if (dbType == "PostGres") {
sql+="		   SUM(CASE\n" +
					"				WHEN optimised_topojson::Text = '{" + 
					'"type": "FeatureCollection","features"' + ":[]}' THEN 1\n" +
					"				ELSE 0 END)::Text||'/'||\n" +
					"		   COUNT(tile_id)::Text AS tiles\n";
					}		
					else if (dbType == "MSSQLServer") {	
sql+="		   CAST(SUM(CASE\n" +
					"				WHEN optimised_topojson = '{" + 
					'"type": "FeatureCollection","features"' + ":[]}' THEN 1\n" + 
					"				ELSE 0 END) AS VARCHAR) + '/' +\n" + 
					"		   CAST(COUNT(tile_id) AS VARCHAR) AS tiles\n";
					}
sql+="	  FROM t_tiles_" + geographyTable.toLowerCase() + "\n" +
"	 GROUP BY geolevel_id, zoomlevel\n" +
")\n" +
"SELECT a2.zoomlevel,\n";
					for (var i=1; i<=numGeolevels; i++) {
						if (i< numGeolevels) {
							sql+="       a" + i + ".tiles AS geolevel_" + i + ",\n";
						}
						else {
							sql+="       a" + i + ".tiles AS geolevel_" + i + "\n";
						}
					}
					sql+="  FROM a a2\n";
					for (var i=1; i<=numGeolevels; i++) {
						if (i != 2) { // Drive on geolevel 2
							sql+="	LEFT OUTER JOIN a a" + i + 
								" ON (a" + i + ".zoomlevel = a2.zoomlevel AND a" + i + ".geolevel_id = " + i + ")\n";
						}	
					}					
					sql+=" WHERE a2.geolevel_id = 2\n" +
						" ORDER BY 1";
					winston.log("debug", "SQL> " + sql);
					var request;
					if (dbType == "PostGres") {
						request=client;
					}		
					else if (dbType == "MSSQLServer") {	
						request=new dbSql.Request();
					}
					
					var query=request.query(sql, function(err, recordSet) {
			
						var results;
						if (err) {		
							dbErrorHandler(err, sql, tileTestsEndCallback);
						}
						else {			
							if (dbType == "PostGres") {
								results=recordSet.rows;
							}
							else if (dbType == "MSSQLServer") {	
								if (recordSet.recordset) {
									results=recordSet.recordset;
								}
								else {
									record=recordSet;
								}
							}			
							var rowsAffected=(results || results.length);
							var str="";
							var hdr="";
							var hdr2="";
							var padStr="                    ";
							var padStr2="--------------------";
							for (var k=0; k<results.length; k++) {
								keys=Object.keys(results[k]);
								for (var j=0; j<keys.length; j++) {
									if (k == 0) {
										hdr+=(padStr + keys[j]).slice(-20) + " ";
										hdr2+=(padStr2).slice(-20) +" ";
									}
									if (results[k][keys[j]] == 0 && keys[j] == "zoomlevel") {
										str+=(padStr + "0").slice(-20) + " ";
									}
									else {
										str+=(padStr + (results[k][keys[j]]||"")).slice(-20) + " ";
									}
								}
								str+="\n";
							}
							winston.log("info", "Zoomlevel and geolevel report (null tiles/total tiles)\n" +
								(hdr||"") + "\n" + (hdr2||"") + "\n" + str);
								
							tileTestsEndCallback();
						}
					});
				}
			} // End of tileTestsSeries()
		); // End of async.forEachOfSeries()
	} // End of tileTests()
	
	/*
	 * Function: 	tileProcessing()
	 * Parameters:	Error (from callback), geography Name, Geograpy table, geography table description, XML file directory (original location of XML file), 
	 *				callback function: tileIntersectsProcessingGeolevelLoop
	 * Returns:		Nothing
	 * Description:	Call getNumGeolevelsZoomlevels() then tile processing callback function: tileIntersectsProcessingGeolevelLoop() via callback
	 */
	function tileProcessing(err, geographyName, geographyTable, geographyTableDescription, xmlFileDir, tileProcessingCallback2) {
		if (err) {
			dbErrorHandler(err, sql);
		}
		else {		
			createTileBlocksTable(geographyName, function callGetNumGeolevelsZoomlevels() {
				getNumGeolevelsZoomlevels(geographyTable, geographyTableDescription, xmlFileDir, tileProcessingCallback2);
			});
		}
	} // End of tileProcessing()
	
	var tileArray = [];	
	var svgFileList = {};
	var clipRectObj = {}
	var totalTileSize=0;
	/*
	 * Function: 	getTileArray()
	 * Parameters:	None
	 * Returns:		Tile array
	 * Description:	Fetch tile array. So async modules can access the tile array
	 */
	function getTileArray() {
		return tileArray;
	} // End of getTileArray()
	
	var lstart = new Date().getTime();				// Overall processing start time
	var zstart;										// Zoomlevel processing start time
	var tileNo=0;									// Overall time counter
	var topojson_options = new TopojsonOptions();	// Topojson processor options
	var sql;										// SQL Statement
	
	var numZoomlevels; 
	var numGeolevels;
	var numTiles=0;
	if (tileMakerConfig.xmlConfig == undefined) {
		dbErrorHandler(new Error("No XML config"));	
	} 
	else if (tileMakerConfig.xmlConfig.dataLoader == undefined) {
		dbErrorHandler(new Error("No dataLoader in XML config"));
	} 
	else if (tileMakerConfig.xmlConfig.dataLoader[0] == undefined) {
		dbErrorHandler(new Error("No dataLoader element 0 in XML config"));
	} 
	else if (tileMakerConfig.xmlConfig.dataLoader[0].geographyName == undefined) {
		dbErrorHandler(new Error("No geography name in XML config"));
	}
	else if (tileMakerConfig.xmlConfig.xmlFileDir == undefined) {
		dbErrorHandler(new Error("No xmlFileDir in XML config"));
	}
	var xmlFileDir=tileMakerConfig.xmlConfig.xmlFileDir;
		
	var transaction=undefined; // MSSQL only
	startTransaction(function startTransactionCallback2(err) {
		
		addUserToPath(function addUserToPathCallback2(err) {
			var hierarchyProcessingCallback=function hierarchyProcessingCallback(err) {
				if (err) {
					dbErrorHandler(err);
				}
				else {
					var lookupProcessingCallback=function lookupProcessingCallback(err) {
						if (err) {
							dbErrorHandler(err);
						}
						else {
							var adjacencyProcessingCallback=function adjacencyProcessingCallback(err) {
								if (err) {
									dbErrorHandler(err);
								}
								else {
									geometryProcessing(tileProcessing, tileMakerConfig.xmlConfig.dataLoader[0], 
										xmlFileDir, tileIntersectsProcessingGeolevelLoop);
								}
								
							};
							
							adjacencyProcessing(adjacencyProcessingCallback, tileMakerConfig.xmlConfig.dataLoader[0], 
								xmlFileDir);
						}
					}	

					lookupProcessing(lookupProcessingCallback, tileMakerConfig.xmlConfig.dataLoader[0], 
						xmlFileDir);						
				}
			}

			hierarchyProcessing(hierarchyProcessingCallback, tileMakerConfig.xmlConfig.dataLoader[0], 
				xmlFileDir);			
		});			
	});
	
}

module.exports.dbTileMaker = dbTileMaker;

//
// Eof