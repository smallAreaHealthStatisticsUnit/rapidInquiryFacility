RIF.chart.multipleAreaCharts = ( function() {

  var chart = this,

    settings = {
      element: "rr_chart",
      id_field: "gid",
      x_field: "x_order",
      line_field: "srr",
      line_field_color: "#8DB6CD",
      cl_field: "llsrr",
      cu_field: "ulsrr",
      margin: {
        top: 10,
        right: 0,
        bottom: 0,
        left: 30
      },

      dimensions: {
        width: function() {
          return $( '#mAreaCharts' ).width()
        },
        height: function() {
          return $( '#mAreaCharts' ).height()
        }
      }
    },

    _render = function( update ) {
      _clear();
      RIF.chart.multipleAreaCharts.d3renderer( settings, d3.csv.parse( data ), update );
      chart.facade.multipleAreaCharts();
    },

    _clear = function() {
      $( '#mAreaCharts' ).empty();
    },

    _p = {

      updateMultipleAreaCharts: function( sett ) {

        console.log( "updateMultipleAreaCharts" )
        return; // JUST FOR NOW 14/08/14

        var callback = function() {
          _render( false );
        };
        RIF.getRiskResults( callback, [ /*type, studyId, invId /*year*/] );
      }

    };


  return _p;

} );