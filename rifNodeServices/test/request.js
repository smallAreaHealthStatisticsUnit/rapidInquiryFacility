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

// Process Args
var nRequests = process.argv[2];
var max_nRequests = 18;
if (!nRequests) {
	nRequests = 0;
	console.log('Processing all request tests');
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

var MakeRequest = function(){

    this.request = require('request'); 
	
	var inputFile = './data/test_6_sahsu_4_level4_0_0_0.js';
	var contentType = 'application/json';
	var json_file;
	var json_file2;
	var json_file3;
	
// Tests using differed files other than ./data/test_6_sahsu_4_level4_0_0_0.js
	if (nRequests == 5) { 
		inputFile = './data/test_6_sahsu_4_level4_0_0_0.js.gz';
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
	
	}
	else if (nRequests == 6) { // wrong Content-Type, binary stream
		inputFile = './data/test_6_sahsu_4_level4_0_0_0.js.lz77';
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
	else if (nRequests == 10) { // Invalid GeoJSON
		inputFile = './data/helloworld.js';
		json_file = fs.createReadStream(inputFile);
	}	
	else if (nRequests == 11) { // Invalid lz77 (actaully a zip file!)
		inputFile = './data/test_6_sahsu_4_level4_0_0_0_is_zip.lz77';
		json_file = fs.createReadStream(inputFile);
	}
	else if (nRequests == 12) { // Invalid zip file
		inputFile = './data/test_6_sahsu_4_level4_0_0_0.zip';
		json_file = fs.createReadStream(inputFile);
	}	
	else if (nRequests == 13) { // Zero sized file
		inputFile = './data/test_6_sahsu_4_level4_0_0_0_zero_sized.js';
		json_file = fs.createReadStream(inputFile);
	}		
	else { // Defasult	
		json_file = fs.createReadStream(inputFile);
	}
	
    var length = fs.statSync(inputFile).size;
	var id=nRequests;
	
// Multi-file test	
	if (nRequests == 7) { 
			var formData = {
			my_test: "Defaults",
			my_reference: nRequests,
			attachments: [
				json_file,
				json_file2,
				json_file3
			],
			expected_to_pass: "true" 
		};
	}
	else {
		var formData = {
			my_test: "Defaults",
			my_reference: nRequests,
			attachments: [
				json_file
			],
			expected_to_pass: "true" 
		};
	}

	
// Test case: fields
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
	}	
	else if (nRequests == 6) {
		formData["verbose"]="true";
		formData["zoomLevel"]=0;	
		formData["my_test"]="gzip geoJSON file; wrong Content-Type; will work";		
	}
	else if (nRequests == 7) {
		formData["verbose"]="true";
		formData["zoomLevel"]=0;	
		formData["my_test"]="gzip geoJSON multiple files";		
	}
	else if (nRequests == 8) {
		formData["verbose"]="true";
		formData["zoomLevel"]=0;	
		formData["my_test"]="TopoJSON id support";	
		formData["id"]="gid";		
	}
	else if (nRequests == 9) {
		formData["verbose"]="true";
		formData["zoomLevel"]=0;	
		formData["my_test"]="TopoJSON id support: invalid id";	
		formData["id"]="invalid_id";
		formData["expected_to_pass"]="false"; 		
	}
	else if (nRequests == 10) {
		formData["verbose"]="true";
		formData["zoomLevel"]=0;	
		formData["my_test"]="TopoJSON conversion: invalid geoJSON";	
		formData["expected_to_pass"]="false"; 		
	}
	else if (nRequests == 11) {
		formData["verbose"]="true";
		formData["zoomLevel"]=0;	
		formData["my_test"]="Uncompress: invalid lz77";	
		formData["expected_to_pass"]="false"; 		
	}
	else if (nRequests == 12) {
		formData["verbose"]="true";
		formData["zoomLevel"]=0;	
		formData["my_test"]="Invalid zip file (not supported)";	
		formData["expected_to_pass"]="false"; 		
	}
	else if (nRequests == 13) {
		formData["verbose"]="true";
		formData["zoomLevel"]=0;	
		formData["my_test"]="Zero sized file";	
		formData["expected_to_pass"]="false"; 		
	}
	else if (nRequests == 14) {
		formData["verbose"]="true";
		formData["zoomLevel"]=0;	
		formData["my_test"]="TopoJSON property-transform test";	
		formData["property-transform-fields"]='["name","area_id","gid"]'; // Javascript array as text
	}	
	else if (nRequests == 15) {
		formData["verbose"]="true";
		formData["zoomLevel"]=0;	
		formData["my_test"]="TopoJSON property-transform support: invalid property-transform field";	
		formData["expected_to_pass"]="false"; 	
		formData["property-transform-fields"]='["invalid"]';
	}	
	else if (nRequests == 16) {
		formData["verbose"]="true";
		formData["zoomLevel"]=0;	
		formData["my_test"]="TopoJSON property-transform support: invalid property-transform array";	
		formData["expected_to_pass"]="false"; 	
		formData["property-transform-fields"]="invalid";
	}
	else if (nRequests == 17) {
		formData["verbose"]="true";
		formData["zoomLevel"]=0;	
		formData["my_test"]="TopoJSON id and property-transform test";	
		formData["property-transform-fields"]='["name","area_id","gid"]'; // Javascript array as tex
		formData["id"]="gid";				
	}
	else if (nRequests == 18) {
		formData["verbose"]="true";
		formData["zoomLevel"]=0;	
		formData["my_test"]="TopoJSON property-transform support: JSON injection tests";	
		formData["expected_to_pass"]="false"; 	
		formData["property-transform-fields"]='["eval(console.error(JSON.stringify(req, null, 4)))"]';
	}
	
	console.log("Sending " + inputFile + " request:" + nRequests + "; length: " + length); 
		
    this.options = {
        url:  'http://127.0.0.1:3000/toTopojson',
        headers:{'Content-Type': contentType},
        formData: formData, 
        'content-length': length
    }; 
	if (nRequests == 5) { // Gzipped file test	
		this.debug = true;
		this.options.headers={'Content-Type': contentType, 'Content-Encoding': 'gzip', "accept-encoding" : "gzip"};
		this.options.gzip = true;
		console.error("GZIP: " + JSON.stringify(formData, null, 4));
	}	
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
			console.error('Upload #' + nRequests + ' failed: ' + JSON.stringify(httpResponse, null, 4) + 
				"err.connect: " + err.connect + 
				"\nError: ", err);
			failed++;
		} else if (err) {
			console.error('Upload #' + nRequests + ' failed: ' + JSON.stringify(httpResponse, null, 4) + 
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
					'\nServer debug >>>' + jsonData.message + 
					'\n<<< End of server debug\nError exception caught by server >>>\n' + error +
					'\n<<< End of error exception caught by server\nfiles processed: ' + jsonData.no_files +
					'; fields: ' + JSON.stringify(ofields, null, 4));
				for (i = 0; i < jsonData.no_files; i++) {	
					 console.error("File [" + (i+1) + ":" + file_list[i].file_name + "]");
				}
				console.error('\nEnd of upload #' + ofields["my_reference"] + '\n');

			} catch (e) {                            // Catch message not in JSON errors			
				console.error('Upload failed with HTTP status: ' + httpResponse.statusCode + 
					"\n\nError(" + e.name + "): " + e.message + "\nStack>>>\n" + e.stack + "<<<" +
					"\nMessage body>>>\n" + body + "\n<<< End of message body\n");
			}
			if (expected_to_pass == "true") {
				failed++;
				console.error('WARNING! test failed when expected to pass');
			}
			else {
				passed++;
				console.error('GOOD! test failed as expected');
			}					
		}
		else { // No HTTP status
			var expected_to_pass=true;
			try {			
				var jsonData = JSON.parse(body);
				var topojson;
				var ofields=jsonData.fields;		
				var file_list=jsonData.file_list;
				var pct_compression;
				expected_to_pass = ofields["expected_to_pass"] || true;
				console.error('\nUpload #' + ofields["my_reference"] + '\nServer debug >>>' + jsonData.message + 
					'\n<<< End of server debug\n\nfiles processed: ' + jsonData.no_files +
					'; fields: ' + JSON.stringify(ofields, null, 4));
				for (i = 0; i < jsonData.no_files; i++) {	
					topojson = JSON.stringify(file_list[i].topojson);
					if (!file_list[i].uncompress_size) {
						pct_compression=Math.round((topojson.length/file_list[i].file_size)*100);
					}
					else {
						pct_compression=Math.round((topojson.length/file_list[i].uncompress_size)*100);
					}
					console.error("File [" + (i+1) + ":" + file_list[i].file_name + "]\n" +			
						"topoJSON length: " + topojson.length + 
						"; file size: " + file_list[i].file_size + 
						"; Topology() runtime: " + file_list[i].topojson_runtime + " S" + 
						"; transfer time: " + file_list[i].transfer_time + " S;\n" + 
						"uncompress time: " + (file_list[i].uncompress_time || "(Not compressed)") + " S" +
						"; uncompress file size: " + (file_list[i].uncompress_size || "(Not compressed)") + 
						"; JSON compression: " + pct_compression + "%\n");
					// Single test mode: print first 600 characters of formatted topoJSON
					if (max_nRequests == 1) {
						 console.error("First 600 characters of formatted topoJSON >>>\n" + 
							JSON.stringify(file_list[i].topojson, null, 2).substring(0, 600) + "\n\n<<< formatted topoJSON\n");
					}
				}
				console.error('\nEnd of upload #' + ofields["my_reference"] + '\n');				
			} catch (e) {                            // Catch message not in JSON errors			
				console.error('Upload failed with client exception: ' +
					"\n\nError(" + e.name + "): " + e.message + "\nStack>>>\n" + e.stack + "<<<" +
					"\nMessage body>>>\n" + body + "\n<<< End of message body\n");
			}	
			if (expected_to_pass == "true") {
				passed++;
			}
			else {
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

