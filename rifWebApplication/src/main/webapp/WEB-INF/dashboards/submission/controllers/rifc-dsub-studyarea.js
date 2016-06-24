/* CONTROLLER for disease submission study area modal
 * Uses the maptable directive
 * On close(), stores results in ModelService
 */
/* global L */

angular.module("RIF")
        .controller('ModalStudyAreaCtrl', ['$scope', '$uibModal', 'StudyAreaStateService', 'SubmissionStateService',
            function ($scope, $uibModal, StudyAreaStateService, SubmissionStateService) {
                $scope.tree = SubmissionStateService.get_state().studyTree;
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
                            SubmissionStateService.get_state().studyTree = false;
                            $scope.tree = false;
                        } else {
                            SubmissionStateService.get_state().studyTree = true;
                            $scope.tree = true;
                        }

                        //Store what has been selected
                        StudyAreaStateService.get_state().polygonIDs = input.selectedPolygon;  
                        StudyAreaStateService.get_state().selectAt = input.selectAt;
                        StudyAreaStateService.get_state().studyResolution = input.studyResolution;
                    });
                };
            }])
        .controller('ModalStudyAreaInstanceCtrl', function ($scope, $uibModalInstance, StudyAreaStateService) {
            $scope.input = {};
            $scope.input.selectedPolygon = StudyAreaStateService.get_state().polygonIDs;
            $scope.input.selectAt = StudyAreaStateService.get_state().selectAt;         
            $scope.input.studyResolution = StudyAreaStateService.get_state().studyResolution;
            
            $scope.close = function () {
                $uibModalInstance.dismiss();
            };
            $scope.submit = function () {
                $uibModalInstance.close($scope.input);
            };
        });