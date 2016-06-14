/* CONTROLLER for disease submission study area modal
 * Uses the maptable directive
 * On close(), stores results in ModelService
 */
/* global L */

angular.module("RIF")
        .controller('ModalStudyAreaCtrl', ['$scope', '$uibModal', 'ModelService',
            function ($scope, $uibModal, ModelService) {
                $scope.tree = false;
                $scope.animationsEnabled = false;
                $scope.open = function () {
                    var modalInstance = $uibModal.open({
                        animation: $scope.animationsEnabled,
                        templateUrl: 'submission/partials/rifp-dsub-studyarea.html',
                        controller: 'ModalStudyAreaInstanceCtrl',
                        windowClass: 'modal-fit',
                        backdrop: 'static',
                        keyboard: false
                    });
                    modalInstance.result.then(function () {
                        //Change tree icon colour
                        $scope.tree = true;

                        //TODO: add to model service
                     //   ModelService.set_studyName('THIS-STUDY-NAME');

                        //      $scope.showError("an error message");
                        $scope.showSuccess("a success message");
                        //      $scope.showWarning("a warning message");
                    });
                };
            }])
        .controller('ModalStudyAreaInstanceCtrl', function ($scope, $uibModalInstance) {
            $scope.close = function () {
                $uibModalInstance.dismiss();
            };
            $scope.submit = function () {
                $uibModalInstance.close();
            };
        });