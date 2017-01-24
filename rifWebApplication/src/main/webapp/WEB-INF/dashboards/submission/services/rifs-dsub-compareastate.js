/* 
 * SERVICE to store state of comparison area modal
 */
angular.module("RIF")
        .factory('CompAreaStateService',
                function () {
                    var s = {
                        polygonIDs: [],
                        selectAt: "",
                        studyResolution: "",
                        center: {'zoom': 1, 'lng': 0, 'lat': 0},
                        transparency: 0.7,
                        geography: ""
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