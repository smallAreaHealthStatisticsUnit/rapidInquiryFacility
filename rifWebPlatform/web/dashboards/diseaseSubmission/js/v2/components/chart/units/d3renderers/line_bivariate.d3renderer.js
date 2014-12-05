RIF.chart.line_bivariate.d3renderer = ( function( opt, data, gidSelected ) {

  /*
	    Need to change X axis , probably into ordinal and set the domain as the gid array (maybe!)
		Currently only works when the data passed is ordered by gid.
	*/
  var id = opt.element,
    margin = opt.margin,
    width = opt.dimensions.width(),
    xWidth = width - margin.left - margin.right - 4,
    height = opt.dimensions.height() - margin.top - margin.bottom,
    idField = $.trim( opt.id_field ),
    orderField = $.trim( opt.x_field ),
    lineField = $.trim( opt.risk_field ),
    lowField = $.trim( opt.cl_field ),
    highField = $.trim( opt.cu_field ),
    dataLength = data.length;


  var line = d3.svg.line()
    .interpolate( "basis" )
    .x( function( d ) {
      return x( d[ orderField ] );
    } )
    .y( function( d ) {
      return y( d[ lineField ] );
    } );

  var x = d3.scale.linear()
    .range( [ 0, xWidth ] );

  var x2 = d3.scale.linear()
    .range( [ 0, xWidth ] );

  var y = d3.scale.linear()
    .range( [ height, 0 ] );

  var y2 = d3.scale.linear()
    .range( [ height, 0 ] );

  var xAxis = d3.svg.axis()
    .scale( x )
    .orient( "bottom" );

  var yAxis = d3.svg.axis()
    .scale( y )
    .orient( "left" );

  var xAxis2 = d3.svg.axis()
    .scale( x2 )
    .orient( "bottom" );

  var yAxis2 = d3.svg.axis()
    .scale( y2 )
    .orient( "left" );


  var area = d3.svg.area()
    .x( function( d ) {
      return x( d[ orderField ] );
    } )
    .y0( function( d ) {
      return y( d[ lowField ] );
    } )
    .y1( function( d ) {
      return y( d[ highField ] );
    } )
    //.interpolate("monotone");

  var svg = d3.select( "#" + id ).insert( "svg", "div" )
    .attr( "width", width )
    .attr( "height", height )
    .attr( "id", "lineBivariatesvg" );

  svg.append( "defs" ).append( "clipPath" )
    .attr( "id", "clipchart" )
    .append( "rect" )
    .attr( "width", width )
    .attr( "height", height );

  var focus = svg.append( "g" )
    .attr( "class", "focus" )
    .attr( "transform", "translate(" + margin.left + "," + margin.top + ")" );


  //Used to have a reference to the actual GIDS
  var lookUp = {},
    lookUpOrder = {};

  data.forEach( function( d ) {
    d[ idField ] = +d[ idField ];
    d[ orderField ] = +d[ orderField ];
    d[ lineField ] = +d[ lineField ];
    d[ lowField ] = +d[ lowField ];
    d[ highField ] = +d[ highField ];
    lookUp[ d[ idField ] ] = [ d[ orderField ], d[ lineField ], d[ lowField ], d[ highField ] ];
    lookUpOrder[ d[ orderField ] ] = d[ idField ];
  } );

  var xDomain = d3.extent( data, function( d ) {
    return d[ orderField ];
  } );

  x.domain( xDomain );
  x2.domain( xDomain );

  y.domain( [ d3.min( data, function( d ) {
    return d[ lowField ] - 0.2;
  } ), d3.max( data, function( d ) {
    return d[ highField ] + 0.2;
  } ) ] );

  y2.domain( [ d3.min( data, function( d ) {
    return d[ lowField ] - 0.2;
  } ), d3.max( data, function( d ) {
    return d[ highField ] + 0.2;
  } ) ] );


  focus.append( "path" )
    .datum( data )
    .attr( "class", "area" )
    .attr( "d", area )
    .attr( "clip-path", "url(#clipchart)" );

  focus.append( "g" )
    .attr( "class", "x axis" )
    .attr( "transform", "translate(0," + height + ")" )
    .call( xAxis );

  focus.append( "g" )
    .attr( "class", "y axis" )
    .call( yAxis )

  svg.append( "text" )
    .attr( "transform", "translate(36,20)" )
    .attr( "id", "labelLineBivariate" )
    .text( lineField );

  var currentFigures = svg.append( "text" )
    .attr( "transform", "translate(36,40)" )
    .attr( "id", "currentFiguresLineBivariate" )
    .text( "" );

  focus.append( "path" )
    .datum( data )
    .attr( "class", "line" )
    .attr( "id", "lineRisk" )
    .attr( "clip-path", "url(#clipchart)" )
    .attr( "d", line );


  /* Highlighter */
  var highlighter = focus.append( "line" )
    .attr( "x1", 0 )
    .attr( "y1", 0 )
    .attr( "x2", 0 )
    .attr( "y2", height )
    .attr( "height", height )
    .attr( "class", "bivariateHiglighter" );

  var pointerLighter = focus.append( "circle" )
    .attr( "r", 3 )
    .attr( "class", "bivariateHiglighter" );

  this.gid = gidSelected || null;

  var setGid = function( gid ) {
    this.gid = gid;
  };

  var updateLine = function() {
    var gid = this.gid;
    if ( gid !== null ) {
      highlighter.attr( "transform", "translate(" + x( lookUp[ gid ][ 0 ] ) + "," + 0 + ")" );
      pointerLighter.attr( "transform", "translate(" + x( lookUp[ gid ][ 0 ] ) + "," + y( lookUp[ gid ][ 1 ] ) + ")" );
    };
    currentFigures
      .text( ( lookUp[ gid ][ 1 ] ).toFixed( 3 ) + " ( " + lookUp[ gid ][ 2 ] + " - " + lookUp[ gid ][ 3 ] + " )" );

  };



  return function brushed( updateInfo ) {
    /* updateInfo can be either { xDomain, yDomain } on BRUSH 
     * OR
     * { gid, resSet } on CLICK
     *  When click need to lookup gid to xOrder
     */

    //Arrow keys from area chart event
    if ( typeof updateInfo === 'function' ) { // updateInfo = incrementDecrement function()
      var order = lookUp[ gid ][ 0 ] || 0,
        newOrder = updateInfo.call( null, lookUp[ gid ][ 0 ] );
      if ( newOrder >= 0 && newOrder < dataLength ) {
        setGid( lookUpOrder[ newOrder ] );
        updateLine();
      };
      return;
    };

    // Click on area charts event and Map clicks
    if ( typeof updateInfo.gid !== 'undefined' ) {
      setGid( updateInfo.gid );
      updateLine();
      return;
    };

    //Zoom, on Brush event
    x.domain( updateInfo.xDomain );
    y.domain( updateInfo.yDomain );
    focus.select( ".area" ).attr( "d", area );
    focus.select( "#lineRisk" ).attr( "d", line );
    focus.select( ".x.axis" ).call( xAxis );
    focus.select( ".y.axis" ).call( yAxis );
    updateLine();
  }
} );