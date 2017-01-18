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
        .directive('submissionMapTable', ['leafletData', 'ModalAreaService', 'LeafletDrawService', '$uibModal', 'JSONService',
            'GISService', 'LeafletBaseMapService', '$timeout', 'user', 'SubmissionStateService',
            function (leafletData, ModalAreaService, LeafletDrawService, $uibModal, JSONService,
                    GISService, LeafletBaseMapService, $timeout, user, SubmissionStateService) {
                return {
                    templateUrl: 'dashboards/submission/partials/rifp-dsub-maptable.html',
                    restrict: 'AE',
                    link: function ($scope) {

                        //Called on DOM render completion to ensure basemap is rendered
                        $timeout(function () {
                            $scope.renderMap("area");
                            $scope.renderMap("area");
                        });
                        
                        /*
                         * LOCAL VARIABLES
                         */
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
                        $scope.possibleBands = $scope.input.bands;
                        $scope.currentBand = 1; //from dropdown
                        //d3 polygon rendering, changed by slider
                        $scope.transparency = $scope.input.transparency;


                        /*
                         * TOOL STRIP
                         */
                        //Clear all selection from map and table
                        $scope.clear = function () {
                            $scope.selectedPolygon.length = 0;
                            $scope.input.selectedPolygon.length = 0;
                            $scope.clearAOI();
                        };

                        //remove AOI layer
                        $scope.clearAOI = function () {
                            leafletData.getMap("area").then(function (map) {
                                if (map.hasLayer(shpfile)) {
                                    map.removeLayer(shpfile);
                                }
                            });
                        };

                        //Select all in map and table
                        $scope.selectAll = function () {
                            $scope.selectedPolygon.length = 0;
                            for (var i = 0; i < $scope.gridOptions.data.length; i++) {
                                $scope.selectedPolygon.push({id: $scope.gridOptions.data[i].id, gid: $scope.gridOptions.data[i].id, label: $scope.gridOptions.data[i].label, band: $scope.currentBand});
                            }
                        };

                        $scope.changeOpacity = function () {
                            $scope.input.transparency = $scope.transparency;
                            $scope.topoLayer.eachLayer(handleLayer);
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

                        /*
                         * RENDER THE MAP AND THE TABLE
                         */
                        getMyMap = function () {
                            user.getTiles(user.currentUser, thisGeography, $scope.input.selectAt, "area").then(function (res) {

                                //populate the table
                                for (var i = 0; i < res.data.objects['2_1_1'].geometries.length; i++) {
                                    var thisPoly = res.data.objects['2_1_1'].geometries[i].properties.area_id;
                                    var bFound = false;
                                    for (var j = 0; j < $scope.selectedPolygon.length; j++) {
                                        if ($scope.selectedPolygon[j].id === thisPoly) {
                                            res.data.objects['2_1_1'].geometries[i].properties.band = $scope.selectedPolygon[j].band;
                                            bFound = true;
                                            break;
                                        }
                                    }
                                    if (!bFound) {
                                        res.data.objects['2_1_1'].geometries[i].properties.band = 0;
                                    }
                                }
                                $scope.gridOptions.data = ModalAreaService.fillTable(res.data);

                                //draw the map
                                leafletData.getMap("area").then(function (map) {
                                    latlngList = [];
                                    centroidMarkers = new L.layerGroup();
                                    if (map.hasLayer($scope.topoLayer)) {
                                        map.removeLayer($scope.topoLayer);
                                    }
                                    $scope.topoLayer = new L.TopoJSON(res.data, {
                                        renderer: L.canvas(),                                 
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
                                    maxbounds = $scope.topoLayer.getBounds();
                                    $scope.topoLayer.addTo(map);
                                    $scope.totalPolygonCount = latlngList.length;

                                    //Store the current zoom and view on map changes
                                    map.on('zoomend', function (e) {
                                        $scope.input.zoomLevel = map.getZoom();
                                    });
                                    map.on('moveend', function (e) {
                                        $scope.input.view = map.getCenter();
                                    });
                                    if ($scope.input.view[0] === 0) {
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
                            $scope.clearAOI();
                            user.getGeoLevelViews(user.currentUser, thisGeography, $scope.input.selectAt).then(handleGeoLevelViews, handleGeographyError);
                        };

                        function handleGeoLevelSelect(res) {
                            $scope.geoLevels.length = 0;
                            for (var i = 0; i < res.data[0].names.length; i++) {
                                $scope.geoLevels.push(res.data[0].names[i]);
                            }
                            //To check that comparison study area not greater than study area
                            //Assumes that geoLevels is ordered array
                            $scope.input.geoLevels = $scope.geoLevels;
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
                            $scope.close();
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
                        $scope.thisLayer = LeafletBaseMapService.setBaseMap(LeafletBaseMapService.getCurrentBaseMapInUse("area"));

                        $scope.renderMap = function (mapID) {
                            leafletData.getMap(mapID).then(function (map) {
                                map.removeLayer($scope.thisLayer);
                                if (!LeafletBaseMapService.getNoBaseMap("area")) {
                                    $scope.thisLayer = LeafletBaseMapService.setBaseMap(LeafletBaseMapService.getCurrentBaseMapInUse("area"));
                                    map.addLayer($scope.thisLayer);
                                }
                                //restore setView
                                map.setView($scope.input.view, $scope.input.zoomLevel);
                                //hack to refresh map
                                setTimeout(function () {
                                    map.invalidateSize();
                                }, 50);
                            });
                        };

                        function renderFeature(feature) {
                            for (var i = 0; i < $scope.selectedPolygon.length; i++) {
                                if ($scope.selectedPolygon[i].id === feature) {
                                    bFound = true;
                                    //max possible is six bands according to specs
                                    var cb = ['#e41a1c', '#377eb8', '#4daf4a', '#984ea3', '#ff7f00', '#ffff33'];
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

                        /*
                         * TABLE CONTROLS
                         */
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
                                multiStop = -1;
                            }
                        };

                        //Table click event to update selectedPolygon 
                        $scope.rowClick = function (row) {
                            var myVisibleRows = $scope.gridApi.core.getVisibleRows();
                            if (!bShift) {
                                //We are doing a single click select on the table
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
                            } else {
                                //We are doing a multiple select on the table, shift key is down
                                multiStop = ModalAreaService.matchRowNumber(myVisibleRows, row.entity.id);
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
                            }
                            multiStart = ModalAreaService.matchRowNumber(myVisibleRows, row.entity.id);
                        };
                        
                        //*********************************************************************************************************************
                        //SELECTION METHODS

                        /*
                         * SELECT AREAS USING LEAFLETDRAW
                         */
                        //Add Leaflet.Draw capabilities
                        var drawnItems;
                        LeafletDrawService.get_CircleCapability(Math.max.apply(null, $scope.possibleBands));
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
                            L.control.scale({position: 'topleft', imperial: false}).addTo(map);

                            //Attributions to open in new window
                            map.attributionControl.options.prefix = '<a href="http://leafletjs.com" target="_blank">Leaflet</a>';
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
                            if (!shape.circle && !shape.shapefile) {
                                removeMapDrawItems();
                                //auto increase band dropdown
                                if ($scope.currentBand < Math.max.apply(null, $scope.possibleBands)) {
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
                            leafletData.getMap("area").then(function (map) {
                                map.addLayer(drawnItems);
                            });
                            $scope.input.bDrawing = false; //re-enable layer events
                        }

                        /*
                         * SELECT AREAS WITH A SHAPEFILE (AOI)
                         */
                        var shpfile = new L.layerGroup();
                        $scope.addAOI = function () {
                            $scope.modalHeader = "Upload Zipped AOI Shapefile";
                            $scope.accept = ".zip";

                            var modalInstance = $uibModal.open({
                                animation: true,
                                templateUrl: 'dashboards/submission/partials/rifp-dsub-fromfile.html',
                                controller: 'ModalAOIShapefileInstanceCtrl',
                                windowClass: 'stats-Modal',
                                backdrop: 'static',
                                scope: $scope,
                                keyboard: false
                            });
                            //remove any existing AOI layer
                            leafletData.getMap("area").then(function (map) {
                                if (map.hasLayer(shpfile)) {
                                    map.removeLayer(shpfile);
                                    shpfile = new L.layerGroup();
                                }
                            });
                        };

                        $scope.uploadShapeFile = function () {
                            //http://jsfiddle.net/ashalota/ov0p4ajh/10/
                            //http://leaflet.calvinmetcalf.com/#3/31.88/10.63
                            var files = document.getElementById('setUpFile').files;
                            if (files.length === 0) {
                                return;
                            }

                            try {
                                var file = files[0];
                                if (file.name.slice(-3) !== 'zip') {
                                    //not a zip file
                                    $scope.$parent.$$childHead.$parent.$parent.$$childHead.showError("All parts of the Shapefile expected in one zipped file");
                                    return;
                                } else {
                                    var reader = new FileReader();
                                    reader.onload = readerLoad;
                                    reader.readAsArrayBuffer(file);

                                    function readerLoad() {
                                        var poly = new L.Shapefile(this.result, {
                                            style: function (feature) {
                                                return {
                                                    fillColor: 'none',
                                                    weight: 3,
                                                    color: 'blue'
                                                };
                                            },
                                            onEachFeature: function (feature, layer) {
                                                var polygon = L.polygon(layer.feature.geometry.coordinates[0], {});
                                                var shape = {data: angular.copy(polygon)};
                                                shape.circle = false;
                                                shape.shapefile = true;
                                                shape.band = -1;
                                                shape.data._latlngs.length = 0;
                                                //Shp Library inverts lat, lngs for some reason (Bug?) - switch back
                                                for (var i = 0; i < polygon._latlngs[0].length; i++) {
                                                    var flip = new L.latLng(polygon._latlngs[0][i].lng, polygon._latlngs[0][i].lat);
                                                    shape.data._latlngs.push(flip);
                                                }
                                                makeDrawSelection(shape);
                                            }
                                        });
                                        //add AOI layer to map
                                        shpfile.addLayer(poly);
                                        leafletData.getMap("area").then(function (map) {
                                            try {
                                                shpfile.addTo(map);
                                                map.fitBounds(poly.getBounds());
                                            } catch (err) {
                                                $scope.$parent.$$childHead.$parent.$parent.$$childHead.showError("Could not open Shapefile, no valid polygons");
                                            }
                                        });
                                    }
                                }
                            } catch (err) {
                                $scope.$parent.$$childHead.$parent.$parent.$$childHead.showError("Could not open Shapefile: " + err.message);
                            }
                        };

                        /*
                         * SELECT AREAS FROM A LIST, CSV
                         */
                        $scope.openFromList = function () {
                            $scope.modalHeader = "Upload ID file";
                            $scope.accept = ".csv";

                            $scope.showContent = function ($fileContent) {
                                $scope.content = $fileContent.toString();
                            };

                            $scope.uploadFile = function () {
                                try {
                                    //parse the csv file
                                    var listOfIDs = JSON.parse(JSONService.getCSV2JSON($scope.content));

                                    //attempt to fill 'selectedPolygon' with valid entries
                                    $scope.clear();
                                    var bPushed = false;
                                    var bInvalid = false;
                                    for (var i = 0; i < listOfIDs.length; i++) {
                                        for (var j = 0; j < $scope.gridOptions.data.length; j++) {
                                            if ($scope.gridOptions.data[j].id === listOfIDs[i].ID) {
                                                var thisBand = Number(listOfIDs[i].Band);
                                                if ($scope.possibleBands.indexOf(thisBand) !== -1) {
                                                    bPushed = true;
                                                    $scope.selectedPolygon.push({id: $scope.gridOptions.data[j].id, gid: $scope.gridOptions.data[j].id,
                                                        label: $scope.gridOptions.data[j].label, band: Number(listOfIDs[i].Band)});
                                                    break;
                                                } else {
                                                    bInvalid = true;
                                                }
                                            }
                                        }
                                    }
                                    if (!bPushed) {
                                        $scope.$parent.$$childHead.$parent.$parent.$$childHead.showWarning("No valid districts found in your list");
                                    } else if (!bInvalid) {
                                        $scope.$parent.$$childHead.$parent.$parent.$$childHead.showSuccess("List uploaded sucessfully");
                                    } else {
                                        $scope.$parent.$$childHead.$parent.$parent.$$childHead.showSuccess("List uploaded sucessfully, but some enteries were not valid");
                                    }
                                } catch (e) {
                                    $scope.$parent.$$childHead.$parent.$parent.$$childHead.showError("Could not read or process the file: Please check formatting");
                                }
                            };
                            var modalInstance = $uibModal.open({
                                animation: true,
                                templateUrl: 'dashboards/submission/partials/rifp-dsub-fromfile.html',
                                controller: 'ModalFileListInstanceCtrl',
                                windowClass: 'stats-Modal',
                                backdrop: 'static',
                                scope: $scope,
                                keyboard: false
                            });
                        };                     
                        //********************************************************************************************************


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
            }])
        .controller('ModalAOIShapefileInstanceCtrl', function ($scope, $uibModalInstance) {
            $scope.close = function () {
                $uibModalInstance.dismiss();
            };
            $scope.submit = function () {
                $scope.uploadShapeFile();
                $uibModalInstance.close();
            };
        })
        .controller('ModalFileListInstanceCtrl', function ($scope, $uibModalInstance) {
            $scope.close = function () {
                $uibModalInstance.dismiss();
            };
            $scope.submit = function () {
                $scope.uploadFile();
                $uibModalInstance.close();
            };
        });