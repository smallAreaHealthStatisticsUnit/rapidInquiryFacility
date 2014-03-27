RIF.table = (function(){

	var _p = {
		
		init:( function(study){
			
		}),
		
		facade: {
	
            addSelection: function(a){
			    console.log("Table selection added " + a);
            },
		
		    removeSelection: function(a){
			    console.log("Table selection removed " + a);	
            }
	    }	
	};
	
	
	_p.init();
	return _p.facade;
	
});