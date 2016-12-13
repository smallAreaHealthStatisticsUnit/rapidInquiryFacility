/* global L, key, topojson, d3, ss, values */

angular.module("RIF")
        .controller('ViewerCtrl', ['$scope', 'user', 'leafletData', 'LeafletBaseMapService', '$timeout', 'ViewerStateService',
            'ChoroService', 'LeafletExportService', 'D3ToPNGService',
            function ($scope, user, leafletData, LeafletBaseMapService, $timeout, ViewerStateService,
                    ChoroService, LeafletExportService, D3ToPNGService) {

                //ui-grid data
                $scope.tableData = {
                    "viewermap": []
                };

                //data for top-left histogram panel
                $scope.histoData = [];
                function getHistoData() {
                    $scope.histoData.length = 0;
                    for (var i = 0; i < $scope.viewerTableOptions.data.length; i++) {
                        $scope.histoData.push($scope.viewerTableOptions.data[i][ChoroService.getMaps(0).feature]);
                    }
                }

                //ui-container sizes
                $scope.distHistoCurrentHeight = 200;
                $scope.distHistoCurrentWidth = 200;
                $scope.pyramidCurrentHeight = 200;
                $scope.pyramidCurrentWidth = 200;
                $scope.vSplit1 = ViewerStateService.getState().vSplit1;
                $scope.hSplit1 = ViewerStateService.getState().hSplit1;
                $scope.hSplit2 = ViewerStateService.getState().hSplit2;

                //TODO: if browser window resized - see mapping controller

                $scope.$on('ui.layout.loaded', function () {
                    $scope.distHistoCurrentHeight = d3.select("#hSplit1").node().getBoundingClientRect().height;
                    $scope.distHistoCurrentWidth = d3.select("#hSplit1").node().getBoundingClientRect().width;
                    $scope.pyramidCurrentHeight = d3.select("#hSplit2").node().getBoundingClientRect().height;
                    $scope.pyramidCurrentWidth = d3.select("#hSplit1").node().getBoundingClientRect().width;
                });

                $scope.$on('ui.layout.resize', function (e, beforeContainer, afterContainer) {
                    //Monitor split sizes                  
                    if (beforeContainer.id === "vSplit1") {
                        ViewerStateService.getState().vSplit1 = (beforeContainer.size / beforeContainer.maxSize) * 100;
                        $scope.distHistoCurrentWidth = beforeContainer.size;
                        $scope.pyramidCurrentWidth = beforeContainer.size;
                    }
                    if (beforeContainer.id === "hSplit1") {
                        ViewerStateService.getState().hSplit1 = (beforeContainer.size / beforeContainer.maxSize) * 100;
                        $scope.distHistoCurrentHeight = beforeContainer.size;
                        $scope.pyramidCurrentHeight = afterContainer.size;
                    }
                    if (beforeContainer.id === "hSplit2") {
                        ViewerStateService.getState().hSplit2 = (beforeContainer.size / beforeContainer.maxSize) * 100;
                    }
                    //Rescale leaflet container        
                    leafletData.getMap("viewermap").then(function (map) {
                        setTimeout(function () {
                            map.invalidateSize();
                        }, 50);
                    });
                });

                //Drop-downs
                $scope.studyIDs = [1, 214, 205, 239];
                $scope.studyID = 1;
                $scope.years = [1990, 1991, 1992, 1993, 1994, 1995];
                $scope.year = 1995;
                $scope.sexes = ["Male", "Female", "Both"];
                $scope.sexString = "Male";
                var sex = $scope.sexes.indexOf($scope.sexString) + 1;

                //draw relevant geography for this study
                $scope.renderGeography = function () {
                   // user.getTiles(user.currentUser, "SAHSU", "LEVEL3", "viewer").then(handleTopoJSON, handleTopoJSONError);
                    user.getTiles(user.currentUser, "SAHSU", "LEVEL4", "viewer").then(handleTopoJSON, handleTopoJSONError);
                };

                //Study ID drop-down chnaged
                $scope.studyChanged = function () {
                    //TODO: 
                    //get possible years
                    //get possible sexes   
                    //get current geography
                };

                //Study year or sex changed
                $scope.studyOptionsChanged = function () {
                    //if geography changed then $scope.renderGeography();
                    //else call getAttributeTable()
                };


                //initial state
                $scope.renderGeography();

                //leaflet render
                $scope.transparency = ViewerStateService.getState().transparency;
                $scope.selectedPolygon = ViewerStateService.getState().selected;
                var maxbounds;
                var thisMap = [];
                $scope.domain = [];
                var attr;
                $scope.populationData = [];

                //get the user defined basemap
                $scope.thisLayer = LeafletBaseMapService.setBaseMap(LeafletBaseMapService.getCurrentBaseMapInUse("viewermap"));
                //called on bootstrap and on modal submit
                $scope.renderMap = function (mapID) {
                    leafletData.getMap(mapID).then(function (map) {
                        map.removeLayer($scope.thisLayer);
                        if (!LeafletBaseMapService.getNoBaseMap("viewermap")) {
                            $scope.thisLayer = LeafletBaseMapService.setBaseMap(LeafletBaseMapService.getCurrentBaseMapInUse("viewermap"));
                            map.addLayer($scope.thisLayer);
                        }
                        //restore setView
                        if (!angular.isUndefined(maxbounds) && ViewerStateService.getState().view[0] === 0) {
                            map.fitBounds(maxbounds);
                        } else {
                            map.setView(ViewerStateService.getState().view, ViewerStateService.getState().zoomLevel);
                        }
                        //hack to refresh map
                        setTimeout(function () {
                            map.invalidateSize();
                        }, 50);
                    });
                };

                $timeout(function () {
                    leafletData.getMap("viewermap").then(function (map) {
                        map.on('zoomend', function (e) {
                            ViewerStateService.getState().zoomLevel = map.getZoom();
                        });
                        map.on('moveend', function (e) {
                            ViewerStateService.getState().view = map.getCenter();
                        });
                        new L.Control.GeoSearch({
                            provider: new L.GeoSearch.Provider.OpenStreetMap()
                        }).addTo(map);
                        L.control.scale({position: 'topleft', imperial: false}).addTo(map);

                        //Attributions to open in new window
                        map.attributionControl.options.prefix = '<a href="http://leafletjs.com" target="_blank">Leaflet</a>';
                    });
                });

                //quick export leaflet panel
                $scope.saveLeaflet = function () {
                    var thisLegend = document.getElementsByClassName("info legend leaflet-control")[0]; //the legend
                    var thisScale = document.getElementsByClassName("leaflet-control-scale leaflet-control")[0]; //the scale bar
                    html2canvas(thisLegend, {
                        onrendered: function (canvas) {
                            var thisScaleCanvas;
                            html2canvas(thisScale, {
                                onrendered: function (canvas1) {
                                    thisScaleCanvas = canvas1;
                                    $scope.renderMap("viewermap");
                                    LeafletExportService.getLeafletExport("viewermap", "resultsViewerMap", canvas, thisScaleCanvas);
                                }
                            });
                        }
                    });
                };

                //Clear all selection from map and table
                $scope.clear = function () {
                    $scope.selectedPolygon.length = 0;
                };

                //Save D3 to PNG           
                $scope.saveD3Chart = function (chart) {
                    var pngHeight = $scope.distHistoCurrentHeight * 3;
                    var pngWidth = $scope.distHistoCurrentWidth * 3;
                    var fileName = "histogram.png";
                    if (chart === "#poppyramid") {
                        pngHeight = $scope.pyramidCurrentHeight * 3;
                        pngWidth = $scope.pyramidCurrentWidth * 3;
                        fileName = "populationPyramid.png";
                    }

                    var svgString = D3ToPNGService.getGetSVGString(d3.select(chart)
                            .attr("version", 1.1)
                            .attr("xmlns", "http://www.w3.org/2000/svg")
                            .node());
                    D3ToPNGService.getSvgString2Image(svgString, pngWidth, pngHeight, 'png', save);

                    function save(dataBlob, filesize) {
                        saveAs(dataBlob, fileName); // FileSaver.js function
                    }
                };

                //Zoom to layer
                $scope.zoomToExtent = function () {
                    leafletData.getMap("viewermap").then(function (map) {
                        map.fitBounds(maxbounds);
                    });
                    //TODO: zoom to results extent
                };

                //UI-Grid setup options
                $scope.viewerTableOptions = {
                    enableGridMenu: true,
                    gridMenuShowHideColumns: false,
                    exporterMenuPdf: false,
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

                function handleLayer(layer) {
                    //Join geography and results table
                    var thisAttr;
                    for (var i = 0; i < $scope.viewerTableOptions.data.length; i++) {
                        if ($scope.viewerTableOptions.data[i].area_id === layer.feature.properties.area_id) {
                            thisAttr = $scope.viewerTableOptions.data[i][ChoroService.getMaps(0).feature];
                            break;
                        }
                    }
                    //is selected?
                    var selected = false;
                    if ($scope.selectedPolygon.indexOf(layer.feature.properties.area_id) !== -1) {
                        selected = true;
                    }
                    var polyStyle = ChoroService.getRenderFeature(thisMap.scale, thisAttr, selected);
                    layer.setStyle({
                        fillColor: polyStyle,
                        fillOpacity: $scope.transparency
                    });
                }

                $scope.changeOpacity = function () {
                    ViewerStateService.getState().transparency = $scope.transparency;
                    $scope.topoLayer.eachLayer(handleLayer);
                };

                //Hover box and Legend
                var infoBox = L.control({position: 'bottomleft'});
                var legend = L.control({position: 'topright'});
                infoBox.onAdd = function () {
                    this._div = L.DomUtil.create('div', 'info');
                    this.update();
                    return this._div;
                };
                infoBox.update = function (poly) {
                    if (poly) {
                        var thisAttr;
                        for (var i = 0; i < $scope.viewerTableOptions.data.length; i++) {
                            if ($scope.viewerTableOptions.data[i].area_id === poly) {
                                thisAttr = $scope.viewerTableOptions.data[i][ChoroService.getMaps(0).feature];
                                break;
                            }
                        }
                        if (ChoroService.getMaps(0).feature !== "" && !angular.isUndefined(thisAttr)) {
                            this._div.innerHTML = '<h4>ID: ' + poly + '</br>' + ChoroService.getMaps(0).feature.toUpperCase().replace("_", " ") + ": " + Number(thisAttr).toFixed(3) + '</h4>';
                        } else {
                            this._div.innerHTML = '<h4>ID: ' + poly + '</h4>';
                        }
                    } else {
                        this._div.innerHTML = '';
                    }
                };
                leafletData.getMap("viewermap").then(function (map) {
                    infoBox.addTo(map);
                });

                //information from choropleth modal to colour map                             
                $scope.refresh = function () {
                    //get selected colour ramp
                    var rangeIn = ChoroService.getMaps(0).renderer.range;
                    $scope.distHistoName = ChoroService.getMaps(0).feature;
                    attr = ChoroService.getMaps(0).feature;

                    //get choropleth map renderer
                    thisMap = ChoroService.getMaps(0).renderer;

                    //not a choropleth, but single colour
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

                    //remove old legend and add new
                    legend.onAdd = ChoroService.getMakeLegend(thisMap, attr);
                    leafletData.getMap("viewermap").then(function (map) {
                        if (legend._map) { //This may break in future leaflet versions
                            map.removeControl(legend);
                        }
                        legend.addTo(map);
                    });

                    //force a redraw
                    $scope.topoLayer.eachLayer(handleLayer);

                    //Histogram
                    getHistoData();
                };

                function handleTopoJSON(res) {
                    leafletData.getMap("viewermap").then(function (map) {
                        $scope.topoLayer = new L.TopoJSON(res.data, {
                            renderer: L.canvas(),
                            style: function (feature) {
                                return {
                                    fillColor: ChoroService.getRenderFeature(thisMap.scale, attr, false),
                                    weight: 1,
                                    opacity: 1,
                                    color: 'gray',
                                    fillOpacity: $scope.transparency
                                };
                            },
                            onEachFeature: function (feature, layer) {
                                layer.on('mouseover', function (e) {
                                    this.setStyle({
                                        color: 'gray',
                                        weight: 1.5,
                                        fillOpacity: function () {
                                            return($scope.transparency - 0.3 > 0 ? $scope.transparency - 0.3 : 0.1);
                                        }()
                                    });
                                    infoBox.update(layer.feature.properties.area_id);
                                });
                                layer.on('click', function (e) {
                                    var thisPoly = e.target.feature.properties.area_id;
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
                                });
                                layer.on('mouseout', function (e) {
                                    $scope.topoLayer.eachLayer(handleLayer);
                                    infoBox.update(false);
                                });
                            }
                        });
                        $scope.topoLayer.addTo(map);
                        maxbounds = $scope.topoLayer.getBounds();
                    }).then(function () {
                        $scope.getAttributeTable();
                    }).then(function () {
                        $scope.renderMap("viewermap");
                        $scope.refresh();
                    });
                }

                function handleTopoJSONError() {
                    $scope.showError("Something went wrong when getting the geography");
                }

                $scope.getAttributeTable = function () {

                    //All results in table
                    user.getSmoothedResults(user.currentUser, $scope.studyID, sex, $scope.year)
                            .then(handleSmoothedResults, attributeError);

                    //Population pyramid data
                    user.getAllPopulationPyramidData(user.currentUser, $scope.studyID, $scope.year)
                            .then(handlePopulation, attributeError);

                    /*
                     //All results in table
                     user.getSmoothedResults(user.currentUser, 1, 1, 1990)
                     .then(handleSmoothedResults, attributeError);
                     
                     //Population pyramid data
                     user.getAllPopulationPyramidData(user.currentUser, 1, 1990)
                     .then(handlePopulation, attributeError);
                     */

                    function handleSmoothedResults(res) {
                        //fill results table
                        var colDef = [];
                        var attrs = [];

                        for (var i = 0; i < res.data.smoothed_results.length; i++) {
                            res.data.smoothed_results[i]._selected = 0;
                            $scope.tableData.viewermap.push(res.data.smoothed_results[i]);
                        }
                        //supress some pre-selected columns (see the service)
                        for (var i in res.data.smoothed_results[0]) {
                            if (ViewerStateService.getValidColumn(i)) {
                                if (i !== "area_id" && i !== "_selected") {
                                    attrs.push(i);
                                }
                                colDef.push({
                                    name: i,
                                    width: 100
                                });
                            }
                        }

                        $scope.distHistoName = ChoroService.getMaps(0).feature;
                        ChoroService.getMaps(0).features = attrs;
                        $scope.viewerTableOptions.columnDefs = colDef;
                        $scope.viewerTableOptions.data = $scope.tableData.viewermap;

                        //refresh view
                        $scope.refresh();
                        getHistoData();
                        updateTable();
                    }

                    function handlePopulation(res) {
                        $scope.populationData = res.data.smoothed_results;
                    }

                    function attributeError(e) {
                        console.log(e);
                    }
                };

                function updateTable() {
                    $scope.gridApi.selection.clearSelectedRows();
                    for (var i = 0; i < $scope.viewerTableOptions.data.length; i++) {
                        $scope.viewerTableOptions.data[i]._selected = 0;
                        for (var j = 0; j < $scope.selectedPolygon.length; j++) {
                            if ($scope.viewerTableOptions.data[i].area_id === $scope.selectedPolygon[j]) {
                                $scope.viewerTableOptions.data[i]._selected = 1;
                            }
                        }
                    }
                }

                //Multiple select with shift
                var bShift = false;
                var multiStart = -1;
                var multiStop = -1;
                //detect shift key (16) down
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
                $scope.rowClick = function (row) {
                    var myVisibleRows = $scope.gridApi.core.getVisibleRows();
                    if (!bShift) {
                        //We are doing a single click select on the table
                        var thisPoly = row.entity.area_id;
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
                    } else {
                        //We are doing a multiple select on the table, shift key is down
                        multiStop = matchRowNumber(myVisibleRows, row.entity.area_id);
                        for (var i = Math.min(multiStop, multiStart);
                                i <= Math.min(multiStop, multiStart) + (Math.abs(multiStop - multiStart)); i++) {
                            var thisPoly = myVisibleRows[i].entity.area_id;
                            var bFound = false;
                            for (var j = 0; j < $scope.selectedPolygon.length; j++) {
                                if ($scope.selectedPolygon[j] === thisPoly) {
                                    bFound = true;
                                    break;
                                }
                            }
                            if (!bFound) {
                                $scope.selectedPolygon.push(thisPoly);
                            }
                        }
                    }
                    multiStart = matchRowNumber(myVisibleRows, row.entity.area_id);

                    function matchRowNumber(visible, id) {
                        for (var i = 0; i < visible.length; i++) {
                            if (visible[i].entity.area_id === id) {
                                return(i);
                            }
                        }
                    }
                };

                //Watch selectedPolygon array for any changes
                $scope.$watchCollection('selectedPolygon', function (newNames, oldNames) {
                    if (newNames === oldNames) {
                        return;
                    }
                    //Update table selection
                    updateTable();
                    ViewerStateService.getState().selected = newNames;
                    //Update map selection
                    $scope.topoLayer.eachLayer(handleLayer);
                });
            }]);