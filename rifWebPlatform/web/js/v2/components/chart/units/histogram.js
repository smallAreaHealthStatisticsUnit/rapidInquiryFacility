RIF.chart.histogram = (function( geolevel ) {

	
	var chart = this,
	    
		settings = {
			
			field: "popcount",
			gidsToupdate: [],
			year: null,
			
			margin: {
				top: 0, 
				right: 10, 
				bottom: 20, 
				left: 10
			},
			
			dimensions: { 
				width: $('#distHisto').width() ,
				height: $('#distHisto').height() 
			}
    }, 
	
	_doHisto = function(){
		var params = [ settings.field ];
		
		if( settings.gidsToupdate.length > 0){
		    params.push( settings.ageGroups );
		};
		
		if( settings.year !== null){
		    params.push( settings.year );
		};
		
		RIF.getPyramidData( _render , params );
	},
	
	_render = function(){
		var r = RIF.chart.histogram.d3renderer(  settings, data);		
	},
	
	_p = {	
	    
		setHistoSettings: function( sett ){
			settings = RIF.mix( settings, sett );
		},
		
		updateHisto: function( sett ){
			var callback = function(){
				_p.setHistoSettings(sett);
				_doHisto();
			};
			
			//RIF.getAgeGroups( callback, sett.geolevel);
		},
		
		drawHisto: function( sett ){
			//_doHisto();
			_render();
		}
	};
	
	return _p;
	
});