RIF.modelAccessor = ( function () {


  var model = RIF.model();
  var mapper = RIF.modelMapper();

  var _mandatory = [ "studyName", "healthTheme", "numerator", "denominator", "project", "parameters.taxonomy", "parameters.healthOutcomes",
    "parameters.gender", "parameters.minYear", "parameters.maxYear", "parameters.ageGroups", "studyArea_resolution", "studyArea_areas",
    "studyArea_selectAt", "comparisonArea_resolution", "comparisonArea_areas", "comparisonArea_selectAt"
  ];

  var _optional = [ "covariates", "description" ];


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
    setStudyName: function ( s ) {
      model.studyName = s;
      mapper( modelAccessor );
    },
    setHealthTheme: function ( s ) {
      model.healthTheme = s;
    },
    setNumerator: function ( s ) {
      model.numerator = s;
    },
    setDenominator: function ( s ) {
      model.denominator = s;
    },
    setProject: function ( s ) {
      model.project = s;
    },
    setDescription: function ( s ) {
      model.description = s;
    },
    setStudyAreaSelectAt: function ( s ) {
      model.studyArea.studyArea_selectAt = s;
    },
    setStudyAreaResolution: function ( s ) {
      model.studyArea.studyArea_resolution = s;
    },
    setStudyAreas: function ( s ) {
      model.studyArea.studyArea_areas = s;
    },
    setComparisonArea: function ( s ) {
      model.comparisonArea.comparisonArea_resolution = s.resolution;
      model.comparisonArea.comparisonArea_areas = s.areas;
      model.comparisonArea.comparisonArea_selectAt = s.selectAt;
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
      delete model.investigations[ i ];
    },
    unsetParameter: function ( i, h ) {
      delete this.parameters[ i ][ h ];
    },

    //GETTERS
    getMappedObject: function () {
      return mapper.call( this );
    },

    get: function ( param ) {
      return model[ param ];
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
    getStudyAreaSelectAt: function () {
      return model.studyArea.studyArea_selectAt;
    },
    getStudyAreaResolution: function () {
      return model.studyArea.studyArea_resolution;
    },
    getStudyArea: function () {
      return model.studyArea;
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



  return modelAccessor;
} );