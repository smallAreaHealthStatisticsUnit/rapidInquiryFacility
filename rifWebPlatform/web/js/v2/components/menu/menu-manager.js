RIF.menu = (function(menus){
	
	menus.push('utils');
	
	var m = menus.length,
	//Shared methods across menus
        _p = {
		
			events: function(){
				$(".modal_close").click(function(){
					$(".overlay").hide();
				});
				
				$(".dropdown dt > div").click(function() {
					$(".dropdown .palette").toggle();
				});
				
				$("#clearSelection").click(function() {
					_p.facade.clearMapTable();
				});
				
				$("#zoomExtent").click(function() {
					_p.facade.zoomToExtent();
				});
				
				$("#updateCharts").click(function() {
					_p.facade.chartUpdate();
				});
				
			}(),
			
		    
			callbacks: {
				avlbFieldsSettings: function(){
					_p.dropDown( this, _p.hoverSlct );
					_p.fieldCheckboxes( this, _p.colsFilter, _p.colsFilterName );
				},
				
				avlbFieldsChoro: function(){
					_p.dropDown( this, _p.fieldToMap );
					if( this.length === 0 ){
						_p.greyOut( _p.menu );
					}else {
						_p.removeGreyOut( _p.menu );
					}
				},
				
				avlbFieldsHistogram: function(){
					_p.dropDown( this, _p.histoSlct );
					_p.facade.updateHistogram();

				},
				
				avlbFieldsPyramid: function(){
					_p.dropDown( this, _p.pyramidSlct );
					_p.facade.updatePyramid();
				},
				
				zoomTo: function(){
					_p.dropDown( this, _p.zoomTo );
				},
				
			},
			
			populate: function( args ) {
				RIF.getFields( _p.callbacks.avlbFieldsSettings , [_p.getDataset()] );
				RIF.getNumericFields(  [_p.callbacks.avlbFieldsChoro, _p.callbacks.avlbFieldsHistogram], [_p.getDataset()] );
				RIF.getFieldsStratifiedByAgeGroup(  _p.callbacks.avlbFieldsPyramid , [  _p.getGeolevel(), _p.getDataset()] );
				RIF.getZoomIdentifiers( _p.callbacks.zoomTo , [ args.geoLvl ] );
			},
			
			facade: {
				/* Subscribers */	
				uDropdownFlds: function( args ){
					_p.populate( args );
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
					this.fire('filterCols', [fields, _p.getGeolevel() ]);
				},
				
				clearMapTable: function(){
					this.fire('clearMapTable', []);
				},
				
				changeNumRows: function( nRows ){
					//this.fire('changeNumRows', nRows);
				},
				
				updateHistogram: function(){
					_p.facade.fire( 'updateHistogram' , {
						geoLevel: _p.getGeolevel(), 
						field:  _p.getHistogramSelection(),
						dataSet: _p.getDataset()
					});
				},
				
				updatePyramid: function(){
					_p.facade.fire( 'updatePyramid' , {
						geoLevel: _p.getGeolevel(), 
						field:  _p.getPyramidSelection(),
						dataSet: _p.getDataset()
					});
				},
				
				chartUpdate: function(){
					_p.facade.fire( 'chartUpdateClick' , []);
				}
			}	
		};
	
	/* Extend _p with all menus */ 
	(function(){
		while(m--){ 
			var r = RIF.menu[menus[m]].call(_p);
			_p = RIF.mix(r , _p);		
		}
	}());
	
	return _p.facade;
	
});