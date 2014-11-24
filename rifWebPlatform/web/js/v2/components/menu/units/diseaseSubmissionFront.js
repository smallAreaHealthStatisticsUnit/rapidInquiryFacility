RIF.menu.diseaseSubmissionFront = ( function() {

  var parent = this,

    /* DOM ELEMENTS */
    _domObjects = {
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


    /* EVENTS */
    _events = function() {

      $( _domObjects.studyArea ).click( function() {
        $( '#studyAreaModal' ).show();
      } );

      $( _domObjects.compArea ).click( function() {
        $( '#comparisonAreaModal' ).show();
      } );

      $( _domObjects.healthConds ).click( function() {
        $( '#healththemeModal' ).show();
      } );

      $( _domObjects.invParameters ).click( function() {
        $( '#parametersModal' ).show();
      } );

      $( _domObjects.healthThemeAvailablesEl ).children().click( function() {
        //Fire event Health theme changed
      } );

      $( _domObjects.numeratorAvailablesEl ).children().click( function() {
        //Fire event Numerator changed
      } );

      $( _domObjects.denominatorAvailablesEl ).children().click( function() {
        //Fire event Denominator changed
      } );


    },

    /* geolevel obj */
    _p = {

      getStudyName: function() {},
      getHealthThemes: function() {},
      getNumerator: function() {},
      getDenominator: function() {},
      getRetrievableStudies: function() {},

      getHealthThemesListDom: function() {
        return $( _domObjects.healthThemeAvailablesEl );
      },

      diseaseSubmissionFront: function() {
        _events();
      }



    };

  _p.diseaseSubmissionFront();

  return _p;
} );