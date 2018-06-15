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
 * TODO: This prob needs refactoring / overhauling to fit in with the mapping controllers
 * although it does work fine as it is
 */

/* global L, d3, key, topojson */
angular.module("RIF")
        .directive('submissionMapTable', ['ModalAreaService', 'LeafletDrawService', '$uibModal', 'JSONService', 'mapTools',
            'GISService', 'LeafletBaseMapService', '$timeout', 'user', 'SubmissionStateService',
			'SelectStateService', 
            function (ModalAreaService, LeafletDrawService, $uibModal, JSONService, mapTools,
                    GISService, LeafletBaseMapService, $timeout, user, SubmissionStateService,
					SelectStateService) {
                return {
                    templateUrl: 'dashboards/submission/partials/rifp-dsub-maptable.html',
                    restrict: 'AE',
                    link: function ($scope) {

                        $scope.areamap = L.map('areamap', {condensedAttributionControl: false}).setView([0, 0], 1);

						SubmissionStateService.setAreaMap($scope.areamap);
						
                        $scope.thisLayer = LeafletBaseMapService.setBaseMap(LeafletBaseMapService.getCurrentBaseMapInUse("areamap"));

                        //Reference the child scope
                        //will be from the comparison area or study area controller
                        $scope.child = {};
                        var alertScope = $scope.$parent.$$childHead.$parent.$parent.$$childHead;
						$scope.areamap.on('remove', function(e) {
                            alertScope.consoleDebug("[rifd-dsub-maptable.js] removed shared areamap");
						});
						
                        ///Called on DOM render completion to ensure basemap is rendered
                        $timeout(function () {
                            //add baselayer
                            $scope.renderMap("areamap");

                            //Store the current zoom and view on map changes
                            $scope.areamap.on('zoomend', function (e) {
                                $scope.input.center.zoom = $scope.areamap.getZoom();
                            });
                            $scope.areamap.on('moveend', function (e) {
                                $scope.input.center.lng = $scope.areamap.getCenter().lng;
                                $scope.input.center.lat = $scope.areamap.getCenter().lat;
                            });

                            //slider
                            var slider = L.control.slider(function (v) {
                                $scope.changeOpacity(v);
                            }, {
                                id: slider,
                                position: 'topleft',
                                orientation: 'horizontal',
                                min: 0,
                                max: 1,
                                step: 0.01,
                                value: $scope.transparency,
                                title: 'Transparency',
                                logo: '',
                                syncSlider: true
                            }).addTo($scope.areamap);

                            //Custom toolbar
                            var tools = mapTools.getSelectionTools($scope);
                            for (var i = 0; i < tools.length; i++) {
                                new tools[i]().addTo($scope.areamap);
                            }

                            //scalebar and fullscreen
                            L.control.scale({position: 'bottomleft', imperial: false}).addTo($scope.areamap);
                            $scope.areamap.addControl(new L.Control.Fullscreen());

                            //drop down for bands
                            var dropDown = mapTools.getBandDropDown($scope);
                            new dropDown().addTo($scope.areamap);

                            //Set initial map extents
                            $scope.center = $scope.input.center;
                            $scope.areamap.setView([$scope.center.lat, $scope.center.lng], $scope.center.zoom);

                            //Attributions to open in new window
                            L.control.condensedAttribution({
                                prefix: '<a href="http://leafletjs.com" target="_blank">Leaflet</a>'
                            }).addTo($scope.areamap);

                            $scope.areamap.doubleClickZoom.disable();
                            $scope.areamap.band = Math.max.apply(null, $scope.possibleBands);
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
                         * These repeat stuff in the leafletTools directive - possible refactor
                         */
                        //Clear all selection from map and table
                        $scope.clear = function () {
                            $scope.selectedPolygon.length = 0;
                            $scope.input.selectedPolygon.length = 0;
                            $scope.clearAOI();
							if ($scope.input.type === "Risk Analysis") {
								SelectStateService.initialiseRiskAnalysis();
							}
							else {			
								SelectStateService.resetState();
							}
							
                            if ($scope.areamap.hasLayer(shapes)) {
                                $scope.areamap.removeLayer(shapes);
								shapes = new L.layerGroup();
								$scope.areamap.addLayer(shapes);
                            }
                        };
                        //remove AOI layer
                        $scope.clearAOI = function () {
                            if ($scope.areamap.hasLayer($scope.shpfile)) {
                                $scope.areamap.removeLayer($scope.shpfile);
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
                        $scope.changeOpacity = function (v) {
                            $scope.transparency = v;
                            $scope.input.transparency = $scope.transparency;
                            if ($scope.geoJSON) {
                                $scope.geoJSON._geojsons.default.eachLayer(handleLayer);
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
                            $scope.areamap.fitBounds(maxbounds);
                        };
                        //Zoom to selection
                        $scope.zoomToSelection = function () {
                            var studyBounds = new L.LatLngBounds();
                            if (angular.isDefined($scope.geoJSON)) {
                                $scope.geoJSON._geojsons.default.eachLayer(function (layer) {
                                    for (var i = 0; i < $scope.selectedPolygon.length; i++) {
                                        if ($scope.selectedPolygon[i].id === layer.feature.properties.area_id) {
                                            studyBounds.extend(layer.getBounds());
                                        }
                                    }
                                });
                                if (studyBounds.isValid()) {
                                    $scope.areamap.fitBounds(studyBounds);
                                }
                            }
                        };
                        //Show-hide centroids
                        $scope.showCentroids = function () {
                            if ($scope.areamap.hasLayer(centroidMarkers)) {
                                $scope.areamap.removeLayer(centroidMarkers);
                            } else {
                                $scope.areamap.addLayer(centroidMarkers);
                            }
                        }; 
						
                        //Show-hide shapes
						$scope.showShapes = function () {
                            if ($scope.areamap.hasLayer(shapes)) {
                                $scope.areamap.removeLayer(shapes);
                            } else {
                                $scope.areamap.addLayer(shapes);
                            }
                        };

                        /*
                         * DISEASE MAPPING OR RISK MAPPING
                         */
                        $scope.studyTypeChanged = function () {
                            //clear selection
                            $scope.clear();
                            //offer the correct number of bands
                            SubmissionStateService.getState().studyType = $scope.input.type;
                            if ($scope.input.type === "Risk Analysis") {
                                $scope.possibleBands = [1, 2, 3, 4, 5, 6];
                                $scope.areamap.band = 6;
								
								SelectStateService.initialiseRiskAnalysis();
                            } else {
                                $scope.possibleBands = [1];
                                $scope.currentBand = 1;
                                $scope.areamap.band = 1;
								
								SelectStateService.resetState();
                            }
                        };

                        /*
                         * RENDER THE MAP AND THE TABLE
                         */
                        getMyMap = function () {

                            if ($scope.areamap.hasLayer($scope.geoJSON)) {
                                $scope.areamap.removeLayer($scope.geoJSON);
                            }

                            var topojsonURL = user.getTileMakerTiles(user.currentUser, thisGeography, $scope.input.selectAt);
                            latlngList = [];
                            centroidMarkers = new L.layerGroup();

                            //Get the centroids from DB
                            var bWeightedCentres = true;
                            user.getTileMakerCentroids(user.currentUser, thisGeography, $scope.input.selectAt).then(function (res) {
                                for (var i = 0; i < res.data.smoothed_results.length; i++) {
                                    var p = res.data.smoothed_results[i];
                                    latlngList.push([L.latLng([p.y, p.x]), p.name, p.id]);
                                    var circle = new L.CircleMarker([p.y, p.x], {
                                        radius: 2,
                                        fillColor: "blue",
                                        color: "#000",
                                        weight: 1,
                                        opacity: 1,
                                        fillOpacity: 0.8
                                    });
                                    centroidMarkers.addLayer(circle);
                                }
                            }, function () {
                                //couldn't get weighted centres so generate geographic with leaflet
                                alertScope.showWarning("Could not find (weighted) centroids stored in database - using geographic centroids on the fly");
                                bWeightedCentres = false;
                            }).then(function () {
                                $scope.geoJSON = new L.topoJsonGridLayer(topojsonURL, {
                                    attribution: 'Polygons &copy; <a href="http://www.sahsu.org/content/rapid-inquiry-facility" target="_blank">Imperial College London</a>',
                                    layers: {
                                        default: {
                                            renderer: L.canvas(),
                                            style: style,
                                            onEachFeature: function (feature, layer) {
                                                //get as centroid marker layer. 
                                                if (!bWeightedCentres) {
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
                                                }
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
                                                    $scope.$digest();
                                                });
                                                layer.on('mouseout', function (e) {
                                                    $scope.geoJSON._geojsons.default.resetStyle(e.target);
                                                    $scope.thisPolygon = "";
                                                    $scope.$digest();
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
                                $scope.areamap.addLayer($scope.geoJSON);

                                //Get max bounds
                                user.getGeoLevelSelectValues(user.currentUser, thisGeography).then(function (res) {
                                    var lowestLevel = res.data[0].names[0];
                                    user.getTileMakerTilesAttributes(user.currentUser, thisGeography, lowestLevel).then(function (res) {
                                        maxbounds = L.latLngBounds([res.data.bbox[1], res.data.bbox[2]], [res.data.bbox[3], res.data.bbox[0]]);
                                        if (Math.abs($scope.input.center.lng) < 1 && Math.abs($scope.input.center.lat < 1)) {
                                            $scope.areamap.fitBounds(maxbounds);
                                        }
                                    });
                                });

                                //Get overall layer properties
                                user.getTileMakerTilesAttributes(user.currentUser, thisGeography, $scope.input.selectAt).then(function (res) {
                                    if (angular.isUndefined(res.data.objects)) {
                                        alertScope.showError("Could not get district polygons from database");
                                        return;
                                    }                                  
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
                            if ($scope.areamap.hasLayer(centroidMarkers)) {
                                $scope.areamap.removeLayer(centroidMarkers);
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
						var shapes = new L.layerGroup();
                        $scope.areamap.addLayer(shapes);
						
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
                            $scope.areamap.removeLayer($scope.thisLayer);
                            if (!LeafletBaseMapService.getNoBaseMap("areamap")) {
                                $scope.thisLayer = LeafletBaseMapService.setBaseMap(LeafletBaseMapService.getCurrentBaseMapInUse("areamap"));
                                $scope.thisLayer.addTo($scope.areamap);
                            }
                            //hack to refresh map
                            setTimeout(function () {
                                $scope.areamap.invalidateSize();
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
                                    if ($scope.gridOptions.data[i].area_id === $scope.selectedPolygon[j].id) {
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
                        $scope.areamap.addLayer(drawnItems);
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
                        $scope.areamap.addControl(drawControl);
                        new L.Control.GeoSearch({
                            provider: new L.GeoSearch.Provider.OpenStreetMap()
                        }).addTo($scope.areamap);
                        //add the circle to the map
                        $scope.areamap.on('draw:created', function (e) {
                            drawnItems.addLayer(e.layer);
                        });
                        //override other map mouse events
                        $scope.areamap.on('draw:drawstart', function (e) {
                            $scope.input.bDrawing = true;
                        });

                        //selection event fired from service
                        $scope.$on('makeDrawSelection', function (event, data) {
                            $scope.makeDrawSelection(data);
                        });
                        $scope.makeDrawSelection = function (shape) {
							
							// Create savedShape for SelectStateService
							var savedShape = {
								circle: shape.circle,
								freehand: shape.freehand,
								band: shape.band,
								radius: undefined,
								latLng: undefined,
								geojson: undefined
							}
							if (shape.circle) { // Represent circles as a point and a radius
								savedShape.radius=shape.data.getRadius();
								savedShape.latLng=shape.data.getLatLng();	
								
								// basic shape to map shapes layer group
								var circle = new L.Circle([savedShape.latLng.lat, savedShape.latLng.lng], {
									radius: savedShape.radius,
									color: "#000",
									weight: 1,
									opacity: 0.4,
									fillOpacity: 0
								});
								shapes.addLayer(circle);
							}
							else { // Use geoJSON
								savedShape.geojson=shape.data.toGeoJSON();
								var geojson= new L.geoJSON(savedShape.geojson, {
									color: "#000",
									weight: 1,
									opacity: 0.4,
									fillOpacity: 0
								});
								
								shapes.addLayer(geojson);
							}	

							// Save to SelectStateService
							if ($scope.input.name == "ComparisionAreaMap") {
								SelectStateService.getState().studySelection.comparisonShapes.push(savedShape);
							}
							else {
								SelectStateService.getState().studySelection.studyShapes.push(savedShape);
							}
							
                            latlngList.forEach(function (point) {
                                //is point in defined polygon?
                                var test;
                                if (shape.circle) {
                                    test = GISService.getPointincircle(point[0], shape);
                                } else {
                                    test = GISService.getPointinpolygon(point[0], shape);
                                }
                                if (test) {
									var thisLatLng = point[0];
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
                                            $scope.selectedPolygon.push({id: thisPolyID, gid: thisPolyID, label: thisPoly, band: $scope.currentBand, centroid: thisLatLng});
                                        } else {
                                            $scope.selectedPolygon.push({id: thisPolyID, gid: thisPolyID, label: thisPoly, band: shape.band, centroid: thisLatLng});
                                        }
										if (SelectStateService.getState().studyType == "Risk Analysis") {
															
//											SelectStateService.getState().studySelection.points.pushIfNotExist(thisLatLng, function(e) { 
//												return e.lat === thisLatLng.lat && e.lng === thisLatLng.lng; 
//											});
										}
                                    }
                                }
                            });

                            $scope.$applyAsync();

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
                            $scope.areamap.addLayer(drawnItems);
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
								/* Upload CSV file. Required fields: ID,Band. Name is 
								   included to make the file more understandable. Ideally
								   this function should be made capitalisation insensitive
								   and more flexible in the names, i.e. ID/areaId/area_id
								   and Band/bandId/band_id
								
								e.g.
								ID,NAME,Band
								01779778,California,1
								01779780,Connecticut,1
								01705317,Georgia,1
								01779785,Iowa,1
								01779786,Kentucky,1
								01629543,Louisiana,1
								01779789,Michigan,1
								01779795,New Jersey,1
								00897535,New Mexico,1
								01455989,Utah,1
								01779804,Washington,1
								
								Structure of parsed JSON:
								
								listOfIDs=[
								  {
									"ID": "01785533",
									"NAME": "Alaska",
									"Band": "1"
								  },
								  ...
								  {
									"ID": "01779804",
									"NAME": "Washington",
									"Band": "1"
								  }
								]; 
								 */
                                try {
                                    //parse the csv file
                                    var listOfIDs = JSON.parse(JSONService.getCSV2JSON($scope.content));
                                    //attempt to fill 'selectedPolygon' with valid entries
                                    $scope.clear();
									
									if ($scope.input.type === "Risk Analysis") {
										SelectStateService.initialiseRiskAnalysis();
									}
									else {			
										SelectStateService.resetState();
									}
									
                                    var bPushed = false;
                                    var bInvalid = false;
                                    for (var i = 0; i < listOfIDs.length; i++) {
                                        for (var j = 0; j < $scope.gridOptions.data.length; j++) {
                                            if ($scope.gridOptions.data[j].area_id === listOfIDs[i].ID) {
                                                var thisBand = Number(listOfIDs[i].Band);
//												$scope.consoleLog("[" + i + "," + j + "] MATCH area_id: " + $scope.gridOptions.data[j].area_id + 
//													"; ID: " + listOfIDs[i].ID +
//													"; thisBand: " + thisBand);
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
                                        alertScope.showWarning("No valid 'ID' fields or 'Band' numbers found in your list");
//										$scope.consoleLog(JSON.stringify(listOfIDs, null, 2));
                                    } else if (!bInvalid) {
                                        alertScope.showSuccess("List uploaded sucessfully");
                                    } else {
                                        alertScope.showSuccess("List uploaded sucessfully, but some 'ID' fields or 'Band' numbers were not valid");
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