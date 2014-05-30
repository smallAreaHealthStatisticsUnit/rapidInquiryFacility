RIF.table.events = (function ( grid, loader, parent ) {
	
	var selectedRows = [];
	var _p = {
			
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
							selectedRows = grid.getSelectedRows();
							console.log(selectedRows)
							
						})
					}(),	
					
					setClickEvnt: function(){	
						grid.onClick.subscribe(function(e, args) {
						    //console.log(parent.dataView.getItem(args.row))
						});
					}()
				}	
			}	
		
	};
	
	_p.init();
		
});