/* context passed by calling object 
   type: "geojson"| "tilegeojson"| "topojson"| "tiletopojson",
*/
RIF.map.layer = (function (type, sett) {

	var map = this,
        
		layer = RIF.mix(
			      
				  RIF.map.layer.settings(sett), 
			      RIF.map.layer.hover(),
				  
				  {	

					selection: {},
					hoverLbls: {}, // selection field
					
					init: function (layerType) {			
						RIF.map.layer[layerType].call(layer);
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
					
					highlight: function(id, slctd){
						var isSlctd = slctd || layer.isSlctd(id),
						    fill = (isSlctd) ? layer.style.slctd.fill : layer.style.colors[id],
						    stroke = (isSlctd) ? layer.style.slctd.stroke : layer.style.default.stroke ;
						
						d3.select("#"+id)
						 .style("fill", fill)
						 .style("stroke", stroke)
						 .style("stroke-width", 2);
					},
					
					slct: function (id) {
						if (typeof this.selection[id] === 'undefined') {
							this.selection[id] = 1;
						} else {
							delete this.selection[id];
						}
						this.highlight(id);
					},
					
					clearLegend: function(){
					    $('.map-legend').empty();
					}
					
				});
			
    layer.init( type );
	
    return layer;
});