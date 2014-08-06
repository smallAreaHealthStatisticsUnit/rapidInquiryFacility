RIF.table['table-event-manager'] = (function () {
	
	var table = this,
		grid = table.renderer.getGrid(),
		loader = table.renderer.getLoader(),
		dataView = table.renderer.getdataView(),
	    selectedRows = [],
	    _p = {
			
			init: function(){
				this.setEvents();
				grid.onViewportChanged.notify();
			},
			
			setEvents: function(){
				
				return {
					setViewPortChanged: function(){
						grid.onViewportChanged.subscribe(function (e, args) {
							var vp = grid.getViewport();
							loader.ensureData(vp.top, vp.bottom);
						});
					}(),
					
					setSort: function(){
						grid.onSort.subscribe(function (e, args) {
							loader.setSort(args.sortCol.field, args.sortAsc ? 1 : -1);
							var vp = grid.getViewport();
							loader.ensureData(vp.top, vp.bottom);
						});
					}(),
					
					setDataLoaded: function(){
						loader.onDataLoaded.subscribe(function (e, args) {
							for (var i = args.from; i <= args.to; i++) {
							  grid.invalidateRow(i);
							}

							grid.updateRowCount();
							grid.render();
						});	
					}(),
					
					rowChanged: function(){
						grid.onSelectedRowsChanged.subscribe(function(e, args) {

						  if( table.stopRowChange){		
							  return;
						  };
						  						  
						  var rows = args.rows,
							  slctd = args.rows.length, 
							  rowsSlctd = [];
							  
						  while(slctd--){
							var item = dataView.getItemByIdx(rows[slctd]);
						    var id = item.id.split("_")[0];;
							rowsSlctd.push(id);	
						  };
						  table.rowClicked( rowsSlctd );
						  
						});
					}(),	
					
					setClickEvnt: function(){	
						grid.onClick.subscribe(function(e, args) {
							//console.log("click")
						});
						
					}()	
				}	
			}	
		
	};
	
	_p.init();
	return _p;	
});