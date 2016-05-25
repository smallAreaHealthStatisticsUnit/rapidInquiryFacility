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
		
		document.getElementById('status').innerHTML = "Size h x w: " + h + "+" + w +
			"; map size old: " + old_h + "+" + old_w + ", new: " + new_h + "+" + new_w +
			"; new status width: " + new_status_width
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

// Display file size nicely	
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

function isIE() {
	var myNav = navigator.userAgent.toLowerCase();
	return (myNav.indexOf('msie') != -1) ? parseInt(myNav.split('msie')[1]) : false;
}
	
function createMap(boundingBox) {
	console.log('New map');	
	var map = new L.map('map');
	var msg;
	
	try {
		map.fitBounds([
			[boundingBox.ymin, boundingBox.xmin],
			[boundingBox.ymax, boundingBox.xmax]], {maxZoom: 11}
		);
	}
	catch (e) {
		msg="Unable to create map: " + e.message;
		console.error(msg);
		document.getElementById("status").innerHTML = msg;	
		map.remove(); 
		return undefined;
	}
	
	try {
		document.getElementById("status").innerHTML = "Creating basemap...";															
		console.log('New tileLayer');
		tileLayer=L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token=pk.eyJ1IjoibWFwYm94IiwiYSI6ImNpandmbXliNDBjZWd2M2x6bDk3c2ZtOTkifQ._QA7i5Mpkd_m30IGElHziw', {
			maxZoom: 9,
			attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, ' +
				'<a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, ' +
				'Imagery &copy; <a href="http://mapbox.com">Mapbox</a>',
			id: 'mapbox.light'
		});
		tileLayer.addTo(map);	
		L.control.scale().addTo(map); // Add scale
	
		console.log('Added tileLayer and scale to map');	
	
		return map;
	}
	catch (e) {
		msg="Unable to add tile layer to map: " + e.message;
		console.error(msg);
		document.getElementById("status").innerHTML = msg;	
		map.remove(); 
		return undefined;
	}

}

/*
 * Function: 	displayResponse()
 * Parameters: 	Response text (JSON as a string), status code
 * Returns: 	Nothing
 * Description:	Display reponse from submit form
 */
function displayResponse(responseText, status, formName) {
	var response;
	var msg="";
	var responseErrors=0;
	var JSONLayer=[];
	
	document.getElementById("status").innerHTML = "Processing response from server...";
	if (responseText != null && typeof responseText == 'object') { // Already JSON
		response=responseText;
	}
	else { // Parse it
		try {
			response=JSON.parse(responseText);
		} catch (e) {  
			if (responseText) {
				document.getElementById("status").innerHTML = "<h1>Send Failed</h1><h2>Error parsing response: " +  
					e.message +"</h2><p>Reponse:" + responseText + "</p>";
			}
			else {
				document.getElementById("status").innerHTML = "<h1>Send Failed</h1><h2>Error parsing response: " +  
					e.message +"</h2><p>no response from server</p>";
			}
			return;
		}
	}
	
	if (response.error) {
		msg+="<p>Error message" + response.error + "</p>"
	}

	if (!response.no_files) {
		msg+="<p>ERROR! No processed files returned</p>";
	}
	else {
		msg+="<p>Files processed: " + response.no_files;
		if (!response.file_list) {
			msg+="; no file list";	
		}
		else {
			if (response.no_files > 0) {
				if (!response.file_list[0]) {
					msg+="</br>First file in list (size: " + response.no_files + ") is not defined";
					console.error("ERROR! First file in list (size: " + response.no_files + ") is not defined");	
				}
				else if (!response.file_list[0].file_name) {
					msg+="</br>File bane of first file in list (size: " + response.no_files + ") is not defined";
					console.error("ERROR! File bane of first file in list (size: " + response.no_files + ") is not defined");
				}	
				else {
					msg+="<table border=\"1\" style=\"width:100%\">" + 
					"<tr>" +
					"<th>File</th>" + 
					"<th>Size</th>" +
					"<th>Geo/Topo JSON length</th>" +
					"<th>Areas</th>" + 
					"<th>Geo level</th>" +
					"</tr>";			

					for (var i=0; i < response.no_files; i++) {						
						if (!response.file_list[i].boundingBox) {
							if (response.file_list[i].topojson && 
								response.file_list[i].topojson.objects && 
								response.file_list[i].topojson.objects.collection && 
								response.file_list[i].topojson.objects.collection.bbox) {
								console.log("File [" + i + "]: Using topojson bounding box");	
								
								response.file_list[i].boundingBox={
									xmin: response.file_list[i].topojson.objects.collection.bbox[0], 
									ymin: response.file_list[i].topojson.objects.collection.bbox[1], 
									xmax: response.file_list[i].topojson.objects.collection.bbox[2], 
									ymax: response.file_list[i].topojson.objects.collection.bbox[3]};										
							}
							else {
								console.log("File [" + i + "]: bounding box is not defined; using whole world as bounding box");								
								response.file_list[i].boundingBox={xmin: -180, ymin: -85, xmax: 180, ymax: 85};
							}
						}
					}
			
					if (!map) {
						map=createMap(response.file_list[0].boundingBox);
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
							
							map=createMap(response.file_list[0].boundingBox);				
//						}	
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
							    response.file_list[i].topojson.objects && 
							    response.file_list[i].topojson.objects.collection && 
							    response.file_list[i].topojson.objects.collection.geometries) {
								response.file_list[i].total_areas=response.file_list[i].topojson.objects.collection.geometries.length;

							}
							else if (response.file_list[i].geojson && 
							         response.file_list[i].topojson.features) {
							    response.file_list[i].total_areas=response.file_list[i].topojson.features.length;
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
					for (var i=0; i < response.no_files; i++) {	//. Re-order by geolevel_id						
						if (response.file_list[i].geolevel_id) { // Geolevel ID present in data
							console.log("File[" + (i+1) + "]: " + response.file_list[i].file_name +
								"; geolevel: " +  response.no_files-response.file_list[i].geolevel_id +
								"; size: " +  response.no_files-response.file_list[i].file_size +
								"; areas: " +  response.no_files-response.file_list[i].total_areas);
							layerAddOrder[(response.no_files-response.file_list[i].geolevel_id)]=i;	
						}
						else {
							var ngeolevels = geolevels.sort(function (a, b) { // Sort function
								if (a.total_areas > b.total_areas) {
									return 1;
								}
								if (a.total_areas < b.total_areas) {
									return -1;
								}
								// a must be equal to b
								return 0;
							});
	
							ngeolevels[i].geolevel_id=i+1;
							response.file_list[ngeolevels[i].i].geolevel_id = ngeolevels[i].geolevel_id;				
							console.log("File[" + (i+1) + "]: " + response.file_list[i].file_name +
								"; deduced geolevel: " +  (i+1) +
								"; size: " + (response.no_files-response.file_list[i].file_size || "not defined") +
								"; areas: " +  response.no_files-response.file_list[i].total_areas);
							layerAddOrder[(response.no_files-response.file_list[i].geolevel_id)]=i;	
						}
					}
					if (layerAddOrder.length == 0) {
						msg+="</br>ERROR! layerAddOrder[] array is zero sized; response.no_files: " + response.no_files
						console.log('ERROR! layerAddOrder[] array is zero sized; response.no_files: ' + response.no_files);						
					}
					
					for (var i=0; i < response.no_files; i++) {	// Now processe					
						var weight=(response.no_files - i); // i.e. Lower numbers - high resolution have most weight
						var color=layerColours[i];	
	// Chroma.js - to be added (Node module)
	//					var colorScale = chroma  
	//						.scale(['#D5E3FF', '#003171'])
	//						.domain([0,1]);	
						var opacity;
						var fillOpacity;
	//					if (response.file_list[i].geolevel_id != response.no_files) { 
						if (i > 0) { // All but the first are transparent
							opacity=0;		
							fillOpacity=0;
						}
						else { // First - i.e. max geolevel
							opacity=1;
							fillOpacity=0.4;				
						}
	//					if (!color || response.file_list[i].geolevel_id == 1) {
						if (!color || i == (response.no_files - 1)) { // if >7 layers or lowest resolution (last layer) - make it black
							color="#000000"; // Black
							weight=response.no_files;
						}
				
						if (!response.file_list[layerAddOrder[i]] || !response.file_list[layerAddOrder[i]].file_name) {
							msg+="</br>ERROR! layerAddOrder problem: Adding data to JSONLayer[" + i + "]; layerAddOrder[" + layerAddOrder[i] + "]: NO FILE";
							console.error("ERROR layerAddOrder problem: Adding data to JSONLayer[" + i + "]; layerAddOrder[" + layerAddOrder[i] + "]: NO FILE");
						}
						else {
							console.log("Add data to JSONLayer[" + i + "]; file [" + layerAddOrder[i] + "]: " +
								response.file_list[layerAddOrder[i]].file_name +
								"; colour: " + color + "; weight: " + weight + "; opacity: " + opacity + "; fillOpacity: " + fillOpacity);
								
							msg+="<tr style=\"color:" + color + "\"><td>" + response.file_list[layerAddOrder[i]].file_name + "</td>" +
									"<td>" + (fileSize(response.file_list[layerAddOrder[i]].file_size) || "N/A") + "</td>";
							if (response.file_list[layerAddOrder[i]].topojson) {			
								try {
	//									var topojson = JSON.stringify(response.file_list[i].topojson);
									if (response.file_list[i].boundingBox && map) {
					
										if (JSONLayer[i]) {	
											console.log('Remove topoJSONLayer' + i + ']');
											map.removeLayer(JSONLayer[i]);
										}		
										
										console.log('New topoJSONLayer[' + i + '], color: ' + color);	
										document.getElementById("status").innerHTML = "Adding topoJSONLayer[" + i + "]...";	
										JSONLayer[i] = new L.topoJson(undefined, 
											{style: 
												{color: 	color,
												 fillColor: "#ccf4ff",
												 weight: 	weight, // i.e. Lower numbers have most weight
												 opacity: 	1,
												 fillOpacity: fillOpacity}
											}).addTo(map);
										JSONLayer[i].addData(response.file_list[layerAddOrder[i]].topojson);									
									}
									
									msg+="<td>" + (fileSize(response.file_list[layerAddOrder[i]].topojson_length) || "N/A ") + "/" + 
										(fileSize(response.file_list[layerAddOrder[i]].geojson_length) || " N/A") + "</td>";
								} catch (e) {  							
									msg+="TopoJSON conversion error: " + e.message;
									responseErrors++;
								}
							}
							else if (response.file_list[layerAddOrder[i]].geojson) {			
								try {
	//									var geojson = JSON.stringify(response.file_list[i].geojson);
									if (response.file_list[i].boundingBox && map) {
					
										if (JSONLayer[i]) {	
											console.log('Remove JSONLayer' + i + ']');
											map.removeLayer(JSONLayer[i]);
										}		
										
										console.log('New JSONLayer[' + i + '], color: ' + color);	
										document.getElementById("status").innerHTML = "Adding JSONLayer[" + i + "]...";	
										JSONLayer[i] = L.geoJson(undefined, 
											{style: 
												{color: 	color,
												 fillColor: "#ccf4ff",
												 weight: 	weight, // i.e. Lower numbers have most weight
												 opacity: 	1,
												 fillOpacity: fillOpacity}
											}).addTo(map);
										JSONLayer[i].addData(response.file_list[layerAddOrder[i]].geojson);									
									}
									
									msg+="<td>" + fileSize(response.file_list[layerAddOrder[i]].geojson_length) + "</td>";
								} catch (e) {  							
									msg+="GeoJSON conversion error: " + e.message;
									responseErrors++;
								}
							}
							else {
								msg+="ERROR! no GeoJSON/topoJSON returned";
								responseErrors++;
							}
							msg+="<td>" + response.file_list[layerAddOrder[i]].total_areas + "</td>" +
								"<td>" + (response.file_list[layerAddOrder[i]].geolevel_id || "N/A") + "</td>" + 
								"</tr>";
						}

					} // end of for loop
				
					msg+="</table>";			
				} // response.file_list[0] exists
				

			} // response.no_files > 0
			else {
				msg+="</br>ERROR! response.no_files == 0"; 
				console.error("ERROR! response.no_files == 0");
			}
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
	
	if (status == 200 && responseErrors == 0 && map) {	
		document.getElementById("status").innerHTML = "<h1>" + formName + " processed OK</h1>" + msg;
	}
	else if (status == 200 && responseErrors > 0 && map) {	
		document.getElementById("status").innerHTML = "<h1>" + formName + " processed OK; but with " + 
			responseErrors + " error(s) in response</h1>" + msg;
	}
	else if (status == 200 && responseErrors > 0 && !map) {	
		document.getElementById("status").innerHTML = "<h1>" + formName + " processed OK; but with " + 
			responseErrors + " error(s) in response; no map was produced</h1>" + '<h2>Error: ' + document.getElementById("status").innerHTML + '</h2>' +  msg;
	}	
	else if (status == 200 && responseErrors == 0 && !map) {	
		document.getElementById("status").innerHTML = "<h1>" + formName + " processed OK; no map was produced</h1>" + '<h2>Error: ' + document.getElementById("status").innerHTML + '</h2>' + msg;
	}	
	else {
		document.getElementById("status").innerHTML = "<h1>Send Failed; http status: " + status + "</h1></br>Message:" + msg;
	}
}	