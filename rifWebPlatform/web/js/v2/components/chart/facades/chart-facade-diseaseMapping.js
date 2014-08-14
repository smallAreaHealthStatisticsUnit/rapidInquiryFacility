RIF.chart[ 'chart-facade-diseaseMapping' ] = ( function( _p ) {


  var facade = {
    /* subscribers */

    updateMultipleAreaCharts: function( args ) {
      _p.updateChart( 'multipleAreaCharts', args );
    },

    updateLineBivariate: function( args ) {
      _p.updateChart( 'line_bivariate', args );
    },

    refreshLineBivariate: function() {
      _p._refreshLineBivariate();
    },

    addResizableChart: function() {
      this.fire( 'addResizableChart', [] )
    }

  };

  return facade;

} );