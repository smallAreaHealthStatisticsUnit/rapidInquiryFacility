RIF.menu.frontMapping = ( function( _dom ) {

  var parent = this,

    _requests = {

      getStudies: function() {
        RIF.getStudies( _callbacks.getStudies, [] );
      },

      getInvestigations: function( studyId ) {
        RIF.getInvestigations( _callbacks.getInvestigations, [ studyId ] );
      },

      getResultsSetAvailable: function() {
        RIF.getResultsSetAvailable( _callbacks.getResultsSetAvailable, [] );
      },

      getGenderAvailable: function() {
        RIF.getGenderAvailableForStudy( _callbacks.getGenderAvailable, [] );
      },

      getYearsAvailable: function() {
        RIF.getYearsAvailableForStudy( _callbacks.getYearsAvailable, [] );
      },
    },

    _callbacks = {

      getStudies: function() {
        if ( this.length > 0 ) {
          parent.dropDown( this, _dom.study );
          parent.proxy.studyReady( this );
          _requests.getInvestigations( this[ 0 ] );
        }
      },

      getInvestigations: function() {
        parent.dropDown( this, _dom.investigation );
        parent.proxy.investigationReady( this );
        _requests.getResultsSetAvailable();
      },

      getResultsSetAvailable: function() {
        parent.dropDown( this, _dom.resultSet );
        parent.proxy.resultSetsReady( this );
        _requests.getGenderAvailable();
      },

      getGenderAvailable: function() {
        parent.dropDown( this, _dom.gender );
        parent.proxy.genderReady( this );
        _requests.getYearsAvailable();
      },

      getYearsAvailable: function() {
        parent.dropDown( this, _dom.year );
        parent.proxy.yearReady( this );
      },

    }

  _p = {

    initdiseaseStudyLevel: function() {
      _requests.getStudies();
    },

    request: function( reqName, params ) {
      _requests[ reqName ]( params );
    },

    callback: function( clbkName, params ) {
      _callbacks[ clbkName ]( params );
    }

  };

  _p.initdiseaseStudyLevel();

  return {
    frontMapping: _p
  };
} );