/**
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * Copyright 2016 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
 *
 * This file is part of the Rapid Inquiry Facility (RIF) project.
 * RIF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RIF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
 * to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
 * Boston, MA 02110-1301 USA

 * David Morley
 * @author dmorley
 */

/* 
 * SERVICE to store investigation parameter state
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
                        possibleCovariates: [],
                        possibleAges: []
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
                                    "numerator_table_name": SubmissionStateService.getState().denominator.numeratorTableName,
                                    "numerator_table_description": SubmissionStateService.getState().denominator.numeratorTableDescription,
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