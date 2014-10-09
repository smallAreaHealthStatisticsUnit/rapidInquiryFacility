RIF.menu[ 'menu-facade-diseaseMapping' ] = ( function( _p ) {

  /*
   * Facades can only communicate to main component.
   * Cannot call of directly component sub units
   */

  var facade = {

    /* Subscribers */

    resultSetSelectionChanged: function( args ) {
      this.fire( 'resultSetSelectionChanged', args );
    },

    populateZoomIds: function( args ) {
      _p.populate( args );
    },

    getScaleRange: function( args ) {
      _p.showScaleRangeInterface( args );
    },

    zoomToExtent: function() {
      this.fire( 'zoomToExtent', [] );
    },

    menusReady: function( currentSet ) {
      this.fire( 'menusReady', currentSet );
    },

    addTabularData: function( dataSets ) {
      this.fire( 'addTabularData', dataSets );
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
    }


  };

  return facade;


} );