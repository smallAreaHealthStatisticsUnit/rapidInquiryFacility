RIF.table['table-facade-manager'] = (function ( _p ) {
	
	var facade = {
		/* Subscribed Events */
		getTabularData: function( dataset ){
			_p.init( dataset );
		},	
		
		resizeTable: function(){
			_p.resize();
		},
			
		updateSelection: function( ids ){
			_p.renderer.mapToRows(ids);
		},
			
		changeNumRows: function( nRows ){
			if( nRows !== _p.renderer.nRows ){  
				_p.init( _p.renderer.dataset, _p.renderer.fields, nRows);
			}
		},
			
		filterCols: function( args ){
	    /*
	     * args[0] = fields
	     * args[1] = geolevel
		 */
			var eq = RIF.arraysEqual( args[0], _p.renderer.fields);
			if ( !eq ){
					_p.init( _p.renderer.dataset, args[0].reverse());
					 this.fire('selectionchange', [[], 'table'] );
			}
		},
			
		clearSelection: function(){
			_p.renderer.setSelected([]);
		},
			
		/* FIRERS */	
        rowClicked: function(a){
			this.fire('selectionchange', [a, 'table'] );
        }
	
	};

		
	return facade;	 
	
	
});