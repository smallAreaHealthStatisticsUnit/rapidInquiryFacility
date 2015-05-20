RIF.menu['event-areaSelection'] = (function(_dom, firer) {

   _dom.selectAt.change(function() {
      var val = $(this).val();
      _dom.resolutionCountLabel.innerHTML = val + ' ';
      firer.selectAtChanged(val);

   });

   _dom.resolution.change(function() {
      var val = $(this).val();
      firer.resolutionChanged(val);
   });

   _dom.sync.click(function() {
      firer.syncStudyAreaButtonClicked();
   });

});