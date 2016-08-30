/* SERVICE to store state of investigation parameters modal
 * will be used eventually to load studies
 */
angular.module("RIF")
        .factory('ParameterStateService',
                function (SubmissionStateService) {
                    var activeHealthTheme = "";
                    var possibleAges = [];
                    var possibleCovariates = [];
                    var s = {
                        rows: new Array()
                    };
                    var defaults = angular.copy(JSON.parse(JSON.stringify(s)));

                    return {
                        setActiveHealthTheme: function (s) {
                            activeHealthTheme = s;
                        },
                        getActiveHealthTheme: function () {
                            return activeHealthTheme;
                        },
                        setPossibleAges: function (s) {
                            possibleAges = s;
                        },
                        getPossibleAges: function () {
                            return possibleAges;
                        },
                        setPossibleCovariates: function (s) {
                            possibleCovariates = s;
                        },
                        getPossibleCovariates: function () {
                            return possibleCovariates;
                        },
                        getState: function () {
                            return s;
                        },
                        resetState: function () {
                            s = angular.copy(defaults);
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
                                    this.covariates = [];
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
                                                    "name": SubmissionStateService.getState().healthTheme.name,
                                                    "description": SubmissionStateService.getState().healthTheme.description
                                                };
                                                thisInv.numerator_denominator_pair = {
                                                    "numerator_table_name": SubmissionStateService.getState().numerator.numeratorTableName,
                                                    "numerator_table_description": SubmissionStateService.getState().numerator.numeratorTableDescription,
                                                    "denominator_table_name": SubmissionStateService.getState().denominator.denominatorTableName,
                                                    "denominator_table_description": SubmissionStateService.getState().denominator.denominatorTableDescription
                                                };
                                                var lowerBand = s.rows[k].age_groups.split(',')[0].split(': ')[1];
                                                var upperBand = s.rows[k].age_groups.split(',')[1].split(': ')[1];
                                                var lwrGroup;
                                                var uprGroup;
                                                for (var i = 0; i < possibleAges.length; i++) {
                                                    if (possibleAges[i].name === lowerBand) {
                                                        lwrGroup = possibleAges[i];
                                                    }
                                                    if (possibleAges[i].name === upperBand) {
                                                        uprGroup = possibleAges[i];
                                                    }
                                                }
                                                thisInv.age_band = {
                                                    "lower_age_group": {
                                                        "id": lwrGroup.id,
                                                        "name": lwrGroup.name,
                                                        "lower_limit": lwrGroup.lower_limit,
                                                        "upper_limit": lwrGroup.upper_limit
                                                    },
                                                    "upper_age_group": {
                                                        "id": uprGroup.id,
                                                        "name": uprGroup.name,
                                                        "lower_limit": uprGroup.lower_limit,
                                                        "upper_limit": uprGroup.upper_limit
                                                    }
                                                };
                                                thisInv.year_range = {
                                                    "lower_bound": s.rows[k].years.split(' - ')[0],
                                                    "upper_bound": s.rows[k].years.split(' - ')[1].split(' [')[0]
                                                };
                                                thisInv.years_per_interval = s.rows[k].years.split('[')[1].split(']')[0];
                                                var jump = Number(thisInv.years_per_interval);
                                                for (var i = Number(thisInv.year_range.lower_bound); i <= Number(thisInv.year_range.upper_bound); i += jump) {
                                                    thisInv.year_intervals.year_interval.push(
                                                            {
                                                                "start_year": i.toString(),
                                                                "end_year": (i + jump - 1).toString()
                                                            }
                                                    );
                                                }
                                                thisInv.sex = s.rows[k].sex;
                                                var covars = s.rows[k].covariates.split(';');
                                                for (var v = 0; v < covars.length; v++) {
                                                    for (var i = 0; i < possibleCovariates.length; i++) {
                                                        if (possibleCovariates[i].name === covars[v].trim()) {
                                                            thisInv.covariates.push(
                                                                    {
                                                                        "adjustable_covariate": {
                                                                            "name": possibleCovariates[i].name,
                                                                            "minimum_value": possibleCovariates[i].minimum_value,
                                                                            "maximum_value": possibleCovariates[i].maximum_value,
                                                                            "covariate_type": possibleCovariates[i].covariate_type
                                                                        }
                                                                    }
                                                            );
                                                        }
                                                    }
                                                }
                                            }
                                            //all other rows contain just health codes
                                            thisInv.health_codes.health_code.push(
                                                    {
                                                        "code": s.rows[k].identifier.split('-')[0],
                                                        "name_space": s.rows[k].identifier.split('-')[1],
                                                        "description": s.rows[k].health_outcomes,
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