/* 
 * D3 Multiple Area Plots for disease mapping
 */

//https://bl.ocks.org/mbostock/4349545
//http://bl.ocks.org/mbostock/34f08d5e11952a80609169b7917d4172
//http://stackoverflow.com/questions/25872777/d3-hover-chart-thousands-of-x-entries-mouse-sensitivity 

/* global d3, Infinity */

angular.module("RIF")
        .directive('mareaChart', function ($rootScope, MappingStateService) { //marea-chart
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
                        if (MappingStateService.getState().areaContainerV === scope.width) {
                            return;
                        } else {
                            MappingStateService.getState().areaContainerV = scope.width;
                            scope.renderBase();
                        }
                    });

                    scope.renderBase = function () {

                        var idField = scope.opt.id_field;
                        var orderField = scope.opt.x_field;
                        var lineField = scope.opt.risk_field;
                        var plotsCount = scope.opt.rSet;
                        var length = scope.data[0].length;

                        var n = [];
                        var chartList = [];
                        for (var i = 0; i < plotsCount; i++) {
                            n.push(i);
                            chartList.push("areaChart" + (i + 1));
                        }

                        var hGridPadding = 30;
                        var margin = {top: 5, right: 20, bottom: 0, left: 45};
                        var scopeHeight = scope.height - margin.top - margin.bottom;
                        var xHeight = scope.height / plotsCount;
                        var xWidth = scope.width - margin.left - margin.right;

                        //area plot click handling
                        var areaSetIndex = MappingStateService.getState().selectedSet;

                        //Define the scales and domains for each chart
                        var x = d3.scaleLinear().range([0, xWidth]); //x is the same for all
                        var y = []; //y varies by result set
                        for (var i = 0; i < plotsCount; i++) {
                            y.push(d3.scaleLinear().range([(xHeight * (i + 1)) - hGridPadding, (xHeight * (i))]));
                            var xDomain = d3.extent(scope.data[i], function (d) {
                                return d[orderField];
                            });
                            x.domain(xDomain);
                            var yDomain = d3.extent(scope.data[i], function (d) {
                                return d[lineField];
                            });
                            y[i].domain(yDomain);
                        }

                        //clear existing charts
                        d3.select("#areaCharts").remove();

                        //Define the canvas for all graphs
                        var svg = d3.select(element[0]).append("svg")
                                .attr("width", scope.width)
                                .attr("height", scopeHeight + margin.bottom + margin.top)
                                .attr("id", "areaCharts");

                        //Draw the plot backgrounds
                        if (xWidth > 0 && xHeight - hGridPadding > 0) {
                            var rectangle = svg.selectAll("rect")
                                    .data(n).enter()
                                    .append("rect")
                                    .attr("id", function (d) {
                                        return "rect" + (d + 1);
                                    })
                                    .attr("x", 0)
                                    .attr("y", function (d) {
                                        return xHeight * d;
                                    })
                                    .attr("width", xWidth)
                                    .attr("height", xHeight - hGridPadding)
                                    .attr("transform", "translate(" + margin.left + "," + margin.top + ")")
                                    .style('opacity', 0.5)
                                    .style('fill', '#ededed');

                            var focus = svg.append("g")
                                    .attr("class", "focus")
                                    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

                            //Draw the plots on the backround rectangles
                            var areaClickXPos = 0;
                            svg.selectAll('rect').each(function (i) {
                                var yAxis = d3.axisLeft()
                                        .scale(y[i]);

                                var area = d3.area()
                                        .x(function (d) {
                                            return x(d[orderField]);
                                        })
                                        .y0(function (d) {
                                            if (d[lineField] < 1) {
                                                return y[i](d[lineField]);
                                            } else {
                                                return y[i](1);
                                            }
                                        })
                                        .y1(function (d) {
                                            if (d[lineField] < 1) {
                                                return y[i](1);
                                            } else {
                                                return y[i](d[lineField]);
                                            }
                                        });

                                focus.append("g")
                                        .attr("class", "y axis")
                                        .call(yAxis);

                                focus.append("path")
                                        .datum(scope.data[i])
                                        .attr("class", "areaChart" + (i + 1))
                                        .attr("id", "areaChart" + (i + 1))
                                        .attr("d", area)
                                        .on("click", function (d) {
                                            var xy = d3.mouse(this);
                                            areaClickXPos = snapToBounds(xy[0]);
                                            areaSetIndex = d3.select(this).attr("id");
                                            MappingStateService.getState().selectedSet = areaSetIndex;
                                            //  MappingStateService.getState().selected.x_order = areaClickXPos;
                                            var nPos = chartList.indexOf(areaSetIndex);
                                            var thisGid = scope.data[nPos][areaClickXPos].gid;
                                            $rootScope.$broadcast('syncMappingEvents', thisGid, true);
                                        });

                                var currentFigures = svg.append("text")
                                        .attr("transform", "translate(50," + (25 + (xHeight * i)) + ")")
                                        .attr("id", "currentFiguresLineBivariate")
                                        .text("ResultSet " + i);

                                //Zoom-brush box on 1st graph
                                if (i === 0) {
                                    var brush = d3.brushX()
                                            .extent([[0, 0], [xWidth, xHeight - hGridPadding]])
                                            .on("start brush end", brushmoved);

                                    var gBrush = focus.append("g")
                                            .attr("class", "brush")
                                            .call(brush);

                                    var brushStartLoc = x.domain()[0];
                                    var brushEndLoc = x.domain()[1];

                                    gBrush.call(brush.move, [brushStartLoc, brushEndLoc].map(x));

                                    function brushmoved() {
                                        //broadcast extent xmin-xmax

                                        var s = d3.event.selection || x.range();
                                        var sx = s.map(x.invert, x);

                                        //var s = d3.event.selection;
                                        //var sx = s.map(x.invert);
                                        $rootScope.$broadcast('brushXaxisChange', sx);
                                    }
                                }
                            });
                        }

                        scope.$on('areaDropLineRedraw', function (event, data) {
                            updateLines(data); //data is the GID
                        });

                        //draw the droplines
                        function updateLines(gid) {
                            //remove existing lines
                            d3.selectAll(".dropLine").remove();
                            d3.selectAll(".areaValue").remove();

                            MappingStateService.getState().gid = gid;

                            svg.selectAll('rect').each(function (i) {

                                //skip if this is the brushing rectangle
                                if (angular.isUndefined(i.type)) {

                                    //get the x position for i
                                    var thisX = 0;
                                    var thisY = 0;
                                    for (var j = 0; j < length; j++) {
                                        if (scope.data[i][j].gid === gid) {
                                            thisX = scope.data[i][j].x_order;
                                            thisY = scope.data[i][j].srr;
                                            break;
                                        }
                                    }
                                    //draw the lines
                                    focus.append("line")
                                            .attr('class', 'dropLine')
                                            .attr('x1', x(thisX))
                                            .attr('x2', x(thisX))
                                            .attr('y1', y[i](y[i].domain()[0]))
                                            .attr('y2', y[i](y[i].domain()[1]))
                                            .attr("stroke", "#7fa7c9")
                                            .attr("stroke-width", 1);

                                    focus.append("text")
                                            .attr("transform", "translate(110," + (20 + xHeight * i) + ")")
                                            .attr("class", "areaValue")
                                            .text(thisY.toFixed(3));
                                }
                            });
                        }

                        //Add dropLines on container resize
                        if (MappingStateService.getState().gid !== null) {
                            var thisGid = MappingStateService.getState().gid;
                            updateLines(thisGid);
                        }

                        //ensure that the click is within range
                        var snapToBounds = function (mouseX) {
                            var val = Math.round(x.invert(mouseX));
                            return (val < 0) ? 0 : (val > length) ? length - 1 : val;
                        };

                        //keydown event for increments
                        d3.select("body").on("keydown", keyDown);
                        d3.select("body").on("keyup", keyUp);
                        function keyDown() {
                            var code = d3.event.keyCode;
                            var nPos = chartList.indexOf(areaSetIndex);
                            var xPosition = 0;

                            for (var i = 0; i < scope.data[nPos].length; i++) {
                                if (MappingStateService.getState().gid === scope.data[nPos][i].gid) {
                                    xPosition = scope.data[nPos][i].x_order;
                                    break;
                                }
                            }
                            if (code === 37) {
                                //left arrow
                                if (xPosition - 1 > 0) {
                                    xPosition--;
                                    areaIncrementShift();
                                }
                            } else if (code === 39) {
                                //right arrow  
                                if (xPosition + 1 < length) {
                                    xPosition++;
                                    areaIncrementShift();
                                }
                            }
                            function areaIncrementShift() {
                                var thisGid = 0;
                                for (var i = 0; i < scope.data[nPos].length; i++) {
                                    if (scope.data[nPos][i].x_order === xPosition) {
                                        thisGid = scope.data[nPos][i].gid;
                                        MappingStateService.getState().gid = thisGid;
                                        break;
                                    }
                                }
                                $rootScope.$broadcast('syncMappingEvents', thisGid, false);
                            }
                        }
                        function keyUp() {
                            //only refresh map on key up
                            var code = d3.event.keyCode;
                            if (code === 37 | code === 39) {
                                var thisGid = MappingStateService.getState().gid;
                                $rootScope.$broadcast('syncMappingEvents', thisGid, true);
                            }
                        }
                    };
                }
            };
            return directiveDefinitionObject;
        });




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

                        //the drop reference line
                        var selected = MappingStateService.getState().selected;

                        var x = d3.scaleLinear()
                                .domain(d3.extent(scope.data, function (d) {
                                    return d[orderField];
                                }))
                                .range([0, xWidth]);

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