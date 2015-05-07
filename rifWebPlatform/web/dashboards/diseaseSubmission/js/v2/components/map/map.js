RIF.Map = ( function () {

  this.initMap = function () {
    console.log( 'mapInit' )
  };

  this.setFullExtent = function ( geolevel ) {
    RIF.getFullExtent( this.zoomTo, [ geolevel ] );
  };

  this.zoomTo = function ( bounds ) {
    var bounds = ( typeof bounds === 'undefined' ) ? this : bounds;
    var ymax = bounds[ 0 ][ 'ymax' ],
      xmax = bounds[ 0 ][ 'xmax' ],
      xmin = bounds[ 0 ][ 'xmin' ],
      ymin = bounds[ 0 ][ 'ymin' ];
    var bounds = [
      [ ymax, xmax ],
      [ ymin, xmin ]
    ];
    _p.map.fitBounds( bounds, {
      maxZoom: 15
    } );
  },

  this.removeLayer = function () {
    if ( typeof this.layer === 'object' ) {
      $( 'svg.leaflet-zoom-animated g' ).remove();
      this.layer.clearLegend();
      this.map.removeLayer( this.layer.mylyr );
      this.map.invalidateSize();
    };
  };


} );