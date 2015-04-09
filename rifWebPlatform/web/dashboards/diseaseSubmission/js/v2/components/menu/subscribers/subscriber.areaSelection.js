RIF.menu[ 'subscriber-areaSelection' ] = ( function ( controller ) {

  var subscriber = {
    startAreaSelection: function () {
      controller.getSelectAtsAvailable();
    },

    getResolutions: function ( selectAt ) {
      controller.getResolutionsAvailable( selectAt );
    }
  };

  return subscriber;
} );