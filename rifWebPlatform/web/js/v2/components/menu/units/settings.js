RIF.menu.settings = (function(){
	
	var parent = this,
		
		/* geolevel obj */
	    _p = {
        
			init: function(){
				this.events();
			},
			
			/* DOM elements */
			save:       $(".save-fld-settings"),
			hoverSlct:  $('#fldSlct'),
			histoSlct:  $('#fldHistogram'),
			pyramidSlct:$('#fldPyramid'),
			settings:   $(".settings"),
			colsFilter: $('#colsFilter'),
			numRows:    $('#numOfRows'),
	        colsFilterName : "filterCols",
		
			
			getHistogramSelection: function(){
				return _p.histoSlct.val();
			},
			
			getPyramidSelection: function(){
				return _p.pyramidSlct.val();
			},
			
			/* events */
			events: function(){
				
				this.settings.click(function(){
					$("#settings").show();
				});	
				
				this.save.click(function(){
					var fields = parent.getCheckedValues( _p.colsFilterName );
					parent.facade.filterTablebyCols( fields );
					parent.facade.changeNumRows( _p.numRows.val() );
					parent.facade.hoverFieldChange(_p.hoverSlct.val());
					parent.facade.updatePyramid();
					parent.facade.updateHistogram();
					$("#settings").hide();
				});
			}	
	    };
	
	_p.init();
	
	return _p;
});