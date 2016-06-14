/*
 * DIRECTIVE for map and table linked area selections
 * Used in Study Area and Comparison Area Modals
 * 
 * Map and table selections are synchronised by updating the array 'selectedPolygon'
 * 'selectedPolygon' is updated using click events on the map and table
 * captured clicks do not lead directly to an update of the map or table rather
 * 'selectedPolygon' is monitored using a $watchCollection which renders the selections on changes
 * 
 * TODO: restrict scope
 */

/* global L, d3, key, topojson */
angular.module("RIF")
        .directive('submissionMapTable', ['leafletData', 'ModalAreaService', 'LeafletDrawService', 'GISService', 'LeafletBaseMapService', '$timeout',
            function (leafletData, ModalAreaService, LeafletDrawService, GISService, LeafletBaseMapService, $timeout) {
                return {
                    templateUrl: 'submission/partials/rifp-dsub-maptable.html',
                    restrict: 'AE',
                    link: function ($scope) {

                        //Called on DOM render completion to ensure basemap is rendered
                        $timeout(function () {
                            leafletData.getMap("area").then(function (map) {
                                LeafletBaseMapService.set_currentZoomLevel(map.getZoom());
                                LeafletBaseMapService.set_currentCentre(map.getCenter());
                            });
                            $scope.parent.renderMap("area");
                        });

                        //map max bounds from topojson layer
                        var maxbounds;

                        //selectedPolygon array synchronises the map <-> table selections
                        $scope.selectedPolygon = [];
                        $scope.selectedPolygonCount = 0; //total for display

                        //band colour look-up for selected districts
                        $scope.possibleBands = [1, 2, 3, 4, 5, 6, 7, 8, 9];
                        $scope.currentBand = 1; //from dropdown

                        //keeps track of which polygon is colured as what colour
                        var bandColours = {};

                        //d3 polygon rendering, changed by slider
                        $scope.transparency = 0.7;

                        //district centres for rubberband selection
                        var latlngList = [];
                        var centroidMarkers = new L.layerGroup();
                        var bDrawing = false;
                        //Set up table (UI-grid)
                        $scope.gridOptions = ModalAreaService.getAreaTableOptions();
                        $scope.gridOptions.columnDefs = ModalAreaService.getAreaTableColumnDefs();
                        //Enable row selections
                        $scope.gridOptions.onRegisterApi = function (gridApi) {
                            $scope.gridApi = gridApi;
                        };

                        //Set the user defined basemap
                        $scope.parent = {};
                        $scope.parent.thisLayer = LeafletBaseMapService.set_baseMap(LeafletBaseMapService.get_currentBase());

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
                        $scope.parent.renderMap("area");

                        //Add Leaflet.Draw capabilities
                        var drawnItems;
                        LeafletDrawService.get_CircleCapability();
                        LeafletDrawService.get_PolygonCapability();

                        //Add Leaflet.Draw toolbar
                        leafletData.getMap("area").then(function (map) {
                            L.drawLocal.draw.toolbar.buttons.circle = "Select by concentric bands";
                            L.drawLocal.draw.toolbar.buttons.polygon = "Select by freehand polygons";
                            drawnItems = new L.FeatureGroup();
                            map.addLayer(drawnItems);
                            var drawControl = new L.Control.Draw({
                                draw: {
                                    polygon: {
                                        shapeOptions: {
                                            color: '#0099cc',
                                            weight: 4,
                                            opacity: 1,
                                            fillOpacity: 0.2
                                        }
                                    },
                                    marker: false,
                                    polyline: false,
                                    rectangle: false
                                },
                                edit: {
                                    remove: false,
                                    edit: false,
                                    featureGroup: drawnItems
                                }
                            });
                            map.addControl(drawControl);
                            new L.Control.GeoSearch({
                                provider: new L.GeoSearch.Provider.OpenStreetMap()
                            }).addTo(map);
                            //add the circle to the map
                            map.on('draw:created', function (e) {
                                drawnItems.addLayer(e.layer);
                            });
                            //override other map mouse events
                            map.on('draw:drawstart', function (e) {
                                bDrawing = true;
                            });
                        });

                        //selection event fired from service
                        $scope.$on('makeDrawSelection', function (event, data) {
                            makeDrawSelection(data);
                        });
                        function makeDrawSelection(shape) {
                            latlngList.forEach(function (point) {
                                //is point in defined polygon?
                                var test;
                                if (shape.circle) {
                                    test = GISService.get_pointincircle(point[0], shape);
                                } else {
                                    test = GISService.get_pointinpolygon(point[0], shape);
                                }
                                if (test) {
                                    var thisIndex = $scope.selectedPolygon.indexOf(point[1]);
                                    if (thisIndex === -1) {
                                        //add district to selection
                                        $scope.selectedPolygon.push(point[1]);
                                        //add band number to array look-up
                                        if (shape.band === -1) {
                                            bandColours[point[1]] = $scope.currentBand;
                                        } else {
                                            bandColours[point[1]] = shape.band;
                                        }
                                    }
                                }
                            });
                            if (!shape.circle) {
                                removeMapDrawItems();
                                //auto increase band dropdown
                                if ($scope.currentBand < 9) {
                                    $scope.currentBand++;
                                }
                            }
                        }
                        //remove drawn items event fired from service
                        $scope.$on('removeDrawnItems', function (event, data) {
                            removeMapDrawItems();
                        });

                        function removeMapDrawItems() {
                            drawnItems.clearLayers();
                            bDrawing = false; //re-enable layer events
                        }

                        //Functions to style topoJson on selection changes
                        function style(feature) {
                            return {
                                fillColor: ModalAreaService.getColor($scope.selectedPolygon.indexOf(feature.properties.LAD13NM),
                                        bandColours[feature.properties.LAD13NM]),
                                weight: 1,
                                opacity: 1,
                                color: 'gray',
                                dashArray: '3',
                                fillOpacity: $scope.transparency
                            };
                        }
                        function handleLayer(layer) {
                            layer.setStyle({
                                fillColor: ModalAreaService.getColor($scope.selectedPolygon.indexOf(layer.feature.properties.LAD13NM),
                                        bandColours[layer.feature.properties.LAD13NM]),
                                fillOpacity: $scope.transparency
                            });
                        }
                        $scope.changeOpacity = function () {
                            $scope.topoLayer.eachLayer(handleLayer);
                        };

                        //Read topoJson with d3
                        d3.json("eng.json", function (error, data) {
                            //populate the table
                            $scope.gridOptions.data = ModalAreaService.fillTable(data);
                            $scope.refresh = false;
                            leafletData.getMap("area").then(function (map) {
                                latlngList = [];
                                centroidMarkers = new L.layerGroup();

                                $scope.topoLayer = new L.TopoJSON(data, {
                                    style: style,
                                    onEachFeature: function (feature, layer) {
                                        //define polygon centroids
                                        var p = layer.getBounds().getCenter();
                                        latlngList.push([L.latLng([p.lat, p.lng]), feature.properties.LAD13NM]);

                                        //get as optional marker layer
                                        var circle = new L.CircleMarker([p.lat, p.lng], {
                                            radius: 2,
                                            fillColor: "red",
                                            color: "#000",
                                            weight: 1,
                                            opacity: 1,
                                            fillOpacity: 0.8
                                        });
                                        centroidMarkers.addLayer(circle);

                                        //define initial bands
                                        bandColours[feature.properties.LAD13NM] = 0;
                                        layer.on('mouseover', function (e) {
                                            //if drawing then return
                                            if (bDrawing) {
                                                return;
                                            }
                                            this.setStyle({
                                                color: 'gray',
                                                dashArray: 'none',
                                                weight: 1.5,
                                                fillOpacity: function () {
                                                    //set tranparency from slider
                                                    return($scope.transparency - 0.3 > 0 ? $scope.transparency - 0.3 : 0.1);
                                                }()
                                            });
                                            $scope.thisPolygon = feature.properties.LAD13NM;
                                        });
                                        layer.on('mouseout', function (e) {
                                            $scope.topoLayer.resetStyle(e.target);
                                            $scope.thisPolygon = "";
                                        });
                                        layer.on('click', function (e) {
                                            //if drawing then return
                                            if (bDrawing) {
                                                return;
                                            }
                                            var thisPoly = e.target.feature.properties.LAD13NM;
                                            var thisIndex = $scope.selectedPolygon.indexOf(thisPoly);
                                            if (thisIndex === -1) {
                                                bandColours[feature.properties.LAD13NM] = $scope.currentBand;
                                                $scope.selectedPolygon.push(feature.properties.LAD13NM);
                                            } else {
                                                bandColours[feature.properties.LAD13NM] = 0;
                                                $scope.selectedPolygon.splice(thisIndex, 1);
                                            }
                                        });
                                    }
                                });
                                $scope.topoLayer.addTo(map);
                                maxbounds = $scope.topoLayer.getBounds();
                                $scope.totalPolygonCount = latlngList.length;
                                map.fitBounds(maxbounds);
                            });
                        });
                        //Multiple select with shift
                        //detect shift key (16) down
                        var bShift = false;
                        var multiStart = -1;
                        var multiStop = -1;
                        $scope.keyDown = function ($event) {
                            if (!bShift && $event.keyCode === 16) {
                                bShift = true;
                            }
                        };
                        //detect shift key (16) up
                        $scope.keyUp = function ($event) {
                            if (bShift && $event.keyCode === 16) {
                                bShift = false;
                                multiStart = -1;
                                multiStop = -1;
                            }
                        };
                        //Table click event to update selectedPolygon 
                        $scope.rowClick = function (row) {
                            //We are doing a single click select on the table
                            var thisIndex = $scope.selectedPolygon.indexOf(row.entity.name);
                            if (thisIndex === -1) {
                                bandColours[row.entity.name] = $scope.currentBand;
                                $scope.selectedPolygon.push(row.entity.name);
                            } else {
                                bandColours[row.entity.name] = 0;
                                $scope.selectedPolygon.splice(thisIndex, 1);
                            }

                            //We are doing a multiple select on the table, shift key is down
                            if (bShift) {
                                //get array of rows in filtered order
                                var myVisibleRows = $scope.gridApi.core.getVisibleRows();
                                if (multiStart === -1) {
                                    //get index of start multi select in the filtered table
                                    multiStart = ModalAreaService.matchRowNumber(myVisibleRows, row.entity.name);
                                } else {
                                    //get index of end multi select in the filtered table
                                    multiStop = ModalAreaService.matchRowNumber(myVisibleRows, row.entity.name);
                                    //do the multiple selection
                                    for (var i = Math.min(multiStop, multiStart);
                                            i <= Math.min(multiStop, multiStart) + (Math.abs(multiStop - multiStart)); i++) {
                                        var thisIndex = $scope.selectedPolygon.indexOf(myVisibleRows[i].entity.name);
                                        if (thisIndex === -1) {
                                            $scope.selectedPolygon.push(myVisibleRows[i].entity.name);
                                        }
                                        bandColours[myVisibleRows[i].entity.name] = $scope.currentBand;
                                    }
                                    multiStart = -1;
                                }
                            }
                        };
                        //Clear all selection from map and table
                        $scope.clear = function () {
                            $scope.selectedPolygon = [];
                            for (var k in bandColours) {
                                bandColours[k] = 0;
                            }
                        };
                        //Select all in map and table
                        $scope.selectAll = function () {
                            $scope.selectedPolygon = [];
                            for (var i = 0; i < $scope.gridOptions.data.length; i++) {
                                $scope.selectedPolygon.push($scope.gridOptions.data[i].name);
                                bandColours[$scope.gridOptions.data[i].name] = $scope.currentBand;
                            }
                        };
                        //Reset only the selected band back to 0
                        $scope.clearBand = function () {
                            for (var i in bandColours) {
                                if (bandColours[i] === $scope.currentBand) {
                                    bandColours[i] = 0;
                                    $scope.selectedPolygon.splice($scope.selectedPolygon.indexOf(i), 1);
                                }
                            }
                        };
                        //Zoom to layer
                        $scope.zoomToExtent = function () {
                            leafletData.getMap("area").then(function (map) {
                                map.fitBounds(maxbounds);
                            });
                        };
                        //Show-hide centroids
                        $scope.showCentroids = function () {
                            leafletData.getMap("area").then(function (map) {
                                if (map.hasLayer(centroidMarkers)) {
                                    map.removeLayer(centroidMarkers);
                                } else {
                                    map.addLayer(centroidMarkers);
                                }
                            });
                        };

                        //This function fires all the rendering from UI events
                        //Watch selectedPolygon array for any changes
                        $scope.$watchCollection('selectedPolygon', function (newNames, oldNames) {
                            if (newNames === oldNames) {
                                return;
                            }
                            //Update the area counter
                            $scope.selectedPolygonCount = newNames.length;
                            //Update map selection
                            $scope.topoLayer.eachLayer(handleLayer);
                            //Update table selection
                            $scope.gridApi.selection.clearSelectedRows();
                            for (var i = 0; i < $scope.gridOptions.data.length; i++) {
                                $scope.gridOptions.data[i].band = bandColours[$scope.gridOptions.data[i].name];
                            }
                        });
                    }
                };
            }]);