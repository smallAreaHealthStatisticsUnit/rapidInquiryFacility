RIF.map.controls = (function () {
	
	var _p = {
	    addLegend: function(){
		    var legend = L.Control.extend({
			    options: {
				    position: 'topright'
				},
				onAdd: function (map){
				    var container = L.DomUtil.create('div', 'map-legend');
					return container;
				}			
			});
			
			_p.add(new legend());
		},
		
		add: function(ctrl){
		    this.map.addControl (ctrl);
		}
	
	};
	
});