/* global L */

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
 * SERVICE to add tool icons to leaflet containers
 *
 * Icons are defined in: ..\..\css\rifx-css-leaflet.css
 */
angular.module("RIF")
        .factory('mapTools', ['$compile', 'AlertService',
                function ($compile, AlertService) {
                    var selectionMapTools = [
                        '<button ng-controller="BaseMapModalCtrl" ng-click="open(\'areamap\')" type="button" class="btn btn-basemap" title="Base map"></button>',
                        '<button type="button" class="btn btn-selectAll" title="Select all" ng-click="selectAll()"></button>',
                        '<button type="button" class="btn btn-clear" title="Clear selection" ng-click="clear()"></button>',
                        '<button type="button" class="btn btn-zoomExtent" title="Zoom to full extent" ng-click="zoomToExtent()"></button>',
                        '<button type="button" class="btn btn-zoomSelected" title="Zoom to selection" ng-click="zoomToSelection()"></button>',
                        '<button type="button" class="btn btn-addPostalCode" title="Select by postal code/WGS84/grid coordinates" postal-code></button>',
                        '<button type="button" class="btn btn-addAOI" title="Select by shapefile" risk-analysis></button>',
                        '<button type="button" class="btn btn-exportMap" title="Quick export map" leaflet-to-png mapid="areamap"></button>',
                        '<div ng-switch on=bShowHideCentroids>' +
							'<button type="button" class="btn btn-centroids" title="Show centroids" id="showHideCentroidsTrue" ng-click="showCentroids()" ng-switch-when="true"></button>' +
							'<button type="button" class="btn btn-centroids" title="Hide centroids" id="showHideCentroidsFalse" ng-click="showCentroids()" ng-switch-when="false"></button>' +
						'</div>',
                        '<div ng-switch on=bShowHideSelectionShapes>' +
							'<button type="button" class="btn btn-shapes" title="Show selction shapes" id="showHideSelectionShapesTrue" ng-click="showShapes()" ng-switch-when="true"></button>' +
							'<button type="button" class="btn btn-shapes" title="Hide selction shapes" id="showHideSelectionShapesFalse" ng-click="showShapes()" ng-switch-when="false"></button>' +
						'</div>'
                    ];

                    var basicMapTools = [
                        '<button ng-controller="ChoroplethModalCtrl" ng-click="open(\'XMAPX\')" type="button" class="btn btn-choropleth" title="Choropleth map"></button>',
                        '<button ng-controller="BaseMapModalCtrl" ng-click="open(\'XMAPX\')" type="button" class="btn btn-basemap" title="Base map"></button>',
                        '<button type="button" class="btn btn-clear" title="Clear selection" clear-array-selection mapid=\'XMAPX\'></button>',
                        '<button type="button" class="btn btn-zoomExtent" title="Zoom to full extent" zoom-extent mapid=\'XMAPX\'></button>',
                        '<button type="button" class="btn btn-zoomStudy" title="Zoom to study extent" zoom-study mapid=\'XMAPX\'></button>',
                        '<button type="button" class="btn btn-zoomSelected" title="Zoom to selection" zoom-selection-single mapid=\'XMAPX\'></button>',
                        '<button type="button" class="btn btn-exportMap" title="Quick export map" leaflet-to-png mapid=\'map.XMAPX\'></button>',
                        '<div ng-switch on=bShowHideSelectionShapes[\'XMAPX\']>' +
							'<button type="button" class="btn btn-shapes" title="Show selction shapes" id="showHideSelectionShapesTrue" ng-click="showShapes(\'XMAPX\')" ng-switch-when="true"></button>' +
							'<button type="button" class="btn btn-shapes" title="Hide selction shapes" id="showHideSelectionShapesFalse" ng-click="showShapes(\'XMAPX\')" ng-switch-when="false"></button>' +
						'</div>'
                    ];

                    var extraMapTools = [
                        '<button type="button" class="btn btn-switchSymbology" title="Transfer symbology" ng-click="copySymbology()"></button>',
                        '<div ng-switch on=bLockCenters ><a ng-click="lockExtent()" title="Unlock map extents" id="lockExtentTrue" ng-switch-when="true"></a>' +
                                '<a ng-click = "lockExtent()" title = "Lock map extents" id="lockExtentFalse" ng-switch-when ="false"> </a></div>',
                        '<div ng-switch on=bLockSelect><a ng-click="lockSelect()" title="Unlock selection" id="lockSelectTrue" ng-switch-when="true"></a>' +
                                '<a ng-click="lockSelect()" title="Lock selection" id="lockSelectFalse" ng-switch-when="false"></a></div>'
                    ];

                    var bandMapDropDown =
                            '<div id="bandHolder"> <label for="selectbands">Band</label>' +
                            '<select id="selectbands" class="dropdownBand" ng-options="n for n in possibleBands" ng-model="currentBand" ng-init="currentBand"></select>' +
                            '</div>';

                    var areaMapExportSelect =
                            '<div id="areaHolder">' +
                            '<input ng-model="area.name" type="radio" name="area" value="study" ng-change="updateStudy(\'exportmap\')"> Study Area<br>' +
                            '<input ng-model="area.name" type="radio" name="area" value="comparison" ng-change="updateStudy(\'exportmap\')"> Comparison Area<br>' +
                            '<input ng-model="exportLevel" ng-change="detailLevelChange()" id="zoomLevel" type="number" min="6" max="11"> Detail Level</input><br>' +
                            '</div>';

                    var zoomToStudy =
                            '<button type="button" class="btn btn-zoomStudy" title="Zoom to study extent" zoom-area></button>';

                    function basicTools(scope, map) {
                        var tmp = [];
                        for (var i = 0; i < basicMapTools.length; i++) {
                            var html = basicMapTools[i].replace(/XMAPX/g, map);
                            if (i === 5 && map === "viewermap") {
                                html = html.replace("single", "multiple");
                            }
                            tmp.push(makeToolIcon(scope, html, 'bottomleft'));
                        }
                        return tmp;
                    }
                    function extraTools(scope) {
                        var tmp = [];
                        for (var i = 0; i < extraMapTools.length; i++) {
                            tmp.push(makeToolIcon(scope, extraMapTools[i], 'bottomleft'));
                        }
                        return tmp;
                    }
                    function selectionTools(scope) {
                        var tmp = [];
                        for (var i = 0; i < selectionMapTools.length; i++) {
                            tmp.push(makeToolIcon(scope, selectionMapTools[i], 'bottomleft'));
                        }
                        return tmp;
                    }
                    function bandDropDown(scope) {
                        return makeToolIcon(scope, bandMapDropDown, 'topright');
                    }
                    function areaExportSelect(scope) {
                        return makeToolIcon(scope, areaMapExportSelect, 'topright');
                    }
                    function zoomToStudyTool(scope) {
                        return makeToolIcon(scope, zoomToStudy, 'bottomleft');
                    }
                    function makeToolIcon(scope, html, pos) {
                        var tool = L.Control.extend({
                            options: {
                                position: pos
                            },
                            onAdd: function () {
                                var linkFunction = $compile(angular.element(html));
                                var container = linkFunction(scope)[0];
								if (linkFunction == undefined || container == undefined) {	
									AlertService.consoleError("[rifs-util-maptools.js] unable to compile: " + html);
								}
                                return container;
                            }
                        });
                        return tool;
                    }
                    return {
                        getMakeToolIcon: function (scope, html) {
                            return makeToolIcon(scope, html);
                        },
                        getBasicTools: function (scope, map) {
                            return basicTools(scope, map);
                        },
                        getExtraTools: function (scope) {
                            return extraTools(scope);
                        },
                        getSelectionTools: function (scope) {
                            return selectionTools(scope);
                        },
                        getBandDropDown: function (scope) {
                            return bandDropDown(scope);
                        },
                        getAreaExportSelect: function (scope) {
                            return areaExportSelect(scope);
                        },
                        getZoomToStudy: function (scope) {
                            return zoomToStudyTool(scope);
                        }
                    };
                }]);
