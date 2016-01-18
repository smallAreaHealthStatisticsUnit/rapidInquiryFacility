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
		formData["my_test"]="Verbose";		
	}	
	console.log("Sending " + inputFile + " request:" + nRequests + "; length: " + length +
		'; ' + JSON.stringify(formData, null, 4));
		
    this.options = {
        url:  'http://127.0.0.1:3000/toTopojson',
        headers:{'Content-Type': contentType},
        formData: formData, 
        'content-length':length
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
				var json = JSON.stringify(jsonData.topojson)
				console.log('Upload #' + nRequests + ': '+ jsonData.message + '; new length: ' + json.length);
			}
		});
//	var f=p.form();
 
//	f.append('verbose', true);
};
  

var timeOut = function(){
  setTimeout(function(){
    if(nRequests++ < 3){ 
//     console.log(nRequests);
      postIt();
      timeOut();    
    }
  },100);
};

timeOut();