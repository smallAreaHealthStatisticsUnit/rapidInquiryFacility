RIF.map.layer.settings = (function (mysett) {

    var settings = {
	
        geoLevel: "",
        selectionField: "code",
        renderer: "svg", // svg || canvas
		style: RIF.style()
		
    };
	
    return RIF.extend( mysett , settings );
});