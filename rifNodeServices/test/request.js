var FormData = require('form-data');
var fs = require('fs');
var inputFile = './data/test_6_sahsu_4_level4_0_0_0.js';
var contentType = 'application/json';

var nRequests = 0;

var MakeRequest = function(){

    this.request = require('request');
	var json_file = fs.createReadStream(inputFile);
    var length = fs.statSync(inputFile).size;
	var id=nRequests;
	var formData = {
		my_test: "Defaults",
		my_reference: nRequests,
		attachments: [
			json_file
		]
	};
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
	formData["length"]=length;		
	console.log("Sending " + inputFile + " request:" + nRequests + "; length: " + length); 
//		'; ' + JSON.stringify(formData, null, 4));
		
    this.options = {
        url:  'http://127.0.0.1:3000/toTopojson',
        headers:{'Content-Type': contentType},
        formData: formData, 
        'content-length': length
    }; 
}

var postIt = function(){
	var r = new MakeRequest();       
	var p=r.request.post(r.options, function optionalCallback(err, httpResponse, body) {
		if (err) {
			return console.error('Upload #' + nRequests + ' failed: ' + JSON.stringify(httpResponse, null, 4) + "\r\nError: ", err);
		}
		else if (httpResponse.statusCode != 200) {
			return console.error('Upload #' + nRequests + ' failed HTTP status: ' + httpResponse.statusCode + "\r\nError: [", body + "]\r\n");
		   
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
  

var timeOut = function(){
  setTimeout(function(){
    if(nRequests++ < 4){ 
      postIt();
      timeOut();    
    }
  },100);
};

timeOut();