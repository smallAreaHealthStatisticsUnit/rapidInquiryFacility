
angular.module("RIF")
        .controller('ChoroplethModalCtrl', ['$scope', '$uibModal', 'ColorBrewerService',
            function ($scope, $uibModal, ColorBrewerService) {
                //get list of available colour schemes for drop-down and defaults
                $scope.checkboxInvert = false;
                $scope.mySchemes = ColorBrewerService.get_schemeList();
                $scope.intervalRange = ColorBrewerService.get_schemeIntervals('YlGn');
                $scope.selectedSchemeName = ColorBrewerService.get_selectedScheme();
                $scope.selectedN = ColorBrewerService.get_selectedN();

                //list of html colours to use
                $scope.selectedScheme = ColorBrewerService.get_colorbrewer($scope.selectedSchemeName, $scope.selectedN);

                //update preview swatch in modal
                $scope.renderSwatch = function () {
                    $scope.intervalRange = ColorBrewerService.get_schemeIntervals($scope.selectedSchemeName);
                    //ensure that the colour scheme allows the selected number of classes
                    if ($scope.selectedN > Math.max.apply(Math, $scope.intervalRange)) {
                        $scope.selectedN = Math.max.apply(Math, $scope.intervalRange);
                    } else if ($scope.selectedN < Math.min.apply(Math, $scope.intervalRange)) {
                        $scope.selectedN = Math.min.apply(Math, $scope.intervalRange);
                    }
                    $scope.selectedScheme = ColorBrewerService.get_colorbrewer($scope.selectedSchemeName, $scope.selectedN);
                    if ($scope.checkboxInvert) {
                        $scope.selectedScheme.reverse();
                    }
                };
                
                //checkbox change event
                $scope.invertColours = function () {
                    $scope.selectedScheme.reverse();
                };

                $scope.open = function () {
                    var modalInstance = $uibModal.open({
                        animation: true,
                        templateUrl: 'utils/partials/rifp-util-choro.html',
                        controller: 'ChoroplethModalInstanceCtrl',
                        windowClass: 'mapping-Modal'
                    });
                    modalInstance.result.then(function () {
                        //TODO: save state

                    });
                };
            }])
        .controller('ChoroplethModalInstanceCtrl', function ($scope, $uibModalInstance) {
            $scope.close = function () {
                $uibModalInstance.dismiss();
            };
            $scope.apply = function () {
                $uibModalInstance.close();
            };
        });

