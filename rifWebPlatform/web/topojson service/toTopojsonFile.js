(function() {

   var http = require('http'),
      inspect = require('util').inspect,
      Busboy = require('busboy'),
      topojson = require('topojson'),
      fs = require('fs'),
      endRequest = function(res, code, text, mime) {
         res.writeHead(code, {
            "Content-Type": mime
         });
         res.end(text);
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

   /*
    *
    * SERVER LISTENS TO PORT 1337
    * Excpects a vaild geojson as input
    * Topojson output is echoed as plain text
    *
    */


   http.createServer(function(req, res) {
      var fullData = '',
         fName = '',
         topology = '',
         UPPER_LIMIT = 1e6;

      req.setEncoding('utf-8');

      if (req.method == 'POST') {
         var busboy = new Busboy({
            headers: req.headers
         });

         var options = {
            verbose: false,
            //quantization:default
         };

         busboy.on('file', function(fieldname, stream, filename, encoding, mimetype) {
            fName = filename;
            stream.on('data', function(data) {
               fullData += data;
               if (fullData.length > UPPER_LIMIT) { // Max geojs allowed 2MB
                  //req.connection.destroy();
                  endRequest(res, 200, "File too big", "text/html");
               };
            });

            stream.on('end', function() {
               console.log("end");
               if (fName != '') {
                  try {
                     jsonData = JSON.parse(fullData);
                     topology = topojson.topology({
                        collection: jsonData
                     }, options);
                  } catch (e) {
                     var txt = "Your input file does not seem to be valid.<br/>" + e;
                     endRequest(res, 200, txt, "text/html");
                     return;
                  };
                  //console.log(topology.objects.collection); 
               };
            });
         });

         busboy.on('field', function(fieldname, val, fieldnameTruncated, valTruncated) {
            //console.log('Field [' + fieldname + ']: value: ' + inspect(val));
            if (fieldname == 'zoomLevel') {
               options.quantization = getQuantization(val);
            };
         });

         busboy.on('finish', function() {
            if (fName != '' && topology != '') {
               var output = JSON.stringify(topology);
               var fileTopoName = fName.replace(/\.[0-9a-z]+$/i, '.toposjon');
               fs.writeFile('./topojs/' + fileTopoName, output, function(err) {
                  if (err) {
                     throw err;
                     res.writeHead(200, {
                        "Content-Type": "text/plain"
                     });
                     endRequest(res, 200, "An error occured" + err, "text/plain");
                  };
                  console.log(fileTopoName + " has been saved!");
               });
            } else {
               endRequest(res, 200, "No File received", "text/plain");
            };

            res.writeHead(303, {
               Connection: 'close',
               Location: '/'
            });
            res.end();
         });

         req.pipe(busboy);

      } else {
         res.writeHead(200, {
            "Content-Type": "text/html"
         })
         res.end('<html><head><title>405 - Method not supported</title></head><body>' +
            '<form method=POST enctype=multipart/form-data><input type=file name=geojs><br><input type=submit>' +
            '<input type="hidden" name="quantization" value="7" /> ' + '</form>' + '</body></html>');
      }
   }).listen(1337);

}());