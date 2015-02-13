RIF.style.tilesvg = (function(type) {
   var stylesvg = {
      paths: function() {
         return d3.select("svg.leaflet-zoom-animated").selectAll("path");
      },
      style: function(id) {
         var c = this.default.fill;
         this.setAreaColor(id, c);
         return "fill:" + c + ";stroke:" + this.default.stroke + ";stroke-width" + this.default["stroke-width"];
      },
      applyTransparency: function(val, selectionFill) {
         stylesvg.paths().style({
            'opacity': function() {
               return (this.style.fill == selectionFill) ? 1 : val;
            }
         });
      },
      repaint: function() {
         var style = this; // reference to parent RIF.style
         stylesvg.paths().each(function(d, i) {
            if (typeof d !== 'undefined') {
               var pathId = RIF.addG(d.id);
               this.style.fill = style.colors[pathId];
               this.style.stroke = style.default.stroke;
            }
         });
      }
   };
   return stylesvg;
});