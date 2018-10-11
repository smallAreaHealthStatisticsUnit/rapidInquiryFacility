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
 * CONTROLLER for disease submission study area modal
 */
/* global L */

angular.module("RIF")
        .controller('ModalStudyAreaCtrl', ['$state', '$scope', '$uibModal', 'StudyAreaStateService', 
			'SubmissionStateService', 'CompAreaStateService', 'SelectStateService', 'ModelService', 'CommonMappingStateService',
            function ($state, $scope, $uibModal, StudyAreaStateService, 
			SubmissionStateService, CompAreaStateService, SelectStateService, ModelService, CommonMappingStateService) {
                $scope.tree = SubmissionStateService.getState().studyTree;
                $scope.animationsEnabled = false;
                $scope.open = function () {
                    var modalInstance = $uibModal.open({
                        animation: $scope.animationsEnabled,
                        templateUrl: 'dashboards/submission/partials/rifp-dsub-studyarea.html',
                        controller: 'ModalStudyAreaInstanceCtrl',
                        windowClass: 'modal-fit',
                        backdrop: 'static',
                        keyboard: false
                    });
                    modalInstance.result.then(function (input) {
                        //Change tree icon colour
                        if (input.selectedPolygon.length === 0) {
                            SubmissionStateService.getState().studyTree = false;
                            $scope.tree = false;
						}
						else if (!ModelService.verifyStudyState()) { // Check study types - FAILS
                            SubmissionStateService.getState().studyTree = false;
                            $scope.tree = false;
                        } 
						else {
                            //if CompAreaStateService studyResolution is now greater than the new Study studyResolution
                            //then clear the comparison area tree and show a warning
                            //Study tree will not change
                            if (CompAreaStateService.getState().studyResolution !== "") {
                                if (input.geoLevels.indexOf(CompAreaStateService.getState().studyResolution) >
                                        input.geoLevels.indexOf(input.studyResolution)) {
                                    $scope.showError("Comparison area study resolution cannot be higher than for the study area");
                                    //clear the comparison tree
                                    SubmissionStateService.getState().comparisonTree = false;
                                    CompAreaStateService.getState().studyResolution = "";
                                    //reset tree
                                    $state.go('state1').then(function () {
                                        $state.reload();
                                    });
                                }
                            }
                            SubmissionStateService.getState().studyTree = true;
                            $scope.tree = true;
                        }

						$scope.areamap=SubmissionStateService.getAreaMap();
						SubmissionStateService.setRemoveMap(function() { // Setup map remove function
                            $scope.consoleDebug("[rifc-dsub-studyarea.js] remove shared areamap");
							$scope.areamap.remove(); 
						});
						
                        //Store what has been selected
                        StudyAreaStateService.getState().setGeoLevels(input.geoLevels);
                        StudyAreaStateService.getState().setPolygonIDs(input.selectedPolygon);
                        StudyAreaStateService.getState().setSelectAt(input.selectAt);
                        StudyAreaStateService.getState().setStudyResolution(input.studyResolution);
                        StudyAreaStateService.getState().setCenter(input.center);
                        StudyAreaStateService.getState().setGeography(input.geography);
                        StudyAreaStateService.getState().setTransparency(input.transparency);
                        StudyAreaStateService.getState().setType(input.type);
						
						if (SelectStateService.getState().studySelection == undefined) {
							if (input.type == "Disease Mapping") {
								SelectStateService.resetState();
							}
							else {
								SelectStateService.initialiseRiskAnalysis();
							}
						}
						if (SelectStateService.getState().studyType == "disease_mapping_study" && input.type == "Disease Mapping") {
						}
						else if (SelectStateService.getState().studyType == "risk_analysis_study" && input.type == "Risk Analysis") {
						}
						else {
							$scope.showErrorNoHide("[rifc-dsub-studyarea.js] Study type mismatch, expecting SelectStateService.getState().studyType: " +
								SelectStateService.getState().studyType + " ; got: " + input.type);
								
                            SubmissionStateService.getState().studyTree = false;
                            $scope.tree = false;
						}
						
						if (input.selectAt) {
							SelectStateService.getState().studySelection.studySelectAt = angular.copy(input.selectAt);
						}
						if (input.selectedPolygon) {
							SelectStateService.getState().studySelection.studySelectedAreas = angular.copy(
								input.selectedPolygon);
						}
						
						try {
							if (SelectStateService.getState().studySelection.studySelectedAreas.length > 0) {
								var r=SelectStateService.verifyStudySelection;
								var areaNameList=CommonMappingStateService.getState("areamap").setAreaNameList(input.name);
							}
							else {
								$scope.showWarning("No study areas selected");
                                SubmissionStateService.getState().studyTree = false;
                                $scope.tree = false;
							}
						}
						catch (e) {
							$scope.showWarningNoHide("Unable to verify study area selection: " + e.message);
							$scope.consoleDebug("[rifc-dsub-studyarea.js] input: " +
								JSON.stringify(input, null, 1));
							$scope.consoleDebug("[rifc-dsub-studyarea.js] SelectStateService.getState(): " +
								JSON.stringify(SelectStateService.getState(), null, 1));
								
                            SubmissionStateService.getState().studyTree = false;
                            $scope.tree = false;
						}
                    });
                };
            }])
        .controller('ModalStudyAreaInstanceCtrl', ['$scope', '$uibModalInstance', 'StudyAreaStateService', 'AlertService', 
				function ($scope, $uibModalInstance, StudyAreaStateService, AlertService) {
            $scope.input = {};
            $scope.input.name = "StudyAreaMap";
            $scope.input.selectedPolygon = StudyAreaStateService.getState().polygonIDs;
//			AlertService.consoleDebug("[rifc-dsub-studyarea.js] selectedPolygon[" + $scope.input.selectedPolygon.length + "]: " +
//				JSON.stringify($scope.input.selectedPolygon));
            $scope.input.selectAt = StudyAreaStateService.getState().selectAt;
            $scope.input.studyResolution = StudyAreaStateService.getState().studyResolution;
            $scope.input.center = StudyAreaStateService.getState().center;
            $scope.input.geography = StudyAreaStateService.getState().geography;
            $scope.input.transparency = StudyAreaStateService.getState().transparency;
            $scope.input.type = StudyAreaStateService.getState().type; 
			$scope.input.showSwitch = true; // Show switch
            if ($scope.input.type === "Risk Analysis") {  
                $scope.input.bands = [1, 2, 3, 4, 5, 6];
            } else {  
                $scope.input.bands = [1];
            }

            $scope.close = function () {
                $uibModalInstance.dismiss();
            };
            $scope.submit = function () {
                $uibModalInstance.close($scope.input);
            };
        }]);