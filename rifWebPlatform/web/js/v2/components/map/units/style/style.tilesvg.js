RIF.style.tilesvg = (function ( type ) {

    var stylesvg = {

        style: function (id) {
			var c = this.default.fill;
			this.setAreaColor ( id, c);
            
			return "fill:" + c + ";stroke:" + this.default.stroke + ";stroke-width" + this.default["stroke-width"];
        },
		
		repaint: function(){
			var style = this; // reference to parent RIF.style
			d3.select(".leaflet-zoom-animated").selectAll("path")
			    .each(function(d,i){	
					if(typeof d !== 'undefined'){
						var pathId = RIF.addG(d.id);
						this.style.fill =  style.colors[pathId] ;	
						this.style.stroke =  style.default.stroke ;
					}	
				});
		}
		
    };

    return stylesvg;
});