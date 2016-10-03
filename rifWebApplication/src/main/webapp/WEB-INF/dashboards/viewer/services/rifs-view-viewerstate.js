/* SERVICE to store state of viewer tab
 * 
 */
angular.module("RIF")
        .factory('ViewerStateService',
                function () {
                    var s = {
                        zoomLevel: -1,
                        view: [0,0],
                        vSplit1: 33,
                        hSplit1: 40,
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