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
 * DIRECTIVE for D3 distribution histogram
 */

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
                        if (angular.isUndefined(scope.width)) {
                            scope.width=150;
                        }
                        if (angular.isUndefined(scope.height)) {
                            scope.height=150;
                        }
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

                        if (height > 0 & width > 0 & !angular.isUndefined(bins[0])) {
                            bar.append("rect")
                                    .attr("x", 0)
                                    .attr("width", function (d) {
                                        return x(d.x1) - x(d.x0);
                                    })
                                    .attr("height", function (d) {
                                        return height - y(d.length);
                                    });

                            //y axis
                            var yAxis = d3.axisLeft()
                                    .scale(y);

                            function customYAxis(g) {
                                g.call(yAxis);
                                g.selectAll(".tick text").style("font-size", function (d) {
                                    return Math.min((height / 20), 15);
                                });
                            }

                            svg.append("g")
                                    .call(customYAxis);

                            //x axis
                            var xAxis = d3.axisBottom()
                                    .scale(x);

                            function customXAxis(g) {
                                g.call(xAxis);
                                g.attr("transform", "translate(0," + height + ")");
                                g.selectAll(".tick text").style("font-size", function (d) {
                                    return Math.min((width / 20), 10);
                                });
                            }

                            svg.append("g")
                                    .call(customXAxis);

                            svg.append("text")
                                    .style("font-size", function (d) {
                                        return Math.min((width / 15), 15);
                                    })
                                    .attr("transform", "translate(" + width / 2 + "," + (height + 35) + ")")
                                    .style("text-anchor", "middle")
                                    .attr("class", "xlabel")
                                    .text(scope.name);
                            svg.append("text")
                                    .style("font-size", function (d) {
                                        return Math.min((height / 15), 15);
                                    })
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