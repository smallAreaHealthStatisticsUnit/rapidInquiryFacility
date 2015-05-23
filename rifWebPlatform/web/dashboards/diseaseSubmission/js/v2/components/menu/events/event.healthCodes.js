RIF.menu['event-healthCodes'] = (function (_dom, firer) {

  var currentTaxonomy = null,
    index = 0,
    searchEnter = false,
    previousSearch = '',
    indexSelection = {},
    healthSelection = {}; //{ taxonomy: null , description: null, code: null }

  var clearIcdCodes = function () {
    $('.' + _dom.icdSelection).removeClass(_dom.icdSelection).css('background-color', 'white');
    $(_dom.hiddenIcdSelection).empty();
  };

  var checkTaxonomy = function (tax) {
    if (!healthSelection.hasOwnProperty(tax)) {
      healthSelection[tax] = [];
      indexSelection[tax] = [];
    };
  };

  var getTaxonomy = function () {
    return currentTaxonomy || _dom.icdClassification.val();
  };

  _dom.icdClassification.change(function () {
    currentTaxonomy = $(this).val();
    $('.taxonomySection').hide();
    firer.updateTopLevelHealthCodes(currentTaxonomy);
    previousSearch = '';
  });

  $(_dom.healthCodes).on("click", _dom.healthCodesHeader, function (aEvent) {
    var target = $(this);
    var spans = target.find('>span'),
      plusMinus = spans[0],
      childContainer = target.next('div')[0];
    if (target.hasClass('headerClicked')) {
      plusMinus.innerHTML = ' + ';
      childContainer.style.display = 'none';
      target.toggleClass('headerClicked');
      return;
    };
    if (childContainer.hasChildNodes()) {
      plusMinus.innerHTML = ' - ';
      childContainer.style.display = 'block';
      target.toggleClass('headerClicked');
      return;
    };
    var val = spans[1].innerHTML.trim();
    plusMinus.innerHTML = ' - ';
    target.toggleClass('headerClicked');

    var tax = getTaxonomy();
    firer.updateSubLevelHealthCodes({
      "taxonomy": tax,
      "code": val,
      "dom": childContainer
    });

  });

  $(_dom.tree).on("click", _dom.noChildElements, function (aEvent) {
    var spans = $(this).find('span'),
      code = spans[0].innerHTML.trim(),
      description = spans[1].innerHTML.trim();
    var classes = $(this).attr("class").toString().split(' '),
      classLength = classes.length,
      taxonomy = function () {
        while (classLength--) {
          if (classes[classLength] != 'icdSelected' && classes[classLength] != 'noChildElements') {
            return classes[classLength];
          };
        };
        return null;
      }();
    checkTaxonomy(taxonomy);
    if (indexSelection[taxonomy].indexOf(code) === -1) {
      $(this).css('background-color', 'rgba( 152,251,152	,0.2);');
      indexSelection[taxonomy].push(code);
      healthSelection[taxonomy].push({
        description: description,
        code: code
      });
      $(this).addClass('icdSelected');
    } else {
      //$( this ).css( 'background-color', 'white' );
      var i = indexSelection[taxonomy].indexOf(code);
      indexSelection[taxonomy].splice(i, 1);;
      healthSelection[taxonomy].splice(i, 1);
      if (healthSelection[taxonomy].length == 0) {
        delete healthSelection[taxonomy];
      };
      $(this).removeClass('icdSelected');
    };
    firer.healthSelectionChanged(healthSelection);
    firer.isInvestigationReady();
    copySelection();
  });

  _dom.clearAll.click(function () {
    clearIcdCodes();
    indexSelection = {};
    healthSelection = {};
    var taxonomy = getTaxonomy();
    firer.searchHealthCodes({
      taxonomy: taxonomy,
      searchTxt: '',
      dom: null
    });
  });

  _dom.searchCodeInput.keypress(function (event) {
    var keycode = (event.keyCode ? event.keyCode : event.which);
    if (keycode == '13') {
      var searchTxt = _dom.searchCodeInput.val(),
        taxonomy = currentTaxonomy || _dom.icdClassification.val(),
        domParent = document.getElementById(taxonomy);
      if (searchTxt === previousSearch) {
        return;
      };
      if (domParent === null) {
        RIF.statusBar('Could not find element to append health codes search', true, 'notify');
        return;
      };
      firer.searchHealthCodes({
        taxonomy: taxonomy,
        searchTxt: searchTxt
      });
      previousSearch = searchTxt;
    };
  });

  $(_dom.searchResults).on("click", '.opacityBackground', function (aEvent) {
    $(_dom.searchResults).hide();
  });

  var copySelection = function () {
    _dom.hiddenIcdSelection.empty();
    var taxonomy = getTaxonomy();
    for (var t in healthSelection) {
      for (var i = 0; i < healthSelection[t].length; i++) {
        _dom.hiddenIcdSelection.append("<div>" + healthSelection[t][i]['code'] + " - " + healthSelection[t][i]['description'] + "</div>")
      };
    }
  };
});