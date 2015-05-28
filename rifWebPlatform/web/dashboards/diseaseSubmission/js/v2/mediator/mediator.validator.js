RIF.mediator.validator = (function (modelAccessor) {


  var _dialogsStatus = {
    areaSelectionModal: false,
    comparisonAreaSelectionModal: false,
    parametersModal: false,
    statModal: false
  };

  var _dialogsComplete = {
    "Study area": false,
    "Comparison area": false,
    "Investigation parameters": false,
    "Statistical parameters": false,
  };


  this.setDialogStatus = function (dialog, status) {
    _dialogsStatus[dialog] = status;
  };

  this.getDialogStatus = function (dialog) {
    return _dialogsStatus[dialog];
  };

  this.isStudyReadyToBeSubmitted = function () {
    var mandatory = modelAccessor.getMandatoryFrontVariables(),
      toBeSet = [];
    for (var l in mandatory) {
      var isSet = mandatory[l];
      if (isSet == null || jQuery.isEmptyObject(isSet)) {
        toBeSet.push(l);
      }
    };
    for (var i in _dialogsComplete) {
      if (!_dialogsComplete[i]) {
        toBeSet.push(i);
      };
    };
    this.displayMissingParameters(toBeSet);
    return (toBeSet.length > 0) ? false : true;
  };

  this.isDialogReady = function (dialog) {
    switch (dialog) {
    case 'studyAreaDialog':
      return true;
    case 'comparisonAreaDialog':
      return true;
    case 'investigationDialog':
      return this.isInvestigationDialogReady();
    case 'statDialog':
      return true;
    default:
      return false;
    };
  };

  /* 
   * Check if Modal dialog is ready to be opened
   */
  this.isDialogSelectionComplete = function (dialog) {
    switch (dialog) {
    case 'areaSelectionModal':
      var isComplete = this.isStudyAreaSelectionComplete(dialog);
      _dialogsComplete['Study area'] = isComplete;
      return isComplete;
    case 'comparisonAreaSelectionModal':
      var isComplete = this.isComparisonAreaSelectionComplete(dialog);
      _dialogsComplete['Comparison area'] = isComplete;
      return isComplete;
    case 'parametersModal':
      var isComplete = this.isInvestigationSelectionComplete(dialog);
      _dialogsComplete['Investigation parameters'] = isComplete;
      return isComplete;
    case 'statModal':
      var isComplete = this.isStatSelectionComplete(dialog);
      _dialogsComplete['Statistical parameters'] = isComplete;
      return isComplete;
    default:
      return false;
    };
  };


  this.isOptional = function (p) {
    var optional = modelAccessor.getOptionals();
    var l = optional.length;
    while (l--) {
      if (p == optional[l]) {
        return true;
      };
    }
    return false;
  };

  this.isInvestigationReadyToBeSubmitted = function () {
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
      } else if (i == 'ageGroups' && params[i] == null) {
        return false;
      } else if (i != 'covariates' && params[i] == null) {
        return false;
      }
    };
    return true
  };

  /*
   * The following methods are invoked  when a tree is clicked
   * Some dialogs require certain parameter to be set before opening
   */

  this.isInvestigationDialogReady = function () {
    var front = {
      studyName: modelAccessor.getStudyName(),
      healthTheme: modelAccessor.getHealthTheme(),
      numerator: modelAccessor.getNumerator(),
      denominator: modelAccessor.getDenominator(),
      studyArea: modelAccessor.getStudyArea()
    };
    var ready = this.isReadyAndNotify(front);
    return ready;
  };
  this.isstudyAreaDialogReady = function () {};
  this.isComparisonAreaDialogReady = function () {};
  this.isStatDialogReady = function () {};


  /*
   * SELECTION COMPLETE CHECKS
   * The following methods are invoked  when a dialog is closed
   * Check if all parameters have been set for each dialog
   * Which then allows to singnal the completion of a specific dialog
   * And change of background image
   *
   */
  this.isStudyAreaSelectionComplete = function (dialog) {
    var tableGIDs = this.getGIDs(modelAccessor.getStudyAreas()),
      mapGIDs = this.getGIDs(modelAccessor.getStudyAreaFromMap());

    var areAreasSelected = (tableGIDs.length > 0 || mapGIDs.length > 0);
    var areAreasEqual = this.areEqual(tableGIDs, mapGIDs);

    var isResolutionReady = this.isReady(modelAccessor.getStudyAreaResolution());
    var isSelectAtReady = this.isReady(modelAccessor.getStudyAreaSelectAt());
    var almostReady = isResolutionReady * isSelectAtReady * areAreasSelected;

    if (almostReady && !areAreasEqual) {
      this.syncMapTableNotification(dialog);
    };

    return almostReady * areAreasEqual;
  };

  this.isComparisonAreaSelectionComplete = function (dialog) {
    var tableGIDs = this.getGIDs(modelAccessor.getComparisonAreas()),
      mapGIDs = this.getGIDs(modelAccessor.getComparisonAreaFromMap());

    var areAreasSelected = (tableGIDs.length > 0 || mapGIDs.length > 0);
    var areAreasEqual = this.areEqual(tableGIDs, mapGIDs);

    var isResolutionReady = this.isReady(modelAccessor.getComparisonAreaResolution());
    var isSelectAtReady = this.isReady(modelAccessor.getComparisonAreaAreaSelectAt());

    var almostReady = isResolutionReady * isSelectAtReady * areAreasSelected;
    if (almostReady && !areAreasEqual) {
      this.syncMapTableNotification(dialog);
    };

    return almostReady * areAreasEqual;
  };

  this.isInvestigationSelectionComplete = function (dialog) {
    var ready = !jQuery.isEmptyObject(modelAccessor.getInvestigations());
    return ready;
  };

  this.isStatSelectionComplete = function (dialog) {
    return true;
  };


});