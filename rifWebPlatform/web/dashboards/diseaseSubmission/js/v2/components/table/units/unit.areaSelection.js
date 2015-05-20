RIF.table['unit-areaSelection'] = (function(_dom) {

   var _renderTable = function(data, toBeSelected) {
      var select = (typeof toBeSelected != 'undefined') ? _dom.selectionClass : '';
      var fragment = document.createDocumentFragment();

      var gids = data.gid,
         ids = data.area_id,
         labels = data.name,
         l = ids.length;
      while (l--) {
         var id = "studyArea_" + gids[l];
         if (document.getElementById(id) != null) {
            continue;
         };
         var oddOreven = (l % 2 == 0) ? 'even' : 'odd',
            div = document.createElement("div");
         div.className = 'aSR ' + oddOreven + ' ' + select;
         div.id = "studyArea_" + gids[l];
         div.innerHTML = '<div>' + ids[l] + '</div><div>' + labels[l] + '</div>';
         fragment.appendChild(div);
      };

      _dom.tableContent.appendChild(fragment);
      _dom.tableContent.style.display = 'block';
   };

   var _hide = function() {
      _dom.tableContent.style.display = 'none';
   };


   _p = {
      getTabularData: function(data) {
         _renderTable(data);
      },
      getTabularDataFromMap: function(data) {
         _renderTable(data, true);
      },

      empty: function() {
         _hide();
         $(_dom.tableContent).empty();
      },
      emptyPreserveSelection: function() {
         _hide()
         $(_dom.tableContent).find(">div:not(." + _dom.selectionClass + ")").remove();
      }
   };


   return _p;
});