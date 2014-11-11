RIF.study = ( function( type ) {

  var _study = {

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

      //DOM Related    
      updateStudyLabel: function() {
        $( '#studyLabel h1' ).text( this.getCurrentStudy() );
      },

      updateInvestigationLabel: function() {
        $( '#studyLabel h2' ).text( this.getCurrentInvestigation() );
      },

      updateInvestigationInfo: function() {
        var params = [ this.getCurrentStudy(), this.getCurrentInvestigation() ],
          callback = function() {
            $( '#nAreas span' ).text( this[ 0 ] );
            $( '#covariate span' ).text( this[ 1 ] );
            $( '#ageGroup span' ).text( this[ 2 ] );
            $( '#year span' ).text( this[ 3 ] );
          };

        RIF.getInvestigationInfo( callback, params );
      },

      updateResultFigures: function( gid ) {
        var params = [ this.getCurrentStudy(),
                   this.getCurrentInvestigation(),
                   gid, this.getCurrentGender(),
                   this.getCurrentYear ],
          callback = function() {

            $( '#areaLevel span' ).text( this[ 'code' ] );
            $( '#totDenom span' ).text( this[ 'pop' ] );
            $( '#observed span' ).text( this[ 'observed' ] );
            $( '#expected span' ).text( this[ 'expected' ] );
          };

        RIF.getResultFigures( callback, params );
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

    }

  };

  return RIF.mix( _study[ type ], RIF.study[ 'study-facade-diseaseMapping' ]() );
} );