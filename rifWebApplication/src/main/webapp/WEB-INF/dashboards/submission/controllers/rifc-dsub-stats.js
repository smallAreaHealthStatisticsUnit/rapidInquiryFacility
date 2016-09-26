/* 
 * CONTROLLER for disease submission stats options modal
 */
angular.module("RIF")
        .controller('ModalStatsCtrl', ['$scope', '$uibModal', 'StatsStateService', 'SubmissionStateService', 'user',
            function ($scope, $uibModal, StatsStateService, SubmissionStateService, user) {
                $scope.tree = SubmissionStateService.getState().statsTree;

                //get available methods
                user.getAvailableCalculationMethods(user.currentUser).then(handleAvailableCalculationMethods, handleAvailableCalculationMethods);

                function handleAvailableCalculationMethods(res) {
                    try {
                        $scope.methods = res.data;

                        //fill default state on form first load              
                        if (StatsStateService.getState().model.length === 0) {
                            var myModel = [];
                            for (var i = 0; i < res.data.length; i++) {
                                var params = [];
                                for (var j = 0; j < res.data[i].parameterProxies.length; j++) {
                                    params.push(Number(res.data[i].parameterProxies[j].value));
                                }
                                myModel.push(params);
                            }
                            StatsStateService.getState().model = myModel; //default parameters
                            StatsStateService.getState().methods = res.data;
                        }
                    } catch (e) {
                        $scope.showError("Could not retrieve statistical methods");
                    }
                }

                $scope.openStatsManual = function () {
                    alert("This will open the stats manual one day");
                };

                $scope.open = function () {
                    var modalInstance = $uibModal.open({
                        animation: true,
                        templateUrl: 'dashboards/submission/partials/rifp-dsub-stats.html',
                        controller: 'ModalStatsInstanceCtrl',
                        windowClass: 'stats-Modal',
                        backdrop: 'static',
                        scope: $scope,
                        keyboard: false
                    });
                    modalInstance.result.then(function (input) {
                        if (input.checked >= -1) {
                            SubmissionStateService.getState().statsTree = true;
                            $scope.tree = true;
                        } else {
                            SubmissionStateService.getState().statsTree = false;
                            $scope.tree = false;
                        }
                        StatsStateService.getState().checked = input.checked;
                        StatsStateService.getState().model = input.model;
                    });
                };
            }])
        .controller('ModalStatsInstanceCtrl', function ($scope, $uibModalInstance, StatsStateService) {
            $scope.input = {};
            $scope.input.checked = StatsStateService.getState().checked;
            $scope.input.model = StatsStateService.getState().model;

            //reset to opening instance on dismiss
            var modelOnOpening = angular.copy($scope.input.model);
            var checkedOnOpening = angular.copy($scope.input.checked);
            $scope.close = function () {
                StatsStateService.getState().checked = checkedOnOpening;
                StatsStateService.getState().model = modelOnOpening;
                $uibModalInstance.dismiss();
            };

            $scope.submit = function () {
                //check numeric
                for (var i = 0; i < $scope.input.model.length; i++) {
                    for (var j = 0; j < $scope.input.model[i].length; j++) {
                        if ($scope.input.model[i][j] === null | isNaN(Number($scope.input.model[i][j]))) {
                            $scope.showError("Non-numeric parameter value provided");
                            return;
                        }
                    }
                }
                $uibModalInstance.close($scope.input);
            };
        });