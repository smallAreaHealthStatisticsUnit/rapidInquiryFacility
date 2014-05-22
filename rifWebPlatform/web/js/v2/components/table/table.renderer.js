RIF.table.renderer = (function () {
	
	var grid,
	   
       _p = RIF.mix( RIF.table.settings(), {
		
			cols     : [],
			
			render: function(){
				/*
				 * this = [columns, data]
				 */
				 _p.setDataView( this[1] );
				 _p.cols = this[0];
				 grid = new Slick.Grid(".dataLbl", _p.dataView , _p.formatCols(this[0]), _p.toptions);
				 _p.resize(_p.defaultSize);
			},
			
			setDataView: function (data) {
				_p.dataView = new Slick.Data.DataView();
				_p.dataView.setItems( data );
			},
			
			setClickEvnt: function(){	
				grid.onClick.subscribe(function(e, args) {
				  var item = _p.dataView.getItem(args.row);
				  // Split item id with "_" to get gid to be used to higlight map area
				});
			},
			
			getData: function( geolevel, fields /*optional*/ ){
				return RIF.getTabularData(this.render, [geolevel, fields] );
			},
			
			filterCols: function ( fields, geolevel ){
				var eq = RIF.arraysEqual( fields, _p.cols);
				console.log(eq)
				if ( !eq ){
					_p.getData( geolevel , fields );
				}
			},
			
			formatCols: function(cols){
				var columns =  [];
				for(var i = 0; i < cols.length; i++ ){
					columns[i] = { id: cols[i], name: cols[i], field: cols[i], minWidth: _p.minColumnWidth };
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