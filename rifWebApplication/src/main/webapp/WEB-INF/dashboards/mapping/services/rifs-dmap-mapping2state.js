/* 
 * SERVICE to store state of mapping tab
 */
angular.module("RIF")
        .factory('MappingStateService',
                function () {
                    var s = {
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
                        rr1ContainerH: 0,
                        rr1ContainerV: 0,
                        rr2ContainerH: 0,
                        rr2ContainerV: 0,
                        vSplit1: 25,
                        hSplit1: 75,
                        hSplit2: 75
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