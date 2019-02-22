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
 
 * Peter Hambly
 * @author phambly
 */

/*
 * DIRECTIVE for D3 risk graph - info version - see also rifd-view-d3riskGraph2.js which is the same apart from the 
 * directive tag
 */

/* global d3 */

//http://bl.ocks.org/mbostock/3048450

angular.module("RIF")
        .directive('riskGraph', ['AlertService', 'D3ChartsService', '$timeout',
            function(AlertService, D3ChartsService, $timeout) { // riskFactorChart
            var directiveDefinitionObject = {
                restrict: 'E',
                replace: false,
                scope: {
                    riskGraphData: '=riskGraphData',
                    name: '=name',
                    width: '=width',
                    height: '=height',
                    gendersArray: '=gendersArray',
                    riskFactor: '=riskFactor',
                    riskFactor2FieldName: '=riskFactor2FieldName'        
                },
                link: function (scope, element, attrs) {
              
                    var watchCount=0;
                    var nullCount=0;
                    var timeoutPromise;

                    var unregister = scope.$watch(function () {
                        
                        if (angular.isUndefined(scope.width)) {
                            scope.width=150;
                        }
                        if (angular.isUndefined(scope.height)) {
                            scope.height=150;
                        }
                        
                        var riskGraphCallback = function(err, svg, elementName) {
                            if (err) {
                                AlertService.showError("rifd-util-d3riskGraph.js] riskGraph: " + scope.name + 
                                    " error: " + err);
                                unregister();
                            }
                            else if (scope.name == undefined) {
                                AlertService.showError("rifd-util-d3riskGraph.js] riskGraph: no scope.name");
                                unregister();
                            }
                            else if (svg == undefined || svg.size() == 0) {
                                AlertService.showError("rifd-util-d3riskGraph.js] riskGraph: " + scope.name + 
                                    " div not created");
                                unregister();
                            }
                            else if (elementName == undefined) {
                                AlertService.showError("rifd-util-d3riskGraph.js] riskGraph: " + scope.name + 
                                    " elementName not provided");
                                unregister();
                            }
                            else {
                                AlertService.consoleDebug("rifd-util-d3riskGraph.js] riskGraph: " + scope.name + 
                                    " created with " + svg.size() + " elements" +
                                    "; watchCount: " + watchCount);
                                if (watchCount > 0) {
                                    watchCount--;
                                }
                            }
                        }
                    
                        var hasRiskGraphDataChangedCallback = function(err, result) {
                            var elementName="#" + scope.name;
                            var svg=d3.select(element[0]).select("svg");
                            
                            if (err) {
                                AlertService.showError("rifd-util-d3riskGraph.js] riskGraph: " + scope.name + 
                                    "hasRiskGraphDataChanged error: " + err);
                                unregister();
                            }
                            else if (result || svg == undefined || svg.size() == 0) {
                                if (svg && svg.size() > 0) {
                                    AlertService.consoleDebug("[rifd-util-d3riskGraph.js] has data changed result: " + result + 
                                        "; svg remove: " + 
                                        scope.name);
                                    svg.remove();
                                }
                                svg = d3.select(element[0]).append("svg");
                                
                                var hSplit3=d3.select("#hSplit3");
                                var riskGraphChartCurrentHeight;
                                var riskGraphChartCurrentWidth;
                                if (hSplit3) {
                                    riskGraphChartCurrentHeight = hSplit3.node().getBoundingClientRect().height;
                                    riskGraphChartCurrentWidth = hSplit3.node().getBoundingClientRect().width;
                                }
 
                                AlertService.consoleDebug("[rifd-util-d3riskGraph.js] call getD3RiskGraph() " +
                                    "; watchCount: " + watchCount +
                                    "; gendersArray: " + JSON.stringify(scope.gendersArray) + 
                                    "; riskFactor: " + scope.riskFactor +
                                    "; name: " + scope.name +
                                    "; width: " + (scope.width || riskGraphChartCurrentWidth) +
                                    "; height: " + (scope.height || riskGraphChartCurrentHeight) +
                                    "; riskFactor2FieldName: " + scope.riskFactor2FieldName[scope.riskFactor]);
                                D3ChartsService.getD3RiskGraph(svg, elementName, scope.riskGraphData, scope.gendersArray, 
                                    scope.riskFactor2FieldName[scope.riskFactor], scope.riskFactor, scope.name,
                                    (scope.width || riskGraphChartCurrentWidth), 
                                    (scope.height || riskGraphChartCurrentHeight),
                                    riskGraphCallback);
                            }
                            else {
//                                AlertService.consoleDebug(
//                                    "[rifd-util-d3riskGraph.js] riskGraph: " + scope.name + 
//                                    " no change in risk graph data; watchCount: " + 
//                                    watchCount);
                                if (watchCount > 0) {
                                    watchCount=0;
                                }
                            }
                        }
                            
                        if (angular.isUndefined(scope.riskGraphData) || 
                            Object.keys(scope.riskGraphData).length === 0) { // No data
                            if (timeoutPromise) {
                                $timeout.cancel(timeoutPromise);  
                            }
                            timeoutPromise = $timeout(function() {   //Set timeout
                                var svg=d3.select(element[0]).select("svg");
                                if (angular.isUndefined(scope.riskGraphData) || 
                                    Object.keys(scope.riskGraphData).length === 0) {
                                    if (svg == undefined || svg.size() == 0) {
                                        AlertService.consoleDebug("[rifd-util-d3riskGraph.js] no data found (call: " + 
                                            nullCount + ") for: " + scope.name);
                                    }
                                    else {
                                        AlertService.showError("[rifd-util-d3riskGraph.js] no data found (call: " + 
                                            nullCount + ") for: " + scope.name + "; removing existing SVG graphic");
                                        svg.remove();
                                    }
                                    nullCount++;
                                }
                                else {
                                    if (svg == undefined || svg.size() == 0) {
                                        AlertService.consoleDebug("[rifd-util-d3riskGraph.js] found data (call: " + 
                                            nullCount + ") for: " + scope.name +
                                            "; watchCount: " + watchCount);
                                    }
                                    else {
                                        AlertService.consoleDebug("[rifd-util-d3riskGraph.js] found data (call: " + 
                                            nullCount + ") for: " + scope.name + "; SVG graphic:  " +
                                            svg.size() +
                                            "; watchCount: " + watchCount);
                                    }
                                    nullCount=0;
                                    watchCount=0;
                                }
                            }, 500);  
                           
                            if (nullCount > 50) { // c25 secs
                                AlertService.showError("[rifd-util-d3riskGraph.js] Still no data found for: " + 
                                    scope.name);
                                unregister();
                            }
//                            d3.select(elementName).remove();
                        }
                        else { // Has data
                            nullCount=0;
                            if (angular.isUndefined(scope.gendersArray) ||
                                angular.isUndefined(scope.riskFactor) ||
                                angular.isUndefined(scope.name) ||
                                angular.isUndefined(scope.riskFactor2FieldName)) {
                                AlertService.showError("rifd-util-d3riskGraph.js] riskGraph: " + scope.name + 
                                    " one or more of gendersArray/riskFactor/name/riskFactor2FieldName is undefined");
                                unregister();
                            }
                            else if (watchCount == 0) {
                                
                                watchCount++;
                                D3ChartsService.hasRiskGraphDataChanged(
                                    scope.riskGraphData, 
                                    scope.gendersArray, 
                                    ((scope.riskFactor2FieldName && scope.riskFactor) ? 
                                        scope.riskFactor2FieldName[scope.riskFactor] : undefined), 
                                    scope.riskFactor, 
                                    scope.width, scope.height, scope.name, 
                                    hasRiskGraphDataChangedCallback);
                            }
                        }
                    });
                }
            };
            return directiveDefinitionObject;
        }]);