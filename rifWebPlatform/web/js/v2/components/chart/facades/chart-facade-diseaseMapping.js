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

    updateLineChartWithBrush: function( brushInfo ) {
      _p.updateLineChartWithBrushInterface( brushInfo );
    },

    slctLineBivariateFromAreaChart: function( gidAndRes ) {
      _p.updateLineChartWithClickInterface( gidAndRes );
    },

    lineBivariateHighlighterStep: function( incrementDecrement ) {
      _p.lineBivariateHighlighterStepInterface( incrementDecrement );
    },

    refreshMultipleArea: function() {
      _p._refreshMultipleArea();
    },

    changeYear: function() {

    },

    changeGender: function() {

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

    selectionFromAreaChartChange: function( args ) { // gid , dataset
      /*var gid = args[ 0 ], resultSet = args[ 1 ];*/
      facade.fire( 'selectionFromAreaChartChange', {
        gid: args[ 0 ],
        resSet: args[ 1 ]
      } );
      facade.fire( "zoomToArea", args[ 0 ] );
    },

    areaChartKeyDown: function( args ) {
      facade.fire( 'areaChartKeyDown', args );
    }

  };

  return facade;

} );