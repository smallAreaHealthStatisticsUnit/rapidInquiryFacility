/* 
 * D3 Multiple Area Plots for disease mapping
 */
//Brush handles
//https://bl.ocks.org/mbostock/4349545
//http://stackoverflow.com/questions/25872777/d3-hover-chart-thousands-of-x-entries-mouse-sensitivity 

/* global d3 */

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

                        //brushing
                        var brushIsOn = false;

                        //area plot click handling
                        var areaSetIndex = MappingStateService.getState().selectedSet;
                        var ln = MappingStateService.getState().selected;
                        var areaClickXPos = 0;
                        if (ln !== null) {
                            areaClickXPos = MappingStateService.getState().selected.x_order;
                        }

                        //Define the scales and domains for each chart
                        var x = d3.scale.linear().range([0, xWidth]); //x is the same for all
                        var y = []; //y varies by result set
                        for (var i = 0; i < plotsCount; i++) {
                            y.push(d3.scale.linear().range([(xHeight * (i + 1)) - hGridPadding, (xHeight * (i))]));
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
                        d3.select(".brushButton").remove();

                        d3.select(element[0]).append("a")
                                .attr("class", "brushButton")
                                .text("zoom")
                                .attr("transform", "translate(" + margin.right + "," + margin.top + ")")
                                .on("click", function () {
                                    brushIsOn = !brushIsOn;
                                    (brushIsOn) ? showBrush(true, false) : showBrush(false, true);
                                });

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
                            svg.selectAll('rect').each(function (i) {
                                var yAxis = d3.svg.axis()
                                        .scale(y[i])
                                        .orient("left");

                                var area = d3.svg.area()
                                        .interpolate("basis")
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
                                            var nPos = chartList.indexOf(areaSetIndex);
                                            var thisGid = scope.data[nPos][areaClickXPos].gid;
                                            $rootScope.$broadcast('xxxx', thisGid);
                                        });

                                var currentFigures = svg.append("text")
                                        .attr("transform", "translate(50," + (25 + (xHeight * i)) + ")")
                                        .attr("id", "currentFiguresLineBivariate")
                                        .text("ResultSet " + i);
                            });

                        }

                        scope.$on('areaDropLineRedraw', function (event, data) {
                            updateLines2(data); //data is the GID
                        });

                        //draw the droplines
                        function updateLines2(gid) {
                            //remove existing lines
                            d3.selectAll(".dropLine").remove();
                            d3.selectAll(".areaValue").remove();

                            svg.selectAll('rect').each(function (i) {
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

                        //add dropLines on container resize
                        if (MappingStateService.getState().selected !== null) {
                            var ln = MappingStateService.getState().selected;
                            var st = MappingStateService.getState().selectedSet;
                            //              updateLines(ln.x_order, st);
                        }

                        //ensure that the click is within range
                        var snapToBounds = function (mouseX) {
                            var val = Math.round(x.invert(mouseX));
                            return (val < 0) ? 0 : (val > length) ? length - 1 : val;
                        };

                        //keydown event for increments
                        d3.select("body").on("keydown", keyDown);
                        function keyDown() {
                            var code = d3.event.keyCode;
                            if (code === 37) {
                                //left arrow
                                if (areaClickXPos - 1 > 0) {
                                    areaClickXPos--;
                                    areaIncrementShift();
                                }
                            } else if (code === 39) {
                                //right arrow
                                if (areaClickXPos + 1 < length) {
                                    areaClickXPos++;
                                    areaIncrementShift();
                                }
                            } else if (code === 13) { //enter
                                console.log("enter");
                            }
                        }
                        function areaIncrementShift() {
                            var nPos = chartList.indexOf(areaSetIndex);
                            var thisGid = scope.data[nPos][areaClickXPos].gid;
                            $rootScope.$broadcast('xxxx', thisGid);
                        }
                    };
                }
            };
            return directiveDefinitionObject;
        });