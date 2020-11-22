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
// Rapid Enquiry Facility (RIF) - RIF Node web services; implemented using Express 
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
//
// Uses:
//
// Prototype author: Federico Fabbri
// Imperial College London
//
const express = require('express'),
    busboy = require('connect-busboy'),
	helmet = require('helmet'),
    morgan = require('morgan'),
    nodeGeoSpatialServices = require('./lib/nodeGeoSpatialServices'),
    simplify = require('./routes/simplify'),
    zipfile = require('./routes/zipfile');

console.error("STarting express server");

var app = express(); 	// default options, no immediate parsing 

// development error handler
// will print stacktrace
/*
if (app.get('env') === 'development') {
  app.use(function(err, req, res, next) {
    res.status(err.status || 500);
	console.log('expressServer.js: error: ' + err.message + "\n" + err.stack);
    res.render('expressServer.js: error', {
        message: err.message,
        error: err
    });
  });

}
 */
 
// production error handler
// no stacktraces leaked to user
/*
app.use(function(err, req, res, next) {
    res.status(err.status || 500);
	console.log('expressServer.js: error: ' + err.message);
    res.render('expressServer.js: error', {
        message: err.message,
        error: {}
    });
});  
 */

app.use(morgan('combined')); // Logging
 
app.use(
	helmet()); // Helmet helps you secure your Express apps by setting various HTTP headers. It's not a silver bullet, but it can help!

app.use( 				// For parsing incoming HTML form data.
	busboy({
		highWaterMark: (4000 * 1024 * 1024),	// Buffer memory
		limits: {
			fileSize: (2000 * 1024 * 1024)-1, 	// 2G-1 file size limit
			files: 100							// Total file attachments limit	
		},
		defCharset: 'binary'
	}));

// Add headers
app.use(function (req, res, next) {

    // Website you wish to allow to connect - cross-origin HTTP request (CORS) rules
//    res.setHeader('Access-Control-Allow-Origin', 'http://localhost:3000');
//    res.setHeader('Access-Control-Allow-Origin', 'http://127.0.0.1:3000');
    res.setHeader('Access-Control-Allow-Origin', '*'); // Allow all access

    // Request methods you wish to allow
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST');

    // Request headers you wish to allow
    res.setHeader('Access-Control-Allow-Headers', 'X-Requested-With,content-type');

    // Set to true if you need the website to include cookies in the requests sent
    // to the API (e.g. in case you use sessions)
    res.setHeader('Access-Control-Allow-Credentials', true);

    // Pass to next layer of middleware
    next();
});

// Serving static files
app.use(express.static('dataLoader'));

/*
 * Services supported:
 * 
 * shpConvert: Upload then convert shapefile to geoJSON;
 * geo2TopoJSON: Convert geoJSON to TopoJSON;
 * getShpConvertStatus: Get shapefile conversion status;
 * getShpConvertTopoJSON: Get shapefile converts into topoJSON optimised for zoomnlevels 6-11
 * shpConvertGetConfig.xml: Get shapefile conversion XML configuration
 * shpConvertGetResults.zip: Get shapefile conversion results zip file
 *
 * Not yet imnplement:
 *
 * simplifyGeoJSON: Load, validate, aggregate, clean and simplify converted shapefile data;
 * geoJSONtoWKT: Convert geoJSON to Well Known Text (WKT);
 * createHierarchy: Create hierarchical geospatial intersection of all the shapefiles;
 * createCentroids: Create centroids for all shapefiles;
 * createMaptiles: Create topoJSON maptiles for all geolevels and zoomlevels; 
 * getGeospatialData: Fetches GeoSpatial Data;
 * getNumShapefilesInSet: Returns the number of shapefiles in the set. This is the same as the highest resolution geolevel id;
 * getMapTile: Get maptile for specified geolevel, zoomlevel, X and Y tile number.
 */		
var services=["/:shpConvert",
			  "/:geo2TopoJSON",
			  "/:getShpConvertStatus" ,
			  "/:getShpConvertTopoJSON" ,
			  "/:shpConvertGetConfig.xml" ,
			  "/:shpConvertGetResults.zip",
			  "/:getGeographies",
			  "/:getMapTile" /*,
			  "/simplifyGeoJSON",
			  "/geoJSON2WKT",
			  "/createHierarchy",
			  "/createCentroids",
			  "/createMaptiles",
			  "/getGeospatialData",
			  "/getNumShapefilesInSet"*/];
var theDate = new Date();
for (var i=0; i<services.length; i++) { // Call common method
// Get methods are dummies for test purposes
	app.get(services[i], nodeGeoSpatialServices.convert);
	console.error('expressServer.js: register service: ' + services[i]);
	app.post(services[i], nodeGeoSpatialServices.convert);
}
// Old Fred zip shapefile code
//app.get('/simplify', simplify.convert);
//app.post('/simplify', simplify.convert);

// Zipfile test method. Assumes compressed JSON file
//app.get('/zipfile', zipfile.convert);
//app.post('/zipfile', zipfile.convert);
 
//app.use(express.static(__dirname + '/public'));
  
var server=app.listen(3000);
server.timeout=10*60*1000; // 10 minutes
console.error(theDate.toString() + '\nexpressServer.js: RIF Node web services listening on 127.0.0.1 port 3000, timeout 10 minutes...');

const v8 = require('v8');
if (global.gc) {
	console.error("Garbage collection exposed (see forever.log); will attempt to prod gc into acction when releasing >500M memory");
	v8.setFlagsFromString('--trace_gc');
	v8.setFlagsFromString('--trace_gc_verbose');
	v8.setFlagsFromString('--trace_gc_ignore_scavenger');
//	v8.setFlagsFromString('--trace_external_memory');
}

var usage=process.memoryUsage();
console.error('Memory usage >>>');
for (var key in usage) {
	console.error(key + ": " + usage[key]);
}
console.error('<<< End of memory usage.');

var heap=v8.getHeapStatistics();
console.error('Memory heap >>>');
for (var key in heap) {
	console.error(key + ": " + heap[key]);
}
console.error('<<< End of memory heap.');

if (v8.getHeapSpaceStatistics) { // Currently not exposed, altough in manual; --trace_gc turned on to compensate; appears in forever.log
	var heapSpace=v8.getHeapSpaceStatistics();
	console.error('Memory heap space >>>');
	for (var i=0; i<heapSpace.length; i++) {
		var heap=heapSpace[i];
		var heapName;
		for (var key in heap) {
			if (key == "space_name") {
				heapName = heap[key];
			}
			else {
				console.error("[" + heapName + "]: " + key + ": " + heap[key]);
			}
		}
	}
	console.error('<<< End of memory heap space.');
}
else {	
	console.error('No memory heap space statistics');
}