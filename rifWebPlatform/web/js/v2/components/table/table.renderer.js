RIF.table.renderer = (function () {
	
	var grid, 
		loader,
		request = 0,
		onDataLoaded = new Slick.Event(),
	   
	   _p = RIF.mix( RIF.table.settings(), {
			
			initGrid: function() {
			
				 request = 0; 
				 
				 loader = new Slick.Data.RemoteModel(_p);
				 
				 _p.dataView = new Slick.Data.DataView( { inlineFilters: true } ); 
				
				 grid = new Slick.Grid(".dataLbl", _p.dataView , _p.gridCols , _p.toptions); 
				 grid.setSelectionModel(new Slick.RowSelectionModel());
				 
				 RIF.table.events(grid, loader, _p);		 
			},
			
			setUpGrid: function(){
				
				var callback = function(){ 
					_p.setFields(this); 
					_p.initGrid();
				}
				
				RIF.getTableFields( callback , [_p.geolevel] );
			},
			
			request: function( from, to ){ /*Called by slick.remotemodel.ensureData() */
			    var params = [ _p.geolevel, from, to, _p.fields];
			    RIF.getTabularData(_p.render, params);
		    },
			
			render: function(){
				// this = data 
				if( request === 0){
					request++;
					grid.setData( this );
					_p.resize(_p.defaultSize);
				}else {
					_p.addRows( this );
				}
				
				onDataLoaded.notify({from: 0, to: _p.nRows});
			},
			
			setGeolevel: function( geolevel ){
				_p.geolevel = geolevel;
			},
			
			setFields: function( fields ){
				_p.fields = fields; // array of fields
				_p.gridCols = _p.formatCols(fields);// used to render grid columns
			},
			
			addRows: function( rows ){
				var l = rows.length,
				    data = grid.getData();
				
				while(l--){
					data.push(rows[l]);
				}
				
				grid.updateRowCount();
				grid.render();		
			},
			
			setDataView: function (data) {  
				_p.dataView = new Slick.Data.DataView( { inlineFilters: true } );
				_p.dataView.setItems( data );
			},
			
			formatCols: function(gridCols){
				var columns =  [];
				for(var i = 0; i < gridCols.length; i++ ){
					columns[i] = { id: gridCols[i], name: gridCols[i], field: gridCols[i], minWidth: _p.minColumnWidth };
				}
				
				return columns;
			},	
			
			resize: function( dimensions ){
				if( typeof dimensions !== 'undefined' ){
					$("#data").css(dimensions);
				}
				
				grid.resizeCanvas();
			}
		});

	
    return _p;
	
});