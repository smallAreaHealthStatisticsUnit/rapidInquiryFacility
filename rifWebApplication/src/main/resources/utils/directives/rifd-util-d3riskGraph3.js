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
 * DIRECTIVE for D3 risk graph - common version
 */

/* global d3 */

//http://bl.ocks.org/mbostock/3048450

angular.module("RIF")
        .directive('riskGraph3', ['AlertService', 'D3ChartsService', '$timeout',
            function(AlertService, D3ChartsService, $timeout) { // riskFactorChart
            var directiveDefinitionObject = {
                restrict: 'E',
                replace: false,
                scope: {
                    riskGraphData: '=riskGraphData' /*,
                    name: '=name',
                    width: '=width',
                    height: '=height',
                    gendersArray: '=gendersArray',
                    riskFactor: '=riskFactor',
                    riskFactor2FieldName: '=riskFactor2FieldName' */       
                },
                link: function (scope, element, attrs) {
              
                    var nullCount=0;
                    var timeoutPromise;
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
                                " created with " + svg.size() + " elements");
                        }
                    }
                   
                    var unregister = scope.$watch('riskGraphData', function(newValue, oldValue) {
                        var svg=d3.select(element[0]).select("svg");
                            
                        if (newValue && newValue.name && 
                            newValue.riskGraphData && Object.keys(newValue.riskGraphData).length > 0 &&
                            D3ChartsService.diffParameters(newValue.name, newValue, oldValue)) {
                                         
                            if (angular.isUndefined(newValue.width)) {
                                newValue.width=150;
                            }
                            if (angular.isUndefined(newValue.height)) {
                                newValue.height=150;
                            }
                        
                            var elementName="#" + newValue.name;                         
                            if (svg == undefined || svg.size() == 0) {
                                if (svg && svg.size() > 0) {
                                    AlertService.consoleDebug("[rifd-util-d3riskGraph.js] has data changed result: " + result + 
                                        "; svg remove: " + 
                                        newValue.name);
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
                                    "; gendersArray: " + JSON.stringify(newValue.gendersArray) + 
                                    "; riskFactor: " + newValue.riskFactor +
                                    "; name: " + newValue.name +
                                    "; width: " + (newValue.width || riskGraphChartCurrentWidth) +
                                    "; height: " + (newValue.height || riskGraphChartCurrentHeight) +
                                    "; riskFactor2FieldName: " + newValue.riskFactor2FieldName[newValue.riskFactor]);
                                D3ChartsService.getD3RiskGraph(svg, elementName, newValue.riskGraphData, newValue.gendersArray, 
                                    newValue.riskFactor2FieldName[newValue.riskFactor], newValue.riskFactor, newValue.name,
                                    (newValue.width || riskGraphChartCurrentWidth), 
                                    (newValue.height || riskGraphChartCurrentHeight),
                                    riskGraphCallback);
                            }
                            nullCount=0;
                        }
                        else { // No data
                            if (svg == undefined || svg.size() == 0) {
                                AlertService.consoleDebug("[rifd-util-d3riskGraph.js] no data found (call: " + 
                                    nullCount + ") for: " + (newValue ? newValue.name : "No name"));
                            }
                            else {
                                AlertService.showError("[rifd-util-d3riskGraph.js] no data found (call: " + 
                                    nullCount + ") for: " + (newValue ? newValue.name : "No name") + "; removing existing SVG graphic");
                                svg.remove();
                            }
                            nullCount++;
                        }
                        
                    });
                }
            };
            return directiveDefinitionObject;
        }]);