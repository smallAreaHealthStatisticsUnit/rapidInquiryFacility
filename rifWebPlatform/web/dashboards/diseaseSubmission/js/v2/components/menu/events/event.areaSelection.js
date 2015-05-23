RIF.menu['event-areaSelection'] = (function (_dom, firer) {

  _dom.selectAt.change(function () {
    var val = $(this).val();
    _dom.resolutionCountLabel.innerHTML = val + ' ';
    firer.selectAtChanged(val);

  });

  _dom.resolution.change(function () {
    var val = $(this).val();
    firer.resolutionChanged(val);
  });

  _dom.syncMap.click(function () {
    firer.syncMapButtonClicked();
  });

  _dom.syncTable.click(function () {
    firer.syncTableButtonClicked();
  });

  _dom.selectAlRowsStudy.click(function () {
    firer.studySelectAllRowsClicked();
  });

  _dom.clearStudy.click(function () {
    firer.clearAreaSelectionEvent();
  });

});