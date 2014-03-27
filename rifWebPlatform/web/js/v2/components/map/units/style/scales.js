/* params = {classification: , colorScale: , field: , intervals:, domain , colors  }  */
RIF.style.scales  = (function( params ){
	
	var type = params["classification"],
		min_value = params.min,
        max_value = params.max,
		intervals = parseInt(params["intervals"]),
		values = params["values"],
		domain = params["domain"],	
		colors = params["colors"],	
	    
		scales = {
			
			quantize: function(){
				var q = d3.scale.quantize()
					.domain([ min_value, max_value])
					.range(d3.range( intervals ));
				
				return q;
			},
			
			quantile: function(){
				var q = d3.scale.quantile()
					.domain([ min_value, max_value])
					.range(d3.range( intervals ));
				
				return q;	
			},
			
			jenks: function(){
				var jenks = ss.jenks(values, intervals);
				/* remove extreme intervals */
				var l = jenks.length;
				var j = d3.scale.threshold()
					  .domain(jenks)
					  .range(d3.range( intervals +2 ).map(function(i) {  return  i-1 }));
							
				return j;	
			},
			
			threshold: function(){
				var t = d3.scale.threshold()
					.domain( domain )//[.02, .04, .06, .08, .10]
					.range( d3.range( intervals + 1 ).map(function(i) {  return  i }) );
				
				return t;	
			},
			
			logarithmic: function(){
				var l = d3.scale.log()
					.domain([ min_value, max_value])
					.range(d3.range( intervals ).map(function(i) {  return  i }));
				
				return l;
					
			},
			standardDeviation: function(){}
	   };
	
	return scales[type]();
		
});