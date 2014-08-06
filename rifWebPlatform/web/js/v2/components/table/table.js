RIF.table = (function( settings ){
	/*
	 * Element
	 * Columns
	 * Data
	 * options	
	 */
	var _p =  {
		
		selectedRows: [],
		stopRowChange: false,
		
		init: function( dataset, fields , rows ){
			RIF.dropDatatable();
			
			_p.renderer = RIF.table.renderer.call(this);
			_p.renderer.setNRows( rows );
			_p.renderer.setDataSet(dataset);

			if( typeof fields === 'object'){
				_p.renderer.setFields( fields );
				_p.renderer.initGrid();
				return;
			}

			_p.renderer.setUpGrid();
			
			return this;
		},

		resize: function( dimensions ){
			if( typeof dimensions !== 'undefined' ){
				$("#data").css(dimensions);
			}
			_p.renderer.resize();
		},	
		
		rowClicked: function( rows ){
			_p.selectedRows = rows;
			_p.facade.rowClicked( rows );
		},
		
		setEvents: function(){
			//Empty for now due to how table handles events
			var ev = RIF.getEvent( 'table', settings.studyType );
			ev.call(this);
		},
		
		//conforms	
		extendTable: function(){
			//Empty for now and maybe forever
			return _p;
		},
			
		getFacade: function(){
			this.facade =  RIF.getFacade('table', settings.studyType, _p);
			return this;
		}
			
			
	};
	
	_p.getFacade()
	  .extendTable();
	   //-.setEvents() : called after initGrid event is fired from within renderer init()

	   
	return _p.facade;

});