/*
 * CONVERTS GEOJSON(MAX 1MB) TO TOPOJSON
 * Only POST requests are processed
 * Expects a vaild geojson as input
 * Topojson output is echoed as plain text
 * Topojson have quantization on
 * The level of quantization is based on map tile zoom level
 * More info on quantization here: https://github.com/mbostock/topojson/wiki/Command-Line-Reference
 *
 * Author: Federico Fabbri
 * Imperial College London
 *
 */

var inspect = require('util').inspect,
    topojson = require('topojson'),
    fs = require('fs'),
    setStatusCode = function(res, code ){
        res.statusCode = code;
    },
    getQuantization = function(lvl) {
         if (lvl <= 6) {
            return 400;
         } else if (lvl == 7) {
            return 700;
         } else if (lvl == 8) {
            return 1500;
         } else if (lvl == 9) {
            return 3000;
         } else if (lvl == 10) {
            return 5000;
         } else {
            return 10000;
         }
     };



exports.convert = function(req, res) {
    var fullData = '',
         fName = '',
         topology = '',
         withinLimit = true,
         UPPER_LIMIT = 1e6;

      req.setEncoding('utf-8');
      res.setHeader("Content-Type", "text/plain");
    
      if (req.method == 'POST') {
           
         console.log("___");  
         var options = {
            verbose: false,
            //quantization:default
         };
          
         req.busboy.on('file', function(fieldname, stream, filename, encoding, mimetype) {
            fName = filename;
            stream.on('data', function(data) {
               fullData += data;  
               if (fullData.length > UPPER_LIMIT) { // Max geojs allowed 2MB
                  withinLimit = false;  
                  try { 
                      throw new Error("Stopping file upload...");
                  } catch (e) { 
                      res.end("File upload stopped."); 
                  };     
               };
            });

            stream.on('end', function() {
               console.log("end");
               if (fName != '' && withinLimit) {
                  try {
                     jsonData = JSON.parse(fullData);
                     topology = topojson.topology({
                        collection: jsonData
                     }, options);
                     var output = JSON.stringify(topology); 
                     res.write(output);  
                  } catch (e) {
                     res.write("Your input file does not seem to be valid." );
                     return;
                  };
                  //console.log(topology.objects.collection); 
               };
            });
             
         });
          
          
        req.busboy.on('field', function(fieldname, val, fieldnameTruncated, valTruncated) {
            // inspect(val));
            if (fieldname == 'zoomLevel') {
               options.quantization = getQuantization(val);
            };
         });
          
        req.busboy.on('finish', function() {
            if(withinLimit){
                res.end();
            }
         });

         req.pipe(req.busboy);
          
    }else {
         setStatusCode( res, 405  );
         res.end("GET Requests not allowed.");
    };      
};