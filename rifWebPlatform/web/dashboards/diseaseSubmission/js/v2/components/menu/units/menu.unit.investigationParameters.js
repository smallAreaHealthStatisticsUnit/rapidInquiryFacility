RIF.menu.investigationParameters = ( function( _dom ) {

  var parent = this,

    _requests = {

      getYears: function( user ) {
        RIF.getYearRange( _callbacks[ 'getYears' ], null );
      },
      getGender: function( params ) {
        RIF.getSexes( _callbacks[ 'getGender' ], null );
      },
      getCovariates: function() {
        RIF.getCovariates( _callbacks[ 'getCovariates' ], null );
      },
      getAgeGroups: function() {
        RIF.getAgeGroups( _callbacks[ 'getAgeGroups' ], null );
      }

    },

    _callbacks = {

      getYears: function() {
        var lower = [ this[ 0 ].lowerBound ],
          upper = [ this[ 0 ].upperBound ],
          elStart = _dom[ 'startYearAvailable' ],
          elEnd = _dom[ 'endYearAvailable' ],
          years = [],
          i = 0;
        while ( lower <= upper ) {
          years.push( lower++ );
        }
        parent.dropDownInputText( years, elStart );
        parent.dropDownInputText( years, elEnd );

        _requests.getGender();

      },

      getGender: function() {
        var genders = this[ 0 ].names,
          elGender = _dom[ 'genderAvailable' ];

        parent.dropDownInputText( genders, elGender );
        _requests.getCovariates();
      },

      getCovariates: function() {
        var elCovariates = _dom[ 'covariatesAvailable' ];
        parent.checkboxesCovariates( this, elCovariates, 'covariates' );
      }
    },

    /* geolevel obj */
    _p = {

      request: function( reqName, params ) {
        _requests[ reqName ]( params );
      },

    };


  return {
    investigationParameters: _p
  };
} );