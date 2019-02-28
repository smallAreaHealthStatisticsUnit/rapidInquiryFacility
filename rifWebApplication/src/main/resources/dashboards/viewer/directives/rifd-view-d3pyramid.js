/**
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * Copyright 2016 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
 *
 * This file is part of the Rapid Inquiry Facility (RIF) project.
 * RIF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RIF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
 * to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
 * Boston, MA 02110-1301 USA
 
 * David Morley
 * @author dmorley
 */

/*
 * DIRECTIVE for D3 population pyramid
 */

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

                        if (angular.isUndefined(scope.width)) {
                            scope.width=150;
                        }
                        if (angular.isUndefined(scope.height)) {
                            scope.height=150;
                        }
                        
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

                        xScale.domain([0, maximumPopulation]);
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
                                .ticks(5)
                                .tickFormat(function (d) {
                                    return myFormatter(+d);
                                });

                        function customXAxis(g) {
                            g.call(xAxis);
                            g.selectAll(".tick text").style("font-size", function (d) {
                                return Math.min((chartWidth / 20), 10);
                            });
                        }

                        mainImageArea.append("g")
                                .attr("transform", "translate(0," + chartHeight + ")")
                                .call(customXAxis);

                        // Add the Y Axis
                        var yAxis = d3.axisLeft()
                                .scale(yScale);

                        function customYAxis(g) {
                            g.call(yAxis);
                            g.selectAll(".tick text").style("font-size", function (d) {
                                return Math.min((chartHeight / 25), 10);
                            });
                        }

                        mainImageArea.append("g")
                                .call(customYAxis);

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
                                .attr("width", yScale.bandwidth())
                                .attr("height", yScale.bandwidth())
                                .style("fill", "#c97f82")
                                .attr("transform", "translate(0, " + (-yScale.bandwidth() - 10) + ")");
                        mainImageArea.append("text")
                                .style("font-size", function (d) {
                                    return Math.min((chartWidth / 15), 10, (chartHeight / 15));
                                })
                                .attr("transform", "translate(" + (yScale.bandwidth() + 2) + "," + ((-yScale.bandwidth() / 2) - 10) + ")")
                                .style("text-anchor", "start")
                                .text("Male");
                        mainImageArea.append("rect")
                                .attr("width", yScale.bandwidth())
                                .attr("height", yScale.bandwidth())
                                .style("fill", "#7f82c9")
                                .attr("transform", "translate(80, " + (-yScale.bandwidth() - 10) + ")");
                        mainImageArea.append("text")
                                .style("font-size", function (d) {
                                    return Math.min((chartWidth / 15), 10, (chartHeight / 15));
                                })
                                .attr("transform", "translate(" + (yScale.bandwidth() + 82) + "," + ((-yScale.bandwidth() / 2) - 10) + ")")
                                .style("text-anchor", "start")
                                .text("Female");
                        mainImageArea.append("text")
                                .style("font-size", function (d) {
                                    return Math.min((chartWidth / 15), 15);
                                })
                                .attr("transform", "translate(" + chartWidth / 2 + "," + (chartHeight + 35) + ")")
                                .style("text-anchor", "middle")
                                .text("TOTAL POPULATION");
                        mainImageArea.append("text")
                                .style("font-size", function (d) {
                                    return Math.min((chartHeight / 15), 15);
                                })
                                .attr("text-anchor", "middle")
                                .attr("transform", "rotate(-90)")
                                .attr("y", 15 - margins.top)
                                .attr("x", 0 - (chartHeight / 2))
                                .text("AGE GROUP");
                    };
                }
            };
            return directiveDefinitionObject;
        });