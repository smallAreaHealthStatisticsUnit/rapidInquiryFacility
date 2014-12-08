RIF.menu.logIn = ( function( _dom ) {

  var parent = this,


    _requests = {

      isLoggedIn: function( params ) {
        RIF.getIsLoggedIn( _callbacks[ 'isLoggedIn' ], params );
      },

      logIn: function( params ) {
        RIF.getLogIn( _callbacks[ 'logIn' ], params );
      },

    },

    _callbacks = {

      isLoggedIn: function() {
        if ( this == true ) {
          window.top.location = "../dashboards/diseaseSubmission/#" + RIF.user; //default to this for now
        };
      },

      logIn: function() {
        if ( typeof this === 'object' && this.hasOwnProperty( 'errorMessages' ) ) {
          var t = this.errorMessages[ 0 ];
          RIF.statusBar( t, 1, 1 );
        } else {
          window.top.location = "../dashboards/diseaseSubmission/#" + RIF.user; //default to this for now
        }
      }

    },


    _p = {

      request: function( reqName, params ) {
        _requests[ reqName ]( params );
      },

      callback: function( clbkName, params ) {
        _callbacks[ clbkName ]( params );
      }

    };


  _requests.isLoggedIn( [ RIF.user ] );

  return {
    logIn: _p
  };

} );