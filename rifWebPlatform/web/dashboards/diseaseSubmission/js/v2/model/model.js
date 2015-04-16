RIF.model = ( function () {

  var _model = {

    studyName: null,
    healthTheme: null,
    numerator: null,
    denominator: null,
    project: null,
    description: null,

    investigations: {}, //parameters: healthOutcomes - ageGroups - minYear - maxYear - gender - covariates

    studyArea: {
      studyArea_resolution: null,
      studyArea_areas: [],
      studyArea_selectAt: null
    },

    comparisonArea: {
      comparisonArea_resolution: null,
      comparisonArea_areas: [],
      comparisonArea_selectAt: null
    }

  };




  return _model;

} );