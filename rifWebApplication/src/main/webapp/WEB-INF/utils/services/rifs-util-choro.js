/*
 * SERVICE to render choropleth maps using Colorbrewer
 */

/* global d3, ss, L, Infinity */

angular.module("RIF")
        .factory('ChoroService', ['ColorBrewerService',
            function (ColorBrewerService) {
                var maps = [
                    {
                        //Viewer map: index 0
                        map: "viewermap",
                        features: [],
                        brewerName: "LightGreen",
                        intervals: 1,
                        feature: "relative_risk",
                        invert: false,
                        method: "quantile",
                        renderer:
                                {
                                    scale: null,
                                    breaks: null,
                                    range: ["#9BCD9B"],
                                    mn: null,
                                    mx: null
                                },
                        init: false
                    },
                    {
                        //Disease left map: index 1
                        map: "diseasemap1",
                        features: [],
                        brewerName: "LightGreen",
                        intervals: 1,
                        feature: "",
                        invert: false,
                        method: "quantile",
                        renderer:
                                {
                                    scale: null,
                                    breaks: null,
                                    range: ["#9BCD9B"],
                                    mn: null,
                                    mx: null
                                },
                        init: false
                    },
                    {
                        //Disease right map: index 2
                        map: "diseasemap2",
                        features: [],
                        brewerName: "LightGreen",
                        intervals: 1,
                        feature: "",
                        invert: false,
                        method: "quantile",
                        renderer:
                                {
                                    scale: null,
                                    breaks: null,
                                    range: ["#9BCD9B"],
                                    mn: null,
                                    mx: null
                                },
                        init: false
                    }
                ];
                //used in map
                function renderFeature(scale, attr, selected) {
                    //returns fill colour
                    //selected
                    if (selected && !angular.isUndefined(attr)) {
                        return "green";
                    }
                    //choropleth
                    if (scale && !angular.isUndefined(attr)) {
                        return scale(attr);
                    } else if (angular.isUndefined(attr)) {
                        return "transparent";
                    } else {
                        return "#9BCD9B";
                    }
                }

                //used in disease mapping
                function renderFeature2(feature, value, scale, attr, selection) {
                    //returns [fill colour, border colour, border width]
                    //selected (a single polygon)
                    if (selection === feature.properties.area_id) {
                        if (scale && attr !== "") {
                            return [scale(value), "green", 5];
                        } else {
                            return ["lightgreen", "green", 5];
                        }
                    }
                    //choropleth
                    if (scale && !angular.isUndefined(attr)) {
                        return [scale(value), "gray", 1];
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
                        case "quantize":
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
                            if (maps[map].brewerName === "LightGreen") {
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
                        case "equalSize":
                            //Equal number of points per group
                            var n = Math.floor(domain.length / range.length);
                            domain.sort(d3.ascending);
                            var breaks = [];
                            for (var i = 1; i <= range.length - 1; i++) {
                                breaks.push(domain[i * n]);
                            }
                            scale = d3.scaleThreshold()
                                    .domain(breaks)
                                    .range(range);
                            break;
                        case "AtlasRelativeRisk":
                            //RR scale as used in Health Atlas
                            var tmp;
                            var invalidScales = ["LightGreen", "Dark2", "Accent", "Pastel2", "Set2"];
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
                            var invalidScales = ["LightGreen"];
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
                return {
                    getMaps: function (i) {
                        return maps[i];
                    },
                    getRenderFeature: function (layer, scale, attr) {
                        return renderFeature(layer, scale, attr);
                    },
                    getRenderFeature2: function (feature, layer, scale, attr, selected) {
                        return renderFeature2(feature, layer, scale, attr, selected);
                    },
                    getChoroScale: function (method, domain, rangeIn, flip, map) {
                        return choroScale(method, domain, rangeIn, flip, map);
                    },
                    getMakeLegend: function (thisMap, attr) {
                        return makeLegend(thisMap, attr);
                    }
                };
            }]);