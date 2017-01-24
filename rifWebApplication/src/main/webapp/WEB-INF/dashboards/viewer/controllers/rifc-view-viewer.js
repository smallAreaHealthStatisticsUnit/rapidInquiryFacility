/* 
 * CONTROLLER for data viewer
 */

/* global L, key, topojson, d3 */
angular.module("RIF")
        .controller('ViewerCtrl', ['$scope', 'user', 'leafletData', '$timeout',
            'ViewerStateService', 'ChoroService',
            function ($scope, user, leafletData, $timeout,
                    ViewerStateService, ChoroService) {

                //Reference the child scope
                $scope.child = {};

                //A flag to keep renderers if changing tabs
                $scope.$on("$destroy", function () {
                    ViewerStateService.getState().initial = true;
                });

                //Set initial map panels and events
                $timeout(function () {
                    leafletData.getMap("viewermap").then(function (map) {
                        map.on('zoomend', function (e) {
                            ViewerStateService.getState().center["viewermap"].zoom = map.getZoom();
                        });
                        map.on('moveend', function (e) {
                            ViewerStateService.getState().center["viewermap"].lng = map.getCenter().lng;
                            ViewerStateService.getState().center["viewermap"].lat = map.getCenter().lat;
                        });
                        new L.Control.GeoSearch({
                            provider: new L.GeoSearch.Provider.OpenStreetMap()
                        }).addTo(map);
                        L.control.scale({position: 'topleft', imperial: false}).addTo(map);
                        //Attributions to open in new window
                        map.attributionControl.options.prefix = '<a href="http://leafletjs.com" target="_blank">Leaflet</a>';
                        map.doubleClickZoom.disable();
                    });
                    //Set initial map extents
                    $scope.center = ViewerStateService.getState().center['viewermap'];

                    //Fill study drop-downs
                    $scope.child.getStudies();
                });

                //ui-container sizes
                $scope.distHistoCurrentHeight = 200;
                $scope.distHistoCurrentWidth = 200;
                $scope.pyramidCurrentHeight = 200;
                $scope.pyramidCurrentWidth = 200;
                $scope.vSplit1 = ViewerStateService.getState().vSplit1;
                $scope.hSplit1 = ViewerStateService.getState().hSplit1;
                $scope.hSplit2 = ViewerStateService.getState().hSplit2;

                $scope.getD3Frames = function () {
                    $scope.distHistoCurrentHeight = d3.select("#hSplit1").node().getBoundingClientRect().height;
                    $scope.distHistoCurrentWidth = d3.select("#hSplit1").node().getBoundingClientRect().width;
                    $scope.pyramidCurrentHeight = d3.select("#hSplit2").node().getBoundingClientRect().height;
                    $scope.pyramidCurrentWidth = d3.select("#hSplit1").node().getBoundingClientRect().width;
                };

                $scope.getD3FramesOnResize = function (beforeContainer, afterContainer) {
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
                    $scope.child.rescaleLeafletContainer();
                };

                /*
                 * Local map variables
                 */
                $scope.myMaps = ["viewermap"];

                //define non-null centre for leaflet directive
                $scope.center = {};
                //transparency as set by slider
                $scope.transparency = {
                    "viewermap": ViewerStateService.getState().transparency["viewermap"]
                };
                //target for getTiles topoJSON
                $scope.geoJSON = {};

                //selected polygons
                $scope.thisPoly = ViewerStateService.getState().selected["viewermap"];

                //study to be mapped
                $scope.studyID = {
                    "viewermap": ViewerStateService.getState().study['viewermap']
                };
                $scope.sex = {
                    "viewermap": ViewerStateService.getState().sex['viewermap']
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
                    }
                };

                /*
                 * D3
                 */
                //Draw the attribute histogram
                $scope.getD3chart = function (mapID, attribute) {
                    $scope.histoData["viewermap"].length = 0;
                    for (var i = 0; i < $scope.child.tableData[mapID].length; i++) {
                        $scope.histoData[mapID].push($scope.child.tableData[mapID][i][ChoroService.getMaps(mapID).feature]);
                    }
                };

                //population pyramaid year selection
                $scope.yearsPop = [];
                $scope.fillPyramidData = function () {
                    user.getYearsForStudy(user.currentUser, 1, "viewermap").then(function (res) {
                        if (!angular.isUndefined(res.data['years{'])) { //Note erroneous trailing '{' in middle ware method
                            $scope.yearsPop.length = 0;
                            for (var i = 0; i < res.data['years{'].length; i++) {
                                $scope.yearsPop.push(res.data['years{'][i]);
                            }
                        }
                    }, function () {
                        console.log("year error");
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
                                    console.log("pyramid data error");
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
                //Watch selectedPolygon array for any changes
                $scope.$watchCollection('thisPoly', function (newNames, oldNames) {
                    if (newNames === oldNames) {
                        return;
                    }
                    //Update table selection
                    $scope.updateTable();
                    ViewerStateService.getState().selected["viewermap"] = newNames;
                    //Update map selection
                    $scope.geoJSON["viewermap"].eachLayer($scope.child.handleLayer);
                });
            }]);