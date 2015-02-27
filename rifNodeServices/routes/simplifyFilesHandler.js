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



function Process(){    
    
 var shps = [],
     outputDir = './output/',
     jobId = '',
     jobIdDir = '',
     zipName = '';


 this.makeOutFolder = function(){
  var d = new Date();   
  jobId  =  makeid() + d.getTime();
  jobIdDir = outputDir +  jobId + '_inProgress/' ;  
  fs.mkdir(jobIdDir, function(err, data){
     if(err) throw err;  
     makeLogFile();
  }); 
 };
     
    
 this.copyFile = function(rd, filename, cb,res) {  
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
    console.log('doneWriting')   
    done();
  });
  rd.pipe(wr);

  function done(err) {
    if (!cbCalled) {
      unzip(err,res);
      cbCalled = true;
    }
  }
};    
    
 var unzip = function(res){
    shps = [];    
    var zip = new AdmZip( jobIdDir + zipName);
    var zipEntries = zip.getEntries(); // an array of ZipEntry records
    zipEntries.forEach(function(zipEntry) {
        var fileName = zipEntry.name,
            pathName = zipEntry.entryName;
        if(fileName.split('.').pop() == 'shp'){
            console.log("PUSHING:" + fileName);
            shps.push ([ pathName, fileName]);
        };
    });
    try{
        zip.extractAllTo( jobIdDir, true);
        simplify(0);
    }catch(e){
        errorOccured('Exception occured' + e);
        console.log('Exception occured' + e);
    }
 };      
  
var makeLogFile = function(){
  var headerMsg = 'Job '+ jobId + ' started' ;    
  fs.writeFile( jobIdDir + 'log.log', '', function (err,data) {
    if (err) {
      return console.log(err);
    }
     //console.log(data);
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


var addMsgToLog = function(msg, clbk, status){
  var d = new Date();    
  var datetime = "---" + d.getDate() + "/"
        + (d.getMonth()+1)  + "/" 
        + d.getFullYear() + " @ "  
        + d.getHours() + ":"  
        + d.getMinutes() + ":" 
        + d.getSeconds();  
  fs.appendFile( this.jobIdDir + 'log.log',  datetime + "\r\n" +  msg + "\r\n\r\n"   , function (err) {
      if (err) throw err;
      if (clbk){ // rename folder
          clbk(status); 
          reset();      
      };
  });
 };


 var errorOccured = function(msg){
    addMsgToLog( msg, renameFolder, '_error/');
 };

 var success = function(msg){
    addMsgToLog("All Shapefiles have been succesfully simplified. Job done.", renameFolder, '_completed/');  
 };


renameFolder = function(status){
     return;
     var completedJobIdDir =  outputDir + jobId + status;
     fs.rename(this.jobIdDir, completedJobIdDir, function(err,data){
         if(err){
            addMsgToLog( "Error renaming jobId folder:" + err);
            throw err;
         }
         jobIdDir = completedJobIdDir;
     });
}


 var simplify = function(i){
    if( i >= shps.length){
        if(i>0){
          success(); 
        };
        return;
    };
    var currentShpPath = shps[i][0],
        currentShp = shps[i][1];
    addMsgToLog("Simplifying shapefile:" + currentShp);  
    mapshaper.runCommands('-i ' + jobIdDir + currentShpPath +'  -simplify 5% -o ' + jobIdDir + 'simpl' + currentShp , function(error,dataset){
       if(error) {
        console.log(process.memoryUsage());    
        errorOccured('Error while simplifying occured' + error);    
        throw   error;
       }
       dataset = null;    
       addMsgToLog("Done simplifying:" + currentShp  );    
       console.log("Done simplifying:" + currentShp );
       simplify(++i);    
    });
};
    
    
var reset = function(){
    shps = [],
    jobId = '',
    jobIdDir = '',
    zipName = ''
};    

};

exports.Process =  Process;


