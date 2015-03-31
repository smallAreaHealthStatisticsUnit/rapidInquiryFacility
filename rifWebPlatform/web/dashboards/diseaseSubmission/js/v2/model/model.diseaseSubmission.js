RIF.model = (function(type) {
   
    var _study = {
         
         studyName: null,
         healthTheme: null,
         numerator: null,
         denominator: null,
        
         investigations: {},
        
         studyArea: {
            resolution: null,
            areas: [],
            selectAt: null
         },
         comparisonArea: {
            resolution: null,
            areas: [],
            selectAt: null
         }
     },
       _studyMethods = {
       
         investigationReady: false,  
         investigationCounts: 0,
         
         parameters: {
            healthOutcomes: null,
            ageGroups: null,
            minYear: null,
            maxYear: null,
            gender: null,
            covariates: null,
         },   
           
         showInvestigations: function() {
            for (var l in _study.investigations) {
               for (var i in _study.investigations[l]) {
                  console.log(_study.investigations[l][i]);
               };
               console.log('_____');
            }
            console.log('----------------------');
            console.log('----------------------');
            console.log('----------------------');
         },
         addCurrentInvestigation: function() {
            var parametersClone = RIF.utils.extend(this.parameters, {});
            _study.investigations[this.investigationCounts] = parametersClone;
            console.log(JSON.stringify(_study));
            // this.showInvestigations();  
            return this.investigationCounts++;
         },
         removeInvestigation: function(i) {
            if (typeof _study.investigations[i] === 'object') {
               delete _study.investigations[i];
               console.log('Investigation ' + i + ' removed')
            };
            // this.showInvestigations();  
         },
         //SETTERS
         setStudyName: function(s) {
            _study.studyName = s;
         },
         setHealthTheme: function(s) {
            _study.healthTheme = s;
         },
         setNumerator: function(s) {
            _study.numerator = s;
         },
         setDenominator: function(s) {
            _study.denominator = s;
         },
         setStudyAreaSelectAt: function(s) {
            _study.studyArea.selectAt = s;
         },
         setStudyAreaResolution: function(s) {
            _study.studyArea.resolution = s;
         },
         setStudyAreas: function(s) {
            _study.studyArea.areas = s;
         },
         setComparisonArea: function(s) {
            _study.comparisonArea.resolution = s.resolution;
            _study.comparisonArea.areas = s.areas;
            _study.comparisonArea.selectAt = s.selectAt;
         },
         setHealthConditionTaxonomy: function(s) {
            this.parameters.taxonomy = s;
         },
         setHealthOutcomes: function(s) {
            this.parameters.healthOutcomes = s;
         },
         setMinYear: function(s) {
            this.parameters.minYear = s;
         },
         setMaxYear: function(s) {
            this.parameters.maxYear = s;
         },
         setGender: function(s) {
            this.parameters.gender = s;
         },
         setCovariates: function(s) {
            this.parameters.covariates = s;
         },
         setAgeGroups: function(s) {
            this.parameters.ageGroups = s;
         },
         setParameter: function(param, s ){
            this.parameters[param] = s;
         },   
         //GETTERS
         getInvestigations: function() {
            return _study.investigations;
         },
         getStudyName: function() {
            return _study.studyName;
         },
         getHealthTheme: function() {
            return _study.healthTheme;
         },
         getNumerator: function() {
            return _study.numerator;
         },
         getDenominator: function() {
            return _study.denominator;
         },
         getStudyAreas: function() {
            return _study.studyArea.areas;;
         },
         getComparisonArea: function() {
            return _study.comparisonArea;
         },
         getHealthConditionTaxonomy: function() {
            return this.parameters.taxonomy;
         },
         getHealthOutcomes: function() {
            return this.parameters.healthOutcomes;
         },
         getMinYear: function() {
            return this.parameters.minYear;
         },
         getMaxYear: function() {
            return this.parameters.maxYear;
         },
         getGender: function() {
            return this.parameters.gender;
         },
         getCovariates: function() {
            return this.parameters.covariates;
         },
         getAgeGroups: function() {
            return this.parameters.ageGroups;
         },
         getParameters: function(){
            return this.parameters;
         },   
         
           
         unsetParameter: function(i,h){
            delete this.parameters[i][h];
         },
           
         isFrontSubmissionReady: function() {
            var front = {
                studyName:_study.studyName,
                healthTheme:_study.healthTheme,
                numerator:_study.numerator,
                denominator:_study.denominator
            };  
            var toComplete = [];
            for (var i in front) {
               if (front[i] == null) {
                  toComplete.push(i);
               }
            };
            if (toComplete.length == 0) {
               return true;
            } else {
               var msg = 'Before continuing make sure the following parameters are not empty: <br/> ' + toComplete.join(", ");
               RIF.statusBar(msg, true, 'notify');
               return false;
            };
         },
          
           
         isComparisonAreaDialogReady: function() {},
         isInvestigationDialogReady: function() {
            var ready = this.isFrontSubmissionReady();
            return ready;
            //this.isStudyAreaDialogReady();
         }
   };
   return RIF.utils.mix(_studyMethods, RIF.model['observable-diseaseSubmission']());
});