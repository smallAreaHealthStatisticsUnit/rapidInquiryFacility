RIF.chart[ 'chart-facade-diseaseMapping' ] = ( function(  _p  ) {


  var facade = {
	/* subscribers */
    updateLineBivariate: function( args ) {
      _p.updateChart( 'line_bivariate', args );
    },
	
	refreshLineBivariate: function(){
	  _p._refreshLineBivariate();	
	},
	
	addResizableChart: function(){
	  this.fire('addResizableChart' , [])	
	}
	
  };

  return facade;

} );