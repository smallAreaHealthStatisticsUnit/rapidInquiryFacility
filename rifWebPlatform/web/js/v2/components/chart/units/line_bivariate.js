/*
 * On Brush web services will need to retrieve the relevant results and its confidence intervals.
 * Currently retrieving always rr_unadj, however code accomodates already future implementation 
 *
 */

RIF.chart.line_bivariate = ( function() {

  var chart = this,

    data = null,

    _updateDomainLineChart = null,

    _settings = {
      element: "rr_chart",
      id_field: "gid",
      x_field: "x_order",
      line_field: "rr_unadj",
      cl_field: "llsrr", // to be standardized as cl
      cu_field: "ulsrr", // to be standardized as cu
      line_field_color: "#8DB6CD",
      margin: {
        top: 10,
        right: 0,
        bottom: 0,
        left: 30
      },

      dimensions: {
        width: function() {
          return $( '#rr_chart' ).innerWidth()
        },
        height: function() {
          return $( '#rr_chart' ).innerHeight()
        }
      }
    },

    _render = function( update, brushInfo ) {
      _clear();
      _updateDomainLineChart = RIF.chart.line_bivariate.d3renderer( _settings, d3.csv.parse( data ), update );
      chart.facade.addResizableChart();
      
      if ( typeof brushInfo !== 'undefined'){
         _p.updateLineChartWithBrush( brushInfo );
      };
        
    },

    _clear = function() {
      $( '#rr_chart' ).empty();
    },
    
    _setLineField = function( fld ){
       _settings.line_field = fld;
      // TO BE USED WHEN INTEGRATED PROPERLY WITH WEB SERVICES  
      //_settings.cl_field = RIF.resultNames[ fld ][ "cl" ]; 
      // _settings.cu_field = RIF.resultNames[ fld ][ "cu" ]; 
    }  
      
    _p = {

      renderLineBivariate: function() {
        _render( true );  
      },

      updateLineChartWithBrush: function( brushInfo ) {
        // domain = { xDomain: domain, yDomain: YdomainBrushed, chart: localName }
        if ( brushInfo.chart == _settings.line_field) {
          _updateDomainLineChart.call( null, brushInfo );
        }else {
           _setLineField( brushInfo.chart );
           this.updateLine_bivariate( brushInfo.chart, brushInfo ); 
        }
          
      },

      clearLineBivariate: function() {
        console.log( "line bivariate cleared" );
      },

      updateLine_bivariate: function( field, brushInfo ) {
        var callback = function() {
          _setLineField( field );
          data = this;
          _render( false, brushInfo );
        };
        
        RIF.getResultSet( callback, [ _settings.line_field /*type, studyId, invId /*year*/ ] ); // Should I pass  _settings.cl_field and _settings.cu_field ???? 
      }

    };


  return _p;

} );