/* SERVICE to store state of comparison area modal
 * will be used eventually to load studies
 */
angular.module("RIF")
        .factory('CompAreaStateService',
                function () {
                    var s = {
                        polygonIDs: [],
                        selectAt: "Select at",
                        studyResolution: "Study resolution"
                    };
                    var defaults = JSON.parse(JSON.stringify(s));
                    return {
                        get_state: function () {
                            return s;
                        },
                        reset_state: function () {
                            s = defaults;
                        }
                    };
                });