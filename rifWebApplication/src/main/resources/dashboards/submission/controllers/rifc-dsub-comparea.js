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
 * CONTROLLER for disease submission comparison area modal
 */

/* global L */

angular.module("RIF")
        .controller('ModalComparisonAreaCtrl', ['$scope', '$uibModal', 'CompAreaStateService', 
			'SubmissionStateService', 'StudyAreaStateService', 'SelectStateService', 'ModelService', 'CommonMappingStateService',
            function ($scope, $uibModal, CompAreaStateService, SubmissionStateService, 
				StudyAreaStateService, SelectStateService, ModelService, CommonMappingStateService) {
                $scope.tree = SubmissionStateService.getState().comparisonTree;
                $scope.animationsEnabled = false;
                $scope.open = function () {
                    var modalInstance = $uibModal.open({
                        animation: $scope.animationsEnabled,
                        templateUrl: 'dashboards/submission/partials/rifp-dsub-comparea.html',
                        controller: 'ModalComparisonAreaInstanceCtrl',
                        windowClass: 'modal-fit',
                        backdrop: 'static',
                        keyboard: false
                    });
                    modalInstance.result.then(function (input) {
                        //Change tree icon colour
                        if (input.selectedPolygon.length === 0) {
                            SubmissionStateService.getState().comparisonTree = false;
                            $scope.tree = false;
                        } 

						else if (!ModelService.verifyStudyState()) { // Check study types - FAILS
                            SubmissionStateService.getState().comparisonTree = false;
                            $scope.tree = false;
                        }						
						else {
                            //check resolutions are compatible
                            if (StudyAreaStateService.getState().studyResolution !== "") {
                                if (input.geoLevels.indexOf(input.studyResolution) >
                                        input.geoLevels.indexOf(StudyAreaStateService.getState().studyResolution)) {
                                    $scope.showError("Comparison area study resolution cannot be higher than for the study area");
                                    SubmissionStateService.getState().comparisonTree = false;
                                    $scope.tree = false;
                                } else {
                                    SubmissionStateService.getState().comparisonTree = true;
                                    $scope.tree = true;
                                }
                            } else {
                                SubmissionStateService.getState().comparisonTree = true;
                                $scope.tree = true;
                            }
                        }
						$scope.areamap=SubmissionStateService.getAreaMap();
						SubmissionStateService.setRemoveMap(function() { // Setup map remove function
                            $scope.consoleDebug("[rifc-dsub-comparea.js] remove shared areamap");
							$scope.areamap.remove(); 
						});
                        //Store what has been selected
                        CompAreaStateService.getState().setPolygonIDs(input.selectedPolygon);
                        CompAreaStateService.getState().setSelectAt(input.selectAt);
                        CompAreaStateService.getState().setStudyResolution(input.studyResolution);
                        CompAreaStateService.getState().setCenter(input.center);
                        CompAreaStateService.getState().setGeography(input.geography);
                        CompAreaStateService.getState().setTransparency(input.transparency);
						
						if (input.selectAt) {
							SelectStateService.getState().studySelection.comparisonSelectAt = angular.copy(input.selectAt);
						}
						if (input.selectedPolygon) {
							SelectStateService.getState().studySelection.comparisonSelectedAreas = angular.copy(
								input.selectedPolygon);
							var areaNameList=CommonMappingStateService.getState("areamap").setAreaNameList(input.name);
						}
						
						try {
							if (SelectStateService.getState().studySelection.comparisonSelectedAreas.length > 0) {
								// FIX: Warning: Unable to verify comparison area selection: study selection resolution not setup correctly
								if (SelectStateService.getState().studySelection.studySelectAt == undefined) {
									SelectStateService.getState().studySelection.studySelectAt=angular.copy(StudyAreaStateService.getState().selectAt);
								}
								// FIX: Warning: Unable to verify comparison area selection: at least one study area required
								if (SelectStateService.getState().studySelection.studySelectedAreas == undefined ||
									SelectStateService.getState().studySelection.studySelectedAreas.length <1) {
//									$scope.consoleDebug("[rifc-dsub-comparea.js] Fix studySelectedAreas: StudyAreaStateService.getState(): " +
//										JSON.stringify(StudyAreaStateService.getState(), null, 1));
									SelectStateService.getState().studySelection.studySelectedAreas=angular.copy(StudyAreaStateService.getState().polygonIDs);
								}
								
								var r=SelectStateService.verifyStudySelection();
							}
							else {
								$scope.showWarning("No comparison areas selected");
                                SubmissionStateService.getState().comparisonTree = false;
                                $scope.tree = false;
							}
						}
						catch (e) {
							$scope.showWarningNoHide("Unable to verify comparison area selection: " + e.message);
							$scope.consoleDebug("[rifc-dsub-comparea.js] input: " +
								JSON.stringify(input, null, 1));
							$scope.consoleDebug("[rifc-dsub-comparea.js] SelectStateService.getState(): " +
								JSON.stringify(SelectStateService.getState(), null, 1));
								
                            CompAreaStateService.getState().comparisonTree = false;
                            $scope.tree = false;
						}						
                    });
                };
            }])
        .controller('ModalComparisonAreaInstanceCtrl', ['$scope', '$uibModalInstance', 'CompAreaStateService', 'AlertService', 
				function ($scope, $uibModalInstance, CompAreaStateService, AlertService) {
            $scope.input = {};
            $scope.input.name = "ComparisionAreaMap";
            $scope.input.selectedPolygon = CompAreaStateService.getState().polygonIDs;
//			AlertService.consoleDebug("[rifc-dsub-comparea.js] selectedPolygon[" + $scope.input.selectedPolygon.length + "]: " +
//				JSON.stringify($scope.input.selectedPolygon));
            $scope.input.selectAt = CompAreaStateService.getState().selectAt;
            $scope.input.studyResolution = CompAreaStateService.getState().studyResolution;
            $scope.input.center = CompAreaStateService.getState().center;
            $scope.input.geography = CompAreaStateService.getState().geography;
            $scope.input.transparency = CompAreaStateService.getState().transparency;
            $scope.input.showSwitch = false;
            $scope.input.bands = [1]; //comparison has 1 band only

            $scope.close = function () {
                $uibModalInstance.dismiss();
            };
            $scope.submit = function () {
                $uibModalInstance.close($scope.input);
            };
        }]);