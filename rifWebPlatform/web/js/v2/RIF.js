/*

 * RIF 4.0
 * Created by Federico Fabbri
 * Imperial College London
 * 
 *
 */
var RIF = (function (R) {

        R.version = "4.0";
		R.components = {} ;
		R.modules = {} ;
		
		R.resizeWidth = function(/*obj,*/px){
			
		};

		/*
			NEED TO MAKE THIS REDUDANT
		
			R.namespace = function (ns_string) {
				var parts  = ns_string.split('.'),
					parent = R,
					i;	
				if(parts[0]==="R"){
					parts = parts.slice(1);
				}	
				for(i = 0; i < parts.length; i+=1 ){
					if(typeof parent[parts[i]] === "undefined"){
						parent[parts[i]] = {} ;
					}
					parent = parent[parts[i]];	
				}
				return parent;	
			};
			
			R.ajax = function(){
				var args = Array.prototype.slice.call(arguments ,0),
					options = $.extend( {}, {
							dataType: "text",
							cache: false,
							type:"GET",
							success: function(data, textStatus, jqXHR){
								if(typeof args[1] === "function" ){
									args[1].call( this , data);
								}
							}
					},(typeof args[0] === "object" ) ? args[0] : {} );
				jQuery.ajax(options);
			}
		*/
		
		
	    return R;

}(RIF || {}));