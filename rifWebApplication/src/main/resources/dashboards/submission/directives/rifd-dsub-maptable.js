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
 *
 * David Morley
 * Original @author dmorley
 *
 * Re-structured to use promises chains throughout and tuned for UK COA 220,000 areas maps
 * Peter Hambly
 * @author phambly
 */

/*
 * DIRECTIVE for map and table linked area selections
 * TODO: This prob needs refactoring / overhauling to fit in with the mapping controllers
 * although it does work fine as it is
 */

/* global L, d3, key, topojson */
angular.module("RIF")
        .directive('submissionMapTable', ['ModalAreaService', 'LeafletDrawService', '$uibModal', 'JSONService', 'mapTools',
            'LeafletBaseMapService', '$timeout', 'user', 'SubmissionStateService', 
			'SelectStateService', 'ParametersService', 'StudyAreaStateService', 'CompAreaStateService', 'CommonMappingStateService', 
			'DrawSelectionService', '$q', '$timeout',
            function (ModalAreaService, LeafletDrawService, $uibModal, JSONService, mapTools,
                    LeafletBaseMapService, $timeout, user, SubmissionStateService,
					SelectStateService, ParametersService, StudyAreaStateService, CompAreaStateService, CommonMappingStateService,
					DrawSelectionService, $q, $timeout) {
                return {
                    templateUrl: 'dashboards/submission/partials/rifp-dsub-maptable.html',
                    restrict: 'AE',
                    link: function ($scope) {
						var parameters=ParametersService.getParameters();
					    var selectorBands = { // Study and comparison are selectors
								weight: 3,
								opacity: 0.8,
								fillOpacity: 0,
								bandColours: ['#e41a1c', '#377eb8', '#4daf4a', '#984ea3', '#ff7f00', '#ffff33']
							};
						if (parameters && parameters.selectorBands) {
							selectorBands=parameters.selectorBands
						}	
						var disableMouseClicksAt = 5000;
						if (parameters && parameters.disableMouseClicksAt) {
							disableMouseClicksAt=parameters.disableMouseClicksAt
						}							
						$scope.centroid_type="UNKNOWN";
						$scope.noMouseClocks=false;						
//						$scope.geoJSONLayers = [];
						$scope.selectionData = [];
						
						CommonMappingStateService.getState("areamap").map = // Initialise if required
							L.map("areamap", {condensedAttributionControl: false}).setView([0, 0], 1);	
                        $scope.areamap = CommonMappingStateService.getState("areamap").map;
						
						var shapesPane = CommonMappingStateService.getState("areamap").map.createPane('shapes');
						CommonMappingStateService.getState("areamap").map.getPane('shapes').style.zIndex = 650; // set shapes to show on top of markers but below pop-ups					
										
						SubmissionStateService.setAreaMap(CommonMappingStateService.getState("areamap").map);
						$scope.bShowHideSelectionShapes=(SelectStateService.getState().showHideSelectionShapes || true);
						$scope.bShowHideCentroids=SelectStateService.getState().showHideCentroids;
                        $scope.thisLayer = LeafletBaseMapService.setBaseMap(LeafletBaseMapService.getCurrentBaseMapInUse("areamap"));

                        //Reference the child scope
                        //will be from the comparison area or study area controller
                        $scope.child = {};
                        var alertScope = $scope.$parent.$$childHead.$parent.$parent.$$childHead;
						CommonMappingStateService.getState("areamap").map.on('remove', function(e) {
                            alertScope.consoleDebug("[rifd-dsub-maptable.js] removed shared areamap");
						});
						CommonMappingStateService.getState("areamap").map.on('error', function(errorEvent){
                            alertScope.consoleError("[rifd-dsub-maptable.js] error in areamap" +
								(errorEvent.message || "(no message)"));
						});	

                        /*
                         * LOCAL VARIABLES
                         */
                        //map max bounds from topojson layer
                        //If geog changed then clear selected
                        var thisGeography = SubmissionStateService.getState().geography;
                        if (thisGeography !== $scope.input.geography) {
							alertScope.consoleLog("[rifd-dsub-maptable.js] Geography change: clear $scope.input.selectedPolygon");
                            $scope.input.selectedPolygon.length = 0;
                            $scope.input.selectAt = "";
                            $scope.input.studyResolution = "";
                            $scope.input.geography = thisGeography;
                        }
						
						// Also defined in rifs-util-leafletdraw.js

                        //selectedPolygon array synchronises the map <-> table selections 
						var selectedPolygon = angular.copy($scope.input.selectedPolygon); // Copy to prevent going out of scope						
						alertScope.consoleLog("[rifd-dsub-maptable.js] initialiseSelectedPolygon(" + $scope.input.name + ") $scope.input.selectedPolygon: " +
							selectedPolygon.length);                  
                        CommonMappingStateService.getState("areamap").initialiseSelectedPolygon($scope.input.name, selectedPolygon);       
						$scope.selectedPolygon = CommonMappingStateService.getState("areamap").sortSelectedPolygon($scope.input.name);		
                        $scope.selectedPolygonCount = $scope.selectedPolygon.length; //total for display
						
						$scope.shapeLoadUpdate = "";
                        //band colour look-up for selected districts
                        CommonMappingStateService.getState("areamap").possibleBands = $scope.input.bands;
                        CommonMappingStateService.getState("areamap").currentBand = 1; //from dropdown
						$scope.possibleBands=CommonMappingStateService.getState("areamap").possibleBands;
						$scope.currentBand=CommonMappingStateService.getState("areamap").currentBand;
                        //d3 polygon rendering, changed by slider
                        $scope.transparency = $scope.input.transparency;
						$scope.geoJSONLoadCount = 0;
						
                        $scope.latlngListById = []; // centroids!
						
						var eachFeatureArray = [];
                        var bWeightedCentres = true;
						var popWeightedCount=0;
						var dbCentroidCount=0;

						var start=new Date().getTime();	
									
                        /*
                         * TOOL STRIP 
                         * These repeat stuff in the leafletTools directive - possible refactor
                         */

                        $scope.geoLevelChange = function () {
							alertScope.consoleLog("[rifd-dsub-maptable.js] $scope.geoLevelChange() clear $scope.input.selectedPolygon");
                            //Clear the map
							$scope.clear();

                            user.getGeoLevelViews(user.currentUser, thisGeography, $scope.input.selectAt).then(handleGeoLevelViews, handleGeographyError);
                        };
						
						/* Function: 	setStudyType()
						 * Description: Reset study state to $scope.input.type
						 */
						function setStudyType(callCount) {

							if (callCount > 2) {
								throw new Error("setStudyType() recursion protector: " + callCount);
							}
							
							if ($scope.input.type === "Risk Analysis" && 
							    SelectStateService.getState().studyType != "Risk Analysis") {
								SelectStateService.initialiseRiskAnalysis();
								SelectStateService.getState().studyType="risk_analysis_study";
								
                                CommonMappingStateService.getState("areamap").possibleBands = [1, 2, 3, 4, 5, 6];
                                CommonMappingStateService.getState("areamap").map.band = 6;
															
								SubmissionStateService.getState().studyType = $scope.input.type;
								StudyAreaStateService.getState().setType($scope.input.type);		
								CompAreaStateService.getState().type = $scope.input.type;		
								alertScope.consoleLog("[rifd-dsub-maptable.js] setStudyType(RA): " + 
									SubmissionStateService.getState().studyType);	

							}
							else if ($scope.input.type === "Disease Mapping" && 
							    SelectStateService.getState().studyType != "Disease Mapping") {			
								SelectStateService.resetState();
								SelectStateService.getState().studyType="disease_mapping_study";
								
                                CommonMappingStateService.getState("areamap").possibleBands = [1];
                                CommonMappingStateService.getState("areamap").currentBand = 1;
                                CommonMappingStateService.getState("areamap").map.band = 1;

								SubmissionStateService.getState().studyType = $scope.input.type;
								StudyAreaStateService.getState().setType($scope.input.type);		
								CompAreaStateService.getState().type = $scope.input.type;		
								alertScope.consoleLog("[rifd-dsub-maptable.js] setStudyType(DM): " + 
									SubmissionStateService.getState().studyType);	

							}					
							else if ($scope.input.type === SelectStateService.getState().studyType) { // No change	
								alertScope.consoleLog("[rifd-dsub-maptable.js] setStudyType() No change: " +
									SelectStateService.getState().studyType);
							}								
							else if ($scope.input.type == undefined) {
								if (SubmissionStateService.getState().studyType != undefined &&
								    StudyAreaStateService.getState().type       != undefined &&
								    SubmissionStateService.getState().studyType === 
											StudyAreaStateService.getState().type &&
									$scope.input.name == "ComparisionAreaMap"	/* In Comparison area modal */) {
									$scope.input.type=StudyAreaStateService.getState().type;
//									setStudyType((callCount + 1));
								}
								else {
									throw new Error("setStudyType: undefined $scope.input.type");
								}		
								alertScope.consoleLog("[rifd-dsub-maptable.js] setStudyType() No change, set ComparisionAreaMap $scope.input.type: " +
									SelectStateService.getState().studyType);
							}
							else {
								throw new Error("setStudyType: invalid $scope.input.type: " + $scope.input.type);
							}
										
						}
							
                        //Clear all selection from map and table
                        $scope.clear = function () {
							alertScope.consoleLog("[rifd-dsub-maptable.js] $scope.clear() $scope.input.selectedPolygon");
                            $scope.input.selectedPolygon.length = 0;
							$scope.selectedPolygonCount = 0;
                            $scope.selectedPolygon = CommonMappingStateService.getState("areamap").clearSelectedPolygon($scope.input.name);
							$scope.shapeLoadUpdate = "";
							if (!$scope.geoJSON) {
							} else if ($scope.geoJSON && $scope.geoJSON._geojsons && $scope.geoJSON._geojsons.default) {
								//Update map selection    
								$scope.geoJSON._geojsons.default.eachLayer(handleLayer);
							}
							
							setStudyType(1);
							
                            if (CommonMappingStateService.getState("areamap").map.hasLayer(	// Reset shapes
									CommonMappingStateService.getState("areamap").shapes)) {
                                CommonMappingStateService.getState("areamap").map.removeLayer(
									CommonMappingStateService.getState("areamap").shapes);
								CommonMappingStateService.getState("areamap").shapes = new L.layerGroup();
								CommonMappingStateService.getState("areamap").map.addLayer(
									CommonMappingStateService.getState("areamap").shapes);
								
								CommonMappingStateService.getState("areamap").info.update();
                            }
                            if (CommonMappingStateService.getState("areamap").map.hasLayer(centroidMarkers)) { // Remove centroids
								$scope.bShowHideCentroids = false;
								SelectStateService.getState().showHideCentroids = false;
                                CommonMappingStateService.getState("areamap").map.removeLayer(centroidMarkers);
                            }
							
							if (CommonMappingStateService.getState("areamap").maxbounds) { //  Zoom back to maximum extent of geolevel
								CommonMappingStateService.getState("areamap").map.fitBounds(
									CommonMappingStateService.getState("areamap").maxbounds);
							}
                        };
						
						// Bring shapes to front by descending band order; lowest in front (so mouseover/mouseout works!)
						$scope.bringShapesToFront = function() {
							var layerCount=0;
							var maxBands=0;
							var shapeLayerOptionsBanderror=0;
							var shapeLayerBringToFrontError=0;
							
							if (CommonMappingStateService.getState("areamap").shapes) {
								var shapesLayerList=CommonMappingStateService.getState("areamap").shapes.getLayers();
								var shapesLayerBands = {};
								var shapesLayerAreas = {};
								var useBands=false;
								
								for (var i=0; i<shapesLayerList.length; i++) {
									var shapeLayer=shapesLayerList[i];
									if (shapeLayer.options.icon) { // Factory icon - ignore
									}										
									else if (shapeLayer.options.band == undefined) {	
										alertScope.consoleLog("[rifd-dsub-maptable.js] cannot resolve shapesLayerList[" + i + 
											 "].options.band/area; options: " + JSON.stringify(shapeLayer.options));
										shapeLayerOptionsBanderror++;
									}
									else {
										if (shapesLayerBands[shapeLayer.options.band] == undefined) {
											shapesLayerBands[shapeLayer.options.band] = [];
										}
										
										if (shapeLayer.options.area) {
											if (shapesLayerAreas[shapeLayer.options.area] == undefined) {
												shapesLayerAreas[shapeLayer.options.area] = [];
											}
											shapesLayerAreas[shapeLayer.options.area].push(CommonMappingStateService.getState("areamap").shapes.getLayerId(shapeLayer));
										}
										else {
											useBands=true;
										}
										shapesLayerBands[shapeLayer.options.band].push(CommonMappingStateService.getState("areamap").shapes.getLayerId(shapeLayer));
										if (maxBands < shapeLayer.options.band) {
											maxBands=shapeLayer.options.band;
										}
										layerCount++;
									}
								}

								if (!useBands) { // Use areas - all present
	
									shapesLayerAreaList=Object.keys(shapesLayerAreas); 
									shapesLayerAreaList.sort(function(a, b){return b - a}); 
									// Sort into descended list so the smallest areas are in front
									alertScope.consoleDebug("[rifd-dsub-maptable.js] sorted shape areas: " + shapesLayerAreaList.length + 
										"; " + JSON.stringify(shapesLayerAreaList));
									
									var areaNameList=CommonMappingStateService.getState("areamap").getAreaNameList(
										$scope.input.name);
									alertScope.consoleLog("[rifd-dsub-maptable.js] getAreaNameList() for: " + 
										$scope.input.name + 
										"; areaNameList: " + (areaNameList ? Object.keys(areaNameList).length : "0"));
										
									for (var k=0; k<shapesLayerAreaList.length; k++) {
																									
										for (var area in shapesLayerAreas) {
											if (area == shapesLayerAreaList[k]) {
												var areaIdList=shapesLayerAreas[area];
												for (var l=0; l<areaIdList.length; l++) {
													var shapeLayer=CommonMappingStateService.getState("areamap").shapes.getLayer(areaIdList[l]);
													if (shapeLayer && typeof shapeLayer.bringToFront === "function") { 

														if (areaNameList == undefined || 
														    (Object.keys(areaNameList).length === 0 && areaNameList.constructor === Object)) {
															alertScope.consoleDebug("[rifd-dsub-maptable.js] bring layer: " + areaIdList[l] + " to front" +
																"; band: " + shapeLayer.options.band +
																"; area: " + shapeLayer.options.area +
																"; polygons: unknown");
															shapeLayer.bringToFront();
														}
														else if (shapeLayer.options.band && 
															areaNameList && areaNameList[shapeLayer.options.band] &&
														    areaNameList[shapeLayer.options.band].length > 0) {
															alertScope.consoleDebug("[rifd-dsub-maptable.js] bring layer: " + areaIdList[l] + " to front" +
																"; band: " + shapeLayer.options.band +
																"; area: " + shapeLayer.options.area +
																"; polygons: " + areaNameList[shapeLayer.options.band].length);
															shapeLayer.bringToFront();
														}
														else {
															alertScope.consoleDebug("[rifd-dsub-maptable.js] ignore layer: " + areaIdList[l] + " to front" +
																"; band: " + shapeLayer.options.band +
																"; area: " + shapeLayer.options.area +
																"; areaNameList: " + JSON.stringify(areaNameList) +
																"; no polygons");
														}
													}
													else {		
														shapeLayerBringToFrontError++;
														alertScope.consoleLog("[rifd-dsub-maptable.js] cannot resolve shapesLayerAreas[" + area + 
															"][" + l + "].bringToFront()");
													}
												}
											}
										}
									}
								}
								else { // Use bands
									
									for (var j=maxBands; j>0; j--) { 
										alertScope.consoleDebug("[rifd-dsub-maptable.js] band: " + j + "/" + maxBands + 
											"; areas: "  + Object.keys(shapesLayerAreas).length +
											"; bands: " + Object.keys(shapesLayerBands).length + 
											"; layers: " + shapesLayerBands[j].length + "; ids: " + JSON.stringify(shapesLayerBands[j]));
										for (var k=0; k<shapesLayerBands[j].length; k++) {
											var shapeLayer=CommonMappingStateService.getState("areamap").shapes.getLayer(shapesLayerBands[j][k]);
											if (shapeLayer && typeof shapeLayer.bringToFront === "function") { 
												shapeLayer.bringToFront();
											}
											else {		
												shapeLayerBringToFrontError++;
												alertScope.consoleLog("[rifd-dsub-maptable.js] cannot resolve shapesLayerBands[" + j + 
													"][" + k + "].bringToFront()");
											}
										}
									}
								} 
								
								alertScope.consoleDebug("[rifd-dsub-maptable.js] brought " + layerCount + " shapes in " + 
									maxBands + " layer(s) to the front");
								if (shapeLayerOptionsBanderror > 0) {	
									alertScope.showError("[rifd-dsub-maptable.js] no band set in shapeLayer options (" + 
										shapeLayerOptionsBanderror + ")");
								}
								if (shapeLayerBringToFrontError > 0) {
									alertScope.showError("[rifd-dsub-maptable.js] shapeLayer bingToFront() error (" + 
										shapeLayerBringToFrontError + ")");
								}
							}
						}; 
						
                        //Select all in map and table
                        $scope.selectAll = function () {
                            CommonMappingStateService.getState("areamap").clearSelectedPolygon($scope.input.name);
                            for (var i = 0; i < $scope.gridOptions.data.length; i++) {
								CommonMappingStateService.getState("areamap").addToSelectedPolygon($scope.input.name, {
									id: $scope.gridOptions.data[i].area_id, 
									gid: $scope.gridOptions.data[i].area_id, 
									label: $scope.gridOptions.data[i].label, 
									band: CommonMappingStateService.getState("areamap").currentBand});
                            }                     
							$scope.input.selectedPolygon = CommonMappingStateService.getState("areamap").getSelectedPolygon($scope.input.name); 
							$scope.selectedPolygon = CommonMappingStateService.getState("areamap").sortSelectedPolygon($scope.input.name);        
							$scope.selectedPolygonCount = $scope.selectedPolygon.length; //total for display
                        };
                        $scope.changeOpacity = function (v) {
                            $scope.transparency = v;
                            $scope.input.transparency = $scope.transparency;
                            if ($scope.geoJSON) {
                                $scope.geoJSON._geojsons.default.eachLayer(handleLayer);
                            }

                        };
                        //Reset only the selected band back to 0
                        $scope.clearBand = function () {
                            var i = CommonMappingStateService.getState("areamap").getSelectedPolygon($scope.input.name).length;
                            while (i--) {
                                if (CommonMappingStateService.getState("areamap").getSelectedPolygon($scope.input.name)[i].band === CommonMappingStateService.getState("areamap").currentBand) {
									var thisPolyID = CommonMappingStateService.getState("areamap").getSelectedPolygon($scope.input.name)[i].id;
									CommonMappingStateService.getState("areamap").removeFromSselectedPolygon($scope.input.name, thisPolyID);
                                }
                            }	    
							$scope.selectedPolygon = CommonMappingStateService.getState("areamap").getSelectedPolygon($scope.input.name);     							
							$scope.selectedPolygonCount = $scope.selectedPolygon.length; //total for display
                        };
                        //Zoom to layer
                        $scope.zoomToExtent = function () {	
							if (CommonMappingStateService.getState("areamap").maxbounds) { //  Zoom back to maximum extent of geolevel
								CommonMappingStateService.getState("areamap").map.fitBounds(
									CommonMappingStateService.getState("areamap").maxbounds);
							}
                        };
                        //Zoom to selection
                        $scope.zoomToSelection = function () {
                            var studyBounds = new L.LatLngBounds();
                            if (angular.isDefined($scope.geoJSON && $scope.geoJSON._geojsons && $scope.geoJSON._geojsons.default)) {
                                $scope.geoJSON._geojsons.default.eachLayer(function (layer) {
									if (CommonMappingStateService.getState("areamap").getSelectedPolygonObj($scope.input.name, 
										layer.feature.properties.area_id)) {
                                        studyBounds.extend(layer.getBounds());
                                    }
                                });
                                if (studyBounds.isValid()) {
                                    CommonMappingStateService.getState("areamap").map.fitBounds(studyBounds);
                                }
                            }
                        };
                        //Show-hide centroids
                        $scope.showCentroids = function () {
					
							CommonMappingStateService.getState("areamap").map.spin(true);  // on
							
							// Delays are to help the spinner
							$timeout(function() {
								if (CommonMappingStateService.getState("areamap").map.hasLayer(centroidMarkers)) {
									$scope.bShowHideCentroids = false;
									SelectStateService.getState().showHideCentroids = false;
									CommonMappingStateService.getState("areamap").map.removeLayer(centroidMarkers);
								} else {
									$scope.bShowHideCentroids = true;
									SelectStateService.getState().showHideCentroids = true;
									CommonMappingStateService.getState("areamap").map.addLayer(centroidMarkers);
								}
							}, 500);
							
							$timeout(function() {
								CommonMappingStateService.getState("areamap").map.whenReady(function () {
									CommonMappingStateService.getState("areamap").map.spin(false);  // off
								});
							}, 500);
                        }; 
						
                        // Show-hide shapes and associated info
						$scope.showShapes = function () {
                            if (CommonMappingStateService.getState("areamap").shapes == undefined) {
								alertScope.showError("[rifd-dsub-maptable.js] no shapes layerGroup");
							}
							else if (CommonMappingStateService.getState("areamap").map.hasLayer(CommonMappingStateService.getState("areamap").shapes)) {
                                CommonMappingStateService.getState("areamap").map.removeLayer(CommonMappingStateService.getState("areamap").shapes);
								alertScope.consoleDebug("[rifd-dsub-maptable.js] remove shapes layerGroup");
								if (CommonMappingStateService.getState("areamap").info._map) { // Remove info control
									CommonMappingStateService.getState("areamap").info.remove();
									alertScope.consoleDebug("[rifd-dsub-maptable.js] remove info control");
								}
								
								$scope.bShowHideSelectionShapes = false;
								SelectStateService.getState().showHideSelectionShapes = false;

                            } 
							else {
                                CommonMappingStateService.getState("areamap").map.addLayer(CommonMappingStateService.getState("areamap").shapes);
								alertScope.consoleDebug("[rifd-dsub-maptable.js] add shapes layerGroup");
								if (CommonMappingStateService.getState("areamap").info._map == undefined) { // Add back info control
									CommonMappingStateService.getState("areamap").info.addTo(CommonMappingStateService.getState("areamap").map);
									alertScope.consoleDebug("[rifd-dsub-maptable.js] add info control");
								}
								
								$scope.bShowHideSelectionShapes = true;
								SelectStateService.getState().showHideSelectionShapes = true;
								
								$scope.bringShapesToFront();
                            }
							alertScope.consoleDebug("[rifd-dsub-maptable.js] showHideSelectionShapes: " + 
								SelectStateService.getState().showHideSelectionShapes);

                        };

                        /*
                         * DISEASE MAPPING OR RISK MAPPING
                         */
                        $scope.studyTypeChanged = function () {
							alertScope.consoleDebug("[rifd-dsub-maptable.js] studyTypeChanged(): " +
								"to input.type: "+ $scope.input.type + 
								"; from SubmissionStateService.getState().studyType: " + SubmissionStateService.getState().studyType + 
								"; and from StudyAreaStateService.getState().type: " + StudyAreaStateService.getState().type);
								
                            //clear selection
                            $scope.clear();
							
							// Reset study state to $scope.input.type
							setStudyType(1);
							
                            //offer the correct number of bands			
							$scope.possibleBands=CommonMappingStateService.getState("areamap").possibleBands;
							$scope.currentBand=CommonMappingStateService.getState("areamap").currentBand;
//							SelectStateService.verifyStudySelection(); 	// Don't - it is not setup
						};
						
						eachFeaureFunction = function (feature, layer, eachFeatureCallback) {
							try {
//								$scope.geoJSONLayers.push(layer);
								//get as centroid marker layer. 
								if (!bWeightedCentres || 										// Not using weighted centres 
									$scope.latlngListById[feature.properties.area_id] == undefined) {	// No weighted centres for this area
									var p = layer.getBounds().getCenter();
									latlngList.push({
										latLng: L.latLng([p.lat, p.lng]), 
										name: feature.properties.name, 
										id: feature.properties.area_id,
										band: -1
									});
									feature.properties.latLng = L.latLng([p.lat, p.lng]);
									var circle = new L.CircleMarker([p.lat, p.lng], {
										radius: 2,
										fillColor: "red",
										color: "#000",
										weight: 1,
										opacity: 1,
										fillOpacity: 0.8
									});
									
									centroidMarkers.addLayer(circle);
									
									if ($scope.latlngListById[feature.properties.area_id]) {
										latlngListDups++;
									}
									else {
										$scope.latlngListById[feature.properties.area_id] = {
											latLng: L.latLng([p.lat, p.lng]), 
											name: p.name,
											circleId: centroidMarkers.getLayerId(circle)
										}
									}
								}
								else { // Using database centroids
									feature.properties.latLng = $scope.latlngListById[feature.properties.area_id].latLng;
								}
								feature.properties.circleId = $scope.latlngListById[feature.properties.area_id].circleId;
								
								
								if (eachFeatureCallback && typeof eachFeatureCallback === "function") {
									eachFeatureCallback();
								}
								else {
									throw new Error("No eachFeatureCallback() function");
								}
							}
							catch(e) {
								if (eachFeatureCallback && typeof eachFeatureCallback === "function") {
									eachFeatureCallback(e.message);
								}
								else {
									throw new Error("No eachFeatureCallback() function");
								}
							}
							
						}

                        /*
						 * Function: 	setupMap()
						 * Parameters:  None
						 * Returns:		Promise
						 * Called from: Directive initialisation
                         * Description:	Initialise basemap, controls etc
                         */		 
						function setupMap() { // Return promise
							// Called on DOM render completion to ensure basemap is rendered
							return $timeout(function () {
								//add baselayer
								$scope.renderMap("areamap");

								//Store the current zoom and view on map changes
								CommonMappingStateService.getState("areamap").map.on('zoomend', function (e) {
									$scope.input.center.zoom = CommonMappingStateService.getState("areamap").map.getZoom();
								});
								CommonMappingStateService.getState("areamap").map.on('moveend', function (e) {
									$scope.input.center.lng = CommonMappingStateService.getState("areamap").map.getCenter().lng;
									$scope.input.center.lat = CommonMappingStateService.getState("areamap").map.getCenter().lat;
								});

								//slider
								var slider = L.control.slider(function (v) {
									$scope.changeOpacity(v);
								}, {
									id: slider,
									position: 'topleft',
									orientation: 'horizontal',
									min: 0,
									max: 1,
									step: 0.01,
									value: $scope.transparency,
									title: 'Transparency',
									logo: '',
									syncSlider: true
								}).addTo(CommonMappingStateService.getState("areamap").map);

								//Custom toolbar
								var tools = mapTools.getSelectionTools($scope);
								for (var i = 0; i < tools.length; i++) {
									new tools[i]().addTo(CommonMappingStateService.getState("areamap").map);
								}

								//scalebar and fullscreen
								L.control.scale({position: 'bottomleft', imperial: false}).addTo(CommonMappingStateService.getState("areamap").map);
								CommonMappingStateService.getState("areamap").map.addControl(new L.Control.Fullscreen());

								//drop down for bands
								var dropDown = mapTools.getBandDropDown($scope);
								new dropDown().addTo(CommonMappingStateService.getState("areamap").map);

								//Set initial map extents
								$scope.center = $scope.input.center;
	//
	// TO STOP LEAFLET NOT DISPLAYING SELECTED AREAS (experimental)
	//                            CommonMappingStateService.getState("areamap").map.setView([$scope.center.lat, $scope.center.lng], $scope.center.zoom);

								//Attributions to open in new window
								L.control.condensedAttribution({
									prefix: '<a href="http://leafletjs.com" target="_blank">Leaflet</a>'
								}).addTo(CommonMappingStateService.getState("areamap").map);

								CommonMappingStateService.getState("areamap").map.doubleClickZoom.disable();
								CommonMappingStateService.getState("areamap").map.band = Math.max.apply(null, CommonMappingStateService.getState("areamap").possibleBands);
							});		
						}
						
                        /*
						 * Function: 	getMyMap()
						 * Parameters:  None
						 * Returns:		Nothing
						 * Called from:
                         * Description:	Render the map and the table
                         */
                        getMyMap = function () {
							start=new Date().getTime();	

                            if (CommonMappingStateService.getState("areamap").map.hasLayer($scope.geoJSON)) {
                                CommonMappingStateService.getState("areamap").map.removeLayer($scope.geoJSON);
                            }

                            var topojsonURL = user.getTileMakerTiles(user.currentUser, thisGeography, $scope.input.selectAt, "topojson"); //  With no x/y/z returns URL
                            latlngList = []; // centroids!
                            $scope.latlngListById = []; // centroids!
                            centroidMarkers = new L.layerGroup();
							
                            //Get the centroids from DB (from the lookup table - not the root tile)
                            bWeightedCentres = true;							
                            user.getTileMakerCentroids(user.currentUser, thisGeography, $scope.input.selectAt).then(function (res) { // Success case 
								// Create LatLng List for centroids
								return createLatLngList(res);	
							}, function (err) { // Error case: not an error - processing continues
								alertScope.consoleError("[rifd-dsub-maptable.js] user.getTileMakerCentroids had error: " + 
									(err ? err : "no error specified"));
								
                                //couldn't get weighted centres so generate geographic with leaflet
                                alertScope.showWarning("Could not find (weighted) centroids stored in database - calculating geographic centroids on the fly");
                                bWeightedCentres = false;	// Force use of GeoJSON centroids
								$scope.centroid_type="Leaflet calculated geographic";
                            }).then(function (res) { // No res etc
							
								//Get map max bounds
								user.getGeoLevelSelectValues(user.currentUser, thisGeography).then(function (res) {
									var lowestLevel = res.data[0].names[0];
									user.getTileMakerTilesAttributes(user.currentUser, thisGeography, lowestLevel).then(function (res) {
										CommonMappingStateService.getState("areamap").maxbounds = 
											L.latLngBounds([res.data.bbox[1], res.data.bbox[2]], [res.data.bbox[3], res.data.bbox[0]]);
//										if (Math.abs($scope.input.center.lng) < 1 && Math.abs($scope.input.center.lat < 1)) {
											CommonMappingStateService.getState("areamap").map.fitBounds(
												CommonMappingStateService.getState("areamap").maxbounds);
//										}
									}, function(err) { // Error case
										promisesErrorHandler("user.getTileMakerTilesAttributes", err); // Abort												
									}).then(function (res) {
										alertScope.consoleDebug("[rifd-dsub-maptable.js] user.getTileMakerTilesAttributes OK: " + 
											(res ? res : "no status"));
										// Delays are to allow the map to initialise
										$timeout(function() {
											
											CommonMappingStateService.getState("areamap").map.whenReady(function() {
												return $timeout(function() { //  Return promise
													alertScope.consoleDebug("[rifd-dsub-maptable.js] map ready, zoomlevel: " +
														CommonMappingStateService.getState("areamap").map.getZoom() +
														"; range: " + CommonMappingStateService.getState("areamap").map.getMinZoom() +
														" to " + CommonMappingStateService.getState("areamap").map.getMaxZoom());
												});
													
											}, 100);
										}, 200);
									}, function(err) { // Error case
										promisesErrorHandler("mapInitialise", err); // Abort												
									}).then(function (res) {
										alertScope.consoleDebug("[rifd-dsub-maptable.js] mapInitialise OK: " + 
											(res ? res : "no status"))
										// Add topoJSON tiles to map
										return asyncCreateTileLayer(topojsonURL);
									}).then(function (res) {
										alertScope.consoleDebug("[rifd-dsub-maptable.js] TileLayer OK: " + 
											(res ? res : "no status"))
										// Add topoJSON tiles to map
										return asyncCreateTopoJsonLayer(topojsonURL);
									}).then(function (res) {
										alertScope.consoleDebug("[rifd-dsub-maptable.js] asyncCreateTopoJsonLayer OK: " + 
											(res ? res : "no status"));
										// Adds centroids to map from GeoJSON. Build eachFeatureArray for centroids using LatLng list 
										// or $scope.latlngListById
										// Edge appears to crash here when retrieving study selection data, but not when choosing
										return addCentroidsToMap(); 
									}, function(err) { // Error case
										promisesErrorHandler("addCentroidsToMap", err); // Abort												
									}).then(function (res) {
										alertScope.consoleDebug("[rifd-dsub-maptable.js] addCentroidsToMap OK: " + (res ? res : "no status"));
									                   
										// Get overall layer properties from lookup table
										user.getTileMakerAttributes(user.currentUser, thisGeography, $scope.input.selectAt).then(function (res) {
											alertScope.consoleDebug("[rifd-dsub-maptable.js] user.getTileMakerAttributes: " + 
												(res ? "OK" : "no status"));
											if (angular.isUndefined(res.data.attributes)) {
												alertScope.showError("Could not get tile attributes from database");
												return; // Stop processing
											}      
											else {								
												$scope.totalPolygonCount = res.data.attributes.length;
											
												// Populate the grid table
												$scope.gridOptions.data = ModalAreaService.fillTable(res.data);		
												alertScope.consoleDebug("[rifd-dsub-maptable.js] ModalAreaService.fillTable: " + 
													$scope.totalPolygonCount + " rows");
												/* Check selected polygon list matches:
												 *		Sort CommonMappingStateService.getState("areamap").getSelectedPolygon($scope.input.name) alphabetically by id
												 *		Build/rebuild sibling CommonMappingStateService.getState("areamap").getAllSelectedPolygonObj($scope.input.name) object used for quick access via id
												 *      Scan data to check in CommonMappingStateService.getState("areamap").getAllSelectedPolygonObj($scope.input.name), set band if required
												 *      Check for duplicates
												 *		Check for area IDs not found in the Lat/long list
												 *		Fail if a) not all CommonMappingStateService.getState("areamap").getAllSelectedPolygonObj($scope.input.name) are in data or b) duplicates are found
												 *		or c) area IDs were not found in the Lat/long list	
												 */						 
												return checkSelectedPolygonList(res.data); // promise						
											}
										}, function (err) {
											promisesErrorHandler("user.getTileMakerAttributes", err); // Abort	
										}).then(function (res) {
											alertScope.consoleDebug("[rifd-dsub-maptable.js] checkSelectedPolygonList: " + 
												(res ? res : "no status"));
											// Add selected shapes to map
											return addSelectedShapes(); // promise
										}, function(err) { // Error case
											promisesErrorHandler("addSelectedShapes", err); // Abort	
										}).then(function (res) {
											alertScope.consoleDebug("[rifd-dsub-maptable.js] addSelectedShapes: " + 
												(res ? res : "no status"));
												
											if ($scope.selectedPolygonCount > 0) {
												$scope.zoomToSelection(); // Zoom to selection	
											}
											else {
												$scope.zoomToExtent(); // Zoom to extent
											}
											
											$timeout(function() {	

												CommonMappingStateService.getState("areamap").map.spin(false);  	// off	
												enableMapSpinners();			// Turn on zoom/pan spinners									
												$scope.redrawMap();				// Force redraw
											}, 100);
										}, function(err) { // Error case
											promisesErrorHandler("addSelectedShapes", err); // Abort	
										})
									});
								}, 
								function(err) { // Error case
									promisesErrorHandler("user.getTileMakerTilesAttributes", err); // Abort	
								});
							}, function(err) { // Error case
								promisesErrorHandler("user.getGeoLevelSelectValues", err); // Abort	
							});		
                        }; // End of getMyMap()

						function promisesErrorHandler(functionName, err) { // Abort the chain of promises processing
							alertScope.showError("[rifd-dsub-maptable.js] " + functionName + " had error: " + 
								(err ? err : "no error specified"));
							CommonMappingStateService.getState("areamap").map.spin(false);  // off	
							enableMapSpinners();
							$scope.shapeLoadUpdate="Map load failed";
							throw new Error("promisesErrorHandler: " + functionName + " had error: " + 
								(err ? err : "no error specified"));
						}
						
                        /*
                         * GET THE SELECT AND VIEW RESOLUTIONS
                         */
                        $scope.geoLevels = [];
                        $scope.geoLevelsViews = [];
						
						// Initialise basemap, controls etc
						setupMap().then(function(res) {
							// Then setup map
							user.getGeoLevelSelectValues(user.currentUser, thisGeography).then(handleGeoLevelSelect, handleGeographyError);
						},
						function(err) {
							promisesErrorHandler("setupMap", err);
						});
						
						function createLatLngList(res) {
							return $q(function(resolve, reject) {
								var latlngListDups=0;
								var latlngListWarnings=0;
								$scope.centroid_type="Not known";
								popWeightedCount=0;
								dbCentroidCount=0;
                                for (var i = 0; i < res.data.smoothed_results.length; i++) {
													
									var circle = undefined;
																		
									if (res.data.smoothed_results[i].pop_x == 'null') { // Fix NULL DB data
										res.data.smoothed_results[i].pop_x=undefined;
									}
									if (res.data.smoothed_results[i].pop_y == 'null') {
										res.data.smoothed_results[i].pop_y=undefined;
									}
									if (res.data.smoothed_results[i].x & isNaN(res.data.smoothed_results[i].x)) { // Not a number
										res.data.smoothed_results[i].x=undefined;
										latlngListWarnings++;
									}
									if (res.data.smoothed_results[i].y && isNaN(res.data.smoothed_results[i].y)) {
										res.data.smoothed_results[i].y=undefined;
										latlngListWarnings++;
									}
									if (res.data.smoothed_results[i].pop_x && isNaN(res.data.smoothed_results[i].pop_x)) { // Not a number
										res.data.smoothed_results[i].pop_x=undefined;
										latlngListWarnings++;
									}
									if (res.data.smoothed_results[i].pop_y && isNaN(res.data.smoothed_results[i].pop_y)) {
										res.data.smoothed_results[i].pop_y=undefined;
										latlngListWarnings++;
									}
									
                                    var p = res.data.smoothed_results[i];	

									if (res.data.smoothed_results[i] && 
									    res.data.smoothed_results[i].pop_x && 
										res.data.smoothed_results[i].pop_y) {
										popWeightedCount++;
										var pwLatLng=undefined;
										try { // Try to convert, will use GeoJSON if it fails
											pwLatLng=L.latLng([p.pop_y, p.pop_x]);
										}
										catch (e) {
											latlngListWarnings++;
											if (latlngListWarnings < 10) {
												alertScope.consoleError("[rifd-dsub-maptable.js] Unable to create population weighted centroid from: [" + 
													p.pop_y + ", " + p.pop_x + "]; using GeoJSON", e);
											}
										}
										
										if (pwLatLng) {
											latlngList.push({
												latLng: pwLatLng, 
												popWeighted: true,
												name: p.name, 
												id: p.id,
												band: -1
											});
											circle = new L.CircleMarker([p.pop_y, p.pop_x], {
												radius: 2,
												fillColor: "green",
												color: "#000",
												weight: 1,
												opacity: 1,
												fillOpacity: 0.8
											});
											
											centroidMarkers.addLayer(circle);

											if ($scope.latlngListById[p.id]) {
												latlngListDups++;
											}
											else {
												$scope.latlngListById[p.id] = {
													latLng: pwLatLng, 
													name: p.name,
													popWeighted: true,
													circleId: centroidMarkers.getLayerId(circle)
												}
											}
										}
									}
									else if (res.data.smoothed_results[i] && 
									    res.data.smoothed_results[i].x && 
										res.data.smoothed_results[i].y) {
										dbCentroidCount++;
										var dbLatLng=undefined;
										try {
											dbLatLng=L.latLng([p.y, p.x]);
										}
										catch (e) {
											latlngListWarnings++;
											if (latlngListWarnings < 10) {
												alertScope.consoleError("[rifd-dsub-maptable.js] Unable to create database centroid from: [" + 
													p.y + ", " + p.x + "]; using GeoJSON", e);
											}
										}

										if (dbLatLng) {
											latlngList.push({
												latLng: dbLatLng, 
												popWeighted: false,
												name: p.name, 
												id: p.id,
												band: -1
											});
											circle = new L.CircleMarker([p.y, p.x], {
												radius: 2,
												fillColor: "blue",
												color: "#000",
												weight: 1,
												opacity: 1,
												fillOpacity: 0.8
											});
											
											centroidMarkers.addLayer(circle);

											if ($scope.latlngListById[p.id]) {
												latlngListDups++;
											}
											else {
												$scope.latlngListById[p.id] = {
													latLng: dbLatLng, 
													name: p.name,
													popWeighted: false,
													circleId: centroidMarkers.getLayerId(circle)
												}
											}											
										}
									}										
                                } // End of for loop
								
								if (latlngListDups > 0) {
									alertScope.showWarning(latlngListDups + " duplicate IDs in centroid list");
								}
								var pctPopWeighted=Math.round(10000*popWeightedCount/res.data.smoothed_results.length)/100;
									
								if (res.data.smoothed_results.length == popWeightedCount) {
									$scope.centroid_type="population weighted";
								}
								else if (popWeightedCount > 0) {
									$scope.centroid_type=pctPopWeighted + "% population weighted";
								}
								else if (res.data.smoothed_results.length == dbCentroidCount) {
									$scope.centroid_type="database geographic";
								}
								else if (0 == dbCentroidCount) { // No centroids at all
									throw new Error("user.getTileMakerCentroids: dbCentroidCount=0" +
										"; popWeightedCount: " + popWeightedCount +
										"; res.data.smoothed_results.length: " + res.data.smoothed_results.length +
										"; pctPopWeighted: " + pctPopWeighted);
								}
								else {
									throw new Error("user.getTileMakerCentroids() invalid condition: dbCentroidCount=" + dbCentroidCount +
										"; popWeightedCount: " + popWeightedCount +
										"; res.data.smoothed_results.length: " + res.data.smoothed_results.length +
										"; pctPopWeighted: " + pctPopWeighted);
								}
								
								if (latlngListWarnings > 0) {
									alertScope.showWarning(latlngListWarnings + 
										" coordinates in centroid list were invalid; calculated instead from GeoJSON shape");
									$scope.centroid_type=$scope.centroid_type + "; " + latlngListWarnings + " invalid centroids";
								}
								
								if (res.data.smoothed_results.length > disableMouseClicksAt) { // 5000 by default
									$scope.noMouseClocks=true;
								}
								else {	
									$scope.noMouseClocks=false;
								}
								
								alertScope.consoleDebug("[rifd-dsub-maptable.js] " + thisGeography + "; " + $scope.input.selectAt +
									"; polygons: " + res.data.smoothed_results.length +
									"; dbCentroidCount: " + dbCentroidCount +
									"; popWeightedCount: " + popWeightedCount +
									"; pctPopWeighted: " + pctPopWeighted +
									"; disableMouseClicksAt: " + disableMouseClicksAt +
									"; noMouseClocks: " + $scope.noMouseClocks +
									"; centroid_type: " + $scope.centroid_type);
									
								CommonMappingStateService.getState("areamap").info.update();
								
								resolve("processed centroids");						
							}); // End of $q constructor
						}
							
						function asyncCreateTileLayer(topojsonURL) {
							return $q(function(resolve, reject) {
								if ($scope.noMouseClocks && 
								    CommonMappingStateService.getState("areamap").getSelectedPolygon($scope.input.name).length > 0) {
									var pngURL=topojsonURL.replace('&tileType=topojson', '&tileType=png');
									alertScope.consoleDebug("[rifd-dsub-maptable.js] TileLayer enabled; pngURL: " +
										pngURL);
                                    // Needs to handle PNG creation timeouts, e.g: https://jsfiddle.net/rendrom/0bef9r7z/                                     
									$scope.pngTiles = new L.TileLayer(pngURL, {
										attribution: 'Polygons &copy; <a href="http://www.sahsu.org/content/rapid-inquiry-facility" target="_blank">Imperial College London</a>',
										interactive: true,
                                        maxNativeZoom: 11 // Avoid: Error code is 'INVALID_ZOOM_FACTOR'. Message list is: 
                                                    // '12 is an invalid zoom factor.  It must be a number in [0,11].'
									}); // End of L.TileLayer
									var pngTileErrorCount=0;
                                    
									if ($scope.pngTiles == undefined) {
										reject("failed to create TileLayer");
									}
									else {
										$scope.pngTiles.on('load', function(layer) {
                                            var zoomLevel = CommonMappingStateService.getState("areamap").map.getZoom();
                                            if (pngTileErrorCount > 0) {
                                                alertScope.showWarning("PNG TileLayer loaded zoomlevel " + zoomLevel + 
                                                    " with " + pngTileErrorCount + " errors (please re-try in one minute)");
                                                pngTileErrorCount=0;
                                            }
                                            else {
                                                alertScope.consoleDebug("[rifd-dsub-maptable.js] zoomlevel " + zoomLevel + 
                                                    " tileLayer: " + pngURL + " loaded");
                                            }
											resolve("topoJsonGridLayer enable areaId filtering: added TileLayer");
										});
										$scope.pngTiles.on('moveend', function(layer) {
                                            var zoomLevel = CommonMappingStateService.getState("areamap").map.getZoom();
                                            if (pngTileErrorCount > 0) {
                                                alertScope.showWarning("Zoomlevel " + zoomLevel + 
                                                    " PNG TileLayer moved with " + 
                                                    pngTileErrorCount + " errors (please re-try in one minute)");
                                                pngTileErrorCount=0;
                                            }
                                            else {
                                                alertScope.consoleDebug("[rifd-dsub-maptable.js] zoomlevel " + zoomLevel + 
                                                    " tileLayer: " + pngURL + " moved");
                                            }
										});
										$scope.pngTiles.on('zoomend', function(layer) {
                                            var zoomLevel = CommonMappingStateService.getState("areamap").map.getZoom();
                                            if (pngTileErrorCount > 0) {
                                                alertScope.showWarning("Zoomlevel " + zoomLevel + 
                                                    " PNG TileLayer zoomed with " + 
                                                    pngTileErrorCount + " errors (please re-try in one minute)");
                                                pngTileErrorCount=0;
                                            }
                                            else {
                                                alertScope.consoleDebug("[rifd-dsub-maptable.js] zoomlevel " + zoomLevel + 
                                                    " tileLayer: " + pngURL + " zoomed");
                                            }
										});
										$scope.pngTiles.on('tileerror', function(error) {
                                            var zoomLevel = CommonMappingStateService.getState("areamap").map.getZoom();
                                            pngTileErrorCount++;
											alertScope.consoleError("[rifc-util-mapping.js] Error[" + pngTileErrorCount + 
                                                "]: loading PNG tile using URL: " + pngURL +
                                                "; zoomLevel: " + zoomLevel + 
                                                "; error: " + ((error && error.message && error.error.message) ? error.error.message : "N/A") +
                                                "; tile coordinates: " + 
                                                ((error && error.coords) ? JSON.stringify(error.coords) : "UNK"));	
						
										});
										CommonMappingStateService.getState("areamap").map.addLayer($scope.pngTiles);
									}
								}
								else {
									resolve("topoJsonGridLayer enable areaId filtering disabled");
								}
							});							
						}
						
						function asyncCreateTopoJsonLayer(topojsonURL) {
							$scope.geoJSONLoadCount=0;
							return $q(function(resolve, reject) {
								
								var latlngListDups=0;
								var areaIdObj={};
								if ($scope.noMouseClocks && 
								    CommonMappingStateService.getState("areamap").getSelectedPolygon($scope.input.name).length > 0) {
									alertScope.consoleDebug("[rifd-dsub-maptable.js] topoJsonGridLayer enable areaId filtering");
									areaIdObj=CommonMappingStateService.getState("areamap").getAllSelectedPolygonObj($scope.input.name);
								}
								else {
									alertScope.consoleDebug("[rifd-dsub-maptable.js] topoJsonGridLayer disable areaId filtering");
								}
								eachFeatureArray = [];
								CommonMappingStateService.getState("areamap").map.spin(true);  // on
                                $scope.geoJSON = new L.topoJsonGridLayer(topojsonURL, {
								   areaIdObj: areaIdObj,
								   consoleDebug: function(msg) {
									   if (msg) {
											alertScope.consoleDebug(msg);
									   }
								   },
                                   attribution: 'Polygons &copy; <a href="http://www.sahsu.org/content/rapid-inquiry-facility" target="_blank">Imperial College London</a>',
                                   interactive: true,
                                   maxNativeZoom: 11, // Avoid: Error code is 'INVALID_ZOOM_FACTOR'. Message list is: 
                                                // '12 is an invalid zoom factor.  It must be a number in [0,11].'
								   layers: {
                                        default: {
                                            renderer: L.canvas(),
                                            style: style,
                                            onEachFeature: function (feature, layer) {
												eachFeatureArray.push({ // So can be queued
													feature: feature,
													layer: layer});
														
												if (!$scope.noMouseClocks) {
													layer.on('mouseover', function (e) {
	//													alertScope.consoleDebug("[rifd-dsub-maptable.js] topoJsonGridLayer " + e.type + ": " + 
	//														feature.properties.name + "; " + e.target._leaflet_id);
														//if drawing then return
														if ($scope.input.bDrawing) {
															return;
														}
														this.setStyle({
															color: 'gray',
															weight: 1.5,
															fillOpacity: function () {
																//set tranparency from slider
																return($scope.transparency - 0.3 > 0 ? $scope.transparency - 0.3 : 0.1);
															}()
														});
														$scope.thisPolygon = feature.properties.name;
														// Centroids: feature.properties.latLng [app] and 
														// feature.properties.geographic_centroid{} [tilemaker]
														
														if (feature.properties.circleId) {
															$scope.highLightedCircleId=feature.properties.circleId;
															var circle=centroidMarkers.getLayer(feature.properties.circleId);
															circle.setStyle({
																radius: 3,
																weight: 2
															});
														}
														$scope.$digest();
													});
													layer.on('mouseout', function (e) {
														$scope.geoJSON._geojsons.default.resetStyle(e.target);
														$scope.thisPolygon = "";
														if ($scope.highLightedCircleId) {
															var circle=centroidMarkers.getLayer(feature.properties.circleId);
															circle.setStyle({
																radius: 2,
																weight: 1
															});
															$scope.highLightedCircleId=undefined;
														}
														$scope.$digest();
													});
													layer.on('click', function (e) {
														//if drawing then return
														if ($scope.input.bDrawing) {
															return;
														}
														var thisPolyID = e.target.feature.properties.area_id;
														var bFound = false;
														for (var i = 0; i < CommonMappingStateService.getState("areamap").getSelectedPolygon($scope.input.name).length; i++) {
															if (CommonMappingStateService.getState("areamap").getSelectedPolygon($scope.input.name)[i].id === thisPolyID) {
																bFound = true;
																$scope.selectedPolygon = 
																	CommonMappingStateService.getState("areamap").removeFromSselectedPolygon(
																		$scope.input.name, thisPolyID);
																break;
															}
														}

														if (!bFound) {	
															var newSelectedPolygon = {
																id: thisPolyID, 
																gid: e.target.feature.properties.gid, label: 
																e.target.feature.properties.name, 
																band: CommonMappingStateService.getState("areamap").currentBand};
															$scope.selectedPolygon = 
																CommonMappingStateService.getState("areamap").addToSelectedPolygon($scope.input.name,
																	newSelectedPolygon);
														}       

														$scope.input.selectedPolygon = CommonMappingStateService.getState("areamap").getSelectedPolygon($scope.input.name); 														
														$scope.selectedPolygonCount = $scope.selectedPolygon.length; //total for display
														$scope.$digest(); // Force $watch sync
													});
												}
											}
                                        }
                                    } // End of layers definition
                                }); // End of L.topoJsonGridLayer
								
								if ($scope.geoJSON == undefined) {
									reject("failed to create topoJsonGridLayer");
								}
								else {
									$scope.geoJSON.on('loading', function(layer) {
										alertScope.consoleDebug("[rifd-dsub-maptable.js] topoJsonGridLayer loading " + $scope.input.selectAt + ": " + ($scope.geoJSONLoadCount+1));
										if ($scope.geoJSONLoadCount == 0) {
											$scope.shapeLoadUpdate = "Loading " + $scope.input.selectAt + "...";
										}
									});
									$scope.geoJSON.on('load', function(layer) {
										alertScope.consoleDebug("[rifd-dsub-maptable.js] topoJsonGridLayer loaded " + $scope.input.selectAt + ": " + ($scope.geoJSONLoadCount+1));
										$scope.geoJSONLoadCount++;
										$timeout(function() { // Slow things down a bit for MS Edge to avoid crash [DID NOT WORK]
											if ($scope.geoJSONLoadCount == 1) {
													$scope.shapeLoadUpdate = "Loaded " + $scope.input.selectAt;
													$scope.totalPolygonCount = eachFeatureArray.length;
											}
										}, 100).then(function(res) {
											resolve("map ready topoJsonGridLayer eachFeatureArray: " + 
												eachFeatureArray.length + "; res: " + (res ? res : "no status"));
										},
										function(err) {
											reject("map ready topoJsonGridLayer with error eachFeatureArray: " + 
												eachFeatureArray.length + "; res: " + (err ? err : "no error"));
										});
									});
									CommonMappingStateService.getState("areamap").map.addLayer($scope.geoJSON);
								}
							}); // End of $q constructor
						}
					
						/*
						 * Function:    checkSelectedPolygonList()
						 * Parameter:   res.data from user.getTileMakerAttributes()
						 * Returns:     A promise
						 * Description: Sort CommonMappingStateService.getState("areamap").getSelectedPolygon($scope.input.name) alphabetically by id
						 *				Build/rebuild sibling CommonMappingStateService.getState("areamap").getAllSelectedPolygonObj($scope.input.name) object used for quick access via id
						 *              Scan data to check in CommonMappingStateService.getState("areamap").getAllSelectedPolygonObj($scope.input.name), set band if required
						 *              Check for duplicates
						 *				Check for area IDs not found in the Lat/long list
						 *				Fail if a) not all CommonMappingStateService.getState("areamap").getAllSelectedPolygonObj($scope.input.name) are in data or b) duplicates are found
						 *  			or c) area IDs were not found in the Lat/long list
						 */
						function checkSelectedPolygonList(data) {
							
							return $q(function(resolve, reject) {

								var foundCount=0;
								var missingLatLng=0;
											
								alertScope.consoleDebug("[rifd-dsub-maptable.js] checking selectedPolygon: " + 
									CommonMappingStateService.getState("areamap").getSelectedPolygon($scope.input.name).length);
									
//								var notFoundPolys = [];
//								var geojsonPolys = [];
								var collectionLength = data.attributes.length;
								// Scan data to check in CommonMappingStateService.getState("areamap").getAllSelectedPolygonObj($scope.input.name), set band if required 			   
								for (var i = 0; i < collectionLength; i++) {
									var thisPoly = data.attributes[i];
//									geojsonPolys.push(thisPoly.area_id)
									if (CommonMappingStateService.getState("areamap").getSelectedPolygonObj($scope.input.name, thisPoly.area_id)) {
										data.attributes[i].band = CommonMappingStateService.getState("areamap").getSelectedPolygonObj($scope.input.name, 
											thisPoly.area_id).band; // Set the band
//										CommonMappingStateService.getState("areamap").getSelectedPolygonObj($scope.input.name, thisPoly.area_id).found=true;
										foundCount++;
									}
									else {
										data.attributes[i].band = 0;
									}
								}
								CommonMappingStateService.getState("areamap").sortSelectedPolygon($scope.input.name); // May force a watchCollection
								doWatchUpdate(CommonMappingStateService.getState("areamap").getSelectedPolygon($scope.input.name),
									CommonMappingStateService.getState("areamap").getSelectedPolygon($scope.input.name)); // Do a a watchCollection
	
// For finding missing polygons:	
//								for (key in CommonMappingStateService.getState("areamap").getAllSelectedPolygonObj($scope.input.name)) {
//									if (CommonMappingStateService.getState("areamap").getSelectedPolygonObj($scope.input.name, key).found == false) {
//										notFoundPolys.push(CommonMappingStateService.getState("areamap").getSelectedPolygonObj($scope.input.name, key));
//									}
//								}
//								notFoundPolys.sort(); // Alphabetically!
	
								for (var i = 0; i < CommonMappingStateService.getState("areamap").getSelectedPolygon($scope.input.name).length; i++) { // Check for duplicates
									if ($scope.latlngListById[CommonMappingStateService.getState("areamap").getSelectedPolygon($scope.input.name)[i].id] == undefined) {
										missingLatLng++;
									}
								}
								
								// Fail if a) not all CommonMappingStateService.getState("areamap").getAllSelectedPolygonObj($scope.input.name) are in data or b) duplicates are found	
								// or c) area IDs were not found in the Lat/long list							
								var hasErrors=false;
								if (missingLatLng > 0) {
									alertScope.showError(missingLatLng + 
										" area IDs were not found in the Lat/long list");
									hasErrors=true;
								}								
								if (foundCount != CommonMappingStateService.getState("areamap").getSelectedPolygon($scope.input.name).length) {
									
									if (CommonMappingStateService.getState("areamap").getSelectedPolygon($scope.input.name).length < 10) {
										alertScope.consoleDebug("[rifd-dsub-maptable.js] unmatchable selectedPolygon[" + 
											CommonMappingStateService.getState("areamap").getSelectedPolygon($scope.input.name).length + "]: " +
											JSON.stringify(CommonMappingStateService.getState("areamap").getSelectedPolygon($scope.input.name)) +
											"; foundCount: " + foundCount);										
									}
									else {
										alertScope.consoleDebug("[rifd-dsub-maptable.js] unmatchable selectedPolygon: " + 
											CommonMappingStateService.getState("areamap").getSelectedPolygon($scope.input.name).length + 
											"; foundCount: " + foundCount);										
									}
	
									alertScope.showError("Could not match " + (CommonMappingStateService.getState("areamap").getSelectedPolygon($scope.input.name).length - foundCount) + " polygons from database with selected polygons list");
									hasErrors=true;
								}
								
								if (hasErrors) {
									reject("error processing selected polygons list");
										
	//								alertScope.consoleDebug("[rifd-dsub-maptable.js] CommonMappingStateService.getState("areamap").getSelectedPolygon($scope.input.name): " + 
	//									JSON.stringify(CommonMappingStateService.getState("areamap").getSelectedPolygon($scope.input.name), null, 1));
									
									alertScope.consoleDebug("[rifd-dsub-maptable.js] foundCount: " + foundCount + 
										"; data.attributes: " + collectionLength + 
										"; $scope.totalPolygonCount: " + $scope.totalPolygonCount + 
										"; selectedPolygon.length " + CommonMappingStateService.getState("areamap").getSelectedPolygon($scope.input.name).length +
										"; selectedPolygonCount: " + $scope.selectedPolygonCount /* + 
										"; geojsonPolys(" + geojsonPolys.length + "): " + JSON.stringify(geojsonPolys, null, 1) +
										"; notFoundPolys(" + notFoundPolys.length + "): " + JSON.stringify(notFoundPolys, null, 1) */);
								}
								else {	
									$scope.input.selectedPolygon = CommonMappingStateService.getState("areamap").getSelectedPolygon($scope.input.name); 														
									$scope.selectedPolygon = CommonMappingStateService.getState("areamap").sortSelectedPolygon($scope.input.name);
									$scope.selectedPolygonCount = $scope.selectedPolygon.length; //total for display
									resolve("processed selected polygons list");
								}
							})
						}										
						
						function addSelectedShapes() {
							return $q(function(resolve, reject) {
								var selectedShapes=undefined;
								
								// Add back selected shapes
								if (SelectStateService.getState().studySelection) {
									if ($scope.input.name == "ComparisionAreaMap") {
										selectedShapes=SelectStateService.getState().studySelection.comparisonShapes;
									}
									else {
										selectedShapes=SelectStateService.getState().studySelection.studyShapes;
									}
								}
								
								if (selectedShapes) {
									alertScope.consoleDebug("[rifd-dsub-maptable.js] addSelectedShapes() selectedShapes " + 
										$scope.input.name + ": " + 
										selectedShapes.length + " shape");
									for (var i=0; i<selectedShapes.length; i++) {
										var points=0;
										if (selectedShapes[i].geojson &&
											selectedShapes[i].geojson.geometry.coordinates[0]) {
											points=selectedShapes[i].geojson.geometry.coordinates[0].length;
										}
										alertScope.consoleDebug("[rifd-dsub-maptable.js] selectedShape[" + i + "] " +
											"band: " + selectedShapes[i].band +
											"; color[" + (selectedShapes[i].band-1) + "]: " + selectorBands.bandColours[selectedShapes[i].band-1] +
											"; circle: " + selectedShapes[i].circle +
											"; freehand: " + selectedShapes[i].freehand +
											"; points: " + points);
									}
										
								
									alertScope.consoleDebug("[rifd-dsub-maptable.js] showHideSelectionShapes: " + SelectStateService.getState().showHideSelectionShapes +
									 "; $scope.bShowHideSelectionShapes: " + $scope.bShowHideSelectionShapes + 
									 "; map.hasLayer(shapes): " + CommonMappingStateService.getState("areamap").map.hasLayer(CommonMappingStateService.getState("areamap").shapes));
									if (SelectStateService.getState().showHideSelectionShapes) {
										$scope.bShowHideSelectionShapes = true;
										if (!CommonMappingStateService.getState("areamap").map.hasLayer(CommonMappingStateService.getState("areamap").shapes)) {
											alertScope.consoleDebug("[rifd-dsub-maptable.js] add shapes layerGroup");
											CommonMappingStateService.getState("areamap").map.addLayer(CommonMappingStateService.getState("areamap").shapes);
											if (CommonMappingStateService.getState("areamap").info._map == undefined) { // Add back info control
												CommonMappingStateService.getState("areamap").info.addTo(CommonMappingStateService.getState("areamap").map);
												alertScope.consoleDebug("[rifd-dsub-maptable.js] add info control");
											}
										}
										if (CommonMappingStateService.getState("areamap").shapes.getLayers().length == 0) {
											alertScope.consoleDebug("[rifd-dsub-maptable.js] start addSelectedShapes(): shapes layerGroup has no layers");				
										}
										else {
											alertScope.consoleDebug("[rifd-dsub-maptable.js] start addSelectedShapes(): shapes layerGroup has " +
												CommonMappingStateService.getState("areamap").shapes.getLayers().length + " layers");
										}
									} else {
										if (CommonMappingStateService.getState("areamap").map.hasLayer(CommonMappingStateService.getState("areamap").shapes)) {
											alertScope.consoleDebug("[rifd-dsub-maptable.js] remove shapes layerGroup");
											CommonMappingStateService.getState("areamap").map.removeLayer(CommonMappingStateService.getState("areamap").shapes);
											if (CommonMappingStateService.getState("areamap").info._map) { // Remove info control
												CommonMappingStateService.getState("areamap").info.remove();
												alertScope.consoleDebug("[rifd-dsub-maptable.js] remove info control");
											}
										}
										$scope.bShowHideSelectionShapes = false;
									}	
									
									var errorCount=0;
									$scope.shapeLoadUpdate = "0 / " + selectedShapes.length + " shapes, 0%";
									async.eachOfSeries(selectedShapes, 
										function selectedShapesIteratee(eachFeature, i, selectedShapesCallback) {
											var selectedShape=selectedShapes[i];
											
											$scope.shapeLoadUpdate = (i) + " / " + selectedShapes.length + " shapes, " + 
												(Math.round(((i)/selectedShapes.length)*100)) + "%";
											function selectedShapesHighLightFeature(e, selectedShape) {
		//										alertScope.consoleDebug("[rifd-dsub-maptable.js] selectedShapesHighLightFeature " + 
		//											"(" + e.target._leaflet_id + "; for: " + e.originalEvent.currentTarget._leaflet_id + 
		//											"; " + JSON.stringify((e.target._latlng || e.latlng)) + "): " +
		//											(JSON.stringify(selectedShape.properties) || "no properties"));
												CommonMappingStateService.getState("areamap").info.update(selectedShape, (e.target._latlng || e.latlng));
											}									
											function selectedShapesResetFeature(e) {
		//										alertScope.consoleDebug("[rifd-dsub-maptable.js] selectedShapesResetFeature " +  
		//											"(" + e.target._leaflet_id + "; for: " + e.originalEvent.currentTarget._leaflet_id + 
		//											"; " + JSON.stringify((e.target._latlng || e.latlng)) + "): " +
		//											(JSON.stringify(selectedShape.properties) || "no properties"));
												CommonMappingStateService.getState("areamap").info.update(undefined, (e.target._latlng || e.latlng));
											}		
											
											if (selectedShape.circle) { // Represent circles as a point and a radius
											
												if ((selectedShape.band == 1) || (selectedShape.band > 1 && !selectedShape.finalCircleBand)) {
													// basic shape to map shapes layer group
													var circle = new L.Circle([selectedShape.latLng.lat, selectedShape.latLng.lng], {
															pane: 'shapes', 
															band: selectedShape.band,
															area: selectedShape.area,
															radius: selectedShape.radius,
															color: (selectorBands.bandColours[selectedShapes[i].band-1] || 'blue'),
															weight: (selectorBands.weight || 3),
															opacity: (selectorBands.opacity || 0.8),
															fillOpacity: (selectorBands.fillOpacity || 0),
															selectedShape: selectedShape
														});										
													circle.on({
														mouseover : function(e) {
															selectedShapesHighLightFeature(e, this.options.selectedShape);
														}, 
														mouseout : function(e) {
															selectedShapesResetFeature(e, this.options.selectedShape);
														} 
													}); 
													
													if (circle) {
														CommonMappingStateService.getState("areamap").shapes.addLayer(circle);
														alertScope.consoleDebug("[rifd-dsub-maptable.js] addSelectedShapes(): " +
															"adding circle: " + JSON.stringify(selectedShape.latLng) + 
															"; color[" + (selectedShapes[i].band-1) + "]: " + (selectorBands.bandColours[selectedShapes[i].band-1] || 'blue') + 
															"; radius: " + selectedShape.radius + 
															"; band: " + selectedShape.band +
															"; area: " + selectedShape.area);
													}
													else {
														alertScope.showError("Could not restore circle");
														errorCount++;
													}
													
													if (selectedShape.band == 1) {
													   var factory = L.icon({
															iconUrl: 'images/factory.png',
															iconSize: 15
														});
														var marker = new L.marker([selectedShape.latLng.lat, selectedShape.latLng.lng], {
															pane: 'shapes',
															icon: factory
														});
														CommonMappingStateService.getState("areamap").shapes.addLayer(marker);
													}
												}
											}
											else { // Use L.polygon(), L.geoJSON needs a GeoJSON layer
												var polygon; 
												var coordinates=selectedShape.geojson.geometry.coordinates[0];												
												if (selectedShape.freehand) { // Shapefile		
													coordinates=selectedShape.coordinates;	
												}		
																				
												polygon=L.polygon(coordinates, {
														pane: 'shapes', 
														band: selectedShape.band,
														area: selectedShape.area,
														color: (selectorBands.bandColours[selectedShapes[i].band-1] || 'blue'),
														weight: (selectorBands.weight || 3),
														opacity: (selectorBands.opacity || 0.8),
														fillOpacity: (selectorBands.fillOpacity || 0),
														selectedShape: selectedShape
													});		
												if (polygon && polygon._latlngs.length > 0) {										
													polygon.on({
														mouseover : function(e) {
															selectedShapesHighLightFeature(e, this.options.selectedShape);
														}, 
														mouseout : function(e) {
															selectedShapesResetFeature(e, this.options.selectedShape);
														} 
													}); 
													CommonMappingStateService.getState("areamap").shapes.addLayer(polygon);
													alertScope.consoleDebug("[rifd-dsub-maptable.js] addSelectedShapes(): adding polygon" + 
														"; band: " + selectedShape.band +
														"; area: " + selectedShape.area +
														"; freehand: " + selectedShape.freehand +
														"; " + coordinates.length + " coordinates; " +
														JSON.stringify(coordinates).substring(0,100) + "...");							
												}
												else {
													alertScope.consoleDebug("[rifd-dsub-maptable.js] addSelectedShapes(): L.Polygon is undefined" +
														"; geoJSON: " + JSON.stringify(selectedShape.geojson, null, 1));
													errorCount++;
													if (selectedShape.freehand) {	
														alertScope.showError("Could not restore freehand Polygon shape");
													}
													else {
														alertScope.showError("Could not restore shapefile Polygon shape");
													}
												}
											}
											selectedShapesCallback();
										}, function done(err) {	
											var end=new Date().getTime();
											var elapsed=(Math.round((end - start)/100))/10; // in S	
											
											if (selectedShapes.length > 0) {
												$scope.shapeLoadUpdate = selectedShapes.length + " shapes loaded; total time: " + elapsed + " S";
											}
											else {
												$scope.shapeLoadUpdate = "no shapes loaded";
											}
											if (errorCount > 0) {
												reject(errorCount + "errors adding " + selectedShapes.length + 
													" selected shapes; total time: " + elapsed + " S");
											}
											else {
												resolve("added " + selectedShapes.length + " selected shapes; total time: " + elapsed + " S");
											}
										}); // End of async.eachOfSeries()				
								}
								else {
									resolve("map has no selected shapes");
								}
							});
						}

						function addCentroidsToMap() {

							return $q(function(resolve, reject) {					

								alertScope.consoleDebug("[rifd-dsub-maptable.js] start add " + eachFeatureArray.length + 
									" feature centroids");
											
								async.eachOfSeries(eachFeatureArray, 
									function eachFeatureArrayIteratee(eachFeature, indexKey, eachFeatureCallback) {
										if (indexKey % 500 == 0) {
											alertScope.consoleDebug("[rifd-dsub-maptable.js] processing feature " + indexKey + "/" + eachFeatureArray.length + 
												" feature centroids; call $timeout()");		
											$scope.shapeLoadUpdate = "Loading centroids: " + indexKey + "/" + eachFeatureArray.length + "; " + 
												(Math.round(((indexKey)/eachFeatureArray.length)*100)) + "%";											
											$timeout(function() { // Be nice to the stack if you are going to be aggressive!
												eachFeaureFunction(eachFeature.feature, eachFeature.layer, eachFeatureCallback);
											}, 100);
										}									
										else if (indexKey % 50 == 0) {	
											async.setImmediate(function() { // Be nice to the stack if you are going to be aggressive!
												eachFeaureFunction(eachFeature.feature, eachFeature.layer, eachFeatureCallback);
											});
										}	
										else {
	//										alertScope.consoleDebug("[rifd-dsub-maptable.js] processing feature " + indexKey + "/" + eachFeatureArray.length + 
	//											" feature centroids");	
											eachFeaureFunction(eachFeature.feature, eachFeature.layer, eachFeatureCallback);
										}
									}, function done(err) {
										alertScope.consoleDebug("[rifd-dsub-maptable.js] end add " + eachFeatureArray.length + 
											" feature centroids");
										$scope.shapeLoadUpdate = "Loaded " + eachFeatureArray.length + " centroids";	
										eachFeatureArray = [];
										
										alertScope.consoleDebug("[rifd-dsub-maptable.js] showHideCentroid: " + SelectStateService.getState().showHideCentroids +
										 "; $scope.bShowHideCentroids: " + $scope.bShowHideCentroids + 
		//								 "; centroidMarkers: " + JSON.stringify(centroidMarkers) + 
										 "; map.hasLayer(centroidMarkers): " + CommonMappingStateService.getState("areamap").map.hasLayer(centroidMarkers));
										if (SelectStateService.getState().showHideCentroids) {
											$scope.bShowHideCentroids = true;
											if (!CommonMappingStateService.getState("areamap").map.hasLayer(centroidMarkers)) {
												CommonMappingStateService.getState("areamap").map.addLayer(centroidMarkers);
											}
										} else {
											if (CommonMappingStateService.getState("areamap").map.hasLayer(centroidMarkers)) {
												CommonMappingStateService.getState("areamap").map.removeLayer(centroidMarkers);
											}
											$scope.bShowHideCentroids = false;
										}	
									
										CommonMappingStateService.getState("areamap").map.whenReady(function() {
											
											$timeout(function() {
												if (err) {
													reject("eachFeaureFunction error: " + err);
												}		
												else {
													resolve("map complete.");
												}		
											}, 100);	
										});	
							
									}); // End of async.eachOfSeries()		
							});									
						}
								
						function enableMapSpinners() {
							CommonMappingStateService.getState("areamap").map.on('zoomstart', function(ev) {
								CommonMappingStateService.getState("areamap").map.spin(true);  // on
							});
							CommonMappingStateService.getState("areamap").map.on('zoomend', function(ev) {
								CommonMappingStateService.getState("areamap").map.spin(false);  // off
							});
							CommonMappingStateService.getState("areamap").map.on('movestart', function(ev) {
								CommonMappingStateService.getState("areamap").map.spin(true);  // on
							});
							CommonMappingStateService.getState("areamap").map.on('moveend', function(ev) {
								CommonMappingStateService.getState("areamap").map.spin(false);  // off
							});								
						}
						
                        function handleGeoLevelSelect(res) {
							
							function handleDefaultGeoLevels(res) {
								//get the select levels
								$scope.input.selectAt = res.data[0].names[0];
								$scope.input.studyResolution = res.data[0].names[0];
								$scope.geoLevelChange();
							}
                            $scope.geoLevels.length = 0;
                            for (var i = 0; i < res.data[0].names.length; i++) {
                                $scope.geoLevels.push(res.data[0].names[i]);
                            }
                            //To check that comparison study area not greater than study area
                            //Assumes that geoLevels is ordered array
                            $scope.input.geoLevels = $scope.geoLevels;
                            //Only get default if pristine
                            if ($scope.input.selectAt === "" & $scope.input.studyResolution === "") {
                                user.getDefaultGeoLevelSelectValue(user.currentUser, thisGeography).then(handleDefaultGeoLevels, handleGeographyError);
                            } else {
                                user.getGeoLevelViews(user.currentUser, thisGeography, $scope.input.selectAt).then(handleGeoLevelViews, handleGeographyError);
                            }
                        }
						
						/*
						 * Called from: setupMap() via handleGeoLevelSelect(), $scope.geoLevelChange() 
						 * Calls: 		getMyMap() to setup map content and tables
						 */
                        function handleGeoLevelViews(res) {
                            $scope.geoLevelsViews.length = 0;
                            for (var i = 0; i < res.data[0].names.length; i++) {
                                $scope.geoLevelsViews.push(res.data[0].names[i]);
                            }
                            //if not in list then match (because result res cannot be lower than select res)
                            if ($scope.geoLevelsViews.indexOf($scope.input.studyResolution) === -1) {
                                $scope.input.studyResolution = $scope.input.selectAt;
                            }
                            // Setup map content and tables
							try {
								getMyMap();
							}
							catch (e) {
								alertScope.consoleError("[rifd-dsub-maptable.js] Unable to fetch map: ", e);
                                alertScope.showError("Unable to fetch map: " + e.message);
                            }
								
                        }

                        function handleGeographyError() {
                            $scope.close();
                        }

                        /*
                         * MAP SETUP
                         */
                        //district centres for rubberband selection
                        var latlngList = [];
                        var centroidMarkers = new L.layerGroup();
						CommonMappingStateService.getState("areamap").shapes = new L.layerGroup();
                        CommonMappingStateService.getState("areamap").map.addLayer(CommonMappingStateService.getState("areamap").shapes);

                        //Set up table (UI-grid)
                        $scope.gridOptions = ModalAreaService.getAreaTableOptions();
                        $scope.gridOptions.columnDefs = ModalAreaService.getAreaTableColumnDefs();
                        //Enable row selections
                        $scope.gridOptions.onRegisterApi = function (gridApi) {
                            $scope.gridApi = gridApi;
                        };
						
                        //Set the user defined basemap
						// Called from rifc-dmap-main.js
                        $scope.renderMap = function (mapID, currentBaseMapInUse) {
							
							if (mapID == undefined) {
								throw new Error("mapID is undefined");
							}
							
							var getCurrentBaseMap=LeafletBaseMapService.getCurrentBaseMapInUse(mapID);
							if (currentBaseMapInUse && getCurrentBaseMap) {
								LeafletBaseMapService.setCurrentBaseMapInUse(mapID, currentBaseMapInUse);
								setBaseMapCallback(undefined /* No error */, mapID);
							}
							else if (CommonMappingStateService.getState(mapID).basemap) {
								LeafletBaseMapService.setCurrentBaseMapInUse(mapID, CommonMappingStateService.getState(mapID).basemap);
								LeafletBaseMapService.setNoBaseMap(mapID, (CommonMappingStateService.getState(mapID).noBasemap || false));
								setBaseMapCallback(undefined /* No error */, mapID);
							}
							else if (thisGeography) {
								alertScope.consoleLog("[rifd-dsub-maptable.js] setDefaultMapBackground for map: " + mapID + 
									"; geography: " + thisGeography + 
									"; currentBaseMapInUse: " + currentBaseMapInUse + 
									"; getCurrentBaseMap: " + getCurrentBaseMap, new Error("Dummy"));		
								LeafletBaseMapService.setDefaultMapBackground(thisGeography, setBaseMapCallback, mapID);
							}
							else {
								alertScope.consoleLog("[rifd-dsub-maptable.js] WARNING unable to LeafletBaseMapService.setDefaultMapBackground; no geography defined for map: " +
									mapID);
								LeafletBaseMapService.setDefaultBaseMap(mapID);
								setBaseMapCallback(undefined /* No error */, mapID);
							}							
                        };
					
						function setBaseMapCallback(err, areamap) {
							if (err) { // LeafletBaseMapService.setDefaultMapBackground had error
								alertScope.consoleLog("[rifd-dsub-maptable.js] LeafletBaseMapService.setDefaultMapBackground had error: " + 
									err);
							}
								
                            if (CommonMappingStateService.getState("areamap").map.hasLayer($scope.thisLayer)) {
								CommonMappingStateService.getState("areamap").map.removeLayer($scope.thisLayer);
							}
							var getCurrentBaseMap=LeafletBaseMapService.getCurrentBaseMapInUse("areamap");
                            if (!LeafletBaseMapService.getNoBaseMap("areamap")) {
								var currentBaseMapInUse=(($scope.thisLayer && $scope.thisLayer.name) ? $scope.thisLayer.name: undefined);
                                $scope.thisLayer = LeafletBaseMapService.setBaseMap(getCurrentBaseMap);
								CommonMappingStateService.getState("areamap").setBasemap(getCurrentBaseMap, false /* no basemap*/);
								var basemapError=CommonMappingStateService.getState("areamap").getBasemapError(getCurrentBaseMap);
								if (basemapError == 0) {
									$scope.thisLayer.on("load", function() { 
										var basemapError=CommonMappingStateService.getState("areamap").getBasemapError(getCurrentBaseMap);
										if (LeafletBaseMapService.getNoBaseMap("areamap")) { // Has been disabled by error
										
										}
										else if (getCurrentBaseMap != currentBaseMapInUse) {
											if (basemapError == 0) {
												alertScope.showSuccess("Change current base map in use to: " + getCurrentBaseMap);
											}
											else {
												alertScope.showWarning("Unable to change current base map in use from: " + currentBaseMapInUse + 
													"; to: " + getCurrentBaseMap + "; " + basemapError +
													" tiles loaded with errors");
												LeafletBaseMapService.setNoBaseMap("areamap", true); // Disable
											}
										}
										else {	
											if (basemapError == 0) {
												alertScope.consoleLog("[rifd-dsub-maptable.js] setCurrentBaseMapInUse for map: areamap" + 
													"; currentBaseMapInUse: " + currentBaseMapInUse + 
													"; getCurrentBaseMap: " + getCurrentBaseMap);
											}
											else {	
												alertScope.showWarning("Unable to set base map to: " + getCurrentBaseMap + "; " + basemapError +
													" tiles loaded with errors");
												LeafletBaseMapService.setNoBaseMap("areamap", true); // Disable
											}
										}
									});
									$scope.thisLayer.on("tileerror", function() { 
										CommonMappingStateService.getState("areamap").basemapError(getCurrentBaseMap);
									});
												
									$scope.thisLayer.addTo(CommonMappingStateService.getState("areamap").map);
								}
								else {
									alertScope.consoleLog("[rifd-dsub-maptable.js] setBaseMap: " + getCurrentBaseMap +
										" disabled by previous basemapErrors: " + basemapError);
								}
                            }
							else {
								alertScope.consoleLog("[rifd-dsub-maptable.js] setBaseMap: " + getCurrentBaseMap + " disabled by getNoBaseMap");
							}
                            //hack to refresh map
                            $timeout(function () {
                                CommonMappingStateService.getState("areamap").map.invalidateSize();
                            }, 50);
						}
						
                        function renderFeature(feature) {
							if (CommonMappingStateService.getState("areamap").getSelectedPolygonObj($scope.input.name, feature)) {
                                //max possible is six bands according to specs
                                return selectorBands.bandColours[CommonMappingStateService.getState("areamap").getSelectedPolygonObj($scope.input.name, 
									feature).band - 1];
                            }
                            return '#F5F5F5'; //whitesmoke
                        }

                        //Functions to style topoJson on selection changes
                        function style(feature) {
                            return {
                                fillColor: renderFeature(feature.properties.area_id),
                                weight: 1,
                                opacity: 1,
                                color: 'gray',
                                fillOpacity: $scope.transparency
                            };
                        }
                        function handleLayer(layer) {
                            layer.setStyle({
                                fillColor: renderFeature(layer.feature.properties.area_id),
                                fillOpacity: $scope.transparency
                            });
                        }

						function doWatchUpdate(newNames, oldNames) {

                            //Update table selection
                            $scope.gridApi.selection.clearSelectedRows();
							var foundCount=0;
                            for (var i = 0; i < $scope.gridOptions.data.length; i++) {
                                $scope.gridOptions.data[i].band = 0;
								
								if (CommonMappingStateService.getState("areamap").getSelectedPolygonObj($scope.input.name, 
									$scope.gridOptions.data[i].area_id)) {
									foundCount++;
                                    $scope.gridOptions.data[i].band = 
										CommonMappingStateService.getState("areamap").getSelectedPolygonObj($scope.input.name,
											$scope.gridOptions.data[i].area_id).band;
//									if (foundCount < 5) {
//										alertScope.consoleLog("[rifd-dsub-maptable.js] found: " + foundCount + "; area_id: " + 
//											$scope.gridOptions.data[i].area_id + "; data[" + i + "]: " + 
//											JSON.stringify($scope.gridOptions.data[i]));
//									}
                                }
                            }
		
                            //Update the area counter
							if (newNames.length > 0) {
								$scope.selectedPolygon = CommonMappingStateService.getState("areamap").getSelectedPolygon($scope.input.name);
								$scope.selectedPolygonCount = $scope.selectedPolygon.length;
								$scope.input.selectedPolygon = $scope.selectedPolygon; 
								if (!$scope.geoJSON) {
								} else if ($scope.geoJSON && $scope.geoJSON._geojsons && $scope.geoJSON._geojsons.default) {
									//Update map selection    
									$scope.geoJSON._geojsons.default.eachLayer(handleLayer);
								}	
							}
							alertScope.consoleLog("[rifd-dsub-maptable.js] doWatchUpdate() newNames: " + newNames.length +
								"; oldNames: " + oldNames.length +
								"; foundCount: " + foundCount +
								"; $scope.selectedPolygonCount: " + $scope.selectedPolygonCount +
								"; $scope.gridOptions.data: " + $scope.gridOptions.data.length);
													
						}
						
                        //********************************************************************************************************
                        //Watch selectedPolygon array for any changes
                        $scope.$watchCollection('selectedPolygon', function (newNames, oldNames) {
                            if (newNames === oldNames) {
                                return;
                            }
							doWatchUpdate(newNames, oldNames);
                        });
                        //*********************************************************************************************************************
                        //SELECTION METHODS

                        /*
                         * SELECT AREAS USING LEAFLETDRAW
                         */
                        //Add Leaflet.Draw capabilities
                        CommonMappingStateService.getState("areamap").drawnItems = new L.FeatureGroup();
                        CommonMappingStateService.getState("areamap").map.addLayer(CommonMappingStateService.getState("areamap").drawnItems);
                        $scope.drawControl;	
						
						LeafletDrawService.getCircleCapability();
						LeafletDrawService.getPolygonCapability();
						//Add Leaflet.Draw toolbar
						L.drawLocal.draw.toolbar.buttons.circle = "Select by concentric bands";
						L.drawLocal.draw.toolbar.buttons.polygon = "Select by freehand polygons";
						$scope.drawControl = new L.Control.Draw({
							draw: {
								polygon: {
									shapeOptions: {
										color: '#0099cc',
										weight: 4,
										opacity: 1,
										fillOpacity: 0.2
									}
								},
								marker: false,
								polyline: false,
								rectangle: false
							},
							edit: {
								remove: false,
								edit: false,
								featureGroup: CommonMappingStateService.getState("areamap").drawnItems
							}
						});
                        CommonMappingStateService.getState("areamap").map.addControl($scope.drawControl);
						
                        new L.Control.GeoSearch({
                            provider: new L.GeoSearch.Provider.OpenStreetMap()
                        }).addTo(CommonMappingStateService.getState("areamap").map);
                        //add the circle to the map
                        CommonMappingStateService.getState("areamap").map.on('draw:created', function (e) {
                            CommonMappingStateService.getState("areamap").drawnItems.addLayer(e.layer);
                        });
                        //override other map mouse events
                        CommonMappingStateService.getState("areamap").map.on('draw:drawstart', function (e) {
                            $scope.input.bDrawing = true;
                        });

						// To trigger the watchers
						$scope.safeApply = function(count, fn) {
							var phase = this.$root.$$phase;
							if (phase == '$apply' || phase == '$digest') {
								if (fn && (typeof(fn) === 'function')) {
									fn();
								}
								alertScope.consoleLog("[rifd-dsub-maptable.js] (" + count + ") No need to apply(), in progress");
								if (count <= 10) { // try again up to 10 times
									$timeout(function() {
										$scope.safeApply(count++, fn);
										}, 1000);
								}
							} else {
								alertScope.consoleLog("[rifd-dsub-maptable.js] (" + count + ") Call apply() to trigger watchers");
								this.$apply(fn);
							}
						};
						
						// Map redraw function with slight delay for leaflet
						$scope.redrawMap = function() {
							$scope.bringShapesToFront();
									
							CommonMappingStateService.getState("areamap").map.whenReady(function() {
								$timeout(function() {										
										
										alertScope.consoleLog("[rifd-dsub-maptable.js] redraw map");
										CommonMappingStateService.getState("areamap").map.fitBounds(CommonMappingStateService.getState("areamap").map.getBounds()); // Force map to redraw after 0.5s delay
									}, 500);	
							});									
						};
						
                        // completed selection event fired from service
						$scope.$on('completedDrawSelection', function (event, data) {
							
							CommonMappingStateService.getState("areamap").map.spin(true);  // on	
							var riskAnalysisExposureField=undefined;

							start=new Date().getTime();
//							$scope.selectionData.sort(function(a, b){return ((a.area && b.area) ? (a.area - b.area) : false) });
// Already sorted: DO NOT CHANGE
							$scope.shapeLoadUpdate = "0 / " + $scope.selectionData.length + " shapes, 0%";
							async.eachOfSeries($scope.selectionData, 
								function iteratee(item, indexKey, callback) {
								$scope.shapeLoadUpdate = (indexKey) + " / " + $scope.selectionData.length + " shapes, " + 
									(Math.round(((indexKey)/$scope.selectionData.length)*100)) + "%";
								
								try {
									if (indexKey % 50 == 0) {
										$timeout(function() { // Be nice to the stack if you are going to be aggressive!
											DrawSelectionService.makeDrawSelection(item, selectorBands, $scope.input, "areamap", latlngList, callback);
										}, 100);
									}	
									else {	
										async.setImmediate(function() {
											DrawSelectionService.makeDrawSelection(item, selectorBands, $scope.input, "areamap", latlngList, callback);
										});
									}
								}
								catch (e) {
									callback(e.message);
								}
							}, function done(err) {
								if (err) {
									alertScope.showError("[rifd-dsub-maptable.js] completedDrawSelection error: " + err);
								}
								else {
									var end=new Date().getTime();
									var elapsed=(Math.round((end - start)/100))/10; // in S	
									$scope.shapeLoadUpdate = $scope.selectionData.length + " shapes loaded in " + elapsed + " S";
									CommonMappingStateService.getState("areamap").map.spin(false);  // off	
									alertScope.consoleLog("[rifd-dsub-maptable.js] completed Draw Selection in " + elapsed + " S");
								}
								
								// Update maxIntersectCount
								var savedShapes;
								if ($scope.input.name == "ComparisionAreaMap") { 
									savedShapes=SelectStateService.getState().studySelection.comparisonShapes;
								}
								else {
									savedShapes=SelectStateService.getState().studySelection.studyShapes;
								}
								for (var i=0; i<savedShapes.length; i++) {
									var maxIntersectCount;
									var intersectCount;
									var properties;
									if ($scope.input.name == "ComparisionAreaMap") { 
										maxIntersectCount = CommonMappingStateService.getState("areamap").getMaxIntersectCount($scope.input.name, savedShapes[i].rifShapePolyId);
										SelectStateService.getState().studySelection.comparisonShapes[i].properties.maxIntersectCount = maxIntersectCount;
										if (maxIntersectCount) {
											var intersectCount = CommonMappingStateService.getState("areamap").getIntersectCounts(
												$scope.input.name, savedShapes[i].rifShapePolyId);
											SelectStateService.getState().studySelection.comparisonShapes[i].intersectCount = intersectCount;
											for (var key in intersectCount) {
												if (intersectCount[key].total) {
													SelectStateService.getState().studySelection.comparisonShapes[i].properties[key] = 
														intersectCount[key].total;
												}													
											}
										}	
										properties=SelectStateService.getState().studySelection.comparisonShapes[i].properties;
									}
									else { 
										maxIntersectCount = CommonMappingStateService.getState("areamap").getMaxIntersectCount($scope.input.name, savedShapes[i].rifShapePolyId);
										SelectStateService.getState().studySelection.studyShapes[i].properties.maxIntersectCount = maxIntersectCount;
										if (maxIntersectCount) {
											intersectCount = CommonMappingStateService.getState("areamap").getIntersectCounts(
												$scope.input.name, savedShapes[i].rifShapePolyId);
											SelectStateService.getState().studySelection.studyShapes[i].intersectCount = intersectCount;
											for (var key in intersectCount) {
												if (intersectCount[key].total) {
													SelectStateService.getState().studySelection.studyShapes[i].properties[key] = 
														intersectCount[key].total;
												}													
											}
										}	
										properties=SelectStateService.getState().studySelection.studyShapes[i].properties;
									}
									alertScope.consoleLog("[rifd-dsub-maptable.js] process shape[" + i + "] area: " + savedShapes[i].area + 
										"; maxIntersectCount: " + maxIntersectCount +
										"; intersectCount: " + JSON.stringify(intersectCount) +
										"; properties: " + JSON.stringify(properties) +
										"; rifShapePolyId: " + savedShapes[i].rifShapePolyId +
										"; rifShapeId: " + savedShapes[i].rifShapeId +
										"; exposureValue: " + savedShapes[i].exposureValue +
										"; riskAnalysisExposureField: " + savedShapes[i].riskAnalysisExposureField +
										"; shapeFile: " + (savedShapes[i].fileName ? savedShapes[i].fileName : "N/A"));
									if (savedShapes[i].riskAnalysisExposureField) {
										if (riskAnalysisExposureField == undefined) {
											riskAnalysisExposureField=savedShapes[i].riskAnalysisExposureField;	
										}
										else if (riskAnalysisExposureField == savedShapes[i].riskAnalysisExposureField) {
										}
										else {
											alertScope.showError("[rifd-dsub-maptable.js] Multi riskAnalysisExposureFields used: " + 
												riskAnalysisExposureField + "; " + savedShapes[i].riskAnalysisExposureField);
										}
									}
								} // End of for shapes loop
								
								$scope.selectionData = [];
								if (CommonMappingStateService.getState("areamap").info._map == undefined) { // Add back info control
									CommonMappingStateService.getState("areamap").info.addTo(
										CommonMappingStateService.getState("areamap").map);
								}
															
								if ($scope.input.type === "Risk Analysis" && $scope.input.name == "StudyAreaMap" && riskAnalysisExposureField) {
									SubmissionStateService.getState().riskAnalysisExposureField = riskAnalysisExposureField;
								}
								
								if ($scope.selectedPolygonCount > 0) {
									$scope.zoomToSelection(); // Zoom to selection	
								}
								else {
									$scope.zoomToExtent(); // Zoom to extent
								}
								
								$scope.safeApply(0, function() {
									$scope.redrawMap();
								});	
							});
                        });
							
                        // selection event fired from service: rifd-dsub-risk.js etc
                        $scope.$on('makeDrawSelection', function (event, data) {
                            $scope.selectionData.push(data);
                        });
						// rifs-util-leafletdraw.js
                        $scope.$on('makeDrawSelection2', function (event, data) { // Data cannot be added to an array and cannot be copied by angular.copy()
                            DrawSelectionService.makeDrawSelection(data, selectorBands, $scope.input, "areamap", latlngList, 
								function makeDrawSelection2Callback(err) { 
									if (err) {
										alertScope.showError("[rifd-dsub-maptable.js] makeDrawSelection2 error: " + err);
									}
								});
                        });
						
                        //remove drawn items event fired from service
                        $scope.$on('removeDrawnItems', function (event, data) {
                            removeMapDrawItems();
                        });
                        function removeMapDrawItems() {
                            CommonMappingStateService.getState("areamap").drawnItems.clearLayers();
                            CommonMappingStateService.getState("areamap").map.addLayer(CommonMappingStateService.getState("areamap").drawnItems);
                            $scope.input.bDrawing = false; //re-enable layer events
                        }
						
						CommonMappingStateService.getState("areamap").info = L.control();
						CommonMappingStateService.getState("areamap").info.onAdd = function(map) {
							
							alertScope.consoleDebug("[rifd-dsub-maptable.js] create info <div>");
							this._div = L.DomUtil.create('div', 'info');
							this.update();
							return this._div;
						};

						// method that we will use to update the control based on feature properties passed
						CommonMappingStateService.getState("areamap").info.update = function (savedShape, latLng /* Of shape, not mouse! */) {
							
							if (this._div) {
								if (savedShape) {
									var bandCount = {};
									var studySelection=CommonMappingStateService.getState("areamap").getSelectedPolygon($scope.input.name);
									var properties;
									if ($scope.input.name == "ComparisionAreaMap") { 
										var comparisonShapes = SelectStateService.getState().studySelection.comparisonShapes;
										for (var i=0; i<comparisonShapes.length; i++) {
											if (comparisonShapes[i].rifShapePolyId == savedShape.rifShapePolyId) {
												properties=comparisonShapes[i].properties;
											}
										}
									}
									else {
										var studyShapes = SelectStateService.getState().studySelection.studyShapes;
										for (var i=0; i<studyShapes.length; i++) {
											if (studyShapes[i].rifShapePolyId == savedShape.rifShapePolyId) {
												properties=studyShapes[i].properties;
											}
										}
									}
									if (properties == undefined) {
										properties = savedShape.properties;
									}
									if (studySelection) {
										for (var i=0; i<studySelection.length; i++) {
											var band=studySelection[i].band;
											if (bandCount[band]) {
												bandCount[band]++;
											}
											else {
												bandCount[band]=1;
											}
										}
									}
									if (savedShape.circle) {
										this._div.innerHTML = '<h4>Circle;</h4><b>Radius: ' + Math.round(savedShape.radius * 10) / 10 + 'm</b></br>' +
											"<b>Lat: " + Math.round(savedShape.latLng.lat * 1000) / 1000 + // 100m precision
											"&deg;; long: " +  Math.round(savedShape.latLng.lng * 1000) / 1000 +'&deg;</b></br>';
									}
									else {
										var coordinates=savedShape.geojson.geometry.coordinates[0];												
										if (savedShape.freehand) { // Shapefile		
											savedShape=savedShape.coordinates;	
										}	
										
										if (savedShape.freehand) {
											this._div.innerHTML = '<h4>Freehand polygon</h4>';
										}
										else  {
											this._div.innerHTML = '<h4>Shapefile polygon</h4>';
										}
										if (coordinates) {
											this._div.innerHTML+='<b>' + coordinates.length + ' points</b></br>';
										}
									}
									
									if (savedShape.area) {
										this._div.innerHTML+= '<b>Area: ' + savedShape.area + ' square km</b><br />'
									}
										
									for (var property in properties) {
										if (property == 'area') {
											if (savedShape.area === undefined) {
												this._div.innerHTML+= '<b>Area: ' + properties[property] + ' square km</b><br />'
											}
										}
										else if (property != '$$hashKey') {
											this._div.innerHTML+= '<b>' + property + ': ' + properties[property] + '</b><br />';
										}
									}
									this._div.innerHTML += '<b>Band: ' + (savedShape.band || "unknown") + '</b><br />';
								}
								else if (CommonMappingStateService.getState("areamap").shapes.getLayers().length > 0&& $scope.noMouseClocks) {
									this._div.innerHTML = '<h4>Mouse over selection shapes to show properties</br></h4>';
								}
								else if (CommonMappingStateService.getState("areamap").shapes.getLayers().length > 0 && !$scope.noMouseClocks) {
									this._div.innerHTML = '<h4>Mouse over selection shapes to show properties</br>' +
										'Hide selection shapes to mouse over area names</h4>';
								}
								else if ($scope.noMouseClocks) {
									this._div.innerHTML = '<h4>Mouse over area names not available</h4>';
								}
								else {
									this._div.innerHTML = '<h4>Mouse over area names</h4>';
								}
								this._div.innerHTML += '<b>Centroids: ' + $scope.centroid_type + '</b>';
								alertScope.consoleDebug("[rifd-dsub-maptable.js] set info: " + this._div.innerHTML);
							}
							else {
								alertScope.consoleDebug("[rifd-dsub-maptable.js] no info <div>"); 
								
								if (CommonMappingStateService.getState("areamap").shapes == undefined) {
									alertScope.showError("[rifd-dsub-maptable.js] no shapes layerGroup");
								}
								else if (CommonMappingStateService.getState("areamap").map.hasLayer(CommonMappingStateService.getState("areamap").shapes)) {
									alertScope.consoleDebug("[rifd-dsub-maptable.js] add shapes layerGroup");
									if (CommonMappingStateService.getState("areamap").info._map == undefined) { // Add back info control
										CommonMappingStateService.getState("areamap").info.addTo(CommonMappingStateService.getState("areamap").map);
										alertScope.consoleDebug("[rifd-dsub-maptable.js] add info control");
									}
									
									$scope.bShowHideSelectionShapes = true;
									SelectStateService.getState().showHideSelectionShapes = true;
									
									$scope.bringShapesToFront();
								}
								alertScope.consoleDebug("[rifd-dsub-maptable.js] showHideSelectionShapes: " + 
									SelectStateService.getState().showHideSelectionShapes);

							}
							
/* The aim of this bit of code was to display the area. However "layer.fireEvent('mouseover');" breaks the selection and
   the latLng is the shape, not the position of the mouse. Encourage user to use show/hide selection instead
   
                            if (!$scope.input.bDrawing && 
							    angular.isDefined($scope.geoJSON && $scope.geoJSON._geojsons && $scope.geoJSON._geojsons.default)) {
							
								$scope.geoJSON._geojsons.default.eachLayer(function (layer) {	
									if (savedShape) {
										layer.fireEvent('mouseover'); // Breaks selection		
									}
									else {
										layer.fireEvent('mouseout');  	
									}
								});
							} */
						};
						
                        /*
                         * SELECT AREAS FROM A LIST, CSV
                         */
                        $scope.openFromList = function () {
                            $scope.modalHeader = "Upload ID file";
                            $scope.accept = ".csv";
                            $scope.showContent = function ($fileContent, $fileName) {
                                $scope.content = $fileContent.toString();
								$scope.fileName = $fileName;
                            };
                            $scope.uploadFile = function () {
								/* Upload CSV file. Required fields: ID,Band. Name is 
								   included to make the file more understandable. Ideally
								   this function should be made capitalisation insensitive
								   and more flexible in the names, i.e. ID/areaId/area_id
								   and Band/bandId/band_id
								
								e.g.
								ID,NAME,Band
								01779778,California,1
								01779780,Connecticut,1
								01705317,Georgia,1
								01779785,Iowa,1
								01779786,Kentucky,1
								01629543,Louisiana,1
								01779789,Michigan,1
								01779795,New Jersey,1
								00897535,New Mexico,1
								01455989,Utah,1
								01779804,Washington,1
								
								Structure of parsed JSON:
								
								listOfIDs=[
								  {
									"ID": "01785533",
									"NAME": "Alaska",
									"Band": "1"
								  },
								  ...
								  {
									"ID": "01779804",
									"NAME": "Washington",
									"Band": "1"
								  }
								]; 
								 */
                                try {
                                    //parse the csv file
                                    var listOfIDs = JSON.parse(JSONService.getCSV2JSON($scope.content));
                                    //attempt to fill 'selectedPolygon' with valid entries
                                    $scope.clear();	
											
                                    var bPushed = false;
                                    var bInvalid = false;
                                    for (var i = 0; i < listOfIDs.length; i++) {
                                        for (var j = 0; j < $scope.gridOptions.data.length; j++) {
                                            if ($scope.gridOptions.data[j].area_id === listOfIDs[i].ID) {
                                                var thisBand = Number(listOfIDs[i].Band);
//												alertScope.consoleLog("[rifd-dsub-maptable.js] [" + i + "," + j + "] MATCH area_id: " + $scope.gridOptions.data[j].area_id + 
//													"; ID: " + listOfIDs[i].ID +
//													"; thisBand: " + thisBand);
                                                if (CommonMappingStateService.getState("areamap").possibleBands.indexOf(thisBand) !== -1) {
                                                    bPushed = true;
                                                    CommonMappingStateService.getState("areamap").getSelectedPolygon($scope.input.name).push({
														id: $scope.gridOptions.data[j].area_id, 
														gid: $scope.gridOptions.data[j].area_id,
                                                        label: $scope.gridOptions.data[j].label, 
														band: Number(listOfIDs[i].Band)});
                                                    break;
                                                } else {
                                                    bInvalid = true;
                                                }
                                            }
                                        }
                                    }
                                    if (!bPushed) {
                                        alertScope.showWarning("No valid 'ID' fields or 'Band' numbers found in your list");
//										alertScope.consoleDebug("[rifd-dsub-maptable.js] " + JSON.stringify(listOfIDs, null, 2));
                                    } else if (!bInvalid) {
                                        alertScope.showSuccess("List uploaded sucessfully");
                                    } else {
                                        alertScope.showSuccess("List uploaded sucessfully, but some 'ID' fields or 'Band' numbers were not valid");
                                    }
                                } catch (e) {
                                    alertScope.showError("Could not read or process the file: Please check formatting");
                                }
                            };
							
                            var modalInstance = $uibModal.open({
                                animation: true,
                                templateUrl: 'dashboards/submission/partials/rifp-dsub-fromfile.html',
                                controller: 'ModalFileListInstanceCtrl',
                                windowClass: 'stats-Modal',
                                backdrop: 'static',
                                scope: $scope,
                                keyboard: false
                            });
                        }; // End of $scope.openFromList()
						
//
// EXPERIMENTAL pointer event code related to issue: #66
//						
						// returns a list of all elements under the cursor
						// https://gist.github.com/Rooster212/4549f9ab0acb2fc72fe3
/*						function elementsFromPoint(x,y) {
							var elements = [], previousPointerEvents = [], current, i, d;

							if(typeof document.elementsFromPoint === "function")
								return document.elementsFromPoint(x,y);
							if(typeof document.msElementsFromPoint === "function")
								return document.msElementsFromPoint(x,y);
							
							// get all elements via elementFromPoint, and remove them from hit-testing in order
							while ((current = document.elementFromPoint(x,y)) && elements.indexOf(current)===-1 && current != null) {
								  
								// push the element and its current style
								elements.push(current);
								previousPointerEvents.push({
									value: current.style.getPropertyValue('pointer-events'),
									priority: current.style.getPropertyPriority('pointer-events')
								});
								  
								// add "pointer-events: none", to get to the underlying element
								current.style.setProperty('pointer-events', 'none', 'important'); 
							}

							// restore the previous pointer-events values
							for(i = previousPointerEvents.length; d=previousPointerEvents[--i]; ) {
								elements[i].style.setProperty('pointer-events', d.value?d.value:'', d.priority); 
							}
							  
							// return our results
							return elements;
						} */
						// Modified from: https://gist.github.com/perliedman/84ce01954a1a43252d1b917ec925b3dd
						// NOT CURRENTLY IN USE
						function shapesClickThrough(e, map, geojsonLayers) {
							if (e._stopped) { 
//								alertScope.consoleDebug("[rifd-dsub-maptable.js] shapesClickThrough " +  
//									"(" + e.target._leaflet_id + "): " + e.type + "; STOPPED");
									L.DomEvent.stop(e);
								return; 
							}

							var target = e.target;
							var stopped;
							var removed;
							var ev = new MouseEvent(e.type, e)

							removed = {node: target, display: target.style.display};
							target.style.display = 'none';
							
/*						if ("areamap") {
								var layerPoint = map.latLngToLayerPoint(latlngList[0].latLng);
								var containerPoint = map.latLngToContainerPoint(latlngList[0].latLng);
								var mousePoint = map.mouseEventToContainerPoint(e);
								alertScope.consoleDebug("[rifd-dsub-maptable.js] shapesClickThrough; latlngList.length: " + latlngList.length +
									"; latlngList[0]: " + JSON.stringify(latlngList[0]) + 
									"; client: [" + e.clientX + "," + e.clientY + "]" + 
//									"; layerPoint: [" + layerPoint.x + "," + layerPoint.y + "]" + 
//									"; containerPoint: [" + containerPoint.x + "," + containerPoint.y + "]" +
									"; mousePoint: [" + mousePoint.x + "," + mousePoint.y + "]");
							}
							else {
								alertScope.consoleDebug("[rifd-dsub-maptable.js] shapesClickThrough; latlngList.length: " + latlngList.length +
									"; latlngList[0]: " + JSON.stringify(latlngList[0]) + "; client: [" + e.clientX + "," + e.clientY + "]");
							} */
							
							// Look for closest geoJSON layer. Mouse could be anywhere in the shape so centroids will not match					
							var geojsonLayer=L.GeometryUtil.closestLayer(map, geojsonLayers, map.mouseEventToLatLng(e));
							var properties;
							var leafletId;
							var leafletIdFound=false;
							if (geojsonLayer) {
								properties = geojsonLayer.layer.feature.properties;
								leafletId = geojsonLayer.layer._leaflet_id;
							}
							
							// This does not work as the geoJSON tile layer is blocking mouse clicks for some reason
/*							var elementList = elementsFromPoint(e.clientX, e.clientY);
							for (var k=0; k<elementList.length; k++) {
								target=elementList[k];
								if (target && target !== shapes && target._leaflet_id) { // Leaflet only targets												
									stopped = !target.dispatchEvent(ev);
									if (stopped || ev._stopped) {
										L.DomEvent.stop(e);
									}
									if (target._leaflet_id && leafletId && target._leaflet_id == leafletId) {
										leafletIdFound=true;
									}
									alertScope.consoleDebug("[rifd-dsub-maptable.js] shapesClickThrough[" + k + "/" + elementList.length + "] " +  
										"(" + e.target._leaflet_id + "; for: " + (e.currentTarget._leaflet_id || 'N/A') + "): " + e.type + 
										"; PROPAGATE to: (" +
										(target._leaflet_id || 'N/A') + ")" +
										"; leafletIdFound: " + leafletIdFound); 
								}
								else if (!target._leaflet_id) {
									alertScope.consoleDebug("[rifd-dsub-maptable.js] shapesClickThrough[" + k + "/" + elementList.length + "] " +  
										"(no target._leaflet_id for: " + e.currentTarget._leaflet_id + "): " + e.type); 
								}
								else if (!target) {
									alertScope.consoleDebug("[rifd-dsub-maptable.js] shapesClickThrough[" + k + "/" + elementList.length + "] " +  
										"(no target for: " + e.currentTarget._leaflet_id + "): " + e.type); 
								}
								else if (target === shapes) {
									alertScope.consoleDebug("[rifd-dsub-maptable.js] shapesClickThrough[" + k + "/" + elementList.length + "] " +  
										"(target === shapes for: " + e.currentTarget._leaflet_id + "): " + e.type); 
								} 
							} */

							if (!leafletIdFound) { // Did not propagate (clicks disabled by early 1.0 leaflet; may work with later versions)	
								var mousePoint = map.mouseEventToContainerPoint(e);	
								if (geojsonLayer && map && leafletId && properties) {
//									map._layers[leafletId].fire(e.type);
									// Do manually. This again does not work as you only get one click per entry/exit from a shape layer
/*									if (e.type == 'mouseover') {
                                        $scope.thisPolygon = properties.name;
									}
									else if (e.type == 'mouseout')  {
                                        $scope.thisPolygon = "";
									}
									alertScope.consoleDebug("[rifd-dsub-maptable.js] shapesClickThrough direct to geoJSON for: " + 
										(e.currentTarget._leaflet_id || 'N/A') + "): " + e.type + 
										"; name: " + properties.name  +
										"; leafletId: " + leafletId +
										"; mousePoint: [" + mousePoint.x + "," + mousePoint.y + "]");
                                    $scope.$digest(); */
								}
								else {
									alertScope.consoleError("[rifd-dsub-maptable.js] unable shapesClickThrough direct to geoJSON for: " + 
										(e.currentTarget._leaflet_id || 'N/A') + "): " + e.type + 
										"; properties: " + JSON.stringify(properties)  +
										"; mousePoint: [" + mousePoint.x + "," + mousePoint.y + "]");
								}
							} 							
							removed.node.style.display = removed.display;
						}
//						L.DomEvent.on(shapes, 'mouseover', function(e) {
//							shapesClickThrough(e, CommonMappingStateService.getState("areamap").map, $scope.geoJSONLayers);
//						});
//						L.DomEvent.on(shapes, 'mouseout', function(e) {
//							shapesClickThrough(e, CommonMappingStateService.getState("areamap").map, $scope.geoJSONLayers);
//						}); 
						
                    }
                };
            }]);