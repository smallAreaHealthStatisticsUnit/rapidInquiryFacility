RIF.menu = (function(menus){
	
	menus.push('utils');
	
	var m = menus.length,
	//Shared methods across menus
        _p = {
		
			events: function(){
				
				$("#zoomExtent").click(function() {
					_p.facade.zoomToExtent();
				});
				
				$("#updateCharts").click(function() {
					_p.facade.chartUpdate();
				});
				
			}(),
			
		    
			callbacks: {
				
				studyCallback: function(){ // called once only
					
					if ( this.length > 0 ){
						_p.dropDown( this, _p.study );
						_p.getInvestigations( this[0]  );
						_p.getZoomIds( this[0]  );
					}

					_p.facade.addGeolevel( this[0] );	
				},
				
				avlbInvestigations: function(){
					_p.dropDown( this, _p.investigation );
					_p.getResultsSet( this[0]  );
				},
				
				avlbResultSet: function(){
					_p.dropDown( this, _p.resultSet );
					//Fire add geolevel
				},
				
				avlbFieldsChoro: function(){
					_p.dropDown( this, _p.fieldToMap );
					if( this.length === 0 ){
						_p.greyOut( _p.menu );
					}else {
						_p.removeGreyOut( _p.menu );
					}
				},
			
				
				zoomTo: function(){
					_p.dropDown( this, _p.zoomTo );
				},
				
			},
			
			populate: function( args ) {
				RIF.getStudies( _p.callbacks.avlbStudies , [] );
				//RIF.getNumericFields(  [_p.callbacks.avlbFieldsChoro, _p.callbacks.avlbFieldsHistogram], [_p.getDataset()] );
				//RIF.getZoomIdentifiers( _p.callbacks.zoomTo , [ args.geoLvl ] );
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
				addGeolevel: function( geolvl ){
					this.fire('addGeolevel', { "geoLevel" : geolvl});
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