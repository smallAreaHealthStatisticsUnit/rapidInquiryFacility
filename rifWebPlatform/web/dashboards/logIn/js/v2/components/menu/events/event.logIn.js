RIF.menu['event-logIn'] = (function(dom, firer) {

   var menuContext = this,

      _attemptLogIn = function() {
         var username = dom.username.val(),
            pw = dom.password.val();

         RIF.utils.setUser(username);

         firer.fireLogIn([username, pw]);
      };

   $('body').keydown(function(e) {
      if (e.keyCode == 13) {
         _attemptLogIn();
      }
   });

   dom.logInBtn.click(function() {
      _attemptLogIn();
   });

   dom.dialogClose.click(function() {
      RIF.statusBar(null, null, -1);

      var id = $(this).attr('href');
      $(id).hide();
   });


});