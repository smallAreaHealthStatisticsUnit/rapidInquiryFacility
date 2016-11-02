/* global L, d3 */

angular.module("RIF")
        .controller('DiseaseMappingCtrl', ['$scope', 'leafletData', 'LeafletBaseMapService', '$timeout', 'MappingStateService', 'user', 'ChoroService', '$rootScope',
            function ($scope, leafletData, LeafletBaseMapService, $timeout, MappingStateService, user, ChoroService, $rootScope) {

                //ui-container sizes
                $scope.rrCurrentWidth = 100;
                $scope.rrCurrentHeight = 100;
                $scope.areaCurrentHeight = 500;
                $scope.areaCurrentWidth = 100;
                $scope.vSplit1 = MappingStateService.getState().vSplit1;
                $scope.hSplit1 = MappingStateService.getState().hSplit1;

                //Options for d3 directives
                $scope.optionsRR = {
                    x_field: "x_order",
                    risk_field: "rr", //for label
                    cl_field: "cl",
                    cu_field: "ul",
                    label_field: ""
                };

                $scope.optionsAreas = {
                    id_field: "gid", //TODO: not needed?
                    x_field: "x_order",
                    risk_field: "rr",
                    rSet: 4
                };

                //invalidate d3 areas for refresh on tab close
                $scope.$on("$destroy", function () {
                    MappingStateService.getState().cleanState = true;
                });

                //Drop-downs
                $scope.studyIDs = [1];
                $scope.studyID = $scope.studyIDs[0];
                $scope.years = [1990, 1991, 1992, 1993];
                $scope.year = $scope.years[0];
                $scope.sexes = ["Male", "Female", "Both"];
                $scope.sex = $scope.sexes[0];

                //Synchronisation
                $scope.thisPoly = null;
                $scope.$on('syncMappingEvents', function (event, data, map) {

                    $scope.thisPoly = data;
                    MappingStateService.getState().gid = data;
                    //display attributes from table
                    for (var i = 0; i < $scope.tableData.length; i++) {
                        if ($scope.tableData[i].area_id === data) {
                            $scope.thisResultFigures = $scope.tableData[i];
                            break;
                        }
                    }
                    $scope.thisResultFigures.expected = Number($scope.thisResultFigures.expected).toFixed(3);
                    $scope.$apply();
                    //lines on area charts
                    $rootScope.$broadcast('areaDropLineRedraw', data);
                    $rootScope.$broadcast('rrDropLineRedraw', data);
                    //highlight map
                    if (map) {
                        $scope.topoLayer.eachLayer(handleLayer);
                    }
                });

                //get geography
                user.getTiles(user.currentUser, "SAHSU", "LEVEL4").then(handleTopoJSON, handleTopoJSON);

                //Browser window resize - HACK:
                //angular.element($window).bind('resize', function () {
                // $scope.vSplit1++;
                // $scope.hSplit1--;
                //$scope.$emit('ui.layout.loaded', null);
                // $scope.updateDisplay();
                //});

                $scope.$on('ui.layout.loaded', function () {
                    $scope.rrCurrentHeight = d3.select("#rr").node().getBoundingClientRect().height;
                    $scope.rrCurrentWidth = d3.select("#rr").node().getBoundingClientRect().width;
                    $scope.areaCurrentWidth = d3.select(".mapInfoBottom").node().getBoundingClientRect().width;
                    $scope.areaCurrentHeight = d3.select(".mapInfoBottom").node().getBoundingClientRect().height;
                });

                $scope.$on('ui.layout.resize', function (e, beforeContainer, afterContainer) {
                    //Monitor split sizes
                    if (beforeContainer.id === "vSplit1") {
                        MappingStateService.getState().vSplit1 = (beforeContainer.size / beforeContainer.maxSize) * 100;
                        $scope.rrCurrentWidth = afterContainer.size;
                        $scope.areaCurrentWidth = d3.select(".mapInfoBottom").node().getBoundingClientRect().width;
                        $scope.areaCurrentHeight = d3.select(".mapInfoBottom").node().getBoundingClientRect().height;
                    }
                    if (beforeContainer.id === "hSplit1") {
                        MappingStateService.getState().hSplit1 = (beforeContainer.size / beforeContainer.maxSize) * 100;
                        $scope.rrCurrentHeight = afterContainer.size;
                    }
                    rescaleLeafletContainer();
                });

                //Rescale leaflet container       
                function rescaleLeafletContainer() {
                    leafletData.getMap("diseasemap").then(function (map) {
                        setTimeout(function () {
                            map.invalidateSize();
                        }, 50);
                    });
                }

                $scope.transparency = 0.7;
                var maxbounds;
                var thisMap = [];
                $scope.domain = [];
                var attr;

                //get the user defined basemap
                $scope.parent = {};
                $scope.parent.thisLayer = LeafletBaseMapService.setBaseMap(LeafletBaseMapService.getCurrentBaseMapInUse("diseasemap"));
                //called on bootstrap and on modal submit
                $scope.parent.renderMap = function (mapID) {
                    leafletData.getMap(mapID).then(function (map) {
                        map.removeLayer($scope.parent.thisLayer);
                        if (!LeafletBaseMapService.getNoBaseMap("diseasemap")) {
                            $scope.parent.thisLayer = LeafletBaseMapService.setBaseMap(LeafletBaseMapService.getCurrentBaseMapInUse("diseasemap"));
                            map.addLayer($scope.parent.thisLayer);
                        }
                        //restore setView
                        if (maxbounds && MappingStateService.getState().zoomLevel === -1) {
                            map.fitBounds(maxbounds);
                        } else {
                            map.setView(MappingStateService.getState().view, MappingStateService.getState().zoomLevel);
                        }

                        //hack to refresh map
                        setTimeout(function () {
                            map.invalidateSize();
                        }, 50);
                    });
                };
                $scope.parent.renderMap("diseasemap");

                $timeout(function () {
                    leafletData.getMap("diseasemap").then(function (map) {
                        map.on('zoomend', function (e) {
                            MappingStateService.getState().zoomLevel = map.getZoom();
                        });
                        map.on('moveend', function (e) {
                            MappingStateService.getState().view = map.getCenter();
                        });
                        new L.Control.GeoSearch({
                            provider: new L.GeoSearch.Provider.OpenStreetMap()
                        }).addTo(map);
                    });
                });

                //Render map functions
                function style(feature) {
                    return {
                        fillColor: ChoroService.getRenderFeature(thisMap.scale, attr, false),
                        weight: 1,
                        opacity: 1,
                        color: 'gray',
                        fillOpacity: $scope.transparency
                    };
                }
                function handleLayer(layer) {
                    //find attr value                    
                    //TODO: LookUp - will make more efficient when using API
                    if (!angular.isUndefined($scope.tableData)) {
                        var thisAttr;
                        for (var i = 0; i < $scope.tableData.length; i++) {
                            if ($scope.tableData[i].area_id === layer.feature.properties.area_id) {
                                thisAttr = $scope.tableData[i]['smoothed_smr'];
                                break;
                            }
                        }
                        var polyStyle = ChoroService.getRenderFeature2(layer.feature, thisAttr, thisMap.scale, attr, $scope.thisPoly);
                        layer.setStyle({
                            weight: polyStyle[2],
                            color: polyStyle[1],
                            fillColor: polyStyle[0],
                            fillOpacity: $scope.transparency
                        });
                    }
                }

                //map layer opacity with slider
                $scope.changeOpacity = function () {
                    $scope.topoLayer.eachLayer(handleLayer);
                };

                //Zoom to layer
                $scope.zoomToExtent = function () {
                    leafletData.getMap("diseasemap").then(function (map) {
                        map.fitBounds(maxbounds);
                    });
                };

                //Clear selected
                $scope.clear = function () {
                    $scope.thisPoly = null;
                    MappingStateService.getState().selected = null;
                    MappingStateService.getState().gid = null;
                    $rootScope.$broadcast('rrDropLineRedraw', null);
                    $rootScope.$broadcast('areaDropLineRedraw', null);
                    $scope.topoLayer.eachLayer(handleLayer);
                };

                //Zoom to single selected polygon
                $scope.zoomToSelected = function () {
                    var selbounds = null;
                    $scope.topoLayer.eachLayer(function (layer) {
                        if (layer.feature.properties.area_id === $scope.thisPoly) {
                            selbounds = layer.getBounds();
                        }
                    });
                    if (selbounds !== null) {
                        leafletData.getMap("diseasemap").then(function (map) {
                            map.fitBounds(selbounds);
                        });
                    }
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
                    var thisAttr;
                    for (var i = 0; i < $scope.rrChartData.length; i++) {
                        if ($scope.rrChartData[i].gid === poly) {
                            thisAttr = $scope.rrChartData[i].rr;
                            break;
                        }
                    }
                    if (ChoroService.getMaps(0).feature !== "") {
                        this._div.innerHTML = '<h4>ID: ' + poly + '</br>' + ChoroService.getMaps(0).feature.toUpperCase() + ": " + Number(thisAttr).toFixed(3) + '</h4>';
                    } else {
                        this._div.innerHTML = '<h4>ID: ' + poly + '</h4>';
                    }
                };

                //information from choropleth modal to colour map                             
                $scope.parent.refresh = function () {
                    //get selected colour ramp
                    var rangeIn = ChoroService.getMaps(0).brewer;
                    attr = ChoroService.getMaps(0).feature;

                    //not a choropleth, but single colour
                    if (rangeIn.length === 1) {
                        attr = "";
                        leafletData.getMap("diseasemap").then(function (map) {
                            //remove existing legend
                            if (legend._map) {
                                map.removeControl(legend);
                            }
                        });
                        $scope.topoLayer.eachLayer(handleLayer);
                        return;
                    }

                    thisMap = ChoroService.getMaps(0).renderer;

                    //remove old legend and add new
                    legend.onAdd = ChoroService.getMakeLegend(thisMap, attr);
                    leafletData.getMap("diseasemap").then(function (map) {
                        if (legend._map) { //This may break in future leaflet versions
                            map.removeControl(legend);
                        }
                        legend.addTo(map);
                    });
                    //force a redraw
                    $scope.topoLayer.eachLayer(handleLayer);
                };

                function handleTopoJSON(res) {
                    leafletData.getMap("diseasemap").then(function (map) {
                        map.keyboard.disable();
                        $scope.topoLayer = new L.TopoJSON(res.data, {
                            style: style,
                            onEachFeature: function (feature, layer) {
                                layer.on('mouseover', function (e) {
                                    this.setStyle({
                                        color: 'gray',
                                        weight: 1.5,
                                        fillOpacity: function () {
                                            return($scope.transparency - 0.3 > 0 ? $scope.transparency - 0.3 : 0.1);
                                        }()
                                    });
                                    infoBox.addTo(map);
                                    infoBox.update(layer.feature.properties.area_id);
                                });
                                layer.on('click', function (e) {
                                    $scope.thisPoly = e.target.feature.properties.area_id;
                                    $rootScope.$broadcast('syncMappingEvents', $scope.thisPoly, true);
                                });
                                layer.on('mouseout', function (e) {
                                    $scope.topoLayer.eachLayer(handleLayer);
                                    map.removeControl(infoBox);
                                });
                            }
                        });
                        $scope.topoLayer.addTo(map);
                        maxbounds = $scope.topoLayer.getBounds();
                    }).then(function () {
                        $scope.getAttributeTable();
                    }).then(function () {
                        $scope.parent.renderMap("diseasemap");
                        $scope.parent.refresh();
                    });
                }

                $scope.getAttributeTable = function () {

                    user.getSmoothedResults(user.currentUser, 1, 1, 1990)
                            .then(handleSmoothedResults, attributeError);

                    function handleSmoothedResults(res) {

                        //   if () { //if in rrSets
                        //       
                        //  }
                        //make array for choropleth
                        $scope.tableData = [];
                        for (var i = 0; i < res.data.smoothed_results.length; i++) {
                            $scope.tableData.push(res.data.smoothed_results[i]);
                        }

                        //Get names of the 4 selected result sets
                        var rrSets = ["smoothed_smr", "smoothed_smr", "smoothed_smr", "smoothed_smr"];
                        $scope.optionsRR.label_field = rrSets[0];

                        var tempData = [];
                        for (var j = 0; j < rrSets.length; j++) {
                            var rs = [];
                            for (var i = 0; i < res.data.smoothed_results.length; i++) {
                                //make array for d3 areas
                                rs.push(
                                        {
                                            name: rrSets[j],
                                            gid: res.data.smoothed_results[i].area_id,
                                            x_order: i,
                                            rr: res.data.smoothed_results[i][rrSets[j]],
                                            cl: res.data.smoothed_results[i][rrSets[j] + "_lower95"],
                                            ul: res.data.smoothed_results[i][rrSets[j] + "_upper95"]
                                        }
                                );
                            }
                            tempData.push(rs);

                            //reorder
                            tempData[j].sort(function (a, b) {
                                return parseFloat(a.rr) - parseFloat(b.rr);
                            });
                            for (var i = 0; i < res.data.smoothed_results.length; i++) {
                                tempData[j][i]["x_order"] = i + 1;
                            }
                        }

                        //Summary information
                        $scope.numberOfAreas = res.data.smoothed_results.length;

                        //Choropleth tableData
                        //Features to map are limited to pre-defined RR columns
                        ChoroService.setFeaturesToMap(rrSets);

                        $scope.rrChartData = angular.copy(tempData[0]); //for RR chart (detailed) - same as choropleth attribute
                        $scope.rrAreaData = angular.copy(tempData); //for multiple areas
                    }

                    function attributeError(e) {
                        console.log(e);
                    }
                };



            }
        ]);    