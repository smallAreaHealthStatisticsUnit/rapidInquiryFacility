RIF.dom = ( function() {


  var get = function( id ) {
    return document.getElementById( id )
  },

  dom = {
    menu: {
      frontSubmission: {
        /* Health theme drop down list */
        healthThemeAvailablesEl: get( 'healthThemeAvailables' ),
        /* Numerator  drop down list */
        numeratorAvailablesEl: get( 'numeratorAvailables' ),
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
      },
    },
  };


  RIF.dom = dom;

} );