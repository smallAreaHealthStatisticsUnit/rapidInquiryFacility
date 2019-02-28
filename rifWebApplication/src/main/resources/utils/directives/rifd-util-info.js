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
 *
 * Peter Hambly
 * @author phambly
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
        .directive('getStudyInfo', ['$uibModal', 'user', '$sce', 'AlertService', 'D3ChartsService', 
                function ($uibModal, user, $sce, AlertService, D3ChartsService) {
                return {
                    restrict: 'A',
                    link: function (scope, element, attr) {

                        var alertScope = scope.$parent.$$childHead.$parent.$parent.$$childHead;
                        
                        /*
                         * Change functions
                         */
                        scope.covariateChange= function (covariateType) {
                            scope.covariateType = covariateType;
                            AlertService.consoleDebug("[rifd-util-info.js] covariateChange(): " + scope.covariateType);

                            var covariateLossReportHtml = '<header>Covariate Loss Report &ndash; ' + scope.headerInfo + '</header>' +
                                scope.covariateHtml[covariateType];
                            AlertService.consoleDebug("[rifd-util-info.js] covariateChange(): " + scope.covariateType +
                                "HTML: " + covariateLossReportHtml);
                            scope.covariateLossReport = $sce.trustAsHtml(covariateLossReportHtml);
                        }
                        scope.reportChange = function (reportType) {
                            scope.reportType = reportType;
                            AlertService.consoleDebug("[rifd-util-info.js] reportChange(): " + scope.reportType);
                            if (scope.reportType) {
                                if (scope.reportType == "Summary") {
                                    scope.showSummary=true;
                                    scope.showCovariateLossReport=false;
                                    scope.showHomogeneityTests=false;
									scope.showRiskFactorCharts=false;
                                }
                                else if (scope.reportType == "Covariate Loss Report") {
                                    scope.showSummary=false;
                                    scope.showCovariateLossReport=true;
                                    scope.showHomogeneityTests=false;
									scope.showRiskFactorCharts=false;
                                }
                                else if (scope.reportType == "Homogeneity Tests") {
                                    scope.showSummary=false;
                                    scope.showCovariateLossReport=false;
                                    scope.showHomogeneityTests=true;
									scope.showRiskFactorCharts=false;
                                }
                                else if (scope.reportType == "Risk Graphs") {
                                    scope.showSummary=false;
                                    scope.showCovariateLossReport=false;
                                    scope.showHomogeneityTests=false;
									scope.showRiskFactorCharts=true;
                                }
                            }
                            else {
                                scope.reportType = "Summary";
                                scope.showSummary=true;
                                scope.showCovariateLossReport=false;
                                scope.showHomogeneityTests=false;
									scope.showRiskFactorCharts=false;
                            }
                            scope.reportTitle='Study ' + scope.reportType;
                            scope.reportDescription=scope.reportTitle;
                        }
						
						scope.d3RiskGraphChange = function (gendersName1, gendersName2, riskFactor) {
		
							var homogeneityChartHtml='<header>Risk Graph &ndash; ' + scope.headerInfo + '</header>';
							scope.riskFactorChartHeader = $sce.trustAsHtml(homogeneityChartHtml);
                            
                            var gendersArray=['males', 'females'];
                            if (gendersName1) {
                                gendersArray[0] = gendersName1;
                            }
                            if (gendersName2) {
                                gendersArray[1] = gendersName2;
                            }
                            scope.gendersArray=gendersArray;
                            scope.riskFactor=riskFactor || scope.riskFactor;

                            scope.optionsd3 = { // For save button
                                "riskGraph": {
                                    container: "riskGraph",
                                    element: "#hSplit3",
                                    filename: "riskGraph.png"
                                }
                            };        
                            
                            /* Directive inputs:
                             * <risk-graph 
                             *       risk-graph-data="riskGraphData" 
                             *       name="riskGraphChartName1" 
                             *       width="riskGraphChartCurrentWidth" 
                             *       height="riskGraphChartCurrentHeight" 
                             *       genders-array="gendersArray" 
                             *       risk-factor="riskFactor" 
                             *       risk-factor2-field-name="riskFactor2FieldName">
                             *  </risk-graph>
                             */
                            AlertService.consoleDebug("[rifd-util-info.js] d3RiskGraphChange() " +
                                "gendersArray: " + JSON.stringify(scope.gendersArray) + 
                                "; riskFactor: " + scope.riskFactor +
                                "; riskGraphChartName1: " + scope.riskGraphChartName1 +
                                "; riskGraphChartCurrentWidth: " + scope.riskGraphChartCurrentWidth +
                                "; riskGraphChartCurrentHeight: " + scope.riskGraphChartCurrentHeight +
                                "; riskFactor2FieldName: " + scope.riskFactor2FieldName[scope.riskFactor]);
                            
						}
						
                        /*
                         * Initiation function: when the "i" is clicked
                         */
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
							scope.showRiskFactorCharts=false;
                            scope.covariateList = [];
                            scope.covariateType = null;
                            scope.covariateDescriptions = {};
                            scope.covariateHtml = {};
                            scope.isRiskAnalysisStudy=false;
                            scope.hasCovariates=false;
                            scope.gendersName1="males";
                            scope.gendersName2="females";
							scope.gendersList = ['males', 'females', 'both'];			
                            scope.gendersArray=['males', 'females'];	
                            scope.riskFactorList=['average exposure', 'band', 'average distance from nearest source'];
                            scope.riskFactor='band';
                            scope.riskGraphHasBothMalesAndFemales=false;
                            scope.riskGraphHasMales=false;
                            scope.riskGraphHasFemales=false;
                            scope.hSplit1=100;
                            scope.riskFactor2FieldName = {
                                'average exposure': 'avgExposureValue', 
                                'band': 'bandId', 
                                'average distance from nearest source': 'avgDistanceFromNearestSource'
                            };
                            scope.riskGraphChartCurrentWidth=0;
                            scope.riskGraphChartCurrentHeight=0;
                            scope.riskGraphChartName1="riskGraph";
							
                            if (scope.myMaps) {
                                scope.mapType=scope.myMaps[0]; // E.g. viewermap
                            }
                            if (scope.mapType && scope.studyID && scope.studyID[scope.mapType]) {
                                scope.mapDefs=scope.studyID[scope.mapType]; 
                            }
                            if (scope.mapDefs && scope.mapDefs.study_type && scope.mapDefs.study_type == "Risk Analysis") {
                                scope.isRiskAnalysisStudy=true;
                                scope.reportList = ["Summary", "Covariate Loss Report", "Homogeneity Tests", "Risk Graphs"];
                            }
                            else {
                                scope.reportList = ["Summary", "Covariate Loss Report"];
                            }
                        
                            // Set defaults for summaries, headers
                            scope.headerInfo=((scope.mapDefs && scope.mapDefs.study_id) ? "Study: " + 
                                scope.mapDefs.study_id : "Unknown study") + 
                                ' &ndash; ' +
                                ((scope.mapDefs && scope.mapDefs.riskAnalysisDescription) ? 
                                       scope.mapDefs.riskAnalysisDescription : (scope.mapDefs.study_type || "Unknown study type") + " study") +
                                ' &ndash; ' +
                                ((scope.mapDefs && scope.mapDefs.name) ? scope.mapDefs.name : "No name");
                            scope.summary = $sce.trustAsHtml('<header class="info-header">Fetching Study Information for ' + 
                                scope.headerInfo + '...</header>');
                            scope.covariateLossReport = $sce.trustAsHtml('<header class="info-header">Fetching Study Covariate Loss Report for ' + 
                                scope.headerInfo + '...</header>');
                            scope.homogeneityTests = $sce.trustAsHtml('<header class="info-header">Fetching Study Homogeneity Tests for ' + 
                                scope.headerInfo + '...</header>');
                            scope.riskFactorChartHeader = $sce.trustAsHtml('<header class="info-header">Fetching Study Risk Graphs for ' + 
                                scope.headerInfo + '...</header>');
								
                            var homogeneityTestsHtml = '<header>Homogeneity Tests &ndash; ' + scope.headerInfo + '</header>';
                            var covariateLossReportHtml = '<header>Covariate Loss Report &ndash; ' + scope.headerInfo + '</header>';
                            
                            var thisStudy = scope.studyID[attr.mapid].study_id;
                            _getAttr = function (v) {
                                if (!v) {
                                    return "&nbsp;";
                                }
                                else if (isNaN(v)) {
                                    return '<attr>' + $sce.trustAsHtml(v) + '</attr></br>';
                                }
                                else {
                                    return roundTo3DecimalPlaces(v);
                                }
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
                            
                            function processDetailsForProcessedStudy(res) {
                                var project = '<header>Overview</header><section>Project Name:</section>' + _getAttr(res.data[0][13]) +
                                        '<section>Study:</section>' + _getAttr(thisStudy) +
                                        '<section>Project Description:</section>' + _getAttr(res.data[0][14]) +
                                        '<section>Submitted By:</section>' + _getAttr(res.data[0][0]) +
                                        '<section>Date:</section>' + _getAttr(res.data[0][3]) +
                                        '<section>Study Name:</section>' + _getAttr(res.data[0][1]) +
                                        '<section>Study Description:</section>' + _getAttr(res.data[0][2] || 
                                            '<em>' + _getAttr('TODO: not set in DB') + '</em>') +
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
                                project += '<section>Health Theme:</section><em>' + _getAttr('TODO: not returned from DB') + '</em>' + 
                                        '<section>Numerator Table:</section>' + _getAttr(res.data[0][19]) +
                                        '<section>Denominator Table:</section>' + _getAttr(res.data[0][7]);
                              
                                return project;
                            }
                            
                            function processAgeGroups(res, resAge, inv, project) {   
                                //Table
                                var studyTable = '<table width="90%" align="center"><tr>' +
                                        '<th class="info-table">Title</th>' +
                                        '<th class="info-table">Identifier</th>' +
                                        '<th class="info-table">Description</th>' +
                                        '<th class="info-table">Years</th>' +
                                        '<th class="info-table">Sex</th>' +
                                        '<th class="info-table">Age Range</th>' +
                                        '<th class="info-table">Covariates</th>' +
                                        '</tr>';  
                                        
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
                                                '<td class="info-table"> Lower: ' + lwr + ", Upper: " + upr + '</td>' +
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

                                scope.summary = $sce.trustAsHtml(project);   
                            }
                            
                            function processHomogeneity(res) {

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
                             /*       '<th colspan="3" class="info-table">Unadjusted</th>' */
                                    '<th align="left" class="info-table">Statistic</th>' +
                                    '<th colspan="3" class="info-table">Adjusted</th>'+
                                    '</tr>' +
                                    '<tr>' +
                            /*        '<th align="left" class="info-table">Males</th>' +
                                    '<th align="left" class="info-table">Females</th>' +
                                    '<th align="left" class="info-table">Both</th>' + */
                                    '<th align="left" class="info-table"></th>' +
                                    '<th align="left" class="info-table">Males</th>' +
                                    '<th align="left" class="info-table">Females</th>' +
                                    '<th align="left" class="info-table">Both</th>' +
                                    '</tr>';
                                var homogeneityList = ["linearityP", "linearityChi2", "explt5", "homogeneityDof", 
                                "homogeneityP", "homogeneityChi2"];
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
                                    /*
                                    for (var j=0; j<scope.gendersList.length; j++) {
                                        var genderAttr=scope.gendersList[j];
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
                                    } */
                                    homogeneityTable+='<td  class="info-table" align="left">' + 
                                        _getAttr(homogeneityDescriptions[homogeneityAttr] || "No description") + 
                                        '</td>';
                                    for (var j=0; j<scope.gendersList.length; j++) {
                                        var genderAttr=scope.gendersList[j];
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
                            }
                                                        
                            function cautionMessage(message) {
                                return '</br><section class="info-caution">&#9888;&nbsp;' + message + '</section>';
                            }
                            function buildCovariateLossReport(res) {
                                var covariateLossReportValid=false;
                                var covariateTableDescription;
                                var i=0;
     /* Example data:
     "SES": {
        "S": {
          "missingCovariateAreas": "174",
          "covariateTableDescription": "socio-economic status",
          "denominatorMaxYear": "1996",
          "covariateName": "SES",
          "covariateTableName": "COVAR_SAHSULAND_COVARIATES4",
          "numeratorMinYear": "1995",
          "studyOrComparison": "S",
          "extractMinYear": "1995",
          "missingDenominatorAreas": "0",
          "denominatorCount": "1.4074236E7",
          "missingDenominatorCovariateCount": "1852276.0",
          "numeratorTableName": "NUM_SAHSULAND_CANCER",
          "missingNumerator": "0",
          "icdFilter": "([icd] LIKE 'C33%' -* Value filter *- OR</br> [icd] LIKE 'C34%' -* Value filter *-)",
          "extractMaxYear": "1996",
          "numeratorMaxYear": "1996",
          "missingExtractYears": "0",
          "denominatorTableName": "POP_SAHSULAND_POP",
          "ageSexGroupFilter": "age_sex_group BETWEEN 100 AND 221",
          "numeratorCount": "8750",
          "denominatorMinYear": "1995",
          "missingDenominator": "0.0",
          "covariateFilter": "CASE WHEN b.ses BETWEEN '1.0' AND '5.0'",
          "missingNumeratorCovariatePct": "0.006892025968585435",
          "extractYears": "1995&ndash;1996; years verified",
          "missingNumeratorCovariateCount": "970",
          "mappingGeolevelAreas": "889",
          "missingDenominatorCovariatePct": "13.160757003079954",
          "denominatorYears": "1995&ndash;1996",
          "numeratorYears": "1995&ndash;1996",
          "extractNumeratorCount": "Yes",
          "extractDenominatorCount": "Yes"
        },
        "C": {
            ...
        }
      */
                                var fieldColumnList = [
                                {
                                        description: "Total",
                                        studyNumerator: "numeratorCount",
                                        studyDenominator: "denominatorCount",
                                        comparisonNumerator: "numeratorCount",
                                        comparisonDenominator: "denominatorCount",
                                }, {
                                        description: "Areas",
                                        studyNumerator: "mappingGeolevelAreas",
                                        studyDenominator: "mappingGeolevelAreas",
                                        comparisonNumerator: "mappingGeolevelAreas",
                                        comparisonDenominator: "mappingGeolevelAreas",
                                }, {
                                        description: "&nbsp;" /* Spacer */
                                }, {
                                        description: "Covariate Exclusion",
                                        header: true
                                }, {
                                        description: "Areas Excluded",
                                        studyNumerator: "missingCovariateAreas",
                                        studyDenominator: "missingCovariateAreas",
                                        comparisonNumerator: "missingCovariateAreas",
                                        comparisonDenominator: "missingCovariateAreas",
                                }, {
                                        description: "Records Excluded",
                                        studyNumerator: "missingNumeratorCovariateCount",
                                        studyDenominator: "missingDenominatorCovariateCount",
                                        comparisonNumerator: "missingNumeratorCovariateCount",
                                        comparisonDenominator: "missingDenominatorCovariateCount",
                                }, {
                                        description: "% Excluded",
                                        studyNumerator: "missingNumeratorCovariatePct",
                                        studyDenominator: "missingDenominatorCovariatePct",
                                        comparisonNumerator: "missingNumeratorCovariatePct",
                                        comparisonDenominator: "missingDenominatorCovariatePct",
                                }, {
                                        description: "&nbsp;" /* Spacer */
                                }, {
                                        description: "Study Setup",
                                        header: true
                                }, {
                                        description: "Database Table",
                                        studyNumerator: "numeratorTableName",
                                        studyDenominator: "denominatorTableName",
                                        comparisonNumerator: "numeratorTableName",
                                        comparisonDenominator: "denominatorTableName",
                                }, {
                                        description: "Covariate Table",
                                        studyNumerator: "covariateTableName",
                                        studyDenominator: "covariateTableName",
                                        comparisonNumerator: "covariateTableName",
                                        comparisonDenominator: "covariateTableName",
                                }, {
                                        description: "&nbsp;" /* Spacer */
                                }, {
                                        description: "Covariate Filter",
                                        studyNumerator: "covariateFilter",
                                        studyDenominator: "covariateFilter",
                                        comparisonNumerator: "covariateFilter",
                                        comparisonDenominator: "covariateFilter",
                                }, {
                                        description: "ICD filter",
                                        studyNumerator: "icdFilter",
                                        studyDenominator: "icdFilter",
                                        comparisonNumerator: "icdFilter",
                                        comparisonDenominator: "icdFilter",
                                }, {
                                        description: "Age sex group filter",
                                        studyNumerator: "ageSexGroupFilter",
                                        studyDenominator: "ageSexGroupFilter",
                                        comparisonNumerator: "ageSexGroupFilter",
                                        comparisonDenominator: "ageSexGroupFilter",
                                }, {
                                        description: "&nbsp;" /* Spacer */
                                }, {
                                        description: "Extract Verification",
                                        header: true
                                }, {
                                        description: "Verification Period",
                                        studyNumerator: "numeratorYears",
                                        studyDenominator: "denominatorYears",
                                        comparisonNumerator: "numeratorYears",
                                        comparisonDenominator: "denominatorYears",
                                }, {
                                        description: "ExtractPeriod",
                                        studyNumerator: "extractYears",
                                        studyDenominator: "extractYears",
                                        comparisonNumerator: "extractYears",
                                        comparisonDenominator: "extractYears",
                                }, {
                                        description: "Extract Counts Verified",
                                        studyNumerator: "extractNumeratorCount",
                                        studyDenominator: "extractDenominatorCount",
                                        comparisonNumerator: "extractNumeratorCount",
                                        comparisonDenominator: "extractDenominatorCount",
                                }, {
                                        description: "Missing Extract Areas",
                                        studyNumerator: "missingExtractAreas",
                                        studyDenominator: "missingExtractAreas",
                                        comparisonNumerator: "missingExtractAreas",
                                        comparisonDenominator: "missingExtractAreas",
                                }
                                ];
                                
                                for (var key in res.data) {
                                    if (res.data[key] && res.data[key].S && res.data[key].S["extractMinYear"] && res.data[key].S["extractMaxYear"] &&
                                                         res.data[key].C && res.data[key].C["extractMinYear"] && res.data[key].C["extractMaxYear"]) {
                                        res.data[key].S["extractYears"] = res.data[key].S["extractMinYear"] + "&ndash;" + 
                                            res.data[key].S["extractMaxYear"];
                                        if (res.data[key].S["missingExtractYears"] && res.data[key].S["missingExtractYears"] != "0") { 
                                            res.data[key].S["extractYears"] += ";&nbsp;" + res.data[key].S["missingExtractYears"];
                                        }
                                        else {
                                            res.data[key].S["extractYears"] += "; years verified";
                                        }
                                        res.data[key].C["extractYears"] = res.data[key].C["extractMinYear"] + "&ndash;" + 
                                            res.data[key].C["extractMaxYear"];
                                        if (res.data[key].C["missingExtractYears"] && res.data[key].C["missingExtractYears"] != "0") { 
                                            res.data[key].C["extractYears"] += ";&nbsp;" + res.data[key].C["missingExtractYears"];
                                        }
                                        else {
                                            res.data[key].C["extractYears"] += "; years verified";
                                        }
                                    }
                               
                                    if (res.data[key] && res.data[key].S && res.data[key].S["denominatorMinYear"] && res.data[key].S["denominatorMaxYear"] && 
                                                         res.data[key].C && res.data[key].C["denominatorMinYear"] && res.data[key].C["denominatorMaxYear"]) {
                                        res.data[key].S["denominatorYears"] = res.data[key].S["denominatorMinYear"] + "&ndash;" + 
                                            res.data[key].S["denominatorMaxYear"];
                                        res.data[key].C["denominatorYears"] = res.data[key].C["denominatorMinYear"] + "&ndash;" + 
                                            res.data[key].C["denominatorMaxYear"];
                                    }
                                    
                                    if (res.data[key] && res.data[key].S && res.data[key].S["numeratorMinYear"] && res.data[key].S["numeratorMinYear"] && 
                                                         res.data[key].C && res.data[key].C["numeratorMinYear"] && res.data[key].C["numeratorMinYear"]) {
                                        res.data[key].S["numeratorYears"] = res.data[key].S["numeratorMinYear"] + "&ndash;" + 
                                            res.data[key].S["numeratorMaxYear"];
                                        res.data[key].C["numeratorYears"] = res.data[key].C["numeratorMinYear"] + "&ndash;" + 
                                            res.data[key].C["numeratorMinYear"];
                                    }
                                    
                                    processMissingKey(res.data[key], "extractNumeratorCount", "missingNumerator", "S");
                                    processMissingKey(res.data[key], "extractDenominatorCount", "missingDenominator", "S");
                                    processMissingKey(res.data[key], "extractNumeratorCount", "missingNumerator", "C");
                                    processMissingKey(res.data[key], "extractDenominatorCount", "missingDenominator", "C");
                                    
                                }
                                for (var key in res.data) {
                                    if (i ==0) {
                                        scope.covariateType=key;
                                    }
                                    if (res.data[key] && res.data[key].S && res.data[key].C) {
                                        covariateLossReportValid=true;
                                        scope.covariateList.push(key);
                                        scope.covariateDescriptions[key] =
                                            res.data[key].S.covariateTableDescription;
                                        scope.covariateHtml[key] = 
                                            '<section>' + key + ': </section>' + 
                                                _getAttr(scope.covariateDescriptions[key] || "No description") + '</br>' +
                                            '<table class="info-table"><tr>' +
                                            '<th class="info-table" colspan="2">Numerator</th>' +
                                            '<th>&nbsp;</th>' +
                                            '<th class="info-table" colspan="2">Denominator</th>' +
                                            '</tr>' +
                                            '<tr>' +
                                            '<th class="info-table">Study area</th>' +
                                            '<th class="info-table">Comparison area</th>' +
                                            '<th class="info-table">Summary</th>' +
                                            '<th class="info-table">Study area</th>' +
                                            '<th class="info-table">Comparison area</th>' +
                                            '</tr>';
                                        for (var k=0; k<fieldColumnList.length; k++) {
                                            if (fieldColumnList[k] && fieldColumnList[k].header) {
                                                scope.covariateHtml[key] +=  '<tr>' +
                                                    '<th class="info-table" colspan="2" align="center">&nbsp;</th>' +
                                                    '<th class="info-table" align="center">' + 
                                                        _getAttr(fieldColumnList[k].description || "UNKNOWN") + '</th>' +
                                                    '<th class="info-table" colspan="2" align="center">&nbsp;</th>' +
                                                    '</tr>';
                                            }
                                            else if (fieldColumnList[k] && fieldColumnList[k].description) {
                                                
                                                if (fieldColumnList[k].studyNumerator && res.data[key].S[fieldColumnList[k].studyNumerator] &&
                                                    fieldColumnList[k].comparisonNumerator && res.data[key].C[fieldColumnList[k].comparisonNumerator] &&
                                                    res.data[key].S[fieldColumnList[k].studyNumerator] ==
                                                                res.data[key].C[fieldColumnList[k].comparisonNumerator]) {
                                                    scope.covariateHtml[key] +=  '<tr>' +
                                                        '<td class="info-table" colspan="2" align="center">' + 
                                                        _getAttr(res.data[key].S[fieldColumnList[k].studyNumerator]) + '</td>';
                                                }
                                                else {               
                                                    scope.covariateHtml[key] += '<tr>' +
                                                        '<td class="info-table" align="center">' +
                                                            (
                                                                (fieldColumnList[k].studyNumerator && res.data[key].S[fieldColumnList[k].studyNumerator]) ?
                                                                _getAttr(res.data[key].S[fieldColumnList[k].studyNumerator]) : 
                                                                (fieldColumnList[k].studyNumerator || '')
                                                            ) + '</td>' +
                                                        '<td class="info-table" align="center">' +
                                                            (
                                                                (fieldColumnList[k].comparisonNumerator && res.data[key].C[fieldColumnList[k].comparisonNumerator]) ?
                                                                _getAttr(res.data[key].C[fieldColumnList[k].comparisonNumerator]) : 
                                                                (fieldColumnList[k].comparisonNumerator || '')
                                                            ) + '</td>';
                                                }
                                                scope.covariateHtml[key] += 
                                                    '<td class="info-table" align="center">' + _getAttr(fieldColumnList[k].description || "UNKNOWN") + '</td>';
                                                if (fieldColumnList[k].studyDenominator && res.data[key].S[fieldColumnList[k].studyDenominator] &&
                                                    fieldColumnList[k].comparisonDenominator && res.data[key].C[fieldColumnList[k].comparisonDenominator] &&
                                                    res.data[key].S[fieldColumnList[k].studyDenominator] ==
                                                                res.data[key].C[fieldColumnList[k].comparisonDenominator]) {
                                                    scope.covariateHtml[key] += 
                                                        '<td class="info-table" colspan="2" align="center">' +
                                                        _getAttr(res.data[key].C[fieldColumnList[k].comparisonDenominator]) + '</td>' +
                                                        '</tr>';  
                                                }
                                                else {
                                                    scope.covariateHtml[key] += 
                                                        '<td class="info-table" align="center">' +
                                                            (
                                                                (fieldColumnList[k].studyDenominator && 
                                                                 res.data[key].S[fieldColumnList[k].studyDenominator]) ?
                                                                _getAttr(res.data[key].S[fieldColumnList[k].studyDenominator]) : 
                                                                (fieldColumnList[k].studyDenominator || '')
                                                            ) + '</td>' +
                                                        '<td class="info-table" align="center">' +
                                                            (
                                                                (fieldColumnList[k].comparisonDenominator && 
                                                                 res.data[key].C[fieldColumnList[k].comparisonDenominator]) ?
                                                                _getAttr(res.data[key].C[fieldColumnList[k].comparisonDenominator]) : 
                                                                (fieldColumnList[k].comparisonDenominator || '')
                                                            ) + '</td>' +
                                                        '</tr>';
                                                }
                                            }
                                        }
                                        scope.covariateHtml[key] += '</table>';
                                        if (i == 0) {
                                            covariateLossReportHtml+=scope.covariateHtml[key];
                                        }
                                    }     
                                    else {
                                        covariateLossReportValid=false;
                                    }          
                                 
                                    i++;
                                }  
                                
                                if (!covariateLossReportValid) {
                                    scope.covariateLossReport = $sce.trustAsHtml(
                                        cautionMessage("No covariates were found for this study") + 
                                        covariateLossReportHtml);
//                                    AlertService.consoleDebug("[rifd-util-info.js] no Covariate Loss Report: " + 
//                                        JSON.stringify(res.data, null, 2));
                                }   
                                else {
                                    scope.covariateLossReport = $sce.trustAsHtml(covariateLossReportHtml);
//                                    AlertService.consoleDebug("[rifd-util-info.js] Covariate Loss Report: " + 
//                                        JSON.stringify(res.data, null, 2));
                                }    
                            }
                                   
                            function processMissingKey(dataRecord, outputKeyName, inputKeyName, studyOrComparison) { 
                                if (dataRecord && dataRecord[studyOrComparison] && dataRecord[studyOrComparison][inputKeyName]) {
                                    var value=dataRecord[studyOrComparison][inputKeyName];
                                    try {
                                        value=parseFloat(dataRecord[studyOrComparison][inputKeyName]);
                                        if (value == 0) {
                                            dataRecord[studyOrComparison][outputKeyName] = "Yes";
                                        }    
                                        else if (value > 0){
                                            dataRecord[studyOrComparison][outputKeyName] = "Extra: " + value;
                                        } 
                                        else {
                                            dataRecord[studyOrComparison][outputKeyName] = "Missing: " + (value*-1);
                                        }  
                                    }
                                    catch (e) {
                                        dataRecord[studyOrComparison][outputKeyName] = "NaN: " + value + "error: " + JSON.stringify(e);
                                    }    
                                }
                                else {
                                    dataRecord[studyOrComparison][outputKeyName] = "N/A";
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
 
                           user.getHealthCodesForProcessedStudy(user.currentUser, thisStudy).then(function (invx) {
                                var inv = invx.data[0];

                                user.getDetailsForProcessedStudy(user.currentUser, thisStudy).then(function (res) {
                                    var project=processDetailsForProcessedStudy(res);

                                    user.getAgeGroups(user.currentUser, res.data[0][4], res.data[0][19]).then(function (resAge) {
                                        processAgeGroups(res, resAge, inv, project);
                                         
                                        if (scope.isRiskAnalysisStudy) {  
                                            user.getHomogeneity(user.currentUser, thisStudy).then(function (res) {
                                                processHomogeneity(res);
                                                
                                                user.getRiskGraph(user.currentUser, thisStudy).then(function (res) {
                                                    scope.riskGraphData=angular.copy(res.data);
                                                    
                                                    var selector=D3ChartsService.setupRiskGraphSelector(
                                                        scope.riskGraphData, scope.riskFactor2FieldName);
                                                    for (var key in selector) { // Copy to scope
                                                        scope[key]=angular.copy(selector[key]);
                                                    }
                                                    scope.d3RiskGraphChange(undefined, undefined, undefined);
                                            
//                                                    AlertService.consoleDebug("[rifd-util-info.js] homogeneityTestsHtml: " + 
//                                                        homogeneityTestsHtml);
                                                    scope.homogeneityTests = $sce.trustAsHtml(homogeneityTestsHtml); 
                                                            
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
                                                        retrieveError(err, "risk graph");
                                                });
                                                                
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