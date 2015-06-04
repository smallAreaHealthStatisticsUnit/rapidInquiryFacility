var FormData = require('form-data');
var fs = require('fs');
var inputFile = '../rifDatabase/GeospatialData/SAHSULAND/SAHSU_GRD_Level4.shp';
var contentType = 'application/zip';

var MakeRequest = function(){

    this.request = require('request');

    var formData = {
       my_field: 'my_value',
       my_buffer: new Buffer([1, 2, 3]),
       //my_file: fs.createReadStream(inputFile),
       attachments: [
         fs.createReadStream(inputFile)
      ]
    };

    var length = fs.statSync(inputFile).size;

     this.options = {
        url:  'http://127.0.0.1:3000/simplify',
        headers:{'Content-Type': contentType},
        formData: formData, 
        'content-length':length
    }; 
}


var nRequests = 0;

var postIt = function(){
   var r = new MakeRequest();    
   console.log("Sending requests:" + nRequests);    
   r.request.post(r.options, function optionalCallback(err, httpResponse, body) {
   if (err) {
      return console.error('upload failed:', err);
   }
   if (nRequests == 1) {
		console.log('Upload #'+nRequests+' successful!  Server responded with:', body);
    }
	else {
		console.log('Upload #'+nRequests+' successful!');
	}
 });
};
  

var timeOut = function(){
  setTimeout(function(){
    if(nRequests++ < 100){ 
      console.log(nRequests);
      postIt();
      timeOut();    
    }
  },100);
};

timeOut();