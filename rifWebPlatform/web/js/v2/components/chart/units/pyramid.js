RIF.chart.pyramid = (function( geolevel ) {
    
	var chart = this,
	    
		settings = {
			
			geolevel: "",
			field: "popcount",
			element:"pyramid",
			ageGroups: [],
			gids: [],
			year: null,
			
			margin: {
				top: 0, 
				right: 20, 
				bottom: 10, 
				left: 35
			},
			
			dimensions:{ 
				width: $('#pyramid').width() - 40 ,
				height: $('#pyramid').height() -20
			}
    }, 
	
	_doPyramid = function(){
	
		_clear();
		
		var params = [ settings.geoLevel, settings.field ];
		
		if( settings.gids.length > 0){
		    params.push( settings.gids );
		};
		
		if( settings.year !== null){
		    params.push( settings.year );
		};
		
		RIF.getPyramidData( _render , params );
	},
	
	_render = function(){
		var r = RIF.chart.pyramid.d3renderer( settings, d3.csv.parse(this));		
	},
	
	_clear = function(){
		$('#pyramid').empty(); 
	},
	
	_p = {	
	    
		setPyramidSettings: function( sett ){
			settings = RIF.mix( settings, sett );
		},
		
		updatePyramid: function( sett ){
			var callback = function(){
				settings.ageGroups = this;
				_doPyramid();
			};
			
			_p.setPyramidSettings(sett);
			RIF.getAgeGroups( callback, [settings.geoLevel]);
		},
		
		drawPyramid: function( sett ){
			_doPyramid();
		}
	};
	
	return _p;
	
});