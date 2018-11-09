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
 * DIRECTIVE for risk analysis area selection using shapefiles
 */

/* global L */
angular.module("RIF")
        //Open a shapefile for risk analysis
        .controller('ModalAOIShapefileInstanceCtrl', function ($scope, $uibModalInstance) {
            $scope.close = function () {
                $uibModalInstance.dismiss();
            };
            $scope.submit = function () {
				
				$scope.disableSubmit=true;
                var bOk = $scope.displayShapeFile();
				$scope.disableSubmit=false;
                if (bOk) {
                    $uibModalInstance.close();
                }
            };
        })
        .directive('riskAnalysis', ['$rootScope', '$uibModal', '$q', 'ParametersService', 'uiGridConstants', 'SelectStateService', 
				'AlertService', 'CommonMappingStateService', 'DrawSelectionService', 'GISService', '$window', '$interval',
			// SelectStateService is not need as makeDrawSelection() in rifd-dsub-maptable.js is called to update
            function ($rootScope, $uibModal, $q, ParametersService, uiGridConstants, SelectStateService, 
				AlertService, CommonMappingStateService, DrawSelectionService, GISService, $window, $interval) {
                return {
                    restrict: 'A', //added as attribute to in to selectionMapTools > btn-addAOI in rifs-utils-mapTools
                    link: function (scope, element, attr) {
                        var alertScope = scope.$parent.$$childHead.$parent.$parent.$$childHead;
                        var studyType = scope.$parent.input.type; // Disease Mapping or Risk Analysis
						
                        var poly; //polygon shapefile
                        var buffers; //concentric buffers around points					
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
						var initialShapefileGridOptions = {
							enableFiltering: true,
							enableRowSelection: true,
							enableColumnResizing: true,
							enableRowHeaderSelection: false,
							enableHorizontalScrollbar: uiGridConstants.scrollbars.WHEN_NEEDED,
							enableVerticalScrollbar: uiGridConstants.scrollbars.WHEN_NEEDED,
							selectionRowHeaderWidth: 35,
							multiSelect: true,			
							minRowsToShow: 5,
							maxVisibleColumnCount: 8,
							rowHeight: 20,
							columnDefs: [],
							data: []
						};
						
						var shapefileGridOptions = angular.copy(initialShapefileGridOptions);
						shapefileGridOptions.onRegisterApi = shapefileGridLoaded;
						
						function shapefileGridLoaded(gridApi) { 
								scope.gridApi = gridApi; 									
						};	
						
						// Also defined in rifs-util-leafletdraw.js
                        var factory = L.icon({
                            iconUrl: 'images/factory.png',
                            iconAnchor: [16, 16]
                        });
                        //user input boxes
                        scope.bandAttr = [];
                        element.on('click', function (event) {
                            scope.modalHeader = "Select with a shapefile";
                            scope.accept = ".zip";
                            var modalInstance = $uibModal.open({
                                animation: true,
                                templateUrl: 'dashboards/submission/partials/rifp-dsub-fromshp.html',
                                windowClass: 'shapefile-modal',
                                controller: 'ModalAOIShapefileInstanceCtrl',
                                backdrop: 'static',
                                scope: scope,
                                keyboard: false
                            });
                            //ng-show which options to display
                            //depending on the type of shapefile uploaded
                            scope.selectionMethod = 1;
                            scope.bProgress = false;
                            scope.isPolygon = false;
                            scope.isPoint = false;
                            scope.isTable = false;
							scope.hasBandAttribute = false;
							scope.hasExposureAttributes = false;
                            scope.bandAttr.length = 0;
							scope.hasGrid = false;
							scope.shapefileGridOptions = {};
							scope.disableSubmit=false;
							scope.possibleBands = CommonMappingStateService.getState('areamap').possibleBands;
							
                            //remove any existing AOI layer
                            poly = null;
                            buffers = null;
//                            if (scope.areamap.hasLayer(scope.shpfile)) {
//                                scope.areamap.removeLayer(scope.shpfile);
//                                scope.shpfile = new L.layerGroup();
//                            }
                        });
                        scope.radioChange = function (selectionMethod) {
                            scope.selectionMethod = selectionMethod;
                            if (selectionMethod === 3) { // make selection by attribute value in file
                                scope.isTable = true;
                            } else {
                                scope.isTable = false;
                            }
                        };
						
						scope.changedValue = function(attr) {
							scope.selectedAttr=attr;
						}
						
						function getSelectionMethodAsString(attributeName) {
							if (scope.selectionMethod === 1) { // Single boundary; already set
								return "selection by single boundary";
							}
							else if (scope.selectionMethod === 2) { // make selection by band attribute in file
								return "selection by band attribute";
							}
							else if (scope.selectionMethod === 3) { // make selection by attribute value in file
								return "selection by attribute value: " + (attributeName||"(unknown attribute)");
							}
							else {
								return "unknown selection method " + scope.selectionMethod;
							}
						};
						
                        function readShpFile(file) {
							
                            try {
                                if (file.name.slice(-3) !== 'zip') {
                                    //not a zip file
                                    alertScope.showError("File: " + file.name + ": all parts of the shapefile expected in one zipped file");
                                    return;
                                } else {

                                    //type of study
                                    if (scope.possibleBands.length === 1) {
                                        scope.isRiskMapping = false;
                                    } else {
                                        scope.isRiskMapping = true;
                                    }

                                    var reader = new FileReader();
                                    var deferred = $q.defer();
                                    //http://jsfiddle.net/ashalota/ov0p4ajh/10/
                                    //http://leaflet.calvinmetcalf.com/#3/31.88/10.63
									
									scope.shapeFile = {
										fileName: file.name,
										fileSize: file.size,
										featureCount: 0,
										points: 0,
										polygons: 0,
										propertiesList: [],
										hasBandAttribute: false
									};
									
                                    reader.onload = function () {
                                        var bAttr = false;
                                        scope.attrs = [];
                                        poly = new L.Shapefile(this.result, {
                                            style: function (feature) {
												
												scope.shapeFile.featureCount++;
												if (feature.properties) {
													scope.shapeFile.propertiesList.push(feature.properties);
												}
												
                                                if (feature.geometry.type === "Point") {
													scope.shapeFile.points++;
                                                    scope.isPolygon = false;
                                                    scope.isPoint = true;
                                                    scope.isTable = true;
                                                } else if (feature.geometry.type === "Polygon") {
													scope.shapeFile.polygons++;
                                                    if (!bAttr) {

														var exposureAttributesCount = 0;
                                                        for (var property in feature.properties) {
                                                            scope.attrs.push(property);
															if (property == "band") {
																scope.hasBandAttribute = true;
																scope.shapeFile.hasBandAttribute = true;
															}
															else {
																exposureAttributesCount++;
															}
                                                        }
														if (exposureAttributesCount > 0) {
															scope.hasExposureAttributes = true;	
														}
							
                                                        bAttr = true;
                                                        scope.selectedAttr = scope.attrs[scope.attrs.length - 1];
															// Set default
                                                    }
                                                    scope.isPolygon = true;
                                                    scope.isPoint = false;
                                                    scope.isTable = false;
                                                    return {
                                                        fillColor: 'none',
                                                        weight: 2,
                                                        color: 'blue'
                                                    };
                                                }
																
                                            },
                                            onEachFeature: function (feature, layer) {
                                                //add markers with pop-ups if points
												
                                                if (feature.geometry.type === "Point") {
                                                    layer.setIcon(factory);
                                                    var popupContent = "";
													
                                                    for (var property in feature.properties) {
                                                        popupContent = popupContent + property.toUpperCase() +
                                                                ":\t" + feature.properties[property] + "</br>";
                                                    }
                                                    layer.bindPopup(popupContent);
                                                }
                                            }
                                        });
										
                                        deferred.resolve(poly);
                                    };
									
                                    reader.onloadend = function () {
										SelectStateService.getState().studySelection.fileList.push(scope.shapeFile);
										AlertService.consoleDebug("[rifd-dsub-risk.js] Read shapeFile " + file.name + "; " + 
											SelectStateService.getState().studySelection.fileList.length + " files");
									};
									
                                    reader.readAsArrayBuffer(file);
												
                                    return deferred.promise;
                                }
                            } catch (err) {
                                alertScope.showError("Could not open Shapefile: " + file.name + "; error: " + err.message);
                                scope.bProgress = false;
                            }
                        }

                        scope.screenShapeFile = function () {
                            scope.bProgress = true;
                            var files = document.getElementById('setUpFile').files;
                            if (files.length === 0) {
                                return;
                            }
                            var file = files[0];
                            //clear existing layers
//                            if (scope.shpfile.hasLayer(buffers)) {
//                               scope.shpfile.removeLayer(buffers);
//                            }
//                            if (scope.shpfile.hasLayer(poly)) {
//                                scope.shpfile.removeLayer(poly);
//                            }
                            poly = null;
                            buffers = null;
                            //async for progress bar
                            readShpFile(file).then(function () {
                                //switch off progress bar
                                scope.bProgress = false;
								shapefileGridOptions = angular.copy(initialShapefileGridOptions);
								shapefileGridOptions.onRegisterApi = shapefileGridLoaded;
						
                                if (!scope.isPolygon & !scope.isPoint) {
                                    alertScope.showError("File: " + file.name + ": is not a valid point or polygon zipped shapefile");
                                }
								
								var columnDefs = {};
								for (var layer in poly._layers) {		
									shapefileGridOptions.data.push(poly._layers[layer].feature.properties);
									for (var property in poly._layers[layer].feature.properties) {
										if (columnDefs[property] == undefined) {
											columnDefs[property] = { 
												counter: 0,
												totalLength: 0,
												averageLength: 0,
												width: 0
											};
										}
										columnDefs[property].counter++;
										columnDefs[property].totalLength+=(' ' + poly._layers[layer].feature.properties[property]).length;
									}
								}
								
								for (var column in columnDefs) {
									columnDefs[column].averageLength=columnDefs[column].totalLength/columnDefs[column].counter;
									columnDefs[column].width=80;
									if (columnDefs[column].averageLength < 3 && column.lnegth < 4) {				
										columnDefs[column].width=60;
									}
									else if (columnDefs[column].averageLength > 7 && column.lnegth > 8) {				
										columnDefs[column].width=100;
									}

									if (column == 'band') {
										shapefileGridOptions.columnDefs.push({
											name: column,
											defaultSort: {
												direction: uiGridConstants.ASC,
												priority: 0
										   }, 
										   averageLength: columnDefs[column].averageLength,
										   width: columnDefs[column].width
										});
									}
									else {
										shapefileGridOptions.columnDefs.push({
											name: column, 
										    averageLength: columnDefs[column].averageLength,
											width: columnDefs[column].width
										});

									}
								}
								
								if (shapefileGridOptions.data.length > 0) {
//									alertScope.consoleDebug("[rifd-dsub-risk.js] scope.shapefileGridOptions: " +
//										JSON.stringify(shapefileGridOptions, null, 1));
									scope.shapefileGridOptions = shapefileGridOptions;
									scope.hasGrid = true;
									if (scope.gridApi) {
							
										$interval(function () {
											if (shapefileGrid[0]) {
												scope.gridApi.core.handleWindowResize(); // It got bigger once the shapefile was loaded						
												scope.gridApi.core.refresh();	
											}											
											}, 2000 /* mS */, 1).then(function () {
												var shapefileGrid = angular.element( document.querySelector( '#shapefileGrid' ) );
												if (shapefileGrid[0]) {
													var shapefileGridHeight = shapefileGrid[0].offsetheight;
													var windowHeight =$window.innerHeight; 
													if (shapefileGridHeight == undefined && windowHeight) {
														shapefileGrid[0].offsetheight=Math.round(windowHeight/2);
														shapefileGridHeight = shapefileGrid[0].offsetheight;
													}
													AlertService.consoleDebug("[rifd-dsub-risk.js]: readShpFile done height #shapefileGrid: " + shapefileGridHeight +
														"; window: " + $window.innerHeight);
												}	
												else {
													AlertService.consoleDebug("[rifd-dsub-risk.js]: readShpFile no #shapefileGrid element");
												}	
										});
									}
								}
                            });
                        };
                        scope.displayShapeFile = function () {
                            //exit if there is no shapefile
                            if (!scope.isPolygon && !scope.isPoint) {
                                alertScope.showError("File: " + scope.shapeFile.fileName + ": is not a shapefile");
                                return false;
                            }
							var rifShapeFileId=CommonMappingStateService.getState("areamap").getNextShapeFileId(scope.$parent.input.name, scope.shapeFile);
							
							if (scope.bandAttr.length > 0) {
								SelectStateService.getState().studySelection.bandAttr=angular.copy(scope.bandAttr);
							}
							
                            //check user input on bands
                            if (scope.selectionMethod === 3 || scope.isPoint) {
                                //trim any trailing zeros
                                //check numeric
                                var bZero = [];
                                for (var i = 0; i < scope.bandAttr.length; i++) {
                                    var thisBreak = Number(scope.bandAttr[i]);
                                    if (!isNaN(thisBreak)) {
                                        if (thisBreak !== 0) {
                                            bZero.push(1);
                                        } else {
                                            bZero.push(0);
                                        }
                                    } else {
                                        alertScope.showError("File: " + scope.shapeFile.fileName + ': has a non-numeric band value entered: "' + 
											thisBreak + '"');
                                        return false; //and only display the points
                                    }
                                }
                                var total = 0;
                                for (var i in bZero) {
                                    total += bZero[i];
                                }
                                if (total !== scope.bandAttr.length) {
                                    var tmp = angular.copy(scope.bandAttr);
                                    //there are zero values, are they at the end?
                                    for (var i = scope.bandAttr.length - 1; i >= 0; i--) {
                                        if (scope.bandAttr[i] === '') {
                                            tmp.pop();
                                        }
                                        else {
                                            break;
                                        }
                                    }
                                    scope.bandAttr = angular.copy(tmp);
                                }
                                if (scope.isPoint) {
									if (scope.bandAttr.length == 0) {
										alertScope.showError("File: " + scope.shapeFile.fileName + ": has no distance band values supplied for points");
										return false;
									}
									else {
										//check ascending and sequential for radii
										for (var i = 0; i < scope.bandAttr.length - 1; i++) {
											var a = parseInt(scope.bandAttr[i]);
											var b = parseInt(scope.bandAttr[i+1]);
											
											if (a.toString() != scope.bandAttr[i] || 
												b.toString() != scope.bandAttr[i+1]) {
												alertScope.consoleLog("[rifd-dsub-risk.js] scope.bandAttr[" + i + "]: " + 
													JSON.stringify(scope.bandAttr));
												alertScope.showError("File: " + scope.shapeFile.fileName + ": has distance band values that are not integers");
												return false;
											}
											else if (a > b) {
												alertScope.consoleLog("[rifd-dsub-risk.js] scope.bandAttr[" + i + "]: " + 
													JSON.stringify(scope.bandAttr));
												alertScope.showError("File: " + scope.shapeFile.fileName +  ": has distance band values that are not in ascending order");
												return false;
											}
										}
									}
                                } else {
									if (scope.bandAttr.length == 0) {
										alertScope.showError("File: " + scope.shapeFile.fileName + ": has no exposure attribute values supplied for shapefile");
										return false;
									}
									else {
										//check descending and sequential for exposures
										for (var i = 0; i < scope.bandAttr.length - 1; i++) {
											var a = parseInt(scope.bandAttr[i]);
											var b = parseInt(scope.bandAttr[i+1]);
											
											if (a.toString() != scope.bandAttr[i] || 
												b.toString() != scope.bandAttr[i+1]) {
												alertScope.consoleLog("[rifd-dsub-risk.js] scope.bandAttr[" + i + "]: " + 
													JSON.stringify(scope.bandAttr));
												alertScope.showError("File: " + scope.shapeFile.fileName + ": has distance band values are not integers");
												return false;
											}
											else if (a < b) {
												alertScope.consoleLog("[rifd-dsub-risk.js] scope.bandAttr[" + i + "]: " + 
													JSON.stringify(scope.bandAttr));
												alertScope.showError("File: " + scope.shapeFile.fileName + ": has exposure band values are not in descending order");
												return false;
											}
										}
									}
                                }
                            }

                            //make bands around points
                            if (scope.isPoint) { 
                                //make polygons and apply selection
                                buffers = new L.layerGroup();
								
								var points = 0;
                                for (var j in poly._layers) {
									var rifShapeId= CommonMappingStateService.getState("areamap").getNextShapeId();
									for (i=0; i < scope.bandAttr.length; i++) {
										var rifShapePolyId= CommonMappingStateService.getState("areamap").getNextShapePolyId();
                                        //Shp Library inverts lat, lngs for some reason (Bug?) - switch back
                                        var circle = L.circle(
											[poly._layers[j].feature.geometry.coordinates[1],
                                             poly._layers[j].feature.geometry.coordinates[0]],
                                                {
                                                    radius: scope.bandAttr[i],
                                                    fillColor: 'none',
                                                    weight: (selectorBands.weight || 3),
													opacity: (selectorBands.opacity || 0.8),
													fillOpacity: (selectorBands.fillOpacity || 0),
                                                    color: selectorBands.bandColours[i] // Band i+1
                                                });
                                        buffers.addLayer(circle);
										var properties = poly._layers[j].feature.properties;
										if (properties['$$hashKey']) {
											properties['$$hashKey']=undefined;
										}
										if (!scope.bProgress) {
											scope.bProgress = true;
										}
										var shape={
                                            data: circle,
											properties: properties,
                                            circle: true,
                                            freehand: false,
                                            band: i + 1,
											area: Math.round((Math.PI*Math.pow(scope.bandAttr[i], 2)*100)/1000000)/100, // Square km to 2dp
											rifShapeId: rifShapeId,
											rifShapePolyId: rifShapePolyId,
											rifShapeFileId: rifShapeFileId
                                        };
										
										shape.properties.area=shape.area;
										shape.properties.rifShapeFileId=rifShapeFileId;
										shape.properties.rifShapeId=rifShapeId;
										shape.properties.rifShapePolyId=rifShapePolyId;
									
                                        $rootScope.$broadcast('makeDrawSelection', shape);
										points++;
                                    }
                                }
								
								alertScope.showSuccess("Adding " + i + " band(s) from shape file: " + scope.shapeFile.fileName + ": " + 
									points + " points using " +
									getSelectionMethodAsString());
								scope.bProgress = false;
                                $rootScope.$broadcast('completedDrawSelection', { maxBand: scope.bandAttr.length });
                            } 
							else if (scope.isPolygon) {
                                if (scope.selectionMethod === 2) { // make selection by band attribute in file
                                    for (var i in poly._layers) {
                                        //check these are valid bands
										if (poly._layers[i].feature.properties.band == undefined) {                                            //band number not recognized
                                            alertScope.showError("File: " + scope.shapeFile.fileName + ": invalid band descriptor: (no band field)");
                                            return false;
										}
                                        if (scope.possibleBands.indexOf(poly._layers[i].feature.properties.band) === -1) {
                                            //band number not recognized
                                            alertScope.showError("File: " + scope.shapeFile.fileName +  ": invalid band descriptor: " + poly._layers[i].feature.properties.band);
                                            return false;
                                        }
                                    }
                                } 
								else if (scope.selectionMethod === 3) {
                                    //check the attribute is numeric etc
									scope.shapeFile.selectedAttr = scope.selectedAttr;
                                    for (var i in poly._layers) {
                                        //check these are valid exposure values
                                        if (!angular.isNumber(poly._layers[i].feature.properties[scope.selectedAttr])) {
											var a=parseFloat(poly._layers[i].feature.properties[scope.selectedAttr]);
											if (!isNaN(a)) {
												poly._layers[i].feature.properties[scope.selectedAttr]=a;
											}
											else {
												//number not recognized 
												alertScope.showError("File: " + scope.shapeFile.fileName + ": non-numeric value in file: " + 
													poly._layers[i].feature.properties[scope.selectedAttr]);
												return false;
											}
                                        }
                                    }
                                }
                                //make the selection for each polygon
								
								var maxBand=0;
								var attributeName=undefined;
								var bandValues={};
								var shapeList = [];
								var rifShapeId= CommonMappingStateService.getState("areamap").getNextShapeId();
                                for (var i in poly._layers) {
									
									var rifShapePolyId= CommonMappingStateService.getState("areamap").getNextShapePolyId();
                                    var polygon = L.polygon(poly._layers[i].feature.geometry.coordinates[0], {});
									var properties = poly._layers[i].feature.properties;					
									if (properties['$$hashKey']) {
										properties['$$hashKey']=undefined;
									}
                                    var shape = {
										isShapefile: true,
                                        data: angular.copy(polygon),
										circle: false,
										freehand: false,
										shapefile: true,
										properties: properties,
										area: undefined,
										index: i,
										selectionMethod: scope.selectionMethod
                                    };
									
									shape.centroid=GISService.getCentroid(shape);
									shape.polygon=GISService.getPolygon(shape);
									shape.bbox=GISService.getBoundingBox(shape);
									shape.rifShapeFileId = rifShapeFileId;
									shape.rifShapePolyId = rifShapePolyId;
									shape.rifShapeId = rifShapeId;
//									shape.area = turf.area(polygon.toGeoJSON());  // Square m
									shape.area = Math.round((turf.area(polygon.toGeoJSON())*100)/1000000)/100; // Square km to 2dp
									
									shape.properties.area=shape.area;
									shape.properties.rifShapeFileId=rifShapeFileId;
									shape.properties.rifShapeId=rifShapeId;
									shape.properties.rifShapePolyId=rifShapePolyId;
									shapeList.push(shape);
								}
								shapeList.sort(function(a, b){return a.area - b.area}); 
									// Sort into ascending order by area
								var biggestShape=shapeList[shapeList.length-1];
								var notEnclosedWithinBiggestShape=0;
								biggestShape.properties.biggestShape=true;
                                for (var i = 0; i< shapeList.length; i++) {
									var shape = shapeList[i];
									var point = GISService.geojsonPointToLatLng(centroid);
									if (turf.booleanWithin(centroid, biggestShape.polygon)) {
//									if (GISService.getPointinpolygon(point, biggestShape)) {
										if (i != (shapeList.length-1)) {
											shape.properties.enclosedWithinBiggestShape=true;
										}
									}
									else {
										alertScope.consoleDebug("[rifd-dsub-risk.js] shape: " + i + "/" + shapeList.length + ": " + rifShapePolyId +
											" is not enclosed" +
											"; point: " + JSON.stringify(point) +
											"; centroid: " + JSON.stringify(centroid));
										notEnclosedWithinBiggestShape++;
									}
                                    if (scope.selectionMethod === 1) { // Single boundary; already set
                                        shape.band = 1;
										maxBand=1;
										
										if (bandValues[i]) {
											bandValues[i].band = shape.band;
										}
										else {
											bandValues[i] = {
												band: shape.band,
												value: undefined
											};
										}
                                    } 
									else if (scope.selectionMethod === 2) { // make selection by band attribute in file
                                        shape.band = poly._layers[shapeList[i].index].feature.properties.band;
										attributeName="band";
										
										if (bandValues[i]) {
											bandValues[i].band = shape.band;
										}
										else {
											bandValues[i]={
												band: shape.band,
												value: undefined
											};
										}
                                    } 
									else if (scope.selectionMethod === 3) { // make selection by attribute value in file
                                        var attr = poly._layers[shapeList[i].index].feature.properties[scope.selectedAttr];
										attributeName=scope.selectedAttr;
                                        shape.band = -1;
                                        for (var k = 0; k < scope.bandAttr.length; k++) { // In descending order
                                            if (shape.band == -1 && attr >= scope.bandAttr[k]) {
                                                shape.band = k  + 1;
												shape.exposureValue = attr;
												shape.riskAnalysisExposureField = attributeName;
												alertScope.consoleDebug("[rifd-dsub-risk.js] selection by attribute value shape.band[" + i + "]: " + shape.band +
													"; attr value: " + attr +
													"; k: " + k + 
													"; exposureValue: " + shape.exposureValue + 
													"; riskAnalysisExposureField: " + attributeName + 
													">= scope.bandAttr[k] " + scope.bandAttr[k]);
                                            } 
											else {
												alertScope.consoleDebug("[rifd-dsub-risk.js] No selection by attribute value shape.band[" + i + "]: " + shape.band +
													"; attr value: " + attr + 
													"; k: " + k + 
													"; NOT >= scope.bandAttr[k] " + scope.bandAttr[k]);
											}
											
											if (bandValues[i]) {
												bandValues[i].band = shape.band;
												bandValues[i].value = attr;
											}
											else {
												bandValues[i]={
													band: shape.band,
													value: attr
												};
											}
											
                                        }
										
                                    }
									
									if (shape.band > maxBand) {
										maxBand = shape.band;
									}
									
									if (!scope.bProgress) {
										scope.bProgress = true;
									}
                                    //make the selection
									$rootScope.$broadcast('makeDrawSelection', shape);
                                } // End of shapefile polygon processing for loop
								
								if (notEnclosedWithinBiggestShape == 0) {
									alertScope.consoleDebug("[rifd-dsub-risk.js] File: " + scope.shapeFile.fileName + " has " + 
										notEnclosedWithinBiggestShape + "/"+ shapeList.length + " enclosed polygons");
								}
								else {
									alertScope.showError("File: " + scope.shapeFile.fileName + " has " + notEnclosedWithinBiggestShape + "/" + 
										shapeList.length + " polygons that are not enclosed within the biggest shape: " + shape.rifShapeFileId);
									scope.bProgress = false;
									return true; /* Close window */			
								}
								
								var bandsUsed={};
								var noBandsUsed=0;
								for (var k in bandValues) {
									if (bandValues[k].band > 0) {
										if (bandsUsed[bandValues[k].band]) {
											bandsUsed[bandValues[k].band]++;
										}
										else {
											bandsUsed[bandValues[k].band]=1;
										}
									}
								}								
								alertScope.consoleDebug("[rifd-dsub-risk.js] " + getSelectionMethodAsString(attributeName) +	
									"; maxBand: " + maxBand +
									(scope.selectedAttr ? ("; riskAnalysisExposureField: " + scope.selectedAttr) : "") +
									"; scope.attrs: " + (scope.attrs||"(no attributes in shapefile)") +
									"; scope.bandAttr (user supplied band values): " + JSON.stringify(scope.bandAttr) +
									"; bandsUsed: " + JSON.stringify(bandsUsed, null , 1) +
									"; bandValues: " + JSON.stringify(bandValues, null , 1));
								if (maxBand > 0) {
									alertScope.showSuccess("File: " + scope.shapeFile.fileName + ": adding " + 
										Object.keys(bandsUsed).length + "/" + maxBand + " band(s) from " + 
										Object.keys(poly._layers).length + " polygons using " +
										getSelectionMethodAsString(attributeName));
								}
								else {
									alertScope.showError("File: " + scope.shapeFile.fileName +  ": added no bands from " + 
										Object.keys(poly._layers).length + " polygons using " +
										getSelectionMethodAsString(attributeName));
									scope.bProgress = false;
									return false;			
								}
														
//								try {
//									scope.shpfile.addLayer(poly); // Add poly to layerGroup
//								} catch (err) {
//									alertScope.showError("File: " + scope.shapeFile.fileName + ": could not open Shapefile, no valid features");
//									return false;
//								} 
								
								scope.bProgress = false;
                                $rootScope.$broadcast('completedDrawSelection', {maxBand: maxBand});
                            } // End of isPolygon()

                            return true;
                        };
                    }
                };
            }]);