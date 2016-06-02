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
var JSONLayer;
var start;
var jsonAddLayerParamsArray=[];
					
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
 * Function: 	setupMap()
 * Parameters: 	None
 * Returns: 	Nothing
 * Description:	Setup map width for Leaflet 
 */
function setupMap() {	
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
			new_w=w-850;
		} 
		else {
			new_w=w-500;
		}
		var new_h=h-150;
		var new_status_width=w-new_w-50;
	
		document.getElementById('map').style.width=new_w;
		document.getElementById('map').style.height=new_h;
		document.getElementById('status').style.height=new_h;
		document.getElementById('status').style.width=new_status_width;
		
		console.log("Size h x w: " + h + "+" + w +
			"; map size old: " + old_h + "+" + old_w + ", new: " + new_h + "+" + new_w +
			"; new status width: " + new_status_width);
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
 * Parameters: 	Status message, error message (optional), diagnostic (optional)
 * Returns: 	Nothing
 * Description:	Set status. Optional error message raised as an exception to halt processing 
 */	
function setStatus(msg, errm, diagnostic) {
	if (document.getElementById("status").innerHTML != msg) {
		var end=new Date().getTime();
		var elapsed=(end - start)/1000; // in S
		
		if (!errm) {
			document.getElementById("status").innerHTML = msg;
			if (diagnostic) {
				document.getElementById("status").innerHTML = 
					document.getElementById("status").innerHTML + 
					"<p>Processing diagnostic:</br><pre>" + diagnostic + "</pre></p>";
			}
			console.log("[" + elapsed + "] " + msg);
		}
		else {
			document.getElementById("status").innerHTML = "<h1>" + msg + "</h1><h2>Error message: " + errm + "</h2>";
			if (diagnostic) {
				document.getElementById("status").innerHTML = 
					document.getElementById("status").innerHTML + 
					"<p>Processing diagnostic:</br><pre>" + diagnostic + "</pre></p>";
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
	var elapsed=(end - start)/1000; // in S
									
	console.log("[" + elapsed + "] Create Leaflet map");	
	var map = new L.map('map' , {
			zoom: 9,
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
			map.on('zoomend', function (event) {
				zoomBasedLayerchange(event);
			});
			map.eachLayer(function(layer) {
				if (!layer.on) return;
				layer.on({
					loading: function(event) { console.log("Loading: " + layer); },
					load: function(event) { console.log("Loaded: " + layer); }
				}, this);
			});
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
		elapsed=(end - start)/1000; // in S		
		console.log("[" + elapsed + "] Creating basemap...");															
		tileLayer=L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token=pk.eyJ1IjoibWFwYm94IiwiYSI6ImNpandmbXliNDBjZWd2M2x6bDk3c2ZtOTkifQ._QA7i5Mpkd_m30IGElHziw', {
			maxZoom: 9,
			attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, ' +
				'<a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, ' +
				'Imagery &copy; <a href="http://mapbox.com">Mapbox</a>',
			id: 'mapbox.light'
		});
		tileLayer.addTo(map);	
		L.control.scale().addTo(map); // Add scale
	
		end=new Date().getTime();
		elapsed=(end - start)/1000; // in S		
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
		"<th>Geo/Topo JSON length</th>" +
		"<th>Areas</th>" + 
		"<th>Geo level</th>" +
		"</tr>";	
	for (var i=0; i < response.no_files; i++) {	
		msg+="<tr style=\"color:" + layerColours[i] + "\"><td>" + response.file_list[layerAddOrder[i]].file_name + "</td>" +
				"<td>" + (fileSize(response.file_list[layerAddOrder[i]].file_size) || "N/A") + "</td>";
		if (response.file_list[layerAddOrder[i]].topojson && response.file_list[layerAddOrder[i]].topojson[0].topojson_length) {	
			msg+="<td>" + (fileSize(response.file_list[layerAddOrder[i]].topojson[0].topojson_length) || "N/A ") + "/" + 
				(fileSize(response.file_list[layerAddOrder[i]].geojson_length) || " N/A") + "</td>";	
		}
		else if (response.file_list[layerAddOrder[i]].geojson) {	
			msg+="<td>" + fileSize(response.file_list[layerAddOrder[i]].geojson_length) + "</td>";							
		}		
		msg+="<td>" + response.file_list[layerAddOrder[i]].total_areas + "</td>" +
			"<td>" + (response.file_list[layerAddOrder[i]].geolevel_id || "N/A") + "</td>" + 
			"</tr>";								
	}		
	msg+="</table>";	

	return msg;
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
	var msg="";
	var JSONLayer=[];
	
	setStatus("Processing response from server...");
	if (responseText != null && typeof responseText == 'object') { // Already JSON
		response=responseText;
	}
	else { // Parse it
		try {
			response=JSON.parse(responseText);
		} catch (e) {  
			if (responseText) {
				setStatus("Send Failed, error parsing response", e, responseText);
			}
			else {		
				setStatus("Send Failed, no response from server", e);
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
							
							map.whenReady( // Basemap is ready
								function whenMapIsReady() { 
									var end=new Date().getTime();
									var elapsed=(end - start)/1000; // in S
									console.log("[" + elapsed + "] Basemap completed; zoomlevel: " +  map.getZoom());	

									setTimeout(	// Make async	
										function asyncEachSeries() { // Process map layers using async in order
											async.eachSeries(jsonAddLayerParamsArray, 
												function asyncEachSeriesHandler(item, callback) {
													setTimeout(jsonAddLayer, 200, item, JSONLayer, callback); // Put a slight delay in for Leaflet to allow the map to redraw
												}, 
												function asyncEachSeriesError(err) {
													var end=new Date().getTime();
													var elapsed=(end - start)/1000; // in S
													if (err) {
														console.error("[" + elapsed + "] asyncEachErrorHandler: " + err.message);								
													}
													else {
														console.log("[" + elapsed + "] " + response.no_files + " layers processed OK.");
													}
												} // End of asyncEachSeriesError()
											);						
										}, // End of asyncEachSeries()
										300);									
								} // End of whenMapIsReady()
							);
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
 *					isGeoJSON boolean }, JSONLayer array, async callback
 * Returns: 	Nothing
 * Description:	Remove then add geo/topoJSON layer to map
 */	
function jsonAddLayer(jsonAddLayerParams, JSONLayer, callback) { 
	var end=new Date().getTime();
	var elapsed=(end - start)/1000; // in S
	console.log("[" + elapsed + "] Adding data to JSONLayer[" + jsonAddLayerParams.i + "/" + jsonAddLayerParams.no_files + 
		"]; file layer [" + jsonAddLayerParams.layerAddOrder + "]: " +
		jsonAddLayerParams.file_name +
		"; colour: " + jsonAddLayerParams.style.color + "; weight: " + jsonAddLayerParams.style.weight + 
		"; opacity: " + jsonAddLayerParams.style.opacity + "; fillOpacity: " + jsonAddLayerParams.style.fillOpacity + "; zoomlevel: " +  map.getZoom());
							
	try {
		if (jsonAddLayerParams.JSONLayer[jsonAddLayerParams.i]) {	
			console.log("[" + elapsed + "] Remove topoJSONLayer" + jsonAddLayerParams.i + "]");
			map.removeLayer(jsonAddLayerParams.JSONLayer[jsonAddLayerParams.i]);
		}	
		try {	
			if (jsonAddLayerParams.isGeoJSON) { // Use the right function
				JSONLayer[jsonAddLayerParams.i] = L.geoJson(undefined /* Geojson options */, 
					jsonAddLayerParams.style).addTo(map);
			}
			else {
				JSONLayer[jsonAddLayerParams.i] = new L.topoJson(undefined /* Topojson options */, 
					jsonAddLayerParams.style).addTo(map);					
			}
			jsonAddLayerParams.JSONLayer[jsonAddLayerParams.i] = JSONLayer[jsonAddLayerParams.i];
			
			if (jsonAddLayerParams.json) {
				if (jsonAddLayerParams.jsonZoomlevel) {
					jsonAddLayerParams.JSONLayer[jsonAddLayerParams.i].addData(
						jsonZoomlevelData(
							jsonAddLayerParams.json, map.getZoom(), jsonAddLayerParams.i)
						);	
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
						elapsed=(end - start)/1000; // in S
						console.log("[" + elapsed + "] Added JSONLayer [" + jsonAddLayerParams.i  + "/" + jsonAddLayerParams.no_files + 
							"]: " + jsonAddLayerParams.file_name + "; zoomlevel: " +  map.getZoom());
				//		console.log("Callback: " + jsonAddLayerParams.i);
						callback();
					}, this); 
			}
			else {
				throw new Error("jsonAddLayer(): jsonAddLayerParams.json is not defined.");
			}	

		}
		catch (e) {
			end=new Date().getTime();
			elapsed=(end - start)/1000; // in S
			throw new Error("[" + elapsed + "] Error adding JSON layer [" + jsonAddLayerParams.i  + "/" + jsonAddLayerParams.no_files + "] to map: " + e.message);
		}			
	}			
	catch (e) {
		end=new Date().getTime();
		elapsed=(end - start)/1000; // in S
		callback(new Error("[" + elapsed + "] Error removing JSON layer [" + jsonAddLayerParams.i  + "/" + jsonAddLayerParams.no_files + "]  map: " + e.message));
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
			if (!firstKey) { // Save first key so there is one good match!
				firstKey=key;
			}
			
			if (minZoomlevel == undefined) {
				minZoomlevel=key;
			}
			else if (key < minZoomlevel) {
				minZoomlevel=key;
			}
			if (maxZoomlevel == undefined) {
				maxZoomlevel=key;
			}
			else if (key > maxZoomlevel) {
				maxZoomlevel=key;
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
		
		if (json == undefined && mapZoomlevel > maxZoomlevel) {
			console.log("Layer [" + layerNum + "]: no json found for zoomlevel: " + mapZoomlevel + "; using maxZoomlevel key: " + maxZoomlevel);
			json=jsonZoomlevels[maxZoomlevel];
		}	
		
		if (json == undefined && mapZoomlevel < minZoomlevel) {
			console.log("Layer [" + layerNum + "]: no json found for zoomlevel: " + mapZoomlevel + "; using minZoomlevel key: " + minZoomlevel);
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
		
		if (!json) {
			throw new Error("jsonZoomlevelData(): Layer [" + layerNum + "]: no json available");
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
function errorHandler(error) {
	if (error) {
		console.error(JSON.stringify(error, null, 4));
		var msg="<h1>Send Failed; http status: ";
		if (error.status) {
			msg+=error.status;
		}
		else {
			msg+="(no error status)";
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
	else {
		console.error("errorHandler(): No error returned");
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
}

/*
 * Function: 	uploadProgressHandler()
 * Parameters:  event, position, total, percentComplete
 * Returns: 	Nothing
 * Description:	Upload progress handler for JQuery form
 */
function uploadProgressHandler(event, position, total, percentComplete) {
	var msg;
	
	if (percentComplete == 100) {
		msg="Uploaded: " + percentComplete.toString() + '%; ' + fileSize(position) + "/" + fileSize(total);
	}
	else {
		msg="Uploading: " + percentComplete.toString() + '%; ' + fileSize(position) + "/" + fileSize(total);
	}
	document.getElementById('status').innerHTML = msg;
	console.log(msg);
}

/*
 * Function: 	zoomBasedLayerchange()
 * Parameters:  Event object
 * Returns: 	Nothing
 * Description:	Change all map layer to optimised topoJSON for that zoomlevel 
 */
function zoomBasedLayerchange(event) {
	
//    $("#zoomlevel").html(map.getZoom());
    var currentZoom = map.getZoom();
    console.log("New zoomlevel: " + currentZoom + "; event: " + event.type);
    switch (currentZoom) {
/*        case 11:
            clean_map();
            coorsLayer.addTo(map); //show Coors Field
            $("#layername").html("Coors Field");
            break; */

        default:
            // do nothing
            break;
    }
}
