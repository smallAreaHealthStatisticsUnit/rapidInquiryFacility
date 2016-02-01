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
    toTopojson = require('./routes/toTopojson'),
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

app.use( 				// For parsing incoming HTML form data.
	busboy({
		highWaterMark: 2000 * 1024 * 1024,
		limits: {
			fileSize: 1000 * 1024 * 1024
		},
		defCharset: 'binary'
	}));

// Get methods are dummies for test purposes
app.get('/toTopojson', toTopojson.convert);
app.post('/toTopojson', toTopojson.convert);
app.get('/simplify', simplify.convert);
app.post('/simplify', simplify.convert);

// Zipfile test method. Assumes compressed JSON file
app.get('/zipfile', zipfile.convert);
app.post('/zipfile', zipfile.convert);
 
//app.use(express.static(__dirname + '/public'));
  
app.listen(3000);

console.error('expressServer.js: RIF Node web services listening on 127.0.0.1 port 3000...');