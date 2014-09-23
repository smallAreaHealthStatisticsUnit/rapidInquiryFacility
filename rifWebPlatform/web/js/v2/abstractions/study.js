RIF.study = ( function( type ) {

  var _study = {

    manager: {},

    diseaseMapping: {

      // Current data being looked at
      studyId: null,
      invId: null,
      resultSets: [], // Max 4
      mapField: null,
      mapData: null, // single map
      areaCharts: [],
      lineBivariate: null,
      year: null,
      gender: null,

      map: {
        ids: [],
        geolevel: [],
        style: []
      },

      menusReady: function( currentSet ) {

        /*   study, investigation, resultSet, gender, year*/
        this.setCurrentStudy( currentSet.study );
        this.setCurrentInvestigation( currentSet.investigation );
        this.setCurrentResultSetAvailable( currentSet.resultSet );
        this.setCurrentMapField( currentSet.resultSet[ 0 ] );
        this.setCurrentlineBivariate( currentSet.resultSet[ 0 ] ); //First field in Current result set used to draw line bivariate
        this.setCurrentAreaChartSet( currentSet.resultSet );

        this.addGeolevel();
        this.drawLineBivariateChart();
        this.drawMultipleAreaCharts();

      },

      uHoverField: function( field ) {
        this.setCurrentMapField( field );
        this.fire( 'hoverFieldChangeApply', field );
      },

      //FIRERS
      addGeolevel: function() {
        var study = this.getCurrentStudy(),
          dataSet = this.getMapData(),
          field = this.getCurrentMapField();
        this.fire( 'addGeolevel', {
          geoLevel: study,
          dataset: dataSet,
          field: field
        } ); // retrieve geolevel, possibly a temp table or view with a name, i.e study_id_geom_userxx

      },

      drawLineBivariateChart: function() {
        this.fire( 'drawLineBivariateChart', this.getCurrentlineBivariate() );
      },

      drawMultipleAreaCharts: function() {
        this.fire( 'drawMultipleAreaCharts', this.getResultSetsAvailable() );
      },

      //SETTERS
      setCurrentStudy: function( study ) {
        this.studyId = study
      },

      setCurrentInvestigation: function( inv ) {
        this.invId = inv;
      },

      setCurrentMapField: function( resSet ) {
        this.mapField = resSet;
      },
      setCurrentResultSetAvailable: function( resSets ) { //MAX 4
        this.resultSets = resSets;
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

      //GETTERS
      getCurrentStudy: function() {
        return this.studyId;
      },

      getCurrentInvestigation: function() {
        return this.invId;
      },

      getResultSetsAvailable: function() {
        return this.resultSets;
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

  return _study[ type ];
} );