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
 * CONTROLLER for the main 'tree' page
 */
angular.module("RIF")
        .controller('SumbmissionCtrl', ['$scope', 'user', '$state', 'SubmissionStateService', 
			'StudyAreaStateService', 'CompAreaStateService', 'ParameterStateService', 'SelectStateService', 
            function ($scope, user, $state, SubmissionStateService, 
				StudyAreaStateService, CompAreaStateService, ParameterStateService, SelectStateService) {
                /*
                 * STUDY, GEOGRAPHY AND FRACTION DROP-DOWNS
                 * Calls to API returns a chain of promises
                 */
				$scope.numerator = {};
				$scope.denominator = "";
                $scope.geographies = [];
				$scope.fractions = [];	
				$scope.fraction = "";	
				$scope.studyName = "";	
				$scope.geography = "";		
				$scope.healthThemes = [];
				$scope.healthTheme = "";

				//Get geographies
				$scope.consoleDebug("[rifc-dsub-main.js] Load $scope.numerator, $scope.fractions etc from DB");
				user.getGeographies(user.currentUser).then(
					function (res) { handleGeographies(res); }, 
					function (err) { handleError(err, "unable to get geographies"); }); //1ST PROMISE
				
                /*
                 * STUDY NAME
                 */
                $scope.studyNameChanged = function () {
                    SubmissionStateService.getState().studyName = $scope.studyName;
                };
				
                function handleGeographies(res) {
                    $scope.geographies.length = 0;
//					$scope.geographyDescriptions = {}; // Need to add description to array
//					$scope.consoleDebug("[rifc-dsub-main.js] res.data[0]: " + JSON.stringify(res.data[0], null, 1));
					
                    for (var i = 0; i < res.data[0].names.length; i++) {
                        $scope.geographies.push(res.data[0].names[i]);
                    }
                    var thisGeography = SubmissionStateService.getState().geography;
                    if ($scope.geographies.indexOf(thisGeography) !== -1) {
                        $scope.geography = thisGeography;
                    } else {
                        $scope.geography = $scope.geographies[0];
                    }
                    SubmissionStateService.getState().geography = $scope.geography;
                    //Fill health themes drop-down
                    $scope.healthThemes = [];
                    user.getHealthThemes(user.currentUser, $scope.geography).then(
						function (res) { handleHealthThemes(res); }, 
						function (err) { handleError(err, "unable to get health themes"); }); //2ND PROMISE
                }
                $scope.geographyChange = function () {
                    SubmissionStateService.getState().geography = $scope.geography;
                    //reset states using geography
                    StudyAreaStateService.resetState();
                    CompAreaStateService.resetState();
                    SelectStateService.resetState();
                    ParameterStateService.resetState();
                    SubmissionStateService.getState().comparisonTree = false;
                    SubmissionStateService.getState().studyTree = false;
                    SubmissionStateService.getState().investigationTree = false;
                    SubmissionStateService.getState().numerator = "";
                    SubmissionStateService.getState().denominator = "";
                    $scope.resetState();
                };

                //Get health themes
                function handleHealthThemes(res) {
                    $scope.healthThemes.length = 0;
                    for (var i = 0; i < res.data.length; i++) {
                        $scope.healthThemes.push({name: res.data[i].name, description: res.data[i].description});
                    }
                    $scope.healthTheme = $scope.healthThemes[0];
                    SubmissionStateService.getState().healthTheme = $scope.healthTheme;
                    $scope.healthThemeChange();
                }

                //Get relevant numerators and associated denominators
                $scope.healthThemeChange = function () {
                    if ($scope.healthTheme) {
                        SubmissionStateService.getState().healthTheme = $scope.healthTheme;
                        user.getNumerator(user.currentUser, $scope.geography, $scope.healthTheme.description).then(
							function (res) { handleFractions(res); }, 
							function (err) { handleError(err, "unable to get numerator/denominator pair"); }); //3RD PROMISE
                    } else {
                        $scope.fractions.length = 0;
                    }
                };
				
                function handleFractions(res) {
                    $scope.fractions = []; // Zero array
                    for (var i = 0; i < res.data.length; i++) {
                        $scope.fractions.push(res.data[i]);
                    }
					
                    if (SubmissionStateService.getState().numerator && 
					    typeof(SubmissionStateService.getState().numerator) == "string" &&
						SubmissionStateService.getState().numerator.length > 0) {
                        var thisNum = SubmissionStateService.getState().numerator;
                        for (var i = 0; i < $scope.fractions.length; i++) {
                            if ($scope.fractions[i].numeratorTableName == thisNum) {
								$scope.consoleDebug("[rifc-dsub-main.js] Set $scope.numerator, $scope.fractions etc from DB/SubmissionStateService: " + 
									$scope.fractions[i].numeratorTableName);
                                $scope.numerator = $scope.fractions[i];
                                $scope.denominator = $scope.fractions[i].denominatorTableName;
                            }
                        }
                    } else {
						$scope.consoleDebug("[rifc-dsub-main.js] Set default $scope.numerator, $scope.fractions etc from DB: " + 
							$scope.fractions[0].numeratorTableName);
                        $scope.numerator = $scope.fractions[0];
                        $scope.denominator = $scope.fractions[0].denominatorTableName;
                    }
					
					// Default $scope.denominator and $scope.numerator is set in SubmissionStateService
					if (checkNumeratorKeys($scope.numerator)) {
//						$scope.consoleDebug("[rifc-dsub-main.js] Set SubmissionStateService.getState().numerator: " + 
//							JSON.stringify($scope.numerator, null, 1));
//						$scope.numerator = copyFraction($scope.numerator);  // This removes the hashkey and causes the {{numerator.numeratorTableName}}
																			// tooltip to "disappear"
						SubmissionStateService.getState().numerator = $scope.numerator.numeratorTableName;
						SubmissionStateService.getState().denominator = $scope.numerator;
					}
/*					else if (checkNumeratorKeys(SubmissionStateService.getState().numerator)) {
						$scope.consoleDebug("[rifc-dsub-main.js] Restore $scope.numerator from SubmissionStateService.getState().numerator: " + 
							JSON.stringify(SubmissionStateService.getState().numerator, null, 1));
						$scope.numerator = copyFraction(SubmissionStateService.getState().numerator);
					} */
					
					if ($scope.denominator == undefined && SubmissionStateService.getState().numerator) {
						$scope.denominator = SubmissionStateService.getState().numerator.denominatorTableName;
					}
					
					
					if ($scope.studyName == "" && SubmissionStateService.getState().studyName) {
						$scope.studyName = SubmissionStateService.getState().studyName;
					}
					
					$scope.consoleDebug("[rifc-dsub-main.js] handleFractions(): " + JSON.stringify($scope.fractions, null, 1) +
						"; SubmissionStateService.getState(): " + JSON.stringify(SubmissionStateService.getState(), null, 1) +
						"; $scope.numerator: " + JSON.stringify($scope.numerator, null, 1) +
						"; $scope.denominator: " + $scope.denominator +
						"; $scope.studyName: " + $scope.studyName);
                }
				
				function checkNumeratorKeys(numerator) {
					if (numerator && 
						numerator.numeratorTableName && 
						numerator.denominatorTableName && 
						numerator.numeratorTableDescription && 
						numerator.denominatorTableDescription) {
						return true;
					}
					else {
						return false;
					}
				}
				
				function copyFraction(fraction) {
					var rVal=undefined;
					
					if (fraction && 
						fraction.numeratorTableName && 
						fraction.denominatorTableName && 
						fraction.numeratorTableDescription && 
						fraction.denominatorTableDescription) {
						rVal={
							numeratorTableName: fraction.numeratorTableName,
							denominatorTableName: fraction.denominatorTableName,
							numeratorTableDescription: fraction.numeratorTableDescription,
							denominatorTableDescription: fraction.denominatorTableDescription
						};
					}
				
					return rVal;
				}
				
				function compareFractions(fraction1, fraction2) {	
					if (fraction1 && fraction2 &&
						fraction1.numeratorTableName && 
						fraction1.denominatorTableName && 
						fraction1.numeratorTableDescription && 
						fraction1.denominatorTableDescription && 
						fraction2.numeratorTableName && 
						fraction2.denominatorTableName && 
						fraction2.numeratorTableDescription && 
						fraction2.denominatorTableDescription && 
						fraction1.numeratorTableName == fraction2.numeratorTableName && 
						fraction1.denominatorTableName == fraction2.denominatorTableName && 
						fraction1.numeratorTableDescription == fraction2.numeratorTableDescription && 
						fraction1.denominatorTableDescription == fraction2.denominatorTableDescription) {
						return true;
					}
					else {
						return false;
					}
				}

                //sync the denominator
                $scope.numeratorChange = function () {
                    if ($scope.numerator) {
                        $scope.denominator = $scope.numerator.denominatorTableName;
                    } else {
                        $scope.denominator = "";
                    }
					
					if (!compareFractions(SubmissionStateService.getState().numerator, $scope.numerator) &&
					    !compareFractions(SubmissionStateService.getState().denominator, $scope.numerator)) {
					
						$scope.consoleDebug("[rifc-dsub-main.js] numeratorChange(), reset investigation parameters, $scope.numerator: " + 
							JSON.stringify($scope.numerator, null, 1) +
							"; SubmissionStateService.getState().numerator: " + JSON.stringify(SubmissionStateService.getState().numerator, null, 1) +
							"; SubmissionStateService.getState().denominator: " + JSON.stringify(SubmissionStateService.getState().denominator, null, 1));
						SubmissionStateService.getState().numerator = $scope.numerator;
						SubmissionStateService.getState().denominator = $scope.numerator;
						//This will have an impact on investigations year range, so reset investigation parameters
						ParameterStateService.resetState();
					}
					else {		
						$scope.consoleDebug("[rifc-dsub-main.js] numeratorChange(), no change: " + JSON.stringify($scope.numerator, null, 2));
					}	
                };

                function handleError(e, reason) {
                    $scope.showError("Could not retrieve your project information from the database: " + reason);
					$scope.consoleDebug("[rifc-dsub-main.js] handleError: " + (JSON.stringify(e) || "(no error message)"));
                }

                /*
                 * RESET
                 */
                $scope.resetState = function () {
                    $state.go('state1').then(function () {
                        $state.reload();
                    });
					if ($scope.studyName == "" && SubmissionStateService.getState().studyName) {
						$scope.studyName = SubmissionStateService.getState().studyName;
					}
					$scope.consoleDebug("[rifc-dsub-main.js] resetState() $scope.studyName: " + $scope.studyName);
                };
            }]);