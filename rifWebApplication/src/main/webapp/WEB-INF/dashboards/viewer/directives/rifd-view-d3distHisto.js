/* global d3 */

//http://bl.ocks.org/mbostock/34f08d5e11952a80609169b7917d4172 //brushing

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
                        /*
                         var formatCount = d3.format(".0f"),
                         myFormatter = function (d) {
                         return (d / 1e6 >= 1) ? (d / 1e6 + "M") :
                         (d / 1e3 >= 1) ? (d / 1e3 + "K") : d;
                         };
                         */
                        var nbins = Math.floor(width / 20);
                        var max = d3.max(scope.data);
                        var min = d3.min(scope.data);

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

                        if (width > 0 & !angular.isUndefined(bins[ 0 ])) {

                            bar.append("rect")
                                    .attr("x", 0)
                                    .attr("width", function (d) {
                                        return x(d.x1) - x(d.x0);
                                    })
                                    .attr("height", function (d) {
                                        return height - y(d.length);
                                    });
                            /*
                             bar.append("text")
                             .attr("dy", ".75em")
                             .attr("y", 6)
                             .attr("x", (x(bins[0].x1) - x(bins[0].x0)) / 2)
                             .attr("text-anchor", "middle")
                             .text(function (d) {
                             return formatCount(d.length);
                             });
                             */
                            svg.append("g")
                                    .attr("class", "axis axis--x")
                                    .attr("transform", "translate(0," + height + ")")
                                    .call(d3.axisBottom(x));
                        }
                    };
                }
            };
            return directiveDefinitionObject;
        });