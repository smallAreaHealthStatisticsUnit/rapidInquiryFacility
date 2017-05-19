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
var map;
var topojsonTileLayer;
var baseLayer;
var legend;
var mouseoverPopup;
var topojsonTileLayerCount=0;
var LegendDiv;
var basemaps;
var pouchDBErrors = {};

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
	
	/* OR: https://gist.github.com/brendanvinson/0e3c3c86d96863f1c33f55454705bca7

L.TopoJSON = L.GeoJSON.extend({
    addData: function (data) {
        var geojson, key;
        if (data.type === "Topology") {
            for (key in data.objects) {
                if (data.objects.hasOwnProperty(key)) {
                    geojson = topojson.feature(data, data.objects[key]);
                    L.GeoJSON.prototype.addData.call(this, geojson);
                }
            }

            return this;
        }

        L.GeoJSON.prototype.addData.call(this, data);

        return this;
    }
});

L.topoJson = function (data, options) {
    return new L.TopoJSON(data, options);
};

	 */
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
	try {
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
	catch (err) {
		console.log("consoleError: ERROR: " + err.message + "\n" + msg);
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
 * Parameters: 	method name, method description, method callback, method fields object, optional error callback
 * Returns: 	Nothing
 * Description:	Generic XHR GET method
 */
function xhrGetMethod(methodName, methodDescription, methodCallback, methodFields, xhrGetMethodErrorCallback) {
	scopeChecker({
		methodName: methodName, 
		methodDescription: methodDescription, 
		callback: methodCallback, 
		methodFields: methodFields
	}, {
		callback: xhrGetMethodErrorCallback
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
					msg+="<pre>" + response.message + "</pre>";
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
			
			if (response.sqlError) {
				consoleError("SQL Error: " + JSON.stringify(response.sqlError, null, 2));
			}
			if (response && response.diagnostic) {
				errorPopup(msg, "<pre>" + response.diagnostic + "</pre>");
			}
			else if (e && e.message) {
				errorPopup(msg, "<pre>" + e.message + "</pre>");
			}
			else {
				errorPopup(msg);
			}
			
			if (xhrGetMethodErrorCallback) {
				var nerr=new Error("jqXHRError(): " + msg);
				xhrGetMethodErrorCallback(nerr);
			}
		} // End of jqXHRError()
	);		
} // End of xhrGetMethod()

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
 * Function: 	addSelector()
 * Parameters: 	selectorId, selectorChangeCallback, newHTML, defaultValue
 * Returns: 	map
 * Description:	Add JQuery selector
 */	
function addSelector(selectorId, selectorChangeCallback, newHTML, defaultValue) {
	scopeChecker({
		selectorId: selectorId,
		newHTML: newHTML,
		callback: selectorChangeCallback}
	);
		
	var buttonObject=$( selectorId ).button( "instance" );
	if (buttonObject) {
		$( selectorId ).html(newHTML);		
		var checkedValue=$( selectorId + " option:checked" ).val();
		
		$( selectorId ).button( "refresh" );
		$( selectorId ).selectmenu( "refresh" );
			
		if (defaultValue) {
			$( selectorId ).val(defaultValue);			
//			consoleLog("addSelector(): refresh with defaultValue: " + selectorId + 
//				"; val: " + $( selectorId ).val() + " ; checkedValue: " + checkedValue + 
//				"; HTML>\n" + newHTML);
		}
		else {
			$( selectorId ).val(checkedValue);
//			consoleLog("addSelector(): refresh with checkedValue: " + selectorId + 
//				"; val: " + $( selectorId ).val() + " ; checkedValue: " + checkedValue + 
//				"; HTML>\n" + newHTML);
		}			
	}
	else {
		$( selectorId ).html(newHTML);		
		var checkedValue=$( selectorId + " option:checked" ).val();

//		consoleLog("addSelector(): pre-create: " + selectorId + 
//			"; val: " + $( selectorId ).val()   + " ; checkedValue: " + checkedValue + 
//			" ; HTML>\n" + newHTML);
			
		$( selectorId )									// Create selector
			.selectmenu({
				change: function( event, ui ) {			// DB Change function
					selectorChangeCallback( event, ui );
				}
			})
			.selectmenu( "menuWidget" ).addClass( "overflow" );
		$( selectorId ).button();

		if (defaultValue) {
			$( selectorId ).val(defaultValue);
//			consoleLog("addSelector(): create with defaultValue: " + selectorId + 
//				"; val: " + $( selectorId ).val() + " ; checkedValue: " + checkedValue + 
//				"; HTML>\n" + newHTML);
		}
		else {
			$( selectorId ).val(checkedValue);
//			consoleLog("addSelector(): create with checkedValue: " + selectorId + 
//				"; val: " + $( selectorId ).val() + " ; checkedValue: " + checkedValue + 
//				"; HTML>\n" + newHTML);
		}		
	}
		
	$( selectorId ).tooltip(									// Enable tooltips
		{
			position: { 					
				my: "right bottom+50"
			},
			tooltipClass: "entry-tooltip-positioner"
		}
	);	
	
	selectorChangeCallback();	// Defaults
} // End of addSelector()

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
 * Function: 	setHeight()
 * Parameters: 	id, height
 * Returns: 	nothing
 * Description:	Set object height
 */
function setHeight(id, lheight) {
	if (document.getElementById(id)) {
		document.getElementById(id).setAttribute("style","display:block;cursor:pointer;cursor:hand;");
		document.getElementById(id).setAttribute("draggable", "true");
		document.getElementById(id).style.display = "block"	;
		document.getElementById(id).style.cursor = "hand";			
		document.getElementById(id).style.height=lheight + "px";						
		
//		consoleLog("setHeight(" + id + "," + lheight + ") h x w: " + 
//			document.getElementById(id).offsetHeight + "x" + document.getElementById(id).offsetWidth);	
	}
	else {
		consoleError("setHeight() " + id + " not found");
	}
} // End of setHeight()
		
/*
 * Function: 	createMap()
 * Parameters: 	Bounding box, max Zoomlevel
 * Returns: 	map
 * Description:	Create map, add Openstreetmap basemap and scale
 */	
function createMap(boundingBox, maxZoomlevel) {
		
	if (map == undefined) {
		consoleLog("Create Leaflet map; h x w: " + document.getElementById('map').style.height + "x" + 
			document.getElementById('map').style.width + "; version: " + L.version);	
		map = new L.map('map' , {
//				zoom: maxZoomlevel,
				maxZoom: 21 /* maxZoomlevel */,
				preferCanvas: true,
				// Tell the map to use a fullsreen control
				fullscreenControl: true,
				// loadingControl: true
				// Does not work with Leaflet 1.0: probably lack of 'done' callbacks in addLayer()'s 
			} 	
		);
		L.control.scale({position: 'topleft'}).addTo(map); // Add scale	
	}	
	else {
		consoleLog("Leaflet map already created; h x w: " + document.getElementById('map').style.height + "x" + document.getElementById('map').style.width);	
	}
	
	if (boundingBox) {
		try {
			consoleLog("Fit bounding box: " + JSON.stringify(boundingBox));	
			map.fitBounds([
				[boundingBox.ymin, boundingBox.xmin],
				[boundingBox.ymax, boundingBox.xmax]]
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
		if (baseLayer) {
			consoleLog("Redrawing basemap...");	
			map.invalidateSize();
			baseLayer.redraw();
		}
		else {
			var defaultBaseMap="OpenStreetMap Mapnik";
			consoleLog("Creating basemap: " + defaultBaseMap);	
	
			basemaps=new mapArrays(map, defaultBaseMap, maxZoomlevel);		
					
			if (L.version >= "1.0.0") { // Leaflet 0.7 code
//				addGridLayer(); 	// Add grid layer Leaflet 1.0+
			}
/*			
			(function mapResetButton() {
				var control = new L.Control({position:'topright'});
				control.onAdd = function mapResetButtonControl(map) {
						var azoom = L.DomUtil.create('a','resetzoom');
						azoom.innerHTML = "[Reset Zoom]";
						L.DomEvent
							.disableClickPropagation(azoom)
							.addListener(azoom, 'click', function mapResetButtonListener() {
								consoleLog("Map reset");	
								map.setView(map.options.center, map.options.zoom);
							},azoom);
						return azoom;
					};
				return control;
			}())
			.addTo(map); */
		}
		
		return map;
	}
	catch (e) {
		try {
			map.remove();
		}
		catch (e2) {
			consoleLog("WARNING! Unable to remove map during error recovery");
		}		
		var nerr=new Error("Unable to add tile layer to map: " + e.message);
		nerr.stack=e.stack;
		throw nerr;
	}
} // End of createMap()
 
/*
 * Function: 	addTileLayer()
 * Parameters: 	methodFields object, max zoom level
 * Returns: 	tile layer
 * Description:	Add tile layer to map; remove old layer if required
 */
function addTileLayer(methodFields, maxzoomlevel) {
	if (map == undefined) {
		return undefined;
	}
	
    var style = {
        "clickable": true,
        "color": "#00D",
        "fillColor": "#81aefa",
        "weight": 3.0,
        "opacity": 0.7,
        "fillOpacity": 0.02
    };
//    var hoverStyle = {
//       "fillOpacity": 0.5
//    };
/*
 * Example URL:
 *
 * 127.0.0.1:3000/getMapTile/?zoomlevel=1&x=0&y=0&databaseType=PostGres&table_catalog=sahsuland_dev&table_schema=peter&table_name=geography_sahsuland&geography=SAHSULAND&geolevel_id=2&tiletable=tiles_sahsuland
 */
    var topojsonURL = 'http://127.0.0.1:3000/getMapTile/?zoomlevel={z}&x={x}&y={y}';
	for (var key in methodFields) { // append methodFields
		if (key != 'geolevel') {
			topojsonURL+='&' + key + '=' + methodFields[key];
		}
	}
	var geolevel=methodFields.geolevel[(methodFields.geolevel_id-1)];
	geolevel.databaseType=methodFields.databaseType;
	geolevel.databaseName=methodFields.table_catalog;
	geolevel.tableName=methodFields.table_schema + '.' + methodFields.table_name;
	geolevel.table_description=methodFields.table_description;
	geolevel.geography=methodFields.geography;
	geolevel.maxzoomlevel=methodFields.maxzoomlevel;
	geolevel.output="GeoJSON";
	
// This overrides to use the database maxzoomlevel figure	
//	map.options.maxZoom = methodFields.maxzoomlevel;
	
//	consoleLog("geolevel data: " + JSON.stringify(geolevel, null, 0));
	consoleLog("topojsonURL: " + topojsonURL);
	
	if (topojsonTileLayer && map) { // Remove topoJSON layer
		map.removeLayer(topojsonTileLayer);
	}
		
	if (mouseoverPopup && map) { // Remove old popup
		map.closePopup(mouseoverPopup);
		mouseoverPopup = null;
	}
	
	if (L.version < "1.0.0") { // Leaflet 0.7 code
		topojsonTileLayerCount++;

		topojsonTileLayer = new L.TileLayer.GeoJSON(topojsonURL, {
				clipTiles: true,
				attribution: 'Tiles &copy; <a href="http://www.sahsu.org/content/rapid-inquiry-facility">Imperial College London</a>',
				unique: function (feature) {
					return feature.id; 
				},
				maxZoom: maxzoomlevel
			}, {
				style: style,
				onEachFeature: createPopup
			}
		);	
	} // End of Leaflet 0.7 code
	else { // Leadflet 1.0 code - uses topoJSON
		geolevel.output="TopoJSON";
		topojsonURL+='&output=topojson';

		topojsonTileLayerCount++;
		
		topojsonTileLayer = new L.topoJsonGridLayer(topojsonURL, {
				attribution: 'Tiles &copy; <a href="http://www.sahsu.org/content/rapid-inquiry-facility">Imperial College London</a>',
                layers: {
					default: { // If not using a feature collection (which we are not)
						style: style,
						onEachFeature: createPopup
					} 
                },
				name: geolevel.databaseType + "." + geolevel.databaseName + "." + geolevel.tableName + "." + geolevel.geolevel_name, 
											// Should be unique (includes schema and geolevel name)
				maxZoom: maxzoomlevel,
				useCache: true,				// Use PouchDB caching
				auto_compaction: autoCompaction	
			}
		);
	}
	geolevel.cacheStats={
		hits: 0,
		misses: 0,
		errors: 0,
		tiles: 0,
		size: 0
	};	
	topojsonTileLayer.on('tileerror', function(error, tile) {
		var msg="";
		if (error && error.message) {
			msg+=error.message;
		}
		if (tile) {
			consoleError("Error: loading topoJSON tile: " + 
				(JSON.stringify(tile.coords)||"UNK"));		
		}
	});
	topojsonTileLayer.on('tilecacheerror', function tileCacheErrorHandler(ev) {
		if (geolevel && geolevel.cacheStats) {
			geolevel.cacheStats.errors++;
		}
	});
	topojsonTileLayer.on('tilecachemiss', function tileCacheErrorHandler(ev) {
		if (geolevel && geolevel.cacheStats) {
			geolevel.cacheStats.misses++;
		}
	});
	topojsonTileLayer.on('tilecachehit', function tileCacheErrorHandler(ev) {
		if (geolevel && geolevel.cacheStats) {
			geolevel.cacheStats.hits++;
		}
	});
	
	topojsonTileLayer.on('load', function topojsonTileLayerLoad() {
		consoleLog("Topojson tile layer " + topojsonTileLayerCount + " loaded; geolevel_id: " + methodFields.geolevel_id +
			"; zoom: " + map.getZoom() +
			"; map maxZoom: " + map.getMaxZoom() +
			"; layer maxZoom: " + topojsonTileLayer.options.maxZoom);
		if (topojsonTileLayer && topojsonTileLayer.PouchDBError && topojsonTileLayer.PouchDBError.error) {
			if (! pouchDBErrors[topojsonTileLayer.PouchDBError.name] ) {
				errorPopup(topojsonTileLayer.PouchDBError.reason, "<pre>" + JSON.stringify(topojsonTileLayer.PouchDBError, null, 0) + "</pre>");
			}
			pouchDBErrors[topojsonTileLayer.PouchDBError.name]=true;
			topojsonTileLayer.options.useCache=false;
		}
	});
	
	map.addLayer(topojsonTileLayer);
	map.whenReady( // Map is ready, topojsonTileLayer still loading
		function whenMapIsReady3() { 
			if (baseLayer && baseLayer.cacheStats && baseLayer.cacheStats.errors > 0 && baseLayer.cacheStats.useCache == false) { // Detect baseLayer caching errors
				topojsonTileLayer.options.useCache=false;
			}
			consoleLog("Adding topojson tile layer; maxZoom: " + map.getMaxZoom()+ "; cached: " + (topojsonTileLayer.options.useCache ? "true" : "false"));
			
			if (legend) {
				map.removeControl(legend);
			}
			legend = L.control({position: 'bottomleft'});
					
			var keyTable = {	// Translate tags
				geolevel_id: 		"GEOLEVEL_ID",
				geolevel_name: 		"Name",
				table_description:	"Table Description",
				description: 		"Geolevel Description",
				areaid_count: 		"AreaID Count",
				databaseType:		"Database Type",
				databaseName:		"Database Name",
				tableName:			"Table",
				geography:			"Geography",
				maxzoomlevel:		"Max zoomlevel",
				output:				"Tile format",
				baseLayer:			"Base layer",
				BasemapCaching:		"Basemap caching enabled"
			}
			legend.onAdd = function onAddLegend(map) {
				LegendDiv = L.DomUtil.create('div', 'info legend');
				var labels=[];

				for (var key in geolevel) {
					if (key != "cacheStats") {
						labels.push('<tr><td id="legend_' + key + '">' + (keyTable[key]||key) + 
							': </td><td id="legend_' + key + '_value">' + geolevel[key] + "</td></tr>");			
					}
					else {
						labels.push('<tr><td id="legend_' + key + '">' + (keyTable['BasemapCaching']||'BasemapCaching') + 
							': </td><td id="legend_' + key + '_value">' + 
							(baseLayer.options.useCache ? "true" : "false") + "</td></tr>");				
					}					
				}
				labels.push('<tr><td id="legend_baseLayer">' + (keyTable["baseLayer"]) + 
					': </td><td id="legend_baseLayer_value">' + (baseLayer && baseLayer.name || "UNKNOWN") + "</td></tr>");	
				
				var html = '<table id="legend">' + labels.join("") + '</table>';
		//		consoleLog("Add legend: " + html);
				LegendDiv.innerHTML = html;
				return LegendDiv;
			};
								
			legend.addTo(map);
	
		}
	);

	return topojsonTileLayer;
} // End of addTileLayer()

/*
 * Function:	getTopojsonTileLayerStats()
 * Parameters:	nonBasemapCacheStats
 * Returns:		Cache rows html
 * Description: Get topojsonTileLayer stats
 */	
function getTopojsonTileLayerStats(nonBasemapCacheStats) {
	var tableHtml="";
	var keys=Object.keys(nonBasemapCacheStats);
	
	for (var i=0; i<methodFields.geolevel.length; i++) {
		var geolevel=methodFields.geolevel[i];
		if (geolevel.databaseType && geolevel.databaseName && geolevel.tableName && geolevel.cacheStats) {
			var name=geolevel.databaseType + "." + geolevel.databaseName + "." + geolevel.tableName + "." + geolevel.geolevel_name; 
				// Of tilelayer geolevel
			var found=false;
			
			for (var j=0; j<keys.length; j++) {
				if (name && keys[j] && name == keys[j]) {	
					nonBasemapCacheStats[name].found=true;
					found=true;
					if (topojsonTileLayer.options.name == name) {
						tableHtml+='  <tr>\n' +
							'    <td>' + name + ' [Current TopoJSON]</td>\n' +
							'    <td>' + geolevel.cacheStats.hits + '</td>\n' +
							'    <td>' + geolevel.cacheStats.misses + '</td>\n' +
							'    <td>' + geolevel.cacheStats.errors +  '</td>\n' +
							'    <td>' + nonBasemapCacheStats[name].tiles + '</td>\n' +
							'    <td>' + (fileSize(nonBasemapCacheStats[name].size)||'N/A') + '</td>\n' +	
							'  </tr>';	
					}
					else  {
						tableHtml+='  <tr>\n' +
							'    <td>' + name + '</td>\n' +
							'    <td>' + geolevel.cacheStats.hits + '</td>\n' +
							'    <td>' + geolevel.cacheStats.misses + '</td>\n' +
							'    <td>' + geolevel.cacheStats.errors +  '</td>\n' +
							'    <td>' + nonBasemapCacheStats[name].tiles + '</td>\n' +
							'    <td>' + (fileSize(nonBasemapCacheStats[name].size)||'N/A') + '</td>\n' +	
							'  </tr>';	
					}
					consoleLog('[' + j + '] Found nonBasemapCacheStats[' + keys[j] + ']; name: ' + name + '; ' + 
						JSON.stringify(nonBasemapCacheStats[name]));
					break;					
				}				
				else {	
					consoleLog('[' + j + '] Ignored previously found nonBasemapCacheStats[' + keys[j] + ']; name: ' + name + '; ' + 
						JSON.stringify(nonBasemapCacheStats[name]));
				}
			}
		
			if (found == false) {
				tableHtml+='<tr>\n'+
					'    <td>' + name + '</td>\n' +
					'    <td>' + geolevel.cacheStats.hits + '</td>\n' +
					'    <td>' + geolevel.cacheStats.misses + '</td>\n' +
					'    <td>' + geolevel.cacheStats.errors +  '</td>\n' +
					'    <td>N/A</td>\n' +				
					'    <td>N/A</td>\n' +
					'  </tr>';					
			}			
		}
	}
		
	for (var i=0; i<keys.length; i++) {
		if (nonBasemapCacheStats[keys[i]].found == false) {
			tableHtml+='  <tr>\n' +
			'    <td>' + keys[i] + '</td>\n' +
			'    <td>0</td>\n' + // Hits
			'    <td>0</td>\n' + // Misses
			'    <td>0</td>\n' + // Errors
			'    <td>' + nonBasemapCacheStats[keys[i]].tiles + '</td>\n' +
			'    <td>' + (fileSize(nonBasemapCacheStats[keys[i]].size)||'N/A') + '</td>\n' +
			'  </tr>';	
			consoleLog('nonBasemapCacheStats[' + keys[i] + ']: ' + JSON.stringify(nonBasemapCacheStats[keys[i]]));
		}
		else {
			consoleLog('Ignored nonBasemapCacheStats[' + keys[i] + ']');
		}
	}
	
	return tableHtml;
 }
 
/*
 * Function:	createPopup()
 * Parameters:	GeoJSON feature, layer
 * Returns:		Nothing
 * Description: Create Leaflet popup from geoJSON features using onEachFeature option
 */	
function createPopup(feature, layer) {
	var popop=[];
	var errorArray=[];
	var latlng;
	var id = feature.id ? feature.id : feature.properties.id;
	
	for (var key in feature.properties) { // Process properties
		if (key == "geographic_centroid") {
			if (typeof feature.properties[key] == "object") {
				if (feature.properties[key].type && 
					feature.properties[key].type == "Point" && 
					feature.properties[key].coordinates) {
					popop.push("<tr><td>" + (key) + ": </td><td>" +
						JSON.stringify(feature.properties[key].coordinates, null, 2) + "</td></tr>");
					latlng=L.latLng({
						lat: feature.properties[key].coordinates[1], 
						lng: feature.properties[key].coordinates[0]
					});
				}
				else {
					errorArray.push("createPopup() Unknown feature.properties[key] JSON: " +
						JSON.stringify(feature.properties[key]) + 
						"; for: " + JSON.stringify(feature.properties, null, 2));	
				}
			}
			else if (feature.properties[key]) { // Parse: caused by bug in tileMaker
				var geographic_centroid=feature.properties[key];
				
				try {
					geographic_centroid=JSON.parse(feature.properties[key]);
					if (geographic_centroid.type && 
						geographic_centroid.type == "Point" && 
						geographic_centroid.coordinates) {
						popop.push("<tr><td>" + (key) + ": </td><td>" +
							JSON.stringify(geographic_centroid.coordinates, null, 2) + "</td></tr>");
							latlng=L.latLng({
								lat: geographic_centroid.coordinates[1], 
								lng: geographic_centroid.coordinates[0]
							});	
						errorArray.push("createPopup() Had to parse stringified JSON: " +
							JSON.stringify(geographic_centroid) + 
							"; for: " + JSON.stringify(feature.properties, null, 2));
					}
					else {
						errorArray.push("createPopup() Unknown geographic_centroid JSON: " +
							JSON.stringify(geographic_centroid) + 
							"; for: " + JSON.stringify(feature.properties, null, 2));									
					}
				}
				catch (e) {
					consoleError("createPopup() cannot parse geographic_centroid: " + e.message, 
						"feature.properties[key]: " + 
						feature.properties[key] + 
						"; for: " + JSON.stringify(feature.properties, null, 2));
				}
			}	
			else {
				errorArray.push("createPopup() No centroid for: " + JSON.stringify(feature.properties, null, 2));	
			}					
		}
		else if (key == "zoomlevel") {
			// Remove - as confusing
		}
		else {
			popop.push("<tr><td>" + (key) + ": </td><td>" + feature.properties[key] + "</td></tr>");	
		}		
		
	} // End of for loop				
	var html = '<a><table id="popups">' + (popop.join("")||"No data, id: " + feature.properties.id) + '</table></a>';
//	consoleLog("Popup HTML, id: " + feature.properties.id + " >>>\n" + html);
	
//	layer.bindPopup(html);			// Non dynamic popup
	layer.on('click', function(e) { // Create popup dynamically. Only one at a time is allowed by default
		if (mouseoverPopup && map) { // Remove old popup
			map.closePopup(mouseoverPopup);
			mouseoverPopup = null;
		}
		mouseoverPopup = L.popup()
			.setLatLng(latlng || e.latlng) 
			.setContent(html)
			.openOn(map);
	});	
	
	if (errorArray.length > 0) {
		errorPopup(new Error("createPopup() tile id " + 0 + ": " + errorArray.length + " error(s) adding: " + 
			feature.length + " dynamic popups"), "<pre>" + errorArray.join("\n") + "</pre>");
	}

} // End of createPopup()
			
/*
 * Function:	getBbox()
 * Parameters:	databaseType, databaseName, databaseSchema, geography, table_name, tileTable, getBboxCallback
 * Returns:		Nothing
 * Description: Get boundoiing box from root tile
 */	
function getBbox(databaseType, databaseName, databaseSchema, geography, table_name, tileTable, getBboxCallback) {
	scopeChecker({
		callback: getBboxCallback,
		databaseType: databaseType,
		databaseName: databaseName,			
		databaseSchema: databaseSchema,
		table_name: table_name,
		geography: geography,
		tileTable: tileTable
	});
	//http://127.0.0.1:3000/getMapTile/?zoomlevel=1&x=0&y=0&databaseType=PostGres&table_catalog=sahsuland_dev
	// &table_schema=peter&table_name=geography_sahsuland&geography=SAHSULAND
	// &geolevel_id=2&tiletable=tiles_sahsuland&output=topojson
	var getMapTileParams={ 
			zoomlevel: 0, 
			x: 0,
			y: 0,
			geolevel_id: 1,
			databaseType: databaseType,	
			table_catalog: databaseName,
			table_schema: databaseSchema,
			table_name: table_name,
			geography: geography,
			tiletable: tileTable,
			output: "topojson"
		};
	consoleLog("getBbox() getMapTileParams: " + JSON.stringify(getMapTileParams, null, 0));
	
	var jqXHRgetBboxStatus=$.get("getMapTile", getMapTileParams,
		function getBboxStatus(data, status, xhr) {
			consoleLog("getBboxStatus() data: " + JSON.stringify(data, null, 0).substring(1, 100));
			// "type":"Topology","objects":{"collection":{"type":"GeometryCollection","bbox":[-7.58829480400111,52
			var bbox;
			if (data && data.objects && data.objects.collection) {
				bbox={
					xmin: data.objects.collection.bbox[0],
					ymin: data.objects.collection.bbox[1],
					xmax: data.objects.collection.bbox[2],
					ymax: data.objects.collection.bbox[3]
				};
			}
			else {
				bbox={xmin: -180.0000, ymin: -90.0000, xmax: 180.0000, ymax: 90.0000}; /* whole world bounding box */			
			}
			getBboxCallback(bbox);
		}, // End of getBboxStatus() 
		"json");
	jqXHRgetBboxStatus.fail(function getBboxError(x, e) {
		try {
			if (x.responseText) {
				response=JSON.parse(x.responseText);
			}
			errorPopup(response.message, 
				"<pre>" + response.diagnostic + "</pre><pre>Fields: " + 
				JSON.stringify(response.fields, null, 2) + "</pre>");
		}
		catch (e) {
			errorPopup("getBbox(): Error parsing response: " + e.message);
		}
	} // End of getBboxError()
	);
} // End of getBbox()

/*
 * Function:	addGridLayer()
 * Parameters:	None
 * Returns:		Nothing
 * Description: Leaflet GridLayer example with Canvas
 *				By Maik Riechert: https://gist.github.com/letmaik/e71eae5b3ae9e09f8aeb288c3b95230b
 *
 *				Needs Leaflet 1.0+
 *
 * 				See also: https://github.com/ebrelsford/leaflet-geojson-gridlayer/tree/master/src
 */	
function addGridLayer() {
	
	var tiles = new L.GridLayer();
	tiles.createTile = function(coords) {
	  var tile = L.DomUtil.create('canvas', 'leaflet-tile');
	  var ctx = tile.getContext('2d');
	  var size = this.getTileSize()
	  tile.width = size.x
	  tile.height = size.y
	  
	  // calculate projection coordinates of top left tile pixel
	  var nwPoint = coords.scaleBy(size)
	  
	  // calculate geographic coordinates of top left tile pixel
	  var nw = map.unproject(nwPoint, coords.z)
	  
	  ctx.fillStyle = 'white';
	  ctx.fillRect(0, 0, size.x, 50);
	  ctx.fillStyle = 'black';
	  ctx.fillText('x: ' + coords.x + ', y: ' + coords.y + ', zoom: ' + coords.z, 20, 20);
	  ctx.fillText('lat: ' + nw.lat + ', lon: ' + nw.lng, 20, 40);
	  ctx.strokeStyle = 'red';
	  ctx.beginPath();
	  ctx.moveTo(0, 0);
	  ctx.lineTo(size.x-1, 0);
	  ctx.lineTo(size.x-1, size.y-1);
	  ctx.lineTo(0, size.y-1);
	  ctx.closePath();
	  ctx.stroke();
	  return tile;
	}
	
	tiles.addTo(map);
} // End of addGridLayer()
