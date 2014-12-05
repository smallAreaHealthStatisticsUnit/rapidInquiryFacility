RIF.study = ( function( type ) {

  var _study = {

    diseaseSubmission: {

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

      healthConditions: [], // {name:ddd, icd: ooo }   
      parameters: {
        ageGroup: null,
        years: null,
        gender: null,
        covariates: null
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

      setStudyArea: function( s ) {
        this.studyArea.resolution = s.resolution;
        this.studyArea.areas = s.areas;
        this.studyArea.selectAt = s.selectAt;
      },

      setComparisonArea: function( s ) {
        this.comparisonArea.resolution = s.resolution;
        this.comparisonArea.areas = s.areas;
        this.comparisonArea.selectAt = s.selectAt;
      },

      setHealthConditions: function( s ) {
        this.healthConditions = s;
      },

      setParameters: function( s ) {
        this.ageGroup = s.ageGroup;
        this.years = s.years;
        this.gender = s.gender;
        this.covariates = s.covariates;
      },

      //GETTERS
      getStudyName: function( s ) {
        return this.studyName;
      },

      setHealthTheme: function( s ) {
        return this.healthTheme;
      },

      setNumerator: function( s ) {
        return this.numerator;
      },

      setDenominator: function( s ) {
        return this.denominator;
      },

      setStudyArea: function( s ) {
        return this.studyArea;
      },

      setComparisonArea: function( s ) {
        return this.comparisonArea;
      },

      setHealthConditions: function( s ) {
        return this.healthConditions;
      },

      setParameters: function( s ) {
        return this.parameters;
      }

    }

  };

  return RIF.mix( _study[ type ], RIF.study[ 'facade-diseaseSubmission' ]() );
} );