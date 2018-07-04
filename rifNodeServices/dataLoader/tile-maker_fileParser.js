// ************************************************************************
//
// GIT Header
//
// $Format:Git ID: (%h) %ci$
// $Id: 7ccec3471201c4da4d181af6faef06a362b29526 $
// Version hash: $Format:%H$
//
// Description:
//
// Rapid Enquiry Facility (RIF) - File parser functions
//
// Copyright:
//
// The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
// that rapidly addresses epidemiological and public health questions using 
// routinely collected health and population data and generates standardised 
// rates and relative risks for any given health outcome, for specified age 
// and year ranges, for any given geographical area.
//
// Copyright 2014 Imperial College London, developed by the Small Area
// Health Statistics Unit. The work of the Small Area Health Statistics Unit 
// is funded by the Public Health England as part of the MRC-PHE Centre for 
// Environment and Health. Funding for this project has also been received 
// from the Centers for Disease Control and Prevention.  
//
// This file is part of the Rapid Inquiry Facility (RIF) project.
// RIF is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// RIF is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
// to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
// Boston, MA 02110-1301 USA
//
// Author:
//
// Peter Hambly, SAHSU
//

var fileList = {};
var geoDataLoaderParameters={};
var hierarchy_post_processing_sql = undefined;
	
/*
 * Function: 	shpConvertInput()
 * Parameters: 	files object
 * Returns: 	Nothing
 * Description:	Lots
 */
function shpConvertInput(files, shpConvertInputCallback) {

	/*
	 * Function: 	processFile()
	 * Parameters: 	File name, file size, base file name (no extension, no directory), unzipped file base name (optional), 
	 *				data (for DBF as arraybuf, for .shp.ea.iso.xml and .prj as text), original file name (unzipped or not)
	 * Returns: 	Nothing
	 * Description:	Process file by type: SHP, DBF or .SHP.EA.ISO.XML (extended attributes XML file); add to three used objects; detect duplicates
	 * Uses:		fileList 
	 *				xmlDocList 
	 *				shapefileList 
	 *				projectionList
	 */	
	function processFile(fileName, fileSizeBytes, baseName, ext, unzipFileName, data, originalFileName) {
		scopeChecker({ // Check - should be in scope!
			fileList: fileList,
			xmlDocList: xmlDocList,
			shapefileList: shapefileList,
			projectionList: projectionList			
		});
		
		var end=new Date().getTime();
		var elapsed=(end - lstart)/1000; // in S	
				
		console.log("Processing file: " + fileName + "; size: " + fileSizeBytes);
//		console.error("originalFileName: " + JSON.stringify(originalFileName, null, 4));
			
		if (ext == 'shp') {
			document.getElementById("status").innerHTML =
				document.getElementById("status").innerHTML + "<br>Loaded shapefile file: " + name + "; " + fileSize(fileSizeBytes) + " in: " + elapsed + " S";
			if (shapefileList[baseName]) {
				if (fileList[baseName].fileName) {
					throw new Error("Duplicate file: " +  fileName + "; shape file: " + fileList[baseName].fileName + " already processed");
				}
				else {
					throw new Error("Duplicate file: " +  fileName + "; file: " + baseName + ".shp already processed");
				}
			}		
			shapefileList[baseName] = {
				fileSize: fileSizeBytes
			}
		}
		else if (ext == 'shp.ea.iso.xml') {
			if (xmlDocList[baseName]) {
				if (fileList[baseName].fileName) {
					throw new Error("Duplicate file: " +  fileName + "; shape file: " + fileList[baseName].fileName + " already processed");
				}
				else {
					throw new Error("Duplicate file: " +  fileName + "; file: " + baseName + ".shp.ea.iso.xml already processed");
				}
			}	
			var x2js = new X2JS();					
			var FC_FeatureCatalogue=x2js.xml_str2json(data);
			if (FC_FeatureCatalogue["FC_FeatureCatalogue"]) {
				xmlDocList[baseName] = FC_FeatureCatalogue["FC_FeatureCatalogue"];
				console.log("Parse extended attributes file: " +  fileName + "; shape file: " + fileList[baseName].fileName);
			}	
			else {
				throw new Error("Extended attributes file: " +  fileName + "; shape file: " + fileList[baseName].fileName + " does not contain FC_FeatureCatalogue");	
			}
//			console.log(JSON.stringify(xmlDocList[baseName], null, 4));
		}					
		else if (ext == 'dbf') {
			if (fileList[baseName]) {
				if (fileList[baseName].fileName) {
					throw new Error("Duplicate file: " +  fileName + "; shape file: " + fileList[baseName].fileName + " already processed");
				}
				else {
					throw new Error("Duplicate file: " +  fileName + "; file: " + baseName + ".dbf already processed");
				}
			}	
			console.log("fileName: " + JSON.stringify(fileName, null, 4) + "; size: " + fileSizeBytes);
			console.log("baseName: " + JSON.stringify(baseName, null, 4));	
//			console.log("name: " + JSON.stringify(name, null, 4));	
			var dbfHeader=undefined;
			try {
				dbfHeader=readDbfHeader(data, fileName);
			}
			catch (err) {
				console.error("processFile() DBF file: " + baseName + ".shp; caught: " + err.message + JSON.stringify(err));
				throw err;
			}			
			fileList[baseName] = {
				fileName: 	baseName + ".shp",
				zipFileBaseName: originalFileName.substring(0, originalFileName.indexOf('.')).toLowerCase(),
				description: "",
				dbfHeader: 	dbfHeader,
				exAML: 		undefined,
				fileSize: 	undefined,
				projection:	undefined
			};
//			console.log("fileList: " + JSON.stringify(fileList, null, 4));
			console.log("shapefileList: " + JSON.stringify(shapefileList, null, 4));
		}
		else if (ext == 'prj') {
			if (projectionList[baseName]) {
				if (fileList[baseName].fileName) {
					throw new Error("Duplicate file: " +  fileName + "; file: " + fileList[baseName].fileName + " already processed");
				}
				else {
					throw new Error("Duplicate file: " +  fileName + "; file: " + baseName + ".shp.ea.iso.xml already processed");
				}
			}		
			projectionList[baseName]=data;
		}
	} // End of processFile()
	
	var lstart;
	var totalFileSize=0;
	fileList = {};
	var xmlDocList = {};
	var shapefileList = {};
	var projectionList = {};
				
	document.getElementById("status").innerHTML=initHtml;
	// Process inputted files
	updateCustomFileUploadInput(files.length);
	if (document.getElementById("progressbar")) { // JQuery-UI version
		$(".progress-label").text( "Processing selected files");
	}	
	async.forEachOfSeries(files, 
		function asyncSeriesIteree(file, fileno, callback) {
			try {
				if (file !== null && file.name !== undefined) {
						
					var name = file.name;	
					var ext = name.substring(name.indexOf('.') + 1).toLowerCase(); 				// Use the first "."
					var fileName = name.substring(name.lastIndexOf('/') + 1).toLowerCase(); 	// Remove path
					var baseName = fileName.substring(0, fileName.indexOf('.')).toLowerCase();	// Remove extension, use the first "."
					
					var reader = new FileReader();
					var savHtlml=document.getElementById("status").innerHTML;

					if (ext == "zip") {				
						document.getElementById("status").innerHTML =
							savHtlml + '<br>Loading and unzipping file [' + (fileno+1) + ']: ' + name + "; " + fileSize(file.size);	
					}
					else {				
						document.getElementById("status").innerHTML =
							savHtlml + '<br>Loading file [' + (fileno+1) + ']: ' + name + "; " + fileSize(file.size);	
					}

					lstart=new Date().getTime();		
					
					reader.onloadend = function fileReaderOnloadend(event) {
						console.log("[" + fileno + "] Loading file: " + name);
							
						var end=new Date().getTime();
						var elapsed=(end - lstart)/1000; // in S			
					
						var arrayBuf = new Uint8Array(event.target.result);
						var arrayIndex = 0;		  
						
						var data;
						if (ext == 'shp.ea.iso.xml') {
							data=arrayBuf.toString();
						}
						if (ext == 'prj') {
							data=arrayBuf.toString();
						}
						else if (ext == 'dbf') {
							data=arrayBuf;
						}
												
						if (ext == 'shp.ea.iso.xml' || ext == 'shp' || ext == 'dbf' || ext == 'prj') {
							try {
								// processFile(fileName, fileSizeBytes, baseName, ext, unzipFileName, data, originalFileName);
								processFile(file.name, file.size, baseName, ext, undefined /* unzipFileName */, data,
									file.name);
							}
							catch (e) {
								console.log("asyncSeriesIteree() WARNING! Caught error in processFile(): " + e.message);
								callback(e);
								return;
							}
						}
						else if (ext == 'xml' && baseName == "geoDataLoader") {
							parser = new DOMParser();
							xmlDoc = parser.parseFromString(data,"text/xml");
							console.log("Parsed geoDataLoader.xml >>>\n" + JSON.stringify(xmlDoc, null, 2));
						}
						
//
// Zip file processing
//				
						else if (ext == "zip") {
							var zipFileName=file.name;
							var zip=new JSZip(arrayBuf, {} /* Options */);
							var noZipFiles=0;
							var totalUncompressedSize=0;
							var zipMsg="";
							for (var ZipIndex in zip.files) {
								noZipFiles++;
								var fileContainedInZipFile=zip.files[ZipIndex];
								
								var zipExt = fileContainedInZipFile.name.substring(fileContainedInZipFile.name.indexOf('.', 1) + 1).toLowerCase();		// Use the first "."
								var zipName = fileContainedInZipFile.name.substring(fileContainedInZipFile.name.lastIndexOf('/') + 1).toLowerCase();	// Remove path
								var zipBaseName = zipName.substring(0, zipName.indexOf('.')).toLowerCase();												// Remove extension, use the first "."
								
								if (fileContainedInZipFile.dir) {
									zipMsg+="<br>Zip [" + noZipFiles + "]: directory: " + fileContainedInZipFile.name;
								}
								else if (zipExt == 'shp.ea.iso.xml') {
									zipMsg+="<br>Zip [" + noZipFiles + "]: " + zipName + 
										"; ESRI extended attributes file";
									data=fileContainedInZipFile.asText();
								}
								else if (zipExt == 'prj') {
									zipMsg+="<br>Zip [" + noZipFiles + "]: " + zipName + 
										"; Projection file";
									data=fileContainedInZipFile.asText();
								}
								else if (zipExt == 'shp') {
									unzipPct=Math.round(fileContainedInZipFile._data.uncompressedSize*100/fileContainedInZipFile._data.compressedSize)
									zipMsg+="<br>Zip [" + noZipFiles + "]: shapefile file: " + zipName + 
										"; expanded: " + unzipPct + 
										"% to: " +  fileSize(fileContainedInZipFile._data.length);
									totalUncompressedSize+=fileContainedInZipFile._data.length;
								}
								else if (zipExt == 'dbf') {
									data=fileContainedInZipFile.asArrayBuffer();
								}
								else {
									console.log("asyncSeriesIteree() WARNING! Ignoring[" + ZipIndex + "]: " + zipName + "; size: " + 
										fileContainedInZipFile._data.length || fileContainedInZipFile._data.uncompressedSize + " bytes");
		//							zipMsg+="<br>Zip file[" + noZipFiles + "]: file: " + zipName + 
		//								"; expanded: " + unzipPct + 
		//								"% to: " +  fileSize(fileContainedInZipFile._data.uncompressedSize) + "; extension: " + zipExt;
									totalUncompressedSize+=(fileContainedInZipFile._data.length || fileContainedInZipFile._data.uncompressedSize);
								}
								
								if (zipExt == 'shp.ea.iso.xml' || zipExt == 'shp' || zipExt == 'dbf'|| zipExt == 'prj') {
									try {
									console.log("asyncSeriesIteree() call processFile [" + ZipIndex + "]: " + zipName + "; size: " + 
										fileContainedInZipFile._data.length  || fileContainedInZipFile._data.uncompressedSize + " bytes");
									// processFile(fileName, fileSizeBytes, baseName, ext, unzipFileName, data, originalFileName);
										processFile(zipName, fileContainedInZipFile._data.length || fileContainedInZipFile._data.uncompressedSize, 
											zipBaseName, zipExt, zipName /* unzipFileName */, data, zipFileName);
									}
									catch (e) {
										console.log("asyncSeriesIteree() WARNING! Caught error in processFile(): " + e.message);
										callback(e);
										return;
									}
								}
								else if (zipExt == 'xml' && zipBaseName.toLowerCase() == "geodataloader") {
									var data=fileContainedInZipFile.asText();
									var x2js = new X2JS();					
									var geodataloader=x2js.xml_str2json(data);
									if (geodataloader["geoDataLoader"]) {
										if (geodataloader["geoDataLoader"].hierarchy_post_processing_sql) {
											hierarchy_post_processing_sql=geodataloader["geoDataLoader"].hierarchy_post_processing_sql;
											consoleLog("Parsed dataloader XML hierarchy_post_processing_sql; SQL>" + 
												hierarchy_post_processing_sql);
										}
										else {
											consoleLog("No dataloader XML hierarchy_post_processing_sql parsed");
										}
										if (geodataloader["geoDataLoader"].parameters) {
											consoleLog("Parsed dataloader XML configuration file: geoDataLoader.xml");
											geoDataLoaderParameters=geodataloader["geoDataLoader"].parameters;
											for (var key in geoDataLoaderParameters) { 
												consoleLog("geoDataLoader.xml parameter: " +
													key + '="' + geoDataLoaderParameters[key] + '"');
											}
										}
										else {
											throw new Error("Parsed dataloader XML configuration file: geoDataLoader.xml contains no parameters >>>\n" + 
												JSON.stringify(geodataloader["geoDataLoader"]));
										}	
									}
									else {
										throw new Error("dataloader XML configuration file: geoDataLoader.xml does not contain geoDataLoader element" +
											JSON.stringify(geodataloader));	
									}				
								}
								else {
									consoleLog("Ignored: " + zipBaseName + "." + zipExt)
								}
							} // End of for loop
							document.getElementById("status").innerHTML =
								document.getElementById("status").innerHTML + '<br>Loaded and unzipped file: ' + name + 
									"; from: " + fileSize(file.size) + " to: " + fileSize(totalUncompressedSize) + "; in: " + elapsed + " S" + zipMsg;	
									
							zip=undefined;
						} // End of zip processing			
						else {
							document.getElementById("status").innerHTML =
								document.getElementById("status").innerHTML + '<br>Loaded file: ' + name + "; " + fileSize(file.size) + " in: " + elapsed + " S";	
						}
		
						var percentComplete=Math.round((((fileno+1)/files.length)*100));
						if (document.getElementById("tabs")) { // JQuery-UI version
							progressbar.progressbar("value", percentComplete);
							if (percentComplete == 100) {
								progressLabel.text( "All selected files processed");
							}
						}		
						callback();						
					} // End of jsZipReaderOnloadend()
					
					reader.onerror = function fileReaderOnerror(err) {
						callback(err);
					}
					totalFileSize+=file.size;
			
					reader.readAsArrayBuffer(file); // Async		
				}		
			}
			catch (e) {
				console.log("asyncSeriesIteree() WARNING! Caught error: " + e.message);
				callback(e);
			}
		}, // End of asyncSeriesIteree() 
		function asyncSeriesEnd(err) {
			
			if (err) {
				try {	
					if (document.getElementById("tabs") && tabs && document.getElementById("error")) { // JQuery-UI version
						error=document.getElementById("error");
						error.innerHTML="<h3>Unable to process list of files</h3><p>" + err.message + "</p>";
						var errorWidth=document.getElementById('tabbox').offsetWidth-300;
						$( "#error" ).dialog({
							modal: true,
							width: errorWidth
						});
						tabs.tabs("refresh" );
					}
					else {	
						setStatus("ERROR! Processing list of files", err, undefined, err.stack);
					}
				}
				catch (e) {
					console.log("Error: " + e.message);
				}
				shpConvertInputCallback(err);
			}
			
			for (var key in fileList) { // Add extended attributes XML doc
				if (xmlDocList[key]) {
					fileList[key].exAML=xmlDocList[key];
				}	
				/* Scope XML fragment:
				
					<gmx:scope>
						<gco:CharacterString>The State-County at a scale of 1:500,000</gco:CharacterString>
					</gmx:scope>
				 */
				if (xmlDocList[key] && xmlDocList[key].scope && xmlDocList[key].scope.CharacterString && xmlDocList[key].scope.CharacterString.__text) {
					fileList[key].description = xmlDocList[key].scope.CharacterString.__text;	// Add scope 
					
					if (xmlDocList[key].featureType && xmlDocList[key].featureType.FC_FeatureType && xmlDocList[key].featureType.FC_FeatureType.carrierOfCharacteristics) {
						var carrierOfCharacteristics=xmlDocList[key].featureType.FC_FeatureType.carrierOfCharacteristics;
						for (var i=0; i<carrierOfCharacteristics.length; i++) {
							var field;
							var description;
							
							if (carrierOfCharacteristics[i].FC_FeatureAttribute &&
							    carrierOfCharacteristics[i].FC_FeatureAttribute.memberName &&
							    carrierOfCharacteristics[i].FC_FeatureAttribute.memberName.LocalName) {
								field=carrierOfCharacteristics[i].FC_FeatureAttribute.memberName.LocalName.__text;
							}
							if (carrierOfCharacteristics[i].FC_FeatureAttribute &&
							    carrierOfCharacteristics[i].FC_FeatureAttribute.definition &&
							    carrierOfCharacteristics[i].FC_FeatureAttribute.definition.CharacterString) {
								description=carrierOfCharacteristics[i].FC_FeatureAttribute.definition.CharacterString.__text
							}
							if (fileList[key].dbfHeader.fields && field && description) {
								for (var j=0; j < fileList[key].dbfHeader.fields.length ; j++) {
									if (fileList[key].dbfHeader.fields[j].name.toUpperCase() == field.toUpperCase()) {									
										if (geoDataLoaderParameters[key + "_" + field]) {
											console.log("[" + key + "] Feature: " + i + "; geoDataLoader set field[" + j + "]: " + field + 
												" description='" + description + "'");
											fileList[key].dbfHeader.fields[j].description=geoDataLoaderParameters[key + "_" + field];
										}
										else {
											console.log("[" + key + "] Feature: " + i + "; ISO set field[" + j + "]: " + field + " description='" + description + "'");
											fileList[key].dbfHeader.fields[j].description=description;
										}
									}
									else {
										console.log("[" + key + "] Feature: " + i + "; NO MATCH Field: " + field + "='" + fileList[key].dbfHeader.fields[j].name + "'");
									}
								}
							}
							else if (field && description) {
								console.log("[" + key + "] Feature: " + i + " NO DBF header for field: " + field + " description='" + description + "'");
							}
						}
					}
					/*    
	"featureType": {
        "FC_FeatureType": {
			"carrierOfCharacteristics": [
			{
                    "FC_FeatureAttribute": {
                        "memberName": {
                            "LocalName": {
                                "__prefix": "gco",
                                "__text": "STATEFP"
                            },
                            "__prefix": "gfc"
                        },
                        "definition": {
                            "CharacterString": {
                                "__prefix": "gco",
                                "__text": "Current state Federal Information Processing Series (FIPS) code"
                            },
                            "__prefix": "gfc"
                        },
			*/
				}
			}
		//				console.log("xmlDocList: " + JSON.stringify(xmlDocList, null, 4));
			for (var key in fileList) { // Add shapefile size
				if (shapefileList[key]) {
					fileList[key].fileSize=shapefileList[key].fileSize;
				}
				else {
					shpConvertInputCallback(new Error("Missing shape file: " + fileList[baseName].fileName));						
				}
			}
			for (var key in fileList) { // Add projection
				if (projectionList[key]) {
					fileList[key].projection=projectionList[key];
				}
				else {
					shpConvertInputCallback(new Error("Missing projection file: " + fileList[baseName].fileName));						
				}
			}	
			
//			console.log("Completed fileList: " + JSON.stringify(fileList, null, 4));
					
			try {
				createAccordion(fileList);
				shpConvertInputCallback();
			}
			catch (err) {
				setStatus("ERROR! Unable to create accordion from list of files", 
					err, undefined, err.stack); 
				shpConvertInputCallback(err);
			}
		} // End of asyncSeriesEnd
	);		
				
} // End of shpConvertInput()

function xmlToJson(xml) {
    var attr,
        child,
        attrs = xml.attributes,
        children = xml.childNodes,
        key = xml.nodeType,
        obj = {},
        i = -1;

    if (key == 1 && attrs.length) {
      obj[key = '@attributes'] = {};
      while (attr = attrs.item(++i)) {
        obj[key][attr.nodeName] = attr.nodeValue;
      }
      i = -1;
    } else if (key == 3) {
      obj = xml.nodeValue;
    }
    while (child = children.item(++i)) {
      key = child.nodeName;
      if (obj.hasOwnProperty(key)) {
        if (obj.toString.call(obj[key]) != '[object Array]') {
          obj[key] = [obj[key]];
        }
        obj[key].push(xmlToJson(child));
      }
      else {
        obj[key] = xmlToJson(child);
      }
    }
    return obj;
  }
  
/*
 * Function: 	getFileList()
 * Parameters: 	None
 * Returns: 	file list object (processed files from shpConvertInput()
 * Description:	Creates accordion
 */
function getFileList() {
	return fileList;
}

/*
 * Function: 	createAccordion()
 * Parameters: 	file list object (processed files from shpConvertInput()
 * Returns: 	Nothing
 * Description:	Creates accordion
 */
function createAccordion(fileList) {					
	if (document.getElementById("tabs") && tabs) { // JQuery-UI version
		var newDiv = "";
		var fieldSelect1;
		var fieldSelect2;	
		var buttonList = [];
		var selectList = [];
				
		if (fileList == undefined || Object.keys(fileList).length == 0) {
			throw new Error("No fileList");
		}
		
//
// Sort file list by size
// 		
		var sortFileList=[];
		for (var key in fileList) {
			sortFileList.push([key, fileList[key]]);
		}
		var nSortFileList=sortFileList.sort(function fileListSort(a, b) {
			return parseInt(a[1].fileSize) - parseInt(b[1].fileSize);
		});
		var nFileList={};
		for (var i=0; i<nSortFileList.length; i++) {
			nFileList[nSortFileList[i][0]]=nSortFileList[i][1];
		}	

//
// Set globals from geoDataLoader XML parameters 
//
/*
geoDataLoader.xml parameter: quantization="1000000  nodeGeoSpatialFrontEnd.js:90:4
geoDataLoader.xml parameter: simplificationFactor="0.75  nodeGeoSpatialFrontEnd.js:90:4
geoDataLoader.xml parameter: max_zoomlevel="9  nodeGeoSpatialFrontEnd.js:90:4
geoDataLoader.xml parameter: geographyName="USA_2014  nodeGeoSpatialFrontEnd.js:90:4
geoDataLoader.xml parameter: geographyDesc="Description of: cb_2014_us_nation_5m  nodeGeoSpatialFrontEnd.js:90:4
 */
		if (geoDataLoaderParameters["geographyName"]) {
			document.getElementById('geographyName').value=geoDataLoaderParameters["geographyName"];
			consoleLog("geoDataLoader set geographyName: " + geoDataLoaderParameters["geographyName"]);
		}
		if (geoDataLoaderParameters["geographyDesc"]) {
			document.getElementById('geographyDesc').value=geoDataLoaderParameters["geographyDesc"];
			consoleLog("geoDataLoader set geographyDesc: " + geoDataLoaderParameters["geographyDesc"]);
		}
		if (geoDataLoaderParameters["quantization"]) {
			document.getElementById('quantizationSelect').value=geoDataLoaderParameters["quantization"];
			consoleLog("geoDataLoader set quantizationSelect: " + geoDataLoaderParameters["quantization"]);
		} 
		if (geoDataLoaderParameters["simplificationFactor"]) {
			document.getElementById('simplificationFactor').value=geoDataLoaderParameters["simplificationFactor"];
			consoleLog("geoDataLoader set simplificationFactor: " + geoDataLoaderParameters["simplificationFactor"]);
		} 
		if (geoDataLoaderParameters["max_zoomlevel"]) {
			document.getElementById('maxzoomlevelSelect').value=geoDataLoaderParameters["max_zoomlevel"];
			consoleLog("geoDataLoader set maxzoomlevelSelect: " + geoDataLoaderParameters["max_zoomlevel"]);
		} 
			
//		console.log("nFileList: " + JSON.stringify(nFileList, null, 4));
		fileList=nFileList;
		
		for (var key in fileList) {
			
			buttonList.push("#" + key + "_desc");
			selectList.push("#" + key + "_areaID");
			selectList.push("#" + key + "_areaName");
			buttonList.push("#" + key + "_areaID_desc");
			buttonList.push("#" + key + "_areaName_desc");
			
			fieldSelect1="";	
			fieldSelect2="";										
//			console.log(fileList[key].fileName + ": " + JSON.stringify(fileList[key].dbfHeader.fieldNames, null, 4));
 
//
// Create accordion HTML; see: https://jqueryui.com/accordion/
//

			for (var i=0; i< fileList[key].dbfHeader.fieldNames.length; i++) {

				if (i==0) { // Default for areaID
					fieldSelect1+='      <option value="' + fileList[key].dbfHeader.fieldNames[i] + 
						'" selected="selected">' + fileList[key].dbfHeader.fieldNames[i] + '</option>\n';
							
					// Also set geographyName to the key (base name of file) if not set
					if (!document.getElementById('geographyName').value) {
						if (fileList[key].zipFileBaseName) {
							document.getElementById('geographyName').value=fileList[key].zipFileBaseName;		
						}
						else {
							document.getElementById('geographyName').value=key;
						}
					}
					if (!document.getElementById('geographyDesc').value) {
						document.getElementById('geographyDesc').value="Description of: " + key;
					}
				}
				else {
					fieldSelect1+='      <option value="' + fileList[key].dbfHeader.fieldNames[i] + '">' + fileList[key].dbfHeader.fieldNames[i] + '</option>\n';
				}

				if (fileList[key].dbfHeader.fieldNames[i].toUpperCase() == "NAME") { // Default for areaName
					fieldSelect2+='      <option value="' + fileList[key].dbfHeader.fieldNames[i] + '" selected="selected">' + fileList[key].dbfHeader.fieldNames[i] + '</option>\n';
				}
				else {
					fieldSelect2+='      <option value="' + fileList[key].dbfHeader.fieldNames[i] + '">' + fileList[key].dbfHeader.fieldNames[i] + '</option>\n';
				}
			}
			
			newDiv+= 
				'<h3>Shapefile: ' + fileList[key].fileName + '; size: ' + fileSize(fileList[key].fileSize) + 
					'; fields: ' + fileList[key].dbfHeader.noFields + '; records: ' + fileList[key].dbfHeader.count + '</h3>\n' + 
				'<div id="' + key + '">\n' +	
				'  <label class="my-accordion-fields1" for="' + 
					key + '_desc" title="Please enter a description of the shapefile">Shape file description:\n' +  
				'  <input class="my-accordion-fields1" id="' + 
					key + '_desc" name="' + key + '_desc" type="text" value="' + fileList[key].description + '"  required/>' +
				'  </label>\n' + '<br>\n' +
				'  <label id="' + key + '_areaID_Tooltip" title="Please choose an area ID. This is the unique administrative code for area on the map" ' +
					'class="my-accordion-fields-areaID" for="' + 
					key + '_areaID">Area ID: \n' +
				'    <select required class="my-accordion-areaID2" id="' + key + '_areaID" name="' + key + 
					'_areaID" form="shpConvert">\n' +
				fieldSelect1 +
				'    </select>\n' + 
				'  </label>\n' +							
				'  <label class="my-accordion-desc" for="' + key + '_areaID_desc" title="Please enter a description of the choosen area ID">Label:\n' +  
				'    <input class="my-accordion-desc2" id="' + 
					key + '_areaID_desc" name="' + key + '_areaID_desc" type="text" required/>\n' +	
				'  </label>\n' +							
				'  <label id="' + key + '_areaName_Tooltip" title="Please choose an area name. This is the administrative name corresponding to the area ID" ' +
					'class="my-accordion-name" for="' + key + '_areaName">Area Name: \n' +
				'    <select required class="my-accordion-name2" id="' + key + '_areaName" name="' + key + 
					'_areaName" form="shpConvert">\n' +
				fieldSelect2 +
				'    </select>\n' + 
				'  </label>\n' +
				'  <label class="my-accordion-fields2" for="' + key + '_areaName_desc" title="Please enter a description of the choosen area name">Label:\n' +  
				'    <input class="my-accordion-fields2" id="' + 
					key + '_areaName_desc" name="' + key + '_areaName_desc" type="text" required>' +
				'  </label>\n' +							
				'</div>\n';

		} // End of for loop

//
// Parse accordion HTML to check validity
//	
		var html=$.parseHTML(newDiv);		
		if (html) { // OK
			if ($("#accordion").data("ui-accordion")) {
				$("#accordion").accordion("destroy");   // Removes the accordion bits
				$("#accordion").empty();                // Clears the contents
			}
//			console.log("newDiv >>>\n" + newDiv + "\n<<<");
			$("#accordion").html(newDiv);			// Add new
//
// Restore shapopefile field parameters from geoDataLoader XML parameters 
// 		
/*
geoDataLoader.xml parameter: cb_2014_us_nation_5m_desc="The nation at a scale of 1:5,000,000  nodeGeoSpatialFrontEnd.js:90:4
geoDataLoader.xml parameter: cb_2014_us_nation_5m_areaID="GEOID  nodeGeoSpatialFrontEnd.js:90:4
geoDataLoader.xml parameter: cb_2014_us_nation_5m_areaID_desc="Nation identifier  nodeGeoSpatialFrontEnd.js:90:4
geoDataLoader.xml parameter: cb_2014_us_nation_5m_areaName="NAME  nodeGeoSpatialFrontEnd.js:90:4
geoDataLoader.xml parameter: cb_2014_us_nation_5m_areaName_desc="Nation name  nodeGeoSpatialFrontEnd.js:90:4
 */
			var paramList=['desc', 'areaID_desc', 'areaName_desc'];
			var paramList2=['areaID', 'areaName'];
			for (var key in fileList) {		
				for (var i=0; i<paramList.length; i++) {
					var docParam = key + "_" + paramList[i];
					var elem=document.getElementById(docParam);
					if (elem &&geoDataLoaderParameters[docParam]) {	
						elem.value=geoDataLoaderParameters[docParam];
						consoleLog("geoDataLoader set: " + docParam + "=" + geoDataLoaderParameters[docParam]);	
					}	
					else {
						if (elem) {
							consoleLog("geoDataLoader unable to set: " + docParam + "=" + (geoDataLoaderParameters[docParam]||"N/A"));
						}
						else {
							consoleLog("geoDataLoader unable to set (no element): " + docParam + "=" + (geoDataLoaderParameters[docParam]||"N/A"));
						}
					}
				}	
	
// Now process areaID, areaName select lists
				for (var i=0; i<paramList2.length; i++) {
					var docParam = key + "_" + paramList2[i];
					var elem=document.getElementById(docParam);
					if (elem &&geoDataLoaderParameters[docParam]) {	
						for (var j = 0; j < elem.length; j++) {
							elem[j].selected = false;
							if (elem[j].value == geoDataLoaderParameters[docParam]) {
								elem[j].selected = true;
								consoleLog("geoDataLoader select: " + docParam + "=" + geoDataLoaderParameters[docParam]);
							} 
						}
					}	
					else {
						if (elem) {
							consoleLog("geoDataLoader unable to select: " + docParam + "=" + (geoDataLoaderParameters[docParam]||"N/A"));
						}
						else {
							consoleLog("geoDataLoader unable to select (no element): " + docParam + "=" + (geoDataLoaderParameters[docParam]||"N/A"));
						}
					}	
				}
			}			
		}
		else {
			throw new Error("Invalid HTML; newDiv >>>\n" + newDiv + "\n<<<");
		}					

		styleShapeFile();
		
		$("#accordion").accordion({ // Create accordion from HTML
//			active: false,
//			collapsible: true,
			heightStyle: "content"
		});
		$( buttonList.join(",") ).button();
		$( selectList.join(",") )
		  .selectmenu()
		  .selectmenu( "menuWidget" )
			.addClass( "overflow" );		
				
		var  styleArr2 = ["_areaID-button", "_areaName-button"]; // Style areaID, areaName select lists
		for (var key in fileList) {
			for (var j=0; j< styleArr2.length; j++) {
				var id=key + styleArr2[j];
				var item = document.getElementById(id);
				if (item) {		
					item.style.verticalAlign = "middle";
				}
				else {
					throw new Error("Cannot find id: " + id + "; unable to style; newDiv >>>\n" + newDiv + "\n<<<");
				}							
			}	
	
			console.log("Added accordion[" + key + "]: " + fileList[key].fileName);	
		} // End of for loop
		
		tabs.tabs("refresh" );
		
	} // End of if JQuery-UI version
}	

/*
 * Function: 	styleShapeFile()
 * Parameters: 	None 
 * Returns: 	Nothing
 * Description:	Resize shapefile accordions
 */
function styleShapeFile() {

	var isOpera = (window.navigator.userAgent.indexOf("OPR") > -1);
	var width="800px";	// Shape file description text entry box
	if (!!window.chrome && !isOpera) { // Chrome
		width="850px"; 	// Chrome is out by about -50px
	}
	var width2="170px";	// Area ID, area name buttons
	var width3="170px";	// Area ID, area name description text entry boxes
//		console.log("accordion width: " + document.getElementById('accordion').offsetWidth);
	if (document.getElementById('accordion').offsetWidth > 1500) {
		if (!!window.chrome && !isOpera) { // Chrome
			width="1308px";
			width2="220px";
			width3="370px";
		}
		else {
			width="1308px";
			width2="220px";
			width3="370px";
		}
	}		
	else if (document.getElementById('accordion').offsetWidth < 900) {
		width=document.getElementById('accordion').offsetWidth-175;
		if (!!window.chrome && !isOpera) { // Chrome
			width+=50;
		}
		var remain=width;
		width+="px";
		width2=(remain/4)-30; // Area ID, area name buttons
		width3=(remain/4)-15; // Area ID, area name description text entry boxes
		width2+="px";
		width3+="px";
		consoleLog("styleShapeFile() offsetWidth: " + document.getElementById('accordion').offsetWidth +
			", remain: " + remain +
			", width: " + width +
			", width2: " + width2 +
			", width3: " + width3);
	}
		
	for (var key in fileList) { // Style shapefile descriptions
		var id = key + "_desc";
		var item = document.getElementById(id);
		if (item) {
			item.style.width = width;
			item.style.textAlign = "left";
		}
		else {
			throw new Error("Cannot find id: " + id + "; unable to style; newDiv >>>\n" + newDiv + "\n<<<");
		}
	}
	
	var  styleArr = ["areaID", "areaName"]; // Add change function to select menus
	for (var key in fileList) {
		for (var j=0; j< styleArr.length; j++) {
			var id=key + "_" + styleArr[j];
			var item = document.getElementById(id);
			
			if (item) { // Style areaID, areaName
				item.style.width = width2;
				item.style.textAlign = "left";
			}
			else {
				throw new Error("Cannot find id: " + id + "; unable to style; newDiv >>>\n" + newDiv + "\n<<<");
			}
			
			$( "#" + id ).on( "selectmenuchange", function(event, ui) { // Add change function to aync _desc field
				updateAreaIdNameDesc(event.target.id, event.target.parentNode.parentNode.id, this.value);
			});
			
			var myId = document.getElementById(id);
			if (myId && geoDataLoaderParameters[id]) {	
				consoleLog("updateAreaIdNameDesc() not needed, id: " + myId.id +" set to: " + geoDataLoaderParameters[id + "_desc"]);
			}
			else if (myId) {
				updateAreaIdNameDesc(myId.id, myId.parentNode.parentNode.id, $( "#" + id ).val()); // Set defaults
			}
		}

	}
	
	var  styleArr = ["areaID_desc", "areaName_desc"]; // Style areaID, areaName descriptions
	for (var key in fileList) {
		for (var j=0; j< styleArr.length; j++) {
			var id=key + "_" + styleArr[j];		
			var item = document.getElementById(id);
			if (item) {
				item.style.width = width3;
				item.style.textAlign = "left";
			}
			else {
				throw new Error("Cannot find id: " + id + "; unable to style; newDiv >>>\n" + newDiv + "\n<<<");
			}
		}		
	} // End of for loop
} // End of styleShapeFile()
	
/*
 * Function: 	updateAreaIdNameDesc()
 * Parameters: 	Calling id, parent div id (file key), value
 * Returns: 	Nothing
 * Description:	Change event function to aync _desc field
 */
function updateAreaIdNameDesc(id, fileKey, value) {
	if (document.getElementById(id)) {	
//		console.log("updateAreaIdNameDesc() id: " + id + "; file key: " + fileKey + "; new value: " + value);
		if (fileList[fileKey] && fileList[fileKey].dbfHeader.fields) {
			for (var j=0; j < fileList[fileKey].dbfHeader.fields.length ; j++) {
				if (fileList[fileKey].dbfHeader.fields[j].name.toUpperCase() == value.toUpperCase()) {
//					console.log("Get field: " + value + " description='" + fileList[fileKey].dbfHeader.fields[j].description + "'");
					$( "#" + id + "_desc" ).val(fileList[fileKey].dbfHeader.fields[j].description);
				}
			}			
		}	
		else {
			throw new Error("updateAreaIdNameDesc(): invalid fileKey: " + fileKey);
		}
	}
	else {
		throw new Error("updateAreaIdNameDesc(): invalid id: " + id);
	}
}		

/*
 * Function: 	readDbfHeader()
 * Parameters: 	File data as array buffer, file name
 * Returns: 	Object {
 *					version: <file type>, [3 means "File without DBT" NOT DBase III!]
 *					date: <file date>,
 *					count: <record count>,
 *					headerBytes: <length of header in bytes>,
 *					noFields: <number of fields>,
 *					recordBytes: <record length in bytes>,
 *					fields: {
 *						name: <field name>,
 *						type: <type>,
 *						length: <field length>
 *					}
 * Description:	Read DBF file header
 * 				Derived very loosely from Mike Bostocks's shapefile reader code: https://github.com/mbostock/shapefile/blob/master/dbf.js
 * 				and the XBase DBF file format: http://www.clicketyclick.dk/databases/xbase/format/dbf.html#DBF_STRUCT
 *
 * Example object:
 
sahsu_grd_level4.dbf: {
	"description": undefined,
    "version": 3,
    "date": "2006-07-02T23:00:00.000Z",
    "count": 1230,
    "headerBytes": 193,
    "noFields": 5,
    "recordBytes": 61,
    "fields": [
        {
            "name": "PERIMETER",
            "type": "N",
            "length": 19,
			"description": undefined
        },
        {
            "name": "LEVEL4",
            "type": "C",
            "length": 15,
			"description": undefined
        },
        {
            "name": "LEVEL2",
            "type": "C",
            "length": 6,
			"description": undefined
        },
        {
            "name": "LEVEL1",
            "type": "C",
            "length": 5,
			"description": undefined
        },
        {
            "name": "LEVEL3",
            "type": "C",
            "length": 15,
			"description": undefined
        }
    ],
    "fieldNames": [
        "PERIMETER",
        "LEVEL4",
        "LEVEL2",
        "LEVEL1",
        "LEVEL3"
    ]
}

 */
function readDbfHeader(dbfData, fileName) {

	/*
	 * Function: 	ua2text()
	 * Parameters: 	Uint8Array, file name
	 * Returns: 	Uint8Array rendered as ASCII text
	 * Description:	Converts Uint8Array to ASCII text, ignoring NULLs
	 */	
	function ua2text(ua, j) {
		var s = '';
		for (var i = 0; i < ua.length; i++) {
			if (ua[i] != 0) { // Ignore terminating \0
				var c=String.fromCharCode(ua[i]);
				if (c == "") {
					throw new Error("readDbfHeader().ua2text() DBF file: " + fileName + "; field: " + j + "; unexpected non ascii character: " + ua[i] + " in position: " + i + "Uint8Array: " + ua.toString());
				}
				s += c;
			}
		}
		return s;
	}
	
	
	if (typeof dbfData == 'undefined') {
		throw new Error("readDbfHeader() DBF file: " + fileName + "; dbfData is undefined");
	}
	
	var header=new Uint8Array(dbfData, 0, 30);			// First 31 bytes of file are the header
	var records=new Uint32Array(dbfData, 4, 1);			// i.e. a 32 bit number
	var headerLength=new Uint16Array(dbfData, 8, 1);	// i.e. a 16 bit number
	var recordLength=new Uint16Array(dbfData, 10, 1);	// i.e. a 16 bit number

	try {
		var dbfHeader={
			version: header[0],
			date: new Date(1900 + header[1], header[2] - 1, header[3]), // Bytes 1-3 in format YYMMDD
			count: records[0],
			headerBytes: headerLength[0], 
			noFields: (headerLength[0] - 1 /* Terminator */ - 32 /* Record header */)/32,
			recordBytes: recordLength[0], 
			fields: [],
			fieldNames: []
		}
		
		for (var i=0; i< dbfHeader.noFields; i++) { // Now add fields (in 32 bit "rows")
			var fieldDescriptor=new Uint8Array(dbfData, 32+(i*32), 32);
			var dataType=String.fromCharCode(fieldDescriptor[11]);
			var field = {
				position: 32+(i*32),
				name:  ua2text(new Uint8Array(dbfData, 32+(i*32), 11), i).toUpperCase(),
				description: "",
				type: dataType,
				length: fieldDescriptor[16]
			};
			dbfHeader.fieldNames.push(field.name);
			dbfHeader.fields[i] = field;
		}
	}
	catch (err) {
		console.error("readDbfHeader() DBF file read header: " + fileName + "; caught: " + err.message + JSON.stringify(err));
		throw err;
	}

	try {
		var terminator=new Uint8Array(dbfData, headerLength[0]-1, 1); // This should align on 32 bit boundaries... (header has 31 bytes)
	}
	catch (err) {
		console.error("readDbfHeader() DBF file read terminator: " + fileName + "; caught: " + err.message + JSON.stringify(err) + "; dbfData length: " + dbfData.byteLength);
		throw err;
	}	
	if (terminator[0] != 0x0d) {
		console.error("readDbfHeader() DBF file: " + fileName + "; found: " + terminator[0].toString() + "(" + ua2text(terminator) + ")" +
			"; no terminator (0x0d) found in dbf file at postion: " + (headerLength[0]-1) + 
			"\nDBF header >>>\n" + JSON.stringify(dbfHeader, null, 4) + "\n<<< End of DBF header");
		throw new Error("readDbfHeader() DBF file: " + fileName + "; found: " + terminator[0].toString() + "(" + ua2text(terminator) + ")" +
			"; no terminator (0x0d) found in dbf file at postion: " + (headerLength[0]-1) + 
			"\nDBF header >>>\n" + JSON.stringify(dbfHeader, null, 4) + "\n<<< End of DBF header");
	}
	
	return dbfHeader;
}
