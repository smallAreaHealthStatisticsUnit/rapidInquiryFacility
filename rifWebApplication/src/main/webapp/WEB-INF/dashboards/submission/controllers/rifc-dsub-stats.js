/* CONTROLLER for disease submission stats options modal
 * 
 * 
 */
angular.module("RIF")
        .controller('ModalStatsCtrl', ['$scope', '$uibModal', 'StatsStateService', 'SubmissionStateService',
            function ($scope, $uibModal, StatsStateService, SubmissionStateService) {
                $scope.tree = SubmissionStateService.get_state().statsTree;
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

                        SubmissionStateService.get_state().statsTree = true;
                        $scope.tree = true;

                        StatsStateService.get_state().checked = input.checked;
                        StatsStateService.get_state().bym_c = input.bym_c;
                        StatsStateService.get_state().het_a = input.het_a;
                        StatsStateService.get_state().het_b = input.het_b;
                        StatsStateService.get_state().car_a = input.car_a;

                        $scope.showWarning("a warning message");
                    });
                };
            }])
        .controller('ModalStatsInstanceCtrl', function ($scope, $uibModalInstance, StatsStateService) {
            $scope.input = {};
            $scope.input.checked = StatsStateService.get_state().checked;
            $scope.input.bym_c = StatsStateService.get_state().bym_c;
            $scope.input.het_a = StatsStateService.get_state().het_a;
            $scope.input.het_b = StatsStateService.get_state().het_b;
            $scope.input.car_a = StatsStateService.get_state().car_a;

            $scope.close = function () {
                StatsStateService.reset_state();
                $uibModalInstance.dismiss();
            };
            $scope.submit = function () {
                $uibModalInstance.close($scope.input);
            };
        });