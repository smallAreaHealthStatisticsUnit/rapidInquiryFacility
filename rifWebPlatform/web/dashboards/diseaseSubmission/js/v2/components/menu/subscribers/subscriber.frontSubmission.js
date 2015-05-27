RIF.menu['subscriber-frontSubmission'] = (function (controller) {

  var subscriber = {

    userLoggedIn: function () {
      controller.userLoggedIn();
    },

    updateHealthThemeAvailables: function () {},
    updateNumeratorAvailables: function () {},
    updateDenominatorAvailables: function () {},

    /*updateCovariates: function () {
        controller.getCovariates();
    },*/

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