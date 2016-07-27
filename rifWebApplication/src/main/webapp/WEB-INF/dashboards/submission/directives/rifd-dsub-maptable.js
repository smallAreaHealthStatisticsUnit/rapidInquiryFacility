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
 * TODO: refactor selectpolygon
 */

/* global L, d3, key, topojson */
angular.module("RIF")
        .directive('submissionMapTable', ['leafletData', 'leafletMapEvents', 'ModalAreaService', 'LeafletDrawService', 'GISService', 'LeafletBaseMapService', '$timeout',
            function (leafletData, leafletMapEvents, ModalAreaService, LeafletDrawService, GISService, LeafletBaseMapService, $timeout) {
                return {
                    templateUrl: 'dashboards/submission/partials/rifp-dsub-maptable.html',
                    restrict: 'AE',
                    link: function ($scope) {

                        //Called on DOM render completion to ensure basemap is rendered
                        $timeout(function () {
                            $scope.parent.renderMap("area");
                        });
/*
                        var mapEvents = leafletMapEvents.getAvailableMapEvents();
                        for (var k in mapEvents) {
                            var eventName = 'leafletDirectiveMap.' + mapEvents[k];
                            $scope.$on(eventName, function (event) {
                                console.log(event.name);
                            });
                        }
*/
                        //map max bounds from topojson layer
                        var maxbounds;
                        //selectedPolygon array synchronises the map <-> table selections                        
                        $scope.selectedPolygon = $scope.input.selectedPolygon;

                        //total for display
                        $scope.selectedPolygonCount = 0;

                        //band colour look-up for selected districts
                        $scope.possibleBands = [1, 2, 3, 4, 5, 6, 7, 8, 9];
                        $scope.currentBand = 1; //from dropdown

                        //d3 polygon rendering, changed by slider
                        $scope.transparency = 0.7;

                        //district centres for rubberband selection
                        var latlngList = 0;
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
                        $scope.parent.thisLayer = LeafletBaseMapService.setBaseMap(LeafletBaseMapService.getCurrentBase());

                        $scope.parent.renderMap = function (mapID) {
                            leafletData.getMap(mapID).then(function (map) {
                                map.removeLayer($scope.parent.thisLayer);
                                if (!LeafletBaseMapService.getNoBaseMap()) {
                                    $scope.parent.thisLayer = LeafletBaseMapService.setBaseMap(LeafletBaseMapService.getCurrentBase());
                                    map.addLayer($scope.parent.thisLayer);
                                }
                                //restore setView
                                map.setView($scope.$parent.input.view, $scope.$parent.input.zoomLevel);
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
                                    var thisPoly = point[1];
                                    var thisPolyID = point[2];
                                    var bFound = false;
                                    for (var i = 0; i < $scope.selectedPolygon.length; i++) {
                                        if ($scope.selectedPolygon[i].id === thisPolyID) {
                                            bFound = true;
                                            break;
                                        }
                                    }
                                    if (!bFound) {
                                        if (shape.band === -1) {
                                            $scope.selectedPolygon.push({id: thisPolyID, gid: thisPolyID, label: thisPoly, band: $scope.currentBand});
                                        } else {
                                            $scope.selectedPolygon.push({id: thisPolyID, gid: thisPolyID, label: thisPoly, band: shape.band});
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

                        function renderFeature(feature) {
                            for (var i = 0; i < $scope.selectedPolygon.length; i++) {
                                if ($scope.selectedPolygon[i].label === feature) {
                                    bFound = true;
                                    var cb = ['#e41a1c', '#377eb8', '#4daf4a', '#984ea3', '#ff7f00', '#ffff33', '#a65628', '#f781bf', '#999999'];
                                    return cb[$scope.selectedPolygon[i].band - 1];
                                }
                            }
                            return '#F5F5F5'; //whitesmoke
                        }

                        //Functions to style topoJson on selection changes
                        function style(feature) {
                            return {
                                fillColor: renderFeature(feature.properties.LAD13NM),
                                weight: 1,
                                opacity: 1,
                                color: 'gray',
                                dashArray: '3',
                                fillOpacity: $scope.transparency
                            };
                        }
                        function handleLayer(layer) {
                            layer.setStyle({
                                fillColor: renderFeature(layer.feature.properties.LAD13NM),
                                fillOpacity: $scope.transparency
                            });
                        }
                        $scope.changeOpacity = function () {
                            $scope.topoLayer.eachLayer(handleLayer);
                        };

                        //Read topoJson with d3
                        d3.json("test/eng.json", function (error, data) {
                            //populate the table
                            for (var i = 0; i < data.objects.lad.geometries.length; i++) {
                                var thisPoly = data.objects.lad.geometries[i].properties.LAD13NM;
                                var bFound = false;
                                for (var j = 0; j < $scope.selectedPolygon.length; j++) {
                                    if ($scope.selectedPolygon[j].label === thisPoly) {
                                        data.objects.lad.geometries[i].properties.band = $scope.selectedPolygon[j].band;
                                        bFound = true;
                                        break;
                                    }
                                }
                                if (!bFound) {
                                    data.objects.lad.geometries[i].properties.band = 0;
                                }
                            }
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
                                        latlngList.push([L.latLng([p.lat, p.lng]), feature.properties.LAD13NM, feature.properties.LAD13CD]);

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
                                            var bFound = false;
                                            for (var i = 0; i < $scope.selectedPolygon.length; i++) {
                                                if ($scope.selectedPolygon[i].label === thisPoly) {
                                                    bFound = true;
                                                    $scope.selectedPolygon.splice(i, 1);
                                                    break;
                                                }
                                            }
                                            if (!bFound) {
                                                $scope.selectedPolygon.push({id: feature.properties.LAD13CD, gid: feature.properties.LAD13CD, label: feature.properties.LAD13NM, band: $scope.currentBand});
                                            }
                                        });
                                    }
                                });
                                $scope.topoLayer.addTo(map);
                                maxbounds = $scope.topoLayer.getBounds();
                                $scope.totalPolygonCount = latlngList.length;
                                if ($scope.$parent.input.zoomLevel === -1) {
                                    map.fitBounds(maxbounds);
                                    //Store the current zoom and view on map changes
                                    map.on('zoomend', function (e) {
                                        $scope.$parent.input.zoomLevel = map.getZoom();
                                    });
                                    map.on('moveend', function (e) {
                                        $scope.$parent.input.view = map.getCenter();
                                    });                                    
                                    $scope.$parent.input.zoomLevel = map.getZoom();
                                    $scope.$parent.input.view = map.getCenter();
                                }
                            });
                        });

                        //Multiple select with shift
                        //detect shift key (16) down
                        //TODO: Change to last click location as start point
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
                            //TODO: REFACTOR
                            var thisPoly = row.entity.label;
                            var thisPolyID = row.entity.id;
                            var bFound = false;
                            for (var i = 0; i < $scope.selectedPolygon.length; i++) {
                                if ($scope.selectedPolygon[i].id === thisPolyID) {
                                    bFound = true;
                                    $scope.selectedPolygon.splice(i, 1);
                                    break;
                                }
                            }
                            if (!bFound) {
                                $scope.selectedPolygon.push({id: thisPolyID, gid: thisPolyID, label: thisPoly, band: $scope.currentBand});
                            }

                            //We are doing a multiple select on the table, shift key is down
                            if (bShift) {
                                //get array of rows in filtered order
                                var myVisibleRows = $scope.gridApi.core.getVisibleRows();
                                if (multiStart === -1) {
                                    //get index of start multi select in the filtered table
                                    multiStart = ModalAreaService.matchRowNumber(myVisibleRows, row.entity.id);
                                } else {
                                    //get index of end multi select in the filtered table
                                    multiStop = ModalAreaService.matchRowNumber(myVisibleRows, row.entity.id);
                                    //do the multiple selection
                                    for (var i = Math.min(multiStop, multiStart);
                                            i <= Math.min(multiStop, multiStart) + (Math.abs(multiStop - multiStart)); i++) {
                                        var thisPoly = myVisibleRows[i].entity.label;
                                        var thisPolyID = myVisibleRows[i].entity.id;
                                        var bFound = false;
                                        for (var j = 0; j < $scope.selectedPolygon.length; j++) {
                                            if ($scope.selectedPolygon[j].id === thisPolyID) {
                                                bFound = true;
                                                break;
                                            }
                                        }
                                        if (!bFound) {
                                            $scope.selectedPolygon.push({id: thisPolyID, gid: thisPolyID, label: thisPoly, band: $scope.currentBand});
                                        }
                                    }
                                    multiStart = -1;
                                }
                            }
                        };

                        //Clear all selection from map and table
                        $scope.clear = function () {
                            $scope.selectedPolygon.length = 0;
                            $scope.input.selectedPolygon.length = 0;
                        };

                        //Select all in map and table
                        $scope.selectAll = function () {
                            $scope.selectedPolygon.length = 0;
                            for (var i = 0; i < $scope.gridOptions.data.length; i++) {
                                $scope.selectedPolygon.push({id: $scope.gridOptions.data[i].id, gid: $scope.gridOptions.data[i].id, label: $scope.gridOptions.data[i].label, band: $scope.currentBand});
                            }
                        };

                        //Reset only the selected band back to 0
                        $scope.clearBand = function () {
                            var i = $scope.selectedPolygon.length;
                            while (i--) {
                                if ($scope.selectedPolygon[i].band === $scope.currentBand) {
                                    $scope.selectedPolygon.splice(i, 1);
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
                                $scope.gridOptions.data[i].band = 0;
                                for (var j = 0; j < $scope.selectedPolygon.length; j++) {
                                    if ($scope.gridOptions.data[i].label === $scope.selectedPolygon[j].label) {
                                        $scope.gridOptions.data[i].band = $scope.selectedPolygon[j].band;
                                    }
                                }
                            }
                        });
                    }
                };
            }]);