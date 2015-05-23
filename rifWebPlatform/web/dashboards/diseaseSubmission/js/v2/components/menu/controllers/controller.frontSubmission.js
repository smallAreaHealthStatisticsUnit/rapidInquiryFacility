RIF.menu['controller-frontSubmission'] = (function (unit) {

  var _p = {

    isLoggedIn: function () {
      var clbk = function () {
        if (this[0]["result"] == "true") {
          unit.writeUserName(RIF.user);
          _p.getHealthThemes();
          _p.setGlobalMapFullExtent();
        } else {

          var msg = "User:" + RIF.user + " is  not currently authenticated.<br/>" +
            "Please <a href='../logIn/'>log in </a>";

          RIF.statusBar(msg, true, 1, true);
        };
      };
      RIF.getIsLoggedIn(clbk, [RIF.user]);
    },

    //Create a global variable to the RIF object - with time it could be refactored 
    setGlobalMapFullExtent: function () {
      var callback = function () {
        RIF.mapExtent = this[0];
      };
      RIF.getFullExtent(callback, []);
    },

    getHealthThemes: function () {
      var clbk = function () {
        var themes = [this[0].name],
          description = [this[0].description];
        unit.getHealthThemes(themes, description);
        _p.getNumDenom(description);

      };

      RIF.getHealthThemes(clbk, null);
    },
    getNumDenom: function (desc) {
      var clbk = function () {
        var num = [this[0].numeratorTableName],
          denom = [this[0].denominatorTableName];

        unit.getNumDenom(num, denom);
      };
      RIF.getNumeratorDenominator(clbk, desc);
    },
    logOut: function () {
      var clbk = function () {
        RIF.utils.redirect("../logIn/?rd=diseaseSubmission");
        RIF.statusBar(' You are now logged out', null, 'notify');
      };

      RIF.getLogOut(clbk, [RIF.user]);
    },

    showDialog: function (dialog) {
      unit.showDialog(dialog);
    },

    dialogBgChange: function (dialog) {
      unit.dialogBgChange(dialog);
    },

  };


  if (RIF.user == null || RIF.user == "") {
    var msg = "Please <a href='../logIn/'>log in </a> first.";
    RIF.statusBar(msg, true, 1, true);
  } else {
    _p.isLoggedIn();
  };

  return _p;

});