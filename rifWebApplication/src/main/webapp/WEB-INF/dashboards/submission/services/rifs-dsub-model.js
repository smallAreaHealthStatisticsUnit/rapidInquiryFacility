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
 * SERVICE storing results of the submission stage
 */
angular.module("RIF")
        .factory('ModelService', function (StudyAreaStateService, CompAreaStateService, StatsStateService,
                SubmissionStateService, ParameterStateService, user) {
            updateModel = function () {
                var model = {
                    "rif_job_submission": {
                        "submitted_by": user.currentUser,
                        "job_submission_date": function () {
                            var d = new Date();
                            return d.toLocaleDateString() + " " + d.toLocaleTimeString();
                        }(),
                        "project": {
                            "name": SubmissionStateService.getState().projectName,
                            "description": SubmissionStateService.getState().projectDescription
                        },
                        "disease_mapping_study": {
                            "name": SubmissionStateService.getState().studyName,
                            "description": SubmissionStateService.getState().studyDescription,
                            "geography": {
                                "name": SubmissionStateService.getState().geography,
                                "description": SubmissionStateService.getState().geography
                            },
                            "disease_mapping_study_area": {
                                "geo_levels": {
                                    "geolevel_select": {
                                        name: StudyAreaStateService.getState().selectAt
                                    },
                                    "geolevel_area": {
                                        name: ""
                                    },
                                    "geolevel_view": {
                                        name: StudyAreaStateService.getState().studyResolution
                                    },
                                    "geolevel_to_map": {
                                        name: StudyAreaStateService.getState().studyResolution
                                    }
                                },
                                "map_areas": {
                                    "map_area": function () {
                                        var tmp = [];
                                        for (var i = 0; i < StudyAreaStateService.getState().polygonIDs.length; i++) {
                                            tmp.push({
                                                "id": StudyAreaStateService.getState().polygonIDs[i].id,
                                                "gid": StudyAreaStateService.getState().polygonIDs[i].gid,
                                                "label": StudyAreaStateService.getState().polygonIDs[i].label,
                                                "band": StudyAreaStateService.getState().polygonIDs[i].band
                                            });
                                        }
                                        return tmp;
                                    }()
                                }
                            },
                            "comparison_area": {
                                "geo_levels": {
                                    "geolevel_select": {
                                        name: CompAreaStateService.getState().selectAt
                                    },
                                    "geolevel_area": {
                                        name: ""
                                    },
                                    "geolevel_view": {
                                        name: CompAreaStateService.getState().studyResolution
                                    },
                                    "geolevel_to_map": {
                                        name: CompAreaStateService.getState().studyResolution
                                    }
                                },
                                "map_areas": {
                                    "map_area": function () {
                                        var tmp = [];
                                        for (var i = 0; i < CompAreaStateService.getState().polygonIDs.length; i++) {
                                            tmp.push({
                                                "id": CompAreaStateService.getState().polygonIDs[i].id,
                                                "gid": CompAreaStateService.getState().polygonIDs[i].gid,
                                                "label": CompAreaStateService.getState().polygonIDs[i].label,
                                                "band": CompAreaStateService.getState().polygonIDs[i].band
                                            });
                                        }
                                        return tmp;
                                    }()
                                }
                            },
                            "investigations": {"investigation": ParameterStateService.getModelInvestigation()}
                        },
                        "calculation_methods": {
                            "calculation_method": StatsStateService.getModelStats()
                        },
                        "rif_output_options": {
                            "rif_output_option": [
                                "Data",
                                "Maps",
                                "Ratios and Rates"
                            ]
                        }
                    }
                };
                return model;
            };
            //HTML formatting functions below
            areaTable = function (areas) {
                var studyAreaTable = "<table><tr>" +
                        "<th>ID</th>" +
                        "<th>Label</th>" +
                        "<th>Band</th>";
                for (var i = 0; i < areas.length; i++) {
                    studyAreaTable += "<tr><td>" + areas[i].id + "</td><td>" + areas[i].label + "</td><td>" + areas[i].band + "</td></tr>";
                }
                return studyAreaTable + "</table>";
            };
            investigationTable = function (inv) {
                var studyTable = "<table><tr>" +
                        "<th>Title</th>" +
                        "<th>Identifier</th>" +
                        "<th>Description</th>" +
                        "<th>Years</th>" +
                        "<th>Sex</th>" +
                        "<th>Age Range</th>" +
                        "<th>Covariates</th>" +
                        "</tr>";
                for (var i = 0; i < inv.length; i++) {
                    var covars = "";
                    for (var v = 0; v < inv[i].covariates.length; v++) {
                        covars += inv[i].covariates[v].adjustable_covariate.name;
                        if (v !== inv[i].covariates.length - 1) {
                            covars += "; ";
                        }
                    }
                    for (var j = 0; j < inv[i].health_codes.health_code.length; j++) {
                        if (j === 0) {
                            studyTable += "<tr><td>" + inv[i].title + "</td><td>" +
                                    inv[i].health_codes.health_code[j].code + "-" +
                                    inv[i].health_codes.health_code[j].name_space + "</td>" +
                                    "<td>" + inv[i].health_codes.health_code[j].description + "</td>" +
                                    "<td>" + inv[i].year_range.lower_bound + "-" + inv[i].year_range.upper_bound + "</td>" +
                                    "<td>" + inv[i].sex + "</td>" +
                                    "<td> LWR: " + inv[i].age_band.lower_age_group.name + ", UPR: " + inv[i].age_band.upper_age_group.name + "</td>" +
                                    "<td>" + covars + "</td>" +
                                    "</tr>";
                        } else {
                            studyTable += "<tr><td></td>" + "</td><td>" +
                                    inv[i].health_codes.health_code[j].code + "-" +
                                    inv[i].health_codes.health_code[j].name_space + "</td>" +
                                    "<td>" + inv[i].health_codes.health_code[j].description + "</td>" +
                                    "</tr>";
                        }
                    }
                }
                return studyTable + "</table>";
            };
            _getAttr = function (v) {
                return '<attr>' + v + '</attr></br>';
            };

            return {
                //return the job submission as unformatted JSON
                get_rif_job_submission_JSON: function () {
                    return updateModel();
                },
                //return the job submission as formatted HTML
                get_rif_job_submission_HTML: function () {
                    var modelJSON = updateModel();

                    //Overview
                    var project = '<header>Overview</header><section>Project Name:</section>' + _getAttr(modelJSON.rif_job_submission.project.name) +
                            '<section>Project Description:</section>' + _getAttr(modelJSON.rif_job_submission.project.description) +
                            '<section>Submitted By:</section>' + _getAttr(modelJSON.rif_job_submission.submitted_by) +
                            '<section>Date:</section>' + _getAttr(modelJSON.rif_job_submission.job_submission_date) +
                            '<section>Study Name:</section>' + _getAttr(modelJSON.rif_job_submission.disease_mapping_study.name) +
                            '<section>Study Description:</section>' + _getAttr(modelJSON.rif_job_submission.disease_mapping_study.description) +
                            '<section>Geography:</section>' + _getAttr(modelJSON.rif_job_submission.disease_mapping_study.geography.name);

                    //Study area
                    project += '<header>Study Area</header>' +
                            '<section>Selection Resolution:</section>' + _getAttr(modelJSON.rif_job_submission.disease_mapping_study.disease_mapping_study_area.geo_levels.geolevel_select.name) +
                            '<section>Resolution of Results:</section>' + _getAttr(modelJSON.rif_job_submission.disease_mapping_study.disease_mapping_study_area.geo_levels.geolevel_to_map.name);
                    if (modelJSON.rif_job_submission.disease_mapping_study.disease_mapping_study_area.map_areas.map_area.length !== 0) {
                        project += areaTable(modelJSON.rif_job_submission.disease_mapping_study.disease_mapping_study_area.map_areas.map_area);
                    }

                    //Comparision area
                    project += '<header>Comparison Area</header>' +
                            '<section>Selection Resolution:</section>' + _getAttr(modelJSON.rif_job_submission.disease_mapping_study.comparison_area.geo_levels.geolevel_select.name) +
                            '<section>Resolution of Results:</section>' + _getAttr(modelJSON.rif_job_submission.disease_mapping_study.comparison_area.geo_levels.geolevel_to_map.name);
                    if (modelJSON.rif_job_submission.disease_mapping_study.comparison_area.map_areas.map_area.length !== 0) {
                        project += areaTable(modelJSON.rif_job_submission.disease_mapping_study.comparison_area.map_areas.map_area);
                    }

                    //Investigations
                    var investigations = modelJSON.rif_job_submission.disease_mapping_study.investigations.investigation;
                    project += '<header>Investigations</header>';
                    if (investigations) {
                        project += '<section>Health Theme:</section>' + _getAttr(investigations[0].health_theme.name + " - " + investigations[0].health_theme.description) +
                                '<section>Numerator Table:</section>' + _getAttr(investigations[0].numerator_denominator_pair.numerator_table_name + " - " +
                                        investigations[0].numerator_denominator_pair.numerator_table_description) +
                                '<section>Denominator Table:</section>' + _getAttr(investigations[0].numerator_denominator_pair.denominator_table_name + " - " +
                                        investigations[0].numerator_denominator_pair.denominator_table_description);
                    }
                    if (modelJSON.rif_job_submission.disease_mapping_study.investigations.investigation) {
                        project += investigationTable(investigations);
                    }

                    //Statistics
                    project += '<header>Statistics</header>';
                    var statistics = modelJSON.rif_job_submission.calculation_methods.calculation_method;
                    project += '<section>Calculation Method:</section>' + _getAttr(statistics.code_routine_name);

                    //AT PRESENT USER DOES NOT SUPPLY ANY PARAMETERS FOR SMOOTHING SCRIPT
                    /*
                     var statParams = "";
                     for (var i = 0; i < statistics.parameters.parameter.length; i++) {
                     statParams += statistics.parameters.parameter[i].name + ": " +
                     statistics.parameters.parameter[i].value + "; ";
                     }
                     statParams = statParams.substring(0, statParams.length - 2);
                     project += '<section>Parameters:</section>' + _getAttr(statParams);
                     */
                    //Output
                    //DEPRECIATED?
//                    project += '<header>Output Options</header>';
//                    var outputOptions = "";
//                    for (var i = 0; i < modelJSON.rif_job_submission.rif_output_options.rif_output_option.length; i++) {
//                        outputOptions += modelJSON.rif_job_submission.rif_output_options.rif_output_option[i] + "; ";
//                    }
//                    outputOptions = outputOptions.substring(0, outputOptions.length - 2);
//                    project += '<section>Options:</section>' + _getAttr(outputOptions);
                    return project;
                }
            };
        });