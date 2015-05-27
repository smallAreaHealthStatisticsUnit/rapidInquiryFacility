RIF.menu['subscriber-investigationParameters'] = (function (controller) {

  var subscriber = {
    startInvestigationParameter: function (args /* [ { numerator,selectAt, resolution} ] */ ) {
      var numerator = args[0].numerator,
        selectAt = args[0].selectAt,
        resolution = args[0].resolution;
      controller.startInvestigationParameter(numerator, selectAt, resolution);
    },

    investigationReadyToBeAdded: function () {
      controller.investigationReadyToBeAdded();
    },

    investigationNotReadyToBeAdded: function () {
      controller.investigationNotReadyToBeAdded();
    }

  };

  return subscriber;
});