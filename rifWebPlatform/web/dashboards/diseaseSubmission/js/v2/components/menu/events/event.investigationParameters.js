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



} );