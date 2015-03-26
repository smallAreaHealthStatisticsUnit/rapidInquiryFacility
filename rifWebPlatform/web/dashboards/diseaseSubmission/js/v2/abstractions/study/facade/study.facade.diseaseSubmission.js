RIF.study['facade-diseaseSubmission'] = (function() {
   var facade = {
      //SuBSCRIBERS
      studyNameChanged: function(arg) {
         this.setStudyName(arg);
      },
      healthThemeChanged: function(arg) {
         this.setHealthTheme(arg);
      },
      numeratorChanged: function(arg) {
         this.setNumerator(arg);
      },
      denominatorChanged: function(arg) {
         this.setDenominator(arg);
      },
      selectAtChanged: function(arg) {
         this.setStudyAreaSelectAt(arg);
         //this.selectAtChangeUpdate(arg);
      },
      resolutionChanged: function(arg) {
         this.setStudyAreaResolution(arg);
      },
      studyAreaSelectionEvent: function(rows) {
         this.setStudyAreas(RIF.utils.unique(rows));
         console.log(this.getStudyAreas())
      },
      healthSelectionChanged: function(arg) {
         if (arg.length == 0) {
            arg = null;
         };
         this.setHealthOutcomes(arg);
      },
      startYearChanged: function(arg) {
         this.setMinYear(arg);
      },
      endYearChanged: function(arg) {
         this.setMaxYear(arg);
      },
      genderChanged: function(arg) {
         this.setGender(arg);
      },
      covariatesChanged: function(arg) {
         if (arg.length == 0) {
            arg = null;
         };
         this.setCovariates(arg);
      },
      ageGroupsChanged: function(arg) {
         if (arg.length <= 0) {
            arg = null;
         };
         this.setAgeGroups(arg);
      },
      isInvestigationReady: function() {
         for (var i in this.parameters) {
            if (i == 'healthOutcomes') {
               for (var h in this.parameters[i]) {
                  if (this.parameters[i][h].length == 0) {
                     delete this.parameters[i][h];
                  };
               };
               if (jQuery.isEmptyObject(this.parameters[i])) {
  
                  this.investigationNotReadyToBeAdded();
                  return;
               };
            } else if (i != 'covariates' && this.parameters[i] == null) {
               this.investigationNotReadyToBeAdded();
               return;
            }
         };
  
         this.investigationReadyToBeAdded();
      },
      addInvestigation: function() {
         if(this.investigationReady){  
            var nInvestigation = this.addCurrentInvestigation();
            this.fire('addInvestigationRow', [nInvestigation, this.parameters]);
         }
      },
      removeInvestigationRow: function(arg) {
         this.removeInvestigation(arg);
      },
      clearAllParameters: function() {
         for (var i in this.parameters) {
            this.parameters[i] = null;
         };
      },
      isDialogReady: function(dialog) {
         var ready = false;  
         if(dialog == 'investigationDialog') {
             ready = this.isInvestigationDialogReady();
         };
         if (ready) {
            if( this[dialog] != 1 ){
               this.startInvestigationParameter(this.front.numerator);  
            }; 
            this.showDialog(dialog);
            this[dialog] = 1;   
         };
        
      },
      // FIRERS  
      selectAtChangeUpdate: function(geolvl) {
         this.fire('selectAtChangeUpdate', geolvl);
      },
      investigationReadyToBeAdded: function() {
         this.investigationReady = true;    
         this.fire('investigationReadyToBeAdded', null);
      },
      investigationNotReadyToBeAdded: function() {
         this.investigationReady = false;  
         this.fire('investigationNotReadyToBeAdded', null);
      },
      showDialog: function(dialog) {
         this.fire('showDialog', dialog);
      },
      
      startInvestigationParameter: function(num){
        this.fire('startInvestigationParameter', num);
      }   
       
   };
   return facade;
});