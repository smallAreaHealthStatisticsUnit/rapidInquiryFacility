RIF.menu.logIn = (function(_dom) {

   var parent = this,

      _redirect = function() {
         var url = RIF.getRedirectFromURL();
         window.top.location = "../" + url + "/?user=" + RIF.user;
      },

      _requests = {

         isLoggedIn: function(params) {
            RIF.getIsLoggedIn(_callbacks['isLoggedIn'], params);
         },

         logIn: function(params) {
            RIF.getLogIn(_callbacks['logIn'], params);
         },

      },

      _callbacks = {

         isLoggedIn: function() {
            if (this["result"] == "true") { //
               _redirect();
            };
         },

         logIn: function() {
            if (this.hasOwnProperty("result")) {
               _redirect();
            } else if (this.hasOwnProperty('errorMessages')) {
               var t = this.errorMessages[0];
               RIF.statusBar(t, 1, 1);
            } else {
               RIF.statusBar("Could not log you in.", 1, 1);
            }
         }

      },


      _p = {

         request: function(reqName, params) {
            _requests[reqName](params);
         },

         callback: function(clbkName, params) {
            _callbacks[clbkName](params);
         }

      };


   _requests.isLoggedIn([RIF.user]);

   return {
      logIn: _p
   };

});