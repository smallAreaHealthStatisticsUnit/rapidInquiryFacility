/* CONTROLLER for disease submission retrieve study modal
 * 
 * 
 */
angular.module("RIF")
        .controller('ModalSummaryCtrl', ['$scope', '$uibModal', 'ModelService',
            function ($scope, $uibModal, ModelService) {

                $scope.summary = ModelService.get_rif_job_submission();

                $scope.open = function () {
                    var modalInstance = $uibModal.open({
                        animation: true,
                        templateUrl: 'dashboards/submission/partials/rifp-dsub-summary.html',
                        controller: 'ModalSummaryInstanceCtrl',
                        windowClass: 'modal-fit',
                        backdrop: 'static',
                        keyboard: false
                    });
                    modalInstance.result.then(function () {
                        $scope.showWarning("a warning message");
                    });
                };
            }])
        .controller('ModalSummaryInstanceCtrl', function ($scope, $uibModalInstance) {
            $scope.close = function () {
                $uibModalInstance.dismiss();
            };
            $scope.submit = function () {
                $uibModalInstance.close();
            };
        });