RIF.chart.histogram = ( function ( geolevel ) {
  var chart = this,
    settings = {
      dataSet: '',
      field: '',
      gids: '',
      year: '',
      margin: {
        top: 10,
        right: 10,
        bottom: 20,
        left: 10
      },
      dimensions: {
        width: $( '#distHisto' ).width(),
        height: $( '#distHisto' ).height()
      }
    },
    _doHisto = function () {
      var params = [ settings.field ];
      if ( settings.year !== null ) {
        params.push( settings.year );
      };
      RIF.getHistogramData( _render, params );
    },
    _render = function ( data ) {
      _clear();
      var r = RIF.chart.histogram.d3renderer( settings, data );
    },
    _clear = function () {
      $( '#distHisto' ).empty();
    },
    _setHistoFieldName = function ( field ) {
      $( '#distHistoField' ).text( field );
    },
    _p = {
      setHistoSettings: function ( sett ) {
        settings = RIF.mix( settings, sett );
      },
      updateHisto: function ( sett ) {
        var callback = function () {
          _setHistoFieldName( sett.field );
          _render( this );
        };
        _p.setHistoSettings( sett );
        RIF.getHistogramData( callback, [ settings.dataSet, settings.field, settings.gids, settings.year ] );
      },
      drawHisto: function ( sett ) {
        _doHisto();
        //_render();
      }
    };
  return _p;
} );