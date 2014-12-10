RIF.menu[ 'event-studyArea' ] = ( function( _dom ) {

  var menuContext = this;

  _dom.selectAt.change( function() {
    var val = $( this ).val();
    menuContext.proxy.selectAtChange( val );
  } );

  _dom.resolution.change( function() {
    var val = $( this ).val();
    menuContext.facade.resolutionChanged( val );
  } );


} );