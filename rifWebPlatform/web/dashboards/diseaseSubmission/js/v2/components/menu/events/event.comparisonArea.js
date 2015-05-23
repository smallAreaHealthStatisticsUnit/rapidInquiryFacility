RIF['menu']['event-comparisonArea'] = (function (_dom, firer) {

  _dom.selectAt.change(function () {
    var val = $(this).val();
    _dom.resolutionCountLabel.innerHTML = val + ' ';
    firer.comparisonSelectAtChanged(val);

  });

  _dom.resolution.change(function () {
    var val = $(this).val();
    firer.comparisonResolutionChanged(val);
  });

  /*_dom.syncMap.click( function () {    
    firer.comparisonSyncMapButtonClicked();
  }); 

  _dom.syncTable.click( function () {
    firer.studyMapAreaSelectionEvent();  
    firer.syncTableButtonClicked();
  }); 
    
  _dom.selectAlRowsStudy.click( function () {
      firer.studySelectAllRowsClicked();
  });
      
  _dom.clearStudy.click( function () {
      firer.clearAreaSelectionEvent();
  });*/

});