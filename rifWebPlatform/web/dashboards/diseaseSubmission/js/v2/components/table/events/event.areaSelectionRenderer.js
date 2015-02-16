RIF.table['event-areaSelectionRenderer'] = (function(_dom) {
   var menuContext = this,
      slctd = [],
      isHighlighted;
   _dom.rows.unbind('mouseover');
   _dom.rows.unbind('selectstart');
   _dom.rows.unbind('mousedown');
   _dom.rows.unbind('mouseup');
   _dom.rows.mousedown(function() {
      menuContext.isMouseDown = true;
      $(this).toggleClass("rowSelected");
      isHighlighted = $(this).hasClass("rowSelected");
      return false; // prevent text selection
   }).mouseup(function() {
      var slctd = [];
      if (menuContext.isMouseDown) {
         var r = d3.selectAll('#areaSelectionWrapper .rowSelected').each(function(d, i) {
            slctd.push(+this.id);
         });
         menuContext.facade.studyAreaSelectionEvent(slctd);
         _dom.studyAreaCount.innerHTML = slctd.length;
      }
   }).mouseover(function() {
      if (menuContext.isMouseDown) {
         $(this).toggleClass("rowSelected", isHighlighted);
      }
   }).bind("selectstart", function() {
      return false;
   })
});