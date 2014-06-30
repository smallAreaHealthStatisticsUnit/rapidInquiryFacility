RIF.map.layer.settings = (function (mysett, type) {

    var settings = {
        geoLevel: "",
        selectionField: "code",
		style: RIF.style( type )
    };
	
    return RIF.extend( mysett , settings );
});