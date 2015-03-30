RIF.menu.areaSelection = (function(_dom, menuUtils) {
   var _p = {
         getSelectAt: function( data ) {
            menuUtils.dropDownInputText(data, _dom.selectAtAvailable);
            _dom.resolutionCountLabel.innerHTML =  ''; 
            //menuUtils.proxy.studyAreaReady();
         },
         getResolutions: function(data) {
            _dom.resolution.removeClass('inputBorderSelection').val('');
            menuUtils.dropDownInputText(data, _dom.resolutionAvailable);
         }
      };
   
    return _p
   
});