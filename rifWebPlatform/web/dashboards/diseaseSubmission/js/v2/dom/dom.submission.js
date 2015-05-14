RIF.dom = (function() {
   var get = function(id) {
      return document.getElementById(id)
   },
   dom = {
      map: {
         studyArea: function() {
            return {
               id: 'areaSelectionMap'
            }
         },
         comparisonArea: function() {
            return {
               id: 'comparisonAreaSelectionMap'
            }
         }
      },
      menu: {
         frontSubmission: function() {
            return {
               studyName: $('#studyName'),
               /* Health theme drop down list */
               healthThemeAvailablesEl: $('#healthThemeAvailables'),
               /* Text input Health theme drop down list */
               healthTheme: $('#healthTheme'),
               /* Numerator input */
               numerator: $('#numerator'),
               /* Numerator  drop down list */
               numeratorAvailablesEl: get('numeratorAvailables'),
               /* numerator input */
               denominator: $('#denominator'),
               /* Denominator   drop down list */
               denominatorAvailablesEl: get('denominatorAvailables'),
               /* Import export   drop down list */
               importExportEl: get('importExport'),
               /* Run button */
               runEl: get('run'),
               /* Run f modal */
               runModal: get('runModal'),
               /* Run from file modal */
               runFromFileModal: get('runFromFileModal'),
               /* Study area front summary */
               fromFile: get('fromFile'),
               studyArea: get('studyArea'),
               /* Comparison area front summary */
               compArea: get('comparisonArea'),
               /* Investigation  front summary */
               invParameters: get('invParameters'),
               /* Investigation  front summary */
               stat: get('statsParameters'),
               /* Study area modal dialog  */
               studyAreaDialog: get('areaSelectionModal'),
               /* Comparison area modal dialog  */
               compAreaDialog: get('areaSelectionModal'),
               /* Investigation modal dialog  */
               investigationDialog: get('parametersModal'),
               /* Investigation modal dialog  */
               statDialog: get('statModal'),
               /* Retrieve modal dialog  */
               retrieveDialog: get('retrieveModal'),
               /* All Close button on modal dialogs */
               dialogClose: $('.modal_close'),
               /* Log out button */
               logOut: get('logOut'),
               /* User name*/
               userName: $('.userName'),
               done: $('.done a'),
               /* All custom made drop downs*/
               dropdownInputs: $('.dropdownInput:not(.editable)')
            };
         },
         areaSelection: function() {
            return {
               /* Resolution field */
               resolution: $('#resolution'),
               resolutionAvailable: $('#resolutionAvailable'),
               /* Resolution list */
               selectAtAvailable: $('#selectAtAvailable'),
               /* Select at field */
               selectAt: $('#selectAt'),
               /* Table listing area ids/name used for selection of study/comparison area */
               areaSelectionTable: $('#areaSelectionTable'),
               /* Map available for selection of study/comparison area */
               areaSelectionMap: $('#areaSelectionMap'),
               /* Label used to count number of areas selected */
               resolutionCountLabel: get('resolutionCountLabel')
            };
         },
         healthCodes: function() {
            return {
               /* Resolution field */
               icdClassification: $('#icdClassification'),
               icdHeader: $('.healthCodesHeader'),
               icdClassificationAvailable: ('#icdClassificationAvailable'),
               tree: get('tree'),
               healthCodesHeader: '.healthCodesHeader',
               healthCodesHeaderSpans: $('.healthCodesHeader span'),
               noChildElements: '.noChildElements',
               selection: function() {
                  return $('.icdSelected span')
               },
               icdSelection: 'icdSelected',
               clearAll: $('#clearParameters'),
               healthCodes: '.healthCodes',
               searchCodeInput: $('#searchCodeInput'),
               hiddenIcdSelection: $('#icdSelection'),
               searchResults: get('searchResults'),
               healthResults: get('healthResults')
            };
         },
         investigationParameters: function() {
            return {
               startYear: $('#startYear'),
               endYear: $('#endYear'),
               gender: $('#gender'),
               startYearAvailable: $('#startYearAvailable'),
               endYearAvailable: $('#endYearAvailable'),
               genderAvailable: $('#genderAvailable'),
               covariatesAvailable: $('.covariatesAvailable'),
               labels: function() {
                  return $('#covariatesWrapper label')
               },
               covariatesWrapper: $('#covariatesWrapper'),
               covariatesCheckbox: $('#covariatesWrapper input'),
               covariatesChecked: function() {
                  return $('#covariatesWrapper input:checked')
               },
               addInvestigation: $('#addInvestigation'),
               clearAll: $('#clearParameters'),
               inputBorderSelection: 'inputBorderSelection',
               labelSelected: 'labelSelected'
            };
         },

         retrievableRunnable: function() {
            return {
               summaryModal: ('#summaryModal'),
               openFileStudy: $('#openFileStudy'),
               setUpFile: $('#setUpFile'),
               finalRun: $('#finalRun'),
               project: $('#projectCode'),
               studyDescription: $('#studyDescription'),
               viewSummary: ('.viewSummary'),

            };
         },

         models: function() {
            return {
               modelsList: $('#modelsList'),
               statDone: $('#statDone')
            };
         }
      },
      table: {
         areaSelection: function() {
            return {
               headers: $('#areaSelectionWrapper .aSH li'),
               tableContent: get('allRows'),
               rows: $('#areaSelectionWrapper .aSR'),
               studyAreaCount: get('resolutionCount'),
               areaSelectionWrapper: $('#areaSelectionWrapper'),
               areasSelectionDialog: $('.areasSelectionDialog')
            };
         },
         ageGroups: function() {
            return {
               investigationBox: $('.investigationBox'),
               ageGroupsWrapper: $('#ageGroupsWrapper'),
               headers: $('#ageGroupsWrapper .aSH li'),
               tableContent: get('allRowsAgeGroups'),
               rows: $('#ageGroupsWrapper .aSR')
            };
         },
         investigationsRecap: function() {
            return {
               //headers: $('#areaSelectionWrapper .aSH li'),
               tableHead: $('#investigationRecap table thead'),
               tableBody: $('#investigationRecap table tbody'),
               tableContent: get('allRowsInvestigations'),
               rows: $('#investigationRecap .aSR'),
               removeInvestigation: '.removeInvestigation',
               container: $('#investigationRecap')
            };
         },
         summary: function() {
            return {
               studyName: $('#summaryStudyName'),
               project: $('#summaryProject'),
               healthTheme: $('#summaryHealthTheme'),
               numerator: $('#summaryNumerator'),
               denominator: $('#summaryDenominator'),
               studyArea: $('#summaryStudyArea'),
               comparisonArea: $('#summaryComparisonArea'),
               investigations: $('#summaryInvestigations'),
               stats: $('#summaryStats'),
               summaryStudy: $('.summaryStudy')
            };
         }

      }
   };
   RIF.dom = dom;
});