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
         study: 'diseaseSubmission',
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
            subscribers: ["study"],
            firer: ["menu"],
            method: "studyNameChanged"
         },
         healthThemeChanged: {
            subscribers: ["study"],
            firer: ["menu"],
            method: "healthThemeChanged"
         },
         numeratorChanged: {
            subscribers: ["study" /*, "table" */],
            firer: ["menu"],
            method: "numeratorChanged"
         },
         denominatorChanged: {
            subscribers: ["study"],
            firer: ["menu"],
            method: "denominatorChanged"
         },
         logOut: {
            subscribers: ["menu"],
            firer: ["menu"],
            method: "logOut"
         },
         isDialogReady: {
            subscribers: ["study"],
            firer: ["menu"],
            method: "isDialogReady"
         },
         showDialog: {
            subscribers: ["menu"],
            firer: ["study"],
            method: "showDialog"
         },
         
          
        /**-- Area Selection ---**/  
         
         selectAtChanged: {
            subscribers: ["study","table"],
            firer: ["menu"],
            method: "selectAtChanged"
         },
          
        studyAreaSelectionEvent: {
            subscribers: ["study"],
            firer: ["table"],
            method: "studyAreaSelectionEvent"
         },  
          
         /**-- INVESTIGATION PARAMETER ---**/
         
         startInvestigationParameter: {
            subscribers: ["menu","table"],
            firer: ["study"],
            method: "startInvestigationParameter"
         },
         
         startYearChanged: {
            subscribers: ["study"],
            firer: ["menu"],
            method: "startYearChanged"
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
          
         isInvestigationReady: {
            subscribers: ["study"],
            firer: ["table", "menu"],
            method: "isInvestigationReady"
         }, 
          
         investigationReadyToBeAdded: {
            subscribers: ["menu"],
            firer: ["study"],
            method: "investigationReadyToBeAdded"
         },
          
        investigationNotReadyToBeAdded: {
            subscribers: ["menu"],
            firer: ["study"],
            method: "investigationNotReadyToBeAdded"
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
          
        /**-- INVESTIGATION PARAMETER: Health Codes ---**/  
        healthSelectionChanged: {
            subscribers: ["study"],
            firer: ["menu"],
            method: "healthSelectionChanged"
         },
        
         /**-- INVESTIGATION PARAMETER: Age groups table ---**/    
         ageGroupsChanged: {
            subscribers: ["study"],
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