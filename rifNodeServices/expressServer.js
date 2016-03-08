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
    nodeGeoSpatialServices = require('./lib/nodeGeoSpatialServices'),
    simplify = require('./routes/simplify'),
    zipfile = require('./routes/zipfile');

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

 /*
app.use( 				// For parsing incoming HTML form data.
	busboy());
 */	
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

/*
 * Services supported:
 * 
 * shp2GeoJSON: Upload then convert shapefile to geoJSON;
 * simplifyGeoJSON: Load, validate, aggregate, clean and simplify converted shapefile data;
 * geo2topoJSON: Convert geoJSON to TopoJSON;
 * geoJSON2WKT: Convert geoJSON to Well Known Text (WKT);
 * createHierarchy: Create hierarchical geospatial intersection of all the shapefiles;
 * createCentroids: Create centroids for all shapefiles;
 * createMaptiles: Create topoJSON maptiles for all geolevels and zoomlevels; 
 * getGeospatialData: Fetches GeoSpatial Data;
 * getNumShapefilesInSet: Returns the number of shapefiles in the set. This is the same as the highest resolution geolevel id;
 * getMapTile: Get maptile for specified geolevel, zoomlevel, X and Y tile number.
 */		
var services=["/shp2GeoJSON",
			  "/simplifyGeoJSON",
			  "/geo2TopoJSON",
			  "/geoJSON2WKT",
			  "/createHierarchy",
			  "/createCentroids",
			  "/createMaptiles",
			  "/getGeospatialData",
			  "/getNumShapefilesInSet",
			  "/getMapTile"];
var theDate = new Date();
for (var i=0; i<services.length; i++) { // Call common method
// Get methods are dummies for test purposes
	app.get(services[i], nodeGeoSpatialServices.convert);
	console.error('expressServer.js: register service: ' + services[i]);
	app.post(services[i], nodeGeoSpatialServices.convert);
}
// Old Fred zip shaepfile code
app.get('/simplify', simplify.convert);
app.post('/simplify', simplify.convert);

// Zipfile test method. Assumes compressed JSON file
app.get('/zipfile', zipfile.convert);
app.post('/zipfile', zipfile.convert);
 
//app.use(express.static(__dirname + '/public'));
  
app.listen(3000);

console.error(theDate.toString() + '\nexpressServer.js: RIF Node web services listening on 127.0.0.1 port 3000...');