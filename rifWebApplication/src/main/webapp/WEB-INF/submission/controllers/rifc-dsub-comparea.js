/* CONTROLLER for disease submission comparison area modal
 * Uses the maptable directive
 * On close(), stores results in ModelService
 */
/* global L */

angular.module("RIF")
        .controller('ModalComparisonAreaCtrl', ['$scope', '$uibModal', 'ModelService', 
            function ($scope, $uibModal, ModelService) {
                $scope.tree = false;
                $scope.animationsEnabled = false;
                $scope.open = function () {
                    var modalInstance = $uibModal.open({
                        animation: $scope.animationsEnabled,
                        templateUrl: 'submission/partials/rifp-dsub-comparea.html',
                        controller: 'ModalComparisonAreaInstanceCtrl',
                        windowClass: 'modal-fit',
                        backdrop: 'static',
                        keyboard: false
                    });
                    modalInstance.result.then(function () {
                        //Change tree icon colour
                        $scope.tree = true;   
                    });
                };
            }])
        .controller('ModalComparisonAreaInstanceCtrl', function ($scope, $uibModalInstance) {
            $scope.close = function () {
                $uibModalInstance.dismiss();
            };
            $scope.submit = function () {
                $uibModalInstance.close();
            };
        });
