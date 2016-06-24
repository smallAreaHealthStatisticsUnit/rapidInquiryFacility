/* SERVICE to store state of study area modal
 * will be used eventually to load studies
 * and also write to the JSON model object
 */
angular.module("RIF")
        .factory('StudyAreaStateService',
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