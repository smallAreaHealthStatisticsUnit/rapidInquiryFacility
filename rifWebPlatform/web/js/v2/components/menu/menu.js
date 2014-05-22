RIF.menu = (function(menus){
	
	var m = menus.length,
	//Shared methods across menus
        _p = {
		
			events: function(){
				$(".modal_close").click(function(){
					$(".overlay").hide();
				});
				
				$(".dropdown dt > div").click(function() {
					$(".dropdown .palette").toggle();
				});
				
			}(),
			
		    dropDown: function( data, el ){
			    el.empty();
				if( data.length > 0){
					_p.dropDownFromArray(data, el);
				}else if ( ! jQuery.isEmptyObject( data ) ){
				    _p.dropDownFromObj( data, el);
				}else{
					_p.dropDownFromObj( { "N/A": "None available"}, el);
				}
			},
			
			dropDownFromArray: function(arr, el){
				var l = arr.length;
				while(l--){
					var val = (arr[l][0]) ? arr[l][0].replace(/\s+/g, '_') : arr[l],
					    option_text = ( arr[l][1] ) ? arr[l][1] : (arr[l][0]) ? arr[l][0] : val ;
						
					_p.addSelectOption( el, val, option_text );
				}
			},
			
			fieldCheckboxes: function( obj , el , name ){
				el.empty();
				var counter = 0, checked = true;
				for (var key in obj) {
					var id = "filterCols" + counter++,
					    p  = _p.getCheckBoxLabel( name, obj[key], obj[key], id , checked );
					 
					 el.prepend( '<div>' + p + '</div>'); 	 
				}
			},
			
			greyOut: function (el){
				el.find("select, button").prop('disabled', 'disabled').css({opacity:'0.5'});
			},
			
			removeGreyOut: function (el){
				el.find("select, button").prop('disabled', false).css({opacity:'1'});
			},
			
			dropDownFromObj: function(obj, el){
				for (var key in obj) {
					_p.addSelectOption( el, key, obj[key] );
				}
			},
			
			getSpecialDropdownValue: function (id) {
				return $("#" + id).find("dt a span.value").html();
			},
			
			addSelectOption: function(slct, val , option_text ){
			      slct.prepend( "<option value="+ val +">"+ option_text + "</option>" )
			},
			
			getCheckBoxLabel: function( name, val_txt , id, checked ){
				  var c = (checked) ? "checked" : "",
			          p = '<input type="checkbox" name="'+name+'" value="'+val_txt+'" id="'+id+'" class="colsChoice" '+c+' />' + 
						   '<label for="'+id+'" class="colsChoiceLbl">'+ val_txt +'</label>';
				  
				   return p;
			},
			
			getCheckedValues: function( name ){
				var checkedValues = $('input[name="'+name+'"]:checked').map(function() {
					return this.value;
				}).get().reverse();
				
				return checkedValues;
			},
			
			getAvlbFields: function( arg ){
			    RIF.getFields( _p.avlbFieldsClbkSettings , [arg] );
			},
			
			getNumericFields: function( arg ){
			    RIF.getNumericFields(  _p.avlbFieldsClbkChoro, [arg] );
			},
			
			facade: {
				/* Subscribers */
				uZoomOpts: function(args){
					_p.getZoomIdentifiers(args);
				},
				
				uAvlbFlds: function(args){
					_p.getAvlbFields(args);
					_p.getNumericFields(args);
				},
				
				getScaleRange: function(args){
					_p.showScaleRange( args );
				},
				
				/* firers */
				addGeolevel: function( geolvl ){
					this.fire('addGeolevel', { "geoLevel" : geolvl });
				},
				
				zoomTo: function( id ){
					this.fire('zoomToArea', id);
				},
				
				hoverFieldChange: function( field ){
					this.fire('hoverFieldChange', field);
				},
				
				filterTablebyCols: function( fields ){
					this.fire('filterCols', [fields, _p.getGeolevel() ]);
				}
				
			}	
		};
	
	/* Extend _p with all menus */ 
	(function(){
		while(m--){ 
			var r = RIF.menu[menus[m]].call(_p);
			_p = RIF.mix(r , _p);		
		}
	}());
	
	return _p.facade;
	
});