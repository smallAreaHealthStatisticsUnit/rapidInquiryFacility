/* SERVICE storing results of the submission stage
 * This is filled by the states provided in services in dsub dashboard
 * Returns either JSON or HTML for the study summary dialogue
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
                                    "map_area": StudyAreaStateService.getState().polygonIDs //TODO: is gid needed when it is the same as id?
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
                                    "map_area": CompAreaStateService.getState().polygonIDs
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
                        "<th>Interval</th>" +
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
                                    "<td>" + inv[i].years_per_interval + "</td>" +
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
                    if (!angular.isUndefined(statistics.code_routine_name)) {
                        project += '<section>Calculation Method:</section>' + _getAttr(statistics.code_routine_name);
                        var statParams = "";
                        for (var i = 0; i < statistics.parameters.parameter.length; i++) {
                            statParams += statistics.parameters.parameter[i].name + ": " +
                                    statistics.parameters.parameter[i].value + "; ";
                        }
                        statParams = statParams.substring(0, statParams.length - 2);
                        project += '<section>Parameters:</section>' + _getAttr(statParams);
                    }

                    //Output
                    project += '<header>Output Options</header>';
                    var outputOptions = "";
                    for (var i = 0; i < modelJSON.rif_job_submission.rif_output_options.rif_output_option.length; i++) {
                        outputOptions += modelJSON.rif_job_submission.rif_output_options.rif_output_option[i] + "; ";
                    }
                    outputOptions = outputOptions.substring(0, outputOptions.length - 2);
                    project += '<section>Options:</section>' + _getAttr(outputOptions);
                    return project;
                }
            };
        });