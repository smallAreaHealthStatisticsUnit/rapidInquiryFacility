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
 
 * Peter Hambly
 * @author phambly
 */

/*
 * DIRECTIVE for risk analysis area selection using postal codes
 */

/* global L */
angular.module("RIF")
        //Selection by postal code and radiii for risk analysis
        .controller('ModalPostalCodeInstanceCtrl', function ($scope, $uibModalInstance) {
            $scope.close = function () {
                $uibModalInstance.dismiss();
            };
            $scope.submit = function () {
                var bOk = $scope.applyCoordinates();
                if (bOk) {
                    $uibModalInstance.close();
                }
            };
        })
        .directive('postalCode', ['$rootScope', '$uibModal', '$q', 'ParametersService', 'uiGridConstants', 'AlertService', 'SubmissionStateService', 
			'ProjectionService', 'DrawSelectionService', 'CommonMappingStateService', 'user',
			// SelectStateService is not need as makeDrawSelection() in rifd-dsub-maptable.js is called to update
            function ($rootScope, $uibModal, $q, ParametersService, uiGridConstants, AlertService, SubmissionStateService, ProjectionService,
				DrawSelectionService, CommonMappingStateService, user) {
                return {
                    restrict: 'A', //added as attribute to in to selectionMapTools > btn-addPostalCode in rifs-utils-mapTools
                    link: function (scope, element, attr) {

                        var alertScope = scope.$parent.$$childHead.$parent.$parent.$$childHead;
                        var studyType = scope.$parent.input.type; // Disease Mapping or Risk Analysis
						var selectAt = scope.$parent.input.selectAt;
										
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
						scope.initialPostalcodeGridOptions = {
							enableSorting: true,
							enableRowSelection: false,
							enableFiltering: true,
							enableColumnResizing: true,
							enableRowHeaderSelection: false,
							enableHorizontalScrollbar: uiGridConstants.scrollbars.ALWAYS,
							enableVerticalScrollbar: uiGridConstants.scrollbars.ALWAYS,
							minRowsToShow: 5,
							maxVisibleColumnCount: 4,
							rowHeight: 20,
							multiSelect: false, 
							onRegisterApi: function(gridApi) { scope.gridApi = gridApi; }, 
							columnDefs: [],
							data: []
						};
						
						// User selection
						
						function setCapabilities() {
							//  Capability dependent defaults
							if (scope.hasPostalCode) {
								scope.modalHeader = "Select by Postal Code";
								
								scope.wgs84Checked=false; // Options checked
								scope.postalCodeChecked=true;
								scope.nationalGridChecked=false;
								
								scope.selectionMethod = 2; // postal code
								
								scope.isWGS84=false;  // Show hide options
								scope.isNationalGrid=false;
								scope.isPostalCode=true;
							}
							else if (scope.hasNationalGrid) {
								scope.modalHeader = "Select by National Grid X/Y Coordinates";
								
								scope.wgs84Checked=false;
								scope.postalCodeChecked=false;
								scope.nationalGridChecked=true;	
								
								scope.selectionMethod = 3; // National Grid
								
								scope.isWGS84=false;
								scope.isNationalGrid=true;
								scope.isPostalCode=false;
							}
							else { // defaults
								scope.modalHeader = "Select by WGS84 GPS Coordinates";
								
								scope.wgs84Checked=true;
								scope.postalCodeChecked=false;
								scope.nationalGridChecked=false;
								scope.selectionMethod = 1; // WGS84
								
								scope.isWGS84=true;
								scope.isNationalGrid=false;
								scope.isPostalCode=false;
							}
						}
											
						// Capabilities
						scope.hasNationalGrid=false;
						scope.hasPostalCode=false;							
						setCapabilities();
								
                        var thisGeography = SubmissionStateService.getState().geography;
						
                        //user input boxes
                        scope.bandAttr = [];	
						scope.xcoordinate = undefined;
						scope.ycoordinate = undefined;
						scope.coordinatesSet = false;
						scope.properties = undefined;
						scope.hasPostalGrid = false;
						scope.postalCodeGridOptions = {};
						
						function getPostalCodeCapabilities(callback) {
							user.getPostalCodeCapabilities(user.currentUser, thisGeography).then(function (res) { 
								
								AlertService.consoleDebug("[rifd-dsub-postal.js] getPostalCodeCapabilities: " + JSON.stringify(res.data, null, 1));
								
								if (res.data.error) {
									AlertService.rifMessage('error', res.data.error);
								}
								else {
									if (res.data.postalPopulationTable) {
										scope.hasPostalCode=true;
									}
									if (res.data.srid && ProjectionService.canReproject(res.data.srid)) {
										scope.srid=res.data.srid;
										scope.hasNationalGrid=true;
									}
								}
								setCapabilities();
 
							}, function () { // Error handler
								AlertService.rifMessage('warning', "Could not postal code capabilities from the database for geography: " + thisGeography);
								setCapabilities();
							}).then(function() {
								if (callback && typeof callback == "function") {
									callback();
								}
								else {
									AlertService.rifMessage("warning", "getPostalCodeCapabilities() callback is undefined.");
								}
							});
						}
						
						function parseCoordinate(coordinate) {  // Lat, Long
							if (coordinate) {
								var splitCords = coordinate.split(',');
								if (splitCords.length == 2) {
									var nCoordinates = [];
									try {
										nCoordinates[0] = parseFloat(splitCords[0]);
										nCoordinates[1] = parseFloat(splitCords[1]);
										AlertService.consoleDebug("[rifd-dsub-postal.js] parseCoordinate(" + coordinate + 
											") nCoordinates: " + JSON.stringify(nCoordinates));
										return nCoordinates;
									}
									catch (e) {
										AlertService.rifMessage('warning', "Unable to parse coordinate: " + coordinate +
											"; error: " + e.message);
									}
								}
								else {
									AlertService.rifMessage('warning', "Coordinates are not in the correct comma separated form: " + coordinate);
								}
							}
							
							return undefined;
						}
						
						function checkCoordinate(coordinate) {
							if (coordinate) {
								if (coordinate[0] <= -90 && coordinate[0] >= 90) { // y
									AlertService.rifMessage('warning', "Coordinates check failed; latitude (Y) is invalid: " + 
										JSON.stringify(coordinate));
									return false;
								}
								else if (coordinate[0] < -180 && coordinate[0] > 180) { // y
									AlertService.rifMessage('warning', "Coordinates check failed; longitude (X) is invalid: " + 
										JSON.stringify(coordinate));
									return false;
								}
								else {
									return true;
								}									
							}
							else {
								AlertService.rifMessage('warning', "Coordinates check failed: no coordinate provided");
								return false;
							}
						}
						
                        element.on('click', function (event) {
							
                            scope.bandAttr.length = 0;
							scope.xcoordinate = undefined;
							scope.ycoordinate = undefined;
							scope.coordinatesSet = false;
							scope.properties = undefined;
							scope.hasPostalGrid = false;			// Results table
							scope.postalCodeGridOptions = {};
							
							getPostalCodeCapabilities(function callModal() {
								var modalInstance = $uibModal.open({
									animation: true,
									templateUrl: 'dashboards/submission/partials/rifp-dsub-frompostalcode.html',
									windowClass: 'postal-code-modal',
									controller: 'ModalPostalCodeInstanceCtrl',
									backdrop: 'static',
									scope: scope,
									keyboard: false
								});
							});
							
                        });		
						
						scope.postalCodeRadioChange = function (selectionMethod) {
							
//							AlertService.consoleDebug("[rifd-dsub-postal.js] postalCodeRadioChange: " + selectionMethod);
							switch (selectionMethod) {
								case 1: // WGS84
									scope.modalHeader = "Select by " + printSelectionMethod(selectionMethod);
									scope.isWGS84=true;
									scope.isNationalGrid=false;
									scope.isPostalCode=false;
									scope.selectionMethod=selectionMethod;
									break;
								case 2: // Postal Code	
									scope.modalHeader = "Select by " + printSelectionMethod(selectionMethod);
									scope.isWGS84=false;
									scope.isNationalGrid=false;
									scope.isPostalCode=true;
									scope.selectionMethod=selectionMethod;
									break;
								case 3: // National Grid
									scope.modalHeader = "Select by " + printSelectionMethod(selectionMethod);
									scope.isWGS84=false;
									scope.isNationalGrid=true;
									scope.isPostalCode=false;
									scope.selectionMethod=selectionMethod;
									break;
							}
							scope.hasPostalGrid = false;
							scope.postalCodeGridOptions = {};
							if (scope.gridApi) {
								scope.gridApi.core.refresh();
							}
						}
						
						scope.nationalGridChange = function(attr) {
							scope.nationalGridCoordinate=attr;
						}
						scope.wgs84Change = function(attr) {
							scope.wgs84=attr;
						}
						scope.postcodeChange = function(attr) {
							scope.postcode=attr;
						}
						
						scope.checkWGS84 = function() {
							
							if (scope.wgs84) {
								wgs84Coordinate=parseCoordinate(scope.wgs84);
								if (checkCoordinate(wgs84Coordinate)) {
									AlertService.consoleDebug("[rifd-dsub-postal.js] checkWGS84: " + JSON.stringify(wgs84Coordinate));
									scope.xcoordinate = wgs84Coordinate[1];
									scope.ycoordinate = wgs84Coordinate[0];	
									scope.coordinatesSet = true;
									scope.properties = {
										"wgs84Coordinate": 			JSON.stringify(wgs84Coordinate)
									};		
									smoothed_results = [];
									smoothed_results.push({
										"Name": "wgs84Coordinate",
										"Value": JSON.stringify(wgs84Coordinate)
									});
									scope.setupGrid(smoothed_results);		
								}
							}
						}
						scope.checkNationalGrid = function() {
							
							if (scope.nationalGridCoordinate && scope.srid) {
								coordinate=parseCoordinate(scope.nationalGridCoordinate); // in X, Y
//								coordinateReversed=[];
//								coordinateReversed[0]=coordinate[1];
//								coordinateReversed[1]=coordinate[0];
								wgs84Coordinate=ProjectionService.reproject2wgs84(scope.srid, coordinate);
								if (checkCoordinate(wgs84Coordinate)) {
									scope.xcoordinate = wgs84Coordinate[0];
									scope.ycoordinate = wgs84Coordinate[1];		
									scope.coordinatesSet = true;
									scope.properties = {
										"wgs84Coordinate": 			JSON.stringify(wgs84Coordinate),
										"nationalGridCoordinate": 	scope.nationalGridCoordinate
									};		
									smoothed_results = [];
									smoothed_results.push({
										"Name": "wgs84Coordinate",
										"Value": JSON.stringify(wgs84Coordinate)
									});
									smoothed_results.push({
										"Name": "nationalGridCoordinate",
										"Value": scope.nationalGridCoordinate
									});
									scope.setupGrid(smoothed_results);		
								}
							}
						}
						scope.checkPostcode = function() {
							
							if (scope.postcode) {
								user.getPostalCodes(user.currentUser, thisGeography, scope.postcode).then(function (res) {    
									if (res.data.nopostcodefound) {
										AlertService.rifMessage('warning', res.data.nopostcodefound.warning);
//										setPostcode(undefined);
										scope.setupGrid(undefined);	

										scope.xcoordinate = undefined;
										scope.ycoordinate = undefined;	
										scope.coordinatesSet = false;
										scope.properties = undefined;									
									}
									else {
										scope.properties = {"Postal code": res.data.additionalTableJson.postalCode};
										if (res.data.additionalTableJson && res.data.additionalTableJson.postalCode) {
//											setPostcode(res.data.additionalTableJson.postalCode);
										}
										
										if (res.data.additionalTableJson && res.data.additionalTableJson.xcoordinate && 
										    res.data.additionalTableJson.ycoordinate) {
											scope.xcoordinate = res.data.additionalTableJson.xcoordinate;
											scope.ycoordinate = res.data.additionalTableJson.ycoordinate;	
											scope.coordinatesSet = true;									
										}
//										AlertService.consoleDebug("[rifd-dsub-postal.js] postcode change: " + scope.postcode +
//											"; res: " + JSON.stringify(res, null, 1));
										scope.setupGrid(res.data.smoothed_results);			  
									}
								}, function () { // Error handler
									AlertService.rifMessage('warning', "Could not fetch postal codes from the database");

								});
							}
							else {
								AlertService.rifMessage('warning', "You must enter a postal code");
							}
						}
						
//						function setPostcode(newPostcode) {
//							AlertService.consoleDebug("[rifd-dsub-postal.js] set postcode change: " + newPostcode);
//							scope.postcode=newPostcode;
//						}				
						function printSelectionMethod(selectionMethod) {
							var rVal="";
							
							switch (selectionMethod) {
								case 1: // WGS84
									rVal = "WGS84 GPS Coordinates";
									break;
								case 2: // Postal Code	
									rVal = "Postal Code";
									break;
								case 3: // National Grid
									rVal = "National Grid X/Y Coordinates";
									break;
							}
							
							return rVal;
						}
						
						scope.setupGrid = function(smoothed_results) {
							if (smoothed_results) {
								var postalCodeGridOptions=angular.copy(scope.initialPostalcodeGridOptions);
								postalCodeGridOptions.columnDefs = [
									{ field: 'Name', width: 100 },
									{ field: 'Value', width: 250 }
								];
								var data=[];
								for (var i=0; i<smoothed_results.length; i++) {
									if (smoothed_results[i].Value && typeof smoothed_results[i].Value != "object") {
										data.push(smoothed_results[i]);
									}
								}
								postalCodeGridOptions.data=data;
								
								if (postalCodeGridOptions.data.length > 0) {
									
//									AlertService.consoleDebug("[rifd-dsub-postal.js] setupGrid postalCodeGridOptions: " +
//										JSON.stringify(postalCodeGridOptions, null, 1));
									scope.postalCodeGridOptions=postalCodeGridOptions;
									scope.hasPostalGrid = true;
									if (scope.gridApi) {
										scope.gridApi.core.refresh();
									}
								}			
							}	
							else {
								scope.postalCodeGridOptions={};
								scope.hasPostalGrid = false;
								if (scope.gridApi) {
									scope.gridApi.core.refresh();
								}
							}
						} 
						
                        scope.applyCoordinates = function () {
							if (scope.coordinatesSet) {
								AlertService.consoleDebug("[rifd-dsub-postal.js] apply coordinates: [" + 
									scope.xcoordinate + ", " + scope.ycoordinate + "]");	
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
                                        AlertService.rifMessage('warning', "Non-numeric band value entered");
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
								if (scope.bandAttr.length == 0) {
									AlertService.rifMessage('warning', "No distance band values supplied for points");
									return false;
								}
								else {
									//check ascending and sequential for radii
									for (var i = 0; i < scope.bandAttr.length - 1; i++) {
										var a = parseInt(scope.bandAttr[i]);
										var b = parseInt(scope.bandAttr[i+1]);
										
										if (a.toString() != scope.bandAttr[i] || 
											b.toString() != scope.bandAttr[i+1]) {
											AlertService.consoleLog("[rifd-dsub-postal.js] scope.bandAttr[" + i + "]: " + 
												JSON.stringify(scope.bandAttr));
											AlertService.rifMessage('warning', "Distance band values are not integers");
											return false;
										}
										else if (a > b) {
											AlertService.consoleLog("[rifd-dsub-postal.js] scope.bandAttr[" + i + "]: " + 
												JSON.stringify(scope.bandAttr));
											AlertService.rifMessage('warning', "Distance band values are not in ascending order");
											return false;
										}
									}
								
									//make polygons and apply selection
									var i = 0;
									var rifShapeId= CommonMappingStateService.getState("areamap").getNextShapeId();
									for (; i < scope.bandAttr.length; i++) {
										var rifShapePolyId= CommonMappingStateService.getState("areamap").getNextShapePolyId();
										var circle = L.circle([scope.ycoordinate, scope.xcoordinate],
												{
													radius: scope.bandAttr[i],
													fillColor: 'none',
													weight: (selectorBands.weight || 3),
													opacity: (selectorBands.opacity || 0.8),
													fillOpacity: (selectorBands.fillOpacity || 0),
													color: selectorBands.bandColours[i] // Band i+1
												});
										var shape={
											data: circle,
											properties: scope.properties,
											circle: true,
											freehand: false,
											band: i + 1,
											area: Math.round((Math.PI*Math.pow(scope.bandAttr[i], 2)*100)/1000000)/100, // Square km to 2dp
											rifShapeId: rifShapeId,
											rifShapePolyId: rifShapePolyId,
										};											
										shape.properties.area=shape.area;
										shape.properties.rifShapeId=rifShapeId;
										shape.properties.rifShapePolyId=rifShapePolyId;
										
										$rootScope.$broadcast('makeDrawSelection', shape);
									}
									
									$rootScope.$broadcast('completedDrawSelection', { maxBand: i});
								}
							}
							else {
								AlertService.rifMessage('warning', 'You must enter a valid ' + printSelectionMethod(scope.selectionMethod));
								return false;
							}
                            return true;
                        };
                    }
                };
            }]);