RIF.chart[ 'chart-facade-diseaseMapping' ] = ( function(  _p  ) {


  var facade = {
	/* subscribers */
    updateLineBivariate: function( args ) {
      _p.updateChart( 'line_bivariate', args );
    }
	
  };

  return facade;

} );