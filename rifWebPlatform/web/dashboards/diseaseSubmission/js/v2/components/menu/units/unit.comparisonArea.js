RIF['menu']['unit-comparisonArea'] = (function (_dom, menuUtils) {

  var _p = {

    getSelectAt: function (data) {
      _dom.selectAt.val(data[0]);
      _dom.selectAt.change() //-- TEMPORARILY DISABLED
      menuUtils.dropDownInputText(data, _dom.selectAtAvailable);
      _dom.resolutionCountLabel.innerHTML = '';
    },

    getResolutions: function (data) {
      _dom.resolution.removeClass('inputBorderSelection').val('');
      menuUtils.dropDownInputText(data, _dom.resolutionAvailable);
    }

  };

  return _p;
});