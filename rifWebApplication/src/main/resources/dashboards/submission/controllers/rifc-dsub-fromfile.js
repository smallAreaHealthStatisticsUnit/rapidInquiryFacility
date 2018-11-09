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
            'StudyAreaStateService', 'CompAreaStateService', 'SubmissionStateService', 'StatsStateService', 
			'ParameterStateService', 'SelectStateService',
            function ($q, user, $scope, $uibModal,
                    StudyAreaStateService, CompAreaStateService, SubmissionStateService, StatsStateService, 
					ParameterStateService, SelectStateService) {

                // Magic number for the always-included first method (see rifp-dsub-stats.html).
                const FIXED_NO_SMOOTHING_METHOD_POSITION  = -1;

                var rifJob;
                var studyType = "disease_mapping_study";
                var studyAreaType = "disease_mapping_study_area";
				var riskAnalysisExposureField;
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
				var fromFileErrorCount=0;

                /*
                 * THE FUNCIONS FOR CHECKING RIFJOB JSON
                 * This is done in a chain of promises
                 */
                //checking this is a valid RIF study object
                function uploadCheckStructure() {
                    try {
                        rifJob = JSON5.parse($scope.$$childHead.$$childTail.content).rif_job_submission;
                    } catch (e) {
                        return "Could not read file";
                    }
                    //has a non-empty object been uploaded?
                    if (angular.isUndefined(rifJob) || rifJob === null) {
                        return "Not a valid or recognized RIF job";
                    }

                    //JSON headers
                    var thisHeaders = [];
                    for (var i in rifJob) {
						//Risk analysis OR disease mapping study?
						if (i == "risk_analysis_study") {
							studyType = "risk_analysis_study";
							studyAreaType = "risk_analysis_study_area";
						}
                        thisHeaders.push(rifJob[i]);
                    }

                    //Expected headers present for RIF study; boolean is mandatory
                    var expectedHeaders = {
						submitted_by: true,
						job_submission_date: true, 
						project: true, 
						calculation_methods: true, 
						rif_output_options: true, 
						study_selection: false
					};
					expectedHeaders[studyType]=true;
					
					if (rifJob[studyType].riskAnalysisExposureField) {	
						riskAnalysisExposureField = rifJob[studyType].riskAnalysisExposureField;
						$scope.consoleDebug("[rifc-dsub-fromfile.js] riskAnalysisExposureField: " + 
							riskAnalysisExposureField);
					}
					
					// Check for missing
					var headerMissingCount = 0;
					var missingHeaders = undefined;
					for (var header in expectedHeaders) {
                        if (!rifJob[header]) {
							if (!expectedHeaders[header]) { // Not mandatory
								$scope.showWarning(header + " non mandatory header is missing"); 
							}
							else if (!missingHeaders) {
								headerMissingCount++;
								missingHeaders = header;
							}
							else {
								headerMissingCount++;
								missingHeaders+=", " + header;
							}
						}
					}
					if (headerMissingCount > 0) {
						$scope.consoleDebug("[rifc-dsub-fromfile.js] Not a recognized RIF job, " +
							headerMissingCount + " expected header(s) not found: " + missingHeaders + 
							"; rifJob: " + JSON.stringify(rifJob, null, 2) 
							);
						return "Not a recognized RIF job, " +
							headerMissingCount + " expected header(s) not found: " + missingHeaders;
					}
					
					// Check for extra
					var headerExtraCount = 0;
					var extraHeaders = undefined;
					for (var header in rifJob) {
                        if (!expectedHeaders[header]) {
							if (!missingHeaders) {
								headerExtraCount++;
								extraHeaders = header;
							}
							else {
								headerExtraCount++;
								extraHeaders+=", " + header;
							}
						}
					}
					if (headerMissingCount > 0) {
						$scope.consoleDebug("[rifc-dsub-fromfile.js] Not a recognized RIF job, " +
							headerExtraCount + " extra header(s) found: " + extraHeaders + 
							"; rifJob: " + JSON.stringify(rifJob, null, 2) 
							);
						return "Not a recognized RIF job, " +
							headerExtraCount + " extra header(s) found: " + extraHeaders;
					}

					if (rifJob[studyType] == undefined) {
						$scope.consoleDebug("[rifc-dsub-fromfile.js] No " + studyType + " object found." + 
							"; rifJob: " + JSON.stringify(rifJob, null, 2) 
							);
						
						return "No " + studyType + " object found";
					}
					else if (rifJob[studyType][studyAreaType] == undefined) {
	 					var keys=Object.keys(rifJob[studyType]);
						return "No " + studyType + "." + studyAreaType + " object found; keys: " +
							JSON.stringify(keys, null, 0);
					}
                    $scope.consoleDebug("[rifc-dsub-fromfile.js] Parsed study: " + studyType + 
						"; " + thisHeaders.length + " headers found");
					
                    return true;
                }

                //checking if the Health theme exists and matches geography
                function uploadHealthThemes() {
					try {
						if (rifJob[studyType].geography == undefined) {
							return "No rif_job_submission[studyType].geography defined, unable to determine health theme";
						}
						else if (rifJob[studyType].geography.name == undefined) {
							return "No rif_job_submission[studyType].geography.name defined, unable to determine health theme";
						}						
						else if (rifJob[studyType].investigations == undefined) {
							return "No rif_job_submission[studyType].investigations defined, unable to determine health theme";
						}							
						else if (rifJob[studyType].investigations.investigation[0] == undefined) {
							return "No rif_job_submission[studyType].investigations.investigation[0] defined, unable to determine health theme";
						}						
						else if (rifJob[studyType].investigations.investigation[0].health_theme == undefined) {
							return "No rif_job_submission[studyType].investigations.investigation[0].health_theme defined, unable to determine health theme";
						}
						else if (rifJob[studyType].investigations.investigation[0].health_theme.name == undefined) {
							return "No rif_job_submission[studyType].investigations.investigation[0].health_theme.name defined, unable to determine health theme";
						}
						else {
							tmpHealthThemeName = rifJob[studyType].investigations.investigation[0].health_theme.name;
							if (rifJob[studyType].investigations.investigation[0].health_theme.description == undefined) {
								return "No rif_job_submission[studyType].investigations.investigation[0].health_theme.description defined, unable to determine health theme";
							}
						}
						tmpHealthThemeDescription = rifJob[studyType].investigations.investigation[0].health_theme.description;
						
						$scope.consoleDebug("[rifc-dsub-fromfile.js] Check health theme: " + tmpHealthThemeName + " (" + 
							tmpHealthThemeDescription + ") using geography: " + rifJob[studyType].geography.name);
						var themeErr = user.getHealthThemes(user.currentUser, rifJob[studyType].geography.name).then(uploadHandleHealthThemes, fromFileError);

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
								$scope.consoleDebug("[rifc-dsub-fromfile.js] health theme OK: " + 
									tmpHealthThemeName);
								return true;
							}
						}
						return themeErr;
                    } catch (e) {
                        return "Could not upload health themes: " + (e.message||"(no message)");
                    }
                }

                //Checking if the geography exists in the user database
                function uploadCheckGeography() {
					try {
						if (rifJob[studyType].geography == undefined) {
							return "No rif_job_submission[studyType].geography defined, unable to check geography";
						}
						else if (rifJob[studyType].geography.name == undefined) {
							return "No rif_job_submission[studyType].geography.name defined, unable to check geography";
						}
						else if (rifJob[studyType][studyAreaType].geo_levels == undefined) {
							return "No rif_job_submission[studyType][studyAreaType].geo_levels defined, unable to check geography";
						}
						else if (rifJob[studyType][studyAreaType].geo_levels.geolevel_select == undefined) {
							return "No rif_job_submission[studyType][studyAreaType].geo_levels.geolevel_select defined, unable to check geography";
						}
						else if (rifJob[studyType][studyAreaType].geo_levels.geolevel_select.name == undefined) {
							return "No rif_job_submission[studyType][studyAreaType].geo_levels.geolevel_select.name defined, unable to check geography";
						}
						tmpGeography = rifJob[studyType].geography.name;
						tmpGeoLevel = rifJob[studyType][studyAreaType].geo_levels.geolevel_select.name;
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
							$scope.consoleDebug("[rifc-dsub-fromfile.js] Check geography OK: " + tmpGeography +
								" and geolevel: " + tmpGeoLevel);
							return true;
						}
                    } catch (e) {
                        return "Could not upload and check geography: " + (e.message||"(no message)");
                    }
                }

                function uploadFractions() {
					try {	
						if (rifJob[studyType].investigations == undefined) {
							return "No rif_job_submission[studyType].disease_mapping_study.investigations defined, unable to check numerator/denominator pair";
						}
						else if (rifJob[studyType].investigations.investigation[0] == undefined) {
							return "No rif_job_submission[studyType].disease_mapping_study.investigations.investigation[0] defined, unable to check numerator/denominator pair";
						}
						else if (rifJob[studyType].investigations.investigation[0].numerator_denominator_pair == undefined) {
							return "No rif_job_submission[studyType].disease_mapping_study.investigations.investigation[0].numerator_denominator_pair defined, unable to check numerator/denominator pair";
						}
						else if (rifJob[studyType].investigations.investigation[0].numerator_denominator_pair.numerator_table_name == undefined) {
							return "No rif_job_submission[studyType].disease_mapping_study.investigations.investigation[0].numerator_denominator_pair.numerator_table_name defined, unable to check numerator/denominator pair";
						}
						else if (rifJob[studyType].investigations.investigation[0].numerator_denominator_pair.denominator_table_name == undefined) {
							return "No rif_job_submission[studyType].disease_mapping_study.investigations.investigation[0].numerator_denominator_pair.denominator_table_name defined, unable to check numerator/denominator pair";
						}
						tmpNumeratorName = rifJob[studyType].investigations.investigation[0].numerator_denominator_pair.numerator_table_name;
						tmpDenominatorName = rifJob[studyType].investigations.investigation[0].numerator_denominator_pair.denominator_table_name;
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
                    } catch (e) {
                        return "Could not upload and check numerator denominator pair: " + (e.message||"(no message)");
                    }
                }

                function uploadStats() {
					try {
						if (rifJob.calculation_methods == undefined
						    || rifJob.calculation_methods.calculation_method == undefined) {
							return true;
						}

						tmpMethod = rifJob.calculation_methods.calculation_method;
						if (angular.isUndefined(tmpMethod.code_routine_name)) {
							//method not yet selected by the user
							return true;
						}

						// Special case for "No Bayesian Smoothing"
						if (tmpMethod.name === "NONE" || tmpMethod.name === "") {
						    tmpChecked = FIXED_NO_SMOOTHING_METHOD_POSITION;
						    return true;
						}

						//check method is actually available to user
						//currently, it always will be
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
								$scope.consoleDebug("[rifc-dsub-fromfile.js] Statistical Method not found error: " +
									JSON.stringify(tmpMethod) + "; valid methods: " + JSON.stringify(res.data, null, 2));
								return "Statistical Method '" + tmpMethod.description + "' not found in database, or has incomplete description";
							} else {
								return true;
							}
						}
						return statErr;	
                    } catch (e) {
                        return "Could not upload and check statistical methods: " + (e.message||"(no message)");
                    }
                }

                function uploadProjects() {
					try {	
						if (rifJob.project == undefined) {
							return "No rif_job_submission.project defined, unable to check project";
						}	
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
                    } catch (e) {
                        return "Could not upload and check projects: " + (e.message||"(no message)");
                    }
                }

                //will need adjusting when rif can handle multiple investigations
                function uploadInvestigations() {
					try {
						if (rifJob[studyType].investigations == undefined) {
							return "No rif_job_submission[studyType].investigations defined, unable to check investigations";
						}	
						if (rifJob[studyType].investigations.investigation == undefined) {
							return "No rif_job_submission[studyType].investigations.investigation defined, unable to check investigations";
						}	
						//terms
						var inv = rifJob[studyType].investigations.investigation;
						if (inv.length != 1) {
							return "Found no/multiple .nvestigations, unable to check investigations";
						}
						
						if (inv[0].health_codes.health_code.length == 0) {
							return "Found no investigation health codes, unable to check investigations";
						}
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
                    } catch (e) {
                        return "Could not upload and check investigations: " + (e.message||"(no message)");
                    }
                }

                function uploadPossibleAges() {
					try {
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
                    } catch (e) {
                        return "Could not upload and check ages: " + (e.message||"(no message)");
                    }
                }

                function uploadPossibleCovariates() {
					try {
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
                    } catch (e) {
                        return "Could not upload and check covariates: " + (e.message||"(no message)");
                    }
                }

                function uploadStudySelection() {
					try {	
						SelectStateService.getState().studyType=studyType;
						if (rifJob.study_selection) { // Set in JSON file
							//set StudySelection					
							SelectStateService.setStudySelection(rifJob.study_selection, studyType);
							if (SelectStateService.getState().studyType == undefined) {	// Set studyType
								return "SelectStateService.getState().studyType is undefined";
							}	
							return true;
						} else {
							if (SelectStateService.getState().studyType == 'disease_mapping_study') {
								SelectStateService.resetState();
							}
							else if (SelectStateService.getState().studyType == 'risk_analysis_study') {
								SelectStateService.initialiseRiskAnalysis();
							}
							else {
								return "Invalid SelectStateService.getState().studyType: " + SelectStateService.getState().studyType;
							}
							return true; // Optional for backward compatibility
						}				
                    } catch (e) {
						$scope.consoleDebug("[rifc-dsub-fromfile.js] SelectStateService.getState(): " + 
							JSON.stringify(SelectStateService.getState(), null, 1));
                        return "Could not upload and check study selection: " + (e.message||"(no message)");
                    }
                }
				
                /*
                 * All tests passed so commit changes to states
                 */
                function confirmStateChanges() {
					try {
						//general
						SubmissionStateService.getState().studyName = rifJob[studyType].name;
						SubmissionStateService.getState().geography = rifJob[studyType].geography.name;
						SubmissionStateService.getState().numerator = rifJob[studyType].investigations.investigation[0].numerator_denominator_pair.numerator_table_name;
						SubmissionStateService.getState().denominator = rifJob[studyType].investigations.investigation[0].numerator_denominator_pair.denominator_table_name;
						SubmissionStateService.getState().studyDescription = rifJob[studyType].description;
						SubmissionStateService.getState().healthTheme = rifJob[studyType].investigations.investigation[0].health_theme.name;

						//Study area
						StudyAreaStateService.getState().selectAt = rifJob[studyType][studyAreaType].geo_levels.geolevel_select.name;
						StudyAreaStateService.getState().studyResolution = rifJob[studyType][studyAreaType].geo_levels.geolevel_to_map.name;
						StudyAreaStateService.getState().polygonIDs = rifJob[studyType][studyAreaType].map_areas.map_area;
						if (StudyAreaStateService.getState().polygonIDs.length == 0) {
							throw new Error("No study area polygons");
						}
						else {
							$scope.consoleDebug("[rifc-dsub-fromfile.js] StudyAreaStateService.getState().polygonIDs[0]: " +
								JSON.stringify(StudyAreaStateService.getState().polygonIDs[0]));
						}
						StudyAreaStateService.getState().geography = rifJob[studyType].geography.name;
						if (StudyAreaStateService.getState().polygonIDs.length !== 0) {
							SubmissionStateService.getState().studyTree = true;
						}
						if (studyType === "risk_analysis_study") {
							StudyAreaStateService.getState().type = "Risk Analysis";	
						}
						SubmissionStateService.getState().studyType = StudyAreaStateService.getState().type;
						if (riskAnalysisExposureField) {
							SubmissionStateService.getState().riskAnalysisExposureField	= riskAnalysisExposureField;			
							$scope.consoleDebug("[rifc-dsub-fromfile.js] SubmissionStateService.getState().riskAnalysisExposureField: " + 
								SubmissionStateService.getState().riskAnalysisExposureField);
						} 
						
						//Comparison area
						CompAreaStateService.getState().selectAt = rifJob[studyType].comparison_area.geo_levels.geolevel_select.name;
						CompAreaStateService.getState().studyResolution = rifJob[studyType].comparison_area.geo_levels.geolevel_to_map.name;
						CompAreaStateService.getState().polygonIDs = rifJob[studyType].comparison_area.map_areas.map_area;
						if (CompAreaStateService.getState().polygonIDs.length == 0) {
							throw new Error("No comparison area polygons");
						}
						else {
							$scope.consoleDebug("[rifc-dsub-fromfile.js] CompAreaStateService.getState().polygonIDs[0]: " +
								JSON.stringify(CompAreaStateService.getState().polygonIDs[0]));
						}
						CompAreaStateService.getState().geography = rifJob[studyType].geography.name;
						if (CompAreaStateService.getState().polygonIDs.length !== 0) {
							SubmissionStateService.getState().comparisonTree = true;
						}

						//Parameters
						var inv = rifJob[studyType].investigations.investigation;
						ParameterStateService.getState().title = inv[0].title;
						ParameterStateService.getState().start = inv[0].year_range.lower_bound;
						ParameterStateService.getState().end = inv[0].year_range.upper_bound;
						ParameterStateService.getState().lowerAge = inv[0].age_band.lower_age_group.name;
						ParameterStateService.getState().upperAge = inv[0].age_band.upper_age_group.name;
						ParameterStateService.getState().interval = inv[0].years_per_interval;
						ParameterStateService.getState().sex = inv[0].sex;
						ParameterStateService.getState().covariate = tmpCovariate;
						ParameterStateService.getState().activeHealthTheme = rifJob[studyType].investigations.investigation[0].health_theme.name;
						ParameterStateService.getState().terms = tmpFullICDselection;
						if (tmpFullICDselection.length !== 0) {
							SubmissionStateService.getState().investigationTree = true;
						}

						//Stats
						StatsStateService.getState().checked = tmpChecked;
						if (tmpChecked >= FIXED_NO_SMOOTHING_METHOD_POSITION) {
							for (var i = 0; i < tmpMethod.parameters.parameter.length; i++) {
								StatsStateService.getState().model[tmpChecked][i] = tmpMethod.parameters.parameter[i].value;
							}
							SubmissionStateService.getState().statsTree = true;
						}	
						
						if (SelectStateService.getState().studySelection.comparisonSelectAt == undefined) {
							SelectStateService.getState().studySelection.comparisonSelectAt = 
								CompAreaStateService.getState().selectAt;
						}
						if (SelectStateService.getState().studySelection.studySelectAt === undefined) {
							SelectStateService.getState().studySelection.studySelectAt = 
								StudyAreaStateService.getState().selectAt;
						}					
//						if (SelectStateService.getState().studySelection.studySelectedAreas === undefined  &&
//						    studyType === "disease_mapping_study") {
//							SelectStateService.getState().studySelection.studySelectedAreas = 
//								StudyAreaStateService.getState().studySelectedAreas;
//						}
//						if (SelectStateService.getState().studySelection.comparisonSelectedAreas === undefined) {
//							SelectStateService.getState().studySelection.comparisonSelectedAreas = 
//								StudyAreaStateService.getState().comparisonSelectedAreas;
//						}
						// Fix for: WARNING: Could not upload and check study selection: study comparison resolution not setup correctly
						// (Older saved studies)
						if (rifJob.study_selection.comparisonSelectAt == undefined) {
							$scope.consoleDebug("[rifc-dsub-fromfile.js] set rifJob.study_selection.comparisonSelectAt: " +
								JSON.stringify(CompAreaStateService.getState().comparisonSelectAt, null, 1));
							rifJob.study_selection.comparisonSelectAt=CompAreaStateService.getState().comparisonSelectAt;
						}
						if (rifJob.study_selection.comparisonSelectedAreas == undefined ||
							rifJob.study_selection.comparisonSelectedAreas.length <1) {
							$scope.consoleDebug("[rifc-dsub-fromfile.js] set rifJob.study_selection.comparisonSelectedAreas: " +
								JSON.stringify(CompAreaStateService.getState().polygonIDs, null, 1));
							rifJob.study_selection.comparisonSelectedAreas=CompAreaStateService.getState().polygonIDs;
						}	
							
						try {
							if (SelectStateService.getState().studySelection) {
								var r=SelectStateService.verifyStudySelection();
								$scope.consoleDebug("[rifc-dsub-fromfile.js] verifyStudySelection() OK.");
							}
						}
						catch (e) {
							fromFileErrorCount++;
							$scope.showError("Unable to verify study selection: " + e.message);
							return false;
						}							

                    } catch (e) {
                        $scope.showError("Could not set study state: " + (e.message||"(no message)"));
						return false;
                    }
					return true;
                }

                function fromFileError() {
					fromFileErrorCount++;
					if (fromFileErrorCount < 2) {
						if ($scope.fileName) {
							$scope.showError("Could not upload saved study file: " + $scope.fileName);
						}
						else {
							$scope.showError("Could not upload saved study file: no file specified");
						}
					}
                }

                $scope.open = function () {
                    $scope.modalHeader = "Open study from file";
                    $scope.accept = ".json";

                    $scope.showContent = function ($fileContent, $fileName) {
                        $scope.content = $fileContent.toString();
						$scope.fileName = $fileName;
                    };

                    $scope.uploadFile = function () {

						//reset all submission states to default
						SubmissionStateService.resetState();
						StudyAreaStateService.resetState();
						SelectStateService.resetState();
						CompAreaStateService.resetState();
						ParameterStateService.resetState();
						SelectStateService.resetState();
                        $scope.consoleDebug("[rifc-dsub-fromfile.js] Starting upload...");

						// Create promises
                        var d1 = $q.defer();
                        var p1 = d1.promise;
                        var d2 = $q.defer();
                        var p2 = d2.promise;
                        var d3 = $q.defer();
                        var p3 = d3.promise;
                        var d4 = $q.defer();
                        var p4 = d4.promise;
                        var d5 = $q.defer();
                        var p5 = d5.promise;
                        var d6 = $q.defer();
                        var p6 = d6.promise;
                        var d7 = $q.defer();
                        var p7 = d7.promise;
                        var d8 = $q.defer();
                        var p8 = d8.promise;
                        var d9 = $q.defer();
                        var p9 = d9.promise;
                        var d10 = $q.defer();
                        var p10 = d10.promise;
						
                        //check initial file structure
                        d1.resolve(uploadCheckStructure());
                        p1.then(function (value) {
							
							if (value === true) {
								//check geography exists
								d2.resolve(uploadCheckGeography());
								p2.then(function (value) {
									return value;
								}, fromFileError);
								
								//check health theme
								d3.resolve(uploadHealthThemes());
								p3.then(function (value) {
									return value;
								}, fromFileError);

								//check numerator-denominator match
								d4.resolve(uploadFractions());
								p4.then(function (value) {
									return value;
								}, fromFileError);

								//check stats and parameter match
								d5.resolve(uploadStats());
								p5.then(function (value) {
									return value;
								}, fromFileError);

								//check project matches
								d6.resolve(uploadProjects());
								p6.then(function (value) {
									return value;
								}, fromFileError);

								//check possible ages filled
								d7.resolve(uploadPossibleAges());
								p7.then(function (value) {
									return value;
								}, fromFileError);

								//check possible covariates filled
								d8.resolve(uploadPossibleCovariates());
								p8.then(function (value) {
									return value;
								}, fromFileError);

								//check investigations
								d9.resolve(uploadInvestigations());
								p9.then(function (value) {
									return value;
								}, fromFileError);
								
								//check studySelection
								d10.resolve(uploadStudySelection());
								p10.then(function (value) {
									return value;
								}, fromFileError);
								
								//resolve all the promises
								$q.all([p2, p3, p4, p5, p6, p7, p8, p9, p10]).then(function (result) {
									var bPass = true;
									var errorCount=0;
									for (var i = 0; i < result.length; i++) {
										if (result[i] !== true) {
											bPass = false;
											$scope.showWarningNoHide(result[i]);
											errorCount++;
										}
									}
									if (bPass) {
										//All tests passed
										if (confirmStateChanges()) {
											$scope.showSuccess('RIF ' + StudyAreaStateService.getState().type + ' study ' +
											(rifJob[studyType].name ? 
												('"' + rifJob[studyType].name + '" '): "") +
											'opened from file: ' + ($scope.fileName ? ('"' + $scope.fileName + '"'): 'N/A'));
											$scope.$parent.studyName = rifJob[studyType].name;
											$scope.$parent.resetState();
										}
										else {
											$scope.showError("RIF study opened from file: "  +
											(rifJob[studyType].name ? 
												('"' + rifJob[studyType].name + '" '): "") +
											'opened from file: ' + ($scope.fileName ? ('"' + $scope.fileName + '"'): 'N/A') + 
											" failed in state change setup");
										}
									}
									else {
										$scope.showError("RIF study opened from file: "  +
											(rifJob[studyType].name ? 
												('"' + rifJob[studyType].name + '" '): "") +
											'opened from file: ' + ($scope.fileName ? ('"' + $scope.fileName + '"'): 'N/A') + 
											" failed with " +
											errorCount + " error(s)");
									}
								});
							} // uploadCheckStructure() OK
							else {
								$scope.consoleDebug("[rifc-dsub-fromfile.js] uploadCheckStructure() failed");
							}
							
                            return value;
                        }, fromFileError);
						
						$q.all([p1]).then(function (result) {
							var bPass = true;
							for (var i = 0; i < result.length; i++) {
								if (result[i] !== true) {
									bPass = false;
									$scope.showWarningNoHide(result[i]);
									break;
								}
							}
							if (bPass) {
								//All tests passed
								$scope.consoleDebug("[rifc-dsub-fromfile.js] RIF study parsed from file: " + $scope.fileName);
								$scope.$parent.resetState();
							}
							else {
								fromFileError();
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
