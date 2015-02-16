var request = require('request');
var FormData = require('form-data');
var fs = require('fs');
var formData = {
   my_field: 'my_value',
   my_buffer: new Buffer([1, 2, 3]),
   // my_file: fs.createReadStream(  './geojsons/npm-debug.log'),
   attachments: [
    fs.createReadStream('./geojsons/78.json'),
    fs.createReadStream('./geojsons/26.json')
  ]
};
request.post({
   url: 'http://127.0.0.1:1337',
   formData: formData
}, function optionalCallback(err, httpResponse, body) {
   if (err) {
      return console.error('upload failed:', err);
   }
   console.log('Upload successful!  Server responded with:', body);
});