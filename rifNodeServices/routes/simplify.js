/*
 *
 * Only POST requests are processed
 * Expects a shapefile as input
 * Simplified shapefile as output 
 *
 */


var processData = require('./simplifyFilesHandler');  

exports.convert = function(req, res) {
      res.setHeader("Content-Type", "text/plain");
      
      if (req.method == 'POST') {
         var dataProcessor = new processData.Process();
         dataProcessor.makeOutFolder();
          
         req.busboy.on('file', function(fieldname, stream, filename, encoding, mimetype) {
            dataProcessor.copyFile(stream, filename, res);
          });
           
         req.busboy.on('field', function(fieldname, val, fieldnameTruncated, valTruncated) {});
          
         req.busboy.on('finish', function() {
           //addMsgToLog("Files are now processing....." ); 
           res.end('Files are now processing..'); 
         });

         return req.pipe(req.busboy);
          
    }else {
         setStatusCode( res, 405 );
         res.end("GET Requests not allowed.");
    };      
};


