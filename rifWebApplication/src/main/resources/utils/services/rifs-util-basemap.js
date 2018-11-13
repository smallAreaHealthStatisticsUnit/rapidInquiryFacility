/**
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * Copyright 2016 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
 *
 * This file is part of the Rapid Inquiry Facility (RIF) project.
 * RIF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RIF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
 * to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
 * Boston, MA 02110-1301 USA
 
 * David Morley
 * @author dmorley
 */

/*
 * SERVICE to supply pre-defined basemaps tiles to leaflet
 */

/* global L */
angular.module("RIF")
        .factory('LeafletBaseMapService', ['user', 'AlertService', 'CommonMappingStateService',
                function (user, AlertService, CommonMappingStateService) {

				    var defaultBaseMap = 'OpenStreetMap Mapnik';
					
                    var thunderforestAPIkey = "f01dbbea1da44b649ee7f0ab6be56756";

                    //for disease mapping
                    var baseMapInUse = {
                        areamap: defaultBaseMap,
                        viewermap: defaultBaseMap,
                        diseasemap1: defaultBaseMap,
                        diseasemap2: defaultBaseMap,
                        exportmap: defaultBaseMap
                    };
                    //from checkbox, disable basemap
                    var noBaseMap = {
                        areamap: false,
                        viewermap: false,
                        diseasemap1: false,
                        diseasemap2: false,
                        exportmap: false
                    };

                    //https://leaflet-extras.github.io/leaflet-providers/preview/
                    var basemaps = [];
                    basemaps.push({name: "OpenStreetMap Mapnik", tile: L.tileLayer(window.location.protocol + '//{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                            attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
                        })});
                    basemaps.push({name: "OpenStreetMap BlackAndWhite", tile: L.tileLayer(window.location.protocol + '//{s}.tiles.wmflabs.org/bw-mapnik/{z}/{x}/{y}.png', {
                            attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
                        })});
                    basemaps.push({name: "OpenTopoMap", tile: L.tileLayer(window.location.protocol + '//{s}.tile.opentopomap.org/{z}/{x}/{y}.png', {
                            attribution: 'Map data: &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>, <a href="http://viewfinderpanoramas.org" target="_blank">SRTM</a> | Map style: &copy; <a href="https://opentopomap.org" target="_blank">OpenTopoMap</a> (<a href="https://creativecommons.org/licenses/by-sa/3.0/" target="_blank">CC-BY-SA</a>)'
                        })});
                    basemaps.push({name: "Humanitarian OpenStreetMap", tile: L.tileLayer(window.location.protocol + '//{s}.tile.openstreetmap.fr/hot/{z}/{x}/{y}.png', {
                            attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>, Tiles courtesy of <a href="http://hot.openstreetmap.org/" target="_blank">Humanitarian OpenStreetMap Team</a>'
                        })});
                    basemaps.push({name: "Thunderforest OpenCycleMap", tile: L.tileLayer(window.location.protocol + '//{s}.tile.thunderforest.com/cycle/{z}/{x}/{y}.png?apikey=' + thunderforestAPIkey, {
                            attribution: '&copy; <a href="http://www.thunderforest.com/" target="_blank">Thunderforest</a>, &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
                        })});
                    basemaps.push({name: "Thunderforest Transport", tile: L.tileLayer(window.location.protocol + '//{s}.tile.thunderforest.com/transport/{z}/{x}/{y}.png?apikey=' + thunderforestAPIkey, {
                            attribution: '&copy; <a href="http://www.thunderforest.com/" target="_blank">Thunderforest</a>, &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
                        })});
                    basemaps.push({name: "Thunderforest TransportDark", tile: L.tileLayer(window.location.protocol + '//{s}.tile.thunderforest.com/transport-dark/{z}/{x}/{y}.png?apikey=' + thunderforestAPIkey, {
                            attribution: '&copy; <a href="http://www.thunderforest.com/" target="_blank">Thunderforest</a>, &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
                        })});
                    basemaps.push({name: "Thunderforest Landscape", tile: L.tileLayer(window.location.protocol + '//{s}.tile.thunderforest.com/landscape/{z}/{x}/{y}.png?apikey=' + thunderforestAPIkey, {
                            attribution: '&copy; <a href="http://www.thunderforest.com/" target="_blank">Thunderforest</a>, &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
                        })});
                    basemaps.push({name: "Thunderforest SpinalMap", tile: L.tileLayer(window.location.protocol + '//{s}.tile.thunderforest.com/spinal-map/{z}/{x}/{y}.png?apikey=' + thunderforestAPIkey, {
                            attribution: '&copy; <a href="http://www.thunderforest.com/" target="_blank">Thunderforest</a>, &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
                        })});
                    basemaps.push({name: "Thunderforest Outdoors", tile: L.tileLayer(window.location.protocol + '//{s}.tile.thunderforest.com/outdoors/{z}/{x}/{y}.png?apikey=' + thunderforestAPIkey, {
                            attribution: '&copy; <a href="http://www.thunderforest.com/" target="_blank">Thunderforest</a>, &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
                        })});
                    basemaps.push({name: "Thunderforest Pioneer", tile: L.tileLayer(window.location.protocol + '//{s}.tile.thunderforest.com/pioneer/{z}/{x}/{y}.png?apikey=' + thunderforestAPIkey, {
                            attribution: '&copy; <a href="http://www.thunderforest.com/" target="_blank">Thunderforest</a>, &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
                        })});
                    basemaps.push({name: "Thunderforest Mobile Atlas", tile: L.tileLayer('https://{s}.tile.thunderforest.com/mobile-atlas/{z}/{x}/{y}.png?apikey=' + thunderforestAPIkey, {
                            attribution: '&copy; <a href="http://www.thunderforest.com/" target="_blank">Thunderforest</a>, &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
                        })});
                    basemaps.push({name: "Thunderforest Neighbourhood", tile: L.tileLayer('https://{s}.tile.thunderforest.com/neighbourhood/{z}/{x}/{y}.png?apikey=' + thunderforestAPIkey, {
                            attribution: '&copy; <a href="http://www.thunderforest.com/" target="_blank">Thunderforest</a>, &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
                        })});
                    basemaps.push({name: "OpenMapSurfer Roads", tile: L.tileLayer(window.location.protocol + '//korona.geog.uni-heidelberg.de/tiles/roads/x={x}&y={y}&z={z}', {
                            attribution: 'Imagery from <a href="http://giscience.uni-hd.de/" target="_blank">GIScience Research Group @ University of Heidelberg</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
                        })});
                    basemaps.push({name: "OpenMapSurfer Grayscale", tile: L.tileLayer(window.location.protocol + '//korona.geog.uni-heidelberg.de/tiles/roadsg/x={x}&y={y}&z={z}', {
                            attribution: 'Imagery from <a href="http://giscience.uni-hd.de/" target="_blank">GIScience Research Group @ University of Heidelberg</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
                        })});
                    basemaps.push({name: "Hydda Full", tile: L.tileLayer(window.location.protocol + '//{s}.tile.openstreetmap.se/hydda/full/{z}/{x}/{y}.png', {
                            attribution: 'Tiles courtesy of <a href="http://openstreetmap.se/" target="_blank">OpenStreetMap Sweden</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
                        })});
                    basemaps.push({name: "Hydda Base", tile: L.tileLayer(window.location.protocol + '//{s}.tile.openstreetmap.se/hydda/base/{z}/{x}/{y}.png', {
                            attribution: 'Tiles courtesy of <a href="http://openstreetmap.se/" target="_blank">OpenStreetMap Sweden</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
                        })});
                    basemaps.push({name: "Stamen Toner", tile: L.tileLayer(window.location.protocol + '//stamen-tiles-{s}.a.ssl.fastly.net/toner/{z}/{x}/{y}.{ext}', {
                            attribution: 'Map tiles by <a href="http://stamen.com" target="_blank">Stamen Design</a>, <a href="http://creativecommons.org/licenses/by/3.0" target="_blank">CC BY 3.0</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
                            subdomains: 'abcd',
                            ext: 'png'
                        })});
                    basemaps.push({name: "Stamen TonerBackground", tile: L.tileLayer(window.location.protocol + '//stamen-tiles-{s}.a.ssl.fastly.net/toner-background/{z}/{x}/{y}.{ext}', {
                            attribution: 'Map tiles by <a href="http://stamen.com" target="_blank">Stamen Design</a>, <a href="http://creativecommons.org/licenses/by/3.0" target="_blank">CC BY 3.0</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
                            subdomains: 'abcd',
                            ext: 'png'
                        })});
                    basemaps.push({name: "Stamen TonerLite", tile: L.tileLayer(window.location.protocol + '//stamen-tiles-{s}.a.ssl.fastly.net/toner-lite/{z}/{x}/{y}.{ext}', {
                            attribution: 'Map tiles by <a href="http://stamen.com" target="_blank">Stamen Design</a>, <a href="http://creativecommons.org/licenses/by/3.0" target="_blank">CC BY 3.0</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
                            subdomains: 'abcd',
                            ext: 'png'
                        })});
                    basemaps.push({name: "Stamen Watercolor", tile: L.tileLayer(window.location.protocol + '//stamen-tiles-{s}.a.ssl.fastly.net/watercolor/{z}/{x}/{y}.{ext}', {
                            attribution: 'Map tiles by <a href="http://stamen.com" target="_blank">Stamen Design</a>, <a href="http://creativecommons.org/licenses/by/3.0" target="_blank">CC BY 3.0</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
                            subdomains: 'abcd',
                            ext: 'png'
                        })});
                    basemaps.push({name: "Esri WorldStreetMap", tile: L.tileLayer(window.location.protocol + '//server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/{z}/{y}/{x}', {
                            attribution: 'Tiles &copy; Esri &mdash; Source: Esri, DeLorme, NAVTEQ, USGS, Intermap, iPC, NRCAN, Esri Japan, METI, Esri China (Hong Kong), Esri (Thailand), TomTom, 2012'
                        })});
                    basemaps.push({name: "Esri DeLorme", tile: L.tileLayer(window.location.protocol + '//server.arcgisonline.com/ArcGIS/rest/services/Specialty/DeLorme_World_Base_Map/MapServer/tile/{z}/{y}/{x}', {
                            attribution: 'Tiles &copy; Esri &mdash; Copyright: &copy;2012 DeLorme'
                        })});
                    basemaps.push({name: "Esri WorldTopoMap", tile: L.tileLayer(window.location.protocol + '//server.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer/tile/{z}/{y}/{x}', {
                            attribution: 'Tiles &copy; Esri &mdash; Esri, DeLorme, NAVTEQ, TomTom, Intermap, iPC, USGS, FAO, NPS, NRCAN, GeoBase, Kadaster NL, Ordnance Survey, Esri Japan, METI, Esri China (Hong Kong), and the GIS User Community'
                        })});
                    basemaps.push({name: "Esri WorldImagery", tile: L.tileLayer(window.location.protocol + '//server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
                            attribution: 'Tiles &copy; Esri &mdash; Source: Esri, i-cubed, USDA, USGS, AEX, GeoEye, Getmapping, Aerogrid, IGN, IGP, UPR-EGP, and the GIS User Community'
                        })});
                    basemaps.push({name: "Esri WorldTerrain", tile: L.tileLayer(window.location.protocol + '//server.arcgisonline.com/ArcGIS/rest/services/World_Terrain_Base/MapServer/tile/{z}/{y}/{x}', {
                            attribution: 'Tiles &copy; Esri &mdash; Source: USGS, Esri, TANA, DeLorme, and NPS'
                        })});
                    basemaps.push({name: "Esri WorldShadedRelief", tile: L.tileLayer(window.location.protocol + '//server.arcgisonline.com/ArcGIS/rest/services/World_Shaded_Relief/MapServer/tile/{z}/{y}/{x}', {
                            attribution: 'Tiles &copy; Esri &mdash; Source: Esri'
                        })});
                    basemaps.push({name: "Esri WorldPhysical", tile: L.tileLayer(window.location.protocol + '//server.arcgisonline.com/ArcGIS/rest/services/World_Physical_Map/MapServer/tile/{z}/{y}/{x}', {
                            attribution: 'Tiles &copy; Esri &mdash; Source: US National Park Service'
                        })});
                    basemaps.push({name: "Esri OceanBasemap", tile: L.tileLayer(window.location.protocol + '//server.arcgisonline.com/ArcGIS/rest/services/Ocean_Basemap/MapServer/tile/{z}/{y}/{x}', {
                            attribution: 'Tiles &copy; Esri &mdash; Sources: GEBCO, NOAA, CHS, OSU, UNH, CSUMB, National Geographic, DeLorme, NAVTEQ, and Esri'
                        })});
                    basemaps.push({name: "Esri NatGeoWorldMap", tile: L.tileLayer(window.location.protocol + '//server.arcgisonline.com/ArcGIS/rest/services/NatGeo_World_Map/MapServer/tile/{z}/{y}/{x}', {
                            attribution: 'Tiles &copy; Esri &mdash; National Geographic, Esri, DeLorme, NAVTEQ, UNEP-WCMC, USGS, NASA, ESA, METI, NRCAN, GEBCO, NOAA, iPC'
                        })});
                    basemaps.push({name: "Esri WorldGrayCanvas", tile: L.tileLayer(window.location.protocol + '//server.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Light_Gray_Base/MapServer/tile/{z}/{y}/{x}', {
                            attribution: 'Tiles &copy; Esri &mdash; Esri, DeLorme, NAVTEQ'
                        })});
                    basemaps.push({name: "CartoDB Positron", tile: L.tileLayer(window.location.protocol + '//{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png', {
                            attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a> &copy; <a href="http://cartodb.com/attributions" target="_blank">CartoDB</a>',
                            subdomains: 'abcd'
                        })});
                    basemaps.push({name: "CartoDB PositronNoLabels", tile: L.tileLayer(window.location.protocol + '//{s}.basemaps.cartocdn.com/light_nolabels/{z}/{x}/{y}.png', {
                            attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a> &copy; <a href="http://cartodb.com/attributions" target="_blank">CartoDB</a>',
                            subdomains: 'abcd'
                        })});
                    basemaps.push({name: "CartoDB PositronOnlyLabels", tile: L.tileLayer(window.location.protocol + '//{s}.basemaps.cartocdn.com/light_only_labels/{z}/{x}/{y}.png', {
                            attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a> &copy; <a href="http://cartodb.com/attributions" target="_blank">CartoDB</a>',
                            subdomains: 'abcd'
                        })});
                    basemaps.push({name: "CartoDB DarkMatter", tile: L.tileLayer(window.location.protocol + '//{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}.png', {
                            attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a> &copy; <a href="http://cartodb.com/attributions" target="_blank">CartoDB</a>',
                            subdomains: 'abcd'
                        })});
                    basemaps.push({name: "CartoDB DarkMatterNoLabels", tile: L.tileLayer(window.location.protocol + '//{s}.basemaps.cartocdn.com/dark_nolabels/{z}/{x}/{y}.png', {
                            attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a> &copy; <a href="http://cartodb.com/attributions" target="_blank">CartoDB</a>',
                            subdomains: 'abcd'
                        })});
                    basemaps.push({name: "CartoDB DarkMatterOnlyLabels", tile: L.tileLayer(window.location.protocol + '//{s}.basemaps.cartocdn.com/dark_only_labels/{z}/{x}/{y}.png', {
                            attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a> &copy; <a href="http://cartodb.com/attributions" target="_blank">CartoDB</a>',
                            subdomains: 'abcd'
                        })});
                    basemaps.push({name: "HikeBike HikeBike", tile: L.tileLayer(window.location.protocol + '//{s}.tiles.wmflabs.org/hikebike/{z}/{x}/{y}.png', {
                            attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
                        })});
                    basemaps.push({name: "HikeBike HillShading", tile: L.tileLayer(window.location.protocol + '//{s}.tiles.wmflabs.org/hillshading/{z}/{x}/{y}.png', {
                            attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
                        })});
                    basemaps.push({name: "NASAGIBS ViirsEarthAtNight2012", tile: L.tileLayer(window.location.protocol + '//map1.vis.earthdata.nasa.gov/wmts-webmerc/VIIRS_CityLights_2012/default/{time}/{tilematrixset}{maxZoom}/{z}/{y}/{x}.{format}', {
                            attribution: 'Imagery provided by services from the Global Imagery Browse Services (GIBS), operated by the NASA/GSFC/Earth Science Data and Information System (<a href="https://earthdata.nasa.gov" target="_blank">ESDIS</a>) with funding provided by NASA/HQ.',
                            bounds: [[-85.0511287776, -179.999999975], [85.0511287776, 179.999999975]],
                            minZoom: 1,
                            maxZoom: 8,
                            format: 'jpg',
                            time: '',
                            tilematrixset: 'GoogleMapsCompatible_Level'
                        })});
                    //Additional
                    basemaps.push({name: "OSM UK Postcodes", tile: L.tileLayer(window.location.protocol + '//random.dev.openstreetmap.org/postcodes/tiles/pc-npe/{z}/{x}/{y}.png', {
                            attribution: '&copy; <a href="http://random.dev.openstreetmap.org/postcodes/" target="_blank">OSM Postcode</a>'
                        })});
                    basemaps.push({name: "Code-Point Open UK Postcodes", tile: L.tileLayer(window.location.protocol + '//random.dev.openstreetmap.org/postcodes/tiles/pc-os/{z}/{x}/{y}.png', {
                            attribution: '&copy; <a href="http://random.dev.openstreetmap.org/postcodes/" target="_blank">Code-Point Open layers</a>'
                        })});
						
						
					function setCurrentBaseMapInUse2(map, layer) {
						if (layer && baseMapInUse[map] != layer) {
							AlertService.consoleDebug("[rifs-util-basemap.js] map: " + map + "; set new current baseMapInUse from: " +
								baseMapInUse[map] + "; to: " + layer);
							baseMapInUse[map] = layer;
						}
					}
						
                    return {
                        //Get a list of all basemaps available to fill modal selects
                        getBaseMapList: function () {
                            var layerList = [];
                            for (var i = 0; i < basemaps.length; i++) {
                                layerList.push(basemaps[i].name);
                            }
                            return layerList;
                        },
                        //Sets the selected basemap by updating "tile" in leaflet directive
                        setBaseMap: function (layer) {
							var found=false;
                            for (var i = 0; i < basemaps.length; i++) {
                                if (basemaps[i].name === layer) {
									found=true;
									
									AlertService.consoleDebug("[rifs-util-basemap.js] set baseMap: " + layer);
                                    //Need to return a copy of the map as directive cannot share instance
                                    var tile=angular.copy(basemaps[i].tile);
									tile.name = layer;
									
									return tile;
                                }
                            }
							if (!found) {
								AlertService.rifMessage("warning", "Unable to set basemap; map: " + layer + " is invalid");
								return undefined;
							}
                        },
                        //state on current basemap
                        setCurrentBaseMapInUse: function (map, layer) {
							setCurrentBaseMapInUse2(map, layer);
                        },
                        getCurrentBaseMapInUse: function (map) {
                            return baseMapInUse[map];
                        },
                        //state on map T/F
                        setNoBaseMap: function (map, checkBox) {
                            noBaseMap[map] = checkBox;
                        },
                        getNoBaseMap: function (map) {
                            return noBaseMap[map];
                        },
						setDefaultBaseMap(map) {
							noBaseMap[map] = false;
							
							AlertService.consoleDebug("[rifs-util-basemap.js] map: " + map + "; default baseMapInUse: " + defaultBaseMap);
							baseMapInUse[map] = defaultBaseMap;
							return defaultBaseMap;
						},
						setDefaultMapBackground: function(geography, callback, map) {
							
							if (map == undefined) {
								AlertService.rifMessage("warning", "setDefaultMapBackground() map is undefined.");
								if (callback && typeof(callback) == "function") {
									callback("setDefaultMapBackground() map is undefined.");
								}
								else {
									AlertService.rifMessage("warning", "setDefaultMapBackground() callback is undefined.");
								}
								return;
							}
							else if (baseMapInUse[map] == undefined) {
								AlertService.rifMessage("warning", "setDefaultMapBackground() map is invalid: " + map);
								if (callback && typeof(callback) == "function") {
									callback("setDefaultMapBackground() map is invalid: " + map);
								}
								else {
									AlertService.rifMessage("warning", "setDefaultMapBackground() callback is undefined.");
								}
								return;
							}
							
							// WARNING: The name is validated as constraint check map_background_ck on rif40_geographies.map_background
							user.getMapBackground(user.currentUser, geography).then(function (res) {
								var mapBackground=res.data;
								var found=false;
								if (mapBackground && mapBackground.mapBackground == "NONE") {
									found=true;
									AlertService.consoleDebug('[rifs-util-basemap.js]: mapBackground is NONE' +
										" for map: " + map +
										" and geography: " + geography);
									noBaseMap[map] = true;
									setCurrentBaseMapInUse2(map, defaultBaseMap);
									CommonMappingStateService.getState(map).setBasemap(defaultBaseMap, true /* no basemap*/);
								}
								else if (mapBackground && mapBackground.mapBackground) {
									var basemapsList = [];
									for (var i = 0; i < basemaps.length; i++) {
										basemapsList.push(basemaps[i].name);
										if (basemaps[i].name == mapBackground.mapBackground) {
											found=true;
									
											AlertService.consoleDebug('[rifs-util-basemap.js] mapBackground is: ' + mapBackground.mapBackground +
												" for map: " + map +
												" and geography: " + geography, new Error("dummy"));

											noBaseMap[map] = false;
											baseMapInUse[map] = mapBackground.mapBackground;
											setCurrentBaseMapInUse2(map, mapBackground.mapBackground);
											CommonMappingStateService.getState(map).setBasemap(mapBackground.mapBackground, false /* no basemap*/);
										}	
									}
								}
								if (!found) {
									if (mapBackground.mapBackground) {
										AlertService.rifMessage("warning", "Default map background " + mapBackground.mapBackground + 
											" not found in database " +
											" for map: " + map +
											" and geography: " + geography);
									}
									else {
										AlertService.rifMessage("warning", "No default map background found in database " +
											" for map: " + map +
											" and geography: " + geography);
									}
									AlertService.consoleDebug('[rifs-util-basemap.js]: mapBackground not found; ' + JSON.stringify(mapBackground) +
										" for map: " + map +
										" and geography: " + geography);
								}
								
								if (callback && typeof(callback) == "function") {
//									AlertService.consoleDebug("[rifs-util-basemap.js]: basemapsList: " + JSON.stringify(basemapsList));
									callback(undefined /* no error */, map);
								}
								else {
									AlertService.rifMessage("warning", "setDefaultMapBackground() callback is undefined.");
								}
                            }, function (err) {
								
                                AlertService.rifMessage("warning", "Could not get default map background from database " +
									" for map: " + map +
									" and geography: " + geography +
									"; using OpenStreetMap Mapnik", err);	

								if (callback && typeof(callback) == "function") {
									callback((err || "Could not get default map background from database " +
										" for map: " + map +
										" and geography: " + geography +
										"; using OpenStreetMap Mapnik"), 
										map);
								}	
								else {
									AlertService.rifMessage("warning", "setDefaultMapBackground() callback is undefined.");
								}
							})
						}
                    };
                }]);