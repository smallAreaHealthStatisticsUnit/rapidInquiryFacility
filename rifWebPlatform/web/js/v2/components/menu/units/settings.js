RIF.menu.settings = (function(){
	
	var parent = this,
		
		/* geolevel obj */
	    _p = {
        
			init: function(){
				this.events();
			},
			
			/* DOM elements */
			save:       $(".save-fld"),
			hoverSlct:  $('#fldSlct'),
			settings:   $(".settings"),
			colsFilter: $('#colsFilter'),
	        colsFilterName : "filterCols",
			
			avlbFieldsClbkSettings: function(){
				parent.dropDown( this, _p.hoverSlct );
				parent.fieldCheckboxes( this, _p.colsFilter, _p.colsFilterName );
			},
			
			/* events */
			events: function(){
				
				this.settings.click(function(){
					$("#settings").show();
				});	
				
				this.save.click(function(){
					var fields = parent.getCheckedValues( _p.colsFilterName );
					parent.facade.filterTablebyCols( fields );
					parent.facade.hoverFieldChange(_p.hoverSlct.val());
					$("#settings").hide();
				});
			}	
	    };
	
	_p.init();
	
	return _p;
});