/*
 * SERVICE for UI-Grid helper functions
 */
angular.module("RIF")
        .factory('GridService',
                function () {
                    function matchRowNumber(visible, id) {
                        for (var i = 0; i < visible.length; i++) {
                            if (visible[i].entity.area_id === id) {
                                return i;
                            }
                        }
                    }
                    return {
                        getMatchRowNumber: function (visible, id) {
                            return matchRowNumber(visible, id);
                        }
                    };
                });