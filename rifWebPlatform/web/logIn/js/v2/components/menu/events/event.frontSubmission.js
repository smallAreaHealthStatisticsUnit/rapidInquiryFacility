RIF.menu[ 'event-frontSubmission' ] = ( function( dom ) {

  var menuContext = this;

  $( dom.dialogClose ).click( function() {
    var id = $( this ).attr( 'href' );
    $( id ).hide();
  } );

  $( dom.studyArea ).click( function() { //Fire
    $( dom.studyAreaDialog ).show();
  } );

  $( dom.compArea ).click( function() { //Fire
    $( dom.compAreaDialog ).show();
  } );

  $( dom.invParameters ).click( function() { //Fire
    $( dom.investigationDialog ).show();
  } );


  $( dom.healthThemeAvailablesEl ).children().click( function() {
    //Fire event Health theme changed
  } );

  $( dom.numeratorAvailablesEl ).children().click( function() {
    //Fire event Numerator changed
  } );

  $( dom.denominatorAvailablesEl ).children().click( function() {
    //Fire event Denominator changed
  } );




} );