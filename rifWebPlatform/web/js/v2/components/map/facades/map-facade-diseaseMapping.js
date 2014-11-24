RIF.map[ 'map-facade-diseaseMapping' ] = ( function( _p ) {


  // TO BE MODIFIED, below is the copy of data manager facade
  var facade = {
    /* Subscribed Events */

    slctMapAreaFromAreaChart: function( gid ) {
      _p.layer.slct( gid );
    },

    updateSelection: function( a ) {
      _p.layer.selection = {};
      _p.layer.style.repaint();
      _p.layer.selectAreas( a );
    },

    zoomTo: function( a ) {
      _p.getBounds( a );
    },

    uGeolevel: function( mapData ) {
      _p.removeLayer();
      _p.setDataset( "atlas_leu_f" /*mapData.dataSet*/ );
      _p.addLayer( {
        "geoLevel": mapData.geoLevel,
        "study": "diseaseMapping",
        field: mapData.field
      } );

    },

    resizeMap: function() {
      _p.map.invalidateSize( true );
    },

    uHoverField: function( a ) {
      _p.layer.joinField( a );
    },

    uMapStyle: function( a ) {
      _p.layer.uStyle( a );
    },

    editBreaks: function( a ) {
      _p.layer.getBreaks( a );
    },
    zoomToExtent: function() {
      _p.setFullExtent( _p.layer.geoLevel );
    },

    clearSelection: function() {
      _p.layer.clearSelection();
    },

    changeBasemap: function( url ) {
      _p.setBaseMap( url );
    },

    changeTransparency: function( val ) {
      _p.layer.setTransparency( val );
    },

    selectFromEnterKeyDown: function( gid ) {
      _p.layer.slctNoPropagation( gid );
    },


    changeYear: function() {

    },

    changeGender: function() {

    },

    /* Firers */
    populateMenus: function( args ) {
      this.fire( 'populateZoomIds', args );
    },

    scaleRange: function( args ) { /* scale */
      this.fire( 'scaleRangeReady', args );
    },

    selectionChanged: function( selection ) {
      this.fire( 'selectionchange', [ selection ] );
    }

  };

  return facade;


} );