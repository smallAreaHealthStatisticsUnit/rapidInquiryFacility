/*
 * CONTROLLER
 */
angular.module("RIF")
        .controller('ChoroplethModalCtrl', ['$scope', '$uibModal', 'ChoroService', 'ColorBrewerService',
            function ($scope, $uibModal, ChoroService, ColorBrewerService) {
                $scope.open = function () {
                    var modalInstance = $uibModal.open({
                        animation: true,
                        templateUrl: 'utils/partials/rifp-util-choro.html',
                        controller: 'ChoroplethModalInstanceCtrl',
                        windowClass: 'mapping-Modal'
                    });
                    modalInstance.result.then(function (modal) {
                        ChoroService.getViewMap().brewerName = modal.selectedSchemeName;
                        ChoroService.getViewMap().invert = modal.checkboxInvert;
                        ChoroService.getViewMap().brewer = ColorBrewerService.getColorbrewer(modal.selectedSchemeName, modal.selectedN);
                        ChoroService.getViewMap().intervals = modal.selectedN;
                        ChoroService.getViewMap().feature = modal.selectedFeature;
                        ChoroService.getViewMap().method = modal.method;

                        $scope.parent.refresh(modal.checkboxInvert, modal.method);
                    });
                };
            }])
        .controller('ChoroplethModalInstanceCtrl', function ($scope, $uibModalInstance, ColorBrewerService, ChoroService) {
            //get list of available colour schemes for drop-down and defaults
            $scope.input = {};
            $scope.input.mySchemes = ColorBrewerService.getSchemeList();
            $scope.input.checkboxInvert = ChoroService.getViewMap().invert;
            $scope.input.selectedSchemeName = ChoroService.getViewMap().brewerName;
            $scope.input.intervalRange = ColorBrewerService.getSchemeIntervals($scope.input.selectedSchemeName);
            $scope.input.selectedN = ChoroService.getViewMap().intervals;
            $scope.input.method = ChoroService.getViewMap().method;

            //list of attributes
            $scope.input.features = ChoroService.getFeaturesToMap();
            if ($scope.input.features.indexOf(ChoroService.getViewMap().feature) === -1) {
                $scope.input.selectedFeature = $scope.input.features[0];
            } else {
                $scope.input.selectedFeature = ChoroService.getViewMap().feature;
            }

            //ensure that the colour scheme allows the selected number of classes
            $scope.renderSwatch = function () {
                $scope.input.intervalRange = ColorBrewerService.getSchemeIntervals($scope.input.selectedSchemeName);
                if ($scope.input.selectedN > Math.max.apply(Math, $scope.input.intervalRange)) {
                    $scope.input.selectedN = Math.max.apply(Math, $scope.input.intervalRange);
                } else if ($scope.input.selectedN < Math.min.apply(Math, $scope.input.intervalRange)) {
                    $scope.input.selectedN = Math.min.apply(Math, $scope.input.intervalRange);
                }
            };

            $scope.close = function () {
                $uibModalInstance.dismiss();
            };
            $scope.apply = function () {
                $uibModalInstance.close($scope.input);
            };
        });

