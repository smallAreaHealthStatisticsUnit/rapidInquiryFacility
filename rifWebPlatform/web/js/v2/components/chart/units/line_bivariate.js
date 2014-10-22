/*
 * On Brush web services will need to retrieve the relevant results and its confidence intervals.
 * Currently retrieving always rr_unadj, however code accomodates already future implementation 
 *
 */

RIF.chart.line_bivariate = ( function() {

  var chart = this,

    data = null,

    _updateDomainLineChart = null,
    
    _gidSelected = null,
      
    _settings = {
      element: "rr_chart",
      id_field: "gid",
      x_field: "x_order",
      risk_field: null,
      cl_field: "llsrr", // to be standardized as cl
      cu_field: "ulsrr", // to be standardized as cu
      risk_field_color: "#8DB6CD",
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

    _render = function( updateInfo ) {
      _clear();
      _updateDomainLineChart = RIF.chart.line_bivariate.d3renderer( _settings, d3.csv.parse( data ), _gidSelected );
      chart.facade.addResizableChart();
      
      if ( typeof updateInfo !== 'undefined'){
          _updateDomainLineChart.call( null, updateInfo );
      };
        
    },

    _clear = function() {
      $( '#rr_chart' ).empty();
    },
    
    _setLineField = function( fld ){
       _settings.risk_field = fld;
      // TO BE USED WHEN INTEGRATED PROPERLY WITH WEB SERVICES  
      //_settings.cl_field = RIF.resultNames[ fld ][ "cl" ]; 
      // _settings.cu_field = RIF.resultNames[ fld ][ "cu" ]; 
    },  
     
    _getResultSet = function( callback ){
       RIF.getResultSet( callback, [ _settings.risk_field /*type, studyId, invId /*year*/ ] ); // Should I pass  _settings.cl_field and _settings.cu_field ???? 
    },
        
    _isRiskFieldSame = function( newFld){
      return ( newFld === _settings.risk_field ) ? true : false;
     },
         
     _setGidSelected = function( gid ){
       _gidSelected = gid;
     },    
         
    _p = {

      renderLineBivariate: function() {
        _render();  
      },

      clearLineBivariate: function() {
        console.log( "line bivariate cleared" );
      },
    
     updateLine_bivariateWithBrush: function( brushInfo ) {
        // domain = { xDomain: domain, yDomain: YdomainBrushed, chart: localName }
        if ( _isRiskFieldSame( brushInfo.resSet) ) {
          _updateDomainLineChart.call( null, brushInfo );
        }else {
           this.updateLine_bivariate( brushInfo.resSet, brushInfo ); 
        }
          
      },   
     
      updateLine_bivariateWithClick: function( updateInfo ) { 
         _setGidSelected(updateInfo.gid) ;
         if( _isRiskFieldSame( updateInfo.resSet) ){
            _updateDomainLineChart.call( null, updateInfo );
            return;
        };
          this.updateLine_bivariate( updateInfo.resSet, updateInfo );
      },
      
      //    
      updateLine_bivariate: function( field, updateInfo, clbk ) {
         
        var callback = clbk || function() {
          data = this;
          _render( updateInfo );
        };
        
        _setLineField( field );  
       _getResultSet( callback );
          
      }    
        

    };


  return _p;

} );