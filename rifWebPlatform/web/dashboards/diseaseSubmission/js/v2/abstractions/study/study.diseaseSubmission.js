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

      parameters: {
        taxonomy: null,
        codes: null,
        ageGroups: null,
        minYear: null,
        maxYear: null,
        gender: null,
        covariates: null,
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
        this.parameters.taxonomy = s;
      },

      setHealthCodes: function( s ) {
        this.parameters.codes = s;
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

      setAgeGroups: function( s ) {
        this.parameters.ageGroups = s;
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
        return this.parameters.taxonomy;
      },

      getCodes: function() {
        return this.parameters.codes;
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
      },

      getAgeGroups: function() {
        return this.parameters.ageGroups;
      }

    }

  };

  return RIF.mix( _study[ type ], RIF.study[ 'facade-diseaseSubmission' ]() );
} );