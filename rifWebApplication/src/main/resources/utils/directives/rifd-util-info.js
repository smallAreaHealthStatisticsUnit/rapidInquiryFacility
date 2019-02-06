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
                            scope.hSplit1 = 100;
                            
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
                                            '<section>Study Description:</section>' + _getAttr(res.data[0][2] || 
                                                "<em>TODO: not returned from DB</em>") +
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
                                    project += '<section>Health Theme:</section><em>' + _getAttr("TODO: not returned from DB") + '</em>' + 
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
                                                        _getAttr(homogeneityDescriptions[homogeneityAttr] || "No description") + 
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
                                                
                                                var d3data=d3HomogeneityChart();
                                                
                                                homogeneityTestsHtml+=  "<section><pre>" + JSON.stringify(d3data, null, 2) +
                                                    "</pre></section><header>d3 Homogeneity Chart</header>";
                                            
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
                                var covariateLossReportValid=false;
                                var i=0;
                                for (key in res.data) {
                                    scope.covariateType=key;
                                    if (res.data[key] && res.data[key].S && res.data[key].C) {
                                        covariateLossReportValid=true;
                                        scope.covariateList.push(key);
                                        scope.covariateDescriptions[key] =
                                            res.data[key].S.covariateTableDescription;
                                        if (i == 0) {
                                            scope.showCovariateLossCovariate[key] = true;
                                        }
                                        else {
                                            scope.showCovariateLossCovariate[key] = false;
                                        }
                                        covariateLossReportHtml+='<div ng-show="showCovariateLossCovariate[' + 
                                            key + ']">';
                                        covariateLossReportHtml+="<section><header>" +
                                            key + ": " + res.data[key].S.covariateTableDescription;
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
                                        covariateLossReportHtml+="<section><pre>" + JSON.stringify(res.data[key], null, 2) +
                                            "</pre></section></div>";
                                    }     
                                    else {
                                        covariateLossReportValid=false;
                                    }
                                    
                                    scope.covariateLossReport = $sce.trustAsHtml(
                                        cautionMessage("This Information is a work in progress (RIF developers)") + 
                                        covariateLossReportHtml);


                                     
                                    i++;
                                }  
                                
                                if (!covariateLossReportValid) {
                                    scope.covariateLossReport = $sce.trustAsHtml(
                                        cautionMessage("No covariates were used by this study") + 
                                        covariateLossReportHtml);
                                    AlertService.consoleDebug("[rifd-util-info.js] no Covariate Loss Report: " + 
                                        JSON.stringify(res.data, null, 2));
                                }    
                            }
                  
                            function roundTo3DecimalPlaces(attr) {
                                if (isNaN(attr)) {
                                    return _getAttr(attr);
                                }
                                else {
                                    return '<attr>' + Math.round(attr * 1000) / 1000 + '</attr></br>';
                                }
                            }
    
                            function d3HomogeneityChart() {
//                                    var tooltip = d3.select("body").append("div")
//                                        .attr("class", "tooltip")
//                                        .style("visibility", "hidden");

                                    var margin = {top: 20, right: 20, bottom: 30, left: 40},
                                        width = 960 - margin.left - margin.right,
                                        height = 500 - margin.top - margin.bottom;
/* Need new REST call
1> SELECT genders, band_id, adjusted, observed, expected, lower95, upper95, relative_risk FROM rif_studies.s193_map;
2> go
band_id    adjusted observed                                 expected                                 lower95                                  upper95                                  relative_risk
---------- -------- ---------------------------------------- ---------------------------------------- ---------------------------------------- ---------------------------------------- ----------------------------------------
         1        1                               836.000000                               878.177373                                  .889578                                 1.018741                                  .951972
         1        1                               442.000000                               471.964956                                  .853148                                 1.028018                                  .936510
         1        1                              1278.000000                              1353.429654                                  .893890                                  .997484                                  .944268
         2        1                              4840.000000                              4964.536076                                  .947832                                 1.002772                                  .974915
         2        1                              2632.000000                              2621.799539                                  .966261                                 1.042986                                 1.003891
         2        1                              7472.000000                              7575.446185                                  .964231                                 1.008965                                  .986345

(6 rows affected)
 
WITH b AS (
    SELECT band_id, MAX(exposure_value) As max_exposure_value
      FROM s55_extract
     GROUP BY band_id
), a AS (
    SELECT genders, a.band_id, adjusted, observed, expected, lower95, upper95, relative_risk, b.max_exposure_value
      FROM s55_map a, b
     WHERE a.band_id = b.band_id
)
SELECT JSON_AGG(a) FROM a;

  */
                                    var data= [{ // x is max_exposure_value, y is relative_risk, e is upper95
 		"genders": 1,
 		"band_id": 1,
 		"adjusted": 1,
 		"observed": 122,
 		"expected": 134.614918177067,
 		"lower95": 0.758928839190033,
 		"upper95": 1.08226153160508,
 		"relative_risk": 0.906288854549734,
 		"max_exposure_value": 72
 	}, {
 		"genders": 2,
 		"band_id": 1,
 		"adjusted": 1,
 		"observed": 80,
 		"expected": 69.225522543931,
 		"lower95": 0.916353134795538,
 		"upper95": 1.4382979182895,
 		"relative_risk": 1.15564313652138,
 		"max_exposure_value": 72
 	}, {
 		"genders": 3,
 		"band_id": 1,
 		"adjusted": 1,
 		"observed": 202,
 		"expected": 203.56522154764,
 		"lower95": 0.864482796580928,
 		"upper95": 1.13904063929265,
 		"relative_risk": 0.992310957953723,
 		"max_exposure_value": 72
 	}, {
 		"genders": 1,
 		"band_id": 2,
 		"adjusted": 1,
 		"observed": 154,
 		"expected": 160.986693034749,
 		"lower95": 0.816841334381448,
 		"upper95": 1.12027276266715,
 		"relative_risk": 0.956600804059993,
 		"max_exposure_value": 66
 	}, {
 		"genders": 2,
 		"band_id": 2,
 		"adjusted": 1,
 		"observed": 56,
 		"expected": 87.6085630538271,
 		"lower95": 0.48285004008843,
 		"upper95": 0.830063357652665,
 		"relative_risk": 0.639206922793533,
 		"max_exposure_value": 66
 	}, {
 		"genders": 3,
 		"band_id": 2,
 		"adjusted": 1,
 		"observed": 210,
 		"expected": 249.66589886741,
 		"lower95": 0.734717697589835,
 		"upper95": 0.962940900563699,
 		"relative_risk": 0.841124082033824,
 		"max_exposure_value": 66
 	}, {
 		"genders": 1,
 		"band_id": 3,
 		"adjusted": 1,
 		"observed": 428,
 		"expected": 398.740546340158,
 		"lower95": 0.976356077183336,
 		"upper95": 1.18004482604304,
 		"relative_risk": 1.07337967991568,
 		"max_exposure_value": 60
 	}, {
 		"genders": 2,
 		"band_id": 3,
 		"adjusted": 1,
 		"observed": 248,
 		"expected": 215.487326637354,
 		"lower95": 1.01619628358071,
 		"upper95": 1.3034137340007,
 		"relative_risk": 1.15087974717586,
 		"max_exposure_value": 60
 	}, {
 		"genders": 3,
 		"band_id": 3,
 		"adjusted": 1,
 		"observed": 676,
 		"expected": 617.567300476479,
 		"lower95": 1.01513378274594,
 		"upper95": 1.18032478276612,
 		"relative_risk": 1.09461754124358,
 		"max_exposure_value": 60
 	}
 ];
                                    AlertService.consoleDebug("[rifd-util-info.js] homogeneityChart data: " + JSON.stringify(data, null, 1));
                                    var xScale = d3.scaleLinear()
                                             .range([0, width])
                                             .domain([0, d3.max(data, function(d) { return d.max_exposure_value; })]).nice();
                                             
                                    var yScale = d3.scaleLinear()
                                       .range([height, 0])
                                       .domain([0, d3.max(data, function(d) { return d.relative_risk; })]).nice();

                                    var xAxis = d3.axisBottom(xScale).ticks(12),
                                       yAxis = d3.axisLeft(yScale).ticks(12 * height / width);

                                    let line = d3.line()
                                        .x(function(d) {
                                        return xScale(d.max_exposure_value);
                                      })
                                      .y(function(d) {
                                        return yScale(d.relative_risk);
                                      });

                                    var svg = d3.select("#homogeneityChart").append("svg")
                                        .attr("width", width + margin.left + margin.right)
                                        .attr("height", height + margin.top + margin.bottom)
                                      .append("g")
                                        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
                                        
                                    svg.append("g").append("rect").
                                        attr("width", width).attr("height", height).attr("class", "homogeneityChart-bg");

                                    // Add Axis labels
                                    svg.append("g").attr("class", "axis axis--x")
                                    .attr("transform", "translate(" + 0 + "," + height + ")")
                                    .call(xAxis);

                                    svg.append("g").attr("class", "axis axis--y").call(yAxis);

                                    // Add Error Line
                                    svg.append("g").selectAll("line")
                                        .data(data).enter()
                                      .append("line")
                                      .attr("class", "homogeneityChart-error-line")
                                      .attr("x1", function(d) {
                                        return xScale(d.max_exposure_value);
                                      })
                                      .attr("y1", function(d) {
                                        return yScale(d.upper95);
                                      })
                                      .attr("x2", function(d) {
                                        return xScale(d.max_exposure_value);
                                      })
                                      .attr("y2", function(d) {
                                        return yScale(d.lower95);
                                      });

                                    // Add Error Top Cap
                                    svg.append("g").selectAll("line")
                                        .data(data).enter()
                                      .append("line")
                                      .attr("class", "homogeneityChart-error-cap")
                                      .attr("x1", function(d) {
                                        return xScale(d.max_exposure_value) - 4;
                                      })
                                      .attr("y1", function(d) {
                                        return yScale(d.upper95);
                                      })
                                      .attr("x2", function(d) {
                                        return xScale(d.max_exposure_value) + 4;
                                      })
                                      .attr("y2", function(d) {
                                        return yScale(d.upper95);
                                      });
                                      
                                     // Add Error Bottom Cap
                                    svg.append("g").selectAll("line")
                                        .data(data).enter()
                                      .append("line")
                                      .attr("class", "error-cap")
                                      .attr("x1", function(d) {
                                        return xScale(d.max_exposure_value) - 4;
                                      })
                                      .attr("y1", function(d) {
                                        return yScale(d.lower95);
                                      })
                                      .attr("x2", function(d) {
                                        return xScale(d.max_exposure_value) + 4;
                                      })
                                      .attr("y2", function(d) {
                                        return yScale(d.lower95);
                                      });
                                      
                                    // Add Scatter Points
                                    svg.append("g").attr("class", "scatter")
                                    .selectAll("circle")
                                    .data(data).enter()
                                    .append("circle")
                                    .attr("cx", function(d) {
                                      return xScale(d.max_exposure_value);
                                    })
                                    .attr("cy", function(d) {
                                      return yScale(d.relative_risk);
                                    })
                                    .attr("r", 4);
/*                                    .on("mouseover", function(d){
                                        return tooltip.html(d.relative_risk.toFixed(3) + " &plusmn; " + d.upper95.toFixed(3))
                                            .style("visibility", "visible")
                                          .style("top", (event.pageY-17)+"px").style("left",(event.pageX+25)+"px");
                                     })
                                    .on("mouseout", function(){
                                        return tooltip.style("visibility", "hidden");
                                     });  */
                                return data;
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