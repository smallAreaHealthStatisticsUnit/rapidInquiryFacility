RIF.chart = ( function( settings ) {

  var charts = settings.charts,

    c = charts.length,

    _p = {

      init: function() {
        return this;
      },

      updateChart: function( type, args ) {
        ( type === 'line_bivariate' ) ? _p.updateLine_bivariate( args ) :
          ( type === 'line_bivariateWithHighlight' ) ? _p.updateLine_bivariateWithClick( args ) :
          ( type === 'multipleAreaCharts' ) ? _p.updateMultipleAreaCharts( args ) :
          ( type === 'multipleAreaChartsFromMap' ) ? _p.updateAllCharts( args ) : 0;
      },

      clearChart: function( type, args ) {
        ( type === 'line_bivariate' ) ? _p.clearLineBivariate( args ) :
          ( type === 'multipleAreaCharts' ) ? _p.clearMultipleAreaCharts( args ) : 0;
      },

      _refreshLineBivariate: function() {
        _p.renderLineBivariate();
      },

      _refreshMultipleArea: function() {
        _p.renderMultipleArea();
      },

      updateLineChartWithBrushInterface: function( brushInfo ) {
        _p.updateLine_bivariateWithBrush( brushInfo );
      },

      updateLineChartWithClickInterface: function( gidAndRes ) {
        _p.updateLine_bivariateWithClick( gidAndRes );
      },

      slctLineBivariateFromMapInterface: function( gid ) {
        _p.updateLine_bivariateFromMapClick( gid );
      },

      lineBivariateHighlighterStepInterface: function( incrementDecrement ) {
        _p.lineBivariateHighlighterStep( incrementDecrement );
      },

      //conforms
      setEvents: function() {
        return this;
      },

      extendChart: function() {
        _p = RIF.extendComponent( 'chart', _p, charts );
        return _p;
      },

      getFacade: function() {
        _p.facade = RIF.getFacade( 'chart', settings.studyType, _p );
        return _p;
      }

    };


  _p.init()
    .getFacade()
    .extendChart()
    .setEvents();


  return _p.facade;
} );