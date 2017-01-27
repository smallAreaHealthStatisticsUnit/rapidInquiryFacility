/*
 * DIRECTIVE to save D3 charts to a PNG
 * See https://bl.ocks.org/Rokotyan/raw/0556f8facbaf344507cdc45dc3622177/
 */
/* global d3 */

angular.module("RIF")
        .directive('saved3Chart', function () {
            return {
                restrict: 'A',
                link: function (scope, element, attr) {

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
                            alert("You are using Internet Explorer:\nPlease use 'right click' then 'save picture as...' instead");
                            return;
                        }

                        opts = scope.$parent.optionsd3[attr.mapid];

                        var container;
                        if (opts.container === "rrchart") {
                            container = opts.container + attr.mapid;
                        } else {
                            container = attr.mapid;
                        }

                        if (document.getElementById(container) === null) {
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
        });