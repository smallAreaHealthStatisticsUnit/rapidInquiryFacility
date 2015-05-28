RIF.menu['subscriber-frontSubmission'] = (function (controller) {

  var subscriber = {

    userLoggedIn: function () {
      controller.userLoggedIn();
    },

    updateHealthThemeAvailables: function () {},

    // This may need to be refactored to allow health theme rather than description as parameter   
    updateNumDenom: function (description) {
      controller.getNumDenom(description)
    },

    /*
    updateCovariates: function () {
        controller.getCovariates();
    },
    */

    showDialog: function (dialog) {
      controller.showDialog(dialog);
    },

    dialogBgChange: function (dialog) {
      controller.dialogBgChange(dialog);
    },

    logOut: function () {
      controller.logOut();
    }

  };

  return subscriber;
});