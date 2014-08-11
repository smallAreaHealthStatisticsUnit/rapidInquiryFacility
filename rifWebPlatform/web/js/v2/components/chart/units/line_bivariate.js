RIF.chart.line_bivariate = ( function() {

  var settings = {
        element: "rr_chart",
		id_field: "gid",
		x_field: "x_order",
        line_field: "srr",
        line_field_color: "#8DB6CD",
        cl_field: "llsrr",
        cu_field: "ulsrr",
		margin: {
	      top: 10,
		  right: 10,
		  bottom: 20,
		  left: 10
		},
		dimensions: {
	      width: $( '#rr_chart' ).width(),
		  height: $( '#rr_chart' ).height()
        }
      },
       	
      _render = function( data ) {
         _clear();
         RIF.chart.line_bivariate.d3renderer( settings, d3.csv.parse(data) );
      },
	 
	  _clear = function() {
        $( '#rr_chart' ).empty();
      },
   
      _p = {

	   updateLine_bivariate: function( sett ) {
          var callback = function() {
            //_setLineBivariateField( sett.field );
            _render( this );
          };
         //_p.setHistoSettings( sett );
          RIF.getResultSet( callback, [ /*type, studyId, invId /*year*/]);
	    }
		
	  };	


  return _p;

} );