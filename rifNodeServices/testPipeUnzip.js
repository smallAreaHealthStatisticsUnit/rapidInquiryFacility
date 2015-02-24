var fs = require('fs'),
    AdmZip = require('adm-zip');


function copyFile(source, target, cb) {
  var cbCalled = false;

  var rd = fs.createReadStream(source);
  rd.on("error", function(err) {
    done(err);
  });
  var wr = fs.createWriteStream(target);
  wr.on("error", function(err) {
    done(err);
  });
  wr.on("close", function(ex) {
    done();
  });
  rd.pipe(wr);

  function done(err) {
    if (!cbCalled) {
      cb(err);
      cbCalled = true;
    }
  }
};

copyFile('./data/geojs.zip','./output/geojs.zip', function(){
    var zip = new AdmZip('./output/geojs.zip');
    var zipEntries = zip.getEntries(); // an array of ZipEntry records 
    zipEntries.forEach(function(zipEntry) {
    console.log(zipEntry.toString()); // outputs zip entries information 
        //console.log(zipEntry.data.toString('utf8')); 
    });
    zip.extractAllTo('./output/',true);

});