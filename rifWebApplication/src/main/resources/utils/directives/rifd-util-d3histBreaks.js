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
 * DIRECTIVE for D3 chart showing histogram and choropleth break points
 *
 * THIS CODE IS NOT IN USE!
 */
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
                        if (!angular.isUndefined(scope.data) && !angular.isUndefined(scope.breaks)) {
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

