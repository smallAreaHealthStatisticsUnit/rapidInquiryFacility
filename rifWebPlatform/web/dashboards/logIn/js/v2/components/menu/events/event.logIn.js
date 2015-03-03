RIF.menu['event-logIn'] = (function(dom) {

   var menuContext = this,

      attemptLogIn = function() {
         var username = dom.username.val(),
            pw = dom.password.val();

         menuContext.logIn.request('logIn', [username, pw]);
      };

   $('body').keydown(function() {
      if (event.keyCode == 13) {
         attemptLogIn();
      }
   });

   dom.logInBtn.click(function() {
      attemptLogIn();
   });

   dom.dialogClose.click(function() {
      RIF.statusBar(null, null, -1);

      var id = $(this).attr('href');
      $(id).hide();
   });


});