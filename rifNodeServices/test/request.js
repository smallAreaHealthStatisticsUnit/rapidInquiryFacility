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
// Rapid Enquiry Facility (RIF) - Node.js webservice request tests
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
// Usage: node test/request.js
//
// Tests: 
//
var FormData = require('form-data');
var fs = require('fs');

var nRequests = 0;

var MakeRequest = function(){

    this.request = require('request'); 
	
	var inputFile = './data/test_6_sahsu_4_level4_0_0_0.js';
	var contentType = 'application/json';
	var json_file;
	var json_file2;
	var json_file3;
	
// Gzipped file tests
	if (nRequests == 5) { 
		inputFile = './data/test_6_sahsu_4_level4_0_0_0.js.gz';
//		contentType = 'application/gzip';
		json_file = fs.createReadStream(inputFile);
		json_file2 = fs.createReadStream(inputFile);
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
			console.log('Gzipped binary stream: ' + data.toString('hex').substring(0, 132))
		});	

//		require('request-debug')(this.request);	
	}
	else if (nRequests == 6) { // wrong Content-Type, binary stream
		inputFile = './data/test_6_sahsu_4_level4_0_0_0.js.gz';
		json_file = fs.createReadStream(inputFile);
	
	}	
	else if (nRequests == 7) { 
		inputFile = './data/test_6_sahsu_4_level4_0_0_0.js.gz';
		var inputFile2 = './data/test_6a_sahsu_4_level4_0_0_0.js';
		var inputFile3 = './data/test_6_sahsu_4_level4_0_0_0.js.lz77';
		json_file = fs.createReadStream(inputFile);
		json_file2 = fs.createReadStream(inputFile2);
		json_file3 = fs.createReadStream(inputFile3);
	}
	else {
		json_file = fs.createReadStream(inputFile);
	}
	
    var length = fs.statSync(inputFile).size;
	var id=nRequests;
		
	if (nRequests == 7) { 
			var formData = {
			my_test: "Defaults",
			my_reference: nRequests,
			attachments: [
				json_file,
				json_file2,
				json_file3
			]
		};
	}
	else {
		var formData = {
			my_test: "Defaults",
			my_reference: nRequests,
			attachments: [
				json_file
			]
		};
	}

	
// Test cases
	if (nRequests == 2) {
		formData["verbose"]="true";
		formData["my_test"]="Verbose";		
	}
	else if (nRequests == 3) {
		formData["verbose"]="true";
		formData["zoomLevel"]=0;		
		formData["my_test"]="zoomLevel: 0";		
	}
	else if (nRequests == 4) {
		formData["verbose"]="true";
		formData["zoomLevel"]=0;
		formData["projection"]=27700;		
		formData["my_test"]="projection: 27700";		
	}	
	else if (nRequests == 5) {
		formData["verbose"]="true";
		formData["zoomLevel"]=0;	
		formData["my_test"]="gzip geoJSON file";	
//		this.request.debug = true;		
	}	
	else if (nRequests == 6) {
		formData["verbose"]="true";
		formData["zoomLevel"]=0;	
		formData["my_test"]="gzip geoJSON file; wrong Content-Type";		
	}
	else if (nRequests == 7) {
		formData["verbose"]="true";
		formData["zoomLevel"]=0;	
		formData["my_test"]="gzip geoJSON multiple files";		
	}
		
	console.log("Sending " + inputFile + " request:" + nRequests + "; length: " + length); 
//		'; ' + JSON.stringify(formData, null, 4));
		
    this.options = {
        url:  'http://127.0.0.1:3000/toTopojson',
        headers:{'Content-Type': contentType},
        formData: formData, 
        'content-length': length
    }; 
	if (nRequests == 5) { // Gzipped file test	
		this.debug = true;
		this.options.headers={'Content-Type': contentType, 'Content-Encoding': 'gzip', "accept-encoding" : "gzip"};
//		this.options.headers:{'Content-Type': contentType};
		this.options.gzip = true;
//		json_file.setDefaultEncoding('binary');
		console.error("GZIP: " + JSON.stringify(formData, null, 4));
	}
	
}

var postIt = function(){
	var r = new MakeRequest(); 
	var p=r.request.post(r.options, function optionalCallback(err, httpResponse, body) {
		if (err) {
			return console.error('Upload #' + nRequests + ' failed: ' + JSON.stringify(httpResponse, null, 4) + 
				"\r\nError: ", err);
		}
		else if (httpResponse.statusCode != 200) {
			return console.error('Upload failed HTTP status: ' + httpResponse.statusCode + 
				"\r\nError: [", body + "]\r\n");
		   
		}
		else {
			var jsonData = JSON.parse(body);
			var topojson;
			var ofields=jsonData.fields;		
			var file_list=jsonData.file_list;
			console.error('Upload #' + ofields["my_reference"] + '\n'+ jsonData.message + 
				'\nfiles processed: ' + jsonData.no_files +
				'; fields: ' + JSON.stringify(ofields, null, 4));
			for (i = 0; i < jsonData.no_files; i++) {	
				 topojson = JSON.stringify(file_list[i].topojson);
				 console.error("File [" + (i+1) + ":" + file_list[i].file_name + "] topoJSON length: " + topojson.length);
			}
		}
	});
};
  

var timeOut = function(){
  setTimeout(function(){
    if(nRequests++ < 7){ 
      postIt();
      timeOut();    
    }
  },100);
};

timeOut();