/* global d3 */

angular.module("RIF")
        .directive('pyramid', function () { //pyramid
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
                        if (angular.isUndefined(scope.data) || scope.data.length === 0) {
                            d3.select("#poppyramid").remove();
                            return;
                        } else {
                            scope.renderBase();
                        }
                    });

                    scope.renderBase = function () {

                        /* General dimensions of canvas */
                        var margins = {top: 60, right: 60, bottom: 80, left: 80};
                        var chartWidth = scope.width - margins.left - margins.right;
                        var chartHeight = scope.height - margins.top - margins.bottom;

                        var numberOfXAxisTicks = 10;

                        var xScale = d3.scaleLinear()
                                .range([0, chartWidth]);
                        var yScale = d3.scaleBand()
                                .rangeRound([chartHeight, 0], .6);

                        d3.select("#poppyramid").remove();

                        /* Create the main display area */
                        var mainImageArea = d3.select(element[0]).append("svg")
                                .attr("width", chartWidth + margins.left + margins.right)
                                .attr("height", chartHeight + margins.top + margins.bottom)
                                .attr("id", "poppyramid")
                                .append("g")
                                .attr("transform", "translate(" + margins.left + "," + margins.top + ")");

                        var maximumPopulation = d3.max(scope.data, function (d) {
                            return(d.males + d.females);
                        });

                        xScale.domain([0, maximumPopulation + 10]);
                        if (angular.isArray(scope.data)) {
                            yScale.domain(scope.data.map(function (d) {
                                return d.population_label;
                            }));
                        }

                        var xTickPositions = xScale.ticks(numberOfXAxisTicks);

                        mainImageArea.selectAll("g.maleBar")
                                .data(scope.data)
                                .enter().append("g")
                                .append("rect")
                                .attr("class", "maleBar")
                                .attr("x", 0)
                                .attr("y", function (d) {
                                    return(yScale(d.population_label));
                                })
                                .attr("width", function (d, i) {
                                    return(xScale(d.males));
                                })
                                .attr("height", yScale.bandwidth());

                        mainImageArea.selectAll("g.femaleBar")
                                .data(scope.data)
                                .enter().append("g")
                                .append("rect")
                                .attr("class", "femaleBar")
                                .attr("x", function (d) {
                                    return(xScale(d.males));
                                })
                                .attr("y", function (d) {
                                    return(yScale(d.population_label));
                                })
                                .attr("width", function (d, i) {
                                    return(xScale(d.females));
                                })
                                .attr("height", yScale.bandwidth());

                        mainImageArea.selectAll("g.totalPopulationBar")
                                .data(scope.data)
                                .enter().append("g")
                                .append("rect")
                                .attr("class", "totalPopulationBar")
                                .attr("x", function (d) {
                                    return(0);
                                })
                                .attr("y", function (d) {
                                    return(yScale(d.population_label));
                                })
                                .attr("width", function (d, i) {
                                    return(xScale(d.males + d.females));
                                })
                                .attr("height", yScale.bandwidth());


                        // Add the X Axis
                        var myFormatter = function (d) {
                            return (d / 1e6 >= 1) ? (d / 1e6 + "M") :
                                    (d / 1e3 >= 1) ? (d / 1e3 + "K") : d;
                        };

                        var xAxis = d3.axisBottom()
                                .scale(xScale)
                                .tickFormat(function (d) {
                                    return myFormatter(+d);
                                });

                        mainImageArea.append("g")
                                .attr("transform", "translate(0," + chartHeight + ")")
                                .call(xAxis);

                        // Add the Y Axis
                        var yAxis = d3.axisLeft()
                                .scale(yScale);

                        mainImageArea.append("g")
                                .call(yAxis);

                        // Add drop lines
                        mainImageArea.selectAll("g.xTickMarkProjectionLines")
                                .data(xTickPositions.slice(1, xTickPositions.length))
                                .enter().append("g")
                                .append("line")
                                .attr("class", "xAxisDashedLines")
                                .attr("x1", function (d) {
                                    return(xScale(d));
                                })
                                .attr("y1", function (d) {
                                    return(0);
                                })
                                .attr("x2", function (d, i) {
                                    return(xScale(d));
                                })
                                .attr("y2", function (d) {
                                    return(chartHeight);
                                });

                        //Add legend and axis labels
                        mainImageArea.append("rect")
                                .attr("width", 20)
                                .attr("height", 20)
                                .style("fill", "#c97f82")
                                .attr("transform", "translate(0,-30)");
                        mainImageArea.append("text")
                                .attr("transform", "translate(30,-15)")
                                .text("Male");
                        mainImageArea.append("rect")
                                .attr("width", 20)
                                .attr("height", 20)
                                .style("fill", "#7f82c9")
                                .attr("transform", "translate(80,-30)");
                        mainImageArea.append("text")
                                .attr("transform", "translate(110,-15)")
                                .text("Female");
                        mainImageArea.append("text")
                                .attr("transform", "translate(" + chartWidth / 2 + "," + (chartHeight + 35) + ")")
                                .style("text-anchor", "middle")
                                .text("TOTAL POPULATION");
                        mainImageArea.append("text")
                                .style("text-anchor", "middle")
                                .attr("transform", "rotate(-90)")
                                .attr("y", 15 - margins.top)
                                .attr("x", 0 - (chartHeight / 2))
                                .text("AGE GROUP");
                    };
                }
            };
            return directiveDefinitionObject;
        });