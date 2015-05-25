RIF.mediator['firer-'] = (function () {


  var firer = {
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