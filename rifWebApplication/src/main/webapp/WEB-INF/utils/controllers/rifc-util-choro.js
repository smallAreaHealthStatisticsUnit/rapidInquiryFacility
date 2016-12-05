/*
 * CONTROLLER for choropleth map symbology modal used by viewer and mapper
 */
/* global d3 */

angular.module("RIF")
        .controller('ChoroplethModalCtrl', ['$scope', '$uibModal', 'ChoroService', 'ColorBrewerService',
            function ($scope, $uibModal, ChoroService, ColorBrewerService) {

                $scope.open = function (map) {
                    $scope.map = map;
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
                        ChoroService.getMaps(map).brewerName = modal.selectedSchemeName;
                        ChoroService.getMaps(map).invert = modal.checkboxInvert;
                        ChoroService.getMaps(map).brewer = ColorBrewerService.getColorbrewer(modal.selectedSchemeName, modal.selectedN);
                        ChoroService.getMaps(map).intervals = modal.selectedN;
                        ChoroService.getMaps(map).feature = modal.selectedFeature;
                        ChoroService.getMaps(map).method = modal.method;
                        ChoroService.getMaps(map).renderer = modal.thisMap;
                        $scope.refresh(ChoroService.getMaps(map).map);
                    });
                };
            }])
        .controller('ChoroplethModalInstanceCtrl', function ($scope, $uibModalInstance, ColorBrewerService, ChoroService) {
            //get list of available colour schemes for drop-down and defaults
            $scope.input = {};
            $scope.input.mySchemes = ColorBrewerService.getSchemeList();
            $scope.input.checkboxInvert = ChoroService.getMaps($scope.map).invert;
            $scope.input.selectedSchemeName = ChoroService.getMaps($scope.map).brewerName;
            $scope.input.intervalRange = ColorBrewerService.getSchemeIntervals($scope.input.selectedSchemeName);
            $scope.input.selectedN = ChoroService.getMaps($scope.map).intervals;
            $scope.input.method = ChoroService.getMaps($scope.map).method;

            //list of attributes
            $scope.input.features = ChoroService.getMaps($scope.map).features;
            if ($scope.input.features.indexOf(ChoroService.getMaps($scope.map).feature) === -1) {
                $scope.input.selectedFeature = $scope.input.features[0];
            } else {
                $scope.input.selectedFeature = ChoroService.getMaps($scope.map).feature;
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

                var thisLeaflet = ChoroService.getMaps($scope.map).map;

                //get the domain 
                $scope.domain.length = 0;
                for (var i = 0; i < $scope.tableData[thisLeaflet].length; i++) {
                    $scope.domain.push(Number($scope.tableData[thisLeaflet][i][$scope.input.selectedFeature]));
                }

                ChoroService.getMaps($scope.map).brewerName = $scope.input.selectedSchemeName;
                $scope.input.thisMap = ChoroService.getChoroScale($scope.input.method, $scope.domain, ColorBrewerService.getColorbrewer($scope.input.selectedSchemeName,
                        $scope.input.selectedN), $scope.input.checkboxInvert, $scope.map);
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

