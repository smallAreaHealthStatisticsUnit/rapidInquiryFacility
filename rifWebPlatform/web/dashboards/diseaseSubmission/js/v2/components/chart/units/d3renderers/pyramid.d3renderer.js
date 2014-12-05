RIF.chart.pyramid.d3renderer = ( function( opt, data ) {

  /*
   *	Population Pyramid using d3.
   *	Readaptation of https://gist.github.com/bollwyvl/6223504, with the x and y axes switched.
   *
   *  @data: agegroup, sex, count
   *
   */

  var id = opt.element,
    width = opt.dimensions.width,
    height = opt.dimensions.height,
    agegroups = opt.ageGroups,
    length = agegroups.length
  barHeight = Math.floor( opt.dimensions.height / length ) - 1,
  margin = opt.margin,
  field = opt.field;

  var y = d3.scale.linear()
    .range( [ barHeight / 2, height - barHeight / 2 ] );

  var x = d3.scale.linear()
    .range( [ 0, width ] );


  var myFormatter = function( d ) {
    return ( d / 1e6 >= 1 ) ? ( d / 1e6 + "M" ) :
      ( d / 1e3 >= 1 ) ? ( d / 1e3 + "K" ) : d;
  }

  var xAxis = d3.svg.axis()
    .scale( x )
    .orient( "bottom" )
    .tickSize( -height )
    .tickFormat( function( d ) {
      return myFormatter( +d );
    } );

  // An SVG element with a bottom right origin.
  var svg = d3.select( '#' + id ).append( "svg" )
    .attr( "width", width + margin.left + margin.right )
    .attr( "height", height + margin.top + 20 )
    .append( "g" )
    .attr( "transform", "translate(" + margin.left + "," + margin.top + ")" );


  // A sliding container to hold the bars by birthyear.
  var birthyears = svg.append( "g" )
    .attr( "class", "birthyears" );



  // Convert strings to numbers.
  data.forEach( function( d ) {
    d[ field ] = +d[ field ];
    d.sex = +d.sex;
    d.agegroup = +d.agegroup;
  } );

  var maxagegroup = d3.max( data, function( d ) {
      return d.agegroup;
    } ),
    minagegroup = d3.min( data, function( d ) {
      return d.agegroup;
    } );

  // Update the scale domains.
  y.domain( [ maxagegroup, minagegroup ] );
  x.domain( [ 0, d3.max( data, function( d ) {
    return d[ field ];
  } ) ] );

  data = d3.nest()
    .key( function( d ) {
      return d.agegroup;
    } )
    .key( function( d ) {
      return d.sex;
    } )
    .rollup( function( v ) {
      return v.map( function( d ) {
        return d[ field ];
      } );
    } )
    .map( data );

  // Add an axis to show the population values.
  svg.append( "g" )
    .attr( "class", "y axis" )
    .attr( "transform", "translate(0," + height + ")" )
    .call( xAxis )
    .selectAll( "g" )
    .filter( function( value ) {
      return !value;
    } )
    .classed( "zero", true );

  var birthyear = birthyears.selectAll( ".birthyear" )
    .data( agegroups )
    .enter().append( "g" )
    .attr( "class", "birthyear" )
    .attr( "transform", function( agegroup ) {
      return "translate(0," + y( agegroup.agegroup ) + ")";
    } );

  birthyear.selectAll( "rect" )
    .data( function( agegroup ) {
      return [ data[ agegroup.agegroup ][ 1 ][ 0 ] /*Male*/ , data[ agegroup.agegroup ][ 2 ][ 0 ] ] /*Female*/ || [ 0, 0 ];
    } )
    .enter().append( "rect" )
    .attr( "y", -barHeight / 2 )
    .attr( "height", barHeight )
    .attr( "x", 0 )
    .attr( "width", function( value ) {
      return x( value );
    } );

  svg.selectAll( ".age" )
    .data( agegroups )
    .enter().append( "text" )
    .attr( "class", "age" )
    .attr( "y", function( agegroup ) {
      return y( agegroup.agegroup ) - 7;
    } )
    .attr( "x", -20 )
    .attr( "fill", "#8F8F8F" )
    .attr( "dy", ".9em" )
    .text( function( agegroup ) {
      return agegroup.minage + '-' + agegroup.maxage;
    } );

} );