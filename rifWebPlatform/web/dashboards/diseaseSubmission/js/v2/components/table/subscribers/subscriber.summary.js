RIF['table']['subscriber-summary'] = (function (controller) {
  var subscriber = {
    modelToSchemaReady: function (modelToSchemaObj) {
      controller.updateSummary(modelToSchemaObj[0]);
    }

  };
  return subscriber;
});