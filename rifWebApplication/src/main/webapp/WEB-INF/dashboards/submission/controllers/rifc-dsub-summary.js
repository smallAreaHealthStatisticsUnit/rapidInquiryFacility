/* 
 * CONTROLLER for disease submission summary study modal
 */
angular.module("RIF")
        .controller('ModalSummaryCtrl', ['$scope', '$uibModal', 'ModelService', '$sce',
            function ($scope, $uibModal, ModelService, $sce) {

                $scope.toggleText = "JSON";
                //get model summary as either JSON or HTML
                getModel = function (x) {
                    if (x === "JSON") {
                        $scope.summary = $sce.trustAsHtml(ModelService.get_rif_job_submission_HTML());
                    } else {
                        $scope.summary = ModelService.get_rif_job_submission_JSON();
                    }
                };
                $scope.toggleJSON = function () {
                    $scope.toggleText = $scope.toggleText === "JSON" ? "Formatted" : "JSON";
                    getModel($scope.toggleText);
                };
                getModel($scope.toggleText);

                $scope.open = function () {
                    var modalInstance = $uibModal.open({
                        animation: true,
                        templateUrl: 'dashboards/submission/partials/rifp-dsub-summary.html',
                        controller: 'ModalSummaryInstanceCtrl',
                        windowClass: 'summary-Modal',
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