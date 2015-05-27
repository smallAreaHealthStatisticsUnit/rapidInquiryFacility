RIF['table']['unit-summary'] = (function (_dom, menuUtils) {


  var summarizers = {
    geography: function (o) {
      var name = "<h5>Name</h5>" + _getDiv(o.name);
      var desc = "<h5> Description </h5>" + _getDiv(o.description);
      return name + desc;
    },

    disease_mapping_study_area: function (o) {
      var selectAt = "<h5>Select at</h5>" + _getDiv(o.geo_levels.geolevel_select.name);
      var resolution = "<h5>Resolution</h5>" + _getDiv(o.geo_levels.geolevel_view.name);
      var mapAreas = o.map_areas.map_area;
      var areas = "<h5>Map Areas</h5>";
      for (var i in mapAreas) {
        areas += _getDiv(mapAreas[i].id + ' - ' + mapAreas[i].label);
      };
      return selectAt + resolution + areas;
    },

    comparison_area: function (o) {
      var selectAt = "<h5>Select at</h5>" + _getDiv(o.geo_levels.geolevel_select.name);
      var resolution = "<h5>Resolution</h5>" + _getDiv(o.geo_levels.geolevel_view.name);
      var mapAreas = o.map_areas.map_area;
      var areas = "<h5>Map Areas</h5>";
      for (var i in mapAreas) {
        areas += _getDiv(mapAreas[i].id + ' - ' + mapAreas[i].label);
      };
      return selectAt + resolution + areas;
    },

    investigations: function () {
      var allinv = $('#allRowsInvestigations');
      return $(allinv).html();
    },

    calculation_method: function (o) {
      var cMethods = '';
      for (var i in o) {
        cMethods += '<h5>' + o[i]["code_routine_name"] + '</h5>';
        var params = o[i].parameters.parameter;
        for (var h in params) {
          cMethods += _getDiv(params[h].name + ' : ' + params[h].value);
        }
      };
      return cMethods;
    },
  };

  var _getDiv = function (v) {
    return "<div>" + v + "</div>";
  };

  var _checkForChildren = function (val) {

    if (typeof val !== 'object') {
      return val || '';
    };

    var child = '';
    for (var i in val) {
      var isUnd = (typeof summarizers[i] == 'undefined');
      var subChild = (isUnd) ? val[i] : summarizers[i](val[i]);
      child += "<h4>" + i + "</h4>" +
        _getDiv(subChild);
    }

    return child;
  };

  var _templateEntry = function (fieldName, value) {
    return "<div>" +
      "<h3>" + fieldName + "</h3>" +
      "<div>" +
      "<div>" + _checkForChildren(value) + "</div>" +
      "</div>" +
      "</div>";
  };


  var _render = function (modelToSchemaObj) {
    _dom.summaryStudy.empty();
    for (var i in modelToSchemaObj['rif_job_submission']) {
      var obj = modelToSchemaObj['rif_job_submission'][i];
      _dom.summaryStudy.append(_templateEntry(i, obj));

    };
  };


  var _p = {
    updateSummaryView: function (modelToSchemaObj) {
      _render(modelToSchemaObj);
    }
  };
  return _p;
});