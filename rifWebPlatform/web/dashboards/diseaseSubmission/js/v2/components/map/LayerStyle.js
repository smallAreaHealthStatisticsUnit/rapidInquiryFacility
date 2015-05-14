/*
 * RIF.style extend RIF.style[ "tilesvg" | "tilecanvas" ] specific styles renderer for SVG or CANVAS
 * The following methods must be implemented by :
 *
 * @style(id)
 * @repaint()
 *
 */
RIF.LayerStyle = (function() {

   this.style = {
      colors: {},
      default: {
         fill: "#9BCD9B",
         stroke: "#F8F8FF",
         "stroke-width": 2,
      },
      hover: {
         fill: "#1E90FF"
      },
      slctd: {
         fill: 'rgb(242, 14, 41)',
         stroke: '#9BCD9B',
         "stroke-width": 0.1
      },
      getStyle: function(id, renderType) {
         var c = this.colors[id];
         this.setAreaColor(id, c);
         if (typeof c !== 'undefined') {
            var s = "fill:" + c +
               ";stroke:" + style.default.stroke +
               ";opacity:" + style.transparency;
            return s;
         };
         return this.style(id);
      },

      highlight: function(id, slctd) {
         var s = this.getLayerStyle(id, slctd);
         d3.select("#" + id).style({
            "fill": s.fill,
            "stroke": s.stroke,
            "stroke-width": s.stroke_width,
            "opacity": 1
         });
      }

   };

});