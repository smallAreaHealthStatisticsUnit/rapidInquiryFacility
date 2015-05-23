RIF['menu']['unit-models'] = (function (_dom, menuUtils) {


  var _insertModel = function (model, id) {
    //_dom.modelsList
    var container = document.createElement("div"),
      name = model["codeRoutineName"],
      description = model["description"];

    var tickBox = "<input type='checkbox' name='" + name + "'  id='" + id + "' class='colsChoice'>" +
      "<label for='" + id + "' class='colsChoiceLbl' >" + name + "</label>";

    container.innerHTML = tickBox;

    var priorAvailable = model["parameterProxies"].length;
    for (var i = 0; i < priorAvailable; i++) {
      var priorContainer = document.createElement("div");
      var priorName = document.createElement("p");
      priorName.innerHTML = model["parameterProxies"][i]["name"];
      var input = document.createElement("input");
      input.type = "text";
      input.name = model["parameterProxies"][i]["name"];
      input.value = model["parameterProxies"][i]["value"];
      priorContainer.appendChild(priorName);
      priorContainer.appendChild(input);
      container.appendChild(priorContainer);
    };

    var description = document.createElement("p");
    description.innerHTML = model["description"];
    container.appendChild(description);

    _dom.modelsList.append(container);

  }

  var _p = {
    populateModels: function (data) {
      var l = data.length;
      while (l--) {
        var id = "model_" + l;
        _insertModel(data[l], id);
      }
    }
  };

  return _p;
});