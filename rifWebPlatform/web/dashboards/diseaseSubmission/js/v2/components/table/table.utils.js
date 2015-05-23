RIF.table.utils = (function () {
  var _p = {
    getTableSelection: function (selectionClassD3Compatible) {
      var slctd = [];
      var r = d3.selectAll(selectionClassD3Compatible).each(function (d, i) {
        var idLabel = $(this).children();
        var gid = this.id.split('_')[1];
        slctd.push({
          id: $(idLabel[0]).text(),
          gid: gid, // USING GID FOR NOW RATHER THAN THE ACTUAL AREA ID ABOVE    
          label: $(idLabel[1]).text()
        });
      });
      return slctd;
    }
  };
  return _p;
});