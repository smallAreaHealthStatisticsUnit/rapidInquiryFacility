RIF.statusBar = ( function() {

  var statusBarMsgs = {},
    _error = 0,

    _appendRemove = function( i, msg, showOrHide ) {
      if ( showOrHide ) {
        $( "#statusbar>div" ).append( "<div class='info msg" + i + "' >" + msg + "</div>" );
      } else {
        $( ".msg" + i ).remove();

      }
    },

    _showError = function( msg ) {
      _error = 1;
      $( "#statusbar" ).show();
      $( "#statusbar>div" ).append( "<div class='error' >" + msg + "</div>" );
    },

    _removeErrors = function() {
      _error = 0;
      $( "#statusbar" ).hide();
      $( '.error' ).remove();
    };


  var updateStatusBar = function( msg, showOrHide, error ) {


    if ( error === -1 ) { // reset errors
      _removeErrors();
    };

    if ( error == 1 ) { // errors notification
      _showError( msg );
      return;
    };

    var indexOfMsg = -1,
      nRequests = 0,
      requestsInProgress = 0,
      requestsCompleted = 0;

    statusBarMsgs[ msg ] = showOrHide;

    for ( var cmsg in statusBarMsgs ) {
      if ( msg === cmsg ) {
        indexOfMsg = nRequests;
      };
      if ( !statusBarMsgs[ cmsg ] ) {
        requestsCompleted++;
      } else if ( statusBarMsgs[ cmsg ] ) {
        requestsInProgress++;
      }
      nRequests++;
    };

    if ( typeof error === 'undefined' ) {
      _appendRemove( indexOfMsg, msg, showOrHide );
    };

    if ( nRequests == requestsCompleted && _error !== 1 ) {
      $( "#statusbar" ).hide();
      return;
    };

    if ( requestsInProgress == 1 || _error === 1 ) {
      $( "#statusbar" ).show();
    };
  };

  return updateStatusBar;

}() );