RIF.map['map-facade-manager'] = (function ( _p ) {
	
	 var facade = {
            /* Subscribed Events */
            updateSelection: function (a) {
				_p.layer.selection = {};
				_p.layer.style.repaint();		
                _p.layer.selectAreas(a);
            },
			
            zoomTo: function (a) {
				_p.getBounds(a);
            },
			
			uGeolevel: function( args ){
				_p.removeLayer();
				_p.addLayer({ "geoLevel" : args.geoLevel } );
				_p.setDataset(args.dataset);
			},
			
			resizeMap: function(){
				_p.map.invalidateSize(true);
			},
			
			uHoverField: function(a){
				_p.layer.joinField(a);
			},
			
			uMapStyle: function(a){
				_p.layer.uStyle(a);
			},
			
			editBreaks: function(a){
			    _p.layer.getBreaks(a);
			},
			zoomToExtent: function(){
				_p.setFullExtent(_p.layer.geoLevel);
			},
			
			clearSelection: function(){
				_p.layer.clearSelection();
			},
			
			/* Firers */			
			populateMenus: function( args ){/* [geolevel] */
			    this.fire('populateMenus', args);
			},
			
			addTabularData: function( dataSets ){
				this.fire('addTabularData', dataSets);
			},
			
			scaleRange: function(args){/* scale */
			    this.fire('scaleRangeReady', args);
			},
			
			selectionChanged: function( selection ){
				this.fire('selectionchange', [ selection, 'map'] );
			}
        };
		
	return facade;	
	
});