RIF.menu[ 'menu-event-diseaseSubmission' ] = ( function() {

  var _p = this,
    dropdowns = $( ".dropdowns input" ),
    availables = $( '.availables' ),
    isDropDwonHovered = false;


  dropdowns.focus( function() {
    $( this ).next().show();
  } );

  dropdowns.blur( function() {
    if ( !isDropDwonHovered ) {
      $( this ).next().hide();
    }
  } );

  availables.mouseover( function() {
    isDropDwonHovered = true;
  } );

  availables.mouseleave( function() {
    $( this ).hide();
    $( this ).prev().blur();
    isDropDwonHovered = false;
  } );

  availables.children().click( function() {
    var input = $( this ).parent().prev();
    $( input )
      .attr( "value", $( this ).text() )
      .addClass( "inpputBorderSelection" );

    $( this ).parent().hide();
  } );


  $( ".modal_close" ).click( function() {
    $( ".overlay" ).hide();
  } );

  $( "#clearSelection" ).click( function() {
    _p.facade.clearSelectionClicked();
  } );

  $( "#zoomExtent" ).click( function() {
    _p.facade.zoomToExtent();
  } );


} );