/* SERVICE to store state of viewer tab
 * 
 */
angular.module("RIF")
        .factory('ViewerStateService',
                function () {
                    var s = {
                        zoomLevel: -1,
                        view: [0,0]
                    };
                    var defaults = JSON.parse(JSON.stringify(s));
                    return {
                        getState: function () {
                            return s;
                        },
                        resetState: function () {
                            s = defaults;
                        }
                    };
                });