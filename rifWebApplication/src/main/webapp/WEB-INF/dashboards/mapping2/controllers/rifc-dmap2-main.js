/* global L, key, topojson, d3 */

angular.module("RIF")
        .controller('MappingCtrl', ['$scope', 'LeafletBaseMapService', 'leafletData', '$timeout', 'Mapping2StateService',
            function ($scope, LeafletBaseMapService, leafletData, $timeout, Mapping2StateService) {

                var myMaps = ["diseasemap1", "diseasemap2"];

                //ui-container sizes
                $scope.currentWidth1 = 100;
                $scope.currentHeight1 = 100;
                $scope.vSplit1 = Mapping2StateService.getState().vSplit1;
                $scope.hSplit1 = Mapping2StateService.getState().hSplit1;

                $scope.$on('ui.layout.resize', function (e, beforeContainer, afterContainer) {
                    rescaleLeafletContainer();
                });

                //Rescale leaflet container       
                function rescaleLeafletContainer() {
                    for (var i in myMaps) {
                        leafletData.getMap(myMaps[i]).then(function (map) {
                            setTimeout(function () {
                                map.invalidateSize();
                            }, 50);
                        });
                    }
                }

                $scope.bLockCenters = false;
                $scope.lockExtent = function () {
                    if ($scope.bLockCenters) {
                        $scope.bLockCenters = false;
                        $scope.center2 = angular.copy($scope.center1);
                    } else {
                        $scope.bLockCenters = true;
                        $scope.center2 = $scope.center1;
                    }
                };

                $scope.center1 = {
                    lat: 43.7350,
                    lng: -79.3734,
                    zoom: 11
                };
                $scope.lockExtent();






                //get the user defined basemap
                $scope.parent = {};
                $scope.thisLayer = {
                    diseasemap1: LeafletBaseMapService.setBaseMap(LeafletBaseMapService.getCurrentBaseMapInUse("diseasemap1")),
                    diseasemap2: LeafletBaseMapService.setBaseMap(LeafletBaseMapService.getCurrentBaseMapInUse("diseasemap2"))
                };

                //called on bootstrap and on modal submit
                $scope.parent.renderMap = function (mapID) {
                    leafletData.getMap(mapID).then(function (map) {
                        map.eachLayer(function (layer) {
                            map.removeLayer(layer);
                        });
                        if (!LeafletBaseMapService.getNoBaseMap(mapID)) {
                            $scope.thisLayer[mapID] = LeafletBaseMapService.setBaseMap(LeafletBaseMapService.getCurrentBaseMapInUse(mapID));
                            map.addLayer($scope.thisLayer[mapID]);
                        }
                        setTimeout(function () {
                            map.invalidateSize();
                        }, 50);
                    });
                };

                $timeout(function () {
                    leafletData.getMap("diseasemap1").then(function (map) {
                        new L.Control.GeoSearch({
                            provider: new L.GeoSearch.Provider.OpenStreetMap()
                        }).addTo(map);
                        map.on('zoomend', function (e) {

                        });
                        map.on('moveend', function (e) {

                        });
                    });
                });






            }]);

            