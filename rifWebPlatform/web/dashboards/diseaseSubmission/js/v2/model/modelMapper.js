RIF.modelMapper = ( function ( modelAccessor ) {

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
                inv[ "title" ] = 'investigation_' + ( parseInt( l ) + 1 );
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
                        "name_space": h,
                        "code": outcomes[ h ][ oLength ][ "code" ],
                        "description": outcomes[ h ][ oLength ][ "description" ],
                        "is_top_level_term": "false"
                      } );
                    };
                  };
                  return {
                    "health_code": mappedOutcomes
                  };
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
          }
        }
      }
    };

    console.log( JSON.stringify( _mapper ) )

  };
} );