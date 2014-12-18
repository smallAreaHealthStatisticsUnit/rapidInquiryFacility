RIF.dom = ( function() {


  var get = function( id ) {
    return document.getElementById( id )
  },

  dom = {
    menu: {
      frontSubmission: function() {
        return {
          studyName: $( '#studyName' ),
          /* Health theme drop down list */
          healthThemeAvailablesEl: $( '#healthThemeAvailables' ),
          /* Text input Health theme drop down list */
          healthTheme: $( '#healthTheme' ),
          /* Numerator input */
          numerator: $( '#numerator' ),
          /* Numerator  drop down list */
          numeratorAvailablesEl: get( 'numeratorAvailables' ),
          /* numerator input */
          denominator: $( '#denominator' ),
          /* Denominator   drop down list */
          denominatorAvailablesEl: get( 'denominatorAvailables' ),
          /* Import export   drop down list */
          importExportEl: get( 'importExport' ),
          /* Run button */
          runEl: get( 'run' ),
          /* Study area front summary */
          studyArea: get( 'studyArea' ),
          /* Comparison area front summary */
          compArea: get( 'comparisonArea' ),
          /* Investigation  front summary */
          invParameters: get( 'invParameters' ),
          /* Study area modal dialog  */
          studyAreaDialog: get( 'areaSelectionModal' ),
          /* Comparison area modal dialog  */
          compAreaDialog: get( 'areaSelectionModal' ),
          /* Investigation modal dialog  */
          investigationDialog: get( 'parametersModal' ),
          /* All Close button on modal dialogs */
          dialogClose: $( '.modal_close' ),
          /* Log out button */
          logOut: get( 'logOut' ),
          /* All custom made drop downs*/
          dropdownInputs: $( '.dropdownInput:not(.editable)' )
        };
      },
      studyArea: function() {
        return {
          /* Resolution field */
          resolution: $( '#resolution' ),

          resolutionAvailable: $( '#resolutionAvailable' ),
          /* Resolution list */
          selectAtAvailable: $( '#selectAtAvailable' ),
          /* Select at field */
          selectAt: $( '#selectAt' ),
          /* Table listing area ids/name used for selection of study/comparison area */
          areaSelectionTable: $( '#areaSelectionTable' ),
          /* Map available for selection of study/comparison area */
          areaSelectionMap: $( '#areaSelectionMap' )
        };
      },
      healthCodes: function() {
        return {
          /* Resolution field */
          icdClassification: $( '#icdClassification' ),
          icdHeader: $( '.healthCodesHeader' ),
          icdClassificationAvailable: ( '#icdClassificationAvailable' ),
          tree: get( 'tree' ),
          healthCodesHeader: $( 'div.healthCodesHeader' )
        };
      }

    },

    table: {
      areaSelectionRenderer: function() {
        return {
          headers: $( '.aSH li' ),
          tableContent: get( 'allRows' ),
          rows: $( '.aSR' )
        };
      }

    }
  };


  RIF.dom = dom;

} );