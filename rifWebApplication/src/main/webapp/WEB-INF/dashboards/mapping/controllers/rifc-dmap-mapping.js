/* global L, d3 */

angular.module("RIF")

        .controller('DiseaseMappingCtrl2', ['$scope',
            function ($scope) {


                //TEST DATA FOR d3
                $scope.myData = [10, 25, 60, 40, 70];
                $scope.myData2 = [10, 25, 60, 40, 70, 80];
                $scope.rrTestData = [];
                for (var i = 0, t = 400; i < t; i++) {
                    $scope.rrTestData.push(Math.round(Math.random() * 2));
                }

            }])
        .controller('DiseaseMappingCtrl', ['$scope', 'leafletData', 'LeafletBaseMapService', '$timeout', 'MappingStateService', 'D3RRService', 'user',
            function ($scope, leafletData, LeafletBaseMapService, $timeout, MappingStateService, D3RRService, user) {

                $scope.rrCurrentWidth = d3.select("#rr").node().getBoundingClientRect().width;
                $scope.rrCurrentHeight = d3.select("#rr").node().getBoundingClientRect().height;

                $scope.$on('ui.layout.resize', function (e) {

                    //Stop svg flicker as event fires even when bbox not resized
                    if ($scope.rrCurrentWidth === d3.select("#rr").node().getBoundingClientRect().width &
                            $scope.rrCurrentHeight === d3.select("#rr").node().getBoundingClientRect().height) {
                        return;
                    }

                    //Rescale D3 graphs
                    D3RRService.getPlot(d3.select("#rr").node().getBoundingClientRect().width,
                            d3.select("#rr").node().getBoundingClientRect().height, "SOME DATA", '#rr');

                    $scope.rrCurrentWidth = d3.select("#rr").node().getBoundingClientRect().width;
                    $scope.rrCurrentHeight = d3.select("#rr").node().getBoundingClientRect().height;

                    //Rescale leaflet container        
                    leafletData.getMap("diseasemap").then(function (map) {
                        setTimeout(function () {
                            map.invalidateSize();
                        }, 50);
                    });
                });

                $scope.transparency = 0.7;
                var maxbounds;

                //get the user defined basemap
                $scope.parent = {};
                $scope.parent.thisLayer = LeafletBaseMapService.setBaseMap(LeafletBaseMapService.getCurrentBase());
                //called on bootstrap and on modal submit
                $scope.parent.renderMap = function (mapID) {
                    leafletData.getMap(mapID).then(function (map) {
                        map.removeLayer($scope.parent.thisLayer);
                        if (!LeafletBaseMapService.getNoBaseMap()) {
                            $scope.parent.thisLayer = LeafletBaseMapService.setBaseMap(LeafletBaseMapService.getCurrentBase());
                            map.addLayer($scope.parent.thisLayer);
                        }
                        /*
                         //restore setView
                         if (maxbounds && ViewerStateService.getState().zoomLevel === -1) {
                         map.fitBounds(maxbounds);
                         } else {
                         map.setView(ViewerStateService.getState().view, ViewerStateService.getState().zoomLevel);
                         }*/
                        //hack to refresh map
                        setTimeout(function () {
                            map.invalidateSize();
                        }, 50);
                    });
                };
                $scope.parent.renderMap("diseasemap");

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

                function style(feature) {
                    return {
                        fillColor: 'red',
                        weight: 1,
                        opacity: 1,
                        color: 'gray',
                        dashArray: '3',
                        fillOpacity: $scope.transparency
                    };
                }

                function handleLayer(layer) {
                    layer.setStyle({
                        fillOpacity: $scope.transparency
                    });
                }
                $scope.changeOpacity = function () {
                    $scope.topoLayer.eachLayer(handleLayer);
                };

                //Zoom to layer
                $scope.zoomToExtent = function () {
                    leafletData.getMap("diseasemap").then(function (map) {
                        map.fitBounds(maxbounds);
                    });
                };

                user.getTiles(user.currentUser, "SAHSU", "LEVEL4").then(handleTopoJSON, handleTopoJSON);

                function handleTopoJSON(res) {
                    leafletData.getMap("diseasemap").then(function (map) {
                        $scope.topoLayer = new L.TopoJSON(res.data, {
                            style: style,
                            onEachFeature: function (feature, layer) {
                                layer.on('mouseover', function (e) {
                                    this.setStyle({
                                        color: 'gray',
                                        dashArray: 'none',
                                        weight: 1.5,
                                        fillOpacity: function () {
                                            //set tranparency from slider
                                            return($scope.transparency - 0.3 > 0 ? $scope.transparency - 0.3 : 0.1);
                                        }()
                                    });
                                });
                                layer.on('mouseout', function (e) {
                                    $scope.topoLayer.resetStyle(e.target);
                                });
                            }
                        });
                        $scope.topoLayer.addTo(map);
                        maxbounds = $scope.topoLayer.getBounds();
                    });
                }



            }]);


        