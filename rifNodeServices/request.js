var FormData = require('form-data');
var fs = require('fs');
var inputFile = './data/geojs/26.json';
var contentType = /*'application/zip'*/'application/json';

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
        url:  'http://127.0.0.1:3000/toTopojson',
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
   console.log('Upload successful!  Server responded with:', body);
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