RIF.menu[ 'menu-facade-diseaseMapping' ] = ( function( _p ) {

  /*
   * Facades can only communicate to main component.
   * Cannot call of directly component sub units
   */

  var facade = {

    /* Subscribers */
    uDropdownFlds: function( args ) {
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

    clearMapTable: function() {
      this.fire( 'clearMapTable', [] );
    },

    changeNumRows: function( nRows ) {
      //this.fire('changeNumRows', nRows);
    }


  };

  return facade;


} );