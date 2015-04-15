RIF.model = ( function ( type ) {

  var _study = {

      studyName: null,
      healthTheme: null,
      numerator: null,
      denominator: null,
      project: null,
      description: null,

      investigations: {},

      studyArea: {
        studyArea_resolution: null,
        studyArea_areas: [],
        studyArea_selectAt: null
      },
      comparisonArea: {
        comparisonArea_resolution: null,
        comparisonArea_areas: [],
        comparisonArea_selectAt: null
      }
    },

    _dialogsStatus = {
      areaSelectionModal: false,
      //comparisonArea: false,
      parametersModal: false,
      //stats: false     
    },

    _optionalVariables = [ "covariates", "description" ],

    _studyMethods = {

      investigationReady: false,
      investigationCounts: 0,

      parameters: {
        healthOutcomes: null,
        ageGroups: null,
        minYear: null,
        maxYear: null,
        gender: null,
        covariates: null,
      },

      showInvestigations: function () {
        for ( var l in _study.investigations ) {
          for ( var i in _study.investigations[ l ] ) {
            console.log( _study.investigations[ l ][ i ] );
          };
          console.log( '_____' );
        }
        console.log( '----------------------' );
        console.log( '----------------------' );
        console.log( '----------------------' );
      },
        
      addCurrentInvestigation: function () {
        var parametersClone = RIF.utils.extend( this.parameters, {} );
        _study.investigations[ this.investigationCounts ] = parametersClone;
        console.log( JSON.stringify( _study ) );
        // this.showInvestigations();  
        return this.investigationCounts++;
      },
        
      removeInvestigation: function ( i ) {
        if ( typeof _study.investigations[ i ] === 'object' ) {
          delete _study.investigations[ i ];
          console.log( 'Investigation ' + i + ' removed' )
        };
        // this.showInvestigations();  
      },
      //SETTERS
      setStudyName: function ( s ) {
        _study.studyName = s;
      },
      setHealthTheme: function ( s ) {
        _study.healthTheme = s;
      },
      setNumerator: function ( s ) {
        _study.numerator = s;
      },
      setDenominator: function ( s ) {
        _study.denominator = s;
      },
      setProject: function ( s ) {
        _study.project = s;
      },
      setDescription: function ( s ) {
        _study.description = s;
      },
      setStudyAreaSelectAt: function ( s ) {
        _study.studyArea.studyArea_selectAt = s;
      },
      setStudyAreaResolution: function ( s ) {
        _study.studyArea.studyArea_resolution = s;
      },
      setStudyAreas: function ( s ) {
        _study.studyArea.studyArea_areas = s;
      },
      setComparisonArea: function ( s ) {
        _study.comparisonArea.comparisonArea_resolution = s.resolution;
        _study.comparisonArea.comparisonArea_areas = s.areas;
        _study.comparisonArea.comparisonArea_selectAt = s.selectAt;
      },
      setHealthConditionTaxonomy: function ( s ) {
        this.parameters.taxonomy = s;
      },
      setHealthOutcomes: function ( s ) {
        this.parameters.healthOutcomes = s;
      },
      setMinYear: function ( s ) {
        this.parameters.minYear = s;
      },
      setMaxYear: function ( s ) {
        this.parameters.maxYear = s;
      },
      setGender: function ( s ) {
        this.parameters.gender = s;
      },
      setCovariates: function ( s ) {
        this.parameters.covariates = s;
      },
      setAgeGroups: function ( s ) {
        this.parameters.ageGroups = s;
      },
      setParameter: function ( param, s ) {
        this.parameters[ param ] = s;
      },
      //GETTERS
      getInvestigations: function () {
        return _study.investigations;
      },
      getStudyName: function () {
        return _study.studyName;
      },
      getHealthTheme: function () {
        return _study.healthTheme;
      },
      getDescription: function () {
        return _study.description;
      },

      getProject: function () {
        return _study.project;
      },

      getNumerator: function () {
        return _study.numerator;
      },
      getDenominator: function () {
        return _study.denominator;
      },
      getStudyAreas: function () {
        return _study.studyArea.studyArea_areas;;
      },
      getComparisonArea: function () {
        return _study.comparisonArea;
      },
      getHealthConditionTaxonomy: function () {
        return this.parameters.taxonomy;
      },
      getHealthOutcomes: function () {
        return this.parameters.healthOutcomes;
      },
      getMinYear: function () {
        return this.parameters.minYear;
      },
      getMaxYear: function () {
        return this.parameters.maxYear;
      },
      getGender: function () {
        return this.parameters.gender;
      },
      getCovariates: function () {
        return this.parameters.covariates;
      },
      getAgeGroups: function () {
        return this.parameters.ageGroups;
      },
      getParameters: function () {
        return this.parameters;
      },

      /* 
       * Model utils
       *
       */

      unsetParameter: function ( i, h ) {
        delete this.parameters[ i ][ h ];
      },

      setDialogStatus: function ( dialog, status ) {
        _dialogsStatus[ dialog ] = status;
      },

      getDialogStatus: function ( dialog ) {
        return _dialogsStatus[ dialog ];
      },

      isReadyAndNotify: function ( o ) {
        if ( jQuery.isEmptyObject( o ) ) {
          return false;
        };
        var toComplete = [],
          iterate = function ( o ) {
            for ( var i in o ) {
              if ( _studyMethods.isOptional( i ) ) {
                continue;
              };
              if ( o[ i ] == null || jQuery.isEmptyObject( o[ i ] ) ) {
                toComplete.push( i );
              } else if ( typeof o[ i ] == 'object' ) {
                iterate( o[ i ] );
              };
            };
          };
        iterate( o );
        return this.displayMissingParameters( toComplete );
      },

      isReady: function ( o ) {
        if ( jQuery.isEmptyObject( o ) ) {
          return false;
        };
        var toComplete = [],
          iterate = function ( o ) {
            for ( var i in o ) {
              if ( _studyMethods.isOptional( i ) ) {
                continue;
              };
              if ( o[ i ] == null || jQuery.isEmptyObject( o[ i ] ) ) {
                toComplete.push( i );
              } else if ( typeof o[ i ] == 'object' ) {
                iterate( o[ i ] );
              };
            };
          };
        iterate( o );
        return ( toComplete.length > 0 ) ? false : true;
      },

      isOptional: function ( p ) {
        var l = _optionalVariables.length;
        while ( l-- ) {
          if ( p == _optionalVariables[ l ] ) {
            return true;
          };
        }
        return false;
      },

      displayMissingParameters: function ( missing ) {
        if ( missing.length == 0 ) {
          return true;
        } else {
          var msg = 'Before continuing make sure the following parameters are set: <p> ' + missing.join( ", " ) + '</p>';
          RIF.statusBar( msg, true, 'notify' );
          return false;
        };
      },

      isStudyReadyToBeSubmitted: function () {
        var study = RIF.utils.extend( _studyMethods.parameters, _study );
        var ready = this.isReadyAndNotify( _study );
        console.log( "Ready:" + ready )
        return ready
      },



      /*
       * The following methods are invoked  when a tree is clicked
       * Some dialogs require certain parameter to be set before opening
       */

      isstudyAreaDialogReady: function () {},
      isComparisonAreaDialogReady: function () {},

      isInvestigationDialogReady: function () {
        var front = {
          studyName: _study.studyName,
          healthTheme: _study.healthTheme,
          numerator: _study.numerator,
          denominator: _study.denominator
        };
        var ready = this.isReadyAndNotify( front );
        return ready;
      },

      isStatDialogReady: function () {},




      /*
       * SELECTION COMPLETE CHECKS
       * The following methods are invoked  when a dialog is closed
       * Check if all parameters have been set for each dialog
       * Which then allows to singnal the completion of a specific dialog
       * And change of background image
       *
       */

      isStudyAreaSelectionComplete: function ( dialog ) {
        var r = this.isReady( _study.studyArea );
        this.setDialogStatus( dialog, true );
        return r;
      },

      isComparisonAreaSelectionComplete: function () {},
      isInvestigationSelectionComplete: function ( dialog ) {
        var r = this.isReady( _study.investigations );
        this.setDialogStatus( dialog, r );
        return r;
      },
      isStatSelectionComplete: function () {},

    };
    
  return RIF.utils.mix( _studyMethods, RIF.model[ 'observable-diseaseSubmission' ]() );
    
} );