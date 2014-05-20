RIF.menu.geoLevel = (function(){
	
	var parent = this,
	    
		/* geolevel obj */
	    _p = {
			
			init: function(){
				this.events();
				RIF.getGeolevels( _p.geolevelsClbk );
			},
			
			/* DOM elements */
			geolevels: $('#geolevel select'),
			zoomTo:    $('#zoomTo select'),		
			avlbFlds:  $(''),

			/* callbacks */
			geolevelsClbk: function(){ // called once only
				if ( this.length > 0 ){
				    parent.dropDown( this , _p.geolevels );
					_p.setGeolevel( this[0][0] );
				    parent.facade.addGeolevel( this[0][0] );
				}	
			},
			
			getZoomIdentifiers: function ( args ){
			    RIF.getZoomIdentifiers( this.zoomToClbk , [args] );
			},
			
			zoomToClbk: function(){
				parent.dropDown( this, _p.zoomTo );
			},
			
			avlbFieldsClbk: function(arg){
				parent.dropDown( this, z.el );
			},
			
			setGeolevel: function(geolvl){
				_p.currentGeolvl = geolvl;
			},
			
			getGeolevel: function(geolvl){
				return _p.currentGeolvl;
			},
			
			/* events */
			events: function(){
				this.zoomTo.on('change', function() {
					parent.facade.zoomTo( RIF.replaceAll('_',' ', this.value) );
				});
				
				this.geolevels.on('change', function() {
					_p.setGeolevel( this.value );
					parent.facade.addGeolevel( this.value );
				});
			}
	    };
	
	_p.init();
	
	return _p;
});