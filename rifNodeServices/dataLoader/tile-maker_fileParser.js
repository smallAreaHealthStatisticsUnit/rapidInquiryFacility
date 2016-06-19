/*
 * Function: 	shpConvertInput()
 * Parameters: 	files object
 * Returns: 	Nothing
 * Description:	Lots
 */
function shpConvertInput(files) {
	var totalFileSize=0;
	var fileList = {};
	var xmlDocList = {};
				
	document.getElementById("status").innerHTML=initHtml;
	// Process inputted files
	updateCustomFileUploadInput(files.length);
	
	for (var fileno = 0; fileno < files.length; ++fileno) {
		var file=files[fileno];
		if (file !== null) {
				
			var name = file.name;	
			var ext = name.substring(name.indexOf('.') + 1).toLowerCase();
			var baseName = name.substring(0, name.indexOf('.')).toLowerCase();
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

			var lstart=new Date().getTime();				
			reader.onloadend = function(event) {
					
				var end=new Date().getTime();
				var elapsed=(end - lstart)/1000; // in S			
			
				var arrayBuf = new Uint8Array(event.target.result);
				var arrayIndex = 0;		  
				

				if (ext == 'shp') {
					document.getElementById("status").innerHTML =
						document.getElementById("status").innerHTML + "<br>Loaded shapefile file: " + name + "; " + fileSize(file.size) + " in: " + elapsed + " S";
				}
				else if (ext == 'dbf') {
					fileList[baseName] = {
						fileName: baseName + ".shp",
						dbfHeader: readDbfHeader(arrayBuf, name),
						exAML: undefined
					};
				}
				else if (ext == "zip") {						
					var zip=new JSZip(arrayBuf, {} /* Options */);
					var noZipFiles=0;
					var totalUncompressedSize=0;
					var zipMsg="";
					for (var ZipIndex in zip.files) {
						noZipFiles++;
						var fileContainedInZipFile=zip.files[ZipIndex];
						var zipExt = fileContainedInZipFile.name.substring(fileContainedInZipFile.name.indexOf('.', 1) + 1).toLowerCase();
						var zipName = fileContainedInZipFile.name.substring(fileContainedInZipFile.name.lastIndexOf('/') + 1).toLowerCase();
						var zipBaseName = zipName.substring(0, zipName.indexOf('.')).toLowerCase()
						if (fileContainedInZipFile.dir) {
							zipMsg+="<br>Zip [" + noZipFiles + "]: directory: " + fileContainedInZipFile.name;
						}
						else if (zipExt == 'shp.ea.iso.xml') {
							zipMsg+="<br>Zip [" + noZipFiles + "]: " + zipName + 
								"; ESRI extended attributes file";
							var x2js = new X2JS();
							xmlDocList[zipBaseName] = x2js.xml_str2json(fileContainedInZipFile.asText());
							totalUncompressedSize+=fileContainedInZipFile._data.uncompressedSize;
						}
						else if (zipExt == 'shp') {
							unzipPct=Math.round(fileContainedInZipFile._data.uncompressedSize*100/fileContainedInZipFile._data.compressedSize)
							zipMsg+="<br>Zip [" + noZipFiles + "]: shapefile file: " + zipName + 
								"; expanded: " + unzipPct + 
								"% to: " +  fileSize(fileContainedInZipFile._data.uncompressedSize);
							totalUncompressedSize+=fileContainedInZipFile._data.uncompressedSize;
						}
						else if (zipExt == 'dbf') {
							unzipPct=Math.round(fileContainedInZipFile._data.uncompressedSize*100/fileContainedInZipFile._data.compressedSize)

							totalUncompressedSize+=fileContainedInZipFile._data.uncompressedSize;
							var dbfData=fileContainedInZipFile.asArrayBuffer();
							fileList[zipBaseName] = {
								fileName: zipBaseName + ".shp",
								dbfHeader: readDbfHeader(dbfData, zipName),
								exAML: undefined
							};
							
							zipMsg+="<br>Zip [" + noZipFiles + "]: dBase file: " + zipName + 
								"; expanded: " + unzipPct + 
								"% to: " +  fileSize(fileContainedInZipFile._data.uncompressedSize) + "; ";		
//							console.log(zipName + ": " + JSON.stringify(fileList[zipBaseName].dbfHeader, null, 4));
						}
						else {
//							zipMsg+="<br>Zip file[" + noZipFiles + "]: file: " + zipName + 
//								"; expanded: " + unzipPct + 
//								"% to: " +  fileSize(fileContainedInZipFile._data.uncompressedSize) + "; extension: " + zipExt;
							totalUncompressedSize+=fileContainedInZipFile._data.uncompressedSize;
						}
					} // End of for loop
					document.getElementById("status").innerHTML =
						document.getElementById("status").innerHTML + '<br>Loaded and unzipped file: ' + name + 
							"; from: " + fileSize(file.size) + " to: " + fileSize(totalUncompressedSize) + "; in: " + elapsed + " S" + zipMsg;	
				}			
				else {
					document.getElementById("status").innerHTML =
						document.getElementById("status").innerHTML + '<br>Loaded file: ' + name + "; " + fileSize(file.size) + " in: " + elapsed + " S";	
				}	
				
				if (document.getElementById("tabs") && tabs) { // JQuery-UI version
					var newDiv = "";
					var fieldSelect1;
					var fieldSelect2;	
					var buttonList = [];
					var selectList = [];
					
					for (var key in fileList) {
						console.log("Added accordion[" + key + "]: " + fileList[key].fileName);
						
						buttonList.push("#" + key + "_desc");
						selectList.push("#" + key + "_areaID");
						selectList.push("#" + key + "_areaName");
						buttonList.push("#" + key + "_areaID_desc");
						buttonList.push("#" + key + "_areaName_desc");
						
						fieldSelect1="";	
						fieldSelect2="";										
						console.log(zipName + ": " + JSON.stringify(fileList[key].dbfHeader.fieldNames, null, 4));
						for (var i=0; i< fileList[key].dbfHeader.fieldNames.length; i++) {
							if (i==0) {
								fieldSelect1+='      <option value="' + fileList[key].dbfHeader.fieldNames[i] + '" selected="selected">' + fileList[key].dbfHeader.fieldNames[i] + '</option>\n';
							}
							else {
								fieldSelect1+='      <option value="' + fileList[key].dbfHeader.fieldNames[i] + '">' + fileList[key].dbfHeader.fieldNames[i] + '</option>\n';
							}
							fieldSelect2+='      <option value="' + fileList[key].dbfHeader.fieldNames[i] + '">' + fileList[key].dbfHeader.fieldNames[i] + '</option>\n';
						}
						newDiv+= 
						    '<h3>Shapefile: ' + fileList[key].fileName + '; fields: ' + fileList[key].dbfHeader.noFields + '; records: ' + fileList[key].dbfHeader.count + '</h3>\n' + 
							'<div>\n' +	
							'  <label for="' + key + '_desc">Description: </label>\n' +  
							'  <input id="' + key + '_desc" name="' + key + '_desc" type="text"><br>\n' +
							'  <label for="' + key + '_areaIDList">Area ID\n' +
							'    <select id="' + key + '_areaID" name="' + key + '_areaIDListname" form="shpConvert">\n' +
							fieldSelect1 +
							'    </select>\n' + 
							'  </label>\n' +							
							'  <label for="' + key + '_areaID_desc">Area ID description: </label>\n' +  
							'  <input id="' + key + '_areaID_desc" name="' + key + '_areaID_desc" type="text">\n' +	
							'  <label for="' + key + '_areaNameList">Area Name\n' +
							'    <select id="' + key + '_areaName" name="' + key + '_areaNameListname" form="shpConvert">\n' +
//							'      <option value="test" selected="selected">Text</option>' +
							fieldSelect2 +
							'    </select>\n' + 
							'  </label>\n' +
							'  <label for="' + key + '_areaName_desc">Area Name description: </label>\n' +  
							'  <input id="' + key + '_areaName_desc" name="' + key + '_areaName_desc" type="text"></div>\n' +								
							'</div>\n';
//						console.log("newDiv >>>\n" + newDiv + "\n<<<");
						$('#accordion').append(newDiv)
					} // End of for loop
					document.getElementById("accordion").innerHTML = newDiv;
										
					var  styleArr = ["_areaID", "_areaName"];
					for (var key in fileList) {
						for (var j=0; j< styleArr.length; j++) {
							var id=key + styleArr[j];
							var item = document.getElementById(id);
							if (item) {
								item.style.width = "200px";
							}
							else {
								throw new Error("Cannot find id: " + id + "; unable to style; newDiv >>>\n" + newDiv + "\n<<<");
							}
						}		
					} // End of for loop
					
					$("#accordion").accordion({
						active: false,
						collapsible: true,
						heightStyle: "fill"
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
					console.log("xmlDocList: " + JSON.stringify(xmlDocList, null, 4));
					tabs.tabs("refresh" );

				}				
			}
			reader.onerror = function(err) {
				setStatus("ERROR! Unable to upload file + " + fileno + "/" + files.length + ": " + 
					name, err, undefined, err.stack); 		
				return;
			}
			totalFileSize+=file.size;
			
			reader.readAsArrayBuffer(file);
			
		}
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
