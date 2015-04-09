RIF.chart[ 'chart-facade-manager' ] = ( function ( _p ) {
  /*
   * Facades can only communicate to main component.
   * Cannot call of directly component sub units
   */
  var facade = {
    /* subscribers */
    updatePyramid: function ( args ) {
      _p.updateChart( 'pyramid', args );
    },
    updateHistogram: function ( args ) {
      _p.updateChart( 'histogram', args );
    },
    updateCharts: function ( args ) {
      _p.updateChart( 'histogram', args );
      _p.updateChart( 'pyramid', args );
    }
  };
  return facade;
} );