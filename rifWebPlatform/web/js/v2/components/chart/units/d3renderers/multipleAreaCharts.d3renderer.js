RIF.chart.multipleAreaCharts.d3renderer = ( function( settings, rSet, max, facade ) {
  //Will sort it later
  var margin = settings.margin,
    width = settings.dimensions.width(),
    height = settings.dimensions.height(),
    contextHeight = 50,
    contextWidth = width * .5,
    el = settings.element,
	xOrder = settings.x_field;

  
  var areaChartsCount = 0,
      brushIsOn = false;
  
  var dataSets = {},
      lines = {},
	  texts = {};
 
  d3.select( "#" + el ).append( "a" )
    .attr( "class", "brushButton" )
    .text( "zoom" )
	 .on("click", function(){
	    brushIsOn = !brushIsOn;
		(brushIsOn) ? showBrush( true, false ) : showBrush( false, true ) ;
	 });
  
  var showBrush = function( isBrush, isOverlay ){
	
	var brush = (isBrush) ? "block" : "none";
	var overlay = (isOverlay) ? "block" : "none";
	
	svg.selectAll("g.context")
	  .attr( "style", "display:" + brush );
		  
	svg.selectAll(".overlayHover") 
      .attr( "style", "display:" + overlay ); 
  };
  
  
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
	
    var localName = this.name,
		length = options.data.length;
	
	dataSets[localName]  = options.data;
	
	
	var xS = d3.scale.linear()
	  .range( [ 0, this.width - 40 ] )//24px is the size of Y axis 
      .domain( [1,  length]); 

    var yS = d3.scale.linear()
      .range( [ this.height, 0 ] )
      .domain( [ 0, this.maxDataPoint ] );
	
	var brushed = function () {
		var domain = brush.empty() ? xS.domain() : brush.extent(),
			domain = [ parseInt( domain[ 0 ] ), parseInt( domain[ 1 ] ) ];
		
		facade.areaChartBrushed.call(null,domain);
   }
	
	var brush = d3.svg.brush()
      .x( xS )
      .on( "brushend", brushed );
	
	var linename = 	this.name + "_line";


    this.area = d3.svg.area()
      .interpolate( "monotone" )
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
      .attr( "height", chartHeight  );
	
	
	var chartClass = "areaChart" +  ++areaChartsCount;
	
    this.chartContainer.append( "path" )
      .data( [ options.data ] )
      .attr( "class", chartClass )
      .attr( "clip-path", "url(#clip-" + this.id + ")" )
      .attr( "d", this.area );

	
	var invertX = function(mouseX){
		 var val = Math.round(xS.invert(mouseX));
		 return (val < 0 ) ? 0 :
			    (val > length ) ? length - 1 :
			     val;	 
	};
	
	var updateAllCharts = function(xValue ){
		if( typeof dataSets[localName][xValue] !== 'undefined'){
			gid =  dataSets[localName][xValue]["gid"];// Sync with other area charts
			facade.mapAreaFromAreaChartChange.call(null, [gid, localName]);// NEED to pass dataSets[localName]
			iterateToGid( gid );
		};
	};
	
	var iterateToGid = function( gid ){
		for (var set in dataSets){ 
			var dataLength = dataSets[set].length ;
			while(dataLength--){
				if(dataSets[set][dataLength]["gid"] === gid){
					var xVal =  +dataSets[set][dataLength][xOrder],
					    yVal = dataSets[set][dataLength][set];
					update(xVal, yVal, set);  
					break;
				};		
			};
		};
	};
	
	var update = function( xVal , txt, set ){
		    lines[set]
			  .attr("transform","translate(" + xS(xVal) + "," + 0 + ")");
			texts[set]
			  .text( txt );
	};
	
	var mousemove = function( d ) {	
		var xValue = invertX( (d3.mouse(this)[0])),
			gid = null;
		//console.log( d3.mouse(this)[0] + " " + invertX(d3.mouse(this)[0] ));
		if( typeof dataSets[localName][xValue] !== 'undefined'){
			gid =  dataSets[localName][xValue]["gid"];// Sync with other area charts
			iterateToGid( gid );
		};
    };
	
	var mouseclick = function( d ){
		var xValue = invertX((d3.mouse(this)[0]));
		updateAllCharts(xValue)
	};
	
	var mouseclickOnYAxis = function( d ){
		updateAllCharts(0);
	};
	
	this.chartContainer.append( "rect" )
		.attr( "class", "overlayHover" )
		.attr( "width", width + 24)
		.attr( "height", chartHeight )
		.attr( "transform", "translate(0,0)"  )
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

    this.yContainer = this.chartContainer.append( "g" )
      .attr( "class", "y axis" )
      .call( this.yAxis );
	
	this.chartContainer.append( "rect" )
	 .attr( "width", 25)
	 .attr( "class", "yClickWrapper" )
     .attr( "height", chartHeight )
	 .attr( "transform", "translate(-25,0)" )
	 .on( "click", mouseclickOnYAxis )
	  
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