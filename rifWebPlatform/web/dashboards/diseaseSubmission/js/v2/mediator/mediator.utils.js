RIF.mediator.utils = (function (modelAccessor) {


  var _mapExtentSet = false;

  var _dialogsStatus = {
    areaSelectionModal: false,
    comparisonArea: false,
    parametersModal: false,
    statModal: false
  };

  var _utils = {

    investigationReady: false,
    investigationCounts: 0,

    setModelProperty: function (fn, prop) {
      modelAccessor[fn](prop);
    },

    getModelProperty: function (fn) {
      return modelAccessor[fn]();
    },

    clearAllParameters: function () {
      for (var i in modelAccessor.getParameters()) {
        modelAccessor.setParameter(i, null);
      };
    },

    /*filterUniqueStudyAreas: function (currentSelection, newSelection) {
      var newLength = newSelection.length;
      while (newLength--) {
        var id = newSelection[newLength]['id'];
        for (var k = 0, l = currentSelection.length; k < l; ++k) {
          if (currentSelection[k]['id'] == id) {
            currentSelection.splice(k);
            l--;
          };
        };
      };
      return currentSelection.concat(newSelection);
    },*/


    addCurrentInvestigation: function () {
      var parametersClone = RIF.utils.extend(modelAccessor.parameters, {});
      var invs = modelAccessor.getInvestigations();
      invs[_utils.investigationCounts] = parametersClone;
      return _utils.investigationCounts++;
    },

    removeInvestigation: function (i) {
      var invs = modelAccessor.getInvestigations();
      if (typeof invs[i] === 'object') {
        modelAccessor.unsetInvestigation(i);
        console.log('Investigation ' + i + ' removed')
      };
    },

    /* 
     * Used to map data uniformly
     * between table and map
     *
     */
    tableToMap: function (tableSelection) {
      var gids = [],
        area_ids = [],
        names = [];

      tableSelection.map(function (o) {
        gids.push(o.gid);
        area_ids.push(o.id);
        names.push(o.label);
      });

      return {
        gid: gids,
        area_id: area_ids,
        name: names
      };
    },
    mapToTable: function (tableSelection) {
      var gids = [],
        area_ids = [],
        names = [];

      tableSelection.map(function (o) {
        gids.push(o.gid);
        area_ids.push(o["area_id"]);
        names.push(o.label);
      });
      return {
        gid: gids,
        area_id: area_ids,
        name: names
      };
    },

    /* 
     * Check if Modal dialog is ready to be opened
     */
    isDialogReady: function (dialog) {
      switch (dialog) {
      case 'studyAreaDialog':
        return true;
      case 'comparisonAreaDialog':
        return true;
      case 'investigationDialog':
        return _utils.isInvestigationDialogReady();
      case 'statDialog':
        return true;
      default:
        return false;
      };
    },

    /* 
     * Check if Modal dialog is ready to be opened
     */
    isDialogSelectionComplete: function (dialog) {
      switch (dialog) {
      case 'areaSelectionModal':
        return _utils.isStudyAreaSelectionComplete(dialog);
      case 'comparisonAreaSelectionModal':
        return _utils.isComparisonAreaSelectionComplete(dialog);
      case 'parametersModal':
        return _utils.isInvestigationSelectionComplete(dialog);
      case 'statModal':
        return _utils.isStatSelectionComplete(dialog);
      default:
        return false;
      };
    },


    /* 
     * Model utils
     *
     */
    setDialogStatus: function (dialog, status) {
      _dialogsStatus[dialog] = status;
    },

    getDialogStatus: function (dialog) {
      return _dialogsStatus[dialog];
    },

    setMapExtentStatus: function (status) {
      _mapExtentSet = status;
    },

    getMapExtentStatus: function (dialog) {
      return _mapExtentSet;
    },

    isReadyAndNotify: function (o) {
      if (jQuery.isEmptyObject(o)) {
        return false;
      };
      var toComplete = [],
        iterate = function (o) {
          for (var i in o) {
            if (_utils.isOptional(i)) {
              continue;
            };
            if (o[i] == null || jQuery.isEmptyObject(o[i])) {
              toComplete.push(i);
            } else if (typeof o[i] == 'object') {
              iterate(o[i]);
            };
          };
        };
      iterate(o);
      return this.displayMissingParameters(toComplete);
    },

    isReady: function (o) {
      if (jQuery.isEmptyObject(o)) {
        return false;
      };
      var toComplete = [],
        iterate = function (o) {
          for (var i in o) {
            if (_utils.isOptional(i)) {
              continue;
            };
            if (o[i] == null || jQuery.isEmptyObject(o[i])) {
              toComplete.push(i);
              console.log(jQuery.isEmptyObject(o[i]));
            } else if (typeof o[i] == 'object') {
              iterate(o[i]);
            };
          };
        };
      iterate(o);
      return (toComplete.length > 0) ? false : true;
    },

    getFrontMenuVariables: function () {
      var front = {
        studyName: modelAccessor.getStudyName(),
        healthTheme: modelAccessor.getHealthTheme(),
        numerator: modelAccessor.getNumerator(),
        denominator: modelAccessor.getDenominator()
      };

      return front;
    },

    isOptional: function (p) {
      var optional = modelAccessor.getOptionals();
      var l = optional.length;
      while (l--) {
        if (p == optional[l]) {
          return true;
        };
      }
      return false;
    },

    mapAreaObjectProperties: function () {

    },

    displayMissingParameters: function (missing) {
      if (missing.length == 0) {
        return true;
      } else {
        var msg = 'Before continuing make sure the following parameters are set: <p> ' + missing.join(", ") + '</p>';
        RIF.statusBar(msg, true, 'notify');
        return false;
      };
    },

    isStudyReadyToBeSubmitted: function () {
      var mandatory = modelAccessor.getMandatoryVariablesNames()
      l = mandatory.length,
      toBeSet = [];
      while (l--) {
        var isSet = modelAccessor.get(mandatory[l]);
        if (isSet == null || jQuery.isEmptyObject(isSet)) {
          toBeSet.push(mandatory[l]);
        }
      };
      this.displayMissingParameters(toBeSet);
      return (toBeSet.length > 0) ? false : true;
    },

    isInvestigationReadyToBeSubmitted: function () {
      var params = modelAccessor.getParameters();
      for (var i in params) {
        if (i == 'healthOutcomes') {
          for (var h in params[i]) {
            if (params[i][h].length == 0) {
              modelAccessor.unsetParameter(i, h);
            };
          };
          if (jQuery.isEmptyObject(params[i])) {

            return false;
          };
        } else if (i != 'covariates' && params[i] == null) {
          return false;
        }
      };
      return true
    },

    /*
     * The following methods are invoked  when a tree is clicked
     * Some dialogs require certain parameter to be set before opening
     */

    isInvestigationDialogReady: function () {
      var front = {
        studyName: modelAccessor.getStudyName(),
        healthTheme: modelAccessor.getHealthTheme(),
        numerator: modelAccessor.getNumerator(),
        denominator: modelAccessor.getDenominator()
      };
      var ready = this.isReadyAndNotify(front);
      return ready;
    },
    isstudyAreaDialogReady: function () {},
    isComparisonAreaDialogReady: function () {},
    isStatDialogReady: function () {},


    /*
     * SELECTION COMPLETE CHECKS
     * The following methods are invoked  when a dialog is closed
     * Check if all parameters have been set for each dialog
     * Which then allows to singnal the completion of a specific dialog
     * And change of background image
     *
     */
    isStudyAreaSelectionComplete: function (dialog) {
      var studyArea = modelAccessor.getStudyArea();
      var r = this.isReady(studyArea);
      return r;
    },
    isInvestigationSelectionComplete: function (dialog) {
      var ready = !jQuery.isEmptyObject(modelAccessor.getInvestigations());
      return ready;
    },
    isComparisonAreaSelectionComplete: function (dialog) {
      var compArea = modelAccessor.getStudyArea();
      var r = this.isReady(compArea);
      return r;
    },

    isStatSelectionComplete: function (dialog) {
      return true;
    },

    mapToSchema: function () {
      return modelAccessor.mapToSchema()
    }
  };

  return _utils;

});