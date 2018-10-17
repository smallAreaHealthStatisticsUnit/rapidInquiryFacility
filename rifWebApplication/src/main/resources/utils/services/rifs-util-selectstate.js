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
 
 * Peter Hambly
 * @author phambly
 */

/* 
 * SERVICE to store state of selection modal
 */
angular.module("RIF")
        .factory('SelectStateService', ['$rootScope', 'AlertService', 'user',
                function ($rootScope, AlertService, user) {

                    var s = {
                        studyType: "disease_mapping_study",
						studySelection: {			
							studySelectAt: undefined,
							studySelectedAreas: [],
							studyShapes: [],
							comparisonSelectAt: undefined,
							comparisonSelectedAreas: [],
							fileList: [],
							bandAttr: []
						},
						showHideCentroids: false,
						showHideSelectionShapes: true
                    };
                    var t = {
                        studyType: "risk_analysis_study",
						studySelection: {			
							studySelectAt: undefined,
							studySelectedAreas: [],
							riskAnalysisType: 12, 	// assume point sources, many areas, one to six bands
													// Can come from shapefile points or by manual entry
							riskAnalysisDescription: getriskAnalysisDesription2(),
							studyShapes: [],
							comparisonShapes: [],
							
//
// Risk analysis study types (as per rif40_studies.stype_type): 
//
// 11 - Risk Analysis (many areas, one band), 
// 12 - Risk Analysis (point sources, many areas, one to six bands) [DEFAULT], 
// 13 - Risk Analysis (exposure covariates), 
// 14 - Risk Analysis (coverage shapefile), 
// 15 - Risk Analysis (exposure shapefile)
							comparisonSelectAt: undefined,
							comparisonSelectedAreas: [],
							fileList: [],
							bandAttr: []
						},
						showHideCentroids: false,
						showHideSelectionShapes: true
                    };
					
                    var defaults = angular.copy(JSON.parse(JSON.stringify(s)));
					var diseaseMappingDefaults = angular.copy(JSON.parse(JSON.stringify(defaults)));
                    var riskAnalysisDefaults = angular.copy(JSON.parse(JSON.stringify(t)));
					
					function verifyStudySelection2(newStudySelection, newStudyType) {
						
						if (newStudySelection === undefined) {
							throw new Error("rifs-dsub-selectstate.js(): newStudySelection is undefined");
						}
						if (Object.keys(newStudySelection) === undefined) {
							throw new Error("rifs-dsub-selectstate.js(): newStudySelection has no keys");
						}						
						if (newStudyType === undefined) {
							throw new Error("rifs-dsub-selectstate.js(): newStudyType is undefined");
						}
						
 						if (Object.keys(newStudySelection).length < 2 && Object.keys(newStudySelection).length > 4) {
							throw new Error("rifs-dsub-selectstate.js(): expecting 2 to 4 newStudySelection keys got " +
								Object.keys(newStudySelection).length + ": " +
								Object.keys(newStudySelection).join(", "));
						}	

						// Initialise if not set
						if (newStudySelection.comparisonShapes == undefined) {
							newStudySelection.comparisonShapes = [];
						}
						if (newStudySelection.studyShapes == undefined) {
							newStudySelection.studyShapes = [];
						}
						if (newStudySelection.fileList == undefined) {
							newStudySelection.fileList = [];
						}
						if (newStudySelection.bandAttr == undefined) {
							newStudySelection.bandAttr = [];
						}
							
						if (newStudySelection.studySelectAt && newStudySelection.comparisonSelectAt) {
							if (!newStudySelection.studySelectedAreas) {
								AlertService.consoleDebug("rifs-dsub-selectstate.js(): studySelectedAreas key not found, got: " +
									Object.keys(newStudySelection).join(", "));
										throw new Error("no study areas selected");
							}	
							else if (newStudySelection.studySelectedAreas) {
								if (!newStudySelection.comparisonSelectedAreas) {
									AlertService.consoleDebug("rifs-dsub-selectstate.js(): comparisonSelectedAreas key not found, got: " +
										Object.keys(newStudySelection).join(", "));
										throw new Error("no comparison areas selected");
								}								
							}	
							
							if (newStudySelection.studySelectedAreas.length == 0 &&
							    newStudySelection.comparisonSelectedAreas.length == 0) { // Pre alter 10 study
								AlertService.consoleDebug("[rifs-dsub-selectstate.js] newStudySelection: " +
									JSON.stringify(newStudySelection, null, 1));
								throw new Error("upload no longer supported for pre-alter 10 studies");
								// This is because studySelectAt and the selectedPolygon array are probably incompatible 
								// (i.e. at different geolevels)
							}							
							else if (newStudySelection.studySelectedAreas.length <1) {
								AlertService.consoleDebug("[rifs-dsub-selectstate.js] newStudySelection: " + JSON.stringify(newStudySelection, 1));
								throw new Error("at least one study area required");
							}						
//							else if (newStudySelection.comparisonSelectedAreas.length < 1) { // Not necessarily; may be derived from older study
//								throw new Error("at least one comparison area required");
//							}					
						}
						else if (newStudySelection.studySelectAt == undefined && newStudySelection.comparisonSelectAt == undefined) {
							AlertService.consoleDebug("[rifs-dsub-selectstate.js] studySelectAt and comparisonSelectAt not found; newStudySelection: " + 
								JSON.stringify(newStudySelection, 1));		
							throw new Error("study selection and comparison resolution not setup correctly");
						}
						else if (newStudySelection.studySelectAt == undefined && newStudySelection.comparisonSelectAt) {
							AlertService.consoleDebug("[rifs-dsub-selectstate.js] studySelectAt not found; newStudySelection: " + 
								JSON.stringify(newStudySelection, 1));		
							throw new Error("study selection resolution not setup correctly");
						}
						else if (newStudySelection.studySelectAt && newStudySelection.comparisonSelectAt == undefined) {
							AlertService.consoleDebug("[rifs-dsub-selectstate.js] comparisonSelectAt not found; newStudySelection: " + 
								JSON.stringify(newStudySelection, 1));		
							throw new Error("study comparison resolution not setup correctly");
						}
							
						if (newStudyType == "disease_mapping_study") {

						}
						else if (newStudyType == "risk_analysis_study") {

						}
						else {
							throw new Error("unexpected study type: " + newStudyType);
						}		
						
						return newStudySelection;
					}
					
					function getriskAnalysisDesription2() {
						var r=undefined;
						
						if (s && s.studySelection && s.studySelection.riskAnalysisType) {
							switch(s.studySelection.riskAnalysisType) {
								case 11:
									r='Risk Analysis (many areas, one band)';
									break;
								case 12:
									r='Risk Analysis (point sources)';
									break;
								case 13:
									r='Risk Analysis (exposure covariates)';
									break;
								case 14:
									r='Risk Analysis (coverage shapefile)';
									break;
								case 15:
									r='Risk Analysis (exposure shapefile)';
									break;
							}
						}
						
						return r;
					}
					
					function getStudySelection2(mapID, studyId, getStudySelectionCallback) {
						
						var dbStudySelection;
						
						if (getStudySelectionCallback && typeof(getStudySelectionCallback) == "function") { // OK
						}
						else {
							throw new Error("rifs-dsub-selectstate.js(): invalid callback for map: " + mapID + "; studyId: " + studyId);
						}
								
						user.getSelectState(user.currentUser, studyId).then(function (res) {
							
							dbStudySelection=res.data.select_state;
							s=(dbStudySelection||defaults);
							s.showHideSelectionShapes=true;
							s.showHideCentroids=false;
							if (dbStudySelection && dbStudySelection.riskAnalysisDescription && dbStudySelection.riskAnalysisType) {
								s.studyType="risk_analysis_study";
							} 
							else {
								s.studyType="disease_mapping_study";
							}
							getStudySelectionCallback(undefined /* no error */, mapID, dbStudySelection);
						}, function (err) {
							if (err) {
								AlertService.consoleLog("[rifs-dsub-selectstate.js] getStudySelection2() user.getSelectState err: " + 
									JSON.stringify(err, null, 1));
							}
							getStudySelectionCallback("Error in getSelectState REST call" /* error */, mapID, dbStudySelection);
						});
					}
					
                    return {
                        getState: function () {
							if (s.studySelection && !s.studySelection.riskAnalysisDescription) {
								s.studySelection.riskAnalysisDescription=getriskAnalysisDesription2();
							}
//							AlertService.consoleDebug("[rifs-dsub-selectstate.js] getState(): " + JSON.stringify(s, null, 1));
                            return s;
                        },
                        resetState: function () {
							AlertService.consoleDebug("[rifs-dsub-selectstate.js] resetState()");
                            s = angular.copy(diseaseMappingDefaults);
                        },
						initialiseRiskAnalysis: function() {
							AlertService.consoleDebug("[rifs-dsub-selectstate.js] initialiseRiskAnalysis()");
                            s = angular.copy(riskAnalysisDefaults);
						},
						setStudySelection: function(newStudySelection, newStudyType) { // Needs to verify
							try {
								studySelection=verifyStudySelection2(newStudySelection, newStudyType);
							}
							catch(e) {
								AlertService.consoleError("[rifs-dsub-selectstate.js] verification issue: " + e.message, e);
								studySelection=newStudySelection;			
							}
							finally {
								if (studySelection && studySelection.studySelectedAreas && studySelection.studyShapes && studySelection.comparisonShapes) {
									AlertService.consoleDebug("[rifs-dsub-selectstate.js] set verified study selection: " + newStudyType + 
										"; studySelectAt: " + studySelection.studySelectAt +
										"; studySelectedAreas: " + studySelection.studySelectedAreas.length +
										", riskAnalysisType: " + studySelection.riskAnalysisType + 
										", studyShapes: " + studySelection.studyShapes.length +
										", comparisonShapes: " + studySelection.comparisonShapes.length);
								}
								else {
									AlertService.consoleDebug("[rifs-dsub-selectstate.js] set partially verified study selection: " + newStudyType + 
										"; studySelection: " + JSON.stringify(studySelection, null, 1));
								}
						
								if (newStudyType == undefined) {
									throw new EnewStudyTyperror("rifs-dsub-selectstate.js(): newStudyType is undefined");
								}
								else if (newStudySelection.studyType && newStudySelection.studyType != newStudyType) {
									throw new EnewStudyTyperror("rifs-dsub-selectstate.js(): newStudySelection study type: " + newStudyType +
										" does not match newStudyType: " + newStudyType);
								}
								
								s.studySelection=studySelection;
								if (newStudyType === "disease_mapping_study") {		
									s.studyType=newStudyType;
								}
								else if (newStudyType === "risk_analysis_study") {	
									s.studyType=newStudyType;
								}
								else {
									throw new Error("rifs-dsub-selectstate.js(): unexpected study type: " + newStudyType);
								} 
							}
						},
						verifyStudySelection: function() {
							var r;
							if (s === undefined) {
								throw new Error("rifs-dsub-selectstate.js(): s is undefined");
							}
							else if (s.studyType === undefined) {
								throw new Error("rifs-dsub-selectstate.js(): s.studyType is undefined");
							}
							else if (s.studyType === "disease_mapping_study") {		
								r=verifyStudySelection2(s.studySelection, "disease_mapping_study");
							}
							else if (s.studyType === "risk_analysis_study") {	
								r=verifyStudySelection2(s.studySelection, "risk_analysis_study");
							}
							else {
								throw new Error("rifs-dsub-selectstate.js(): unable to verify study type: " + s.studyType);
							}
							return r;
						},
						getriskAnalysisDesription: function() {
							return getriskAnalysisDesription2();
						},
						getStudySelection: function(mapID, studyId, getStudySelectionCallback) {
							getStudySelection2(mapID, studyId, getStudySelectionCallback);
						}
                    };
                }]);
               