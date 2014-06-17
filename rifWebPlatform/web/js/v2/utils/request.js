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
				var dataTable = params[0]  ,
					msg = "Retrieving values",
				    args = "?geolevel=" + dataTable + 
					       "&identifier=" + params[1];	
						   
				xhr("getIdentifiers.php" + args , myCallback, msg );
			},
			
			getSingleFieldChoro: function( myCallback, params ){
				var dataTable = params[0]  ,
					msg = "Retrieving choropleth fields",
				    args = "?geolevel=" + dataTable + 
					       "&identifier=" + params[1];	
						   
				xhr("getSingleFieldChoro.php" + args , myCallback, msg );
			},
			
			getFields: function( myCallback, params ){
				var dataTable = params[0] ,
					msg = "Retrieving available fields",
				    args = "?table=" + dataTable;
					
				xhr( "getFieldsAvailable.php" + args , myCallback, msg );
			},
			
			getTableFields: function( myCallback, params ){
				var dataTable = params[0],
					msg = "Retrieving table fields",
				    args = "?table=" + dataTable;
					
				xhr( "getTableFieldsAvailable.php" + args , myCallback, msg );
			},
			
			getDataSetsAvailable: function( myCallback, params ){
				var msg = "Retrieving Data Sets available",
				    args = "?geolevel=" + params[0];
					
				xhr( "getDataSetsAvailable.php" + args , myCallback, msg );
			},
			
			getNumericFields: function( myCallback, params ){
				var dataTable = params[0] ,
					msg = "Retrieving numeric fields",
				    args = "?geolevel=" + dataTable;
					
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
				var dataTable = params[0] ,
				    msg = "Retrieving data for table",
			        args = '?table='+ dataTable + 
				    '&from='+ params[1]  + '&to='+ params[2]

				if( typeof params[3] !== 'undefined'){
					var l = params[3].length;
					while(l--){
						args += '&fields[]='+params[3][l];
					}
				};

				if( typeof params[4] !== 'undefined'){
					var l = params[4].length;
					while(l--){
						args += '&gids[]='+params[4	][l];
					}
				};
				
			    xhr( 'getDataTable.php' + args,  myCallback );
			},
			
			getTableRows: function( myCallback, params ){
				var dataTable = params[0]  ,
				    msg = "Retrieving rows for table",
			        args = '?table='+ dataTable ;

				if( typeof params[1] !== 'undefined'){
					var l = params[1].length;
					while(l--){
						args += '&fields[]='+params[1][l];
					}
				} 
				
				if( typeof params[2] !== 'undefined'){
					var l = params[2].length;
					while(l--){
						args += '&gids[]='+params[2][l];
					}
				} 

			   xhr( 'getTableRows.php' + args,  myCallback );
			},
			
			dropDatatable: function(){	
				var empty = function(){};
			    xhr( 'dropDatatable.php',  empty );
			}
			
	    };
	
	
	RIF.extend( requests, RIF );
	
}());