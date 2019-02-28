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
 * DIRECTIVE to save D3 charts to a PNG
 */
/* global d3 */

angular.module("RIF")
        .directive('saved3Chart', ['AlertService', 
        function (AlertService) {
            return {
                restrict: 'A',
                link: function (scope, element, attr) {
                //See https://bl.ocks.org/Rokotyan/raw/0556f8facbaf344507cdc45dc3622177/
                
                    //export options set in scope
                    var opts;

                    function save(dataBlob, filesize) {
                        saveAs(dataBlob, opts.filename); // FileSaver.js function
                    }

                    function getSVGString(svgNode) {
                        svgNode.setAttribute('xlink', 'http://www.w3.org/1999/xlink');
                        var cssStyleText = getCSSStyles(svgNode);
                        appendCSS(cssStyleText, svgNode);

                        var serializer = new XMLSerializer();
                        var svgString = serializer.serializeToString(svgNode);
                        svgString = svgString.replace(/(\w+)?:?xlink=/g, 'xmlns:xlink='); // Fix root xlink without namespace
                        svgString = svgString.replace(/NS\d+:href/g, 'xlink:href'); // Safari NS namespace fix

                        return svgString;

                        function getCSSStyles(parentElement) {
                            var selectorTextArr = [];

                            // Add Parent element Id and Classes to the list
                            selectorTextArr.push('#' + parentElement.id);
                            for (var c = 0; c < parentElement.classList.length; c++) //TODO: ClassList on IE? http://stackoverflow.com/questions/8098406/code-with-classlist-does-not-work-in-ie
                                if (!contains('.' + parentElement.classList[c], selectorTextArr))
                                    selectorTextArr.push('.' + parentElement.classList[c]);

                            // Add Children element Ids and Classes to the list
                            var nodes = parentElement.getElementsByTagName("*");
                            for (var i = 0; i < nodes.length; i++) {
                                var id = nodes[i].id;
                                if (!contains('#' + id, selectorTextArr))
                                    selectorTextArr.push('#' + id);

                                var classes = nodes[i].classList;
                                for (var c = 0; c < classes.length; c++)
                                    if (!contains('.' + classes[c], selectorTextArr))
                                        selectorTextArr.push('.' + classes[c]);
                            }

                            // Extract CSS Rules
                            var extractedCSSText = "";

                            for (var i = 0; i < document.styleSheets.length; i++) {
                                var s = document.styleSheets[i];

                                try {
                                    if (!s.cssRules)
                                        continue;
                                } catch (e) {
                                    if (e.name !== 'SecurityError')
                                        throw e; // for Firefox
                                    continue;
                                }

                                var cssRules = s.cssRules;
                                for (var r = 0; r < cssRules.length; r++) {
                                    if (contains(cssRules[r].selectorText, selectorTextArr))
                                        extractedCSSText += cssRules[r].cssText;
                                }
                            }

                            return extractedCSSText;

                            function contains(str, arr) {
                                return arr.indexOf(str) === -1 ? false : true;
                            }
                        }

                        function appendCSS(cssText, element) {
                            var styleElement = document.createElement("style");
                            styleElement.setAttribute("type", "text/css");
                            styleElement.innerHTML = cssText;
                            var refNode = element.hasChildNodes() ? element.children[0] : null;
                            element.insertBefore(styleElement, refNode);
                        }
                    }

                    function svgString2Image(svgString, width, height, format, callback) {
                        var format = format ? format : 'png';
                        var imgsrc = 'data:image/svg+xml;base64,' + btoa(unescape(encodeURIComponent(svgString))); // Convert SVG string to dataurl
                        var canvas = document.createElement("canvas");
                        var context = canvas.getContext("2d");

                        canvas.width = width;
                        canvas.height = height;

                        var image = new Image;
                        image.onload = function () {
                            context.clearRect(0, 0, width, height);
                            context.drawImage(image, 0, 0, width, height);

                            canvas.toBlob(function (blob) {
                                var filesize = Math.round(blob.length / 1024) + ' KB';
                                if (callback)
                                    callback(blob, filesize);
                            });
                        };

                        image.src = imgsrc;
                    }


                    element.on('click', function (event) {
                        //check if using IE or EDGE
                        //http://stackoverflow.com/questions/31757852/how-can-i-detect-internet-explorer-ie-and-microsoft-edge-using-javascript
                        if (/MSIE 10/i.test(navigator.userAgent) ||
                                /MSIE 9/i.test(navigator.userAgent) ||
                                /rv:11.0/i.test(navigator.userAgent) ||
                                /Edge\/\d./i.test(navigator.userAgent)) {
                            AlertService.showWarning("You are using Internet Explorer:\nPlease use 'right click' then 'save picture as...' instead");
                            return;
                        }

                        opts = scope.$parent.optionsd3[attr.mapid];

                        var container;
                        if (angular.isUndefined(opts)) {
                            AlertService.consoleError("[rifd-util-savechart.js] optionsd3: " + 
                                JSON.stringify(scope.$parent.optionsd3, 0, 1));
                            AlertService.showError("Unable to get D3 options for: " + attr.mapid + 
                                " from parent window for save");
                            return;
                        }
                        else if (attr.mapid === "riskGraph" || attr.mapid === "riskGraph2" || 
                                 attr.mapid === "riskGraph3" || attr.mapid === "riskGraph4") {
                            container = opts.container;
                        }
                        else if (opts.container === "rrchart") {
                            container = opts.container + attr.mapid;
                        } 
                        else {
                            container = attr.mapid;
                        }

                        if (document.getElementById(container) === null) {
                            AlertService.consoleError("[rifd-util-savechart.js] optionsd3: " + 
                                JSON.stringify(scope.$parent.optionsd3, 0, 1));
                            AlertService.showError("Unable to find D3 container: " + container + " for save");
                            return;
                        } else {
                            var svgString = getSVGString(d3.select("#" + container)
                                    .attr("version", 1.1)
                                    .attr("xmlns", "http://www.w3.org/2000/svg")
                                    .node());

                            var pngWidth = d3.select(opts.element).node().getBoundingClientRect().width * 3;
                            var pngHeight = d3.select(opts.element).node().getBoundingClientRect().height * 3;

                            svgString2Image(svgString, pngWidth, pngHeight, 'png', save);
                        }
                    });
                }
            };
        }]);