RIF['menu']['subscriber-comparisonArea'] = (function (controller) {

  var subscriber = {

    startComparisonAreaSelection: function () {
      controller.getSelectAtsAvailable();
    },

    comparisonGetResolutions: function (selectAt) {
      controller.getResolutionsAvailable(selectAt);
    }

  };

  return subscriber;
});