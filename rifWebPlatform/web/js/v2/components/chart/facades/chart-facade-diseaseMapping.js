RIF.chart[ 'chart-facade-diseaseMapping' ] = ( function( _p ) {


  var facade = {
    /* subscribers */
    changeResultSetSelection: function( resSetsChosen ) {
      _p.updateChart( 'multipleAreaCharts', resSetsChosen );
    },

    clearSelection: function() {
      _p.clearChart( 'multipleAreaCharts', [] );
      _p.clearChart( 'line_bivariate', [] );
    },

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
    
	changeYear: function(){
	
	},
	
	changeGender: function(){
	
	},
	
    addResizableChart: function() {
      this.fire( 'addResizableChart', [] )
    },

    addResizableAreaCharts: function() {
      this.fire( 'addResizableAreaCharts', [] )
    },

    areaChartBrushed: function( brushInfo ) {
      facade.fire( 'areaChartBrushed', brushInfo );
    },

    updateLineChartWithBrush: function( brushInfo ) {
      _p.updateLineChartWithBrushInterface( brushInfo );
    },

    mapAreaFromAreaChartChange: function( args ) { // gid , dataset
      var gid = args[ 0 ],
        resultSet = args[ 1 ],
        mapGid = "g" + gid;
      // method called using .call no context passed keep facade.fire not this.fire
      facade.fire( 'mapAreaFromAreaChartChange', mapGid /*[ mapGid , resultSet ]*/ );
      facade.fire( "zoomToArea", gid );
    }

  };

  return facade;

} );