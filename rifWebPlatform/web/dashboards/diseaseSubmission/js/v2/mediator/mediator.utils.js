RIF.mediator.utils = (function(modelAccessor) {


   var _dialogsStatus = {
      areaSelectionModal: false,
      comparisonArea: false,
      parametersModal: false,
      statModal: false
   };

   var _utils = {

      investigationReady: false,
      investigationCounts: 0,

      setModelProperty: function(fn, prop) {
         modelAccessor[fn](prop);
      },

      getModelProperty: function(fn) {
         return modelAccessor[fn]();
      },

      clearAllParameters: function() {
         for (var i in modelAccessor.getParameters()) {
            modelAccessor.setParameter(i, null);
         };
      },

      filterUniqueStudyAreas: function(currentSelection, newSelection) {
         var newLength = newSelection.length;
         while (newLength--) {
            var id = newSelection[newLength]['id'];
            //console.log("checking gid" + id);
            for (var k = 0, l = currentSelection.length; k < l; ++k) {
               if (currentSelection[k]['id'] == id) {
                  currentSelection.splice(k);
                  l--;
               };
            };
         };
         return currentSelection.concat(newSelection);
      },


      addCurrentInvestigation: function() {
         var parametersClone = RIF.utils.extend(modelAccessor.parameters, {});
         var invs = modelAccessor.getInvestigations();
         invs[_utils.investigationCounts] = parametersClone;
         return _utils.investigationCounts++;
      },

      removeInvestigation: function(i) {
         var invs = modelAccessor.getInvestigations();
         if (typeof invs[i] === 'object') {
            modelAccessor.unsetInvestigation(i);
            console.log('Investigation ' + i + ' removed')
         };
      },

      /* 
       * Model utils
       *
       */
      setDialogStatus: function(dialog, status) {
         _dialogsStatus[dialog] = status;
      },

      getDialogStatus: function(dialog) {
         return _dialogsStatus[dialog];
      },

      isReadyAndNotify: function(o) {
         if (jQuery.isEmptyObject(o)) {
            return false;
         };
         var toComplete = [],
            iterate = function(o) {
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

      isReady: function(o) {
         if (jQuery.isEmptyObject(o)) {
            return false;
         };
         var toComplete = [],
            iterate = function(o) {
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

      getFrontMenuVariables: function() {
         var front = {
            studyName: modelAccessor.getStudyName(),
            healthTheme: modelAccessor.getHealthTheme(),
            numerator: modelAccessor.getNumerator(),
            denominator: modelAccessor.getDenominator()
         };

         return front;
      },

      isOptional: function(p) {
         var optional = modelAccessor.getOptionals();
         var l = optional.length;
         while (l--) {
            if (p == optional[l]) {
               return true;
            };
         }
         return false;
      },

      mapAreaObjectProperties: function() {

      },

      displayMissingParameters: function(missing) {
         if (missing.length == 0) {
            return true;
         } else {
            var msg = 'Before continuing make sure the following parameters are set: <p> ' + missing.join(", ") + '</p>';
            RIF.statusBar(msg, true, 'notify');
            return false;
         };
      },

      isStudyReadyToBeSubmitted: function() {
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

      isInvestigationReadyToBeSubmitted: function() {
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

      isInvestigationDialogReady: function() {
         var front = {
            studyName: modelAccessor.getStudyName(),
            healthTheme: modelAccessor.getHealthTheme(),
            numerator: modelAccessor.getNumerator(),
            denominator: modelAccessor.getDenominator()
         };
         var ready = this.isReadyAndNotify(front);
         return ready;
      },
      isstudyAreaDialogReady: function() {},
      isComparisonAreaDialogReady: function() {},
      isStatDialogReady: function() {},


      /*
       * SELECTION COMPLETE CHECKS
       * The following methods are invoked  when a dialog is closed
       * Check if all parameters have been set for each dialog
       * Which then allows to singnal the completion of a specific dialog
       * And change of background image
       *
       */
      isStudyAreaSelectionComplete: function(dialog) {
         var r = this.isReady(modelAccessor.getStudyArea());
         this.setDialogStatus(dialog, r);
         return r;
      },
      isInvestigationSelectionComplete: function(dialog) {
         var ready = !jQuery.isEmptyObject(modelAccessor.getInvestigations());
         this.setDialogStatus(dialog, ready);
         return ready;
      },
      isComparisonAreaSelectionComplete: function() {},

      isStatSelectionComplete: function(dialog) {
         this.setDialogStatus(dialog, true); // hardcoded to true as the stat dialog is optional
         return true;
      },

      mapToSchema: function() {
         return modelAccessor.mapToSchema()
      }
   };

   return _utils;

});