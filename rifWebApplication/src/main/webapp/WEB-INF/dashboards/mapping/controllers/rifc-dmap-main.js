/* global L, key, topojson, d3 */

angular.module("RIF")
        .controller('MappingCtrl', ['$scope', 'LeafletBaseMapService', 'leafletData', '$timeout', 'MappingStateService', 'ChoroService', 'user', 'LeafletExportService',
            function ($scope, LeafletBaseMapService, leafletData, $timeout, MappingStateService, ChoroService, user, LeafletExportService) {

                /*
                 //Texture fill for probability
                 var stripes = new L.StripePattern({color: 'blue', spaceColor: 'red', spaceOpacity: 1.0});
                 stripes.options.spaceColor = ChoroService.getRenderFeature(thisMap[mapID].scale, attr[mapID], false);
                 fillPattern: stripes
                 stripes.addTo(map);
                 */

                //geoJSON on diseasemap1
                var maxbounds;

                //1 is left panel, 2 is right panel
                var myMaps = ["diseasemap1", "diseasemap2"];

                //renderers defined by choropleth map service
                var thisMap = {
                    "diseasemap1": [],
                    "diseasemap2": []
                };

                //attribute being mapped
                var attr = {
                    "diseasemap1": "",
                    "diseasemap2": ""
                };
                //set by slider
                $scope.transparency = {
                    "diseasemap1": MappingStateService.getState().transparency["diseasemap1"],
                    "diseasemap2": MappingStateService.getState().transparency["diseasemap2"]
                };

                //get the default basemap
                $scope.thisLayer = {
                    diseasemap1: LeafletBaseMapService.setBaseMap(LeafletBaseMapService.getCurrentBaseMapInUse("diseasemap1")),
                    diseasemap2: LeafletBaseMapService.setBaseMap(LeafletBaseMapService.getCurrentBaseMapInUse("diseasemap2"))
                };
                //target for getTiles topoJSON
                $scope.geoJSON = {};

                //selected polygons
                $scope.thisPoly = {
                    'diseasemap1': MappingStateService.getState().area_id["diseasemap1"],
                    'diseasemap2': MappingStateService.getState().area_id["diseasemap2"]
                };

                //attributes for d3
                $scope.tableData = {
                    "diseasemap1": [],
                    "diseasemap2": []
                };

                //options for d3 directives
                $scope.optionsRR1 = {
                    this: "optionsRR1",
                    panel: "diseasemap1",
                    x_field: "x_order",
                    risk_field: "rr",
                    cl_field: "cl",
                    cu_field: "ul",
                    label_field: "",
                    zoomStart: MappingStateService.getState().brushStartLoc['diseasemap1'],
                    zoomEnd: MappingStateService.getState().brushEndLoc['diseasemap1']
                };
                $scope.optionsRR2 = {
                    this: "optionsRR2",
                    panel: "diseasemap2",
                    x_field: "x_order",
                    risk_field: "rr",
                    cl_field: "cl",
                    cu_field: "ul",
                    label_field: "",
                    zoomStart: MappingStateService.getState().brushStartLoc['diseasemap2'],
                    zoomEnd: MappingStateService.getState().brushEndLoc['diseasemap2']
                };

                //ui-container sizes
                $scope.currentWidth1 = 100;
                $scope.currentHeight1 = 100;
                $scope.currentWidth2 = 100;
                $scope.currentHeight2 = 100;
                $scope.vSplit1 = MappingStateService.getState().vSplit1;
                $scope.hSplit1 = MappingStateService.getState().hSplit1;
                $scope.hSplit2 = MappingStateService.getState().hSplit2;

                $scope.$on('ui.layout.loaded', function () {
                    $scope.currentWidth1 = d3.select("#rr1").node().getBoundingClientRect().width;
                    $scope.currentHeight1 = d3.select("#rr1").node().getBoundingClientRect().height;
                    $scope.currentWidth2 = d3.select("#rr2").node().getBoundingClientRect().width;
                    $scope.currentHeight2 = d3.select("#rr2").node().getBoundingClientRect().height;
                });

                $scope.$on('ui.layout.resize', function (e, beforeContainer, afterContainer) {
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
                    rescaleLeafletContainer();
                });

                //Drop-downs
                $scope.studyIDs = [1];
                $scope.studyID = $scope.studyIDs[0];
                $scope.years = [1990, 1991, 1992, 1993];
                $scope.year = $scope.years[0];
                $scope.sexes = ["Male", "Female", "Both"];
                $scope.sex = $scope.sexes[0];

                //Rescale leaflet container       
                function rescaleLeafletContainer() {
                    for (var i in myMaps) {
                        leafletData.getMap(myMaps[i]).then(function (map) {
                            setTimeout(function () {
                                map.invalidateSize();
                            }, 50);
                        });
                    }
                }

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

                //define non-null centres for leaflet directive
                $scope.center1 = {};
                $scope.center2 = {};

                //sync map selections
                $scope.bLockSelect = MappingStateService.getState().selectionLock;
                $scope.lockSelect = function () {
                    if ($scope.bLockSelect) {
                        $scope.bLockSelect = false;
                        MappingStateService.getState().selectionLock = false;
                    } else {
                        //TODO: check if geographies match:  "SAHSU" === "SAHSU"
                        $scope.bLockSelect = true;
                        MappingStateService.getState().selectionLock = true;
                    }
                };

                //use current symbology on left map on the right map as well
                $scope.copySymbology = function () {
                    thisMap["diseasemap2"] = angular.copy(thisMap["diseasemap1"]);
                    ChoroService.getMaps(2).renderer = ChoroService.getMaps(1).renderer;
                    $scope.refresh("diseasemap2");
                };

                //map layer opacity with slider
                $scope.changeOpacity = function (mapID) {
                    MappingStateService.getState().transparency[mapID] = $scope.transparency[mapID];
                    $scope.geoJSON[mapID].eachLayer(handleLayer);
                };

                //Zoom to layer
                $scope.zoomToExtent = function (mapID) {
                    leafletData.getMap(mapID).then(function (map) {
                        map.fitBounds(maxbounds);
                    });
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
                                    LeafletExportService.getLeafletExport(mapID, "DiseaseMap" + (myMaps.indexOf(mapID) + 1), canvas, thisScaleCanvas);
                                }
                            });
                        }
                    });
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
                        infoBox2[mapID].update($scope.thisPoly[toClear[i]]);
                        updateMapSelection(null, toClear[i]);
                    }
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

                $scope.studyDropsChanged1 = function () {
                    user.getTiles(user.currentUser, "SAHSU", "LEVEL4", "diseasemap1").then(handleTopoJSON, handleTopoJSONError);
                };
                $scope.studyDropsChanged2 = function () {
                    user.getTiles(user.currentUser, "SAHSU", "LEVEL4", "diseasemap2").then(handleTopoJSON, handleTopoJSONError);
                };

                //TODO: hard-typed, will be on drop changes
                $scope.studyDropsChanged1();
                $scope.studyDropsChanged2();

                function handleTopoJSON(res) {
                    var mapID = res.config.leaflet;
                    leafletData.getMap(mapID).then(function (map) {
                        map.keyboard.disable();
                        $scope.geoJSON[mapID] = new L.TopoJSON(res.data, {
                            renderer: L.canvas(),
                            map_id: mapID,
                            style: function (feature) {
                                return({
                                    weight: 1,
                                    color: "gray",
                                    fillColor: "#9BCD9B",
                                    fillOpacity: $scope.transparency[mapID]
                                });
                            },
                            onEachFeature: function (feature, layer) {
                                layer.on('mouseover', function (e) {
                                    var polyStyle = ChoroService.getRenderFeature2(layer.feature, "",
                                            thisMap[mapID].scale, "", $scope.thisPoly[mapID]);
                                    layer.setStyle({
                                        weight: polyStyle[2],
                                        color: polyStyle[1],
                                        fillColor: "lightgreen",
                                        fillOpacity: function () {
                                            return($scope.transparency[mapID] - 0.3 > 0 ? $scope.transparency[mapID] - 0.3 : 0.1);
                                        }()
                                    });
                                    infoBox[mapID].update(layer.feature.properties.area_id);
                                });
                                layer.on('click', function (e) {
                                    $scope.thisPoly[mapID] = e.target.feature.properties.area_id;
                                    MappingStateService.getState().area_id[mapID] = e.target.feature.properties.area_id;
                                    infoBox2[mapID].update($scope.thisPoly[mapID]);
                                    updateMapSelection($scope.thisPoly[mapID], mapID);
                                    if ($scope.bLockSelect) {
                                        var otherMap = getOtherMap(mapID);
                                        $scope.thisPoly[otherMap] = e.target.feature.properties.area_id;
                                        MappingStateService.getState().area_id[otherMap] = e.target.feature.properties.area_id;
                                        dropLine(otherMap, e.target.feature.properties.area_id, true);
                                    }
                                });
                                layer.on('mouseout', function (e) {
                                    $scope.geoJSON[mapID].eachLayer(handleLayer);
                                    infoBox[mapID].update(false);
                                });
                            }
                        });
                        $scope.geoJSON[mapID].addTo(map);
                        //use max bounds from map 1 only
                        if (mapID === "diseasemap1") {
                            maxbounds = $scope.geoJSON[mapID].getBounds();
                            if (MappingStateService.getState().center["diseasemap1"].lng === 0) {
                                leafletData.getMap("diseasemap1").then(function (map) {
                                    map.fitBounds(maxbounds);
                                });
                            }
                        }
                    }).then(function () {
                        $scope.getAttributeTable(mapID);
                    }).then(function () {
                        $scope.renderMap(mapID);
                        $scope.refresh(mapID);
                    });
                }

                function handleTopoJSONError() {
                    $scope.showError("Something went wrong when getting the geography");
                }

                //apply renderer to layer
                function handleLayer(layer) {
                    var mapID = layer.options.map_id;
                    if ($scope.tableData[mapID].length !== 0) {
                        var thisAttr;
                        for (var i = 0; i < $scope.tableData[mapID].length; i++) {
                            if ($scope.tableData[mapID][i].area_id === layer.feature.properties.area_id) {
                                thisAttr = $scope.tableData[mapID][i][attr[mapID]];
                                break;
                            }
                        }
                        var polyStyle = ChoroService.getRenderFeature2(layer.feature, thisAttr,
                                thisMap[mapID].scale, attr[mapID], $scope.thisPoly[mapID]);

                        layer.setStyle({
                            weight: polyStyle[2],
                            color: polyStyle[1],
                            fillColor: polyStyle[0],
                            fillOpacity: $scope.transparency[mapID]
                        });
                    }
                }

                $scope.refresh = function (mapID) {
                    var indx = myMaps.indexOf(mapID);
                    //get selected colour ramp
                    var rangeIn = ChoroService.getMaps(indx + 1).brewer;
                    attr[mapID] = ChoroService.getMaps(indx + 1).feature;
                    //not a choropleth, but single colour
                    if (rangeIn.length === 1) {
                        attr[mapID] = "";
                        leafletData.getMap(mapID).then(function (map) {
                            //remove existing legend
                            if (legend[mapID]._map) {
                                map.removeControl(legend[mapID]);
                            }
                        });
                        $scope.geoJSON[mapID].eachLayer(handleLayer);
                        return;
                    }

                    //get choropleth map renderer
                    thisMap[mapID] = ChoroService.getMaps(indx + 1).renderer;

                    //remove old legend and add new
                    legend[mapID].onAdd = ChoroService.getMakeLegend(thisMap[mapID], attr[mapID]);
                    leafletData.getMap(mapID).then(function (map) {
                        if (legend[mapID]._map) { //This may break in future leaflet versions
                            map.removeControl(legend[mapID]);
                        }
                        legend[mapID].addTo(map);
                    });

                    //force a redraw
                    $scope.geoJSON[mapID].eachLayer(handleLayer);
                    getRRchart(mapID, attr[mapID]);
                };

                //change the basemaps - called from modal
                $scope.renderMap = function (mapID) {
                    leafletData.getMap(mapID).then(function (map) {
                        //remove baselayer
                        map.removeLayer($scope.thisLayer[mapID]);
                        //add new baselayer if requested
                        if (!LeafletBaseMapService.getNoBaseMap(mapID)) {
                            $scope.thisLayer[mapID] = LeafletBaseMapService.setBaseMap(LeafletBaseMapService.getCurrentBaseMapInUse(mapID));
                            map.addLayer($scope.thisLayer[mapID]);
                        }
                    });
                };

                //draw rr chart from d3 directive 'rrZoom'
                function getRRchart(mapID, attribute) {
                    //make array for d3 areas
                    var rs = [];
                    for (var i = 0; i < $scope.tableData[mapID].length; i++) {
                        //Handle inconsistant naming in results table
                        //Handle if there are no confidence intervals
                        var ciString = ["lower95", "upper95"];
                        if (attribute === "smoothed_smr") {
                            ciString = ["smoothed_smr_lower95", "smoothed_smr_upper95"];
                        }
                        rs.push(
                                {
                                    name: attribute,
                                    gid: $scope.tableData[mapID][i].area_id,
                                    x_order: i,
                                    rr: $scope.tableData[mapID][i][attribute],
                                    cl: function () {
                                        if (attribute !== "posterior_probability") {
                                            return $scope.tableData[mapID][i][ciString[0]];
                                        } else {
                                            return $scope.tableData[mapID][i][attribute];
                                        }
                                    }(),
                                    ul: function () {
                                        if (attribute !== "posterior_probability") {
                                            return $scope.tableData[mapID][i][ciString[1]];
                                        } else {
                                            return $scope.tableData[mapID][i][attribute];
                                        }
                                    }()
                                }
                        );
                    }

                    //reorder
                    rs.sort(function (a, b) {
                        return parseFloat(a.rr) - parseFloat(b.rr);
                    });
                    for (var i = 0; i < $scope.tableData[mapID].length; i++) {
                        rs[i]["x_order"] = i + 1;
                    }
                    //set options for directive
                    if (mapID === "diseasemap1") {
                        $scope.optionsRR1.label_field = attribute;
                        $scope.rrChartData1 = angular.copy(rs);

                    } else {
                        $scope.optionsRR2.label_field = attribute;
                        $scope.rrChartData2 = angular.copy(rs);
                    }
                }

                $scope.getAttributeTable = function (mapID) {

                    user.getSmoothedResults(user.currentUser, 1, 1, 1990).then(handleSmoothedResults, attributeError); //TODO: hardtyped

                    function handleSmoothedResults(res) {
                        //variables possible to map
                        var attrs = ["smoothed_smr", "relative_risk", "posterior_probability"];
                        ChoroService.getMaps(myMaps.indexOf(mapID) + 1).features = attrs;

                        //make array for choropleth
                        $scope.tableData[mapID].length = 0;
                        for (var i = 0; i < res.data.smoothed_results.length; i++) {
                            $scope.tableData[mapID].push(res.data.smoothed_results[i]);
                        }

                        //TODO: Adding fake PP data
                        for (var i = 0; i < $scope.tableData[mapID].length; i++) {
                            $scope.tableData[mapID][i]['posterior_probability'] = Math.random();
                        }
                        ////

                        $scope.refresh(mapID);
                    }

                    function attributeError(e) {
                        $scope.showError("Something went wrong when getting the attribute data");
                    }
                };

                //add events to ui-leaflet
                $timeout(function () {
                    leafletData.getMap("diseasemap1").then(function (map) {
                        map.on('zoomend', function (e) {
                            MappingStateService.getState().center["diseasemap1"].zoom = map.getZoom();
                            MappingStateService.getState().center["diseasemap1"].lng = map.getCenter().lng;
                            MappingStateService.getState().center["diseasemap1"].lat = map.getCenter().lat;
                        });
                        map.on('moveend', function (e) {
                            MappingStateService.getState().center["diseasemap1"].zoom = map.getZoom();
                            MappingStateService.getState().center["diseasemap1"].lng = map.getCenter().lng;
                            MappingStateService.getState().center["diseasemap1"].lat = map.getCenter().lat;
                        });
                        new L.Control.GeoSearch({
                            provider: new L.GeoSearch.Provider.OpenStreetMap()
                        }).addTo(map);
                        L.control.scale({position: 'topleft', imperial: false}).addTo(map);
                    });
                    leafletData.getMap("diseasemap2").then(function (map) {
                        map.on('zoomend', function (e) {
                            MappingStateService.getState().center["diseasemap2"].zoom = map.getZoom();
                            MappingStateService.getState().center["diseasemap2"].lng = map.getCenter().lng;
                            MappingStateService.getState().center["diseasemap2"].lat = map.getCenter().lat;
                        });
                        map.on('moveend', function (e) {
                            MappingStateService.getState().center["diseasemap2"].zoom = map.getZoom();
                            MappingStateService.getState().center["diseasemap2"].lng = map.getCenter().lng;
                            MappingStateService.getState().center["diseasemap2"].lat = map.getCenter().lat;
                        });
                        new L.Control.GeoSearch({
                            provider: new L.GeoSearch.Provider.OpenStreetMap()
                        }).addTo(map);
                        L.control.scale({position: 'topleft', imperial: false}).addTo(map);
                    });

                    //Set initial map extents
                    if ($scope.bLockCenters) {
                        $scope.center1 = MappingStateService.getState().center["diseasemap1"];
                        $scope.center2 = $scope.center1;
                    } else {
                        $scope.center1 = MappingStateService.getState().center["diseasemap1"];
                        $scope.center2 = MappingStateService.getState().center["diseasemap2"];
                    }
                });

                //key events
                $scope.mapInFocus = "";
                angular.element(document).bind('keydown', function (e) {
                    if ($scope.mapInFocus !== "") {
                        if (e.keyCode === 37 || e.keyCode === 39) { //left || right
                            $scope.$broadcast('rrKeyEvent', false, e.keyCode, $scope.mapInFocus);
                        }
                    }
                });
                angular.element(document).bind('keyup', function (e) {
                    if ($scope.mapInFocus !== "") {
                        if (e.keyCode === 37 || e.keyCode === 39) { //left || right
                            $scope.$broadcast('rrKeyEvent', true, e.keyCode, $scope.mapInFocus);
                        }
                    }
                });

                //Legends and Infoboxes
                var legend = {
                    'diseasemap1': L.control({position: 'topright'}),
                    'diseasemap2': L.control({position: 'topright'})
                };
                var infoBox = {
                    'diseasemap1': L.control({position: 'bottomright'}),
                    'diseasemap2': L.control({position: 'bottomright'})
                };
                var infoBox2 = {
                    'diseasemap1': L.control({position: 'bottomleft'}),
                    'diseasemap2': L.control({position: 'bottomleft'})
                };

                infoBox[myMaps[0]].onAdd = function () {
                    this._div = L.DomUtil.create('div', 'info');
                    this.update();
                    return this._div;
                };
                infoBox[myMaps[0]].update = function (poly) {
                    if (poly) {
                        var thisAttr;
                        for (var i = 0; i < $scope.tableData[myMaps[0]].length; i++) {
                            if ($scope.tableData[myMaps[0]][i].area_id === poly) {
                                thisAttr = $scope.tableData[myMaps[0]][i][attr[myMaps[0]]];
                                break;
                            }
                        }
                        if (ChoroService.getMaps(1).feature !== "") {
                            this._div.innerHTML = '<h4>ID: ' + poly + '</br>' + ChoroService.getMaps(1).feature.toUpperCase().replace("_", " ") + ": " + Number(thisAttr).toFixed(3) + '</h4>';
                        } else {
                            this._div.innerHTML = '<h4>ID: ' + poly + '</h4>';
                        }
                    } else {
                        this._div.innerHTML = '';
                    }
                };
                infoBox[myMaps[1]].onAdd = function () {
                    this._div = L.DomUtil.create('div', 'info');
                    this.update();
                    return this._div;
                };
                infoBox[myMaps[1]].update = function (poly) {
                    if (poly) {
                        var thisAttr;
                        for (var i = 0; i < $scope.tableData[myMaps[1]].length; i++) {
                            if ($scope.tableData[myMaps[1]][i].area_id === poly) {
                                thisAttr = $scope.tableData[myMaps[1]][i][[attr[myMaps[1]]]];
                                break;
                            }
                        }
                        if (ChoroService.getMaps(2).feature !== "") {
                            this._div.innerHTML = '<h4>ID: ' + poly + '</br>' + ChoroService.getMaps(2).feature.toUpperCase().replace("_", " ") + ": " + Number(thisAttr).toFixed(3) + '</h4>';
                        } else {
                            this._div.innerHTML = '<h4>ID: ' + poly + '</h4>';
                        }
                    } else {
                        this._div.innerHTML = '';
                    }
                };
                infoBox2[myMaps[0]].onAdd = function () {
                    this._div = L.DomUtil.create('div', 'info');
                    this.update();
                    return this._div;
                };
                infoBox2[myMaps[0]].update = function (poly) {
                    if (poly === null) {
                        this._div.innerHTML = "";
                    } else {
                        var results = null;
                        for (var i = 0; i < $scope.tableData[myMaps[0]].length; i++) {
                            if ($scope.tableData[myMaps[0]][i].area_id === poly) {
                                results = $scope.tableData[myMaps[0]][i];
                            }
                        }
                        if (results !== null) {
                            this._div.innerHTML =
                                    '<h4>ID: ' + poly + '</br>' +
                                    'Population: ' + results.population + '</br>' +
                                    'Observed: ' + results.observed + '</br>' +
                                    'Expected: ' + Number(results.expected).toFixed(2) + '</br>' + '</h4>';
                        }
                    }
                };
                infoBox2[myMaps[1]].onAdd = function () {
                    this._div = L.DomUtil.create('div', 'info');
                    this.update();
                    return this._div;
                };
                infoBox2[myMaps[1]].update = function (poly) {
                    if (poly === null) {
                        this._div.innerHTML = "";
                    } else {
                        var results = null;
                        for (var i = 0; i < $scope.tableData[myMaps[1]].length; i++) {
                            if ($scope.tableData[myMaps[1]][i].area_id === poly) {
                                results = $scope.tableData[myMaps[1]][i];
                            }
                        }
                        if (results !== null) {
                            this._div.innerHTML =
                                    '<h4>ID: ' + poly + '</br>' +
                                    'Population: ' + results.population + '</br>' +
                                    'Observed: ' + results.observed + '</br>' +
                                    'Expected: ' + Number(results.expected).toFixed(2) + '</br>' + '</h4>';
                        }
                    }
                };
                leafletData.getMap(myMaps[0]).then(function (map) {
                    infoBox2[myMaps[0]].addTo(map);
                    infoBox[myMaps[0]].addTo(map);
                });
                leafletData.getMap(myMaps[1]).then(function (map) {
                    infoBox2[myMaps[1]].addTo(map);
                    infoBox[myMaps[1]].addTo(map);
                });

                //Synchronisation
                function updateMapSelection(data, mapID) {
                    dropLine(mapID, data, true);
                }
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
                        }
                    }
                });

                //draw the drop line in d3 and poly on map
                function dropLine(mapID, gid, map) {
                    //update map
                    if (map) {
                        $scope.thisPoly[mapID] = gid;
                        $scope.geoJSON[mapID].eachLayer(handleLayer);
                        infoBox2[mapID].update($scope.thisPoly[mapID]);
                        if ($scope.bLockSelect) {
                            var otherMap = getOtherMap(mapID);
                            $scope.thisPoly[otherMap] = gid;
                            $scope.geoJSON[otherMap].eachLayer(handleLayer);
                        }
                    }
                    //update chart
                    $scope.$broadcast('rrDropLineRedraw', gid, mapID);
                }

                //switch to work on the other map        
                function getOtherMap(id) {
                    var otherMapID = "diseasemap2";
                    if (myMaps.indexOf(id) === 1) {
                        otherMapID = "diseasemap1";
                    }
                    return otherMapID;
                }
            }]);         