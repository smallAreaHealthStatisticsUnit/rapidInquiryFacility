RIF.menu.diseaseSubmissionFront = ( function() {

  var parent = this,

    _dom = RIF.dom.diseaseSubmissionFront,

    /* EVENTS */
    _events = function() {

      $( _dom.studyArea ).click( function() {
        $( '#studyAreaModal' ).show();
      } );

      $( _dom.compArea ).click( function() {
        $( '#comparisonAreaModal' ).show();
      } );

      $( _dom.healthConds ).click( function() {
        $( '#healththemeModal' ).show();
      } );

      $( _dom.invParameters ).click( function() {
        $( '#parametersModal' ).show();
      } );

      $( _dom.healthThemeAvailablesEl ).children().click( function() {
        //Fire event Health theme changed
      } );

      $( _dom.numeratorAvailablesEl ).children().click( function() {
        //Fire event Numerator changed
      } );

      $( _dom.denominatorAvailablesEl ).children().click( function() {
        //Fire event Denominator changed
      } );

    },


    _requests = {

      getHealthThemes: function() {

        var callback = function() {
          var themes = [ this[ 0 ].name ],
            el = _dom[ 'healthThemeAvailablesEl' ];
          parent.dropDownInputText( themes, el );
        };

        RIF.getHealthThemes( callback, null );
      },

      getNumDenom: function() {},


    },

    /* geolevel obj */
    _p = {

      initDiseaseSubmissionFront: function() {
        _requests.getHealthThemes();
        _events();
      },

      getStudyName: function() {},
      getHealthThemes: function() {},
      getNumerator: function() {},
      getDenominator: function() {},
      getRetrievableStudies: function() {},

      getDiseaseSubmissionFrontDom: function( obj ) {
        return $( _domObjects[ obj ] );
      }

    };

  _p.initDiseaseSubmissionFront();

  return _p;
} );