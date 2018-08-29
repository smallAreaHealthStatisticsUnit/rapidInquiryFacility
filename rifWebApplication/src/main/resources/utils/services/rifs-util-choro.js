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
 * SERVICE to render choropleth maps using Colorbrewer
 */

/* global d3, ss, L, Infinity */

angular.module("RIF")
        .factory('ChoroService', ['ColorBrewerService', 'ParametersService', 'AlertService', 'ViewerStateService',
            function (ColorBrewerService, ParametersService, AlertService, ViewerStateService) {

				var defaultChoroScaleMethod = {
					'viewermap': {
							'method': 		'quantile', 
							'feature':		'relative_risk',
							'intervals': 	9,
							'invert':		true,
							'brewerName':	"PuOr",
							isDefault:		true
					},
					'diseasemap1': {
							'method': 		'quantile', 
							'feature':		'smoothed_smr',
							'intervals': 	9,
							'invert':		true,
							'brewerName':	"PuOr",
							isDefault:		true
					},
					'diseasemap2': {
							'method': 		'AtlasProbability', 
							'feature':		'posterior_probability',
							'intervals': 	3,
							'invert':		false,
							'brewerName':	"Constant",
							isDefault:		true
					}
				};
                      
				var defaultClassificationsList = [ // Standard methods
					{
						id: 	'quantile',
						label: 	'Quantile'
					},
					{
						id: 	'quantize',
						label: 	'Equal Interval'
					},
					{
						id: 	'jenks',
						label: 	'Jenks'
					},
					{
						id: 	'standardDeviation',
						label: 	'Standard Deviation'
					}
				];
				var classificationsList=angular.copy(defaultClassificationsList);	
				
                //a default symbology
                function symbology(mapID, choroScaleMethod) {
					
                    this.features = [];
                    this.brewerName = choroScaleMethod[mapID].brewerName || defaultChoroScaleMethod[mapID].brewerName;
                    this.intervals = choroScaleMethod[mapID].intervals || defaultChoroScaleMethod[mapID].intervals;
                    this.feature = choroScaleMethod[mapID].feature || defaultChoroScaleMethod[mapID].feature;
                    this.invert = choroScaleMethod[mapID].invert || defaultChoroScaleMethod[mapID].invert;
                    this.method = choroScaleMethod[mapID].method || defaultChoroScaleMethod[mapID].method;
					this.isDefault = defaultChoroScaleMethod[mapID].isDefault || false; 
                    this.renderer = {
						intervals: this.intervals,
                        scale: null,
                        breaks: [],
                        range: ["#9BCD9B"],
                        mn: null,
                        mx: null
                    };

                    this.init = false;
                }
				
				var parameters=ParametersService.getParameters();
				
				var choroScaleMethod = undefined;
				if (parameters && parameters.mappingDefaults) {
					choroScaleMethod = parameters.mappingDefaults;
				}
                var maps = {	
                    'viewermap': new symbology('viewermap', choroScaleMethod),					
                    'diseasemap1': new symbology('diseasemap1', choroScaleMethod),
                    'diseasemap2': new symbology('diseasemap2', choroScaleMethod) //default for 2nd disease map is probability */
                };
				
                //used in viewer map
                function renderFeatureMapping(scale, value, selected) {
                    //returns fill colour
                    //selected
                    if (selected && !angular.isUndefined(value)) {
                        return "green";
                    }
                    //choropleth
                    if (scale && !angular.isUndefined(value)) {
                        return scale(value);
                    } else if (angular.isUndefined(value)) {
                        return "lightgray";
                    } else {
                        return "#9BCD9B";
                    }
                }

                //used in disease mapping
                function renderFeatureViewer(scale, feature, value, selection) {
                    //returns [fill colour, border colour, border width]
                    //selected (a single polygon)
                    if (selection === feature.properties.area_id) {
                        if (scale && !angular.isUndefined(value)) {
                            return [scale(value), "green", 5];
                        } else {
                            return ["#9BCD9B", "green", 5];
                        }
                    }
                    //choropleth
                    if (scale && !angular.isUndefined(value)) {
                        return [scale(value), "gray", 1];
                    } else if (angular.isUndefined(value)) {
                        return ["lightgray", "gray", 1];
                    } else {
                        return ["#9BCD9B", "gray", 1];
                    }
                }

				/*
				 * Function: 	choroScale()
				 * Arguments:	method name, data domain, input range, flip (invert color ramp),
				 *				map ID, previous rval object (may be null), number of intervals,
				 * 				scope of calling controller (for access to alert controller)
				 * Description:	Set up choropleth map scale
				 * Called from: renderSwatch(), getChoroScale() [thence controller rifc-util-choro.js]
				 * Returns:		rval object {
                 *    			   	scale: scale,
                 * 			      	breaks: breaks,
                 * 			      	range: range,
                 *       			mn: mn,
                 *       			mx: mx}
				 */
                function choroScale(methodObj, domain, rangeIn, flip, map, oldRval, intervals, choroScope) {
                    var scale;
					var method;
					if (typeof methodObj === 'object') { // Comes from rifp-util-choro.html renderSwatch()
						throw new Error("Choropeth map method: " + JSON.stringify(methodObj) + 
							" not valid for choroScale()");  
					}
					else {
						method=methodObj;
					}
                    var mx = Math.max.apply(Math, domain); // Defaults: max
                    var mn = Math.min.apply(Math, domain); // Defaults: min
					
                    var range = [];
					var breaks = [];
                    //flip the colour ramp
                    if (!flip) {
                        range = angular.copy(rangeIn);
                    } else {
                        range = angular.copy(rangeIn).reverse();
                    }
					var brewerName=maps[map].brewerName;
					var description=method;
					var changeAble=true;
					var selectedFeature=choroScope.input.selectedFeature;

                    //find the breaks
                    switch (method) {
                        case "quantile":
                            scale = d3.scaleQuantile()
                                    .domain(domain)
                                    .range(range);
                            breaks = scale.quantiles();
                            break;
                        case "quantize": // Equal Interval
                            scale = d3.scaleQuantize()
                                    .domain([mn, mx])
                                    .range(range);
                            var l = (mx - mn) / scale.range().length;
                            for (var i = 0; i < range.length; i++) {
                                breaks.push(mn + (i * l));
                            }
                            breaks.shift();
                            break;
                        case "jenks":
                            breaks = ss.jenks(domain, range.length);
                            breaks.pop(); //remove max
                            breaks.shift(); //remove min
                            scale = d3.scaleThreshold()
                                    .domain(breaks)
                                    .range(range);
                            break;
                        case "standardDeviation":
                            /*
                             * Implementation derived by ArcMap Stand. Deviation classification
                             * 5 intervals of which those around the mean are 1/2 the Standard Deviation
                             */
                            if (maps[map].brewerName === "Constant") {
                                scale = d3.scaleQuantile()
                                        .domain(domain)
                                        .range(range);
                                breaks = scale.quantiles();
                                break;
                            }
                            var sd = ss.sample_standard_deviation(domain);
                            var mean = d3.mean(domain);
                            var below_mean = mean - sd / 2;
                            var above_mean = mean + sd / 2;
                            for (var i = 0; below_mean > mn && i < 2; i++) {
                                breaks.push(below_mean);
                                below_mean = below_mean - sd;
                            }
                            for (var i = 0; above_mean < mx && i < 2; i++) {
                                breaks.push(above_mean);
                                above_mean = above_mean + sd;
                            }
                            breaks.sort(d3.ascending);
                            //dynamic scale range as number of classes unknown
                            range = ColorBrewerService.getColorbrewer(maps[map].brewerName, breaks.length + 1);
                            scale = d3.scaleThreshold()
                                    .domain(breaks)
                                    .range(range);
                            break;						
                        case "logarithmic":
							throw new Error("Choropeth map method: " + method + " not implemented");       
                            break;
						default:
							// Process 
							if (!parameters.userMethods) {		
								throw new Error("Cannot find user defined methods for choropeth map method: " + 
									method);  
							}
							else {
								var userMethodFound=false;
								for (var userMethodName in parameters.userMethods) {
									if (userMethodName == method) {
										userMethodFound=true;
										changeAble=false;
										
										var userMethod=parameters.userMethods[userMethodName]
										var numBreaks=0; 
										if (userMethod.breaks == undefined) {
											if (choroScope.showWarning) { // Should always be in scope
												choroScope.showWarning("No breaks are defined");
											}
											else {
												throw new Error("choroScope.showWarning() not in scope");
											}
										}
										else if (userMethod.breaks) {
											breaks = angular.copy(userMethod.breaks); 
										}										
										if (breaks && breaks.length < 2) {
											if (choroScope.showWarning) { // Should always be in scope
												choroScope.showWarning("Insufficent breaks defined: " + 
													breaks.length + "; a minumum of 2 are required.");
											}					
										}
										else {
											numBreaks=breaks.length-1; // Breaks expected
										}
										
										if (userMethod.invalidScales == undefined) {
											throw new Error("No invalidScales are defined for " + userMethodName);
										}
										
										if (userMethod.brewerName == undefined) {
											throw new Error("No brewerName are defined for " + userMethodName);
										}
										
										if (userMethod.feature) {
											selectedFeature=userMethodName.feature;
										}
										if (userMethod.description) {
											description=userMethod.description;
										}
										if (userMethod.invert && userMethod.invert == true) {
											flip=userMethod.invert;
										}
										else {
											flip=false;
										}
										
										var tmp;
										if (oldRval && oldRval.method && oldRval.method == method) {
											// Method has not changed
											
											if (userMethod.invalidScales &&
												userMethod.invalidScales.indexOf(maps[map].brewerName) !== -1) {
												
												if (choroScope.showWarning) { // Should always be in scope
													choroScope.showWarning("Color brewer: " + maps[map].brewerName +
														" is not valid for " + userMethodName);
												}
												else {
													throw new Error("choroScope.showWarning() not in scope");
												}
												tmp = ColorBrewerService.getColorbrewer(userMethod.brewerName, 
													numBreaks);
												brewerName=userMethod.brewerName;
											} 
											else { // Method has not changed; user has changed color brewer
												tmp = ColorBrewerService.getColorbrewer(maps[map].brewerName, 
													numBreaks);
											}
										}
										else { // Method has changed, use default
											tmp = ColorBrewerService.getColorbrewer(userMethod.brewerName, 
												numBreaks);
											brewerName=userMethod.brewerName;
										}
										
										if (tmp == undefined) {
											throw new Error("No brewerName range cound be deduced for " + userMethodName);
										}
										
										if (!flip) {
											range = angular.copy(tmp);
										} 
										else {
											range = angular.copy(tmp).reverse();
										}
		
										if (breaks) {
											mn=breaks.shift(); // Remove first element (mn)
											mx=breaks.pop(); // Remove last element (mx)
											if (mn == undefined) {
												mn=-Infinity;
											}
											if (mx == undefined) {
												mx=Infinity;
											}	
											
											scale = d3.scaleThreshold()
													.domain(breaks)
													.range(range);
										}			
									}
								}
								if (!userMethodFound) {	
									throw new Error("Cannot find user method for choropeth map method: " + 
										JSON.stringify(method));  
								}
							}
                    }
					var rval={
						selectedFeature: selectedFeature,
						intervals: (breaks.length+1),
						invert: flip,
						method: method,
						description: description,
						changeAble: changeAble,
						brewerName: brewerName,
                        scale: scale,
                        breaks: breaks,
                        range: range,
                        mn: mn,
                        mx: mx,
						oldRval: oldRval
                    };
					
                    return rval;
                }

                function makeLegend(thisMap, attr) {
                    return (function () {
                        var div = L.DomUtil.create('div', 'info legend');
                        div.innerHTML += '<h4>' + attr.toUpperCase().replace("_", " ") + '</h4>';
                        if (!angular.isUndefined(thisMap.range)) {
							
							if (thisMap.breaks.length != (thisMap.range.length-1)) {
								throw new Error("[rifs-util-choro.js] thisMap.breaks length error: " + i +
									"; thisMap.breaks: " + JSON.stringify(thisMap.breaks) +
									"; length: " + thisMap.breaks.length +
									"; thisMap.range: " + JSON.stringify(thisMap.range) +
									"; length: " + thisMap.range.length);
							}
							
                            for (var i = thisMap.range.length - 1; i >= 0; i--) {
                                div.innerHTML += '<i style="background:' + thisMap.range[i] + '"></i>';
                                if (i === 0) { //first break
									if (thisMap.breaks[i]) {
										div.innerHTML += '<span>&lt;' + thisMap.breaks[i].toFixed(2) + '</span>';
									}
									else {
										div.innerHTML += '<span>&lt;0</span>';
									}
                                } else if (i === thisMap.range.length - 1) { //last break
									if (thisMap.breaks[i - 1]) {
										div.innerHTML += '<span>&ge;' + thisMap.breaks[i - 1].toFixed(2) + '</span><br>';
									}
									else {
										div.innerHTML += '<span>&ge;0</span><br>';
									}
                                } else {
									if (thisMap.breaks[i - 1] && thisMap.breaks[i]) {
										div.innerHTML += '<span>' + thisMap.breaks[i - 1].toFixed(2) + ' - &lt;' + thisMap.breaks[i].toFixed(2) + '</span><br>';
									}
									else if (thisMap.breaks[i - 1]) {
										div.innerHTML += '<span>' + thisMap.breaks[i - 1].toFixed(2) + ' - &lt;0</span><br>';	
									}
									else if (thisMap.breaks[i]) {						
										div.innerHTML += '<span>0 - &lt;' + thisMap.breaks[i].toFixed(2) + '</span><br>';			
									}
									else { // This is surely a bug
										div.innerHTML += '<span>0</span><br>';									
									}
                                }
                            }
                        }
                        return div;
                    });
                }
				
				/*
				 * Function: 	renderSwatch()
				 * Arguments:	Called on modal open, bCalc (believed to always be true),
				 *				scope of calling controller, color brewer service
				 * Called from: doRenderSwatch()
				 * Description:	Redo all choropleth map scales
				 * Returns:		choroScope.input.thisMap
				 */
				function renderSwatch(
					bOnOpen /* Called on modal open */, 
					bCalc /* Secret field, always true */, 
					choroScope, 
					ColorBrewerService) {
				//Extract method from methodObj

					if (typeof choroScope.input.methodObj === 'object' &&
						choroScope.input.methodObj.id) { // Comes from rifp-util-choro.html renderSwatch()
						choroScope.input.method=choroScope.input.methodObj.id;
					}
					else {
						if (choroScope.consoleError) { // Should always be in scope
							if (choroScope.input.methodObj) {
								choroScope.consoleError("Error in renderSwatch() choroScope.input.methodObj not recognized: " + 
									JSON.stringify(choroScope.input.methodObj));
							}
							else if (choroScope.input) { // Not really and error - being called too early
								choroScope.consoleLog("WARNING: renderSwatch() being called too early choroScope.input.methodObj not defined: " + 
									JSON.stringify(choroScope.input)); 

//								throw new Error("renderSwatch() being called too early");
							}
							else {
								choroScope.consoleError("Error in renderSwatch() choroScope.input not defined", 
									new Error("renderSwatch() choroScope.input not defined"));
							}
						}
						else {
							throw new Error("Error in renderSwatch() choroScope.iconsoleError not defined: " + 
								JSON.stringify(choroScope.input.methodObj));
						}
					}	
				
                //ensure that the colour scheme allows the selected number of classes
					var n = angular.copy(choroScope.input.selectedN);
					choroScope.input.intervalRange = ColorBrewerService.getSchemeIntervals(
						choroScope.input.currOption.name);
					if (choroScope.input.selectedN > Math.max.apply(Math, choroScope.input.intervalRange)) {
						choroScope.input.selectedN = Math.max.apply(Math, choroScope.input.intervalRange);
					} else if (choroScope.input.selectedN < Math.min.apply(Math, choroScope.input.intervalRange)) {
						choroScope.input.selectedN = Math.min.apply(Math, choroScope.input.intervalRange);
					}
					
					if (n != choroScope.input.selectedN) {					
						if (choroScope.consoleLog) { // Should always be in scope
							choroScope.consoleLog("[rifs-util-choro.js] choroScope.input.selectedN changed from: " + n + " to: " +
								choroScope.input.selectedN);
						}
					}

					//get the domain 
					choroScope.domain.length = 0;
					for (var i = 0; i < choroScope.tableData[choroScope.mapID].length; i++) {
						choroScope.domain.push(Number(choroScope.tableData[choroScope.mapID][i][choroScope.input.selectedFeature]));
					}

					//save the selected brewer
					maps[choroScope.mapID].brewerName = choroScope.input.currOption.name;

					try {
						var oldRval;
						if (maps[choroScope.mapID].renderer && maps[choroScope.mapID].renderer.breaks &&
						    maps[choroScope.mapID].renderer.breaks.length > 0) {
							oldRval=maps[choroScope.mapID].renderer;
						}
						
						if (bOnOpen) {
							//if called on modal open
							if (!maps[choroScope.mapID].init) {
								//initialise basic renderer
								maps[choroScope.mapID].init = true;
								choroScope.input.thisMap = choroScale(
									choroScope.input.method, 		// Method name
									choroScope.domain, 				// Data
									ColorBrewerService.getColorbrewer(
										choroScope.input.currOption.name, choroScope.input.selectedN), 
									choroScope.input.checkboxInvert, // Flip
									choroScope.mapID,				// Map
									oldRval,						// Old (previous) rval
									choroScope.input.selectedN,		// Intervals
									choroScope);					// $scope of controller 
																	// (for access to alert service)
								maps[choroScope.mapID].renderer = choroScope.input.thisMap;
							} 
							else {
								//restore previous renderer
								choroScope.input.thisMap = maps[choroScope.mapID].renderer;
							}
						} else {
							//update current renderer
							if (!bCalc) {
								if (n !== choroScope.input.selectedN) {
									//reset as class number requested not possible
									choroScope.input.thisMap = choroScale(
										choroScope.input.method,  		// Method name
										choroScope.domain,  			// Data
										ColorBrewerService.getColorbrewer(
											choroScope.input.currOption.name,
											choroScope.input.selectedN), 
										choroScope.input.checkboxInvert, // Flip 
										choroScope.mapID,				// Map
										oldRval,						// Old (previous) rval
										choroScope.input.selectedN,		// Intervals
										choroScope);					// $scope of controller 
																		// (for access to alert service)
								} 
								else {
									var tempRenderer = choroScale(
										choroScope.input.method,  		// Method name
										choroScope.domain,  			// Data
										ColorBrewerService.getColorbrewer(
											choroScope.input.currOption.name,
											choroScope.input.selectedN), 
										choroScope.input.checkboxInvert, // Flip 
										choroScope.mapID,				// Map
										oldRval,						// Old (previous) rval
										choroScope.input.selectedN,		// Intervals
										choroScope);					// $scope of controller 
																		// (for access to alert service)
									choroScope.input.thisMap.range = tempRenderer.range;
									choroScope.input.thisMap.scale = tempRenderer.scale;
								}
							} 
							else {
								choroScope.input.thisMap = choroScale(
									choroScope.input.method,  		// Method name
									choroScope.domain,  			// Data
									ColorBrewerService.getColorbrewer(
										choroScope.input.currOption.name,
										choroScope.input.selectedN), 
									choroScope.input.checkboxInvert, // Flip 
									choroScope.mapID,				// Map
									oldRval,						// Old (previous) rval
									choroScope.input.selectedN,		// Intervals
									choroScope);					// $scope of controller 
																	// (for access to alert service)
							}
						}
					}
					catch(e) {
						if (choroScope.consoleError) { // Should always be in scope
							choroScope.consoleError("Error in renderSwatch(): " + JSON.stringify(e.message));
						}
						else {
							throw e;
						}
					}
					
					if (oldRval == undefined && 	// First run (init)
					    choroScope.consoleLog) {    // Should always be in scope
						choroScope.consoleLog("[rifs-util-choro.js] choroScope.input: " + 
							JSON.stringify(choroScope.input, null, 2));
					}
// Redo all scales							
					choroScope.input.thisMap.scale = d3.scaleThreshold()
						   .domain(choroScope.input.thisMap.breaks)
						   .range(choroScope.input.thisMap.range);	

					return choroScope.input.thisMap;
				}
				
				function checkuserMethods(map, studyType, userMethodName) {
					if (studyType == "Disease Mapping") {
						return true;
					}
					else if (studyType == "Risk Analysis") {
						if (userMethodName == "AtlasRelativeRisk") {
							return true;
						}
						else {
							return false;
						}
					}
					else {
						throw new Error("Invalid studyType: " + studyType + " for map: " + mapID + "; userMethodName: " + userMethodName);
					}	
				}
				
                return {
                    getMaps: function (i) {
                        return maps[i];
                    },
					getClassifications: function() {
						return classificationsList;
					},
                    getRenderFeatureMapping: function (scale, value, selected) {
                        return renderFeatureMapping(scale, value, selected);
                    },
                    getRenderFeatureViewer: function (scale, feature, value, selected) {
                        return renderFeatureViewer(scale, feature, value, selected);
                    },
                    getChoroScale: function (method, domain, rangeIn, flip, map, oldRval, intervals, choroScope) {
                        return choroScale(method, domain, rangeIn, flip, map, oldRval, intervals, choroScope);
                    },
                    getMakeLegend: function (thisMap, attr) {
						var legend=makeLegend(thisMap, attr);
						AlertService.consoleLog("getMakeLegend(): legend: " + JSON.stringify(legend, null, 1));
                        return legend;
                    },
					doRenderSwatch: function (
						bOnOpen /* Called on modal open */, 
						bCalc /* Secret field, always true */, 
						choroScope, 
						ColorBrewerService) {
						return renderSwatch(
							bOnOpen /* Called on modal open */, 
							bCalc /* Secret field, always true */, 
							choroScope, 
							ColorBrewerService);
					},
                    resetState: function (map) {
						if (map == undefined) {
							throw new Error("Unable to reesetState, map is undefined");
						}	
                        maps[map] = new symbology(map, choroScaleMethod);
                    },
					setType: function (map, studyType) {				
						var validColumnList=ViewerStateService.getValidColumnList(map, studyType);
					
						if (studyType == "Risk Analysis") {
							maps['diseasemap1'] = maps['viewermap'];
							maps['diseasemap2'] = maps['viewermap'];
						}
						
						if (parameters && parameters.userMethods) { // Add userMethods

							classificationsList=angular.copy(defaultClassificationsList);				
							for (var userMethodName in parameters.userMethods) {
								var userMethod=parameters.userMethods[userMethodName];
								if (ViewerStateService.getValidColumn(validColumnList, studyType, userMethodName.feature)) {	
									AlertService.consoleLog("classificationsList[" + JSON.stringify(userMethod) + "] for map: " + map + 
										"; studyType: " + studyType +
										"; add for validated feature: " + userMethodName.feature + "; " + userMethod.description);				
									classificationsList.push({
											id: userMethodName,
											label: userMethod.description
										});
								}
								else if (userMethodName.feature == undefined && checkuserMethods(map, studyType, userMethodName)) {
									AlertService.consoleLog("classificationsList[" + userMethodName + "] for map: " + map + 
										"; studyType: " + studyType +
										"; add for undefined feature: " + JSON.stringify(userMethod));							
									classificationsList.push({
											id: userMethodName,
											label: userMethod.description
										});
								}
							}
//							AlertService.consoleLog("New classificationsList for map: " + map + 
//								"; studyType: " + studyType +
//								"; list: " + JSON.stringify(classificationsList, null, 2));
						
						}
					}
                };
            }]);