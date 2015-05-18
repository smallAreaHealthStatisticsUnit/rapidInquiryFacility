RIF.menu['unit-areaSelection'] = (function(_dom, menuUtils) {

   var _p = {

      getSelectAt: function(data) {
         _dom.selectAt.val(data[0]);
         _dom.selectAt.change()
         menuUtils.dropDownInputText(data, _dom.selectAtAvailable);
         _dom.resolutionCountLabel.innerHTML = '';
      },

      getResolutions: function(data) {
         _dom.resolution.removeClass('inputBorderSelection').val('');
         menuUtils.dropDownInputText(data, _dom.resolutionAvailable);
      }

   };

   return _p

});