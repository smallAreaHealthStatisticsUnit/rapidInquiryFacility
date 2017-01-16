/* 
 * CONTROLLER for disease mapping 
 */

/* global L, key, topojson, d3 */

angular.module("RIF")
        .controller('MappingCtrl', ['$scope', 'leafletData', '$timeout', 'MappingStateService',
            'ChoroService', 'LeafletExportService', 'D3ToPNGService', 'MappingService',
            function ($scope, leafletData, $timeout, MappingStateService,
                    ChoroService, LeafletExportService, D3ToPNGService, MappingService) {

                //Reference the child scope
                $scope.child = {};

                //A flag to keep renderers if changing tabs
                $scope.$on("$destroy", function () {
                    MappingStateService.getState().initial = true;
                });

                //Set initial map panels and events
                $timeout(function () {
                    leafletData.getMap("diseasemap1").then(function (map) {
                        map.on('zoomend', function (e) {
                            MappingStateService.getState().center["diseasemap1"].zoom = map.getZoom();
                        });
                        map.on('moveend', function (e) {
                            MappingStateService.getState().center["diseasemap1"].lng = map.getCenter().lng;
                            MappingStateService.getState().center["diseasemap1"].lat = map.getCenter().lat;
                        });
                        new L.Control.GeoSearch({
                            provider: new L.GeoSearch.Provider.OpenStreetMap()
                        }).addTo(map);
                        L.control.scale({position: 'topleft', imperial: false}).addTo(map);
                        //Attributions to open in new window
                        map.attributionControl.options.prefix = '<a href="http://leafletjs.com" target="_blank">Leaflet</a>';
                    });
                    leafletData.getMap("diseasemap2").then(function (map) {
                        map.on('zoomend', function (e) {
                            MappingStateService.getState().center["diseasemap2"].zoom = map.getZoom();
                        });
                        map.on('moveend', function (e) {
                            MappingStateService.getState().center["diseasemap2"].lng = map.getCenter().lng;
                            MappingStateService.getState().center["diseasemap2"].lat = map.getCenter().lat;
                        });
                        new L.Control.GeoSearch({
                            provider: new L.GeoSearch.Provider.OpenStreetMap()
                        }).addTo(map);
                        L.control.scale({position: 'topleft', imperial: false}).addTo(map);
                        //Attributions to open in new window
                        map.attributionControl.options.prefix = '<a href="http://leafletjs.com" target="_blank">Leaflet</a>';
                    });
                    //Set initial map extents
                    if ($scope.bLockCenters) {
                        $scope.center1 = MappingStateService.getState().center["diseasemap1"];
                        $scope.center2 = $scope.center1;
                    } else {
                        $scope.center1 = MappingStateService.getState().center["diseasemap1"];
                        $scope.center2 = MappingStateService.getState().center["diseasemap2"];
                    }

                    //Fill study drop-downs
                    $scope.child.getStudies();
                });

                //ui-container sizes
                $scope.currentWidth1 = 100;
                $scope.currentHeight1 = 100;
                $scope.currentWidth2 = 100;
                $scope.currentHeight2 = 100;
                $scope.vSplit1 = MappingStateService.getState().vSplit1;
                $scope.hSplit1 = MappingStateService.getState().hSplit1;
                $scope.hSplit2 = MappingStateService.getState().hSplit2;

                $scope.getD3Frames = function () {
                    $scope.currentWidth1 = d3.select("#rr1").node().getBoundingClientRect().width;
                    $scope.currentHeight1 = d3.select("#rr1").node().getBoundingClientRect().height;
                    $scope.currentWidth2 = d3.select("#rr2").node().getBoundingClientRect().width;
                    $scope.currentHeight2 = d3.select("#rr2").node().getBoundingClientRect().height;
                };

                $scope.getD3FramesOnResize = function (beforeContainer, afterContainer) {
                    if (beforeContainer.id === "hSplit1") {
                        MappingStateService.getState().hSplit1 = (beforeContainer.size / beforeContainer.maxSize) * 100;
                        $scope.currentHeight1 = d3.select("#rr1").node().getBoundingClientRect().height;
                    }
                    if (beforeContainer.id === "vSplit1") {
                        MappingStateService.getState().vSplit1 = (beforeContainer.size / beforeContainer.maxSize) * 100;
                        $scope.currentWidth1 = d3.select("#rr1").node().getBoundingClientRect().width;
                        $scope.currentWidth2 = d3.select("#rr2").node().getBoundingClientRect().width;
                    }
                    if (beforeContainer.id === "hSplit2") {
                        MappingStateService.getState().hSplit2 = (beforeContainer.size / beforeContainer.maxSize) * 100;
                        $scope.currentHeight2 = d3.select("#rr2").node().getBoundingClientRect().height;
                    }
                    $scope.child.rescaleLeafletContainer();
                };

                /*
                 * Local map variables
                 */
                $scope.myMaps = ["diseasemap1", "diseasemap2"];

                //define non-null centres for leaflet directive
                $scope.center1 = {};
                $scope.center2 = {};
                //transparency as set by slider
                $scope.transparency = {
                    "diseasemap1": MappingStateService.getState().transparency["diseasemap1"],
                    "diseasemap2": MappingStateService.getState().transparency["diseasemap2"]
                };
                //target for getTiles topoJSON
                $scope.geoJSON = {};
                //selected polygons
                $scope.thisPoly = {
                    'diseasemap1': MappingStateService.getState().area_id["diseasemap1"],
                    'diseasemap2': MappingStateService.getState().area_id["diseasemap2"]
                };

                //study to be mapped
                $scope.studyID = {
                    "diseasemap1": MappingStateService.getState().study['diseasemap1'],
                    "diseasemap2": MappingStateService.getState().study['diseasemap2']
                };
                $scope.sex = {
                    "diseasemap1": MappingStateService.getState().sex['diseasemap1'],
                    "diseasemap2": MappingStateService.getState().sex['diseasemap2']
                };

                //attributes for d3
                $scope.rrChartData = {
                    "diseasemap1": [],
                    "diseasemap2": []
                };

                //options for d3 directives
                $scope.optionsRR = {
                    "diseasemap1": {
                        this: "optionsRR1",
                        panel: "diseasemap1",
                        x_field: "x_order",
                        risk_field: "rr",
                        cl_field: "cl",
                        cu_field: "ul",
                        label_field: "",
                        zoomStart: MappingStateService.getState().brushStartLoc['diseasemap1'],
                        zoomEnd: MappingStateService.getState().brushEndLoc['diseasemap1']
                    },
                    "diseasemap2": {
                        this: "optionsRR2",
                        panel: "diseasemap2",
                        x_field: "x_order",
                        risk_field: "rr",
                        cl_field: "cl",
                        cu_field: "ul",
                        label_field: "",
                        zoomStart: MappingStateService.getState().brushStartLoc['diseasemap2'],
                        zoomEnd: MappingStateService.getState().brushEndLoc['diseasemap2']
                    }
                };

                /*
                 * Leaflet Toolstrip TODO: REFACTOR to SERVICE or DIRECTIVE
                 */

                //map layer opacity with slider
                $scope.changeOpacity = function (mapID) {
                    MappingStateService.getState().transparency[mapID] = $scope.transparency[mapID];
                    leafletData.getMap(mapID).then(function (map) {
                        if (map.hasLayer($scope.geoJSON[mapID])) {
                            $scope.geoJSON[mapID].eachLayer($scope.child.handleLayer);
                        }
                    });
                };

                //Zoom to layer DOUBLE click
                $scope.zoomToExtent = function (mapID) {
                    leafletData.getMap(mapID).then(function (map) {
                        map.fitBounds($scope.child.maxbounds);
                    });
                };

                //Zoom to study extent SINGLE click
                $scope.zoomToStudy = function (mapID) {
                    var studyBounds = new L.LatLngBounds();
                    $scope.geoJSON[mapID].eachLayer(function (layer) {
                        //if area ID is in the attribute table
                        for (var i = 0; i < $scope.child.tableData[mapID].length; i++) {
                            if ($scope.child.tableData[mapID][i]['area_id'].indexOf(layer.feature.properties.area_id) !== -1) {
                                studyBounds.extend(layer.getBounds());
                            }
                        }
                    });
                    leafletData.getMap(mapID).then(function (map) {
                        if (studyBounds.isValid()) {
                            map.fitBounds(studyBounds);
                        }
                    });
                };

                //Zoom to single selected polygon
                $scope.zoomToSelected = function (mapID) {
                    var selbounds = null;
                    $scope.geoJSON[mapID].eachLayer(function (layer) {
                        if (layer.feature.properties.area_id === $scope.thisPoly[mapID]) {
                            selbounds = layer.getBounds();
                        }
                    });
                    if (selbounds !== null) {
                        leafletData.getMap(mapID).then(function (map) {
                            map.fitBounds(selbounds);
                        });
                    }
                };

                //clear selected polygon
                $scope.clear = function (mapID) {
                    var toClear = [mapID];
                    if ($scope.bLockSelect) {
                        toClear.push(getOtherMap(mapID));
                    }
                    for (var i in toClear) {
                        $scope.thisPoly[toClear[i]] = null;
                        MappingStateService.getState().selected[toClear[i]] = null;
                        $scope.child.infoBox2[mapID].update($scope.thisPoly[toClear[i]]);
                        $scope.updateMapSelection(null, toClear[i]);
                    }
                };


                /*
                 * Save Graphs and map to PNG
                 */

                //TODO:REFACTOR
                //save D3 charts
                $scope.saveD3Chart = function (mapID) {
                    var container = "rrchart" + mapID;
                    if (document.getElementById("rrchart" + mapID) === null) {
                        return;
                    }
                    var pngHeight = $scope.currentHeight1 * 3;
                    var pngWidth = $scope.currentWidth1 * 3;
                    var fileName = "risk1.png";
                    if (mapID === "diseasemap2") {
                        pngHeight = $scope.currentHeight2 * 3;
                        pngWidth = $scope.currentWidth2 * 3;
                        fileName = "risk2.png";
                    }
                    var svgString = D3ToPNGService.getGetSVGString(d3.select("#" + container)
                            .attr("version", 1.1)
                            .attr("xmlns", "http://www.w3.org/2000/svg")
                            .node());
                    D3ToPNGService.getSvgString2Image(svgString, pngWidth, pngHeight, 'png', save);
                    function save(dataBlob, filesize) {
                        saveAs(dataBlob, fileName); // FileSaver.js function
                    }
                };

                //quick export leaflet panel
                $scope.saveLeaflet = function (mapID) {
                    var hostMap = document.getElementById(mapID);
                    var thisLegend = hostMap.getElementsByClassName("info legend leaflet-control")[0]; //the legend
                    var thisScale = hostMap.getElementsByClassName("leaflet-control-scale leaflet-control")[0]; //the scale bar
                    html2canvas(thisLegend, {
                        onrendered: function (canvas) {
                            var thisScaleCanvas;
                            html2canvas(thisScale, {
                                onrendered: function (canvas1) {
                                    thisScaleCanvas = canvas1;
                                    $scope.renderMap(mapID);
                                    //the map
                                    LeafletExportService.getLeafletExport(mapID, "DiseaseMap" + ($scope.myMaps.indexOf(mapID) + 1), canvas, thisScaleCanvas);
                                }
                            });
                        }
                    });
                };

                /*
                 * D3
                 */

                //draw rr chart from d3 directive 'rrZoom'
                $scope.getD3chart = function (mapID, attribute) {
                    //reset brush handles        
                    MappingStateService.getState().brushEndLoc[mapID] = null;
                    MappingStateService.getState().brushStartLoc[mapID] = null;
                    $scope.optionsRR[mapID].zoomStart = null;
                    $scope.optionsRR[mapID].zoomEnd = null;

                    //make array for d3 areas
                    var rs = [];
                    for (var i = 0; i < $scope.child.tableData[mapID].length; i++) {
                        //Handle inconsistant naming in results table
                        //Handle if there are no confidence intervals
                        var ciString = ["lower95", "upper95"];
                        if (attribute === "smoothed_smr") {
                            ciString = ["smoothed_smr_lower95", "smoothed_smr_upper95"];
                        }
                        rs.push(
                                {
                                    name: attribute,
                                    gid: $scope.child.tableData[mapID][i].area_id,
                                    x_order: i,
                                    rr: $scope.child.tableData[mapID][i][attribute],
                                    cl: function () {
                                        if (attribute !== "posterior_probability") {
                                            return $scope.child.tableData[mapID][i][ciString[0]];
                                        } else {
                                            return $scope.child.tableData[mapID][i][attribute];
                                        }
                                    }(),
                                    ul: function () {
                                        if (attribute !== "posterior_probability") {
                                            return $scope.child.tableData[mapID][i][ciString[1]];
                                        } else {
                                            return $scope.child.tableData[mapID][i][attribute];
                                        }
                                    }()
                                }
                        );
                    }

                    //reorder
                    rs.sort(function (a, b) {
                        return parseFloat(a.rr) - parseFloat(b.rr);
                    });
                    for (var i = 0; i < $scope.child.tableData[mapID].length; i++) {
                        rs[i]["x_order"] = i + 1;
                    }
                    //set options for directive
                    $scope.optionsRR[mapID].label_field = attribute;
                    $scope.rrChartData[mapID] = angular.copy(rs);
                };

                //key events to move the dropline
                $scope.child.mapInFocus = "";
                angular.element(document).bind('keydown', function (e) {
                    if ($scope.mapInFocus !== "") {
                        if (e.keyCode === 37 || e.keyCode === 39) { //left || right
                            $scope.$broadcast('rrKeyEvent', false, e.keyCode, $scope.child.mapInFocus);
                        }
                    }
                });
                angular.element(document).bind('keyup', function (e) {
                    if ($scope.mapInFocus !== "") {
                        if (e.keyCode === 37 || e.keyCode === 39) { //left || right
                            $scope.$broadcast('rrKeyEvent', true, e.keyCode, $scope.child.mapInFocus);
                        }
                    }
                });

                /*
                 * Specific to handle 2 Leaflet Panels
                 */

                //sync map extents
                $scope.bLockCenters = MappingStateService.getState().extentLock;
                $scope.lockExtent = function () {
                    if ($scope.bLockCenters) {
                        $scope.bLockCenters = false;
                        MappingStateService.getState().extentLock = false;
                        $scope.center2 = angular.copy($scope.center1);
                    } else {
                        $scope.bLockCenters = true;
                        MappingStateService.getState().extentLock = true;
                        $scope.center2 = $scope.center1;
                    }
                };

                //sync map selections
                $scope.bLockSelect = MappingStateService.getState().selectionLock;
                $scope.lockSelect = function () {
                    if ($scope.bLockSelect) {
                        $scope.bLockSelect = false;
                        MappingStateService.getState().selectionLock = false;
                    } else {
                        //Check if geographies and levels match:  "SAHSU" === "SAHSU"
                        var g1 = $scope.child.tileInfo["diseasemap1"];
                        var g2 = $scope.child.tileInfo["diseasemap2"];
                        if (g1.geography !== g2.geography) {
                            //different geographies     
                            $scope.showWarning("Cannot link selections for different geographies: " + g1.geography + " & " + g2.geography);
                        } else {
                            if (g1.level !== g2.level) {
                                //different levels
                                $scope.showWarning("Cannot link selections for different geolevels: " + g1.level + " & " + g2.level);
                            } else {
                                $scope.bLockSelect = true;
                                MappingStateService.getState().selectionLock = true;
                            }
                        }
                    }
                };

                //use current symbology on left map on the right map as well
                $scope.copySymbology = function () {
                    ChoroService.getMaps("diseasemap2").renderer = angular.copy(ChoroService.getMaps("diseasemap1").renderer);
                    $scope.child.refresh("diseasemap2");
                };

                /*
                 * Watch for Changes
                 */

                $scope.updateMapSelection = function (data, mapID) {
                    dropLine(mapID, data, true);
                };
                $scope.$on('syncMapping2Events', function (event, data) {
                    if (data.selected !== null && !angular.isUndefined(data.selected)) {
                        dropLine(data.mapID, data.selected.gid, data.map);
                        MappingStateService.getState().area_id[data.mapID] = data.selected.gid;
                        //update the other map if selections locked
                        if ($scope.bLockSelect) {
                            var otherMap = getOtherMap(data.mapID);
                            $scope.thisPoly[otherMap] = data.selected.gid;
                            MappingStateService.getState().area_id[otherMap] = data.selected.gid;
                            dropLine(otherMap, data.selected.gid, false);
                            $scope.child.infoBox2[otherMap].update($scope.thisPoly[otherMap]);
                        }
                    }
                });

                //draw the drop line in d3 and poly on map
                function dropLine(mapID, gid, map) {
                    //update map
                    if (map) {
                        $scope.thisPoly[mapID] = gid;
                        $scope.geoJSON[mapID].eachLayer($scope.child.handleLayer);
                        $scope.child.infoBox2[mapID].update($scope.thisPoly[mapID]);
                        if ($scope.bLockSelect) {
                            var otherMap = getOtherMap(mapID);
                            $scope.thisPoly[otherMap] = gid;
                            if (!angular.isUndefined($scope.geoJSON[otherMap])) {
                                $scope.geoJSON[otherMap].eachLayer($scope.child.handleLayer);
                            }
                        }
                    }
                    //update chart
                    $scope.$broadcast('rrDropLineRedraw', gid, mapID);
                }

                //switch to work on the other map        
                function getOtherMap(id) {
                    var otherMapID = "diseasemap2";
                    if ($scope.myMaps.indexOf(id) === 1) {
                        otherMapID = "diseasemap1";
                    }
                    return otherMapID;
                }
            }]);         