/*
 *
 *
 *
 *
 *
 */
RIF.LayerStyle = (function() {

   var style = {
      colors: {},
      default: {
         fill: "#9BCD9B",
         stroke: "#F8F8FF",
         "stroke-width": 2,
         transparency: 1
      },
      hover: {
         fill: "#1E90FF"
      },
      slctd: {
         fill: '#FF4500',
         stroke: '#FFC125',
      }
   };

   this.style = {
      getDefaultStyle: function(id) {
         var s = "fill:" + style.default.fill +
            ";stroke:" + style.default.stroke +
            ";opacity:" + style.default.transparency;
         return s;
      },
      getSelectedStyle: function(id) {
         var s = "fill:" + style.slctd.fill +
            ";stroke:" + style.slctd.stroke;
         return s;
      },

      highlight: function(id, isSlctd) {
         d3.select("#" + id).style(style.slctd);
      },

      unhighlight: function(id, isSlctd) {
         d3.select("#" + id).style(style.default);
      },
   };


});