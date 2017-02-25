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
 * Parameters:	basemapOptions, mapArrays object
 * Returns:		Basemap() Object
 * Description:	Create Basemap object
 */	
function Basemap(basemapOptions, mapArrays) { 
	this.name=basemapOptions.name||"UNK";
	this.tileLayer=basemapOptions.tileLayer;
	this.tileLayer.mapArrays=mapArrays; // Add pointer to container object
	if (this.tileLayer.mapArrays) {
		this.tileLayer.mapArrays.cacheStatsArray[this.name]={
			hits: 0,
			misses: 0,
			errors: 0
		};
		this.tileLayer.mapArrays.basemapArray.push(this);
	}
	else {
		consoleError("Basemap() constructor: no mapArrays object");
	}
	
	this.tileLayer.on('tileerror', function(tile) {
		consoleError("Error: loading " + this.name + " tile: " + JSON.stringify(tile.coords)||"UNK");
		// this.tileLayer.mapArrays.cacheStatsArray[baseLayer.name].errors++;
	});
	
} // End of Basemap() object constructor

/*
 * Function: 	mapArray() 
 * Parameters:  map, defaultBaseMap, maxZoomlevel
 * Returns:		mapArray() Object
 * Description: Constructot for mapArrays
 */
function mapArrays(map, defaultBaseMap, maxZoomlevel) {
		this.basemapArray=[];
		this.overlaymapArray=[];
		this.cacheStatsArray={};
		
		this.cacheSize=0;
		this.totalTiles=0;					
		this.pouchDB=undefined;

		this.initBaseMaps(map, defaultBaseMap, maxZoomlevel);
	} // End of mapArrays() object constructor
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
						attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
					useCache: true,
					crossOrigin: true
				})}, this);
			new Basemap({
				name: "OpenStreetMap BlackAndWhite", 
				tileLayer: L.tileLayer('http://{s}.tiles.wmflabs.org/bw-mapnik/{z}/{x}/{y}.png', {
						attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
					useCache: false, // Not CORS (Cross-Origin Resource Sharing) compliant
					crossOrigin: false
				})}, this); 
			new Basemap({
				name: "OpenTopoMap", 
				tileLayer: L.tileLayer('http://{s}.tile.opentopomap.org/{z}/{x}/{y}.png', { 
					attribution: 'Map data: &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>, <a href="http://viewfinderpanoramas.org" target="_blank">SRTM</a> | Map style: &copy; <a href="https://opentopomap.org" target="_blank">OpenTopoMap</a> (<a href="https://creativecommons.org/licenses/by-sa/3.0/" target="_blank">CC-BY-SA</a>)',
					useCache: false, // Not CORS (Cross-Origin Resource Sharing) compliant
					crossOrigin: false
				})}, this);
			new Basemap({
				 name: "Humanitarian OpenStreetMap", 
				 tileLayer: L.tileLayer('http://{s}.tile.openstreetmap.fr/hot/{z}/{x}/{y}.png', {
						attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>, Tiles courtesy of <a href="http://hot.openstreetmap.org/" target="_blank">Humanitarian OpenStreetMap Team</a>',
					useCache: true,
					crossOrigin: true
				})}, this);
			new Basemap({
				name: "Thunderforest OpenCycleMap", 
				tileLayer: L.tileLayer('http://{s}.tile.thunderforest.com/cycle/{z}/{x}/{y}.png', { // API key required
					attribution: '&copy; <a href="http://www.thunderforest.com/" target="_blank">Thunderforest</a>, &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
					useCache: true,
					crossOrigin: true
				})}, this);
			new Basemap({
				name: "Thunderforest Transport", 
				tileLayer: L.tileLayer('http://{s}.tile.thunderforest.com/transport/{z}/{x}/{y}.png', { // API key required
					attribution: '&copy; <a href="http://www.thunderforest.com/" target="_blank">Thunderforest</a>, &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
					useCache: true,
					crossOrigin: true
				})}, this);
			new Basemap({
				name: "Thunderforest TransportDark", 
				tileLayer: L.tileLayer('http://{s}.tile.thunderforest.com/transport-dark/{z}/{x}/{y}.png', { // API key required
					attribution: '&copy; <a href="http://www.thunderforest.com/" target="_blank">Thunderforest</a>, &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
					useCache: true,
					crossOrigin: true
				})}, this);
			new Basemap({
				name: "Thunderforest Landscape", 
				tileLayer: L.tileLayer('http://{s}.tile.thunderforest.com/landscape/{z}/{x}/{y}.png', { // API key required
					attribution: '&copy; <a href="http://www.thunderforest.com/" target="_blank">Thunderforest</a>, &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
					useCache: true,
					crossOrigin: true
				})}, this);
			new Basemap({
				name: "Thunderforest SpinalMap", 
				tileLayer: L.tileLayer('http://{s}.tile.thunderforest.com/spinal-map/{z}/{x}/{y}.png', { // API key required
					attribution: '&copy; <a href="http://www.thunderforest.com/" target="_blank">Thunderforest</a>, &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
					useCache: true,
					crossOrigin: true
				})}, this);
			new Basemap({
				name: "Thunderforest Outdoors", 
				tileLayer: L.tileLayer('http://{s}.tile.thunderforest.com/outdoors/{z}/{x}/{y}.png', { // API key required
					attribution: '&copy; <a href="http://www.thunderforest.com/" target="_blank">Thunderforest</a>, &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
					useCache: true,
					crossOrigin: true
				})}, this);
			new Basemap({
				name: "Thunderforest Pioneer", 
				tileLayer: L.tileLayer('http://{s}.tile.thunderforest.com/pioneer/{z}/{x}/{y}.png', { // API key required
					attribution: '&copy; <a href="http://www.thunderforest.com/" target="_blank">Thunderforest</a>, &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
					useCache: true,
					crossOrigin: true
				})}, this);
			new Basemap({
				name: "OpenMapSurfer Roads", 
				tileLayer: L.tileLayer('http://korona.geog.uni-heidelberg.de/tiles/roads/x={x}&y={y}&z={z}', {
					attribution: 'Imagery from <a href="http://giscience.uni-hd.de/" target="_blank">GIScience Research Group @ University of Heidelberg</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
					useCache: true,
					crossOrigin: true
				})}, this);
			new Basemap({
				name: "OpenMapSurfer Grayscale", 
				tileLayer: L.tileLayer('http://korona.geog.uni-heidelberg.de/tiles/roadsg/x={x}&y={y}&z={z}', {
					attribution: 'Imagery from <a href="http://giscience.uni-hd.de/" target="_blank">GIScience Research Group @ University of Heidelberg</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
					useCache: true,
					crossOrigin: true
				})}, this);
			new Basemap({
				name: "Hydda Full", 
				tileLayer: L.tileLayer('http://{s}.tile.openstreetmap.se/hydda/full/{z}/{x}/{y}.png', {
					attribution: 'Tiles courtesy of <a href="http://openstreetmap.se/" target="_blank">OpenStreetMap Sweden</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
					useCache: false, // Not CORS (Cross-Origin Resource Sharing) compliant
					crossOrigin: false
				})}, this);
			new Basemap({
				name: "Hydda Base", 
				tileLayer: L.tileLayer('http://{s}.tile.openstreetmap.se/hydda/base/{z}/{x}/{y}.png', {
					attribution: 'Tiles courtesy of <a href="http://openstreetmap.se/" target="_blank">OpenStreetMap Sweden</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
					useCache: false, // Not CORS (Cross-Origin Resource Sharing) compliant
					crossOrigin: false
				})}, this);
			new Basemap({
				name: "Stamen Toner", 
				tileLayer: L.tileLayer('http://stamen-tiles-{s}.a.ssl.fastly.net/toner/{z}/{x}/{y}.{ext}', {
					attribution: 'Map tiles by <a href="http://stamen.com" target="_blank">Stamen Design</a>, <a href="http://creativecommons.org/licenses/by/3.0" target="_blank">CC BY 3.0</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
					subdomains: 'abcd',
					ext: 'png',
					useCache: true,
					crossOrigin: true
				})}, this);
			new Basemap({
				name: "Stamen TonerBackground", 
				tileLayer: L.tileLayer('http://stamen-tiles-{s}.a.ssl.fastly.net/toner-background/{z}/{x}/{y}.{ext}', {
					attribution: 'Map tiles by <a href="http://stamen.com" target="_blank">Stamen Design</a>, <a href="http://creativecommons.org/licenses/by/3.0" target="_blank">CC BY 3.0</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
					subdomains: 'abcd',
					ext: 'png',
					useCache: true,
					crossOrigin: true
				})}, this);
			new Basemap({
				name: "Stamen TonerLite", 
				tileLayer: L.tileLayer('http://stamen-tiles-{s}.a.ssl.fastly.net/toner-lite/{z}/{x}/{y}.{ext}', {
					attribution: 'Map tiles by <a href="http://stamen.com" target="_blank">Stamen Design</a>, <a href="http://creativecommons.org/licenses/by/3.0" target="_blank">CC BY 3.0</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
					subdomains: 'abcd',
					ext: 'png',
					useCache: true,
					crossOrigin: true
				})}, this);
			new Basemap({
				name: "Stamen Watercolor", 
				tileLayer: L.tileLayer('http://stamen-tiles-{s}.a.ssl.fastly.net/watercolor/{z}/{x}/{y}.{ext}', {
					attribution: 'Map tiles by <a href="http://stamen.com" target="_blank">Stamen Design</a>, <a href="http://creativecommons.org/licenses/by/3.0" target="_blank">CC BY 3.0</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
					subdomains: 'abcd',
					ext: 'png',
					useCache: true,
					crossOrigin: true
				})}, this);		
			new Basemap({
				name: "Esri WorldStreetMap", 
				tileLayer: L.tileLayer('http://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/{z}/{y}/{x}', {
					attribution: 'Tiles &copy; Esri &mdash; Source: Esri, DeLorme, NAVTEQ, USGS, Intermap, iPC, NRCAN, Esri Japan, METI, Esri China (Hong Kong), Esri (Thailand), TomTom, 2012',
					useCache: true,
					crossOrigin: true
				})}, this);
			new Basemap({
				name: "Esri DeLorme", 
				tileLayer: L.tileLayer('http://server.arcgisonline.com/ArcGIS/rest/services/Specialty/DeLorme_World_Base_Map/MapServer/tile/{z}/{y}/{x}', {
					attribution: 'Tiles &copy; Esri &mdash; Copyright: &copy;2012 DeLorme',
					useCache: true,
					crossOrigin: true
				})}, this);
			new Basemap({
				name: "Esri WorldTopoMap", 
				tileLayer: L.tileLayer('http://server.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer/tile/{z}/{y}/{x}', {
					attribution: 'Tiles &copy; Esri &mdash; Esri, DeLorme, NAVTEQ, TomTom, Intermap, iPC, USGS, FAO, NPS, NRCAN, GeoBase, Kadaster NL, Ordnance Survey, Esri Japan, METI, Esri China (Hong Kong), and the GIS User Community',
					useCache: true,
					crossOrigin: true
				})}, this);
			new Basemap({
				name: "Esri WorldImagery", 
				tileLayer: L.tileLayer('http://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
					attribution: 'Tiles &copy; Esri &mdash; Source: Esri, i-cubed, USDA, USGS, AEX, GeoEye, Getmapping, Aerogrid, IGN, IGP, UPR-EGP, and the GIS User Community',
					useCache: true,
					crossOrigin: true
				})}, this);
			new Basemap({
				name: "Esri WorldTerrain", 
				tileLayer: L.tileLayer('http://server.arcgisonline.com/ArcGIS/rest/services/World_Terrain_Base/MapServer/tile/{z}/{y}/{x}', {
					attribution: 'Tiles &copy; Esri &mdash; Source: USGS, Esri, TANA, DeLorme, and NPS',
					useCache: true,
					crossOrigin: true
				})}, this);
			new Basemap({
				name: "Esri WorldShadedRelief", 
				tileLayer: L.tileLayer('http://server.arcgisonline.com/ArcGIS/rest/services/World_Shaded_Relief/MapServer/tile/{z}/{y}/{x}', {
					attribution: 'Tiles &copy; Esri &mdash; Source: Esri',
					useCache: true,
					crossOrigin: true
				})}, this);
			new Basemap({
				name: "Esri WorldPhysical ", 
				tileLayer: L.tileLayer('http://server.arcgisonline.com/ArcGIS/rest/services/World_Physical_Map/MapServer/tile/{z}/{y}/{x}', {
					attribution: 'Tiles &copy; Esri &mdash; Source: US National Park Service',
					useCache: true,
					crossOrigin: true
				})}, this);
			new Basemap({
				name: "Esri OceanBasemap", 
				tileLayer: L.tileLayer('http://server.arcgisonline.com/ArcGIS/rest/services/Ocean_Basemap/MapServer/tile/{z}/{y}/{x}', {
					attribution: 'Tiles &copy; Esri &mdash; Sources: GEBCO, NOAA, CHS, OSU, UNH, CSUMB, National Geographic, DeLorme, NAVTEQ, and Esri',
					useCache: true,
					crossOrigin: true
				})}, this);
			new Basemap({
				name: "Esri NatGeoWorldMap", 
				tileLayer: L.tileLayer('http://server.arcgisonline.com/ArcGIS/rest/services/NatGeo_World_Map/MapServer/tile/{z}/{y}/{x}', {
					attribution: 'Tiles &copy; Esri &mdash; National Geographic, Esri, DeLorme, NAVTEQ, UNEP-WCMC, USGS, NASA, ESA, METI, NRCAN, GEBCO, NOAA, iPC',
					useCache: true,
					crossOrigin: true
				})}, this);
			new Basemap({
				name: "Esri WorldGrayCanvas", 
				tileLayer: L.tileLayer('http://server.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Light_Gray_Base/MapServer/tile/{z}/{y}/{x}', {
					attribution: 'Tiles &copy; Esri &mdash; Esri, DeLorme, NAVTEQ',
					useCache: true,
					crossOrigin: true
				})}, this);
			new Basemap({
				name: "CartoDB Positron", 
				tileLayer: L.tileLayer('http://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png', {
					attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a> &copy; <a href="http://cartodb.com/attributions" target="_blank">CartoDB</a>',
					subdomains: 'abcd',
					useCache: true,
					crossOrigin: true
				})}, this);
			new Basemap({
				name: "CartoDB PositronNoLabels", 
				tileLayer: L.tileLayer('http://{s}.basemaps.cartocdn.com/light_nolabels/{z}/{x}/{y}.png', {
					attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a> &copy; <a href="http://cartodb.com/attributions" target="_blank">CartoDB</a>',
					subdomains: 'abcd',
					useCache: true,
					crossOrigin: true
				})}, this);
			new Basemap({
				name: "CartoDB PositronOnlyLabels", 
				tileLayer: L.tileLayer('http://{s}.basemaps.cartocdn.com/light_only_labels/{z}/{x}/{y}.png', {
					attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a> &copy; <a href="http://cartodb.com/attributions" target="_blank">CartoDB</a>',
					subdomains: 'abcd',
					useCache: true,
					crossOrigin: true
				})}, this);
			new Basemap({
				name: "CartoDB DarkMatter", 
				tileLayer: L.tileLayer('http://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}.png', {
					attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a> &copy; <a href="http://cartodb.com/attributions" target="_blank">CartoDB</a>',
					subdomains: 'abcd',
					useCache: true,
					crossOrigin: true
				})}, this);
			new Basemap({
				name: "CartoDB DarkMatterNoLabels", 
				tileLayer: L.tileLayer('http://{s}.basemaps.cartocdn.com/dark_nolabels/{z}/{x}/{y}.png', {
					attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a> &copy; <a href="http://cartodb.com/attributions" target="_blank">CartoDB</a>',
					subdomains: 'abcd',
					useCache: true,
					crossOrigin: true
				})}, this);
			new Basemap({
				name: "CartoDB DarkMatterOnlyLabels", 
				tileLayer: L.tileLayer('http://{s}.basemaps.cartocdn.com/dark_only_labels/{z}/{x}/{y}.png', {
					attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a> &copy; <a href="http://cartodb.com/attributions" target="_blank">CartoDB</a>',
					subdomains: 'abcd',
					useCache: true,
					crossOrigin: true
				})}, this);
			new Basemap({
				name: "HikeBike HikeBike", 
				tileLayer: L.tileLayer('http://{s}.tiles.wmflabs.org/hikebike/{z}/{x}/{y}.png', {
					attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
					useCache: false, // Not CORS (Cross-Origin Resource Sharing) compliant
					crossOrigin: false
				})}, this);
			new Basemap({
				name: "HikeBike HillShading", 
				tileLayer: L.tileLayer('http://{s}.tiles.wmflabs.org/hillshading/{z}/{x}/{y}.png', {
					attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
					useCache: false, // Not CORS (Cross-Origin Resource Sharing) compliant
					crossOrigin: false
				})}, this);
			new Basemap({
				name: "NASAGIBS ViirsEarthAtNight2012", 
				tileLayer: L.tileLayer('http://map1.vis.earthdata.nasa.gov/wmts-webmerc/VIIRS_CityLights_2012/default/{time}/{tilematrixset}{maxZoom}/{z}/{y}/{x}.{format}', {
					attribution: 'Imagery provided by services from the Global Imagery Browse Services (GIBS), operated by the NASA/GSFC/Earth Science Data and Information System (<a href="https://earthdata.nasa.gov" target="_blank">ESDIS</a>) with funding provided by NASA/HQ.',
					bounds: [[-85.0511287776, -179.999999975], [85.0511287776, 179.999999975]],
					minZoom: 1,
					maxZoom: 8,
					format: 'jpg',
					time: '',
					tilematrixset: 'GoogleMapsCompatible_Level',
					useCache: true,
					crossOrigin: true
				})}, this);
			//Additional
			new Basemap({
				name: "OSM UK Postcodes", 
				tileLayer: L.tileLayer('http://random.dev.openstreetmap.org/postcodes/tiles/pc-npe/{z}/{x}/{y}.png', {
					attribution: '&copy; <a href="http://random.dev.openstreetmap.org/postcodes/" target="_blank">OSM Postcode</a>',
					useCache: true,
					crossOrigin: true
				})}, this);
			new Basemap({
				name: "Code-Point Open UK Postcodes", 
				tileLayer: L.tileLayer('http://random.dev.openstreetmap.org/postcodes/tiles/pc-os/{z}/{x}/{y}.png', {
					attribution: '&copy; <a href="http://random.dev.openstreetmap.org/postcodes/" target="_blank">Code-Point Open layers</a>',
					useCache: true,
					crossOrigin: true
				})}, this);
								
			var currentBaseMap;	
			var layerList = {};
			for (var i=0; i<this.basemapArray.length; i++) { // Add handlers
				
				this.basemapArray[i].tileLayer.on('tilecachehit', function tileCacheHitHandler(ev) {
					if (baseLayer && baseLayer.mapArrays && baseLayer.mapArrays.cacheStatsArray && 
					    baseLayer.mapArrays.cacheStatsArray[baseLayer.name]) {
						baseLayer.mapArrays.cacheStatsArray[baseLayer.name].hits++;
						consoleLog("tileCacheHitHandler(): Cache hit " + baseLayer.name + " tile: " + ev.url +
							"; total hits: " + baseLayer.mapArrays.cacheStatsArray[baseLayer.name].hits);
					}
					else {
						consoleLog("tileCacheHitHandler(): Cache hit " + baseLayer.name + " tile: " + ev.url + " [No stats update]");
					}
				});
				this.basemapArray[i].tileLayer.on('tilecachemiss', function tileCacheMissHandler(ev) {
					if (baseLayer && baseLayer.mapArrays && baseLayer.mapArrays.cacheStatsArray && 
					    baseLayer.mapArrays.cacheStatsArray[baseLayer.name]) {
						baseLayer.mapArrays.cacheStatsArray[baseLayer.name].misses++;
						consoleLog("tileCacheMissHandler(): Cache miss " + baseLayer.name + " tile: " + ev.url+
							"; total misses: " + baseLayer.mapArrays.cacheStatsArray[baseLayer.name].misses);
					}
					else {
						consoleLog("tileCacheMissHandler(): Cache miss " + baseLayer.name + " tile: " + ev.url + " [No stats update]");
					}
				});
				this.basemapArray[i].tileLayer.on('tilecacheerror', function tileCacheErrorHandler(ev) {
					if (baseLayer && baseLayer.mapArrays && baseLayer.mapArrays.cacheStatsArray && 
					    baseLayer.mapArrays.cacheStatsArray[baseLayer.name]) {
						baseLayer.mapArrays.cacheStatsArray[baseLayer.name].errors++;
						consoleLog("tileCacheErrorHandler(): Cache error: " + ev.error + "; " + baseLayer.name + ": " + ev.tile+
							"; total errors: " + baseLayer.mapArrays.cacheStatsArray[baseLayer.name].errors);
					}
					else {
						consoleLog("tileCacheErrorHandler(): Cache error: " + ev.error + "; " + baseLayer.name + ": " + ev.tile + 
							" [No stats update]");
					}
				});
				
				this.basemapArray[i].tileLayer.name=this.basemapArray[i].name;
				if (currentBaseMap == undefined) {
					currentBaseMap=this.basemapArray[i].tileLayer;
				}
				layerList[this.basemapArray[i].name]=this.basemapArray[i].tileLayer;
				if (this.pouchDB == undefined && this.basemapArray[i].tileLayer._db) {
					this.pouchDB=this.basemapArray[i].tileLayer._db;
				}
			}
				
			var overlayList = {};
			for (var i=0; i<this.overlaymapArray.length; i++) {
				this.overlaymapArray[i].tileLayer.name=this.overlaymapArray[i].name;
				overlayList[this.overlaymapArray[i].name]=this.overlaymapArray[i].tileLayer;
			}
			
			if (layerList[defaultBaseMap]) {			
				currentBaseMap=layerList[defaultBaseMap];
			}
			else {
				errorPopup(new Error("initBaseMaps(): Cannot load: " + defaultBaseMap + "; not found in basemapArray"));
			}
			
			if (currentBaseMap) {
				baseLayer = currentBaseMap;
					
				controlLayers=L.control.layers(
					layerList, 		// Base layers 
					overlayList, 	// Overlays
				{
					position: 'topright',
					collapsed: true
				});	
				controlLayers.addTo(map);
				
				currentBaseMap.on('load', function onBaseMapLoad(ev) {		
					consoleLog("initBaseMaps(): Added baseLayer to map: " + baseLayer.name + "; default: " + defaultBaseMap);
					this._getCacheSize(
						function _getCacheSizeCallback(err) {
							if (err) {
								errorPopup(new Error("initBaseMaps(): _getCacheSize() error: " + err.message));
							}
							else {
								consoleLog("initBaseMaps(): _getCacheSize() done.");
							}
						});
					});	
				currentBaseMap.addTo(map);	
				
				map.on('baselayerchange', function baselayerchangeEvent(changeEvent) {
					baseLayer=changeEvent.layer;
					if (changeEvent.layer.mapArrays) {	
						changeEvent.layer.mapArrays._getCacheSize();
					}
					if (changeEvent.layer._db) {	
						changeEvent.layer._db.info(
							function getInfo(err, result) {		
								if (err) {
									consoleError("Error: " + err.message + " in _db.info() for base layer: " + changeEvent.layer.name);			
								}
								else {
									consoleLog("base layer changed to: " + changeEvent.layer.name + "; cache info: " + 
										JSON.stringify(result));
								}							
							});
					}
					else {
						consoleLog("baselayerchangeEvent(): Base layer changed to: " + changeEvent.layer.name + "; not cached");
					}
					document.getElementById("legend_baseLayer_value").innerHTML=changeEvent.layer.name;	
				});
				map.on('overlayadd', function overlayAddEvent(changeEvent) {
					consoleLog("overlayAddEvent(): overlay added: " + changeEvent.name);
				});
				map.on('overlayremove', function overlayRemoveEvent(changeEvent) {
					consoleLog("overlayRemoveEvent(): overlay removed: " + changeEvent.name);
				});
								
				currentBaseMap.off('load');				
			}
			else {
				errorPopup(new Error("initBaseMaps(): Cannot load basemap, no currentBaseMap"));
			}
		}, // End of initBaseMaps()
		/*
		 * Function: 	_getCacheSize()
		 * Parameters:	callback (optional)
		 * Returns:		Nothing (use callback to access this.cacheSize, this.totalTiles)
		 * Description:	Add basemap to basemap array
		 */	
		_getCacheSize: function(getCacheSizeCallback) {
			if (this.pouchDB) {
				this.pouchDB.allDocs({
						include_docs: true,
						attachments: true
					}, 
					function getAllDocs(err, result) {
						if (err) {
							consoleError("_getCacheSize(): Error: " + err.message + " in _db.allDocs()");
							if (getCacheSizeCallback) {
								getCacheSizeCallback(err);
							}		
						}
						else {
							this.cacheSize=0;
							this.totalTiles=result.total_rows;
							for (var i=0; i<result.total_rows; i++) {
								this.cacheSize+=result.rows[i].doc.dataUrl.length;
							}
							
							var cacheRows="";				
							if (this.cacheStatsArray) {
								var cacheStatsArray=Object.keys(this.cacheStatsArray);
								for (var i=0; i<cacheStatsArray.length; i++) {
									if (this.cacheStatsArray[cacheStatsArray[i]].hits > 0 ||
										this.cacheStatsArray[cacheStatsArray[i]].misses > 0 ||
										this.cacheStatsArray[cacheStatsArray[i]].errors > 0) {
										cacheRows+="\n[" + i + "] " + cacheStatsArray[i] + 
											": hits: " + this.cacheStatsArray[cacheStatsArray[i]].hits +
											"misses: " + this.cacheStatsArray[cacheStatsArray[i]].misses +
											"errors: " + this.cacheStatsArray[cacheStatsArray[i]].errors;
									}
								} 
							}
							else {
								consoleError("_getCacheSize() no cacheStatsArray");
							}
							consoleLog("_getCacheSize(): " + result.total_rows + " items; size: " + this.cacheSize + cacheRows + " bytes");
							if (getCacheSizeCallback) {
								getCacheSizeCallback();
							}
						}
					}
				);
			}
		} // End of getCacheSize
		
}; // End of mapArrays() object	
						
// Eof