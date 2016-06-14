/* CONTROLLER for disease submission stats options modal
 * 
 * 
 */
angular.module("RIF")
        .controller('ModalStatsCtrl', ['$scope', '$uibModal', 'ModelService',
            function ($scope, $uibModal, ModelService) {
                $scope.tree = false;
                $scope.animationsEnabled = false;
                $scope.open = function () {
                    var modalInstance = $uibModal.open({
                        animation: true,
                        templateUrl: 'submission/partials/rifp-dsub-stats.html',
                        controller: 'ModalStatsInstanceCtrl',
                        windowClass: 'stats-Modal',
                        backdrop: 'static',
                        keyboard: false
                    });
                    modalInstance.result.then(function (input) {
                        //TODO: form validation
                        
                        ModelService.set_calculationmethod(input);

                        //Change tree icon colour
                        $scope.tree = true;
                        $scope.showWarning("a warning message");
                    });
                };
            }])
        .controller('ModalStatsInstanceCtrl', function ($scope, $uibModalInstance) {
            $scope.input = {};
            $scope.input.checked = "1";
            $scope.input.bym_c = 10;
            $scope.input.het_a = 5;
            $scope.input.het_b = 10;
            $scope.input.car_a = 5;
            
            $scope.close = function () {
                $uibModalInstance.dismiss();
            };
            $scope.submit = function () {
                $uibModalInstance.close($scope.input);
            };
        });