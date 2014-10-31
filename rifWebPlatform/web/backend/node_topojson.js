(function(){
    var topojson = require("topojson"),
	     geojs = "";
	process.argv.forEach(function(val, index, array) {
	   if( index === 3){
	      geojs = val;
	   }
	});
    var topology = topojson.topology({collection: geojs});
});	