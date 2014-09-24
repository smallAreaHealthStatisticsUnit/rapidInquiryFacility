RIF.chart = ( function( settings ) {

  var charts = settings.charts,

    c = charts.length,

    _p = {

      init: function() {
        return this;
      },

      updateChart: function( type, args ) {
        ( type === 'line_bivariate' ) ? _p.updateLine_bivariate( args ) :
          ( type === 'pyramid' ) ? _p.updatePyramid( args ) :
          ( type === 'histogram' ) ? _p.updateHisto( args ) :
          ( type === 'multipleAreaCharts' ) ? _p.updateMultipleAreaCharts( args ) : 0;
      },

      _refreshLineBivariate: function() {
        _p.renderLineBivariate();
      },

      _refreshMultipleArea: function() {
        _p.renderMultipleArea();
      },

      updateDomainLineChartInterface: function( domain ) {
        _p.updateDomainLineChart( domain );
      },

      //conforms
      setEvents: function() {
        //Empty for now
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