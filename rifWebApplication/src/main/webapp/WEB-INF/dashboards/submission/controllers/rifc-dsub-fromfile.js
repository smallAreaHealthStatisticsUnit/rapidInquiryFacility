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
 * CONTROLLER for disease submission run study from file modal
 */
angular.module("RIF")
        .controller('ModalRunFileCtrl', ['$q', 'user', '$scope', '$uibModal',
            'StudyAreaStateService', 'CompAreaStateService', 'SubmissionStateService', 'StatsStateService', 'ParameterStateService',
            function ($q, user, $scope, $uibModal,
                    StudyAreaStateService, CompAreaStateService, SubmissionStateService, StatsStateService, ParameterStateService) {

                var rifJob;
                var tmpProjects;
                var tmpGeography;
                var tmpGeoLevel;
                var tmpHealthThemeName;
                var tmpHealthThemeDescription;
                var tmpNumeratorName;
                var tmpDenominatorName;
                var tmpMethod;
                var tmpChecked = -2;
                var tmpFullICDselection = [];
                var tmpTitle;
                var tmpStart;
                var tmpEnd;
                var tmpInterval;
                var tmpSex;
                var tmpCovariate;

                /*
                 * THE FUNCIONS FOR CHECKING RIFJOB
                 */
                //checking this is a valid RIF study object
                function uploadCheckStructure() {
                    try {
                        rifJob = JSON.parse($scope.$$childHead.$$childTail.content).rif_job_submission;
                    } catch (e) {
                        return "Could not read file";
                    }
                    //has a non-empty object been uploaded?
                    if (angular.isUndefined(rifJob) || rifJob === null) {
                        return "Not a valid or recognised RIF job";
                    }
                    //Expected headers present for RIF study
                    var expectedHeaders = ['submitted_by', 'job_submission_date', 'project', 'disease_mapping_study', 'calculation_methods', 'rif_output_options'];
                    var thisHeaders = [];
                    for (var i in rifJob) {
                        thisHeaders.push(rifJob[i]);
                    }
                    if (expectedHeaders.length !== thisHeaders.length) {
                        return "Not a recognised RIF job, expected headers not found";
                    } else {
                        for (var i = 0; i < rifJob.length; i++) {
                            if (thisHeaders[i] !== expectedHeaders[i]) {
                                return "Expected header not found " + expectedHeaders[i];
                            }
                        }
                    }
                    return true;
                }

                //checking if the Health theme exists and matches geography
                function uploadHealthThemes() {
                    tmpHealthThemeName = rifJob.disease_mapping_study.investigations.investigation[0].health_theme.name;
                    tmpHealthThemeDescription = rifJob.disease_mapping_study.investigations.investigation[0].health_theme.description;
                    var themeErr = user.getHealthThemes(user.currentUser, rifJob.disease_mapping_study.geography.name).then(uploadHandleHealthThemes, fromFileError);

                    function uploadHandleHealthThemes(res) {
                        var bFound = false;
                        for (var i = 0; i < res.data.length; i++) {
                            if (res.data[i].name === tmpHealthThemeName & res.data[i].description === tmpHealthThemeDescription) {
                                bFound = true;
                                break;
                            }
                        }
                        if (!bFound) {
                            return "Health Theme '" + tmpHealthThemeName + "' not found in database";
                        } else {
                            return true;
                        }
                    }
                    return themeErr;
                }

                //Checking if the geography exists in the user database
                function uploadCheckGeography() {
                    tmpGeography = rifJob.disease_mapping_study.geography.name;
                    tmpGeoLevel = rifJob.disease_mapping_study.disease_mapping_study_area.geo_levels.geolevel_select.name;
                    var bFound = false;
                    for (var i = 0; i < $scope.$parent.geographies.length; i++) {
                        if ($scope.$parent.geographies[i] === tmpGeography) {
                            bFound = true;
                            break;
                        }
                    }
                    if (!bFound) {
                        return "Geography '" + tmpGeography + "' not found in database";
                    } else {
                        return true;
                    }
                }

                function uploadFractions() {
                    tmpNumeratorName = rifJob.disease_mapping_study.investigations.investigation[0].numerator_denominator_pair.numerator_table_name;
                    tmpDenominatorName = rifJob.disease_mapping_study.investigations.investigation[0].numerator_denominator_pair.denominator_table_name;
                    var fractionErr = user.getNumerator(user.currentUser, tmpGeography, tmpHealthThemeDescription).then(uploadHandleFractions, fromFileError);

                    function uploadHandleFractions(res) {
                        var bFound = false;
                        for (var i = 0; i < res.data.length; i++) {
                            if (res.data[i].numeratorTableName === tmpNumeratorName & res.data[i].denominatorTableName === tmpDenominatorName) {
                                bFound = true;
                                break;
                            }
                        }
                        if (!bFound) {
                            return "Numerator-Denominator Pair '" + tmpNumeratorName + " - " + tmpDenominatorName + "' not found in database";
                        } else {
                            return true;
                        }
                    }
                    return fractionErr;
                }

                function uploadStats() {
                    tmpMethod = rifJob.calculation_methods.calculation_method;
                    if (angular.isUndefined(tmpMethod.code_routine_name)) {
                        //method not yet selected by the user
                        return true;
                    }
                    //check method is actually availble to user
                    var statErr = user.getAvailableCalculationMethods(user.currentUser).then(uploadHandleAvailableCalculationMethods, fromFileError);

                    function uploadHandleAvailableCalculationMethods(res) {
                        var bFound = false;
                        var pCount = 0;
                        for (var i = 0; i < res.data.length; i++) {
                            if (tmpMethod.code_routine_name === res.data[i].codeRoutineName) {
                                if (tmpMethod.description === res.data[i].description) {
                                    for (var j = 0; j < tmpMethod.parameters.parameter.length; j++) {
                                        for (var k = 0; k < res.data[i].parameterProxies.length; k++) {
                                            if (tmpMethod.parameters.parameter[j].name === res.data[i].parameterProxies[k].name) {
                                                pCount++;
                                            }
                                        }
                                    }
                                    if (pCount === res.data[i].parameterProxies.length) {
                                        tmpChecked = i;
                                        bFound = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if (!bFound) {
                            return "Statistical Method '" + tmpMethod.description + "' not found in database, or has incomplete description";
                        } else {
                            return true;
                        }
                    }
                    return statErr;
                }

                function uploadProjects() {
                    tmpProjects = rifJob.project;
                    var projectErr = user.getProjects(user.currentUser).then(uploadHandleProjects, fromFileError);

                    function uploadHandleProjects(res) {
                        var bFound = false;
                        for (var i = 0; i < res.data.length; i++) {
                            if (res.data[i].name === tmpProjects.name) {
                                bFound = true;
                                break;
                            }
                        }
                        if (!bFound & tmpProjects.name !== "") {
                            return "Project '" + tmpProjects.name + "' not found in database";
                        } else {
                            return true;
                        }
                    }
                    return projectErr;
                }

                function uploadInvestigations() {
                    //terms
                    var inv = rifJob.disease_mapping_study.investigations.investigation;
                    for (var j = 0; j < inv[0].health_codes.health_code.length; j++) {
                        tmpFullICDselection.push([inv[0].health_codes.health_code[j].code + '-' + inv[0].health_codes.health_code[j].name_space,
                            inv[0].health_codes.health_code[j].description]);
                    }
                    //parameters
                    tmpTitle = inv[0].title;
                    tmpStart = inv[0].year_range.lower_bound;
                    tmpEnd = inv[0].year_range.upper_bound;
                    tmpInterval = inv[0].years_per_interval;
                    tmpSex = inv[0].sex;
                    var cv = "";
                    if (!angular.isUndefined(inv[0].covariates[0])) {
                        cv = inv[0].covariates[0].adjustable_covariate.name;
                    }
                    tmpCovariate = cv;
                    return true;
                }

                function uploadPossibleAges() {
                    if (ParameterStateService.getState().possibleAges.length === 0
                            & !angular.isUndefined(tmpGeography)
                            & !angular.isUndefined(tmpNumeratorName)) {
                        //get possible ages
                        var agesErr = user.getAgeGroups(user.currentUser, tmpGeography, tmpNumeratorName).then(fillHandleAgeGroups, fromFileError);

                        function fillHandleAgeGroups(res) {
                            if (!angular.isUndefined(res.data)) {
                                var tmp = [];
                                for (var i = 0; i < res.data[0].name.length; i++) {
                                    tmp.push({id: i, name: res.data[0].name[i], lower_limit: res.data[1].lowerAgeLimit[i], upper_limit: res.data[2].upperAgeLimit[i]});
                                }
                                ParameterStateService.getState().possibleAges = tmp;
                                return true;
                            } else {
                                return "Could not find valid age groups";
                            }
                        }
                        return agesErr;
                    } else {
                        return true;
                    }
                }

                function uploadPossibleCovariates() {
                    if (ParameterStateService.getState().possibleCovariates.length === 0
                            & !angular.isUndefined(tmpGeography)
                            & !angular.isUndefined(tmpNumeratorName)) {
                        //get possible covariates
                        var covErr = user.getCovariates(user.currentUser, tmpGeography, tmpGeoLevel).then(fillHandleCovariates, fromFileError);
                        function fillHandleCovariates(res) {
                            if (!angular.isUndefined(res.data)) {
                                var tmp = [];
                                for (var i = 0; i < res.data.length; i++) {
                                    tmp.push(res.data[i].name);
                                    tmp.push({name: res.data[i].name, minimum_value: res.data[i].minimumValue,
                                        maximum_value: res.data[i].maximumValue, covariate_type: res.data[i].covariateType});
                                }
                                ParameterStateService.getState().possibleCovariates = tmp;
                                return true;
                            } else {
                                return "Could not find valid covariates";
                            }
                        }
                        return covErr;
                    } else {
                        return true;
                    }
                }

                /*
                 * All tests passed so commit changes to states
                 */
                function confirmStateChanges() {
                    //general
                    SubmissionStateService.getState().studyName = rifJob.disease_mapping_study.name;
                    SubmissionStateService.getState().geography = rifJob.disease_mapping_study.geography.name;
                    SubmissionStateService.getState().numerator = rifJob.disease_mapping_study.investigations.investigation[0].numerator_denominator_pair.numerator_table_name;
                    SubmissionStateService.getState().denominator = rifJob.disease_mapping_study.investigations.investigation[0].numerator_denominator_pair.denominator_table_name;
                    SubmissionStateService.getState().studyDescription = rifJob.disease_mapping_study.description;
                    SubmissionStateService.getState().healthTheme = rifJob.disease_mapping_study.investigations.investigation[0].health_theme.name;

                    //Study area
                    StudyAreaStateService.getState().selectAt = rifJob.disease_mapping_study.disease_mapping_study_area.geo_levels.geolevel_select.name;
                    StudyAreaStateService.getState().studyResolution = rifJob.disease_mapping_study.disease_mapping_study_area.geo_levels.geolevel_to_map.name;
                    StudyAreaStateService.getState().polygonIDs = rifJob.disease_mapping_study.disease_mapping_study_area.map_areas.map_area;
                    StudyAreaStateService.getState().geography = rifJob.disease_mapping_study.geography.name;
                    if (StudyAreaStateService.getState().polygonIDs.length !== 0) {
                        SubmissionStateService.getState().studyTree = true;
                    }
                    //Comparison area
                    CompAreaStateService.getState().selectAt = rifJob.disease_mapping_study.comparison_area.geo_levels.geolevel_select.name;
                    CompAreaStateService.getState().studyResolution = rifJob.disease_mapping_study.comparison_area.geo_levels.geolevel_to_map.name;
                    CompAreaStateService.getState().polygonIDs = rifJob.disease_mapping_study.comparison_area.map_areas.map_area;
                    CompAreaStateService.getState().geography = rifJob.disease_mapping_study.geography.name;
                    if (CompAreaStateService.getState().polygonIDs.length !== 0) {
                        SubmissionStateService.getState().comparisonTree = true;
                    }

                    //Parameters
                    var inv = rifJob.disease_mapping_study.investigations.investigation;
                    ParameterStateService.getState().title = inv[0].title;
                    ParameterStateService.getState().start = inv[0].year_range.lower_bound;
                    ParameterStateService.getState().end = inv[0].year_range.upper_bound;
                    ParameterStateService.getState().lowerAge = inv[0].age_band.lower_age_group.name;
                    ParameterStateService.getState().upperAge = inv[0].age_band.upper_age_group.name;
                    ParameterStateService.getState().interval = inv[0].years_per_interval;
                    ParameterStateService.getState().sex = inv[0].sex;
                    ParameterStateService.getState().covariate = tmpCovariate;
                    ParameterStateService.getState().activeHealthTheme = rifJob.disease_mapping_study.investigations.investigation[0].health_theme.name;
                    ParameterStateService.getState().terms = tmpFullICDselection;
                    if (tmpFullICDselection.length !== 0) {
                        SubmissionStateService.getState().investigationTree = true;
                    }

                    //Stats
                    StatsStateService.getState().checked = tmpChecked;
                    if (tmpChecked >= 0) {
                        for (var i = 0; i < tmpMethod.parameters.parameter.length; i++) {
                            StatsStateService.getState().model[tmpChecked][i] = tmpMethod.parameters.parameter[i].value;
                        }
                        SubmissionStateService.getState().statsTree = true;
                    }
                }

                function fromFileError() {
                    $scope.showError("Could not upload the file");
                }

                $scope.open = function () {
                    $scope.modalHeader = "Open study from file";
                    $scope.accept = ".json";

                    $scope.showContent = function ($fileContent) {
                        $scope.content = $fileContent.toString();
                    };

                    $scope.uploadFile = function () {
                        
                        $scope.showSuccess("Starting upload...");

                        //check initial file structure
                        var d1 = $q.defer();
                        var p1 = d1.promise;
                        d1.resolve(uploadCheckStructure());
                        p1.then(function (value) {
                            return value;
                        }, fromFileError);

                        //check geography exists
                        var p2 = uploadCheckGeography();

                        //check health theme
                        var d3 = $q.defer();
                        var p3 = d3.promise;
                        d3.resolve(uploadHealthThemes());
                        p3.then(function (value) {
                            return value;
                        }, fromFileError);

                        //check numerator-denominator match
                        var d4 = $q.defer();
                        var p4 = d4.promise;
                        d4.resolve(uploadFractions());
                        p4.then(function (value) {
                            return value;
                        }, fromFileError);

                        //check stats and parameter match
                        var d5 = $q.defer();
                        var p5 = d5.promise;
                        d5.resolve(uploadStats());
                        p5.then(function (value) {
                            return value;
                        }, fromFileError);

                        //check project matches
                        var d6 = $q.defer();
                        var p6 = d6.promise;
                        d6.resolve(uploadProjects());
                        p6.then(function (value) {
                            return value;
                        }, fromFileError);

                        //check possible ages filled
                        var d7 = $q.defer();
                        var p7 = d7.promise;
                        d7.resolve(uploadPossibleAges());
                        p7.then(function (value) {
                            return value;
                        }, fromFileError);

                        //check possible covariates filled
                        var d8 = $q.defer();
                        var p8 = d8.promise;
                        d8.resolve(uploadPossibleCovariates());
                        p8.then(function (value) {
                            return value;
                        }, fromFileError);

                        //check investigations
                        var d9 = $q.defer();
                        var p9 = d9.promise;
                        d9.resolve(uploadInvestigations());
                        p9.then(function (value) {
                            return value;
                        }, fromFileError);

                        //resolve all the promises
                        $q.all([p1, p2, p3, p4, p5, p6, p7, p8, p9]).then(function (result) {
                            var bPass = true;
                            for (var i = 0; i < result.length; i++) {
                                if (result[i] !== true) {
                                    bPass = false;
                                    $scope.showError(result[i]);
                                    break;
                                }
                            }
                            if (bPass) {
                                //All tests passed
                                confirmStateChanges();
                                $scope.showSuccess("RIF study opened from file");
                                $scope.$parent.resetState();
                            }
                        });
                    };

                    var modalInstance = $uibModal.open({
                        animation: false,
                        templateUrl: 'dashboards/submission/partials/rifp-dsub-fromfile.html',
                        controller: 'ModalRunFileInstanceCtrl',
                        windowClass: 'stats-Modal',
                        backdrop: 'static',
                        scope: $scope,
                        keyboard: false
                    });
                };
            }])
        .controller('ModalRunFileInstanceCtrl', function ($scope, $uibModalInstance) {
            $scope.close = function () {
                $uibModalInstance.dismiss();
            };
            $scope.submit = function () {
                $scope.uploadFile();
                $uibModalInstance.close();
            };
        });