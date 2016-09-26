/* 
 * 
 */
/* global d3 */

angular.module("RIF")
        .factory('D3DistHisto',
                function () {
                    return {
                        getPlot: function (width, height, data, element) {
                            //http://bl.ocks.org/mbostock/3048450

                            var margin = {top: 30, right: 40, bottom: 30, left: 10};
                            width = width - margin.left - margin.right;
                            height = height - margin.top - margin.bottom;

                            var bins = Math.floor(width / 30);

                            data = data.map(function (d) {
                                if (+d >= 0) {
                                    return +d;
                                }
                            });

                            var max = d3.max(data);
                            var min = d3.min(data);

                            max = (max > 0) ? max : bins;
                            min = (min > 0) ? min : 0;
                            bins = (max > bins) ? bins : 15;

                            // A formatter for counts.
                            var formatCount = d3.format(".0f"),
                                    myFormatter = function (d) {
                                        return (d / 1e6 >= 1) ? (d / 1e6 + "M") :
                                                (d / 1e3 >= 1) ? (d / 1e3 + "K") : d;
                                    };

                            var x = d3.scale.linear()
                                    .domain([0, max])
                                    .range([0, width]);

                            var data = d3.layout.histogram()
                                    .bins(x.ticks(bins))
                                    (data);

                            var y = d3.scale.linear()
                                    .domain([0, d3.max(data, function (d) {
                                            return d.y;
                                        })])
                                    .range([height, 0]);

                            var xAxis = d3.svg.axis()
                                    .scale(x)
                                    .orient("bottom")
                                    .ticks(data.length)
                                    .tickFormat(function (d) {
                                        return myFormatter(d);
                                    });

                            d3.select("#distHisto").remove();

                            var svg = d3.select(element)
                                    .classed("svg-container", true)
                                    .append("svg")
                                    .attr("id", "distHisto")
                                    .attr("viewBox", "0 0 " + width - margin.left - margin.right + " " + height - margin.top - margin.bottom)
                                    .classed("svg-content-responsive2", true)
                                    .attr("width", width + margin.left + margin.right)
                                    .attr("height", height + margin.top + margin.bottom)
                                    .attr("preserveAspectRatio", "none")
                                    .append("g")
                                    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

                            var bar = svg.selectAll(".bar")
                                    .data(data)
                                    .enter().append("g")
                                    .attr("class", "bar")
                                    .attr("transform", function (d) {
                                        return "translate(" + x(d.x) + "," + y(d.y) + ")";
                                    });

                            bar.append("rect")
                                    .attr("x", 1)
                                    .attr("width", x(data[ 0 ].dx) - 1)
                                    .attr("height", function (d) {
                                        return height - y(d.y);
                                    });

                            bar.append("text")
                                    .attr("dy", ".70em")
                                    .attr("y", -10)
                                    .attr("x", x(data[ 0 ].dx) / 2)
                                    .attr("text-anchor", "middle")
                                    .text(function (d) {
                                        return formatCount(d.y);
                                    });

                            svg.append("g")
                                    .attr("class", "x axis")
                                    .attr("transform", "translate(10," + height + ")")
                                    .call(xAxis);
                        }
                    };
                });