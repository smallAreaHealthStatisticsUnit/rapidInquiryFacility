/* 
 * CONTROLLER for disease submission retrieve study modal
 */
/* global URL */

angular.module("RIF")
        .controller('ModalRetrieveCtrl', ['$scope', '$uibModal', 'ModelService',
            function ($scope, $uibModal, ModelService) {
                
                //get the study object
                $scope.getBlob = function () {
                    var data = ModelService.get_rif_job_submission_JSON();
                    var json = JSON.stringify(data);
                    return new Blob([json], {type: "application/json"});
                };

                $scope.open = function () {
                    var modalInstance = $uibModal.open({
                        animation: true,
                        templateUrl: 'dashboards/submission/partials/rifp-dsub-retrieve.html',
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