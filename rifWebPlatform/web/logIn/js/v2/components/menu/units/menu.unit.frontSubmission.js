RIF.menu.frontSubmission = ( function( _dom ) {

  var parent = this,

    _requests = {

      getHealthThemes: function() {
        RIF.getHealthThemes( function() {
          var themes = [ this[ 0 ].name ],
            description = [ this[ 0 ].description ],
            el = _dom[ 'healthThemeAvailablesEl' ];

          parent.dropDownInputText( themes, el );
          _requests.getNumDenom( description );
        }, null );
      },

      getNumDenom: function( desc ) {
        RIF.getNumeratorDenominator( function() {
          var num = [ this[ 0 ].numeratorTableName ],
            denom = [ this[ 0 ].denominatorTableName ];

          parent.dropDownInputText( num, _dom[ 'numeratorAvailablesEl' ] );
          parent.dropDownInputText( denom, _dom[ 'denominatorAvailablesEl' ] );
        }, desc );
      },

    },

    /* geolevel obj */
    _p = {

      initDiseaseSubmissionFront: function() {
        _requests.getHealthThemes();
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