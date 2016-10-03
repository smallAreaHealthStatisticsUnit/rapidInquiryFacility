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
// Rapid Enquiry Facility (RIF) - Tile Maker test program
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

const turf = require("turf"),
	  fs = require("fs"),
	  async = require("async"),
	  tileMaker = require('../lib/tileMaker'),
	  reproject = require("reproject"),
	  proj4 = require("proj4");

//const svg2png = require("svg2png");

var geolevelGeojson = [];
geolevelGeojson[1] = JSON.parse(fs.readFileSync("tile_tester\\cb_2014_us_nation_5m.json"));
geolevelGeojson[2] = JSON.parse(fs.readFileSync("tile_tester\\cb_2014_us_state_500k.json"));
geolevelGeojson[3] = JSON.parse(fs.readFileSync("tile_tester\\cb_2014_us_county_500k.json"));

var inputGeoJSON={};
for (var i=0; i<=11; i++) {
	inputGeoJSON[i] = {
		zoomlevel: i,
		geojson: {}
	}
	for (var j=1; j<=3; j++) {
		inputGeoJSON[i].geojson[j] = {
			geolevel: j,
			zoomlevel: i,
			geojson: geolevelGeojson[j]
		};
	}
}

var intersectTileGeolevel = function intersectTileGeolevel(zoomlevel, X, Y, inputGeoJSON) {
	var intersection=tileMaker.intersectTile(1 /* geolevel */, zoomlevel, X, Y, inputGeoJSON);
	if (intersection) {
		var tileCallback = function tileCallback(e) {
			if (e) {
				throw e;
			}	
			async.forEachOfSeries(inputGeoJSON[zoomlevel].geojson, 
				function (value, key, callback) {	
					console.error("key: " + key);						
					if (key == "1") { // Already done
//					if (key != "2") { // Only do state; county breaks JSTS with intersectTileGeolevel() error: side location conflict [ (-86.149806, 34.533633, undefined) ]
						callback();
					}
					else {
						var intersection;
						try {
							intersection=tileMaker.intersectTile(key /* geolevel */, zoomlevel, X, Y, inputGeoJSON);
							if (intersection) {
								tileMaker.writeSVGTile('tile_tester', key /* geolevel */, zoomlevel, X, Y, callback, intersection);
							}
							else {
								callback(new Error("No intersection when expected for geolevel: " + 
									key + "; zoomlevel: " + zoomlevel + "; X: " + X + "; Y: " + Y));
							}
						}
						catch (e) {
							callback(e);
						}
					}
				},
				function (err) {
					if (err) {
						console.error("intersectTileGeolevel() error: " + err.message + "\nStack: " + (err.stack || "no stack"));
					}
					console.error("intersectTileGeolevel() done")
				});			
		} // End of tileCallback()
		
//		console.error("inputGeoJSON[" + zoomlevel + "]: " + JSON.stringify(inputGeoJSON[zoomlevel].geojson, null, 4).substring(0, 400));
		
		// path, geolevel, zoomlevel, X, Y, callback, intersection
		tileMaker.writeSVGTile('tile_tester', 1 /* geolevel */, zoomlevel, X, Y, tileCallback, intersection);
	}		
} // End of intersectTileGeolevel

intersectTileGeolevel(0 /* zoomlevel */, 0 /* X */, 0 /* Y */, inputGeoJSON);
