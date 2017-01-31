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
 * CONTROLLER for disease submission run study modal
 */
angular.module("RIF")
        .controller('ModalRunCtrl', ['$scope', '$uibModal',
            function ($scope, $uibModal) {
                $scope.open = function () {
                    var modalInstance = $uibModal.open({
                        animation: true,
                        templateUrl: 'dashboards/submission/partials/rifp-dsub-runstudy.html',
                        controller: 'ModalRunInstanceCtrl',
                        windowClass: 'stats-Modal',
                        backdrop: 'static',
                        scope: $scope,
                        keyboard: false
                    });
                    modalInstance.result.then(function () {
                        $scope.showSuccess("Study Submitted: Processing may take several minutes");
                    });
                };
            }])
        .controller('ModalRunInstanceCtrl', function ($scope, $uibModalInstance, SubmissionStateService, user, ModelService) {
            $scope.input = {};
            $scope.input.studyDescription = SubmissionStateService.getState().studyDescription;
            $scope.input.projectName = SubmissionStateService.getState().projectName;

            //Fill health themes drop-down
            $scope.input.projects = [];
            $scope.input.fillProjects = function () {
                user.getProjects(user.currentUser).then(function (res) {
                    $scope.input.projects.length = 0;
                    for (var i = 0; i < res.data.length; i++) {
                        $scope.input.projects.push(res.data[i].name);
                    }
                    if ($scope.input.projectName === "") {
                        $scope.input.projectName = $scope.input.projects[0];
                    }
                    $scope.updateModel();
                });
            }();

            $scope.close = function () {
                $uibModalInstance.dismiss();
            };
            
            $scope.submit = function () {
                //check ready to submit
                var errMsg = [];
                //1: Quick check on trees
                if (!SubmissionStateService.getState().studyTree) {
                    errMsg.push(" Study Area");
                }
                if (!SubmissionStateService.getState().comparisonTree) {
                    errMsg.push(" Comparision Area");
                }
                if (!SubmissionStateService.getState().investigationTree) {
                    errMsg.push(" Investigation Parameters");
                }
                if (!SubmissionStateService.getState().statsTree) {
                    errMsg.push(" Statistical Methods");
                }
                if (errMsg.length !== 0) {
                    $scope.showError("Could not submit study. Please complete - " + errMsg);
                    return;
                }
                //TODO: error if year params not set (if loaded from file)

                //If tests passed, then submitStudy
                var thisStudy = ModelService.get_rif_job_submission_JSON();
                user.submitStudy(user.currentUser, thisStudy);
                $uibModalInstance.close($scope.input);
            };

            $scope.updateModel = function () {
                user.getProjectDescription(user.currentUser, $scope.input.projectName).then(function (res) {
                    SubmissionStateService.getState().projectDescription = res.data[0].result;
                });
                SubmissionStateService.getState().projectName = $scope.input.projectName;
                SubmissionStateService.getState().studyDescription = $scope.input.studyDescription;
            };
        });