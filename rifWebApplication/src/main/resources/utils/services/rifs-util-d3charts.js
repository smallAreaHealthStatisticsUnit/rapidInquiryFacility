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

 * Peter Hambly
 * @author phambly
 */

/*
 * SERVICE for UI-Grid helper functions
 */
angular.module("RIF")
        .factory('D3ChartsService', ['AlertService', '$window',
                function (AlertService, $window) {
                    
                    var colors = {
                        males: '#c97f82',
                        females: '#7f82c9',
                        both: '#660033'
                    };                
                    
                    // Scale to 20% bigger than range
                    function getDomain(data, gendersArray, riskFactorFieldName) {
                        var domainExtent = {
                            riskFactorFieldName: riskFactorFieldName,
                            gendersArray: gendersArray,
                            xMin: 0,
                            xMax: 0,
                            yMin: 0,
                            yMax: 0
                        };
                        
                        for (var i=0; i<gendersArray.length; i++) {
                            var yMin = d3.min(data[gendersArray[i]], function(d) { return (d.lower95*0.8); }); 
                            if (!domainExtent.yMin) {
                                domainExtent.yMin=yMin;
                            }
                            else if (yMin < domainExtent.yMin) {
                                domainExtent.yMin=yMin;
                            }
                            
                            var yMax = d3.max(data[gendersArray[i]], function(d) { return (d.upper95*1.2); });            
                            if (!domainExtent.yMax) {
                                domainExtent.yMax=yMax;
                            }
                            else if (yMax > domainExtent.yMax) {
                                domainExtent.yMax=yMax;
                            }
                            
                            var xMin = d3.min(data[gendersArray[i]], function(d) { 
                                return d[riskFactorFieldName]*0.8; });                        
                            if (!domainExtent.xMin) {
                                domainExtent.xMin=xMin;
                            }
                            else if (xMin < domainExtent.xMin) {
                                domainExtent.xMin=xMin;
                            }
                            
                            var xMax = d3.max(data[gendersArray[i]], function(d) { 
                                return d[riskFactorFieldName]*1.2; });                            
                            if (!domainExtent.xMax) {
                                domainExtent.xMax=xMax;
                            }
                            else if (xMax > domainExtent.xMax) {
                                domainExtent.xMax=xMax;
                            }
                        } 
                        return domainExtent;
                    }
                    
                    function d3HomogeneityChart(data, gendersArray, riskFactorFieldName, riskFactorFieldDesc) {
                        
                        // Check parameters are defined
                        if (!data || !(gendersArray && gendersArray.length > 0) || !riskFactorFieldName || 
                            !riskFactorFieldDesc) {
                            AlertService.consoleError("[rifs-util-d3charts.js] null data in d3HomogeneityChart: "  + 
                                "; gendersArray: " + JSON.stringify(gendersArray) + 
                                "; riskFactor: " + riskFactorFieldName + 
                                "; riskFactorFieldDesc: " + riskFactorFieldDesc + 
                                "; data: " + JSON.stringify(data));
                            AlertService.showError("Null data passed to d3HomogeneityChart");
                            return;
                        }
                        
                        function gender2text(gender) {
                            var rval;
                            switch (gender) {
                                case 1: 
                                    rval="males";
                                    break;
                                case 2: 
                                    rval="females";
                                    break;
                                case 3: 
                                    rval="males and females";
                                    break;
                            }
                            return rval;
                        }          		
                        
                        function addErrorBar(gendersName, riskFactorFieldName) {
                            // Add Error Line
                            svg.append("g").selectAll("line")
                                .data(data[gendersName]).enter()
                              .append("line")
                              .attr("class", "homogeneityChart-error-line")
                              .attr("x1", function(d) {
                                return xScale(d[riskFactorFieldName]);
                              })
                              .attr("y1", function(d) {
                                return yScale(d.upper95);
                              })
                              .attr("x2", function(d) {
                                return xScale(d[riskFactorFieldName]);
                              })
                              .attr("y2", function(d) {
                                return yScale(d.lower95);
                              }).
                              style("stroke", colors[gendersName]);

                            // Add Error Top Cap
                            svg.append("g").selectAll("line")
                                .data(data[gendersName]).enter()
                              .append("line")
                              .attr("class", "homogeneityChart-error-cap")
                              .attr("x1", function(d) {
                                return xScale(d[riskFactorFieldName]) - 4;
                              })
                              .attr("y1", function(d) {
                                return yScale(d.upper95);
                              })
                              .attr("x2", function(d) {
                                return xScale(d[riskFactorFieldName]) + 4;
                              })
                              .attr("y2", function(d) {
                                return yScale(d.upper95);
                              }).
                              style("stroke", colors[gendersName]);
                              
                             // Add Error Bottom Cap
                            svg.append("g").selectAll("line")
                                .data(data[gendersName]).enter()
                              .append("line")
                              .attr("class", "homogeneityChart-error-cap")
                              .attr("x1", function(d) {
                                return xScale(d[riskFactorFieldName]) - 4;
                              })
                              .attr("y1", function(d) {
                                return yScale(d.lower95);
                              })
                              .attr("x2", function(d) {
                                return xScale(d[riskFactorFieldName]) + 4;
                              })
                              .attr("y2", function(d) {
                                return yScale(d.lower95);
                              }).
                              style("stroke", colors[gendersName]); 
                              
                            // Add Scatter Points
                            svg.append("g").attr("class", "scatter")
                            .selectAll("circle")
                            .data(data[gendersName]).enter()
                            .append("circle")
                            .attr("cx", function(d) {
                              return xScale(d[riskFactorFieldName]);
                            })
                            .attr("cy", function(d) {
                              return yScale(d.relative_risk);
                            })
                            .attr("r", 4)
                            .style("background", colors[gendersName])
                            .on("mouseover", function(d, i, n) {
                                var transform=this.getScreenCTM().translate(+this.getAttribute("cx"),
                                    +this.getAttribute("cy")); /* Actual pixel position of circle */
                                var transform0=this.getScreenCTM().translate(0, 0); 
                                                               /* Actual pixel position of SVG graph div origin */

                                AlertService.consoleDebug("[rifs-util-d3charts.js] homogeneityChart mouseover: " + JSON.stringify(d, 0, 1));
                                return tooltip.html("Band " + d.band_id + " " +
                                            gender2text(d.genders) + "</br>" + 
                                            d.relative_risk.toFixed(3) + 
                                            "&nbsp;[95% CI&nbsp;" + d.lower95.toFixed(3) +
                                            "&ndash;" + d.upper95.toFixed(3) + "]") 
                                     .style("visibility", "visible")
                                     .style("left", (transform.e - transform0.e + 53) + "px") // Correct as position is relative
                                     .style("top", (transform.f - transform0.f + 25) + "px")
                                     .style("background", colors[gendersName]);
                             })
                            .on("mouseout", function() {
                                return tooltip.style("visibility", "hidden");
                             });  
                        } 
                        
                        /* Questions: 
                         * 1. Do we need to use band_id if max_exposure_value is undefined/null YES
                         * 2. Would average exposure value be better? YES
                         * 3. Do we want a choice: max/min/average/median/band_id/distance from nearest source
                         */
                        var svg=d3.select("#homogeneityChart").select("svg");
                         
/*                              var tooltip = d3.select("#homogeneityTooltip");
                                if (tooltip._group && tooltip._group[0] && tooltip._group[0][0] != null) { 
                                }
                                else {                                      
                                    tooltip = d3.select("#homogeneityTooltip").append("div")
                                        .attr("id", "homogeneityTooltip")
                                        .attr("class", "homogeneityChart-tooltip")
                                        .style("visibility", "hidden");
                                } */
                                
                        var tooltip = d3.select("#homogeneityTooltip").append("div")
                            .attr("class", "homogeneityChart-tooltip")
                            .style("visibility", "hidden");
                        
                        /*
                        marginHeightWidth: {
 "margin": {
  "top": 20,
  "right": 20,
  "bottom": 40,
  "left": 40
 },
 "width": 900,
 "height": 350,
 "innerWidth": 1920,
 "innerHeight": 938
}
                         */
                        function getMargin() {
                            var marginHeightWidth = {
                                margin: {top: 20, right: 20, bottom: 40, left: 40}, // Scale using CSS
                                width: 0,
                                height: 0,
                                innerWidth: $window.innerWidth,
                                innerHeight: $window.innerHeight
                            };
                            // 1346x416 
                            var width = 960;
                            var height = 410;
                            if (marginHeightWidth.innerWidth) {
                                width=marginHeightWidth.innerWidth*0.65;
                            }
                            if (marginHeightWidth.innerHeight) {
                                height=marginHeightWidth.innerHeight*0.55;
                            }
                            marginHeightWidth.width=width - marginHeightWidth.margin.left - marginHeightWidth.margin.right;
                            marginHeightWidth.height=height - marginHeightWidth.margin.top - marginHeightWidth.margin.bottom;
                            
                            return marginHeightWidth;
                        }
                        
                        var domainExtent=getDomain(data, gendersArray, riskFactorFieldName);
                        var marginHeightWidth = getMargin();
                        AlertService.consoleDebug("[rifd-util-info.js] homogeneityChart domainExtent: " + 
                            JSON.stringify(domainExtent, null, 1) +
                            "; marginHeightWidth: " + JSON.stringify(marginHeightWidth, null, 1) );
                            
                        var xScale = d3.scaleLinear()
                            .range([0, marginHeightWidth.width])
                            .domain([domainExtent.xMin, domainExtent.xMax]).nice();
                        var yScale = d3.scaleLinear()
                           .range([marginHeightWidth.height, 0])
                           .domain([domainExtent.yMin, domainExtent.yMax]).nice();
                                       
                        
                        var xAxis = d3.axisBottom(xScale).ticks(12);
                        var yAxis = d3.axisLeft(yScale).ticks(12 * marginHeightWidth.height / marginHeightWidth.width);

                        let line = d3.line()
                            .x(function(d) {
                            return xScale(d.max_exposure_value);
                          })
                          .y(function(d) {
                            return yScale(d.relative_risk);
                          });

                        if (svg) {
                            d3.select("#homogeneityChart").select("svg").remove();
                        }
                        svg = d3.select("#homogeneityChart").append("svg")
                            .attr("width", 
                                marginHeightWidth.width + marginHeightWidth.margin.left + marginHeightWidth.margin.right)
                            .attr("height", 
                                marginHeightWidth.height + marginHeightWidth.margin.top + marginHeightWidth.margin.bottom)
                            .append("g")
                            .attr("transform", "translate(" + 
                                marginHeightWidth.margin.left + "," + marginHeightWidth.margin.top + ")");
                 
                        svg.append("g").append("rect")
                            .attr("width", marginHeightWidth.width)
                            .attr("height", marginHeightWidth.height)
                            .attr("class", "homogeneityChart-bg");

                        // Add Axis and labels
                        svg.append("g").attr("class", "axis axis--x")
                        .attr("transform", "translate(" + 0 + "," + marginHeightWidth.height + ")")
                        .call(xAxis);
                        // text label for the x axis
                        svg.append("text")             
                            .attr("transform",
                                  "translate(" + (marginHeightWidth.width/2) + " ," + 
                                               (marginHeightWidth.height + marginHeightWidth.margin.top + 10) + ")")
                            .style("text-anchor", "middle")
                            .text(riskFactorFieldDesc);

                        svg.append("g").attr("class", "axis axis--y").call(yAxis);                           
                        // text label for the y axis
                        svg.append("text")
                            .attr("transform", "rotate(-90)")
                            .attr("y", 0 - marginHeightWidth.margin.left)
                            .attr("x", 0 - (marginHeightWidth.height / 2))
                            .attr("dy", "1em")
                            .style("text-anchor", "middle")
                            .text("Relative Risk");    

                        //Add legend 
      /*                  svg.append("rect")
                                .attr("width", yLegend)
                                .attr("height", yLegend)
                                .style("fill", "#c97f82")
                                .attr("transform", "translate(0, " + (-yLegend - 10) + ")");
                        svg.append("text")
                                .style("font-size", function (d) {
                                    return Math.min((width / 15), 10, (height / 15));
                                })
                                .attr("transform", "translate(" + (yLegend + 2) + "," + ((-yLegend / 2) - 10) + ")")
                                .style("text-anchor", "start")
                                .text("Male");
                        svg.append("rect")
                                .attr("width", yLegend)
                                .attr("height", yLegend)
                                .style("fill", "#7f82c9")
                                .attr("transform", "translate(80, " + (-yLegend - 10) + ")");
                        svg.append("text")
                                .style("font-size", function (d) {
                                    return Math.min((width / 15), 10, (height / 15));
                                })
                                .attr("transform", "translate(" + (yLegend + 82) + "," + ((-yLegend / 2) - 10) + ")")
                                .style("text-anchor", "start")
                                .text("Female"); */
                        
                        for (var i=0; i< gendersArray.length; i++) {
                            addErrorBar(gendersArray[i], riskFactorFieldName);    
                        }                            
                         
                    }
                    return {
                        getD3HomogeneityChart: function (data, gendersArray, riskFactorFieldName, riskFactorFieldDesc) {
                            return d3HomogeneityChart(data, gendersArray, riskFactorFieldName, riskFactorFieldDesc);
                        }
                    };
                }]);