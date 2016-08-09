/* CONTROLLER for disease submission run study modal
 * 
 * 
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
                        keyboard: false
                    });
                    modalInstance.result.then(function () {
                        $scope.showWarning("a warning message");
                    });
                };
            }])
        .controller('ModalRunInstanceCtrl', function ($scope, $uibModalInstance, SubmissionStateService, user) {
            $scope.input = {};
            $scope.input.studyDescription = SubmissionStateService.getState().studyDescription;
            //    $scope.input.name = SubmissionStateService.getState().projectName;
            
            
            //Fill health themes drop-down
            $scope.input.projects = [];
            $scope.input.fillProjects = function () {
                user.getProjects(user.currentUser).then(handleProjects, handleProjects);
            }();
            function handleProjects(res) {
                $scope.input.projects.length = 0;
                for (var i = 0; i < res.data.length; i++) {
                    $scope.input.projects.push({name: res.data[i].name, description: res.data[i].description});
                    //TODO: description is NULL but there is a getProjectDescription method
                }
                $scope.input.project = $scope.input.projects[0];
                $scope.updateModel();
            }

            $scope.close = function () {
                $uibModalInstance.dismiss();
            };
            $scope.submit = function () {
                $uibModalInstance.close($scope.input);
            };
            $scope.updateModel = function () {
                SubmissionStateService.getState().studyDescription = $scope.input.studyDescription;
                SubmissionStateService.getState().projectDescription = $scope.input.project.description;
                SubmissionStateService.getState().projectName = $scope.input.project.name;
            };
        });