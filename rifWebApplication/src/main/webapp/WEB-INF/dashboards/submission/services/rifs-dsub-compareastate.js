/* SERVICE to store state of comparison area modal
 * will be used eventually to load studies
 * and also write to the JSON model object
 */
angular.module("RIF")
        .factory('CompAreaStateService',
                function () {
                    var s = {
                        polygonIDs: [],
                        selectAt: "",
                        studyResolution: "",
                        zoomLevel: -1,
                        view: [0, 0],
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