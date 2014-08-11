RIF.chart.line_bivariate.d3renderer = ( function( opt, data ) {
	
	
	/*
	    Need to change X axis , probably into ordinal and set the domain as the gid array (maybe!)
		Currently only works when the data passed is ordered by gid.
	*/
	var id = opt.element,
		margin = opt.margin,
		width = opt.dimensions.width - margin.left - margin.right,
		height = opt.dimensions.height - margin.top - margin.bottom,
		idField = opt.id_field,
		orderField = opt.x_field,
		lineField = opt.line_field,
		lowField = opt.cl_field,
		highField = opt.cu_field;
    

    var line = d3.svg.line()
	.interpolate("linear")
    .x(function(d) { return x(d[orderField]); })
    .y(function(d) { return y(d[lineField]); });

    var x =  d3.scale.linear()
      .range( [ 0, width ] );

    var y = d3.scale.linear()
      .range( [ height, 0 ] );

    var xAxis = d3.svg.axis()
      .scale( x )
      .orient( "bottom" );

    var yAxis = d3.svg.axis()
      .scale( y )
      .orient( "left" );

    var area = d3.svg.area()
      .x( function( d ) {
        return x( d[orderField] );
      } )
      .y0( function( d ) {
        return y( d[lowField] );
      } )
      .y1( function( d ) {
        return y( d[highField] );
      } )
	 // .interpolate("basis");

    var svg = d3.select( "#" + id ).append( "svg" )
      .attr( "width", width + margin.left + margin.right )
      .attr( "height", height + margin.top + margin.bottom )
      .append( "g" )
      .attr( "transform", "translate(" + margin.left + "," + margin.top + ")" );

    //d3.tsv( "data.tsv", function( error, data ) {
      data.forEach( function( d ) {
	    d[idField] = +d[idField];
		d[orderField] = +d[orderField];
        d[lineField] = +d[lineField];
        d[lowField] = +d[lowField];
        d[highField] = +d[highField];
      } );

      x.domain( d3.extent( data, function( d ) {
        return  d[orderField];
      } ) );
      y.domain( [ d3.min( data, function( d ) {
        return d[lowField];
      } ), d3.max( data, function( d ) {
        return d[highField];
      } ) ] );

      svg.append( "path" )
        .datum( data )
        .attr( "class", "area" )
        .attr( "d", area );

      svg.append( "g" )
        .attr( "class", "x axis" )
        .attr( "transform", "translate(0," + height + ")" )
        .call( xAxis );

      svg.append( "g" )
        .attr( "class", "y axis" )
        .call( yAxis )
        .append( "text" )
        .attr( "transform", "rotate(-90)" )
        .attr( "y", 6 )
        .attr( "dy", ".71em" )
        .style( "text-anchor", "end" )
        .text( "RR " );
		
	  
	  svg.append("path")
      .datum(data)
      .attr("class", "line")
      .attr("d", line);
	  
	  /*svg.append('g')
		.attr("clip-path", "url(#clip)") 
		.data(data)
		.enter()
		.append("path")
		.attr("class", "line")
		.attr("d", line);
		//.attr("clip-path", "url(#clip)")
		/*.attr('stroke', function(d,i){ 			
			return "white";
		})
		/*.attr("id", function(d, i){
			return  + "g_" + d[idField]
		})*/
		
    		
	  
		
    } );