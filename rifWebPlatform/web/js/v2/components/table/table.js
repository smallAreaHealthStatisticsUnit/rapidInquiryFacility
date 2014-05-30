RIF.table = (function(){
	/*
	 * Element
	 * Columns
	 * Data
	 * options	
	 */
	var _p = RIF.mix( RIF.table.renderer(), {
		
		init: function( geolevel, fields ){
			_p.setGeolevel(geolevel);

			if( typeof fields === 'object'){
				_p.setFields( fields );
				_p.initGrid();
				return;
			}
				
			_p.setUpGrid();
		},	
		
		facade: {
			
			/* Subscribed Events */
			getTabularData: function( geolevel ){
				_p.init( geolevel );
			},	
		
			resizeTable: function(){
				_p.resize();
			},
			
			filterCols: function( args ){
				/*
				 * args[0] = fields
				 * args[1] = geolevel
				 */
				var eq = RIF.arraysEqual( args[0], _p.fields);
				if ( !eq ){
					_p.init( args[1] , args[0] );
				}
			}
			
			
            /*addSelection: function(a){
			    console.log("Table selection added " + a);
            },
		
		    removeSelection: function(a){
			    console.log("Table selection removed " + a);	
            }*/
	    }	
	});
	

	return _p.facade;
	
});