/* global d3 */

//http://bl.ocks.org/mbostock/3048450

angular.module("RIF")
        .directive('distHisto', function () { //dist-histo
            var directiveDefinitionObject = {
                restrict: 'E',
                replace: false,
                scope: {
                    name: '=name',
                    data: '=chartData',
                    width: '=width',
                    height: '=height'
                },
                link: function (scope, element, attrs) {

                    scope.$watch(function () {
                        if (angular.isUndefined(scope.data) || scope.data.length === 0) {
                            d3.select("#distHisto").remove();
                            return;
                        } else {
                            scope.renderBase();
                        }
                    });

                    scope.renderBase = function () {

                        if (scope.data.length === 0) {
                            return;
                        }

                        var margin = {top: 60, right: 60, bottom: 80, left: 80};
                        var width = scope.width - margin.left - margin.right;
                        var height = scope.height - margin.top - margin.bottom;

                        var nbins = Math.floor(width / 20);
                        var max = d3.max(scope.data);
                        var min = d3.min(scope.data);

                        //Some numbers in the database appear to be stored as strings
                        for (var i = 0; i < scope.data.length; i++) {
                            scope.data[i] = Number(scope.data[i]);
                        }

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

                        d3.select("#distHisto").remove();

                        var svg = d3.select(element[0])
                                .append("svg")
                                .attr("id", "distHisto")
                                .attr("width", width + margin.left + margin.right)
                                .attr("height", height + margin.top + margin.bottom)
                                .append("g")
                                .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

                        var bar = svg.selectAll(".bar")
                                .data(bins)
                                .enter().append("g")
                                .attr("class", "bar")
                                .attr("transform", function (d) {
                                    return "translate(" + x(d.x0) + "," + y(d.length) + ")";
                                });

                        if (height > 0 & width > 0 & !angular.isUndefined(bins[ 0 ])) {
                            bar.append("rect")
                                    .attr("x", 0)
                                    .attr("width", function (d) {
                                        return x(d.x1) - x(d.x0);
                                    })
                                    .attr("height", function (d) {
                                        return height - y(d.length);
                                    });

                            svg.append("g")
                                    .attr("class", "axis axis--x")
                                    .attr("transform", "translate(0," + height + ")")
                                    .call(d3.axisBottom(x));

                            svg.append("g")
                                    .attr("class", "axis axis--y")
                                    .call(d3.axisLeft(y));

                            svg.append("text")
                                    .attr("transform", "translate(" + width / 2 + "," + (height + 35) + ")")
                                    .style("text-anchor", "middle")
                                    .attr("class", "xlabel")
                                    .text(scope.name);
                            svg.append("text")
                                    .style("text-anchor", "middle")
                                    .attr("transform", "rotate(-90)")
                                    .attr("y", 15 - margin.top)
                                    .attr("x", 0 - (height / 2))
                                    .text("FREQUENCY");
                        }
                    };
                }
            };
            return directiveDefinitionObject;
        });