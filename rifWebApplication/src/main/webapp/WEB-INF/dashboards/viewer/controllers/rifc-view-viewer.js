/* global L, key, topojson, d3 */

angular.module("RIF")
        .controller('ViewerCtrl2', ['$scope', 'leafletData', 'LeafletBaseMapService',
            function ($scope, leafletData, LeafletBaseMapService) {



            }])
        .controller('ViewerCtrl', ['$scope', 'leafletData', 'LeafletBaseMapService', '$timeout', 'ViewerStateService',
            function ($scope, leafletData, LeafletBaseMapService, $timeout, ViewerStateService) {
                $timeout(function () {
                    leafletData.getMap("viewermap").then(function (map) {
                        map.on('zoomend', function (e) {
                            ViewerStateService.getState().zoomLevel = map.getZoom();
                        });
                        map.on('moveend', function (e) {
                            ViewerStateService.getState().view = map.getCenter();
                        });
                        new L.Control.GeoSearch({
                            provider: new L.GeoSearch.Provider.OpenStreetMap()
                        }).addTo(map);
                    });
                    $scope.parent.renderMap("viewermap");
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
                        map.setView(ViewerStateService.getState().view, ViewerStateService.getState().zoomLevel);
                        //hack to refresh map
                        setTimeout(function () {
                            map.invalidateSize();
                        }, 50);
                    });
                };
                $scope.parent.renderMap("viewermap");
            }]);