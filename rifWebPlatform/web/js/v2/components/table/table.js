RIF.table = (function(){
	/*
	 * Element
	 * Columns
	 * Data
	 * options	
	 */
	 
	var _p = RIF.mix( RIF.table.renderer(), {
		
		init:( function(){
			var a = 1;
		}),
		
		facade: {
			
			/* Subscribed Events */
			getTabularData: function( geolevel ){
				_p.getData( geolevel );
			},	
		
			resizeTable: function(){
				_p.resize();
			},
			
			filterCols: function( args ){
				/*
				 * args[0] = fields
				 * args[1] = geolevel
				 */
				_p.filterCols( args[0], args[1] );
			}
			
            /*addSelection: function(a){
			    console.log("Table selection added " + a);
            },
		
		    removeSelection: function(a){
			    console.log("Table selection removed " + a);	
            }*/
	    }	
	});
	
	
	_p.init();
	return _p.facade;
	
});