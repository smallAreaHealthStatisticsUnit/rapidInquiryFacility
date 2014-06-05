RIF.table.events = (function ( grid, loader, rowClicked, dataView ) {
	
	var selectedRows = [];
	var _p = {
	
			stopRowChange: false,
			
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
						  
						  if(_p.stopRowChange){
							  return;
						  };
						  
						  var rows = args.rows,
							  slctd = args.rows.length, rowsslctd = [];
							  
						  while(slctd--){
						    var id = (dataView.getItemByIdx(rows[slctd])).id;
							id = id.split("_")[0];
							rowsslctd.push(id);	
						  };
						  
						  rowClicked.call(null, rowsslctd);
						  
						});
					}(),	
					
					setClickEvnt: function(){	
						grid.onClick.subscribe(function(e, args) {
							console.log("click")
						});
						
					}()	
				}	
			}	
		
	};
	
	_p.init();
	return _p;	
});