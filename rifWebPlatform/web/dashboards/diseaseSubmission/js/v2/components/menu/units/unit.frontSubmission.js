RIF.menu.frontSubmission = ( function ( _dom, menuUtils ) {

  var _p = {
    showDialog: function ( dialog ) {
      console.log( dialog );
      $( _dom[ dialog ] ).show();
    },
    getHealthThemes: function ( themes, description ) {
      var el = _dom[ 'healthThemeAvailablesEl' ];
      menuUtils.dropDownInputText( themes, el );
    },
    getNumDenom: function ( num, denom ) {
      menuUtils.dropDownInputText( num, _dom[ 'numeratorAvailablesEl' ] );
      menuUtils.dropDownInputText( denom, _dom[ 'denominatorAvailablesEl' ] );
    },

    writeUserName: function ( userName ) {
      _dom.userName.text( userName );
      $( _dom.logOut ).show();
    }


  };

  return _p;
} );