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

var geolevelsHtml=[];
var geographiesJson=[];
var methodFields;
var bodyFontSize;	

/*
 * Function: 	databaseSelectChange()
 * Parameters: 	event object, ui object
 * Returns: 	Nothing
 * Description:	Gets valid RIF geographies from database 
 */
function databaseSelectChange(event, ui) {
	var db = (ui && ui.item && ui.item.value) || $( "#databaseSelect option:checked" ).val();

	function xhrGetMethodErrorCallback(err) {
		if (err) {
			consoleError("databaseSelectChange(): for db: " + db + "; caught: " + err.message);
			if (db == "MSSQLServer") {
				db="PostGres";
				$( "#databaseSelect option:checked" ).val(db);
				if (ui && ui.item && ui.item.value) {
					ui.item.value=db;
				}
				
				xhrGetMethod("getGeographies", "xhrGetMethodErrorCallback(): get geography listing from database: " + db, 
					getGeographies, {databaseType: db}, xhrGetMethodErrorCallback);
			}
		}
	}
	
	consoleLog("databaseSelectChange() db: " + db + "; ui: " + ((ui == undefined) && 0 || 1));	
	xhrGetMethod("getGeographies", "get geography listing from database: " + db, 
		getGeographies, {databaseType: db}, xhrGetMethodErrorCallback);
		
} // End of databaseSelectChange()

/*
 * Function: 	geolevelSelectChange()
 * Parameters: 	event object, ui object
 * Returns: 	Nothing
 * Description:	Sets up map for selected geography and database
 */
function geolevelSelectChange(event, ui) {
	var geolevel = (ui && ui.item && ui.item.value) || $( "#geolevelSelect option:checked" ).val();
	
	consoleLog("geolevelSelectChange() geolevel: " + geolevel + "; ui: " + ((ui == undefined) && 0 || 1));
	if (methodFields) {
		methodFields.geolevel_id=geolevel || 2;
		
		var geolevel=methodFields.geolevel[(methodFields.geolevel_id-1)];		
		var tlayer=addTileLayer(methodFields, methodFields.maxzoomlevel);	
	}
	else {
		consoleLog("geolevelSelectChange(): No methodFields");
	}
	
} // End of geolevelSelectChange()

/*
 * Function: 	geographySelectChange()
 * Parameters: 	event object, ui object
 * Returns: 	Nothing
 * Description:	Sets up map for selected geography and database
 */
function geographySelectChange(event, ui) {
	var geographyIndex = (ui && ui.item && ui.item.value) || $( "#geographySelect option:checked" ).val();

	consoleLog("geographySelectChange() geographyIndex: " + geographyIndex + "; ui: " + 
		((ui == undefined) && 0 || 1));	
	
	addSelector("#geolevelSelect", geolevelSelectChange, geolevelsHtml[geographyIndex], 
		undefined /* Use checked */);
	var geolevel = $( "#geolevelSelect option:checked" ).val();
		
	var geography;
	
	try {
		geography=geographiesJson[geographyIndex];
		methodFields= {
			databaseType: geography.database_type,
			table_catalog: geography.table_catalog,
			table_schema: geography.table_schema,
			table_name: geography.table_name,
			table_description: geography.description,
			geography: geography.geography,
			tiletable: geography.tiletable,
			geolevel_id: geolevel || 2,
			geolevel: geography.geolevel,
			maxzoomlevel: geography.maxzoomlevel
		}
//		consoleLog("geographySelectChange: " +  JSON.stringify(methodFields, null, 2)); 
//	xhrGetMethod("getMapTile", "get gmap tile from " + methodFields.database_type + " database: " + methodFields.table_catalog, 
//		getMapTile, methodFields);
// 
		getBbox(geography.database_type, 
			geography.table_catalog, 		// databaseName
			geography.table_schema, 		// databaseSchema
			geography.geography, 
			geography.table_name,
			geography.tiletable, 
			function getBboxCallback(bbox) {
				map=createMap(bbox, geography.maxzoomlevel);
				// Wait 100 mS
				var timeOut = setTimeout(function() {	
					map.whenReady( // Basemap is ready
						function whenMapIsReady() { 
							consoleLog("Map created, zoom: " + map.getZoom() + "; maxZoom: " + map.getMaxZoom());
							// Wait 100 mS
							// Does not work with Leaflet 1.0
//							try {
//								var loadingControl = L.Control.loading({
//									separate: false
//								});
//								map.addControl(loadingControl);
//							}
//							catch (e) {
//								try {
//									map.remove();
//								}
//								catch (e2) {
//									consoleLog("WARNING! Unable to remove map during error recovery");
//								}
//								throw new Error("Unable to add loading control to map: " + e.message);
//							}	
		
							var timeOut2 = setTimeout(function() {		 
								var tLayer=addTileLayer(methodFields, geography.maxzoomlevel);
								map.whenReady( // Basemap is ready
									function whenMapIsReady2() { 
										consoleLog("Tilelayer ready, zoom: " + map.getZoom() + "; maxZoom: " + map.getMaxZoom());
									}
								);
								clearTimeout(timeOut2);
							}, 100);
						}
					);
					clearTimeout(timeOut);
				}, 100);
				map.on('zoomend', function() {
					consoleLog("Map zoom changed to: " + map.getZoom() + "; maxZoom: " + map.getMaxZoom());
				});	
				map.on('resize', function() {
					consoleLog("Map resized");
				});				
			}) // End of getBbox() call
	}
	catch (e) {
		errorPopup("geographySelectChange() caught: " + e.message);
	}
	
} // End of databaseSelectChange()

/*
 * Function: 	getGeographies()
 * Parameters: 	data, status, XHR object
 * Returns: 	Nothing
 * Description:	getGeographies XHR GET reponse callback
 */
function getGeographies(data, status, xhr) {
//	consoleLog("getGeographies() OK: " + JSON.stringify(data, null, 2));	
	
	var geographies=data.geographies;
	var geographyHtml='<label id="geographyLabel"  title="Choose geography to display" for="geography">Geography:\n' +
		'<select required id="geographySelect" name="databaseType" form="dbSelect">';

	if (geographies == undefined) {
		var msg="getGeographies() FAILED: no geographies found in database";
		var extendedMasg;
		if (data && data.connectionError) {
			msg+="; error: " + (data.connectionError.message||JSON.stringify(data.connectionError));
			extendedMasg="<pre>" + (data.message||data.connectionError.stack||"N/A") + "</pre>";
		}
		errorPopup(msg, extendedMasg);
	}
	else {		
		for (var i=0; i<geographies.length; i++) {
			geographiesJson[i]=geographies[i];
			if (i == 0) {
				consoleLog("getGeographies() OK for " + geographies[i].database_type + " database: " + geographies[i].table_catalog);
			     geographyHtml+="<option value='" + i + "' selected='selected'>" + 
					geographies[i].table_catalog + "." + geographies[i].table_schema + "." + 
						geographies[i].table_name + ": " + geographies[i].description + "</option>";
			}
			else {
			     geographyHtml+="<option value='" + i + "'>" + 
					geographies[i].table_catalog + "." + geographies[i].table_schema + "." +
						geographies[i].table_name + ": " + geographies[i].description + "</option>";
			}
			geolevelsHtml[i]='<label id="geolevelLabel"  title="Choose geolevel to display" for="geolevel">Geolevel:\n' +
				'<select required id="geolevelSelect" name="databaseType" form="dbSelect">';		
			
			for (var j=0; j<geographies[i].geolevel.length; j++) {
				if (j == 1) {
					geolevelsHtml[i]+="<option value='" + geographies[i].geolevel[j].geolevel_id + "' selected='selected'>" +
						geographies[i].geolevel[j].description + "</option>";					
				}
				else {
					geolevelsHtml[i]+="<option value='" + geographies[i].geolevel[j].geolevel_id + "'>" + 
						geographies[i].geolevel[j].description + "</option>";					
				}
			}
			geolevelsHtml[i]+='</select>\n' + '</label>';
		}
		geographyHtml+='</select>\n' + '</label>';		
		
		addSelector("#geographySelect", geographySelectChange, geographyHtml, undefined /* Use checked */);
		addSelector("#geolevelSelect", geolevelSelectChange, geolevelsHtml[0], undefined /* Use checked */);	
	}
			
} // End of getGeographies()
	
/*
 * Function: 	cacheTabBeforeActivate()
 * Parameters: 	None
 * Returns: 	Nothing
 * Description:	Setup cache tab html before activation
 */	
function cacheTabBeforeActivate() {
	consoleLog("cacheTabBeforeActivate()");
	if (basemaps && topojsonTileLayer && topojsonTileLayer.options.useCache) { // Caching enabled
		document.getElementById("cacheTab").innerHTML='<a>Please wait, fetching cache data...</a><div id="progressbar"></div>';
		basemaps.getCacheSize(function getCacheSizeCallback(err, results) {
			if (err) {
				errorPopup(new Error("cacheTabBeforeActivate(): getCacheSize() error: " +  (err.message || JSON.stringify(err))));
				document.getElementById("cacheTab").innerHTML='<a>Cache information not available due to error</a>';
			}
			else {
				consoleLog("cacheTabBeforeActivate(): getCacheSize() done.");
				if (results.tableHtml && results.nonBasemapCacheStats) {
					var toopjsonCacheStatHtml=(getTopojsonTileLayerStats(results.nonBasemapCacheStats) || "");
					document.getElementById("cacheTab").innerHTML='<a>' + 
										'<table id="cachetable" style="width:100%">\n' +
											'  <tr>\n' +
											'    <th>Name</th>\n' +
											'    <th>Hits</th>\n' +
											'    <th>Misses</th>\n' +
											'    <th>Errors</th>\n' +
											'    <th>Cached</th>\n' +
											'    <th>Size</th>\n' +
											'  </tr>' +
						results.tableHtml + toopjsonCacheStatHtml +
						'</table></a>' + 
						'<a><table>' +
						'<tr><td>Total tiles: </td><td>' + results.totalTiles + '</td></tr>' +
						'<tr><td>Cache size: </td><td>' + (fileSize(results.cacheSize)||"N/A") + '</td></tr>' +
						'<tr><td>Auto compaction: </td><td>' + (results.autoCompaction ? "Yes" : "No") + '</td></tr></table>';
				}
				else {
					consoleLog("cacheTabBeforeActivate() results: " + JSON.stringify(results, nuill, 2));
				}
			}			
		});
	}
	else {
		document.getElementById("cacheTab").innerHTML='<a>Cache information not available; topojson caching enabled: ' + 
			(topojsonTileLayer.options.useCache ? "true" : "false") + '; basemap caching enabled: ' + 
			(baseLayer.options.useCache ? "true" : "false") + '</a>';	
	}
}

/*
 * Function: 	cacheEmpty()
 * Parameters: 	settings JQuery UI dialog object
 * Returns: 	Nothing
 * Description:	Empty cache
 */
function cacheEmpty(settings) {
	consoleLog("cacheEmpty()");	
	
	if (basemaps && topojsonTileLayer && topojsonTileLayer.options.useCache) { // Caching enabled
		document.getElementById("cacheTab").innerHTML='<a>Please wait, emptying cache data...</a><div id="progressbar"></div>';
		basemaps.empty(function emptyCallback(err, results) {
			if (err) {
				errorPopup(new Error("cacheEmpty(): empty() error: " + err.message));
			}
			else {
				consoleLog("cacheEmpty(): empty() done: " + JSON.stringify(results));
				document.getElementById("cacheTab").innerHTML="<a>Done; deleted: " + (results && results.totalTiles|| 0) + " tiles</a>";
//				settings.dialog("close"); 
			}			
		});
	}
}
	
/*
 * Function: 	setupTileViewer()
 * Parameters: 	Select fields array
 * Returns: 	Nothing
 * Description:	Setup tile viewer screen size
 */		
function setupTileViewer(allSelectFields) {
	var w = window,
		d = document,
		e = d.documentElement,
		g = d.getElementsByTagName('body')[0],
		x = w.innerWidth || e.clientWidth || g.clientWidth,
		y = w.innerHeight|| e.clientHeight|| g.clientHeight;
	
	var selectDiv = document.getElementById("selectDiv"); // Change database, geography and geolevel
	var selectDivHeightStr = window.getComputedStyle(selectDiv, null).getPropertyValue("height");
	var selectDivHeight = parseInt(selectDivHeightStr.substring(0, selectDivHeightStr.length - 2));
	var selectDivWidthStr = window.getComputedStyle(selectDiv, null).getPropertyValue("width");
	var selectDivWidth = parseInt(selectDivWidthStr.substring(0, selectDivWidthStr.length - 2));
	var mapcontainerHeight=y-selectDivHeight-20; 
	var dialogFormHieght=y-selectDivHeight-50; 
	var dialogFormWidth=x-250; 
//	if (dialogFormWidth < 200) {
//		dialogFormWidth=200;
//	}
	var selectButtonWidthStr=$("#select-button").css("width");
	var selectButtonWidth = parseInt(selectButtonWidthStr.substring(0, selectButtonWidthStr.length - 2));
	var dbSelectorHeight=dialogFormHieght-320;
	var dbSelectorWidth=x-325;
//	if (dbSelectorHeight < 200) {
//		dbSelectorHeight=200;
//	}
	var labelClassWidth=dialogFormWidth-90;
	var selectClassWidth=dialogFormWidth-180;
//	if (selectClassWidth < 300) {
//		dbSelectorWidth=225;
//		selectClassWidth=200;
//		labelClassWidth=300;
//	}	

	consoleLog("setupTileViewer(): " +
		"Window height: " + y + " px, width: " + x + " px\n" +
		"selectDiv height: " + selectDivHeight + " px, width: " + selectDivWidth + " px\n" +
		"mapcontainer height: " + mapcontainerHeight + " px\n" +
		"dialogForm height: " + dialogFormHieght + " px, width: " + dialogFormWidth + " px\n" +
		"selectButton Width: " + selectButtonWidth + " px\n" +
		"labelClass Width: " + labelClassWidth + " px\n" +
		"selectClass Width: " + selectClassWidth + " px\n" +
		"dbSelector Height: " + dbSelectorHeight + " px, width: " + dbSelectorWidth + " px");
	
	if (map == undefined) { // Only set the height if leaflet is not initialised or you will make a mess of the screen
		setHeight("mapcontainer", mapcontainerHeight);
		setHeight("map", mapcontainerHeight-3);			
	}	
	else { // Set via Leaflet
//		setHeight("mapcontainer", mapcontainerHeight);
//		setHeight("map", mapcontainerHeight-3);		
//		map.invalidateSize();​
	}
	setHeight("topbar", selectDivHeight);		

	$( "#dialog-form" ).dialog( "option", "height", dialogFormHieght );	
	$( "#dialog-form" ).dialog( "option", "width", dialogFormWidth );
	$( "#settings-tabs" ).dialog( "option", "height", dialogFormHieght );	
	$( "#settings-tabs" ).dialog( "option", "width", dialogFormWidth );	
	
	var labelClass=document.getElementsByClassName("labelClass");
	for (var i=0;i<labelClass.length; i++) {
		setWidth(labelClass[i], labelClassWidth);
	}
	var selectClass=document.getElementsByClassName("selectClass");
	for (var i=0;i<selectClass.length; i++) {
		setWidth(selectClass[i], selectClassWidth);
	}
	var selectClass=document.getElementsByClassName("inputClass");
	for (var i=0;i<selectClass.length; i++) {
		setWidth(selectClass[i], selectClassWidth);
	}	

	document.getElementById("dbSelector").style.height=dbSelectorHeight + "px";
	setWidth(document.getElementById("dbSelector"), dbSelectorWidth);	
	
	var fontSizeStr=$( "#tileviewerbody" ).css('font-size');
	var fontSize=parseInt(fontSizeStr.substring(0, fontSizeStr.length - 2));
	if (bodyFontSize == undefined) {
		bodyFontSize=fontSize;
	}
	if (dbSelectorWidth < 600) {
		fontSize-=2;
		$( "#tileviewerbody" ).css('font-size', fontSize + "px");
		consoleLog("New body font size(" + fontSize+ "): " + $( "#tileviewerbody" ).css('font-size'));
	} 
	else {
		$( "#tileviewerbody" ).css('font-size', bodyFontSize + "px");
		consoleLog("Reset body font size: " + $( "#tileviewerbody" ).css('font-size'));
	}
	
//	autoResizeLabels();
	
	if (allSelectFields) { // Defined if select fields are initialised
	// Refresh select fields
		for (var i=0; i<allSelectFields.length; i++) {
			
//			consoleLog("Select field: " + allSelectFields[i].id + " options: " +
//				JSON.stringify($( allSelectFields[i] ).button( "option" ), null, 2));
			$( allSelectFields[i] ).button( "refresh" );
			$( allSelectFields[i] ).selectmenu( "refresh" );
		};
	}
		
} // End of setupTileViewer()

/*
 * Function: 	setupTileViewer()
 * Parameters: 	None
 * Returns: 	Nothing
 * Description:	Change label font size to fit labels (no longer used)
 */	
function autoResizeLabels() {
		// Auto resize .labelClass class
	var numResizable=0;
	var j=0;
	$('.labelClass').each(function(i, obj) {
		numResizable++;

		var startWidth=$(obj).width();
		var parentWidth=$(obj).parent().width();
		var startFontSize=$(obj).css('font-size');
		if ($(obj).parent().attr("id")) {			
			if (startWidth && startFontSize && parentWidth && parentWidth > 0) {
				j++;
				var k=0;
				while ($(obj).width() > parentWidth && k<20 /* Recursion preventor */) {
					k++;
					var newFontSize=(parseInt($(obj).css('font-size')) - 1);
					$(obj).css('font-size', newFontSize + "px");
				}			
			}
			if (startWidth == $(obj).width() && 
				startFontSize == $(obj).css('font-size')) {
				consoleLog("Auto resize [" + j + "] " + $(obj).attr("id") + ": width/font size " + 
				startWidth + "px/" + startFontSize + " is unchanged; parent (" + 
					$(obj).parent().attr("id") + ") width: " + parentWidth + "/" + $(obj).width() + "px");
			}
			else {
				consoleLog("Auto resize [" + j + "] " + $(obj).attr("id") + ": width/font size " + 
				startWidth + "px/" + startFontSize + " to " + 
				$(obj).width() + "px/" + $(obj).css('font-size') + "; parent (" + 
					$(obj).parent().attr("id") + ") width: " + parentWidth + "/" + $(obj).width() + "px");
			}
		}
	});
	consoleLog("Total auto resize: " + j + "/" + numResizable);
	
}