var request = require('request');
var FormData = require('form-data');
var fs = require('fs');


var inputFile = './data/shapefiles.zip';
var contentType = 'application/zip'; //'application/json'

var formData = {
   my_field: 'my_value',
   my_buffer: new Buffer([1, 2, 3]),
   my_file: fs.createReadStream(inputFile),
   /*attachments: [
     fs.createReadStream(inputFile)
  ]*/
};

var length = fs.statSync(inputFile).size;

var options = {
    url:  'http://127.0.0.1:3000/simplify',
    headers:{'Content-Type': contentType},
    formData: formData,
    'content-length':length
};


request.post(options, function optionalCallback(err, httpResponse, body) {
   if (err) {
      return console.error('upload failed:', err);
   }
   console.log('Upload successful!  Server responded with:', body);
});
