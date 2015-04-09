RIF.menu[ 'subscriber-investigationParameters' ] = ( function ( controller ) {

  var subscriber = {
    startInvestigationParameter: function ( numerator ) {
      controller.startInvestigationParameter( numerator );
    },

    investigationReadyToBeAdded: function () {
      controller.investigationReadyToBeAdded();
    },

    investigationNotReadyToBeAdded: function () {
      controller.investigationNotReadyToBeAdded();
    }

  };

  return subscriber;
} );