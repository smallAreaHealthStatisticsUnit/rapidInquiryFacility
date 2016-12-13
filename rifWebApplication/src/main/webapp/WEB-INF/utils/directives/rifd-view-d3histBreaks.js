/* global d3 */

angular.module("RIF")
        .directive('histImgBreaks', function () {
            var directiveDefinitionObject = {
                restrict: 'E',
                replace: false,
                scope: {
                    data: '=chartData',
                    breaks: '=chartBreaks'
                },
                link: function (scope, element, attrs) {

                    scope.$watch(function () {

                        d3.select("#domainHistogram").remove();

                        var border = 0.5;
                        var nbins = 100;
                        var bordercolor = 'gray';

                        var margin = {top: 10, right: 30, bottom: 20, left: 30};


                        var width = 800 - margin.left - margin.right;
                        var height = 175 - margin.top - margin.bottom;

                        //Some numbers in the database appear to be stored as strings
                        for (var i = 0; i < scope.data.length; i++) {
                            scope.data[i] = Number(scope.data[i]);
                        }

                        //Scales
                        var max = d3.max(d3.values(scope.data));
                        var min = d3.min(d3.values(scope.data));

                        var x = d3.scaleLinear()
                                .domain([min, max])
                                .range([0, width]);

                        var bins = d3.histogram()
                                .domain(x.domain())
                                .thresholds(x.ticks(nbins))
                                (scope.data);

                        var y = d3.scaleLinear()
                                .domain([0, d3.max(bins, function (d) {
                                        return d.length;
                                    })])
                                .range([height, 0]);

                        var xAxis = d3.axisBottom()
                                .scale(x);

                        //canvas
                        var svg = d3.select(element[0]).append("svg")
                                .attr("id", "domainHistogram")
                                .attr("width", width + margin.left + margin.right)
                                .attr("height", height + margin.top + margin.bottom)
                                .attr("border", border)
                                .append("g")
                                .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

                        //border box
                        var borderPath = svg.append("rect")
                                .attr("x", 0)
                                .attr("y", 0)
                                .attr("height", height)
                                .attr("width", width)
                                .style("stroke", bordercolor)
                                .style("fill", "none")
                                .style("stroke-width", border);

                        //bins
                        var bar = svg.selectAll(".bar")
                                .data(bins)
                                .enter().append("g")
                                .attr("class", "bar")
                                .attr("transform", function (d) {
                                    return "translate(" + x(d.x0) + "," + y(d.length) + ")";
                                });

                        bar.append("rect")
                                .attr("x", 0)
                                .attr("width", function (d) {
                                    return x(d.x1) - x(d.x0);
                                })
                                .attr("height", function (d) {
                                    return height - y(d.length);
                                });

                        //vertical reference lines
                        if (!angular.isUndefined(scope.data)) {
                            var breakRefs = svg.selectAll('.breakRefs')
                                    .data(scope.breaks) 
                                    .enter().append("line")
                                    .attr("stroke", "#d472bc")
                                    .attr("stroke-width", 2)
                                    .attr('x1', function (d) {
                                        return x(d);
                                    })
                                    .attr('x2', function (d) {
                                        return x(d);
                                    })
                                    .attr('y1', 0)
                                    .attr('y2', height);
                        }

                        svg.append("g")
                                .attr("class", "x axis")
                                .attr("transform", "translate(0," + height + ")")
                                .call(xAxis);

                        svg.select(".x.axis")
                                .selectAll(".text")
                                .style("fill", "#999999");
                    });
                }
            };
            return directiveDefinitionObject;
        });

