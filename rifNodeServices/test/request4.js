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
// Rapid Enquiry Facility (RIF) - Node.js webservice request test
//								  Test4: Not using request!
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
// Usage: node test/request4.js
//
// Test4: Not using request!

var FormData = require('form-data');
var fs = require('fs');
var zlib = require('zlib');
var inspect = require('util').inspect;

var form = new FormData();

form.append('verbose', 'true');

// Create lz77 ZLIB inflate() file
var zstream=zlib.createDeflate();
var rstream=fs.createReadStream('./data/test_6_sahsu_4_level4_0_0_0.js');
var wstream=fs.createWriteStream('./data/test_6_sahsu_4_level4_0_0_0.js.lz77');

rstream.pipe(zstream).pipe(wstream);
wstream.on('finish', function() {
	json_file2 = fs.createReadStream('./data/test_6_sahsu_4_level4_0_0_0.js.lz77');
	var data = new Buffer('');
	var chunks = [];
	var chunk;

	json_file2.on('readable', function() {
		while ((chunk=json_file2.read()) != null) {
			chunks.push(chunk);
		}
	});
	json_file2.on('end', function() {
		data=Buffer.concat(chunks)
		console.log("Lz77 (zlib) binary stream(" + data.length + "): " + data.toString('hex').substring(0, 132))

		console.error("Stream 1: test_6_sahsu_4_level4_0_0_0.js.gz")
		form.append('my_file 1', fs.createReadStream('./data/test_6_sahsu_4_level4_0_0_0.js.gz'), {
			filename: 'test_6_sahsu_4_level4_0_0_0.js.gz',
			ContentType: 'application/gzip', 
			ContentTransferEncoding: 'gzip', 
			TransferEncoding: 'gzip', 
			ContentEncoding: 'gzip', 
			AcceptEncoding: "gzip,zip,zlib"});
			
		console.error("Stream 2: test_6_sahsu_4_level4_0_0_0.js.gz")
		form.append('my_file 2', fs.createReadStream('./data/test_6_sahsu_4_level4_0_0_0.js.gz'), {
			filename: 'test_6a_sahsu_4_level4_0_0_0.js.gz'});	

		console.error("Stream 3: test_6_sahsu_4_level4_0_0_0.js.lz77")	
		form.append('my_file 3', fs.createReadStream('./data/test_6_sahsu_4_level4_0_0_0.js.lz77'), {
			filename: 'test_6a_sahsu_4_level4_0_0_0.js.lz77',
			ContentType: 'application/zlib', 
			ContentTransferEncoding: 'zlib', 
			TransferEncoding: 'zlib', 
			ContentEncoding: 'zlib', 
			AcceptEncoding: "gzip,zip,zlib",
			knownLength: data.length}); 
			
		console.error("SUBMIT");	
		form.submit('http://127.0.0.1:3000/zipfile',
			function (error, response) {
				if (error) {
				  return console.error('upload failed:', error);
				}
				console.log('Upload successful!  Server responded with:', JSON.stringify(response.headers, null, 4));
				response.on('data', (chunk) => {
					console.log(`BODY: ${chunk}`);
				});
				response.on('end', () => {
					console.log('No more data in response.')
				})
				response.resume();
		  });
	});		  
});