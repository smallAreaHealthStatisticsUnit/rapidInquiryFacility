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
// Rapid Enquiry Facility (RIF) - Node Geospatial common test front end
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
var map;
var tileLayer;
var JSONLayer=[];
var start;
var uploadTime;
var serverTime;
var jsonAddLayerParamsArray=[];
var initHtml;
var status;
		
// Extend Leaflet to use topoJSON
L.topoJson = L.GeoJSON.extend({  
  addData: function(jsonData) {    
    if (jsonData.type === "Topology") {
      for (key in jsonData.objects) {
        geojson = topojson.feature(jsonData, jsonData.objects[key]);
        L.GeoJSON.prototype.addData.call(this, geojson);
      }
    }    
    else {
      L.GeoJSON.prototype.addData.call(this, jsonData);
    }
  }  
});
// Copyright (c) 2013 Ryan Clark

/*
 * Function: 	formSetup()
 * Parameters:  formId, formName
 * Returns: 	Nothing
 * Description:	Common form setup code 
 */
function formSetup(formId, formName) {

	/*
	 * Function: 	processJson()
	 * Parameters:  data, status
	 * Returns: 	Nothing
	 * Description:	Post-submit callback for JQuery form
	 * 				Do not use setStatus or you become devine 
	 */
	function processJson(data, status) { 
		// 'data' is the json object returned from the server 
		if (!data) {
			document.getElementById("status").innerHTML = "FATAL: processJson(): data undefined: " + formName;
			console.error("FATAL: processJson(): data undefined: " + formName);
		}

		try {
			if (status == "success") {
				displayResponse(data, 200, formName);
			}
			else {
				displayResponse(data, status, formName);
			}
		}
		catch (e) {
			document.getElementById("status").innerHTML = "<h1>FATAL: Caught exception in displayResponse()</h1><h2>Error message: " + e.message + "</h2><pre>" + e.stack + "</pre>";
			console.error("FATAL: Caught exception in displayResponse(): " + e.message + "\n" + e.stack);	
		}
	}

    var options = { 
        target:        '#' + formId,   	    // target element(s) to be updated with server response 
        beforeSubmit:  checkRequest,  		// pre-submit callback 
        success:       processJson,  		// post-submit callback 
		error:		   errorHandler, 		// Error handler callback
		uploadProgress: uploadProgressHandler,	// uploadProgress handler
		data: 			{uuidV1: "1"}, 		// Add randomised reference		
        dataType:    'json',			   	// 'xml', 'script', or 'json' (expected server response type) 
        // other available options: 
        //url:       url         // override for form's 'action' attribute 
        //type:      type        // 'get' or 'post', override for form's 'method' attribute 
        //clearForm: true        // clear all form fields after successful submit 
        //resetForm: true        // reset the form after successful submit 
 
        // $.ajax options can be used here too, for example: 
        timeout:     180000    // mS - 10 minutes
		
    }; 

//	if (navigator.userAgent.toLowerCase().indexOf('firefox') > -1){
//		options.error=undefined;
//		options.iframe=true;		// This was a potential fix a spurious error 
									// Message:{"readyState":0,"responseText":"","status":0,"statusText":"error"} issues with firefox and ajax
									// Only. Has no effect other than no upload status
//	}
	
	try {
		files_elem=document.getElementById('files'); // Set up file checker
		var files = files_elem.files;
	
		function shpConvertInputHandler() {
			var fileList = this.files;
			
			console.log("shpConvertInput() event files: " + JSON.stringify(fileList, null, 4));
			shpConvertInput(fileList);
		}		
		files_elem.addEventListener("change", shpConvertInputHandler, false);
	

		
		if  (files.length > 0) { // Already set
			console.log("shpConvertInput() already set");
			setTimeout(function() {
				document.getElementById("status").innerHTML = document.getElementById("status").innerHTML + "<br>" + "Please wait for " + files.length + " file(s) to load";
				shpConvertInput(files);
			}, 500);
		}
		
		if (navigator.userAgent.toLowerCase().indexOf('firefox') > -1){
			document.getElementById(formId + "Submit").addEventListener("click", function xMLHttpRequestSubmitForm(event) {
				console.log("Event: " + formId + "Submit");
				event.preventDefault();
				submitFormXMLHttpRequest(formId, formName);
		 
				// !!! Important !!! 
				// always return false to prevent standard browser submit and page navigation 
				return false; 
			});
		}
		else {
// bind to the form's submit event 
			$('#' + formId).submit(function ajaxSubmitForm() { 
				// inside event callbacks 'this' is the DOM element so we first 
				// wrap it in a jQuery object and then invoke ajaxSubmit 
				$(this).ajaxSubmit(options); 
		 
				// !!! Important !!! 
				// always return false to prevent standard browser submit and page navigation 
				return false; 
			}); 
		}
	
	}
	catch (e) {
		document.getElementById("status").innerHTML = "Caught error in ajaxSubmit(" + formId + "): " + e.message + "\n" + e.stack;
		console.error(document.getElementById("status").innerHTML);
	}
	
	
	if (document.getElementById("shapeFileStatus")) { // JQuery-UI version
		document.getElementById("shapeFileStatus").innerHTML = formName + " form ready.";
		initHtml=document.getElementById("shapeFileStatus").innerHTML;
	}
	else {
		document.getElementById("status").innerHTML = document.getElementById("status").innerHTML + "<br>" + formName + " form ready.";
		initHtml=document.getElementById("status").innerHTML;
	}
	
	if (document.getElementById("tabs") && tabs) { // JQuery-UI version
		tabs.tabs("refresh" );
	}
			
	console.log("Ready: " + formId);
} 	

/*
 * Function: 	shpConvertInput()
 * Parameters: 	files object
 * Returns: 	Nothing
 * Description:	Lots
 */
function shpConvertInput(files) {
	var totalFileSize=0;
	var fileArray = [];
				
	document.getElementById("status").innerHTML=initHtml;
	// Process inputted files
	updateCustomFileUploadInput(files.length);
	
	for (var fileno = 0; fileno < files.length; ++fileno) {
		var file=files[fileno];
		if (file !== null) {
				
			var name = file.name;	
			var ext = name.substring(name.lastIndexOf('.') + 1).toLowerCase();
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
					fileArray.push(name);
					document.getElementById("status").innerHTML =
						document.getElementById("status").innerHTML + "<br>Loaded shapefile file: " + name + "; " + fileSize(file.size) + " in: " + elapsed + " S";
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
						
						if (fileContainedInZipFile.dir) {
							zipMsg+="<br>Zip [" + noZipFiles + "]: directory: " + fileContainedInZipFile.name;
						}
						else if (zipExt == 'shp.ea.iso.xml') {
							zipMsg+="<br>Zip [" + noZipFiles + "]: " + zipName + 
								"; ESRI extended attributes file";
							totalUncompressedSize+=fileContainedInZipFile._data.uncompressedSize;
						}
						else if (zipExt == 'shp') {
							unzipPct=Math.round(fileContainedInZipFile._data.uncompressedSize*100/fileContainedInZipFile._data.compressedSize)
							fileArray.push(zipName);
							zipMsg+="<br>Zip [" + noZipFiles + "]: shapefile file: " + zipName + 
								"; expanded: " + unzipPct + 
								"% to: " +  fileSize(fileContainedInZipFile._data.uncompressedSize);
							totalUncompressedSize+=fileContainedInZipFile._data.uncompressedSize;
						}
						else if (zipExt == 'dbf') {
							unzipPct=Math.round(fileContainedInZipFile._data.uncompressedSize*100/fileContainedInZipFile._data.compressedSize)
							zipMsg+="<br>Zip [" + noZipFiles + "]: dBase file: " + zipName + 
								"; expanded: " + unzipPct + 
								"% to: " +  fileSize(fileContainedInZipFile._data.uncompressedSize);
							totalUncompressedSize+=fileContainedInZipFile._data.uncompressedSize;
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
					for (var i = 0; i < fileArray.length; i++) {
						console.log("Added accordion[" + i + "]: " + fileArray[i]);
						newDiv+= "<h3>" + fileArray[i] + "</h3><div><p>" + fileArray[i] + "</p></div>";
						$('#accordion').append(newDiv)
					}
					document.getElementById("accordion").innerHTML = newDiv;
					$("#accordion").accordion({
						active: false,
						collapsible: true,
						heightStyle: "fill"
					});
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
 * Function: 	submitFormXMLHttpRequest()
 * Parameters: 	output_type, formName
 * Returns: 	Nothing
 * Description:	Submit form button
 */
function submitFormXMLHttpRequest(output_type, formName) {
	var files_elem=document.getElementById('files');
	var files = files_elem.files;
	var verbose=document.getElementById('diagnostics').checked;
	
	var fileno = 0;
	var totalFileSize = 0;
	var timeout = 600*1000 // 600,000 ms; 10 minutes
	
	var request = new XMLHttpRequest();
    var formData = new FormData(document.getElementById(output_type));
	
	nodeGeoSpatialFrontEndInit();
	
	if (files.length == 0) {
		document.getElementById("status").innerHTML = "<h1>No files selected</h1>"
		console.log("FATAL! No files selected");
		return;
	}
	
	if (verbose) { // i.e. diagnostics
		console.log('Verbose mode: ' + verbose);
		formData.append('verbose', verbose);
	}	
	formData.append('uuidV1', generateUUID()); // Random reference

	// Display the key/value pairs
//	for (var pair of formData.entries()) { // Breaks in IE!
//		console.log("Key: " + pair[0] + '='+ pair[1]); 
//	}

	try {
		request.open('POST', output_type);
			// Process results
	
		request.onreadystatechange = function onreadystatechangeFunc() {
			
			if (request.readyState == 1) { 
				setStatus("Connected...");
			}
			else if (request.readyState == 2) { 
				setStatus("Request received...");
			}		
			else if (request.readyState == 3) { 
				setStatus("Processing request...");
			}
			else if (request.readyState == 4) {
				var end=new Date().getTime();
				var elapsed=(end - start)/1000; // in S
			
				if (request.response) {
					setStatus("Processing response..."); 
					displayResponse(request.response, request.status, formName);
				}
				else {
					console.log("[" + elapsed + "] Ignoring null response"); 
				}
			}
			else {		
				setStatus("request.onreadystatechange() unknown state: " + request.readyState, new Error("readyState error"));
			}
		}
		
		if (isIE() && request.timeout) {
			request.timeout = timeout;
			console.log('IE Timeout set: ' + timeout);
		}
		else {
			request.timeout = timeout;
			console.log('Timeout set: ' + timeout);
		}
		
		request.onabort = function onabortFunc() {
			var end=new Date().getTime();
			var elapsed=(end - start)/1000; // in S
		
			setStatus("Processing aborted after " + elapsed + " seconds", new Error("timeout error"));	
		}
		
		request.onerror = function onerrorFunc(e) {
			var end=new Date().getTime();
			var elapsed=(end - start)/1000; // in S
			
			if (e) {
				setStatus("Processing had error after " + elapsed + " seconds", undefined, "Event: " + JSON.stringify(e, null, 4) +
					"\nResponse headers: " + (request.getAllResponseHeaders() || "N/A") +
					"\nResponse type: " + (request.responseType || "N/A (DomString)"), undefined);	
			}
			else {
				setStatus("Processing had error after " + elapsed + " seconds", undefined, 
					"Response headers: " + (request.getAllResponseHeaders() || "N/A") +
					"\nResponse type: " + (request.responseType || "N/A (DomString)"), undefined);	
			}
		}
		request.ontimeout = function ontimeoutFunc(evt) {
			setStatus("Processing timed out after " + (timeout/1000) + " seconds", "timeout error");	
		}
		
	}
	catch (e) {
		setStatus("ERROR! Unable to open for post to: http://127.0.0.1:3000/" + output_type, e, undefined, e.stack);
		return;
	}
	
	// 
	request.upload.onprogress = function onprogressFunc(evt) {
		
		var end=new Date().getTime();
		var elapsed=(end - start)/1000; // in S
			
		if (evt.lengthComputable) {
			var percentComplete = Math.round(evt.loaded * 100 / evt.total);
			uploadProgressHandler(evt, evt.loaded, evt.total, percentComplete);
		}
		else {
			setStatus("[" + elapsed + "] " + "ERROR! Uploading files: unable to compute percent complete", new Error("UNKNOWN error in onprogressFunc()"));
		}
	}	

	try {			
		setStatus("Sending to server...");
		request.send(formData);	
		return;
	}
	catch (e) {
		setStatus("ERROR! Unable to post to: http://127.0.0.1:3000/" + output_type, e, undefined, e.stack);
		return;
	}		
//    request.sendAsBinary(formData); // Bad crash!!  
}
	
/*
 * Function: 	updateSimplificationFactorInput()
 * Parameters:  Value
 * Returns: 	Nothing
 * Description:	Update simplificationFactorInput box
 */
function updateSimplificationFactorInput(val) {
      document.getElementById('simplificationFactorInput').value=val; 
}
		
/*
 * Function: 	updateCustomFileUploadInput()
 * Parameters:  Value
 * Returns: 	Nothing
 * Description:	Update customFileUploadInput box if it exists
 */
function updateCustomFileUploadInput(val) {
	if (document.getElementById('customFileUploadInput')) {
		if (val == 0) {
			document.getElementById('customFileUploadInput').value="No files selected";
		}
		else if (val == 1) {
			document.getElementById('customFileUploadInput').value=val + " file selected";
		}
		else {
			document.getElementById('customFileUploadInput').value=val + " files selected";
		}
	}
}
	
/*
 * Function: 	checkRequest()
 * Parameters:  formData, jqForm, options
 * Returns: 	true/false. False prevents form submitting!
 * Description:	Pre-submit callback handler for JQuery form
 */
function checkRequest(formData, jqForm, options) { 
    // formData is an array; here we use $.param to convert it to a string to display it 
    // but the form plugin does this for you automatically when it submits the data 
		 	
	options.data.uuidV1=generateUUID();
	console.log("uuidV1: " + options.data.uuidV1);
	var queryString = $.param(formData); 
	var fileCount=0;
		
/*
formData: Array[4]
0:
Object {
name: "file"
type: "file"
value: File Object {
lastModified: 1464097549540
lastModifiedDate: Tue May 24 2016 14:45:49 GMT+0100 (GMT Daylight Time)
name: "SAHSU_GRD_Level4.json"
size: 5562945
type: "",
webkitRelativePath: ""} .. then inherited

*/	
	for (var i=0; i<formData.length; i++) {
		var value=formData[i].value;
		if (formData[i].type == "file" && !value.name) {
			console.log("Formdata: null file");
		}
		else if (formData[i].type == "file") {
			console.log("Formdata file[" + i + "]: " + value.name + "; size: " + value.size + "; last modified: " + value.lastModifiedDate);
			fileCount++;
		}
		else if (formData[i].type == "checkbox") {
			if (formData[i].required) {
				console.log("Formdata file[" + i + "] mandatory field: " + formData[i].name + "=" + formData[i].value);	
			}
			else {
				console.log("Formdata file[" + i + "] field: " + formData[i].name + "=" + formData[i].value);	
			}
		}		
		else if (formData[i].type == "range") {
			if (formData[i].required) {
				console.log("Formdata file[" + i + "] mandatory field: " + formData[i].name + "=" + formData[i].value);	
			}
			else {
				console.log("Formdata file[" + i + "] field: " + formData[i].name + "=" + formData[i].value);	
			}
		}
		else if (formData[i].type == "select-one") {
			if (formData[i].required) {
				console.log("Formdata file[" + i + "] mandatory field: " + formData[i].name + "=" + formData[i].value);	
			}
			else {
				console.log("Formdata file[" + i + "] field: " + formData[i].name + "=" + formData[i].value);	
			}
		}
		else {
			console.log("Formdata other [" + i + "]: " + JSON.stringify(formData[i], null, 4));
		}
	}
	if (fileCount == 0) {
		document.getElementById("status").innerHTML = "<h1>No files selected</h1>"
		console.log("FATAL! No files selected");
		return false;
	}
	
    // jqForm is a jQuery object encapsulating the form element.  To access the 
    // DOM element for the form do this: 
    // var formElement = jqForm[0]; 
	if (!setupMap) {
		document.getElementById("status").innerHTML = "<h1>setupMap() not defined: errors in nodeGeoSpatialServices.js</h1>"
		console.log("FATAL! setupMap() not defined: errors in nodeGeoSpatialServices.js");
		return false;
	}
    console.log('About to submit: ' + JSON.stringify(queryString, null, 4)); 
	nodeGeoSpatialFrontEndInit();
 
    // here we could return false to prevent the form from being submitted; 
    // returning anything other than false will allow the form submit to continue 
    return true; 
} 
 
 
/*
 * Function: 	nodeGeoSpatialFrontEndInit()
 * Parameters:	None
 * Description: Initialise globals
 */
function nodeGeoSpatialFrontEndInit() {
	JSONLayer=[];
	start=new Date().getTime();
	jsonAddLayerParamsArray=[];
}

/*
 * Function: 	scopeChecker()
 * Parameters:	file, line called from, named array object to scope checked mandatory, 
 * 				optional array (used to check optional callbacks)
 * Description: Scope checker function. Throws error if not in scope
 *				Tests: serverError2(), serverError(), serverLog2(), serverLog() are functions; serverLog module is in scope
 *				Checks if callback is a function if in scope
 *				Raise a test exception if the calling function matches the exception field value
 * 				For this to work the function name must be defined, i.e.:
 *
 *					scopeChecker = function scopeChecker(fFile, sLine, array, optionalArray) { ... 
 *				Not:
 *					scopeChecker = function(fFile, sLine, array, optionalArray) { ... 
 *				Add the ofields (formdata fields) array must be included
 */
scopeChecker = function scopeChecker(array, optionalArray) {
	var errors=0;
	var undefinedKeys;
	var msg="";
	var calling_function = scopeChecker.name || '(anonymous)';
	
	for (var key in array) {
		if (typeof array[key] == "undefined") {
			if (undefinedKeys == undefined) {
				undefinedKeys=key;
			}
			else {
				undefinedKeys+=", " + key;
			}
			errors++;
		}
	}
	if (errors > 0) {
		msg+=errors + " variable(s) not in scope: " + undefinedKeys;
	}
	
	// Check callback
	if (array && array["callback"]) { // Check callback is a function if in scope
		if (typeof array["callback"] != "function") {
			msg+="\nMandatory callback (" + typeof(callback) + "): " + (callback.name || "anonymous") + " is in use but is not a function: " + 
				typeof callback;
			errors++;
		}
	}	
	// Check optional callback
	if (optionalArray && optionalArray["callback"]) { // Check callback is a function if in scope
		if (typeof optionalArray["callback"] != "function") {
			msg+="\noptional callback (" + typeof(callback) + "): " + (callback.name || "anonymous") + " is in use but is not a function: " + 
				typeof callback;
			errors++;
		}
	}
	
	// Raise exception if errors
	if (errors > 0) {
		console.log("scopeChecker() ERROR: " + msg);
		throw new Error(msg);
	}	
} // End of scopeChecker()
	
/*
 * Function: 	setupMap()
 * Parameters: 	None
 * Returns: 	Nothing
 * Description:	Setup map width for Leaflet 
 */
function setupMap() {	
	if (document.getElementById("tabs")) { // JQuery-UI version
		var tabboxheight=document.getElementById('tabbox').offsetHeight;
		var height=document.getElementById('tilemakerbody').offsetHeight-(tabboxheight+24);
		function setHeight(id, lheight) {
			document.getElementById(id).setAttribute("style","display:block;cursor:pointer;cursor:hand;");
			document.getElementById(id).setAttribute("draggable", "true");
			document.getElementById(id).style.display = "block"	;
			document.getElementById(id).style.cursor = "hand";			
			document.getElementById(id).style.height=lheight + "px";						
			
			console.log(id + " h x w: " + document.getElementById(id).offsetHeight + "x" + document.getElementById(id).offsetWidth);	
		}

		console.log("Using JQuery-UI; setup map; height: " + height + "px; tabboxheight: " + tabboxheight + "px");
		
		// Check for min height
		
		setHeight("maptab", height);
		setHeight("map", (height-30));
		setHeight("statustab", (height-20));
		setHeight("status", (height-40));
		setHeight("shapeFileSelectortab", (height-10));
		setHeight("shapeFileSelector", (height-145));
		setHeight("accordion", (height-300));
		setHeight("shapeFileStatus", 70);			
		setHeight("progressbar", 40);			
	}
	else {
		var w = window.innerWidth
			|| document.documentElement.clientWidth
			|| document.body.clientWidth;

		var h = window.innerHeight
			|| document.documentElement.clientHeight
			|| document.body.clientHeight;
		if (h && w) {
			var old_w=document.getElementById('map').style.width;
			var old_h=document.getElementById('map').style.height;
			var new_w
			if (w > 1500) {
				new_w=w-750;
			} 
			else {
				new_w=w-400;
			}
			var new_h=h-150;
			var new_status_width=w-new_w-50;
		
			document.getElementById("status").setAttribute("style","display:block;cursor:pointer;cursor:hand;overflow:auto;");
			document.getElementById("status").setAttribute("draggable", "true");
			document.getElementById("status").style.display = "block"	;
			document.getElementById("status").style.cursor = "hand";
			document.getElementById('status').style.height=new_h + "px";
			document.getElementById('status').style.width=new_status_width + "px";
			
			document.getElementById("map").setAttribute("style","display:block;cursor:pointer;cursor:hand;");
			document.getElementById("map").setAttribute("draggable", "true");
			document.getElementById("map").style.display = "block"	;
			document.getElementById("map").style.cursor = "hand";		
			document.getElementById('map').style.width=new_w + "px";
			document.getElementById('map').style.height=new_h + "px";
			
			console.log("Size h x w: " + h + "x" + w +
				"; map size old: " + old_h + "x" + old_w + ", new: " + new_h + "x" + new_w +
				"; new status width: " + new_status_width);
			console.log("Map; h x w: " + document.getElementById('map').style.height + "x" + document.getElementById('map').style.width);	
			console.log("Status; h x w: " + document.getElementById('status').style.height + "x" + document.getElementById('status').style.width);	
		}
	}
}
	
/*
 * Function: 	generateUUID()
 * Parameters: 	None
 * Returns: 	RFC4122 version 4 compliant UUID
 * Description:	Generate a random UUID
 */
function generateUUID() { // Post by briguy37 on stackoverflow
    var d = new Date().getTime();
    if(window.performance && typeof window.performance.now === "function"){
        d += performance.now(); //use high-precision timer if available
    }
    var uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        var r = (d + Math.random()*16)%16 | 0;
        d = Math.floor(d/16);
        return (c=='x' ? r : (r&0x3|0x8)).toString(16);
    });
    return uuid;
}

/*
 * Function: 	fileSize()
 * Parameters: 	File size
 * Returns: 	Nicely formatted file size
 * Description:	Display file size nicely	
 */
function fileSize(file_size) {
	var niceFileSize;
	if (!file_size) {
		return undefined;
	}
	else if (file_size > 1024 * 1024 * 1024) {
		niceFileSize = (Math.round(file_size * 100 / (1024 * 1024 * 1024)) / 100).toString() + 'GB';
	}
	else if (file_size > 1024 * 1024) {
		niceFileSize = (Math.round(file_size * 100 / (1024 * 1024)) / 100).toString() + 'MB';
	} 
	else {
		niceFileSize = (Math.round(file_size * 100 / 1024) / 100).toString() + 'KB';
	}
	return niceFileSize;
}

/*
 * Function: 	isIE()
 * Parameters: 	None
 * Returns: 	Nothing
 * Description:	Test for IE nightmare 
 */
function isIE() {
	var myNav = navigator.userAgent.toLowerCase();
	return (myNav.indexOf('msie') != -1) ? parseInt(myNav.split('msie')[1]) : false;
}
	
/*
 * Function: 	setStatus()
 * Parameters: 	Status message, error object (optional), diagnostic (optional), alternate stack (optional)
 * Returns: 	Nothing
 * Description:	Set status. Optional error message raised as an exception to halt processing 
 */	
function setStatus(msg, err, diagnostic, stack) {
	if (document.getElementById("status").innerHTML != msg) {
		var end=new Date().getTime();
		var elapsed=(Math.round(end - start))/1000; // in S
		var errm
		if (err) {
			errm=err.message;
		}
		
		if (status == undefined) {
			if (document.getElementById("tabs") && document.getElementById("shapeFileStatus")) { // JQuery-UI version
				status=document.getElementById("shapeFileStatus");
			}
			else {
				status=document.getElementById("status");
			}
		}
		
		if (!errm) {
			status.innerHTML = msg;
			if (diagnostic) {
				document.getElementById("status").innerHTML = 
					document.getElementById("status").innerHTML + 
					"<p>Processing diagnostic:</br><pre>" + diagnostic + "</pre></p>";
			}
			if (document.getElementById("tabs") && tabs) { // JQuery-UI version
				tabs.tabs("refresh" );
			}
			console.log("[" + elapsed + "] " + msg);
		}
		else {
			status.innerHTML = "<h1>" + msg + "</h1><h2>Error message: " + errm + "</h2>";
			if (stack) {
				document.getElementById("status").innerHTML = 
					document.getElementById("status").innerHTML + 
					"<p>Stack:</br><pre>" + stack + "</pre></p>";
				console.log("[" + elapsed + "] Stack: " + stack);
			}
			else if (err && err.stack) {
				document.getElementById("status").innerHTML = 
					document.getElementById("status").innerHTML + 
					"<p>Stack:</br><pre>" + err.stack + "</pre></p>";
				console.log("[" + elapsed + "] err.Stack: " + stack);
			}
			if (diagnostic) {
				document.getElementById("status").innerHTML = 
					document.getElementById("status").innerHTML + 
					"<p>Processing diagnostic:</br><pre>" + diagnostic + "</pre></p>";
				console.log("[" + elapsed + "] Diagnostic: " + diagnostic);
			}
				
			if (document.getElementById("tabs") && tabs) { // JQuery-UI version
				tabs.tabs("refresh" );
			}		
			throw new Error("[" + elapsed + "] " + msg + "; " + errm);
		}
	}
}
		
/*
 * Function: 	createMap()
 * Parameters: 	Bounding box, number of Zoomlevels
 * Returns: 	map
 * Description:	Create map, add Openstreetmap basemap and scale
 */	
function createMap(boundingBox, noZoomlevels) {

	var end=new Date().getTime();
	var elapsed=(Math.round(end - start))/1000; // in S
									
	console.log("[" + elapsed + "] Create Leaflet map; h x w: " + document.getElementById('map').style.height + "x" + document.getElementById('map').style.width);	
	var map = new L.map('map' , {
			zoom: 11,
			// Tell the map to use a fullsreen control
			fullscreenControl: true
		} 
	);
	
	try {
		var loadingControl = L.Control.loading({
			separate: true
		});
		map.addControl(loadingControl);
	}
	catch (e) {
		try {
			map.remove();
		}
		catch (e2) {
			console.log("WARNING! Unable to remove map during error recovery");
		}
		throw new Error("Unable to add loading control to map: " + e.message);
	}
		
	try {
		map.fitBounds([
			[boundingBox.ymin, boundingBox.xmin],
			[boundingBox.ymax, boundingBox.xmax]], {maxZoom: 11}
		);
	}
	catch (e) {
		try {
			map.remove();
		}
		catch (e2) {
			console.log("WARNING! Unable to remove map during error recovery");
		}
		throw new Error("Unable to create map: " + e.message);
	}
	
	try {
		if (noZoomlevels > 0) {
			map.on('zoomend', function zoomendEvent(event) {
				changeJsonLayers(event);
			});
			/*
			map.eachLayer(function(layer) {
				if (!layer.on) return;
				layer.on({
					loading: function(event) { console.log("Loading: " + layer); },
					load: function(event) { console.log("Loaded: " + layer); }
				}, this);
			}); */
		}
		else {
			console.log("Zoomlevel based layer support disabled; only one zoomlevel of data present");
		}
	}
	catch (e) {
		try {
			map.remove();
		}
		catch (e2) {
			console.log("WARNING! Unable to remove map during error recovery");
		}
		throw new Error("Unable to add zoomend event to map: " +  e.message);
	}		
	
	try {
		end=new Date().getTime();
		elapsed=(Math.round(end - start))/1000; // in S		
		console.log("[" + elapsed + "] Creating basemap...");															
		tileLayer=L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token=pk.eyJ1IjoibWFwYm94IiwiYSI6ImNpandmbXliNDBjZWd2M2x6bDk3c2ZtOTkifQ._QA7i5Mpkd_m30IGElHziw', {
			maxZoom: 11,
			attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, ' +
				'<a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, ' +
				'Imagery &copy; <a href="http://mapbox.com">Mapbox</a>',
			id: 'mapbox.light'
		});
		tileLayer.addTo(map);	
		L.control.scale().addTo(map); // Add scale
		
		end=new Date().getTime();
		elapsed=(Math.round(end - start))/1000; // in S		
		console.log("[" + elapsed + "] Added tileLayer and scale to map");	
	
		return map;
	}
	catch (e) {
		try {
			map.remove();
		}
		catch (e2) {
			console.log("WARNING! Unable to remove map during error recovery");
		}		
		throw new Error("Unable to add tile layer to map: " + e.message);
	}
}

/*
 * Function: 	setupBoundingBox()
 * Parameters: 	Response JSON
 * Returns: 	Nothing
 * Description:	Setup bounding boxes - use response common bounding box (shapefiles only); then topoJSON; then whole world
 *				Map uses index 0 (first one!) 
 */
function setupBoundingBox(response) {
	for (var i=0; i < response.no_files; i++) {						
		if (!response.file_list[i]) {
			setStatus("Unable to setup bounding boxes", new Error("File [" + i + "/" + (response.no_files - 1) + "] is not defined"));
		}
		else if (!response.file_list[i].boundingBox) {
			if (response.file_list[i].topojson && 
				response.file_list[i].topojson[0] && 
				response.file_list[i].topojson[0].topojson && 
				response.file_list[i].topojson[0].topojson.objects && 
				response.file_list[i].topojson[0].topojson.objects.collection && 
				response.file_list[i].topojson[0].topojson.objects.collection.bbox) {
				console.log("File [" + i + "]: Using topojson bounding box");	
				
				response.file_list[i].boundingBox={
					xmin: response.file_list[i].topojson[0].topojson.objects.collection.bbox[0], 
					ymin: response.file_list[i].topojson[0].topojson.objects.collection.bbox[1], 
					xmax: response.file_list[i].topojson[0].topojson.objects.collection.bbox[2], 
					ymax: response.file_list[i].topojson[0].topojson.objects.collection.bbox[3]};										
			}
			else {
				console.log("WARNING! File [" + i + "/" + (response.no_files - 1) + "]: bounding box is not defined; using whole world as bounding box");								
				response.file_list[i].boundingBox={xmin: -180, ymin: -85, xmax: 180, ymax: 85};
			}
		}
	}	
}

/*
 * Function: 	setupLayers()
 * Parameters: 	Response JSON
 * Returns: 	layerAddOrder array
 * Description:	Setup layers and geolevels
 * 				Total areas not defined; deduce from topojson or geojson
 * 				Check bounding box
 * 				Sort geolevels by area
 *				Create sorted ngeolevels array for geolevel_id for re-order (if required)
 *				For geolevel 1 - Check that minimum resolution shapefile has only 1 area
 * 				Re-order by geolevel_id if required	; creating layerAddOrder array
 */
function setupLayers(response) {
	
	var layerAddOrder = [];			

/*
Add shapefiles starting from the highest resolution first:

Add data to JSONLayer[0]; shapefile [3]: SAHSU_GRD_Level4.shp
Add data to JSONLayer[1]; shapefile [2]: SAHSU_GRD_Level3.shp
Add data to JSONLayer[2]; shapefile [1]: SAHSU_GRD_Level2.shp
Add data to JSONLayer[3]; shapefile [0]: SAHSU_GRD_Level1.shp
*/
	var geolevels = [];
	var bbox=response.file_list[0].boundingBox;
	
	for (var i=0; i < response.no_files; i++) {	
		if (!response.file_list[i].total_areas) { // Total areas not defined; deduce from topojson or geojson
			if (response.file_list[i].topojson && 
				response.file_list[i].topojson[0] && 
				response.file_list[i].topojson[0].topojson && 
				response.file_list[i].topojson[0].topojson.objects && 
				response.file_list[i].topojson[0].topojson.objects.collection && 
				response.file_list[i].topojson[0].topojson.objects.collection.geometries) {
				response.file_list[i].total_areas=response.file_list[i].topojson[0].topojson.objects.collection.geometries.length;

			}
			else if (response.file_list[i].geojson && 
					 response.file_list[i].topojson[0].topojson &&
					 response.file_list[i].topojson[0].topojson.features) {
				response.file_list[i].total_areas=response.file_list[i].topojson[0].topojson.features.length;
			}		
			else {
				setStatus("Some total areas were not defined", new Error("Unable to deduce total areas for layer: " + i));
			}	
		}	
		geolevels[i] = { // Initialise geolevels[] array if required to create geolevels
			i: i,
			file_name: response.file_list[i].file_name,
			total_areas: response.file_list[i].total_areas,
			points:  response.file_list[i].points,
			geolevel_id: 0
		};				

		if (bbox[0] != response.file_list[i].boundingBox[0] &&
			bbox[1] != response.file_list[i].boundingBox[1] &&
			bbox[2] != response.file_list[i].boundingBox[2] &&
			bbox[3] != response.file_list[i].boundingBox[3]) { // Bounding box checks
			bbox_errors++;
			msg+="\nERROR: Bounding box " + i + ": [" +
				"xmin: " + response.file_list[i].boundingBox[0] + ", " +
				"ymin: " + response.file_list[i].boundingBox[1] + ", " +
				"xmax: " + response.file_list[i].boundingBox[2] + ", " +
				"ymax: " + response.file_list[i].boundingBox[3] + "];" +
				"\n is not the same as the first bounding box: " + 
				"xmin: " + response.file_list[i].boundingBox[0] + ", " +
				"ymin: " + response.file_list[i].boundingBox[1] + ", " +
				"xmax: " + response.file_list[i].boundingBox[2] + ", " +
				"ymax: " + response.file_list[i].boundingBox[3] + "];";
			console.error("\nERROR: Bounding box " + i + ": [" +
				"xmin: " + response.file_list[i].boundingBox[0] + ", " +
				"ymin: " + response.file_list[i].boundingBox[1] + ", " +
				"xmax: " + response.file_list[i].boundingBox[2] + ", " +
				"ymax: " + response.file_list[i].boundingBox[3] + "];" +
				"\n is not the same as the first bounding box: " + 
				"xmin: " + response.file_list[i].boundingBox[0] + ", " +
				"ymin: " + response.file_list[i].boundingBox[1] + ", " +
				"xmax: " + response.file_list[i].boundingBox[2] + ", " +
				"ymax: " + response.file_list[i].boundingBox[3] + "];");
		}						
	}
	
	var ngeolevels = geolevels.sort(function (a, b) { // Sort function: sort geolevels by area
		if (a.total_areas > b.total_areas) {
			return 1;
		}
		if (a.total_areas < b.total_areas) {
			return -1;
		}
		// a must be equal to b
		return 0;
	});
			
	for (var i=0; i < response.no_files; i++) {	// Create sorted ngeolevels array for geolevel_id for re-order (if required)		
		ngeolevels[i].geolevel_id=i+1;						
//							console.log("ngeolevels[" + i + "]: " + JSON.stringify(ngeolevels[i], null, 4));
		if (i == 0 && ngeolevels.length > 1 && ngeolevels[i].total_areas != 1) { // Geolevel 1 - Check that minimum resolution shapefile has only 1 area
			setStatus("Check that minimum resolution shapefile has only 1 area", 
				new Error("geolevel 1/" + ngeolevels.length + " shapefile: " + ngeolevels[i].file_name + " has >1 (" + ngeolevels[i].total_areas + ") area)"));
		}
	}
	
	for (var i=0; i < response.no_files; i++) {	// Re-order by geolevel_id if required	
		var j=ngeolevels[i].i;
		if (response.file_list[j].geolevel_id) { // Geolevel ID present in data
			console.log("File[" + j + "]: " + response.file_list[j].file_name +
				"; geolevel: " + response.file_list[j].geolevel_id +
				"; size: " + response.file_list[j].file_size +
				"; areas: " + response.file_list[j].total_areas);
		}
		else {
			response.file_list[j].geolevel_id = ngeolevels[i].geolevel_id;
			if (response.file_list[j].geolevel_id) {
				console.log("File[" + j + "]: " + response.file_list[j].file_name +
					"; deduced geolevel: " + response.file_list[j].geolevel_id +
					"; size: " + (response.file_list[j].file_size || "not defined") +
					"; areas: " +  response.file_list[j].total_areas);
			}
			else {
				setStatus("Geo level reorder", new Error("File[" + j + "]: " + response.file_list[j].file_name +
					"; deduced geolevel is undefined; ngeolevels[" + ij+ "]: " + JSON.stringify(ngeolevels[i], null, 4)));
			}
		}
	}
	
//					for (var i=0; i < response.no_files; i++) {	// Display now re-ordered geolevels array		
//							console.log("geolevels[" + i + "]: " + JSON.stringify(geolevels[i], null, 4));
//					}
	
	for (var i=0; i < response.no_files; i++) {	// Re-order by geolevel_id; creating layerAddOrder array		
		if (response.file_list[i].geolevel_id) {
			console.log("Re-order: layerAddOrder[" + (response.no_files-response.file_list[i].geolevel_id) + "]=" + i);
			layerAddOrder[(response.no_files-response.file_list[i].geolevel_id)]=i;	
		}
		else {
			setStatus("Geo level reorder", new Error("Geo level [" + i + "]; layerAddOrder[] response.no_files-response.file_list[i].geolevel_id is undefined"));
		}
	}
	if (layerAddOrder.length == 0) {
		setStatus("Geo level reorder", new Error("layerAddOrder[] array is zero sized; response.no_files: " + response.no_files));
	}
	else if (layerAddOrder.length != response.no_files) {
		setStatus("Geo level reorder", new Error("layerAddOrder[] array: " + layerAddOrder.length + "; response.no_files: " + response.no_files));
	}
	
	return layerAddOrder;
}
				
/*
 * Function: 	displayResponse()
 * Parameters: 	Response text (JSON as a string), status code, form name (textual)
 * Returns: 	Nothing
 * Description:	Display reponse from submit form
 */
function displayResponse(responseText, status, formName) {
										
	var response;
	var msg=""
	var end=new Date().getTime();
	serverTime=(Math.round(end - start - (uploadTime*1000), 2))/1000; // in S;
	
	setStatus("Processing response from server...");
	if (responseText != null && typeof responseText == 'object') { // Already JSON
		response=responseText;
	}
	else if (responseText == undefined) {
		setStatus("Send Failed, no response from server", new Error("no response from server"), "Status: " +
			JSON.stringify(status, null, 4));
	}
	else { // Parse it
		try {
			response=JSON.parse(responseText);
		} catch (e) {  
			if (responseText) {
				setStatus("Send Failed, error parsing response", e, responseText, e.stack);
			}
			else {		
				setStatus("Send Failed, no response from server", e, "Status: " +
						JSON.stringify(status, null, 4), e.stack);
			}
			return;
		}
	}
	
	if (response.error) { // Should be handled by on-error hander
		setStatus("Status: " + status + "; unexpected error message, in JSON", new(response.error), response.diagnostic);
	}

	if (!response.no_files) {
		setStatus("Error in processing file list", new Error("no files returned"));
	}
	else {
		msg+="<p>Files processed: " + response.no_files;
		if (!response.file_list) {	
			setStatus("Error in processing file list", new Error("no file list"));
		}
		else {
			if (response.no_files == 0) {
				setStatus("Error in processing file list", new Error("response.no_files == 0"));
			}			
			else {
				if (!response.file_list[0]) {
					setStatus("Error in processing file list", new Error("First file in list (size: " + response.no_files + ") is not defined"));
				}
				else if (!response.file_list[0].file_name) {
					setStatus("Error in processing file list", new Error("File nane of first file in list (size: " + response.no_files + ") is not defined"));
				}	
				else {	
					setupBoundingBox(response);
					var noZoomlevels;
					if (response.file_list[0] && response.file_list[0].topojson[0]) {
						noZoomlevels=response.file_list[0].topojson.length;
					}
					else {
						setStatus("Error in map setup", new Error("Unable to determine the number of zoomlevels"));
					}
					
					var layerColours = [ // Some advice needed here!!!
						"#ff0000", // Red
						"#0000ff", // Blue
						"#00ff00", // Green
						"#ff00c8", // Magenta
						"#ffb700", // Orange
						"#f0f0f0", // Light grey
						"#0f0f0f"  // Onyx (nearly black)
						];
					var layerAddOrder = setupLayers(response);
							
					for (var i=0; i < response.no_files; i++) {	// Now process the files					
						var weight=(response.no_files - i); // i.e. Lower numbers - high resolution have most weight;	
						
	// Chroma.js - to be added (Node module)
	//					var colorScale = chroma  
	//						.scale(['#D5E3FF', '#003171'])
	//						.domain([0,1]);	
	
						var opacity;
						var fillOpacity;
						
						if (i > 0) { // All but the first are transparent
							opacity=0;		
							fillOpacity=0;
						}
						else { // First - i.e. max geolevel
							opacity=1;
							fillOpacity=0.4;				
						}
						
						if (!layerColours[i] || i == (response.no_files - 1)) { // if >7 layers or lowest resolution (last layer) - make it black
							layerColours[i]="#000000"; // Black
							weight=response.no_files;
						}
				
						if (!response.file_list[layerAddOrder[i]] || !response.file_list[layerAddOrder[i]].file_name) {
							setStatus("Geo level reorder", new Error("layerAddOrder problem: Adding data to JSONLayer[" + i + "]; layerAddOrder[" + layerAddOrder[i] + "]: NO FILE"));
						}
						else {
							if (response.file_list[layerAddOrder[i]].topojson && response.file_list[layerAddOrder[i]].topojson[0].topojson) {			
					
								var topojsonZoomlevels = {};
								for (var k=0; k< response.file_list[layerAddOrder[i]].topojson.length; k++) {
									topojsonZoomlevels[response.file_list[layerAddOrder[i]].topojson[k].zoomlevel] = response.file_list[layerAddOrder[i]].topojson[k].topojson;
								}
					
								jsonAddLayerParamsArray[(response.no_files - i - 1)]={ // jsonAddLayer() parameters
									i: i,	
									no_files: (response.no_files - 1),
									file_name: response.file_list[layerAddOrder[i]].file_name,		
									areas: response.file_list[layerAddOrder[i]].total_areas,									
									layerAddOrder: layerAddOrder[i],		
									style: 
										{color: 	layerColours[i],
										 fillColor: "#ccf4ff",
										 weight: 	weight, // i.e. Lower numbers have most weight
										 opacity: 	1,
										 fillOpacity: fillOpacity
									},
									JSONLayer: JSONLayer, 
									jsonZoomlevel: true,		/* json key supports multi zoomlevels */
									json: topojsonZoomlevels,
									isGeoJSON: false /* isGeoJSON */
								};
							}
							else if (response.file_list[layerAddOrder[i]].geojson) {			
										
								jsonAddLayerParamsArray[(response.no_files - i - 1)]={ // jsonAddLayer() parameters
									i: i,
									no_files: (response.no_files - 1),
									file_name: response.file_list[layerAddOrder[i]].file_name,		
									areas: response.file_list[layerAddOrder[i]].total_areas,							
									layerAddOrder: layerAddOrder[i],		
									style: 
										{color: 	layerColours[i],
										 fillColor: "#ccf4ff",
										 weight: 	weight, // i.e. Lower numbers have most weight
										 opacity: 	1,
										 fillOpacity: fillOpacity
									}, 
									JSONLayer: JSONLayer, 
									jsonZoomlevel: true,		/* json key supports multi zoomlevels */
									json: response.file_list[layerAddOrder[i]].geojson,
									isGeoJSON: true /* isGeoJSON */
								};												
							}
							else {
								setStatus("Add data to JSONLayer[" + i + "/" + (response.no_files - 1) + "]", new Error("ERROR! no GeoJSON/topoJSON returned"));
							}						
						}
					} // end of for loop

					msg+=createTable(response, layerColours, layerAddOrder);

					setTimeout(
						function createMapAsync() {
													
							if (document.getElementById("tabs")) { // JQuery-UI version	
								$( "#tabs" ).tabs( "option", "active", 1 ); // Activate panel 1 (map) so leaflet works (and the user can see the map!)
							}
							
							if (!map) {
								map=createMap(response.file_list[0].boundingBox, noZoomlevels); // Create map using first bounding box in file list
							}
							else {
								var centre=map.getCenter();
	
								console.log("Centre: " + centre.lat + ", " + centre.lng);
		//						if (+centre.lat.toFixed(4) == +y_avg.toFixed(4) && 
		//							+centre.lng.toFixed(4) == +x_avg.toFixed(4)) {
		//							console.log("Map centre has not changed");
		//						}
		//						else {
									map.eachLayer(function (layer) {
										console.log('Remove tileLayer');
										map.removeLayer(layer);
									});
									console.log('Remove map');
									map.remove(); // Known leaflet bug:
												  // Failed to execute 'removeChild' on 'Node': The node to be removed is not a child of this node.
									
									map=createMap(response.file_list[0].boundingBox, noZoomlevels);				
		//						}	
							}

							addLegend(map, response, layerColours, layerAddOrder);							
							changeJsonLayers();

						}, // End of createMapAsync()
						100);
						
				} // response.file_list[0] exists
				
			} // response.no_files > 0
			
			if (response.file_list[0]) {
				if (response.file_list[0].srid) {
					console.log("SRID: " + response.file_list[0].srid);
				}	
					
				if (response.file_list[0].projection_name) {
					console.log("Projection name: " + response.file_list[0].projection_name);
				}	
				
				if (response.file_list[0].boundingBox) {
					console.log("Bounding box [" +
								"xmin: " + response.file_list[0].boundingBox.xmin + ", " +
								"ymin: " + response.file_list[0].boundingBox.ymin + ", " +
								"xmax: " + response.file_list[0].boundingBox.xmax + ", " +
								"ymax: " + response.file_list[0].boundingBox.ymax + "]");
				}	
			}			
		}
	}
	msg+="</p>";

	if (response.fields) {
		var fieldCount=0;
		for (var field in response.fields) {
			fieldCount++;
			if (fieldCount == 1) {
				msg+="<p>Fields returned by nodeGeoSpatialServices<ul id=\"fields\">";
			}
			msg+="<li>" + field + "=" + response.fields[field] + "</li>";
		}
		if (fieldCount > 0) {
			msg+="</ul></p>";
		}
	}	
	
	var end=new Date().getTime();
	var elapsed=(Math.round(end - start, 2))/1000; // in S
	msg+="Time taken: " + elapsed + " S; upload time: " + uploadTime + " S; server time: " + serverTime + " S [excluding map draw time]</br>";
	
	if (response.message) {
		msg+="<p>Processing diagnostic messages:</br><div id=\"div_message\" style=\"overflow:scroll; height: 400px;\"><pre>" + response.message + "</pre></div?</p>"
	}
	
	if (response.diagnostic) {
		msg+="<p>Processing diagnostic:</br><pre>" + response.diagnostic + "</pre></p>";
	}	
	
	if (status == 200) {	
		setStatus("<h1>" + formName + " processed OK</h1>", undefined, msg);
	}	
	else {
		setStatus("Send Failed", new Error("Unexpected http status: " + status), "Message:" + msg);
	}
}	
											
/*
 * Function: 	jsonAddLayer()
 * Parameters: 	jsonAddLayerParams object, keys: { 
 *					index (into JSONLayer array), 
 *					no_files,
 *					file name,							
 *					layerAddOrder array,
 *					layer style, 
 *					JSONLayer array, 
 *					jsonZoomlevel - json key supports multi zoomlevels,
 *					json - geo/topojson object, 
 *					isGeoJSON boolean }, 
 *					JSONLayer array, async callback, initialRun (boolean)
 * Returns: 	Nothing
 * Description:	Remove then add geo/topoJSON layer to map
 */	
function jsonAddLayer(jsonAddLayerParams, JSONLayer, callback, initialRun) { 

	try {
//		if (initialRun) { // Removed because of suppression
			scopeChecker({
				jsonAddLayerParams: jsonAddLayerParams,
				JSONLayer: JSONLayer,
				callback: callback,
				start: start,
				map: map
			});
/*		}
		else {
			scopeChecker({
				jsonAddLayerParams: jsonAddLayerParams,
				JSONLayer: JSONLayer,
				callback: callback,
				start: start,
				map: map,
				JSONLayerElement: JSONLayer[jsonAddLayerParams.i],
				AddParamsJSONLayerElement: jsonAddLayerParams.JSONLayer[jsonAddLayerParams.i]
			});
		} */
	}
	catch (e) {
		console.log("jsonAddLayer() scopeChecker ERROR: " + e.message);
		callback(e);
		return;
	}
	
	var end=new Date().getTime();
	var elapsed=(Math.round(end - start))/1000; // in S
	var verb="Modifying data in";
	var msg;
	
	// Suppress for performance
	if (jsonAddLayerParams.areas > 200 && map.getZoom() < 6) {
		console.log("[" + elapsed + "] " + "Suppressing (jsonAddLayerParams.areas > 200 && map.getZoom() < 6) JSONLayer[" + jsonAddLayerParams.i + "/" + jsonAddLayerParams.no_files + 
			"]; file layer [" + jsonAddLayerParams.layerAddOrder + "]: " +
			jsonAddLayerParams.file_name + "; areas: " + jsonAddLayerParams.areas + "; zoomlevel: " +  map.getZoom());
		return;
	}
	else if (jsonAddLayerParams.areas > 10000) {
		console.log("[" + elapsed + "] " + "Suppressing (jsonAddLayerParams.areas > 10,000) JSONLayer[" + jsonAddLayerParams.i + "/" + jsonAddLayerParams.no_files + 
			"]; file layer [" + jsonAddLayerParams.layerAddOrder + "]: " +
			jsonAddLayerParams.file_name + "; areas: " + jsonAddLayerParams.areas + "; zoomlevel: " +  map.getZoom());
		return;
	}
		
	if (initialRun) {
		verb="Adding data to";
	}
	console.log("[" + elapsed + "] " + verb + " JSONLayer[" + jsonAddLayerParams.i + "/" + jsonAddLayerParams.no_files + 
		"]; file layer [" + jsonAddLayerParams.layerAddOrder + "]: " +
		jsonAddLayerParams.file_name +
		"; colour: " + jsonAddLayerParams.style.color + "; weight: " + jsonAddLayerParams.style.weight + 
		"; opacity: " + jsonAddLayerParams.style.opacity + "; fillOpacity: " + jsonAddLayerParams.style.fillOpacity + "; zoomlevel: " +  map.getZoom());
							
	try {
		if (jsonAddLayerParams.JSONLayer[jsonAddLayerParams.i]) {	
			console.log("[" + elapsed + "] Removing topoJSONLayer data: " + jsonAddLayerParams.i);
			jsonAddLayerParams.JSONLayer[jsonAddLayerParams.i].clearLayers();
			console.log("[" + elapsed + "] Removed topoJSONLayer data: " + jsonAddLayerParams.i);
		}
	}
	catch (e) {
		end=new Date().getTime();
		elapsed=(Math.round(end - start))/1000; // in S
		
		msg="[" + elapsed + "] Error removing JSON layer [" + jsonAddLayerParams.i  + "/" + jsonAddLayerParams.no_files + "]  map: " + e.message;
		console.log("jsonAddLayer() ERROR: " + msg);
		callback(new Error(msg));
		return;
	}			
		
	try {	
		if (jsonAddLayerParams.JSONLayer[jsonAddLayerParams.i] == undefined) {
			if (jsonAddLayerParams.isGeoJSON) { // Use the right function
				JSONLayer[jsonAddLayerParams.i] = L.geoJson(undefined /* Geojson options */, 
					jsonAddLayerParams.style).addTo(map);
			}
			else {
				JSONLayer[jsonAddLayerParams.i] = new L.topoJson(undefined /* Topojson options */, 
					jsonAddLayerParams.style).addTo(map);					
			}
			jsonAddLayerParams.JSONLayer[jsonAddLayerParams.i] = JSONLayer[jsonAddLayerParams.i];
		}
		
		var json=jsonZoomlevelData(jsonAddLayerParams.json, map.getZoom(), jsonAddLayerParams.i);
		if (jsonAddLayerParams.json) {
			if (jsonAddLayerParams.jsonZoomlevel) {
				jsonAddLayerParams.JSONLayer[jsonAddLayerParams.i].addData(json);	
			}
			else {
				jsonAddLayerParams.JSONLayer[jsonAddLayerParams.i].addData(jsonAddLayerParams.json);		
			}				
	
			for (var j=0; j<=jsonAddLayerParams.no_files; j++) {
				if (JSONLayer[j]  && j != jsonAddLayerParams.i ) {
					console.log("Map layer: " + jsonAddLayerParams.i + "; bring layer: " + j + " to front");
					JSONLayer[j].bringToFront();
				}
			}
		
			map.whenReady(function jsonAddLayerReady() { 
					end=new Date().getTime();
					elapsed=(Math.round(end - start))/1000; // in S
					console.log("[" + elapsed + "] Added JSONLayer [" + jsonAddLayerParams.i  + "/" + jsonAddLayerParams.no_files + 
						"]: " + jsonAddLayerParams.file_name + "; zoomlevel: " +  map.getZoom() /* + "; size: " + sizeof(json) SLOW!!! */);
			//		console.log("Callback: " + jsonAddLayerParams.i);
					callback();
				}, this); 
		}
		else {
			msg="jsonAddLayer(): jsonAddLayerParams.json is not defined.";
			console.log("jsonAddLayer() ERROR: " + msg);
			callback(new Error(msg));
		}	
	}
	catch (e) {
		end=new Date().getTime();
		elapsed=(Math.round(end - start))/1000; // in S
		
		msg="[" + elapsed + "] Error adding JSON layer [" + jsonAddLayerParams.i  + "/" + jsonAddLayerParams.no_files + "] to map: " + e.message;
		console.log("jsonAddLayer() ERROR: " + msg);
		callback(new Error(msg));
	}			
		
} // End of jsonAddLayer()

/*
 * Function: 	jsonZoomlevelData()
 * Parameters:  Json zoom level object:  { <numeric zoomlevel>: <json>, ... }, map zoomlevel, layer number
 * Returns: 	[Topo]json object
 * Description: Get JSON data for zoomlevel using the best key
 */
function jsonZoomlevelData(jsonZoomlevels, mapZoomlevel, layerNum) {

	var json;
	var firstKey;
	var maxZoomlevel;
	var minZoomlevel;

	if (jsonZoomlevels) {	
		for (var key in jsonZoomlevels) {
			if (firstKey == undefined) { // Save first key so there is one good match!
				firstKey=parseInt(key);
			}
			
			if (minZoomlevel == undefined) {
				minZoomlevel=parseInt(key);
			}
			else if (key < minZoomlevel) {
				minZoomlevel=parseInt(key);
			}
			if (maxZoomlevel == undefined) {
				maxZoomlevel=parseInt(key);
			}
			else if (key > maxZoomlevel) {
				maxZoomlevel=parseInt(key);
			}		
			
			if (key == mapZoomlevel) {
				json=jsonZoomlevels[key];
				break;
			}
			else {
				console.log("Layer [" + layerNum + "]: key: " + key + "; no match for zoomlevel: " + mapZoomlevel + 
					"; maxZoomlevel key: " + maxZoomlevel + "; minZoomlevel key: " + minZoomlevel);
			}
		}
		
		if (json) {
			console.log("Layer [" + layerNum + "]: json match for zoomlevel: " + mapZoomlevel + 
				"; maxZoomlevel key: " + maxZoomlevel + "; minZoomlevel key: " + minZoomlevel);
			return json;
		}
		// no json found for zoomlevel: 3; mapZoomlevel < minZoomlevel; using minZoomlevel key: 10
		if (json == undefined && mapZoomlevel > maxZoomlevel) {
			console.log("Layer [" + layerNum + "]: no json found for zoomlevel: " + mapZoomlevel + "; using maxZoomlevel key: " + maxZoomlevel);
			json=jsonZoomlevels[maxZoomlevel];
		}	
		
		if (json == undefined && mapZoomlevel < minZoomlevel) {
			console.log("Layer [" + layerNum + "]: no json found for zoomlevel: " + mapZoomlevel + "; mapZoomlevel < minZoomlevel; using minZoomlevel key: " + minZoomlevel);
			json=jsonZoomlevels[minZoomlevel];
		}
		
		if (json == undefined && minZoomlevel) {
			console.log("Layer [" + layerNum + "]: no json found for zoomlevel: " + mapZoomlevel + "; using minZoomlevel key: " + minZoomlevel);
			json=jsonZoomlevels[minZoomlevel];
		}
		
		if (json == undefined && firstKey) {
			console.log("Layer [" + layerNum + "]: no json found for zoomlevel: " + mapZoomlevel + "; using first key: " + firstKey);
			json=jsonZoomlevels[firstKey];
		}
		
		if (json == undefined) {
			console.log("Layer [" + layerNum + "]: no json found for zoomlevel: " + mapZoomlevel + "; using default zoomlevel: 11");
			json=jsonZoomlevels["11"];
		}
		
		if (json == undefined) {
			throw new Error("jsonZoomlevelData(): Layer [" + layerNum + "]: no json available");
		}		
		else if (json && Object.keys(json).length == 0) {
			throw new Error("jsonZoomlevelData(): Layer [" + layerNum + "]: json has no keys");
		}
		return json;
	}
	else {
		throw new Error("jsonZoomlevelData(): Layer [" + layerNum + "]: jsonZoomlevels is not defined");
	}

} // End of jsonZoomlevelData()
	
/*
 * Function: 	errorHandler()
 * Parameters:  JQuery form error object
 * Returns: 	Nothing
 * Description: Error handler
 */	
function errorHandler(error, ajaxOptions, thrownError) {
	
	if (error) {
		console.error("error: " + JSON.stringify(error, null, 4));
		if (ajaxOptions) {
			console.error("ajaxOptions: " + JSON.stringify(ajaxOptions, null, 4));
		}
		if (thrownError) {
			console.error("thrownError: " + JSON.stringify(thrownError, null, 4));
		}
		var msg="<h1>Send Failed; http status: ";
		
		if (error.status == undefined && thrownError) {
			msg = "errorHandler() Ajax exception from JQuery Form: " + thrownError.message + "\nStack:\n" + (errorHandler.stack || ("No stack)"));
			document.getElementById("status").innerHTML = "<h1>Send Failed;</h1>" + msg;
			if (ajaxOptions) {
				console.log("errorHandler(): ajaxOptions: " + JSON.stringify(ajaxOptions, null, 4));
			}
			console.error(msg);
		}
		else if (error.status) {
			msg+=error.status;
		}
		else if (thrownError) {
			msg+="errorHandler() Ajax exception from JQuery Form: " + thrownError.message + "\nStack:\n" + (errorHandler.stack || ("No stack)"));
		}	
		else {
			msg+="(no error status or thrownError returned - likely server failure/firefox bug)";
		}
		msg+="</h1>";
		if (error.responseJSON && error.responseJSON.error) {
			msg+="</br>Error text: " + error.responseJSON.error;
		}
		else {
			console.log("No error text in JSON response");
		}		
		msg+="</br>Message:";
		if (error.responseJSON && error.responseJSON.message) {
			msg+=error.responseJSON.message;
		}
		else {
			msg+="(no error message)";
		}
		if (error.responseJSON && error.responseJSON.diagnostic) {
			msg+="<p>Processing diagnostic:</br><pre>" + error.responseJSON.diagnostic + "</pre></p>";
		}	
		
		document.getElementById("status").innerHTML = msg;
	}
	else if (thrownError) {
		msg = "errorHandler() Ajax exception from JQuery Form: " + thrownError.message + "\nStack:\n" + (errorHandler.stack || ("No stack)"));
		document.getElementById("status").innerHTML = "<h1>Send Failed;</h1>" + msg;
		if (ajaxOptions) {
			console.log("errorHandler(): ajaxOptions: " + JSON.stringify(ajaxOptions, null, 4));
		}
		console.error(msg);
	}
	else {
		console.error("errorHandler(): No error or thrownError returned");
	}
	
	if (map) {
		map.eachLayer(function (layer) {
			console.log('Remove tileLayer');
			map.removeLayer(layer);
		});
		console.log('Remove map');
		map.remove(); // Known leaflet bug:
					  // Failed to execute 'removeChild' on 'Node': The node to be removed is not a child of this node.
		map = undefined;
		document.getElementById("map").innerHTML = "";			 
		console.log('Remove map element'); 
	}										  
} // End of errorHandler()

/*
 * Function: 	uploadProgressHandler()
 * Parameters:  event, position, total, percentComplete
 * Returns: 	Nothing
 * Description:	Upload progress handler for JQuery form
 */
function uploadProgressHandler(event, position, total, percentComplete) {
	var msg;
		
	if (percentComplete == 100 && position == total) {
		var end=new Date().getTime();
		uploadTime=(Math.round(end - start, 2))/1000; // in S
		msg="Uploaded files: " + percentComplete.toString() + '%; ' + fileSize(position) + "; took: " + uploadTime + " S; sending request to server and awaiting response";
	}
	else {
		msg="Uploading files: " + percentComplete.toString() + '%; ' + fileSize(position) + "/" + fileSize(total);
	}
	if (document.getElementById("tabs")) { // JQuery-UI version
		progressbar.progressbar("value", percentComplete);
		if (percentComplete == 100 && position == total) {
			progressLabel.text( "Upload complete; awaiting server response");
		}
		document.getElementById("shapeFileStatus").innerHTML = msg;
	}
	else {
		document.getElementById("status").innerHTML = msg;
	}
	console.log(msg);
}

/*
 * Function: 	changeJsonLayers()
 * Parameters:  Event object
 * Returns: 	Nothing
 * Description:	Change all map layers to optimised topoJSON for that zoomlevel 
 */
function changeJsonLayers(event) {
	
	map.whenReady( // Basemap is ready
		function whenMapIsReady() { 

			try {
				scopeChecker({
					start: start
				});
			}
			catch(e) {
				console.error("whenMapIsReady(): caught: " + e.message + "\nStack: " + e.stack);
				return;
			}
							
			var end=new Date().getTime();
			var elapsed=(Math.round(end - start))/1000; // in S
			var initialRun;
			
			if (event) {
				console.log("[" + elapsed + "] New zoomlevel: " +  map.getZoom() + "; event: " + event.type);	
				initialRun=false;
			}
			else {
				console.log("[" + elapsed + "] Initial zoomlevel: " +  map.getZoom());	
				initialRun=true;
			}			
			
			setTimeout(	// Make async	
				function asyncEachSeries() { // Process map layers using async in order
					try {
						scopeChecker({
							jsonAddLayerParamsArray: jsonAddLayerParamsArray
						});
					}
					catch(e) {
						console.error("asyncEachSeries(): caught: " + e.message);
						return;
					}
			
					async.eachSeries(jsonAddLayerParamsArray, 
						function asyncEachSeriesHandler(item, callback) {	

							function asyncEachSeriesCallback(e) {
								callback(e);
							}											
								
							try {
								scopeChecker({
									item: item,
									callback: callback,
									JSONLayer: JSONLayer,
									callback: callback
								});
							}
							catch(e) {
								asyncEachSeriesCallback(e);
							}

							setTimeout(jsonAddLayer, 200, item, JSONLayer, asyncEachSeriesCallback, initialRun); 
								// Put a slight delay in for Leaflet to allow the map to redraw
						}, 
						function asyncEachSeriesError(err) {
							
							try {
								scopeChecker({
									start: start,
									jsonAddLayerParamsArray: jsonAddLayerParamsArray,
									no_files: jsonAddLayerParamsArray[0].no_files
								});
							
								var end=new Date().getTime();
								var elapsed=(Math.round(end - start))/1000; // in S
								if (err) {
									console.error("[" + elapsed + "] asyncEachErrorHandler: " + err.message + "\nStack: " + err.stack);								
								}
								else {
									console.log("[" + elapsed + "] " + jsonAddLayerParamsArray.length + " layers processed OK.");
								}
							}
							catch(e) {
								console.error("asyncEachSeriesError(): caught: " + e.message + "\nStack: " + e.stack);
							}
						} // End of asyncEachSeriesError()
					);						
				}, // End of asyncEachSeries()
				100);									
		} // End of whenMapIsReady()
	);
} // End of changeJsonLayers()

/*
 * Function: 	createTable()
 * Parameters: 	Response JSON, layerColours array, layerAddOrder array
 * Returns: 	Table as HTML
 * Description:	Create results table 
 */
function createTable(response, layerColours, layerAddOrder) {
	
	var msg="<table border=\"1\" style=\"width:100%\">" + 
		"<tr>" +
		"<th>File</th>" + 
		"<th>Size</th>" +
		"<th>Level " + response.file_list[layerAddOrder[0]].topojson[0].zoomlevel + " topo/geo<br>JSON length</th>" +
		"<th>Total<br>topojson length</th>" + 
		"<th>Areas</th>" + 
		"<th>Geo<br>level</th>" +
		"</tr>";	
	for (var i=0; i < response.no_files; i++) {	
		msg+="<tr style=\"color:" + layerColours[i] + "\"><td>" + response.file_list[layerAddOrder[i]].file_name + "</td>" +
				"<td>" + (fileSize(response.file_list[layerAddOrder[i]].file_size) || "N/A") + "</td>";
		if (response.file_list[layerAddOrder[i]].topojson && response.file_list[layerAddOrder[i]].topojson[0].topojson_length) {	
			var pct=Math.round((100*response.file_list[layerAddOrder[i]].topojson[0].topojson_length)/
									response.file_list[layerAddOrder[i]].topojson[0].geojson_length);
			msg+="<td>" + (fileSize(response.file_list[layerAddOrder[i]].topojson[0].topojson_length) || "N/A ") + "/" + 
				(fileSize(response.file_list[layerAddOrder[i]].topojson[0].geojson_length) || " N/A") + "; " + pct + "%</td>";	
		}
		else if (response.file_list[layerAddOrder[i]].geojson) {	
			msg+="<td>" + fileSize(response.file_list[layerAddOrder[i]].geojson_length) + "</td>";							
		}	
		if (response.file_list[layerAddOrder[i]].total_topojson_length) {
			msg+="<td>" + fileSize(response.file_list[layerAddOrder[i]].total_topojson_length) + "</td>";
		}
		else {
			msg+="<td>N/A</td>";
		}
		msg+="<td>" + response.file_list[layerAddOrder[i]].total_areas + "</td>" +
			"<td>" + (response.file_list[layerAddOrder[i]].geolevel_id || "N/A") + "</td>" + 
			"</tr>";								
	}		
	msg+="</table>";	

	return msg;
}	

/*
 * Function: 	addLegend()
 * Parameters: 	Map, response JSON, layerColours array, layerAddOrder array
 * Returns: 	HTML div object
 * Description:	Add legend to map 
 */
 function addLegend(map, response, layerColours, layerAddOrder) {
	
	scopeChecker({
		response: response,
		layerColours: layerColours,
		layerAddOrder: layerAddOrder,
		L: L,
		map: map
	});

	var legend = L.control({position: 'bottomright'});
			
	legend.onAdd = function onAddLEgend(map) {
		var div = L.DomUtil.create('div', 'info legend');
		var labels=[];

		for (var i = 0; i < response.no_files; i++) {
			labels.push(
				'<i style="background:' +layerColours[i] + '"></i>' + response.file_list[layerAddOrder[i]].total_areas);
		}

		div.innerHTML = '<i style="background:#FFF"></i><em>Areas</em><br>' + labels.join('<br>');

		return div;
	};

	legend.addTo(map);
}
