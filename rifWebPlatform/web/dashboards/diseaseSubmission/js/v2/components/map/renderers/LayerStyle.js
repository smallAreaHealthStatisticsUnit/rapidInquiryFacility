/*
 *
 *
 */
RIF.LayerStyle = (function() {

   var style = {
      colors: {},
      default: {
         fill: "#9BCD9B",
         stroke: "#F8F8FF",
         transparency: 1
      },
      hover: {
         fill: "#1E90FF"
      },
      slctd: {
         fill: '#CD96CD',
         stroke: '#F8F8FF',
         "fill-opacity": 1
      }
   };

   this.style = {
      getDefaultStyle: function(id) {
         var s = "fill:" + style.default.fill +
            ";stroke:" + style.default.stroke;
         return s;
      },
      getSelectedStyle: function(id) {
         var s = "fill:" + style.slctd.fill +
            ";stroke:" + style.slctd.stroke +
            ";opacity:" + style.slctd.transparency +
            ";fill-opacity:" + style.slctd["fill-opacity"];
         return s;
      },

      highlight: function(id, isSlctd) {
         d3.select("#" + id).style(style.slctd);
      },

      unhighlight: function(id, isSlctd) {
         d3.select("#" + id).style(style.default);
      },

      unhighlightAll: function(mapContainerId) {
         d3.selectAll("#" + mapContainerId + " path")
            .style(style.default)
            .attr('class', 'polygon');
      }
   };


});