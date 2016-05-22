"use strict";

var fs = require("fs");
var JSZip = require("jszip");
var async = require("async");
var path = require("path");

// read a file as a stream and add it to a zip
var fileName="data/sahsuland.zip";
if (!fs.existsSync(fileName)) {
	throw new Error("Unable to create stream, file: " + fileName + "; does not exist");
}

fs.readFile(fileName, function fileReaderCallback(err, data) {
	if (err) {
		throw err;
	}
	var zip=new JSZip(data, {} /* Options */);
	var noZipFiles=0;
	var zipUncompressedSize=0;
	var msg="";
							
	async.forEachOfSeries(zip.files /* col */, 
		function zipProcessingSeries(zipFileName, ZipIndex, seriesCallback) { // Process zip file and uncompress			

			var seriesCallbackFunc = function seriesCallbackFunc(e) { // Cause seriesCallback to be named
				seriesCallback(e);
			}
					
			noZipFiles++;	
			var fileContainedInZipFile=zip.files[ZipIndex];	
			if (fileContainedInZipFile.dir) {
				msg+="Zip file[" + noZipFiles + "]: directory: " + fileContainedInZipFile.name + "\n";
			}
			else {
				msg+="Zip file[" + noZipFiles + "]: " +  path.basename(fileContainedInZipFile.name) + "; relativePath: " + fileContainedInZipFile.name + 
					"; date: " + fileContainedInZipFile.date + "\n";  
				if (fileContainedInZipFile._data) {
					zipUncompressedSize+=fileContainedInZipFile._data.uncompressedSize;
					msg+="Decompress from: " + fileContainedInZipFile._data.compressedSize + " to: " +  fileContainedInZipFile._data.uncompressedSize;
		
				}
				else {
					throw Error("No fileContainedInZipFile._data for file in zip: " + fileContainedInZipFile.name);
				}
				var buf=zip.files[ZipIndex].asNodeBuffer(); // No longer causes Error(RangeError): Invalid string length with >255M files!!! (as expected)
				msg+="; size: " + buf.length + " bytes\n";
			}
			seriesCallbackFunc();										
		}, 
		function zipProcessingSeriesEnd(err) {	
			if (err) {
				throw err;
			}
			else {
				msg+="Processed Zipfile file: " + fileName + "; number of files: " + noZipFiles + "; Uncompressed size: " + zipUncompressedSize;				
				console.error(msg);		
			}
		}); // End of async zip file processing		
});	