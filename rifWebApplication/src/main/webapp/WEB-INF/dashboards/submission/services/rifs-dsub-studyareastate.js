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
                        studyResolution: "Study resolution",
                        zoomLevel: -1,
                        view: [0, 0]
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