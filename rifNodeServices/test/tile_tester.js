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
	  tileMaker = require('../lib/tileMaker'),
	  reproject = require("reproject"),
	  proj4 = require("proj4");

//const svg2png = require("svg2png");

var input_geojson={
	bbox: [],		
	geolevel: [],
	zoomlevel: 0
};
input_geojson.bbox[0]=tileMaker.tile2longitude(0, input_geojson.zoomlevel);		// xmin
input_geojson.bbox[1]=tileMaker.tile2latitude(0, input_geojson.zoomlevel);		// ymin
input_geojson.bbox[2]=-(tileMaker.tile2longitude(0, input_geojson.zoomlevel));	// xmax
input_geojson.bbox[3]=-(tileMaker.tile2latitude(0, input_geojson.zoomlevel));	// ymax

//console.error("bbox: " + JSON.stringify(input_geojson.bbox, null, 4));

for (var i=1; i<=3; i++) {
	input_geojson.geolevel[i] = {};
}
input_geojson.geolevel[1].geojson = JSON.parse(fs.readFileSync("tile_tester\\cb_2014_us_nation_5m.json"));
input_geojson.geolevel[2].geojson = JSON.parse(fs.readFileSync("tile_tester\\cb_2014_us_state_500k.json"));
input_geojson.geolevel[3].geojson = JSON.parse(fs.readFileSync("tile_tester\\cb_2014_us_county_500k.json"));

var bboxPolygon = turf.bboxPolygon(input_geojson.bbox);
//console.error("bboxPolygon: " + JSON.stringify(bboxPolygon, null, 4));
var intersectlist = [];
for (var i = 0; i < input_geojson.geolevel[1].geojson.features.length; i++) {
	var kinks = turf.kinks(input_geojson.geolevel[1].geojson.features[i]);

	if (kinks && kinks.intersections && kinks.intersections.features) {
		var resultFeatures = kinks.intersections.features.concat(input_geojson.geolevel[1].geojson.features[i]);
		var result = {
		  "type": "FeatureCollection",
		  "features": resultFeatures
		};
		console.error("kinks: " + i + "; geojson: " + JSON.stringify(result, null, 4).substring(0, 400));
	}
	
	var intersectedFeature = turf.intersect(input_geojson.geolevel[1].geojson.features[i], bboxPolygon);
	if (intersectedFeature != null) {
		
		console.error("Intersection: " + i + "; geojson: " + JSON.stringify(input_geojson.geolevel[1].geojson.features[i], null, 4).substring(0, 400));
		intersectlist.push(input_geojson.geolevel[1].geojson.features[i]);
	}
}
if (intersectlist.length > 0) {
	intersectlist.push(bboxPolygon); // Add boundary to tile for test purtposes
	var intersection={
		type: "FeatureCollection",
		features: intersectlist,
		bbox: input_geojson.bbox
	}
//		var result = turf.clip(bboxPolygon /* clipping geojson */, intersection);
		
//		fs.writeFile("test.json", JSON.stringify(intersection));
//		tileJSON=turf.bboxClip(intersection, bbox);

		var tileCallback = function tileCallback(e) {
			if (e) {
				throw e;
			}
		}
		tileMaker.writeSVGTile('tile_tester', 1 /* geolevel*/ , input_geojson.zoomlevel, 0 /* X */, 0 /* Y */, tileCallback, intersection);
}		
