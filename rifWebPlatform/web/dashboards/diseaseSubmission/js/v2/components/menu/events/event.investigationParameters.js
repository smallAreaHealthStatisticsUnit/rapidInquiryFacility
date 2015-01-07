RIF.menu[ 'event-investigationParameters' ] = ( function( _dom ) {

  var menuContext = this;

  _dom.startYear.change( function() {
    var val = $( this ).val();
    menuContext.facade.startYearChanged( val );
  } );

  _dom.endYear.change( function() {
    var val = $( this ).val();
    menuContext.facade.endYearChanged( val );
  } );

  _dom.gender.change( function() {
    var val = $( this ).val();
    menuContext.facade.genderChanged( val );
  } );


} );