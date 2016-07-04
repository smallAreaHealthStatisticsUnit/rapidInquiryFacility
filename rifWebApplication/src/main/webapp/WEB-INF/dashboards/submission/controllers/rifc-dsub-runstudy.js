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
            $scope.input.description = SubmissionStateService.getState().projectDescription;
            $scope.input.name = SubmissionStateService.getState().projectName;

            $scope.close = function () {
                $uibModalInstance.dismiss();
            };
            $scope.submit = function () {
                $uibModalInstance.close($scope.input);
            };
            $scope.updateModel = function () {
                SubmissionStateService.getState().projectDescription = $scope.input.description;
                SubmissionStateService.getState().projectName = $scope.input.name;
            };
        });