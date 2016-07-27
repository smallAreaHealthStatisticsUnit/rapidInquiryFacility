/* global L, key, topojson, d3, ss, values */

angular.module("RIF")
        .controller('ViewerCtrl3', ['$scope',
            function ($scope) {

                //unused


            }])
        .controller('ViewerCtrl2', ['$scope', 'leafletData', 'LeafletBaseMapService', '$timeout', 'ViewerStateService', 'ChoroService', 'ColorBrewerService',
            function ($scope, leafletData, LeafletBaseMapService, $timeout, ViewerStateService, ChoroService, ColorBrewerService) {

                $scope.size1 = "33%";
                $scope.size2 = "66%";
                $scope.$on('ui.layout.resize', function () {
                    console.log('resize');
                });

                //leaflet render
                $scope.transparency = 0.7;
                var maxbounds;
                $scope.selectedPolygon = [];

                //d3 choropleth plotting
                var domain = [];
                var attr;
                var scale;

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
                        map.setView(ViewerStateService.getState().view, ViewerStateService.getState().zoomLevel);
                        //hack to refresh map
                        setTimeout(function () {
                            map.invalidateSize();
                            map.fitBounds(maxbounds); //TODO: from service
                        }, 50);
                    });
                };
                $scope.parent.renderMap("viewermap");

                $timeout(function () {
                    $scope.parent.renderMap("viewermap");
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
                        fillColor: renderFeature(feature),
                        weight: 1,
                        opacity: 1,
                        color: 'gray',
                        dashArray: '3',
                        fillOpacity: $scope.transparency
                    };
                }
                function renderFeatureSelect(feature) {
                    //TODO: use or not?
                    if (feature.properties._selected === 1) {
                        return 1;
                    } else {
                        return 1;
                    }
                }
                function renderFeature(feature) {
                    //TODO: should be a service                         

                    //handle selected
                    if (feature.properties._selected === 1) {
                        return "green";
                    }
                    //choropleth
                    if (scale && attr !== "") {
                        return scale(feature.properties[attr]);
                    } else {
                        return "#9BCD9B";
                    }
                }
                function handleLayer(layer) {
                    layer.setStyle({
                        fillColor: renderFeature(layer.feature),
                        fillOpacity: $scope.transparency
                    });
                }
                $scope.changeOpacity = function () {
                    $scope.topoLayer.eachLayer(handleLayer);
                };

                //Legend and hover box
                var legend = L.control({position: 'topright'});
                var infoBox = L.control({position: 'bottomleft'});
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
                $scope.parent.refresh = function (flip, method) {

                    //get selected colour ramp
                    var rangeIn = ChoroService.getViewMap().brewer;
                    attr = ChoroService.getViewMap().feature;

                    //Not a choropleth, but single colour
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

                    //get value range
                    domain.length = 0;
                    $scope.topoLayer.eachLayer(fillDomain);
                    var mx = Math.max.apply(Math, domain);
                    var mn = Math.min.apply(Math, domain);

                    //flip the colour ramp?
                    var range = [];
                    if (!flip) {
                        range = angular.copy(rangeIn);
                    } else {
                        range = angular.copy(rangeIn).reverse();
                    }

                    //find the breaks
                    switch (method) {
                        case "quantile":
                            scale = d3.scale.quantile()
                                    .domain(domain)
                                    .range(range);
                            var breaks = scale.quantiles();
                            break;
                        case "quantize":
                            scale = d3.scale.quantize()
                                    .domain([mn, mx])
                                    .range(range);
                            var breaks = [];
                            var dom = scale.domain();
                            var l = (dom[1] - dom[0]) / scale.range().length;
                            var breaks = d3.range(0, scale.range().length).map(function (i) {
                                return i * l;
                            });
                            breaks.shift();
                            break;
                        case "jenks":
                            var breaks = ss.jenks(domain, range.length);
                            breaks.pop();
                            breaks.shift();
                            scale = d3.scale.threshold()
                                    .domain(breaks)
                                    .range(range);
                            break;
                        case "standardDeviation":
                            /*
                             * Implementation derived by ArcMap Stand. Deviation classification
                             * 5 intervals of which those around the mean are 1/2 the Standard Deviation
                             */
                            var sd = ss.sample_standard_deviation(domain);
                            var mean = d3.mean(domain);

                            var below_mean = mean - sd / 2;
                            var above_mean = mean + sd / 2;
                            var breaks = [];

                            for (i = 0; below_mean > mn && i < 2; i++) {
                                breaks.push(below_mean);
                                below_mean = below_mean - sd;
                            }
                            for (i = 0; above_mean < mx && i < 2; i++) {
                                breaks.push(above_mean);
                                above_mean = above_mean + sd;
                            }
                            breaks.sort(d3.ascending);

                            //dynamic scale range
                            range = ColorBrewerService.getColorbrewer(ChoroService.getViewMap().brewerName, breaks.length + 1);

                            scale = d3.scale.threshold()
                                    .domain(breaks)
                                    .range(range);
                            break;
                        case "logarithmic":
                            //TODO: check, not implemented by Fred
                            scale = d3.scale.log()
                                    .domain([mn, mx])
                                    .range(range);
                            break;
                    }

                    //http://leafletjs.com/examples/choropleth.html
                    legend.onAdd = function () {
                        var div = L.DomUtil.create('div', 'info legend');
                        div.innerHTML += '<h4>' + attr + '</h4>';
                        for (var i = 0; i < range.length; i++) {
                            div.innerHTML += '<i style="background:' + range[i] + '"></i>';
                            if (i === 0) { //first break
                                div.innerHTML += '<span>' + mn.toFixed(2) + '&ndash;' + breaks[i].toFixed(2) + '</span><br>';
                            } else if (i === range.length - 1) { //last break
                                div.innerHTML += '<span>' + breaks[i - 1].toFixed(2) + '&ndash;' + mx.toFixed(2) + '</span>';
                            } else {
                                div.innerHTML += '<span>' + breaks[i - 1].toFixed(2) + '&ndash;' + breaks[i].toFixed(2) + '</span><br>';
                            }
                        }
                        return div;
                    };

                    leafletData.getMap("viewermap").then(function (map) {
                        //remove existing legend
                        if (legend._map) { //This may break in future leaflet versions
                            map.removeControl(legend);
                        }
                        legend.addTo(map);
                    });

                    //force a redraw
                    $scope.topoLayer.eachLayer(handleLayer);
                };

                function fillDomain(layer) {
                    domain.push(layer.feature.properties[attr]);
                }

                d3.json("test/za.js", function (error, data) {
                    //Fill data table   
                    var colDef = [];
                    var attrs = [];
                    for (var i in data.objects.layer1.geometries[0].properties) {
                        if (typeof (data.objects.layer1.geometries[0].properties[i]) === "number") {
                            //Attributes possible to map
                            attrs.push(i);
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
                        map.fitBounds(maxbounds);
                    });
                });




                //This function fires all the rendering from UI events
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