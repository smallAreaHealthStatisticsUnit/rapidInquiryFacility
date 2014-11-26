window.onload = function( studyType ) {

  var menuUnits = {

    diseaseSubmissionFront: {
      healthTheme: document.getElementById( 'healthTheme' ),
      healthThemeAvailablesEl: document.getElementById( 'healthThemeAvailables' ),
      numeratorAvailablesEl: document.getElementById( 'numeratorAvailables' ),
      denominatorAvailablesEl: document.getElementById( 'denominatorAvailables' ),
      importExportEl: document.getElementById( 'importExport' ),
      runEl: document.getElementById( 'run' ),
      studyArea: document.getElementById( 'studyArea' ),
      compArea: document.getElementById( 'comparisonArea' ),
      healthConds: document.getElementById( 'healthConditions' ),
      invParameters: document.getElementById( 'invParameters' )
    },

    areaSelection: {
      resolutionAvailableEl: document.getElementById( 'resolutionAvailable' ),
      selectAtAvailableEl: document.getElementById( 'selectAtAvailable' ),
      tableEl: document.getElementById( 'areaSelectionTable' ),
      mapEl: document.getElementById( 'areaSelectionMap' ),
    }

  };




  if ( studyType == 'diseaseSubmission' ) {
    var dom = {
      diseaseSubmissionFront: menuUnits.diseaseSubmissionFront,
      areaSelection: menuUnits.areaSelection
    };
  };


  RIF.extend( {
    dom: dom
  }, RIF );

};