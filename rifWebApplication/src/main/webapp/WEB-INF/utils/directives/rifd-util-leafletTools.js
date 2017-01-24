/* 
 * DIRECTIVES to handle shared Leaflet tools
 */

/* global L */

angular.module("RIF")
        /*
         * Change Opacity
         */
        .directive('changeOpacity', function () {
            return {
                restrict: 'A',
                link: function (scope, element, attr) {
                    scope.$watch(attr['ngModel'], function (v) {
                        if (!angular.isUndefined(scope.$parent.geoJSON[attr.mapid])) {
                            scope.$parent.geoJSON[attr.mapid].eachLayer(scope.child.handleLayer);
                            scope.myService.getState().transparency = v;
                        }
                    });
                }
            };
        })
        /*
         * Zooming
         */
        .directive('zoomExtent', ['leafletData', function (leafletData) {
                return {
                    restrict: 'A',
                    link: function (scope, element, attr) {
                        element.on('click', function (event) {
                            leafletData.getMap(attr.mapid).then(function (map) {
                                map.fitBounds(scope.child.maxbounds);
                            });
                        });
                    }
                };
            }])
        .directive('zoomStudy', ['leafletData', function (leafletData) {
                return {
                    restrict: 'A',
                    link: function (scope, element, attr) {
                        element.on('click', function (event) {
                            var studyBounds = new L.LatLngBounds();
                            scope.geoJSON[attr.mapid].eachLayer(function (layer) {
                                //if area ID is in the attribute table
                                for (var i = 0; i < scope.child.tableData[attr.mapid].length; i++) {
                                    if (scope.child.tableData[attr.mapid][i]['area_id'].indexOf(layer.feature.properties.area_id) !== -1) {
                                        studyBounds.extend(layer.getBounds());
                                    }
                                }
                            });
                            leafletData.getMap(attr.mapid).then(function (map) {
                                if (studyBounds.isValid()) {
                                    map.fitBounds(studyBounds);
                                }
                            });
                        });
                    }
                };
            }])
        .directive('zoomSelectionSingle', ['leafletData', function (leafletData) {
                return {
                    restrict: 'A',
                    link: function (scope, element, attr) {
                        element.on('click', function (event) {
                            //Zoom to a single selected Polygon
                            var selbounds = null;
                            scope.geoJSON[attr.mapid].eachLayer(function (layer) {
                                if (layer.feature.properties.area_id === scope.thisPoly[attr.mapid]) {
                                    selbounds = layer.getBounds();
                                }
                            });
                            if (selbounds !== null) {
                                leafletData.getMap(attr.mapid).then(function (map) {
                                    map.fitBounds(selbounds);
                                });
                            }
                        });
                    }
                };
            }])
        .directive('zoomSelectionMultiple', ['leafletData', function (leafletData) {
                return {
                    restrict: 'A',
                    link: function (scope, element, attr) {
                        element.on('click', function (event) {
                            //Zoom to combined extent of selected polygons       
                            var studyBounds = new L.LatLngBounds();
                            scope.geoJSON[attr.mapid].eachLayer(function (layer) {
                                //if area ID is in the attribute table
                                for (var i = 0; i < scope.child.tableData[attr.mapid].length; i++) {
                                    if (scope.child.tableData[attr.mapid][i]['area_id'] === layer.feature.properties.area_id) {
                                        if (scope.child.tableData[attr.mapid][i]['_selected'] === 1) {
                                            studyBounds.extend(layer.getBounds());
                                        }
                                    }
                                }
                            });
                            leafletData.getMap(attr.mapid).then(function (map) {
                                if (studyBounds.isValid()) {
                                    map.fitBounds(studyBounds);
                                }
                            });
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
                        });
                    }
                };
            }])
        .directive('leafletToPng', ['leafletData', function (leafletData) {
                //When defining new L.TopoJSON
                //Need to set renderer: L.canvas(),
                //Not: L.svg()
                return {
                    restrict: 'A',
                    link: function (scope, element, attr) {
                        element.on('click', function (event) {
                            var hostMap = document.getElementById(attr.mapid);
                            var thisLegend = hostMap.getElementsByClassName("info legend leaflet-control")[0]; //the legend
                            var thisScale = hostMap.getElementsByClassName("leaflet-control-scale leaflet-control")[0]; //the scale bar
                            html2canvas(thisLegend, {
                                onrendered: function (legend) {
                                    var thisScaleCanvas;
                                    html2canvas(thisScale, {
                                        onrendered: function (canvas1) {
                                            thisScaleCanvas = canvas1;
                                            if (!angular.isUndefined(scope.child.renderMap)) {
                                                scope.child.renderMap(attr.mapid);
                                            } else {
                                                scope.renderMap(attr.mapid);
                                            }
                                            //the map
                                            leafletData.getMap(attr.mapid).then(function (map) {
                                                leafletImage(map, function (err, canvas) {
                                                    //4 canvas elements here canvas=leaflet map, legend=legend, scale=scalebar                          
                                                    //and a new blank canvas
                                                    var can4 = document.createElement('canvas');
                                                    can4.width = canvas.width;
                                                    can4.height = canvas.height;
                                                    var ctx4 = can4.getContext('2d');
                                                    ctx4.drawImage(canvas, 0, 0);

                                                    var pad = 10;
                                                    //overlay the legend
                                                    if (!angular.isUndefined(legend)) {
                                                        if (legend.width < canvas.width / 2) {
                                                            ctx4.drawImage(legend, canvas.width - legend.width - pad, pad);
                                                        }
                                                    }
                                                    //overlay the scale bar
                                                    if (!angular.isUndefined(thisScaleCanvas)) {
                                                        if (thisScaleCanvas.width < canvas.width / 2) {
                                                            ctx4.drawImage(thisScaleCanvas, pad, pad);
                                                        }
                                                    }

                                                    //Download with Filesaver.js
                                                    can4.toBlob(function (blob) {
                                                        saveAs(blob, attr.mapid + ".png");
                                                    });
                                                });
                                            });
                                        }
                                    });
                                }
                            });
                        });
                    }
                };
            }]);
