RIF.table['event-studyArea'] = (function(_dom, firer) {

   var slctd = [],
      isMouseDown = false,
      shiftKeyDown = false,
      isHighlighted;

   var tableUtils = RIF.table.utils();

   var selectionClass = _dom.selectionClass;

   _dom.areaSelectionWrapper.on("mousedown", _dom.rows, function(aEvent) {
      var row = $(aEvent.target).parent();
      if (!row.hasClass(_dom.rowClass)) {
         return false;
      };
      isMouseDown = true;

      $(row).toggleClass(selectionClass);
      isHighlighted = $(row).hasClass(selectionClass);
      return false; // prevent text selection
   }).on("mouseover", _dom.rows, function(aEvent) {
      var row = $(aEvent.target).parent();
      if (!row.hasClass(_dom.rowClass)) {
         return false;
      };
      if (isMouseDown) {
         $(row).toggleClass(selectionClass, isHighlighted);
      }
   });

   _dom.areasSelectionDialog.on("mouseup", _dom.areaSelectionWrapper, function(aEvent) {
      if (isMouseDown) {
         var slctd = tableUtils.getTableSelection(_dom.selectionClassD3Compatible);
         firer.studyAreaSelectionEvent(slctd);
         _dom.studyAreaCount.innerHTML = slctd.length;
      }
      isMouseDown = false;
   });


});