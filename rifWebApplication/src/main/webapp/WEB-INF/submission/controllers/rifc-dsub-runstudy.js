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
                        templateUrl: 'submission/partials/rifp-dsub-runstudy.html',
                        controller: 'ModalRunInstanceCtrl',
                        windowClass: 'stats-Modal',
                        backdrop: 'static',
                        keyboard: false
                    });
                    modalInstance.result.then(function (input) {
                        //input is returned from Modal instance, not used 
                        $scope.showWarning("a warning message");
                    });
                };
            }])
        .controller('ModalRunInstanceCtrl', function ($scope, $uibModalInstance, ModelService) {
            $scope.input = {};
            $scope.input.description = "";
            $scope.input.name = "";
            ModelService.set_description($scope.input.description);
            ModelService.set_name($scope.input.name);

            $scope.close = function () {
                $uibModalInstance.dismiss();
            };
            $scope.submit = function () {
                $uibModalInstance.close($scope.input);
            };
            $scope.updateModel = function () {
                ModelService.set_description($scope.input.description);
                ModelService.set_name($scope.input.name);
            };
        });