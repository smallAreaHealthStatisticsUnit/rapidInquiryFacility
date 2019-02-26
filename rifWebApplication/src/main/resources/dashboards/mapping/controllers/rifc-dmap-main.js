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
 * CONTROLLER for disease mapping and viewer (shared)
 */

/* global L, key, topojson, d3 */

angular.module("RIF")
        .controller('MappingCtrl', ['$scope', 'user', '$timeout', 'MappingStateService', 'ChoroService', 'MappingService', 'mapTools', 
			'ParametersService', 'D3ChartsService',
            function ($scope, user, $timeout, MappingStateService, ChoroService, MappingService, mapTools, 
				ParametersService, D3ChartsService) {

                //Reference the child scope (controller is embedded)
                //child scope will be on either the mapping or viewer dashboards
                //controller instance recreated in each case
                $scope.child = {};

                //A flag to keep renderers if changing tabs
                $scope.$on("$destroy", function () {
                    MappingStateService.getState().initial = true;
                    $scope.child.map['diseasemap1'].remove();
                    $scope.child.map['diseasemap2'].remove();
                });

				$scope.parameters=ParametersService.getParameters()||{
					syncMapping2EventsDisabled: false,			// Disable syncMapping2Events handler (leak debugging)
					disableMapLocking: false,					// Disable disease map initial sync 
					disableSelectionLocking: false				// Disable selection locking
				};
				$scope.syncMapping2EventsDisabled=$scope.parameters.syncMapping2EventsDisabled||false;
				$scope.disableMapLocking=$scope.parameters.disableMapLocking||false;		
				$scope.disableSelectionLocking=$scope.parameters.disableSelectionLocking||false;		
                $scope.studyType = {
                    "diseasemap1": "",
                    "diseasemap2": ""
				};
				$scope.thisPolygon = {
                    "diseasemap1": "",
                    "diseasemap2": ""
				};				
				
                //Transparency change function
                function closureAddSliderControl(m) {
                    return function (v) {
                        MappingStateService.getState().transparency[m] = v;
                        $scope.transparency[m] = v;
                        if (angular.isDefined($scope.child.geoJSON[m]._geojsons)) {
                            $scope.child.geoJSON[m]._geojsons.default.eachLayer($scope.child.handleLayer);
                        }
                    };
                }

                //Set initial map panels and events
                $timeout(function () {
                    //make maps
                    for (var map in $scope.child.myMaps) {
						if ($scope.child.myMaps[map] && typeof $scope.child.myMaps[map] != 'function') {
							//initialise map
							var container = angular.copy($scope.child.myMaps[map]);
							$scope.consoleDebug("[rifc-dmap-main.js] Create map: " + container);
							$scope.child.map[container] = L.map(container, {condensedAttributionControl: false}).setView([0, 0], 1);

							//search box
							new L.Control.GeoSearch({
								provider: new L.GeoSearch.Provider.OpenStreetMap()
							}).addTo($scope.child.map[container]);

							//full screen control             
							$scope.child.map[container].addControl(new L.Control.Fullscreen());
							$scope.child.map[container].on('fullscreenchange', function () {
								setTimeout(function () {
									$scope.child.map[container].invalidateSize();
								}, 50);
							});

							//slider
							var slider = L.control.slider(closureAddSliderControl(container), {
								id: slider,
								position: 'topleft',
								orientation: 'horizontal',
								min: 0,
								max: 1,
								step: 0.01,
								value: MappingStateService.getState().transparency[container],
								title: 'Transparency',
								logo: '',
								syncSlider: true

							}).addTo($scope.child.map[container]);

							//left map only tools (the linking controls)
							if (container === "diseasemap1") {
								var tools = mapTools.getExtraTools($scope.child);
								for (var i = 0; i < tools.length; i++) {
									new tools[i]().addTo($scope.child.map[container]);
								}
							}
							//Custom Toolbar
							var tools = mapTools.getBasicTools($scope.child, container);
							for (var i = 0; i < tools.length; i++) {
								new tools[i]().addTo($scope.child.map[container]);
							}

							//scalebar
							L.control.scale({position: 'bottomleft', imperial: false}).addTo($scope.child.map[container]);

							//Attributions to open in new window
							L.control.condensedAttribution({
								prefix: '<a href="http://leafletjs.com" target="_blank">Leaflet</a>'
							}).addTo($scope.child.map[container]);
							
							$scope.child.map[container].doubleClickZoom.disable();
							$scope.child.map[container].keyboard.disable();

							setTimeout(function () {
								if ($scope.child.map[container] && typeof $scope.child.map[container].invalidateSize === 'function') {
									$scope.child.map[container].invalidateSize();
								}
							}, 50);
						}
						else {
							$scope.consoleLog("[rifc-dmap-main.js] Unable to create map, the container is invalid");
						}
					}

                    //Fill study drop-downs
                    $scope.child.getStudies();
                    $scope.child.renderMap("diseasemap1");
                    $scope.child.renderMap("diseasemap2");
                });

                //ui-container sizes
                $scope.currentWidth1 = 100;
                $scope.currentHeight1 = 100;
                $scope.currentWidth2 = 100;
                $scope.currentHeight2 = 100;
                $scope.vSplit1 = MappingStateService.getState().vSplit1;
                $scope.hSplit1 = MappingStateService.getState().hSplit1;
                $scope.hSplit2 = MappingStateService.getState().hSplit2;

                $scope.getD3Frames = function () {
                    $scope.currentWidth1 = d3.select("#rr1").node().getBoundingClientRect().width;
                    $scope.currentHeight1 = d3.select("#rr1").node().getBoundingClientRect().height;
                    $scope.currentWidth2 = d3.select("#rr2").node().getBoundingClientRect().width;
                    $scope.currentHeight2 = d3.select("#rr2").node().getBoundingClientRect().height;
                    $scope.riskGraphData3['diseasemap1'].width = d3.select("#rr1").node().getBoundingClientRect().width;
                    $scope.riskGraphData3['diseasemap1'].height = d3.select("#rr1").node().getBoundingClientRect().height;
                    $scope.riskGraphData3['diseasemap2'].width = d3.select("#rr2").node().getBoundingClientRect().width;
                    $scope.riskGraphData3['diseasemap2'].height = d3.select("#rr2").node().getBoundingClientRect().height;
                };

                $scope.getD3FramesOnResize = function (beforeContainer, afterContainer) {
                    if (beforeContainer.id === "hSplit1") {
                        MappingStateService.getState().hSplit1 = (beforeContainer.size / beforeContainer.maxSize) * 100;
                        $scope.currentHeight1 = d3.select("#rr1").node().getBoundingClientRect().height;
						$scope.riskGraphData3['diseasemap1'].height = d3.select("#rr1").node().getBoundingClientRect().height;
                    }
                    if (beforeContainer.id === "vSplit1") {
                        MappingStateService.getState().vSplit1 = (beforeContainer.size / beforeContainer.maxSize) * 100;
                        $scope.currentWidth1 = d3.select("#rr1").node().getBoundingClientRect().width;
                        $scope.currentWidth2 = d3.select("#rr2").node().getBoundingClientRect().width;
						$scope.riskGraphData3['diseasemap1'].width = d3.select("#rr1").node().getBoundingClientRect().width;
						$scope.riskGraphData3['diseasemap2'].width = d3.select("#rr2").node().getBoundingClientRect().width;
                    }
                    if (beforeContainer.id === "hSplit2") {
                        MappingStateService.getState().hSplit2 = (beforeContainer.size / beforeContainer.maxSize) * 100;
                        $scope.currentHeight2 = d3.select("#rr2").node().getBoundingClientRect().height;
						$scope.riskGraphData3['diseasemap2'].height = d3.select("#rr2").node().getBoundingClientRect().height;
                    }
					
					$scope.$broadcast('rrZoomReset', {msg: "watchCall reset: resize"}); // Restart watcher
                };

                /*
                 * Local map variables
                 */
                $scope.myMaps = ["diseasemap1", "diseasemap2"];

                //transparency as set by slider
                $scope.transparency = {
                    "diseasemap1": MappingStateService.getState().transparency["diseasemap1"],
                    "diseasemap2": MappingStateService.getState().transparency["diseasemap2"]
                };

                //selected polygons
                $scope.thisPoly = {
                    'diseasemap1': MappingStateService.getState().area_id["diseasemap1"],
                    'diseasemap2': MappingStateService.getState().area_id["diseasemap2"]
                };

                //study to be mapped
                $scope.studyID = {
                    "diseasemap1": MappingStateService.getState().study['diseasemap1'],
                    "diseasemap2": MappingStateService.getState().study['diseasemap2']
                };
                $scope.sex = {
                    "diseasemap1": MappingStateService.getState().sex['diseasemap1'],
                    "diseasemap2": MappingStateService.getState().sex['diseasemap2']
                };

                //attributes for d3
                $scope.rrChartData = {
                    "diseasemap1": [],
                    "diseasemap2": []
                };

                //options for d3 directives
                $scope.optionsRR = {
                    "diseasemap1": {
                        this: "optionsRR1",
                        panel: "diseasemap1",
                        x_field: "x_order",
                        risk_field: "rr",
                        cl_field: "cl",
                        cu_field: "ul",
                        label_field: "",
                        zoomStart: MappingStateService.getState().brushStartLoc['diseasemap1'],
                        zoomEnd: MappingStateService.getState().brushEndLoc['diseasemap1']
                    },
                    "diseasemap2": {
                        this: "optionsRR2",
                        panel: "diseasemap2",
                        x_field: "x_order",
                        risk_field: "rr",
                        cl_field: "cl",
                        cu_field: "ul",
                        label_field: "",
                        zoomStart: MappingStateService.getState().brushStartLoc['diseasemap2'],
                        zoomEnd: MappingStateService.getState().brushEndLoc['diseasemap2']
                    }
                };
                $scope.optionsd3 = {
                    "diseasemap1": {
                        container: "rrchart",
                        element: "#rr1",
                        filename: "risk1.png"
                    },
                    "diseasemap2": {
                        container: "rrchart",
                        element: "#rr1",
                        filename: "risk2.png"
                    },
                    "riskGraph3": {
                        container: "riskGraph3",
                        element: "#rr1",
                        filename: "riskGraph4.png"
                    },
                    "riskGraph4": {
                        container: "riskGraph4",
                        element: "#rr1",
                        filename: "riskGraph4.png"
                    }
                };
                $scope.isDiseaseMappingStudy = {
					"diseasemap1": true,
					"diseasemap2": true
				};
				
                $scope.riskGraphData3={
                    diseasemap1: {
						riskGraphData: undefined,
						mapID: "diseasemap1",
						hSplitTag: "hSplit2",
						studyID: -1,
                        name: "riskGraph",
                        width: 150,
                        height: 150,
						gotCount: 1,
                        gendersArray: ['males', 'females'],
                        riskFactor: 'band',
                        riskFactor2FieldName: {
                            'average exposure': 'avgExposureValue', 
                            'band': 'bandId', 
                            'average distance from nearest source': 'avgDistanceFromNearestSource'
                        }
                    },
                    diseasemap2: {
						riskGraphData: undefined,
						mapID: "diseasemap2",
						hSplitTag: "hSplit2",
						studyID: -1,
                        name: "riskGraph",
                        width: 150,
                        height: 150,
						gotCount: 1,
                        gendersArray: ['males', 'females'],
                        riskFactor: 'band',
                        riskFactor2FieldName: {
                            'average exposure': 'avgExposureValue', 
                            'band': 'bandId', 
                            'average distance from nearest source': 'avgDistanceFromNearestSource'
                        }
                    }
                };

                /*
                 * D3
                 */
                //draw rr chart from d3 directive 'rrZoom'
                $scope.getD3chart = function (mapID, attribute) {
					
					$scope.consoleDebug("[rifc-dmap-main.js] getD3chart, map: " + mapID + "; study type: " + $scope.studyType[mapID] +
						"; isDiseaseMappingStudy: " + $scope.isDiseaseMappingStudy[mapID] +
						"; attribute: " + attribute + "; data rows: " + $scope.child.tableData[mapID].length);
                    //reset brush handles        
                    MappingStateService.getState().brushEndLoc[mapID] = null;
                    MappingStateService.getState().brushStartLoc[mapID] = null;
                    $scope.optionsRR[mapID].zoomStart = null;
                    $scope.optionsRR[mapID].zoomEnd = null;

                    //make array for d3 areas
                    var rs = [];
					if ($scope.child.tableData[mapID].length == 0) {
						$scope.showWarning("Unable to create D3 chart for map: " + mapID + "; no table data");
					}
					
                    for (var i = 0; i < $scope.child.tableData[mapID].length; i++) {
                        //check for invalid column, therefore no graph possible
                        if ($scope.child.tableData[mapID][i][attribute] === undefined) {
                            $scope.rrChartData[mapID] = [];
							$scope.showError("Unable to create relative risk chart for map: " + mapID + "; attribute column: " + attribute + " not found");
                            return;
                        }                       
                        //Handle inconsistant naming in results table
                        //Handle if there are no confidence intervals
                        var ciString = ["lower95", "upper95"];
                        if (attribute === "smoothed_smr") {
                            ciString = ["smoothed_smr_lower95", "smoothed_smr_upper95"];
                        }
                        rs.push(
                                {
                                    name: attribute,
                                    gid: $scope.child.tableData[mapID][i].area_id,
                                    x_order: i,
                                    rr: $scope.child.tableData[mapID][i][attribute],
                                    cl: function () {
                                        if (attribute !== "posterior_probability") {
                                            return $scope.child.tableData[mapID][i][ciString[0]];
                                        } else {
                                            return $scope.child.tableData[mapID][i][attribute];
                                        }
                                    }(),
                                    ul: function () {
                                        if (attribute !== "posterior_probability") {
                                            return $scope.child.tableData[mapID][i][ciString[1]];
                                        } else {
                                            return $scope.child.tableData[mapID][i][attribute];
                                        }
                                    }()
                                }
                        );
                    } // End of for loop

		//			$scope.consoleDebug("[rifc-dmap-main.js] getD3chart, map: " + mapID + 
		//				"; rs[0]: " + JSON.stringify(rs[0], null, 2) + 
		//				"; tableData[0]: " + JSON.stringify($scope.child.tableData[mapID][0], null, 2));
                    //reorder
                    rs.sort(function (a, b) {
                        return parseFloat(a.rr) - parseFloat(b.rr);
                    });
					
                    for (var i = 0; i < $scope.child.tableData[mapID].length; i++) {
                        rs[i]["x_order"] = i + 1;
                    }
					
                    //set options for directive
                    $scope.optionsRR[mapID].label_field = attribute;
                    $scope.rrChartData[mapID] = angular.copy(rs); // Copy data to scoope of D3 chart
					$scope.consoleDebug("[rifc-dmap-main.js] getD3chart, map: " + mapID + 
						"; $scope.rrChartData[mapID][0]: " + JSON.stringify($scope.rrChartData[mapID][0], null, 0) + 
						"; length: " + $scope.rrChartData[mapID].length);

					//get risk graph data
					if (!$scope.isDiseaseMappingStudy[mapID]) {
						user.getRiskGraph(user.currentUser, $scope.studyID[mapID].study_id).then(function (res) {
							if (res && res.data && Object.keys(res.data).length > 0 && 
								$scope.riskGraphData3[mapID].riskFactor2FieldName) {
								var selector=D3ChartsService.setupRiskGraphSelector(
									res.data, $scope.riskGraphData3[mapID].riskFactor2FieldName);
								var newRiskGraphData3 = angular.copy($scope.riskGraphData3[mapID]);
								for (var key in selector) { // Copy to scope
									newRiskGraphData3[key]=angular.copy(selector[key]);
								}
								newRiskGraphData3.riskFactorFieldName=newRiskGraphData3.riskFactor2FieldName[selector.riskFactor];
                                newRiskGraphData3.riskFactorFieldDesc=selector.riskFactor;
								newRiskGraphData3.riskGraphData=res.data;    
								newRiskGraphData3.studyID = $scope.studyID[mapID].study_id;
								newRiskGraphData3.width=$scope.riskGraphData3[mapID].width;
								newRiskGraphData3.height=$scope.riskGraphData3[mapID].height;
								$scope.riskGraphData3[mapID].gotCount++;
								newRiskGraphData3.gotCount=$scope.riskGraphData3[mapID].gotCount;
								
								$scope.riskGraphData3[mapID] = angular.copy(newRiskGraphData3);								
								$scope.consoleDebug("[rifc-dmap-main.js] d3RisKGraph map: " + mapID + 
									"; study: " + $scope.studyID[mapID].study_id +
									"; got risk graph data: " + JSON.stringify($scope.riskGraphData3[mapID], 0, 0) + 
									"; selector: " + JSON.stringify(selector, 0, 1));
									
								$scope.$broadcast('rrZoomReset', {msg: "watchCall reset: " + mapID});  // Restart watcher
							}
							else {
								throw new Error("[rifc-dmap-main.js] no risk graph data error for field: " +
									($scope.riskGraphData3[mapID].riskFactor2FieldName ? 
										$scope.riskGraphData3[mapID].riskFactor2FieldName : "Not defined"));
							}
						}, function () {
							$scope.showError("[rifc-dmap-main.js] get risk graph data error");
						});
					}						
                }; // End of getD3chart()

                //key events to move the dropline
                $scope.child.mapInFocus = "";
                angular.element(document).bind('keydown', function (e) {
                    if ($scope.mapInFocus !== "") {
                        if (e.keyCode === 37 || e.keyCode === 39) { //left || right
                            $scope.$broadcast('rrKeyEvent', false, e.keyCode, $scope.child.mapInFocus);
                        }
                    }
                });
                angular.element(document).bind('keyup', function (e) {
                    if ($scope.mapInFocus !== "") {
                        if (e.keyCode === 37 || e.keyCode === 39) { //left || right
                            $scope.$broadcast('rrKeyEvent', true, e.keyCode, $scope.child.mapInFocus);
                        }
                    }
                });
				if ($scope.disableMapLocking) {
					MappingStateService.getState().extentLock = false;
				}
				else {
					MappingStateService.getState().extentLock = true;
				}
				if ($scope.disableSelectionLocking) {
					MappingStateService.getState().selectionLock = false;
				}
				else {
					MappingStateService.getState().selectionLock = true;
				}
                /*
                 * Specific to handle 2 Leaflet Panels in disease mapping
                 */
                //sync map extents
                $scope.bLockCenters = MappingStateService.getState().extentLock;
                $scope.bLockSelect = MappingStateService.getState().selectionLock;
				$scope.consoleLog("[rifc-dmap-main.js] Map selection lock; $scope.bLockSelect: " + $scope.bLockSelect);
				$scope.consoleLog("[rifc-dmap-main.js] Map linking; $scope.bLockCenters: " + $scope.bLockCenters);
				
                $scope.lockExtent = function () {
                    if ($scope.bLockCenters) {
                        $scope.bLockCenters = false;
                        MappingStateService.getState().extentLock = false;
                    } else {
                        $scope.bLockCenters = true;
                        MappingStateService.getState().extentLock = true;
                    }
                    $scope.child.mapLocking();
                };

                //sync map selections
				
                $scope.lockSelect = function () {
                    if ($scope.bLockSelect) {
                        $scope.bLockSelect = false;
                        MappingStateService.getState().selectionLock = false;
                    } else {
                        //Check if geographies and levels match:  "SAHSU" === "SAHSU"
                        var g1 = $scope.child.tileInfo["diseasemap1"];
                        var g2 = $scope.child.tileInfo["diseasemap2"];
                        if (g1.geography !== g2.geography) {
                            //different geographies     
                            $scope.showWarning("Cannot link selections for different geographies: " + g1.geography + " & " + g2.geography);
                        } else {
                            if (g1.level !== g2.level) {
                                //different levels
                                $scope.showWarning("Cannot link selections for different geolevels: " + g1.level + " & " + g2.level);
                            } else {
                                $scope.bLockSelect = true;
                                MappingStateService.getState().selectionLock = true;
                            }
                        }
                    }
                };

                //transfer current symbology on left map on the right map as well
                $scope.copySymbology = function () {
                    if (angular.isDefined($scope.studyID["diseasemap1"])) {
                        ChoroService.getMaps("diseasemap2").renderer = angular.copy(ChoroService.getMaps("diseasemap1").renderer);
                        ChoroService.getMaps("diseasemap2").brewerName = angular.copy(ChoroService.getMaps("diseasemap1").brewerName);
                        ChoroService.getMaps("diseasemap2").intervals = angular.copy(ChoroService.getMaps("diseasemap1").intervals);
                        ChoroService.getMaps("diseasemap2").invert = angular.copy(ChoroService.getMaps("diseasemap1").invert);
                        ChoroService.getMaps("diseasemap2").method = angular.copy(ChoroService.getMaps("diseasemap1").method);
                        ChoroService.getMaps("diseasemap2").feature = angular.copy(ChoroService.getMaps("diseasemap1").feature);
                        $scope.child.refresh("diseasemap2");
                    }
                };

                /*
                 * Watch for Changes
                 */
                $scope.updateMapSelection = function (data, mapID) {
                    dropLine(mapID, data, true);
                };
                $scope.$on('syncMapping2Events', function (event, data) {
                    if (!$scope.syncMapping2EventsDisabled && data.selected !== null && !angular.isUndefined(data.selected)) {
						$scope.consoleDebug("[rifc-dmap-main.js] on syncMapping2Events, map: " + data.mapID + "; gid: " + data.selected.gid);
                        dropLine(data.mapID, data.selected.gid, data.map);
                        MappingStateService.getState().area_id[data.mapID] = data.selected.gid;
                        //update the other map if selections locked
                        if ($scope.bLockSelect) {
                            var otherMap = MappingService.getOtherMap(data.mapID);
                            $scope.thisPoly[otherMap] = data.selected.gid;
                            MappingStateService.getState().area_id[otherMap] = data.selected.gid;
                            dropLine(otherMap, data.selected.gid, false);
                            $scope.child.infoBox2[otherMap].update($scope.thisPoly[otherMap]);
                        }
                    }
                });

                //draw the drop line in d3 and poly on map
                function dropLine(mapID, gid, map) {
                    //update map
                    if (map) {
                        $scope.thisPoly[mapID] = gid;
                        $scope.child.geoJSON[mapID]._geojsons.default.eachLayer($scope.child.handleLayer);
                        $scope.child.infoBox2[mapID].update($scope.thisPoly[mapID]);
                        if ($scope.bLockSelect) {
                            var otherMap = MappingService.getOtherMap(mapID);
                            $scope.thisPoly[otherMap] = gid;
                            if (!angular.isUndefined($scope.child.geoJSON[otherMap])) {
                                $scope.child.geoJSON[otherMap]._geojsons.default.eachLayer($scope.child.handleLayer);
                            }
                        }
                    }
                    //update chart
					
					$scope.consoleDebug("[rifc-dmap-main.js] broadcast rrDropLineRedraw, map: " + mapID + "; gid: " + gid);
                    $scope.$broadcast('rrDropLineRedraw', gid, mapID);
                }
            }]);         