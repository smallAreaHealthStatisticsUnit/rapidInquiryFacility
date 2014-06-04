RIF.table = (function(){
	/*
	 * Element
	 * Columns
	 * Data
	 * options	
	 */
	var _p =  {
		
		init: function( geolevel, fields ){
			
			_p.renderer = RIF.table.renderer.call(this);
			
			_p.renderer.setGeolevel(geolevel);

			if( typeof fields === 'object'){
				_p.renderer.setFields( fields );
				_p.renderer.initGrid();
				return;
			}
				
			_p.renderer.setUpGrid();
		},

		resize: function( dimensions ){
			if( typeof dimensions !== 'undefined' ){
				$("#data").css(dimensions);
			}
			_p.renderer.resize();
		},	
		
		facade: {
			
			/* Subscribed Events */
			getTabularData: function( geolevel ){
				_p.init( geolevel );
			},	
		
			resizeTable: function(){
				_p.resize();
			},
			
			updateSelection: function( ids ){
				_p.renderer.mapToRows(ids);
			},
			
			filterCols: function( args ){
				/*
				 * args[0] = fields
				 * args[1] = geolevel
				 */
				var eq = RIF.arraysEqual( args[0], _p.renderer.fields);
				if ( !eq ){
					_p.init( args[1] , args[0].reverse() );
				}
			},
			
			/* FIRERS */	
            rowClicked: function(a){
			    this.fire('selectionchange', [a, 'table'] );
            },
		
		    /*removeSelection: function(a){
			    console.log("Table selection removed " + a);	
            }*/
	    }	
	};
	

	return _p.facade;
	
});