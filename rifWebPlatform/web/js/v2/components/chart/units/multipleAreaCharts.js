RIF.chart.multipleAreaCharts = ( function() {

  var chart = this,

    minMax = null,

    data = [],

    rSet = null,

    newChart = null,

    settings = {
      element: "mAreaCharts",
      id_field: "gid",
      x_field: "x_order",
      line_field: "srr",
      line_field_color: "#8DB6CD",
      cl_field: "llsrr",
      cu_field: "ulsrr",
	  lineColor: "7D9EC0",
	  lineSelectionColor: "#EEA9B8",
      margin: {
        top: 5,
        right: 10,
        bottom: 0,
        left: 25
      },

      dimensions: {
        width: function() {
          return $( '#' + settings.element ).innerWidth()
        },
        height: function() {
          return $( '#' + settings.element ).innerHeight()
        }
      }
    },

    _findMinMaxResultSet = function( clbk ) {
      RIF.getMinMaxResultSet( clbk, [ /* studyID, invId, [year] */] );
    },

    _getNewChart = function( resultSets ) {
      
	  _clear();
	  
	  if ( typeof resultSets !== 'undefined'  ){
	      rSet = resultSets;
	  };
	  
      var callback = function() {
        minMax = this;
        _initSVG();
        _getRiskResultOneByOne( newChart );
      };

      _findMinMaxResultSet( callback );
    },

    _initSVG = function() {
      newChart = RIF.chart.multipleAreaCharts.d3renderer( settings, rSet, minMax.max, chart.facade );
    },

    _getRiskResultOneByOne = function( newChart ) {
      var requestCount = 0,
        callback = function() {
          data.push( d3.csv.parse( this ) );
          drawChart( requestCount );
          if ( ++requestCount === rSet.length ) {
            _addResizable();
          } else {
            _getRiskData( callback, requestCount );
          }
        }
      _getRiskData( callback, requestCount );
    },

    _getRiskData = function( clbk, idx ) {
      RIF.getRiskResults( clbk, [ rSet[ idx ] /* studyId, invId /*year*/ ] );
    }

  drawChart = function( idx ) {
    newChart( {
      data: data[ idx ],
      id: idx,
      name: rSet[ idx ]
    } );
  }

  _rerender = function() {
    _clear();
    _initSVG();
    var l = data.length;
    for ( var i = 0; i < l; i++ ) {
      drawChart( i );
    };
    _addResizable();
  },

  _addResizable = function() {
    chart.facade.addResizableAreaCharts();
  },

  _clear = function() {
    data = [];
	minMax = null;
    newChart = null;
	rSet = null;
    $( '#' + settings.element ).empty();
  }

  _p = {

    updateMultipleAreaCharts: function( resultSets ) {
      _getNewChart( resultSets );
    },
    
	clearMultipleAreaCharts: function(){
	  console.log(" clearMultipleAreaCharts cleared");
	},
	
    renderMultipleArea: function() {
      _rerender();
    }

  };

  return _p;

} );