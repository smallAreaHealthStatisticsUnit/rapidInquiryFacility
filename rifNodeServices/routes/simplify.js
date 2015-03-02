/*
 *
 * USE BUSBOY TO PARSE MULTIPLART FORM REQUESTS
 * Only POST requests are processed
 * Expects a shapefile as input
 * Simplified shapefile as output 
 *
 */


var processData = require('./simplifyFiles/simplifyFilesHandler');  

exports.convert = function(req, res) {
      res.setHeader("Content-Type", "text/plain");
      var filesAttached = 1;
    
      if (req.method == 'POST') {
         var dataProcessor = new processData.Process();
          
         req.busboy.on('file', function(fieldname, stream, filename, encoding, mimetype) {
             var extension = filename.split('.').pop();
             if(extension != 'zip'){
                res.end('Not a zipped folder.');
                return false; 
             };
             
             if(filesAttached++ == 1){
                 dataProcessor.makeOutFolder();
                 var jobId = dataProcessor.copyFile(stream, filename);
                 res.write('Files are now being unzipped..please wait..'); 
             }else{
                res.end('Only one zipped folder allowed.'); 
             }
             
          });
           
         req.busboy.on('field', function(fieldname, val, fieldnameTruncated, valTruncated) {});
          
         req.busboy.on('finish', function() {
           res.end('Files are now being simplified..'); 
         });

         return req.pipe(req.busboy);
          
    }else {
         setStatusCode( res, 405 );
         res.end("GET Requests not allowed.");
    };      
};


