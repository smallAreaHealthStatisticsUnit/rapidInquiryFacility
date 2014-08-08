RIF.chart.histogram.d3renderer = ( function( opt, values ) {

  /*
   *	Bates distribution Histogram using d3.
   *	Readaptation of http://bl.ocks.org/mbostock/3048450
   *
   */

  var id = opt.element,
    margin = opt.margin,
    width = opt.dimensions.width - margin.left - margin.right,
    height = opt.dimensions.height - margin.top - margin.bottom,
    field = opt.field,
    bins = Math.floor( width / 30 );

  values = values.map( function( d ) {
    if ( +d >= 0 ) {
      return +d;
    }
  } );

  var max = d3.max( values ),
    min = d3.min( values );


  max = ( max > 0 ) ? max : bins;
  min = ( min > 0 ) ? min : 0;
  bins = ( max > bins ) ? bins : 15;
  // A formatter for counts.
  var formatCount = d3.format( ".0f" ),
    myFormatter = function( d ) {
      return ( d / 1e6 >= 1 ) ? ( d / 1e6 + "M" ) :
        ( d / 1e3 >= 1 ) ? ( d / 1e3 + "K" ) : d;
    }

  var x = d3.scale.linear()
    .domain( [ 0, max ] )
    .range( [ 0, width ] );

  // Generate a histogram using twenty uniformly-spaced bins.
  //Could allows users to change this
  var data = d3.layout.histogram()
    .bins( x.ticks( bins ) )
    ( values );


  var y = d3.scale.linear()
    .domain( [ 0, d3.max( data, function( d ) {
      return d.y;
    } ) ] )
    .range( [ height, 0 ] );

  var xAxis = d3.svg.axis()
    .scale( x )
    .orient( "bottom" )
    .ticks( data.length )
    .tickFormat( function( d ) {
      return myFormatter( d );
    } );

  var svg = d3.select( "#distHisto" ).append( "svg" )
    .attr( "width", width + margin.left + margin.right )
    .attr( "height", height + margin.top + margin.bottom )
    .append( "g" )
    .attr( "transform", "translate(" + margin.left + "," + margin.top + ")" );


  var bar = svg.selectAll( ".bar" )
    .data( data )
    .enter().append( "g" )
    .attr( "class", "bar" )
    .attr( "transform", function( d ) {
      return "translate(" + x( d.x ) + "," + y( d.y ) + ")";
    } );

  bar.append( "rect" )
    .attr( "x", 1 )
    .attr( "width", x( data[ 0 ].dx ) - 1 )
    .attr( "height", function( d ) {
      return height - y( d.y );
    } );

  bar.append( "text" )
    .attr( "dy", ".70em" )
    .attr( "y", -10 )
    .attr( "x", x( data[ 0 ].dx ) / 2 )
    .attr( "text-anchor", "middle" )
    .text( function( d ) {
      return formatCount( d.y );
    } );

  svg.append( "g" )
    .attr( "class", "x axis" )
    .attr( "transform", "translate(0," + height + ")" )
    .call( xAxis );


} );