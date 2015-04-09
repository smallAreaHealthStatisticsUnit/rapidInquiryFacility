( function () {
  var c = function ( myFunc, msg ) {
      //IE compatible
      return function ( error, status, json ) {
        try {
          var data = jQuery.parseJSON( json.responseText );
          for ( var key in data[ 0 ] ) {
            if ( key == 'errorMessages' ) {
              RIF.statusBar( msg, false );
              RIF.statusBar( data[ 0 ][ key ], true, 1 );
              return;
            };
          };
          callback( myFunc, data );
        } catch ( e ) {
          callback( myFunc, json.responseText ); // This should change when working on results viewer
        }
        RIF.statusBar( msg, false );
      };
    },
    asynCall = function () {

      var args = Array.prototype.slice.call( arguments, 0 ),
        callback = args[ 1 ],
        mime = args[ 2 ] || "text/plain",
        parameters = args[ 0 ] + '&userID=' + RIF.user,
        url = 'http://localhost:8080/rifServices/' + parameters;

      $.ajax( {
        url: url
      } ).done( callback ).error( function ( jqXHR, textStatus, errorThrown ) {
        var msg = "Something went wrong with the following service: <br/>" + url + '<br/><br/>' + textStatus + '<br/>' + errorThrown;
        RIF.statusBar( msg, true, 1 );
      } );

    },

    callback = function ( myFuncts, data ) {
      if ( myFuncts instanceof Array ) {
        var l = myFuncts.length;
        while ( l-- ) {
          myFuncts[ l ].call( data );
        }
        return;
      } else if ( typeof myFuncts === 'function' ) {
        myFuncts.call( data )
      };
    },

    xhr = function ( url, clbk, msg, mime ) {
      RIF.statusBar( msg, true );
      asynCall( url, c( clbk, msg ), mime );
    },

    requests = {

      getGeneralRequest: function ( url, myCallback ) {
        xhr( url, myCallback );
        return {};
      },

      getIsLoggedIn: ( function ( myCallback, params ) {
        var msg = " Checking if already logged in.. ";
        xhr( 'studySubmission/isLoggedIn?', myCallback, msg );
      } ),

      getLogOut: ( function ( myCallback, params ) {
        var msg = " Checking if already logged in.. ",
          userName = params[ 0 ],
          pw = params[ 1 ];

        xhr( 'studySubmission/logout?', myCallback, msg );
      } ),

      getLogIn: ( function ( myCallback, params ) {
        var msg = "Logging in.. ",
          userName = params[ 0 ],
          pw = params[ 1 ],
          args = 'password=' + pw;
        xhr( 'studySubmission/login?' + args, myCallback, msg );
      } )

    };

  RIF.utils.extend( requests, RIF );

}() );