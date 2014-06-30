RIF.chart.pyramid.d3renderer = (function( opt, data ){
	
	/*
		Population Pyramid using d3.
		Adaptation of [http://bl.ocks.org/mbostock/4062085], with the x and y axes switched.
		More info at https://gist.github.com/bollwyvl/6223504
	*/
	
		/*
		 * async file must have the following headers:
		 * GID, year, age_group, sex
		 * PLUS 
		 * a field to actually displaty in the chat ex:
		 * pop
		 * case
		 */
	
	var id = opt.element,
		width = opt.dimensions.width,
		height = opt.dimensions.height,
		agegroups = opt.ageGroups,
		barHeight = Math.floor(opt.dimensions.height / agegroups.length) - 1,
		margin = opt.margin,
		field = opt.field;	
	
	var y = d3.scale.linear()
		.range([barHeight / 2, height - barHeight / 2]);
 
	var x = d3.scale.linear()
		.range([0, width]);
 
	var xAxis = d3.svg.axis()
		.scale(x)
		.orient("bottom")
		.tickSize(-height)
		.tickFormat(function(d) { return Math.round(d / 1e6) + "M"; });
 
	// An SVG element with a bottom right origin.
	var svg = d3.select('#' + id ).append("svg")
		.attr("width", width + margin.left + margin.right)
		.attr("height", height + margin.top + margin.bottom)
	  .append("g")
		.attr("transform", "translate(" + margin.left + "," + margin.top + ")");
	
	// A sliding container to hold the bars by birthyear.
	var birthyears = svg.append("g")
		.attr("class", "birthyears");
 
	// A label for the current year.
	var title = svg.append("text")
		.attr("class", "title")
		.attr("dy", ".71em")
			.attr("x", width)
			.text(2000);
 
	//d3.csv("data/pop.csv", function(error, data) {
	  // Convert strings to numbers.
	  data.forEach(function(d) {
		d[field] = +d[field];
		d.year = +d.year;
		d.minage = +d.minage;
		d.maxage = +d.maxage;
	  });
 
	  // Compute the extent of the data set in age and years.
	  var age1 = d3.max(data, function(d) { return d.minage; }),
		  year0 = d3.min(data, function(d) { return d.year; }),
		  year1 = d3.max(data, function(d) { return d.year; }),
		  year = year1;
	
	   var minmaxage = {}, minage = [];
	   for (var key in data){
			var c =  data[key].minage ;
			if( typeof minmaxage[c] === 'undefined' ){
				minmaxage[c] =   data[key].maxage ;
				minage.push(c);
			}	
	    };
	
	  // Update the scale domains.
	  y.domain([d3.min(minage), d3.max(minage)]);
	  x.domain([0, d3.max(data, function(d) { return d[field]; })]);
	  
	  	

	  
	  // Produce a map from year and birthyear to [male, female].
	 data = d3.nest()
		  .key(function(d) { return d.minage; })
		  .key(function(d) { return d.sex ; })
		  .rollup(function(v) { return v.map(function(d) { return d[field]; }); })
		  .map(data);
 
	  // Add an axis to show the population values.
	  svg.append("g")
		  .attr("class", "y axis")
		  .attr("transform", "translate(0," + height + ")")
		  .call(xAxis)
		.selectAll("g")
		.filter(function(value) { return !value; })
		  .classed("zero", true);
 
	  /* 
	   * Add labeled rects for each birthyear (so that no enter or exit is required).
	   * THIS NEEDS TO CHANGE, onl y works for age group intervals of 5 years
	   */

	  var birthyear = birthyears.selectAll(".birthyear")
		  .data(minage)
		.enter().append("g")
		  .attr("class", "birthyear")
		  .attr("transform", function(birthyear) { return "translate(0," + y(birthyear) + ")"; });
		
	  birthyear.selectAll("rect")
		  .data(function(data) { console.log( data);/*return data[year][birthyear] || [0, 0]; */ return 0;})
		.enter().append("rect")
		  .attr("y", -barHeight / 2)
		  .attr("height", barHeight)
		  .attr("x", 0)
		  .attr("width", function(value) { return x(value); });
 
	  // Add labels to show birthyear.
	 /* birthyear.append("text")
		  .attr("x", 12)
		  .text(function(birthyear) { return birthyear; });
	 */
	  // Add labels to show age (separate; not animated).

		  
    svg.selectAll(".age")
		  .data(d3.range(0, 18, 1))
		.enter().append("text")
		  .attr("class", "age")
		  .attr("y", function(minage) { return y(minage) - 7; })
		  .attr("x", -12)
		  .attr("dy", ".9em")
		  .text(function(minage) { return minage + '-' + minmaxage[minage]; });
		  
	
	update();
	
	  // Allow the arrow keys to change the displayed year.
	  window.focus();
	  d3.select(window).on("keydown", function() {
		switch (d3.event.keyCode) {
		  case 37: year = Math.max(year0, year - 10); break;
		  case 39: year = Math.min(year1, year + 10); break;
		}
		
	  });
 
	  function update() {
		if (!(year in data)) return;
		title.text(year);
	 
		birthyears.transition()
			.duration(750)
			.attr("transform", "translate(0," + (y(year1) - y(year)) + ")");
	 
		birthyear.selectAll("rect")
			.data(function(birthyear) { return data[year][birthyear] || [0, 0]; })
		  .transition()
			.duration(750)
					.attr("x", x(0))
			.attr("width", function(value) { return x(value); });
	  }
	
	//});*/
});