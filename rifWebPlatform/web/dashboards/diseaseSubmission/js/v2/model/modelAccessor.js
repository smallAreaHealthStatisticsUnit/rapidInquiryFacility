RIF.modelAccessor = ( function () {

  var modelAccessor = Object.create( RIF.model() ); //ECMA5  

  var _mandatory = [ "studyName", "healthTheme", "numerator", "denominator", "project", "parameters.taxonomy", "parameters.healthOutcomes",
    "parameters.gender", "parameters.minYear", "parameters.maxYear", "parameters.ageGroups", "studyArea_resolution", "studyArea_areas",
    "studyArea_selectAt", "comparisonArea_resolution", "comparisonArea_areas", "comparisonArea_selectAt"
  ];

  var _optional = [ "covariates", "description" ];


  modelAccessor.prototype = {

    parameters: {
      healthOutcomes: null,
      ageGroups: null,
      minYear: null,
      maxYear: null,
      gender: null,
      covariates: null,
    },

    //SETTERS
    setStudyName: function ( s ) {
      modelAccessor.studyName = s;
    },
    setHealthTheme: function ( s ) {
      modelAccessor.healthTheme = s;
    },
    setNumerator: function ( s ) {
      modelAccessor.numerator = s;
    },
    setDenominator: function ( s ) {
      modelAccessor.denominator = s;
    },
    setProject: function ( s ) {
      modelAccessor.project = s;
    },
    setDescription: function ( s ) {
      modelAccessor.description = s;
    },
    setStudyAreaSelectAt: function ( s ) {
      modelAccessor.studyArea.studyArea_selectAt = s;
    },
    setStudyAreaResolution: function ( s ) {
      modelAccessor.studyArea.studyArea_resolution = s;
    },
    setStudyAreas: function ( s ) {
      modelAccessor.studyArea.studyArea_areas = s;
    },
    setComparisonArea: function ( s ) {
      modelAccessor.comparisonArea.comparisonArea_resolution = s.resolution;
      modelAccessor.comparisonArea.comparisonArea_areas = s.areas;
      modelAccessor.comparisonArea.comparisonArea_selectAt = s.selectAt;
    },
    setHealthConditionTaxonomy: function ( s ) {
      this.parameters.taxonomy = s;
    },
    setHealthOutcomes: function ( s ) {
      this.parameters.healthOutcomes = s;
    },
    setMinYear: function ( s ) {
      this.parameters.minYear = s;
    },
    setMaxYear: function ( s ) {
      this.parameters.maxYear = s;
    },
    setGender: function ( s ) {
      this.parameters.gender = s;
    },
    setCovariates: function ( s ) {
      this.parameters.covariates = s;
    },
    setAgeGroups: function ( s ) {
      this.parameters.ageGroups = s;
    },
    setParameter: function ( param, s ) {
      this.parameters[ param ] = s;
    },

    //UNSET
    unsetInvestigation: function ( i ) {
      delete modelAccessor.investigations[ i ];
    },
    unsetParameter: function ( i, h ) {
      delete this.parameters[ i ][ h ];
    },

    //GETTERS

    get: function ( param ) {
      return modelAccessor[ param ];
    },
    getInvestigations: function () {
      return modelAccessor.investigations;
    },
    getStudyName: function () {
      return modelAccessor.studyName;
    },
    getHealthTheme: function () {
      return modelAccessor.healthTheme;
    },
    getDescription: function () {
      return modelAccessor.description;
    },
    getProject: function () {
      return modelAccessor.project;
    },
    getNumerator: function () {
      return modelAccessor.numerator;
    },
    getDenominator: function () {
      return modelAccessor.denominator;
    },
    getStudyAreas: function () {
      return modelAccessor.studyArea.studyArea_areas;
    },
    getStudyAreaSelectAt: function () {
      return modelAccessor.studyArea.studyArea_selectAt;
    },
    getStudyAreaResolution: function () {
      return modelAccessor.studyArea.studyArea_resolution;
    },
    getStudyArea: function () {
      return modelAccessor.studyArea;
    },
    getComparisonArea: function () {
      return modelAccessor.comparisonArea;
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
    getParameters: function () {
      return this.parameters;
    },
    getMandatoryVariablesNames: function () {
      return _mandatory;
    },
    getOptionals: function () {
      return _optional;
    }

  };


  return modelAccessor.prototype;
} );