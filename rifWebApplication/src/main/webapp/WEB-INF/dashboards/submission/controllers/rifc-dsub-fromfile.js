/* CONTROLLER for disease submission run study from file modal
 * 
 * 
 */
angular.module("RIF")
        .controller('ModalRunFileCtrl', ['$scope', '$uibModal',
            function ($scope, $uibModal) {
                $scope.open = function () {
                    var modalInstance = $uibModal.open({
                        animation: true, 
                        templateUrl: 'dashboards/submission/partials/rifp-dsub-fromfile.html',
                        controller: 'ModalRunFileInstanceCtrl',
                        windowClass: 'stats-Modal',
                        backdrop: 'static',
                        keyboard: false
                    });
                    modalInstance.result.then(function () {
                        $scope.showWarning("a warning message");
                    });
                };
            }])
        .controller('ModalRunFileInstanceCtrl', function ($scope, $uibModalInstance) {
            $scope.close = function () {
                $uibModalInstance.dismiss();
            };
            $scope.submit = function () {
                $uibModalInstance.close();
            };
        });