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
 * SERVICE to store state of main submission page
 */
angular.module("RIF")
        .factory('SubmissionStateService', ['AlertService',
                function (AlertService) {
					var areamap;
                    var s = {
                        //these are on the main disease submission page
                        studyTree: false,
                        comparisonTree: false,
                        investigationTree: false,
                        statsTree: false,
                        studyName: "", //1 input
                        healthTheme: "", //2 drop-down
                        geography: "SAHSU", //3 drop-down
                        numerator: "", //4 drop-down
                        denominator: "", //5 non-editable input
                        //these are in the run-study modal
                        projectName: "",
                        projectDescription: "",
                        studyDescription: "",
                        studyType: "Disease Mapping",
						removeMap: undefined
                    };
                    var defaults = angular.copy(JSON.parse(JSON.stringify(s)));
					
					/*
						+24.3: [DEBUG] [rifs-dsub-submissionstate.js] verifySubmissionState2 SubmissionStateService: {
						 "studyTree": true,
						 "comparisonTree": true,
						 "investigationTree": true,
						 "statsTree": true,
						 "studyName": "1006 LUNG CANCER RA",
						 "healthTheme": {
						  "name": "cancers",
						  "description": "covering various types of cancers",
						  "$$hashKey": "object:231"
						 },
						 "geography": "SAHSULAND",
						 "numerator": {
						  "numeratorTableName": "NUM_SAHSULAND_CANCER",
						  "numeratorTableDescription": "cancer numerator",
						  "denominatorTableName": "POP_SAHSULAND_POP",
						  "denominatorTableDescription": "population health file",
						  "$$hashKey": "object:248"
						 },
						 "denominator": {
						  "numeratorTableName": "NUM_SAHSULAND_CANCER",
						  "numeratorTableDescription": "cancer numerator",
						  "denominatorTableName": "POP_SAHSULAND_POP",
						  "denominatorTableDescription": "population health file",
						  "$$hashKey": "object:248"
						 },
						 "projectName": "",
						 "projectDescription": "",
						 "studyDescription": "TEST 1006 LUNG CANCER BYM 95 96 Risk Analyisis 02 db covariate",
						 "studyType": "Risk Analysis"
						}
					 */
					verifySubmissionState2 = function(strict) {
						var errors=0;
						var stringKeyList;
						if (strict) { // Strict: study name has to exist
							stringKeyList = ['studyName', 'geography', 'numerator', 'studyType'];
						}
						else {
							stringKeyList = ['geography', 'numerator', 'studyType'];
						}
						var objectKeyList = ['healthTheme', 'denominator'];
						for (var i=0; i<stringKeyList.length; i++) {
							if (s[stringKeyList[i]] && s[stringKeyList[i]].length > 0) { // OK
							}
							else {
								AlertService.rifMessage('warning', 'Submission state verification: no string key: ' + 
									stringKeyList[i]);
								errors++;
							}
						}
						for (var i=0; i<objectKeyList.length; i++) {
							if (s[objectKeyList[i]] && typeof(s[objectKeyList[i]]) == "object") { // OK
								var stringKeyList2;
								if (objectKeyList[i] == "healthTheme") {
									stringKeyList2 = ['name', 'description'];
								}
								else {
									stringKeyList2 = ['numeratorTableName', 'numeratorTableDescription', 'denominatorTableName', 'denominatorTableDescription'];
								}
								for (var j=0; j<stringKeyList2.length; j++) {
									if (s[objectKeyList[i]] && s[objectKeyList[i]][stringKeyList2[j]] && 
									    s[objectKeyList[i]][stringKeyList2[j]].length > 0) { // OK
									}
									else {
										AlertService.rifMessage('warning', 'Submission state verification: no string key: ' + 
											stringKeyList2[j] + '" for object key: ' + objectKeyList[i]);
										errors++;
									}
								}
							}
							else {
								AlertService.rifMessage('warning', 'Submission state verification: no object key: ' + 
									objectKeyList[i]);
								errors++;
							}
						}
						
						if (s.studyTree && s.comparisonTree && s.investigationTree && s.statsTree) { // OK
						}
						else {
							AlertService.rifMessage('warning', 'Submission state verification: not all trees complete');
							errors++;
						}
							
						AlertService.consoleDebug('[rifs-dsub-submissionstate.js] verifySubmissionState2 SubmissionStateService: ' +
							JSON.stringify(s, null, 1));		
						if (errors > 0) {
							var err = new Error("Submission state verification failed with " + errors + " error(s)");
							
							AlertService.rifMessage('error', "Submission state verification failed with " + errors + " error(s)", err);
							return false;
						}
						return true;
					}
					
                    return {
						setAreaMap: function (map) {
							areamap=map;
						},
						getAreaMap: function () {
							return areamap;
						},
                        getState: function () {
                            return s;
                        },
                        verifySubmissionState: function (strict) { // Strict: study name has to exist
                            return verifySubmissionState2(strict);
                        },
                        resetState: function () {
							if (s.removeMap) { // Remove Map
								s.removeMap();
							}
                            s = angular.copy(defaults);
                        },
                        setRemoveMap: function (removeMap) {
                            s.removeMap=removeMap;
                        }
                    };
                }]);