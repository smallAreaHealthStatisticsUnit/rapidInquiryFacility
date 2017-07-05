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
 * DIRECTIVE to get info on a completed study
 */

angular.module("RIF")
        .controller('ModalInfoInstanceCtrl', function ($scope, $uibModalInstance) {
            $scope.close = function () {
                $uibModalInstance.dismiss();
            };
            $scope.submit = function () {
                $uibModalInstance.close();
            };
        })
        .directive('getStudyInfo', ['$uibModal', 'user', '$sce', function ($uibModal, user, $sce) {
                return {
                    restrict: 'A',
                    link: function (scope, element, attr) {

                        var alertScope = scope.$parent.$$childHead.$parent.$parent.$$childHead;
                        scope.summary = null;

                        element.on('click', function (event) {
                            scope.summary = "Fetching Study Information...";
                            
                            var thisStudy = scope.studyID[attr.mapid].study_id;
                            _getAttr = function (v) {
                                return '<attr>' + $sce.trustAsHtml(v) + '</attr></br>';
                            };
                            function rerieveError() {
                                alertScope.showError("Could not get information for study");
                            }

                            user.getHealthCodesForProcessedStudy(user.currentUser, thisStudy).then(function (invx) {
                                var inv = invx.data[0];

                                user.getDetailsForProcessedStudy(user.currentUser, thisStudy).then(function (res) {
                                    var project = '<header>Overview</header><section>Project Name:</section>' + _getAttr(res.data[0][13]) +
                                            '<section>Project Description:</section>' + _getAttr(res.data[0][14]) +
                                            '<section>Submitted By:</section>' + _getAttr(res.data[0][0]) +
                                            '<section>Date:</section>' + _getAttr(res.data[0][3]) +
                                            '<section>Study Name:</section>' + _getAttr(res.data[0][1]) +
                                            '<section>Study Description: "TODO: not returned from DB"</section>' + _getAttr(res.data[0][2]) +
                                            '<section>Geography:</section>' + _getAttr(res.data[0][4]) +
                                            '<section>Study Type:</section>' + _getAttr(res.data[0][5]);

                                    //Study area
                                    project += '<header>Study Area</header>' +
                                            '<section>Resolution of Results:</section>' + _getAttr(res.data[0][12]);

                                    //Comparision area
                                    project += '<header>Comparison Area</header>' +
                                            '<section>Resolution of Results:</section>' + _getAttr(res.data[0][6]);

                                    //Investigations
                                    project += '<header>Investigations</header>';
                                    project += '<section>Health Theme:</section>' + _getAttr("TODO: not returned from DB") +
                                            '<section>Numerator Table:</section>' + _getAttr(res.data[0][19]) +
                                            '<section>Denominator Table:</section>' + _getAttr(res.data[0][7]);

                                    //Table
                                    var studyTable = "<table><tr>" +
                                            "<th>Title</th>" +
                                            "<th>Identifier</th>" +
                                            "<th>Description</th>" +
                                            "<th>Years</th>" +
                                            "<th>Sex</th>" +
                                            "<th>Age Range</th>" +
                                            "<th>Covariates</th>" +
                                            "</tr>";

                                    user.getAgeGroups(user.currentUser, res.data[0][4], res.data[0][19]).then(function (resAge) {
                                        for (var j = 0; j < inv.length; j++) {
                                            if (j === 0) {
                                                //match age group limits to fieldname
                                                var lwr = resAge.data[0].name[res.data[0][11]];
                                                var upr = resAge.data[0].name[res.data[0][10]];
                                                
                                                studyTable += "<tr><td>" + res.data[0][17] + "</td><td>" +
                                                        inv[j] + "</td>" +
                                                        "<td>" + inv[j] + "</td>" +
                                                        "<td>" + res.data[0][8] + "-" + res.data[0][9] + "</td>" +
                                                        "<td>" + res.data[0][18] + "</td>" +
                                                        "<td> LWR: " + lwr + ", UPR: " + upr + "</td>" +
                                                        "<td>" + function () {
                                                            if (res.data[0][16]) {
                                                                return res.data[0][16];
                                                            } else {
                                                                return "None";
                                                            }
                                                        }() + "</td>" +
                                                        "</tr>";
                                            } else {
                                                studyTable += "<tr><td></td>" + "</td><td>" +
                                                        inv[j] + "</td>" +
                                                        "<td>" + inv[j] + "</td>" +
                                                        "</tr>";
                                            }
                                        }
                                        project += studyTable + "</table>";

                                        //Statistics
                                        project += '<header>Statistics</header>';
                                        project += '<section>Calculation Method:</section>' + _getAttr(res.data[0][15]);

                                        scope.summary = $sce.trustAsHtml("NOTE: This Information is a work in progress (RIF developers)" + project);
                                    });
                                }, rerieveError);
                            }, rerieveError);

                            var modalInstance = $uibModal.open({
                                animation: true,
                                templateUrl: 'utils/partials/rifp-util-info.html',
                                controller: 'ModalInfoInstanceCtrl',
                                windowClass: 'summary-Modal',
                                backdrop: 'static',
                                scope: scope,
                                keyboard: false
                            });
                        });
                    }
                };
            }]);