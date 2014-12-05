RIF.menu[ 'facade-diseaseMapping' ] = ( function( _p ) {

  /*
   * Facades can only communicate to main component.
   * Cannot call of directly component sub units
   */

  var facade = {

    /* Subscribers */

    populateZoomIds: function( args ) {
      _p.populate( args );
    },

    getScaleRange: function( args ) {
      _p.showScaleRangeInterface( args );
    },



    /* Firers  */

    zoomToExtent: function() {
      this.fire( 'zoomToExtent', [] );
    },

    menusReady: function() {
      this.fire( 'menusReady', [] );
    },

    zoomTo: function( id ) {
      this.fire( 'zoomToArea', id );
    },

    hoverFieldChange: function( field ) {
      this.fire( 'hoverFieldChange', field );
    },

    filterTablebyCols: function( fields ) {
      this.fire( 'filterCols', [ fields, _p.getGeolevel() ] );
    },

    clearSelectionClicked: function() {
      this.fire( 'clearSelectionClicked', [] );
    },

    baseMapChanged: function( basemap ) {
      this.fire( 'baseMapChanged', [ basemap ] );
    },

    transparencyChanged: function( val ) {
      this.fire( 'transparencyChanged', val );
    },




    /* Study Related */
    setStudy: function( study ) {
      this.fire( 'studyChanged', study );
    },

    setInvestigation: function( inv ) {
      this.fire( 'investigationChanged', inv );
    },

    setResultSet: function( resSet ) {
      this.fire( 'resultSetChanged', resSet );
    },

    setResultSets: function( resSets ) {
      this.fire( 'resultSetsAvailableChanged', resSets );
    },

    areaChartSelectionChanged: function( args ) {
      this.fire( 'areaChartSelectionChanged', args );
    },

    setGender: function( gender ) {
      this.fire( 'genderChanged', gender );
    },

    setYear: function( year ) {
      this.fire( 'yearChanged', year );
    }

  };

  return facade;


} );