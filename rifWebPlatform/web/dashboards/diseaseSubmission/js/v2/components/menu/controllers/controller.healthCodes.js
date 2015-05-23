RIF.menu['controller-healthCodes'] = (function (unit) {


  var _topLevelHealthCodesRequested = [],
    _reRequestTopLevelHealthCodes = function (taxonomy) {
      var i = _topLevelHealthCodesRequested.indexOf(taxonomy);
      if (i >= 0) {
        _topLevelHealthCodesRequested.splice(i, 1);
        return true;
      };
    };

  var _p = {

    getTaxonomy: function () {
      var clbk = function () {
        var entries = [],
          l = this.length;
        while (l--) {
          entries.push(this[l]['nameSpace']);
        };

        if (this.length > 0) {
          unit.getTaxonomy(entries);
          var firstTaxonomy = this[0]['nameSpace'];
          _p.getTopLevelHealthCodes(firstTaxonomy);
        };
      }
      RIF.getHealthTaxonomy(clbk, null);
    },

    getTopLevelHealthCodes: function (firstTaxonomy) {
      if (_topLevelHealthCodesRequested.indexOf(firstTaxonomy) != -1) {
        $('#' + firstTaxonomy).show();
        return;
      };
      _topLevelHealthCodesRequested.push(firstTaxonomy);
      var _callback = function () {
        unit.getTopLevelHealthCodes(firstTaxonomy, this);
      };

      RIF.getTopLevelHealthCodes(_callback, [firstTaxonomy]); // param hardcoded for now
    },

    getSubLevelHealthCodes: function (params) { // {taxonomy,code,dom}
      var _callback = function () {
        unit.getSubLevelHealthCodes(this, params.dom, params.taxonomy);
      };
      RIF.getSubHealthCodes(_callback, [params.taxonomy, params.code]);
    },

    getSearchHealthCodes: function (params) {
      var _callback = function () {
        unit.getSearchHealthCodes(this, params.taxonomy);
      };
      if (params.searchTxt == '') {
        _p.getTopLevelHealthCodes(params.taxonomy);
        return;
      };
      RIF.getSearchHealthCodes(_callback, [params.taxonomy, params.searchTxt]);
    }

  };



  return _p;
});