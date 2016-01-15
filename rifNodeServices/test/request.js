var FormData = require('form-data');
var fs = require('fs');
var inputFile = './data/test_6_sahsu_4_level4_0_0_0.js';
var contentType = 'application/json';

var nRequests = 0;

var MakeRequest = function(){

    this.request = require('request');
	var json_file = fs.createReadStream(inputFile);
    var length = fs.statSync(inputFile).size;
	
    var formData = {
       my_field: 'my_value',
//       my_buffer: new Buffer([1, 2, 3]),     
       attachments: [
         json_file
      ]
    };

   console.log("Sending " + inputFile + " request:" + nRequests + "; length: " + length); 
   
     this.options = {
        url:  'http://127.0.0.1:3000/toTopojson',
        headers:{'Content-Type': contentType},
        formData: formData, 
        'content-length':length
    }; 
}

var postIt = function(){
   var r = new MakeRequest();       
   r.request.post(r.options, function optionalCallback(err, httpResponse, body) {
   if (err) {
      return console.error('Upload #' + nRequests + ' failed: ' + JSON.stringify(httpResponse, null, 4) + "\r\nError: ", err);
   }
   else if (httpResponse.statusCode != 200) {
      return console.error('Upload #' + nRequests + ' failed HTTP status: ' + httpResponse.statusCode + "\r\nError: [", body + "]\r\n");
	   
   }
   else if (nRequests == 1) {
		console.log('Upload #' + nRequests + ': successful!  Server responded with:\r\n', body);
    }
	else {
		console.log('Upload #' + nRequests + ': successful!');
	}
 });
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