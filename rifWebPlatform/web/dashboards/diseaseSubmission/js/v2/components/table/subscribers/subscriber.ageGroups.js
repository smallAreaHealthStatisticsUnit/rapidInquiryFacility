RIF.table['subscriber-ageGroups'] = (function (controller) {

  var subscriber = {
    startInvestigationParameter: function (args /* [ { numerator,selectAt, resolution} ] */ ) {
      var numerator = args[0].numerator;
      controller.getAgeGroups(numerator);
    },

    clearAllParameters: function () {
      $('.rowSelected').removeClass('rowSelected');
    },

  };

  return subscriber;
});