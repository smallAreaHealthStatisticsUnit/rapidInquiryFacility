

/* global d3 */

angular.module("RIF")
        .factory('D3RR',
                function () {
                    return {
                        getPlot: function (width, height, data, element) {

                            var margin = 90;

                            width = width - margin;
                            height = height - margin;















                            //https://bl.ocks.org/mbostock/3884914
                            /*
                             var parseDate = d3.time.format("%Y%m%d").parse;
                             var x = d3.time.scale()
                             .range([0, width]);
                             var y = d3.scale.linear()
                             .range([height, 0]);
                             var xAxis = d3.svg.axis()
                             .scale(x)
                             .orient("bottom");
                             var yAxis = d3.svg.axis()
                             .scale(y)
                             .orient("left");
                             var area = d3.svg.area()
                             .x(function (d) {
                             return x(d.date);
                             })
                             .y0(function (d) {
                             return y(d.low);
                             })
                             .y1(function (d) {
                             return y(d.high);
                             });
                             
                             d3.select("#rrchart").remove();
                             
                             var svg = d3.select(element)
                             .classed("svg-container", true) //container class to make it responsive
                             .append("svg")
                             .attr("id", "rrchart")
                             .attr("viewBox", "0 0 " + width - margin + " " + height - margin)
                             .classed("svg-content-responsive", true)
                             .attr("width", width + margin)
                             .attr("height", height + margin)
                             .attr("preserveAspectRatio", "none")
                             .append("g")
                             .attr("transform", "translate(" + margin / 2 + "," + margin / 2 + ")");
                             
                             d3.tsv("test/data.tsv", function (error, data) {
                             if (error)
                             throw error;
                             data.forEach(function (d) {
                             d.date = parseDate(d.date);
                             d.low = +d.low;
                             d.high = +d.high;
                             });
                             x.domain(d3.extent(data, function (d) {
                             return d.date;
                             }));
                             y.domain([d3.min(data, function (d) {
                             return d.low;
                             }), d3.max(data, function (d) {
                             return d.high;
                             })]);
                             svg.append("path")
                             .datum(data)
                             .attr("class", "area")
                             .attr("d", area);
                             svg.append("g")
                             .attr("class", "x axis")
                             .attr("transform", "translate(0," + height + ")")
                             .call(xAxis);
                             svg.append("g")
                             .attr("class", "y axis")
                             .call(yAxis)
                             .append("text")
                             .attr("transform", "rotate(-90)")
                             .attr("y", 6)
                             .attr("dy", ".71em")
                             .style("text-anchor", "end")
                             .text("Temperature (ºF)");
                             });
                             */
                        }
                    };
                });
