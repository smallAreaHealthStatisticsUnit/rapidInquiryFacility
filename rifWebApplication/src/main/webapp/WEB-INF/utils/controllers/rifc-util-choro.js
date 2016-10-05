/*
 * CONTROLLER for choropleth map symbology modal used by viewer and mapper
 */
/* global d3 */

angular.module("RIF")
        .controller('ChoroplethModalCtrl', ['$scope', '$uibModal', 'ChoroService', 'ColorBrewerService',
            function ($scope, $uibModal, ChoroService, ColorBrewerService) {

                $scope.open = function () {
                    var modalInstance = $uibModal.open({
                        animation: true,
                        templateUrl: 'utils/partials/rifp-util-choro.html',
                        controller: 'ChoroplethModalInstanceCtrl',
                        windowClass: 'mapping-Modal',
                        scope: $scope
                    });
                    modalInstance.opened.then(function () {
                        $scope.$$childHead.renderSwatch();
                    });
                    modalInstance.result.then(function (modal) {
                        ChoroService.getViewMap().brewerName = modal.selectedSchemeName;
                        ChoroService.getViewMap().invert = modal.checkboxInvert;
                        ChoroService.getViewMap().brewer = ColorBrewerService.getColorbrewer(modal.selectedSchemeName, modal.selectedN);
                        ChoroService.getViewMap().intervals = modal.selectedN;
                        ChoroService.getViewMap().feature = modal.selectedFeature;
                        ChoroService.getViewMap().method = modal.method;
                        ChoroService.getViewMap().renderer = modal.thisMap;

                        $scope.parent.refresh();
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

            $scope.domain = [];

            //ensure that the colour scheme allows the selected number of classes
            $scope.renderSwatch = function () {
                $scope.input.intervalRange = ColorBrewerService.getSchemeIntervals($scope.input.selectedSchemeName);
                if ($scope.input.selectedN > Math.max.apply(Math, $scope.input.intervalRange)) {
                    $scope.input.selectedN = Math.max.apply(Math, $scope.input.intervalRange);
                } else if ($scope.input.selectedN < Math.min.apply(Math, $scope.input.intervalRange)) {
                    $scope.input.selectedN = Math.min.apply(Math, $scope.input.intervalRange);
                }

                //get the domain 
                $scope.domain.length = 0;
                var set = 0;
                for (var i = 0; $scope.$parent.rrTestData.length; i++) {
                    if ($scope.$parent.rrTestData[i][0].name === $scope.input.selectedFeature) {
                        set = i;
                        break;
                    }
                }
                for (var i = 0; i < $scope.$parent.rrTestData[0].length; i++) {
                    $scope.domain.push($scope.$parent.rrTestData[set][i].srr);
                }

                //get the breaks
                $scope.input.thisMap = ChoroService.getChoroScale($scope.input.method, $scope.domain, ColorBrewerService.getColorbrewer($scope.input.selectedSchemeName,
                        $scope.input.selectedN), $scope.input.checkboxInvert);
            };

            $scope.close = function () {
                $uibModalInstance.dismiss();
            };

            $scope.apply = function () {
                //check breaks are numeric               
                for (var i = 0; i < $scope.input.thisMap.breaks.length; i++) {
                    var thisBreak = Number($scope.input.thisMap.breaks[i]);
                    if (!isNaN(thisBreak)) {
                        $scope.input.thisMap.breaks[i] = thisBreak;
                    } else {
                        $scope.showWarning("Non-numeric break value entered");
                        return;
                    }
                }

                //check breaks are sequential
                var tmp = angular.copy($scope.input.thisMap.breaks);
                tmp.push($scope.input.thisMap.mx);
                tmp.unshift($scope.input.thisMap.mn);
                for (var i = 0; i < tmp.length - 1; i++) {
                    if (tmp[i] > tmp[i + 1]) {
                        $scope.showWarning("Breaks are not in ascending order");
                        return;
                    }
                }
                $uibModalInstance.close($scope.input);
            };
        });

