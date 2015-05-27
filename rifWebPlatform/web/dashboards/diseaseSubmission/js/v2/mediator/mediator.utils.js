RIF.mediator.utils = (function (modelAccessor) {

  var _mapExtentSet = false;

  var _utils = {

    investigationReady: false,
    investigationCounts: 0,

    setModelProperty: function (fn, prop) {
      modelAccessor[fn](prop);
    },

    getModelProperty: function (fn) {
      return modelAccessor[fn]();
    },

    clearAllParameters: function () {
      for (var i in modelAccessor.getParameters()) {
        modelAccessor.setParameter(i, null);
      };
    },

    addCurrentInvestigation: function () {
      var parametersClone = RIF.utils.extend(modelAccessor.parameters, {});
      var invs = modelAccessor.getInvestigations();
      invs[_utils.investigationCounts] = parametersClone;
      return _utils.investigationCounts++;
    },

    removeInvestigation: function (i) {
      var invs = modelAccessor.getInvestigations();
      if (typeof invs[i] === 'object') {
        modelAccessor.unsetInvestigation(i);
      };
    },
    /* 
     * Used to map data uniformly
     * between table and map
     *
     */
    tableToMap: function (tableSelection) {
      var gids = [],
        area_ids = [],
        names = [];

      tableSelection.map(function (o) {
        gids.push(o.gid);
        area_ids.push(o.id);
        names.push(o.label);
      });

      return {
        gid: gids,
        area_id: area_ids,
        name: names
      };
    },

    mapToTable: function (tableSelection) {
      var gids = [],
        area_ids = [],
        names = [];

      tableSelection.map(function (o) {
        gids.push(o.gid);
        area_ids.push(o["area_id"]);
        names.push(o.label);
      });
      return {
        gid: gids,
        area_id: area_ids,
        name: names
      };
    },

    setMapExtentStatus: function (status) {
      _mapExtentSet = status;
    },

    getMapExtentStatus: function (dialog) {
      return _mapExtentSet;
    },

    isReadyAndNotify: function (o) {
      if (jQuery.isEmptyObject(o)) {
        return false;
      };
      var toComplete = [],
        iterate = function (o) {
          for (var i in o) {
            if (_utils.isOptional(i)) {
              continue;
            };
            if (o[i] == null || jQuery.isEmptyObject(o[i])) {
              toComplete.push(i);
            } else if (typeof o[i] == 'object') {
              iterate(o[i]);
            };
          };
        };
      iterate(o);
      return this.displayMissingParameters(toComplete);
    },

    isReady: function (o) {
      if (jQuery.isEmptyObject(o)) {
        return false;
      };
      var toComplete = [],
        iterate = function (o) {
          for (var i in o) {
            if (_utils.isOptional(i)) {
              continue;
            };
            if (o[i] == null || jQuery.isEmptyObject(o[i])) {
              toComplete.push(i);
              console.log(jQuery.isEmptyObject(o[i]));
            } else if (typeof o[i] == 'object') {
              iterate(o[i]);
            };
          };
        };
      iterate(o);
      return (toComplete.length > 0) ? false : true;
    },

    isOptional: function (p) {
      var optional = modelAccessor.getOptionals();
      var l = optional.length;
      while (l--) {
        if (p == optional[l]) {
          return true;
        };
      }
      return false;
    },

    displayMissingParameters: function (missing) {
      if (missing.length == 0) {
        return true;
      } else {
        var msg = 'Before continuing make sure to complete the following: <p> ' + missing.join(", ") + '</p>';
        RIF.statusBar(msg, true, 'notify');
        return false;
      };
    },

    mapToSchema: function () {
      return modelAccessor.mapToSchema()
    },

    getGIDs: function (areas) {
      return areas.map(function (o) {
        return o.gid;
      });
      return gids;
    },

    areEqual: function (arr1, arr2) {
      arr1.sort();
      arr2.sort();
      if (arr1.length !== arr2.length)
        return false;
      for (var i = arr1.length; i--;) {
        if (arr1[i] !== arr2[i])
          return false;
      }
      return true;
    },

    syncMapTableNotification(dialog) {
      $('#' + dialog).show();
      RIF.statusBar('Please syncronize map with table or table with map', true, 'notify');
    }

  };


  RIF.mediator.validator.call(_utils, modelAccessor);

  return _utils;

});