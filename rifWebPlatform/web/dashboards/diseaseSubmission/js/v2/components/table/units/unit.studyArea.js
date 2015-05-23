RIF.table['unit-studyArea'] = (function (_dom, tableUtils) {

  var _getRowDiv = function (gid, i, isSelected) {
    var selected = (isSelected) ? ' ' + _dom.selectionClass : '';
    var id = "studyArea_" + gid;
    var oddOreven = (i % 2 == 0) ? 'even' : 'odd',
      div = document.createElement("div");
    div.className = 'aSR ' + oddOreven + selected;
    div.id = id;
    return div;
  };


  var _getChildDiv = function (id, label) {
    return '<div>' + id + '</div><div>' + label + '</div>';
  };


  var _show = function (fragment) {
    _dom.tableContent.appendChild(fragment);
    _dom.tableContent.style.display = 'block';
  };


  var _hide = function () {
    _dom.tableContent.style.display = 'none';
  };

  var _renderTable = function (data) {
    var fragment = document.createDocumentFragment();
    var gids = data.gid,
      ids = data["area_id"],
      labels = data.name,
      l = ids.length;
    while (l--) {
      var id = "studyArea_" + gids[l];
      if (document.getElementById(id) != null) {
        continue;
      };
      var div = _getRowDiv(gids[l], l, false);
      div.innerHTML = _getChildDiv(ids[l], labels[l]);
      fragment.appendChild(div);
    };
    _show(fragment);
  };



  var _addSelected = function (data) {
    var fragment = document.createDocumentFragment();
    var gids = data.gid,
      ids = data["area_id"],
      labels = data.name,
      l = ids.length;
    while (l--) {
      var div = _getRowDiv(gids[l], l, true);
      div.innerHTML = _getChildDiv(ids[l], labels[l]);
      fragment.appendChild(div);
    };
    _show(fragment);
  };




  var _p = {
    getTabularData: function (data) {
      _renderTable(data);
    },
    updateCounter: function (nRows) {
      _dom.studyAreaCount.innerHTML = nRows;
    },
    getTabularDataFromMap: function (data) {
      _addSelected(data);
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