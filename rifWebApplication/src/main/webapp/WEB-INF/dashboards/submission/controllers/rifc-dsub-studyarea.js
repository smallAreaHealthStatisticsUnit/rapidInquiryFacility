/* CONTROLLER for disease submission study area modal
 * Uses the maptable directive
 * On close(), stores results in ModelService
 */
/* global L */

angular.module("RIF")
        .controller('ModalStudyAreaCtrl', ['$state', '$scope', '$uibModal', 'StudyAreaStateService', 'SubmissionStateService', 'CompAreaStateService',
            function ($state, $scope, $uibModal, StudyAreaStateService, SubmissionStateService, CompAreaStateService) {
                $scope.tree = SubmissionStateService.getState().studyTree;
                $scope.animationsEnabled = false;
                $scope.open = function () {
                    var modalInstance = $uibModal.open({
                        animation: $scope.animationsEnabled,
                        templateUrl: 'dashboards/submission/partials/rifp-dsub-studyarea.html',
                        controller: 'ModalStudyAreaInstanceCtrl',
                        windowClass: 'modal-fit',
                        backdrop: 'static',
                        keyboard: false
                    });
                    modalInstance.result.then(function (input) {
                        //Change tree icon colour
                        if (input.selectedPolygon.length === 0) {
                            SubmissionStateService.getState().studyTree = false;
                            $scope.tree = false;
                        } else {
                            //if CompAreaStateService studyResolution is now greater than the new Study studyResolution
                            //then clear the comparison area tree and show a warning
                            //Study tree will not change
                            if (CompAreaStateService.getState().studyResolution !== "") {
                                if (input.geoLevels.indexOf(CompAreaStateService.getState().studyResolution) >
                                        input.geoLevels.indexOf(input.studyResolution)) {
                                    $scope.showError("Comparision area study resolution cannot be higher than for the study area");
                                    //clear the comparison tree
                                    SubmissionStateService.getState().comparisonTree = false;
                                    CompAreaStateService.getState().studyResolution = "";
                                    //reset tree
                                    $state.go('state1').then(function () {
                                        $state.reload();
                                    });
                                }
                            }
                            SubmissionStateService.getState().studyTree = true;
                            $scope.tree = true;
                        }

                        //Store what has been selected
                        StudyAreaStateService.getState().geoLevels = input.geoLevels;
                        StudyAreaStateService.getState().polygonIDs = input.selectedPolygon;
                        StudyAreaStateService.getState().selectAt = input.selectAt;
                        StudyAreaStateService.getState().studyResolution = input.studyResolution;
                        StudyAreaStateService.getState().zoomLevel = input.zoomLevel;
                        StudyAreaStateService.getState().view = input.view;
                        StudyAreaStateService.getState().geography = input.geography;
                        StudyAreaStateService.getState().transparency = input.transparency;
                    });
                };
            }])
        .controller('ModalStudyAreaInstanceCtrl', function ($scope, $uibModalInstance, StudyAreaStateService) {
            $scope.input = {};
            $scope.input.name = "StudyAreaMap";
            $scope.input.selectedPolygon = StudyAreaStateService.getState().polygonIDs;
            $scope.input.selectAt = StudyAreaStateService.getState().selectAt;
            $scope.input.studyResolution = StudyAreaStateService.getState().studyResolution;
            $scope.input.zoomLevel = StudyAreaStateService.getState().zoomLevel;
            $scope.input.view = StudyAreaStateService.getState().view;
            $scope.input.geography = StudyAreaStateService.getState().geography;
            $scope.input.transparency = StudyAreaStateService.getState().transparency;
            $scope.input.bands = [1, 2, 3, 4, 5, 6];

            $scope.close = function () {
                $uibModalInstance.dismiss();
            };
            $scope.submit = function () {
                $uibModalInstance.close($scope.input);
            };
        });