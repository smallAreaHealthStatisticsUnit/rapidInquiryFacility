var FormData = require('form-data');
var fs = require('fs');

var nRequests = 5;

var MakeRequest = function(){

    this.request = require('request');
	require('request-debug')(this.request, 
		function(type, data, r) {
			console.log('Request debug: ' + type + 
				";\nheaders" + JSON.stringify(data.headers, null, 4) + 
				";\nbody" + JSON.stringify(data, null, 4))
});
	
	var inputFile = './data/test_6_sahsu_4_level4_0_0_0.js';
	var contentType = 'application/json';
	var json_file;
	var json_file2;
	
// Gzipped file tests
	if (nRequests == 5) { 
		inputFile = './data/test_6_sahsu_4_level4_0_0_0.js.gz';
		contentType = 'application/zip';
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
	else {
		json_file = fs.createReadStream(inputFile);
	}
	
    var length = fs.statSync(inputFile).size;
	var id=nRequests;
	
// Default: test case 1	
	var formData = {
		my_test: "Defaults",
		my_reference: nRequests,
		attachments: [
			json_file
		]
	};
	
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
		this.request.debug = true;		
	}	
	else if (nRequests == 6) {
		formData["verbose"]="true";
		formData["zoomLevel"]=0;	
		formData["my_test"]="gzip geoJSON file; wrong Content-Type";		
	}
	
	formData["length"]=length;		
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
		this.options.headers={
			'Content-Type': contentType, 
			'Content-Transfer-Encoding': 'gzip', 
			'Transfer-Encoding': 'gzip', 
			'Content-Encoding': 'gzip', 
			'Accept-Encoding' : "gzip,zip,zlib"};
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
			var json = JSON.stringify(jsonData.topojson);
			var ofields=jsonData.fields;
			console.log('Upload #' + ofields["my_reference"] + ': '+ jsonData.message + '; new length: ' + json.length +
				'; fields: ' + JSON.stringify(ofields, null, 4));
		}
	});
};
  
postIt();   
