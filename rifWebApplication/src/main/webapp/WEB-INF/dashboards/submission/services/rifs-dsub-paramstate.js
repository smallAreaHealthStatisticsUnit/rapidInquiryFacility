/* SERVICE to store state of investigation parameters modal
 * will be used eventually to load studies
 */
angular.module("RIF")
        .factory('ParameterStateService',
                function (SubmissionStateService) {
                    var s = {
                        rows: new Array()
                    };
                    var defaults = JSON.parse(JSON.stringify(s));
                    return {
                        getState: function () {
                            return s;
                        },
                        resetState: function () {
                            s = defaults;
                        },
                        getModelInvestigation: function () {
                            if (s.rows.length !== 0) {
                                var investigation = new Array();
                                function inv()
                                {
                                    this.title = "";
                                    this.health_theme = "";
                                    this.numerator_denominator_pair = "";
                                    this.age_band = "";
                                    this.health_codes = {"health_code": []};
                                    this.year_range = "";
                                    this.year_intervals = {"year_interval": []};
                                    this.years_per_interval = "";
                                    this.sex = "";
                                    this.covariates = {"adjustable_covariate": []};
                                }
                                var n = s.rows[s.rows.length - 1].i;
                                for (var j = 1; j <= n; j++) {
                                    var bFirstRow = true;
                                    var thisInv = new inv();
                                    for (var k = 0; k < s.rows.length; k++) {
                                        if (s.rows[k].i === j) {
                                            //take descriptors for the 1st row
                                            if (bFirstRow) {
                                                thisInv.title = s.rows[k].title;
                                                thisInv.health_theme = {
                                                    "name": SubmissionStateService.getState().healthTheme,
                                                    "description": "TODO"
                                                };
                                                thisInv.numerator_denominator_pair = {
                                                    "numerator_table_name": SubmissionStateService.getState().numerator,
                                                    "numerator_table_description": SubmissionStateService.getState().numerator,
                                                    "denominator_table_name": SubmissionStateService.getState().denominator,
                                                    "denominator_table_description": SubmissionStateService.getState().denominator
                                                };
                                                thisInv.age_band = {
                                                    "lower_age_group": {
                                                        "id": s.rows[k].age_groups,
                                                        "name": "5_9",
                                                        "lower_limit": "5",
                                                        "upper_limit": "9"
                                                    },
                                                    "upper_age_group": {
                                                        "id": "1",
                                                        "name": "5_9",
                                                        "lower_limit": "5",
                                                        "upper_limit": "9"
                                                    }
                                                };
                                                thisInv.year_range = {
                                                    "lower_bound": s.rows[k].years.split(' - ')[0],
                                                    "upper_bound": s.rows[k].years.split(' - ')[1]
                                                };
                                                thisInv.year_intervals.year_interval.push(
                                                        {
                                                            "start_year": "1993xxx",
                                                            "end_year": "1996xxx"
                                                        }
                                                );
                                                thisInv.years_per_interval = "TODO";
                                                thisInv.sex = s.rows[k].gender;
                                                thisInv.covariates.adjustable_covariate.push(
                                                        {
                                                            "name": s.rows[k].covariates,
                                                            "minimum_value": "TODO",
                                                            "maximum_value": "TODO",
                                                            "covariate_type": "TODO"
                                                        }
                                                );
                                            }
                                            //all other rows contain just health codes
                                            thisInv.health_codes.health_code.push(
                                                    {
                                                        "code": s.rows[k].health_outcomes.split(' - ')[0],
                                                        "name_space": s.rows[k].taxonomy,
                                                        "description": s.rows[k].health_outcomes.split(' - ')[1],
                                                        "is_top_level_term": "no"
                                                    }
                                            );
                                            bFirstRow = false;
                                        }
                                    }
                                    investigation.push(thisInv);
                                }
                                return investigation;
                            }
                        }
                    };
                });