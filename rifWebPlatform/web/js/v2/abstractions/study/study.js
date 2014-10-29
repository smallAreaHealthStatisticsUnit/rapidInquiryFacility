RIF.study = ( function( type ) {

  var _study = {

    manager: {},

    diseaseMapping: {

      // Current data being looked at
      studyId: null,
      invId: null,
	  resultSet: null,
	  year: null,
      gender: null,
      resultSets: [],
      resultSetsSelected: [], // Max 4
      mapField: null,
      mapData: null, // single map
      areaCharts: [],
      lineBivariate: null,
      selection: [],
      baseMap: null,
      transparency: 1,

      map: {
        ids: [],
        geolevel: [],
        style: []
      },

	  
      //SETTERS
      setCurrentStudy: function( study ) {
        this.studyId = study
      },

      setCurrentInvestigation: function( inv ) {
        this.invId = inv;
      },
      
	  setCurrentResultsSet: function( set ) {
        this.resultSet = set;
      },
	  
      setCurrentMapField: function( resSet ) {
        this.mapField = resSet;
      },
	  
      setCurrentResultSetAvailable: function( resSets ) {
        this.resultSets = resSets;
      },

      setCurrentResultSetSelected: function( resSets ) { //MAX 4
        this.resultSetsSelected = resSets;
      },

      setCurrentAreaChartSet: function( resSets ) {
        this.areaCharts = resSets;
      },

      setCurrentlineBivariate: function( field ) {
        this.lineBivariate = field;
      },

      setMapData: function( field ) {
        this.mapData = field;
      },

      setGender: function( gender ) {
        this.gender = gender;
      },

      setYear: function( yr ) {
        this.year = yr;
      },

      setSelection: function( slctn ) {
        this.selection = RIF.unique( slctn );
      },

      setBaseMap: function( url ) {
        this.baseMap = url;
      },

      setTransparency: function( val ) {
        this.transparency = val;
      },

      //GETTERS
      getCurrentStudy: function() {
        return this.studyId;
      },

      getCurrentInvestigation: function() {
        return this.invId;
      },
      
	  getCurrentResultSet: function() {
        return this.resultSet;
      },
	  
      getResultSetsAvailable: function() {
        return this.resultSets;
      },

      getResultSetsSelected: function() {
        return this.resultSetsSelected;
      },

      getCurrentMapField: function() {
        return this.mapField;
      },

      getCurrentAreaChartSet: function() {
        return this.areaCharts;
      },

      getCurrentlineBivariate: function() {
        return this.lineBivariate;
      },

      getCurrentGender: function() {
        return this.gender;
      },

      getCurrentYear: function() {
        return this.year;
      },

      getSelection: function() {
        return this.selection;
      },

      getBaseMap: function( url ) {
        return this.baseMap;
      },

      getTransparency: function( url ) {
        return this.transparency;
      },

      getMapData: function() {
        return [
          this.getCurrentStudy(),
          this.getCurrentInvestigation(),
          this.getCurrentMapField(),
          this.getCurrentGender(),
          this.getCurrentYear()
        ].join( "___" );
      }

    },

    riskAnalysis: {},

  };

  return RIF.mix( _study[ type ], RIF.study[ 'study-facade-diseaseMapping' ]() ); 
} );