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
	
	refreshMultipleArea: function() {
      _p._refreshMultipleArea();
    },

    addResizableChart: function() {
      this.fire( 'addResizableChart', [] )
    },
	
	addResizableAreaCharts: function() {
      this.fire( 'addResizableAreaCharts', [] )
    },
	
	mapAreaFromAreaChartChange: function( gid ){
		var mapGid = "g" + gid;
		// method called using .call no context passed keep facade.fire not this.fire
		facade.fire( 'mapAreaFromAreaChartChange', mapGid );
		facade.fire( "zoomToArea" , gid );
	}
	

  };

  return facade;

} );