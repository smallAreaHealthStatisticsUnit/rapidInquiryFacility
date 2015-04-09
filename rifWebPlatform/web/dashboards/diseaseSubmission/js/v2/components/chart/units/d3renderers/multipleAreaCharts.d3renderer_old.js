RIF.chart.multipleAreaCharts.d3renderer = ( function ( settings, rSet, max ) {
  //Will sort it later
  var margin = settings.margin,
    width = settings.dimensions.width(),
    height = settings.dimensions.height(),
    contextHeight = 50,
    contextWidth = width * .5,
    el = settings.element;
  var svg = d3.select( "#" + el ).append( "svg" ).attr( "width", width ).attr( "height", ( height + margin.top + margin.bottom ) ).attr( "class", "areaCharts" );
  var rSetCount = rSet.length,
    chartHeight = ( height / rSetCount ) - ( margin.top + margin.bottom ) - ( Math.log( ( height * height ) ) ),
    maxDataPoint = max;
  var mousemove = function ( d ) {
    var x0 = d3.mouse( this )[ 0 ];
    console.log( x0 );
  };
  svg.append( "rect" ).attr( "class", "overlayHover" ).attr( "width", width ).attr( "height", height ).on( "mousemove", mousemove );
  return function Chart( options ) {
    this.chartData = options.data;
    this.width = width - 15;
    this.height = chartHeight;
    this.maxDataPoint = maxDataPoint;
    this.id = options.id;
    this.name = options.name;
    this.margin = margin;
    var localName = this.name;
    this.xScale = d3.scale.ordinal().domain( this.chartData.map( function ( d ) {
      return d.gid;
    } ) ).rangeBands( [ 0, this.width ] );
    this.yScale = d3.scale.linear().range( [ this.height, 0 ] ).domain( [ 0, this.maxDataPoint ] );
    var xS = this.xScale;
    var yS = this.yScale;
    this.area = d3.svg.area().interpolate( "basis" ).x( function ( d ) {
      return xS( +d.gid.toString() );
    } ).y0( function ( d ) {
      if ( d[ localName ] < 1 ) {
        return yS( d[ localName ] );
      } else {
        return yS( 1 )
      }
    } ).y1( function ( d ) {
      if ( d[ localName ] < 1 ) {
        return yS( 1 );
      } else {
        return yS( d[ localName ] )
      }
    } );
    this.area2 = d3.svg.area().interpolate( "monotone" ).x( function ( d ) {
      return xS( +d.gid.toString() );
    } ).y0( function ( d ) {
      var f = d[ localName ] - 0.1;
      if ( f < 1 ) {
        return yS( f );
      } else {
        return yS( 1 )
      }
    } ).y1( function ( d ) {
      var f = d[ localName ] - 0.1;
      if ( f < 1 ) {
        return yS( 1 );
      } else {
        return yS( f )
      }
    } );
    /*
				This isn't required - it simply creates a mask. If this wasn't here,
				when we zoom/panned, we'd see the chart go off to the left under the y-axis 
			*/
    svg.append( "defs" ).append( "clipPath" ).attr( "id", "clip-" + this.id ).append( "rect" ).attr( "width", this.width ).attr( "height", this.height );
    /*
				Assign it a class so we can assign a fill color
				And position it on the page
			*/
    this.chartContainer = svg.append( "g" ).attr( 'class', this.name.toLowerCase() ).attr( "transform", "translate(" + this.margin.left + "," + ( this.margin.top + ( this.height * this.id ) + ( 10 * this.id ) ) + ")" );
    /* We've created everything, let's actually add it to the page */
    this.chartContainer.append( "path" ).data( [ this.chartData ] ).attr( "class", "chart unadj " + this.name.toLowerCase() ).attr( "clip-path", "url(#clip-" + this.id + ")" ).attr( "d", this.area );
    this.chartContainer.append( "path" ).data( [ this.chartData ] ).attr( "class", "chart adj " + this.name.toLowerCase() ).attr( "clip-path", "url(#clip-" + this.id + ")" ).attr( "d", this.area2 );
    this.xAxisTop = d3.svg.axis().scale( this.xScale ).orient( "bottom" );
    this.xAxisBottom = d3.svg.axis().scale( this.xScale ).orient( "top" );
    this.yAxis = d3.svg.axis().scale( this.yScale ).orient( "left" ).tickValues( [ 0, 1, this.maxDataPoint ] );
    this.chartContainer.append( "g" ).attr( "class", "y axis" ).attr( "transform", "translate(0,0)" ).call( this.yAxis );
    this.chartContainer.append( "text" ).attr( "class", "country-title" ).attr( "transform", "translate(10,20)" ).text( this.name );
  };
  Chart.prototype.showOnly = function ( b ) {
    this.xScale.domain( b );
    this.chartContainer.select( "path" ).data( [ this.chartData ] ).attr( "d", this.area );
    this.chartContainer.select( ".x.axis.top" ).call( this.xAxisTop );
    this.chartContainer.select( ".x.axis.bottom" ).call( this.xAxisBottom );
  }
} );