RIF.mediator[ 'subscriber-' ] = ( function ( mediatorUtils ) {

  var _setProperty = mediatorUtils.setModelProperty;
  var _getProperty = mediatorUtils.getModelProperty;


  var subscriber = {
    //SUBSCRIBERS
    studyNameChanged: function ( arg ) {
      _setProperty( 'setStudyName', arg );
    },
    healthThemeChanged: function ( arg ) {
      _setProperty( 'setHealthTheme', arg );
    },
    numeratorChanged: function ( arg ) {
      _setProperty( 'setNumerator', arg );
    },
    denominatorChanged: function ( arg ) {
      _setProperty( 'setDenominator', arg );
    },
    selectAtChanged: function ( arg ) {
      _setProperty( 'setStudyAreaSelectAt', arg );
    },
    resolutionChanged: function ( arg ) {
      _setProperty( 'setStudyAreaResolution', arg );
    },
    studyAreaSelectionEvent: function ( rows ) {
      _setProperty( 'setStudyAreas', RIF.utils.unique( rows ) );
    },
    healthSelectionChanged: function ( arg ) {
      if ( arg.length == 0 ) {
        arg = null;
      };
      _setProperty( 'setHealthOutcomes', arg );
    },
    startYearChanged: function ( arg ) {
      _setProperty( 'setMinYear', arg );
    },
    endYearChanged: function ( arg ) {
      _setProperty( 'setMaxYear', arg );
    },
    genderChanged: function ( arg ) {
      _setProperty( 'setGender', arg );
    },
    covariatesChanged: function ( arg ) {
      if ( arg.length == 0 ) {
        arg = null;
      };
      _setProperty( 'setCovariates', arg );
    },
    ageGroupsChanged: function ( arg ) {
      if ( arg.length <= 0 ) {
        arg = null;
      };
      _setProperty( 'setAgeGroups', arg );
    },
    projectChanged: function ( arg ) {
      _setProperty( 'setProject', arg );
    },
    studyDescriptionChanged: function ( arg ) {
      _setProperty( 'setDescription', arg );
    },
    isStudyReady: function () {
      mediatorUtils.isStudyReadyToBeSubmitted()
    },
    isInvestigationReady: function () {
      if ( mediatorUtils.isInvestigationReadyToBeSubmitted() ) {
        this.investigationReadyToBeAdded(); //firer
      } else {
        this.investigationNotReadyToBeAdded(); //firer
      }
    },
    addInvestigation: function () {
      if ( this.investigationReady ) {
        var nInvestigation = mediatorUtils.addCurrentInvestigation();
        this.addInvestigationRow( nInvestigation, _getProperty( 'getParameters' ) );
      }
    },
    removeInvestigationRow: function ( arg ) {
      mediatorUtils.removeInvestigation( arg );
    },
    clearAllParameters: function () {
      mediatorUtils.clearAllParameters()
    },
    /*
     *  Check if dialog is ready to be opened
     */

    isDialogReady: function ( dialog ) {
      var ready = false;
      if ( dialog == 'investigationDialog' ) {
        ready = mediatorUtils.isInvestigationDialogReady();
        if ( ready && this[ dialog ] != 1 ) {
          this.startInvestigationParameter( _getProperty( 'getNumerator' ) ); //firer
        };
      } else if ( dialog == 'studyAreaDialog' ) {
        ready = true;
        if ( ready && this[ dialog ] != 1 ) {
          this.startAreaSelection(); //firer
        };
      };

      if ( ready ) {
        this.showDialog( dialog );
        this[ dialog ] = 1;
      };
    },

    /*
     *  Check if all parameters for the specific
     *  dialog have been selected
     */

    isDialogSelectionComplete: function ( dialog ) {
      var previousState = {
        state: mediatorUtils.getDialogStatus( dialog )
      };
      var ready;

      if ( dialog == 'parametersModal' ) {
        ready = mediatorUtils.isInvestigationSelectionComplete( dialog );
      } else if ( dialog == 'areaSelectionModal' ) {
        ready = mediatorUtils.isStudyAreaSelectionComplete( dialog );
      };

      if ( previousState.state != ready ) {
        this.fire( 'dialogBgChange', dialog );
      };
    }
  };

  return subscriber;

} );