RIF.menu[ 'event-frontSubmission' ] = ( function ( dom, firer ) {

  var checkDialogSelection = function ( d ) {
    var dialogName = d.replace( '#', '' );
    firer.isDialogSelectionComplete( dialogName );
  };

  var close = function () {
    var id = $( this ).attr( 'href' );
    $( id ).hide();
    if ( id === '#statusbar' ) {
      RIF.statusBar( null, null, -1 );
    };
    checkDialogSelection( id );
  };

  dom.dropdownInputs.keydown( function ( e ) {
    return false;
  } );

  $( dom.dialogClose ).click( close );
  $( dom.done ).click( close );


  $( dom.studyArea ).click( function () {
    firer.isDialogReady( 'studyAreaDialog' );
  } );

  $( dom.compArea ).click( function () {
    $( dom.compAreaDialog ).show();
  } );

  $( dom.stat ).click( function () {
    //$( dom.statDialog ).show();
    firer.isDialogReady( 'statDialog' );
  } );

  $( dom.importExportEl ).click( function () {
    $( dom.retrieveDialog ).show();
  } );

  $( dom.fromFile ).click( function () {
    $( dom.runFromFileModal ).show();
  } );

  $( dom.runEl ).click( function () {
    $( dom.runModal ).show();
  } );

  $( dom.invParameters ).click( function () {
    firer.isDialogReady( 'investigationDialog' );
  } );

  $( dom.logOut ).click( function () {
    firer.fire( 'logOut', null );
  } );


  dom.studyName.change( function () {
    var val = $( this ).val();
    if ( val != '' ) {
      dom.studyName.addClass( 'inputBorderSelection' );
    } else {
      dom.studyName.removeClass( 'inputBorderSelection' );
      val = null;
    };
    firer.studyNameChanged( val );
  } );

  dom.healthTheme.change( function () {
    var val = $( this ).val();
    firer.healthThemeChanged( val );
  } );

  dom.numerator.change( function () {
    var val = $( this ).val();
    firer.numeratorChanged( val );
  } );

  dom.denominator.change( function () {
    var val = $( this ).val();
    firer.denominatorChanged( val );
  } );
} );