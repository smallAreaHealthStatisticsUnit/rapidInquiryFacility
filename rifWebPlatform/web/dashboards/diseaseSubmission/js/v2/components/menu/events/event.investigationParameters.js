RIF.menu[ 'event-investigationParameters' ] = ( function( _dom ) {

  var menuContext = this;

  _dom.startYear.change( function() {
    var val = $( this ).val(),
      fnct = menuContext.facade.startYearChanged;
    menuContext.proxy.investigationParameterChange( val, fnct );

  } );

  _dom.endYear.change( function() {
    var val = $( this ).val(),
      fnct = menuContext.facade.endYearChanged;
    menuContext.proxy.investigationParameterChange( val, fnct );
  } );

  _dom.gender.change( function() {
    var val = $( this ).val();
    fnct = menuContext.facade.genderChanged;
    menuContext.proxy.investigationParameterChange( val, fnct );
  } );


  _dom.covariatesWrapper.on( "click", 'input', function( aEvent ) {
    $( this ).next().toggleClass( 'labelSelected' );
    var val = menuContext.getCheckedValues( 'covariates' ),
      fnct = menuContext.facade.covariatesChanged;
    menuContext.proxy.investigationParameterChange( val, fnct );

  } );


  _dom.addInvestigation.click( function() {
    menuContext.proxy.addCurrentInvestigation();
  } );


  _dom.clearAll.click( function() {
    $( '.' + _dom.inputBorderSelection ).removeClass( _dom.inputBorderSelection );
    $( '.' + _dom.labelSelected ).removeClass( _dom.labelSelected );

    _dom.startYear.val( "" );
    _dom.endYear.val( "" );
    _dom.gender.val( "" );
    _dom.covariatesChecked().attr( 'checked', false ); // Unchecks it
    menuContext.proxy.investigationNotReadyToBeAdded();
    menuContext.facade.clearAllParameters();
  } );


} );