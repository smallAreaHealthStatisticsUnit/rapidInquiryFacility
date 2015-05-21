RIF.mediator['subscriber-'] = (function(mediatorUtils) {

   var _setProperty = mediatorUtils.setModelProperty;
   var _getProperty = mediatorUtils.getModelProperty;


   var subscriber = {
      //SUBSCRIBERS
      studyNameChanged: function(arg) {
         _setProperty('setStudyName', arg);
      },
      healthThemeChanged: function(arg) {
         _setProperty('setHealthTheme', arg);
      },
      numeratorChanged: function(arg) {
         _setProperty('setNumerator', arg);
      },
      denominatorChanged: function(arg) {
         _setProperty('setDenominator', arg);
      },
      selectAtChanged: function(arg) {
         _setProperty('setStudyAreaSelectAt', arg);
         this.studyAreaSelectionEvent([]);
         this.studyMapAreaSelectionEvent([])
         this.selectAtChangeUpdate(arg);
      },
      resolutionChanged: function(arg) {
         _setProperty('setStudyAreaResolution', arg);
      },
      studyAreaSelectionEvent: function(rows) {
         _setProperty('setStudyAreas', rows);
      },
      studyMapAreaSelectionEvent: function(rows) {
         _setProperty('setStudyMapAreas', rows);
      },
      healthSelectionChanged: function(arg) {
         if (arg.length == 0) {
            arg = null;
         };
         _setProperty('setHealthOutcomes', arg);
      },
      startYearChanged: function(arg) {
         _setProperty('setMinYear', arg);
      },
      endYearChanged: function(arg) {
         _setProperty('setMaxYear', arg);
      },
      genderChanged: function(arg) {
         _setProperty('setGender', arg);
      },
      covariatesChanged: function(arg) {
         if (arg.length == 0) {
            arg = null;
         };
         _setProperty('setCovariates', arg);
      },
      ageGroupsChanged: function(arg) {
         if (arg.length <= 0) {
            arg = null;
         };
         _setProperty('setAgeGroups', arg);
      },
      projectChanged: function(arg) {
         _setProperty('setProject', arg);
      },
      calculationMethodsChanged: function(arg) {
         _setProperty('setCalculationMethods', arg);
      },
      studyDescriptionChanged: function(arg) {
         _setProperty('setDescription', arg);
      },
      isStudyReady: function() {
         mediatorUtils.isStudyReadyToBeSubmitted()
      },
      isInvestigationReady: function() {
         if (mediatorUtils.isInvestigationReadyToBeSubmitted()) {
            this.investigationReadyToBeAdded(); //firer
         } else {
            this.investigationNotReadyToBeAdded(); //firer
         }
      },
      addInvestigation: function() {
         if (this.investigationReady) {
            var nInvestigation = mediatorUtils.addCurrentInvestigation();
            this.addInvestigationRow(nInvestigation, _getProperty('getParameters'));
         }
      },
      removeInvestigationRow: function(arg) {
         mediatorUtils.removeInvestigation(arg);
      },
      clearAllParameters: function() {
         mediatorUtils.clearAllParameters()
      },

      clearAreaSelectionEvent: function(args) {
         var dialog = args[0];
         if (dialog == 'studyArea') {
            this.studyAreaSelectionEvent([]);
            this.studyMapAreaSelectionEvent([]);
         };
         this.clearStudySelection();
      },

      mapModelToSchema: function() {
         var modelToSchema = mediatorUtils.mapToSchema();
         this.modelToSchemaReady(modelToSchema);
      },

      syncMapButtonClicked: function() {
         /* Set map selection to be equal table's */
         var tableSelection = _getProperty('getStudyAreas');
         this.studyMapAreaSelectionEvent(tableSelection);

         var gids = [],
            area_ids = [],
            names = [];
         var selection = tableSelection.map(function(o) {
            gids.push(o.gid);
            area_ids.push(o.id);
            names.push(o.label);
         });
         this.syncStudyAreaMap({
            gid: gids,
            area_id: area_ids,
            name: names
         });
      },

      syncTableButtonClicked: function() {
         /* Set table selection to be equal map's */
         var mapSelection = _getProperty('getStudyAreaFromMap');
         this.studyAreaSelectionEvent(mapSelection);

         var gids = [],
            area_ids = [],
            names = [];
         mapSelection.map(function(o) {
            gids.push(o.gid);
            area_ids.push(o["area_id"]);
            names.push(o.label);
         });
         this.syncStudyAreaTable({
            gid: gids,
            area_id: area_ids,
            name: names
         });
      },


      /*
       *  Check if dialog is ready to be opened
       */
      isDialogReady: function(dialog) {
         var ready = false;
         if (dialog == 'investigationDialog') {
            ready = mediatorUtils.isInvestigationDialogReady();
            if (ready && this[dialog] != 1) {
               this.startInvestigationParameter(_getProperty('getNumerator')); //firer
            };
         } else if (dialog == 'studyAreaDialog') {
            ready = true;
            if (ready && this[dialog] != 1) {
               this.startAreaSelection(); //firer
            };
         } else if (dialog == 'statDialog') {
            ready = true;
            if (ready && this[dialog] != 1) {
               this.startStatDialog(); //firer
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
      isDialogSelectionComplete: function(dialog) {
         var previousState = {
            state: mediatorUtils.getDialogStatus(dialog)
         };
         var ready;
         if (dialog == 'parametersModal') {
            ready = mediatorUtils.isInvestigationSelectionComplete(dialog);
         } else if (dialog == 'areaSelectionModal') {
            ready = mediatorUtils.isStudyAreaSelectionComplete(dialog);
         } else if (dialog == 'statModal') {
            ready = mediatorUtils.isStatSelectionComplete(dialog);
         }

         if (previousState.state != ready) {
            this.fire('dialogBgChange', dialog);
         };
      }

   };

   return subscriber;

});