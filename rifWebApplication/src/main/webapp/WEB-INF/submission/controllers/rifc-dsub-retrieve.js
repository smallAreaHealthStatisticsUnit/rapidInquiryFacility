/* CONTROLLER for disease submission retrieve study modal
 * 
 * 
 */
angular.module("RIF")
        .controller('ModalRetrieveCtrl', ['$scope', '$uibModal',
            function ($scope, $uibModal) {
                $scope.open = function () {
                    var modalInstance = $uibModal.open({
                        animation: true, 
                        templateUrl: 'submission/partials/rifp-dsub-retrieve.html',
                        controller: 'ModalRetrieveInstanceCtrl',
                        windowClass: 'stats-Modal',
                        backdrop: 'static',
                        keyboard: false
                    });
                    modalInstance.result.then(function () {
                        $scope.showWarning("a warning message");
                    });
                };
            }])
        .controller('ModalRetrieveInstanceCtrl', function ($scope, $uibModalInstance) {
            $scope.close = function () {
                $uibModalInstance.dismiss();
            };
            $scope.submit = function () {
                $uibModalInstance.close();
            };
        });