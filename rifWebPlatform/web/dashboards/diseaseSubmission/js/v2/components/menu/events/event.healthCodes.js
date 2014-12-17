RIF.menu[ 'event-healthCodes' ] = ( function( _dom ) {

  var menuContext = this;

  _dom.icdClassification.change( function() {
    var val = $( this ).val();
    menuContext.proxy.updateTopLevelHealthCodes( val );
  } );


} );