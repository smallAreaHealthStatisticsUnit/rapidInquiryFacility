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
var uuidV1;
var calls=0;
var jqXHRgetShpConvertStatus=undefined;
	
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
 * Function: 	consoleLog()
 * Parameters:  Message
 * Returns: 	Nothing
 * Description:	IE safe console log 
 */
function consoleLog(msg) {
	if (window.console && console && console.log && typeof console.log == "function") {
		if (isIE()) {
			if (window.__IE_DEVTOOLBAR_CONSOLE_COMMAND_LINE) {
				console.log(msg);
			}
		}
		else {
			console.log(msg);
		}
	}  
}

/*
 * Function: 	consoleError()
 * Parameters:  Message
 * Returns: 	Nothing
 * Description:	IE safe console error 
 */
function consoleError(msg) {
	if (window.console && console && console.error && typeof console.error == "function") {
		if (isIE()) {
			if (window.__IE_DEVTOOLBAR_CONSOLE_COMMAND_LINE) {	
				console.error(msg);
			}
		}
		else {
			console.error(msg);
		}
	}
}

/*
 * Function: 	addDbfDescFields()
 * Parameters:  file list object (processed files from shpConvertInput(), form data object
 * Returns: 	Nothing
 * Description:	Process file list object (processed files from shpConvertInput() to add descriptive fields to formdata
 */
function addDbfDescFields(fileList, formData) {
			
	for (var key in fileList) { // Add extended attributes XML doc
		for (var j=0; j < fileList[key].dbfHeader.fields.length ; j++) {
			var field=fileList[key].dbfHeader.fields[j].name.toUpperCase();
			var fieldKey=key + "_" + field;
			var fieldKey2=key + "_" + field.toUpperCase();
			var value=fileList[key].dbfHeader.fields[j].description;
			var afield="XML: " + field;
			if (value == undefined || value == "") {
				if (geoDataLoaderParameters[fieldKey]) {
					value=geoDataLoaderParameters[fieldKey];
					afield="Params: " + fieldKey;
				}
				else if (geoDataLoaderParameters[fieldKey2]) {
					value=geoDataLoaderParameters[fieldKey2];
					afield="Params2: " + fieldKey2;
				}
				else {
					value="Default: " + field.toLowerCase();
					afield="Default";
				}
			}
			consoleLog("addDbfDescFields() key: " + key + "; field: " + j + "[" + afield + "] set: " + fieldKey + '="' + value + '"');
			formData.append(fieldKey, value);
		}
	}
	
//	consoleLog("DBF file field descriptors: " + JSON.stringify(fields, null, 4));
}
						
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
			consoleError("FATAL: processJson(): data undefined: " + formName);
		}

		try {
			if (status == "success") {
				displayResponse(data, 200, formName, true /* enableGetStatus */);
			}
			else {
				displayResponse(data, status, formName, true /* enableGetStatus */);
			}
		}
		catch (e) {
			document.getElementById("status").innerHTML = "<h1>FATAL: Caught exception in displayResponse()</h1><h2>Error message: " + e.message + "</h2><pre>" + e.stack + "</pre>";
			consoleError("FATAL: Caught exception in displayResponse(): " + e.message + "\n" + e.stack);	
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
        timeout:     180000,    // mS - 10 minutes
		cache:		false		// Do not cache (helps IE)
		
    }; 

//	if (navigator.userAgent.toLowerCase().indexOf('firefox') > -1){
//		options.error=undefined;
//		options.iframe=true;		// This was a potential fix a spurious error 
									// Message:{"readyState":0,"responseText":"","status":0,"statusText":"error"} issues with firefox and ajax
									// Only. Has no effect other than no upload status
//	}
	
	var fileList;
	try {
		files_elem=document.getElementById('files'); // Set up file checker
		var files = files_elem.files;
	
		function shpConvertInputHandler() {
			
			shpConvertInput(this.files, function shpConvertInputCallback(err) {
				if (err) {
					consoleError(err.message);
				}
				else {
					fileList=getFileList();
					consoleLog("shpConvertInput() event files: " + JSON.stringify(fileList, null, 4));
				}			
			});
		}		
		files_elem.addEventListener("change", shpConvertInputHandler, false);
		
		if (files.length > 0) { // Already set
			consoleLog("shpConvertInput() already set");
			setTimeout(function shpConvertInputAsync() {
				document.getElementById("status").innerHTML = document.getElementById("status").innerHTML + "<br>" + 
					"Please wait for " + files.length + " file(s) to load";
				shpConvertInput(files, function shpConvertInputCallback2(err) {
					if (err) {
						consoleError(err.message);
					}
					else {
						fileList=getFileList();
						consoleLog("shpConvertInput() previously set files: " + JSON.stringify(fileList, null, 4));
					}
				});	
				}, 500);
		}
		
		if (navigator.userAgent.toLowerCase().indexOf('firefox') > -1){
			document.getElementById(formId + "Submit").addEventListener("click", function xMLHttpRequestSubmitForm(event) {
				consoleLog("Event: " + formId + "Submit");
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
		consoleError(document.getElementById("status").innerHTML);
	}
	
	if (document.getElementById("progressbar")) { // JQuery-UI version
		initHtml="";
	}
	else {
		document.getElementById("status").innerHTML = document.getElementById("status").innerHTML + "<br>" + formName + " form ready.";
		initHtml=document.getElementById("status").innerHTML;
	}
	
	if (document.getElementById("tabs") && tabs) { // JQuery-UI version
		tabs.tabs("refresh" );
	}
			
	consoleLog("Ready: " + formId);
} 	

/*
 * Function: 	errorPopup()
 * Parameters: 	Message, extended message
 * Returns: 	Nothing
 * Description:	Error message popup
 */
function errorPopup(msg, extendedMessage) {
	if (document.getElementById("tabs") && tabs && document.getElementById("error")) { // JQuery-UI version
		document.getElementById("error").innerHTML = "<h3>" + msg + "</h3>";
		var errorWidth=document.getElementById('tabbox').offsetWidth-300;
		var dialogObject={
			modal: true,
			width: errorWidth,
			closeText: "",
			dialogClass: "no-close",
			buttons: [ {
				text: "OK",
				click: function() {
					$( this ).dialog( "close" );
				}
			}]
		};
		
		if (extendedMessage) {
			consoleLog("extended message: " + extendedMessage);
			dialogObject.buttons.push({
				text: "Extended Info",
				click: function() {
					$( this ).dialog( "close" );
					errorPopup(extendedMessage);
				}
			});
		}
		
		$( "#error" ).dialog(dialogObject);
	}	
	else {	
		document.getElementById("status").innerHTML = "<h1>" + msg + "</h1>";
	}
	consoleLog("FATAL! " + msg);
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
	
	calls=0;
	if (jqXHRgetShpConvertStatus) {
		try {
			jqXHRgetShpConvertStatus.abort();
			qXHRgetShpConvertStatus=undefined;
		}
		catch (e) {
			console.log("Unable to abort previous getShpConvertStatus() request; call: " + calls)
		}
	}
	
	nodeGeoSpatialFrontEndInit();
	
	$( "#shpConvertGetResults, #shpConvertGetConfig" ).button( "option", "disabled", true );
	
	if (files.length == 0) {
		errorPopup("No files selected");
		return;
	}
	
	if (verbose) { // i.e. diagnostics
		consoleLog('Verbose mode: ' + verbose);
		formData.append('verbose', verbose);
	}	
	formData.append('uuidV1', generateUUID()); // Random reference
	if (hierarchy_post_processing_sql) {
		formData.append('hierarchy_post_processing_sql', hierarchy_post_processing_sql);
	}
	addDbfDescFields(fileList, formData);
	
	// Display the key/value pairs
	for (var pair of formData.entries()) { // Breaks in IE!
		consoleLog("Key: " + pair[0] + '="'+ pair[1] + '"'); 
	}

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
					displayResponse(request.response, request.status, formName, true /* enableGetStatus */);
				}
				else {
					consoleLog("[" + elapsed + "] Ignoring null response"); 
				}
			}
			else {		
				setStatus("request.onreadystatechange() unknown state: " + request.readyState, new Error("readyState error"));
			}
		}
		
		if (isIE() && request.timeout) {
			request.timeout = timeout;
			consoleLog('IE Timeout set: ' + timeout);
		}
		else {
			request.timeout = timeout;
			consoleLog('Timeout set: ' + timeout);
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
	consoleLog("uuidV1: " + options.data.uuidV1);
	uuidV1=options.data.uuidV1;
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
			consoleLog("Formdata: null file");
		}
		else if (formData[i].type == "file") {
			consoleLog("Formdata file[" + i + "]: " + value.name + "; size: " + value.size + "; last modified: " + value.lastModifiedDate);
			fileCount++;
		}
		else if (formData[i].type == "checkbox") {
			if (formData[i].required && formData[i].value) {
				consoleLog("Formdata file[" + i + "] mandatory field: " + formData[i].name + "=" + formData[i].value);	
			}
			else if (formData[i].required) {
				errorPopup("Mandatory checkbox: " + formData[i].name + " is not checked");
				return false;
			}
			else {
				consoleLog("Mandatory checkbox: " + formData[i].name + "=" + formData[i].value);	
			}			
		}		
		else if (formData[i].type == "range") {
			if (formData[i].required && formData[i].value) {
				consoleLog("Formdata file[" + i + "] mandatory field: " + formData[i].name + "=" + formData[i].value);	
			}
			else if (formData[i].required) {	
				errorPopup("Mandatory range: " + formData[i].name + " is not set");
				return false;
			}
			else {
				consoleLog("Formdata file[" + i + "] field: " + formData[i].name + "=" + formData[i].value);	
			}
		}
		else if (formData[i].type == "select-one") {
			if (formData[i].required && formData[i].value) {
				consoleLog("Formdata file[" + i + "] mandatory field: " + formData[i].name + "=" + formData[i].value);	
			}
			else if (formData[i].required) {	
				errorPopup("Mandatory field: " + formData[i].name + " is not filled in");
				return false;
			}			
			else {
				consoleLog("Formdata file[" + i + "] field: " + formData[i].name + "=" + formData[i].value);	
			}
		}
		else {
			consoleLog("Formdata other [" + i + "]: " + JSON.stringify(formData[i], null, 4));
		}
	}
	if (fileCount == 0) {
		errorPopup("No files selected");
		return false;
	}
	
    // jqForm is a jQuery object encapsulating the form element.  To access the 
    // DOM element for the form do this: 
    // var formElement = jqForm[0]; 
	if (!setupMap) {
		document.getElementById("status").innerHTML = "<h1>setupMap() not defined: errors in nodeGeoSpatialServices.js</h1>"
		consoleLog("FATAL! setupMap() not defined: errors in nodeGeoSpatialServices.js");
		return false;
	}
    consoleLog('About to submit: ' + JSON.stringify(queryString, null, 4)); 
	nodeGeoSpatialFrontEndInit();
 
	document.getElementById("status").innerHTML  = "";
	
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
		consoleLog("scopeChecker() ERROR: " + msg);
		throw new Error(msg);
	}	
} // End of scopeChecker()

/*
 * Function: 	setWidth()
 * Parameters: 	HTML element, height
 * Returns: 	nothing
 * Description:	Set object width
 */
function setWidth(elem, width){
	if (elem.style) {
		elem.style.width=width + "px";
//		consoleLog("setWidth(" + elem.id + "," + width + ") h x w: " + 
//			elem.offsetHeight + "x" + elem.offsetWidth);	
	}
	else {
		consoleError("setWidth() " + (elem.id||JSON.stringify(elem)) + " not found");
	}
}
	
/*
 * Function: 	setupMap()
 * Parameters: 	None
 * Returns: 	Nothing
 * Description:	Setup map width for Leaflet 
 */
function setupMap() {	
	var w = window,
		d = document,
		e = d.documentElement,
		g = d.getElementsByTagName('body')[0],
		x = w.innerWidth || e.clientWidth || g.clientWidth,
		y = w.innerHeight|| e.clientHeight|| g.clientHeight;
		
	if (document.getElementById("tabs")) { // JQuery-UI version
		var tabboxheight=document.getElementById('tabbox').offsetHeight;
//		var height=document.getElementById('tilemakerbody').offsetHeight-(tabboxheight+24);
//		var height=y-22-(tabboxheight+24); // Use Y instead with a correction factor (22)
		var height=y-tabboxheight-46; // Merge all the correction factors (46px)
		function setHeight(id, lheight) {
			document.getElementById(id).setAttribute("style","display:block;cursor:pointer;cursor:hand;");
			document.getElementById(id).setAttribute("draggable", "true");
			document.getElementById(id).style.display = "block"	;
			document.getElementById(id).style.cursor = "hand";			
			document.getElementById(id).style.height=lheight + "px";						
			
			consoleLog(id + " h x w: " + document.getElementById(id).offsetHeight + "x" + document.getElementById(id).offsetWidth);	
		}

		consoleLog("Using JQuery-UI; setup map; height: " + height + "px; tabboxheight: " + tabboxheight + "px");
		
		// Check for min height
		
		setHeight("maptab", (height-50));
		setHeight("mapcontainer", (height-52));
		setHeight("map", (height-55));
		
//		document.getElementById("map").setAttribute("style", "overflow:auto;"); // Breaks leaflet
			
		setHeight("statustab", (height-50));
		setHeight("status", (height-55));
		setHeight("shapeFileSelectortab", (height-50));
		setHeight("shapeFileSelector", (height-55));
		setHeight("accordion", (height-180));
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
			
			document.getElementById("map").setAttribute("style","display:block;cursor:pointer;cursor:hand;overflow:auto;");
			document.getElementById("map").setAttribute("draggable", "true");
			document.getElementById("map").style.display = "block"	;
			document.getElementById("map").style.cursor = "hand";	
			
			document.getElementById('map').style.width=new_w + "px";
			document.getElementById('map').style.height=new_h + "px";
			
			consoleLog("Size h x w: " + h + "x" + w +
				"; map size old: " + old_h + "x" + old_w + ", new: " + new_h + "x" + new_w +
				"; new status width: " + new_status_width);
			consoleLog("Map; h x w: " + document.getElementById('map').style.height + "x" + document.getElementById('map').style.width);	
			consoleLog("Status; h x w: " + document.getElementById('status').style.height + "x" + document.getElementById('status').style.width);	
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
			status=document.getElementById("status");
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
			consoleLog("+" + elapsed + " " + msg);
		}
		else {
			var error=status;
			if (document.getElementById("tabs") && tabs && document.getElementById("error")) { // JQuery-UI version
				error=document.getElementById("error");
				error.innerHTML="<h3>" + msg + "</h3><p>" + errm + "</p>";
			}
			else {
				error.innerHTML = "<h1>" + msg + "</h1><h2>Error: " + errm + "</h2>";
			}
			consoleLog(error.innerHTML);
			if (stack) {
				error.innerHTML += "<p>Stack:</br><pre>" + stack + "</pre></p>";
				consoleLog("[" + elapsed + "] Stack: " + stack);
			}
			else if (err && err.stack) {
				error.innerHTML += "<p>Stack:</br><pre>" + err.stack + "</pre></p>";
				consoleLog("[" + elapsed + "] err.Stack: " + stack);
			}
			if (diagnostic) {
				error.innerHTML += "<p>Processing diagnostic:</br><pre>" + diagnostic + "</pre></p>";
				consoleLog("[" + elapsed + "] Diagnostic: " + diagnostic);
			} 
				
			if (document.getElementById("tabs") && tabs) { // JQuery-UI version
			
				var errorWidth=document.getElementById('tabbox').offsetWidth-300;
				if (errm) {
					$( "#error" ).dialog({
						modal: true,
						width: errorWidth
					});
				}
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
									
	consoleLog("[" + elapsed + "] Create Leaflet map; h x w: " + document.getElementById('map').style.height + "x" + document.getElementById('map').style.width);	
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
			consoleLog("WARNING! Unable to remove map during error recovery");
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
			consoleLog("WARNING! Unable to remove map during error recovery");
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
					loading: function(event) { consoleLog("Loading: " + layer); },
					load: function(event) { consoleLog("Loaded: " + layer); }
				}, this);
			}); */
		}
		else {
			consoleLog("Zoomlevel based layer support disabled; only one zoomlevel of data present");
		}
	}
	catch (e) {
		try {
			map.remove();
		}
		catch (e2) {
			consoleLog("WARNING! Unable to remove map during error recovery");
		}
		throw new Error("Unable to add zoomend event to map: " +  e.message);
	}		
	
	try {
		end=new Date().getTime();
		elapsed=(Math.round(end - start))/1000; // in S		
		consoleLog("[" + elapsed + "] Creating basemap...");															
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
		consoleLog("[" + elapsed + "] Added tileLayer and scale to map");	
	
		return map;
	}
	catch (e) {
		try {
			map.remove();
		}
		catch (e2) {
			consoleLog("WARNING! Unable to remove map during error recovery");
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
				consoleLog("File [" + i + "]: Using topojson bounding box");	
				
				response.file_list[i].boundingBox={
					xmin: response.file_list[i].topojson[0].topojson.objects.collection.bbox[0], 
					ymin: response.file_list[i].topojson[0].topojson.objects.collection.bbox[1], 
					xmax: response.file_list[i].topojson[0].topojson.objects.collection.bbox[2], 
					ymax: response.file_list[i].topojson[0].topojson.objects.collection.bbox[3]};										
			}
			else {
				consoleLog("WARNING! File [" + i + "/" + (response.no_files - 1) + "]: bounding box is not defined; using whole world as bounding box");								
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
			consoleError("\nERROR: Bounding box " + i + ": [" +
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
//							consoleLog("ngeolevels[" + i + "]: " + JSON.stringify(ngeolevels[i], null, 4));
		if (i == 0 && ngeolevels.length > 1 && ngeolevels[i].total_areas != 1) { // Geolevel 1 - Check that minimum resolution shapefile has only 1 area
			setStatus("Check that minimum resolution shapefile has only 1 area", 
				new Error("geolevel 1/" + ngeolevels.length + " shapefile: " + ngeolevels[i].file_name + " has >1 (" + ngeolevels[i].total_areas + ") area)"));
		}
	}
	
	for (var i=0; i < response.no_files; i++) {	// Re-order by geolevel_id if required	
		var j=ngeolevels[i].i;
		if (response.file_list[j].geolevel_id) { // Geolevel ID present in data
			consoleLog("File[" + j + "]: " + response.file_list[j].file_name +
				"; geolevel: " + response.file_list[j].geolevel_id +
				"; size: " + response.file_list[j].file_size +
				"; areas: " + response.file_list[j].total_areas);
		}
		else {
			response.file_list[j].geolevel_id = ngeolevels[i].geolevel_id;
			if (response.file_list[j].geolevel_id) {
				consoleLog("File[" + j + "]: " + response.file_list[j].file_name +
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
//							consoleLog("geolevels[" + i + "]: " + JSON.stringify(geolevels[i], null, 4));
//					}
	
	for (var i=0; i < response.no_files; i++) {	// Re-order by geolevel_id; creating layerAddOrder array		
		if (response.file_list[i].geolevel_id) {
			consoleLog("Re-order: layerAddOrder[" + (response.no_files-response.file_list[i].geolevel_id) + "]=" + i);
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
 * Parameters: 	Response text (JSON as a string), status code, form name (textual), enable getStatus(): true/false
 * Returns: 	Nothing
 * Description:	Display reponse from submit form
 */
function displayResponse(responseText, status, formName, enableGetStatus) {
										
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
				if (response.fields["batchMode"] == "true" && enableGetStatus) {
					displayProgress("Server is processing response");
					setTimeout(waitForServerResponse(
						response.fields["uuidV1"], response.fields["diagnosticFileDir"], response.fields["statusFileName"], response.fields["responseFileName"] + ".2",
						1000 /* Next timeout */, 1 /* Recursion count */, -1 /* Index */), 5000 /* 5 S timer */);
					return;
				}
				else if (!response.file_list[0]) {
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
	
								consoleLog("Centre: " + centre.lat + ", " + centre.lng);
		//						if (+centre.lat.toFixed(4) == +y_avg.toFixed(4) && 
		//							+centre.lng.toFixed(4) == +x_avg.toFixed(4)) {
		//							consoleLog("Map centre has not changed");
		//						}
		//						else {
									map.eachLayer(function (layer) {
										consoleLog('Remove tileLayer');
										map.removeLayer(layer);
									});
									consoleLog('Remove map');
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
					consoleLog("SRID: " + response.file_list[0].srid);
				}	
					
				if (response.file_list[0].projection_name) {
					consoleLog("Projection name: " + response.file_list[0].projection_name);
				}	
				
				if (response.file_list[0].boundingBox) {
					consoleLog("Bounding box [" +
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
	var elapsed=(Math.round((end - start)/100))/10; // in S
	msg+="Time taken: " + elapsed + " S; upload time: " + uploadTime + " S; server time: " + serverTime + " S [excluding map draw time]</br>";
	if (document.getElementById("progressbar")) { // JQuery-UI version
		$(".progress-label").text( "Map completed; took " +  elapsed + " S");
	}
	if (response.message) {
		msg+="<p>Processing diagnostic messages:</br><div id=\"div_message\" style=\"overflow:scroll; height: 400px;\"><pre>" + response.message + "</pre></div?</p>"
	}
	
	if (response.diagnostic) {
		msg+="<p>Processing diagnostic:</br><pre>" + response.diagnostic + "</pre></p>";
	}	
	
	if (status == 200  || status == "success") {	
		setStatus("<h1>" + formName + " processed OK</h1>", undefined, msg);
	}	
	else {
		setStatus("Send Failed", new Error("Unexpected http status: " + status), "Message:" + msg);
	}
}	


/*
 * Function: 	getShpConvertTopoJSON()
 * Parameters:  uuidV1, diagnosticFileDir, response file name
 * Returns: 	Nothing
 * Description: Wait for server response: call getShpConvertStatus method until shpConvert completes
 */
function getShpConvertTopoJSON(uuidV1, diagnosticFileDir, responseFileName) {
	consoleLog("Wait for server response for getShpConvertTopoJSON(uuidV1): " + uuidV1);
	var lstart=new Date().getTime();
	var atEnd=false;
	
	var jqXHR=$.get("getShpConvertTopoJSON", 
		{ 
			uuidV1: uuidV1, 
			diagnosticFileDir: diagnosticFileDir,
			responseFileName: responseFileName			
		}, function getShpConvertTopoJSON(data, status, xhr) {

			var end=new Date().getTime();
			var elapsed=(Math.round((end - lstart)/100))/10; // in S	
			consoleLog("BATCH_INTERMEDIATE_END +" + elapsed + " Got server response for getShpConvertTopoJSON(uuidV1): " + uuidV1);		
			
			displayResponse(data, status, "shpConvert" /* formName */, false /* enableGetStatus */);
		}, // End of getShpConvertTopoJSON() 
		"json");
	jqXHR.fail(function getShpConvertTopoJSONError(x, e) {
		var msg="";
		var response;
		try {
			if (x.responseText) {
				response=JSON.parse(x.responseText);
			}
		}
		catch (e) {
			msg+="Error parsing response: " + e.message;
		}
		
		if (x.status == 0) {
			msg+="Unable to get TopoJSON data from shapefile conversion request; network error";
		} 
		else if (x.status == 404) {
			msg+="Unable to get TopoJSON data from shapefile conversion request; URL not found: getShpConvertStatus";
		} 
		else if (x.status == 500) {
			msg+="Unable to get TopoJSON data from shapefile conversion request; internal server error";
			if (response && response.message) {
				msg+="<br><pre>" + response.message + "</pre>";
			}
		}  
		else if (response && response.message) {
			msg+="Unable to get TopoJSON data from shapefile conversion request; unknown error: " + x.status + "<p>" + response.message + "</p>";
		}	
		else if (response) {
			msg+="Unable to get TopoJSON data from shapefile conversion request; unknown error: " + x.status + "<br><pre>" + JSON.stringify(response, null, 4) + "</pre>";
		}
		else {
			msg+="Unable to get TopoJSON data from shapefile conversion request; unknown error: " + x.status + "<br><pre>" + x.responseText + "</pre>";
		}
		
		if (e && e.message) {
			errorPopup(msg + "<br><pre>" + e.message + "</pre>");
		}
		else {
			errorPopup(msg);
		}
		displayProgress("Unable to fetch map");
		consoleError(msg);
	});
		
} // End of getShpConvertTopoJSON()
	
/*
 * Function: 	displayProgress()
 * Parameters:  Msg
 * Returns: 	Nothing
 * Description: Log and display progress message
 */	
function displayProgress(msg) {
	var end=new Date().getTime();
	var elapsed=(Math.round(end - start))/1000; // in S
	consoleLog("+" + elapsed + " [PROGRESS]: " + msg);
	progressLabel.text(msg);
}	

/*
 * Function: 	waitForServerResponse()
 * Parameters:  uuidV1, diagnosticFileDir, status file name, response file name, next timeout (mS), recursion count, index
 * Returns: 	Nothing
 * Description: Wait for server response: call getShpConvertStatus method until shpConvert completes
 */
function waitForServerResponse(uuidV1, diagnosticFileDir, statusFileName, responseFileName, nextTimeout, recursionCount, index) {
	
	var end=new Date().getTime();
	var elapsed=(Math.round(end - start))/1000; // in S
	consoleLog("+" + elapsed + " Wait: " + recursionCount + " for server response for getShpConvertStatus(uuidV1): " + uuidV1 + "; current index: " + index);
	var lstart=new Date().getTime();
	var atEnd=false;
	var hasErrors=false;
	var errorCount=0;

	if (jqXHRgetShpConvertStatus) {
		console.log("Previous getShpConvertStatus() request; call: " + calls + "; state: " + jqXHRgetShpConvertStatus.readyState)
	}
	calls++;
	
	jqXHRgetShpConvertStatus=$.get("getShpConvertStatus", 
		{ 
			uuidV1: uuidV1, 
			diagnosticFileDir: diagnosticFileDir,
			statusFileName: statusFileName,
			responseFileName: responseFileName,
			lstart: lstart,							// Added to prevent IE caching!
			calls: calls,
			index: index
		}, function getShpConvertStatus(data, status, xhr) {
			var nrecursionCount=recursionCount+1;
			
			if (data && data.status) {
				nIndex=data.status.length;
				if (nIndex > index) { // Found new status's 
					nrecursionCount=0;
				}
				
				for (var i=0; i<data.status.length; i++) { // Look for any errors
					if (data.status[i]) {
						if (data.status[i].statusText == "FATAL") {
							atEnd=true;
							hasErrors=true;
							errorCount++;
						}
						if (data.status[i].statusText == "ERROR") {
							hasErrors=true;
							errorCount++;
						}
					}
				}
					
				for (var i=index; i<data.status.length; i++) { // Add new statii; look for end
					if (data.status[i]) {
						consoleLog("+" + data.status[i]["etime"] + data.status[i].httpStatus + " " + 
							" [" + data.status[i].sfile + ":" + data.status[i].sline + ":" + 
							data.status[i].calling_function + "] (i: " + i + "/" + data.status.length + "; index: " + index + "): " + 
							data.status[i].statusText);	
							
						if (data.status[i].statusText == "BATCH_END") {
							consoleLog("Enable shpConvertGetResults button");
							var shpConvertGetResults=document.getElementById("shpConvertGetResults");
							shpConvertGetResults.href= "shpConvertGetResults.zip?uuidV1="+ uuidV1;
							shpConvertGetResults.download= "shpConvertGetResults_"+ uuidV1 + ".zip";
							consoleLog("change shpConvertGetResults href to: " + shpConvertGetResults.href);
							$( "#shpConvertGetResults" ).button( "enable" ); // Enable shpConvertGetResults button
							// Load tiles
							atEnd=true;
						}					
						else if (data.status[i].statusText == "BATCH_INTERMEDIATE_END") {
							consoleLog("Enable shpConvertGetConfig button");
							var shpConvertGetConfig=document.getElementById("shpConvertGetConfig");
							shpConvertGetConfig.href= "shpConvertGetConfig.xml?uuidV1="+ uuidV1;
							shpConvertGetConfig.download= "shpConvertGetConfig_"+ uuidV1 + ".xml";
							consoleLog("change shpConvertGetConfig href to: " + shpConvertGetConfig.href);
							$( "#shpConvertGetConfig" ).button( "enable" ); // Enable shpConvertGetConfig button
							
							getShpConvertTopoJSON(uuidV1, diagnosticFileDir, responseFileName); // Load intermediate map
//								atEnd=true;
						}				
							
					}
				} // End of for loop					
					
				var end=new Date().getTime();
				var ltime=(end - lstart)/1000; // in S	
				if (ltime > 5) { // Help find non async code - causes status delays
					var elapsed=(Math.round(end - start))/1000; // in S
					consoleLog("+" + elapsed + " WARNING: status interruption of " + ltime + " seconds for last status"); 
				}	
					
				var status=data.status[(data.status.length-1)];
				if (status && status["statusText"]) {
					if (status["statusText"] == "BATCH_END") { // Display the last status in the progress bar
						consoleLog("No need for more status updates: at end");
					}					
					else if (status["statusText"] == "BATCH_INTERMEDIATE_END") {
						displayProgress("Intermediate processing complete, loading maps, making tiles");
					}				
					else if (status["statusText"] == "FATAL") {
						displayProgress("Processing failed");
						hasErrors=true;
					}
					else if (status["statusText"] == "ERROR") {
						displayProgress("Processing failed");
					}
					else {
						displayProgress(status["statusText"]);
					}	
				} // End of if (status && status["statusText"])
				else if (status) { // This is a suspect error
					consoleError("No statusText for status: " + (data.status.length-1));
				}
				else {
					consoleError("No status for status: " + (data.status.length-1));
				}
				
				if (nrecursionCount < 90 && atEnd == false) { // Not at end; setTimeout for more statii
//					consoleLog("Not at end; setTimeout for more statii");
					setTimeout(waitForServerResponse, nextTimeout, uuidV1, diagnosticFileDir, statusFileName, responseFileName, nextTimeout /* Next timeout */, 
						nrecursionCount /* Recursion count */, nIndex /* New index */);
				}
		
				if (atEnd) { // At end no errors				
					consoleLog("No need for more status updates: at end");
					var end=new Date().getTime();
					var elapsed=Math.round((end - start)/1000, 2); // in S
					displayProgress("All node processing completed in " + elapsed + " S");	
				}
				
				if (hasErrors) { // At has errors
					var end=new Date().getTime();
					var elapsed=Math.round((end - start)/1000, 2); // in S
					var errorText="Node processing had error after " + elapsed + " S";
					if (atEnd) {
						errorText="Node processing failed with error after " + elapsed + " S";
					}
					displayProgress(errorText);
					consoleLog("Has " + errorCount + " errors: " + errorText);
					var j=0;
					var errorName;
					for (var i=0; i<data.status.length; i++) {
						if (data.status[i] && data.status[i].httpStatus != 200) {
							j++;
							if (j==1) {
								errorText+="</br><p><table id=\"errorpopuptable\">" + 
								"<caption>Status events in error</caption>" +
								"<tr><th>Elapsed time</th><th>" +
									"Code</th><th>" + 
									"File/Line/Function</td><td>Error</th>" +
//									"<td>Stack</th>" + 
									"<td>Addtional Info</th></tr>";	
							}
							errorText+="<tr><td>+" + data.status[i]["etime"] + " S</td><td>" + 
								data.status[i].httpStatus + "</td><td>" + 
								"[" + data.status[i].sfile + ":" + data.status[i].sline + ":" + 
								data.status[i].calling_function + "]</td><td>" + 
								data.status[i].statusText + "</td><td>" +
//								"<pre>" + (data.status[i].stack || "No stack") + "</pre></td><td>" +
								"<pre>" + (data.status[i].additionalInfo || "&nbsp;") + "</pre></td></tr>";
							if (data.status[i].errorName) {
								consoleLog("extended message errorName[" + i + "]: " + data.status[i].errorName);
								errorName=data.status[i].errorName;
							}
						}
					}	

					if (j>0) {	// Error popup support
						errorText+="</table></p>";
						switch (errorName) {
							case "AREA_NAME_MISMATCH":
								errorPopup("Area names do not match for the same duplicate area ID", errorText);
								break;
							case "DUPLICATE_AREA_ID":
								errorPopup("Duplicate area ID detected in shapefile", errorText);
								break;
							case undefined:
								errorPopup(errorText);
								break;
							default:
								errorPopup(errorText);
								break;
						}
					}					
					consoleLog("No need for more status updates: at end");
				} // End of hasErrors
				
				if (nrecursionCount >= 90 && atEnd == false) { // Status timeout detector
					
					for (var i=0; i<data.status.length; i++) { // Add new statii; look for end
						if (data.status[i] && data.status[i].statusText == "BATCH_END") {
							atEnd=true;
						}
					}
					if (atEnd) {
						displayProgress("Processing failed, no success or failure detected, atEnd detected, no status change in " + 
							nrecursionCount + " seconds");
						consoleError("Status update recursion limit reached with no new status: " + nrecursionCount);
					}
					else {
						displayProgress("Processing failed, no success or failure detected, no status change in " + 
							nrecursionCount + " seconds");
						consoleError("Status update recursion limit reached with no new status: " + nrecursionCount);				
					}
				}
			}
			else {
				consoleError("No data.status")
			}
		}, // End of getShpConvertStatus() 
		"json");
		
	jqXHRgetShpConvertStatus.fail(function getShpConvertStatusError(x, e) {
		var msg="";
		var response;
		try {
			if (x.responseText) {
				response=JSON.parse(x.responseText);
			}
		}
		catch (e) {
			msg+="Error parsing response: " + e.message;
		}
		
		if (x.status == 0) {
			msg+="Unable to get status for shapefile conversion request; network error";
		} 
		else if (x.status == 404) {
			msg+="Unable to get status for shapefile conversion request; URL not found: getShpConvertStatus";
		} 
		else if (x.status == 500) {
			msg+="Unable to get status for shapefile conversion request; internal server error";
		}  
		else if (response && response.message) {
			msg+="Unable to get status for shapefile conversion request; unknown error: " + x.status + "<p>" + response.message + "</p>";
		}	
		else if (response) {
			msg+="Unable to get status for shapefile conversion request; unknown error: " + x.status + "<br><pre>" + JSON.stringify(response, null, 4) + "</pre>";
		}
		else {
			msg+="Unable to get status for shapefile conversion request; unknown error: " + x.status + "<br><pre>" + x.responseText + "</pre>";
		}
		
		if (e && e.message) {
			errorPopup(msg + "<br><pre>" + e.message + "</pre>");
		}
		else {
			errorPopup(msg);
		}
		
		displayProgress("Proccessing failed");
		if (response && response.message) {
			msg+=response.message;
		}		
		consoleError(msg);
	});
		
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
		consoleLog("jsonAddLayer() scopeChecker ERROR: " + e.message);
		callback(e);
		return;
	}
	
	var end=new Date().getTime();
	var elapsed=(Math.round(end - start))/1000; // in S
	var verb="Modifying data in";
	var msg;
	
	// Suppress for performance
	if (jsonAddLayerParams.areas > 200 && map.getZoom() < 6) {
		consoleLog("[" + elapsed + "] " + "Suppressing (jsonAddLayerParams.areas > 200 && map.getZoom() < 6) JSONLayer[" + jsonAddLayerParams.i + "/" + jsonAddLayerParams.no_files + 
			"]; file layer [" + jsonAddLayerParams.layerAddOrder + "]: " +
			jsonAddLayerParams.file_name + "; areas: " + jsonAddLayerParams.areas + "; zoomlevel: " +  map.getZoom());
		callback();
		return;
	}
	else if (jsonAddLayerParams.areas > 10000) {
		consoleLog("[" + elapsed + "] " + "Suppressing (jsonAddLayerParams.areas > 10,000) JSONLayer[" + jsonAddLayerParams.i + "/" + jsonAddLayerParams.no_files + 
			"]; file layer [" + jsonAddLayerParams.layerAddOrder + "]: " +
			jsonAddLayerParams.file_name + "; areas: " + jsonAddLayerParams.areas + "; zoomlevel: " +  map.getZoom());
		callback();
		return;
	}
		
	if (initialRun) {
		verb="Adding data to";
	}
							
	try {
		if (jsonAddLayerParams.JSONLayer[jsonAddLayerParams.i]) {	
			consoleLog("[" + elapsed + "] Removing topoJSONLayer data: " + jsonAddLayerParams.i);
			jsonAddLayerParams.JSONLayer[jsonAddLayerParams.i].clearLayers();
			consoleLog("[" + elapsed + "] Removed topoJSONLayer data: " + jsonAddLayerParams.i);
		}
	}
	catch (e) {
		end=new Date().getTime();
		elapsed=(Math.round(end - start))/1000; // in S
		
		msg="[" + elapsed + "] Error removing JSON layer [" + jsonAddLayerParams.i  + "/" + jsonAddLayerParams.no_files + "]  map: " + e.message;
		consoleLog("jsonAddLayer() ERROR: " + msg);
		callback(new Error(msg));
		return;
	}			
		
	try {	
		if (jsonAddLayerParams.JSONLayer[jsonAddLayerParams.i] == undefined) {
			if (jsonAddLayerParams.isGeoJSON) { // Use the right function
				displayProgress("Adding geoJSON map layer for " + jsonAddLayerParams.file_name);
				consoleLog("+" + elapsed + " " + verb + " GeoJSONLayer[" + jsonAddLayerParams.i + "/" + jsonAddLayerParams.no_files + 
					"]; file layer [" + jsonAddLayerParams.layerAddOrder + "]: " +
					jsonAddLayerParams.file_name +
					"; colour: " + jsonAddLayerParams.style.color + "; weight: " + jsonAddLayerParams.style.weight + 
					"; opacity: " + jsonAddLayerParams.style.opacity + "; fillOpacity: " + jsonAddLayerParams.style.fillOpacity + "; zoomlevel: " +  map.getZoom());
				JSONLayer[jsonAddLayerParams.i] = L.geoJson(undefined /* Geojson options */, 
					jsonAddLayerParams.style).addTo(map);
			}
			else {
				displayProgress("Adding topoJSON map layer for " + jsonAddLayerParams.file_name);
				consoleLog("+" + elapsed + " " + verb + " TopoJSONLayer[" + jsonAddLayerParams.i + "/" + jsonAddLayerParams.no_files + 
					"]; file layer [" + jsonAddLayerParams.layerAddOrder + "]: " +
					jsonAddLayerParams.file_name +
					"; colour: " + jsonAddLayerParams.style.color + "; weight: " + jsonAddLayerParams.style.weight + 
					"; opacity: " + jsonAddLayerParams.style.opacity + "; fillOpacity: " + jsonAddLayerParams.style.fillOpacity + "; zoomlevel: " +  map.getZoom());
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
					consoleLog("Map layer: " + jsonAddLayerParams.i + "; bring layer: " + j + " to front");
					JSONLayer[j].bringToFront();
				}
			}
		
			map.whenReady(function jsonAddLayerReady() { 
					end=new Date().getTime();
					elapsed=(Math.round(end - start))/1000; // in S
					consoleLog("[" + elapsed + "] Added JSONLayer [" + jsonAddLayerParams.i  + "/" + jsonAddLayerParams.no_files + 
						"]: " + jsonAddLayerParams.file_name + "; zoomlevel: " +  map.getZoom() /* + "; size: " + sizeof(json) SLOW!!! */);
			//		consoleLog("Callback: " + jsonAddLayerParams.i);
					callback();
				}, this); 
		}
		else {
			msg="jsonAddLayer(): jsonAddLayerParams.json is not defined.";
			consoleLog("jsonAddLayer() ERROR: " + msg);
			callback(new Error(msg));
		}	
	}
	catch (e) {
		end=new Date().getTime();
		elapsed=(Math.round(end - start))/1000; // in S
		
		msg="[" + elapsed + "] Error adding JSON layer [" + jsonAddLayerParams.i  + "/" + jsonAddLayerParams.no_files + "] to map: " + e.message;
		consoleLog("jsonAddLayer() ERROR: " + msg);
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
				consoleLog("Layer [" + layerNum + "]: key: " + key + "; no match for zoomlevel: " + mapZoomlevel + 
					"; maxZoomlevel key: " + maxZoomlevel + "; minZoomlevel key: " + minZoomlevel);
			}
		}
		
		if (json) {
			consoleLog("Layer [" + layerNum + "]: json match for zoomlevel: " + mapZoomlevel + 
				"; maxZoomlevel key: " + maxZoomlevel + "; minZoomlevel key: " + minZoomlevel);
			return json;
		}
		// no json found for zoomlevel: 3; mapZoomlevel < minZoomlevel; using minZoomlevel key: 10
		if (json == undefined && mapZoomlevel > maxZoomlevel) {
			consoleLog("Layer [" + layerNum + "]: no json found for zoomlevel: " + mapZoomlevel + "; using maxZoomlevel key: " + maxZoomlevel);
			json=jsonZoomlevels[maxZoomlevel];
		}	
		
		if (json == undefined && mapZoomlevel < minZoomlevel) {
			consoleLog("Layer [" + layerNum + "]: no json found for zoomlevel: " + mapZoomlevel + "; mapZoomlevel < minZoomlevel; using minZoomlevel key: " + minZoomlevel);
			json=jsonZoomlevels[minZoomlevel];
		}
		
		if (json == undefined && minZoomlevel) {
			consoleLog("Layer [" + layerNum + "]: no json found for zoomlevel: " + mapZoomlevel + "; using minZoomlevel key: " + minZoomlevel);
			json=jsonZoomlevels[minZoomlevel];
		}
		
		if (json == undefined && firstKey) {
			consoleLog("Layer [" + layerNum + "]: no json found for zoomlevel: " + mapZoomlevel + "; using first key: " + firstKey);
			json=jsonZoomlevels[firstKey];
		}
		
		if (json == undefined) {
			consoleLog("Layer [" + layerNum + "]: no json found for zoomlevel: " + mapZoomlevel + "; using default zoomlevel: 11");
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
		consoleError("error: " + JSON.stringify(error, null, 4));
		if (ajaxOptions) {
			consoleError("ajaxOptions: " + JSON.stringify(ajaxOptions, null, 4));
		}
		if (thrownError) {
			consoleError("thrownError: " + JSON.stringify(thrownError, null, 4));
		}
		var msg="<h1>Send Failed; http status: ";
		var popupMsg;
		
		if (error.status == undefined && thrownError) {
			msg = "errorHandler() Ajax exception from JQuery Form: " + thrownError.message + "\nStack:\n" + (errorHandler.stack || ("No stack)"));
			document.getElementById("status").innerHTML = "<h1>Send Failed;</h1>" + msg;
			if (ajaxOptions) {
				consoleLog("errorHandler(): ajaxOptions: " + JSON.stringify(ajaxOptions, null, 4));
			}
			consoleError(msg);
			popupMsg="<h3>Failed to process request: (no error status or thrownError returned - likely server failure/firefox bug)</h3>";
		}
		else if (error.status) {
			msg+=error.status;
			popupMsg="<h3>Failed to process request: " + error.status + "</h3>";
		}
		else if (thrownError) {
			msg+="errorHandler() Ajax exception from JQuery Form: " + thrownError.message + "\nStack:\n" + (errorHandler.stack || ("No stack)"));
			popupMsg="<h3>Failed to process request: "+ thrownError.message + "</h3>";
		}	
		else {
			msg+="(no error status or thrownError returned - likely server failure/firefox bug)";
			popupMsg="<h3>Failed to process request: (no error status or thrownError returned - likely server failure/firefox bug)</h3>";
		}
		msg+="</h1>";
		if (error.responseJSON && error.responseJSON.error) {
			msg+="</br>Error text: " + error.responseJSON.error;
			popupMsg+= error.responseJSON.error;
		}
		else {
			consoleLog("No error text in JSON response");
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
			consoleLog("errorHandler(): ajaxOptions: " + JSON.stringify(ajaxOptions, null, 4));
		}
		consoleError(msg);
	}
	else {
		consoleError("errorHandler(): No error or thrownError returned");
	}
	
	if (map) {
		map.eachLayer(function (layer) {
			consoleLog('Remove tileLayer');
			map.removeLayer(layer);
		});
		consoleLog('Remove map');
		map.remove(); // Known leaflet bug:
					  // Failed to execute 'removeChild' on 'Node': The node to be removed is not a child of this node.
		map = undefined;
		document.getElementById("map").innerHTML = "";			 
		consoleLog('Remove map element'); 
	}		

	if (document.getElementById("tabs") && tabs && document.getElementById("error")) { // JQuery-UI version
		displayProgress("Upload failed");
		error=document.getElementById("error");
		error.innerHTML=popupMsg;
		var errorWidth=document.getElementById('tabbox').offsetWidth-300;
		$( "#error" ).dialog({
			modal: true,
			width: errorWidth
		});
		tabs.tabs("refresh" );
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
			displayProgress( "Upload complete; awaiting server response");
		}
	}
	else {
		document.getElementById("status").innerHTML = msg;
	}
	consoleLog(msg);
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
				consoleError("whenMapIsReady(): caught: " + e.message + "\nStack: " + e.stack);
				return;
			}
							
			var end=new Date().getTime();
			var elapsed=(Math.round(end - start))/1000; // in S
			var initialRun;
			
			if (event) {
				consoleLog("[" + elapsed + "] New zoomlevel: " +  map.getZoom() + "; event: " + event.type);	
				initialRun=false;
			}
			else {
				consoleLog("[" + elapsed + "] Initial zoomlevel: " +  map.getZoom());	
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
						consoleError("asyncEachSeries(): caught: " + e.message);
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
									consoleError("[" + elapsed + "] asyncEachErrorHandler: " + err.message + "\nStack: " + err.stack);								
								}
								else if (event) {
									consoleLog("[" + elapsed + "] " + jsonAddLayerParamsArray.length + " zoomed layers processed OK.");
								}
								else {
									var end=new Date().getTime();
									var elapsed=(Math.round(end - start))/1000; // in S
									displayProgress("All node processing completed in " + elapsed + " S");	
									consoleLog("[" + elapsed + "] " + jsonAddLayerParamsArray.length + " initial layers processed OK.");
								}
							}
							catch(e) {
								consoleError("asyncEachSeriesError(): caught: " + e.message + "\nStack: " + e.stack);
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
	
	var msg="<table id=\"diagnosticstable\" border=\"1\" style=\"width:100%\">" + 
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
				'<i style="background:' + layerColours[i] + '"></i>' + (response.file_list[layerAddOrder[i]].desc || response.file_list[layerAddOrder[i]].file_name) + 
					" (" + response.file_list[layerAddOrder[i]].total_areas + " areas)");
		}

		div.innerHTML = '<i style="background:#FFF"></i><em>Geolevels</em><br>' + labels.join('<br>');

		return div;
	};

	legend.addTo(map);
}


/*
 * Function: 	shpConvertGetResultsFunc()
 * Parameters: 	Value
 * Returns: 	Nothing
 * Description:	Action when shpConvertGetResults button is clicked
 */
function shpConvertGetResultsFunc(event) {
	event = event || window.event;
	
	if (event) {
//		event.preventDefault();
		consoleLog("shpConvertGetResultsFunc: call shpConvertGetResults("+ uuidV1 + ")");
	}
	else {
		consoleError("shpConvertGetResultsFunc(): no event defined");
	}	
}

/*
 * Function: 	shpConvertGetConfigFunc()
 * Parameters: 	Event
 * Returns: 	Nothing
 * Description:	Action when shpConvertGetConfig button is clicked
 */
function shpConvertGetConfigFunc(event) {
	event = event || window.event;

	if (event) {
//		event.preventDefault();
		consoleLog("shpConvertGetConfigFunc: call shpConvertGetConfig("+ uuidV1 + ")");
	}
	else {
		consoleError("shpConvertGetConfigFunc(): no event defined");
	}
}
