/*
 *	Initialize a mapLayer and implements all methods
 *	in the module-> events object
 *  of which the component map is subscribed to.
 *
 *  @init
 *  @param: { type: [geoJSON, tileGeoJSON, tileTopoJSON] }
 *
 */
RIF.map = ( function( settings ) {

  var _p = {
    extentSet: 0,

    basemap: L.tileLayer( '', {} ),

    init: function() {
      this.map = new L.Map( "map", {
        attributionControl: false,
        layers: _p.basemap
      } );

      return this;
    },

    addLegend: function() {
      var legend = L.Control.extend( {
        options: {
          position: 'topright',
          width: "500px"
        },
        onAdd: function( map ) {
          var container = L.DomUtil.create( 'div', 'map-legend' );
          return container;
        }
      } );

      this.map.addControl( new legend() );
      return this;
    },

    addLayer: function( mysett ) {
      this.layer = RIF.map.layer.call( this, settings.layerType, mysett );
    },

    removeLayer: function() {
      if ( typeof this.layer === 'object' ) {
        $( 'svg.leaflet-zoom-animated g' ).remove();
        this.layer.clearLegend();
        this.map.removeLayer( this.layer.mylyr );
        this.map.invalidateSize();
      };
    },

    addTiled: function( lyr, geoTable ) {
      if ( !this.extentSet ) {
        this.setFullExtent( geoTable );
      };
      _p.map.addLayer( lyr );
    },

    setBaseMap: function( url ) {
      this.basemap.setUrl( url );
    },

    setTransparency: function( val ) {
      this.layer.setTransparency( val );
    },

    getZoom: function() {
      return _p.map.getZoom();
    },

    getCentroid: function( t, clbk ) {
      RIF.xhr( 'getCentroid.php?table=' + t, clbk );
    },

    setFullExtent: function( geolevel ) {
      this.extentSet = 1;
      RIF.getFullExtent( this.zoomTo, [ geolevel ] );
    },

    getBounds: function( id ) {
      var table = _p.layer.geoLevel;

      RIF.getBounds( _p.zoomTo, [ table, id ] );
    },

    zoomTo: function() { /* Json context */
      var r = this.split( ',' ),
        bounds = [
          [ parseFloat( r[ 0 ] ), parseFloat( r[ 1 ] ) ],
          [ parseFloat( r[ 2 ] ), parseFloat( r[ 3 ] ) ]
        ];

      _p.map.fitBounds( bounds );
    },

    setDataset: function( dataset ) {
      this.currentDataset = dataset;
    },

    getDataset: function() {
      return this.currentDataset;
    },

    /* events */
    setEvents: function() {
      var ev = RIF.getEvent( 'map', settings.studyType );
      ev.call( this );
    },

    //conforms
    extendMap: function() {
      return _p;
    },

    getFacade: function() {
      _p.facade = RIF.getFacade( 'map', settings.studyType, _p );
      return _p;
    }
  };


  _p.init()
    .getFacade()
    .extendMap()
    .setEvents();


  return _p.facade;
} );