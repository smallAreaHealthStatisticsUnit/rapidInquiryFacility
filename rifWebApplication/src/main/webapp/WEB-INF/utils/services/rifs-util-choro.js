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
        .factory('ChoroService', ['ColorBrewerService',
            function (ColorBrewerService) {
                
                //a default symbology
                function symbology(defaultMethod, defaultFeature, defaultIntervals) {
                    this.features = [];
                    this.brewerName = "Constant";
                    this.intervals = defaultIntervals;
                    this.feature = defaultFeature;
                    this.invert = false;
                    this.method = defaultMethod;
                    this.renderer = {
                        scale: null,
                        breaks: [],
                        range: ["#9BCD9B"],
                        mn: null,
                        mx: null
                    };
					
					if (defaultMethod == "quantile") {
						this.brewerName = "PuOr";
						this.invert = true;
					}

                    this.init = false;
                }
                var maps = {
                    'viewermap': new symbology("quantile", "relative_risk", 9),
                    'diseasemap1': new symbology("quantile", "smothed_smr", 9),
                    'diseasemap2': new symbology("AtlasProbability", "posterior_probability", 3) //default for 2nd disease map is probability
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

                function choroScale(method, domain, rangeIn, flip, map) {
                    var scale;
                    var mx = Math.max.apply(Math, domain);
                    var mn = Math.min.apply(Math, domain);
                    //flip the colour ramp
                    var range = [];
                    if (!flip) {
                        range = angular.copy(rangeIn);
                    } else {
                        range = angular.copy(rangeIn).reverse();
                    }

                    //find the breaks
                    switch (method) {
                        case "quantile":
                            scale = d3.scaleQuantile()
                                    .domain(domain)
                                    .range(range);
                            var breaks = scale.quantiles();
                            break;
                        case "quantize": // Equal Interval
                            scale = d3.scaleQuantize()
                                    .domain([mn, mx])
                                    .range(range);
                            var breaks = [];
                            var l = (mx - mn) / scale.range().length;
                            for (var i = 0; i < range.length; i++) {
                                breaks.push(mn + (i * l));
                            }
                            breaks.shift();
                            break;
                        case "jenks":
                            var breaks = ss.jenks(domain, range.length);
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
                                var breaks = scale.quantiles();
                                break;
                            }
                            var sd = ss.sample_standard_deviation(domain);
                            var mean = d3.mean(domain);
                            var below_mean = mean - sd / 2;
                            var above_mean = mean + sd / 2;
                            var breaks = [];
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
                        case "AtlasRelativeRisk":
                            //RR scale as used in Health Atlas
                            var tmp;
                            var invalidScales = ["Constant", "Dark2", "Accent", "Pastel2", "Set2"];
                            if (invalidScales.indexOf(maps[map].brewerName) !== -1) {
                                tmp = ColorBrewerService.getColorbrewer("PuOr", 9).reverse();
                                maps[map].brewerName = "PuOr";
                            } else {
                                tmp = ColorBrewerService.getColorbrewer(maps[map].brewerName, 9);
                            }
                            if (!flip) {
                                range = angular.copy(tmp);
                            } else {
                                range = angular.copy(tmp).reverse();
                            }
                            var breaks = [0.68, 0.76, 0.86, 0.96, 1.07, 1.2, 1.35, 1.51];
                            scale = d3.scaleThreshold()
                                    .domain(breaks)
                                    .range(range);
                            mn = -Infinity;
                            mx = Infinity;
                            break;
                        case "AtlasProbability":
                            //Probability scale as used in Health Atlas
                            var tmp;
                            var invalidScales = ["Constant"];
                            if (invalidScales.indexOf(maps[map].brewerName) !== -1) {
                                tmp = angular.copy(ColorBrewerService.getColorbrewer("RdYlGn", 3)).reverse();
                                maps[map].brewerName = "RdYlGn";
                            } else {
                                tmp = ColorBrewerService.getColorbrewer(maps[map].brewerName, 3);
                            }
                            if (!flip) {
                                range = angular.copy(tmp);
                            } else {
                                range = angular.copy(tmp).reverse();
                            }
                            var breaks = [0.20, 0.81];
                            scale = d3.scaleThreshold()
                                    .domain(breaks)
                                    .range(range);
                            mn = 0;
                            mx = 1;
                            break;
                        case "logarithmic":
                            //Check, not implemented by Fred         
                            break;
                    }
                    return {
                        scale: scale,
                        breaks: breaks,
                        range: range,
                        mn: mn,
                        mx: mx
                    };
                }

                function makeLegend(thisMap, attr) {
                    return (function () {
                        var div = L.DomUtil.create('div', 'info legend');
                        div.innerHTML += '<h4>' + attr.toUpperCase().replace("_", " ") + '</h4>';
                        if (!angular.isUndefined(thisMap.range)) {
                            for (var i = thisMap.range.length - 1; i >= 0; i--) {
                                div.innerHTML += '<i style="background:' + thisMap.range[i] + '"></i>';
                                if (i === 0) { //first break
                                    div.innerHTML += '<span>' + '<' + thisMap.breaks[i].toFixed(2) + '</span>';
                                } else if (i === thisMap.range.length - 1) { //last break
                                    div.innerHTML += '<span>' + '&ge;' + thisMap.breaks[i - 1].toFixed(2) + '</span><br>';
                                } else {
                                    div.innerHTML += '<span>' + thisMap.breaks[i - 1].toFixed(2) + ' - <' + thisMap.breaks[i].toFixed(2) + '</span><br>';
                                }
                            }
                        }
                        return div;
                    });
                }
				
				function renderSwatch(bOnOpen /* Called on modal open */, bCalc /* Secret field, always true */, choroScope, ColorBrewerService) {
                //ensure that the colour scheme allows the selected number of classes
					var n = angular.copy(choroScope.input.selectedN);
					choroScope.input.intervalRange = ColorBrewerService.getSchemeIntervals(choroScope.input.currOption.name);
					if (choroScope.input.selectedN > Math.max.apply(Math, choroScope.input.intervalRange)) {
						choroScope.input.selectedN = Math.max.apply(Math, choroScope.input.intervalRange);
					} else if (choroScope.input.selectedN < Math.min.apply(Math, choroScope.input.intervalRange)) {
						choroScope.input.selectedN = Math.min.apply(Math, choroScope.input.intervalRange);
					}

					//get the domain 
					choroScope.domain.length = 0;
					for (var i = 0; i < choroScope.tableData[choroScope.mapID].length; i++) {
						choroScope.domain.push(Number(choroScope.tableData[choroScope.mapID][i][choroScope.input.selectedFeature]));
					}

					//save the selected brewer
					maps[choroScope.mapID].brewerName = choroScope.input.currOption.name;

					if (bOnOpen) {
						//if called on modal open
						if (!maps[choroScope.mapID].init) {
							//initialise basic renderer
							maps[choroScope.mapID].init = true;
							choroScope.input.thisMap = choroScale(choroScope.input.method, choroScope.domain, ColorBrewerService.getColorbrewer(choroScope.input.currOption.name,
									choroScope.input.selectedN), choroScope.input.checkboxInvert, choroScope.mapID);
							maps[choroScope.mapID].renderer = choroScope.input.thisMap;
						} else {
							//restore previous renderer
							choroScope.input.thisMap = maps[choroScope.mapID].renderer;
						}
					} else {
						//update current renderer
						if (!bCalc) {
							if (n !== choroScope.input.selectedN) {
								//reset as class number requested not possible
								choroScope.input.thisMap = choroScale(choroScope.input.method, choroScope.domain, ColorBrewerService.getColorbrewer(choroScope.input.currOption.name,
										choroScope.input.selectedN), choroScope.input.checkboxInvert, choroScope.mapID);
							} else {
								var tempRenderer = choroScale(choroScope.input.method, choroScope.domain, ColorBrewerService.getColorbrewer(choroScope.input.currOption.name,
										choroScope.input.selectedN), choroScope.input.checkboxInvert, choroScope.mapID);
								choroScope.input.thisMap.range = tempRenderer.range;
							}
						} else {
							choroScope.input.thisMap = choroScale(choroScope.input.method, choroScope.domain, ColorBrewerService.getColorbrewer(choroScope.input.currOption.name,
									choroScope.input.selectedN), choroScope.input.checkboxInvert, choroScope.mapID);
						}
					}
									
				}
				
                return {
                    getMaps: function (i) {
                        return maps[i];
                    },
                    getRenderFeatureMapping: function (scale, value, selected) {
                        return renderFeatureMapping(scale, value, selected);
                    },
                    getRenderFeatureViewer: function (scale, feature, value, selected) {
                        return renderFeatureViewer(scale, feature, value, selected);
                    },
                    getChoroScale: function (method, domain, rangeIn, flip, map) {
                        return choroScale(method, domain, rangeIn, flip, map);
                    },
                    getMakeLegend: function (thisMap, attr) {
                        return makeLegend(thisMap, attr);
                    },
					doRenderSwatch: function (bOnOpen /* Called on modal open */, bCalc /* Secret field, always true */, choroScope, ColorBrewerService) {
						return renderSwatch(bOnOpen /* Called on modal open */, bCalc /* Secret field, always true */, choroScope, ColorBrewerService);
					},
                    //TODO: not used, add reset button
                    resetState: function (map) {
                        maps[map] = new symbology("quantile", "smothed_smr", 9);
                    }
                };
            }]);