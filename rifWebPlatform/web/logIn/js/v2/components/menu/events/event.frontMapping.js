RIF.menu[ 'event-frontMapping' ] = ( function( dom ) {

  var menuContext = this;

  dom.dialogClose.click( function() {
    var id = $( this ).attr( 'href' );
    $( id ).hide();
  } );


  dom.clearBtn.click( function() {
    menuContext.facade.clearSelectionClicked();
  } );

  dom.zoomToExtent.click( function() {
    menuContext.facade.zoomToExtent();
  } );

} );