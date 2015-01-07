RIF.study = ( function( type ) {

  var _study = {

    diseaseSubmission: {

      investigations: [],

      // SELECTION
      studyName: null,
      healthTheme: null,
      numerator: null,
      denominator: null,
      studyArea: {
        resolution: null,
        areas: [],
        selectAt: null
      },

      comparisonArea: {
        resolution: null,
        areas: [],
        selectAt: null
      },

      healthConditions: {
        taxonomy: null,
        codes: []
      },

      parameters: {
        ageGroup: null,
        minYear: null,
        maxYear: null,
        gender: null,
        covariates: []
      },


      //SETTERS
      setStudyName: function( s ) {
        this.studyName = s;
      },

      setHealthTheme: function( s ) {
        this.healthTheme = s;
      },

      setNumerator: function( s ) {
        this.numerator = s;
      },

      setDenominator: function( s ) {
        this.denominator = s;
      },
      setStudyAreaSelectAt: function( s ) {
        this.studyArea.selectAt = s;
      },

      setStudyAreaResolution: function( s ) {
        this.studyArea.resolution = s;
      },

      setStudyAreas: function( s ) {
        this.studyArea.areas = s;
      },

      setComparisonArea: function( s ) {
        this.comparisonArea.resolution = s.resolution;
        this.comparisonArea.areas = s.areas;
        this.comparisonArea.selectAt = s.selectAt;
      },

      setHealthConditionTaxonomy: function( s ) {
        this.healthConditions.taxonomy = s;
      },

      setHealthCodes: function( s ) {
        this.healthConditions.codes = s;
      },

      setMinYear: function( s ) {
        this.parameters.minYear = s;
      },

      setMaxYear: function( s ) {
        this.parameters.maxYear = s;
      },

      setGender: function( s ) {
        this.parameters.gender = s;
      },

      setCovariates: function( s ) {
        this.parameters.covariates = s;
      },

      //GETTERS
      getStudyName: function() {
        return this.studyName;
      },

      getHealthTheme: function() {
        return this.healthTheme;
      },

      getNumerator: function() {
        return this.numerator;
      },

      getDenominator: function() {
        return this.denominator;
      },

      getStudyAreas: function() {
        return this.studyArea.areas;;
      },

      getComparisonArea: function() {
        return this.comparisonArea;
      },

      getHealthConditionTaxonomy: function() {
        return this.healthConditions.taxonomy;
      },

      getMinYear: function() {
        return this.parameters.minYear;
      },

      getMaxYear: function() {
        return this.parameters.maxYear;
      },

      getGender: function() {
        return this.parameters.gender;
      },

      getCovariates: function() {
        return this.parameters.covariates;
      }

    }

  };

  return RIF.mix( _study[ type ], RIF.study[ 'facade-diseaseSubmission' ]() );
} );