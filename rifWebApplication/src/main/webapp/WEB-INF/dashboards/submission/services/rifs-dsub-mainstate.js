/* SERVICE to store state of main submission page
 * will be used eventually to load studies
 */
angular.module("RIF")
        .factory('SubmissionStateService', 
                function () {
                    var s = {
                        //these are on the main disease submission page
                        studyTree: false,
                        comparisonTree: false,
                        investigationTree: false,
                        statsTree: false,
                        studyName: "Study Name",
                        healthTheme: "Health Theme",
                        numerator: "Numerator",
                        denominator: "Denominator",
                        //these are in the run-study modal
                        projectName: "",
                        projectDescription: ""
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