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
 * DIRECTIVE for risk analysis area selection using shapefiles
 */

/* global L */

angular.module("RIF")
        //Open a shapefile for risk analysis
        .controller('ModalAOIShapefileInstanceCtrl', function ($scope, $uibModalInstance) {
            $scope.close = function () {
                $uibModalInstance.dismiss();
            };
            $scope.submit = function () {
                var bOk = $scope.displayShapeFile();
                if (bOk) {
                    $uibModalInstance.close();
                }
            };
        })
        .directive('riskAnalysis', ['$rootScope', '$uibModal', '$q',
            function ($rootScope, $uibModal, $q) {
                return {
                    restrict: 'A',
                    link: function (scope, element, attr) {

                        var alertScope = scope.$parent.$$childHead.$parent.$parent.$$childHead;
                        var poly; //polygon shapefile
                        var buffers; //concentric buffers around points
                        var bandColours = ['#e41a1c', '#377eb8', '#4daf4a', '#984ea3', '#ff7f00', '#ffff33'];

                        var factory = L.icon({
                            iconUrl: 'images/factory.png',
                            iconAnchor: [16, 16]
                        });

                        //user input boxes
                        scope.bandAttr = [];

                        element.on('click', function (event) {
                            scope.modalHeader = "Select with a shapefile";
                            scope.accept = ".zip";
                            var modalInstance = $uibModal.open({
                                animation: true,
                                templateUrl: 'dashboards/submission/partials/rifp-dsub-fromshp.html',
                                windowClass: 'shapefile-modal',
                                controller: 'ModalAOIShapefileInstanceCtrl',
                                backdrop: 'static',
                                scope: scope,
                                keyboard: false
                            });

                            //ng-show which options to display
                            scope.selectionMethod = 1;
                            scope.bProgress = false;
                            scope.isPolygon = false;
                            scope.isPoint = false;
                            scope.isTable = false;
                            scope.bandAttr.length = 0;

                            //remove any existing AOI layer
                            poly = null;
                            buffers = null;
                            if (scope.areaMap.hasLayer(scope.shpfile)) {
                                scope.areaMap.removeLayer(scope.shpfile);
                                scope.shpfile = new L.layerGroup();
                            }
                        });

                        scope.radioChange = function (selectionMethod) {
                            scope.selectionMethod = selectionMethod;
                            if (selectionMethod === 3) {
                                scope.isTable = true;
                            } else {
                                scope.isTable = false;
                            }
                        };

                        function readShpFile(file) {
                            try {
                                if (file.name.slice(-3) !== 'zip') {
                                    //not a zip file
                                    alertScope.showError("All parts of the Shapefile expected in one zipped file");
                                    return;
                                } else {

                                    //type of study
                                    if (scope.possibleBands.length === 1) {
                                        scope.isRiskMapping = false;
                                    } else {
                                        scope.isRiskMapping = true;
                                    }

                                    var reader = new FileReader();
                                    var deferred = $q.defer();
                                    //http://jsfiddle.net/ashalota/ov0p4ajh/10/
                                    //http://leaflet.calvinmetcalf.com/#3/31.88/10.63
                                    reader.onload = function () {                                     
                                        var bAttr = false;   
                                        scope.attrs = [];
                                        poly = new L.Shapefile(this.result, {
                                            style: function (feature) {
                                                if (feature.geometry.type === "Point") {
                                                    scope.isPolygon = false;
                                                    scope.isPoint = true;
                                                    scope.isTable = true;
                                                } else if (feature.geometry.type === "Polygon") {
                                                    if (!bAttr) {
                                                        for (var property in feature.properties) {
                                                             scope.attrs.push(property);
                                                        }                                                   
                                                        bAttr = true;
                                                        scope.attr = scope.attrs[scope.attrs.length - 1];
                                                    }
                                                    scope.isPolygon = true;
                                                    scope.isPoint = false;
                                                    scope.isTable = false;
                                                    return {
                                                        fillColor: 'none',
                                                        weight: 2,
                                                        color: 'blue'
                                                    };
                                                }
                                            },
                                            onEachFeature: function (feature, layer) {
                                                if (feature.geometry.type === "Point") {
                                                    layer.setIcon(factory);
                                                    //TODO: add some type of pop-up
                                                    
                                                    //##########################################################


                                                }
                                            }
                                        });
                                        deferred.resolve(poly);
                                    };

                                    reader.readAsArrayBuffer(file);
                                    return deferred.promise;
                                }
                            } catch (err) {
                                alertScope.showError("Could not open Shapefile: " + err.message);
                                scope.bProgress = false;
                            }
                        }

                        scope.screenShapeFile = function () {
                            scope.bProgress = true;
                            var files = document.getElementById('setUpFile').files;
                            if (files.length === 0) {
                                return;
                            }
                            var file = files[0];

                            //clear existing layers
                            if (scope.shpfile.hasLayer(buffers)) {
                                scope.shpfile.removeLayer(buffers);
                            }
                            if (scope.shpfile.hasLayer(poly)) {
                                scope.shpfile.removeLayer(poly);
                            }
                            poly = null;
                            buffers = null;

                            //async for progress bar
                            readShpFile(file).then(function () {
                                //switch off progress bar
                                scope.bProgress = false;
                                if (!scope.isPolygon & !scope.isPoint) {
                                    alertScope.showError("This is not a valid point or polygon zipped shapefile");
                                }
                            });
                        };

                        scope.displayShapeFile = function () {
                            //exit if there is no shapefile
                            if (!scope.isPolygon && !scope.isPoint) {
                                return false;
                            }
                            
                            //check user input on bands
                            if (scope.selectionMethod === 3 || scope.isPoint) {
                                //check radii
                                for (var i = 0; i < scope.bandAttr.length; i++) {
                                    var thisBreak = Number(scope.bandAttr[i]);
                                    if (!isNaN(thisBreak)) {
                                        scope.bandAttr[i] = thisBreak;
                                    } else {
                                        alertScope.showError("Non-numeric band value entered");
                                        return false; //and only display the points
                                    }
                                }
                                if (scope.isPoint) {
                                    //check ascending and sequential for radii
                                    for (var i = 0; i < scope.bandAttr.length - 1; i++) {
                                        if (scope.bandAttr[i] > scope.bandAttr[i + 1]) {
                                            alertScope.showError("Distance band values are not in ascending order");
                                            return false;
                                        }
                                    }
                                } else {
                                    //check descending and sequential for exposures
                                    for (var i = 0; i < scope.bandAttr.length - 1; i++) {
                                        if (scope.bandAttr[i] < scope.bandAttr[i + 1]) {
                                            alertScope.showError("Exposure band values are not in descending order");
                                            return false;
                                        }
                                    }
                                }
                            }

                            //make bands around points
                            if (scope.isPoint) {
                                //make polygons and apply selection
                                buffers = new L.layerGroup();
                                for (var i = 0; i < scope.bandAttr.length; i++) {
                                    for (var j in poly._layers) {
                                        //Shp Library inverts lat, lngs for some reason (Bug?) - switch back
                                        var polygon = L.circle([poly._layers[j].feature.geometry.coordinates[1],
                                            poly._layers[j].feature.geometry.coordinates[0]],
                                                {
                                                    radius: scope.bandAttr[i],
                                                    fillColor: 'none',
                                                    weight: 3,
                                                    color: bandColours[i]
                                                });
                                        buffers.addLayer(polygon);
                                        $rootScope.$broadcast('makeDrawSelection', {
                                            data: polygon,
                                            circle: true,
                                            freehand: false,
                                            band: i + 1
                                        });
                                    }
                                }
                                scope.shpfile.addLayer(buffers);
                            } else if (scope.isPolygon) {
                                if (scope.selectionMethod === 2) {
                                    //make selection by band attribute in file
                                    for (var i in poly._layers) {
                                        //check these are valid bands
                                        if (scope.possibleBands.indexOf(poly._layers[i].feature.properties.band) === -1) {
                                            //band number not recognised
                                            alertScope.showError("Invalid band descriptor: " + poly._layers[i].feature.properties.band);
                                            return false;
                                        }
                                    }
                                } else if (scope.selectionMethod === 3) {
                                    //check the attribute is numeric etc
                                    for (var i in poly._layers) {
                                        //check these are valid exposure values
                                        if (!angular.isNumber(poly._layers[i].feature.properties[scope.attr])) {
                                            //number not recognised 
                                            alertScope.showError("Non-numeric value in file: " + poly._layers[i].feature.properties[scope.attr]); 
                                            return false;
                                        }
                                    }
                                }
                                //make the selection for each polygon
                                for (var i in poly._layers) {
                                    var polygon = L.polygon(poly._layers[i].feature.geometry.coordinates[0], {});
                                    var shape = {
                                        data: angular.copy(polygon)
                                    };
                                    shape.circle = false;
                                    shape.freehand = false;
                                    shape.shapefile = true;

                                    if (scope.selectionMethod === 1) {
                                        shape.band = -1;
                                    } else if (scope.selectionMethod === 2) {
                                        shape.band = poly._layers[i].feature.properties.band;
                                    } else if (scope.selectionMethod === 3) {
                                        var attr = poly._layers[i].feature.properties[scope.attr]; 
                                        shape.band = -1;
                                        for (var k = 0; k < scope.bandAttr.length; k++) {
                                            if (k === 0) {
                                                if (attr >= scope.bandAttr[k]) {
                                                    shape.band = 1;
                                                }
                                            } else if (k < scope.bandAttr.length - 1) {
                                                if (attr >= scope.bandAttr[k] && attr < scope.bandAttr[k - 1]) {
                                                    shape.band = k + 1;
                                                }
                                            }
                                        }
                                    }
                                    //make the selection
                                    scope.makeDrawSelection(shape);
                                }
                            }

                            //add AOI layer to map on modal close                            
                            try {
                                scope.shpfile.addLayer(poly);
                                scope.shpfile.addTo(scope.areaMap);
                            } catch (err) {
                                alertScope.showError("Could not open Shapefile, no valid features");
                                return false;
                            }
                            return true;
                        };
                    }
                };
            }]);