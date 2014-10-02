RIF.menu[ 'menu-event-diseaseMapping' ] = ( function() {

  var _p = this; //context

  $( ".modal_close" ).click( function() {
    $( ".overlay" ).hide();
  } );

  $( ".dropdown dt > div" ).click( function() {
    $( ".dropdown .palette" ).toggle();
  } );

  $( "#clearSelection" ).click( function() {
    _p.facade.clearSelectionClicked();
  } );

  $( "#zoomExtent" ).click( function() {
    _p.facade.zoomToExtent();
  } );


} );