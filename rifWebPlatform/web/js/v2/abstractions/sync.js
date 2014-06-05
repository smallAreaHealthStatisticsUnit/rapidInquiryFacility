RIF.sync = (function(type){
    
	var _shared = {
            
			currentGeoLvl: "",

            mapSync: function( params ){
			   	/*
				 * Area ids are in the following format: g + id , example: "g101"
				 * params[0]: ids
				 * params[1]: firer
				 */
				 var newlySlctd = params[0]; 
				 if( RIF.arraysEqual( newlySlctd, _study[type].selection ) ){
					return;
				 }; 
					
                 _study[type].selection = RIF.unique(newlySlctd);
					
				 if( params[1] === 'table' ){
					_study[type].fire('updateSelectionMap', _study[type].selection);
				 };
					
				 if( params[1] === 'map' ){
					_study[type].fire('updateSelectionTable', _study[type].selection);
				};
				console.log(_study[type].selection)
			}
			
        },
		
	    _study = {    
			
			manager: {
 			    selection : [],
                uAreaSelection: function( params ){	
					_shared.mapSync( params );
                }		
			},
			
			diseaseMapping: {},
            
			riskAnalysis: {}
		};
	
	return  RIF.extend(_shared, _study[type]);	
});