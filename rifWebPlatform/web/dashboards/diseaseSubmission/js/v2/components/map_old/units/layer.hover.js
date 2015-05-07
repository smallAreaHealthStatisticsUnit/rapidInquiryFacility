RIF.map.layer.hover = ( function () {
  var tooltip = document.getElementById( 'tooltip' ),
    geoname = document.getElementById( 'geoname' ),
    offset = 25;
  leaflet_svg = {
    hover: function ( label ) {
      $( this ).mousemove( function ( e ) {
        var xPos = e.pageX + 15,
          yPos = e.pageY - 40;
        $( tooltip ).css( {
          'top': yPos,
          'left': xPos,
          'display': 'block'
        } );
        geoname.innerHTML = label;
      } )
    },
    out: function () {
      $( this ).unbind();
      tooltip.style.display = 'none';
    }
  };
  return leaflet_svg;
} );