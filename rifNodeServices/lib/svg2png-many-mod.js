'use strict';

Object.defineProperty(exports, "__esModule", {
    value: true
});

var _slicedToArray = function () { function sliceIterator(arr, i) { var _arr = []; var _n = true; var _d = false; var _e = undefined; try { for (var _i = arr[Symbol.iterator](), _s; !(_n = (_s = _i.next()).done); _n = true) { _arr.push(_s.value); if (i && _arr.length === i) break; } } catch (err) { _d = true; _e = err; } finally { try { if (!_n && _i["return"]) _i["return"](); } finally { if (_d) throw _e; } } return _arr; } return function (arr, i) { if (Array.isArray(arr)) { return arr; } else if (Symbol.iterator in Object(arr)) { return sliceIterator(arr, i); } else { throw new TypeError("Invalid attempt to destructure non-iterable instance"); } }; }();

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol ? "symbol" : typeof obj; };

exports.svg2PngFiles = svg2PngFiles;
exports.svg2PngDir = svg2PngDir;

var _phantom = require('phantom');

var _phantom2 = _interopRequireDefault(_phantom);

var _fs = require('fs');

var _fs2 = _interopRequireDefault(_fs);

var _path = require('path');

var _path2 = _interopRequireDefault(_path);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

/**
 * @typedef {object} Sizes
 * @prop {number} [height]
 * @prop {number} [width]
 */

/**
 * @typedef {number} ParallelPages How many pages can be opened simultaneously in PhantomJS
 */

/**
 * All matched files will be converted
 */
var SVG_REGEX = /\.svg$/i;

/**
 * Default value
 * @type {ParallelPages} 
 */
var PARALLEL_PAGES = 20;

var DEBUG = (typeof v8debug === 'undefined' ? 'undefined' : _typeof(v8debug)) === 'object' || process.env.DEBUG === 'true' || process.env.VERBOSE === 'true';

exports.default = svg2PngDir;

/**
 * @param fileMap {object.<string, string>} key - src file path, value - dst file path
 * @param {Sizes} [size]
 * @param {ParallelPages} [pages]
 */

function svg2PngFiles(fileMap) {
    var size = arguments.length <= 1 || arguments[1] === undefined ? {} : arguments[1];
    var pages = arguments.length <= 2 || arguments[2] === undefined ? PARALLEL_PAGES : arguments[2];
    var clipRectObj = arguments.length <= 3 || arguments[3] === undefined ? {} : arguments[3];
	
    var phantomInstance = void 0;
    var closePhantom = function closePhantom() {
        if (phantomInstance) {
            log('close phantom instance');
            phantomInstance.exit();
        }
    };
    return _phantom2.default.create().then(function (instance) {
        log('phantom instance created');
        phantomInstance = instance;
        return convertMany(instance, fileMap, size, pages, clipRectObj);
    }).then(function (results) {
        closePhantom();
        return results;
    }, function (errors) {
        closePhantom();
        return Promise.reject(errors);
    });
}

/**
 * All svg files from srcDir will be converted with png into dstDir with the same name
 * @param {string} srcDir
 * @param {string} dstDir
 * @param {Sizes} [size]
 * @param {ParallelPages} [pages]
 */
function svg2PngDir(srcDir, dstDir) {
    var size = arguments.length <= 2 || arguments[2] === undefined ? {} : arguments[2];
    var pages = arguments.length <= 3 || arguments[3] === undefined ? PARALLEL_PAGES : arguments[3];

    return new Promise(function (resolve, reject) {
        _fs2.default.readdir(srcDir, function (error, files) {
            if (error) {
                return reject(error);
            }
            files = files.filter(function (file) {
                return SVG_REGEX.test(file);
            });
            var fileMap = {};
            files.forEach(function (file) {
                var srcFile = _path2.default.join(srcDir, file);
                var dstFile = _path2.default.join(dstDir, _path2.default.parse(file).name + '.png');
                fileMap[srcFile] = dstFile;
            });
            resolve(fileMap);
        });
    }).then(function (fileMap) {
        return svg2PngFiles(fileMap, size, pages);
    });
}

/**
 * @param {object} instance PhantomJS instance
 * @param {object.<string, string>} fileMap key - src file path, value - dst file path
 * @param {Sizes} size
 * @param {ParallelPages} pages
 * @returns {Promise<Array<*>,Array<*>>} resolved with list of results, rejected with list of errors
 */
function convertMany(instance, fileMap, size, pages, clipRectObj) {
    return new Promise(function (resolveAll, rejectAll) {
        var results = [];
        var errors = [];
        var poolCapacity = pages;
        var restWorkers = Object.keys(fileMap).map(function (srcPath) {
            return function () {
                return convert(instance, srcPath, size, clipRectObj[srcPath]).then(function (buffer) {
                    return saveBuffer(fileMap[srcPath], buffer);
                });
            };
        });
        log(restWorkers.length + ' files will be processed');
        var startWorker = function startWorker(worker) {
            return Promise.resolve(worker()).then(function (result) {
                results.push(result);
            }, function (error) {
                errors.push(error);
            });
        };
        var processNext = function processNext() {
            if (restWorkers.length > 0) {
                var nextWorker = restWorkers.pop();
                startWorker(nextWorker).then(processNext);
            } else if (errors.length + results.length >= Object.keys(fileMap).length) {
                if (errors.length > 0) {
                    rejectAll(errors);
                } else {
                    resolveAll(results);
                }
            }
        };
        restWorkers.splice(0, poolCapacity).forEach(function (worker) {
            startWorker(worker).then(processNext);
        });
    });
}

function saveBuffer(dstPath, buffer) {
    log(dstPath + ' will be saved ');
    return new Promise(function (resolve, reject) {
        _fs2.default.writeFile(dstPath, buffer, function (error) {
            if (error) {
                log(dstPath + ' saved with error');
                reject(error);
            }
            log(dstPath + ' saved successfully');
            resolve(dstPath);
        });
    });
}

/**
 * @param {object} instance Phantom instance
 * @param {string} srcPath
 * @param {Sizes} [size]
 * @param { left, top, width, height} [clipRect]
 * @returns {Promise<Buffer>} resolved with image data
 */
function convert(instance, srcPath, size, clipRect) {
    return Promise.all([instance.createPage(), fileToBase64(srcPath)]).then(function (_ref) {
        var _ref2 = _slicedToArray(_ref, 2);

        var page = _ref2[0];
        var pageContent = _ref2[1];

        var closePage = function closePage() {
            if (page) {
                page.close();
            }
        };
        return page.open(pageContent).then(function (status) {
            if (status !== "success") {
                var errMsg = 'File ' + srcPath + ' has been opened with status ' + status;
                logError(errMsg);
                throw new Error(errMsg);
            }
            if (DEBUG) {
                page.property('onConsoleMessage', function (msg) {
                    return console.log(msg);
                });
            }
            size = size || {};
            console.error(srcPath + ' opened; size: ' + JSON.stringify(size) + '; clipRect: ' + JSON.stringify(clipRect));
            return page.evaluate(setSVGDimensions, size || {}).then(checkEvalError).then(function () {
                return page.evaluate(getSVGDimensions);
            }).then(checkEvalError).then(function (dimensions) {
                return page.evaluate(setSVGDimensions, dimensions);
            }).then(checkEvalError).then(function (dimensions) {
				console.error('dimensions: ' + srcPath + '; dimensions: ' + JSON.stringify(dimensions));
                return page.property('viewportSize', dimensions);
            })/*.then(checkEvalError).then(function () { // Clipping 
				var nclipRect={top: clipRect.top, left: clipRect.left, width: size.width, height: size.height};
				console.error('clipRect: ' + srcPath + '; clipRect: ' + JSON.stringify(nclipRect));
                return page.property('clipRect', nclipRect);
            }) */;
        }).then(function () {
            return page.renderBase64("PNG");
        }).then(function (imageBase64) {
            console.error('Rendered ' + srcPath + ": " + imageBase64.length + " bytes");
            return new Buffer(imageBase64, 'base64');
        }).then(function (imageData) {
            log(srcPath + ' converted successfully');
            closePage();
            return imageData;
        }, function (error) {
            console.log('Ooops');
            closePage();
            return Promise.reject(error);
        });
    });
}

/**
 * PhantomJS node brige cannot reject promises by exception,
 * it is always succeed. This extracts error from result and returns rejected promise,
 * or returns evaluate result, if no error.
 */
function checkEvalError(result) {
    if (result && result.error) {
        return Promise.reject(result.error);
    }
    return result;
}

/**
 * @param {string} filePath
 * @returns {Promise.<string>} resolved with base64 file data
 */
function fileToBase64(filePath) {
    var dataPrefix = 'data:image/svg+xml;base64,';
    return new Promise(function (resolve, reject) {
        _fs2.default.readFile(filePath, function (error, data) {
            if (error) {
                return reject(error);
            }
            var base64Data = new Buffer(data).toString('base64');
            resolve(dataPrefix + base64Data);
        });
    });
}

function log() {
    DEBUG && console.log.apply(console, arguments);
}

function logError() {
    DEBUG && console.error.apply(console, arguments);
}

/**
 * Get actual sizes of root elem
 * Interpreted by PhantomJS
 * @returns {Sizes|null}
 */
function getSVGDimensions() {
    console.log('Get page sizes');
    /* global document: true */
    try {
        var el = document.documentElement;

        var widthIsPercent = /%\s*$/.test(el.getAttribute("width") || ""); // Phantom doesn't have endsWith
        var heightIsPercent = /%\s*$/.test(el.getAttribute("height") || "");
        var width = !widthIsPercent && parseFloat(el.getAttribute("width"));
        var height = !heightIsPercent && parseFloat(el.getAttribute("height"));

        if (width && height) {
            return { width: width, height: height };
        }

        var viewBoxWidth = el.viewBox.animVal.width;
        var viewBoxHeight = el.viewBox.animVal.height;

        if (width && viewBoxHeight) {
            return { width: width, height: width * viewBoxHeight / viewBoxWidth };
        }

        if (height && viewBoxWidth) {
            return { width: height * viewBoxWidth / viewBoxHeight, height: height };
        }

        return null;
    } catch (error) {
        return { error: error };
    }
}

/**
 * Set sizes to root elem
 * Interpreted by PhantomJS
 * @param {Sizes} sizes
 * @returns {Sizes} same as size param
 */
function setSVGDimensions(sizes) {
    console.log('Set page sizes', JSON.stringify(sizes));
    try {
        var height = sizes.height;
        var width = sizes.width;

        /* global document: true */
        if (!width && !height) {
            return sizes;
        }

        var el = document.documentElement;

        if (!!width) {
            el.setAttribute("width", width + "px");
        } else {

            el.removeAttribute("width");
        }

        if (!!height) {
            el.setAttribute("height", height + "px");
        } else {
            el.removeAttribute("height");
        }
        return sizes;
    } catch (error) {
        return { error: error };
    }
}