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

//TODO: LOTS TO SORT OUT HERE IN BACKEND FIRST

angular.module("RIF")
        .controller('ModalInfoInstanceCtrl', function ($scope, $uibModalInstance) {
            $scope.close = function () {
                $uibModalInstance.dismiss();
            };
            $scope.submit = function () {
                $uibModalInstance.close();
            };
        })
        .directive('getStudyInfo', ['$uibModal', 'user', '$sce', 'AlertService', function ($uibModal, user, $sce, AlertService) {
                return {
                    restrict: 'A',
                    link: function (scope, element, attr) {

                        var alertScope = scope.$parent.$$childHead.$parent.$parent.$$childHead;
                        scope.covariateChange= function (covariateType) {
                            scope.covariateType = covariateType;
                            AlertService.consoleDebug("[rifd-util-info.js] covariateChange(): " + scope.covariateType);
                            scope.showCovariateLossCovariate[scope.covariateType] = true;
                            /* JS way;  also class="ng-hide"
                            var id="utilInfo" + covariateType;
                            var x = document.getElementById(id);
                            if (x) {
                                if (x.style.display === "none") {
                                    x.style.display = "block";
                                } 
                                else {
                                    x.style.display = "none";
                                }
                            } */
                        }
                        scope.reportChange = function (reportType) {
                            scope.reportType = reportType;
                            AlertService.consoleDebug("[rifd-util-info.js] repoortChange(): " + scope.reportType);
                            if (scope.reportType) {
                                if (scope.reportType == "Summary") {
                                    scope.showSummary=true;
                                    scope.showCovariateLossReport=false;
                                    scope.showHomogeneityTests=false;
                                }
                                else if (scope.reportType == "Covariate Loss Report") {
                                    scope.showSummary=false;
                                    scope.showCovariateLossReport=true;
                                    scope.showHomogeneityTests=false;
                                }
                                else if (scope.reportType == "Homogeneity Tests") {
                                    scope.showSummary=false;
                                    scope.showCovariateLossReport=false;
                                    scope.showHomogeneityTests=true;
                                }
                            }
                            else {
                                scope.reportType = "Summary";
                                scope.showSummary=true;
                                scope.showCovariateLossReport=false;
                                scope.showHomogeneityTests=false;
                            }
                            scope.reportTitle='Study ' + scope.reportType;
                            scope.reportDescription=scope.reportTitle;
                        }

                        element.on('click', function (event) {
                            scope.mapType = undefined;
                            scope.mapDefs = undefined;
                            scope.summary = undefined;
                            scope.covariateLossReport = null;
                            scope.homogeneityTests = null;
                            scope.reportType = "Summary";
                            scope.reportTitle='Study ' + scope.reportType;
                            scope.reportDescription=scope.reportTitle;
                            scope.showSummary=true;
                            scope.showCovariateLossReport=false;
                            scope.showHomogeneityTests=false;
                            scope.covariateList = [];
                            scope.covariateType = null;
                            scope.covariateDescription = null;
                            scope.covariateDescriptions = {};
                            scope.isRiskAnalysisStudy=false;
                            scope.hasCovariates=false;
                            scope.studyType="Disease Mapping";
                            scope.showCovariateLossCovariate = {};
                        
                            if (scope.myMaps) {
                                scope.mapType=scope.myMaps[0]; // E.g. viewermap
                            }
                            if (scope.mapType && scope.studyID && scope.studyID[scope.mapType]) {
                                scope.mapDefs=scope.studyID[scope.mapType]; 
                                /* name: "1006 LUNG CANCER RA" ​​​​​
                                   riskAnalysisDescription: "Risk Analysis (point sources)" ​​​​​
                                   study_id: "55";
                                   study_type: "Risk Analysis" */
                            }
                            if (scope.mapDefs && scope.mapDefs.study_type && scope.mapDefs.study_type == "Risk Analysis") {
                                scope.isRiskAnalysisStudy=true;
                                scope.studyType=scope.mapDefs.study_type;
                                if (scope.mapDefs.riskAnalysisDescription) {
                                    scope.studyType=scope.mapDefs.riskAnalysisDescription;
                                }
                                scope.reportList = ["Summary", "Covariate Loss Report", "Homogeneity Tests"];
                            }
                            else {
                                scope.reportList = ["Summary", "Covariate Loss Report"];
                            }
                        
                            scope.summary = '<header class="info-header">Fetching Study Information for ' + scope.studyType.toLowerCase() + " study " + 
                               ((scope.mapDefs && scope.mapDefs.study_id) ? scope.mapDefs.study_id : "Unknown") + 
                                '...</header>';
                            scope.covariateLossReport = '<header class="info-header">Fetching Study Covariate Loss Report for ' + scope.studyType.toLowerCase() + " study " + 
                               ((scope.mapDefs && scope.mapDefs.study_id) ? scope.mapDefs.study_id : "Unknown") + 
                                '...</header>';
                            scope.homogeneityTests = '<header class="info-header">Fetching Study Homogeneity Tests for ' + scope.studyType.toLowerCase() + " study " + 
                               ((scope.mapDefs && scope.mapDefs.study_id) ? scope.mapDefs.study_id : "Unknown") + 
                                '...</header>';
                            
                            var homogeneityTestsHtml = '<header>Homogeneity Tests &ndash; ' + 
                                ((scope.mapDefs && scope.mapDefs.riskAnalysisDescription) ? scope.mapDefs.riskAnalysisDescription : "No risk analysis description") +
                                ' &ndash; ' +
                                ((scope.mapDefs && scope.mapDefs.name) ? scope.mapDefs.name : "No name") + 
                                '</header>';
                            var covariateLossReportHtml = '<header>Covariate Loss Report &ndash; ' +
                                ((scope.mapDefs && scope.mapDefs.name) ? scope.mapDefs.name : "No name") + 
                                '</header>';
                            
                            var thisStudy = scope.studyID[attr.mapid].study_id;
                            _getAttr = function (v) {
                                return '<attr>' + $sce.trustAsHtml(v) + '</attr></br>';
                            };
                            function retrieveError(err, functionName) {
                                if (functionName) {
                                    alertScope.showError("Could not get information for study: " + functionName + " failed; " +
                                        (err ? err : "no error specified"));
                                }
                                else  {
                                    alertScope.showError("Could not get information for study; " +
                                        (err ? err : "no error specified"));
                                }
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
                                    var studyTable = '<table class="info-table"><tr>' +
                                            '<th class="info-table">Title</th>' +
                                            '<th class="info-table">Identifier</th>' +
                                            '<th class="info-table">Description</th>' +
                                            '<th class="info-table">Years</th>' +
                                            '<th class="info-table">Sex</th>' +
                                            '<th class="info-table">Age Range</th>' +
                                            '<th class="info-table">Covariates</th>' +
                                            '</tr>';

                                    user.getAgeGroups(user.currentUser, res.data[0][4], res.data[0][19]).then(function (resAge) {
                                        for (var j = 0; j < inv.length; j++) {
                                            if (j === 0) {
                                                //match age group limits to fieldname
                                                var lwr = resAge.data[0].name[res.data[0][11]];
                                                var upr = resAge.data[0].name[res.data[0][10]];
                                                
                                                studyTable += '<tr><td class="info-table">' + res.data[0][17] + 
                                                        '</td><td class="info-table">' +
                                                        inv[j] + '</td>' +
                                                        '<td class="info-table">' + inv[j] + '</td>' +
                                                        '<td class="info-table">' + res.data[0][8] + "-" + res.data[0][9] + '</td>' +
                                                        '<td class="info-table">' + res.data[0][18] + '</td>' +
                                                        '<td class="info-table"> LWR: ' + lwr + ", UPR: " + upr + '</td>' +
                                                        '<td class="info-table">' + function () {
                                                            if (res.data[0][16]) {
                                                                scope.hasCovariates=true;
                                                                return res.data[0][16]; /* Covariates */
                                                            } else {
                                                                return "None";
                                                            }
                                                        }() + "</td>" +
                                                        "</tr>";
                                            } else {
                                                studyTable += '<tr><td class="info-table"></td>' + 
                                                        '</td><td class="info-table">' +
                                                        inv[j] + '</td>' +
                                                        '<td class="info-table">' + inv[j] + '</td>' +
                                                        '</tr>';
                                            }
                                        }
                                        project += studyTable + '</table>';

                                        //Statistics
                                        project += '<header>Statistics</header>';
                                        project += '<section>Calculation Method:</section>' + _getAttr(res.data[0][15]);

                                        scope.summary = $sce.trustAsHtml(
                                            cautionMessage("This Information is a work in progress due to missing data (RIF developers)") + 
                                            project);    
                                        if (scope.isRiskAnalysisStudy) {  
                                            user.getHomogeneity(user.currentUser, thisStudy).then(function (res) {
    /*
     * Returned JSON: 
     * "adjusted": {
	 *      "females": {
	 * 	    	"linearityP": 0,
	 * 	    	"linearityChi2": 0,
	 * 	    	"explt5": 0,
	 * 	    	"homogeneityDof": 2,
	 * 	    	"homogeneityP": 1.95058679437527E-4,
	 * 	    	"homogeneityChi2": 17.084420248951
	 * 	    },
	 * 	    "males": {
	 * 	    	"linearityP": 0,
	 * 	    	"linearityChi2": 0,
	 * 	    	"explt5": 0,
	 * 	    	"homogeneityDof": 2,
	 * 	    	"homogeneityP": 0.178163986654135,
	 * 	    	"homogeneityChi2": 3.45010175892807
	 * 	    },
	 * 	    "both": {
	 * 		"linearityP": 0,
	 * 		"linearityChi2": 0,
	 * 		"explt5": 0,
	 * 		"homogeneityDof": 2,
	 * 		"homogeneityP": 0.00337359835580779,
	 * 		"homogeneityChi2": 11.3835506858045
     *      }
     * },
     * "unadjusted": {
     * }
	 *  */
                                
                                                var homogeneityTable = '<table class="info-table"><tr>' +
                                                    '<th colspan="3" class="info-table">Unadjusted</th>' +
                                                    '<th align="center" class="info-table"></th>' +
                                                    '<th colspan="3" class="info-table">Adjusted</th>'+ +
                                                    '</tr>' +
                                                    '<tr>' +
                                                    '<td class="info-table">Males</td>' +
                                                    '<td class="info-table">Females</td>' +
                                                    '<td class="info-table">Both</td>' +
                                                    '<td class="info-table"></td>' +
                                                    '<td class="info-table">Males</td>' +
                                                    '<td class="info-table">Females</td>' +
                                                    '<td class="info-table">Both</td>' +
                                                    '</tr>';
                                                var homogeneityList = ["linearityP", "linearityChi2", "explt5", "homogeneityDof", 
                                                "homogeneityP", "homogeneityChi2"];
                                                var gendersList = ["males", "females", "both"];
                                                var homogeneityDescriptions = {
                                                    "linearityP": "Linearity p-value",
                                                    "linearityChi2": "Linearity Chi&sup2; statistic",
                                                    "explt5": "#bands with expected &lt;5",
                                                    "homogeneityDof": "Homogeneity Degree of Freedom",
                                                    "homogeneityP": "Homogeneity p-value",
                                                    "homogeneityChi2": "Homogeneity Chi&sup2; statistic"
                                                }
                                                var hasHomogeneityTable=false; 
                                                for (var i=0; i<homogeneityList.length; i++) {
                                                    var homogeneityAttr=homogeneityList[i];
                                                    homogeneityTable+='<tr>';
                                                    for (var j=0; j<gendersList.length; j++) {
                                                        var genderAttr=gendersList[j];
                                                        if (res.data.unadjusted && res.data.unadjusted[genderAttr] && 
                                                            res.data.unadjusted[genderAttr][homogeneityAttr]) {
                                                            homogeneityTable+='<td class="info-table">' + 
                                                                roundTo3DecimalPlaces(res.data.unadjusted[genderAttr][homogeneityAttr]) +
                                                                '</td>';
                                                            hasHomogeneityTable=true;
                                                        }
                                                        else {
                                                            homogeneityTable+='<td class="info-table">0</td>';
                                                        }
                                                    }
                                                    homogeneityTable+='<td  class="info-table" align="center">' + 
                                                        (homogeneityDescriptions[homogeneityAttr] || "No description") + 
                                                        '</td>';
                                                    for (var j=0; j<gendersList.length; j++) {
                                                        var genderAttr=gendersList[j];
                                                        if (res.data.adjusted && res.data.adjusted[genderAttr] && 
                                                            res.data.adjusted[genderAttr][homogeneityAttr]) {
                                                            homogeneityTable+='<td class="info-table">' + 
                                                                roundTo3DecimalPlaces(res.data.adjusted[genderAttr][homogeneityAttr]) +
                                                                '</td>';
                                                            hasHomogeneityTable=true;
                                                        }
                                                        else {
                                                            homogeneityTable+='<td class="info-table">0</td>';
                                                        }
                                                    }
                                                    homogeneityTable+='</tr>';
                                                } 
                                                homogeneityTable+='</table>';
                                                if (hasHomogeneityTable) {
                                                   homogeneityTestsHtml+= homogeneityTable;
                                                }
                                                else {
                                                    AlertService.consoleDebug("[rifd-util-info.js] no Homogeneity Table: " + 
                                                        JSON.stringify(res.data, null, 2));
                                                }
                                                AlertService.consoleDebug("[rifd-util-info.js] homogeneityTestsHtml: " + homogeneityTestsHtml);
                                                scope.homogeneityTests = $sce.trustAsHtml(
                                                    cautionMessage("This Information is a work in progress as the unadjusted values are missing (RIF developers)") + 
                                                    homogeneityTestsHtml);
                                                        
                                                if (scope.hasCovariates) {        
                                                    user.getCovariateLossReport(user.currentUser, thisStudy).then(function (res) {
                                                            buildCovariateLossReport(res)
                                                        }, function(err) {
                                                        retrieveError(err, "covariate loss report");
                                                    });
                                                }
                                                else {
                                                    scope.covariateLossReport = $sce.trustAsHtml(
                                                        cautionMessage("No covariates were used by this study") + 
                                                        covariateLossReportHtml);
                                                }
                                                                
                                            }, function(err) {
                                                    retrieveError(err, "homogeneity report");
                                            });
                                        }
                                        else if (scope.hasCovariates) {
                                            user.getCovariateLossReport(user.currentUser, thisStudy).then(function (res) {
                                                    buildCovariateLossReport(res)
                                                }, function(err) {
                                                    retrieveError(err, "covariate loss report");
                                                });
                                        }
                                        else {
                                            scope.covariateLossReport = $sce.trustAsHtml(
                                                cautionMessage("No covariates were used by this study") + 
                                                covariateLossReportHtml);
                                        }
                                    }, function(err) {
                                        retrieveError(err, "age groups");
                                    });
                                }, function(err) {
                                    retrieveError(err, "study details");
                                });
                            }, function(err) {
                                retrieveError(err, "health code");
                            });

                            function cautionMessage(message) {
                                return '</br><section class="info-caution">&#9888;&nbsp;' + message + '</section>';
                            }
                            function buildCovariateLossReport(res) {
                                if (res.data.S && res.data.S[0] && res.data.S[0].covariatename) {
                                   
                                    scope.covariateType = res.data.S[0].covariatename;
                                    scope.covariateDescription = res.data.S[0].covariateTableDescription; 
                                    for (var i=0; i<res.data.S.length; i++) {
                                        scope.covariateList.push(res.data.S[i].covariatename);
                                        scope.covariateDescriptions[res.data.S[i].covariatename] =
                                            res.data.S[i].covariateTableDescription;
                                        if (i == 0) {
                                            scope.showCovariateLossCovariate[res.data.S[i].covariatename] = true;
                                        }
                                        else {
                                            scope.showCovariateLossCovariate[res.data.S[i].covariatename] = false;
                                        }
                                        covariateLossReportHtml+='<div ng-show="showCovariateLossCovariate[' + 
                                            res.data.S[i].covariatename + ']">';
                                        covariateLossReportHtml+="<section><header>" +
                                            res.data.S[i].covariatename + ": " + res.data.S[i].covariateTableDescription;
                                            '</header><table class="info-table"><tr>' +
                                            "<th colspan='2'>Numerator</th>"+
                                            "<th>&nbsp;</th>" +
                                            "<th colspan='2'>Denominator</th>"+ +
                                            "</tr>" +
                                            "<tr>" +
                                            "<td>Study area</td>" +
                                            "<td>Comparison area</td>" +
                                            "<td>&nbsp;</td>" +
                                            "<td>Study area</td>" +
                                            "<td>Comparison area</td>" +
                                            "</tr>";
                                        covariateLossReportHtml+="</table></section>";
                                        covariateLossReportHtml+="<section><pre>" + JSON.stringify(res.data.S[i], null, 2) +
                                            "</pre></section></div>";
                                    }
                                    
                                    scope.covariateLossReport = $sce.trustAsHtml(
                                        cautionMessage("This Information is a work in progress (RIF developers)") + 
                                        covariateLossReportHtml);
                                }       
                                else {
                                    scope.covariateLossReport = $sce.trustAsHtml(
                                        cautionMessage("No covariates were used by this study") + 
                                        covariateLossReportHtml);
                                    AlertService.consoleDebug("[rifd-util-info.js] no Covariate Loss Report: " + 
                                        JSON.stringify(res.data, null, 2));
                                }    
                            }
                  
                            function roundTo3DecimalPlaces(attr) {
                                if (isNaN(attr)) {
                                    return attr;
                                }
                                else {
                                    return Math.round(attr * 1000) / 1000;
                                }
                            }
                           
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