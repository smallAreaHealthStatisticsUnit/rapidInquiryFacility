/*
 * DIRECTIVE for map and table linked area selections
 * Used in Study Area and Comparison Area Modals
 * 
 * Map and table selections are synchronised by updating the array 'selectedPolygon'
 * 'selectedPolygon' is updated using click events on the map and table
 * captured clicks do not lead directly to an update of the map or table rather
 * 'selectedPolygon' is monitored using a $watchCollection which renders the selections on changes
 * 
 */

/* global L, d3, key, topojson */
angular.module("RIF")
        .directive('submissionMapTable', ['leafletData', 'ModalAreaService', 'LeafletDrawService',
            'GISService', 'LeafletBaseMapService', '$timeout', 'user', 'SubmissionStateService',
            function (leafletData, ModalAreaService, LeafletDrawService,
                    GISService, LeafletBaseMapService, $timeout, user, SubmissionStateService) {
                return {
                    templateUrl: 'dashboards/submission/partials/rifp-dsub-maptable.html',
                    restrict: 'AE',
                    link: function ($scope) {

                        //Called on DOM render completion to ensure basemap is rendered
                        $timeout(function () {
                            $scope.parent.renderMap("area");
                        });
                        //map max bounds from topojson layer
                        var maxbounds;

                        //If geog changed then clear selected
                        var thisGeography = SubmissionStateService.getState().geography;
                        if (thisGeography !== $scope.input.geography) {
                            $scope.input.selectedPolygon.length = 0;
                            $scope.input.selectAt = "";
                            $scope.input.studyResolution = "";
                            $scope.input.geography = thisGeography;
                        }

                        //selectedPolygon array synchronises the map <-> table selections  
                        $scope.selectedPolygon = $scope.input.selectedPolygon;

                        //total for display
                        $scope.selectedPolygonCount = $scope.selectedPolygon.length;

                        //band colour look-up for selected districts
                        $scope.possibleBands = [1, 2, 3, 4, 5, 6, 7, 8, 9];
                        $scope.currentBand = 1; //from dropdown

                        //d3 polygon rendering, changed by slider
                        $scope.transparency = 0.7;

                        getMyMap = function () {
                            user.getTiles(user.currentUser, thisGeography, $scope.input.selectAt).then(function (topo) {

                                //populate the table
                                for (var i = 0; i < topo.data.objects['2_1_1'].geometries.length; i++) {
                                    var thisPoly = topo.data.objects['2_1_1'].geometries[i].properties.area_id;
                                    var bFound = false;
                                    for (var j = 0; j < $scope.selectedPolygon.length; j++) {
                                        if ($scope.selectedPolygon[j].id === thisPoly) {
                                            topo.data.objects['2_1_1'].geometries[i].properties.band = $scope.selectedPolygon[j].band;
                                            bFound = true;
                                            break;
                                        }
                                    }
                                    if (!bFound) {
                                        topo.data.objects['2_1_1'].geometries[i].properties.band = 0;
                                    }
                                }
                                $scope.gridOptions.data = ModalAreaService.fillTable(topo.data);

                                //draw the map
                                leafletData.getMap("area").then(function (map) {
                                    latlngList = [];
                                    centroidMarkers = new L.layerGroup();

                                    if (map.hasLayer($scope.topoLayer)) {
                                        map.removeLayer($scope.topoLayer);
                                    }

                                    $scope.topoLayer = new L.TopoJSON(topo.data, {
                                        style: style,
                                        onEachFeature: function (feature, layer) {
                                            //define polygon centroids
                                            var p = layer.getBounds().getCenter();
                                            latlngList.push([L.latLng([p.lat, p.lng]), feature.properties.name, feature.properties.area_id]);

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
                                                if ($scope.input.bDrawing) {
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
                                                $scope.thisPolygon = feature.properties.name;
                                            });
                                            layer.on('mouseout', function (e) {
                                                $scope.topoLayer.resetStyle(e.target);
                                                $scope.thisPolygon = "";
                                            });
                                            layer.on('click', function (e) {
                                                //if drawing then return
                                                if ($scope.input.bDrawing) {
                                                    return;
                                                }
                                                var thisPoly = e.target.feature.properties.area_id;
                                                var bFound = false;
                                                for (var i = 0; i < $scope.selectedPolygon.length; i++) {
                                                    if ($scope.selectedPolygon[i].id === thisPoly) {
                                                        bFound = true;
                                                        $scope.selectedPolygon.splice(i, 1);
                                                        break;
                                                    }
                                                }
                                                if (!bFound) {
                                                    $scope.selectedPolygon.push({id: feature.properties.area_id, gid: feature.properties.gid, label: feature.properties.name, band: $scope.currentBand});
                                                }
                                            });
                                        }
                                    });
                                    $scope.topoLayer.addTo(map);
                                    maxbounds = $scope.topoLayer.getBounds();
                                    $scope.totalPolygonCount = latlngList.length;

                                    //Store the current zoom and view on map changes
                                    map.on('zoomend', function (e) {
                                        $scope.input.zoomLevel = map.getZoom();
                                    });
                                    map.on('moveend', function (e) {
                                        $scope.input.view = map.getCenter();
                                    });
                                    if ($scope.input.zoomLevel === -1) {
                                        map.fitBounds(maxbounds);
                                    }
                                });
                            }, handleGeographyError);
                        };

                        /*
                         * GET THE SELECT AND VIEW RESOLUTIONS
                         */
                        $scope.geoLevels = [];
                        $scope.geoLevelsViews = [];

                        user.getGeoLevelSelectValues(user.currentUser, thisGeography).then(handleGeoLevelSelect, handleGeographyError);

                        $scope.geoLevelChange = function () {
                            $scope.selectedPolygon.length = 0;
                            user.getGeoLevelViews(user.currentUser, thisGeography, $scope.input.selectAt).then(handleGeoLevelViews, handleGeographyError);
                        };

                        function handleGeoLevelSelect(res) {
                            $scope.geoLevels.length = 0;
                            for (var i = 0; i < res.data[0].names.length; i++) {
                                $scope.geoLevels.push(res.data[0].names[i]);
                            }
                            //Only get default if pristine
                            if ($scope.input.selectAt === "" & $scope.input.studyResolution === "") {
                                user.getDefaultGeoLevelSelectValue(user.currentUser, thisGeography).then(handleDefaultGeoLevels, handleGeographyError);
                            } else {
                                user.getGeoLevelViews(user.currentUser, thisGeography, $scope.input.selectAt).then(handleGeoLevelViews, handleGeographyError);
                            }
                        }
                        function handleDefaultGeoLevels(res) {
                            //get the select levels
                            $scope.input.selectAt = res.data[0].names[0];
                            $scope.input.studyResolution = res.data[0].names[0];
                            $scope.geoLevelChange();
                        }
                        function handleGeoLevelViews(res) {
                            $scope.geoLevelsViews.length = 0;
                            for (var i = 0; i < res.data[0].names.length; i++) {
                                $scope.geoLevelsViews.push(res.data[0].names[i]);
                            }
                            //if not in list then match (because result res cannot be lower than select res)
                            if ($scope.geoLevelsViews.indexOf($scope.input.studyResolution) === -1) {
                                $scope.input.studyResolution = $scope.input.selectAt;
                            }
                            //get table
                            getMyMap();
                        }

                        function handleGeographyError() {
                            //close the modal                           
                            $scope.$parent.close();
                        }

                        /*
                         * MAP SETUP
                         */
                        //district centres for rubberband selection
                        var latlngList = 0;
                        var centroidMarkers = new L.layerGroup();
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
                                map.setView($scope.input.view, $scope.input.zoomLevel);
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
                                $scope.input.bDrawing = true;
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
                            $scope.input.bDrawing = false; //re-enable layer events
                        }

                        function renderFeature(feature) {
                            for (var i = 0; i < $scope.selectedPolygon.length; i++) {
                                if ($scope.selectedPolygon[i].id === feature) {
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
                                fillColor: renderFeature(feature.properties.area_id),
                                weight: 1,
                                opacity: 1,
                                color: 'gray',
                                dashArray: '3',
                                fillOpacity: $scope.transparency
                            };
                        }
                        function handleLayer(layer) {
                            layer.setStyle({
                                fillColor: renderFeature(layer.feature.properties.area_id),
                                fillOpacity: $scope.transparency
                            });
                        }
                        $scope.changeOpacity = function () {
                            $scope.topoLayer.eachLayer(handleLayer);
                        };

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
                            //TODO: REFACTOR - NEEDS ALSO FIXING
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
                            //Update the area counter
                            $scope.selectedPolygonCount = newNames.length;
                            if (!$scope.topoLayer) {
                                return;
                            } else {
                                //Update map selection
                                $scope.topoLayer.eachLayer(handleLayer);
                            }
                        });
                    }
                };
            }]);