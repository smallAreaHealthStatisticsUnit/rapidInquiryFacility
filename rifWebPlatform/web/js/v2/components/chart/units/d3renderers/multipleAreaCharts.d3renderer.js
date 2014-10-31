RIF.chart.multipleAreaCharts.d3renderer = ( function( settings, rSet, max, facade ) {
  //Will sort it later
  var margin = settings.margin,
    width = settings.dimensions.width(),
    height = settings.dimensions.height(),
    contextHeight = 50,
    contextWidth = width * .5,
    el = settings.element,
    xOrder = settings.x_field,
    lineColor = settings.lineColor,
    lineSelectionColor = settings.lineSelectionColor;


  var areaChartsCount = 0,
    brushIsOn = false,
    clickSelection = false,
    gid = null;

  var xScales = {},
    dataSets = {},
    lines = {},
    texts = {},
    xOrders = {};


  var keyDown = function() {
    var tagName = d3.select( d3.event.target ).node().tagName,
      code = d3.event.keyCode;
    if ( tagName == 'INPUT' || tagName == 'SELECT' || tagName == 'TEXTAREA' ) {
      return;
    };
    if ( code == 37 ) { // left arrow
      d3.event.preventDefault();
      moveLineByOne( decrement );
      facade.areaChartKeyDown( decrement );     
    } else if ( code == 39 ) { //right arrow
      d3.event.preventDefault();
      moveLineByOne( increment );
      facade.areaChartKeyDown( increment );     
    };

  };

  var increment = function( val ) {
    return ++val;
  };

  var decrement = function( val ) {
    return --val;
  };

  var moveLineByOne = function( augment ) {
    for ( var set in dataSets ) {
      var xVal = augment.call( null, xOrders[ set ] );
      if ( xVal < dataSets[ set ].length - 1 && xVal > 0 ) {
        var yVal = dataSets[ set ][ xVal ][ set ];
        update( xVal, yVal, set );
        continue;
      };
    };
  };

  var update = function( xVal, txt, set ) {
    xOrders[ set ] = xVal;
    lines[ set ]
      .attr( "transform", "translate(" + xScales[ set ]( xVal ) + "," + 0 + ")" );
    texts[ set ]
      .text( txt );
  };

  d3.select( "body" )
    .on( "keydown", keyDown )

  d3.select( "#" + el ).append( "a" )
    .attr( "class", "brushButton" )
    .text( "zoom" )
    .on( "click", function() {
      brushIsOn = !brushIsOn;
      ( brushIsOn ) ? showBrush( true, false ) : showBrush( false, true );
    } );

  var showBrush = function( isBrush, isOverlay ) {

    var brush = ( isBrush ) ? "block" : "none",
      overlay = ( isOverlay ) ? "block" : "none";

    svg.selectAll( "g.context" )
      .attr( "style", "display:" + brush );

    svg.selectAll( ".overlayHover" )
      .attr( "style", "display:" + overlay );
  };


  var lineColorUpdate = function( col ) {
    svg.selectAll( ".lineHover" )
      .attr( "style", "stroke:" + col );
  };

  var svg = d3.select( "#" + el ).append( "svg" )
    .attr( "width", width )
    .attr( "height", ( height + margin.top + margin.bottom ) )
    .attr( "class", "areaCharts" );

  var rSetCount = rSet.length,
    chartHeight = ( height / rSetCount ) - 20, //20 is the height of zoom a label
    maxDataPoint = max;


  return function Chart( options ) {

    this.width = width;
    this.height = chartHeight;
    this.maxDataPoint = maxDataPoint;
    this.id = options.id;
    this.name = options.name;
    this.margin = margin;

    var localName = this.name,
      length = options.data.length;

    dataSets[ localName ] = options.data;

    var xS = d3.scale.linear()
      .range( [ 0, this.width - 40 ] ) //24px is the size of Y axis 
      .domain( [ 1, length ] );

    xScales[ localName ] = xS;

    var yS = d3.scale.linear()
      .range( [ this.height, 0 ] )
      .domain( [ 0, this.maxDataPoint ] );

    
    var brushed = function() {
      var domain = brush.empty() ? xS.domain() : brush.extent(),
        domain = [ parseInt( domain[ 0 ] ), parseInt( domain[ 1 ] ) ];
      
      if ( domain[ 1 ] == dataSets[ localName ].length ){
        domain[ 1 ] = domain[ 1 ] -1;
      };
        
      var minYBrush =  parseFloat( dataSets[ localName ][domain[0]][ localName ]) - 0.5,
          maxYBrush =  parseFloat( dataSets[ localName ][domain[1]][ localName ]) + 0.5,
          YdomainBrushed = [ minYBrush, maxYBrush ];
      
      facade.areaChartBrushed.call( null, { xDomain: domain, yDomain: YdomainBrushed, resSet: localName } );
    }

    var brush = d3.svg.brush()
      .x( xS )
      .on( "brushend", brushed );

    var linename = this.name + "_line";

    this.area = d3.svg.area()
      .interpolate( "basis" )
      .x( function( d ) {
        return xS( +d[ xOrder ] );
      } )
      .y0( function( d ) {
        if ( d[ localName ] < 1 ) {
          return yS( d[ localName ] );
        } else {
          return yS( 1 )
        }

      } )
      .y1( function( d ) {
        if ( d[ localName ] < 1 ) {
          return yS( 1 );
        } else {
          return yS( d[ localName ] )
        }
      } );


    this.chartContainer = svg.append( "g" )
      .attr( 'class', this.name.toLowerCase() )
      .attr( "transform", "translate(" + this.margin.left + "," + ( this.margin.top + ( this.height * this.id ) + ( 10 * this.id ) ) + ")" );


    var context = this.chartContainer.append( "g" )
      .attr( "class", "context context" + localName )
      .attr( "transform", "translate(" + 0 + "," + 0 + ")" );


    context.append( "g" )
      .attr( "class", "x axis" )
      .attr( "transform", "translate(0," + chartHeight + ")" )
      .call( xS );

    context.append( "g" )
      .attr( "class", "x brush" )
      .call( brush )
      .selectAll( "rect" )
      .attr( "height", chartHeight );


    var chartClass = "areaChart" + ++areaChartsCount;

    this.chartContainer.append( "path" )
      .data( [ options.data ] )
      .attr( "class", chartClass )
      .attr( "clip-path", "url(#clip-" + this.id + ")" )
      .attr( "d", this.area );

    var invertX = function( mouseX ) {
      var val = Math.round( xS.invert( mouseX ) );
      return ( val < 0 ) ? 0 :
        ( val > length ) ? length - 1 :
        val;
    };

    var updateAllCharts = function( xValue ) {
      var tempClickSelection = clickSelection;
      clickSelection = !clickSelection;
      if ( tempClickSelection ) {
        lineColorUpdate( lineColor );
        return;
      };
      if ( typeof dataSets[ localName ][ xValue ] !== 'undefined' ) {
        lineColorUpdate( lineSelectionColor );
        gid = dataSets[ localName ][ xValue ][ "gid" ]; // Sync with other area charts
        facade.selectionFromAreaChartChange.call( null, [ gid, localName ] ); // NEED to pass dataSets[localName]
        iterateToGid( gid );
      };
    };

    var iterateToGid = function( _gid ) {
      for ( var set in dataSets ) {
        var dataLength = dataSets[ set ].length;
        while ( dataLength-- ) {
          if ( dataSets[ set ][ dataLength ][ "gid" ] === _gid ) {
            var xVal = +dataSets[ set ][ dataLength ][ xOrder ],
              yVal = dataSets[ set ][ dataLength ][ set ];
            update( xVal, yVal, set );
            break;
          };
        };
      };
    };

    var mousemove = function( d ) {
      if ( clickSelection ) {
        return;
      };
      var xValue = invertX( ( d3.mouse( this )[ 0 ] ) );
      if ( typeof dataSets[ localName ][ xValue ] !== 'undefined' ) {
        gid = dataSets[ localName ][ xValue ][ "gid" ]; // Sync with other area charts
        iterateToGid( gid );
      };
    };

    var mouseclick = function( d ) {
      var xValue = invertX( ( d3.mouse( this )[ 0 ] ) );
      updateAllCharts( xValue );
    };

    var mouseclickOnYAxis = function( d ) {
      updateAllCharts( 0 );
    };

    var rectInteractive = this.chartContainer.append( "rect" )
      .attr( "class", "overlayHover" )
      .attr( "width", width + 24 )
      .attr( "height", chartHeight )
      .attr( "transform", "translate(0,0)" )
      .on( "mousemove", mousemove )
      .on( "click", mouseclick )
      .on( "keydown", keyDown );


    /* Highlighter */
    lines[ localName ] = this.chartContainer.append( "line" )
      .attr( "class", "lineHover" )
      .attr( "x1", 0 )
      .attr( "y1", 0 )
      .attr( "x2", 0 )
      .attr( "y2", chartHeight )
      .attr( "height", 2 )
      .attr( "height", chartHeight )
      .attr( "id", linename );
    

    this.yAxis = d3.svg.axis().scale( yS ).orient( "left" ).tickValues( [ 0, 1, this.maxDataPoint ] );

    this.yContainer = this.chartContainer.append( "g" )
      .attr( "class", "y axis" )
      .call( this.yAxis );

    this.chartContainer.append( "rect" )
      .attr( "width", 25 )
      .attr( "class", "yClickWrapper" )
      .attr( "height", chartHeight )
      .attr( "transform", "translate(-25,0)" )
      .on( "click", mouseclickOnYAxis )

    this.chartContainer.append( "text" )
      .attr( "class", "country-title" )
      .attr( "transform", "translate(10,20)" )
      .text( this.name );

    texts[ localName ] = this.chartContainer.append( "text" )
      .attr( "class", "areaValue" )
      .attr( "id", localName + "_text" )
      .attr( "transform", "translate(10,32)" )
      .text( "0.00" );

  };

} );