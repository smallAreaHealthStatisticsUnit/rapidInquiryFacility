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
// Rapid Enquiry Facility (RIF) - Node.js webservice request tests - shpConvert service
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
// Usage: node test/request-shpConvert.js
//
// Tests shpConvert service
//
// 1: Level 1 shapefile;
// 2: Level 1, 2 shapefiles;
// 3: Level 1, 2, 3 shapefiles;
// 4: Level 1, 2, 3, 4 shapefiles;
// 5: Level 1 shapefile; missing prj file [intentional failure];
// 6: Level 1 shapefile; missing dbf file [intentional failure];			
// 7: Level 1 shapefile; missing sbn file;
// 8: Level 1 shapefile; missing sbx file;
// 9: Level 1 shapefile;  missing sbn, sbx files;
// 10: Level 1 shapefile; missing .shp.xml file;
//
var FormData = require('form-data');
var fs = require('fs');
var path = require('path');

// Process Args
var nRequests = process.argv[2];
var max_nRequests = 10;
if (!nRequests) {
	nRequests = 0;
	console.log('Processing all shpConvert tests');
}
else if (nRequests > max_nRequests) {
	throw new Error("Invalid request number: " + nRequests + "; must be between 1 and " + max_nRequests);
}
else {
	console.log('Using arg[2] nRequests: ' + nRequests);
	max_nRequests = 1;
}

var passed=0;
var failed=0;
var tests=0;

var resultArray=[];
var resultArrayDesc=[];

var MakeRequest = function(){

    this.request = require('request'); 
	
	var contentType = 'application/json';
	var id=nRequests;
	var idx=nRequests;
	
	var formData = {
		my_test: nRequests + ": Defaults",
		my_reference: nRequests,
		attachments: [				
		],
		expected_to_pass: "true", 
		verbose: "true"
	};
	
	
// test 5 missing prj file - FAILS
// test 6 missing dbf file - FAILS
	
	if (nRequests > 4) { 
		idx=1; // Do case 1
		if ((nRequests == 5)||(nRequests == 6)) {
			formData.expected_to_pass="false";
		}
	}
	else {
		formData["my_test"]+=" (" + idx + " shapefiles)";
	}
	var msg;


// Multi-file test	


	var noFiles=0;
	for (var i = 1; i <= idx; i++) {	
		var inputShapeFile = './data/sahsuland/SAHSU_GRD_Level' + i + '.shp';
		var dirname = path.dirname(inputShapeFile);
		var file_noext = path.basename(inputShapeFile, '.shp');
		if (msg) {
			msg+=",\n" + inputShapeFile;
		}
		else {
			msg=inputShapeFile;
		}
	
		formData.attachments.push(fs.createReadStream(inputShapeFile));
		noFiles++;
		
		var extList = [".dbf", ".prj", ".shp.xml", ".sbn", ".sbx", ".fbn", ".fbx", ".ain", ".aih", ".ixs", ".mxs", ".atx", ".cpg", ".qix"];
		for (var j=0; j<extList.length; j++) {
			var extFile=dirname + "/" + file_noext + extList[j];
// test 5 missing prj file - FAILS
// test 6 missing dbf file - FAILS			
// test 7 missing sbn file
// test 8 missing sbx file
// test 9 missing sbn, sbx files
// test 10 missing .shp.xml file
			if ((nRequests == 5)&&(extList[j] == ".prj")) {
				formData.my_test=nRequests + ": missing prj file";				
			}
			else if ((nRequests == 6)&&(extList[j] == ".dbf")) {
				formData.my_test=nRequests + ": missing dbf file";				
			}	
			else if ((nRequests == 7)&&(extList[j] == ".sbn")) {
				formData.my_test=nRequests + ": missing sbn file";				
			}		
			else if ((nRequests == 8)&&(extList[j] == ".sbx")) {
				formData.my_test=nRequests + ": missing sbx file";				
			}	
			else if ((nRequests == 9)&&( (extList[j] == ".sbn")||(extList[j] == ".sbx") )) {
				formData.my_test=nRequests + ": missing sbn, sbx files";				
			}	
			else if ((nRequests == 10)&&(extList[j] == ".shp.xml")) {
				formData.my_test=nRequests + ": missing .shp.xml file";				
			}				
			else if (fs.existsSync(extFile)) {
				msg+=",\n" + extFile;
				formData.attachments.push(fs.createReadStream(extFile));
				noFiles++;			
				console.log("extFile[" + noFiles + "]: " + extFile);
			}
		}
	}
	
// Test case: fields
	
	console.log("Sending SAHSULAND request: " + nRequests + "; files: " + noFiles + "\n" + msg); 
	resultArrayDesc[nRequests]=formData.my_test;
	
    this.options = {
        url:  'http://127.0.0.1:3000/shpConvert',
        headers:{'Content-Type': contentType},
        formData: formData
    }; 
}

/*
 * Function: postIt()
 * Parameter: Request debug (true/false) 
 */
var postIt = function(debug) {
	var r = new MakeRequest(); 
	r.request = require('request');

// Request debugging - single test node only	
	if (debug) {
		r.request.debug = true;
	
		// Debugger function
		require('request-debug')(r.request, 
			function(type, data, r) {
				console.log('Request debug: ' + type + 
					";\nheaders" + JSON.stringify(data.headers, null, 4).substring(0, 132) + 
					";\nbody" + JSON.stringify(data, null, 4).substring(0, 132))
		});
	}

	// Post request, with callback for when complete. This will cause multiple tests to run in almost any order
	var p=r.request.post(r.options, function optionalCallback(err, httpResponse, body) {
		tests++;
		if (err && err.code === 'ETIMEOUT') {
			console.error('Upload failed: ' + JSON.stringify(httpResponse, null, 4) + 
				"err.connect: " + err.connect + 
				"\nError: ", err);
			failed++;
		} 		
		else if (err && err.code === 'ECONNRESET') {
			console.error('Upload failed [SERVER FAILURE]: ' + 
				"\nError: ", err);
			failed++;
		} 
		else if (err) {
			console.error('Upload failed: ' + JSON.stringify(httpResponse, null, 4) + 
				"\nError: ", err);
			failed++;
		}
		else if (httpResponse.statusCode != 200) {
			var expected_to_pass=true;
			try {
				var jsonData = JSON.parse(body);
				var ofields=jsonData.fields;		
				var file_list=jsonData.file_list;
				var my_reference = ofields["my_reference"] || 'No reference';
				expected_to_pass = ofields["expected_to_pass"] || true;
				var error = jsonData.error || 'No error';
				console.error('\nUpload #' + my_reference + 
					' failed with HTTP status: ' + httpResponse.statusCode + 
					'\nServer error message >>>' + jsonData.message + 
					'\n<<< End of server error message\nError exception caught by server >>>\n' + error +
					'\n<<< End of error exception caught by server\nfiles processed: ' + jsonData.no_files +
					'; fields: ' + JSON.stringify(ofields, null, 4));
				if (jsonData.diagnostic) {
					console.error('\nServer diagnostic >>>' + jsonData.diagnostic + '\n<<< End of server diagnostic');
				}
				for (i = 0; i < jsonData.no_files; i++) {	
					 console.error("File [" + (i+1) + ":" + file_list[i].file_name + "]");
				}
				console.error('\nEnd of upload #' + ofields["my_reference"] + '\n');

			} catch (e) {                            // Catch message not in JSON errors			
				console.error('Upload failed with HTTP status: ' + httpResponse.statusCode + 
					"\n\nError(" + e.name + "): " + e.message + "\nStack>>>\n" + e.stack + "<<<" +
					"\nMessage body>>>\n" + body + "\n<<< End of message body\n");
				failed++;
				return;
			}
			if (expected_to_pass == "true") {
				if (ofields["my_reference"]) {
					resultArray[ofields["my_reference"]]=false;
				}
				failed++;
				console.error('WARNING! test failed when expected to pass');
			}
			else {
				if (ofields["my_reference"]) {
					resultArray[ofields["my_reference"]]=true;
				}				
				passed++;
				console.error('GOOD! test failed as expected');
			}					
		}
		else { // No HTTP status
			var expected_to_pass=true;
			try {			
				var jsonData = JSON.parse(body);
				var geojson;
				var ofields=jsonData.fields;		
				var file_list=jsonData.file_list;
				var pct_compression;
				expected_to_pass = ofields["expected_to_pass"] || true;
				console.error('\nUpload #' + ofields["my_reference"] + '\nServer debug >>>' + jsonData.message + 
					'\n<<< End of server debug\n\nfiles processed: ' + jsonData.no_files +
					'; fields: ' + JSON.stringify(ofields, null, 4));
				for (i = 0; i < jsonData.no_files; i++) {	
					if (file_list[i].geojson) {
						geojson = JSON.stringify(file_list[i].geojson);
						console.error("File [" + (i+1) + ":" + file_list[i].file_name + "] - geojson length: " + geojson.length +
							"; file size: " + file_list[i].file_size + 
							"; transfer time: " + file_list[i].transfer_time + " S" + 
							"; geojson convert time: " + file_list[i].geojson_time + " S;\n" + 
							"uncompress time: " + (file_list[i].uncompress_time || "(Not compressed)") + " S" +
							"; uncompress file size: " + (file_list[i].uncompress_size || "(Not compressed)"));

					// Single test mode: print first 600 characters of formatted topoJSON
						 console.error("First 600 characters of formatted topoJSON >>>\n" + 
							JSON.stringify(geojson, null, 2).substring(0, 600) + "\n\n<<< formatted topoJSON\n");						
					}
					else {
						console.error("File [" + (i+1) + ":" + file_list[i].file_name + "] - no geojson\n" + 
							"; transfer time: " + file_list[i].transfer_time + " S;\n" + 
							"uncompress time: " + (file_list[i].uncompress_time || "(Not compressed)") + " S" +
							"; uncompress file size: " + (file_list[i].uncompress_size || "(Not compressed)"));
					}

				}
				console.error('\nEnd of upload #' + ofields["my_reference"] + '\n');				
			} catch (e) {                            // Catch message not in JSON errors			
				console.error('Upload failed with client exception: ' +
					"\n\nError(" + e.name + "): " + e.message + "\nStack>>>\n" + e.stack + "<<<" +
					"\nMessage body>>>\n" + body + "\n<<< End of message body\n");
			}	
			if (expected_to_pass == "true") {
				if (ofields["my_reference"]) {
					resultArray[ofields["my_reference"]]=true;
				}				
				passed++;
			}
			else {
				if (ofields["my_reference"]) {
					resultArray[ofields["my_reference"]]=false;
				}				
				console.error('WARNING! test passed when expected to fail');			
				failed++;
			}
		}
	});
};
  
// Test processing loop  
if (nRequests == 0) { // Process all requests
	var timeOut = function() {
		setTimeout(function() {
			if(nRequests++ < max_nRequests){ 
				postIt(false); //No request debug
				timeOut();    
			}
		}, 100);
	};

	timeOut();
}
else {
	postIt(true); // Enable request debug
}

// Wait until all tests complete
var timeOut2 = function() {
	
	setTimeout(function() {
		if (tests < max_nRequests) {
			timeOut2();   
		}
		else {
			if (max_nRequests != 1) { // argv
				for (var i=1; i<resultArray.length; i++) {
					if (resultArray[i] && resultArray[i] == false) {
						console.error("Test: " + i + " failed: " + resultArrayDesc[i]);
					}
					else if (resultArray[i] && resultArray[i] == true) {
						console.error("Test: " + i + " passed: " + resultArrayDesc[i]);
					}		
					else {
						console.error("Test: " + i + " undefined [likely an exception]: " + resultArrayDesc[i]);
					}				
				}	
			}
			else {
				if (resultArray[nRequests] && resultArray[nRequests] == false) {
					console.error("Test: " + nRequests + " failed: " + resultArrayDesc[nRequests]);
				}
				else if (resultArray[nRequests] && resultArray[nRequests] == true) {
					console.error("Test: " + nRequests + " passed: " + resultArrayDesc[nRequests]);
				}		
				else {
					console.error("Test: " + nRequests + " undefined [likely an exception]: " + resultArrayDesc[nRequests]);
				}					
			}
			if (failed > 0) {
				throw new Error("Failed " + failed + "/" + tests + "; passed: " + passed);		
			}
			else {
				console.error("All tests passed: " + passed + "/" + tests);
			}
		}
	}, 100);
}

timeOut2();

