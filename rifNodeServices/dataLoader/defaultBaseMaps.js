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
// Rapid Enquiry Facility (RIF) - Default basemap definitions
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

var defaultBaseMaps = [];
defaultBaseMaps.push({
	name: "OpenStreetMap Mapnik", 
	tileLayerType: 	"tileLayer",
	tileLayerURL: 	'http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
	tileLayerOptions: {
		attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',		
		maxZoom: 18,
		useCache: true,
		crossOrigin: true,
		auto_compaction: false
	}});
					
defaultBaseMaps.push({
	name: "OpenStreetMap BlackAndWhite", 
	tileLayerType: 	"tileLayer",
	tileLayerURL: 	'http://{s}.tiles.wmflabs.org/bw-mapnik/{z}/{x}/{y}.png',
	tileLayerOptions: {
		attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
		maxZoom: 16,
		useCache: false, // Not CORS (Cross-Origin Resource Sharing) compliant
		crossOrigin: false
	}}); 

defaultBaseMaps.push({
	name: "OpenTopoMap", 
	tileLayerType: 	"tileLayer",
	tileLayerURL: 	'http://{s}.tile.opentopomap.org/{z}/{x}/{y}.png',
	tileLayerOptions: { 
		attribution: 'Map data: &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>, <a href="http://viewfinderpanoramas.org" target="_blank">SRTM</a> | Map style: &copy; <a href="https://opentopomap.org" target="_blank">OpenTopoMap</a> (<a href="https://creativecommons.org/licenses/by-sa/3.0/" target="_blank">CC-BY-SA</a>)',
		maxZoom: 17,
		useCache: false, // Not CORS (Cross-Origin Resource Sharing) compliant
		crossOrigin: false
	}});
defaultBaseMaps.push({
	 name: "Humanitarian OpenStreetMap", 
	tileLayerType: 	"tileLayer",
	tileLayerURL: 	'http://{s}.tile.openstreetmap.fr/hot/{z}/{x}/{y}.png',
	tileLayerOptions: {
			attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>, Tiles courtesy of <a href="http://hot.openstreetmap.org/" target="_blank">Humanitarian OpenStreetMap Team</a>',
		maxZoom: 17,
		useCache: true,
		crossOrigin: true
	}});
				
defaultBaseMaps.push({
	name: "Thunderforest OpenCycleMap", 
	tileLayerType: 	"tileLayer",
	tileLayerURL: 	'http://{s}.tile.thunderforest.com/cycle/{z}/{x}/{y}.png', // API key required
	tileLayerOptions: { // API key required
		attribution: '&copy; <a href="http://www.thunderforest.com/" target="_blank">Thunderforest</a>, &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
		maxZoom: 17,
		useCache: true,
		crossOrigin: true
	}});
defaultBaseMaps.push({
	name: "Thunderforest Railways", 
	tileLayerType: 	"tileLayer",
	tileLayerURL: 	'http://{s}.tile.thunderforest.com/transport/{z}/{x}/{y}.png', // API key required
	tileLayerOptions: { // API key required
		attribution: '&copy; <a href="http://www.thunderforest.com/" target="_blank">Thunderforest</a>, &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
		maxZoom: 17,
		useCache: true,
		crossOrigin: true
	}});
defaultBaseMaps.push({
	name: "Thunderforest Railways Dark", 
	tileLayerType: 	"tileLayer",
	tileLayerURL: 	'http://{s}.tile.thunderforest.com/transport-dark/{z}/{x}/{y}.png', // API key required
	tileLayerOptions: { // API key required
		attribution: '&copy; <a href="http://www.thunderforest.com/" target="_blank">Thunderforest</a>, &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
		maxZoom: 17,
		useCache: true,
		crossOrigin: true
	}});
defaultBaseMaps.push({
	name: "Thunderforest Landscape", 
	tileLayerType: 	"tileLayer",
	tileLayerURL: 	'http://{s}.tile.thunderforest.com/landscape/{z}/{x}/{y}.png', // API key required
	tileLayerOptions: { // API key required
		attribution: '&copy; <a href="http://www.thunderforest.com/" target="_blank">Thunderforest</a>, &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
		maxZoom: 17,
		useCache: true,
		crossOrigin: true
	}});
defaultBaseMaps.push({
	name: "Thunderforest SpinalMap", 
	tileLayerType: 	"tileLayer",
	tileLayerURL: 	'http://{s}.tile.thunderforest.com/spinal-map/{z}/{x}/{y}.png', // API key required
	tileLayerOptions: { // API key required
		attribution: '&copy; <a href="http://www.thunderforest.com/" target="_blank">Thunderforest</a>, &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
		maxZoom: 17,
		useCache: true,
		crossOrigin: true
	}});
defaultBaseMaps.push({
	name: "Thunderforest Outdoors", 
	tileLayerType: 	"tileLayer",
	tileLayerURL: 	'http://{s}.tile.thunderforest.com/outdoors/{z}/{x}/{y}.png', // API key required
	tileLayerOptions: { // API key required
		attribution: '&copy; <a href="http://www.thunderforest.com/" target="_blank">Thunderforest</a>, &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
		maxZoom: 17,
		useCache: true,
		crossOrigin: true
	}});
defaultBaseMaps.push({
	name: "Thunderforest Pioneer", 
	tileLayerType: 	"tileLayer",
	tileLayerURL: 	'http://{s}.tile.thunderforest.com/pioneer/{z}/{x}/{y}.png', // API key required
	tileLayerOptions: { 
		attribution: '&copy; <a href="http://www.thunderforest.com/" target="_blank">Thunderforest</a>, &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
		maxZoom: 17,
		useCache: true,
		crossOrigin: true
	}});
				
defaultBaseMaps.push({
	name: "OpenMapSurfer Roads", 
	tileLayerType: 	"tileLayer",
	tileLayerURL: 	'http://korona.geog.uni-heidelberg.de/tiles/roads/x={x}&y={y}&z={z}',
	tileLayerOptions: {
		attribution: 'Imagery from <a href="http://giscience.uni-hd.de/" target="_blank">GIScience Research Group @ University of Heidelberg</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
		maxZoom: 17,
		useCache: true,
		crossOrigin: true
	}});
defaultBaseMaps.push({
	name: "OpenMapSurfer Grayscale", 
	tileLayerType: 	"tileLayer",
	tileLayerURL: 	'http://korona.geog.uni-heidelberg.de/tiles/roadsg/x={x}&y={y}&z={z}',
	tileLayerOptions: {
		attribution: 'Imagery from <a href="http://giscience.uni-hd.de/" target="_blank">GIScience Research Group @ University of Heidelberg</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
		maxZoom: 17,
		useCache: true,
		crossOrigin: true
	}});
				
defaultBaseMaps.push({
	name: "Hydda Full", 
	tileLayerType: 	"tileLayer",
	tileLayerURL: 	'http://{s}.tile.openstreetmap.se/hydda/full/{z}/{x}/{y}.png',
	tileLayerOptions: {
		attribution: 'Tiles courtesy of <a href="http://openstreetmap.se/" target="_blank">OpenStreetMap Sweden</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
		maxZoom: 17,
		useCache: false, // Not CORS (Cross-Origin Resource Sharing) compliant
		crossOrigin: false
	}});
defaultBaseMaps.push({
	name: "Hydda Base", 
	tileLayerType: 	"tileLayer",
	tileLayerURL: 	'http://{s}.tile.openstreetmap.se/hydda/base/{z}/{x}/{y}.png',
	tileLayerOptions: {
		attribution: 'Tiles courtesy of <a href="http://openstreetmap.se/" target="_blank">OpenStreetMap Sweden</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
		maxZoom: 17,
		useCache: false, // Not CORS (Cross-Origin Resource Sharing) compliant
		crossOrigin: false
	}});
				
defaultBaseMaps.push({
	name: "Stamen Toner", 
	tileLayerType: 	"tileLayer",
	tileLayerURL: 	'http://stamen-tiles-{s}.a.ssl.fastly.net/toner/{z}/{x}/{y}.{ext}',
	tileLayerOptions: {
		attribution: 'Map tiles by <a href="http://stamen.com" target="_blank">Stamen Design</a>, <a href="http://creativecommons.org/licenses/by/3.0" target="_blank">CC BY 3.0</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
		subdomains: 'abcd',
		ext: 'png',
		maxZoom: 17,
		useCache: true,
		crossOrigin: true
	}});
defaultBaseMaps.push({
	name: "Stamen TonerBackground", 
	tileLayerType: 	"tileLayer",
	tileLayerURL: 	'http://stamen-tiles-{s}.a.ssl.fastly.net/toner-background/{z}/{x}/{y}.{ext}',
	tileLayerOptions: {
		attribution: 'Map tiles by <a href="http://stamen.com" target="_blank">Stamen Design</a>, <a href="http://creativecommons.org/licenses/by/3.0" target="_blank">CC BY 3.0</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
		subdomains: 'abcd',
		maxZoom: 17,
		ext: 'png',
		useCache: true,
		crossOrigin: true
	}});
defaultBaseMaps.push({
	name: "Stamen TonerLite", 
	tileLayerType: 	"tileLayer",
	tileLayerURL: 	'http://stamen-tiles-{s}.a.ssl.fastly.net/toner-lite/{z}/{x}/{y}.{ext}',
	tileLayerOptions: {
		attribution: 'Map tiles by <a href="http://stamen.com" target="_blank">Stamen Design</a>, <a href="http://creativecommons.org/licenses/by/3.0" target="_blank">CC BY 3.0</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
		subdomains: 'abcd',
		maxZoom: 17,
		ext: 'png',
		useCache: true,
		crossOrigin: true
	}});
defaultBaseMaps.push({
	name: "Stamen Watercolor", 
	tileLayerType: 	"tileLayer",
	tileLayerURL: 	'http://stamen-tiles-{s}.a.ssl.fastly.net/watercolor/{z}/{x}/{y}.{ext}',
	tileLayerOptions: {
		attribution: 'Map tiles by <a href="http://stamen.com" target="_blank">Stamen Design</a>, <a href="http://creativecommons.org/licenses/by/3.0" target="_blank">CC BY 3.0</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
		subdomains: 'abcd',
		ext: 'png',
		maxZoom: 17,
		useCache: true,
		crossOrigin: true
	}});		
				
defaultBaseMaps.push({
	name: "Esri WorldStreetMap", 
	tileLayerType: 	"tileLayer",
	tileLayerURL: 	'http://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/{z}/{y}/{x}',
	tileLayerOptions: {
		attribution: 'Tiles &copy; Esri &mdash; Source: Esri, DeLorme, NAVTEQ, USGS, Intermap, iPC, NRCAN, Esri Japan, METI, Esri China (Hong Kong), Esri (Thailand), TomTom, 2012',
		maxZoom: 17,
		useCache: true,
		crossOrigin: true
	}});
defaultBaseMaps.push({
	name: "Esri DeLorme",  
	tileLayerType: 	"tileLayer",
	tileLayerURL: 	'http://server.arcgisonline.com/ArcGIS/rest/services/Specialty/DeLorme_World_Base_Map/MapServer/tile/{z}/{y}/{x}',
	tileLayerOptions: {
		attribution: 'Tiles &copy; Esri &mdash; Copyright: &copy;2012 DeLorme',
		maxZoom: 14,
		useCache: true,
		crossOrigin: true
	}});
defaultBaseMaps.push({
	name: "Esri WorldTopoMap", 
	tileLayerType: 	"tileLayer",
	tileLayerURL: 	'http://server.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer/tile/{z}/{y}/{x}',
	tileLayerOptions: {
		attribution: 'Tiles &copy; Esri &mdash; Esri, DeLorme, NAVTEQ, TomTom, Intermap, iPC, USGS, FAO, NPS, NRCAN, GeoBase, Kadaster NL, Ordnance Survey, Esri Japan, METI, Esri China (Hong Kong), and the GIS User Community',
		maxZoom: 17,
		useCache: true,
		crossOrigin: true
	}});
defaultBaseMaps.push({
	name: "Esri WorldImagery", 
	tileLayerType: 	"tileLayer",
	tileLayerURL: 	'http://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}',
	tileLayerOptions: {
		attribution: 'Tiles &copy; Esri &mdash; Source: Esri, i-cubed, USDA, USGS, AEX, GeoEye, Getmapping, Aerogrid, IGN, IGP, UPR-EGP, and the GIS User Community',
		maxZoom: 17,
		useCache: true,
		crossOrigin: true
	}});
defaultBaseMaps.push({
	name: "Esri WorldTerrain", 
	tileLayerType: 	"tileLayer",
	tileLayerURL: 	'http://server.arcgisonline.com/ArcGIS/rest/services/World_Terrain_Base/MapServer/tile/{z}/{y}/{x}',
	tileLayerOptions: {
		attribution: 'Tiles &copy; Esri &mdash; Source: USGS, Esri, TANA, DeLorme, and NPS',
		maxZoom: 17,
		useCache: true,
		crossOrigin: true
	}});
defaultBaseMaps.push({
	name: "Esri WorldShadedRelief", 
	tileLayerType: 	"tileLayer",
	tileLayerURL: 	'http://server.arcgisonline.com/ArcGIS/rest/services/World_Shaded_Relief/MapServer/tile/{z}/{y}/{x}',
	tileLayerOptions: {
		attribution: 'Tiles &copy; Esri &mdash; Source: Esri',
		maxZoom: 17,
		useCache: true,
		crossOrigin: true
	}});
defaultBaseMaps.push({
	name: "Esri WorldPhysical ", 
	tileLayerType: 	"tileLayer",
	tileLayerURL: 	'http://server.arcgisonline.com/ArcGIS/rest/services/World_Physical_Map/MapServer/tile/{z}/{y}/{x}',
	tileLayerOptions: {
		attribution: 'Tiles &copy; Esri &mdash; Source: US National Park Service',
		maxZoom: 17,
		useCache: true,
		crossOrigin: true
	}});
defaultBaseMaps.push({
	name: "Esri OceanBasemap", 
	tileLayerType: 	"tileLayer",
	tileLayerURL: 	'http://server.arcgisonline.com/ArcGIS/rest/services/Ocean_Basemap/MapServer/tile/{z}/{y}/{x}',
	tileLayerOptions: {
		attribution: 'Tiles &copy; Esri &mdash; Sources: GEBCO, NOAA, CHS, OSU, UNH, CSUMB, National Geographic, DeLorme, NAVTEQ, and Esri',
		maxZoom: 17,
		useCache: true,
		crossOrigin: true
	}});
defaultBaseMaps.push({
	name: "Esri NatGeoWorldMap", 
	tileLayerType: 	"tileLayer",
	tileLayerURL: 	'http://server.arcgisonline.com/ArcGIS/rest/services/NatGeo_World_Map/MapServer/tile/{z}/{y}/{x}',
	tileLayerOptions: {
		attribution: 'Tiles &copy; Esri &mdash; National Geographic, Esri, DeLorme, NAVTEQ, UNEP-WCMC, USGS, NASA, ESA, METI, NRCAN, GEBCO, NOAA, iPC',
		maxZoom: 17,
		useCache: true,
		crossOrigin: true
	}});
defaultBaseMaps.push({
	name: "Esri WorldGrayCanvas", 
	tileLayerType: 	"tileLayer",
	tileLayerURL: 	'http://server.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Light_Gray_Base/MapServer/tile/{z}/{y}/{x}',
	tileLayerOptions: {
		attribution: 'Tiles &copy; Esri &mdash; Esri, DeLorme, NAVTEQ',
		maxZoom: 17,
		useCache: true,
		crossOrigin: true
	}});

defaultBaseMaps.push({
	name: "Google roadmap", 
	tileLayerType: 	"googleMutant",
	tileLayerOptions: {
		type: 'roadmap',
		maxZoom: 21,
		useCache: true,
		crossOrigin: true
	}});
defaultBaseMaps.push({
	name: "Google satellite", 
	tileLayerType: 	"googleMutant",
	tileLayerOptions: {
		type: 'satellite',
		maxZoom: 19,
		useCache: true,	// Does not work yet
		crossOrigin: true
	}});
defaultBaseMaps.push({
	name: "Google terrain", 
	tileLayerType: 	"googleMutant",
	tileLayerOptions: {
		type: 'terrain',
		maxZoom: 19,
		useCache: true,	// Does not work yet
		crossOrigin: true
	}});
defaultBaseMaps.push({
	name: "Google hybrid", 
	tileLayerType: 	"googleMutant",
	tileLayerOptions: {
		type:'hybrid',
		maxZoom: 19,
		useCache: true,	// Does not work yet
		crossOrigin: true
	}}); 
	
defaultBaseMaps.push({
	name: "CartoDB Positron", 
	tileLayerType: 	"tileLayer",
	tileLayerURL: 	'http://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png',
	tileLayerOptions: {
		attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a> &copy; <a href="http://cartodb.com/attributions" target="_blank">CartoDB</a>',
		subdomains: 'abcd',
		maxZoom: 19,
		useCache: true,	
		crossOrigin: true
	}});
defaultBaseMaps.push({
	name: "CartoDB PositronNoLabels", 
	tileLayerType: 	"tileLayer",
	tileLayerURL: 	'http://{s}.basemaps.cartocdn.com/light_nolabels/{z}/{x}/{y}.png',
	tileLayerOptions: {
		attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a> &copy; <a href="http://cartodb.com/attributions" target="_blank">CartoDB</a>',
		subdomains: 'abcd',
		maxZoom: 17,
		useCache: true,
		crossOrigin: true
	}});
defaultBaseMaps.push({
	name: "CartoDB PositronOnlyLabels", 
	tileLayerType: 	"tileLayer",
	tileLayerURL: 	'http://{s}.basemaps.cartocdn.com/light_only_labels/{z}/{x}/{y}.png',
	tileLayerOptions: {
		attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a> &copy; <a href="http://cartodb.com/attributions" target="_blank">CartoDB</a>',
		subdomains: 'abcd',
		maxZoom: 17,
		useCache: true,
		crossOrigin: true
	}});
defaultBaseMaps.push({
	name: "CartoDB DarkMatter", 
	tileLayerType: 	"tileLayer",
	tileLayerURL: 	'http://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}.png',
	tileLayerOptions: {
		attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a> &copy; <a href="http://cartodb.com/attributions" target="_blank">CartoDB</a>',
		subdomains: 'abcd',
		maxZoom: 17,
		useCache: true,
		crossOrigin: true
	}});
defaultBaseMaps.push({
	name: "CartoDB DarkMatterNoLabels", 
	tileLayerType: 	"tileLayer",
	tileLayerURL: 	'http://{s}.basemaps.cartocdn.com/dark_nolabels/{z}/{x}/{y}.png',
	tileLayerOptions: {
		attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a> &copy; <a href="http://cartodb.com/attributions" target="_blank">CartoDB</a>',
		subdomains: 'abcd',
		maxZoom: 17,
		useCache: true,
		crossOrigin: true
	}});
defaultBaseMaps.push({
	name: "CartoDB DarkMatterOnlyLabels", 
	tileLayerType: 	"tileLayer",
	tileLayerURL: 	'http://{s}.basemaps.cartocdn.com/dark_only_labels/{z}/{x}/{y}.png',
	tileLayerOptions: {
		attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a> &copy; <a href="http://cartodb.com/attributions" target="_blank">CartoDB</a>',
		subdomains: 'abcd',
		maxZoom: 17,
		useCache: true,
		crossOrigin: true
	}});
defaultBaseMaps.push({
	name: "HikeBike HikeBike", 
	tileLayerType: 	"tileLayer",
	tileLayerURL: 	'http://{s}.tiles.wmflabs.org/hikebike/{z}/{x}/{y}.png',
	tileLayerOptions: {
		attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
		useCache: false, // Not CORS (Cross-Origin Resource Sharing) compliant
		maxZoom: 15,
		crossOrigin: false
	}});
defaultBaseMaps.push({
	name: "HikeBike HillShading", 
	tileLayerType: 	"tileLayer",
	tileLayerURL: 	'http://{s}.tiles.wmflabs.org/hillshading/{z}/{x}/{y}.png',
	tileLayerOptions: {
		attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
		maxZoom: 15,
		useCache: false, // Not CORS (Cross-Origin Resource Sharing) compliant
		crossOrigin: false
		}});
/*
defaultBaseMaps.push({
	name: "NASAGIBS ViirsEarthAtNight2012", // DOES NOT WORK
	tileLayerType: 	"tileLayer",
	tileLayerURL: 	'http://map1.vis.earthdata.nasa.gov/wmts-webmerc/VIIRS_CityLights_2012/default/{time}/{tilematrixset}{maxZoom}/{z}/{y}/{x}.{format}',
	tileLayerOptions: {
		attribution: 'Imagery provided by services from the Global Imagery Browse Services (GIBS), operated by the NASA/GSFC/Earth Science Data and Information System (<a href="https://earthdata.nasa.gov" target="_blank">ESDIS</a>) with funding provided by NASA/HQ.',
		bounds: [[-85.0511287776, -179.999999975], [85.0511287776, 179.999999975]],
		minZoom: 1,
		maxZoom: 8,
		format: 'jpg',
		time: '',
		tilematrixset: 'GoogleMapsCompatible_Level',
		useCache: true,
		crossOrigin: true
	}});
	*/			 
var defaultOverlayMaps = [];
/*
defaultOverlayMaps.push({
	name: "OSM UK Postcodes", // Rubbish
	tileLayerType: 	"tileLayer",
	tileLayerURL: 	'http://random.dev.openstreetmap.org/postcodes/tiles/pc-npe/{z}/{x}/{y}.png',
	tileLayerOptions:  {
		attribution: '&copy; <a href="http://random.dev.openstreetmap.org/postcodes/" target="_blank">OSM Postcode</a>',
		useCache: false, // Not CORS (Cross-Origin Resource Sharing) compliant,
		maxZoom: 12,
		crossOrigin: false
	}}); */
				
defaultOverlayMaps.push({
	name: "Code-Point Open UK Postcodes", // Needs bounding box to avoid tile load errors
	tileLayerType: 	"tileLayer",
	tileLayerURL: 	'http://random.dev.openstreetmap.org/postcodes/tiles/pc-os/{z}/{x}/{y}.png',
	tileLayerOptions: {
		attribution: '&copy; <a href="http://random.dev.openstreetmap.org/postcodes/" target="_blank">Code-Point Open layers</a>',
		useCache: false, // Not CORS (Cross-Origin Resource Sharing) compliant,
		maxZoom: 12,
		crossOrigin: false
	}}); 			 
	
//
// Eof	