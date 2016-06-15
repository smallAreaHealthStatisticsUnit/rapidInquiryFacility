/* SERVICE storing results of the submission stage
 * RIF.modelMapper 
 * 
 */
angular.module("RIF")
        .factory('ModelService', function () {
            var model = {
                "rif_job_submission": {
                    "submitted_by": "NULL",
                    "job_submission_date": function () {
                        var d = new Date();
                        return d.toLocaleDateString() + " " + d.toLocaleTimeString();
                    }(),
                    "project": {
                        "name": "NULL",
                        "description": "NULL"
                    },
                    "disease_mapping_study": {
                        "name": "accessor.getStudyName()",
                        "description": "",
                        "geography": {
                            "name": "RIF.geography",
                            "description": "Geography description cannot be edited from web interface"
                        },
                        "disease_mapping_study_area": {
                            "geo_levels": {
                                "geolevel_select": {
                                    name: "accessor.getStudyAreaSelectAt()"
                                },
                                "geolevel_view": {
                                    name: "accessor.getStudyAreaResolution()"
                                },
                                "geolevel_to_map": {
                                    "name": "accessor.getStudyAreaResolution()"
                                },
                                "geolevel_area": ''
                            },
                            "map_areas": {
                                "map_area": "accessor.getStudyAreas()"
                            }
                        },
                        "comparison_area": {
                            "geo_levels": {
                                "geolevel_select": {
                                    name: "accessor.getComparisonAreaAreaSelectAt()"
                                },
                                "geolevel_view": {
                                    name: "accessor.getComparisonAreaResolution()"
                                },
                                "geolevel_to_map": {
                                    name: "accessor.getComparisonAreaResolution()"
                                },
                                "geolevel_area": ''
                            },
                            "map_areas": {
                                "map_area": "accessor.getComparisonAreas()"
                            }
                        },
                        "investigations": {
                            "investigation": "NULL"
                        }
                    },
                    "calculation_methods": {
                        "calculation_method": "NULL"
                    }
                }
            };
            return {
                //return the job submission
                get_rif_job_submission: function () {
                    return model;
                },
                //method to reset all fields
                reset_rif_job_submission: function () {
                    //TODO:
                },
                //SUBMITTED BY

                //PROJECT
                set_name: function (s) {
                    model.rif_job_submission.project.name = s;
                },
                set_description: function (s) {
                    model.rif_job_submission.project.description = s;
                },
                //DISEASE MAPPING STUDY








                //CALCULATION METHODS
                set_calculationmethod: function (s) {
                    var method = {
                        "name": "",
                        "code_routine_name": "",
                        "description": "",
                        "parameters": ""
                    };
                    if (s.checked === "1") {
                        method.name = "BYM";
                        method.code_routine_name = "bym_r_proceedure";
                        method.description = "Besag, York and Mollie Model";
                        method.parameters = {"parameter": {"name": "c", "value": s.bym_c}};
                    } else if (s.checked === "2") {
                        method.name = "HET";
                        method.code_routine_name = "het_r_proceedure";
                        method.description = "Heterogenous Model";
                        method.parameters = {"parameter": [{"name": "a", "value": s.het_a}, {"name": "b", "value": s.het_b}]};
                    } else if (s.checked === "3") {
                        method.name = "CAR";
                        method.code_routine_name = "car_r_proceedure";
                        method.description = "Conditional autoregressive";
                        method.parameters = {"parameter": {"name": "a", "value": s.car_a}};
                    }
                    model.rif_job_submission.calculation_methods.calculation_method = method;
                }
            };
        });