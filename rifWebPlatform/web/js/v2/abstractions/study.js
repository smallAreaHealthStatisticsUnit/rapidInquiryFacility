RIF.study = (function(type){
    
	var _sharedMethods = {
            
			currentGeoLvl: "",

            geolvlchange: function(a){
              //
            }
			
        },
		
	    _study = {    
			manager: {
                geoLevel: "counties",
                selectionField: "code",
 			    selection : [],
				
                uAreaSelection: function(a){
                    console.log( a );
					this.fire("removeFromSelection", a);
                },
				
			},
			
			diseaseMapping: {},
            
			riskAnalysis: {},
        
		};
	
	return  RIF.extend(_sharedMethods,_study[type]);	
});