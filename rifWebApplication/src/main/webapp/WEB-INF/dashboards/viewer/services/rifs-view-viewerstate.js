/* 
 * SERVICE to store state of viewer tab
 */
angular.module("RIF")
        .factory('ViewerStateService',
                function () {
                    //These are the relevant columns to display from the results table
                    var validColumns = ["area_id", "band_id", "observed", "expected", "population", "adjusted", "inv_id",
                        "posterior_probability",
                        "lower95", "upper95", "relative_risk",
                        "smoothed_smr", "smoothed_smr_lower95", "smoothed_smr_upper95",
                        "_selected"];
                    var s = {
                        initial: true,
                        center: {
                            'viewermap': {'zoom': 1, 'lng': 0, 'lat': 0}
                        },
                        area_id: {
                            'viewermap': null
                        },
                        study: {
                            'viewermap': {'study_id': null, 'name': null}
                        },
                        sex: {
                            'viewermap': null
                        },
                        selected: {
                            'viewermap': []
                        },
                        transparency: {
                            'viewermap': 0.7
                        },
                        vSplit1: 33,
                        hSplit1: 40,
                        hSplit2: 60
                    };
                    var defaults = angular.copy(JSON.parse(JSON.stringify(s)));
                    return {
                        getValidColumn: function (header) {
                            if (validColumns.indexOf(header) !== -1) {
                                return true;
                            } else {
                                return false;
                            }
                        },
                        getState: function () {
                            return s;
                        },
                        resetState: function () {
                            s = angular.copy(defaults);
                        }
                    };
                });