/* global d3 */

//http://bl.ocks.org/mbostock/3048450

angular.module("RIF")
        .directive('distHisto', function (ViewerStateService) { //dist-histo
            var directiveDefinitionObject = {
                restrict: 'E',
                replace: false,
                scope: {
                    data: '=chartData',
                    width: '=width',
                    height: '=height'
                },
                link: function (scope, element, attrs) {

                    scope.$watch(function () {
                        scope.renderBase();
                    });

                    scope.renderBase = function () {
                        var margin = {top: 30, right: 40, bottom: 30, left: 10};
                        var width = scope.width - margin.left - margin.right;
                        var height = scope.height - margin.top - margin.bottom;

                        var formatCount = d3.format(".0f"),
                                myFormatter = function (d) {
                                    return (d / 1e6 >= 1) ? (d / 1e6 + "M") :
                                            (d / 1e3 >= 1) ? (d / 1e3 + "K") : d;
                                };

                        var nbins = Math.floor(width / 30);
                        var max = d3.max(scope.data);

                        var x = d3.scale.linear()
                                .domain([0, max])
                                .range([0, width]);

                        var bins = d3.layout.histogram()
                                .bins(x.ticks(nbins))
                                (scope.data);

                        var y = d3.scale.linear()
                                .domain([0, d3.max(bins, function (d) {
                                        return d.y;
                                    })])
                                .range([height, 0]);

                        var xAxis = d3.svg.axis()
                                .scale(x)
                                .orient("bottom")
                                .ticks(bins.length)
                                .tickFormat(function (d) {
                                    return myFormatter(d);
                                });

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
                                    return "translate(" + x(d.x) + "," + y(d.y) + ")";
                                });

                        if (!angular.isUndefined(bins[ 0 ])) {
                            bar.append("rect")
                                    .attr("x", 1)
                                    .attr("width", x(bins[ 0 ].dx) - 1)
                                    .attr("height", function (d) {
                                        return height - y(d.y);
                                    });

                            bar.append("text")
                                    .attr("dy", ".70em")
                                    .attr("y", -10)
                                    .attr("x", x(bins[ 0 ].dx) / 2)
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
                }
            };
            return directiveDefinitionObject;
        });