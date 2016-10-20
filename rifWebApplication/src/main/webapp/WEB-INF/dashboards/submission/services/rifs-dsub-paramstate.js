/* 
 * 
 */
angular.module("RIF")
        .factory('ParameterStateService',
                function (SubmissionStateService) {
                    var s = {
                        activeHealthTheme: "",
                        title: "My_New_Investigation",
                        start: 1,
                        end: 1,
                        interval: 1,
                        sex: "",
                        covariate: "NONE",
                        terms: [],
                        lowerAge: "",
                        upperAge: "",
                        possibleCovariates: []
                    };
                    var defaults = angular.copy(JSON.parse(JSON.stringify(s)));
                    return {
                        getState: function () {
                            return s;
                        },
                        resetState: function () {
                            s = angular.copy(defaults);
                        },
                        getModelInvestigation: function () {
                            if (s.terms.length !== 0) {
                                var investigation = [];
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
                                var thisInv = new inv();
                                thisInv.title = s.title;
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
                                var uprGroup;
                                var lwrGroup;
                                for (var i = 0; i < s.possibleAges.length; i++) {
                                    if (s.possibleAges[i].name === s.upperAge) {
                                        uprGroup = s.possibleAges[i];
                                    }
                                    if (s.possibleAges[i].name === s.lowerAge) {
                                        lwrGroup = s.possibleAges[i];
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
                                    "lower_bound": s.start,
                                    "upper_bound": s.end
                                };
                                thisInv.years_per_interval = s.interval;
                                var jump = Number(thisInv.years_per_interval);
                                for (var i = Number(thisInv.year_range.lower_bound); i <= Number(thisInv.year_range.upper_bound); i += jump) {
                                    thisInv.year_intervals.year_interval.push(
                                            {
                                                "start_year": i.toString(),
                                                "end_year": (i + jump - 1).toString()
                                            }
                                    );
                                }
                                thisInv.sex = s.sex;
                                for (var i = 0; i < s.possibleCovariates.length; i++) {
                                    if (s.possibleCovariates[i].name === s.covariate) {
                                        thisInv.covariates.push(
                                                {
                                                    "adjustable_covariate": {
                                                        "name": s.possibleCovariates[i].name,
                                                        "minimum_value": s.possibleCovariates[i].minimum_value,
                                                        "maximum_value": s.possibleCovariates[i].maximum_value,
                                                        "covariate_type": s.possibleCovariates[i].covariate_type
                                                    }
                                                }
                                        );
                                    }
                                }
                                for (var k = 0; k < s.terms.length; k++) {
                                    var thisID = s.terms[k][0].split('-');
                                    var thisName = angular.copy(thisID);
                                    thisName.pop();
                                    if (angular.isArray(thisName)) {
                                        thisName = thisName.join('-');
                                    }
                                    thisInv.health_codes.health_code.push(
                                            {
                                                "code": thisName,
                                                "name_space": thisID[thisID.length - 1],
                                                "description": s.terms[k][1],
                                                "is_top_level_term": "no"
                                            }
                                    );
                                }
                                investigation.push(thisInv);
                                return investigation;
                            }
                        }
                    };
                });