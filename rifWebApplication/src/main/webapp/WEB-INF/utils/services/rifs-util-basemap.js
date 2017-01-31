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
        .factory('LeafletBaseMapService',
                function () {

                    var defaultBaseMap = 'OpenStreetMap Mapnik';

                    //for disease mapping
                    var baseMapInUse = {
                        area: defaultBaseMap,
                        viewermap: defaultBaseMap,
                        diseasemap1: defaultBaseMap,
                        diseasemap2: defaultBaseMap
                    };
                    //from checkbox, disable basemap
                    var noBaseMap = {
                        area: false,
                        viewermap: false,
                        diseasemap1: false,
                        diseasemap2: false
                    };

                    //https://leaflet-extras.github.io/leaflet-providers/preview/
                    var basemaps = [];
                    basemaps.push({name: "OpenStreetMap Mapnik", tile: L.tileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                            attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
                        })});
                    basemaps.push({name: "OpenStreetMap BlackAndWhite", tile: L.tileLayer('http://{s}.tiles.wmflabs.org/bw-mapnik/{z}/{x}/{y}.png', {
                            attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
                        })});
                    basemaps.push({name: "OpenTopoMap", tile: L.tileLayer('http://{s}.tile.opentopomap.org/{z}/{x}/{y}.png', {
                            attribution: 'Map data: &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>, <a href="http://viewfinderpanoramas.org" target="_blank">SRTM</a> | Map style: &copy; <a href="https://opentopomap.org" target="_blank">OpenTopoMap</a> (<a href="https://creativecommons.org/licenses/by-sa/3.0/" target="_blank">CC-BY-SA</a>)'
                        })});
                    basemaps.push({name: "Humanitarian OpenStreetMap", tile: L.tileLayer('http://{s}.tile.openstreetmap.fr/hot/{z}/{x}/{y}.png', {
                            attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>, Tiles courtesy of <a href="http://hot.openstreetmap.org/" target="_blank">Humanitarian OpenStreetMap Team</a>'
                        })});
                    basemaps.push({name: "Thunderforest OpenCycleMap", tile: L.tileLayer('http://{s}.tile.thunderforest.com/cycle/{z}/{x}/{y}.png', {
                            attribution: '&copy; <a href="http://www.thunderforest.com/" target="_blank">Thunderforest</a>, &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
                        })});
                    basemaps.push({name: "Thunderforest Transport", tile: L.tileLayer('http://{s}.tile.thunderforest.com/transport/{z}/{x}/{y}.png', {
                            attribution: '&copy; <a href="http://www.thunderforest.com/" target="_blank">Thunderforest</a>, &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
                        })});
                    basemaps.push({name: "Thunderforest TransportDark", tile: L.tileLayer('http://{s}.tile.thunderforest.com/transport-dark/{z}/{x}/{y}.png', {
                            attribution: '&copy; <a href="http://www.thunderforest.com/" target="_blank">Thunderforest</a>, &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
                        })});
                    basemaps.push({name: "Thunderforest Landscape", tile: L.tileLayer('http://{s}.tile.thunderforest.com/landscape/{z}/{x}/{y}.png', {
                            attribution: '&copy; <a href="http://www.thunderforest.com/" target="_blank">Thunderforest</a>, &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
                        })});
                    basemaps.push({name: "Thunderforest SpinalMap", tile: L.tileLayer('http://{s}.tile.thunderforest.com/spinal-map/{z}/{x}/{y}.png', {
                            attribution: '&copy; <a href="http://www.thunderforest.com/" target="_blank">Thunderforest</a>, &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
                        })});
                    basemaps.push({name: "Thunderforest Outdoors", tile: L.tileLayer('http://{s}.tile.thunderforest.com/outdoors/{z}/{x}/{y}.png', {
                            attribution: '&copy; <a href="http://www.thunderforest.com/" target="_blank">Thunderforest</a>, &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
                        })});
                    basemaps.push({name: "Thunderforest Pioneer", tile: L.tileLayer('http://{s}.tile.thunderforest.com/pioneer/{z}/{x}/{y}.png', {
                            attribution: '&copy; <a href="http://www.thunderforest.com/" target="_blank">Thunderforest</a>, &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
                        })});
                    basemaps.push({name: "OpenMapSurfer Roads", tile: L.tileLayer('http://korona.geog.uni-heidelberg.de/tiles/roads/x={x}&y={y}&z={z}', {
                            attribution: 'Imagery from <a href="http://giscience.uni-hd.de/" target="_blank">GIScience Research Group @ University of Heidelberg</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
                        })});
                    basemaps.push({name: "OpenMapSurfer Grayscale", tile: L.tileLayer('http://korona.geog.uni-heidelberg.de/tiles/roadsg/x={x}&y={y}&z={z}', {
                            attribution: 'Imagery from <a href="http://giscience.uni-hd.de/" target="_blank">GIScience Research Group @ University of Heidelberg</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
                        })});
                    basemaps.push({name: "Hydda Full", tile: L.tileLayer('http://{s}.tile.openstreetmap.se/hydda/full/{z}/{x}/{y}.png', {
                            attribution: 'Tiles courtesy of <a href="http://openstreetmap.se/" target="_blank">OpenStreetMap Sweden</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
                        })});
                    basemaps.push({name: "Hydda Base", tile: L.tileLayer('http://{s}.tile.openstreetmap.se/hydda/base/{z}/{x}/{y}.png', {
                            attribution: 'Tiles courtesy of <a href="http://openstreetmap.se/" target="_blank">OpenStreetMap Sweden</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
                        })});
                    basemaps.push({name: "Stamen Toner", tile: L.tileLayer('http://stamen-tiles-{s}.a.ssl.fastly.net/toner/{z}/{x}/{y}.{ext}', {
                            attribution: 'Map tiles by <a href="http://stamen.com" target="_blank">Stamen Design</a>, <a href="http://creativecommons.org/licenses/by/3.0" target="_blank">CC BY 3.0</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
                            subdomains: 'abcd',
                            ext: 'png'
                        })});
                    basemaps.push({name: "Stamen TonerBackground", tile: L.tileLayer('http://stamen-tiles-{s}.a.ssl.fastly.net/toner-background/{z}/{x}/{y}.{ext}', {
                            attribution: 'Map tiles by <a href="http://stamen.com" target="_blank">Stamen Design</a>, <a href="http://creativecommons.org/licenses/by/3.0" target="_blank">CC BY 3.0</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
                            subdomains: 'abcd',
                            ext: 'png'
                        })});
                    basemaps.push({name: "Stamen TonerLite", tile: L.tileLayer('http://stamen-tiles-{s}.a.ssl.fastly.net/toner-lite/{z}/{x}/{y}.{ext}', {
                            attribution: 'Map tiles by <a href="http://stamen.com" target="_blank">Stamen Design</a>, <a href="http://creativecommons.org/licenses/by/3.0" target="_blank">CC BY 3.0</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
                            subdomains: 'abcd',
                            ext: 'png'
                        })});
                    basemaps.push({name: "Stamen Watercolor", tile: L.tileLayer('http://stamen-tiles-{s}.a.ssl.fastly.net/watercolor/{z}/{x}/{y}.{ext}', {
                            attribution: 'Map tiles by <a href="http://stamen.com" target="_blank">Stamen Design</a>, <a href="http://creativecommons.org/licenses/by/3.0" target="_blank">CC BY 3.0</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
                            subdomains: 'abcd',
                            ext: 'png'
                        })});
                    basemaps.push({name: "Esri WorldStreetMap", tile: L.tileLayer('http://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/{z}/{y}/{x}', {
                            attribution: 'Tiles &copy; Esri &mdash; Source: Esri, DeLorme, NAVTEQ, USGS, Intermap, iPC, NRCAN, Esri Japan, METI, Esri China (Hong Kong), Esri (Thailand), TomTom, 2012'
                        })});
                    basemaps.push({name: "Esri DeLorme", tile: L.tileLayer('http://server.arcgisonline.com/ArcGIS/rest/services/Specialty/DeLorme_World_Base_Map/MapServer/tile/{z}/{y}/{x}', {
                            attribution: 'Tiles &copy; Esri &mdash; Copyright: &copy;2012 DeLorme'
                        })});
                    basemaps.push({name: "Esri WorldTopoMap", tile: L.tileLayer('http://server.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer/tile/{z}/{y}/{x}', {
                            attribution: 'Tiles &copy; Esri &mdash; Esri, DeLorme, NAVTEQ, TomTom, Intermap, iPC, USGS, FAO, NPS, NRCAN, GeoBase, Kadaster NL, Ordnance Survey, Esri Japan, METI, Esri China (Hong Kong), and the GIS User Community'
                        })});
                    basemaps.push({name: "Esri WorldImagery", tile: L.tileLayer('http://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
                            attribution: 'Tiles &copy; Esri &mdash; Source: Esri, i-cubed, USDA, USGS, AEX, GeoEye, Getmapping, Aerogrid, IGN, IGP, UPR-EGP, and the GIS User Community'
                        })});
                    basemaps.push({name: "Esri WorldTerrain", tile: L.tileLayer('http://server.arcgisonline.com/ArcGIS/rest/services/World_Terrain_Base/MapServer/tile/{z}/{y}/{x}', {
                            attribution: 'Tiles &copy; Esri &mdash; Source: USGS, Esri, TANA, DeLorme, and NPS'
                        })});
                    basemaps.push({name: "Esri WorldShadedRelief", tile: L.tileLayer('http://server.arcgisonline.com/ArcGIS/rest/services/World_Shaded_Relief/MapServer/tile/{z}/{y}/{x}', {
                            attribution: 'Tiles &copy; Esri &mdash; Source: Esri'
                        })});
                    basemaps.push({name: "Esri WorldPhysical ", tile: L.tileLayer('http://server.arcgisonline.com/ArcGIS/rest/services/World_Physical_Map/MapServer/tile/{z}/{y}/{x}', {
                            attribution: 'Tiles &copy; Esri &mdash; Source: US National Park Service'
                        })});
                    basemaps.push({name: "Esri OceanBasemap", tile: L.tileLayer('http://server.arcgisonline.com/ArcGIS/rest/services/Ocean_Basemap/MapServer/tile/{z}/{y}/{x}', {
                            attribution: 'Tiles &copy; Esri &mdash; Sources: GEBCO, NOAA, CHS, OSU, UNH, CSUMB, National Geographic, DeLorme, NAVTEQ, and Esri'
                        })});
                    basemaps.push({name: "Esri NatGeoWorldMap", tile: L.tileLayer('http://server.arcgisonline.com/ArcGIS/rest/services/NatGeo_World_Map/MapServer/tile/{z}/{y}/{x}', {
                            attribution: 'Tiles &copy; Esri &mdash; National Geographic, Esri, DeLorme, NAVTEQ, UNEP-WCMC, USGS, NASA, ESA, METI, NRCAN, GEBCO, NOAA, iPC'
                        })});
                    basemaps.push({name: "Esri WorldGrayCanvas", tile: L.tileLayer('http://server.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Light_Gray_Base/MapServer/tile/{z}/{y}/{x}', {
                            attribution: 'Tiles &copy; Esri &mdash; Esri, DeLorme, NAVTEQ'
                        })});
                    basemaps.push({name: "CartoDB Positron", tile: L.tileLayer('http://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png', {
                            attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a> &copy; <a href="http://cartodb.com/attributions" target="_blank">CartoDB</a>',
                            subdomains: 'abcd'
                        })});
                    basemaps.push({name: "CartoDB PositronNoLabels", tile: L.tileLayer('http://{s}.basemaps.cartocdn.com/light_nolabels/{z}/{x}/{y}.png', {
                            attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a> &copy; <a href="http://cartodb.com/attributions" target="_blank">CartoDB</a>',
                            subdomains: 'abcd'
                        })});
                    basemaps.push({name: "CartoDB PositronOnlyLabels", tile: L.tileLayer('http://{s}.basemaps.cartocdn.com/light_only_labels/{z}/{x}/{y}.png', {
                            attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a> &copy; <a href="http://cartodb.com/attributions" target="_blank">CartoDB</a>',
                            subdomains: 'abcd'
                        })});
                    basemaps.push({name: "CartoDB DarkMatter", tile: L.tileLayer('http://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}.png', {
                            attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a> &copy; <a href="http://cartodb.com/attributions" target="_blank">CartoDB</a>',
                            subdomains: 'abcd'
                        })});
                    basemaps.push({name: "CartoDB DarkMatterNoLabels", tile: L.tileLayer('http://{s}.basemaps.cartocdn.com/dark_nolabels/{z}/{x}/{y}.png', {
                            attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a> &copy; <a href="http://cartodb.com/attributions" target="_blank">CartoDB</a>',
                            subdomains: 'abcd'
                        })});
                    basemaps.push({name: "CartoDB DarkMatterOnlyLabels", tile: L.tileLayer('http://{s}.basemaps.cartocdn.com/dark_only_labels/{z}/{x}/{y}.png', {
                            attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a> &copy; <a href="http://cartodb.com/attributions" target="_blank">CartoDB</a>',
                            subdomains: 'abcd'
                        })});
                    basemaps.push({name: "HikeBike HikeBike", tile: L.tileLayer('http://{s}.tiles.wmflabs.org/hikebike/{z}/{x}/{y}.png', {
                            attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
                        })});
                    basemaps.push({name: "HikeBike HillShading", tile: L.tileLayer('http://{s}.tiles.wmflabs.org/hillshading/{z}/{x}/{y}.png', {
                            attribution: '&copy; <a href="http://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
                        })});
                    basemaps.push({name: "NASAGIBS ViirsEarthAtNight2012", tile: L.tileLayer('http://map1.vis.earthdata.nasa.gov/wmts-webmerc/VIIRS_CityLights_2012/default/{time}/{tilematrixset}{maxZoom}/{z}/{y}/{x}.{format}', {
                            attribution: 'Imagery provided by services from the Global Imagery Browse Services (GIBS), operated by the NASA/GSFC/Earth Science Data and Information System (<a href="https://earthdata.nasa.gov" target="_blank">ESDIS</a>) with funding provided by NASA/HQ.',
                            bounds: [[-85.0511287776, -179.999999975], [85.0511287776, 179.999999975]],
                            minZoom: 1,
                            maxZoom: 8,
                            format: 'jpg',
                            time: '',
                            tilematrixset: 'GoogleMapsCompatible_Level'
                        })});
                    //Additional
                    basemaps.push({name: "OSM UK Postcodes", tile: L.tileLayer('http://random.dev.openstreetmap.org/postcodes/tiles/pc-npe/{z}/{x}/{y}.png', {
                            attribution: '&copy; <a href="http://random.dev.openstreetmap.org/postcodes/" target="_blank">OSM Postcode</a>'
                        })});
                    basemaps.push({name: "Code-Point Open UK Postcodes", tile: L.tileLayer('http://random.dev.openstreetmap.org/postcodes/tiles/pc-os/{z}/{x}/{y}.png', {
                            attribution: '&copy; <a href="http://random.dev.openstreetmap.org/postcodes/" target="_blank">Code-Point Open layers</a>'
                        })});
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
                            for (var i = 0; i < basemaps.length; i++) {
                                if (basemaps[i].name === layer) {
                                    //Need to return a copy of the map as directive cannot share instance
                                    return angular.copy(basemaps[i].tile);
                                }
                            }
                        },
                        //state on current basemap
                        setCurrentBaseMapInUse: function (map, layer) {
                            baseMapInUse[map] = layer;
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
                        }
                    };
                });