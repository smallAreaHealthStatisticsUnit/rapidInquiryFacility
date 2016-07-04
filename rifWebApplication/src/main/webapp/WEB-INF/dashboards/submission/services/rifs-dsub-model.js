/* SERVICE storing results of the submission stage
 * This is filled by the states provided in services in dsub dashboard
 * 
 */
angular.module("RIF")
        .factory('ModelService', function (StudyAreaStateService, CompAreaStateService, StatsStateService,
                SubmissionStateService, ParameterStateService) {
            updateModel = function () {
                var model = {
                    "rif_job_submission": {
                        "submitted_by": "TODO: USERNAME",
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
                            "description": "TODO: UNKNOWN VARIABLE",
                            "geography": {
                                "name": "TODO: RIF.geography",
                                "description": "TODO: Geography description cannot be edited from web interface"
                            },
                            "disease_mapping_study_area": {
                                "geo_levels": {
                                    "geolevel_select": {
                                        name: StudyAreaStateService.getState().selectAt
                                    },
                                    "geolevel_view": {
                                        name: StudyAreaStateService.getState().studyResolution //TODO: why are there the same?
                                    },
                                    "geolevel_to_map": {
                                        name: StudyAreaStateService.getState().studyResolution
                                    },
                                    "geolevel_area": "TODO: UNKNOWN VARIABLE"
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
                                    "geolevel_view": {
                                        name: CompAreaStateService.getState().studyResolution
                                    },
                                    "geolevel_to_map": {
                                        name: CompAreaStateService.getState().studyResolution
                                    },
                                    "geolevel_area": "TODO: UNKNOWN VARIABLE"
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
            return {
                //return the job submission
                get_rif_job_submission: function () {
                    return updateModel();
                },
                //method to reset all fields
                reset_rif_job_submission: function () {
                    //TODO: call all reset methods is services
                }
            };
        });