/*
 *
 *  The initialize file allows you to choose components and units as
 *  needed. The events object sets all relationships between
 *  each component.
 *
 */
RIF.initialize = (function () {

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
        menus: ['frontSubmission', 'investigationParameters', 'healthCodes', 'areaSelection', 'comparisonArea', 'retrievableRunnable', 'models']
      },
      table: {
        studyType: 'diseaseSubmission',
        tables: ['ageGroups', 'investigationsRecap', 'studyArea', 'summary']
      }
    },
    events: {
      /*************************/
      /*************************/
      /**     FRONT MENU      **/
      /*************************/
      /*************************/
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



      /*************************/
      /*************************/
      /**     RUN DIALOG      **/
      /*************************/
      /*************************/
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

      /*************************/
      /*************************/
      /**     STUDY AREA      **/
      /*************************/
      /*************************/
      selectAtChanged: {
        subscribers: ["mediator"],
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

      // STUDY AREA - MAP
      syncMapButtonClicked: {
        subscribers: ["mediator"],
        firer: ["menu"],
        method: "syncMapButtonClicked"
      },

      syncTableButtonClicked: {
        subscribers: ["mediator"],
        firer: ["menu"],
        method: "syncTableButtonClicked"
      },

      syncStudyAreaTable: {
        subscribers: ["table"],
        firer: ["mediator"],
        method: "syncStudyAreaTable"
      },

      syncStudyAreaMap: {
        subscribers: ["map"],
        firer: ["mediator"],
        method: "syncStudyAreaMap"
      },

      // STUDY AREA - SELECT ALL ROWS FROM TABLE
      studySelectAllRowsClicked: {
        subscribers: ["table"],
        firer: ["menu"],
        method: "studySelectAllRowsClicked"
      },

      // STUDY AREA - CLEAR ALL SELECTION
      clearAreaSelectionEvent: {
        subscribers: ["mediator"],
        firer: ["menu"],
        method: "clearAreaSelectionEvent"
      },

      clearStudySelection: {
        subscribers: ["map", "table"],
        firer: ["mediator"],
        method: "clearStudySelection"
      },


      /*************************/
      /*************************/
      /**  COMPARISON AREA    **/
      /*************************/
      /*************************/

      comparisonSelectAtChanged: { //done
        subscribers: ["mediator"],
        firer: ["menu"],
        method: "comparisonSelectAtChanged"
      },

      comparisonResolutionChanged: { //done
        subscribers: ["mediator"],
        firer: ["menu"],
        method: "comparisonResolutionChanged"
      },

      comparisonMapAreaSelectionEvent: { //done
        subscribers: ["mediator"],
        firer: ["map"],
        method: "comparisonMapAreaSelectionEvent"
      },

      comparisonSyncTableButtonClicked: { //done
        subscribers: ["mediator"],
        firer: ["menu"],
        method: "comparisonSyncTableButtonClicked"
      },

      startComparisonAreaSelection: { //done
        subscribers: ["menu"],
        firer: ["mediator"],
        method: "startComparisonAreaSelection"
      },

      // COMPARISON AREA - MAP
      comparisonSyncMapButtonClicked: { //done
        subscribers: ["mediator"],
        firer: ["menu"],
        method: "comparisonSyncMapButtonClicked"
      },

      // STUDY AREA - CLEAR ALL SELECTION
      comparisonClearAreaSelectionEvent: { //done
        subscribers: ["mediator"],
        firer: ["menu"],
        method: "comparisonClearAreaSelectionEvent"
      },

      // COMPARISON AREA - SELECT ALL ROWS FROM TABLE
      comparisonSelectAllRowsClicked: {
        subscribers: ["table"],
        firer: ["menu"],
        method: "comparisonSelectAllRowsClicked"
      },

      comparisonSelectAtChangeUpdate: {
        subscribers: [ /*"table",*/ "map"],
        firer: ["mediator"],
        method: "comparisonSelectAtChangeUpdate"
      },

      /** Comparison TODO **/
      /*
      
        
      comparisonStudyAreaSelectionEvent: {
        subscribers: [ "mediator" ],
        firer: [ "table" ],
        method: "comparisonStudyAreaSelectionEvent"
      },
      
      
      comparisonSyncStudyAreaTable: {
        subscribers: [ "table" ], 
        firer: [ "mediator" ],
        method: "comparisonSyncStudyAreaTable"
      },
        
      comparisonSyncStudyAreaMap: {
        subscribers: [ "map" ], 
        firer: [ "mediator" ],
        method: "comparisonSyncStudyAreaMap"
      }, 
        
      comparisonClearStudySelection: {
        subscribers: [ "map", "table" ], 
        firer: [ "mediator" ],
        method: "comparisonClearStudySelection"
      }, 
      
      */


      /*************************/
      /*************************/
      /**   INVESTIGATIONS    **/
      /*************************/
      /*************************/

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
    init: function () {
      RIF.dom();
      RIF.utils.initComponents.call(this);
      RIF.utils.addEvents.call(this);
    }
  };

  return {
    setUp: (function (args) {
      _p.init();
    }())
  };

});