/* 
 * 
 */
//Brush handles
//https://bl.ocks.org/mbostock/4349545
//http://stackoverflow.com/questions/25872777/d3-hover-chart-thousands-of-x-entries-mouse-sensitivity 

/* global d3 */
angular.module("RIF")
        .factory('D3marea',
                function () {
                    return {
                        getPlot: function (width, height, data, element, opt) {

                            var hGridPadding = 30;
                            var margin = {top: 5, right: 10, bottom: 0, left: 25};
                            height = height - margin.top - margin.bottom;
                            var xWidth = width - margin.left - margin.right;

                            var idField = opt.id_field;
                            var orderField = opt.x_field;
                            var lineField = opt.risk_field;

                            var plotsCount = opt.rSet;
                            plotsCount = 4;
                            var n = [0, 1, 2, 3];

                            d3.select("#areaCharts").remove();
                            //   d3.select(".brushButton").remove();
                            /*
                             var brushIsOn = false;
                             d3.select(element).append("a")
                             .attr("class", "brushButton")
                             .text("zoom")
                             .on("click", function () {
                             brushIsOn = !brushIsOn;
                             (brushIsOn) ? showBrush(true, false) : showBrush(false, true);
                             });
                             */

                            var xheight = height / plotsCount;

                            //Define the canvas for all graphs
                            var svg = d3.select(element).insert("svg", "div")
                                    .attr("width", width)
                                    .attr("height", height + margin.bottom + margin.top)
                                    .attr("id", "areaCharts");

                            var x = d3.scale.linear()
                                    .range([0, xWidth]);
                                 var y = d3.scale.linear()
                                        .range([(xheight * (0 + 1)) - hGridPadding, (xheight * (0))]); //i = 0

                  /*          var y = d3.scale.linear()
                                    .range(
                                            function (d) {
                                                return[(xheight * (0 + 1)) - hGridPadding, (xheight * (0))];
                                            }); //i = 0*/


                            var area = d3.svg.area()
                                    .interpolate("basis")
                                    .x(function (d) {
                                        return x(d[orderField]);
                                    })
                                    .y0(function (d, i) {
                                        if (d[lineField] < 1) {
                                            return y(d[lineField]);
                                        } else {
                                            return y(1);
                                        }

                                    })
                                    .y1(function (d, i) {
                                        if (d[lineField] < 1) {
                                            return y(1);
                                        } else {
                                            return y(d[lineField]);
                                        }
                                    });

                            var rectangle = svg.selectAll("rect")
                                    .data(n).enter()
                                    .append("rect")
                                    .attr("id", function (d) {
                                        return "rect" + (d + 1);
                                    })
                                    .attr("x", 0)
                                    .attr("y", function (d) {
                                        return xheight * d;
                                    })
                                    .attr("width", width - margin.right - margin.left)
                                    .attr("height", xheight - hGridPadding)
                                    .attr("transform", "translate(" + margin.left + "," + margin.top + ")")
                                    .style('opacity', 0.5)
                                    .style('fill', '#ededed');

                            var focus = svg.append("g")
                                    .attr("class", "focus")
                                    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

                            var lookUp = {};
                            var lookUpOrder = {};

                            data.forEach(function (d) {
                                d[idField] = +d[idField];
                                d[orderField] = +d[orderField];
                                d[lineField] = +d[lineField];
                                lookUp[d[idField]] = [d[orderField], d[lineField]];
                                lookUpOrder[d[orderField]] = d[idField];
                            });

                            var xDomain = d3.extent(data, function (d) {
                                return d[orderField];
                            });
                            x.domain(xDomain);

                            var yDomain = d3.extent(data, function (d) {
                                return d[lineField];
                            });
                            y.domain(yDomain);

                            focus.append("path")
                                    .datum(data)
                                    .attr("class", "areaChart" + (0 + 1))
                                    .attr("d", area);


             focus.append("path")
                                        .datum(data[i])
                                        .attr("class", "areaChart" + (i + 1))
                                        .attr("id", "areaChart" + (i + 1))
                                        .attr("d", area)
                                        .on("click", function (d) {
                                            var xy = d3.mouse(this);
                                            var el = d3.select(this).attr("id");
                                            updateLines(xy, el);
                                        });

                                var currentFigures = svg.append("text")
                                        .attr("transform", "translate(40," + (25 + (xHeight * i)) + ")")
                                        .attr("id", "currentFiguresLineBivariate")
                                        .text("ResultSet " + i);
                            });

                            function updateLines(xy, set) {
                                var xPos = snapToBounds(xy[0]);
                                var nPos = chartList.indexOf(set);
                                var gidClicked = data[nPos][xPos].gid;

                                //console.log(gidClicked);
                                d3.selectAll(".dropLine").remove();

                                svg.selectAll('rect').each(function (i) {




                            /*                
                             
                             //For each reault set, maximum possible is 4
                             for (var i = 0; i < plotsCount; i++) {
                             var line = d3.svg.line()
                             .interpolate("basis")
                             .x(function (d) {
                             return x(d[orderField]);
                             })
                             .y(function (d) {
                             return y(d[lineField]);
                             });
                             
                             var x = d3.scale.linear()
                             .range([0, xWidth]);
                             var y = d3.scale.linear()
                             .range([(xheight * (i + 1)) - hGridPadding, (xheight * (i))]);
                             
                             var yAxis = d3.svg.axis()
                             .scale(y)
                             .orient("left");
                             
                             var area = d3.svg.area()
                             .interpolate("basis")
                             .x(function (d) {
                             return x(d[orderField]);
                             })
                             .y0(function (d) {
                             if (d[lineField] < 1) {
                             return y(d[lineField]);
                             } else {
                             return y(1);
                             }
                             
                             })
                             .y1(function (d) {
                             if (d[lineField] < 1) {
                             return y(1);
                             } else {
                             return y(d[lineField]);
                             }
                             });
                             
                             
                             var rectangle = svg.append("rect")
                             .attr("id", "rect" + (i + 1))
                             .attr("x", 0)
                             .attr("y", xheight * i)
                             .attr("width", width - margin.right - margin.left)
                             .attr("height", xheight - hGridPadding)
                             .attr("transform", "translate(" + margin.left + "," + margin.top + ")")
                             .style('opacity', 0.5)
                             .style('fill', '#ededed');
                             
                             var focus = svg.append("g")
                             .attr("class", "focus")
                             .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
                             
                             var lookUp = {};
                             var lookUpOrder = {};
                             
                             data.forEach(function (d) {
                             d[idField] = +d[idField];
                             d[orderField] = +d[orderField];
                             d[lineField] = +d[lineField];
                             lookUp[d[idField]] = [d[orderField], d[lineField]];
                             lookUpOrder[d[orderField]] = d[idField];
                             });
                             
                             var xDomain = d3.extent(data, function (d) {
                             return d[orderField];
                             });
                             x.domain(xDomain);
                             
                             var yDomain = d3.extent(data, function (d) {
                             return d[lineField];
                             });
                             y.domain(yDomain);
                             
                             focus.append("path")
                             .datum(data)
                             .attr("class", "areaChart" + (i + 1))
                             .attr("d", area);
                             
                             focus.append("g")
                             .attr("class", "y axis")
                             .call(yAxis);
                             
                             var labelShift = 25 + (xheight * i);
                             
                             var currentFigures = svg.append("text")
                             .attr("transform", "translate(40," + labelShift + ")")
                             .attr("id", "currentFiguresLineBivariate")
                             .text("ResultSet " + i);
                             
                             }*/
                        }
                    };
                });



