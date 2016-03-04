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
// Rapid Enquiry Facility (RIF) - Node.js webservice request tests - create bighelloworld.js for geo2TopoJSON test 20
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
// Usage: node test/make_bighelloworld.js
//
// create bighelloworld.js for geo2TopoJSON test 20
//
var fs = require('fs');

var inputFile = './data/bighelloworld.js';
//fs.unlinkSync(inputFile);

if (!fs.existsSync(inputFile)) {
	var bighelloworld = fs.createWriteStream(inputFile);
	var numc=0;

	bighelloworld.on('finish', function() {
        console.error('Created: ' + inputFile + ": " + fs.statSync(inputFile).size + " chars");
		process.exit(0);
	});
	bighelloworld.on('error', function (err) {
		console.log('Error creating file: ' + inputFile + ": " + err);
		process.exit(1);
	});
	
	var msg='{ "name":"value", array:[1,2,3], "hello", "world"}\n';
	for (j=1; j<4000; j++) {
		msg=msg+'{ "name":"value", array:[1,2,3], "hello", "world"}\n';
	}
//	console.error('msg length: ' + msg.length);
	
	var i=1;
	for (; ; i++) {	
		numc+=msg.length;
		bighelloworld.write(msg);
		
		if (numc> (2*1024*1024*1024)) { // Stop at 2G
			break;
		}

	}
	bighelloworld.end();
}
else {	
	console.error('File already exists: ' + inputFile + ": " + fs.statSync(inputFile).size + " chars");
	process.exit(0);
}  	
