/* global L */

angular.module("RIF")
        .controller('DiseaseMappingCtrl2', ['$scope',
            function ($scope) {

                $scope.myData = [10, 25, 60, 40, 70]; //TEST DATA FOR d3

            }])
        .controller('DiseaseMappingCtrl', ['$scope', 'leafletData', 'LeafletBaseMapService', '$timeout', 'MappingStateService',
            function ($scope, leafletData, LeafletBaseMapService, $timeout, MappingStateService) {
                $timeout(function () {
                    leafletData.getMap("diseasemap").then(function (map) {
                        map.on('zoomend', function (e) {
                            MappingStateService.getState().zoomLevel = map.getZoom();
                        });
                        map.on('moveend', function (e) {
                            MappingStateService.getState().view = map.getCenter();
                        });
                        new L.Control.GeoSearch({
                            provider: new L.GeoSearch.Provider.OpenStreetMap()
                        }).addTo(map);
                    });
                    $scope.parent.renderMap("diseasemap");
                });

                //get the user defined basemap
                $scope.parent = {};
                $scope.parent.thisLayer = LeafletBaseMapService.setBaseMap(LeafletBaseMapService.getCurrentBase());
                //called on bootstrap and on modal submit
                //REFACTOR - OR UNIQUE TO SPECIFIC MAP IDS?
                $scope.parent.renderMap = function (mapID) {
                    leafletData.getMap(mapID).then(function (map) {
                        map.removeLayer($scope.parent.thisLayer);
                        if (!LeafletBaseMapService.getNoBaseMap()) {
                            $scope.parent.thisLayer = LeafletBaseMapService.setBaseMap(LeafletBaseMapService.getCurrentBase());
                            map.addLayer($scope.parent.thisLayer);
                        }
                        //restore setView
                        map.setView(MappingStateService.getState().view, MappingStateService.getState().zoomLevel);
                        //hack to refresh map
                        setTimeout(function () {
                            map.invalidateSize();
                        }, 50);
                    });
                };
                $scope.parent.renderMap("diseasemap");
            }]);


        