/*
 * CONTROLLER to handle map panels
 */

/* global L */

//TODO: remove year from getSmoothedResults

angular.module("RIF")
        .controller('leafletLayersCtrl', ['$scope', 'user', 'LeafletBaseMapService', 'leafletData', 'ChoroService',
            'MappingStateService', 'ViewerStateService', 'MappingService',
            function ($scope, user, LeafletBaseMapService, leafletData, ChoroService,
                    MappingStateService, ViewerStateService, MappingService) {

                //Reference the parent scope
                var parentScope = $scope.$parent;
                parentScope.child = $scope;
                //Reference the state service
                $scope.myService = MappingStateService;
                if ($scope.myMaps[0] === "viewermap") {
                    $scope.myService = ViewerStateService;
                }

                //Legends and Infoboxes
                $scope.legend = {
                    'diseasemap1': L.control({position: 'topright'}),
                    'diseasemap2': L.control({position: 'topright'}),
                    'viewermap': L.control({position: 'topright'})
                };
                var infoBox = {
                    'diseasemap1': L.control({position: 'bottomright'}),
                    'diseasemap2': L.control({position: 'bottomright'}),
                    'viewermap': L.control({position: 'bottomright'})
                };
                $scope.infoBox2 = {
                    'diseasemap1': L.control({position: 'bottomleft'}),
                    'diseasemap2': L.control({position: 'bottomleft'}),
                    'viewermap': null
                };
                //Handle UI-Layout resize events
                $scope.$on('ui.layout.loaded', function () {
                    $scope.getD3Frames();
                });
                $scope.$on('ui.layout.resize', function (e, beforeContainer, afterContainer) {
                    $scope.getD3FramesOnResize(beforeContainer, afterContainer);
                });
                //Rescale leaflet container       
                $scope.rescaleLeafletContainer = function () {
                    for (var i in $scope.myMaps) {
                        leafletData.getMap($scope.myMaps[i]).then(function (map) {
                            setTimeout(function () {
                                map.invalidateSize();
                            }, 50);
                        });
                    }
                };
                //the default basemap              
                $scope.thisLayer = {
                    "diseasemap1": LeafletBaseMapService.setBaseMap(LeafletBaseMapService.getCurrentBaseMapInUse("diseasemap1")),
                    "diseasemap2": LeafletBaseMapService.setBaseMap(LeafletBaseMapService.getCurrentBaseMapInUse("diseasemap2")),
                    "viewermap": LeafletBaseMapService.setBaseMap(LeafletBaseMapService.getCurrentBaseMapInUse("viewermap"))
                };
                //metadata of TopoJSON being mapped
                $scope.tileInfo = {
                    "diseasemap1": {'geography': null, 'level': null},
                    "diseasemap2": {'geography': null, 'level': null},
                    "viewermap": {'geography': null, 'level': null}
                };

                //attribute being mapped
                $scope.attr = {
                    "diseasemap1": "",
                    "diseasemap2": "",
                    "viewermap": ""
                };
                //renderers defined by choropleth map service             
                var thisMap = {
                    "diseasemap1": [],
                    "diseasemap2": [],
                    "viewermap": []
                };
                $scope.tableData = {
                    "diseasemap1": [],
                    "diseasemap2": [],
                    "viewermap": []
                };
                $scope.studyIDs = [];
                $scope.sexes = {
                    "diseasemap1": [],
                    "diseasemap2": [],
                    "viewermap": []
                };
                /*
                 * Tidy up on error
                 */
                function clearTheMapOnError(mapID) {
                    //on error, remove polygon, legends and D3 charts
                    if (mapID === "viewermap") {
                        //datatable
                        $scope.viewerTableOptions.data.length = 0;
                        //pyramid
                        $scope.yearPop = null;
                        $scope.yearsPop.length = 0;
                        $scope.populationData["viewermap"].length = 0;
                        //histogram                                                                   
                        $scope.histoData["viewermap"].length = 0;
                    } else {
                        $scope.thisPoly[mapID] = null;
                        if (!angular.isUndefined($scope.infoBox2[mapID].update)) {
                            $scope.infoBox2[mapID].update(null);
                        }
                        $scope.rrChartData[mapID].length = 0;
                    }

                    leafletData.getMap(mapID).then(function (map) {
                        if (map.hasLayer($scope.geoJSON[mapID])) {
                            map.removeLayer($scope.geoJSON[mapID]);
                            if ($scope.legend[mapID]._map) {
                                map.removeControl($scope.legend[mapID]);
                            }
                        }
                    });
                }

                //Get the possible studies
                $scope.getStudies = function () {
                    user.getCurrentStatusAllStudies(user.currentUser).then(function (res) {
                        //  $scope.studyIDs.length = 0;
                        for (var i = 0; i < res.data.smoothed_results.length; i++) {
                            if (res.data.smoothed_results[i].study_state === "R") {
                                var thisStudy = {
                                    "study_id": res.data.smoothed_results[i].study_id,
                                    "name": res.data.smoothed_results[i].study_name
                                };
                                $scope.studyIDs.push(thisStudy);
                            }
                        }
                        //sort array on ID
                        $scope.studyIDs.sort(function (a, b) {
                            return parseFloat(a.study_id) - parseFloat(b.study_id);
                        });
                        //Remember defaults
                        for (var j = 0; j < $scope.myMaps.length; j++) {
                            var s = $scope.myService.getState().study[$scope.myMaps[j]].study_id;
                            if (s !== null) {
                                for (var i = 0; i < $scope.studyIDs.length; i++) {
                                    if ($scope.studyIDs[i].study_id === s) {
                                        $scope.$parent.studyID[$scope.myMaps[j]] = $scope.studyIDs[i];
                                    }
                                }
                            } else {
                                $scope.$parent.studyID[$scope.myMaps[j]] = $scope.studyIDs[$scope.studyIDs.length - 1];
                            }
                        }
                        //update sex drop-down
                        for (var j = 0; j < $scope.myMaps.length; j++) {
                            $scope.updateSex($scope.myMaps[j]);
                        }
                    }, function (e) {
                        $scope.showError("Could not retrieve study status");
                    });
                };
                $scope.updateSex = function (mapID) {
                    //Store this study selection
                    $scope.myService.getState().study[mapID] = $scope.studyID[mapID];
                    //Get the sexes for this study
                    user.getSexesForStudy(user.currentUser, $scope.studyID[mapID].study_id, mapID)
                            .then(handleSexes, clearTheMapOnError(mapID));
                    function handleSexes(res) {
                        $scope.sexes[res.config.leaflet].length = 0;
                        if (!angular.isUndefined(res.data[0].names)) {
                            for (var i = 0; i < res.data[0].names.length; i++) {
                                $scope.sexes[res.config.leaflet].push(res.data[0].names[i]);
                            }
                        }
                        //if no preselection, then set dropdown to last one in list         
                        if ($scope.sex[res.config.leaflet] === null) {
                            $scope.sex[res.config.leaflet] = $scope.sexes[res.config.leaflet][0];
                        }

                        //dashboard specific
                        if (mapID === "viewermap") {
                            //update pyramid if in viewer
                            $scope.child.fillPyramidData();
                        } else {
                            //check selection link is possible
                            if ($scope.myService.getState().selectionLock) {
                                var g1 = $scope.tileInfo["diseasemap1"];
                                var g2 = $scope.tileInfo["diseasemap2"];
                                if (g1.geography !== null && g2.geography !== null) {
                                    if (g1.geography !== g2.geography) {
                                        //different geographies     
                                        $scope.showWarning("Cannot link selections for different geographies: " + g1.geography + " & " + g2.geography);
                                        $scope.myService.getState().selectionLock = false;
                                        $scope.$parent.bLockSelect = false;
                                    } else {
                                        if (g1.level !== g2.level) {
                                            //different levels
                                            $scope.showWarning("Cannot link selections for different geolevels: " + g1.level + " & " + g2.level);
                                        }
                                    }
                                }
                            }
                        }
                        $scope.updateStudy(res.config.leaflet);
                    }
                };

                //change the basemaps 
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
                //Draw the map
                $scope.refresh = function (mapID) {
                    //get selected colour ramp
                    var rangeIn = ChoroService.getMaps(mapID).renderer.range;
                    $scope.attr[mapID] = ChoroService.getMaps(mapID).feature;
                    //get choropleth map renderer
                    thisMap[mapID] = ChoroService.getMaps(mapID).renderer;
                    //not a choropleth, but single colour
                    if (rangeIn.length === 1) {
                        $scope.attr[mapID] = "";
                        leafletData.getMap(mapID).then(function (map) {
                            //remove existing legend
                            if ($scope.legend[mapID]._map) {
                                map.removeControl($scope.legend[mapID]);
                            }
                        });
                        $scope.geoJSON[mapID].eachLayer($scope.handleLayer);
                        return;
                    }

                    //remove old legend and add new
                    $scope.legend[mapID].onAdd = ChoroService.getMakeLegend(thisMap[mapID], $scope.attr[mapID]);
                    leafletData.getMap(mapID).then(function (map) {
                        if ($scope.legend[mapID]._map) { //This may break in future leaflet versions
                            map.removeControl($scope.legend[mapID]);
                        }
                        $scope.legend[mapID].addTo(map);
                    });
                    //force a redraw
                    $scope.geoJSON[mapID].eachLayer($scope.handleLayer);
                    //GET D3...
                    $scope.getD3chart(mapID, $scope.attr[mapID]);
                };
                //apply relevent renderer to layer
                $scope.handleLayer = function (layer) {
                    var mapID = layer.options.map_id;
                    if (mapID === "viewermap") {
                        //Join geography and results table
                        var thisAttr;
                        for (var i = 0; i < $scope.viewerTableOptions.data.length; i++) {
                            if ($scope.viewerTableOptions.data[i].area_id === layer.feature.properties.area_id) {
                                thisAttr = $scope.viewerTableOptions.data[i][ChoroService.getMaps("viewermap").feature];
                                break;
                            }
                        }
                        //is selected?
                        var selected = false;
                        if (angular.isArray($scope.thisPoly) && $scope.thisPoly.indexOf(layer.feature.properties.area_id) !== -1) {
                            selected = true;
                        }
                        var polyStyle = ChoroService.getRenderFeature(thisMap["viewermap"].scale, thisAttr, selected);
                        layer.setStyle({
                            fillColor: polyStyle,
                            fillOpacity: $scope.child.transparency[mapID]
                        });
                    } else {
                        if ($scope.tableData[mapID].length !== 0) {
                            var thisAttr;
                            for (var i = 0; i < $scope.tableData[mapID].length; i++) {
                                if ($scope.tableData[mapID][i].area_id === layer.feature.properties.area_id) {
                                    thisAttr = $scope.tableData[mapID][i][$scope.attr[mapID]];
                                    break;
                                }
                            }
                            var polyStyle = ChoroService.getRenderFeature2(layer.feature, thisAttr,
                                    thisMap[mapID].scale, $scope.attr[mapID], $scope.thisPoly[mapID]);
                            layer.setStyle({
                                weight: polyStyle[2],
                                color: polyStyle[1],
                                fillColor: polyStyle[0],
                                fillOpacity: $scope.child.transparency[mapID]
                            });
                        }
                    }
                };
                $scope.updateStudy = function (mapID) {
                    //Check inputs are valid
                    if ($scope.studyID[mapID] === null || $scope.sex[mapID] === null) {
                        $scope.showError("Invalid study or sex code");
                        clearTheMapOnError(mapID);
                    } else {
                        //Reset all renderers, but only if not called from state change
                        if (!$scope.myService.getState().initial) {
                            ChoroService.resetState(mapID);
                            thisMap[mapID] = ChoroService.getMaps(mapID).renderer;
                        }
                        $scope.myService.getState().initial = false;
                        //Remove RR chart
                        if (mapID !== "viewermap" && !angular.isUndefined($scope.rrChartData[mapID])) {
                            $scope.rrChartData[mapID].length = 0;
                        }

                        //Remove any existing geography
                        leafletData.getMap(mapID).then(function (map) {
                            if (map.hasLayer($scope.geoJSON[mapID])) {
                                map.removeLayer($scope.geoJSON[mapID]);
                            }
                        });
                        //save study, sex selection
                        $scope.myService.getState().sex[mapID] = $scope.sex[mapID];
                        $scope.myService.getState().study[mapID] = $scope.$parent.studyID[mapID];
                        //add the requested geography
                        user.getGeographyAndLevelForStudy(user.currentUser, $scope.studyID[mapID].study_id).then(
                                function (res) {
                                    $scope.tileInfo[mapID].geography = res.data[0][0]; //e.g. SAHSU
                                    $scope.tileInfo[mapID].level = res.data[0][1]; //e.g. LEVEL3
                                    user.getTiles(user.currentUser, $scope.tileInfo[mapID].geography, $scope.tileInfo[mapID].level, mapID).then(
                                            handleTopoJSON, handleTopoJSONError);
                                }
                        );
                    }

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
                                        opacity: 1,
                                        color: "gray",
                                        fillColor: "#9BCD9B",
                                        fillOpacity: $scope.child.transparency[mapID]
                                    });
                                },
                                onEachFeature: function (feature, layer) {
                                    layer.on('mouseover', function (e) {
                                        this.setStyle({
                                            color: 'gray',
                                            weight: 1.5,
                                            fillOpacity: function () {
                                                return($scope.child.transparency[mapID] - 0.3 > 0 ? $scope.child.transparency[mapID] - 0.3 : 0.1);
                                            }()
                                        });
                                        infoBox[mapID].update(layer.feature.properties.area_id);
                                    });
                                    layer.on('click', function (e) {
                                        if (mapID === "viewermap") {
                                            //Multiple selections
                                            var thisPoly = e.target.feature.properties.area_id;
                                            var bFound = false;
                                            for (var i = 0; i < $scope.thisPoly.length; i++) {
                                                if ($scope.thisPoly[i] === thisPoly) {
                                                    bFound = true;
                                                    $scope.thisPoly.splice(i, 1);
                                                    break;
                                                }
                                            }
                                            if (!bFound) {
                                                $scope.thisPoly.push(thisPoly);
                                            }
                                        } else {
                                            //Single selections
                                            $scope.thisPoly[mapID] = e.target.feature.properties.area_id;
                                            $scope.myService.getState().area_id[mapID] = e.target.feature.properties.area_id;
                                            $scope.infoBox2[mapID].update($scope.thisPoly[mapID]);
                                            $scope.updateMapSelection($scope.thisPoly[mapID], mapID);
                                            if ($scope.bLockSelect) {
                                                var otherMap = MappingService.getOtherMap(mapID);
                                                $scope.thisPoly[otherMap] = e.target.feature.properties.area_id;
                                                $scope.myService.getState().area_id[otherMap] = e.target.feature.properties.area_id;
                                                dropLine(otherMap, e.target.feature.properties.area_id, true);
                                            }
                                        }
                                    });
                                    layer.on('mouseout', function (e) {
                                        $scope.geoJSON[mapID].eachLayer($scope.handleLayer);
                                        infoBox[mapID].update(false);
                                    });
                                }
                            });
                            $scope.geoJSON[mapID].addTo(map);
                            //do not get maxbounds for diseasemap2
                            if (mapID !== "diseasemap2") {
                                $scope.maxbounds = $scope.geoJSON[mapID].getBounds();
                                if ($scope.myService.getState().center[mapID].lng === 0) {
                                    leafletData.getMap(mapID).then(function (map) {
                                        map.fitBounds($scope.maxbounds);
                                    });
                                }
                            }
                        }).then(function () {
                            getAttributeTable(mapID);
                        }).then(function () {
                            $scope.renderMap(mapID);
                            $scope.refresh(mapID);
                        });
                    }

                    function handleTopoJSONError() {
                        $scope.showError("Something went wrong when getting the geography");
                    }

                    function getAttributeTable(mapID) {
                        user.getSmoothedResults(user.currentUser, $scope.studyID[mapID].study_id, MappingService.getSexCode($scope.sex[mapID]))
                                .then(function (res) {
                                    //variables possible to map
                                    var attrs = ["smoothed_smr", "relative_risk", "posterior_probability"];
                                    ChoroService.getMaps(mapID).features = attrs;
                                    //make array for choropleth
                                    $scope.tableData[mapID].length = 0;
                                    for (var i = 0; i < res.data.smoothed_results.length; i++) {
                                        $scope.tableData[mapID].push(res.data.smoothed_results[i]);
                                    }

                                    //supress some pre-selected columns (see the service)   
                                    if (mapID === "viewermap") {
                                        //fill results table for data viewer only
                                        var colDef = [];
                                        var attrs = [];
                                        //Add column for selected
                                        for (var i = 0; i < res.data.smoothed_results.length; i++) {
                                            res.data.smoothed_results[i]._selected = 0;
                                        }

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
                                        //draw and refresh histogram and table
                                        $scope.distHistoName = ChoroService.getMaps("viewermap").feature;
                                        ChoroService.getMaps("viewermap").features = attrs;
                                        $scope.viewerTableOptions.columnDefs = colDef;
                                        $scope.viewerTableOptions.data = $scope.tableData.viewermap;
                                        $scope.getD3chart("viewermap", $scope.attr["viewermap"]);
                                        $scope.updateTable();
                                    }
                                    $scope.refresh(mapID);
                                }, function (e) {
                                    console.log("Something went wrong when getting the attribute data");
                                    clearTheMapOnError(mapID);
                                });
                    }

                    /*
                     * INFO BOXES AND LEGEND
                     */
                    function hoverUpdateAttr(map, poly, feature) {
                        var tmp;
                        var inner;
                        for (var i = 0; i < $scope.tableData[map].length; i++) {
                            if ($scope.tableData[map][i].area_id === poly) {
                                tmp = $scope.tableData[map][i][$scope.attr[map]];
                                break;
                            }
                        }
                        if (feature !== "" && angular.isNumber(tmp)) {
                            inner = '<h4>ID: ' + poly + '</br>' + feature.toUpperCase().replace("_", " ") + ": " + Number(tmp).toFixed(3) + '</h4>';
                        } else {
                            inner = '<h4>ID: ' + poly + '</h4>';
                        }
                        return inner;
                    }

                    //Cannot seem to do this by other than declaring each explicitly
                    for (var i = 0; i < $scope.myMaps.length; i++) {
                        if ($scope.myMaps.indexOf("diseasemap1") !== -1) {
                            infoBox["diseasemap1"].onAdd = function () {
                                this._div = L.DomUtil.create('div', 'info');
                                this.update();
                                return this._div;
                            };
                            infoBox["diseasemap1"].update = function (poly) {
                                if (poly) {
                                    this._div.innerHTML = hoverUpdateAttr("diseasemap1", poly, ChoroService.getMaps("diseasemap1").feature);
                                } else {
                                    this._div.innerHTML = '';
                                }
                            };
                            $scope.infoBox2["diseasemap1"].onAdd = function () {
                                this._div = L.DomUtil.create('div', 'info');
                                this.update();
                                return this._div;
                            };
                            $scope.infoBox2["diseasemap1"].update = function (poly) {
                                if (poly === null) {
                                    this._div.innerHTML = "";
                                } else {
                                    var results = null;
                                    for (var i = 0; i < $scope.tableData["diseasemap1"].length; i++) {
                                        if ($scope.tableData["diseasemap1"][i].area_id === poly) {
                                            results = $scope.tableData["diseasemap1"][i];
                                        }
                                    }
                                    if (results !== null) {
                                        this._div.innerHTML =
                                                '<h4>ID: ' + poly + '</br>' +
                                                'Population: ' + results.population + '</br>' +
                                                'Observed: ' + results.observed + '</br>' +
                                                'Expected: ' + Number(results.expected).toFixed(2) + '</br>' + '</h4>';
                                    } else {
                                        this._div.innerHTML = "";
                                    }
                                }
                            };
                            leafletData.getMap("diseasemap1").then(function (map) {
                                $scope.infoBox2["diseasemap1"].addTo(map);
                                infoBox["diseasemap1"].addTo(map);
                            });
                        }
                        if ($scope.myMaps.indexOf("diseasemap2") !== -1) {
                            infoBox["diseasemap2"].onAdd = function () {
                                this._div = L.DomUtil.create('div', 'info');
                                this.update();
                                return this._div;
                            };
                            infoBox["diseasemap2"].update = function (poly) {
                                if (poly) {
                                    this._div.innerHTML = hoverUpdateAttr("diseasemap1", poly, ChoroService.getMaps("diseasemap1").feature);
                                } else {
                                    this._div.innerHTML = '';
                                }
                            };
                            $scope.infoBox2["diseasemap2"].onAdd = function () {
                                this._div = L.DomUtil.create('div', 'info');
                                this.update();
                                return this._div;
                            };
                            $scope.infoBox2["diseasemap2"].update = function (poly) {
                                if (poly === null) {
                                    this._div.innerHTML = "";
                                } else {
                                    var results = null;
                                    for (var i = 0; i < $scope.tableData["diseasemap2"].length; i++) {
                                        if ($scope.tableData["diseasemap2"][i].area_id === poly) {
                                            results = $scope.tableData["diseasemap2"][i];
                                        }
                                    }
                                    if (results !== null) {
                                        this._div.innerHTML =
                                                '<h4>ID: ' + poly + '</br>' +
                                                'Population: ' + results.population + '</br>' +
                                                'Observed: ' + results.observed + '</br>' +
                                                'Expected: ' + Number(results.expected).toFixed(2) + '</br>' + '</h4>';
                                    } else {
                                        this._div.innerHTML = "";
                                    }
                                }
                            };
                            leafletData.getMap("diseasemap2").then(function (map) {
                                $scope.infoBox2["diseasemap2"].addTo(map);
                                infoBox["diseasemap2"].addTo(map);
                            });
                        }
                        if ($scope.myMaps.indexOf("viewermap") !== -1) {
                            infoBox["viewermap"].onAdd = function () {
                                this._div = L.DomUtil.create('div', 'info');
                                this.update();
                                return this._div;
                            };
                            infoBox["viewermap"].update = function (poly) {
                                if (poly) {
                                    this._div.innerHTML = hoverUpdateAttr("viewermap", poly, ChoroService.getMaps("viewermap").feature);
                                } else {
                                    this._div.innerHTML = '';
                                }
                            };
                            leafletData.getMap("viewermap").then(function (map) {
                                infoBox["viewermap"].addTo(map);
                            });
                        }
                    }
                };
            }]);