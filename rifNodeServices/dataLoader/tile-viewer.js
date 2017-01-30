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
	
/*
 * Function: 	databaseSelectChange()
 * Parameters: 	event object, ui object
 * Returns: 	Nothing
 * Description:	Gets valid RIF geographies from database 
 */
function databaseSelectChange(event, ui) {
	var db = (ui && ui.item && ui.item.value) || $( "#databaseSelect option:checked" ).val();
	
	xhrGetMethod("getGeographies", "get geography listing from database: " + db, 
		getGeographies, {databaseType: db});
		
} // End of databaseSelectChange()

/*
 * Function: 	geolevelSelectChange()
 * Parameters: 	event object, ui object
 * Returns: 	Nothing
 * Description:	Sets up map for selected geography and database
 */
function geolevelSelectChange(event, ui) {
	var geolevel = (ui && ui.item && ui.item.value) || $( "#geolevelSelect option:checked" ).val();
	
	consoleLog("geolevelSelectChange() geolevel: " + geolevel);
	
} // End of geolevelSelectChange()

/*
 * Function: 	geographySelectChange()
 * Parameters: 	event object, ui object
 * Returns: 	Nothing
 * Description:	Sets up map for selected geography and database
 */
function geographySelectChange(event, ui) {
	var geographyText = (ui && ui.item && ui.item.value) || $( "#geographySelect option:checked" ).val();
// geolevelsHtml
//		consoleLog("Replace #geographySelect HTML: " + html);
//		document.getElementById('geolevelSelect').innerHTML=html;
	var methodFields;
	var geography;
	
	try {
		geography=JSON.parse(geographyText);
		methodFields={
			databaseType: geography.database_type,
			table_catalog: geography.table_catalog,
			table_schema: geography.table_schema,
			table_name: geography.table_name,
			geography: geography.geography,
			tiletable: geography.tiletable,
			geolevel_id: 2,
			geolevel: geography.geolevel
		}
		consoleLog("geographySelectChange: " +  JSON.stringify(methodFields, null, 2)); 
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
				createMap(bbox, geography.maxzoomlevel);
				addTileLayer(methodFields);
			}) // End of getBbox() call
	}
	catch (e) {
		errorPopup("geographySelectChange() caught: " + e.message);
	}
	
} // End of databaseSelectChange()

/*
 * Function: 	getGeographies()
 * Parameters: 	data, status,XHR object
 * Returns: 	Nothing
 * Description:	getGeographies XHR GET reponse callback
 */
function getGeographies(data, status, xhr) {
//	consoleLog("getGeographies() OK: " + JSON.stringify(data, null, 2));	
	
	var geographies=data.geographies;
	var html='<label id="geographyLabel"  title="Choose geography to display" for="geography">Geography:\n' +
		'<select required id="geographySelect" name="databaseType" form="dbSelect">';

	if (geographies == undefined) {
		errorPopup("getGeographies() FAILED: no geographies found in database");
	}
	else {		
		for (var i=0; i<geographies.length; i++) {
			var value=JSON.stringify(geographies[i]);
			if (i == 0) {
				consoleLog("getGeographies() OK for " + geographies[i].database_type + " database: " + geographies[i].table_catalog);
			     html+="<option value='" + value + "' selected='selected'>" + 
					geographies[i].table_schema + "." + geographies[i].table_name + ": " + geographies[i].description + "</option>";
			}
			else {
			     html+="<option value='" + value + "'>" + 
					geographies[i].table_schema + "." +geographies[i].table_name + ": " + geographies[i].description + "</option>";
			}
			geolevelsHtml[value]='<label id="geolevelLabel"  title="Choose geolevel to display" for="geolevel">Geolevel:\n' +
				'<select required id="geolevelSelect" name="databaseType" form="dbSelect">';		
			
			for (var j=0; j<geographies[i].geolevel.length; j++) {
				if (j == 1) {
					geolevelsHtml[value]+="<option value='" + geographies[i].geolevel[j].geolevel_id + "' selected='selected'>" + 
						geographies[i].geolevel[j].geolevel_name + "(" + 
						geographies[i].geolevel[j].description + ")</option>";					
				}
				else {
					geolevelsHtml[value]+="<option value='" + geographies[i].geolevel[j].geolevel_id + "'>" + 
						geographies[i].geolevel[j].geolevel_name + "(" + 
						geographies[i].geolevel[j].description + ")</option>";					
				}
			}
			geolevelsHtml[value]+='</select>\n' + '</label>';
		}
		html+='</select>\n' + '</label>';		
//		consoleLog("Replace #geographySelect HTML: " + html);
		consoleLog("geolevelsHtml: " + JSON.stringify(geolevelsHtml, null, 2));
		document.getElementById('geographySelect').innerHTML=html;
		
		var w = window,
			d = document,
			e = d.documentElement,
			g = d.getElementsByTagName('body')[0],
			x = w.innerWidth || e.clientWidth || g.clientWidth,
			y = w.innerHeight|| e.clientHeight|| g.clientHeight;
		document.getElementById('geographySelect').style.width=(x-650) + "px";	

		var height=y-46; // Merge all the correction factors (46px)
		if (map == undefined) { // Only set the height if leaflet is not initialised or you will make a mess of the screen
			setHeight("mapcontainer", (height-52));
			setHeight("map", (height-55));			
		}		
		
		addSelector("#geographySelect", geographySelectChange);
		addSelector("#geolevelSelect", geolevelSelectChange);
	}
			
} // End of getGeographies()
		
