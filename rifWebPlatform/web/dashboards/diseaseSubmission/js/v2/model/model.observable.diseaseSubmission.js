RIF.model['observable-diseaseSubmission'] = (function() {
   var observable = {
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
         var params = this.getParameters();  
         for (var i in params) {
            if (i == 'healthOutcomes') {
               for (var h in params[i]) {
                  if (params[i][h].length == 0) {
                     this.unsetParameter(i, h);
                  };
               };
               if (jQuery.isEmptyObject(params[i])) {
                  this.investigationNotReadyToBeAdded();
                  return;
               };
            } else if (i != 'covariates' && params[i] == null) {
               this.investigationNotReadyToBeAdded();
               return;
            }
         };
  
         this.investigationReadyToBeAdded();
      },
       
      addInvestigation: function() {
         if(this.investigationReady){  
            var nInvestigation = this.addCurrentInvestigation();
            this.fire('addInvestigationRow', [nInvestigation, this.getParameters()]);
         }
      },
       
      removeInvestigationRow: function(arg) {
         this.removeInvestigation(arg);
      },
       
      clearAllParameters: function() {
         for (var i in this.getParameters()) {
            this.setParameter(i, null);
         };
      },
       
      isDialogReady: function(dialog) {
         var ready = false;  
         if(dialog == 'investigationDialog') {
             ready = this.isInvestigationDialogReady();
             if( ready && this[dialog] != 1 ){
               this.startInvestigationParameter(this.getNumerator());  
            }; 
         }else if(dialog == 'areaSelection') {
             ready = true;
             if( ready && this[dialog] != 1 ){
                this.startAreaSelection();
             };
         };
          
         if (ready) {
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
      },
       
      startAreaSelection: function(){
        this.fire('startAreaSelection', null);
      }   
       
       
       
   };
   return observable;
});