RIF.chart.line_bivariate.d3renderer = ( function( opt, data, svgElement ) {
	
	
	/*
	    Need to change X axis , probably into ordinal and set the domain as the gid array (maybe!)
		Currently only works when the data passed is ordered by gid.
	*/
	var id = opt.element,
		margin = opt.margin,
		width = opt.dimensions.width() ,
		height = opt.dimensions.height() - margin.top - margin.bottom,
		idField = opt.id_field,
		orderField = opt.x_field,
		lineField = opt.line_field,
		lowField = opt.cl_field,
		highField = opt.cu_field;
    

    var line = d3.svg.line()
	.interpolate("linear")
    .x(function(d) { return x(d[orderField]); })
    .y(function(d) { return y(d[lineField]); });

    var x = d3.scale.linear()
      .range( [ 0, width ] );
	
	var x2 = d3.scale.linear()
      .range( [ 0, width ] );
	
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
	
	var brush = d3.svg.brush()
		.x(x2)
		.on("brush", brushed);
	
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
	  .interpolate("monotone");
	
    var svg = d3.select( "#" + id ).insert( "svg", "div" )
      .attr( "width", width  )
      .attr( "height", height  );  
	  
	 var focus = svg.append("g")
	   .attr("class", "focus") 
	   .attr( "transform", "translate(" + margin.left + "," + margin.top + ")" );
	
    var context = svg.append( "g" )
		.attr("class", "context")
        .attr( "transform", "translate(" + margin.left + "," + margin.top + ")" );
	
	//Used to have a reference to the actual GIDS
    var lookUpIdsOrderId = {};
	
    data.forEach( function( d ) {
	    d[idField] = +d[idField];
		d[orderField] = +d[orderField];
        d[lineField] = +d[lineField];
        d[lowField] = +d[lowField];
        d[highField] = +d[highField];
		lookUpIdsOrderId[d[orderField]] = d[idField];
      } );
	
	var xDomain = d3.extent( data, function( d ) {
			return  d[orderField];
       });
	
      x.domain( xDomain );
	  x2.domain( xDomain );
	  
      y.domain( [ d3.min( data, function( d ) {
        return d[lowField];
      } ), d3.max( data, function( d ) {
        return d[highField];
      } ) ] );
	  
	  y2.domain( [ d3.min( data, function( d ) {
        return d[lowField];
      } ), d3.max( data, function( d ) {
        return d[highField];
      } ) ] );
	  

      focus.append( "path" )
        .datum( data )
        .attr( "class", "area" )
        .attr( "d", area );

      focus.append( "g" )
        .attr( "class", "x axis" )
        .attr( "transform", "translate(0," + height + ")" )
		.call(xAxis);

      focus.append( "g" )
        .attr( "class", "y axis" )
        .call( yAxis )
        .append( "text" )
		.attr( "x", 30 )
        .attr( "y", 2 )
		.attr( "style","text-transform:uppercase" )
        .attr( "dy", ".4em" )
        .style( "text-anchor", "end" )
		.style ("fill", "#919191")
        .text( lineField );
		
	  
	  focus.append("path")
      .datum(data)
      .attr("class", "line")
      .attr("d", line);
	  
	  
	  context.append("g")
		  .attr("class", "x axis")
		  .attr("transform", "translate(0," + height + ")")
		  .call(xAxis2);

	  context.append("g")
		  .attr("class", "x brush")
		  .call(brush)
		.selectAll("rect")
		  .attr("y", -6)
		  .attr("height", height + 7);
	  
	  
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
		
		function brushed() {
		   var domain = brush.empty() ? x2.domain() : brush.extent(),
		   domain = [ parseInt(domain[0]), parseInt(domain[1]) ];
		   console.log(domain)
		  x.domain(domain);
		  y.domain(y2.domain());
		  focus.select(".area").attr("d", area);
		  focus.select(".line").attr("d", line);
		  focus.select(".x.axis").call(xAxis);
		  /*d3.select("rect.extent")
			.attr('x', 0)
			.width('width', 0)*/
		}
    } );