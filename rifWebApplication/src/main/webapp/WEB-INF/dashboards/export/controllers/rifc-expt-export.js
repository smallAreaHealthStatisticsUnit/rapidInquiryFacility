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
 * CONTROLLER for data export
 */

/* global L, key, topojson, d3 */
angular.module("RIF")
        .controller('ExportCtrl', ['$scope', 'user',
            function ($scope, user) {

                $scope.studyIDs = [];
                $scope.studyID = {
                    "exportmap": ""
                };

                $scope.getStudies = function () {
                    user.getCurrentStatusAllStudies(user.currentUser).then(function (res) {
                        for (var i = 0; i < res.data.smoothed_results.length; i++) {
                            if (res.data.smoothed_results[i].study_state === "R") {
                                var thisStudy = {
                                    "study_id": res.data.smoothed_results[i].study_id,
                                    "name": res.data.smoothed_results[i].study_name
                                };
                                $scope.studyIDs.push(thisStudy);
                            }
                        }
                        //sort array on ID with most recent first
                        $scope.studyIDs.sort(function (a, b) {
                            return parseFloat(a.study_id) - parseFloat(b.study_id);
                        }).reverse();

                        //TODO: defaults
                        $scope.studyID["exportmap"] = $scope.studyIDs[0];

                    }, function (e) {
                        $scope.showError("Could not retrieve study status");
                    });
                };

                //update study list if new study processed
                $scope.$on('updateStudyDropDown', function (event, thisStudy) {
                    $scope.studyIDs.push(thisStudy);
                });
                $scope.getStudies();

                //export query, map and extract tables as a Zip File
                $scope.exportAllTables = function () {
                    $scope.showSuccess("Export started...");
                    user.getZipFile(user.currentUser, $scope.studyID["exportmap"].study_id).then(function (res) {
                        if (res.data === "") {
                            $scope.showSuccess("Export finished: " + $scope.studyID["exportmap"].name + " please check your defined extract directory");
                        } else {
                            $scope.showError("Error exporting study tables");
                        }
                    });
                };




            }]);