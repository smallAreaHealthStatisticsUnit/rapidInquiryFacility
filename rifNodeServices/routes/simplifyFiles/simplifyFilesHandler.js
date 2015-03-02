/*
 * COPY / UNZIP / SIMPLIFY FIRST SHAPEFILE FOUND
 * Only POST requests are processed
 * Expects a shapefile as input
 * Simplified shapefile as output 
 * NOTE: ADM-ZIP is blocking
 *
 */

function Process(){    
    
var mapshaper = require('mapshaper'),
    AdmZip = require('adm-zip');
    fs = require('fs');     
    
 var shps = [],
     outputDir = './output/',
     jobId = '',
     jobIdDir = '',
     zipName = '';

     
 var unzip = function(){
    shps = [];    
    var zip = new AdmZip( jobIdDir + zipName);
    var zipEntries = zip.getEntries(); // an array of ZipEntry records
    if( zipEntries == 0){
        addMsgToLog("No Files found in zipped Folder.")
        return;
    };
     
    zipEntries.every(function(zipEntry) {
        var fileName = zipEntry.name,
            pathName = zipEntry.entryName;

        if(fileName.split('.').pop() == 'shp'){
            addMsgToLog("ABOUT TO UNZIP SHAPEFILE");
            zip.extractEntryTo(zipEntry, jobIdDir );
            shps.push ([ pathName, fileName]);
            return false;
        };
        return true;
    });
     
    simplify(0);

 };      
  
var makeLogFile = function(){
  var headerMsg = 'Job '+ jobId + ' started' ;    
  console.log("JOBDIR:" + jobIdDir);    
  fs.writeFile( jobIdDir + 'log.log', '', function (err,data) {
    if (err) {
      return console.log(err);
    }
  });
  addMsgToLog(headerMsg);  
};

    
var makeid = function(){
    var text = "";
    var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    for( var i=0; i < 15; i++ )
        text += possible.charAt(Math.floor(Math.random() * possible.length));

    return text;
};


var addMsgToLog = function( msg ){
  var d = new Date();    
  var datetime = "---" + d.getDate() + "/"
        + (d.getMonth()+1)  + "/" 
        + d.getFullYear() + " @ "  
        + d.getHours() + ":"  
        + d.getMinutes() + ":" 
        + d.getSeconds();  
  fs.appendFile(jobIdDir + 'log.log',  datetime + "\r\n" +  msg + "\r\n\r\n"   , function (err) {
      if (err){
         addMsgToLog( " Could not append text to file, error:"  + err );
      };
  });
 };


 var errorOccured = function(msg){
    addMsgToLog( msg );
 };

 var success = function(msg){
    addMsgToLog("All Shapefiles have been succesfully simplified. Job done." );  
 };


 var simplify = function(i){

    var currentShpPath = shps[i][0],
        currentShp = shps[i][1];
     
    console.log("Simplifying shapefile:" + currentShp); 
    addMsgToLog("Simplifying shapefile:" + currentShp);  
     
    mapshaper.runCommands('-i ' + jobIdDir + currentShpPath +' encoding=latin1  -simplify 5%  -o ' + jobIdDir + 'simpl' + currentShp , 
    function(error,dataset){
       if(error) {
        console.log(process.memoryUsage());    
        errorOccured('Shapefile may not be valid. Error while simplifying occured' + error);    
       };
       dataset = null;    
       addMsgToLog("Done simplifying:" + currentShp  );    
       console.log("Done simplifying:" + currentShp );  
    });
};
       
     

 this.makeOutFolder = function(){
  var d = new Date();   
  jobId  =  makeid() + d.getTime();
  jobIdDir = outputDir +  jobId + '/' ;  
  fs.mkdir(jobIdDir, function(err, data){
     if(err){
         jobIdDir = outputDir;
         addMsgToLog('Could not create new folder for jobId:' + jobId);
     }
     makeLogFile();
  }); 
 };
     
    
 this.copyFile = function(rd, filename, cb) {  
  var target = jobIdDir + filename;
  zipName = filename;       
  var cbCalled = false;
  rd.on("error", function(err) {
    done(err);
  });
  var wr = fs.createWriteStream(target);
  wr.on("error", function(err) { 
    errorOccured("Error copying POST folder to disk:" + err);   
    done(err);
  });
  wr.on("close", function(ex) {
    console.log('doneWriting');  
    done();
    return jobId;  
  });
     
  rd.pipe(wr);

  function done(err) {
    if (!cbCalled) {
      unzip(err);
      cbCalled = true;
    }
  }
};    
   

};

exports.Process =  Process;


