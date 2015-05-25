RIF.table['unit-studyArea'] = (function (_dom, tableUtils) {


  var _show = function (fragment) {
    _dom.tableContent.appendChild(fragment);
    _dom.tableContent.style.display = 'block';
  };


  var _hide = function () {
    _dom.tableContent.style.display = 'none';
  };


  var _p = {
    getTabularData: function (data) {
      var fragment = tableUtils.renderTable(data, 'studyArea', _dom.selectionClass);
      _show(fragment);
    },
    updateCounter: function (nRows) {
      _dom.studyAreaCount.innerHTML = nRows;
    },
    getTabularDataFromMap: function (data) {
      var fragment = tableUtils.addSelected(data, 'studyArea', _dom.selectionClass);
      _show(fragment);
      _p.updateCounter(data.gid.length);
    },
    empty: function () {
      _hide();
      $(_dom.tableContent).empty();
      _p.updateCounter(0);
    },
    emptyPreserveSelection: function () {
      _hide()
      $(_dom.tableContent).find(">div:not(." + _dom.selectionClass + ")").remove();
    },
    clearAll: function () {
      $(_dom.tableContent).find(">div").removeClass(_dom.selectionClass);
      _p.updateCounter(0);
    },
    selectAll: function () {
      var rows = $(_dom.tableContent).find('>div');
      rows.addClass(_dom.selectionClass);
      _p.updateCounter(rows.length);
    },
    getSelection: function () {
      return tableUtils.getTableSelection(_dom.selectionClassD3Compatible)
    }
  };


  return _p;

});