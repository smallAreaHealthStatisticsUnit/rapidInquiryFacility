(function() {
   var http = require('http'),
      inspect = require('util').inspect,
      Busboy = require('busboy'),
      topojson = require('topojson'),
      fs = require('fs');
   http.createServer(function(req, res) {
      req.setEncoding('utf-8');
      if (req.method == 'POST') {
         var busboy = new Busboy({
            headers: req.headers
         });
         var fullData = '';
         busboy.on('file', function(fieldname, stream, filename, encoding, mimetype) {
            console.log('File [' + fieldname + ']: filename: ' + filename + ', encoding: ' + encoding + ', mimetype: ' + mimetype);
            stream.on('data', function(data) {
               fullData += data;
               //stream.resume();
            });
            stream.on('end', function() {
               console.log(fullData)
               console.log('File [' + fieldname + '] Finished');
            });
         });
         busboy.on('field', function(fieldname, val, fieldnameTruncated, valTruncated) {
            console.log('Field [' + fieldname + ']: value: ' + inspect(val));
         });
         busboy.on('finish', function() {
            console.log('Done parsing form!');
            res.writeHead(303, {
               Connection: 'close',
               Location: '/'
            });
            res.end();
         });
         req.pipe(busboy);
      } else {
         console.log("[405] " + req.method + " to " + req.url);
         res.writeHead(405, "Method not supported", {
            'Content-Type': 'text/html'
         });
         res.end('<html><head><title>405 - Method not supported</title></head><body>   <h1>Method not supported.</h1></body></html>');
      }
      res.end();
   }).listen(1337);
}());