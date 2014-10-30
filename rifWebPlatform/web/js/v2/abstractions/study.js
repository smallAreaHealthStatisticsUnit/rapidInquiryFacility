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

      menusReady: function( currentSet ) {

        this.setCurrentMapField( this.getCurrentResultSet() );
        this.setCurrentlineBivariate( this.getCurrentResultSet() ); //First field in Current result set used to draw line bivariate

        this.addGeolevel();
        this.drawLineBivariateChart();

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

      changeAreaChartsSelection: function() {
        this.fire( 'changeAreaSelection', this.getResultSetsSelected() );
      },

      clearSelection: function() {
        this.setSelection( [] );
        this.fire( 'clearSelection', [] );
      },

      drawLineBivariateChart: function() {
        this.fire( 'drawLineBivariateChart', this.getCurrentlineBivariate() );
      },

      drawMultipleAreaCharts: function() {
        this.fire( 'drawMultipleAreaCharts', this.getResultSetsSelected() );
      },

      changeBasemap: function( url ) {
        this.setBaseMap( url );
        this.fire( 'changeBasemap', url );
      },

      changeTransparency: function( v ) {
        this.setTransparency( v );
        this.fire( 'changeTransparency', v );
      },

      changeMapStyle: function( s ) {
        this.fire( 'changeMapStyle', s );
      },

      //SuBSCRIBERS

      studyChanged: function( study ) {
        this.setCurrentStudy( study );
      },

      investigationChanged: function( investigation ) {
        this.setCurrentInvestigation( investigation );
      },

      resultSetChanged: function( resSet ) {
        this.setCurrentResultsSet( resSet );
      },

      yearChanged: function( yr ) {
        this.setYear( yr );
      },

      genderChanged: function( gender ) {
        this.setGender( gender );
      },


      uAreaSelection: function( params ) {
        this.setSelection( params[ 0 ] );
      },

      selectionFromAreaChartChange: function( args ) {
        /*args = { gid , resSet }*/
        var mapGid = "g" + args.gid;
        this.fire( 'slctMapAreaFromAreaChart', mapGid );
        this.fire( 'slctLineBivariateFromAreaChart', args );
      },

      areaChartSelectionChanged: function( resSetsChoice ) {
        if ( RIF.arraysEqual( resSetsChoice, this.getResultSetsSelected() ) ) {
          return;
        };

        this.setCurrentResultSetSelected( resSetsChoice );
        this.changeAreaChartsSelection();
      },

      areaChartKeyDown: function( incrementDecrement ) {
        this.fire( 'lineBivariateHighlighterStep', incrementDecrement );
      },

      areaChartBrushed: function( domain ) {
        this.fire( 'updateLineChartWithBrush', domain );
      },

      clearSelectionClicked: function() {
        this.clearSelection();
      },

      baseMapChanged: function( args ) {
        var url = args[ 0 ];
        if ( this.baseMap != url ) {
          this.changeBasemap( url );
        }
      },

      transparencyChanged: function( value ) {
        if ( this.transparency != value ) {
          this.changeTransparency( value );
        }
      },

      yearChanged: function( yr ) {
        this.setYear( yr );
      },

      genderChanged: function( gender ) {
        this.setGender( gender );
      },

      mapStyleChange: function( params ) {
        var fieldToMap = this.getCurrentResultSet(),
          mapStyle = RIF.extend( {
            field: fieldToMap
          }, params );
        this.changeMapStyle( mapStyle );

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

  return _study[ type ];
} );