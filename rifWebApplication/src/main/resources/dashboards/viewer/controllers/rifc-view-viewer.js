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
 * CONTROLLER for data viewer
 */

/* global L, key, topojson, d3 */
angular.module("RIF")
        .controller('ViewerCtrl', 
            ['$scope', 'user', '$timeout', 'ViewerStateService', 'ChoroService', 'mapTools', 'D3ChartsService',
            function ($scope, user, $timeout, ViewerStateService, ChoroService, mapTools, D3ChartsService) {

                //Reference the child scope (controller is embedded)
                //child scope will be on either the mapping or viewer dashboards
                //controller instance recreated in each case
                $scope.child = {};

                $scope.$on("$destroy", function () {
                    //A flag to keep renderers if changing tabs
                    ViewerStateService.getState().initial = true;
                    $scope.child.map['viewermap'].remove();
                });

                $scope.$on('ui.layout.resize', function (e, beforeContainer, afterContainer) {
                    $scope.child.map['viewermap'].invalidateSize();
                });

                $timeout(function () {
                    //make map
                    var view = $scope.child.myService.getState().center['viewermap'];
                    $scope.child.map['viewermap'] = L.map('viewermap', {condensedAttributionControl: false}).setView([0, 0], 1);

                    //Attributions to open in new window
                    L.control.condensedAttribution({
                        prefix: '<a href="http://leafletjs.com" target="_blank">Leaflet</a>'
                    }).addTo($scope.child.map['viewermap']);

                    //search box
                    new L.Control.GeoSearch({
                        provider: new L.GeoSearch.Provider.OpenStreetMap()
                    }).addTo($scope.child.map['viewermap']);

                    //full screen control             
                    $scope.child.map['viewermap'].addControl(new L.Control.Fullscreen());
                    $scope.child.map['viewermap'].on('fullscreenchange', function () {
                        setTimeout(function () {
                            $scope.child.map['viewermap'].invalidateSize();
                        }, 50);
                    });

                    //slider
                    var slider = L.control.slider(function (v) {
                        ViewerStateService.getState().transparency['viewermap'] = v;
                        $scope.transparency['viewermap'] = v;
                        if (angular.isDefined($scope.child.geoJSON['viewermap']._geojsons)) {
                            $scope.child.geoJSON['viewermap']._geojsons.default.eachLayer($scope.child.handleLayer);
                        }
                    }, {
                        id: slider,
                        position: 'topleft',
                        orientation: 'horizontal',
                        min: 0,
                        max: 1,
                        step: 0.01,
                        value: ViewerStateService.getState().transparency['viewermap'],
                        title: 'Transparency',
                        logo: '',
                        syncSlider: true

                    }).addTo($scope.child.map['viewermap']);

                    //Custom Toolbar
                    var tools = mapTools.getBasicTools($scope.child, "viewermap");
                    for (var i = 0; i < tools.length; i++) {
                        new tools[i]().addTo($scope.child.map['viewermap']);
                    }

                    //scalebar
                    L.control.scale({position: 'bottomleft', imperial: false}).addTo($scope.child.map['viewermap']);

                    $scope.child.map['viewermap'].doubleClickZoom.disable();
                    $scope.child.map['viewermap'].keyboard.disable();

                    //Fill study drop-downs
                    $scope.child.getStudies();
                    $scope.child.renderMap("viewermap");

                    setTimeout(function () {
                        $scope.child.map['viewermap'].invalidateSize();
                    }, 50);
                });

                $scope.isDiseaseMapping=true;
                $scope.isRiskAnalysis=false;	
                $scope.isDiseaseMappingStudy = { 
					"viewermap": true
				};
				
                $scope.testShow=true;                
                $scope.gendersArray2=['males', 'females'];	
                $scope.riskFactor2FieldName2 = {
                    'average exposure': 'avgExposureValue', 
                    'band': 'bandId', 
                    'average distance from nearest source': 'avgDistanceFromNearestSource'
                };
                $scope.riskGraphChartName2="riskGraph2";
                $scope.riskFactor2='band';
                $scope.riskGraphData2={};
                
                //ui-container sizes
                $scope.distHistoCurrentHeight = 400;
                $scope.distHistoCurrentWidth = 250;
                $scope.riskGraphChartCurrentWidth2=400;
                $scope.riskGraphChartCurrentHeight2=250;
                $scope.pyramidCurrentHeight=400;
                $scope.pyramidCurrentWidth=250;
                $scope.vSplit1 = ViewerStateService.getState().vSplit1;
                $scope.hSplit1 = ViewerStateService.getState().hSplit1;
                $scope.hSplit2 = ViewerStateService.getState().hSplit2;
				
                $scope.getD3Frames = function () { // Called from ui.layout.loaded event
                    $scope.distHistoCurrentHeight = d3.select("#hSplit1").node().getBoundingClientRect().height;
                    $scope.distHistoCurrentWidth = d3.select("#hSplit1").node().getBoundingClientRect().width;
                    $scope.pyramidCurrentHeight = d3.select("#hSplit2").node().getBoundingClientRect().height;
                    $scope.pyramidCurrentWidth = d3.select("#hSplit1").node().getBoundingClientRect().width;
                    $scope.riskGraphChartCurrentHeight2 = d3.select("#hSplit1").node().getBoundingClientRect().height;
                    $scope.riskGraphChartCurrentWidth2 = d3.select("#hSplit1").node().getBoundingClientRect().width;                  
                };

                $scope.getD3FramesOnResize = function (beforeContainer, afterContainer) { 
                    // Called from ui.layout.resize event
                    //Monitor split sizes            
                    if (beforeContainer.id === "vSplit1") {
                        ViewerStateService.getState().vSplit1 = (beforeContainer.size / beforeContainer.maxSize) * 100;
                        $scope.distHistoCurrentWidth = beforeContainer.size;
                        $scope.pyramidCurrentWidth = beforeContainer.size;
                        $scope.riskGraphChartCurrentWidth2 = beforeContainer.size;
                    }     
                    if (beforeContainer.id === "hSplit2") {
                        ViewerStateService.getState().hSplit2 = (beforeContainer.size / beforeContainer.maxSize) * 100;
                    } 
                    if (beforeContainer.id === "hSplit1") {
                        ViewerStateService.getState().hSplit1 = (beforeContainer.size / beforeContainer.maxSize) * 100;
                        $scope.distHistoCurrentHeight = beforeContainer.size;
                        $scope.pyramidCurrentHeight = afterContainer.size;
                        $scope.riskGraphChartCurrentHeight2 = beforeContainer.size;
                    }
/*                    if (beforeContainer.id === "hSplit3") {
                        ViewerStateService.getState().hSplit1 = (beforeContainer.size / beforeContainer.maxSize) * 100;
                        $scope.distHistoCurrentHeight = beforeContainer.size;
                        $scope.pyramidCurrentHeight = afterContainer.size;
                        $scope.riskGraphChartCurrentHeight2 = beforeContainer.size;
                    } */
               
                };

                /*
                 * Local map variables
                 */
                $scope.myMaps = ["viewermap"];

                //transparency as set by slider
                $scope.transparency = {
                    "viewermap": ViewerStateService.getState().transparency["viewermap"]
                };

                //selected polygons
                $scope.thisPoly = ViewerStateService.getState().selected["viewermap"];

                //study to be mapped
                $scope.studyID = {
                    "viewermap": ViewerStateService.getState().study['viewermap']
                };
                $scope.sex = {
                    "viewermap": ViewerStateService.getState().sex['viewermap']
                };
                $scope.studyType = {
                    "viewermap": ""
				};
				$scope.thisPolygon = {
                    "viewermap": ""
				};
				
                //attributes for d3
                $scope.histoData = {
                    "viewermap": []
                };
                $scope.populationData = {
                    "viewermap": []
                };
                $scope.domain = {
                    "viewermap": []
                };
				
				
                $scope.optionsd3 = {
                    "poppyramid": {
                        container: "poppyramid",
                        element: "#hSplit2",
                        filename: "populationPyramid.png"
                    },
                    "distHisto": {
                        container: "distHisto",
                        element: "#hSplit1",
                        filename: "histogram.png"
                    },
                    "riskGraph": {
                        container: "riskGraph2",
                        element: "#hSplit1",
                        filename: "riskGraph.png"
                    }
                };

                /*
                 * D3
                 */
                //Draw the attribute histogram
                $scope.getD3chart = function (mapID, attribute) {	
					
					$scope.consoleDebug("[rifc-view-viewer.js] getD3chart, map: " + mapID + "; study type: " + $scope.studyType[mapID] +
						"; isDiseaseMappingStudy: " + $scope.isDiseaseMappingStudy[mapID] +
						"; attribute: " + attribute + "; data rows: " + $scope.child.tableData[mapID].length);
						
                    $scope.distHistoName = ChoroService.getMaps("viewermap").feature;
                    $scope.histoData["viewermap"].length = 0;
                    for (var i = 0; i < $scope.child.tableData[mapID].length; i++) {
                        $scope.histoData[mapID].push($scope.child.tableData[mapID][i][ChoroService.getMaps(mapID).feature]);
                    }
                    //get risk graph data
                    if ($scope.isRiskAnalysis) {
                        user.getRiskGraph(user.currentUser, $scope.studyID["viewermap"].study_id).then(function (res) {
                            $scope.riskGraphData2=angular.copy(res.data);                                 
                            var selector=D3ChartsService.setupRiskGraphSelector(
                                $scope.riskGraphData2, $scope.riskFactor2FieldName2);
                            for (var key in selector) { // Copy to scope
                                $scope[key]=angular.copy(selector[key]);
                            }
                            $scope.gendersArray2=selector.gendersArray;
                            $scope.riskFactor2=selector.riskFactor;
//                            $scope.consoleDebug("[rifc-view-viewer.js] got risk graph data: " +
//                                JSON.stringify($scope.riskGraphData2, 0, 1) + 
//                                "; study: " + $scope.studyID["viewermap"].study_id +
//                                "; selector: " + JSON.stringify(selector, 0, 1));
                        }, function () {
                            $scope.showError("[rifc-view-viewer.js] get risk graph data error");
                        });
                    }
                };

                //population pyramaid year selection
                $scope.yearsPop = [];
                
                $scope.fillPyramidData = function () {
                    user.getYearsForStudy(user.currentUser, $scope.studyID["viewermap"].study_id, "viewermap").then(function (res) {
                        if (!angular.isUndefined(res.data['years{'])) { //Note erroneous trailing '{' in middle ware method
                            $scope.yearsPop.length = 0;
                            for (var i = 0; i < res.data['years{'].length; i++) {
                                $scope.yearsPop.push(res.data['years{'][i]);
                            }
                        }
                    }, function () {
                        $scope.showError("[rifc-view-viewer.js] get population pyramid years data error");
                    }).then(function () {
                        $scope.child.yearPop = $scope.yearsPop[0];
                        $scope.updatePyramidForYear();
                    });
                };

                $scope.updatePyramidForYear = function () {
                    if ($scope.child.yearPop !== null) {
                        user.getAllPopulationPyramidData(user.currentUser, $scope.studyID["viewermap"].study_id, $scope.child.yearPop)
                                .then(function (res) {
                                    $scope.populationData["viewermap"] = res.data.smoothed_results;
                                }, function () {
                                    $scope.showError("[rifc-view-viewer.js] pyramid data fetch error");
                                });
                    }
                };

                /*
                 * Specific to Attribute table
                 */
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

                $scope.updateTable = function () {
                    $scope.gridApi.selection.clearSelectedRows();
                    for (var i = 0; i < $scope.viewerTableOptions.data.length; i++) {
                        $scope.viewerTableOptions.data[i]._selected = 0;
                        for (var j = 0; j < $scope.thisPoly.length; j++) {
                            if ($scope.viewerTableOptions.data[i].area_id === $scope.thisPoly[j]) {
                                $scope.viewerTableOptions.data[i]._selected = 1;
                            }
                        }
                    }
                };

                /*
                 * Watch for Changes
                 */
                //Watch thisPoly array for any changes
                $scope.$watchCollection('thisPoly', function (newNames, oldNames) {
                    if (newNames === oldNames) {
                        return;
                    }
                    //Update table selection
                    $scope.updateTable();
                    ViewerStateService.getState().selected["viewermap"] = newNames;
                    //Update map selection
                    $scope.child.geoJSON["viewermap"]._geojsons.default.eachLayer($scope.child.handleLayer);
                });
            }]);