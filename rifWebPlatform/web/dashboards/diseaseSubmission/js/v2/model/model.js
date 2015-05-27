RIF.model = (function () {

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
      studyArea_areasFromMap: [],
      studyArea_selectAt: null
    },

    comparisonArea: {
      comparisonArea_resolution: null,
      comparisonArea_areas: [],
      comparisonArea__areasFromMap: [],
      comparisonArea_selectAt: null
    },

    calculationMethods: [], //[{name:'', parameterProxies:[{"code_routine_name":"a","value":"5"}]}]  
  };

  return _model;

});