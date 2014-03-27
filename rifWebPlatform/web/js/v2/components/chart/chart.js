RIF.chart = (function () {
    
	var _p = {
            
			init: function(study){
                this.study = study;
            },

			facade: {
			
			    updatePyramidWithSelection: function(a){
                    console.log("update pyramid")
                },
		
                changePyramidField: function(a){
                    console.log("Change pyramid field")
                },
		
                changePyramidYear: function(a){
                    console.log("Pyramid year changed")
                },
		
                updateHistWithSelection: function(a){
                    console.log("update hist")
                },
		
                changeHistField: function(a){
                    console.log("Change hist field")
                },
		
                changeHistYear: function(a){
                    console.log("hist year changed")
                }	
            }
		};
	
	_p.init();
	return _p.facade;
	
});