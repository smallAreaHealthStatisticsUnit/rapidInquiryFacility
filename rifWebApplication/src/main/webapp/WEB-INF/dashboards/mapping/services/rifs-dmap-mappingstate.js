/* 
 * SERVICE to store state of mapping tab
 */
angular.module("RIF")
        .factory('MappingStateService',
                function () {
                    var s = {
                        extentLock: true,
                        selectionLock: false,
                        center: {
                            'diseasemap1': {'zoom': 1, 'lng': 0, 'lat': 0},
                            'diseasemap2': {'zoom': 1, 'lng': 0, 'lat': 0}
                        },
                        area_id: {
                            'diseasemap1': null,
                            'diseasemap2': null
                        },
                        selected: {
                            'diseasemap1': null,
                            'diseasemap2': null
                        },
                        brushStartLoc: {
                            'diseasemap1': null,
                            'diseasemap2': null
                        },
                        brushEndLoc: {
                            'diseasemap1': null,
                            'diseasemap2': null
                        },
                        transparency: {
                            'diseasemap1': 0.7,
                            'diseasemap2': 0.7
                        },
                        vSplit1: 50,
                        hSplit1: 60,
                        hSplit2: 60
                    };
                    var defaults = angular.copy(JSON.parse(JSON.stringify(s)));
                    return {
                        getState: function () {
                            return s;
                        },
                        resetState: function () {
                            s = angular.copy(defaults);
                        }
                    };
                });