RIF.modelMapper = (function (modelAccessor) {

  return function (accessor) {

    var _mapper = {

      "rif_job_submission": {

        "submitted_by": RIF.user,

        "job_submission_date": function () {
          var d = new Date();
          var n = d.getTime();
          return n;
        }(),

        "project": {
          "name": accessor.getProject(),
          "description": accessor.getDescription()
        },

        "disease_mapping_study": {

          "name": accessor.getStudyName(),
          "description": "",
          "geography": {
            "name": RIF.geography,
            "description": "xxx"
          },

          "disease_mapping_study_area": {
            "geo_levels": {
              "geolevel_select": {
                name: accessor.getStudyAreaSelectAt()
              },
              "geolevel_view": {
                name: accessor.getStudyAreaResolution()
              },
              "geolevel_area": '',
              "geolevel_to_map": ''
            },
            "map_areas": {
              "map_area": accessor.getStudyAreas()
            }
          },

          "comparison_area": {
            "geo_levels": {
              "geolevel_select": {
                name: accessor.getComparisonAreaAreaSelectAt()
              },
              "geolevel_view": {
                name: accessor.getComparisonAreaResolution()
              },
              "geolevel_area": '',
              "geolevel_to_map": ''
            },
            "map_areas": {
              "map_area": accessor.getComparisonAreas()
            }
          },

          "investigations": {
            "investigation": function () {
              var invs = accessor.getInvestigations();
              var investigations = [];
              for (var l in invs) {
                var inv = {};
                //inv[ "-id" ] = parseInt( l ) + 1;
                inv["title"] = 'investigation_' + (parseInt(l) + 1);
                inv["health_theme"] = {
                  "name": accessor.getHealthTheme(),
                  "description": ""
                };
                inv["numerator_denominator_pair"] = {
                  "numerator_table_name": invs[l]["numerator"] || accessor.getNumerator(),
                  "numerator_table_description": "",
                  "denominator_table_name": invs[l]["denominator"] || accessor.getDenominator(),
                  "denominator_table_description": ""
                };
                var ageGroups = accessor.getAgeGroups();
                inv["age_band"] = {
                  "lower_age_group": {
                    "id": "",
                    "name": ageGroups.lower.name,
                    "lower_limit": ageGroups.lower.ageLimits.split('-')[0],
                    "upper_limit": ageGroups.lower.ageLimits.split('-')[1]
                  },
                  "upper_age_group": {
                    "id": "",
                    "name": ageGroups.upper.name,
                    "lower_limit": ageGroups.upper.ageLimits.split('-')[0],
                    "upper_limit": ageGroups.upper.ageLimits.split('-')[1]
                  }
                };
                inv["health_codes"] = function () {
                  var outcomes = invs[l]["healthOutcomes"];
                  var mappedOutcomes = [];
                  for (var h in outcomes) {
                    var oLength = outcomes[h].length;
                    while (oLength--) {
                      mappedOutcomes.push({
                        "name_space": h,
                        "code": outcomes[h][oLength]["code"],
                        "description": outcomes[h][oLength]["description"],
                        "is_top_level_term": "no"
                      });
                    };
                  };
                  return {
                    "health_code": mappedOutcomes
                  };
                }();
                inv["year_range"] = {
                  "lower_bound": accessor.getMinYear(),
                  "upper_bound": accessor.getMaxYear()
                };

                inv["years_per_interval"] = 1;
                inv["covariates"] = function () {
                  var mappedCovs = {};
                  mappedCovs.adjustable_covariate = [];
                  var covs = accessor.getCovariates();
                  var l = (covs != null) ? covs.length : 0;
                  while (l--) {
                    mappedCovs.adjustable_covariate.push({
                      name: covs[l]
                    });
                  };
                  return mappedCovs;
                }();
                inv["sex"] = invs[l]["gender"];
                investigations.push(inv);
              };
              return investigations;
            }()
          }
        },
        "calculation_methods": {
          "calculation_method": accessor.getCalculationMethods()
        }
      }
    };
    console.log(JSON.stringify(_mapper));
    return _mapper;

  };
});