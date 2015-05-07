RIF.map[ 'map-event-diseaseMapping' ] = ( function () {
  var map = this.map;
  //Map events
  map.on( "dragend", function ( e ) {
    var dist = e.target.dragging._draggable._newPos.x,
      x = $( "#tooltip" ).position();
    $( "#tooltip" ).css( {
      left: x + dist + "px"
    } )
  } );
  //Add empty geoJSON trick
  ( function () {
    new L.geoJson( {
      "type": "LineString",
      "coordinates": [
        [ 0, 0 ],
        [ 0, 0 ]
      ]
    } ).addTo( map );
  }() );
  //Add Legend
  ( function () {
    var legend = L.Control.extend( {
      options: {
        position: 'topright',
        width: "500px"
      },
      onAdd: function ( map ) {
        var container = L.DomUtil.create( 'div', 'map-legend' );
        return container;
      }
    } );
    map.addControl( new legend() );
  }() );
} );