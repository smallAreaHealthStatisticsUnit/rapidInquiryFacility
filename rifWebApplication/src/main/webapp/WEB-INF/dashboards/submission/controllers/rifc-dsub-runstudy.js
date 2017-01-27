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