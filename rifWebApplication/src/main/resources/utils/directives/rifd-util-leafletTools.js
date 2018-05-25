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
 * DIRECTIVES to handle shared Leaflet tools
 */
/* global L */

angular.module("RIF")
        /*
         * Zooming
         */
        .directive('zoomExtent', [function () {
                return {
                    restrict: 'A',
                    link: function (scope, element, attr) {
                        element.on('click', function (event) {
                            if (angular.isDefined(scope.child.maxbounds)) {
                                scope.child.map[attr.mapid].fitBounds(scope.child.maxbounds);
                            }
                        });
                    }
                };
            }])
        .directive('zoomArea', [function () {
                return {
                    restrict: 'A',
                    link: function (scope, element, attr) {
                        element.on('click', function (event) {
                            if (angular.isDefined(scope.studyBounds)) {
                                scope.map['exportmap'].fitBounds(scope.studyBounds);
                            }
                        });
                    }
                };
            }])
        .directive('zoomStudy', [function () {
                return {
                    restrict: 'A',
                    link: function (scope, element, attr) {
                        element.on('click', function (event) {
                            var studyBounds = new L.LatLngBounds();
                            if (angular.isDefined(scope.geoJSON[attr.mapid])) {
                                scope.geoJSON[attr.mapid]._geojsons.default.eachLayer(function (layer) {
                                    //if area ID is in the attribute table
                                    for (var i = 0; i < scope.child.tableData[attr.mapid].length; i++) {
                                        if (scope.child.tableData[attr.mapid][i]['area_id'].indexOf(layer.feature.properties.area_id) !== -1) {
                                            studyBounds.extend(layer.getBounds());
                                        }
                                    }
                                });
                                if (studyBounds.isValid()) {
                                    scope.child.map[attr.mapid].fitBounds(studyBounds);
                                }
                            }
                        });
                    }
                };
            }])
        .directive('zoomSelectionSingle', [function () {
                return {
                    restrict: 'A',
                    link: function (scope, element, attr) {
                        element.on('click', function (event) {
                            //Zoom to a single selected Polygon
                            var selbounds = null;
                            if (angular.isDefined(scope.geoJSON[attr.mapid])) {
                                scope.geoJSON[attr.mapid]._geojsons.default.eachLayer(function (layer) {
                                    if (layer.feature.properties.area_id === scope.thisPoly[attr.mapid]) {
                                        selbounds = layer.getBounds();
                                    }
                                });
                                if (selbounds !== null) {
                                    scope.child.map[attr.mapid].fitBounds(selbounds);
                                }
                            }
                        });
                    }
                };
            }])
        .directive('zoomSelectionMultiple', [function () {
                return {
                    restrict: 'A',
                    link: function (scope, element, attr) {
                        element.on('click', function (event) {
                            //Zoom to combined extent of selected polygons       
                            var studyBounds = new L.LatLngBounds();
                            if (angular.isDefined(scope.geoJSON[attr.mapid])) {
                                scope.geoJSON[attr.mapid]._geojsons.default.eachLayer(function (layer) {
                                    //if area ID is in the attribute table
                                    for (var i = 0; i < scope.child.tableData[attr.mapid].length; i++) {
                                        if (scope.child.tableData[attr.mapid][i]['area_id'] === layer.feature.properties.area_id) {
                                            if (scope.child.tableData[attr.mapid][i]['_selected'] === 1) {
                                                studyBounds.extend(layer.getBounds());
                                            }
                                        }
                                    }
                                });
                                if (studyBounds.isValid()) {
                                    scope.child.map[attr.mapid].fitBounds(studyBounds);
                                }
                            }
                        });
                    }
                };
            }])
        /*
         * Clear selections
         */
        .directive('clearArraySelection', ['MappingService', function (MappingService) {
                return {
                    restrict: 'A',
                    link: function (scope, element, attr) {
                        element.on('click', function (event) {
                            if (angular.isDefined(scope.geoJSON[attr.mapid])) {
                                if (angular.isArray(scope.$parent.thisPoly)) {
                                    //is an array from viewermap or study areas
                                    scope.$parent.thisPoly.length = 0;
                                    scope.refresh(attr.mapid);
                                } else {
                                    //is an object, single selections from diseasemap
                                    var toClear = [attr.mapid];
                                    if (scope.bLockSelect) {
                                        toClear.push(MappingService.getOtherMap(attr.mapid));
                                    }
                                    for (var i in toClear) {
                                        scope.thisPoly[toClear[i]] = null;
                                        scope.myService.getState().selected[toClear[i]] = null;
                                        scope.child.infoBox2[attr.mapid].update(scope.thisPoly[toClear[i]]);
                                        scope.updateMapSelection(null, toClear[i]);
                                    }
                                }
                                scope.$parent.$digest();
                            }
                        });
                    }
                };
            }])
        .directive('leafletToPng', [function () {
                //When defining new L.TopoJSON
                //Need to set renderer: L.canvas(),
                //Not: L.svg()
                return {
                    restrict: 'A',
                    link: function (scope, element, attr) {
                        element.on('click', function (event) {

                            var alertScope;
                            if (attr.mapid === "areamap") {
                                alertScope = scope.$parent.$$childHead.$parent.$parent.$$childHead;
                            } else {
                                alertScope = scope;
                            }

                            alertScope.showSuccess("Starting download...");

                            try {
                                //get the map object
                                var x = attr.mapid.split('.');
                                var thisMap = scope[x[0]];
                                if (x.length !== 1) {
                                    thisMap = scope[x[0]][x[1]];
                                }

                                //get the map elements
                                var hostMap = document.getElementById(x[x.length - 1]);
                                var thisLegend = hostMap.getElementsByClassName("info legend leaflet-control")[0]; //the legend
                                var thisScale = hostMap.getElementsByClassName("leaflet-control-scale leaflet-control")[0]; //the scale bar

                                //render
                                html2canvas(thisLegend, {
                                    onrendered: function (legend) {
                                        var scaleCanvas;
                                        html2canvas(thisScale, {
                                            onrendered: function (canvas1) {
                                                scaleCanvas = canvas1;
                                                leafletImage(thisMap, function (err, canvas) {

                                                    //padding between elements
                                                    var pad = 10;

                                                    //blank canvas
                                                    var blank = document.createElement('canvas');
                                                    blank.width = canvas.width;
                                                    blank.height = canvas.height;

                                                    //draw the map tiles
                                                    var blankctx = blank.getContext('2d');
                                                    blankctx.drawImage(canvas, 0, 0);

                                                    //overlay the legend
                                                    if (!angular.isUndefined(legend)) {
                                                        if (legend.width < canvas.width / 2) {
                                                            blankctx.drawImage(legend, canvas.width - legend.width - pad, pad);
                                                        }
                                                    }
                                                    //overlay the scale bar
                                                    if (!angular.isUndefined(scaleCanvas)) {
                                                        if (scaleCanvas.width < canvas.width / 2) {
                                                            blankctx.drawImage(scaleCanvas, pad, pad);
                                                        }
                                                    }

                                                    //Download with Filesaver.js
                                                    blank.toBlob(function (blob) {
                                                        saveAs(blob, "map.png");
                                                    });
                                                });
                                            }
                                        });
                                    }
                                });
                            } catch (err) {
                                alertScope.showError("Could not export the map");
                            }
                        });
                    }
                };
            }]);
