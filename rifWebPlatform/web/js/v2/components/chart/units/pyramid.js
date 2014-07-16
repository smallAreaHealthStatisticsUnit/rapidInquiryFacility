RIF.chart.pyramid = (function( geolevel ) {
    
	var chart = this,
	    
		settings = {
			
			geolevel: "",
			field: "popcount",
			element:"pyramid",
			ageGroups: [],
			gidsToupdate: [],
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
		
		var params = [ settings.geolevel, settings.field ];
		
		if( settings.gidsToupdate.length > 0){
		    params.push( settings.ageGroups );
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
				_p.setPyramidSettings(sett);
				settings.ageGroups = this;
				_doPyramid();
			};
			
			RIF.getAgeGroups( callback, sett.geolevel);
		},
		
		drawPyramid: function( sett ){
			_doPyramid();
		}
	};
	
	return _p;
	
});