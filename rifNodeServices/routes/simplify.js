/*
 *
 * Only POST requests are processed
 * Expects a shapefile as input
 * Simplified shapefile as output 
 *
 */

var mapshaper = require('mapshaper'),
    AdmZip = require('adm-zip');
    fs = require('fs');

 

var shps = [],
    directory = './output/',
    outputDir = './output/',
    zipName = '',
    d = new Date();


var copyFile = function(rd, target, cb,res) {
  var cbCalled = false;
  rd.on("error", function(err) {
    done(err);
  });
  var wr = fs.createWriteStream(target);
  wr.on("error", function(err) { 
    done(err);
  });
  wr.on("close", function(ex) {
    console.log('doneWriting')   
    done();
  });
  rd.pipe(wr);

  function done(err) {
    if (!cbCalled) {
      cb(err,res);
      cbCalled = true;
    }
  }
};


var unzip = function(res){
    var zip = new AdmZip(outputDir + zipName);
    var zipEntries = zip.getEntries(); // an array of ZipEntry records
    zipEntries.forEach(function(zipEntry) {
        var fileName = zipEntry.name,
            pathName = zipEntry.entryName;
        if(fileName.split('.').pop() == 'shp'){
            shps.push ([ pathName, fileName]);
        };
    });
    try{
        zip.extractAllTo( outputDir, true);
        simplify(0);
    }catch(e){
        console.log('Exception occured' + e);
    }
};



var simplify = function(i){
    if( i >= shps.length){
        return;
    };
    
    console.log('-i ' + outputDir + shps[i][0]  +'  -simplify 5% -o ' + outputDir + 'simpl' + shps[i][1] );
    var start = d.getTime();
    mapshaper.runCommands('-i ' + outputDir + shps[i][0]  +'  -simplify 5% -o ' + outputDir + 'simpl' + shps[i][1] , function(error,dataset){
       if(error) {
         console.log(process.memoryUsage());    
        throw   error;
       }
       dataset =null;    
       console.log("done:" +  (d.getTime() - start) /1000 );
       simplify(++i);    
    });
};



exports.convert = function(req, res) {
      res.setHeader("Content-Type", "text/plain");
      if (req.method == 'POST') {
          
         req.busboy.on('file', function(fieldname, stream, filename, encoding, mimetype) {
            zipName = filename;  
            copyFile(stream, outputDir + filename, unzip,res);
          });
           
         req.busboy.on('field', function(fieldname, val, fieldnameTruncated, valTruncated) {});
          
         req.busboy.on('finish', function() {
            console.log('finished');  
           res.end('Files are now processing..'); 
         });

         return req.pipe(req.busboy);
          
    }else {
         setStatusCode( res, 405 );
         res.end("GET Requests not allowed.");
    };      
};


