RIF.study = (function(type){
    
	var _sharedMethods = {
            
			currentGeoLvl: "",

            geolvlchange: function(a){
              //
            }
			
        },
		
	    _study = {    
			
			manager: {
                geoLevel: "",
                selectionField: "",
 			    selection : [],
				
                uAreaSelection: function( params ){
					/*
					 * Area ids are in the following format: g + id , example: "g101"
					 * params[0]: ids
					 * params[1]: firer
					 */
					var newlySlctd = params[0]; 
					if( RIF.arraysEqual( newlySlctd, this.selection ) ){
						return;
					} 
					
                    this.selection = RIF.unique(newlySlctd);
					
					if( params[1] === 'table' ){
						this.fire('updateSelectionMap', this.selection);
					}
					
					if( params[1] === 'map' ){
						this.fire('updateSelectionTable', this.selection);
					}

                },
				
			},
			
			diseaseMapping: {},
            
			riskAnalysis: {},
        
		};
	
	return  RIF.extend(_sharedMethods,_study[type]);	
});