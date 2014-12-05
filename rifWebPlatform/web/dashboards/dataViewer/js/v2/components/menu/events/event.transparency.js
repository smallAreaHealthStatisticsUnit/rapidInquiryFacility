RIF.menu[ 'event-transparency' ] = ( function( dom ) {

  var menuContext = this;

  dom.transpSlider.change( function() {
    var transparency = $( this ).val();
    menuContext.facade.transparencyChanged( transparency );
  } );

} );