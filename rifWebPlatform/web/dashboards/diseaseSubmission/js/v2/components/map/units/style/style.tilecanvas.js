RIF.style.tilecanvas = (function(type) {
   var stylecanvas = {
      style: function(id) {
         var c = this.default.fill;
         return {
            color: c,
            outline: {
               color: this.default.stroke,
               size: this.default["stroke-width"]
            }
         };
      },
      /* to be implemented */
      repaint: function(values) {}
   };
   return stylecanvas;
});