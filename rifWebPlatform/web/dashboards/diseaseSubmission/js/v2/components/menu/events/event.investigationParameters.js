RIF.menu['event-investigationParameters'] = (function(_dom, firer, menuUtils) {
    
    var _isReady = function(){
        firer.isInvestigationReady();
    };    
    
   _dom.startYear.change(function() {
      var val = $(this).val();
      firer.startYearChanged(val);
      _isReady();   
   });
    
   _dom.endYear.change(function() {
      var val = $(this).val();
      firer.endYearChanged(val);
      _isReady();   
   });
    
   _dom.gender.change(function() {
      var val = $(this).val();
      firer.genderChanged(val);
      _isReady();   
   });
    
   _dom.covariatesWrapper.on("click", 'input', function(aEvent) {
      $(this).next().toggleClass('labelSelected');
      var val = menuUtils.getCheckedValues('covariates');
      firer.covariatesChanged(val);
      _isReady();   
   });
    
   _dom.addInvestigation.click(function() {
      firer.addInvestigation();
   });
    
   _dom.clearAll.click(function() {
      $('.' + _dom.inputBorderSelection).removeClass(_dom.inputBorderSelection);
      $('.' + _dom.labelSelected).removeClass(_dom.labelSelected);
      _dom.startYear.val("");
      _dom.endYear.val("");
      _dom.gender.val("");
      _dom.covariatesChecked().attr('checked', false); // Unchecks it
      firer.clearAllParameters();
      firer.investigationNotReadyToBeAdded();   
   });
});