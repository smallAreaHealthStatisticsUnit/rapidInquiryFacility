RIF.style = (function (json) {

    var style = {

        colors: {},
		mappedField: {},
        default: {
            fill: "#EAEAEA",
			stroke: "#D1D1D1"
        },
		
        hover: {
            fill: "#1E90FF"
        },
		
        slctd: {
            fill: '#FF0000',
			stroke: '#FFFF00',
			"stroke-width": 2
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
			
			if( renderType == 'tilesvg' ){
                return this.svgStyle(id);
			}else if ( renderType == 'tilecanvas' ){
				return this.canvasStyle(id); 
			}	   
        },

        svgStyle: function (id) {
			var c = this.getRandom( id );
            return  "fill:" + c + ";stroke:" + style.default.stroke ;
        },
		
		canvasStyle: function (id) {
            var c = this.getRandom( id );
            return { color: c, outline:{ color: "transparent", size: 1} };
        },
		
		getRandom: function (id) {
		    var rando = function(min, max){
			    return Math.floor(Math.random() * (max - min + 1)) + min;
			},
		    c = "hsl(" + rando(140, 150) +
                ", " + rando(20, 60) +
                "%, " + rando(40, 95) + "%)";
				
            this.setAreaColor ( id, c);
			
            return c; 			     
        },
		
		setAreaColor: function( id , c , override ){
			if (typeof this.colors[id] === 'undefined' || override) {
                this.colors[id] = c;
            };
		},
		
		setScale: function( params, min, max ){
			this.scale = RIF.style.scales( params, min, max ); 
		},
		
		setColorBrewer: function( colorScale, intervals ){
		    this.colorbrewer = RIF.colorbrewer[colorScale][intervals];
		},
		
		setChoropleth: function( values, params, updateLegend ){
			
			if( params.domain.length === 0){
			    params.values = d3.values(values).map(function(d) { return +d; });
			};
			
			params.max = d3.max( params.domain) || d3.max(params.values );
			params.min = d3.min( params.domain) || d3.min(params.values );
			
            this.setColorBrewer( params["colorScale"], params["intervals"] );			
			this.setScale( params );
			this.setScaleBreaks(params);
			
			if( updateLegend ){
			    this.updateLegend();
			};	
		},
		
		setSingleColor: function(params){
		    this.setColorBrewer( params["colorScale"], params["intervals"] );
			this.updateColors();
		},
		
		setScaleBreaks: function(params){
			var l = params.intervals;
			this.scale.breaks = [];
			while(l--){
			    var r = style.scale.invertExtent(l);
				this.scale.breaks.push(d3.round(r[1], 0));
			}
		},
		
		updateLegend: function( ){
						
			var legend = d3.select('.map-legend');
			$(legend[0]).empty();
			legend.append('ul')
				.attr('class', 'list-inline');
			
			var length = style.scale.breaks.length,		
			    keys = legend.selectAll('li.key')
				 .data(style.scale.breaks);//reverve to order legend from min to max
				
			keys.enter().append('li')
				.attr('class', 'key')
				.style('border-left-color', function(d,i){return  style.colorbrewer[length - (i + 1)];})
				.append('a')
				 .text(function(d,i) { 
					 return d;
				 });
		},
		
		repaint: function( values ){
		
			d3.select(".leaflet-zoom-animated").selectAll("path")
			    .each(function(d,i){
				    var pathId = RIF.addG(d.id);
					this.style.fill =  style.colors[pathId] ;	
					this.style.stroke =  style.default.stroke ;
				});
		},
		
		updateColors: function( values ){
		    if(typeof values !== 'object'){
				    var values = this.colors,
				        singleColor = style.colorbrewer[0];
			};
			
			for (var key in values) {
					var col = singleColor || style.colorbrewer[style.scale(values[key])];
					this.colors[key] = col;
			};
			
			style.repaint();	
		}
		
    };

    return style;
});