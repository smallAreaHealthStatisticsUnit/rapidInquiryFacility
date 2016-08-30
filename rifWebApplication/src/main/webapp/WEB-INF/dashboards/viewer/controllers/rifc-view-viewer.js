/* global L, key, topojson, d3, ss, values */

angular.module("RIF")
        .controller('ViewerCtrl3', ['$scope',
            function ($scope) {

                //unused

            }])
        .controller('ViewerCtrl2', ['$scope', 'leafletData', 'LeafletBaseMapService', '$timeout', 'ViewerStateService', 'ChoroService',
            function ($scope, leafletData, LeafletBaseMapService, $timeout, ViewerStateService, ChoroService) {

                //ui-container sizes
                $scope.size1 = "33%";
                $scope.size2 = "66%";

                $scope.$on('ui.layout.resize', function (e, beforeContainer, afterContainer) {
                    //Monitor panel sizes
                    

                    //Rescale D3 graphs
                  
                    //Rescale leaflet container        
                    leafletData.getMap("viewermap").then(function (map) {
                        setTimeout(function () {
                            map.invalidateSize();
                        }, 50);
                    });
                });

                //leaflet render
                $scope.transparency = 0.7;
                $scope.selectedPolygon = [];
                var maxbounds;
                var thisMap = [];
                $scope.domain = [];
                var attr;

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
                        //restore setView
                        if (maxbounds && ViewerStateService.getState().zoomLevel === -1) {
                            map.fitBounds(maxbounds);
                        } else {
                            map.setView(ViewerStateService.getState().view, ViewerStateService.getState().zoomLevel);
                        }
                        //hack to refresh map
                        setTimeout(function () {
                            map.invalidateSize();
                        }, 50);
                    });
                };
                $scope.parent.renderMap("viewermap");

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
                    //refresh map with saved state
                    $scope.parent.renderMap("viewermap");
                    $scope.parent.refresh(ChoroService.getViewMap().invert, ChoroService.getViewMap().method);
                });

                //Clear all selection from map and table
                $scope.clear = function () {
                    $scope.selectedPolygon.length = 0;
                };

                //Zoom to layer
                $scope.zoomToExtent = function () {
                    leafletData.getMap("viewermap").then(function (map) {
                        map.fitBounds(maxbounds);
                    });
                };

                //UI-Grid setup options
                $scope.viewerTableOptions = {
                    enableFiltering: true,
                    enableRowSelection: true,
                    enableColumnResizing: true,
                    enableRowHeaderSelection: false,
                    enableHorizontalScrollbar: 1,
                    rowHeight: 25,
                    multiSelect: true,
                    rowTemplate: rowTemplate(),
                    onRegisterApi: function (gridApi) {
                        $scope.gridApi = gridApi;
                    }
                };
                function rowTemplate() {
                    return  '<div id="testdiv" tabindex="0" ng-keydown="grid.appScope.keyDown($event)" ng-keyup="grid.appScope.keyUp($event);">' +
                            '<div style="height: 100%" ng-class="{ ' +
                            'viewerSelected: row.entity._selected === 1' +
                            '}">' +
                            '<div ng-click="grid.appScope.rowClick(row)">' +
                            '<div ng-repeat="(colRenderIndex, col) in colContainer.renderedColumns track by col.colDef.name" class="ui-grid-cell" ui-grid-cell></div>' +
                            '</div>';
                }
                $scope.rowClick = function (row) {
                    updateSelection(row.entity);
                };

                //Render map functions
                function style(feature) {
                    return {
                        fillColor: ChoroService.getRenderFeature(feature, thisMap.scale, attr),
                        weight: 1,
                        opacity: 1,
                        color: 'gray',
                        dashArray: '3',
                        fillOpacity: $scope.transparency
                    };
                }

                function handleLayer(layer) {
                    layer.setStyle({
                        fillColor: ChoroService.getRenderFeature(layer.feature, thisMap.scale, attr),
                        fillOpacity: $scope.transparency
                    });
                }
                $scope.changeOpacity = function () {
                    $scope.topoLayer.eachLayer(handleLayer);
                };

                //Hover box and Legend
                var infoBox = L.control({position: 'bottomleft'});
                var legend = L.control({position: 'topright'});
                infoBox.onAdd = function () {
                    this._div = L.DomUtil.create('div', 'info');
                    this.update();
                    return this._div;
                };
                infoBox.update = function (poly) {
                    if (poly) {
                        this._div.innerHTML = '<h4>' + poly[attr] + '</h4>';
                    }
                };

                //information from choropleth modal to colour map                             
                $scope.parent.refresh = function () {
                    //get selected colour ramp
                    var rangeIn = ChoroService.getViewMap().brewer;
                    attr = ChoroService.getViewMap().feature;

                    //not a choropleth, but single colour
                    if (rangeIn.length === 1) {
                        attr = "";
                        leafletData.getMap("viewermap").then(function (map) {
                            //remove existing legend
                            if (legend._map) {
                                map.removeControl(legend);
                            }
                        });
                        $scope.topoLayer.eachLayer(handleLayer);
                        return;
                    }

                    thisMap = ChoroService.getViewMap().renderer;

                    //remove old legend and add new
                    legend.onAdd = ChoroService.getMakeLegend(thisMap, attr);
                    leafletData.getMap("viewermap").then(function (map) {
                        if (legend._map) { //This may break in future leaflet versions
                            map.removeControl(legend);
                        }
                        legend.addTo(map);
                    });

                    //force a redraw
                    $scope.topoLayer.eachLayer(handleLayer);
                };

                d3.json("test/za.js", function (error, data) {
                    //Fill data table   
                    var colDef = [];
                    var attrs = [];
                    for (var i in data.objects.layer1.geometries[0].properties) {
                        if (typeof (data.objects.layer1.geometries[0].properties[i]) === "number") {
                            attrs.push(i); //Numeric attributes possible to map
                        }
                        colDef.push({
                            name: i,
                            width: 100
                        });
                    }
                    ChoroService.setFeaturesToMap(attrs);
                    var tableData = [];
                    for (var i = 0; i < data.objects.layer1.geometries.length; i++) {
                        data.objects.layer1.geometries[i].properties._selected = 0;
                        tableData.push(data.objects.layer1.geometries[i].properties);
                    }
                    $scope.viewerTableOptions.columnDefs = colDef;
                    $scope.viewerTableOptions.data = tableData;

                    leafletData.getMap("viewermap").then(function (map) {
                        $scope.topoLayer = new L.TopoJSON(data, {
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
                                    infoBox.addTo(map);
                                    infoBox.update(layer.feature.properties);
                                });
                                layer.on('mouseout', function (e) {
                                    $scope.topoLayer.resetStyle(e.target);
                                    map.removeControl(infoBox);
                                });
                                layer.on('click', function (e) {
                                    updateSelection(e.target.feature.properties);
                                });
                            }
                        });
                        $scope.topoLayer.addTo(map);
                        maxbounds = $scope.topoLayer.getBounds();
                    });
                });

                //Watch selectedPolygon array for any changes
                $scope.$watchCollection('selectedPolygon', function (newNames, oldNames) {
                    if (newNames === oldNames) {
                        return;
                    }
                    //Update table selection
                    $scope.gridApi.selection.clearSelectedRows();
                    for (var i = 0; i < $scope.viewerTableOptions.data.length; i++) {
                        $scope.viewerTableOptions.data[i]._selected = 0;
                        for (var j = 0; j < $scope.selectedPolygon.length; j++) {
                            if ($scope.viewerTableOptions.data[i] === $scope.selectedPolygon[j]) {
                                $scope.viewerTableOptions.data[i]._selected = 1;
                            }
                        }
                    }
                    //Update map selection
                    $scope.topoLayer.eachLayer(handleLayer);
                });
                function updateSelection(thisPoly) {
                    var bFound = false;
                    for (var i = 0; i < $scope.selectedPolygon.length; i++) {
                        if ($scope.selectedPolygon[i] === thisPoly) {
                            bFound = true;
                            $scope.selectedPolygon.splice(i, 1);
                            break;
                        }
                    }
                    if (!bFound) {
                        $scope.selectedPolygon.push(thisPoly);
                    }
                }
            }]);