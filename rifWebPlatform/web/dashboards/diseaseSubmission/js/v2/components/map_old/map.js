/*
 *	Initialize a mapLayer and implements all methods
 *	in the module-> events object
 *  of which the component map is subscribed to.
 *
 *  @init
 *  @param: { type: [geoJSON, tileGeoJSON, tileTopoJSON] }
 *
 */
RIF.map = ( function ( settings ) {
  var _p = {
    extentSet: 0,
    basemap: L.tileLayer( '', {} ),
    init: function () {
      this.map = new L.Map( "areaSelectionMap", {
        attributionControl: false,
        layers: _p.basemap
      } );

      _p.map.invalidateSize();

      return this;
    },
    addLegend: function () {
      /*var legend = L.Control.extend( {
        options: {
          position: 'topright',
          width: "500px"
        },
        onAdd: function ( map ) {
          var container = L.DomUtil.create( 'div', 'map-legend' );
          return container;
        }
      } );
      this.map.addControl( new legend() );
      return this;*/
    },
    addLayer: function ( mysett ) {
      this.layer = RIF.map.layer.call( this, settings.layerType, mysett );
    },
    removeLayer: function () {
      if ( typeof this.layer === 'object' ) {
        $( 'svg.leaflet-zoom-animated g' ).remove();
        this.layer.clearLegend();
        this.map.removeLayer( this.layer.mylyr );
        this.map.invalidateSize();
      };
    },
    addTiled: function ( lyr, geoTable ) {
      if ( !this.extentSet ) {
        this.setFullExtent( geoTable );
      };
      _p.map.addLayer( lyr );

    },
    setBaseMap: function ( url ) {
      this.basemap.setUrl( url );
    },
    setTransparency: function ( val ) {
      this.layer.setTransparency( val );
    },
    getZoom: function () {
      return _p.map.getZoom();
    },
    getCentroid: function ( t, clbk ) {
      RIF.xhr( 'getCentroid.php?table=' + t, clbk );
    },
    setFullExtent: function ( geolevel ) {
      this.extentSet = 0;
      RIF.getFullExtent( this.zoomTo, [ geolevel ] );
    },
    getBounds: function ( id ) {
      //var table = _p.layer.geoLevel;
      //RIF.getBounds( _p.zoomTo, [ table, id ] );
    },
    zoomTo: function () { /* Json context */
      //var bounds = this;    
      var ymax = this[ 0 ][ 'ymax' ],
        xmax = this[ 0 ][ 'xmax' ],
        xmin = this[ 0 ][ 'xmin' ],
        ymin = this[ 0 ][ 'ymin' ];

      var bounds = [
        [ ymax, xmax ],
        [ ymin, xmin ]
      ];

      _p.map.fitBounds( bounds, {
        maxZoom: 15
      } );
    },
    setDataset: function ( dataset ) {
      this.currentDataset = dataset;
    },
    getDataset: function () {
      return this.currentDataset;
    },
    /* events */
    setEvents: function () {
      var ev = RIF.map[ 'map-event-diseaseMapping' ].call( this ); //RIF.getEvent( 'map', settings.studyType );
      //ev.call( this );
    },
    //conforms
    extendMap: function () {
      return _p;
    },
    getFacade: function () {
      _p.facade = RIF.map[ 'facade-diseaseMapping' ]( _p );
      return _p;
    }
  };
  _p.init().getFacade().extendMap().setEvents();

  _p.addLayer( {
    "geoLevel": "LEVEL1",
    "study": "",
    field: ''
  } );

  return _p.facade;
} );