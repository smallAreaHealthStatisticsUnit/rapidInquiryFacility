RIF.dom = ( function() {


  var dom = {
    menu: {
      logIn: function() {
        return {
          username: $( '#userName' ),
          password: $( '#pw' ),
          logInBtn: $( '#logInBtn' ),
          dialogClose: $( '.modal_close' )
        }
      }
    }
  };


  RIF.dom = dom;

} );