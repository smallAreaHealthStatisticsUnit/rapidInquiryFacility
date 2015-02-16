RIF.table['event-ageGroupsRenderer'] = (function(_dom) {
   var menuContext = this,
      slctd = [],
      isHighlighted,
      ageGroupClicked = false;
   _dom.rows.unbind('mouseover');
   _dom.rows.unbind('selectstart');
   _dom.rows.unbind('mousedown');
   _dom.investigationBox.unbind('mouseup');
   _dom.rows.mousedown(function() {
      menuContext.isMouseDown = true;
      $(this).toggleClass("rowSelected");
      isHighlighted = $(this).hasClass("rowSelected");
      ageGroupClicked = true;
      return false; // prevent text selection
   }).mouseover(function() {
      if (menuContext.isMouseDown) {
         $(this).toggleClass("rowSelected", isHighlighted);
      }
   }).bind("selectstart", function() {
      return false;
   });
   _dom.investigationBox.on('mouseup', _dom.ageGroupsWrapper, (function() {
      if (!ageGroupClicked) {
         return;
      };
      slctd = [];
      var r = d3.selectAll('#ageGroupsWrapper .rowSelected').each(function(d, i) {
         var idRow = (this.id).split('ageGroup')[1],
            bandRow = $(this).find('.ageBand').text();
         slctd.push({
            id: idRow,
            band: bandRow
         });
      });
      menuContext.facade.ageGroupsChanged(slctd);
      ageGroupClicked = false;
   }));
});