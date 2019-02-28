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

                //reset the form to untouched
                $scope.$on("$destroy", function () {
                    ExportStateService.getState().initial = true;
                });

				$scope.exportTAG="Export Study Tables";
				$scope.disableMapListButton=false;
				$scope.myMaps = ["exportmap"];
				
                //study 
                $scope.studyIDs = [];
                $scope.studyID = {
                    "exportmap": ExportStateService.getState().study['exportmap']
                };

                $scope.area = {
                    name: ExportStateService.getState().area.name
                };

                //preview map
                $scope.map = ({
                    "exportmap": {}
                });
                $scope.exportLevel = ExportStateService.getState().zoomLevel;
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

                //ui-layout
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
                    $scope.map['exportmap'] = L.map('exportmap', {
                        condensedAttributionControl: false}
                    ).setView([0, 0], 1);

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

                //remember what has been selected, default is 1 - 100
                $scope.rows = {
                    extract: ExportStateService.getState().rows.extract,
                    results: ExportStateService.getState().rows.results
                };
				 /*
					C: created, not verified; 
					V: verified, but no other work done; [NOT USED BY MIDDLEWARE]
					E: extracted imported or created, but no results or maps created; 
					G: Extract failure, extract, results or maps not created;
					R: initial results population, create map table; [NOT USED BY MIDDLEWARE]
					S: R success;
					F: R failure, R has caught one or more exceptions [depends on the exception handler design]
					W: R warning. [NOT USED BY MIDDLEWARE]
				 */
                //fills the selction drop-down
                $scope.getStudies = function () {
                    user.getCurrentStatusAllStudies(user.currentUser).then(function (res) {
                        for (var i = 0; i < res.data.smoothed_results.length; i++) {
                            if (res.data.smoothed_results[i].study_state === "S") { // New success
                                var thisStudy = {
                                    "study_id": res.data.smoothed_results[i].study_id,
                                    "name": res.data.smoothed_results[i].study_name,
                                    "study_type": res.data.smoothed_results[i].study_type
                                };
                                $scope.studyIDs.push(thisStudy);
                            }
                            else if (res.data.smoothed_results[i].study_state === "R") { // Old success
                                var thisStudy = {
                                    "study_id": res.data.smoothed_results[i].study_id,
                                    "name": res.data.smoothed_results[i].study_name,
                                    "study_type": res.data.smoothed_results[i].study_type
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
						// Study ID now set as: $scope.studyID["exportmap"].study_id
						$scope.setupZipButton();

                    }, function (e) {
                        $scope.showError("Could not retrieve study status; unable to export or view data");
						$scope.consoleLog("[rifc-expt-export.js] Could not retrieve study status; unable to export or view data" + 
							JSON.stringify(e));
                    }).then(function () {
                        //fill initial preview
                        $scope.updateStudy("exportmap");
                        $scope.preview('extract');
                        $scope.preview('results');
                    });
                };

				$scope.setupZipButton = function setupZipButton() {
					$scope.extractStatus=user.getExtractStatus(user.currentUser, $scope.studyID["exportmap"].study_id).then(
						function successCallback(res) {
							if (res.data.status === "STUDY_EXTRACTABLE_NEEDS_ZIPPING") {			
								$scope.exportTAG="Export Study Tables";
								$scope.exportURL = undefined;
								$scope.disableMapListButton=false;
							}
							else if (res.data.status === "STUDY_EXTRACTBLE_ZIPPID") {
								$scope.exportURL = user.getZipFileURL(user.currentUser, $scope.studyID["exportmap"].study_id, 
									$scope.exportLevel); // Set mapListButtonExport URL
								$scope.exportTAG="Download Study Export";
								$scope.disableMapListButton=false;
							}
							else if (res.data.status === "STUDY_INCOMPLETE_NOT_ZIPPABLE") {
								$scope.exportTAG="Study Incomplete";
								$scope.exportURL = undefined;
								$scope.disableMapListButton=true;
							}
							else if (res.data.status === "STUDY_FAILED_NOT_ZIPPABLE") {
								$scope.exportTAG="Study Failed";
								$scope.exportURL = undefined;
								$scope.disableMapListButton=true;
							}
							else if (res.data.status === "STUDY_ZIP_IN_PROGRESS") {
								$scope.exportTAG="Zip in progress";
								$scope.exportURL = undefined;
								$scope.disableMapListButton=true;
							}
							else if (res.data.status === "STUDY_ZIP_FAILED") {
								$scope.exportTAG="Zip Failed";
								$scope.exportURL = undefined;
								$scope.disableMapListButton=true;
							}
							else {		
								$scope.exportTAG="Study NOT Exportable";
								$scope.exportURL = undefined;
								$scope.disableMapListButton=true;
							}
						}, function errorCallback(res) {
							// called asynchronously if an error occurs
							// or server returns response with an error status.
							$scope.showError("Could not retrieve extract status; unable to export data");
							$scope.consoleLog("Could not retrieve extract status; error: " + JSON.stringify(err));
						}
					);
				}
					
                //update study list if new study processed
                $scope.$on('updateStudyDropDown', function (event, thisStudy) {
                    $scope.studyIDs.push(thisStudy);
                });

                //TileMaker can give tiles at varying levels of details
                //user has to decide how detailed they want to download
                $scope.detailLevelChange = function () {
                    ExportStateService.getState().zoomLevel = $scope.exportLevel;
                };

                //export query, map and extract tables as a Zip File
                $scope.exportAllTables = function (e) {
					if ($scope.exportTAG == "Export Study Tables") {
						$scope.showSuccess("Export started...");
						$scope.exportTAG="Exporting...";
						$scope.exportURL = undefined;
						$scope.disableMapListButton=true;
						var startTime = new Date().getTime();
						$scope.createZipFileTimeout=360000;	/* in mS: 360s - 6 mins */
						user.createZipFile(user.currentUser, $scope.studyID["exportmap"].study_id, $scope.exportLevel, $scope.createZipFileTimeout).then(
							function (res) { // Sucesss handler
								if (res.data.status === "OK") {
									$scope.disableMapListButton=false;
									$scope.showSuccessNoHide("Export finished: " + $scope.studyID["exportmap"].name + "; ready to download.");
									$scope.exportURL = user.getZipFileURL(user.currentUser, $scope.studyID["exportmap"].study_id, 
										$scope.exportLevel); // Set mapListButtonExport URL
									$scope.exportTAG="Download Study Export";
															// Set mapListButtonExport text
								} else {
									$scope.exportTAG="Export Study Tables";
									$scope.exportURL = undefined;
									$scope.disableMapListButton=true;
									$scope.showError("Error exporting study tables for: " + $scope.studyID["exportmap"].name);
								}
							},
							function (err) { // Error handler 
													
								var respTime = new Date().getTime() - startTime;
								if(respTime >= $scope.createZipFileTimeout){
									// time out handling
									$scope.showWarningNoHide("Study tables export timeout for: " + 
										$scope.studyID["exportmap"].name + " after: " + (respTime/1000) + 
										" S; please re-vist this page later");
									$scope.consoleLog("Export timeout: " + JSON.stringify(err));
								} 
								else {
									// other error handling
									$scope.showErrorNoHide("Study tables export error for: " + 
										$scope.studyID["exportmap"].name);
									$scope.consoleLog("Export error: " + JSON.stringify(err));
								}							
								$scope.exportURL = undefined;
								$scope.disableMapListButton=true;
								$scope.exportTAG="Download had error";
							}
						);
					}
                };

                //get rows from the database
                $scope.preview = function (table) {
                    if (angular.isUndefined($scope.studyID["exportmap"])) {
                        return;
                    }
                    if (angular.isUndefined($scope.rows[table][0]) | angular.isUndefined($scope.rows[table][1])) {
                        $scope.showError("Row number cannot be empty");
                        return;
                    }
                    if ($scope.rows[table][0] === 0 | $scope.rows[table][1] === 0) {
                        $scope.showError("Row number cannot be zero");
                        return;
                    }
                    if ($scope.rows[table][0] > $scope.rows[table][1]) {
                        $scope.showError("Upper bound must be higher than lower bound");
                        return;
                    }
                    var tableData = [];
                    var colDef = [];
                    user.getStudyTableForProcessedStudy(user.currentUser, $scope.studyID["exportmap"].study_id, table,
                            $scope.rows[table][0], $scope.rows[table][1]).then(function (res) {

                        //save row numbers used
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
                        //fix upper bound to max possible
                        var maxn = parseInt(tableData[tableData.length - 1]['row']);
                        if ($scope.rows[table][1] > maxn) {
                            $scope.rows[table][1] = maxn;
                        }
                    });
                };

                //basemap, this is not changeable here (but could be as in other dashboards)
                $scope.renderMap = function (mapID) {
                    $scope.map[mapID].removeLayer($scope.thisLayer[mapID]);
                    $scope.thisLayer[mapID] = LeafletBaseMapService.setBaseMap(LeafletBaseMapService.getCurrentBaseMapInUse(mapID));
                    $scope.thisLayer[mapID].addTo($scope.map[mapID]);
                };

                var areaIDs = [];
                var bBB = true; //is bounding box not defined yet, needed to set zoom to layer

                $scope.updateStudy = function (mapID) {
					$scope.exportURL = "";
					$scope.exportTAG="Export Study Tables";
					
                    if ($scope.map[mapID].hasLayer($scope.geoJSON)) {
                        $scope.map[mapID].removeLayer($scope.geoJSON);
                    }
                    bBB = true;

                    if (!ExportStateService.getState().initial) {
                        $scope.rows = {
                            extract: [1, 100],
                            results: [1, 100]
                        };
                    }

                    var thisGeography;
                    var thisResolution;

                    $scope.studyBounds = new L.LatLngBounds();

                    if ($scope.studyID[mapID]) {



                        user.getGeographyAndLevelForStudy(user.currentUser, $scope.studyID[mapID].study_id).then(
                                function (res) {
                                    //Store this study selection
                                    ExportStateService.getState().study['exportmap'] = $scope.studyID['exportmap'];
                                    ExportStateService.getState().area.name = $scope.area.name;

                                    thisGeography = res.data[0][0];
                                    thisResolution = res.data[0][1];

									$scope.setupZipButton();
									
                                    user.getStudySubmission(user.currentUser, $scope.studyID["exportmap"].study_id)
                                            .then(
                                                    function (res) {
                                                        areaIDs.length = 0;
                                                        //TODO: will cause issues for a risk analysis study
                                                        //This naming does not seem very consistent or logical - middleware issue                                                          
                                                        var whyDidYouDoItLikeThatKevin = "disease_mapping_study";
														if ($scope.studyID['exportmap'].study_type && 
															$scope.studyID['exportmap'].study_type == "risk_analysis_study") {
															whyDidYouDoItLikeThatKevin = "risk_analysis_study";
														}
														
														if (res.data.rif_job_submission[whyDidYouDoItLikeThatKevin] == undefined &&
															whyDidYouDoItLikeThatKevin != "risk_analysis_study") {
															whyDidYouDoItLikeThatKevin = "risk_analysis_study";
														}
														
														if (res.data.rif_job_submission[whyDidYouDoItLikeThatKevin] == undefined &&
                                                            $scope.area.name !== "study") {
                                                            whyDidYouDoItLikeThatKevin = "comparison_area";
                                                        }
														
														if (res.data.rif_job_submission[whyDidYouDoItLikeThatKevin]) {
															var tmp = res.data.rif_job_submission[whyDidYouDoItLikeThatKevin].map_areas.map_area;
															for (var i = 0; i < tmp.length; i++) {
																areaIDs.push(tmp[i].id);
															}
														}
														else {
															$scope.consoleLog("[rifc-expt-export.js] WARNING res.data.rif_job_submission[" + 
																whyDidYouDoItLikeThatKevin + "] does not exist, $scope.studyID['exportmap']: " +
																JSON.stringify($scope.studyID['exportmap'], null, 1) + "; res.data.rif_job_submission: " + 	
																JSON.stringify(res.data.rif_job_submission, null, 1));
															$scope.consoleLog("Cannot load areaIDs for export map");
														}
                                                    })
                                            .then(
                                                    function () {
                                                        var topojsonURL = user.getTileMakerTiles(user.currentUser, thisGeography, thisResolution);
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
                                                                        if (areaIDs.indexOf(layer.feature.properties.area_id) !== -1) {
                                                                            layer.bindPopup(feature.properties.name || 
																					feature.properties.area_id, {
                                                                                closeButton: false,
                                                                                autoPan: false
                                                                            });
                                                                            layer.on('mouseover', function () {
                                                                                layer.openPopup();
                                                                                this.setStyle({
                                                                                    fillOpacity: function () {
                                                                                        return($scope.transparency[mapID] - 0.3 > 0 ? $scope.transparency[mapID] - 0.3 : 0.1);
                                                                                    }()
                                                                                });

                                                                            });
                                                                            layer.on('mouseout', function () {
                                                                                layer.closePopup();
                                                                                $scope.geoJSON._geojsons.default.eachLayer($scope.handleLayer);
                                                                            });
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        });
                                                        $scope.geoJSON.on('load', function (e) {
                                                            $scope.geoJSON._geojsons.default.eachLayer($scope.handleLayer);
                                                            if (bBB) {
                                                                if (ExportStateService.getState().initial) {
                                                                    if (ExportStateService.getState().center['exportmap'].lat === 0) {
                                                                        $scope.map['exportmap'].fitBounds($scope.studyBounds);
                                                                    } else {
                                                                        var centre = ExportStateService.getState().center['exportmap'];
                                                                        $scope.map['exportmap'].setView([centre.lat, centre.lng], centre.zoom);
                                                                    }
                                                                } else {
                                                                    $scope.map['exportmap'].fitBounds($scope.studyBounds);
                                                                }
                                                                bBB = false;
                                                                ExportStateService.getState().initial = false;
                                                            }
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
                    }
                };

                $scope.handleLayer = function (layer) {
                    if (areaIDs.indexOf(layer.feature.properties.area_id) === -1) {
                        layer.setStyle({
                            color: "transparent",
                            fillColor: "transparent"
                        });
                    } else {
                        if (bBB) {
                            //get the bounding box for the area
                            $scope.studyBounds.extend(layer.getBounds());
                        }
                        layer.setStyle({
                            weight: 1,
                            color: "gray",
                            fillColor: function () {
                                //colour study and comparison areas differently
                                if ($scope.area.name === "study") {
                                    return "blue";
                                } else {
                                    return "purple";
                                }
                            }(),
                            fillOpacity: $scope.transparency["exportmap"]
                        });
                    }
                };
            }]);