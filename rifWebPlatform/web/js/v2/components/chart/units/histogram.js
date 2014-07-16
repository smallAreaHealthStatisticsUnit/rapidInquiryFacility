RIF.chart.histogram = (function( geolevel ) {

	
	var chart = this,
	    
		settings = {
			
			field: "popcount",
			gidsToupdate: [],
			year: null,
			
			margin: {
				top: 10, 
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
		
		if( settings.year !== null){
		    params.push( settings.year );
		};
		
		RIF.getHistogramData( _render , params );
	},
	
	_render = function( data ){
		_clear();
		var r = RIF.chart.histogram.d3renderer(  settings, data );		
	},
	
	_clear = function(){
		$('#distHisto').empty(); 
	},
	
	_setHistoFieldName = function( field ){
		$('#distHistoField').text( field );
	},
	
	_p = {	
	    
		setHistoSettings: function( sett ){
			settings = RIF.mix( settings, sett );
		},
		
		updateHisto: function( sett ){
			var callback = function(){
				_setHistoFieldName(sett.field);
				_p.setHistoSettings(sett);
				_render( this );
			};
			
			RIF.getHistogramData( callback, [sett.dataSet, sett.field ] );
		},
		
		drawHisto: function( sett ){
			_doHisto();
			//_render();
		}
	};
	
	return _p;
	
});