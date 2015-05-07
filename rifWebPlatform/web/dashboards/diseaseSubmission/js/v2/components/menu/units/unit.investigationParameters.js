RIF.menu[ 'unit-investigationParameters' ] = ( function ( _dom, menuUtils ) {

  var _p = {
    getYears: function ( years ) {
      menuUtils.dropDownInputText( years, _dom[ 'startYearAvailable' ] );
      menuUtils.dropDownInputText( years, _dom[ 'endYearAvailable' ] );
    },

    getCovariates: function ( covs ) {
      var elCovariates = _dom[ 'covariatesAvailable' ];
      menuUtils.checkboxesCovariates( covs, elCovariates, 'covariates' );
    },

    getGender: function ( genders ) {
      var elGender = _dom[ 'genderAvailable' ];
      menuUtils.dropDownInputText( genders, elGender );
    },

    investigationReadyToBeAdded: function () {
      _dom.addInvestigation.addClass( 'addInvestigationActive' );
    },

    investigationNotReadyToBeAdded: function () {
      _dom.addInvestigation.removeClass( 'addInvestigationActive' );
    },

  };

  return _p;
} );