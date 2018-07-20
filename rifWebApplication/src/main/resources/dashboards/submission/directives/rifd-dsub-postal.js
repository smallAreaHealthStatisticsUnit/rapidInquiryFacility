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
							
                        var thisGeography = SubmissionStateService.getState().geography;
						
						// Also defined in rifs-util-leafletdraw.js
                        var factory = L.icon({
                            iconUrl: 'images/factory.png',
                            iconAnchor: [16, 16]
                        });
                        //user input boxes
                        scope.bandAttr = [];	
						
                        element.on('click', function (event) {
							
							scope.postcodeChange = function(attr) {
								scope.postcode=attr;
							}
							scope.checkPostcode = function() {
								
								user.getPostalCodes(user.currentUser, thisGeography, scope.postcode).then(function (res) {     
									AlertService.consoleDebug("[rifd-dsub-postal.js] postcode change: " + scope.postcode +
										"; res: " + JSON.stringify(res, null, 1));
								}, function () { // Error handler
									AlertService.rifMessage('warning', "Could not postal codes from the database");

								}).then(function () {
								});
							}
							
                            scope.modalHeader = "Select by Postal Code";
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