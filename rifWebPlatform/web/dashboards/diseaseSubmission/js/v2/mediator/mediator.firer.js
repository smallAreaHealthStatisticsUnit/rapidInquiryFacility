RIF.mediator['firer-'] = (function () {


  var firer = {

    isLoggedIn: function () { //Called by Initializer
      if (RIF.user == null || RIF.user == "") {
        var msg = "Please <a href='../logIn/'>log in </a> first.";
        RIF.statusBar(msg, true, 1, true);
        return;
      };
      // Callback stores results in this object hence local copy is necessary to recreate a reference to the firer object
      var that = this;
      var clbk = function () {
        if (this[0]["result"] == "true") {
          firer.userLoggedIn.call(that);
        } else {
          var msg = "User:" + RIF.user + " is  not currently authenticated.<br/>" +
            "Please <a href='../logIn/'>log in </a>";
          RIF.statusBar(msg, true, 1, true);
        };
      };

      RIF.getIsLoggedIn(clbk, [RIF.user]);
    },

    userLoggedIn: function () {
      this.fire('userLoggedIn', []);
    },

    // FIRERS  
    selectAtChangeUpdate: function (geolvl) {
      this.fire('selectAtChangeUpdate', geolvl);
    },
    investigationReadyToBeAdded: function () {
      this.investigationReady = true;
      this.fire('investigationReadyToBeAdded', null);
    },
    investigationNotReadyToBeAdded: function () {
      this.investigationReady = false;
      this.fire('investigationNotReadyToBeAdded', null);
    },
    showDialog: function (dialog) {
      this.fire('showDialog', dialog);
    },

    startInvestigationParameter: function (num) {
      this.fire('startInvestigationParameter', num);
    },

    startAreaSelection: function () {
      this.fire('startAreaSelection', null);
    },

    startStatDialog: function g() {
      this.fire('startStatDialog', null);
    },

    addInvestigationRow: function (nInvestigation, parameters) {
      this.fire('addInvestigationRow', [nInvestigation, parameters]);
    },

    modelToSchemaReady: function (modelToSchema) {
      this.fire('modelToSchemaReady', [modelToSchema]);
    },

    setMapExtent: function () {
      this.fire('setMapExtent', []);
    },

    syncStudyAreaTable: function (mapAreas) {
      this.fire('syncStudyAreaTable', mapAreas);
    },

    syncStudyAreaMap: function (gids) {
      this.fire('syncStudyAreaMap', gids);
    },

    clearStudySelection: function () {
      this.fire('clearStudySelection', []);
    },

    //Comparison Area  
    comparisonSelectAtChangeUpdate: function (geolvl) {
      this.fire('comparisonSelectAtChangeUpdate', geolvl);
    },
    startComparisonAreaSelection: function () {
      this.fire('startComparisonAreaSelection', null);
    },

    /*syncComparisonAreaTable: function (mapAreas) {
      this.fire('syncStudyAreaTable', mapAreas);
    },

    syncComparisonAreaMap: function (gids) {
      this.fire('syncStudyAreaMap', gids);
    },*/


  };

  return firer;
});