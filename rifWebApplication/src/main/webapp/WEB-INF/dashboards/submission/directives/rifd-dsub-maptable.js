/**
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * Copyright 2016 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
 *
 * This file is part of the Rapid Inquiry Facility (RIF) project.
 * RIF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RIF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
 * to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
 * Boston, MA 02110-1301 USA
 
 * David Morley
 * @author dmorley
 */

/*
 * DIRECTIVE for map and table linked area selections
 */

/* global L, d3, key, topojson */
angular.module("RIF")
        .directive('submissionMapTable', ['ModalAreaService', 'LeafletDrawService', '$uibModal', 'JSONService',
            'GISService', 'LeafletBaseMapService', '$timeout', 'user', 'SubmissionStateService',
            function (ModalAreaService, LeafletDrawService, $uibModal, JSONService,
                    GISService, LeafletBaseMapService, $timeout, user, SubmissionStateService) {
                return {
                    templateUrl: 'dashboards/submission/partials/rifp-dsub-maptable.html',
                    restrict: 'AE',
                    link: function ($scope) {

                        var areaMap = L.map('area').setView([51.505, -0.09], 13);
                        $scope.thisLayer = LeafletBaseMapService.setBaseMap(LeafletBaseMapService.getCurrentBaseMapInUse("area"));

                        //Reference the child scope
                        $scope.child = {};
                        var alertScope = $scope.$parent.$$childHead.$parent.$parent.$$childHead;

                        ///Called on DOM render completion to ensure basemap is rendered
                        $timeout(function () {
                            //add baselayer
                            $scope.renderMap("area");

                            //Store the current zoom and view on map changes
                            areaMap.on('zoomend', function (e) {
                                $scope.input.center.zoom = areaMap.getZoom();
                            });
                            areaMap.on('moveend', function (e) {
                                $scope.input.center.lng = areaMap.getCenter().lng;
                                $scope.input.center.lat = areaMap.getCenter().lat;
                            });
                            //scalebar and fullscreen
                            L.control.scale({position: 'topleft', imperial: false}).addTo(areaMap);
                            areaMap.addControl(new L.Control.Fullscreen());

                            //Set initial map extents
                            $scope.center = $scope.input.center;
                            areaMap.setView([$scope.center.lat, $scope.center.lng], $scope.center.zoom);

                            //Attributions to open in new window
                            areaMap.attributionControl.options.prefix = '<a href="http://leafletjs.com" target="_blank">Leaflet</a>';
                            areaMap.doubleClickZoom.disable();
                            areaMap.band = Math.max.apply(null, $scope.possibleBands);
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
                            if (areaMap.hasLayer($scope.shpfile)) {
                                areaMap.removeLayer($scope.shpfile);
                                $scope.shpfile = new L.layerGroup();
                            }
                        };
                        //Select all in map and table
                        $scope.selectAll = function () {
                            $scope.selectedPolygon.length = 0;
                            for (var i = 0; i < $scope.gridOptions.data.length; i++) {
                                $scope.selectedPolygon.push({id: $scope.gridOptions.data[i].area_id, gid: $scope.gridOptions.data[i].area_id, label: $scope.gridOptions.data[i].label, band: $scope.currentBand});
                            }
                        };
                        $scope.changeOpacity = function () {
                            $scope.input.transparency = $scope.transparency;
                            $scope.geoJSON._geojsons.default.eachLayer(handleLayer);
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
                            areaMap.fitBounds(maxbounds);
                        };
                        //Show-hide centroids
                        $scope.showCentroids = function () {
                            if (areaMap.hasLayer(centroidMarkers)) {
                                areaMap.removeLayer(centroidMarkers);
                            } else {
                                areaMap.addLayer(centroidMarkers);
                            }
                        };

                        /*
                         * DISEASE MAPPING OR RISK MAPPING
                         */
                        $scope.studyTypeChanged = function () {
                            //clear selection
                            $scope.clear();
                            //offer the correct number of bands
                            if ($scope.input.type === "Risk Analysis") {
                                $scope.possibleBands = [1, 2, 3, 4, 5, 6];
                                areaMap.band = 6;
                            } else {
                                $scope.possibleBands = [1];
                                $scope.currentBand = 1;
                                areaMap.band = 1;
                            }
                        };

                        /*
                         * RENDER THE MAP AND THE TABLE
                         */
                        getMyMap = function () {

                            if (areaMap.hasLayer($scope.geoJSON)) {
                                areaMap.removeLayer($scope.geoJSON);
                            }

                            var topojsonURL = user.getTileMakerTiles(user.currentUser, thisGeography, $scope.input.selectAt);
                            latlngList = [];
                            centroidMarkers = new L.layerGroup();

                            $scope.geoJSON = new L.topoJsonGridLayer(topojsonURL, {
                                attribution: 'Polygons &copy; <a href="http://www.sahsu.org/content/rapid-inquiry-facility" target="_blank">Imperial College London</a>',
                                layers: {
                                    default: {
                                        renderer: L.canvas(),
                                        style: style,
                                        onEachFeature: function (feature, layer) {

                                            //TODO: get these centroids from rif_data.look_up_tables lookup_$scope.input.selectAt

                                            //get as centroid marker layer. 
                                            var p = layer.getBounds().getCenter();
                                            latlngList.push([L.latLng([p.lat, p.lng]), feature.properties.name, feature.properties.area_id]);
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
                                                    weight: 1.5,
                                                    fillOpacity: function () {
                                                        //set tranparency from slider
                                                        return($scope.transparency - 0.3 > 0 ? $scope.transparency - 0.3 : 0.1);
                                                    }()
                                                });
                                                $scope.thisPolygon = feature.properties.name;
                                            });
                                            layer.on('mouseout', function (e) {
                                                $scope.geoJSON._geojsons.default.resetStyle(e.target);
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
                                                $scope.$digest();
                                            });
                                        }
                                    }
                                }
                            });
                            areaMap.addLayer($scope.geoJSON);

                            //Get max bounds
                            user.getGeoLevelSelectValues(user.currentUser, thisGeography).then(function (res) {
                                var lowestLevel = res.data[0].names[0];
                                user.getTileMakerTilesAttributes(user.currentUser, thisGeography, lowestLevel).then(function (res) {
                                    maxbounds = L.latLngBounds([res.data.bbox[1], res.data.bbox[2]], [res.data.bbox[3], res.data.bbox[0]]);
                                    if ($scope.input.center.lng === 0) {
                                        areaMap.fitBounds(maxbounds);
                                    }
                                });
                            });

                            //Get overall layer properties
                            user.getTileMakerTilesAttributes(user.currentUser, thisGeography, $scope.input.selectAt).then(function (res) {
                                //populate the table
                                for (var i = 0; i < res.data.objects.collection.geometries.length; i++) {
                                    var thisPoly = res.data.objects.collection.geometries[i];
                                    var bFound = false;
                                    for (var j = 0; j < $scope.selectedPolygon.length; j++) {
                                        if ($scope.selectedPolygon[j].id === thisPoly.properties.area_id) {
                                            res.data.objects.collection.geometries[i].properties.band = $scope.selectedPolygon[j].band;
                                            bFound = true;
                                            break;
                                        }
                                    }
                                    if (!bFound) {
                                        res.data.objects.collection.geometries[i].properties.band = 0;
                                    }
                                }
                                $scope.gridOptions.data = ModalAreaService.fillTable(res.data);
                                $scope.totalPolygonCount = res.data.objects.collection.geometries.length;
                            });
                        };

                        /*
                         * GET THE SELECT AND VIEW RESOLUTIONS
                         */
                        $scope.geoLevels = [];
                        $scope.geoLevelsViews = [];
                        user.getGeoLevelSelectValues(user.currentUser, thisGeography).then(handleGeoLevelSelect, handleGeographyError);

                        $scope.geoLevelChange = function () {
                            //Clear the map
                            $scope.selectedPolygon.length = 0;
                            $scope.clearAOI();
                            if (areaMap.hasLayer(centroidMarkers)) {
                                areaMap.removeLayer(centroidMarkers);
                            }
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

                        //shapefile AOI, used in directive
                        $scope.shpfile = new L.layerGroup();

                        //Set up table (UI-grid)
                        $scope.gridOptions = ModalAreaService.getAreaTableOptions();
                        $scope.gridOptions.columnDefs = ModalAreaService.getAreaTableColumnDefs();
                        //Enable row selections
                        $scope.gridOptions.onRegisterApi = function (gridApi) {
                            $scope.gridApi = gridApi;
                        };

                        //Set the user defined basemap
                        $scope.renderMap = function (mapID) {
                            areaMap.removeLayer($scope.thisLayer);
                            if (!LeafletBaseMapService.getNoBaseMap("area")) {
                                $scope.thisLayer = LeafletBaseMapService.setBaseMap(LeafletBaseMapService.getCurrentBaseMapInUse("area"));
                                $scope.thisLayer.addTo(areaMap);
                            }
                            //hack to refresh map
                            setTimeout(function () {
                                areaMap.invalidateSize();
                            }, 50);
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
                                fillOpacity: $scope.transparency
                            };
                        }
                        function handleLayer(layer) {
                            layer.setStyle({
                                fillColor: renderFeature(layer.feature.properties.area_id),
                                fillOpacity: $scope.transparency
                            });
                        }

                        //********************************************************************************************************
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

                            if (!$scope.geoJSON) {
                                return;
                            } else {
                                //Update map selection    
                                $scope.geoJSON._geojsons.default.eachLayer(handleLayer);
                            }

                        });
                        //*********************************************************************************************************************
                        //SELECTION METHODS

                        /*
                         * SELECT AREAS USING LEAFLETDRAW
                         */
                        //Add Leaflet.Draw capabilities
                        var drawnItems;
                        LeafletDrawService.getCircleCapability();
                        LeafletDrawService.getPolygonCapability();
                        //Add Leaflet.Draw toolbar

                        L.drawLocal.draw.toolbar.buttons.circle = "Select by concentric bands";
                        L.drawLocal.draw.toolbar.buttons.polygon = "Select by freehand polygons";
                        drawnItems = new L.FeatureGroup();
                        areaMap.addLayer(drawnItems);
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
                        areaMap.addControl(drawControl);
                        new L.Control.GeoSearch({
                            provider: new L.GeoSearch.Provider.OpenStreetMap()
                        }).addTo(areaMap);
                        //add the circle to the map
                        areaMap.on('draw:created', function (e) {
                            drawnItems.addLayer(e.layer);
                        });
                        //override other map mouse events
                        areaMap.on('draw:drawstart', function (e) {
                            $scope.input.bDrawing = true;
                        });

                        //selection event fired from service
                        $scope.$on('makeDrawSelection', function (event, data) {
                            $scope.makeDrawSelection(data);
                        });
                        $scope.makeDrawSelection = function (shape) {
                            latlngList.forEach(function (point) {
                                //is point in defined polygon?
                                var test;
                                if (shape.circle) {
                                    test = GISService.getPointincircle(point[0], shape);
                                } else {
                                    test = GISService.getPointinpolygon(point[0], shape);
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
                            $scope.$digest();
                            if (!shape.circle && !shape.shapefile) {
                                removeMapDrawItems();
                                //auto increase band dropdown
                                if ($scope.currentBand < Math.max.apply(null, $scope.possibleBands)) {
                                    $scope.currentBand++;
                                }
                            }
                        };
                        //remove drawn items event fired from service
                        $scope.$on('removeDrawnItems', function (event, data) {
                            removeMapDrawItems();
                        });
                        function removeMapDrawItems() {
                            drawnItems.clearLayers();
                            areaMap.addLayer(drawnItems);
                            $scope.input.bDrawing = false; //re-enable layer events
                        }

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
                                            if ($scope.gridOptions.data[j].area_id === listOfIDs[i].ID) {
                                                var thisBand = Number(listOfIDs[i].Band);
                                                if ($scope.possibleBands.indexOf(thisBand) !== -1) {
                                                    bPushed = true;
                                                    $scope.selectedPolygon.push({id: $scope.gridOptions.data[j].area_id, gid: $scope.gridOptions.data[j].area_id,
                                                        label: $scope.gridOptions.data[j].label, band: Number(listOfIDs[i].Band)});
                                                    break;
                                                } else {
                                                    bInvalid = true;
                                                }
                                            }
                                        }
                                    }
                                    if (!bPushed) {
                                        alertScope.showWarning("No valid districts found in your list");
                                    } else if (!bInvalid) {
                                        alertScope.showSuccess("List uploaded sucessfully");
                                    } else {
                                        alertScope.showSuccess("List uploaded sucessfully, but some enteries were not valid");
                                    }
                                } catch (e) {
                                    alertScope.showError("Could not read or process the file: Please check formatting");
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
                    }
                };
            }]);