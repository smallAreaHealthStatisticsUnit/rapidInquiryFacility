/* global d3 */
//https://bl.ocks.org/mbostock/4349545

angular.module("RIF")
        .directive('rrChart', function (MappingStateService) { //rr-chart
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

                    scope.$watch(function () {
                        if (angular.isUndefined(scope.data)) {
                            return;
                        }
                        if (MappingStateService.getState().cleanState) {
                            MappingStateService.getState().cleanState = false;
                            scope.renderBase();
                        }                      
                        if (MappingStateService.getState().rrContainerH === scope.height &
                                MappingStateService.getState().rrContainerV === scope.width) {
                            return;
                        } else {
                            MappingStateService.getState().rrContainerV = scope.width;
                            MappingStateService.getState().rrContainerH = scope.height;
                            scope.renderBase();
                        }
                    });

                    scope.renderBase = function () {

                        var margin = {top: 30, right: 20, bottom: 50, left: 40};
                        var xHeight = scope.height - margin.top - margin.bottom;
                        var xWidth = scope.width - margin.left - margin.right - 4;

                        var idField = scope.opt.id_field;
                        var orderField = scope.opt.x_field;
                        var lineField = scope.opt.risk_field;
                        var lowField = scope.opt.cl_field;
                        var highField = scope.opt.cu_field;
                        var dataLength = scope.data.length;

                        //the drop reference line
                        var selected = MappingStateService.getState().selected;

                        //x scale linked to area graphs
                        var x = d3.scaleLinear()
                                .range([0, xWidth]);
                        if (MappingStateService.getState().brushStartLoc === null) {
                            x.domain(d3.extent(scope.data, function (d) {
                                return d[orderField];
                            }));
                        } else {
                            x.domain([MappingStateService.getState().brushStartLoc, MappingStateService.getState().brushEndLoc]);
                        }

                        var y = d3.scaleLinear()
                                .domain([d3.min(scope.data, function (d) {
                                        return d[ lowField ] - 0.2;
                                    }), d3.max(scope.data, function (d) {
                                        return d[ highField ] + 0.2;
                                    })])
                                .range([xHeight, 0]);

                        var xAxis = d3.axisBottom()
                                .scale(x);

                        var yAxis = d3.axisLeft()
                                .scale(y);

                        var line = d3.line()
                                .x(function (d) {
                                    return x(d[ orderField ]);
                                })
                                .y(function (d) {
                                    return y(d[ lineField ]);
                                });

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

                        var refLine = d3.line()
                                .x(function (d) {
                                    return x(d[ orderField ]);
                                })
                                .y(function (d) {
                                    return y(1);
                                });

                        d3.select("#rrchart").remove();

                        //ensure draw only when the ui-layout panel has a size
                        if (xWidth > 0 && xHeight > 0) {

                            var svg = d3.select(element[0]).append("svg")
                                    .attr("width", scope.width)
                                    .attr("height", xHeight + margin.bottom + margin.top)
                                    .attr("id", "rrchart");

                            var focus = svg.append("g")
                                    .attr("class", "focus")
                                    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

                            focus.append("clipPath")
                                    .attr("id", "plot-clip")
                                    .append("rect")
                                    .attr("width", xWidth)
                                    .attr("height", xHeight);

                            focus.append("path")
                                    .datum(scope.data)
                                    .attr("class", "area")
                                    .attr("clip-path", "url(#plot-clip)")
                                    .attr("d", area);

                            focus.append("path")
                                    .datum(scope.data)
                                    .attr("class", "line")
                                    .attr("id", "lineRisk")
                                    .attr("clip-path", "url(#plot-clip)")
                                    .attr("d", line);

                            focus.append("g")
                                    .attr("class", "x axis")
                                    .attr("transform", "translate(0," + xHeight + ")")
                                    .call(xAxis);

                            focus.append("g")
                                    .attr("class", "y axis")
                                    .call(yAxis);

                            svg.append("text")
                                    .attr("transform", "translate(60,20)")
                                    .attr("id", "labelLineBivariate")
                                    .text(lineField);

                            var currentFigures = svg.append("text")
                                    .attr("transform", "translate(60,40)")
                                    .attr("id", "currentFiguresLineBivariate")
                                    .text("");

                            focus.append("path")
                                    .datum(scope.data)
                                    .attr("class", "line")
                                    .attr("id", "refLine")
                                    .attr("clip-path", "url(#plot-clip)")
                                    .attr("d", refLine);

                            var highlighter = focus.append("line")
                                    .attr("x1", 0)
                                    .attr("y1", 0)
                                    .attr("x2", 0)
                                    .attr("y2", xHeight)
                                    .attr("height", xHeight)
                                    .attr("class", "bivariateHiglighter");

                            var pointerLighter = focus.append("circle")
                                    .attr("r", 3)
                                    .attr("class", "bivariateHiglighter");
                        }

                        scope.updateLine = function () {

                            d3.select("#bivariateHiglighter").remove();

                            if (selected !== null) {
                                if (selected.x_order > x.domain()[0] && selected.x_order < x.domain()[1]) {
                                    highlighter.style("stroke", "#EEA9B8");
                                    pointerLighter.style("stroke", "#EEA9B8");
                                    pointerLighter.style("fill", "#EEA9B8");
                                    highlighter.attr("transform", "translate(" + x(selected.x_order) + "," + 0 + ")");
                                    pointerLighter.attr("transform", "translate(" + x(selected.x_order) + "," + y(selected.srr) + ")");
                                } else {
                                    highlighter.style("stroke", "transparent");
                                    pointerLighter.style("stroke", "transparent");
                                    pointerLighter.style("fill", "transparent");
                                }
                                currentFigures.text(selected.srr.toFixed(3) + " (" + selected.cl.toFixed(2) + " - " + selected.ul.toFixed(2) + ")");
                            } else {
                                highlighter.style("stroke", "transparent");
                                pointerLighter.style("stroke", "transparent");
                                pointerLighter.style("fill", "transparent");
                                highlighter.attr("transform", "translate(" + x(0) + "," + 0 + ")");
                                pointerLighter.attr("transform", "translate(" + x(0) + "," + y(1) + ")");
                                currentFigures.text("");
                            }
                        };

                        //add dropLine on container resize
                        scope.updateLine();

                        //add dropLine on area plot events
                        scope.$on('rrDropLineRedraw', function (event, data) {
                            selected = null;
                            for (var i = 0; i < dataLength; i++) {
                                if (scope.data[i].gid === data) {
                                    selected = scope.data[i];
                                    MappingStateService.getState().selected = selected;
                                }
                            }
                            scope.updateLine();
                        });

                        //limit xaxis on brush from areaplot panel
                        scope.$on('brushXaxisChange', function (event, data) {
                            x.domain(data);
                            focus.select("g")
                                    .attr("class", "x axis")
                                    .call(xAxis);
                            focus.select(".area").attr("d", area);
                            focus.select(".line").attr("d", line);
                            scope.updateLine();
                        });

                    };
                }
            };
            return directiveDefinitionObject;
        });