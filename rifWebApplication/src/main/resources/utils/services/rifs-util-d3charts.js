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
        .factory('D3ChartsService', ['AlertService', '$window', '$timeout',
                function (AlertService, $window, $timeout) {
                    
                    var colors = {
                        males: '#c97f82',
                        females: '#7f82c9',
                        both: '#660033'
                    };      

                    // Global data - one per handler
                    var riskGraphParameters = {};        
                    var timeoutPromise = {};
					
				   /*
					 * Function:    diffParameters2()
					 * Uses:        name, graphName, newParameters, oldParameters
					 * Returns:     0 - same, 1+ differences
					 */
					function diffParameters2(callNo, graphName, newParameters, oldParameters) {
						var differences=0;
						
						if (newParameters && Object.keys(newParameters).length > 0 && oldParameters == undefined) {
							differences++;
						}
						else if (!angular.equals(newParameters, oldParameters)) {
							differences++;
							for (var key in oldParameters) {
								if (oldParameters && oldParameters[key] &&
									newParameters[key] &&
									!angular.equals(newParameters[key], oldParameters[key])) {
									differences++;
									AlertService.consoleDebug("[rifs-util-d3charts.js] " + graphName + 
										" [" + differences + 
										"] key: " + key +
										" has changed; callNo: " + callNo +
										"; old: " + JSON.stringify(oldParameters[key]) + " != " +
										"; new: " + JSON.stringify(newParameters[key]));
								}
							}                                                                               
						}  
						
						if (differences == 0) {
//                                AlertService.consoleDebug("[rifs-util-d3charts.js] callNo: " + callNo + 
//                                    "; name: " + graphName + " no differences"/* + 
//                                    JSON.stringify(oldParameters) */);
						}
						
						return differences;                            
					}
                                    
                    /*
                     * Function:    setupRiskGraphSelector2()
                     * Parameters:  data, riskFactor2FieldName
                     * Description: Setup Risk Graph Selector
                     * Returns:     selector object (objects to set as scope)
                     */
                    function setupRiskGraphSelector2(data, riskFactor2FieldName) {
                        var fieldList=[];
                        var selector={};
                        selector.riskFactor2FieldName=riskFactor2FieldName;
                        for (var key in selector.riskFactor2FieldName) {
                            fieldList.push(selector.riskFactor2FieldName[key]);
                        }
                        
                        if (data.males && data.males.length > 0 &&
                            data.females && data.females.length > 0) {
                            selector.riskGraphHasBothMalesAndFemales=true;
                            selector.riskGraphHasFemales=true;
                            selector.riskGraphHasMales=true;
                            selector.gendersName1="males";
                            selector.gendersName2="females";
                            selector.gendersList = ['males', 'females', 'both'];			
                            selector.gendersArray=['males', 'females'];	
                        }
                        else if (data.males && data.males.length > 0) {
                            selector.riskGraphHasBothMalesAndFemales=false;
                            selector.riskGraphHasMales=true;
                            selector.riskGraphHasFemales=false;
                            selector.gendersName1="males";
                            selector.gendersName2=undefined;
                            selector.gendersList = ['males'];			
                            selector.gendersArray=['males']
                        }
                        else if (data.females && data.females.length > 0) {
                            selector.riskGraphHasBothMalesAndFemales=false;
                            selector.riskGraphHasMales=false;
                            selector.gendersName1="females";
                            selector.gendersName2=undefined;
                            selector.gendersList = ['females'];			
                            selector.gendersArray=['females']
                            selector.riskGraphHasFemales=true;
                        }
                       
                        var fieldList=[];
                        var newFieldList={};
                        for (var key in selector.riskFactor2FieldName) {
                            fieldList.push(selector.riskFactor2FieldName[key]);
                        }
                        for (var j=0; j<fieldList.length; j++) {
                            for (var k=0; k<selector.gendersList.length; k++) {
                                var gender=selector.gendersList[k];
                                var bands=data[gender];         
                                var field=fieldList[j];                      
                                for (var l=0; l<bands.length; l++) {
                                    if (data[gender] &&
                                        data[gender][l] &&
                                        data[gender][l][field]) {
                                        if (data[gender][l][field] != "0.0" &&
                                            !isNaN(data[gender][l][field]) &&
                                            parseFloat(data[gender][l][field]) > 0.0) {
                                            if (newFieldList[field]) {
                                                newFieldList[field]++;
                                            }
                                            else {
                                                newFieldList[field]=1;
                                            } 
/*                                                                        
                                            AlertService.consoleDebug("[rifs-util-d3charts.js] data[" + 
                                                gender + "][" + l + "][" + field + "]: " +
                                                JSON.stringify(data[gender][l][field]) +
                                                "; newFieldList[" + field + "]: " + newFieldList[field]);
                                        }
                                        else {                                                                 
                                            AlertService.consoleDebug("[rifs-util-d3charts.js] data[" + 
                                                gender + "][" + l + "][" + field + "]: " +
                                                JSON.stringify(data[gender][l][field]) +
                                                "; !isNaN(data[" + 
                                                gender + "][" + l + "][" + field + "]): " + !isNaN(data[gender][l][field]) +
                                                "; parseFloat(data[" + 
                                                gender + "][" + l + "][" + field + "]: " + parseFloat(data[gender][l][field])); */
                                        }
                                    }
                                }
                            }
                        }
                        var newRiskFactor2FieldName={};
                        var newRiskFactorList = [];
                        for (var key in selector.riskFactor2FieldName) {
                            if (newFieldList[selector.riskFactor2FieldName[key]] &&
                                newFieldList[selector.riskFactor2FieldName[key]] > 0) {
                                newRiskFactor2FieldName[key] = selector.riskFactor2FieldName[key];
                                newRiskFactorList.push(key);
                            }
                        }
                        if (newRiskFactorList[0]) {
                            selector.riskFactorList=newRiskFactorList;
                            selector.riskFactor=newRiskFactorList[0];
                        }
                        else {                                                    
                            AlertService.consoleDebug("[rifs-util-d3charts.js] unable to rebuild riskFactorList" +
                                ", selector.riskFactor2FieldName: " + JSON.stringify(selector.riskFactor2FieldName) +
                                ", newFieldList: " + JSON.stringify(newFieldList) +
                                "; data: " + JSON.stringify(data));
                        }
//                        AlertService.consoleDebug("[rifs-util-d3charts.js] setupRiskGraphSelector2() selector: " + 
//                            JSON.stringify(selector));
                        
                        return selector;
                    }
                                                        
                    /*
                     * Function:    hasRiskGraphDataChanged2()
                     * Parameters:  data, gendersArray, riskFactorFieldName, riskFactorFieldDesc
                     *              width, height, name, hasRiskGraphDataChangedCallback
                     * Description: Test if risk graph data and parameters have changed.
                     *              If no change, waits 0.5 seconds to avoid digest() trashing
                     * Returns:     Callback returns (error text, true/false)
                     */
                    function hasRiskGraphDataChanged2(
                        data, gendersArray, riskFactorFieldName, riskFactorFieldDesc,
                        width, height, name, hasRiskGraphDataChangedCallback) {
                            
                        if ((angular.isUndefined(data)) || (Object.keys(data).length === 0)) {
                            $timeout( function() { // To avoid Error: $rootScope:infdig Infinite $digest Loop errors
                                AlertService.consoleError("[rifs-util-d3charts.js] null data in hasRiskGraphDataChanged2");
                                hasRiskGraphDataChangedCallback(undefined /* No error */, false);
                            }, 500 /* mS */);  
                            return;
                        }
                        else if (data && gendersArray && gendersArray.length > 0 &&
                            angular.isUndefined(data[gendersArray[0]])) {
                            $timeout( function() { // To avoid Error: $rootScope:infdig Infinite $digest Loop errors
                                AlertService.consoleError("[rifs-util-d3charts.js] empty data in hasRiskGraphDataChanged2: " +
                                    JSON.stringify(data));
                                hasRiskGraphDataChangedCallback(undefined /* No error */, false);
                            }, 500 /* mS */);  
                            return;
                        }
                        else if (!(gendersArray && gendersArray.length > 0) || !riskFactorFieldName || 
                            !riskFactorFieldDesc) {
                            AlertService.consoleError("[rifs-util-d3charts.js] null parameters in hasRiskGraphDataChanged2: "  + 
                                "; gendersArray: " + (gendersArray ? JSON.stringify(gendersArray) :  "INVALID") + 
                                "; riskFactorFieldName: " + (riskFactorFieldName || "INVALID") + 
                                "; riskFactorFieldDesc: " + (riskFactorFieldDesc || "INVALID"));
                            hasRiskGraphDataChangedCallback("Null parameters passed to hasRiskGraphDataChanged2", 
                                undefined); 
                            return;
                        }
                        else if (angular.isUndefined(name)) {
                            AlertService.consoleError("[rifs-util-d3charts.js] no name in hasRiskGraphDataChanged2: "  + 
                                "; gendersArray: " + JSON.stringify(gendersArray) + 
                                "; riskFactorFieldName: " + riskFactorFieldName + 
                                "; riskFactorFieldDesc: " + riskFactorFieldDesc + 
                                "; data: " + JSON.stringify(data));
                            hasRiskGraphDataChangedCallback("No name in hasRiskGraphDataChanged2", 
                                undefined); 
                            return;
                        }
/*                        else {                     
                            AlertService.consoleDebug("[rifs-util-d3charts.js] hasRiskGraphDataChanged2: "  + 
                                "; gendersArray: " + JSON.stringify(gendersArray) + 
                                "; riskFactorFieldName: " + riskFactorFieldName + 
                                "; riskFactorFieldDesc: " + riskFactorFieldDesc + 
                                "; data: " + JSON.stringify(data));
                        } */
                                          
                        var newRiskGraphParameters = {
                            data: data, 
                            gendersArray: gendersArray, 
                            riskFactorFieldName: riskFactorFieldName, 
                            riskFactorFieldDesc: riskFactorFieldDesc,
                            modalWidth: width,
                            modalHeight: height
                        };
                        if (angular.isUndefined(riskGraphParameters[name])) {
                            riskGraphParameters[name] = angular.copy(newRiskGraphParameters);
                            hasRiskGraphDataChangedCallback(undefined /* No error */, true); // Initialisation
                        }
                        else if (diffParameters2(1, name, newRiskGraphParameters, riskGraphParameters[name]) == 0) { // No change
                            
                            if (timeoutPromise[name]) {
                                $timeout.cancel(timeoutPromise[name]);  
                            }
                            timeoutPromise[name] = $timeout( function() { // To avoid Error: $rootScope:infdig Infinite $digest Loop errors
                                var differences=diffParameters2(2, name, newRiskGraphParameters, riskGraphParameters[name]);
                                hasRiskGraphDataChangedCallback(undefined /* No error */, 
                                    ((differences == 0) ? false : true)); // Maybe
                            }, 500 /* mS */);   
                        }
                        else {
                            riskGraphParameters[name] = angular.copy(newRiskGraphParameters);
                            hasRiskGraphDataChangedCallback(undefined /* No error */, true); // Change
                        }    
                    }
                    
                    /*
                     * Function:    d3RiskGraph()
                     * Parameters:  SVG object, element Name, data, gendersArray, riskFactorFieldName, riskFactorFieldDesc, 
                     *              name (for ids), modalWidth, modalHeight, mapID, riskGraphCallback
                     * Description: Create risk graph error bars 
                     *              https://bl.ocks.org/NGuernse/8dc8b9e96de6bedcb6ad2c5467f5ef9a
                     *              1. We do need to use band_id if max_exposure_value is undefined/null
                     *              2. Use average exposure value be
                     *              3. A choice: average/band_id/distance from nearest source
                     * Returns:     Nothing
                     */
                    function d3RiskGraph(svg, elementName, mapID, newData, riskGraphCallback) {
						
                         // Check parameters are defined
                        if (angular.isUndefined(newData)) {
                            AlertService.consoleError("[rifs-util-d3charts.js] null newData in d3RiskGraph");
                            riskGraphCallback("Null newData passed to d3RiskGraph", undefined, undefined, undefined, undefined); 
                            return;
                        }
                        if (angular.isUndefined(mapID)) {
                            AlertService.consoleError("[rifs-util-d3charts.js] null newData in d3RiskGraph");
                            riskGraphCallback("Null newData passed to d3RiskGraph", undefined, undefined, undefined, undefined); 
                            return;
                        }
						
						var data=newData.riskGraphData;
						var gendersArray=newData.gendersArray;
						var riskFactorFieldName=newData.riskFactorFieldName; 
                        var riskFactorFieldDesc=newData.riskFactorFieldDesc;
						var name=newData.name;
						var modalWidth=newData.width;
						var modalHeight=newData.height;
						
                        // Check parameters are defined
                        if (angular.isUndefined(data)) {
                            AlertService.consoleError("[rifs-util-d3charts.js] null data in d3RiskGraph; newData: " +
								JSON.stringify(newData, 0, 1));
                            riskGraphCallback("Null data passed to d3RiskGraph", undefined, undefined, undefined, undefined); 
                            return;
                        }
                        else if (!(gendersArray && gendersArray.length > 0) || !riskFactorFieldName || 
                            !riskFactorFieldDesc || !name) {
                            AlertService.consoleError("[rifs-util-d3charts.js] null parameters in d3RiskGraph: "  + 
                                "; gendersArray: " + (gendersArray ? JSON.stringify(gendersArray) :  "INVALID") + 
                                "; riskFactorFieldName: " + (riskFactorFieldName || "INVALID") + 
                                "; riskFactorFieldDesc: " + (riskFactorFieldDesc || "INVALID") + 
                                "; name: " + (name || "INVALID"));
                            AlertService.consoleError("[rifs-util-d3charts.js] newData: " + JSON.stringify(newData));
                            riskGraphCallback("Null parameters passed to d3RiskGraph", undefined, undefined, undefined, undefined); 
                            return;
                        }
                        else if (data && gendersArray && gendersArray.length > 0 &&
                            angular.isUndefined(data[gendersArray[0]])) {
                            AlertService.consoleError("[rifs-util-d3charts.js] empty data in d3RiskGraph: " + 
                                JSON.stringify(data));
                            riskGraphCallback("Empty data passed to d3RiskGraph: " + JSON.stringify(data), 
								undefined, undefined, undefined, undefined); 
                            return;
                        }
                        else if (data && gendersArray && gendersArray.length > 0 && data[gendersArray[0]] && (
                            angular.isUndefined(data[gendersArray[0]][0]) ||
                            angular.isUndefined(data[gendersArray[0]][0][riskFactorFieldName]))) {
                            AlertService.consoleError("[rifs-util-d3charts.js] no " + riskFactorFieldName + 
                                " data in d3RiskGraph: " + JSON.stringify(data));
                            riskGraphCallback("No " + riskFactorFieldName + " data passed to d3RiskGraph: " + 
                                JSON.stringify(data), undefined, undefined, undefined, undefined); 
                            return;
                        }
                        else {                     
//                            AlertService.consoleDebug("[rifs-util-d3charts.js] d3RiskGraph"  + 
//                                "; gendersArray: " + JSON.stringify(gendersArray) + 
//                                "; riskFactorFieldName: " + riskFactorFieldName + 
//                                "; riskFactorFieldDesc: " + riskFactorFieldDesc + 
//                                "; data: " + JSON.stringify(data));
                        }
                   
                        /*
                         * Function:    getDomain()
                         * Parameters:  gender
                         * Description: Get max/min for X/Y data domains; scale to 20% bigger than range
                         * Returns:     domainExtent: {
                         *        "riskFactorFieldName": "max_exposure_value",
                         *        "gendersArray": [
                         *         "males",
                         *         "females"
                         *        ],
                         *        "xMin": 48,
                         *        "xMax": 86.39999999999999,
                         *        "yMin": 0.386280032070744,
                         *        "yMax": 1.7259575019473998
                         *       }
                         */ 
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
                                if (angular.isUndefined(data[gendersArray[i]])) {
                                    riskGraphCallback("No data found", undefined, undefined);
                                    return;
                                }
                                var yMin = d3.min(data[gendersArray[i]], function(d) { return (parseFloat(d.lower95)*0.8); }); 
                                if (!domainExtent.yMin) {
                                    domainExtent.yMin=yMin;
                                }
                                else if (yMin < domainExtent.yMin) {
                                    domainExtent.yMin=yMin;
                                }
                                
                                var yMax = d3.max(data[gendersArray[i]], function(d) { return (parseFloat(d.upper95)*1.2); });            
                                if (!domainExtent.yMax) {
                                    domainExtent.yMax=yMax;
                                }
                                else if (yMax > domainExtent.yMax) {
                                    domainExtent.yMax=yMax;
                                }
                                
                                var xMin = d3.min(data[gendersArray[i]], function(d) { 
                                    return parseFloat(d[riskFactorFieldName])*0.8; });                        
                                if (!domainExtent.xMin) {
                                    domainExtent.xMin=xMin;
                                }
                                else if (xMin < domainExtent.xMin) {
                                    domainExtent.xMin=xMin;
                                }
                                
                                var xMax = d3.max(data[gendersArray[i]], function(d) { 
                                    return parseFloat(d[riskFactorFieldName])*1.4; });                            
                                if (!domainExtent.xMax) {
                                    domainExtent.xMax=xMax;
                                }
                                else if (xMax > domainExtent.xMax) {
                                    domainExtent.xMax=xMax;
                                }
                            } 
                            return domainExtent;
                        }
                          
                        /*
                         * Function:    gender2text()
                         * Parameters:  gender
                         * Returns:     Gender name
                         */                  
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
                                default:
                                    rval="unknown: " + gender;
                                    break;
                            }
                            return rval;
                        }          		
                        
                        /*
                         * Function:    addErrorBar()
                         * Parameters:  svg object, gendersName, riskFactorFieldName, name (id of SVG)
                         * Description: Add error bar
                         * Returns:     Nothing
                         */
                        function addErrorBar(svg, gendersName, riskFactorFieldName, name) {
                            // Add Error Line
                            svg.append("g").selectAll("line")
                               .data(data[gendersName]).enter()
                               .append("line")
                               .attr("class", "d3RiskGraph-error-line")
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
                               })
                               .style("stroke", colors[gendersName]);

                            // Add Error Top Cap
                            svg.append("g").selectAll("line")
                               .data(data[gendersName]).enter()
                               .append("line")
                               .attr("class", "d3RiskGraph-error-cap")
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
                              })
                              .style("stroke", colors[gendersName]);
                              
                             // Add Error Bottom Cap
                            svg.append("g").selectAll("line")
                               .data(data[gendersName]).enter()
                               .append("line")
                               .attr("class", "d3RiskGraph-error-cap")
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
                               })
                               .style("stroke", colors[gendersName]); 
                              
                            // Add Scatter Points
                            svg.append("g").attr("class", "scatter")
                               .selectAll("circle")
                               .data(data[gendersName]).enter()
                               .append("circle")
                               .attr("cx", function(d) {
                                    return xScale(d[riskFactorFieldName]);
                               })
                               .attr("cy", function(d) {
                                    return yScale(d.relativeRisk);
                               })
                               .attr("r", 4)
                               .style("fill", colors[gendersName])
                               .on("mouseover", function(d, i, n) {
                                    var transform=this.getScreenCTM().translate(+this.getAttribute("cx"),
                                        +this.getAttribute("cy")); /* Actual pixel position of circle */
                                    var transform0=this.getScreenCTM().translate(0, 0); 
                                                                   /* Actual pixel position of SVG graph div origin */

                                    var correctionFactors = {
                                        riskGraph: {
                                           left: 18,
                                           top: 153
                                        },
                                        riskGraph2: { // These can be slightly wrong if you change the width
                                           left: 14,
                                           top: -35
                                        }
                                    }
/*                                    AlertService.consoleDebug("[rifs-util-d3charts.js] d3RiskGraph: " + name + 
                                        " mouseover(" +
                                        transform.e + " - " + transform0.e + ", " +
                                        transform.f + " - " + transform0.f + ")" +
                                        "; marginHeightWidth.margin: " + JSON.stringify(marginHeightWidth.margin) +
                                        "; correctionFactors: " + JSON.stringify(correctionFactors[name]) +
                                        "; data: " + JSON.stringify(d, 0, 1)); */
                                    return tooltip.html("Band " + parseInt(d.bandId) + " " +
                                                gender2text(parseInt(d.genders)) + "</br>" + 
                                                parseFloat(d.relativeRisk).toFixed(3) + 
                                                "&nbsp;[95% CI&nbsp;" + parseFloat(d.lower95).toFixed(3) +
                                                "&ndash;" + parseFloat(d.upper95).toFixed(3) + "]") 
                                        .style("visibility", "visible")
                                        .style("left", (transform.e - transform0.e + marginHeightWidth.margin.left + 
                                            (correctionFactors[name] ? correctionFactors[name].left : 18)) + "px") 
                                            // Correct as position is relative
                                        .style("top", (transform.f - transform0.f + marginHeightWidth.margin.top +  + 
                                            (correctionFactors[name] ? correctionFactors[name].top : 153)) + "px")
                                        .style("background", colors[gendersName]);
                               })
                               .on("mouseout", function() {
                                    return tooltip.style("visibility", "hidden");
                               });  
                        } 
                        
                        /*
                         * Function:    getMargin()
                         * Parameters:  Modal width, height
                         * Description: Setup margin, height and width
                         * Returns:     marginHeightWidth: {
                         *    "margin": {
                         *     "top": 20,
                         *     "right": 20,
                         *     "bottom": 40,
                         *     "left": 40
                         *    },
                         *    "width": 900,
                         *    "height": 350,
                         *    "hSplit3Width": 0,
                         *    "hSplit3Height": 0,
                         *    "innerWidth": 1920,
                         *    "innerHeight": 938
                         *   }
                         */
                        function getMargin(modalWidth, modalHeight) {
                            var marginHeightWidth = {
                                margin: {top: 20, right: 20, bottom: 40, left: 80}, // Scale using CSS
                                modalWidth: modalWidth,
                                modalHeight: modalHeight,
                                hSplitWidth: 0,
                                hSplitHeight: 0,
                                hSplitTag: "#hSplit3",
                                using: "defaults",
                                innerWidth: $window.innerWidth,
                                innerHeight: $window.innerHeight
                            };
                            
                            // 1346x416 
                            var hSplit=d3.select(marginHeightWidth.hSplitTag); // Info
                            if (hSplit && hSplit.size() > 0) {
                                marginHeightWidth.hSplitWidth = hSplit.node().getBoundingClientRect().width;
                                marginHeightWidth.hSplitHeight = hSplit.node().getBoundingClientRect().height;
                            }
                            else {
                                marginHeightWidth.hSplitTag="#hSplit1";
                                hSplit=d3.select(marginHeightWidth.hSplitTag); // Viewer
                                if (hSplit && hSplit.size() > 0) {
                                    marginHeightWidth.hSplitWidth = hSplit.node().getBoundingClientRect().width;
                                    marginHeightWidth.hSplitHeight = hSplit.node().getBoundingClientRect().height;
                                }
                            }
                            var width=860; // Defaults for info modal
                            var height=410; 
                            if (marginHeightWidth.modalWidth && marginHeightWidth.modalHeight) {
                                width = marginHeightWidth.modalWidth;
                                height = marginHeightWidth.modalHeight;
                                marginHeightWidth.using = "modal directive supplied values";
                            }
                            else if (marginHeightWidth.hSplitWidth && marginHeightWidth.hSplitHeight) {
                                width = marginHeightWidth.hSplitWidth;
                                height = marginHeightWidth.hSplitHeight;
                                marginHeightWidth.using = "hSplit tag: " + marginHeightWidth.hSplitTag;
                            }
                            else if (marginHeightWidth.innerWidth && marginHeightWidth.innerHeight) {
                                width=marginHeightWidth.innerWidth*0.65;
                                height=marginHeightWidth.innerHeight*0.55;
                                marginHeightWidth.using = "$window.innerWidth, $window.innerHeight";
                                
                                if (marginHeightWidth.hSplitTag == "#hSplit3") { // viewer: set hSplit3
                                    var hSplit3=document.getElementById("hSplit3");
                                    if (hSplit3) { 
                                        hSplit3.style.height = height + "px";
                                        hSplit3.style.width = width + "px";
                                    }
                                }
                            }                
                            
                            marginHeightWidth.width = width - 
                                marginHeightWidth.margin.left - marginHeightWidth.margin.right;                        
                            marginHeightWidth.height = height - 
                                marginHeightWidth.margin.top - marginHeightWidth.margin.bottom;
                                
                            return marginHeightWidth;
                        }                      
                              
                        var tooltip=d3.select("#" + name + "Tooltip")
                        if (tooltip && tooltip.size() > 0) {
                            tooltip.selectAll("div").remove(); 
                        }                            
                        tooltip = d3.select("#" + name + "Tooltip").append("div")
                                        .attr("class", "riskGraph-tooltip")
                                        .style("visibility", "hidden");                       
                        
                        var domainExtent=getDomain(data, gendersArray, riskFactorFieldName);
                        var marginHeightWidth = getMargin(modalWidth, modalHeight);
//                        AlertService.consoleDebug("[rifd-util-info.js] riskGraph: " + name + 
//                            "; domainExtent: " + JSON.stringify(domainExtent, null, 1) +
//                            "; marginHeightWidth: " + JSON.stringify(marginHeightWidth, null, 1) );
                            
                        var xScale = d3.scaleLinear()
                                       .range([0, marginHeightWidth.width])
                                       .domain([domainExtent.xMin, domainExtent.xMax]).nice();
                        var yScale = d3.scaleLinear()
                                       .range([marginHeightWidth.height, 0])
                                       .domain([domainExtent.yMin, domainExtent.yMax]).nice();                                      
                         
                        var xAxis;
                        if (riskFactorFieldName == "bandId") {
                            xAxis = d3.axisBottom(xScale).ticks(domainExtent.xMax.toFixed());
                        }
                        else {
                            xAxis = d3.axisBottom(xScale).ticks(12);
                        }
                        var yAxis = d3.axisLeft(yScale).ticks(12 * marginHeightWidth.height / marginHeightWidth.width);

                        let line = d3.line()
                                     .x(function(d) {
                                        return xScale(d.max_exposure_value);
                                     })
                                     .y(function(d) {
                                            return yScale(d.relativeRisk);
                                     });

                        var svg2=svg.attr("id", name)
                                    .attr("width", 
                                            marginHeightWidth.width + marginHeightWidth.margin.left + marginHeightWidth.margin.right)
                                    .attr("height", 
                                            marginHeightWidth.height + marginHeightWidth.margin.top + marginHeightWidth.margin.bottom /* + 20 */)
                                    .append("g")
                                    .attr("transform", "translate(" + 
                                            marginHeightWidth.margin.left + "," + marginHeightWidth.margin.top + ")");
                 
                        svg2.append("g").append("rect")
                            .attr("width", marginHeightWidth.width)
                            .attr("height", marginHeightWidth.height)
                            .attr("class", "riskGraph-bg");

                        // Add Axis and labels
                        svg2.append("g").attr("class", "axis axis--x")
                            .attr("transform", "translate(" + 0 + "," + marginHeightWidth.height + ")")
                            .call(xAxis);
                            
                        svg2.append("g").attr("class", "axis axis--y")
                            .call(yAxis);            
              
                        // text label for the x axis
                        svg2.append("text")           
                            .attr("transform",                           
                                  "translate(" + (marginHeightWidth.width/2) + " ," + 
                                                 (marginHeightWidth.height + marginHeightWidth.margin.top + 10) + ")")
//                            .attr("x", (marginHeightWidth.width / 2))
//                            .attr("y", (marginHeightWidth.height + 20))  
//                            .attr("dy", "1em")
                            .style("text-anchor", "middle")
                            .text(riskFactorFieldDesc.toUpperCase());
                            
                        // text label for the y axis
                        svg2.append("text")
                            .attr("transform", "rotate(-90)")
                            .attr("y", 0 - ((45<marginHeightWidth.margin.left) ? 45 : marginHeightWidth.margin.left)  
                                    /* was marginHeightWidth.margin.left */)
                            .attr("x", 0 - (marginHeightWidth.height / 2))
                            .attr("dy", "1em")
                            .style("text-anchor", "middle")
                            .text("RELATIVE RISK");  
                            
                        // Add title	  
//                        svg2.append("text")
//                            .attr("class", "homogeneityTitle")
//                            .attr("x", (marginHeightWidth.width / 2))
//                            .attr("y", 20)
//                            .style("text-anchor", "middle")
//                            .text("Risk Graph");
                            
                        // Add legend   
                        var legend = svg2.append("g")
                                         .attr("class", "legend")
                                         .attr("height", 100)
                                         .attr("width", 100)
                                         .attr('transform', 'translate(-20,20)');   // Was (-20,50) */                          
                         
                        for (var i=0; i< gendersArray.length; i++) {
                            addErrorBar(svg2, gendersArray[i], riskFactorFieldName, name);  
                        
                            // Add legend   
                            legend.append("rect")
                                  .attr("x", marginHeightWidth.width - 135)
                                  .attr("y", (i * 12)) // Was (i * 20)
                                  .attr("width", 10)
                                  .attr("height", 10)
                                  .style("fill", colors[gendersArray[i]]); 
                            legend.append("text")
                                  .attr("x", marginHeightWidth.width - 122)
                                  .attr("y", ((i * 12) + 9)) // Was (i * 20)
                                  .style("text-transform", "capitalize")
                                  .text((gendersArray[i] == "both" ? "Both males and females" : gendersArray[i]));                          
                        }                            
                         
                         riskGraphCallback(undefined /* No error */, svg2, elementName, mapID, newData);
                    }
                    return {
                        getD3RiskGraph: function (svg, elementName, data, gendersArray, riskFactorFieldName, 
                            riskFactorFieldDesc, name, modalWidth, modalHeight, riskGraphCallback) {
                            try {
								var newData = {
									riskGraphData: data, 
									gendersArray: gendersArray, 
									riskFactorFieldName: riskFactorFieldName, 
                                    riskFactorFieldDesc: riskFactorFieldDesc, 
									name: name, 
									width: modalWidth, 
									height: modalHeight
								};
                                return d3RiskGraph(svg, elementName, "viewer", newData, riskGraphCallback);
                            }
                            catch (e) {
                                riskGraphCallback("Caught exception: " + e.toString(), undefined, undefined, undefined);
                            }
						},
                        getD3RiskGraph2: function (svg, elementName, mapID, newData, riskGraphCallback) {
                            try {
                                return d3RiskGraph(svg, elementName, mapID, newData, riskGraphCallback);
                            }
                            catch (e) {
                                riskGraphCallback("Caught exception: " + e.toString(), undefined, undefined, undefined);
                            }
                        },
                        hasRiskGraphDataChanged: function (data, gendersArray, riskFactorFieldName, riskFactor, 
                             width, height, name, hasRiskGraphDataChangedCallback) {
                            try {
                                return hasRiskGraphDataChanged2(data, gendersArray, riskFactorFieldName, riskFactor, 
                                    width, height, name, hasRiskGraphDataChangedCallback);
                            }
                            catch (e) {
                                hasRiskGraphDataChangedCallback("Caught exception: " + e.toString(), undefined);
                            }
                        },
                        setupRiskGraphSelector: function (data, riskFactor2FieldName) {
                            return setupRiskGraphSelector2(data, riskFactor2FieldName);
                        },
                        diffParameters: function (graphName, newParameters, oldParameters) {
                            return diffParameters2(1, graphName, newParameters, oldParameters);
                        }
                        
                    };
                }]);