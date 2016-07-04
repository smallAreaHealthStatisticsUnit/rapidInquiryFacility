/* CONTROLLER for disease submission study area modal
 * Uses the maptable directive
 * On close(), stores results in ModelService
 */
/* global L */

angular.module("RIF")
        .controller('ModalStudyAreaCtrl', ['$scope', '$uibModal', 'StudyAreaStateService', 'SubmissionStateService',
            function ($scope, $uibModal, StudyAreaStateService, SubmissionStateService) {
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
                            SubmissionStateService.getState().studyTree = true;
                            $scope.tree = true;
                        }
                        //Store what has been selected
                        StudyAreaStateService.getState().polygonIDs = input.selectedPolygon;
                        StudyAreaStateService.getState().selectAt = input.selectAt;
                        StudyAreaStateService.getState().studyResolution = input.studyResolution;
                        StudyAreaStateService.getState().zoomLevel = input.zoomLevel;
                        StudyAreaStateService.getState().view = input.view;
                    });
                };
            }])
        .controller('ModalStudyAreaInstanceCtrl', function ($scope, $uibModalInstance, StudyAreaStateService) {
            $scope.input = {};
            $scope.input.selectedPolygon = StudyAreaStateService.getState().polygonIDs;
            $scope.input.selectAt = StudyAreaStateService.getState().selectAt;
            $scope.input.studyResolution = StudyAreaStateService.getState().studyResolution;
            $scope.input.zoomLevel = StudyAreaStateService.getState().zoomLevel;
            $scope.input.view = StudyAreaStateService.getState().view;

            $scope.close = function () {
                $uibModalInstance.dismiss();
            };
            $scope.submit = function () {
                $uibModalInstance.close($scope.input);
            };
        });