RIF.modelAccessor = (function () {


  var model = RIF.model();
  var mapper = RIF.modelMapper();

  var _mandatory = ["studyName", "healthTheme", "numerator", "denominator", "project", "investigations",
    "studyArea.studyArea_resolution", "studyArea.studyArea_areas",
    "studyArea.studyArea_selectAt", "comparisonArea.comparisonArea_resolution", "comparisonArea.comparisonArea_areas",
    "comparisonArea.comparisonArea_selectAt"
  ];

  var _optional = ["covariates", "description"];


  var modelAccessor = {

    parameters: {
      healthOutcomes: null,
      ageGroups: null,
      minYear: null,
      maxYear: null,
      gender: null,
      covariates: null,
    },

    //SETTERS
    setStudyName: function (s) {
      model.studyName = s;
      mapper(modelAccessor);
    },
    setHealthTheme: function (s) {
      model.healthTheme = s;
    },
    setNumerator: function (s) {
      model.numerator = s;
    },
    setDenominator: function (s) {
      model.denominator = s;
    },
    setProject: function (s) {
      model.project = s;
    },
    setDescription: function (s) {
      model.description = s;
    },
    setStudyAreaSelectAt: function (s) {
      model.studyArea.studyArea_selectAt = s;
    },
    setStudyAreaResolution: function (s) {
      model.studyArea.studyArea_resolution = s;
    },
    setStudyAreas: function (s) {
      model.studyArea.studyArea_areas = s;
    },
    setStudyMapAreas: function (s) {
      model.studyArea.studyArea_areasFromMap = s;
    },
    setComparisonAreaSelectAt: function (s) {
      model.comparisonArea.comparisonArea_selectAt = s;
    },
    setComparisonAreaResolution: function (s) {
      model.comparisonArea.comparisonArea_resolution = s;
    },
    setComparisonAreas: function (s) {
      model.comparisonArea.comparisonArea_areas = s;
    },
    setComparisonMapAreas: function (s) {
      model.comparisonArea.comparisonArea_areasFromMap = s;
    },
    setHealthConditionTaxonomy: function (s) {
      this.parameters.taxonomy = s;
    },
    setHealthOutcomes: function (s) {
      this.parameters.healthOutcomes = s;
    },
    setMinYear: function (s) {
      this.parameters.minYear = s;
    },
    setMaxYear: function (s) {
      this.parameters.maxYear = s;
    },
    setGender: function (s) {
      this.parameters.gender = s;
    },
    setCovariates: function (s) {
      this.parameters.covariates = s;
    },
    setAgeGroups: function (s) {
      this.parameters.ageGroups = s;
    },
    setParameter: function (param, s) {
      this.parameters[param] = s;
    },
    setCalculationMethods: function (s) {
      model.calculationMethods = s;
    },

    //UNSET
    unsetInvestigation: function (i) {
      delete model.investigations[i];
    },
    unsetParameter: function (i, h) {
      delete this.parameters[i][h];
    },

    //GETTERS
    getMappedObject: function () {
      return mapper.call(this);
    },

    get: function (param) {
      var props = param.split(".");
      return (props.length > 1) ?
        model[props[0]][props[1]] :
        (props.length == 1) ?
        model[props[0]] : null;
    },

    getInvestigations: function () {
      return model.investigations;
    },
    getStudyName: function () {
      return model.studyName;
    },
    getHealthTheme: function () {
      return model.healthTheme;
    },
    getDescription: function () {
      return model.description;
    },
    getProject: function () {
      return model.project;
    },
    getNumerator: function () {
      return model.numerator;
    },
    getDenominator: function () {
      return model.denominator;
    },
    getStudyAreas: function () {
      return model.studyArea.studyArea_areas;
    },
    getStudyAreaFromMap: function () {
      return model.studyArea.studyArea_areasFromMap;
    },
    getStudyAreaSelectAt: function () {
      return model.studyArea.studyArea_selectAt;
    },
    getStudyAreaResolution: function () {
      return model.studyArea.studyArea_resolution;
    },
    getStudyArea: function () {
      return model.studyArea;
    },
    getComparisonArea: function () {
      return model.comparisonArea;
    },
    getComparisonAreaAreaSelectAt: function () {
      return model.comparisonArea.comparisonArea_selectAt;
    },
    getComparisonAreaResolution: function () {
      return model.comparisonArea.comparisonArea_resolution;
    },
    getComparisonAreas: function () {
      return model.comparisonArea.comparisonArea_areas;
    },
    getComparisonAreaFromMap: function () {
      return model.comparisonArea.comparisonArea_areasFromMap;
    },
    getComparisonArea: function () {
      return model.comparisonArea;
    },
    getComparisonArea: function () {
      return model.comparisonArea;
    },
    getHealthConditionTaxonomy: function () {
      return this.parameters.taxonomy;
    },
    getHealthOutcomes: function () {
      return this.parameters.healthOutcomes;
    },
    getMinYear: function () {
      return this.parameters.minYear;
    },
    getMaxYear: function () {
      return this.parameters.maxYear;
    },
    getGender: function () {
      return this.parameters.gender;
    },
    getCovariates: function () {
      return this.parameters.covariates;
    },
    getAgeGroups: function () {
      return this.parameters.ageGroups;
    },
    getCalculationMethods: function () {
      return model.calculationMethods;
    },
    getParameters: function () {
      return this.parameters;
    },
    getMandatoryVariablesNames: function () {
      return _mandatory;
    },
    getOptionals: function () {
      return _optional;
    },

    mapToSchema: function () {
      return mapper(modelAccessor);
    }



  };



  return modelAccessor;
});