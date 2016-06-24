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
        .controller('ModalRunInstanceCtrl', function ($scope, $uibModalInstance, SubmissionStateService) {
            $scope.input = {};
            $scope.input.description = SubmissionStateService.get_state().projectDescription;
            $scope.input.name = SubmissionStateService.get_state().projectName;

            $scope.close = function () {
                $uibModalInstance.dismiss();
            };
            $scope.submit = function () {
                $uibModalInstance.close($scope.input);
            };
            $scope.updateModel = function () {
                SubmissionStateService.get_state().projectDescription = $scope.input.description;
                SubmissionStateService.get_state().projectName = $scope.input.name;
            };
        });