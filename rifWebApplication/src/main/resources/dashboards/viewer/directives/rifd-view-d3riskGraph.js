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
 * DIRECTIVE for D3 risk graph
 */

/* global d3 */

//http://bl.ocks.org/mbostock/3048450

angular.module("RIF")
        .directive('riskGraph', ['AlertService', 'D3ChartsService', 
            function(AlertService, D3ChartsService) { // riskFactorChart
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
                    
                    var unregister = scope.$watch(function () {
                        var riskGraphCallback = function(err, svg) {
                            if (err) {
                                AlertService.showError("rifd-view-d3riskGraph.js] riskGraph error: " + err);
                                unregister();
                            }
                            else if (svg == undefined || svg.size() == 0) {
                                AlertService.showError("rifd-view-d3riskGraph.js] riskGraph div not created");
                                unregister();
                            }
                            else {
                                AlertService.consoleDebug("rifd-view-d3riskGraph.js] riskGraph created with " +
                                    svg.size() + " elements");
                                if (watchCount > 0) {
                                    watchCount--;
                                }
                            }
                        }
                    
                        if (angular.isUndefined(scope.riskGraphData) || scope.riskGraphData.length === 0) {
                            d3.select("#riskGraph").remove();
                        }
                        else if (angular.isUndefined(scope.gendersArray) ||
                            angular.isUndefined(scope.riskFactor) ||
                            angular.isUndefined(scope.name) ||
                            angular.isUndefined(scope.riskFactor2FieldName)) {
                            AlertService.showError("rifd-view-d3riskGraph.js] one or more of gendersArray/riskFactor/name/riskFactor2FieldName is undefined");
                            unregister();
                        }
                        else if (watchCount == 0) {
                            var hasRiskGraphDataChangedCallback = function(err, result) {
                                if (err) {
                                    AlertService.showError("rifd-view-d3riskGraph.js] hasRiskGraphDataChanged error: " + err);
                                    unregister();
                                }
                                else if (result) {
                                    d3.select("#riskGraph").remove();

                                    var svg = d3.select(element[0]).append("svg");
                                    AlertService.consoleDebug("[rifd-view-d3riskGraph.js] call getD3RiskGraph() " +
                                        "gendersArray: " + JSON.stringify(scope.gendersArray) + 
                                        "; riskFactor: " + scope.riskFactor +
                                        "; name: " + scope.name +
                                        "; width: " + (scope.width || "N/A") +
                                        "; height: " + (scope.height || "N/A") +
                                        "; riskFactor2FieldName: " + scope.riskFactor2FieldName[scope.riskFactor]);
                                    D3ChartsService.getD3RiskGraph(svg, scope.riskGraphData, scope.gendersArray, 
                                        scope.riskFactor2FieldName[scope.riskFactor], scope.riskFactor, riskGraphCallback);
                                }
                                else {
//                                    AlertService.consoleDebug(
//                                        "[rifd-view-d3riskGraph.js] no change in risk graph data; watchCount: " + 
//                                        watchCount);
                                    if (watchCount > 0) {
                                        watchCount--;
                                    }
                                }
                            }
                            watchCount++;
                            D3ChartsService.hasRiskGraphDataChanged(scope.riskGraphData, scope.gendersArray, 
                                scope.riskFactor2FieldName[scope.riskFactor], scope.riskFactor, 
                                hasRiskGraphDataChangedCallback);
                        }
                    });
                }
            };
            return directiveDefinitionObject;
        }]);