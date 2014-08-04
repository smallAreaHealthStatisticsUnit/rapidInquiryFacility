RIF.chart = (function (charts) {
    
	var c = charts.length,
	   
 	   _p = {
            
			init: function(){
                
            },

			facade: {
			    /* subscribers */
				updatePyramid: function( args ){
					_p.updatePyramid( args );
				},	
				
				updateHistogram: function( args ){
					_p.updateHisto( args );
				},

				updateCharts: function ( args ){
					this.updateHistogram( args );
					this.updatePyramid( args );
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