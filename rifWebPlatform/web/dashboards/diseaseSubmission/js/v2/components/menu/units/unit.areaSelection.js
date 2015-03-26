RIF.menu.areaSelection = (function(_dom, menuUtils) {
   var _p = {
         getSelectAt: function( data ) {
            menuUtils.dropDownInputText(data, _dom.selectAtAvailable);
            //menuUtils.proxy.studyAreaReady();
         },
         getResolutions: function(data) {
            _dom.resolution.removeClass('inputBorderSelection').val('');
            parent.dropDownInputText(data, _dom.resolutionAvailable);
         }
      };
   
    return _p
   
});