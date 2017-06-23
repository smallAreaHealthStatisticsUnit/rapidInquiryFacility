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
 * CONTROLLER for data export
 */

/* global L, key, topojson, d3 */
angular.module("RIF")
        .controller('ExportCtrl', ['$scope', 'user', '$timeout', 'LeafletBaseMapService', 'mapTools', 'ExportStateService',
            function ($scope, user, $timeout, LeafletBaseMapService, mapTools, ExportStateService) {

                //study 
                $scope.studyIDs = [];
                $scope.studyID = {
                    "viewermap": ExportStateService.getState().study['exportmap']
                };

                $scope.area = {
                    name: ExportStateService.getState().area.name
                };

                //preview map
                $scope.map = ({
                    "exportmap": {}
                });
                $scope.exportLevel = 9;
                $scope.geoJSON = {};
                $scope.studyBounds = new L.LatLngBounds();

                //transparency as set by slider
                $scope.transparency = {
                    "exportmap": ExportStateService.getState().transparency["exportmap"]
                };

                //the default basemap              
                $scope.thisLayer = {
                    "exportmap": LeafletBaseMapService.setBaseMap(LeafletBaseMapService.getCurrentBaseMapInUse("exportmap"))
                };

                $scope.vSplit1 = ExportStateService.getState().vSplit1;
                $scope.hSplit1 = ExportStateService.getState().hSplit1;

                $scope.$on('ui.layout.resize', function (e, beforeContainer, afterContainer) {
                    if (beforeContainer.id === "vSplit1") {
                        ExportStateService.getState().vSplit1 = (beforeContainer.size / beforeContainer.maxSize) * 100;
                        $scope.map['exportmap'].invalidateSize();
                    } else {
                        ExportStateService.getState().hSplit1 = (beforeContainer.size / beforeContainer.maxSize) * 100;
                    }
                });

                $timeout(function () {
                    //make map
                    $scope.map['exportmap'] = L.map('exportmap', {condensedAttributionControl: false}).setView([0, 0], 1);

                    //Attributions to open in new window
                    L.control.condensedAttribution({
                        prefix: '<a href="http://leafletjs.com" target="_blank">Leaflet</a>'
                    }).addTo($scope.map['exportmap']);

                    //slider
                    var slider = L.control.slider(function (v) {
                        ExportStateService.getState().transparency['exportmap'] = v;
                        $scope.transparency['exportmap'] = v;
                        if (angular.isDefined($scope.geoJSON._geojsons)) {
                            $scope.geoJSON._geojsons.default.eachLayer($scope.handleLayer);
                        }
                    }, {
                        id: slider,
                        position: 'topleft',
                        orientation: 'horizontal',
                        min: 0,
                        max: 1,
                        step: 0.01,
                        value: ExportStateService.getState().transparency['exportmap'],
                        title: 'Transparency',
                        logo: '',
                        syncSlider: true

                    }).addTo($scope.map['exportmap']);

                    var areaSelect = mapTools.getAreaExportSelect($scope);
                    new areaSelect().addTo($scope.map['exportmap']);

                    var tool = mapTools.getZoomToStudy($scope);
                    new tool().addTo($scope.map['exportmap']);

                    //scalebar
                    L.control.scale({position: 'bottomleft', imperial: false}).addTo($scope.map['exportmap']);

                    $scope.map['exportmap'].doubleClickZoom.disable();
                    $scope.map['exportmap'].keyboard.disable();

                    $scope.getStudies();
                    $scope.renderMap("exportmap");

                    setTimeout(function () {
                        $scope.map['exportmap'].invalidateSize();
                    }, 50);
                });

                //tables
                $scope.tableOptions = {
                    extract: {
                        gridMenuShowHideColumns: false,
                        exporterMenuPdf: false,
                        enableColumnResizing: true,
                        enableHorizontalScrollbar: 1,
                        rowHeight: 25,
                        onRegisterApi: function (gridApi) {
                            $scope.gridApi = gridApi;
                        }
                    },
                    results: {
                        gridMenuShowHideColumns: false,
                        exporterMenuPdf: false,
                        enableColumnResizing: true,
                        enableHorizontalScrollbar: 1,
                        rowHeight: 25,
                        onRegisterApi: function (gridApi) {
                            $scope.gridApi = gridApi;
                        }
                    }
                };

                $scope.rows = {
                    extract: ExportStateService.getState().rows.extract,
                    results: ExportStateService.getState().rows.results
                };

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

                        //Restore defaults
                        var s = ExportStateService.getState().study['exportmap'].study_id;
                        if (s !== null) {
                            for (var i = 0; i < $scope.studyIDs.length; i++) {
                                if ($scope.studyIDs[i].study_id === s) {
                                    $scope.studyID['exportmap'] = $scope.studyIDs[i];
                                }
                            }
                        } else {
                            $scope.studyID['exportmap'] = $scope.studyIDs[0];
                        }
                    }, function (e) {
                        $scope.showError("Could not retrieve study status");
                    }).then(function () {
                        //fill initial preview
                        $scope.updateStudy("exportmap");
                        $scope.preview('extract');
                        $scope.preview('results');
                    });
                };

                //update study list if new study processed
                $scope.$on('updateStudyDropDown', function (event, thisStudy) {
                    $scope.studyIDs.push(thisStudy);
                });


                //export query, map and extract tables as a Zip File
                $scope.exportAllTables = function () {
                    $scope.showSuccess("Export started...");
                    user.getZipFile(user.currentUser, $scope.studyID["exportmap"].study_id).then(function (res) {
                        if (res.data === "") {
                            $scope.showSuccess("Export finished: " + $scope.studyID["exportmap"].name + " please check your defined extract directory");
                        } else {
                            $scope.showError("Error exporting study tables");
                        }
                    });
                };

                $scope.preview = function (table) {
                    if ($scope.rows[table][0] === 0 | $scope.rows[table][1] === 0) {
                        $scope.showError("Row number cannot be zero");
                        return;
                    }
                    if ($scope.rows[table][0] >= $scope.rows[table][1]) {
                        $scope.showError("Upper bound must be higher than lower bound");
                        return;
                    }
                    var tableData = [];
                    var colDef = [];
                    user.getStudyTableForProcessedStudy(user.currentUser, $scope.studyID["exportmap"].study_id, table,
                            $scope.rows[table][0], $scope.rows[table][1]).then(function (res) {

                        //save row numbers
                        ExportStateService.getState().rows[table] = [$scope.rows[table][0], $scope.rows[table][1]];

                        for (var i = 0; i < res.data[0].columnNames.length; i++) {
                            colDef.push({
                                name: res.data[0].columnNames[i],
                                width: 100
                            });
                        }
                        for (var i = 0; i < res.data[0].data.length; i++) {
                            var obj = {};
                            for (var j = 0; j < colDef.length; j++) {
                                obj[colDef[j].name] = res.data[0].data[i][j];
                            }
                            tableData.push(obj);
                        }
                    }).then(function () {
                        $scope.tableOptions[table].columnDefs = colDef;
                        $scope.tableOptions[table].data = tableData;
                    });
                };

                //export map geojson as a Zip File
                $scope.exportToGeoJSON = function () {
                    $scope.showWarning("This don't do nowt yet it don't");
                };

                //change the basemaps 
                //TODO: do not need this of nt offering basemaps
                $scope.renderMap = function (mapID) {
                    $scope.map[mapID].removeLayer($scope.thisLayer[mapID]);
                    //add new baselayer if requested
                    if (!LeafletBaseMapService.getNoBaseMap(mapID)) {
                        $scope.thisLayer[mapID] = LeafletBaseMapService.setBaseMap(LeafletBaseMapService.getCurrentBaseMapInUse(mapID));
                        $scope.thisLayer[mapID].addTo($scope.map[mapID]);
                    }
                };

                var areaIDs = [];
                var bBB = true;

                $scope.updateStudy = function (mapID) {
                    if ($scope.map[mapID].hasLayer($scope.geoJSON)) {
                        $scope.map[mapID].removeLayer($scope.geoJSON);
                    }
                    bBB = true;

                    var thisGeography;
                    var thisResolution;

                    $scope.studyBounds = new L.LatLngBounds();

                    user.getGeographyAndLevelForStudy(user.currentUser, $scope.studyID[mapID].study_id).then(
                            function (res) {
                                //Store this study selection
                                ExportStateService.getState().study['exportmap'] = $scope.studyID['exportmap'];
                                ExportStateService.getState().area.name = $scope.area.name;

                                thisGeography = res.data[0][0];
                                thisResolution = res.data[0][1];

                                user.getStudySubmission(user.currentUser, $scope.studyID["exportmap"].study_id)
                                        .then(
                                                function (res) {
                                                    areaIDs.length = 0;
                                                    //TODO: will cause issues for risk analysis study
                                                    var whyDidYouDoItLikeThatKevin = "disease_mapping_study_area";
                                                    if ($scope.area.name !== "study") {
                                                        whyDidYouDoItLikeThatKevin = "comparison_area";
                                                    }
                                                    var tmp = res.data.rif_job_submission.disease_mapping_study[whyDidYouDoItLikeThatKevin].map_areas.map_area;
                                                    for (var i = 0; i < tmp.length; i++) {
                                                        areaIDs.push(tmp[i].id);
                                                    }
                                                })
                                        .then(
                                                function () {
                                                    var topojsonURL = user.getTileMakerTiles(user.currentUser, thisGeography, thisResolution);
                                                    if (bBB) {
                                                        bBB = false;
                                                    }
                                                    $scope.geoJSON = new L.topoJsonGridLayer(topojsonURL, {
                                                        attribution: 'Polygons &copy; <a href="http://www.sahsu.org/content/rapid-inquiry-facility" target="_blank">Imperial College London</a>',
                                                        layers: {
                                                            default: {
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
                                                                    if (!bBB) {
                                                                        if (areaIDs.indexOf(layer.feature.properties.area_id) !== -1) {
                                                                            $scope.studyBounds.extend(layer.getBounds());
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    });
                                                    $scope.geoJSON.on('load', function (e) {
                                                        $scope.geoJSON._geojsons.default.eachLayer($scope.handleLayer);
                                                        bBB = true;
                                                    });
                                                    $scope.map[mapID].addLayer($scope.geoJSON);
                                                    $scope.preview('extract');
                                                    $scope.preview('results');

                                                    //pan events                            
                                                    $scope.map[mapID].on('zoomend', function (e) {
                                                        ExportStateService.getState().center[mapID].zoom = $scope.map[mapID].getZoom();
                                                    });
                                                    $scope.map[mapID].on('moveend', function (e) {
                                                        ExportStateService.getState().center[mapID].lng = $scope.map[mapID].getCenter().lng;
                                                        ExportStateService.getState().center[mapID].lat = $scope.map[mapID].getCenter().lat;
                                                    });
                                                });
                            }
                    );
                };

                $scope.handleLayer = function (layer) {
                    if (areaIDs.indexOf(layer.feature.properties.area_id) === -1) {
                        layer.setStyle({
                            color: "transparent",
                            fillColor: "transparent"
                        });
                    } else {
                        layer.setStyle({
                            weight: 1,
                            color: "gray",
                            fillColor: function () {
                                if ($scope.area.name === "study") {
                                    return "blue";
                                } else {
                                    return "purple";
                                }
                            }(),
                            fillOpacity: $scope.transparency["exportmap"]
                        });
                        if (!bBB) {
                            if (ExportStateService.getState().center['exportmap'].lat === 0) {
                                $scope.map['exportmap'].fitBounds($scope.studyBounds);
                            } else {
                                var centre = ExportStateService.getState().center['exportmap'];
                                $scope.map['exportmap'].setView([centre.lat, centre.lng], centre.zoom);
                            }
                        }
                    }
                };
            }]);