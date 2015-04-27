RIF.menu[ 'subscriber-frontSubmission' ] = ( function ( controller ) {

  var subscriber = {

    updateHealthThemeAvailables: function () {},
    updateNumeratorAvailables: function () {},
    updateDenominatorAvailables: function () {},

    showDialog: function ( dialog ) {
      controller.showDialog( dialog );
    },

    dialogBgChange: function ( dialog ) {
      controller.dialogBgChange( dialog );
    },

    logOut: function () {
      controller.logOut();
    }

  };

  return subscriber;
} );