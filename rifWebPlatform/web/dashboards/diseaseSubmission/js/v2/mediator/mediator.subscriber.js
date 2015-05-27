RIF.mediator['subscriber-'] = (function (mediatorUtils) {

  var _setProperty = mediatorUtils.setModelProperty;
  var _getProperty = mediatorUtils.getModelProperty;


  var subscriber = {
    //FRONT
    studyNameChanged: function (arg) {
      _setProperty('setStudyName', arg);
    },
    healthThemeChanged: function (arg) {
      _setProperty('setHealthTheme', arg);
    },
    numeratorChanged: function (arg) {
      _setProperty('setNumerator', arg);
    },
    denominatorChanged: function (arg) {
      _setProperty('setDenominator', arg);
    },
    //--- END OF FRONT  

    //---STUDY AREA 
    selectAtChanged: function (arg) {
      _setProperty('setStudyAreaSelectAt', arg);
      this.studyAreaSelectionEvent([]);
      this.studyMapAreaSelectionEvent([]);
      this.resolutionChanged([]);
      this.selectAtChangeUpdate(arg);
    },
    resolutionChanged: function (arg) {
      _setProperty('setStudyAreaResolution', arg);
    },
    studyAreaSelectionEvent: function (rows) {
      _setProperty('setStudyAreas', rows);
    },
    studyMapAreaSelectionEvent: function (rows) {
      _setProperty('setStudyMapAreas', rows);
    },
    clearAreaSelectionEvent: function (args) {
      var dialog = args[0];
      if (dialog == 'studyArea') {
        this.studyAreaSelectionEvent([]);
        this.studyMapAreaSelectionEvent([]);
      };
      this.clearStudySelection();
    },
    syncMapButtonClicked: function () {
      var tableSelection = _getProperty('getStudyAreas');
      var mappedData = mediatorUtils.tableToMap(tableSelection);
      this.studyMapAreaSelectionEvent(tableSelection); //Set map selection to be equal to table's  
      this.syncStudyAreaMap(mappedData);
    },
    syncTableButtonClicked: function () {
      var mapSelection = _getProperty('getStudyAreaFromMap');
      var mappedData = mediatorUtils.mapToTable(mapSelection);
      this.studyAreaSelectionEvent(mapSelection); //Set table selection to be equal to map's 
      this.syncStudyAreaTable(mappedData);
    },
    //---END OF STUDY AREA 

    //COMPARISON AREA 
    comparisonSelectAtChanged: function (arg) {
      _setProperty('setComparisonAreaSelectAt', arg);
      this.comparisonAreaSelectionEvent([]);
      this.comparisonMapAreaSelectionEvent([]);
      this.comparisonResolutionChanged([]);
      this.comparisonSelectAtChangeUpdate(arg);
    },
    comparisonResolutionChanged: function (arg) {
      _setProperty('setComparisonAreaResolution', arg);
    },
    comparisonMapAreaSelectionEvent: function (rows) {
      _setProperty('setComparisonMapAreas', rows);
    },
    comparisonAreaSelectionEvent: function (rows) {
      _setProperty('setComparisonAreas', rows);
    },
    comparisonSyncTableButtonClicked: function () {
      var mapSelection = _getProperty('getComparisonAreaFromMap');
      var mappedData = mediatorUtils.mapToTable(mapSelection);
      this.comparisonAreaSelectionEvent(mapSelection);
      this.syncComparisonAreaTable(mappedData);
    },
    comparisonSyncMapButtonClicked: function () {
      var tableSelection = _getProperty('getComparisonAreas');
      var mappedData = mediatorUtils.tableToMap(tableSelection);
      this.comparisonMapAreaSelectionEvent(tableSelection);
      this.syncComparisonAreaMap(mappedData);
    },
    comparisonClearAreaSelectionEvent: function (args) {
      this.comparisonAreaSelectionEvent([]);
      this.comparisonMapAreaSelectionEvent([]);
      this.comparisonClearStudySelection();
    },
    //----------------------------------------------  
    //----------------------------------------------  

    //INVESTIGATION PARAMETERS
    healthSelectionChanged: function (arg) {
      if (arg.length == 0) {
        arg = null;
      };
      _setProperty('setHealthOutcomes', arg);
    },
    startYearChanged: function (arg) {
      _setProperty('setMinYear', arg);
    },
    endYearChanged: function (arg) {
      _setProperty('setMaxYear', arg);
    },
    genderChanged: function (arg) {
      _setProperty('setGender', arg);
    },
    covariatesChanged: function (arg) {
      if (arg.length == 0) {
        arg = null;
      };
      _setProperty('setCovariates', arg);
    },
    ageGroupsChanged: function (arg) {
      if (arg.length <= 0) {
        arg = null;
      };
      _setProperty('setAgeGroups', arg);
    },
    projectChanged: function (arg) {
      _setProperty('setProject', arg);
    },
    calculationMethodsChanged: function (arg) {
      _setProperty('setCalculationMethods', arg);
    },
    studyDescriptionChanged: function (arg) {
      _setProperty('setDescription', arg);
    },
    isStudyReady: function () {
      mediatorUtils.isStudyReadyToBeSubmitted()
    },
    isInvestigationReady: function () {
      if (mediatorUtils.isInvestigationReadyToBeSubmitted()) {
        this.investigationReadyToBeAdded(); //firer
      } else {
        this.investigationNotReadyToBeAdded(); //firer
      }
    },
    addInvestigation: function () {
      if (this.investigationReady) {
        var nInvestigation = mediatorUtils.addCurrentInvestigation();
        this.addInvestigationRow(nInvestigation, _getProperty('getParameters'));
      }
    },
    removeInvestigationRow: function (arg) {
      mediatorUtils.removeInvestigation(arg);
    },
    clearAllParameters: function () {
      mediatorUtils.clearAllParameters()
    },

    mapModelToSchema: function () {
      var modelToSchema = mediatorUtils.mapToSchema();
      this.modelToSchemaReady(modelToSchema);
    },


    /*
     *  Check if dialog is ready to be opened
     */
    isDialogReady: function (dialog) {
      var ready = mediatorUtils.isDialogReady(dialog);
      var mapExtent = mediatorUtils.getMapExtentStatus();
      if (dialog == 'investigationDialog') {
        if (ready && this[dialog] != 1) {
          this.startInvestigationParameter(_getProperty('getNumerator')); //firer
        };
      } else if (dialog == 'studyAreaDialog') {
        if (ready && this[dialog] != 1) {
          this.startAreaSelection();
        }
      } else if (dialog == 'comparisonAreaDialog') {
        if (ready && this[dialog] != 1) { //refactor
          this.startComparisonAreaSelection();
        };
      } else if (dialog == 'statDialog') {
        if (ready && this[dialog] != 1) { //refactor
          this.startStatDialog();
        };
      }
      if (ready) {
        this.showDialog(dialog);
        this[dialog] = 1;
      };
    },


    /*
     *  Check if all parameters for the specific
     *  dialog have been selected
     */
    isDialogSelectionComplete: function (dialog) {
      var isReady = mediatorUtils.isDialogSelectionComplete(dialog);
      var previousState = {
        state: mediatorUtils.getDialogStatus(dialog)
      };
      if (previousState.state != isReady) {
        this.fire('dialogBgChange', dialog);
      };
      mediatorUtils.setDialogStatus(dialog, isReady);
    }

  };

  return subscriber;

});