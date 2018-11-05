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
 * CONTROLLER for disease submission stats options modal
 */
angular.module("RIF")
        .controller('ModalStatsCtrl', ['$scope', '$uibModal', 'StatsStateService', 'SubmissionStateService', 'AlertService', 'user', '$window',
            function ($scope, $uibModal, StatsStateService, SubmissionStateService, AlertService, user, $window) {
                $scope.tree = SubmissionStateService.getState().statsTree;

                //get available methods
                user.getAvailableCalculationMethods(user.currentUser).then(handleAvailableCalculationMethods, handleAvailableCalculationMethods);

                function handleAvailableCalculationMethods(res) {
                    try {
                        $scope.methods = res.data;

                        //fill default state on form first load              
                        if (StatsStateService.getState().model.length === 0) {
                            var myModel = [];
                            for (var i = 0; i < res.data.length; i++) {
                                //Params not used at present - may or may not in future depending on stats methods used
                                var params = [];
                                for (var j = 0; j < res.data[i].parameterProxies.length; j++) {
                                    params.push(Number(res.data[i].parameterProxies[j].value));
                                }
                                myModel.push(params);
                            }
                            StatsStateService.getState().model = myModel;
                            StatsStateService.getState().methods = res.data;
                        }
                    } catch (e) {
                        $scope.showError("Could not retrieve statistical methods: " + e);
                    }
                }

                $scope.openStatsManual = function () {
                    var baseUrl = "https://smallareahealthstatisticsunit.github.io/rapidInquiryFacility/standalone/RIF_v40_Manual.pdf";
                    $window.open(baseUrl);
                };

                $scope.open = function () {				
					$scope.defaultText="Indirectly standardised rates, Relative risk ratios, Empirical Bayes";
					$scope.isDiseaseMapping=false;
					if (SubmissionStateService.getState().studyType == "Disease Mapping") {
						$scope.isDiseaseMapping=true;
					}
					
					AlertService.consoleDebug("[rifc-dsub-stats.js] SubmissionStateService.getState().studyType: " + 
						SubmissionStateService.getState().studyType +
						"; $scope.isDiseaseMapping: " + $scope.isDiseaseMapping);
												
                    var modalInstance = $uibModal.open({
                        animation: true,
                        templateUrl: 'dashboards/submission/partials/rifp-dsub-stats.html',
                        controller: 'ModalStatsInstanceCtrl',
                        windowClass: 'stats-Modal',
                        backdrop: 'static',
                        scope: $scope,
                        keyboard: false
                    });
                    modalInstance.result.then(function (input) {
                        if (input.checked >= -1) {
                            SubmissionStateService.getState().statsTree = true;
                            $scope.tree = true;
                        } else {
                            SubmissionStateService.getState().statsTree = false;
                            $scope.tree = false;
                        }
                        StatsStateService.getState().checked = input.checked;
                    });
                };
            }])
        .controller('ModalStatsInstanceCtrl', function ($scope, $uibModalInstance, StatsStateService) {
            $scope.input = {};
            $scope.input.checked = StatsStateService.getState().checked;
            $scope.input.model = StatsStateService.getState().model;

            //reset to opening instance on dismiss
            var modelOnOpening = angular.copy($scope.input.model);
            var checkedOnOpening = angular.copy($scope.input.checked);
            $scope.close = function () {
                StatsStateService.getState().checked = checkedOnOpening;
                StatsStateService.getState().model = modelOnOpening;
                $uibModalInstance.dismiss();
            };

            $scope.submit = function () {
                //check numeric
                for (var i = 0; i < $scope.input.model.length; i++) {
                    for (var j = 0; j < $scope.input.model[i].length; j++) {
                        if ($scope.input.model[i][j] === null | isNaN(Number($scope.input.model[i][j]))) {
                            $scope.showError("Non-numeric parameter value provided");
                            return;
                        }
                    }
                }
                $uibModalInstance.close($scope.input);
            };
        });
