RIF.table['unit-ageGroups'] = (function (_dom) {

  var _renderTable = function (data) {

      _dom.tableContent.style.display = 'none';
      $(_dom.tableContent).empty();

      var fragment = document.createDocumentFragment(),
        names = data[0].name,
        lower = data[1].lowerAgeLimit,
        upper = data[2].upperAgeLimit,
        l = names.length;

      while (l--) {
        var oddOreven = (l % 2 == 0) ? 'even' : 'odd',
          div = document.createElement("div");
        div.className = 'aSR ' + oddOreven;
        div.id = 'ageGroup_' + l;
        div.innerHTML = '<div class="ageGroupName">' + names[l] + '</div><div class="ageBand">' + lower[l] + ' - ' + upper[l] + '</div>';
        fragment.appendChild(div);
      }

      _dom.tableContent.appendChild(fragment);
      _dom.tableContent.style.display = 'block';

    },

    _p = {
      getAgeGroups: function (data) {
        _renderTable(data);
        _dom.rows.change();
      }
    };

  return _p;
});