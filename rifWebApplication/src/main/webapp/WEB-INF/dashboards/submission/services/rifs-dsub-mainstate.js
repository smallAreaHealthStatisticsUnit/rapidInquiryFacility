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
                        projectName: "Project Name",
                        projectDescription: "Study Description (Optional)"
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