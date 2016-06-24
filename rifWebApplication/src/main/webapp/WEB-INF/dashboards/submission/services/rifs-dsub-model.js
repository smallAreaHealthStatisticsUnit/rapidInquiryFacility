/* SERVICE storing results of the submission stage
 * This is filled by the states provided in services in dsub dashboard
 * 
 */
angular.module("RIF")
        .factory('ModelService', function (StudyAreaStateService, CompAreaStateService, StatsStateService, SubmissionStateService) {
            updateModel = function () {
                var model = {
                    "rif_job_submission": {
                        "submitted_by": "TODO: USERNAME",
                        "job_submission_date": function () {
                            var d = new Date();
                            return d.toLocaleDateString() + " " + d.toLocaleTimeString();
                        }(),
                        "project": {
                            "name": SubmissionStateService.get_state().projectName,
                            "description": SubmissionStateService.get_state().projectDescription
                        },
                        "disease_mapping_study": {
                            "name": SubmissionStateService.get_state().studyName,
                            "description": "TODO: UNKNOWN VARIABLE",
                            "geography": {
                                "name": "TODO: RIF.geography",
                                "description": "TODO: Geography description cannot be edited from web interface"
                            },
                            "disease_mapping_study_area": {
                                "geo_levels": {
                                    "geolevel_select": {
                                        name: StudyAreaStateService.get_state().selectAt
                                    },
                                    "geolevel_view": {
                                        name: StudyAreaStateService.get_state().studyResolution //TODO: why are there the same?
                                    },
                                    "geolevel_to_map": {
                                        name: StudyAreaStateService.get_state().studyResolution
                                    },
                                    "geolevel_area": "TODO: UNKNOWN VARIABLE"
                                },
                                "map_areas": {
                                    "map_area": StudyAreaStateService.get_state().polygonIDs //TODO: is gid needed when it is the same as id?
                                }
                            },
                            "comparison_area": {
                                "geo_levels": {
                                    "geolevel_select": {
                                        name: CompAreaStateService.get_state().selectAt
                                    },
                                    "geolevel_view": {
                                        name: CompAreaStateService.get_state().studyResolution
                                    },
                                    "geolevel_to_map": {
                                        name: CompAreaStateService.get_state().studyResolution
                                    },
                                    "geolevel_area": "TODO: UNKNOWN VARIABLE"
                                },
                                "map_areas": {
                                    "map_area": CompAreaStateService.get_state().polygonIDs
                                }
                            },
                            "investigations": {
                                "investigation": "TODO: NULL"
                            }
                        },
                        "calculation_methods": {
                            "calculation_method": StatsStateService.get_modelStats()
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