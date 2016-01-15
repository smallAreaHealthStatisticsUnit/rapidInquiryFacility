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
     },
     TempData = function(){
        this.fullData = '';
        this.fName = '';
        this.topology = '';
        this.withinLimit = true;
        
        return this; 
     },
     UPPER_LIMIT = 1e6;
     



exports.convert = function(req, res) {

      req.setEncoding('utf-8');
      res.setHeader("Content-Type", "text/plain");
	  
      var d = new TempData();
	  var expected_length = req.headers["content-length"];
//	  var expected_length = 0;
	  
      if (req.method == 'POST') {
            
         var options = {
            verbose: true,
            //quantization:default
         };
          
         req.busboy.on('file', function(fieldname, stream, filename, encoding, mimetype) {
            d.fName = filename;
			d.expected_length = expected_length;
			
            stream.on('data', function(data) {
               d.fullData += data;  
//			   if (d.fullData != '') {
//					console.error("toTopoJSON(): read: " + d.fullData.length);
//			   }
               if (d.fullData.length > d.UPPER_LIMIT) { // Max geojs allowed 2MB
                  d.withinLimit = false;  
                  try { 
                      console.log("toTopoJSON(): Stopping file: " + d.fName + " upload...");
                  } catch (e) { 
                      res.end("toTopoJSON(): File: " + d.fName + " upload stopped."); 
                  };     
               };
            });

            stream.on('end', function() {
//			   d.fullData = d.fullData.replace(/(\r\n|\n|\r)/gm,"");
               if (d.fName != '' && d.withinLimit) {
                  try {
                     jsonData = JSON.parse(d.fullData);
                     d.topology = topojson.topology({
                        collection: jsonData
                     }, options);
                     d.output = JSON.stringify(d.topology); 
                     res.write(d.output);  
					 console.error("toTopoJSON() file: " + d.fName + "; size: " + d.fullData.length+ "\r\nData:\r\n" + 
						d.output.substring(0, 132) + "\r\n");					 
                  } catch (e) {
                     console.error("ERROR! toTopoJSON(): Your input file: " + 
							d.fName + "; size: " + d.fullData.length + "/" + d.expected_length + ": does not seem to be valid: \n\n" + 
								e + "\r\nTruncated data:\r\n" + 
								d.fullData.substring(0, 132) + "...\r\n");					  
				     res.status(500);					  
                     res.write("ERROR! toTopoJSON(): Your input file: " + 
							d.fName + "; size: " + d.fullData.length + "/" + d.expected_length + ": does not seem to be valid: \n\n" + 
								e + "\r\Truncated data:\r\n" + 
								d.fullData.substring(0, 132) + "...\r\n");
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
            if(d.withinLimit){
                res.end();
            }
         });

         req.pipe(req.busboy);
          
    }else {
         setStatusCode( res, 405  );
         res.end("GET Requests not allowed.");
    };      
};