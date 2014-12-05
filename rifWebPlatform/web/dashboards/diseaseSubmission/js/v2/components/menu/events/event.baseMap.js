RIF.menu[ 'event-baseMap' ] = ( function( dom ) {

  var menuContext = this;

  dom.baseMapObj.click( function() {
    dom.baseMap.show();
  } );

  dom.saveObj.click( function() {
    dom.baseMap.hide();
    menuContext.facade.baseMapChanged( _getBaseMapSelected() );
  } );

  dom.nobasemap.change( function() {
    if ( _isBaseMapChecked() ) {
      _enableDisable( '#EDEDED', true );
      return;
    };
    _enableDisable( '#F7F7F7', false );
  } );

  var _enableDisable = function( color, disableEnable ) {
      dom.baseMapFieldSelection.attr( "disabled", disableEnable );
      dom.baseMapFieldSelection.css( 'background', color );
      dom.basemapselector.css( 'background', color );
    },

    _getBaseMapSelected = function() {
      if ( _isBaseMapChecked() ) {
        return '';
      };
      var m = dom.baseMapFieldSelection.find( ":selected" ).val();
      return menuContext.baseMap.getBaseMap( m );
    },

    _changeBaseMap = function() {
      menuContext.facade.baseMapChanged( _getBaseMapSelected() );
    };

  _isBaseMapChecked = function() {
    return dom.nobasemap.is( ":checked" );
  };

  menuContext.dropDown( menuContext.baseMap.getBaseMapsName(), dom.baseMapFieldSelection );




} );