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
 * DIRECTIVE for map and table linked area selections
 * TODO: This prob needs refactoring / overhauling to fit in with the mapping controllers
 * although it does work fine as it is
 */

/* global L, d3, key, topojson */
angular.module("RIF")
        .directive('submissionMapTable', ['ModalAreaService', 'LeafletDrawService', '$uibModal', 'JSONService', 'mapTools',
            'GISService', 'LeafletBaseMapService', '$timeout', 'user', 'SubmissionStateService', 
			'SelectStateService', 'ParametersService', 'StudyAreaStateService', '$q', '$timeout',
            function (ModalAreaService, LeafletDrawService, $uibModal, JSONService, mapTools,
                    GISService, LeafletBaseMapService, $timeout, user, SubmissionStateService,
					SelectStateService, ParametersService, StudyAreaStateService, $q, $timeout) {
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
						
                        $scope.areamap = L.map('areamap', {condensedAttributionControl: false}).setView([0, 0], 1);	

						var shapes = $scope.areamap.createPane('shapes');
						$scope.areamap.getPane('shapes').style.zIndex = 650; // set shapes to show on top of markers but below pop-ups					
						
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
							
/*						if (map) {
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
//							shapesClickThrough(e, $scope.areamap, $scope.geoJSONLayers);
//						});
//						L.DomEvent.on(shapes, 'mouseout', function(e) {
//							shapesClickThrough(e, $scope.areamap, $scope.geoJSONLayers);
//						}); 
										
						SubmissionStateService.setAreaMap($scope.areamap);
						$scope.bShowHideSelectionShapes=(SelectStateService.getState().showHideSelectionShapes || true);
						$scope.bShowHideCentroids=SelectStateService.getState().showHideCentroids;
                        $scope.thisLayer = LeafletBaseMapService.setBaseMap(LeafletBaseMapService.getCurrentBaseMapInUse("areamap"));

                        //Reference the child scope
                        //will be from the comparison area or study area controller
                        $scope.child = {};
                        var alertScope = $scope.$parent.$$childHead.$parent.$parent.$$childHead;
						$scope.areamap.on('remove', function(e) {
                            alertScope.consoleDebug("[rifd-dsub-maptable.js] removed shared areamap");
						});
						$scope.areamap.on('error', function(errorEvent){
                            alertScope.consoleError("[rifd-dsub-maptable.js] error in areamap" +
								(errorEvent.message || "(no message)"));
						});	

                        /*
                         * LOCAL VARIABLES
                         */
                        //map max bounds from topojson layer
                        var maxbounds;
                        //If geog changed then clear selected
                        var thisGeography = SubmissionStateService.getState().geography;
                        if (thisGeography !== $scope.input.geography) {
                            $scope.input.selectedPolygon.length = 0;
                            $scope.input.selectAt = "";
                            $scope.input.studyResolution = "";
                            $scope.input.geography = thisGeography;
                        }
						
						// Also defined in rifs-util-leafletdraw.js

                        //selectedPolygon array synchronises the map <-> table selections  
                        $scope.selectedPolygon = $scope.input.selectedPolygon;                       
                        $scope.selectedPolygonCount = $scope.selectedPolygon.length; //total for display
						$scope.selectedPolygonObj = {};
						for (var i = 0; i < $scope.selectedPolygon.length; i++) { // Maintain keyed list for faster checking
							$scope.selectedPolygonObj[$scope.selectedPolygon[i].id] = $scope.selectedPolygon[i];
						}
						
						$scope.shapeLoadUpdate = "";
                        //band colour look-up for selected districts
                        $scope.possibleBands = $scope.input.bands;
                        $scope.currentBand = 1; //from dropdown
                        //d3 polygon rendering, changed by slider
                        $scope.transparency = $scope.input.transparency;
						$scope.geoJSONLoadCount = 0;
						
						var eachFeatureArray = [];
                        var bWeightedCentres = true;
						var popWeightedCount=0;
						var dbCentroidCount=0;
						
                        /*
                         * TOOL STRIP 
                         * These repeat stuff in the leafletTools directive - possible refactor
                         */
                        //Clear all selection from map and table
                        $scope.clear = function () {
                            $scope.selectedPolygon.length = 0;
							$scope.selectedPolygonObj = {};
							$scope.selectedPolygonCount = 0;
                            $scope.input.selectedPolygon.length = 0;
							$scope.selectedPolygonCount = 0;
							$scope.shapeLoadUpdate = "";
							if ($scope.input.type === "Risk Analysis") {
								SelectStateService.initialiseRiskAnalysis();
							}
							else {			
								SelectStateService.resetState();
							}
							
                            if ($scope.areamap.hasLayer($scope.shapes)) {
                                $scope.areamap.removeLayer($scope.shapes);
								$scope.shapes = new L.layerGroup();
								$scope.areamap.addLayer($scope.shapes);
								
								$scope.info.update();
                            }
							
							if (maxbounds) { //  Zoom back to maximum extent of geolevel
								$scope.areamap.fitBounds(maxbounds);
							}
                        };
						
						// Bring shapes to front by descending band order; lowest in front (so mouseover/mouseout works!)
						$scope.bringShapesToFront = function() {
							var layerCount=0;
							var maxBands=0;
							var shapeLayerOptionsBanderror=0;
							var shapeLayerBringToFrontError=0;
							
							if ($scope.shapes) {
								var shapesLayerList=$scope.shapes.getLayers();
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
											shapesLayerAreas[shapeLayer.options.area].push($scope.shapes.getLayerId(shapeLayer));
										}
										else {
											useBands=true;
										}
										shapesLayerBands[shapeLayer.options.band].push($scope.shapes.getLayerId(shapeLayer));
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
									if ($scope.areaNameList == undefined) {
										$scope.createAreaNameList();
									}
									
									for (var k=0; k<shapesLayerAreaList.length; k++) {
											
										for (var area in shapesLayerAreas) {
											if (area == shapesLayerAreaList[k]) {
												var areaIdList=shapesLayerAreas[area];
												for (var l=0; l<areaIdList.length; l++) {
													var shapeLayer=$scope.shapes.getLayer(areaIdList[l]);
													if (shapeLayer && typeof shapeLayer.bringToFront === "function") { 
														if ($scope.areaNameList == undefined) {
															alertScope.consoleDebug("[rifd-dsub-maptable.js] bring layer: " + areaIdList[l] + " to front" +
																"; band: " + shapeLayer.options.band +
																"; area: " + shapeLayer.options.area +
																"; polygons: unknwon");
															shapeLayer.bringToFront();
														}
														else if (shapeLayer.options.band && $scope.areaNameList &&
															$scope.areaNameList[shapeLayer.options.band] &&
														    $scope.areaNameList[shapeLayer.options.band].length > 0) {
															alertScope.consoleDebug("[rifd-dsub-maptable.js] bring layer: " + areaIdList[l] + " to front" +
																"; band: " + shapeLayer.options.band +
																"; area: " + shapeLayer.options.area +
																"; polygons: " + $scope.areaNameList[shapeLayer.options.band].length);
															shapeLayer.bringToFront();
														}
														else {
															alertScope.consoleDebug("[rifd-dsub-maptable.js] ignore layer: " + areaIdList[l] + " to front" +
																"; band: " + shapeLayer.options.band +
																"; area: " + shapeLayer.options.area +
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
											var shapeLayer=$scope.shapes.getLayer(shapesLayerBands[j][k]);
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
                            $scope.selectedPolygon.length = 0;
                            for (var i = 0; i < $scope.gridOptions.data.length; i++) {
                                $scope.selectedPolygon.push({
									id: $scope.gridOptions.data[i].area_id, 
									gid: $scope.gridOptions.data[i].area_id, 
									label: $scope.gridOptions.data[i].label, 
									band: $scope.currentBand});
                            }
							$scope.selectedPolygonCount = $scope.selectedPolygon.length; //total for display
							$scope.selectedPolygonObj = {};
							for (var i = 0; i < $scope.selectedPolygon.length; i++) { // Maintain keyed list for faster checking
								$scope.selectedPolygonObj[$scope.selectedPolygon[i].id] = $scope.selectedPolygon[i];
							}
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
                            var i = $scope.selectedPolygon.length;
                            while (i--) {
                                if ($scope.selectedPolygon[i].band === $scope.currentBand) {
									delete $scope.selectedPolygonObj[$scope.selectedPolygon[i].id];
                                    $scope.selectedPolygon.splice(i, 1); // delete element i
                                }
                            }
							$scope.selectedPolygonCount = $scope.selectedPolygon.length; //total for display
                        };
                        //Zoom to layer
                        $scope.zoomToExtent = function () {
                            $scope.areamap.fitBounds(maxbounds);
                        };
                        //Zoom to selection
                        $scope.zoomToSelection = function () {
                            var studyBounds = new L.LatLngBounds();
                            if (angular.isDefined($scope.geoJSON && $scope.geoJSON._geojsons && $scope.geoJSON._geojsons.default)) {
                                $scope.geoJSON._geojsons.default.eachLayer(function (layer) {
									if ($scope.selectedPolygonObj[layer.feature.properties.area_id]) {
                                        studyBounds.extend(layer.getBounds());
                                    }
                                });
                                if (studyBounds.isValid()) {
                                    $scope.areamap.fitBounds(studyBounds);
                                }
                            }
                        };
                        //Show-hide centroids
                        $scope.showCentroids = function () {
					
							$scope.areamap.spin(true);  // on
							
							// Delays are to help the spinner
							$timeout(function() {
								if ($scope.areamap.hasLayer(centroidMarkers)) {
									$scope.bShowHideCentroids = false;
									SelectStateService.getState().showHideCentroids = false;
									$scope.areamap.removeLayer(centroidMarkers);
								} else {
									$scope.bShowHideCentroids = true;
									SelectStateService.getState().showHideCentroids = true;
									$scope.areamap.addLayer(centroidMarkers);
								}
							}, 500);
							
							$timeout(function() {
								$scope.areamap.whenReady(function () {
									$scope.areamap.spin(false);  // off
								});
							}, 500);
                        }; 
						
                        // Show-hide shapes and associated info
						$scope.showShapes = function () {
                            if ($scope.shapes == undefined) {
								alertScope.showError("[rifd-dsub-maptable.js] no shapes layerGroup");
							}
							else if ($scope.areamap.hasLayer($scope.shapes)) {
                                $scope.areamap.removeLayer($scope.shapes);
								alertScope.consoleDebug("[rifd-dsub-maptable.js] remove shapes layerGroup");
								if ($scope.info._map) { // Remove info control
									$scope.info.remove();
									alertScope.consoleDebug("[rifd-dsub-maptable.js] remove info control");
								}
								
								$scope.bShowHideSelectionShapes = false;
								SelectStateService.getState().showHideSelectionShapes = false;

                            } 
							else {
                                $scope.areamap.addLayer($scope.shapes);
								alertScope.consoleDebug("[rifd-dsub-maptable.js] add shapes layerGroup");
								if ($scope.info._map == undefined) { // Add back info control
									$scope.info.addTo($scope.areamap);
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
                            //offer the correct number of bands
                            SubmissionStateService.getState().studyType = $scope.input.type;
							StudyAreaStateService.getState().type = $scope.input.type;
                            if ($scope.input.type === "Risk Analysis") {
                                $scope.possibleBands = [1, 2, 3, 4, 5, 6];
                                $scope.areamap.band = 6;
								
								SelectStateService.initialiseRiskAnalysis();
								SelectStateService.getState().studyType="risk_analysis_study";
                            } else {
                                $scope.possibleBands = [1];
                                $scope.currentBand = 1;
                                $scope.areamap.band = 1;
								
								SelectStateService.resetState();
								SelectStateService.getState().studyType="disease_mapping_study";
                            }
//							SelectStateService.verifyStudySelection(); 	// Don't - it is not setup
						};
						
						eachFeaureFunction = function (feature, layer, eachFeatureCallback) {
							try {
//								$scope.geoJSONLayers.push(layer);
								//get as centroid marker layer. 
								if (!bWeightedCentres || 										// Not using weighted centres 
									latlngListById[feature.properties.area_id] == undefined) {	// No weighted centres for this area
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
									
									if (latlngListById[feature.properties.area_id]) {
										latlngListDups++;
									}
									else {
										latlngListById[feature.properties.area_id] = {
											latLng: L.latLng([p.lat, p.lng]), 
											name: p.name,
											circleId: centroidMarkers.getLayerId(circle)
										}
									}
								}
								else { // Using database centroids
									feature.properties.latLng = latlngListById[feature.properties.area_id].latLng;
								}
								feature.properties.circleId = latlngListById[feature.properties.area_id].circleId;
								
								
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
						
						function setupMap() { // Return promise
							// Called on DOM render completion to ensure basemap is rendered
							return $timeout(function () {
								//add baselayer
								$scope.renderMap("areamap");

								//Store the current zoom and view on map changes
								$scope.areamap.on('zoomend', function (e) {
									$scope.input.center.zoom = $scope.areamap.getZoom();
								});
								$scope.areamap.on('moveend', function (e) {
									$scope.input.center.lng = $scope.areamap.getCenter().lng;
									$scope.input.center.lat = $scope.areamap.getCenter().lat;
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
								}).addTo($scope.areamap);

								//Custom toolbar
								var tools = mapTools.getSelectionTools($scope);
								for (var i = 0; i < tools.length; i++) {
									new tools[i]().addTo($scope.areamap);
								}

								//scalebar and fullscreen
								L.control.scale({position: 'bottomleft', imperial: false}).addTo($scope.areamap);
								$scope.areamap.addControl(new L.Control.Fullscreen());

								//drop down for bands
								var dropDown = mapTools.getBandDropDown($scope);
								new dropDown().addTo($scope.areamap);

								//Set initial map extents
								$scope.center = $scope.input.center;
	//
	// TO STOP LEAFLET NOT DISPLAYING SELECTED AREAS (experimental)
	//                            $scope.areamap.setView([$scope.center.lat, $scope.center.lng], $scope.center.zoom);

								//Attributions to open in new window
								L.control.condensedAttribution({
									prefix: '<a href="http://leafletjs.com" target="_blank">Leaflet</a>'
								}).addTo($scope.areamap);

								$scope.areamap.doubleClickZoom.disable();
								$scope.areamap.band = Math.max.apply(null, $scope.possibleBands);
							});		
						}
						
                        /*
                         * RENDER THE MAP AND THE TABLE
                         */
                        getMyMap = function () {

                            if ($scope.areamap.hasLayer($scope.geoJSON)) {
                                $scope.areamap.removeLayer($scope.geoJSON);
                            }

                            var topojsonURL = user.getTileMakerTiles(user.currentUser, thisGeography, $scope.input.selectAt); //  With no x/y/z returns URL
                            latlngList = []; // centroids!
                            latlngListById = []; // centroids!
                            centroidMarkers = new L.layerGroup();
							
                            //Get the centroids from DB
                            bWeightedCentres = true;
							
                            user.getTileMakerCentroids(user.currentUser, thisGeography, $scope.input.selectAt).then(function (res) { // Success case 
								return processCentroids(res);	
                            }, function (err) { // Error case
								promisesErrorHandler("user.getTileMakerCentroids", err);
								
                                //couldn't get weighted centres so generate geographic with leaflet
                                alertScope.showWarning("Could not find (weighted) centroids stored in database - calculating geographic centroids on the fly");
                                bWeightedCentres = false;
								$scope.centroid_type="Leaflet calculated geographic";
                            }).then(function (res) { // No res etc
								//Get max bounds
								user.getGeoLevelSelectValues(user.currentUser, thisGeography).then(function (res) {
									var lowestLevel = res.data[0].names[0];
									user.getTileMakerTilesAttributes(user.currentUser, thisGeography, lowestLevel).then(function (res) {
										maxbounds = L.latLngBounds([res.data.bbox[1], res.data.bbox[2]], [res.data.bbox[3], res.data.bbox[0]]);
										if (Math.abs($scope.input.center.lng) < 1 && Math.abs($scope.input.center.lat < 1)) {
											$scope.areamap.fitBounds(maxbounds);
										}
									}).then(function (res) {
										alertScope.consoleDebug("[rifd-dsub-maptable.js] user.getTileMakerTilesAttributes OK: " + 
											(res ? res : "no status"))
										return asyncCreateTopoJsonLayer(topojsonURL);
									}).then(function (res) {
										alertScope.consoleDebug("[rifd-dsub-maptable.js] asyncCreateTopoJsonLayer OK: " + 
											(res ? res : "no status"));
										return processEachFeatureArray(); // Adds centroids using a promise 
									}, function(err) { // Error case
										promisesErrorHandler("processEachFeatureArray", err);
																					
									}).then(function (res) {
										alertScope.consoleDebug("[rifd-dsub-maptable.js] processEachFeatureArray OK: " + (res ? res : "no status"));
									                   
										//Get overall layer properties
										// Edge appears to crash here when retrieving study selection data, but not when choosing
										user.getTileMakerAttributes(user.currentUser, thisGeography, $scope.input.selectAt).then(function (res) {
											alertScope.consoleDebug("[rifd-dsub-maptable.js] user.getTileMakerAttributes: " + 
												(res ? res : "no status"));
											if (angular.isUndefined(res.data.attributes)) {
												alertScope.showError("Could not get tile attributes from database");
												return; // Stop processing
											}      
											else {								
												$scope.totalPolygonCount = res.data.attributes.length;
											
												//populate the table
												$scope.gridOptions.data = ModalAreaService.fillTable(res.data);		
												alertScope.consoleDebug("[rifd-dsub-maptable.js] ModalAreaService.fillTable: " + 
													$scope.totalPolygonCount + " rows");		
												return checkSelectedPolygonList(res.data); // promise						
											}
										}, function (err) {
											promisesErrorHandler("user.getTileMakerAttributes", err);
											/*
										}).then(function (res) {
											alertScope.consoleDebug("[rifd-dsub-maptable.js] checkSelectedPolygonList: " + 
												(res ? res : "no status"));
											// Add back selected shapes
											
											return processEachFeatureArray(); // Adds centroids using a promise 
										}, function(err) { // Error case
											promisesErrorHandler("checkSelectedPolygonList", err);
											*/
										}).then(function (res) {
											alertScope.consoleDebug("[rifd-dsub-maptable.js] checkSelectedPolygonList: " + 
//											alertScope.consoleDebug("[rifd-dsub-maptable.js] processEachFeatureArray: " + 
												(res ? res : "no status"));
											// Add selected shapes
											return addSelectedShapes(); // promise
										}, function(err) { // Error case
											promisesErrorHandler("addSelectedShapes", err);
										}).then(function (res) {
											alertScope.consoleDebug("[rifd-dsub-maptable.js] addSelectedShapes: " + 
												(res ? res : "no status"));
												
											$scope.zoomToSelection(); // Zoom to selection	
											$timeout(function() {	

												$scope.areamap.spin(false);  // off	
												enableMapSpinners();											
												$scope.redrawMap();
											}, 100);
										}, function(err) { // Error case
											promisesErrorHandler("addSelectedShapes", err);
										})
									});
								});
							}, function(err) { // Error case
								promisesErrorHandler("user.getTileMakerCentroids", err);
							});		
                        }; // End of getMyMap()

						function promisesErrorHandler(functionName, err) { // Abort the chain of promises processing
							alertScope.consoleError("[rifd-dsub-maptable.js] " + functionName + " had error: " + 
								(err ? err : "no error specified"));
							$scope.areamap.spin(false);  // off	
							enableMapSpinners();
							throw new Error("promisesErrorHandler: " + functionName + " had error: " + 
								(err ? err : "no error specified"));
						}
						
                        /*
                         * GET THE SELECT AND VIEW RESOLUTIONS
                         */
                        $scope.geoLevels = [];
                        $scope.geoLevelsViews = [];
						
						setupMap().then(function(res) {
							user.getGeoLevelSelectValues(user.currentUser, thisGeography).then(handleGeoLevelSelect, handleGeographyError);
						},
						function(err) {
							promisesErrorHandler("setupMap", err);
						});

                        $scope.geoLevelChange = function () {
                            //Clear the map
                            $scope.selectedPolygon.length = 0;
							$scope.selectedPolygonObj = {};
							$scope.selectedPolygonCount = 0;
                            $scope.input.selectedPolygon.length = 0;
							$scope.selectedPolygonCount = 0;
							$scope.geoJSONLoadCount = 0;
                            if ($scope.areamap.hasLayer(centroidMarkers)) {
								$scope.bShowHideCentroids = false;
								SelectStateService.getState().showHideCentroids = false;
                                $scope.areamap.removeLayer(centroidMarkers);
                            }
                            user.getGeoLevelViews(user.currentUser, thisGeography, $scope.input.selectAt).then(handleGeoLevelViews, handleGeographyError);
                        };
						
						function processCentroids(res) {
							return $q(function(resolve, reject) {
								var latlngListDups=0;
								var latlngListWarnings=0;
								$scope.centroid_type="Not known";
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

									if (res.data.smoothed_results[i] && res.data.smoothed_results[i].pop_x && res.data.smoothed_results[i].pop_y) {
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

											if (latlngListById[p.id]) {
												latlngListDups++;
											}
											else {
												latlngListById[p.id] = {
													latLng: pwLatLng, 
													name: p.name,
													popWeighted: true,
													circleId: centroidMarkers.getLayerId(circle)
												}
											}
											
											if (res.data.smoothed_results[i] && res.data.smoothed_results[i].x && res.data.smoothed_results[i].y) {
												dbCentroidCount++;
											}
										}
									}
									else if (res.data.smoothed_results[i] && res.data.smoothed_results[i].x && res.data.smoothed_results[i].y) {
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

											if (latlngListById[p.id]) {
												latlngListDups++;
											}
											else {
												latlngListById[p.id] = {
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
								var pctPopWeighted=Math.round(10000*popWeightedCount/dbCentroidCount)/100;
									
								if (res.data.smoothed_results.length == popWeightedCount) {
									$scope.centroid_type="population weighted";
								}
								else if (res.data.smoothed_results.length == dbCentroidCount) {
									$scope.centroid_type="database geographic";
								}
								else if (0 == dbCentroidCount) {
									throw new Error("user.getTileMakerCentroids: dbCentroidCount=0");
								}
								else {
									$scope.centroid_type=pctPopWeighted + "% population weighted";
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
									
								$scope.info.update();
								
								resolve("processed centroids");						
							}); // End of $q constructor
						}
							
						function asyncCreateTopoJsonLayer(topojsonURL) {
							$scope.geoJSONLoadCount=0;
							return $q(function(resolve, reject) {
								
								var latlngListDups=0;
								eachFeatureArray = [];
								$scope.areamap.spin(true);  // on
                                $scope.geoJSON = new L.topoJsonGridLayer(topojsonURL, {
                                   attribution: 'Polygons &copy; <a href="http://www.sahsu.org/content/rapid-inquiry-facility" target="_blank">Imperial College London</a>',
                                   interactive: true,
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
														var thisPoly = e.target.feature.properties.area_id;
														var bFound = false;
														for (var i = 0; i < $scope.selectedPolygon.length; i++) {
															if ($scope.selectedPolygon[i].id === thisPoly) {
																bFound = true;
																delete $scope.selectedPolygonObj[$scope.selectedPolygon[i].id];
																$scope.selectedPolygon.splice(i, 1); // delete element i
																break;
															}
														}
														if (!bFound) {
															$scope.selectedPolygon.push({
																id: feature.properties.area_id, 
																gid: feature.properties.gid, label: 
																feature.properties.name, 
																band: $scope.currentBand});
															 $scope.selectedPolygonObj[feature.properties.area_id] = {
																id: feature.properties.area_id, 
																gid: feature.properties.gid, label: 
																feature.properties.name, 
																band: $scope.currentBand}; 
														}
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
									$scope.areamap.addLayer($scope.geoJSON);
								}
							}); // End of $q constructor
						}
					
						function checkSelectedPolygonList(data) {
							
							return $q(function(resolve, reject) {

								var foundCount=0;
								var dupCount=0;
								var dupBandCount=0;
								
								$scope.selectedPolygon.sort(function(a, b) {
									if (a.id < b.id) {
										return -1;
									}
									else if (a.id > b.id) {
										return 1;
									}
									else { // Same
										return 0;
									}
								}); // Alphabetically by id!
								alertScope.consoleDebug("[rifd-dsub-maptable.js] sorted $scope.selectedPolygon: " + 
									$scope.selectedPolygon.length);
								
								$scope.selectedPolygonObj = {};
								for (var i = 0; i < $scope.selectedPolygon.length; i++) { // Check for duplicates
//									$scope.selectedPolygon[i].found=false;
									if (i > 0 && $scope.selectedPolygon[i].id === $scope.selectedPolygon[i-1].id) {
										if (i > 0 && $scope.selectedPolygon[i].band === $scope.selectedPolygon[i-1].band) {
											dupBandCount++;
											$scope.selectedPolygon.splice(i, 1);  // delete duplicate
										}
										else {
											$scope.selectedPolygonObj[$scope.selectedPolygon[i].id] = $scope.selectedPolygon[i];
											dupCount++;
										}
									}
									else {
										$scope.selectedPolygonObj[$scope.selectedPolygon[i].id] = $scope.selectedPolygon[i];
									}
								}
								alertScope.consoleDebug("[rifd-dsub-maptable.js] de-duplicate $scope.selectedPolygon: " + 
									$scope.selectedPolygon.length);
											
//								var notFoundPolys = [];
//								var geojsonPolys = [];
								var collectionLength = data.attributes.length;
											   
								for (var i = 0; i < collectionLength; i++) {
									var thisPoly = data.attributes[i];
//									geojsonPolys.push(thisPoly.area_id)
									if ($scope.selectedPolygonObj[thisPoly.area_id]) {
										data.attributes[i].band = $scope.selectedPolygonObj[thisPoly.area_id].band; // Set the band
//										$scope.selectedPolygonObj[thisPoly.area_id].found=true;
										foundCount++;
									}
									else {
										data.attributes[i].band = 0;
									}
								}
								alertScope.consoleDebug("[rifd-dsub-maptable.js] checked $scope.selectedPolygon: " + 
									$scope.selectedPolygon.length);
								
//								for (key in $scope.selectedPolygonObj) {
//									if ($scope.selectedPolygonObj[key].found == false) {
//										notFoundPolys.push($scope.selectedPolygonObj[key]);
//									}
//								}
//								notFoundPolys.sort(); // Alphabetically!
												
								var hasErrors=false;
								if (dupCount > 0) {
									alertScope.showError(dupCount + 
										" duplicates with differing bands were found in the selected polygons list");
									hasErrors=true;
								}
								if (dupBandCount > 0) {
									alertScope.showWarning(dupBandCount + " fixable duplicates were found in the selected polygons list");
								}
								
								if (foundCount != $scope.selectedPolygon.length) {
									alertScope.showError("Could not match " + ($scope.selectedPolygon.length - foundCount) + " polygons from database with selected polygons list");
									hasErrors=true;
								}
								
								if (hasErrors) {
									reject("error processing selected polygons list");
										
	//								alertScope.consoleDebug("[rifd-dsub-maptable.js] $scope.selectedPolygon: " + 
	//									JSON.stringify($scope.selectedPolygon, null, 1));
									
									alertScope.consoleDebug("[rifd-dsub-maptable.js] foundCount: " + foundCount + 
										"; dupCount: " + dupCount +
										"; dupBandCount: " + dupBandCount +
										"; data.attributes: " + collectionLength + 
										"; $scope.totalPolygonCount: " + $scope.totalPolygonCount + 
										"; $scope.selectedPolygon.length: " + $scope.selectedPolygon.length + 
										"; $scope.selectedPolygonCount: " + $scope.selectedPolygonCount /* + 
										"; geojsonPolys(" + geojsonPolys.length + "): " + JSON.stringify(geojsonPolys, null, 1) +
										"; notFoundPolys(" + notFoundPolys.length + "): " + JSON.stringify(notFoundPolys, null, 1) */);
								}
								else {
									resolve("processed selected polygons list");
								}
							})
						}										
						
						function addSelectedShapes() {
							return $q(function(resolve, reject) {
								var selectedShapes=undefined;
								
								// Add back selected shapes
								if (selectedShapes=SelectStateService.getState().studySelection) {
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
									 "; $scope.areamap.hasLayer($scope.shapes): " + $scope.areamap.hasLayer($scope.shapes));
									if (SelectStateService.getState().showHideSelectionShapes) {
										$scope.bShowHideSelectionShapes = true;
										if (!$scope.areamap.hasLayer($scope.shapes)) {
											alertScope.consoleDebug("[rifd-dsub-maptable.js] add shapes layerGroup");
											$scope.areamap.addLayer($scope.shapes);
											if ($scope.info._map == undefined) { // Add back info control
												$scope.info.addTo($scope.areamap);
												alertScope.consoleDebug("[rifd-dsub-maptable.js] add info control");
											}
										}
										if ($scope.shapes.getLayers().length == 0) {
											alertScope.consoleDebug("[rifd-dsub-maptable.js] start addSelectedShapes(): shapes layerGroup has no layers");				
										}
										else {
											alertScope.consoleDebug("[rifd-dsub-maptable.js] start addSelectedShapes(): shapes layerGroup has " +
												$scope.shapes.getLayers().length + " layers");
										}
									} else {
										if ($scope.areamap.hasLayer($scope.shapes)) {
											alertScope.consoleDebug("[rifd-dsub-maptable.js] remove shapes layerGroup");
											$scope.areamap.removeLayer($scope.shapes);
											if ($scope.info._map) { // Remove info control
												$scope.info.remove();
												alertScope.consoleDebug("[rifd-dsub-maptable.js] remove info control");
											}
										}
										$scope.bShowHideSelectionShapes = false;
									}	
									
									var errorCount=0;
									var start=new Date().getTime();
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
												$scope.info.update(selectedShape, (e.target._latlng || e.latlng));
											}									
											function selectedShapesResetFeature(e) {
		//										alertScope.consoleDebug("[rifd-dsub-maptable.js] selectedShapesResetFeature " +  
		//											"(" + e.target._leaflet_id + "; for: " + e.originalEvent.currentTarget._leaflet_id + 
		//											"; " + JSON.stringify((e.target._latlng || e.latlng)) + "): " +
		//											(JSON.stringify(selectedShape.properties) || "no properties"));
												$scope.info.update(undefined, (e.target._latlng || e.latlng));
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
														$scope.shapes.addLayer(circle);
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
														$scope.shapes.addLayer(marker);
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
													$scope.shapes.addLayer(polygon);
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
												$scope.shapeLoadUpdate = selectedShapes.length + " shapes loaded in " + elapsed + " S";
											}
											else {
												$scope.shapeLoadUpdate = "no shapes loaded";
											}
											if (errorCount > 0) {
												reject(errorCount + "errors adding " + selectedShapes.length + " selected shapes in " + 
													elapsed + " S");
											}
											else {
												resolve("added " + selectedShapes.length + " selected shapes in " + elapsed + " S");
											}
										}); // End of async.eachOfSeries()				
								}
								else {
									resolve("map has no selected shapes");
								}
							});
						}

						function processEachFeatureArray() {

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
										 "; $scope.areamap.hasLayer(centroidMarkers): " + $scope.areamap.hasLayer(centroidMarkers));
										if (SelectStateService.getState().showHideCentroids) {
											$scope.bShowHideCentroids = true;
											if (!$scope.areamap.hasLayer(centroidMarkers)) {
												$scope.areamap.addLayer(centroidMarkers);
											}
										} else {
											if ($scope.areamap.hasLayer(centroidMarkers)) {
												$scope.areamap.removeLayer(centroidMarkers);
											}
											$scope.bShowHideCentroids = false;
										}	
									
										$scope.areamap.whenReady(function() {
											
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
							$scope.areamap.on('zoomstart', function(ev) {
								$scope.areamap.spin(true);  // on
							});
							$scope.areamap.on('zoomend', function(ev) {
								$scope.areamap.spin(false);  // off
							});
							$scope.areamap.on('movestart', function(ev) {
								$scope.areamap.spin(true);  // on
							});
							$scope.areamap.on('moveend', function(ev) {
								$scope.areamap.spin(false);  // off
							});								
						}
						
                        function handleGeoLevelSelect(res) {
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
                        function handleDefaultGeoLevels(res) {
                            //get the select levels
                            $scope.input.selectAt = res.data[0].names[0];
                            $scope.input.studyResolution = res.data[0].names[0];
                            $scope.geoLevelChange();
                        }
                        function handleGeoLevelViews(res) {
                            $scope.geoLevelsViews.length = 0;
                            for (var i = 0; i < res.data[0].names.length; i++) {
                                $scope.geoLevelsViews.push(res.data[0].names[i]);
                            }
                            //if not in list then match (because result res cannot be lower than select res)
                            if ($scope.geoLevelsViews.indexOf($scope.input.studyResolution) === -1) {
                                $scope.input.studyResolution = $scope.input.selectAt;
                            }
                            //get table
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
						$scope.shapes = new L.layerGroup();
                        $scope.areamap.addLayer($scope.shapes);

                        //Set up table (UI-grid)
                        $scope.gridOptions = ModalAreaService.getAreaTableOptions();
                        $scope.gridOptions.columnDefs = ModalAreaService.getAreaTableColumnDefs();
                        //Enable row selections
                        $scope.gridOptions.onRegisterApi = function (gridApi) {
                            $scope.gridApi = gridApi;
                        };
						
                        //Set the user defined basemap
						// Called from rifc-dmap-main.js
                        $scope.renderMap = function (mapID) {
							if (thisGeography) {
								LeafletBaseMapService.setDefaultMapBackground(thisGeography, setBaseMapCallback, mapID);
							}
							else {
								$scope.consoleLog("[rifc-util-mapping.js] WARNING unable to LeafletBaseMapService.setDefaultMapBackground; no geography defined for map: " +
									mapID);
								LeafletBaseMapService.setDefaultBaseMap(mapID);
							}							
                        };
					
						function setBaseMapCallback(err, areamap) {
							if (err) { // LeafletBaseMapService.setDefaultMapBackground had error
								alertScope.consoleLog("[rifd-dsub-maptable.js] LeafletBaseMapService.setDefaultMapBackground had error: " + 
									err);
							}
							
                            $scope.areamap.removeLayer($scope.thisLayer);
                            if (!LeafletBaseMapService.getNoBaseMap("areamap")) {
								var newBaseMap = LeafletBaseMapService.getCurrentBaseMapInUse("areamap");
								alertScope.consoleLog("[rifd-dsub-maptable.js] setBaseMap: " + newBaseMap);
                                $scope.thisLayer = LeafletBaseMapService.setBaseMap(newBaseMap);
                                $scope.thisLayer.addTo($scope.areamap);
								LeafletBaseMapService.setNoBaseMap("areamap", false);
                            }
							else {
								alertScope.consoleLog("[rifd-dsub-maptable.js] setBaseMap: NONE");
								LeafletBaseMapService.setNoBaseMap("areamap", true);
							}
                            //hack to refresh map
                            $timeout(function () {
                                $scope.areamap.invalidateSize();
                            }, 50);
						}
						
                        function renderFeature(feature) {
							if ($scope.selectedPolygonObj[feature]) {
                                //max possible is six bands according to specs
                                return selectorBands.bandColours[$scope.selectedPolygonObj[feature].band - 1];
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

                        //********************************************************************************************************
                        //Watch selectedPolygon array for any changes
                        $scope.$watchCollection('selectedPolygon', function (newNames, oldNames) {
                            if (newNames === oldNames) {
                                return;
                            }
                            //Update table selection
                            $scope.gridApi.selection.clearSelectedRows();
							var foundCount=0;
                            for (var i = 0; i < $scope.gridOptions.data.length; i++) {
                                $scope.gridOptions.data[i].band = 0;
								
								if ($scope.selectedPolygonObj[$scope.gridOptions.data[i].area_id]) {
									foundCount++;
                                    $scope.gridOptions.data[i].band = $scope.selectedPolygonObj[$scope.gridOptions.data[i].area_id].band;
                                }
                            }
							alertScope.consoleLog("[rifd-dsub-maptable.js] newNames: " + newNames.length +
								"; oldNames: " + oldNames.length +
								"; foundCount: " + foundCount +
								"; $scope.gridOptions.data: " + $scope.gridOptions.data.length);
		
                            //Update the area counter
                            $scope.selectedPolygonCount = newNames.length;

                            if (!$scope.geoJSON) {
                                return;
                            } else {
                                //Update map selection    
                                $scope.geoJSON._geojsons.default.eachLayer(handleLayer);
                            }

                        });
                        //*********************************************************************************************************************
                        //SELECTION METHODS

                        /*
                         * SELECT AREAS USING LEAFLETDRAW
                         */
                        //Add Leaflet.Draw capabilities
                        var drawnItems;
						
                        drawnItems = new L.FeatureGroup();
                        $scope.areamap.addLayer(drawnItems);
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
								featureGroup: drawnItems
							}
						});
                        $scope.areamap.addControl($scope.drawControl);
						
                        new L.Control.GeoSearch({
                            provider: new L.GeoSearch.Provider.OpenStreetMap()
                        }).addTo($scope.areamap);
                        //add the circle to the map
                        $scope.areamap.on('draw:created', function (e) {
                            drawnItems.addLayer(e.layer);
                        });
                        //override other map mouse events
                        $scope.areamap.on('draw:drawstart', function (e) {
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
									
							$scope.areamap.whenReady(function() {
								$timeout(function() {										
										
										alertScope.consoleLog("[rifd-dsub-maptable.js] redraw map");
										$scope.areamap.fitBounds($scope.areamap.getBounds()); // Force map to redraw after 0.5s delay
									}, 500);	
							});									
						};
						
                        // completed selection event fired from service
						$scope.$on('completedDrawSelection', function (event, data) {
							
							$scope.areamap.spin(true);  // on	
							var start=new Date().getTime();
								
							$scope.shapeLoadUpdate = "0 / " + $scope.selectionData.length + " shapes, 0%";
							async.eachOfSeries($scope.selectionData, 
								function iteratee(item, indexKey, callback) {
								$scope.shapeLoadUpdate = (indexKey) + " / " + $scope.selectionData.length + " shapes, " + 
									(Math.round(((indexKey)/$scope.selectionData.length)*100)) + "%";
								
								if (indexKey % 50 == 0) {
									$timeout(function() { // Be nice to the stack if you are going to be aggressive!
										makeDrawSelection(item, callback);
									}, 100);
								}	
								else {	
									async.setImmediate(function() {
										makeDrawSelection(item, callback);
									});
								}
							}, function done(err) {
								if (err) {
									alertScope.showError("[rifd-dsub-maptable.js] completedDrawSelection error: " + err);
								}
								
								var end=new Date().getTime();
								var elapsed=(Math.round((end - start)/100))/10; // in S	
								$scope.shapeLoadUpdate = $scope.selectionData.length + " shapes loaded in " + elapsed + " S";
								$scope.areamap.spin(false);  // off	
								$scope.selectionData = [];
								alertScope.consoleLog("[rifd-dsub-maptable.js] completed Draw Selection in " + elapsed + " S");
								if ($scope.info._map == undefined) { // Add back info control
									$scope.info.addTo($scope.areamap);
								}
								$scope.zoomToSelection(); // Zoom to selection
								$scope.safeApply(0, function() {
									$scope.redrawMap();
								});	
							});
                        });
									
						$scope.createAreaNameList = function () { // Not from latlngList - not in scope when restored
							var newAreaNameList = {};
							
							if (SelectStateService.getState().studySelection && SelectStateService.getState().studySelection.studySelectedAreas) {
								var studySelectedAreas=SelectStateService.getState().studySelection.studySelectedAreas;
								
								for (var i = 0; i < studySelectedAreas.length; i++) {              									
									// Update areaNameList for debug
									if (studySelectedAreas[i].band && studySelectedAreas[i].band != -1) {
										if (newAreaNameList[studySelectedAreas[i].band]) {
											newAreaNameList[studySelectedAreas[i].band].push(studySelectedAreas[i].label);
										}
										else {
											newAreaNameList[studySelectedAreas[i].band] = [];
											newAreaNameList[studySelectedAreas[i].band].push(studySelectedAreas[i].label);
										}
									}
								}
							}
							if ($scope.areaNameList == undefined) {
								$scope.areaNameList = {};
							}
//							alertScope.consoleLog("[rifd-dsub-maptable.js] createAreaNameList(); studySelectedAreas: " + studySelectedAreas.length +
//								"; old areaNameList: " + Object.keys($scope.areaNameList).length +
//								"; new areaNameList: " + Object.keys(newAreaNameList).length +
//								"; " + JSON.stringify(newAreaNameList));
								
							if (newAreaNameList) {
								$scope.areaNameList = newAreaNameList;
							}
						}
							
                        // selection event fired from service
                        $scope.$on('makeDrawSelection', function (event, data) {
                            $scope.selectionData.push(data);
                        });
                        makeDrawSelection = function (shape, makeDrawSelectionCallback) {
							
							// Create savedShape for SelectStateService
							var savedShape = {
								isShapefile: (shape.isShapefile || false),
								circle: shape.circle,
								freehand: shape.freehand,
								band: shape.band,
								area: shape.area, 
								properties: shape.properties,
								radius: undefined,
								latLng: undefined,
								geojson: undefined,
								bbox: undefined,
								finalCircleBand: (shape.finalCircleBand || false),
								style: undefined,
								selectionMethod: shape.selectionMethod
							}
//							
// Risk analysis study types (as per rif40_studies.stype_type): 
//
// 11 - Risk Analysis (many areas, one band), 
// 12 - Risk Analysis (point sources, many areas, one to six bands) [DEFAULT], 
// 13 - Risk Analysis (exposure covariates), 
// 14 - Risk Analysis (coverage shapefile), 
// 15 - Risk Analysis (exposure shapefile)
//
							if (shape.selectionMethod === 1) { // selectionMethod 1: Single boundary; already set
								SelectStateService.getState().studySelection.riskAnalysisType = 11;
							}
							else if (shape.selectionMethod === 2) { // selectionMethod 2: make selection by band attribute in file
								SelectStateService.getState().studySelection.riskAnalysisType = 13;
							}
							else if (shape.selectionMethod === 3) { // selectionMethod 3: make selection by attribute value in file			
								SelectStateService.getState().studySelection.riskAnalysisType = 15;				
							}
								
							
							if (savedShape.freehand) {						
								
								if ($scope.input.type === "Risk Analysis") {
									alertScope.showError("Freehand selection not permitted for risk analysis");
									var polyId=shape.data._leaflet_id;
									drawnItems.eachLayer(
										function(layer) {
											alertScope.consoleDebug("[rifd-dsub-maptable.js] Remove freehand polygon; " + layer._leaflet_id);
											$scope.areamap.removeLayer(layer);
										});
										
									if (makeDrawSelectionCallback && typeof makeDrawSelectionCallback === "function") {
										makeDrawSelectionCallback("Freehand selection not permitted for risk analysis");
									}	
									else {
										throw new Error("No makeDrawSelectionCallback() function");
									}	
									return;
								}
								else {
								
									if (shape.band == -1) {
										shape.band=1;
										savedShape.band=1;
									}
									
									if (shape.data._latlngs && shape.data._latlngs.length > 1) { // Fix freehand polygons
										if (shape.data._latlngs[0].lat == shape.data._latlngs[shape.data._latlngs.length-1].lat &&
										   shape.data._latlngs[0].lng == shape.data._latlngs[shape.data._latlngs.length-1].lng) { // OK
										} 
										else { // Make it a polygon
											shape.data._latlngs.push({
												lat: shape.data._latlngs[0].lat,
												lng: shape.data._latlngs[0].lng
											});
											alertScope.consoleDebug("[rifd-dsub-maptable.js] Fix freehand polygon; " +
											shape.data._latlngs.length + " points: " + 
												JSON.stringify(shape.data._latlngs));
										}
									}
								}
							}
							else {
								var fileList = SelectStateService.getState().studySelection.fileList;
									if (fileList && fileList.length > 0) {
									alertScope.consoleDebug("[rifd-dsub-maptable.js] " + fileList.length +
										"; savedShape.isShapefile: " + savedShape.isShapefile);	
								}
								else {
									alertScope.consoleDebug("[rifd-dsub-maptable.js] no shapefiles");
								}								
							}
							
							if (SelectStateService.getState().studySelection.bandAttr.length > 0) {
								alertScope.consoleDebug("[rifd-dsub-maptable.js] " + 
									SelectStateService.getState().studySelection.bandAttr.length +
									"; bandAttr: " + JSON.stringify(SelectStateService.getState().studySelection.bandAttr, null, 1));	
							}					
							
							savedShape.style={
										color: (selectorBands.bandColours[savedShape.band-1] || 'blue'),
										weight: (selectorBands.weight || 3),
										opacity: (selectorBands.opacity || 0.8),
										fillOpacity: (selectorBands.fillOpacity || 0)
									};
		
							function highLightFeature(e) {
								alertScope.consoleDebug("[rifd-dsub-maptable.js] makeDrawSelection highLightFeature " +  
										"(" + this._leaflet_id + "; " + JSON.stringify(this._latlng) + "): " +
										(JSON.stringify(savedShape.properties) || "no properties"));
								$scope.info.update(savedShape, this._latlng); 
							}									
							function resetFeature(e) {
								alertScope.consoleDebug("[rifd-dsub-maptable.js] makeDrawSelection resetFeature " +  
										"(" + this._leaflet_id + "; " + JSON.stringify(this._latlng) + "): " +
										(JSON.stringify(savedShape.properties) || "no properties"));
								$scope.info.update(undefined, this._latlng);
							}		
							
							if (shape.circle) { // Represent circles as a point and a radius
								savedShape.radius=shape.data.getRadius();
								savedShape.latLng=shape.data.getLatLng();
								if (savedShape.area == undefined || savedShape.area == 0) {
									savedShape.area = Math.round((Math.PI*Math.pow(shape.data.getRadius(), 2)*100)/1000000)/100 // Square km to 2dp
									
									if (savedShape.area == undefined || savedShape.area == 0) {
										alertScope.consoleDebug("[rifd-dsub-maptable.js] makeDrawSelection(): Cannot determine area" +
											"; geoJSON: " + JSON.stringify(savedShape.geojson, null, 1));
										alertScope.showError("Could not create circle shape");
									}
								}
								if ((shape.band == 1) || (shape.band > 1 && !savedShape.finalCircleBand)) {
									// basic shape to map shapes layer group
									var circle = new L.Circle([savedShape.latLng.lat, savedShape.latLng.lng], {
											pane: 'shapes', 
											band: savedShape.band,
											area: savedShape.area,
											radius: savedShape.radius,
											color: (savedShape.style.color || selectorBands.bandColours[savedShape.band-1] || 'blue'),
											weight: (savedShape.style.weight || selectorBands.weight || 3),
											opacity: (savedShape.style.opacity || selectorBands.opacity || 0.8),
											fillOpacity: (savedShape.style.fillOpacity || selectorBands.fillOpacity || 0)
										});				
									circle.on({
										mouseover: highLightFeature,
										mouseout: resetFeature
									});
									
									$scope.shapes.addLayer(circle);
									alertScope.consoleLog("[rifd-dsub-maptable.js] makeDrawSelection() added circle" +
										"; color: " + selectorBands.bandColours[savedShape.band-1] +
										"; savedShape: " + JSON.stringify(savedShape, null, 1));
									if (shape.band == 1) {
									   var factory = L.icon({
											iconUrl: 'images/factory.png',
											iconSize: 15
										});
										var marker = new L.marker([savedShape.latLng.lat, savedShape.latLng.lng], {
											pane: 'shapes',
											icon: factory
										});
										$scope.shapes.addLayer(marker);
									}
								}
								else {
									alertScope.consoleLog("[rifd-dsub-maptable.js] makeDrawSelection() suppressed circle" +
										"; savedShape: " + JSON.stringify(savedShape, null, 1));
								}
							}
							else { // Use geoJSON								
								
								var coordinates;
								
								if (shape.data._latlngs && shape.data._latlngs.length > 1) {
									coordinates=shape.data._latlngs;
								}
								else {
									coordinates=shape.data._latlngs[0];
								}
							
								var polygon;
								
								if (savedShape.freehand) {
									polygon=L.polygon(coordinates, {
											pane: 'shapes', 
											band: savedShape.band,
											area: savedShape.area,
											color: (savedShape.style.color || selectorBands.bandColours[savedShape.band-1] || 'blue'),
											weight: (savedShape.style.weight || selectorBands.weight || 3),
											opacity: (savedShape.style.opacity || selectorBands.opacity || 0.8),
											fillOpacity: (savedShape.style.fillOpacity || selectorBands.fillOpacity || 0)
										});		
									savedShape.coordinates=coordinates; // L.Polygon()	- now fixed; was a lineString
									savedShape.geojson=angular.copy(polygon.toGeoJSON());								
								}
								else { // Shapefile
									savedShape.geojson=angular.copy(shape.data.toGeoJSON());	
									polygon=L.polygon(savedShape.geojson.geometry.coordinates[0], {
											pane: 'shapes', 
											band: savedShape.band,
											area: savedShape.area,
											color: (savedShape.style.color || selectorBands.bandColours[savedShape.band-1] || 'blue'),
											weight: (savedShape.style.weight || selectorBands.weight || 3),
											opacity: (savedShape.style.opacity || selectorBands.opacity || 0.8),
											fillOpacity: (savedShape.style.fillOpacity || selectorBands.fillOpacity || 0)
										});
								}
							
								if (polygon && polygon._latlngs.length > 0) {	
									
									if (savedShape.area == undefined || savedShape.area == 0) {
										if (savedShape.geojson) {
											savedShape.area = Math.round((turf.area(savedShape.geojson)*100)/1000000)/100; // Square km to 2dp
										}
										else {
											alertScope.consoleLog("[rifd-dsub-maptable.js] makeDrawSelection(): savedShape.area could not be set: " + 
												JSON.stringify(savedShape));
										}
									
										if (savedShape.area == undefined || savedShape.area == 0) {
											alertScope.consoleDebug("[rifd-dsub-maptable.js] makeDrawSelection(): Cannot determine area" +
												"; geoJSON: " + JSON.stringify(savedShape.geojson, null, 1));
											if (savedShape.freehand) {	
												alertScope.showError("Could not create freehand Polygon shape");
											}
											else {
												alertScope.showError("Could not create shapefile Polygon shape");
											}
										}
										polygon.options.area=savedShape.area;
									}
								
									polygon.on({
										mouseover: highLightFeature,
										mouseout: resetFeature
									}); 
									$scope.shapes.addLayer(polygon);
										
									alertScope.consoleDebug("[rifd-dsub-maptable.js] makeDrawSelection(): added Polygon" + 
										"; band: " + savedShape.band +
										"; area: " + savedShape.area +
										"; freehand: " + savedShape.freehand +
										"; style: " + JSON.stringify(savedShape.style) +
										"; " + coordinates.length + " coordinates; " +
												JSON.stringify(coordinates).substring(0,100) + "..." +
										"; properties: " + (JSON.stringify(savedShape.properties) || "None"));										
								}
								else {
									alertScope.consoleDebug("[rifd-dsub-maptable.js] makeDrawSelection(): L.Polygon is undefined" +
										"; geoJSON: " + JSON.stringify(savedShape.geojson, null, 1));
									if (savedShape.freehand) {	
										alertScope.showError("Could not create freehand Polygon shape");
									}
									else {
										alertScope.showError("Could not create shapefile Polygon shape");
									}
								}
							}	
							
							function addBbox() { // Returns a promise
								
								return $timeout(function() { // Wait a bit to avoid
									// TypeError: Unable to get property 'layerPointToLatLng' of undefined or null reference				  
									try {	
										var bbox=undefined;	
										var bounds=undefined;										
										if (shape.circle) { // Represent circles as a point and a radius								
											bounds=circle.getBounds();
										}
										else if (savedShape.geojson) {
											bounds = polygon.getBounds();
										}
										
										if (bounds.isValid()) {
											bbox=[bounds.getWest(), 
												 bounds.getSouth(),
												 bounds.getEast(),
												 bounds.getNorth()]; // Bounding box:  [minX, minY, maxX, maxY]	 
										}	
										
										if (bbox) {
											savedShape.bbox=L.latLngBounds(
												L.latLng(bbox[1], bbox[0]),  // South west [minY, minX]
												L.latLng(bbox[3], bbox[2])); // North east [maxY, maxX]
											if (savedShape.bbox && 
											    savedShape.bbox.isValid() && 
												bounds && 
												bounds.isValid() && 
												savedShape.bbox.equals(bounds)) { 
												// Verify
												shape.bbox = savedShape.bbox;
											}
											else { // It doesn't matter if the BBOX is invalid as the GISService will be called directly
												bboxErrors++;
												if (bboxErrors < 10) {							
													alertScope.consoleError("[rifd-dsub-maptable.js] makeDrawSelection() invalid bbox: " +
														JSON.stringify(bbox) +
														"; savedShape.bbox: " + 
															(savedShape.bbox ? savedShape.bbox.toBBoxString() : "undefined") +
														"; savedShape.bbox.isValid(): " + 
															(savedShape.bbox ? savedShape.bbox.isValid() : "undefined") +
														"; bounds: " + (bounds ? bounds.toBBoxString() : "undefined") +
														"; bounds.isValid(): " + (bounds ? bounds.isValid() : "undefined")+
														"; savedShape.bbox.equals(bounds): " + 
															((bounds && savedShape.bbox) ? savedShape.bbox.equals(bounds) : "undefined"));
												}
												savedShape.bbox=undefined;
											}
										}
										else {
											bboxErrors++;
											if (bboxErrors < 10) {
												alertScope.consoleError("[rifd-dsub-maptable.js] makeDrawSelection() no bbox" +
													"; bounds: " + (bounds ? bounds.toBBoxString() : "undefined"));
											}
										}
									}	
									catch (e) { // For a better message
										alertScope.consoleError("[rifd-dsub-maptable.js] makeDrawSelection() error in bbox creation: " + 
											e.message +
											"; bounds: " + (bounds ? bounds.toBBoxString() : "undefined"), e);
										bboxErrors++;
									}	
								}, 100);
							}
										
							// Save to SelectStateService
							if ($scope.input.name == "ComparisionAreaMap") { 
								SelectStateService.getState().studySelection.comparisonShapes.push(savedShape);
								alertScope.consoleDebug("[rifd-dsub-maptable.js] Save to ComparisionAreaMap SelectStateService " +
									SelectStateService.getState().studySelection.comparisonShapes.length);
							}
							else {
								SelectStateService.getState().studySelection.studyShapes.push(savedShape);							
							
								alertScope.consoleDebug("[rifd-dsub-maptable.js] Save to StudyAreaMap SelectStateService " +
									SelectStateService.getState().studySelection.studyShapes.length);
							}
							
							function latlngListEnd () { 
								$scope.areaNameList = {};
								var areaCheck = {};
								var duplicateAreaCheckIds = [];
								
								// Check for duplicate selectedPolygons 
								for (var j = 0; j < $scope.selectedPolygon.length; j++) {
									var thisPolyID = $scope.selectedPolygon[j].id;
									if (areaCheck[thisPolyID]) {
										areaCheck[thisPolyID].count++;
									}
									else {
										areaCheck[thisPolyID] = { 
											count: 1,
											index: []
										};
									}
									areaCheck[thisPolyID].index.push(j);
								}
								for (var id in areaCheck) {
									if (areaCheck[id].count > 1) {
										duplicateAreaCheckIds.push(id);
									}
									
/*									if (duplicateAreaCheckIds.length < 10) {
										alertScope.consoleDebug("[rifd-dsub-maptable.js] " + 
											"duplicateAreaCheckIds[" + duplicateAreaCheckIds.length + "] " +
											id + "; duplicates: " +
											JSON.stringify(areaCheck[id].index));
									} */
								}
								if (duplicateAreaCheckIds.length > 0) {
									alertScope.consoleDebug("[rifd-dsub-maptable.js] " + 
										duplicateAreaCheckIds.length +
										" duplicateAreaCheckIds: " + JSON.stringify(duplicateAreaCheckIds));
									alertScope.showError("Duplicate area IDs detected in selected polygon list");
								}
								
								for (var i = 0; i < latlngList.length; i++) {
									var thisPolyID = latlngList[i].id;
									
									// Update band
									if ($scope.selectedPolygonObj[thisPolyID]) {
										latlngList[i].band=$scope.selectedPolygonObj[thisPolyID].band;
										break;
									} 	
									// Sync table - done by $scope.$watchCollection() above
                            									
									// Update areaNameList for debug
									if (latlngList[i].band && latlngList[i].band != -1) {
										if ($scope.areaNameList[latlngList[i].band]) {
											$scope.areaNameList[latlngList[i].band].push(latlngList[i].name);
										}
										else {
											$scope.areaNameList[latlngList[i].band] = [];
											$scope.areaNameList[latlngList[i].band].push(latlngList[i].name);
										}
									}
								}
										
								alertScope.consoleDebug("[rifd-dsub-maptable.js] $scope.selectedPolygon.length: " + $scope.selectedPolygon.length);
								
								for (var band in $scope.areaNameList) {
									alertScope.consoleDebug("[rifd-dsub-maptable.js] areaNameList band: " + 
									band + "; " + $scope.areaNameList[band].length + " areas");
//										": " + JSON.stringify($scope.areaNameList[band]));	
								}

								if (!shape.circle && !shape.shapefile) {
									removeMapDrawItems();
									//auto increase band dropdown
									if ($scope.currentBand < Math.max.apply(null, $scope.possibleBands)) {
										$scope.currentBand++;
									}
								}
								
								if (bboxErrors > 0) {
									alertScope.showWarning(bboxErrors + " errors occurred create bounding boxes for shape");
								}
								if (excludedByBbox > 0) {
									alertScope.showWarning(excludedByBbox + " shapes would have been excluded by using a bounding box");
								}
								
								if (makeDrawSelectionCallback && typeof makeDrawSelectionCallback === "function") {
									makeDrawSelectionCallback();
								}
								else {
									throw new Error("No makeDrawSelectionCallback() function");
								}								
							} // End of latlngListEnd ()
							var itemsProcessed = 0;
							var bboxErrors = 0;
							var excludedByBbox = 0;
							
							addBbox().then(function(res) {
								async.eachOfSeries(latlngList, 
									function latlngListIteratee(latLng, indexKey, latlngListCallback) {
									if (indexKey == 0) {
										alertScope.consoleDebug("[rifd-dsub-maptable.js] processing latLng " + indexKey + "/" + latlngList.length + 
											" to check if in shape: " + JSON.stringify(shape.bbox));	
									}
									if (indexKey % 500 == 0) {
										alertScope.consoleDebug("[rifd-dsub-maptable.js] processing latLng " + indexKey + "/" + latlngList.length + 
											" to check if in shape: " + JSON.stringify(shape.bbox));									
										$timeout(function() { // Be nice to the stack if you are going to be aggressive!
											latlngListFunction(latLng, latlngListCallback);
										}, 100);
									}	
									else if (indexKey % 50 == 0) {
										async.setImmediate(function() { // Be nice to the stack if you are going to be aggressive!
											latlngListFunction(latLng, latlngListCallback);
										});
									}
									else {
										latlngListFunction(latLng, latlngListCallback);
									}
								}, function done(err) {
									if (err) {
										alertScope.showError("[rifd-dsub-maptable.js] latlngList error: " + err);
									}
									latlngListEnd();
								});		
							}, function (err) {
								if (makeDrawSelectionCallback && typeof makeDrawSelectionCallback === "function") {
									makeDrawSelectionCallback(err);
								}
								else {
									throw new Error("No makeDrawSelectionCallback() function");
								}
							});								
							
							function latlngListFunction(latLng, latlngListCallbackFunction) { 
								// Check centroids of areas lie within the shape
								//is point in defined polygon?
								var test=false;
								
								if (shape.bbox && shape.bbox.isValid()) { // Use the bounding box first to eliminate most points
									if (shape.circle) {
										test = GISService.getPointincircle(latLng.latLng, shape);
									}
									else if (shape.bbox.contains(latLng.latLng)) { // This does NOT work!
										// Shape bounding box contains point
										test = GISService.getPointinpolygon(latLng.latLng, shape);
									}
									else { // Test above
										// This code should only be commented out when we are sure shape.bbox.contains(latLng.latLng) is working OK
										test = GISService.getPointinpolygon(latLng.latLng, shape);
										if (test) {	// It is in polygon, shape.bbox.contains(latLng.latLng) produce false when it should be true		
											excludedByBbox++;								
											alertScope.consoleError("[rifd-dsub-maptable.js] latlngListFunction bbox contains() failed to match point: " +
												"; latLng.latLng: " + JSON.stringify(latLng.latLng) +
												"; shape.bbox: " + shape.bbox.toBBoxString() +
												"; shape.bbox.isValid(): " + shape.bbox.isValid() +
												"; shape.bbox.contains(latLng.latLng)): " + shape.bbox.contains(latLng.latLng));
										}
										// Replace with:
										// excludedByBbox++;	0
									}
								}
								else if (shape.circle) {
									test = GISService.getPointincircle(latLng.latLng, shape);
								} else {
									test = GISService.getPointinpolygon(latLng.latLng, shape);
								}
								
								if (test) { // Intersects
									var thisLatLng = latLng.latLng;
									var thisPoly = latLng.name;
									var thisPolyID = latLng.id;
									var bFound = false;
									
//									alertScope.consoleDebug("[rifd-dsub-maptable.js] latlngList.forEach(" +
//										itemsProcessed + ")");
									// Selects the correct polygons
									if ($scope.selectedPolygonObj[thisPolyID]) { // Found
										if ($scope.selectedPolygonObj[thisPolyID].band == undefined ||
											$scope.selectedPolygonObj[thisPolyID].band === -1) { 
													// If not set in concentric shapes
											if (latlngList[itemsProcessed].band == undefined ||
												latlngList[itemsProcessed].band === -1) { 
													// If not set on map
												if (shape.band === -1) {  // Set band
													$scope.selectedPolygonObj[thisPolyID].band=$scope.currentBand;
												}
												else if ($scope.selectedPolygonObj[thisPolyID].band > 0 &&
													$scope.selectedPolygonObj[thisPolyID].band < shape.band) {  // Do not set band
													$scope.selectedPolygonObj[thisPolyID].band=$scope.currentBand;
												}
												else {
													$scope.selectedPolygonObj[thisPolyID].band=shape.band;
												}		
											}
										}
										bFound = true;
									}
																			
//										alertScope.consoleDebug("[rifd-dsub-maptable.js] latlngList.forEach(" +
//											itemsProcessed + ") bFound: " + bFound + 
//											"; latLng: " + JSON.stringify(latLng));
										
									if (!bFound) {
										if (shape.band === -1) {
											$scope.selectedPolygon.push({
												id: thisPolyID, 
												gid: thisPolyID, 
												label: thisPoly, 
												band: $scope.currentBand, 
												centroid: thisLatLng
											});
											$scope.selectedPolygonObj[thisPolyID] = {
												id: thisPolyID, 
												gid: thisPolyID, 
												label: thisPoly, 
												band: $scope.currentBand, 
												centroid: thisLatLng
											};
											latlngList[itemsProcessed].band=$scope.currentBand;
										} else {
											$scope.selectedPolygon.push({
												id: thisPolyID, 
												gid: thisPolyID, 
												label: thisPoly, 
												band: shape.band, 
												centroid: thisLatLng
											});
											$scope.selectedPolygonObj[thisPolyID] = {
												id: thisPolyID, 
												gid: thisPolyID, 
												label: thisPoly, 
												band: shape.band, 
												centroid: thisLatLng
											};
											latlngList[itemsProcessed].band=shape.band;
										}
									}
								}
								itemsProcessed++;
								if (latlngListCallbackFunction && typeof latlngListCallbackFunction === "function") {
									latlngListCallbackFunction();
								}
								else {
									throw new Error("No latlngListCallback() function");
								}
							}

                        }; // End of makeDrawSelection()
						
                        //remove drawn items event fired from service
                        $scope.$on('removeDrawnItems', function (event, data) {
                            removeMapDrawItems();
                        });
                        function removeMapDrawItems() {
                            drawnItems.clearLayers();
                            $scope.areamap.addLayer(drawnItems);
                            $scope.input.bDrawing = false; //re-enable layer events
                        }
						
						$scope.info = L.control();
						$scope.info.onAdd = function(map) {
							
							alertScope.consoleDebug("[rifd-dsub-maptable.js] create info <div>");
							this._div = L.DomUtil.create('div', 'info');
							this.update();
							return this._div;
						};

						// method that we will use to update the control based on feature properties passed
						$scope.info.update = function (savedShape, latLng /* Of shape, not mouse! */) {
							
							if (this._div) {
								if (savedShape) {
									var bandCount = {};
									var studySelection=$scope.selectedPolygon;
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
											"; long: " +  Math.round(savedShape.latLng.lng * 1000) / 1000 +'</b></br>';
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
										this._div.innerHTML+= '<b>area: ' + savedShape.area + ' square km</b><br />'
									}
										
									for (var property in savedShape.properties) {
										if (property == 'area') {
											if (savedShape.area === undefined) {
												this._div.innerHTML+= '<b>' + property + ': ' + savedShape.properties[property] + ' square km</b><br />'
											}
										}
										else if (property != '$$hashKey') {
											this._div.innerHTML+= '<b>' + property + ': ' + savedShape.properties[property] + '</b><br />';
										}
									}
									this._div.innerHTML += '<b>Band: ' + (savedShape.band || "unknown") + '</b><br />';
									this._div.innerHTML += '<b>Areas selected: ' + (bandCount[savedShape.band] || 0) + '/' +
										latlngList.length +  '</b><br />';
								}
								else if ($scope.shapes.getLayers().length > 0&& $scope.noMouseClocks) {
									this._div.innerHTML = '<h4>Mouse over selection shapes to show properties</br></h4>';
								}
								else if ($scope.shapes.getLayers().length > 0 && !$scope.noMouseClocks) {
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
							}
							else {
								alertScope.consoleDebug("[rifd-dsub-maptable.js] no info <div>"); 
								
								if ($scope.shapes == undefined) {
									alertScope.showError("[rifd-dsub-maptable.js] no shapes layerGroup");
								}
								else if ($scope.areamap.hasLayer($scope.shapes)) {
									alertScope.consoleDebug("[rifd-dsub-maptable.js] add shapes layerGroup");
									if ($scope.info._map == undefined) { // Add back info control
										$scope.info.addTo($scope.areamap);
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
									
									if ($scope.input.type === "Risk Analysis") {
										SelectStateService.initialiseRiskAnalysis();
									}
									else {			
										SelectStateService.resetState();
									}
									
                                    var bPushed = false;
                                    var bInvalid = false;
                                    for (var i = 0; i < listOfIDs.length; i++) {
                                        for (var j = 0; j < $scope.gridOptions.data.length; j++) {
                                            if ($scope.gridOptions.data[j].area_id === listOfIDs[i].ID) {
                                                var thisBand = Number(listOfIDs[i].Band);
//												alertScope.consoleLog("[rifd-dsub-maptable.js] [" + i + "," + j + "] MATCH area_id: " + $scope.gridOptions.data[j].area_id + 
//													"; ID: " + listOfIDs[i].ID +
//													"; thisBand: " + thisBand);
                                                if ($scope.possibleBands.indexOf(thisBand) !== -1) {
                                                    bPushed = true;
                                                    $scope.selectedPolygon.push({
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
                        };
                    }
                };
            }]);