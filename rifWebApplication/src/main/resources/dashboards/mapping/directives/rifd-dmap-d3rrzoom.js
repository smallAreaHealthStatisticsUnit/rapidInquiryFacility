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
 * DIRECTIVE for D3 RR chart rr-zoom
 * http://bl.ocks.org/mbostock/34f08d5e11952a80609169b7917d4172
 */

/* global d3, Infinity */

angular.module("RIF")
        .directive('rrZoom', function ($rootScope, MappingStateService, ParametersService) { //rr-zoom

			var parameters=ParametersService.getParameters()||{
				rrDropLineRedrawDisabled: false,		// Disable rrDropLineRedraw handler (leak debugging), If set to true stops leak!
				rrchartWatchDisabled: false				// Disable Angular $watch on rrchart<mapID> [for leak testing]
			};
			var rrDropLineRedrawDisabled=parameters.rrDropLineRedrawDisabled;
			var rrchartWatchDisabled=parameters.rrchartWatchDisabled;
			var watchCall = {
                    'diseasemap1': 0,
                    'diseasemap2': 0,
                    'viewermap': 0
                };
			watchCallDone = {
                    'diseasemap1': false,
                    'diseasemap2': false,
                    'viewermap': false
                };
				
            var directiveDefinitionObject = {
                restrict: 'E',
                replace: false,
                scope: {
                    data: '=chartData',
                    opt: '=options',
                    width: '=width',
                    height: '=height'
                },
                link: function (scope, element, attrs) {

                    //listener for global key event
                    var keyListener;
                    element.on('$destroy', function () {
                        //remove zombies
                        if (!angular.isUndefined(keyListener)) {
                            keyListener();
                        }
                    });

					if (!rrchartWatchDisabled) {
						scope.$on('rrZoomReset', function (event, rrZoomReset) { // Allow refresh
							$rootScope.$broadcast('rrZoomStatus', {level: "DEBUG", msg: "watchCall reset at: " + watchCall[scope.opt.panel] + 
								"; map: " + scope.opt.panel + "; watchCallDone[scope.opt.panel]: " + watchCallDone[scope.opt.panel]});
							watchCallDone[scope.opt.panel]=false;
							watchCall[scope.opt.panel]=0;
						});
						
						scope.$watch(function (scope) { // watchExpression is called on every call to $digest() and should return the value that will be watched
							watchCall[scope.opt.panel]++;
							
							if (angular.isUndefined(scope.data) || scope.data.length === 0) {
								d3.select("#rrchart" + scope.opt.panel).remove();
								watchCallDone[scope.opt.panel]=false;
								return watchCallDone[scope.opt.panel];
							} 
							else if (watchCall[scope.opt.panel] < 3 /* Always the first */ && !watchCallDone[scope.opt.panel]) { // Limit to 10 times to prevent $digest complaining	
							
//								$rootScope.$broadcast('rrZoomStatus', {level: "DEBUG", msg: "Pre call renderBase for map: " + scope.opt.panel + 
//									"; watchCall" + scope.opt.panel + ": " + watchCall[scope.opt.panel] +
//									"; watchCallDone[" + scope.opt.panel + "]: " + watchCallDone[scope.opt.panel]});	
								watchCallDone[scope.opt.panel]=true; // This is to prevent further calls
//								$rootScope.$broadcast('rrZoomStatus', {level: "DEBUG", msg: "Call renderBase for map: " + scope.opt.panel + 
//									"; watchCall[" + scope.opt.panel + "]: " + watchCall[scope.opt.panel] +
//									"; watchCallDone[" + scope.opt.panel + "]: " + watchCallDone[scope.opt.panel]});	
								scope.renderBase();	
								return watchCallDone[scope.opt.panel];
							}
							else {
								return watchCallDone[scope.opt.panel];
							}
						}, 
						undefined /* No listener function */, 
						undefined /* No check equality */);
					}

                    scope.renderBase = function () {
                        var margin = {top: 30, right: 20, bottom: 30, left: 60};
                        var xHeight = scope.height - margin.top - (margin.bottom + scope.height * 0.3);
                        var xHeight2 = scope.height - margin.top - (margin.bottom + scope.height * 0.7);
                        var xWidth = scope.width - margin.left - margin.right - 4;
                        var orderField = scope.opt.x_field;
                        var lineField = scope.opt.risk_field;
                        var lowField = scope.opt.cl_field;
                        var highField = scope.opt.cu_field;
                        var dataLength = scope.data.length;
                        var labelField = scope.opt.label_field;
                        var panel = scope.opt.panel;

						if (lineField == undefined) {
							$rootScope.$broadcast('rrZoomStatus', {level: "WARNING", msg: "renderBase: Failed for map: " + panel + 
								"; risk_field not defined"});
							return;
						}
						if (!scope.data) {
							$rootScope.$broadcast('rrZoomStatus', {level: "WARNING", msg: "renderBase: Failed for map: " + panel + 
								"; no data in scope"});
							return;				
						}
		
                        //Plot confidence interal areas on the charts?
                        var bConfidence = true;
                        if (labelField === "posterior_probability") {
                            bConfidence = false;
                        }

                        var x = d3.scaleLinear().range([0, xWidth]);
                        var x2 = d3.scaleLinear().range([0, xWidth]);

                        x.domain(d3.extent(scope.data, function (d) {
                            return d[orderField];
                        }));

                        x2.domain(x.domain());

                        var y;

                        if (bConfidence) {
                            y = d3.scaleLinear()
                                    .domain([d3.min(scope.data, function (d) {
                                            return d[ lowField ];
                                        }), d3.max(scope.data, function (d) {
                                            return d[ highField ];
                                        })])
                                    .range([xHeight, 0]);
                        } else {
                            y = d3.scaleLinear()
                                    .domain([0, 1]) //probability so fixed
                                    .range([xHeight, 0]);
                        }

                        var y2 = d3.scaleLinear().range([xHeight2, 0]);

                        y2.domain(y.domain());

                        //If domain is constant, do not plot
                        var domainCheck = [d3.min(scope.data, function (d) {
                                return d[ lineField ];
                            }), d3.max(scope.data, function (d) {
                                return d[ lineField ];
                            })];
                        if (!angular.isUndefined(domainCheck[0])) {
                            if (domainCheck[0].toFixed(5) === domainCheck[1].toFixed(5)) {
                                d3.select("#rrchart" + panel).remove();
								$rootScope.$broadcast('rrZoomStatus', {level: "WARNING", msg: "renderBase: Failed for map: " + panel + 
									"; data max in and values are the same: " + domainCheck[0].toFixed(5)});
								watchCallDone[panel]=false;
                                return;
                            }
                        } else {
							$rootScope.$broadcast('rrZoomStatus', {level: "WARNING", msg: "renderBase: Failed for map: " + panel + 
								"; no data for risk_field: " + lineField});
							watchCallDone[panel]=false;
                            return;
                        }

//						$rootScope.$broadcast('rrZoomStatus', {level: "DEBUG", msg: "renderBase: for map: " + panel + 
//							"; Data OK; watchCallDone[panel]: " + watchCallDone[panel]});
                        var xAxis = d3.axisBottom().scale(x).ticks(0);
                        var xAxis2 = d3.axisBottom().scale(x2);
                        var yAxis = d3.axisLeft().scale(y);

                        var brush = d3.brushX()
                                .extent([[0, 0], [xWidth, xHeight2]])
                                .on("start brush", brushed);

                        var zoom = d3.zoom()
                                .scaleExtent([1, Infinity])
                                .translateExtent([[0, 0], [xWidth, xHeight]])
                                .extent([[0, 0], [xWidth, xHeight]]);

                        var line = d3.line()
                                .x(function (d) {
                                    return x(d[ orderField ]);
                                })
                                .y(function (d) {
                                    return y(d[ lineField ]);
                                });
                        var line2 = d3.line()
                                .x(function (d) {
                                    return x2(d[ orderField ]);
                                })
                                .y(function (d) {
                                    return y2(d[ lineField ]);
                                });

                        if (bConfidence) {
                            //draw confidence intervals
                            var area = d3.area()
                                    .x(function (d) {
                                        return x(d[ orderField ]);
                                    })
                                    .y0(function (d) {
                                        return y(d[ lowField ]);
                                    })
                                    .y1(function (d) {
                                        return y(d[ highField ]);
                                    });
                            var area2 = d3.area()
                                    .x(function (d) {
                                        return x2(d[ orderField ]);
                                    })
                                    .y0(function (d) {
                                        return y2(d[ lowField ]);
                                    })
                                    .y1(function (d) {
                                        return y2(d[ highField ]);
                                    });
                            var refLine = d3.line()
                                    .x(function (d) {
                                        return x(d[ orderField ]);
                                    })
                                    .y(function (d) {
                                        return y(1);
                                    });
                        }

                        d3.select("#rrchart" + panel).remove();

                        //graph canvas
                        var svg = d3.select(element[0]).append("svg")
                                .attr("width", scope.width)
                                .attr("height", scope.height)
                                .attr("id", "rrchart" + panel);

                        svg.append("rect")
                                .attr("class", "focusBackground")
                                .attr("width", xWidth)
                                .attr("height", xHeight)
                                .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

                        var focus = svg.append("g")
                                .attr("class", "focus")
                                .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

                        var context = svg.append("g")
                                .attr("class", "context")
                                .attr("transform", "translate(" + margin.left + "," + (xHeight + margin.bottom + margin.top) + ")");

                        //clip-path clashes on multiple instance so needs unique id                    
                        focus.append("clipPath")
                                .attr("id", "plot-clip" + panel)
                                .append("rect")
                                .attr("width", xWidth)
                                .attr("height", xHeight);

                        focus.append("path")
                                .datum(scope.data)
                                .attr("class", "area")
                                .attr("clip-path", "url(#plot-clip" + panel + ")")
                                .attr("d", area);

                        focus.append("path")
                                .datum(scope.data)
                                .attr("class", "line")
                                .attr("id", "lineRisk")
                                .attr("clip-path", "url(#plot-clip" + panel + ")")
                                .attr("d", line);

                        focus.append("path")
                                .datum(scope.data)
                                .attr("class", "line")
                                .attr("id", "refLine")
                                .attr("clip-path", "url(#plot-clip" + panel + ")")
                                .attr("d", refLine);

                        focus.append("g")
                                .attr("class", "axis x")
                                .attr("transform", "translate(0," + xHeight + ")")
                                .call(xAxis);

                        focus.append("g")
                                .attr("class", "y axis")
                                .call(yAxis);

                        if (bConfidence) {
                            context.append("path")
                                    .datum(scope.data)
                                    .attr("class", "area")
                                    .attr("d", area2);
                        } else {
                            //draw probability classes
                            focus.append("rect")
                                    .attr("class", "prob1")
                                    .attr("x", 0)
                                    .attr("y", y(0.19))
                                    .attr("width", xWidth)
                                    .attr("height", y(0.81));

                            focus.append("rect")
                                    .attr("class", "prob2")
                                    .attr("x", 0)
                                    .attr("y", y(0.81))
                                    .attr("width", xWidth)
                                    .attr("height", y(0.38));

                            focus.append("rect")
                                    .attr("class", "prob3")
                                    .attr("x", 0)
                                    .attr("y", y(1))
                                    .attr("width", xWidth)
                                    .attr("height", y(0.81));
                        }

                        context.append("path")
                                .datum(scope.data)
                                .attr("class", "line")
                                .attr("id", "lineRisk2")
                                .attr("d", line2);

                        context.append("g")
                                .attr("transform", "translate(0," + xHeight2 + ")")
                                .call(xAxis2);

                        //set brush handle ends
                        var startLoc = scope.opt.zoomStart;
                        var endLoc = scope.opt.zoomEnd;
                        if (startLoc === null) {
                            startLoc = x2.range()[0];
                        }
                        if (endLoc === null) {
                            endLoc = x2.range()[1];
                        }

                        var gBrush = context.append("g")
                                .attr("class", "brush")
                                .call(brush)
                                .call(brush.move, [scope.opt.zoomStart, scope.opt.zoomEnd].map(x2, x2.invert));

                        svg.append("rect")
                                .attr("class", "zoom")
                                .attr("width", xWidth)
                                .attr("height", xHeight)
                                .attr("transform", "translate(" + margin.left + "," + margin.top + ")")
                                .on("click", function (d) {
                                    scope.$parent.mapInFocus = panel;
                                    var xy = d3.mouse(this);
                                    scope.clickXPos = snapToBounds(xy[0]);
                                    broadcastAreaUpdate({xpos: scope.clickXPos, map: true});
                                })
                                .call(zoom);

                        function broadcastAreaUpdate(data) {
                            //get selected from x_order
                            for (var i = 0; i < dataLength; i++) {
                                if (scope.data[i].x_order === data.xpos) {
                                    selected = scope.data[i];
                                    MappingStateService.getState().selected[panel] = scope.data[i];
                                    break;
                                }
                            }
                            $rootScope.$broadcast('syncMapping2Events', {selected: selected, mapID: panel, map: data.map});
                        }

                        function snapToBounds(mouseX) {
                            var val = Math.round(x.invert(mouseX));
                            return (val < 0) ? 0 : (val > dataLength) ? dataLength - 1 : val;
                        }

                        var highlighter = focus.append("line")
                                .attr("x1", 0)
                                .attr("y1", 0)
                                .attr("x2", 0)
                                .attr("y2", xHeight)
                                .attr("height", xHeight)
                                .attr("class", "bivariateHiglighter")
                                .attr("id", "bivariateHiglighter1" + panel);

                        var highlighter2 = context.append("line")
                                .attr("x1", 0)
                                .attr("y1", 0)
                                .attr("x2", 0)
                                .attr("y2", xHeight2)
                                .attr("height", xHeight2)
                                .attr("class", "bivariateHiglighter")
                                .attr("id", "bivariateHiglighter2" + panel);

                        svg.append("text")
                                .attr("transform", "translate(" + margin.left + "," + (margin.top - 5) + ")")
                                .attr("id", "labelLineBivariate")
                                .text(labelField.replace("_", " "));

                        var currentFigures = svg.append("text")
                                .attr("transform", "translate(" + (margin.left + 150) + "," + (margin.top - 5) + ")")
                                .attr("class", "currentFiguresLineBivariate")
                                .attr("id", "currentFiguresLineBivariate" + panel)
                                .text("");

                        //the drop reference line
                        var selected = MappingStateService.getState().selected[panel];
                        if (selected !== null) {
                            focus.select("#bivariateHiglighter1" + panel).attr("transform", "translate(" + x(selected.x_order) + "," + 0 + ")");
                            context.select("#bivariateHiglighter2" + panel).attr("transform", "translate(" + x2(selected.x_order) + "," + 0 + ")");
                            if (angular.isNumber(selected.rr)) {
                                if (bConfidence) {
                                    svg.select("#currentFiguresLineBivariate" + panel).text(selected.rr.toFixed(3) + " (" + selected.cl.toFixed(3) +
                                            " - " + selected.ul.toFixed(3) + ")");
                                } else {
                                    svg.select("#currentFiguresLineBivariate" + panel).text(selected.rr.toFixed(3));
                                }
                            } else {
                                svg.select("#currentFiguresLineBivariate" + panel).text("Invalid results");
                            }
                        }

                        function brushed() {
                            if (d3.event.sourceEvent && d3.event.sourceEvent.type === "zoom") {
                                return; // ignore brush-by-zoom
                            }
                            var s = d3.event.selection || x2.range();
                            var si = s.map(x2.invert, x2);
                            x.domain(si);

                            focus.select(".area").attr("d", area);
                            focus.select(".line").attr("d", line);
                            focus.select(".axis--x").call(xAxis);
                            svg.select(".zoom").call(zoom.transform, d3.zoomIdentity
                                    .scale(xWidth / (s[1] - s[0]))
                                    .translate(-s[0], 0));

                            //ignore if brush has not changed
                            if (MappingStateService.getState().brushStartLoc[panel] === si[0] &&
                                    MappingStateService.getState().brushEndLoc[panel] === si[1]) {
                                return;
                            } else {
                                //ensure highlighter reset on clear
                                if (MappingStateService.getState().selected[panel] === null) {
                                    scope.clickXPos = 0;
                                }
                                //remember brush locations
                                scope.$parent.optionsRR[panel].zoomStart = si[0];
                                scope.$parent.optionsRR[panel].zoomEnd = si[1];
                                MappingStateService.getState().brushStartLoc[panel] = si[0];
                                MappingStateService.getState().brushEndLoc[panel] = si[1];
                                broadcastAreaUpdate({xpos: scope.clickXPos, map: false});
                            }
                        }

                        //add dropLine on map select events
                        scope.$on('rrDropLineRedraw', function (event, data, container) {
							if (!rrDropLineRedrawDisabled) {
								//get selected from area_id
								if (panel === container) {
									selected = null;
									for (var i = 0; i < dataLength; i++) {
										if (!angular.isUndefined(scope.data[i])) {
											if (scope.data[i].gid === data) {
												selected = scope.data[i];
												scope.clickXPos = scope.data[i].x_order;
												MappingStateService.getState().selected[container] = scope.data[i];
												break;
											}
										}
									}

									if (selected !== null) {
										
										context.select("#bivariateHiglighter2" + panel).attr("transform", "translate(" + x2(selected.x_order) + "," + 0 + ")");
										if (angular.isNumber(selected.rr)) {
											if (bConfidence) {
												svg.select("#currentFiguresLineBivariate" + panel).text(selected.rr.toFixed(3) +
														" (" + selected.cl.toFixed(3) + " - " + selected.ul.toFixed(3) + ")");
											} else {
												svg.select("#currentFiguresLineBivariate" + panel).text(selected.rr.toFixed(3));
											}
										} else {
											svg.select("#currentFiguresLineBivariate" + panel).text("Invalid results");
										}
										//is highlighter out of x range?
										if (selected.x_order >= x.domain()[0] && selected.x_order <= x.domain()[1]) {
											focus.select("#bivariateHiglighter1" + panel).attr("transform", "translate(" + x(selected.x_order) + "," + 0 + ")");
											highlighter.style("stroke", "#EEA9B8");
										} else {
											highlighter.style("stroke", "transparent");
										}
									} else {
										scope.clickXPos = 0;
										MappingStateService.getState().selected[container] = null;
									}
								}
							}
                        });

                        //handle left, right key events   
                        if (angular.isUndefined(keyListener)) {
                            if (angular.isUndefined(scope.$$listeners.rrKeyEvent)) {
                                keyListener = scope.$on('rrKeyEvent', function (event, up, keyCode, container) {
                                    if (panel === container) {
                                        if (keyCode === 37) { //left minus
                                            if (!up & scope.clickXPos - 1 > 0) {
                                                scope.clickXPos--;
                                            }
                                        } else if (!up & keyCode === 39) { //right plus
                                            if (scope.clickXPos + 1 <= dataLength) {
                                                scope.clickXPos++;
                                            }
                                        }
                                        if (keyCode === 37 || keyCode === 39) {
                                            broadcastAreaUpdate({xpos: scope.clickXPos, map: up});
                                        }
                                    }
                                });
                            }
                        }
						
						$rootScope.$broadcast('rrZoomStatus', {level: "DEBUG", msg: "renderBase: for map: " + panel + "; Completed; watchCallDone[panel]: " + watchCallDone[panel]});
                    }; // End of  scope.renderBase
					
					$rootScope.$broadcast('rrZoomStatus', {level: "DEBUG", msg: "INIT complete for map: " + scope.opt.panel});
                } // End of init()
            };
            return directiveDefinitionObject;
        });