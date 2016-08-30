/* CONTROLLER for disease submission comparison area modal
 * Uses the maptable directive
 * On close(), stores results in ModelService
 */
/* global L */

angular.module("RIF")
        .controller('ModalComparisonAreaCtrl', ['$scope', '$uibModal', 'CompAreaStateService', 'SubmissionStateService',
            function ($scope, $uibModal, CompAreaStateService, SubmissionStateService) {
                $scope.tree = SubmissionStateService.getState().comparisonTree;
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
                            SubmissionStateService.getState().comparisonTree = false;
                            $scope.tree = false;
                        } else {
                            SubmissionStateService.getState().comparisonTree = true;
                            $scope.tree = true;
                        }
                        //Store what has been selected
                        CompAreaStateService.getState().polygonIDs = input.selectedPolygon;
                        CompAreaStateService.getState().selectAt = input.selectAt;
                        CompAreaStateService.getState().studyResolution = input.studyResolution;
                        CompAreaStateService.getState().zoomLevel = input.zoomLevel;
                        CompAreaStateService.getState().view = input.view;
                        CompAreaStateService.getState().geography = input.geography;
                    });
                };
            }])
        .controller('ModalComparisonAreaInstanceCtrl', function ($scope, $uibModalInstance, CompAreaStateService) {
            $scope.input = {};
            $scope.input.selectedPolygon = CompAreaStateService.getState().polygonIDs;
            $scope.input.selectAt = CompAreaStateService.getState().selectAt;
            $scope.input.studyResolution = CompAreaStateService.getState().studyResolution;
            $scope.input.zoomLevel = CompAreaStateService.getState().zoomLevel;
            $scope.input.view = CompAreaStateService.getState().view;
            $scope.input.geography = CompAreaStateService.getState().geography;

            $scope.close = function () {
                $uibModalInstance.dismiss();
            };
            $scope.submit = function () {
                $uibModalInstance.close($scope.input);
            };
        });
