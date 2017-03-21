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
var autoCompaction=true;
	
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
	this.tileLayer.cacheStats={
		hits: 0,
		misses: 0,
		errors: 0,
		tiles: 0,
		size: 0
	};
	if (this.tileLayer.mapArrays) {
		this.tileLayer.mapArrays.basemapArray.push(this);
	}
	else {
		consoleError("Basemap() constructor: no mapArrays object");
	}
	
	this.tileLayer.on('tileerror', function(tile) {
		consoleError("Error: loading " + this.name + " baselayer tile: " + JSON.stringify(tile.coords)||"UNK");
		this.cacheStats.errors++;
	});
	
} // End of Basemap() object constructor

/*
 * Function: 	Overlaymap()
 * Parameters:	basemapOptions, mapArrays object
 * Returns:		Basemap() Object
 * Description:	Create Basemap object
 */	
function Overlaymap(overlaymapOptions, mapArrays) { 
	this.name=overlaymapOptions.name||"UNK";
	this.tileLayer=overlaymapOptions.tileLayer;
	this.tileLayer.mapArrays=mapArrays; // Add pointer to container object
	this.tileLayer.cacheStats={
		hits: 0,
		misses: 0,
		errors: 0,
		tiles: 0,
		size: 0
	};
	if (this.tileLayer.mapArrays) {
		this.tileLayer.mapArrays.overlaymapArray.push(this);
	}
	else {
		consoleError("Overlaymap() constructor: no mapArrays object");
	}
	
	this.tileLayer.on('tileerror', function(tile) {
		consoleError("Error: loading " + this.name + " overlay tile: " + JSON.stringify(tile.coords)||"UNK");
		this.cacheStats.errors++;
	});
	
} // End of Overlaymap() object constructor

/*
 * Function: 	mapArray() 
 * Parameters:  map, defaultBaseMap, maxZoomlevel
 * Returns:		mapArray() Object
 * Description: Constructot for mapArrays
 */
function mapArrays(map, defaultBaseMap, maxZoomlevel, options) {
		this.basemapArray=[];
		this.overlaymapArray=[];
		
		this.cacheSize=0;
		this.totalTiles=0;					
		this.pouchDB=undefined;
		
		if (options && options.auto_compaction) {
			this.options.auto_compaction=options.auto_compaction;
		}

		this.initBaseMaps(map, defaultBaseMap, maxZoomlevel);
	} // End of mapArrays() object constructor
	
	mapArrays.prototype = { // Add methods
		options:	{
			auto_compaction: autoCompaction	// option auto_compaction: true/false
			// This turns on auto compaction, which means compact() is called after every change to the database. Defaults to false.
		},
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
					maxZoom: 18,
					useCache: true,
					crossOrigin: true,
					auto_compaction: this.options.auto_compaction
				})}, this);
			new Basemap({
				name: "OpenStreetMap BlackAndWhite", 
				tileLayer: L.tileLayer('http://{s}.tiles.wmflabs.org/bw-mapnik/{z}/{x}/{y}.png', {
						attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
					maxZoom: 16,
					useCache: false, // Not CORS (Cross-Origin Resource Sharing) compliant
					crossOrigin: false
				})}, this); 
			new Basemap({
				name: "OpenTopoMap", 
				tileLayer: L.tileLayer('http://{s}.tile.opentopomap.org/{z}/{x}/{y}.png', { 
					attribution: 'Map data: &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>, <a href="http://viewfinderpanoramas.org" target="_blank">SRTM</a> | Map style: &copy; <a href="https://opentopomap.org" target="_blank">OpenTopoMap</a> (<a href="https://creativecommons.org/licenses/by-sa/3.0/" target="_blank">CC-BY-SA</a>)',
					maxZoom: 17,
					useCache: false, // Not CORS (Cross-Origin Resource Sharing) compliant
					crossOrigin: false
				})}, this);
			new Basemap({
				 name: "Humanitarian OpenStreetMap", 
				 tileLayer: L.tileLayer('http://{s}.tile.openstreetmap.fr/hot/{z}/{x}/{y}.png', {
						attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>, Tiles courtesy of <a href="http://hot.openstreetmap.org/" target="_blank">Humanitarian OpenStreetMap Team</a>',
					maxZoom: 17,
					useCache: true,
					crossOrigin: true,
					auto_compaction: this.options.auto_compaction
				})}, this);
				
			new Basemap({
				name: "Thunderforest OpenCycleMap", 
				tileLayer: L.tileLayer('http://{s}.tile.thunderforest.com/cycle/{z}/{x}/{y}.png', { // API key required
					attribution: '&copy; <a href="http://www.thunderforest.com/" target="_blank">Thunderforest</a>, &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
					maxZoom: 17,
					useCache: true,
					crossOrigin: true,
					auto_compaction: this.options.auto_compaction
				})}, this);
			new Basemap({
				name: "Thunderforest Railways", 
				tileLayer: L.tileLayer('http://{s}.tile.thunderforest.com/transport/{z}/{x}/{y}.png', { // API key required
					attribution: '&copy; <a href="http://www.thunderforest.com/" target="_blank">Thunderforest</a>, &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
					maxZoom: 17,
					useCache: true,
					crossOrigin: true,
					auto_compaction: this.options.auto_compaction
				})}, this);
			new Basemap({
				name: "Thunderforest Railways Dark", 
				tileLayer: L.tileLayer('http://{s}.tile.thunderforest.com/transport-dark/{z}/{x}/{y}.png', { // API key required
					attribution: '&copy; <a href="http://www.thunderforest.com/" target="_blank">Thunderforest</a>, &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
					maxZoom: 17,
					useCache: true,
					crossOrigin: true,
					auto_compaction: this.options.auto_compaction
				})}, this);
			new Basemap({
				name: "Thunderforest Landscape", 
				tileLayer: L.tileLayer('http://{s}.tile.thunderforest.com/landscape/{z}/{x}/{y}.png', { // API key required
					attribution: '&copy; <a href="http://www.thunderforest.com/" target="_blank">Thunderforest</a>, &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
					maxZoom: 17,
					useCache: true,
					crossOrigin: true,
					auto_compaction: this.options.auto_compaction
				})}, this);
			new Basemap({
				name: "Thunderforest SpinalMap", 
				tileLayer: L.tileLayer('http://{s}.tile.thunderforest.com/spinal-map/{z}/{x}/{y}.png', { // API key required
					attribution: '&copy; <a href="http://www.thunderforest.com/" target="_blank">Thunderforest</a>, &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
					maxZoom: 17,
					useCache: true,
					crossOrigin: true,
					auto_compaction: this.options.auto_compaction
				})}, this);
			new Basemap({
				name: "Thunderforest Outdoors", 
				tileLayer: L.tileLayer('http://{s}.tile.thunderforest.com/outdoors/{z}/{x}/{y}.png', { // API key required
					attribution: '&copy; <a href="http://www.thunderforest.com/" target="_blank">Thunderforest</a>, &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
					maxZoom: 17,
					useCache: true,
					crossOrigin: true,
					auto_compaction: this.options.auto_compaction
				})}, this);
			new Basemap({
				name: "Thunderforest Pioneer", 
				tileLayer: L.tileLayer('http://{s}.tile.thunderforest.com/pioneer/{z}/{x}/{y}.png', { // API key required
					attribution: '&copy; <a href="http://www.thunderforest.com/" target="_blank">Thunderforest</a>, &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
					maxZoom: 17,
					useCache: true,
					crossOrigin: true,
					auto_compaction: this.options.auto_compaction
				})}, this);
				
			new Basemap({
				name: "OpenMapSurfer Roads", 
				tileLayer: L.tileLayer('http://korona.geog.uni-heidelberg.de/tiles/roads/x={x}&y={y}&z={z}', {
					attribution: 'Imagery from <a href="http://giscience.uni-hd.de/" target="_blank">GIScience Research Group @ University of Heidelberg</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
					maxZoom: 17,
					useCache: true,
					crossOrigin: true,
					auto_compaction: this.options.auto_compaction
				})}, this);
			new Basemap({
				name: "OpenMapSurfer Grayscale", 
				tileLayer: L.tileLayer('http://korona.geog.uni-heidelberg.de/tiles/roadsg/x={x}&y={y}&z={z}', {
					attribution: 'Imagery from <a href="http://giscience.uni-hd.de/" target="_blank">GIScience Research Group @ University of Heidelberg</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
					maxZoom: 17,
					useCache: true,
					crossOrigin: true,
					auto_compaction: this.options.auto_compaction
				})}, this);
				
			new Basemap({
				name: "Hydda Full", 
				tileLayer: L.tileLayer('http://{s}.tile.openstreetmap.se/hydda/full/{z}/{x}/{y}.png', {
					attribution: 'Tiles courtesy of <a href="http://openstreetmap.se/" target="_blank">OpenStreetMap Sweden</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
					maxZoom: 17,
					useCache: false, // Not CORS (Cross-Origin Resource Sharing) compliant
					crossOrigin: false
				})}, this);
			new Basemap({
				name: "Hydda Base", 
				tileLayer: L.tileLayer('http://{s}.tile.openstreetmap.se/hydda/base/{z}/{x}/{y}.png', {
					attribution: 'Tiles courtesy of <a href="http://openstreetmap.se/" target="_blank">OpenStreetMap Sweden</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
					maxZoom: 17,
					useCache: false, // Not CORS (Cross-Origin Resource Sharing) compliant
					crossOrigin: false
				})}, this);
				
			new Basemap({
				name: "Stamen Toner", 
				tileLayer: L.tileLayer('http://stamen-tiles-{s}.a.ssl.fastly.net/toner/{z}/{x}/{y}.{ext}', {
					attribution: 'Map tiles by <a href="http://stamen.com" target="_blank">Stamen Design</a>, <a href="http://creativecommons.org/licenses/by/3.0" target="_blank">CC BY 3.0</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
					subdomains: 'abcd',
					ext: 'png',
					maxZoom: 17,
					useCache: true,
					crossOrigin: true,
					auto_compaction: this.options.auto_compaction
				})}, this);
			new Basemap({
				name: "Stamen TonerBackground", 
				tileLayer: L.tileLayer('http://stamen-tiles-{s}.a.ssl.fastly.net/toner-background/{z}/{x}/{y}.{ext}', {
					attribution: 'Map tiles by <a href="http://stamen.com" target="_blank">Stamen Design</a>, <a href="http://creativecommons.org/licenses/by/3.0" target="_blank">CC BY 3.0</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
					subdomains: 'abcd',
					maxZoom: 17,
					ext: 'png',
					useCache: true,
					crossOrigin: true,
					auto_compaction: this.options.auto_compaction
				})}, this);
			new Basemap({
				name: "Stamen TonerLite", 
				tileLayer: L.tileLayer('http://stamen-tiles-{s}.a.ssl.fastly.net/toner-lite/{z}/{x}/{y}.{ext}', {
					attribution: 'Map tiles by <a href="http://stamen.com" target="_blank">Stamen Design</a>, <a href="http://creativecommons.org/licenses/by/3.0" target="_blank">CC BY 3.0</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
					subdomains: 'abcd',
					maxZoom: 17,
					ext: 'png',
					useCache: true,
					crossOrigin: true,
					auto_compaction: this.options.auto_compaction
				})}, this);
			new Basemap({
				name: "Stamen Watercolor", 
				tileLayer: L.tileLayer('http://stamen-tiles-{s}.a.ssl.fastly.net/watercolor/{z}/{x}/{y}.{ext}', {
					attribution: 'Map tiles by <a href="http://stamen.com" target="_blank">Stamen Design</a>, <a href="http://creativecommons.org/licenses/by/3.0" target="_blank">CC BY 3.0</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
					subdomains: 'abcd',
					ext: 'png',
					maxZoom: 17,
					useCache: true,
					crossOrigin: true,
					auto_compaction: this.options.auto_compaction
				})}, this);		
				
			new Basemap({
				name: "Esri WorldStreetMap", 
				tileLayer: L.tileLayer('http://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/{z}/{y}/{x}', {
					attribution: 'Tiles &copy; Esri &mdash; Source: Esri, DeLorme, NAVTEQ, USGS, Intermap, iPC, NRCAN, Esri Japan, METI, Esri China (Hong Kong), Esri (Thailand), TomTom, 2012',
					maxZoom: 17,
					useCache: true,
					crossOrigin: true,
					auto_compaction: this.options.auto_compaction
				})}, this);
			new Basemap({
				name: "Esri DeLorme", 
				tileLayer: L.tileLayer('http://server.arcgisonline.com/ArcGIS/rest/services/Specialty/DeLorme_World_Base_Map/MapServer/tile/{z}/{y}/{x}', {
					attribution: 'Tiles &copy; Esri &mdash; Copyright: &copy;2012 DeLorme',
					maxZoom: 14,
					useCache: true,
					crossOrigin: true,
					auto_compaction: this.options.auto_compaction
				})}, this);
			new Basemap({
				name: "Esri WorldTopoMap", 
				tileLayer: L.tileLayer('http://server.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer/tile/{z}/{y}/{x}', {
					attribution: 'Tiles &copy; Esri &mdash; Esri, DeLorme, NAVTEQ, TomTom, Intermap, iPC, USGS, FAO, NPS, NRCAN, GeoBase, Kadaster NL, Ordnance Survey, Esri Japan, METI, Esri China (Hong Kong), and the GIS User Community',
					maxZoom: 17,
					useCache: true,
					crossOrigin: true,
					auto_compaction: this.options.auto_compaction
				})}, this);
			new Basemap({
				name: "Esri WorldImagery", 
				tileLayer: L.tileLayer('http://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
					attribution: 'Tiles &copy; Esri &mdash; Source: Esri, i-cubed, USDA, USGS, AEX, GeoEye, Getmapping, Aerogrid, IGN, IGP, UPR-EGP, and the GIS User Community',
					maxZoom: 17,
					useCache: true,
					crossOrigin: true,
					auto_compaction: this.options.auto_compaction
				})}, this);
			new Basemap({
				name: "Esri WorldTerrain", 
				tileLayer: L.tileLayer('http://server.arcgisonline.com/ArcGIS/rest/services/World_Terrain_Base/MapServer/tile/{z}/{y}/{x}', {
					attribution: 'Tiles &copy; Esri &mdash; Source: USGS, Esri, TANA, DeLorme, and NPS',
					maxZoom: 17,
					useCache: true,
					crossOrigin: true,
					auto_compaction: this.options.auto_compaction
				})}, this);
			new Basemap({
				name: "Esri WorldShadedRelief", 
				tileLayer: L.tileLayer('http://server.arcgisonline.com/ArcGIS/rest/services/World_Shaded_Relief/MapServer/tile/{z}/{y}/{x}', {
					attribution: 'Tiles &copy; Esri &mdash; Source: Esri',
					maxZoom: 17,
					useCache: true,
					crossOrigin: true,
					auto_compaction: this.options.auto_compaction
				})}, this);
			new Basemap({
				name: "Esri WorldPhysical ", 
				tileLayer: L.tileLayer('http://server.arcgisonline.com/ArcGIS/rest/services/World_Physical_Map/MapServer/tile/{z}/{y}/{x}', {
					attribution: 'Tiles &copy; Esri &mdash; Source: US National Park Service',
					maxZoom: 17,
					useCache: true,
					crossOrigin: true,
					auto_compaction: this.options.auto_compaction
				})}, this);
			new Basemap({
				name: "Esri OceanBasemap", 
				tileLayer: L.tileLayer('http://server.arcgisonline.com/ArcGIS/rest/services/Ocean_Basemap/MapServer/tile/{z}/{y}/{x}', {
					attribution: 'Tiles &copy; Esri &mdash; Sources: GEBCO, NOAA, CHS, OSU, UNH, CSUMB, National Geographic, DeLorme, NAVTEQ, and Esri',
					maxZoom: 17,
					useCache: true,
					crossOrigin: true,
					auto_compaction: this.options.auto_compaction
				})}, this);
			new Basemap({
				name: "Esri NatGeoWorldMap", 
				tileLayer: L.tileLayer('http://server.arcgisonline.com/ArcGIS/rest/services/NatGeo_World_Map/MapServer/tile/{z}/{y}/{x}', {
					attribution: 'Tiles &copy; Esri &mdash; National Geographic, Esri, DeLorme, NAVTEQ, UNEP-WCMC, USGS, NASA, ESA, METI, NRCAN, GEBCO, NOAA, iPC',
					maxZoom: 17,
					useCache: true,
					crossOrigin: true,
					auto_compaction: this.options.auto_compaction
				})}, this);
			new Basemap({
				name: "Esri WorldGrayCanvas", 
				tileLayer: L.tileLayer('http://server.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Light_Gray_Base/MapServer/tile/{z}/{y}/{x}', {
					attribution: 'Tiles &copy; Esri &mdash; Esri, DeLorme, NAVTEQ',
					maxZoom: 17,
					useCache: true,
					crossOrigin: true,
					auto_compaction: this.options.auto_compaction
				})}, this);

			new Basemap({
				name: "Google roadmap", 
				tileLayer: L.gridLayer.googleMutant({
					type: 'roadmap',
					maxZoom: 21,
					useCache: true,
					crossOrigin: true,
					auto_compaction: this.options.auto_compaction
				})}, this);
			new Basemap({
				name: "Google satellite", 
				tileLayer: L.gridLayer.googleMutant({
					type: 'satellite',
					maxZoom: 19,
					useCache: true,	// Does not work yet
					crossOrigin: true,
					auto_compaction: this.options.auto_compaction
				})}, this);
			new Basemap({
				name: "Google terrain", 
				tileLayer: L.gridLayer.googleMutant({
					type: 'terrain',
					maxZoom: 19,
					useCache: true,	// Does not work yet
					crossOrigin: true,
					auto_compaction: this.options.auto_compaction
				})}, this);
			new Basemap({
				name: "Google hybrid", 
				tileLayer: L.gridLayer.googleMutant({
					type:'hybrid',
					maxZoom: 19,
					useCache: true,	// Does not work yet
					crossOrigin: true,
					auto_compaction: this.options.auto_compaction
				})}, this); 
	
			new Basemap({
				name: "CartoDB Positron", 
				tileLayer: L.tileLayer('http://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png', {
					attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a> &copy; <a href="http://cartodb.com/attributions" target="_blank">CartoDB</a>',
					subdomains: 'abcd',
					maxZoom: 19,
					useCache: true,	// Does not work yet
					crossOrigin: true,
					auto_compaction: this.options.auto_compaction
				})}, this);
			new Basemap({
				name: "CartoDB PositronNoLabels", 
				tileLayer: L.tileLayer('http://{s}.basemaps.cartocdn.com/light_nolabels/{z}/{x}/{y}.png', {
					attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a> &copy; <a href="http://cartodb.com/attributions" target="_blank">CartoDB</a>',
					subdomains: 'abcd',
					maxZoom: 17,
					useCache: true,
					crossOrigin: true,
					auto_compaction: this.options.auto_compaction
				})}, this);
			new Basemap({
				name: "CartoDB PositronOnlyLabels", 
				tileLayer: L.tileLayer('http://{s}.basemaps.cartocdn.com/light_only_labels/{z}/{x}/{y}.png', {
					attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a> &copy; <a href="http://cartodb.com/attributions" target="_blank">CartoDB</a>',
					subdomains: 'abcd',
					maxZoom: 17,
					useCache: true,
					crossOrigin: true,
					auto_compaction: this.options.auto_compaction
				})}, this);
			new Basemap({
				name: "CartoDB DarkMatter", 
				tileLayer: L.tileLayer('http://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}.png', {
					attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a> &copy; <a href="http://cartodb.com/attributions" target="_blank">CartoDB</a>',
					subdomains: 'abcd',
					maxZoom: 17,
					useCache: true,
					crossOrigin: true,
					auto_compaction: this.options.auto_compaction
				})}, this);
			new Basemap({
				name: "CartoDB DarkMatterNoLabels", 
				tileLayer: L.tileLayer('http://{s}.basemaps.cartocdn.com/dark_nolabels/{z}/{x}/{y}.png', {
					attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a> &copy; <a href="http://cartodb.com/attributions" target="_blank">CartoDB</a>',
					subdomains: 'abcd',
					maxZoom: 17,
					useCache: true,
					crossOrigin: true,
					auto_compaction: this.options.auto_compaction
				})}, this);
			new Basemap({
				name: "CartoDB DarkMatterOnlyLabels", 
				tileLayer: L.tileLayer('http://{s}.basemaps.cartocdn.com/dark_only_labels/{z}/{x}/{y}.png', {
					attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a> &copy; <a href="http://cartodb.com/attributions" target="_blank">CartoDB</a>',
					subdomains: 'abcd',
					maxZoom: 17,
					useCache: true,
					crossOrigin: true,
					auto_compaction: this.options.auto_compaction
				})}, this);
			new Basemap({
				name: "HikeBike HikeBike", 
				tileLayer: L.tileLayer('http://{s}.tiles.wmflabs.org/hikebike/{z}/{x}/{y}.png', {
					attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
					useCache: false, // Not CORS (Cross-Origin Resource Sharing) compliant
					maxZoom: 15,
					crossOrigin: false,
					auto_compaction: this.options.auto_compaction
				})}, this);
			new Basemap({
				name: "HikeBike HillShading", 
				tileLayer: L.tileLayer('http://{s}.tiles.wmflabs.org/hillshading/{z}/{x}/{y}.png', {
					attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
					maxZoom: 15,
					useCache: false, // Not CORS (Cross-Origin Resource Sharing) compliant
					crossOrigin: false,
					auto_compaction: this.options.auto_compaction
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
					crossOrigin: true,
					auto_compaction: this.options.auto_compaction
				})}, this);
			//Additional overlays
			new Overlaymap({
				name: "OSM UK Postcodes", 
				tileLayer: L.tileLayer('http://random.dev.openstreetmap.org/postcodes/tiles/pc-npe/{z}/{x}/{y}.png', {
					attribution: '&copy; <a href="http://random.dev.openstreetmap.org/postcodes/" target="_blank">OSM Postcode</a>',
					useCache: false, // Not CORS (Cross-Origin Resource Sharing) compliant,
					maxZoom: 12,
					crossOrigin: false,
					auto_compaction: this.options.auto_compaction
				})}, this);
			new Overlaymap({
				name: "Code-Point Open UK Postcodes", 
				tileLayer: L.tileLayer('http://random.dev.openstreetmap.org/postcodes/tiles/pc-os/{z}/{x}/{y}.png', {
					attribution: '&copy; <a href="http://random.dev.openstreetmap.org/postcodes/" target="_blank">Code-Point Open layers</a>',
					useCache: false, // Not CORS (Cross-Origin Resource Sharing) compliant,
					maxZoom: 12,
					crossOrigin: false,
					auto_compaction: this.options.auto_compaction
				})}, this);
								
			var currentBaseMap;	
			var layerList = {};
			for (var i=0; i<this.basemapArray.length; i++) { // Add handlers
				
				this.basemapArray[i].tileLayer.on('tilecachehit', function tileCacheHitHandler(ev) {
					if (baseLayer && baseLayer.cacheStats) {
						baseLayer.cacheStats.hits++;
//						consoleLog("tileCacheHitHandler(): Cache hit " + baseLayer.name + " tile: " + ev.url +
//							"; total hits: " + baseLayer.cacheStats.hits);
					}
					else {
						consoleLog("tileCacheHitHandler(): Cache hit " + baseLayer.name + " tile: " + ev.url + " [No stats update]");
					}
				});
				this.basemapArray[i].tileLayer.on('tilecachemiss', function tileCacheMissHandler(ev) {
					if (baseLayer && baseLayer.cacheStats) {
						baseLayer.cacheStats.misses++;
//						consoleLog("tileCacheMissHandler(): Cache miss " + baseLayer.name + " tile: " + ev.url+
//							"; total misses: " + baseLayer.cacheStats.misses);
					}
					else {
						consoleLog("tileCacheMissHandler(): Cache miss " + baseLayer.name + " tile: " + ev.url + " [No stats update]");
					}
				});
				this.basemapArray[i].tileLayer.on('tilecacheerror', function tileCacheErrorHandler(ev) {
					if (baseLayer && baseLayer.cacheStats) {
						baseLayer.cacheStats.errors++;
//						consoleLog("tileCacheErrorHandler(): Cache error: " + ev.error + "; " + baseLayer.name + ": " + ev.tile+
//							"; total errors: " + baseLayer.cacheStats.errors);
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
					/*
					this.getCacheSize(
						function getCacheSizeCallback(err) {
							if (err) {
								errorPopup(new Error("initBaseMaps(): getCacheSize() error: " + err.message));
							}
							else {
								consoleLog("initBaseMaps(): getCacheSize() done.");
							}
						}); */
					});	
				
				currentBaseMap.on('load', function currentBaseMapLoad(ev) {
					
					if (baseLayer.options && baseLayer.options.useCache) {						
						consoleLog("currentBaseMapLoad(): Base layer loaded: " + baseLayer.name + "; cached");
					}
					else {
						consoleLog("currentBaseMapLoad(): Base layer loaded: " + baseLayer.name + "; not cached");
					}
					
					map.on('baselayerchange', function baselayerchangeEvent(changeEvent) {
						baseLayer=changeEvent.layer;
						if (changeEvent.layer.mapArrays) {	
							changeEvent.layer.mapArrays.getCacheSize();
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
				});
				currentBaseMap.addTo(map);			
			}
			else {
				errorPopup(new Error("initBaseMaps(): Cannot load basemap, no currentBaseMap"));
			}
		}, // End of initBaseMaps()
		/*
		 * Function: 	empty()
		 * Parameters:	callback(err, results)
		 * Returns:		Nothing (use callback to access this.cacheSize, this.totalTiles)
		 * Description:	Add basemap to basemap array
		 *				Then does a viewCleanup()
		 */	
		empty: function(emptyCallback) {
			
			scopeChecker({
				callback: emptyCallback
			})
			
			if (this.pouchDB) {

				var mapArrays=this;
				mapArrays.pouchDB.info(
					function getInfo(err, result) {			
						if (err) {
							consoleError("empty(): Error: " + err.message + " in _db.getInfo()");
							emptyCallback(err);
						}
						else {
							$( "#progressbar" ).progressbar({
								max: result.doc_count
							});
							
							mapArrays.pouchDB.allDocs({
									include_docs: true,
									attachments: false
								}, 
								function getAllDocs(err, result) {
									if (err) {
										consoleError("empty(): Error: " + err.message + " in _db.allDocs()");
										emptyCallback(err);
									}
									else {
										mapArrays.cacheSize=0;
										mapArrays.totalTiles=result.total_rows;
										var i=0;

										async.eachSeries(result.rows, 
											function emptyAsyncEachSeriesHandler(item, callback) {
												i++;	
												if (i%10 == 0) {
													$( "#progressbar" ).progressbar({
														value: i
													});
												}
											
												if (item.doc) {
													if (mapArrays.basemapArray) {
														for (var j=0; j<mapArrays.basemapArray.length; j++) {
															if (mapArrays.basemapArray[j].name == item.doc.name) {
																mapArrays.basemapArray[j].tileLayer.cacheStats.tiles=0;
																mapArrays.basemapArray[j].tileLayer.cacheStats.size=0;
																break; // Out of for loop
															}
														}
													}	
													if (mapArrays.overlaymapArray) {
														for (var j=0; j<mapArrays.overlaymapArray.length; j++) {
															if (mapArrays.overlaymapArray[j].name == item.doc.name) {
																mapArrays.overlaymapArray[j].tileLayer.cacheStats.tiles=0
																mapArrays.overlaymapArray[j].tileLayer.cacheStats.size=0;
																break; // Out of for loop
															}
														}
													}	
													mapArrays.pouchDB.remove(item.doc, undefined /* Options */, callback); 
												}
												else {
													throw new Error("emptyAsyncEachSeriesHandler: no doc in item: " + 
														JSON.stringify(item).substring(0, 200));
												}			
											}, 
											function emptyAsyncEachSeriesError(err) {
												if (err) {
													emptyCallback(err);
												}
												else {
													consoleLog("emptyAsyncEachSeriesError() deleted: " + mapArrays.totalTiles + " tiles");
													var results={ totalTiles: mapArrays.totalTiles };
													$( "#progressbar" ).progressbar({
														value: result.total_rows
													});
													mapArrays.compact(function() {
														emptyCallback(undefined, results);
													});
													
													mapArrays.totalTiles=0;
												}
											}
										);
									}
								}
							);							
						}
					}	
				);				

			}
			else {
				emptyCallback();
			}
		},
		/*
		 * Function: 	viewCleanup()
		 * Parameters:	callback (mandatory)
		 * Returns:		Nothing 
		 * Description:	Triggers a viewCleanup operation in the local pouchDB database
		 */	
		viewCleanup: function(viewCleanupCallback) {
			
			scopeChecker({
				callback: viewCleanupCallback
			})
			
			if (this.pouchDB && this.options.auto_compaction == false) {
				this.pouchDB.viewCleanup(viewCleanupCallback)
			}
			else {
				viewCleanupCallback();
			}
		},
		/*
		 * Function: 	compact()
		 * Parameters:	callback (mandatory)
		 * Returns:		Nothing 
		 * Description:	Triggers a compaction operation in the local pouchDB database if auto_compaction is not on
		 *				Then does a viewCleanup()
		 */	
		compact: function(compactCallback) {
			
			scopeChecker({
				callback: compactCallback
			})
			
			if (this.pouchDB && this.options.auto_compaction == false) {
				var mapArrays=this;
				this.pouchDB.compact(undefined /* Options */, 
					function() {
						mapArrays.pouchDB.viewCleanup(compactCallback); 
					});
			}
			else {
				compactCallback();
			}
		},
		/*
		 * Function: 	getCacheSize()
		 * Parameters:	callback(err, results) (optional)
		 * Returns:		Nothing (use callback to access this.cacheSize, this.totalTiles)
		 * Description:	Get cacheSize; returns data as results in callback:
		 *					results: {
		 *						tableHtml: 		tableHtml,
		 *						totalTiles: 	mapArrays.totalTiles,
		 *						cacheSize:		mapArrays.cacheSize,
		 *	 					autoCompaction:	baseLayer.options.auto_compaction
		 *					} 
		 *				Then does a viewCleanup()
		 */	
		getCacheSize: function(getCacheSizeCallback) {
			
			scopeChecker(undefined, {
				callback: getCacheSizeCallback
			})
			
			var mapArrays=this;
			
			if (this.pouchDB) {
				mapArrays.pouchDB.info( // convert to promises form
					function getInfo(err, result) {			
						if (err) {
							var nerr=new Error("getCacheSize(): Error: " + (err.reason || JSON.stringify(err)) + " in _db.getInfo()");
							consoleError(nerr.message);
							getCacheSizeCallback(nerr);
						}
						else {
							$( "#progressbar" ).progressbar({
								value: false
							});
							
							mapArrays.pouchDB.allDocs({
									include_docs: true,
									attachments: true
								}, 
								function getAllDocs(err, result) {
									if (err) {
										consoleError("getCacheSize(): Error: " + err.message + " in _db.allDocs()");
										if (getCacheSizeCallback) {
											getCacheSizeCallback(err);
										}		
									}
									else {
										mapArrays.cacheSize=0;
										mapArrays.totalTiles=result.total_rows;
										for (var j=0; j<mapArrays.basemapArray.length; j++) {
											mapArrays.basemapArray[j].tileLayer.cacheStats.tiles=0;
											mapArrays.basemapArray[j].tileLayer.cacheStats.size=0;
										}
										for (var j=0; j<mapArrays.overlaymapArray.length; j++) {
											mapArrays.overlaymapArray[j].tileLayer.cacheStats.tiles=0;
											mapArrays.overlaymapArray[j].tileLayer.cacheStats.size=0;
										}
										
										for (var i=0; i<result.total_rows; i++) {
											mapArrays.cacheSize+=(result.rows[i].doc.urlLength || result.rows[i].doc.dataUrl.length);
											result.rows[i].nameFound=false;											
											if (mapArrays.basemapArray) {
												for (var j=0; j<mapArrays.basemapArray.length; j++) {
													if (mapArrays.basemapArray[j].name == result.rows[i].doc.name) {
														result.rows[i].nameFound=true;
														mapArrays.basemapArray[j].tileLayer.cacheStats.tiles++;
														mapArrays.basemapArray[j].tileLayer.cacheStats.size+=
															(result.rows[i].doc.urlLength || result.rows[i].doc.dataUrl.length);
														break; // Out of for loop
													}
												}
											}																			
											if (mapArrays.overlaymapArray) {
												for (var j=0; j<mapArrays.overlaymapArray.length; j++) {
													if (mapArrays.overlaymapArray[j].name == result.rows[i].doc.name) {
														result.rows[i].nameFound=true;
														mapArrays.overlaymapArray[j].tileLayer.cacheStats.tiles++;
														mapArrays.overlaymapArray[j].tileLayer.cacheStats.size+=
															(result.rows[i].doc.urlLength || result.rows[i].doc.dataUrl.length);
														break; // Out of for loop
													}
												}
											}								
										}
										
										$( "#progressbar" ).progressbar({
											value: result.total_rows
										});
										
										var cacheRows="";
										var tableHtml="";					
										if (mapArrays.basemapArray) {
											for (var i=0; i<mapArrays.basemapArray.length; i++) {
												if (mapArrays.basemapArray[i].tileLayer && mapArrays.basemapArray[i].tileLayer.cacheStats &&
													mapArrays.basemapArray[i].tileLayer.cacheStats.hits > 0 ||
													mapArrays.basemapArray[i].tileLayer.cacheStats.misses > 0 ||
													mapArrays.basemapArray[i].tileLayer.cacheStats.errors > 0||
													mapArrays.basemapArray[i].tileLayer.cacheStats.tiles > 0) {
													cacheRows+="\n[" + i + "] " + mapArrays.basemapArray[i].tileLayer.name + 
														": hits: " + mapArrays.basemapArray[i].tileLayer.cacheStats.hits +
														"; misses: " + mapArrays.basemapArray[i].tileLayer.cacheStats.misses +
														"; errors: " + mapArrays.basemapArray[i].tileLayer.cacheStats.errors +
														"; tiles: " + mapArrays.basemapArray[i].tileLayer.cacheStats.tiles;
													tableHtml+='  <tr>\n' +
														'    <td>' + mapArrays.basemapArray[i].tileLayer.name + '</td>\n' +
														'    <td>' + mapArrays.basemapArray[i].tileLayer.cacheStats.hits + '</td>\n' +
														'    <td>' + mapArrays.basemapArray[i].tileLayer.cacheStats.misses + '</td>\n' +
														'    <td>' + mapArrays.basemapArray[i].tileLayer.cacheStats.errors +  '</td>\n' +
														'    <td>' + mapArrays.basemapArray[i].tileLayer.cacheStats.tiles + '</td>\n' +
														'    <td>' + (fileSize(mapArrays.basemapArray[i].tileLayer.cacheStats.size)||'N/A') + '</td>\n' +
														'  </tr>';	
												}
											} 
										}
										else {
											consoleError("getCacheSize() no basemapArray");
										}
													
										if (mapArrays.overlaymapArray) {
											for (var i=0; i<mapArrays.overlaymapArray.length; i++) {
												if (mapArrays.overlaymapArray[i].tileLayer && mapArrays.overlaymapArray[i].tileLayer.cacheStats &&
													mapArrays.overlaymapArray[i].tileLayer.cacheStats.hits > 0 ||
													mapArrays.overlaymapArray[i].tileLayer.cacheStats.misses > 0 ||
													mapArrays.overlaymapArray[i].tileLayer.cacheStats.errors > 0) {
													cacheRows+="\n[" + i + "] " + mapArrays.overlaymapArray[i].tileLayer.name + 
														": hits: " + mapArrays.overlaymapArray[i].tileLayer.cacheStats.hits +
														"; misses: " + mapArrays.overlaymapArray[i].tileLayer.cacheStats.misses +
														"; errors: " + mapArrays.overlaymapArray[i].tileLayer.cacheStats.errors;
													tableHtml+='  <tr>\n' +
														'    <td>' + mapArrays.overlaymapArray[i].tileLayer.name + '</td>\n' +
														'    <td>' + mapArrays.overlaymapArray[i].tileLayer.cacheStats.hits + '</td>\n' +
														'    <td>' + mapArrays.overlaymapArray[i].tileLayer.cacheStats.misses + '</td>\n' +
														'    <td>' + mapArrays.overlaymapArray[i].tileLayer.cacheStats.errors +  '</td>\n' +
														'    <td>' + mapArrays.overlaymapArray[i].tileLayer.cacheStats.tiles + '</td>\n' +
														'    <td>' + (fileSize(mapArrays.overlaymapArray[i].tileLayer.cacheStats.size)||'N/A') + '</td>\n' +
														'  </tr>';	
												}
											} 
										}
										else {
											consoleError("getCacheSize() no overlaymapArray");
										}

//
// Process cache stats not used by basename (i.e topoJSON geolevels)
//
										var nonBasemapCacheStats = {};
										for (var i=0; i<result.total_rows; i++) {
											if (result.rows[i].nameFound == false) { // Not yet processed
												var name=result.rows[i].doc.name;
												if (nonBasemapCacheStats[name]) {
													nonBasemapCacheStats[name].tiles++;
													nonBasemapCacheStats[name].size+=
														(result.rows[i].doc.urlLength || result.rows[i].doc.dataUrl.length);
												}
												else {
													nonBasemapCacheStats[name] = {
														found: false,
														tiles: 1,
														size: (result.rows[i].doc.urlLength || result.rows[i].doc.dataUrl.length)										
													}
												}
											}
										}
										
										consoleLog("nonBasemapCacheStats() size: " + Object.keys(nonBasemapCacheStats).length);
										consoleLog("getCacheSize(): " + mapArrays.totalTiles + " tiles; size: " + mapArrays.cacheSize + " bytes" + cacheRows);
										if (getCacheSizeCallback) {
											$( "#progressbar" ).progressbar({
												value: result.total_rows
											});	
											mapArrays.viewCleanup(function() {
												getCacheSizeCallback(undefined /* No error */, 
													{
														tableHtml: 		tableHtml,
														nonBasemapCacheStats: nonBasemapCacheStats,
														totalTiles: 	mapArrays.totalTiles,
														cacheSize:		mapArrays.cacheSize,
														autoCompaction:	baseLayer.options.auto_compaction
													} // Results
												);
											}); 
										}
									}
								}
							);
						}
					}
				);
			}
		} // End of getCacheSize
		
}; // End of mapArrays() object	
		
// Eof