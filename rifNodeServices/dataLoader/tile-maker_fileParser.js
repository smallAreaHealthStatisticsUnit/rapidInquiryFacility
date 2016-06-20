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

/*
 * Function: 	shpConvertInput()
 * Parameters: 	files object
 * Returns: 	Nothing
 * Description:	Lots
 */
function shpConvertInput(files) {

	/*
	 * Function: 	processFile()
	 * Parameters: 	File name, file size, base file name (no extension, no directory), zip file name (optional), 
	 *				data (for DBF as arraybuf, for .shp.ea.iso.xml and .prj as text)
	 * Returns: 	Nothing
	 * Description:	Process file by type: SHP, DBF or .SHP.EA.ISO.XML (extended attributes XML file); add to three used objects; detect duplicates
	 * Uses:		fileList 
	 *				xmlDocList 
	 *				shapefileList 
	 *				projectionList
	 */	
	function processFile(fileName, fileSizeBytes, baseName, ext, zipFileName, data) {
		scopeChecker({ // Check - should be in scope!
			fileList: fileList,
			xmlDocList: xmlDocList,
			shapefileList: shapefileList,
			projectionList: projectionList			
		});
		
		var end=new Date().getTime();
		var elapsed=(end - lstart)/1000; // in S	
				
		console.log("Processing file: " + fileName);
		if (ext == 'shp') {
			document.getElementById("status").innerHTML =
				document.getElementById("status").innerHTML + "<br>Loaded shapefile file: " + name + "; " + fileSize(fileSizeBytes) + " in: " + elapsed + " S";
			if (shapefileList[baseName]) {
				if (fileList[baseName].fileName) {
					throw new Error("Duplicate file: " +  fileName + "; shape file: " + fileList[baseName].fileName + " already processed");
				}
				else {
					throw new Error("Duplicate file: " +  ffileName + "; file: " + baseName + ".shp already processed");
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
			xmlDocList[baseName] = x2js.xml_str2json(data);
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
			fileList[baseName] = {
				fileName: 	baseName + ".shp",
				dbfHeader: 	readDbfHeader(data, name),
				exAML: 		undefined,
				fileSize: 	undefined,
				projection:	undefined
			};
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
	var fileList = {};
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
				if (file !== null) {
						
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
								processFile(file.name, file.size, baseName, ext, undefined /* Zip file name */, data, fileList);
							}
							catch (e) {
								console.log("asyncSeriesIteree() WARNING! Caught error in processFile(): " + e.message);
								callback(e);
								return;
							}
						}
						
//
// Zip file processing
//				
						else if (ext == "zip") {						
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
										"% to: " +  fileSize(fileContainedInZipFile._data.uncompressedSize);
									totalUncompressedSize+=fileContainedInZipFile._data.uncompressedSize;
								}
								else if (zipExt == 'dbf') {
									data=fileContainedInZipFile.asArrayBuffer();
								}
								else {
		//							zipMsg+="<br>Zip file[" + noZipFiles + "]: file: " + zipName + 
		//								"; expanded: " + unzipPct + 
		//								"% to: " +  fileSize(fileContainedInZipFile._data.uncompressedSize) + "; extension: " + zipExt;
									totalUncompressedSize+=fileContainedInZipFile._data.uncompressedSize;
								}
								
								if (zipExt == 'shp.ea.iso.xml' || zipExt == 'shp' || zipExt == 'dbf'|| zipExt == 'prj') {
									try {
										processFile(zipName, fileContainedInZipFile._data.uncompressedSize, zipBaseName, zipExt, zipName /* Zip file name */, data, fileList);
									}
									catch (e) {
										console.log("asyncSeriesIteree() WARNING! Caught error in processFile(): " + e.message);
										callback(e);
										return;
									}
								}
							} // End of for loop
							document.getElementById("status").innerHTML =
								document.getElementById("status").innerHTML + '<br>Loaded and unzipped file: ' + name + 
									"; from: " + fileSize(file.size) + " to: " + fileSize(totalUncompressedSize) + "; in: " + elapsed + " S" + zipMsg;	
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
				return;
			}
			
			for (var key in fileList) { // Add extended attributes XML doc
				if (xmlDocList[key]) {
					fileList[key].exAML=xmlDocList[key];
				}
			}
		//				console.log("xmlDocList: " + JSON.stringify(xmlDocList, null, 4));
			for (var key in fileList) { // Add shapefile size
				if (shapefileList[key]) {
					fileList[key].fileSize=shapefileList[key].fileSize;
				}
				else {
					throw new Error("Missing shape file: " + fileList[baseName].fileName);						
				}
			}
			for (var key in fileList) { // Add projection
				if (projectionList[key]) {
					fileList[key].projection=projectionList[key];
				}
				else {
					throw new Error("Missing projection file: " + fileList[baseName].fileName);						
				}
			}	
						
			try {
				createAccordion(fileList);
			}
			catch (err) {
				setStatus("ERROR! Unable to create accordion from list of files", 
					err, undefined, err.stack); 
			}
		} // End of asyncSeriesEnd
	);		
				
} // End of shpConvertInput()

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
		
		for (var key in fileList) {
			console.log("Added accordion[" + key + "]: " + fileList[key].fileName);
			
			buttonList.push("#" + key + "_desc");
			selectList.push("#" + key + "_areaID");
			selectList.push("#" + key + "_areaName");
			buttonList.push("#" + key + "_areaID_desc");
			buttonList.push("#" + key + "_areaName_desc");
			
			fieldSelect1="";	
			fieldSelect2="";										
//			console.log(fileList[key].fileName + ": " + JSON.stringify(fileList[key].dbfHeader.fieldNames, null, 4));
			for (var i=0; i< fileList[key].dbfHeader.fieldNames.length; i++) {
				if (i==0) {
					fieldSelect1+='      <option value="' + fileList[key].dbfHeader.fieldNames[i] + '" selected="selected">' + fileList[key].dbfHeader.fieldNames[i] + '</option>\n';
				}
				else {
					fieldSelect1+='      <option value="' + fileList[key].dbfHeader.fieldNames[i] + '">' + fileList[key].dbfHeader.fieldNames[i] + '</option>\n';
				}
				if (fileList[key].dbfHeader.fieldNames[i].toUpperCase() == "NAME") {
					fieldSelect2+='      <option value="' + fileList[key].dbfHeader.fieldNames[i] + '" selected="selected">' + fileList[key].dbfHeader.fieldNames[i] + '</option>\n';
				}
				else {
					fieldSelect2+='      <option value="' + fileList[key].dbfHeader.fieldNames[i] + '">' + fileList[key].dbfHeader.fieldNames[i] + '</option>\n';
				}
			}
			newDiv+= 
				'<h3>Shapefile: ' + fileList[key].fileName + '; size: ' + fileSize(fileList[key].fileSize) + '; fields: ' + fileList[key].dbfHeader.noFields + '; records: ' + fileList[key].dbfHeader.count + '</h3>\n' + 
				'<div>\n' +	
				'  <label class="my-accordion-fields1" for="' + key + '_desc">Shape file description: </label>\n' +  
				'  <input class="my-accordion-fields1" id="' + key + '_desc" name="' + key + '_desc" type="text"><br>\n' +
				'  <label class="my-accordion-fields2" for="' + key + '_areaIDList">Area ID: \n' +
				'    <select class="my-accordion-fields2" id="' + key + '_areaID" name="' + key + '_areaIDListname" form="shpConvert">\n' +
				fieldSelect1 +
				'    </select>\n' + 
				'  </label>\n' +							
				'  <label class="my-accordion-fields2" for="' + key + '_areaID_desc">Label: </label>\n' +  
				'  <input class="my-accordion-fields2" id="' + key + '_areaID_desc" name="' + key + '_areaID_desc" type="text">\n' +	
				'  <label class="my-accordion-fields2" for="' + key + '_areaNameList">Area Name: \n' +
				'    <select class="my-accordion-fields2" id="' + key + '_areaName" name="' + key + '_areaNameListname" form="shpConvert">\n' +
				fieldSelect2 +
				'    </select>\n' + 
				'  </label>\n' +
				'  <label class="my-accordion-fields2" for="' + key + '_areaName_desc">Label: </label>\n' +  
				'  <input class="my-accordion-fields2" id="' + key + '_areaName_desc" name="' + key + '_areaName_desc" type="text"></div>\n' +								
				'</div>\n';
		
		} // End of for loop
		
		var html=$.parseHTML(newDiv);
		if (html) {
			if ($("#accordion").data("ui-accordion")) {
				$("#accordion").accordion("destroy");   // Removes the accordion bits
				$("#accordion").empty();                // Clears the contents
			}
			console.log(fileList[key].fileName + ": newDiv >>>\n" + newDiv + "\n<<<");
			$("#accordion").html(newDiv);			// Add new
		}
		else {
			throw new Error("Invalid HTML; newDiv >>>\n" + newDiv + "\n<<<");
		}					
			
		for (var key in fileList) {
			var id = key + "_desc";
			var item = document.getElementById(id);
			if (item) {
				item.style.width = "800px"; // Chrome is out by about -50px
			}
			else {
				throw new Error("Cannot find id: " + id + "; unable to style; newDiv >>>\n" + newDiv + "\n<<<");
			}
		}
		
		var  styleArr = ["_areaID", "_areaName"];
		for (var key in fileList) {
			for (var j=0; j< styleArr.length; j++) {
				var id=key + styleArr[j];
				var item = document.getElementById(id);
				if (item) {
					item.style.width = "170px";
				}
				else {
					throw new Error("Cannot find id: " + id + "; unable to style; newDiv >>>\n" + newDiv + "\n<<<");
				}
			}		
		} // End of for loop
		
		$("#accordion").accordion({
//			active: false,
//			collapsible: true,
			heightStyle: "content"
		});
		$( buttonList.join(",") ).button();
		$( selectList.join(",") )
		  .selectmenu()
		  .selectmenu( "menuWidget" )
			.addClass( "overflow" );
			
		var  styleArr2 = ["_areaID-button", "_areaName-button"];
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
		} // End of for loop
		tabs.tabs("refresh" );
	} // End of if JQuery-UI version
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
            "length": 19
        },
        {
            "name": "LEVEL4",
            "type": "C",
            "length": 15
        },
        {
            "name": "LEVEL2",
            "type": "C",
            "length": 6
        },
        {
            "name": "LEVEL1",
            "type": "C",
            "length": 5
        },
        {
            "name": "LEVEL3",
            "type": "C",
            "length": 15
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
	
	var header=new Uint8Array(dbfData, 0, 30);
	var records=new Uint32Array(dbfData, 4, 1);
	var headerLength=new Uint16Array(dbfData, 8, 1);
	var recordLength=new Uint16Array(dbfData, 10, 1);

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
	for (var i=0; i< dbfHeader.noFields; i++) {
		var fieldDescriptor=new Uint8Array(dbfData, 32+(i*32), 32);
		var dataType=String.fromCharCode(fieldDescriptor[11]);
		var field = {
			position: 32+(i*32),
			name:  ua2text(new Uint8Array(dbfData, 32+(i*32), 11), i),
			type: dataType,
			length: fieldDescriptor[16]
		};
		dbfHeader.fieldNames.push(field.name);
		dbfHeader.fields[i] = field;
	}
	
	var terminator=new Uint8Array(dbfData, headerLength[0]-1, 32); 
	if (terminator[0] != 0x0d) {
		throw new Error("readDbfHeader() DBF file: " + fileName + "; found: " + terminator[0].toString() + "(" + ua2text(terminator) + ")" +
			"; no terminator (0x0d) found in dbf file at postion: " + (headerLength[0]-1) + 
			"\nDBF header >>>\n" + JSON.stringify(dbfHeader, null, 4) + "\n<<< End of DBF header");
	}
	
	return dbfHeader;
}
