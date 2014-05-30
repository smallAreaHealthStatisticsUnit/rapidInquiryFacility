(function(){
	
	var c = function( myFunc, errorMessage ){
			return function(  error, json ){
			   if( error !== null ){
			       statusBar("error" , true);
				   return;
			   }
			   try {
					var data = jQuery.parseJSON(json.responseText);
					callback(myFunc, data);
			   } catch(e) {
				    callback(myFunc, json.responseText);
			   }
				
			   statusBar( "", false);
			};
		},
	    
		callback = function( myFuncts, data ){
			if( myFuncts instanceof Array )	{
				var l = myFuncts.length;
				while(l--){
				     myFuncts[l].call(data);
				}
				return;
			}
			myFuncts.call(data);
		},
         		
		xhr = function( url , clbk, msg){
			statusBar( msg, true);
		    RIF.xhr(  url , c( clbk, "")  );
		},
		
		statusBar = function( msg, showOrHide ){
			$("#statusbar").toggle(showOrHide);
			if( !showOrHide){
			    return;
			};
			$("#statusbar .info").text(msg);
		},
		
		
		requests = {
			
			getGeneralRequest: function( url, myCallback ){  
			    xhr( url,  myCallback );
				return {};
			},
			
			getGeolevels: function( myCallback ){
				var msg = "Retrieving geolevels";
				xhr( "getGeoAvailable.php" , myCallback, msg );
			},
			
			getZoomIdentifiers: function( myCallback, params ){
				var msg = "Retrieving zoom identifiers",
					args = "?geolevel=" + params[0] + 
					       "&identifier=" + "code";		
						   
				xhr("getIdentifiers.php" + args , myCallback, msg );
			},
			
			getSingleFieldData: function( myCallback, params ){
				var msg = "Retrieving values",
				    args = "?geolevel=" + params[0] + 
					       "&identifier=" + params[1];	
						   
				xhr("getIdentifiers.php" + args , myCallback, msg );
			},
			
			getSingleFieldChoro: function( myCallback, params ){
				var msg = "Retrieving choropleth fields",
				    args = "?geolevel=" + params[0] + 
					       "&identifier=" + params[1];	
						   
				xhr("getSingleFieldChoro.php" + args , myCallback, msg );
			},
			
			getFields: function( myCallback, params ){
				var msg = "Retrieving available fields",
				    args = "?geolevel=" + params[0];
					
				xhr( "getFieldsAvailable.php" + args , myCallback, msg );
			},
			
			getTableFields: function( myCallback, params ){
				var msg = "Retrieving table fields",
				    args = "?geolevel=" + params[0];
					
				xhr( "getTableFieldsAvailable.php" + args , myCallback, msg );
			},
			
			getNumericFields: function( myCallback, params ){
				var msg = "Retrieving numeric fields",
				    args = "?geolevel=" + params[0];
					
				xhr( "getNumericFields.php" + args , myCallback );
			},
			
			/* Map */
			getBounds: function( myCallback, params ){
				var msg = "Retrieving bounds", 
				    args = '?table='+ params[0] + '&id='+ params[1] ;	
					
		        xhr( "getBounds.php" + args , myCallback );
	        },
			
			getFullExtent: function( myCallback, params ){
			   var msg = "Retrieving full extent",
			       args = '?table='+ params[0];
			   
			   xhr( 'getFullExtent.php' + args,  myCallback );
		    },
			
			getTabularData: function( myCallback, params ){
				var msg = "Retrieving data for table",
			       args = '?geolevel='+ params[0] +
				   '&from='+ params[1]  + '&to='+ params[2]

				if( typeof params[3] !== 'undefined'){
					var l = params[3].length;
					while(l--){
						args += '&fields[]='+params[3][l];
					}
				}   
			    xhr( 'getDataTable.php' + args,  myCallback );
			}
			
	    };
	
	
	RIF.extend( requests, RIF );
	
}());