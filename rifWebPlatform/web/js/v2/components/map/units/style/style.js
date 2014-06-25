/*
 * RIF.style extend RIF.style[ "tilesvg" | "tilecanvas" ] specific styles renderer for SVG or CANVAS
 * The following methods must be implemented by :
 * 
 * @style(id)
 * @repaint()
 *
 */

RIF.style = (function ( type ) {

	var style = RIF.mix( RIF.style[type](), {

        colors: {},
		
		mappedField: {},
        
		default: {
            fill: "#9BCD9B",
			stroke: "#F8F8FF",
			"stroke-width" : 0.5
        },
		
        hover: {
            fill: "#1E90FF"
        },
		
        slctd: {
            fill: '#FFE1FF',
			stroke: '#9BCD9B',
			"stroke-width" : 0.1
        },
		
        getStyle: function (id, renderType) { 
		    
			var c = this.colors[id];
			this.setAreaColor( id, c);
			
			if (typeof c !== 'undefined') {
				var s = (renderType === 'tilesvg' ) ? 
				     "fill:" + c + ";stroke:" + style.default.stroke  :
				    { color: c, outline:{ color: "transparent", size: 0} };
				return s;	
			};
			
			return this.style(id);   
        },
		
		setAreaColor: function( id , c , override ){
			if (typeof this.colors[id] === 'undefined' || override) {
                this.colors[id] = c;
            };
		},
		
		setScale: function( params, min, max ){
			this.scale = RIF.style.scales.call(this, params, min, max ); 
		},
		
		setColorBrewer: function( colorScale, intervals ){
		    this.colorbrewer = RIF.colorbrewer[colorScale][intervals];
		},
		
		setSingleColor: function( params ){
		    this.setColorBrewer( params["colorScale"], params["intervals"] );
			this.updateColors();
		},
		
		updateColors: function( values ){
		    if(typeof values !== 'object'){
			    var values = this.colors,
				    singleColor = style.colorbrewer[0];
			};
			
			for (var key in values) {
				var col = singleColor || 
					style.colorbrewer[style.scale(values[key])];
				this.colors[key] = col;
			};
			style.repaint();	
		},
		
		updateLegend: function( ){		
			var legend = d3.select('.map-legend');
			$(legend[0]).empty();
			legend.append('ul')
				.attr('class', 'list-inline');
			
			var length = style.breaks.length,		
			    keys = legend.selectAll('li.key')
				 .data(style.breaks);//reverve to order legend from min to max
				
			keys.enter().append('li')
				.attr('class', function(d,i){ 
					if( i === 0  ){
						return 'key maxkey';
					 }
					return 'key'
				})
				.style('border-left-color', function(d,i){return  style.colorbrewer[length - (i + 1)];})
				.append('a')
				 .text(function(d,i) { 
					 return d;
				 }); 
		},
		
		setChoropleth: function( values, params, updateLegend ){
			
			params.values = d3.values( values ).map(function(d) { return +d; });
			params.max = d3.max( params.domain ) || d3.max( params.values );
			params.min = d3.min( params.domain ) || d3.min( params.values );
			
            this.setColorBrewer( params["colorScale"], params["intervals"] );			
			this.setScale( params );
			
			if( updateLegend ){
			    this.updateLegend();
			};	
		}
		
    });

    return style;
});