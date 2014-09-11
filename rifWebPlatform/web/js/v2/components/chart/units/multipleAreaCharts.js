RIF.chart.multipleAreaCharts = ( function() {

  var chart = this,

    data = null,

    rSet = null,

    settings = {
      element: "mAreaCharts",
      id_field: "gid",
      x_field: "x_order",
      line_field: "srr",
      line_field_color: "#8DB6CD",
      cl_field: "llsrr",
      cu_field: "ulsrr",
      margin: {
        top: 0,
        right: 10,
        bottom: 0,
        left: 20
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

    _initSVG = function() {

    },

    _render = function( update ) {
      _clear();
      RIF.chart.multipleAreaCharts.d3renderer( settings, rSet, d3.csv.parse( data ), update );
      chart.facade.addResizableAreaCharts();
    },

    _clear = function() {
      $( '#mAreaCharts' ).empty();
    },

    getRiskResultOneByOne = function( resultSets ) {
      var l = resultSets.length;
      while ( l-- ) {
        RIF.getRiskResults( callback, [ resultSets[ l ] /* studyId, invId /*year*/ ] );
      };
    },

    _p = {

      updateMultipleAreaCharts: function( resultSets ) {
        rSet = resultSets;

        var callback = function() {
          data = this;
          _render( false );
        };

        RIF.getRiskResults( callback, [ /* studyId, invId /*year*/] );
        //getRiskResultOneByOne( resultSets );

      },

      renderMultipleArea: function() {
        _render( true );
      }


    };


  return _p;

} );