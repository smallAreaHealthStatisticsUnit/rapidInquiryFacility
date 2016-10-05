/* global d3 */

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

                    var selected = MappingStateService.getState().selected; //dropline location

                    scope.$watch(function () {

                        if (angular.isUndefined(scope.data)) {
                            return;
                        }
                        if (MappingStateService.getState().rrContainerH === scope.height &&
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

                        var line = d3.svg.line()
                                .interpolate("basis")
                                .x(function (d) {
                                    return x(d[ orderField ]);
                                })
                                .y(function (d) {
                                    return y(d[ lineField ]);
                                });

                        var x = d3.scale.linear()
                                .range([0, xWidth]);

                        var x2 = d3.scale.linear()
                                .range([0, xWidth]);

                        var y = d3.scale.linear()
                                .range([xHeight, 0]);

                        var y2 = d3.scale.linear()
                                .range([xHeight, 0]);

                        var xAxis = d3.svg.axis()
                                .scale(x)
                                .orient("bottom");

                        var yAxis = d3.svg.axis()
                                .scale(y)
                                .orient("left");

                        var xAxis2 = d3.svg.axis()
                                .scale(x2)
                                .orient("bottom");

                        var yAxis2 = d3.svg.axis()
                                .scale(y2)
                                .orient("left");

                        var area = d3.svg.area()
                                .x(function (d) {
                                    return x(d[ orderField ]);
                                })
                                .y0(function (d) {
                                    return y(d[ lowField ]);
                                })
                                .y1(function (d) {
                                    return y(d[ highField ]);
                                });

                        d3.select("#rrchart").remove();

                        if (xWidth > 0 && xHeight > 0) {

                            var svg = d3.select(element[0]).append("svg")
                                    .attr("width", scope.width)
                                    .attr("height", xHeight + margin.bottom + margin.top)
                                    .attr("id", "rrchart");

                            svg.append("defs").append("clipPath")
                                    .attr("id", "clipchart")
                                    .append("rect")
                                    .attr("width", scope.width)
                                    .attr("height", xHeight);

                            var focus = svg.append("g")
                                    .attr("class", "focus")
                                    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

                            var xDomain = d3.extent(scope.data, function (d) {
                                return d[ orderField ];
                            });

                            x.domain(xDomain);
                            x2.domain(xDomain);

                            y.domain([d3.min(scope.data, function (d) {
                                    return d[ lowField ] - 0.2;
                                }), d3.max(scope.data, function (d) {
                                    return d[ highField ] + 0.2;
                                })]);

                            y2.domain([d3.min(scope.data, function (d) {
                                    return d[ lowField ] - 0.2;
                                }), d3.max(scope.data, function (d) {
                                    return d[ highField ] + 0.2;
                                })]);

                            focus.append("path")
                                    .datum(scope.data)
                                    .attr("class", "area")
                                    .attr("d", area)
                                    .attr("clip-path", "url(#clipchart)");

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
                                    .attr("id", "lineRisk")
                                    .attr("clip-path", "url(#clipchart)")
                                    .attr("d", line);

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
                            if (selected !== null) {
                                highlighter.attr("transform", "translate(" + x(selected.x_order) + "," + 0 + ")");
                                pointerLighter.attr("transform", "translate(" + x(selected.x_order) + "," + y(selected.srr) + ")");
                                currentFigures.text(selected.srr.toFixed(3) + " (" + selected.cl.toFixed(2) + " - " + selected.ul.toFixed(2) + ")");
                            }
                        };

                        //add dropLine on container resize
                        scope.updateLine();

                        //add dropLine on area plot events
                        scope.$on('rrDropLineRedraw', function (event, data) {
                            for (var i = 0; i < dataLength; i++) {
                                if (scope.data[i].gid === data) {
                                    selected = scope.data[i];
                                }
                            }
                            scope.updateLine();
                        });
                    };
                }
            };
            return directiveDefinitionObject;
        });