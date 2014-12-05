RIF.menu[ 'event-settings' ] = ( function( _dom ) {

  var menuContext = this;

  _dom.settingsBtn.click( function() {
    _dom.settingsMenu.show();
  } );

  _dom.save.click( function() {
    menuContext.facade.hoverFieldChange( _dom.hoverSlct.val() );
    var resultSetChoice = menuContext.getCheckedValues( "resultsSets" );
    menuContext.facade.areaChartSelectionChanged( resultSetChoice );
    _dom.settingsMenu.hide();
  } );


} );