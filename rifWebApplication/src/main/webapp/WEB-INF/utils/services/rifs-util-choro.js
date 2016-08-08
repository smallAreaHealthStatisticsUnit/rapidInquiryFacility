/* global d3, ss, L */

angular.module("RIF")
        .factory('ChoroService', ['ColorBrewerService',
            function (ColorBrewerService) {
                var features = [];
                var viewMap = {
                    brewerName: "LightGreen",
                    brewer: ["#9BCD9B"],
                    intervals: 1,
                    feature: "",
                    invert: false,
                    method: "quantile"
                };
                function renderFeature(feature, scale, attr) {
                    //selected
                    if (feature.properties._selected === 1) {
                        return "green";
                    }
                    //choropleth
                    if (scale && attr !== "") {
                        return scale(feature.properties[attr]);
                    } else {
                        return "#9BCD9B";
                    }
                }
                function choroScale(method, domain, rangeIn, flip) {
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
                            scale = d3.scale.quantile()
                                    .domain(domain)
                                    .range(range);
                            var breaks = scale.quantiles();
                            break;
                        case "quantize":
                            scale = d3.scale.quantize()
                                    .domain([mn, mx])
                                    .range(range);
                            var breaks = [];
                            var dom = scale.domain();
                            var l = (dom[1] - dom[0]) / scale.range().length;
                            var breaks = d3.range(0, scale.range().length).map(function (i) {
                                return i * l;
                            });
                            breaks.shift();
                            break;
                        case "jenks":
                            var breaks = ss.jenks(domain, range.length);
                            breaks.pop(); //remove max
                            breaks.shift(); //remove min
                            scale = d3.scale.threshold()
                                    .domain(breaks)
                                    .range(range);
                            break;
                        case "standardDeviation":
                            /*
                             * Implementation derived by ArcMap Stand. Deviation classification
                             * 5 intervals of which those around the mean are 1/2 the Standard Deviation
                             */
                            var sd = ss.sample_standard_deviation(domain);
                            var mean = d3.mean(domain);

                            var below_mean = mean - sd / 2;
                            var above_mean = mean + sd / 2;
                            var breaks = [];

                            for (i = 0; below_mean > mn && i < 2; i++) {
                                breaks.push(below_mean);
                                below_mean = below_mean - sd;
                            }
                            for (i = 0; above_mean < mx && i < 2; i++) {
                                breaks.push(above_mean);
                                above_mean = above_mean + sd;
                            }
                            breaks.sort(d3.ascending);

                            //dynamic scale range as number of classes unknown
                            range = ColorBrewerService.getColorbrewer(viewMap.brewerName, breaks.length + 1);

                            scale = d3.scale.threshold()
                                    .domain(breaks)
                                    .range(range);
                            break;
                        case "logarithmic":
                            //TODO: check, not implemented by Fred
                            scale = d3.scale.log()
                                    .domain([mn, mx])
                                    .range(range);
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
                        div.innerHTML += '<h4>' + attr + '</h4>';
                        for (var i = 0; i < thisMap.range.length; i++) {
                            div.innerHTML += '<i style="background:' + thisMap.range[i] + '"></i>';
                            if (i === 0) { //first break
                                div.innerHTML += '<span>' + thisMap.mn.toFixed(2) + '&ndash;' + thisMap.breaks[i].toFixed(2) + '</span><br>';
                            } else if (i === thisMap.range.length - 1) { //last break
                                div.innerHTML += '<span>' + thisMap.breaks[i - 1].toFixed(2) + '&ndash;' + thisMap.mx.toFixed(2) + '</span>';
                            } else {
                                div.innerHTML += '<span>' + thisMap.breaks[i - 1].toFixed(2) + '&ndash;' + thisMap.breaks[i].toFixed(2) + '</span><br>';
                            }
                        }
                        return div;
                    });
                }
                return {
                    setFeaturesToMap: function (attrs) {
                        features = attrs;
                    },
                    getFeaturesToMap: function () {
                        return features;
                    },
                    getViewMap: function () {
                        return viewMap;
                    },
                    getRenderFeature: function (layer, scale, attr) {
                        return renderFeature(layer, scale, attr);
                    },
                    getChoroScale: function (method, domain, rangeIn, flip) {
                        return choroScale(method, domain, rangeIn, flip);
                    },
                    getMakeLegend: function (thisMap, attr) {
                        return makeLegend(thisMap, attr);
                    }
                };
            }]);