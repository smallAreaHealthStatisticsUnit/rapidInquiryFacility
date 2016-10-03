/* SERVICE to store state of mapping tab
 * 
 */
angular.module("RIF")
        .factory('MappingStateService',
                function () {
                    var s = {
                        zoomLevel: -1,
                        view: [0, 0],
                        vSplit1: 25,
                        hSplit1: 75
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