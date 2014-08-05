RIF.menu['menu-facade-manager'] = (function ( _p ) {
	
	/*
	 * Facades can only communicate to main component.
	 * Cannot call of directly component sub units
	 */
	
	var facade = {
	
		/* Subscribers */	
		uDropdownFlds: function( args ){
			_p.populateManager( args );
		},
				
		getScaleRange: function(args){
			_p.showScaleRange( args );
		},
		
		zoomToExtent: function(){
			this.fire('zoomToExtent', []);
		},
				
		/* firers */
		addGeolevel: function( geolvl, dataSet ){
			RIF.dropDatatable();
			this.fire('addGeolevel', { "geoLevel" : geolvl, "dataset": dataSet});
		},
				
		addTabularData: function( dataSets ){
			this.fire('addTabularData', dataSets);
		},
				
		zoomTo: function( id ){
			this.fire('zoomToArea', id);
		},
				
		hoverFieldChange: function( field ){
			this.fire('hoverFieldChange', field);
		},
				
		filterTablebyCols: function( fields ){
			this.fire('filterCols', [fields, _p.currentGeolevel() ]);
		},
				
		clearMapTable: function(){
			this.fire('clearMapTable', []);
		},
				
		changeNumRows: function( nRows ){
			//this.fire('changeNumRows', nRows);
		},
				
		updateHistogram: function(){
			_p.facade.fire( 'updateHistogram' , _p.getHistogramSettings() );
		},
				
		updatePyramid: function(){
			_p.facade.fire( 'updatePyramid' ,  _p.getPyramidSettings() );
		},
				
		chartUpdate: function(){
			_p.facade.fire( 'chartUpdateClick' , []);
		}
	};	
		
	return facade;	 
	
});