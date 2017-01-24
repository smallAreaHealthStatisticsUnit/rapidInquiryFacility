/* 
 * CONTROLLER for disease submission comparison area modal
 * Uses the maptable directive
 */
/* global L */

angular.module("RIF")
        .controller('ModalComparisonAreaCtrl', ['$scope', '$uibModal', 'CompAreaStateService', 'SubmissionStateService', 'StudyAreaStateService',
            function ($scope, $uibModal, CompAreaStateService, SubmissionStateService, StudyAreaStateService) {
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
                            //check resolutions are compatible
                            if (StudyAreaStateService.getState().studyResolution !== "") {
                                if (input.geoLevels.indexOf(input.studyResolution) >
                                        input.geoLevels.indexOf(StudyAreaStateService.getState().studyResolution)) {
                                    $scope.showError("Comparision area study resolution cannot be higher than for the study area");
                                    SubmissionStateService.getState().comparisonTree = false;
                                    $scope.tree = false;
                                } else {
                                    SubmissionStateService.getState().comparisonTree = true;
                                    $scope.tree = true;
                                }
                            } else {
                                SubmissionStateService.getState().comparisonTree = true;
                                $scope.tree = true;
                            }
                        }
                        //Store what has been selected
                        CompAreaStateService.getState().polygonIDs = input.selectedPolygon;
                        CompAreaStateService.getState().selectAt = input.selectAt;
                        CompAreaStateService.getState().studyResolution = input.studyResolution;
                        CompAreaStateService.getState().center = input.center;
                        CompAreaStateService.getState().geography = input.geography;
                        CompAreaStateService.getState().transparency = input.transparency;
                    });
                };
            }])
        .controller('ModalComparisonAreaInstanceCtrl', function ($scope, $uibModalInstance, CompAreaStateService) {
            $scope.input = {};
            $scope.input.name = "ComparisionAreaMap";
            $scope.input.selectedPolygon = CompAreaStateService.getState().polygonIDs;
            $scope.input.selectAt = CompAreaStateService.getState().selectAt;
            $scope.input.studyResolution = CompAreaStateService.getState().studyResolution;
            $scope.input.center = CompAreaStateService.getState().center;
            $scope.input.geography = CompAreaStateService.getState().geography;
            $scope.input.transparency = CompAreaStateService.getState().transparency;
            $scope.input.bands = [1];

            $scope.close = function () {
                $uibModalInstance.dismiss();
            };
            $scope.submit = function () {
                $uibModalInstance.close($scope.input);
            };
        });
