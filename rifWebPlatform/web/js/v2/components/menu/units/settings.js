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
			
			avlbFieldsSettings: function(){
				parent.dropDown( this, _p.hoverSlct );
				parent.fieldCheckboxes( this, _p.colsFilter, _p.colsFilterName );
			},
			
			avlbFieldsHistogram: function(){
				parent.dropDown( this, _p.histoSlct );
			},
			
			avlbFieldsPyramid: function(){
				parent.dropDown( this, _p.pyramidSlct );
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
					$("#settings").hide();
				});
			}	
	    };
	
	_p.init();
	
	return _p;
});