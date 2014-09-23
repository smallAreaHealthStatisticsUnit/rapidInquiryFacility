RIF.chart.multipleAreaCharts.d3renderer = ( function( settings, rSet, max, clickEvntPropagation ) {
  //Will sort it later
  var margin = settings.margin,
    width = settings.dimensions.width(),
    height = settings.dimensions.height(),
    contextHeight = 50,
    contextWidth = width * .5,
    el = settings.element,
	xOrder = settings.x_field;

  
  var areaChartsCount = 0;
  
  var dataSets = {},
      lines = {},
	  texts = {};
  
  var svg = d3.select( "#" + el ).append( "svg" )
    .attr( "width", width )
    .attr( "height", ( height + margin.top + margin.bottom ) )
    .attr( "class", "areaCharts" );


  var rSetCount = rSet.length,
    chartHeight = ( height / rSetCount ) - ( margin.top + margin.bottom ) - ( Math.log( ( height * height ) ) ),
    maxDataPoint = max;
 

 
 
 return function Chart( options ) {
    
	this.width = width ;
    this.height = chartHeight;
    this.maxDataPoint = maxDataPoint;
    this.id = options.id;
    this.name = options.name;
    this.margin = margin;
	
    var localName = this.name;
	
	dataSets[localName]  = options.data;
	
	var xS = d3.scale.linear()
	  .range( [ 0, this.width ] )
      .domain( [1,  options.data.length]); 

    var yS = d3.scale.linear()
      .range( [ this.height, 0 ] )
      .domain( [ 0, this.maxDataPoint ] );

	 
	var linename = 	this.name + "_line";


    this.area = d3.svg.area()
      //.interpolate( "monotone" )
      .x( function( d ) {
        return xS( +d[xOrder] );
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
    
	/*
     
	 This isn't required - it simply creates a mask. If this wasn't here,
	 when we zoom/panned, we'd see the chart go off to the left under the y-axis 
	
    svg.append( "defs" ).append( "clipPath" )
      .attr( "id", "clip-" + this.id )
      .append( "rect" )
      .attr( "width", this.width )
      .attr( "height", this.height );
	  
	*/

    this.chartContainer = svg.append( "g" )
      .attr( 'class', this.name.toLowerCase() )
      .attr( "transform", "translate(" + this.margin.left + "," + ( this.margin.top + ( this.height * this.id ) + ( 10 * this.id ) ) + ")" );

    
	
	var chartClass = "areaChart" +  ++areaChartsCount;
    this.chartContainer.append( "path" )
      .data( [ options.data ] )
      .attr( "class", chartClass )
      .attr( "clip-path", "url(#clip-" + this.id + ")" )
      .attr( "d", this.area );

	
	var mousemove = function( d ) {

		var xValue = Math.round(xS.invert(d3.mouse(this)[0])) ,
			gid = null;
		
		var update = function( xVal , txt, set ){
			lines[set]
			  .attr("transform","translate(" + xS(xVal) + "," + 0 + ")");
			texts[set]
			  .text( txt );
		};
			
		if( typeof dataSets[localName][xValue] !== 'undefined'){
			gid =  dataSets[localName][xValue]["gid"];// Sync with other area charts
		};		
		
		for (var set in dataSets){ 
			var dataLength = dataSets[set].length ;
			while(dataLength--){
				if(dataSets[set][dataLength]["gid"] === gid){
					var xVal =  +dataSets[set][dataLength][xOrder],
					    yVal = dataSets[set][dataLength][set];
					update(xVal, yVal, set);  
				};		
			};
		};	
    };
	
	var mouseclick = function( d ){
		var xValue = Math.round(xS.invert(d3.mouse(this)[0])) ,
			gid = null;
			
		if( typeof dataSets[localName][xValue] !== 'undefined'){
			gid =  dataSets[localName][xValue]["gid"];// Sync with other area charts
			clickEvntPropagation.call(null, [gid]);
		};
		
		
	};
	
	this.chartContainer.append( "rect" )
		.attr( "class", "overlayHover" )
		.attr( "width", width)
		.attr( "height", chartHeight )
		.on( "mousemove", mousemove )
		.on( "click", mouseclick );
	
	/* Highlighter */
	lines[localName] = this.chartContainer.append("line")
		.attr("class", "lineHover")
        .attr("x1", 0)
		.attr("y1", 0)
		.attr("x2", 0)
		.attr("y2", chartHeight)
		.attr("height", 2 )
        .attr("height", chartHeight )
		.attr("id", linename );
		

    this.yAxis = d3.svg.axis().scale( yS ).orient( "left" ).tickValues( [ 0, 1, this.maxDataPoint ] );

    this.chartContainer.append( "g" )
      .attr( "class", "y axis" )
      .attr( "transform", "translate(0,0)" )
      .call( this.yAxis );

    this.chartContainer.append( "text" )
      .attr( "class", "country-title" )
      .attr( "transform", "translate(10,20)" )
      .text( this.name  );
	
   texts[localName] = this.chartContainer.append( "text" )
      .attr( "class","areaValue")
	  .attr ( "id", localName + "_text"  )
      .attr( "transform", "translate(10,32)" )
      .text("0.00");	
	   
  };

} );