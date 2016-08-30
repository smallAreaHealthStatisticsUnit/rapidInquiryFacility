/* CONTROLLER for disease submission reset model
 * 
 * 
 */
angular.module("RIF")
        .controller('ModalResetCtrl', ['$scope', '$uibModal', 'StudyAreaStateService',
            function ($scope, $uibModal, StudyAreaStateService) {
                $scope.open = function () {
                    var modalInstance = $uibModal.open({
                        animation: true,
                        templateUrl: 'dashboards/submission/partials/rifp-dsub-reset.html',
                        controller: 'ModalResetInstanceCtrl',
                        windowClass: 'stats-Modal',
                        backdrop: 'static',
                        scope: $scope,
                        keyboard: false
                    });
                    modalInstance.result.then(function () {

                    });
                };
            }])
        .controller('ModalResetInstanceCtrl', function ($scope, $uibModalInstance) {

            $scope.close = function () {
                $uibModalInstance.dismiss();
            };
            $scope.submit = function () {
                $scope.showSuccess("RIF study reset");
                $uibModalInstance.close();
            };
        });