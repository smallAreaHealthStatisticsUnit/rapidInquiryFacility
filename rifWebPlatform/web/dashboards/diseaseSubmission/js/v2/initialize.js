/*
 *  The initialize file allows you to choose components and units as
 *  needed. The events object sets all relationships between 
 *  each component. 
 *
 *  @components
 *      list of components [map,chart,table,menu] dynamically initialized
 *  @units
 *      list of units which take care of the rendering for parts of the component they belong to 
 *  @events
 *      list of events occurring within the manager module
 *      ->firer:
 *          objects that will fire the specific event
 *      ->subscriber
 *          objects that will handle the event fired
 *      ->method
 *          method which must be implemented in subscriber object
 */
RIF.initialize = (function() {
   var _p = {
      components: {
         model: 'diseaseSubmission',
         /*map: {
            studyType: 'diseaseSubmission',
            layerType: 'tilesvg'
         },*/
         menu: {
            studyType: 'diseaseSubmission',
            menus: ['frontSubmission', 'investigationParameters', 'healthCodes' , 'areaSelection']
         },
         table: {
            studyType: 'diseaseSubmission',
            tables: [ 'ageGroups', 'investigationsRecap' ,'areaSelection']
         }
      },
      events: {
         /**--FRONT SUBMISSION MENU---**/
         studyNameChanged: {
            subscribers: ["model"],
            firer: ["menu"],
            method: "studyNameChanged"
         },
         healthThemeChanged: {
            subscribers: ["model"],
            firer: ["menu"],
            method: "healthThemeChanged"
         },
         numeratorChanged: {
            subscribers: ["model" /*, "table" */],
            firer: ["menu"],
            method: "numeratorChanged"
         },
         denominatorChanged: {
            subscribers: ["model"],
            firer: ["menu"],
            method: "denominatorChanged"
         },
         logOut: {
            subscribers: ["menu"],
            firer: ["menu"],
            method: "logOut"
         },
         isDialogReady: {
            subscribers: ["model"],
            firer: ["menu"],
            method: "isDialogReady"
         },
         showDialog: {
            subscribers: ["menu"],
            firer: ["model"],
            method: "showDialog"
         },
         
          
        /**-- Area Selection ---**/  
         
         selectAtChanged: {
            subscribers: ["model","table"],
            firer: ["menu"],
            method: "selectAtChanged"
         },
          
        studyAreaSelectionEvent: {
            subscribers: ["model"],
            firer: ["table"],
            method: "studyAreaSelectionEvent"
         },  
         
         startAreaSelection: {
            subscribers: ["menu"],
            firer: ["model"],
            method: "startAreaSelection"
         },  
          
         resolutionChanged: {
            subscribers: ["model"],
            firer: ["menu"],
            method: "resolutionChanged"
         },
          
         /**-- INVESTIGATION PARAMETER ---**/
         
         startInvestigationParameter: {
            subscribers: ["menu","table"],
            firer: ["model"],
            method: "startInvestigationParameter"
         },
         
         startYearChanged: {
            subscribers: ["model"],
            firer: ["menu"],
            method: "startYearChanged"
         }, 
          
        endYearChanged: {
            subscribers: ["model"],
            firer: ["menu"],
            method: "endYearChanged"
         },  
          
         genderChanged: {
            subscribers: ["model"],
            firer: ["menu"],
            method: "genderChanged"
         },
          
         covariatesChanged: {
            subscribers: ["model"],
            firer: ["menu"],
            method: "covariatesChanged"
         },
          
         isInvestigationReady: {
            subscribers: ["model"],
            firer: ["table", "menu"],
            method: "isInvestigationReady"
         }, 
          
         investigationReadyToBeAdded: {
            subscribers: ["menu"],
            firer: ["model"],
            method: "investigationReadyToBeAdded"
         },
          
        investigationNotReadyToBeAdded: {
            subscribers: ["menu"],
            firer: ["model"],
            method: "investigationNotReadyToBeAdded"
         },
          
        addInvestigation: {
            subscribers: ["model"],
            firer: ["menu"],
            method: "addInvestigation"
        },
          
        addInvestigationRow: {
            subscribers: ["table"],
            firer: ["model"],
            method: "addInvestigationRow"
         },
          
        removeInvestigationRow: {
            subscribers: ["model"],
            firer: ["table"],
            method: "removeInvestigationRow"
         },
          
        clearAllParameters: {
            subscribers: ["model", "table"],
            firer: ["menu"],
            method: "clearAllParameters"
         },  
          
        /**-- INVESTIGATION PARAMETER: Health Codes ---**/  
        healthSelectionChanged: {
            subscribers: ["model"],
            firer: ["menu"],
            method: "healthSelectionChanged"
         },
        
         /**-- INVESTIGATION PARAMETER: Age groups table ---**/    
         ageGroupsChanged: {
            subscribers: ["model"],
            firer: ["table"],
            method: "ageGroupsChanged"
         },  
          /*
         resolutionChanged: {
            subscribers: ["study"],
            firer: ["menu"],
            method: "resolutionChanged"
         },
         selectAtChanged: {
            subscribers: ["study"],
            firer: ["menu"],
            method: "selectAtChanged"
         },
         selectAtChangeUpdate: {
            subscribers: ["table"], //Table too later
            firer: ["study"],
            method: "selectAtChanged"
         },
         resolutiontChangeUpdate: {
            subscribers: ["menu"], //Table too later
            firer: ["study"],
            method: "resolutiontChangeUpdate"
         },
         studyAreaSelectionEvent: {
            subscribers: ["study"],
            firer: ["table"],
            method: "studyAreaSelectionEvent"
         },
         taxonomyChanged: {
            subscribers: ["study"],
            firer: ["menu"],
            method: "taxonomyChanged"
         },
         icdSelectionChanged: {
            subscribers: ["study"],
            firer: ["menu"],
            method: "icdSelectionChanged"
         },

         endYearChanged: {
            subscribers: ["study"],
            firer: ["menu"],
            method: "endYearChanged"
         },
         genderChanged: {
            subscribers: ["study"],
            firer: ["menu"],
            method: "genderChanged"
         },
         covariatesChanged: {
            subscribers: ["study"],
            firer: ["menu"],
            method: "covariatesChanged"
         },
         ageGroupsChanged: {
            subscribers: ["study"],
            firer: ["table"],
            method: "ageGroupsChanged"
         },

         investigationNotReadyToBeAdded: {
            subscribers: ["menu"],
            firer: ["study"],
            method: "investigationNotReadyToBeAdded"
         },
         investigationReadyToBeAdded: {
            subscribers: ["menu"],
            firer: ["study"],
            method: "investigationReadyToBeAdded"
         },
         covariatesChanged: {
            subscribers: ["study"],
            firer: ["menu"],
            method: "covariatesChanged"
         },
         addInvestigation: {
            subscribers: ["study"],
            firer: ["menu"],
            method: "addInvestigation"
         },
         addInvestigationRow: {
            subscribers: ["table"],
            firer: ["study"],
            method: "addInvestigationRow"
         },
         removeInvestigationRow: {
            subscribers: ["study"],
            firer: ["table"],
            method: "removeInvestigationRow"
         },
         clearAllParameters: {
            subscribers: ["study", "table"],
            firer: ["menu"],
            method: "clearAllParameters"
         },
         */
      },
      init: function() {
         RIF.dom();
         RIF.utils.initComponents.call(this);
         RIF.utils.addEvents.call(this);
      }
   };
   return {
      setUp: (function(args) {
         _p.init();
      }())
   };
});