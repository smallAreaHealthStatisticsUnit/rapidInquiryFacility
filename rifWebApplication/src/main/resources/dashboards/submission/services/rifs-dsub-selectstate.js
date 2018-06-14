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
        .factory('SelectStateService',
                function () {
					// check if an element exists in array using a comparer function
					// comparer : function(currentElement)
					Array.prototype.inArray = function(comparer) { 
						for(var i=0; i < this.length; i++) { 
							if(comparer(this[i])) return true; 
						}
						return false; 
					}; 

					// adds an element to the array if it does not already exist using a comparer 
					// function
					Array.prototype.pushIfNotExist = function(element, comparer) { 
						if (!this.inArray(comparer)) {
							this.push(element);
						}
					};

                    var s = {
                        studyType: "Disease Mapping",
						studySelection: {			
							studySelectAt: undefined,
							studySelectedAreas: [],
							comparisonSelectAt: undefined,
							comparisonSelectedAreas: []
						}
                    };
                    var t = {
                        studyType: "Risk Analysis",
						studySelection: {			
							studySelectAt: undefined,
							studySelectedAreas: [],
							riskAnalysisType: 12, 	// assume point sources, many areas, one to six bands
													// Can come from shapefile points or by manual entry
							points: [],
							shapes: [],
//
// Risk analysis study types (as per rif40_studies.stype_type): 
//
// 11 - Risk Analysis (many areas, one band), 
// 12 - Risk Analysis (point sources, many areas, one to six bands), 
// 13 - Risk Analysis (exposure covariates), 
// 14 - Risk Analysis (coverage shapefile), 
// 15 - Risk Analysis (exposure shapefile)
							comparisonSelectAt: undefined,
							comparisonSelectedAreas: []
						}
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

						if (newStudySelection.comparisonSelectAt) {
							if (!newStudySelection.comparisonSelectedAreas) {
								throw new Error("rifs-dsub-selectstate.js(): comparisonSelectedAreas key not found, got: " +
									Object.keys(newStudySelection).join(", "));
							}		
							else if (newStudySelection.comparisonSelectedAreas.length < 1) {
								throw new Error("rifs-dsub-selectstate.js(): at least one comparisonSelectedAreas required");
							}									
						}
						
						if (newStudySelection.studySelectAt) {
							if (!newStudySelection.studySelectedAreas) {
								throw new Error("rifs-dsub-selectstate.js(): studySelectedAreas key not found, got: " +
									Object.keys(newStudySelection).join(", "));
							}			
							else if (newStudySelection.studySelectedAreas.length < 1) {
								throw new Error("rifs-dsub-selectstate.js(): at least one studySelectedAreas required");
							}								
						}
						else {
							throw new Error("rifs-dsub-selectstate.js(): studySelectAt not found");
						}
							
						if (newStudyType === undefined) {
							newStudyType=studyType;
						}						
						else if (newStudyType == "disease_mapping_study") {

						}
						else if (newStudyType == "risk_analysis_study") {

						}
						else {
							throw new Error("rifs-dsub-selectstate.js(): unexpected study type: " + 
								newStudyType);
						}		
						
						return newStudySelection;
					}
					
                    return {
                        getState: function () {
                            return s;
                        },
                        resetState: function () {
                            s = angular.copy(diseaseMappingDefaults);
                        },
						initialiseRiskAnalysis() {
                            s = angular.copy(riskAnalysisDefaults);
						},
						setStudySelection: function(newStudySelection, newStudyType) { // Needs to verify
							studySelection=verifyStudySelection2(newStudySelection, newStudyType);
							if (newStudyType === "Disease Mapping") {		
								studyType=newStudyType;
							}
							else if (newStudyType === "Risk Analysis") {	
								studyType=newStudyType;
							}
							else {
								throw new Error("rifs-dsub-selectstate.js(): unexpected study type: " + newStudyType);
							}
						},
						verifyStudySelection: function() {
							var r;
							if (s === undefined) {
								throw new Error("rifs-dsub-selectstate.js(): s is undefined");
							}
							else if (s.studyType === "Disease Mapping") {		
								r=verifyStudySelection2(s.studySelection, "disease_mapping_study");
							}
							else if (s.studyType === "Risk Analysis") {	
								r=verifyStudySelection2(s.studySelection, "risk_analysis_study");
							}
							else {
								throw new Error("rifs-dsub-selectstate.js(): unexpected study type: " + newStudyType);
							}
							return r;
						}
                    };
                });
               