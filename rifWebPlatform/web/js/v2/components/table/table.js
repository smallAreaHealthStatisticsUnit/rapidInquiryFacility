RIF.table = (function( settings ){
	/*
	 * Element
	 * Columns
	 * Data
	 * options	
	 */
	var _p =  {
		
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
		
		setEvents: function(){
			//Empty for now due to how table handles events
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
	   .extendTable()
	   .setEvents();

	   
	return _p.facade;

});