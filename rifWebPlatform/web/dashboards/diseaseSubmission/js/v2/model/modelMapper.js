RIF.modelMapper = ( function ( modelAccessor ) {

  /*var _mapper = {
      "rif_job_submission": {
        "submitted_by": "kgarwood",
        "job_submission_date": "17-Apr-2015 16:14:08:965",
        "disease_mapping_study": {
          "name": "public health study",
          "disease_mapping_study_area": {
            "geo_levels": {
              "geolevel_select":'',
              "geolevel_area":'',
              "geolevel_view": '',
              "geolevel_to_map": ''
            },
            "map_areas": {
              "map_area": [
                {
                  "id": "01.011.012600.1",
                  "label": "01.011.012600.1"
                },
                {
                  "id": "01.011.012600.2",
                  "label": "01.011.012600.2"
                }
              ]
            }
          },
          "comparison_area": {
            "geo_levels": {
              "geolevel_select": '',
              "geolevel_area": '',
              "geolevel_view":'',
              "geolevel_to_map": ''
            },
            "map_areas": {
              "map_area": [
                {
                  "id": "01.011.012600.1",
                  "label": "01.011.012600.1"
                },
                {
                  "id": "01.011.012600.2",
                  "label": "01.011.012600.2"
                }
              ]
            }
          },
          "investigations": {
            "investigation": [
              {
                "-id": "9",
                "title": "CANCERSTUDY1",
                "numerator_denominator_pair": {
                  "numerator_table_name": "SAHSULAND_CANCER",
                  "numerator_table_description": "Cancer cases in SAHSU land",
                  "denominator_table_name": "SAHSULAND_POP",
                  "denominator_table_description": "SAHSU land population"
                },
                "year_range": {
                  "lower_bound": "1992",
                  "upper_bound": "1997"
                },
                "sex": "Both"
              },
              {
                "-id": "10",
                "title": "BRAINCANCERSTUDY",
                "numerator_denominator_pair": {
                  "numerator_table_name": "SAHSULAND_CANCER",
                  "numerator_table_description": "Cancer cases in SAHSU land",
                  "denominator_table_name": "SAHSULAND_POP",
                  "denominator_table_description": "SAHSU land population"
                },
                "year_range": {
                  "lower_bound": "1992",
                  "upper_bound": "1997"
                },
                "sex": "Both"
              }
            ]
          }
        }
      }
    }*/


  return function ( accessor ) {

    var _mapper = {

      "rif_job_submission": {

        "submitted_by": RIF.user,

        "job_submission_date": function () {
          var d = new Date();
          var n = d.getTime();
          return n;
        }(),

        "disease_mapping_study": {

          "name": accessor.getStudyName(),

          "disease_mapping_study_area": {
            "geo_levels": {
              "geolevel_select": accessor.getStudyAreaSelectAt(),
              "geolevel_view": accessor.getStudyAreaResolution(),
              "geolevel_area": '',
              "geolevel_to_map": ''
            },
            "map_areas": {
              "map_area": accessor.getStudyAreas()
            }
          },

          "comparison_area": {
            "geo_levels": {
              "geolevel_select": accessor.getComparisonAreaAreaSelectAt(),
              "geolevel_view": accessor.getComparisonAreaResolution(),
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
              for ( var l in invs ) {
                var inv = {};
                inv[ "-id" ] = parseInt( l ) + 1;
                inv[ "title" ] = 'investigation_' + parseInt( l ) + 1;
                inv[ "numerator_denominator_pair" ] = {
                  "numerator_table_name": invs[ l ][ "numerator" ] || accessor.getNumerator(),
                  "numerator_table_description": "",
                  "denominator_table_name": invs[ l ][ "denominator" ] || accessor.getDenominator(),
                  "denominator_table_description": ""
                };
                inv[ "health_codes" ] = function () {
                  var outcomes = invs[ l ][ "healthOutcomes" ];
                  var mappedOutcomes = [];
                  for ( var h in outcomes ) {
                    var oLength = outcomes[ h ].length;
                    while ( oLength-- ) {
                      mappedOutcomes.push( {
                        "health_code": {
                          "taxonomy": h,
                          "code": outcomes[ h ][ oLength ][ "code" ],
                          "description": outcomes[ h ][ oLength ][ "description" ]
                        }
                      } );
                    };
                  };
                  return mappedOutcomes;
                }();
                inv[ "year_range" ] = {
                  "lower_bound": invs[ l ][ "minYear" ],
                  "upper_bound": invs[ l ][ "maxYear" ],
                };
                inv[ "sex" ] = invs[ l ][ "gender" ];
                investigations.push( inv );
              };
              return investigations;
            }()
            /*[
              {
                "-id": "9",
                "title": "CANCERSTUDY1",
                "numerator_denominator_pair": {
                  "numerator_table_name": "SAHSULAND_CANCER",
                  "numerator_table_description": "Cancer cases in SAHSU land",
                  "denominator_table_name": "SAHSULAND_POP",
                  "denominator_table_description": "SAHSU land population"
                },
                "year_range": {
                  "lower_bound": "1992",
                  "upper_bound": "1997"
                },
                "sex": "Both"
              }
            ]*/
          }
        }
      }
    };
    console.log( JSON.stringify( _mapper ) )
  };
} );