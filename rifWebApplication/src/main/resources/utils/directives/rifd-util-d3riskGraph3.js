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
                    riskGraphData: '=riskGraphData'     
                },
                link: function (scope, element, attrs) {
              
                    var nullCount=0;
					var processing=false;
					var oldData = {};
					var changeCount=0;
					var timeoutPromise;
					var watcherCount=0;
					
                    var riskGraphCallback = function(err, svg, elementName, mapID, newData) {
                        if (err) {
                            AlertService.showError("rifd-util-d3riskGraph3.js] riskGraph: " + 
								((newData && newData.name) ? newData.name : "no name") + 
                                " error: " + err);
                            unregister();
                        }
                        else if (mapID == undefined) {
                            AlertService.showError("rifd-util-d3riskGraph3.js] riskGraph: no mapID");
                            unregister();
                        }
                        else if (newData == undefined) {
                            AlertService.showError("rifd-util-d3riskGraph3.js] riskGraph: no newData");
                            unregister();
                        }
                        else if (newData.name == undefined) {
                            AlertService.showError("rifd-util-d3riskGraph3.js] riskGraph: no newData.name");
                            unregister();
                        }
                        else if (svg == undefined || svg.size() == 0) {
                            AlertService.showError("rifd-util-d3riskGraph3.js] riskGraph: " + snewData.name + 
                                " div not created");
                            unregister();
                        }
                        else if (elementName == undefined) {
                            AlertService.showError("rifd-util-d3riskGraph3.js] riskGraph: " + newData.name + 
                                " elementName not provided");
                            unregister();
                        }
                        else if (scope.riskGraphData.mapID == undefined) {
                            AlertService.showError("rifd-util-d3riskGraph3.js] riskGraph: " + newData.name + 
                                " mapID not provided");
                            unregister();
                        }
                        else {
							processing=false;
                            AlertService.consoleDebug("rifd-util-d3riskGraph.js] map: " + mapID + 
								"; riskGraph: " + scope.riskGraphData.name + 
                                " created with " + svg.size() + " elements");
                        }
						oldData[mapID]=angular.copy(newData); // Only save now when the data has been used
                    }
                   
					var riskGraphListener = function(newValue, oldValue) {
						try {
							var svg=d3.select(element[0]).select("svg");
								
							if (newValue && newValue.name && 
								newValue.riskGraphData && Object.keys(newValue.riskGraphData).length > 0) {
									
								var numDataChanges=D3ChartsService.diffParameters(newValue.name, newValue, oldValue);
								if (numDataChanges > 0) {
											 
									if (angular.isUndefined(newValue.width)) {
										newValue.width=150;
									}
									if (angular.isUndefined(newValue.height)) {
										newValue.height=150;
									}
								
									var elementName="#" + newValue.name;                         
									if (svg == undefined || svg.size() == 0) {
										if (svg && svg.size() > 0) {
											AlertService.consoleDebug("[rifd-util-d3riskGraph.js] map: " + newValue.mapID + 
												"; has " + numDataChanges + " data changes result: " + 
												"; svg remove: " + newValue.name);
											svg.remove();
										}
										svg = d3.select(element[0]).append("svg");
										
										var hSplit;
										if (newValue.hSplitTag) {
											hSplit=d3.select("#" + newValue.hSplitTag);
										}
										var riskGraphChartCurrentHeight;
										var riskGraphChartCurrentWidth;
										if (hSplit && hSplit.size() > 0) {
											riskGraphChartCurrentHeight = hSplit.node().getBoundingClientRect().height;
											riskGraphChartCurrentWidth = hSplit.node().getBoundingClientRect().width;
										}
										
										callGetD3RiskGraph = function(mapID, callCount) {
											if (!processing) {
												processing=true;
												AlertService.consoleDebug("[rifd-util-d3riskGraph3.js] map: " + newValue.mapID + 
													"; call: " + callCount + " getD3RiskGraph() " +
													"; gendersArray: " + JSON.stringify(newValue.gendersArray) + 
													"; riskFactor: " + newValue.riskFactor +
													"; name: " + newValue.name +
													"; hSplitTag: " + newValue.hSplitTag +
													"; width: " + (newValue.width || riskGraphChartCurrentWidth) +
													"; height: " + (newValue.height || riskGraphChartCurrentHeight) +
													"; riskFactor: " + newValue.riskFactor2FieldName[newValue.riskFactor]);
												D3ChartsService.getD3RiskGraph2(svg, elementName, mapID, newValue, riskGraphCallback);
											}
											else {
												$scope.consoleDebug("[rifc-util-d3riskGraph3.js] map: " + newValue.mapID + 
													"; call: " + callCount + " getD3RiskGraph() " +
													"; chart busy for mapID: " + mapID);	
												setTimeout(callGetD3RiskGraph, 1000, newValue.mapID, (callCount+1));	
											}
										}
										setTimeout(callGetD3RiskGraph, 1000, newValue.mapID, 1);	
									}
									nullCount=0;
								}
								else {
									AlertService.consoleDebug("[rifd-util-d3riskGraph3.js] map: " + scope.riskGraphData.mapID + 
										"; no data changes (call: " + 
										nullCount + ") for: " + ((newValue && newValue.name) ? newValue.name : "No name") + 
										"; riskGraphData newValue: " + JSON.stringify(newValue, 0, 0));
								}
							}
							else { // No data
								if (svg && svg.size() > 0) {
									AlertService.showError("[rifd-util-d3riskGraph3.js] map: " + scope.riskGraphData.mapID + 
										"; no data found (call: " + 
										nullCount + ") for: " + (newValue ? newValue.name : "No name") + "; removing existing SVG graphic");
									svg.remove();
								} 
								else {
									AlertService.consoleDebug("[rifd-util-d3riskGraph3.js] no data found (call: " + 
										nullCount + ") for: " + ((newValue && newValue.name) ? newValue.name : "No name") + 
										"; riskGraphData newValue: " + JSON.stringify(newValue, 0, 0));
								}
								nullCount++;
							}   
						}
						catch (e) {
							AlertService.showError("[rifd-util-d3riskGraph3.js] map: " + scope.riskGraphData.mapID + 
								"; caught exception: " + e.toString());
                            unregister();
						}							
                    }
										
                    var riskGraphWatcher = function(mapID, newData) {
						watcherCount++;
						try {
							if (watcherCount > 1000) {
								throw new Error("[rifd-util-d3riskGraph3.js] map: " + mapID + 
									" AAAA; changeCount: " + changeCount);
							}
							if (oldData && oldData[mapID] && oldData[mapID].riskGraphData &&
								Object.keys(oldData[mapID].riskGraphData).length > 0) {
								if (newData && newData.riskGraphData && Object.keys(newData.riskGraphData).length > 0) {
									if (!angular.equals(newData, oldData[mapID])) {
										changeCount++;						
									AlertService.consoleDebug("[rifd-util-d3riskGraph3.js] map: " + mapID + 
										" change; changeCount: " + changeCount);				
									}
								}
							}
							else if (newData && newData.riskGraphData && Object.keys(newData.riskGraphData).length > 0) { // First run
								AlertService.consoleDebug("[rifd-util-d3riskGraph3.js] map: " + mapID + 
									" first run; changeCount: " + changeCount);
								changeCount++;
							}
							
							return changeCount;
						}
						catch (e) {
							AlertService.showError("[rifd-util-d3riskGraph3.js] map: " + scope.riskGraphData.mapID + 
								"; caught exception: " + e.toString());
                            unregister();
						}	
					}
					
                    var unregister = scope.$watch('riskGraphData', riskGraphListener, true /* Uses angular.equals */);
//                    var unregister = scope.$watch(function() {
//						return riskGraphWatcher(
//							((scope.riskGraphData && scope.riskGraphData.mapID) ? scope.riskGraphData.mapID : "unknown"), 
//							scope.riskGraphData);
//					}, riskGraphListener, false /* Uses angular.equals */);
                }
            };
            return directiveDefinitionObject;
        }]);