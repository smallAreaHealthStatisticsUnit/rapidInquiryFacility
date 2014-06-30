RIF.table.renderer = (function () {
	
	var grid, 
		loader,
		request = 0,
		onDataLoaded = new Slick.Event(),
	   
	   _p = RIF.mix( RIF.table.settings(), {
			
			initGrid: function() {
			
				 request = 0; 
				 
				 var groupItemMetadataProvider = new Slick.Data.GroupItemMetadataProvider();

				 loader = new Slick.Data.RemoteModel(_p);
				 
				 _p.dataView = new Slick.Data.DataView( {groupItemMetadataProvider: groupItemMetadataProvider,inlineFilters: true } ); 
				
				 grid = new Slick.Grid(".dataLbl", _p.dataView , _p.gridCols , _p.toptions); 
				 grid.registerPlugin(groupItemMetadataProvider);
				 
				 //grid.setSelectionModel(new Slick.RowSelectionModel());
				 
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
					_p.dataView.setItems( this );
					_p.setGroups();
					_p.resize(_p.defaultSize);	
				}else {
					_p.addRows( this );
				}
				onDataLoaded.notify({from: 0, to: _p.nRows});
			},
			
			setGroups: function(){
				_p.dataView.setGrouping([
				    {
						getter: "ward01",
						formatter: function (g) {
							return "<span style='color:#99ccff;text-transform:none;font-size:11px'>" + g.value +"</span>";
						},
						aggregators: [
							//new Slick.Data.Aggregators.Sum("m0_4all")
						],
						aggregateCollapsed: true,
						lazyTotalsCalculation: true
				    }
				]);
				_p.dataView.onRowCountChanged.subscribe(function (e, args) {
					grid.updateRowCount();
					grid.render();
				});
				_p.dataView.onRowsChanged.subscribe(function (e, args) {
					grid.invalidateRows(args.rows);
					grid.render();
				});
				
				console.log(_p.dataView);//.collapseAllGroups();
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
				    data = _p.dataView.getItems();
				while(l--){
					data.push(rows[l]);
				}
				_p.dataView.setItems( data );;		
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