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
                        if (MappingStateService.getState().cleanState) {
                            scope.renderBase();
                        }
                        if (MappingStateService.getState().areaContainerV === scope.width) {
                            return;
                        } else {
                            MappingStateService.getState().areaContainerV = scope.width;
                            scope.renderBase();
                        }
                    });

                    function replaceBrush() {
                        scope.renderBase();
                        scope.renderBase();
                    }

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
                        var margin = {top: 50, right: 20, bottom: 0, left: 45};
                        var scopeHeight = scope.height - margin.top - margin.bottom;
                        var xHeight = (scope.height / plotsCount) - hGridPadding;
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

                        //store user defined domain
                        if (MappingStateService.getState().brushStartLoc === null && MappingStateService.getState().brushEndLoc === null) {
                            MappingStateService.getState().brushStartLoc = x.domain()[0];
                            MappingStateService.getState().brushEndLoc = x.domain()[1];
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
                                    .attr("class", "bowTieRect")
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
                                        .attr("transform", "translate(50," + (70 + (xHeight * i)) + ")")
                                        .attr("id", "currentFiguresLineBivariate")
                                        .text("ResultSet " + i);

                                //Zoom-brush box on 1st graph
                                if (i === 0) {
                                    var brush = d3.brushX()
                                            .extent([[0, 0], [xWidth, hGridPadding / 2]])
                                            .on("brush", brushmoved);

                                    var brushBackground = focus.append("rect")
                                            .attr("transform", "translate(0, -15)")
                                            .attr("id", "brushBackground")
                                            .attr("width", xWidth)
                                            .attr("height", 15)
                                            .attr("fill", "#ccdcea")
                                            .on("click", replaceBrush);

                                    var gBrush = focus.append("g")
                                            .attr("transform", "translate(0, -15)")
                                            .attr("class", "brush")
                                            .call(brush);

                                    var brushStartLoc = MappingStateService.getState().brushStartLoc;
                                    var brushEndLoc = MappingStateService.getState().brushEndLoc;

                                    gBrush.call(brush.move, [brushStartLoc, brushEndLoc].map(x));
                                }

                                function brushmoved() {
                                    //broadcast extent xmin-xmax
                                    var s = d3.event.selection || x.range();
                                    var sx = s.map(x.invert, x);
                                    //store selected
                                    MappingStateService.getState().brushStartLoc = sx[0];
                                    MappingStateService.getState().brushEndLoc = sx[1];
                                    //sync rrChart
                                    $rootScope.$broadcast('brushXaxisChange', sx);
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

                            svg.selectAll('.bowTieRect').each(function (i) {
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