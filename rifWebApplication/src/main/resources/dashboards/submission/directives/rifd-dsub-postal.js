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
                var bOk = $scope.applyPostalCode();
                if (bOk) {
                    $uibModalInstance.close();
                }
            };
        })
        .directive('postalCode', ['$rootScope', '$uibModal', '$q', 'ParametersService', 'uiGridConstants', 'AlertService', 'SubmissionStateService', 
			'user',
			// SelectStateService is not need as makeDrawSelection() in rifd-dsub-maptable.js is called to update
            function ($rootScope, $uibModal, $q, ParametersService, uiGridConstants, AlertService, SubmissionStateService,
				user) {
                return {
                    restrict: 'A', //added as attribute to in to selectionMapTools > btn-addPostalCode in rifs-utils-mapTools
                    link: function (scope, element, attr) {

                        var alertScope = scope.$parent.$$childHead.$parent.$parent.$$childHead;
                        var studyType = scope.$parent.input.type; // Disease Mapping or Risk Analysis
						var selectAt = scope.$parent.input.selectAt;
						
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
						var initialPostalcodeGridOptions = {
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
						
						var postalcodeGridOptions = angular.copy(initialPostalcodeGridOptions);
						
						// Capabilities
						scope.hasNationalGrid=true;
						scope.hasPostalCode=true;
						
						// User selection
						
						//  Capability dependent defaults
						if (scope.hasPostalCode) {
                            scope.modalHeader = "Select by Postal Code";
							
							scope.wgs84Checked=false;
							scope.postalCodeChecked=true;
							scope.nationalGridChecked=false;
							
							scope.isWGS84=false;
							scope.isNationalGrid=false;
							scope.isPostalCode=true;
						}
						else if (scope.hasNationalGrid) {
                            scope.modalHeader = "Select by National Grid X/Y Coordinates";
							scope.wgs84Checked=false;
							scope.postalCodeChecked=false;
							scope.nationalGridChecked=true;	
							
							scope.isWGS84=false;
							scope.isNationalGrid=true;
							scope.isPostalCode=false;
						}
						else { // defaults
                            scope.modalHeader = "Select by WGS84 GPS Coordinates";
							
							scope.wgs84Checked=true;
							scope.postalCodeChecked=false;
							scope.nationalGridChecked=false;
							
							scope.isWGS84=true;
							scope.isNationalGrid=false;
							scope.isPostalCode=false;
						}
						
                        var thisGeography = SubmissionStateService.getState().geography;
						
						// Also defined in rifs-util-leafletdraw.js
                        var factory = L.icon({
                            iconUrl: 'images/factory.png',
                            iconAnchor: [16, 16]
                        });
                        //user input boxes
                        scope.bandAttr = [];	
						scope.hasGrid = false;
						
                        element.on('click', function (event) {
							
							scope.radioChange = function (selectionMethod) {
								switch (selectionMethod) {
									case 1: // WGS84
										scope.modalHeader = "Select by WGS84 GPS Coordinates";
										scope.isWGS84=true;
										scope.isNationalGrid=false;
										scope.isPostalCode=false;
										break;
									case 2: // Postal Code	
										scope.modalHeader = "Select by National Grid X/Y Coordinates";
										scope.isWGS84=false;
										scope.isNationalGrid=false;
										scope.isPostalCode=true;
										break;
									case 3: // National Grid
										scope.modalHeader = "Select by National Grid X/Y Coordinates";
										scope.isWGS84=false;
										scope.isNationalGrid=true;
										scope.isPostalCode=false;
										break;
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
							}
							scope.checkNationalGrid = function() {
							}
							scope.setPostcode = function(newPostcode) {
								scope.postcode=newPostcode;
							}
							scope.setupGrid = function(newPostcode) {
								postalcodeGridOptions = angular.copy(initialPostalcodeGridOptions);
/*								
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
									alertScope.consoleDebug("[rifd-dsub-risk.js] scope.shapefileGridOptions: " +
										JSON.stringify(shapefileGridOptions, null, 1));
									scope.shapefileGridOptions = shapefileGridOptions;
									scope.hasGrid = true;
									if (scope.gridApi) {
										scope.gridApi.core.refresh();
									}
								}			*/					
							}
							scope.checkPostcode = function() {
								
								user.getPostalCodes(user.currentUser, thisGeography, scope.postcode).then(function (res) {    
									if (res.data.nopostcodefound) {
										scope.postcode=res.data.nopostcodefound.postalCode || scope.postcode;
										AlertService.rifMessage('warning', res.data.nopostcodefound.warning);
										scope.setPostcode(undefined);
										scope.setupGrid(undefined);		
									}
									else {
										if (res.data.additionalTableJson && res.data.additionalTableJson.postalCode) {
											scope.setPostcode(res.data.additionalTableJson.postalCode);
										}
										AlertService.consoleDebug("[rifd-dsub-postal.js] postcode change: " + scope.postcode +
											"; res: " + JSON.stringify(res, null, 1));
										scope.setupGrid(res.data.smoothed_results);			  
									}
								}, function () { // Error handler
									AlertService.rifMessage('warning', "Could not fetch postal codes from the database");

								}).then(function () {
								});
							}
							
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
						
                        scope.applyPostalCode = function () {
							if (scope.postcode) {
								AlertService.consoleDebug("[rifd-dsub-postal.js] apply postcode: " + scope.postcode);
							}
							else {
								AlertService.rifMessage('warning', 'You must enter a valid postcode');
								return false;
							}
                            return true;
                        };
                    }
                };
            }]);