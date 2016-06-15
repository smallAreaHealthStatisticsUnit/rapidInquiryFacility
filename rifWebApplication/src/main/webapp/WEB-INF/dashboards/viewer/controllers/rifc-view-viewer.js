/* global L, key, topojson, d3 */

angular.module("RIF")
        .controller('ViewerCtrl2', ['$scope', 'leafletData', 'LeafletBaseMapService',
            function ($scope, leafletData, LeafletBaseMapService) {



            }])
        .controller('ViewerCtrl', ['$scope', 'leafletData', 'LeafletBaseMapService', '$timeout',
            function ($scope, leafletData, LeafletBaseMapService, $timeout) {
                $timeout(function () {
                    leafletData.getMap("viewermap").then(function (map) {
                        LeafletBaseMapService.set_currentZoomLevel(map.getZoom());
                        LeafletBaseMapService.set_currentCentre(map.getCenter());
                        new L.Control.GeoSearch({
                            provider: new L.GeoSearch.Provider.OpenStreetMap()
                        }).addTo(map);
                    });
                    $scope.parent.renderMap("viewermap");
                });
                
                //get the user defined basemap
                $scope.parent = {};
                $scope.parent.thisLayer = LeafletBaseMapService.set_baseMap(LeafletBaseMapService.get_currentBase());
                //called on bootstrap and on modal submit
                //REFACTOR - OR UNIQUE TO SPECIFIC MAP IDS?
                $scope.parent.renderMap = function (mapID) {
                    leafletData.getMap(mapID).then(function (map) {
                        map.removeLayer($scope.parent.thisLayer);
                        if (!LeafletBaseMapService.get_noBaseMap()) {
                            $scope.parent.thisLayer = LeafletBaseMapService.set_baseMap(LeafletBaseMapService.get_currentBase());
                            map.addLayer($scope.parent.thisLayer);
                        }
                        //restore setView
                        map.setView(LeafletBaseMapService.get_currentCentre(), LeafletBaseMapService.get_currentZoomLevel());
                        //hack to refresh map
                        setTimeout(function () {
                            map.invalidateSize();
                        }, 50);
                    });
                };
                $scope.parent.renderMap("viewermap");
            }]);