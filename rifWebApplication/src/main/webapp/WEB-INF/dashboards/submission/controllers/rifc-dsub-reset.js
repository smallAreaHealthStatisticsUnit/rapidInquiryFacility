/* CONTROLLER for disease submission reset model
 * 
 */
angular.module("RIF")
        .controller('ModalResetCtrl', ['$scope', '$uibModal', '$state', 'SubmissionStateService', 'StudyAreaStateService', 'CompAreaStateService', 'ParameterStateService', 'StatsStateService',
            function ($scope, $uibModal, $state, SubmissionStateService, StudyAreaStateService, CompAreaStateService, ParameterStateService, StatsStateService) {
                
                $scope.resetToDefaults = function () {
                    //reset all submission states to default
                    SubmissionStateService.resetState();
                    StudyAreaStateService.resetState();
                    CompAreaStateService.resetState();
                    ParameterStateService.resetState();
                    StatsStateService.resetState();
                    $scope.resetState();
                };

                $scope.resetState = function () {
                    //Reload submission (state1)
                    $state.go('state1').then(function () {
                        $state.reload();
                    });
                };
                        
                $scope.modalHeader = "Reset Study";
                $scope.modalBody = "Are you sure?";   
                
                $scope.open = function () {
                    var modalInstance = $uibModal.open({
                        animation: true,
                        templateUrl: 'utils/partials/rifp-util-yesno.html',
                        controller: 'ModalResetYesNoInstanceCtrl',
                        windowClass: 'stats-Modal',
                        backdrop: 'static',
                        scope: $scope,
                        keyboard: false
                    });
                };
            }])
        .controller('ModalResetYesNoInstanceCtrl', function ($scope, $uibModalInstance) {
            $scope.close = function () {
                $uibModalInstance.dismiss();
            };
            $scope.submit = function () {
                $scope.showSuccess("RIF study reset");
                $scope.resetToDefaults();
                $uibModalInstance.close();
            };
        });