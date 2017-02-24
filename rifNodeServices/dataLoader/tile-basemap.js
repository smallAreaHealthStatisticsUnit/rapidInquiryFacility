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

var controlLayers;

/*
 * Function: 	Basemap()
 * Parameters:	basemapOptions, mapArray
 * Returns:		Basemap() Object
 * Description:	Create Basemap object
 */	
function Basemap(basemapOptions, mapArray) { 
	this.name=basemapOptions.name;
	this.tileLayer=basemapOptions.tileLayer;
	
	this.tileLayer.on('tileerror', function(tile) {
		consoleError("Error: loading " + this.name + " tile: " + JSON.stringify(tile.coords)||"UNK");
	});
	
	mapArray.push(this);
}// End of ~Basemap() object constructor

/*
 * Function: 	mapArray() 
 * Parameters:  map, defaultBaseMap, maxZoomlevel
 * Returns:		mapArray() Object
 * Description: Construction for mapArray
 */
function mapArrays(map, defaultBaseMap, maxZoomlevel) {
		this.basemapArray=[];
		this.overlaymapArray=[];
		
		this.initBaseMaps(map, defaultBaseMap, maxZoomlevel);
	}
	mapArrays.prototype = { // Add methods
		/*
		 * Function: 	addBaseMap()
		 * Parameters:	baseMap object
		 * Returns:		Nothing
		 * Description:	Add basemap to basemap array
		 */	
		addBaseMap: function(baseMap) {
			this.basemapArray.push(baseMap);
		},	
		/*
		 * Function: 	addOverlayMap()
		 * Parameters:	overlayMap object
		 * Returns:		Nothing
		 * Description:	Add overlayMap to overlayMap array
		 */	
		addOverlayMap: function(overlayMap) {
			this.overlaymapArray.push(overlayMap);
		},	
		/*
		 * Function: 	initBaseMaps()
		 * Parameters:	map, defaultBaseMap, maxZoomlevel
		 * Returns:		Nothing
		 * Description:	Initialise base and overlay maps
		 */	
		initBaseMaps: function(map, defaultBaseMap, maxZoomlevel) {
			new Basemap({
				name: "OpenStreetMap Mapnik", 
				tileLayer: L.tileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
						attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
				})}, this.basemapArray);
			new Basemap({
				name: "OpenStreetMap BlackAndWhite", 
				tileLayer: L.tileLayer('http://{s}.tiles.wmflabs.org/bw-mapnik/{z}/{x}/{y}.png', {
						attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
				})}, this.basemapArray);
			new Basemap({
				name: "OpenTopoMap", 
				tileLayer: L.tileLayer('http://{s}.tile.opentopomap.org/{z}/{x}/{y}.png', {
						attribution: 'Map data: &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>, <a href="http://viewfinderpanoramas.org" target="_blank">SRTM</a> | Map style: &copy; <a href="https://opentopomap.org" target="_blank">OpenTopoMap</a> (<a href="https://creativecommons.org/licenses/by-sa/3.0/" target="_blank">CC-BY-SA</a>)'
				})}, this.basemapArray);
			new Basemap({
				 name: "Humanitarian OpenStreetMap", 
				 tileLayer: L.tileLayer('http://{s}.tile.openstreetmap.fr/hot/{z}/{x}/{y}.png', {
						attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>, Tiles courtesy of <a href="http://hot.openstreetmap.org/" target="_blank">Humanitarian OpenStreetMap Team</a>'
				})}, this.basemapArray);
			new Basemap({
				name: "Thunderforest OpenCycleMap", 
				tileLayer: L.tileLayer('http://{s}.tile.thunderforest.com/cycle/{z}/{x}/{y}.png', {
					attribution: '&copy; <a href="http://www.thunderforest.com/" target="_blank">Thunderforest</a>, &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
				})}, this.basemapArray);
			new Basemap({
				name: "Thunderforest Transport", 
				tileLayer: L.tileLayer('http://{s}.tile.thunderforest.com/transport/{z}/{x}/{y}.png', {
					attribution: '&copy; <a href="http://www.thunderforest.com/" target="_blank">Thunderforest</a>, &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
				})}, this.basemapArray);
			new Basemap({
				name: "Thunderforest TransportDark", 
				tileLayer: L.tileLayer('http://{s}.tile.thunderforest.com/transport-dark/{z}/{x}/{y}.png', {
					attribution: '&copy; <a href="http://www.thunderforest.com/" target="_blank">Thunderforest</a>, &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
				})}, this.basemapArray);
			new Basemap({
				name: "Thunderforest Landscape", 
				tileLayer: L.tileLayer('http://{s}.tile.thunderforest.com/landscape/{z}/{x}/{y}.png', {
					attribution: '&copy; <a href="http://www.thunderforest.com/" target="_blank">Thunderforest</a>, &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
				})}, this.basemapArray);
			new Basemap({
				name: "Thunderforest SpinalMap", 
				tileLayer: L.tileLayer('http://{s}.tile.thunderforest.com/spinal-map/{z}/{x}/{y}.png', {
					attribution: '&copy; <a href="http://www.thunderforest.com/" target="_blank">Thunderforest</a>, &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
				})}, this.basemapArray);
			new Basemap({
				name: "Thunderforest Outdoors", 
				tileLayer: L.tileLayer('http://{s}.tile.thunderforest.com/outdoors/{z}/{x}/{y}.png', {
					attribution: '&copy; <a href="http://www.thunderforest.com/" target="_blank">Thunderforest</a>, &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
				})}, this.basemapArray);
			new Basemap({
				name: "Thunderforest Pioneer", 
				tileLayer: L.tileLayer('http://{s}.tile.thunderforest.com/pioneer/{z}/{x}/{y}.png', {
					attribution: '&copy; <a href="http://www.thunderforest.com/" target="_blank">Thunderforest</a>, &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
				})}, this.basemapArray);
			new Basemap({
				name: "OpenMapSurfer Roads", 
				tileLayer: L.tileLayer('http://korona.geog.uni-heidelberg.de/tiles/roads/x={x}&y={y}&z={z}', {
					attribution: 'Imagery from <a href="http://giscience.uni-hd.de/" target="_blank">GIScience Research Group @ University of Heidelberg</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
				})}, this.basemapArray);
			new Basemap({
				name: "OpenMapSurfer Grayscale", 
				tileLayer: L.tileLayer('http://korona.geog.uni-heidelberg.de/tiles/roadsg/x={x}&y={y}&z={z}', {
					attribution: 'Imagery from <a href="http://giscience.uni-hd.de/" target="_blank">GIScience Research Group @ University of Heidelberg</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
				})}, this.basemapArray);
			new Basemap({
				name: "Hydda Full", 
				tileLayer: L.tileLayer('http://{s}.tile.openstreetmap.se/hydda/full/{z}/{x}/{y}.png', {
					attribution: 'Tiles courtesy of <a href="http://openstreetmap.se/" target="_blank">OpenStreetMap Sweden</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
				})}, this.basemapArray);
			new Basemap({
				name: "Hydda Base", 
				tileLayer: L.tileLayer('http://{s}.tile.openstreetmap.se/hydda/base/{z}/{x}/{y}.png', {
					attribution: 'Tiles courtesy of <a href="http://openstreetmap.se/" target="_blank">OpenStreetMap Sweden</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
				})}, this.basemapArray);
			new Basemap({
				name: "Stamen Toner", 
				tileLayer: L.tileLayer('http://stamen-tiles-{s}.a.ssl.fastly.net/toner/{z}/{x}/{y}.{ext}', {
					attribution: 'Map tiles by <a href="http://stamen.com" target="_blank">Stamen Design</a>, <a href="http://creativecommons.org/licenses/by/3.0" target="_blank">CC BY 3.0</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
					subdomains: 'abcd',
					ext: 'png'
				})}, this.basemapArray);
			new Basemap({
				name: "Stamen TonerBackground", 
				tileLayer: L.tileLayer('http://stamen-tiles-{s}.a.ssl.fastly.net/toner-background/{z}/{x}/{y}.{ext}', {
					attribution: 'Map tiles by <a href="http://stamen.com" target="_blank">Stamen Design</a>, <a href="http://creativecommons.org/licenses/by/3.0" target="_blank">CC BY 3.0</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
					subdomains: 'abcd',
					ext: 'png'
				})}, this.basemapArray);
			new Basemap({
				name: "Stamen TonerLite", 
				tileLayer: L.tileLayer('http://stamen-tiles-{s}.a.ssl.fastly.net/toner-lite/{z}/{x}/{y}.{ext}', {
					attribution: 'Map tiles by <a href="http://stamen.com" target="_blank">Stamen Design</a>, <a href="http://creativecommons.org/licenses/by/3.0" target="_blank">CC BY 3.0</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
					subdomains: 'abcd',
					ext: 'png'
				})}, this.basemapArray);
			new Basemap({
				name: "Stamen Watercolor", 
				tileLayer: L.tileLayer('http://stamen-tiles-{s}.a.ssl.fastly.net/watercolor/{z}/{x}/{y}.{ext}', {
					attribution: 'Map tiles by <a href="http://stamen.com" target="_blank">Stamen Design</a>, <a href="http://creativecommons.org/licenses/by/3.0" target="_blank">CC BY 3.0</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
					subdomains: 'abcd',
					ext: 'png'
				})}, this.basemapArray);		
			new Basemap({
				name: "Esri WorldStreetMap", 
				tileLayer: L.tileLayer('http://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/{z}/{y}/{x}', {
					attribution: 'Tiles &copy; Esri &mdash; Source: Esri, DeLorme, NAVTEQ, USGS, Intermap, iPC, NRCAN, Esri Japan, METI, Esri China (Hong Kong), Esri (Thailand), TomTom, 2012'
				})}, this.basemapArray);
			new Basemap({
				name: "Esri DeLorme", 
				tileLayer: L.tileLayer('http://server.arcgisonline.com/ArcGIS/rest/services/Specialty/DeLorme_World_Base_Map/MapServer/tile/{z}/{y}/{x}', {
					attribution: 'Tiles &copy; Esri &mdash; Copyright: &copy;2012 DeLorme'
				})}, this.basemapArray);
			new Basemap({
				name: "Esri WorldTopoMap", 
				tileLayer: L.tileLayer('http://server.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer/tile/{z}/{y}/{x}', {
					attribution: 'Tiles &copy; Esri &mdash; Esri, DeLorme, NAVTEQ, TomTom, Intermap, iPC, USGS, FAO, NPS, NRCAN, GeoBase, Kadaster NL, Ordnance Survey, Esri Japan, METI, Esri China (Hong Kong), and the GIS User Community'
				})}, this.basemapArray);
			new Basemap({
				name: "Esri WorldImagery", 
				tileLayer: L.tileLayer('http://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
					attribution: 'Tiles &copy; Esri &mdash; Source: Esri, i-cubed, USDA, USGS, AEX, GeoEye, Getmapping, Aerogrid, IGN, IGP, UPR-EGP, and the GIS User Community'
				})}, this.basemapArray);
			new Basemap({
				name: "Esri WorldTerrain", 
				tileLayer: L.tileLayer('http://server.arcgisonline.com/ArcGIS/rest/services/World_Terrain_Base/MapServer/tile/{z}/{y}/{x}', {
					attribution: 'Tiles &copy; Esri &mdash; Source: USGS, Esri, TANA, DeLorme, and NPS'
				})}, this.basemapArray);
			new Basemap({
				name: "Esri WorldShadedRelief", 
				tileLayer: L.tileLayer('http://server.arcgisonline.com/ArcGIS/rest/services/World_Shaded_Relief/MapServer/tile/{z}/{y}/{x}', {
					attribution: 'Tiles &copy; Esri &mdash; Source: Esri'
				})}, this.basemapArray);
			new Basemap({
				name: "Esri WorldPhysical ", 
				tileLayer: L.tileLayer('http://server.arcgisonline.com/ArcGIS/rest/services/World_Physical_Map/MapServer/tile/{z}/{y}/{x}', {
					attribution: 'Tiles &copy; Esri &mdash; Source: US National Park Service'
				})}, this.basemapArray);
			new Basemap({
				name: "Esri OceanBasemap", 
				tileLayer: L.tileLayer('http://server.arcgisonline.com/ArcGIS/rest/services/Ocean_Basemap/MapServer/tile/{z}/{y}/{x}', {
					attribution: 'Tiles &copy; Esri &mdash; Sources: GEBCO, NOAA, CHS, OSU, UNH, CSUMB, National Geographic, DeLorme, NAVTEQ, and Esri'
				})}, this.basemapArray);
			new Basemap({
				name: "Esri NatGeoWorldMap", 
				tileLayer: L.tileLayer('http://server.arcgisonline.com/ArcGIS/rest/services/NatGeo_World_Map/MapServer/tile/{z}/{y}/{x}', {
					attribution: 'Tiles &copy; Esri &mdash; National Geographic, Esri, DeLorme, NAVTEQ, UNEP-WCMC, USGS, NASA, ESA, METI, NRCAN, GEBCO, NOAA, iPC'
				})}, this.basemapArray);
			new Basemap({
				name: "Esri WorldGrayCanvas", 
				tileLayer: L.tileLayer('http://server.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Light_Gray_Base/MapServer/tile/{z}/{y}/{x}', {
					attribution: 'Tiles &copy; Esri &mdash; Esri, DeLorme, NAVTEQ'
				})}, this.basemapArray);
			new Basemap({
				name: "CartoDB Positron", 
				tileLayer: L.tileLayer('http://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png', {
					attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a> &copy; <a href="http://cartodb.com/attributions" target="_blank">CartoDB</a>',
					subdomains: 'abcd'
				})}, this.basemapArray);
			new Basemap({
				name: "CartoDB PositronNoLabels", 
				tileLayer: L.tileLayer('http://{s}.basemaps.cartocdn.com/light_nolabels/{z}/{x}/{y}.png', {
					attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a> &copy; <a href="http://cartodb.com/attributions" target="_blank">CartoDB</a>',
					subdomains: 'abcd'
				})}, this.basemapArray);
			new Basemap({
				name: "CartoDB PositronOnlyLabels", 
				tileLayer: L.tileLayer('http://{s}.basemaps.cartocdn.com/light_only_labels/{z}/{x}/{y}.png', {
					attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a> &copy; <a href="http://cartodb.com/attributions" target="_blank">CartoDB</a>',
					subdomains: 'abcd'
				})}, this.basemapArray);
			new Basemap({
				name: "CartoDB DarkMatter", 
				tileLayer: L.tileLayer('http://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}.png', {
					attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a> &copy; <a href="http://cartodb.com/attributions" target="_blank">CartoDB</a>',
					subdomains: 'abcd'
				})}, this.basemapArray);
			new Basemap({
				name: "CartoDB DarkMatterNoLabels", 
				tileLayer: L.tileLayer('http://{s}.basemaps.cartocdn.com/dark_nolabels/{z}/{x}/{y}.png', {
					attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a> &copy; <a href="http://cartodb.com/attributions" target="_blank">CartoDB</a>',
					subdomains: 'abcd'
				})}, this.basemapArray);
			new Basemap({
				name: "CartoDB DarkMatterOnlyLabels", 
				tileLayer: L.tileLayer('http://{s}.basemaps.cartocdn.com/dark_only_labels/{z}/{x}/{y}.png', {
					attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a> &copy; <a href="http://cartodb.com/attributions" target="_blank">CartoDB</a>',
					subdomains: 'abcd'
				})}, this.basemapArray);
			new Basemap({
				name: "HikeBike HikeBike", 
				tileLayer: L.tileLayer('http://{s}.tiles.wmflabs.org/hikebike/{z}/{x}/{y}.png', {
					attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
				})}, this.basemapArray);
			new Basemap({
				name: "HikeBike HillShading", 
				tileLayer: L.tileLayer('http://{s}.tiles.wmflabs.org/hillshading/{z}/{x}/{y}.png', {
					attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
				})}, this.basemapArray);
			new Basemap({
				name: "NASAGIBS ViirsEarthAtNight2012", 
				tileLayer: L.tileLayer('http://map1.vis.earthdata.nasa.gov/wmts-webmerc/VIIRS_CityLights_2012/default/{time}/{tilematrixset}{maxZoom}/{z}/{y}/{x}.{format}', {
					attribution: 'Imagery provided by services from the Global Imagery Browse Services (GIBS), operated by the NASA/GSFC/Earth Science Data and Information System (<a href="https://earthdata.nasa.gov" target="_blank">ESDIS</a>) with funding provided by NASA/HQ.',
					bounds: [[-85.0511287776, -179.999999975], [85.0511287776, 179.999999975]],
					minZoom: 1,
					maxZoom: 8,
					format: 'jpg',
					time: '',
					tilematrixset: 'GoogleMapsCompatible_Level'
				})}, this.basemapArray);
			//Additional
			new Basemap({
				name: "OSM UK Postcodes", 
				tileLayer: L.tileLayer('http://random.dev.openstreetmap.org/postcodes/tiles/pc-npe/{z}/{x}/{y}.png', {
					attribution: '&copy; <a href="http://random.dev.openstreetmap.org/postcodes/" target="_blank">OSM Postcode</a>'
				})}, this.overlaymapArray);
			new Basemap({
				name: "Code-Point Open UK Postcodes", 
				tileLayer: L.tileLayer('http://random.dev.openstreetmap.org/postcodes/tiles/pc-os/{z}/{x}/{y}.png', {
					attribution: '&copy; <a href="http://random.dev.openstreetmap.org/postcodes/" target="_blank">Code-Point Open layers</a>'
				})}, this.overlaymapArray);
								
			var currentBaseMap;	
			var layerList = {};
			for (var i=0; i<this.basemapArray.length; i++) {
				this.basemapArray[i].tileLayer.on('tileerror', function(tile) {
					consoleError("Error: loading " + baseLayer.name + " tile: " + JSON.stringify(tile.coords)||"UNK");
				});
				this.basemapArray[i].tileLayer.name=this.basemapArray[i].name;
				currentBaseMap=this.basemapArray[i].tileLayer;
				layerList[this.basemapArray[i].name]=this.basemapArray[i].tileLayer;
			}
				
			var overlayList = {};
			for (var i=0; i<this.overlaymapArray.length; i++) {
				this.overlaymapArray[i].tileLayer.name=this.overlaymapArray[i].name;
				overlayList[this.overlaymapArray[i].name]=this.overlaymapArray[i].tileLayer;
			}
			
			if (layerList[defaultBaseMap]) {
				
				currentBaseMap=layerList[defaultBaseMap];
				currentBaseMap.addTo(map);
				controlLayers=L.control.layers(
					layerList, 		// Base layers 
					overlayList, 	// Overlays
				{
					position: 'topright',
					collapsed: true
				});	
				baseLayer = currentBaseMap;
				
				map.on('baselayerchange', function baselayerchangeEvent(changeEvent) {
					baseLayer=changeEvent.layer;
					consoleLog("base layer changed to: " + changeEvent.layer.name);
					document.getElementById("legend_baseLayer_value").innerHTML=changeEvent.layer.name;
				});
				map.on('overlayadd', function baselayerchangeEvent(changeEvent) {
					consoleLog("overlayer added: " + changeEvent.name);
				});
				map.on('overlayremove', function baselayerchangeEvent(changeEvent) {
					consoleLog("overlayer removed: " + changeEvent.name);
				});
				controlLayers.addTo(map);
				
				consoleLog("Added baseLayer to map: " + baseLayer.name + "; default: " + defaultBaseMap);	
			}
			else {
				errorPopup(new Error("Cannot load: " + defaultBaseMap + "; not found in basemapArray"));
			}
		} // End of initBaseMaps()
		
}; // End of mapArrays() object	
						
// Eof