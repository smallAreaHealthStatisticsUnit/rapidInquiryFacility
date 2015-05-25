RIF.table.utils = (function () {
  var _p = {

    getTableSelection: function (selectionClassD3Compatible) {
      var slctd = [];
      var r = d3.selectAll(selectionClassD3Compatible).each(function (d, i) {
        var idLabel = $(this).children();
        var gid = this.id.split('_')[1];
        slctd.push({
          id: $(idLabel[0]).text(),
          gid: gid,
          label: $(idLabel[1]).text()
        });
      });
      return slctd;
    },

    getChildDiv: function (id, label) {
      return '<div>' + id + '</div><div>' + label + '</div>';
    },

    getRowDiv: function (gid, i, isSelected, areaType, selectionClass) {
      var selected = (isSelected) ? ' ' + selectionClass : '';
      var id = areaType + "_" + gid;
      var oddOreven = (i % 2 == 0) ? 'even' : 'odd',
        div = document.createElement("div");
      div.className = 'aSR ' + oddOreven + selected;
      div.id = id;
      return div;
    },

    renderTable: function (data, areaType, selectionClass) {
      var fragment = document.createDocumentFragment();
      var gids = data.gid,
        ids = data["area_id"],
        labels = data.name,
        l = ids.length;
      while (l--) {
        var id = areaType + "_" + gids[l];
        if (document.getElementById(id) != null) {
          continue;
        };
        var div = this.getRowDiv(gids[l], l, false, areaType, selectionClass);
        div.innerHTML = this.getChildDiv(ids[l], labels[l]);
        fragment.appendChild(div);
      };
      return fragment;
    },


    addSelected: function (data, areaType, selectionClass) {
      var fragment = document.createDocumentFragment();
      var gids = data.gid,
        ids = data["area_id"],
        labels = data.name,
        l = ids.length;
      while (l--) {
        var div = this.getRowDiv(gids[l], l, true, areaType, selectionClass);
        div.innerHTML = this.getChildDiv(ids[l], labels[l]);
        fragment.appendChild(div);
      };
      return fragment;
    },


  };
  return _p;
});