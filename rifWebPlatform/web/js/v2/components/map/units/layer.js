/*  context passed by calling object 
 *  type: "geojson"| "tilegeojson"| "topojson"| "tiletopojson",
 *  
 *   Layer Types must implement the following methods:
 *   
 *   @highlight(id,scltd) // allows to select a map area
 *   @slct(id) // selects an area and call highlight method
 */
RIF.map.layer = (function (type, sett) {

	var map = this,
        
		layer = RIF.mix(
			      
				  RIF.map.layer.settings( sett, type), 
			      RIF.map.layer.hover(),
				  
				  {	

					selection: {},
					hoverLbls: {}, // selection field
					
					init: function (layerType) {			
						RIF.map.layer[layerType].call(layer);
						map.facade.addTabularData( layer.geoLevel );
					},
					
					add: { 
						tile: function(myLyr){ 
						    layer.mylyr = myLyr;
							map.addTiled(layer.mylyr, layer.geoLevel);
						},
						geojson: function(){},
						topojson: function(){}
					},
					
					clbk: { /* called after layer is rendered */
						tile: function(){ 
							map.facade.addAvlbFields( layer.geoLevel );
							map.facade.addZoomIdentifiers( layer.geoLevel );	
						},
						topojson: function(){}
					},
					
					joinField: function(field){
						var join = function(){
							layer.hoverLbls = this;
						};
						layer.selectionField = field || layer.selectionField;
						RIF.getSingleFieldData( join, [layer.geoLevel, layer.selectionField] );
					},
					
					uStyle: function(params){ /* {classification: , colorScale: , field: , intervals:  }  */
						
						if( params.intervals === 1 ){
							this.style.setSingleColor(params);
							this.clearLegend();
							return;
						};
						
						var doChoro = function(){
							layer.style.setChoropleth( this, params, true ); 
							layer.style.updateColors( this );
                            layer.repaintSlctd();		
						};
						
						RIF.getSingleFieldChoro( doChoro, [ layer.geoLevel, params.field ] )
					},
					
					getBreaks: function( params ){
						 var getScale = function(){
							 layer.style.setChoropleth( this, params, false ); 
							 map.facade.scaleRange(layer.style.scale.breaks);
						 };
						 
						 RIF.getSingleFieldChoro( getScale, [ layer.geoLevel, params.field ] )
					},
					
					resetSlctd: function () {
						/*if (!layer.isTiled()) {
							return;
						}*/
						for (var key in layer.selection) {
							var e = $("#" + key);
							e[0].style.fill = style.colors[key];
						}
					},
					
					repaintSlctd: function(){
						for (var key in this.selection) {
							layer.highlight(key);
						};
					},
					
					isSlctd: function (id) {
						if (this.selection[id] === undefined) {
							return false;
						}
						return true;
					},
					
					slct: function (id) {
						if (typeof this.selection[id] === 'undefined') {
							this.selection[id] = 1;
						} else {
							delete this.selection[id];
						}

						this.highlight(id);
					},
					
					selectAreas: function (ids){
						var l = ids.length;
						while(l--){
							var id = "g" + ids[l];
							this.slct( id );
						}
					},
					
					getLayerStyle: function( id, slctd){
						var isSlctd = slctd || this.isSlctd(id);
						return {
							fill : (isSlctd) ? layer.style.slctd.fill : layer.style.colors[id],
							stroke: (isSlctd) ? layer.style.slctd.stroke : layer.style.default.stroke,
							stroke_width : layer.style.slctd["stroke-width"]
						}	
					},
					
					clearLegend: function(){
					    $('.map-legend').empty();
					},
					
					clearSelection: function(){
						this.selection = [];
						this.resetSlctd();
						this.style.repaint();
					},
					
					selectionChanged: function(){
						var selection = [];
						for (var key in layer.selection) {
							var key = key.substring(1);// remove 'g' from id
							selection.push(key);
						}
						map.facade.selectionChanged(selection);
					}
	
				});
			
    layer.init( type );
    return layer;
});