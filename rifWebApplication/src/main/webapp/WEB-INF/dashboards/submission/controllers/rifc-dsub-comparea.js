/* CONTROLLER for disease submission comparison area modal
 * Uses the maptable directive
 * On close(), stores results in ModelService
 */
/* global L */

angular.module("RIF")
        .controller('ModalComparisonAreaCtrl', ['$scope', '$uibModal', 'CompAreaStateService', 'SubmissionStateService',
            function ($scope, $uibModal, CompAreaStateService, SubmissionStateService) {
                $scope.tree = SubmissionStateService.get_state().comparisonTree;
                $scope.animationsEnabled = false;
                $scope.open = function () {
                    var modalInstance = $uibModal.open({
                        animation: $scope.animationsEnabled,
                        templateUrl: 'dashboards/submission/partials/rifp-dsub-comparea.html',
                        controller: 'ModalComparisonAreaInstanceCtrl',
                        windowClass: 'modal-fit',
                        backdrop: 'static',
                        keyboard: false
                    });
                    modalInstance.result.then(function (input) {
                        //Change tree icon colour
                        if (input.selectedPolygon.length === 0) {
                            SubmissionStateService.get_state().comparisonTree = false;
                            $scope.tree = false;
                        } else {
                            SubmissionStateService.get_state().comparisonTree = true;
                            $scope.tree = true;
                        }

                        //Store what has been selected
                        CompAreaStateService.get_state().polygonIDs = input.selectedPolygon;  
                        CompAreaStateService.get_state().selectAt = input.selectAt;
                        CompAreaStateService.get_state().studyResolution = input.studyResolution;
                    });
                };
            }])
        .controller('ModalComparisonAreaInstanceCtrl', function ($scope, $uibModalInstance, CompAreaStateService) {
            $scope.input = {};
            $scope.input.selectedPolygon = CompAreaStateService.get_state().polygonIDs;
            $scope.input.selectAt = CompAreaStateService.get_state().selectAt;         
            $scope.input.studyResolution = CompAreaStateService.get_state().studyResolution;
            
            $scope.close = function () {
                $uibModalInstance.dismiss();
            };
            $scope.submit = function () {
                $uibModalInstance.close($scope.input);
            };
        });
