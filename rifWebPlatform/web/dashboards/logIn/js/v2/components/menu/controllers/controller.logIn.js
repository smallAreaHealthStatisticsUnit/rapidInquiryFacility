RIF.menu[ 'controller-logIn' ] = ( function ( unit ) {

  var _p = {

    _redirect: function () {
      var url = RIF.utils.getRedirectFromURL();
      RIF.utils.redirect( "../" + url + "/?user=" + RIF.user );
    },

    isLoggedIn: function ( params ) {
      var callback = function () {
        if ( this[0][ "result" ] == "true" ) { //
          _p._redirect();
          console.log( "redirect" );
        };
      };
      RIF.getIsLoggedIn( callback, null );
    },

    logIn: function ( params ) {
      var callback = function () {
        if ( this[0].hasOwnProperty( "result" ) ) {
          _p._redirect();
        } else if ( this.hasOwnProperty( 'errorMessages' ) ) {
          var t = this.errorMessages[ 0 ];
          RIF.statusBar( t, 1, 1 );
        } else {
          RIF.statusBar( "Could not log you in.", 1, 1 );
        }
      };
      RIF.getLogIn( callback, params );
    },

  };


  _p.isLoggedIn();

  return _p;
} );