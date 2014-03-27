RIF.menu.settings = (function(){
	
	var parent = this,
	    
		/* firers */
	    
		
		/* geolevel obj */
	    _p = {
        
			init: function(){
				this.events();
			},
			
			/* DOM elements */
			
			save: $(".save-fld"),
			hoverSlct: $('#fldSlct'),
			settings: $(".settings"),
	        
			avlbFieldsClbkSettings: function(){
				parent.dropDown( this, _p.hoverSlct );
			},
			
			/* events */
			events: function(){
				
				this.settings.click(function(){
					$("#settings").show();
				});	
				
				this.save.click(function(){
					parent.facade.hoverFieldChange(_p.hoverSlct.val());
					$("#settings").hide();
				});
			}	
	    };
	
	_p.init();
	
	return _p;
});