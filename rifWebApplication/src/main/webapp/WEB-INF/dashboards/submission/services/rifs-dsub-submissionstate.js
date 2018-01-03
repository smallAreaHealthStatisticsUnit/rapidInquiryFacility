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
        .factory('SubmissionStateService', 
                function () {
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
                });