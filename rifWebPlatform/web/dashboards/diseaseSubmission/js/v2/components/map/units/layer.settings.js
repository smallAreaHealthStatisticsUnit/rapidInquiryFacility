RIF.map.layer.settings = (function(mysett, type) {
   var settings = {
      geoLevel: "",
      selectionField: "code",
      style: RIF.style(type, mysett.study),
      study: null
   };
   return RIF.extend(mysett, settings);
});