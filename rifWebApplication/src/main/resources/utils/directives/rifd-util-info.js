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
									scope.showHomogeneityCharts=false;
                                }
                                else if (scope.reportType == "Covariate Loss Report") {
                                    scope.showSummary=false;
                                    scope.showCovariateLossReport=true;
                                    scope.showHomogeneityTests=false;
									scope.showHomogeneityCharts=false;
                                }
                                else if (scope.reportType == "Homogeneity Tests") {
                                    scope.showSummary=false;
                                    scope.showCovariateLossReport=false;
                                    scope.showHomogeneityTests=true;
									scope.showHomogeneityCharts=false;
                                }
                                else if (scope.reportType == "Homogeneity Charts") {
                                    scope.showSummary=false;
                                    scope.showCovariateLossReport=false;
                                    scope.showHomogeneityTests=false;
									scope.showHomogeneityCharts=true;
                                }
                            }
                            else {
                                scope.reportType = "Summary";
                                scope.showSummary=true;
                                scope.showCovariateLossReport=false;
                                scope.showHomogeneityTests=false;
									scope.showHomogeneityCharts=false;
                            }
                            scope.reportTitle='Study ' + scope.reportType;
                            scope.reportDescription=scope.reportTitle;
                        }
						
						scope.d3HomogeneityChartChange = function (gendersName) {
							scope.gendersName=gendersName;
                            AlertService.consoleDebug("[rifd-util-info.js] d3HomogeneityChartChange(): " + scope.gendersName);
							var homogeneityChartHtml='<header>Homogeneity Chart &ndash; ' + scope.headerInfo + '</header>';
							scope.homogeneityChartHeader = $sce.trustAsHtml(homogeneityChartHtml);
							
							function gender2text(gender) {
								var rval;
								switch (gender) {
									case 1: 
										rval="males";
										break;
									case 2: 
										rval="females";
										break;
									case 3: 
										rval="males and females";
										break;
								}
								return rval;
							}          		
							
							/* Questions: 
							 * 1. Do we need to use band_id if max_exposure_value is undefined/null YES
							 * 2. Would average exposure value be better? YES
							 * 3. Do we want a choice: max/min/average/median/band_id/distance from nearest source
							 */
							 
//									d3.select("#homogeneityTooltip").remove();
                                    var tooltip = d3.select("#homogeneityTooltip").append("div")
                                        .attr("class", "homogeneityChart-tooltip")
                                        .style("visibility", "hidden");

                                    var margin = {top: 20, right: 20, bottom: 30, left: 40}, // Scale using CSS
                                        width = 960 - margin.left - margin.right,
                                        height = 400 - margin.top - margin.bottom;
/* Need new REST call
 genders | band_id | adjusted | observed |     expected     |      lower95      |      upper95      |   relative_risk   |     avg_exposure_value     | avg_distance_from_nearest_source
---------+---------+----------+----------+------------------+-------------------+-------------------+-------------------+----------------------------+----------------------------------
       1 |       1 |        1 |      388 | 427.777061286851 | 0.821107894649418 |  1.00190890842638 |  0.90701450618415 |     0.00000000000000000000 |            3418.7734693877551020
       1 |       2 |        1 |     2346 | 2406.33491747343 | 0.936262657024983 |  1.01518728045123 | 0.974926633431069 | 0.000000000000000000000000 |               12563.830341340076
       2 |       1 |        1 |      198 |  231.71056006628 | 0.743405880514966 | 0.982228959508871 | 0.854514355942012 |     0.00000000000000000000 |            3418.7734693877551020
       2 |       2 |        1 |     1298 | 1271.54908641107 | 0.966751520813936 |  1.07787465630659 |  1.02080211756794 | 0.000000000000000000000000 |               12563.830341340076
       3 |       1 |        1 |      586 | 661.286241242518 | 0.817230697113517 | 0.960885383035752 | 0.886151810597087 |     0.00000000000000000000 |            3418.7734693877551020
       3 |       2 |        1 |     3644 | 3671.76065065572 |  0.96073356367783 |  1.02519161807457 | 0.992439417136092 | 0.000000000000000000000000 |               12563.830341340076
(6 rows)
 
WITH b AS (
    SELECT band_id, sex AS genders, AVG(exposure_value) AS avg_exposure_value, 
								    AVG(distance_from_nearest_source) AS avg_distance_from_nearest_source
      FROM s563_extract
	 wHERE study_or_comparison = 'S'
     GROUP BY band_id, sex
	UNION
    SELECT band_id, 3 AS genders, AVG(exposure_value) AS avg_exposure_value, 
								    AVG(distance_from_nearest_source) AS avg_distance_from_nearest_source
      FROM s563_extract
	 wHERE study_or_comparison = 'S'
     GROUP BY band_id
), a AS (
    SELECT a.genders, a.band_id, adjusted, observed, expected, lower95, upper95, relative_risk, 
	       b.avg_exposure_value, b.avg_distance_from_nearest_source
      FROM s563_map a 
		LEFT OUTER JOIN b  ON (a.band_id = b.band_id AND a.genders = b.genders)
)
SELECT * FROM a
 ORDER BY 1, 2;
SELECT JSON_AGG(a) FROM a;

  */
                                    var data= {
											males: [{ // x is max_exposure_value, y is relative_risk, e is upper95
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
												"genders": 1,
												"band_id": 2,
												"adjusted": 1,
												"observed": 154,
												"expected": 160.986693034749,
												"lower95": 0.816841334381448,
												"upper95": 1.12027276266715,
												"relative_risk": 0.956600804059993,
												"max_exposure_value": 66
											},{
												"genders": 1,
												"band_id": 3,
												"adjusted": 1,
												"observed": 428,
												"expected": 398.740546340158,
												"lower95": 0.976356077183336,
												"upper95": 1.18004482604304,
												"relative_risk": 1.07337967991568,
												"max_exposure_value": 60
											}],
											females: [{
												"genders": 2,
												"band_id": 1,
												"adjusted": 1,
												"observed": 80,
												"expected": 69.225522543931,
												"lower95": 0.916353134795538,
												"upper95": 1.4382979182895,
												"relative_risk": 1.15564313652138,
												"max_exposure_value": 72
											},{
												"genders": 2,
												"band_id": 2,
												"adjusted": 1,
												"observed": 56,
												"expected": 87.6085630538271,
												"lower95": 0.48285004008843,
												"upper95": 0.830063357652665,
												"relative_risk": 0.639206922793533,
												"max_exposure_value": 66
											},{
												"genders": 2,
												"band_id": 3,
												"adjusted": 1,
												"observed": 248,
												"expected": 215.487326637354,
												"lower95": 1.01619628358071,
												"upper95": 1.3034137340007,
												"relative_risk": 1.15087974717586,
												"max_exposure_value": 60
											}],
											both: [{
												"genders": 3,
												"band_id": 1,
												"adjusted": 1,
												"observed": 202,
												"expected": 203.56522154764,
												"lower95": 0.864482796580928,
												"upper95": 1.13904063929265,
												"relative_risk": 0.992310957953723,
												"max_exposure_value": 72
											},{
												"genders": 3,
												"band_id": 2,
												"adjusted": 1,
												"observed": 210,
												"expected": 249.66589886741,
												"lower95": 0.734717697589835,
												"upper95": 0.962940900563699,
												"relative_risk": 0.841124082033824,
												"max_exposure_value": 66
											},{
												"genders": 3,
												"band_id": 3,
												"adjusted": 1,
												"observed": 676,
												"expected": 617.567300476479,
												"lower95": 1.01513378274594,
												"upper95": 1.18032478276612,
												"relative_risk": 1.09461754124358,
												"max_exposure_value": 60
											}]
									};
                                    AlertService.consoleDebug("[rifd-util-info.js] homogeneityChart data[" + gendersName + "]: " + 
										JSON.stringify(data[gendersName], null, 1));
                                    var xScale = d3.scaleLinear()
                                             .range([0, width])
                                             .domain([0, d3.max(data[gendersName], function(d) { return d.max_exposure_value; })]).nice();
                                             
                                    var yScale = d3.scaleLinear()
                                       .range([height, 0])
//                                       .domain([0, d3.max(data[gendersName], function(d) { return d.upper95*1.2; })]).nice();
                                       .domain([d3.min(data[gendersName], function(d) { return d.lower95*0.8; }), 
									            d3.max(data[gendersName], function(d) { return d.upper95*1.2; })]).nice();

                                    var xAxis = d3.axisBottom(xScale).ticks(12),
                                       yAxis = d3.axisLeft(yScale).ticks(12 * height / width);

                                    let line = d3.line()
                                        .x(function(d) {
                                        return xScale(d.max_exposure_value);
                                      })
                                      .y(function(d) {
                                        return yScale(d.relative_risk);
                                      });
	
									if (scope.svg) {
										d3.select("#homogeneityChart").select("svg").remove();
									}
                                    scope.svg = d3.select("#homogeneityChart").append("svg")
                                        .attr("width", width + margin.left + margin.right)
                                        .attr("height", height + margin.top + margin.bottom)
                                      .append("g")
                                        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
                                        
                                    scope.svg.append("g").append("rect").
                                        attr("width", width).attr("height", height).attr("class", "homogeneityChart-bg");

                                    // Add Axis labels
                                    scope.svg.append("g").attr("class", "axis axis--x")
                                    .attr("transform", "translate(" + 0 + "," + height + ")")
                                    .call(xAxis);

                                    scope.svg.append("g").attr("class", "axis axis--y").call(yAxis);

                                    // Add Error Line
                                    scope.svg.append("g").selectAll("line")
                                        .data(data[gendersName]).enter()
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
                                    scope.svg.append("g").selectAll("line")
                                        .data(data[gendersName]).enter()
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
                                    scope.svg.append("g").selectAll("line")
                                        .data(data[gendersName]).enter()
                                      .append("line")
                                      .attr("class", "homogeneityChart-error-cap")
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
                                    scope.svg.append("g").attr("class", "scatter")
                                    .selectAll("circle")
                                    .data(data[gendersName]).enter()
                                    .append("circle")
                                    .attr("cx", function(d) {
                                      return xScale(d.max_exposure_value);
                                    })
                                    .attr("cy", function(d) {
                                      return yScale(d.relative_risk);
                                    })
                                    .attr("r", 4)
                                    .on("mouseover", function(d){
										AlertService.consoleDebug("[rifd-util-info.js] homogeneityChart mouseover: " +
											JSON.stringify(d));
										var matrix = this.getScreenCTM()
											.translate(+ this.getAttribute("cx"), + this.getAttribute("cy"));
                                        return tooltip.html("Band " + d.band_id + " " +
											gender2text(d.genders) + "</br>" + 
											d.relative_risk.toFixed(3) + 
											"&nbsp;[95% CI&nbsp;" + d.lower95.toFixed(3) +
											"&ndash;" + d.upper95.toFixed(3) + "]")
                                            .style("visibility", "visible")
  //                                        .style("top", (event.pageY-17)+"px").
  //										.style("left",(event.pageX+25)+"px");
											.style("left", (window.pageXOffset + matrix.e + 15) + "px")
											.style("top", (window.pageYOffset + matrix.f - 30) + "px");
                                     })
                                    .on("mouseout", function(){
                                        return tooltip.style("visibility", "hidden");
                                     });  
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
							scope.showHomogeneityCharts=false;
                            scope.covariateList = [];
                            scope.covariateType = null;
                            scope.covariateDescription = null;
                            scope.covariateDescriptions = {};
                            scope.covariateHtml = {};
                            scope.isRiskAnalysisStudy=false;
                            scope.hasCovariates=false;
                            scope.hSplit1 = 100;
                            scope.gendersName="males";
							scope.gendersList = ['males', 'females', 'both'];
							scope.svg  = undefined;
							
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
                                scope.reportList = ["Summary", "Covariate Loss Report", "Homogeneity Tests", "Homogeneity Charts"];
                            }
                            else {
                                scope.reportList = ["Summary", "Covariate Loss Report"];
                            }
                        
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
                            scope.homogeneityChartHeader = $sce.trustAsHtml('<header class="info-header">Fetching Study Homogeneity Charts for ' + 
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
                            user.getHealthCodesForProcessedStudy(user.currentUser, thisStudy).then(function (invx) {
                                var inv = invx.data[0];

                                user.getDetailsForProcessedStudy(user.currentUser, thisStudy).then(function (res) {
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

                                        scope.summary = $sce.trustAsHtml(project);    
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
				
                                                scope.d3HomogeneityChartChange(scope.gendersName);
                                            
                                                AlertService.consoleDebug("[rifd-util-info.js] homogeneityTestsHtml: " + 
                                                    homogeneityTestsHtml);
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
                                    AlertService.consoleDebug("[rifd-util-info.js] no Covariate Loss Report: " + 
                                        JSON.stringify(res.data, null, 2));
                                }   
                                else {
                                    scope.covariateLossReport = $sce.trustAsHtml(covariateLossReportHtml);
                                    AlertService.consoleDebug("[rifd-util-info.js] Covariate Loss Report: " + 
                                        JSON.stringify(res.data, null, 2));
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