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
	  proj4 = require("proj4"),
	  wellknown = require('wellknown'),
	  topojson = require('topojson'),
	  sizeof = require('object-sizeof'),
	  svg2png = require('svg2png-many');

/*
 * Function: 	createSVGTile
 * Parameters:	geolevel, zoomlevel, X, Y, callback, insertion geoJSON
 * Returns:		tile X
 * Description: Create SVG tile from geoJSON
 */
var createSVGTile = function createSVGTile(geolevel_id, zoomlevel, x, y, geojson) {
	scopeChecker(__file, __line, {
		geojson: geojson,
		bbox: geojson.bbox
	});
//
// Get bounding box from intersection, reproject to 3857
//
	var bboxPolygon = turf.bboxPolygon(geojson.bbox);		
	var bbox3857Polygon = reproject.reproject(
		bboxPolygon,'EPSG:4326','EPSG:3857',proj4.defs);
	var mapExtent={ 
		left: bbox3857Polygon.geometry.coordinates[0][0][0], 	// Xmin
		bottom: bbox3857Polygon.geometry.coordinates[0][1][1], 	// Ymin
		right: bbox3857Polygon.geometry.coordinates[0][2][0], 	// Xmax
		top: bbox3857Polygon.geometry.coordinates[0][3][1] 		// Ymax
	};	

	var svgFileName=geolevel_id + "/" + zoomlevel + "/" + x + "/" + y + ".svg";	
	var svgOptions = {
		mapExtent: mapExtent,
		attributes: { id: svgFileName }
	};
	
//
// Reproject intersection to 3857 and convert to SVG
//	
	var intersection3857 = reproject.reproject(
		geojson,'EPSG:4326','EPSG:3857',proj4.defs);
	var svgString = converter.convert(intersection3857, svgOptions);
	svgString='<?xml version="1.0" standalone="no"?>\n' +
		' <!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">\n' +
		'  <svg width="256" height="256" version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">\n' + 
		'   ' + svgString + '\n' +
		'  </svg>';
//	console.error(svgFileName + "; size: " + nodeGeoSpatialServicesCommon.fileSize(sizeof(svgString)));

	return svgString;
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
			console.error("Wrote svg file: " + svgFileName + "; size: " + nodeGeoSpatialServicesCommon.fileSize(sizeof(svgTile)));
			callback();
		});		
	svgStream.on('error', 
		function svgStreamError(e) {
			callback(e);						
		});
		
//
// Write SVG file
//		
	svgStream.write(svgTile);
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
 
	pgTileMaker(endCallback)										
} // End of tileMaker()


var pgTileMaker = function pgTileMaker(client, pgTileMakerCallback) {
	
	scopeChecker(__file, __line, { // Check callback
		callback: pgTileMakerCallback
	});

	/*
	 * Function: 	pgErrorHandler()
	 * Parameters:	Error object, SQL query
	 * Returns:		Nothing
	 * Description:	Creates new Error; raise using pgTileMakerCallback
	 */
	var pgErrorHandler = function pgErrorHandler(err, sqlInError) {
		var nerr;
		if (err) {	
			nerr=new Error(err.message + "\nSQL> " + (sqlInError||sql));
			nerr.stack=err.stack;
		}
		else {
			nerr=new Error("pgErrorHandler() No error object passed")
		}
		try {
//			console.error("pgErrorHandler() Error: " + nerr.message);
			endTransaction(nerr, pgTileMakerCallback, (sqlInError||sql));
		}
		catch (e) {
			console.error("pgErrorHandler() Caught error in end transaction: " + e.message);
			pgTileMakerCallback(nerr);
		}
	} // End of pgErrorHandler()						

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
			if (!d.properties["id"]) {
				throw new Error("FIELD PROCESSING ERROR! Invalid id field: d.properties.id does not exist in geoJSON");
			}
			else {
				return d.properties["id"];
			}							
		};
	} // End of TopojsonOptions() object constructor

	/*
	 * Function: 	Tile()
	 * Parameters:	Row object (from SQL query), geojson, topojson module options object, tile array
	 * Returns:		Object
	 * Description:	Create internal tile object
	 */	
	function Tile(row, geojson, topojson_options, tileArray) { 
	
		this.tileId=row.tile_id;
		this.geojson={type: "FeatureCollection", bbox: undefined, features: [geojson]};
		this.geolevel_id=row.geolevel_id;	
		this.geolevel_name=row.geolevel_name;	
		this.zoomlevel=row.zoomlevel;
		this.x=row.x;
		this.y=row.y;
		
		this.topojson_options=topojson_options;		

		if (tileArray) {
			tileArray.push(this);	
			tileNo++;
			this.id=tileNo;
			this.geojson.features[0].properties.id=this.id;
			
			// Add other keys to properties; i.e, anything else in lookup table
			var usedAready=['tile_id', 'geolevel_id', 'zoomlevel', 'optimised_wkt', 'areaid'];
			for (var key in row) {
				if (usedAready.indexOf(key) ==	-1) { // Not tile_id, geolevel_id, zoomlevel, optimised_wkt, areaid
					this.geojson.features[0].properties[key] = row[key];
				}
			}
		}
		else {
			throw new Error("No tileArray defined");
		}		
	} // End of ~Tile() object constructor
	Tile.prototype = { // Add methods
		/*
		 * Function: 	addFeature()
		 * Parameters:	Row object (from SQL query), geojson
		 * Returns:		Nothing
		 * Description:	Add geoJSON feature to object
		 */			
		addFeature: function(row, geojson) {
			this.geojson.features.push(geojson);
			this.geojson.features[(this.geojson.features.length-1)].properties.id=this.id;
			
			// Add other keys to properties; i.e, anything else in lookup table
			var usedAready=['tile_id', 'geolevel_id', 'zoomlevel', 'optimised_wkt', 'areaid'];
			for (var key in row) {
				if (usedAready.indexOf(key) ==	-1) { // Not tile_id, geolevel_id, zoomlevel, optimised_wkt, areaid
					this.geojson.features[(this.geojson.features.length-1)].properties[key] = row[key];
				}
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
			
			if (this.geojson) {
				var bbox=turf.bbox(this.geojson);
				this.geojson.bbox=bbox;
				var numFeatures=(this.geojson.features.length || 0);	
				if (this.id == 1) {
					this.topojson_options.verbose=true;
					console.error("GEOJSON: " + JSON.stringify(this.geojson, null, 2).substring(0, 1000));
				}
				else {
					this.topojson_options.verbose=false;
				}
				
				// Create SVG tile		
				this.svgTile=createSVGTile(this.geolevel_id, this.zoomlevel, this.x, this.y, this.geojson);
				
				this.topojson=topojson.topology({   // Convert geoJSON to topoJSON
					collection: this.geojson
					}, this.topojson_options);
					
				this.insertArray=[this.geolevel_id, this.zoomlevel, this.x, this.y, JSON.stringify(this.topojson), this.tileId];
				tileSize=sizeof(this.insertArray);
				console.error('Make tile ' + this.id + ': "' + this.tileId + 
					'", ' +  numFeatures + ' feature(s)' + 
					'; size: ' + (nodeGeoSpatialServicesCommon.fileSize(tileSize)||'N/A') +
					'; bounding box: ' + JSON.stringify(bbox));	
				if (this.id == 1) {
					console.error("TOPOJSON: " + JSON.stringify(this.topojson, null, 2).substring(0, 1000));
				}	
			
				this.geojson=undefined; // Free up memory used for geoJSON
				this.topojson=undefined; // Free up memory used for topojson	
			}	
			else {
				throw new Error("addTopoJson(): no geoJSON for tile: " + this.tileId)
			}
//			console.error("addTopoJson(): " + tileSize)
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
	function tileIntersectsRowProcessing(row) {
				
		var tileArray=getTileArray();
		var geojson={
			type: "Feature",
			properties: {
				id: 	  undefined,
				areaID:   row.areaid,
				areaName: row.areaname
			}, 
			geometry: wellknown.parse(row.optimised_wkt)
		};
		var tileSize=0;
//				console.error("wktjson: " + JSON.stringify(wktjson, null, 2).substring(0, 1000));	
			
		if (tileArray.length == 0) { // First row in zoomlevel/geolevel combination
			var geojsonTile=new Tile(row, geojson, topojson_options, tileArray);
//			console.error('Tile ' + geojsonTile.id + ': ' + geojsonTile.tileId + "; properties: " + 
//				JSON.stringify(geojsonTile.geojson.features[0].properties, null, 2));
		}
		else {
			var geojsonTile=tileArray[(tileArray.length-1)]; // Get last tile from array
			if (geojsonTile.tileId == row.tile_id) { 	// Same tile
				geojsonTile.addFeature(row, geojson);	// Add geoJSON feature to collection
//						console.error('Add areaID: ' + row.areaid + "; properties: " + 
//							JSON.stringify(geojsonTile.geojson.features[(geojsonTile.geojson.features.length-1)].properties, null, 2));
			}
			else {										// New tile
				tileSize=geojsonTile.addTopoJson();		// Complete last tile
				geojsonTile=new Tile(row, geojson, topojson_options, tileArray);
														// Create new tile
//				console.error('Tile ' + geojsonTile.id + ': "' + geojsonTile.tileId + "; features[0] properties: " + 
//					JSON.stringify(geojsonTile.geojson.features[0].properties, null, 2));	
		
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
				console.error("endTransaction(): " + sql + "\nSQL in error> " + sqlInError);
				sql=sqlInError;		// Restore SQL in error
			}
			else {
				console.error("endTransaction(): " + sql);
			}
			endTransactionCallback(terr || err);	
		});		
	}
	
	/*
	 * Function: 	startTransaction()
	 * Parameters:	Callback: addUserToPath()
	 * Returns:		Nothing
	 * Description:	Start transaction
	 */	
	function startTransaction(startTransactionCallback) {
		sql='BEGIN TRANSACTION';	
		var query=client.query(sql, function beginTransaction(err, result) {
			if (err) {
				pgErrorHandler(err, sql);
			}
			startTransactionCallback();	
		});		
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
		sql="SELECT reset_val FROM pg_settings WHERE name='search_path'";
		var query=client.query(sql, function getSearchPath(err, result) {
			if (err) {
				pgErrorHandler(err, sql);
			}
			sql='SET SEARCH_PATH TO "$user",' + result.rows[0].reset_val;
		
			var query=client.query(sql, function setSearchPath(err, result) {
				if (err) {
					pgErrorHandler(err, sql);
				}
				addUserToPathCallback();	
			});
		});	
	} // End of addUserToPath()

	/*
	 * Function: 	getNumGeolevelsZoomlevels()
	 * Parameters:	Geography table name, callback: tileIntersectsProcessingGeolevelLoop()
	 * Returns:		Nothing
	 * Description:	Convert geoJSON featureList to topojson, add to object, free up memory used for geoJSON
	 */		
	function getNumGeolevelsZoomlevels(geographyTable, getNumGeolevelsZoomlevelsCallback) {
		sql="SELECT * FROM " + geographyTable;
		var query=client.query(sql, function setSearchPath(err, result) {
			if (err) {
				pgErrorHandler(err, sql);
			}
			if (result.rows.length != 1) {
				pgErrorHandler(new Error("getNumGeolevelsZoomlevels() geography table: " + geographyTable + " fetch rows !=1 (" + result.rows.length + ")"), sql);
			}
			var geographyTableData=result.rows[0];
			
			sql="TRUNCATE TABLE t_tiles_" + geographyTableData.geography;
			console.error(sql);
			var query=client.query(sql, function t_tilesDelete(err, result) {
				if (err) {
					pgErrorHandler(err, sql);
				}
				else {
					sql="SELECT MAX(geolevel_id) AS max_geolevel_id,\n" +
							"       MAX(zoomlevel) AS max_zoomlevel\n" +
							"  FROM " + geographyTableData.geometrytable;
							
					var query=client.query(sql, function setSearchPath(err, result) {
						if (err) {
							pgErrorHandler(err, sql);
						}
						numZoomlevels=result.rows[0].max_zoomlevel;
						numGeolevels=result.rows[0].max_geolevel_id;
						
						getNumGeolevelsZoomlevelsCallback(geographyTableData);	
					});		
				}
			});		
		});			
	} // End of getNumGeolevelsZoomlevels()

	/*
	 * Function: 	tileIntersectsProcessingGeolevelLoop()
	 * Parameters:	Geography table data
	 * Returns:		Nothing
	 * Description:	Asynchronous do while loop to process all geolevels; calls tileIntersectsProcessingZoomlevelLoop() for each geolevel
	 */		
	function tileIntersectsProcessingGeolevelLoop(geographyTableData) {
	
		var tileIntersectsTable='tile_intersects_' + geographyTableData.geography;
		var geolevelsTable='geolevels_' + geographyTableData.geography;
		 
		sql="SELECT * FROM " + geolevelsTable + " WHERE geography = '" +geographyTableData.geography  + "' ORDER BY geolevel_id";
		var query=client.query(sql, function setSearchPath(err, result) {
			if (err) {
				pgErrorHandler(err, sql);
			}		  
			if (result.rows.length < 1) {
				pgErrorHandler(new Error("tileIntersectsProcessingGeolevelLoop() geolevelsTable table: " + geolevelsTable + 
					" fetch rows <1 (" + result.rows.length + ") for geography: " + geographyTableData.geography), sql);	
			}
			var geolvelTableData=result.rows;
			console.error("Geography: " + geographyTableData.geography + "; geolevels: " + result.rows.length);
	
		var i=0;
		async.doWhilst(
			function geolevelProcessing(callback) {		
				tileIntersectsProcessingZoomlevelLoop(tileIntersectsTable, geolvelTableData[i].geolevel_name, geolvelTableData[i].geolevel_id, 
					geographyTableData.geography, callback);
			}, 
			function geolevelTest() { // Mimic for loop
				var res=false;
				
				i++;
				if (i<numGeolevels) { res=true; }; 
				return (res);
			},
			function geolevelProcessingEndCallback(err) { // Call main tileMaker complete callback
				endTransaction(err, pgTileMakerCallback, sql);
			});
			
		});
		 
	} // End of tileIntersectsProcessingGeolevelLoop()

	/*
	 * Function: 	tileIntersectsProcessingZoomlevelLoop()
	 * Parameters:	Tile intersects table name, geolevel name, geolevel id, geography, geolevel processing callback (callback from geolevelProcessing async)
	 * Returns:		Nothing
	 * Description:	Asynchronous do while loop to process all zoomlevels in a geolevel; calls tileIntersectsProcessing() for each zoomlevel
	 */		
	function tileIntersectsProcessingZoomlevelLoop(tileIntersectsTable, geolevelName, geolevel_id, geography, geolevelProcessingCallback) {
		 
// 
// Primary key: geolevel_id, zoomlevel, areaid, x, y
//			
		sql="SELECT z.geolevel_id::VARCHAR||'_'||'" + geolevelName + "'||'_'||z.zoomlevel::VARCHAR||'_'||z.x::VARCHAR||'_'||z.y::VARCHAR AS tile_id,\n" +
			"       z.geolevel_id, z.zoomlevel, z.optimised_wkt, z.areaid, z.x, z.y, a.*\n" +				
			"  FROM " + tileIntersectsTable + " z, lookup_" + geolevelName + " a\n" +
			" WHERE z.geolevel_id = $2\n" + 
			"   AND z.zoomlevel   = $1\n" + 
			"   AND z.areaid      = a." + geolevelName + "\n" + 
			" ORDER BY 1";
	
		var zoomlevel=0;
		zstart = new Date().getTime(); // Set timer for zoomlevel
		async.doWhilst(
			function zoomlevelProcessing(callback) {		
				tileIntersectsProcessing(sql, zoomlevel, geolevel_id, geography, callback);
			}, 
			function zoomlevelTest() { // Mimic for loop
				var res=false;
				
				zstart = new Date().getTime(); // Set timer for zoomlevel
				
				zoomlevel++;
				/*
				 * 5: 180 tiles in 42.579 S
				 * 6: 410 tiles in 70.932 S
				 * 7: 1024 tiles in 143.471 S
				 * 8: 2681 tiles in 363.273 S
				 */
				var maxZoomlevel=numZoomlevels;
				// For testing
				maxZoomlevel=5;
				if (zoomlevel<= maxZoomlevel) { res=true; }; 
				return (res);
			},
			function zoomlevelProcessingEndCallback(err) {
				geolevelProcessingCallback(err);
			});
	} // End of tileIntersectsProcessingZoomlevelLoop()

	/*
	 * Function: 	tileIntersectsProcessing()
	 * Parameters:	SQL statement, zoomlevel, geolevel id, geography, tile intersects processing callback (callback from zoomlevelProcessing async)
	 * Returns:		Nothing
	 * Description:	Asynchronous SQL fetch for all tile intersects in a geolevel/zoomlevel. Calls tileIntersectsRowProcessingI() for each row. 
	 *				Process last tile if required
	 */			
	function tileIntersectsProcessing(sql, zoomlevel, geolevel_id, geography, tileIntersectsProcessingCallback) {

		/*
		 * Function: 	tileInsert()
		 * Parameters:	Exepcted rows
		 * Returns:		Nothing
		 * Description:	Multi row insert in t_tile_<geography> table
		 */	
		function tileInsert(expectedRows) {
			sql="INSERT INTO t_tiles_" + geography +
				 '	(geolevel_id, zoomlevel, x, y, optimised_topojson, tile_id)\nVALUES ';
			var j=1;
			var insertArray=[];
			for (var i=0; i<tileArray.length; i++) {
				if (i>0) {
					sql+=",\n";
				}
				sql+='($' + j + ',$' + (j+1) + ',$' + (j+2) + ',$' + (j+3) + ',$' + (j+4) + ',$' + (j+5) + ') /* Row ' + i + ' */';
				j+=6;
				insertArray=insertArray.concat(tileArray[i].insertArray)
			}
//			console.error("INSERT SQL> " + sql + "\nValues (" + insertArray.length + "): " + JSON.stringify(insertArray).substring(0, 1000));
	/*
	SELECT geolevel_id, zoomlevel, x, y, tile_id, LENGTH(optimised_topojson::Text) AS len
	FROM t_tiles_cb_2014_us_500k
	WHERE LENGTH(optimised_topojson::Text) < 1000
	ORDER BY geolevel_id, zoomlevel, x, y;

	*/
			var query=client.query(sql, insertArray, function tilesInsert(err, result) {
				if (err) {
					pgErrorHandler(err, sql);
				}
				else if (result == undefined) {
					pgErrorHandler(new Error("tileIntersectsProcessing() tile INSERT: result undefined != expected: " + expectedRows), sql);
					
				}
				else if (expectedRows != result.rowCount) {
					pgErrorHandler(new Error("tileIntersectsProcessing() tile INSERT: " + result.rowCount + " != expected: " + expectedRows), sql);
				}
				else {
					tileArray=[];  // Re-initialize tile array
					svgArray={};
					tileIntersectsProcessingCallback(); // callback from zoomlevelProcessing async
				}	
			});	
		} // End of tileInsert()
			
		var query = client.query(sql, [zoomlevel, geolevel_id]);
		query.on('error', pgErrorHandler);

		query.on('row', function tileIntersectsRow(row) {
			totalTileSize+=tileIntersectsRowProcessing(row, geography);			
		});
		
		query.on('end', function(result) {
			// Process last tile if it exists
			var geojsonTile=tileArray[(tileArray.length-1)];
			if (geojsonTile) {
				totalTileSize+=geojsonTile.addTopoJson();				// Complete final tile	
			}
			
			var end = new Date().getTime();
			var elapsedTime=(end - zstart)/1000; // in S
			var tElapsedTime=(end - lstart)/1000; // in S
			console.error('Geolevel: ' + geolevel_id + '; zooomlevel: ' + zoomlevel + '; ' + 
				result.rowCount + ' tile intersects processed; ' + tileArray.length + " tiles in " + elapsedTime + " S; " +  
				Math.round((tileArray.length/elapsedTime)*100)/100 + " tiles/S" + 
				"; total: " + tileNo + " tiles in " + tElapsedTime + " S; size: " + nodeGeoSpatialServicesCommon.fileSize(totalTileSize));
			var expectedRows=tileArray.length;
			
			if (tileArray.length > 0) {	
				async.forEachOfSeries(tileArray, 
				function writeSVGTileSeries(value, j, writeSVGTileCallback) { // Processing code	
					// Write SVG tiles to disk

						writeSVGTile('/Users/Peter/Google Drive/work/tiles', 
							value.geolevel_id, value.zoomlevel, value.x, value.y, writeSVGTileCallback, value.svgTile);
							var svgTileFileName=getSVGTileFileName('/Users/Peter/Google Drive/work/tiles', 
							value.geolevel_id, value.zoomlevel, value.x, value.y)
						svgArray[svgTileFileName + '.svg']=svgTileFileName + '.png';
	//  C:\Users\Peter\Documents\GitHub\rapidInquiryFacility\rifNodeServices\node_modules\phantomjs-prebuilt\lib\phantom\bin\phantomjs.exe
					},
					function writeSVGTileEnd(err) { //  Callback
		
						if (err) {
							pgErrorHandler(err);
						}
						else {
//							console.error(JSON.stringify(svgArray, null, 2));
							var sizes = {
								height: 256,
								width: 256
							};
							var parallelPages = 10;
							svg2png.svg2PngFiles(svgArray, sizes, parallelPages).then(results => {
								process.env.VERBOSE='true'; // Enable log messages
								if (Array.isArray(results)) {
									console.error(results.length + ' SVG files have been converted successfully');
								} else {
									console.error('SVG convert completed with result ' + results);
								}
								
								tileInsert(expectedRows);
								
							}, errors => {
								if (!Array.isArray(errors)) {
									errors = [errors];
								}
								console.error('Completed with ' + errors.length + ' errors');
								errors.forEach(error => console.error(error.stack || error));
							});					
						}
					}
				); // End of async.forEachOfSeries(tileArray, ...)	
											
			}
			else {
				tileIntersectsProcessingCallback(); // callback from zoomlevelProcessing async
			}
		});	
	} // End of tileIntersectsProcessing()
			
	var tileArray = [];	
	var svgArray = [];
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
	var geographyTable="geography_cb_2014_us_500k";
	startTransaction(function startTransactionCallback2(err) {
		addUserToPath(function addUserToPathCallback2(err) {
			getNumGeolevelsZoomlevels(geographyTable, tileIntersectsProcessingGeolevelLoop);
		});
	});
}

module.exports.pgTileMaker = pgTileMaker;

//
// Eof