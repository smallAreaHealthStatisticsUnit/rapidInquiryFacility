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
 * CONTROLLER to handle map panels
 */

/* global L */
angular.module("RIF")
        .controller('leafletLayersCtrl', ['$scope', 'user', 'LeafletBaseMapService', 'ChoroService',
            'MappingStateService', 'ViewerStateService', 'MappingService',
            function ($scope, user, LeafletBaseMapService, ChoroService,
                    MappingStateService, ViewerStateService, MappingService) {

                //Reference the parent scope
                var parentScope = $scope.$parent;
                parentScope.child = $scope;

                //Reference the state service
                $scope.myService = MappingStateService;
                if (parentScope.myMaps[0] === "viewermap") {
                    $scope.myService = ViewerStateService;
                }

                //Handle UI-Layout resize events
                $scope.$on('ui.layout.loaded', function () {
                    $scope.getD3Frames();
                });
                $scope.$on('ui.layout.resize', function (e, beforeContainer, afterContainer) {
                    $scope.getD3FramesOnResize(beforeContainer, afterContainer);
                    for (var i in parentScope.myMaps) {
                        $scope.map[parentScope.myMaps[i]].invalidateSize();
                    }
                });

                //Leaflet maps
                $scope.map = ({
                    'diseasemap1': {},
                    'diseasemap2': {},
                    'viewermap': {}
                });

                //Polygons
                $scope.geoJSON = ({
                    'diseasemap1': {},
                    'diseasemap2': {},
                    'viewermap': {}
                });

                //Legends and Infoboxes
                $scope.legend = {
                    'diseasemap1': L.control({position: 'topright'}),
                    'diseasemap2': L.control({position: 'topright'}),
                    'viewermap': L.control({position: 'topright'})
                };
                $scope.infoBox = {
                    'diseasemap1': L.control({position: 'bottomright'}),
                    'diseasemap2': L.control({position: 'bottomright'}),
                    'viewermap': L.control({position: 'bottomright'})
                };
                $scope.infoBox2 = {
                    'diseasemap1': L.control({position: 'bottomright'}),
                    'diseasemap2': L.control({position: 'bottomright'}),
                    'viewermap': null
                };

                //the default basemap              
                $scope.thisLayer = {
                    "diseasemap1": LeafletBaseMapService.setBaseMap(LeafletBaseMapService.getCurrentBaseMapInUse("diseasemap1")),
                    "diseasemap2": LeafletBaseMapService.setBaseMap(LeafletBaseMapService.getCurrentBaseMapInUse("diseasemap2")),
                    "viewermap": LeafletBaseMapService.setBaseMap(LeafletBaseMapService.getCurrentBaseMapInUse("viewermap"))
                };
                //metadata of TopoJSON being mapped
                $scope.tileInfo = {
                    "diseasemap1": {'geography': null, 'level': null},
                    "diseasemap2": {'geography': null, 'level': null},
                    "viewermap": {'geography': null, 'level': null}
                };
                //attribute being mapped
                $scope.attr = {
                    "diseasemap1": "",
                    "diseasemap2": "",
                    "viewermap": ""
                };
                //renderers defined by choropleth map service             
                var thisMap = {
                    "diseasemap1": [],
                    "diseasemap2": [],
                    "viewermap": []
                };
                $scope.tableData = {
                    "diseasemap1": [],
                    "diseasemap2": [],
                    "viewermap": []
                };
                $scope.sexes = {
                    "diseasemap1": [],
                    "diseasemap2": [],
                    "viewermap": []
                };
                $scope.studyIDs = [];

                /*
                 * Tidy up on error
                 */
                function clearTheMapOnError(mapID) {
                    //on error, remove polygon, legends and D3 charts
                    if (mapID === "viewermap") {
                        //datatable
                        $scope.viewerTableOptions.data.length = 0;
                        //pyramid
                        $scope.yearPop = null;
                        $scope.yearsPop.length = 0;
                        $scope.populationData["viewermap"].length = 0;
                        //histogram                                                                   
                        $scope.histoData["viewermap"].length = 0;
                    } else {
                        $scope.thisPoly[mapID] = null;
                        if (!angular.isUndefined($scope.infoBox2[mapID].update)) {
                            $scope.infoBox2[mapID].update(null);
                        }
                        $scope.rrChartData[mapID].length = 0;
                    }

                    if ($scope.map[mapID].hasLayer($scope.geoJSON[mapID])) {
                        $scope.map[mapID].removeLayer($scope.geoJSON[mapID]);
                        if ($scope.legend[mapID]._map) {
                            $scope.map[mapID].removeControl($scope.legend[mapID]);
                        }
                    }
                }

                /*.
                 * Fill the study drop-downs
                 */
                //update study list if new study processed, but do not update maps
                $scope.$on('updateStudyDropDown', function (event, thisStudy) {
                    $scope.studyIDs.push(thisStudy);
                });

                //Get the possible studies initially
                $scope.getStudies = function () {
                    user.getCurrentStatusAllStudies(user.currentUser).then(function (res) {
                        for (var i = 0; i < res.data.smoothed_results.length; i++) {
                            if (res.data.smoothed_results[i].study_state === "R") {
                                var thisStudy = {
                                    "study_id": res.data.smoothed_results[i].study_id,
                                    "name": res.data.smoothed_results[i].study_name
                                };
                                $scope.studyIDs.push(thisStudy);
                            }
                        }
                        //sort array on ID with most recent first
                        $scope.studyIDs.sort(function (a, b) {
                            return parseFloat(a.study_id) - parseFloat(b.study_id);
                        }).reverse();
                        //Remember defaults
                        for (var j = 0; j < parentScope.myMaps.length; j++) {
                            var s = $scope.myService.getState().study[parentScope.myMaps[j]].study_id;
                            if (s !== null) {
                                for (var i = 0; i < $scope.studyIDs.length; i++) {
                                    if ($scope.studyIDs[i].study_id === s) {
                                        $scope.$parent.studyID[parentScope.myMaps[j]] = $scope.studyIDs[i];
                                    }
                                }
                            } else {
                                $scope.$parent.studyID[parentScope.myMaps[j]] = $scope.studyIDs[0];
                            }
                        }
                        //update sex drop-down
                        for (var j = 0; j < parentScope.myMaps.length; j++) {
                            $scope.updateSex(parentScope.myMaps[j]);
                        }
                    }, function (e) {
                        $scope.showError("Could not retrieve study status");
                    });
                };

                $scope.updateSex = function (mapID) {
                    if ($scope.studyID[mapID] !== null && angular.isDefined($scope.studyID[mapID])) {
                        //Store this study selection
                        $scope.myService.getState().study[mapID] = $scope.studyID[mapID];
                        //Get the sexes for this study
                        user.getSexesForStudy(user.currentUser, $scope.studyID[mapID].study_id, mapID)
                                .then(handleSexes, clearTheMapOnError(mapID));

                        function handleSexes(res) {
                            $scope.sexes[res.config.leaflet].length = 0;
                            if (!angular.isUndefined(res.data[0].names)) {
                                for (var i = 0; i < res.data[0].names.length; i++) {
                                    $scope.sexes[res.config.leaflet].push(res.data[0].names[i]);
                                }
                            }
                            //if no or invalid preselection, then set dropdown to last one in list     
                            if ($scope.sexes[res.config.leaflet].indexOf($scope.sex[res.config.leaflet]) === -1 | $scope.sex[res.config.leaflet] === null) {
                                $scope.sex[res.config.leaflet] = $scope.sexes[res.config.leaflet][$scope.sexes[res.config.leaflet].length - 1];
                            }
                            //dashboard specific
                            if (mapID === "viewermap") {
                                //update pyramid if in viewer
                                $scope.child.fillPyramidData();
                            } else {
                                //check selection link is possible
                                if ($scope.myService.getState().selectionLock) {
                                    var g1 = $scope.tileInfo["diseasemap1"];
                                    var g2 = $scope.tileInfo["diseasemap2"];
                                    if (g1.geography !== null && g2.geography !== null) {
                                        if (g1.geography !== g2.geography) {
                                            //different geographies     
                                            $scope.showWarning("Cannot link selections for different geographies: " + g1.geography + " & " + g2.geography);
                                            $scope.myService.getState().selectionLock = false;
                                            $scope.$parent.bLockSelect = false;
                                        } else {
                                            if (g1.level !== g2.level) {
                                                //different levels
                                                $scope.showWarning("Cannot link selections for different geolevels: " + g1.level + " & " + g2.level);
                                            }
                                        }
                                    }
                                }
                            }
                            $scope.updateStudy(res.config.leaflet);
                        }
                    }
                };

                /*
                 * Map rendering
                 */
                //change the basemaps 
                $scope.renderMap = function (mapID) {
                    $scope.map[mapID].removeLayer($scope.thisLayer[mapID]);
                    //add new baselayer if requested
                    if (!LeafletBaseMapService.getNoBaseMap(mapID)) {
                        $scope.thisLayer[mapID] = LeafletBaseMapService.setBaseMap(LeafletBaseMapService.getCurrentBaseMapInUse(mapID));
                        $scope.thisLayer[mapID].addTo($scope.map[mapID]);
                    }
                };

                //Draw the map
                $scope.refresh = function (mapID) {
                    //get choropleth map renderer
                    $scope.attr[mapID] = ChoroService.getMaps(mapID).feature;
                    thisMap[mapID] = ChoroService.getMaps(mapID).renderer;

                    //not a choropleth, but single colour
                    if (thisMap[mapID].range.length === 1) {
                        //remove existing legend
                        if ($scope.legend[mapID]._map) {
                            $scope.map[mapID].removeControl($scope.legend[mapID]);
                        }
                        if (angular.isDefined($scope.geoJSON[mapID]._geojsons.default)) {
                            $scope.geoJSON[mapID]._geojsons.default.eachLayer($scope.handleLayer);
                        }
                        return;
                    }

                    //remove old legend and add new
                    $scope.legend[mapID].onAdd = ChoroService.getMakeLegend(thisMap[mapID], $scope.attr[mapID]);
                    if ($scope.legend[mapID]._map) { //This may break in future leaflet versions
                        $scope.map[mapID].removeControl($scope.legend[mapID]);
                    }
                    $scope.legend[mapID].addTo($scope.map[mapID]);
                    //force a redraw
                    if (angular.isDefined($scope.geoJSON[mapID]._geojsons.default)) {
                        $scope.geoJSON[mapID]._geojsons.default.eachLayer($scope.handleLayer);
                    }
                };

                //apply relevent renderer to layer
                $scope.handleLayer = function (layer) {
                    var mapID = layer.options.mapID;
                    if (mapID === "viewermap") {
                        //Join geography and results table
                        var thisAttr;
                        for (var i = 0; i < $scope.viewerTableOptions.data.length; i++) {
                            if ($scope.viewerTableOptions.data[i].area_id === layer.feature.properties.area_id) {
                                thisAttr = $scope.viewerTableOptions.data[i][ChoroService.getMaps("viewermap").feature];
                                break;
                            }
                        }
                        //is selected?
                        var selected = false;
                        if (angular.isArray($scope.thisPoly) && $scope.thisPoly.indexOf(layer.feature.properties.area_id) !== -1) {
                            selected = true;
                        }
                        var polyStyle = ChoroService.getRenderFeatureMapping(thisMap["viewermap"].scale, thisAttr, selected);
                        layer.setStyle({
                            weight: 1,
                            color: "gray",
                            fillColor: polyStyle,
                            fillOpacity: $scope.child.transparency[mapID]
                        });
                    } else {
                        if ($scope.tableData[mapID].length !== 0) {
                            var thisAttr;
                            for (var i = 0; i < $scope.tableData[mapID].length; i++) {
                                if ($scope.tableData[mapID][i].area_id === layer.feature.properties.area_id) {
                                    thisAttr = $scope.tableData[mapID][i][ChoroService.getMaps(mapID).feature];
                                    break;
                                }
                            }
                            var polyStyle = ChoroService.getRenderFeatureViewer(thisMap[mapID].scale, layer.feature, thisAttr, $scope.thisPoly[mapID]);
                            layer.setStyle({
                                weight: polyStyle[2],
                                color: polyStyle[1],
                                fillColor: polyStyle[0],
                                fillOpacity: $scope.child.transparency[mapID]
                            });
                        }
                    }
                };
                $scope.updateStudy = function (mapID) {
                    //Check inputs are valid
                    if ($scope.studyID[mapID] === null || $scope.sex[mapID] === null) {
                        $scope.showError("Invalid study or sex code");
                        clearTheMapOnError(mapID);
                    } else {
                        //Reset all renderers, but only if not called from state change
                        if (!$scope.myService.getState().initial) {
                            thisMap[mapID] = ChoroService.getMaps(mapID).renderer;
                        }
                        $scope.myService.getState().initial = false;
                        //Remove RR chart
                        if (mapID !== "viewermap" && !angular.isUndefined($scope.rrChartData[mapID])) {
                            $scope.rrChartData[mapID].length = 0;
                        }

                        //Remove any existing geography
                        if ($scope.map[mapID].hasLayer($scope.geoJSON[mapID])) {
                            $scope.map[mapID].removeLayer($scope.geoJSON[mapID]);
                        }

                        //save study, sex selection
                        $scope.myService.getState().sex[mapID] = $scope.sex[mapID];
                        $scope.myService.getState().study[mapID] = $scope.$parent.studyID[mapID];
                        //add the requested geography
                        user.getGeographyAndLevelForStudy(user.currentUser, $scope.studyID[mapID].study_id).then(
                                function (res) {
                                    $scope.tileInfo[mapID].geography = res.data[0][0]; //e.g. SAHSU
                                    $scope.tileInfo[mapID].level = res.data[0][1]; //e.g. LEVEL3

                                    var topojsonURL = user.getTileMakerTiles(user.currentUser, $scope.tileInfo[mapID].geography, $scope.tileInfo[mapID].level);
                                    $scope.geoJSON[mapID] = new L.topoJsonGridLayer(topojsonURL, {
                                        attribution: 'Polygons &copy; <a href="http://www.sahsu.org/content/rapid-inquiry-facility" target="_blank">Imperial College London</a>',
                                        layers: {
                                            default: {
                                                mapID: mapID,
                                                renderer: L.canvas(),
                                                style: function (feature) {
                                                    return({
                                                        weight: 1,
                                                        opacity: 1,
                                                        color: "gray",
                                                        fillColor: "transparent"
                                                    });
                                                },
                                                onEachFeature: function (feature, layer) {
                                                    layer.on('mouseover', function (e) {
                                                        this.setStyle({
                                                            color: 'gray',
                                                            weight: 1.5,
                                                            fillOpacity: function () {
                                                                return($scope.child.transparency[mapID] - 0.3 > 0 ? $scope.child.transparency[mapID] - 0.3 : 0.1);
                                                            }()
                                                        });
                                                        $scope.infoBox[mapID].update(layer.feature.properties.area_id);
                                                    });
                                                    layer.on('mouseout', function (e) {
                                                        $scope.geoJSON[mapID]._geojsons.default.eachLayer($scope.handleLayer);
                                                        $scope.infoBox[mapID].update(false);
                                                    });
                                                    layer.on('click', function (e) {
                                                        if (mapID === "viewermap") {
                                                            //Multiple selections
                                                            var thisPoly = e.target.feature.properties.area_id;
                                                            var bFound = false;
                                                            for (var i = 0; i < $scope.thisPoly.length; i++) {
                                                                if ($scope.thisPoly[i] === thisPoly) {
                                                                    bFound = true;
                                                                    $scope.thisPoly.splice(i, 1);
                                                                    break;
                                                                }
                                                            }
                                                            if (!bFound) {
                                                                $scope.thisPoly.push(thisPoly);
                                                            }
                                                        } else {
                                                            //Single selections
                                                            $scope.thisPoly[mapID] = e.target.feature.properties.area_id;
                                                            $scope.myService.getState().area_id[mapID] = e.target.feature.properties.area_id;
                                                            $scope.infoBox2[mapID].update($scope.thisPoly[mapID]);
                                                            $scope.$parent.updateMapSelection($scope.thisPoly[mapID], mapID);
                                                            if ($scope.bLockSelect) {
                                                                var otherMap = MappingService.getOtherMap(mapID);
                                                                $scope.thisPoly[otherMap] = e.target.feature.properties.area_id;
                                                                $scope.myService.getState().area_id[otherMap] = e.target.feature.properties.area_id;
                                                                $scope.$parent.updateMapSelection(e.target.feature.properties.area_id, otherMap);
                                                            }
                                                        }
                                                        $scope.$parent.$digest();
                                                    });
                                                }
                                            }
                                        }
                                    });

                                    //force re-render of new tiles
                                    $scope.geoJSON[mapID].on('load', function (e) {
                                        $scope.geoJSON[mapID]._geojsons.default.eachLayer($scope.handleLayer);
                                    });

                                    user.getGeoLevelSelectValues(user.currentUser, $scope.tileInfo[mapID].geography).then(function (res) {
                                        var lowestLevel = res.data[0].names[0];
                                        user.getTileMakerTilesAttributes(user.currentUser, $scope.tileInfo[mapID].geography, lowestLevel).then(function (res) {
                                            $scope.maxbounds = L.latLngBounds([res.data.bbox[1], res.data.bbox[2]], [res.data.bbox[3], res.data.bbox[0]]);
                                            if (mapID !== "diseasemap2") {
                                                //do not get maxbounds for diseasemap2
                                                if ($scope.myService.getState().center[mapID].lat === 0) {
                                                    $scope.map[mapID].fitBounds($scope.maxbounds);
                                                } else {
                                                    var centre = $scope.myService.getState().center[mapID];
                                                    $scope.map[mapID].setView([centre.lat, centre.lng], centre.zoom);
                                                }
                                            } else {
                                                var centre = $scope.myService.getState().center[mapID];
                                                $scope.map[mapID].setView([centre.lat, centre.lng], centre.zoom);
                                            }
                                        });
                                    });
                                    if (mapID !== "viewermap") {
                                        $scope.mapLocking();
                                    }
                                }
                        ).then(function () {
                            $scope.map[mapID].addLayer($scope.geoJSON[mapID]);
                        }).then(function () {
                            getAttributeTable(mapID);

                            //pan events                            
                            $scope.map[mapID].on('zoomend', function (e) {
                                $scope.myService.getState().center[mapID].zoom = $scope.map[mapID].getZoom();
                            });
                            $scope.map[mapID].on('moveend', function (e) {
                                $scope.myService.getState().center[mapID].lng = $scope.map[mapID].getCenter().lng;
                                $scope.myService.getState().center[mapID].lat = $scope.map[mapID].getCenter().lat;
                            });

                        }).then(function () {
                            $scope.refresh(mapID);
                        });
                    }

                    //Sync or unsync map extents
                    $scope.mapLocking = function () {
                        if ($scope.$parent.bLockCenters) {
                            $scope.map["diseasemap1"].sync($scope.map["diseasemap2"]);
                            $scope.map["diseasemap2"].sync($scope.map["diseasemap1"]);
                        } else {
                            $scope.map["diseasemap1"].unsync($scope.map["diseasemap2"]);
                            $scope.map["diseasemap2"].unsync($scope.map["diseasemap1"]);
                        }
                    };

                    function getAttributeTable(mapID) {
                        user.getSmoothedResults(user.currentUser, $scope.studyID[mapID].study_id, MappingService.getSexCode($scope.sex[mapID]))
                                .then(function (res) {
                                    //variables possible to map
                                    var attrs = ["smoothed_smr", "relative_risk", "posterior_probability"];
                                    ChoroService.getMaps(mapID).features = attrs;
                                    //make array for choropleth
                                    $scope.tableData[mapID].length = 0;
                                    for (var i = 0; i < res.data.smoothed_results.length; i++) {
                                        $scope.tableData[mapID].push(res.data.smoothed_results[i]);
                                    }

                                    //supress some pre-selected columns (see the service)   
                                    if (mapID === "viewermap") {
                                        //fill results table for data viewer only
                                        var colDef = [];
                                        var attrs = [];
                                        //Add column for selected
                                        for (var i = 0; i < res.data.smoothed_results.length; i++) {
                                            res.data.smoothed_results[i]._selected = 0;
                                        }

                                        for (var i in res.data.smoothed_results[0]) {
                                            if (ViewerStateService.getValidColumn(i)) {
                                                if (i !== "area_id" && i !== "_selected") {
                                                    attrs.push(i);
                                                }
                                                colDef.push({
                                                    name: i,
                                                    width: 100
                                                });
                                            }
                                        }
                                        //draw and refresh histogram and table
                                        ChoroService.getMaps("viewermap").features = attrs;
                                        $scope.viewerTableOptions.columnDefs = colDef;
                                        $scope.viewerTableOptions.data = $scope.tableData.viewermap;
                                        $scope.updateTable();
                                    }
                                    
                                    //draw D3 plots
                                    $scope.getD3chart(mapID, $scope.attr[mapID]);

                                }, function (e) {
                                    console.log("Something went wrong when getting the attribute data");
                                    clearTheMapOnError(mapID);
                                });
                    }

                    /*
                     * INFO BOXES AND LEGEND
                     */
                    //An empty control on map
                    function closureAddControl(m) {
                        return function () {
                            this._div = L.DomUtil.create('div', 'info');
                            this.update();
                            return this._div;
                        };
                    }
                    //The hover box update
                    function closureInfoBoxUpdate(m) {
                        return function (poly) {
                            if (poly) {
                                this._div.innerHTML =
                                        function () {
                                            var feature = ChoroService.getMaps(m).feature;
                                            var tmp;
                                            var inner = '<h5>ID: ' + poly + '</h5>';
                                            if ($scope.attr[m] !== "") {
                                                for (var i = 0; i < $scope.tableData[m].length; i++) {
                                                    if ($scope.tableData[m][i].area_id === poly) {
                                                        tmp = $scope.tableData[m][i][$scope.attr[m]];
                                                        break;
                                                    }
                                                }
                                                if (feature !== "" && !isNaN(Number(tmp))) {
                                                    inner = '<h5>ID: ' + poly + '</br>' + feature.toUpperCase().replace("_", " ") + ": " + Number(tmp).toFixed(3) + '</h5>';
                                                }
                                            }
                                            return inner;
                                        }();
                            } else {
                                this._div.innerHTML = '';
                            }
                        };
                    }
                    //Area info box update
                    function closureInfoBox2Update(m) {
                        return function (poly) {
                            if (poly === null) {
                                this._div.innerHTML = "";
                            } else {
                                var results = null;
                                for (var i = 0; i < $scope.tableData[m].length; i++) {
                                    if ($scope.tableData[m][i].area_id === poly) {
                                        results = $scope.tableData[m][i];
                                    }
                                }
                                if (results !== null) {
                                    this._div.innerHTML =
                                            '<h5>ID: ' + poly + '</br>' +
                                            'Population: ' + results.population + '</br>' +
                                            'Observed: ' + results.observed + '</br>' +
                                            'Expected: ' + Number(results.expected).toFixed(2) + '</br>' + '</h5>';
                                } else {
                                    this._div.innerHTML = "";
                                }
                            }
                        };
                    }

                    //Add the controls
                    for (var i = 0; i < parentScope.myMaps.length; i++) {
                        var m = parentScope.myMaps[i];
                        $scope.infoBox[m].onAdd = closureAddControl(m);
                        $scope.infoBox[m].update = closureInfoBoxUpdate(m);
                        if (m !== "viewermap") {
                            $scope.infoBox2[m].onAdd = closureAddControl(m);
                            $scope.infoBox2[m].update = closureInfoBox2Update(m);
                        }
                    }
                    for (var i = 0; i < parentScope.myMaps.length; i++) {
                        if (parentScope.myMaps.indexOf("diseasemap1") !== -1) {
                            $scope.infoBox2["diseasemap1"].addTo($scope.map["diseasemap1"]);
                            $scope.infoBox["diseasemap1"].addTo($scope.map["diseasemap1"]);
                        }
                        if (parentScope.myMaps.indexOf("diseasemap2") !== -1) {
                            $scope.infoBox2["diseasemap2"].addTo($scope.map["diseasemap2"]);
                            $scope.infoBox["diseasemap2"].addTo($scope.map["diseasemap2"]);
                        }
                        if (parentScope.myMaps.indexOf("viewermap") !== -1) {
                            $scope.infoBox["viewermap"].addTo($scope.map["viewermap"]);
                        }
                    }
                };
            }]);