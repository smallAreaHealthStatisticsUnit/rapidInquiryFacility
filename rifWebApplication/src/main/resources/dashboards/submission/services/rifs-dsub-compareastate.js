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
 * SERVICE to store state of comparison area modal
 */
angular.module("RIF")
        .factory('CompAreaStateService', ['AlertService',
                function (AlertService) {
                    var s = {
                        polygonIDs: [],
                        selectAt: "",
                        studyResolution: "",
                        center: {'zoom': 1, 'lng': 0, 'lat': 0},
                        transparency: 0.7,
                        geography: ""
                    };
                    var defaults = angular.copy(JSON.parse(JSON.stringify(s)));
                    return {
                        getState: function () {
							if (s.removeMap) { // Remove Map
								s.removeMap();
							}
                            return s;
                        },
                        resetState: function () {
                            s = angular.copy(defaults);
							// Set method do deep copy to avoid scope issues
							s.setGeography = function(ngeography) {
								AlertService.consoleLog("[rifs-dsub-studyareastate.js] setGeography: " + ngeography);
								this.geography = angular.copy(ngeography);
							};
							s.setStudyResolution = function(nstudyResolution) {
								AlertService.consoleLog("[rifs-dsub-compareastate.js] setStudyResolution: " + nstudyResolution);
								this.studyResolution = angular.copy(nstudyResolution);
							};
							s.setSelectAt = function(nselectAt) {
								AlertService.consoleLog("[rifs-dsub-compareastate.js] setSelectAt: " + nselectAt);
								this.selectAt = angular.copy(nselectAt);
							};
							s.setPolygonIDs = function(npolygonIDs) {
								AlertService.consoleLog("[rifs-dsub-compareastate.js] setPolygonIDs: " + npolygonIDs.length);
								this.polygonIDs = angular.copy(npolygonIDs);
							};
							s.setCenter = function(ncenter) {
								AlertService.consoleLog("[rifs-dsub-compareastate.js] setCenter: " + JSON.stringify(ncenter));
								this.center = angular.copy(ncenter);
							};	
							s.setTransparency = function(ntransparency) {
								AlertService.consoleLog("[rifs-dsub-compareastate.js] setTransparency: " + ntransparency);
								this.transparency = angular.copy(ntransparency);
							};							
                        }
                    };
                }]);