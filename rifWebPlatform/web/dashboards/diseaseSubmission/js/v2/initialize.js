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
         mediator: '',
         map: {
            studyType: 'diseaseSubmission',
            layerType: 'tilesvg',
            maps: ['studyArea', 'comparisonArea']
         },
         menu: {
            studyType: 'diseaseSubmission',
            menus: ['frontSubmission', 'investigationParameters', 'healthCodes', 'areaSelection', 'retrievableRunnable', 'models']
         },
         table: {
            studyType: 'diseaseSubmission',
            tables: ['ageGroups', 'investigationsRecap', 'areaSelection', 'summary']
         }
      },
      events: {
         /**--FRONT SUBMISSION MENU---**/
         studyNameChanged: {
            subscribers: ["mediator"],
            firer: ["menu"],
            method: "studyNameChanged"
         },
         healthThemeChanged: {
            subscribers: ["mediator"],
            firer: ["menu"],
            method: "healthThemeChanged"
         },
         numeratorChanged: {
            subscribers: ["mediator" /*, "table" */ ],
            firer: ["menu"],
            method: "numeratorChanged"
         },
         denominatorChanged: {
            subscribers: ["mediator"],
            firer: ["menu"],
            method: "denominatorChanged"
         },
         logOut: {
            subscribers: ["menu"],
            firer: ["menu"],
            method: "logOut"
         },
         isDialogReady: {
            subscribers: ["mediator"],
            firer: ["menu"],
            method: "isDialogReady"
         },
         showDialog: {
            subscribers: ["menu"],
            firer: ["mediator"],
            method: "showDialog"
         },
         isDialogSelectionComplete: {
            subscribers: ["mediator"],
            firer: ["menu"],
            method: "isDialogSelectionComplete"
         },
         dialogBgChange: {
            subscribers: ["menu"],
            firer: ["mediator"],
            method: "dialogBgChange"
         },

         /**-- Run Dialog ---**/
         isStudyReady: {
            subscribers: ["mediator"],
            firer: ["menu"],
            method: "isStudyReady"
         },

         projectChanged: {
            subscribers: ["mediator"],
            firer: ["menu"],
            method: "projectChanged"
         },

         studyDescriptionChanged: {
            subscribers: ["mediator"],
            firer: ["menu"],
            method: "studyDescriptionChanged"
         },

         mapModelToSchema: {
            subscribers: ["mediator"],
            firer: ["menu"],
            method: "mapModelToSchema"
         },

         modelToSchemaReady: {
            subscribers: ["table"],
            firer: ["mediator"],
            method: "modelToSchemaReady"
         },

         /**-- Area Selection ---**/

         selectAtChanged: {
            subscribers: ["mediator" /*, "table", "map"*/ ],
            firer: ["menu"],
            method: "selectAtChanged"
         },

         selectAtChangeUpdate: {
            subscribers: ["table", "map"],
            firer: ["mediator"],
            method: "selectAtChangeUpdate"
         },

         studyAreaSelectionEvent: {
            subscribers: ["mediator"],
            firer: ["table"],
            method: "studyAreaSelectionEvent"
         },

         studyMapAreaSelectionEvent: {
            subscribers: ["mediator"],
            firer: ["map"],
            method: "studyMapAreaSelectionEvent"
         },

         startAreaSelection: {
            subscribers: ["menu"],
            firer: ["mediator"],
            method: "startAreaSelection"
         },

         resolutionChanged: {
            subscribers: ["mediator"],
            firer: ["menu"],
            method: "resolutionChanged"
         },

         /**-- INVESTIGATION PARAMETER ---**/

         startInvestigationParameter: {
            subscribers: ["menu", "table"],
            firer: ["mediator"],
            method: "startInvestigationParameter"
         },

         startYearChanged: {
            subscribers: ["mediator"],
            firer: ["menu"],
            method: "startYearChanged"
         },

         endYearChanged: {
            subscribers: ["mediator"],
            firer: ["menu"],
            method: "endYearChanged"
         },

         genderChanged: {
            subscribers: ["mediator"],
            firer: ["menu"],
            method: "genderChanged"
         },

         covariatesChanged: {
            subscribers: ["mediator"],
            firer: ["menu"],
            method: "covariatesChanged"
         },

         isInvestigationReady: {
            subscribers: ["mediator"],
            firer: ["table", "menu"],
            method: "isInvestigationReady"
         },

         investigationReadyToBeAdded: {
            subscribers: ["menu"],
            firer: ["mediator"],
            method: "investigationReadyToBeAdded"
         },

         investigationNotReadyToBeAdded: {
            subscribers: ["menu"],
            firer: ["mediator"],
            method: "investigationNotReadyToBeAdded"
         },

         addInvestigation: {
            subscribers: ["mediator"],
            firer: ["menu"],
            method: "addInvestigation"
         },

         addInvestigationRow: {
            subscribers: ["table"],
            firer: ["mediator"],
            method: "addInvestigationRow"
         },

         removeInvestigationRow: {
            subscribers: ["mediator"],
            firer: ["table"],
            method: "removeInvestigationRow"
         },

         clearAllParameters: {
            subscribers: ["mediator", "table"],
            firer: ["menu"],
            method: "clearAllParameters"
         },

         /**-- INVESTIGATION PARAMETER: Health Codes ---**/
         healthSelectionChanged: {
            subscribers: ["mediator"],
            firer: ["menu"],
            method: "healthSelectionChanged"
         },

         /**-- INVESTIGATION PARAMETER: Age groups table ---**/
         ageGroupsChanged: {
            subscribers: ["mediator"],
            firer: ["table"],
            method: "ageGroupsChanged"
         },

         /**-- RETRIEVE CALCULATION MODELS AVAILABLE ---**/
         startStatDialog: {
            subscribers: ["menu"],
            firer: ["mediator"],
            method: "startStatDialog"
         },

         calculationMethodsChanged: {
            subscribers: ["mediator"],
            firer: ["menu"],
            method: "calculationMethodsChanged"
         },

         /**-- MAPS ---**/
         syncStudyAreaButtonClicked: {
            subscribers: ["mediator"], // not going through mediator
            firer: ["menu"],
            method: "syncStudyAreaButtonClicked"
         },

         syncStudyAreaTable: {
            subscribers: ["table"], // not going through mediator
            firer: ["mediator"],
            method: "syncStudyAreaTable"
         },


         /* setInitialExtent: {
        subscribers: [ "map" ],
        firer: [ "mediator" ],
        method: "setInitialExtent"
      }, */


         /**-- SUMMARY TABLE ---**/

         //getModelObject: {
         //    subscribers: [ "mediator" ],
         //    firer: [ "menu" ],
         //    method: "getModelObject"   
         //},
         // showModelObject: {
         //   subscribers: [ "table" ],
         //   firer: [ "mediator" ],
         //   method: "getModelObject"
         //},  

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