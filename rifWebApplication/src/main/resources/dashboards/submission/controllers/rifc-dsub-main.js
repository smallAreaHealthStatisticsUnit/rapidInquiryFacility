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
            'Rif40NumDenomService',
            function ($scope, user, $state, SubmissionStateService, 
				StudyAreaStateService, CompAreaStateService, ParameterStateService, SelectStateService,
                Rif40NumDenomService) {
                /*
                 * STUDY, GEOGRAPHY AND FRACTION DROP-DOWNS
                 * Calls to API returns a chain of promises
                 */
                if (!Rif40NumDenomService.checkInitialised) {
                    $state.go('state0').then(function () {
                        $scope.showError("Rif40NumDenomService is not initialised");
                    });
                }
                else {
                    $scope.rif40NumDenom=Rif40NumDenomService.getRif40NumDenom();
                    $scope.fraction = {};
                    $scope.denominator = "";
                    $scope.geographies = [];
                    $scope.fractions = [];	
                    $scope.fraction = "";	
                    $scope.studyName = "";	
                    $scope.geography = "";		
                    $scope.geographyDescription = "";
                    $scope.healthThemes = [];
                    $scope.healthTheme = ""
                    if ($scope.rif40NumDenom && $scope.rif40NumDenom.themes && $scope.rif40NumDenom.geographies) {
                        $scope.healthThemes = $scope.rif40NumDenom.themes; // Keys name, description
                        var found=false;
                        if (SubmissionStateService.getState().healthTheme &&
                            SubmissionStateService.getState().healthTheme.name) {

                            for (var i=0; i<$scope.healthThemes.length; i++) {
                                if ($scope.healthThemes[i].name == SubmissionStateService.getState().healthTheme.name) {
                                    $scope.healthTheme = $scope.healthThemes[i];
                                    found=true;
                                }
                            }
                        }
                        if (!found) {
                            $scope.consoleDebug("[rifc-dsub-main.js] Load default $scope.fraction, $scope.fractions etc; SubmissionStateService: " +
                                JSON.stringify(SubmissionStateService.getState(), null, 1) +
                                "; $scope.healthThemes: " + JSON.stringify($scope.healthThemes, null, 1));
                            $scope.healthTheme = $scope.healthThemes[0];
                            SubmissionStateService.getState().healthTheme = $scope.healthTheme;
                        }
                        else {          
                           $scope.consoleDebug("[rifc-dsub-main.js] Load $scope.fraction, $scope.fractions etc from SubmissionStateService: " +
                                JSON.stringify(SubmissionStateService.getState(), null, 1) +
                                "; $scope.healthThemes: " + JSON.stringify($scope.healthThemes, null, 1));
                        }
                        setupTheme($scope.healthTheme.description);
                    }
                    else {
                        $state.go('state0').then(function () {
                            $scope.showError("Rif40NumDenomService has no themes or geographies");
                        });
                    }
                }
            				
                /*
                 * STUDY NAME
                 */
                $scope.studyNameChanged = function () {
                    SubmissionStateService.getState().studyName = $scope.studyName;
                };

                $scope.geographyChange = function (nGeography) {
                    geographyChange(nGeography);
                };
                
                //Get relevant numerators and associated denominators
                $scope.healthThemeChange = function () {
                    if ($scope.healthTheme && $scope.healthTheme.description) {
                        setupTheme($scope.healthTheme.description);
                        SubmissionStateService.getState().healthTheme = $scope.healthTheme;
/*                        user.getNumerator(user.currentUser, $scope.geography, $scope.healthTheme.description).then(
							function (res) { handleFractions(res); }, 
							function (err) { handleError(err, "unable to get numerator/denominator pair"); }); //3RD PROMISE
                    } else {
                        $scope.fractions.length = 0; */
                    } 
                    else {
                        $scope.showError("Cannot get geography and numerator data for theme: " + themeDescription);
                    }
                };
                
                //sync the denominator
                $scope.fractionChange = function () {
                    if ($scope.fraction) {
                        $scope.denominator = $scope.fraction.denominatorTableName;
                    } else {
                        $scope.denominator = "";
                    }
					
					if (!compareFractions(SubmissionStateService.getState().fraction, $scope.fraction)) {
					
						$scope.consoleDebug("[rifc-dsub-main.js] numeratorChange(), reset investigation parameters, $scope.fraction: " + 
							JSON.stringify($scope.fraction, null, 1) +
							"; old SubmissionStateService.getState().fraction: " + 
                            JSON.stringify(SubmissionStateService.getState().fraction, null, 1));
						SubmissionStateService.getState().fraction = $scope.fraction;
						//This will have an impact on investigations year range, so reset investigation parameters
						ParameterStateService.resetState();
					}
					else {		
						$scope.consoleDebug("[rifc-dsub-main.js] numeratorChange(), no change: " + JSON.stringify($scope.fraction, null, 2));
					}	
                };
				
                function geographyChange(nGeography) {
                    if (nGeography && $scope.geography && 
                        ($scope.geography != nGeography ||
                         SubmissionStateService.getState().geography != nGeography)) {
                        SubmissionStateService.getState().geography = nGeography;
                        $scope.geographyDescription = 
                            $scope.rif40NumDenom.geographies[$scope.healthTheme.description].geographyDescriptions[$scope.geography];

                        //reset states using geography
                        StudyAreaStateService.resetState();
                        CompAreaStateService.resetState();
                        SelectStateService.resetState();
                        ParameterStateService.resetState();
                        SubmissionStateService.getState().comparisonTree = false;
                        SubmissionStateService.getState().studyTree = false;
                        SubmissionStateService.getState().investigationTree = false;
                                 
                        $scope.consoleDebug("[rifc-dsub-main.js] resetState() $scope.studyName: " + $scope.studyName);
                    }
                    else if (nGeography && $scope.geography && $scope.geography == nGeography) {
                        $scope.consoleDebug("[rifc-dsub-main.js] no change in geography: " + 
                            "; $scope.geography: " + $scope.geography +
                            "; SubmissionStateService.getState().geography: " + 
                            SubmissionStateService.getState().geography);
                    }
                    else {           
                        if (nGeography) {
                            $scope.geography=nGeography; 
                            $scope.geographyDescription = 
                                $scope.rif40NumDenom.geographies[$scope.healthTheme.description].geographyDescriptions[$scope.geography];

                            //reset states using geography
                            StudyAreaStateService.resetState();
                            CompAreaStateService.resetState();
                            SelectStateService.resetState();
                            ParameterStateService.resetState();
                            SubmissionStateService.getState().comparisonTree = false;
                            SubmissionStateService.getState().studyTree = false;
                            SubmissionStateService.getState().investigationTree = false;
                                     
                            $scope.consoleDebug("[rifc-dsub-main.js] DEFAULT resetState() $scope.studyName: " + $scope.studyName);
                        }
                    }
                    
                    StudyAreaStateService.getState().geography = $scope.geography;
                    SubmissionStateService.getState().geography = $scope.geography;
                    $scope.consoleDebug("[rifc-dsub-main.js] geography: " + $scope.geography);
					setupNumerator($scope.geography);   

					if ($scope.studyName == "" && SubmissionStateService.getState().studyName) {
						$scope.studyName = SubmissionStateService.getState().studyName;
					}              
                }
                
                function setupTheme(themeDescription) {
                    if ($scope.rif40NumDenom && $scope.rif40NumDenom.geographies && 
                        $scope.rif40NumDenom.geographies[themeDescription] && 
                        $scope.rif40NumDenom.geographies[themeDescription].geographyList && 
                        $scope.rif40NumDenom.geographies[themeDescription].geographyList.length > 0) {
                        var theme = $scope.rif40NumDenom.geographies[themeDescription];        
                        $scope.geographies = theme.geographyList;      
                        
                        var found=false;
                        if (SubmissionStateService.getState().geography) {
                            for (var i=0; i<$scope.geographies.length; i++) {
                                if ($scope.geographies[i] == SubmissionStateService.getState().geography) {
                                    $scope.geography = $scope.geographies[i];
                                    found=true;
									break;
                                }
                            }
                        }
                        if (!found) {
                            $scope.consoleDebug("[rifc-dsub-main.js] using default geography: " + $scope.geography +
                                "; $scope.geographies: " + JSON.stringify($scope.geographies) +
                                "; themeDescription: " + themeDescription);
                            $scope.geography = $scope.geographies[0];
                            SubmissionStateService.getState().geography = $scope.geography;
                        }                    
                        $scope.geographyDescription = theme.geographyDescriptions[$scope.geography];
                        
                        geographyChange($scope.geography);
                    }
                    else {
                        $scope.showError("Cannot get initial geography and numerator data for theme: " + themeDescription);
                    }
                }
                
                function setupNumerator(nGeography) {
                    var theme = $scope.rif40NumDenom.geographies[$scope.healthTheme.description]; 
					$scope.fractions = theme[nGeography];

                    $scope.consoleDebug("[rifc-dsub-main.js] setupNumerator(" + nGeography + ")" +
                        "; $scope.fractions: " + JSON.stringify($scope.fractions, null, 1));
					var found=false;
					if (SubmissionStateService.getState().fraction) {
						for (var i=0; i<$scope.fractions.length; i++) {
							if (compareFractions($scope.fractions[i], SubmissionStateService.getState().fraction)) {
								$scope.fraction = $scope.fractions[i];
								$scope.denominator = $scope.fractions[i].denominatorTableName;
								found=true;
								break;
							}
						}
					}
					if (!found) {
						$scope.fraction = $scope.fractions[0];
						$scope.denominator = $scope.fractions[0].denominatorTableName;
						SubmissionStateService.getState().fraction = $scope.fractions[0];
                        
                        $scope.consoleDebug("[rifc-dsub-main.js] using default numerator/denominator: " + 
                            JSON.stringify($scope.fraction, null, 1) + 
                            "; for geography: " + nGeography +
                            "; fractions: " + JSON.stringify($scope.fractions, null, 1) +
                            "; SubmissionStateService.getState().fraction: " + 
                            JSON.stringify(SubmissionStateService.getState().fraction, null, 1));
					}    
                    else {
                        $scope.consoleDebug("[rifc-dsub-main.js] using SubmissionStateService numerator/denominator: " + 
                            JSON.stringify($scope.fraction, null, 1) + 
                            "; for geography: " + nGeography +
                            "; fractions: " + JSON.stringify($scope.fractions, null, 1) +
                            "; SubmissionStateService.getState().fraction: " + 
                            JSON.stringify(SubmissionStateService.getState().fraction, null, 1));
                    }    
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

                /*
                 * RESET: called from rifc-dsub-fromfile.js
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