RIF.table.renderer = (function () {
	
	var options = {
		enableCellNavigation: true,
		enableColumnReorder: false,
		forceFitColumns: true 
	},
	
	   grid,
	   
       _p = {
		
		cols     : [],
		
		render: function(){
			/*
			 * this = [columns, data]
 			 */
			 _p.cols = this[0];
			 grid = new Slick.Grid(".dataLbl", this[1], _p.formatCols(this[0]), options);
		},
		
		getData: function( geolevel, fields /*optional*/ ){
		    return RIF.getTabularData(this.render, [geolevel, fields] );
		},
		
		filterCols: function ( fields, geolevel ){
			var eq = RIF.arraysEqual( fields, _p.cols);
			if ( !eq ){
				_p.getData( geolevel , fields );
			}
		},
		
		formatCols: function(cols){
			var columns =  [];
			for(var i = 0; i < cols.length; i++ ){
				columns[i] = { id: cols[i], name: cols[i], field: cols[i] };
			}
			
			return columns;
		},	
		
		resize: function(){
			grid.resizeCanvas();
		}

	};
	
    return _p;
	
});