RIF.chart = (function (charts) {
    
	var c = charts.length,
	   
 	   _p = {
            
			init: function(){
                
            },

			facade: {
			    /* subscribers */
				uGeolevel: function( args ){
					_p.updatePyramid( { 
						geolevel: args.geoLevel		
					});
				} 	
            }
		};
	
	
	/* Extend _p with all charts */ 
	(function(){
		while(c--){ 
		    var r = RIF.chart[charts[c]].call(_p );
		    _p = RIF.mix(r , _p);		
		}
	}());
	
	
	return _p.facade;
});