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
 * SERVICE to store state of viewer tab
 */
angular.module("RIF")
        .factory('ViewerStateService', ['AlertService', 
                function (AlertService) {
                    //These are the relevant columns to display from the results table
                    var diseaseMapValidColumns = ["area_id", "band_id", "observed", "expected", "population", "adjusted", "inv_id",
                        "posterior_probability",
                        "lower95", "upper95", "relative_risk",
                        "smoothed_smr", "smoothed_smr_lower95", "smoothed_smr_upper95",
                        "_selected"];
                    var riskAnalysisValidColumns = ["area_id", "band_id", "observed", "expected", "population", "adjusted", "inv_id",
                        "lower95", "upper95", "relative_risk",
                        "_selected"];
                    var s = {
                        initial: true,
                        center: {
                            'viewermap': {'zoom': 1, 'lng': 0, 'lat': 0}
                        },
                        area_id: {
                            'viewermap': null
                        },
                        study: {
                            'viewermap': {'study_id': null, 'name': null}
                        },
                        sex: {
                            'viewermap': null
                        },
                        studyType: {
                            'viewermap': "Disease Mapping"
                        },
                        selected: {
                            'viewermap': []
                        },
                        transparency: {
                            'viewermap': 0.7
                        },
                        vSplit1: 33,
                        hSplit1: 40,
                        hSplit2: 60,
						removeMap: undefined
                    };
                    var defaults = angular.copy(JSON.parse(JSON.stringify(s)));
                    return {
						getValidColumnList: function (mapID, studyType) {
							if (studyType == "Disease Mapping") {
								return diseaseMapValidColumns;
							}
							else if (studyType == "Risk Analysis") {
								return riskAnalysisValidColumns;
							}
							else {
								throw new Error("Invalid studyType: " + studyType + " for map: " + mapID);
							}
						},
                        getValidColumn: function (header, studyType, mapID) {
							var validColumns;
							if (studyType == "Disease Mapping") {
								validColumns = diseaseMapValidColumns;
							}
							else if (studyType == "Risk Analysis") {
								validColumns = riskAnalysisValidColumns;
							}
							else {
								throw new Error("Invalid studyType: " + studyType + " for map: " + mapID + "; column: " + header);
							}
//							AlertService.consoleDebug("[rifs-view-viewerstate.js] getValidColumn studyType: " + studyType + 
//								" for map: " + mapID + "; column: " + header);
                            if (validColumns && validColumns.indexOf(header) !== -1) {
                                return true;
                            } else {
                                return false;
                            }
                        },
                        getState: function () {
                            return s;
                        },
                        setRemoveMap: function (removeMap) {
                            s.removeMap=removeMap;
                        },
                        resetState: function () {
							if (s.removeMap) { // Remove Map
								s.removeMap();
							}
                            s = angular.copy(defaults);
                        }
                    };
                }]);