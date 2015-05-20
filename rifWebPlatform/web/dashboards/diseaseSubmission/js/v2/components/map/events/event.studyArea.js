RIF['map']['event-studyArea'] = (function(dom, firer) {

   var isDragging = false;
   var _getAreaSelection = function() {
      var slctd = [];
      var r = d3.selectAll('path.areaSelected').each(function(d, i) {
         slctd.push({
            id: d.properties["area_id"],
            gid: String(d.properties.gid), // USING GID FOR NOW RATHER THAN THE ACTUAL AREA ID ABOVE    
            label: d.properties.name
         });
      });
      return slctd;
   };


   $('#' + dom.id).on("mousedown", "path", function() {
      $(window).mousemove(function() {
         isDragging = true;
         $(window).unbind("mousemove");
      });
   }).on("mouseup", "path", function() {
      var wasDragging = isDragging;
      isDragging = false;
      $(window).unbind("mousemove");
      if (!wasDragging) { //was clicking
         var c = this.getAttribute("class");
         var newClass = (c == 'polygon') ? "areaSelected polygon" : "polygon";
         this.setAttribute("class", newClass);
         firer.studyMapAreaSelectionEvent(_getAreaSelection());
      };
   });


   $('#' + dom.id).on("mousemove", 'path', function(aEvent) {
      var xPos = aEvent.pageX + 40,
         yPos = aEvent.pageY - 50;
      dom.tooltip.css({
         'top': yPos,
         'left': xPos,
         'display': 'block'
      });
   });

   $('#' + dom.id).on("mouseout", function(aEvent) {
      dom.tooltip.hide();
   });

});