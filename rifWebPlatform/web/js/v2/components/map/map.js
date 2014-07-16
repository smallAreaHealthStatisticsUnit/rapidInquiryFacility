/*
 *	Initialize a mapLayer and implements all methods
 *	in the module-> events object
 *  of which the component map is subscribed to.
 *
 *  @init
 *  @param: { type: [geoJSON, tileGeoJSON, tileTopoJSON] }
 *
 */
RIF.map = (function (type) {

    var _p = {
		extentSet: 0,
        init: function () {
            this.map = new L.Map("map", {attributionControl: false});
			this.events().addLegend();
			
            new L.geoJson({
                "type": "LineString",
                "coordinates": [
                    [0, 0],
                    [0, 0]
                ]
            }).addTo( this.map );
        },
        /* events */
		events: function(){
		    this.map.on("dragend",function(e){
				var dist = e.target.dragging._draggable._newPos.x ,
				    x = $("#tooltip").position();
				$("#tooltip").css({left: x + dist + "px"})	
			});
			return this;
		},
		
		addLegend: function(){
		    var legend = L.Control.extend({
			    options: {
				    position: 'topright',
					width: "500px"
				},
				onAdd: function (map){
				    var container = L.DomUtil.create('div', 'map-legend');
					return container;
				}			
			});
			
			this.map.addControl (new legend());
		    return this;
		},
		
		addLayer: function( mysett ){
			this.layer = RIF.map.layer.call( this, type, mysett );
		},
		
		removeLayer: function(){
			if (typeof this.layer === 'object'){
				$('svg.leaflet-zoom-animated g').remove();
				this.layer.clearLegend();
		        this.map.removeLayer( this.layer.mylyr );
		        this.map.invalidateSize();
			};
		},
		
        addTiled: function (lyr, geoTable) {
            if( !this.extentSet){
				this.setFullExtent(geoTable);
			};	
			
			_p.map.addLayer(lyr);	
        },
		
		getZoom: function(){
		    return _p.map.getZoom();	
		},
		
        getCentroid: function (t, clbk) {
            RIF.xhr('getCentroid.php?table=' + t, clbk);
        },
		
		setFullExtent: function( geolevel ){
			this.extentSet = 1;
		    RIF.getFullExtent( this.zoomTo, [geolevel]);
		},
		
		getBounds: function(id){
			var table = _p.layer.geoLevel;
				
		    RIF.getBounds( _p.zoomTo , [table, id]);
	    },
		
		zoomTo: function(){ /* Json context */
			var r = this.split(','),
		        bounds =[[parseFloat(r[0]) , parseFloat(r[1])],[parseFloat(r[2]) , parseFloat(r[3])]];
			
			_p.map.fitBounds(bounds);	
		},
		
		setDataset: function( dataset){
			this.currentDataset = dataset;
		},
		
		getDataset: function(){
			return this.currentDataset;
		},
		
        facade: {
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
        }
    };
    
    _p.init();
    
	return _p.facade;
});