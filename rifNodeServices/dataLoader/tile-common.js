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
// Rapid Enquiry Facility (RIF) - Tile viewer code
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

var lstart=new Date().getTime(); 	// GLOBAL: Start time for console log/error messages

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
		consoleError("scopeChecker(): " + msg);
		throw new Error(msg);
	}	
} // End of scopeChecker()
		
/*
 * Function: 	consoleLog()
 * Parameters:  Message
 * Returns: 	Nothing
 * Description:	Extend Leaflet to use topoJSON 
 * 				Copyright (c) 2013 Ryan Clark
 */
function leafletEnabletopoJson() {
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
}
 
/*
 * Function: 	consoleLog()
 * Parameters:  Message
 * Returns: 	Nothing
 * Description:	IE safe console log 
 */
function consoleLog(msg) {
	var end=new Date().getTime();
	var elapsed=(Math.round((end - lstart)/100))/10; // in S	
	if (window.console && console && console.log && typeof console.log == "function") {
		if (isIE()) {
			if (window.__IE_DEVTOOLBAR_CONSOLE_COMMAND_LINE) {
				console.log("+" + elapsed + ": " + msg);
			}
		}
		else {
			console.log("+" + elapsed + ": " + msg);
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
	var end=new Date().getTime();
	var elapsed=(Math.round((end - lstart)/100))/10; // in S
	if (window.console && console && console.error && typeof console.error == "function") {
		if (isIE()) {
			if (window.__IE_DEVTOOLBAR_CONSOLE_COMMAND_LINE) {	
				console.log("+" + elapsed + " ERROR: " + msg);
			}
		}
		else {
			console.error("+" + elapsed + " ERROR: " + msg);
		}
	}
}

/*
 * Function: 	errorPopup()
 * Parameters: 	Message, extended message
 * Returns: 	Nothing
 * Description:	Error message popup
 */
function errorPopup(msg, extendedMessage) {
	if (document.getElementById("error")) { // JQuery-UI version
		document.getElementById("error").innerHTML = "<h3>" + msg + "</h3>";
		var errorWidth=document.getElementById('tileviewerbody').offsetWidth-300;
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

	consoleError(msg);
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
 * Function: 	xhrGetMethod()
 * Parameters: 	method name, method description, method callback, method fields object
 * Returns: 	Nothing
 * Description:	Generic XHR GET method
 */
function xhrGetMethod(methodName, methodDescription, methodCallback, methodFields) {
	scopeChecker({
		methodName: methodName, 
		methodDescription: methodDescription, 
		callback: methodCallback, 
		methodFields: methodFields
	});
	
	var jqXHR=$.get(methodName, methodFields, methodCallback, // callback function
		"json");	

	consoleLog("Wait for server response for: " + methodDescription);
	jqXHR.fail(
		function jqXHRError(x, e) {
			var msg="";
			var response;
			try {
				if (x.responseText) {
					response=JSON.parse(x.responseText);
				}
			}
			catch (err) {
				msg+="Error parsing response: " + err.message;
			}
			
			if (x.status == 0) {
				msg+="Unable to " + methodDescription + "; network error";
			} 
			else if (x.status == 404) {
				msg+="Unable to " + methodDescription + "; URL not found: getGeographies";
			} 
			else if (x.status == 500) {
				msg+="Unable to " + methodDescription + "; internal server error";
				if (response && response.message) {
					msg+="<p>" + response.message + "</p>";
				}	
				else if (response) {
					msg+="<br><pre>Response: " + JSON.stringify(response, null, 4) + "</pre>";
				}
				else if (x.responseText) {
					msg+="<br><pre>Response Text: " + x.responseText + "</pre>";
				}
				else  {
					msg+="<br><pre>No reponse text</pre>";
				}
			}  
			else if (response && response.message) {
				msg+="Unable to " + methodDescription + "; unknown error: " + x.status + "<p>" + response.message + "</p>";
			}	
			else if (response) {
				msg+="Unable to " + methodDescription + "; unknown error: " + x.status + "<br><pre>Response: " + JSON.stringify(response, null, 4) + "</pre>";
			}
			else if (x.responseText) {
				msg+="Unable to " + methodDescription + "; unknown error: " + x.status + "<br><pre>Response Text: " + x.responseText + "</pre>";
			}
			else {
				msg+="Unable to " + methodDescription + "; unknown error: " + x.status + "<br><pre>No reponse text</pre>";
			}
			
			if (e && e.message) {
				errorPopup(msg + "<br><pre>" + e.message + "</pre>");
			}
			else {
				errorPopup(msg);
			}
		} // End of jqXHRError()
	);		
} // End of xhrGetMethod()

/*
 * Function: 	createMap()
 * Parameters: 	Bounding box, number of Zoomlevels
 * Returns: 	map
 * Description:	Create map, add Openstreetmap basemap and scale
 */	
function createMap(boundingBox, noZoomlevels) {
							
	consoleLog("Create Leaflet map; h x w: " + document.getElementById('map').style.height + "x" + document.getElementById('map').style.width);	
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
	
	if (boundingBox) {
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
	}			
	
	try {	
		consoleLog("Creating basemap...");															
		tileLayer=L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token=pk.eyJ1IjoibWFwYm94IiwiYSI6ImNpandmbXliNDBjZWd2M2x6bDk3c2ZtOTkifQ._QA7i5Mpkd_m30IGElHziw', {
			maxZoom: 11,
			attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, ' +
				'<a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, ' +
				'Imagery &copy; <a href="http://mapbox.com">Mapbox</a>',
			id: 'mapbox.light'
		});
		tileLayer.addTo(map);	
		L.control.scale().addTo(map); // Add scale	
		consoleLog("Added tileLayer and scale to map");	
	
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
} // End of createMap()
