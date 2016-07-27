/* CONTROLLER for disease submission stats options modal
 * 
 * 
 */
angular.module("RIF")
        .controller('ModalStatsCtrl', ['$scope', '$uibModal', 'StatsStateService', 'SubmissionStateService',
            function ($scope, $uibModal, StatsStateService, SubmissionStateService) {
                $scope.tree = SubmissionStateService.getState().statsTree;
                $scope.animationsEnabled = false;
                $scope.open = function () {
                    var modalInstance = $uibModal.open({
                        animation: true,
                        templateUrl: 'dashboards/submission/partials/rifp-dsub-stats.html',
                        controller: 'ModalStatsInstanceCtrl',
                        windowClass: 'stats-Modal',
                        backdrop: 'static',
                        keyboard: false
                    });
                    modalInstance.result.then(function (input) {
                        //TODO: form validation

                        SubmissionStateService.getState().statsTree = true;
                        $scope.tree = true;

                        StatsStateService.getState().checked = input.checked;
                        StatsStateService.getState().bym_c = input.bym_c;
                        StatsStateService.getState().het_a = input.het_a;
                        StatsStateService.getState().het_b = input.het_b;
                        StatsStateService.getState().car_a = input.car_a;
                    });
                };
            }])
        .controller('ModalStatsInstanceCtrl', function ($scope, $uibModalInstance, StatsStateService) {
            $scope.input = {};
            $scope.input.checked = StatsStateService.getState().checked;
            $scope.input.bym_c = StatsStateService.getState().bym_c;
            $scope.input.het_a = StatsStateService.getState().het_a;
            $scope.input.het_b = StatsStateService.getState().het_b;
            $scope.input.car_a = StatsStateService.getState().car_a;

            $scope.close = function () {
                StatsStateService.resetState();
                $uibModalInstance.dismiss();
            };
            $scope.submit = function () {
                $uibModalInstance.close($scope.input);
            };
        });