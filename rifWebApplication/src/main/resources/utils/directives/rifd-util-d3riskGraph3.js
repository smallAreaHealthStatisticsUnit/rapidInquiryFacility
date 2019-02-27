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
 *
 * Still to do:
 *
 * 1. Convert to info and viewer risk graphs to using common version with a single data source. Rename rifd-util-d3riskGraph3.js to 
 *    rifd-util-d3riskGraph.js and remove rifd-util-d3riskGraph2.js;
 * 2. Fix info scaler to use d3.select("#???????").node().getBoundingClientRect().height/width. This removes a known bug. Also test with
 *    window resizing;
 * 3. Remove non d3 code from rifs-util-d3charts.js (functionality now in rifd-util-d3riskGraph3.js), rename to rifs-util-d3riskgraph.js;
 * 4. Convert rr-zoom, dist-histo and pyramid directive so the d3 code is in a utils service and now use same the methods as 
 *    rifd-util-d3riskGraph.js. Resizing should now work!
 * 5. Remove rrZoomReset anti memory leak functionality. It should no longer be needed;
 * 6. Multiple redraws in the mapping panes should be remove when the fetch code is all converted to use promises;
 * 7. Add rr-zoom, dist-histo and pyramid to the info modal;
 * 8. Add "NONE" to second gender selector in info risk graph. The work around is to set both to the same;
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

				    /*
					 * Function:    riskGraphCallback()
					 * Parameters:  err, svg, elementName, mapID, newData
					 * Description:	Async callback function. Terminates watcher on error. Checks if SVG graphic was created.
					 *				newData is provided so you can provided your own watcher.
					 * Returns:     N/A
					 */					
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
 
				    /*
					 * Function:    riskGraphCallback()
					 * Parameters:  newValue, oldValue
					 * Description:	Async watcher listener function. Creates SVG graphic on data cnange
					 * Returns:     N/A
					 */	                  
					var riskGraphListener = function(newValue, oldValue) {
						try {
							var svg=d3.select(element[0]).select("svg");
								
							if (newValue && newValue.name && 
								newValue.riskGraphData && Object.keys(newValue.riskGraphData).length > 0) {
									
								// Check data really has changed
								var numDataChanges=D3ChartsService.diffParameters(newValue.name, newValue, oldValue);
								if (numDataChanges > 0) {
											 
									if (angular.isUndefined(newValue.width)) { // Rubbish defaults
										newValue.width=150;
									}
									if (angular.isUndefined(newValue.height)) {
										newValue.height=150;
									}
								
									var elementName="#" + newValue.name;                         
									if (svg && svg.size() > 0) { // Remove old SVG
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
									
									// Create SVG graphic
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
						
					// Watcher function						
                    var unregister = scope.$watch('riskGraphData', riskGraphListener, true /* Uses angular.equals */);
				}
            };
            return directiveDefinitionObject;
        }]);