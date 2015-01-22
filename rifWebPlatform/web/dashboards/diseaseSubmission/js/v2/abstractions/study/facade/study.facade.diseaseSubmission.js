RIF.study[ 'facade-diseaseSubmission' ] = ( function() {



  var facade = {


    //SuBSCRIBERS
    studyNameChanged: function( arg ) {
      this.setStudyName( arg );
    },
    healthThemeChanged: function( arg ) {
      this.setHealthTheme( arg );
    },
    numeratorChanged: function( arg ) {
      this.setNumerator( arg );
    },
    denominatorChanged: function( arg ) {
      this.setDenominator( arg );
    },
    selectAtChanged: function( arg ) {
      this.setStudyAreaSelectAt( arg );
      this.selectAtChangeUpdate( arg );
    },
    resolutionChanged: function( arg ) {
      this.setStudyAreaResolution( arg );
    },
    studyAreaSelectionEvent: function( rows ) {
      this.setStudyAreas( RIF.unique( rows ) );
    },
    icdSelectionChanged: function( arg ) {
      if ( arg.length == 0 ) {
        arg = null;
      };
      this.setHealthOutcomes( arg );
    },
    startYearChanged: function( arg ) {
      this.setMinYear( arg );
    },
    endYearChanged: function( arg ) {
      this.setMaxYear( arg );
    },
    genderChanged: function( arg ) {
      this.setGender( arg );
    },
    covariatesChanged: function( arg ) {
      if ( arg.length == 0 ) {
        arg = null;
      };
      this.setCovariates( arg );
    },
    ageGroupsChanged: function( arg ) {
      if ( arg.length <= 0 ) {
        arg = null;
      };
      this.setAgeGroups( arg );
    },

    isInvestigationReady: function() {
      for ( var i in this.parameters ) {
        if ( i == 'healthOutcomes' ) {
          for ( var h in this.parameters[ i ] ) {
            if ( this.parameters[ i ][ h ].length == 0 ) {
              delete this.parameters[ i ][ h ];
            };
          };
          if ( jQuery.isEmptyObject( this.parameters[ i ] ) ) {
            this.investigationNotReadyToBeAdded();
            return;
          };
        } else if ( i != 'covariates' && this.parameters[ i ] == null ) {
          this.investigationNotReadyToBeAdded();
          return;
        }
      };

      this.investigationReadyToBeAdded();
    },

    addInvestigation: function() {
      var nInvestigation = this.addCurrentInvestigation();
      this.fire( 'addInvestigationRow', [ nInvestigation, this.parameters ] );
    },

    removeInvestigationRow: function( arg ) {
      this.removeInvestigation( arg );
    },

    clearAllParameters: function() {
      for ( var i in this.parameters ) {
        this.parameters[ i ] = null;
      };
    },

    isDialogReady: function( dialog ) {
      var ready = ( dialog == 'investigationDialog' ) ?
        this.isInvestigationDialogReady() : false;

      if ( ready ) {
        this.showDialog( dialog );
      };
    },

    // FIRERS  
    selectAtChangeUpdate: function( geolvl ) {
      this.fire( 'selectAtChangeUpdate', geolvl );
    },

    investigationReadyToBeAdded: function() {
      this.fire( 'investigationReadyToBeAdded', null );
    },

    investigationNotReadyToBeAdded: function() {
      this.fire( 'investigationNotReadyToBeAdded', null );
    },

    showDialog: function( dialog ) {
      this.fire( 'showDialog', dialog );
    }


  };


  return facade;
} );