RIF.menu[ 'event-frontSubmission' ] = ( function( dom ) {

  var menuContext = this;


  dom.dropdownInputs.keydown( function( e ) {
    return false;
  } );

  $( dom.dialogClose ).click( function() {
    var id = $( this ).attr( 'href' );
    $( id ).hide();

    if ( id === '#statusbar' ) {
      RIF.statusBar( null, null, -1 ); //remove error msgs   
    };

  } );

  $( '#statusbar' ).click( function() {
    $( this ).hide();
    RIF.statusBar( null, null, -1 ); //remove error msgs   
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

  $( dom.logOut ).click( function() {
    menuContext.frontSubmission.request( 'logOut' );

  } )

  /*  Signal change to facade --> Study */

  dom.studyName.change( function() {
    var val = $( this ).val();
    menuContext.facade.studyNameChanged( val );
  } );

  dom.healthTheme.change( function() {
    var val = $( this ).val();
    menuContext.facade.healthThemeChanged( val );
  } );

  dom.numerator.change( function() {
    var val = $( this ).val();
    menuContext.facade.numeratorChanged( val );
  } );

  dom.denominator.change( function() {
    var val = $( this ).val();
    menuContext.facade.denominatorChanged( val );
  } );




} );