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
        .controller('leafletLayersCtrl', ['$scope', 'user', 'LeafletBaseMapService', 'ChoroService', 'ColorBrewerService', 
            'MappingStateService', 'ViewerStateService', 'MappingService', 'ParametersService', 'SelectStateService', 
			'CommonMappingStateService', '$timeout', '$q',
            function ($scope, user, LeafletBaseMapService, ChoroService, ColorBrewerService,
                    MappingStateService, ViewerStateService, MappingService, ParametersService, SelectStateService, 
					CommonMappingStateService, $timeout, $q) {

                //Reference the parent scope, viewer or disease mapping
                var parentScope = $scope.$parent;
                parentScope.child = $scope;
						
                //Reference the state service
                $scope.myService = MappingStateService;
                if (parentScope.myMaps[0] === "viewermap") {
                    $scope.myService = ViewerStateService;
					ViewerStateService.setRemoveMap(function() { 
						$scope.removeMap("viewermap"); 
					});
                }
                else {
					MappingStateService.setRemoveMap(function() { 
						$scope.removeMap("diseasemap1");
						$scope.removeMap("diseasemap2");
					});
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
				
				$scope.parameters=ParametersService.getParameters()||{
						usePouchDBCache: false,		// DO NOT Use PouchDB caching in TopoJSONGridLayer.js; it interacts with the diseasemap sync;
						disableMapLocking: false,	// Disable front end debugging
						mapLockingOptions: {},		// Map locking options (for Leaflet.Sync())
						mappingDefaults: {					
							'diseasemap1': {
									method: 	'quantile', 
									feature:	'smoothed_smr',
									intervals: 	9,
									invert:		true,
									brewerName:	"PuOr"
							},
							'diseasemap2': {
									method: 	'AtlasProbability', 
									feature:	'posterior_probability',
									intervals: 	3,
									invert:		false,
									brewerName:	"Constant"
							},
							'viewermap': {
									method: 	'quantile', 
									feature:	'relative_risk',
									intervals: 	9,
									invert:		true,
									brewerName:	"PuOr"
							}
						},
						userMethods: {
							'AtlasRelativeRisk': {
									description: 'Atlas Relative Risk',
									breaks:		[-Infinity, 0.68, 0.76, 0.86, 0.96, 1.07, 1.2, 1.35, 1.51, Infinity],
									invert:		true,
									brewerName: "PuOr",
									invalidScales: ["Constant", "Dark2", "Accent", "Pastel2", "Set2"]
							},
							'AtlasProbability': {
									description: 'Atlas Probability',
									breaks: 	[0.0, 0.20, 0.81, 1.0],	
									invert:		false,
									brewerName:	"RdYlGn",
									invalidScales: ["Constant"]
							}
						},
						selectorBands: { // Study and comparison are selectors
							weight: 3,
							opacity: 0.8,
							fillOpacity: 0,
							bandColours: ['#e41a1c', '#377eb8', '#4daf4a', '#984ea3', '#ff7f00', '#ffff33']
						}
					};	
					// DO NOT Use PouchDB caching in TopoJSONGridLayer.js; it interacts with the diseasemap sync;	

				$scope.selectorBands = { // Study and comparison are selectors
						weight: 3,
						opacity: 0.8,
						fillOpacity: 0,
						bandColours: ['#e41a1c', '#377eb8', '#4daf4a', '#984ea3', '#ff7f00', '#ffff33']
					};
				if ($scope.parameters && $scope.parameters.selectorBands) {
					$scope.selectorBands=$scope.parameters.selectorBands
				}
						
				$scope.disableMapLocking=$scope.parameters.disableMapLocking||false;	
					// Disable disease map initial sync [for leak testing]
				$scope.layerStats = {
					layerAdds: 0,
					subLayerAdds: 0,
					layerUpdates: 0,
					subLayerUpdates: 0,
					subLayerRefreshes: 0,
					layerRemoves: 0,
					subLayerRemoves: 0,
					Layerwarnings: 0,
					errors: 0
				};
				
				$scope.cacheStats={
					hits: 0,
					misses: 0,
					errors: 0,
					tiles: 0,
					size: 0
				};					
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
			
				// selection shapee (usually risk analysis)
                $scope.shapes = ({
                    'diseasemap1': new L.layerGroup(),
                    'diseasemap2': new L.layerGroup(),
                    'viewermap': new L.layerGroup()
                });	
				
				// Show hide selection shapes
				$scope.bShowHideSelectionShapes = ({
                    'diseasemap1': true,
                    'diseasemap2': true,
                    'viewermap': true
                });	
				
				// Area name list by band
				$scope.areaNameList = {
                    'diseasemap1': {},
                    'diseasemap2': {},
                    'viewermap': {}
                }
				
				// Array of selected polygons
				$scope.selectedPolygon = {
                    'diseasemap1': [],
                    'diseasemap2': [],
                    'viewermap': []
                }	
				
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
                $scope.initialRefresh = {
                    "diseasemap1": false,
                    "diseasemap2": false,
                    "viewermap": false
                };
                $scope.attributeDataLoaded = {
                    "diseasemap1": false,
                    "diseasemap2": false,
                    "viewermap": false
                };
                $scope.checkAttributeDataLoadedTimer = {
                    "diseasemap1": undefined,
                    "diseasemap2": undefined,
                    "viewermap": undefined
                };
				
                $scope.studyIDs = [];

				$scope.removeMap = function(mapID) {
					$scope.consoleLog("[rifc-util-mapping.js] remove map: " + mapID);
					if ($scope.map[mapID].hasLayer($scope.geoJSON[mapID])) {
						$scope.geoJSON[mapID]._geojsons.default.eachLayer($scope.removeSubLayer);
						$scope.map[mapID].removeLayer($scope.geoJSON[mapID]);
						$scope.geoJSON[mapID]={};
                    }
				}
							
				// Show-hide shapes and associated info
				$scope.showShapes = function (mapID) {
					if ($scope.shapes[mapID] == undefined) {
						$scope.showError("[rifc-util-mapping.js] no shapes layerGroup for map: " + mapID);
					}
					else if ($scope.map[mapID].hasLayer($scope.shapes[mapID])) {
						$scope.map[mapID].removeLayer($scope.shapes[mapID]);
						$scope.consoleDebug("[rifc-util-mapping.js] remove shapes layerGroup for map: " + mapID);
//						if ($scope.info._map) { // Remove info control
//							$scope.info.remove();
//							$scope.consoleDebug("[rifc-util-mapping.js] remove info control");
//						}
						
						$scope.bShowHideSelectionShapes[mapID] = false;
						SelectStateService.getState().showHideSelectionShapes = false;

					} 
					else {
						$scope.map[mapID].addLayer($scope.shapes[mapID]);
						$scope.consoleDebug("[rifc-util-mapping.js] add shapes layerGroup for map: " + mapID);
//						if ($scope.info._map == undefined) { // Add back info control
//							$scope.info.addTo($scope.map[mapID]);
//							$scope.consoleDebug("[rifc-util-mapping.js] add info control");
//						}
						
						$scope.bShowHideSelectionShapes[mapID] = true;
						SelectStateService.getState().showHideSelectionShapes = true;
						
						$scope.bringShapesToFront(mapID);
					}
					$scope.consoleDebug("[rifc-util-mapping.js] showHideSelectionShapes for map: " + mapID + "; " + 
						SelectStateService.getState().showHideSelectionShapes);

				};
						
				/* 
				 * Add selection shapee (usually risk analysis)
				 */
				function addSelectedShapes(mapID, addSelectedShapesCallback) {		
				
					if (!addSelectedShapesCallback || typeof addSelectedShapesCallback !== "function") { 
						$scope.showError("No callback function for addSelectedShapes()");
						throw new Error("No callback function for addSelectedShapes()");
					}
					
					var selectedShapes;
					if ($scope.studyID[mapID].study_id) {
						
						SelectStateService.getStudySelection(mapID, $scope.studyID[mapID].study_id, 
							function getStudySelectionCallback(err, mapID, studySelection) {	
							
								$scope.bShowHideSelectionShapes[mapID]=(SelectStateService.getState().showHideSelectionShapes || true);
								
								// Add back selected shapes				
								if (err) {
									addSelectedShapesCallback(err);
								}
								else if (studySelection && studySelection.studyShapes) {
									$scope.myService.getState().studyType[mapID] = "Disease Mapping";
                                    $scope.$parent.studyType[mapID]="DISEASE MAPPING";
                                    $scope.$parent.isDiseaseMapping=true;
                                    $scope.$parent.isRiskAnalysis=false;
									$scope.$parent.isDiseaseMappingStudy[mapID] = true;
									if (ParametersService.getParameters()[mapID]) { // Reset mapping defaults
										$scope.parameters.mappingDefaults[mapID]=ParametersService.getParameters()[mapID];
									}
									if (studySelection.riskAnalysisDescription && studySelection.riskAnalysisType) {
										$scope.studyID[mapID].riskAnalysisDescription = studySelection.riskAnalysisDescription;
//										$scope.$parent.studyType[mapID]=$scope.studyID[mapID].riskAnalysisDescription.toUpperCase();
										$scope.$parent.studyType[mapID]="RISK ANALYSIS";
										$scope.myService.getState().studyType[mapID] = "Risk Analysis";
                                        $scope.$parent.isRiskAnalysis=true;
                                        $scope.$parent.isDiseaseMapping=false;
										$scope.$parent.isDiseaseMappingStudy[mapID] = false;
										$scope.parameters.mappingDefaults[mapID] = { // No Bayesian smoothing in risk analysis
											method: 	'quantile', 
											feature:	'relative_risk',
											intervals: 	9,
											invert:		true,
											brewerName:	"PuOr"
										};
									} // Otherwise disease mapping
									
									ChoroService.setType(mapID, $scope.myService.getState().studyType[mapID]);
									// Will need to set print state here

									// Remove old layers
									if ($scope.shapes[mapID].getLayers().length > 0) {
										$scope.shapes[mapID].clearLayers();
									}
									
									// set shapes to show on top of markers but below pop-ups
									if ($scope.map[mapID].getPane('shapes') == undefined) {
										var shapes = $scope.map[mapID].createPane('shapes');
										$scope.map[mapID].getPane('shapes').style.zIndex = 650; 
									}
									
									// Build list of selectedPolygons from DB studySlection
									var studySelectedAreas=studySelection.studySelectedAreas;
									$scope.selectedPolygon[mapID] = [];									
									for (var i=0; i<studySelectedAreas.length; i++) {
										if (studySelectedAreas[i].id) {
											$scope.selectedPolygon[mapID].push(studySelectedAreas[i].id);
										}
										else if (i == 0) {
											$scope.consoleDebug("[rifc-util-mapping.js] mapID: " + mapID + 
												"; no ID key found for " + 
												"; studySelectedAreas[0] " + JSON.stringify(studySelectedAreas[i]));
										}
									}
									
									// Debug info
									selectedShapes=studySelection.studyShapes;									
									for (var i=0; i<selectedShapes.length; i++) {
										var points=0;
										if (selectedShapes[i].geojson &&
											selectedShapes[i].geojson.geometry.coordinates[0]) {
											points=selectedShapes[i].geojson.geometry.coordinates[0].length;
										}
										$scope.consoleDebug("[rifc-util-mapping.js] mapID: " + mapID + 
											"; selectedShape[" + i + "] " +
											"band: " + selectedShapes[i].band +
											"; color[" + (selectedShapes[i].band-1) + "]: " + 
												$scope.selectorBands.bandColours[selectedShapes[i].band-1] +
											"; circle: " + selectedShapes[i].circle +
											"; freehand: " + selectedShapes[i].freehand +
											"; points: " + points);
									}	
									
									// Set shapes show/hide state
									if (SelectStateService.getState().showHideSelectionShapes) {
										$scope.bShowHideSelectionShapes[mapID] = true;
										if (!$scope.map[mapID].hasLayer($scope.shapes[mapID])) {
											$scope.consoleDebug("[rifc-util-mapping.js] add shapes layerGroup to map: " + mapID);
											$scope.map[mapID].addLayer($scope.shapes[mapID]);
										}
										
										if ($scope.shapes[mapID].getLayers().length == 0) {
											$scope.consoleDebug("[rifc-util-mapping.js] start addSelectedShapes(): shapes layerGroup has no layers for map: " + 
												mapID);				
										}
										else {
											$scope.consoleDebug("[rifc-util-mapping.js] start addSelectedShapes(): shapes layerGroup has " +
												$scope.shapes[mapID].getLayers().length + " layers for map: " + mapID);
										}
									} 
									else {
										if ($scope.map[mapID].hasLayer($scope.shapes[mapID])) {
											$scope.consoleDebug("[rifc-util-mapping.js] remove shapes layerGroup from map: " + mapID);
											$scope.map[mapID].removeLayer($scope.shapes[mapID]);
										}
										$scope.bShowHideSelectionShapes[mapID] = false;
									}	

									$scope.consoleDebug("[rifc-util-mapping.js] mapID: " + mapID + 
										"; showHideSelectionShapes: " + SelectStateService.getState().showHideSelectionShapes +
										"; $scope.bShowHideSelectionShapes: " + $scope.bShowHideSelectionShapes[mapID] + 
										"; $scope.map[" + mapID + "].hasLayer($scope.shapes[" + mapID + "]): " + 
											$scope.map[mapID].hasLayer($scope.shapes[mapID]));
										
									// Add shapes to layer group
									for (var i = 0; i < selectedShapes.length; i++) {
										var selectedShape=selectedShapes[i];
										
										// Shape info highlighter
								
										function selectedShapesHighLightFeature(e, selectedShape) {
											$scope.consoleDebug("[rifc-util-mapping.js] mapID: " + mapID + " selectedShapesHighLightFeature " + 
												"(" + e.target._leaflet_id + "; for: " + e.originalEvent.currentTarget._leaflet_id + 
												"; " + JSON.stringify(e.target._latlng) + "): " +
												(JSON.stringify(selectedShape.properties) || "no properties"));
//											$scope.shapeInfoUpdate(mapID, selectedShape, e.target._latlng);
										}									
										function selectedShapesResetFeature(e) {
											$scope.consoleDebug("[rifc-util-mapping.js] mapID: " + mapID + " selectedShapesResetFeature " +  
												"(" + e.target._leaflet_id +  "; for: " + e.originalEvent.currentTarget._leaflet_id + 
												"; " + JSON.stringify(e.target._latlng) + "): " +
												(JSON.stringify(selectedShape.properties) || "no properties"));
//											$scope.shapeInfoUpdate(mapID, undefined, e.target._latlng);
										} 	
										
										if (selectedShape.circle) { // Represent circles as a point and a radius
										
											if ((selectedShape.band == 1) || (selectedShape.band > 1 && !selectedShape.finalCircleBand)) {
												// basic shape to map shapes layer group
												var circle = new L.Circle([selectedShape.latLng.lat, selectedShape.latLng.lng], {
														pane: 'shapes', 
														band: selectedShape.band,
														area: selectedShape.area,
														radius: selectedShape.radius,
														color: ($scope.selectorBands.bandColours[selectedShapes[i].band-1] || 'blue'),
														weight: ($scope.selectorBands.weight || 3),
														opacity: ($scope.selectorBands.opacity || 0.8),
														fillOpacity: ($scope.selectorBands.fillOpacity || 0),
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
													$scope.shapes[mapID].addLayer(circle);
													$scope.consoleDebug("[rifc-util-mapping.js] mapID: " + mapID + " addSelectedShapes(): " +
														"adding circle: " + JSON.stringify(selectedShape.latLng) + 
														"; color[" + (selectedShapes[i].band-1) + "]: " + ($scope.selectorBands.bandColours[selectedShapes[i].band-1] || 'blue') + 
														"; radius: " + selectedShape.radius + 
														"; band: " + selectedShape.band +
														"; area: " + selectedShape.area);
												}
												else {
													$scope.showError("Could not restore circle");
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
													$scope.shapes[mapID].addLayer(marker);
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
													color: ($scope.selectorBands.bandColours[selectedShapes[i].band-1] || 'blue'),
													weight: ($scope.selectorBands.weight || 3),
													opacity: ($scope.selectorBands.opacity || 0.8),
													fillOpacity: ($scope.selectorBands.fillOpacity || 0),
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
												$scope.shapes[mapID].addLayer(polygon);
												$scope.consoleDebug("[rifc-util-mapping.js] mapID: " + mapID + " addSelectedShapes(): adding polygon" + 
													"; band: " + selectedShape.band +
													"; area: " + selectedShape.area +
													"; freehand: " + selectedShape.freehand +
													"; " + coordinates.length + " coordinates; " +
													JSON.stringify(coordinates).substring(0,100) + "...");							
											}
											else {
												$scope.consoleDebug("[rifc-util-mapping.js] mapID: " + mapID + " addSelectedShapes(): L.Polygon is undefined" +
													"; geoJSON: " + JSON.stringify(selectedShape.geojson, null, 1));
												if (selectedShape.freehand) {	
													$scope.showError("Could not restore freehand Polygon shape");
												}
												else {
													$scope.showError("Could not restore shapefile Polygon shape");
												}
											}
											
										}
									} /* end of for add shapes to layer group loop */
									
						
									$scope.map[mapID].whenReady(function() {
										$scope.consoleDebug("[rifc-util-mapping.js] mapID: " + mapID + 
											"; end addSelectedShapes(): shapes layerGroup has " +
											$scope.shapes[mapID].getLayers().length + " layers");
											
										$timeout(function() {

											$scope.zoomToSelection(mapID); // Zoom to selection	
											$timeout(function() {								
												$scope.refresh(mapID);
											}, 100);			
										}, 100);			
									}); 
												
									ChoroService.setType(mapID, $scope.myService.getState().studyType[mapID]);
									
									addSelectedShapesCallback();
								}	
								else {
									$scope.consoleLog("[rifc-util-mapping.js] mapID: " + mapID + " no selected shapes");
									// Remove old layers
									if ($scope.shapes[mapID].getLayers().length > 0) {
										$scope.shapes[mapID].clearLayers();
									}
									
									$scope.$parent.studyType[mapID]="DISEASE MAPPING";
                                    $scope.$parent.isDiseaseMapping=true;
                                    $scope.$parent.isRiskAnalysis=false;
									$scope.$parent.isDiseaseMappingStudy[mapID] = true;
									$scope.myService.getState().studyType[mapID] = "Disease Mapping";
//									ChoroService.resetState(mapID);
									ChoroService.setType(mapID, $scope.myService.getState().studyType[mapID]);
									// Will need to set print state here

									addSelectedShapesCallback();
								}
							});
					}
					else {
						// Remove old layers
						if ($scope.shapes[mapID].getLayers().length > 0) {
							$scope.shapes[mapID].clearLayers();
						}
						addSelectedShapesCallback("[rifc-util-mapping.js] mapID: " + mapID + " no studyID");
					}
				}			
				
				// method that we will use to update the control based on feature properties passed
				$scope.shapeInfoUpdate = function (mapID, savedShape, latLng /* Of shape, not mouse! */) {
					this._div=L.DomUtil.get('infobox');
					
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
								
							for (var property in savedShape.properties) {
								if (property == 'area') {
									if (savedShape.area === undefined) {
										this._div.innerHTML+= '<b>Area: ' + savedShape.properties[property] + ' square km</b><br />'
									}
								}
								else if (property != '$$hashKey') {
									this._div.innerHTML+= '<b>' + property + ': ' + savedShape.properties[property] + '</b><br />';
								}
							}
							this._div.innerHTML += '<b>Band: ' + (savedShape.band || "unknown") + '</b><br />';
//							this._div.innerHTML += '<b>Areas selected: ' + (bandCount[savedShape.band] || 0) + '/' +
//								latlngList.length +  '</b><br />';
						}
						else if ($scope.shapes[mapID].getLayers().length > 0) {
							this._div.innerHTML = '<h4>Mouse over selection shapes to show properties</br>' +
								'Hide selection shapes to mouse over area names</h4>';
						}
						else {
							this._div.innerHTML = '<h4>Mouse over area names</h4>';
						}
//						this._div.innerHTML += '<b>Centroids: ' + $scope.centroid_type + '</b>';
					}
					else {
						$scope.consoleError("[rifc-util-mapping.js] mapID: " + mapID + " shapeInfoUpdate() no infoBox element found");
					}
				};
						
				// Zoom to selection
				$scope.zoomToSelection = function (mapID) {
					var studyBounds = new L.LatLngBounds();
					if (angular.isDefined($scope.geoJSON[mapID] && $scope.geoJSON[mapID]._geojsons && $scope.geoJSON[mapID]._geojsons.default)) {
						$scope.geoJSON[mapID]._geojsons.default.eachLayer(function (layer) {
							for (var i = 0; i < $scope.selectedPolygon[mapID].length; i++) {
								if ($scope.selectedPolygon[mapID][i].id === layer.feature.properties.area_id) {
									studyBounds.extend(layer.getBounds());
								}
							}
						});
						if (studyBounds.isValid()) {
							$scope.map[mapID].fitBounds(studyBounds);
						}
					}
				};
						
				// Bring shapes to front by descending band order; lowest in front (so mouseover/mouseout works!)
				$scope.bringShapesToFront = function(mapID) {
					var layerCount=0;
					var maxBands=0;
					var shapeLayerOptionsBanderror=0;
					var shapeLayerBringToFrontError=0;
					
					if ($scope.shapes[mapID]) {
						var shapesLayerList=$scope.shapes[mapID].getLayers();
						var shapesLayerBands = {};
						var shapesLayerAreas = {};
						var useBands=false;
						
						for (var i=0; i<shapesLayerList.length; i++) {
							var shapeLayer=shapesLayerList[i];
							if (shapeLayer.options.icon) { // Factory icon - ignore
							}										
							else if (shapeLayer.options.band == undefined) {	
								$scope.consoleLog("[rifc-util-mapping.js] mapID: " + mapID + "; cannot resolve shapesLayerList[" + i + 
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
									shapesLayerAreas[shapeLayer.options.area].push($scope.shapes[mapID].getLayerId(shapeLayer));
								}
								else {
									useBands=true;
								}
								shapesLayerBands[shapeLayer.options.band].push($scope.shapes[mapID].getLayerId(shapeLayer));
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
							$scope.consoleDebug("[rifc-util-mapping.js] mapID: " + mapID + "; sorted shape areas: " + shapesLayerAreaList.length + 
								"; " + JSON.stringify(shapesLayerAreaList));
//							if ($scope.areaNameList[mapID] == undefined) {
								$scope.areaNameList[mapID] = createAreaNameList(mapID);
//							}
							
							for (var k=0; k<shapesLayerAreaList.length; k++) {
									
								for (var area in shapesLayerAreas) {
									if (area == shapesLayerAreaList[k]) {
										var areaIdList=shapesLayerAreas[area];
										for (var l=0; l<areaIdList.length; l++) {
											var shapeLayer=$scope.shapes[mapID].getLayer(areaIdList[l]);
											if (shapeLayer && typeof shapeLayer.bringToFront === "function") { 
												if ($scope.areaNameList[mapID] == undefined) {
													$scope.consoleDebug("[rifc-util-mapping.js] mapID: " + mapID + 
														"; bring layer: " + areaIdList[l] + " to front" +
														"; band: " + shapeLayer.options.band +
														"; area: " + shapeLayer.options.area +
														"; polygons: unknwon");
													shapeLayer.bringToFront();
												}
												else if (shapeLayer.options.band && $scope.areaNameList[mapID] &&
													$scope.areaNameList[mapID][shapeLayer.options.band] &&
													$scope.areaNameList[mapID][shapeLayer.options.band].length > 0) {
													$scope.consoleDebug("[rifc-util-mapping.js] mapID: " + mapID + 
														"; bring layer: " + areaIdList[l] + " to front" +
														"; band: " + shapeLayer.options.band +
														"; area: " + shapeLayer.options.area +
														"; polygons: " + $scope.areaNameList[mapID][shapeLayer.options.band].length);
													shapeLayer.bringToFront();
												}
												else {
													$scope.consoleDebug("[rifc-util-mapping.js] mapID: " + mapID + 
														"; ignore layer: " + areaIdList[l] + " to front" +
														"; band: " + shapeLayer.options.band +
														"; area: " + shapeLayer.options.area +
														"; no polygons");
												}
											}
											else {		
												shapeLayerBringToFrontError++;
												$scope.consoleLog("[rifc-util-mapping.js] mapID: " + mapID + 
													"; cannot resolve shapesLayerAreas[" + area + 
													"][" + l + "].bringToFront()");
											}
										}
									}
								}
							}
						}
						else { // Use bands
							
							for (var j=maxBands; j>0; j--) { 
								$scope.consoleDebug("[rifc-util-mapping.js] mapID: " + mapID + " band: " + j + "/" + maxBands + 
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
										$scope.consoleLog("[rifc-util-mapping.js] mapID: " + mapID + " cannot resolve shapesLayerBands[" + j + 
											"][" + k + "].bringToFront()");
									}
								}
							}
						} 
						
						$scope.consoleDebug("[rifc-util-mapping.js] mapID: " + mapID + " brought " + layerCount + " shapes in " + 
							maxBands + " layer(s) to the front");
						if (shapeLayerOptionsBanderror > 0) {	
							$scope.showError("[rifc-util-mapping.js] mapID: " + mapID + " no band set in shapeLayer options (" + 
								shapeLayerOptionsBanderror + ")");
						}
						if (shapeLayerBringToFrontError > 0) {
							$scope.showError("[rifc-util-mapping.js] mapID: " + mapID + " shapeLayer bingToFront() error (" + 
								shapeLayerBringToFrontError + ")");
						}
					}
				}; 

				// Area name list by band
				function createAreaNameList(mapID) { // Not from latlngList - not in scope when restored
					var studySelectedAreas=SelectStateService.getState().studySelection.studySelectedAreas;
					if (studySelectedAreas) {
						newAreaNameList = {};
						
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
											
					return newAreaNameList;
				}
						
                /*
                 * Tidy up on error
                 */
                function clearTheMapOnError(mapID) {
					$scope.consoleLog("[rifc-util-mapping.js] clearTheMapOnError: " + mapID);
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

				$scope.$on('rrZoomStatus', function (event, rrZoomStatus) { // For trace from rifd-dmap-d3rrzoom.js
					if (rrZoomStatus.level = "DEBUG") {
						$scope.consoleDebug("[rifc-util-mapping.js] rrZoomStatus: " + rrZoomStatus.msg);
					}
					else if (rrZoomStatus.level = "WARNING") {
						$scope.showWarning("[rifc-util-mapping.js] rrZoomStatus: " + rrZoomStatus.msg);
					}
					else if (rrZoomStatus.level = "ERROR") {
						$scope.showError("[rifc-util-mapping.js] rrZoomStatus: " + rrZoomStatus.msg);
					}
					else if (rrZoomStatus.level = "INFO") {
						$scope.consoleLog("[rifc-util-mapping.js] rrZoomStatus: " + rrZoomStatus.msg);
					}
					else {
						$scope.consoleDebug("[rifc-util-mapping.js] rrZoomStatus: " + JSON.stringify(rrZoomStatus, null, 0));
					}
				});
													
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
                //Get the possible studies initially
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
/*							
							else if (res.data.smoothed_results[i].study_state === "W") { // R warning. [NOT USED BY MIDDLEWARE]
                                var thisStudy = {
                                    "study_id": res.data.smoothed_results[i].study_id,
                                    "name": res.data.smoothed_results[i].study_name
                                };
                                $scope.studyIDs.push(thisStudy);
                            } */
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
                        $scope.showError("Unable to retrieve study status");
						$scope.consoleError("[rifc-util-mapping.js] Unable to retrieve study status: " + 
							JSON.stringify(e));
                    });
                };

                $scope.updateSex = function (mapID) {
                    if ($scope.studyID[mapID] !== null && angular.isDefined($scope.studyID[mapID])) {
                        //Store this study selection
                        $scope.myService.getState().study[mapID] = $scope.studyID[mapID]; // Set studyID in ViewerStateService or MappingStateService	
						$scope.consoleDebug("[rifc-util-mapping.js] set myService - updateSex: " + 
							"; studyID: " + JSON.stringify($scope.myService.getState().study[mapID]) +
							"; studyType: " + $scope.myService.getState().studyType[mapID]);
							
                        //Get the sexes for this study
						if ($scope.studyID[mapID].study_id) {
							user.getSexesForStudy(user.currentUser, $scope.studyID[mapID].study_id, mapID)
									.then(handleSexes, function getSexesForStudy(e) {
										$scope.showWarning("Error " + e + "; getting sexes for study: " + $scope.studyID[mapID].study_id + "; map: " + mapID);
										clearTheMapOnError(mapID);
									});
						}
						else {
							$scope.showWarning("No study ID defined for map: " + mapID);
						}

                        function handleSexes(res) {
							$scope.consoleDebug("[rifc-util-mapping.js] handleSexes: " + JSON.stringify(res, null, 0));
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
				/* Needs a rename to renderBaseMap() [all similar functions]; called from:
				 * 
				 * src\main\resources\dashboards\mapping\controllers\rifc-dmap-main.js:                    $scope.child.renderMap("diseasemap1");
				 * src\main\resources\dashboards\mapping\controllers\rifc-dmap-main.js:                    $scope.child.renderMap("diseasemap2");
				 *
				 * This is wrong as a) $scope.tileInfo[mapID].geography is not in scope
				 * Needs to be called when you change the study ID 
				 */
                $scope.renderMap = function (mapID, currentBaseMapInUse, renderMapCallback) {
					
					function setBaseMapCallback(err, mapID) {
						if (err) { // LeafletBaseMapService.setDefaultMapBackground had error
							$scope.consoleLog("[rifc-util-mapping.js] WARNING LeafletBaseMapService.setDefaultMapBackground for map: " + mapID + 
								" had error: " + err);
							if (renderMapCallback && typeof renderMapCallback === "function") {
								renderMapCallback("[rifc-util-mapping.js] WARNING LeafletBaseMapService.setDefaultMapBackground for map: " + mapID + 
								" had error: " + err);
							}
						}				
						var getCurrentBaseMap=LeafletBaseMapService.getCurrentBaseMapInUse(mapID);
							
						if ($scope.studyID[mapID].study_id) {
							$scope.consoleDebug("[rifc-util-mapping.js] renderMap (basemap) for mapID: " + mapID + 
								"; study: " + $scope.studyID[mapID].study_id + 
								"; sex: " + $scope.sex[mapID] + 
								"; getCurrentBaseMap: " + getCurrentBaseMap);			
						}
						else {
							$scope.consoleDebug("[rifc-util-mapping.js] renderMap (basemap) for mapID: " + mapID + 
								"; study: not set; sex: not set ; getCurrentBaseMap: " + getCurrentBaseMap);	
						}
						
						if ($scope.thisLayer[mapID]) {
							$scope.map[mapID].removeLayer($scope.thisLayer[mapID]);
						}
						
						//add new baselayer if requested
						if (!LeafletBaseMapService.getNoBaseMap(mapID)) {
							
							var currentBaseMapInUse= (($scope.thisLayer[mapID] && $scope.thisLayer[mapID].name) ? 
								$scope.thisLayer[mapID].name : undefined);
								
							var thisGeography = $scope.tileInfo[mapID].geography;
							$scope.thisLayer[mapID] = LeafletBaseMapService.setBaseMap(getCurrentBaseMap);
							CommonMappingStateService.getState(mapID).setBasemap(getCurrentBaseMap, false /* no basemap*/);
							var basemapError=CommonMappingStateService.getState(mapID).getBasemapError(getCurrentBaseMap);
							if (basemapError == 0) {
								$scope.thisLayer[mapID].on("load", function() { 
									var basemapError=CommonMappingStateService.getState(mapID).getBasemapError(getCurrentBaseMap);
									if (LeafletBaseMapService.getNoBaseMap(mapID)) { // Has been disabled by error
									
									}
									else if (getCurrentBaseMap != currentBaseMapInUse) {
										if (basemapError == 0) {
											$scope.showSuccess("Change current base map in use to: " + getCurrentBaseMap);
										}
										else {
											$scope.showWarning("Unable to change current base map in use from: " + currentBaseMapInUse + 
												"; to: " + getCurrentBaseMap + "; " + basemapError +
												" tiles loaded with errors");
											LeafletBaseMapService.setNoBaseMap(mapID, true); // Disable
										}
									}
									else {	
										if (basemapError == 0) {
											$scope.consoleLog("[rifc-util-mapping.js] setCurrentBaseMapInUse for map: " + mapID + 
												"; currentBaseMapInUse: " + currentBaseMapInUse + 
												"; getCurrentBaseMap: " + getCurrentBaseMap);
										}
										else {	
											$scope.showWarning("Unable to set base map to: " + getCurrentBaseMap + "; " + basemapError +
												" tiles loaded with errors");
											LeafletBaseMapService.setNoBaseMap(mapID, true); // Disable
										}
									}
								});
								$scope.thisLayer[mapID].on("tileerror", function() { 
									CommonMappingStateService.getState(mapID).basemapError(getCurrentBaseMap);
								});
								
								$scope.thisLayer[mapID].addTo($scope.map[mapID]);
                            }	
							else {
								$scope.consoleLog("[rifd-dsub-maptable.js] setBaseMap: " + getCurrentBaseMap +
									" disabled by previous basemapErrors: " + basemapError);
							}				
						}	
						else {
							$scope.consoleLog("[rifd-dsub-maptable.js] setBaseMap: " + getCurrentBaseMap + " disabled by getNoBaseMap");
						}
						
						if (renderMapCallback && typeof renderMapCallback === "function") {
							renderMapCallback();
						}
					};
				
					var thisGeography = $scope.tileInfo[mapID].geography;
					var getCurrentBaseMap=LeafletBaseMapService.getCurrentBaseMapInUse(mapID);
					
/*
+141.4: [rifc-util-mapping.js] use CommonMappingStateService.getState("viewermap").basemap for map: viewermap; geography: SAHSULAND; 
                               currentBaseMapInUse: undefined; getCurrentBaseMap: Thunderforest Transport
 */
					if (currentBaseMapInUse && getCurrentBaseMap) {
						$scope.consoleLog("[rifc-util-mapping.js] use currentBaseMapInUse for map: " + mapID + 
							"; geography: " + thisGeography + 
							"; currentBaseMapInUse: " + currentBaseMapInUse + 
							"; getCurrentBaseMap: " + getCurrentBaseMap);
						LeafletBaseMapService.setCurrentBaseMapInUse(mapID, currentBaseMapInUse);
						setBaseMapCallback(undefined /* No error */, mapID);
					}
//					else if (CommonMappingStateService.getState(mapID).basemap) {
//						$scope.consoleLog('[rifc-util-mapping.js] use CommonMappingStateService.getState("' + mapID + '").basemap for map: ' + 
//							mapID + 
//							"; geography: " + thisGeography + 
//							"; currentBaseMapInUse: " + currentBaseMapInUse + 
//							"; getCurrentBaseMap: " + getCurrentBaseMap);
//						LeafletBaseMapService.setCurrentBaseMapInUse(mapID, CommonMappingStateService.getState(mapID).basemap);
//						LeafletBaseMapService.setNoBaseMap(mapID, (CommonMappingStateService.getState(mapID).noBasemap || false));
//						setBaseMapCallback(undefined /* No error */, mapID);
//					}
					else if (thisGeography) {
						$scope.consoleLog("[rifc-util-mapping.js] setDefaultMapBackground for map: " + mapID + 
							"; geography: " + thisGeography + 
							"; currentBaseMapInUse: " + currentBaseMapInUse + 
							"; getCurrentBaseMap: " + getCurrentBaseMap);	
						LeafletBaseMapService.setDefaultMapBackground(thisGeography, setBaseMapCallback, mapID);
					}
					else {
						LeafletBaseMapService.setDefaultBaseMap(mapID);
						$scope.consoleLog("[rifc-util-mapping.js] WARNING unable to LeafletBaseMapService.setDefaultMapBackground; no geography defined for map: " +
							mapID + 
							"; using default basemap: " + LeafletBaseMapService.getCurrentBaseMapInUse(mapID));
						setBaseMapCallback(undefined /* No error */, mapID);
					}
				};

                //Draw the map
                $scope.refresh = function (mapID) {
					if (!$scope.initialRefresh[mapID]) {
						$scope.consoleDebug("[rifc-util-mapping.js] initial refresh start for mapID: " + mapID);
						$scope.initialRefresh[mapID]=true;
					}
					else {
						$scope.consoleDebug("[rifc-util-mapping.js] refresh start for mapID: " + mapID);
					}					
                    //get choropleth map renderer
                    $scope.attr[mapID] = ChoroService.getMaps(mapID).feature;
                    thisMap[mapID] = ChoroService.getMaps(mapID).renderer;
					
                    //not a choropleth, but single colour
                    if (thisMap[mapID].range && thisMap[mapID].range.length === 1) {
                        //remove existing legend
                        if ($scope.legend[mapID]._map) {
                            $scope.map[mapID].removeControl($scope.legend[mapID]);
                        }
                        if ($scope.geoJSON[mapID]._geojsons &&
						    $scope.geoJSON[mapID]._geojsons.default && 
							angular.isDefined($scope.geoJSON[mapID]._geojsons.default)) {
                            $scope.geoJSON[mapID]._geojsons.default.eachLayer($scope.handleLayer);
                        }
                    }
					else {
						//remove old legend and add new
						$scope.legend[mapID].onAdd = ChoroService.getMakeLegend(thisMap[mapID], $scope.attr[mapID]);
						
						if (thisMap[mapID].breaks.length != (thisMap[mapID].range.length-1)) {
							throw new Error("[rifc-util-mapping.js] " + mapID + " thisMap.breaks: " + thisMap[mapID].breaks.length +
								" length != thisMap.range: " + thisMap[mapID].range.length + " -1 length");
						}
											
						if ($scope.legend[mapID]._map) { //This may break in future leaflet versions
							$scope.map[mapID].removeControl($scope.legend[mapID]);
						}
						$scope.legend[mapID].addTo($scope.map[mapID]);
						//force a redraw
						
						$scope.map[mapID].whenReady(function() {
							$timeout(function() {					
									if (angular.isDefined($scope.geoJSON[mapID]._geojsons.default)) {
										$scope.geoJSON[mapID]._geojsons.default.eachLayer($scope.handleLayer);
									}
								}, 500);	
								
								$scope.bringShapesToFront(mapID);
										
								$scope.map[mapID].whenReady(function() {
									$timeout(function() {										
											
											$scope.consoleLog("[rifc-util-mapping.js] redraw map");
											$scope.map[mapID].fitBounds($scope.map[mapID].getBounds()); // Force map to redraw after 0.2s delay
										}, 500);	
								});	
						});	
					}
					
                    //draw histogram [IT MUST BW HERE OR D3 GETS CONFUSED!]
					callGetD3chart = function(mapID) {
						
						if ($scope.tableData[mapID].length == 0) {
							$scope.consoleDebug("[rifc-util-mapping.js] map data not ready for mapID: " + mapID);	
							setTimeout(callGetD3chart, 1000, mapID);		
						}
						else {
							$scope.getD3chart(mapID, $scope.attr[mapID]); // Crashes firefox	
							$scope.$broadcast('rrZoomReset', {msg: "watchCall reset: " + mapID});
						
							$scope.consoleDebug("[rifc-util-mapping.js] refresh completed for mapID: " + mapID);	
						}
					}
					setTimeout(callGetD3chart, 1000, mapID);		 
                }; //End of refresh(0)

                //remove rsub layer
				$scope.removeSubLayer  = function (layer) {
                    var mapID = layer.options.mapID;
					$scope.layerStats.subLayerRemoves++;
//					layer.remove(); 	// Remove [should be done by TopoJSONGridLayer.js]
//					layer=undefined;
				}
				
                //apply relevent renderer to layer
                $scope.handleLayer = function (layer) {
                    var mapID = layer.options.mapID;
					$scope.layerStats.subLayerRefreshes++;
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
                        var polyStyle = ChoroService.getRenderFeatureMapping(undefined, thisAttr, selected);
						if (thisMap["viewermap"].scale) {
							polyStyle = ChoroService.getRenderFeatureMapping(thisMap["viewermap"].scale, thisAttr, selected);
						}
                        layer.setStyle({
                            weight: 1,
                            color: "gray",
                            fillColor: polyStyle,
                            fillOpacity: $scope.child.transparency[mapID]
                        });
                    } else {
						if (mapID === undefined) { // Occurs only on SQL Server!
//							Do nothing!						
							$scope.consoleError("[rifc-util-mapping.js] Null mapID; layer options: " + JSON.stringify(layer.options, null, 2));
							if (layer !== undefined) {
								$scope.layerStats.LayerRemoves++;
								layer.remove(); 	// Remove 
								layer=undefined;
							}
						}
						else if ($scope.tableData[mapID] === undefined) {
							$scope.showError("Invalid table data for mapID: " + mapID);
							clearTheMapOnError(mapID);
						}
                        else if ($scope.tableData[mapID].length !== 0) {
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
						else {
							$scope.layerStats.LayerWarnings++;
							if ($scope.layerStats.Layerwarnings < 20) {
								$scope.consoleDebug("[rifc-util-mapping.js] No table data for mapID: " + mapID + " [20 warnings max.]"); // You will get 1000's of these!!	
							}
						}
                    }
                };
				
				// Render geoJSON map
				// Called from $scope.geoJSON[mapID].on('load', ...) function
				$scope.defaultRenderMap = function (mapID) {
					
					var choroScope = {
						input: {},
						mapID: mapID,
						options: [],
						domain: [],
						tableData: {},
						consoleLog: $scope.consoleLog,
						consoleError: $scope.consoleError,
						consoleDebug: $scope.consoleDebug,
						showError: $scope.showError,
						showWarning: $scope.showWarning
					}
					choroScope.input.isDefault = ChoroService.getMaps(mapID).isDefault;
					choroScope.input.checkboxInvert = ChoroService.getMaps(mapID).invert;
					choroScope.input.selectedSchemeName = ChoroService.getMaps(mapID).brewerName;
					choroScope.input.intervalRange = ColorBrewerService.getSchemeIntervals(choroScope.selectedSchemeName);
					choroScope.input.selectedN = ChoroService.getMaps(mapID).intervals;
					choroScope.input.method = ChoroService.getMaps(mapID).method;
					choroScope.input.classifications = ChoroService.getMaps(mapID).classifications;
                    var colorBrewerList = ColorBrewerService.getSchemeList();
                    for (var j in colorBrewerList) {
                        choroScope.options.push({name: colorBrewerList[j], image: 'images/colorBrewer/' + colorBrewerList[j] + '.png'});
                    }
					//set saved swatch selection
					var cb = ChoroService.getMaps(mapID).brewerName;
					for (var i = 0; i < choroScope.options.length; i++) {
						if (choroScope.options[i].name === cb) {
							choroScope.input.currOption = choroScope.options[i];
						}
					}

					//list of attributes
					choroScope.input.features = ChoroService.getMaps(mapID).features;
					if (choroScope.input.features.indexOf(ChoroService.getMaps(mapID).feature) === -1) {
						choroScope.input.selectedFeature = choroScope.input.features[0];
					} else {
						choroScope.input.selectedFeature = ChoroService.getMaps(mapID).feature;
					}
				
					choroScope.brewerName = ChoroService.getMaps(mapID).brewerName;
					choroScope.invert = ChoroService.getMaps(mapID).invert;
					choroScope.brewer = ChoroService.getMaps(mapID).brewer;
					choroScope.intervals = ChoroService.getMaps(mapID).intervals;
					choroScope.feature = ChoroService.getMaps(mapID).feature;
					choroScope.method = ChoroService.getMaps(mapID).method;
					choroScope.renderer = ChoroService.getMaps(mapID).renderer;
					
//					$scope.consoleDebug("[rifc-util-mapping.js] defaultRenderMap() mapID: " + mapID + "; choroScope: " + JSON.stringify(choroScope, null, 2)); 
					choroScope.tableData[mapID]=$scope.tableData[mapID];	
					// XXXX TEMPORARILIY DISABLED
					// +16.6: WARNING: renderSwatch() being called too early choroScope.input.methodObj not defined: 
					// {"isDefault":true,"checkboxInvert":true,"selectedSchemeName":"PuOr","intervalRange":[],"selectedN":9,"method":"AtlasRelativeRisk","currOption":{"name":"PuOr","image":"images/colorBrewer/PuOr.png"},"features":[]}
//					try {
//						ChoroService.doRenderSwatch(
//							true /* Called on modal open */, 
//							true /* Secret field, always true */, 
//							choroScope, 
//							ColorBrewerService);
//					}
//					catch (e) {
//						$scope.consoleError("[rifc-util-mapping.js] Caught error in doRenderSwatch(): " + 
//							JSON.stringify(e));
//					}
					
					$scope.input=choroScope.input;
					$scope.domain=choroScope.domain;
					
					var savedMapState = {
						input: choroScope.input,
//						domain:	choroScope.domain,  // Contains functions - will cause XML parse errors!
						maps: {					// Probably not needed apart from the initial state
							features: [],
							brewerName: choroScope.brewerName,
							intervals: choroScope.intervals,
							feature: choroScope.feature,
							invert: choroScope.invert,
							method: choroScope.method,
							isDefault: false,
							renderer: { //  May need more here
								scale: null,
								breaks: [],
								range: ["#9BCD9B"],
								mn: null,
								mx: null
							},
							init: false
						}
					};
					savedMapState.maps=ChoroService.getMaps(mapID);
					
				}
				
				$scope.createTopoJSONLayer = function (mapID) {
					var topojsonURL = user.getTileMakerTiles(user.currentUser, $scope.tileInfo[mapID].geography, $scope.tileInfo[mapID].level);
					
					if (!$scope.geoJSON[mapID]) { // Created in: mapping\controllers\rifc-dmap-main.js, viewer\controllers\rifc-view-viewer.js:
						$scope.consoleError("[rifc-util-mapping.js] Unable to create topoJsonGridLayer for mapID: " + mapID + 
							"; no map");
						return;
					}
								
					$scope.consoleDebug("[rifc-util-mapping.js] create topoJsonGridLayer for mapID: " + mapID + 
						"; Geography: " + $scope.tileInfo[mapID].geography +
						"; Geolevel: " + $scope.tileInfo[mapID].level +
						"; URL: " + topojsonURL +
						"; study: " + $scope.studyID[mapID].study_id + 
						"; sex: " + $scope.sex[mapID]);
					$scope.geoJSON[mapID] = new L.topoJsonGridLayer(topojsonURL, {
						attribution: 'Polygons &copy; <a href="http://www.sahsu.org/content/rapid-inquiry-facility" target="_blank">Imperial College London</a>',
						// Options
						consoleDebug: $scope.consoleDebug,
						consoleError: $scope.consoleError,
						name: mapID + "." + $scope.tileInfo[mapID].geography + "." + $scope.tileInfo[mapID].level, 
													// Should be unique (includes mapID, geography and geolevel name)
//										maxZoom: maxzoomlevel,
						useCache: $scope.parameters.usePouchDBCache,
						auto_compaction: true,
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
										
//										$scope.consoleDebug("[rifc-util-mapping.js] mapID: " + mapID + " onEachFeature " +  
//											"(" + e.target._leaflet_id + "): " + e.type + 
//											"; area: " + layer.feature.properties.name||layer.feature.properties.area_id);
										$scope.infoBox[mapID].update(layer.feature.properties.area_id, 
											layer.feature.properties.name);
										$scope.$parent.thisPolygon[mapID]=(layer.feature.properties.name||layer.feature.properties.area_id||"Unknown");
										$scope.$parent.$digest();
									});
									layer.on('mouseout', function (e) {
//										$scope.consoleDebug("[rifc-util-mapping.js] mapID: " + mapID + " onEachFeature " +  
//											"(" + e.target._leaflet_id + "): " + e.type);
										$scope.geoJSON[mapID]._geojsons.default.eachLayer($scope.handleLayer);
										$scope.infoBox[mapID].update(false);
										$scope.$parent.thisPolygon[mapID]="";
										$scope.$parent.$digest();
									});
									layer.on('click', function (e) {
//										$scope.consoleDebug("[rifc-util-mapping.js] mapID: " + mapID + " onEachFeature " +  
//											"(" + e.target._leaflet_id + "): " + e.type);
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
									});
								}
							}
						}
					}); // End of new L.topoJsonGridLayer()
					
					//force re-render of new tiles								
					$scope.geoJSON[mapID].on('load', function (e) {
						$scope.map[mapID].whenReady(function(e) {			
							if ($scope.geoJSON[mapID]._tiles) {												
								$scope.consoleDebug("[rifc-util-mapping.js] load event for mapID: " + mapID + 
									"; layer stats " + JSON.stringify($scope.layerStats, null, 2) + 
									"; cache stats " + JSON.stringify($scope.cacheStats, null, 2) + 
									"; study: " + $scope.studyID[mapID].study_id + 
									"; sex: " + $scope.sex[mapID] +
									"; tiles: " + Object.keys($scope.geoJSON[mapID]._tiles).length +
									"; zoomlevel: " + $scope.map[mapID].getZoom() +
									"; areas: " + $scope.geoJSON[mapID]._geojsons.default.getLayers().length);	
							}
							else {
								$scope.consoleDebug("[rifc-util-mapping.js] load event for mapID: " + mapID + 
									"; layer stats " + JSON.stringify($scope.layerStats, null, 2) + 
									"; cache stats " + JSON.stringify($scope.cacheStats, null, 2) + 	
									"; study: " + $scope.studyID[mapID].study_id + 
									"; sex: " + $scope.sex[mapID] +
									"; tiles: UNKNOWN" +
									"; zoomlevel: " + $scope.map[mapID].getZoom() +
									"; areas: " + $scope.geoJSON[mapID]._geojsons.default.getLayers().length);
							}
						
							doLoadWork = function(mapID) {							
								$scope.defaultRenderMap(mapID);
								$scope.refresh(mapID);	
								
								if (mapID !== "viewermap") { 
									if ($scope.disableMapLocking) {
										$scope.consoleDebug("[rifc-util-mapping.js] map locking disabled for mapID: " + mapID +
											"; disableMapLocking: " + $scope.disableMapLocking);
									}
									else {			
										$scope.consoleDebug("[rifc-util-mapping.js] map locking enabled for mapID: " + mapID +
											"; disableMapLocking: " + $scope.disableMapLocking);
										$scope.mapLocking();								
									}
								}									
							};
							checkAttributeDataLoaded = function(mapID) {
								if ($scope.initialRefresh[mapID]) {
									$scope.consoleDebug("[rifc-util-mapping.js] attribute data wait already refeshed for mapID: " + mapID +
										"; attribute data loaded: " + $scope.attributeDataLoaded[mapID] + 
										"; clear interval timer: " + $scope.checkAttributeDataLoadedTimer[mapID]);
									clearInterval($scope.checkAttributeDataLoadedTimer[mapID]);
								}
								else if ($scope.attributeDataLoaded[mapID]) {
									$scope.consoleDebug("[rifc-util-mapping.js] attribute data ready for mapID: " + mapID +
										"; clear interval timer: " + $scope.checkAttributeDataLoadedTimer[mapID]);
									clearInterval($scope.checkAttributeDataLoadedTimer[mapID]);
//									$scope.checkAttributeDataLoadedTimer[mapID] = undefined;

//									if ($scope.geoJSON[mapID]._geojsons.default) {
//										$scope.geoJSON[mapID]._geojsons.default.eachLayer($scope.handleLayer);
//									}
//									else {							
//										$scope.consoleError("[rifc-util-mapping.js] geoJSON not yet set up: $scope.geoJSON[mapID]._geojsons.default is NULL");
//									}
									doLoadWork(mapID);
								}
								else {
									$scope.consoleDebug("[rifc-util-mapping.js] attribute data wait for mapID: " + mapID);
								}
							};
							
							if (!$scope.initialRefresh[mapID] && !$scope.attributeDataLoaded[mapID] && $scope.checkAttributeDataLoadedTimer[mapID] == undefined) {
								$scope.checkAttributeDataLoadedTimer[mapID] = setInterval(checkAttributeDataLoaded, 1000, mapID);	
							}	
							else {
								doLoadWork(mapID);
							}																			
						});
					});
								
					$scope.geoJSON[mapID].on('remove', function (e) {
						$scope.layerStats.layerRemoves++;
					});		
					$scope.geoJSON[mapID].on('add', function (e) {
						$scope.layerStats.layerAdds++;
					});	
					$scope.geoJSON[mapID].on('addsublayer', function (stats) {
						$scope.layerStats.subLayerAdds+=stats.subLayerAdds;
						$scope.layerStats.subLayerUpdates+=stats.subLayerUpdates;
					});
					$scope.geoJSON[mapID].on('tileerror', function(error, tile) {
						if ($scope.cacheStats) {
							$scope.layerStats.errors++;
						}
						var msg="";
						if (error && error.message) {
							msg+=error.message;
						}
						if (tile) {
							$scope.consoleError("[rifc-util-mapping.js] Error: loading topoJSON tile: " + 
								(JSON.stringify(tile.coords)||"UNK"));		
						}
					});
					$scope.geoJSON[mapID].on('tilecacheerror', function tileCacheErrorHandler(ev) {
						if ($scope.cacheStats) {
							$scope.cacheStats.errors++;
						}
					});
					$scope.geoJSON[mapID].on('tilecachemiss', function tileCacheErrorHandler(ev) {
						if ($scope.cacheStats) {
							$scope.cacheStats.misses++;
						}
					});
					$scope.geoJSON[mapID].on('tilecachehit', function tileCacheErrorHandler(ev) {
						if ($scope.cacheStats) {
							$scope.cacheStats.hits++;
						}
					});
														
				} // End of createTopoJSONLayer()
				
                $scope.updateStudy = function (mapID) {
                    //Check inputs are valid
                    if ($scope.studyID[mapID] === null || $scope.sex[mapID] === null) {
                        $scope.showError("Invalid study or sex code");
                        clearTheMapOnError(mapID);
                    } 
					else {
						$scope.initialRefresh[mapID]=false;
						$scope.attributeDataLoaded[mapID]=false;
						$scope.consoleDebug("[rifc-util-mapping.js] updateStudy for mapID: " + mapID + "; study: " + $scope.studyID[mapID].study_id + 
							"; sex: " + $scope.sex[mapID]);
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
							$scope.geoJSON[mapID]._geojsons.default.eachLayer($scope.removeSubLayer);
							$scope.map[mapID].removeLayer($scope.geoJSON[mapID]);
							$scope.geoJSON[mapID]={};
							$scope.map[mapID].setView({lat: 0, lng: 0}); // Pay a quick visit to west Africa so the zoom to extent can work!
							$scope.map[mapID].setZoom(1);
                        }
					
                        //save study, sex selection
                        $scope.myService.getState().sex[mapID] = $scope.sex[mapID]; 
                        $scope.myService.getState().study[mapID] = $scope.$parent.studyID[mapID]; // Set studyID in ViewerStateService or MappingStateService
						$scope.consoleDebug("[rifc-util-mapping.js] set myService - sex: " + $scope.myService.getState().sex[mapID] +
							"; studyID: " + JSON.stringify($scope.myService.getState().study[mapID]) +
							"; studyType: " + $scope.myService.getState().studyType[mapID]);

						if (ChoroService.getMaps(mapID) && ChoroService.getMaps(mapID).features) {
							var features=ChoroService.getMaps(mapID).features; // Save feature list
							ChoroService.resetState(mapID);
							ChoroService.getMaps(mapID).features=features;
						}
						else {
							ChoroService.resetState(mapID);
						}
				
						// Add selection shapes (usually risk analysis)
						addSelectedShapes(mapID, addSelectedShapesCallback);
						
						function addSelectedShapesCallback(err) {
							if (err) {
								$scope.showError(err);
							}
							else {	
								if ($scope.myService.getState().studyType[mapID] == undefined) {
									$scope.showError("studyType not defined for map: " + mapID);
								}
								else if ($scope.myService.getState().study[mapID].study_type == undefined) {
									$scope.consoleError("addSelectedShapesCallback() study_type not defined for map: " + mapID + 
										"; study: " + JSON.stringify($scope.myService.getState().study[mapID]));
									$scope.showError("studyType not defined for map: " + mapID + 
										"; study ID: " + $scope.myService.getState().study[mapID].study_id);
								}
								else if ($scope.myService.getState().study[mapID].study_type == undefined) {
									$scope.consoleError("addSelectedShapesCallback() study_type mismatch for map: " + mapID + 
										"; study: " + JSON.stringify($scope.myService.getState().study[mapID]) +
										"; select state for map: " + mapID + " studyType: " + $scope.myService.getState().studyType[mapID] +
										"; database studyType is not yet defined");	
								}
								else if ($scope.myService.getState().studyType[mapID] != $scope.myService.getState().study[mapID].study_type) {
									$scope.consoleError("addSelectedShapesCallback() study_type mismatch for map: " + mapID + 
										"; study: " + JSON.stringify($scope.myService.getState().study[mapID]) +
										"; select state for map: " + mapID + " studyType: " + $scope.myService.getState().studyType[mapID] +
										" != database: " + $scope.myService.getState().study[mapID].study_type);	
								}
								else {
									$scope.consoleLog("addSelectedShapesCallback() study_type OK for map: " + mapID + 
										"; study: " + JSON.stringify($scope.myService.getState().study[mapID]) +
										"; select state for map: " + mapID + " studyType: " + $scope.myService.getState().studyType[mapID] +
										" == database: " + $scope.myService.getState().study[mapID].study_type);	
								}	
								
								user.getGeographyAndLevelForStudy(user.currentUser, $scope.studyID[mapID].study_id).then(
									function geographyAndLevelForStudyProcessing(res) {
										return $q(function(resolve, reject) {
											if (res.data && res.data[0] && res.data[0][0] && res.data[0][1]) { // OK
											}
											else {
												reject("Null data returned by user.getGeographyAndLevelForStudy: " + JSON.stringify(res.data));
											}	
											$scope.tileInfo[mapID].geography = res.data[0][0]; //e.g. SAHSU
											$scope.tileInfo[mapID].level = res.data[0][1]; //e.g. LEVEL3
											$scope.consoleDebug("[rifc-util-mapping.js] set tileInfo for map: " + mapID + ": " + 
												"; geography: " + $scope.tileInfo[mapID].geography +
												"; level: " + $scope.tileInfo[mapID].level);
						
											function renderMapCallback(err) {
												if (err) {
													reject(err);
												}
											}
											$scope.renderMap(mapID, undefined /* currentBaseMapInUse */, renderMapCallback);
											
											$scope.consoleDebug("[rifc-util-mapping.js] set studyType for map: " + mapID + ": " + 
												$scope.myService.getState().studyType[mapID] + 
												"; studyID: " + JSON.stringify($scope.studyID[mapID], null, 1) + 
												"; mappingDefaults: " + JSON.stringify($scope.parameters.mappingDefaults[mapID], null, 1));

											$scope.consoleDebug("[rifc-util-mapping.js] setup ChoroService, mapID: " + mapID + 
												"; study_id: " + $scope.studyID[mapID].study_id +
												"; Saved map state: " + JSON.stringify(ChoroService.getMaps(mapID), null, 2));
											
											getAttributeTable(mapID, // Needs study_id, sex
												function getAttributeTableCallBack(msg) {
													$scope.consoleDebug(msg);
													$scope.createTopoJSONLayer(mapID);
													$scope.consoleDebug("[rifc-util-mapping.js] completed topoJsonGridLayer for mapID: " + mapID + 
														"; study: " + $scope.studyID[mapID].study_id);												
													$scope.map[mapID].addLayer($scope.geoJSON[mapID]); // Add layer to map
																
													$scope.map[mapID].whenReady(function(e) {	
														//pan events                            
															$scope.map[mapID].on('zoomend', function (e) {
																$scope.myService.getState().center[mapID].zoom = $scope.map[mapID].getZoom();
															});
															$scope.map[mapID].on('moveend', function (e) {
																$scope.myService.getState().center[mapID].lng = $scope.map[mapID].getCenter().lng;
																$scope.myService.getState().center[mapID].lat = $scope.map[mapID].getCenter().lat;
															});
															resolve("Map created " + mapID + " OK");
														});
													// End of create grid layer	
												},
												function getAttributeTableError(e) {
													$scope.showError("Error fetching table data for mapID: " + mapID + "; " + e);
													reject("Error fetching table data for mapID: " + mapID + "; " + e);
												});

										});										

									}, function (e) { //getGeographyAndLevelForStudy error handler
										$scope.consoleError("[rifc-util-mapping.js] Unable to getGeographyAndLevelForStudy(): " + 
											JSON.stringify(e));
									}

								// End of user.getGeographyAndLevelForStudy()
								).then(function () { 
									$scope.map[mapID].whenReady(function(e) { // BG tiles set

										setMapCentreAndBounds(mapID,
											function setMapCentreAndBoundsCallback(msg) {	// setMapCentreAndBoundsCallback
												$scope.consoleDebug(msg); 
												
												$scope.myService.getState().center[mapID].zoom = $scope.map[mapID].getZoom();
												$scope.myService.getState().center[mapID].lng = $scope.map[mapID].getCenter().lng;
												$scope.myService.getState().center[mapID].lat = $scope.map[mapID].getCenter().lat;
												$scope.consoleDebug("[rifc-util-mapping.js] add topoJsonGridLayer for mapID: " + mapID + 
													"; study: " + $scope.studyID[mapID].study_id);		
												$scope.consoleDebug("[rifc-util-mapping.js] initial setView for mapID: " + mapID + 
														"; centre: " + JSON.stringify($scope.myService.getState().center[mapID]));		
		 
											}, function setMapCentreAndBoundsError(e) {	// setMapCentreAndBoundsError
												$scope.consoleError(e); 
											}
										);
									});
									
								}, function (e) { //getGeographyAndLevelForStudy error handler
										$scope.consoleError("[rifc-util-mapping.js] Error in geographyAndLevelForStudyProcessing(): " + 
											JSON.stringify(e));
								});								
							}
						}			
                    }

                    //Sync or unsync map extents using https://github.com/jieter/Leaflet.Sync ONCE!
                    $scope.mapLocking = function () {
                        if ($scope.$parent.bLockCenters) {
							if (!$scope.map["diseasemap1"].isSynced($scope.map["diseasemap2"])) { // sync interactions on diseasemap1 with diseasemap2.
								$scope.consoleDebug("[rifc-util-mapping.js] mapLocking: sync interactions on diseasemap1 with diseasemap2");
								$scope.map["diseasemap1"].sync($scope.map["diseasemap2"], $scope.parameters.mapLockingOptions);
							}	
							if (!$scope.map["diseasemap2"].isSynced($scope.map["diseasemap1"])) {
								$scope.consoleDebug("[rifc-util-mapping.js] mapLocking: sync interactions on diseasemap2 with diseasemap1");
								$scope.map["diseasemap2"].sync($scope.map["diseasemap1"], $scope.parameters.mapLockingOptions);
							}
                        } 
						else {
							if ($scope.map["diseasemap1"].isSynced($scope.map["diseasemap2"])) {
								$scope.consoleDebug("[rifc-util-mapping.js] mapLocking: unsync diseasemap1 with diseasemap2");
								$scope.map["diseasemap1"].unsync($scope.map["diseasemap2"]);
							}
							if ($scope.map["diseasemap2"].isSynced($scope.map["diseasemap1"])) {
								$scope.consoleDebug("[rifc-util-mapping.js] mapLocking: unnsync diseasemap2 with diseasemap1");
								$scope.map["diseasemap2"].unsync($scope.map["diseasemap1"]);
							}
                        }
                    };
					
					function setMapCentreAndBounds(mapID, setMapCentreAndBoundsCallback, setMapCentreAndBoundsError) {
						var promise=new Promise(function(resolve, reject) {
								user.getGeoLevelSelectValues(user.currentUser, $scope.tileInfo[mapID].geography).then(function (res) {
								var lowestLevel = res.data[0].names[0];
								user.getTileMakerTilesAttributes(user.currentUser, $scope.tileInfo[mapID].geography, lowestLevel).then(function (res) {
									$scope.maxbounds = L.latLngBounds([res.data.bbox[1], res.data.bbox[2]], [res.data.bbox[3], res.data.bbox[0]]);
									if (mapID !== "diseasemap2" || $scope.disableMapLocking) {
										
										//do not get maxbounds for diseasemap2
										if ($scope.myService.getState().center[mapID].lat === 0) {
											$scope.map[mapID].fitBounds($scope.maxbounds);
											$scope.map[mapID].whenReady(function(e) {	
												var centre = $scope.myService.getState().center[mapID];
												resolve("[rifc-util-mapping.js] set fitBounds (1) for mapID: " + mapID + 
																"; disableMapLocking: " + $scope.disableMapLocking +
																"; lowestLevel: " + lowestLevel +
																"; maxbounds: " + JSON.stringify($scope.maxbounds) +
																"; centre: " + JSON.stringify(centre));
											});					
										} 
										else {
											var centre = $scope.myService.getState().center[mapID];
											$scope.map[mapID].setView([centre.lat, centre.lng], centre.zoom);
											$scope.map[mapID].whenReady(function(e) {	
												resolve("[rifc-util-mapping.js] set setView (2) for mapID: " + mapID + 
															"; disableMapLocking: " + $scope.disableMapLocking +
															"; lowestLevel: " + lowestLevel +
															"; maxbounds: " + JSON.stringify($scope.maxbounds) +
															"; centre: " + JSON.stringify(centre));
											});
										}
									} 
									else { // diseasemap2
										var centre = $scope.myService.getState().center[mapID];
										$scope.map[mapID].setView([centre.lat, centre.lng], centre.zoom);
										$scope.map[mapID].whenReady(function(e) {	
											resolve("[rifc-util-mapping.js] set setView (3) for mapID: " + mapID + 
														"; disableMapLocking: " + $scope.disableMapLocking +
														"; lowestLevel: " + lowestLevel +
														"; maxbounds: " + JSON.stringify($scope.maxbounds) +
														"; centre: " + JSON.stringify(centre));
										});
									}
								}, function (e) {
									reject("[rifc-util-mapping.js] Unable to getTileMakerTilesAttributes(): " + 
										JSON.stringify(e));
								});
							}, function (e) {
								reject("[rifc-util-mapping.js] Unable to getGeoLevelSelectValues(): " + 
									JSON.stringify(e));
							});											
						});
						
						promise.then(function(result) {
							setMapCentreAndBoundsCallback(result); 
						}, function(err) {
							setMapCentreAndBoundsError(err); 
						});

					}; // End of setMapCentreAndBounds()

                    function getAttributeTable(mapID, getAttributeTableCallBack, getAttributeTableError) {
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
/*
 * Valid data viewer columns: "area_id", "band_id", "observed", "expected", "population", "adjusted", "inv_id", "posterior_probability",
 *                            "lower95", "upper95", "relative_risk", "smoothed_smr", "smoothed_smr_lower95", "smoothed_smr_upper95"
 */
                                        for (var i in res.data.smoothed_results[0]) {
                                            if (ViewerStateService.getValidColumn(i, $scope.myService.getState().studyType[mapID], mapID)) {
                                                if (i !== "area_id" && // Valid Choropleth map features
												    i !== "band_id" && 
												    i !== "inv_id" && 
												    i !== "adjusted" && 
												    i !== "_selected") {
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
									getAttributeTableCallBack("[rifc-util-mapping.js] " + $scope.tableData[mapID].length + 
										" rows of attribute data fetched for map: " + mapID +
										"; study : " + $scope.studyID[mapID].study_id);		
									$scope.attributeDataLoaded[mapID]=true;
                                }, function (e) {
                                    clearTheMapOnError(mapID);
									getAttributeTableError(e);
                                });
                    }

                    /*
                     * INFO BOXES AND LEGEND
                     */
                    //An empty control on map
                    function closureAddControl(m) {
                        return function () {
                            this._div = L.DomUtil.create('div', 'info');
							this._div.id = "infobox";
                            this.update();
                            return this._div;
                        };
                    }
                    //The hover box update
                    function closureInfoBoxUpdate(m) {
                        return function (poly, name) {
                            if (poly) {
                                this._div.style["display"] = "inline";
                                this._div.innerHTML =
                                        function () {
                                            var feature = ChoroService.getMaps(m).feature;
                                            var tmp;
                                            var inner = '<h5>ID: ' + poly + '</br>Name: ' + name + '</h5>';
                                            if ($scope.attr[m] !== "") {
                                                for (var i = 0; i < $scope.tableData[m].length; i++) {
                                                    if ($scope.tableData[m][i].area_id === poly) {
                                                        tmp = $scope.tableData[m][i][$scope.attr[m]];
														$scope.tableData[m][i].name = name;
                                                        break;
                                                    }
                                                }
                                                if (feature !== "" && !isNaN(Number(tmp))) {
                                                    inner = '<h5>ID: ' + poly + '</br>Name: ' + name + '</br>' + feature.toUpperCase().replace("_", " ") + ": " + Number(tmp).toFixed(3) + '</h5>';	
                                                }
                                            }
                                            return inner;
                                        }();
                            } else {
                                this._div.innerHTML = '';
                                this._div.style["display"] = "none";
                            }
                        };
                    }
					
                    //Area info box update
                    function closureInfoBox2Update(m) {
                        return function (poly) {
                            if (poly == undefined) {
								if ($scope.shapes[m].getLayers().length > 0) {
									this._div.innerHTML = '<h4>Mouse over selection shapes to show properties</br>' +
										'Hide selection shapes to mouse over area names</h4>';
								}
								else {
									this._div.innerHTML = '<h4>Mouse over area names</h4>';
								}
                                this._div.style["display"] = "inline";
                            } else {
                                var results = null;
                                for (var i = 0; i < $scope.tableData[m].length; i++) {
                                    if ($scope.tableData[m][i].area_id === poly) {
                                        results = $scope.tableData[m][i];
                                    }
                                }
                                if (results !== null) {
                                    this._div.style["display"] = "inline";
                                    this._div.innerHTML =
                                            '<h5>ID: ' + poly + '</br>' +
                                            'Name: ' + (results.name||'N/A') + '</br>' +
                                            'Population: ' + results.population + '</br>' +
                                            'Observed: ' + results.observed + '</br>' +
                                            'Expected: ' + Number(results.expected).toFixed(2) + '</br>' + '</h5>';
                                } else {
									if ($scope.shapes[m].getLayers().length > 0) {
										this._div.innerHTML = '<h4>Mouse over selection shapes to show properties</br>' +
											'Hide selection shapes to mouse over area names</h4>';
									}
									else {
										this._div.innerHTML = '<h4>Mouse over area names</h4>';
									}
									this._div.style["display"] = "inline";
                                }
                            }
                        };
                    };

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