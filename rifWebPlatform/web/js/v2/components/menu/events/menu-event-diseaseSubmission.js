RIF.menu[ 'menu-event-diseaseSubmission' ] = ( function() {

  var _p = this; //context

  $( ".modal_close" ).click( function() {
    $( ".overlay" ).hide();
  });    

  $( "#clearSelection" ).click( function() {
    _p.facade.clearSelectionClicked();
  });

  $( "#zoomExtent" ).click( function() {
    _p.facade.zoomToExtent();
  });


} );